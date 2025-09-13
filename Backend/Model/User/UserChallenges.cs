using System.Text.Json.Serialization;
using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace AiRise.Models.User
{
    public class UserChallenges
    {
        [BsonId]
        [BsonRepresentation(BsonType.ObjectId)]
        public string? Id { get; set; }

        [BsonElement("firebaseUid")]
        [JsonPropertyName("firebaseUid")]
        public string FirebaseUid { get; set; } = null!;

        [BsonElement("activeChallengeId")]
        [JsonPropertyName("activeChallengeId")]
        public string? ActiveChallengeId { get; set; }

        // Epoch day (UTC) of the last completion (nullable if never completed)
        [BsonElement("lastCompletionEpochDay")]
        [JsonPropertyName("lastCompletionEpochDay")]
        public long? LastCompletionEpochDay { get; set; }
    }
}