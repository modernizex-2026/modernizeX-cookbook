# OCPAUIN — Pending Authorization Inquiry — Test-case NOTES

**App:** OCPAUIN (CICS transaction **ORPI**, mapset `MPAUIN` / map `MPAUINA`)
**Kind:** UI (CICS/BMS) — `design_evidence.py` → `kind: ui` (a UI ScreenDesign doc exists; also a BMS map exists).
**Deliverable language:** English (skill edition fixed). All business wording translated to English; only verbatim on-screen strings, message text, code identifiers and fixture values are reproduced unchanged.
**AREA word (ID scheme):** `PAUTHINQ` — unique across the run (siblings used CARDINQ / CUSTINQ). IDs = `{IT|UT}_PAUTHINQ_{HAPPY|ABNORMAL|BOUNDARY}_{NNN}`.

## Evidence read (only these, per design_evidence.py)
- UI doc: `design_asis/UI/OCPAUIN_PendingAuthInquiry_ScreenDesign.md` (§1 overview/keys, §2 map + item states, §3 checks, §4 events, §5 CRUD).
- Linked member design: `design_asis/Batch/OUIMSPA_PendingAuthDLI_ProgramDesign.md`.
- Ground truth (AST + source, since ORION docs are thin — see project memory): `reg/parsed/cobol_xml/ORION-CCMS/cbl/{OCPAUIN,OUIMSPA}.xml` and source `cbl/{OCPAUIN,OUIMSPA}.cbl`; copybooks `RPAUSEG, WMSG, WHEAD, KCOMM, WCONST, KDLI`; `dbd/OPAUDB.dbd`.
- `msg_codes.py OCPAUIN.xml` = **0 codes**; `msg_codes.py OUIMSPA.xml` = **0 codes**. As with every ORION-CCMS app, the screen raises **literal English message strings**, not EI/EF/GF codes — the assertion in each abnormal case IS the verbatim string.

## Approval gate (NON-INTERACTIVE)
The runner is non-interactive, so the viewpoint table below is **recorded as an approved assumption** and case generation continued (per skill instruction). If a reviewer disagrees with a viewpoint decision, adjust the table then regenerate cases.json.

## How OCPAUIN works (grounded)
OCPAUIN is a single-input inquiry screen. The operator keys an **Auth ID** and presses a key; OCPAUIN `EXEC CICS LINK`s the DL/I access sub **OUIMSPA** (function code in the shared commarea `CA-WORK-AREA`), which reads the `PAUSEG` root segment from IMS database `OPAUDB` via `CBLTDLI`, and OCPAUIN paints the returned segment onto the map. It is **read-only** (design §5 — no create/update/delete).

Reachable functions from this screen (the only two the driver ever sends):
- **ENTER** → `INQ ` (GU by key) — inquire one authorization by Auth ID.
- **PF8** → `NXT ` (GN browse) — page forward to the next authorization (key ascending).
- **PF3** → hands control to the main menu (OCMENU). **PF4** → clears and re-displays the key prompt. Any other key → invalid-key message.

**Status classifier** (`4100-STATUS-WORD`, one display word per `PA-STATUS` value) — the happy-path density driver:
`P`→PENDING · `A`→APPROVED · `D`→DECLINED · `X`→PURGED · any other byte→UNKNOWN.

**Messages reachable through OCPAUIN (8 — all literal strings, each gets a dedicated TC):**
| # | Verbatim string | Type | Trigger | TC |
|---|---|---|---|---|
| 1 | `Enter an authorization id and press ENTER.` | I | initial send (`1000-SEND-INITIAL`) | IT_..._001 / _002 |
| 2 | `Invalid key pressed. Please try again.` | E | unsupported AID key (`2000` OTHER, `WS-MSG-INVALID-KEY`) | UT_..._ABNORMAL_003 |
| 3 | `Please enter all required fields.` | E | blank Auth ID on ENTER (`2100`, `WS-MSG-REQUIRED`) | UT_..._ABNORMAL_001 |
| 4 | `Pending authorization retrieved.` | I | GU/GN OK (`6000-RETURN-SEGMENT`) | IT_..._007–013 |
| 5 | `No pending authorization for that id.` | E | GE on inquire (`6100-NOT-FOUND`) | UT_..._ABNORMAL_002 |
| 6 | `End of pending authorization list.` | I | GB/GE on browse (`6200-END-OF-LIST`) | UT_..._BOUNDARY_007 |
| 7 | `Unable to link to IMS module OUIMSPA.` | E | LINK RESP not NORMAL (`3000-CALL-SUB`) | UT_..._ABNORMAL_005 |
| 8 | `DL/I error status=… func=…` | E | unexpected DL/I status (`6900-DLI-ERROR`, via GU or GN) | UT_..._ABNORMAL_004 |

