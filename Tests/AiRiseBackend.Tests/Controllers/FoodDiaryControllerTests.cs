using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using AiRise.Controllers;
using AiRise.Models.FoodDiary;
using AiRise.Services;
using Microsoft.AspNetCore.Mvc;
using MongoDB.Driver;
using Moq;
using Xunit;

public class FoodDiaryController_Tests
{
    // ---- Minimal helpers to mock FindAsync → cursor (no extension setups) ----
    private static Mock<IAsyncCursor<T>> MakeCursor<T>(IEnumerable<T> items)
    {
        var data = (items ?? Enumerable.Empty<T>()).ToList();
        var idx = 0;
        var batch = new List<T>();
        var cursor = new Mock<IAsyncCursor<T>>();
        cursor.SetupGet(c => c.Current).Returns(() => batch);
        cursor.Setup(c => c.MoveNext(It.IsAny<CancellationToken>())).Returns(() =>
        {
            if (idx >= data.Count) { batch = new List<T>(); return false; }
            batch = new List<T> { data[idx++] };
            return true;
        });
        cursor.Setup(c => c.MoveNextAsync(It.IsAny<CancellationToken>())).ReturnsAsync(() =>
        {
            if (idx >= data.Count) { batch = new List<T>(); return false; }
            batch = new List<T> { data[idx++] };
            return true;
        });
        return cursor;
    }

    private sealed class Harness
    {
        public Mock<IMongoCollection<FoodDiaryMonth>> Coll { get; } = new();
        public FoodDiaryService Service => new FoodDiaryService(Coll.Object);

        public void SetupFindAsyncSingle(FoodDiaryMonth? doc)
        {
            var cursor = MakeCursor(doc is null ? Enumerable.Empty<FoodDiaryMonth>() : new[] { doc });
            Coll.Setup(c => c.FindAsync(
                    It.IsAny<FilterDefinition<FoodDiaryMonth>>(),
                    It.IsAny<FindOptions<FoodDiaryMonth, FoodDiaryMonth>>(),
                    It.IsAny<CancellationToken>()))
                .ReturnsAsync(cursor.Object);
        }

        public void SetupReplaceSuccess()
        {
            var r = new Mock<ReplaceOneResult>();
            r.SetupGet(x => x.IsAcknowledged).Returns(true);
            r.SetupGet(x => x.MatchedCount).Returns(1);
            r.SetupGet(x => x.ModifiedCount).Returns(1);
            Coll.Setup(c => c.ReplaceOneAsync(
                    It.IsAny<FilterDefinition<FoodDiaryMonth>>(),
                    It.IsAny<FoodDiaryMonth>(),
                    It.IsAny<ReplaceOptions>(),
                    It.IsAny<CancellationToken>()))
                .ReturnsAsync(r.Object);
        }

        public void SetupInsertNoop()
        {
            Coll.Setup(c => c.InsertOneAsync(
                    It.IsAny<FoodDiaryMonth>(),
                    It.IsAny<InsertOneOptions>(),
                    It.IsAny<CancellationToken>()))
                .Returns(Task.CompletedTask);
        }
    }

    [Fact]
    public async Task GetMonth_Returns_Ok_When_Service_Succeeds()
    {
        var h = new Harness();

        // existing doc to be returned by service
        var m = new FoodDiaryMonth { UserId = "u1", Year = 2025, Month = 10, Days = new List<DiaryDay>(new DiaryDay[31]) };
        h.SetupFindAsyncSingle(m);
        h.SetupInsertNoop(); // GetMonth might insert when malformed/absent

        var ctrl = new FoodDiaryController(h.Service);

        var res = await ctrl.GetMonth("u1", 2025, 10, CancellationToken.None);

        var ok = Assert.IsType<OkObjectResult>(res);
        var payload = Assert.IsType<FoodDiaryMonth>(ok.Value);
        Assert.Equal("u1", payload.UserId);
        Assert.Equal(2025, payload.Year);
        Assert.Equal(10, payload.Month);
    }

    [Fact]
    public async Task GetMonth_Returns_BadRequest_On_InvalidOperation()
    {
        var h = new Harness();

        // Make FindAsync return empty; service should throw for "older than 1 year" if you request far in the past.
        h.SetupFindAsyncSingle(null);

        var ctrl = new FoodDiaryController(h.Service);

        // Choose a date that your service treats as invalid (older than one year).
        var res = await ctrl.GetMonth("u1", DateTime.UtcNow.AddYears(-2).Year, DateTime.UtcNow.Month, CancellationToken.None);

        Assert.IsType<BadRequestObjectResult>(res);
    }

