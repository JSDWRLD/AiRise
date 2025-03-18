using Microsoft.AspNetCore.Mvc;
using AiRise.Services;
using AiRise.Models;

namespace AiRise.Controllers;

[Controller]
[Route("api/[controller]")]
public class AuthController : Controller
{
    private readonly AuthService _authService;
    private readonly ILogger<AuthController> _logger;

    public AuthController(AuthService authService, ILogger<AuthController> logger)
    {
        _authService = authService;
        _logger = logger;
    }
    
    [HttpPost("register")]
    public async Task<IActionResult> register([FromBody] User user)
    {
        try 
        {
            await _authService.register(user);
            return CreatedAtAction(nameof(login), new { id = user.Id }, user);;
        }
        catch (ArgumentException e)
        {
            return BadRequest(e.Message);
        }
        catch (InvalidOperationException e)
        {
            // Or use the custom message class 
            return Conflict(e.Message);
        }
        catch (Exception e)
        {
            return BadRequest(e.Message); // 400 Bad Request for other errors
        }
    }
    
    [HttpPost("login")]
    public async Task<ActionResult<User>> login([FromBody] LoginRequest obj)
    {
        try 
        {
            var user = await _authService.login(obj.Email, obj.Password);
            if (user == null)
            {
                return Unauthorized("Invalid credentials");
            }

            return Ok(user);
        } 
        catch (UnauthorizedAccessException e) 
        {
            return Unauthorized(e.Message); // Email is right, password is wrong
        } 
        catch (Exception e)
        {
            return BadRequest(e.Message); // 400 Bad Request for other errors
        }

    }
}

public class LoginRequest
{
    public string Email { get; set; }
    public string Password { get; set; }
}

    
