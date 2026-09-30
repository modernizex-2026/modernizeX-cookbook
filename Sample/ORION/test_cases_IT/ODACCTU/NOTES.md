# NOTES — Test cases for ODACCTU (Account Update, DB2 variant)

- **App:** ODACCTU — Account Update (DB2). CICS/BMS online screen, DB2 relational backend.
- **CICS transaction:** OD02 · **Map/mapset:** MACCTUA / MACCTU · **Program:** ODACCTU
- **App kind:** UI (CICS/BMS) — decided by EVIDENCE (`design_evidence.py` → `kind=ui`, a UI ScreenDesign doc exists; the reg also owns BMS map MACCTUA). NOT by root_type.
- **Ground truth (reg):** `/Users/thangnguyen/Library/Caches/.skr/run-57032/reg` — AST `parsed/cobol_xml/ORION-CCMS/cbl/ODACCTU.xml` (439 lines) + copybooks `cpy/{WMSG,WCONST,WHEAD,KCOMM,RACCT,MACCTU}.xml`.
- **Design suite (preferred evidence):** `…/ORION/design_asis/UI/ODACCTU_AccountUpdateDB2_ScreenDesign.md`.
- **OUT:** `…/analysis_output/datastore/design_runs/test_cases/ODACCTU`
- **Deliverable language:** English (fixed by skill edition). On-screen message strings reproduced verbatim inside `『…』`.
- **AREA (ID scheme):** `ACCTUPD` (unique across the run). IDs = `{IT|UT}_ACCTUPD_{HAPPY|ABNORMAL|BOUNDARY}_{NNN}`.

## How ODACCTU behaves (grounded on the AST)

Pseudo-conversational, **two-step fetch-then-save** protocol driven by a mode flag stored in the commarea work area (`CA-WORK-AREA(1:1)` = `F` fetch / `U` update):

1. **First entry** (`EIBCALEN = 0`) or `CA-FIRST-ENTER` → `1000-SEND-INITIAL`: clears the map, populates the header (tran/title/date/pgm/time via CICS `ASKTIME`/`FORMATTIME`), sets mode `F`, shows msg #1, `SEND MAP … ERASE`.
2. **Re-entry** → `2000-PROCESS-INPUT` restores the mode flag then branches on `EIBAID`:
   - **ENTER** → `2100-FETCH-ACCT`: validate Account ID, `SELECT … FROM ORION.ACCT WHERE AC_ID`, on hit populate the editable fields + set mode `U` + msg #2.
   - **PF3** → `7000-XCTL-MENU`: `XCTL PROGRAM(OCMENU)` — back to the main menu.
   - **PF4** → `1000-SEND-INITIAL`: clear/restart (mode resets to `F`).
   - **PF5** → `2200-UPDATE-ACCT`: requires mode `U`; `2300-EDIT-INPUT` validates; on valid, `UPDATE ORION.ACCT … WHERE AC_ID`; msg #5 on success.
   - **any other key** → re-init + msg #11.

**Column-level UPDATE (PF5):** only `AC_ACTIVE_STATUS, AC_CREDIT_LIMIT, AC_CASH_LIMIT, AC_EXPIRY_DATE, AC_GROUP_ID` are written by key. `AC_CURR_BAL, AC_OPEN_DATE, AC_REISSUE_DATE, AC_CYC_CREDIT, AC_CYC_DEBIT, AC_ADDR_ZIP` are **preserved** (never in the SET clause).

**Editable input fields (BMS map MACCTU):** Account ID `ACCTIDI X(11)`, Status `ACSTATI X(01)`, Credit Lim `ACCRLIMI X(13)`, Cash Lim `ACCSLIMI X(13)`, Expiry `ACEXPI X(10)`, Group ID `ACGRPI X(10)`. Header/message fields are program-supplied display.

### Validation asymmetry (fetch path vs update path)

