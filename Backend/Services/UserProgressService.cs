using MongoDB.Driver;
using MongoDB.Bson;
using AiRise.Models.User;

namespace AiRise.Services
{
    public class UserProgressService
    {
        private readonly IMongoCollection<UserProgress> _userProgressCollection;

        public UserProgressService(MongoDBService mongoDBService)
        {
            _userProgressCollection = mongoDBService.GetCollection<UserProgress>("user.progress");
        }

        public async Task<string> CreateAsync(string firebaseUid)
        {
            var userProgress = new UserProgress();
            userProgress.FirebaseUid = firebaseUid;
            await _userProgressCollection.InsertOneAsync(userProgress);
            return userProgress.Id;
        }
    }

}