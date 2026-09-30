# TO-BE Screen Design — OCACTIN_AccountInquiry

> **Rule 1: TO-BE = AS-IS in business terms.** The interface moves to a web SPA, but the
> input/output data, the check conditions, the business flow and the database results are
> preserved. The section order follows the AS-IS document so the two compare 1-to-1.

## 0. Cover

| Item | Content |
|---|---|
| System | ORION-CCMS (ORION Credit Card Management System) |
| Subsystem | On-line (was CICS/BMS; now Vue SPA + REST) |
| Function name | Account Inquiry (consolidated) |
| Function ID | OCACTIN（COBOL program）／Trans-ID `ORAI`／Map `MACTINA` |
| Module | `ocactin` |
| Platform | Java 17 · Spring Boot 3.x · Vue 3 + TypeScript · DB Oracle (H2 in dev) |
| Version ／ Author ／ Date | 1 ／ modernizeX ／ 2026-09-28 |

## 1. Function overview

### 1.1 Overview diagram

System architecture — the screen, the request path to the server, the program service it runs, the
linked account browse engine, and the data store the engine reads a page at a time.

```mermaid
---
config:
  layout: elk
  look: neo
  theme: base
---
flowchart LR
    U["Operator (browser)"] --> V["MACTINA.vue<br>src/programs/ocactin/"]
    V -- "POST /api/terminal/execute<br>api/client.ts" --> API["TerminalController → AppRunner<br>(app-runtime)"]
    API --> PG["OcactinService<br>service/OcactinService.java"]
    PG -- "link (account browse engine)" --> SUB["OuactinService<br>OUACTIN"]
    SUB --> DAO["AcctfileFileDao<br>orion-web/dao"]
    DAO --> DB[("acctfile")]
    PG -- "ScreenResponse (redirect on PF3)" --> V
    V -- "router → main menu" --> MENU["MMENUA.vue (OCMENU)"]
```

### 1.2 Function overview

| # | Function | Implementation |
|---|---|---|
| 1 | Accept a status filter for the inquiry | The operator types a filter word (ALL, DELINQUENT, OVER-LIMIT, DORMANT, CLOSED, NEW or HIGH-UTIL) into the entry box |
| 2 | Retrieve and display the matching accounts for review | The linked account browse engine reads accounts in id order and returns a page of up to thirteen that match the filter, with the matched count and the portfolio balance and available totals |
| 3 | Let the operator page through and refine the filter | A forward action fetches the next page, a top action restarts the list, and a new filter re-runs the inquiry |
| 4 | Return to the calling menu when finished | The back action ends the inquiry and returns the operator to the main menu |

**Roles ／ authorisation**: authenticated operator (signed on through the Sign On screen). Sign-on
state travels in the conversation carried between requests; no framework role gate is wired yet.

**Keys → operations** (the SPA renders each former PF key as a button):

| Key | Label as rendered (verbatim) | What it does |
|---|---|---|
| `ENTER` | `ENTER=APPLY` | apply the typed filter and list the first page of matching accounts |
| `PF7` | `PF7=TOP` | restart the list from the top |
| `PF8` | `PF8=FWD` | page forward to the next page of matching accounts |
| `PF3` | `PF3=MENU` | return to the main menu |
| `PF12` | `PF12` | cancel and return to the main menu |
| `PF4` | `PF4` | clear the entry and start again |
| `CLEAR` | `Clear` | clear the screen and redisplay it |

> The middle column is the string the button really shows, copied from the program's button
> definitions. The physical PF keystrokes are not reproduced as keyboard shortcuts, so operators
> used to typing PF8 now click the `PF8=FWD` button — same paging action, one extra pointer move.

### 1.3 Databases used

| № | Business name | Table | C | R | U | D |
|---|---|---|---|---|---|---|
| 1 | Account master — read in id order by the linked browse engine | `acctfile` | - | 〇 | - | - |

> This program itself opens no file; the read is performed by the linked account browse engine
> (OUACTIN), which browses `acctfile` in account-id order and returns each page of matching accounts.

## 2. Screen spec

Screen transition of the whole function — every screen a box, every edge labelled with what moves
the operator. The list is paged: each page shows up to thirteen accounts, read in account-id order by
the linked browse engine, and each next page resumes from the key the previous page returned.

