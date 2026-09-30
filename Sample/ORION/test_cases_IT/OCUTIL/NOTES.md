# NOTES — Test-case set for OCUTIL (Admin Data-Utilities Driver)

- **App / root:** `OCUTIL` — CICS transaction **ORUT**, mapset **MUTIL**, map **MUTILA**
- **Kind (by evidence, NOT root_type):** **UI (CICS/BMS)** — `design_evidence.py` → `kind=ui` ("a UI document exists in the design set"); the reg also owns BMS map `MUTILA`. UI viewpoint catalog (A–E) applies, sourced from the BMS map + handler AST.
- **REG (ground truth, read-only):** `/Users/thangnguyen/Library/Caches/.skr/run-65766/reg`
- **Design evidence (read-only):**
  - UI doc: `design_asis/UI/OCUTIL_DataUtilities_ScreenDesign.md`
  - AST: `reg/parsed/cobol_xml/ORION-CCMS/cbl/OCUTIL.xml` (838 lines)
  - Copybooks: `WMSG` (message constants), `WHEAD` (header/title), `KCOMM` (`ORION-COMMAREA`), `MUTIL` (BMS symbolic map), `KUX/KSTMTB/KFLAG/KIMP/KARCH/KPURG/KBKP` (per-utility request/result areas)
  - Design run NOTES: `reg/datastore/design_runs/design_asis/NOTES.md`
- **OUT:** `.../design_runs/test_cases/OCUTIL/` — this NOTES.md · `cases.json` · `OCUTIL_TestCases.xlsx`
- **Deliverable language:** English (fixed by skill edition). On-screen text here is already English → quoted verbatim in `「…」`.

## Platform note — message model for this app

ORION-CCMS is an IBM z/OS **CICS/BMS** system. It uses **no `EI/EF/GF/ER` message-code scheme**; the program `MOVE`s a literal English string into the on-screen message field (`ERRMSGO`) or into a `WMSG` copybook constant. Verified: the design skill's `msg_codes.py OCUTIL.xml` returns **0 codes, 0 STOP-literals**. Therefore every abnormal case's expected result cites the **verbatim on-screen message string** `「…」` rather than a code. (This is why `audit_testcase.py` will emit `[vague] …異常系 case but expected cites no message code / STOP` for each abnormal TC — a false positive for this codebase; dismissed as a class, see Audit result.)

## What OCUTIL does (from OCUTIL.xml)

Pseudo-conversational dispatcher (`RETURN TRANSID('ORUT')`). On first entry (`EIBCALEN = 0`, or commarea state `CA-FIRST-ENTER`) it sends the initial screen (`1000-SEND-INITIAL`, default `Mode = REBL`); otherwise it processes the AID key (`2000-PROCESS-INPUT` → `EVALUATE EIBAID`):

| AID | Handler | Behaviour |
|---|---|---|
| `ENTER` | `2100-RUN` | validate Option (1–7), edit params, `LINK` to the matching `OU*` utility, show counts |
| `PF3` / `PF12` | `7000-XCTL-MENU` | `XCTL` to Administrator Menu **OCADMEN** |
| `PF4` / `CLEAR` | `1000-SEND-INITIAL` | clear + redisplay the screen |
| any other | `2000` WHEN OTHER | message `WS-MSG-INVALID-KEY` → re-send data-only |

Option → utility sub-program (`2100-RUN` `EVALUATE WS-OPTION` → `3100`–`3700`), each `LINK`ed with a per-utility parameter area and formatted by its own `5x00-FMT`:

