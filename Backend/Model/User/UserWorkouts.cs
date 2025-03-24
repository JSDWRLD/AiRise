using System.Text.Json.Serialization;
using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace AiRise.Models.User
{
    public class UserWorkouts
    {
        [BsonId]
        [BsonRepresentation(BsonType.ObjectId)]
        public string? Id { get; set; }

        [BsonElement("firebaseUid")]
        [JsonPropertyName("firebaseUid")]
        public string FirebaseUid { get; set; } = null!;

        [BsonElement("selected_workout_id")]
        [JsonPropertyName("selected_workout_id")]
        public string SelectedWorkoutId { get; set; } = string.Empty;

        [BsonElement("saved_workout_ids")]
        [JsonPropertyName("saved_workout_ids")]
        public List<string> SavedWorkouts { get; set; } = new List<string>();
    }
}