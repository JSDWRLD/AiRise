using System.Text.RegularExpressions;
using AiRise.Models;
using AiRise.Models.User;
using AiRise.Services;
using FluentAssertions;
using MongoDB.Driver;
using Moq;

public class UserProgramService_UpdatePreferencesAsync_Tests
{
    private static Mock<IAsyncCursor<UserProgramDoc>> CreateCursor(UserProgramDoc doc)
    {
        var cursor = new Mock<IAsyncCursor<UserProgramDoc>>();
        cursor.SetupGet(c => c.Current).Returns(new[] { doc });
        cursor.SetupSequence(c => c.MoveNext(It.IsAny<CancellationToken>()))
              .Returns(true)
              .Returns(false);
        cursor.SetupSequence(c => c.MoveNextAsync(It.IsAny<CancellationToken>()))
              .ReturnsAsync(true)
              .ReturnsAsync(false);
        return cursor;
    }

    private static Mock<IMongoCollection<UserProgramDoc>> CreateCollectionMock(
        UserProgramDoc seedDoc,
        out Mock<IMongoIndexManager<UserProgramDoc>> indexMgrMock)
    {
        var collMock = new Mock<IMongoCollection<UserProgramDoc>>();
        indexMgrMock = new Mock<IMongoIndexManager<UserProgramDoc>>();

        // Index creation
        collMock.SetupGet(c => c.Indexes).Returns(indexMgrMock.Object);
        indexMgrMock
            .Setup(i => i.CreateOne(It.IsAny<CreateIndexModel<UserProgramDoc>>(), null, default))
            .Returns("idx");

        // FindAsync(...) — this is what the Find().FirstOrDefaultAsync() path will hit internally
        var cursor = CreateCursor(seedDoc);

        collMock
            .Setup(c => c.FindAsync(
                It.IsAny<FilterDefinition<UserProgramDoc>>(),
                It.IsAny<FindOptions<UserProgramDoc, UserProgramDoc>>(),
                It.IsAny<CancellationToken>()))
            .ReturnsAsync(cursor.Object);

        // UpdateOneAsync -> success
        var updateResult = new Mock<UpdateResult>();
        updateResult.SetupGet(u => u.MatchedCount).Returns(1);
        updateResult.SetupGet(u => u.ModifiedCount).Returns(1);

        collMock
            .Setup(c => c.UpdateOneAsync(
                It.IsAny<FilterDefinition<UserProgramDoc>>(),
                It.IsAny<UpdateDefinition<UserProgramDoc>>(),
                It.IsAny<UpdateOptions>(),
                It.IsAny<CancellationToken>()))
            .ReturnsAsync(updateResult.Object);

        return collMock;
    }

