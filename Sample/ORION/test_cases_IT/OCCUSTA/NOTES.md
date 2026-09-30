# NOTES — OCCUSTA (Customer Add) test-case set

- **App / root:** `OCCUSTA` — Customer Add (app_011), CICS transaction **OROC**, mapset `MCUSTA` / map `MCUSTAA`.
- **Kind (by EVIDENCE, not root_type):** **UI (CICS/BMS)** — a UI design doc exists and the program owns BMS map `MCUSTAA` (`design_evidence.py` → `kind: ui`). Viewpoints use the UI catalog A–E with BMS instantiation (AID keys, map fields, CICS navigation).
- **REG (ground truth, read-only):** `/Users/thangnguyen/Library/Caches/.skr/run-57032/reg`
- **Design suite (preferred evidence, read-only):** `…/output/10/ORION/design_asis/UI/OCCUSTA_CustomerAdd_ScreenDesign.md`
- **Source of record (AST, read-only):** `reg/parsed/cobol_xml/ORION-CCMS/cbl/OCCUSTA.xml` (542 lines, 12 paragraphs) + copybooks `WMSG/WCONST/WHEAD/KCOMM/RCUST/MCUSTA`.
- **OUT:** `…/analysis_output/datastore/design_runs/test_cases/OCCUSTA`
- **Deliverable language:** English (fixed by skill edition). **Author:** modernizeX. **Date:** 2026/09/28.
- **Approval gate (NON-INTERACTIVE):** the viewpoint table below is **recorded as an approved assumption** and the set was built without pausing, per the run instruction.

## Grounding note — design doc is thin, source (AST) is ground truth

The design doc §3 check table lists **one** message ("Invalid key pressed…"). The REG is ground truth (skill rule), and the AST `OCCUSTA.xml` carries a **full field-validation catalog** of **13 program message literals**. `msg_codes.py OCCUSTA.xml` returns **0 codes / 0 raises** — ORION-CCMS screens raise **literal English strings** on the `ERRMSG` line, not EI/EF/GF codes (project convention, same as OCCARDA/OCCUSIN). Every message + raise paragraph below was confirmed **active on the AST**; no commented-out / dead validation was found, so nothing is excluded on that basis.

Unlike its sibling OCCARDA, OCCUSTA is a **single-file** add: it performs **no** sub-program `CALL`/`LINK` (no OUDATE), **no** master-existence lookup, **no** cross-reference write, and has **no** ABEND handler. Duplicate detection is done **only** at the VSAM `WRITE` via the `DUPREC` response (there is no pre-write uniqueness read). Confirmed on the AST: the only outbound control transfer is `PF3 → XCTL OCMENU`; there is no `CA-USER-TYPE`/authority reference.

> apps.json lists 25 "members" for app_011 — that is the degenerate call-reachability cluster (every screen pulls in the shared menu subtree via XCTL), **not** real dependencies (see design NOTES "Routing decision"). OCCUSTA's own AST calls nothing. The `member-program-designs` that `design_evidence.py` printed (OUANLIN/OUDATE/OUSTMIN) are spurious cluster matches and were **not** used.

### Message catalog (literal → trigger paragraph) — the abnormal / info assertions

| # | Message (verbatim, on `ERRMSG`) | Type | Raised at | Copybook / WS item |
|---|---|---|---|---|
| 1 | `Enter new customer details and press ENTER.` | I | 1000-SEND-INITIAL (open / PF4 / after success) | WS-M-PROMPT |
| 2 | `Invalid key pressed. Please try again.` | E | 2000-PROCESS-INPUT WHEN OTHER (unsupported AID) | WS-MSG-INVALID-KEY (WMSG) |
| 3 | `Customer id must be numeric.` | E | 5010-VAL-CUST (blank / non-numeric / >9 digits) | WS-M-CUST-NUM |
| 4 | `First name is required.` | E | 5020-VAL-FNAME (spaces/low-values) | WS-M-FNAME-REQ |
| 5 | `Last name is required.` | E | 5030-VAL-LNAME (spaces/low-values) | WS-M-LNAME-REQ |
| 6 | `Address line 1 is required.` | E | 5040-VAL-ADDR (spaces/low-values) | WS-M-ADDR-REQ |
| 7 | `City is required.` | E | 5050-VAL-CITY (spaces/low-values) | WS-M-CITY-REQ |
| 8 | `SSN must be nine numeric digits.` | E | 5060-VAL-SSN (blank / non-numeric / digits≠9) | WS-M-SSN-NUM |
| 9 | `FICO score must be numeric.` | E | 5070-VAL-FICO (blank / non-numeric / >3 digits) | WS-M-FICO-NUM |
| 10 | `FICO score must be 300 through 850.` | E | 5070-VAL-FICO (value <300 OR >850) | WS-M-FICO-RNG |
| 11 | `Customer id already exists.` | E | 3500-WRITE-CUST WHEN DFHRESP(DUPREC) | WS-M-CUST-DUP |
| 12 | `Customer added successfully.` | C | 2100-ADD-CUST after CUSTFILE written | WS-M-OK |
| 13 | `Error writing the customer file.` | E | 3500-WRITE-CUST WHEN OTHER (write RESP not NORMAL/DUPREC) | WS-M-WRITE-ERR |

