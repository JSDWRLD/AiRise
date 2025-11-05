using AiRise.Models;
using AiRise.Models.User;
using AiRise.Services;
using MongoDB.Driver;
using Moq;

public class UserDataService_Tests
{
    private static Mock<IMongoCollection<UserData>> CreateUserDataCollectionMock()
    {
        var collMock = new Mock<IMongoCollection<UserData>>();
        var indexMgrMock = new Mock<IMongoIndexManager<UserData>>();

        collMock.SetupGet(c => c.Indexes).Returns(indexMgrMock.Object);
        indexMgrMock
            .Setup(i => i.CreateOne(It.IsAny<CreateIndexModel<UserData>>(), null, default))
            .Returns("idx");

        return collMock;
    }

    private static Mock<IMongoCollection<UserChallenges>> CreateUserChallengesCollectionMock()
    {
        var collMock = new Mock<IMongoCollection<UserChallenges>>();
        return collMock;
    }

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

    [Fact]
    public async Task GetUserData_Returns_Data()
    {
        var userDataColl = CreateUserDataCollectionMock();
        var challengesColl = CreateUserChallengesCollectionMock();
        var programService = CreateUserProgramServiceMock();
        
        var expected = UserDataFactory.Create(firstName: "New", lastName: "User");

        userDataColl.SetupFindAsyncSingle(expected);
        
        var svc = new UserDataService(userDataColl.Object, challengesColl.Object, programService.Object);
        
        var result = await svc.GetUserData("test-uid");
        
        Assert.NotNull(result);
        Assert.Equal("New", result.FirstName);
        Assert.Equal("User", result.LastName);
    }

    [Fact]
    public async Task GetUserData_Returns_Null_When_Not_Found()
    {
        var userDataColl = CreateUserDataCollectionMock();
        var challengesColl = CreateUserChallengesCollectionMock();
        var programService = CreateUserProgramServiceMock();

        userDataColl.SetupFindAsyncSingle((UserData?)null);

        var svc = new UserDataService(userDataColl.Object, challengesColl.Object, programService.Object);

        var result = await svc.GetUserData("non-existent-uid");

        Assert.Null(result);
    }

    [Fact]
    public async Task UpdateUserDataAsync_Ok()
    {
        var userDataColl = CreateUserDataCollectionMock();
        var challengesColl = CreateUserChallengesCollectionMock();
        var programService = CreateUserProgramServiceMock();

        var existing = UserDataFactory.Create();

        var updated = UserDataFactory.Create(
            firstName: "New",
            lastName: "Name");

        userDataColl.SetupFindAsyncSingle(existing);

        userDataColl
            .Setup(c => c.UpdateOneAsync(
                It.IsAny<FilterDefinition<UserData>>(),
                It.IsAny<UpdateDefinition<UserData>>(),
                It.IsAny<UpdateOptions>(),
                It.IsAny<CancellationToken>()))
            .ReturnsAsync(new UpdateResult.Acknowledged(1, 1, null));

        var svc = new UserDataService(userDataColl.Object, challengesColl.Object, programService.Object);

        var result = await svc.UpdateUserDataAsync("test-uid", updated);

        Assert.True(result);
        
        userDataColl.Verify(c => c.UpdateOneAsync(
            It.IsAny<FilterDefinition<UserData>>(),
            It.IsAny<UpdateDefinition<UserData>>(),
            default,
            default), Times.Once);
    }

    [Fact]
    public async Task UpdateUserDataAsync_Notfound_When_NoMatch(){
        var userDataColl = CreateUserDataCollectionMock();
        var challengesColl = CreateUserChallengesCollectionMock();
        var programService = CreateUserProgramServiceMock();

        var existing = UserDataFactory.Create();
        var updated = UserDataFactory.Create(firebaseUid: "fake", firstName: "New");

        userDataColl.SetupFindAsyncSingle(existing);

        userDataColl
            .Setup(c => c.UpdateOneAsync(
                It.IsAny<FilterDefinition<UserData>>(),
                It.IsAny<UpdateDefinition<UserData>>(),
                It.IsAny<UpdateOptions>(),
                It.IsAny<CancellationToken>()))
            .ReturnsAsync(new UpdateResult.Acknowledged(0, 0, null));

        var svc = new UserDataService(userDataColl.Object, challengesColl.Object, programService.Object);

        var result = await svc.UpdateUserDataAsync("fake", updated);

        Assert.False(result);
    }

