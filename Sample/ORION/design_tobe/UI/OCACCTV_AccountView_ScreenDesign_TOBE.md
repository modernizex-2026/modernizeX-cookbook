# TO-BE Screen Design — OCACCTV_AccountView

> **Rule 1: TO-BE = AS-IS in business terms.** The interface moves to a web SPA, but the
> input/output data, the check conditions, the business flow and the database results are
> preserved. The section order follows the AS-IS document so the two compare 1-to-1.

## 0. Cover

| Item | Content |
|---|---|
| System | ORION-CCMS (ORION Credit Card Management System) |
| Subsystem | On-line (was CICS/BMS; now Vue SPA + REST) |
| Function name | Account View |
| Function ID | OCACCTV（COBOL program）／Trans-ID `ORAV`／Map `MACCTVA` |
| Module | `ocacctv` |
| Platform | Java 17 · Spring Boot 3.x · Vue 3 + TypeScript · DB Oracle (H2 in dev) |
| Version ／ Author ／ Date | 1 ／ modernizeX ／ 2026-09-28 |

## 1. Function overview

### 1.1 Overview diagram

System architecture — the screen, the request path to the server, the program service it runs,
and the data store it reads.

```mermaid
---
config:
  layout: elk
  look: neo
  theme: base
---
flowchart LR
    U["Operator (browser)"] --> V["MACCTVA.vue<br>src/programs/ocacctv/"]
    V -- "POST /api/terminal/execute<br>api/client.ts" --> API["TerminalController → AppRunner<br>(app-runtime)"]
    API --> PG["OcacctvService<br>service/OcacctvService.java"]
    PG --> DAO["AcctfileFileDao<br>orion-web/dao"]
    DAO --> DB[("acctfile")]
    PG -- "ScreenResponse (redirect on PF3)" --> V
    V -- "router → main menu" --> MENU["MMENUA.vue (OCMENU)"]
```

### 1.2 Function overview

| # | Function | Implementation |
|---|---|---|
| 1 | Look up an account by its account id | The operator enters the id and the account record is read by primary key from `acctfile` |
| 2 | Show the current account details in read-only form | Status, balances, limits, dates and group are displayed; nothing on this screen is editable |
| 3 | Return to the calling menu when finished | The back action ends the view and returns the operator to the main menu |

**Roles ／ authorisation**: authenticated operator (signed on through the Sign On screen). Sign-on
state travels in the conversation carried between requests; no framework role gate is wired yet.

**Keys → operations** (the SPA renders each former PF key as a button):

| Key | Label as rendered (verbatim) | What it does |
|---|---|---|
| `ENTER` | `ENTER=PROCESS` | read the keyed account and show its details |
| `PF3` | `PF3=BACK` | return to the main menu |
| `PF4` | `PF4=CLEAR` | clear the entry so the operator can start again |

> The middle column is the string the button really shows, copied from the program's button
> definitions. The physical PF keystrokes are not reproduced as keyboard shortcuts, so operators
> used to typing PF3 now click the `PF3=BACK` button — same action, one extra pointer move.

### 1.3 Databases used

| № | Business name | Table | C | R | U | D |
|---|---|---|---|---|---|---|
| 1 | Account master — read by account id to show the detail | `acctfile` | - | 〇 | - | - |

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
    ENTRY["Calling menu / transaction"] -- "ORAV / selection" --> S["MACCTVA<br>Account View"]
    S -- "ENTER: keyed id read and shown" --> S
    S -- "PF3 (back)" --> MENU["MMENUA<br>Main Menu"]
    S -- "blank id / not found / read error" --> MSG["Message line on MACCTVA"]
