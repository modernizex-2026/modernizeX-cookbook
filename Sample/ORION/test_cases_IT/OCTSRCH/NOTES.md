# OCTSRCH — Transaction Search · Test-case derivation notes

**App:** OCTSRCH (CICS transaction `ORTS`) — "Transaction Search"
**System:** ORION-CCMS (ORION Credit Card Management System) · On-line (CICS/BMS)
**Kind:** **UI (CICS/BMS)** — `design_evidence.py` reports `kind=ui` ("a UI document exists"); the reg carries BMS mapset `MTSRCH`/map `MTSRCHA`. Derivation uses the UI viewpoint catalog (A–E), BMS-instantiated (AID keys, map fields, CICS navigation).
**Deliverable language:** English (skill EN edition). Verbatim on-screen strings are reproduced unchanged inside 「…」; program/paragraph/field/line identifiers live only in the 備考 (remark) column.

**Evidence read (ground truth):**
- Source AST / raw: `parsed/cobol_xml/ORION-CCMS/cbl/OCTSRCH.xml` and `input/.../cbl/OCTSRCH.cbl` (485 lines) — authoritative.
- UI design: `design_asis/UI/OCTSRCH_TransactionSearch_ScreenDesign.md`.
- BMS map: mapset `MTSRCH` / map `MTSRCHA` (item details in design §2; raw `bms/MTSRCH.bms`).
- Copybooks: `RTRAN` (transaction record), `WMSG` (shared messages), `WHEAD` (header + title), `WCONST` (flags / file names / edit mask `WS-ED-AMT`), `KCOMM` (commarea), `MTSRCH` (map I/O).
- `msg_codes.py OCTSRCH.xml` → **0 EI/EF codes** (this app emits **literal English strings** to the on-screen message line `ERRMSG`, not coded messages — the established ORION-CCMS convention, same as OCSTMV / OCREPT / OCTRNIN).
- `crud_from_ast.py` / design §5 → **read-only**: browses `WS-TRANFILE` (`STARTBR` / `READNEXT` / `ENDBR`) only; no C/U/D. Only inter-program hand-off is `XCTL PROGRAM('OCMENU')` (PF3); **no** `CALL`/`LINK` (see Discrepancies 2).

> **Approval gate (NON-INTERACTIVE run):** the viewpoint table below is **recorded as an assumption and accepted** so generation can proceed without a live approval round, per the run's non-interactive instruction. If a reviewer later rejects a viewpoint decision, only the affected rows change.

---

## What OCTSRCH actually does (from the AST)

`0000-MAIN` (L123): first entry (`EIBCALEN=0`, or `CA-FIRST-ENTER` / context 0) → `1000-SEND-INITIAL` (paint the empty form + the guidance prompt); otherwise → `2000-PROCESS-INPUT`. Always ends with `9000-RETURN TRANSID('ORTS')` — pseudo-conversational.

`1000-SEND-INITIAL` (L142): clear the map, populate the header (`8000-POPULATE-HEADER`), blank the five result lines (`3050-CLEAR-ROWS`), put the prompt 「Enter card and amount range, press ENTER.」 on the message line, place the cursor on Card Num, `SEND MAP ERASE`.

`2000-PROCESS-INPUT` (L160) evaluates `EIBAID`:
- **ENTER** → `2100-SEARCH`
- **PF3** → `7000-XCTL-MENU` → `XCTL PROGRAM('OCMENU')` (back to the main menu)
- **PF4** → `1000-SEND-INITIAL` (clear the form, re-prompt)
- **any other key** → repaint + message 「Invalid key pressed. Please try again.」 (`WS-MSG-INVALID-KEY`, WMSG)

