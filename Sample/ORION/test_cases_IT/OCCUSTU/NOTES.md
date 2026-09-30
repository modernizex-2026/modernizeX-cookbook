# OCCUSTU — Customer Update — Test-case NOTES

## 1. Scope & classification

| Item | Value |
|---|---|
| App / Function | **OCCUSTU** — Customer Update |
| System / Project | ORION Credit Card Management System (ORION-CCMS) |
| Subsystem | On-line — CICS/BMS, pseudo-conversational |
| CICS transaction | **ORUU** |
| Mapset / Map | **MCUSTU / MCUSTUA** (24×80) |
| App kind | **UI (CICS/BMS)** — `design_evidence.py` → `kind: ui` (a UI design doc exists; the reg also carries the BMS map). Routing by evidence, not `root_type`. |
| Files touched | **CUSTFILE** (Customer master, VSAM KSDS) — **R** (READ) + **R/U** (READ UPDATE → REWRITE) by key CU-ID |
| Sub-programs | none (no CALL/LINK); PF3 hands control to **OCMENU** (main menu) via XCTL |
| AREA (ID scheme) | **CUSTUPD** (unique across the run; sibling OCCUSTA used CUSTADD) |

**Ground truth:** the reg source `OCCUSTU.cbl` + copybooks `RCUST` / `WMSG` / `KCOMM` /
`WCONST` / `WHEAD` and the BMS map `MCUSTU.bms`, all verified present/active on the AST
(`parsed/cobol_xml/ORION-CCMS/cbl/OCCUSTU.xml`). `msg_codes.py` returns **0 codes** — this
family emits **literal English message strings** (WORKING-STORAGE `VALUE` clauses), not
EI/EF codes; assertions therefore quote the verbatim message text. Same pattern as OCCUSTA
/ OCCARDA.

### Two-step pseudo-conversation (the core business logic)

The load flag `WS-STATE-FLAG` lives in `CA-WORK-AREA(1:1)` and drives a two-step ENTER:

- **STEP 1 (load)** — key a Cust ID and press ENTER → `2200-EDIT-CUST-ID` validates the id →
  `2300-LOAD-CUST` reads CUSTFILE and shows the editable fields; flag ← `L`, id remembered in
  `CA-CUST-ID`.
- **STEP 2 (save)** — with the same id still loaded, change any of First / Last / Address /
  City / Phone and press ENTER → `5000-EDIT-FIELDS` validates → `6000-READ-FOR-UPDATE` re-reads
  with UPDATE intent → `6100-MOVE-CHANGES` + `6200-REWRITE-CUST` commits.
- **Keying a different id at any time restarts STEP 1** (`WS-IN-CUST-ID NOT = CA-CUST-ID` →
  load), so an operator can switch customers without pressing PF4.

Only **five fields are editable** on this screen (First, Last, Address line 1, City, Phone 1).
Everything else on the record (middle name, addr line 2, state, country, zip, phone 2, SSN,
govt id, DOB, FICO) is **preserved** through the read-for-update/REWRITE (`6100` overlays only
the five editable fields).

## 2. Fixture — `F-STD` (shared, cited by every TC as its baseline)

CUSTFILE (Customer master, VSAM KSDS) loaded from the seed file `data/CUSTFILE.txt` — **5 real
records** (all values below are real fixture data, reproduced unchanged):

| Cust ID | First | Mid | Last | Address line 1 | City | ST | ZIP | Phone 1 | SSN | FICO | DOB |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 000000001 | JOHN | Q | SMITH | 123 MAIN STREET | NEW YORK | NY | 10001 | 212-555-0101 | 123456789 | 720 | 1985-06-15 |
| 000000002 | MARIA | A | GARCIA | 456 MARKET STREET | SAN FRANCISCO | CA | 94105 | 415-555-0102 | 234567890 | 680 | 1990-02-28 |
| 000000003 | ROBERT | B | JOHNSON | 789 LAKE SHORE DRIVE | CHICAGO | IL | 60601 | 312-555-0103 | 345678901 | 640 | 1978-12-01 |
| 000000004 | LINDA | C | WILLIAMS | 321 OCEAN DRIVE | MIAMI | FL | 33101 | 305-555-0104 | 456789012 | 750 | 1995-09-09 |
| 000000005 | DAVID | D | BROWN | 654 PINE STREET | SEATTLE | WA | 98101 | 206-555-0105 | 567890123 | 700 | 1982-03-21 |

