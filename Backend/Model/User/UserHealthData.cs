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

        // Populate with json array from client side app
        [BsonElement("health_data_id")]
        [JsonPropertyName("health_data_id")]
        public string HealthDataId { get; set; } = string.Empty;

        [BsonElement("calories")]
        [JsonPropertyName("calories")]
        public int Calories { get; set; } = 0;

        [BsonElement("steps")]
        [JsonPropertyName("steps")]
        public int Steps { get; set; } = 0;

        [BsonElement("sleep")]
        [JsonPropertyName("sleep")]
        public int Sleep { get; set; } = 0;
    }
}