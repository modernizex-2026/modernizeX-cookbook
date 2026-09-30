# OCTRANV — Transaction View · Test-case derivation notes

- **System / app**: ORION-CCMS · **OCTRANV** — Transaction View (CICS/BMS on-line, pseudo-conversational)
- **CICS transaction**: `ORTV` · **Mapset/Map**: `MTRANV` / `MTRANVA` (24×80)
- **App kind**: **UI (CICS/BMS)** — `design_evidence.py` reports `kind=ui` (a UI document exists; the reg owns BMS map MTRANVA).
- **AREA (ID scheme)**: `TRANVIEW` (unique across the ORION run — `TRANINQ` = OCTRNIN, `STMTVIEW` = OCSTMV are different apps).
- **Evidence read**: `UI/OCTRANV_TransactionView_ScreenDesign.md`; source `ORION-CCMS/cbl/OCTRANV.cbl`; map `ORION-CCMS/bms/MTRANV.bms`; copybooks `RTRAN, WMSG, WCONST, WHEAD, KCOMM`; AST `parsed/cobol_xml/ORION-CCMS/cbl/OCTRANV.xml`.
- **Author**: modernizeX · **Language**: English deliverable.

## Approval gate (NON-INTERACTIVE)

The viewpoint table below was **auto-approved and recorded as an assumption** per the
non-interactive run directive. Rationale for every decision is in the Evidence column.

## Source-is-ground-truth reconciliation (design vs code)

The design doc's **§3 check spec lists only 1 message** ("Invalid key pressed…"), but the
**source defines 6 operator messages** (5 local `WS-M-*` + the shared `WS-MSG-INVALID-KEY`).
Per the ORION-CCMS convention (literal-English messages, no EI/EF codes; `msg_codes.py`
returns 0 codes) **the source is the ground truth** and all *live* messages are covered.

| # | Message text (verbatim, from source) | Field | Type | Trigger (paragraph) | Status |
|---|---|---|---|---|---|
| M1 | `Enter a transaction id and press ENTER.` | WS-M-PROMPT | I | initial / PF4 clear (1000-SEND-INITIAL) | **live** → covered in normal display |
| M2 | `Transaction details displayed.` | WS-M-TRAN-FOUND | C | record found (2100-READ-AND-SHOW) | **live** → covered in happy lookup |
| M3 | `Transaction not found - check the id.` | WS-M-TRAN-NOTFND | E | key not on file (2100) | **live** → dedicated abnormal TC |
| M4 | `Transaction id is required.` | WS-M-ID-REQUIRED | E | blank / spaces key (2100) | **live** → dedicated abnormal TC |
| M5 | `Invalid key pressed. Please try again.` | WS-MSG-INVALID-KEY | E | unsupported AID key (2000-PROCESS-INPUT) | **live** → dedicated abnormal TC |
| M6 | `Error reading the transaction file.` | WS-M-READ-ERROR | E | READ resp ≠ NORMAL/NOTFND (3000-READ-TRAN) | **DEAD — excluded (reason below)** |

**M6 is dead code — excluded, no TC.** `3000-READ-TRAN` moves `WS-M-READ-ERROR` to `ERRMSGO`
on a non-NORMAL/non-NOTFND response, but it *also* sets `REC-NOT-FOUND`; control returns to
`2100-READ-AND-SHOW` whose `EVALUATE … WHEN OTHER` branch **overwrites `ERRMSGO` with
`WS-M-TRAN-NOTFND`** before the SEND. So M6 can never reach a screen. (Skill rule: never
write a TC for dead code — exclude with reason.)

## Fixture — `F-STD` (shared; each TC states only its delta)

No customer **TRANFILE** seed exists (only ACCT/CARD/CUST/XREF/CTRL/TCAT/TTYP/DGRP/USRSEC are
seeded). F-STD records are **constructed to the `RTRAN` copybook layout** (illustrative 16-char
`TR-ID` keys and values); card numbers reuse the real `CARDFILE` seed and type codes the real
`TTYPFILE` seed.

- Operator is **already signed on** (Sign On screen passed) and reaches OCTRANV via transaction
  **`ORTV`** from the main menu (OCMENU). Transaction file **TRANFILE** (VSAM KSDS, key = `TR-ID`
  X(16)) is online and contains:

| TR-ID (X16, key) | Type (X2) | Card num (X16) | Amount `S9(9)V99` | Merchant (X50) | Description (X100) |
|---|---|---|---|---|---|
| `TXN0000000000001` | `01` PURCHASE | `4000000000000001` | `1234.56` | `ACME RETAIL STORE` | `Grocery purchase downtown` (25) |
| `TXN0000000000002` | `02` PAYMENT | `4000000000000002` | `-500.00` | `ONLINE PAYMENT CENTER` | `Monthly card payment received` (29) |
| `TXN0000000000003` | `03` CASH ADVANCE | `4000000000000003` | `0.00` | `ATM NETWORK LLC` | `Balance adjustment` (18) |
| `TXN0000000000004` | `01` PURCHASE | `4000000000000004` | `999999999.99` | `GLOBAL MEGASTORE CHAIN` | `AAAA… 100 chars exactly …ZZZZ` (100) |

Field formats (RTRAN + WCONST/WHEAD): `TR-ID` X(16) alphanumeric key · `TR-TYPE-CD` X(2) (shown
verbatim, no TTYPFILE lookup) · `TR-CARD-NUM` X(16) · `TR-AMT` S9(9)V99 shown through
`WS-ED-AMT PIC -,---,---,--9.99`, then `(2:15)` drops the always-blank leading pad byte into the
15-col `TRAMT` field · `TR-MERCHANT-NAME` X(50) exact fit · `TR-DESC` **X(100) truncated to the
50-col `TRDESC`** field · header date `YYYY-MM-DD` / time `HH:MM:SS` from `EXEC CICS ASKTIME/FORMATTIME`.

## Viewpoint table (観点表) — every base UI viewpoint decided

| 観点 (中項目) | Applicable? | Evidence (spec / AST) | Planned TC |
|---|---|---|---|
| 画面表示・レイアウト (Screen display / layout) | ✅ | §2.1 mockup + MTRANV.bms captions/legend; 1000-SEND-INITIAL | IT…001, IT…002 |
| 初期値・デフォルト (Initial values / defaults) | ✅ | prompt M1, IC cursor on TRANID, empty detail, header date/time | IT…003 |
| 表示条件・活性制御 (Display conditions / enable control) | ✅ | §2 item-states: TRANID `UNPROT/IC` only; all detail `ASKIP` (display-only) | IT…004 |
| 文字・書式表示 (Characters / format display) | ✅ | date/time format; type shown as 2-char code; desc X(100)→50 truncation | IT…005, IT…006, UT_B…001 |
| 入力チェック (Input validation) | ✅ | 2100 required-check (M4); read/not-found (M3) — the single input field TRANID | UT_A…001, UT_A…002 |
| 操作性・キー (Operability / function keys) | ✅ | §1.2 keys + 2000-PROCESS-INPUT EIBAID: ENTER/PF3/PF4/other | IT…008, IT…009 (ENTER→IT…012; other→UT_A…003) |
| 状態遷移 (Screen / state transitions) | ✅ | 0000-MAIN dispatch (EIBCALEN=0 / CA-FIRST-ENTER); record→record re-query | IT…010, IT…011 |
| 機能・業務フロー (Function / business flow) | ✅ | §1.2 lookup+view; one content variant (payment/negative) — no per-type branching | IT…012, IT…013 |
| 計算・編集ロジック (Calculation / editing logic) | ✅ | 4000-POPULATE-DETAIL amount edit (comma / sign / zero / max) | IT…007, UT_B…002/003/004 |
| 出力・帳票 (Output / report) | ❌ N/A | §5 "Not applicable — read only"; source has no WRITE/REWRITE/DELETE | — |
| データ整合性・冪等性 (Data integrity / idempotency) | ✅ (light) | read-only; re-read same key → identical, no state change | IT…014 |
| 排他・同時実行 (Exclusion / concurrent access) | ❌ N/A | `EXEC CICS READ` (no UPDATE/lock); read-only, no concurrency effect | — |
| メッセージ・異常系 (Messages / abnormal) | ✅ | one TC per **live** message (M3/M4/M5); M6 dead → excluded | UT_A…001/002/003 |
| 境界値 (Boundary values) | ✅ | TRANID len 1/16/blank (BMS LEN=16); amount 0/max/neg; desc 50/100 | UT_B…001–006 |
| 回復・リラン (Recovery / rerun) | ❌ N/A | pseudo-conversational read-only; no mid-flow commit; re-entry = re-prompt (PF4) | — |
| 権限・セキュリティ (Authority / security) | ❌ N/A | OCTRANV performs **no** role/auth check; signon is upstream (Sign On); CA-USER-TYPE not gated here | — |
| ログ・監査 (Log / audit) | ❌ N/A | no audit/log record written | — |
| 連携・インターフェース (Linkage / interface) | ✅ (covered) | PF3 `XCTL OCMENU`; pseudo-conv `RETURN TRANSID(ORTV)`; reads TRANFILE — covered by IT…008 + IT…012 | (IT…008, IT…012) |
| 運用 (Operation) | ✅ (covered) | key legend line always shown — covered in IT…001 | (IT…001) |