```mermaid
---
config:
  layout: elk
  look: neo
  theme: base
---
flowchart TB
    ENTRY["Calling menu / transaction"] -- "ORAI / selection" --> S["MACTINA<br>Account Inquiry (consolidated)"]
    S -- "ENTER: filter applied, first page listed" --> S
    S -- "PF8: next page (resume in id order)" --> S
    S -- "PF7: restart at the top" --> S
    S -- "PF3 / PF12 (menu)" --> MENU["MMENUA<br>Main Menu"]
    S -- "bad filter / none found / end of selection / engine error / bad key" --> MSG["Message line on MACTINA"]
```

### 2.1 MACTINA — Account Inquiry (consolidated)

**Layout**

![TO-BE modernised MACTINA](../Image/OCACTIN_MACTINA_TOBE.png)

**Archetype applied**: filtered paged list (`_archetypes/screenModel`, LIST). The header carries the
transaction/program/date/time meta strip; the body has one filter box with the recognised words shown
beside it, a thirteen-row result grid (account id, status, balance, credit limit, available amount,
utilisation %), and a totals line; a message line and a button row close the screen.

| Area | Notes |
|---|---|
| Header (Tran/Prog/Date/Time) | meta strip above the title, filled by the server |
| Input area | one filter box, with the recognised words shown beside it (ALL, DELINQUENT, OVER-LIMIT, DORMANT, CLOSED, NEW, HIGH-UTIL) |
| List area | up to thirteen rows, each an account id, status, balance, credit limit, available amount and utilisation % |
| Totals line | current page number, matched count, portfolio balance and portfolio available |
| Message line | inline alert / paging hint below the list (was row 23 `ERRMSG`) |
| Button row | `ENTER=APPLY`, `PF7=TOP`, `PF8=FWD`, `PF3=MENU`, `PF12`, `PF4`, `Clear` |

**Screen items**

_State legend: ○ shown · 必 must be entered · □ read-only · － not shown._

| № | Item name | Field | Type ／ length | Control | Required | Initial | Key prompt | Record shown | Notes |
|---|---|---|---|---|---|---|---|---|---|
| 1 | Filter | `FILT` | text, 10 | entry box, 10 chars | ○ | ○ | ○ | □ | ALL / DELINQUENT / OVER-LIMIT / DORMANT / CLOSED / NEW / HIGH-UTIL; blank is treated as ALL |
| 2 | Filter description | `FDESC` | text, 12 | read-only | - | ○ | - | □ | the filter now in effect, in words |
| 3 | Account ID (each row) | `AID1`…`AID13` | numeric text, 11 | read-only | - | － | □ | □ | account id for each listed row |
| 4 | Status (each row) | `AST1`…`AST13` | text, 1 | read-only | - | － | □ | □ | active status code for each listed row |
| 5 | Balance (each row) | `ABL1`…`ABL13` | amount text, 15 | read-only | - | － | □ | □ | current balance for each listed row |
| 6 | Credit limit (each row) | `ALM1`…`ALM13` | amount text, 15 | read-only | - | － | □ | □ | credit limit for each listed row |
| 7 | Available (each row) | `AAV1`…`AAV13` | amount text, 15 | read-only | - | － | □ | □ | available amount for each listed row |
| 8 | Utilisation % (each row) | `AUT1`…`AUT13` | text, 6 | read-only | - | － | □ | □ | utilisation percentage for each listed row |
| 9 | Page number | `PAGENO` | numeric text, 3 | read-only | - | ○ | - | □ | current page of the result set |
| 10 | Matched count | `RUNTOT` | numeric text, 7 | read-only | - | ○ | - | □ | running count of accounts matched so far |
| 11 | Portfolio balance | `PGBAL` | amount text, 20 | read-only | - | ○ | - | □ | total balance across the matched accounts |
| 12 | Portfolio available | `PGAVL` | amount text, 20 | read-only | - | ○ | - | □ | total available across the matched accounts |

> **Length and format unchanged**: the filter box accepts 10 characters and each result row keeps the
> same column widths (account id 11, status 1, balance 15, credit limit 15, available 15, utilisation 6)
> as the terminal screen; the grid keeps its thirteen rows per page.

## 3. Check specifications

### 3.1 MACTINA — Account Inquiry (consolidated)

All strings below are the literal messages the server returns to the on-screen message line.
Type: E=Error / I=Information.

