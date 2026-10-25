package com.ticka.app.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** 应用内语言：zh（默认）/ en，全部界面文案从这里取 */
object I18n {
    var lang by mutableStateOf("zh")
    val en get() = lang == "en"

    // ---------- 导航 ----------
    val navTodos get() = if (en) "To-dos" else "待办"
    val navGoals get() = if (en) "Goals" else "目标"
    val navSky get() = if (en) "Sky" else "星空"
    val navSettings get() = if (en) "Settings" else "设置"

    // ---------- 通用 ----------
    val save get() = if (en) "Save" else "保存"
    val cancel get() = if (en) "Cancel" else "取消"
    val delete get() = if (en) "Delete" else "删除"
    val ok get() = if (en) "OK" else "确定"

    // ---------- 待办 ----------
    val todos get() = if (en) "To-dos" else "待办"
    val newTodo get() = if (en) "New To-do" else "新待办"
    val editTodo get() = if (en) "Edit To-do" else "编辑待办"
    val filterAll get() = if (en) "All" else "全部"
    val filterActive get() = if (en) "Active" else "进行中"
    val filterDone get() = if (en) "Done" else "已完成"
    val emptyUniverse get() = if (en) "The universe is still empty" else "宇宙还很空旷"
    val emptyAddFirst get() = if (en) "Add your first to-do and light up the sky" else "添加第一个待办，点亮你的星图"
    val emptyNoTodo get() = if (en) "Nothing here" else "这里没有待办"
    val emptyTryFilter get() = if (en) "Try another filter" else "换一个筛选条件看看"
    val titleField get() = if (en) "Title" else "标题"
    val notesField get() = if (en) "Notes" else "备注"
    val titlePlaceholder get() = if (en) "What needs doing?" else "要做什么？"
    val notesPlaceholder get() = if (en) "Extra details (optional)" else "补充细节（可选）"
    val dueTime get() = if (en) "Due" else "截止时间"
    val pickDate get() = if (en) "Pick date" else "选择日期"
    val remind get() = if (en) "Reminder" else "提醒"
    val remindOff get() = if (en) "Off" else "不提醒"
    val remindDue get() = if (en) "At due" else "截止时"
    fun remindQuick(mins: Int) = when (mins) {
        5 -> if (en) "5 min before" else "提前5分"
        30 -> if (en) "30 min before" else "提前30分"
        60 -> if (en) "1 h before" else "提前1时"
        1440 -> if (en) "1 day before" else "提前1天"
        else -> if (en) "Before…" else "提前"
    }
    val remindCustom get() = if (en) "Custom…" else "提前…"
    val remindPickerTitle get() = if (en) "Remind me before" else "提前多久提醒"
    val unitDays get() = if (en) "Days" else "天"
    val unitHours get() = if (en) "Hours" else "小时"
    val unitMinutes get() = if (en) "Minutes" else "分钟"
    val hintNeedDue get() = if (en) "Set a due time first to get reminders" else "提醒需要先设置截止时间"
    fun hintBefore(txt: String) = if (en) "Reminds $txt before due" else "截止前 $txt 提醒你"
    val hintAtDue get() = if (en) "Reminds at due time" else "到截止时间时提醒你"
    val hintOff get() = if (en) "No reminders" else "不会提醒"
    val deleteTodoQ get() = if (en) "Delete this to-do?" else "删除这个待办？"
    fun deleteTodoBody(t: String) = if (en) "「$t」will be deleted. This cannot be undone." else "「$t」将被删除，无法恢复。"

    /** 提前量文案：x天x小时x分 */
    fun advance(mins: Int): String {
        val d = mins / 1440
        val h = mins % 1440 / 60
        val m = mins % 60
        val parts = mutableListOf<String>()
        if (d > 0) parts += if (en) "${d}d" else "${d}天"
        if (h > 0) parts += if (en) "${h}h" else "${h}小时"
        if (m > 0) parts += if (en) "${m}m" else "${m}分"
        return parts.joinToString(" ").ifEmpty { if (en) "0m" else "0分" }
    }

    // ---------- 倒计时 ----------
    fun cdDays(d: Long, h: Long) = if (en) "in ${d}d ${h}h" else "剩 ${d}天${h}小时"
    fun cdHm(h: Long, mm: String) = if (en) "in ${h}:$mm" else "剩 ${h}:$mm"
    fun cdMs(mm: String, ss: String) = if (en) "in $mm:$ss" else "剩 $mm:$ss"
    fun odDays(d: Long, h: Long) = if (en) "overdue ${d}d ${h}h" else "超时 ${d}天${h}小时"
    fun odHm(h: Long, mm: String) = if (en) "overdue ${h}:$mm" else "超时 ${h}:$mm"

