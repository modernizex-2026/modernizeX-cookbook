# NOTES — Test-case set for OCCUSIN (Customer Inquiry, consolidated)

- **App / root:** `OCCUSIN` — app_007, ORION-CCMS (ORION Credit Card Management System)
- **Kind (by evidence):** **UI (CICS/BMS)** — owns BMS map `MCUSINA` (mapset `MCUSIN`), CICS transaction `ORQC`. `design_evidence.py` → `kind: ui`.
- **Deliverable language:** English (fixed by skill edition). Verbatim on-screen message strings reproduced unchanged.
- **REG (ground truth):** `/Users/thangnguyen/Library/Caches/.skr/run-57032/reg`
- **Design evidence read (primary test basis):**
  - `design_asis/UI/OCCUSIN_CustomerInquiry_ScreenDesign.md` (screen spec, item states, checks §3, events §4)
  - Browse sub-program design `design_asis/Batch/OUCUSIN_CustomerInquiryBrowse_ProgramDesign.md`
  - Source cross-checked on the AST (`parsed/cobol_xml/ORION-CCMS/cbl/OCCUSIN.xml`, `OUCUSIN.xml`) and raw `OCCUSIN.cbl` / `OUCUSIN.cbl` — the design §3 lists only one message, so the **complete message + branch set was grounded on the source** (all 8 literal strings confirmed ACTIVE on the AST).
- **OUT:** `.../design_runs/test_cases/OCCUSIN/`
- **Author:** modernizeX. **Generated:** 2026-09-28. **Today (for date-relative tests):** 2026-09-28.

## Approval gate — recorded as an assumption (NON-INTERACTIVE run)

Per the runner's non-interactive instruction, the 観点表 (viewpoint table) below is **taken as approved** and recorded here as an assumption; the case set was written directly from it. If a reviewer rejects a viewpoint decision, adjust the table row and regenerate the affected cases from `cases.json`.

## What OCCUSIN does (grounded)

`OCCUSIN` is a **read-only, pseudo-conversational paged customer inquiry** that replaces three batch listings (OBCUSTP / OBRSCORE / OBRZIP). The operator picks a **filter mode** in the `Mode (I=ID F=FICO S=State/ZIP):` field and keys its arguments, then presses **ENTER**; the screen `EXEC CICS LINK`s the browse engine **`OUCUSIN`**, which scans the customer master file (`CUSTFILE`) and returns **one page of up to 13** matching customers (Cust ID, Name, ST, ZIP, FICO) plus filter-wide aggregates (match count and FICO min / avg / max). `PF8` pages forward, `PF7` restarts from the top, `PF3` returns to the Main Menu (`OCMENU`). No row is ever created/updated/deleted (design §5 = "read only").

**Three filter modes (classifier values), parsed in `5000-PARSE-FILTER`:**
- **I = customer-id range** (`5100`): `Cust ID From`/`To`; blank field → range default 000000000 … 999999999. Both fields parsed by `6000-PARSE-NUM` (9-char).
- **F = FICO score band** (`5200`): `FICO From`/`To`; blank field → band default 000 … 999. Both fields parsed by `6000-PARSE-NUM` (3-char).
- **S = state + ZIP prefix** (`5300`): `State` is **required**; `ZIP` optional, matched on its keyed leading characters (prefix).

**Browse selection rules** (`OUCUSIN` `2400-FILTER-CHECK` / `2450-CHECK-STATE-ZIP`): `I` = `CU-ID` between From/To **inclusive** · `F` = `CU-FICO-SCORE` between From/To **inclusive** · `S` = `CU-ADDR-STATE` equal, and (if a ZIP is keyed) `CU-ADDR-ZIP` leading `WS-ZIPLEN` chars equal the keyed prefix. The whole filtered set is scanned every call so the aggregates are genuine totals; the first `WS-MAX-ROWS` = **13** matches past the resume key form the page, and a further match sets the more-switch. Average FICO is `ROUNDED` (`3000-FINALIZE`).

### Messages (all 8 grounded from source — design §3 listed only #8)

