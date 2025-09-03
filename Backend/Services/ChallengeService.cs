using AiRise.Models;
using System.Collections.Generic;

namespace AiRise.Services
{
    public class ChallengeService
    {
        public List<Challenge> GetAllChallenges()
        {
            return Data.ChallengeData.Challenges;
        }
    }
}