```

### 2.1 MACCTVA — Account View

**Layout**

![TO-BE modernised MACCTVA](../Image/OCACCTV_MACCTVA_TOBE.png)

**Archetype applied**: display form with a single lookup key (`_archetypes/screenModel`). The header
carries the transaction/program/date/time meta strip; the body has one entry box and a read-only
details block; a message line and a button row close the screen.

| Area | Notes |
|---|---|
| Header (Tran/Prog/Date/Time) | meta strip above the title, filled by the server |
| Input area | one entry box — the account id lookup key |
| Details area | read-only outputs: status, balances, limits, dates, group |
| Message line | inline alert below the form (was row 23 `ERRMSG`) |
| Button row | `ENTER=PROCESS`, `PF3=BACK`, `PF4=CLEAR` |

**Screen items**

_State legend: ○ shown · 必 must be entered · □ read-only · － not shown._

| № | Item name | Field | Type ／ length | Control | Required | Initial | Key prompt | Record shown | Notes |
|---|---|---|---|---|---|---|---|---|---|
| 1 | Account ID | `ACCTID` | numeric text, 11 | entry box, 11 chars | 必 | ○ | 必 | □ | lookup key; kept at 11 as in the source |
| 2 | Status | `ACSTAT` | text, 1 | read-only | - | － | □ | □ | active status code |
| 3 | Current balance | `ACBAL` | amount, 15 | read-only | - | － | □ | □ | edited amount |
| 4 | Credit limit | `ACCRLIM` | amount, 15 | read-only | - | － | □ | □ | edited amount |
| 5 | Cash limit | `ACCSLIM` | amount, 15 | read-only | - | － | □ | □ | edited amount |
| 6 | Open date | `ACOPEN` | date text, 10 | read-only | - | － | □ | □ | as stored |
| 7 | Expiry | `ACEXP` | date text, 10 | read-only | - | － | □ | □ | as stored |
| 8 | Group ID | `ACGRP` | text, 10 | read-only | - | － | □ | □ | disclosure group |

> **Length and format unchanged**: the entry box accepts 11 characters and the server reads the same
> 11-character key; the amounts and dates are shown with the same widths as the terminal screen.

## 3. Check specifications

### 3.1 MACCTVA — Account View

All strings below are the literal messages the server returns to the on-screen message line.
Type: E=Error / I=Information.

| No. | What is checked | When it is checked | Message code | Message shown | If it fails |
|---|---|---|---|---|---|
| 1 | Opening prompt (informational) | when the screen first appears | — | Enter account id and press ENTER. | not a failure; guides the operator |
| 2 | The account id must be entered | on `ENTER`, before the lookup | — | Please enter all required fields. | the cursor stays on the entry; nothing is read |
| 3 | The keyed account must exist | on `ENTER`, after the lookup | — | Record not found. | the entry stays; no details are shown |
| 4 | The account store must be readable | on `ENTER`, during the lookup | — | Error reading account file. | the entry stays; no details are shown |
| 5 | Only a supported key may be used | on any key press | — | Invalid key pressed. Please try again. | the screen is redisplayed unchanged |

## 4. Event specifications

### 4.1 MACCTVA — Account View

| No. | Event | What the system does | Result ／ next screen |
|---|---|---|---|
| 1 | The operator enters an id and presses `ENTER` | the keyed account is read and its details are filled in | stays on Account View with the details shown, or a message when the id is blank / not found |
| 2 | The operator presses `PF3` | the view ends and control returns to the calling menu | the main menu appears |
| 3 | The operator presses `PF4` | the entered value is cleared and the opening prompt returns | stays on Account View, ready for a new id |
| 4 | The operator presses an unsupported key | the request is rejected and a message is shown | stays on Account View, unchanged |

## 5. DB CRUD

**Update spec**

Not applicable — Account View only reads; no row is created, updated or deleted.

**Column-level CRUD (per event ／ business type)**

| Table | Operation | Field | Value source | Transaction |
|---|---|---|---|---|
| `acctfile` | SELECT (read by key) | whole record | the account id the operator entered | read-only; no commit |

## 6. API contract

| Request | Response | What it does | Errors |
|---|---|---|---|
| `POST /api/terminal/execute` — `{ programName: "OCACCTV", aidKey, fields: { ACCTID } }` | `ScreenResponse` — `{ fields: { ACSTAT, ACBAL, ACCRLIM, ACCSLIM, ACOPEN, ACEXP, ACGRP }, message, buttons } ` | reads the account by key and returns its detail fields, or a message | blank id → "Please enter all required fields."; missing → "Record not found."; read error → "Error reading account file." |

FE types: `src/programs/ocacctv/schemas.d.ts` (`MACCTVAFields`) · client: `src/api/client.ts` ·
endpoint: `TerminalController` (`/api/terminal/execute`), one round-trip per key press (CICS
pseudo-conversation preserved through the HTTP session).
