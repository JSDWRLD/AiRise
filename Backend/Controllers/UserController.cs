using System;
using Microsoft.AspNetCore.Mvc;
using AiRise.Services;
using AiRise.Models;
using Microsoft.AspNetCore.Authorization;

namespace AiRise.Controllers;

[Authorize]
[Controller]
[Route("api/[controller]")]
public class UserController : Controller
{
    private readonly UserService _userService;
    private readonly ILogger<UserController> _logger;

    public UserController(UserService userService, ILogger<UserController> logger)
    {
        _userService = userService;
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
                FirebaseUid = request.FirebaseUid
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
        await _userService.DeleteAsync(id);
        return NoContent();
    }

    // // Get List of Users
    // [HttpGet]
    // public async Task<List<User>> Get() 
    // {
    //     return await _userService.GetAsync();
    // }
}