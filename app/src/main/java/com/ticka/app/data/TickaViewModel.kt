package com.ticka.app.data

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ticka.app.notify.AlarmPlayer
import com.ticka.app.notify.ReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.Executors
import kotlin.random.Random

class TickaViewModel(app: Application) : AndroidViewModel(app) {

    private val store = Store(app)

    /** 所有磁盘写入都排到这个单线程，绝不并发写文件 */
    private val storeExecutor = Executors.newSingleThreadExecutor()

    private val _data = MutableStateFlow(AppData())
    val data: StateFlow<AppData> = _data

    /** 每秒心跳，驱动倒计时 */
    val now = MutableStateFlow(System.currentTimeMillis())

    /** 刚获得的星星（非 null 时 UI 显示获得浮层） */
    private val _lastAwarded = MutableStateFlow<Star?>(null)
    val lastAwarded: StateFlow<Star?> = _lastAwarded

    /** 全局轻提示（Toast） */
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    init {
        _data.value = migrateSync(store.load())
        I18n.lang = _data.value.settings.lang
        rescheduleAll()
        viewModelScope.launch {
            while (true) {
                delay(1000)
                now.value = System.currentTimeMillis()
                checkInAppReminders()
            }
        }
    }

    private fun persist() {
        val snapshot = _data.value
        storeExecutor.execute { store.save(snapshot) }
    }

    /** 旧版单一 serverUrl 自动拆分为 主机 + 路径 */
    private fun migrateSync(data: AppData): AppData {
        val s = data.settings
        if (s.host.isBlank() && s.serverUrl.isNotBlank()) {
            var rest = s.serverUrl.trim()
            val proto = when {
                rest.startsWith("https://") -> { rest = rest.removePrefix("https://"); "https://" }
                rest.startsWith("http://") -> { rest = rest.removePrefix("http://"); "http://" }
                else -> "http://"
            }
            val slash = rest.indexOf('/')
            val host = proto + if (slash >= 0) rest.substring(0, slash) else rest
            val path = if (slash >= 0) rest.substring(slash) else ""
            return data.copy(settings = s.copy(host = host, path = path, serverUrl = ""))
        }
        return data
    }

    fun showMessage(msg: String) { _message.value = msg }
    fun consumeMessage() { _message.value = null }

    /** 关闭获得星星浮层 */
    fun consumeAward() { _lastAwarded.value = null }

    fun setLang(l: String) {
        I18n.lang = l
        _data.update { it.copy(settings = it.settings.copy(lang = l)) }
        persist()
    }

    // ---------- 待办 ----------

    fun upsertTask(task: Task) {
        _data.update { d ->
            d.copy(tasks = if (d.tasks.any { it.id == task.id })
                d.tasks.map { if (it.id == task.id) task else it }
            else d.tasks + task)
        }
        reschedule(task)
        persist()
    }

    fun toggleTask(id: String) {
        val task = _data.value.tasks.find { it.id == id } ?: return
        if (!task.completed) {
            // 完成：恢复此前收回的星，或首次授星
            var starId = task.starId
            val existing = starId?.let { sid -> _data.value.stars.find { it.id == sid } }
            if (existing != null) {
                setStarRevoked(existing.id, false)
            } else {
                starId = createStar(1, task.title).id
            }
            _data.update { d ->
                d.copy(tasks = d.tasks.map {
                    if (it.id == id) it.copy(completed = true, completedAt = System.currentTimeMillis(), starId = starId) else it
                })
            }
            persist()
        } else {
            // 取消完成：收回星星
            task.starId?.let { setStarRevoked(it, true) }
            _data.update { d ->
                d.copy(tasks = d.tasks.map {
                    if (it.id == id) it.copy(completed = false, completedAt = null) else it
                })
            }
            persist()
        }
    }

    fun deleteTask(id: String) {
        _data.update { d -> d.copy(tasks = d.tasks.filterNot { it.id == id }) }
        ReminderScheduler.cancel(getApplication(), id)
        persist()
    }

    fun addPomodoro(id: String) {
        _data.update { d ->
            d.copy(tasks = d.tasks.map { if (it.id == id) it.copy(pomodoros = it.pomodoros + 1) else it })
        }
        persist()
    }

    // ---------- 目标 ----------

    fun upsertHabit(habit: Habit) {
        _data.update { d ->
            d.copy(habits = if (d.habits.any { it.id == habit.id })
                d.habits.map { if (it.id == habit.id) habit else it }
            else d.habits + habit)
        }
        persist()
    }

