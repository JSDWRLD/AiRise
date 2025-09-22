
namespace AiRise.Models
{
    public enum ProgramType
    {
        Bodyweight,
        HomeDumbbell, // "home (dumbbell only)"
        Gym
    }

    public sealed class ExerciseWeight
    {
        public int Value { get; set; }            // default user-entered weight value
        public string Unit { get; set; } = "lbs"; // keep "lbs" for consistency
    }

    public sealed class ExerciseEntry
    {
        public string Name { get; set; } = "";
        public int Sets { get; set; }
        public string Reps { get; set; } = "";        // target reps (can be "8-10", "AMRAP", "30 sec", etc.)
        public ExerciseWeight Weight { get; set; } = new ExerciseWeight { Value = 0, Unit = "lbs" };

        // Initialize with zero
        public int RepsCompleted { get; set; } = 0;
    }

    public sealed class ProgramDay
    {
        public int Day { get; set; }                  // 1-based index; your backend will assign chronological dates
        public string Focus { get; set; } = "";       // e.g., "Upper Push", "Lower", "Full Body"
        public List<ExerciseEntry> Exercises { get; set; } = new List<ExerciseEntry>();
    }

    public sealed class ProgramTemplate
    {
        public string Name { get; set; } = "";
        public int Days { get; set; }                 // 3–6
        public ProgramType Type { get; set; }         // Bodyweight | HomeDumbbell | Gym
        public List<ProgramDay> Schedule { get; set; } = new List<ProgramDay>();
    }
}