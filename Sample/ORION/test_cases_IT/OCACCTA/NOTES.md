# NOTES — Test-case set for OCACCTA (Account Open)

## 0. Run record

| Item | Value |
|---|---|
| App | **OCACCTA** — Account Open (CICS transaction `OROA`, mapset `MACCTA`, map `MACCTAA`) |
| System / project | ORION-CCMS (ORION Credit Card Management System) |
| App kind (evidence) | **UI (CICS/BMS)** — `design_evidence.py` reports `kind=ui` (a UI ScreenDesign exists); the reg also owns BMS map `MACCTAA` and the handler drives it with `EXEC CICS SEND/RECEIVE MAP`. Not decided by `root_type`. |
| REG (ground truth) | `/Users/thangnguyen/Library/Caches/.skr/run-65766/reg` (read-only) |
| Design suite (evidence) | `…/ORION/design_asis/UI/OCACCTA_AccountOpen_ScreenDesign.md` (+ member designs `OUDATE`, `OUANLIN`, `OUSTMIN`) |
| AST verified | `reg/parsed/cobol_xml/ORION-CCMS/cbl/OCACCTA.xml` (849 lines, active code only) + copybooks `WMSG/WHEAD/KCOMM/KDATE/RACCT/RCUST/RXREF`, BMS `bms_maps.json` (MACCTAA, SDD-MAP-001), DDL `ORION.ddl` |
| Deliverable language | **English** (fixed by skill edition) |
| Output | `cases.json`, `OCACCTA_TestCases.xlsx` (built `--lang en`) |
| Author | modernizeX |

### Why the AST — not just the design doc — is the primary basis
The AS-IS ScreenDesign for OCACCTA is **thin**: its §3 check table lists only ONE
message (`Invalid key pressed…`). The program actually carries **13 business
messages** and a full 9-step validation chain (`5000-VALIDATE-ALL`). Per the skill
("verify on the AST, never raw source as primary"), the test basis below is taken
from the **active AST**; the design doc supplies the layout, item states, roles,
keys and CRUD. Every message string here is a verbatim `VALUE` literal from
`OCACCTA.xml` / `WMSG.xml`.

### Message-code scheme — none (grounded exclusion)
Per the design-set NOTES (assumption 5) and confirmed on the AST, ORION-CCMS uses
**literal on-screen message strings**, NOT an `EI/EF/GF/ER` code scheme. Each
異常系 TC therefore quotes its **verbatim message string** `『…』` as the assertion
instead of a code. `msg_codes.py`/`crud_from_ast.py` return nothing for this reg
(they read native `MOVE "EIxxx"` / SELECT-FD, which CICS does not use).

## 1. Approval gate (non-interactive)

Per the skill's viewpoint-first workflow the 観点表 (§3) and coverage matrix (§5)
would be presented for sign-off before writing cases. **This is a non-interactive
run**, so the viewpoint table below is **recorded as an assumption and taken as
approved**, and case generation proceeds. Anything a human should still confirm is
listed in §7.

## 2. Standard fixture — `F-STD`

One shared fixture; each TC's 前提条件 says "F-STD" plus only its delta.

- The 12 VSAM KSDS clusters are defined (`DEFVSAM`) and loaded (`LOAD*` jobs):
  account master (ACCT), customer master (CUST), card cross-reference (XREF).
- The operator is signed on (via Sign On `OCSGNON`) and has navigated to
  **Account Open** (transaction `OROA`) from the Main Menu (`OCMENU`).
- Customer master contains customer **000000123** *(needs real data — confirm an
  existing customer id in the customer master)*.
- Account master does **not** contain account **00000004567** (free to create).
- The date utility subroutine `OUDATE` is available (invoked by `EXEC CICS LINK`/CALL).

Field formats (from PIC / BMS map / DDL) used for all literals:
Account ID = numeric ≤ 11 digits (`AC-ID 9(11)`, field width 11);
Customer ID = numeric ≤ 9 digits (`CU-ID 9(09)`, field width 9);
Credit/Cash limit = amount, ≤ 10 integer digits + ≤ 2 decimals, digits/comma/dot
only (`S9(10)V99`, field width 13); Open Date = `YYYY-MM-DD` 10-char, optional;
Group ID = 10-char, required (`AC-GROUP-ID`/`AC_GROUP_ID CHAR(10)`).