| Field | ENTER / fetch (2100) | PF5 / update (2300-EDIT-INPUT) |
|---|---|---|
| Account ID | blank → msg #12; non-numeric → msg #3; numeric+absent → msg #13 | blank OR non-numeric → msg #6 |
| Status | *(not checked on fetch)* | not `Y`/`N` → msg #7 |
| Credit / Cash limits | *(not checked on fetch)* | either blank → msg #8 |
| Expiry / Group ID | *(never validated)* | *(never validated — moved as-is)* |

- Limits are converted with `FUNCTION NUMVAL` — tolerant of commas/decimals/spaces; a **non-numeric limit converts to 0.00 with no error** (only the blank check exists). Documented quirk (see BOUNDARY_005).
- Account ID numeric test is on the full `X(11)` field: entering fewer than 11 digits leaves trailing spaces → the field is **not numeric** → msg #3 (see BOUNDARY_002).

### Message catalog (all 13 ACTIVE in source — verified on the AST)

`msg_codes.py` returns **0 codes** — this system raises **literal English strings**, not EI/EF/GF codes (project convention). Each string is the assertion.

| # | Verbatim string | Type | Raise paragraph (line) | Trigger |
|---|---|---|---|---|
| 1 | `Enter account id and press ENTER to fetch.` | C | 1000-SEND-INITIAL (63) | initial screen / PF4 clear |
| 2 | `Amend fields and press PF5 to update.` | C | 2100-FETCH-ACCT (111) | ENTER, numeric id, row found |
| 3 | `Account id must be numeric.` | E | 2100-FETCH-ACCT (119) | ENTER, id non-numeric |
| 4 | `Press ENTER to fetch a row before PF5.` | E | 2200-UPDATE-ACCT (134) | PF5 while mode≠U (no fetch first) |
| 5 | `Account updated successfully.` | I | 2200-UPDATE-ACCT (143) | PF5, valid, UPDATE SQLCODE 0 |
| 6 | `Account id is required and numeric.` | E | 2300-EDIT-INPUT (157) | PF5 path, id blank/non-numeric |
| 7 | `Status must be Y or N.` | E | 2300-EDIT-INPUT (164) | PF5 path, status ≠ Y/N |
| 8 | `Credit and cash limits are required.` | E | 2300-EDIT-INPUT (170) | PF5 path, credit or cash blank |
| 9 | `Error reading account table.` | E | 3000-READ-ACCT (208) | SELECT SQLCODE other (≠0,≠100) |
| 10 | `Error updating account table.` | E | 3100-UPDATE-ACCT (231) | UPDATE SQLCODE other |
| 11 | `Invalid key pressed. Please try again.` (WS-MSG-INVALID-KEY) | E | 2000-PROCESS-INPUT (87) | unsupported AID key |
| 12 | `Please enter all required fields.` (WS-MSG-REQUIRED) | E | 2100-FETCH-ACCT (101) | ENTER, id blank (spaces/low-values) |
| 13 | `Record not found.` (WS-MSG-NOTFND) | E | 2100-FETCH-ACCT (115) **and** 2200/3100-UPDATE (146) | ENTER fetch SQLCODE 100 · PF5 update SQLCODE 100 (row deleted between fetch and save) |

Msg #13 has **two distinct trigger points** → two dedicated abnormal TCs (fetch-not-found ABNORMAL_007; concurrent-delete-on-update ABNORMAL_011). No dead messages in this program.

## Discrepancies / traps (recorded)