- Ids **000000006 and above** (and **999999999**) are **not on file**.
- Operator is signed on; transaction **ORUU** is started from the main menu (OCMENU).
- Field limits (from `RCUST` / BMS `MCUSTU.bms`): Cust ID `9(09)`; First / Last `X(25)`;
  Address line 1 / City `X(50)`; Phone 1 `X(15)`.

## 3. Viewpoint table (観点表) — every base UI viewpoint decided

Legend: ✅ applicable → TCs · ❌ N/A (reason) · ⚠️ partial/runtime.

| # | 観点 (中項目) | Applicable? | Evidence (source / AST) | Planned TC |
|---|---|---|---|---|
| U1 | 画面表示・レイアウト (Screen display / layout) | ✅ | BMS `MCUSTUA` captions + header (`8000-POPULATE-HEADER`), legend `ENTER=Process  PF3=Back  PF4=Clear` | HAPPY_001 |
| U2 | 初期値・デフォルト (Initial values / defaults) | ✅ | `1000-SEND-INITIAL`: LOW-VALUES map, cursor at Cust ID (`MOVE -1 TO CUSTIDL`), prompt `WS-M-PROMPT` | HAPPY_002 |
| U3 | 表示条件・活性制御 (Display conditions / enable control) | ❌ N/A | all six map fields are permanently `UNPROT` in the BMS map; the program never disables/hides a field by mode, only moves the cursor (`MOVE -1 TO xxxL`). No conditional enable/disable exists | — |
| U4 | 文字・書式表示 (Characters / format display) | ✅ folded | short id is zero-extended and redisplayed as `000000005` (CU-ID `9(09)` → CUSTIDO) — covered in BOUNDARY_001 | (BOUNDARY_001) |
| U5 | 入力チェック (Input validation) | ✅ | `2200-EDIT-CUST-ID` (blank / non-numeric) + `5000-EDIT-FIELDS` (First/Last/Addr/City/Phone required, stops at first error) | ABNORMAL_001–008 |
| U6 | 操作性・キー (Operability / function keys) | ✅ | `2000-PROCESS-INPUT` AID branches: ENTER=process, PF3=back, PF4=clear, other=invalid | HAPPY_009, HAPPY_010 (ENTER in flow; other→ABNORMAL_013) |
| U7 | 状態遷移 (Screen / state transitions) | ✅ | load↔save state flag; new-id restart (`WS-IN-CUST-ID NOT = CA-CUST-ID`); re-edit after save (flag stays `L`) | HAPPY_005, HAPPY_006 |
| U8 | 機能・業務フロー (Function / business flow) | ✅ | `2300-LOAD-CUST` (STEP 1), `2400-APPLY-UPDATE`→`6200-REWRITE-CUST` (STEP 2). Classifier = load vs save conversation state | HAPPY_003, HAPPY_004 |
| U9 | 計算・編集ロジック (Calculation / editing logic) | ❌ N/A | no arithmetic/derivation; only screen→record moves (`6100`) + header date/time formatting. The id NUMVAL parse is covered under input validation/boundary | — |
| U10 | 出力・帳票 (Output / report) | ✅ | REWRITE of CUSTFILE — five editable columns written (`6100-MOVE-CHANGES` + `6200`). No print/report/mail | HAPPY_007 |
| U11 | データ整合性・冪等性 (Data integrity / idempotency) | ✅ | `6100` overlays only First/Last/Addr/City/Phone → non-editable fields (mid name, SSN, FICO, state, zip, DOB) preserved; re-save of same values is idempotent | HAPPY_008 (+HAPPY_006) |
| U12 | 排他・同時実行 (Exclusion / concurrent access) | ⚠️ partial | save uses `READ … UPDATE` (exclusive) then REWRITE; true lock contention is runtime. Testable effect = record deleted between load and save | ABNORMAL_010 |
| U13 | メッセージ・異常系 (Messages / abnormal) | ✅ | customer-not-found, deleted-mid-conversation, read error, rewrite error, invalid key | ABNORMAL_009–013 |
| U14 | 境界値 (Boundary values) | ✅ | Cust ID width 1↔9 digits; editable field max lengths 25/50/15 | BOUNDARY_001–007 |
| U15 | 回復・リラン (Recovery / rerun) | ✅ folded | after any validation failure the conversation stays put and the operator corrects & resubmits; PF4 clears to restart; new id restarts STEP 1 — covered by HAPPY_005/010 + abnormal re-prompt cases. No batch restart | (HAPPY_005/010) |
| U16 | 権限・セキュリティ (Authority / security) | ❌ N/A | OCCUSTU performs no sign-on / role check; `CA-USER-TYPE` exists in the shared COMMAREA but is never read here (sign-on is upstream). No gate to test | — |
| U17 | ログ・監査 (Log / audit) | ❌ N/A | no audit/log record written; the program only reads/rewrites CUSTFILE | — |
| U18 | 連携・インターフェース (Linkage / interface) | ✅ folded | only linkage is PF3 XCTL → OCMENU; no sub-program CALL/LINK. Covered by HAPPY_009 | (HAPPY_009) |
| U19 | 性能・運用 (Operation) | ❌ N/A/runtime | single-record maintenance; no volume/paper/printer setup. Deferred to runtime | — |

