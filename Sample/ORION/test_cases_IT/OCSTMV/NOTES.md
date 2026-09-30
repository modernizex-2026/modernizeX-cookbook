# OCSTMV — Statement View · Test-case derivation notes

**App:** OCSTMV (CICS transaction `ORSV`) — "Statement View"
**System:** ORION-CCMS (ORION Credit Card Management System) · On-line (CICS/BMS)
**Kind:** **UI (CICS/BMS)** — `design_evidence.py` reports `kind=ui` ("a UI document exists"); the reg carries BMS mapset `MSTMV`/map `MSTMVA`. Derivation uses the UI viewpoint catalog (A–E), BMS-instantiated (AID keys, map fields, CICS navigation).
**Deliverable language:** English (skill EN edition). Verbatim on-screen strings are reproduced unchanged inside 「…」; program/paragraph/field/line identifiers live only in the 備考 (remark) column.

**Evidence read (ground truth):**
- Source AST: `parsed/cobol_xml/ORION-CCMS/cbl/OCSTMV.xml` (412 lines) — authoritative.
- UI design: `design_asis/UI/OCSTMV_StatementView_ScreenDesign.md`.
- BMS map: mapset `MSTMV` / map `MSTMVA` (item details in design §2).
- Copybooks: `RSTMT` (statement record), `WMSG` (shared messages), `WHEAD` (header), `WCONST` (flags / file names / edit masks), `KCOMM` (commarea), `MSTMV` (map I/O).
- `msg_codes.py OCSTMV.xml` → **0 EI/EF codes** (this app emits **literal English strings** to the on-screen message line `ERRMSG`, not coded messages — the established ORION-CCMS convention, same as OCREPT/OCTRNIN).
- `crud_from_ast.py` / design §5 → **read-only**: reads `WS-STMTFILE` only; no C/U/D. No sub-program `CALL`/`LINK` (design_evidence listed OUANLIN/OUDATE/OUSTMIN as members by name only — OCSTMV does **not** call them; see Discrepancies 3).

> **Approval gate (NON-INTERACTIVE run):** the viewpoint table below is **recorded as an assumption and accepted** so generation can proceed without a live approval round, per the run's non-interactive instruction. If a reviewer later rejects a viewpoint decision, only the affected rows change.

---

## What OCSTMV actually does (from the AST)

`0000-MAIN` (L101): first entry (`EIBCALEN=0`, or `CA-FIRST-ENTER`) → `1000-SEND-INITIAL` (paint the empty form + the guidance prompt); otherwise → `2000-PROCESS-INPUT`. Always ends with `9000-RETURN TRANSID('ORSV')` — pseudo-conversational.

`1000-SEND-INITIAL` (L120): clear the map, populate the header (`8000-POPULATE-HEADER`), put the prompt 「Enter account id and cycle, press ENTER.」 on the message line, place the cursor on Acct ID, `SEND MAP ERASE`.

`2000-PROCESS-INPUT` (L137) evaluates `EIBAID`:
- **ENTER** → `2100-VIEW-STMT`
- **PF3** → `7000-XCTL-MENU` → `XCTL PROGRAM('OCMENU')` (back to the main menu)
- **PF4** → `1000-SEND-INITIAL` (clear the form, re-prompt)
- **any other key** → repaint + message 「Invalid key pressed. Please try again.」 (`WS-MSG-INVALID-KEY`)

`2100-VIEW-STMT` (L153): receive the map → `6100-VALIDATE-KEY` → if invalid, repaint with the validation message and stay; else `3000-READ-STMT`:
- **record found** (`DFHRESP(NORMAL)`) → `4000-POPULATE-DETAIL` fills Open Bal / Close / Min Due / Due Date, message 「Statement displayed.」
- **not found** (`DFHRESP(NOTFND)`) → `4100-CLEAR-DETAIL` blanks the four detail fields, message 「No statement for that account and cycle.」
- **any other `RESP`** → `9500-ABEND-RTN`: `SEND TEXT` 「OCSTMV: unrecoverable file error. Contact support.」 then `RETURN` (transaction ends).

