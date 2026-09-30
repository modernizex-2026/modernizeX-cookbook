# Test-case run notes — OCACTIN (Account Inquiry, consolidated)

- **App / kind:** `OCACTIN` — CICS/BMS **UI (screen) app** (transaction `ORAI`, mapset `MACTIN`, map `MACTINA`). Kind decided by evidence (a UI ScreenDesign exists; the reg has the BMS map) — `design_evidence.py` reports `kind: ui`.
- **REG (ground truth):** `/Users/thangnguyen/Library/Caches/.skr/run-19580/reg`
- **Design evidence read (read-only):**
  - `UI/OCACTIN_AccountInquiry_ScreenDesign.md` (screen spec — the UI test basis)
  - `Batch/OUACTIN_AccountInquiryBrowse_ProgramDesign.md` (the browse engine OCACTIN links to — where the filter/derivation business logic lives)
  - `datastore/design_runs/design_asis/NOTES.md` (routing / naming / message policy)
  - AST verified directly: `parsed/cobol_xml/ORION-CCMS/cbl/OCACTIN.xml`, `…/OUACTIN.xml`; copybooks `RACCT`, `KACTIN`, `KCOMM`, `WMSG`, `WHEAD`, `MACTIN`.
- **Deliverable language:** English (fixed by skill edition). On-screen literals reproduced verbatim in `「…」`; message strings reproduced verbatim.
- **Author:** modernizeX (fixed).

## Scope decision (what OCACTIN actually runs)

`apps.json` app_004 lists a 26-member call-reachability cluster (the whole menu subtree reached via XCTL). By the evidence rule the **runtime path of the OCACTIN inquiry** is only:

- `OCACTIN` (transaction `ORAI`, screen handler for map `MACTINA`) → `EXEC CICS LINK PROGRAM('OUACTIN')` (the account-browse engine) → renders one page → `RETURN TRANSID('ORAI')` (pseudo-conversational). `PF3`/`PF12` → `XCTL PROGRAM('OCMENU')`.

The other cluster siblings (`OUANLIN`, `OUSTMIN`, `OUDATE`, and the `OC*` screen members) are **not invoked by OCACTIN** — `OCACTIN.xml` links only `OUACTIN` (`WS-SUB-PGM = 'OUACTIN'`) and `OUACTIN.xml` calls no sub-program. They are covered by their own apps' test sets (`OCANLIN`, `OCSTMIN`, …). **Out of scope here.**

## App facts grounded from source (used to build the cases)

**Screen `MACTINA` (24×80).** One input field `FILT` (`FILTI`, 10 cols, initial focus); everything else is program-supplied display: header (`Tran:`=ORAI, `Pgm:`=OCACTIN, title 「ORION CREDIT CARD MANAGEMENT SYSTEM」, `Date:`/`Time:`), the filter-type legend captions 「ALL DELINQUENT OVER-LIMIT DORMANT」 / 「CLOSED NEW HIGH-UTIL」, a 13-row result grid (`Acct ID`, `St`, `Balance`, `Credit Lim`, `Available`, `Util%`), the totals line (`Page:`, `Matched:`, `PortBal:`, `PortAvl:`), the red `ERRMSG` message line, and the key legend 「ENTER=Apply  PF7=Top  PF8=Fwd  PF3=Menu」.

**AID keys handled (`2000-PROCESS-INPUT`, EVALUATE EIBAID):** `ENTER`→apply filter · `PF7`→restart at top · `PF8`→page forward · `PF3`→menu (XCTL OCMENU) · `PF12`→menu · `PF4`→reset to initial · `CLEAR`→reset to initial · any other key→invalid-key message. (The §1.2 key table lists only ENTER/PF7/PF8/PF3; PF12/PF4/CLEAR are real in the handler and §4 event spec — covered.)

**Filter parse (`6000-PARSE-FILTER`):** input upper-cased, first 3 chars matched: `SPACES`→ALL, `ALL`→A, `DEL`→D, `OVE`→O, `DOR`→M, `CLO`→C, `NEW`→N, `HIG`→H; anything else → `WS-INVALID`.

