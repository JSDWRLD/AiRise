using AiRise.Data;
using AiRise.Models.User;
using AiRise.Models;
using MongoDB.Driver;

namespace AiRise.Services
{
    public interface IUserProgramService
    {
        // Create with explicit inputs (caller decides type + days)
        Task<string> CreateAsync(string firebaseUid, ProgramType type, List<string> workoutDays, CancellationToken ct = default);

        Task<UserProgramDoc?> GetAsync(string firebaseUid, CancellationToken ct = default);

        // Replace whole program (only weights/repsCompleted should differ in normal use)
        Task<bool> UpdateAsync(string firebaseUid, UserProgram mutatedProgram, CancellationToken ct = default);

        // Assign/reassign from explicit inputs (days + type + day names)
        Task<UserProgramDoc> AssignFromExplicitAsync(string firebaseUid, ProgramType type, List<string> workoutDays, ProgramPreferences? programPreferences = null, CancellationToken ct = default);

        // Only rename day labels (count must match); preserves weights/reps
        Task<bool> RelabelDayNamesAsync(string firebaseUid, List<string> workoutDays, ProgramPreferences? preferences = null, CancellationToken ct = default);

        // Re-Personalizes template with new preferences, preserving weights/reps
        Task<bool> UpdatePreferencesAsync(string firebaseUid, ProgramPreferences preferences, CancellationToken ct = default);
    }

    public class UserProgramService : IUserProgramService
    {
        private readonly IMongoCollection<UserProgramDoc> _userProgramCollection;

        [ActivatorUtilitiesConstructor]
        public UserProgramService(MongoDBService mongoDBService)
        {
            _userProgramCollection = mongoDBService.GetCollection<UserProgramDoc>("user.program");

            _userProgramCollection.Indexes.CreateOne(
                new CreateIndexModel<UserProgramDoc>(
                    Builders<UserProgramDoc>.IndexKeys.Ascending(x => x.FirebaseUid),
                    new CreateIndexOptions { Unique = true }
                ));
        }

        // CTOR for unit tests
        public UserProgramService(IMongoCollection<UserProgramDoc> collection)
        {
            _userProgramCollection = collection;
            _userProgramCollection.Indexes.CreateOne(
                new CreateIndexModel<UserProgramDoc>(
                    Builders<UserProgramDoc>.IndexKeys.Ascending(x => x.FirebaseUid),
                    new CreateIndexOptions { Unique = true }
                ));
        }

        public async Task<string> CreateAsync(string firebaseUid, ProgramType type, List<string> workoutDays, CancellationToken ct = default)
        {
            var existing = await _userProgramCollection.Find(x => x.FirebaseUid == firebaseUid).FirstOrDefaultAsync(ct);
            if (existing != null) return existing.Id!;

            // Validate / default workoutDays (3–6). If invalid, default to M/W/F.
            var daysList = (workoutDays ?? new()).Where(s => !string.IsNullOrWhiteSpace(s)).ToList();
            if (daysList.Count < 3 || daysList.Count > 6)
                daysList = new List<string> { "Monday", "Wednesday", "Friday" };

            var days = daysList.Count;

            var template = ProgramTemplatesData.Programs
                .Where(p => p.Days == days && p.Type == type)
                .OrderBy(p => p.Name)
                .FirstOrDefault()
                ?? ProgramTemplatesData.Programs
                    .Where(p => p.Days == days)
                    .OrderBy(p => p.Type).ThenBy(p => p.Name)
                    .FirstOrDefault();

            if (template == null)
                throw new InvalidOperationException($"No program template available for days={days}, type={type}.");

            var userProgram = ToUserProgram(template, daysList);
            var doc = new UserProgramDoc
            {
                FirebaseUid = firebaseUid,
                Program = userProgram,
                LastUpdatedUtc = DateTime.UtcNow
            };

            await _userProgramCollection.InsertOneAsync(doc, cancellationToken: ct);
            return doc.Id!;
        }

        public async Task<UserProgramDoc?> GetAsync(string firebaseUid, CancellationToken ct = default)
        {
            return await _userProgramCollection.Find(x => x.FirebaseUid == firebaseUid).FirstOrDefaultAsync(ct);
        }

        // Replace the whole program with a user-mutated copy (weights + repsCompleted are the only expected changes)
        public async Task<bool> UpdateAsync(string firebaseUid, UserProgram mutatedProgram, CancellationToken ct = default)
        {
            mutatedProgram.UpdatedAtUtc = DateTime.UtcNow;

            var filter = Builders<UserProgramDoc>.Filter.Eq(x => x.FirebaseUid, firebaseUid);
            var update = Builders<UserProgramDoc>.Update
                .Set(x => x.Program, mutatedProgram)
                .Set(x => x.LastUpdatedUtc, mutatedProgram.UpdatedAtUtc);

            var result = await _userProgramCollection.UpdateOneAsync(filter, update, cancellationToken: ct);
            return result.MatchedCount > 0 && result.ModifiedCount > 0;
        }

