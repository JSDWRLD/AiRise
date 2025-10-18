using AiRise.Models.DTOs;
using AiRise.Models.User;
using MongoDB.Driver;

namespace AiRise.Services
{
    public class LeaderboardService
    {
        private readonly IMongoCollection<UserData> _userData;
        private readonly UserChallengesService _challenges;
        private readonly UserSettingsService _settings;
        private readonly UserFriendsService _friends;

        public LeaderboardService(
            MongoDBService mongo,
            UserChallengesService challenges,
            UserSettingsService settings,
            UserFriendsService friends)
        {
            _userData = mongo.GetCollection<UserData>("user.data");
            _challenges = challenges;
            _settings = settings;
            _friends = friends;
        }

        public async Task<IReadOnlyList<LeaderboardEntry>> GlobalTopNAsync(int n, CancellationToken ct = default)
        {
            var top = await _challenges.GetTopByStreakAsync(n, ct);
            return await JoinProfiles(top, ct);
        }

        public async Task<IReadOnlyList<LeaderboardEntry>> FriendsAsync(string firebaseUid, CancellationToken ct = default)
        {
            var uf = await _friends.GetUserFriends(firebaseUid);
            var ids = new HashSet<string>(uf?.FriendIds ?? Enumerable.Empty<string>(), StringComparer.Ordinal);
            ids.Add(firebaseUid);

            // Pull all challenges for this set
            var col = await _challenges.GetTopByStreakAsync(int.MaxValue, ct);
            var filtered = col.Where(c => ids.Contains(c.FirebaseUid))
                              .OrderByDescending(c => c.StreakCount)
                              .ToList();

            return await JoinProfiles(filtered, ct);
        }

        private async Task<IReadOnlyList<LeaderboardEntry>> JoinProfiles(IReadOnlyList<UserChallenges> cs, CancellationToken ct)
        {
            var uids = cs.Select(c => c.FirebaseUid).ToList();
            var profiles = await _userData
                .Find(Builders<UserData>.Filter.In(x => x.FirebaseUid, uids))
                .ToListAsync(ct);
            var profileMap = profiles.ToDictionary(p => p.FirebaseUid, p => p, StringComparer.Ordinal);

            var pictureMap = await _settings.GetPictureUrlMapAsync(uids, ct);

            return cs.Select(c =>
            {
                profileMap.TryGetValue(c.FirebaseUid, out var p);
                pictureMap.TryGetValue(c.FirebaseUid, out var pic);

                var name = !string.IsNullOrWhiteSpace(p?.FullName)
                    ? p!.FullName
                    : $"{p?.FirstName} {p?.LastName}".Trim();

                return new LeaderboardEntry
                {
                    Name = name ?? "",
                    ImageUrl = pic ?? "",
                    Streak = c.StreakCount
                };
            }).ToList();
        }
    }
}
