using Microsoft.AspNetCore.Authorization;
using Microsoft.Extensions.Caching.Memory;
using AiRise.Models.Admin;
using AiRise.Services;
using System.Security.Claims;
using MongoDB.Bson.Serialization.Serializers;

namespace AiRise.Authorization
{
    public class AdminRequirement: IAuthorizationRequirement
    {
        public int MaxTokenAgeMinutes { get; } = 5;
        public int MaxSessionAgeMinutes { get; } = 10;
    }

    public class AdminAuthorizationHandler : AuthorizationHandler<AdminRequirement>
    {
        private readonly IMemoryCache _cache;
        private readonly UserDataService _userDataService;
        private readonly ILogger<AdminAuthorizationHandler> _logger;

        public AdminAuthorizationHandler(IMemoryCache cache, UserDataService userDataService, ILogger<AdminAuthorizationHandler> logger)
        {
            _cache = cache;
            _userDataService = userDataService;
            _logger = logger;
        }
        protected override async Task HandleRequirementAsync(AuthorizationHandlerContext context, AdminRequirement requirement)
        {
            var firebaseUid = context.User.FindFirst("user_id")?.Value;
            if (firebaseUid == null){
                _logger.LogWarning("Admin authorization failed: No Firebase UID found");
                context.Fail();
                return; 
            }


            _logger.LogWarning("Admin authorization: Session not found, checking token age");
            var tokenAge = GetTokenAge(context.User);
            if (tokenAge > TimeSpan.FromMinutes(requirement.MaxTokenAgeMinutes))
            {
                _logger.LogWarning("Admin authorization failed: Token expired");
                context.Fail();
                return;
            }


            var cachedSession = $"admin_session:{firebaseUid}";
            if (_cache.TryGetValue(cachedSession, out AdminSession? session))
            {
                if (session != null && session.ExpiresAt > DateTime.UtcNow)
                {
                    _logger.LogWarning("Admin authorization granted via cached session");
                    context.Succeed(requirement);
                    return;
                }
                else
                {
                    _cache.Remove(cachedSession);
                    _logger.LogWarning("Cached session expired - removed from cache");
                }
            }

            
            var userData = await _userDataService.GetUserData(firebaseUid);
            if (userData == null || !userData.IsAdmin){
                _logger.LogWarning("Admin authorization failed: User is not an admin");
                context.Fail();
                return;
            }
            var newSession = new AdminSession
            {
                FirebaseUid = firebaseUid,
                IssuedAt = DateTime.UtcNow,
                ExpiresAt = DateTime.UtcNow.AddMinutes(requirement.MaxSessionAgeMinutes)
            };

            _cache.Set(cachedSession, newSession, TimeSpan.FromMinutes(requirement.MaxSessionAgeMinutes));
            _logger.LogInformation("Admin authorization granted: New session created and cached");
            context.Succeed(requirement);
            return;
}

        private TimeSpan GetTokenAge(ClaimsPrincipal user)
        {
            var issuedAt = user.FindFirst("iat")?.Value;
            if (long.TryParse(issuedAt, out var timestamp))
            {
                var issuedAtDate = DateTimeOffset.FromUnixTimeSeconds(timestamp).UtcDateTime;
                return DateTime.UtcNow - issuedAtDate;
            }

            _logger.LogWarning("Could not determine token age - no 'iat' claim found");
            return TimeSpan.MaxValue; // If we can't determine age, fail
        }
    }
}