# OCTRNIN — Test-case derivation notes

**App:** OCTRNIN — Transaction Inquiry (on-line consolidated transaction look-up + analytics)
**Kind:** UI (CICS/BMS) — decided by EVIDENCE (a UI ScreenDesign doc exists; mapset `MTRNIN` / map `MTRNINA`). CICS transaction **ORQT**.
**AREA word:** `TRANINQ` (unique across this run — OCCRDIN=CARDINQ, OCCUSIN=CUSTINQ, OCPAUIN=PAUTHINQ).
**Deliverable language:** English (skill edition default). Japanese business terms carry an English gloss at first use; verbatim on-screen strings are reproduced unchanged.
**Author:** modernizeX (fixed).

## Sources read (ground truth)

- UI design: `design_asis/UI/OCTRNIN_TransactionInquiry_ScreenDesign.md` (§2 map layout, §3 checks, §4 events).
- Sub-program design: `design_asis/Batch/OUTRNIN_TransactionInquiryBrowse_ProgramDesign.md` (browse + analytics engine).
- AST (active code only): `reg/parsed/cobol_xml/ORION-CCMS/cbl/OCTRNIN.xml`, `OUTRNIN.xml`.
- Raw source (for reading, all messages re-confirmed active on the AST): `input/10/ORION/ORION-CCMS/cbl/OCTRNIN.cbl`, `OUTRNIN.cbl`.
- Copybooks: `RTRAN` (transaction record PICs), `KTRNIN` (LINK commarea + analytics result), `KCOMM` (shared COMMAREA), `WMSG` (`WS-MSG-INVALID-KEY` = "Invalid key pressed. Please try again."), `WHEAD` (title "ORION CREDIT CARD MANAGEMENT SYSTEM").
- Seed data: `input/10/ORION/ORION-CCMS/data/TTYPFILE.txt` (type master 01–07), `TCATFILE.txt` (type+category master). **No `TRANFILE.txt` seed exists** → the transaction fixture (F-STD) is constructed on the `RTRAN` PICs and marked *(needs real data)* for exact production values.

## Approval gate (non-interactive assumption)

This run is non-interactive. Per the skill, the 観点表 below is **recorded as an approved assumption** and case authoring proceeded without a live confirmation round. If a reviewer disagrees with a viewpoint decision, adjust the table row and regenerate the affected cases from `cases.json`.

## Message convention (per ORION-CCMS)

`msg_codes.py` returns **0 codes** for OCTRNIN.xml and OUTRNIN.xml — this screen raises **literal English strings** to the on-screen message line (`ERRMSGO`), not EI/EF/GF codes. The full message set was grounded from the source and re-confirmed active on the AST:

| # | Working-storage name | Verbatim string | Raised at |
|---|---|---|---|
| 1 | `WS-M-PROMPT` (info) | Choose filter C/D/M/T/A, key args, press ENTER. | `1000-SEND-INITIAL` (initial screen) |
| 2 | `WS-M-BAD-MODE` | Mode must be C D M T or A. | `5000-PARSE-FILTER` WHEN OTHER |
| 3 | `WS-M-CARD-REQ` | Card number is required for the card filter. | `5100-PARSE-CARD` (blank card) |
| 4 | `WS-M-MERCH-REQ` | Merchant id is required and must be numeric. | `5300-PARSE-MERCH` (blank **and** non-numeric — two triggers) |
| 5 | `WS-M-TYPE-REQ` | Type is required for the type/category filter. | `5400-PARSE-TYPECAT` (blank type) |
| 6 | `WS-M-BAD-NUM` | A numeric filter argument is not valid. | `5400-PARSE-TYPECAT` (non-numeric category) |
| 7 | `WS-M-BAD-AMT` | Amount threshold is not a valid number. | `5500-PARSE-AMOUNT` (invalid amount) |
| 8 | `WS-M-NONE-FOUND` | No transactions match the filter. | `4000-SHOW-PAGE` (row count 0) |
| 9 | `WS-M-END-LIST` | End of list - no more transactions. | `2200-PAGE-NEXT` (PF8 past last page) |
| 10 | `WS-M-LINK-ERR` | Unable to link to analytics engine OUTRNIN. | `3000-CALL-SUB` LINK failure → shown by `4000` |
| 11 | `WS-MSG-INVALID-KEY` (WMSG) | Invalid key pressed. Please try again. | `2000-PROCESS-INPUT` WHEN OTHER (unsupported AID) |
| 12 | paging line (`4600-BUILD-MSG`) | `<n> matching tran(s).  PF8=more PF7=top` / `<n> matching tran(s).  End of list.` | `4000-SHOW-PAGE` (rows shown) |