`2100-SEARCH` (L177): `RECEIVE MAP` → `6100-VALIDATE-CRIT` → if invalid, repaint with the validation message and stay; else `3000-BROWSE-TRANS` → `2200-BUILD-SUMMARY` → repaint. The search **browses `TRANFILE` forward from the first record** (`STARTBR` at `LOW-VALUES` GTEQ, then `READNEXT`) and keeps the records whose **card matches exactly** (`TR-CARD-NUM = WS-CARD-KEY`, X16) **and** whose **amount is within the range** (`TR-AMT >= WS-FROM-AMT AND TR-AMT <= WS-TO-AMT`, inclusive both ends), formatting each into the next result line up to **five** (`WS-MAX-ROWS = 5`).

`6100-VALIDATE-CRIT` (L323) — criteria validation:
- **Card** (`CARDNUMI`, X16, required): blank / `LOW-VALUES` → 「Card number is required.」 (L328) and stop. No numeric/length check — any 16 characters become the exact-match key.
- **From amount** (`FRAMTI`, X12, optional): **blank → 0** (lower bound, L336); else parse via `5100-PARSE-AMOUNT`; parse-invalid → 「From amount is not a valid number.」 (L340) and stop.
- **To amount** (`TOAMTI`, X12, optional): **blank → 9999999999.99** (upper bound = largest representable, L348); else parse; parse-invalid → 「To amount is not a valid number.」 (L352) and stop.
- **Range**: `WS-FROM-AMT > WS-TO-AMT` → 「From amount cannot exceed to amount.」 (L359).

`5100-PARSE-AMOUNT` (L368) — amount-string parser over `WS-AE-IN` X(13) (the X12 map field padded one byte). Rules: **spaces and commas are ignored** (L383-386); **one decimal point** allowed, a second dot → invalid (L387-392); **digits accumulate** — **at most 10 integer digits** (`WS-AE-INT-CNT > 10` → invalid, L420) and **at most 2 fraction digits** (`WS-AE-FRAC-CNT > 2` → invalid, L413); any other character → invalid (L395-396). Result = int + frac/100 (`WS-FROM-AMT`/`WS-TO-AMT` are `S9(10)V99`).

`2200-BUILD-SUMMARY` (L200): rows found = 0 → 「No transactions in that range.」 (`WS-M-NONE-FOUND`, L202); else message = `WS-CNT-ED` (PIC `Z9`) + `WS-M-SUFFIX` (' match(es) displayed.') → e.g. 「 3 match(es) displayed.」 (L204-208).

`4000-MOVE-ROW` (L291) formats each kept record into a 70-column result line: `TR-ID`(16) + space + `TR-TYPE-CD`(2) + space + amount + space + `TR-MERCHANT-NAME`(50), truncated to 70. Amount is edited through `WS-ED-AMT` PIC `-,---,---,--9.99`, taking `WS-ED-AMT(2:15)` into the 15-col slot → **thousands separators + two decimals** (the composed line = 36 fixed cols + 50-col merchant → **the merchant name is truncated to ~34 chars** to fit the 70-column line).

Header: `TRNNAMEO=ORTS`, `PGMNAMEO=OCTSRCH`, `TITLEO=WS-HDR-TITLE` = 「ORION CREDIT CARD MANAGEMENT SYSTEM」; `CURDATEO`/`CURTIMEO` from `8500-GET-DATE-TIME` (`ASKTIME`/`FORMATTIME`, date `YYYY-MM-DD` with `-`, time `HH:MM:SS` with `:`).

---

## Fixture — `F-STD` (constructed; **no TRANFILE seed exists** in the input `data/` dir — `WS-TRANFILE`=`TRANFILE` is a VSAM KSDS keyed by `TR-ID` X(16), populated by the transaction-load path, not by an instream dataset. Values are constructed from the `RTRAN` layout; card numbers and amounts are plausible test literals, marked *(constructed)*. Same convention as OCTRNIN / OCTRANV.)

