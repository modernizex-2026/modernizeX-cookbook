# NOTES — Test cases for OCPWDCH (Password Change)

## 1. App scope & classification

| Item | Value |
|---|---|
| System / project | ORION-CCMS (ORION Credit Card Management System) |
| App (root) | OCPWDCH — Password Change |
| CICS transaction | ORPW |
| Kind | **UI (CICS/BMS)** — `design_evidence.py` → `kind=ui` (a UI document exists; owns BMS mapset `MPWDCH`, map `MPWDCHA`) |
| Test level | 機能テスト（画面） (functional / screen test) |
| AREA (ID scheme) | `PWDCHG` (unique across the run) |
| Evidence read | `UI/OCPWDCH_PasswordChange_ScreenDesign.md`; source `ORION-CCMS/cbl/OCPWDCH.cbl` (121 LOC); copybooks `RUSER`, `WMSG`, `WCONST`, `KCOMM`, `MPWDCH`; BMS `MPWDCH.bms`; seed `data/USRSEC.txt` |

**Language:** deliverable is English (skill edition fixed). Verbatim on-screen message
strings are reproduced unchanged in `` `backticks` ``; code identifiers live only in 備考.

## 2. What the program actually does (ground truth from OCPWDCH.cbl)

Pseudo-conversational CICS program. `0000-MAIN`:
1. `EIBCALEN = 0` (started with no communication area / cold) → transfer straight to
   the main menu **OCMENU** (no password screen). *(L33-36)*
2. Otherwise, first entry (`CA-PGM-CONTEXT = 0`, `CA-FIRST-ENTER`) → `1000-SEND-INITIAL`
   (send map, show `` `Enter user id, old and new password.` ``, set context = 1). *(L38-51)*
3. Re-entry (context = 1) → `2000-PROCESS-INPUT` → `EVALUATE EIBAID`:
   - **PF3** → return to main menu OCMENU, passing the shared commarea *(L55-59)*
   - **PF4** → clear & redisplay the initial screen *(L60-61)*
   - **ENTER** → `2100-CHANGE-PWD` *(L62-63)*
   - **any other key** → redisplay + `` `Invalid key pressed. Please try again.` `` *(L64-67)*

`2100-CHANGE-PWD` validation order *(L70-97)*:
1. **User ID blank** (SPACES / LOW-VALUES) → `` `Please enter all required fields.` `` — **only User ID is checked for blank; Old/New/Confirm are NOT** *(L73-75)*.
2. **New ≠ Confirm** → `` `New password and confirm do not match.` `` *(L77-80)*.
3. Read user by keyed User ID (`READ … UPDATE`, `3000-READ-USER`). **Record not found** → `` `Record not found.` `` *(L84-86)*.
4. **Stored password ≠ Old Pass entered** → `` `Current password is incorrect.` `` *(L88-91)*.
5. Else `4000-UPDATE-PWD`: move New Pass into the record, `REWRITE`. Success → `` `Password changed successfully.` ``; non-normal response → `` `Password change failed.` `` *(L107-116)*.

Then `9000-RETURN TRANSID(ORPW)` — screen stays after every message.

**Grounded behavioural findings (design-relevant, not fabricated):**
- **No per-user authority gate.** The record changed is the one keyed in **User ID**;
  the program never compares it to the signed-on user (`CA-USER-ID` unused). Any
  authenticated operator can change *any* user's password if they know that user's
  current password. → covered by `IT_PWDCHG_HAPPY_007`, and 権限 marked accordingly.
- **Passwords shown in clear text.** BMS fields `USERID/OLDPWD/NEWPWD/CFMPWD` are
  `UNPROT,IC,FSET,GREEN` with **no `DRK`/non-display attribute** → typed characters are
  visible. → covered by `IT_PWDCHG_HAPPY_004`.
- **Only User ID is a required field.** Old/New/Confirm blank are not rejected → a blank
  New/Confirm passes the match check and **sets the stored password to spaces**. →
  `UT_PWDCHG_BOUNDARY_001` (blank new password) and `UT_PWDCHG_ABNORMAL_006` (blank old).
- **No password complexity/length-min rule.** New Pass is free text `X(08)`; a 1-char
  password is accepted. → `UT_PWDCHG_BOUNDARY_003`.

## 3. Message inventory (ground truth)

`msg_codes.py OCPWDCH.xml` → **0 EI/EF/GF codes** — ORION-CCMS uses **literal-English
messages** to the on-screen message line (`ERRMSGO`), not coded IDs. The 8 literals
(design §3 = source):