`6100-VALIDATE-KEY` (L227) — key parsing via `6000-PARSE-NUM` (L259):
- `6000-PARSE-NUM` scans up to `WS-NC-LEN` characters: **embedded/leading/trailing SPACES are skipped** (`CONTINUE`, L267-268), digits are accumulated (L269-273), any **other character makes the field invalid** (L274-275); **zero digits found also makes it invalid** (L278-279).
- **Acct ID** (`ACCTIDI`, len 11): if invalid **or** digit-count > 11 → 「Account id must be numeric.」 (L233-236). The value is right-aligned into `WS-ACCT` PIC 9(11), so **1–11 digits are all accepted** (fewer digits = implied leading zeros). *(digit-count > 11 is unreachable — the map field is only 11 columns; see Discrepancies 4.)*
- **Cycle** (`STCYCI`, len 6): if invalid **or** digit-count **≠ 6** → 「Cycle must be six digits (YYYYMM).」 (L242-245). Cycle must be **exactly six digits**.
- **Cycle month** = `WS-CYCLE(5:2)` (last two of YYYYMM): if `< 1` **or** `> 12` → 「Cycle month must be 01 through 12.」 (L249-251).

Header: `TRNNAMEO=ORSV`, `PGMNAMEO=OCSTMV`, `TITLEO=WS-HDR-TITLE` = 「ORION CREDIT CARD MANAGEMENT SYSTEM」; `CURDATEO`/`CURTIMEO` from `8500-GET-DATE-TIME` (`ASKTIME`/`FORMATTIME`, date `YYYY-MM-DD` with `-`, time `HH:MM:SS` with `:`).

Balance display: `4000-POPULATE-DETAIL` moves each amount through the edit mask `WS-ED-BAL` PIC `-,---,---,--9.99` and takes `WS-ED-BAL(2:15)` into the 15-column display field — so amounts show **thousands separators, two decimals, a floating leading minus for credits**, and no BLANK-WHEN-ZERO (zero shows `0.00`).

---

## Fixture — `F-STD` (constructed; **no STMTFILE seed exists** in the input `data/` dir — `WS-STMTFILE` is a VSAM KSDS built by batch `OUSTMB`/`OUSTMIN`, not seeded from an instream dataset. Values are constructed from the `RSTMT` layout; account ids reuse the OCCARDA real-seed low numbers for cross-app consistency, marked *(constructed)*.)

- **Session:** an operator has signed on (via the Sign On screen) and reached Statement View (`ORSV`) from the main menu.
- **Statement file (`WS-STMTFILE` = `STMTFILE`, VSAM KSDS)** — key `ST-ACCT-ID` 9(11) + `ST-CYCLE` 9(06); amounts `ST-OPEN-BAL`/`ST-CLOSE-BAL`/`ST-MIN-DUE` S9(10)V99; `ST-DUE-DATE` X(10):
  - **S1** `Acct=00000000001` · `Cycle=202412` · Open `1,500.00` · Close `2,350.75` · Min Due `235.08` · Due `2025-01-15`
  - **S2** `Acct=00000000001` · `Cycle=202411` · Open `1,000.00` · Close `1,200.00` · Min Due `120.00` · Due `2024-12-15`
  - **S3** `Acct=00000000005` · `Cycle=202412` · Open `320.50` · Close `-45.00` (credit) · Min Due `0.00` · Due `2025-01-15`
  - **S4** `Acct=00000000009` · `Cycle=202412` · Open `0.00` · Close `0.00` · Min Due `0.00` · Due `2025-01-15`
  - *(No record for `Acct=00000000001 Cycle=202401`, `Acct=00000000099 Cycle=202412`, or `Acct=99999999999 Cycle=202412` — used for the not-found / max-length cases.)*

Each TC's 前提条件 says "F-STD" plus only its delta.

---

## Viewpoint table (観点表) — every base UI viewpoint decided

大項目 = **Statement View** (single function). 中項目 = canonical viewpoint label.

