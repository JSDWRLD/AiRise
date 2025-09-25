package notifications

import kotlinx.datetime.Clock

private const val ACTIVE_ID = 2001

class WorkoutReminderUseCase(private val notifier: LocalNotifier) {

    fun scheduleAt(title: String, body: String, triggerAtEpochMillis: Long) {
        notifier.schedule(ACTIVE_ID, title, body, triggerAtEpochMillis)
    }

    fun scheduleDailyAt(hour: Int, minute: Int, title: String, body: String) {
        notifier.scheduleDaily(ACTIVE_ID, title, body, hour, minute)
    }

    fun cancelActive() {
        notifier.cancel(ACTIVE_ID)
    }

    // Optional debug helper
    fun scheduleTestIn(seconds: Long = 5) {
        val whenMs = Clock.System.now().toEpochMilliseconds() + seconds * 1000
        notifier.schedule(ACTIVE_ID, "Test notification", "It works 🎉", whenMs)
    }
}
