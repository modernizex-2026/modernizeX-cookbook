# OCDGRP — Disclosure Group Maintenance — Test-case NOTES

- **App / kind:** OCDGRP (`app_013`), **UI (CICS/BMS)** — resolved by `design_evidence.py` (a UI screen-design doc exists; owns BMS map `MDGRP/MDGRPA`).
- **CICS transaction:** `ORDG` · **mapset/map:** `MDGRP` / `MDGRPA` (24×80) · **file:** `DGRPFILE` (VSAM KSDS, Create/Read/Update).
- **Business function (機能名):** Disclosure Group Maintenance — look up a disclosure/interest group by its key (Account Group + Type Code + Category Code) and add or change its annual interest rate.
- **AREA token:** `DGRPMNT` (unique across the run). **ID scheme:** `{IT|UT}_DGRPMNT_{HAPPY|ABNORMAL|BOUNDARY}_{NNN}`.
- **Evidence read (ground truth):**
  - REG AST `parsed/cobol_xml/ORION-CCMS/cbl/OCDGRP.xml` (748 lines) + raw `cbl/OCDGRP.cbl` (543 lines, cross-checked all messages/paragraphs/AID keys are ACTIVE on the AST).
  - Copybooks: `RDGRP` (record layout), `WMSG` (shared `WS-MSG-INVALID-KEY`), `WHEAD` (title), `WCONST` (`WS-DGRPFILE`='DGRPFILE', `REC-FOUND` flag), `KCOMM` (`CA-WORK-AREA`, `CA-FIRST-ENTER`).
  - BMS map `parsed/bms/ORION-CCMS/bms/MDGRP.json` (field lengths/attributes) + `bms_mockup.py --render MDGRPA`.
  - Design suite `UI/OCDGRP_DisclosureGroupMaint_ScreenDesign.md`.
  - Real seed `input/10/ORION/ORION-CCMS/data/DGRPFILE.txt` (5 records).

## Assumption (non-interactive approval gate)

This run is non-interactive. The 観点表 (viewpoint table) and coverage matrix below are **recorded as an approved assumption** and the case set was generated directly from them. If a reviewer disagrees with any viewpoint decision, adjust `cases.json` and rebuild — the viewpoint rows are cheap to change; the ID counters stay stable.

## Message convention (ORION-CCMS — critical)

OCDGRP raises **literal English message strings, not EI/EF/GF codes** — `msg_codes.py OCDGRP.xml` returns **0 codes** (confirmed by direct run). The design doc §3 check table lists **only 1** message; the **source/AST is ground truth** and carries **13 distinct on-screen messages** (11 program-specific `WS-M-*` + shared `WS-MSG-INVALID-KEY` + the `9500-ABEND-RTN` full-screen text). Every message below has a dedicated TC. All assertions quote the verbatim English string (in backticks).

| # | Data-name | Verbatim string | Type | Raised at | Covered by |
|---|---|---|---|---|---|
| 1 | WS-M-PROMPT | `Enter group, type, category and press ENTER.` | I | 1000-SEND-INITIAL (initial / PF4) | HAPPY_001, HAPPY_012 |
| 2 | WS-M-FOUND | `Group found. Change rate, PF5 to update.` | I | 2100-LOOKUP REC-FOUND | HAPPY_002, HAPPY_003 |
| 3 | WS-M-NEW | `New group. Enter rate, PF5 to add.` | I | 2100-LOOKUP REC-NOT-FOUND | HAPPY_004 (+ boundary lookups) |
| 4 | WS-M-ADDED | `Disclosure group added.` | C | 3600-ADD-DGRP WRITE NORMAL | HAPPY_005/008/010, BOUNDARY_004/007/008 |
| 5 | WS-M-UPDATED | `Disclosure group updated.` | C | 3500-UPDATE REWRITE NORMAL | HAPPY_006/009/010/013 |
| 6 | WS-M-GRP-REQ | `Account group id is required.` | E | 6100-VALIDATE-KEY (group blank) | ABNORMAL_001, ABNORMAL_009 |
| 7 | WS-M-TYPE-REQ | `Type code is required.` | E | 6100-VALIDATE-KEY (type blank) | ABNORMAL_002 |
| 8 | WS-M-CAT-NUM | `Category code must be numeric.` | E | 6100 → 6000-PARSE-NUM | ABNORMAL_003 |
| 9 | WS-M-RATE-BAD | `Interest rate is not a valid number.` | E | 6200 → 6300-PARSE-RATE (PF5 only) | ABNORMAL_004, BOUNDARY_005/006/009 |
| 10 | WS-M-KEY-FIRST | `Enter the key and press ENTER first.` | E | 2300-SAVE guard (state still 'K') | ABNORMAL_006 |
| 11 | WS-M-SAVE-ERR | `Error saving the disclosure group.` | E | 3600 WRITE DUPREC / 3500 REWRITE non-normal | ABNORMAL_008 |
| 12 | WS-MSG-INVALID-KEY | `Invalid key pressed. Please try again.` | E | 2000-PROCESS-INPUT WHEN OTHER | ABNORMAL_005 |
| 13 | (SEND TEXT) | `OCDGRP: unrecoverable file error. Contact support.` | E | 9500-ABEND-RTN (I/O WHEN OTHER) | ABNORMAL_007 |

