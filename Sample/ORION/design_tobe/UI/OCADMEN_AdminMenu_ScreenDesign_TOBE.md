# TO-BE Screen Design — OCADMEN_AdminMenu

> **Rule 1: TO-BE = AS-IS in business terms.** The interface moves to a web SPA, but the
> input/output data, the check conditions, the business flow and the routing are preserved. The
> section order follows the AS-IS document so the two compare 1-to-1.

## 0. Cover

| Item | Content |
|---|---|
| System | ORION-CCMS (ORION Credit Card Management System) |
| Subsystem | On-line (was CICS/BMS; now Vue SPA + REST) |
| Function name | Administrator Menu |
| Function ID | OCADMEN（COBOL program）／Trans-ID `ORAD`／Map `MADMENA` |
| Module | `ocadmen` |
| Platform | Java 17 · Spring Boot 3.x · Vue 3 + TypeScript · DB Oracle (H2 in dev) |
| Version ／ Author ／ Date | 1 ／ modernizeX ／ 2026-09-28 |

## 1. Function overview

### 1.1 Overview diagram

System architecture — the screen, the request path to the server, the program service it runs, and
the administration functions it routes the operator to.

```mermaid
---
config:
  layout: elk
  look: neo
  theme: base
---
flowchart LR
    U["Operator (browser)"] --> V["MADMENA.vue<br>src/programs/ocadmen/"]
    V -- "POST /api/terminal/execute<br>api/client.ts" --> API["TerminalController → AppRunner<br>(app-runtime)"]
    API --> PG["OcadmenService<br>service/OcadmenService.java"]
    PG -- "option routes to (redirect)" --> T["OCUSRL · OCUSRA · OCUSRU · OCUSRD<br>User List / Add / Update / Delete"]
    PG -- "PF3 → sign off" --> SGN["OCSGNON<br>Sign On"]
    PG -- "non-administrator guard → main menu" --> MENU["OCMENU<br>Main Menu"]
    PG -- "ScreenResponse (redirect)" --> V
```

### 1.2 Function overview

| # | Function | Implementation |
|---|---|---|
| 1 | Present the administrator functions as a numbered option list | Four choices are shown and the operator types the number of the one they want |
| 2 | Route the operator to the matching user-administration function | The chosen number is checked and control passes to the corresponding program |
| 3 | Sign off and return to the sign-on screen on exit | The back action leaves the menu and returns the operator to the sign-on screen |

**Roles ／ authorisation**: authenticated operator (signed on through the Sign On screen). Only an
operator whose sign-on profile is an administrator may use this menu; anyone else is routed straight
to the main menu. No framework role gate is wired yet.

**Keys → operations** (the SPA renders each former PF key as a button):

| Key | Label as rendered (verbatim) | What it does |
|---|---|---|
| `ENTER` | `ENTER=PROCESS` | check the chosen option and route to the matching user-admin function |
| `PF3` | `PF3=BACK` | sign off and return to the sign-on screen |
| `PF4` | `PF4=CLEAR` | clear the entered option so the operator can start again |

> The middle column is the string the button really shows, copied from the program's button
> definitions. The physical PF keystrokes are not reproduced as keyboard shortcuts, so operators
> used to typing PF3 now click the `PF3=BACK` button — same action, one extra pointer move.

### 1.3 Databases used

| № | Business name | Table | C | R | U | D |
|---|---|---|---|---|---|---|
| 1 | Navigation only — the target function opens its own data | — | - | - | - | - |

## 2. Screen spec

Screen transition of the whole function — every screen a box, every edge labelled with what moves
the operator. Each menu number routes to one user-administration function.

```mermaid
---
config:
  layout: elk
  look: neo
  theme: base
---
flowchart TB
    ENTRY["Sign On / calling transaction"] -- "ORAD / after sign-on" --> S["MADMENA<br>Administrator Menu"]
    S -- "option 1" --> O1["MUSRLA<br>User List"]
    S -- "option 2" --> O2["MUSRAA<br>User Add"]
    S -- "option 3" --> O3["MUSRUA<br>User Update"]
    S -- "option 4" --> O4["MUSRDA<br>User Delete"]
    S -- "PF3 (sign off)" --> SGN["MSGNONA<br>Sign On"]
    S -- "invalid option / invalid key" --> MSG["Message line on MADMENA"]
```

### 2.1 MADMENA — Administrator Menu

**Layout**

![TO-BE modernised MADMENA](../Image/OCADMEN_MADMENA_TOBE.png)