        // Explicit (re)assignment from a given days/type + day names
        public async Task<UserProgramDoc> AssignFromExplicitAsync(
            string firebaseUid,
            ProgramType type, List<string> workoutDays,
            ProgramPreferences? preferences = null,
            CancellationToken ct = default)
        {
            if (workoutDays is null || workoutDays.Count < 3 || workoutDays.Count > 6)
                throw new ArgumentOutOfRangeException(nameof(workoutDays), "Workout days must contain 3 to 6 entries.");

            var days = workoutDays.Count;
            var template = ProgramTemplatesData.Programs
                .Where(p => p.Days == days && p.Type == type)
                .OrderBy(p => p.Name)
                .FirstOrDefault();

            if (template == null)
                throw new InvalidOperationException($"No program template found for days={days}, type={type}.");

            var userProgram = ToUserProgram(template, workoutDays);

            if (preferences != null)
            {
                userProgram = Personalize(userProgram, preferences);
            }

            var filter = Builders<UserProgramDoc>.Filter.Eq(x => x.FirebaseUid, firebaseUid);
            var update = Builders<UserProgramDoc>.Update
                .SetOnInsert(x => x.FirebaseUid, firebaseUid)
                .Set(x => x.Program, userProgram)
                .Set(x => x.LastUpdatedUtc, DateTime.UtcNow);

            var options = new FindOneAndUpdateOptions<UserProgramDoc> { IsUpsert = true, ReturnDocument = ReturnDocument.After };
            return await _userProgramCollection.FindOneAndUpdateAsync(filter, update, options, ct);
        }

        // NEW: used when only the names changed (count same) — keeps weights/reps intact
        public async Task<bool> RelabelDayNamesAsync(string firebaseUid, List<string> workoutDays, ProgramPreferences? preferences = null, CancellationToken ct = default)
        {
            var doc = await GetAsync(firebaseUid, ct);
            if (doc?.Program?.Schedule == null || doc.Program.Schedule.Count == 0)
                return false;

            var normalized = workoutDays?.Where(s => !string.IsNullOrWhiteSpace(s))
                                         .Select(NormalizeDayName)
                                         .ToList() ?? new List<string>();

            if (normalized.Count != doc.Program.Days)
                return false;

            var sched = doc.Program.Schedule.OrderBy(d => d.DayIndex).ToList();
            for (int i = 0; i < sched.Count; i++)
            {
                sched[i].DayName = normalized[i];
            }
            if (preferences != null)
            {
                doc.Program = Personalize(doc.Program, preferences);
            }
            doc.Program.UpdatedAtUtc = DateTime.UtcNow;
            return await UpdateAsync(firebaseUid, doc.Program, ct);
        }

        public async Task<bool> UpdatePreferencesAsync(string firebaseUid, ProgramPreferences preferences, CancellationToken ct = default)
        {
            var doc = await GetAsync(firebaseUid, ct);
            if (doc?.Program == null)
                return false;

            var updatedProgram = Personalize(doc.Program, preferences);
            return await UpdateAsync(firebaseUid, updatedProgram, ct);
        }              

        // ===== internal helpers kept for this service =====

        private static UserProgram ToUserProgram(ProgramTemplate template, List<string> workoutDays)
        {
            var names = workoutDays.Select(NormalizeDayName).ToList();

            var up = new UserProgram
            {
                TemplateName = template.Name,
                Days = template.Days,
                Type = template.Type,
                CreatedAtUtc = DateTime.UtcNow,
                UpdatedAtUtc = DateTime.UtcNow
            };

            var ordered = template.Schedule.OrderBy(d => d.Day).ToList();
            for (int i = 0; i < ordered.Count; i++)
            {
                var tDay = ordered[i];
                var label = names[i % names.Count];

                var uDay = new UserProgramDay
                {
                    DayIndex = tDay.Day,
                    DayName = label,
                    Focus = tDay.Focus
                };

                foreach (var ex in tDay.Exercises)
                {
                    uDay.Exercises.Add(new UserExerciseEntry
                    {
                        Name = ex.Name,
                        Sets = ex.Sets,
                        TargetReps = ex.Reps,
                        RepsCompleted = 0,
                        Weight = new UserExerciseWeight
                        {
                            Value = 0,
                            Unit = ex.Weight?.Unit ?? "lbs"
                        }
                    });
                }

                up.Schedule.Add(uDay);
            }

            return up;
        }