| Option | On-screen label (verbatim) | Utility (LINK) | Params forwarded | Result labels |
|---|---|---|---|---|
| 1 | `1. Rebuild/validate card xref  (OUXREF)` | OUXREF | Mode (blank→`REBL`), Max count | Read/Written/Updated/Skip-acct/Skip-xref/Skip-cust/Errors/More |
| 2 | `2. Build monthly statements    (OUSTMB)` | OUSTMB | Cycle, Cutoff/Due (as due date), Max count | Acct read/Stmts/No-tran/Errors/Tot cr/Tot dr/More/Next acct |
| 3 | `3. Delinquency + expiry flags  (OUFLAG)` | OUFLAG | Mode (blank→`BOTH`), Cutoff/Due (cutoff), Max count | Read/Delinq/B30/B60/B90/Expired/Errors/More |
| 4 | `4. Import accounts from feed   (OUIMP)` | OUIMP | Max count | Read/Accepted/Added/Updated/Rejected/Skipped/Errors/More |
| 5 | `5. Archive aged transactions   (OUARCH)` | OUARCH | Cutoff/Due (cutoff), Max count | Read/Archived/Deleted/Kept/Arch amt/Errors/More |
| 6 | `6. Purge aged transactions     (OUPURG)` | OUPURG | Cutoff/Due (cutoff), Max count | Read/Purged/Kept/Errors/Purge amt/Keep amt/More |
| 7 | `7. Backup transactions to feed (OUBKP)` | OUBKP | Max count | Read/Written/Tot amt/Errors/Credit/Debit/More |

Field editing (`2150-EDIT-OPTION` / `2200-EDIT-INPUTS` / `6000-PARSE-NUM`): **Option** required, parsed numeric, must be 1–7. **Max count** (`9(07)`) and **Cycle** (YYYYMM) optional — if entered must be all-numeric (spaces skipped, ≥1 digit); Cycle is **not** month-range checked. **Mode** (`X(04)`) and **Cutoff/Due** (`X(10)`) are **not validated** by the driver — forwarded verbatim to the utility. Result formatting: counts via `WS-E9 PIC ZZZ,ZZZ,ZZ9`, amounts via `WS-EA PIC -,---,---,--9.99`; status/message from the utility's own `Kxx-STATUS` / `Kxx-MSG`.

**Key behaviour for the driver's own message:** `2100-RUN` picks its message solely on `WS-LINK-BAD` (the CICS `LINK` RESP). If the LINK succeeds it shows `Utility complete - see counts below.` even when the utility itself returned an error status (which then appears in `Status:`/result message). Only a not-`NORMAL` LINK RESP yields `Sub-program link failed - check resources.` (See IT_UTIL_HAPPY_021 vs UT_UTIL_ABNORMAL_006.)

### Live on-screen messages (all covered) vs dead

| # | Verbatim string | Raised at | Coverage |
|---|---|---|---|
| M1 | `Select a utility (1-7), key params, press ENTER.` | `1000-SEND-INITIAL` L134 (info, initial) | IT_UTIL_HAPPY_001 (+ redisplay via _008/_009) |
| M2 | `Sub-program link failed - check resources.` | `2100-RUN` L200 (LINK RESP ≠ NORMAL) | UT_UTIL_ABNORMAL_006 |
| M3 | `Utility complete - see counts below.` | `2100-RUN` L203 (LINK OK) | IT_UTIL_HAPPY_011–021 |
| M4 | `Enter a utility number 1 through 7.` | `2150-EDIT-OPTION` L217 (Option blank / required) | UT_UTIL_ABNORMAL_001 |
| M5 | `Utility number must be 1 through 7.` | `2150-EDIT-OPTION` L224 (Option non-numeric or <1 / >7) | UT_UTIL_ABNORMAL_002; boundaries UT_UTIL_BOUNDARY_003/004/005 |
| M6 | `Max count must be numeric.` | `2200-EDIT-INPUTS` L247 (UTNUM non-numeric) | UT_UTIL_ABNORMAL_003 |
| M7 | `Cycle must be numeric (YYYYMM).` | `2200-EDIT-INPUTS` L258 (UTCYC non-numeric) | UT_UTIL_ABNORMAL_004 |
| M8 | `Invalid key pressed. Please try again.` (`WS-MSG-INVALID-KEY`) | `2000-PROCESS-INPUT` WHEN OTHER L160 | UT_UTIL_ABNORMAL_005 |

No dead message strings in OCUTIL (all 8 string constants are reachable; every `5x00-FMT` and `3x00-DO` paragraph is referenced by the `2100-RUN` `EVALUATE WS-OPTION`). The design doc §3 types M3 as `E`, but the source raises it on LINK success — treated as an **information/completion** message here.

## Standard fixture — `F-STD`