| # | Verbatim message | Type | Trigger (para / line) | Dedicated TC |
|---|---|---|---|---|
| M1 | `Enter user id, old and new password.` | I | initial send / PF4 clear (`1000` L48) | IT_PWDCHG_HAPPY_001 |
| M2 | `New password and confirm do not match.` | E | New ≠ Confirm (`2100` L78) | UT_PWDCHG_ABNORMAL_003 |
| M3 | `Current password is incorrect.` | E | stored ≠ Old (`2100` L89) | UT_PWDCHG_ABNORMAL_005 |
| M4 | `Password changed successfully.` | I | REWRITE normal (`4000` L112) | IT_PWDCHG_HAPPY_005 |
| M5 | `Password change failed.` | E | REWRITE non-normal (`4000` L114) | UT_PWDCHG_ABNORMAL_007 |
| M6 | `Invalid key pressed. Please try again.` | E | unsupported AID (`2000` OTHER L66) | UT_PWDCHG_ABNORMAL_001 |
| M7 | `Please enter all required fields.` | E | User ID blank (`2100` L74) | UT_PWDCHG_ABNORMAL_002 |
| M8 | `Record not found.` | E | user not found (`2100` L85) | UT_PWDCHG_ABNORMAL_004 |

All 8 covered by a dedicated TC. (No EI/EF codes exist, so the audit's code-coverage
and "異常系 expected cites no code" checks do not apply — see §7.)

## 4. Fixture — `F-STD` (USRSEC seed, `data/USRSEC.txt`, layout `RUSER`)

VSAM KSDS `USRSEC` (logical: User security), key = **User ID** `US-ID X(08)`. Record =
ID `X(08)` · First name `X(20)` · Last name `X(20)` · Password `X(08)` · Type `X(01)` · filler.

| User ID | First | Last | Password | Type |
|---|---|---|---|---|
| `ADMIN001` | SYSTEM | ADMINISTRATOR | `PASS0001` | A (admin) |
| `USER0001` | ALICE | ANDERSON | `PASS0002` | U (normal) |
| `USER0002` | BOB | BAKER | `PASS0003` | U (normal) |
| `OPER0001` | CAROL | CLARK | `PASS0004` | U (normal) |

Each TC's 前提条件 says "F-STD" + only its delta. Operator is signed on and reached the
Password Change screen from the main menu (commarea present, first entry).

## 5. Viewpoint table (観点表) — every base viewpoint decided

**Approval gate (non-interactive run): the table below is recorded as an approved
assumption and generation proceeds** (per skill instruction). 大項目 = "Password Change".

### UI base catalog (A–E)

