# TO-BE Screen Design — OCDGRP_DisclosureGroupMaint

> **Rule 1: TO-BE = AS-IS in business terms.** The interface moves to a web SPA, but the
> input/output data, the check conditions, the business flow and the database results are
> preserved. The section order follows the AS-IS document so the two compare 1-to-1.

## 0. Cover

| Item | Content |
|---|---|
| System | ORION-CCMS (ORION Credit Card Management System) |
| Subsystem | On-line (was CICS/BMS; now Vue SPA + REST) |
| Function name | Disclosure Group Maintenance |
| Function ID | OCDGRP（COBOL program）／Trans-ID `ORDG`／Map `MDGRPA` |
| Module | `ocdgrp` |
| Platform | Java 17 · Spring Boot 3.x · Vue 3 + TypeScript · DB Oracle (H2 in dev) |
| Version ／ Author ／ Date | 1 ／ modernizeX ／ 2026-09-28 |

## 1. Function overview

### 1.1 Overview diagram

System architecture — the screen, the request path to the server, the program service it runs, and
the data store it maintains.

```mermaid
---
config:
  layout: elk
  look: neo
  theme: base
---
flowchart LR
    U["Operator (browser)"] --> V["MDGRPA.vue<br>src/programs/ocdgrp/"]
    V -- "POST /api/terminal/execute<br>api/client.ts" --> API["TerminalController → AppRunner<br>(app-runtime)"]
    API --> PG["OcdgrpService<br>service/OcdgrpService.java"]
    PG --> DAO["DgrpfileFileDao<br>orion-web/dao"]
    DAO --> DB[("dgrpfile")]
    PG -- "ScreenResponse (redirect on PF3)" --> V
    V -- "router → main menu" --> MENU["MMENUA.vue (OCMENU)"]
```

### 1.2 Function overview

| # | Function | Implementation |
|---|---|---|
| 1 | Look up a disclosure group by its account group, type code and category code | The keyed triple is read by primary key from `dgrpfile` and the current interest rate is shown |
| 2 | Add a new disclosure group or change an existing one's rate | The operator keys the interest rate and confirms; the row is written when new or rewritten when it already exists |
| 3 | Return to the calling menu when finished | The back action ends the maintenance and returns the operator to the main menu |

**Roles ／ authorisation**: authenticated operator (signed on through the Sign On screen). Sign-on
state travels in the conversation carried between requests; no framework role gate is wired yet.

**Keys → operations** (the SPA renders each former PF key as a button):

| Key | Label as rendered (verbatim) | What it does |
|---|---|---|
| `ENTER` | `ENTER=PROCESS` | read the keyed disclosure group and show its current rate |
| `PF3` | `PF3=BACK` | return to the main menu |
| `PF4` | `PF4=CLEAR` | clear the entered values so the operator can start again |
| `PF5` | `PF5` | confirm and save — add a new group or update the existing rate |

> The middle column is the string the button really shows, copied from the program's button
> definitions. The physical PF keystrokes are not reproduced as keyboard shortcuts, so operators
> used to typing PF3 now click the `PF3=BACK` button — same action, one extra pointer move.

### 1.3 Databases used

| № | Business name | Table | C | R | U | D |
|---|---|---|---|---|---|---|
| 1 | Disclosure group master — keyed by account group, type code and category code | `dgrpfile` | 〇 | 〇 | 〇 | - |

> The on-line service reads the group, adds a new row and rewrites an existing row; it does not
> delete a disclosure group, so the delete column stays empty.

## 2. Screen spec

Screen transition of the whole function — every screen a box, every edge labelled with what moves
the operator.

```mermaid
---
config:
  layout: elk
  look: neo
  theme: base
---
flowchart TB
    ENTRY["Calling menu / transaction"] -- "ORDG / selection" --> S["MDGRPA<br>Disclosure Group Maintenance"]
    S -- "ENTER: keyed group read and shown" --> S
    S -- "PF5: add or update saved" --> S
    S -- "PF3 (back)" --> MENU["MMENUA<br>Main Menu"]
    S -- "missing key / non-numeric / bad rate / save error" --> MSG["Message line on MDGRPA"]
```

### 2.1 MDGRPA — Disclosure Group Maintenance

**Layout**

![TO-BE modernised MDGRPA](../Image/OCDGRP_MDGRPA_TOBE.png)

**Archetype applied**: maintenance form with a three-part lookup key and one editable rate field
(`_archetypes/screenModel`). The header carries the transaction/program/date/time meta strip; the
body has the key entry boxes and the rate box; a message line and a button row close the screen.

| Area | Notes |
|---|---|
| Header (Tran/Prog/Date/Time) | meta strip above the title, filled by the server |
| Input area | three key boxes (account group, type code, category code) plus the interest rate box |
| Details area | the rate read back after a lookup shows the current stored value |
| Message line | inline alert below the form (was row 23 `ERRMSG`) |
| Button row | `ENTER=PROCESS`, `PF3=BACK`, `PF4=CLEAR`, `PF5` |

**Screen items**

_State legend: ○ shown · 必 must be entered · □ read-only · － not shown._

