# TO-BE Screen Design — OCTRANV_TransactionView

> **Rule 1: TO-BE = AS-IS in business terms.** The interface moves to a web SPA, but the
> input/output data, the check conditions, the business flow and the database results are
> preserved. The section order follows the AS-IS document so the two compare 1-to-1.

## 0. Cover

| Item | Content |
|---|---|
| System | ORION-CCMS (ORION Credit Card Management System) |
| Subsystem | On-line (was CICS/BMS; now Vue SPA + REST) |
| Function name | Transaction View |
| Function ID | OCTRANV（COBOL program）／Trans-ID `ORTV`／Map `MTRANVA` |
| Module | `octranv` |
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
    U["Operator (browser)"] --> V["MTRANVA.vue<br>src/programs/octranv/"]
    V -- "POST /api/terminal/execute<br>api/client.ts" --> API["TerminalController → AppRunner<br>(app-runtime)"]
    API --> PG["OctranvService<br>service/OctranvService.java"]
    PG -- "R (by tr_id)" --> DAO["TranfileFileDao<br>orion-web/dao"]
    DAO --> DB[("tranfile")]
    PG -- "ScreenResponse (redirect on PF3)" --> V
    V -- "router → main menu" --> MENU["MMENUA.vue (OCMENU)"]
```

### 1.2 Function overview

| # | Function | Implementation |
|---|---|---|
| 1 | Look up a transaction by its id | The operator enters the id and the transaction record is read by primary key from `tranfile` |
| 2 | Show the current transaction details in read-only form | Card number, type, amount, merchant and description are displayed; nothing on this screen is editable |
| 3 | Return to the calling menu when finished | The back action ends the view and returns the operator to the main menu |

**Roles ／ authorisation**: authenticated operator (signed on through the Sign On screen). Sign-on
state travels in the conversation carried between requests; no framework role gate is wired yet.

**Keys → operations** (the SPA renders each former PF key as a button):

| Key | Label as rendered (verbatim) | What it does |
|---|---|---|
| `ENTER` | `ENTER=PROCESS` | read the keyed transaction and show its details |
| `PF3` | `PF3=BACK` | return to the main menu |
| `PF4` | `PF4=CLEAR` | clear the entry so the operator can start again |

> The middle column is the string the button really shows, copied from the program's button
> definitions. The physical PF keystrokes are not reproduced as keyboard shortcuts, so operators
> used to typing PF3 now click the `PF3=BACK` button — same action, one extra pointer move.

### 1.3 Databases used

| № | Business name | Table | C | R | U | D |
|---|---|---|---|---|---|---|
| 1 | Transaction master — read by transaction id to show the detail | `tranfile` | - | 〇 | - | - |

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
    ENTRY["Calling menu / transaction"] -- "ORTV / selection" --> S["MTRANVA<br>Transaction View"]
    S -- "ENTER: keyed id read and shown" --> S
    S -- "PF3 (back)" --> MENU["MMENUA<br>Main Menu"]
    S -- "blank id / not found / read error" --> MSG["Message line on MTRANVA"]
```

### 2.1 MTRANVA — Transaction View

**Layout**

![TO-BE modernised MTRANVA](../Image/OCTRANV_MTRANVA_TOBE.png)

**Archetype applied**: display form with a single lookup key (`_archetypes/screenModel`). The header
carries the transaction/program/date/time meta strip; the body has one entry box and a read-only
details block; a message line and a button row close the screen.

| Area | Notes |
|---|---|
| Header (Tran/Prog/Date/Time) | meta strip above the title, filled by the server |
| Input area | one entry box — the transaction id lookup key |
| Details area | read-only outputs: card number, type, amount, merchant, description |
| Message line | inline alert below the form (was row 23 `ERRMSG`) |
| Button row | `ENTER=PROCESS`, `PF3=BACK`, `PF4=CLEAR` |

**Screen items**

_State legend: ○ shown · 必 must be entered · □ read-only · － not shown._

