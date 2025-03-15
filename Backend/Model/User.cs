using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;
using System.Text.Json.Serialization;

namespace AiRise.Models;

public class User
{
    [BsonId]
    [BsonRepresentation(BsonType.ObjectId)]
    public string? Id { get; set; }

    [BsonElement("username")]
    [JsonPropertyName("username")]
    public string Username { get; set; } = null!;

    [BsonElement("password")] // TODO: Hash this with bcrypt later on
    [JsonPropertyName("password")]
    public string Password { get; set; } = null!;

    [BsonElement("email")]
    [JsonPropertyName("email")]
    public string Email { get; set; } = null!;
}