public class ProgramPreferences
{
    public string? WorkoutGoal { get; set; }
    public int WorkoutLength { get; set; }
}


/// Centralized tuning values for personalization of workout template
public sealed class PersonalizationTuning
{
    // Expected seconds per rep (avg tempo across concentric/eccentric)
    public double SecondsPerRep { get; set; } = 3.0;

    // Base rest windows by goal (seconds)
    public int RestLoseWeightSec { get; set; } = 60;
    public int RestGainMuscleSec { get; set; } = 90;
    public int RestMaintainSec { get; set; } = 75;

    // Bounds on per-exercise set changes
    public int MinSets { get; set; } = 2;
    public int MaxSetsBump { get; set; } = 2; // at most +2 over template

    // For time-based entries like "30 sec", allow mild scale
    public double TimeSetScaleMin { get; set; } = 0.9;
    public double TimeSetScaleMax { get; set; } = 1.25;

    public static PersonalizationTuning Default => new PersonalizationTuning();
}
