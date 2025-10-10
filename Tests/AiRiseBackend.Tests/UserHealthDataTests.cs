using System.Linq.Expressions;
using AiRise.Models.DTOs;
using AiRise.Models.User;
using AiRise.Services;
using MongoDB.Bson.Serialization;
using MongoDB.Driver;
using Moq;
using ZstdSharp.Unsafe;

public class UserHealthData_Tests
{
    private static Mock<IAsyncCursor<UserHealthData>> CreateCursor(UserHealthData doc)
    {
        var cursor = new Mock<IAsyncCursor<UserHealthData>>();
        cursor.SetupGet(c => c.Current).Returns(new[] { doc });
        cursor.SetupSequence(c => c.MoveNext(It.IsAny<CancellationToken>()))
              .Returns(doc != null)
              .Returns(false);
        cursor.SetupSequence(c => c.MoveNextAsync(It.IsAny<CancellationToken>()))
              .ReturnsAsync(doc != null)
              .ReturnsAsync(false);
        return cursor;
    }
    private static Mock<IMongoCollection<UserHealthData>> CreateMockCollection(UserHealthData? doc)
    {
        var collMock = new Mock<IMongoCollection<UserHealthData>>();
        var indexMgrMock = new Mock<IMongoIndexManager<UserHealthData>>();

        // Index creation
        collMock.SetupGet(c => c.Indexes).Returns(indexMgrMock.Object);
        indexMgrMock
            .Setup(i => i.CreateOne(It.IsAny<CreateIndexModel<UserHealthData>>(), null, default))
            .Returns("idx");

        //Find().FirstOrDefaultAsync() path
        var cursor = CreateCursor(doc);
        collMock
            .Setup(c => c.FindAsync(
                It.IsAny<FilterDefinition<UserHealthData>>(),
                It.IsAny<FindOptions<UserHealthData, UserHealthData>>(),
                It.IsAny<CancellationToken>()
            ))
            .Returns(Task.FromResult(cursor.Object));
        // UpdateOneAsync -> success if doc is not null, failure if null
        var updateResult = new Mock<UpdateResult>();
        updateResult.SetupGet(u => u.MatchedCount).Returns(doc != null ? 1 : 0);
        updateResult.SetupGet(u => u.ModifiedCount).Returns(doc != null ? 1 : 0);
        updateResult.SetupGet(u => u.IsAcknowledged).Returns(true);

        collMock
            .Setup(c => c.UpdateOneAsync(
                It.IsAny<FilterDefinition<UserHealthData>>(),
                It.IsAny<UpdateDefinition<UserHealthData>>(),
                It.IsAny<UpdateOptions>(),
                It.IsAny<CancellationToken>()
            ))
            .Callback<FilterDefinition<UserHealthData>, UpdateDefinition<UserHealthData>, UpdateOptions, CancellationToken>((filter, update, options, ct) =>
            {
                // Best-effort: render the UpdateDefinition to Bson and apply the $set.Program value to the in-memory seedDoc
                try
                {
                    var renderArgs = new MongoDB.Driver.RenderArgs<UserHealthData>(BsonSerializer.SerializerRegistry.GetSerializer<UserHealthData>(), BsonSerializer.SerializerRegistry);
                    var rendered = update.Render(renderArgs).AsBsonDocument;
                    if (rendered.TryGetValue("$set", out var setDocValue) && setDocValue.IsBsonDocument)
                    {
                        var bd = setDocValue.AsBsonDocument;
                        if (bd.TryGetValue("caloriesBurned", out var v1))
                            doc.CaloriesBurned = v1.ToInt32();

                        if (bd.TryGetValue("caloriesEaten", out var v2))
                            doc.CaloriesEaten = v2.ToInt32();

                        if (bd.TryGetValue("caloriesTarget", out var v3))
                            doc.CaloriesTarget = v3.ToInt32();

                        if (bd.TryGetValue("steps", out var v4))
                            doc.Steps = v4.ToInt32();

                        if (bd.TryGetValue("sleep", out var v5))
                            doc.Sleep = v5.ToDouble();

                        if (bd.TryGetValue("hydration", out var v6))
                            doc.Hydration = v6.ToDouble();

                        if (bd.TryGetValue("hydrationTarget", out var v7))
                            doc.HydrationTarget = v7.ToDouble();

                        if (bd.TryGetValue("localDate", out var v8))
                        {
                            if (v8.IsString && DateOnly.TryParse(v8.AsString, out var d))
                                doc.LocalDate = d;
                            else if (v8.IsValidDateTime)
                                doc.LocalDate = DateOnly.FromDateTime(v8.ToUniversalTime());
                        }
                    }
                }
                catch
                {
                    // ignore - this is a test helper best-effort application
                }
            })
            .ReturnsAsync(updateResult.Object);

        return collMock;
    }

