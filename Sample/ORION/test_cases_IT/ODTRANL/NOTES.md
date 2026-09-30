# NOTES — Test-case set for ODTRANL (Transaction List, DB2)

- **App / root:** `ODTRANL` — CICS transaction `OD05`, mapset `MTRANL` / map `MTRANLA` (SDD-MAP-032).
- **Kind (evidence, not root_type):** **UI (CICS/BMS)** — a UI ScreenDesign doc exists and the reg owns BMS map `MTRANLA`. Resolved with `design_evidence.py` → `kind=ui`.
- **REG (ground truth):** `/Users/thangnguyen/Library/Caches/.skr/run-90298/reg`
- **Design evidence (read-only):** `.../design_asis/UI/ODTRANL_TransactionListDB2_ScreenDesign.md` (+ `Image/MTRANL_MTRANLA.png`).
- **OUT:** `.../analysis_output/datastore/design_runs/test_cases/ODTRANL`
- **Deliverable language:** English (fixed by skill edition). On-screen message strings reproduced verbatim in `「…」`.
- **Author:** modernizeX.

## Approval gate (NON-INTERACTIVE)

> **Assumption (recorded, run continued):** This is a non-interactive run. The 観点表
> (viewpoint table) below is **taken as approved** and cases were written against it.
> If a reviewer rejects a viewpoint decision, adjust the row and rebuild `cases.json`
> before regenerating the workbook.

## What ODTRANL actually does (verified on the AST — `parsed/cobol_xml/ORION-CCMS/cbl/ODTRANL.xml`)

Pseudo-conversational CICS program. A single screen (`MTRANLA`) with **one input
field** (Card Num, `CARDNUM`, BMS length 16, `UNPROT/IC/FSET`). On `ENTER` it opens a
DB2 cursor `TRANCSR` = `SELECT TR_ID, TR_AMT, TR_TYPE_CD FROM ORION.TRAN WHERE
TR_CARD_NUM = :TR-CARD-NUM ORDER BY TR_ID`, fetches **up to 5 rows** (`PERFORM …
VARYING WS-IDX FROM 1 BY 1 UNTIL WS-IDX > 5 OR END-OF-FILE`) into the five display
slots, and reports the outcome on the `ERRMSG` line. **Read-only** — no C/U/D on any
file/table (design §5 confirms). Control: `RETURN TRANSID(OD05)` (stay), `XCTL
PROGRAM(OCMENU)` on PF3 (back to main menu).

**AID keys handled (`EVALUATE EIBAID`, para 2000):** `ENTER`→list · `PF3`→menu ·
`PF4`→clear · **any other key**→invalid-key message.

**Messages (7 free-text strings sent to `ERRMSGO`; NOT EI/EF codes — `msg_codes.py`
returns 0 codes):**

| # | Verbatim string | Type | Trigger (para · line) |
|---|---|---|---|
| 1 | Enter card number and press ENTER. | Info | 1000-SEND-INITIAL · L82 (initial / after PF4) |
| 2 | Transactions listed. | Info | 2100-LIST-AND-SHOW · L111 (`WS-ROW-CNT > 0`) |
| 3 | No transactions for this card. | Info | 2100-LIST-AND-SHOW · L114 (0 rows, no prior error) |
| 4 | Error opening transaction cursor. | Error | 3000-LIST-TRANS · L130 (`SQLCODE NOT = 0` on OPEN) |
| 5 | Error fetching transactions. | Error | 3100-FETCH-ROW · L155 (`SQLCODE` other, not 0/100) |
| 6 | Invalid key pressed. Please try again. | Error | 2000-PROCESS-INPUT OTHER · L90 (`WS-MSG-INVALID-KEY`) |
| 7 | Please enter all required fields. | Error | 2100-LIST-AND-SHOW · L102 (`WS-MSG-REQUIRED`, Card Num blank) |

