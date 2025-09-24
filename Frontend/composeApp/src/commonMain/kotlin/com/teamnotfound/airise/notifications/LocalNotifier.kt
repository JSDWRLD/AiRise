package notifications

interface LocalNotifier {
    fun schedule(id: Int, title: String, body: String, triggerAtEpochMillis: Long)
    fun cancel(id: Int)
}