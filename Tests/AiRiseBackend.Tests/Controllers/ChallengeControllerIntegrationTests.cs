using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using AiRise.Controllers;
using AiRise.Models;
using AiRise.Services;
using Microsoft.AspNetCore.Mvc;
using Microsoft.VisualStudio.TestPlatform.Utilities;
using Mongo2Go;
using MongoDB.Bson;
using MongoDB.Driver;
using Moq;
using Xunit;

public class ChallengeController_IntegrationTests : IClassFixture<MongoIntegrationTest<Challenge>>
{
    private readonly MongoIntegrationTest<Challenge> _fixture;
    private readonly ChallengeController _controller;
    private readonly IMongoCollection<Challenge> _collection;

    public ChallengeController_IntegrationTests(MongoIntegrationTest<Challenge> fixture)
    {
        _fixture = fixture;
        _collection = fixture.Collection;
        _fixture.ClearCollectionAsync().GetAwaiter().GetResult();

        var service = new ChallengeService(_collection);
        _controller = new ChallengeController(service);
    }

    [Fact]
    public async Task GetAllChallenges_Returns_Ok_With_Data()
    {
        await _fixture.ClearCollectionAsync();
        // Arrange 
        var challenges = new List<Challenge>
        {
            new Challenge { Name = "1" },
            new Challenge { Name = "2" }
        };
        await _collection.InsertManyAsync(challenges);

        // Act
        var result = await _controller.GetAllChallenges();

        // Assert
        var ok = Assert.IsType<OkObjectResult>(result.Result);
        var payload = Assert.IsType<List<Challenge>>(ok.Value);
        Assert.Equal(2, payload.Count);
        Assert.Contains(payload, c => c.Name == challenges[0].Name);
        Assert.Contains(payload, c => c.Name == challenges[1].Name);
    }

    [Fact]
    public async Task GetAllChallenges_Returns_EmptyList_When_No_Data()
    {
        await _fixture.ClearCollectionAsync(); 

        var result = await _controller.GetAllChallenges();

        var ok = Assert.IsType<OkObjectResult>(result.Result);
        var challenges = Assert.IsType<List<Challenge>>(ok.Value);

        Assert.Empty(challenges);
    }


    [Fact]
    public async Task UpsertChallenge_Updates_Existing_Record()
    {
        await _fixture.ClearCollectionAsync();
        // Arrange
        var existing = new Challenge
        {
            Id = ObjectId.GenerateNewId().ToString(),
            Name = "Old",
            Description = "desc",
            Url = "url"
        };

        await _collection.InsertOneAsync(existing);

        // Act
        existing.Name = "Updated Name";
        var result = await _controller.UpsertChallenge(existing);

        // Assert
        var ok = Assert.IsType<OkObjectResult>(result);
        var updated = await _collection.Find(c => c.Id == existing.Id).FirstOrDefaultAsync();

        Assert.Equal("Updated Name", updated.Name);
    }

    [Fact]
    public async Task UpsertChallenge_Inserts_New_Record()
    {
        await _fixture.ClearCollectionAsync();
        // Arrange
        var challenge = new Challenge
        {
            Name = "New"
        };

        // Act
        var result = await _controller.UpsertChallenge(challenge);

        //Assert
        var ok = Assert.IsType<OkObjectResult>(result);
        var inserted = await _collection.Find(c => c.Name == challenge.Name).FirstOrDefaultAsync();
        Assert.NotNull(inserted);
    }

    [Fact]
    public async Task UpsertChallenge_Returns_BadRequest_On_Empty_Name()
    {
        await _fixture.ClearCollectionAsync();
        // Arrange
        var challenge = new Challenge
        {
            Name = "" // Names cannot be empty
        };

        // Act
        var result = await _controller.UpsertChallenge(challenge);

        // Assert 
        var badRequest = Assert.IsType<BadRequestObjectResult>(result);
    }

    [Fact]
    public async Task DeleteChallenge_Deletes_Exisiting_Record()
    {
        await _fixture.ClearCollectionAsync();
        // Arrange
        var existing = new Challenge
        {
            Id = ObjectId.GenerateNewId().ToString(),
            Name = "To be deleted"
        };
        await _collection.InsertOneAsync(existing);

        // Act
        var result = await _controller.DeleteChallenge(existing.Id);

        // Assert
        var ok = Assert.IsType<OkObjectResult>(result);
        var deleted = await _collection.Find(c => c.Id == existing.Id).FirstOrDefaultAsync();
        Assert.Null(deleted);
    }

    [Fact]
    public async Task DeleteChallenge_Fails_When_No_Match()
    {
        await _fixture.ClearCollectionAsync();
        // Arrange
        var challenge = new Challenge
        {
            Id = ObjectId.GenerateNewId().ToString(),
            Name = "To be deleted"
        };

        // Act
        var result = await _controller.DeleteChallenge(challenge.Id);

        // Assert
        var notFound = Assert.IsType<NotFoundObjectResult>(result);
    }
}