**Key grounding decisions**
- **Only validation = required (blank Card Num).** `CARDNUM` has no `picin` (BMS) and
  the source applies no numeric/format/length check. A well-formed but non-matching
  card is **not an error** — it simply returns 0 rows → message #3. No "card must be
  numeric / must exist" check exists → do **not** invent one.
- **Page = first 5 rows only, no paging.** The fetch loop caps at `WS-IDX > 5`; no
  restart position is saved to the commarea and there are **no** PF7/PF8 keys.
- **Amount edit:** `TR-AMT PIC S9(9)V99` → `WS-ED-AMT PIC -,---,---,--9.99`
  (zero-suppress, thousands separators, floating sign, 2 decimals). Max ±999,999,999.99.
- **Date/time header:** `EXEC CICS FORMATTIME … DATESEP('-') YYYYMMDD(...) TIME(...)
  TIMESEP(':')` → date `year-month-day` (e.g. `2026-09-29`), time `HH:MM:SS`.

## 観点表 (viewpoint table) — every base UI viewpoint decided

中項目 labels are the canonical `<JP> (<EN>)` strings from `references/viewpoints.md`.

| # | 観点 (中項目) | Applicable? | Evidence (spec/AST) | Planned TC |
|---|---|---|---|---|
| U1 | 画面表示・レイアウト (Screen display / layout) | ✅ | §2 mockup + item details; BMS SDD-MAP-032 (31 fields) | IT…HAPPY_001,002 |
| U2 | 初期値・デフォルト (Initial values / defaults) | ✅ | 1000-SEND-INITIAL (msg #1) + 8000/8500 header populate; `CARDNUM IC` cursor | IT…HAPPY_003 |
| U3 | 表示条件・活性制御 (Display conditions / enable control) | ✅ | §2 item-states (Card Num ○ initial / □ page); echo `CARDNUMO ← TR-CARD-NUM` L108 | IT…HAPPY_004 |
| U4 | 文字・書式表示 (Characters / format display) | ✅ | `WS-ED-AMT` edit mask; `FORMATTIME` date/time | IT…HAPPY_005 |
| U5 | 入力チェック (Input validation) | ✅ (one check only) | 2100 blank check → msg #7; no other field check exists | UT…ABNORMAL_001 |
| U6 | 操作性・キー (Operability / function keys) | ✅ | 2000 `EVALUATE EIBAID`: ENTER/PF3/PF4 (+invalid→U13) | IT…HAPPY_006,007 (ENTER via U8) |
| U7 | 状態遷移 (Screen / state transitions) | ✅ | 0000-MAIN `EIBCALEN=0` / `CA-FIRST-ENTER`; `RETURN TRANSID(OD05)`; XCTL OCMENU | IT…HAPPY_008,009 |
| U8 | 機能・業務フロー (Function / business flow) | ✅ | §1.2 list function; 2100→3000/3100 list; msgs #2/#3 | IT…HAPPY_010,011,012 |
| U9 | 計算・編集ロジック (Calculation / editing logic) | ✅ | `ORDER BY TR_ID`; 3200-MOVE-ROW slot fill; amount edit | IT…HAPPY_013 |
| U10 | 出力・帳票・メール (Output / report / mail) | ❌ N/A | read-only screen; no report/file write/mail (§5). On-screen list covered by U1/U8/U9 | — |
| U11 | データ整合性・冪等性 (Data integrity / idempotency) | ❌ N/A | §5: no C/U/D; re-query is inherently idempotent (no committed state) | — |
| U12 | 排他・同時実行 (Exclusion / concurrent access) | ❌ N/A (runtime) | read-only `SELECT` cursor, no lock/update verbs; concurrency is a DB/runtime concern | — |
| U13 | メッセージ・異常系 (Messages / abnormal) | ✅ | msgs #4/#5/#6 dedicated (msg #7→U5) | UT…ABNORMAL_002,003,004 |
| U14 | 境界値 (Boundary values) | ✅ | `CARDNUM` len 16; page cap 5 (`WS-IDX>5`); `TR-AMT S9(9)V99` | UT…BOUNDARY_001–006 |
| U15 | 回復・リラン (Recovery / rerun) | ❌ N/A | pseudo-conversational, no persistent/partial state; re-prompt after error is covered inside each error TC | — |
| U16 | 権限・セキュリティ (Authority / security) | ❌ N/A in-program | ODTRANL has no authority branch (no `CA-USER-TYPE` check); access gated upstream by Sign On + menu; direct entry (`EIBCALEN=0`) still shows the screen (covered in HAPPY_008) | — |
| U17 | ログ・監査 (Log / audit) | ❌ N/A | no audit/log record written (read-only; no WRITE verb) | — |
| U18 | 連携・インターフェース (Linkage / interface) | ➖ covered | only linkage = PF3 XCTL→OCMENU (U6) + DB2 read of ORION.TRAN (U8); no other external CALL | (U6/U8) |
| U19 | 性能・運用 (Performance / operation) | ❌ N/A (runtime) | fixed 5-row page, no printer/volume; legend line covered in U1 | — |
| APP-a | *(app-specific)* Paging forward/backward | ❌ EXCLUDED | §1.2 overview mentions it, but active source has **no** paging keys and the fetch caps at first 5 rows with no saved restart position → **not implemented**. Do not test dead/unimplemented behaviour | — |
| APP-b | *(app-specific)* Row selection → detail | ❌ EXCLUDED | §1.2 overview mentions it, but rows are display-only `ASKIP` fields and there is **no** selection field / no XCTL to a detail screen → **not implemented** | — |

## Coverage matrix (viewpoint → evidence rows → TC)

| 中項目 | Evidence rows | TC IDs | Category |
|---|---|---|---|
| 画面表示・レイアウト | captions/legend; list grid | IT_TRANLIST_HAPPY_001, _002 | Normal |
| 初期値・デフォルト | header auto-fill + msg #1 + IC cursor | IT_TRANLIST_HAPPY_003 | Normal |
| 表示条件・活性制御 | Card Num enterable-only; echo after list | IT_TRANLIST_HAPPY_004 | Normal |
| 文字・書式表示 | amount mask; date/time format | IT_TRANLIST_HAPPY_005 | Normal |
| 入力チェック | blank Card Num → msg #7 | UT_TRANLIST_ABNORMAL_001 | Abnormal |
| 操作性・キー | ENTER (→U8), PF3→menu, PF4→clear | IT_TRANLIST_HAPPY_006, _007 | Normal |
| 状態遷移 | cold/direct entry; pseudo-conv re-entry | IT_TRANLIST_HAPPY_008, _009 | Normal |
| 機能・業務フロー | list (msg #2); no rows (msg #3); 1 row | IT_TRANLIST_HAPPY_010, _011, _012 | Normal |
| 計算・編集ロジック | ORDER BY TR_ID; slot fill; type shown | IT_TRANLIST_HAPPY_013 | Normal |
| メッセージ・異常系 | msg #6 invalid key; #4 open err; #5 fetch err | UT_TRANLIST_ABNORMAL_002, _003, _004 | Abnormal |
| 境界値 | card len 16/1; page 5/6; amount 0/neg/max | UT_TRANLIST_BOUNDARY_001–006 | Boundary |

**Messages: 7 covered / 0 excluded.** #1→HAPPY_003 · #2→HAPPY_010 · #3→HAPPY_011 ·
#4→ABNORMAL_003 · #5→ABNORMAL_004 · #6→ABNORMAL_002 · #7→ABNORMAL_001.
**AID keys: 4/4.** ENTER→HAPPY_010 · PF3→HAPPY_006 · PF4→HAPPY_007 · other→ABNORMAL_002.

## Fixture — F-STD (shared; each TC states F-STD + its delta)

CICS region up; transaction `OD05` defined for program `ODTRANL` / mapset `MTRANL`;
operator already signed on via the Sign On screen; DB2 available with table
`ORION.TRAN`. Seed rows (fixture data VALUES reproduced verbatim in the cases):

| Card number (16) | # rows | TR-ID (X16) / amount / type (X2) |
|---|---|---|
| `4111111111111111` | 6 | TXN0000000000001 / 1,234.56 / PU · …002 / -50.00 / RF · …003 / 0.00 / AD · …004 / 999,999,999.99 / PU · …005 / 200.00 / PY · …006 / 75.25 / PU |
| `4000000000000002` | 1 | TXN0000000000010 / 500.00 / PU |
| `4222222222222222` | 5 | TXN0000000000020…024 / 100.00 each / PU |
| `9999999999999999` | 0 | (no rows) |

`TR-AMT PIC S9(9)V99` (max ±999,999,999.99); `TR-ID`/`TR-CARD-NUM PIC X(16)`;
`TR-TYPE-CD PIC X(02)`; type codes shown as opaque fixture values (no legend in this
program). DB-fault cases (#4/#5) need a forced SQL error — marked `*(needs real data)*`
(environment/DBA setup).

## ID scheme

`{IT|UT}_{AREA}_{CLASS}_{NNN}` — `IT`=Normal, `UT`=Boundary/Abnormal; `AREA=TRANLIST`
(unique for this app); `CLASS`∈{HAPPY,ABNORMAL,BOUNDARY}. Order: normal flows →
abnormal → boundary.

## Audit result

`audit_testcase.py cases.json --reg <REG> --root ODTRANL` → **5 CANDIDATE findings, all
grounded/dismissed** (leads, not verdicts). Build clean: 23 cases, 11 中項目, count cell
G4 = 23, `auto=×` everywhere, no duplicate/empty ids.

1. **`[valid] could not run msg_codes … skip code coverage`** — GROUNDED (benign, path
   layout). The audit's `_root_xml` only probes `parsed/cobol_xml/{main,sub}/`, but this
   reg stores the AST at `parsed/cobol_xml/ORION-CCMS/cbl/ODTRANL.xml`, so the built-in
   probe misses it. I ran the design skill's `msg_codes.py` on the **real** path directly
   → `{"codes": {}, "stop_literals": []}` = **0 message codes**. There are therefore no
   coded validations to cover; the skipped check has nothing to find. (Skill scripts left
   unmodified per the run rules.)
2–5. **`[vague] UT_TRANLIST_ABNORMAL_001..004: 異常系 case but expected cites no message
   code / STOP`** — DISMISSED with reason. Confirmed by (1): ODTRANL emits **free-text**
   message strings on the `ERRMSG` line, not EI/EF/GF codes. Each of the 7 strings has
   its own dedicated TC and the abnormal cases assert the **verbatim** on-screen string in
   `「…」` (msgs #4/#5/#6/#7), which is the correct, grounded assertion for a
   free-text-message program. Not a defect; no `[A-Z]{2}\d{3}` code exists to cite.

**Message coverage: 7/7** (#1→HAPPY_003, #2→HAPPY_010, #3→HAPPY_011, #4→ABNORMAL_003,
#5→ABNORMAL_004, #6→ABNORMAL_002, #7→ABNORMAL_001). **AID keys 4/4.** No "invented code"
/ "fold" / "wording" / duplicate-id findings.

**Spot-check (3 random TCs re-verified against cited AST lines):**
- `IT_TRANLIST_HAPPY_010` — msg #2 「Transactions listed.」: `2100` `IF WS-ROW-CNT > 0`
  MOVE literal L110-111. ✔
- `UT_TRANLIST_ABNORMAL_003` — msg #4 「Error opening transaction cursor.」: `3000` OPEN
  TRANCSR then `IF SQLCODE NOT = 0` MOVE literal L128-130. ✔
- `UT_TRANLIST_BOUNDARY_004` — 5-row cap / no paging: `3000` `PERFORM 3100 VARYING WS-IDX
  … UNTIL WS-IDX > 5 OR END-OF-FILE` L132; no PF7/PF8 in `2000` EIBAID EVALUATE. ✔
