# NOTES — OUMSG test-case set (run record)

- **App:** `OUMSG` — *Message-Text Retriever* (ORION-CCMS credit-card system)
- **Deliverable language:** English (fixed by skill edition; source is English — reproduced, message texts quoted verbatim)
- **REG (ground truth, read-only):** `/Users/thangnguyen/Library/Caches/.skr/run-90298/reg`
- **Design suite (preferred evidence, read-only):** `/Users/thangnguyen/Library/Application Support/ModernizeX/data/output/10/ORION/design_asis`
- **Design evidence read:** `Batch/OUMSG_MessageRetriever_ProgramDesign.md` + design-run `NOTES.md` (app_029 routing row)
- **OUT:** `…/analysis_output/datastore/design_runs/test_cases/OUMSG`
- **Generated:** 2026-09-29 · author `modernizeX`

## App-kind classification — **batch/sub** (CICS-linked subprogram)

| Signal | Value | Source |
|---|---|---|
| `design_evidence.py` verdict | **ui** — *"the reg has a BMS map"* | coarse: `reg_says_ui()` returns UI whenever **any** BMS map exists **anywhere** in the reg, not per-app |
| apps.json `root_type` / programs.json `type` | **batch** / **batch** | `datastore/apps.json` app_029 · `datastore/programs.json` OUMSG |
| `has_screen_section` | **false** | `datastore/programs.json` OUMSG |
| Owns a BMS map? | **no** (OUMSG is in no `bms_usage` / `reverse_bms_usage` entry) | `datastore/relationships.json` |
| Design-suite routing (authoritative) | **batch/sub (no map, no SCREEN SECTION)** → `Batch/OUMSG_MessageRetriever_ProgramDesign.md` | design-run `NOTES.md` routing table, app_029 |
| Source header | `TYPE : SUBROUTINE (CALL / EXEC CICS LINK)` | `cbl/OUMSG.cbl` L4 |

**Decision: OUMSG is a batch/sub app** (a called subprogram), tested data-driven per the Batch
viewpoint catalog (B1–B12). The `design_evidence.py` "ui" result is a false positive — its
`reg_says_ui()` fires on the presence of *any* BMS map in the whole ORION-CCMS reg (41 maps
exist system-wide), not on OUMSG owning one. OUMSG has no screen section, no own map, no
files/DB, no SQL — it is the message-text lookup module every screen links to.

> **Approval gate (non-interactive run):** the viewpoint table below is **recorded as an
> assumption and accepted** so generation can continue, per the run's NON-INTERACTIVE
> directive. If a reviewer rejects a viewpoint decision, adjust the table row and regenerate
> the affected cases from `cases.json`.

## Program summary (ground truth — AST `parsed/cobol_xml/…/OUMSG.xml`, raw `cbl/OUMSG.cbl`)

Pure logic module — **no files, no SQL, no screen, no called subprograms** (`file_selects=[]`,
`has_sql=false`, `call_targets=[]`). 23 lines of source, one paragraph `0000-MAIN` (L17–L33).

**Interface — COMMAREA `KMSG-PARM` (copybook `KMSG`, 88 bytes), passed on `EXEC CICS LINK`
or static `CALL 'OUMSG' USING KMSG-PARM`:**

| Field | PIC | Bytes | Pos | Direction | Meaning |
|---|---|---|---|---|---|
| `KM-CODE` | X(06) | 6 | 1–6 | in | message code to look up |
| `KM-TEXT` | X(80) | 80 | 7–86 | out | message text returned |
| `KM-STATUS` | X(02) | 2 | 87–88 | out | `'00'` = found · `'01'` = not found |

**Logic (`0000-MAIN`):** `MOVE '00' TO KM-STATUS` (L18) → `EVALUATE KM-CODE` (L19) → `GOBACK` (L33).

| KM-CODE | KM-TEXT returned (verbatim) | KM-STATUS | Semantic | Source |
|---|---|---|---|---|
| `I0001` | `Operation completed successfully.` | `00` | info | L20 |
| `E0001` | `Record not found.` | `00` | error | L22 |
| `E0002` | `Duplicate record.` | `00` | error | L23 |
| `E0003` | `Please enter required fields.` | `00` | error | L24 |
| `E0004` | `Invalid data entered.` | `00` | error | L26 |
| `E0005` | `File access error.` | `00` | error | L27 |
| `W0001` | `No records to display.` | `00` | warning | L28 |
| *any other* | *(echoes the input code into KM-TEXT)* | `01` | not found | L29–L31 (WHEN OTHER) |

