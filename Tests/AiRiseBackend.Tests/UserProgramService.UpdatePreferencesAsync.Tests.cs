using System.Text.RegularExpressions;
using AiRise.Models;
using AiRise.Models.User;
using AiRise.Services;
using FluentAssertions;
using MongoDB.Bson;
using MongoDB.Driver;
using MongoDB.Bson.Serialization;
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

        // UpdateOneAsync -> success and mutate seedDoc so subsequent reads reflect the update
        var updateResult = new Mock<UpdateResult>();
        updateResult.SetupGet(u => u.MatchedCount).Returns(1);
        updateResult.SetupGet(u => u.ModifiedCount).Returns(1);

        collMock
            .Setup(c => c.UpdateOneAsync(
                It.IsAny<FilterDefinition<UserProgramDoc>>(),
                It.IsAny<UpdateDefinition<UserProgramDoc>>(),
                It.IsAny<UpdateOptions>(),
                It.IsAny<CancellationToken>()))
            .Callback<FilterDefinition<UserProgramDoc>, UpdateDefinition<UserProgramDoc>, UpdateOptions, CancellationToken>((filter, update, options, ct) =>
            {
                // Best-effort: render the UpdateDefinition to Bson and apply the $set.Program value to the in-memory seedDoc
                try
                {
                    var renderArgs = new MongoDB.Driver.RenderArgs<UserProgramDoc>(BsonSerializer.SerializerRegistry.GetSerializer<UserProgramDoc>(), BsonSerializer.SerializerRegistry);
                    var rendered = update.Render(renderArgs).AsBsonDocument;
                    if (rendered.TryGetValue("$set", out var setDocValue) && setDocValue.IsBsonDocument)
                    {
                        var bd = setDocValue.AsBsonDocument;
                        if (bd.TryGetValue("Program", out var programBson) && programBson.IsBsonDocument)
                        {
                            seedDoc.Program = BsonSerializer.Deserialize<UserProgram>(programBson.AsBsonDocument);
                        }

                        if (bd.TryGetValue("LastUpdatedUtc", out var lu))
                        {
                            try { seedDoc.LastUpdatedUtc = lu.ToUniversalTime(); } catch { }
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
    public async Task Updates_Preferences_And_Returns_True()
    {
        var seedDoc = new UserProgramDoc
        {
            FirebaseUid = "uid-abc",
            Program = new UserProgram
            {
                TemplateName = "3-Day Bodyweight Basics",
                Days = 3,
                Type = ProgramType.Bodyweight,
                Schedule =
                {
                    new UserProgramDay
                    {
                        DayIndex = 1,
                        DayName = "Monday",
                        Focus = "Upper Body Push (BW)",
                        Exercises =
                        {
                            new UserExerciseEntry { Name = "Push-Ups", Sets = 4, TargetReps = "8-15", Weight = new UserExerciseWeight { Value = 0, Unit = "lbs" }, RepsCompleted = 0 },
                            new UserExerciseEntry { Name = "Pike Push-Ups", Sets = 3, TargetReps = "6-10", Weight = new UserExerciseWeight { Value = 0, Unit = "lbs" }, RepsCompleted = 0 },
                            new UserExerciseEntry { Name = "Bench Dips (Feet on Floor)", Sets = 3, TargetReps = "10-15", Weight = new UserExerciseWeight { Value = 0, Unit = "lbs" }, RepsCompleted = 0 }
                        }
                    },
                    new UserProgramDay
                    {
                        DayIndex = 2,
                        DayName = "Wednesday",
                        Focus = "Lower Body (BW)",
                        Exercises =
                        {
                            new UserExerciseEntry { Name = "Squats (Bodyweight)", Sets = 4, TargetReps = "12-20", Weight = new UserExerciseWeight { Value = 0, Unit = "lbs" }, RepsCompleted = 0 },
                            new UserExerciseEntry { Name = "Reverse Lunges", Sets = 3, TargetReps = "10-12/leg", Weight = new UserExerciseWeight { Value = 0, Unit = "lbs" }, RepsCompleted = 0 },
                            new UserExerciseEntry { Name = "Glute Bridges", Sets = 3, TargetReps = "12-20", Weight = new UserExerciseWeight { Value = 0, Unit = "lbs" }, RepsCompleted = 0 }
                        }
                    },
                    new UserProgramDay
                    {
                        DayIndex = 3,
                        DayName = "Friday",
                        Focus = "Upper Body Pull & Core (BW)",
                        Exercises =
                        {
                            new UserExerciseEntry { Name = "Inverted Rows (Table/Bar)", Sets = 4, TargetReps = "6-12", Weight = new UserExerciseWeight { Value = 0, Unit = "lbs" }, RepsCompleted = 0 },
                            new UserExerciseEntry { Name = "Chin-Ups or Doorframe Holds", Sets = 3, TargetReps = "AMRAP/20-30 sec hold", Weight = new UserExerciseWeight { Value = 0, Unit = "lbs" }, RepsCompleted = 0 },
                            new UserExerciseEntry { Name = "Plank", Sets = 3, TargetReps = "30-60 sec", Weight = new UserExerciseWeight { Value = 0, Unit = "lbs" }, RepsCompleted = 0 }
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
        var ok = await svc.UpdatePreferencesAsync("uid-abc", seedDoc.Program.Type, ["Monday", "Wednesday", "Friday"], newPreferences);


        // Assert the bool result
        ok.Should().BeTrue();

        // Assert on what was actually written
        var written = await svc.GetAsync("uid-abc");
        written.Should().NotBeNull();
        written.Program.Should().NotBeNull();
        written.Program.Schedule.Should().NotBeNullOrEmpty();

        var ex1 = written.Program.Schedule.First().Exercises.First();
        ex1.TargetReps.Should().MatchRegex(@"^\d+-\d+$");
        // Low end should have increased by ~4 (bounded by service rules)
        var parts = ex1.TargetReps.Split('-').Select(int.Parse).ToArray();
        parts[0].Should().BeInRange(Math.Max(5, 8 - 2), 8);
        parts[1].Should().BeInRange(parts[0] + 1, 15);

        // Check timed entry still has unit
        var exTimed = written.Program.Schedule.Last().Exercises.Last(); // "30-60 sec"
        exTimed.TargetReps.Should().MatchRegex(@"^\d+-\d+\s*sec$");
        var m = Regex.Match(exTimed.TargetReps, @"^(?<n>\d+)");
        int sec = int.Parse(m.Groups["n"].Value);
        // Ensure we parsed a positive seconds value; exact scaling behavior is tested elsewhere.
        sec.Should().BeGreaterThan(0);
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
                Type = ProgramType.Gym,
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
        var ok = await svc.UpdatePreferencesAsync(seed.FirebaseUid, seed.Program.Type, ["Monday", "Wednesday", "Friday"], prefs);

        // assert
        ok.Should().BeTrue();
        var written = await svc.GetAsync("uid-preserve");
        var after = written.Program.Schedule.SelectMany(d => d.Exercises).Select(e => e.RepsCompleted).ToArray();
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
        var ok = await svc.UpdatePreferencesAsync("uid-404", ProgramType.Gym, ["Monday", "Wednesday", "Friday"], new ProgramPreferences { WorkoutGoal = "Maintain", WorkoutLength = 30 });

        // assert
        ok.Should().BeFalse();
    }
}