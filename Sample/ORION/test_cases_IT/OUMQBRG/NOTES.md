# NOTES — Test-case set for OUMQBRG (VSAM-to-MQ Transaction Bridge)

- **App:** OUMQBRG (app_028) — ORION-CCMS (ORION Credit Card Management System)
- **Kind:** **Batch** (data-driven run) — see classification decision below.
- **REG (ground truth):** `/Users/thangnguyen/Library/Caches/.skr/run-90298/reg`
- **Design evidence (read-only):** `.../ORION/design_asis/Batch/OUMQBRG_MQTransactionBridge_ProgramDesign.md` + `.../Batch/RUNMQBRG_MQBridgeRun_JobFlow.md`
- **OUT:** `.../analysis_output/datastore/design_runs/test_cases/OUMQBRG`
- **Deliverable language:** English (fixed by skill edition). Verbatim console DISPLAY diagnostics and code identifiers are kept in the 備考/Remark column for traceability.
- **Author:** modernizeX

## Classification decision (evidence, not root_type — and NOT the evidence-script's global BMS verdict)

`design_evidence.py OUMQBRG` returns `kind: "ui"` with reason *"the reg has a BMS map"*. That is a **global false positive**: `reg_says_ui()` returns UI whenever `bms_maps.json` is non-empty for the *whole* reg — and this CardDemo-style project has 41 BMS maps for its `OC*/OD*` CICS screens — so it labels **every** app UI regardless of the app itself.

The **app-specific** evidence all says **batch**, and I follow it:

| Evidence | Value | Source |
|---|---|---|
| Own BMS map | none (`bms_maps: []`) | apps.json app_028 |
| `has_screen_section` | `false` | programs.json OUMQBRG |
| CICS commands | 0 (`cics_command_count: 0`) | programs.json |
| Linkage fields | 0 (`linkage_field_count: 0`) | programs.json |
| `root_type` | `batch` | apps.json app_028 |
| Design suite placement | `Batch/…_ProgramDesign.md` (+ `RUNMQBRG_…_JobFlow.md`) — **"batch/sub (no map, no SCREEN SECTION)"** | design_asis/NOTES.md routing table |
| Invocation | JCL `RUNMQBRG.jcl` `PGM=OUMQBRG`; program ends with `GOBACK` | source L8/L120, jcl_jobs.json |

→ **Batch app.** The Batch viewpoint catalog (B1–B12) applies; the UI catalog (A–E) is N/A.

