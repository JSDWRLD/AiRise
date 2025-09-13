using MongoDB.Driver;
using AiRise.Models.User;

namespace AiRise.Services
{
    public class UserChallengesService
    {
        private readonly IMongoCollection<UserChallenges> _userChallengesCollection;

        public UserChallengesService(MongoDBService mongoDBService)
        {
            _userChallengesCollection = mongoDBService.GetCollection<UserChallenges>("user.challenges");
        }

        public async Task<string> CreateAsync(string firebaseUid)
        {
            var userChallenges = new UserChallenges { FirebaseUid = firebaseUid };
            await _userChallengesCollection.InsertOneAsync(userChallenges);
            return userChallenges.Id!;
        }

        public async Task<UserChallenges> GetAsync(string firebaseUid, CancellationToken ct = default)
        {
            var doc = await _userChallengesCollection.Find(x => x.FirebaseUid == firebaseUid)
                                                     .FirstOrDefaultAsync(ct);
            return doc ?? new UserChallenges { FirebaseUid = firebaseUid };
        }

        public async Task<UserChallenges> GetOrCreateAsync(string firebaseUid, CancellationToken ct = default)
        {
            var doc = await _userChallengesCollection.Find(x => x.FirebaseUid == firebaseUid)
                                                     .FirstOrDefaultAsync(ct);
            if (doc != null) return doc;

            doc = new UserChallenges { FirebaseUid = firebaseUid };
            await _userChallengesCollection.InsertOneAsync(doc, cancellationToken: ct);
            return doc;
        }

        public async Task SetActiveAsync(string firebaseUid, string challengeId, CancellationToken ct = default)
        {
            var filter = Builders<UserChallenges>.Filter.Eq(x => x.FirebaseUid, firebaseUid);
            var update = Builders<UserChallenges>.Update.Set(x => x.ActiveChallengeId, challengeId);
            await _userChallengesCollection.UpdateOneAsync(filter, update, new UpdateOptions { IsUpsert = true }, ct);
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
                return doc; // already marked today

            var filter = Builders<UserChallenges>.Filter.Eq(x => x.FirebaseUid, firebaseUid);
            var update = Builders<UserChallenges>.Update.Set(x => x.LastCompletionEpochDay, today);

            await _userChallengesCollection.UpdateOneAsync(filter, update, new UpdateOptions { IsUpsert = true }, ct);
            doc.LastCompletionEpochDay = today;
            return doc;
        }

        /// <summary>
        /// Clears the last completion marker (does NOT touch ActiveChallengeId).
        /// </summary>
        public async Task ClearCompletionAsync(string firebaseUid, CancellationToken ct = default)
        {
            var filter = Builders<UserChallenges>.Filter.Eq(x => x.FirebaseUid, firebaseUid);
            var update = Builders<UserChallenges>.Update.Unset(x => x.LastCompletionEpochDay);
            await _userChallengesCollection.UpdateOneAsync(filter, update, new UpdateOptions { IsUpsert = true }, ct);
        }

        public async Task<bool> CompletedTodayAsync(string firebaseUid, CancellationToken ct = default)
        {
            var doc = await GetAsync(firebaseUid, ct);
            var today = (long)(DateTime.UtcNow.Date - DateTime.UnixEpoch).TotalDays;
            return doc.LastCompletionEpochDay.HasValue && doc.LastCompletionEpochDay.Value == today;
        }

        private static long TodayEpochDayUtc() =>
            (long)(DateTime.UtcNow.Date - DateTime.UnixEpoch).TotalDays;
    }
}
