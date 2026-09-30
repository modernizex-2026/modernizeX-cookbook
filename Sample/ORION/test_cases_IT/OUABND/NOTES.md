# OUABND — Test-case derivation notes (viewpoint-first)

**App:** `OUABND` — Abnormal-End Handler (ORION-CCMS, ORION Credit Card Management System)
**Deliverable language:** English (fixed by the skill edition)
**Sources (read-only):**
- Design: `design_asis/Batch/OUABND_AbendHandler_ProgramDesign.md` (ground evidence)
- REG (ground truth): `reg/` — AST `parsed/cobol_xml/ORION-CCMS/cbl/OUABND.xml`, copybook `KABND.cpy`, raw `OUABND.cbl` (40 lines)

## 1. What OUABND actually is (evidence-grounded)

A **CICS-linked utility subroutine** invoked by online programs when they hit an
unrecoverable error:

```
EXEC CICS LINK PROGRAM('OUABND') COMMAREA(KABND-PARM) END-EXEC.
```

Single paragraph `0000-MAIN`, strictly **linear — no IF / EVALUATE / PERFORM /
GO TO branches** (verified on the AST: 1 STRING, 1 WRITEQ TD, 1 ABEND, 1 GOBACK).
It does three things in order:

1. **Assembles** a 120-byte diagnostic line by concatenating (all operands
   `DELIMITED SIZE`): `'ABEND IN '` + program + `' AT '` + paragraph + `' - '` +
   detail.
2. **Writes** that line to the CICS transient-data queue **`CSSL`** (the CICS
   system-log destination) — this IS the failure audit record.
3. **Abends** the failing task via `EXEC CICS ABEND ABCODE('OABN')` (with the
   default transaction dump), then falls through to `GOBACK`.

### COMMAREA contract — copybook `KABND` (`KABND-PARM`, 126 bytes)

| Field (biz name) | Data-name | Type | Bytes | Pos | Used by OUABND? |
|---|---|---|---|---|---|
| Failing program | `KA-PROGRAM` | X(08) | 8 | 1–8 | ✅ in the line |
| Failing paragraph | `KA-PARAGRAPH` | X(30) | 30 | 9–38 | ✅ in the line |
| CICS response code | `KA-RESP-CD` | S9(09) COMP | 4 | 39–42 | ❌ **not consumed** |
| CICS reason code | `KA-REAS-CD` | S9(09) COMP | 4 | 43–46 | ❌ **not consumed** |
| Detail text | `KA-DETAIL` | X(80) | 80 | 47–126 | ✅ in the line (tail truncated) |

### Message-length arithmetic (the only real boundary)

Fixed parts `'ABEND IN '`(9) + `' AT '`(4) + `' - '`(3) = 16; plus program(8) +
paragraph(30) + detail(80) = **134 bytes** assembled into `WS-ABEND-MSG` which is
only **120 bytes**. All operands are `DELIMITED SIZE`, so the assembled length is
**always 134** and the receiving field **always truncates the last 14 bytes** —
i.e. only the **first 66 bytes of the detail** ever reach the log; detail bytes
67–80 are dropped. There is **no `ON OVERFLOW` clause**, so truncation is silent
and processing continues to the ABEND regardless.

### Ground-truth facts used

- `msg_codes.py OUABND.xml` → **0 validation codes, 0 STOP literals** (no EI/EF/GF/ER).
- `crud_from_ast.py` → **no file/table CRUD** (no VSAM/DB2 I/O; only the TD queue write).
- The only literal "codes" are the abend code **`OABN`** and the queue name **`CSSL`**.
- `WS-RESP-CD` is captured on both `WRITEQ TD` and `ABEND` but **never tested** —
  the program has no conditional handling of it.

## 2. Classification decision (assumption — recorded, non-interactive)

`design_evidence.py … OUABND` reports **`kind = ui`, reason "the reg has a BMS
map"**. That is a **system-wide false positive**: `bms_maps.json` is non-empty for
ORION-CCMS as a whole, so the resolver marks *every* member UI. **OUABND itself
has no `SCREEN SECTION`, no BMS map, and no `*_ScreenDesign.md`** — its design
document is a **Batch `ProgramDesign`**. It is a called CICS subroutine with no
operator interface.

**Decision (assumed approved for this non-interactive run):** derive OUABND with
the **Batch viewpoint catalog (B1–B12)** — data-driven: 前提 = COMMAREA fixtures,
手順 = LINK the handler with a COMMAREA, 期待 = the CSSL log record + the ABEND
outcome — augmented with the CICS specifics (TD-queue write, `EXEC CICS ABEND`).
The UI viewpoints (A–E) are all decided **N/A with reason** below. The viewpoint
table is recorded here as the approval artifact per the skill's non-interactive
rule.

