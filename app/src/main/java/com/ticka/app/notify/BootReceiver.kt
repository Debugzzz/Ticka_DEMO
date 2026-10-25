package com.ticka.app.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ticka.app.data.Store

/** 开机后重新登记所有未触发的提醒闹钟 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val data = Store(context).load()
        val now = System.currentTimeMillis()
        data.tasks
            .filter { !it.completed && !it.remindFired && it.remindAt != null && it.remindAt > now }
            .forEach { task ->
                val at = task.remindAt
                if (at != null) ReminderScheduler.schedule(context, task.id, at)
            }
    }
}
