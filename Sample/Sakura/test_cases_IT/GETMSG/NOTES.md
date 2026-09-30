# GETMSG テストケース設計ノート

- **プロジェクト**: SAKURA 販売管理システム
- **対象アプリ**: GETMSG（メッセージ文言取得 / 共通下位モジュール）
- **アプリ種別**: **バッチ（サブルーチン）** — `design_evidence.py` 判定＝ `batch`（「BMS マップなし、SCREEN SECTION を持つメンバーもなし」）。
- **作成者**: modernizeX ／ 作成日 2026/08/30
- **成果物言語**: 日本語（フィクスチャのデータ値・メッセージコード・コード識別子のみ原文保持）

## 0. エビデンス（真実源）

| 種別 | パス／ツール | 得た事実 |
|---|---|---|
| プログラム詳細設計書 | `design_asis/Batch/GETMSG_メッセージ取得_プログラム詳細設計書.md` | I/O・処理内容・返却レイアウト・チェック仕様 |
| ジョブフロー | `design_asis/Batch/GETMSG_メッセージ取得_ジョブフロー.md` | orchestrator 不在、CALL 起動、分岐図 |
| AST | `reg/parsed/cobol_xml/Sakura/sub/GETMSG.xml` | 全 4 分岐を確認（下記） |
| メッセージコード | `msg_codes.py GETMSG.xml` | **EI/EF/GF/ER コード 0 件・STOP 0 件**（このモジュールは EI/EF 系コードを送出しない） |
| CRUD | `crud_from_ast.py --root GETMSG` | 動詞レベル出力 0（本体は MSGF を OPEN INPUT / READ / CLOSE のみ、read-only） |
| MSGF フィクスチャ | `INITDB.xml` L681-695（LOAD-MSG） | メッセージマスタの実登録値（下表 F-STD） |

### AST が示す完全な処理（GETMSG.xml, MAIN-000）

| 行 | 文 | 意味 |
|---|---|---|
| L31 | `MOVE "00" TO KM-STATUS` | 状態を正常で初期化 |
| L32 | `MOVE SPACE TO KM-TEXT` | 文言を空白で初期化 |
| L33 | `OPEN INPUT MSGF` | マスタを参照用に開く（毎回 OPEN/CLOSE） |
| L34 | `IF FSTS NOT = "00"` | **分岐①** マスタ利用不可 |
| L35-37 | `MOVE KM-CODE TO KM-TEXT` / `MOVE "99" TO KM-STATUS` / `EXIT PROGRAM` | コードを文言として返し 99・即復帰（READ せず） |
| L39 | `MOVE KM-CODE TO MG-CODE` | 照会キー設定 |
| L40 | `READ MSGF ... INVALID KEY` | コードで 1 件照会 |
| L42-43 | `MOVE KM-CODE TO KM-TEXT` / `MOVE "01" TO KM-STATUS` | **分岐②** 該当なし：コードを文言として返し 01 |
| L45 | `MOVE MG-TEXT TO KM-TEXT` | **分岐③** 該当あり：登録文言を返す（状態は 00 のまま） |
| L47-48 | `CLOSE MSGF` / `EXIT PROGRAM` | 閉じて復帰 |

分岐は 3 系統（該当あり=00／該当なし=01／マスタ利用不可=99）。READ の I/O ハードエラー
（INVALID KEY 以外）に対する明示処理は**ソースに存在しない**（READ 後に FSTS 再判定なし）→
デッド／未実装パスにつき TC を書かない（下表 B5 参照）。

## 1. 標準フィクスチャ F-STD

MSGF（メッセージマスタ、索引編成・キー MG-CODE X(6)）に INITDB が投入する実登録レコード
（`INITDB.xml` LOAD-MSG L683-693。値はフィクスチャデータとして原文保持）：

| MG-CODE | MG-TEXT (X(60)) |
|---|---|
| I0001 | Processing completed |
| E0001 | Record not found |
| E0002 | Duplicate key |
| W0001 | Stock shortage |

- 連絡領域 KMSG（copybook KLINK, 68 byte）＝ KM-CODE X(6)｜KM-TEXT X(60)｜KM-STATUS X(2)。
- 各 TC の前提は "F-STD" ＋ 差分のみを記す。
- 実機の 60 バイト最大長メッセージは reg に存在しない → 境界 TC で `*(要実データ)*` と明記。

## 2. 観点表（基本観点の判断 — 非対話実行のため下記のとおり承認済みとして続行）