| # | 観点 (中項目) | Applicable? | Evidence | Planned TC |
|---|---|---|---|---|
| U1 | 画面表示・レイアウト (Screen display / layout) | ✅ | §2.1 mockup + item table; BMS MPWDCHA | IT_PWDCHG_HAPPY_001, _002 |
| U2 | 初期値・デフォルト (Initial values / defaults) | ✅ (folded into U1) | `1000` LOW-VALUES + initial msg L46-48 | IT_PWDCHG_HAPPY_001 |
| U3 | 表示条件・活性制御 (Display conditions / enable control) | ❌ — item-states table identical in both modes; no conditional/mode-dependent display. Field enable/protect attributes covered in U1. | §2.1 item states; BMS attrs | — |
| U4 | 文字・書式表示 (Characters / format display) | ✅ | header date `YYYY-MM-DD` / time `HH:MM:SS` (FORMATTIME L123-126); clear-text password (no DRK) | IT_PWDCHG_HAPPY_003, _004 |
| U5 | 入力チェック (Input validation) | ✅ | §3 checks; `2100` L73-91 | UT_PWDCHG_ABNORMAL_002,_003,_004,_005,_006 |
| U6 | 操作性・キー (Operability / function keys) | ✅ | §1.2 keys; `2000` EVALUATE EIBAID | IT_PWDCHG_HAPPY_008(ENTER),_009(PF3),_010(PF4); UT_PWDCHG_ABNORMAL_001 |
| U7 | 状態遷移 (Screen / state transitions) | ✅ | `0000` EIBCALEN guard L33; context 0→1; RETURN TRANSID | IT_PWDCHG_HAPPY_011, _012 |
| U8 | 機能・業務フロー (Function / business flow) | ✅ | §1.2; `2100`→`4000` REWRITE | IT_PWDCHG_HAPPY_005, _006, _007 |
| U9 | 計算・編集ロジック (Calculation / editing logic) | ❌ — no business calculation; only header date/time formatting (in U4). | source has no COMPUTE / totals | — |
| U10 | 出力・帳票・メール (Output / report / mail) | ❌ — no report/print/mail; the only write is the USRSEC password update (see U11). | §1.3; no SEND to printer / no mail | — |
| U11 | データ整合性・冪等性 (Data integrity / idempotency) | ✅ | `4000` REWRITE persists US-PASSWORD; re-change with new pwd | IT_PWDCHG_HAPPY_013; UT_PWDCHG_BOUNDARY_001 |
| U12 | 排他・同時実行 (Exclusion / concurrent access) | ❌ — `READ … UPDATE` locks the single user record until REWRITE/RETURN; concurrent behaviour is runtime/environment, not deterministically testable here. | `3000` READ UPDATE L101 | — (deferred, runtime) |
| U13 | メッセージ・異常系 (Messages / abnormal) | ✅ | §3; `4000` non-normal REWRITE (M5) | UT_PWDCHG_ABNORMAL_007 (others distributed to U5/U6) |
| U14 | 境界値 (Boundary values) | ✅ | New Pass `X(08)` / BMS LENGTH=8; no min-length; case-sensitive compare | UT_PWDCHG_BOUNDARY_001,_002,_003,_004,_005 |
| U15 | 回復・リラン (Recovery / rerun) | ❌ — pseudo-conversational single-record edit; no partial-commit/restart logic. Re-entry idempotence covered in U11. | source has no restart/flag logic | — |
| U16 | 権限・セキュリティ (Authority / security) | ⚠️ partial — the only in-program gate is the `EIBCALEN=0` cold-start guard (→ menu, covered in U7). **No per-user restriction** (any user's record can be keyed) and **passwords shown in clear** are grounded findings, covered as behaviour in IT_PWDCHG_HAPPY_007 / _004, not as a pass/fail gate. Upstream Sign-On (separate app) enforces authentication. | `0000` L33; `CA-USER-ID` unused; BMS no DRK | (via HAPPY_007, _004, _011) |
| U17 | ログ・監査 (Log / audit) | ❌ — no audit/log record written on password change. | no log-file WRITE in source | — |
| U18 | 連携・インターフェース (Linkage / interface) | ✅ (folded into U7) | XCTL → OCMENU (L34 cold, L57 PF3 w/ commarea); RETURN TRANSID(ORPW) | IT_PWDCHG_HAPPY_009, _011 |
| U19 | 性能・運用 (Operation) | ❌ — no paper/printer setup, no volume/long-run aspect (single online edit). | — | — |

### App-specific viewpoints revealed by the spec

| 観点 | Applicable? | Evidence | Planned TC |
|---|---|---|---|
| "Only User ID required" quirk | ✅ | `2100` checks only USERID blank (L73), not Old/New/Confirm | UT_PWDCHG_ABNORMAL_006, UT_PWDCHG_BOUNDARY_001 |
| No per-user restriction (change another user) | ✅ | `CA-USER-ID` never compared to keyed USERID | IT_PWDCHG_HAPPY_007 |
| Clear-text password display | ✅ | BMS password fields lack DRK | IT_PWDCHG_HAPPY_004 |

## 6. Coverage matrix (viewpoint → cases)

| 中項目 | Cases | Categories |
|---|---|---|
| 画面表示・レイアウト | HAPPY_001, _002 | Normal |
| 文字・書式表示 | HAPPY_003, _004 | Normal |
| 機能・業務フロー | HAPPY_005, _006, _007 | Normal |
| 操作性・キー | HAPPY_008, _009, _010, ABNORMAL_001 | Normal / Abnormal |
| 状態遷移 | HAPPY_011, _012 | Normal |
| データ整合性・冪等性 | HAPPY_013, BOUNDARY_001 | Normal / Boundary |
| 入力チェック | ABNORMAL_002, _003, _004, _005, _006 | Abnormal |
| メッセージ・異常系 | ABNORMAL_007 | Abnormal |
| 境界値 | BOUNDARY_002, _003, _004, _005 | Boundary |

- **Messages:** 8 / 8 literal messages each have a dedicated TC (see §3).
- **Function keys:** ENTER, PF3, PF4, invalid-key — all covered.
- **Validations:** User-ID-blank, new≠confirm, record-not-found, wrong-old-password,
  only-User-ID-required quirk — all covered.
- **Boundaries:** New Pass length both sides (blank/0, 1, max 8, truncate at 8) +
  case-sensitive old-password compare.
- **Totals:** 25 TC — Normal 13, Abnormal 7, Boundary 5.

## 7. Audit result (`audit_testcase.py --reg <REG> --root OCPWDCH`)

Candidates and their resolution (each grounded, per skill):

- **"could not run msg_codes (no reg xml…) — skip code coverage"** — *dismissed.* The
  audit's `_root_xml` only looks in `parsed/cobol_xml/{main,sub}`; this reg nests the AST
  under `parsed/cobol_xml/ORION-CCMS/cbl/OCPWDCH.xml`. Running `msg_codes.py` on the real
  path returns **0 codes** (literal-English messages), so there is no code-coverage gap.
- **"異常系 case but expected cites no message code / STOP"** (fires for each Abnormal TC)
  — *dismissed.* ORION-CCMS raises **literal-English messages**, not EI/EF/GF codes; each
  Abnormal TC's 期待結果 quotes the exact verbatim message in `` `backticks` `` (the real
  assertion). No coded ID exists to cite; inventing one would be a defect.
- No duplicate/empty ids, no empty 中項目, no folding (each error = its own TC), no
  wording/code-leak, automation columns consistent (all `auto=×`, manual — no automation
  harness for this AS-IS CICS screen).

## 8. Assumptions

- Viewpoint table (§5) recorded as **approved** (non-interactive run).
- `F-STD` seed values (§4) taken from the committed `data/USRSEC.txt`; no `*(needs real data)*` placeholders were required.
- Automation: `自動=×`, `自動化ID` empty — no runnable automation target for this AS-IS CICS/BMS screen.