The ORION-CCMS CICS region is up; the operator is **signed on as an authorized admin operator** (`OCSGNON`/`OCADMEN` enforce signon; OCUTIL itself does not re-check `CA-USER-TYPE`). The seven utility sub-programs (OUXREF, OUSTMB, OUFLAG, OUIMP, OUARCH, OUPURG, OUBKP) are installed and enabled. The VSAM/DB2 stores hold representative card, account and transaction data so each utility returns non-zero counts. The operator reaches `MUTILA` by selecting the data-utilities option from the Administrator Menu (`OCADMEN`), which starts transaction ORUT. Each TC states **F-STD + only its delta**.

## Viewpoint table (観点表) — every base UI viewpoint decided

Approval gate is non-interactive → recorded here as an **assumption** and taken as approved (see Assumptions). 中項目 uses the canonical `<JP> (<EN>)` labels.

**A. Display / UI**

| Base viewpoint (中項目) | Applicable? | Evidence | Planned TC |
|---|---|---|---|
| 画面表示・レイアウト (Screen display / layout) | ✅ | §2 MUTILA mockup + item details; `1000-SEND-INITIAL`, `8000-POPULATE-HEADER` | IT_UTIL_HAPPY_001, _002 |
| 初期値・デフォルト (Initial values / defaults) | ✅ | `Mode` default `REBL` (L133); `UTOPT` `UNPROT/IC/FSET` initial focus; entries empty | IT_UTIL_HAPPY_003 |
| 表示条件・活性制御 (Display conditions / enable control) | ✅ | §2 item-states (result fields `□` display-only; entries `○` initial→`□` after run); `ASKIP` attrs | IT_UTIL_HAPPY_004 |
| 文字・書式表示 (Characters / format display) | ✅ | `WS-E9 PIC ZZZ,ZZZ,ZZ9`, `WS-EA PIC -,---,---,--9.99`; date/time from FORMATTIME | UT_UTIL_BOUNDARY_001 |

**B. Input / operation**

| Base viewpoint | Applicable? | Evidence | Planned TC |
|---|---|---|---|
| 入力チェック (Input validation) | ✅ | `2150-EDIT-OPTION` (blank→M4, non-numeric/range→M5); `2200-EDIT-INPUTS` (UTNUM→M6, UTCYC→M7); Mode/Cut-off not validated | UT_UTIL_ABNORMAL_001 (blank), _002 (non-numeric), _003 (max), _004 (cycle), IT_UTIL_HAPPY_005 (pass-through) |
| 操作性・キー (Operability / function keys) | ✅ | `2000` `EVALUATE EIBAID` — ENTER/PF3/PF12/PF4/CLEAR/OTHER | IT_UTIL_HAPPY_006–009, UT_UTIL_ABNORMAL_005; ENTER via IT_UTIL_HAPPY_011–017 |
| 状態遷移 (Screen / state transitions) | ✅ | `0000-MAIN` `EIBCALEN`/`CA-FIRST-ENTER` pseudo-conversational; XCTL→OCADMEN | IT_UTIL_HAPPY_010; XCTL via IT_UTIL_HAPPY_006/007 |

**C. Function / data**

| Base viewpoint | Applicable? | Evidence | Planned TC |
|---|---|---|---|
| 機能・業務フロー (Function / business flow) | ✅ | `2100-RUN` `EVALUATE WS-OPTION` — one classifier value per utility 1–7 | IT_UTIL_HAPPY_011–017 (7) |
| 計算・編集ロジック (Calculation / editing logic) | ✅ | blank-Mode default substitution (`3100`/`3300`); `6000-PARSE-NUM` numeric parse (spaces skipped) | IT_UTIL_HAPPY_018; UT_UTIL_BOUNDARY_002 |
| 出力・帳票 (Output / report) | ✅ | `5100`–`5700-FMT` — status/message + count/amount lines per utility (content variants) | IT_UTIL_HAPPY_019 (xref column-level), _020 (STMB amounts), _021 (error-status variant) |
| データ整合性・冪等性 (Data integrity / idempotency) | ❌ — OCUTIL performs **no** C/U/D (§5 CRUD = none; navigation only). Commit/idempotency belong to the linked `OU*` utilities (their own test sets). Pressing ENTER again re-links with no in-screen guard — noted for the utility suites. | UI §5; AST has no file/DB verb | — |
| 排他・同時実行 (Exclusion / concurrent access) | ❌ — no record lock / I-O in OCUTIL (no file access); concurrency is a utility concern | AST (no OPEN/READ/lock verb) | — |

