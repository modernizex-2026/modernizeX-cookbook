# OCTCAT — Transaction Category Maintenance — Test-case NOTES

- **App / kind:** OCTCAT, **UI (CICS/BMS)** — resolved by `design_evidence.py` (a UI screen-design doc exists; owns BMS mapset `MTCAT` / map `MTCATA`). Decided by EVIDENCE, not `root_type`.
- **CICS transaction:** `ORTC` · **mapset/map:** `MTCAT` / `MTCATA` (24×80) · **file:** `TCATFILE` (VSAM KSDS, Create/Read/Update).
- **Business function (機能名):** Transaction Category Maintenance — look up a transaction category by its key (Type Code + 4-digit Category Code) and add or change its description.
- **AREA token:** `TCATMNT` (unique across the run; distinct from `DGRPMNT`). **ID scheme:** `{IT|UT}_TCATMNT_{HAPPY|ABNORMAL|BOUNDARY}_{NNN}`.
- **Evidence read (ground truth):**
  - REG AST `parsed/cobol_xml/ORION-CCMS/cbl/OCTCAT.xml` (545 lines) + raw `cbl/OCTCAT.cbl` (439 lines, cross-checked all messages/paragraphs/AID keys are ACTIVE on the AST).
  - Copybooks: `RTCAT` (record layout), `WMSG` (shared `WS-MSG-INVALID-KEY`, `WS-MSG-TEXT`), `WHEAD` (title `ORION CREDIT CARD MANAGEMENT SYSTEM`), `WCONST` (`WS-TCATFILE`='TCATFILE', `REC-FOUND` flag), `KCOMM` (`CA-WORK-AREA`, `CA-FIRST-ENTER`, `CA-USER-TYPE`).
  - BMS map `bms/MTCAT.bms` (field lengths/attributes) — `TCTYPE` len 2, `TCCD` len 4, `TCDESC` len 50, all `UNPROT/IC/FSET`.
  - Design suite `UI/OCTCAT_TranCategoryMaint_ScreenDesign.md`.
  - Real seed `input/10/ORION/ORION-CCMS/data/TCATFILE.txt` (7 records).

## Assumption (non-interactive approval gate)

This run is non-interactive. The 観点表 (viewpoint table) and coverage matrix below are **recorded as an approved assumption** and the case set was generated directly from them. If a reviewer disagrees with any viewpoint decision, adjust `cases.json` and rebuild — the viewpoint rows are cheap to change; the ID counters stay stable.

## Message convention (ORION-CCMS — critical)

OCTCAT raises **literal English message strings, not EI/EF/GF/ER codes** — `msg_codes.py OCTCAT.xml` returns **0 codes** (confirmed by direct run). The design doc §3 check table lists **only 1** message; the **source/AST is ground truth** and carries **12 distinct on-screen messages** (10 program-specific `WS-M-*` + shared `WS-MSG-INVALID-KEY` + the `9500-ABEND-RTN` full-screen text). Every message below has a dedicated TC. All assertions quote the verbatim English string (in backticks).

| # | Data-name | Verbatim string | Type | Raised at | Covered by |
|---|---|---|---|---|---|
| 1 | WS-M-PROMPT | `Enter type + category code and press ENTER.` | I | 1000-SEND-INITIAL (initial / PF4) | HAPPY_001, HAPPY_002, HAPPY_004 |
| 2 | WS-M-FOUND | `Category found. Change text, PF5 to update.` | I | 2100-LOOKUP REC-FOUND | HAPPY_006, HAPPY_003/013, BOUNDARY_003 |
| 3 | WS-M-NEW | `New category. Enter text, PF5 to add.` | I | 2100-LOOKUP REC-NOT-FOUND | HAPPY_007, BOUNDARY_002/004 |
| 4 | WS-M-ADDED | `Transaction category added.` | C | 3600-ADD-TCAT WRITE NORMAL | HAPPY_008/011, BOUNDARY_001/005/006 |
| 5 | WS-M-UPDATED | `Transaction category updated.` | C | 3500-UPDATE-TCAT REWRITE NORMAL | HAPPY_009/012/013 |
| 6 | WS-M-TYPE-REQ | `Type code is required.` | E | 6100-VALIDATE-KEY (type blank) | ABNORMAL_001, ABNORMAL_009 |
| 7 | WS-M-CD-NUM | `Category code must be numeric.` | E | 6100 → 6000-PARSE-NUM (blank / non-digit) | ABNORMAL_002, ABNORMAL_003 |
| 8 | WS-M-DESC-REQ | `Description is required.` | E | 2300-SAVE (description blank) | ABNORMAL_004 |
| 9 | WS-M-KEY-FIRST | `Enter the key and press ENTER first.` | E | 2300-SAVE guard (state still 'K') | ABNORMAL_007 |
| 10 | WS-M-SAVE-ERR | `Error saving the transaction category.` | E | 3600 WRITE DUPREC / 3500 REWRITE non-normal | ABNORMAL_006 |
| 11 | WS-MSG-INVALID-KEY | `Invalid key pressed. Please try again.` | E | 2000-PROCESS-INPUT WHEN OTHER | ABNORMAL_005 |
| 12 | (SEND TEXT) | `OCTCAT: unrecoverable file error. Contact support.` | E | 9500-ABEND-RTN (READ / READ UPDATE WHEN OTHER) | ABNORMAL_008 |