    [Fact]
    public async Task UpdateUserNameAsync_Updates_And_Returns_True()
    {
        var userDataColl = CreateUserDataCollectionMock();
        var challengesColl = CreateUserChallengesCollectionMock();
        var programService = CreateUserProgramServiceMock();

        var updatedData = UserDataFactory.Create(
            firstName: "Updated",
            middleName: "New",
            lastName: "Name"
        );

        userDataColl
            .Setup(c => c.UpdateOneAsync(
                It.IsAny<FilterDefinition<UserData>>(),
                It.IsAny<UpdateDefinition<UserData>>(),
                It.IsAny<UpdateOptions>(),
                It.IsAny<CancellationToken>()))
            .ReturnsAsync(new UpdateResult.Acknowledged(1, 1, null));

        var svc = new UserDataService(userDataColl.Object, challengesColl.Object, programService.Object);

        var result = await svc.UpdateUserNameAsync("test-uid", updatedData);

        Assert.True(result);

        userDataColl.Verify(c => c.UpdateOneAsync(
            It.IsAny<FilterDefinition<UserData>>(),
            It.IsAny<UpdateDefinition<UserData>>(),
            default,
            default), Times.Once);
    }

    [Fact]
    public async Task UpdateUserNameAsync_Returns_False_When_NoModification()
    {
        var userDataColl = CreateUserDataCollectionMock();
        var challengesColl = CreateUserChallengesCollectionMock();
        var programService = CreateUserProgramServiceMock();

        var existing = UserDataFactory.Create();

        userDataColl
            .Setup(c => c.UpdateOneAsync(
                It.IsAny<FilterDefinition<UserData>>(),
                It.IsAny<UpdateDefinition<UserData>>(),
                It.IsAny<UpdateOptions>(),
                It.IsAny<CancellationToken>()))
            .ReturnsAsync(new UpdateResult.Acknowledged(1, 0, null));

        var svc = new UserDataService(userDataColl.Object, challengesColl.Object, programService.Object);

        var result = await svc.UpdateUserNameAsync("test-uid", existing);

        Assert.False(result);

        userDataColl.Verify(c => c.UpdateOneAsync(
            It.IsAny<FilterDefinition<UserData>>(),
            It.IsAny<UpdateDefinition<UserData>>(),
            default,
            default), Times.Once);
    }

        [Fact]
    public async Task UpdateUserDOBAsync_Updates_And_Returns_True()
    {
        var userDataColl = CreateUserDataCollectionMock();
        var challengesColl = CreateUserChallengesCollectionMock();
        var programService = CreateUserProgramServiceMock();

        var updatedData = UserDataFactory.Create(
            dobDay: 2,
            dobMonth: 2,
            dobYear: 2001
        );

        userDataColl
            .Setup(c => c.UpdateOneAsync(
                It.IsAny<FilterDefinition<UserData>>(),
                It.IsAny<UpdateDefinition<UserData>>(),
                It.IsAny<UpdateOptions>(),
                It.IsAny<CancellationToken>()))
            .ReturnsAsync(new UpdateResult.Acknowledged(1, 1, null));

        var svc = new UserDataService(userDataColl.Object, challengesColl.Object, programService.Object);

        var result = await svc.UpdateUserDOBAsync("test-uid", updatedData);

        Assert.True(result);

        userDataColl.Verify(c => c.UpdateOneAsync(
            It.IsAny<FilterDefinition<UserData>>(),
            It.IsAny<UpdateDefinition<UserData>>(),
            default,
            default), Times.Once);
    }

