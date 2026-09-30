# NOTES — OCAUTHQ (Card Authorization via MQ) test-case set

## Run record

- **App / root program:** OCAUTHQ · CICS transaction **ORAQ** · BMS map **MAUTHQ/MAUTHQA** (SDD-MAP-008)
- **App kind (by evidence, not root_type):** **UI (CICS/BMS)** — `design_evidence.py` → `kind=ui` ("a UI document exists"); the reg owns BMS map `MAUTHQA`.
- **REG (read-only ground truth):** `/Users/thangnguyen/Library/Caches/.skr/run-19580/reg`
  - Program AST: `parsed/cobol_xml/ORION-CCMS/cbl/OCAUTHQ.xml` (+ linked sub `OUMQREQ.xml`)
  - Copybooks: `KAUTH` (auth request/response), `KCOMM` (ORION-COMMAREA), `WMSG` (messages), `WCONST` (flags/file names), `WHEAD` (header), `MAUTHQ` (map)
- **Design suite (preferred evidence, read-only):** `…/ORION/design_asis/UI/OCAUTHQ_CardAuthorization_ScreenDesign.md` + design NOTES.md
- **OUT:** `…/analysis_output/datastore/design_runs/test_cases/OCAUTHQ`
- **Deliverable language:** English (fixed by skill edition). Verbatim on-screen strings kept unchanged inside 「…」; code identifiers demoted to 備考 (remark).
- **Automation:** none (AS-IS CICS screen, no UI harness) → `自動 = ×`, `自動化ID` empty.
- **Author:** modernizeX.

### Approval gate (NON-INTERACTIVE)

The run is non-interactive. **Assumption recorded and proceeded:** the viewpoint table
below is taken as approved; the coverage matrix and cases were then generated from it.
If a reviewer rejects a viewpoint decision, only the affected table row + its TCs change.

## Program behaviour (grounded facts used for the cases)

OCAUTHQ is a **pseudo-conversational** CICS screen. `0000-MAIN`: first entry
(`EIBCALEN=0`) or `CA-FIRST-ENTER` → send the initial map; otherwise `EIBAID` decides:

| AID key | Action (source) | Result |
|---|---|---|
| `ENTER` | `2100-AUTHORIZE` → `2200-VALIDATE-INPUT`; if OK `2300-CALL-MQREQ` (EXEC CICS LINK OUMQREQ) then `2400-SHOW-DECISION` | validate → send request over MQ → show decision |
| `PF3` | `7000-XCTL-MENU` (XCTL OCMENU) | return to Main Menu |
| `PF4` | `1000-SEND-INITIAL` | clear entries, redisplay blank screen |
| any other | `1000-SEND-INITIAL` + `WS-MSG-INVALID-KEY` | invalid-key message |

**Validation (`2200-VALIDATE-INPUT`), in order — stops at first failure:**
1. Card Num blank/low-values → 「Card number is required.」
2. Amount blank/low-values → 「Amount is required.」
3. `NUMVAL(Amount) <= 0` → 「Amount must be greater than zero.」 (non-numeric → NUMVAL=0 → same message)
- **Merchant is NOT validated** — optional (no required/format check in the AST).

**Decision (displayed by `2400-SHOW-DECISION`; computed by linked `OUMQREQ`, copybook `KAUTH`):**
`AS-DECISION` = APPROVED / DECLINED / ERROR; `AS-REASON` X(20); `AS-AVAIL-CREDIT` S9(10)V99 edited into the 15-char Avail Crd field (`WS-ED-BAL(2:15)`).

| Outcome (verbatim reason) | Decision | Trigger in OUMQREQ | Avail |
|---|---|---|---|
| 「APPROVED OK」 | APPROVED | active account, amount ≤ (limit − balance) — `3600-DECIDE` WHEN OTHER | limit − balance |
| 「INSUFFICIENT CREDIT」 | DECLINED | amount > available — `3600` WHEN `AQ-AMOUNT > WS-AMT` | limit − balance |
| 「ACCOUNT INACTIVE」 | DECLINED | `AC-ACTIVE-STATUS NOT = 'Y'` — `3600` | limit − balance |
| 「ACCOUNT NOT FOUND」 | DECLINED | account read NOTFND — `3400-DECLINE-NOACCT` | 0 |
| 「CARD NOT FOUND」 | DECLINED | card XREF read NOTFND — `3500-DECLINE-NOCARD` | 0 |
| 「FILE READ ERROR」 | ERROR | backend read I/O error — `3300` IO-ERROR; sets `CA-ERR-ON`, `CA-ERR-MSG`="AUTH ERROR - FILE READ ERROR" | — |
| 「LINK OUMQREQ FAILED」 | ERROR | `EXEC CICS LINK` to OUMQREQ non-normal — OCAUTHQ `2300` ELSE; system DISPLAY 'OCAUTHQ: LINK RESP=' | — |

