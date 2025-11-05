using AiRise.Models.User;
using AiRise.Services;
using MongoDB.Bson;
using MongoDB.Driver;
using Moq;

public class UserFriendsService_Test()
{
    private static Mock<IMongoCollection<UserFriends>> createMockFriendsCollection()
    {
        var collMock = new Mock<IMongoCollection<UserFriends>>();
        var indexMgrMock = new Mock<IMongoIndexManager<UserFriends>>();

        collMock.SetupGet(c => c.Indexes).Returns(indexMgrMock.Object);
        indexMgrMock.Setup(i => i.CreateOne(It.IsAny<CreateIndexModel<UserFriends>>(), null, default))
                    .Returns("idx");
        return collMock;
    }

    private static Mock<IMongoCollection<UserData>> createMockUserDataCollection()
    {
        var collMock = new Mock<IMongoCollection<UserData>>();
        var indexMgrMock = new Mock<IMongoIndexManager<UserData>>();

        collMock.SetupGet(c => c.Indexes).Returns(indexMgrMock.Object);
        indexMgrMock.Setup(i => i.CreateOne(It.IsAny<CreateIndexModel<UserData>>(), null, default))
                    .Returns("idx");
        return collMock;
    }
    
    [Fact]
    public async Task GetUserFriends_Returns_Existing_Document(){
        var friendsCollMock = createMockFriendsCollection();
        var userDataCollMock = createMockUserDataCollection();

        var existing = UserFriendsFactory.CreateUserFriends("user1");

        friendsCollMock.SetupFindAsyncSingle(existing);

        var service = new UserFriendsService(friendsCollMock.Object, userDataCollMock.Object);
        
        var result = await service.GetUserFriends("user1");
        Assert.Equal(existing, result);
        friendsCollMock.Verify(c => c.FindAsync(
            It.IsAny<FilterDefinition<UserFriends>>(), 
            It.IsAny<FindOptions<UserFriends, UserFriends>>(), 
            It.IsAny<CancellationToken>()), Times.Once());    
    }

    [Fact]
    public async Task GetUserFriends_Creates_New_When_NotFound()
    {
        var friendsCollMock = createMockFriendsCollection();
        var userDataCollMock = createMockUserDataCollection();

        friendsCollMock.SetupFindAsyncSingle(null);

        var service = new UserFriendsService(friendsCollMock.Object, userDataCollMock.Object);

        var result = await service.GetUserFriends("user1");
        Assert.NotNull(result);
        Assert.Equal("user1", result.FirebaseUid);
        Assert.Empty(result.FriendIds);
        friendsCollMock.Verify(c => c.InsertOneAsync(
            It.Is<UserFriends>(u => u.FirebaseUid == "user1" && u.FriendIds.Count == 0),
            null,
            default), Times.Once());
    }

    [Fact]
    public async Task GetUserFriendsList_Returns_Empty_List_When_No_Friends()
    {
        var friendsCollMock = createMockFriendsCollection();
        var userDataCollMock = createMockUserDataCollection();

        var existing = UserFriendsFactory.CreateUserFriends("user1");
        friendsCollMock.SetupFindAsyncSingle(existing);

        var service = new UserFriendsService(friendsCollMock.Object, userDataCollMock.Object);

        var result = await service.GetUserFriendsList("user1");
        Assert.NotNull(result);
        Assert.Empty(result.Users);
        friendsCollMock.Verify(c => c.FindAsync(
            It.IsAny<FilterDefinition<UserFriends>>(), 
            It.IsAny<FindOptions<UserFriends, UserFriends>>(), 
            It.IsAny<CancellationToken>()), Times.Once());    
    }

    [Fact]
    public async Task GetUserFriendsList_Returns_Friends_List()
    {
        var friendsCollMock = createMockFriendsCollection();
        var userDataCollMock = createMockUserDataCollection();

        var existing = UserFriendsFactory.CreateUserFriends("user1", "friend1", "friend2");
        friendsCollMock.SetupFindAsyncSingle(existing);

        var expected = UserFriendsFactory.CreateUserList(new List<string> { "friend1", "friend2" });
        var aggregateCursor = MongoMoqHelpers.MakeCursor(expected.Users);
        userDataCollMock.Setup(c => c.Aggregate<UserProfile>(
            It.IsAny<PipelineDefinition<UserData, UserProfile>>(),
            It.IsAny<AggregateOptions>(),
            It.IsAny<CancellationToken>()))
            .Returns(aggregateCursor.Object);

        var service = new UserFriendsService(friendsCollMock.Object, userDataCollMock.Object);

        var result = await service.GetUserFriendsList("user1");
        Assert.NotNull(result);
        Assert.Equal(expected.Users, result.Users);
        friendsCollMock.Verify(c => c.FindAsync(
            It.IsAny<FilterDefinition<UserFriends>>(), 
            It.IsAny<FindOptions<UserFriends, UserFriends>>(), 
            It.IsAny<CancellationToken>()), Times.Once());    
        userDataCollMock.Verify(c => c.Aggregate<UserProfile>(
            It.IsAny<PipelineDefinition<UserData, UserProfile>>(),
            It.IsAny<AggregateOptions>(),
            It.IsAny<CancellationToken>()), Times.Once());    
    }

