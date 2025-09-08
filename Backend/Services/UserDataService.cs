using AiRise.Models.User;
using MongoDB.Driver;
using MongoDB.Bson;

namespace AiRise.Services
{

    public class UserDataService
    {
        private readonly IMongoCollection<User> _userCollection;
        private readonly IMongoCollection<UserData> _userDataCollection;


        public UserDataService(MongoDBService mongoDBService)
        {
            _userCollection = mongoDBService.GetCollection<User>("users"); // Use the Users collection
            _userDataCollection = mongoDBService.GetCollection<UserData>("user.data");
        }

        public async Task<string> CreateAsync(string firebaseUid)
        {
            var userData = new UserData();
            userData.FirebaseUid = firebaseUid;
            await _userDataCollection.InsertOneAsync(userData);
            return userData.Id;
        }

        public async Task CreateAsync(UserData userData)
        {
            await _userDataCollection.InsertOneAsync(userData);
            return;
        }

        public async Task<UserData?> GetUserData(string firebaseUid)
        {
            return await _userDataCollection.Find(u => u.FirebaseUid == firebaseUid).FirstOrDefaultAsync();
        }

        private string prepFullName(string firstName, string middleName, string lastName)
        {
            string updatedFullName = string.Join(" ",
            new[] { firstName, middleName, lastName }
            .Where(s => !string.IsNullOrEmpty(s)));

            return updatedFullName;
        }

        public async Task<bool> UpdateUserDataAsync(string firebaseUid, UserData updatedData)
        {
            string updatedFullName = prepFullName(updatedData.FirstName, updatedData.MiddleName, updatedData.LastName);

            var filter = Builders<UserData>.Filter.Eq(u => u.FirebaseUid, firebaseUid);
            var update = Builders<UserData>.Update
                .Set(u => u.FirstName, updatedData.FirstName)
                .Set(u => u.LastName, updatedData.LastName)
                .Set(u => u.MiddleName, updatedData.MiddleName)
                .Set(u => u.FullName, updatedFullName)
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

        // Only updates the user name
        public async Task<bool> UpdateUserNameAsync(string firebaseUid, UserData updatedData)
        {
            string updatedFullName = prepFullName(updatedData.FirstName, updatedData.MiddleName, updatedData.LastName);

            var filter = Builders<UserData>.Filter.Eq(u => u.FirebaseUid, firebaseUid);
            var update = Builders<UserData>.Update
                .Set(u => u.FirstName, updatedData.FirstName)
                .Set(u => u.LastName, updatedData.LastName)
                .Set(u => u.MiddleName, updatedData.MiddleName)
                .Set(u => u.FullName, updatedFullName);

            var result = await _userDataCollection.UpdateOneAsync(filter, update);
            return result.ModifiedCount > 0;
        }
        
        // Only updates user birthday
        public async Task<bool> UpdateUserDOBAsync(string firebaseUid, UserData updatedData)
        {
            var filter = Builders<UserData>.Filter.Eq(u => u.FirebaseUid, firebaseUid);
            var update = Builders<UserData>.Update
                .Set(u => u.DobDay, updatedData.DobDay)
                .Set(u => u.DobMonth, updatedData.DobMonth)
                .Set(u => u.DobYear, updatedData.DobYear);

            var result = await _userDataCollection.UpdateOneAsync(filter, update);
            return result.ModifiedCount > 0;
        }

        // Only updates the user height
        public async Task<bool> UpdateUserHeightAsync(string firebaseUid, UserData updatedData)
        {
            var filter = Builders<UserData>.Filter.Eq(u => u.FirebaseUid, firebaseUid);
            var update = Builders<UserData>.Update
                .Set(u => u.HeightMetric, updatedData.HeightMetric)
                .Set(u => u.HeightValue, updatedData.HeightValue);

            var result = await _userDataCollection.UpdateOneAsync(filter, update);
            return result.ModifiedCount > 0;
        }

        // Only updates the user weight
        public async Task<bool> UpdateUserWeightAsync(string firebaseUid, UserData updatedData)
        {
            var filter = Builders<UserData>.Filter.Eq(u => u.FirebaseUid, firebaseUid);
            var update = Builders<UserData>.Update
                .Set(u => u.WeightMetric, updatedData.WeightMetric)
                .Set(u => u.WeightValue, updatedData.WeightValue);

            var result = await _userDataCollection.UpdateOneAsync(filter, update);
            return result.ModifiedCount > 0;
        }

        public async Task<UserList> SearchUsersByNameAsync(string query)
        {
            //uses the search index on UserData to find people by their full names
            BsonDocument[] pipeline =
            [
                new BsonDocument("$search", new BsonDocument
                {
                    { "index", "nameSearch" },
                    { "autocomplete", new BsonDocument
                        {
                            { "query", query },
                            { "path", "fullName" }
                        }
                    }
                }),
                new BsonDocument("$lookup", new BsonDocument {
                    {"from", "users"},
                    { "localField", "firebaseUid" },
                    { "foreignField", "firebaseUid" },
                    { "as", "users" }
                }),
                //join with UserSettings
                new BsonDocument("$unwind", "$users"),
                new BsonDocument("$lookup", new BsonDocument {
                    {"from", "user.settings"},
                    {"localField", "firebaseUid"},
                    {"foreignField", "firebaseUid"},
                    {"as", "settings"}
                }),
                new BsonDocument("$unwind", "$settings"),
                //Shaping document to only give user profile
                new BsonDocument("$project", new BsonDocument {
                        { "firebaseUid", 1 },
                        { "profile_picture_url", "$settings.profile_picture_url" },
                        { "streak", "$users.streak" },
                        { "firstName", 1 },
                        { "middleName", 1 },
                        { "lastName", 1 },
                        { "fullName", 1 },
                        {"_id", 0 }
                })
            ];
            return new UserList { Users = await _userDataCollection.Aggregate<UserProfile>(pipeline).ToListAsync() };
        }
        
    }
}