    fun toggleHabitToday(id: String) {
        val habit = _data.value.habits.find { it.id == id } ?: return
        val key = DayUtils.todayKey()
        if (habit.records.containsKey(key)) {
            // 取消打卡：收回今天的星星
            habit.starIds[key]?.let { setStarRevoked(it, true) }
            _data.update { d ->
                d.copy(habits = d.habits.map {
                    if (it.id == id) it.copy(records = it.records - key, starIds = it.starIds - key) else it
                })
            }
            persist()
        } else {
            // 打卡：连续的每一天都有一颗星；取消后重打恢复同一颗
            var newStar: Star? = null
            val existing = habit.starIds[key]?.let { sid -> _data.value.stars.find { it.id == sid } }
            if (existing != null) {
                setStarRevoked(existing.id, false)
            } else {
                newStar = createStar(2, habit.name)
            }
            _data.update { d ->
                d.copy(habits = d.habits.map {
                    if (it.id == id) {
                        val starIds = if (newStar != null) it.starIds + (key to newStar.id) else it.starIds
                        it.copy(records = it.records + (key to System.currentTimeMillis()), starIds = starIds)
                    } else it
                })
            }
            persist()
        }
    }

    fun deleteHabit(id: String) {
        _data.update { d -> d.copy(habits = d.habits.filterNot { it.id == id }) }
        persist()
    }

    // ---------- 星星 ----------

    private fun createStar(source: Int, sourceName: String): Star {
        // 星空已满：自动封存，开启新夜空
        val capacity = _data.value.settings.skyCapacity.coerceIn(10, 99)
        if (_data.value.stars.filter { !it.revoked }.size >= capacity) {
            archiveCurrentSky()
        }
        // 落位防重叠：多次采样取与现有星星间距最大的位置（x 按画布宽高比加权）
        val placed = _data.value.stars.filter { !it.revoked }
        var bestX = 0.5f
        var bestY = 0.5f
        var bestDist = -1f
        for (attempt in 0 until 80) {
            val x = 0.06f + Random.nextFloat() * 0.88f
            val y = 0.12f + Random.nextFloat() * 0.76f
            var dist = 1f
            for (s in placed) {
                val dx = (x - s.x) * 1.3f
                val dy = y - s.y
                dist = minOf(dist, kotlin.math.sqrt(dx * dx + dy * dy))
            }
            if (dist > bestDist) {
                bestDist = dist
                bestX = x
                bestY = y
            }
            if (bestDist >= 0.13f) break
        }
        val kind = rollStarKind()
        val star = Star(
            id = UUID.randomUUID().toString(),
            kind = kind,
            source = source,
            sourceName = sourceName.trim().ifBlank { I18n.sourceDefault(source) },
            obtainedAt = System.currentTimeMillis(),
            x = bestX,
            y = bestY
        )
        _data.update { d -> d.copy(stars = d.stars + star) }
        _lastAwarded.value = star
        persist()
        return star
    }

    /** 番茄钟授星，返回种类名 */
    fun awardStar(source: Int, sourceName: String): String =
        starKinds[createStar(source, sourceName).kind].name

    private fun setStarRevoked(id: String, revoked: Boolean) {
        _data.update { d ->
            d.copy(stars = d.stars.map { if (it.id == id) it.copy(revoked = revoked) else it })
        }
        persist()
    }

    /** 封存当前星空（内部方法，不做空星空校验） */
    private fun archiveCurrentSky() {
        val kept = _data.value.stars.filter { !it.revoked }
        if (kept.isEmpty()) return
        val archive = StarArchive(
            id = UUID.randomUUID().toString(),
            stars = kept,
            archivedAt = System.currentTimeMillis()
        )
        _data.update { d ->
            d.copy(archivedSkies = d.archivedSkies + archive, stars = emptyList())
        }
    }

    /** 手动封存当前星空，开启新的夜空 */
    fun archiveSky() {
        val kept = _data.value.stars.filter { !it.revoked }
        if (kept.isEmpty()) {
            showMessage(I18n.skyEmpty)
            return
        }
        archiveCurrentSky()
        persist()
        showMessage(I18n.archivedMsg(kept.size))
    }

    // ---------- 设置 / 数据 ----------

