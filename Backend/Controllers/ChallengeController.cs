using AiRise.Models;
using AiRise.Services;
using Microsoft.AspNetCore.Mvc;

namespace AiRise.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class ChallengeController : ControllerBase
    {
        private readonly ChallengeService _challengeService;

        public ChallengeController(ChallengeService challengeService)
        {
            _challengeService = challengeService;
        }

        [HttpGet]
        public async Task<ActionResult<List<Challenge>>> GetAllChallenges()
        {
            var challenges = await _challengeService.GetAllChallengesAsync();
            return Ok(challenges);
        }

        // Insert a new challenge
        [HttpPost]
        public async Task<IActionResult> InsertChallenge([FromBody] Challenge challenge)
        {
            await _challengeService.InsertChallengeAsync(challenge);
            return Ok();
        }
    }
}
