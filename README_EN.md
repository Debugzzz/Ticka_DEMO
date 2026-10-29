<div align="center">
<img src="art/icon.png" width="180" alt="Ticka" />

# Ticka ✦

**To-dos · Focus · Daily Check-ins · Star Collecting**

A deep-space, glassmorphism Android productivity app: every focus session or completed task catches a random star — collect a whole night sky of your own.

English · [简体中文](README.md)

`Kotlin` `Jetpack Compose` `Material 3` `minSdk 26` `targetSdk 36`

</div>

---

## ✨ Features

### To-dos
- Full CRUD: title, notes, due time
- Live countdown with color states: normal (cyan) → near (amber pulse) → overdue (red)
- **Alarm-grade reminders**: exact alarms + alarm-ringtone playback (bypasses vendor channel quirks) + strong vibration + full-screen launch on lock screen
- Quick presets (5/30 min, 1 h, 1 day) + custom lead time (days/hours/minutes steppers)
- Long-press a card to delete

### Pomodoro
- 25-min focus + 5-min break with a glowing gradient ring
- Completion notification (with sound) and 🍅 counter per task

### Goals (daily check-ins)
- Custom name, gradient palette, weekly schedule
- Big check-in button; detail page with **start date, current streak, longest streak, total check-ins** and a 12-week heatmap
- Streaks only count scheduled days; missing today won't break the chain immediately

### 🌌 Star Collecting (Forest's trees → Ticka's stars)
- Every completed focus / first-completed to-do / daily check-in → catch a random star
- **16 star kinds, 4 rarity tiers**: Rare 50% (6 kinds, 8.3% each) · Epic 30% (5 kinds, 6% each) · Legendary 20% (5 kinds, 4% each)
- Dust, Radiant, Flame, Twin, Supernova, Comet, Aurora, Ringed World, Diamond Star, Pulsar, Nebula, Eclipse Star, Corona Star, Prism Star, Crescent Star, Crystal Star — most with twinkle/flow/rotation animations
- No-overlap star placement; un-checking revokes the star, re-doing restores the exact same one
- **Swipe between skies**; skies auto-archive at capacity (configurable) and a fresh night begins; **export any sky as an image** to your gallery
- Collected stars scatter behind the To-dos & Goals pages (toggle in Settings)

### Sync & Data
- **SSH/SFTP sync**: server (with port) + username + password + path; one-tap upload/restore across devices
- **Export all data** as JSON and import on any device
- Clear all data (double confirmation)

### Misc
- 🌐 **In-app language switch** (Chinese default / English), including notifications and toasts
- Daily checkpoint at **3:00 AM**: check-ins between 0:00–3:00 count as the previous day

## 🎨 Design

Deep-space theme: `#070714` base + nebula glows and star specks, glass cards, indigo→cyan gradient `#6366F1→#22D3EE`, neon glow. Inspired by [uiverse.io/galaxy](https://github.com/uiverse-io/galaxy).

## 📥 Download

Grab the prebuilt APK: [releases/Ticka-v0.1.0.apk](releases/Ticka-v0.2.0.apk?raw=true) (~12MB, debug-signed)

Or build it yourself:

## 📦 Build

```bash
git clone https://github.com/Debugzzz/Ticka_DEMO.git
cd Ticka
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Requires JDK 17+ and Android SDK Platform 36.

## 🛠 Tech Stack

- Kotlin 2.2 · Jetpack Compose (BOM 2025.06.01) · Material 3
- Single Activity + two tabs + bottom sheets; no Room / Hilt / navigation libs
- Persistence: kotlinx-serialization JSON (atomic writes)
- Reminders: exact AlarmManager + in-app fallback + boot rescheduling; alarm sound via MediaPlayer on the alarm stream
- Sync: JSch (SFTP); export via SAF
- Icon: Adaptive Icon (bitmap foreground + monochrome layer)

## 📄 Data Shape

```jsonc
{
  "tasks":  [{ "id", "title", "notes", "dueAt", "remindBeforeMin", "remindAt", "completed", "pomodoros", "starId", ... }],
  "habits": [{ "id", "name", "palette", "weekdays", "createdAt", "records": { "2026-10-26": 1791... }, "starIds": { ... } }],
  "stars":  [{ "id", "kind", "source", "sourceName", "obtainedAt", "x", "y", "revoked" }],
  "archivedSkies": [{ "id", "stars": [...], "archivedAt" }],
  "settings": { "host", "username", "password", "path", "lang", "skyBackdrop", "skyCapacity", ... }
}
```

## 🗺 Roadmap

- [ ] macOS companion app (same design language)
- [ ] SSH key authentication
- [ ] Deep-link notifications to the matching to-do

## License

MIT

---

<div align="center">
Turn every day into a star ✦
</div>