## Filter modes (classifier `TMODE` / `KTI-FILTER`) — one happy TC each

| Mode | Meaning | Parse para | Match rule (`2400-FILTER-CHECK`) |
|---|---|---|---|
| `C` | Card number (default) | `5100` | `TR-CARD-NUM = KTI-CARD` (exact) |
| `D` | Processed-date range | `5200` | `TR-PROC-TS(1:10)` in `[from, to]` inclusive; blank from→`0000-00-00`, blank to→`9999-99-99` |
| `M` | Merchant id (numeric) | `5300` | `TR-MERCHANT-ID = KTI-MERCH-ID` |
| `T` | Type + category | `5400` | `TR-TYPE-CD = KTI-F-TYPE` AND `TR-CAT-CD = KTI-F-CAT` (blank cat→0) |
| `A` | Amount threshold | `5500` | `TR-AMT >= KTI-AMT-THRESH`; blank→0 |

## Analytics (`OUTRNIN 2700-ACCUMULATE`, filter-wide over the whole file)

- Match count + net total (every match); Top transaction = max `TR-AMT` + its id.
- Per-class subtotals keyed on `TR-TYPE-CD`: `PU`→Purchases, `PY`→Payments, `FE`→Fees, `IN`→Interest, any other type → net total only.
- Row description (`2600-LOOKUP-DESC`): read `TCATFILE` by type+cat → `TC-DESC`; else fall back to `TTYPFILE` by type → `TT-DESC`; else `UNKNOWN`.

## Per-app 観点表 (viewpoint table) — EVERY base viewpoint decided

| 観点 (中項目) | Applicable? | Evidence (spec/AST) | Planned TC |
|---|---|---|---|
| 画面表示・レイアウト (Screen display / layout) | ✅ | UI §2.1 map MTRNINA (captions, filter fields, 6-row grid, analytics block, key legend) | IT_TRANINQ_HAPPY_001 |
| 初期値・デフォルト (Initial values / defaults) | ✅ | `1000-SEND-INITIAL`: mode pre-set `C`, prompt shown, cursor on Mode (`MOVE -1 TO TMODEL`) | IT_TRANINQ_HAPPY_002 |
| 表示条件・活性制御 (Display conditions / enable control) | ❌ — every filter field is always enterable and every result field always display-only; no conditional show/hide (UI §2 item-states are uniform ○/□) | UI §2.1 item states | — |
| 文字・書式表示 (Characters / format display) | ✅ | card shown last 4 digits (`KTR-CARD (13:4)`), amount edit mask (`WS-ED-AMT`/`WS-ED-SUM`) | IT_TRANINQ_HAPPY_023 |
| 入力チェック (Input validation) | ✅ | `5000`–`5500` parsers; blank/format checks per mode | IT_TRANINQ_HAPPY_002 basis; UT_TRANINQ_ABNORMAL_002–007 |
| 操作性・キー (Operability / function keys) | ✅ | `2000-PROCESS-INPUT` EIBAID: ENTER / PF7 / PF8 / PF3 + other | IT_TRANINQ_HAPPY_011,012,013; UT_TRANINQ_ABNORMAL_011 |
| 状態遷移 (Screen / state transitions) | ✅ | initial→result (stay); result→PF8/PF7 (stay); →PF3 OCMENU (`7000-XCTL-MENU`) | IT_TRANINQ_HAPPY_011,012,013 |
| 機能・業務フロー (Function / business flow) | ✅ | 5 filter modes + open-range/blank-arg variants | IT_TRANINQ_HAPPY_003–010 |
| 計算・編集ロジック (Calculation / editing logic) | ✅ | net total, 4 subtotals, top tran (`2700-ACCUMULATE`) | IT_TRANINQ_HAPPY_014–019, 026 |
| 出力・帳票 (Output / report) | ✅ | displayed page rows + type/category description lookup + fallbacks (`2500`/`2600`/`2650`) | IT_TRANINQ_HAPPY_020,021,022 |
| データ整合性・冪等性 (Data integrity / idempotency) | ✅ | whole-file re-scan each call → analytics identical across pages; read-only (no C/U/D) | IT_TRANINQ_HAPPY_024 |
| 排他・同時実行 (Exclusion / concurrent access) | ❌ — read-only browse (`STARTBR`/`READNEXT`, no update verb; UI §5 "only reads data"); no lock taken | OUTRNIN §1.3 I-only, §5 | — |
| メッセージ・異常系 (Messages / abnormal) | ✅ | 11 literal messages (table above) | UT_TRANINQ_ABNORMAL_001–011 |
| 境界値 (Boundary values) | ✅ | merchant 9(09), category 9(04), amount S9(09)V99, card X(16), date range edges, page size 6 (`WS-MAX-ROWS`) | UT_TRANINQ_BOUNDARY_001–011 |
| 回復・リラン (Recovery / rerun) | ⚠ partial → PF7 restart-to-top is a re-entry of the same filter (`2300-RESTART`), covered as a transition; true abort/restart is CICS-runtime | `2300-RESTART` | IT_TRANINQ_HAPPY_012 |
| 権限・セキュリティ (Authority / security) | ❌ — no in-screen role gate; `CA-USER-TYPE` is not checked in OCTRNIN. Sign-on is enforced upstream (OCSGNON); PF3 returns to OCMENU | KCOMM `CA-USER-TYPE` unused in OCTRNIN.xml | — |
| ログ・監査 (Log / audit) | ❌ — no audit/log record written (no WRITE verb; inquiry only) | OCTRNIN.xml / OUTRNIN.xml no WRITE | — |
| 連携・インターフェース (Linkage / interface) | ✅ | `EXEC CICS LINK PROGRAM('OUTRNIN') COMMAREA(KTRNIN-AREA)` — request→page+analytics contract; PF3 XCTL OCMENU | IT_TRANINQ_HAPPY_025 |
| 運用 (Operation) | ❌ — no operational caption (paper/printer/environment), pure on-line inquiry | UI §2 captions | — |
| 性能・運用 (Performance) | ❌ — whole-file scan per call is a volume concern only decidable at runtime | OUTRNIN design note | deferred |

