# NOTES — INITDB テストケース（データベース初期化・初期データ投入）

- **対象アプリ**: INITDB（`Sakura/main/INITDB.cob`）／ SAKURA 販売管理システム
- **アプリ種別**: **バッチ**（`design_evidence.py` 判定: BMS マップなし・SCREEN SECTION を持つメンバーなし。`has_screen_section=false`）
- **REG（真実源）**: `C:\Users\tuan.vu3\AppData\Local\.skr\run-3232\reg`（読み取り専用）
- **設計エビデンス（優先）**:
  - `design_asis/Batch/INITDB_データベース初期化_プログラム詳細設計書.md`
  - `design_asis/Batch/INITDB_データベース初期化_ジョブフロー.md`
- **AST**: `reg/parsed/cobol_xml/Sakura/main/INITDB.xml`（`msg_codes.py`＝0 コード、`crud_from_ast` の C/R/U/D 集計は本 reg レイアウトでは 0 行を返すため AST 直読で根拠付け）
- **成果物言語**: 日本語（スキル日本語版で固定）。逐語コンソールメッセージ「…」、コード識別子（ファイル／項目 ID）、フィクスチャ値（会社名・商品名・住所等）は原文のまま。
- **作成者**: `modernizeX`（固定）。
- **承認（非対話実行）**: 本 NOTES の観点表・カバレッジマトリクスは**承認済みとみなして続行**（NON-INTERACTIVE）。

## プログラム構造の要約（AST / ソース根拠）

INITDB は分岐・入力・パラメータを持たない**直線的なローダーバッチ**である（`IF`/`EVALUATE` は AST に 0 件）。

1. 開始メッセージ表示（`DISPLAY "SAKURA-SMS  INITDB - creating files ..."` L93）。
2. `CREATE-EMPTY-FILES`（L114）: 取引 17 ファイルを `OPEN OUTPUT`→即 `CLOSE`（空で確保）。
3. `LOAD-*`（L137〜）: 16 マスタへハードコードのデモ初期データを投入（静的 `WRITE` 62 本＝実行時 69 レコード）。
4. 完了メッセージ表示（`DISPLAY "SAKURA-SMS  INITDB - complete."` L111）→ `STOP RUN`（RC=0, L112）。

- 全 `WRITE` は `INVALID KEY CONTINUE`（重複キー無視）。`FSTS`（FILE STATUS）は宣言のみで**未チェック**。
- 引数・SYSIN・LINKAGE **なし**（§5.1）。EXEC SQL **なし**（§1.5）。呼出しサブ **なし**（§1.4）。
- **破壊的**: 全ファイル `OPEN OUTPUT` のため既存データは全消失（§1.6）。
- reg にオーケストレーター（JCL/BAT）**なし** → 初回セットアップ時に手動で一度だけ起動。

### 投入レコード内訳（実行時 69 レコード／16 マスタ、AST WRITE 数＋ソース値で確認）

| ファイル（業務名） | 件数 | 代表値（ソース根拠） |
|---|---|---|
| SYSCF システム制御 | 1 | キー=1、SAKURA Trading Co., Ltd.、期首月4、当期202607、税率0.100（L137-154） |
| NUMCF 採番マスタ | 11 | ORDER/SO,INVOICE/IV,SHIP/SH,PO/PO,RECV/RC,PURCH/PU,RECEIPT/RE,PAYMENT/PY,ARLDG/AL,APLDG/PL,STKMOV/SM 各 桁数10・現在値0（L166-233） |
| TAXF 消費税率 | 2 | 1=Standard 10%(0.100), 2=Reduced 8%(0.080)（L237-252） |
| REGNF 地域 | 4 | 1 Kanto,2 Kansai,3 Chubu,4 Kyushu（L263-277） |
| DEPTF 部門 | 3 | 1000 Sales,2000 Purchasing,9000 Administration（L280-292） |
| CATGF 商品分類 | 4 | 100/200/300/400（レベル1）（L296-315） |
| BANKF 銀行 | 3 | 1 MUFG,2 SMBC,3 Mizuho（L319-334） |
| WHSEF 倉庫 | 3 | 1 Main,2 East,3 Returns（L338-356） |
| STAFF 社員 | 4 | 1001/1002/1003/2001（L360-379） |
| USERF ユーザー | 3 | 9999 admin(ロール1),1001 yamada(ロール2),1002 suzuki(ロール3)（L383-409） |
| CUSTF 得意先 | 5 | 100001-100005（L420-469） |
| SUPPF 仕入先 | 4 | 200001-200004（L472-500） |
| PRODF 商品 | 8 | 10000001-10000008、ランク単価(1)〜(5)（L511-640） |
| CPRCF 得意先別単価 | 2 | 100005×10000005=480.00, 100002×10000001=105.00（L643-656） |
| STOKF 在庫残高 | 8 | ループ i=1..8、商品10000000+i、在庫=1000−i×50、倉庫1、ロケーションA-01（L660-675） |
| MSGF メッセージ | 4 | I0001,E0001,E0002,W0001（L679-695） |
| 取引 17 ファイル | 0 | ORDHF/ORDDF/SHPHF/SHPDF/INVHF/INVDF/POHF/PODF/RCVHF/RCVDF/PURHF/PURDF/ARLF/APLF/RCPTF/PAYF/SMOVF（空）（L114-135） |

