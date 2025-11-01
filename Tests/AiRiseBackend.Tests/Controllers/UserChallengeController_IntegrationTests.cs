using System.Threading;
using System.Threading.Tasks;
using AiRise.Controllers;
using AiRise.Models.User;
using AiRise.Services;
using Microsoft.AspNetCore.Mvc;
using MongoDB.Driver;
using Xunit;

public class UserChallengesController_IntegrationTests : IClassFixture<MongoIntegrationTest<UserChallenges>>
{
    private readonly MongoIntegrationTest<UserChallenges> _fixture;
    private readonly IMongoCollection<UserChallenges> _collection;
    private readonly UserChallengesService _svc;
    private readonly UserChallengesController _ctrl;

    public UserChallengesController_IntegrationTests(MongoIntegrationTest<UserChallenges> fixture)
    {
        _fixture = fixture;
        _collection = _fixture.Collection;
        _fixture.ClearCollectionAsync().GetAwaiter().GetResult();

        _svc = new UserChallengesService(_collection); // test-only overload
        _ctrl = new UserChallengesController(_svc);
    }

    [Fact]
    public async Task Get_BadRequest_When_EmptyUid()
    {
        var res = await _ctrl.Get("", CancellationToken.None);
        Assert.IsType<BadRequestObjectResult>(res.Result);
    }

    [Fact]
    public async Task Get_Ok_With_Doc_Shell_When_Missing()
    {
        var res = await _ctrl.Get("abc", CancellationToken.None);
        var ok = Assert.IsType<OkObjectResult>(res.Result);
        var doc = Assert.IsType<UserChallenges>(ok.Value);
        Assert.Equal("abc", doc.FirebaseUid);
    }

    [Fact]
    public async Task Create_BadRequest_When_EmptyUid()
    {
        var res = await _ctrl.Create(new UserChallengesController.CreateReq(""), CancellationToken.None);
        Assert.IsType<BadRequestObjectResult>(res.Result);
    }

    [Fact]
    public async Task Create_Ok_Returns_Id()
    {
        var res = await _ctrl.Create(new UserChallengesController.CreateReq("u1"), CancellationToken.None);
        var ok = Assert.IsType<OkObjectResult>(res.Result);
        var id = Assert.IsType<string>(ok.Value);
        Assert.False(string.IsNullOrWhiteSpace(id));
    }

    [Fact]
    public async Task SetActive_BadRequest_When_Missing_Fields()
    {
        var r1 = await _ctrl.SetActive(new UserChallengesController.SetActiveReq("", "c1"), CancellationToken.None);
        var r2 = await _ctrl.SetActive(new UserChallengesController.SetActiveReq("u1", ""), CancellationToken.None);
        Assert.IsType<BadRequestObjectResult>(r1);
        Assert.IsType<BadRequestObjectResult>(r2);
    }

    [Fact]
    public async Task SetActive_NoContent_On_Success()
    {
        await _fixture.ClearCollectionAsync();
        await _ctrl.Create(new UserChallengesController.CreateReq("u2"), CancellationToken.None);

        var res = await _ctrl.SetActive(new UserChallengesController.SetActiveReq("u2", "c1"), CancellationToken.None);
        Assert.IsType<NoContentResult>(res);

        var get = await _ctrl.Get("u2", CancellationToken.None);
        var ok = Assert.IsType<OkObjectResult>(get.Result);
        var doc = Assert.IsType<UserChallenges>(ok.Value);
        Assert.Equal("c1", doc.ActiveChallengeId);
    }

    [Fact]
    public async Task CompletedToday_BadRequest_When_EmptyUid()
    {
        var res = await _ctrl.CompletedToday("", CancellationToken.None);
        Assert.IsType<BadRequestObjectResult>(res.Result);
    }

    [Fact]
    public async Task CompletedToday_Ok_With_Bool()
    {
        await _fixture.ClearCollectionAsync();
        await _ctrl.Create(new UserChallengesController.CreateReq("u3"), CancellationToken.None);

        var r0 = await _ctrl.CompletedToday("u3", CancellationToken.None);
        var ok0 = Assert.IsType<OkObjectResult>(r0.Result);
        Assert.False((bool)ok0.Value!);

        await _ctrl.CompleteToday(new UserChallengesController.UidOnly("u3"), CancellationToken.None);

        var r1 = await _ctrl.CompletedToday("u3", CancellationToken.None);
        var ok1 = Assert.IsType<OkObjectResult>(r1.Result);
        Assert.True((bool)ok1.Value!);
    }

    [Fact]
    public async Task CompleteToday_BadRequest_When_EmptyUid()
    {
        var res = await _ctrl.CompleteToday(new UserChallengesController.UidOnly(""), CancellationToken.None);
        Assert.IsType<BadRequestObjectResult>(res.Result);
    }

    [Fact]
    public async Task CompleteToday_Ok_Returns_Doc_And_Clears_Active()
    {
        await _fixture.ClearCollectionAsync();
        await _ctrl.SetActive(new UserChallengesController.SetActiveReq("u4", "c9"), CancellationToken.None);

        var res = await _ctrl.CompleteToday(new UserChallengesController.UidOnly("u4"), CancellationToken.None);
        var ok = Assert.IsType<OkObjectResult>(res.Result);
        var doc = Assert.IsType<UserChallenges>(ok.Value);
        Assert.Equal("u4", doc.FirebaseUid);
        Assert.True(doc.LastCompletionEpochDay.HasValue);

        var after = await _ctrl.Get("u4", CancellationToken.None);
        var ok2 = Assert.IsType<OkObjectResult>(after.Result);
        var persisted = Assert.IsType<UserChallenges>(ok2.Value);
        Assert.Null(persisted.ActiveChallengeId);
    }

    [Fact]
    public async Task ClearCompletion_BadRequest_When_EmptyUid()
    {
        var res = await _ctrl.ClearCompletion(new UserChallengesController.UidOnly(""), CancellationToken.None);
        Assert.IsType<BadRequestObjectResult>(res);
    }

    [Fact]
    public async Task ClearCompletion_NoContent_On_Success()
    {
        await _fixture.ClearCollectionAsync();
        await _ctrl.CompleteToday(new UserChallengesController.UidOnly("u5"), CancellationToken.None);

        var res = await _ctrl.ClearCompletion(new UserChallengesController.UidOnly("u5"), CancellationToken.None);
        Assert.IsType<NoContentResult>(res);

        var get = await _ctrl.Get("u5", CancellationToken.None);
        var ok = Assert.IsType<OkObjectResult>(get.Result);
        var doc = Assert.IsType<UserChallenges>(ok.Value);
        Assert.Null(doc.LastCompletionEpochDay);
    }
}