| # | 観点 (中項目) | Applicable? | Evidence | Planned TC |
|---|---|---|---|---|
| U1 | 画面表示・レイアウト (Screen display / layout) | ✅ | §2 map MSTMVA; captions 「Tran:」「Date:」「Pgm :」「Time:」「Acct ID :」「Cycle   :」「Open Bal:」「Close   :」「Min Due :」「Due Date:」 + key line | IT_STMTVIEW_HAPPY_001 |
| U2 | 初期値・デフォルト (Initial values / defaults) | ✅ | `8000-POPULATE-HEADER` L296-302 (Tran/Pgm/Title/Date/Time); form blank on `LOW-VALUES`; prompt 「Enter account id and cycle, press ENTER.」 | IT_STMTVIEW_HAPPY_002 |
| U3 | 表示条件・活性制御 (Display conditions / enable control) | ❌ — no conditional enable/disable: the two key fields are always enterable, the six detail fields are always display-only; found→populate vs not-found→clear is a data outcome (covered by HAPPY_003/004), not an attribute change | design §2 item states; `SEND … DATAONLY` keeps map attributes | — (confirmed in HAPPY_001) |
| U4 | 文字・書式表示 (Characters / format display) | ✅ | edit mask `WS-ED-BAL -,---,---,--9.99`, `WS-ED-BAL(2:15)` L206-214; field widths (Acct 11, Cycle 6, detail 15, ERRMSG 78) | HAPPY_005 (sign/separators) + BOUNDARY_006 (0.00) + HAPPY_001 (widths) |
| U5 | 入力チェック (Input validation) | ✅ | §3 + `6100-VALIDATE-KEY`/`6000-PARSE-NUM` | ABNORMAL_001–005, BOUNDARY_003/004/005 |
| U6 | 操作性・キー (Operability / function keys) | ✅ | `EIBAID` = ENTER/PF3/PF4/other (L138-147) | HAPPY_006 (PF3), HAPPY_007 (PF4), ABNORMAL_006 (other), HAPPY_003 (ENTER) |
| U7 | 状態遷移 (Screen / state transitions) | ✅ | `0000-MAIN` first-enter vs re-enter; `RETURN TRANSID('ORSV')` L340; cursor back to Acct ID after display (`MOVE -1 TO ACCTIDL` L176) | IT_STMTVIEW_HAPPY_008 |
| U8 | 機能・業務フロー (Function / business flow) | ✅ | main lookup: found (「Statement displayed.」) vs not-found (「No statement…」) — the two display variants | HAPPY_003 (found), HAPPY_004 (not-found) |
| U9 | 計算・編集ロジック (Calculation / editing logic) | ❌ — no derived/computed value; amounts are read straight from the record and only edited for display (covered under 文字・書式表示) | AST — only `MOVE` through the edit mask, no `COMPUTE` on business data | — (covered by HAPPY_005) |
| U10 | 出力・帳票 (Output / report) | ❌ — read-only; nothing is written/printed; "output" is the on-screen detail | design §5 "no row created/updated/deleted" | — (covered by HAPPY_003) |
| U11 | データ整合性・冪等性 (Data integrity / idempotency) | ✅ (read-only) | repeated lookups return the same detail; no `WRITE`/`REWRITE`/`DELETE` | HAPPY_009 (consecutive lookups) |
| U12 | 排他・同時実行 (Exclusion / concurrent access) | ❌ — plain `READ` (no `UPDATE`/lock), read-only | AST verbs L191 | — (runtime) |
| U13 | メッセージ・異常系 (Messages / abnormal) | ✅ | 8 literal outcomes (see coverage matrix) | ABNORMAL_001–007, BOUNDARY_003/004/005 |
| U14 | 境界値 (Boundary values) | ✅ | Acct 1..11 digits; cycle exactly 6 digits; month 01–12 both sides; zero balance | BOUNDARY_001–006 |
| U15 | 回復・リラン (Recovery / rerun) | ❌ — pseudo-conversational, nothing committed to recover; re-press ENTER re-reads | AST (no restart/flag) | — (runtime) |
| U16 | 権限・セキュリティ (Authority / security) | ❌ — OCSTMV has **no** signon/`CA-USER-TYPE` gate of its own; role enforced upstream (Sign On → menu) | KCOMM `CA-*` fields; design §1.2 roles | — |
| U17 | ログ・監査 (Log / audit) | ❌ — no log/audit record written | AST (no WRITE to a log) | — |
| U18 | 連携・インターフェース (Linkage / interface) | ✅ | `XCTL 'OCMENU'` L292; `RETURN TRANSID('ORSV')` L340; `READ WS-STMTFILE` L191 — no other `CALL`/`LINK` | HAPPY_006 (XCTL), HAPPY_008 (RETURN), HAPPY_003 (file read) |
| U19 | 運用 (Operation) | ❌ — on-screen only; no paper/printer setup; performance is runtime | captions/env | — (perf runtime) |

