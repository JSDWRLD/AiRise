using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;
using System.Text.Json.Serialization;

namespace AiRise.Models
{
    public class User
    {
        [BsonId]
        [BsonRepresentation(BsonType.ObjectId)]
        public string? Id { get; set; }

        [BsonElement("firebaseUid")]
        [JsonPropertyName("firebaseUid")]
        public string FirebaseUid { get; set; } = null!;

        [BsonElement("streak")]
        [JsonPropertyName("streak")]
        public int Streak { get; set; } = 0;

        // Store references to other collection objects via their id
        [BsonElement("user_data_ref")]
        [JsonPropertyName("user_data_ref")]
        public string? UserData { get; set; } = null;

        [BsonElement("user_settings_ref")]
        [JsonPropertyName("user_settings_ref")]
        public string? UserSettings { get; set; } = null;

        [BsonElement("goals_ref")]
        [JsonPropertyName("goals_ref")]
        public string? Goals { get; set; } = null;

        [BsonElement("workouts_ref")]
        [JsonPropertyName("workouts_ref")]
        public string? Workouts { get; set; } = null;

        [BsonElement("meal_plan_ref")]
        [JsonPropertyName("meal_plan_ref")]
        public string? MealPlan { get; set; } = null;

        [BsonElement("progress_ref")]
        [JsonPropertyName("progress_ref")]
        public string? Progress { get; set; } = null;

        [BsonElement("challenges_ref")]
        [JsonPropertyName("challenges_ref")]
        public string? Challenges { get; set; } = null;

        [BsonElement("health_data_ref")]
        [JsonPropertyName("health_data_ref")]
        public string? HealthData { get; set; } = null;

        [BsonElement("chat_history_ref")]
        [JsonPropertyName("chat_history_ref")]
        public string? ChatHistory { get; set; } = null;
    }
}