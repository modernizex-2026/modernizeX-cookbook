# NOTES — Test-case set for ODTRANA (Transaction Add, DB2)

- **App / root:** `ODTRANA` — CICS transaction **OD06** (apps.json `app_025`, root_type `online`).
- **Kind (by evidence, `design_evidence.py`):** **UI (CICS/BMS)** — a UI document exists in the design set and the reg owns BMS map `MTRANAA` (SDD-MAP-031). Not routed by `root_type`.
- **Deliverable language:** English (fixed by skill edition). Verbatim on-screen strings are reproduced unchanged in backticks; table/column/program identifiers are traceability only (kept in the 備考/Remark column).
- **Primary test basis (read):**
  - `design_asis/UI/ODTRANA_TransactionAddDB2_ScreenDesign.md` (§1 overview/roles/keys · §2 mockup + item detail + item states · §3 check spec (11 messages) · §4 events · §5 DB CRUD).
  - AST `reg/parsed/cobol_xml/ORION-CCMS/cbl/ODTRANA.xml` (487 lines — full procedure logic verified line-by-line).
  - BMS map `datastore/bms_maps.json → MTRANAA` (field lengths).
  - Copybooks `RTRAN` (SDD-CPY-072, transaction record layout), `WMSG` (SDD-CPY-078, message constants), `KCOMM` (SDD-CPY-007, commarea / `CA-PGM-CONTEXT`), `WHEAD` (header title).
  - DDL `input/…/ORION-CCMS/ddl/ORION.ddl` (CARD / TTYP / CTRL / TRAN column types).
- **OUT:** `analysis_output/datastore/design_runs/test_cases/ODTRANA/` — `NOTES.md`, `cases.json`, `ODTRANA_TestCases.xlsx`.
- **Automation:** none for an AS-IS CICS screen → every case `自動 = ×`, `自動化ID` empty.
- **AREA word (ID scheme):** `TRANADD` (unique in this run). IDs `IT_TRANADD_HAPPY_NNN`, `UT_TRANADD_ABNORMAL_NNN`, `UT_TRANADD_BOUNDARY_NNN`.

## Approval gate (NON-INTERACTIVE) — assumption

This run is non-interactive. **Assumption A1:** the viewpoint table below (every base viewpoint decided applicable / excluded-with-reason, plus the app-specific viewpoints) is taken as **approved** and cases were generated against it. If a reviewer rejects a row, only that row's cases change — the ID scheme keeps existing IDs stable.

## Program logic (verified on the AST — the ground truth every case traces to)

Pseudo-conversational, single map `MTRANAA`, `RETURN TRANSID('OD06')`.

