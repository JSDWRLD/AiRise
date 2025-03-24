using System;
using Microsoft.AspNetCore.Mvc;
using AiRise.Services;
using AiRise.Models.User;
using Microsoft.AspNetCore.Authorization;

namespace AiRise.Controllers;

[Authorize]
[Controller]
[Route("api/[controller]")]
public class UserDataController : Controller
{
    private readonly UserDataService _userDataService;
    private readonly ILogger<UserController> _logger;

    public UserDataController(UserDataService userDataService, ILogger<UserController> logger)
    {
        _userDataService = userDataService;
        _logger = logger;
    }

    [HttpPut("{firebaseUid}")]
    public async Task<IActionResult> UpdateUserData(string firebaseUid, [FromBody] UserData updatedData)
    {
        bool success = await _userDataService.UpdateUserDataAsync(firebaseUid, updatedData);

        if (!success)
            return NotFound(new { message = "UserData not found or update failed" });

        return Ok(new { message = "UserData updated successfully" });
    }
}