CICS/BMS-specific instantiation applied: U6 = **AID keys** (ENTER/PF3/PF4/other, from `EIBAID`), U7/U18 = **CICS navigation** (`XCTL`, `RETURN TRANSID`).

---

## Coverage matrix — messages · variants · boundaries → TC

**On-screen messages** (literal strings sent to `ERRMSG` / `SEND TEXT`; source is ground truth — design §3 lists only M2, the rest are recovered from the AST):

| # | Verbatim message | Type | Trigger (paragraph, line) | TC |
|---|---|---|---|---|
| M1 | 「Enter account id and cycle, press ENTER.」 (`WS-M-PROMPT`) | I (prompt) | `1000-SEND-INITIAL` L123 | HAPPY_001, HAPPY_002, HAPPY_007 |
| M2 | 「Invalid key pressed. Please try again.」 (`WS-MSG-INVALID-KEY`, WMSG) | E | `2000-PROCESS-INPUT` WHEN OTHER L146 | ABNORMAL_006 |
| M3 | 「Account id must be numeric.」 (`WS-M-ACCT-NUM`) | E | `6100-VALIDATE-KEY` L235 | ABNORMAL_001 (blank), ABNORMAL_002 (non-numeric), BOUNDARY_001/002 (accept side) |
| M4 | 「Cycle must be six digits (YYYYMM).」 (`WS-M-CYC-NUM`) | E | `6100-VALIDATE-KEY` L244 | ABNORMAL_003 (blank), ABNORMAL_004 (non-numeric), BOUNDARY_003 (5 digits) |
| M5 | 「Cycle month must be 01 through 12.」 (`WS-M-CYC-MM`) | E | `6100-VALIDATE-KEY` L251 | ABNORMAL_005 (13), BOUNDARY_004 (00), BOUNDARY_005 (13/12) |
| M6 | 「No statement for that account and cycle.」 (`WS-M-NOTFND`) | E/I | `2100-VIEW-STMT` ELSE L174 (`DFHRESP(NOTFND)`) | HAPPY_004, BOUNDARY_002 |
| M7 | 「Statement displayed.」 (`WS-M-SHOWN`) | C/I | `2100-VIEW-STMT` L171 (`DFHRESP(NORMAL)`) | HAPPY_003, HAPPY_005, HAPPY_009, BOUNDARY_001/006 |
| M8 | 「OCSTMV: unrecoverable file error. Contact support.」 (`SEND TEXT`) | E (abend) | `3000-READ-STMT` WHEN OTHER → `9500-ABEND-RTN` L345/L353 | ABNORMAL_007 |

**Display variants** (`2100-VIEW-STMT`): found → detail populated + M7 (HAPPY_003); not-found → detail cleared + M6 (HAPPY_004).

**Boundaries:** Acct 1-digit accepted / space-tolerant parse (BOUNDARY_001); Acct max 11-digit `99999999999` accepted as numeric (BOUNDARY_002); cycle 5-digit rejected — one short of the required 6 (BOUNDARY_003); cycle month lower — `00` rejected / `01` accepted (BOUNDARY_004); cycle month upper — `12` accepted / `13` rejected (BOUNDARY_005); zero balances show `0.00` (BOUNDARY_006).

