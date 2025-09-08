using System.Text.Json.Serialization;
using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace AiRise.Models.User
{
    public class UserData
    {
        [BsonId]
        [BsonRepresentation(BsonType.ObjectId)]
        public string? Id { get; set; }

        [BsonElement("firebaseUid")]
        [JsonPropertyName("firebaseUid")]
        public string FirebaseUid { get; set; } = null!;

        [BsonElement("firstName")]
        [JsonPropertyName("firstName")]
        public string FirstName { get; set; } = string.Empty;

        [BsonElement("lastName")]
        [JsonPropertyName("lastName")]
        public string LastName { get; set; } = string.Empty;

        [BsonElement("middleName")]
        [JsonPropertyName("middleName")]
        public string MiddleName { get; set; } = string.Empty;
        
        [BsonElement("fullName")]
        [JsonPropertyName("fullName")]
        public string FullName { get; set; } = string.Empty;

        [BsonElement("workoutGoal")]
        [JsonPropertyName("workoutGoal")]
        public string WorkoutGoal { get; set; } = string.Empty;

        [BsonElement("fitnessLevel")]
        [JsonPropertyName("fitnessLevel")]
        public string FitnessLevel { get; set; } = string.Empty;

        [BsonElement("workoutLength")]
        [JsonPropertyName("workoutLength")]
        public int WorkoutLength { get; set; }

        [BsonElement("workoutEquipment")]
        [JsonPropertyName("workoutEquipment")]
        public string WorkoutEquipment { get; set; } = string.Empty;

        // array of day names
        [BsonElement("workoutDays")]
        [JsonPropertyName("workoutDays")]
        public List<string> WorkoutDays { get; set; } = new List<string>();

        [BsonElement("workoutTime")]
        [JsonPropertyName("workoutTime")]
        public string WorkoutTime { get; set; } = string.Empty;

        [BsonElement("dietaryGoal")]
        [JsonPropertyName("dietaryGoal")]
        public string DietaryGoal { get; set; } = string.Empty;

        [BsonElement("workoutRestrictions")]
        [JsonPropertyName("workoutRestrictions")]
        public string WorkoutRestrictions { get; set; } = string.Empty;

        [BsonElement("heightMetric")]
        [JsonPropertyName("heightMetric")]
        public bool HeightMetric { get; set; }

        [BsonElement("heightValue")]
        [JsonPropertyName("heightValue")]
        public int HeightValue { get; set; }

        [BsonElement("weightMetric")]
        [JsonPropertyName("weightMetric")]
        public bool WeightMetric { get; set; }

        [BsonElement("weightValue")]
        [JsonPropertyName("weightValue")]
        public int WeightValue { get; set; }

        [BsonElement("dobDay")]
        [JsonPropertyName("dobDay")]
        public int DobDay { get; set; }

        [BsonElement("dobMonth")]
        [JsonPropertyName("dobMonth")]
        public int DobMonth { get; set; }

        [BsonElement("dobYear")]
        [JsonPropertyName("dobYear")]
        public int DobYear { get; set; }

        [BsonElement("activityLevel")]
        [JsonPropertyName("activityLevel")]
        public string ActivityLevel { get; set; } = string.Empty;
    }
}