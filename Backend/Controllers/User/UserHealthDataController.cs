using AiRise.Controllers;
using AiRise.Models.User;
using AiRise.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

[Authorize]
[Controller]
[Route("api/[controller]")]
public class UserHealthDataController : Controller
{
    private readonly UserHealthDataService _userHealthDataService;
    private readonly ILogger<UserController> _logger;

    public UserHealthDataController(UserHealthDataService userHealthDataService, ILogger<UserController> logger)
    {
        _userHealthDataService = userHealthDataService;
        _logger = logger;
    }

    [HttpGet("{firebaseUid}")]
    public async Task<UserHealthData> GetUserHealthData(string firebaseUid)
    {
        return await _userHealthDataService.GetUserHealthDataAsync(firebaseUid);
    }

    [HttpPut("update-health-data/{firebaseUid}")]
    public async Task<IActionResult> UpdateUserHealthData(string firebaseUid, [FromBody] UserHealthData updatedData)
    {
        bool success = await _userHealthDataService.UpdateUserHealthDataAsync(firebaseUid, updatedData);

        if (!success)
            return NotFound(new { message = "UserHealtData not found or update failed" });
        return Ok(new { message = "UserHealthData updated successfully" });
    }
}