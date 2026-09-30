# OCTTYP — Transaction Type Maintenance — Test-case NOTES

- **App / kind:** OCTTYP, **UI (CICS/BMS)** — resolved by `design_evidence.py` (a UI screen-design doc exists; owns BMS mapset `MTTYP` / map `MTTYPA`). Decided by EVIDENCE, not `root_type`.
- **CICS transaction:** `ORTT` · **mapset/map:** `MTTYP` / `MTTYPA` (24×80) · **file:** `TTYPFILE` (VSAM KSDS, Create/Read/Update).
- **Business function (機能名):** Transaction Type Maintenance — look up a transaction type by its 2-character code and add or change its description.
- **AREA token:** `TTYPMNT` (unique across the run; distinct from `TCATMNT` / `DGRPMNT`). **ID scheme:** `{IT|UT}_TTYPMNT_{HAPPY|ABNORMAL|BOUNDARY}_{NNN}`.
- **Evidence read (ground truth):**
  - REG AST `parsed/cobol_xml/ORION-CCMS/cbl/OCTTYP.xml` (431 lines) — every message, paragraph and AID key verified ACTIVE on the AST.
  - Copybooks: `RTTYP` (record layout TT-CD X(2) + TT-DESC X(50) + FILLER X(8), LRECL 60), `WMSG` (shared `WS-MSG-INVALID-KEY`, `WS-MSG-TEXT`), `WHEAD` (title `ORION CREDIT CARD MANAGEMENT SYSTEM`), `WCONST` (`WS-TTYPFILE`='TTYPFILE', `REC-FOUND`/`REC-NOT-FOUND` flags, `WS-RESP-CD`), `KCOMM` (`ORION-COMMAREA`: `CA-WORK-AREA`, `CA-PGM-CONTEXT`/`CA-FIRST-ENTER`, `CA-USER-TYPE`, `CA-FROM-*`).
  - BMS map `bms/MTTYP.bms` (via `bms_maps.json`): `TTCD` input len **2**, `TTDESC` input len **50**, both `UNPROT/IC/FSET`; `ERRMSG` len 78 (RED); header/display fields `ASKIP/NORM`.
  - Design suite `UI/OCTTYP_TranTypeMaint_ScreenDesign.md`.
  - Real seed `input/10/ORION/ORION-CCMS/data/TTYPFILE.txt` (7 records).

## Assumption (non-interactive approval gate)

This run is non-interactive. The 観点表 (viewpoint table) and coverage matrix below are **recorded as an approved assumption** and the case set was generated directly from them. If a reviewer disagrees with any viewpoint decision, adjust `cases.json` and rebuild — the viewpoint rows are cheap to change; the ID counters stay stable.

## Message convention (ORION-CCMS — critical)

OCTTYP raises **literal English message strings, not EI/EF/GF/ER codes** — `msg_codes.py OCTTYP.xml` returns **0 codes** (confirmed by direct run). The design doc §3 check table lists **only 1** message; the **source/AST is ground truth** and carries **11 distinct on-screen messages** (9 program-specific `WS-M-*` + shared `WS-MSG-INVALID-KEY` + the `9500-ABEND-RTN` full-screen text). Every message below has a dedicated TC. All assertions quote the verbatim English string (in backticks).

