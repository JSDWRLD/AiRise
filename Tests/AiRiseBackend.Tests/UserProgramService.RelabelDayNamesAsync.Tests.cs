using AiRise.Models;
using AiRise.Models.User;
using AiRise.Services;
using FluentAssertions;
using Moq;
using MongoDB.Driver;

public class UserProgramService_RelabelDayNamesAsync_Tests
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
    public async Task Renames_Days_And_Preserves_Progress()
    {
        // Seed
        var seed = new UserProgramDoc
        {
            FirebaseUid = "uid-abc",
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

        var newNames = new System.Collections.Generic.List<string> { "Tue", "Thu", "Sat" };

        var coll = CreateCollectionMock(seed, out _);
        var svc = new UserProgramService(coll.Object);

        // act
        var ok = await svc.RelabelDayNamesAsync(seed.FirebaseUid, newNames);

        // assert
        ok.Should().BeTrue();

        var sched = seed.Program.Schedule.OrderBy(d => d.DayIndex).ToList();
        sched[0].DayName.Should().Be("Tue");
        sched[1].DayName.Should().Be("Thu");
        sched[2].DayName.Should().Be("Sat");

        // progress preserved
        sched[0].Exercises[0].RepsCompleted.Should().Be(7);
        sched[0].Exercises[0].Weight.Value.Should().Be(135);
        sched[1].Exercises[0].RepsCompleted.Should().Be(5);
        sched[1].Exercises[0].Weight.Value.Should().Be(185);
        sched[2].Exercises[0].RepsCompleted.Should().Be(10);
        sched[2].Exercises[0].Weight.Value.Should().Be(95);
    }
}
