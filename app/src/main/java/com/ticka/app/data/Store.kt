package com.ticka.app.data

import android.content.Context
import kotlinx.serialization.json.Json
import java.io.File

/** 全局共享 JSON 配置（Store 与网络同步共用） */
val tickaJson = Json { ignoreUnknownKeys = true; prettyPrint = true }

/** 轻量 JSON 文件存储：filesDir/ticka.json，原子写入 */
class Store(context: Context) {
    private val file = File(context.filesDir, "ticka.json")

    fun load(): AppData = try {
        if (file.exists()) tickaJson.decodeFromString(AppData.serializer(), file.readText()) else AppData()
    } catch (_: Exception) {
        AppData()
    }

    fun save(data: AppData) {
        try {
            val tmp = File(file.parentFile, "ticka.json.tmp")
            tmp.writeText(tickaJson.encodeToString(AppData.serializer(), data))
            if (!tmp.renameTo(file)) {
                file.delete()
                tmp.renameTo(file)
            }
        } catch (_: Exception) {
        }
    }
}
