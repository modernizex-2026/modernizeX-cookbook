# NOTES — Test-case set for OCOPS (Admin Batch Operations)

- **App / root:** `OCOPS` — CICS transaction **OROP**, mapset **MOPS**, map **MOPSA**
- **Kind (by evidence, NOT root_type):** **UI (CICS/BMS)** — `design_evidence.py` → `kind=ui` ("a UI document exists in the design set"); the reg also owns BMS map `MOPSA`. UI viewpoint catalog (A–E) applies, sourced from the BMS map + handler AST.
- **REG (ground truth, read-only):** `/Users/thangnguyen/Library/Caches/.skr/run-65766/reg`
- **Design evidence (read-only):**
  - UI doc: `design_asis/UI/OCOPS_BatchOperations_ScreenDesign.md`
  - AST: `reg/parsed/cobol_xml/ORION-CCMS/cbl/OCOPS.xml` (519 lines)
  - Copybooks: `KOPS` (request/result area), `WMSG` (message constants), `KCOMM` (`ORION-COMMAREA`), `WHEAD` (header), `MOPS` (BMS symbolic map)
  - Design run NOTES: `reg/datastore/design_runs/design_asis/NOTES.md`
- **OUT:** `.../design_runs/test_cases/OCOPS/` — this NOTES.md · `cases.json` · `OCOPS_TestCases.xlsx`
- **Deliverable language:** English (fixed by skill edition). On-screen text here is already English → quoted verbatim in `「…」`.

## Platform note — message model for this app

ORION-CCMS is an IBM z/OS **CICS/BMS** system. It uses **no `EI/EF/GF/ER` message-code scheme**; the program `MOVE`s a literal English string into the on-screen message field (`ERRMSGO`) or into a `WMSG` copybook constant. Verified: the design skill's `msg_codes.py OCOPS.xml` returns **0 codes, 0 STOP-literals** (matches design NOTES assumption #5). Therefore every abnormal case's expected result cites the **verbatim on-screen message string** `「…」` rather than a code. (This is why `audit_testcase.py` will emit `[vague] …異常系 case but expected cites no message code / STOP` for each abnormal TC — a false positive for this codebase; dismissed as a class, see Audit result.)

## What OCOPS does (from OCOPS.xml)

Pseudo-conversational dispatcher (`RETURN TRANSID('OROP')`). On first entry (`EIBCALEN = 0`, or commarea state `CA-FIRST-ENTER`) it sends the initial menu (`1000-SEND-INITIAL`); otherwise it processes the AID key (`2000-PROCESS-INPUT` → `EVALUATE EIBAID`):

| AID | Handler | Behaviour |
|---|---|---|
| `ENTER` | `2100-RUN-OPERATION` | validate Option (1–8), parse Param, `LINK` to the matching `OU*` sub-program, show result |
| `PF3` / `PF12` | `7000-XCTL-MENU` | `XCTL` to Main Menu `OCMENU` |
| `PF4` / `CLEAR` | `1000-SEND-INITIAL` | clear + redisplay the menu |
| any other | `2000` WHEN OTHER | message `WS-MSG-INVALID-KEY` → re-send data-only |

Option → sub-program (`7000-BUILD-REQUEST` `EVALUATE WS-OPTION`), each with a `KO-FUNCTION` code:

| Option | On-screen label | Sub-program (LINK) | KO-FUNCTION |
|---|---|---|---|
| 1 | `1. Post Daily Transactions` | OUPOST | `POST` |
| 2 | `2. Post Bill Payment` | OUPAY | `PAY ` |
| 3 | `3. Assess Interest` | OUINT | `INT ` |
| 4 | `4. Assess Fees` | OUFEE | `FEE ` |
| 5 | `5. Charge-Off Delinquent` | OUCHGF | `CHGF` |
| 6 | `6. Close Expired/Flagged` | OUCLOS | `CLOS` |
| 7 | `7. Renew / Reissue Cards` | OURNEW | `RNEW` (+ whole Param → `KO-PARM-CARD`) |
| 8 | `8. Cycle / EOM Roll` | OUCYCL | `CYCL` |

Param parsing (`6000/6100/6200`): `UNSTRING PARMI` → token1 = account (digits only, first **11** positions → `KO-PARM-ACCT PIC 9(11)`), token2 = amount (`KO-PARM-AMT PIC S9(10)V99`, `.`-aware, fraction capped at 2 dp, non-digits ignored). Result (`7500-SHOW-RESULT`): `KO-STATUS` `O/W/E` → `「OK」/「WARNING」/「ERROR」`, counts (`KO-*-CNT PIC 9(09)` → display `ZZZ,ZZZ,ZZ9`) and amounts (`KO-AMT-1/2 PIC S9(13)V99` → display `-,---,---,--9.99`), message `「Operation complete. Review the counts below.」`.