| No. | What is checked | When it is checked | Message code | Message shown | If it fails |
|---|---|---|---|---|---|
| 1 | Opening prompt (informational) | when the screen first appears | — | Type a filter and press ENTER. | not a failure; guides the operator |
| 2 | The filter word must be recognised | on `ENTER`, before the inquiry | — | Filter not recognised - see the list above. | nothing is listed; the entry stays |
| 3 | A filter must be applied before paging | on `PF7` / `PF8`, when no list is active | — | Apply a filter first (press ENTER). | no paging happens |
| 4 | There must be more accounts to page to | on `PF8`, when the last page is reached | — | End of selection - no more accounts. | the page stays as it was |
| 5 | The filter must match at least one account | on `ENTER`, after the inquiry | — | No accounts match that filter. | an empty list is shown |
| 6 | The browse engine must be reachable | on `ENTER` / `PF7` / `PF8`, during the inquiry | — | Unable to reach the account browse engine. | no page is shown |
| 7 | The account file must be browseable | on `ENTER` / `PF7` / `PF8`, during the inquiry | — | Error browsing the account file. | no page is shown |
| 8 | More pages remain (informational) | after a page that has more accounts | — | … shown - PF8=more PF7=top | not a failure; offers paging |
| 9 | The last page has been reached (informational) | after the final page | — | … shown - end of selection | not a failure; the list is complete |
| 10 | Only a supported key may be used | on any key press | — | Invalid key pressed. Please try again. | the screen is redisplayed unchanged |

## 4. Event specifications

### 4.1 MACTINA — Account Inquiry (consolidated)

| No. | Event | What the system does | Result ／ next screen |
|---|---|---|---|
| 1 | The operator types a filter and presses `ENTER` | the filter is recognised and the browse engine returns the first page of matching accounts with the matched count and the portfolio totals | stays on Account Inquiry with the first page, or a message when the filter is unknown or nothing matches |
| 2 | The operator presses `PF8` | the browse engine resumes from where the last page stopped and returns the next page | stays on Account Inquiry with the next page, or a message when there are no more |
| 3 | The operator presses `PF7` | the list restarts from the top for the same filter | stays on Account Inquiry showing the first page |
| 4 | The operator presses `PF3` or `PF12` | the inquiry ends and control returns to the calling menu | the main menu appears |
| 5 | The operator presses `PF4` or `CLEAR` | the screen is cleared and the opening prompt returns | stays on Account Inquiry, ready for a new filter |
| 6 | The operator presses an unsupported key | the request is rejected and a message is shown | stays on Account Inquiry, unchanged |

## 5. DB CRUD

**Update spec**

Not applicable — Account Inquiry only reads, and it does so through the linked account browse engine;
no row is created, updated or deleted. The engine browses `acctfile` in account-id order
(ORDER BY `ac_id`), keeps the rows that match the chosen status filter, returns a page of up to
thirteen with the matched count and the portfolio balance and available totals, and hands back a
resume key so the next page continues in order.

**Column-level CRUD (per event ／ business type)**

| Table | Operation | Field | Value source | Transaction |
|---|---|---|---|---|
| `acctfile` | SELECT (ordered browse by `ac_id`, via the linked engine) | account id, status, balance, credit limit, available, utilisation | resumes from the saved key; filtered by the chosen status | read-only; no commit |

## 6. API contract

| Request | Response | What it does | Errors |
|---|---|---|---|
| `POST /api/terminal/execute` — `{ programName: "OCACTIN", aidKey, fields: { FILT } }` | `ScreenResponse` — `{ fields: { AID1..AID13, AST1..AST13, ABL1..ABL13, ALM1..ALM13, AAV1..AAV13, AUT1..AUT13, PAGENO, RUNTOT, PGBAL, PGAVL }, message, buttons }` | applies the filter and returns a page of matching accounts; `aidKey` selects apply (`ENTER`), top (`PF7`) or next (`PF8`) | unknown filter → "Filter not recognised - see the list above."; none → "No accounts match that filter."; page past end → "End of selection - no more accounts."; engine down → "Unable to reach the account browse engine." |

FE types: `src/programs/ocactin/schemas.d.ts` (`MACTINAFields`) · client: `src/api/client.ts` ·
endpoint: `TerminalController` (`/api/terminal/execute`), one round-trip per key press (CICS
pseudo-conversation, and the paging resume key, preserved through the HTTP session).
