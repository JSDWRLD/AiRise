using System.Text.Json.Serialization;

namespace AiRise.Models.DTOs
{
    public class LeaderboardEntry
    {
        [JsonPropertyName("name")]
        public string Name { get; set; } = string.Empty;

        [JsonPropertyName("imageUrl")]
        public string ImageUrl { get; set; } = string.Empty;

        [JsonPropertyName("streak")]
        public int Streak { get; set; }
    }
}
