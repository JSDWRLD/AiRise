using MongoDB.Driver;
using MongoDB.Bson;
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
            var userChallenges = new UserChallenges();
            userChallenges.FirebaseUid = firebaseUid;
            await _userChallengesCollection.InsertOneAsync(userChallenges);
            return userChallenges.Id;
        }
    }
}