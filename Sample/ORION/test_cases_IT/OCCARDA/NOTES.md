# NOTES — OCCARDA (Card Add) test-case set

- **App / root:** `OCCARDA` — Card Add (app_010), CICS transaction **OROD**, mapset `MCARDA` / map `MCARDAA`.
- **Kind (by EVIDENCE, not root_type):** **UI (CICS/BMS)** — a UI design doc exists and the program owns BMS map `MCARDAA` (`design_evidence.py` → `kind: ui`). Viewpoints use the UI catalog A–E with BMS instantiation (AID keys, map fields, CICS navigation).
- **REG (ground truth, read-only):** `/Users/thangnguyen/Library/Caches/.skr/run-57032/reg`
- **Design suite (preferred evidence, read-only):** `…/output/10/ORION/design_asis/UI/OCCARDA_CardAdd_ScreenDesign.md`
- **Source (read-only, header/data only):** `…/input/10/ORION/ORION-CCMS/cbl/OCCARDA.cbl` (+ copybooks `WMSG/WCONST/WHEAD/KCOMM/KDATE/RCARD/RACCT/RXREF/MCARDA`), sub-program `OUDATE.cbl`.
- **OUT:** `…/analysis_output/datastore/design_runs/test_cases/OCCARDA`
- **Deliverable language:** English (fixed by skill edition). **Author:** modernizeX. **Date:** 2026/09/28.
- **Approval gate (NON-INTERACTIVE):** the viewpoint table below is **recorded as an approved assumption** and the set was built without pausing, per the run instruction.

## Grounding note — design doc is thin, source is ground truth

The design doc §3 check table lists **one** message ("Invalid key pressed…"). The REG is ground truth (skill rule), and the source `OCCARDA.cbl` carries a **full field-validation catalog** with 12 program message literals + 1 abend text. `msg_codes.py OCCARDA.xml` returns **0 codes** — ORION-CCMS screens raise **literal English strings** on the `ERRMSG` line, not EI/EF/GF codes (project convention). Every message + paragraph below was confirmed **active on the AST** (`parsed/cobol_xml/ORION-CCMS/cbl/OCCARDA.xml`: 24 paragraphs, all 13 literals present, none commented-out). No dead code found — nothing excluded on that basis.

### Message catalog (literal → trigger paragraph) — the abnormal assertions

| # | Message (verbatim, on `ERRMSG`) | Type | Raised at | Copybook item |
|---|---|---|---|---|
| 1 | `Enter new card details and press ENTER.` | I | 1000-SEND-INITIAL (open / PF4 / after success) | WS-M-PROMPT |
| 2 | `Invalid key pressed. Please try again.` | E | 2000-PROCESS-INPUT WHEN OTHER (unsupported AID) | WS-MSG-INVALID-KEY (WMSG) |
| 3 | `Card number must be sixteen digits.` | E | 5010-VAL-CARD (parse≠16 digits / non-numeric) | WS-M-CARD-REQ |
| 4 | `Account id must be numeric.` | E | 5020-VAL-ACCT (blank / non-numeric / >11 digits) | WS-M-ACCT-NUM |
| 5 | `Embossed name is required.` | E | 5030-VAL-NAME (spaces/low-values) | WS-M-NAME-REQ |
| 6 | `CVV must be three numeric digits.` | E | 5040-VAL-CVV (parse≠3 digits / non-numeric) | WS-M-CVV-NUM |
| 7 | `Expiry date invalid, use YYYY-MM-DD.` | E | 5050-VAL-EXPIRY (OUDATE `VALD` KD-STATUS≠`00`) | WS-M-EXP-BAD |
| 8 | `Account does not exist.` | E | 5060-VAL-ACCT-EXISTS (ACCTFILE READ NOTFND) | WS-M-ACCT-NF |
| 9 | `Card number already exists.` | E | 5070-VAL-CARD-UNIQUE (CARDFILE READ found) **and** 3500-WRITE-CARD DUPREC | WS-M-CARD-DUP |
| 10 | `Error writing the card file.` | E | 3500-WRITE-CARD WHEN OTHER (write RESP not NORMAL/DUPREC) | WS-M-WRITE-ERR |
| 11 | `Card written; cross-ref write failed.` | E | 3700-WRITE-XREF WHEN OTHER (xref write RESP not NORMAL/DUPREC) | WS-M-XREF-ERR |
| 12 | `Card issued successfully.` | C | 2100-ADD-CARD after CARDFILE+XREF written | WS-M-OK |
| 13 | `OCCARDA: unrecoverable file error. Contact support.` | E (ABEND) | 9500-ABEND-RTN (3000/3100 READ RESP not NORMAL/NOTFND) → SEND TEXT + RETURN (task ends) | literal |