        private static string NormalizeDayName(string s)
        {
            if (string.IsNullOrWhiteSpace(s)) return "Unspecified";
            s = s.Trim();
            return char.ToUpperInvariant(s[0]) + (s.Length > 1 ? s.Substring(1).ToLowerInvariant() : string.Empty);
        }

        private static UserProgram Personalize(
            UserProgram program,
            ProgramPreferences prefs,
            PersonalizationTuning tuning = null)
        {
            tuning ??= PersonalizationTuning.Default;

            // Compute once
            var goal = NormalizeGoal(prefs.WorkoutGoal);
            var targetSec = prefs.WorkoutLength * 60;
            var restSec = RestForGoal(goal, tuning);

            // Cache template baseline sets so we can respect MaxSetsBump
            var baseline = program.Schedule
                .SelectMany(d => d.Exercises.Select(ex => new { Key = $"{d.DayIndex}|{ex.Name}", ex.Sets }))
                .ToDictionary(x => x.Key, x => x.Sets);

            // 1) Goal-based rep range shifts (no set changes here)
            foreach (var ex in program.Schedule.SelectMany(d => d.Exercises))
                AdjustRepRangeForGoal(ex, goal);

            // 2) Time scaling (mild duration tweak first, then sets)
            foreach (var day in program.Schedule)
                AdjustDayToTarget(day, goal, restSec, targetSec, baseline, tuning);

            program.UpdatedAtUtc = DateTime.UtcNow;
            return program;
        }

        // ===== Personalize Helpers =====

        private enum Goal { MuscleGain, WeightLoss, Maintain }

        private static Goal NormalizeGoal(string s)
        {
            var k = (s ?? "").Trim().ToLowerInvariant();
            if (k.Contains("gain")) return Goal.MuscleGain;
            if (k.Contains("loss") || k.Contains("lose")) return Goal.WeightLoss;
            return Goal.Maintain;
        }

        private static int RestForGoal(Goal goal, PersonalizationTuning t) => goal switch
        {
            Goal.MuscleGain => t.RestGainMuscleSec,
            Goal.WeightLoss => t.RestLoseWeightSec,
            _ => t.RestMaintainSec
        };

        private static void AdjustRepRangeForGoal(UserExerciseEntry ex, Goal goal)
        {
            var range = ParseRepRange(ex.TargetReps);
            if (!range.HasValue) return;

            var (low, high) = range.Value;
            switch (goal)
            {
                case Goal.MuscleGain:
                    low = Math.Max(5, low - 2); // min rep range is 5
                    high = Math.Max(low + 1, high - 2);
                    break;
                case Goal.WeightLoss:
                    low += 4;
                    high = Math.Max(low + 1, high + 4);
                    break;
                case Goal.Maintain:
                default:
                    break;
            }
            ex.TargetReps = ToRepRange((low, high));
        }

        private static void AdjustDayToTarget(
            UserProgramDay day,
            Goal goal,
            int restSec,
            int targetSec,
            IReadOnlyDictionary<string, int> baseline,
            PersonalizationTuning t)
        {
            const double TOLERANCE = 5; // seconds
            double daySec = EstimateDaySeconds(day, goal, restSec, t);

            // Gentle duration scaling for timed entries before touching sets
            if (Math.Abs(daySec - targetSec) > TOLERANCE)
                daySec += TryScaleTimedEntries(day, t, scaleUp: daySec < targetSec);

            // Precompute per-ex metrics
            var perSetInfos = day.Exercises.Select(ex => new
            {
                Ex = ex,
                Key = $"{day.DayIndex}|{ex.Name}",
                PerSetSec = EstimatePerSetSeconds(ex, goal, restSec, t),
                Baseline = baseline[$"{day.DayIndex}|{ex.Name}"]
            }).ToList();

            // If over time: remove sets from the most time-expensive exercises (bounded by MinSets)
            while (daySec > targetSec + TOLERANCE)
            {
                var cand = perSetInfos
                    .Where(i => i.Ex.Sets > t.MinSets)
                    .OrderByDescending(i => i.PerSetSec)
                    .FirstOrDefault();
                if (cand == null) break;

                cand.Ex.Sets--;
                daySec -= cand.PerSetSec;
            }

            // If under time: add sets up to (baseline + MaxSetsBump)
            while (daySec < targetSec - TOLERANCE)
            {
                var cand = perSetInfos
                    .Where(i => i.Ex.Sets < i.Baseline + t.MaxSetsBump)
                    .OrderBy(i => Math.Abs(daySec + i.PerSetSec - targetSec)) // best fit first
                    .ThenByDescending(i => i.PerSetSec)                         // tie-breaker
                    .FirstOrDefault();

                if (cand == null) break;

                cand.Ex.Sets++;
                daySec += cand.PerSetSec;
            }
        }