## Design-vs-source discrepancies (source wins)

1. **PF5 is real.** Design §1.2 function-key table omits PF5, but §4 event row 4 **and** the source implement `DFHPF5 → 2300-SAVE` (save/add/update). PF5 is covered in every add/update TC.
2. **No DELETE.** Design §1.2 says "add, change or **remove**" and §5 shows an "On delete: delete record" column, but there is **no `EXEC CICS DELETE`** and no PF key wired to a delete path. The file CRUD is **Create/Read/Update only** (§1.3 confirms Delete = "-"). → **Excluded, not tested** (dead/non-existent).
3. **§3 lists 1 message; source has 13.** Grounded the full set from the AST (table above).

## Fixture — F-STD (shared)

`DGRPFILE` (Disclosure group file, VSAM KSDS) loaded from the real seed `data/DGRPFILE.txt` via job `LOADDGRP` (IDCAMS REPRO). Key = Account Group X(10) + Type Code X(2) + Category 9(4). Record = key + interest rate S9(4)V99 + 28-byte filler (LRECL 50).

| Acct Group | Type Cd | Cat Cd | Int Rate |
|---|---|---|---|
| GOLD | 01 | 0001 | 19.99 |
| PLATINUM | 01 | 0001 | 14.99 |
| STANDARD | 01 | 0001 | 24.99 |
| GOLD | 03 | 0001 | 22.99 |
| PLATINUM | 03 | 0001 | 19.99 |

Keys used for "new" tests (not on file): `SILVER/01/0001`, `SILVER/02/0007`, `SILVER/03/0001`, `GOLD/02..09/0001`, `GOLDPLATIN/03/9999`, etc.
Operator is signed on (via the Sign On screen), transaction `ORDG` started, screen `MDGRPA` displayed at the initial key-entry state ('K').

## 観点表 — viewpoint table (every base UI viewpoint decided)