### Validation order (5000-VALIDATE-ALL — first fault stops the chain)
`5010 card(16 num) → 5020 acct(num,≤11) → 5030 name(required) → 5040 cvv(3 num) → 5050 expiry(OUDATE) → 5060 acct-exists → 5070 card-unique`. On any fail: `WS-INVALID`, message set, cursor to Card Num, SEND DATAONLY (screen kept, no write). On clean: 3500 write card → (ok) 3700 write xref → fresh screen + success.

### OUDATE `VALD` rules (expiry) — grounds the boundary cases
`OUDATE.cbl` 2000-VALIDATE: positions 1:4/6:2/9:2 must be numeric (separators at 5,8 **not** checked); month 1–12; day 1–DIM where DIM = 31 default, 30 for Apr/Jun/Sep/Nov, **29 for Feb (no leap-year test)**. Status `00`=valid, `99`=invalid.

## Fixture — F-STD (real seed data)

Real seed lives in `…/input/10/ORION/ORION-CCMS/data/*.txt` (fixed-width per the `R*` copybooks); F-STD is that seed loaded into the CICS VSAM files:

- **ACCTFILE (Account master):** ids `00000000001`(Y), `00000000002`(Y), `00000000003`(**N/inactive**), `00000000004`(Y), `00000000005`(Y).
- **CARDFILE (Card master):** existing cards `4000000000000001`…`4000000000000005` (each linked to the same-suffix account, active status per seed).
- **XREFFILE (Card cross-reference):** rows keyed by card number `4000000000000001`→acct `00000000001`→cust `000000001`, … `…0005`→`000000005`.
- **Derived probes:** account **not on file** = `00000000099`; **unused** card number for the happy add = `4000000000000010`; owning account for the add = `00000000001`.
- CICS region up, mapset `MCARDA` installed, transaction `OROD` defined, operator signed on (authority enforced upstream — see U16).

Each TC's 前提条件 states "F-STD" + only its delta.

> Cross-reference quirk (grounds U10/U11): 3200-READ-STUB reads XREFFILE using the **account id** as the 16-byte key (`WS-XREF-KEY 9(16)` → `XR-CARD-NUM X(16)`), but XREFFILE is keyed by **card number**, so the stub read normally misses and `WS-NEW-CUST` defaults to `000000000`. The new XREF row is therefore written with customer id `000000000` unless a card-keyed stub happens to match the account key. "Absence is not fatal" (source comment).

## Viewpoint table (観点表) — every base UI viewpoint decided (recorded as approved)

