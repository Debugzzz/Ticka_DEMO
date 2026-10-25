package com.ticka.app.data

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

enum class CountdownState { NORMAL, SOON, OVERDUE }

object DayUtils {
    private val zone: ZoneId get() = ZoneId.systemDefault()
    private val keyFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val locale: Locale get() = if (I18n.en) Locale.US else Locale.CHINESE
    private val hmFmt get() = DateTimeFormatter.ofPattern(if (I18n.en) "MMM d HH:mm" else "M月d日 HH:mm", locale)
    private val dateFmt get() = DateTimeFormatter.ofPattern(if (I18n.en) "MMM d, yyyy" else "yyyy年M月d日", locale)
    private val todayFmt get() = DateTimeFormatter.ofPattern(if (I18n.en) "EEE, MMM d" else "M月d日 EEEE", locale)

    /** 每日结算点：凌晨 3 点（0-3 点仍算前一天） */
    private const val DAY_OFFSET_MS = 3 * 3_600_000L

    fun keyOf(date: LocalDate): String = date.format(keyFmt)

    fun localDate(epochMs: Long): LocalDate = Instant.ofEpochMilli(epochMs).atZone(zone).toLocalDate()

    /** 按「凌晨3点结算」规则换算的 Ticka 日：传入时刻若在 0:00-3:00 之间归入前一天 */
    fun tickaDate(epochMs: Long = System.currentTimeMillis()): LocalDate =
        Instant.ofEpochMilli(epochMs - DAY_OFFSET_MS).atZone(zone).toLocalDate()

    fun todayKey(): String = keyOf(tickaDate())

    fun dueText(epochMs: Long): String =
        Instant.ofEpochMilli(epochMs).atZone(zone).toLocalDateTime().format(hmFmt)

    fun startDate(epochMs: Long): String = localDate(epochMs).format(dateFmt)

    fun todayLine(): String = LocalDate.now().format(todayFmt)

    fun toEpochMs(date: LocalDate, hour: Int, minute: Int): Long =
        LocalDateTime.of(date, LocalTime.of(hour, minute)).atZone(zone).toInstant().toEpochMilli()

    /** 开始打卡至今第几天（按 Ticka 日） */
    fun dayNumber(createdAt: Long): Long =
        ChronoUnit.DAYS.between(localDate(createdAt), tickaDate()) + 1

    /** 倒计时文案 + 状态 */
    fun countdown(dueAt: Long, now: Long = System.currentTimeMillis()): Pair<String, CountdownState> {
        val diff = dueAt - now
        if (diff >= 0) {
            val d = diff / 86_400_000L
            val h = diff % 86_400_000L / 3_600_000L
            val m = diff % 3_600_000L / 60_000L
            val s = diff % 60_000L / 1_000L
            return when {
                d > 0 -> I18n.cdDays(d, h) to CountdownState.NORMAL
                h > 0 -> I18n.cdHm(h, m.toString().padStart(2, '0')) to CountdownState.NORMAL
                diff >= 3_600_000L -> I18n.cdHm(h, m.toString().padStart(2, '0')) to CountdownState.NORMAL
                else -> I18n.cdMs(m.toString().padStart(2, '0'), s.toString().padStart(2, '0')) to CountdownState.SOON
            }
        }
        val over = -diff
        val d = over / 86_400_000L
        val h = over % 86_400_000L / 3_600_000L
        val m = over % 3_600_000L / 60_000L
        return if (d > 0) I18n.odDays(d, h) to CountdownState.OVERDUE
        else I18n.odHm(h, m.toString().padStart(2, '0')) to CountdownState.OVERDUE
    }

    fun scheduled(habit: Habit, date: LocalDate): Boolean = habit.weekdays.contains(date.dayOfWeek.value)

    /** 当前连续：今天已打卡则计入；今天未打卡不立即断，从昨天起算（均按 Ticka 日） */
    fun currentStreak(habit: Habit): Int {
        var date = tickaDate()
        if (!(scheduled(habit, date) && habit.records.containsKey(keyOf(date)))) {
            date = date.minusDays(1)
        }
        var streak = 0
        while (scheduled(habit, date) && habit.records.containsKey(keyOf(date))) {
            streak++
            date = date.minusDays(1)
        }
        return streak
    }

    /** 历史最长连续（只统计排期内日期；今天未打卡不算断） */
    fun longestStreak(habit: Habit): Int {
        val today = tickaDate()
        var date = localDate(habit.createdAt)
        var best = 0
        var run = 0
        while (!date.isAfter(today)) {
            if (scheduled(habit, date)) {
                if (habit.records.containsKey(keyOf(date))) {
                    run++
                    if (run > best) best = run
                } else if (date.isBefore(today)) {
                    run = 0
                }
            }
            date = date.plusDays(1)
        }
        return best
    }

    fun weekdayNames(weekdays: Set<Int>): String {
        val names = if (I18n.en)
            listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        else
            listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
        if (weekdays.size == 7) return I18n.everyDay
        return (1..7).filter { it in weekdays }.joinToString(" ") { names[it - 1] }
    }
}
