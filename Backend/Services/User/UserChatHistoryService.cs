using MongoDB.Driver;
using MongoDB.Bson;
using AiRise.Models.User;

namespace AiRise.Services
{
    public class UserChatHistoryService
    {
        private readonly IMongoCollection<UserChatHistory> _userChatHistoryCollection;

        public UserChatHistoryService(MongoDBService mongoDBService)
        {
            _userChatHistoryCollection = mongoDBService.GetCollection<UserChatHistory>("user.chathistory");
        }

        public async Task<string> CreateAsync()
        {
            var userChatHistory = new UserChatHistory();
            await _userChatHistoryCollection.InsertOneAsync(userChatHistory);
            return userChatHistory.Id;
        }
    }
}