| # | 観点 (中項目) | Applicable? | Evidence | Planned TC |
|---|---|---|---|---|
| U1 | 画面表示・レイアウト (Screen display / layout) | ✅ | §2.1 map MCARDAA (24×80), captions Card Num/Acct ID/Name/CVV/Expiry, key legend | HAPPY_001 |
| U2 | 初期値・デフォルト (Initial values / defaults) | ✅ | 1000-SEND-INITIAL: fields cleared, prompt msg, cursor→CARDNUM (`MOVE -1 TO CARDNUML`) | HAPPY_002 |
| U3 | 表示条件・活性制御 (Display conditions / enable control) | ✅ (single add mode) | §2 item-states: 5 fields UNPROT/enterable, header+ERRMSG display-only; no conditional hide/show | HAPPY_003 |
| U4 | 文字・書式表示 (Characters / format display) | ✅ | §2.1 widths (16/11/50/3/10) & colours (green input, blue caption, red ERRMSG, yellow title) | HAPPY_004 |
| U5 | 入力チェック (Input validation) | ✅ | 5010–5070 per-field checks (7 messages) | ABNORMAL_001–007 |
| U6 | 操作性・キー (Operability / function keys) | ✅ | 2000-PROCESS-INPUT: ENTER / PF3 / PF4 / OTHER (AID keys) | HAPPY_005/006, ABNORMAL_008 |
| U7 | 状態遷移 (Screen / state transitions) | ✅ | EIBCALEN=0 cold start; CA-FIRST-ENTER; success re-display; PF3 XCTL→OCMENU; RETURN TRANSID(OROD) | HAPPY_007/008 |
| U8 | 機能・業務フロー (Function / business flow) | ✅ | §1.2 add flow; variants: inactive account, second card on an account | HAPPY_009/010/011 |
| U9 | 計算・編集ロジック (Calculation / editing logic) | ✅ | 6000-PARSE-NUM (numeric parse, blanks ignored, zero-extension); CD-ACTIVE-STATUS='Y' | HAPPY_012, BOUNDARY_002 |
| U10 | 出力・帳票 (Output / report) | ✅ | §5 CRUD: CARDFILE(C) + XREFFILE(C) column-level write | HAPPY_013/014 |
| U11 | データ整合性・冪等性 (Data integrity / idempotency) | ✅ | re-add dup rejected; partial write (card ok, xref fail) | HAPPY_015, ABNORMAL_010 |
| U12 | 排他・同時実行 (Exclusion / concurrent access) | ❌ N/A (runtime) | VSAM record lock at WRITE; concurrent same-card add → DUPREC → covered functionally by write DUPREC path (msg #9/#10). No in-program lock logic to unit-test | — (excluded, reason) |
| U13 | メッセージ・異常系 (Messages / abnormal) | ✅ | invalid key + system/file faults (write error, xref fail, unrecoverable abend) | ABNORMAL_008/009/010/011 |
| U14 | 境界値 (Boundary values) | ✅ | card 15/16; cvv 2/3; acct 1-digit/11-digit; name 50; expiry month/day/Feb-29 (OUDATE) | BOUNDARY_001–008 |
| U15 | 回復・リラン (Recovery / rerun) | ✅ (idempotence only) | re-run of the same add is blocked by the unique-card check → no duplicate (covered in U11) | HAPPY_015 (idempotence); mid-flow restart is runtime |
| U16 | 権限・セキュリティ (Authority / security) | ❌ | OCCARDA has **no** in-program authority check (no `CA-USER-TYPE`/88 reference); signon+role enforced upstream by OCSGNON/OCMENU | — (excluded, reason) |
| U17 | ログ・監査 (Log / audit) | ❌ | no audit/log file written by OCCARDA (only CARDFILE/XREFFILE data writes) | — (excluded, reason) |
| U18 | 連携・インターフェース (Linkage / interface) | ✅ | `CALL 'OUDATE'` (date-utility subroutine, expiry `VALD`); PF3 XCTL→OCMENU | HAPPY_016 (OUDATE), HAPPY_005 (PF3) |
| U19 | 性能・運用 (Operation) | ❌ N/A (runtime) | online single-record entry; no paper/printer/volume instruction | — (excluded, reason) |

## Coverage matrix (viewpoint → TC)

| Viewpoint | TC ids | # |
|---|---|---|
| U1 画面表示 | IT_CARDADD_HAPPY_001 | 1 |
| U2 初期値 | IT_CARDADD_HAPPY_002 | 1 |
| U3 表示条件・活性 | IT_CARDADD_HAPPY_003 | 1 |
| U4 文字・書式 | IT_CARDADD_HAPPY_004 | 1 |
| U6 操作性・キー | IT_CARDADD_HAPPY_005 (PF3), IT_CARDADD_HAPPY_006 (PF4), UT_CARDADD_ABNORMAL_008 (invalid key) | 3 |
| U7 状態遷移 | IT_CARDADD_HAPPY_007 (cold start), IT_CARDADD_HAPPY_008 (success re-display) | 2 |
| U8 機能・業務フロー | IT_CARDADD_HAPPY_009 (main add), 010 (inactive acct), 011 (2nd card) | 3 |
| U9 計算・編集 | IT_CARDADD_HAPPY_012 (active='Y'/name), UT_CARDADD_BOUNDARY_002 (acct zero-extension) | 2 |
| U10 出力 | IT_CARDADD_HAPPY_013 (CARDFILE cols), 014 (XREFFILE cols) | 2 |
| U11 データ整合性・冪等 | IT_CARDADD_HAPPY_015 (idempotent re-add), UT_CARDADD_ABNORMAL_010 (partial write) | 2 |
| U18 連携 | IT_CARDADD_HAPPY_016 (OUDATE expiry), + PF3 in HAPPY_005 | 1 |
| U5 入力チェック | UT_CARDADD_ABNORMAL_001 (card non-num), 002 (acct non-num), 003 (name blank), 004 (cvv non-num), 005 (expiry bad), 006 (acct not found), 007 (card dup) | 7 |
| U13 メッセージ・異常系 | UT_CARDADD_ABNORMAL_008 (invalid key), 009 (write error), 010 (xref fail), 011 (unrecoverable abend) | 4 |
| U14 境界値 | UT_CARDADD_BOUNDARY_001 (card 15), 002 (acct 1-digit), 003 (acct 11-digit), 004 (cvv 2), 005 (name 50), 006 (expiry month 13), 007 (expiry day 32 / Apr-31), 008 (Feb-29 no-leap) | 8 |

**Totals: 16 Normal + 11 Abnormal + 8 Boundary = 35 TC.**
Messages covered: 12 of 13 program literals have a dedicated TC (msg #1 prompt in HAPPY_001/002/006; #12 success in HAPPY_009 etc.); message #9 (`Card number already exists.`) covered at its validation trigger (ABNORMAL_007) with its second trigger (write DUPREC race) noted in 備考 + U12. 0 excluded on grounding; 4 viewpoints N/A with reason (U12/U16/U17/U19).

## Audit result

`audit_testcase.py cases.json --reg <REG> --root OCCARDA` → **11 candidates, all grounded and dismissed** (the two classes expected for every ORION-CCMS app — no avoidable findings). Structural checks pass: 35 unique ids, no empty fields, 14 viewpoints all from the 観点表, no wording/fold/parity/automation flags.

1. **`[valid] could not run msg_codes` — DISMISSED.** `audit_testcase._root_xml` only looks under `parsed/cobol_xml/main|sub/`, but this reg stores XML under `parsed/cobol_xml/ORION-CCMS/cbl/`. Ran `msg_codes.py OCCARDA.xml` directly → **0 codes / 0 raises**. ORION-CCMS screens raise literal English strings, not EI/EF/GF codes, so there is no code-coverage gap.
2. **`[vague] 異常系 case but expected cites no message code / STOP` ×10 (ABNORMAL_001–010) — DISMISSED.** The check wants an `EI###`/`STOP`/`abort`/`ABEND` token in every Abnormal expected. These programs have no message codes; the assertion **is** the verbatim on-screen string the operator sees (`Card number must be sixteen digits.`, `Account does not exist.`, `Card number already exists.`, `Invalid key pressed. Please try again.`, `Error writing the card file.`, `Card written; cross-ref write failed.`, etc.), each quoted in the expected and traced to its raise paragraph in 備考. ABNORMAL_011 is not flagged (its expected names the `ABEND` path for the unrecoverable-file-error `SEND TEXT`).

**Wording-regex trap handled:** the verbatim expiry message contains the format token `YYYY-MM-DD`, which the audit's DATANAME regex misreads as a COBOL data-name. The case bodies describe the expiry format as "ISO date such as `2030-12-31`" and keep the verbatim string `Expiry date invalid, use YYYY-MM-DD.` in the 備考 column only (not scanned) — so no `[wording]` flag fires.

**Spot-check (3 random TCs re-verified against `OCCARDA.cbl`):**
- IT_CARDADD_HAPPY_009 → 5000-VALIDATE-ALL clean → 3500-WRITE-CARD + 3700-WRITE-XREF → WS-M-OK `Card issued successfully.` ✓
- UT_CARDADD_ABNORMAL_007 → 5070-VAL-CARD-UNIQUE REC-FOUND → WS-M-CARD-DUP `Card number already exists.` ✓
- UT_CARDADD_BOUNDARY_008 → OUDATE 2000-VALIDATE `WHEN 2 MOVE 29 TO WS-DIM` (no leap-year branch) → `2027-02-29` KD-STATUS `00` accepted ✓
