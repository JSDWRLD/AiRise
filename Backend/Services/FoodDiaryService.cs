using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using AiRise.Models.DTOs;
using AiRise.Models.FoodDiary;
using AiRise.Models.User;
using MongoDB.Driver;
using Swashbuckle.AspNetCore.SwaggerGen;

namespace AiRise.Services
{
    public class FoodDiaryService
    {
        private readonly IMongoCollection<FoodDiaryMonth> _foodDiaryCollection;
        private readonly IMongoCollection<UserHealthData> _userHealthDataCollection;
        private readonly IUserHealthDataService _userHealthDataService;

        // Production ctor (creates unique index on UserId+Year+Month)
        public FoodDiaryService(MongoDBService mongoDBService, IUserHealthDataService userHealthDataService)
        {
            _foodDiaryCollection = mongoDBService.GetCollection<FoodDiaryMonth>("food.diary");
            _userHealthDataCollection = mongoDBService.GetCollection<UserHealthData>("user.healthdata");
            _userHealthDataService = userHealthDataService;

            var keys = Builders<FoodDiaryMonth>.IndexKeys
                .Ascending(x => x.UserId)
                .Ascending(x => x.Year)
                .Ascending(x => x.Month);

            _foodDiaryCollection.Indexes.CreateOne(
                new CreateIndexModel<FoodDiaryMonth>(
                    keys,
                    new CreateIndexOptions { Unique = true }
                ));
        }

        // Unit-test ctor
        public FoodDiaryService(IMongoCollection<FoodDiaryMonth> collection,
            IMongoCollection<UserHealthData>? userHealthDataCollection = null,
            IUserHealthDataService? userHealthDataService = null
        )
        {
            _foodDiaryCollection = collection;
            _userHealthDataCollection = userHealthDataCollection;
            _userHealthDataService = userHealthDataService;

        }

        // READ (with 1-year lookback rule). Creates the month if not found.
        public async Task<FoodDiaryMonth> GetMonthAsync(string userId, int year, int month, CancellationToken ct = default)
        {
            ValidateYearMonthDay(year, month, 1, validateDay: false);

            // Policy: block access older than 1 year
            var currentDate = DateTime.UtcNow;
            var requestedDate = new DateTime(year, month, 1);
            if (requestedDate < currentDate.AddYears(-1))
                throw new InvalidOperationException("Cannot access diary entries older than 1 year.");

            var doc = await _foodDiaryCollection
                .Find(x => x.UserId == userId && x.Year == year && x.Month == month)
                .FirstOrDefaultAsync(ct);

            if (doc == null)
            {
                doc = new FoodDiaryMonth
                {
                    UserId = userId,
                    Year = year,
                    Month = month,
                    Days = NewMonthDays()
                };
                await _foodDiaryCollection.InsertOneAsync(doc, new InsertOneOptions(), ct);
                return doc;
            }

            // Sanitize existing doc shape
            EnsureMonthShape(doc);
            return doc;
        }

        // ADD
        public async Task AddFoodEntryAsync(
            string userId, int year, int month, int day, string meal, FoodEntry entry, CancellationToken ct = default)
        {
            ValidateYearMonthDay(year, month, day);
            var monthDoc = await GetMonthAsync(userId, year, month, ct);

            var dayDoc = EnsureDay(monthDoc, day);

            // which meal?
            var mealList = GetMealList(dayDoc.Meals, meal);

            entry.Id = Guid.NewGuid().ToString();

            // ensure lists exist (defensive)
            EnsureMealLists(dayDoc.Meals);

            mealList.Add(entry);
            dayDoc.TotalCalories = CalculateTotalCalories(dayDoc.Meals);

            await _foodDiaryCollection.ReplaceOneAsync(
                x => x.UserId == userId && x.Year == year && x.Month == month,
                monthDoc,
                new ReplaceOptions { IsUpsert = true },
                ct);
            // sync the calories with Health Data (only if present local date for user)
            await syncUserHealthDataCaloriesAsync(userId, year, month, day, (int)dayDoc.TotalCalories);
        }