**App-specific viewpoints added:** none beyond the catalog (the 5-mode filter, paging cursor and 4-class analytics all map onto 機能・業務フロー / 操作性 / 計算・編集ロジック / 境界値).

## Coverage matrix

**Message coverage (11 distinct triggers):** BAD-MODE→ABN_001 · CARD-REQ→ABN_002 · MERCH-REQ blank→ABN_003 · MERCH-REQ non-numeric→ABN_004 · TYPE-REQ→ABN_005 · BAD-NUM→ABN_006 · BAD-AMT→ABN_007 · NONE-FOUND→ABN_008 · END-LIST→ABN_009 · LINK-ERR→ABN_010 · INVALID-KEY→ABN_011. PROMPT (info)→HAPPY_002; paging line (info)→HAPPY_011 / BOUNDARY_010. **0 message triggers uncovered.**

**Classifier coverage (5 modes):** C→HAPPY_003 · D→HAPPY_004 · M→HAPPY_005 · T→HAPPY_006 · A→HAPPY_007 (+ open-range/blank-arg variants HAPPY_008/009/010).

**Analytics coverage:** net/count→HAPPY_014 · PU→015 · PY→016 · FE→017 · IN→018 · top→019 · OTHER-type→026 · description TCAT/TTYP/UNKNOWN→020/021/022.

**Boundary coverage:** merchant max→BND_001 · category max→BND_002 · amount max→BND_003 · comma→BND_004 · 1-decimal pad→BND_005 · >2-decimals truncate→BND_006 · date inclusive→BND_007 · from>to empty→BND_008 · exactly 6 (no more)→BND_009 · 7 (more/PF8)→BND_010 · 16-char card→BND_011.

**Totals:** 26 Normal + 11 Boundary + 11 Abnormal = **48 TC**.

## Standard fixture — F-STD

`TRANFILE` is not seeded in the reg, so F-STD is **constructed on the `RTRAN` PICs** and marked *(needs real data)* where exact production values matter. Type/category descriptions use the real `TTYPFILE`/`TCATFILE` seed values.

**F-STD** = the transaction file (`TRANFILE`) contains these 8 records, all on card `4111111111110001` (so the card filter returns the set):

| TR-ID | Type/Cat | Merchant id / name | Amount | Proc date |
|---|---|---|---|---|
| TRN0000000000001 | PU / 0001 | 100000001 ACME STORE | 120.00 | 2026-08-01 |
| TRN0000000000002 | PU / 0002 | 100000002 CAFE ROMA | 45.50 | 2026-08-02 |
| TRN0000000000003 | PU / 0001 | 100000001 ACME STORE | 200.00 | 2026-08-03 |
| TRN0000000000004 | PY / 0001 | 100000003 BANK PAYMENT | -300.00 | 2026-08-04 |
| TRN0000000000005 | PY / 0001 | 100000003 BANK PAYMENT | -150.00 | 2026-08-05 |
| TRN0000000000006 | FE / 0001 | 100000004 LATE FEE CO | 25.00 | 2026-08-06 |
| TRN0000000000007 | IN / 0001 | 100000005 INTEREST CO | 15.75 | 2026-08-07 |
| TRN0000000000008 | RF / 0001 | 100000006 REFUND CO | -30.00 | 2026-08-08 |