## 観点表（バッチ基本観点 B1–B12 の判断）— 承認対象

| 観点（中項目） | 適用？ | エビデンス（仕様/AST） | 想定TC |
|---|---|---|---|
| 機能・業務フロー（B3 変換ロジック含む） | ✅ | MAIN L92-112 一連の投入。分岐なし | IT_INITDB_HAPPY_001 |
| 出力・帳票（B4 出力ファイル、列レベル） | ✅ | 16 マスタ＋17 空ファイルの投入内容。§4 出力仕様・各 copybook | HAPPY_004〜020 |
| 計算・編集ロジック | ✅ | STOKF 在庫数 `COMPUTE SK-ONHAND = 1000 − WK-I×50`（L667） | HAPPY_021 |
| データ整合性・冪等性（B5） | ✅ | デモ初期データの相互参照（得意先→地域/社員、商品→分類/仕入先、倉庫→社員）。入力非依存で冪等 | HAPPY_022, HAPPY_023 |
| 連携・インターフェース（B12 スケジュール連携） | ✅ | `jcl_jobs.json=[]`（orchestrator 不在）→手動起動。下流 NUMGEN/CHKLOG 等が参照 | HAPPY_024 |
| 運用 | ✅ | §1.6/§5.2 初回一度だけ・破壊的・事前バックアップは運用責任 | HAPPY_025 |
| 境界値（B9） | ✅ | STOKF ループ 1..8、在庫計算下限、採番現在値0/桁数10、キー桁数、税率小数精度 | BOUNDARY_001〜005 |
| 回復・リラン（B8） | ✅ | 全 `OPEN OUTPUT`＝破壊的再実行。冪等（同一結果） | ABNORMAL_001（＋ HAPPY_023 冪等） |
| メッセージ・異常系（B7 異常終了） | ✅ | 全 WRITE `INVALID KEY CONTINUE`（重複キー無視）／`FSTS` 未チェック→OPEN 失敗時ランタイム ABEND | ABNORMAL_002, ABNORMAL_003 |
| 入力データ（B1） | ❌ — 入力ファイル・SYSIN なし（ハードコード投入）。§1.3 に入力 I/O なし | — |
| パラメータ・制御（B2） | ❌ — 引数/SYSIN/LINKAGE なし（§5.1）。HAPPY_001 で「引数なし起動」を確認 | （HAPPY_001 で言及） |
| 排他・同時実行（B6） | ❌ — 初回セットアップ専用の単独実行。同時実行の想定なし（運用） | — |
| ログ・監査（B10） | ✅ | コンソール 2 メッセージ（L93/L111）＋ RC=0 | HAPPY_002, HAPPY_003 |
| 性能（B11） | ❌ — 実行時／環境依存。投入量は 69 レコードと小規模 | — |
| 権限・セキュリティ | ❌ — INITDB 自体に認証ゲートなし（画面・ログインなし）。投入されるユーザー権限データは HAPPY_013 で確認 | （HAPPY_013 で言及） |

### 密度ルールの適用（DENSITY floor）

- **異常系トリガー点**: 本 PG に EI/EF/GF/ER メッセージコードは **0**（`msg_codes.py`＝0）。異常系は
  コード駆動ではなく、コード化された挙動クラスで分割：破壊的再実行（1）／重複キー INVALID KEY CONTINUE（1）／
  FILE STATUS 未チェック時の OPEN 失敗（1）＝ 3 TC。
- **正常系分類値**: 「分類値」に相当するのは投入先 16 マスタ＋空ファイル群 → 各ファイルに 1 TC（HAPPY_004〜020）。
  USERF の権限区分（ロール1/2/3）は HAPPY_013 で 3 値をカバー。
- **境界値は両側**: STOKF ループ件数（8 ちょうど／9 件目なし）、在庫計算下限（i=8→600）、採番現在値初期0、
  キー桁数（6桁/8桁）、税率小数精度。
- **具体リテラル**: 全 TC の前提/手順/期待にソース由来の具体値を記載（会社名・コード値・件数・計算結果）。

