package notifications

class WaterReminderUseCase(private val notifier: LocalNotifier) {
    private val baseId = 4000

    fun scheduleEvery2h(
        startHour: Int = 9,
        startMinute: Int = 0,
        endHour: Int = 21,
        endMinute: Int = 0,
        intervalMinutes: Int = 120
    ) {
        require(intervalMinutes in 1..(24 * 60)) { "intervalMinutes must be 1..1440" }

        cancelAll()

        val start = startHour * 60 + startMinute
        val end   = endHour   * 60 + endMinute
        var t     = start
        var id    = baseId

        var iterations = 0
        val maxIterations = 200

        while (t <= end && iterations < maxIterations) {
            val h = t / 60
            val m = t % 60
            notifier.scheduleDaily(
                id = id++,
                title = "Hydration time!",
                body = "Don’t forget to drink some water",
                hour = h,
                minute = m
            )
            t += intervalMinutes
            iterations++
        }
    }

    fun cancelAll() {
        for (i in 0..200) notifier.cancel(baseId + i)
    }
}