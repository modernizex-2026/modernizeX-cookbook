# OCREPT — Report Request · Test-case derivation notes

**App:** OCREPT (CICS transaction `ORRP`) — "Report Request"
**System:** ORION-CCMS (ORION Credit Card Management System) · On-line (CICS/BMS)
**Kind:** **UI (CICS/BMS)** — `design_evidence.py` reports `kind=ui` ("a UI document exists"); the reg carries BMS mapset `MREPT`/map `MREPTA` (SDD-MAP-025). Derivation uses the UI viewpoint catalog (A–E), BMS-instantiated (AID keys, map fields, CICS navigation).
**Deliverable language:** English (skill EN edition). Verbatim on-screen strings are reproduced unchanged inside 「…」; program/paragraph/field/line identifiers live only in the 備考 (remark) column.

**Evidence read (ground truth):**
- Source AST: `parsed/cobol_xml/ORION-CCMS/cbl/OCREPT.xml` (566 lines) — authoritative.
- Called subroutine AST: `.../cbl/OUDATE.xml` (date validator, `VALD` function).
- UI design: `design_asis/UI/OCREPT_ReportRequest_ScreenDesign.md`.
- BMS map: `bms_maps.json` MREPT/MREPTA (rendered with `bms_mockup.py --render MREPTA`).
- Copybooks: WCONST, WMSG, WHEAD, KCOMM, KDATE, RBILL, RTRAN, MREPT.
- `msg_codes.py OCREPT.xml` → **0 EI/EF codes** (this app emits literal English strings to the on-screen message line `ERRMSG`, not coded messages — an ORION-CCMS convention).
- `crud_from_ast.py` / design §5 → **read-only**: reads BILLFILE + TRANFILE; no C/U/D.

> **Approval gate (NON-INTERACTIVE run):** the viewpoint table below is **recorded as an assumption and accepted** so generation can proceed without a live approval round, per the run's non-interactive instruction. If a reviewer later rejects a viewpoint decision, only the affected rows change.

---

## What OCREPT actually does (from the AST)

`0000-MAIN`: first entry (EIBCALEN=0, or `CA-FIRST-ENTER`) → `1000-SEND-INITIAL` (paint the empty form + the guidance prompt); otherwise `2000-PROCESS-INPUT`. Always ends with `9000-RETURN TRANSID('ORRP')` — pseudo-conversational.

`2000-PROCESS-INPUT` evaluates `EIBAID`:
- **ENTER** → `2100-RUN-REPORT`
- **PF3** → `7000-XCTL-MENU` → `XCTL PROGRAM('OCMENU')` (back to main menu)
- **PF4** → `1000-SEND-INITIAL` (clear the form)
- **any other key** → repaint + message 「Invalid key pressed. Please try again.」

`2100-RUN-REPORT`: receive map → `2120-VALIDATE-TYPE` → (on error, send + exit) → `2130-VALIDATE-DATES` → (on error, send + exit) → `EVALUATE TRUE` on the report type: `RPT-BILLS`(01) → `2200-REPORT-BILLS`, `RPT-TRANS`(02) → `2300-REPORT-TRANS` → `2400-BUILD-SUMMARY`.

The "report" is **not** a printed report or a submitted request: each report paragraph browses the file (`STARTBR`/`READNEXT`/`ENDBR`), counts records whose date falls in [From,To] inclusive, sums the amount, and `2400-BUILD-SUMMARY` `STRING`s a one-line summary `Type <NN> Count: <n> Total: <amount>` onto the `ERRMSG` line. The screen stays on `MREPTA`. (The design §1.2 wording "submit the report request / confirm accepted" is aspirational; the source computes and displays inline — see Discrepancies.)

Date filtering: bills use `BL-PAY-DATE` (X(10)); trans use `TR-ORIG-TS(1:10)` (X(10)). Both are compared **as strings** to `WS-FROM-DATE`/`WS-TO-DATE` (X(10)) — correct because the dates are ISO `YYYY-MM-DD`, which sorts lexically.

Date validity is delegated to **OUDATE `VALD`** (`KD-STATUS='00'`=valid): digits at pos 1-4/6-7/9-10 must be numeric (separators unchecked), month 01–12, day 01–(days-in-month) where **February is always 29** (no leap-year test — a source quirk: `2025-02-29` is accepted, `2025-02-30` rejected).

---

