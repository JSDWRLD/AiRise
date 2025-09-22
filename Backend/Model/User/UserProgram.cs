using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace AiRise.Models.User
{
    // ====== USER-MUTABLE Version ======
    public sealed class UserExerciseWeight
    {
        [BsonElement("value")]
        public int Value { get; set; } = 0; // users mutate this

        [BsonElement("unit")]
        public string Unit { get; set; } = "lbs";
    }

    public sealed class UserExerciseEntry
    {
        [BsonElement("name")]
        public string Name { get; set; } = string.Empty;

        [BsonElement("sets")]
        public int Sets { get; set; } = 0;

        // Target reps from the template (e.g., "8-12", "AMRAP", "30-60 sec")
        [BsonElement("targetReps")]
        public string TargetReps { get; set; } = string.Empty;

        // Single integer the user can mutate for "what I actually did" (NOT an array)
        [BsonElement("repsCompleted")]
        public int RepsCompleted { get; set; } = 0;

        [BsonElement("weight")]
        public UserExerciseWeight Weight { get; set; } = new UserExerciseWeight();
    }

    public sealed class UserProgramDay
    {
        // 1..N from the template
        [BsonElement("dayIndex")]
        public int DayIndex { get; set; }

        // Renamed label from WorkoutDays (e.g., "monday")
        [BsonElement("dayName")]
        public string DayName { get; set; } = string.Empty;

        [BsonElement("focus")]
        public string Focus { get; set; } = string.Empty;

        [BsonElement("exercises")]
        public List<UserExerciseEntry> Exercises { get; set; } = new List<UserExerciseEntry>();
    }

    public sealed class UserProgram
    {
        [BsonElement("templateName")]
        public string TemplateName { get; set; } = string.Empty;

        [BsonElement("days")]
        public int Days { get; set; }

        [BsonElement("type")]
        public ProgramType Type { get; set; }

        [BsonElement("schedule")]
        public List<UserProgramDay> Schedule { get; set; } = new List<UserProgramDay>();

        [BsonElement("createdAtUtc")]
        public DateTime CreatedAtUtc { get; set; } = DateTime.UtcNow;

        [BsonElement("updatedAtUtc")]
        public DateTime UpdatedAtUtc { get; set; } = DateTime.UtcNow;
    }

    // One document per user; only one active program
    public sealed class UserProgramDoc
    {
        [BsonId]
        [BsonRepresentation(BsonType.ObjectId)]
        public string? Id { get; set; }

        [BsonElement("firebaseUid")]
        public string FirebaseUid { get; set; } = null!;

        [BsonElement("program")]
        public UserProgram Program { get; set; } = new UserProgram();

        [BsonElement("lastUpdatedUtc")]
        public DateTime LastUpdatedUtc { get; set; } = DateTime.UtcNow;
    }
}