**App-specific viewpoint added:** the **two-step (load → save) pseudo-conversation** and the
**"different id restarts STEP 1"** rule — instantiated under U7/U8 (HAPPY_003–006), not a
separate 中項目.

**Approval gate (non-interactive):** per the runner instruction this table is **recorded as an
approved assumption** and case generation proceeded without a live confirmation.

## 4. Coverage matrix — every message literal → TC

`msg_codes.py` = 0 (literal-message family). The 15 distinct on-screen messages (14 program
literals in `WS-PROGRAM-MSGS` + shared `WS-MSG-INVALID-KEY`) each map to a TC:

| Message literal (verbatim) | Data-name / raise para | Class | TC |
|---|---|---|---|
| Enter a customer id and press ENTER to load. | WS-M-PROMPT · 1000-SEND-INITIAL | info | HAPPY_001, HAPPY_002 |
| Record loaded - change fields and ENTER to save. | WS-M-LOADED · 2300-LOAD-CUST | info | HAPPY_003 |
| Customer updated successfully. | WS-M-UPDATED · 6200-REWRITE-CUST | confirm | HAPPY_004, HAPPY_007 |
| Customer id is required. | WS-M-ID-REQUIRED · 2200-EDIT-CUST-ID | error | ABNORMAL_001 |
| Customer id must be numeric. | WS-M-ID-NOTNUM · 2200-EDIT-CUST-ID | error | ABNORMAL_002 |
| First name is required. | WS-M-FNAME-REQ · 5000-EDIT-FIELDS | error | ABNORMAL_003, ABNORMAL_008 |
| Last name is required. | WS-M-LNAME-REQ · 5000-EDIT-FIELDS | error | ABNORMAL_004 |
| Address line 1 is required. | WS-M-ADDR-REQ · 5000-EDIT-FIELDS | error | ABNORMAL_005 |
| City is required. | WS-M-CITY-REQ · 5000-EDIT-FIELDS | error | ABNORMAL_006 |
| Primary phone is required. | WS-M-PHONE-REQ · 5000-EDIT-FIELDS | error | ABNORMAL_007 |
| Customer not found - check the id and retry. | WS-M-CUST-NOTFND · 2300-LOAD-CUST | error | ABNORMAL_009, BOUNDARY_002 |
| Record no longer on file - reload the id. | WS-M-DELETED · 2400-APPLY-UPDATE | error | ABNORMAL_010 |
| Error reading the customer file. | WS-M-READ-ERROR · 3000-READ-CUST / 6000-READ-FOR-UPDATE | error (env) | ABNORMAL_011 |
| Error rewriting the customer record. | WS-M-UPD-ERROR · 6200-REWRITE-CUST | error (env) | ABNORMAL_012 |
| Invalid key pressed. Please try again. | WS-MSG-INVALID-KEY · 2000-PROCESS-INPUT | error | ABNORMAL_013 |