### Validation order (5000-VALIDATE-ALL — first fault stops the chain, each step guarded by `WS-VALID`)
`5010 cust(numeric, 1–9 digits) → 5020 first(required) → 5030 last(required) → 5040 addr(required) → 5050 city(required) → 5060 ssn(exactly 9 num) → 5070 fico(numeric, 300–850)`. On any fail: `WS-INVALID`, message set on `ERRMSG`, cursor to Cust ID (`MOVE -1 TO CUSTIDL`), `SEND MAP DATAONLY` (screen kept, no write). On clean: 3500-WRITE-CUST `WRITE FILE(WS-CUSTFILE) RIDFLD(CU-ID)` → NORMAL = success (fresh screen + `Customer added successfully.`), DUPREC = `Customer id already exists.`, OTHER = `Error writing the customer file.`

### Numeric-parse rule (6000-PARSE-NUM) — grounds the boundary cases
`INSPECT LOW-VALUE→SPACE`, then scan positions 1..`WS-NC-LEN`: SPACE = skip (`CONTINUE`), `'0'..'9'` = accumulate `WS-NC-VALUE = *10 + digit` and `ADD 1 TO WS-NC-DIGITS`, anything else = `SET WS-INVALID`. Trailing check: `WS-NC-DIGITS = 0 → WS-INVALID` (so an all-blank numeric field is rejected). Effect: numeric fields are **zero-extended** (a Cust ID keyed `100` becomes `000000100`); the `>9`/`>3`-digit guards can never fire because the map fields cap entry at 9/3 columns.

### Field / column map (map MCUSTAA → CUST-REC, copybook RCUST)
| Screen field | caption | width | Record column (PIC) | Rule |
|---|---|---|---|---|
| CUSTID | `Cust ID :` | 9 | CU-ID 9(09) | numeric, 1–9 digits, zero-extended |
| CUFNAM | `First   :` | 25 | CU-FIRST-NAME X(25) | required |
| CULNAM | `Last    :` | 25 | CU-LAST-NAME X(25) | required |
| CUADDR | `Address :` | 50 | CU-ADDR-LINE-1 X(50) | required |
| CUCITY | `City    :` | 50 | CU-ADDR-CITY X(50) | required |
| CUSSN | `SSN     :` | 9 | CU-SSN 9(09) | exactly 9 numeric digits |
| CUFICO | `FICO    :` | 3 | CU-FICO-SCORE 9(03) | numeric, 300–850 |

Columns the screen does **not** capture — CU-MIDDLE-NAME, CU-ADDR-LINE-2, CU-ADDR-STATE, CU-ADDR-COUNTRY, CU-ADDR-ZIP, CU-PHONE-1, CU-PHONE-2, CU-GOVT-ID, CU-DOB — are `MOVE SPACES` before the write (grounds IT_CUSTADD_HAPPY_012).

## Fixture — F-STD (seed data)

F-STD = the CUSTFILE VSAM KSDS loaded with the ORION customer seed (the same 5-customer seed the Customer Inquiry screen OCCUSIN browses; customer ids `000000001`…`000000005` are cross-referenced by the XREFFILE seed used in OCCARDA):

- **CUSTFILE (Customer master):** existing customer ids `000000001`, `000000002`, `000000003`, `000000004`, `000000005`.
- **Derived probes:** new (unused) customer id for the happy add = `000000100`; other unused ids for boundary adds = `000000009`, `123456789`; an id **already on file** for the duplicate test = `000000001`.
- New-customer field values (`JOHN`/`SMITH`/`100 MAIN STREET`/`SPRINGFIELD`/SSN `123456789`/FICO `720`) are operator **inputs**, not seed rows — illustrative literals in the accepted formats.
- CICS region up, mapset `MCUSTA` installed, transaction `OROC` defined, operator signed on (authority enforced upstream — OCCUSTA has no in-program authority gate).

Each TC's 前提条件 states "F-STD" + only its delta.

## Viewpoint table (観点表) — every base UI viewpoint decided (recorded as approved)