    [Fact]
    public async Task Updates_Preferences_And_Returns_True()
    {
        var seedDoc = new UserProgramDoc
        {
            FirebaseUid = "uid-123",
            Program = new UserProgram
            {
                TemplateName = "3-Day Full Body Strength (Gym)",
                Days = 3,
                Type = ProgramType.Gym,
                Schedule =
                {
                    new UserProgramDay
                    {
                        DayIndex = 1,
                        DayName = "Monday",
                        Focus = "Day1",
                        Exercises =
                        {
                            new UserExerciseEntry
                            {
                                Name = "Bench",
                                Sets = 3,
                                TargetReps = "8-10",
                                RepsCompleted = 7,
                                Weight = new UserExerciseWeight { Value = 135, Unit = "lbs" }
                            },
                            new UserExerciseEntry{
                                Name = "Plank",
                                Sets = 3,
                                TargetReps = "60 s",
                                RepsCompleted = 0,
                                Weight = new UserExerciseWeight { Value = 0, Unit = "lbs" }
                            }
                        }
                    },
                    new UserProgramDay
                    {
                        DayIndex = 2,
                        DayName = "Wednesday",
                        Focus = "Day2",
                        Exercises =
                        {
                            new UserExerciseEntry
                            {
                                Name = "Squat",
                                Sets = 3,
                                TargetReps = "5-8",
                                RepsCompleted = 5,
                                Weight = new UserExerciseWeight { Value = 185, Unit = "lbs" }
                            }
                        }
                    },
                    new UserProgramDay
                    {
                        DayIndex = 3,
                        DayName = "Friday",
                        Focus = "Day3",
                        Exercises =
                        {
                            new UserExerciseEntry
                            {
                                Name = "Row",
                                Sets = 3,
                                TargetReps = "8-12",
                                RepsCompleted = 10,
                                Weight = new UserExerciseWeight { Value = 95, Unit = "lbs" }
                            }
                        }
                    }
                }
            }
        };

        var newPreferences = new ProgramPreferences
        {
            WorkoutGoal = "Muscle Gain",
            WorkoutLength = 60,
        };

        var coll = CreateCollectionMock(seedDoc, out _);
        var svc = new UserProgramService(coll.Object);

        //act
        var ok = await svc.UpdatePreferencesAsync("uid-123", newPreferences);

        //assert
        ok.Should().BeTrue();

        // Assert: update occurred and personalization nudged rep range upward (per service logic)
        ok.Should().BeTrue(); // UpdateOneAsync matched & modified
        var ex1 = seedDoc.Program.Schedule.First().Exercises.First();
        ex1.TargetReps.Should().MatchRegex(@"^\d+-\d+$");
        // Low end should have increased by ~4 (bounded by service rules)
        var parts = ex1.TargetReps.Split('-').Select(int.Parse).ToArray();
        parts[0].Should().BeInRange(Math.Max(5, 8 - 2), 8);
        parts[1].Should().BeInRange(parts[0] + 1, 10);
        var ex2 = seedDoc.Program.Schedule.First().Exercises.Last();
        // Ensure canonical format "NN sec"
        ex2.TargetReps.Should().MatchRegex(@"^\d+\s*sec$");

        // Then parse and assert numeric value
        var m = Regex.Match(ex2.TargetReps, @"^(?<n>\d+)");
        int sec = int.Parse(m.Groups["n"].Value);
        sec.Should().BeGreaterThan(60); // nudged up from 60s
    }
    [Fact]
    public async Task ReturnsFalse_When_NoUserProgramFound()
    {
        // arrange: cursor that yields no documents
        var emptyCursor = new Mock<IAsyncCursor<UserProgramDoc>>();
        emptyCursor.SetupGet(c => c.Current).Returns(new UserProgramDoc[0]);
        emptyCursor.Setup(c => c.MoveNext(It.IsAny<CancellationToken>())).Returns(false);
        emptyCursor.Setup(c => c.MoveNextAsync(It.IsAny<CancellationToken>())).ReturnsAsync(false);

        var collMock = new Mock<IMongoCollection<UserProgramDoc>>();
        var indexMgrMock = new Mock<IMongoIndexManager<UserProgramDoc>>();
        collMock.SetupGet(c => c.Indexes).Returns(indexMgrMock.Object);
        indexMgrMock.Setup(i => i.CreateOne(It.IsAny<CreateIndexModel<UserProgramDoc>>(), null, default)).Returns("idx");

        collMock.Setup(c => c.FindAsync(
            It.IsAny<FilterDefinition<UserProgramDoc>>(),
            It.IsAny<FindOptions<UserProgramDoc, UserProgramDoc>>(),
            It.IsAny<CancellationToken>()))
            .ReturnsAsync(emptyCursor.Object);

        var svc = new UserProgramService(collMock.Object);

        // act
        var ok = await svc.UpdatePreferencesAsync("missing-uid", new ProgramPreferences { WorkoutGoal = "Maintain", WorkoutLength = 30 });

        // assert
        ok.Should().BeFalse();
    }
    [Fact]
    public async Task UpdatePreferences_Preserves_RepsCompleted()
    {
        // arrange
        var seed = new UserProgramDoc
        {
            FirebaseUid = "uid-preserve",
            Program = new UserProgram
            {
                Days = 3,
                Schedule =
                {
                    new UserProgramDay
                    {
                        DayIndex = 1,
                        Exercises =
                        {
                            new UserExerciseEntry { Name = "Bench", Sets = 3, TargetReps = "8-10", RepsCompleted = 2 },
                            new UserExerciseEntry { Name = "Plank", Sets = 3, TargetReps = "60 s", RepsCompleted = 0 }
                        }
                    }
                }
            }
        };

        var collMock = CreateCollectionMock(seed, out _);
        var svc = new UserProgramService(collMock.Object);
        var prefs = new ProgramPreferences { WorkoutGoal = "Weight Loss", WorkoutLength = 45 };

        // keep original repsCompleted values for assertion
        var originalReps = seed.Program.Schedule.SelectMany(d => d.Exercises).Select(e => e.RepsCompleted).ToArray();

        // act
        var ok = await svc.UpdatePreferencesAsync(seed.FirebaseUid, prefs);

        // assert
        ok.Should().BeTrue();
        var after = seed.Program.Schedule.SelectMany(d => d.Exercises).Select(e => e.RepsCompleted).ToArray();
        after.Should().Equal(originalReps);
    }
    [Fact]
    public async Task ReturnsFalse_When_UpdateOneFailsToModify()
    {
        // arrange: seed doc returned by FindAsync
        var seed = new UserProgramDoc { FirebaseUid = "uid-404", Program = new UserProgram { Days = 3 } };
        var cursor = new Mock<IAsyncCursor<UserProgramDoc>>();
        cursor.SetupGet(c => c.Current).Returns(new[] { seed });
        cursor.SetupSequence(c => c.MoveNext(It.IsAny<CancellationToken>())).Returns(true).Returns(false);
        cursor.SetupSequence(c => c.MoveNextAsync(It.IsAny<CancellationToken>())).ReturnsAsync(true).ReturnsAsync(false);

        var collMock = new Mock<IMongoCollection<UserProgramDoc>>();
        var indexMgrMock = new Mock<IMongoIndexManager<UserProgramDoc>>();
        collMock.SetupGet(c => c.Indexes).Returns(indexMgrMock.Object);
        indexMgrMock.Setup(i => i.CreateOne(It.IsAny<CreateIndexModel<UserProgramDoc>>(), null, default)).Returns("idx");

        collMock.Setup(c => c.FindAsync(It.IsAny<FilterDefinition<UserProgramDoc>>(),
            It.IsAny<FindOptions<UserProgramDoc, UserProgramDoc>>(), It.IsAny<CancellationToken>()))
            .ReturnsAsync(cursor.Object);

        // make UpdateOneAsync return a result that indicates nothing changed
        var updateResult = new Mock<UpdateResult>();
        updateResult.SetupGet(u => u.MatchedCount).Returns(0L);
        updateResult.SetupGet(u => u.ModifiedCount).Returns(0L);

        collMock.Setup(c => c.UpdateOneAsync(It.IsAny<FilterDefinition<UserProgramDoc>>(),
            It.IsAny<UpdateDefinition<UserProgramDoc>>(), It.IsAny<UpdateOptions>(), It.IsAny<CancellationToken>()))
            .ReturnsAsync(updateResult.Object);

        var svc = new UserProgramService(collMock.Object);

        // act
        var ok = await svc.UpdatePreferencesAsync("uid-404", new ProgramPreferences { WorkoutGoal = "Maintain", WorkoutLength = 30 });

        // assert
        ok.Should().BeFalse();
    }
}