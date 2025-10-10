using System.Text.Json.Serialization;

namespace AiRise.Models.DTOs
{
    public class HealthData
    {
        [JsonPropertyName("caloriesBurned")]
        public int? CaloriesBurned { get; set; }

        [JsonPropertyName("caloriesEaten")]
        public int? CaloriesEaten { get; set; }

        [JsonPropertyName("caloriesTarget")]
        public int? CaloriesTarget { get; set; }

        [JsonPropertyName("steps")]
        public int? Steps { get; set; }

        [JsonPropertyName("sleep")]
        public double? Sleep { get; set; }

        [JsonPropertyName("hydration")]
        public double? Hydration { get; set; }

        [JsonPropertyName("hydrationTarget")]
        public double? HydrationTarget { get; set; }

        [JsonPropertyName("localDate")]
        public DateOnly LocalDate { get; set; } // Format: "YYYY-MM-DD"
    }
}