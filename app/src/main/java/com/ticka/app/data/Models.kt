package com.ticka.app.data

import kotlinx.serialization.Serializable

@Serializable
data class Task(
    val id: String,
    val title: String,
    val notes: String = "",
    val dueAt: Long? = null,
    /** -1 不提醒；0 截止时；>0 提前 N 分钟 */
    val remindBeforeMin: Int = -1,
    val remindAt: Long? = null,
    val remindFired: Boolean = false,
    val completed: Boolean = false,
    val completedAt: Long? = null,
    val pomodoros: Int = 0,
    /** 首次完成时获得的星星，取消完成会收回它 */
    val starId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class Habit(
    val id: String,
    val name: String,
    /** 配色索引，见 ui.theme.habitPalettes */
    val palette: Int = 0,
    /** 排期星期，ISO 编号：周一=1 … 周日=7 */
    val weekdays: Set<Int> = setOf(1, 2, 3, 4, 5, 6, 7),
    val createdAt: Long = System.currentTimeMillis(),
    /** "yyyy-MM-dd"（凌晨3点结算）-> 完成时刻 epoch ms */
    val records: Map<String, Long> = emptyMap(),
    /** 打卡日期 -> 该日获得的星星 id（取消打卡收回，重打恢复同一颗） */
    val starIds: Map<String, String> = emptyMap()
)

/** 收集的星星：番茄钟专注 / 待办完成 / 每日打卡获得 */
@Serializable
data class Star(
    val id: String,
    val kind: Int,
    /** 0 = 番茄钟专注；1 = 待办完成；2 = 每日打卡 */
    val source: Int,
    val sourceName: String,
    val obtainedAt: Long,
    /** 星空画布上的归一化坐标 0..1 */
    val x: Float,
    val y: Float,
    /** 已收回（取消完成/取消打卡），星空上不显示；重新完成时原样恢复 */
    val revoked: Boolean = false
)

/** 封存的往期星空 */
@Serializable
data class StarArchive(
    val id: String,
    val stars: List<Star> = emptyList(),
    val archivedAt: Long
)

@Serializable
data class Settings(
    val serverUrl: String = "",
    val lastSyncAt: Long? = null,
    /** 待办/目标页背景散布已收集的星星 */
    val skyBackdrop: Boolean = true,
    /** 星空收集满多少颗自动封存换新 */
    val skyCapacity: Int = 24,
    /** 远程同步：服务器地址（IP/域名，可含端口和协议） */
    val host: String = "",
    /** 远程同步：用户名（HTTP Basic，可空） */
    val username: String = "",
    /** 远程同步：密码（HTTP Basic，可空） */
    val password: String = "",
    /** 远程同步：保存路径，如 /ticka.json */
    val path: String = "",
    /** 界面语言：zh（默认）/ en */
    val lang: String = "zh"
)

@Serializable
data class AppData(
    val tasks: List<Task> = emptyList(),
    val habits: List<Habit> = emptyList(),
    val stars: List<Star> = emptyList(),
    val archivedSkies: List<StarArchive> = emptyList(),
    val settings: Settings = Settings()
)
