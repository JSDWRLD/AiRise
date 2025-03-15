using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace AiRise.Models;

public class User
{
    [BsonId]
    [BsonRepresentation(BsonType.ObjectId)]
    public string Id { get; set; }

    [BsonElement("username")]
    public string Username { get; set; }

    [BsonElement("password")] // TODO: Hash this with bcrypt later on
    public string Password { get; set; }

    [BsonElement("email")]
    public string Email { get; set; }
}