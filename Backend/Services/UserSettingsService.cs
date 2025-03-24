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

        public async Task<string> CreateAsync()
        {
            var userSettings = new UserSettings();
            await _userSettingsCollection.InsertOneAsync(userSettings);
            return userSettings.Id;
        }
    }
}