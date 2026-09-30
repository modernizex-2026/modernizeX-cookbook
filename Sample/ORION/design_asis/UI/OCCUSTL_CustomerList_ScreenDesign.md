# Screen Design Document — OCCUSTL_CustomerList

## 0. Cover

| Item | Content |
|---|---|
| System | ORION-CCMS (ORION Credit Card Management System) |
| Subsystem | On-line (CICS/BMS) |
| Function name | Customer List |
| Function ID | OCCUSTL |
| CICS transaction | ORLC |
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
    OP["Operator"] --> OCCUSTL["OCCUSTL<br>Customer List"]
    OCCUSTL -- "R" --> T_WS_CUSTFILE[("Customer master<br>WS-CUSTFILE")]
    OCCUSTL -- "PF3 returns" --> X_OCMENU["OCMENU<br>Main Menu"]
```

### 1.2 Function overview

1. List customer records a page at a time from the file.
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
| 1 | Customer master | WS-CUSTFILE | - | 〇 | - | - | VSAM KSDS (EXEC CICS file control) |

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
    ENTRY["Calling menu / transaction"] -- "ORLC / selection" --> S["MCUSTLA<br>Customer List"]
    S -- "PF3 (return)" --> X_OCMENU["MMENUA<br>Main Menu"]
    S -- "invalid key / error" --> MSG["Message line (ERRMSG) on MCUSTLA"]
```

### 2.1 MCUSTLA — Customer List (OCCUSTL)

**Layout**

![AS-IS mockup of MCUSTLA](../Image/MCUSTL_MCUSTLA.png)

*This screen appears when the operator opens the list function; it refreshes on each page key.* Physical size 24×80 (mapset `MCUSTL`, map `MCUSTLA`).

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
| 10 | (literal) | — | caption | ASKIP/NORM | 5 | 5 | 10 | BLUE | — | — | From Cust: | field header |
| 11 | FRCUST | FRCUSTO | input | UNPROT/IC/FSET | 5 | 16 | 9 | GREEN | — | — | — | operator input |
| 12 | (literal) | — | caption | ASKIP/NORM | 7 | 3 | 7 | BLUE | — | — | Cust ID | field header |
| 13 | (literal) | — | caption | ASKIP/NORM | 7 | 14 | 4 | BLUE | — | — | Name | field header |
| 14 | (literal) | — | caption | ASKIP/NORM | 7 | 45 | 4 | BLUE | — | — | FICO | field header |
| 15 | CUL1 | CUL1O | display | ASKIP/NORM | 8 | 3 | 9 | TURQUOISE | — | — | — | program-supplied display |
| 16 | CUN1 | CUN1O | display | ASKIP/NORM | 8 | 14 | 30 | TURQUOISE | — | — | — | program-supplied display |
| 17 | CUF1 | CUF1O | display | ASKIP/NORM | 8 | 45 | 3 | TURQUOISE | — | — | — | program-supplied display |
| 18 | CUL2 | CUL2O | display | ASKIP/NORM | 9 | 3 | 9 | TURQUOISE | — | — | — | program-supplied display |
| 19 | CUN2 | CUN2O | display | ASKIP/NORM | 9 | 14 | 30 | TURQUOISE | — | — | — | program-supplied display |
| 20 | CUF2 | CUF2O | display | ASKIP/NORM | 9 | 45 | 3 | TURQUOISE | — | — | — | program-supplied display |
| 21 | CUL3 | CUL3O | display | ASKIP/NORM | 10 | 3 | 9 | TURQUOISE | — | — | — | program-supplied display |
| 22 | CUN3 | CUN3O | display | ASKIP/NORM | 10 | 14 | 30 | TURQUOISE | — | — | — | program-supplied display |
| 23 | CUF3 | CUF3O | display | ASKIP/NORM | 10 | 45 | 3 | TURQUOISE | — | — | — | program-supplied display |
| 24 | CUL4 | CUL4O | display | ASKIP/NORM | 11 | 3 | 9 | TURQUOISE | — | — | — | program-supplied display |
| 25 | CUN4 | CUN4O | display | ASKIP/NORM | 11 | 14 | 30 | TURQUOISE | — | — | — | program-supplied display |
| 26 | CUF4 | CUF4O | display | ASKIP/NORM | 11 | 45 | 3 | TURQUOISE | — | — | — | program-supplied display |
| 27 | ERRMSG | ERRMSGO | display | ASKIP/NORM | 23 | 1 | 78 | RED | — | — | — | program-supplied display |
| 28 | (literal) | — | caption | ASKIP/NORM | 24 | 1 | 34 | TURQUOISE | — | — | ENTER=Process  PF3=Back  PF4=Clear | field header |

**Item states**

> Legend: `○`=enabled ｜ `必`=enabled (required) ｜ `□`=display only ｜ `×`=hidden ｜ `レ`=initial focus

| № | Field (BMS) | Initial display | Page displayed |
|---|---|---|---|
| 1 | TRNNAME | □ | □ |
| 2 | TITLE | □ | □ |
| 3 | CURDATE | □ | □ |
| 4 | PGMNAME | □ | □ |
| 5 | CURTIME | □ | □ |
| 6 | FRCUST | ○ | □ |
| 7 | CUL1 | □ | □ |
| 8 | CUN1 | □ | □ |
| 9 | CUF1 | □ | □ |
| 10 | CUL2 | □ | □ |
| 11 | CUN2 | □ | □ |
| 12 | CUF2 | □ | □ |
| 13 | CUL3 | □ | □ |
| 14 | CUN3 | □ | □ |
| 15 | CUF3 | □ | □ |
| 16 | CUL4 | □ | □ |
| 17 | CUN4 | □ | □ |
| 18 | CUF4 | □ | □ |
| 19 | ERRMSG | □ | □ |

## 3. Check specifications

### 3.1 MCUSTLA — Customer List

All messages below are the literal strings the program sends to the on-screen message line, extracted from the source. Type: E=Error / W=Warning / C=Confirm / I=Information.

| No. | Timing | Condition | Type | Display | Message content (verbatim) | Notes |
|---|---|---|---|---|---|---|
| 1 | ENTER / validation | an unsupported key was pressed | E | A (message line) | Invalid key pressed. Please try again. | on-screen ERRMSG line |

## 4. Event specifications

### 4.1 MCUSTLA — Customer List

Per-key and per-field behaviour, written as operator steps.

| No. | Item | Timing / condition | Action | Next screen |
|---|---|---|---|---|
| **Function key section** | | | | |
| 1 | `ENTER` | when pressed | the starting key is accepted and the first page of customer rows is listed | stays on the screen, or continues to the main menu (OCMENU) |
| 2 | `PF3` | when pressed | cancel and return to the main menu (OCMENU) | the main menu (OCMENU) |
| 3 | `PF4` | when pressed | clear the entered values so the operator can start again | stays on the screen |
| 4 | `PF8` | when pressed | page forward to the next page of rows | stays on the screen |
| **Data entry section** | | | | |
| 5 | Key field(s) | on ENTER | the starting key is accepted and the first page of customer rows is listed | stays on `MCUSTLA` (or shows a message) |

## 5. DB CRUD

**Update spec**

Not applicable — the Customer List screen only reads data; no table row is created, updated or deleted.

**Column-level CRUD (per event / business type)**

| Screen / table | Physical name | Field group | On ENTER — read |
|---|---|---|---|
| Customer master | WS-CUSTFILE | whole record | R |

