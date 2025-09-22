using AiRise.Models.User;
using AiRise.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace AiRise.Controllers;

[Authorize]
[ApiController]
[Route("api/[controller]")]
public class UserProgramController : ControllerBase
{
    private readonly IUserProgramService _userProgramService;
    private readonly ILogger<UserProgramController> _logger;

    public UserProgramController(IUserProgramService userProgramService, ILogger<UserProgramController> logger)
    {
        _userProgramService = userProgramService;
        _logger = logger;
    }

    /// <summary>Get the user's current program document.</summary>
    [HttpGet("{firebaseUid}")]
    public async Task<ActionResult<UserProgramDoc>> Get(string firebaseUid, CancellationToken ct)
    {
        var doc = await _userProgramService.GetAsync(firebaseUid, ct);
        if (doc == null) return NotFound(new { message = "Program not found" });
        return Ok(doc);
    }

    /// <summary>
    /// Replace the user's program with a client-mutated copy (only weights & repsCompleted should change).
    /// Send the entire UserProgram object in the body.
    /// </summary>
    [HttpPut("{firebaseUid}")]
    public async Task<IActionResult> Update(string firebaseUid, [FromBody] UserProgram program, CancellationToken ct)
    {
        var ok = await _userProgramService.UpdateAsync(firebaseUid, program, ct);
        if (!ok) return NotFound(new { message = "Program not found or update failed" });
        return Ok(new { message = "Program updated successfully" });
    }
}
