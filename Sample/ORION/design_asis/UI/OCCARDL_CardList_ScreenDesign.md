# Screen Design Document — OCCARDL_CardList

## 0. Cover

| Item | Content |
|---|---|
| System | ORION-CCMS (ORION Credit Card Management System) |
| Subsystem | On-line (CICS/BMS) |
| Function name | Card List for an account |
| Function ID | OCCARDL |
| CICS transaction | ORCL |
| Version | 1 |
| Author / Created on | modernizeX／2026-09-28 |
| Updated by / Updated on | modernizeX／2026-09-28 |

Approval frame: Customer｜Author｜Approver｜Responsible person

## 1. Function overview

### 1.1 Overview diagram

System architecture — the transaction program, the files/tables it touches, the sub-programs it links to, and where it hands control.

```mermaid
---
config:
  layout: elk
  look: neo
  theme: base
---
flowchart LR
    OP["Operator"] --> OCCARDL["OCCARDL<br>Card List for an account"]
    OCCARDL -- "R" --> T_WS_CARDFILE[("Card master<br>WS-CARDFILE")]
    OCCARDL -- "PF3 returns" --> X_OCMENU["OCMENU<br>Main Menu"]
```

### 1.2 Function overview

1. List account records a page at a time from the file.
2. Let the operator page forward and backward through the result set.
3. Let the operator select a row to view its detail.

**Roles:** authenticated operator (signed on through the Sign On screen).

**Function keys**

| Key | Label as rendered (verbatim) | What it does |
|---|---|---|
| `ENTER` | `ENTER=Process` | validate the entry and process the current step |
| `PF3` | `PF3=Back` | return to the main menu (OCMENU) |
| `PF4` | `PF4=Clear` | clear the entered values and start again |

### 1.3 Databases used

| № | Logical table name | Physical table/file name | Create(C) | Read(R) | Update(U) | Delete(D) | Notes |
|---|---|---|---|---|---|---|---|
| 1 | Card master | WS-CARDFILE | - | 〇 | - | - | VSAM KSDS (EXEC CICS file control) |

## 2. Screen spec

Screen transition of the whole function — every screen a box, every edge labelled with what moves the operator.

```mermaid
---
config:
  layout: elk
  look: neo
  theme: base
---
flowchart TB
    ENTRY["Calling menu / transaction"] -- "ORCL / selection" --> S["MCARDLA<br>Card List for an account"]
    S -- "PF3 (return)" --> X_OCMENU["MMENUA<br>Main Menu"]
    S -- "invalid key / error" --> MSG["Message line (ERRMSG) on MCARDLA"]
```

### 2.1 MCARDLA — Card List for an account (OCCARDL)

**Layout**

![AS-IS mockup of MCARDLA](../Image/MCARDL_MCARDLA.png)

*This screen appears when the operator opens the list function; it refreshes on each page key.* Physical size 24×80 (mapset `MCARDL`, map `MCARDLA`).

**Item details**

| № | Field (BMS) | COBOL symbolic | Kind | Attributes | Line | Col | Character count (screen columns) | Colour | picin | picout | Initial value / caption | Notes |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 1 | (literal) | — | caption | ASKIP/NORM | 1 | 1 | 5 | BLUE | — | — | Tran: | field header |
| 2 | TRNNAME | TRNNAMEO | display | ASKIP/NORM | 1 | 7 | 4 | BLUE | — | — | — | program-supplied display |
| 3 | TITLE | TITLEO | display | ASKIP/NORM | 1 | 21 | 40 | YELLOW | — | — | — | program-supplied display |
| 4 | (literal) | — | caption | ASKIP/NORM | 1 | 65 | 5 | BLUE | — | — | Date: | field header |
| 5 | CURDATE | CURDATEO | display | ASKIP/NORM | 1 | 71 | 10 | BLUE | — | — | — | program-supplied display |
| 6 | (literal) | — | caption | ASKIP/NORM | 2 | 1 | 5 | BLUE | — | — | Pgm : | field header |
| 7 | PGMNAME | PGMNAMEO | display | ASKIP/NORM | 2 | 7 | 8 | BLUE | — | — | — | program-supplied display |
| 8 | (literal) | — | caption | ASKIP/NORM | 2 | 65 | 5 | BLUE | — | — | Time: | field header |
| 9 | CURTIME | CURTIMEO | display | ASKIP/NORM | 2 | 71 | 8 | BLUE | — | — | — | program-supplied display |
| 10 | (literal) | — | caption | ASKIP/NORM | 5 | 5 | 8 | BLUE | — | — | Acct ID: | field header |
| 11 | ACCTID | ACCTIDO | input | UNPROT/IC/FSET | 5 | 14 | 11 | GREEN | — | — | — | operator input |
| 12 | (literal) | — | caption | ASKIP/NORM | 7 | 3 | 11 | BLUE | — | — | Card Number | field header |
| 13 | (literal) | — | caption | ASKIP/NORM | 7 | 25 | 6 | BLUE | — | — | Status | field header |
| 14 | CARD1 | CARD1O | display | ASKIP/NORM | 8 | 3 | 16 | TURQUOISE | — | — | — | program-supplied display |
| 15 | STAT1 | STAT1O | display | ASKIP/NORM | 8 | 25 | 1 | TURQUOISE | — | — | — | program-supplied display |
| 16 | CARD2 | CARD2O | display | ASKIP/NORM | 9 | 3 | 16 | TURQUOISE | — | — | — | program-supplied display |
| 17 | STAT2 | STAT2O | display | ASKIP/NORM | 9 | 25 | 1 | TURQUOISE | — | — | — | program-supplied display |
| 18 | CARD3 | CARD3O | display | ASKIP/NORM | 10 | 3 | 16 | TURQUOISE | — | — | — | program-supplied display |
| 19 | STAT3 | STAT3O | display | ASKIP/NORM | 10 | 25 | 1 | TURQUOISE | — | — | — | program-supplied display |
| 20 | CARD4 | CARD4O | display | ASKIP/NORM | 11 | 3 | 16 | TURQUOISE | — | — | — | program-supplied display |
| 21 | STAT4 | STAT4O | display | ASKIP/NORM | 11 | 25 | 1 | TURQUOISE | — | — | — | program-supplied display |
| 22 | CARD5 | CARD5O | display | ASKIP/NORM | 12 | 3 | 16 | TURQUOISE | — | — | — | program-supplied display |
| 23 | STAT5 | STAT5O | display | ASKIP/NORM | 12 | 25 | 1 | TURQUOISE | — | — | — | program-supplied display |
| 24 | ERRMSG | ERRMSGO | display | ASKIP/NORM | 23 | 1 | 78 | RED | — | — | — | program-supplied display |
| 25 | (literal) | — | caption | ASKIP/NORM | 24 | 1 | 34 | TURQUOISE | — | — | ENTER=Process  PF3=Back  PF4=Clear | field header |

