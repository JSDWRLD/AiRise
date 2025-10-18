using AiRise.Models.User;
using MongoDB.Driver;
using MongoDB.Bson;

namespace AiRise.Services
{
    public class UserFriendsService
    {
        private readonly IMongoCollection<UserFriends> _userFriendsCollection;
        private readonly IMongoCollection<UserData> _userDataCollection;
        private readonly IMongoCollection<UserSettings> _userSettingsCollection;
        private readonly IMongoCollection<UserChallenges> _userChallengesCollection;

        public UserFriendsService(MongoDBService mongoDBService)
        {
            _userFriendsCollection = mongoDBService.GetCollection<UserFriends>("user.friends");
            _userDataCollection = mongoDBService.GetCollection<UserData>("user.data");
            _userSettingsCollection = mongoDBService.GetCollection<UserSettings>("user.settings");
            _userChallengesCollection = mongoDBService.GetCollection<UserChallenges>("user.challenges");

            // Ensure each user has at most one friends doc
            _userFriendsCollection.Indexes.CreateOne(
                new CreateIndexModel<UserFriends>(
                    Builders<UserFriends>.IndexKeys.Ascending(x => x.FirebaseUid),
                    new CreateIndexOptions { Unique = true }));
        }

        public async Task<string> CreateAsync(string firebaseUid)
        {
            var doc = new UserFriends { FirebaseUid = firebaseUid, FriendIds = new List<string>() };
            await _userFriendsCollection.InsertOneAsync(doc);
            return doc.Id!;
        }

        public async Task<UserFriends?> GetUserFriends(string firebaseUid)
        {
            var filter = Builders<UserFriends>.Filter.Eq(x => x.FirebaseUid, firebaseUid);
            var existing = await _userFriendsCollection.Find(filter).FirstOrDefaultAsync();

            if (existing != null)
                return existing;

            // create a new doc if none exists
            var doc = new UserFriends { FirebaseUid = firebaseUid, FriendIds = new List<string>() };
            try
            {
                await _userFriendsCollection.InsertOneAsync(doc);
                return doc;
            }
            catch (MongoWriteException mwx) when (mwx.WriteError?.Category == ServerErrorCategory.DuplicateKey)
            {
                // created concurrently by another request → return the one that now exists
                return await _userFriendsCollection.Find(filter).FirstOrDefaultAsync();
            }
        }

        // Returns list of friend profiles (keeps your old projection field names)
        public async Task<UserList> GetUserFriendsList(string firebaseUid)
        {
            var uf = await GetUserFriends(firebaseUid);
            var friendIds = uf?.FriendIds ?? new List<string>();
            if (friendIds.Count == 0)
                return new UserList { Users = new List<UserProfile>() };

            return await AggregateFriendProfiles(friendIds);
        }

        // Aggregation starting from user.data, then join settings + challenges
        private async Task<UserList> AggregateFriendProfiles(List<string> friendIds)
        {
            // NOTE: adjust the path "profile_picture_url" if your client expects a different key
            BsonDocument[] pipeline =
            [
                new BsonDocument("$match", new BsonDocument("firebaseUid", new BsonDocument("$in", new BsonArray(friendIds)))),
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
                    { "profile_picture_url", "$settings.profile_picture_url" }, // or "$settings.pictureUrl"
                    { "streak", "$ch.streakCount" },
                    { "firstName", 1 },
                    { "middleName", 1 },
                    { "lastName", 1 },
                    { "fullName", 1 },
                    { "_id", 0 }
                })
            ];

            // IMPORTANT: run the pipeline on user.data, not users
            var results = await _userDataCollection.Aggregate<UserProfile>(pipeline).ToListAsync();
            return new UserList { Users = results };
        }

        public async Task<bool> AddFriend(string firebaseUid, string friendFirebaseUid)
        {
            // Ensure both docs exist
            await _userFriendsCollection.UpdateOneAsync(
                Builders<UserFriends>.Filter.Eq(u => u.FirebaseUid, firebaseUid),
                Builders<UserFriends>.Update.AddToSet(u => u.FriendIds, friendFirebaseUid),
                new UpdateOptions { IsUpsert = true });

            await _userFriendsCollection.UpdateOneAsync(
                Builders<UserFriends>.Filter.Eq(u => u.FirebaseUid, friendFirebaseUid),
                Builders<UserFriends>.Update.AddToSet(u => u.FriendIds, firebaseUid),
                new UpdateOptions { IsUpsert = true });

            return true;
        }

        public async Task<bool> DeleteFriend(string firebaseUid, string friendFirebaseUid)
        {
            var resultUser = await _userFriendsCollection.UpdateOneAsync(
                Builders<UserFriends>.Filter.Eq(u => u.FirebaseUid, firebaseUid),
                Builders<UserFriends>.Update.Pull(u => u.FriendIds, friendFirebaseUid));

            var resultFriend = await _userFriendsCollection.UpdateOneAsync(
                Builders<UserFriends>.Filter.Eq(u => u.FirebaseUid, friendFirebaseUid),
                Builders<UserFriends>.Update.Pull(u => u.FriendIds, firebaseUid));

            return resultUser.ModifiedCount > 0 || resultFriend.ModifiedCount > 0;
        }
    }
}
