using System.Text.Json.Serialization;
using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace AiRise.Models
{
    public class UserGoals
    {
        [BsonId]
        [BsonRepresentation(BsonType.ObjectId)]
        public string? Id { get; set; }

        [BsonElement("calories")]
        [JsonPropertyName("calories")]
        public int Calories { get; set; } = 0;

        [BsonElement("steps")]
        [JsonPropertyName("steps")]
        public int Steps { get; set; } = 0;

        [BsonElement("weight")]
        [JsonPropertyName("weight")]
        public int Weight { get; set; } = 0;
    }
}