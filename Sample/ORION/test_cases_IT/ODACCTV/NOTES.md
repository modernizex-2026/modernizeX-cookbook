# NOTES — Test cases for ODACCTV (Account View (DB2))

## Scope & classification

| Item | Value |
|---|---|
| App / root program | **ODACCTV** — Account View (DB2) |
| CICS transaction | **OD01** |
| BMS mapset / map | `MACCTV` / `MACCTVA` (24×80) |
| Kind (evidence) | **UI (CICS/BMS)** — a UI design doc exists; owns BMS map `MACCTVA` (`design_evidence.py` → `kind=ui`) |
| Backend | **DB2 relational** — `EXEC SQL SELECT … FROM ORION.ACCT` (read-only) |
| Deliverable language | **English** (fixed by skill edition) |
| AREA (ID scheme) | `ACCTVIEW` (unique vs ODACCTU=`ACCTUPD`) |
| Design doc | `UI/ODACCTV_AccountViewDB2_ScreenDesign.md` |
| Source of truth | `parsed/cobol_xml/ORION-CCMS/cbl/ODACCTV.xml` (AST) + copybooks `WMSG/RACCT/WCONST/WHEAD/KCOMM`, `bms/MACCTV.bms`, `ddl/ORION.ddl`, seed `data/ACCTFILE.txt` |

ODACCTV is the read-only sibling of **ODACCTU** (Account Update DB2). It looks up a
single account by its numeric key against the **DB2** `ORION.ACCT` table and shows
the record in display-only form. No row is created, updated or deleted.

## Source-grounded facts (AST)

**Flow** (`0000-MAIN` → pseudo-conversational):
- `EIBCALEN = 0` **or** `CA-FIRST-ENTER` (context 0) → `1000-SEND-INITIAL` (show empty
  screen + prompt, set `CA-PGM-CONTEXT = 1`).
- Re-entry (context 1) → `2000-PROCESS-INPUT`, `EVALUATE EIBAID`:
  - `DFHPF3` → `7000-XCTL-MENU` (XCTL to `OCMENU`, context reset 0).
  - `DFHPF4` → `1000-SEND-INITIAL` (clear, re-prompt).
  - `DFHENTER` → `2100-READ-AND-SHOW`.
  - OTHER key → `1000-SEND-INITIAL` then `MOVE WS-MSG-INVALID-KEY`, `8100-SEND-DATAONLY`.
- `2100-READ-AND-SHOW`: `RECEIVE MAP`; if `ACCTIDI = SPACES OR LOW-VALUES` → required
  message; else if `ACCTIDI IS NUMERIC` → `3000-READ-ACCT`; if `REC-FOUND` →
  `4000-POPULATE-DETAIL` + "Account displayed."; else → `WS-MSG-NOTFND`; else (not
  numeric) → "Account id must be numeric.".
- `3000-READ-ACCT`: `EXEC SQL SELECT … WHERE AC_ID = :AC-ID`; `EVALUATE SQLCODE`
  **0** → REC-FOUND · **100** → REC-NOT-FOUND · **OTHER** → REC-NOT-FOUND +
  `MOVE "Error reading account table."`.

**Input field** — `ACCTID` (BMS `POS=(06,17) LENGTH=11 ATTRB=(UNPROT,IC,FSET)`), no
`PICIN`, no justify. Symbolic `ACCTIDI` = `X(11)`; key `AC-ID` = `9(11)`; table
`AC_ID = DECIMAL(11,0)`. Validation is program-side `IF ACCTIDI IS NUMERIC` over the
**whole 11-char field** — a partial entry (`"5"` + trailing blanks/nulls) is NOT
numeric → "Account id must be numeric.". A successful lookup needs the full
**11-digit zero-padded** key (e.g. `00000000005`). No active-status gate — an inactive
account (status `N`) is still fully displayed.

