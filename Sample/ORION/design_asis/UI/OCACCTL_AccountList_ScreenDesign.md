# Screen Design Document — OCACCTL_AccountList

## 0. Cover

| Item | Content |
|---|---|
| System | ORION-CCMS (ORION Credit Card Management System) |
| Subsystem | On-line (CICS/BMS) |
| Function name | Account List |
| Function ID | OCACCTL |
| CICS transaction | ORLA |
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
    OP["Operator"] --> OCACCTL["OCACCTL<br>Account List"]
    OCACCTL -- "R" --> T_WS_ACCTFILE[("Account master<br>WS-ACCTFILE")]
    OCACCTL -- "PF3 returns" --> X_OCMENU["OCMENU<br>Main Menu"]
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
| 1 | Account master | WS-ACCTFILE | - | 〇 | - | - | VSAM KSDS (EXEC CICS file control) |

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
    ENTRY["Calling menu / transaction"] -- "ORLA / selection" --> S["MACCTLA<br>Account List"]
    S -- "PF3 (return)" --> X_OCMENU["MMENUA<br>Main Menu"]
    S -- "invalid key / error" --> MSG["Message line (ERRMSG) on MACCTLA"]
```

### 2.1 MACCTLA — Account List (OCACCTL)

**Layout**

![AS-IS mockup of MACCTLA](../Image/MACCTL_MACCTLA.png)

*This screen appears when the operator opens the list function; it refreshes on each page key.* Physical size 24×80 (mapset `MACCTL`, map `MACCTLA`).

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
| 10 | (literal) | — | caption | ASKIP/NORM | 5 | 5 | 10 | BLUE | — | — | From Acct: | field header |
| 11 | FRACCT | FRACCTO | input | UNPROT/IC/FSET | 5 | 16 | 11 | GREEN | — | — | — | operator input |
| 12 | (literal) | — | caption | ASKIP/NORM | 7 | 3 | 10 | BLUE | — | — | Account ID | field header |
| 13 | (literal) | — | caption | ASKIP/NORM | 7 | 18 | 6 | BLUE | — | — | Status | field header |
| 14 | (literal) | — | caption | ASKIP/NORM | 7 | 28 | 7 | BLUE | — | — | Balance | field header |
| 15 | ACL1 | ACL1O | display | ASKIP/NORM | 8 | 3 | 11 | TURQUOISE | — | — | — | program-supplied display |
| 16 | ACS1 | ACS1O | display | ASKIP/NORM | 8 | 18 | 1 | TURQUOISE | — | — | — | program-supplied display |
| 17 | ACB1 | ACB1O | display | ASKIP/NORM | 8 | 28 | 15 | TURQUOISE | — | — | — | program-supplied display |
| 18 | ACL2 | ACL2O | display | ASKIP/NORM | 9 | 3 | 11 | TURQUOISE | — | — | — | program-supplied display |
| 19 | ACS2 | ACS2O | display | ASKIP/NORM | 9 | 18 | 1 | TURQUOISE | — | — | — | program-supplied display |
| 20 | ACB2 | ACB2O | display | ASKIP/NORM | 9 | 28 | 15 | TURQUOISE | — | — | — | program-supplied display |
| 21 | ACL3 | ACL3O | display | ASKIP/NORM | 10 | 3 | 11 | TURQUOISE | — | — | — | program-supplied display |
| 22 | ACS3 | ACS3O | display | ASKIP/NORM | 10 | 18 | 1 | TURQUOISE | — | — | — | program-supplied display |
| 23 | ACB3 | ACB3O | display | ASKIP/NORM | 10 | 28 | 15 | TURQUOISE | — | — | — | program-supplied display |
| 24 | ACL4 | ACL4O | display | ASKIP/NORM | 11 | 3 | 11 | TURQUOISE | — | — | — | program-supplied display |
| 25 | ACS4 | ACS4O | display | ASKIP/NORM | 11 | 18 | 1 | TURQUOISE | — | — | — | program-supplied display |
| 26 | ACB4 | ACB4O | display | ASKIP/NORM | 11 | 28 | 15 | TURQUOISE | — | — | — | program-supplied display |
| 27 | ACL5 | ACL5O | display | ASKIP/NORM | 12 | 3 | 11 | TURQUOISE | — | — | — | program-supplied display |
| 28 | ACS5 | ACS5O | display | ASKIP/NORM | 12 | 18 | 1 | TURQUOISE | — | — | — | program-supplied display |
| 29 | ACB5 | ACB5O | display | ASKIP/NORM | 12 | 28 | 15 | TURQUOISE | — | — | — | program-supplied display |
| 30 | ERRMSG | ERRMSGO | display | ASKIP/NORM | 23 | 1 | 78 | RED | — | — | — | program-supplied display |
| 31 | (literal) | — | caption | ASKIP/NORM | 24 | 1 | 34 | TURQUOISE | — | — | ENTER=Process  PF3=Back  PF4=Clear | field header |

**Item states**

> Legend: `○`=enabled ｜ `必`=enabled (required) ｜ `□`=display only ｜ `×`=hidden ｜ `レ`=initial focus

| № | Field (BMS) | Initial display | Page displayed |
|---|---|---|---|
| 1 | TRNNAME | □ | □ |
| 2 | TITLE | □ | □ |
| 3 | CURDATE | □ | □ |
| 4 | PGMNAME | □ | □ |
| 5 | CURTIME | □ | □ |
| 6 | FRACCT | ○ | □ |
| 7 | ACL1 | □ | □ |
| 8 | ACS1 | □ | □ |
| 9 | ACB1 | □ | □ |
| 10 | ACL2 | □ | □ |
| 11 | ACS2 | □ | □ |
| 12 | ACB2 | □ | □ |
| 13 | ACL3 | □ | □ |
| 14 | ACS3 | □ | □ |
| 15 | ACB3 | □ | □ |
| 16 | ACL4 | □ | □ |
| 17 | ACS4 | □ | □ |
| 18 | ACB4 | □ | □ |
| 19 | ACL5 | □ | □ |
| 20 | ACS5 | □ | □ |
| 21 | ACB5 | □ | □ |
| 22 | ERRMSG | □ | □ |

## 3. Check specifications

### 3.1 MACCTLA — Account List

All messages below are the literal strings the program sends to the on-screen message line, extracted from the source. Type: E=Error / W=Warning / C=Confirm / I=Information.

| No. | Timing | Condition | Type | Display | Message content (verbatim) | Notes |
|---|---|---|---|---|---|---|
| 1 | ENTER / validation | an unsupported key was pressed | E | A (message line) | Invalid key pressed. Please try again. | on-screen ERRMSG line |

## 4. Event specifications

### 4.1 MACCTLA — Account List

Per-key and per-field behaviour, written as operator steps.

| No. | Item | Timing / condition | Action | Next screen |
|---|---|---|---|---|
| **Function key section** | | | | |
| 1 | `ENTER` | when pressed | the starting key is accepted and the first page of account rows is listed | stays on the screen, or continues to the main menu (OCMENU) |
| 2 | `PF3` | when pressed | cancel and return to the main menu (OCMENU) | the main menu (OCMENU) |
| 3 | `PF4` | when pressed | clear the entered values so the operator can start again | stays on the screen |
| 4 | `PF8` | when pressed | page forward to the next page of rows | stays on the screen |
| **Data entry section** | | | | |
| 5 | Key field(s) | on ENTER | the starting key is accepted and the first page of account rows is listed | stays on `MACCTLA` (or shows a message) |

## 5. DB CRUD

**Update spec**

Not applicable — the Account List screen only reads data; no table row is created, updated or deleted.

**Column-level CRUD (per event / business type)**

| Screen / table | Physical name | Field group | On ENTER — read |
|---|---|---|---|
| Account master | WS-ACCTFILE | whole record | R |