| # | Verbatim string | Type | Trigger point (paragraph) | Covered by |
|---|---|---|---|---|
| 1 | `Choose filter mode I/F/S, key args, press ENTER.` | Info (prompt) | initial display `1000-SEND-INITIAL` | IT_..HAPPY_001/003 |
| 2 | `Mode must be I (id) F (fico) or S (state/zip).` | Error | mode not I/F/S `5000-PARSE-FILTER WHEN OTHER` | UT_..ABNORMAL_001 |
| 3 | `Numeric filter argument is not valid.` | Error | non-numeric id `5100` **or** FICO `5200` arg → `6000-PARSE-NUM` | UT_..ABNORMAL_002 (id), _003 (FICO) |
| 4 | `State is required for the state/zip filter.` | Error | mode S, blank state `5300-PARSE-STZIP` | UT_..ABNORMAL_004 |
| 5 | `No customers match the filter.` | Info | zero matches `4000-SHOW-PAGE` (row count 0) | UT_..ABNORMAL_006 (+ BOUNDARY_002/006 empty side) |
| 6 | `End of list - no more customers.` | Info | PF8 with no further page `2200-PAGE-NEXT` | UT_..ABNORMAL_007 |
| 7 | `Unable to link to browse engine OUCUSIN.` | Error | LINK to OUCUSIN fails `3000-CALL-SUB` | UT_..ABNORMAL_008 |
| 8 | `Invalid key pressed. Please try again.` | Error | unsupported AID key `2000-PROCESS-INPUT WHEN OTHER` | UT_..ABNORMAL_005 |
| — | `nn match, FICO min/avg/max mn/av/mx  PF8=more PF7=top` / `…  End of list.` | Info (summary) | page painted `4300-BUILD-SUMMARY` | IT_..HAPPY_006/008/010, BOUNDARY_007 |

> This CICS app raises **literal English message strings, not EI/EF/GF codes**. `msg_codes.py` on `OCCUSIN.xml` and `OUCUSIN.xml` finds **0 codes** (confirmed). The audit's "abnormal case cites no message code" and "could not run msg_codes" candidates are therefore expected and dismissed — the verbatim string in 期待結果 *is* the assertion (see Audit result below).

## 観点表 (viewpoint table) — every base UI viewpoint decided (assumed approved)

中項目 uses the canonical `<JP> (<EN>)` labels. Evidence cited from the UI doc (§) or the AST/source.