## Fixture（F-STD — 共有フィクスチャブロック）

**F-STD**: 出力先ディレクトリに書込権限があり、初期化対象の索引編成ファイルが未作成、または既存でも上書き
許容の状態。起動引数・SYSIN なし。コンソール（`/Ccon`）から `INITDB` を単独起動。各 TC の前提条件は
「F-STD」＋その差分のみを記す。

## カバレッジマトリクス（承認済みとみなす）

| 観点 | 根拠行 | TC |
|---|---|---|
| 機能・業務フロー | MAIN L92-112 | HAPPY_001 |
| ログ・監査 | DISPLAY L93 / L111・STOP RUN L112 | HAPPY_002, HAPPY_003 |
| 出力・帳票（16 マスタ） | LOAD-SYSTEM〜LOAD-MESSAGE L137-695 | HAPPY_004〜019 |
| 出力・帳票（空 17 ファイル） | CREATE-EMPTY-FILES L114-135 | HAPPY_020 |
| 計算・編集ロジック | COMPUTE L667 | HAPPY_021 |
| データ整合性・冪等性 | 相互参照／入力非依存 | HAPPY_022, HAPPY_023 |
| 連携・インターフェース | jcl_jobs.json=[]・下流参照 | HAPPY_024 |
| 運用 | §1.6/§5.2 | HAPPY_025 |
| 境界値 | L663/L667・NM-CURRENT L172・キー PIC・TX-RATE L243/249 | BOUNDARY_001〜005 |
| 回復・リラン | OPEN OUTPUT ×33（破壊的） | ABNORMAL_001 |
| メッセージ・異常系 | INVALID KEY CONTINUE（全62 WRITE）／FSTS 未チェック | ABNORMAL_002, ABNORMAL_003 |

- カバーしたメッセージコード: 0 件（本 PG は EI/EF/GF/ER を送出しない）／除外コード: 0 件。
- カバーしたジョブ内フェーズ: CREATE-EMPTY-FILES / LOAD-* / 終了（全 3 フェーズ）。
- 除外観点（理由付き）: 入力データ・パラメータ・排他/同時・性能・権限（上表参照）。

## 監査結果

`audit_testcase.py <cases.json> --reg <REG> --root INITDB` の実行結果と各候補の根拠付けは、
本ファイル末尾「監査ログ」に記録する。

### 監査ログ

実行: `audit_testcase.py cases.json --reg <REG> --root INITDB`
結果: **3 CANDIDATE**（すべて根拠付けにより棄却＝false positive）。構造チェック（ID 一意・必須項目・
mid 一貫・自動化列・具体リテラル）は全 33 件クリア。使用観点 10。

| # | 候補 | 種別 | 根拠付け・処置 |
|---|---|---|---|
| 1 | `could not run msg_codes (no reg xml)` | valid | **棄却（パス層差の誤検知）**。監査の `_root_xml` は `reg/parsed/cobol_xml/{main,sub}/INITDB.xml` を探すが、本 reg は `.../cobol_xml/Sakura/main/INITDB.xml`（プロジェクト層 `Sakura` が1段深い）。正しいパスで `msg_codes.py INITDB.xml --json` を実行済み → `{"codes":{}, "stop_literals":[]}` ＝ **メッセージコード 0**。よってカバー漏れ・除外漏れは無い（本 PG は EI/EF/GF/ER を送出しない直線ローダー）。 |
| 2 | `HAPPY_002 'expected' code-centric (SAKURA-SMS)` | wording | **棄却（逐語引用）**。「SAKURA-SMS  INITDB - creating files ...」は画面（コンソール）に表示される逐語メッセージで「」内。スキル規則（逐語引用はそのまま残す）に従い保持。データ名ではない。 |
| 3 | `HAPPY_003 'expected' code-centric (SAKURA-SMS)` | wording | **棄却（逐語引用）**。「SAKURA-SMS  INITDB - complete.」も逐語コンソールメッセージ。同上。 |

判定観点（人手レビュー）:
- 全 期待結果 は引用したソース行（L92-695）・出力仕様（§4）・ジョブフローに辿れる（投入件数・キー値・
  計算結果はソース `MOVE`/`COMPUTE` リテラルと一致）。
- 手順は再現可能（コンソールから引数なしで起動 → 出力ファイル検証）。
- 観点表は仕様に対して完全（B1–B12 を全判断、除外は理由付き）。
- 抜き取り検証3件: HAPPY_005（採番11系列＝L166-233 と一致）／HAPPY_021（在庫計算 950〜600＝COMPUTE L667）／
  ABNORMAL_002（INVALID KEY CONTINUE＝全 WRITE 句）— いずれもソースと一致を確認。