1. **PF5 is an active key but is NOT on the on-screen legend.** The line-24 caption is `ENTER=Process  PF3=Back  PF4=Clear` and §1.2 of the design lists only ENTER/PF3/PF4 — yet the source handles `DFHPF5` (the save key) and two on-screen messages (#2, #4) explicitly instruct the operator to press PF5. Ground truth = source: PF5 saves. Covered by HAPPY_009 (save works) + ABNORMAL_006 (PF5-before-ENTER gate).
2. **Design §4 "Confirm save" display-only column is not enforced by code.** The item-states table marks the six fields `□` (display-only) in a "Confirm save" state, but the program never protects them — the map fields stay UNPROT and `MACCTUAO` redefines `MACCTUAI`. After fetch/save the fields remain editable. Covered/noted in HAPPY_004.
3. **`design_evidence.py` member-program list is a false positive.** It attaches OUANLIN / OUDATE / OUSTMIN ProgramDesigns, but ODACCTU issues **no** `EXEC CICS LINK`/`CALL` — date/time come from CICS `ASKTIME`/`FORMATTIME`, not OUDATE; there is no analytics or statement browse. Same false-positive pattern seen on OCTRANV/OCSTMV. Linkage viewpoint therefore = XCTL→OCMENU + DB2 access only.
4. **No in-program authority gate.** ODACCTU carries the signed-on user in the commarea (`CA-USER-ID`/`CA-USER-TYPE`) but never checks it; sign-on is handled upstream by OCSGNON. 権限 viewpoint excluded.
5. **No active-status gate on update.** An inactive account (status N) can be fetched and updated (even reactivated) — there is no "account must be active" guard. HAPPY_013 reactivates account 3.

## Fixture — F-STD (shared across all TCs)

The 5 real seed rows loaded into **ORION.ACCT** (DB2), equivalent to the VSAM seed `input/10/ORION/ORION-CCMS/data/ACCTFILE.txt` (loaded to DB2 by the RUNDACCT-family batch). Signed-on CICS session; enter transaction `OD02` (or select the update function from the menu).

| AC_ID | Status | Curr bal | Credit limit | Cash limit | Open date | Expiry | Group |
|---|---|---|---|---|---|---|---|
| 00000000001 | Y | 1,234.56 | 5,000.00 | 1,000.00 | 2019-03-15 | 2027-03-31 | GOLD |
| 00000000002 | Y | 2,500.75 | 10,000.00 | 2,000.00 | 2020-07-01 | 2028-06-30 | PLATINUM |
| 00000000003 | N | 0.00 | 3,000.00 | 500.00 | 2018-01-20 | 2026-01-31 | STANDARD |
| 00000000004 | Y | 15,000.00 | 25,000.00 | 5,000.00 | 2021-11-11 | 2029-11-30 | PLATINUM |
| 00000000005 | Y | 99.99 | 7,500.00 | 1,500.00 | 2022-05-05 | 2030-05-31 | GOLD |

Absent keys used by negative TCs: `00000000099`, `00000000000` (numeric, not in table).

## 観点表 (viewpoint table) — every base UI viewpoint decided

> **Approval gate (non-interactive run):** this table is recorded as an **assumption taken as approved** (CLI/CI invocation, no interactive gate), per the skill's non-interactive rule.

| # | 観点 (中項目) | Applicable? | Evidence (spec / AST) | Planned TC |
|---|---|---|---|---|
| U1 | 画面表示・レイアウト (Screen display / layout) | ✅ | §2.1 layout + MACCTU map; initial + loaded-detail renders | HAPPY_001, 002 |
| U2 | 初期値・デフォルト (Initial values / defaults) | ✅ | 1000-SEND-INITIAL: LOW-VALUES fields, header populate, msg #1 | HAPPY_003 |
| U3 | 表示条件・活性制御 (Display conditions / enable control) | ✅ | item-states table; mode F→U; fields stay editable (trap #2) | HAPPY_004 |
| U4 | 文字・書式表示 (Characters / format display) | ✅ | limits shown via edit mask `-,---,---,--9.99` (4000-POPULATE-DETAIL) | HAPPY_005 |
| U5 | 入力チェック (Input validation) | ✅ | §3 checks; 2100/2300 field rules (see asymmetry table) | ABNORMAL_001–005, BOUNDARY_002,003,005 |
| U6 | 操作性・キー (Operability / function keys) | ✅ | EVALUATE EIBAID: ENTER/PF3/PF4/PF5/OTHER | HAPPY_006–009, ABNORMAL_006,008 |
| U7 | 状態遷移 (Screen / state transitions) | ✅ | pseudo-conv RETURN TRANSID(OD02); first-enter vs re-enter; PF3→OCMENU; PF4 restart | HAPPY_007,008,010 |
| U8 | 機能・業務フロー (Function / business flow) | ✅ | fetch→amend→save; status Y & N variants; fetch-only view | HAPPY_011–015 |
| U9 | 計算・編集ロジック (Calculation / editing logic) | ✅ | FUNCTION NUMVAL parse; edit-mask display | HAPPY_016, BOUNDARY_005 |
| U10 | 出力・帳票 (Output / report) | ❌ — no report/print/mail; the only "output" is the DB2 row write (covered under データ整合性) | AST has no report/SEND to printer/MQ | — |
| U11 | データ整合性・冪等性 (Data integrity / idempotency) | ✅ | column-level UPDATE (5 cols set, 6 preserved); re-save idempotent | HAPPY_017, 018 |
| U12 | 排他・同時実行 (Exclusion / concurrent access) | ✅ (coded path only) | UPDATE SQLCODE 100 = row deleted between fetch and PF5 → msg #13 | ABNORMAL_011 |
| U13 | メッセージ・異常系 (Messages / abnormal) | ✅ | 13 literal messages, all active (catalog above) | ABNORMAL_006–010 + all ABNORMAL/BOUNDARY error rows |
| U14 | 境界値 (Boundary values) | ✅ | field lengths X(11)/X(01)/X(13)/X(10); numeric-key domain; limit magnitude | BOUNDARY_001–007 |
| U15 | 回復・リラン (Recovery / rerun) | ✅ (partial) | PF4 restart; idempotent re-save; concurrent-delete recovery | HAPPY_008, 018; ABNORMAL_011 |
| U16 | 権限・セキュリティ (Authority / security) | ❌ — no in-program auth gate (trap #4); sign-on upstream (OCSGNON) | procedure division never reads CA-USER-* | — |
| U17 | ログ・監査 (Log / audit) | ❌ — no audit/log record written | no log file / WRITE to audit in AST | — |
| U18 | 連携・インターフェース (Linkage / interface) | ✅ | XCTL→OCMENU (PF3); DB2 SELECT/UPDATE on ORION.ACCT; NO OU* LINK (trap #3) | HAPPY_019 |
| U19 | 性能・運用 (Operation / performance) | ❌ — no volume/paper/env instruction; response is runtime-only | no operational caption in map/AST | — |

**App-specific viewpoints folded in:** two-step fetch/save protocol + mode gate (U5/U6/U7 — HAPPY_006/009, ABNORMAL_006); PF5-not-in-legend quirk (U6 — HAPPY_009); partial-column UPDATE preserve (U11 — HAPPY_017); NUMVAL tolerance quirk (U9 — BOUNDARY_005).

## Coverage matrix — message/branch → TC

| Message / branch | Trigger point | TC |
|---|---|---|
| #1 initial prompt | initial screen | HAPPY_001, 003 |
| #2 amend prompt | fetch success | HAPPY_002, 006, 011 |
| #5 update success | PF5 valid, SQLCODE 0 | HAPPY_011, 012, 013, 014 |
| #12 required (blank id, fetch) | ENTER blank | ABNORMAL_001 |
| #3 non-numeric (fetch) | ENTER non-numeric | ABNORMAL_002, BOUNDARY_002 |
| #6 id required+numeric (update) | PF5 blank/non-numeric id | ABNORMAL_003 |
| #7 status Y/N | PF5 status≠Y/N | ABNORMAL_004, BOUNDARY_003 |
| #8 limits required | PF5 credit/cash blank | ABNORMAL_005 |
| #4 PF5 before fetch | PF5 mode F | ABNORMAL_006 |
| #13 not found (fetch) | ENTER absent id | ABNORMAL_007, BOUNDARY_007 |
| #11 invalid key | unsupported AID | ABNORMAL_008 |
| #9 read error | SELECT SQLCODE other | ABNORMAL_009 |
| #10 update error | UPDATE SQLCODE other | ABNORMAL_010 |
| #13 not found (update) | PF5 UPDATE SQLCODE 100 | ABNORMAL_011 |
| ENTER / PF3 / PF4 / PF5 keys | EIBAID branches | HAPPY_006 / 007 / 008 / 009 |
| column-level write + preserve | PF5 UPDATE | HAPPY_017 |
| NUMVAL parse + edit mask | limits | HAPPY_016, 005; BOUNDARY_004, 005 |
| field length boundaries | X(11)/X(13)/X(10) | BOUNDARY_001, 004, 006 |

**Result: 13/13 messages covered (all active, 0 dead, 0 excluded); 5 AID branches covered; UPDATE column set + preserved set covered; 4 base viewpoints excluded with reasons (U10, U16, U17, U19).**

## Test-case count

- **Total: 37** — Normal 19 · Abnormal 11 · Boundary 7.

## Audit result (audit_testcase.py --reg <REG> --root ODACCTU)

Two candidate classes are **EXPECTED and dismissed** for every ORION-CCMS app (project convention — see the ORION-CCMS testcase-conventions memory):

1. `[valid] could not run msg_codes` — the audit's `_root_xml` only looks under `parsed/cobol_xml/{main,sub}/`, but this reg stores the AST under `parsed/cobol_xml/ORION-CCMS/cbl/`. Ran `msg_codes.py` directly on `…/cbl/ODACCTU.xml` → **0 codes** confirmed. No EI/EF/GF scheme in this system; nothing to cover by code.
2. `[vague] 異常系 case but expected cites no message code / STOP` (one per Abnormal TC) — the verbatim on-screen English string (e.g. `『Record not found.』`) IS the assertion; there is no EI/EF code to cite. All abnormal expecteds quote the exact message in `『…』`.

All other candidate classes (ids, fields, wording/code-leak, fold, parity, auto) must be **empty** — see the final audit run below.

### Final audit run (2026-09-28)

`python3 scripts/audit_testcase.py cases.json --reg <REG> --root ODACCTU`

- `# viewpoints used (14)` — all 14 are canonical 中項目 labels from viewpoints.md (no ad-hoc groupings). ✅
- **12 candidates, all in the two EXPECTED dismissible classes:**
  1. `[valid] could not run msg_codes` ×1 — DISMISSED. Re-ran `msg_codes.py` directly on `parsed/cobol_xml/ORION-CCMS/cbl/ODACCTU.xml` → **0 codes / 0 STOP**. The auditor only probes `parsed/cobol_xml/{main,sub}/`; this reg nests under `ORION-CCMS/cbl/`. This system has no EI/EF/GF code scheme — messages are literal English strings.
  2. `[vague] 異常系 case but expected cites no message code / STOP` ×11 (one per Abnormal TC) — DISMISSED. Each abnormal expected quotes the verbatim on-screen English string in `『…』` (that string IS the assertion); there is no EI/EF code to cite.
- **No `[ids]`, `[fields]`, `[wording]`/code-leak, `[fold]`, `[parity]`, `[auto]` candidates.** ✅

### Spot-check (3 TCs re-verified against the AST)

- IT_ACCTUPD_HAPPY_009 (PF5 save) — AST L83 `WHEN DFHPF5 → 2200-UPDATE-ACCT`; success msg `Account updated successfully.` L143. ✅
- UT_ACCTUPD_ABNORMAL_007 (not found on ENTER) — 2100 ELSE `WS-MSG-NOTFND` L115; 3000-READ-ACCT SQLCODE 100 L204-205; `WS-MSG-NOTFND = "Record not found."` (WMSG). ✅
- UT_ACCTUPD_BOUNDARY_004 (max magnitude) — `AC-CREDIT-LIMIT S9(10)V99` (RACCT); input `ACCRLIMI X(13)` (MACCTU); `9999999999.99` = 10 int + 2 dec fits both. ✅

**Definition of done:** coverage matrix approved (recorded assumption); every matrix row → ≥1 TC or exclusion note; cases.json + workbook build cleanly; audit run and every candidate grounded (dismissed with reason); 3 TCs spot-checked. ✅