## Design-vs-source discrepancies (source wins)

1. **PF5 is real; the on-screen legend and §1.2 omit it.** The BMS legend literal (line 24) is `ENTER=Process  PF3=Back  PF4=Clear` and design §1.2 lists only ENTER/PF3/PF4 — but §4 event row 4 **and** the source implement `DFHPF5 → 2300-SAVE` (validate description, then add or update). PF5 is exercised in every add/update TC. ENTER is labelled "Process" on-screen but actually performs the key **lookup** (read), not the save.
2. **No DELETE.** Design §1.2 says "add, change or **remove**" and §5 shows an "On delete: delete record" column, but there is **no `EXEC CICS DELETE`** and no AID key wired to a delete path (`2000-PROCESS-INPUT` handles only ENTER/PF3/PF4/PF5). File CRUD is **Create/Read/Update only** (§1.3 confirms Delete = "-"). → **Excluded, not tested** (dead/non-existent).
3. **§3 lists 1 message; source has 12.** Grounded the full set from the AST (message table above).
4. **No sub-program calls.** `design_evidence.py` lists member program-designs `OUANLIN / OUDATE / OUSTMIN`, but OCTCAT makes **no `EXEC CICS LINK`** — its only transfer is `EXEC CICS XCTL PROGRAM('OCMENU')` on PF3. Date/time come from `EXEC CICS ASKTIME/FORMATTIME`, not `OUDATE`. Those sub-programs are **not part of OCTCAT's behaviour** and are out of scope for this set.
5. **`WS-NC-DIGITS > 4` guard is dead.** `6100-VALIDATE-KEY` tests `WS-INVALID OR WS-NC-DIGITS > 4`, but `6000-PARSE-NUM` scans only `WS-NC-LEN = 4` characters, so at most 4 digits are ever counted — the `> 4` half can never be true. Category over-length is instead prevented by the map field length (4). No TC is written for that dead half; the real rejection (non-digit / blank) is ABNORMAL_002/003.

## Fixture — F-STD (shared)

`TCATFILE` (Transaction-category file, VSAM KSDS) loaded from the real seed `data/TCATFILE.txt`. Record layout `RTCAT` = Type Cd X(2) + Cat Cd 9(4) + Desc X(50) + Filler X(4) (LRECL 60). Key = Type Cd + Cat Cd.

| Type Cd | Cat Cd | Description |
|---|---|---|
| 01 | 0001 | RETAIL PURCHASE |
| 01 | 0002 | RESTAURANT |
| 01 | 0003 | TRAVEL AND LODGING |
| 02 | 0001 | ONLINE PAYMENT |
| 03 | 0001 | ATM WITHDRAWAL |
| 04 | 0001 | PURCHASE INTEREST |
| 05 | 0001 | LATE PAYMENT FEE |

Keys used for "new" (not on file) tests: `09/0001`, `06/0001`, `08/0001`, `02/0002`, `07/0000`, `01/9999`, `9 /0001` (1-char type), `09/0002`.
Operator is signed on (via the Sign On screen), transaction `ORTC` started, screen `MTCATA` displayed at the initial key-entry state ('K').

## 観点表 — viewpoint table (every base UI viewpoint decided)

