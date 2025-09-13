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

    [HttpGet("{firebaseUid}")]
    public async Task<UserData> GetUserData(string firebaseUid) {
        return await _userDataService.GetUserData(firebaseUid);
    }

    [HttpPut("{firebaseUid}")]
    public async Task<IActionResult> UpdateUserData(string firebaseUid, [FromBody] UserData updatedData)
    {
        bool success = await _userDataService.UpdateUserDataAsync(firebaseUid, updatedData);

        if (!success)
            return NotFound(new { message = "UserData not found or update failed" });

        return Ok(new { message = "UserData updated successfully" });
    }

    [HttpPut("/update-name/{firebaseUid}")]
    public async Task<IActionResult> UpdateUserName(string firebaseUid, [FromBody] UserData updatedData)
    {
        bool success = await _userDataService.UpdateUserNameAsync(firebaseUid, updatedData);

        if (!success)
            return NotFound(new { message = "UserData not found or update failed" });

        return Ok(new { message = "UserData updated successfully" });
    }

    [HttpPut("/update-height/{firebaseUid}")]
    public async Task<IActionResult> UpdateUserHeight(string firebaseUid, [FromBody] UserData updatedData)
    {
        bool success = await _userDataService.UpdateUserHeightAsync(firebaseUid, updatedData);

        if (!success)
            return NotFound(new { message = "UserData not found or update failed" });

        return Ok(new { message = "UserData updated successfully" });
    }

    [HttpPut("/update-dob/{firebaseUid}")]
    public async Task<IActionResult> UpdateUserDOB(string firebaseUid, [FromBody] UserData updatedData)
    {
        bool success = await _userDataService.UpdateUserDOBAsync(firebaseUid, updatedData);

        if (!success)
            return NotFound(new { message = "UserData not found or update failed" });

        return Ok(new { message = "UserData updated successfully" });
    }

    [HttpPut("/update-weight/{firebaseUid}")]
    public async Task<IActionResult> UpdateUserWeight(string firebaseUid, [FromBody] UserData updatedData)
    {
        bool success = await _userDataService.UpdateUserWeightAsync(firebaseUid, updatedData);

        if (!success)
            return NotFound(new { message = "UserData not found or update failed" });

        return Ok(new { message = "UserData updated successfully" });
    }

    [HttpGet("search-user/{firebaseUid}")]
    public async Task<UserList> SearchUsersByNameAsync(string firebaseUid, string query)
    {
            return await _userDataService.SearchUsersByNameAsync(query);
    }

}