## Coverage matrix (viewpoint → TC)

- **画面表示**: IT_TRANVIEW_HAPPY_001 (initial), IT_TRANVIEW_HAPPY_002 (detail render)
- **初期値**: IT_TRANVIEW_HAPPY_003
- **活性制御**: IT_TRANVIEW_HAPPY_004
- **文字・書式**: IT_TRANVIEW_HAPPY_005 (date/time), IT_TRANVIEW_HAPPY_006 (2-char type), UT_TRANVIEW_BOUNDARY_001 (desc truncation)
- **計算・編集**: IT_TRANVIEW_HAPPY_007 (comma), UT_TRANVIEW_BOUNDARY_002 (zero), UT_TRANVIEW_BOUNDARY_003 (max), UT_TRANVIEW_BOUNDARY_004 (negative sign)
- **操作性・キー**: IT_TRANVIEW_HAPPY_008 (PF3), IT_TRANVIEW_HAPPY_009 (PF4); ENTER→IT_TRANVIEW_HAPPY_012; other→UT_TRANVIEW_ABNORMAL_003
- **状態遷移**: IT_TRANVIEW_HAPPY_010 (first-entry dispatch), IT_TRANVIEW_HAPPY_011 (re-query)
- **機能・業務フロー**: IT_TRANVIEW_HAPPY_012 (main lookup), IT_TRANVIEW_HAPPY_013 (payment variant)
- **データ整合性・冪等**: IT_TRANVIEW_HAPPY_014
- **入力チェック / メッセージ異常系**: UT_TRANVIEW_ABNORMAL_001 (id required M4), UT_TRANVIEW_ABNORMAL_002 (not found M3), UT_TRANVIEW_ABNORMAL_003 (invalid key M5)
- **境界値**: UT_TRANVIEW_BOUNDARY_001..006 (desc-trunc, amount 0/max/neg, TRANID 16-max, TRANID 1-min→not-found)

**Message coverage: 5 live / 5 covered · 1 dead (M6) excluded with reason · 0 outstanding.**
**Keys: ENTER, PF3, PF4, invalid — all covered.**

## Total: 23 TC — Normal (IT) 14 · Abnormal (UT_ABNORMAL) 3 · Boundary (UT_BOUNDARY) 6

## Audit result

`audit_testcase.py --reg <REG> --root OCTRANV` → **4 candidates, all grounded and dismissed** (0 outstanding):

1. `[valid] could not run msg_codes … skip code coverage` — the audit resolves the root xml only
   under `parsed/cobol_xml/{main,sub}/`, but this reg stores it under `ORION-CCMS/cbl/`. Ran
   `msg_codes.py` **directly** on the real xml → **0 codes** (ORION-CCMS uses literal-English
   messages, not EI/EF codes). Code-coverage is N/A by design. **Dismissed.**
2–4. `[vague] UT_TRANVIEW_ABNORMAL_001/002/003: 異常系 case but expected cites no message code` —
   the check hunts for an `[A-Z]{2}\d{3}` code / STOP / abort; ORION-CCMS has **none**. Each
   abnormal expected quotes the **verbatim on-screen literal** (`「Transaction id is required.」`,
   `「Transaction not found - check the id.」`, `「Invalid key pressed. Please try again.」`) — that
   quoted text **is** the assertion. **Dismissed** (known ORION literal-message pattern).

**Spot-check (3 random TCs re-verified against source):** UT…BOUNDARY_004 (WS-ED-AMT `(2:15)`
drops the blank pad byte, `-500.00` sign preserved) ✓ · UT…ABNORMAL_002 (3000 NOTFND → 2100 WHEN
OTHER → WS-M-TRAN-NOTFND) ✓ · IT…HAPPY_009 (PF4 → 1000-SEND-INITIAL MOVE LOW-VALUES + ERASE +
prompt) ✓.

Build: `OCTRANV_TestCases.xlsx` — 23 cases, `--lang en`, author modernizeX, count cell G4 = 23.
