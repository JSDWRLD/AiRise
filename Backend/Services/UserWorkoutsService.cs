using MongoDB.Driver;
using MongoDB.Bson;
using AiRise.Models.User;

namespace AiRise.Services
{
    public class UserWorkoutsService
    {
        private readonly IMongoCollection<UserWorkouts> _userWorkoutsCollection;

        public UserWorkoutsService(MongoDBService mongoDBService)
        {
            _userWorkoutsCollection = mongoDBService.GetCollection<UserWorkouts>("user.workouts");
        }

        public async Task<string> CreateAsync()
        {
            var userWorkouts = new UserWorkouts();
            await _userWorkoutsCollection.InsertOneAsync(userWorkouts);
            return userWorkouts.Id;
        }
    }
}