## Fixture — `F-STD` (constructed; **no BILLFILE/TRANFILE seed exists** in the input `data/` dir → values are constructed from RBILL/RTRAN layouts, marked *(needs real data)*)

- **Session:** an operator has signed on (via the Sign On screen) and reached Report Request (`ORRP`) from the main menu.
- **Bill file (BILLFILE, VSAM KSDS)** — key `BL-ID` 9(11), date `BL-PAY-DATE` X(10), amount `BL-AMOUNT` S9(10)V99:
  - B1 `BL-ID=00000000001` · `2025-01-10` · `100.00`
  - B2 `BL-ID=00000000002` · `2025-03-15` · `250.00`
  - B3 `BL-ID=00000000003` · `2025-06-30` · `500.00`
  - B4 `BL-ID=00000000004` · `2024-12-31` · `75.00`  (before 2025)
  - B5 `BL-ID=00000000005` · `2025-07-01` · `300.00`
- **Transaction file (TRANFILE, VSAM KSDS)** — key `TR-ID` X(16), date `TR-ORIG-TS(1:10)`, amount `TR-AMT` S9(9)V99:
  - T1 `TR-ID=TXN0000000000001` · `2025-01-05` · `50.00`
  - T2 `TR-ID=TXN0000000000002` · `2025-03-20` · `200.00`
  - T3 `TR-ID=TXN0000000000003` · `2025-07-01` · `125.00`
  - T4 `TR-ID=TXN0000000000004` · `2024-11-11` · `40.00`  (before 2025)

Each TC's 前提条件 says "F-STD" plus only its delta. Amount edit masks: count `ZZZZZZ9`, total `-,---,---,---,--9.99` (so `1150` → `1,150.00`).

---

## Viewpoint table (観点表) — every base UI viewpoint decided

大項目 = **Report Request** (single function). 中項目 = canonical viewpoint label.

| # | 観点 (中項目) | Applicable? | Evidence | Planned TC |
|---|---|---|---|---|
| U1 | 画面表示・レイアウト (Screen display / layout) | ✅ | §2 map MREPTA (SDD-MAP-025); header/legend/PF-line literals | IT_REPTREQ_HAPPY_001 |
| U2 | 初期値・デフォルト (Initial values / defaults) | ✅ | `8000-POPULATE-HEADER` (Tran/Pgm/Title/Date/Time); form blank on `LOW-VALUES` | IT_REPTREQ_HAPPY_002 |
| U3 | 表示条件・活性制御 (Display conditions / enable control) | ❌ — no conditional display: the 3 inputs are always enabled, all others display-only; §2 item-states are identical in "Initial display" and "Selection entered" | design §2 item states | — (confirmed in HAPPY_001) |
| U4 | 文字・書式表示 (Characters / format display) | ✅ (folded) | field widths RPTYPE=2, dates=10, ERRMSG=78; edit masks | HAPPY_001 (widths) + HAPPY_005 (masks) + BOUNDARY_001 |
| U5 | 入力チェック (Input validation) | ✅ | §3 checks; `2120`/`2130` | ABNORMAL_001–007, BOUNDARY_001/004 |
| U6 | 操作性・キー (Operability / function keys) | ✅ | `EIBAID` = ENTER/PF3/PF4/other | HAPPY_006 (PF3), HAPPY_007 (PF4), ABNORMAL_012 (other) |
| U7 | 状態遷移 (Screen / state transitions) | ✅ | `0000-MAIN` first-enter vs re-enter; `RETURN TRANSID('ORRP')` | IT_REPTREQ_HAPPY_008 |
| U8 | 機能・業務フロー (Function / business flow) | ✅ | 2 classifier values `RPT-BILLS`(01), `RPT-TRANS`(02) | HAPPY_003 (bills), HAPPY_004 (trans) |
| U9 | 計算・編集ロジック (Calculation / editing logic) | ✅ | `2220`/`2320` count+total; `2400` STRING + edit masks | HAPPY_005 |
| U10 | 出力・帳票 (Output / report) | ✅ (on-screen summary only) | §5 read-only; output = the summary line, no file/print | HAPPY_003/004/005 |
| U11 | データ整合性・冪等性 (Data integrity / idempotency) | ❌ — read-only, no C/U/D; every run is naturally idempotent | design §5 "no row created/updated/deleted" | — |
| U12 | 排他・同時実行 (Exclusion / concurrent access) | ❌ — browse-only (`STARTBR`/`READNEXT`), no locks/updates | AST verbs | — (runtime) |
| U13 | メッセージ・異常系 (Messages / abnormal) | ✅ | 12 live literal messages (see coverage matrix); 1 dead excluded | ABNORMAL_001–012 |
| U14 | 境界値 (Boundary values) | ✅ | RPTYPE 2-char format; date-range inclusivity both sides; Feb-29 quirk; 0 records | BOUNDARY_001–006 |
| U15 | 回復・リラン (Recovery / rerun) | ❌ — pseudo-conversational; nothing committed to recover; re-press ENTER re-runs | AST (no restart/flag) | — (runtime) |
| U16 | 権限・セキュリティ (Authority / security) | ❌ — OCREPT itself has **no** `CA-USER-TYPE`/signon gate; role is enforced upstream (Sign On → menu) | KCOMM `CA-USER-TYPE` unused here; design §1.2 roles | — |
| U17 | ログ・監査 (Log / audit) | ❌ — no log/audit record written | AST (no WRITE to a log) | — |
| U18 | 連携・インターフェース (Linkage / interface) | ✅ | `CALL 'OUDATE'` (date validation); `XCTL 'OCMENU'`; `RETURN TRANSID('ORRP')` | HAPPY_009 (OUDATE), HAPPY_006 (XCTL), HAPPY_008 (RETURN) |
| U19 | 運用 (Operation) | ❌ — on-screen only; no paper/printer setup; performance is runtime | captions/env | — (perf runtime) |