> **承認前提（NON-INTERACTIVE）**: 本観点表をユーザー承認済みの前提で確定し、ケース生成を続行する。
> バッチ基本カタログ B1–B12（references/viewpoints.md）を全件判断。GETMSG は JCL/BAT 起動ではなく
> `CALL "GETMSG" USING KMSG` の同期呼出しサブルーチンであるため、「1 実行＝1 回の CALL」と読み替える。

| 基本観点 | 適用？ | エビデンス | 想定 中項目（Canonical）／TC |
|---|---|---|---|
| B1 入力データ（分岐ごとフィクスチャ） | ✅ | 該当あり/なし/マスタ不可の 3 分岐（AST L34/L40） | 機能・業務フロー・メッセージ・異常系 → IT_MSG_HAPPY_001-004 / UT_MSG_ABNORMAL_001-002 |
| B2 パラメータ・制御 | ✅ | Linkage KM-CODE（設計 §5.1、AST L24-27） | 入力チェック → UT_MSG_ABNORMAL_003（空白コード） |
| B3 機能・変換ロジック | ✅ | コード→文言変換、非該当時のコード代替（設計 §1.2/§2） | 機能・業務フロー → IT_MSG_HAPPY_001-004 |
| B4 出力ファイル・帳票（列レベル） | ✅ | 返却レイアウト KMSG 68byte（設計 §4.1） | 出力・帳票 → IT_MSG_HAPPY_006 |
| B5 データ整合性 | ✅（一部除外） | 返却領域初期化 L31-32、登録文言のそのまま複写 L45。READ I/O ハードエラー処理は**未実装**→該当 TC 除外 | データ整合性・冪等性 → IT_MSG_HAPPY_007 |
| B6 排他・同時実行 | ❌ — MSGF は `OPEN INPUT`（read-only）、更新なし。毎回 OPEN/CLOSE で状態を持たず、ロック競合の対象外 | AST L33/L47 openMode=INPUT | — |
| B7 異常終了 | ✅ | マスタ利用不可 99・該当なし 01。**ABEND/STOP RUN なし**（フェイルソフト、設計 §1.6） | メッセージ・異常系 → UT_MSG_ABNORMAL_001-002 |
| B8 リラン・リスタート（冪等性） | ✅ | ステートレス、毎回 OPEN/CLOSE → 同一入力で同一出力（AST L33/L47） | データ整合性・冪等性 → IT_MSG_HAPPY_008 |
| B9 境界・大量データ | ✅ | KM-CODE X(6) キー、KM-TEXT X(60)（設計 §4.1） | 境界値 → UT_MSG_BOUNDARY_001-003 |
| B10 ログ・監査 | ❌ — DISPLAY／ログファイル書込なし（AST に該当文なし）。監査対象の副作用を持たない | AST（DISPLAY 0 件） | — |
| B11 性能 | ❌ — N/A（実行時・顧客ボリューム依存）。1 回 1 件参照の下位モジュールで測定対象外 | — | — |
| B12 スケジュール連携 | ✅（連携として） | orchestrator 不在。`CALL "GETMSG" USING KMSG` で各画面・帳票から同期呼出し（設計 ジョブフロー） | 連携・インターフェース → IT_MSG_HAPPY_005 |
| （UI 系 U1–U19） | ❌ — バッチサブルーチンで画面なし（SCREEN SECTION／BMS なし） | design_evidence kind=batch | — |
| 計算・編集ロジック | ❌ — COMPUTE 等の算術・編集加工なし（MOVE のみ） | AST（arithmetic 加工なし） | — |
| 権限・セキュリティ | ❌ — サインオン／権限ゲートなし（下位モジュール） | AST | — |
| 回復・リラン（運用回復） | ❌ — リスタート制御・処理済フラグなし。冪等性は B8 でカバー、フェイルソフト復帰は B7 でカバー | AST | — |
| 運用 | ✅（注記） | 常駐せず毎回 OPEN/CLOSE、単独起動しない（設計 §1.6） | IT_MSG_HAPPY_008 の備考／本 NOTES に記載 |

## 3. カバレッジマトリクス（根拠行 → TC）

