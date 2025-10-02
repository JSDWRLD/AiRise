using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using AiRise.Models.FoodDiary;
using AiRise.Services;
using FluentAssertions;
using Moq;
using MongoDB.Driver;
using MongoDB.Bson;
using MongoDB.Bson.Serialization;
using Xunit;
using MongoDB.Driver.Linq;

public class FoodDiaryService_Tests
{
    // ---------- Helpers ----------

    private sealed class CollectionHarness
    {
        public Mock<IMongoCollection<FoodDiaryMonth>> Coll { get; }
        public FoodDiaryMonth? InsertedDoc { get; private set; }
        public FoodDiaryMonth? ReplacedDoc { get; private set; }

        public CollectionHarness(Mock<IMongoCollection<FoodDiaryMonth>> coll)
        {
            Coll = coll;
        }

        public void CaptureInsert(FoodDiaryMonth doc) => InsertedDoc = doc;
        public void CaptureReplace(FoodDiaryMonth doc) => ReplacedDoc = doc;
    }

    private static FoodEntry Entry(string id, string name, double cals, double f = 0, double carbs = 0, double p = 0) =>
        new FoodEntry { Id = id, Name = name, Calories = cals, Fats = f, Carbs = carbs, Proteins = p };

    private static DiaryDay Day(int day, Action<Meals> fill)
    {
        var meals = new Meals();
        fill(meals);
        var total = (meals.Breakfast?.Sum(e => e.Calories) ?? 0) +
                    (meals.Lunch?.Sum(e => e.Calories) ?? 0) +
                    (meals.Dinner?.Sum(e => e.Calories) ?? 0);
        return new DiaryDay { Day = day, Meals = meals, TotalCalories = total };
    }

    private static FoodDiaryMonth Month(string userId, int year, int month, params DiaryDay?[] days)
    {
        var m = new FoodDiaryMonth
        {
            Id = Guid.NewGuid().ToString(),
            UserId = userId,
            Year = year,
            Month = month,
            Days = new List<DiaryDay>(31)
        };
        // Fill all days with non-null DiaryDay objects
        for (int i = 0; i < 31; i++)
        {
            if (i < days.Length && days[i] != null)
            {
                m.Days.Add(days[i]!);
            }
            else
            {
                m.Days.Add(new DiaryDay { Day = i + 1, Meals = new Meals(), TotalCalories = 0 });
            }
        }
        return m;
    }

    // ---- FindAsync rendering helpers (so we can return different docs per Year/Month) ----

    private static (string? userId, int? year, int? month) ExtractFindKeys(FilterDefinition<FoodDiaryMonth> filter)
    {
        var registry = BsonSerializer.SerializerRegistry;
        var serializer = registry.GetSerializer<FoodDiaryMonth>();

        var args = new RenderArgs<FoodDiaryMonth>(serializer, registry);
        var doc = filter.Render(args); // BsonDocument

        string? uid = TryGetString(doc, "UserId");
        int? yr = TryGetInt(doc, "Year");
        int? mo = TryGetInt(doc, "Month");
        return (uid, yr, mo);

        static string? TryGetString(BsonDocument d, string key)
        {
            if (d.TryGetValue(key, out var v) && v.IsString) return v.AsString;

            // handle compound filters like {$and:[{UserId:"u1"}, {Year:2025}]}
            if (d.TryGetValue("$and", out var andVal) && andVal.IsBsonArray)
            {
                foreach (var el in andVal.AsBsonArray)
                {
                    if (el is BsonDocument bd)
                    {
                        var s = TryGetString(bd, key);
                        if (s != null) return s;
                    }
                }
            }
            return null;
        }

        static int? TryGetInt(BsonDocument d, string key)
        {
            if (d.TryGetValue(key, out var v))
            {
                if (v.IsInt32) return v.AsInt32;
                if (v.IsInt64) return (int)v.AsInt64;
                if (v.IsDouble) return (int)v.AsDouble;
            }

            if (d.TryGetValue("$and", out var andVal) && andVal.IsBsonArray)
            {
                foreach (var el in andVal.AsBsonArray)
                {
                    if (el is BsonDocument bd)
                    {
                        var n = TryGetInt(bd, key);
                        if (n.HasValue) return n;
                    }
                }
            }
            return null;
        }
    }