## 3. 観点表 (viewpoint table) — every base UI viewpoint decided

Legend: ✅ applicable (→ gets ≥1 TC) · ❌ excluded (reason cited).

| # | 観点 (中項目) | Applicable? | Evidence (spec / AST) | Planned TC |
|---|---|---|---|---|
| U1 | 画面表示・レイアウト (Screen display / layout) | ✅ | §2 mockup + BMS `MACCTAA`/SDD-MAP-001; `1000-SEND-INITIAL` | IT_…HAPPY_001 |
| U2 | 初期値・デフォルト (Initial values / defaults) | ✅ | fields = LOW-VALUES, prompt shown, cursor at Account ID (`ACCTIDL=-1`); Open-Date-blank→today via `OUDATE 'TODY'` (`5060`) | HAPPY_002, HAPPY_003 |
| U3 | 表示条件・活性制御 (Display conditions / enable control) | ✅ | §2 item-states: 6 input fields ○ at entry/validated, □ after confirm-create (form re-init) | covered in HAPPY_002 + HAPPY_011 |
| U4 | 文字・書式表示 (Characters / format display) | ✅ | header date/time via `FORMATTIME … YYYYMMDD DATESEP('-') / TIMESEP(':')` (`8500`); error line RED width 78 | HAPPY_004 |
| U5 | 入力チェック (Input validation) | ✅ | `5010-5090` per-field checks + `6000-PARSE-NUM`/`5100-PARSE-AMOUNT` | ABNORMAL_002–014 |
| U6 | 操作性・キー (Operability / function keys) | ✅ | `2000-PROCESS-INPUT` EIBAID: ENTER / PF3 / PF4 / OTHER | HAPPY_008, HAPPY_009, ABNORMAL_001 |
| U7 | 状態遷移 (Screen / state transitions) | ✅ | pseudo-conversational `EIBCALEN`/`CA-PGM-CONTEXT`; success → re-init form | HAPPY_010, HAPPY_011 |
| U8 | 機能・業務フロー (Function / business flow) | ✅ | `2100-OPEN-ACCT` main success path; **no 区分/目的 classifier** (single flow) | HAPPY_005 |
| U9 | 計算・編集ロジック (Calculation / editing logic) | ✅ | `5100-PARSE-AMOUNT`: comma-strip, 1-decimal→×10 scale, int/frac accumulation | HAPPY_006, HAPPY_007 |
| U10 | 出力・帳票 (Output / report) | ✅ | `3500-WRITE-ACCT` (13 cols) + `3700-WRITE-XREF` (card=acct id, acct, cust) | HAPPY_012, HAPPY_013 |
| U11 | データ整合性・冪等性 (Data integrity / idempotency) | ✅ | uniqueness re-check `5090`; **latent**: XREF-error message overwritten by success (`3700`→`226`) | ABNORMAL_017, ABNORMAL_018 |
| U12 | 排他・同時実行 (Exclusion / concurrent access) | ✅ | `3500-WRITE-ACCT` WHEN `DFHRESP(DUPREC)` — duplicate on write (race after the read-check) | ABNORMAL_019 |
| U13 | メッセージ・異常系 (Messages / abnormal) | ✅ | file-write error (`3500` OTHER) + unrecoverable read error abend (`9500`) | ABNORMAL_015, ABNORMAL_016 |
| U14 | 境界値 (Boundary values) | ✅ | field widths 11/9/13/10; parser limits int≤10, frac≤2; cash≤credit; date validity | BOUNDARY_001–011 |
| U15 | 回復・リラン (Recovery / rerun) | ✅ | `8100-SEND-DATAONLY` retains keyed values → correct-and-resubmit | HAPPY_015 |
| U16 | 権限・セキュリティ (Authority / security) | ❌ | OCACCTA performs **no** signon/role check — no reference to `CA-USER-TYPE`/`CA-USER-ADMIN` in the procedure division; access is enforced upstream (Sign On / Main Menu). | — |
| U17 | ログ・監査 (Log / audit) | ❌ | no audit/log file written — only CUST(R), ACCT(C/R), XREF(C); no log verb in the AST. | — |
| U18 | 連携・インターフェース (Linkage / interface) | ✅ | `CALL 'OUDATE'` (date utility) `5060`; PF3 `XCTL OCMENU` `7000`; `RETURN TRANSID(OROA)` | HAPPY_014 (OUDATE) + HAPPY_009 (XCTL) |
| U19 | 性能・運用 (Performance / operation) | ❌ (deferred) | single-record entry screen — no volume / paper / printer aspect; performance is a runtime/customer concern. | — |

