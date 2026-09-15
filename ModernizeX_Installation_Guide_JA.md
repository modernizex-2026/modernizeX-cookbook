<img src="installation_assets/app-icon.png" width="72" align="left" style="margin-right:16px" />

# ModernizeX — インストールガイド（macOS）

**COBOL → Java モダナイゼーション プラットフォーム** · ビルド `dev-621e257`

<br clear="left" />

> 本書は、`ModernizeX-dev-621e257.pkg` パッケージから macOS に **ModernizeX** アプリケーションをインストールする手順を説明します。本書のスクリーンショットはすべて、実際のインストール作業から直接撮影したものです。

> 🎬 **操作動画（約64秒）:** [`ModernizeX_Installation_Guide.mp4`](ModernizeX_Installation_Guide.mp4) — インストール全手順を要約したスライドショーです（英語ナレーション+BGM付き、英語字幕）。

---

## 1. 概要

ModernizeX は、レガシー COBOL システムを解析・リバースエンジニアリングし、Java へ変換するソースコード モダナイゼーション プラットフォームです。インストーラーは以下を設定します。

| コンポーネント | 説明 |
|---|---|
| **ModernizeX バックエンド** | **LaunchAgent** として動作するバックグラウンドサービス（API ポート `3438`、Web UI ポート `3439`） |
| **システムトレイ・コントローラー** | サービスの開始／停止や Web UI の起動を行う **メニューバー** アイコン |
| **組み込み JRE（OpenJDK 21.0.7）** | **Apple Silicon（arm64）と Intel（x86_64）の両方**に対応した Java を同梱 — 別途の Java インストールは不要 |

---

## 2. システム要件

| 項目 | 要件 |
|---|---|
| OS | **macOS 13.0（Ventura）以降** |
| CPU アーキテクチャ | Apple Silicon（M シリーズ）または Intel — いずれも対応 |
| 空き容量 | 約 **430 MB** |
| 権限 | **管理者**アカウント（インストール時にパスワードが必要） |
| ネットワークポート | `3438` と `3439` が空いていること |
| Java | **不要** — パッケージに同梱済み |

---

## 3. インストール前 — Gatekeeper での許可

`dev-621e257` パッケージは **未署名（unsigned）の社内ビルド**です。初回起動時、macOS の Gatekeeper により *「開発元を検証できないため開けません」* / *「悪意のあるソフトウェアが検出されなかったことを Apple で検証できません」* などのメッセージが表示され、ブロックされます。

以下の**いずれか一方の方法**で許可してください。

**方法 A — 右クリック（推奨・最速）:**
1. Finder で `ModernizeX-dev-621e257.pkg` ファイルを **Control キーを押しながらクリック（右クリック）** します。
2. **開く** を選択 → 警告ダイアログでもう一度 **開く** をクリックします。

**方法 B — システム設定から:**
1. `.pkg` ファイルをダブルクリックします（macOS で初回はブロックされます）。
2. **システム設定 →「プライバシーとセキュリティ」** を開きます。
3. **セキュリティ** セクションまでスクロールし、「ModernizeX-dev-621e257.pkg」に関する項目を見つけて **このまま開く** をクリックします。
4. Touch ID またはパスワードで認証し、次のダイアログで **開く** をクリックします。

> 💡 一度許可すれば、以降のインストール手順は通常どおり表示されます。

---

## 4. インストール手順

### ステップ 1 — はじめに

インストーラーが起動し、インストール内容の概要を示すようこそ画面が表示されます。**続行** をクリックします。

<img src="installation_assets/01-introduction.png" width="620" />

---

### ステップ 2 — 使用許諾

**ソフトウェア利用許諾契約**（**ModernizeX 商用ライセンス**、© 2026 ModernizeX Organization）を確認します。**続行** をクリックします。

<img src="installation_assets/02-license.png" width="620" />

確認ダイアログが表示されます。**同意する** をクリックして続行します。