    private static Mock<IAsyncCursor<FoodDiaryMonth>> MakeCursor(IEnumerable<FoodDiaryMonth> items)
    {
        var list = (items ?? Enumerable.Empty<FoodDiaryMonth>()).ToList();
        var cursor = new Mock<IAsyncCursor<FoodDiaryMonth>>();
        bool yielded = false;

        cursor.SetupGet(c => c.Current).Returns(() => yielded ? list : Enumerable.Empty<FoodDiaryMonth>());

        cursor.SetupSequence(c => c.MoveNext(It.IsAny<CancellationToken>()))
              .Returns(() =>
              {
                  if (!yielded && list.Count > 0) { yielded = true; return true; }
                  return false;
              })
              .Returns(false);

        cursor.SetupSequence(c => c.MoveNextAsync(It.IsAny<CancellationToken>()))
              .ReturnsAsync(() =>
              {
                  if (!yielded && list.Count > 0) { yielded = true; return true; }
                  return false;
              })
              .ReturnsAsync(false);

        return cursor;
    }

    // Harness for codepaths that fetch exactly one doc (FirstOrDefault).
    private static CollectionHarness CreateHarness_ForSingleDocPath(FoodDiaryMonth? firstOrDefaultDoc)
    {
        var coll = new Mock<IMongoCollection<FoodDiaryMonth>>();

        // Mock FindAsync for any filter -> single doc (or none)
        coll
            .Setup(c => c.FindAsync(
                It.IsAny<FilterDefinition<FoodDiaryMonth>>(),
                It.IsAny<FindOptions<FoodDiaryMonth, FoodDiaryMonth>>(),
                It.IsAny<CancellationToken>()))
            .ReturnsAsync(() => MakeCursor(firstOrDefaultDoc is null ? Enumerable.Empty<FoodDiaryMonth>() : new[] { firstOrDefaultDoc }).Object);

        var harness = new CollectionHarness(coll);

        // InsertOneAsync
        coll.Setup(c => c.InsertOneAsync(
                It.IsAny<FoodDiaryMonth>(),
                It.IsAny<InsertOneOptions>(),
                It.IsAny<CancellationToken>()))
            .Callback<FoodDiaryMonth, InsertOneOptions, CancellationToken>((doc, _, __) => harness.CaptureInsert(doc))
            .Returns(Task.CompletedTask);

        // ReplaceOneAsync
        var replaceResult = new Mock<ReplaceOneResult>();
        replaceResult.SetupGet(r => r.IsAcknowledged).Returns(true);
        replaceResult.SetupGet(r => r.MatchedCount).Returns(1);
        replaceResult.SetupGet(r => r.ModifiedCount).Returns(1);

        coll.Setup(c => c.ReplaceOneAsync(
                It.IsAny<FilterDefinition<FoodDiaryMonth>>(),
                It.IsAny<FoodDiaryMonth>(),
                It.IsAny<ReplaceOptions>(),
                It.IsAny<CancellationToken>()))
            .Callback<FilterDefinition<FoodDiaryMonth>, FoodDiaryMonth, ReplaceOptions, CancellationToken>((_, doc, __, ___) => harness.CaptureReplace(doc))
            .ReturnsAsync(replaceResult.Object);

        return harness;
    }

