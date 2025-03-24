using AiRise.Models.User;
using MongoDB.Driver;
using MongoDB.Bson;

namespace AiRise.Services
{

    public class UserFriendsService
    {
        private readonly IMongoCollection<User> _userCollection;
        private readonly IMongoCollection<UserFriends> _userFriendsCollection;


        public UserFriendsService(MongoDBService mongoDBService)
        {
            _userCollection = mongoDBService.GetCollection<User>("users"); // Use the Users collection
            _userFriendsCollection = mongoDBService.GetCollection<UserFriends>("user.friends");
        }

        public async Task<string> CreateAsync(string firebaseUid)
        {
            var userFriends = new UserFriends();
            userFriends.FirebaseUid = firebaseUid;
            await _userFriendsCollection.InsertOneAsync(userFriends);
            return userFriends.Id;
        }
    }
}