**App-specific viewpoints revealed by the spec** — all folded into the base rows
above and TC'd: (a) *Open-Date auto-default to today* via `OUDATE 'TODY'` → U2
HAPPY_003; (b) *sequential short-circuit validation* (only the first failing field
is reported) → U5 ABNORMAL_014; (c) *latent cross-ref-error masking* (integrity
gap) → U11 ABNORMAL_018.

## 4. Message inventory → coverage (every message gets a dedicated TC or a reason)

| Message constant | Verbatim string | Trigger (paragraph) | TC |
|---|---|---|---|
| WS-M-PROMPT | Enter new account details and press ENTER. | initial display `1000` | HAPPY_001 |
| WS-M-OK | Account opened successfully. | success `2100`/`226` | HAPPY_005 |
| WS-MSG-INVALID-KEY | Invalid key pressed. Please try again. | unsupported AID `2000` OTHER | ABNORMAL_001 |
| WS-M-ACCT-NUM | Account id must be numeric. | `5010` (blank/non-numeric) | ABNORMAL_002, ABNORMAL_003 |
| WS-M-CUST-NUM | Customer id must be numeric. | `5020` | ABNORMAL_004, ABNORMAL_005 |
| WS-M-CUST-NF | Customer does not exist. | `5080`→`3000` NOTFND | ABNORMAL_006 |
| WS-M-CRLIM-BAD | Credit limit is not a valid amount. | `5030`→`5100` | ABNORMAL_007, ABNORMAL_008 |
| WS-M-CSLIM-BAD | Cash limit is not a valid amount. | `5040`→`5100` | ABNORMAL_009 |
| WS-M-CS-GT-CR | Cash limit cannot exceed credit limit. | `5050` | ABNORMAL_010, BOUNDARY_008 |
| WS-M-OPEN-BAD | Open date invalid, use YYYY-MM-DD. | `5060` OUDATE VALD ≠00 | ABNORMAL_011, BOUNDARY_009 |
| WS-M-GROUP-REQ | Disclosure group id is required. | `5070` | ABNORMAL_012 |
| WS-M-ACCT-DUP | Account id already exists. | `5090` (validation) **and** `3500` DUPREC (race) | ABNORMAL_013 (validation), ABNORMAL_019 (write race) |
| WS-M-WRITE-ERR | Error writing the account file. | `3500` WHEN OTHER | ABNORMAL_015 |
| WS-M-XREF-ERR | Account written; cross-ref write failed. | `3700` WHEN OTHER — **overwritten before SEND** | ABNORMAL_018 (documents the masking) |
| WS-MSG-TEXT (abend) | OCACCTA: unrecoverable file error. Contact support. | `9500` (read resp OTHER) | ABNORMAL_016 |

All 15 message strings are covered by a dedicated TC. `WS-M-ACCT-DUP` fires at two
distinct trigger points (validation read vs write DUPREC) → two TCs (density rule:
one TC per distinct trigger point).

## 5. Coverage matrix (viewpoint → TC IDs)

| 中項目 (viewpoint) | TC IDs | # |
|---|---|---|
| 画面表示・レイアウト | IT_ACCTOPEN_HAPPY_001 | 1 |
| 初期値・デフォルト | IT_ACCTOPEN_HAPPY_002, _003 | 2 |
| 文字・書式表示 | IT_ACCTOPEN_HAPPY_004 | 1 |
| 機能・業務フロー | IT_ACCTOPEN_HAPPY_005 | 1 |
| 計算・編集ロジック | IT_ACCTOPEN_HAPPY_006, _007 | 2 |
| 操作性・キー | IT_ACCTOPEN_HAPPY_008, _009, UT_ACCTOPEN_ABNORMAL_001 | 3 |
| 状態遷移 | IT_ACCTOPEN_HAPPY_010, _011 | 2 |
| 出力・帳票 | IT_ACCTOPEN_HAPPY_012, _013 | 2 |
| 連携・インターフェース | IT_ACCTOPEN_HAPPY_014 | 1 |
| 入力チェック | UT_ACCTOPEN_ABNORMAL_002–014 | 13 |
| メッセージ・異常系 | UT_ACCTOPEN_ABNORMAL_015, _016 | 2 |
| データ整合性・冪等性 | UT_ACCTOPEN_ABNORMAL_017, _018 | 2 |
| 排他・同時実行 | UT_ACCTOPEN_ABNORMAL_019 | 1 |
| 回復・リラン | IT_ACCTOPEN_HAPPY_015 | 1 |
| 境界値 | UT_ACCTOPEN_BOUNDARY_001–011 | 11 |
| **Total** | | **45** |

