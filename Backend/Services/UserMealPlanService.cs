using MongoDB.Driver;
using MongoDB.Bson;
using AiRise.Models.User;

namespace AiRise.Services
{
    public class UserMealPlanService
    {
        private readonly IMongoCollection<UserMealPlan> _userMealPlanCollection;

        public UserMealPlanService(MongoDBService mongoDBService)
        {
            _userMealPlanCollection = mongoDBService.GetCollection<UserMealPlan>("user.mealplans");
        }

        public async Task<string> CreateAsync()
        {
            var userMealPlan = new UserMealPlan();
            await _userMealPlanCollection.InsertOneAsync(userMealPlan);
            return userMealPlan.Id;
        }
    }
}