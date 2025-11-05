using Mongo2Go;
using MongoDB.Bson;
using MongoDB.Driver;
using System;
using System.Linq;

public class MongoIntegrationTest<TDocument> : IDisposable
{
    private readonly string _dbName;
    private readonly IMongoClient _client;
    private readonly HashSet<string> _collectionNames;

    public MongoDbRunner Runner { get; }
    public IMongoDatabase Database { get; }
    public IMongoClient Client => _client;

    public MongoIntegrationTest()
    {
        var typeName = typeof(TDocument).Name;
        _dbName = $"IntegrationTest_{typeName}_{Guid.NewGuid():N}";  

        Runner = MongoDbRunner.Start();
        _client = new MongoClient(Runner.ConnectionString);

        Database = _client.GetDatabase(_dbName);
        _collectionNames = new HashSet<string>();
    }

    public IMongoCollection<T> GetCollection<T>(string name)
    {
        if (string.IsNullOrWhiteSpace(name))
            throw new ArgumentException("Collection name cannot be null or empty.", nameof(name));

        if (!_collectionNames.Contains(name))
        {
            // Auto-create on first access
            Database.CreateCollectionAsync(name).GetAwaiter().GetResult();
            _collectionNames.Add(name);
        }

        return Database.GetCollection<T>(name);
    }

    public async Task ClearAllCollectionsAsync()
    {
        foreach (var name in _collectionNames)
        {
            try
            {
                await Database.DropCollectionAsync(name);
            }
            catch 
            {
                // ignore missing collections
            }

            await Database.CreateCollectionAsync(name);
        }
    }

    public void Dispose()
    {
        _client.DropDatabase(_dbName);
        Runner?.Dispose();
    }
}