- **Session:** an operator has signed on (via the Sign On screen) and reached Transaction Search (`ORTS`) from the main menu.
- **Transaction file (`WS-TRANFILE` = `TRANFILE`, VSAM KSDS)** — key `TR-ID` X(16); fields used: `TR-TYPE-CD` X(2), `TR-AMT` S9(9)V99, `TR-MERCHANT-NAME` X(50), `TR-CARD-NUM` X(16). Browse order = ascending `TR-ID`.

  **Card A = `4111111111111111` — 7 transactions:**
  | TR-ID | Type | Amount | Merchant |
  |---|---|---|---|
  | TXN0000000000001 | PU | 100.00 | AMAZON MARKETPLACE |
  | TXN0000000000002 | PU | 250.50 | WALMART STORE 1234 |
  | TXN0000000000003 | FE | 35.00 | ANNUAL FEE |
  | TXN0000000000004 | PY | 500.00 | ONLINE PAYMENT |
  | TXN0000000000005 | PU | 1000.00 | BEST BUY 0007 |
  | TXN0000000000006 | PU | 2500.75 | APPLE STORE |
  | TXN0000000000007 | PU | 9999.99 | TESLA MOTORS |

  **Card B = `4222222222222222` — 2 transactions:**
  | TR-ID | Type | Amount | Merchant |
  |---|---|---|---|
  | TXN0000000000008 | PU | 75.00 | STARBUCKS 123 |
  | TXN0000000000009 | PU | 300.00 | SHELL GAS |

  **Card C = `4333333333333333`** — *no transactions on file* (used for the valid-card / no-result case).

Each TC's 前提条件 says "F-STD" plus only its delta.

---

## Viewpoint table (観点表) — every base UI viewpoint decided

大項目 = **Transaction Search** (single function). 中項目 = canonical viewpoint label.

