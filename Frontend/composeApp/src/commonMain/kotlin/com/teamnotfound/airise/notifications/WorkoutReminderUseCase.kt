package notifications

class WorkoutReminderUseCase(
    private val notifier: LocalNotifier,
    private val prefs: ReminderPrefs = ReminderPrefs()
) {
    private val NOTIF_ID = 1001

    fun scheduleActive(title: String, body: String) {
        val instant = ReminderPlanner.nextFireInstant(prefs)
        notifier.schedule(NOTIF_ID, title, body, instant.toEpochMilliseconds())
    }

    fun cancelActive() {
        notifier.cancel(NOTIF_ID)
    }
}
