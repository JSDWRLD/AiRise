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
        public ActionResult<List<Challenge>> GetAllChallenges()
        {
            var challenges = _challengeService.GetAllChallenges();
            return Ok(challenges);
        }
    }
}