- `0000-MAIN`: first entry (`EIBCALEN = 0`) or `CA-FIRST-ENTER` → `1000-SEND-INITIAL`; otherwise → `2000-PROCESS-INPUT`.
- `1000-SEND-INITIAL`: clear map, populate header, message line = `Enter transaction detail and press ENTER.`, `SEND MAP … ERASE`, set `CA-PGM-CONTEXT = 1`.
- `2000-PROCESS-INPUT` (EVALUATE `EIBAID`): `DFHPF3` → `7000-XCTL-MENU` (XCTL `OCMENU`); `DFHPF4` → `1000-SEND-INITIAL` (clear); `DFHENTER` → `2100-ADD-TRAN`; **OTHER** → re-show initial + `WS-MSG-INVALID-KEY`.
- `2200-EDIT-INPUT` (sequential, each guarded by `INPUT-VALID` → only the **first** failing field's message shows):
  1. `CARDNUMI` blank → `Card number is required.`
  2. `TRTYPEI` blank → `Transaction type is required.`
  3. `TRCATI` blank **or** not numeric → `Category must be numeric.`
  4. `TRAMTI` blank → `Amount is required.`
  (`TRMERCH`, `TRDESC` have **no** validation → optional.)
- Business checks after edit: `3000-CHECK-CARD` (`SELECT … ORION.CARD`; SQLCODE 0=found, 100→`Card number not found.`, other→`Error reading card table.`), then `3100-CHECK-TYPE` (`ORION.TTYP`; 100→`Transaction type not found.`, other→`Error reading type table.`).
- `3200-GET-NEXT-ID`: `SELECT CT_LAST_VALUE … ORION.CTRL WHERE CT_KEY='TRANID'`; if found → `+1` then `UPDATE`; **else `WS-CT-VALUE = 1`**. `TR-ID` = 16-digit zero-padded counter.
- `3300-BUILD-RECORD`: `TR-SOURCE = 'ONLINE'`, `TR-MERCHANT-ID = 0`, city/zip = spaces, `TR-AMT = FUNCTION NUMVAL(TRAMTI)`, orig/proc timestamps from `ASKTIME/FORMATTIME`.
- `3400-INSERT-TRAN`: `INSERT INTO ORION.TRAN (…)`; SQLCODE 0 → success `Transaction added. Id=<TR-ID>`; other → `Error inserting transaction row.`

### Message inventory (12 distinct on-screen outcomes — NOT EI/EF-coded)

This app emits **literal English strings** to the `ERRMSG` line, not `EI###/EF###` codes. `msg_codes.py ODTRANA.xml` therefore returns **0 codes** (verified). The assertion in each abnormal case is the **verbatim message string** (quoted in backticks), which the check spec (§3) and the AST both give literally.

| # | Message (verbatim) | Type | Trigger (para / line) | Covered by |
|---|---|---|---|---|
| 1 | `Enter transaction detail and press ENTER.` | I | 1000-SEND-INITIAL L66 | IT_TRANADD_HAPPY_001/002 |
| 2 | `Transaction added. Id=<id>` | C | 2100 L109 (insert OK) | IT_TRANADD_HAPPY_004 |
| 3 | `Card number is required.` | E | 2200 L127 | UT_TRANADD_ABNORMAL_001 |
| 4 | `Transaction type is required.` | E | 2200 L132 | UT_TRANADD_ABNORMAL_002 |
| 5 | `Category must be numeric.` | E | 2200 L138 (blank / non-numeric) | UT_TRANADD_ABNORMAL_003/004 |
| 6 | `Amount is required.` | E | 2200 L144 | UT_TRANADD_ABNORMAL_005 |
| 7 | `Card number not found.` | E | 2100 L119 (CARD SQLCODE 100) | UT_TRANADD_ABNORMAL_007 |
| 8 | `Transaction type not found.` | E | 2100 L116 (TTYP SQLCODE 100) | UT_TRANADD_ABNORMAL_008 |
| 9 | `Error reading card table.` | E | 3000 L167 (CARD SQLCODE other) | UT_TRANADD_ABNORMAL_009 |
| 10 | `Error reading type table.` | E | 3100 L188 (TTYP SQLCODE other) | UT_TRANADD_ABNORMAL_010 |
| 11 | `Error inserting transaction row.` | E | 3400 L256 (INSERT SQLCODE other) | UT_TRANADD_ABNORMAL_011 |
| 12 | `Invalid key pressed. Please try again.` | E | 2000 L87 (`WS-MSG-INVALID-KEY`, unsupported AID) | UT_TRANADD_ABNORMAL_012 |

> **Discrepancy (design vs source):** design §3 lists the 11 error/info messages but **omits the success confirmation** `Transaction added. Id=…` (grounded in AST L109-110). It is added as the main happy assertion (IT_TRANADD_HAPPY_004). No fabricated messages were introduced.

## Field / boundary facts (BMS map MTRANAA + RTRAN + DDL)

| Field (BMS) | Screen len | Target column (ORION.TRAN) | Column type (DDL) | Validation in ODTRANA |
|---|---|---|---|---|
| CARDNUM | 16 | (key into ORION.CARD `CD_NUM`) / TR_CARD_NUM CHAR(16) | CHAR(16) | required; must exist in CARD |
| TRTYPE | 2 | TR_TYPE_CD | CHAR(2) | required; must exist in TTYP |
| TRCAT | 4 | TR_CAT_CD | DECIMAL(4,0) | must be numeric (blank→numeric error) |
| TRAMT | 12 | TR_AMT (via NUMVAL) | DECIMAL(11,2) → max `999999999.99` | required; parsed by NUMVAL (sign allowed) |
| TRMERCH | 50 | TR_MERCHANT_NAME | CHAR(50) | none (optional) |
| TRDESC | 50 | TR_DESC CHAR(100) | CHAR(100) | none (optional) |

## Viewpoint table (観点表) — every base UI viewpoint decided

| # | 観点 (viewpoint) | Applicable? | Evidence | Planned TC(s) |
|---|---|---|---|---|
| U1 | 画面表示・レイアウト (Screen display / layout) | ✅ | §2 mockup, MTRANAA fields (SDD-MAP-031) | IT_TRANADD_HAPPY_001 |
| U2 | 初期値・デフォルト (Initial values / defaults) | ✅ | 1000-SEND-INITIAL: LOW-VALUES + header + info msg (L64-66, 8000) | IT_TRANADD_HAPPY_002 |
| U3 | 表示条件・活性制御 (Display conditions / enable control) | ✅ | §2 item-states; IC on CARDNUM (initial focus) | IT_TRANADD_HAPPY_003 |
| U4 | 文字・書式表示 (Characters / format display) | ✅ (merged into boundary) | numeric TRCAT/TRAMT, edited amount | UT_TRANADD_BOUNDARY_004..008 |
| U5 | 入力チェック (Input validation) | ✅ | 2200-EDIT-INPUT + master lookups (3000/3100) | ABNORMAL_001..008 |
| U6 | 操作性・キー (Operability / function keys) | ✅ | 2000 EVALUATE EIBAID (ENTER/PF3/PF4/OTHER) | HAPPY_011/012, ABNORMAL_012 |
| U7 | 状態遷移 (Screen / state transitions) | ✅ | 0000-MAIN EIBCALEN/CA-FIRST-ENTER; RETURN TRANSID OD06; XCTL OCMENU | HAPPY_011/013 |
| U8 | 機能・業務フロー (Function / business flow) | ✅ | §1.2, 2100-ADD-TRAN happy chain | HAPPY_004/005 |
| U9 | 計算・編集ロジック (Calculation / editing logic) | ✅ | 3200 counter, NUMVAL(TRAMTI), timestamps | HAPPY_006/007/008 |
| U10 | 出力・帳票 (Output / report) | ✅ | 3300/3400 INSERT column mapping (RTRAN) | HAPPY_009 |
| U11 | データ整合性・冪等性 (Data integrity / idempotency) | ✅ | counter UPDATE; each ENTER inserts a new row (not idempotent) | HAPPY_010 |
| U12 | 排他・同時実行 (Exclusion / concurrent access) | ❌ **excluded** | ODTRANA has no explicit lock; the `CTRL` counter read→update is a DB2/runtime concern, no COBOL lock verb | reason row only |
| U13 | メッセージ・異常系 (Messages / abnormal) | ✅ | message inventory above (12 outcomes) | ABNORMAL_001..012 |
| U14 | 境界値 (Boundary values) | ✅ | BMS lengths + DDL column ranges | BOUNDARY_001..010 |
| U15 | 回復・リラン (Recovery / rerun) | ⚠️ **partial / deferred** | operator re-enters after an error (re-prompt is covered); no restart/rollback logic in source. Counter is incremented in 3200 before INSERT in 3400 — if INSERT fails the id is consumed (gap), no ROLLBACK — noted, not asserted as a TC (cannot assert a defect as expected behaviour) | re-prompt covered by ABNORMAL_*; gap = observation below |
| U16 | 権限・セキュリティ (Authority / security) | ❌ **excluded** | ODTRANA performs **no** sign-on / role check; access is via the signed-on session + main menu (enforced upstream in OCSGNON/OCMENU). No auth branch exists in ODTRANA.xml | reason row only |
| U17 | ログ・監査 (Log / audit) | ❌ **excluded** | no audit/log write in ODTRANA (no log file, no journal verb) | reason row only |
| U18 | 連携・インターフェース (Linkage / interface) | ✅ | XCTL OCMENU (PF3); reads CARD/TTYP/CTRL, inserts TRAN | HAPPY_011 (+ reads covered by validation cases) |
| U19 | 性能・運用 (Operation / performance) | ❌ **excluded** | no volume / paper / environment instruction on this screen; performance is runtime | reason row only |
| **App-specific** | Pseudo-conversational re-entry (EIBCALEN / CA-PGM-CONTEXT) | ✅ | 0000-MAIN L50-60, 1000 sets context=1 | HAPPY_013 |
| **App-specific** | Control-table ID counter (ORION.CTRL 'TRANID') | ✅ | 3200-GET-NEXT-ID | HAPPY_006/007 |
| **App-specific** | DB2 SQLCODE classification (0/100/other) | ✅ | 3000/3100/3400 EVALUATE SQLCODE | ABNORMAL_007..011 |

### Density decisions
- **Classifier expansion — N/A:** no `区分/目的` classifier changes the written row (`TR-SOURCE` is hard-coded `ONLINE`; transaction type & category are validated free codes that map to the same columns). So no per-classifier happy variants are fabricated; instead the required/optional-field and master-existence dimensions drive the happy variants (HAPPY_004/005).
- **Per message = own TC:** all 10 error messages + 1 info (display) + 1 confirm (success) each get a dedicated case. `Category must be numeric.` is split into its two **distinct triggers** (blank vs non-numeric) = 2 TCs.
- **Both-side boundaries:** category `0000`/`9999`, amount `0.00`/`999999999.99`/`-100.00`, card 16-digit full vs short-key-not-found, merchant/desc 50-char max.
- **Validation ordering:** ABNORMAL_006 asserts the sequential `INPUT-VALID` guard (only the first failing field's message shows).

## Standard fixture — `F-STD` (referenced by every case; each case adds only its delta)

The operator is signed on and has opened **Transaction Add (DB2)** (CICS transaction **OD06**) from the main menu, so map `MTRANAA` is displayed. The DB2 tables hold:
- **Card master `ORION.CARD`:** one active card — `CD_NUM = 4000123412341234`, `CD_ACTIVE_STATUS = Y`.
- **Transaction-type master `ORION.TTYP`:** one type — `TT_CD = 01`, `TT_DESC = PURCHASE`.
- **Control `ORION.CTRL`:** one row — `CT_KEY = TRANID`, `CT_LAST_VALUE = 100` (so the next id assigned is `101` → `Id=0000000000000101`).
- **Transaction `ORION.TRAN`:** empty (or without the id under test).

Fixture literals are authored here (no customer sample DB / seed INSERTs exist in the DDL). They are internally consistent with the DDL column types and are reused verbatim across cases.

## Coverage matrix (viewpoint → cases)

| 観点 (中項目) | Cases | Count |
|---|---|---|
| 画面表示・レイアウト | HAPPY_001 | 1 |
| 初期値・デフォルト | HAPPY_002 | 1 |
| 表示条件・活性制御 | HAPPY_003 | 1 |
| 機能・業務フロー | HAPPY_004, HAPPY_005 | 2 |
| 計算・編集ロジック | HAPPY_006, HAPPY_007, HAPPY_008 | 3 |
| 出力・帳票 | HAPPY_009 | 1 |
| データ整合性・冪等性 | HAPPY_010 | 1 |
| 操作性・キー | HAPPY_011, HAPPY_012 | 2 |
| 状態遷移 | HAPPY_013 | 1 |
| 入力チェック | ABNORMAL_001..008 | 8 |
| メッセージ・異常系 | ABNORMAL_009..012 | 4 |
| 境界値 | BOUNDARY_001..010 | 10 |
| **Total** | | **35** |

- Messages covered: **12 / 12** (11 §3 messages + 1 AST success). Excluded: 0.
- Input fields: 6 / 6 present (画面表示); 4 validated fields × their check types covered; 2 optional fields covered (HAPPY_005).
- Viewpoints: 14 applicable → all have ≥1 TC; 4 excluded with reason (U12, U16, U17, U19); U15 deferred (re-prompt covered, restart is runtime).

## Observations (not test cases — documented for the reviewer)
- **ID gap on insert failure:** 3200 consumes the counter (UPDATE) before 3400 inserts; a failed INSERT (`Error inserting transaction row.`) leaves the counter advanced with no committed row → id gap. No `ROLLBACK` in source. Reported, not asserted (a defect cannot be an "expected result").
- **Control row absent → id defaults to 1 without re-seeding CTRL** (3200 ELSE): repeated adds while the control row is missing would all compute id=1 and the 2nd insert would fail on the `TR_ID` primary key. Covered positively as HAPPY_007 (first add → id 1); the duplicate-key consequence is an observation.

## Audit result

`audit_testcase.py cases.json --reg <REG> --root ODTRANA` → **13 candidates, all grounded/dismissed** (1 `[valid]` + 12 `[vague]`); **no** `[ids]`, `[fields]`, `[fold]`, `[wording]` (code-leak), `[auto]`, or missing-literal findings. 12 viewpoints used, all ⊆ the approved table.

- **`[valid] could not run msg_codes / skip code coverage`** (1) — dismissed: the audit resolves the root xml only under `cobol_xml/{main,sub}/`, but this reg nests it under `cobol_xml/ORION-CCMS/cbl/`. Ran `msg_codes.py` directly on the correct path (`ODTRANA.xml`) → **0 EI/EF codes** (this app uses literal English messages, not coded prefixes). There is no coded-message coverage to audit, so nothing is missed.
- **`[vague] 異常系 case but expected cites no message code / STOP`** (12, one per abnormal TC) — dismissed: this app has no `EI###/EF###` codes; the assertion in each abnormal case is the **verbatim on-screen message string** (backticked in every abnormal `expected`, e.g. `` `Card number not found.` ``), which is exactly the check-spec §3 text and the AST literal. This is the grounded assertion, per the skill's "message text when the check spec has it" rule. The audit regex only recognises `AA999`/`STOP`/`abort`/`ABEND` as an assertion, so it cannot see a literal-string assertion — a false positive for this message style.
- All other structural/wording/literal dims are clean: 35 unique ids, no empty required fields, every `mid` is a canonical viewpoint label, no COBOL identifiers leaked into the body (all in Remark), every `前提/手順/期待` carries concrete literals, `自動 = ×` with empty `自動化ID` throughout.

### Spot-check (3 random TCs re-verified against source)
- `IT_TRANADD_HAPPY_004` → `Transaction added. Id=0000000000000101` = counter 100+1, zero-padded 16 (AST 3200/L108-110). ✓
- `UT_TRANADD_ABNORMAL_007` → `Card number not found.` = CARD `SELECT` SQLCODE 100 → REC-NOT-FOUND → 2100 L119. ✓
- `UT_TRANADD_BOUNDARY_006` → amount `999999999.99` = TR_AMT DECIMAL(11,2)/S9(09)V99 max, fits the 12-char TRAMT field. ✓