    // ---------- 番茄钟 ----------
    val focus get() = if (en) "F O C U S" else "专 注"
    val brk get() = if (en) "B R E A K" else "休 息"
    val start get() = if (en) "Start" else "开始"
    val pause get() = if (en) "Pause" else "暂停"
    val reset get() = if (en) "Reset" else "重置"
    val exit get() = if (en) "Exit" else "退出"
    fun focusDoneTitle() = if (en) "🍅 Focus complete" else "🍅 专注完成"
    fun focusDoneBody(title: String, star: String) =
        if (en) "「$title」+1 🍅 · ✨ got a $star"
        else "「$title」+1 🍅 · ✨ 摘得一颗$star"
    val breakOverTitle get() = if (en) "☕ Break over" else "☕ 休息结束"
    val backToFocus get() = if (en) "Back to focus" else "回来继续专注吧"

    // ---------- 目标 ----------
    val goals get() = if (en) "Goals" else "目标"
    val newGoal get() = if (en) "New Goal" else "新目标"
    val editGoal get() = if (en) "Edit Goal" else "编辑目标"
    val noGoals get() = if (en) "No daily goals yet" else "还没有每日目标"
    val noGoalsSub get() = if (en) "Start one small thing you can do every day" else "立一个每天都能完成的小目标"
    fun dayN(n: Long) = if (en) "Day $n" else "第 $n 天"
    val notScheduledToday get() = if (en) "Not scheduled today" else "今天不排程"
    val daysStreak get() = if (en) "day streak" else "天连续"
    val deleteGoalQ get() = if (en) "Delete this goal?" else "删除这个目标？"
    fun deleteGoalBody(n: String) = if (en) "「$n」and its check-in history will be deleted. This cannot be undone." else "「$n」及其打卡记录将被删除，无法恢复。"
    val nameField get() = if (en) "Name" else "名称"
    val namePlaceholder get() = if (en) "e.g. Read 30 minutes" else "每天坚持的事，如：阅读 30 分钟"
    val palette get() = if (en) "Color" else "配色"
    val repeatOn get() = if (en) "Repeat" else "重复"
    val everyDay get() = if (en) "Every day" else "每天"

    // ---------- 目标详情 ----------
    val currentStreakL get() = if (en) "Current streak" else "当前连续"
    val longestStreakL get() = if (en) "Longest streak" else "最长连续"
    val totalDone get() = if (en) "Total check-ins" else "累计完成"
    val startDateL get() = if (en) "Started" else "开始日期"
    val daysKept get() = if (en) "Days in" else "已坚持"
    val last12w get() = if (en) "Last 12 weeks" else "最近 12 周"
    val weekly get() = if (en) "Weekly: " else "每周："
    val doneTodayL get() = if (en) "Checked in today ✓" else "今日已打卡 ✓"
    val checkTodayL get() = if (en) "Complete today's check-in" else "完成今天的打卡"
    val offScheduleL get() = if (en) "Not scheduled today" else "今天不在排期内"
    fun streakHint(n: Int) = if (en) "$n-day streak — keep the flame alive" else "已连续 $n 天，别断了火焰"
    val deleteGoalBtn get() = if (en) "Delete goal" else "删除目标"

    // ---------- 星空 ----------
    val sky get() = if (en) "Sky" else "星空"
    fun skiesTotal(n: Int) = if (en) "$n skies" else "共 $n 片星图"
    fun todayPlus(n: Int) = if (en) "+$n today" else "今日 +$n"
    val swipeHint get() = if (en) "Swipe · " else "左右滑动切换 · "
    fun pageCurrent(i: Int) = if (en) "sky $i" else "当前第 $i 片"
    val collectingTag get() = if (en) " (collecting)" else "（收集中）"
    val archivedTag get() = if (en) " (archived)" else "（已封存）"
    val testStar get() = if (en) "＋ Test star" else "＋ 测试星星"
    val exportImage get() = if (en) "Export image" else "导出星图图片"
    val savedGallery get() = if (en) "Saved to gallery Pictures/Ticka" else "已保存到相册 Pictures/Ticka"
    val generatedShare get() = if (en) "Image created" else "已生成星图图片"
    fun opFail(e: String) = if (en) "Failed: $e" else "导出失败：$e"
    fun skyFullT(n: Int, cap: Int) = if (en) "This sky holds $n stars (capacity $cap)" else "这片星空已有 $n 颗星（容量 $cap）"
    val skyFullS get() = if (en) "Archive it as a memory, or raise capacity in Settings" else "封存留作纪念，或到设置里调高容量"
    val archiveBtn get() = if (en) "Archive & new" else "封存新启"
    val archiveQ get() = if (en) "Archive this sky?" else "封存这片星空？"
    fun archiveBody(n: Int) = if (en) "These $n stars will be saved under past skies; a brand-new night begins." else "现有 $n 颗星将完整保存到「往期星空」，随时回看；之后从一片全新的夜空开始收集。"
    val collection get() = if (en) "Collection" else "星图鉴"
    val locked get() = if (en) "Locked" else "未解锁"
    fun skyN(n: Int) = if (en) "Past sky · $n stars" else "往期星空 · $n 颗星"
    fun archiveLine(num: Int, n: Int, date: String) = if (en) "Sky #$num · $n stars · archived $date" else "第 $num 片 · $n 颗星 · 封存于 $date"
    val emptySky1 get() = if (en) "Finish a focus or a check-in" else "完成一次专注或打卡"
    val emptySky2 get() = if (en) "to catch your first star" else "摘下你的第一颗星"