    // Harness for codepaths that read many docs (scan across months or single-list query).
    private static CollectionHarness CreateHarness_ForListPath(IEnumerable<FoodDiaryMonth> listResults)
    {
        var all = (listResults ?? Enumerable.Empty<FoodDiaryMonth>()).ToList();
        var coll = new Mock<IMongoCollection<FoodDiaryMonth>>();

        // Smart FindAsync:
        // - If filter contains Year/Month -> return ONLY that matching doc (FirstOrDefault semantics)
        // - Else (e.g., a broad scan filter over user) -> return ALL listResults
        coll
            .Setup(c => c.FindAsync(
                It.IsAny<FilterDefinition<FoodDiaryMonth>>(),
                It.IsAny<FindOptions<FoodDiaryMonth, FoodDiaryMonth>>(),
                It.IsAny<CancellationToken>()))
            .Returns<FilterDefinition<FoodDiaryMonth>, FindOptions<FoodDiaryMonth, FoodDiaryMonth>, CancellationToken>((filter, _, __) =>
            {
                var (uid, yr, mo) = ExtractFindKeys(filter);

                IEnumerable<FoodDiaryMonth> result;
                if (yr.HasValue && mo.HasValue)
                {
                    result = all.Where(m =>
                        (uid == null || m.UserId == uid) &&
                        m.Year == yr.Value &&
                        m.Month == mo.Value
                    ).Take(1); // emulate FirstOrDefault query per month
                }
                else
                {
                    // Broad scan (e.g., list all months for a user / recent window)
                    result = all.Where(m => uid == null || m.UserId == uid).ToList();
                }

                return Task.FromResult(MakeCursor(result).Object);
            });

        var harness = new CollectionHarness(coll);

        // ReplaceOneAsync
        var replaceResult = new Mock<ReplaceOneResult>();
        replaceResult.SetupGet(r => r.IsAcknowledged).Returns(true);
        replaceResult.SetupGet(r => r.MatchedCount).Returns(1);
        replaceResult.SetupGet(r => r.ModifiedCount).Returns(1);

        coll.Setup(c => c.ReplaceOneAsync(
                It.IsAny<FilterDefinition<FoodDiaryMonth>>(),
                It.IsAny<FoodDiaryMonth>(),
                It.IsAny<ReplaceOptions>(),
                It.IsAny<CancellationToken>()))
            .Callback<FilterDefinition<FoodDiaryMonth>, FoodDiaryMonth, ReplaceOptions, CancellationToken>((_, doc, __, ___) => harness.CaptureReplace(doc))
            .ReturnsAsync(replaceResult.Object);

        return harness;
    }

    // ---------- Tests ----------

    [Fact]
    public async Task GetMonthAsync_Creates_New_When_NotFound()
    {
        var harness = CreateHarness_ForSingleDocPath(firstOrDefaultDoc: null);
        var svc = new FoodDiaryService(harness.Coll.Object);

        var now = DateTime.UtcNow;
        var doc = await svc.GetMonthAsync("u1", now.Year, now.Month);

        doc.UserId.Should().Be("u1");
        doc.Year.Should().Be(now.Year);
        doc.Month.Should().Be(now.Month);
        doc.Days.Should().NotBeNull();
        doc.Days.Count.Should().Be(31);

        harness.InsertedDoc.Should().NotBeNull();
        harness.ReplacedDoc.Should().BeNull();
    }

    [Fact]
    public async Task GetMonthAsync_Throws_When_Older_Than_One_Year()
    {
        var harness = CreateHarness_ForSingleDocPath(firstOrDefaultDoc: null);
        var svc = new FoodDiaryService(harness.Coll.Object);

        var tooOld = DateTime.UtcNow.AddYears(-2);
        Func<Task> act = () => svc.GetMonthAsync("u1", tooOld.Year, tooOld.Month);

        await act.Should().ThrowAsync<InvalidOperationException>()
                 .WithMessage("*older than 1 year*");

        harness.InsertedDoc.Should().BeNull();
        harness.ReplacedDoc.Should().BeNull();
    }

    [Fact]
    public async Task GetMonthAsync_Fixes_Shape_When_Existing_Doc_Is_Malformed()
    {
        var malformed = new FoodDiaryMonth
        {
            Id = "X",
            UserId = "u1",
            Year = DateTime.UtcNow.Year,
            Month = DateTime.UtcNow.Month,
            Days = null!
        };

        var harness = CreateHarness_ForSingleDocPath(malformed);
        var svc = new FoodDiaryService(harness.Coll.Object);

        var doc = await svc.GetMonthAsync("u1", malformed.Year, malformed.Month);

        doc.Days.Should().NotBeNull();
        doc.Days.Count.Should().Be(31);
        harness.InsertedDoc.Should().BeNull();
        harness.ReplacedDoc.Should().BeNull();
    }