**ASSUMPTION (non-interactive approval gate):** the viewpoint table + coverage matrix below are **recorded as approved** and cases were written directly (per the run's NON-INTERACTIVE instruction). If a reviewer disagrees with the batch classification or any viewpoint decision, revise here first, then regenerate `cases.json`.

## Source facts (grounded on AST + copybooks + source, cross-checked)

- **Function (§1.2):** read `TRANFILE` (VSAM KSDS, key-order sequential) and MQPUT every posted transaction, wrapped in a routing header, to `ORION.TRAN.OUTBOUND.QUEUE` for downstream consumers (fraud / analytics / general ledger); print run counts. Runs as batch under `RUNMQBRG`.
- **Files:** `TRANFILE` — INDEXED, ACCESS SEQUENTIAL, RECORD KEY `TR-ID`, FILE STATUS `WS-TRAN-FS`, opened **INPUT only** (READ only — no WRITE/REWRITE/DELETE). Record `TRAN-REC` (copybook RTRAN) = **350 bytes**, 14 data fields (`TR-ID` X(16) … `FILLER` X(20)).
- **MQI verbs (native CALL, no EXEC):** `MQCONN` (L147), `MQOPEN` (L165), `MQPUT` (L214), `MQCLOSE` (L247), `MQDISC` (L258). Each is followed by an `MQ-CC-OK` (completion-code = 0) check — **5 distinct MQ failure trigger points.**
- **File-status branches:** OPEN fail `WS-TRAN-FS ≠ '00'` (L129), READ error `≠'00' AND ≠'10'` (L196; `'10'` = AT END = normal EOF), CLOSE fail `≠'00'` (L236) — **3 distinct file failure trigger points.**
- **Run modes (the classifier of this program = MQ availability):**
  1. **Full bridge** — MQCONN ok + MQOPEN ok → every record MQPUT (messages-put count). *(main happy)*
  2. **Read-only** — MQCONN fails → `MQ-IS-DOWN`, MQOPEN skipped, every record counted **skipped**, no MQDISC at end. *(graceful degradation — "safe to run without live MQ", source L29–31)*
  3. **Open-fail skip** — MQCONN ok but MQOPEN fails → `MQ-IS-DOWN`, every record skipped, MQDISC at end. *(degradation variant)*
- **Per-record put outcome:** MQPUT ok → messages-put++; MQPUT fail → put-error++ and continue to next record (never aborts the run).
- **Outbound message layout (`WS-OUT-MSG` = 362 bytes):** routing header `OM-REC-TYPE` X(04)='TRAN' + `OM-SRC-SYSTEM` X(08)='ORION   ' + `OM-TRAN-DATA` X(350) = the record image. `MQ-BUFFER-LEN` = LENGTH OF the message = 362. Descriptor: msg-type datagram (`MQ-MT-DATAGRAM`=8), format 'MQSTR', put option no-syncpoint (`MQ-PM-NO-SYNCPOINT`=4), fresh msg-id/correl-id (LOW-VALUES) per put. Open options = `MQ-OO-OUTPUT`(16) + `MQ-OO-FAIL-IF-QSG`(8192) = 8208.
- **Counters (`WS-COUNTERS`):** records-read / messages-put / put-error / skipped, each `PIC 9(09)` → capacity 999,999,999.
- **Run summary (3900):** DISPLAY of the four counts + `'OUMQBRG: bridge complete.'` to SYSOUT/job log.
- **Termination:** always `GOBACK` — **the program never issues STOP RUN and never abends by design**; even OPEN failure just logs the file status, bypasses the bridge, prints zero counts and returns normally (RC=0). JCL-level ABEND capture (CEEDUMP/SYSUDUMP) exists but is not driven by this program's logic.

### Discrepancy noted (design doc vs source)

The ProgramDesign §1.3 / §5.1 describe a **COMMAREA / KCOMM linkage** and "EXEC CICS LINK" invocation. The **actual source has no LINKAGE SECTION, no `PROCEDURE DIVISION USING`, and zero CICS commands** (`linkage_field_count: 0`, `cics_command_count: 0`); it is a plain batch program driven by JCL. This is boilerplate in the design template that does not match OUMQBRG. → **No parameter/COMMAREA test cases are written**; viewpoint *パラメータ・制御* is excluded with this reason. Queue-manager / queue names are **compile-time constants** (`ORIONQM1`, `ORION.TRAN.OUTBOUND.QUEUE`), not runtime parameters.

## Viewpoint table (観点表) — every base viewpoint decided

Batch catalog B1–B12 (references/viewpoints.md), mapped to canonical 中項目 labels. UI catalog (A–E) is N/A wholesale (no screen).

| Base VP | 中項目 (canonical) | Applicable? | Evidence / reason | Planned TCs |
|---|---|---|---|---|
| B1 入力データ / B3 変換ロジック | 機能・業務フロー (Function / business flow) | ✅ | 3 run modes (full / read-only / open-fail) + record-count variants; §1.2, source 1000/2000/2100/2200 | IT_MQBRIDGE_HAPPY_001–004, 015 |
| B4 出力ファイル・帳票 | 出力・帳票 (Output / report) | ✅ | message layout 362B + descriptor + run summary; RTRAN, KMQ, 2200/3900 | IT_MQBRIDGE_HAPPY_005–007 |
| B5 データ整合性 | データ整合性・冪等性 (Data integrity / idempotency) | ✅ | read = put + put-error + skipped accounting; rerun re-publishes (no processed flag) | IT_MQBRIDGE_HAPPY_008 · IT_MQBRIDGE_HAPPY_015 |
| B7 異常終了 | メッセージ・異常系 (Messages / abnormal) | ✅ | 8 distinct trigger points (3 file-status + 5 MQ verbs); source L129/196/236, MQ CC checks | UT_MQBRIDGE_ABNORMAL_001–008 |
| B8 リラン・リスタート | 回復・リラン (Recovery / rerun) | ✅ | no processed-flag / dedup → rerun re-publishes all (not idempotent) | IT_MQBRIDGE_HAPPY_015 |
| B9 境界・大量データ | 境界値 (Boundary values) | ✅ | empty(0)/single(1) file; record image exactly 350B; EOF fs '10' vs '00'; counter 9-digit capacity | UT_MQBRIDGE_BOUNDARY_001–004 |
| B12 スケジュール連携 | 連携・インターフェース (Linkage / interface) | ✅ | upstream TRANFILE feed, downstream OUTBOUND.QUEUE, RUNMQBRG JCL step | IT_MQBRIDGE_HAPPY_009–011 |
| B10 ログ・監査 | ログ・監査 (Log / audit) | ✅ | startup/connect/open DISPLAY trace to job log (1000/1100/1200) | IT_MQBRIDGE_HAPPY_012 |
| (operational) | 運用 (Operation) | ✅ | "safe to run without live MQ" (L29–31); MQM STEPLIB library concatenation | IT_MQBRIDGE_HAPPY_013–014 |
| B2 パラメータ・制御 | — | ❌ N/A | **no** SYSIN/PARM/linkage in the AST (see discrepancy); queue names are compile-time constants | — |
| B6 排他・同時実行 | 排他・同時実行 (Exclusion / concurrent access) | ❌ N/A | TRANFILE opened INPUT (read-only, DISP=SHR); no lock/REWRITE; MQPUT is no-syncpoint — no concurrency logic in source; runtime/ops concern | — |
| B11 性能 | — | ❌ N/A | throughput/volume — runtime, not decidable from source | — |
| (calc) | 計算・編集ロジック | ❌ N/A | only trivial `ADD 1` counters + one open-options COMPUTE (8208); covered under output/integrity | — |
| (auth) | 権限・セキュリティ | ❌ N/A | batch program, no login/user gate; queue security is external MQ/RACF configuration | — |
| UI A–E (画面表示/入力チェック/操作性/状態遷移/初期値/文字書式/表示制御) | — | ❌ N/A | no SCREEN SECTION, no BMS map — not a UI app | — |

## Coverage matrix (viewpoint → evidence rows → TC)

| 中項目 | Evidence row | TC id | 分類 |
|---|---|---|---|
| 機能・業務フロー | Full bridge run (MQ up, all records put) | IT_MQBRIDGE_HAPPY_001 | Normal |
| 機能・業務フロー | Single-record file | IT_MQBRIDGE_HAPPY_002 | Normal |
| 機能・業務フロー | Read-only mode (MQCONN fails) | IT_MQBRIDGE_HAPPY_003 | Normal |
| 機能・業務フロー | Open-fail skip mode (MQOPEN fails after connect) | IT_MQBRIDGE_HAPPY_004 | Normal |
| 出力・帳票 | Outbound message layout (header + 350B image = 362B) | IT_MQBRIDGE_HAPPY_005 | Normal |
| 出力・帳票 | Message descriptor + open/put options | IT_MQBRIDGE_HAPPY_006 | Normal |
| 出力・帳票 | Run summary report (4 counts + complete) | IT_MQBRIDGE_HAPPY_007 | Normal |
| データ整合性・冪等性 | Count accounting read = put + put-error + skipped | IT_MQBRIDGE_HAPPY_008 | Normal |
| 連携・インターフェース | Upstream TRANFILE feed present, read in key order | IT_MQBRIDGE_HAPPY_009 | Normal |
| 連携・インターフェース | Downstream OUTBOUND.QUEUE delivery for consumers | IT_MQBRIDGE_HAPPY_010 | Normal |
| 連携・インターフェース | RUNMQBRG JCL step wiring (STEPLIB, DD TRANFILE) | IT_MQBRIDGE_HAPPY_011 | Normal |
| ログ・監査 | Startup / connect / open log trace | IT_MQBRIDGE_HAPPY_012 | Normal |
| 運用 | Safe run without live MQ (graceful degradation) | IT_MQBRIDGE_HAPPY_013 | Normal |
| 運用 | MQM STEPLIB libraries concatenated for MQI resolution | IT_MQBRIDGE_HAPPY_014 | Normal |
| 回復・リラン | Rerun re-publishes all records (not idempotent) | IT_MQBRIDGE_HAPPY_015 | Normal |
| メッセージ・異常系 | OPEN TRANFILE failure (fs='35') | UT_MQBRIDGE_ABNORMAL_001 | Abnormal |
| メッセージ・異常系 | READ TRANFILE error (fs='23', not 00/10) | UT_MQBRIDGE_ABNORMAL_002 | Abnormal |
| メッセージ・異常系 | CLOSE TRANFILE failure (fs≠'00') | UT_MQBRIDGE_ABNORMAL_003 | Abnormal |
| メッセージ・異常系 | MQCONN failure → read-only mode | UT_MQBRIDGE_ABNORMAL_004 | Abnormal |
| メッセージ・異常系 | MQOPEN failure → skip mode | UT_MQBRIDGE_ABNORMAL_005 | Abnormal |
| メッセージ・異常系 | MQPUT failure (per record) → put-error count | UT_MQBRIDGE_ABNORMAL_006 | Abnormal |
| メッセージ・異常系 | MQCLOSE failure (warning) | UT_MQBRIDGE_ABNORMAL_007 | Abnormal |
| メッセージ・異常系 | MQDISC failure (warning) | UT_MQBRIDGE_ABNORMAL_008 | Abnormal |
| 境界値 | Empty file — 0 records (min side; 1-record side = IT_MQBRIDGE_HAPPY_002) | UT_MQBRIDGE_BOUNDARY_001 | Boundary |
| 境界値 | Record image exactly 350 bytes fills the image field (no truncation) | UT_MQBRIDGE_BOUNDARY_002 | Boundary |
| 境界値 | EOF file status '10' → clean end vs '00' → continue | UT_MQBRIDGE_BOUNDARY_003 | Boundary |
| 境界値 | Counter capacity — 9-digit counts up to 999,999,999 | UT_MQBRIDGE_BOUNDARY_004 | Boundary |

**Totals:** 27 TC — Normal (正常系) 15 · Abnormal (異常系) 8 · Boundary (境界値) 4.
**Message codes (EI/EF/GF/ER):** 0 in source (`msg_codes(OUMQBRG.xml)` = 0 distinct, 0 raises). This program has **no** message-code convention; abnormal assertions are the exact console DISPLAY diagnostics + MQ completion/reason codes + counter effects. All 8 abnormal trigger points are covered by dedicated TCs; none excluded.

## Standard fixture (F-STD)

Referenced by every TC's 前提条件 (only the delta is stated per case):

- **TRANFILE** = `ORION.TRANFILE` (VSAM KSDS, key `TR-ID` X(16)) populated with **5 posted transactions** in key order: `TR-ID` = `TRN0000000000001` … `TRN0000000000005`; each with `TR-TYPE-CD`='DB', `TR-CAT-CD`=0001, `TR-AMT`=+000001234.56, `TR-CARD-NUM`='4111111111111111', `TR-MERCHANT-NAME`='ACME STORE'. *(field formats from copybook RTRAN; sample values constructible — mark `*(needs real data)*` if a customer VSAM extract is required for exact record images.)*
- **Queue manager** `ORIONQM1` is running; queue `ORION.TRAN.OUTBOUND.QUEUE` is defined and put-enabled.
- **STEPLIB** = `ORION.LOADLIB` + `MQM.V9R3M0.SCSQLOAD` + `MQM.V9R3M0.SCSQANLE` + `MQM.V9R3M0.SCSQAUTH` (DISP=SHR).
- **Job** `RUNMQBRG` submitted with `PGM=OUMQBRG`, DD `TRANFILE`=`ORION.TRANFILE` (DISP=SHR), SYSOUT to MSGCLASS.

## Audit result

`audit_testcase.py cases.json --reg <REG> --root OUMQBRG` — candidates and their grounding:

1. **[wording] IT_MQBRIDGE_HAPPY_009 'precondition' code-centric (data-name 'TR-ID')** — *FIXED.* The record-key data-name `TR-ID` had leaked into the precondition; rewritten to "records keyed by transaction id" (the identifier stays in the Remark for traceability). cases.json regenerated, workbook rebuilt, re-audit clean of this finding.
2. **[valid] "could not run msg_codes … skip code coverage"** — *Expected & benign.* The audit's `_root_xml` looks under `parsed/cobol_xml/{main,sub}/`, but this reg stores the XML at `parsed/cobol_xml/ORION-CCMS/cbl/OUMQBRG.xml`. Running the design-skill `msg_codes.py` directly on the real path returns **0 codes / 0 raises** (verified), so there is no code coverage to check. No missing-code / invented-code / fold findings possible.
3. **[vague] "異常系 case but expected cites no message code / STOP" (×8)** — *Grounded & dismissed.* OUMQBRG has **no EI/EF/GF/ER message codes** and, by design, **never issues STOP RUN and never abends** (source L32 "Ends with GOBACK (never STOP RUN)"). The abnormal assertions are instead the exact console DISPLAY diagnostic text + the MQ completion/reason code (CC=2 / an RC literal) + the counter/flag effect — all concrete literals are present in each `expected`. The audit heuristic assumes the EI/EF or STOP/ABEND convention, which this program does not use; the cases are correctly grounded.

Final re-audit: 9 candidates, all resolved (1 benign path-mismatch + 8 grounded dismissals above). No duplicate ids, no empty required fields, no ad-hoc 中項目 (all from the canonical list), no remaining code-leak in bodies (code identifiers demoted to Remark), automation columns consistent (auto = × manual, no automation harness for an AS-IS batch program). Judgment dims hand-checked: each `expected` traces to the cited paragraph/line (spot-checked HAPPY_003 L145–156/182–185, ABNORMAL_006 L214–223, BOUNDARY_001 L191–199 — all confirmed); steps reproducible as a JCL run; 観点表 complete vs the spec.