### Live on-screen messages (all covered) vs dead

| # | Verbatim string | Raised at | Coverage |
|---|---|---|---|
| M1 | `Select an operation (1-8) and press ENTER.` | `1000-SEND-INITIAL` L99 (info, initial) | IT_OPS_HAPPY_001 |
| M2 | `Invalid option. Enter 1 through 8.` | `2100-RUN-OPERATION` L154 (Option not 1–8) | UT_OPS_ABNORMAL_001, _002; boundaries UT_OPS_BOUNDARY_006/007/008 |
| M3 | `Operation could not be started. Contact support.` | `2200-LINK-SUB` L173 (LINK RESP ≠ NORMAL) | UT_OPS_ABNORMAL_004 |
| M4 | `Operation complete. Review the counts below.` | `7500-SHOW-RESULT` L313 (success) | IT_OPS_HAPPY_010–021 |
| M5 | `Invalid key pressed. Please try again.` (`WS-MSG-INVALID-KEY`) | `2000-PROCESS-INPUT` WHEN OTHER L125 | UT_OPS_ABNORMAL_003 |
| — (dead) | `OCOPS: unrecoverable error. Contact support.` | `9500-ABEND-RTN` L379 | **EXCLUDED** — paragraph unreferenced (grep: 1 hit = its own definition; no `HANDLE ABEND`/`PERFORM` wires it in the active AST). Do not test dead code. |

## Standard fixture — `F-STD`

The ORION-CCMS CICS region is up; the operator is **signed on as an authorized admin operator** (`OCSGNON`/menu enforce signon; OCOPS itself does not re-check `CA-USER-TYPE`). The 8 processing sub-programs (OUPOST, OUPAY, OUINT, OUFEE, OUCHGF, OUCLOS, OURNEW, OUCYCL) are installed and enabled. The VSAM/DB2 stores hold representative account, card and transaction data so each operation returns non-zero counts. The operator reaches `MOPSA` by selecting Admin Batch Operations from the Main Menu (`OCMENU`), which starts transaction OROP. Each TC states **F-STD + only its delta**.

## Viewpoint table (観点表) — every base UI viewpoint decided

Approval gate is non-interactive → recorded here as an **assumption** and taken as approved (see Assumptions). 中項目 uses the canonical `<JP> (<EN>)` labels.

**A. Display / UI**

| Base viewpoint (中項目) | Applicable? | Evidence | Planned TC |
|---|---|---|---|
| 画面表示・レイアウト (Screen display / layout) | ✅ | §2 MOPSA mockup + item details; `1000-SEND-INITIAL`, `8000-POPULATE-HEADER` | IT_OPS_HAPPY_001, _002 |
| 初期値・デフォルト (Initial values / defaults) | ✅ | Option/Param empty on open; `OPTION` `UNPROT/IC/FSET` (initial focus); M1 shown | IT_OPS_HAPPY_003 |
| 表示条件・活性制御 (Display conditions / enable control) | ✅ | §2 item-states (result fields `□` display-only; Option/Param `○`); `ASKIP` attrs | IT_OPS_HAPPY_004 |
| 文字・書式表示 (Characters / format display) | ✅ | `WS-D-CNT PIC ZZZ,ZZZ,ZZ9`, `WS-D-AMT PIC -,---,---,--9.99`; date/time from FORMATTIME | UT_OPS_BOUNDARY_001 |

**B. Input / operation**

| Base viewpoint | Applicable? | Evidence | Planned TC |
|---|---|---|---|
| 入力チェック (Input validation) | ✅ | `2100` `IF OPTIONI NUMERIC`; `7000-BUILD-REQUEST` `EVALUATE WS-OPTION` OTHER → SPACES → M2 | UT_OPS_ABNORMAL_001 (non-numeric), _002 (blank) |
| 操作性・キー (Operability / function keys) | ✅ | `2000` `EVALUATE EIBAID` — ENTER/PF3/PF12/PF4/CLEAR/OTHER | IT_OPS_HAPPY_005–008, UT_OPS_ABNORMAL_003; ENTER via IT_OPS_HAPPY_010–017 |
| 状態遷移 (Screen / state transitions) | ✅ | `0000-MAIN` `EIBCALEN`/`CA-FIRST-ENTER` pseudo-conversational; XCTL→OCMENU | IT_OPS_HAPPY_009; XCTL via IT_OPS_HAPPY_005/006 |

**C. Function / data**