    [Fact]
    public async Task GetUserHealthDataAsync_ReturnsDocument_WhenFound()
    {
        var expectedDoc = new UserHealthData
        {
            Id = "64a7f0c8e4b0f5d6c8e4b0f5",
            FirebaseUid = "testuid",
            CaloriesBurned = 500,
            CaloriesEaten = 2000,
            CaloriesTarget = 2200,
            Steps = 8000,
            Sleep = 7,
            Hydration = 80,
            HydrationTarget = 100,
            LocalDate = DateOnly.FromDateTime(DateTime.UtcNow)
        };
        var collMock = CreateMockCollection(expectedDoc);

        var svc = new UserHealthDataService(collMock.Object);

        var result = await svc.GetUserHealthDataAsync("testuid");

        Assert.NotNull(result);
        Assert.Equal(expectedDoc, result);
    }

    [Fact]
    public async Task GetUserHealthDataAsync_ReturnsNull_WhenNotFound()
    {
        var collMock = CreateMockCollection(null);
        var svc = new UserHealthDataService(collMock.Object);

        var result = await svc.GetUserHealthDataAsync("NOTFOUND");

        Assert.Null(result);
    }

    [Fact]
    public async Task UpdateUserHealthDataAsync_ReturnsTrue_WhenUpdateSuccessful()
    {
        var doc = new UserHealthData
        {
            Id = "id123",
            FirebaseUid = "testuid",
            CaloriesBurned = 127,
            CaloriesEaten = 1500,
            CaloriesTarget = 1750,
            Steps = 8900,
            Sleep = 5.5,
            Hydration = 14.5,
            HydrationTarget = 104
        };
        var collMock = CreateMockCollection(doc);
        var svc = new UserHealthDataService(collMock.Object);

        var updatedData = new HealthData
        {
            CaloriesBurned = 300,
            CaloriesEaten = 1800,
            CaloriesTarget = 2000,
            Steps = 10000,
            Sleep = 6.5,
            Hydration = 20.0,
            HydrationTarget = 110,
            LocalDate = DateOnly.FromDateTime(DateTime.UtcNow)
        };
        var result = await svc.UpdateUserHealthDataAsync("testuid", updatedData);

        Assert.True(result);
        Assert.Equal(300, doc.CaloriesBurned);
        Assert.Equal(1800, doc.CaloriesEaten);
        Assert.Equal(2000, doc.CaloriesTarget);
        Assert.Equal(10000, doc.Steps);
        Assert.Equal(6.5, doc.Sleep);
        Assert.Equal(20.0, doc.Hydration);
        Assert.Equal(110, doc.HydrationTarget);
        Assert.Equal(updatedData.LocalDate, doc.LocalDate);
    }

    [Fact]
    public async Task UpdateUserHealthDataAsync_ReturnsFalse_WhenNoModification()
    {
        var doc = new UserHealthData { FirebaseUid = "u1", LocalDate = DateOnly.FromDateTime(DateTime.UtcNow), Steps = 100 };
        var collMock = CreateMockCollection(doc);

        // Override UpdateOneAsync to return ModifiedCount = 0 for this test
        collMock.Setup(c => c.UpdateOneAsync(
                It.IsAny<FilterDefinition<UserHealthData>>(),
                It.IsAny<UpdateDefinition<UserHealthData>>(),
                It.IsAny<UpdateOptions>(),
                It.IsAny<CancellationToken>()))
            .ReturnsAsync(new UpdateResult.Acknowledged(1, 0, null));

        var svc = new UserHealthDataService(collMock.Object);
        var result  = await svc.UpdateUserHealthDataAsync("u1", new HealthData {
            LocalDate = DateOnly.FromDateTime(DateTime.UtcNow),
            Steps = 100
        });

        Assert.False(result);
    }

    [Fact]
    public async Task UpdateUserHealthDataAsync_ThrowsArgumentNull_WhenDocumentNotFound()
    {
        var collMock = CreateMockCollection(null);
        var svc = new UserHealthDataService(collMock.Object);
        var updatedData = new HealthData
        {
            LocalDate = new DateOnly()
        };

        await Assert.ThrowsAsync<ArgumentException>(() => svc.UpdateUserHealthDataAsync("NOTFOUND", updatedData));
    }

