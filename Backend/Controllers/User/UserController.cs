using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using AiRise.Services;
using AiRise.Models.User;

namespace AiRise.Controllers
{
    [Authorize]
    [ApiController]
    [Route("api/[controller]")]
    public class UserController : ControllerBase
    {
        private readonly UserDataService _userData;
        private readonly UserChallengesService _challenges;
        private readonly LeaderboardService _leaderboard;

        public UserController(UserDataService userData, UserChallengesService challenges, LeaderboardService leaderboard)
        {
            _userData = userData;
            _challenges = challenges;
            _leaderboard = leaderboard;
        }

        // For tests
        public UserController(UserDataService userData, UserChallengesService challenges)
        {
            _userData = userData;
            _challenges = challenges;
            _leaderboard = null!; // not used in these tests
        }

        public class CreateUserRequest { public string FirebaseUid { get; set; } = null!; public string? Email { get; set; } }

        // matches Android: POST /api/User (expects 201 + a "User"-shaped body)
        [HttpPost]
        public async Task<IActionResult> Post([FromBody] CreateUserRequest request)
        {
            if (request == null || string.IsNullOrWhiteSpace(request.FirebaseUid))
                return BadRequest("FirebaseUid is required.");

            // upsert profile & challenges
            await _userData.CreateAsync(request.FirebaseUid, request.Email);
            await _challenges.CreateAsync(request.FirebaseUid);

            // Return legacy-shaped payload (Id is null, refs omitted)
            var legacy = new
            {
                id = (string?)null,
                firebaseUid = request.FirebaseUid,
                email = request.Email ?? string.Empty,
                streak = 0,
                user_friends_ref = (string?)null,
                user_data_ref = (string?)null,
                user_settings_ref = (string?)null,
                goals_ref = (string?)null,
                workouts_ref = (string?)null,
                meal_plan_ref = (string?)null,
                progress_ref = (string?)null,
                challenges_ref = (string?)null,
                health_data_ref = (string?)null,
                chat_history_ref = (string?)null
            };

            return CreatedAtAction(nameof(GetByFirebaseUid), new { firebaseUid = request.FirebaseUid }, legacy);
        }

        // GET /api/User/firebaseUid/{firebaseUid}  (legacy) — return profile + streak, minimally shapable
        [HttpGet("firebaseUid/{firebaseUid}")]
        public async Task<IActionResult> GetByFirebaseUid(string firebaseUid)
        {
            var data = await _userData.GetUserData(firebaseUid);
            var ch = await _challenges.GetAsync(firebaseUid);

            if (data is null) return NotFound();

            // Legacy-ish shape: include streak field so clients relying on it don’t break
            var payload = new
            {
                id = data.Id,
                firebaseUid = data.FirebaseUid,
                email = data.Email,
                streak = ch.StreakCount
            };
            return Ok(payload);
        }

        // POST /api/User/{firebaseUid}/streak
        [HttpPost("{firebaseUid}/streak")]
        public async Task<IActionResult> IncrementStreak(string firebaseUid)
        {
            await _challenges.MarkCompleteTodayAsync(firebaseUid);
            return NoContent();
        }

        // POST /api/User/{firebaseUid}/streak/reset
        [HttpPost("{firebaseUid}/streak/reset")]
        public async Task<IActionResult> ResetStreak(string firebaseUid)
        {
            await _challenges.ResetStreakAsync(firebaseUid);
            return NoContent();
        }

        // Global Leaderboards
        [HttpGet("leaderboard/global/top10")]
        public async Task<IActionResult> GetGlobalTop10Leaderboard() =>
            Ok(await _leaderboard.GlobalTopNAsync(10));

        [HttpGet("leaderboard/global/top100")]
        public async Task<IActionResult> GetGlobalTop100Leaderboard() =>
            Ok(await _leaderboard.GlobalTopNAsync(100));

        // Friends Leaderboard
        [HttpGet("leaderboard/friends/{firebaseUid}")]
        public async Task<IActionResult> GetFriendsLeaderboard(string firebaseUid) =>
            Ok(await _leaderboard.FriendsAsync(firebaseUid));

        // Legacy endpoints you don’t use → 404 or BadRequest to discourage old patterns
        [HttpGet("id/{id}")]
        public IActionResult GetById(string id) => NotFound("Users are no longer keyed by ObjectId.");
        [HttpPut("{id}/firebaseUid")]
        public IActionResult UpdateFirebaseUid(string id, [FromBody] string _) => BadRequest("Not supported.");
        [HttpDelete("{id}")]
        public IActionResult DeleteUser(string id) => BadRequest("Pass firebaseUid to the new endpoints.");
    }
}