    [Fact]
    public async Task AddFriend_Adds_Friend()
    {
        var friendsCollMock = createMockFriendsCollection();
        var userDataCollMock = createMockUserDataCollection();

        var existing = UserFriendsFactory.CreateUserFriends("user1");
        friendsCollMock.Setup(c => c.UpdateOneAsync(
            It.IsAny<FilterDefinition<UserFriends>>(),
            It.IsAny<UpdateDefinition<UserFriends>>(),
            It.IsAny<UpdateOptions>(),
            It.IsAny<CancellationToken>()))
            .ReturnsAsync(new UpdateResult.Acknowledged(1, 1, null));

        var service = new UserFriendsService(friendsCollMock.Object, userDataCollMock.Object);

        var result = await service.AddFriend("user1", "friend1");
        Assert.True(result);
        // Verify both updates were called
        friendsCollMock.Verify(c => c.UpdateOneAsync(
            It.IsAny<FilterDefinition<UserFriends>>(),
            It.IsAny<UpdateDefinition<UserFriends>>(),
            It.IsAny<UpdateOptions>(),
            It.IsAny<CancellationToken>()), Times.Exactly(2));
    }

    [Fact]
    public async Task AddFriend_Adds_Friend_And_Creates_New_When_NotFound()
    {
        var friendsCollMock = createMockFriendsCollection();
        var userDataCollMock = createMockUserDataCollection();

        friendsCollMock.Setup(c => c.UpdateOneAsync(
            It.IsAny<FilterDefinition<UserFriends>>(),
            It.IsAny<UpdateDefinition<UserFriends>>(),
            It.IsAny<UpdateOptions>(),
            It.IsAny<CancellationToken>()))
            .ReturnsAsync(new UpdateResult.Acknowledged(0, 0, BsonObjectId.Create(ObjectId.GenerateNewId())));

        var service = new UserFriendsService(friendsCollMock.Object, userDataCollMock.Object);

        var result = await service.AddFriend("user1", "friend1");
        Assert.True(result);
        friendsCollMock.Verify(c => c.UpdateOneAsync(
            It.IsAny<FilterDefinition<UserFriends>>(),
            It.IsAny<UpdateDefinition<UserFriends>>(),
            It.IsAny<UpdateOptions>(),
            It.IsAny<CancellationToken>()), Times.Exactly(2));
    }

    [Fact]
    public async Task DeleteFriend_Deletes_Friend()
    {
        var friendsCollMock = createMockFriendsCollection();
        var userDataCollMock = createMockUserDataCollection();

        var existing = UserFriendsFactory.CreateUserFriends("user1", "friend1");
        friendsCollMock.Setup(c => c.UpdateOneAsync(
            It.IsAny<FilterDefinition<UserFriends>>(),
            It.IsAny<UpdateDefinition<UserFriends>>(),
            It.IsAny<UpdateOptions>(),
            It.IsAny<CancellationToken>()))
            .ReturnsAsync(new UpdateResult.Acknowledged(1, 1, null));

        var service = new UserFriendsService(friendsCollMock.Object, userDataCollMock.Object);

        var result = await service.DeleteFriend("user1", "friend1");
        Assert.True(result);
        friendsCollMock.Verify(c => c.UpdateOneAsync(
            It.IsAny<FilterDefinition<UserFriends>>(),
            It.IsAny<UpdateDefinition<UserFriends>>(),
            It.IsAny<UpdateOptions>(),
            It.IsAny<CancellationToken>()), Times.Exactly(2));
    }

    [Fact]
    public async Task DeleteFriend_Returns_False_When_Neither_User_Found()
    {
        var friendsCollMock = createMockFriendsCollection();
        var userDataCollMock = createMockUserDataCollection();

        friendsCollMock.Setup(c => c.UpdateOneAsync(
            It.IsAny<FilterDefinition<UserFriends>>(),
            It.IsAny<UpdateDefinition<UserFriends>>(),
            It.IsAny<UpdateOptions>(),
            It.IsAny<CancellationToken>()))
            .ReturnsAsync(new UpdateResult.Acknowledged(0, 0, null));

        var service = new UserFriendsService(friendsCollMock.Object, userDataCollMock.Object);

        var result = await service.DeleteFriend("user1", "friend1");
        Assert.False(result);
        friendsCollMock.Verify(c => c.UpdateOneAsync(
            It.IsAny<FilterDefinition<UserFriends>>(),
            It.IsAny<UpdateDefinition<UserFriends>>(),
            It.IsAny<UpdateOptions>(),
            It.IsAny<CancellationToken>()), Times.Exactly(2));
    }
    
}
public class UserFriendsFactory
{
    public static UserFriends CreateUserFriends(string firebaseUid)
    {
        return new UserFriends { FirebaseUid = firebaseUid, FriendIds = new List<string>() };
    }
    public static UserFriends CreateUserFriends(string firebaseUid, string friendFirebaseUid)
    {
        return new UserFriends { FirebaseUid = firebaseUid, FriendIds = new List<string> { friendFirebaseUid } };
    }
    public static UserFriends CreateUserFriends(string firebaseUid, params string[] friendFirebaseUids)
    {
        return new UserFriends { FirebaseUid = firebaseUid, FriendIds = friendFirebaseUids.ToList() };
    }
    public static UserFriends CreateUserFriends(string firebaseUid, List<string> friendFirebaseUids)
    {
        return new UserFriends { FirebaseUid = firebaseUid, FriendIds = friendFirebaseUids };
    }
    public static UserList CreateUserList(List<string> friendFirebaseUids){
        return new UserList { Users = friendFirebaseUids.Select(uid => new UserProfile { firebaseUid = uid }).ToList() };
    }
}