| 根拠行（分岐／単位） | エビデンス | TC |
|---|---|---|
| 該当あり・情報コード I0001 | INITDB LOAD-MSG／AST L45 | IT_MSG_HAPPY_001 |
| 該当あり・エラーコード E0001 | 同上 | IT_MSG_HAPPY_002 |
| 該当あり・エラーコード E0002 | 同上 | IT_MSG_HAPPY_003 |
| 該当あり・警告コード W0001 | 同上 | IT_MSG_HAPPY_004 |
| CALL USING KMSG 契約 | ジョブフロー／AST using=KMSG | IT_MSG_HAPPY_005 |
| 返却レイアウト（列レベル） | 設計 §4.1 | IT_MSG_HAPPY_006 |
| 返却領域初期化（L31-32） | AST L31-32 | IT_MSG_HAPPY_007 |
| 冪等性・毎回 OPEN/CLOSE | AST L33/L47 | IT_MSG_HAPPY_008 |
| 該当なし（READ INVALID KEY, L41-43）＝ KM-STATUS 01 | AST L40-43 | UT_MSG_ABNORMAL_001 |
| マスタ利用不可（OPEN 失敗, L34-37）＝ KM-STATUS 99 | AST L34-37 | UT_MSG_ABNORMAL_002 |
| KM-CODE 空白（未設定）→ 該当なし | AST L39-40（キー空白で READ） | UT_MSG_ABNORMAL_003 |
| キー長上限：6 桁全桁使用の未登録コード | 設計 §4.1 KM-CODE X(6) | UT_MSG_BOUNDARY_001 |
| キー末尾空白：5 桁コード＋末尾空白の一致 | 設計 §4.1 KM-CODE X(6) | UT_MSG_BOUNDARY_002 |
| KM-TEXT 最大 60 バイトの切り捨てなし返却 | 設計 §4.1 KM-TEXT X(60) | UT_MSG_BOUNDARY_003 |

**メッセージコード網羅**: EI/EF/GF/ER コードは 0 件（`msg_codes.py` 確認）→ 網羅対象なし。
本モジュールの「判定値」は状態コード KM-STATUS（00／01／99）であり、01・99 に専用の異常系 TC を、
00 に正常系 TC 群を割当済み。除外コードなし。

## 4. 監査結果

`audit_testcase.py cases.json --reg <REG> --root GETMSG` を実行。候補は初回7件→ wording 3件を
修正（PIC/動詞トークンを 備考 へ移動）。残り4件は下記のとおり根拠付けて棄却（false positive）。

| 候補 | 判定 | 根拠 |
|---|---|---|
| `[valid] could not run msg_codes` | 棄却 | 監査プロセスが設計スクリプト／xml を自前解決できなかっただけ。`msg_codes.py sub/GETMSG.xml` を手動実行済み＝**0 コード**。網羅漏れなし。 |
| `[vague] UT_MSG_ABNORMAL_001/002/003 が message code を引用しない` | 棄却 | 本モジュールは EI/EF/GF/ER コードを一切送出しない（msg_codes=0）。判定値は状態コード KM-STATUS（01／99）であり、各 期待結果 に「状態コード＝01」「＝99」と**具体リテラルで引用済み**。監査の正規表現が EI*/STOP 形式のみ検出するための誤検知。 |

wording 修正内訳：UT_MSG_ABNORMAL_002（「OPEN 失敗」→「参照不能」）、UT_MSG_BOUNDARY_001
（「X(6)」→「最大6桁」）、UT_MSG_BOUNDARY_003（「X(60)」→「最大長」）。PIC 等のコード識別子は
備考 列に保持しトレーサビリティを確保。

### 抜き取り再検証（3件）

- **IT_MSG_HAPPY_001**: KM-CODE=I0001 → KM-TEXT=「Processing completed」/ 00。根拠＝ AST L45
  `MOVE MG-TEXT TO KM-TEXT` ＋ INITDB LOAD-MSG L683-684 登録値。一致。✅
- **UT_MSG_ABNORMAL_002**: マスタ利用不可 → KM-TEXT=コード / 99・READ 未実行・ABEND なし。根拠＝
  AST L34-37（IF FSTS NOT="00" → MOVE 99 → EXIT PROGRAM）＋設計 §1.6 フェイルソフト。一致。✅
- **UT_MSG_BOUNDARY_003**: 60バイト文言を切り捨てなく返却。根拠＝設計 §4.1 KM-TEXT/MG-TEXT X(60)。
  reg に60バイト実データなしのため `*(要実データ)*` 明記。仕様整合。✅

## 5. 集計（正直なカウント）

- 合計 **14 TC** — 正常系 8 ／ 異常系 3 ／ 境界値 3。
- カバー：業務分岐 3 系統全て、状態コード 3 値全て、フィクスチャ登録 4 コード全て、両側境界（キー内容・キー長・テキスト長）。
- 除外：B6 排他（read-only）・B10 ログ（副作用なし）・B11 性能（実行時）・UI 系・計算・権限・回復リラン（理由付き）。
- 未確定：60 バイト最大長メッセージは reg に実データなし → UT_MSG_BOUNDARY_003 に `*(要実データ)*`。
