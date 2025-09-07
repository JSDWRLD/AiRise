public class UserList
{
    public List<UserProfile> Users { get; set; }
}
public class UserProfile
{
    public string firebaseUid { get; set; }
    public string firstName { get; set; }
    public string lastName { get; set; }
    public string profile_picture_url { get; set; }
    public int streak { get; set; }
}