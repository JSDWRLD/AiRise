using System.Text.Json.Serialization;
using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace AiRise.Models.FoodDiary
{
    public class FoodDiaryMonth
    {
        [BsonId]
        [BsonRepresentation(BsonType.ObjectId)]
        public string? Id { get; set; }

        [BsonElement("userId")]
        [JsonPropertyName("userId")]
        public string UserId { get; set; } = null!;

        [BsonElement("year")]
        [JsonPropertyName("year")]
        public int Year { get; set; }

        [BsonElement("month")]
        [JsonPropertyName("month")]
        public int Month { get; set; }

        [BsonElement("days")]
        [JsonPropertyName("days")]
        public List<DiaryDay> Days { get; set; } = new List<DiaryDay>(new DiaryDay[31]);
    }

    public class DiaryDay
    {
        [BsonElement("day")]
        [JsonPropertyName("day")]
        public int Day { get; set; }

        [BsonElement("totalCalories")]
        [JsonPropertyName("totalCalories")]
        public double TotalCalories { get; set; }

        [BsonElement("meals")]
        [JsonPropertyName("meals")]
        public Meals Meals { get; set; } = new Meals();
    }

    public class Meals
    {
        [BsonElement("breakfast")]
        [JsonPropertyName("breakfast")]
        public List<FoodEntry> Breakfast { get; set; } = new List<FoodEntry>();

        [BsonElement("lunch")]
        [JsonPropertyName("lunch")]
        public List<FoodEntry> Lunch { get; set; } = new List<FoodEntry>();

        [BsonElement("dinner")]
        [JsonPropertyName("dinner")]
        public List<FoodEntry> Dinner { get; set; } = new List<FoodEntry>();
    }

    public class FoodEntry
    {
        [BsonElement("id")]
        [JsonPropertyName("id")]
        public string? Id { get; set; }

        [BsonElement("name")]
        [JsonPropertyName("name")]
        public string Name { get; set; } = null!;

        [BsonElement("calories")]
        [JsonPropertyName("calories")]
        public double Calories { get; set; }

        [BsonElement("fats")]
        [JsonPropertyName("fats")]
        public double Fats { get; set; }

        [BsonElement("carbs")]
        [JsonPropertyName("carbs")]
        public double Carbs { get; set; }

        [BsonElement("proteins")]
        [JsonPropertyName("proteins")]
        public double Proteins { get; set; }
    }
}