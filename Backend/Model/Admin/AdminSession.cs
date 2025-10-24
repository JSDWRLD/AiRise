using System.Text.Json.Serialization;
using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace AiRise.Models.Admin
{
    public class AdminSession
    {
        [BsonElement("firebaseUid")]
        [JsonPropertyName("firebaseUid")]
        public string FirebaseUid { get; set; } = null!;

        [BsonElement("issuedAt")]
        [JsonPropertyName("issuedAt")]
        public DateTime IssuedAt { get; set; } = DateTime.UtcNow;

        // Session expires in 10 minutes
        [BsonElement("expiresAt")]
        [JsonPropertyName("expiresAt")]
        public DateTime ExpiresAt { get; set; } = DateTime.UtcNow.AddMinutes(10); 
    }
}