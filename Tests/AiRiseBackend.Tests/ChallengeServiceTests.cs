using AiRise.Models;
using AiRise.Services;
using MongoDB.Driver;
using Moq;

public class ChallengeService_Tests
{
    private static Challenge C(string name) => new Challenge { Id = "WILL_BE_STRIPPED", Name = name, Description = "d", Url = "u" };

    [Fact]
    public async Task SeedChallengesIfEmptyAsync_Inserts_When_Empty()
    {
        var coll = new Mock<IMongoCollection<Challenge>>();

        // CountDocumentsAsync => 0 (empty)
        coll.Setup(c => c.CountDocumentsAsync(
                It.IsAny<FilterDefinition<Challenge>>(),
                It.IsAny<CountOptions>(),
                It.IsAny<CancellationToken>()))
            .ReturnsAsync(0);

        // Capture InsertManyAsync payload
        List<Challenge>? inserted = null;
        coll.Setup(c => c.InsertManyAsync(
                It.IsAny<IEnumerable<Challenge>>(),
                It.IsAny<InsertManyOptions>(),
                It.IsAny<CancellationToken>()))
            .Callback<IEnumerable<Challenge>, InsertManyOptions, CancellationToken>((docs, _, __) => inserted = docs.ToList())
            .Returns(Task.CompletedTask);

        var svc = new ChallengeService(coll.Object);
        var seed = new List<Challenge> { C("A"), C("B") };

        await svc.SeedChallengesIfEmptyAsync(seed);

        Assert.NotNull(inserted);
        Assert.Equal(2, inserted!.Count);
        Assert.All(inserted, ch => Assert.True(string.IsNullOrEmpty(ch.Id))); // Ids stripped
        Assert.Equal(new[] { "A", "B" }, inserted.Select(x => x.Name));
    }

    [Fact]
    public async Task SeedChallengesIfEmptyAsync_Skips_When_Not_Empty()
    {
        var coll = new Mock<IMongoCollection<Challenge>>();

        coll.Setup(c => c.CountDocumentsAsync(
                It.IsAny<FilterDefinition<Challenge>>(),
                It.IsAny<CountOptions>(),
                It.IsAny<CancellationToken>()))
            .ReturnsAsync(5);

        var svc = new ChallengeService(coll.Object);

        await svc.SeedChallengesIfEmptyAsync(new List<Challenge> { C("X") });

        coll.Verify(c => c.InsertManyAsync(
            It.IsAny<IEnumerable<Challenge>>(),
            It.IsAny<InsertManyOptions>(),
            It.IsAny<CancellationToken>()), Times.Never);
    }

    [Fact]
    public async Task GetAllChallengesAsync_Returns_All()
    {
        var coll = new Mock<IMongoCollection<Challenge>>();

        var expected = new List<Challenge> {
            new Challenge { Name = "Alpha" },
            new Challenge { Name = "Beta"  }
        };

        coll.SetupFindAsync(expected); // from your MongoMoqHelpers

        var svc = new ChallengeService(coll.Object);

        var items = await svc.GetAllChallengesAsync();

        Assert.Equal(new[] { "Alpha", "Beta" }, items.Select(i => i.Name));
    }
}
