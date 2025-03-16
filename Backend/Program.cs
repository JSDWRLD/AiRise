using AiRise.Models;
using AiRise.Services;

namespace AiRise;

public class Program
{
    public static void Main(string[] args)
    {
        var builder = WebApplication.CreateBuilder(args);

        // Create singleton service
        builder.Services.Configure<MongoDBSettings>(builder.Configuration.GetSection("MongoDB"));
        builder.Services.AddSingleton<MongoDBService>();
        builder.Services.AddSingleton<UserService>();
        builder.Services.AddSingleton<AuthService>();

        builder.Services.AddControllers();
        // FOR DEVELOPMENT API TESTING
        builder.Services.AddEndpointsApiExplorer();
        builder.Services.AddSwaggerGen(); 
        // ENV
        DotNetEnv.Env.Load(); // Load .env file
        builder.Configuration.AddEnvironmentVariables(); // Add environment variables


        builder.Services.AddCors(options =>
        {
        options.AddPolicy("AllowAll",
            builder => builder
                .AllowAnyOrigin()
                .AllowAnyMethod()
                .AllowAnyHeader());
        });

        var app = builder.Build();


        // Configure the HTTP request pipeline.
        // ONLY DURING DEVELOPMENT
        if (app.Environment.IsDevelopment()) 
        {
            app.UseSwagger(); 
            app.UseSwaggerUI(); 
        }

        app.UseHttpsRedirection();
        app.UseCors("AllowAll");
        app.MapControllers();
        app.Run();
    }
}