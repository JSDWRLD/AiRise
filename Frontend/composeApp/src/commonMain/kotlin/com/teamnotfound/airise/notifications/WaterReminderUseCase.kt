package notifications

class WaterReminderUseCase(private val notifier: LocalNotifier) {
    private val baseId = 4000

    fun scheduleEvery2h(startHour: Int = 9, endHour: Int = 21, intervalMinutes: Int = 120) {
        // clear previous
        for (i in 0..12) notifier.cancel(baseId + i)

        var h = startHour
        var id = baseId
        while (h <= endHour) {
            notifier.scheduleDaily(
                id = id++,
                title = "Hydration time!",
                body = "Don't forget to drink some water",
                hour = h,
                minute = 0
            )
            h += intervalMinutes / 60
        }
    }

    fun cancelAll() {
        for (i in 0..12) notifier.cancel(baseId + i)
    }
}



