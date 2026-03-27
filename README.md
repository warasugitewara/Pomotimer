# 🍅 Pomotimer

ポモドーロ・テクニックに基づいた Android 集中管理タイマーアプリです。

<p align="center">
  <img src="app/src/main/res/mipmap-xxhdpi/ic_launcher.png" width="96" alt="Pomotimer Icon"/>
</p>

---

## 📱 機能一覧

### ⏱ タイマー
- 作業時間・短休憩・長休憩の 3 モード自動切替
- バックグラウンドでも動作（フォアグラウンドサービス）
- ラップ数・完了ポモドーロ数・本日の作業時間をリアルタイム表示

### 🔔 通知
| 状態 | 表示 |
|------|------|
| 通知を折りたたんだとき | プログレスバーで残り時間を視覚化 |
| 通知を展開したとき | 残り時間・ラップ数・操作ボタン（再開 / 停止） |
| タイマー終了時 | プッシュ通知＋「アラームを停止」ボタン |

- タイマー終了時のアラーム音をアプリ内バナー or 通知ボタンで即時停止可能

### ⏰ ポモドーロサイクル
- 短休憩と長休憩を自動判定（デフォルト: 4 回ごとに長休憩）
- 長休憩の間隔・時間は設定画面でカスタマイズ可能

### 📋 作業ログ
- 作業・休憩セッションを自動記録（Room データベース）
- 日付ナビゲーション（← 前の日 | 日付 | 次の日 →）で過去のログを遡れる
- ログの個別削除・日単位削除・全削除（確認ダイアログ付き）

### 🎨 カラーテーマ
6 種類のプリセット + カスタムテーマに対応:

| テーマ | 特徴 |
|--------|------|
| ライト | 明るく清潔感のある標準テーマ |
| ダーク | 目に優しい暗色テーマ |
| Solarized ライト | 暖色ベースの Solarized 配色 |
| Solarized ダーク | 深い青緑をベースにした Solarized 配色 |
| Monokai | コードエディタで人気のダーク配色 |
| Nord | 北欧インスパイアの落ち着いた配色 |
| カスタム | 背景・テキスト・アクセント色を `#RRGGBB` で自由設定 |

### ⚙️ 設定
- **プッシュ通知** ON/OFF
- **通知音** ON/OFF
- **バイブレーション** ON/OFF
- **カラーテーマ** 選択＋カスタムカラー設定
- **タイマー時間** 作業 / 短休憩 / 長休憩 / 長休憩間隔

---

## 🏗 技術スタック

| カテゴリ | 使用技術 |
|----------|----------|
| UI | Jetpack Compose + Material3 |
| アーキテクチャ | MVVM (ViewModel + StateFlow) |
| バックグラウンド | LifecycleService (フォアグラウンドサービス) |
| データベース | Room 2.8.4 |
| 設定の永続化 | DataStore Preferences 1.2.1 |
| 画面遷移 | Navigation Compose 2.9.7 |
| ビルドツール | Gradle 9.4.1 + AGP 8.13.2 + Kotlin 2.2.0 |
| 最小 SDK | Android 7.0 (API 24) |
| ターゲット SDK | Android 16 (API 36) |

---

## 📦 リリース

最新の APK は [Releases](https://github.com/warasugitewara/Pomotimer/releases) からダウンロードできます。

| バージョン | 主な変更 |
|-----------|----------|
| [v1.2.0](https://github.com/warasugitewara/Pomotimer/releases/tag/v1.2.0) | 長休憩・バイブ・6 テーマ＋カスタム・アラーム停止・作業ログ管理・ドット絵アイコン |
| [v1.1.0](https://github.com/warasugitewara/Pomotimer/releases/tag/v1.1.0) | バックグラウンド動作・通知・作業ログ・設定画面 |
| [v1.0.0](https://github.com/warasugitewara/Pomotimer/releases/tag/v1.0.0) | 初回リリース |

---

## 🚀 ビルド方法

```bash
# リポジトリをクローン
git clone https://github.com/warasugitewara/Pomotimer.git
cd Pomotimer

# デバッグ APK をビルド
./gradlew assembleDebug
```

ビルドには Android SDK (API 36) と JDK 17 以上が必要です。

---

## 📂 プロジェクト構成

```
app/src/main/java/com/example/pomodoro/
├── data/
│   ├── AppDatabase.kt        # Room データベース
│   ├── WorkLog.kt            # 作業ログエンティティ
│   ├── WorkLogDao.kt         # DAO（日付別クエリ・削除）
│   └── SettingsRepository.kt # DataStore ラッパー
├── model/
│   └── TimerState.kt         # タイマー状態データクラス
├── service/
│   └── TimerService.kt       # フォアグラウンドサービス（タイマー・通知・音声・振動）
├── ui/
│   ├── theme/
│   │   └── AppTheme.kt       # 6 テーマ定義 + PomotimerTheme
│   ├── PomotimerApp.kt       # NavHost + BottomNavigation
│   ├── TimerScreen.kt        # タイマー画面
│   ├── WorkLogScreen.kt      # 作業ログ画面
│   └── SettingsScreen.kt     # 設定画面
├── viewmodel/
│   └── TimerViewModel.kt     # ViewModel（サービス委譲 + DataStore）
└── MainActivity.kt
```

---

## 📄 ライセンス

[LICENSE](LICENSE) を参照してください。
