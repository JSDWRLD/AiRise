using Mongo2Go;
using MongoDB.Driver;
using System;

public class MongoIntegrationTest<TDocument> : IDisposable
{
    private readonly string _dbName;
    public MongoDbRunner Runner { get; }
    public IMongoDatabase Database { get; }
    public IMongoCollection<TDocument> Collection { get; }

    public MongoIntegrationTest()
    {
        _dbName = $"IntegrationTest_{typeof(TDocument).Name}_{Guid.NewGuid()}";

        Runner = MongoDbRunner.Start();
        var client = new MongoClient(Runner.ConnectionString);

        Database = client.GetDatabase(_dbName);
        Collection = Database.GetCollection<TDocument>("TestCollection");
    }

    public async Task ClearCollectionAsync()
    {
        await Collection.DeleteManyAsync(FilterDefinition<TDocument>.Empty);
    }

    public void Dispose()
    {
        Runner.Dispose();
    }
}