<img src="installation_assets/03-license-agree.png" width="620" />

---

### ステップ 3 — インストールの種類

インストールサイズ（約 **428 MB**）とインストール先ボリューム（*Macintosh HD*）が表示されます。これは標準インストールです。**インストール** をクリックします。

<img src="installation_assets/04-installation-type.png" width="620" />

> ℹ️ アプリケーションは常に `/Applications/ModernizeX.app` にインストールされます（場所は変更できません）。

---

### ステップ 4 — 管理者認証

macOS は `/Applications` へのファイル書き込みと LaunchAgent の登録のために管理者権限を要求します。求められたら:

- **Touch ID** で認証する、**または**
- **管理者のユーザー名とパスワード**を入力し → **ソフトウェアをインストール** をクリックします。

> 🔒 *最近認証済みの場合や Touch ID を使う場合、この手順はすぐに完了することがあります。そのため本書には認証手順のスクリーンショットはありません。*

---

### ステップ 5 — インストール中

プログレスバーが *準備中 → 設定中 → ファイルの書き込み中 → パッケージスクリプトの実行中 → コンポーネントの登録中* の各段階を進みます。所要時間は約 **30〜60 秒**です。

<img src="installation_assets/05-installing.png" width="620" />

*パッケージスクリプトの実行中* の段階で、インストーラーは自動的に次を行います。
- 設定ファイル `installer.properties` を書き込みます。
- **2 つの LaunchAgent**（バックエンド + トレイ）をインストール・登録します。
- **トレイ・コントローラー**（メニューバーアイコン）を起動します。

---

### ステップ 6 — 概要（完了）

緑色のチェックマークとともに **「インストールが完了しました。」** と表示されます。**閉じる** をクリックしてインストーラーを閉じます。

<img src="installation_assets/06-success.png" width="620" />

> `.pkg` ファイルをゴミ箱に移動するか尋ねられたら、任意で選択してください（**残す** を選択しても問題ありません）。

---

## 5. インストール後

### 5.1. メニューバーのアイコン

インストール直後、**♾️ ModernizeX** アイコンが **メニューバー**（画面右上）に表示されます。

<img src="installation_assets/07-menubar-tray.png" width="300" />

### 5.2. サービス制御メニュー

**♾️ ModernizeX アイコンをクリック** すると、制御メニューが開きます。

<img src="installation_assets/08-tray-menu.png" width="380" />

| メニュー項目 | 機能 |
|---|---|
| **サービスを開始** | バックエンドサービスを開始（実行中は淡色表示） |
| **サービスを停止** | サービスを停止 |
| **サービスを再起動** | サービスを再起動 |
| **ブラウザで開く (:3439)** | ブラウザで Web UI を開く |
| **ログイン時に自動起動** | ログイン時の自動起動をオン／オフ |
| **終了** | トレイ・コントローラーを終了 |

> 既定ではサービスは起動時に**自動実行されません**（`auto.start.on.boot=false`）。バックエンドがまだ動いていない場合は、先に **サービスを開始** をクリックしてください。

### 5.3. Web UI を開く

**ブラウザで開く (:3439)** をクリックすると、ModernizeX の **ダッシュボード** が `http://localhost:3439` で開きます。

<img src="installation_assets/09-webui-dashboard.png" width="820" />

以上で、ModernizeX の利用準備が整いました。🎉

---

## 6. インストールの確認

インストールが成功したことを確認するには、**ターミナル** で次のコマンドを実行します。

```bash
# 1) アプリが /Applications に存在する
ls -d /Applications/ModernizeX.app

# 2) 2 つの LaunchAgent が登録されている（実行中なら PID を表示）
launchctl list | grep modernizex
#   →  com.edx.modernizex        (backend)
#   →  com.edx.modernizex-tray   (tray)

# 3) バックエンドがポート 3438 と 3439 で待ち受けている
lsof -nP -iTCP:3438 -sTCP:LISTEN
lsof -nP -iTCP:3439 -sTCP:LISTEN

# 4) Web UI が HTTP 200 を返す
curl -s -o /dev/null -w "%{http_code}\n" http://localhost:3439/
```

