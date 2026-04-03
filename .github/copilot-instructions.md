# Pomotimer — Copilot Context

## Session Bootstrap Instructions (ALWAYS follow these)

1. **At the start of every session**, check if `.copilot-local.md` exists in the repository root.
   - If it **exists**: read it immediately and silently incorporate its content (progress, pending tasks, version info, notes) before responding to the user.
   - If it **does not exist**: acknowledge that no local memo was found and proceed normally. At the end of the session (or when the user indicates they are done), create `.copilot-local.md` with a summary of what was done (see format below).

2. **During the session**, update `.copilot-local.md` incrementally as work progresses — after each meaningful task the user requests. Keep it current with:
   - Latest version info
   - What was just completed
   - What is pending / next steps
   - Any important technical decisions made

3. **Never commit `.copilot-local.md` to Git** — it is listed in `.gitignore` and is for local state only.
   - When starting work in a **different repository**, always check that `.copilot-local.md` is listed in that repo's `.gitignore`. If it is not, add it before creating the file.

4. **Anti-hallucination rule (strictly enforce)**:
   - Only state facts that are grounded in actual source code, documentation, or information explicitly provided in this session.
   - Do **not** invent API names, library versions, behavior, or configuration details from memory alone.
   - If you are uncertain about something (e.g., an Android API behavior, a library version, a Gradle option), **ask the user to provide a source** (URL, official docs page, or reference name) rather than guessing.
   - When referencing external information, prefer the official documentation (developer.android.com, kotlinlang.org, etc.) and state where the information comes from.

### `.copilot-local.md` format (maintain this structure)
```markdown
# Pomotimer — Local Session Memo

## Current Version
- versionCode: X / versionName: X.X.X
- Latest release tag: vX.X.X

## Recent Work Log
| Version | What was done |
|---|---|
| vX.X.X | ... |

## Pending / Next Tasks
- [ ] item

## Environment
- Local repo: /home/admin/Pomotimer/
- Android SDK: API 36
- Recommended JDK: GraalVM 25

## Notes for Copilot
- ...
```

---

## Project Overview
Android Pomodoro timer app built with Jetpack Compose + Material3.
Supports background operation, lock-screen notifications, work logs, and color themes.

## Tech Stack
| Category | Details |
|---|---|
| UI | Jetpack Compose + Material3 |
| Architecture | MVVM (ViewModel + StateFlow) |
| Background | LifecycleService (Foreground Service) |
| Database | Room 2.8.4 |
| Preferences | DataStore Preferences 1.2.1 |
| Navigation | Navigation Compose 2.9.7 |
| Build | Gradle 9.4.1 + AGP 8.13.2 + Kotlin 2.2.0 |
| Min SDK | Android 7.0 (API 24) |
| Target SDK | Android 16 (API 36) |
| JDK | 17+ (recommended: GraalVM 25) |

## File Structure
```
app/src/main/java/com/example/pomodoro/
├── data/
│   ├── AppDatabase.kt         # Room DB
│   ├── WorkLog.kt             # Work log entity
│   ├── WorkLogDao.kt          # DAO
│   └── SettingsRepository.kt  # DataStore wrapper
├── model/
│   └── TimerState.kt          # Timer state data class
├── service/
│   └── TimerService.kt        # Foreground service (timer, notification, sound, vibration)
├── ui/
│   ├── theme/AppTheme.kt      # 6 themes + custom theme
│   ├── PomotimerApp.kt        # NavHost + BottomNav
│   ├── TimerScreen.kt         # Timer screen
│   ├── WorkLogScreen.kt       # Work log screen
│   └── SettingsScreen.kt      # Settings screen (with credit section)
├── viewmodel/
│   └── TimerViewModel.kt      # ViewModel (delegates to service + DataStore)
└── MainActivity.kt
```

## Current Version
- versionCode: 4 / versionName: 1.3.1
- Latest release: v1.3.1

## Implemented Features
- 3-mode auto-switching: Work / Short break / Long break
- Background operation via foreground service
- **Lock-screen notification** (VISIBILITY_PUBLIC on channel + notification)
- Pause / resume / stop from notification actions
- Alarm sound + vibration on timer end
- Work log (Room DB) with date navigation
- 6 preset themes + custom theme (#RRGGBB input)
- Author credit in Settings screen (tappable GitHub link)

## Notification Channels
| ID | Name | Purpose |
|---|---|---|
| `timer_progress` | Timer progress | Remaining time + action buttons (VISIBILITY_PUBLIC) |
| `timer_alert` | Timer alert | Session end alert |

## Dev Notes
- `TimerService._uiState` is a `MutableStateFlow` in companion object (singleton)
- `SettingsRepository` uses DataStore (not SharedPreferences)
- Room migration is not configured — use `fallbackToDestructiveMigration` on schema change
- Release APK is unsigned (sideload only)
- No GitHub Actions / CI configured yet

## Common Commands
```bash
./gradlew assembleDebug    # debug build
./gradlew assembleRelease  # release build
./gradlew clean assembleRelease  # clean build
# APK output: app/build/outputs/apk/release/
# Release: gh release create vX.X.X <apk> --title "..." --notes "..."
```

## Repository
https://github.com/warasugitewara/Pomotimer