CICS/BMS-specific instantiation applied: U6 = **AID keys** (ENTER/PF3/PF4/other, from `EIBAID`), U7/U18 = **CICS navigation** (`XCTL`, `RETURN TRANSID`, `CALL`).

---

## Coverage matrix — messages · classifiers · boundaries → TC

**On-screen messages** (literal strings sent to `ERRMSG`; design §3):

| # | Verbatim message | Type | Trigger (paragraph, line) | TC |
|---|---|---|---|---|
| M1 | 「Type 01=Bills 02=Trans, dates YYYY-MM-DD.」 | I (prompt) | `1000-SEND-INITIAL` L88 | HAPPY_001 |
| M2 | 「Unsupported report type.」 | I | `2100-RUN-REPORT` WHEN OTHER L137-139 | **EXCLUDED — dead code** (see below) |
| M3 | 「Report type is required.」 | E | `2120-VALIDATE-TYPE` L164 | ABNORMAL_001 |
| M4 | 「Report type must be 01 or 02.」 | E | `2120-VALIDATE-TYPE` L170 | ABNORMAL_002, BOUNDARY_001 |
| M5 | 「From and to dates are required.」 | E | `2130-VALIDATE-DATES` L184 | ABNORMAL_003 (from blank), ABNORMAL_004 (to blank) |
| M6 | 「From date is not a valid date.」 | E | `2130-VALIDATE-DATES` L195 (OUDATE VALD) | ABNORMAL_005 |
| M7 | 「To date is not a valid date.」 | E | `2130-VALIDATE-DATES` L204 (OUDATE VALD) | ABNORMAL_006 |
| M8 | 「From date is later than to date.」 | E | `2130-VALIDATE-DATES` L209 | ABNORMAL_007 |
| M9 | 「Error starting bill browse.」 | E | `2210-STARTBR-BILL` WHEN OTHER L247 | ABNORMAL_008 |
| M10 | 「Error reading bill file.」 | E | `2220-READ-BILLS` WHEN OTHER L272 | ABNORMAL_009 |
| M11 | 「Error starting tran browse.」 | E | `2310-STARTBR-TRAN` WHEN OTHER L317 | ABNORMAL_010 |
| M12 | 「Error reading tran file.」 | E | `2320-READ-TRANS` WHEN OTHER L343 | ABNORMAL_011 |
| M13 | 「Invalid key pressed. Please try again.」 (WS-MSG-INVALID-KEY) | E | `2000-PROCESS-INPUT` WHEN OTHER L111 | ABNORMAL_012 |
| — | Summary `Type <NN> Count: <n> Total: <amt>` | I (result) | `2400-BUILD-SUMMARY` L358-362 | HAPPY_003/004/005 |

