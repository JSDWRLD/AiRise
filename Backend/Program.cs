using System.Text.Json.Serialization;
using AiRise.Authorization;
using AiRise.Models;
using AiRise.Services;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.AspNetCore.Authorization;
using Microsoft.IdentityModel.Tokens;
using Microsoft.OpenApi.Models;

namespace AiRise;

public class Program
{
    public static void Main(string[] args)
    {
        var builder = WebApplication.CreateBuilder(args);

        // Create singleton service
        builder.Services.Configure<MongoDBSettings>(builder.Configuration.GetSection("MongoDB"));
        builder.Services.AddSingleton<MongoDBService>();

        builder.Services.AddControllers()
        .AddJsonOptions(o =>
        {
            o.JsonSerializerOptions.Converters.Add(new JsonStringEnumConverter());
        });

        builder.Services.AddSingleton<IUserProgramService, UserProgramService>();
        builder.Services.AddSingleton<IUserHealthDataService, UserHealthDataService>();

        builder.Services.AddSingleton<UserDataService>();
        builder.Services.AddSingleton<UserFriendsService>();
        builder.Services.AddSingleton<UserSettingsService>();
        builder.Services.AddSingleton<UserChallengesService>();
        builder.Services.AddSingleton<UserProgramService>();
        builder.Services.AddSingleton<LeaderboardService>();
        builder.Services.AddSingleton<ChallengeService>();
        builder.Services.AddSingleton<FoodDiaryService>();

        builder.Services.AddControllers();

        // ENV 
        DotNetEnv.Env.Load();
        builder.Configuration.AddEnvironmentVariables();

        // Enable JWT Authentication Google
        builder.Services.AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
            .AddJwtBearer(options =>
            {
                options.Authority = "https://securetoken.google.com/airise-org";
                options.TokenValidationParameters = new TokenValidationParameters
                {
                    ValidateIssuer = true,
                    ValidIssuer = "https://securetoken.google.com/airise-org",
                    ValidateAudience = true,
                    ValidAudience = "airise-org",
                    ValidateLifetime = true
                };
            });

        // Add memory cache for admin authentication
        builder.Services.AddMemoryCache();
        builder.Services.AddScoped<IAuthorizationHandler, AdminAuthorizationHandler>();
        
        builder.Services.AddAuthorization(options =>
        {
            options.AddPolicy("Admin", policy =>
            {
                policy.Requirements.Add(new AdminRequirement());
            });
        });
        

        // Enable CORS
        builder.Services.AddCors(options =>
        {
            options.AddPolicy("AllowAll",
                builder => builder
                    .AllowAnyOrigin()
                    .AllowAnyMethod()
                    .AllowAnyHeader());
        });

        // Configure Swagger with authentication
        builder.Services.AddSwaggerGen(c =>
        {
            c.SwaggerDoc("v1", new OpenApiInfo { Title = "AiRise API", Version = "v1" });

            c.AddSecurityDefinition("Bearer", new OpenApiSecurityScheme
            {
                Name = "Authorization",
                Type = SecuritySchemeType.Http,
                Scheme = "Bearer",
                BearerFormat = "JWT",
                In = ParameterLocation.Header,
                Description = "Enter your Firebase JWT token like: Bearer {token}"
            });

            c.AddSecurityRequirement(new OpenApiSecurityRequirement
            {
                {
                    new OpenApiSecurityScheme
                    {
                        Reference = new OpenApiReference { Type = ReferenceType.SecurityScheme, Id = "Bearer" }
                    },
                    new List<string>()
                }
            });
        });

        var app = builder.Build();

        // Seed challenges if empty
        using (var scope = app.Services.CreateScope())
        {
            var challengeService = scope.ServiceProvider.GetRequiredService<ChallengeService>();
            var seedChallenges = AiRise.Data.ChallengeData.Challenges;
            challengeService.SeedChallengesIfEmptyAsync(seedChallenges).GetAwaiter().GetResult();
        }

        // Configure the HTTP request pipeline.
        if (app.Environment.IsDevelopment())
        {
            app.UseSwagger();
            app.UseSwaggerUI();
        }

        app.UseHttpsRedirection();
        app.UseCors("AllowAll");
        app.UseAuthentication(); // Enable authentication
        app.UseAuthorization(); // Enable authorization
        app.MapControllers();
        app.Run();
    }
}