---

## 7. インストール後のファイルの場所

| コンポーネント | パス |
|---|---|
| アプリケーション | `/Applications/ModernizeX.app` |
| 設定 | `~/Library/Application Support/ModernizeX/config/installer.properties` |
| プロジェクトデータ | `~/Library/Application Support/ModernizeX/data` |
| ログ | `~/Library/Application Support/ModernizeX/logs/`（`backend.log`、`tray.log`、`installer.log`） |
| LaunchAgent（バックエンド） | `~/Library/LaunchAgents/com.edx.modernizex.plist` |
| LaunchAgent（トレイ） | `~/Library/LaunchAgents/com.edx.modernizex-tray.plist` |

---

## 8. トラブルシューティング

| 症状 | 対処方法 |
|---|---|
| **.pkg ファイルが開けない**（「開発元が未確認」） | **第 3 章**（Gatekeeper）に従ってください。右クリック →「開く」、または システム設定 →「プライバシーとセキュリティ」→ *このまま開く*。 |
| **メニューバーにアイコンが表示されない** | トレイを再起動します: `launchctl kickstart -k gui/$(id -u)/com.edx.modernizex-tray` — もしくは `/Applications/ModernizeX.app` を開きます。 |
| **Web UI が開かない／白い画面** | バックエンドが動作していることを確認します。トレイメニューで **サービスを開始** をクリックし、`backend.log` を確認します。 |
| **サービスが起動しない** | ポート `3438`/`3439` が他のアプリに使われている可能性があります。`lsof -nP -iTCP:3439 -sTCP:LISTEN` で確認し、ログ `~/Library/Application Support/ModernizeX/logs/backend-err.log` を参照します。 |
| **インストールログの確認** | `cat "~/Library/Application Support/ModernizeX/logs/installer.log"` |

---

## 9. アンインストール

ModernizeX を完全に削除するには、**ターミナル** で次のコマンドを実行します。

```bash
UID_=$(id -u)

# 1) 2 つの LaunchAgent を停止・登録解除する
launchctl bootout gui/$UID_/com.edx.modernizex 2>/dev/null
launchctl bootout gui/$UID_/com.edx.modernizex-tray 2>/dev/null

# 2) LaunchAgent の plist ファイルを削除する
rm -f ~/Library/LaunchAgents/com.edx.modernizex.plist
rm -f ~/Library/LaunchAgents/com.edx.modernizex-tray.plist

# 3) アプリケーションを削除する
rm -rf /Applications/ModernizeX.app

# 4)（任意）設定・データ・ログを削除する
#    ⚠️ 移行済みのプロジェクトデータを残したい場合は、この手順をスキップしてください
rm -rf ~/Library/Application\ Support/ModernizeX
```

> 元のアンインストーラー（`preuninstall.sh`）は、削除前に **データを残すか削除するかを確認** します。プロジェクトデータは `~/Library/Application Support/ModernizeX/data` にあります。

---

## 10. バージョン情報

| 属性 | 値 |
|---|---|
| パッケージ名 | `ModernizeX-dev-621e257.pkg` |
| バンドル ID | `com.edx.modernizex` |
| バージョン | `dev-621e257` |
| 組み込み JRE | OpenJDK **21.0.7**（arm64 + x86_64） |
| バックエンド／フロントエンド ポート | `3438` / `3439` |
| ライセンス | ModernizeX 商用ライセンス — © 2026 ModernizeX Organization（[LICENSE](LICENSE) 参照） |
| 最小 macOS | 13.0 |

---

*本書は `ModernizeX-dev-621e257.pkg` インストールパッケージ向けに作成されました。スクリーンショットは macOS インストーラーから直接撮影しています。*