    [Fact]
    public async Task AddFoodEntry_Assigns_Id_Updates_Total_And_Persists()
    {
        var existing = Month("u1", DateTime.UtcNow.Year, DateTime.UtcNow.Month); // all days null
        var harness = CreateHarness_ForSingleDocPath(existing);
        var svc = new FoodDiaryService(harness.Coll.Object);

        var entry = new FoodEntry { Id = null, Name = "Oats", Calories = 250 };
        await svc.AddFoodEntryAsync("u1", existing.Year, existing.Month, day: 1, meal: "breakfast", entry);

        harness.ReplacedDoc.Should().NotBeNull();
        var d1 = harness.ReplacedDoc!.Days[0];
        d1.Should().NotBeNull();
        d1.Meals.Breakfast.Should().HaveCount(1);
        d1.Meals.Breakfast[0].Name.Should().Be("Oats");
        d1.Meals.Breakfast[0].Id.Should().NotBeNullOrWhiteSpace();
        d1.TotalCalories.Should().Be(250);
    }

    [Fact]
    public async Task EditFoodEntry_ByPath_Preserves_Id_Recalculates_Total_And_Persists()
    {
        var eid = "e-123";
        var month = Month("u1", DateTime.UtcNow.Year, DateTime.UtcNow.Month,
            Day(1, meals =>
            {
                meals.Breakfast.Add(Entry(eid, "Toast", 120));
                meals.Breakfast.Add(Entry("e-zzz", "Coffee", 5));
            }));

        var harness = CreateHarness_ForSingleDocPath(month);
        var svc = new FoodDiaryService(harness.Coll.Object);

        var updated = new FoodEntry { Id = null, Name = "Toast w/ PB", Calories = 220 };
        await svc.EditFoodEntryAsync("u1", month.Year, month.Month, day: 1, meal: "breakfast", entryId: eid, updatedEntry: updated);

        harness.ReplacedDoc.Should().NotBeNull();
        var d1 = harness.ReplacedDoc!.Days[0];
        d1.Meals.Breakfast.Should().HaveCount(2);
        d1.Meals.Breakfast[0].Id.Should().Be(eid); // preserved
        d1.Meals.Breakfast[0].Name.Should().Be("Toast w/ PB");
        d1.Meals.Breakfast[0].Calories.Should().Be(220);
        d1.TotalCalories.Should().Be(220 + 5);
    }

    [Fact]
    public async Task DeleteFoodEntry_ByPath_Removes_And_Recalculates_Total()
    {
        var eid = "e-del";
        var month = Month("u1", DateTime.UtcNow.Year, DateTime.UtcNow.Month,
            Day(1, meals =>
            {
                meals.Lunch.Add(Entry(eid, "Burrito", 700));
                meals.Lunch.Add(Entry("keep", "Soda", 150));
            }));

        var harness = CreateHarness_ForSingleDocPath(month);
        var svc = new FoodDiaryService(harness.Coll.Object);

        await svc.DeleteFoodEntryAsync("u1", month.Year, month.Month, day: 1, meal: "lunch", entryId: eid);

        harness.ReplacedDoc.Should().NotBeNull();
        var d1 = harness.ReplacedDoc!.Days[0];
        d1.Meals.Lunch.Select(x => x.Id).Should().BeEquivalentTo(new[] { "keep" });
        d1.TotalCalories.Should().Be(150);
    }

    [Fact]
    public async Task EditFoodEntryById_Scans_Months_Replaces_And_Recalculates()
    {
        var baseDate = DateTime.UtcNow;
        var prev = baseDate.AddMonths(-1);
        var m1 = Month("u1", prev.Year, prev.Month, Day(3, meals => meals.Dinner.Add(Entry("nope", "Soup", 100))));
        var targetEntryId = "hit-me";
        var m2 = Month("u1", baseDate.Year, baseDate.Month,
            Day(1, meals => meals.Breakfast.Add(Entry("x", "Toast", 100))),
            Day(2, meals => meals.Lunch.Add(Entry(targetEntryId, "Burrito", 600)))); // target

        var harness = CreateHarness_ForListPath(new[] { m1, m2 });
        var svc = new FoodDiaryService(harness.Coll.Object);

        var updated = new FoodEntry { Id = null, Name = "Burrito (light)", Calories = 450 };

        await svc.EditFoodEntryByIdAsync("u1", targetEntryId, updated);

        harness.ReplacedDoc.Should().NotBeNull();
        harness.ReplacedDoc!.Id.Should().Be(m2.Id);
        var day2 = harness.ReplacedDoc.Days[1];
        day2.Meals.Lunch.Should().HaveCount(1);
        day2.Meals.Lunch[0].Id.Should().Be(targetEntryId);
        day2.Meals.Lunch[0].Name.Should().Be("Burrito (light)");
        day2.Meals.Lunch[0].Calories.Should().Be(450);
        day2.TotalCalories.Should().Be(450);
    }

