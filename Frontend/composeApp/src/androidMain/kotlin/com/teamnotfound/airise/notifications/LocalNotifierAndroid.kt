package notifications

import android.app.*
import android.content.*
import android.os.Build
import androidx.core.app.NotificationCompat

class LocalNotifierAndroid(private val ctx: Context) : LocalNotifier {
    private val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun schedule(id: Int, title: String, body: String, triggerAtEpochMillis: Long) {
        ensureChannel()
        val i = Intent(ctx, WorkoutReminderReceiver::class.java).apply {
            putExtra("notifId", id); putExtra("title", title); putExtra("body", body)
        }
        val pi = PendingIntent.getBroadcast(
            ctx, id, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (Build.VERSION.SDK_INT >= 23)
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtEpochMillis, pi)
        else
            am.setExact(AlarmManager.RTC_WAKEUP, triggerAtEpochMillis, pi)
    }

    override fun cancel(id: Int) {
        val i = Intent(ctx, WorkoutReminderReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            ctx, id, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.cancel(pi)
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(
                NotificationChannel("WORKOUT", "Workout Reminders", NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
    }
}

