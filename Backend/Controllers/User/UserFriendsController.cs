using Microsoft.AspNetCore.Mvc;
using AiRise.Services;
using AiRise.Models.User;
using Microsoft.AspNetCore.Authorization;

namespace AiRise.Controllers;

[Authorize]
[Controller]
[Route("api/[controller]")]
public class UserFriendsController : Controller
{
    private readonly UserFriendsService _userFriendService;
    private readonly ILogger<UserController> _logger;

    public UserFriendsController(UserFriendsService userFriendsService, ILogger<UserController> logger)
    {
        _userFriendService = userFriendsService;
        _logger = logger;
    }

    // GET /api/UserFriends/{firebaseUid}
    [HttpGet("{firebaseUid}")]
    public async Task<ActionResult<UserList>> GetUserFriendsList(string firebaseUid, CancellationToken ct)
    {
        var list = await _userFriendService.GetUserFriendsList(firebaseUid);
        return Ok(list ?? new UserList { Users = new List<UserProfile>() });
    }

    // POST /api/UserFriends/{firebaseUid}?friendFirebaseUid=abc
    [HttpPost("{firebaseUid}")]
    public async Task<IActionResult> AddFriend(string firebaseUid, [FromQuery] string friendFirebaseUid, CancellationToken ct)
    {
        var success = await _userFriendService.AddFriend(firebaseUid, friendFirebaseUid);
        return success ? Ok(new { message = "Friend added successfully" })
                       : NotFound(new { message = "UserFriend not found or adding failed" });
    }

    // DELETE /api/UserFriends/{firebaseUid}?friendFirebaseUid=abc
    [HttpDelete("{firebaseUid}")]
    public async Task<IActionResult> DeleteFriend(string firebaseUid, [FromQuery] string friendFirebaseUid, CancellationToken ct)
    {
        var success = await _userFriendService.DeleteFriend(firebaseUid, friendFirebaseUid);
        return success ? Ok(new { message = "Friend deleted successfully" })
                       : NotFound(new { message = "UserFriend not found or deleting failed" });
    }
}