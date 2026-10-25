package com.ticka.app.notify

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager

/** 直接在闹钟音频通道播放系统闹钟铃声，绕过通知渠道铃声被 ROM 忽略的问题 */
object AlarmPlayer {

    private var player: MediaPlayer? = null

    @Volatile
    private var stopAtMs = 0L

    private var monitor: Thread? = null

    /**
     * 播放系统闹钟铃声 seconds 秒后自动停止（循环播放到时限）。
     * 已在播放时调用会延长播放时间而不是重启，避免多路径触发导致声音中断。
     */
    @Synchronized
    fun play(context: Context, seconds: Long = 8L) {
        if (player != null) {
            stopAtMs = System.currentTimeMillis() + seconds * 1000
            return
        }
        try {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ?: return
            val p = MediaPlayer()
            p.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            p.setDataSource(context, uri)
            p.isLooping = true
            p.prepare()
            p.start()
            player = p
            stopAtMs = System.currentTimeMillis() + seconds * 1000
            if (monitor == null) {
                monitor = Thread {
                    while (true) {
                        try {
                            Thread.sleep(250)
                        } catch (_: InterruptedException) {
                            return@Thread
                        }
                        synchronized(this) {
                            if (player != null && System.currentTimeMillis() >= stopAtMs) stop()
                        }
                    }
                }.apply { isDaemon = true; start() }
            }
        } catch (_: Exception) {
            stop()
        }
    }

    @Synchronized
    fun stop() {
        stopAtMs = 0L
        player?.let { p ->
            runCatching {
                if (p.isPlaying) p.stop()
                p.release()
            }
        }
        player = null
    }
}
