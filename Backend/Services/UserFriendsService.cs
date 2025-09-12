using AiRise.Models.User;
using MongoDB.Driver;
using MongoDB.Bson;

namespace AiRise.Services
{

    public class UserFriendsService
    {
        private readonly IMongoCollection<User> _userCollection;
        private readonly IMongoCollection<UserFriends> _userFriendsCollection;


        public UserFriendsService(MongoDBService mongoDBService)
        {
            _userCollection = mongoDBService.GetCollection<User>("users"); // Use the Users collection
            _userFriendsCollection = mongoDBService.GetCollection<UserFriends>("user.friends");
        }

        public async Task<string> CreateAsync(string firebaseUid)
        {
            var userFriends = new UserFriends();
            userFriends.FirebaseUid = firebaseUid;
            await _userFriendsCollection.InsertOneAsync(userFriends);
            return userFriends.Id;
        }

        // Simply gets and returns the UserFriends document for the given firebaseUid
        public async Task<UserFriends?> GetUserFriends(string firebaseUid)
        {
            var filter = Builders<UserFriends>.Filter.Eq(x => x.FirebaseUid, firebaseUid);
            return await _userFriendsCollection.Find(filter).FirstOrDefaultAsync();
        }

        //Gets the list of firebaseUids and returns a list of profiles for each one
        public async Task<UserList> GetUserFriendsList(string firebaseUid)
        {
            UserFriends? userFriends = await GetUserFriends(firebaseUid);
            List<string> friendIds = userFriends.FriendIds;
            if (userFriends == null || userFriends.FriendIds == null)
                return new UserList();

            return await AggregateFriendProfiles(friendIds);
        }
        private async Task<UserList> AggregateFriendProfiles(List<string> friendIds)
        {
            BsonDocument[] pipeline = 
            [
                new BsonDocument("$match", new BsonDocument("firebaseUid", new BsonDocument("$in", new BsonArray(friendIds)))),
                new BsonDocument("$lookup", new BsonDocument {
                    {"from", "user.data"},
                    { "localField", "firebaseUid" },
                    { "foreignField", "firebaseUid" },
                    { "as", "data" }
                }),
                new BsonDocument("$unwind", "$data"),
                new BsonDocument("$lookup", new BsonDocument {
                    {"from", "user.settings"},
                    {"localField", "firebaseUid"},
                    {"foreignField", "firebaseUid"},
                    {"as", "settings"}
                }),
                new BsonDocument("$unwind", "$settings"),
                new BsonDocument("$project", new BsonDocument {
                        { "firebaseUid", 1 },
                        { "profile_picture_url", "$settings.profile_picture_url" },
                        { "streak", 1 },
                        { "firstName", "$data.firstName" },
                        { "middleName", "$data.middleName" },
                        { "lastName", "$data.lastName" },
                        { "fullName", "$data.fullName" },
                        { "_id", 0 }
                })
            ];
            return new UserList { Users = await _userCollection.Aggregate<UserProfile>(pipeline).ToListAsync() };
        }

        public async Task<bool> AddFriend(string firebaseUid, string friendFirebaseUid)
        {
            // Add friendFirebaseUid to user's friend list
            var filterUser = Builders<UserFriends>.Filter.Eq(u => u.FirebaseUid, firebaseUid);
            var updateUser = Builders<UserFriends>.Update.AddToSet(u => u.FriendIds, friendFirebaseUid);
            var resultUser = await _userFriendsCollection.UpdateOneAsync(filterUser, updateUser);

            // Add firebaseUid to friend's friend list
            var filterFriend = Builders<UserFriends>.Filter.Eq(u => u.FirebaseUid, friendFirebaseUid);
            var updateFriend = Builders<UserFriends>.Update.AddToSet(u => u.FriendIds, firebaseUid);
            var resultFriend = await _userFriendsCollection.UpdateOneAsync(filterFriend, updateFriend);

            return resultUser.ModifiedCount > 0 || resultFriend.ModifiedCount > 0;
        }

        public async Task<bool> DeleteFriend(string firebaseUid, string friendFirebaseUid)
        {
            // Pull friendFirebaseUid from user's friend list
            var filterUser = Builders<UserFriends>.Filter.Eq(u => u.FirebaseUid, firebaseUid);
            var updateUser = Builders<UserFriends>.Update.Pull(u => u.FriendIds, friendFirebaseUid);
            var resultUser = await _userFriendsCollection.UpdateOneAsync(filterUser, updateUser);

            // Pull firebaseUid from friend's friend list
            var filterFriend = Builders<UserFriends>.Filter.Eq(u => u.FirebaseUid, friendFirebaseUid);
            var updateFriend = Builders<UserFriends>.Update.Pull(u => u.FriendIds, firebaseUid);
            var resultFriend = await _userFriendsCollection.UpdateOneAsync(filterFriend, updateFriend);

            return resultUser.ModifiedCount > 0 || resultFriend.ModifiedCount > 0;
        }
    }
}