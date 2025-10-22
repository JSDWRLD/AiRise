package notifications

class TempNotifier : LocalNotifier {
    override fun schedule(id: Int, title: String, body: String, triggerAtEpochMillis: Long) { }
    override fun cancel(id: Int) {  }
    override fun scheduleDaily(id: Int, title: String, body: String, hour: Int, minute: Int) {  }
}