**Totals:** **22 TC** — 9 Normal / 7 Abnormal / 6 Boundary. Live messages covered 8/8; display variants 2/2; input fields 2/2 × (blank / non-numeric / length / range as applicable) covered; key AIDs 4/4 (ENTER/PF3/PF4/other).

---

## Discrepancies · to confirm (design vs source)

1. **Design §3 lists only one message** (M2 「Invalid key pressed…」). The source actually emits **eight** distinct outcomes (M1–M8 above). Per the skill rule "source is ground truth", all eight are covered; the design check table is under-specified.
2. **Design §1.2 wording "Return to the calling screen"** — the source returns specifically to the **main menu** (`XCTL PROGRAM('OCMENU')`, `WS-MENU-PGM='OCMENU'`), not a generic caller. TCs assert OCMENU.
3. **`design_evidence.py` listed OUANLIN / OUDATE / OUSTMIN as member program designs** — grouped by the "Statement/Analytics" naming, **not** by a real call. OCSTMV has **no** `CALL`/`LINK` to any of them (only `XCTL 'OCMENU'` and `READ WS-STMTFILE`). Those sub-program designs are therefore **not** part of OCSTMV's test basis (U18 covers only the real XCTL/RETURN/READ).
4. **Dead branch — Acct digit-count > 11.** `6100-VALIDATE-KEY` L233 tests `WS-NC-DIGITS > 11`, but the Acct ID map field is only 11 columns, so the digit count can never exceed 11. The `> 11` arm is **unreachable** (the numeric/blank check via `WS-INVALID` still fires for M3). Excluded with reason — no dedicated TC (never test dead code). The reachable M3 triggers (non-numeric, blank) are covered by ABNORMAL_001/002.
5. **Numeric parse tolerates spaces** (`6000-PARSE-NUM` skips SPACE): "` 1`", "`1 2`" etc. are accepted as the digits present (source quirk). Acct ID therefore accepts 1–11 digits with any interspersed blanks; recorded as BOUNDARY_001. Flag to product owner if strict fixed-width entry is expected.

---

## Audit result

`python3 scripts/audit_testcase.py cases.json --reg <REG> --root OCSTMV` → candidates all grounded/dismissed:

- **`[valid] could not run msg_codes (no reg xml)` (1):** *dismissed — tool path-layout mismatch, not a coverage gap.* The audit's `_root_xml` looks under `parsed/cobol_xml/main|sub/`, but this reg stores the program at `parsed/cobol_xml/ORION-CCMS/cbl/OCSTMV.xml`, so the automated code-coverage step self-skips. I ran `msg_codes.py` directly on that XML → **0 EI/EF codes** (literal-message app), so there is no coded-message gap regardless.
- **`[vague]` "異常系 case but expected cites no message code / STOP" (every Abnormal/Boundary row that asserts an error):** *dismissed as a class.* OCSTMV emits **literal English strings**, not EI/EF codes; the assertion IS the verbatim 「…」 message quoted in each 期待結果 (documented ORION-CCMS convention — no code exists to cite). The vagueness *literal* check passes — every such 期待結果 carries a 「…」 quote and concrete data.
- No `[ids]`, `[fields]`, `[fold]`, `[wording]`, `[parity]`, `[auto]` candidates expected: ids unique, all required fields present, no abnormal row folds ≥2 codes, no code tokens in body fields (identifiers kept in 備考), automation columns all `×`.

Every candidate grounded/dismissed; judgment dims (each 期待結果 traces to a cited paragraph/line; steps reproducible; 観点表 complete vs the source) hand-reviewed. Spot-check re-verified: HAPPY_003 (found → M7 + 4 detail fields, L169-171/205-214), BOUNDARY_005 (month 12 accepted / 13 → M5, L249-251), ABNORMAL_003 (cycle blank → 0 digits → M4, L242-245/278-279).
