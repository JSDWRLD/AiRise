using Mongo2Go;
using MongoDB.Driver;
using System;
using System.Linq;

public class MongoIntegrationTest<TDocument> : IDisposable
{
    private readonly string _dbName;

    public MongoDbRunner Runner { get; }
    public IMongoDatabase Database { get; }
    public IMongoCollection<TDocument> Collection { get; }

    public MongoIntegrationTest()
    {
        // Build a SAFE database name (Mongo forbids ".", " ", "/", "\", "$", "\"")
        var typeName = typeof(TDocument).Name;            // e.g., "UserChallenges" (no dots)
        var guid = Guid.NewGuid().ToString("N");          // 32 hex chars, no dashes
        _dbName = $"IntegrationTest_{typeName}_{guid}";   // e.g., IntegrationTest_UserChallenges_a1b2...

        // Extra safety: replace any stray illegal chars just in case
        _dbName = SanitizeForMongoDbName(_dbName);

        Runner = MongoDbRunner.Start();
        var client = new MongoClient(Runner.ConnectionString);

        Database = client.GetDatabase(_dbName);
        Collection = Database.GetCollection<TDocument>("TestCollection");
    }

    public async System.Threading.Tasks.Task ClearCollectionAsync()
    {
        // Drop & recreate collection to guarantee a clean slate, avoiding namespace weirdness
        try
        {
            await Database.DropCollectionAsync("TestCollection");
        }
        catch
        {
            // ignore if it doesn't exist
        }

        // Recreate collection explicitly so DeleteManyAsync never targets a non-existent namespace
        await Database.CreateCollectionAsync("TestCollection");

        // No-op delete (kept for parity with your other tests; safe because the collection exists)
        await Collection.DeleteManyAsync(FilterDefinition<TDocument>.Empty);
    }

    public void Dispose()
    {
        Runner?.Dispose();
    }

    private static string SanitizeForMongoDbName(string name)
    {
        // Mongo DB name cannot contain: / \ . " $ space and must be <= 63 bytes (we’re safely under that)
        var illegal = new[] { '/', '\\', '.', '"', '$', ' ' };
        var chars = name.Select(ch => illegal.Contains(ch) ? '_' : ch).ToArray();
        return new string(chars);
    }
}
