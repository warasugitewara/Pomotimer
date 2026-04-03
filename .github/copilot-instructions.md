# プロジェクト参照情報 — Pomotimer

## 概要
Android ポモドーロタイマーアプリ。Jetpack Compose + Material3。

## 技術スタック
| カテゴリ | 内容 |
|---|---|
| UI | Jetpack Compose + Material3 |
| アーキテクチャ | MVVM (ViewModel + StateFlow) |
| バックグラウンド | LifecycleService (フォアグラウンドサービス) |
| DB | Room 2.8.4 / DataStore Preferences 1.2.1 |
| ビルド | Gradle 9.4.1 + AGP 8.13.2 + Kotlin 2.2.0 |
| SDK | min 24 (Android 7.0) / target 36 (Android 16) |

## 現在のバージョン
- versionCode: 4 / versionName: 1.3.1 / 最新リリース: v1.3.1

## 主要ファイル
```
service/TimerService.kt     # タイマー・通知・音・振動
ui/SettingsScreen.kt        # 設定画面（クレジットあり）
ui/TimerScreen.kt           # タイマー画面
ui/WorkLogScreen.kt         # 作業ログ画面
data/SettingsRepository.kt  # DataStore ラッパー
```

## よく使うコマンド
```bash
./gradlew assembleRelease  # APK: app/build/outputs/apk/release/
gh release create vX.X.X <apk> --title "..." --notes "..."
```

## リポジトリ
https://github.com/warasugitewara/Pomotimer
