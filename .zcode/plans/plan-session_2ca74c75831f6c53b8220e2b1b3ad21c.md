# Ticka — macOS 待办 & 打卡应用（Electron）

在 `/Users/debugz/Documents/Project/Ticka` 从零搭建。纯 HTML/CSS/JS + ES Modules，无构建步骤，`npm install && npm start` 即可运行。后续 Android 端可用 Capacitor 复用这套 UI/逻辑。

## 功能（按你的需求）

**1. 基础任务（带提醒 + 倒计时）**
- 任务 CRUD：标题、备注、截止时间、提醒时间
- 列表卡片实时显示截止倒计时（"剩 2小时15分"），状态变色：正常(青) → 不足1小时(琥珀) → 已超时(红粉)
- 提醒通知：主进程每 30 秒调度检查，到点发 macOS 系统通知（每个提醒只发一次）
- 筛选：全部 / 进行中 / 已完成；完成任务有划线淡出动画

**2. 番茄钟（专注计时）**
- 从任务卡片的"专注"按钮启动，浮层显示任务名 + 渐变圆环进度（25:00 专注 / 5:00 休息）
- 开始/暂停/重置；结束发系统通知；每个任务累计专注次数显示在卡片上

**3. 每日打卡目标（习惯）**
- 目标 CRUD：名称、渐变配色、打卡星期（默认每天）
- 每日大按钮打卡：玻璃圆环按下后渐变填充 + 辉光 + 对勾动画
- 点开详情：**开始日期、当前连续、历史最长连续、累计完成** + 最近 12 周热力图格子
- 连续天数只按排期内的日子计算

## 设计（galaxy / uiverse 风格：深空 + 玻璃拟态 + 霓虹渐变）

- 背景 `#070714` 深空色 + 靛蓝/青色径向星云光晕
- 玻璃卡片：`rgba(255,255,255,.04)` + 1px `rgba(255,255,255,.08)` 描边 + `backdrop-blur(20px)` + 16px 圆角
- 主渐变 `#6366f1 → #22d3ee`（靛蓝→青）；习惯打卡用 紫→粉 渐变
- 辉光 `box-shadow: 0 0 24px rgba(99,102,241,.35)`；数字用等宽字体
- 无边框窗口 `titleBarStyle: hiddenInset`，自定义红绿灯留白，约 1000×680

## 结构

```
Ticka/
├── package.json / main.js / preload.js     # 主进程：窗口、IPC、JSON 存储、通知调度
└── src/
    ├── index.html
    ├── styles/  base.css(设计令牌) tasks.css habits.css pomodoro.css
    └── js/      app.js store.js tasks.js habits.js pomodoro.js time.js icons.js
```

- 数据存 `~/Library/Application Support/Ticka/data.json`（自写轻量 JSON 存储，不引多余依赖）
- `preload.js` 用 contextBridge 暴露安全 API；浏览器预览时自动降级到 mock 层（便于我用浏览器截图做视觉验证）

## 实施步骤

1. 脚手架：package.json + main.js + preload.js + 无边框深色窗口
2. 设计系统 CSS（令牌、玻璃卡片、渐变按钮、输入框、圆环勾选、弹窗、标签页）
3. 任务模块：CRUD + 倒计时 + 筛选
4. 提醒调度 + 系统通知
5. 打卡模块：CRUD + 每日按钮 + 连续天数 + 详情页（开始日期/最长连续/热力图）
6. 番茄钟浮层：圆环计时 + 通知 + 计数
7. 视觉验证（浏览器截图迭代）→ `npx electron .` 实机运行 → README

本轮只做 macOS 端；Android（Capacitor 打包复用）等 macOS 版验收后再启动。