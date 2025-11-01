using System;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;
using AiRise.Controllers;
using AiRise.Models;
using AiRise.Models.User;
using AiRise.Services;
using Microsoft.AspNetCore.Mvc;
using MongoDB.Driver;
using Xunit;

public class UserController_IntegrationTests : IClassFixture<MongoIntegrationTest<UserData>>
{
    private readonly MongoIntegrationTest<UserData> _fixture;
    private readonly IMongoDatabase _db;
    private readonly IMongoCollection<UserData> _userDataCol;
    private readonly IMongoCollection<UserChallenges> _challengesCol;

    private readonly IUserProgramService _programsStub;
    private readonly UserDataService _userDataSvc;
    private readonly UserChallengesService _challengesSvc;
    private readonly UserController _ctrl;

    public UserController_IntegrationTests(MongoIntegrationTest<UserData> fixture)
    {
        _fixture = fixture;
        _db = _fixture.Database;

        // two collections in same DB to avoid invalid namespaces
        _userDataCol = _db.GetCollection<UserData>("TestCollection_UserData");
        _challengesCol = _db.GetCollection<UserChallenges>("TestCollection_UserChallenges");

        _userDataCol.DeleteMany(FilterDefinition<UserData>.Empty);
        _challengesCol.DeleteMany(FilterDefinition<UserChallenges>.Empty);

        _programsStub = new NoopPrograms();
        _userDataSvc = new UserDataService(_userDataCol, _challengesCol, _programsStub);
        _challengesSvc = new UserChallengesService(_challengesCol);

        // TEST-ONLY controller constructor (no leaderboard)
        _ctrl = new UserController(_userDataSvc, _challengesSvc);
    }

    [Fact]
    public async Task Post_Then_GetByFirebaseUid_Returns_Ok_With_Expected_Payload()
    {
        var res = await _ctrl.Post(new UserController.CreateUserRequest { FirebaseUid = "iu1", Email = "iu1@x" });
        var created = Assert.IsType<CreatedAtActionResult>(res);
        Assert.Equal(nameof(UserController.GetByFirebaseUid), created.ActionName);

        var get = await _ctrl.GetByFirebaseUid("iu1");
        var ok = Assert.IsType<OkObjectResult>(get);

        var payload = ok.Value!;
        var uid = (string)payload.GetType().GetProperty("firebaseUid")!.GetValue(payload)!;
        var streak = (int)payload.GetType().GetProperty("streak")!.GetValue(payload)!;

        Assert.Equal("iu1", uid);
        Assert.Equal(0, streak);
    }

    [Fact]
    public async Task Post_With_NullEmail_Stores_Empty_Email()
    {
        var res = await _ctrl.Post(new UserController.CreateUserRequest { FirebaseUid = "iuNE", Email = null });
        Assert.IsType<CreatedAtActionResult>(res);

        var doc = await _userDataCol.Find(x => x.FirebaseUid == "iuNE").FirstOrDefaultAsync();
        Assert.NotNull(doc);
        Assert.Equal(string.Empty, doc.Email);
    }

    [Fact]
    public async Task IncrementStreak_NoContent_And_LastCompletion_Is_Set()
    {
        await _ctrl.Post(new UserController.CreateUserRequest { FirebaseUid = "iu2" });

        var res = await _ctrl.IncrementStreak("iu2");
        Assert.IsType<NoContentResult>(res);

        var ch = await _challengesCol.Find(x => x.FirebaseUid == "iu2").FirstOrDefaultAsync();
        Assert.True(ch.LastCompletionEpochDay.HasValue);
    }

    [Fact]
    public async Task ResetStreak_Zeroes_Streak()
    {
        await _ctrl.Post(new UserController.CreateUserRequest { FirebaseUid = "iu3" });

        var f = Builders<UserChallenges>.Filter.Eq(x => x.FirebaseUid, "iu3");
        var u = Builders<UserChallenges>.Update.Set(x => x.StreakCount, 6);
        await _challengesCol.UpdateOneAsync(f, u);

        var res = await _ctrl.ResetStreak("iu3");
        Assert.IsType<NoContentResult>(res);

        var after = await _challengesCol.Find(x => x.FirebaseUid == "iu3").FirstOrDefaultAsync();
        Assert.Equal(0, after.StreakCount);
    }

    [Fact]
    public void Legacy_Routes_Return_Expected_Statuses()
    {
        Assert.IsType<NotFoundObjectResult>(_ctrl.GetById("zzz"));
        Assert.IsType<BadRequestObjectResult>(_ctrl.UpdateFirebaseUid("zzz", ""));
        Assert.IsType<BadRequestObjectResult>(_ctrl.DeleteUser("zzz"));
    }

    private sealed class NoopPrograms : IUserProgramService
    {
        public Task<string> CreateAsync(
            string firebaseUid,
            ProgramType type,
            List<string> workoutDays,
            CancellationToken ct = default)
            => Task.FromResult(Guid.NewGuid().ToString());

        public Task<UserProgramDoc?> GetAsync(
            string firebaseUid,
            CancellationToken ct = default)
            => Task.FromResult<UserProgramDoc?>(null);

        public Task<bool> UpdateAsync(
            string firebaseUid,
            UserProgram mutatedProgram,
            CancellationToken ct = default)
            => Task.FromResult(true);

        public Task<UserProgramDoc> AssignFromExplicitAsync(
            string firebaseUid,
            ProgramType type,
            List<string> workoutDays,
            ProgramPreferences? programPreferences = null,
            CancellationToken ct = default)
            => Task.FromResult(new UserProgramDoc
            {
                FirebaseUid = firebaseUid,
                Program = new UserProgram
                {
                    TemplateName = "stub",
                    Days = workoutDays?.Count ?? 3,
                    Type = type,
                    CreatedAtUtc = DateTime.UtcNow,
                    UpdatedAtUtc = DateTime.UtcNow
                },
                LastUpdatedUtc = DateTime.UtcNow
            });

        public Task<bool> RelabelDayNamesAsync(
            string firebaseUid,
            List<string> workoutDays,
            ProgramPreferences? preferences = null,
            CancellationToken ct = default)
            => Task.FromResult(true);

        public Task<bool> UpdatePreferencesAsync(
            string firebaseUid,
            ProgramType type,
            List<string> workoutDays,
            ProgramPreferences preferences,
            CancellationToken ct = default)
            => Task.FromResult(true);
    }
}
