# NOTES — Test-case set for OCCRDIN (Card Inquiry, consolidated)

- **App / root:** `OCCRDIN` — app_006, ORION-CCMS (ORION Credit Card Management System)
- **Kind (by evidence):** **UI (CICS/BMS)** — owns BMS map `MCRDINA` (mapset `MCRDIN`), CICS transaction `ORCI`. `design_evidence.py` → `kind: ui`.
- **Deliverable language:** English (fixed by skill edition). Verbatim on-screen message strings reproduced unchanged.
- **REG (ground truth):** `/Users/thangnguyen/Library/Caches/.skr/run-57032/reg`
- **Design evidence read (primary test basis):**
  - `design_asis/UI/OCCRDIN_CardInquiry_ScreenDesign.md` (screen spec, item states, checks §3, events §4)
  - Browse sub-program design `design_asis/Batch/OUCRDIN_CardInquiryBrowse_ProgramDesign.md`
  - Source cross-checked on the AST (`parsed/cobol_xml/ORION-CCMS/cbl/OCCRDIN.xml`, `OUCRDIN.xml`) and raw `OCCRDIN.cbl` / `OUCRDIN.cbl` — the design §3 lists only one message, so the **complete message + branch set was grounded on the source** (all confirmed ACTIVE on the AST).
- **OUT:** `.../design_runs/test_cases/OCCRDIN/`
- **Author:** modernizeX. **Generated:** 2026-09-28. **Today (for date-relative tests):** 2026-09-28.

## Approval gate — recorded as an assumption (NON-INTERACTIVE run)

Per the runner's non-interactive instruction, the 観点表 (viewpoint table) below is **taken as approved** and recorded here as an assumption; the case set was written directly from it. If a reviewer rejects a viewpoint decision, adjust the table row and regenerate the affected cases from `cases.json`.

## What OCCRDIN does (grounded)

`OCCRDIN` is a **read-only, pseudo-conversational paged card inquiry**. The operator types a **filter keyword** into the `Filter:` field and presses **ENTER**; the screen `EXEC CICS LINK`s to the browse engine **`OUCRDIN`**, which walks the card master file (`CARDFILE`) and returns **one page of up to 13** matching cards (Card Number, Acct ID, Embossed Name, Expiry, Status) plus whole-file **Active / Inactive** tallies. `PF8` pages forward, `PF7` restarts from the top, `PF3`/`PF12` return to the Main Menu (`OCMENU`), `PF4`/`CLEAR` reset the screen. No row is ever created/updated/deleted (design §5 = "read only").

**Filter keyword parsing** (`6000-PARSE-FILTER`): input is up-cased, then the **leading 3 characters** are matched — blank→`ALL`, `ALL`→ALL, `ACT`→ACTIVE, `INA`→INACTIVE, `EXP`→Expiring-Soon; anything else → *"Filter not recognised"*.

**Browse selection rules** (`OUCRDIN` 4510–4540): `ALL`=every card · `ACTIVE`=status `Y` · `INACTIVE`=status not `Y` · `Expiring-Soon`=expiry within the next **60 days** from today, inclusive, not already past; undated / unparseable expiry is not selected. Page size = **13** (`WS-MAX-ROWS`).

### Messages (all 10 grounded from source — design §3 listed only #8)

