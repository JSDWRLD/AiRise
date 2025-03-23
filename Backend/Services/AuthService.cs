using AiRise.Models;
using MongoDB.Driver;
using BCrypt.Net;
using System.Text.RegularExpressions;

namespace AiRise.Services
{
    public class AuthService
    {
        private readonly IMongoCollection<User> _userCollection;

        public AuthService(MongoDBService mongoDBService)
        {
            _userCollection = mongoDBService.GetCollection<User>("users");
        }

        // // Register
        // public async Task register(User user)
        // {
        //     // Verify if email follows right schema with regex here
        //     if (!IsValidEmail(user.Email))
        //     {
        //         throw new ArgumentException("Invalid email format.");
        //     }

        //     FilterDefinition<User> emailFilter = Builders<User>.Filter.Eq("Email", user.Email);
        //     if (await _userCollection.Find(emailFilter).AnyAsync())
        //     {
        //         throw new InvalidOperationException("Email already exists.");
        //     }

        //     user.Password = BCrypt.Net.BCrypt.HashPassword(user.Password);
        //     await _userCollection.InsertOneAsync(user);
        // } 

        // // Login
        // public async Task<User?> login(string email, string password)
        // {
        //     var user = await _userCollection.Find(u => u.Email == email).FirstOrDefaultAsync();

        //     if (user == null)
        //     {
        //         throw new Exception("Invalid email.");
        //     }

        //     if (!BCrypt.Net.BCrypt.Verify(password, user.Password))
        //     {
        //         throw new UnauthorizedAccessException("Invalid email or password.");
        //     }
            
        //     return user;
        // }

        // private bool IsValidEmail(string email)
        // {
        //     if (string.IsNullOrWhiteSpace(email))
        //         return false;

        //     try
        //     {
        //         // Basic regex for email validation
        //         var regex = new Regex(@"^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$");
        //         return regex.IsMatch(email);
        //     }
        //     catch (RegexMatchTimeoutException)
        //     {
        //         return false;
        //     }
        // }
    }
}