    [Theory]
    [InlineData(-1, null, null, null, null, null, null)]  // CaloriesBurned
    [InlineData(null, -1, null, null, null, null, null)]  // CaloriesEaten
    [InlineData(null, null, -1, null, null, null, null)]  // CaloriesEaten
    [InlineData(null, null, null, -1, null, null, null)]  // Steps
    [InlineData(null, null, null, null, -1.0, null, null)]// Sleep
    [InlineData(null, null, null, null, null, -1.0, null)]// Hydration
    [InlineData(null, null, null, null, null, null, -1)] // HydrationTarget
    public async Task UpdateUserHealthTargetsAsync_ThrowsArgumentException_WhenAnyNegative(
        int? caloriesBurned, int? caloriesEaten, int? caloriesTarget, int? steps, double? sleep, double? hydration, int? hydrationTarget
    )
    {
        var doc = new UserHealthData
        {
            Id = "id123",
            FirebaseUid = "testuid"
        };
        var collMock = CreateMockCollection(doc);
        var svc = new UserHealthDataService(collMock.Object);

        var updatedData = new HealthData
        {
            CaloriesBurned = caloriesBurned,
            CaloriesEaten = caloriesEaten,
            CaloriesTarget = caloriesTarget,
            Steps = steps,
            Sleep = sleep,
            Hydration = hydration,
            HydrationTarget = hydrationTarget,
            LocalDate = DateOnly.FromDateTime(DateTime.UtcNow)
        };
        await Assert.ThrowsAsync<ArgumentException>(() => svc.UpdateUserHealthDataAsync("testuid", updatedData));
    }

    [Fact]
    public async Task UpdateUserHealthDataAsync_NewDay_ResetsUnprovided_AndAppliesProvided()
    {
        var yesterday = DateOnly.FromDateTime(DateTime.UtcNow.AddDays(-1));
        var doc = new UserHealthData {
            FirebaseUid = "u1",
            LocalDate = yesterday,
            CaloriesBurned = 150,
            CaloriesEaten = 600,
            Steps = 3000,
            Sleep = 6,
            Hydration = 50
        };

        var coll = CreateMockCollection(doc);
        var svc  = new UserHealthDataService(coll.Object);

        var update = new HealthData {
            LocalDate = DateOnly.FromDateTime(DateTime.UtcNow),
            Steps = 500 // only this provided
        };

        var result = await svc.UpdateUserHealthDataAsync("u1", update);

        Assert.True(result);
        // provided
        Assert.Equal(500, doc.Steps);        
        // reset due to new day + not provided
        Assert.Equal(0,   doc.CaloriesEaten);
        Assert.Equal(0,   doc.Sleep);
        Assert.Equal(0,   doc.Hydration);
        Assert.Equal(2000, doc.CaloriesTarget);
        Assert.Equal(104, doc.HydrationTarget);
        Assert.Equal(DateOnly.FromDateTime(DateTime.UtcNow), doc.LocalDate);
    }


    [Fact]
    public async Task UpdateUserHealthTargetsAsync_ReturnsTrue_WhenUpdateSuccessful()
    {
        var doc = new UserHealthData
        {
            Id = "id123",
            FirebaseUid = "testuid"
        };
        var collMock = CreateMockCollection(doc);
        var svc = new UserHealthDataService(collMock.Object);

        var result = await svc.UpdateUserHealthTargetsAsync("testuid", 2200, 120);
        Assert.True(result);
        Assert.Equal(2200, doc.CaloriesTarget);
        Assert.Equal(120, doc.HydrationTarget);
    }
    [Theory]
    [InlineData(null, null)]
    [InlineData(0, -1)]
    [InlineData(0, 0)]
    [InlineData(-1, null)]
    public async Task UpdateUserHealthTargetAsync_ThrowsArgumentException_WhenNoPositiveValues(
        int? caloriesTarget, int? hydrationTarget
    )
    {
        var doc = new UserHealthData
        {
            Id = "id123",
            FirebaseUid = "testuid"
        };
        var collMock = CreateMockCollection(doc);
        var svc = new UserHealthDataService(collMock.Object);

        await Assert.ThrowsAsync<ArgumentException>(() => svc.UpdateUserHealthTargetsAsync("testuid", caloriesTarget, hydrationTarget));
    }
}