| # | 観点 (中項目) | Applicable? | Evidence | Planned TC |
|---|---|---|---|---|
| U1 | 画面表示・レイアウト (Screen display / layout) | ✅ | §2 map MTSRCHA; captions 「Tran:」「Date:」「Pgm :」「Time:」「Card Num:」「From Amt:」「To Amt:」 + 5 result lines + key line | IT_TRANSRCH_HAPPY_001 |
| U2 | 初期値・デフォルト (Initial values / defaults) | ✅ | `8000-POPULATE-HEADER` L441-447; form blank on `LOW-VALUES`; prompt 「Enter card and amount range, press ENTER.」 L146; cursor on Card Num (`MOVE -1 TO CARDNUML` L147) | IT_TRANSRCH_HAPPY_002 |
| U3 | 表示条件・活性制御 (Display conditions / enable control) | ❌ — no conditional enable/disable in source: the 3 key fields stay `UNPROT` throughout (`SEND … DATAONLY` keeps map attributes), header + 5 result lines are always display-only. Design §2 item-states mark the input fields `□` on the result screen, but the source never protects them, so they remain enterable for refine-and-search (see Discrepancy 3) | source `SEND DATAONLY` L451-460; design §2 item states | — (fields-stay-enterable proven in HAPPY_011) |
| U4 | 文字・書式表示 (Characters / format display) | ✅ | amount edit mask `WS-ED-AMT -,---,---,--9.99`, `WS-ED-AMT(2:15)` L292-293; count edit `WS-CNT-ED` PIC `Z9` L204-206; field widths (Card 16, From/To 12, result line 70) | HAPPY_007 (separators) + HAPPY_001 (widths) + HAPPY_003 (count edit) |
| U5 | 入力チェック (Input validation) | ✅ | §3 + `6100-VALIDATE-CRIT` / `5100-PARSE-AMOUNT` | ABNORMAL_001–004, BOUNDARY_005–010 |
| U6 | 操作性・キー (Operability / function keys) | ✅ | `EIBAID` = ENTER / PF3 / PF4 / other (L161-171) | HAPPY_003 (ENTER), HAPPY_009 (PF3), HAPPY_010 (PF4), ABNORMAL_005 (other) |
| U7 | 状態遷移 (Screen / state transitions) | ✅ | `0000-MAIN` first-enter vs re-enter; `RETURN TRANSID('ORTS')` L481; stays on MTSRCHA after search; PF4 re-init; refine-and-search-again | HAPPY_002 (first entry), HAPPY_011 (refine), HAPPY_010 (PF4 clear) |
| U8 | 機能・業務フロー (Function / business flow) | ✅ | main browse+filter: match / no-match; From-blank lower bound; To-blank upper bound; both-blank all | HAPPY_003–006, HAPPY_008 |
| U9 | 計算・編集ロジック (Calculation / editing logic) | ❌ — no derived business value; the amount-string parser (comma/space-tolerant, ≤10 int / ≤2 frac digits) is input decoding covered under 入力チェック + 境界値, and the row count is a display counter under 文字・書式表示 | AST — `5100-PARSE-AMOUNT` decode + `Z9` edit, no business `COMPUTE` | — (covered by U5/U14/U4) |
| U10 | 出力・帳票 (Output / report) | ❌ — read-only; nothing written / printed; the "output" is the on-screen result lines | design §5 "no row created/updated/deleted" | — (covered by HAPPY_003) |
| U11 | データ整合性・冪等性 (Data integrity / idempotency) | ✅ (read-only) | repeated identical search returns the same rows; refine re-reads; no `WRITE`/`REWRITE`/`DELETE` | HAPPY_011 (refine), HAPPY_012 (idempotent re-run) |
| U12 | 排他・同時実行 (Exclusion / concurrent access) | ❌ — browse only (`STARTBR`/`READNEXT`/`ENDBR`), no `UPDATE`/lock | AST verbs L237-286 | — (runtime) |
| U13 | メッセージ・異常系 (Messages / abnormal) | ✅ | 6 **live** literal outcomes (M2–M6, M9); M8 「Error browsing…」 is **dead** (see Discrepancy 1) | ABNORMAL_001–005, HAPPY_008 (M6) |
| U14 | 境界値 (Boundary values) | ✅ | range inclusive both ends; result cap 5; parser 10/11 int digits, 2/3 frac digits, two dots; card exact-16 | BOUNDARY_001–010 |
| U15 | 回復・リラン (Recovery / rerun) | ❌ — pseudo-conversational, nothing committed to recover; re-press ENTER re-browses | AST (no restart/flag) | — (runtime) |
| U16 | 権限・セキュリティ (Authority / security) | ❌ — OCTSRCH has **no** signon / `CA-USER-TYPE` gate of its own; role enforced upstream (Sign On → menu) | KCOMM `CA-*`; design §1.2 roles | — |
| U17 | ログ・監査 (Log / audit) | ❌ — no log / audit record written | AST (no WRITE to a log) | — |
| U18 | 連携・インターフェース (Linkage / interface) | ✅ | `XCTL 'OCMENU'` L433 (PF3); `RETURN TRANSID('ORTS')` L481; browse `WS-TRANFILE` L237-286 — no other `CALL`/`LINK` | HAPPY_009 (XCTL), HAPPY_003 (browse), HAPPY_002 (RETURN) |
| U19 | 運用 (Operation) | ❌ — on-screen only; no paper/printer setup; performance is runtime | captions/env | — (perf runtime) |

CICS/BMS-specific instantiation applied: U6 = **AID keys** (ENTER/PF3/PF4/other, from `EIBAID`), U7/U18 = **CICS navigation** (`XCTL`, `RETURN TRANSID`).

---

## Coverage matrix — messages · variants · boundaries → TC

**On-screen messages** (literal strings sent to `ERRMSG`; source is ground truth — design §3 lists only M6=「Invalid key…」, the rest are recovered from the AST):

