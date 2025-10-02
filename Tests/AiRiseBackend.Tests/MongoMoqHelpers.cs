using MongoDB.Driver;
using Moq;

public static class MongoMoqHelpers
{
    public static Mock<IAsyncCursor<T>> MakeCursor<T>(IEnumerable<T> items)
    {
        var cursor = new Mock<IAsyncCursor<T>>();
        var enumerator = items.GetEnumerator();

        // Current returns the "batch" for the current MoveNext
        cursor.SetupGet(c => c.Current).Returns(() =>
        {
            // Return a single-item batch if enumerator is on a valid item; empty batch otherwise
            return enumerator.Current is null ? Array.Empty<T>() : new[] { enumerator.Current };
        });

        // MoveNext / MoveNextAsync simulate one batch per item (simple + good enough)
        cursor.SetupSequence(c => c.MoveNext(It.IsAny<CancellationToken>()))
              .Returns(() => enumerator.MoveNext())   // first call
              .Returns(false);                        // then end
        cursor.SetupSequence(c => c.MoveNextAsync(It.IsAny<CancellationToken>()))
              .ReturnsAsync(() => enumerator.MoveNext())
              .ReturnsAsync(false);

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
    {
        collMock.SetupFindAsync(item is null ? Enumerable.Empty<T>() : new[] { item });
    }
}
