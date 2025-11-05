using AiRise.Models.User;
using MongoDB.Driver;
using MongoDB.Bson;

namespace AiRise.Services
{
    public class UserDataService
    {
        private readonly IMongoCollection<UserData> _userDataCollection;
        private readonly IMongoCollection<UserChallenges> _challenges;
        private readonly IUserProgramService _userProgramService;

        public UserDataService(MongoDBService mongoDBService, IUserProgramService userProgramService)
        {
            _userDataCollection = mongoDBService.GetCollection<UserData>("user.data");
            _challenges = mongoDBService.GetCollection<UserChallenges>("user.challenges");
            _userProgramService = userProgramService;

            // Unique index on firebaseUid
            _userDataCollection.Indexes.CreateOne(
                new CreateIndexModel<UserData>(
                    Builders<UserData>.IndexKeys.Ascending(x => x.FirebaseUid),
                    new CreateIndexOptions { Unique = true }));
        }

        // for tests
        public UserDataService(
            IMongoCollection<UserData> userDataCollection,
            IMongoCollection<UserChallenges> challengesCollection,
            IUserProgramService userProgramService)
        {
            _userDataCollection = userDataCollection;
            _challenges = challengesCollection;
            _userProgramService = userProgramService;

            _userDataCollection.Indexes.CreateOne(
                new CreateIndexModel<UserData>(
                    Builders<UserData>.IndexKeys.Ascending(x => x.FirebaseUid),
                    new CreateIndexOptions { Unique = true }));
        }

        public async Task<string> CreateAsync(string firebaseUid, string? email = null)
        {
            var userData = new UserData { FirebaseUid = firebaseUid, Email = email ?? string.Empty };
            await _userDataCollection.InsertOneAsync(userData);
            return userData.Id!;
        }

        public Task CreateAsync(UserData userData) => _userDataCollection.InsertOneAsync(userData);

        public Task<UserData?> GetUserData(string firebaseUid) =>
            _userDataCollection.Find(u => u.FirebaseUid == firebaseUid).FirstOrDefaultAsync();

        private static string prepFullName(string firstName, string middleName, string lastName) =>
            string.Join(" ", new[] { firstName, middleName, lastName }.Where(s => !string.IsNullOrEmpty(s)));

        public async Task<bool> UpdateUserDataAsync(string firebaseUid, UserData updatedData)
        {
            var current = await _userDataCollection.Find(u => u.FirebaseUid == firebaseUid).FirstOrDefaultAsync();

            var oldDays = current?.WorkoutDays?.Where(s => !string.IsNullOrWhiteSpace(s)).ToList() ?? new();
            var newDays = updatedData.WorkoutDays?.Where(s => !string.IsNullOrWhiteSpace(s)).ToList() ?? new();

            var oldEquip = current?.WorkoutEquipment ?? string.Empty;
            var newEquip = updatedData.WorkoutEquipment ?? string.Empty;

            var oldWorkoutGoal = current?.WorkoutGoal ?? string.Empty;
            var newWorkoutGoal = updatedData.WorkoutGoal ?? string.Empty;

            var oldWorkoutLength = current?.WorkoutLength ?? 0;
            var newWorkoutLength = updatedData.WorkoutLength;

            var daysCountChanged = oldDays.Count != newDays.Count;
            var typeChanged = ProgramTypeMapper.MapEquipmentToProgramType(oldEquip) != ProgramTypeMapper.MapEquipmentToProgramType(newEquip);
            var workoutGoalChanged = !oldWorkoutGoal.Equals(newWorkoutGoal, StringComparison.OrdinalIgnoreCase);
            var workoutLengthChanged = oldWorkoutLength != newWorkoutLength;

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
                .Set(u => u.WorkoutEquipment, newEquip)
                .Set(u => u.WorkoutDays, newDays)
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

            ProgramPreferences? preferences = null;
            if (workoutGoalChanged || workoutLengthChanged)
            {
                preferences = new ProgramPreferences
                {
                    WorkoutGoal = newWorkoutGoal,
                    WorkoutLength = newWorkoutLength
                };
            }

            if (daysCountChanged || typeChanged)
            {
                if (newDays.Count is >= 3 and <= 6)
                {
                    var primaryType = ProgramTypeMapper.MapEquipmentToProgramType(newEquip);
                    preferences ??= new ProgramPreferences
                    {
                        WorkoutGoal = newWorkoutGoal,
                        WorkoutLength = newWorkoutLength
                    };
                    await _userProgramService.AssignFromExplicitAsync(firebaseUid, primaryType, newDays, preferences);
                }
            }
            else
            {
                if (!SeqEqualIgnoreCase(oldDays, newDays) && newDays.Count is >= 3 and <= 6)
                {
                    await _userProgramService.RelabelDayNamesAsync(firebaseUid, newDays, preferences);
                }
                else if (preferences != null)
                {
                    var primaryType = ProgramTypeMapper.MapEquipmentToProgramType(newEquip);
                    await _userProgramService.UpdatePreferencesAsync(firebaseUid, primaryType, newDays, preferences);
                }
            }

            return result.IsAcknowledged && result.MatchedCount > 0;
        }

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

        public async Task<bool> UpdateUserHeightAsync(string firebaseUid, UserData updatedData)
        {
            var filter = Builders<UserData>.Filter.Eq(u => u.FirebaseUid, firebaseUid);
            var update = Builders<UserData>.Update
                .Set(u => u.HeightMetric, updatedData.HeightMetric)
                .Set(u => u.HeightValue, updatedData.HeightValue);

            var result = await _userDataCollection.UpdateOneAsync(filter, update);
            return result.ModifiedCount > 0;
        }

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
            // Atlas Search on user.data.fullName
            BsonDocument[] pipeline =
            [
                new BsonDocument("$search", new BsonDocument
                {
                    { "index", "nameSearch" },
                    { "autocomplete", new BsonDocument { { "query", query }, { "path", "fullName" } } }
                }),
                new BsonDocument("$lookup", new BsonDocument {
                    { "from", "user.settings" },
                    { "localField", "firebaseUid" },
                    { "foreignField", "firebaseUid" },
                    { "as", "settings" }
                }),
                new BsonDocument("$unwind", new BsonDocument("path", "$settings").Add("preserveNullAndEmptyArrays", true)),
                new BsonDocument("$lookup", new BsonDocument {
                    { "from", "user.challenges" },
                    { "localField", "firebaseUid" },
                    { "foreignField", "firebaseUid" },
                    { "as", "ch" }
                }),
                new BsonDocument("$unwind", new BsonDocument("path", "$ch").Add("preserveNullAndEmptyArrays", true)),
                new BsonDocument("$project", new BsonDocument {
                    { "firebaseUid", 1 },
                    { "profile_picture_url", "$settings.profile_picture_url" },
                    { "streak", "$ch.streakCount" },
                    { "firstName", 1 },
                    { "middleName", 1 },
                    { "lastName", 1 },
                    { "fullName", 1 },
                    { "_id", 0 }
                })
            ];
            return new UserList { Users = await _userDataCollection.Aggregate<UserProfile>(pipeline).ToListAsync() };
        }

        private static bool SeqEqualIgnoreCase(IReadOnlyList<string> a, IReadOnlyList<string> b)
        {
            if (a.Count != b.Count) return false;
            for (int i = 0; i < a.Count; i++)
            {
                var aa = (a[i] ?? "").Trim();
                var bb = (b[i] ?? "").Trim();
                if (!aa.Equals(bb, StringComparison.OrdinalIgnoreCase)) return false;
            }
            return true;
        }
    }
}