    // ---------- 设置 ----------
    val settings get() = if (en) "Settings" else "设置"
    val syncSection get() = if (en) "Remote sync" else "远程同步"
    val syncDesc get() = if (en)
        "Sync over SSH/SFTP: upload writes a JSON file to your server, restore reads it and replaces local data. Empty path = ticka.json in the home directory."
    else
        "通过 SSH/SFTP 同步到你的服务器：上传=写入远端 JSON 文件，恢复=读取并覆盖本机。用户名密码为 SSH 登录凭证；路径为空时保存在家目录 ticka.json。"
    val serverAddr get() = if (en) "Server" else "服务器地址"
    val usernameL get() = if (en) "Username" else "用户名"
    val passwordL get() = if (en) "Password" else "密码"
    val pathL get() = if (en) "Path" else "保存路径"
    val phServer get() = if (en) "10.19.29.140 or 10.19.29.140:2222" else "10.19.29.140 或 10.19.29.140:2222"
    val phUser get() = if (en) "SSH username" else "SSH 登录用户名"
    val phPass get() = if (en) "SSH password" else "SSH 登录密码"
    val phPath get() = if (en) "ticka.json or /root/backup/ticka.json" else "ticka.json 或 /root/backup/ticka.json"
    val saveCfg get() = if (en) "Save config" else "保存配置"
    val restore get() = if (en) "Restore" else "恢复"
    val upload get() = if (en) "Upload" else "上传"
    fun lastSync(t: String) = if (en) "Last sync: $t" else "上次同步：$t"
    val neverSync get() = if (en) "Never synced" else "从未同步"
    val appearance get() = if (en) "Appearance" else "外观"
    val language get() = if (en) "Language" else "语言"
    val skyBackdrop get() = if (en) "Sky backdrop" else "星图背景"
    val skyBackdropDesc get() = if (en) "Scatter your collected stars behind To-dos & Goals" else "在待办与目标页的背景中散布你收集的星星"
    val skyCapacity get() = if (en) "Sky capacity" else "星空容量"
    fun skyCapacityDesc(n: Int) = if (en) "Auto-archive at $n stars and start a new sky" else "收集满 $n 颗自动封存，开启新夜空"
    val dataSection get() = if (en) "Data" else "数据"
    val exportAll get() = if (en) "Export all data" else "导出所有数据"
    val exportSub get() = if (en) "JSON backup · import on any device" else "JSON 备份 · 可跨设备导入"
    val importData get() = if (en) "Import data" else "导入数据"
    val importSub get() = if (en) "Restore from JSON (overwrites local)" else "从 JSON 备份恢复（覆盖本机）"
    val clearAll get() = if (en) "Clear all data" else "清空所有数据"
    val clearSub get() = if (en) "To-dos / Goals / Stars / Settings" else "待办 / 目标 / 星星 / 设置"
    val about get() = if (en) "About" else "关于"
    fun aboutStats(stars: Int, skies: Int) = if (en) "$stars stars shining · $skies skies archived" else "正在点亮 $stars 颗星 · 已封存 $skies 片星空"
    val settle3am get() = if (en) "Daily checkpoint: 3:00 AM" else "每日结算点：凌晨 3 点"
    val version get() = "Ticka v0.1.0"