| # | 観点 (中項目) | Applicable? | Evidence | Planned TC |
|---|---|---|---|---|
| U1 | 画面表示・レイアウト (Screen display / layout) | ✅ | §2.1 mockup MCUSINA + item list (header, mode prompt, id/FICO/state/ZIP entry fields, column headers, 13-row list, ERRMSG line, key legend) | IT_..HAPPY_001, _002 |
| U2 | 初期値・デフォルト (Initial values / defaults) | ✅ | `1000-SEND-INITIAL`: mode pre-filled `'I'`, KUI-F-IDRANGE set, prompt shown, list blank, cursor on mode field (`MOVE -1 TO FMODEL`) | IT_..HAPPY_003 |
| U3 | 表示条件・活性制御 (Display conditions / enable control) | ✅ | §2.1 item-states: 7 entry fields `○` (UNPROT) / header + all list & header fields `□` (ASKIP) | IT_..HAPPY_004 |
| U4 | 文字・書式表示 (Characters / format display) | ✅ | KCUSIN row PIC: Cust ID 9(09), Name X(30), ST X(02), ZIP X(10), FICO 9(03); summary edit ZZZZZZZ9 / ZZ9 | UT_..BOUNDARY_001 |
| U5 | 入力チェック (Input validation) | ✅ | mode (I/F/S) `5000`; numeric id/FICO `6000-PARSE-NUM`; state-required `5300` — per-field validation | UT_..ABNORMAL_001–004 |
| U6 | 操作性・キー (Operability / function keys) | ✅ (AID keys) | `2000-PROCESS-INPUT` EVALUATE EIBAID: ENTER / PF7 / PF8 / PF3 / OTHER | IT_..HAPPY_011 (ENTER re-filter), _012 (PF3); PF7/PF8 → U7; OTHER → ABNORMAL_005 |
| U7 | 状態遷移 (Screen / state transitions) | ✅ | pseudo-conv. paging state in CA-WORK-AREA: ENTER→page1, PF8→next, PF7→top | IT_..HAPPY_013 (PF7), _014 (PF8) |
| U8 | 機能・業務フロー (Function / business flow) | ✅ (per classifier) | 3 filter modes (I / F / S) — OUCUSIN 2400/2450 | IT_..HAPPY_005 (I), _006 (F), _007 (S), _008 (I blank→all), _009 (S+zip) |
| U9 | 計算・編集ロジック (Calculation / editing logic) | ✅ | filter-wide match count + FICO min/avg/max (`4300-BUILD-SUMMARY`, OUCUSIN `3000-FINALIZE` avg ROUNDED); "First Last" name build `2600-BUILD-NAME` | IT_..HAPPY_010 (+ name in HAPPY_002) |
| U10 | 出力・帳票・メール (Output / report / mail) | ❌ N/A | inquiry only; no report file / print / mail — the on-screen list is the output, covered under U1/U8 | — |
| U11 | データ整合性・冪等性 (Data integrity / idempotency) | ✅ (idempotency only) | read-only (§5 no C/U/D); re-list/restart returns the same set (whole-file scan is deterministic) | IT_..HAPPY_015 |
| U12 | 排他・同時実行 (Exclusion / concurrent) | ❌ N/A | read-only browse (STARTBR/READNEXT/ENDBR, no lock/update); no update contention | — |
| U13 | メッセージ・異常系 (Messages / abnormal) | ✅ | system/flow messages: invalid key, no match, PF8 end-of-list, LINK failure (messages #5–8 above) | UT_..ABNORMAL_005–008 |
| U14 | 境界値 (Boundary values) | ✅ | id range inclusive/inverted; id 9-digit width; FICO band inclusive; FICO 3-digit width; ZIP prefix length; page size 13/14 | UT_..BOUNDARY_002–007 |
| U15 | 回復・リラン (Recovery / rerun) | ⚠️ covered elsewhere | pseudo-conv. re-entry & error recovery — screen stays on validation/LINK error, operator retries; no partial commit to recover (read-only) | folded into HAPPY_013/015, ABNORMAL_007/008 |
| U16 | 権限・セキュリティ (Authority / security) | ❌ excluded (reason) | OCCUSIN has **no signon/authority check of its own** (no login field, no dept/mode gate in source); "authenticated operator" is enforced upstream at Sign On before the menu | — |
| U17 | ログ・監査 (Log / audit) | ❌ excluded (reason) | no audit/log record written; OUCUSIN writes nothing (pure browse) | — |
| U18 | 連携・インターフェース (Linkage / interface) | ✅ | `EXEC CICS LINK PROGRAM('OUCUSIN')` with KCUSIN-AREA; `XCTL PROGRAM('OCMENU')` on PF3 | IT_..HAPPY_016 (LINK), _012 (XCTL menu) |
| U19 | 性能・運用 (Performance / operation) | ❌ deferred (runtime) | OUCUSIN scans the whole file on every call — throughput is a runtime/volume concern; no paper/printer/env instruction on this screen | — |

**CICS-specific instantiation applied** (per viewpoints.md "CICS/BMS UI app"): 操作性・キー = **AID keys** (ENTER / PF3 / PF7 / PF8 / other) from the `EIBAID` EVALUATE, not ESTS; 連携/状態遷移 = **CICS transaction navigation** (`LINK` OUCUSIN, `XCTL` OCMENU, `RETURN TRANSID('ORQC')`); 画面表示 grounded in the real `MCUSINA` DFHMDF field positions.

## Coverage matrix (viewpoint → evidence rows → TC)

- **Filter modes / classifier values (3/3):** I→HAPPY_005 (+ blank-default HAPPY_008) · F→HAPPY_006 · S→HAPPY_007 (+ ZIP-prefix HAPPY_009).
- **AID keys (5/5):** ENTER→HAPPY_005–011, _016 · PF7→HAPPY_013/015 · PF8→HAPPY_014 & ABNORMAL_007 · PF3→HAPPY_012 · unsupported→ABNORMAL_005.
- **Messages (8/8):** see the message table above — every string has a dedicated (abnormal) or covering (info/summary) TC. The shared `Numeric filter argument is not valid.` is split by **distinct trigger point** (id field ABNORMAL_002 vs FICO field ABNORMAL_003) per the density rule.
- **Boundaries (both sides):** id From=To single-id / To<From empty → BOUNDARY_002 · id 000000000/999999999 width → BOUNDARY_003 · FICO 680 included / 681 excluded → BOUNDARY_004 · FICO 000/999 width → BOUNDARY_005 · ZIP 1-char prefix match / 99999 no-match → BOUNDARY_006 · page 13 exact / 14 spill → BOUNDARY_007.
- **Interface (2/2 hand-offs):** LINK OUCUSIN → HAPPY_016 · XCTL OCMENU (PF3) → HAPPY_012.
- **Excluded viewpoints (with reason):** U10, U12, U16, U17, U19 — reasons in the 観点表. U15 folded into HAPPY_013/015 / ABNORMAL_007/008.

**Count:** 31 TC — 16 Normal (IT), 7 Boundary (UT), 8 Abnormal (UT).

## Fixtures

**F-STD (standard fixture)** — the real `CUSTFILE` seed shipped with the source (`input/10/ORION/ORION-CCMS/data/CUSTFILE.txt`, VSAM KSDS keyed on the 9-digit customer id, laid out per copybook RCUST). 5 customers:

| Cust ID | Name (First Last) | State | ZIP | FICO |
|---|---|---|---|---|
| 000000001 | JOHN SMITH | NY | 10001 | 720 |
| 000000002 | MARIA GARCIA | CA | 94105 | 680 |
| 000000003 | ROBERT JOHNSON | IL | 60601 | 640 |
| 000000004 | LINDA WILLIAMS | FL | 33101 | 750 |
| 000000005 | DAVID BROWN | WA | 98101 | 700 |

The **Name** column is `First + Last` (the middle name is dropped by `2600-BUILD-NAME`). With F-STD: mode `I` all → 5 rows, FICO min/avg/max = 640/698/750 (avg 3490/5 = 698) · `I` 000000002…000000004 → 3 rows (ids 2,3,4) · `F` 700…999 → 3 rows (720,750,700), min/avg/max 700/723/750 · `F` 680…720 → 3 rows (680,700,720) · `S` CA → 1 row (id 2) · `S` NY ZIP `100` → 1 row (id 1, ZIP 10001). All five FICO scores (640/680/700/720/750) already sit inside the real 300…850 band, so the aggregate maths are exercised by F-STD alone.

**F-PAGE (paging delta)** — F-STD extended so a single filter fills more than one 13-row page: customer ids `000000001`…`000000020` (20 customers, constructed to the RCUST layout — id, first/last name, state, ZIP, FICO), all matched by mode `I` with blank bounds. Trimmed to exactly 13 for the lower page-size boundary and 14 for the spill boundary. Needed by IT_..HAPPY_014 and UT_..BOUNDARY_007 — the shipped `CUSTFILE` seed has only 5 rows, so multi-page paging cannot be reproduced from the seed alone.

Customer ids / names / states / ZIP / FICO in F-STD are the customer's real seed values (reproduced unchanged). F-PAGE extra rows are constructed from the copybook PIC formats (no business fact invented beyond the id/FICO/state needed to fill a page). ABNORMAL_008 (LINK failure) needs the browse engine forced unavailable in the CICS region — marked `*(needs real data)*` (a fault-injection setup, not a data value).

## Audit result

`audit_testcase.py cases.json --reg <REG> --root OCCUSIN` — 9 candidates, all reviewed and dismissed (grounded):

- **`[valid] could not run msg_codes`** (×1) — *dismissed*: the audit's `_root_xml` looks under `parsed/cobol_xml/main|sub/`, but this reg stores XML under `ORION-CCMS/cbl/`. Ran `msg_codes.py` directly on `OCCUSIN.xml` and `OUCUSIN.xml` → **0 codes** (this app uses literal English messages, not EI/EF/GF codes). No code-coverage gap possible.
- **`[vague] … 異常系 case but expected cites no message code / STOP`** (×8, one per abnormal TC) — *dismissed as a class*: the app has no EI/EF codes; each abnormal 期待結果 quotes the **verbatim on-screen message string** (e.g. `Mode must be I (id) F (fico) or S (state/zip).`), which is the real assertion. Every abnormal expected carries a literal quoted string.
- All other checks — unique ids, no empty required fields, every case has a canonical 中項目 (13 viewpoints, all from the catalog), no folding of multiple codes into one abnormal row, no code-leak/wording flags (COBOL identifiers kept in 備考 only), automation columns `×`/empty — **clean**.

Judgment dims hand-reviewed: 3 TCs spot-checked against the source — HAPPY_006 (FICO band + avg 723 ROUNDED), ABNORMAL_004 (state-required `5300`), BOUNDARY_007 (page size 13 / more-switch on the 14th) — all trace to the cited paragraphs. Steps reproducible with F-STD (F-PAGE for paging boundaries); 観点表 complete vs the spec.