## Standard fixture — F-STD
**No seed exists for `OPAUDB`/`PAUSEG` in the reg** (the `data/` folder seeds only the VSAM files; the IMS DB is defined by `OPAUDB.dbd`/`OPAUPSB.psb` with no record dump). The records below are **constructed from the copybook PICs** (`RPAUSEG`: Auth ID X(16) key · Card X(16) · Acct 9(11) · Amount S9(9)V99 · Merchant X(50) · Req TS X(26) · Status X(1) · Decision reason X(30)) to exercise every branch; exact production values are marked `*(needs real data)*`. Key order ascending = id order below.

| Auth ID (key) | Card | Acct | Amount | Merchant | Req TS | Status→word | Decision reason |
|---|---|---|---|---|---|---|---|
| `AUTH000000000001` | 4111111111111111 | 00000000001 | 1,500.00 | AMAZON.COM MARKETPLACE | 2026-09-28-10.30.00.000000 | P→PENDING | AWAITING MANUAL REVIEW |
| `AUTH000000000002` | 4111111111111112 | 00000000002 | 42.50 | STARBUCKS STORE 01234 | 2026-09-28-10.31.00.000000 | A→APPROVED | AUTO APPROVED WITHIN LIMIT |
| `AUTH000000000003` | 4111111111111113 | 00000000003 | 9,999.99 | BEST BUY STORE 00077 | 2026-09-28-10.32.00.000000 | D→DECLINED | INSUFFICIENT AVAILABLE CREDIT |
| `AUTH000000000004` | 4111111111111114 | 00000000004 | 250.00 | WALMART SUPERCENTER 2201 | 2026-09-28-10.33.00.000000 | X→PURGED | — |
| `AUTH000000000005` | 4111111111111115 | 00000000005 | 75.00 | SHELL OIL STATION 55555 | 2026-09-28-10.34.00.000000 | Q→UNKNOWN | — |
| `AUTH000000000006` | 4111111111111116 | 00000000006 | 999,999,999.99 | GLOBAL SUPERSTORE INTERNATIONAL TRADING CO NO 1 | 2026-09-28-10.35.00.000000 | A→APPROVED | HIGH VALUE APPROVED |
| `AUTH000000000007` | 4111111111111117 | 00000000007 | 0.00 | ZERO AUTH TEST MERCHANT | 2026-09-28-10.36.00.000000 | P→PENDING | — |
| `AUTH000000000008` | 4111111111111118 | 00000000008 | -500.00 | REFUND REVERSAL MERCHANT | 2026-09-28-10.37.00.000000 | A→APPROVED | REVERSAL POSTED |

Not-on-file key used for lookup-fail / end-of-list: `AUTH000000000099`. All fixtures `*(needs real data)*` for exact production content; formats are grounded in `RPAUSEG`.

## Per-app viewpoint table (観点表) — EVERY base viewpoint decided
中項目 = canonical viewpoint label (`<JP> (<EN>)`). Category set: Normal / Abnormal / Boundary.