    [Fact]
    public async Task UpdateDOBAsync_Returns_False_When_NoModification()
    {
        var userDataColl = CreateUserDataCollectionMock();
        var challengesColl = CreateUserChallengesCollectionMock();
        var programService = CreateUserProgramServiceMock();

        var existing = UserDataFactory.Create();

        userDataColl
            .Setup(c => c.UpdateOneAsync(
                It.IsAny<FilterDefinition<UserData>>(),
                It.IsAny<UpdateDefinition<UserData>>(),
                It.IsAny<UpdateOptions>(),
                It.IsAny<CancellationToken>()))
            .ReturnsAsync(new UpdateResult.Acknowledged(1, 0, null));

        var svc = new UserDataService(userDataColl.Object, challengesColl.Object, programService.Object);

        var result = await svc.UpdateUserDOBAsync("test-uid", existing);

        Assert.False(result);

        userDataColl.Verify(c => c.UpdateOneAsync(
            It.IsAny<FilterDefinition<UserData>>(),
            It.IsAny<UpdateDefinition<UserData>>(),
            default,
            default), Times.Once);
    }

        [Fact]
    public async Task UpdateUserHeightAsync_Updates_And_Returns_True()
    {
        var userDataColl = CreateUserDataCollectionMock();
        var challengesColl = CreateUserChallengesCollectionMock();
        var programService = CreateUserProgramServiceMock();

        var updatedData = UserDataFactory.Create(
            heightMetric: true,
            heightValue: 185
        );

        userDataColl
            .Setup(c => c.UpdateOneAsync(
                It.IsAny<FilterDefinition<UserData>>(),
                It.IsAny<UpdateDefinition<UserData>>(),
                It.IsAny<UpdateOptions>(),
                It.IsAny<CancellationToken>()))
            .ReturnsAsync(new UpdateResult.Acknowledged(1, 1, null));

        var svc = new UserDataService(userDataColl.Object, challengesColl.Object, programService.Object);

        var result = await svc.UpdateUserHeightAsync("test-uid", updatedData);

        Assert.True(result);

        userDataColl.Verify(c => c.UpdateOneAsync(
            It.IsAny<FilterDefinition<UserData>>(),
            It.IsAny<UpdateDefinition<UserData>>(),
            default,
            default), Times.Once);
    }

    [Fact]
    public async Task UpdateUserHeightAsync_Returns_False_When_NoModification()
    {
        var userDataColl = CreateUserDataCollectionMock();
        var challengesColl = CreateUserChallengesCollectionMock();
        var programService = CreateUserProgramServiceMock();

        var existing = UserDataFactory.Create();

        userDataColl
            .Setup(c => c.UpdateOneAsync(
                It.IsAny<FilterDefinition<UserData>>(),
                It.IsAny<UpdateDefinition<UserData>>(),
                It.IsAny<UpdateOptions>(),
                It.IsAny<CancellationToken>()))
            .ReturnsAsync(new UpdateResult.Acknowledged(1, 0, null));

        var svc = new UserDataService(userDataColl.Object, challengesColl.Object, programService.Object);

        var result = await svc.UpdateUserHeightAsync("test-uid", existing);

        Assert.False(result);

        userDataColl.Verify(c => c.UpdateOneAsync(
            It.IsAny<FilterDefinition<UserData>>(),
            It.IsAny<UpdateDefinition<UserData>>(),
            default,
            default), Times.Once);
    }

        [Fact]
    public async Task UpdateUserWeightAsync_Updates_And_Returns_True()
    {
        var userDataColl = CreateUserDataCollectionMock();
        var challengesColl = CreateUserChallengesCollectionMock();
        var programService = CreateUserProgramServiceMock();

        var updatedData = UserDataFactory.Create(
            weightMetric: true,
            weightValue: 78
        );

        userDataColl
            .Setup(c => c.UpdateOneAsync(
                It.IsAny<FilterDefinition<UserData>>(),
                It.IsAny<UpdateDefinition<UserData>>(),
                It.IsAny<UpdateOptions>(),
                It.IsAny<CancellationToken>()))
            .ReturnsAsync(new UpdateResult.Acknowledged(1, 1, null));

        var svc = new UserDataService(userDataColl.Object, challengesColl.Object, programService.Object);

        var result = await svc.UpdateUserWeightAsync("test-uid", updatedData);

        Assert.True(result);

        userDataColl.Verify(c => c.UpdateOneAsync(
            It.IsAny<FilterDefinition<UserData>>(),
            It.IsAny<UpdateDefinition<UserData>>(),
            default,
            default), Times.Once);
    }

