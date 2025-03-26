using System.Text.Json.Serialization;
using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace AiRise.Models.User
{
    public class UserChatHistory
    {
        [BsonId]
        [BsonRepresentation(BsonType.ObjectId)]
        public string? Id { get; set; }

        [BsonElement("firebaseUid")]
        [JsonPropertyName("firebaseUid")]
        public string FirebaseUid { get; set; } = null!;

        [BsonElement("message_history")]
        [JsonPropertyName("message_history")]
        public List<ChatMessage> MessageHistory { get; set; } = new List<ChatMessage>();
    }

    public class ChatMessage
    {
        [BsonElement("sender")]
        [JsonPropertyName("sender")]
        public string Sender { get; set; } = string.Empty;

        [BsonElement("message")]
        [JsonPropertyName("message")]
        public string Message { get; set; } = string.Empty;

        [BsonElement("timestamp")]
        [JsonPropertyName("timestamp")]
        public DateTime Timestamp { get; set; } = DateTime.UtcNow;
    }
}