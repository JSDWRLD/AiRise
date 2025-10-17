using AiRise.Models.User;
using Microsoft.Extensions.Options;
using MongoDB.Driver;
using MongoDB.Bson;
using AiRise.Services;

namespace AiRise.Services
{
    public class UserService 
    {
        private readonly IMongoCollection<User> _userCollection;
        private readonly UserFriendsService _userFriendsService;
        private readonly UserDataService _userDataService;
        private readonly UserSettingsService _userSettingsService;
        private readonly UserChallengesService _userChallengesService;
        private readonly IUserHealthDataService _userHealthDataService;

        public UserService(
            MongoDBService mongoDBService,
            UserFriendsService userFriendsService,
            UserDataService userDataService,
            UserSettingsService userSettingsService,
            UserChallengesService userChallengesService,
            IUserHealthDataService userHealthDataService
        )
        {
            _userCollection = mongoDBService.GetCollection<User>("users");
            _userFriendsService = userFriendsService;
            _userDataService = userDataService;
            _userSettingsService = userSettingsService;
            _userChallengesService = userChallengesService;
            _userHealthDataService = userHealthDataService;
        }

        public async Task CreateAsync(User user)
        {
            user.Friends = await _userFriendsService.CreateAsync(user.FirebaseUid);
            user.Data = await _userDataService.CreateAsync(user.FirebaseUid);
            user.Settings = await _userSettingsService.CreateAsync(user.FirebaseUid);
            user.Challenges = await _userChallengesService.CreateAsync(user.FirebaseUid);
            user.HealthData = await _userHealthDataService.CreateAsync(user.FirebaseUid);

            await _userCollection.InsertOneAsync(user);
            
            return;
        }

        // Returns a list of all users
        public async Task<List<User>> GetAsync()
        {
            return await _userCollection.Find(new BsonDocument()).ToListAsync();
        }

        public async Task UpdateFirebaseUidAsync(string id, string firebaseUid)
        {
            var filter = Builders<User>.Filter.Eq(u => u.Id, id);
            var update = Builders<User>.Update.Set(u => u.FirebaseUid, firebaseUid);
            await _userCollection.UpdateOneAsync(filter, update);
        }

        public async Task ResetStreakAsync(string firebaseUid)
        {
            var filter = Builders<User>.Filter.Eq(u => u.FirebaseUid, firebaseUid);
            var update = Builders<User>.Update.Set(u => u.Streak, 0);
            await _userCollection.UpdateOneAsync(filter, update);
        }


        public async Task UpdateStreakByOneAsync(string firebaseUid)
        {
            var filter = Builders<User>.Filter.Eq(u => u.FirebaseUid, firebaseUid);
            var update = Builders<User>.Update.Inc(u => u.Streak, 1);
            await _userCollection.UpdateOneAsync(filter, update);
        }

        public async Task UpdateStreakAsync(string firebaseUid, int streak)
        {
            var filter = Builders<User>.Filter.Eq(u => u.FirebaseUid, firebaseUid);
            var update = Builders<User>.Update.Set(u => u.Streak, streak);
            await _userCollection.UpdateOneAsync(filter, update);
        }

        public async Task DeleteUserAsync(string id)
        {
            FilterDefinition<User> filter = Builders<User>.Filter.Eq("Id", id);
            await _userCollection.DeleteOneAsync(filter);
            return;
        }

        public async Task<User?> GetUserByFirebaseUidAsync(string firebaseUid)
        {
            return await _userCollection.Find(u => u.FirebaseUid == firebaseUid).FirstOrDefaultAsync();
        }

        public async Task<User?> GetUserByIdAsync(string id)
        {
            return await _userCollection.Find(u => u.Id == id).FirstOrDefaultAsync();
        }

    }
}