| # | 観点 (中項目) | Applicable? | Evidence | Planned TC |
|---|---|---|---|---|
| U1 | 画面表示・レイアウト (Screen display / layout) | ✅ | §2.1 map MCUSTAA (24×80), captions Cust ID/First/Last/Address/City/SSN/FICO, key legend | HAPPY_001 |
| U2 | 初期値・デフォルト (Initial values / defaults) | ✅ | 1000-SEND-INITIAL: fields cleared, prompt msg, cursor→CUSTID (`MOVE -1 TO CUSTIDL`) | HAPPY_002 |
| U3 | 表示条件・活性制御 (Display conditions / enable control) | ✅ (single add mode) | §2 item-states: 7 fields UNPROT/enterable, header+ERRMSG display-only; no conditional hide/show | HAPPY_003 |
| U4 | 文字・書式表示 (Characters / format display) | ✅ | §2.1 widths (9/25/25/50/50/9/3) & colours (green input, blue caption, yellow title, red ERRMSG, turquoise legend) | HAPPY_004 |
| U5 | 入力チェック (Input validation) | ✅ | 5010–5070 per-field checks (8 messages) | ABNORMAL_001–008 |
| U6 | 操作性・キー (Operability / function keys) | ✅ | 2000-PROCESS-INPUT: ENTER / PF3 / PF4 / OTHER (AID keys) | HAPPY_005/006, ABNORMAL_009 |
| U7 | 状態遷移 (Screen / state transitions) | ✅ | EIBCALEN=0 cold start; CA-FIRST-ENTER; success re-display; PF3 XCTL→OCMENU; RETURN TRANSID(OROC) | HAPPY_007/008 |
| U8 | 機能・業務フロー (Function / business flow) | ✅ (single add mode — no 区分/目的 classifier) | §1.2 add flow; the whole function is one create path (no classifier values / modes to split) | HAPPY_009 |
| U9 | 計算・編集ロジック (Calculation / editing logic) | ✅ | 6000-PARSE-NUM (numeric parse, blanks ignored, zero-extension of Cust ID/SSN/FICO) | HAPPY_010, BOUNDARY_001 |
| U10 | 出力・帳票 (Output / report) | ✅ | §5 CRUD: CUSTFILE(C) column-level write; un-entered columns defaulted to SPACES | HAPPY_011/012 |
| U11 | データ整合性・冪等性 (Data integrity / idempotency) | ✅ | re-add of an existing id rejected at WRITE (DUPREC) → no duplicate row | HAPPY_013 |
| U12 | 排他・同時実行 (Exclusion / concurrent access) | ❌ N/A (runtime) | VSAM record lock at WRITE; a concurrent same-id add resolves to DUPREC → covered functionally by the write DUPREC path (msg #11). No in-program lock logic to unit-test | — (excluded, reason) |
| U13 | メッセージ・異常系 (Messages / abnormal) | ✅ | invalid key + system/file fault (write error) | ABNORMAL_009/010 |
| U14 | 境界値 (Boundary values) | ✅ | Cust ID 1/9 digits; SSN 8/9; FICO 299/300 & 850/851; name 25; address/city 50 | BOUNDARY_001–007 |
| U15 | 回復・リラン (Recovery / rerun) | ❌ (idempotence only) | re-run of the same add is blocked by DUPREC → no duplicate (covered in U11 HAPPY_013); mid-flow restart is runtime | — (excluded, reason) |
| U16 | 権限・セキュリティ (Authority / security) | ❌ | OCCUSTA has **no** in-program authority check (no `CA-USER-TYPE`/88 reference); signon+role enforced upstream by OCSGNON/OCMENU | — (excluded, reason) |
| U17 | ログ・監査 (Log / audit) | ❌ | no audit/log file written by OCCUSTA (only the CUSTFILE data write) | — (excluded, reason) |
| U18 | 連携・インターフェース (Linkage / interface) | ✅ | PF3 `XCTL PROGRAM(WS-MENU-PGM)` → OCMENU (downstream hand-off). No sub-program CALL/LINK exists (unlike OCCARDA's OUDATE) | HAPPY_005 (PF3) |
| U19 | 性能・運用 (Operation) | ❌ N/A (runtime) | online single-record entry; no paper/printer/volume instruction | — (excluded, reason) |

## Coverage matrix (viewpoint → TC)

| Viewpoint | TC ids | # |
|---|---|---|
| U1 画面表示 | IT_CUSTADD_HAPPY_001 | 1 |
| U2 初期値 | IT_CUSTADD_HAPPY_002 | 1 |
| U3 表示条件・活性 | IT_CUSTADD_HAPPY_003 | 1 |
| U4 文字・書式 | IT_CUSTADD_HAPPY_004 | 1 |
| U6 操作性・キー | IT_CUSTADD_HAPPY_005 (PF3), IT_CUSTADD_HAPPY_006 (PF4), UT_CUSTADD_ABNORMAL_009 (invalid key) | 3 |
| U7 状態遷移 | IT_CUSTADD_HAPPY_007 (cold start), IT_CUSTADD_HAPPY_008 (success re-display) | 2 |
| U8 機能・業務フロー | IT_CUSTADD_HAPPY_009 (main add) | 1 |
| U9 計算・編集 | IT_CUSTADD_HAPPY_010 (numeric parse/zero-ext), UT_CUSTADD_BOUNDARY_001 (single-digit zero-ext) | 2 |
| U10 出力 | IT_CUSTADD_HAPPY_011 (entered cols), IT_CUSTADD_HAPPY_012 (un-entered cols blank) | 2 |
| U11 データ整合性・冪等 | IT_CUSTADD_HAPPY_013 (idempotent dup) | 1 |
| U18 連携 | IT_CUSTADD_HAPPY_005 (PF3 XCTL→OCMENU) | (shared) |
| U5 入力チェック | UT_CUSTADD_ABNORMAL_001 (cust non-num), 002 (first blank), 003 (last blank), 004 (addr blank), 005 (city blank), 006 (ssn≠9), 007 (fico non-num), 008 (fico range) | 8 |
| U13 メッセージ・異常系 | UT_CUSTADD_ABNORMAL_009 (invalid key), 010 (write error) | 2 |
| U14 境界値 | UT_CUSTADD_BOUNDARY_001 (cust 1-digit), 002 (cust 9-digit), 003 (ssn 8/9), 004 (fico 299/300), 005 (fico 850/851), 006 (name 25), 007 (addr/city 50) | 7 |

**Totals: 13 Normal + 10 Abnormal + 7 Boundary = 30 TC.**
Messages covered: all **13** program literals have coverage — msg #1 prompt in HAPPY_001/002/006; #12 success in HAPPY_008/009 etc.; #2–#10 each a dedicated Abnormal (invalid key + 8 field checks); #11 duplicate at its only trigger in HAPPY_013 (framed as idempotency); #13 write error in ABNORMAL_010. 0 excluded on grounding; 5 viewpoints N/A-with-reason (U12/U15/U16/U17/U19).

## Audit result

`audit_testcase.py cases.json --reg <REG> --root OCCUSTA` → **11 candidates, all grounded and dismissed** (1 msg_codes-path + 10 literal-string abnormal — the two classes expected for every ORION-CCMS app; no avoidable findings). Structural checks pass: 30 unique ids, no empty fields, 13 viewpoints all from the 観点表, no wording/fold/parity/automation flags. (An initial `[wording]` flag on BOUNDARY_002 — a `WS-NC-DIGITS` token in the expected — was fixed by moving the identifier to 備考 and rebuilding.)

1. **`[valid] could not run msg_codes` — DISMISSED.** `audit_testcase._root_xml` looks under `parsed/cobol_xml/main|sub/`, but this reg stores XML under `parsed/cobol_xml/ORION-CCMS/cbl/`. Ran `msg_codes.py OCCUSTA.xml` directly → **0 codes / 0 raises**. ORION-CCMS screens raise literal English strings, not EI/EF/GF codes, so there is no code-coverage gap.
2. **`[vague] 異常系 case but expected cites no message code / STOP` (ABNORMAL_001–010) — DISMISSED.** The check wants an `EI###`/`STOP`/`abort`/`ABEND` token in every Abnormal expected. These programs have no message codes; the assertion **is** the verbatim on-screen string the operator sees (`Customer id must be numeric.`, `SSN must be nine numeric digits.`, `FICO score must be 300 through 850.`, `Customer id already exists.`, `Invalid key pressed. Please try again.`, `Error writing the customer file.`, etc.), each quoted in the expected and traced to its raise paragraph in 備考.

**Spot-check (3 random TCs re-verified against `OCCUSTA.xml`):**
- IT_CUSTADD_HAPPY_009 → 5000-VALIDATE-ALL clean → 3500-WRITE-CUST WRITE FILE RESP NORMAL → WS-M-OK `Customer added successfully.` ✓ (L181/187/226–227/190)
- UT_CUSTADD_ABNORMAL_008 → 5070-VAL-FICO ELSE branch `WS-NEW-FICO < 300 OR > 850` → WS-M-FICO-RNG `FICO score must be 300 through 850.` ✓ (L333–335)
- UT_CUSTADD_BOUNDARY_003 → 5060-VAL-SSN `WS-NC-DIGITS NOT = 9` → WS-M-SSN-NUM; 9 digits passes ✓ (L315–317)