| # | Verbatim message | Type | Trigger (paragraph, line) | Status | TC |
|---|---|---|---|---|---|
| M1 | 「Enter card and amount range, press ENTER.」 (`WS-M-PROMPT`) | I (prompt) | `1000-SEND-INITIAL` L146 | live | HAPPY_001, HAPPY_002, HAPPY_010 |
| M2 | 「Card number is required.」 (`WS-M-CARD-REQ`) | E | `6100-VALIDATE-CRIT` L328 | live | ABNORMAL_001 |
| M3 | 「From amount is not a valid number.」 (`WS-M-FROM-BAD`) | E | `6100-VALIDATE-CRIT` L340 (From parse) | live | ABNORMAL_002; BOUNDARY_006/008/010 (accept/reject sides) |
| M4 | 「To amount is not a valid number.」 (`WS-M-TO-BAD`) | E | `6100-VALIDATE-CRIT` L352 (To parse) | live | ABNORMAL_003 |
| M5 | 「From amount cannot exceed to amount.」 (`WS-M-RANGE`) | E | `6100-VALIDATE-CRIT` L359 | live | ABNORMAL_004 |
| M6 | 「No transactions in that range.」 (`WS-M-NONE-FOUND`) | I/E | `2200-BUILD-SUMMARY` L202 (rows=0) | live | HAPPY_008, BOUNDARY_002, BOUNDARY_005, BOUNDARY_009 |
| M7 | 「 N match(es) displayed.」 (`WS-CNT-ED` Z9 + `WS-M-SUFFIX`) | C/I | `2200-BUILD-SUMMARY` L204-208 (rows>0) | live | HAPPY_003–007, HAPPY_011/012, BOUNDARY_001/003/004/007 |
| M8 | 「Error browsing the transaction file.」 (`WS-M-BROWSE-ERR`) | E | `3100-START-BROWSE` L252 / `3200-READ-NEXT` L277 (`RESP` OTHER) | **DEAD** | — (excluded, Discrepancy 1) |
| M9 | 「Invalid key pressed. Please try again.」 (`WS-MSG-INVALID-KEY`, WMSG) | E | `2000-PROCESS-INPUT` WHEN OTHER L170 | live | ABNORMAL_005 |

**Business-flow variants** (`2100-SEARCH`): match within range → result lines + M7 (HAPPY_003); no match (card/amount) → M6 (HAPPY_008); From blank → lower bound 0 (HAPPY_004); To blank → upper bound max (HAPPY_005); both blank → all card txns (HAPPY_006).

**Boundaries:** range inclusive at From==To exact value (BOUNDARY_001); values just outside both ends excluded (BOUNDARY_002); exactly 5 matches all shown (BOUNDARY_003); >5 matches capped at 5 (BOUNDARY_004); valid 10-digit amount accepted → runs to no-match (BOUNDARY_005); 11-digit amount → M3 (BOUNDARY_006); 2-decimal amount accepted (BOUNDARY_007); 3-decimal amount → M3 (BOUNDARY_008); valid 16-digit card matches but 15-digit key → no match (BOUNDARY_009); two decimal points → M3 (BOUNDARY_010).

**Totals:** **27 TC** — 12 Normal / 10 Boundary / 5 Abnormal. Live messages covered 8/8 (M1–M7, M9; M8 dead-excluded); flow variants 5/5; input fields 3/3 (Card required; From parse; To parse) × applicable check types; key AIDs 4/4 (ENTER/PF3/PF4/other).

---

## Discrepancies · to confirm (design vs source)