Env-fault note: `WS-M-READ-ERROR` is raised at two READ paragraphs (load-read `3000` and
save read-for-update `6000`); per the env-fault-class rule these are **one representative TC**
(ABNORMAL_011, both paras noted in 備考), not one per paragraph.

**Result: 15 / 15 messages covered, 0 excluded. UC flows: load + save + navigation all covered.**

## 5. Case count / 分類 split

- **30 TC total**: **10 Normal**, **13 Abnormal**, **7 Boundary**.
- IDs: `IT_CUSTUPD_HAPPY_001–010`, `UT_CUSTUPD_ABNORMAL_001–013`, `UT_CUSTUPD_BOUNDARY_001–007`.
- Automation: 自動=× (manual), 自動化ID empty — no automation harness for an AS-IS CICS screen.

## 6. Discrepancies / to confirm

1. **Design §3 under-lists the check spec.** The design doc `OCCUSTU_CustomerUpdate_ScreenDesign.md`
   §3.1 shows only ONE message (`Invalid key pressed. Please try again.`). The **source** has
   **15** distinct on-screen messages (14 program literals + the shared invalid-key). Test cases
   are grounded in the **source/AST (ground truth)**, which is richer; all 15 are covered.
2. **Design §5 references "PF5" for save**, but the source has **no PF5 branch** — `2000-PROCESS-INPUT`
   handles only ENTER / PF3 / PF4; **save happens on the second ENTER** (STEP 2), not PF5. Cases
   follow the source (ENTER-driven save). The §5 "On save (PF5)" caption is a design-doc artifact.
3. Design §5 lists all 17 record columns as "screen input"; in the **source only 5** are editable
   (First, Last, Address line 1, City, Phone 1) — `6100-MOVE-CHANGES`. The other columns are
   preserved, verified by HAPPY_008.

## 7. Audit result

`audit_testcase.py cases.json --reg <REG> --root OCCUSTU` →
**14 CANDIDATE findings, all grounded & dismissed** (no case changed):

- **1 × `[valid]` "could not run msg_codes"** — the audit's `_root_xml` only looks in
  `parsed/cobol_xml/{main,sub}`, but this reg stores the AST under `ORION-CCMS/cbl/`; and
  `msg_codes` returns 0 codes anyway (literal-message family). No EI/EF codes to miss. **Dismissed.**
- **13 × `[vague]` "異常系 case but expected cites no message code / STOP"** — one per Abnormal
  TC. Expected — the audit expects an `AA999` code; this family asserts the **verbatim literal
  English message** (quoted in 期待結果), which is the correct, stronger assertion for ORION-CCMS.
  **Dismissed** (the documented literal-message class, same as OCCUSTA/OCCARDA).

No `[ids]`, `[fields]`, `[viewpoint]`, `[fold]`, `[wording]`, or `[auto]` candidates — ids
unique, every viewpoint is a canonical label, no folded/multi-code abnormal rows, no code-leak
into the business body.