**Archetype applied**: routing menu with a single option entry (`_archetypes/screenModel`). The
header carries the transaction/program/date/time meta strip; the body lists the four numbered
choices with one option box; a message line and a button row close the screen.

| Area | Notes |
|---|---|
| Header (Tran/Prog/Date/Time) | meta strip above the title, filled by the server |
| Option list | four fixed captions: User List, User Add, User Update, User Delete |
| Input area | one entry box — the option number (1–4) |
| Message line | inline alert below the list (was row 23 `ERRMSG`) |
| Button row | `ENTER=PROCESS`, `PF3=BACK`, `PF4=CLEAR` |

**Screen items**

_State legend: ○ shown · 必 must be entered · □ read-only · － not shown._

| № | Item name | Field | Type ／ length | Control | Required | Initial | Key prompt | Record shown | Notes |
|---|---|---|---|---|---|---|---|---|---|
| 1 | Transaction id | `TRNNAME` | text, 4 | read-only | - | ○ | － | □ | header meta, server-filled |
| 2 | Screen title | `TITLE` | text, 40 | read-only | - | ○ | － | □ | header meta |
| 3 | Current date | `CURDATE` | date text, 10 | read-only | - | ○ | － | □ | header meta |
| 4 | Program name | `PGMNAME` | text, 8 | read-only | - | ○ | － | □ | header meta |
| 5 | Current time | `CURTIME` | time text, 8 | read-only | - | ○ | － | □ | header meta |
| 6 | Option | `OPTION` | numeric text, 2 | entry box, 2 chars | 必 | ○ | 必 | － | the menu number 1–4 |
| 7 | Message line | `ERRMSG` | text, 78 | read-only | - | － | － | □ | prompt and result messages |

> **Length and format unchanged**: the option box accepts the same 2 characters as the terminal
> field and the four captions keep their original wording.

## 3. Check specifications

### 3.1 MADMENA — Administrator Menu

All strings below are the literal messages the server returns to the on-screen message line.
Type: E=Error / I=Information.

| No. | What is checked | When it is checked | Message code | Message shown | If it fails |
|---|---|---|---|---|---|
| 1 | Opening prompt (informational) | when the screen first appears | — | Select an option (1-4) and press ENTER. | not a failure; guides the operator |
| 2 | The chosen option must be one of 1–4 | on `ENTER`, before routing | — | Invalid option. Choose 1 through 4. | the menu is redisplayed; no routing happens |
| 3 | Only a supported key may be used | on any key press | — | Invalid key pressed. Please try again. | the screen is redisplayed unchanged |

## 4. Event specifications

### 4.1 MADMENA — Administrator Menu

| No. | Event | What the system does | Result ／ next screen |
|---|---|---|---|
| 1 | The operator types an option and presses `ENTER` | the number is checked and, when it is 1–4, control passes to the matching function | routes to User List (1), User Add (2), User Update (3) or User Delete (4) |
| 2 | The operator types a number outside 1–4 and presses `ENTER` | the choice is rejected and a message is shown | stays on the Administrator Menu with the invalid-option message |
| 3 | The operator presses `PF3` | the menu ends and the operator is signed off | the sign-on screen appears |
| 4 | The operator presses `PF4` | the entered option is cleared and the opening prompt returns | stays on the Administrator Menu, ready for a new option |
| 5 | The operator presses an unsupported key | the request is rejected and a message is shown | stays on the Administrator Menu, unchanged |

## 5. DB CRUD

**Update spec**

Not applicable — the Administrator Menu only routes to other functions; it does not create, read,
update or delete any record. File access is performed by the target program the operator selects.

**Column-level CRUD (per event ／ business type)**

| Table | Operation | Field | Value source | Transaction |
|---|---|---|---|---|
| — (navigation only) | none | — | the option the operator selected | routing only; no commit |

## 6. API contract

| Request | Response | What it does | Errors |
|---|---|---|---|
| `POST /api/terminal/execute` — `{ programName: "OCADMEN", aidKey, fields: { OPTION } }` | `ScreenResponse` — `{ redirect, message, buttons }` | checks the option and returns a redirect to the chosen user-admin program, or a message | out of range → "Invalid option. Choose 1 through 4."; unsupported key → "Invalid key pressed. Please try again." |

FE types: `src/programs/ocadmen/schemas.d.ts` (`MADMENAFields`) · client: `src/api/client.ts` ·
endpoint: `TerminalController` (`/api/terminal/execute`); a valid option returns a redirect that the
SPA router follows to the next program (CICS pseudo-conversation preserved through the HTTP session).