**D. Abnormal / boundary**

| Base viewpoint | Applicable? | Evidence | Planned TC |
|---|---|---|---|
| メッセージ・異常系 (Messages / abnormal) | ✅ | one dedicated TC per live message (M2 LINK-fail here; M4–M8 under 入力/操作性; M1 display, M3 happy) | UT_UTIL_ABNORMAL_006 (M2) + M4/M5/M6/M7/M8 rows above |
| 境界値 (Boundary values) | ✅ | Option 1–7 both sides (0/1…7/8/99); Max count `9(07)` max + blank/0; Cycle numeric-only vs month | UT_UTIL_BOUNDARY_003–008 (+ _001 format, _002 parse) |
| 回復・リラン (Recovery / rerun) | ❌ — pseudo-conversational; PF4/CLEAR re-display is the only "recovery", covered under 操作性. No mid-flow commit to recover (no persistence in OCUTIL) | AST | — (see 操作性) |

**E. Non-functional / operation**

| Base viewpoint | Applicable? | Evidence | Planned TC |
|---|---|---|---|
| 権限・セキュリティ (Authority / security) | ❌ (in-program) — OCUTIL does not check `CA-USER-TYPE`/signon itself; the gate is upstream (`OCSGNON` signon + `OCADMEN`). Recorded as an F-STD precondition (authorized admin operator). | AST (no `CA-USER-*` reference); KCOMM `CA-USER-ADMIN/NORMAL` set upstream | — (F-STD precondition) |
| ログ・監査 (Log / audit) | ❌ — OCUTIL writes no audit/log record (no WRITE to a log file) | AST | — |
| 連携・インターフェース (Linkage / interface) | ✅ | `EXEC CICS LINK PROGRAM(WS-LINK-PGM) COMMAREA(ORION-COMMAREA)` (7 utilities) + request/result hand-off `Kxx-PARM ↔ CA-WORK-AREA`; XCTL→OCADMEN | covered by IT_UTIL_HAPPY_011–017 (each LINK), _006/_007 (XCTL), _019 (result hand-back) |
| 性能・運用 (Performance / operation) | ❌ — no volume/paper/printer setup on this screen; the Max-count cap is a functional param (境界値), response is runtime/customer-decided | — | — |

**App-specific viewpoints revealed by the spec:** none beyond the base set. The two behaviours worth flagging — (a) the driver's completion message keys only on the LINK RESP, not the utility status; (b) Mode/Cutoff-Due are forwarded without driver validation — are captured under 出力・帳票 (IT_UTIL_HAPPY_021) and 入力チェック (IT_UTIL_HAPPY_005) respectively.

## Coverage matrix

- **Classifier values (options 1–7):** 7/7 happy flows → IT_UTIL_HAPPY_011–017.
- **Live messages:** 8/8 covered (M1 IT_001; M2 UT_ABNORMAL_006; M3 IT_011–021; M4 UT_ABNORMAL_001; M5 UT_ABNORMAL_002 + boundaries 003/004/005; M6 UT_ABNORMAL_003; M7 UT_ABNORMAL_004; M8 UT_ABNORMAL_005). No dead message to exclude.
- **AID keys:** ENTER (IT_011–017), PF3 (IT_006), PF12 (IT_007), PF4 (IT_008), CLEAR (IT_009), invalid key (UT_ABNORMAL_005) = 6/6 EIBAID branches.
- **Option boundary (valid 1–7, field 2-char `9(02)`):** below-min 0 (UT_BOUNDARY_003), min 1 accepted (IT_011), max 7 accepted (IT_017), above-max 8 (UT_BOUNDARY_004), field-max 99 (UT_BOUNDARY_005).
- **Max count / Cycle:** 7-digit field max accepted (UT_BOUNDARY_006), blank→0/no-cap (UT_BOUNDARY_007), embedded-space parse (UT_BOUNDARY_002), non-numeric max (UT_ABNORMAL_003), non-numeric cycle (UT_ABNORMAL_004), numeric-but-invalid-month cycle accepted (UT_BOUNDARY_008).
- **Mode/Cutoff-Due:** pass-through (no driver validation) IT_005; blank-Mode default REBL/BOTH IT_018.
- **Result variants:** xref count-only column-level (IT_019), statement monetary totals (IT_020), utility-error status but driver-complete (IT_021), format edit masks (UT_BOUNDARY_001).
- **LINK hand-off / XCTL:** 7 LINK + XCTL→OCADMEN covered via the flow/key TCs; LINK failure → M2 (UT_ABNORMAL_006).

