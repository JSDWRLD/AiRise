using System.Text.Json;
using AiRise.Controllers;
using AiRise.Models;
using AiRise.Models.User;
using AiRise.Services;
using Microsoft.AspNetCore.Mvc;
using Microsoft.VisualStudio.TestPlatform.CommunicationUtilities;
using MongoDB.Bson;
using MongoDB.Driver;
using Moq;

public class UserDataController_IntegrationTests : IClassFixture<MongoIntegrationTest<UserData>>
{
    private readonly MongoIntegrationTest<UserData> _fixture;
    private readonly UserDataController _controller;
    private readonly IMongoCollection<UserData> _collection;

    private static Mock<IUserProgramService> CreateUserProgramServiceMock()
    {
        var programService = new Mock<IUserProgramService>();

        programService.Setup(s => s.UpdatePreferencesAsync(
            It.IsAny<string>(),                 //firebaseUid
            It.IsAny<ProgramType>(),            // program type
            It.IsAny<List<string>>(),           //workoutDays
            It.IsAny<ProgramPreferences>(),     //preferences
            It.IsAny<CancellationToken>()))
            .ReturnsAsync(true);

        programService.Setup(s => s.AssignFromExplicitAsync(
            It.IsAny<string>(),                 //firebaseUid
            It.IsAny<ProgramType>(),            // program type
            It.IsAny<List<string>>(),               //workoutDays
            It.IsAny<ProgramPreferences?>(),    //preferences
            It.IsAny<CancellationToken>()))
            .ReturnsAsync(new UserProgramDoc
            {
                FirebaseUid = "test-uid",
                Program = new UserProgram
                {
                    Days = 3,
                    Type = ProgramType.Gym
                }
            });
        
        programService.Setup(s => s.RelabelDayNamesAsync(
            It.IsAny<string>(),                 //firebaseUid
            It.IsAny<List<string>>(),           //workoutDays
            It.IsAny<ProgramPreferences?>(),    //preferences
            It.IsAny<CancellationToken>()))
            .ReturnsAsync(true);
        return programService;
    }

    public UserDataController_IntegrationTests(MongoIntegrationTest<UserData> fixture)
    {
        _fixture = fixture;
        _collection = fixture.GetCollection<UserData>("TestCollection_UserData");
        _fixture.ClearAllCollectionsAsync().GetAwaiter().GetResult();

        var challengeColl = fixture.GetCollection<UserChallenges>("TestCollection_UserChallenges");
        var programService = CreateUserProgramServiceMock();

        var svc = new UserDataService(_collection, challengeColl, programService.Object);
        _controller = new UserDataController(svc, null);
    }

    [Fact]
    public async Task GetUserData_Returns_Data()
    {
        await _fixture.ClearAllCollectionsAsync();

        // Arrange
        var user = UserDataFactory.Create();
        // user.Id = ObjectId.GenerateNewId().ToString();
        await _collection.InsertOneAsync(user);

        // Act
        var result = await _controller.GetUserData("test-uid");

        // Assert
        Assert.NotNull(result);
        Assert.Equal(result.FirebaseUid, user.FirebaseUid);
        Assert.Equal(result.IsAdmin, user.IsAdmin);
        Assert.Equal(result.FirstName, user.FirstName);
        Assert.Equal(result.MiddleName, user.MiddleName);
        Assert.Equal(result.LastName, user.LastName);
        Assert.Equal(result.Email, user.Email);
        Assert.Equal(result.WorkoutGoal, user.WorkoutGoal);
        Assert.Equal(result.WorkoutEquipment, user.WorkoutEquipment);
        Assert.Equal(result.WorkoutLength, user.WorkoutLength);
        Assert.Equal(result.WorkoutDays, user.WorkoutDays);
        Assert.Equal(result.FitnessLevel, user.FitnessLevel);
        Assert.Equal(result.WorkoutTime, user.WorkoutTime);
        Assert.Equal(result.DietaryGoal, user.DietaryGoal);
        Assert.Equal(result.ActivityLevel, user.ActivityLevel);
        Assert.Equal(result.HeightMetric, user.HeightMetric);
        Assert.Equal(result.HeightValue, user.HeightValue);
        Assert.Equal(result.WeightMetric, user.WeightMetric);
        Assert.Equal(result.WeightValue, user.WeightValue);
        Assert.Equal(result.DobDay, user.DobDay);
        Assert.Equal(result.DobMonth, user.DobMonth);
        Assert.Equal(result.DobYear, user.DobYear);
        Assert.Equal(result.FullName, user.FullName);
    }

    [Fact]
    public async Task GetUserData_Returns_Null_When_Not_Found()
    {
        await _fixture.ClearAllCollectionsAsync();

        var result = await _controller.GetUserData("non-existent-uid");

        Assert.Null(result);
    }

    [Fact]
    public async Task UpdateUserData_Updates_And_Returns_True(){
        await _fixture.ClearAllCollectionsAsync();

        var user = UserDataFactory.Create();
        await _collection.InsertOneAsync(user);

        var updated = UserDataFactory.Create(
            firstName: "New",
            lastName: "Name");

        var result = await _controller.UpdateUserData(user.FirebaseUid, updated);

        var ok = Assert.IsType<OkObjectResult>(result);
        var updatedUser = await _collection.Find(u => u.FirebaseUid == user.FirebaseUid).FirstOrDefaultAsync();
        Assert.Equal(200, ok.StatusCode);
        Assert.Equal("New", updatedUser.FirstName);
        Assert.Equal("Name", updatedUser.LastName);
    }

