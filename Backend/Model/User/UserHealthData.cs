using System.Text.Json.Serialization;
using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace AiRise.Models.User
{
    public class UserHealthData
    {
        [BsonId]
        [BsonRepresentation(BsonType.ObjectId)]
        public string? Id { get; set; }

        [BsonElement("firebaseUid")]
        [JsonPropertyName("firebaseUid")]
        public string FirebaseUid { get; set; } = null!;

        // Populate with json array from client side app
        [BsonElement("health_data_id")]
        [JsonPropertyName("health_data_id")]
        public string HealthDataId { get; set; } = string.Empty;

        [BsonElement("caloriesBurned")]
        [JsonPropertyName("caloriesBurned")]
        public int CaloriesBurned { get; set; } = 0;

        [BsonElement("caloriesEaten")]
        [JsonPropertyName("caloriesEaten")]
        public int CaloriesEaten { get; set; } = 0;

        [BsonElement("caloriesTarget")]
        [JsonPropertyName("caloriesTarget")]
        public int CaloriesTarget { get; set; } = 2000;

        [BsonElement("steps")]
        [JsonPropertyName("steps")]
        public int Steps { get; set; } = 0;

        [BsonElement("sleep")]
        [JsonPropertyName("sleep")]
        public double Sleep { get; set; } = 0;

        [BsonElement("hydration")]
        [JsonPropertyName("hydration")]
        public double Hydration { get; set; } = 0;

        [BsonElement("hydrationTarget")]
        [JsonPropertyName("hydrationTarget")]
        public int HydrationTarget { get; set; } = 104; // ounces
        [BsonElement("lastUpdatedAt")]
        [JsonPropertyName("lastUpdatedAt")]
        public DateTime LastUpdatedAt { get; set; } = DateTime.UtcNow;
    }
}