| Base viewpoint | Applicable? | Evidence | Planned TC |
|---|---|---|---|
| 機能・業務フロー (Function / business flow) | ✅ | `7000-BUILD-REQUEST` — one classifier value per option 1–8 | IT_OPS_HAPPY_010–017 (8) |
| 計算・編集ロジック (Calculation / editing logic) | ✅ | `6100-PARSE-ACCT` / `6200-PARSE-AMT` (account & amount parse) | IT_OPS_HAPPY_018; UT_OPS_BOUNDARY_002–005 |
| 出力・帳票 (Output / report) | ✅ | `7500-SHOW-RESULT` — status O/W/E, 8 counts, 2 amounts, M4 | IT_OPS_HAPPY_019 (OK, column-level), _020 (WARNING), _021 (ERROR) |
| データ整合性・冪等性 (Data integrity / idempotency) | ❌ — OCOPS performs **no** C/U/D (§5 CRUD = none; navigation only). Commit/idempotency belong to the linked `OU*` sub-programs (their own test sets). Re-running an operation by pressing ENTER again re-links with no in-screen guard — noted for the sub-program suites. | UI §5; AST has no file/DB verb | — |
| 排他・同時実行 (Exclusion / concurrent access) | ❌ — no record lock / I-O in OCOPS (no file access); concurrency is a sub-program concern | AST (no OPEN/READ/lock verb) | — |

**D. Abnormal / boundary**

| Base viewpoint | Applicable? | Evidence | Planned TC |
|---|---|---|---|
| メッセージ・異常系 (Messages / abnormal) | ✅ | one dedicated TC per live message (M2–M5); M1 = display, M4 = happy | UT_OPS_ABNORMAL_001–004 (+ M1/M4 above) |
| 境界値 (Boundary values) | ✅ | Option field `9(02)` valid 1–8 → both sides; Param 11-digit / 2-dp limits | UT_OPS_BOUNDARY_002–008 |
| 回復・リラン (Recovery / rerun) | ❌ — pseudo-conversational; PF4/CLEAR re-display is the only "recovery", covered under 操作性. No mid-flow commit to recover (no persistence in OCOPS) | AST | — (see 操作性) |

**E. Non-functional / operation**

| Base viewpoint | Applicable? | Evidence | Planned TC |
|---|---|---|---|
| 権限・セキュリティ (Authority / security) | ❌ (in-program) — OCOPS does not check `CA-USER-TYPE`/signon itself; the gate is upstream (`OCSGNON` signon + menu). Recorded as an F-STD precondition (authorized admin operator). | AST (no `CA-USER-*` reference); KCOMM `CA-USER-ADMIN/NORMAL` set upstream | — (F-STD precondition) |
| ログ・監査 (Log / audit) | ❌ — OCOPS writes no audit/log record (no WRITE to a log file) | AST | — |
| 連携・インターフェース (Linkage / interface) | ✅ | `EXEC CICS LINK PROGRAM(WS-SUB-PGM) COMMAREA(ORION-COMMAREA)` (8 sub-programs) + request/result hand-off `KOPS-AREA ↔ CA-WORK-AREA`; XCTL→OCMENU | covered by IT_OPS_HAPPY_010–017 (each LINK), _005/_006 (XCTL), _019 (result hand-back) |
| 性能・運用 (Performance / operation) | ❌ — no volume/paper/printer setup on this screen; response is runtime/customer-decided | — | — |

**App-specific viewpoints revealed by the spec:** none beyond the base set. (Param routing — option 7 sends the whole Param to the card field, others parse account+amount — is captured under 計算・編集ロジック / 機能・業務フロー.)

## Coverage matrix

- **Classifier values (options 1–8):** 8/8 happy flows → IT_OPS_HAPPY_010–017.
- **Live messages:** 5/5 covered (M1 IT_001; M2 UT_ABNORMAL_001/002 + boundaries; M3 UT_ABNORMAL_004; M4 IT_010–021; M5 UT_ABNORMAL_003). 1 dead message excluded with reason (abend).
- **AID keys:** ENTER (IT_010–017), PF3 (IT_005), PF12 (IT_006), PF4 (IT_007), CLEAR (IT_008), invalid key (UT_ABNORMAL_003) = 6/6 EIBAID branches.
- **Option boundary (valid 1–8, field `9(02)`):** below-min 0 (UT_BOUNDARY_006), min 1 accepted (IT_010), max 8 accepted (IT_017), above-max 9 (UT_BOUNDARY_007), field-max 99 (UT_BOUNDARY_008).
- **Param parse:** account+amount happy (IT_018), 1-dp normalize (UT_BOUNDARY_002), >2-dp truncate (UT_BOUNDARY_003), non-digit ignore (UT_BOUNDARY_004), >11-digit account (UT_BOUNDARY_005).
- **Result variants:** OK (IT_019), WARNING (IT_020), ERROR (IT_021).
- **LINK hand-off / XCTL:** 8 LINK + XCTL covered via the flow/key TCs.

