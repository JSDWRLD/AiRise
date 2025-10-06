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

        public async Task<string> CreateAsync(string firebaseUid)
        {
            var userHealthData = new UserHealthData();
            userHealthData.FirebaseUid = firebaseUid;
            await _userHealthDataCollection.InsertOneAsync(userHealthData);
            return userHealthData.Id;
        }

        public async Task<UserHealthData?> GetUserHealthDataAsync(string firebaseUid)
        {
            return await _userHealthDataCollection.Find(u => u.FirebaseUid == firebaseUid).FirstOrDefaultAsync();
        }

        public async Task<bool> UpdateUserHealthDataAsync(string firebaseUid, UserHealthData updatedData)
        {
            if (updatedData == null)
            {
                throw new ArgumentNullException(nameof(updatedData));
            }

            // Validate and adjust data before updating
            updatedData = await verifyUserHealthData(firebaseUid, updatedData);

            var filter = Builders<UserHealthData>.Filter.Eq(u => u.FirebaseUid, firebaseUid);
            var update = Builders<UserHealthData>.Update
                .Set(u => u.CaloriesBurned, updatedData.CaloriesBurned)
                .Set(u => u.CaloriesEaten, updatedData.CaloriesEaten)
                .Set(u => u.CaloriesTarget, updatedData.CaloriesTarget)
                .Set(u => u.Steps, updatedData.Steps)
                .Set(u => u.Sleep, updatedData.Sleep)
                .Set(u => u.Hydration, updatedData.Hydration)
                .Set(u => u.HydrationTarget, updatedData.HydrationTarget)
                .Set(u => u.LastUpdatedAt, DateTime.UtcNow);
            var result = await _userHealthDataCollection.UpdateOneAsync(filter, update);
            return result.IsAcknowledged && result.ModifiedCount > 0;
        }
        private async Task<UserHealthData> verifyUserHealthData(string firebaseUid, UserHealthData data)
        {
            UserHealthData? oldData = await _userHealthDataCollection.Find(u => u.FirebaseUid == firebaseUid).FirstOrDefaultAsync();
            if (oldData == null)
            {
                throw new ArgumentException("No existing health data found for the given user.");
            }

            if (oldData.LastUpdatedAt.Date < DateTime.UtcNow.Date)
            {
                //Reset daily fields if last update was on a previous day
                oldData.CaloriesBurned = 0;
                oldData.CaloriesEaten = 0;
                oldData.Steps = 0;
                oldData.Sleep = 0;
                oldData.Hydration = 0;
                oldData.LastUpdatedAt = DateTime.UtcNow;
            }

            if (data.CaloriesBurned < 0 || data.CaloriesEaten < 0 || data.CaloriesTarget < 0 || data.Steps < 0 || data.Sleep < 0 || data.Hydration < 0 || data.HydrationTarget < 0)
            {
                throw new ArgumentException("Health data values cannot be negative.");
            }

            if (data.CaloriesBurned == 0) data.CaloriesBurned = oldData.CaloriesBurned;
            if (data.CaloriesEaten == 0) data.CaloriesEaten = oldData.CaloriesEaten;
            if (data.CaloriesTarget == 0) data.CaloriesTarget = oldData.CaloriesTarget;
            if (data.Steps == 0) data.Steps = oldData.Steps;
            if (data.Sleep == 0) data.Sleep = oldData.Sleep;
            if (data.Hydration == 0) data.Hydration = oldData.Hydration;
            if (data.HydrationTarget == 0) data.HydrationTarget = oldData.HydrationTarget;
            // Add more validation rules as needed
            return data;
        }
    }
}