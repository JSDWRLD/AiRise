public class FriendList
{
    public List<FriendListItem> Friends { get; set; }
}
public class FriendListItem
{
    public string firebaseUid { get; set; }
    public string firstName { get; set; }
    public string lastName { get; set; }
    public string email { get; set; }
    public int streak { get; set; }
}