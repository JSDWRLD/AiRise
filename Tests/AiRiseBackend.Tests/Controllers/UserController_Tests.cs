using System;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;
using AiRise.Controllers;
using AiRise.Models;
using AiRise.Models.User;
using AiRise.Services;
using Microsoft.AspNetCore.Mvc;
using Mongo2Go;
using MongoDB.Driver;
using Xunit;

public class UserController_Tests : IDisposable
{
    private readonly MongoDbRunner _runner;
    private readonly IMongoDatabase _db;
    private readonly IMongoCollection<UserData> _userDataCol;
    private readonly IMongoCollection<UserChallenges> _challengesCol;

    private readonly IUserProgramService _programsStub;
    private readonly UserDataService _userDataSvc;
    private readonly UserChallengesService _challengesSvc;
    private readonly UserController _ctrl;

    public UserController_Tests()
    {
        _runner = MongoDbRunner.Start();
        var client = new MongoClient(_runner.ConnectionString);
        _db = client.GetDatabase($"UserCtrl_{Guid.NewGuid():N}");

        _userDataCol = _db.GetCollection<UserData>("user.data_ctrl");
        _challengesCol = _db.GetCollection<UserChallenges>("user.challenges_ctrl");

        _userDataCol.DeleteMany(FilterDefinition<UserData>.Empty);
        _challengesCol.DeleteMany(FilterDefinition<UserChallenges>.Empty);

        _programsStub = new NoopPrograms();
        _userDataSvc = new UserDataService(_userDataCol, _challengesCol, _programsStub);
        _challengesSvc = new UserChallengesService(_challengesCol);

        // TEST-ONLY controller constructor (no leaderboard dependency)
        _ctrl = new UserController(_userDataSvc, _challengesSvc);
    }

    [Fact]
    public async Task Post_Creates_Profile_And_Challenges_And_Returns_201()
    {
        var req = new UserController.CreateUserRequest { FirebaseUid = "u1", Email = "u1@example.com" };

        var result = await _ctrl.Post(req);
        var created = Assert.IsType<CreatedAtActionResult>(result);
        Assert.Equal(nameof(UserController.GetByFirebaseUid), created.ActionName);

        var user = await _userDataCol.Find(x => x.FirebaseUid == "u1").FirstOrDefaultAsync();
        var ch = await _challengesCol.Find(x => x.FirebaseUid == "u1").FirstOrDefaultAsync();

        Assert.NotNull(user);
        Assert.NotNull(ch);
        Assert.Equal("u1@example.com", user.Email);
    }

    [Fact]
    public async Task Post_BadRequest_When_Missing_Uid()
    {
        var res = await _ctrl.Post(new UserController.CreateUserRequest { FirebaseUid = "" });
        Assert.IsType<BadRequestObjectResult>(res);
    }

    [Fact]
    public async Task Post_NullEmail_Is_Stored_As_Empty_String()
    {
        var req = new UserController.CreateUserRequest { FirebaseUid = "u1e", Email = null };
        var result = await _ctrl.Post(req);
        Assert.IsType<CreatedAtActionResult>(result);

        var user = await _userDataCol.Find(x => x.FirebaseUid == "u1e").FirstOrDefaultAsync();
        Assert.NotNull(user);
        Assert.Equal(string.Empty, user.Email);
    }

    [Fact]
    public async Task GetByFirebaseUid_Ok_Returns_Profile_And_Streak()
    {
        await _ctrl.Post(new UserController.CreateUserRequest { FirebaseUid = "u2", Email = "e2@x" });

        var result = await _ctrl.GetByFirebaseUid("u2");
        var ok = Assert.IsType<OkObjectResult>(result);

        var payload = ok.Value!;
        var uid = (string)payload.GetType().GetProperty("firebaseUid")!.GetValue(payload)!;
        var streak = (int)payload.GetType().GetProperty("streak")!.GetValue(payload)!;

        Assert.Equal("u2", uid);
        Assert.Equal(0, streak);
    }

    [Fact]
    public async Task GetByFirebaseUid_NotFound_When_User_Missing()
    {
        var result = await _ctrl.GetByFirebaseUid("nope");
        Assert.IsType<NotFoundResult>(result);
    }

    [Fact]
    public async Task IncrementStreak_NoContent_And_Sets_LastCompletion()
    {
        await _ctrl.Post(new UserController.CreateUserRequest { FirebaseUid = "u3" });
        var res = await _ctrl.IncrementStreak("u3");
        Assert.IsType<NoContentResult>(res);

        var ch = await _challengesCol.Find(x => x.FirebaseUid == "u3").FirstOrDefaultAsync();
        Assert.NotNull(ch);
        Assert.True(ch.LastCompletionEpochDay.HasValue);
    }

    [Fact]
    public async Task ResetStreak_Sets_StreakCount_To_Zero()
    {
        await _ctrl.Post(new UserController.CreateUserRequest { FirebaseUid = "u4" });

        // Manually bump streak
        var f = Builders<UserChallenges>.Filter.Eq(x => x.FirebaseUid, "u4");
        var u = Builders<UserChallenges>.Update.Set(x => x.StreakCount, 9);
        await _challengesCol.UpdateOneAsync(f, u);

        var res = await _ctrl.ResetStreak("u4");
        Assert.IsType<NoContentResult>(res);

        var after = await _challengesCol.Find(x => x.FirebaseUid == "u4").FirstOrDefaultAsync();
        Assert.Equal(0, after.StreakCount);
    }

    [Fact]
    public void Legacy_NotSupported_Routes()
    {
        Assert.IsType<NotFoundObjectResult>(_ctrl.GetById("any"));
        Assert.IsType<BadRequestObjectResult>(_ctrl.UpdateFirebaseUid("any", ""));
        Assert.IsType<BadRequestObjectResult>(_ctrl.DeleteUser("any"));
    }

    // ----- helpers -----

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

    public void Dispose() => _runner.Dispose();
}
