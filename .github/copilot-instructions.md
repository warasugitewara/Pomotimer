# Pomotimer — Copilot コンテキスト

## プロジェクト概要
Android 向けポモドーロタイマーアプリ。Jetpack Compose + Material3 で構築。
バックグラウンド動作・通知・作業ログ・カラーテーマ切替に対応。

## 技術スタック
| カテゴリ | 内容 |
|---|---|
| UI | Jetpack Compose + Material3 |
| アーキテクチャ | MVVM (ViewModel + StateFlow) |
| バックグラウンド | LifecycleService (フォアグラウンドサービス) |
| DB | Room 2.8.4 |
| 設定保存 | DataStore Preferences 1.2.1 |
| 画面遷移 | Navigation Compose 2.9.7 |
| ビルド | Gradle 9.4.1 + AGP 8.13.2 + Kotlin 2.2.0 |
| 最小 SDK | Android 7.0 (API 24) |
| ターゲット SDK | Android 16 (API 36) |
| JDK | 17以上（推奨: GraalVM 25） |

## ファイル構成
```
app/src/main/java/com/example/pomodoro/
├── data/
│   ├── AppDatabase.kt         # Room DB
│   ├── WorkLog.kt             # 作業ログエンティティ
│   ├── WorkLogDao.kt          # DAO
│   └── SettingsRepository.kt  # DataStore ラッパー
├── model/
│   └── TimerState.kt          # タイマー状態
├── service/
│   └── TimerService.kt        # フォアグラウンドサービス（タイマー・通知・音・振動）
├── ui/
│   ├── theme/AppTheme.kt      # 6テーマ + カスタムテーマ
│   ├── PomotimerApp.kt        # NavHost + BottomNav
│   ├── TimerScreen.kt         # タイマー画面
│   ├── WorkLogScreen.kt       # 作業ログ画面
│   └── SettingsScreen.kt      # 設定画面
├── viewmodel/
│   └── TimerViewModel.kt      # VM（サービス委譲 + DataStore）
└── MainActivity.kt
```

## 現在のバージョン
- versionCode: 4 / versionName: 1.3.1
- 最新リリース: v1.3.1

## 実装済み主要機能
- 作業 / 短休憩 / 長休憩の3モード自動切替
- フォアグラウンドサービスによるバックグラウンド動作
- **ロック画面通知対応**（VISIBILITY_PUBLIC）
- 通知からの再開 / 一時停止 / 停止操作
- タイマー終了時のアラーム音 + バイブレーション
- 作業ログ（Room DB）・日付ナビゲーション
- 6種テーマ + カスタムテーマ（#RRGGBB 入力）
- 設定画面に作者クレジット（タップで GitHub 表示）

## 通知チャンネル
| ID | 名前 | 用途 |
|---|---|---|
| `timer_progress` | タイマー進行 | 残り時間・操作ボタン（VISIBILITY_PUBLIC） |
| `timer_alert` | タイマー終了通知 | セッション終了アラート |

## 開発上の注意
- `TimerService` の `_uiState` は companion object の `MutableStateFlow`（シングルトン）
- `SettingsRepository` は DataStore を使用（SharedPreferences ではない）
- Room の migration は未設定（スキーマ変更時は `fallbackToDestructiveMigration` を使用）
- リリース APK は署名なし（サイドロード用）
- GitHub Actions / CI は未設定

## よく使うコマンド
```bash
# デバッグビルド
./gradlew assembleDebug

# リリースビルド
./gradlew assembleRelease

# クリーンビルド
./gradlew clean assembleRelease
```

## リポジトリ
https://github.com/warasugitewara/Pomotimer