Filter-wide analytics for the card filter (all 8): **Matches 8**, Net total **-73.75**; Purchases cnt 3 / sum 365.50; Payments cnt 2 / sum -450.00; Fees cnt 1 / sum 25.00; Interest cnt 1 / sum 15.75; RF (type not classified) → net total only; **Top tran** 200.00 (TRN0000000000003). Page 1 shows the first 6 rows and sets the "more" switch; PF8 shows the remaining 2 then "End of list".

Deltas used by specific TCs are stated in each TC's precondition (e.g. F-STD-6 = only 6 records match; F-STD-7 = 7 match; F-DESC = one record with type `01`/cat `0001` for the description lookup).

## Discrepancies · to confirm

1. **Analytics type codes vs type master (real AS-IS mismatch).** `OUTRNIN 2700-ACCUMULATE` classifies subtotals on `TR-TYPE-CD` = `'PU' / 'PY' / 'FE' / 'IN'` (working-storage constants, confirmed active), but the seeded type master `TTYPFILE` uses numeric codes `01`–`07` and `TCATFILE` keys are `<type2><cat4>` (e.g. `010001`). A single transaction record therefore **cannot both** land in a subtotal (needs `PU`…`IN`) **and** resolve a `TCAT`/`TTYP` description (needs `01`…`07`): with `PU`-style types the description falls through to `UNKNOWN`; with `01`-style types the four subtotals stay zero (accumulate WHEN OTHER). If production `TRANFILE` uses `01`-style type codes, the Purchases/Payments/Fees/Interest subtotal branches never fire. **The subtotal TCs (HAPPY_015–018) use `PU/PY/FE/IN` — what the code keys on — and the description TCs (HAPPY_020–022) use `01`-style codes; both are marked *(needs real data)*. Confirm the production `TR-TYPE-CD` domain.**
2. **PF7 semantics.** The UI §1.2 function-key table describes PF7 as "page backward to the previous page", but `2300-RESTART` resets the paging cursor to the top (`MOVE LOW-VALUES TO KTI-START-KEY`) and re-lists page 1 — there is no true backward paging (the browse is forward-only). The verbatim on-screen labels are `PF7=Restart` / `PF7=top`. TCs ground on the **code** (restart to first page).
3. **Out-of-scope app members.** `apps.json` lists OCTRNIN's app graph with many members (OCMENU, OCSGNON, OUANLIN, OUDATE, OUSTMIN, …). OCTRNIN's screen flow LINKs **only OUTRNIN** and XCTLs OCMENU. OUANLIN / OUDATE / OUSTMIN belong to other screens (OCANLIN / OCSTMIN) and are **not reachable from the Transaction Inquiry screen** → excluded from this set.

## Audit result

`audit_testcase.py --reg <REG> --root OCTRNIN` → **12 candidates, all expected and dismissed with reason** (the ORION-CCMS pattern); no wording / fold / empty-field / parity / automation flags.

- `[valid] could not run msg_codes` ×1 — the audit's `_root_xml` looks only under `parsed/cobol_xml/main|sub/`, but the reg stores XML under `ORION-CCMS/cbl/`. `msg_codes.py` run directly on `OCTRNIN.xml` and `OUTRNIN.xml` confirms **0 codes** — this screen uses literal English strings, not EI/EF codes. **Dismissed.**
- `[vague] 異常系 case but expected cites no message code / STOP` ×11 — one per Abnormal TC (ABNORMAL_001–011). The verbatim on-screen English string in each `expected` (e.g. `Mode must be C D M T or A.`) **is** the assertion; there is no EI/EF code to cite. **Dismissed.**

Two `[wording]` candidates fired on the first draft (a fixture label `F-STD-CAT0` and the data-name `TR-TYPE-CD` had leaked into `precondition`); both were rewritten into business wording with the identifiers moved to `remark`, and the re-audit is clean of them.

**Judgment dims hand-reviewed:** every abnormal `expected` names the exact source message; every classifier/analytics TC's figures reconcile against the F-STD arithmetic (net -73.75 = Σ subtotals -43.75 + unclassified refund -30.00; Top Tran 200.00 = max); steps are reproducible operator actions. Spot-checked HAPPY_014 (net/count), HAPPY_011 (PF8 paging + message), ABNORMAL_007 (two-dot amount → `WS-M-BAD-AMT`) against `OCTRNIN.cbl` / `OUTRNIN.cbl`.
