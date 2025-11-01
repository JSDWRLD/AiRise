using System;
using System.Threading.Tasks;
using AiRise.Controllers;
using AiRise.Models.User;
using AiRise.Services;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;
using Mongo2Go;
using MongoDB.Driver;
using Xunit;

public class UserSettingsController_Tests : IDisposable
{
    private readonly MongoDbRunner _runner;
    private readonly IMongoDatabase _db;
    private readonly IMongoCollection<UserSettings> _col;
    private readonly UserSettingsService _svc;
    private readonly UserSettingsController _ctrl;

    public UserSettingsController_Tests()
    {
        _runner = MongoDbRunner.Start();
        var client = new MongoClient(_runner.ConnectionString);
        _db = client.GetDatabase($"UserSettingsCtrl_{Guid.NewGuid()}");
        _col = _db.GetCollection<UserSettings>("user.settings_ctrl");
        _col.DeleteMany(FilterDefinition<UserSettings>.Empty);

        // Requires the test-only overload in your service:
        // public UserSettingsService(IMongoCollection<UserSettings> collection) { ... }
        _svc = new UserSettingsService(_col);

        // Noop logger to satisfy ctor without Moq
        _ctrl = new UserSettingsController(_svc, new NoopLogger<UserController>());
    }

    // --- Tests ---

    [Fact]
    public async Task GetUserSettings_WhenMissing_CreatesDefaults_AndReturnsThem()
    {
        var result = await _ctrl.GetUserSettings("uid-missing");

        Assert.NotNull(result);
        Assert.Equal("uid-missing", result.FirebaseUid);
        Assert.Equal("", result.PictureUrl);
        Assert.Equal("default", result.AiPersonality);
        Assert.True(result.ChallengeNotifsEnabled);
        Assert.True(result.FriendReqNotifsEnabled);
        Assert.True(result.StreakNotifsEnabled);
        Assert.True(result.MealNotifsEnabled);

        // persisted?
        var fromDb = await _col.Find(x => x.FirebaseUid == "uid-missing").FirstOrDefaultAsync();
        Assert.NotNull(fromDb);
        Assert.Equal("uid-missing", fromDb.FirebaseUid);
    }

    [Fact]
    public async Task GetUserSettings_WhenExists_ReturnsExisting()
    {
        // seed an existing doc
        var seeded = new UserSettings
        {
            FirebaseUid = "u1",
            PictureUrl = "http://pic/u1.png",
            AiPersonality = "friendly",
            ChallengeNotifsEnabled = false,
            FriendReqNotifsEnabled = false,
            StreakNotifsEnabled = false,
            MealNotifsEnabled = false
        };
        await _col.InsertOneAsync(seeded);

        var result = await _ctrl.GetUserSettings("u1");

        Assert.NotNull(result);
        Assert.Equal("u1", result.FirebaseUid);
        Assert.Equal("http://pic/u1.png", result.PictureUrl);
        Assert.Equal("friendly", result.AiPersonality);
        Assert.False(result.ChallengeNotifsEnabled);
        Assert.False(result.FriendReqNotifsEnabled);
        Assert.False(result.StreakNotifsEnabled);
        Assert.False(result.MealNotifsEnabled);
    }

    [Fact]
    public async Task UpdateUserData_NotFound_When_NoMatchingUser()
    {
        var payload = new UserSettings
        {
            PictureUrl = "x",
            AiPersonality = "y",
            ChallengeNotifsEnabled = false,
            FriendReqNotifsEnabled = false,
            StreakNotifsEnabled = false,
            MealNotifsEnabled = false
        };

        var result = await _ctrl.UpdateUserData("missing-uid", payload);
        var notFound = Assert.IsType<NotFoundObjectResult>(result);
        Assert.Contains("not found", notFound.Value!.ToString(), StringComparison.OrdinalIgnoreCase);
    }

    [Fact]
    public async Task UpdateUserData_Ok_When_UserExists()
    {
        // create the doc (service creates a minimal record)
        await _svc.CreateAsync("u2");

        var updated = new UserSettings
        {
            PictureUrl = "http://new/pic.png",
            AiPersonality = "snarky",
            ChallengeNotifsEnabled = false,
            FriendReqNotifsEnabled = true,
            StreakNotifsEnabled = false,
            MealNotifsEnabled = true
        };

        var result = await _ctrl.UpdateUserData("u2", updated);
        var ok = Assert.IsType<OkObjectResult>(result);

        // verify persisted
        var fromDb = await _col.Find(x => x.FirebaseUid == "u2").FirstOrDefaultAsync();
        Assert.NotNull(fromDb);
        Assert.Equal("http://new/pic.png", fromDb.PictureUrl);
        Assert.Equal("snarky", fromDb.AiPersonality);
        Assert.False(fromDb.ChallengeNotifsEnabled);
        Assert.True(fromDb.FriendReqNotifsEnabled);
        Assert.False(fromDb.StreakNotifsEnabled);
        Assert.True(fromDb.MealNotifsEnabled);
    }

    // --- Helpers ---

    private sealed class NoopLogger<T> : ILogger<T>
    {
        public IDisposable BeginScope<TState>(TState state) => new Nop();
        public bool IsEnabled(LogLevel logLevel) => false;
        public void Log<TState>(LogLevel logLevel, EventId eventId, TState state, Exception? exception, Func<TState, Exception?, string> formatter) { }
        private sealed class Nop : IDisposable { public void Dispose() { } }
    }

    public void Dispose() => _runner.Dispose();
}
