package com.ticka.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ticka.app.notify.ReminderScheduler
import com.ticka.app.ui.theme.TickaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ReminderScheduler.ensureChannel(this)
        val startTab = intent?.getIntExtra("start_tab", 0) ?: 0
        setContent {
            TickaTheme {
                TickaApp(initialTab = startTab)
            }
        }
    }
}
