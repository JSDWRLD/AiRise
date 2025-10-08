package com.teamnotfound.airise.notifications

import android.app.*
import android.content.*
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.util.Calendar

class WorkoutReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Workout Reminder"
        val message = intent.getStringExtra("message") ?: "Time to train!"

        // ensure channel for direct triggers
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(
                NotificationChannel("WORKOUT", "Workout Reminders", NotificationManager.IMPORTANCE_DEFAULT)
            )
        }

        val notif = NotificationCompat.Builder(context, "WORKOUT")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(1001, notif)

        // If it’s a daily reminder, schedule the next day now
        val isDaily = intent.getBooleanExtra("isDaily", false)
        if (isDaily) {
            val id = intent.getIntExtra("notifId", 2001)
            val hour = intent.getIntExtra("dailyHour", 9)
            val minute = intent.getIntExtra("dailyMinute", 0)

            val next = nextTriggerAt(hour, minute)

            val nextIntent = Intent(context, WorkoutReminderReceiver::class.java).apply {
                putExtra("notifId", id)
                putExtra("title", title)
                putExtra("message", message)
                putExtra("dailyHour", hour)
                putExtra("dailyMinute", minute)
                putExtra("isDaily", true)
            }
            val pi = PendingIntent.getBroadcast(
                context, id, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) am.canScheduleExactAlarms() else true
            if (canExact) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next, pi)
                } else {
                    am.setExact(AlarmManager.RTC_WAKEUP, next, pi)
                }
            } else {
                am.setWindow(AlarmManager.RTC_WAKEUP, next, 60_000L, pi)
            }
        }
    }

    private fun nextTriggerAt(hour: Int, minute: Int): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            add(Calendar.DAY_OF_YEAR, 1) // reschedule for tomorrow
        }
        return cal.timeInMillis
    }
}
