# 基本ルール
- **ユーザーへの応答は必ず日本語**で行うこと
- ソースのない情報は使わないこと。不明な点はユーザーにURLや資料名を求めること
- コミットの主著者は常に `warasugitewara` とすること（Co-authored-by は可）
- README はプロジェクトの変化に合わせて逐一更新すること

# セッション引き継ぎ
- セッション開始時に `.copilot-local.md` があれば読み込み、内容を引き継ぐこと
- 作業中はユーザーの指示に応じて `.copilot-local.md` を逐次更新すること（バージョン・完了内容・次のタスクを記載）
- セッション終了時に `.copilot-local.md` がなければ、作業内容を要約して作成すること
- `.copilot-local.md` は絶対にコミットしないこと。他リポジトリで使う際も `.gitignore` に追加してから作成すること

---

# プロジェクト参照情報

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
