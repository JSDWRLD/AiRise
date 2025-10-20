public class UserList
{
    public List<UserProfile> Users { get; set; } = new List<UserProfile>();
}

public class UserProfile
{
    public string firebaseUid { get; set; }
    public string firstName { get; set; }
    public string middleName { get; set; }
    public string lastName { get; set; }
    public string fullName { get; set; }
    public string profile_picture_url { get; set; }
    public int streak { get; set; }
}