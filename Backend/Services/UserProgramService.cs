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
        Task<UserProgramDoc> AssignFromExplicitAsync(string firebaseUid, ProgramType type, List<string> workoutDays, CancellationToken ct = default);

        // Only rename day labels (count must match); preserves weights/reps
        Task<bool> RelabelDayNamesAsync(string firebaseUid, List<string> workoutDays, CancellationToken ct = default);
    }

    public class UserProgramService : IUserProgramService
    {
        private readonly IMongoCollection<UserProgramDoc> _userProgramCollection;

        public UserProgramService(MongoDBService mongoDBService)
        {
            _userProgramCollection = mongoDBService.GetCollection<UserProgramDoc>("user.program");

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
        public async Task<UserProgramDoc> AssignFromExplicitAsync(string firebaseUid, ProgramType type, List<string> workoutDays, CancellationToken ct = default)
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

            var filter = Builders<UserProgramDoc>.Filter.Eq(x => x.FirebaseUid, firebaseUid);
            var update = Builders<UserProgramDoc>.Update
                .SetOnInsert(x => x.FirebaseUid, firebaseUid)
                .Set(x => x.Program, userProgram)
                .Set(x => x.LastUpdatedUtc, DateTime.UtcNow);

            var options = new FindOneAndUpdateOptions<UserProgramDoc> { IsUpsert = true, ReturnDocument = ReturnDocument.After };
            return await _userProgramCollection.FindOneAndUpdateAsync(filter, update, options, ct);
        }

        // NEW: used when only the names changed (count same) — keeps weights/reps intact
        public async Task<bool> RelabelDayNamesAsync(string firebaseUid, List<string> workoutDays, CancellationToken ct = default)
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

            doc.Program.UpdatedAtUtc = DateTime.UtcNow;
            return await UpdateAsync(firebaseUid, doc.Program, ct);
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
