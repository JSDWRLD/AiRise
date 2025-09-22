using System.Threading;
using System.Threading.Tasks;
using AiRise.Models;
using AiRise.Models.User;
using AiRise.Services;
using FluentAssertions;
using Moq;
using MongoDB.Driver;
using Xunit;

public class UserProgramService_AssignFromExplicitAsync_Tests
{
    private static Mock<IMongoCollection<UserProgramDoc>> CreateCollectionMock(out Mock<IMongoIndexManager<UserProgramDoc>> indexMgrMock)
    {
        var collMock = new Mock<IMongoCollection<UserProgramDoc>>();
        indexMgrMock = new Mock<IMongoIndexManager<UserProgramDoc>>();

        collMock.SetupGet(c => c.Indexes).Returns(indexMgrMock.Object);
        indexMgrMock.Setup(i => i.CreateOne(It.IsAny<CreateIndexModel<UserProgramDoc>>(), null, default))
                    .Returns("idx");
        return collMock;
    }

    [Theory]
    [InlineData(0)]
    [InlineData(1)]
    [InlineData(2)]
    [InlineData(7)]
    public async Task Throws_When_Days_OutOfRange(int count)
    {
        var coll = CreateCollectionMock(out _);
        var svc = new UserProgramService(coll.Object);

        var days = new System.Collections.Generic.List<string>();
        for (int i = 0; i < count; i++) days.Add("Monday");

        var act = () => svc.AssignFromExplicitAsync("uid-1", ProgramType.Gym, days);
        await act.Should().ThrowAsync<System.ArgumentOutOfRangeException>();
    }

    [Fact]
    public async Task Uses_Upsert_And_Returns_Document()
    {
        var coll = CreateCollectionMock(out _);
        var svc = new UserProgramService(coll.Object);

        var firebaseUid = "uid-123";
        var days = new System.Collections.Generic.List<string> { "Monday", "Wednesday", "Friday" };
        var type = ProgramType.Gym;

        FindOneAndUpdateOptions<UserProgramDoc, UserProgramDoc>? capturedOptions = null;

        coll.Setup(c => c.FindOneAndUpdateAsync(
                It.IsAny<FilterDefinition<UserProgramDoc>>(),
                It.IsAny<UpdateDefinition<UserProgramDoc>>(),
                It.IsAny<FindOneAndUpdateOptions<UserProgramDoc, UserProgramDoc>>(),
                It.IsAny<CancellationToken>()))
            .Callback<FilterDefinition<UserProgramDoc>, UpdateDefinition<UserProgramDoc>, FindOneAndUpdateOptions<UserProgramDoc, UserProgramDoc>, CancellationToken>(
                (f, u, o, ct) => capturedOptions = o)
            .ReturnsAsync(new UserProgramDoc
            {
                FirebaseUid = firebaseUid,
                Program = new UserProgram
                {
                    Days = days.Count,
                    Type = type
                }
            });

        var result = await svc.AssignFromExplicitAsync(firebaseUid, type, days);

        result.Should().NotBeNull();
        result.FirebaseUid.Should().Be(firebaseUid);
        result.Program.Days.Should().Be(days.Count);
        result.Program.Type.Should().Be(type);

        capturedOptions.Should().NotBeNull();
        capturedOptions!.IsUpsert.Should().BeTrue();
        capturedOptions.ReturnDocument.Should().Be(ReturnDocument.After);
    }
}
