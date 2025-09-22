using AiRise.Models;
using System.Collections.Generic;

namespace AiRise.Data
{
    // Sorted by Days (asc), then Type (Bodyweight, HomeDumbbell, Gym), then Name.
    public static class ProgramTemplatesData
    {
        public static readonly List<ProgramTemplate> Programs = new List<ProgramTemplate>
        {
            // ===== 3-DAY PROGRAMS =====
            new ProgramTemplate
            {
                Name = "3-Day Bodyweight Basics",
                Days = 3,
                Type = ProgramType.Bodyweight,
                Schedule = new List<ProgramDay>
                {
                    new ProgramDay
                    {
                        Day = 1,
                        Focus = "Upper Body Push (BW)",
                        Exercises = new List<ExerciseEntry>
                        {
                            new ExerciseEntry { Name = "Push-Ups", Sets = 4, Reps = "8-15", Weight = new ExerciseWeight{ Value = 0, Unit = "lbs" }, RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Pike Push-Ups", Sets = 3, Reps = "6-10", Weight = new ExerciseWeight{ Value = 0, Unit = "lbs" }, RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Bench Dips (Feet on Floor)", Sets = 3, Reps = "10-15", Weight = new ExerciseWeight{ Value = 0, Unit = "lbs" }, RepsCompleted = 0 }
                        }
                    },
                    new ProgramDay
                    {
                        Day = 2,
                        Focus = "Lower Body (BW)",
                        Exercises = new List<ExerciseEntry>
                        {
                            new ExerciseEntry { Name = "Squats (Bodyweight)", Sets = 4, Reps = "12-20", Weight = new ExerciseWeight{ Value = 0, Unit = "lbs" }, RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Reverse Lunges", Sets = 3, Reps = "10-12/leg", Weight = new ExerciseWeight{ Value = 0, Unit = "lbs" }, RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Glute Bridges", Sets = 3, Reps = "12-20", Weight = new ExerciseWeight{ Value = 0, Unit = "lbs" }, RepsCompleted = 0 }
                        }
                    },
                    new ProgramDay
                    {
                        Day = 3,
                        Focus = "Upper Body Pull & Core (BW)",
                        Exercises = new List<ExerciseEntry>
                        {
                            new ExerciseEntry { Name = "Inverted Rows (Table/Bar)", Sets = 4, Reps = "6-12", Weight = new ExerciseWeight{ Value = 0, Unit = "lbs" }, RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Chin-Ups or Doorframe Holds", Sets = 3, Reps = "AMRAP/20-30 sec hold", Weight = new ExerciseWeight{ Value = 0, Unit = "lbs" }, RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Plank", Sets = 3, Reps = "30-60 sec", Weight = new ExerciseWeight{ Value = 0, Unit = "lbs" }, RepsCompleted = 0 }
                        }
                    }
                }
            },
            new ProgramTemplate
            {
                Name = "3-Day Dumbbell Full Body (Home)",
                Days = 3,
                Type = ProgramType.HomeDumbbell,
                Schedule = new List<ProgramDay>
                {
                    new ProgramDay
                    {
                        Day = 1,
                        Focus = "Upper Push (DB)",
                        Exercises = new List<ExerciseEntry>
                        {
                            new ExerciseEntry { Name = "DB Bench Press (Floor/Bench)", Sets = 4, Reps = "8-12", Weight = new ExerciseWeight{ Value = 0, Unit = "lbs" }, RepsCompleted = 0 },
                            new ExerciseEntry { Name = "DB Overhead Press", Sets = 3, Reps = "8-12", Weight = new ExerciseWeight{ Value = 0, Unit = "lbs" }, RepsCompleted = 0 },
                            new ExerciseEntry { Name = "DB Lateral Raise", Sets = 3, Reps = "12-15", Weight = new ExerciseWeight{ Value = 0, Unit = "lbs" }, RepsCompleted = 0 }
                        }
                    },
                    new ProgramDay
                    {
                        Day = 2,
                        Focus = "Lower (DB)",
                        Exercises = new List<ExerciseEntry>
                        {
                            new ExerciseEntry { Name = "DB Goblet Squat", Sets = 4, Reps = "8-12", Weight = new ExerciseWeight{ Value = 0, Unit = "lbs" }, RepsCompleted = 0 },
                            new ExerciseEntry { Name = "DB Romanian Deadlift", Sets = 3, Reps = "8-12", Weight = new ExerciseWeight{ Value = 0, Unit = "lbs" }, RepsCompleted = 0 },
                            new ExerciseEntry { Name = "DB Walking Lunges", Sets = 3, Reps = "10-12/leg", Weight = new ExerciseWeight{ Value = 0, Unit = "lbs" }, RepsCompleted = 0 }
                        }
                    },
                    new ProgramDay
                    {
                        Day = 3,
                        Focus = "Upper Pull + Core (DB)",
                        Exercises = new List<ExerciseEntry>
                        {
                            new ExerciseEntry { Name = "DB One-Arm Row", Sets = 4, Reps = "8-12/side", Weight = new ExerciseWeight{ Value = 0, Unit = "lbs" }, RepsCompleted = 0 },
                            new ExerciseEntry { Name = "DB Hammer Curl", Sets = 3, Reps = "10-12", Weight = new ExerciseWeight{ Value = 0, Unit = "lbs" }, RepsCompleted = 0 },
                            new ExerciseEntry { Name = "DB Dead Bug (Load as needed)", Sets = 3, Reps = "8-10/side", Weight = new ExerciseWeight{ Value = 0, Unit = "lbs" }, RepsCompleted = 0 }
                        }
                    }
                }
            },
            new ProgramTemplate
            {
                Name = "3-Day Full Body Strength (Gym)",
                Days = 3,
                Type = ProgramType.Gym,
                Schedule = new List<ProgramDay>
                {
                    new ProgramDay
                    {
                        Day = 1,
                        Focus = "Upper Push (Gym)",
                        Exercises = new List<ExerciseEntry>
                        {
                            new ExerciseEntry { Name = "Barbell Bench Press", Sets = 4, Reps = "6-10", Weight = new ExerciseWeight{ Value = 0, Unit = "lbs" }, RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Seated DB Shoulder Press", Sets = 3, Reps = "8-12", Weight = new ExerciseWeight{ Value = 0, Unit = "lbs" }, RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Cable Fly", Sets = 3, Reps = "12-15", Weight = new ExerciseWeight{ Value = 0, Unit = "lbs" }, RepsCompleted = 0 }
                        }
                    },
                    new ProgramDay
                    {
                        Day = 2,
                        Focus = "Lower (Gym)",
                        Exercises = new List<ExerciseEntry>
                        {
                            new ExerciseEntry { Name = "Back Squat", Sets = 4, Reps = "5-8", Weight = new ExerciseWeight{ Value = 0, Unit = "lbs" }, RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Romanian Deadlift", Sets = 3, Reps = "6-10", Weight = new ExerciseWeight{ Value = 0, Unit = "lbs" }, RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Leg Press", Sets = 3, Reps = "10-15", Weight = new ExerciseWeight{ Value = 0, Unit = "lbs" }, RepsCompleted = 0 }
                        }
                    },
                    new ProgramDay
                    {
                        Day = 3,
                        Focus = "Upper Pull + Core (Gym)",
                        Exercises = new List<ExerciseEntry>
                        {
                            new ExerciseEntry { Name = "Pull-Ups or Lat Pulldown", Sets = 4, Reps = "6-12", Weight = new ExerciseWeight{ Value = 0, Unit = "lbs" }, RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Barbell or Cable Row", Sets = 3, Reps = "8-12", Weight = new ExerciseWeight{ Value = 0, Unit = "lbs" }, RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Cable Face Pull", Sets = 3, Reps = "12-15", Weight = new ExerciseWeight{ Value = 0, Unit = "lbs" }, RepsCompleted = 0 }
                        }
                    }
                }
            },

            // ===== 4-DAY PROGRAMS =====
            new ProgramTemplate
            {
                Name = "4-Day Calisthenics Upper/Lower",
                Days = 4,
                Type = ProgramType.Bodyweight,
                Schedule = new List<ProgramDay>
                {
                    new ProgramDay
                    {
                        Day = 1,
                        Focus = "Upper A (BW)",
                        Exercises = new List<ExerciseEntry>
                        {
                            new ExerciseEntry { Name = "Push-Ups", Sets = 4, Reps = "10-20", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Chin-Ups", Sets = 3, Reps = "AMRAP", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Side Plank", Sets = 3, Reps = "30-45 sec/side", Weight = new ExerciseWeight(), RepsCompleted = 0 }
                        }
                    },
                    new ProgramDay
                    {
                        Day = 2,
                        Focus = "Lower A (BW)",
                        Exercises = new List<ExerciseEntry>
                        {
                            new ExerciseEntry { Name = "Split Squats", Sets = 4, Reps = "10-12/leg", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Single-Leg RDL (Unloaded)", Sets = 3, Reps = "10-12/leg", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Calf Raises (BW)", Sets = 3, Reps = "15-20", Weight = new ExerciseWeight(), RepsCompleted = 0 }
                        }
                    },
                    new ProgramDay
                    {
                        Day = 3,
                        Focus = "Upper B (BW)",
                        Exercises = new List<ExerciseEntry>
                        {
                            new ExerciseEntry { Name = "Diamond Push-Ups", Sets = 4, Reps = "6-12", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Inverted Rows", Sets = 3, Reps = "8-12", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Hollow Body Hold", Sets = 3, Reps = "20-40 sec", Weight = new ExerciseWeight(), RepsCompleted = 0 }
                        }
                    },
                    new ProgramDay
                    {
                        Day = 4,
                        Focus = "Lower B (BW)",
                        Exercises = new List<ExerciseEntry>
                        {
                            new ExerciseEntry { Name = "Squat Jumps", Sets = 3, Reps = "8-10", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Reverse Lunges", Sets = 3, Reps = "12/leg", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Hip Thrusts (BW)", Sets = 3, Reps = "15-20", Weight = new ExerciseWeight(), RepsCompleted = 0 }
                        }
                    }
                }
            },
            new ProgramTemplate
            {
                Name = "4-Day Dumbbell Upper/Lower (Home)",
                Days = 4,
                Type = ProgramType.HomeDumbbell,
                Schedule = new List<ProgramDay>
                {
                    new ProgramDay
                    {
                        Day = 1,
                        Focus = "Upper A (DB)",
                        Exercises = new List<ExerciseEntry>
                        {
                            new ExerciseEntry { Name = "DB Bench Press", Sets = 4, Reps = "8-12", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "DB One-Arm Row", Sets = 3, Reps = "8-12/side", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "DB Lateral Raise", Sets = 3, Reps = "12-15", Weight = new ExerciseWeight(), RepsCompleted = 0 }
                        }
                    },
                    new ProgramDay
                    {
                        Day = 2,
                        Focus = "Lower A (DB)",
                        Exercises = new List<ExerciseEntry>
                        {
                            new ExerciseEntry { Name = "DB Goblet Squat", Sets = 4, Reps = "8-12", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "DB RDL", Sets = 3, Reps = "8-12", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "DB Calf Raise", Sets = 3, Reps = "12-20", Weight = new ExerciseWeight(), RepsCompleted = 0 }
                        }
                    },
                    new ProgramDay
                    {
                        Day = 3,
                        Focus = "Upper B (DB)",
                        Exercises = new List<ExerciseEntry>
                        {
                            new ExerciseEntry { Name = "DB Incline Press (Bench or Floor)", Sets = 4, Reps = "8-12", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "DB Rear-Delt Fly", Sets = 3, Reps = "12-15", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "DB Curl", Sets = 3, Reps = "10-12", Weight = new ExerciseWeight(), RepsCompleted = 0 }
                        }
                    },
                    new ProgramDay
                    {
                        Day = 4,
                        Focus = "Lower B + Core (DB)",
                        Exercises = new List<ExerciseEntry>
                        {
                            new ExerciseEntry { Name = "DB Split Squat", Sets = 4, Reps = "8-10/leg", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "DB Hip Thrust/Glute Bridge", Sets = 3, Reps = "10-15", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Weighted Plank (DB on hips)", Sets = 3, Reps = "30-45 sec", Weight = new ExerciseWeight(), RepsCompleted = 0 }
                        }
                    }
                }
            },
            new ProgramTemplate
            {
                Name = "4-Day Upper/Lower Strength (Gym)",
                Days = 4,
                Type = ProgramType.Gym,
                Schedule = new List<ProgramDay>
                {
                    new ProgramDay
                    {
                        Day = 1,
                        Focus = "Upper A",
                        Exercises = new List<ExerciseEntry>
                        {
                            new ExerciseEntry { Name = "Barbell Bench Press", Sets = 4, Reps = "5-8", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Weighted Pull-Ups or Pulldown", Sets = 4, Reps = "6-10", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "DB Incline Press", Sets = 3, Reps = "8-12", Weight = new ExerciseWeight(), RepsCompleted = 0 }
                        }
                    },
                    new ProgramDay
                    {
                        Day = 2,
                        Focus = "Lower A",
                        Exercises = new List<ExerciseEntry>
                        {
                            new ExerciseEntry { Name = "Back Squat", Sets = 4, Reps = "5-8", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Leg Press", Sets = 3, Reps = "10-15", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Hanging Knee Raise", Sets = 3, Reps = "10-15", Weight = new ExerciseWeight(), RepsCompleted = 0 }
                        }
                    },
                    new ProgramDay
                    {
                        Day = 3,
                        Focus = "Upper B",
                        Exercises = new List<ExerciseEntry>
                        {
                            new ExerciseEntry { Name = "Overhead Press", Sets = 4, Reps = "6-10", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Barbell Row", Sets = 4, Reps = "6-10", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Cable Lateral Raise", Sets = 3, Reps = "12-15", Weight = new ExerciseWeight(), RepsCompleted = 0 }
                        }
                    },
                    new ProgramDay
                    {
                        Day = 4,
                        Focus = "Lower B",
                        Exercises = new List<ExerciseEntry>
                        {
                            new ExerciseEntry { Name = "Deadlift (Conventional or Sumo)", Sets = 3, Reps = "3-5", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Bulgarian Split Squat", Sets = 3, Reps = "8-10/leg", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Seated Calf Raise", Sets = 3, Reps = "12-15", Weight = new ExerciseWeight(), RepsCompleted = 0 }
                        }
                    }
                }
            },

            // ===== 5-DAY PROGRAMS =====
            new ProgramTemplate
            {
                Name = "5-Day Bodyweight Performance",
                Days = 5,
                Type = ProgramType.Bodyweight,
                Schedule = new List<ProgramDay>
                {
                    new ProgramDay { Day = 1, Focus = "Push (BW)", Exercises = new List<ExerciseEntry>
                        {
                            new ExerciseEntry { Name = "Push-Ups", Sets = 5, Reps = "10-20", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Decline Push-Ups", Sets = 3, Reps = "6-12", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Triceps Bench Dips", Sets = 3, Reps = "10-15", Weight = new ExerciseWeight(), RepsCompleted = 0 }
                        }
                    },
                    new ProgramDay { Day = 2, Focus = "Legs (BW)", Exercises = new List<ExerciseEntry>
                        {
                            new ExerciseEntry { Name = "Bodyweight Squats", Sets = 4, Reps = "15-25", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Walking Lunges", Sets = 3, Reps = "12/leg", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Nordic Curl Eccentrics (Assisted)", Sets = 3, Reps = "5-6", Weight = new ExerciseWeight(), RepsCompleted = 0 }
                        }
                    },
                    new ProgramDay { Day = 3, Focus = "Pull (BW)", Exercises = new List<ExerciseEntry>
                        {
                            new ExerciseEntry { Name = "Pull-Ups", Sets = 4, Reps = "AMRAP", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Inverted Rows", Sets = 3, Reps = "8-12", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Superman Hold", Sets = 3, Reps = "20-40 sec", Weight = new ExerciseWeight(), RepsCompleted = 0 }
                        }
                    },
                    new ProgramDay { Day = 4, Focus = "Core + Conditioning", Exercises = new List<ExerciseEntry>
                        {
                            new ExerciseEntry { Name = "Plank", Sets = 4, Reps = "45-60 sec", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Hollow Body to Arch Rockers", Sets = 3, Reps = "10-12", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Burpees", Sets = 3, Reps = "10-15", Weight = new ExerciseWeight(), RepsCompleted = 0 }
                        }
                    },
                    new ProgramDay { Day = 5, Focus = "Full Body (BW)", Exercises = new List<ExerciseEntry>
                        {
                            new ExerciseEntry { Name = "Handstand Holds (Wall)", Sets = 3, Reps = "20-40 sec", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Pistol Squat Progression", Sets = 3, Reps = "5-8/leg", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Chin-Ups (Neutral/Underhand)", Sets = 3, Reps = "AMRAP", Weight = new ExerciseWeight(), RepsCompleted = 0 }
                        }
                    }
                }
            },
            new ProgramTemplate
            {
                Name = "5-Day Dumbbell Hybrid (Home)",
                Days = 5,
                Type = ProgramType.HomeDumbbell,
                Schedule = new List<ProgramDay>
                {
                    new ProgramDay { Day = 1, Focus = "Push (DB)", Exercises = new List<ExerciseEntry>
                        {
                            new ExerciseEntry { Name = "DB Flat Press", Sets = 4, Reps = "8-12", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "DB Overhead Press", Sets = 3, Reps = "8-12", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "DB Triceps Overhead Extension", Sets = 3, Reps = "10-12", Weight = new ExerciseWeight(), RepsCompleted = 0 }
                        }
                    },
                    new ProgramDay { Day = 2, Focus = "Pull (DB)", Exercises = new List<ExerciseEntry>
                        {
                            new ExerciseEntry { Name = "DB Bent-Over Row", Sets = 4, Reps = "8-12", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "DB Rear-Delt Fly", Sets = 3, Reps = "12-15", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "DB Curl (Alt.)", Sets = 3, Reps = "10-12", Weight = new ExerciseWeight(), RepsCompleted = 0 }
                        }
                    },
                    new ProgramDay { Day = 3, Focus = "Legs (DB)", Exercises = new List<ExerciseEntry>
                        {
                            new ExerciseEntry { Name = "DB Front-Foot Elevated Split Squat", Sets = 4, Reps = "8-10/leg", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "DB RDL", Sets = 3, Reps = "8-12", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "DB Calf Raise", Sets = 3, Reps = "12-20", Weight = new ExerciseWeight(), RepsCompleted = 0 }
                        }
                    },
                    new ProgramDay { Day = 4, Focus = "Upper Accessory + Core (DB)", Exercises = new List<ExerciseEntry>
                        {
                            new ExerciseEntry { Name = "DB Incline Press", Sets = 3, Reps = "8-12", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "DB One-Arm Row (Chest-Supported Optional)", Sets = 3, Reps = "10-12/side", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Weighted Hollow Hold (DB)", Sets = 3, Reps = "20-40 sec", Weight = new ExerciseWeight(), RepsCompleted = 0 }
                        }
                    },
                    new ProgramDay { Day = 5, Focus = "Full Body (DB)", Exercises = new List<ExerciseEntry>
                        {
                            new ExerciseEntry { Name = "DB Clean to Front Squat", Sets = 3, Reps = "6-8", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "DB Push Press", Sets = 3, Reps = "6-8", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "DB Farmer Carry", Sets = 3, Reps = "40-60 yd", Weight = new ExerciseWeight(), RepsCompleted = 0 }
                        }
                    }
                }
            },
            new ProgramTemplate
            {
                Name = "5-Day Push/Pull/Legs + Upper/Full (Gym)",
                Days = 5,
                Type = ProgramType.Gym,
                Schedule = new List<ProgramDay>
                {
                    new ProgramDay { Day = 1, Focus = "Push", Exercises = new List<ExerciseEntry>
                        {
                            new ExerciseEntry { Name = "Barbell Bench Press", Sets = 4, Reps = "5-8", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Overhead Press", Sets = 3, Reps = "6-10", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Cable Fly", Sets = 3, Reps = "12-15", Weight = new ExerciseWeight(), RepsCompleted = 0 }
                        }
                    },
                    new ProgramDay { Day = 2, Focus = "Pull", Exercises = new List<ExerciseEntry>
                        {
                            new ExerciseEntry { Name = "Weighted Pull-Ups / Lat Pulldown", Sets = 4, Reps = "6-10", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Barbell Row", Sets = 3, Reps = "6-10", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Face Pull", Sets = 3, Reps = "12-15", Weight = new ExerciseWeight(), RepsCompleted = 0 }
                        }
                    },
                    new ProgramDay { Day = 3, Focus = "Legs", Exercises = new List<ExerciseEntry>
                        {
                            new ExerciseEntry { Name = "Back Squat", Sets = 4, Reps = "5-8", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Romanian Deadlift", Sets = 3, Reps = "6-10", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Leg Curl", Sets = 3, Reps = "10-12", Weight = new ExerciseWeight(), RepsCompleted = 0 }
                        }
                    },
                    new ProgramDay { Day = 4, Focus = "Upper Accessory", Exercises = new List<ExerciseEntry>
                        {
                            new ExerciseEntry { Name = "Incline DB Press", Sets = 3, Reps = "8-12", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Chest-Supported Row", Sets = 3, Reps = "8-12", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Cable Lateral Raise", Sets = 3, Reps = "12-15", Weight = new ExerciseWeight(), RepsCompleted = 0 }
                        }
                    },
                    new ProgramDay { Day = 5, Focus = "Full Body (Power/Carry)", Exercises = new List<ExerciseEntry>
                        {
                            new ExerciseEntry { Name = "Power Clean (Light/Skill)", Sets = 3, Reps = "3-5", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Front Squat", Sets = 3, Reps = "5-8", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Farmer Carry", Sets = 3, Reps = "50-80 yd", Weight = new ExerciseWeight(), RepsCompleted = 0 }
                        }
                    }
                }
            },

            // ===== 6-DAY PROGRAMS =====
            new ProgramTemplate
            {
                Name = "6-Day Bodyweight PPL (Push/Pull/Legs x2)",
                Days = 6,
                Type = ProgramType.Bodyweight,
                Schedule = new List<ProgramDay>
                {
                    new ProgramDay { Day = 1, Focus = "Push A (BW)", Exercises = new List<ExerciseEntry>
                        {
                            new ExerciseEntry { Name = "Push-Ups", Sets = 4, Reps = "12-20", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Pike Push-Ups", Sets = 3, Reps = "8-12", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Bench Dips", Sets = 3, Reps = "12-15", Weight = new ExerciseWeight(), RepsCompleted = 0 }
                        }
                    },
                    new ProgramDay { Day = 2, Focus = "Pull A (BW)", Exercises = new List<ExerciseEntry>
                        {
                            new ExerciseEntry { Name = "Pull-Ups", Sets = 4, Reps = "AMRAP", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Inverted Rows", Sets = 3, Reps = "10-12", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Reverse Snow Angels", Sets = 3, Reps = "12-15", Weight = new ExerciseWeight(), RepsCompleted = 0 }
                        }
                    },
                    new ProgramDay { Day = 3, Focus = "Legs A (BW)", Exercises = new List<ExerciseEntry>
                        {
                            new ExerciseEntry { Name = "Bodyweight Squats", Sets = 4, Reps = "15-25", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Reverse Lunges", Sets = 3, Reps = "12/leg", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Calf Raises (BW)", Sets = 3, Reps = "15-25", Weight = new ExerciseWeight(), RepsCompleted = 0 }
                        }
                    },
                    new ProgramDay { Day = 4, Focus = "Push B (BW)", Exercises = new List<ExerciseEntry>
                        {
                            new ExerciseEntry { Name = "Decline Push-Ups", Sets = 4, Reps = "8-15", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Close-Grip Push-Ups", Sets = 3, Reps = "8-15", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Shoulder Taps", Sets = 3, Reps = "20 alt.", Weight = new ExerciseWeight(), RepsCompleted = 0 }
                        }
                    },
                    new ProgramDay { Day = 5, Focus = "Pull B (BW)", Exercises = new List<ExerciseEntry>
                        {
                            new ExerciseEntry { Name = "Chin-Ups", Sets = 4, Reps = "AMRAP", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Table Rows (Feet Elevated)", Sets = 3, Reps = "8-12", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Superman Hold", Sets = 3, Reps = "30-40 sec", Weight = new ExerciseWeight(), RepsCompleted = 0 }
                        }
                    },
                    new ProgramDay { Day = 6, Focus = "Legs B (BW) + Core", Exercises = new List<ExerciseEntry>
                        {
                            new ExerciseEntry { Name = "Jump Squats", Sets = 3, Reps = "8-12", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Pistol Squat Progression", Sets = 3, Reps = "5-8/leg", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Plank", Sets = 3, Reps = "45-60 sec", Weight = new ExerciseWeight(), RepsCompleted = 0 }
                        }
                    }
                }
            },
            new ProgramTemplate
            {
                Name = "6-Day Dumbbell PPL (Home)",
                Days = 6,
                Type = ProgramType.HomeDumbbell,
                Schedule = new List<ProgramDay>
                {
                    new ProgramDay { Day = 1, Focus = "Push A (DB)", Exercises = new List<ExerciseEntry>
                        {
                            new ExerciseEntry { Name = "DB Flat Press", Sets = 4, Reps = "8-12", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "DB Overhead Press", Sets = 3, Reps = "8-12", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "DB Lateral Raise", Sets = 3, Reps = "12-15", Weight = new ExerciseWeight(), RepsCompleted = 0 }
                        }
                    },
                    new ProgramDay { Day = 2, Focus = "Pull A (DB)", Exercises = new List<ExerciseEntry>
                        {
                            new ExerciseEntry { Name = "DB One-Arm Row", Sets = 4, Reps = "8-12/side", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "DB Rear-Delt Fly", Sets = 3, Reps = "12-15", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "DB Curl", Sets = 3, Reps = "10-12", Weight = new ExerciseWeight(), RepsCompleted = 0 }
                        }
                    },
                    new ProgramDay { Day = 3, Focus = "Legs A (DB)", Exercises = new List<ExerciseEntry>
                        {
                            new ExerciseEntry { Name = "DB Goblet Squat", Sets = 4, Reps = "8-12", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "DB RDL", Sets = 3, Reps = "8-12", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "DB Calf Raise", Sets = 3, Reps = "12-20", Weight = new ExerciseWeight(), RepsCompleted = 0 }
                        }
                    },
                    new ProgramDay { Day = 4, Focus = "Push B (DB)", Exercises = new List<ExerciseEntry>
                        {
                            new ExerciseEntry { Name = "DB Incline Press", Sets = 4, Reps = "8-12", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "DB Arnold Press", Sets = 3, Reps = "8-12", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "DB Triceps Kickback", Sets = 3, Reps = "12-15", Weight = new ExerciseWeight(), RepsCompleted = 0 }
                        }
                    },
                    new ProgramDay { Day = 5, Focus = "Pull B (DB)", Exercises = new List<ExerciseEntry>
                        {
                            new ExerciseEntry { Name = "DB Chest-Supported Row", Sets = 4, Reps = "8-12", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "DB Pullover", Sets = 3, Reps = "10-12", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "DB Hammer Curl", Sets = 3, Reps = "10-12", Weight = new ExerciseWeight(), RepsCompleted = 0 }
                        }
                    },
                    new ProgramDay { Day = 6, Focus = "Legs B + Core (DB)", Exercises = new List<ExerciseEntry>
                        {
                            new ExerciseEntry { Name = "DB Bulgarian Split Squat", Sets = 4, Reps = "8-10/leg", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "DB Hip Thrust/Glute Bridge", Sets = 3, Reps = "10-15", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Weighted Plank (DB)", Sets = 3, Reps = "30-45 sec", Weight = new ExerciseWeight(), RepsCompleted = 0 }
                        }
                    }
                }
            },
            new ProgramTemplate
            {
                Name = "6-Day Push/Pull/Legs x2 (Gym)",
                Days = 6,
                Type = ProgramType.Gym,
                Schedule = new List<ProgramDay>
                {
                    new ProgramDay { Day = 1, Focus = "Push A", Exercises = new List<ExerciseEntry>
                        {
                            new ExerciseEntry { Name = "Barbell Bench Press", Sets = 4, Reps = "5-8", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "DB Shoulder Press", Sets = 3, Reps = "8-12", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Cable Lateral Raise", Sets = 3, Reps = "12-15", Weight = new ExerciseWeight(), RepsCompleted = 0 }
                        }
                    },
                    new ProgramDay { Day = 2, Focus = "Pull A", Exercises = new List<ExerciseEntry>
                        {
                            new ExerciseEntry { Name = "Weighted Pull-Ups / Lat Pulldown", Sets = 4, Reps = "6-10", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Barbell Row", Sets = 4, Reps = "6-10", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Face Pull", Sets = 3, Reps = "12-15", Weight = new ExerciseWeight(), RepsCompleted = 0 }
                        }
                    },
                    new ProgramDay { Day = 3, Focus = "Legs A", Exercises = new List<ExerciseEntry>
                        {
                            new ExerciseEntry { Name = "Back Squat", Sets = 4, Reps = "5-8", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Romanian Deadlift", Sets = 3, Reps = "6-10", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Leg Press", Sets = 3, Reps = "10-15", Weight = new ExerciseWeight(), RepsCompleted = 0 }
                        }
                    },
                    new ProgramDay { Day = 4, Focus = "Push B", Exercises = new List<ExerciseEntry>
                        {
                            new ExerciseEntry { Name = "Incline Barbell Press", Sets = 4, Reps = "6-10", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Overhead Press", Sets = 3, Reps = "6-10", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Cable Fly", Sets = 3, Reps = "12-15", Weight = new ExerciseWeight(), RepsCompleted = 0 }
                        }
                    },
                    new ProgramDay { Day = 5, Focus = "Pull B", Exercises = new List<ExerciseEntry>
                        {
                            new ExerciseEntry { Name = "Chest-Supported Row", Sets = 4, Reps = "8-12", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Seated Cable Row", Sets = 3, Reps = "10-12", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Barbell or DB Curl", Sets = 3, Reps = "10-12", Weight = new ExerciseWeight(), RepsCompleted = 0 }
                        }
                    },
                    new ProgramDay { Day = 6, Focus = "Legs B + Core", Exercises = new List<ExerciseEntry>
                        {
                            new ExerciseEntry { Name = "Deadlift (Conventional/Sumo)", Sets = 3, Reps = "3-5", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Bulgarian Split Squat", Sets = 3, Reps = "8-10/leg", Weight = new ExerciseWeight(), RepsCompleted = 0 },
                            new ExerciseEntry { Name = "Hanging Leg Raise", Sets = 3, Reps = "10-15", Weight = new ExerciseWeight(), RepsCompleted = 0 }
                        }
                    }
                }
            }
        };
    }
}