**Dead-code exclusion (M2):** `2120-VALIDATE-TYPE` sets the error flag and exits `2100-RUN-REPORT` unless `WS-RPT-TYPE` is exactly `01` or `02`. Control reaches the second `EVALUATE TRUE` only when the error flag is off, i.e. `RPT-BILLS` or `RPT-TRANS` is already true; the `WHEN OTHER` at L137-139 ("Unsupported report type.") is therefore **unreachable**. Excluded with reason — no TC (never test dead code).

**Classifier values** (`WS-RPT-TYPE`): 01=Bills → HAPPY_003; 02=Trans → HAPPY_004. (both = full happy flow.)

**Boundaries:** RPTYPE 2-char leading-zero format (BOUNDARY_001); date-range lower bound inclusive + From−1 excluded (BOUNDARY_002); upper bound inclusive + To+1 excluded (BOUNDARY_003); OUDATE Feb-29 non-leap quirk accepted, Feb-30 rejected (BOUNDARY_004); zero matching records → `Count: 0 Total: 0.00` (BOUNDARY_005); single-day range From=To (BOUNDARY_006).

**Totals:** 27 TC — 9 Normal / 12 Abnormal / 6 Boundary. Live messages covered 12/12; dead 1/1 excluded; classifiers 2/2; input fields 3/3 × (blank/format/validity) covered.

---

## Discrepancies · to confirm (design vs source)

1. **Report-type legend vs accepted values.** The map paints the caption 「1=Trans 2=Accts 3=Cards」 (MREPTA L10, field 16), but the program accepts only `01`=Bills and `02`=Trans (88-levels `RPT-BILLS`/`RPT-TRANS`; guidance prompt 「Type 01=Bills 02=Trans…」). The on-screen legend is stale/misleading. TCs assert the **actual** behaviour (01/02); the legend is quoted verbatim in HAPPY_001 with this note in 備考.
2. **"Submit request / confirm accepted" (design §1.2)** — no request is submitted or queued; the program computes count+total inline and shows a summary line, staying on the screen. TCs describe the real inline behaviour.
3. **Feb-29 in non-leap years is accepted** by OUDATE `VALD` (February hard-coded to 29 days, no leap test). Recorded as BOUNDARY_004; flag to product owner if strict validation is expected.

---

## Audit result

`python3 scripts/audit_testcase.py cases.json --reg <REG> --root OCREPT` → **14 candidates, all grounded/dismissed:**

- **`[valid] could not run msg_codes (no reg xml)` (1):** *dismissed — tool path-layout mismatch, not a coverage gap.* The audit's `_root_xml` resolves the program under `parsed/cobol_xml/main|sub/`, but this reg stores it at `parsed/cobol_xml/ORION-CCMS/cbl/OCREPT.xml`, so the automated code-coverage step self-skips. I ran `msg_codes.py` directly on that XML → **0 EI/EF codes** (literal-message app), so there is no coded-message gap regardless.
- **`[vague]` "異常系 case but expected cites no message code / STOP" (12 — every Abnormal row):** *dismissed as a class.* OCREPT emits **literal English strings**, not EI/EF codes; the assertion IS the verbatim 「…」 message quoted in each 期待結果. This is the documented ORION-CCMS convention — no code exists to cite. (The vagueness *literal* check passed — every abnormal 期待結果 carries a 「…」 quote and concrete data.)
- **`[wording]` data-name `YYYY-MM-DD` on HAPPY_001 (1):** *dismissed.* The token sits inside the **verbatim on-screen prompt** 「Type 01=Bills 02=Trans, dates YYYY-MM-DD.」 reproduced unchanged per the skill's verbatim-quote rule (the audit regex mistakes `YYYY-MM-DD` for a hyphenated data-name — the known ORION "YYYY-MM-DD wording-regex trap"). No other case repeats the token — they all use concrete dates (e.g. `2025-01-01`).
- No `[ids]`, `[fields]`, `[fold]`, `[parity]` or `[auto]` candidates: ids unique, all required fields present, no abnormal row folds ≥2 codes, automation columns all `×`.

Every candidate grounded/dismissed; judgment dims (each 期待結果 traces to a cited paragraph/line; steps reproducible; 観点表 complete vs the source) hand-reviewed. Spot-check re-verified: HAPPY_003 (bills 01, count 4 / 1,150.00 vs B1+B2+B3+B5), BOUNDARY_003 (upper bound inclusive, B3=To counted / B5=To+1 excluded, L263), ABNORMAL_007 (from>to string compare, L207-209).