**Filter business rules (`OUACTIN`, per §1.6 + verified conditions):**
- available = credit limit − balance; utilisation = balance ÷ limit × 100, **capped at 999.99** (`WS-UTIL-BIG > 999.99`).
- min due = 2% of balance, **floor 25.00** (applied when balance > 0).
- ALL = every account · DELINQUENT = `bal>0 AND cyc-credit < min-due` · OVER-LIMIT = `bal > limit` · DORMANT = `cyc-credit=0 AND cyc-debit=0` · CLOSED = `status ≠ 'Y'` · NEW = valid numeric open date AND `open-int >= (today-int − 90)` · HIGH-UTIL = `util >= 80.00`.
- One page = up to **13** rows (`KAI-ROW-COUNT`, 13 grid slots). `WS-ST-MORE='Y'` when more remain. Portfolio totals (Matched / PortBal / PortAvl) are computed **once** at filter-apply (`KAI-DO-KPI`) over the whole matched set and **retained across page-forward** (`KAI-SKIP-KPI`).

**Messages (all verbatim, active in source).** No EI/EF/GF code scheme exists in ORION-CCMS (design NOTES §Assumptions 5); messages are literal strings, so IDs below reference the message by its trigger, not a code:

| Trigger point | Verbatim message | Where set |
|---|---|---|
| open / initial prompt | `Type a filter and press ENTER.` | `WS-M-PROMPT` (1000) |
| unrecognised filter | `Filter not recognised - see the list above.` | `WS-M-BAD-FILTER` (2100) |
| filter matches nothing | `No accounts match that filter.` | `WS-M-NONE-FOUND` (4000) |
| PF8 past last page | `End of selection - no more accounts.` | `WS-M-END-FILE` (2300) |
| PF7/PF8 before any list | `Apply a filter first (press ENTER).` | `WS-M-LIST-FIRST` (2200/2300) |
| LINK response not normal | `Unable to reach the account browse engine.` | `WS-M-LINK-ERR` (3000) |
| browse engine error (KAI-ERROR) | `Error browsing the account file.` | `WS-M-BROWSE-ERR` (3000) |
| unsupported key | `Invalid key pressed. Please try again.` | `WS-MSG-INVALID-KEY` (WMSG) |
| page shown, more remain | `<n> shown - PF8=more PF7=top` | `WS-M-MORE` (4900) |
| page shown, last page | `<n> shown - end of selection` | `WS-M-DONE` (4900) |

## Viewpoint table (観点表) — every base UI viewpoint decided

Approval gate: **non-interactive run — the table below is recorded as an assumption and taken as approved** (see Assumptions). 中項目 uses the canonical labels.

| # | 観点 (中項目) | Applicable? | Evidence | Planned TC |
|---|---|---|---|---|
| U1 | 画面表示・レイアウト (Screen display / layout) | ✅ | UI §2 map `MACTINA` + item details (108 fields) | IT_…HAPPY_001–002 |
| U2 | 初期値・デフォルト (Initial values / defaults) | ✅ | `1000-SEND-INITIAL` (FDESC=ALL, prompt, cursor, zero totals); blank filter → ALL | IT_…HAPPY_003–004 |
| U3 | 表示条件・活性制御 (Display conditions / enable control) | ✅ | item-states (FILT ○ on prompt, □ on result); `4100-CLEAR-ROWS` blanks unused slots | IT_…HAPPY_005–006 |
| U4 | 文字・書式表示 (Characters / format display) | ✅ | edit masks `-,---,---,---,--9.99` / `ZZ9.99` / `ZZ9` / `Z(6)9` (`WS-EDIT-FIELDS`) | IT_…HAPPY_007 (+ boundary cap) |
| U5 | 入力チェック (Input validation) | ✅ | `6000-PARSE-FILTER` (case-insensitive, 3-char prefix, invalid → message) | IT_…HAPPY_008–009, UT_…ABNORMAL_001 |
| U6 | 操作性・キー (Operability / function keys) | ✅ | EVALUATE EIBAID — 8 AID branches | IT_…HAPPY_010–015, UT_…ABNORMAL_002 |
| U7 | 状態遷移 (Screen / state transitions) | ✅ | EIBCALEN=0 cold start; CA-FIRST-ENTER; `RETURN TRANSID('ORAI')` pseudo-conv | IT_…HAPPY_016–018 |
| U8 | 機能・業務フロー (Function / business flow) | ✅ | one happy flow per filter classifier (ALL/DELINQUENT/OVER-LIMIT/DORMANT/CLOSED/NEW/HIGH-UTIL) | IT_…HAPPY_019–025 |
| U9 | 計算・編集ロジック (Calculation / editing logic) | ✅ | available, utilisation, portfolio totals, min-due 2%/floor-25 (`OUACTIN` §1.6) | IT_…HAPPY_026–029 |
| U10 | 出力・帳票・メール (Output / report / mail) | ❌ N/A | read-only inquiry; §5 DB CRUD "Not applicable"; no report/file write/mail. Screen content variants covered under 機能・業務フロー | — |
| U11 | データ整合性・冪等性 (Data integrity / idempotency) | ✅ | KPI computed once (KAI-DO-KPI), retained on PF8 (KAI-SKIP-KPI); read-only (no C/U/D) | IT_…HAPPY_030–031 |
| U12 | 排他・同時実行 (Exclusion / concurrent access) | ❌ N/A | read-only browse (STARTBR/READNEXT/ENDBR); no lock, no update; concurrency runtime-only | — |
| U13 | メッセージ・異常系 (Messages / abnormal) | ✅ | 8 distinct message trigger points (table above) | UT_…ABNORMAL_002–008 (+001 bad-filter under U5) |
| U14 | 境界値 (Boundary values) | ✅ | page 13/14, prefix 3/2 chars, util cap 999.99, NEW cutoff, HIGH 80.00, OVER, DELINQUENT thresholds | UT_…BOUNDARY_001–008 |
| U15 | 回復・リラン (Recovery / rerun) | ❌ N/A | read-only, no partial commit to recover; PF7=Top restart covered under 操作性・キー | — |
| U16 | 権限・セキュリティ (Authority / security) | ❌ N/A | OCACTIN performs no signon/dept/mode check; roles (`CA-USER-ADMIN/NORMAL`) ride in COMMAREA but are **not** referenced here; access is via the authenticated Main Menu (§1.2 role = authenticated operator). Cold-start behaviour covered under 状態遷移 | — |
| U17 | ログ・監査 (Log / audit) | ❌ N/A | no audit/log record written (read-only inquiry) | — |
| U18 | 連携・インターフェース (Linkage / interface) | ✅ | `LINK OUACTIN` request/response COMMAREA (KACTIN); `XCTL OCMENU` COMMAREA hand-off | IT_…HAPPY_032–033 |
| U19 | 性能・運用 (Performance / operation) | ❌ N/A | no perf/paper-setup requirement; result paging covered under 操作性・キー; perf runtime-only | — |