## 3. Standard fixture — `F-STD`

A representative failing-caller COMMAREA, values constructible from the copybook
and a plausible online failure (concrete, reusable; each TC states only its delta):

| Field | `F-STD` value |
|---|---|
| Failing program | `OCTRANV` (an online transaction-view program of the system) |
| Failing paragraph | `2100-READ-TRANSACT` |
| CICS response code | `12` (NOTFND) |
| CICS reason code | `0` |
| Detail text | `TRANSACTION FILE READ FAILED - KEY 0000000123456` |
| Environment | CICS region with TD queue `CSSL` defined; caller runs under a CICS task |

Assembled line under `F-STD` (fits within 120 — detail is 48 chars ≤ 66):
`ABEND IN OCTRANV  AT 2100-READ-TRANSACT           - TRANSACTION FILE READ FAILED - KEY 0000000123456`

## 4. Per-app viewpoint table (中項目 decisions — EVERY base viewpoint)

`✅` = becomes 中項目 rows with TCs · `❌` = excluded with a spec-grounded reason ·
`↪` = applicable but covered under another viewpoint.

| 観点 (canonical 中項目) | Applicable? | Evidence (spec / AST) | Planned TC |
|---|---|---|---|
| 機能・業務フロー (Function / business flow) | ✅ | Design §1.2; AST `0000-MAIN` (STRING→WRITEQ→ABEND) — the single end-to-end flow | IT_ABEND_HAPPY_001 |
| 計算・編集ロジック (Calculation / editing logic) | ✅ | AST STRING assembly `'ABEND IN '…' AT '…' - '…`, all `DELIMITED SIZE` | IT_ABEND_HAPPY_002 |
| 出力・帳票 (Output / report) | ✅ | AST `WRITEQ TD QUEUE('CSSL')`, `LENGTH OF WS-ABEND-MSG`=120; unused resp/reas | IT_ABEND_HAPPY_003, _004, _005 |
| データ整合性・冪等性 (Data integrity / idempotency) | ✅ | One WRITEQ + one ABEND per call; append-only TD queue; stateless | IT_ABEND_HAPPY_006 |
| 連携・インターフェース (Linkage / interface) | ✅ | Design §1.6 `EXEC CICS LINK PROGRAM('OUABND') COMMAREA(KABND-PARM)`; 126-byte layout; ABEND ⇒ no RETURN | IT_ABEND_HAPPY_007 |
| メッセージ・異常系 (Messages / abnormal) | ✅ | AST `EXEC CICS ABEND ABCODE('OABN')`; RESP captured-but-unchecked ⇒ env-fault path | UT_ABEND_ABNORMAL_001, _002 |
| 境界値 (Boundary values) | ✅ | 134-byte assembly vs 120-byte field, `DELIMITED SIZE`, no `ON OVERFLOW` | UT_ABEND_BOUNDARY_001, _002, _003 |
| ログ・監査 (Log / audit) | ↪ | The `CSSL` write IS the failure-audit record — its content is asserted in 出力・帳票 (IT_ABEND_HAPPY_003/004/005); no separate cases (would duplicate) | — |
| 排他・同時実行 (Exclusion / concurrent access) | ❌ (runtime) | CICS serialises TD `WRITEQ`; queue is append-only; no record locks in source. Append-independence is noted in IT_ABEND_HAPPY_006 | — |
| 回復・リラン (Recovery / rerun) | ❌ | Stateless subroutine, no restart/processed-flag/control-record logic; its purpose is to abend — nothing to rerun/recover inside it | — |
| 権限・セキュリティ (Authority / security) | ❌ | Runs under the caller's CICS task and authority; no signon, dept or mode gate of its own | — |
| 運用 (Operation) | ❌ | No paper/printer setup, environment prompt or operator instruction in source/design | — |
| 性能・運用 (Performance) | ❌ (runtime) | 40-line linear handler; throughput/response is a runtime property, deferred | — |
| 画面表示・レイアウト (Screen display / layout) | ❌ | No SCREEN SECTION / BMS map — called subroutine, no operator screen (see §2) | — |
| 初期値・デフォルト (Initial values / defaults) | ❌ | No screen; only `WS-ABCODE VALUE 'OABN'` const (asserted in UT_ABEND_ABNORMAL_001) | — |
| 表示条件・活性制御 (Display conditions / enable control) | ❌ | No screen fields to enable/disable | — |
| 文字・書式表示 (Characters / format display) | ❌ | No screen; edit-mask/zero-suppress N/A (line is plain text, covered in 計算ロジック) | — |
| 入力チェック (Input validation) | ❌ | Handler performs **no** input validation — it strings whatever the COMMAREA holds (0 checks in source) | — |
| 操作性・キー (Operability / function keys) | ❌ | No operator, no AID/ESTS keys — invoked by program LINK | — |
| 状態遷移 (Screen / state transitions) | ❌ | No screen/mode transitions; single linear path ending in ABEND | — |