On a **normal** decision (`CA-ERR-OFF`) the message line shows 「Authorization complete. PF3=Back.」;
on an **errored** decision (`CA-ERR-ON`, set by OUMQREQ `9000-FINALIZE`) it shows `CA-ERR-MSG`.

**Input fields (BMS map MAUTHQA — bms_mockup / design §2):** Card Num `CARDNUM` X(16), Amount `AMOUNT` X(12) (→ S9(09)V99), Merchant `MERCH` X(20). Output: Decision `DECISN` X(8), Reason `REASON` X(20), Avail `AVAIL` 15, error line `ERRMSG` 78.

**No message CODES exist** — `msg_codes.py OCAUTHQ.xml` = 0 codes/0 STOP. This system uses **literal English on-screen strings** (from `WMSG` + inline literals), not EI/EF/GF codes. Therefore every abnormal TC asserts the **verbatim string** (quoted 「…」), which is the grounded assertion. `§5 DB CRUD = none` — the screen creates/updates/deletes no records (navigation + display only).

## Standard fixture — F-STD

Operator is **signed on** (via Sign-On OCSGNON) and has opened **Card Authorization** (transaction **ORAQ**, reached from the Main Menu OCMENU). The MQ authorizer sub-program **OUMQREQ** and its request/reply queues are available; the card cross-reference (`XREFFILE`) and account (`ACCTFILE`) master files are online. Each TC lists only its **delta** from F-STD (the specific card / account state or key pressed). Exact backend master rows are marked `*(needs real data)*`; the scenario values (card numbers, limits, balances, amounts) are concrete and internally consistent.

## Viewpoint table (観点表) — every base UI viewpoint decided

| # | 観点 (中項目) | Applicable? | Evidence (spec/AST) | Planned TC |
|---|---|---|---|---|
| U1 | 画面表示・レイアウト (Screen display / layout) | ✅ | §2 mockup + BMS MAUTHQA (23 fields), header/captions/legend | IT_AUTH_HAPPY_001 |
| U2 | 初期値・デフォルト (Initial values / defaults) | ✅ | `1000-SEND-INITIAL`: LOW-VALUES + prompt + date/time header | IT_AUTH_HAPPY_002 |
| U3 | 表示条件・活性制御 (Display conditions / enable control) | ✅ | §2 item-states: Card/Amount/Merchant enterable, rest display-only | IT_AUTH_HAPPY_003 |
| U4 | 文字・書式表示 (Characters / format display) | ➖ merged | field colours (BLUE/GREEN/TURQUOISE/RED) verified in U1; numeric edit → U9 | (in U1, U9) |
| U5 | 入力チェック (Input validation) | ✅ | `2200-VALIDATE-INPUT` (3 checks, ordered) + merchant optional | UT_AUTH_ABNORMAL_001–005, IT_AUTH_HAPPY_004 |
| U6 | 操作性・キー (Operability / function keys) | ✅ | `EVALUATE EIBAID`: ENTER/PF3/PF4/other | IT_AUTH_HAPPY_005–006, UT_AUTH_ABNORMAL_006 |
| U7 | 状態遷移 (Screen / state transitions) | ✅ | stay-on-screen after decision; error echoes input; §4 events | IT_AUTH_HAPPY_007–008 |
| U8 | 機能・業務フロー (Function / business flow) | ✅ | one happy TC per decision classifier (KAUTH / OUMQREQ 3400/3500/3600) | IT_AUTH_HAPPY_009–013 |
| U9 | 計算・編集ロジック (Calculation / editing logic) | ✅ | Avail = limit − balance, edited `WS-ED-BAL(2:15)` | IT_AUTH_HAPPY_014 |
| U10 | 出力・帳票・メール (Output / report) | ❌ | §5: no record C/U/D, no report, no mail — the MQ request hand-off is covered as 連携 (U18) | — |
| U11 | データ整合性・冪等性 (Data integrity / idempotency) | ✅ | writes no DB; each ENTER re-sends a fresh request (no dedup guard) | IT_AUTH_HAPPY_015 |
| U12 | 排他・同時実行 (Exclusion / concurrent access) | ❌ | OCAUTHQ performs no update/lock; authorizer only READs — nothing to serialise | — |
| U13 | メッセージ・異常系 (Messages / abnormal) | ✅ | error decisions ERROR/FILE READ ERROR + LINK OUMQREQ FAILED | UT_AUTH_ABNORMAL_007–008 |
| U14 | 境界値 (Boundary values) | ✅ | Amount 0/0.01/−0.01/max; Card 16-char/blank; amount = available | UT_AUTH_BOUNDARY_001–005 |
| U15 | 回復・リラン (Recovery / rerun) | ❌ | pseudo-conversational, atomic RETURN; no partial commit to recover (re-submit → U11; PF4 clear → U6) | — |
| U16 | 権限・セキュリティ (Authority / security) | ❌ | OCAUTHQ tests no `CA-USER-*` — access gated upstream by Sign-On/Menu (verified: no role check in AST) | — |
| U17 | ログ・監査 (Log / audit) | ❌ | no business audit record; only a system `DISPLAY 'OCAUTHQ: LINK RESP='` on LINK failure (seen in UT_AUTH_ABNORMAL_008) | — |
| U18 | 連携・インターフェース (Linkage / interface) | ✅ | EXEC CICS LINK OUMQREQ (request "AREQ"+card+amount+merchant over commarea); PF3 XCTL OCMENU | IT_AUTH_HAPPY_016 (+ PF3 in HAPPY_006) |
| U19 | 性能・運用 (Operation / performance) | ❌ | runtime/volume — no paper/printer/env setup for this screen | — |