**Item states**

> Legend: `○`=enabled ｜ `必`=enabled (required) ｜ `□`=display only ｜ `×`=hidden ｜ `レ`=initial focus

| № | Field (BMS) | Initial display | Page displayed |
|---|---|---|---|
| 1 | TRNNAME | □ | □ |
| 2 | TITLE | □ | □ |
| 3 | CURDATE | □ | □ |
| 4 | PGMNAME | □ | □ |
| 5 | CURTIME | □ | □ |
| 6 | ACCTID | ○ | □ |
| 7 | CARD1 | □ | □ |
| 8 | STAT1 | □ | □ |
| 9 | CARD2 | □ | □ |
| 10 | STAT2 | □ | □ |
| 11 | CARD3 | □ | □ |
| 12 | STAT3 | □ | □ |
| 13 | CARD4 | □ | □ |
| 14 | STAT4 | □ | □ |
| 15 | CARD5 | □ | □ |
| 16 | STAT5 | □ | □ |
| 17 | ERRMSG | □ | □ |

## 3. Check specifications

### 3.1 MCARDLA — Card List for an account

All messages below are the literal strings the program sends to the on-screen message line, extracted from the source. Type: E=Error / W=Warning / C=Confirm / I=Information.

| No. | Timing | Condition | Type | Display | Message content (verbatim) | Notes |
|---|---|---|---|---|---|---|
| 1 | ENTER / validation | processing state | C | A (message line) | Enter account id and press ENTER to list cards. | on-screen ERRMSG line |
| 2 | ENTER / validation | processing state | I | A (message line) | List an account first (press ENTER). | on-screen ERRMSG line |
| 3 | ENTER / validation | processing state | I | A (message line) | No more cards for this account. | on-screen ERRMSG line |
| 4 | ENTER / validation | processing state | I | A (message line) | No cards found for this account. | on-screen ERRMSG line |
| 5 | ENTER / validation | processing state | I | A (message line) | Cards listed. PF8=next PF7=top PF3=menu. | on-screen ERRMSG line |
| 6 | ENTER / validation | processing state | I | A (message line) | End of list. PF7=top PF3=menu. | on-screen ERRMSG line |
| 7 | ENTER / validation | a numeric field held non-numeric data | E | A (message line) | Account id must be numeric. | on-screen ERRMSG line |
| 8 | ENTER / validation | an unsupported key was pressed | E | A (message line) | Invalid key pressed. Please try again. | on-screen ERRMSG line |
| 9 | ENTER / validation | a required field was left blank | E | A (message line) | Please enter all required fields. | on-screen ERRMSG line |

## 4. Event specifications

### 4.1 MCARDLA — Card List for an account

Per-key and per-field behaviour, written as operator steps.

| No. | Item | Timing / condition | Action | Next screen |
|---|---|---|---|---|
| **Function key section** | | | | |
| 1 | `ENTER` | when pressed | the starting key is accepted and the first page of account rows is listed | stays on the screen, or continues to the main menu (OCMENU) |
| 2 | `PF3` | when pressed | cancel and return to the main menu (OCMENU) | the main menu (OCMENU) |
| 3 | `PF4` | when pressed | clear the entered values so the operator can start again | stays on the screen |
| 4 | `PF12` | when pressed | cancel the current action and return to the main menu (OCMENU) | the main menu (OCMENU) |
| 5 | `CLEAR` | when pressed | clear the screen and redisplay it | stays on the screen |
| 6 | `PF7` | when pressed | page backward to the previous page of rows | stays on the screen |
| 7 | `PF8` | when pressed | page forward to the next page of rows | stays on the screen |
| **Data entry section** | | | | |
| 8 | Key field(s) | on ENTER | the starting key is accepted and the first page of account rows is listed | stays on `MCARDLA` (or shows a message) |

## 5. DB CRUD

**Update spec**

Not applicable — the Card List for an account screen only reads data; no table row is created, updated or deleted.

**Column-level CRUD (per event / business type)**

| Screen / table | Physical name | Field group | On ENTER — read |
|---|---|---|---|
| Card master | WS-CARDFILE | whole record | R |