| # | Verbatim string | Type | Trigger point (paragraph) | Covered by |
|---|---|---|---|---|
| 1 | `Type a filter and press ENTER.` | Info (prompt) | initial display `1000-SEND-INITIAL` | IT_CARDINQ_HAPPY_001/003 |
| 2 | `Filter not recognised - see the list above.` | Error | ENTER, unrecognised keyword `2100-APPLY-FILTER` | UT_CARDINQ_ABNORMAL_002 |
| 3 | `No cards match that filter.` | Info | zero matches `4000-SHOW-PAGE` (row count 0) | UT_CARDINQ_ABNORMAL_003 |
| 4 | `End of selection - no more cards.` | Info | PF8 with no further page `2300-PAGE-FWD` | UT_CARDINQ_ABNORMAL_004 |
| 5 | `Apply a filter first (press ENTER).` | Warn | PF7 **and** PF8 before any ENTER `2200/2300` | UT_CARDINQ_ABNORMAL_005 (PF7), _006 (PF8) |
| 6 | `Unable to reach the card browse engine.` | Error | LINK to OUCRDIN fails `3000-CALL-SUB` | UT_CARDINQ_ABNORMAL_007 |
| 7 | `Error browsing the card file.` | Error | OUCRDIN returns error `3000-CALL-SUB` | UT_CARDINQ_ABNORMAL_008 |
| 8 | `Invalid key pressed. Please try again.` | Error | unsupported AID key `2000-PROCESS-INPUT WHEN OTHER` | UT_CARDINQ_ABNORMAL_001 |
| 9 | `nn shown - PF8=more PF7=top` | Info (count) | page filled, more remain `4900-BUILD-MSG` | IT_CARDINQ_HAPPY_010, UT_CARDINQ_BOUNDARY_002 |
| 10 | `nn shown - end of selection` | Info (count) | last page `4900-BUILD-MSG` | IT_CARDINQ_HAPPY_010, UT_CARDINQ_BOUNDARY_001 |

> This CICS app raises **literal English message strings, not EI/EF/GF codes**. `msg_codes.py` finds 0 codes (confirmed). The audit's "abnormal case cites no message code" and "could not run msg_codes" candidates are therefore expected and dismissed — the verbatim string in 期待結果 *is* the assertion (see Audit result below).

## 観点表 (viewpoint table) — every base UI viewpoint decided (assumed approved)

中項目 uses the canonical `<JP> (<EN>)` labels. Evidence cited from the UI doc (§) or the AST/source.