| № | Item name | Field | Type ／ length | Control | Required | Initial | Key prompt | Record shown | Notes |
|---|---|---|---|---|---|---|---|---|---|
| 1 | Transaction name | `TRNNAME` | text, 4 | read-only | - | □ | □ | □ | program-supplied header |
| 2 | Title | `TITLE` | text, 40 | read-only | - | □ | □ | □ | program-supplied header |
| 3 | Current date | `CURDATE` | date text, 10 | read-only | - | □ | □ | □ | server-filled |
| 4 | Program name | `PGMNAME` | text, 8 | read-only | - | □ | □ | □ | server-filled |
| 5 | Current time | `CURTIME` | time text, 8 | read-only | - | □ | □ | □ | server-filled |
| 6 | Transaction ID | `TRANID` | numeric text, 16 | entry box, 16 chars | 必 | ○ | 必 | □ | lookup key; kept at 16 as in the source |
| 7 | Card number | `TRCARD` | text, 16 | read-only | - | － | □ | □ | from the transaction record |
| 8 | Type | `TRTYPE` | text, 2 | read-only | - | － | □ | □ | transaction type |
| 9 | Amount | `TRAMT` | amount text, 15 | read-only | - | － | □ | □ | edited amount |
| 10 | Merchant | `TRMERCH` | text, 50 | read-only | - | － | □ | □ | merchant name |
| 11 | Description | `TRDESC` | text, 50 | read-only | - | － | □ | □ | description text |
| 12 | Message line | `ERRMSG` | text, 78 | read-only | - | □ | □ | □ | inline alert or status |

> **Length and format unchanged**: the entry box accepts 16 characters and the server reads the same
> 16-character key; the detail fields are shown with the same widths as the terminal screen.

## 3. Check specifications

### 3.1 MTRANVA — Transaction View

All strings below are the literal messages the server returns to the on-screen message line.
Type: E=Error / I=Information.

| No. | What is checked | When it is checked | Message code | Message shown | If it fails |
|---|---|---|---|---|---|
| 1 | Opening prompt (informational) | when the screen first appears | — | Enter a transaction id and press ENTER. | not a failure; guides the operator |
| 2 | The transaction id must be entered | on `ENTER`, before the lookup | — | Transaction id is required. | the cursor stays on the entry; nothing is read |
| 3 | Record shown (informational) | on `ENTER`, after a successful read | — | Transaction details displayed. | the details block is filled in |
| 4 | The keyed transaction must exist | on `ENTER`, after the lookup | — | Transaction not found - check the id. | the entry stays; no details are shown |
| 5 | The transaction store must be readable | on `ENTER`, during the lookup | — | Error reading the transaction file. | the entry stays; no details are shown |
| 6 | Only a supported key may be used | on any key press | — | Invalid key pressed. Please try again. | the screen is redisplayed unchanged |

## 4. Event specifications

### 4.1 MTRANVA — Transaction View

| No. | Event | What the system does | Result ／ next screen |
|---|---|---|---|
| 1 | The operator enters an id and presses `ENTER` | the keyed transaction is read and its details are filled in | stays on Transaction View with the details shown, or a message when the id is blank / not found |
| 2 | The operator presses `PF3` | the view ends and control returns to the calling menu | the main menu appears |
| 3 | The operator presses `PF4` | the entered value is cleared and the opening prompt returns | stays on Transaction View, ready for a new id |
| 4 | The operator presses an unsupported key | the request is rejected and a message is shown | stays on Transaction View, unchanged |

## 5. DB CRUD

**Update spec**

Not applicable — Transaction View only reads; no row is created, updated or deleted.

**Column-level CRUD (per event ／ business type)**

| Table | Operation | Field | Value source | Transaction |
|---|---|---|---|---|
| `tranfile` | SELECT (read by key `tr_id`) | whole record | the transaction id the operator entered | read-only; no commit |

## 6. API contract

| Request | Response | What it does | Errors |
|---|---|---|---|
| `POST /api/terminal/execute` — `{ programName: "OCTRANV", aidKey, fields: { TRANID } }` | `ScreenResponse` — `{ fields: { TRCARD, TRTYPE, TRAMT, TRMERCH, TRDESC, ERRMSG }, message, buttons }` | reads the transaction by key and returns its detail fields, or a message | blank id → "Transaction id is required."; missing → "Transaction not found - check the id."; read error → "Error reading the transaction file." |

FE types: `src/programs/octranv/schemas.d.ts` (`MTRANVAFields`) · client: `src/api/client.ts` ·
endpoint: `TerminalController` (`/api/terminal/execute`), one round-trip per key press (CICS
pseudo-conversation preserved through the HTTP session).
