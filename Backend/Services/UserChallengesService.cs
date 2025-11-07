using MongoDB.Driver;
using AiRise.Models.User;

namespace AiRise.Services
{
    public class UserChallengesService
    {
        private readonly IMongoCollection<UserChallenges> _userChallengesCollection;
        private readonly IMongoCollection<UserChallenges> _col;

        public UserChallengesService(MongoDBService mongoDBService)
        {
            _col = _userChallengesCollection = mongoDBService.GetCollection<UserChallenges>("user.challenges");
            // Ensure unique index on firebaseUid
            var keys = Builders<UserChallenges>.IndexKeys.Ascending(x => x.FirebaseUid);
            _col.Indexes.CreateOne(new CreateIndexModel<UserChallenges>(keys, new CreateIndexOptions { Unique = true }));
        }

        // For unit tests
        public UserChallengesService(IMongoCollection<UserChallenges> collection)
        {
            _col = _userChallengesCollection = collection;
            var keys = Builders<UserChallenges>.IndexKeys.Ascending(x => x.FirebaseUid);
            _col.Indexes.CreateOne(new CreateIndexModel<UserChallenges>(keys, new CreateIndexOptions { Unique = true }));
        }

        public async Task<string> CreateAsync(string firebaseUid)
        {
            var userChallenges = new UserChallenges { FirebaseUid = firebaseUid };
            await _col.InsertOneAsync(userChallenges);
            return userChallenges.Id!;
        }

        public async Task<UserChallenges> GetAsync(string firebaseUid, CancellationToken ct = default)
        {
            var doc = await _col.Find(x => x.FirebaseUid == firebaseUid).FirstOrDefaultAsync(ct);
            return doc ?? new UserChallenges { FirebaseUid = firebaseUid };
        }

        public async Task<UserChallenges> GetOrCreateAsync(string firebaseUid, CancellationToken ct = default)
        {
            var doc = await _col.Find(x => x.FirebaseUid == firebaseUid)
                                                     .FirstOrDefaultAsync(ct);
            if (doc != null) return doc;

            doc = new UserChallenges { FirebaseUid = firebaseUid };
            await _col.InsertOneAsync(doc, cancellationToken: ct);
            return doc;
        }

        public async Task SetActiveAsync(string firebaseUid, string challengeId, CancellationToken ct = default)
        {
            var filter = Builders<UserChallenges>.Filter.Eq(x => x.FirebaseUid, firebaseUid);
            var update = Builders<UserChallenges>.Update.Set(x => x.ActiveChallengeId, challengeId);
            await _col.UpdateOneAsync(filter, update, new UpdateOptions { IsUpsert = true }, ct);
        }

        /// <summary>
        /// Idempotent: sets lastCompletionEpochDay to "today".
        /// If it's already today, it's a no-op.
        /// </summary>
        public async Task<UserChallenges> MarkCompleteTodayAsync(string firebaseUid, CancellationToken ct = default)
        {
            var today = TodayEpochDayUtc();
            var doc = await GetOrCreateAsync(firebaseUid, ct);

            if (doc.LastCompletionEpochDay == today)
                return doc; // idempotent

            var newStreak = (doc.LastCompletionEpochDay == today - 1)
                ? Math.Max(0, doc.StreakCount) + 1
                : 1;

            var filter = Builders<UserChallenges>.Filter.Eq(x => x.FirebaseUid, firebaseUid);
            var update = Builders<UserChallenges>.Update
                .Set(x => x.LastCompletionEpochDay, today)
                .Set(x => x.StreakCount, newStreak)
                .Unset(x => x.ActiveChallengeId);

            await _col.UpdateOneAsync(filter, update, new UpdateOptions { IsUpsert = true }, ct);

            doc.LastCompletionEpochDay = today;
            doc.StreakCount = newStreak;
            doc.ActiveChallengeId = null;
            return doc;
        }

        /// <summary>
        /// Clears the last completion marker (does NOT touch ActiveChallengeId).
        /// </summary>
        public async Task ClearCompletionAsync(string firebaseUid, CancellationToken ct = default)
        {
            var filter = Builders<UserChallenges>.Filter.Eq(x => x.FirebaseUid, firebaseUid);
            var update = Builders<UserChallenges>.Update.Unset(x => x.LastCompletionEpochDay);
            await _col.UpdateOneAsync(filter, update, new UpdateOptions { IsUpsert = true }, ct);
        }

        public async Task<bool> CompletedTodayAsync(string firebaseUid, CancellationToken ct = default)
        {
            var doc = await GetAsync(firebaseUid, ct);
            var today = (long)(DateTime.UtcNow.Date - DateTime.UnixEpoch).TotalDays;
            return doc.LastCompletionEpochDay.HasValue && doc.LastCompletionEpochDay.Value == today;
        }

        public Task ResetStreakAsync(string firebaseUid, CancellationToken ct = default)
        {
            var f = Builders<UserChallenges>.Filter.Eq(x => x.FirebaseUid, firebaseUid);
            var u = Builders<UserChallenges>.Update.Set(x => x.StreakCount, 0);
            return _col.UpdateOneAsync(f, u, cancellationToken: ct);
        }

        public Task<List<UserChallenges>> GetTopByStreakAsync(int topN, CancellationToken ct = default) =>
            _col.Find(FilterDefinition<UserChallenges>.Empty)
                .SortByDescending(x => x.StreakCount)
                .Limit(topN)
                .ToListAsync(ct);

        private static long TodayEpochDayUtc() =>
            (long)(DateTime.UtcNow.Date - DateTime.UnixEpoch).TotalDays;
    }
}
