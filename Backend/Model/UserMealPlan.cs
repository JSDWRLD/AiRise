using System.Text.Json.Serialization;
using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace AiRise.Models
{
    public class UserMealPlan
    {
        [BsonId]
        [BsonRepresentation(BsonType.ObjectId)]
        public string? Id { get; set; }

        [BsonElement("meal_plan_id")]
        [JsonPropertyName("meal_plan_id")]
        public string MealPlanId { get; set; } = string.Empty;

        [BsonElement("calories_per_day")]
        [JsonPropertyName("calories_per_day")]
        public int CaloriesPerDay { get; set; } = 0;
    }
}