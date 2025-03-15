using System;
using Microsoft.AspNetCore.Mvc;
using AiRise.Services;
using AiRise.Models;

namespace AiRise.Controllers;

[Controller]
[Route("api/[controller]")]
public class UserController : Controller
{
    private readonly MongoDBService _mongoDBService;
    private readonly ILogger<UserController> _logger;

    public UserController(MongoDBService mongoDBService, ILogger<UserController> logger)
    {
        _mongoDBService = mongoDBService;
        _logger = logger;
    }

    [HttpGet]
    public async Task<List<User>> Get() 
    {
        return await _mongoDBService.GetAsync();
    }

    [HttpPost]
    public async Task<IActionResult> Post([FromBody] User user) 
    {
        await _mongoDBService.CreateAsync(user);
        return CreatedAtAction(nameof(Get), new { id = user.Id }, user);
    }

    // Get ID of specific update
    [HttpPut("{id}")]
    public async Task<IActionResult> AddUser(string id, [FromBody] string username) 
    {
        await _mongoDBService.AddUserAsync(id, username);
        return NoContent();
    }

    [HttpDelete("{id}")]
    public async Task<IActionResult> DeleteUser(string id) 
    {
        await _mongoDBService.DeleteAsync(id);
        return NoContent();
    }
}