**Dead code (never tested — excluded with reason):** `WS-FLAG PIC X(01) VALUE 'N'` (L13) is
declared in WORKING-STORAGE but **never referenced** in the procedure — no branch depends on it,
so no TC targets it (skill: never test dead code).

**Match semantics grounding the boundaries:** the WHEN literals (`'I0001'`, `'E0001'`, `'W0001'`)
are 5 characters; `KM-CODE` is 6 bytes. COBOL alphanumeric comparison pads the shorter operand
with trailing spaces, so a code matches only when `KM-CODE` = the 5-char code **left-justified
with a trailing space**. A non-space 6th byte, a leading space, or a case difference all miss →
`WHEN OTHER` (status `'01'`). These are the real boundaries tested below.

## Viewpoint table (観点表) — every base viewpoint decided

中項目 = canonical viewpoint label (`<JP> (<EN>)`). Batch base catalog B1–B12 mapped in Evidence.
✅ applicable · △ applicable, covered under another row · ❌ excluded with reason.

| 観点 (中項目) | ? | Evidence (spec/AST) | Planned TC |
|---|---|---|---|
| 画面表示・レイアウト (Screen display / layout) | ❌ | no SCREEN SECTION, owns no BMS map (`has_screen_section=false`); called subprogram | — |
| 初期値・デフォルト (Initial values / defaults) | △ | KM-STATUS defaulted `'00'` every call (L18) → covered as integrity | see データ整合性 |
| 表示条件・活性制御 (Display conditions / enable control) | ❌ | no screen / no conditional display | — |
| 文字・書式表示 (Characters / format display) | △ | code-field format (case, trailing-space padding) → covered as boundary | see 境界値 |
| 入力チェック (Input validation) | △ | B2 — only match/no-match on KM-CODE; unrecognised input → WHEN OTHER | see メッセージ・異常系 / 境界値 |
| 操作性・キー (Operability / function keys) | ❌ | subprogram — no interactive keys / AID | — |
| 状態遷移 (Screen / state transitions) | ❌ | no screen states | — |
| 機能・業務フロー (Function / business flow) | ✅ | B1/B3 — one happy run per catalog code (7 classifier values), L20–L28 | IT_MESSAGE_HAPPY_001–007 |
| 計算・編集ロジック (Calculation / editing logic) | △ | B3 — the code→text mapping is the only "editing" logic; per-code above | see 機能・業務フロー |
| 出力・帳票 (Output / report) | ✅ | B4 — returned COMMAREA record, column/byte level (KM-CODE 1–6, KM-TEXT 7–86, KM-STATUS 87–88) | IT_MESSAGE_HAPPY_008 |
| データ整合性・冪等性 (Data integrity / idempotency) | ✅ | B5/B8 — status reset each call (L18), idempotent repeat, no carry-over, no side effects | IT_MESSAGE_HAPPY_009–011 |
| 排他・同時実行 (Exclusion / concurrent access) | ❌ | reentrant (`RENT` compile, CMPOUMSG), stateless, no files/DB → no shared mutable resource | — |
| メッセージ・異常系 (Messages / abnormal) | ✅ | B7 — WHEN OTHER: unrecognised code → status `'01'` + echoed text (L29–L31) | UT_MESSAGE_ABNORMAL_001 |
| 境界値 (Boundary values) | ✅ | B9 — KM-CODE X(06) vs 5-char codes (padding/6th-byte/case/leading-space/blank) + KM-TEXT X(80) capacity | UT_MESSAGE_BOUNDARY_001–006 |
| 回復・リラン (Recovery / rerun) | ❌ | no persistent state / no restart logic; each call independent (idempotency covered above) | — |
| 権限・セキュリティ (Authority / security) | ❌ | subprogram — no signon/auth; caller (screen) enforces authority | — |
| ログ・監査 (Log / audit) | ❌ | no log/audit writes (`file_io_operations=[]`) | — |
| 連携・インターフェース (Linkage / interface) | ✅ | B2 — COMMAREA contract via `EXEC CICS LINK` and static `CALL … USING KMSG-PARM` (source L4/L6) | IT_MESSAGE_HAPPY_012–013 |
| 運用 (Operation) | ❌ | no operational run — only a compile/link job (`CMPOUMSG`, DFHYITVL PROC); invoked at runtime by screens | — |
| *(app-specific)* dead variable WS-FLAG | ❌ | `WS-FLAG` (L13) declared, never referenced → dead code, not tested | — |