        // EDIT (by explicit y/m/d/meal/entryId)
        public async Task EditFoodEntryAsync(
            string userId, int year, int month, int day, string meal, string entryId, FoodEntry updatedEntry, CancellationToken ct = default)
        {
            ValidateYearMonthDay(year, month, day);
            var monthDoc = await GetMonthAsync(userId, year, month, ct);

            var dayDoc = monthDoc.Days[day - 1];
            if (dayDoc == null)
                throw new KeyNotFoundException("Day not found.");

            EnsureMealLists(dayDoc.Meals);
            var mealList = GetMealList(dayDoc.Meals, meal);

            var idx = mealList.FindIndex(e => e.Id == entryId);
            if (idx == -1)
                throw new KeyNotFoundException("Food entry not found.");

            // preserve id
            updatedEntry.Id = entryId;
            mealList[idx] = updatedEntry;

            dayDoc.TotalCalories = CalculateTotalCalories(dayDoc.Meals);

            await _foodDiaryCollection.ReplaceOneAsync(
                x => x.UserId == userId && x.Year == year && x.Month == month,
                monthDoc,
                new ReplaceOptions { IsUpsert = true },
                ct);
            // sync the calories with Health Data (only if present local date for user)
            await syncUserHealthDataCaloriesAsync(userId, year, month, day, (int)dayDoc.TotalCalories);
        }

        // DELETE (by explicit y/m/d/meal/entryId)
        public async Task DeleteFoodEntryAsync(
            string userId, int year, int month, int day, string meal, string entryId, CancellationToken ct = default)
        {
            ValidateYearMonthDay(year, month, day);
            var monthDoc = await GetMonthAsync(userId, year, month, ct);

            var dayDoc = monthDoc.Days[day - 1];
            if (dayDoc == null)
                throw new KeyNotFoundException("Day not found.");

            EnsureMealLists(dayDoc.Meals);
            var mealList = GetMealList(dayDoc.Meals, meal);

            var idx = mealList.FindIndex(e => e.Id == entryId);
            if (idx == -1)
                throw new KeyNotFoundException("Food entry not found.");

            mealList.RemoveAt(idx);
            dayDoc.TotalCalories = CalculateTotalCalories(dayDoc.Meals);

            await _foodDiaryCollection.ReplaceOneAsync(
                x => x.UserId == userId && x.Year == year && x.Month == month,
                monthDoc,
                new ReplaceOptions { IsUpsert = true },
                ct);
            // sync the calories with Health Data (only if present local date for user)
            await syncUserHealthDataCaloriesAsync(userId, year, month, day, (int)dayDoc.TotalCalories);
        }

        // EDIT (by entryId only — scans user's months/days/meals)
        public async Task EditFoodEntryByIdAsync(string userId, string entryId, FoodEntry updatedEntry, CancellationToken ct = default)
        {
            var monthDocs = await _foodDiaryCollection.Find(x => x.UserId == userId).ToListAsync(ct);

            foreach (var monthDoc in monthDocs)
            {
                EnsureMonthShape(monthDoc);

                foreach (var day in monthDoc.Days)
                {
                    if (day == null) continue;

                    EnsureMealLists(day.Meals);

                    foreach (var list in EnumerateMealLists(day.Meals))
                    {
                        var idx = list.FindIndex(e => e.Id == entryId);
                        if (idx == -1) continue;

                        updatedEntry.Id = entryId; // preserve id
                        list[idx] = updatedEntry;
                        day.TotalCalories = CalculateTotalCalories(day.Meals);

                        await _foodDiaryCollection.ReplaceOneAsync(
                            x => x.Id == monthDoc.Id,
                            monthDoc,
                            new ReplaceOptions { IsUpsert = true },
                            ct);
                        // Sync total calories with UserHealthData (if present local date for user)
                        await syncUserHealthDataCaloriesAsync(
                            userId,
                            monthDoc.Year,
                            monthDoc.Month,
                            day.Day,
                            (int)day.TotalCalories
                        );
                        return;
                    }
                }
            }

            throw new KeyNotFoundException("Food entry not found.");
        }

        // DELETE (by entryId only — scans user's months/days/meals)
        public async Task DeleteFoodEntryByIdAsync(string userId, string entryId, CancellationToken ct = default)
        {
            var monthDocs = await _foodDiaryCollection.Find(x => x.UserId == userId).ToListAsync(ct);

            foreach (var monthDoc in monthDocs)
            {
                EnsureMonthShape(monthDoc);

                foreach (var day in monthDoc.Days)
                {
                    if (day == null) continue;

                    EnsureMealLists(day.Meals);

                    foreach (var list in EnumerateMealLists(day.Meals))
                    {
                        var idx = list.FindIndex(e => e.Id == entryId);
                        if (idx == -1) continue;

                        list.RemoveAt(idx);
                        day.TotalCalories = CalculateTotalCalories(day.Meals);

                        await _foodDiaryCollection.ReplaceOneAsync(
                            x => x.Id == monthDoc.Id,
                            monthDoc,
                            new ReplaceOptions { IsUpsert = true },
                            ct);
                        // Sync total calories with UserHealthData (if present local date for user)
                        await syncUserHealthDataCaloriesAsync(
                            userId,
                            monthDoc.Year,
                            monthDoc.Month,
                            day.Day,
                            (int)day.TotalCalories
                        );
                        return;
                    }
                }
            }

            throw new KeyNotFoundException("Food entry not found.");
        }

