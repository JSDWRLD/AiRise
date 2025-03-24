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

        public async Task<string> CreateAsync()
        {
            var userChallenges = new UserChallenges();
            await _userChallengesCollection.InsertOneAsync(userChallenges);
            return userChallenges.Id;
        }
    }
}