    [Theory]
    [InlineData(-1, "Oats")]   // invalid calories
    [InlineData(100, "")]      // missing name
    public async Task AddFoodEntry_Returns_BadRequest_On_Invalid_Input(int calories, string name)
    {
        var h = new Harness();
        var ctrl = new FoodDiaryController(h.Service);

        var entry = new FoodEntry { Calories = calories, Name = name };

        var res = await ctrl.AddFoodEntry("u1", 2025, 10, 1, "breakfast", entry, CancellationToken.None);

        var br = Assert.IsType<BadRequestObjectResult>(res);
        Assert.Contains("Invalid input", br.Value!.ToString());
    }

    [Fact]
    public async Task AddFoodEntry_Returns_NoContent_On_Success()
    {
        var h = new Harness();
        // Service needs a month doc present so it can replace after editing
        var m = new FoodDiaryMonth { UserId = "u1", Year = 2025, Month = 10, Days = Enumerable.Range(1, 31).Select(d => new DiaryDay { Day = d, Meals = new Meals() }).ToList() };
        h.SetupFindAsyncSingle(m);
        h.SetupReplaceSuccess();

        var ctrl = new FoodDiaryController(h.Service);

        var entry = new FoodEntry { Calories = 200, Name = "Oats" };
        var res = await ctrl.AddFoodEntry("u1", 2025, 10, 1, "breakfast", entry, CancellationToken.None);

        Assert.IsType<NoContentResult>(res);
    }

    [Fact]
    public async Task EditFoodEntry_Returns_NoContent_On_Success()
    {
        var h = new Harness();

        // Build a month containing an entry id "E1"
        var month = new FoodDiaryMonth
        {
            UserId = "u1",
            Year = 2025,
            Month = 10,
            Days = Enumerable.Range(1, 31).Select(d => new DiaryDay { Day = d, Meals = new Meals() }).ToList()
        };
        month.Days[0].Meals.Breakfast.Add(new FoodEntry { Id = "E1", Name = "Toast", Calories = 100 });

        h.SetupFindAsyncSingle(month);
        h.SetupReplaceSuccess();

        var ctrl = new FoodDiaryController(h.Service);

        var updated = new FoodEntry { Name = "Toast w/ PB", Calories = 220 };
        var res = await ctrl.EditFoodEntry("u1", "E1", updated, CancellationToken.None);

        Assert.IsType<NoContentResult>(res);
    }

    [Fact]
    public async Task EditFoodEntry_Returns_NotFound_On_Missing()
    {
        var h = new Harness();
        // No matching entry → service should throw KeyNotFoundException
        h.SetupFindAsyncSingle(new FoodDiaryMonth
        {
            UserId = "u1",
            Year = 2025,
            Month = 10,
            Days = Enumerable.Range(1, 31).Select(d => new DiaryDay { Day = d, Meals = new Meals() }).ToList()
        });

        var ctrl = new FoodDiaryController(h.Service);

        var res = await ctrl.EditFoodEntry("u1", "missing", new FoodEntry { Name = "x", Calories = 1 }, CancellationToken.None);

        Assert.IsType<NotFoundObjectResult>(res);
    }

    [Fact]
    public async Task DeleteFoodEntry_Returns_NoContent_On_Success()
    {
        var h = new Harness();

        // Month with existing entry E2
        var month = new FoodDiaryMonth
        {
            UserId = "u1",
            Year = 2025,
            Month = 10,
            Days = Enumerable.Range(1, 31).Select(d => new DiaryDay { Day = d, Meals = new Meals() }).ToList()
        };
        month.Days[5].Meals.Dinner.Add(new FoodEntry { Id = "E2", Name = "Pizza", Calories = 800 });

        h.SetupFindAsyncSingle(month);
        h.SetupReplaceSuccess();

        var ctrl = new FoodDiaryController(h.Service);

        var res = await ctrl.DeleteFoodEntry("u1", "E2", CancellationToken.None);

        Assert.IsType<NoContentResult>(res);
    }

    [Fact]
    public async Task DeleteFoodEntry_Returns_NotFound_On_Missing()
    {
        var h = new Harness();
        h.SetupFindAsyncSingle(new FoodDiaryMonth
        {
            UserId = "u1",
            Year = 2025,
            Month = 10,
            Days = Enumerable.Range(1, 31).Select(d => new DiaryDay { Day = d, Meals = new Meals() }).ToList()
        });

        var ctrl = new FoodDiaryController(h.Service);

        var res = await ctrl.DeleteFoodEntry("u1", "missing", CancellationToken.None);

        Assert.IsType<NotFoundObjectResult>(res);
    }
}
