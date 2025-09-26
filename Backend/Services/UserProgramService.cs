using AiRise.Data;
using AiRise.Models.User;
using AiRise.Models;
using MongoDB.Driver;
using System.Text.RegularExpressions;
using ZstdSharp.Unsafe;

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
        Task<bool> UpdatePreferencesAsync(string firebaseUid, ProgramType type, List<string> workoutDays, ProgramPreferences preferences, CancellationToken ct = default);
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

        public async Task<bool> UpdatePreferencesAsync(string firebaseUid, ProgramType type, List<string> workoutDays, ProgramPreferences preferences, CancellationToken ct = default)
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

            var updatedProgram = Personalize(userProgram, preferences);
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
            var reps = ParseTargetReps(ex.TargetReps);
            if (reps.Kind == TargetKind.Amrap || reps.Kind == TargetKind.Unknown)
                return;

            var (low, high) = (reps.Low, reps.High);
            switch (goal)
            {
                case Goal.MuscleGain:
                    low = (int)Math.Round(low * 0.75);
                    high = (int)Math.Round(high * 0.75);
                    break;
                case Goal.WeightLoss:
                    low = (int)Math.Round(low * 1.25);
                    high = (int)Math.Round(high * 1.25);
                    break;
                case Goal.Maintain:
                default:
                    break;
            }
            low = Math.Max(1, low);
            high = Math.Max(low, high); // ensure valid range

            ex.TargetReps = ToRepRange((low, high, reps.Unit));
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
                daySec += TryScaleEntries(day, t, scaleUp: daySec < targetSec);

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
                var restCount = Math.Max(0, ex.Sets - 1);
                return ex.Sets * perSetWork + restCount * restSec;
            });

        // Per-set increment = work for one set + one rest interval that follows it
        private static double EstimatePerSetSeconds(UserExerciseEntry ex, Goal goal, int restSec, PersonalizationTuning t) =>
            EstimatePerSetWorkSeconds(ex, goal, t) + restSec;

        private static double EstimatePerSetWorkSeconds(UserExerciseEntry ex, Goal goal, PersonalizationTuning t)
        {
            var reps = ParseTargetReps(ex.TargetReps);
            if (reps.Kind != TargetKind.Amrap && reps.Kind != TargetKind.Unknown)
            {
                var (low, high) = (reps.Low, reps.High);
                int avgReps = (low + high) / 2;
                return avgReps * t.SecondsPerRep;
            }

            // Unknown (e.g., "AMRAP"): treat as 45s work
            return 45;
        }

        // Return a SIGNED delta applied to the day (positive if scaled up, negative if scaled down)
        private static double TryScaleEntries(UserProgramDay day, PersonalizationTuning t, bool scaleUp)
        {
            double signedDelta = 0;
            foreach (var ex in day.Exercises)
            {
                var reps = ParseTargetReps(ex.TargetReps);

                var low = reps.Low;
                var min_low = low * t.TimeSetScaleMin;
                var max_low = low * t.TimeSetScaleMax;

                var newLow = scaleUp ? Math.Min(max_low, low * 1.25)
                                    : Math.Max(min_low, low * 0.75);

                if (Math.Abs(newLow - low) <= 0.5) continue;

                var high = reps.High;
                var min_high = high * t.TimeSetScaleMin;
                var max_high = high * t.TimeSetScaleMax;

                var newHigh = scaleUp ? Math.Min(max_high, high * 1.25)
                                    : Math.Max(min_high, high * 0.75);

                if (Math.Abs(newHigh - high) <= 0.5) continue;

                ex.TargetReps = $"{(int)Math.Round(newLow)}-{(int)Math.Round(newHigh)} {reps.Unit}";
                signedDelta += (newHigh + newLow) / 2 * ex.Sets;
            }
            return signedDelta;
        }



        private static string ToRepRange((int Low, int High, string Unit) reps) => $"{reps.Low}-{reps.High} {reps.Unit}".Trim();
        private enum TargetKind { RepRange, ValueRangeUnit, Amrap, Unknown }

        private sealed class TargetSpec
        {
            public TargetKind Kind { get; private set; }
            public int Low { get; private set; }     // for rep range low
            public int High { get; private set; }    // for rep range high
            public string Unit { get; private set; } = string.Empty;  // for value range unit (e.g. seconds)
            public int Sets { get; private set; }    // for sets-of-reps (e.g. 3x8)
            public string Raw { get; private set; }

            private TargetSpec() { }

            public static TargetSpec RepRange(int low, int high) => new TargetSpec { Kind = TargetKind.RepRange, Low = low, High = high, Raw = $"{low}-{high}" };
            public static TargetSpec ValueRange(int low, int high, string unit) => new TargetSpec { Kind = TargetKind.ValueRangeUnit, Low = low, High = high, Unit = unit, Raw = $"{low}-{high} {unit}" };
            public static TargetSpec Amrap() => new TargetSpec { Kind = TargetKind.Amrap, Raw = "AMRAP" };
            public static TargetSpec Unknown(string raw) => new TargetSpec { Kind = TargetKind.Unknown, Raw = raw };
        }
        /// <summary>
        /// Parse a target reps string into a TargetSpec that describes rep ranges, seconds, amrap, or sets-of-reps.
        /// </summary>
        private static TargetSpec ParseTargetReps(string reps)
        {
            if (string.IsNullOrWhiteSpace(reps)) return TargetSpec.Unknown(reps ?? string.Empty);
            var txt = reps.Trim();

            // AMRAP
            if (Regex.IsMatch(txt, @"^\s*amrap\s*$", RegexOptions.IgnoreCase)) return TargetSpec.Amrap();

            // Rep range e.g. "8-10"
            var rangeMatch = Regex.Match(txt, @"^(?<low>\d+)\s*-\s*(?<high>\d+)\s*(?<attr>/\w)?$");
            if (rangeMatch.Success && int.TryParse(rangeMatch.Groups["low"].Value, out var low) && int.TryParse(rangeMatch.Groups["high"].Value, out var high))
                return TargetSpec.RepRange(low, high);

            // Entries with units: seconds, yds, etc.
            var valueUnitMatch = Regex.Match(txt, @"^(?<low>\d+)\s*-\s*(?<high>\d+)\s*(?<unit>\w+)?\s*$", RegexOptions.IgnoreCase);
            if (valueUnitMatch.Success
                && int.TryParse(valueUnitMatch.Groups["low"].Value, out var low_val)
                && int.TryParse(valueUnitMatch.Groups["high"].Value, out var high_val))
            {
                var unit = valueUnitMatch.Groups["unit"].Success ? valueUnitMatch.Groups["unit"].Value : string.Empty;
                return TargetSpec.ValueRange(low_val, high_val, unit);
            }

            // Unknown fallback
            return TargetSpec.Unknown(txt);
        }

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