| # | Data-name | Verbatim string | Type | Raised at | Covered by |
|---|---|---|---|---|---|
| 1 | WS-M-PROMPT | `Enter a type code and press ENTER.` | I | 1000-SEND-INITIAL (initial / PF4) | HAPPY_001, HAPPY_002, HAPPY_004 |
| 2 | WS-M-FOUND | `Type found. Change text, PF5 to update.` | I | 2100-LOOKUP REC-FOUND | HAPPY_007, HAPPY_010, BOUNDARY_002 |
| 3 | WS-M-NEW | `New type. Enter text, PF5 to add.` | I | 2100-LOOKUP REC-NOT-FOUND | HAPPY_008, BOUNDARY_001/003 |
| 4 | WS-M-ADDED | `Transaction type added.` | C | 3600-ADD-TTYP WRITE NORMAL | HAPPY_009/011, BOUNDARY_004/005 |
| 5 | WS-M-UPDATED | `Transaction type updated.` | C | 3500-UPDATE-TTYP REWRITE NORMAL | HAPPY_010/012/013 |
| 6 | WS-M-CD-REQ | `Type code is required.` | E | 2100-LOOKUP (code blank / low-values) | ABNORMAL_001, ABNORMAL_007 |
| 7 | WS-M-DESC-REQ | `Description is required.` | E | 2300-SAVE (description blank) | ABNORMAL_002 |
| 8 | WS-M-KEY-FIRST | `Enter a type code and press ENTER first.` | E | 2300-SAVE guard (state still 'K') | ABNORMAL_005 |
| 9 | WS-M-SAVE-ERR | `Error saving the transaction type.` | E | 3600 WRITE DUPREC / OTHER, 3500 REWRITE non-normal | ABNORMAL_004 |
| 10 | WS-MSG-INVALID-KEY | `Invalid key pressed. Please try again.` | E | 2000-PROCESS-INPUT WHEN OTHER | ABNORMAL_003 |
| 11 | (SEND TEXT) | `OCTTYP: unrecoverable file error. Contact support.` | E | 9500-ABEND-RTN (READ / READ UPDATE WHEN OTHER) | ABNORMAL_006 |

## Design-vs-source discrepancies (source wins)

