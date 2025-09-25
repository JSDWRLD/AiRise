package notifications

import kotlinx.datetime.*

data class ReminderPrefs(
    val hour: Int = 19,
    val minute: Int = 0,
    val timeZone: TimeZone = TimeZone.currentSystemDefault()
)

object ReminderPlanner {
    fun nextFireInstant(prefs: ReminderPrefs): Instant {
        val now = Clock.System.now().toLocalDateTime(prefs.timeZone)
        var t = LocalDateTime(
            date = now.date,
            time = LocalTime(prefs.hour, prefs.minute)
        )
        if (t <= now) {
            t = LocalDateTime(
                date = now.date.plus(DatePeriod(days = 1)),
                time = t.time
            )
        }
        return t.toInstant(prefs.timeZone)
    }
}