    /** 解析 "10.19.29.140:2222" → 主机 + 端口（默认 22）；容忍误填的 http:// 前缀 */
    private fun splitHost(host: String): Pair<String, Int> {
        var h = host.trim()
            .removePrefix("https://")
            .removePrefix("http://")
            .trimEnd('/')
        if (h.contains("://")) h = h.substringAfterLast("://")
        return if (h.contains(":")) {
            val parts = h.split(":")
            parts[0] to (parts.getOrNull(1)?.toIntOrNull() ?: 22)
        } else h to 22
    }

    /** 远端保存路径：空 = 家目录下 ticka.json */
    private fun remotePath(s: Settings): String {
        val p = s.path.trim()
        return p.ifEmpty { "ticka.json" }
    }

    /** 远端路径若是已存在的目录，落到其下 ticka.json */
    private fun resolveRemotePath(sftp: com.jcraft.jsch.ChannelSftp, s: Settings): String {
        val p = remotePath(s)
        return runCatching {
            val attrs = sftp.stat(p)
            if (attrs.isDir) p.trimEnd('/') + "/ticka.json" else p
        }.getOrElse { p }
    }

    private fun sftpConnect(s: Settings): Pair<com.jcraft.jsch.Session, com.jcraft.jsch.ChannelSftp> {
        val (h, port) = splitHost(s.host)
        val jsch = com.jcraft.jsch.JSch()
        val session = jsch.getSession(s.username, h, port)
        session.setPassword(s.password)
        // 个人服务器 + 局域网场景：不做主机指纹校验
        session.setConfig("StrictHostKeyChecking", "no")
        session.connect(10_000)
        val sftp = session.openChannel("sftp") as com.jcraft.jsch.ChannelSftp
        sftp.connect(10_000)
        return session to sftp
    }

    private fun sftpPut(s: Settings, payload: String) {
        var session: com.jcraft.jsch.Session? = null
        var sftp: com.jcraft.jsch.ChannelSftp? = null
        try {
            val (se, ch) = sftpConnect(s)
            session = se; sftp = ch
            val path = resolveRemotePath(sftp, s)
            // 绝对路径时逐级尝试创建父目录（已存在则忽略）
            val dir = path.substringBeforeLast('/', "")
            if (dir.startsWith("/")) {
                var cur = ""
                dir.split('/').filter { it.isNotBlank() }.forEach { seg ->
                    cur += "/$seg"
                    runCatching { sftp.mkdir(cur) }
                }
            }
            sftp.put(payload.byteInputStream(), path, com.jcraft.jsch.ChannelSftp.OVERWRITE)
        } finally {
            runCatching { sftp?.exit() }
            runCatching { session?.disconnect() }
        }
    }

    private fun sftpGet(s: Settings): String {
        var session: com.jcraft.jsch.Session? = null
        var sftp: com.jcraft.jsch.ChannelSftp? = null
        try {
            val (se, ch) = sftpConnect(s)
            session = se; sftp = ch
            val out = java.io.ByteArrayOutputStream()
            sftp.get(resolveRemotePath(sftp, s), out)
            return out.toString("UTF-8")
        } finally {
            runCatching { sftp?.exit() }
            runCatching { session?.disconnect() }
        }
    }

    fun updateSyncConfig(host: String, username: String, password: String, path: String) {
        // SSH 不需要协议前缀，顺手剥掉
        val cleanedHost = host.trim()
            .removePrefix("https://")
            .removePrefix("http://")
            .trimEnd('/')
        _data.update { d ->
            d.copy(settings = d.settings.copy(
                host = cleanedHost,
                username = username.trim(),
                password = password,
                path = path.trim(),
                serverUrl = ""
            ))
        }
        persist()
        showMessage(I18n.cfgSaved)
    }

