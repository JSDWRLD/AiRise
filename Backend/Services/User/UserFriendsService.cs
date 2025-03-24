using AiRise.Models.User;
using MongoDB.Driver;
using MongoDB.Bson;

namespace AiRise.Services
{

    public class UserSettingsService
    {
        private readonly IMongoCollection<User> _userCollection;
        private readonly IMongoCollection<UserFriends> _userFriendsCollection;


        public UserSettingsService(MongoDBService mongoDBService)
        {
            _userCollection = mongoDBService.GetCollection<User>("users"); // Use the Users collection
            _userFriendsCollection = mongoDBService.GetCollection<UserFriends>("user.friends");
        }

        public async Task<string> CreateAsync()
        {
            var userFriends = new UserFriends();
            await _userFriendsCollection.InsertOneAsync(userFriends);
            return userFriends.Id;
        }
    }
}