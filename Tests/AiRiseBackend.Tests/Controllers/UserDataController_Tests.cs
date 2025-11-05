using AiRise.Controllers;
using AiRise.Models;
using AiRise.Models.User;
using AiRise.Services;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;
using Mongo2Go;
using MongoDB.Driver;
using Moq;

public class UserDataController_Tests : IDisposable
{
    private readonly MongoDbRunner _runner;
    private readonly IMongoDatabase _db;
    private readonly IMongoCollection<UserData> _collection;
    private readonly IMongoCollection<UserChallenges> _challengeCollection;
    private readonly UserDataController _controller;

    public UserDataController_Tests()
    {
        // Spin up in-memory MongoDB instance
        _runner = MongoDbRunner.Start(singleNodeReplSet: true);
        var client = new MongoClient(_runner.ConnectionString);
        _db = client.GetDatabase($"UserDataCtrl_{Guid.NewGuid()}");

        _collection = _db.GetCollection<UserData>("user.data_ctrl");
        _challengeCollection = _db.GetCollection<UserChallenges>("user.challenges_ctrl");
        _collection.DeleteMany(FilterDefinition<UserData>.Empty);
        _challengeCollection.DeleteMany(FilterDefinition<UserChallenges>.Empty);

        var mockProgramService = CreateUserProgramServiceMock();

        var svc = new UserDataService(_collection, _challengeCollection, mockProgramService.Object);
        _controller = new UserDataController(svc, new NoopLogger<UserController>());
    }

    // --------- Mock setup ----------
    private static Mock<IUserProgramService> CreateUserProgramServiceMock()
    {
        var programService = new Mock<IUserProgramService>();

        programService.Setup(s => s.UpdatePreferencesAsync(
            It.IsAny<string>(),
            It.IsAny<ProgramType>(),
            It.IsAny<List<string>>(),
            It.IsAny<ProgramPreferences>(),
            It.IsAny<CancellationToken>()))
            .ReturnsAsync(true);

        programService.Setup(s => s.AssignFromExplicitAsync(
            It.IsAny<string>(),
            It.IsAny<ProgramType>(),
            It.IsAny<List<string>>(),
            It.IsAny<ProgramPreferences?>(),
            It.IsAny<CancellationToken>()))
            .ReturnsAsync(new UserProgramDoc
            {
                FirebaseUid = "test-uid",
                Program = new UserProgram { Days = 3, Type = ProgramType.Gym }
            });

        programService.Setup(s => s.RelabelDayNamesAsync(
            It.IsAny<string>(),
            It.IsAny<List<string>>(),
            It.IsAny<ProgramPreferences?>(),
            It.IsAny<CancellationToken>()))
            .ReturnsAsync(true);

        return programService;
    }

    private sealed class NoopLogger<T> : ILogger<T>
    {
        public IDisposable BeginScope<TState>(TState state) => new Nop();
        public bool IsEnabled(LogLevel logLevel) => false;
        public void Log<TState>(LogLevel logLevel, EventId eventId, TState state, Exception? exception, Func<TState, Exception?, string> formatter) { }
        private sealed class Nop : IDisposable { public void Dispose() { } }
    }

    // --------- Tests ----------

    [Fact]
    public async Task GetUserData_Returns_Data()
    {
        var user = UserDataFactory.Create();
        await _collection.InsertOneAsync(user);

        var result = await _controller.GetUserData(user.FirebaseUid);

        Assert.NotNull(result);
        Assert.Equal(user.FirebaseUid, result.FirebaseUid);
        Assert.Equal(user.Email, result.Email);
        Assert.Equal(user.FirstName, result.FirstName);
    }

    [Fact]
    public async Task GetUserData_Returns_Null_When_NotFound()
    {
        var result = await _controller.GetUserData("missing-uid");
        Assert.Null(result);
    }

    [Fact]
    public async Task UpdateUserData_Ok()
    {
        var user = UserDataFactory.Create();
        await _collection.InsertOneAsync(user);

        var updated = UserDataFactory.Create(firstName: "New", lastName: "Name");

        var result = await _controller.UpdateUserData(user.FirebaseUid, updated);

        var ok = Assert.IsType<OkObjectResult>(result);
        var updatedUser = await _collection.Find(u => u.FirebaseUid == user.FirebaseUid).FirstOrDefaultAsync();

        Assert.Equal(200, ok.StatusCode);
        Assert.Equal("New", updatedUser.FirstName);
        Assert.Equal("Name", updatedUser.LastName);
    }

