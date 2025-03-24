using System.Text.Json.Serialization;
using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace AiRise.Models
{
    public class UserFriends
    {
        [BsonId]
        [BsonRepresentation(BsonType.ObjectId)]
        public string? Id { get; set; }

        [BsonElement("friend_ids")]
        [JsonPropertyName("friend_ids")]
        public List<string> FriendIds { get; set; } = new List<string>();

        [BsonElement("friend_rankings")]
        [JsonPropertyName("friend_rankings")]
        public List<string> FriendRankings { get; set; } = new List<string>();
    }
}