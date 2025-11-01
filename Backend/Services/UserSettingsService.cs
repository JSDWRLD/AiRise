using AiRise.Models.User;
using MongoDB.Driver;
using MongoDB.Bson;

namespace AiRise.Services
{

    public class UserSettingsService
    {
        private readonly IMongoCollection<UserSettings> _userSettingsCollection;

        public UserSettingsService(MongoDBService mongoDBService)
        {
            _userSettingsCollection = mongoDBService.GetCollection<UserSettings>("user.settings");
        }

        public UserSettingsService(IMongoCollection<UserSettings> collection)
        {
            _userSettingsCollection = collection;
        }

        public async Task<string> CreateAsync(string firebaseUid)
        {
            var userSettings = new UserSettings();
            userSettings.FirebaseUid = firebaseUid;
            await _userSettingsCollection.InsertOneAsync(userSettings);
            return userSettings.Id;
        }

        public async Task<UserSettings?> GetUserSettings(string firebaseUid)
        {
            var existing = await _userSettingsCollection
            .Find(u => u.FirebaseUid == firebaseUid)
            .FirstOrDefaultAsync();

            if (existing != null)
                return existing;

            // Create defaults if nothing found
            var defaults = new UserSettings
            {
                FirebaseUid = firebaseUid,
                PictureUrl = "",
                AiPersonality = "default",
                ChallengeNotifsEnabled = true,
                FriendReqNotifsEnabled = true,
                StreakNotifsEnabled = true,
                MealNotifsEnabled = true
            };

            await _userSettingsCollection.InsertOneAsync(defaults);
            return defaults;
        }

        public async Task<bool> UpdateUserSettingsAsync(string firebaseUid, UserSettings updatedSettings)
        {
            var filter = Builders<UserSettings>.Filter.Eq(u => u.FirebaseUid, firebaseUid);
            var update = Builders<UserSettings>.Update
                .Set(u => u.PictureUrl, updatedSettings.PictureUrl)
                .Set(u => u.AiPersonality, updatedSettings.AiPersonality)
                .Set(u => u.ChallengeNotifsEnabled, updatedSettings.ChallengeNotifsEnabled)
                .Set(u => u.FriendReqNotifsEnabled, updatedSettings.FriendReqNotifsEnabled)
                .Set(u => u.StreakNotifsEnabled, updatedSettings.StreakNotifsEnabled)
                .Set(u => u.MealNotifsEnabled, updatedSettings.MealNotifsEnabled);

            var result = await _userSettingsCollection.UpdateOneAsync(filter, update);
            return result.ModifiedCount > 0;
        }

        /// <summary>
        /// Bulk fetch settings for many Firebase UIDs (used by leaderboard join).
        /// </summary>
        public async Task<List<UserSettings>> GetManyAsync(IEnumerable<string> firebaseUids, CancellationToken ct = default)
        {
            var list = firebaseUids?.ToList() ?? new List<string>();
            if (list.Count == 0) return new List<UserSettings>();

            var filter = Builders<UserSettings>.Filter.In(x => x.FirebaseUid, list);
            return await _userSettingsCollection.Find(filter).ToListAsync(ct);
        }

        /// <summary>
        /// Convenience: returns a UID→PictureUrl map (missing entries map to "").
        /// </summary>
        public async Task<Dictionary<string, string>> GetPictureUrlMapAsync(IEnumerable<string> firebaseUids, CancellationToken ct = default)
        {
            var settings = await GetManyAsync(firebaseUids, ct);
            return settings.ToDictionary(s => s.FirebaseUid, s => s.PictureUrl ?? string.Empty, StringComparer.Ordinal);
        }
    }
}