| 観点 (中項目) | Applicable? | Evidence | Planned TC |
|---|---|---|---|
| 画面表示・レイアウト (Screen display / layout) | ✅ | UI §2 map MPAUINA (header, Auth ID prompt, result captions, key legend line 24) | IT_..._001 |
| 初期値・デフォルト (Initial values / defaults) | ✅ | `1000-SEND-INITIAL` prompt msg #1; blank result fields (LOW-VALUES); header date/time (ASKTIME/FORMATTIME) | IT_..._002 |
| 表示条件・活性制御 (Display conditions / enable control) | ✅ | UI §2 item-states: Auth ID input (UNPROT/IC) vs all result fields display-only | IT_..._003 |
| 操作性・キー (Operability / function keys) | ✅ | UI §1.2 keys ENTER/PF3/PF4/PF8; `2000-PROCESS-INPUT` EVALUATE EIBAID | IT_..._004,005 (PF3,PF4); ENTER/PF8 in 機能; invalid key → ABNORMAL_003 |
| 状態遷移 (Screen / state transitions) | ✅ | pseudo-conv: EIBCALEN=0 / `CA-FIRST-ENTER` → initial; stays on screen after inquire; PF3 → menu | IT_..._006 |
| 機能・業務フロー (Function / business flow) | ✅ | UI §1.2 functions; `2100-INQUIRE`, `2200-BROWSE-NEXT`; status classifier P/A/D/X/other | IT_..._007–013,018 |
| 計算・編集ロジック (Calculation / editing logic) | ✅ | amount edit mask `WS-ED-AMT`; `4100-STATUS-WORD` mapping incl. UNKNOWN fallback | IT_..._014,015 |
| 出力・帳票 (Output / report) | ❌ N/A | screen inquiry, no printed report / file written / mail | — |
| データ整合性・冪等性 (Data integrity / idempotency) | ✅ | read-only (design §5); re-inquire returns same, no row C/U/D | IT_..._016 |
| 排他・同時実行 (Exclusion / concurrent access) | ❌ N/A (runtime) | inquiry issues GU/GN only — no hold (GHU)/REPL/DLET reachable from this screen; the purged-anchor reposition (concurrent delete) is covered functionally as IT_..._013 | IT_..._013 (partial) |
| メッセージ・異常系 (Messages / abnormal) | ✅ | invalid key, DL/I error (env fault), link failure | UT_..._ABNORMAL_003,004,005 |
| 入力チェック (Input validation) | ✅ | Auth ID required (blank) `2100`; lookup-fail (GE) `6100` | UT_..._ABNORMAL_001,002 |
| 境界値 (Boundary values) | ✅ | Auth ID X(16) len; Amount S9(9)V99 (0/max/negative); Merchant X(50)+Req TS X(26) width; browse end-of-list | UT_..._BOUNDARY_001–007 |
| 連携・インターフェース (Linkage / interface) | ✅ | `EXEC CICS LINK` to OUIMSPA over commarea (INQ/NXT + key, status/segment back); `XCTL` to OCMENU | IT_..._017 |
| 回復・リラン (Recovery / rerun) | ❌ N/A (runtime) | pseudo-conversational read-only; no partial-commit/restart state to recover | — |
| 権限・セキュリティ (Authority / security) | ❌ N/A | OCPAUIN performs no signon/role check itself (roles handled upstream by OCSGNON/menu); no dept/mode gate in this program | — |
| ログ・監査 (Log / audit) | ❌ N/A | only OUIMSPA writes a `DISPLAY` to the system log on a DL/I error (operator-invisible); no audit record written by this inquiry | — |
| 性能・運用 (Performance / operation) | ❌ N/A (runtime) | single-record inquiry; no volume/paper/env instruction on the screen | — |

## Coverage matrix (units → TC)
- **Messages (8/8):** #1→001/002 · #2→ABN_003 · #3→ABN_001 · #4→007–013 · #5→ABN_002 · #6→BND_007 · #7→ABN_005 · #8→ABN_004.
- **Status classifier (5/5 happy):** P→007 · A→008 · D→009 · X→010 · other/UNKNOWN→015.
- **Browse behaviours:** next→011 · from start (bare GN)→012 · reposition after purged anchor→013 · end of list→BND_007.
- **Boundaries (both sides):** Auth ID max16→BND_001 / min1→BND_002 · Amount 0→BND_003 / max 999,999,999.99→BND_004 / negative→BND_005 · long-field width (Merchant50+ReqTS26)→BND_006.
- **Function keys:** ENTER→007 · PF3→004 · PF4→005 · PF8→011 · invalid→ABN_003.
- **Linkage / integrity:** LINK/XCTL contract→017 · read-only idempotence→016.

## Excluded with reason (never test dead/unreachable code)
OUIMSPA supports full CRUD, but **OCPAUIN only ever sends INQ (ENTER) and NXT (PF8)** — the screen is read-only. Therefore these OUIMSPA paths are **unreachable from this app** and are excluded (no TC):
- INSERT (`ADD`/ISRT) — msgs `Pending authorization inserted.`, `Authorization id already exists.` (`II` dup), `Card number is required to insert.`, `Account id must be numeric.`
- UPDATE (`UPD`/GHU+REPL) — msg `Pending authorization updated.`
- DELETE (`DEL`/GHU+DLET) — msg `Pending authorization purged.`
- `1100-VALIDATE-REQUEST`: `Unknown DL/I request function code.` (driver always sends a valid INQ/NXT) and `Authorization id is required.` (ENTER blocks blank before the LINK; NXT does not require a key) — both unreachable via OCPAUIN.
- DL/I statuses `II`(dup)/`GA`(not-positioned)/`FR`(no-space) — arise only in the insert/update paths above.

