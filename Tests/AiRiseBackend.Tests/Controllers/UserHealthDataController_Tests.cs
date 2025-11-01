using System;
using System.Threading.Tasks;
using AiRise.Controllers;
using AiRise.Models.DTOs;
using AiRise.Models.User;
using AiRise.Services;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;
using Mongo2Go;
using MongoDB.Driver;
using Xunit;

public class UserHealthDataController_Tests : IDisposable
{
    private readonly MongoDbRunner _runner;
    private readonly IMongoDatabase _db;
    private readonly IMongoCollection<UserHealthData> _col;
    private readonly UserHealthDataService _svc;
    private readonly UserHealthDataController _ctrl;

    public UserHealthDataController_Tests()
    {
        _runner = MongoDbRunner.Start();
        var client = new MongoClient(_runner.ConnectionString);
        _db = client.GetDatabase($"UHealthCtrl_{Guid.NewGuid()}");
        _col = _db.GetCollection<UserHealthData>("user.healthdata_ctrl");
        _col.DeleteMany(FilterDefinition<UserHealthData>.Empty);

        _svc = new UserHealthDataService(_col);
        _ctrl = new UserHealthDataController(_svc, new NoopLogger<UserController>());
    }

    // Minimal logger (no Moq)
    private sealed class NoopLogger<T> : ILogger<T>
    {
        public IDisposable BeginScope<TState>(TState state) => new Nop();
        public bool IsEnabled(LogLevel logLevel) => false;
        public void Log<TState>(LogLevel logLevel, EventId eventId, TState state, Exception? exception, Func<TState, Exception?, string> formatter) { }
        private sealed class Nop : IDisposable { public void Dispose() { } }
    }

    [Fact]
    public async Task GetUserHealthData_WhenMissing_CreatesDefault_AndReturns()
    {
        var doc = await _ctrl.GetUserHealthData("uid-missing");

        Assert.NotNull(doc);
        Assert.Equal("uid-missing", doc.FirebaseUid);

        var persisted = await _col.Find(x => x.FirebaseUid == "uid-missing").FirstOrDefaultAsync();
        Assert.NotNull(persisted);
    }

    [Fact]
    public async Task UpdateUserHealthData_Ok_When_Existing_And_Valid()
    {
        // ensure a document exists (Get will create)
        await _ctrl.GetUserHealthData("u1");

        var payload = new HealthData
        {
            CaloriesBurned = 500,
            CaloriesEaten = 1800,
            CaloriesTarget = 2000,
            Steps = 8000,
            Sleep = 7.0,
            Hydration = 90.0,
            HydrationTarget = 100.0,
            LocalDate = new DateOnly(2025, 1, 1)
        };

        var res = await _ctrl.UpdateUserHealthData("u1", payload);
        var ok = Assert.IsType<OkObjectResult>(res);

        var fromDb = await _col.Find(x => x.FirebaseUid == "u1").FirstOrDefaultAsync();
        Assert.NotNull(fromDb);
        Assert.Equal(500, fromDb.CaloriesBurned);
        Assert.Equal(1800, fromDb.CaloriesEaten);
        Assert.Equal(2000, fromDb.CaloriesTarget);
        Assert.Equal(8000, fromDb.Steps);
        Assert.Equal(7.0, fromDb.Sleep);
        Assert.Equal(90.0, fromDb.Hydration);
        Assert.Equal(100.0, fromDb.HydrationTarget);
        Assert.Equal(new DateOnly(2025, 1, 1), fromDb.LocalDate);
    }

    [Fact]
    public async Task UpdateUserHealthData_NotFound_When_NoActualChanges()
    {
        // Create, then attempt an update that results in ModifiedCount == 0
        await _ctrl.GetUserHealthData("u2");

        var current = await _col.Find(x => x.FirebaseUid == "u2").FirstOrDefaultAsync();

        var unchanged = new HealthData
        {
            CaloriesBurned = current.CaloriesBurned,
            CaloriesEaten = current.CaloriesEaten,
            CaloriesTarget = current.CaloriesTarget,
            Steps = current.Steps,
            Sleep = current.Sleep,
            Hydration = current.Hydration,
            HydrationTarget = current.HydrationTarget,
            LocalDate = current.LocalDate        // DateOnly → exact same date
        };

        var res = await _ctrl.UpdateUserHealthData("u2", unchanged);
        Assert.IsType<NotFoundObjectResult>(res);
    }

    [Fact]
    public async Task UpdateUserHealthTargets_NotFound_When_MissingUser()
    {
        var res = await _ctrl.UpdateUserHealthTargets("missing", caloriesTarget: 2200, hydrationTarget: 120);
        Assert.IsType<NotFoundObjectResult>(res);
    }

    [Fact]
    public async Task UpdateUserHealthTargets_Ok_When_Existing()
    {
        await _ctrl.GetUserHealthData("u3"); // ensure exists

        var res = await _ctrl.UpdateUserHealthTargets("u3", caloriesTarget: 2300, hydrationTarget: 110);
        var ok = Assert.IsType<OkObjectResult>(res);

        var fromDb = await _col.Find(x => x.FirebaseUid == "u3").FirstOrDefaultAsync();
        Assert.NotNull(fromDb);
        Assert.Equal(2300, fromDb.CaloriesTarget);
        Assert.Equal(110, fromDb.HydrationTarget);
    }

    public void Dispose() => _runner.Dispose();
}
