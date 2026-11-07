package com.vikku.dailyschedules.notifications

import android.app.*
import android.content.*
import android.os.Build
import androidx.core.app.NotificationCompat

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val channel = "vikku_reminders"
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) manager.createNotificationChannel(NotificationChannel(channel, "Daily reminders", NotificationManager.IMPORTANCE_DEFAULT))
        val title = intent.getStringExtra("title") ?: "Your next activity"
        val notification = NotificationCompat.Builder(context, channel)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Vikku Daily Schedules")
            .setContentText(title)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT).build()
        manager.notify(intent.getIntExtra("id", 1), notification)
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Daily schedules remain saved locally. Recreate reminders when the user next opens the app.
    }
}

object ReminderScheduler {
    fun schedule(context: Context, id: Int, title: String, atMillis: Long) {
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).putExtra("title", title).putExtra("id", id)
        val pending = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        if (atMillis <= System.currentTimeMillis()) return
        if (android.os.Build.VERSION.SDK_INT >= 23) alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMillis, pending)
        else alarm.set(AlarmManager.RTC_WAKEUP, atMillis, pending)
    }
}