### Dead-code exclusion (never test dead code)

`GOBACK` (final statement, L40) is **unreachable**: `EXEC CICS ABEND` terminates
the task before control reaches it. **No "control returns to the caller after
OUABND" TC is written** — the opposite (no normal return) is the assertion, made
in IT_ABEND_HAPPY_007 and UT_ABEND_ABNORMAL_001.

## 5. Design ↔ source discrepancies (source wins — REG is ground truth)

1. **Design §5.2 "Response check"** describes a conditional ("a file response
   other than normal or not-found → recorded in COMMAREA status and the step
   ends"). The **actual source has no such branch**: `WRITEQ TD` and `ABEND`
   capture `RESP(WS-RESP-CD)` but the program **never tests it** and always
   proceeds to abend. Grounded on the AST. Reflected honestly in
   UT_ABEND_ABNORMAL_002 (env fault → response is ignored, task still abends,
   line is lost). Design §5.2 is treated as a boilerplate row, not behaviour.
2. **Design §5.1** lists `KA-RESP-CD` / `KA-REAS-CD` as parameters. They are part
   of the COMMAREA but **not consumed** by OUABND (absent from the STRING).
   Documented in IT_ABEND_HAPPY_005 so a tester does not expect them in the log.

## 6. Coverage matrix

| Unit (evidence) | Category | TC(s) |
|---|---|---|
| Main flow: assemble → log → abend | Normal | IT_ABEND_HAPPY_001 |
| Diagnostic-line assembly format + separators | Normal | IT_ABEND_HAPPY_002 |
| CSSL record written, fixed length 120 | Normal | IT_ABEND_HAPPY_003 |
| Content pass-through (different failing site) | Normal | IT_ABEND_HAPPY_004 |
| resp/reas codes NOT emitted to the log line | Normal | IT_ABEND_HAPPY_005 |
| One line + one abend per call, append-only | Normal | IT_ABEND_HAPPY_006 |
| CICS LINK contract + 126-byte layout + no return | Normal | IT_ABEND_HAPPY_007 |
| ABEND code `OABN` + dump, abnormal task end | Abnormal | UT_ABEND_ABNORMAL_001 |
| Env fault: `CSSL` undefined → QIDERR unchecked → still abends, line lost | Abnormal | UT_ABEND_ABNORMAL_002 |
| Minimal input (all text fields spaces) | Boundary | UT_ABEND_BOUNDARY_001 |
| Detail fills exactly to 120 (first 66 bytes) — no loss | Boundary | UT_ABEND_BOUNDARY_002 |
| Full-width input 134>120 → tail of detail truncated | Boundary | UT_ABEND_BOUNDARY_003 |

**Totals:** 12 TC — Normal 7, Abnormal 2, Boundary 3.
Validation codes covered: 0 of 0 (no EI/EF/GF/ER in source) · flows covered: 1 of 1 ·
message codes excluded: none (none exist).

## 7. Audit result

`audit_testcase.py cases.json --reg <REG> --root OUABND` → **1 candidate**, grounded & dismissed:

- `[valid] could not run msg_codes (no reg xml / design scripts) — skip code coverage`
  → **Dismissed.** The audit probes `parsed/cobol_xml/{main,sub}/OUABND.xml`, but
  this reg stores XML under `parsed/cobol_xml/ORION-CCMS/cbl/OUABND.xml`, so the
  probe path misses. Run directly against the real XML, `msg_codes.py` returns
  **0 codes / 0 STOP** — there are no validation codes to cover or exclude. No
  action needed.

No duplicate/empty ids, no empty 中項目, no folding, no vagueness, no code-leak,
automation columns consistent (all `auto = ×`, no automation target for an AS-IS
CICS subroutine).

Spot-check (3 TCs re-verified against source): IT_ABEND_HAPPY_002 (STRING operands
L22–29) · UT_ABEND_ABNORMAL_001 (`ABCODE('OABN')` L36–37) · UT_ABEND_BOUNDARY_003
(134 vs 120, `DELIMITED SIZE`, no `ON OVERFLOW` L22–29) — all confirmed.
