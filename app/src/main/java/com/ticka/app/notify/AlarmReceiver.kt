package com.ticka.app.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ticka.app.data.I18n
import com.ticka.app.data.Store

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getStringExtra(EXTRA_ID) ?: return
        val store = Store(context)
        val data = store.load()
        I18n.lang = data.settings.lang
        val task = data.tasks.find { it.id == id } ?: return
        if (task.completed || task.remindFired) return

        val overdue = task.dueAt != null && task.dueAt <= System.currentTimeMillis()
        ReminderScheduler.notifyNow(
            context,
            "⏰ ${task.title}",
            if (overdue) I18n.notifyOverdue else I18n.notifyNearDue
        )
        store.save(data.copy(tasks = data.tasks.map { if (it.id == id) it.copy(remindFired = true) else it }))

        // 闹钟铃声：goAsync 保持进程存活直到播完
        val pending = goAsync()
        Thread {
            try {
                AlarmPlayer.play(context.applicationContext, 8L)
                Thread.sleep(8_500)
            } catch (_: InterruptedException) {
            }
            pending.finish()
        }.start()
    }

    companion object {
        const val EXTRA_ID = "id"
    }
}
