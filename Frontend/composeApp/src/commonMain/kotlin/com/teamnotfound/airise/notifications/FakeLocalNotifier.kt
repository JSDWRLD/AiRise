package notifications

class FakeLocalNotifier : LocalNotifier {

    data class OneShotCall(
        val id: Int,
        val title: String,
        val body: String,
        val triggerAtEpochMillis: Long
    )

    data class DailyCall(
        val id: Int,
        val title: String,
        val body: String,
        val hour: Int,
        val minute: Int
    )

    val oneShotCalls = mutableListOf<OneShotCall>()
    val dailyCalls   = mutableListOf<DailyCall>()
    val canceledIds  = mutableListOf<Int>()

    override fun schedule(id: Int, title: String, body: String, triggerAtEpochMillis: Long) {
        oneShotCalls += OneShotCall(id, title, body, triggerAtEpochMillis)
    }

    override fun cancel(id: Int) {
        canceledIds += id
    }

    override fun scheduleDaily(id: Int, title: String, body: String, hour: Int, minute: Int) {
        dailyCalls += DailyCall(id, title, body, hour, minute)
    }
}
