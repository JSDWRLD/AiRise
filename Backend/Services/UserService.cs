using AiRise.Models;
using Microsoft.Extensions.Options;
using MongoDB.Driver;
using MongoDB.Bson;

namespace AiRise.Services;

public class UserService 
{
    private readonly IMongoCollection<User> _userCollection;
    private readonly IMongoCollection<UserFriends> _userFriendsCollection;
    private readonly IMongoCollection<UserData> _userDataCollection;
    private readonly IMongoCollection<UserSettings> _userSettingsCollection;
    private readonly IMongoCollection<UserGoals> _userGoalsCollection;
    private readonly IMongoCollection<UserWorkouts> _userWorkoutsCollection;
    private readonly IMongoCollection<UserMealPlan> _userMealPlanCollection;
    private readonly IMongoCollection<UserProgress> _userProgressCollection;
    private readonly IMongoCollection<UserChallenges> _userChallengesCollection;
    private readonly IMongoCollection<UserHealthData> _userHealthDataCollection;
    private readonly IMongoCollection<UserChatHistory> _userChatHistoryCollection;

    public UserService(MongoDBService mongoDBService) 
    {
        _userCollection = mongoDBService.GetCollection<User>("users"); // Use the Users collection
        _userFriendsCollection = mongoDBService.GetCollection<UserFriends>("user.friends");
        _userDataCollection = mongoDBService.GetCollection<UserData>("user.data");
        _userSettingsCollection = mongoDBService.GetCollection<UserSettings>("user.settings");
        _userGoalsCollection = mongoDBService.GetCollection<UserGoals>("user.goals");
        _userWorkoutsCollection = mongoDBService.GetCollection<UserWorkouts>("user.workouts");
        _userMealPlanCollection = mongoDBService.GetCollection<UserMealPlan>("user.mealplan");
        _userProgressCollection = mongoDBService.GetCollection<UserProgress>("user.progress");
        _userChallengesCollection = mongoDBService.GetCollection<UserChallenges>("user.challenges");
        _userHealthDataCollection = mongoDBService.GetCollection<UserHealthData>("user.healthdata");
        _userChatHistoryCollection = mongoDBService.GetCollection<UserChatHistory>("user.chathistory");
    }

    public async Task CreateAsync(User user)
    {
        var userFriends = new UserFriends();
        var userData = new UserData();
        var userSettings = new UserSettings();
        var userGoals = new UserGoals();
        var userWorkouts = new UserWorkouts();
        var userMealPlan = new UserMealPlan();
        var userProgress = new UserProgress();
        var userChallenges = new UserChallenges();
        var userHealthData = new UserHealthData();
        var userChatHistory = new UserChatHistory();

        await _userFriendsCollection.InsertOneAsync(userFriends);
        await _userDataCollection.InsertOneAsync(userData);
        await _userSettingsCollection.InsertOneAsync(userSettings);
        await _userGoalsCollection.InsertOneAsync(userGoals);
        await _userWorkoutsCollection.InsertOneAsync(userWorkouts);
        await _userMealPlanCollection.InsertOneAsync(userMealPlan);
        await _userProgressCollection.InsertOneAsync(userProgress);
        await _userChallengesCollection.InsertOneAsync(userChallenges);
        await _userHealthDataCollection.InsertOneAsync(userHealthData);
        await _userChatHistoryCollection.InsertOneAsync(userChatHistory);

        user.Friends = userFriends.Id;
        user.Data = userData.Id;
        user.Settings = userSettings.Id;
        user.Goals = userGoals.Id;
        user.Workouts = userWorkouts.Id;
        user.MealPlan = userMealPlan.Id;
        user.Progress = userProgress.Id;
        user.Challenges = userChallenges.Id;
        user.HealthData = userHealthData.Id;
        user.ChatHistory = userChatHistory.Id;

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

    public async Task<bool> UpdateUserDataAsync(string firebaseUid, UserData updatedData)
    {
        var user = await GetUserByFirebaseUidAsync(firebaseUid);
        if (user == null || string.IsNullOrEmpty(user.Data))
        {
            return false; // User not found or no UserData reference
        }

        var filter = Builders<UserData>.Filter.Eq(u => u.Id, user.Data);
        var update = Builders<UserData>.Update
            .Set(u => u.FirstName, updatedData.FirstName)
            .Set(u => u.LastName, updatedData.LastName)
            .Set(u => u.MiddleName, updatedData.MiddleName)
            .Set(u => u.WorkoutGoal, updatedData.WorkoutGoal)
            .Set(u => u.FitnessLevel, updatedData.FitnessLevel)
            .Set(u => u.WorkoutLength, updatedData.WorkoutLength)
            .Set(u => u.WorkoutEquipment, updatedData.WorkoutEquipment)
            .Set(u => u.WorkoutDays, updatedData.WorkoutDays)
            .Set(u => u.WorkoutTime, updatedData.WorkoutTime)
            .Set(u => u.DietaryGoal, updatedData.DietaryGoal)
            .Set(u => u.WorkoutRestrictions, updatedData.WorkoutRestrictions)
            .Set(u => u.HeightMetric, updatedData.HeightMetric)
            .Set(u => u.HeightValue, updatedData.HeightValue)
            .Set(u => u.WeightMetric, updatedData.WeightMetric)
            .Set(u => u.WeightValue, updatedData.WeightValue)
            .Set(u => u.DobDay, updatedData.DobDay)
            .Set(u => u.DobMonth, updatedData.DobMonth)
            .Set(u => u.DobYear, updatedData.DobYear)
            .Set(u => u.ActivityLevel, updatedData.ActivityLevel);

        var result = await _userDataCollection.UpdateOneAsync(filter, update);
        return result.ModifiedCount > 0;
    }
}
