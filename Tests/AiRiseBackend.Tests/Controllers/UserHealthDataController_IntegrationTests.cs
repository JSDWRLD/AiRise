using System.Threading.Tasks;
using AiRise.Controllers;
using AiRise.Models.DTOs;
using AiRise.Models.User;
using AiRise.Services;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;
using MongoDB.Driver;
using Xunit;

public class UserHealthDataController_IntegrationTests : IClassFixture<MongoIntegrationTest<UserHealthData>>
{
    private readonly MongoIntegrationTest<UserHealthData> _fixture;
    private readonly IMongoCollection<UserHealthData> _collection;
    private readonly UserHealthDataService _svc;
    private readonly UserHealthDataController _ctrl;

    public UserHealthDataController_IntegrationTests(MongoIntegrationTest<UserHealthData> fixture)
    {
        _fixture = fixture;
        _collection = _fixture.Collection;
        _fixture.ClearCollectionAsync().GetAwaiter().GetResult();

        _svc = new UserHealthDataService(_collection);
        _ctrl = new UserHealthDataController(_svc, new NoopLogger<UserController>());
    }

    private sealed class NoopLogger<T> : ILogger<T>
    {
        public IDisposable BeginScope<TState>(TState state) => new Nop();
        public bool IsEnabled(LogLevel logLevel) => false;
        public void Log<TState>(LogLevel logLevel, EventId eventId, TState state, System.Exception? exception, System.Func<TState, System.Exception?, string> formatter) { }
        private sealed class Nop : System.IDisposable { public void Dispose() { } }
    }

    [Fact]
    public async Task GetUserHealthData_Creates_When_Missing()
    {
        var doc = await _ctrl.GetUserHealthData("fx-uid");
        Assert.NotNull(doc);
        Assert.Equal("fx-uid", doc.FirebaseUid);

        var persisted = await _collection.Find(x => x.FirebaseUid == "fx-uid").FirstOrDefaultAsync();
        Assert.NotNull(persisted);
    }

    [Fact]
    public async Task UpdateUserHealthData_Ok()
    {
        await _ctrl.GetUserHealthData("fx-u1");

        var payload = new HealthData
        {
            CaloriesBurned = 450,
            CaloriesEaten = 1700,
            CaloriesTarget = 1900,
            Steps = 9000,
            Sleep = 6.0,
            Hydration = 80.0,
            HydrationTarget = 90.0,
            LocalDate = new DateOnly(2025, 1, 2)
        };

        var res = await _ctrl.UpdateUserHealthData("fx-u1", payload);
        var ok = Assert.IsType<OkObjectResult>(res);

        var fromDb = await _collection.Find(x => x.FirebaseUid == "fx-u1").FirstOrDefaultAsync();
        Assert.NotNull(fromDb);
        Assert.Equal(450, fromDb.CaloriesBurned);
        Assert.Equal(1700, fromDb.CaloriesEaten);
        Assert.Equal(1900, fromDb.CaloriesTarget);
        Assert.Equal(9000, fromDb.Steps);
        Assert.Equal(6.0, fromDb.Sleep);
        Assert.Equal(80.0, fromDb.Hydration);
        Assert.Equal(90.0, fromDb.HydrationTarget);
        Assert.Equal(new DateOnly(2025, 1, 2), fromDb.LocalDate);
    }

    [Fact]
    public async Task UpdateUserHealthData_NotFound_When_NoChange()
    {
        await _fixture.ClearCollectionAsync();
        await _ctrl.GetUserHealthData("fx-u2");

        var current = await _collection.Find(x => x.FirebaseUid == "fx-u2").FirstOrDefaultAsync();

        var unchanged = new HealthData
        {
            CaloriesBurned = current.CaloriesBurned,
            CaloriesEaten = current.CaloriesEaten,
            CaloriesTarget = current.CaloriesTarget,
            Steps = current.Steps,
            Sleep = current.Sleep,
            Hydration = current.Hydration,
            HydrationTarget = current.HydrationTarget,
            LocalDate = current.LocalDate
        };

        var res = await _ctrl.UpdateUserHealthData("fx-u2", unchanged);
        Assert.IsType<NotFoundObjectResult>(res);
    }

    [Fact]
    public async Task UpdateUserHealthTargets_NotFound_When_Missing()
    {
        var res = await _ctrl.UpdateUserHealthTargets("fx-missing", caloriesTarget: 2000, hydrationTarget: 120);
        Assert.IsType<NotFoundObjectResult>(res);
    }

    [Fact]
    public async Task UpdateUserHealthTargets_Ok_When_Existing()
    {
        await _fixture.ClearCollectionAsync();
        await _ctrl.GetUserHealthData("fx-u3");

        var res = await _ctrl.UpdateUserHealthTargets("fx-u3", caloriesTarget: 2100, hydrationTarget: 95);
        var ok = Assert.IsType<OkObjectResult>(res);

        var fromDb = await _collection.Find(x => x.FirebaseUid == "fx-u3").FirstOrDefaultAsync();
        Assert.NotNull(fromDb);
        Assert.Equal(2100, fromDb.CaloriesTarget);
        Assert.Equal(95, fromDb.HydrationTarget);
    }
}
