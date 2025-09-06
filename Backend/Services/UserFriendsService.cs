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
        public async Task<FriendList> GetUserFriendsList(string firebaseUid)
        {
            UserFriends? userFriends = await GetUserFriends(firebaseUid);
            List<string> friendIds = userFriends.FriendIds;
            if (userFriends == null || userFriends.FriendIds == null)
                return new FriendList();

            return await AggregateFriendProfiles(friendIds);
        }
        private async Task<FriendList> AggregateFriendProfiles(List<string> friendIds)
        {
            BsonDocument[] pipeline = new BsonDocument[] {
                new BsonDocument("$match", new BsonDocument("firebaseUid", new BsonDocument("$in", new BsonArray(friendIds)))),
                new BsonDocument("$lookup", new BsonDocument {
                    {"from", "user.data"},
                    { "localField", "firebaseUid" },
                    { "foreignField", "firebaseUid" },
                    { "as", "data" }
                }),
                new BsonDocument("$unwind", "$data"),
                new BsonDocument("$project", new BsonDocument {
                        { "firebaseUid", 1 },
                        { "email", 1 },
                        { "streak", 1 },
                        { "firstName", "$data.firstName" },
                        { "lastName", "$data.lastName" },
                        {"_id", 0 }
                })
            };
            return new FriendList{ Friends = await _userCollection.Aggregate<FriendListItem>(pipeline).ToListAsync() };
        }
    }
}