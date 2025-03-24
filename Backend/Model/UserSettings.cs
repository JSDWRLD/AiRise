using System.Text.Json.Serialization;
using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace AiRise.Models
{
    public class UserSettings
    {
        [BsonId]
        [BsonRepresentation(BsonType.ObjectId)]
        public string? Id { get; set; }

        [BsonElement("profile_picture_url")]
        [JsonPropertyName("profile_picture_url")]
        public string PictureUrl { get; set; } = string.Empty;

        [BsonElement("ai_personality")]
        [JsonPropertyName("ai_personality")]
        public string AiPersonality { get; set; } = string.Empty;

        [BsonElement("challenge_notifs_enabled")]
        [JsonPropertyName("challenge_notifs_enabled")]
        public bool ChallengeNotifsEnabled { get; set; } = false;

        [BsonElement("friend_req_notifs_enabled")]
        [JsonPropertyName("friend_req_notifs_enabled")]
        public bool FriendReqNotifsEnabled { get; set; } = false;

        [BsonElement("streak_notifs_enabled")]
        [JsonPropertyName("streak_notifs_enabled")]
        public bool StreakNotifsEnabled { get; set; } = false;

        [BsonElement("meal_notifs_enabled")]
        [JsonPropertyName("meal_notifs_enabled")]
        public bool MealNotifsEnabled { get; set; } = false;
    }
}