    [Fact]
    public async Task UpdateUserWeightAsync_Returns_False_When_NoModification()
    {
        var userDataColl = CreateUserDataCollectionMock();
        var challengesColl = CreateUserChallengesCollectionMock();
        var programService = CreateUserProgramServiceMock();

        var existing = UserDataFactory.Create();

        userDataColl
            .Setup(c => c.UpdateOneAsync(
                It.IsAny<FilterDefinition<UserData>>(),
                It.IsAny<UpdateDefinition<UserData>>(),
                It.IsAny<UpdateOptions>(),
                It.IsAny<CancellationToken>()))
            .ReturnsAsync(new UpdateResult.Acknowledged(1, 0, null));

        var svc = new UserDataService(userDataColl.Object, challengesColl.Object, programService.Object);

        var result = await svc.UpdateUserWeightAsync("test-uid", existing);

        Assert.False(result);

        userDataColl.Verify(c => c.UpdateOneAsync(
            It.IsAny<FilterDefinition<UserData>>(),
            It.IsAny<UpdateDefinition<UserData>>(),
            default,
            default), Times.Once);
    }

    [Fact]
    public async Task SearchUsersByNameAsync_Returns_Users()
    {
        var userDataColl = CreateUserDataCollectionMock();
        var challengesColl = CreateUserChallengesCollectionMock();
        var programService = CreateUserProgramServiceMock();
        
        var expectedUsers = new List<UserProfile>
        {
            new UserProfile 
            { 
                firebaseUid = "uid-1",
                firstName = "John",
                lastName = "Doe",
                fullName = "John Doe",
                profile_picture_url = "",
                streak = 10
            },
            new UserProfile 
            { 
                firebaseUid = "uid-2",
                firstName = "Jane",
                lastName = "Smith",
                fullName = "Jane Smith",
                profile_picture_url = "",
                streak = 8
            }
        };
        
        // Mock the aggregate method used to search users by name
        var cursor = MongoMoqHelpers.MakeCursor(expectedUsers);
        userDataColl
            .Setup(c => c.Aggregate<UserProfile>(
                It.IsAny<PipelineDefinition<UserData, UserProfile>>(),
                It.IsAny<AggregateOptions>(),
                It.IsAny<CancellationToken>()))
            .Returns(cursor.Object);

        var svc = new UserDataService(userDataColl.Object, challengesColl.Object, programService.Object);

        var result = await svc.SearchUsersByNameAsync("John");

        Assert.NotNull(result);
        Assert.Equal(2, result.Users.Count);
        Assert.Equal("uid-1", result.Users[0].firebaseUid);
        Assert.Equal("John", result.Users[0].firstName);
        Assert.Equal("Doe", result.Users[0].lastName);
    }
}

public static class UserDataFactory
{
    public static UserData Create(
        string firebaseUid = "test-uid",
        string email = "test@example.com",
        string firstName = "John",
        string middleName = "",
        string lastName = "Doe",
        string workoutGoal = "Strength",
        string workoutEquipment = "Bodyweight",
        int workoutLength = 30,
        List<string>? workoutDays = null,
        string fitnessLevel = "Beginner",
        string workoutTime = "Morning",
        string dietaryGoal = "Maintain",
        string activityLevel = "Moderate",
        bool heightMetric = false,
        bool weightMetric = false,
        int heightValue = 70,
        int weightValue = 180,
        int dobDay = 1,
        int dobMonth = 1,
        int dobYear = 1990)
    {
        return new UserData
        {
            FirebaseUid = firebaseUid,
            Email = email,
            FirstName = firstName,
            MiddleName = middleName,
            LastName = lastName,
            FullName = string.Join(" ", new[] { firstName, middleName, lastName }.Where(s => !string.IsNullOrEmpty(s))),
            WorkoutGoal = workoutGoal,
            WorkoutEquipment = workoutEquipment,
            WorkoutLength = workoutLength,
            WorkoutDays = workoutDays ?? new List<string> { "Monday", "Wednesday", "Friday" },
            FitnessLevel = fitnessLevel,
            WorkoutTime = workoutTime,
            DietaryGoal = dietaryGoal,
            ActivityLevel = activityLevel,
            HeightMetric = heightMetric,
            HeightValue = heightValue,
            WeightMetric = weightMetric,
            WeightValue = weightValue,
            DobDay = dobDay,
            DobMonth = dobMonth,
            DobYear = dobYear,
        };
    }
    
}