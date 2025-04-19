using AiRise.Models.User;
using MongoDB.Driver;
using MongoDB.Bson;

namespace AiRise.Services
{

    public class UserSettingsService
    {
        private readonly IMongoCollection<User> _userCollection;
        private readonly IMongoCollection<UserSettings> _userSettingsCollection;

        public UserSettingsService(MongoDBService mongoDBService)
        {
            _userCollection = mongoDBService.GetCollection<User>("users"); // Use the Users collection
            _userSettingsCollection = mongoDBService.GetCollection<UserSettings>("user.settings");
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
            return await _userSettingsCollection.Find(u => u.FirebaseUid == firebaseUid).FirstOrDefaultAsync();
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

        /*
        public async Task<bool> UpdateUserDataAsync(string firebaseUid, UserSettings updatedSettings)
        {
            var filter = Builders<UserSettings>.Filter.Eq(u => u.FirebaseUid, firebaseUid);
            var updates = new List<UpdateDefinition<UserSettings>>();

            if (updatedSettings.PictureUrl != null)
            {
                updates.Add(Builders<UserSettings>.Update.Set(u => u.PictureUrl, updatedSettings.PictureUrl));
            }

            if (updatedSettings.AiPersonality != null)
            {
                updates.Add(Builders<UserSettings>.Update.Set(u => u.AiPersonality, updatedSettings.AiPersonality));
            }

            if (updatedSettings.StreakNotifsEnabled.HasValue) //
            {
                updates.Add(Builders<UserSettings>.Update.Set(u => u.StreakNotifsEnabled, true));
            }


            if (updates.Count == 0)
            {
                // No updates to apply
                return false;
            }

            var combinedUpdate = Builders<UserSettings>.Update.Combine(updates);
            var result = await _userSettingsCollection.UpdateOneAsync(filter, combinedUpdate);
            return result.ModifiedCount > 0;
        }
        */
    }
}