        // ---------- helpers ----------

        private static List<DiaryDay> NewMonthDays()
        {
            // 31 null slots (nullable reference type)
            return new List<DiaryDay>(new DiaryDay[31]);
        }

        private static void EnsureMonthShape(FoodDiaryMonth doc)
        {
            if (doc.Days == null || doc.Days.Count != 31)
                doc.Days = NewMonthDays();

            // Defensive: ensure each non-null day has Meals and non-null lists
            for (int i = 0; i < doc.Days.Count; i++)
            {
                var d = doc.Days[i];
                if (d == null) continue;

                if (d.Meals == null) d.Meals = new Meals();
                EnsureMealLists(d.Meals);
            }
        }

        private static DiaryDay EnsureDay(FoodDiaryMonth doc, int day)
        {
            EnsureMonthShape(doc);
            var idx = day - 1;
            if (doc.Days[idx] == null)
                doc.Days[idx] = new DiaryDay { Day = day, Meals = new Meals(), TotalCalories = 0 };

            // Also ensure inner lists exist
            EnsureMealLists(doc.Days[idx]!.Meals);
            return doc.Days[idx]!;
        }

        private static void EnsureMealLists(Meals meals)
        {
            meals.Breakfast ??= new List<FoodEntry>();
            meals.Lunch ??= new List<FoodEntry>();
            meals.Dinner ??= new List<FoodEntry>();
        }

        private static IEnumerable<List<FoodEntry>> EnumerateMealLists(Meals meals)
        {
            yield return meals.Breakfast ??= new List<FoodEntry>();
            yield return meals.Lunch ??= new List<FoodEntry>();
            yield return meals.Dinner ??= new List<FoodEntry>();
        }

        private static List<FoodEntry> GetMealList(Meals meals, string meal) =>
            (meal ?? string.Empty).Trim().ToLowerInvariant() switch
            {
                "breakfast" => meals.Breakfast ??= new List<FoodEntry>(),
                "lunch"     => meals.Lunch ??= new List<FoodEntry>(),
                "dinner"    => meals.Dinner ??= new List<FoodEntry>(),
                _           => throw new ArgumentException("Invalid meal type.")
            };

        private static void ValidateYearMonthDay(int year, int month, int day, bool validateDay = true)
        {
            if (month < 1 || month > 12)
                throw new ArgumentOutOfRangeException(nameof(month), "Month must be 1–12.");

            if (validateDay)
            {
                var daysInMonth = DateTime.DaysInMonth(year, month);
                if (day < 1 || day > daysInMonth)
                    throw new ArgumentOutOfRangeException(nameof(day), $"Day must be 1–{daysInMonth} for {month}/{year}.");
            }
        }

        private static double CalculateTotalCalories(Meals meals)
        {
            EnsureMealLists(meals);
            double total =
                (meals.Breakfast?.Sum(e => e.Calories) ?? 0) +
                (meals.Lunch?.Sum(e => e.Calories) ?? 0) +
                (meals.Dinner?.Sum(e => e.Calories) ?? 0);
            return total;
        }

        // sync the total eaten calories with the CaloriesEaten field in HealthData
        // only if the date matches the user's HealthData LocalDate field
        private async Task syncUserHealthDataCaloriesAsync(string userId, int year, int month, int day, int calories)
        {
            if (_userHealthDataService == null)
                return;

            // Get-or-create the user's health data (prevents nulls/404s)
            var userHealthData = await _userHealthDataService.GetUserHealthDataAsync(userId);
            if (userHealthData == null)
                return; // extremely defensive; Get should have created one

            var entryDate = new DateOnly(year, month, day);

            // Only sync when the diary entry date is the same as the user's tracked LocalDate
            if (entryDate.Equals(userHealthData.LocalDate))
            {
                var hd = new HealthData
                {
                    CaloriesEaten = calories,
                    LocalDate = userHealthData.LocalDate
                };

                await _userHealthDataService.UpdateUserHealthDataAsync(userHealthData.FirebaseUid, hd);
            }
        }
    }
}
