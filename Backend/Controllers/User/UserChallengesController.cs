using AiRise.Models.User;
using AiRise.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace AiRise.Controllers
{
    [Authorize]
    [Controller]
    [Route("api/[controller]")]
    public class UserChallengesController : ControllerBase
    {
        private readonly UserChallengesService _svc;

        public UserChallengesController(UserChallengesService svc)
        {
            _svc = svc;
        }

        // DTOs
        public record CreateReq(string FirebaseUid);
        public record SetActiveReq(string FirebaseUid, string ChallengeId);
        public record UidOnly(string FirebaseUid);

        /// <summary>
        /// Returns the user's challenge progress document (or an empty shell if none exists).
        /// GET /api/userchallenges/{firebaseUid}
        /// </summary>
        [HttpGet("{firebaseUid}")]
        public async Task<ActionResult<UserChallenges>> Get(string firebaseUid, CancellationToken ct)
        {
            if (string.IsNullOrWhiteSpace(firebaseUid))
                return BadRequest("FirebaseUid is required.");

            var doc = await _svc.GetAsync(firebaseUid, ct);
            return Ok(doc);
        }

        /// <summary>
        /// Creates a user.challenges document for a user (if you need explicit creation).
        /// POST /api/userchallenges
        /// </summary>
        [HttpPost]
        public async Task<ActionResult<string>> Create([FromBody] CreateReq req, CancellationToken ct)
        {
            if (string.IsNullOrWhiteSpace(req.FirebaseUid))
                return BadRequest("FirebaseUid is required.");

            // This will INSERT a new document; use sparingly to avoid duplicates.
            var id = await _svc.CreateAsync(req.FirebaseUid);
            return Ok(id);
        }

        /// <summary>
        /// Upserts active challenge for the user.
        /// POST /api/userchallenges/set-active
        /// </summary>
        [HttpPost("set-active")]
        public async Task<IActionResult> SetActive([FromBody] SetActiveReq req, CancellationToken ct)
        {
            if (string.IsNullOrWhiteSpace(req.FirebaseUid) || string.IsNullOrWhiteSpace(req.ChallengeId))
                return BadRequest("FirebaseUid and ChallengeId are required.");

            await _svc.SetActiveAsync(req.FirebaseUid, req.ChallengeId, ct);
            return NoContent();
        }

        [HttpGet("completed-today/{firebaseUid}")]
        public async Task<ActionResult<bool>> CompletedToday(string firebaseUid, CancellationToken ct)
        {
            if (string.IsNullOrWhiteSpace(firebaseUid))
                return BadRequest("FirebaseUid is required.");

            var completed = await _svc.CompletedTodayAsync(firebaseUid, ct);
            return Ok(completed);
        }

        /// <summary>
        /// Marks today's completion (idempotent). Creates the doc if missing.
        /// POST /api/userchallenges/complete-today
        /// </summary>
        [HttpPost("complete-today")]
        public async Task<ActionResult<UserChallenges>> CompleteToday([FromBody] UidOnly req, CancellationToken ct)
        {
            if (string.IsNullOrWhiteSpace(req.FirebaseUid))
                return BadRequest("FirebaseUid is required.");

            var doc = await _svc.MarkCompleteTodayAsync(req.FirebaseUid, ct);
            return Ok(doc);
        }

        /// <summary>
        /// Clears the last completion marker (does NOT touch ActiveChallengeId).
        /// POST /api/userchallenges/clear-completion
        /// </summary>
        [HttpPost("clear-completion")]
        public async Task<IActionResult> ClearCompletion([FromBody] UidOnly req, CancellationToken ct)
        {
            if (string.IsNullOrWhiteSpace(req.FirebaseUid))
                return BadRequest("FirebaseUid is required.");

            await _svc.ClearCompletionAsync(req.FirebaseUid, ct);
            return NoContent();
        }
    }
}