App-specific viewpoints reviewed: **pseudo-conversational entry** (typed ORAQ `EIBCALEN=0` vs arrival from menu `CA-FIRST-ENTER`) — both reach the initial screen, folded into U1/U2 (noted). **Merchant optional** — folded into U5 (IT_AUTH_HAPPY_004).

## Coverage matrix (viewpoint → evidence row → TC)

| 中項目 | Evidence row | TC IDs | 分類 |
|---|---|---|---|
| 画面表示・レイアウト | initial render: header (Tran ORAQ / Pgm OCAUTHQ / title / date / time), captions, legend, blank inputs | IT_AUTH_HAPPY_001 | Normal |
| 初期値・デフォルト | blank fields + 「Enter card, amount, merchant; press ENTER.」 + live date/time | IT_AUTH_HAPPY_002 | Normal |
| 表示条件・活性制御 | Card/Amount/Merchant enterable; Decision/Reason/Avail/header display-only | IT_AUTH_HAPPY_003 | Normal |
| 入力チェック | Card required | UT_AUTH_ABNORMAL_001 | Abnormal |
| 入力チェック | Amount required | UT_AUTH_ABNORMAL_002 | Abnormal |
| 入力チェック | Amount ≤ 0 (zero) | UT_AUTH_ABNORMAL_003 | Abnormal |
| 入力チェック | Amount non-numeric (NUMVAL=0) | UT_AUTH_ABNORMAL_004 | Abnormal |
| 入力チェック | validation order (card wins when both blank) | UT_AUTH_ABNORMAL_005 | Abnormal |
| 入力チェック | Merchant optional — blank accepted | IT_AUTH_HAPPY_004 | Normal |
| 操作性・キー | PF4 clear | IT_AUTH_HAPPY_005 | Normal |
| 操作性・キー | PF3 → OCMENU | IT_AUTH_HAPPY_006 | Normal |
| 操作性・キー | invalid key | UT_AUTH_ABNORMAL_006 | Abnormal |
| 状態遷移 | stays on screen after a decision; re-entry allowed | IT_AUTH_HAPPY_007 | Normal |
| 状態遷移 | validation error keeps input echoed, clears decision fields | IT_AUTH_HAPPY_008 | Normal |
| 機能・業務フロー | APPROVED (main success) | IT_AUTH_HAPPY_009 | Normal |
| 機能・業務フロー | DECLINED INSUFFICIENT CREDIT | IT_AUTH_HAPPY_010 | Normal |
| 機能・業務フロー | DECLINED ACCOUNT INACTIVE | IT_AUTH_HAPPY_011 | Normal |
| 機能・業務フロー | DECLINED ACCOUNT NOT FOUND | IT_AUTH_HAPPY_012 | Normal |
| 機能・業務フロー | DECLINED CARD NOT FOUND | IT_AUTH_HAPPY_013 | Normal |
| 計算・編集ロジック | Avail Crd = limit − balance, edited | IT_AUTH_HAPPY_014 | Normal |
| データ整合性・冪等性 | re-submit sends a fresh request; no record written | IT_AUTH_HAPPY_015 | Normal |
| メッセージ・異常系 | ERROR / FILE READ ERROR | UT_AUTH_ABNORMAL_007 | Abnormal |
| メッセージ・異常系 | ERROR / LINK OUMQREQ FAILED | UT_AUTH_ABNORMAL_008 | Abnormal |
| 境界値 | Amount = 0.01 (min positive) accepted | UT_AUTH_BOUNDARY_001 | Boundary |
| 境界値 | Amount = −0.01 rejected | UT_AUTH_BOUNDARY_002 | Boundary |
| 境界値 | Amount = 999,999,999.99 (max) accepted | UT_AUTH_BOUNDARY_003 | Boundary |
| 境界値 | Card = 16 chars accepted (no format/min-length check) | UT_AUTH_BOUNDARY_004 | Boundary |
| 境界値 | Amount = available (approved) vs available+0.01 (insufficient) | UT_AUTH_BOUNDARY_005 | Boundary |
| 連携・インターフェース | LINK to OUMQREQ builds request "AREQ"+card+amount+merchant; response displayed | IT_AUTH_HAPPY_016 | Normal |

