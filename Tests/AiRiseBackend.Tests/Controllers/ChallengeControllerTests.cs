using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using AiRise.Controllers;
using AiRise.Models;
using AiRise.Services;
using Microsoft.AspNetCore.Mvc;
using MongoDB.Driver;
using Moq;
using Xunit;

public class ChallengeController_Tests
{
    [Fact]
    public async Task GetAllChallenges_Returns_Ok_With_Data()
    {
        var coll = new Mock<IMongoCollection<Challenge>>();
        var data = new[] { new Challenge { Name = "Alpha" }, new Challenge { Name = "Beta" } };
        coll.SetupFindAsync(data);                 // uses the helper above

        var svc = new ChallengeService(coll.Object); // ✅ real service over mocked collection
        var ctrl = new ChallengeController(svc);

        var result = await ctrl.GetAllChallenges();

        var ok = Assert.IsType<OkObjectResult>(result.Result);
        var payload = Assert.IsType<List<Challenge>>(ok.Value);
        Assert.Equal(new[] { "Alpha", "Beta" }, payload.Select(p => p.Name));
    }

    [Fact]
    public async Task InsertChallenge_Returns_Ok_And_Calls_Insert()
    {
        var coll = new Mock<IMongoCollection<Challenge>>();
        Challenge? written = null;

        coll.Setup(c => c.InsertOneAsync(
                It.IsAny<Challenge>(),
                It.IsAny<InsertOneOptions>(),
                It.IsAny<CancellationToken>()))
            .Callback<Challenge, InsertOneOptions, CancellationToken>((doc, _, __) => written = doc)
            .Returns(Task.CompletedTask);

        var svc = new ChallengeService(coll.Object);
        var ctrl = new ChallengeController(svc);

        var newItem = new Challenge { Name = "New", Description = "D", Url = "U" };
        var res = await ctrl.InsertChallenge(newItem);

        Assert.IsType<OkResult>(res);
        Assert.NotNull(written);
        Assert.Equal("New", written!.Name);
    }
}