1. **Dead message — M8 「Error browsing the transaction file.」** `WS-M-BROWSE-ERR` is moved to `ERRMSGO` on a `STARTBR`/`READNEXT` `RESP` OTHER (L252/L277), but `2100-SEARCH` **always** runs `2200-BUILD-SUMMARY` (L193) *after* `3000-BROWSE-TRANS` (L192), and `2200` **unconditionally** overwrites `ERRMSGO` — with M6 when rows=0 (L202) or the count message otherwise (L204-208) — before the single `SEND` at L195. So the browse-error text can **never reach the screen**. Excluded with reason — no dedicated TC (never test dead code). A physical `TRANFILE` I/O failure surfaces to the operator as M6 「No transactions in that range.」, not M8.
2. **`design_evidence.py` listed OUANLIN / OUDATE / OUSTMIN as member program designs** — grouped by the "Transaction/Analytics/Statement" naming, **not** by a real call. OCTSRCH has **no** `CALL`/`LINK` to any of them (only `XCTL 'OCMENU'` and the `TRANFILE` browse). Those sub-program designs are therefore **not** part of OCTSRCH's test basis (U18 covers only the real XCTL / RETURN / browse).
3. **Design §2 item-states mark the input fields `□` (display-only) on the "Result displayed" screen**, but the source re-transmits with `SEND … DATAONLY` (L451-460) which does **not** protect the fields — they keep their `UNPROT` attribute, so the operator can refine the card/amount and search again (function overview point 3). Covered by HAPPY_011 (refine-and-search). Flag to the design owner if a read-only-after-result behaviour was intended.
4. **Design §3 lists only one message** (M9 「Invalid key pressed…」). The source actually emits **nine** distinct outcomes (M1–M9); one (M8) is dead. Per the skill rule "source is ground truth", the eight live messages are all covered; the design check table is under-specified.
5. **No card format/length validation** — the only card check is blank→M2 (L326). Any 16 characters become the exact-match key (`TR-CARD-NUM = WS-CARD-KEY`), so a mistyped/short card is not rejected; it simply matches nothing → M6 (BOUNDARY_009). Recorded as a source quirk.
6. **Amount parser tolerates spaces and commas** (`5100-PARSE-AMOUNT` skips them, L383-386): "`1,000.00`", "`1 000`" accept as the digits present (HAPPY_007). A credit/negative amount is **not reachable in results** — the parser only yields non-negative bounds, so `TR-AMT >= WS-FROM-AMT (≥0)` excludes negatives; the `WS-ED-AMT` floating-minus branch is therefore never exercised on this screen (no negative-display TC written).

---

## Audit result

`python3 scripts/audit_testcase.py cases.json --reg <REG> --root OCTSRCH` → candidates all grounded/dismissed:

- **`[valid] could not run msg_codes (no reg xml)`:** *dismissed — tool path-layout mismatch, not a coverage gap.* The audit's `_root_xml` looks under `parsed/cobol_xml/main|sub/`, but this reg stores the program at `parsed/cobol_xml/ORION-CCMS/cbl/OCTSRCH.xml`, so the automated code-coverage step self-skips. I ran `msg_codes.py` directly on that XML → **0 EI/EF codes** (literal-message app), so there is no coded-message gap regardless.
- **`[vague]` "異常系 case but expected cites no message code / STOP" (each Abnormal row):** *dismissed as a class.* OCTSRCH emits **literal English strings**, not EI/EF codes; the assertion IS the verbatim 「…」 message quoted in each 期待結果 (documented ORION-CCMS convention — no code exists to cite). The vagueness *literal* check passes — every such 期待結果 carries a 「…」 quote and concrete data.
- No `[ids]`, `[fields]`, `[fold]`, `[wording]`, `[parity]`, `[auto]` candidates expected: ids unique, all required fields present, no abnormal row folds ≥2 codes, no COBOL tokens in body fields (identifiers kept in 備考), automation columns all `×`.

Every candidate grounded/dismissed; judgment dims (each 期待結果 traces to a cited paragraph/line; steps reproducible; 観点表 complete vs the source) hand-reviewed. Spot-check re-verified: HAPPY_003 (match → M7 count + result lines, L192-208/291-317), BOUNDARY_004 (>5 matches capped at 5, `WS-MAX-ROWS=5` L224/270), ABNORMAL_004 (From>To → M5, L357-359).
