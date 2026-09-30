# TO-BE Screen Design — OCTTYP_TranTypeMaint

> **Rule 1: TO-BE = AS-IS in business terms.** The interface moves to a web SPA, but the
> input/output data, the check conditions, the business flow and the database results are
> preserved. The section order follows the AS-IS document so the two compare 1-to-1.

## 0. Cover

| Item | Content |
|---|---|
| System | ORION-CCMS (ORION Credit Card Management System) |
| Subsystem | On-line (was CICS/BMS; now Vue SPA + REST) |
| Function name | Transaction Type Maintenance |
| Function ID | OCTTYP（COBOL program）／Trans-ID `ORTT`／Map `MTTYPA` |
| Module | `octtyp` |
| Platform | Java 17 · Spring Boot 3.x · Vue 3 + TypeScript · DB Oracle (H2 in dev) |
| Version ／ Author ／ Date | 1 ／ modernizeX ／ 2026-09-28 |

## 1. Function overview

### 1.1 Overview diagram

System architecture — the screen, the request path to the server, the program service it runs,
and the data store it maintains.

```mermaid
---
config:
  layout: elk
  look: neo
  theme: base
---
flowchart LR
    U["Operator (browser)"] --> V["MTTYPA.vue<br>src/programs/octtyp/"]
    V -- "POST /api/terminal/execute<br>api/client.ts" --> API["TerminalController → AppRunner<br>(app-runtime)"]
    API --> PG["OcttypService<br>service/OcttypService.java"]
    PG --> DAO["TtypfileFileDao<br>orion-web/dao"]
    DAO --> DB[("ttypfile")]
    PG -- "ScreenResponse (redirect on PF3)" --> V
    V -- "router → main menu" --> MENU["MMENUA.vue (OCMENU)"]
```

### 1.2 Function overview

| # | Function | Implementation |
|---|---|---|
| 1 | Look up a transaction type by its type code | The keyed code is read by primary key from `ttypfile` and the current description is shown |
| 2 | Add a new type or change an existing one | The operator keys the description and confirms; the row is written when new or rewritten when it already exists |
| 3 | Return to the calling menu when finished | The back action ends the maintenance and returns the operator to the main menu |

**Roles ／ authorisation**: authenticated operator (signed on through the Sign On screen). Sign-on
state travels in the conversation carried between requests; no framework role gate is wired yet.

**Keys → operations** (the SPA renders each former PF key as a button):

| Key | Label as rendered (verbatim) | What it does |
|---|---|---|
| `ENTER` | `ENTER=PROCESS` | read the keyed type and show its current definition |
| `PF3` | `PF3=BACK` | return to the main menu |
| `PF4` | `PF4=CLEAR` | clear the entered values so the operator can start again |
| `PF5` | `PF5` | confirm and save — add a new type or update the existing one |

> The middle column is the string the button really shows, copied from the program's button
> definitions. The physical PF keystrokes are not reproduced as keyboard shortcuts, so operators
> used to typing PF3 now click the `PF3=BACK` button — same action, one extra pointer move.

### 1.3 Databases used

| № | Business name | Table | C | R | U | D |
|---|---|---|---|---|---|---|
| 1 | Transaction type master — keyed by type code | `ttypfile` | 〇 | 〇 | 〇 | - |

> The on-line service reads the type, adds a new row and rewrites an existing row; it does not
> delete a type, so the delete column stays empty.

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
    ENTRY["Calling menu / transaction"] -- "ORTT / selection" --> S["MTTYPA<br>Transaction Type Maintenance"]
    S -- "ENTER: keyed type read and shown" --> S
    S -- "PF5: add or update saved" --> S
    S -- "PF3 (back)" --> MENU["MMENUA<br>Main Menu"]
    S -- "missing code / save error" --> MSG["Message line on MTTYPA"]