        private static double EstimateDaySeconds(UserProgramDay day, Goal goal, int restSec, PersonalizationTuning t) =>
            day.Exercises.Sum(ex =>
            {
                var perSetWork = EstimatePerSetWorkSeconds(ex, goal, t);
                var restCount  = Math.Max(0, ex.Sets - 1);         
                return ex.Sets * perSetWork + restCount * restSec;
            });

        // Per-set increment = work for one set + one rest interval that follows it
        private static double EstimatePerSetSeconds(UserExerciseEntry ex, Goal goal, int restSec, PersonalizationTuning t) =>
            EstimatePerSetWorkSeconds(ex, goal, t) + restSec;

        private static double EstimatePerSetWorkSeconds(UserExerciseEntry ex, Goal goal, PersonalizationTuning t)
        {
            var range = ParseRepRange(ex.TargetReps);
            if (range.HasValue)
            {
                var (low, high) = range.Value;
                int reps = (low + high) / 2;
                return reps * t.SecondsPerRep;
            }

            var secs = ParseSecondsLiteral(ex.TargetReps);
            if (secs.HasValue) return secs.Value;

            // Unknown (e.g., "AMRAP"): treat as 45s work
            return 45;
        }

        // Return a SIGNED delta applied to the day (positive if scaled up, negative if scaled down)
        private static double TryScaleTimedEntries(UserProgramDay day, PersonalizationTuning t, bool scaleUp)
        {
            double signedDelta = 0;
            foreach (var ex in day.Exercises)
            {
                var secs = ParseSecondsLiteral(ex.TargetReps);
                if (!secs.HasValue) continue;

                var original = (double)secs.Value;
                var min = original * t.TimeSetScaleMin;
                var max = original * t.TimeSetScaleMax;

                var newVal = scaleUp ? Math.Min(max, original * 1.20)
                                    : Math.Max(min, original * 0.80);

                if (Math.Abs(newVal - original) <= 0.5) continue;

                ex.TargetReps = $"{(int)Math.Round(newVal)} sec";
                signedDelta += (newVal - original) * ex.Sets;
            }
            return signedDelta;
        }

        private static int? ParseSecondsLiteral(string reps)
        {
            if (string.IsNullOrWhiteSpace(reps)) return null;
            reps = reps.Trim().ToLowerInvariant()
                    .Replace("seconds", "sec")
                    .Replace("s", " sec");
            var toks = reps.Split(' ', StringSplitOptions.RemoveEmptyEntries | StringSplitOptions.TrimEntries);
            return (toks.Length >= 1 && int.TryParse(toks[0], out var n)) ? n : (int?)null;
        }

        private static (int low, int high)? ParseRepRange(string reps)
        {
            if (string.IsNullOrWhiteSpace(reps)) return null;
            var parts = reps.Split('-', StringSplitOptions.RemoveEmptyEntries | StringSplitOptions.TrimEntries);
            if (parts.Length != 2) return null;
            return (int.TryParse(parts[0], out var low) && int.TryParse(parts[1], out var high)) ? (low, high) : ((int, int)?)null;
        }

        private static string ToRepRange((int low, int high) r) => $"{r.low}-{r.high}";

    }
    // HELPER FOR USER DATA
    public static class ProgramTypeMapper
    {
        public static ProgramType MapEquipmentToProgramType(string? equipmentCsvOrKey)
        {
            var s = (equipmentCsvOrKey ?? string.Empty).Trim().ToLowerInvariant();

            // Support CSV (multi-select) and single keys
            var tokens = s.Split(',', StringSplitOptions.RemoveEmptyEntries | StringSplitOptions.TrimEntries)
                          .Select(x => x.ToLowerInvariant())
                          .ToHashSet();

            // Priority: Gym > HomeDumbbell > Bodyweight
            if (tokens.Contains("gym") || s == "gym") return ProgramType.Gym;
            if (tokens.Contains("home") || tokens.Contains("dumbbell") || tokens.Contains("home_dumbbell") || s is "home" or "dumbbell" or "home_dumbbell")
                return ProgramType.HomeDumbbell;
            if (tokens.Contains("bodyweight") || tokens.Contains("body weight") || s is "bodyweight" or "body weight")
                return ProgramType.Bodyweight;

            return ProgramType.Bodyweight;
        }
    }
}
