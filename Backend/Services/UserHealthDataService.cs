using MongoDB.Driver;
using AiRise.Models.User;
using AiRise.Models.DTOs;
using MongoDB.Bson;

namespace AiRise.Services
{
    public interface IUserHealthDataService
    {
        Task<string> CreateAsync(string firebaseUid);
        Task<UserHealthData?> GetUserHealthDataAsync(string firebaseUid);
        Task<bool> UpdateUserHealthDataAsync(string firebaseUid, HealthData updatedData);
        Task<bool> UpdateUserHealthTargetsAsync(string firebaseUid, int? caloriesTarget, int? hydrationTarget);
    }
    public class UserHealthDataService : IUserHealthDataService
    {
        private readonly IMongoCollection<UserHealthData> _userHealthDataCollection;

        [ActivatorUtilitiesConstructor]
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
        // CTOR for unit tests
        public UserHealthDataService(IMongoCollection<UserHealthData> collection)
        {
            _userHealthDataCollection = collection;
            _userHealthDataCollection.Indexes.CreateOne(
                new CreateIndexModel<UserHealthData>(
                    Builders<UserHealthData>.IndexKeys.Ascending(x => x.FirebaseUid),
                    new CreateIndexOptions { Unique = true }
                ));
        }

        public async Task<UserHealthData?> GetUserHealthDataAsync(string firebaseUid)
        {
            var existing = await _userHealthDataCollection
                .Find(u => u.FirebaseUid == firebaseUid)
                .FirstOrDefaultAsync();

            if (existing != null)
                return existing;

            // Nothing found → create a default record using your existing helper
            var id = await CreateAsync(firebaseUid);

            // Fetch the newly inserted record and return it
            return await _userHealthDataCollection
                .Find(u => u.FirebaseUid == firebaseUid)
                .FirstOrDefaultAsync();
        }

        // Update the user's health data. This does not update the user's targets for calories or hydration.
        // It only updates the daily tracked values.
        // If field is left null or 0, it will not be updated. Date must be provided in "YYYY-MM-DD" format.
        public async Task<bool> UpdateUserHealthDataAsync(string firebaseUid, HealthData updatedData)
        {
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
                .Set(u => u.LocalDate, updatedData.LocalDate);
            var result = await _userHealthDataCollection.UpdateOneAsync(filter, update);
            return result.IsAcknowledged && result.ModifiedCount > 0;
        }

        public async Task<bool> UpdateUserHealthTargetsAsync(string firebaseUid, int? caloriesTarget, int? hydrationTarget)
        {
            var filter = Builders<UserHealthData>.Filter.Eq(u => u.FirebaseUid, firebaseUid);
            var updateDef = Builders<UserHealthData>.Update;
            var sets = new List<UpdateDefinition<UserHealthData>>();
            if (caloriesTarget.HasValue && caloriesTarget.Value > 0)
            {
                sets.Add(updateDef.Set(u => u.CaloriesTarget, caloriesTarget.Value));
            }

            if (hydrationTarget.HasValue && hydrationTarget.Value > 0)
            {
                sets.Add(updateDef.Set(u => u.HydrationTarget, hydrationTarget.Value));
            }

            if (sets.Count == 0)
            {
                throw new ArgumentException("At least one target value must be provided and greater than zero.");
            }

            var update = updateDef.Combine(sets);

            var result = await _userHealthDataCollection.UpdateOneAsync(filter, update);
            return result.IsAcknowledged && result.ModifiedCount > 0;
        }

        // Private Helper Methods

        // Validates and adjusts the incoming health data
        private async Task<HealthData> verifyUserHealthData(string firebaseUid, HealthData data)
        {
            UserHealthData? oldData = await _userHealthDataCollection.Find(u => u.FirebaseUid == firebaseUid).FirstOrDefaultAsync();
            if (oldData == null)
            {
                throw new ArgumentException("No existing health data found for the given user.");
            }

            //Reset daily fields if last update was on a previous day
            if (oldData.LocalDate < data.LocalDate)
            {
                oldData.CaloriesBurned = 0;
                oldData.CaloriesEaten = 0;
                oldData.Steps = 0;
                oldData.Sleep = 0;
                oldData.Hydration = 0;
            }

            // Ensure no negative values
            if (data.CaloriesBurned < 0 || data.CaloriesEaten < 0 || data.CaloriesTarget < 0 ||
                data.Steps < 0 || data.Sleep < 0 || data.Hydration < 0 || data.HydrationTarget < 0)
            {
                throw new ArgumentException("Health data values cannot be negative.");
            }

            // If any field is null or 0, retain the old value
            if (data.CaloriesBurned == null || data.CaloriesBurned == 0) data.CaloriesBurned = oldData.CaloriesBurned;
            if (data.CaloriesEaten == null || data.CaloriesEaten == 0) data.CaloriesEaten = oldData.CaloriesEaten;
            if (data.CaloriesTarget == null || data.CaloriesTarget == 0) data.CaloriesTarget = oldData.CaloriesTarget;
            if (data.Steps == null || data.Steps == 0) data.Steps = oldData.Steps;
            if (data.Sleep == null || data.Sleep == 0) data.Sleep = oldData.Sleep;
            if (data.Hydration == null || data.Hydration == 0) data.Hydration = oldData.Hydration;
            if (data.HydrationTarget == null || data.HydrationTarget == 0) data.HydrationTarget = oldData.HydrationTarget;

            return data;
        }
    }
}