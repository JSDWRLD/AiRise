using System.Runtime.CompilerServices;
using AiRise.Models;
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

        public async Task<bool> UpdateChallengeAsync(Challenge challenge)
        {
            var filter = Builders<Challenge>.Filter.Eq(c => c.Id, challenge.Id);
            var update = Builders<Challenge>.Update
                .Set(c => c.Name, challenge.Name)
                .Set(c => c.Description, challenge.Description)
                .Set(c => c.Url, challenge.Url);
            var result = await _challengeCollection.UpdateOneAsync(filter, update);
            return result.IsAcknowledged && result.ModifiedCount > 0;
        }

        public async Task<bool> InsertChallengeAsync(Challenge challenge)
        {
            var dupe = _challengeCollection.Find<Challenge>(c => c.Id == challenge.Id).FirstOrDefaultAsync;
            if (dupe != null)
            {
                return false;
            }
            await _challengeCollection.InsertOneAsync(challenge);
            return true;
        }  
            
        public async Task<bool> DeleteChallengeAsync(string id)
        {
            var filter = Builders<Challenge>.Filter.Eq(c => c.Id, id);
            var result = await _challengeCollection.DeleteOneAsync(filter);
            return result.DeletedCount > 0;
        }
    }
}