1. **PF5 is real; the on-screen legend and §1.2 omit it.** The BMS legend literal (line 24) is `ENTER=Process  PF3=Back  PF4=Clear` and design §1.2 lists only ENTER/PF3/PF4 — but §4 event row 4 **and** the source implement `DFHPF5 → 2300-SAVE` (validate description, then add or update). PF5 is exercised in every add/update TC. ENTER is labelled "Process" on-screen but actually performs the key **lookup** (read), not the save.
2. **No DELETE.** Design §1.2 says "add, change or **remove**" and §5 shows an "On delete: delete record" column, but there is **no `EXEC CICS DELETE`** and no AID key wired to a delete path (`2000-PROCESS-INPUT` handles only ENTER/PF3/PF4/PF5). File CRUD is **Create/Read/Update only** (§1.3 confirms Delete = "-"; `files.json` io_operations for `WS-TTYPFILE` across ALL programs = READ/REWRITE/WRITE, never DELETE). → **Excluded, not tested** (dead/non-existent).
3. **§3 lists 1 message; source has 11.** Grounded the full set from the AST (message table above).
4. **No sub-program calls.** `design_evidence.py` lists member program-designs `OUANLIN / OUDATE / OUSTMIN`, but OCTTYP makes **no `EXEC CICS LINK`** — its only transfer is `EXEC CICS XCTL PROGRAM('OCMENU')` on PF3. Date/time come from `EXEC CICS ASKTIME/FORMATTIME`, not `OUDATE`. Those sub-programs are **not part of OCTTYP's behaviour** and are out of scope for this set.
5. **`3500-UPDATE-TTYP` READ-UPDATE NOTFND → ADD fallback is effectively unreachable.** After a found lookup, PF5 re-reads the record with UPDATE; if that returns NOTFND it falls through to add. But **no program ever deletes a TTYPFILE record** (see #2), so a record found at lookup cannot vanish before the immediately-following PF5. This is defensive code with no reachable trigger in normal operation → **not tested** (no dead-path TC). Same reasoning excludes the DUPREC-only-via-race half of `WS-M-SAVE-ERR` from a "normal" TC — it is covered as a contention/abnormal case (ABNORMAL_004) with a fault-injection note.

## Fixture — F-STD (shared)

`TTYPFILE` (Transaction-type file, VSAM KSDS) loaded from the real seed `data/TTYPFILE.txt` via job `LOADTTYP` (IDCAMS REPRO). Record layout `RTTYP` = Type Cd X(2) + Desc X(50) + Filler X(8) (LRECL 60). Key = Type Cd.

| Type Cd | Description |
|---|---|
| 01 | PURCHASE |
| 02 | PAYMENT |
| 03 | CASH ADVANCE |
| 04 | INTEREST |
| 05 | FEE |
| 06 | REFUND |
| 07 | ADJUSTMENT |

Codes used for "new" (not on file) tests: `08`, `09`, `10`, `AB`, `99`, `1 ` (single char). Operator is signed on (via the Sign On screen), transaction `ORTT` started, screen `MTTYPA` displayed at the initial key-entry state ('K').

## 観点表 — viewpoint table (every base UI viewpoint decided)

| # | 観点 (中項目) | Applicable? | Evidence | Planned TC(s) |
|---|---|---|---|---|
| U1 | 画面表示・レイアウト | ✅ | §2.1 mockup, BMS MTTYPA, 1000-SEND-INITIAL | HAPPY_001 |
| U2 | 初期値・デフォルト | ✅ | fields blank + cursor at Type Code + prompt + state 'K' on open | HAPPY_002 |
| U3 | 表示条件・活性制御 | ❌ | both input fields are always unprotected (BMS `UNPROT/IC/FSET`); no conditional enable/disable — behaviour is driven by the K/E/N state and cursor moves (see 状態遷移), not by field activation | — |
| U4 | 文字・書式表示 | ✅ | header ISO date (`FORMATTIME DATESEP('-') YYYYMMDD`) + `HH:MM:SS` time; BMS colours (title YELLOW, captions BLUE, input GREEN, message RED) | HAPPY_003 |
| U5 | 入力チェック | ✅ | 2100-LOOKUP (type code required), 2300-SAVE (description required) — per field × per check; note **no format/range check on the code** | ABNORMAL_001, 002 (+ 007 empty-transmit) |
| U6 | 操作性・キー | ✅ | EIBAID: ENTER/PF3/PF4/PF5 + WHEN OTHER | HAPPY_004 (PF4); HAPPY_014 (PF3); ABNORMAL_003 (invalid key); ENTER/PF5 across HAPPY_007–012 |
| U7 | 状態遷移 | ✅ | K→E/N (ENTER), N→E (add), PF4→K, pseudo-conv commarea persistence + first-entry routing (EIBCALEN=0 / CA-PGM-CONTEXT) | HAPPY_005, HAPPY_006 |
| U8 | 機能・業務フロー | ✅ | §1.2 functions — one happy per classifier (lookup-existing / lookup-new / add / change) | HAPPY_007, 008, 009, 010 |
| U9 | 計算・編集ロジック | ❌ | no computation/derivation; the code is a raw X(2) key (no numeric parse, no zero-pad, unlike OCTCAT); only header date/time formatting, covered under 文字・書式表示 | — |
| U10 | 出力・帳票 | ✅ | 3600-ADD-TTYP column-level write (Cd/Desc + initialised filler); auto-fill of Desc from file on lookup (shown in HAPPY_007) | HAPPY_011 |
| U11 | データ整合性・冪等性 | ✅ | REWRITE preserves key/filler; re-save after add is idempotent (state flips E) | HAPPY_012, 013 |
| U12 | 排他・同時実行 | ✅ *(runtime)* | 3600 WRITE `DUPREC`, 3500 REWRITE contention → save-error | ABNORMAL_004 |
| U13 | メッセージ・異常系 | ✅ | 11 literal messages (table above) — key-first guard, abend text | ABNORMAL_005, 006 (+ all others) |
| U14 | 境界値 | ✅ | code field length X(2) (min 1 / max 2, exact-match no zero-pad, any 2 chars); desc X(50) (min 1 / max 50) | BOUNDARY_001–005 |
| U15 | 回復・リラン | ✅ | 2050-RECEIVE MAPFAIL → empty transmit treated as blank input | ABNORMAL_007 |
| U16 | 権限・セキュリティ | ❌ | OCTTYP does not inspect `CA-USER-TYPE` (admin/normal); sign-on enforced upstream (Sign On screen / menu). No dept/mode gate in source | — |
| U17 | ログ・監査 | ❌ | OCTTYP writes no audit/log record; only TTYPFILE is touched | — |
| U18 | 連携・インターフェース | ✅ | PF3 XCTL to OCMENU (commarea); RETURN TRANSID ORTT pseudo-conv | HAPPY_014 |
| U19 | 性能・運用 | ❌ | no volume/printer/paper aspects; single-record maintenance; pseudo-conv timing is runtime-only | — |
| — | (app-specific) DELETE function | ❌ | no `EXEC CICS DELETE`, no AID key wired to it — non-existent (see discrepancy #2) | — |
| — | (app-specific) update→add NOTFND fallback | ❌ | effectively unreachable (nothing deletes TTYPFILE records; see discrepancy #5) — no dead-path TC | — |
| — | (app-specific) sub-program link | ❌ | no `EXEC CICS LINK`; only XCTL to OCMENU (see discrepancy #4) | folded into HAPPY_014 |

## Coverage matrix (summary)

- **Messages:** 11 / 11 covered (each has a dedicated TC; see message table).
- **Input fields × checks:** Type Code (required / min-length 1 / max-length 2 / any-2-chars, no format-range check) and Description (required / max-length 50 / min-length 1) — all covered.
- **Classifier happy flows:** lookup-existing, lookup-new, add, change — all covered (HAPPY_007–010).
- **AID keys:** ENTER (HAPPY_007/008 + saves), PF5 (HAPPY_009/010 + boundaries), PF3 (HAPPY_014), PF4 (HAPPY_004), unsupported (ABNORMAL_003) — all covered.
- **State machine:** K→E/N, N→E, PF4→K, pseudo-conv routing (EIBCALEN=0 / context) — HAPPY_005, 006.
- **Excluded viewpoints:** U3 (no conditional field control), U9 (no computation), U16 (no in-program authority), U17 (no audit log), U19 (runtime-only), DELETE (non-existent), update→add fallback (unreachable), sub-program LINK (non-existent) — each with a reason above.

## TC totals

- **Total: 26 TC** — Normal (正常系) **14**, Abnormal (異常系) **7**, Boundary (境界値) **5**.

## Audit result (`audit_testcase.py cases.json --reg <REG> --root OCTTYP`)

7 candidates — **all grounded and dismissed** (the two documented ORION-CCMS classes):

1. **`[valid] could not run msg_codes`** (1) — the reg stores XML under `parsed/cobol_xml/ORION-CCMS/cbl/`, but the audit's `_root_xml` only probes `main/`|`sub/`, so it cannot self-run `msg_codes`. **Dismissed:** ran `msg_codes.py OCTTYP.xml` directly → **0 codes** (this app uses literal English messages, no EI/EF/GF/ER). No code-coverage gap exists.
2. **`[vague] … 異常系 case but expected cites no message code / STOP`** (6 of the 7 Abnormal TCs) — the check wants an `EI/EF`-style code or STOP/abort/ABEND in the expected. **Dismissed:** OCTTYP has no message codes; the **verbatim on-screen English string** (e.g. `Type code is required.`) is the assertion and is quoted in each expected. (ABNORMAL_006 was not flagged because its abend expected contains "aborts".)

No `[wording]` (code-leak), `[fold]`, `[ids]` (duplicate), `[fields]` (empty), `[parity]`, or `[auto]` candidates expected. All 中項目 are canonical viewpoint labels. Judgment dims hand-reviewed: 3 spot-checked TCs (HAPPY_010 → 3500-UPDATE-TTYP REWRITE; ABNORMAL_005 → 2300-SAVE state guard; BOUNDARY_001 → TTCD X(2) exact-match no zero-pad) all trace to the cited source lines.

## Deliverables

- `NOTES.md` (this file)
- `cases.json` (editable source of truth — 26 cases)
- `OCTTYP_TestCases.xlsx` (built with `build_testcase_xlsx.py --lang en`; author `modernizeX`; count cell = 26)