**Totals:** 29 TC — Normal 16 · Abnormal 8 · Boundary 5. Applicable viewpoints 11 (+1 merged) all have ≥1 TC; 7 excluded, each with a reason.

## Audit result

`audit_testcase.py cases.json --reg <REG> --root OCAUTHQ` → **10 CANDIDATE findings, all grounded and resolved with no case change required**:

| # | Candidate | Grounding / resolution |
|---|---|---|
| 1 | `[valid] could not run msg_codes (no reg xml / design scripts)` | The audit's `_root_xml` only looks in `parsed/cobol_xml/main|sub/`; this reg nests the AST at `parsed/cobol_xml/ORION-CCMS/cbl/`. Ran `msg_codes.py` directly on the real path → **0 codes, 0 STOP** (`"codes": {}`). No coded validations exist → nothing to cover. **Dismissed.** |
| 2–9 | `[vague] UT_AUTH_ABNORMAL_001–008: 異常系 case but expected cites no message code / STOP` | This system uses **literal English on-screen strings**, not EI/EF/GF message IDs (confirmed by #1). Each abnormal TC asserts its **verbatim** string inside 「…」 (「Card number is required.」, 「Amount is required.」, 「Amount must be greater than zero.」, 「Invalid key pressed. Please try again.」, 「FILE READ ERROR」, 「LINK OUMQREQ FAILED」) — the grounded assertion. Fabricating a code would be a defect. **Dismissed (whole class).** |
| 10 | `[wording] UT_AUTH_ABNORMAL_007: 'expected' code-centric (verb 'READ')` | The token `READ` is inside the verbatim on-screen reason literal 「FILE READ ERROR」 / 「AUTH ERROR - FILE READ ERROR」 (grounded at OUMQREQ `3300` L208), reproduced unchanged per the skill's verbatim-string rule — not a narration COBOL verb. **False positive; dismissed.** |

Structural checks all clean: 29 unique ids; no empty id/name/category/expected; every case has a canonical 中項目 (12 viewpoints used, all from the table); automation columns consistent (`auto=×`, no automation_id). JP/VI parity not applicable (EN-only build).

**Spot-check (definition of done — 3 random TCs re-verified against cited source):**
- IT_AUTH_HAPPY_009 (APPROVED) — OUMQREQ `3600` WHEN OTHER L218–219 (AS-APPROVED / 「APPROVED OK」); OCAUTHQ `2400` L241 「Authorization complete. PF3=Back.」; Avail = limit−balance L232–233. ✓
- UT_AUTH_ABNORMAL_003 (Amount 0) — OCAUTHQ `2200` L149 NUMVAL, L150 `<= ZERO`, L152 「Amount must be greater than zero.」. ✓
- IT_AUTH_HAPPY_006 (PF3) — OCAUTHQ `2000` L98 WHEN DFHPF3 → `7000-XCTL-MENU` L212–220 XCTL OCMENU. ✓

**Conclusion:** coverage matrix approved (assumption); every applicable viewpoint → ≥1 TC; every excluded viewpoint has a reason; every audit candidate grounded; cases.json + workbook build cleanly. **Done.**
