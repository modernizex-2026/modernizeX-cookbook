# TO-BE Screen Design — ODCUSTV_CustomerViewDB2

> **Rule 1: TO-BE = AS-IS in business terms.** The interface moves to a web SPA, but the
> input/output data, the check conditions, the business flow and the database results are
> preserved. The section order follows the AS-IS document so the two compare 1-to-1.

## 0. Cover

| Item | Content |
|---|---|
| System | ORION-CCMS (ORION Credit Card Management System) |
| Subsystem | On-line (was CICS/BMS; now Vue SPA + REST) |
| Function name | Customer View (DB2) |
| Function ID | ODCUSTV（COBOL program）／Trans-ID `OD04`／Map `MCUSTVA` |
| Module | `odcustv` |
| Platform | Java 17 · Spring Boot 3.x · Vue 3 + TypeScript · DB Oracle (H2 in dev) |
| Version ／ Author ／ Date | 1 ／ modernizeX ／ 2026-09-28 |

## 1. Function overview

### 1.1 Overview diagram

System architecture — the screen, the request path to the server, the program service it runs,
and the relational customer table it reads. In the legacy program the data access was embedded SQL
against DB2; it is now a JDBC query issued by this program's own data-access class.

```mermaid
---
config:
  layout: elk
  look: neo
  theme: base
---
flowchart LR
    U["Operator (browser)"] --> V["MCUSTVA.vue<br>src/programs/odcustv/"]
    V -- "POST /api/terminal/execute<br>api/client.ts" --> API["TerminalController → AppRunner<br>(app-runtime)"]
    API --> PG["OdcustvService<br>service/OdcustvService.java"]
    PG --> DAO["OdcustvDao<br>dao/impl/OdcustvDaoImpl.java (JDBC)"]
    DAO --> DB[("ORION.CUST")]
    PG -- "ScreenResponse (redirect on PF3)" --> V
    V -- "router → main menu" --> MENU["MMENUA.vue (OCMENU)"]
```

### 1.2 Function overview

| # | Function | Implementation |
|---|---|---|
| 1 | Look up a customer by its customer id | The operator enters the id and the customer record is read by primary key from the DB2 relational table `ORION.CUST` |
| 2 | Show the current customer details in read-only form | Name, address, city, phone and FICO score are displayed; nothing on this screen is editable |
| 3 | Return to the calling menu when finished | The back action ends the view and returns the operator to the main menu |
| 4 | Reach the customer data through the modern data layer | The former embedded SQL is now a JDBC query issued by this program's own data-access class against the same customer table |

**Roles ／ authorisation**: authenticated operator (signed on through the Sign On screen). Sign-on
state travels in the conversation carried between requests; no framework role gate is wired yet.

**Keys → operations** (the SPA renders each former PF key as a button):

| Key | Label as rendered (verbatim) | What it does |
|---|---|---|
| `ENTER` | `ENTER=PROCESS` | read the keyed customer and show its details |
| `PF3` | `PF3=BACK` | return to the main menu |
| `PF4` | `PF4=CLEAR` | clear the entry so the operator can start again |

> The middle column is the string the button really shows, copied from the program's button
> definitions. The physical PF keystrokes are not reproduced as keyboard shortcuts, so operators
> used to typing PF3 now click the `PF3=BACK` button — same action, one extra pointer move.

### 1.3 Databases used

| № | Business name | Table | C | R | U | D |
|---|---|---|---|---|---|---|
| 1 | Customer master (DB2 relational) — read by customer id to show the detail | `ORION.CUST` | - | 〇 | - | - |

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
    ENTRY["Calling menu / transaction"] -- "OD04 / selection" --> S["MCUSTVA<br>Customer View (DB2)"]
    S -- "ENTER: keyed id read and shown" --> S
    S -- "PF3 (back)" --> MENU["MMENUA<br>Main Menu"]
    S -- "blank id / non-numeric / not found / read error" --> MSG["Message line on MCUSTVA"]
