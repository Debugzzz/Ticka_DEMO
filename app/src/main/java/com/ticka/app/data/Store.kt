package com.ticka.app.data

import android.content.Context
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream

val tickaJson = Json { ignoreUnknownKeys = true; prettyPrint = true }

/**
 * 轻量 JSON 文件存储：filesDir/ticka.json
 * 数据安全设计：
 *  - 所有实例共享一把锁，写入严格串行（并发写同一个临时文件会造成 JSON 损坏）
 *  - 唯一临时文件 + fsync + 原子改名
 *  - 双份轮换备份（.bak / .bak2）：主文件损坏/被清空时自动回退，绝不让一次故障清空数据
 */
class Store(context: Context) {
    private val dir: File = context.filesDir
    private val file get() = File(dir, "ticka.json")

    companion object {
        private val LOCK = Any()

        private fun decode(text: String): AppData? = try {
            tickaJson.decodeFromString(AppData.serializer(), text)
        } catch (_: Exception) {
            null
        }

        private fun AppData.isEmptyContent() =
            tasks.isEmpty() && habits.isEmpty() && stars.isEmpty() && archivedSkies.isEmpty()
    }

    fun load(): AppData = synchronized(LOCK) {
        // 1) 主文件；若内容为空但更晚修改的备份里有数据（异常被清空的场景），采用备份
        if (file.exists()) {
            val main = decode(file.readText())
            if (main != null) {
                if (main.isEmptyContent()) {
                    val bak = File(dir, "ticka.json.bak")
                    if (bak.exists() && bak.lastModified() > file.lastModified()) {
                        decode(bak.readText())?.let { b -> if (!b.isEmptyContent()) return b }
                    }
                }
                return main
            }
            // 主文件损坏：挪走留证，回退备份链
            runCatching {
                file.renameTo(File(dir, "ticka.json.corrupt-${System.currentTimeMillis()}"))
            }
        }
        for (bak in listOf(File(dir, "ticka.json.bak"), File(dir, "ticka.json.bak2"))) {
            if (bak.exists()) decode(bak.readText())?.let { return it }
        }
        AppData()
    }

    fun save(data: AppData) = synchronized(LOCK) {
        try {
            // 轮换备份：当前主文件 → .bak，旧 .bak → .bak2
            if (file.exists()) {
                val bak = File(dir, "ticka.json.bak")
                val bak2 = File(dir, "ticka.json.bak2")
                if (bak.exists()) bak.copyTo(bak2, overwrite = true)
                file.copyTo(bak, overwrite = true)
            }
            val tmp = File(dir, "ticka.json.${System.nanoTime()}.tmp")
            FileOutputStream(tmp).use { out ->
                out.write(tickaJson.encodeToString(AppData.serializer(), data).toByteArray(Charsets.UTF_8))
                out.flush()
                out.fd.sync()
            }
            if (!tmp.renameTo(file)) {
                file.delete()
                tmp.renameTo(file)
            }
        } catch (_: Exception) {
        }
    }

    /** 清空备份（配合“清空所有数据”，避免备份把数据复活） */
    fun clearBackups() = synchronized(LOCK) {
        runCatching {
            File(dir, "ticka.json.bak").delete()
            File(dir, "ticka.json.bak2").delete()
        }
    }
}
