# ODCARDV — Card View (DB2) — Test-case derivation notes

- **App / transaction:** ODCARDV / CICS transaction **OD03**
- **Kind:** UI (CICS/BMS) — decided by EVIDENCE (`design_evidence.py` → `kind: ui`, a UI ScreenDesign doc exists; the reg has BMS map `MCARDV`).
- **AREA (ID token):** `CARDVIEW` (unique across the run's apps)
- **Deliverable language:** English (fixed by skill edition). Verbatim on-screen message strings, table/column identifiers and fixture VALUES reproduced unchanged.
- **Evidence read (only these):**
  - UI design: `design_asis/UI/ODCARDV_CardViewDB2_ScreenDesign.md`
  - Source AST (ground truth): `reg/parsed/cobol_xml/ORION-CCMS/cbl/ODCARDV.xml` (251 lines)
  - Copybooks: `RCARD` (card record PICs), `WMSG` (message literals), `KCOMM` (commarea), `WHEAD` (header title)
  - BMS map: `bms_maps.json` → map `MCARDVA` / field `CARDNUM` (length 16, UNPROT/IC/FSET)
  - DDL: `ddl/ORION.ddl` → `CREATE TABLE ORION.CARD`
  - Seed: `data/CARDFILE.txt` (5 rows, loaded to VSAM CARDFILE and DB2 `ORION.CARD`)

> **Non-interactive run:** the 観点表 (viewpoint table) below is recorded as an **approved assumption** and derivation continued without a live approval gate, per task instruction.

---

## 1. What the program actually does (AST-grounded)

Read-only card lookup, pseudo-conversational (RETURN TRANSID `OD03`), single input field.

- `0000-MAIN`: on cold start (`EIBCALEN = 0`) or when the commarea says first-enter → `1000-SEND-INITIAL`; otherwise → `2000-PROCESS-INPUT`; always `9000-RETURN`.
- `1000-SEND-INITIAL` (L52): clear map, populate header, set message line to **"Enter card number and press ENTER."**, SEND MAP ERASE, set context = re-enter.
- `2000-PROCESS-INPUT` (L65): `EVALUATE EIBAID` — **PF3**→`7000-XCTL-MENU` (to OCMENU); **PF4**→`1000-SEND-INITIAL` (clear); **ENTER**→`2100-READ-AND-SHOW`; **OTHER key**→re-init + **"Invalid key pressed. Please try again."** + SEND DATAONLY.
- `2100-READ-AND-SHOW` (L79): RECEIVE MAP; if Card Num blank (`SPACES`/`LOW-VALUES`) → **"Please enter all required fields."**; else read the card (`3000-READ-CARD`) → if found → `4000-POPULATE-DETAIL` + **"Card displayed."**; else → **"Record not found."**
- `3000-READ-CARD` (L105): `EXEC SQL SELECT CD_ACCT_ID, CD_EMBOSSED_NAME, CD_EXPIRY_DATE, CD_ACTIVE_STATUS INTO … FROM ORION.CARD WHERE CD_NUM = :CD-NUM`; `EVALUATE SQLCODE` — **0**→found; **100**→not-found; **OTHER**→not-found + set message **"Error reading card table."** *(see dead-code note)*.
- `4000-POPULATE-DETAIL` (L129): move Acct ID, Embossed Name, Expiry, Active Status to the display fields. **CD_CVV is neither selected nor displayed.**
- Header (`8000`/`8500`): Tran `OD03`, Pgm `ODCARDV`, Title `ORION CREDIT CARD MANAGEMENT SYSTEM`, current date/time from CICS `ASKTIME`/`FORMATTIME` (date separator `-`, time separator `:`).

### Messages (source of truth = AST, not design §3 ordering)

| # | Verbatim string | Source | Trigger | Active? |
|---|---|---|---|---|
| M1 | `Enter card number and press ENTER.` | inline L55 | initial screen (cold start / first-enter / PF4 clear) | ✅ active (I) |
| M2 | `Card displayed.` | inline L95 | ENTER, card found (SQLCODE 0) | ✅ active (I) |
| M3 | `Error reading card table.` | inline L126 | `3000-READ-CARD` SQLCODE OTHER | ❌ **DEAD** |
| M4 | `Invalid key pressed. Please try again.` | `WMSG` WS-MSG-INVALID-KEY | `2000` unsupported AID key | ✅ active (E) |
| M5 | `Please enter all required fields.` | `WMSG` WS-MSG-REQUIRED | `2100` Card Num blank | ✅ active (E) |
| M6 | `Record not found.` | `WMSG` WS-MSG-NOTFND | `2100` else (SQLCODE 100 **or** OTHER) | ✅ active (E) |

**Dead-code exclusion — M3 "Error reading card table.":** In `3000-READ-CARD` the SQLCODE-OTHER branch sets `REC-NOT-FOUND` **and** moves M3 to the message line (L125–126), but no SEND happens there. Control returns to `2100-READ-AND-SHOW`, where `REC-FOUND` is false → the ELSE branch overwrites the message line with **M6 "Record not found."** (L98) *before* the only SEND. So a real SQL error surfaces to the operator as **"Record not found."**, and M3 is never displayed. Identical pattern to ODACCTV's dead "Error reading account table." → **no TC written for M3; excluded with reason.** (Verified on AST: `msg_codes` finds 0 coded messages — all messages are literal English strings, so there is no code to cover.)

**`msg_codes(ODCARDV.xml)` = 0 codes / 0 STOP.** ORION uses literal on-screen message strings, not EI/EF/GF codes. Therefore every 異常系 `expected` quotes the **verbatim string** (the assertion), not a code — the audit's "異常系 case but expected cites no message code" candidates are expected and dismissed (see §4).

### Sub-program note (design_evidence false-positives)
`design_evidence.py` listed `OUANLIN`, `OUDATE`, `OUSTMIN` as member program designs, but **ODCARDV performs no CALL/LINK** to them (AST has only `XCTL OCMENU`, `RETURN`, the SQL SELECT and CICS SEND/RECEIVE/ASKTIME/FORMATTIME). Date/time comes from CICS `ASKTIME`/`FORMATTIME`, **not** from OUDATE. → No linkage TCs for those programs (same false-positive seen in ODACCTV/OCTRANV/OCSTMV).

---

## 2. Field & fixture facts (for concrete literals)

**Card record (`RCARD` copybook / `ORION.CARD` DDL):**

| Field | PIC / DDL | Displayed? | Screen field | Width |
|---|---|---|---|---|
| CD-NUM (card number, key) | `X(16)` / CHAR(16) | ✅ input + echo | CARDNUM (input) | 16 |
| CD-ACCT-ID (account id) | `9(11)` / DECIMAL(11,0) | ✅ | CDACCT | 11 |
| CD-CVV | `X(03)` / CHAR(3) | ❌ **not selected/shown** | — | — |
| CD-EMBOSSED-NAME (name) | `X(50)` / CHAR(50) | ✅ | CDNAME | 50 |
| CD-EXPIRY-DATE (expiry) | `X(10)` / CHAR(10) | ✅ | CDEXP | 10 |
| CD-ACTIVE-STATUS (status) | `X(01)` / CHAR(1) | ✅ | CDSTAT | 1 |

- `CARDNUM` BMS field: length 16, `UNPROT/IC/FSET` — **IC** = initial cursor (focus). No `picin` mask → **no numeric/format validation**; any 1–16 chars accepted, exact 16-char key match required to find a row.
- Only validation in code = **required/blank** (M5). No format/range/numeric check. Non-matching key (wrong format, short, or absent) → **M6 not found**.
- **No active-status gate:** `4000-POPULATE-DETAIL` displays whatever status is stored; an inactive (`N`) card is shown normally.

### Fixture **F-STD** — `ORION.CARD` (DB2), 5 rows (from `data/CARDFILE.txt`)

| CD_NUM | CD_ACCT_ID | CD_CVV | CD_EMBOSSED_NAME | CD_EXPIRY_DATE | CD_ACTIVE_STATUS |
|---|---|---|---|---|---|
| 4000000000000001 | 00000000001 | 123 | JOHN Q SMITH | 2027-03-31 | Y |
| 4000000000000002 | 00000000002 | 234 | MARIA A GARCIA | 2028-06-30 | Y |
| 4000000000000003 | 00000000003 | 345 | ROBERT B JOHNSON | 2026-01-31 | N |
| 4000000000000004 | 00000000004 | 456 | LINDA C WILLIAMS | 2029-11-30 | Y |
| 4000000000000005 | 00000000005 | 567 | DAVID D BROWN | 2030-05-31 | Y |

Operator is signed on (Sign On screen), at the Card View (DB2) screen (tran `OD03`) reached from the Main Menu (`OCMENU`). Each TC's 前提 says "F-STD" + only its delta.

---

## 3. 観点表 (viewpoint table) — every base viewpoint decided *(approved assumption)*

| 観点 (中項目) | Applicable? | Evidence (spec/AST) | Planned TC |
|---|---|---|---|
| 画面表示・レイアウト (Screen display / layout) | ✅ | §2.1 mockup + item detail; L52 `1000-SEND-INITIAL` | IT_CARDVIEW_HAPPY_001 |
| 初期値・デフォルト (Initial values / defaults) | ✅ | CARDNUM `IC` focus; detail fields display-only/blank on open (§2 item states) | IT_CARDVIEW_HAPPY_002 |
| 表示条件・活性制御 (Display conditions / enable control) | ❌ — only one input (CARDNUM); no conditional show/hide; detail always display-only | §2 item states | — (covered in HAPPY_001/002) |
| 文字・書式表示 (Characters / format display) | ✅ | header date/time separators `-`/`:` (L176); acct zero-pad 11-digit, expiry 10-char | IT_CARDVIEW_HAPPY_003 |
| 入力チェック (Input validation) | ✅ | only checks: required/blank (M5, L86); **no** format/numeric check (picin null) | UT_CARDVIEW_ABNORMAL_001, _002 |
| 操作性・キー (Operability / function keys) | ✅ | ENTER/PF3/PF4 (§1.2, `2000` EVALUATE EIBAID) | IT_CARDVIEW_HAPPY_007; PF3→_008; unsupported→ABNORMAL_004 |
| 状態遷移 (Screen / state transitions) | ✅ | PF3→OCMENU (XCTL); cold-start vs first-enter commarea → initial (L39–47) | IT_CARDVIEW_HAPPY_008, _009 |
| 機能・業務フロー (Function / business flow) | ✅ | lookup→display; classifier = active-status Y/N display variant | IT_CARDVIEW_HAPPY_004, _005, _006 |
| 計算・編集ロジック (Calculation / editing logic) | ❌ — straight field move, no computation/rounding | `4000-POPULATE-DETAIL` (MOVE only) | — |
| 出力・帳票 (Output / report) | ❌ — no report/print; the screen display IS the output | no WRITE/report in AST | — (covered by 機能) |
| データ整合性・冪等性 (Data integrity / idempotency) | ❌ — read-only (SELECT only); no C/U/D; re-lookup is naturally idempotent | §5 "only reads data"; AST no write verb | — |
| 排他・同時実行 (Exclusion / concurrent access) | ❌ — read-only SELECT, no lock/UPDATE | AST | — |
| メッセージ・異常系 (Messages / abnormal) | ✅ | M4/M5/M6 active; M3 dead (excluded) | UT_CARDVIEW_ABNORMAL_003, _004 (+ _001/_002) |
| 境界値 (Boundary values) | ✅ | CARDNUM `X(16)` length: 16 exact / 15 near / 1 min | UT_CARDVIEW_BOUNDARY_001, _002, _003 |
| 回復・リラン (Recovery / rerun) | ❌ — pseudo-conversational re-entry covered under 状態遷移/機能; no mid-flow commit to recover (read-only) | commarea context (L39–47) | — (covered in HAPPY_006/009) |
| 権限・セキュリティ (Authority / security) | ✅ | CVV never selected/shown (security-by-omission); no in-program role gate (CICS signon only) | IT_CARDVIEW_HAPPY_010 |
| ログ・監査 (Log / audit) | ❌ — no audit/log record written | AST | — |
| 連携・インターフェース (Linkage / interface) | ✅ (folded) | XCTL→OCMENU, RETURN TRANSID OD03; OUANLIN/OUDATE/OUSTMIN NOT called | covered in HAPPY_008/009 |
| 運用・性能 (Operation / performance) | ❌ — no operational caption / batch / perf artifact; runtime concern | — | — (deferred) |

---

## 4. Coverage matrix & audit resolution

**Message-code coverage:** M1 (HAPPY_001), M2 (HAPPY_004/005/006), M4 (ABNORMAL_004), M5 (ABNORMAL_001), M6 (ABNORMAL_003, also 002/BOUNDARY_002/003). **M3 excluded (dead)**. 5 of 6 messages covered; 1 excluded-with-reason. No coded (EI/EF) messages exist.

**Classifier coverage:** active-status `Y` (HAPPY_004) and `N` (HAPPY_005).

**Boundary coverage (CARDNUM X(16)):** 16 exact-match (BOUNDARY_001), 15 near-key below (BOUNDARY_002), 1 min (BOUNDARY_003).

**audit_testcase.py candidates — expected & how resolved:**
- *"異常系 case but expected cites no message code / STOP"* on every Abnormal/Boundary-not-found TC → **dismissed**: ORION uses literal on-screen strings, not EI/EF codes; the verbatim message string in `expected` IS the assertion (`msg_codes` = 0 codes confirms there is no code to cite).
- *"could not run msg_codes … skip code coverage"* (root xml under `.../cbl/`, not `main`/`sub`) → **dismissed**: no coded messages regardless; message coverage tracked in this matrix.
- Code identifiers (CD-NUM, CARDNUM, ORION.CARD, paragraph/line) live only in the **備考** column; body fields are business wording (no code-leak).

**Totals:** 17 TC — Normal 10 · Abnormal 4 · Boundary 3.