## Coverage matrix (approved viewpoint → evidence → TC)

| 中項目 | Evidence rows | TC IDs | 分類 |
|---|---|---|---|
| 機能・業務フロー | I0001·E0001·E0002·E0003·E0004·E0005·W0001 (7 codes) | IT_MESSAGE_HAPPY_001–007 | Normal ×7 |
| 出力・帳票 | 88-byte COMMAREA layout after a hit | IT_MESSAGE_HAPPY_008 | Normal ×1 |
| データ整合性・冪等性 | status reset '00'; idempotent repeat; no carry-over after a miss | IT_MESSAGE_HAPPY_009–011 | Normal ×3 |
| 連携・インターフェース | EXEC CICS LINK contract; static CALL contract | IT_MESSAGE_HAPPY_012–013 | Normal ×2 |
| メッセージ・異常系 | WHEN OTHER — unknown code → status '01' + echo | UT_MESSAGE_ABNORMAL_001 | Abnormal ×1 |
| 境界値 | padded match; 6th-byte non-space; case; leading space; blank; text capacity | UT_MESSAGE_BOUNDARY_001–006 | Boundary ×6 |

**Totals:** 20 TC — Normal 13 · Abnormal 1 · Boundary 6.
Every applicable viewpoint → ≥1 TC. Every excluded viewpoint → reason above. No matrix row unmapped.

## Standard fixture — F-STD

> **F-STD** — The message-text retriever is available in the CICS test region (compiled and
> link-edited by job `CMPOUMSG` via the `DFHYITVL` translate+compile+link PROC) and is callable
> both by `EXEC CICS LINK PROGRAM('OUMSG') COMMAREA(KMSG-PARM)` from a screen program and by
> static `CALL 'OUMSG' USING KMSG-PARM`. The caller allocates the 88-byte communication area
> `KMSG-PARM` (code 6 bytes / text 80 bytes / status 2 bytes) and clears it before the call.

Each TC's precondition = **F-STD** + its delta (the exact code value placed in the input field).

## Audit result

`python3 scripts/audit_testcase.py cases.json --reg <REG> --root OUMSG`

- **viewpoints used (6):** 機能・業務フロー, 出力・帳票, データ整合性・冪等性, 連携・インターフェース, メッセージ・異常系, 境界値 — all ⊆ the approved 観点表.
- **2 candidates — both grounded & dismissed:**
  1. `[valid] could not run msg_codes (no reg xml / design scripts) — skip code coverage.`
     **Dismissed (path-layout, not a coverage gap):** the audit's `_root_xml()` only searches
     `<reg>/parsed/cobol_xml/main|sub/<root>.xml`, but this reg is laid out as
     `parsed/cobol_xml/ORION-CCMS/cbl/OUMSG.xml`, so the audit could not locate the XML. Ran
     `msg_codes.py` **directly on the real XML** → `{"codes": {}, "stop_literals": []}` = **0
     codes**. OUMSG raises no EI/EF-style validation codes (its catalog codes `I0001/E0001…/
     W0001` are 1-letter+4-digit *returned data*, covered as classifier happy TCs) → there is
     no code-coverage gap to fill.
  2. `[vague] UT_MESSAGE_ABNORMAL_001: 異常系 case but expected cites no message code / STOP.`
     **Dismissed:** OUMSG's abnormal outcome is *not* a raised message code — the not-found
     result is signalled by `KM-STATUS='01'` (a concrete status literal, present in the
     expected) plus the echoed input code. There is no message code to cite; the assertion is
     the status value. Correct, grounded abnormal expectation for a lookup module.
- Ids unique; every case has id/name/category/expected; auto=`×` throughout (no automation
  harness for this AS-IS CICS subprogram); no code-leak candidates into body fields (all
  data-names / PIC / paragraph / line refs live in 備考).

## Assumptions

1. **Kind = batch/sub** (see classification table) — accepted non-interactively; overrides the
   `design_evidence.py` "ui" false positive.
2. Test level = *Function test (called subprogram)*; environment left `*fill*` — running these
   cases needs a CICS test region (or a COBOL unit harness driving `KMSG-PARM`) the customer
   supplies. Automation columns: 自動=`×`, 自動化ID empty (no AS-IS harness).
3. Message texts are reproduced **verbatim** from the source `MOVE` literals; status values
   `'00'`/`'01'` and code literals are the module's own constants.
