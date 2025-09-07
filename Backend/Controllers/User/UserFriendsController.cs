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

    [HttpGet("{firebaseUid}")]
    public async Task<FriendList> GetUserFriendsList(string firebaseUid)
    {
        return await _userFriendService.GetUserFriendsList(firebaseUid);
    }

    [HttpPost("{firebaseUid}")]
    public async Task<IActionResult> AddFriend(string firebaseUid, string friendFirebaseUid)
    {
        bool success = await _userFriendService.AddFriend(firebaseUid, friendFirebaseUid);

        if (!success)
        {
            return NotFound(new { message = "UserFriend not found or adding failed" });
        }
        return Ok(new { message = "Friend added successfully" });
    }

}