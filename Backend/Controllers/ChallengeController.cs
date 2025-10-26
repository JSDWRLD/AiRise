using AiRise.Models;
using AiRise.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using MongoDB.Driver;

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

        /************** ADMIN ONLY *******************/

        [Authorize("Admin")]
        [HttpPost]
        public async Task<IActionResult> UpsertChallenge([FromBody] Challenge challenge)
        {
            var success = await _challengeService.UpsertChallengeAsync(challenge);
            return success ? Ok(new { message = "Challenge upserted successfully" })
                : BadRequest(new { message = "Challenge upsert failed" });
        }

        [Authorize("Admin")]
        [HttpDelete]
        public async Task<IActionResult> DeleteChallenge(string id)
        {
            var success = await _challengeService.DeleteChallengeAsync(id);
            return success ? Ok(new { message = "Successfully deleted the challenge" })
                : NotFound(new { message = "Unable to find a challenge with a matching ID" }); 
        }
    }
}
