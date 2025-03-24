using System.Text.Json.Serialization;
using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace AiRise.Models
{
    public class UserChallenges
    {
        [BsonId]
        [BsonRepresentation(BsonType.ObjectId)]
        public string? Id { get; set; }

        [BsonElement("user_challenge_id")]
        [JsonPropertyName("user_challenge_id")]
        public string ChallengeId { get; set; } = string.Empty;

        [BsonElement("assigned_by")]
        [JsonPropertyName("assigned_by")]
        public string AssignedById { get; set; } = string.Empty;

        [BsonElement("assigned_on")]
        [JsonPropertyName("assigned_on")]
        public DateTime? AssignedOn { get; set; } = null;

        [BsonElement("complete_by")]
        [JsonPropertyName("complete_by")]
        public DateTime? CompleteBy { get; set; } = null;

        // ID of most recently completed activity
        [BsonElement("completed_activity_id")]
        [JsonPropertyName("completed_activity_id")]
        public string CompletedActivityId { get; set; } = string.Empty;
    }
}