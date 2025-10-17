using AiRise.Models;
using MongoDB.Driver;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace AiRise.Services
{
    public class ChallengeService
    {
        private readonly IMongoCollection<Challenge> _challengeCollection;

        public ChallengeService(MongoDBService mongoDBService)
        {
            _challengeCollection = mongoDBService.GetCollection<Challenge>("challenges");
        }

        public async Task<List<Challenge>> GetAllChallengesAsync()
        {
            return await _challengeCollection.Find(_ => true).ToListAsync();
        }

        public async Task InsertChallengeAsync(Challenge challenge)
        {
            await _challengeCollection.InsertOneAsync(challenge);
        }

        public async Task SeedChallengesIfEmptyAsync(List<Challenge> seedData)
        {
            var count = await _challengeCollection.CountDocumentsAsync(_ => true);
            if (count == 0)
            {
                // Remove Id property so MongoDB generates ObjectId
                var seedWithoutId = seedData.Select(c => new Challenge {
                    Name = c.Name,
                    Description = c.Description,
                    Url = c.Url
                }).ToList();
                await _challengeCollection.InsertManyAsync(seedWithoutId);
            }
        }
    }
}
