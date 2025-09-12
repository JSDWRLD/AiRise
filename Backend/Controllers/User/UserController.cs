using System;
using Microsoft.AspNetCore.Mvc;
using AiRise.Services;
using AiRise.Models.User;
using Microsoft.AspNetCore.Authorization;
using AiRise.Models.DTOs;

namespace AiRise.Controllers;

[Authorize]
[Controller]
[Route("api/[controller]")]
public class UserController : Controller
{
    private readonly UserService _userService;
    private readonly UserDataService _userDataService;
    private readonly UserFriendsService _userFriendsService;
    private readonly UserSettingsService _userSettingsService;
    private readonly ILogger<UserController> _logger;

    public UserController(
        UserService userService,
        UserDataService userDataService,
        UserFriendsService userFriendsService,
        UserSettingsService userSettingsService,
        ILogger<UserController> logger)
    {
        _userService = userService;
        _userDataService = userDataService;
        _userFriendsService = userFriendsService;
        _userSettingsService = userSettingsService;
        _logger = logger;
    }

    // // Get User by id
    [HttpGet("id/{id}")]
    public async Task<User> GetById(string id)
    {
        return await _userService.GetUserByIdAsync(id);
    }

    // Get User by FirebaseUid
    [HttpGet("firebaseUid/{firebaseUid}")]
    public async Task<User> GetByFirebaseUid(string firebaseUid)
    {
        return await _userService.GetUserByFirebaseUidAsync(firebaseUid);
    }

    // Create a User
    [HttpPost]
    public async Task<IActionResult> Post([FromBody] CreateUserRequest request)
    {
        if (request == null)
        {
            return BadRequest("Invalid request body.");
        }

        if (string.IsNullOrEmpty(request.FirebaseUid))
        {
            return BadRequest("FirebaseUid is required.");
        }

        try
        {
            var user = new User
            {
                FirebaseUid = request.FirebaseUid,
                Email = request.Email
            };

            await _userService.CreateAsync(user);
            return CreatedAtAction(nameof(Post), new { id = user.Id }, user); // corrected to use nameof(Post)
        }
        catch (Exception ex)
        {
            return StatusCode(500, $"An error occurred while creating the user: {ex.Message}");
        }
    }

    [HttpPut("{id}/firebaseUid")]
    public async Task<IActionResult> UpdateFirebaseUid(string id, [FromBody] string firebaseUid)
    {
        await _userService.UpdateFirebaseUidAsync(id, firebaseUid);
        return NoContent();
    }

    [HttpDelete("{id}")]
    public async Task<IActionResult> DeleteUser(string id)
    {
        await _userService.DeleteUserAsync(id);
        return NoContent();
    }

    [HttpPost("{firebaseUid}/streak")]
    public async Task<IActionResult> IncrementStreak(string firebaseUid)
    {
        await _userService.UpdateStreakByOneAsync(firebaseUid);
        return NoContent();
    }

    [HttpPost("{firebaseUid}/streak/reset")]
    public async Task<IActionResult> ResetStreak(string firebaseUid)
    {
        await _userService.ResetStreakAsync(firebaseUid);
        return NoContent();
    }

    // Global Leaderboard - Top 10
    [HttpGet("leaderboard/global/top10")]
    public async Task<IActionResult> GetGlobalTop10Leaderboard()
    {
        var users = await _userService.GetAsync();
        var leaderboard = await BuildLeaderboardEntries(users, 10);
        return Ok(leaderboard);
    }

    // Global Leaderboard - Top 100
    [HttpGet("leaderboard/global/top100")]
    public async Task<IActionResult> GetGlobalTop100Leaderboard()
    {
        var users = await _userService.GetAsync();
        var leaderboard = await BuildLeaderboardEntries(users, 100);
        return Ok(leaderboard);
    }


    // Friends Leaderboard
    [HttpGet("leaderboard/friends/{firebaseUid}")]
    public async Task<IActionResult> GetFriendsLeaderboard(string firebaseUid)
    {
        var user = await _userService.GetUserByFirebaseUidAsync(firebaseUid);
        if (user == null || string.IsNullOrEmpty(user.Friends))
        {
            return NotFound("User or friends not found.");
        }

        // Get UserFriends object
        // You need to implement a method in UserFriendsService to get UserFriends by Id or FirebaseUid
        var userFriends = await _userFriendsService.GetUserFriends(user.FirebaseUid);
        if (userFriends == null || userFriends.FriendIds.Count == 0)
        {
            return Ok(new List<LeaderboardEntry>());
        }

        // Get friend users by firebaseUid
        var friendUsers = new List<User>();
        foreach (var friendFirebaseUid in userFriends.FriendIds)
        {
            var friendUser = await _userService.GetUserByFirebaseUidAsync(friendFirebaseUid);
            if (friendUser != null)
            {
                friendUsers.Add(friendUser);
            }
        }
        
        // Add self to the list if not already present
        if (!friendUsers.Any(u => u.FirebaseUid == user.FirebaseUid))
        {
            friendUsers.Add(user);
        }

        var leaderboard = await BuildLeaderboardEntries(friendUsers, friendUsers.Count);
        return Ok(leaderboard);
    }

    // Helper method to build leaderboard entries
    private async Task<List<LeaderboardEntry>> BuildLeaderboardEntries(List<User> users, int maxCount)
    {
        var sortedUsers = users.OrderByDescending(u => u.Streak).Take(maxCount).ToList();
        var leaderboard = new List<LeaderboardEntry>();

        foreach (var user in sortedUsers)
        {
            string name = "";
            string imageUrl = "";

            // Use GetUserData and GetUserSettings with firebaseUid
            var userData = await _userDataService.GetUserData(user.FirebaseUid);
            if (userData != null)
            {
                name = $"{userData.FirstName} {userData.LastName}".Trim();
            }

            var userSettings = await _userSettingsService.GetUserSettings(user.FirebaseUid);
            if (userSettings != null)
            {
                imageUrl = userSettings.PictureUrl;
            }

            leaderboard.Add(new LeaderboardEntry
            {
                Name = name,
                ImageUrl = imageUrl,
                Streak = user.Streak
            });
        }

        return leaderboard;
    }
}