**Totals: 33 TC** — 21 Normal (IT) · 8 Boundary (UT_…_BOUNDARY) · 4 Abnormal (UT_…_ABNORMAL). Excluded viewpoints (7) each carry a reason above; 1 dead message excluded.

## ID scheme

`{IT|UT}_{AREA}_{CLASS}_{NNN}` — AREA = **OPS** (unique for this run), IT=Normal, UT=Boundary/Abnormal, CLASS ∈ {HAPPY, BOUNDARY, ABNORMAL}. Automation: AS-IS CICS app, no automation harness → `auto = ×`, `automation_id` empty.

## Assumptions (non-interactive run — recorded, taken as approved)

1. The viewpoint table above (every base UI viewpoint decided; 7 excluded-with-reason; no extra app-specific viewpoint) is taken as signed off (CLI/CI invocation, no interactive gate).
2. `9500-ABEND-RTN` and its message are **dead** (no `HANDLE ABEND`/`PERFORM` reference in the active AST) → excluded, not tested.
3. This system uses **literal on-screen messages, not `EI/EF/GF` codes** — abnormal expected cites the verbatim `「…」` string. (`audit_testcase.py` `[vague]` flags on abnormal cases are dismissed on this basis.)
4. LINK-failure (M3) requires a runtime condition (target sub-program disabled/undefined → `PGMIDERR`) — marked `*(needs real data)*` where the fault must be injected.
5. Parse results (account/amount/card) are passed into the request area (`KOPS-AREA`) and are not shown on `MOPSA`; parse TCs assert them via the linked operation's business effect + request-area trace (remark).
6. Author of all deliverables is fixed = `modernizeX`.

## Audit result

`python3 audit_testcase.py cases.json --reg <REG> --root OCOPS` → 33 cases, 12 viewpoints, **5 candidates — all grounded and dismissed** (no id/field/wording/fold/parity/auto issues):

| Candidate | Disposition |
|---|---|
| `[valid] could not run msg_codes … skip code coverage` | **Dismissed.** The reg uses a nested layout (`parsed/cobol_xml/ORION-CCMS/cbl/OCOPS.xml`); the audit's `_root_xml` only looks under `main/`\|`sub/`, so it skipped the code-coverage step. Ran the design skill's `msg_codes.py` directly on the correct xml → **0 codes / 0 STOP** (this app uses literal on-screen messages). Nothing for the coverage check to find. REG is read-only → no symlink added. |
| `[vague] UT_OPS_ABNORMAL_001 … no message code / STOP` | **Dismissed.** Expected cites the verbatim assertion 「Invalid option. Enter 1 through 8.」 — the check only recognises `EI/EF`-style codes, which this codebase does not use (assumption #3). |
| `[vague] UT_OPS_ABNORMAL_002 …` | **Dismissed** — same basis; expected cites 「Invalid option. Enter 1 through 8.」 (blank-option trigger). |
| `[vague] UT_OPS_ABNORMAL_003 …` | **Dismissed** — expected cites 「Invalid key pressed. Please try again.」 (`WS-MSG-INVALID-KEY`). |
| `[vague] UT_OPS_ABNORMAL_004 …` | **Dismissed** — expected cites 「Operation could not be started. Contact support.」 (LINK-fail). |

**No `[wording]` candidate** — all code identifiers stay in the 備考 (remark) column; case bodies read as business.

**Spot-check (3 random TCs re-verified against AST):**
- `IT_OPS_HAPPY_016` (Option 7) → `7000-BUILD-REQUEST` WHEN 7 L273-276: `OURNEW`/`RNEW` + `MOVE WS-PARM-IN TO KO-PARM-CARD` ✓
- `UT_OPS_ABNORMAL_003` (invalid key) → `2000` WHEN OTHER L124-125: `WS-MSG-INVALID-KEY` = "Invalid key pressed. Please try again." (WMSG) ✓
- `UT_OPS_BOUNDARY_005` (11-digit account) → `6100-PARSE-ACCT` PERFORM VARYING … UNTIL `WS-NW-POS > 11` L197; `KO-PARM-ACCT PIC 9(11)` ✓

**Verdict: PASS** (structural/coverage clean; the 5 candidates are false positives grounded to the literal-message model of this system).