**Totals: 35 TC** — 21 Normal (IT) · 8 Boundary (UT_…_BOUNDARY) · 6 Abnormal (UT_…_ABNORMAL). Excluded viewpoints (7) each carry a reason above; no dead code tested.

## ID scheme

`{IT|UT}_{AREA}_{CLASS}_{NNN}` — AREA = **UTIL** (unique for this run; siblings use OPS, ACCTOPEN…), IT=Normal, UT=Boundary/Abnormal, CLASS ∈ {HAPPY, BOUNDARY, ABNORMAL}. Automation: AS-IS CICS app, no automation harness → `auto = ×`, `automation_id` empty.

## Assumptions (non-interactive run — recorded, taken as approved)

1. The viewpoint table above (every base UI viewpoint decided; 7 excluded-with-reason; no extra app-specific viewpoint) is taken as signed off (CLI/CI invocation, no interactive gate).
2. This system uses **literal on-screen messages, not `EI/EF/GF` codes** — abnormal expected cites the verbatim `「…」` string. (`audit_testcase.py` `[vague]` flags on abnormal cases are dismissed on this basis.)
3. LINK-failure (M2) requires a runtime condition (target utility disabled/undefined → `PGMIDERR`) — marked `*(needs real data)*` where the fault must be injected. The utility-internal error status (IT_UTIL_HAPPY_021) likewise needs sub-program data to reproduce.
4. Mode and Cutoff/Due are forwarded without driver-side validation; their format/business validity is the linked utility's concern (documented in IT_UTIL_HAPPY_005, not fabricated as OCUTIL errors).
5. The result count/amount values in the 出力 TCs are illustrative literals (constructible from the `WS-E9`/`WS-EA` edit masks); the exact figures originate in the linked utilities' data.
6. Author of all deliverables is fixed = `modernizeX`.

## Audit result

`python3 audit_testcase.py cases.json --reg <REG> --root OCUTIL` → 35 cases, 12 viewpoints. Candidates below — all grounded and dismissed (no id/field/wording/fold/parity/auto issues):

| Candidate | Disposition |
|---|---|
| `[valid] could not run msg_codes … skip code coverage` | **Dismissed.** The reg uses a nested layout (`parsed/cobol_xml/ORION-CCMS/cbl/OCUTIL.xml`); the audit's `_root_xml` only looks under `main/`\|`sub/`, so it skipped the code-coverage step. Ran the design skill's `msg_codes.py` directly on the correct xml → **0 codes / 0 STOP** (this app uses literal on-screen messages). Nothing for the coverage check to find. REG is read-only → no symlink added. |
| `[vague] UT_UTIL_ABNORMAL_00x … no message code / STOP` (×6 abnormal) | **Dismissed.** Each expected cites the verbatim assertion string (M2/M4/M5/M6/M7/M8) in `「…」`; the check only recognises `EI/EF`-style codes, which this codebase does not use (assumption #2). |

**No `[wording]` candidate** — all code identifiers stay in the 備考 (remark) column; case bodies read as business.

**Spot-check (3 random TCs re-verified against AST):**
- `IT_UTIL_HAPPY_013` (Option 3) → `3300-DO-FLAG` L301-302: blank Mode → `'BOTH'` to `KFL-MODE`; `'OUFLAG'` to `WS-LINK-PGM` L310; `5300-FMT-FLAG` labels Read/Delinq/B30/B60/B90/Expired/Errors/More ✓
- `UT_UTIL_ABNORMAL_001` (blank option) → `2150-EDIT-OPTION` L215-217: `WS-NC-IN = SPACES` → `WS-INVALID` + `"Enter a utility number 1 through 7."` (distinct from M5) ✓
- `UT_UTIL_BOUNDARY_006` (Max 9999999) → `WS-MAX-NUM PIC 9(07)` L49; `2200` len 7 parse → `KIM-MAX` L320 ✓

**Verdict: PASS** (structural/coverage clean; the candidates are false positives grounded to the literal-message model of this system).
