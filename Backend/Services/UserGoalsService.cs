using MongoDB.Driver;
using MongoDB.Bson;
using AiRise.Models.User;

namespace AiRise.Services
{
    public class UserGoalsService
    {
        private readonly IMongoCollection<UserGoals> _userGoalsCollection;

        public UserGoalsService(MongoDBService mongoDBService)
        {
            _userGoalsCollection = mongoDBService.GetCollection<UserGoals>("user.goals");
        }

        public async Task<string> CreateAsync(string firebaseUid)
        {
            var userGoals = new UserGoals();
            userGoals.FirebaseUid = firebaseUid;
            await _userGoalsCollection.InsertOneAsync(userGoals);
            return userGoals.Id;
        }
    }
}