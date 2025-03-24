using MongoDB.Driver;
using MongoDB.Bson;
using AiRise.Models.User;

namespace AiRise.Services
{
    public class UserHealthDataService
    {
        private readonly IMongoCollection<UserHealthData> _userHealthDataCollection;

        public UserHealthDataService(MongoDBService mongoDBService)
        {
            _userHealthDataCollection = mongoDBService.GetCollection<UserHealthData>("user.healthdata");
        }

        public async Task<string> CreateAsync()
        {
            var userHealthData = new UserHealthData();
            await _userHealthDataCollection.InsertOneAsync(userHealthData);
            return userHealthData.Id;
        }
    }
}