| # | 観点 (中項目) | Applicable? | Evidence | Planned TC |
|---|---|---|---|---|
| U1 | 画面表示・レイアウト (Screen display / layout) | ✅ | §2.1 mockup MCRDINA + item list (header, Filter, legend, 13-row list, totals, key legend) | IT_..HAPPY_001, _002 |
| U2 | 初期値・デフォルト (Initial values / defaults) | ✅ | `1000-SEND-INITIAL`: filter=ALL, FDESC='ALL', page 0, counts 0, prompt, cursor at Filter (`MOVE -1 TO FILTL`) | IT_..HAPPY_003 |
| U3 | 表示条件・活性制御 (Display conditions / enable control) | ✅ | §2.1 item-states: Filter `○` (enterable, initial focus) / all list & total fields `□` display-only | IT_..HAPPY_004 |
| U4 | 文字・書式表示 (Characters / format display) | ✅ | RCARD/KCRDIN PIC: Card№ X(16), Acct 9(11), Name X(20), Expiry X(10) `YYYY-MM-DD`, Status X(01) | UT_..BOUNDARY_007 |
| U5 | 入力チェック (Input validation) | ✅ (1 field) | only input = Filter; parse `6000-PARSE-FILTER` (blank→ALL; leading-3; else invalid) | UT_..ABNORMAL_002, BOUNDARY_005/006 |
| U6 | 操作性・キー (Operability / function keys) | ✅ (AID keys) | `2000-PROCESS-INPUT` EVALUATE EIBAID: ENTER/PF7/PF8/PF3/PF12/PF4/CLEAR/other | IT_..HAPPY_013 (PF4), _014 (CLEAR); ENTER→HAPPY_005-008; others below |
| U7 | 状態遷移 (Screen / state transitions) | ✅ | pseudo-conv. paging state in CA-WORK-AREA: ENTER→page1, PF8→page+1, PF7→top | IT_..HAPPY_011 |
| U8 | 機能・業務フロー (Function / business flow) | ✅ (per classifier) | 4 filter values (ALL/ACTIVE/INACTIVE/Expiring-Soon) — OUCRDIN 4510–4540 | IT_..HAPPY_005/006/007/008 |
| U9 | 計算・編集ロジック (Calculation / editing logic) | ✅ | KPI whole-file Active/Inactive tally `6000-COMPUTE-KPI`; result-count line `4900-BUILD-MSG`; expiring-window arithmetic | IT_..HAPPY_009, _010; UT_..BOUNDARY_003 |
| U10 | 出力・帳票・メール (Output / report / mail) | ❌ N/A | inquiry only; no report file / print / mail — the on-screen list is the output, covered under U1/U8 | — |
| U11 | データ整合性・冪等性 (Data integrity / idempotency) | ✅ (idempotency only) | read-only (§5 no C/U/D); re-list/restart returns the same set (browse GTEQ from top) | IT_..HAPPY_012 |
| U12 | 排他・同時実行 (Exclusion / concurrent) | ❌ N/A | read-only browse (STARTBR/READNEXT, no lock/update); no update contention | — |
| U13 | メッセージ・異常系 (Messages / abnormal) | ✅ | 8 error/warn triggers (messages #2–8 above) | UT_..ABNORMAL_001–008 |
| U14 | 境界値 (Boundary values) | ✅ | page size 13 (`WS-MAX-ROWS`); expiring window today/+60; filter leading-3; blank; field widths | UT_..BOUNDARY_001–007 |
| U15 | 回復・リラン (Recovery / rerun) | ⚠️ covered elsewhere | pseudo-conv. re-entry & error recovery — screen stays on LINK/browse error, operator retries; no partial commit to recover (read-only) | folded into HAPPY_012, ABNORMAL_005/007/008 |
| U16 | 権限・セキュリティ (Authority / security) | ❌ excluded (reason) | OCCRDIN has **no signon/authority check of its own** (no login field, no dept/mode gate in source); "authenticated operator" is enforced upstream at Sign On (OCSGNON) before the menu | — |
| U17 | ログ・監査 (Log / audit) | ❌ excluded (reason) | no audit/log record written; OUCRDIN emits `DISPLAY` **console debug traces** only (not a business audit record) | — |
| U18 | 連携・インターフェース (Linkage / interface) | ✅ | `EXEC CICS LINK PROGRAM('OUCRDIN')` with KCRDIN-AREA; `XCTL PROGRAM('OCMENU')` on PF3/PF12 | IT_..HAPPY_015 (LINK), _016 (PF3), _017 (PF12) |
| U19 | 性能・運用 (Performance / operation) | ❌ deferred (runtime) | KPI does a whole-file scan on page 1 — throughput is a runtime/volume concern; no paper/printer/env instruction on this screen | — |

**CICS-specific instantiation applied** (per viewpoints.md "CICS/BMS UI app"): 操作性・キー = **AID keys** (ENTER / PF3 / PF4 / PF7 / PF8 / PF12 / CLEAR) from the `EIBAID` EVALUATE, not ESTS; 連携/状態遷移 = **CICS transaction navigation** (`LINK` OUCRDIN, `XCTL` OCMENU, `RETURN TRANSID('ORCI')`); 画面表示 grounded in the real `MCRDINA` DFHMDF field positions.

## Coverage matrix (viewpoint → evidence rows → TC)

- **Classifier values (4/4):** ALL→HAPPY_005 · ACTIVE→HAPPY_006 · INACTIVE→HAPPY_007 · Expiring-Soon→HAPPY_008 · blank→ALL default→BOUNDARY_006.
- **AID keys (8/8):** ENTER→HAPPY_005-008 · PF7→HAPPY_011/012 & ABNORMAL_005 · PF8→HAPPY_010/011, BOUNDARY_002, ABNORMAL_004/006 · PF3→HAPPY_016 · PF12→HAPPY_017 · PF4→HAPPY_013 · CLEAR→HAPPY_014 · unsupported→ABNORMAL_001.
- **Messages (10/10):** see the message table above — every string has a dedicated (abnormal) or covering (info/count) TC.
- **Boundaries (both sides):** page 13 exact / 14 spill → BOUNDARY_001/002 · expiring today & +60 included / +61 & past excluded → BOUNDARY_003 · undated expiry excluded → BOUNDARY_004 · filter leading-3 & `AL` short → BOUNDARY_005 · blank→ALL → BOUNDARY_006 · field display widths → BOUNDARY_007.
- **Interface (2/2 hand-offs):** LINK OUCRDIN → HAPPY_015 · XCTL OCMENU (PF3/PF12) → HAPPY_016/017.
- **Excluded viewpoints (with reason):** U10, U12, U16, U17, U19 — reasons in the 観点表. U15 folded into HAPPY_012 / ABNORMAL_005/007/008.

**Count:** 32 TC — 17 Normal (IT), 7 Boundary (UT), 8 Abnormal (UT).

## Fixtures

**F-STD (standard fixture)** — the real `CARDFILE` seed shipped with the source (`data/CARDFILE.txt`, VSAM KSDS keyed on 16-digit card number). 5 cards, 4 active + 1 inactive:

| Card № | Acct ID | Embossed Name | Expiry | Status |
|---|---|---|---|---|
| 4000000000000001 | 00000000001 | JOHN Q SMITH | 2027-03-31 | Y (active) |
| 4000000000000002 | 00000000002 | MARIA A GARCIA | 2028-06-30 | Y (active) |
| 4000000000000003 | 00000000003 | ROBERT B JOHNSON | 2026-01-31 | N (inactive) |
| 4000000000000004 | 00000000004 | LINDA C WILLIAMS | 2029-11-30 | Y (active) |
| 4000000000000005 | 00000000005 | DAVID D BROWN | 2030-05-31 | Y (active) |

With F-STD: `ALL`→5 rows (Active 4 / Inactive 1) · `ACTIVE`→4 rows · `INACTIVE`→1 row (card …0003) · `Expiring-Soon` (window 2026-09-28 … 2026-11-27)→**0 rows** (all expiries are far-future or already past card …0003 @2026-01-31).

**F-PAGE (paging delta)** — F-STD extended so a filter fills more than one 13-row page: card numbers `4000000000000001`…`4000000000000020` (20 cards, all Status `Y`), constructed to the RCARD layout (Card№ X(16), Acct 9(11), Name X(20), Expiry X(10) far-future, Status X(01)). Needed by HAPPY_010/017 and BOUNDARY_001/002.

**F-EXP (expiring-window delta)** — F-STD plus cards whose expiry is set relative to today (2026-09-28): one @`2026-09-28` (today, day 0), one @`2026-11-27` (today+60), one @`2026-11-28` (today+61), one @`2026-09-27` (yesterday, past), one with a blank/`----------` expiry. Needed by BOUNDARY_003/004 and HAPPY_008.

Card numbers/names in F-STD are the customer's real seed values (reproduced unchanged). F-PAGE/F-EXP extra rows are constructed from the copybook PIC formats (no business fact invented beyond key/expiry/status needed to hit the branch).

## Audit result

`audit_testcase.py cases.json --reg <REG> --root OCCRDIN` — candidates reviewed:

- **`[valid] could not run msg_codes`** — *dismissed*: the audit's `_root_xml` looks under `parsed/cobol_xml/main|sub/`, but this reg stores XML under `ORION-CCMS/cbl/`. Ran `msg_codes.py` directly on `OCCRDIN.xml` and `OUCRDIN.xml` → **0 codes** (this app uses literal English messages, not EI/EF/GF codes). No code-coverage gap possible.
- **`[vague] … 異常系 case but expected cites no message code / STOP`** (×8, one per abnormal TC) — *dismissed as a class*: the app has no EI/EF codes; each abnormal 期待結果 quotes the **verbatim on-screen message string** (e.g. `Filter not recognised - see the list above.`), which is the real assertion. Every abnormal expected carries a literal quoted string.
- All other checks (unique ids, no empty required fields, every case has a canonical 中項目, no folding of multiple codes, no code-leak/wording, automation columns `×`/empty) — **clean**.

See the final printed summary for the exact candidate list and the resolution of each.
</content>
</invoke>
