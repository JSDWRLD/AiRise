using AiRise.Controllers;
using AiRise.Models.User;
using AiRise.Services;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;
using MongoDB.Driver;

public class UserSettingsController_IntegrationTests : IClassFixture<MongoIntegrationTest<UserSettings>>
{
    private readonly MongoIntegrationTest<UserSettings> _fixture;
    private readonly IMongoCollection<UserSettings> _collection;
    private readonly UserSettingsService _svc;
    private readonly UserSettingsController _ctrl;

    public UserSettingsController_IntegrationTests(MongoIntegrationTest<UserSettings> fixture)
    {
        _fixture = fixture;
        _collection = _fixture.GetCollection<UserSettings>("user.settings");
        _fixture.ClearAllCollectionsAsync().GetAwaiter().GetResult();

        _svc = new UserSettingsService(_collection); // <- test-only overload
        _ctrl = new UserSettingsController(_svc, new NoopLogger<UserController>());
    }

    // Simple no-op logger to satisfy ctor (avoid Moq)
    private sealed class NoopLogger<T> : ILogger<T>
    {
        public IDisposable BeginScope<TState>(TState state) => new Nop();
        public bool IsEnabled(LogLevel logLevel) => false;
        public void Log<TState>(LogLevel logLevel, EventId eventId, TState state, System.Exception exception, System.Func<TState, System.Exception, string> formatter) { }
        private sealed class Nop : System.IDisposable { public void Dispose() { } }
    }

    [Fact]
    public async Task GetUserSettings_WhenMissing_CreatesDefaults_AndReturnsThem()
    {
        // No doc exists
        var result = await _ctrl.GetUserSettings("uid-missing");

        Assert.NotNull(result);
        Assert.Equal("uid-missing", result.FirebaseUid);
        Assert.Equal("", result.PictureUrl);
        Assert.Equal("default", result.AiPersonality);
        Assert.True(result.ChallengeNotifsEnabled);
        Assert.True(result.FriendReqNotifsEnabled);
        Assert.True(result.StreakNotifsEnabled);
        Assert.True(result.MealNotifsEnabled);

        // Verify it was actually persisted
        var fromDb = await _collection.Find(x => x.FirebaseUid == "uid-missing").FirstOrDefaultAsync();
        Assert.NotNull(fromDb);
        Assert.Equal("uid-missing", fromDb.FirebaseUid);
    }

    [Fact]
    public async Task GetUserSettings_WhenExists_ReturnsExisting_NotDefaults()
    {
        await _fixture.ClearAllCollectionsAsync();

        // Seed existing settings
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
        await _collection.InsertOneAsync(seeded);

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
        await _fixture.ClearAllCollectionsAsync();

        var payload = new UserSettings
        {
            PictureUrl = "x",
            AiPersonality = "p",
            ChallengeNotifsEnabled = false,
            FriendReqNotifsEnabled = false,
            StreakNotifsEnabled = false,
            MealNotifsEnabled = false
        };

        var result = await _ctrl.UpdateUserData("missing-uid", payload);
        var notFound = Assert.IsType<NotFoundObjectResult>(result);
        Assert.Contains("not found", notFound.Value!.ToString(), System.StringComparison.OrdinalIgnoreCase);
    }

    [Fact]
    public async Task UpdateUserData_Ok_When_UserExists()
    {
        await _fixture.ClearAllCollectionsAsync();

        // create an existing settings doc
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
        var fromDb = await _collection.Find(x => x.FirebaseUid == "u2").FirstOrDefaultAsync();
        Assert.NotNull(fromDb);
        Assert.Equal("http://new/pic.png", fromDb.PictureUrl);
        Assert.Equal("snarky", fromDb.AiPersonality);
        Assert.False(fromDb.ChallengeNotifsEnabled);
        Assert.True(fromDb.FriendReqNotifsEnabled);
        Assert.False(fromDb.StreakNotifsEnabled);
        Assert.True(fromDb.MealNotifsEnabled);
    }
}
