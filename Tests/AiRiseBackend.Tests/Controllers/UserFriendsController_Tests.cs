using AiRise.Controllers;
using AiRise.Models.User;
using AiRise.Services;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Logging.Abstractions;
using Mongo2Go;
using MongoDB.Driver;

public class UserFriendsController_Tests : IDisposable
{
    private readonly MongoDbRunner _runner;
    private readonly IMongoDatabase _db;
    private readonly IMongoCollection<UserFriends> _friendsCollection;
    private readonly IMongoCollection<UserData> _userDataCollection;
    private readonly UserFriendsService _svc;
    private readonly UserFriendsController _ctrl;

    public UserFriendsController_Tests()
    {
        _runner = MongoDbRunner.Start(singleNodeReplSet: true);
        var client = new MongoClient(_runner.ConnectionString);
        _db = client.GetDatabase($"UserFriendsCtrl_{Guid.NewGuid()}");

        _friendsCollection = _db.GetCollection<UserFriends>("user.friends_ctrl");
        _userDataCollection = _db.GetCollection<UserData>("user.data_ctrl");

        _friendsCollection.DeleteMany(FilterDefinition<UserFriends>.Empty);
        _userDataCollection.DeleteMany(FilterDefinition<UserData>.Empty);

        _svc = new UserFriendsService(_friendsCollection, _userDataCollection);
        _ctrl = new UserFriendsController(_svc, new NoopLogger<UserController>());
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
    public async Task GetUserFriendsList_Ok_With_Data()
    {
        var userFriends = UserFriendsFactory.CreateUserFriends("user1", "friend1", "friend2");
        await _friendsCollection.InsertOneAsync(userFriends);

        await _userDataCollection.InsertManyAsync(new[]
        {
            UserDataFactory.Create("friend1"),
            UserDataFactory.Create("friend2")
        });

        var result = await _ctrl.GetUserFriendsList("user1", CancellationToken.None);

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
        var userFriends = UserFriendsFactory.CreateUserFriends("user1");
        await _friendsCollection.InsertOneAsync(userFriends);

        var result = await _ctrl.GetUserFriendsList("user1", CancellationToken.None);

        var ok = Assert.IsType<OkObjectResult>(result.Result);
        var payload = Assert.IsType<UserList>(ok.Value);
        Assert.Equal(200, ok.StatusCode);
        Assert.Empty(payload.Users);
    }

    [Fact]
    public async Task AddFriend_Ok()
    {
        var userFriends = UserFriendsFactory.CreateUserFriends("user1");
        await _friendsCollection.InsertOneAsync(userFriends);

        await _userDataCollection.InsertOneAsync(UserDataFactory.Create("friend1"));

        var result = await _ctrl.AddFriend("user1", "friend1", CancellationToken.None);

        var ok = Assert.IsType<OkObjectResult>(result);
        Assert.Equal(200, ok.StatusCode);
        Assert.Equal("{ message = Friend added successfully }", ok.Value!.ToString());

        var updatedUser = await _friendsCollection.Find(u => u.FirebaseUid == "user1").FirstOrDefaultAsync();
        Assert.Contains("friend1", updatedUser.FriendIds);
    }

    [Fact]
    public async Task AddFriend_Ok_Creates_New_UserFriend()
    {
        var result = await _ctrl.AddFriend("user1", "friend1", CancellationToken.None);

        var ok = Assert.IsType<OkObjectResult>(result);
        Assert.Equal(200, ok.StatusCode);
        Assert.Equal("{ message = Friend added successfully }", ok.Value!.ToString());

        var user1 = await _friendsCollection.Find(u => u.FirebaseUid == "user1").FirstOrDefaultAsync();
        var user2 = await _friendsCollection.Find(u => u.FirebaseUid == "friend1").FirstOrDefaultAsync();

        Assert.NotNull(user1);
        Assert.NotNull(user2);
        Assert.Contains("friend1", user1.FriendIds);
        Assert.Contains("user1", user2.FriendIds);
    }

    [Fact]
    public async Task DeleteFriend_Ok()
    {
        var user1 = UserFriendsFactory.CreateUserFriends("user1", "friend1");
        var friend1 = UserFriendsFactory.CreateUserFriends("friend1", "user1");
        await _friendsCollection.InsertManyAsync(new[] { user1, friend1 });

        var result = await _ctrl.DeleteFriend("user1", "friend1", CancellationToken.None);

        var ok = Assert.IsType<OkObjectResult>(result);
        Assert.Equal(200, ok.StatusCode);
        Assert.Equal("{ message = Friend deleted successfully }", ok.Value!.ToString());

        var updatedUser = await _friendsCollection.Find(u => u.FirebaseUid == "user1").FirstOrDefaultAsync();
        var updatedFriend = await _friendsCollection.Find(u => u.FirebaseUid == "friend1").FirstOrDefaultAsync();

        Assert.DoesNotContain("friend1", updatedUser.FriendIds);
        Assert.DoesNotContain("user1", updatedFriend.FriendIds);
    }

    [Fact]
    public async Task DeleteFriend_NotFound()
    {
        var userFriends = UserFriendsFactory.CreateUserFriends("user1");
        await _friendsCollection.InsertOneAsync(userFriends);

        var result = await _ctrl.DeleteFriend("user1", "friend1", CancellationToken.None);

        var notFound = Assert.IsType<NotFoundObjectResult>(result);
        Assert.Equal(404, notFound.StatusCode);
        Assert.Equal("{ message = UserFriend not found or deleting failed }", notFound.Value!.ToString());
    }

    public void Dispose() => _runner.Dispose();
}