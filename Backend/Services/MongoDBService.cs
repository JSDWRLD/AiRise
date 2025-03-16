using AiRise.Models;
using Microsoft.Extensions.Options;
using MongoDB.Driver;

namespace AiRise.Services
{
    public class MongoDBService
    {
        private readonly IMongoDatabase _database;
        private readonly ILogger<MongoDBService> _logger;

        public MongoDBService(IOptions<MongoDBSettings> mongoDBSettings, ILogger<MongoDBService> logger)
        {
            _logger = logger;
            string connectionString = Environment.GetEnvironmentVariable("MONGODB_CONNECTION_URI");

            if (string.IsNullOrEmpty(connectionString))
            {
                _logger.LogError("MONGODB_CONNECTION_URI environment variable is not set.");
                throw new Exception("MONGODB_CONNECTION_URI environment variable is not set.");
            }

            var client = new MongoClient(connectionString);
            _database = client.GetDatabase(mongoDBSettings.Value.DatabaseName);
        }

        public IMongoCollection<T> GetCollection<T>(string collectionName)
        {
            return _database.GetCollection<T>(collectionName);
        }
    }
}
