using System;
using Microsoft.AspNetCore.Mvc;
using AiRise.Services;
using AiRise.Models;

namespace AiRise.Controllers;

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

    // Get List of Users
    [HttpGet]
    public async Task<List<User>> Get() 
    {
        return await _userService.GetAsync();
    }

    // Get User by id
    [HttpGet("id/{id}")]
    public async Task<User> GetById(string id) 
    {
        return await _userService.GetUserByIdAsync(id);
    }

    // Get User by email
    [HttpGet("email/{email}")]
    public async Task<User> GetByEmail(string email) 
    {
        return await _userService.GetUserByEmailAsync(email);
    }
    
    // Create a User
    [HttpPost]
    public async Task<IActionResult> Post([FromBody] User user) 
    {
        await _userService.CreateAsync(user);
        return CreatedAtAction(nameof(Get), new { id = user.Id }, user);
    }

    // Get ID of specific update
    [HttpPut("{id}/username")]
    public async Task<IActionResult> UpdateUsername(string id, [FromBody] string username) 
    {
        await _userService.UpdateUsernameAsync(id, username);
        return NoContent();
    }
    
    [HttpPut("{id}/email")]
    public async Task<IActionResult> UpdateEmail(string id, [FromBody] string email) 
    {
        await _userService.UpdateEmailAsync(id, email);
        return NoContent();
    }

    [HttpDelete("{id}")]
    public async Task<IActionResult> DeleteUser(string id) 
    {
        await _userService.DeleteAsync(id);
        return NoContent();
    }
}