**Display fields / edit** — `AC-CURR-BAL/AC-CREDIT-LIMIT/AC-CASH-LIMIT` are
`S9(10)V99`, edited through `WS-ED-BAL/WS-ED-AMT` PIC `-,---,---,--9.99` (floating
sign, comma grouping, 2 decimals) into 15-col fields; `AC-ACTIVE-STATUS` `X(01)` shown
raw (`Y`/`N`); dates `X(10)` (e.g. `2027-03-31`); `AC-GROUP-ID` `X(10)`; title
`WS-HDR-TITLE = "ORION CREDIT CARD MANAGEMENT SYSTEM"`; header date/time from
`EXEC CICS ASKTIME/FORMATTIME`.

## Message inventory (design §3 = 7; ground truth = AST)

All are **literal on-screen strings** — ORION-CCMS uses **no EI/EF message-code
scheme** (design NOTES §Assumptions #5). Type: I=info, E=error.

| # | Verbatim text | Type | Raised at | Alive? | Covered by |
|---|---|---|---|---|---|
| 1 | `Enter account id and press ENTER.` | I | `1000-SEND-INITIAL` L55 (first entry / PF4) | ✅ | HAPPY_003, HAPPY_010, HAPPY_012 |
| 2 | `Account displayed.` | I | `2100` REC-FOUND L96 | ✅ | HAPPY_006/007/008, BOUNDARY_001 |
| 3 | `Account id must be numeric.` | E | `2100` ELSE (not numeric) L103 | ✅ | ABNORMAL_003, BOUNDARY_002 |
| 4 | `Error reading account table.` | E | `3000` SQLCODE OTHER L137 | ❌ **DEAD** | *excluded* (see below) |
| 5 | `Invalid key pressed. Please try again.` (`WS-MSG-INVALID-KEY`) | E | `2000` OTHER key L75 | ✅ | ABNORMAL_001 |
| 6 | `Please enter all required fields.` (`WS-MSG-REQUIRED`) | E | `2100` blank L87 | ✅ | ABNORMAL_002 |
| 7 | `Record not found.` (`WS-MSG-NOTFND`) | E | `2100` NOT-FOUND L99 | ✅ | ABNORMAL_004, ABNORMAL_005, BOUNDARY_003 |

### Dead-code exclusion — MSG 4 "Error reading account table."

On `SQLCODE` OTHER, `3000-READ-ACCT` sets **both** `REC-NOT-FOUND` **and** moves
`"Error reading account table."` to `ERRMSGO`. Control returns to `2100`; because
`REC-FOUND` is false the `ELSE` at L98–100 runs **`MOVE WS-MSG-NOTFND TO ERRMSGO`**
then `8100-SEND-DATAONLY` — overwriting the error text before any SEND. Therefore
"Error reading account table." **can never reach the screen**; an SQL failure surfaces
to the operator as **`Record not found.`**. → **No dedicated TC for MSG 4** (never test
dead code). The SQLCODE-OTHER *trigger* (a real, distinct trigger of the NOTFND
message) is documented by **UT_ACCTVIEW_ABNORMAL_005**, which asserts the visible
outcome (`Record not found.`) and records that the read-error text is dead. Same
overwrite trap seen in OCTRANV / OCTSRCH.

## Fixture — F-STD (shared)

`ORION.ACCT` (DB2) holds the 5 seed accounts (from `data/ACCTFILE.txt`, mirrored into
the relational table); the operator is signed on (CICS) and has reached the Account
View (DB2) screen via transaction **OD01**.

| AC_ID | Status | Curr Bal | Credit Lim | Cash Lim | Open Date | Expiry | Group |
|---|---|---|---|---|---|---|---|
| 00000000001 | Y | 1,234.56 | 5,000.00 | 1,000.00 | 2019-03-15 | 2027-03-31 | GOLD |
| 00000000002 | Y | 2,500.75 | 10,000.00 | 2,000.00 | 2020-07-01 | 2028-06-30 | PLATINUM |
| 00000000003 | N | 0.00 | 3,000.00 | 500.00 | 2018-01-20 | 2026-01-31 | STANDARD |
| 00000000004 | Y | 15,000.00 | 25,000.00 | 5,000.00 | 2021-11-11 | 2029-11-30 | PLATINUM |
| 00000000005 | Y | 99.99 | 7,500.00 | 1,500.00 | 2022-05-05 | 2030-05-31 | GOLD |

Every seed account is exercised: 1 (HAPPY_006 + format), 2 (re-query + format), 3
(HAPPY_007 inactive), 4 (HAPPY_008 platinum/large), 5 (BOUNDARY_001 full-width).

## 観点表 (viewpoint table) — every base UI viewpoint decided

| # | 観点 (中項目) | Applicable? | Evidence | Planned TC |
|---|---|---|---|---|
| U1 | 画面表示・レイアウト | ✅ | §2.1 layout + `MACCTV.bms`; `1000-SEND-INITIAL` SEND MAP ERASE | HAPPY_001, HAPPY_002 |
| U2 | 初期値・デフォルト | ✅ | detail LOW-VALUES on open; MSG 1 prompt; header from ASKTIME/FORMATTIME | HAPPY_003 |
| U3 | 表示条件・活性制御 | ✅ | §2 item states — only `ACCTID` input-capable; all detail ASKIP/display-only | HAPPY_004 |
| U4 | 文字・書式表示 | ✅ | `WS-ED-BAL/-AMT` PIC `-,---,---,--9.99`; status `X(01)`; dates `X(10)` | HAPPY_005 |
| U5 | 入力チェック | ✅ | `2100` required-blank + `IS NUMERIC` on `X(11)` field | ABNORMAL_002, ABNORMAL_003, BOUNDARY_002 |
| U6 | 操作性・キー | ✅ | `EVALUATE EIBAID` — ENTER/PF3/PF4 + OTHER | HAPPY_009, HAPPY_010, ABNORMAL_001 (ENTER via happy flows) |
| U7 | 状態遷移 | ✅ | pseudo-conv `RETURN TRANSID(OD01)`; `EIBCALEN=0` cold start; PF3 XCTL `OCMENU` | HAPPY_011, HAPPY_012 |
| U8 | 機能・業務フロー | ✅ | §1.2 lookup+display; SQLCODE 0 happy; status/group content variants | HAPPY_006, HAPPY_007, HAPPY_008 |
| U9 | 計算・編集ロジック | ❌ — no arithmetic; only PIC edit-mask formatting (covered by U4) | AST `4000-POPULATE-DETAIL` = MOVEs only | — |
| U10 | 出力・帳票・メール | ❌ — read-only; no record written, no report, no mail | §5 CRUD = R only | — |
| U11 | データ整合性・冪等性 | ❌ — nothing committed; a read is inherently idempotent | §5 "no create/update/delete" | — |
| U12 | 排他・同時実行 | ❌ — read-only SELECT, no locks/updates | AST single `SELECT … INTO` | — |
| U13 | メッセージ・異常系 | ✅ | §3 messages; SQLCODE 100 + OTHER(masked) | ABNORMAL_001–005, BOUNDARY_002/003 |
| U14 | 境界値 | ✅ | `ACCTID` field width 11 + `IS NUMERIC`; numeric value min/lookup | BOUNDARY_001, BOUNDARY_002, BOUNDARY_003 |
| U15 | 回復・リラン | ❌ — no mid-flow commit to recover; re-entry always fresh prompt | pseudo-conv, read-only | — |
| U16 | 権限・セキュリティ | ❌ — no in-program authority gate; access via CICS signon + menu upstream | AST has no `CA-USER-TYPE` reference | — |
| U17 | ログ・監査 | ❌ — no audit/log write | AST no log I/O | — |
| U18 | 連携・インターフェース | ✅ (covered) — DB2 `SELECT ORION.ACCT` (U8); XCTL `OCMENU` (U6/U7 PF3) | AST `EXEC SQL` + `EXEC CICS XCTL` | HAPPY_006 (DB2 read), HAPPY_009 (XCTL) |
| U19 | 性能・運用 | ❌ — runtime/volume; not decidable from source | — | — |

**App-specific viewpoint — DB2 SQLCODE classifier (0/100/OTHER):** folded into U8
(SQLCODE 0 = happy), U13 (SQLCODE 100 = NOTFND; SQLCODE OTHER = masked NOTFND, dead
MSG 4). Explicitly captured by HAPPY_006/007/008 (0), ABNORMAL_004/BOUNDARY_003 (100),
ABNORMAL_005 (OTHER).

**Approval gate (non-interactive):** this 観点表 and the fixture are **recorded as an
assumption and taken as approved** (CLI run, no interactive reviewer) per the skill's
non-interactive rule.

## Coverage matrix (viewpoint → TC)

| 中項目 | Normal | Abnormal | Boundary | TCs |
|---|---|---|---|---|
| 画面表示・レイアウト | HAPPY_001, HAPPY_002 | — | — | 2 |
| 初期値・デフォルト | HAPPY_003 | — | — | 1 |
| 表示条件・活性制御 | HAPPY_004 | — | — | 1 |
| 文字・書式表示 | HAPPY_005 | — | — | 1 |
| 機能・業務フロー | HAPPY_006, HAPPY_007, HAPPY_008 | — | — | 3 |
| 操作性・キー | HAPPY_009, HAPPY_010 | ABNORMAL_001 | — | 3 |
| 状態遷移 | HAPPY_011, HAPPY_012 | — | — | 2 |
| 入力チェック | — | ABNORMAL_002, ABNORMAL_003 | — | 2 |
| メッセージ・異常系 | — | ABNORMAL_004, ABNORMAL_005 | — | 2 |
| 境界値 | — | — | BOUNDARY_001, BOUNDARY_002, BOUNDARY_003 | 3 |
| **Total** | **12** | **5** | **3** | **20** |

Messages: 6 of 7 alive codes covered by dedicated TCs (MSG 1 & 2 are info, asserted in
display/happy TCs); **1 dead (MSG 4) excluded with reason**. 0 uncovered / unexplained.

## Assumptions (recorded, taken as approved)

1. 観点表 + fixture above signed off (non-interactive CLI run).
2. The DB2 `ORION.ACCT` table is seeded to mirror `data/ACCTFILE.txt` (5 accounts,
   IDs 1–5) — the same seed used by the VSAM sibling apps.
3. ODACCTV has no in-program signon/authority check; access control is upstream
   (CICS signon + menu), so U16 is excluded, not tested here.
4. MSG 4 "Error reading account table." is dead (overwritten by NOTFND before SEND)
   and is excluded; the SQLCODE-OTHER trigger is documented via ABNORMAL_005.
5. `auto = ×`, `automation_id` empty for every TC — AS-IS CICS screen, no automation
   harness.

## Audit result

`audit_testcase.py cases.json --reg <REG> --root ODACCTV` →
**2 candidate classes, both dismissed** (see below); all judgment dims hand-checked.

- `[valid] could not run msg_codes …` — **dismissed**: the AST lives at
  `parsed/cobol_xml/ORION-CCMS/cbl/ODACCTV.xml` (not the `main/`|`sub/` layout the
  script probes) **and** ORION-CCMS has no EI/EF code scheme, so there is nothing to
  match. Message coverage is proven against the literal-string inventory above.
- `[vague] … 異常系 case but expected cites no message code / STOP` (per Abnormal
  case) — **dismissed as a class**: this system uses **literal on-screen message
  text**, not `EI/EF/GF` codes. Each 異常系 expected quotes the verbatim string in
  「…」 (the assertion); the code-citation heuristic is N/A for ORION-CCMS.

No duplicate ids, no empty fields, no ad-hoc 中項目, no folding, no wording code-leak,
no invented codes. Spot-check of HAPPY_006 / ABNORMAL_003 / BOUNDARY_002 against the
cited AST lines passed.
