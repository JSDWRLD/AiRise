using System;
using Microsoft.AspNetCore.Mvc;
using AiRise.Services;
using AiRise.Models.User;
using Microsoft.AspNetCore.Authorization;

namespace AiRise.Controllers;

[Authorize]
[Controller]
[Route("api/[controller]")]
public class UserSettingsController : Controller
{
    private readonly UserSettingsService _userSettingsService;
    private readonly ILogger<UserController> _logger;

    public UserSettingsController(UserSettingsService userSettingsService, ILogger<UserController> logger)
    {
        _userSettingsService = userSettingsService;
        _logger = logger;
    }

    [HttpGet("{firebaseUid}")] 
    public async Task<UserSettings> GetUserSettings(string firebaseUid) {
        return await _userSettingsService.GetUserSettings(firebaseUid);
    }

    [HttpPut("{firebaseUid}")]
    public async Task<IActionResult> UpdateUserData(string firebaseUid, [FromBody] UserSettings updatedSettings)
    {
        bool success = await _userSettingsService.UpdateUserSettingsAsync(firebaseUid, updatedSettings);

        if (!success)
            return NotFound(new { message = "UserData not found or update failed" });

        return Ok(new { message = "UserData updated successfully" });
    }
}