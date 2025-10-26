using System.Runtime.CompilerServices;
using AiRise.Models;
using MongoDB.Bson;
using MongoDB.Driver;

namespace AiRise.Services
{
    public class ChallengeService
    {
        private readonly IMongoCollection<Challenge> _challengeCollection;

        public ChallengeService(MongoDBService mongoDBService)
        {
            _challengeCollection = mongoDBService.GetCollection<Challenge>("challenges");
        }

        public ChallengeService(IMongoCollection<Challenge> collection)
        {
            _challengeCollection = collection;
        }

        public async Task<List<Challenge>> GetAllChallengesAsync()
        {
            return await _challengeCollection.Find(_ => true).ToListAsync();
        }



        public async Task SeedChallengesIfEmptyAsync(List<Challenge> seedData)
        {
            var count = await _challengeCollection.CountDocumentsAsync(_ => true);
            if (count == 0)
            {
                // Remove Id property so MongoDB generates ObjectId
                var seedWithoutId = seedData.Select(c => new Challenge
                {
                    Name = c.Name,
                    Description = c.Description,
                    Url = c.Url
                }).ToList();
                await _challengeCollection.InsertManyAsync(seedWithoutId);
            }
        }

        public async Task<bool> UpsertChallengeAsync(Challenge challenge)
        {
            // Generate a new ID if none is provided
            if (string.IsNullOrWhiteSpace(challenge.Id))
            {
                challenge.Id = ObjectId.GenerateNewId().ToString();
            }
            if (string.IsNullOrWhiteSpace(challenge.Name)) return false;
            
            var filter = Builders<Challenge>.Filter.Eq(c => c.Id, challenge.Id);
            var update = Builders<Challenge>.Update
                .Set(c => c.Name, challenge.Name)
                .Set(c => c.Description, challenge.Description)
                .Set(c => c.Url, challenge.Url);
            var options = new UpdateOptions { IsUpsert = true };

            var result = await _challengeCollection.UpdateOneAsync(filter, update, options);
            return result.IsAcknowledged && (result.ModifiedCount > 0 || result.UpsertedId != null);
        }
            
        public async Task<bool> DeleteChallengeAsync(string id)
        {
            var filter = Builders<Challenge>.Filter.Eq(c => c.Id, id);
            var result = await _challengeCollection.DeleteOneAsync(filter);
            return result.DeletedCount > 0;
        }
    }
}