```

### 2.1 MCUSTVA — Customer View (DB2)

**Layout**

![TO-BE modernised MCUSTVA](../Image/ODCUSTV_MCUSTVA_TOBE.png)

**Archetype applied**: display form with a single lookup key (`_archetypes/screenModel`). The header
carries the transaction/program/date/time meta strip; the body has one entry box and a read-only
details block; a message line and a button row close the screen.

| Area | Notes |
|---|---|
| Header (Tran/Prog/Date/Time) | meta strip above the title, filled by the server |
| Input area | one entry box — the customer id lookup key |
| Details area | read-only outputs: name, address, city, phone, FICO score |
| Message line | inline alert below the form (was row 23 `ERRMSG`) |
| Button row | `ENTER=PROCESS`, `PF3=BACK`, `PF4=CLEAR` |

**Screen items**

_State legend: ○ shown · 必 must be entered · □ read-only · － not shown._

| № | Item name | Field | Type ／ length | Control | Required | Initial | Key prompt | Record shown | Notes |
|---|---|---|---|---|---|---|---|---|---|
| 1 | Customer ID | `CUSTID` | numeric text, 9 | entry box, 9 chars | 必 | ○ | 必 | □ | lookup key; kept at 9 as in the source |
| 2 | Name | `CUNAME` | text, 50 | read-only | - | － | □ | □ | full name (first / middle / last) |
| 3 | Address | `CUADDR` | text, 50 | read-only | - | － | □ | □ | first address line |
| 4 | City | `CUCITY` | text, 50 | read-only | - | － | □ | □ | address city |
| 5 | Phone | `CUPHONE` | text, 15 | read-only | - | － | □ | □ | primary phone |
| 6 | FICO | `CUFICO` | numeric text, 3 | read-only | - | － | □ | □ | credit score |

> **Length and format unchanged**: the entry box accepts 9 characters and the server reads the same
> 9-character key; the outputs are shown with the same widths as the terminal screen.

## 3. Check specifications

### 3.1 MCUSTVA — Customer View (DB2)

All strings below are the literal messages the server returns to the on-screen message line.

| No. | What is checked | When it is checked | Message code | Message shown | If it fails |
|---|---|---|---|---|---|
| 1 | Opening prompt (informational) | when the screen first appears | — | Enter customer id and press ENTER. | not a failure; guides the operator |
| 2 | The customer id must be entered | on `ENTER`, before the lookup | — | Please enter all required fields. | the entry stays; nothing is read |
| 3 | The customer id must be numeric | on `ENTER`, before the lookup | — | Customer id must be numeric. | the entry stays; no details are shown |
| 4 | The keyed customer must exist | on `ENTER`, after the lookup | — | Record not found. | the entry stays; no details are shown |
| 5 | The customer table must be readable | on `ENTER`, during the lookup | — | Error reading customer table. | the entry stays; no details are shown |
| 6 | Confirmation that the customer was found | on `ENTER`, after a successful read | — | Customer displayed. | not a failure; the details are shown |
| 7 | Only a supported key may be used | on any key press | — | Invalid key pressed. Please try again. | the screen is redisplayed unchanged |

## 4. Event specifications

### 4.1 MCUSTVA — Customer View (DB2)

| No. | Event | What the system does | Result ／ next screen |
|---|---|---|---|
| 1 | The operator enters an id and presses `ENTER` | the keyed customer is read and its details are filled in | stays on Customer View with the details shown, or a message when the id is blank / non-numeric / not found |
| 2 | The operator presses `PF3` | the view ends and control returns to the calling menu | the main menu appears |
| 3 | The operator presses `PF4` | the entered value is cleared and the opening prompt returns | stays on Customer View, ready for a new id |
| 4 | The operator presses an unsupported key | the request is rejected and a message is shown | stays on Customer View, unchanged |

## 5. DB CRUD

**Update spec**

Not applicable — Customer View (DB2) only reads; no row is created, updated or deleted.

**Column-level CRUD (per event ／ business type)**

| Table | Operation | Field | Value source | Transaction |
|---|---|---|---|---|
| `ORION.CUST` | SELECT (read by key) | whole record | the customer id the operator entered | read-only; no commit |

## 6. API contract

| Request | Response | What it does | Errors |
|---|---|---|---|
| `POST /api/terminal/execute` — `{ programName: "ODCUSTV", aidKey, fields: { CUSTID } }` | `ScreenResponse` — `{ fields: { CUNAME, CUADDR, CUCITY, CUPHONE, CUFICO }, message, buttons }` | reads the customer by key from `ORION.CUST` and returns its detail fields, or a message | blank id → "Please enter all required fields."; non-numeric → "Customer id must be numeric."; missing → "Record not found."; read error → "Error reading customer table." |

FE types: `src/programs/odcustv/schemas.d.ts` (`MCUSTVAFields`) · client: `src/api/client.ts` ·
endpoint: `TerminalController` (`/api/terminal/execute`), one round-trip per key press (CICS
pseudo-conversation preserved through the HTTP session).