**分類 split:** Normal 15 · Abnormal 19 · Boundary 11.
**Validations:** 10 message-producing checks covered (13 input-check TCs) · 0 excluded ·
1 main UC flow · 2 excluded viewpoints (U16, U17) with reasons · 1 deferred (U19).

### Density justification
6 input fields × (blank + format/existence) + amount parser edge branches +
uniqueness + cash≤credit + date validity + 4 keys + 2 output records + 2 system
faults + integrity/exclusion = 45 TC. This sits in the expected order of magnitude
for a data-entry screen (25–60) and is not folded (every message and every bounded
field is a distinct row).

## 6. Audit result (`audit_testcase.py`)

Command: `audit_testcase.py cases.json --reg <REG> --root OCACCTA`
Result: **21 candidate findings** — 1 × `[valid]` + 18 × `[vague]` + 2 × `[wording]`.
All grounded and dismissed below; 15 viewpoints in use; no structural defects.

Candidates and their grounded resolution (candidates are leads, not verdicts):

1. **`[valid] could not run msg_codes … skip code coverage`** (×1) — expected & correct.
   The auditor looks for `parsed/cobol_xml/{main,sub}/OCACCTA.xml`; this reg nests
   the AST under `…/ORION-CCMS/cbl/…`, and OCACCTA uses no `EI/EF` codes anyway.
   Message coverage is instead proven by the §4 inventory (verbatim strings on the
   AST). **Dismissed.**
2. **`[vague] … 異常系 case but expected cites no message code / STOP`** (×18 — every
   Abnormal TC except ABNORMAL_016, whose expected says "aborts") — grounded
   exclusion: ORION-CCMS has **no message-code scheme** (§0); each 異常系 TC asserts
   its **verbatim on-screen string** `『…』` (which the auditor's `[A-Z]{2}\d{3}` regex
   cannot recognise as a "code"). Every such expected traces to a `MOVE WS-M-* TO
   ERRMSGO` line in the AST (see §4). **Dismissed with reason.**
3. **`[wording] … 'expected' code-centric (data-name; e.g. 'YYYY-MM-DD')`** (×2:
   ABNORMAL_011, BOUNDARY_009) — the token `YYYY-MM-DD` appears only **inside the
   verbatim message** `『Open date invalid, use YYYY-MM-DD.』`, which the skill requires
   to be reproduced unchanged. It is not a COBOL data-name. **Dismissed with reason.**

No duplicate ids, no empty required fields, no ad-hoc 中項目, no folded multi-code
row, automation columns consistent (all `auto=×`, no automation_id — AS-IS screen,
no harness). Judgment dims hand-reviewed: each 期待結果 traces to the cited paragraph
in `OCACCTA.xml`; steps reproducible from F-STD; 観点表 complete vs the spec.

## 7. Left for human review

- Confirm an **existing customer id** in the customer master for the happy-path and
  max-width fixtures (used `000000123` / marked *(needs real data)*).
- **U16 authority** is excluded because OCACCTA itself does no role check — confirm
  the upstream Sign On / Main Menu gate is covered by those apps' test sets.
- **Latent defect (ABNORMAL_018):** the cross-ref-write-failure message
  `WS-M-XREF-ERR` is set then overwritten by the success message before any SEND, so
  the operator sees "Account opened successfully." even though the cross-reference is
  missing. Documented as an integrity discrepancy — confirm intended behaviour.
- System-fault TCs (ABNORMAL_015/016/019) need environment fault-injection
  (disabled file / concurrent insert) — marked *(needs real data)*.
