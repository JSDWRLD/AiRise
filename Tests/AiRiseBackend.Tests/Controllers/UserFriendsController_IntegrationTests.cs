using AiRise.Controllers;
using AiRise.Models.User;
using AiRise.Services;
using Microsoft.AspNetCore.Mvc;
using MongoDB.Driver;

public class UserFriendsController_IntegrationTests : IClassFixture<MongoIntegrationTest<UserFriends>>
{
    private readonly MongoIntegrationTest<UserFriends> _fixture;
    private readonly IMongoCollection<UserFriends> _collection;
    private readonly UserFriendsService _svc;
    private readonly UserFriendsController _ctrl;

    public UserFriendsController_IntegrationTests(MongoIntegrationTest<UserFriends> fixture)
    {
        _fixture = fixture;
        _collection = _fixture.GetCollection<UserFriends>("user.friends");
        _fixture.ClearAllCollectionsAsync().GetAwaiter().GetResult();
        _svc = new UserFriendsService(_collection, _fixture.GetCollection<UserData>("user.data"));
        _ctrl = new UserFriendsController(_svc, null);
    }

    [Fact]
    public async Task GetUserFriendsList_Ok_With_Data()
    {
        await _fixture.ClearAllCollectionsAsync();

        // Arrange
        var userFriends = UserFriendsFactory.CreateUserFriends("user1", "friend1", "friend2");
        await _collection.InsertOneAsync(userFriends);
        var userData = UserDataFactory.Create("friend1");
        await _fixture.GetCollection<UserData>("user.data").InsertOneAsync(userData);
        userData = UserDataFactory.Create("friend2");
        await _fixture.GetCollection<UserData>("user.data").InsertOneAsync(userData);

        // Act
        var result = await _ctrl.GetUserFriendsList("user1", CancellationToken.None);

        // Assert
        var ok = Assert.IsType<OkObjectResult>(result.Result);
        var payload = Assert.IsType<UserList>(ok.Value);
        Assert.Equal(200, ok.StatusCode);
        Assert.Equal(2, payload.Users.Count);
        Assert.Contains(payload.Users, u => u.firebaseUid == "friend1");
        Assert.Contains(payload.Users, u => u.firebaseUid == "friend2");
    }

    [Fact]
    public async Task GetUserFriendsList_Returns_Empty_List_When_No_Data()
    {
        await _fixture.ClearAllCollectionsAsync();

        // Arrange
        var userFriends = UserFriendsFactory.CreateUserFriends("user1");
        await _collection.InsertOneAsync(userFriends);

        // Act
        var result = await _ctrl.GetUserFriendsList("user1", CancellationToken.None);

        // Assert
        var ok = Assert.IsType<OkObjectResult>(result.Result);
        var payload = Assert.IsType<UserList>(ok.Value);
        Assert.Equal(200, ok.StatusCode);
        Assert.Empty(payload.Users);
    }

    [Fact]
    public async Task AddFriend_Ok(){
        await _fixture.ClearAllCollectionsAsync();

        // Arrange
        var userFriends = UserFriendsFactory.CreateUserFriends("user1");
        await _collection.InsertOneAsync(userFriends);
        var userData = UserDataFactory.Create("friend1");
        await _fixture.GetCollection<UserData>("user.data").InsertOneAsync(userData);

        // Act
        var result = await _ctrl.AddFriend("user1", "friend1", CancellationToken.None);

        // Assert
        var ok = Assert.IsType<OkObjectResult>(result);
        Assert.Equal(200, ok.StatusCode);
        Assert.Equal("{ message = Friend added successfully }", ok.Value!.ToString());
        var updatedUserFriends = await _collection.Find(u => u.FirebaseUid == "user1").FirstOrDefaultAsync();
        Assert.NotNull(updatedUserFriends);
        Assert.Contains("friend1", updatedUserFriends.FriendIds);
    }

    [Fact]
    public async Task AddFriend_Ok_Creates_New_UserFriend(){
        await _fixture.ClearAllCollectionsAsync();

        // Arrange
        var userFriends = UserFriendsFactory.CreateUserFriends("user1");

        // Act
        var result = await _ctrl.AddFriend("user1", "friend1", CancellationToken.None);

        // Assert
        var ok = Assert.IsType<OkObjectResult>(result);
        Assert.Equal(200, ok.StatusCode);
        Assert.Equal("{ message = Friend added successfully }", ok.Value!.ToString());
        var updatedUserFriends = await _collection.Find(u => u.FirebaseUid == "user1").FirstOrDefaultAsync();
        Assert.NotNull(updatedUserFriends);
        Assert.Contains("friend1", updatedUserFriends.FriendIds);
        var updatedFriendUserFriends = await _collection.Find(u => u.FirebaseUid == "friend1").FirstOrDefaultAsync();
        Assert.NotNull(updatedFriendUserFriends);
        Assert.Contains("user1", updatedFriendUserFriends.FriendIds);
    }

    [Fact]
    public async Task DeleteFriend_Ok()
    {
        await _fixture.ClearAllCollectionsAsync();

        // Arrange
        var userFriends = UserFriendsFactory.CreateUserFriends("user1", "friend1");
        await _collection.InsertOneAsync(userFriends);
        var friendUserFriends = UserFriendsFactory.CreateUserFriends("friend1", "user1");
        await _collection.InsertOneAsync(friendUserFriends);

        // Act
        var result = await _ctrl.DeleteFriend("user1", "friend1", CancellationToken.None);

        // Assert
        var ok = Assert.IsType<OkObjectResult>(result);
        Assert.Equal(200, ok.StatusCode);
        Assert.Equal("{ message = Friend deleted successfully }", ok.Value!.ToString());
        var updatedUserFriends = await _collection.Find(u => u.FirebaseUid == "user1").FirstOrDefaultAsync();
        Assert.NotNull(updatedUserFriends);
        Assert.DoesNotContain("friend1", updatedUserFriends.FriendIds);
        var updatedFriendUserFriends = await _collection.Find(u => u.FirebaseUid == "friend1").FirstOrDefaultAsync();
        Assert.NotNull(updatedFriendUserFriends);
        Assert.DoesNotContain("user1", updatedFriendUserFriends.FriendIds);
    }

    [Fact]
    public async Task DeleteFriend_NotFound()
    {
        await _fixture.ClearAllCollectionsAsync();

        // Arrange
        var userFriends = UserFriendsFactory.CreateUserFriends("user1");
        await _collection.InsertOneAsync(userFriends);

        // Act
        var result = await _ctrl.DeleteFriend("user1", "friend1", CancellationToken.None);

        // Assert
        var notFound = Assert.IsType<NotFoundObjectResult>(result);
        Assert.Equal(404, notFound.StatusCode);
        Assert.Equal("{ message = UserFriend not found or deleting failed }", notFound.Value!.ToString());
    }
}