    [Fact]
    public async Task DeleteFoodEntryById_Scans_Months_Deletes_And_Recalculates()
    {
        var baseDate = DateTime.UtcNow;
        var m1 = Month("u1", baseDate.Year, baseDate.Month,
            null, null, null, null,
            Day(5, meals => meals.Breakfast.Add(Entry("x", "Bagel", 300))));
        var targetEntryId = "to-del";
        var m2 = Month("u1", baseDate.Year, baseDate.Month,
            null, null, null, null, null,
            Day(6, meals =>
            {
                meals.Dinner.Add(Entry(targetEntryId, "Pizza", 800));
                meals.Dinner.Add(Entry("keep", "Salad", 120));
            }));

        var harness = CreateHarness_ForListPath(new[] { m1, m2 });
        var svc = new FoodDiaryService(harness.Coll.Object);

        // Assert setup is correct before deletion
        var preD6 = m2.Days[5];
        preD6.Should().NotBeNull();
        preD6.Meals.Dinner.Select(x => x.Id).Should().BeEquivalentTo(new[] { "to-del", "keep" });
        preD6.TotalCalories.Should().Be(920);

        await svc.DeleteFoodEntryByIdAsync("u1", targetEntryId);

        harness.ReplacedDoc.Should().NotBeNull();
        harness.ReplacedDoc!.Id.Should().Be(m2.Id);
        var d6 = harness.ReplacedDoc.Days[5];
        d6.Should().NotBeNull();
        d6.Meals.Dinner.Select(x => x.Id).Should().BeEquivalentTo(new[] { "keep" });
        d6.TotalCalories.Should().Be(120);
    }

    [Fact]
    public async Task EditFoodEntryById_Throws_When_Not_Found()
    {
        var baseDate = DateTime.UtcNow;
        var m = Month("u1", baseDate.Year, baseDate.Month,
            Day(1, meals => meals.Breakfast.Add(Entry("other", "Toast", 100))));

        var harness = CreateHarness_ForListPath(new[] { m });
        var svc = new FoodDiaryService(harness.Coll.Object);

        Func<Task> act = () => svc.EditFoodEntryByIdAsync("u1", "missing", new FoodEntry { Name = "x", Calories = 1 });

        await act.Should().ThrowAsync<KeyNotFoundException>()
                 .WithMessage("*Food entry not found*");
        harness.ReplacedDoc.Should().BeNull();
    }

    [Fact]
    public async Task DeleteFoodEntryById_Throws_When_Not_Found()
    {
        var baseDate = DateTime.UtcNow;
        var m = Month("u1", baseDate.Year, baseDate.Month,
            Day(1, meals => meals.Lunch.Add(Entry("keep", "Wrap", 400))));

        var harness = CreateHarness_ForListPath(new[] { m });
        var svc = new FoodDiaryService(harness.Coll.Object);

        Func<Task> act = () => svc.DeleteFoodEntryByIdAsync("u1", "missing");

        await act.Should().ThrowAsync<KeyNotFoundException>()
                 .WithMessage("*Food entry not found*");
        harness.ReplacedDoc.Should().BeNull();
    }

    [Fact]
    public async Task AddFoodEntry_Rejects_Invalid_Meal()
    {
        var existing = Month("u1", DateTime.UtcNow.Year, DateTime.UtcNow.Month);
        var harness = CreateHarness_ForSingleDocPath(existing);
        var svc = new FoodDiaryService(harness.Coll.Object);

        Func<Task> act = () => svc.AddFoodEntryAsync("u1", existing.Year, existing.Month, 1, "brunch", new FoodEntry { Name = "??", Calories = 1 });

        await act.Should().ThrowAsync<ArgumentException>()
                 .WithMessage("*Invalid meal type*");
        harness.ReplacedDoc.Should().BeNull();
    }
}