| # | 観点 (中項目) | Applicable? | Evidence | Planned TC(s) |
|---|---|---|---|---|
| U1 | 画面表示・レイアウト | ✅ | §2.1 mockup, BMS MTCATA, 1000-SEND-INITIAL | HAPPY_001 |
| U2 | 初期値・デフォルト | ✅ | fields blank + cursor at Type Code + prompt + state 'K' on open | HAPPY_002 |
| U3 | 表示条件・活性制御 | ❌ | all three input fields are always unprotected (BMS `UNPROT/IC/FSET`); no conditional enable/disable — behaviour is driven by the K/E/N state and cursor moves (see 状態遷移), not by field activation | — |
| U4 | 文字・書式表示 | ✅ | category redisplayed zero-padded 4-digit (`WS-CD-ED` PIC 9(04)); header ISO date + time | HAPPY_003 |
| U5 | 入力チェック | ✅ | 6100-VALIDATE-KEY (type required, category numeric), 2300-SAVE (description required) — per field × per check | ABNORMAL_001–004 |
| U6 | 操作性・キー | ✅ | EIBAID: ENTER/PF3/PF4/PF5 + WHEN OTHER | HAPPY_004 (PF4); ABNORMAL_005 (invalid key); ENTER/PF5 across HAPPY_006–009 |
| U7 | 状態遷移 | ✅ | K→E/N (ENTER), N→E (add), PF4→K, pseudo-conv commarea persistence | HAPPY_005 |
| U8 | 機能・業務フロー | ✅ | §1.2 functions — one happy per classifier (lookup-existing / lookup-new / add / update) | HAPPY_006, 007, 008, 009 |
| U9 | 計算・編集ロジック | ✅ | 6000-PARSE-NUM space-skipping + leading-zero normalisation | HAPPY_010 (+ BOUNDARY_002/003) |
| U10 | 出力・帳票 | ✅ | 3600-ADD-TCAT column-level write (type/cat/desc + filler) | HAPPY_011 |
| U11 | データ整合性・冪等性 | ✅ | REWRITE preserves key/filler; re-save after add is idempotent (state flips E) | HAPPY_012, 013 |
| U12 | 排他・同時実行 | ✅ *(runtime)* | 3600 WRITE `DUPREC`, 3500 REWRITE contention → save-error | ABNORMAL_006 |
| U13 | メッセージ・異常系 | ✅ | 12 literal messages (table above) — key-first guard, abend text | ABNORMAL_007, 008 (+ all others) |
| U14 | 境界値 | ✅ | field lengths X(2)/9(4)/X(50); category value 0000..9999, digit-count 1..4 | BOUNDARY_001–006 |
| U15 | 回復・リラン | ✅ | 2050-RECEIVE MAPFAIL → empty input treated as blank; update→add fallback on NOTFND | ABNORMAL_009 |
| U16 | 権限・セキュリティ | ❌ | OCTCAT does not inspect `CA-USER-TYPE` (admin/normal); sign-on enforced upstream (Sign On screen / menu). No dept/mode gate in source | — |
| U17 | ログ・監査 | ❌ | OCTCAT writes no audit/log record; only TCATFILE is touched | — |
| U18 | 連携・インターフェース | ✅ | PF3 XCTL to OCMENU (commarea); RETURN TRANSID ORTC pseudo-conv | HAPPY_014 |
| U19 | 性能・運用 | ❌ | no volume/printer/paper aspects; single-record maintenance; pseudo-conv timing is runtime-only | — |
| — | (app-specific) DELETE function | ❌ | no `EXEC CICS DELETE`, no AID key wired to it — non-existent (see discrepancy #2) | — |
| — | (app-specific) sub-program link | ❌ | no `EXEC CICS LINK`; only XCTL to OCMENU (see discrepancy #4) | folded into HAPPY_014 |

## Coverage matrix (summary)

- **Messages:** 12 / 12 covered (each has a dedicated TC; see message table).
- **Input fields × checks:** TCTYPE (required / min-length 1 / max-length 2), TCCD (numeric / blank / min-value 0000 / max-value 9999 / single-digit), TCDESC (required / max-length 50 / min-length 1) — all covered.
- **Classifier happy flows:** lookup-existing, lookup-new, add, update — all covered (HAPPY_006–009).
- **AID keys:** ENTER (HAPPY_006/007 + saves), PF5 (HAPPY_008/009 + boundaries), PF3 (HAPPY_014), PF4 (HAPPY_004), unsupported (ABNORMAL_005) — all covered.
- **Excluded viewpoints:** U3 (no conditional field control), U16 (no in-program authority), U17 (no audit log), U19 (runtime-only), DELETE (non-existent), sub-program LINK (non-existent) — each with a reason above.

## TC totals

- **Total: 29 TC** — Normal (正常系) **14**, Abnormal (異常系) **9**, Boundary (境界値) **6**.

## Audit result (`audit_testcase.py cases.json --reg <REG> --root OCTCAT`)

9 candidates — **all grounded and dismissed** (the two documented ORION-CCMS classes):

1. **`[valid] could not run msg_codes`** (1) — the reg stores XML under `parsed/cobol_xml/ORION-CCMS/cbl/`, but the audit's `_root_xml` only probes `main/`|`sub/`, so it cannot self-run `msg_codes`. **Dismissed:** ran `msg_codes.py OCTCAT.xml` directly → **0 codes** (this app uses literal English messages, no EI/EF/GF/ER). No code-coverage gap exists.
2. **`[vague] … 異常系 case but expected cites no message code / STOP`** (8 of the 9 Abnormal TCs) — the check wants an `EI/EF`-style code or STOP/abort/ABEND in the expected. **Dismissed:** OCTCAT has no message codes; the **verbatim on-screen English string** (e.g. `Type code is required.`) is the assertion and is quoted in each expected. (ABNORMAL_008 was not flagged because its abend expected contains "aborts".)

No `[wording]` (code-leak), `[fold]`, `[ids]` (duplicate), `[fields]` (empty), `[parity]`, or `[auto]` candidates expected. All 中項目 are canonical viewpoint labels. Judgment dims hand-reviewed: 3 spot-checked TCs (HAPPY_009 → 3500-UPDATE-TCAT REWRITE; ABNORMAL_007 → 2300-SAVE state guard; BOUNDARY_002 → 6000-PARSE-NUM digits>0) all trace to the cited source lines.

## Deliverables

- `NOTES.md` (this file)
- `cases.json` (editable source of truth — 29 cases)
- `OCTCAT_TestCases.xlsx` (built with `build_testcase_xlsx.py --lang en`; author `modernizeX`; count cell = 29)