    [Fact]
    public async Task UpdateUserData_Returns_NotFound_When_NoMatch()
    {
        await _fixture.ClearAllCollectionsAsync();

        var result = await _controller.UpdateUserData("non-existent-uid", UserDataFactory.Create());

        var notFound = Assert.IsType<NotFoundObjectResult>(result);
        Assert.Equal(404, notFound.StatusCode);
    }

    [Fact]
    public async Task UpdateUserDOB_Updates_And_Returns_True()
    {
        await _fixture.ClearAllCollectionsAsync();
        var user = UserDataFactory.Create();
        await _collection.InsertOneAsync(user);
        var updated = UserDataFactory.Create(dobDay: 1, dobMonth: 1, dobYear: 2000);
        var result = await _controller.UpdateUserDOB(user.FirebaseUid, updated);
        var ok = Assert.IsType<OkObjectResult>(result);
        Assert.Equal(200, ok.StatusCode);
    }

    [Fact]
    public async Task UpdateUserDOB_Returns_NotFound_When_NoMatch()
    {
        await _fixture.ClearAllCollectionsAsync();
        
        var result = await _controller.UpdateUserDOB("non-existent-uid", UserDataFactory.Create());
        
        var notFound = Assert.IsType<NotFoundObjectResult>(result);
        Assert.Equal(404, notFound.StatusCode);
    }

    [Fact]
    public async Task UpdateUserName_Updates_And_Returns_True()
    {
        await _fixture.ClearAllCollectionsAsync();

        var user = UserDataFactory.Create();
        await _collection.InsertOneAsync(user);

        var updated = UserDataFactory.Create(firstName: "New", lastName: "Name");

        var result = await _controller.UpdateUserName(user.FirebaseUid, updated);

        var ok = Assert.IsType<OkObjectResult>(result);
        var updatedUser = await _collection.Find(u => u.FirebaseUid == user.FirebaseUid).FirstOrDefaultAsync();
        Assert.Equal(200, ok.StatusCode);
        Assert.Equal("New", updatedUser.FirstName);
        Assert.Equal("Name", updatedUser.LastName);
    }

    [Fact]
    public async Task UpdateUserName_Returns_NotFound_When_NoMatch()
    {
        await _fixture.ClearAllCollectionsAsync();

        var result = await _controller.UpdateUserName("non-existent-uid", UserDataFactory.Create());
        var notFound = Assert.IsType<NotFoundObjectResult>(result);
        Assert.Equal(404, notFound.StatusCode);
    }

    [Fact]
    public async Task UpdateUserHeight_Updates_And_Returns_True()
    {
        await _fixture.ClearAllCollectionsAsync();

        var user = UserDataFactory.Create();
        await _collection.InsertOneAsync(user);

        var updated = UserDataFactory.Create(heightMetric: true, heightValue: 185);
        var result = await _controller.UpdateUserHeight(user.FirebaseUid, updated);

        var ok = Assert.IsType<OkObjectResult>(result);
        Assert.Equal(200, ok.StatusCode);
        var updatedUser = await _collection.Find(u => u.FirebaseUid == user.FirebaseUid).FirstOrDefaultAsync();
        Assert.True(updatedUser.HeightMetric);
        Assert.Equal(185, updatedUser.HeightValue);
    }

    [Fact]
    public async Task UpdateUserHeight_Returns_NotFound_When_NoMatch()
    {
        await _fixture.ClearAllCollectionsAsync();

        var result = await _controller.UpdateUserHeight("non-existent-uid", UserDataFactory.Create());

        var notFound = Assert.IsType<NotFoundObjectResult>(result);
        Assert.Equal(404, notFound.StatusCode);
    }

    [Fact]
    public async Task UpdateUserWeight_Updates_And_Returns_True()
    {
        await _fixture.ClearAllCollectionsAsync();

        var user = UserDataFactory.Create();
        await _collection.InsertOneAsync(user);

        var updated = UserDataFactory.Create(weightMetric: true, weightValue: 70);
        var result = await _controller.UpdateUserWeight(user.FirebaseUid, updated);

        var ok = Assert.IsType<OkObjectResult>(result);
        var updatedUser = await _collection.Find(u => u.FirebaseUid == user.FirebaseUid).FirstOrDefaultAsync();
        Assert.Equal(200, ok.StatusCode);
        Assert.True(updatedUser.WeightMetric);
        Assert.Equal(70, updatedUser.WeightValue);
    }

    [Fact]
    public async Task UpdateUserWeight_Returns_NotFound_When_NoMatch()
    {
        await _fixture.ClearAllCollectionsAsync();

        var result = await _controller.UpdateUserWeight("non-existent-uid", UserDataFactory.Create());

        var notFound = Assert.IsType<NotFoundObjectResult>(result);
        Assert.Equal(404, notFound.StatusCode);
    }

    [Fact(Skip = "Atlas Search not supported in Mongo2Go. Run only in Atlas environment.")]
    public async Task SearchUsersByName_Returns_Users()
    {
        await _fixture.ClearAllCollectionsAsync();

        var user1 = UserDataFactory.Create(firstName: "John", lastName: "Doe");
        var user2 = UserDataFactory.Create(firstName: "Jane", lastName: "Smith");
        await _collection.InsertManyAsync(new[] { user1, user2 });

        var result = await _controller.SearchUsersByNameAsync("test-uid", query:"J");

        var ok = Assert.IsType<OkObjectResult>(result);
        var payload = Assert.IsType<UserList>(ok.Value);
        Assert.Equal(2, payload.Users.Count);
        Assert.Contains(payload.Users, u => u.firstName == "John");
        Assert.Contains(payload.Users, u => u.firstName == "Jane");
    }
}