| # | 観点 (中項目) | Applicable? | Evidence | Planned TC(s) |
|---|---|---|---|---|
| U1 | 画面表示・レイアウト | ✅ | §2.1 mockup, BMS MDGRPA, 1000-SEND-INITIAL | HAPPY_001 |
| U2 | 初期値・デフォルト | ✅ (folded) | fields blank + prompt + state 'K' on open; PF4 reset | covered in HAPPY_001, HAPPY_012 |
| U3 | 表示条件・活性制御 | ❌ | all four input fields are always unprotected (BMS `UNPROT/IC/FSET`); no conditional enable/disable — behaviour is driven by the K/E/N state (see 状態遷移) | — |
| U4 | 文字・書式表示 | ✅ | rate edit mask ZZZ9.99 (right-justified, 2 dp) | HAPPY_002 |
| U5 | 入力チェック | ✅ | 6100-VALIDATE-KEY, 6200/6300-VALIDATE-RATE — per field × per check | ABNORMAL_001–004 (+ boundaries) |
| U6 | 操作性・キー | ✅ | EIBAID: ENTER/PF3/PF4/PF5 + WHEN OTHER | ABNORMAL_005 (invalid key); ENTER/PF3/PF4/PF5 across HAPPY_003–006/012/014 |
| U7 | 状態遷移 | ✅ | K→E/N (ENTER), N→E (add), PF4→K, pseudo-conv commarea | HAPPY_012, HAPPY_013 |
| U8 | 機能・業務フロー | ✅ | §1.2 functions — one happy per classifier (lookup-existing / lookup-new / add / update) | HAPPY_003, 004, 005, 006 |
| U9 | 計算・編集ロジック | ✅ | 6300-PARSE-RATE int/frac scaling | HAPPY_007 (+ BOUNDARY_004/007/008) |
| U10 | 出力・帳票 | ✅ | 3600-ADD-DGRP column-level write + filler | HAPPY_008 |
| U11 | データ整合性・冪等性 | ✅ | REWRITE preserves key/filler; re-save idempotent; update→add fallback | HAPPY_009, 010, 011 |
| U12 | 排他・同時実行 | ✅ | 3600 WRITE `DUPREC`, 3500 REWRITE contention | ABNORMAL_008 *(runtime/concurrent)* |
| U13 | メッセージ・異常系 | ✅ | 13 literal messages (table above) | one TC per message |
| U14 | 境界値 | ✅ | field lengths X(10)/X(2)/9(4)/7-char rate; rate S9(4)V99 | BOUNDARY_001–009 |
| U15 | 回復・リラン | ✅ | 2050-RECEIVE MAPFAIL empty; update→add recovery | ABNORMAL_009 (+ HAPPY_011) |
| U16 | 権限・セキュリティ | ❌ | OCDGRP does not inspect `CA-USER-TYPE`; sign-on enforced upstream (Sign On screen / menu). No dept/mode gate in source | — |
| U17 | ログ・監査 | ❌ | OCDGRP writes no audit/log record; only DGRPFILE is touched | — |
| U18 | 連携・インターフェース | ✅ | PF3 XCTL to OCMENU (commarea); RETURN TRANSID ORDG pseudo-conv | HAPPY_014 (+ HAPPY_013) |
| U19 | 性能・運用 | ❌ | no volume/printer/paper aspects; pseudo-conv timing is runtime-only | — |
| — | (app-specific) DELETE function | ❌ | no `EXEC CICS DELETE`, no key wired to it — non-existent (see discrepancy #2) | — |

## Coverage matrix (summary)

- **Messages:** 13 / 13 covered (each has a dedicated TC; see message table).
- **Input fields × checks:** DGGRP (required/max/min), DGTYPE (required/max/min), DGCAT (numeric/blank/min-0/max-9999/single-digit), DGRATE (invalid/max/int-over/frac-over/min-0/no-dot/two-dots) — all covered.
- **Classifier happy flows:** lookup-existing, lookup-new, add, update — all covered (HAPPY_003–006).
- **AID keys:** ENTER (HAPPY_003/004 + saves), PF5 (HAPPY_005/006 + boundaries), PF3 (HAPPY_014), PF4 (HAPPY_012), unsupported (ABNORMAL_005) — all covered.
- **Excluded viewpoints:** U3 (no conditional field control), U16 (no in-program authority), U17 (no audit log), U19 (runtime-only), DELETE (non-existent) — each with a reason above.

## TC totals

- **Total: 32 TC** — Normal (正常系) **14**, Abnormal (異常系) **9**, Boundary (境界値) **9**.

## Audit result (`audit_testcase.py --reg <REG> --root OCDGRP`)

9 candidates — **all grounded and dismissed** (the two documented ORION-CCMS classes):

1. **`[valid] could not run msg_codes`** — the reg stores XML under `parsed/cobol_xml/ORION-CCMS/cbl/`, but the audit's `_root_xml` only probes `main/`|`sub/`, so it cannot self-run `msg_codes`. **Dismissed:** ran `msg_codes.py OCDGRP.xml` directly → **0 codes** (this app uses literal English messages, no EI/EF/GF/ER). No code-coverage gap exists.
2. **`[vague] … 異常系 case but expected cites no message code / STOP`** (8 of the 9 Abnormal TCs) — the check wants an `EI/EF`-style code or STOP in the expected. **Dismissed:** OCDGRP has no message codes; the **verbatim on-screen English string** (e.g. `Account group id is required.`) is the assertion and is quoted in each expected. (ABNORMAL_007 was not flagged because its abend expected contains "aborts".)

No `[wording]` (code-leak), `[fold]`, `[ids]` (duplicate), `[fields]` (empty), `[parity]`, or `[auto]` candidates. All 14 中項目 are canonical viewpoint labels. Judgment dims hand-reviewed: 3 spot-checked TCs (HAPPY_006 → 3500 REWRITE L297-306; ABNORMAL_006 → 2300-SAVE guard L232-236; BOUNDARY_005 → 6350 WS-RE-INT-CNT>4 L435) all trace to the cited source lines.

## Deliverables

- `NOTES.md` (this file)
- `cases.json` (editable source of truth — 32 cases)
- `OCDGRP_TestCases.xlsx` (built with `build_testcase_xlsx.py --lang en`; author `modernizeX`; count cell = 32)