| № | Item name | Field | Type ／ length | Control | Required | Initial | Key prompt | Record shown | Notes |
|---|---|---|---|---|---|---|---|---|---|
| 1 | Transaction id | `TRNNAME` | text, 4 | read-only | - | ○ | － | □ | header meta, server-filled |
| 2 | Screen title | `TITLE` | text, 40 | read-only | - | ○ | － | □ | header meta |
| 3 | Current date | `CURDATE` | date text, 10 | read-only | - | ○ | － | □ | header meta |
| 4 | Program name | `PGMNAME` | text, 8 | read-only | - | ○ | － | □ | header meta |
| 5 | Current time | `CURTIME` | time text, 8 | read-only | - | ○ | － | □ | header meta |
| 6 | Acct Group | `DGGRP` | text, 10 | entry box, 10 chars | 必 | ○ | 必 | □ | key part 1; kept at 10 as in the source |
| 7 | Type Code | `DGTYPE` | text, 2 | entry box, 2 chars | 必 | ○ | 必 | □ | key part 2; kept at 2 |
| 8 | Cat Code | `DGCAT` | numeric text, 4 | entry box, 4 chars | 必 | ○ | 必 | □ | key part 3; numeric, kept at 4 |
| 9 | Int Rate | `DGRATE` | numeric text, 7 | entry box, 7 chars | 必 | ○ | － | □ | the interest rate saved on PF5 |
| 10 | Message line | `ERRMSG` | text, 78 | read-only | - | － | － | □ | prompt and result messages |

> **Length and format unchanged**: the account group accepts 10 characters, the type code 2, the
> category code 4 numeric characters and the rate 7 — the same widths the terminal screen used.

## 3. Check specifications

### 3.1 MDGRPA — Disclosure Group Maintenance

All strings below are the literal messages the server returns to the on-screen message line.
Type: E=Error / I=Information.

| No. | What is checked | When it is checked | Message code | Message shown | If it fails |
|---|---|---|---|---|---|
| 1 | Opening prompt (informational) | when the screen first appears | — | Enter group, type, category and press ENTER. | not a failure; guides the operator |
| 2 | The account group id must be entered | on `ENTER` / `PF5`, during key validation | — | Account group id is required. | the cursor stays on the account group; nothing is read or saved |
| 3 | The type code must be entered | on `ENTER` / `PF5`, during key validation | — | Type code is required. | the cursor stays on the type code; nothing is read or saved |
| 4 | The category code must be numeric | on `ENTER` / `PF5`, during key validation | — | Category code must be numeric. | the cursor stays on the category code; nothing is read or saved |
| 5 | The interest rate must be a valid number | on `PF5`, before the save | — | Interest rate is not a valid number. | the cursor stays on the rate; nothing is saved |
| 6 | The key must be looked up before saving | on `PF5`, before the save | — | Enter the key and press ENTER first. | nothing is saved; the operator must press `ENTER` first |
| 7 | The keyed group already exists (informational) | on `ENTER`, after the read | — | Group found. Change rate, PF5 to update. | not a failure; the stored rate is shown for editing |
| 8 | The keyed group is new (informational) | on `ENTER`, after the read | — | New group. Enter rate, PF5 to add. | not a failure; the rate is left blank for entry |
| 9 | Add applied (informational) | after a successful `PF5` add | — | Disclosure group added. | not a failure; the new row is committed |
| 10 | Update applied (informational) | after a successful `PF5` update | — | Disclosure group updated. | not a failure; the row is committed |
| 11 | The group store must be writable | during the `PF5` save | — | Error saving the disclosure group. | the row is not saved; the entry stays on the screen |
| 12 | Only a supported key may be used | on any key press | — | Invalid key pressed. Please try again. | the screen is redisplayed unchanged |

## 4. Event specifications

### 4.1 MDGRPA — Disclosure Group Maintenance

| No. | Event | What the system does | Result ／ next screen |
|---|---|---|---|
| 1 | The operator keys the account group, type code and category code and presses `ENTER` | the keyed disclosure group is read and its current interest rate is filled in | stays on the maintenance screen; a message says whether the group already exists or is new |
| 2 | The operator edits the interest rate and presses `PF5` | the disclosure group is saved — a new row is added, or the existing row's rate is updated | stays on the screen with a saved message, or an error when the store cannot be written |
| 3 | The operator presses `PF3` | the maintenance ends and control returns to the main menu | the main menu appears |
| 4 | The operator presses `PF4` | the entered values are cleared and the opening prompt returns | stays on the maintenance screen, ready for a new key |
| 5 | The operator presses an unsupported key | the request is rejected and a message is shown | stays on the maintenance screen, unchanged |

## 5. DB CRUD

**Update spec**

The maintenance reads the disclosure group by its three-part key, then either writes a new row when
the group is new or rewrites the existing row's rate when it is found. The on-line service performs
no delete.

**Column-level CRUD (per event ／ business type)**

| Table | Operation | Field | Value source | Transaction |
|---|---|---|---|---|
| `dgrpfile` | SELECT (read by key) | whole record | the account group, type code and category code the operator entered | read-only lookup on `ENTER` |
| `dgrpfile` | INSERT (add) | `dg_acct_group`, `dg_type_cd`, `dg_cat_cd`, `dg_int_rate` | the keyed triple plus the entered rate | committed on `PF5` when the group is new |
| `dgrpfile` | UPDATE (rewrite) | `dg_int_rate` | the entered interest rate | committed on `PF5` when the group already exists |

## 6. API contract

| Request | Response | What it does | Errors |
|---|---|---|---|
| `POST /api/terminal/execute` — `{ programName: "OCDGRP", aidKey, fields: { DGGRP, DGTYPE, DGCAT, DGRATE } }` | `ScreenResponse` — `{ fields: { DGGRP, DGTYPE, DGCAT, DGRATE }, message, buttons }` | reads the keyed group on `ENTER`, or adds/updates it on `PF5` | blank group → "Account group id is required."; blank type → "Type code is required."; non-numeric code → "Category code must be numeric."; bad rate → "Interest rate is not a valid number."; save fault → "Error saving the disclosure group." |

FE types: `src/programs/ocdgrp/schemas.d.ts` (`MDGRPAFields`) · client: `src/api/client.ts` ·
endpoint: `TerminalController` (`/api/terminal/execute`), one round-trip per key press (CICS
pseudo-conversation preserved through the HTTP session).