    fun uploadToServer() {
        val s = _data.value.settings
        if (s.host.isBlank()) {
            showMessage(I18n.needServer)
            return
        }
        if (s.username.isBlank()) {
            showMessage(I18n.needUser)
            return
        }
        // 上传的数据剥离密码，不在服务器留存明文凭证
        val payload = tickaJson.encodeToString(
            AppData.serializer(),
            _data.value.copy(settings = _data.value.settings.copy(password = ""))
        )
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { sftpPut(s, payload) }
                .onSuccess {
                    _data.update { d ->
                        d.copy(settings = d.settings.copy(lastSyncAt = System.currentTimeMillis()))
                    }
                    persist()
                    showMessage(I18n.uploaded)
                }
                .onFailure { showMessage(I18n.uploadFail(it.message ?: "")) }
        }
    }

    fun restoreFromServer() {
        val s = _data.value.settings
        if (s.host.isBlank()) {
            showMessage(I18n.needServer)
            return
        }
        if (s.username.isBlank()) {
            showMessage(I18n.needUser)
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val body = sftpGet(s)
                tickaJson.decodeFromString(AppData.serializer(), body)
            }.onSuccess { remote ->
                I18n.lang = remote.settings.lang
                _data.value.tasks.filter { !it.completed }
                    .forEach { ReminderScheduler.cancel(getApplication(), it.id) }
                // 备份不含密码：若本机已存密码而备份为空，保留本机凭证
                val merged = if (remote.settings.password.isBlank() && _data.value.settings.password.isNotBlank())
                    remote.copy(settings = remote.settings.copy(password = _data.value.settings.password))
                else remote
                _data.value = merged
                rescheduleAll()
                persist()
                showMessage(
                    I18n.restoredMsg(
                        remote.tasks.size, remote.habits.size,
                        remote.stars.size, remote.archivedSkies.size
                    )
                )
            }.onFailure { showMessage(I18n.restoreFail(it.message ?: "")) }
        }
    }

    fun exportTo(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                // 导出文件同样剥离密码
                val safe = _data.value.copy(settings = _data.value.settings.copy(password = ""))
                context.contentResolver.openOutputStream(uri)?.use {
                    it.write(tickaJson.encodeToString(AppData.serializer(), safe).toByteArray(Charsets.UTF_8))
                } ?: error("无法写入所选位置")
            }.onSuccess { showMessage(I18n.exported) }
                .onFailure { showMessage(I18n.exportErr(it.message ?: "")) }
        }
    }

    fun importFrom(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
                    ?: error("无法读取所选文件")
                tickaJson.decodeFromString(AppData.serializer(), text)
            }.onSuccess { imported ->
                I18n.lang = imported.settings.lang
                _data.value.tasks.filter { !it.completed }
                    .forEach { ReminderScheduler.cancel(getApplication(), it.id) }
                val merged = if (imported.settings.password.isBlank() && _data.value.settings.password.isNotBlank())
                    imported.copy(settings = imported.settings.copy(password = _data.value.settings.password))
                else imported
                _data.value = merged
                rescheduleAll()
                persist()
                showMessage(
                    I18n.importedMsg(
                        imported.tasks.size, imported.habits.size,
                        imported.stars.size + imported.archivedSkies.sumOf { it.stars.size }
                    )
                )
            }.onFailure { showMessage(I18n.importErr(it.message ?: "格式错误")) }
        }
    }

    fun clearAllData() {
        val ctx = getApplication<Application>()
        _data.value.tasks.forEach { ReminderScheduler.cancel(ctx, it.id) }
        val settings = _data.value.settings
        _data.value = AppData(settings = settings)
        persist()
        store.clearBackups()
        showMessage(I18n.cleared)
    }

    fun setSkyBackdrop(enabled: Boolean) {
        _data.update { d -> d.copy(settings = d.settings.copy(skyBackdrop = enabled)) }
        persist()
    }

    fun setSkyCapacity(n: Int) {
        _data.update { d -> d.copy(settings = d.settings.copy(skyCapacity = n.coerceIn(10, 99))) }
        persist()
    }

    // ---------- 提醒 ----------

    private fun reschedule(task: Task) {
        val ctx = getApplication<Application>()
        if (!task.completed && !task.remindFired && task.remindAt != null && task.remindAt > System.currentTimeMillis()) {
            ReminderScheduler.schedule(ctx, task.id, task.remindAt)
        } else {
            ReminderScheduler.cancel(ctx, task.id)
        }
    }

    fun rescheduleAll() {
        _data.value.tasks.forEach { reschedule(it) }
    }

    /** 应用前台期间的兜底提醒（系统闹钟可能不精确） */
    private fun checkInAppReminders() {
        val nowMs = now.value
        val due = _data.value.tasks.filter {
            !it.completed && !it.remindFired && it.remindAt != null && it.remindAt <= nowMs
        }
        if (due.isEmpty()) return
        _data.update { d ->
            d.copy(tasks = d.tasks.map { t ->
                if (due.any { it.id == t.id }) t.copy(remindFired = true) else t
            })
        }
        due.forEach {
            ReminderScheduler.notifyNow(getApplication(), "⏰ ${it.title}", I18n.notifyTodoBody)
        }
        AlarmPlayer.play(getApplication(), 8L)
        persist()
    }
}
