using MongoDB.Driver;
using Moq;

public static class MongoMoqHelpers
{
    /// <summary>
    /// Cursor that iterates ALL items, batch-by-batch, compatible with ToListAsync().
    /// </summary>
    public static Mock<IAsyncCursor<T>> MakeCursor<T>(IEnumerable<T> items)
    {
        var data = (items ?? Enumerable.Empty<T>()).ToList();
        var index = 0;
        var currentBatch = new List<T>();

        var cursor = new Mock<IAsyncCursor<T>>();

        cursor.SetupGet(c => c.Current).Returns(() => currentBatch);

        cursor.Setup(c => c.MoveNext(It.IsAny<CancellationToken>()))
              .Returns(() =>
              {
                  if (index >= data.Count) { currentBatch = new List<T>(); return false; }
                  // emit a simple 1-item batch each time (works with ToListAsync accumulation)
                  currentBatch = new List<T> { data[index++] };
                  return true;
              });

        cursor.Setup(c => c.MoveNextAsync(It.IsAny<CancellationToken>()))
              .ReturnsAsync(() =>
              {
                  if (index >= data.Count) { currentBatch = new List<T>(); return false; }
                  currentBatch = new List<T> { data[index++] };
                  return true;
              });

        return cursor;
    }

    public static void SetupFindAsync<T>(this Mock<IMongoCollection<T>> collMock, IEnumerable<T> items)
    {
        var cursor = MakeCursor(items);
        collMock
            .Setup(c => c.FindAsync(
                It.IsAny<FilterDefinition<T>>(),
                It.IsAny<FindOptions<T, T>>(),
                It.IsAny<CancellationToken>()))
            .ReturnsAsync(cursor.Object);
    }

    public static void SetupFindAsyncSingle<T>(this Mock<IMongoCollection<T>> collMock, T? item)
        => collMock.SetupFindAsync(item is null ? Enumerable.Empty<T>() : new[] { item });
}
