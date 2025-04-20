
namespace AiRise.Models.DTO;
public class UpdateUserSettings
{
    public string PictureUrl { get; set; } = string.Empty;
    public string AiPersonality { get; set; } = string.Empty;
    public bool ChallengeNotifsEnabled { get; set; } = false;
    public bool FriendReqNotifsEnabled { get; set; } = false;
    public bool StreakNotifsEnabled { get; set; } = false;
    public bool MealNotifsEnabled { get; set; } = false;
}