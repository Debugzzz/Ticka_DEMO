package com.ticka.app.notify

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.ticka.app.MainActivity
import com.ticka.app.data.I18n
import com.ticka.app.R

object ReminderScheduler {
    /** v2：闹钟铃声 + 闹钟音量 + 强振动（v1 渠道声音不可改，弃用） */
    private const val CHANNEL_ID = "ticka_reminders_v2"
    private const val OLD_CHANNEL_ID = "ticka_reminders"

    private fun contentIntent(context: Context): PendingIntent =
        PendingIntent.getActivity(
            context, 1002,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    private fun pi(context: Context, taskId: String): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).putExtra(AlarmReceiver.EXTRA_ID, taskId)
        return PendingIntent.getBroadcast(
            context, taskId.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun ensureChannel(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.deleteNotificationChannel(OLD_CHANNEL_ID)
        val channel = NotificationChannel(
            CHANNEL_ID, I18n.channelName, NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = I18n.channelDesc
            val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            setSound(
                sound,
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 500, 250, 500, 250, 500)
        }
        nm.createNotificationChannel(channel)
    }

    fun schedule(context: Context, taskId: String, at: Long) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        try {
            val exact = Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms()
            if (exact) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi(context, taskId))
            } else {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi(context, taskId))
            }
        } catch (_: SecurityException) {
            am.set(AlarmManager.RTC_WAKEUP, at, pi(context, taskId))
        }
    }

    fun cancel(context: Context, taskId: String) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pi(context, taskId))
    }

    fun notifyNow(context: Context, title: String, body: String) {
        ensureChannel(context)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val launch = contentIntent(context)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_ticka)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(launch)
            .setFullScreenIntent(launch, true)
            .build()
        try {
            nm.notify(title.hashCode(), notification)
        } catch (_: SecurityException) {
        }
    }
}