    [Fact]
    public async Task UpdateUserData_NotFound()
    {
        var result = await _controller.UpdateUserData("nope", UserDataFactory.Create());
        var notFound = Assert.IsType<NotFoundObjectResult>(result);
        Assert.Equal(404, notFound.StatusCode);
    }

    [Fact]
    public async Task UpdateUserDOB_Ok()
    {
        var user = UserDataFactory.Create();
        await _collection.InsertOneAsync(user);

        var updated = UserDataFactory.Create(dobDay: 1, dobMonth: 1, dobYear: 2000);
        var result = await _controller.UpdateUserDOB(user.FirebaseUid, updated);

        var ok = Assert.IsType<OkObjectResult>(result);
        Assert.Equal(200, ok.StatusCode);
    }

    [Fact]
    public async Task UpdateUserDOB_NotFound()
    {
        var result = await _controller.UpdateUserDOB("nope", UserDataFactory.Create());
        var notFound = Assert.IsType<NotFoundObjectResult>(result);
        Assert.Equal(404, notFound.StatusCode);
    }

    [Fact]
    public async Task UpdateUserName_Ok()
    {
        var user = UserDataFactory.Create();
        await _collection.InsertOneAsync(user);

        var updated = UserDataFactory.Create(firstName: "A", lastName: "B");
        var result = await _controller.UpdateUserName(user.FirebaseUid, updated);

        var ok = Assert.IsType<OkObjectResult>(result);
        var fromDb = await _collection.Find(x => x.FirebaseUid == user.FirebaseUid).FirstOrDefaultAsync();

        Assert.Equal("A", fromDb.FirstName);
        Assert.Equal("B", fromDb.LastName);
    }

        [Fact]
    public async Task UpdateUserName_NotFound()
    {
        var result = await _controller.UpdateUserName("nope", UserDataFactory.Create());
        var notFound = Assert.IsType<NotFoundObjectResult>(result);
        Assert.Equal(404, notFound.StatusCode);
    }

    [Fact]
    public async Task UpdateUserHeight_Ok()
    {
        var user = UserDataFactory.Create(heightMetric: false, heightValue: 160);
        await _collection.InsertOneAsync(user);

        var updated = UserDataFactory.Create(heightMetric: true, heightValue: 185);
        var result = await _controller.UpdateUserHeight(user.FirebaseUid, updated);

        var ok = Assert.IsType<OkObjectResult>(result);
        var fromDb = await _collection.Find(x => x.FirebaseUid == user.FirebaseUid).FirstOrDefaultAsync();

        Assert.True(fromDb.HeightMetric);
        Assert.Equal(185, fromDb.HeightValue);
    }

        [Fact]
    public async Task UpdateUserHeight_NotFound()
    {
        var result = await _controller.UpdateUserHeight("nope", UserDataFactory.Create());
        var notFound = Assert.IsType<NotFoundObjectResult>(result);
        Assert.Equal(404, notFound.StatusCode);
    }

    [Fact]
    public async Task UpdateUserWeight_Ok()
    {
        var user = UserDataFactory.Create(weightMetric: false, weightValue: 60);
        await _collection.InsertOneAsync(user);

        var updated = UserDataFactory.Create(weightMetric: true, weightValue: 72);
        var result = await _controller.UpdateUserWeight(user.FirebaseUid, updated);

        var ok = Assert.IsType<OkObjectResult>(result);
        var fromDb = await _collection.Find(x => x.FirebaseUid == user.FirebaseUid).FirstOrDefaultAsync();

        Assert.True(fromDb.WeightMetric);
        Assert.Equal(72, fromDb.WeightValue);
    }

    [Fact]
    public async Task UpdateUserWeight_NotFound()
    {
        var result = await _controller.UpdateUserWeight("nope", UserDataFactory.Create());
        var notFound = Assert.IsType<NotFoundObjectResult>(result);
        Assert.Equal(404, notFound.StatusCode);
    }
    public void Dispose() => _runner.Dispose();
}