    // ---------- 确认弹窗 ----------
    val restoreQ get() = if (en) "Restore from server?" else "从服务器恢复？"
    val restoreBody get() = if (en) "This will overwrite ALL local data (to-dos, goals, stars, settings)." else "将用服务器上的数据覆盖本机全部数据（待办、目标、星星、设置），确定继续？"
    val restoreBtn get() = if (en) "Overwrite" else "覆盖恢复"
    val importQ get() = if (en) "Import this backup?" else "导入这份备份？"
    val importBody get() = if (en) "This will overwrite ALL local data (to-dos, goals, stars, skies and settings)." else "将用备份文件覆盖本机全部数据（待办、目标、星星、星空与设置），确定继续？"
    val importBtn get() = if (en) "Overwrite & import" else "覆盖导入"
    val clearQ get() = if (en) "Clear all data?" else "清空所有数据？"
    val clearBody get() = if (en) "All to-dos, goals, check-ins, stars and archived skies will be deleted. Export a backup first!" else "所有待办、目标、打卡记录、星星与往期星空都会被删除，无法恢复。建议先导出备份。"
    val clearBtn get() = if (en) "Delete everything" else "全部清空"

    // ---------- Toast ----------
    val cfgSaved get() = if (en) "Sync config saved" else "同步配置已保存"
    val needServer get() = if (en) "Fill in and save the server address first" else "请先填写并保存服务器地址"
    val needUser get() = if (en) "SSH username required" else "请填写 SSH 用户名"
    val uploaded get() = if (en) "Uploaded to server" else "已上传到服务器"
    fun uploadFail(e: String) = if (en) "Upload failed: $e" else "上传失败：$e"
    fun restoredMsg(n1: Int, n2: Int, n3: Int, n4: Int) =
        if (en) "Restored ($n1 to-dos · $n2 goals · $n3 stars · $n4 skies)"
        else "已从服务器恢复（$n1 待办 · $n2 目标 · $n3 星 · $n4 片星空）"
    fun restoreFail(e: String) = if (en) "Restore failed: $e" else "恢复失败：$e"
    val exported get() = if (en) "Backup exported" else "已导出备份"
    fun exportErr(e: String) = if (en) "Export failed: $e" else "导出失败：$e"
    fun importedMsg(n1: Int, n2: Int, n3: Int) = if (en) "Imported ($n1 to-dos · $n2 goals · $n3 stars)" else "已导入（$n1 待办 · $n2 目标 · $n3 颗星）"
    fun importErr(e: String) = if (en) "Import failed: $e" else "导入失败：$e"
    val cleared get() = if (en) "All data cleared" else "已清空所有数据"
    fun archivedMsg(n: Int) = if (en) "Archived $n stars — a new sky begins" else "已封存 $n 颗星，新的夜空开始了"
    val skyEmpty get() = if (en) "This sky is empty" else "这片星空还是空的"

    // ---------- 通知 ----------
    val channelName get() = if (en) "Reminders" else "提醒"
    val channelDesc get() = if (en) "To-do due reminders & focus timer notifications" else "待办截止提醒与专注计时通知"
    val notifyTodoBody get() = if (en) "To-do reminder" else "待办提醒"
    val notifyOverdue get() = if (en) "Due time reached" else "已到截止时间"
    val notifyNearDue get() = if (en) "Due time is close" else "截止时间临近"
    val backToFocusTitle get() = if (en) "☕ Break over" else "☕ 休息结束"

    // ---------- 星星种类 ----------
    private val zhNames = listOf("尘星", "辉星", "焰星", "双子星", "超新星", "彗星", "极光星", "星环星", "钻石星", "脉冲星", "星云", "黑曜星", "花冠星", "棱星", "月痕星", "冰晶星")
    private val enNames = listOf("Dust Star", "Radiant Star", "Flame Star", "Twin Star", "Supernova", "Comet", "Aurora", "Ringed World", "Diamond Star", "Pulsar", "Nebula", "Eclipse Star", "Corona Star", "Prism Star", "Crescent Star", "Crystal Star")
    private val zhRarity = listOf("稀有", "稀有", "稀有", "稀有", "稀有", "稀有", "传说", "史诗", "传说", "传说", "史诗", "传说", "史诗", "传说", "史诗", "传说")
    private val enRarity = listOf("Rare", "Rare", "Rare", "Rare", "Rare", "Rare", "Legendary", "Epic", "Legendary", "Legendary", "Epic", "Legendary", "Epic", "Legendary", "Epic", "Legendary")

    fun starName(k: Int) = if (en) enNames[k.coerceIn(0, 15)] else zhNames[k.coerceIn(0, 15)]
    fun starRarity(k: Int) = if (en) enRarity[k.coerceIn(0, 15)] else zhRarity[k.coerceIn(0, 15)]
    fun awarded(k: String) = if (en) "✨ Got $k" else "✨ 获得 $k"
    fun fromSrc(src: String) = if (en) "from \"$src\"" else "来自「$src」"
    fun sourceDefault(source: Int) = when (source) {
        0 -> if (en) "Focus" else "专注"
        2 -> if (en) "Goal" else "目标"
        3 -> if (en) "Test" else "测试"
        else -> if (en) "To-do" else "待办"
    }
}