```

### 2.1 MTTYPA — Transaction Type Maintenance

**Layout**

![TO-BE modernised MTTYPA](../Image/OCTTYP_MTTYPA_TOBE.png)

**Archetype applied**: maintenance form with a single lookup key and one editable detail field
(`_archetypes/screenModel`). The header carries the transaction/program/date/time meta strip; the
body has the key entry box and the description box; a message line and a button row close the
screen.

| Area | Notes |
|---|---|
| Header (Tran/Prog/Date/Time) | meta strip above the title, filled by the server |
| Input area | one key box (type code) plus the description box |
| Details area | the description read back after a lookup shows the current stored text |
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
| 6 | Type Code | `TTCD` | text, 2 | entry box, 2 chars | 必 | ○ | 必 | □ | lookup key; kept at 2 as in the source |
| 7 | Desc | `TTDESC` | text, 50 | entry box, 50 chars | 必 | ○ | － | □ | the type description saved on PF5 |
| 8 | Message line | `ERRMSG` | text, 78 | read-only | - | － | － | □ | prompt and result messages |

> **Length and format unchanged**: the type code accepts 2 characters and the description 50
> characters — the same widths the terminal screen used.

## 3. Check specifications

### 3.1 MTTYPA — Transaction Type Maintenance

All strings below are the literal messages the server returns to the on-screen message line.
Type: E=Error / I=Information.

| No. | What is checked | When it is checked | Message code | Message shown | If it fails |
|---|---|---|---|---|---|
| 1 | Opening prompt (informational) | when the screen first appears | — | Enter a type code and press ENTER. | not a failure; guides the operator |
| 2 | The type code must be entered | on `ENTER`, during key validation | — | Type code is required. | the cursor stays on the type code; nothing is read |
| 3 | The description must be entered | on `PF5`, before the save | — | Description is required. | the cursor stays on the description; nothing is saved |
| 4 | The type code must be looked up before saving | on `PF5`, before the save | — | Enter a type code and press ENTER first. | nothing is saved; the operator must press `ENTER` first |
| 5 | The keyed type already exists (informational) | on `ENTER`, after the read | — | Type found. Change text, PF5 to update. | not a failure; the stored description is shown for editing |
| 6 | The keyed type is new (informational) | on `ENTER`, after the read | — | New type. Enter text, PF5 to add. | not a failure; the description is left blank for entry |
| 7 | Add applied (informational) | after a successful `PF5` add | — | Transaction type added. | not a failure; the new row is committed |
| 8 | Update applied (informational) | after a successful `PF5` update | — | Transaction type updated. | not a failure; the row is committed |
| 9 | The type store must be writable | during the `PF5` save | — | Error saving the transaction type. | the row is not saved; the entry stays on the screen |
| 10 | Only a supported key may be used | on any key press | — | Invalid key pressed. Please try again. | the screen is redisplayed unchanged |

## 4. Event specifications

### 4.1 MTTYPA — Transaction Type Maintenance

| No. | Event | What the system does | Result ／ next screen |
|---|---|---|---|
| 1 | The operator keys the type code and presses `ENTER` | the keyed type is read and its current description is filled in | stays on the maintenance screen; a message says whether the type already exists or is new |
| 2 | The operator edits the description and presses `PF5` | the type is saved — a new row is added, or the existing row is updated | stays on the screen with a saved message, or an error when the store cannot be written |
| 3 | The operator presses `PF3` | the maintenance ends and control returns to the calling menu | the main menu appears |
| 4 | The operator presses `PF4` | the entered values are cleared and the opening prompt returns | stays on the maintenance screen, ready for a new code |
| 5 | The operator presses an unsupported key | the request is rejected and a message is shown | stays on the maintenance screen, unchanged |

## 5. DB CRUD

**Update spec**

The maintenance reads the type by its code, then either writes a new row when the type is new or
rewrites the existing row when it is found. The on-line service performs no delete.

**Column-level CRUD (per event ／ business type)**

| Table | Operation | Field | Value source | Transaction |
|---|---|---|---|---|
| `ttypfile` | SELECT (read by key) | whole record | the type code the operator entered | read-only lookup on `ENTER` |
| `ttypfile` | INSERT (add) | `tt_cd`, description | the keyed code plus the entered description | committed on `PF5` when the type is new |
| `ttypfile` | UPDATE (rewrite) | description | the entered description | committed on `PF5` when the type already exists |

## 6. API contract

| Request | Response | What it does | Errors |
|---|---|---|---|
| `POST /api/terminal/execute` — `{ programName: "OCTTYP", aidKey, fields: { TTCD, TTDESC } }` | `ScreenResponse` — `{ fields: { TTCD, TTDESC }, message, buttons }` | reads the keyed type on `ENTER`, or adds/updates it on `PF5` | blank code → "Type code is required."; blank description → "Description is required."; save fault → "Error saving the transaction type." |

FE types: `src/programs/octtyp/schemas.d.ts` (`MTTYPAFields`) · client: `src/api/client.ts` ·
endpoint: `TerminalController` (`/api/terminal/execute`), one round-trip per key press (CICS
pseudo-conversation preserved through the HTTP session).
