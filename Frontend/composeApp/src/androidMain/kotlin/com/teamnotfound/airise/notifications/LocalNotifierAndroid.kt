package com.teamnotfound.airise.notifications

import notifications.LocalNotifier
import android.app.*
import android.content.*
import android.os.Build
import java.util.Calendar

class LocalNotifierAndroid(private val ctx: Context) : LocalNotifier {
    private val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun schedule(id: Int, title: String, body: String, triggerAtEpochMillis: Long) {
        ensureChannel()
        val intent = Intent(ctx, WorkoutReminderReceiver::class.java).apply { putExtra("notifId", id)putExtra("title", title)putExtra("message", body)}
        val pi = PendingIntent.getBroadcast(
            ctx, id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        setExactOrWindow(triggerAtEpochMillis, pi)
    }

    override fun cancel(id: Int) {
        val intent = Intent(ctx, WorkoutReminderReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            ctx, id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.cancel(pi)
    }

    override fun scheduleDaily(id: Int, title: String, body: String, hour: Int, minute: Int) {
        ensureChannel()
        val next = nextTriggerAt(hour, minute)

        val intent = Intent(ctx, WorkoutReminderReceiver::class.java).apply {
            putExtra("notifId", id)
            putExtra("title", title)
            putExtra("message", body)
            putExtra("dailyHour", hour)
            putExtra("dailyMinute", minute)
            putExtra("isDaily", true)
        }
        val pi = PendingIntent.getBroadcast(
            ctx, id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        setExactOrWindow(next, pi)

        saveDaily(id, title, body, hour, minute, next)
    }

    private fun nextTriggerAt(hour: Int, minute: Int): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        val now = System.currentTimeMillis()
        if (cal.timeInMillis <= now) {
            cal.add(Calendar.DAY_OF_YEAR, 1) // tomorrow
        }
        return cal.timeInMillis
    }

    private fun setExactOrWindow(triggerAt: Long, pi: PendingIntent) {
        val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) am.canScheduleExactAlarms() else true
        if (canExact) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            } else {
                am.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            }
        } else {
            am.setWindow(AlarmManager.RTC_WAKEUP, triggerAt, 60_000L, pi) // ~1 min window
        }
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(
                NotificationChannel("WORKOUT", "Workout Reminders", NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
    }

    private fun saveDaily(id: Int, title: String, body: String, hour: Int, minute: Int, whenMs: Long) {
        val sp = ctx.getSharedPreferences("workout_reminders", Context.MODE_PRIVATE)
        sp.edit()
            .putBoolean("daily", true)
            .putInt("id", id)
            .putString("title", title)
            .putString("body", body)
            .putInt("hour", hour)
            .putInt("minute", minute)
            .putLong("when", whenMs)
            .apply()
    }
}