App-specific viewpoints revealed by the spec are folded into the base labels: **filter-parse semantics** (case-insensitive, 3-char prefix) → 入力チェック; **portfolio-KPI-vs-page** (totals span the whole matched set, list shows one page) → 計算・編集ロジック + データ整合性・冪等性.

## Standard fixture

**F-STD** — the `ACCTFILE` (Account master, VSAM KSDS, copybook `RACCT`) seeded with 7 accounts, each engineered to hit one filter (values constructed from `RACCT` PIC — legal and constructible; reconcile exact figures with the customer's real ACCTFILE if needed). Today = 2026-09-28, so the NEW cutoff ≈ open-date on/after 2026-06-30.

| Acct ID | St | Balance | Credit Lim | CycCredit (paid) | CycDebit | Open date | Available | Util% | Hits filter |
|---|---|---|---|---|---|---|---|---|---|
| 10000000001 | Y | 500.00 | 5,000.00 | 100.00 | 100.00 | 2018-03-10 | 4,500.00 | 10.00 | ALL only (healthy) |
| 10000000002 | Y | 2,000.00 | 10,000.00 | 5.00 | 400.00 | 2016-01-01 | 8,000.00 | 20.00 | DELINQUENT |
| 10000000003 | Y | 6,000.00 | 5,000.00 | 500.00 | 200.00 | 2017-05-20 | -1,000.00 | 120.00 | OVER-LIMIT (+HIGH-UTIL) |
| 10000000004 | Y | 0.00 | 3,000.00 | 0.00 | 0.00 | 2014-11-11 | 3,000.00 | 0.00 | DORMANT |
| 10000000005 | N | 150.00 | 2,000.00 | 150.00 | 0.00 | 2013-02-02 | 1,850.00 | 7.50 | CLOSED |
| 10000000006 | Y | 300.00 | 4,000.00 | 300.00 | 300.00 | 2026-09-01 | 3,700.00 | 7.50 | NEW |
| 10000000007 | Y | 4,500.00 | 5,000.00 | 500.00 | 200.00 | 2015-08-08 | 500.00 | 90.00 | HIGH-UTIL |

Matched counts under F-STD: ALL 7 · DELINQUENT 1 · OVER-LIMIT 1 · DORMANT 1 · CLOSED 1 · NEW 1 · HIGH-UTIL 2 (accts 3 & 7). Portfolio under ALL: Matched **7**, PortBal **13,450.00**, PortAvl **20,550.00**.

Fixture deltas (named in the cases that use them): **F-PAGE13** = 13 active healthy accounts (all ALL) → one full page; **F-PAGE14** = 14 → page 1 (13) + page 2 (1); **F-NODLQ** = only healthy accounts (no delinquent) for the none-found case; per-boundary single-account deltas are described inline in each boundary TC.

## Coverage matrix (viewpoint → planned TC → status)

- U1 →001,002 ✓ · U2 →003,004 ✓ · U3 →005,006 ✓ · U4 →007 ✓ · U5 →008,009,ABN_001 ✓ · U6 →010–015,ABN_002 ✓ · U7 →016,017,018 ✓ · U8 →019–025 (7 classifiers) ✓ · U9 →026–029 ✓ · U11 →030,031 ✓ · U13 →ABN_002–008 ✓ · U14 →BND_001–008 ✓ · U18 →032,033 ✓.
- **Messages:** 10 message strings / 8 distinct trigger points — all covered (bad-filter under U5; invalid-key, none-found, end-of-selection, apply-first×2 triggers, link-err, browse-err under U13; the two "<n> shown …" info strings asserted inside the paging TCs 014/BND_002).
- **Classifiers:** 7/7 filters each have a dedicated happy TC.
- **Excluded viewpoints (6):** U10, U12, U15, U16, U17, U19 — each with a spec-grounded reason row above.
- Nothing left uncovered without a reason.

## Totals

**49 test cases** — Normal 33 · Abnormal 8 · Boundary 8.

## Audit result

`audit_testcase.py <cases.json> --reg <REG> --root OCACTIN` → run; **10 candidates, every one grounded** (all false positives for a no-message-code CICS system — none required a case change). Structural checks clean: ids unique, all required fields present, all 13 `mid` are canonical viewpoints, no folding, automation columns consistent.

1. **[valid] "could not run msg_codes … skip code coverage"** — dismissed. The audit's `_root_xml` looks under `parsed/cobol_xml/{main,sub}/`, but this reg nests the AST under `parsed/cobol_xml/ORION-CCMS/cbl/`; and ORION-CCMS uses **no EI/EF/GF code scheme** (design NOTES §Assumptions 5 — messages are literal strings). Verified directly: `msg_codes.py OCACTIN.xml` and `OUACTIN.xml` both return `{"codes":{}}`. Code coverage is therefore N/A; message coverage is driven by the verbatim-string trigger table above (all 8 distinct trigger points covered).
2. **[vague] ×8 — "異常系 case but expected cites no message code / STOP"** (ABNORMAL_001–008) — dismissed. The check wants an `EInnn`-style code or STOP/ABEND in the expected; ORION-CCMS has no code scheme, so each abnormal case's expected instead quotes the **verbatim on-screen message string** in 「…」 (the actual assertion, e.g. 「Filter not recognised - see the list above.」, 「Invalid key pressed. Please try again.」). Per the skill, message text is the assertion when no code exists. Grounded — no change.
3. **[wording] HAPPY_002 — "data-name e.g. 'OVER-LIMIT'"** — dismissed. `OVER-LIMIT` / `HIGH-UTIL` here are **verbatim on-screen legend captions** reproduced inside 「…」 (the reproduce-unchanged rule), not COBOL data-names; asserting the legend renders is exactly the job of the 画面表示・レイアウト viewpoint. Grounded — kept verbatim.

**Spot-check (DoD):** HAPPY_020→OUACTIN L287 (`AC-CURR-BAL>0 AND AC-CYC-CREDIT<WS-MIN-DUE`) ✓ · ABNORMAL_004→OCACTIN L261-264 (`WS-ST-MORE NOT='Y'`→「End of selection - no more accounts.」) ✓ · BOUNDARY_004→OUACTIN L242-243 (`WS-UTIL-BIG>999.99`→999.99) ✓.

## Assumptions (non-interactive run — recorded, taken as approved)

1. Viewpoint table + coverage matrix above are taken as signed off (CLI/CI invocation, no interactive gate).
2. Scope = OCACTIN handler + the linked OUACTIN browse engine only; other app_004 cluster siblings are out of scope (each has its own app test set).
3. F-STD account values are constructed from the `RACCT` copybook PIC (legal, constructible); reconcile exact figures against the customer's real ACCTFILE if precise sample data is required.
4. Automation: AS-IS CICS screen app with no UI automation harness → `自動 = ×`, `自動化ID` empty for all cases.
5. Message text is reproduced verbatim from source; no message code is cited because ORION-CCMS uses none.