## Audit result
Command: `python3 scripts/audit_testcase.py cases.json --reg <REG> --root OCPAUIN`
Two candidates are **expected and dismissed** for every ORION-CCMS app (see project memory):
1. `[valid] could not run msg_codes …` — the reg stores XML under `ORION-CCMS/cbl/`, but the audit's `_root_xml` only looks in `main/`|`sub/`. Verified directly: `msg_codes.py OCPAUIN.xml` (and `OUIMSPA.xml`) = **0 codes**. There is no EI/EF/GF code to cover. **Dismissed.**
2. `[vague] 異常系 case but expected cites no message code / STOP` — one per Abnormal TC. ORION raises **literal English strings**, not codes; the verbatim on-screen string quoted in `expected` IS the assertion. **Dismissed (all Abnormal TCs).**
No `[wording]`, `[fold]`, `[ids]`, `[fields]`, `[auto]` findings expected (all COBOL identifiers kept in 備考; "YYYY-MM-DD" avoided per the wording-regex trap — literal ISO timestamps used instead).

### Final audit run (recorded)
```
# viewpoints used (12): データ整合性・冪等性, メッセージ・異常系, 入力チェック, 初期値・デフォルト,
#   境界値, 操作性・キー, 機能・業務フロー, 状態遷移, 画面表示・レイアウト, 表示条件・活性制御,
#   計算・編集ロジック, 連携・インターフェース
# 6 CANDIDATE finding(s) — verify before acting
  [valid] could not run msg_codes (no reg xml / design scripts) — skip code coverage
  [vague] UT_PAUTHINQ_ABNORMAL_001: 異常系 case but expected cites no message code / STOP
  [vague] UT_PAUTHINQ_ABNORMAL_002: 異常系 case but expected cites no message code / STOP
  [vague] UT_PAUTHINQ_ABNORMAL_003: 異常系 case but expected cites no message code / STOP
  [vague] UT_PAUTHINQ_ABNORMAL_004: 異常系 case but expected cites no message code / STOP
  [vague] UT_PAUTHINQ_ABNORMAL_005: 異常系 case but expected cites no message code / STOP
```
**Resolution — all 6 candidates grounded and dismissed:**
- `[valid] could not run msg_codes` — expected: the audit's `_root_xml` only searches `parsed/cobol_xml/{main,sub}/`, but this reg stores XML under `ORION-CCMS/cbl/`. Verified directly: `msg_codes.py OCPAUIN.xml` = 0 codes, `msg_codes.py OUIMSPA.xml` = 0 codes. There is no EI/EF/GF code to cover — the app uses literal English message strings. **Dismissed.**
- `[vague] …001–005` (one per Abnormal TC) — expected: ORION-CCMS raises literal on-screen strings, not codes; each Abnormal TC's `expected` quotes the verbatim string (the assertion): `"Please enter all required fields."`, `"No pending authorization for that id."`, `"Invalid key pressed. Please try again."`, `"DL/I error status=…"`, `"Unable to link to IMS module OUIMSPA."`. No message code exists to cite. **Dismissed.**

No `[wording]`, `[fold]`, `[ids]`, `[fields]`, `[auto]`, `[parity]` findings.

**Judgment dims (hand-reviewed):** spot-checked IT_..._HAPPY_007 (GU→PL-OK→4000/4100 'P'→PENDING, msg 6000), UT_..._ABNORMAL_002 (GU→'GE'→6100 msg via CA-ERR-MSG), UT_..._BOUNDARY_007 (GN→'GB'→6200 end-of-list) — every 期待結果 traces to the cited source line; steps reproducible from F-STD; 観点表 complete vs the screen/sub spec.

**Build/verify:** `OCPAUIN_TestCases.xlsx` built with `--lang en` — 30 rows, header count 30, split Normal 18 / Abnormal 5 / Boundary 7, author `modernizeX`, 中項目 merges intact.
