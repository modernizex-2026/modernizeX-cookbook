# Screen Design Document — ODTRANL_TransactionListDB2

## 0. Cover

| Item | Content |
|---|---|
| System | ORION-CCMS (ORION Credit Card Management System) |
| Subsystem | On-line (CICS/BMS) — DB2 variant |
| Function name | Transaction List (DB2) |
| Function ID | ODTRANL |
| CICS transaction | OD05 |
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
    OP["Operator"] --> ODTRANL["ODTRANL<br>Transaction List (DB2)"]
    ODTRANL -- "R" --> T_ORION_TRAN[("Transaction (relational)<br>ORION.TRAN")]
    ODTRANL -- "PF3 returns" --> X_OCMENU["OCMENU<br>Main Menu"]
```

### 1.2 Function overview

1. List transaction records a page at a time from the file.
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
| 1 | Transaction (relational) | ORION.TRAN | - | 〇 | - | - | DB2 relational table (EXEC SQL) |

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
    ENTRY["Calling menu / transaction"] -- "OD05 / selection" --> S["MTRANLA<br>Transaction List (DB2)"]
    S -- "PF3 (return)" --> X_OCMENU["MMENUA<br>Main Menu"]
    S -- "invalid key / error" --> MSG["Message line (ERRMSG) on MTRANLA"]
```

### 2.1 MTRANLA — Transaction List (DB2) (ODTRANL)

**Layout**

![AS-IS mockup of MTRANLA](../Image/MTRANL_MTRANLA.png)

*This screen appears when the operator opens the list function; it refreshes on each page key.* Physical size 24×80 (mapset `MTRANL`, map `MTRANLA`).

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
| 10 | (literal) | — | caption | ASKIP/NORM | 5 | 5 | 9 | BLUE | — | — | Card Num: | field header |
| 11 | CARDNUM | CARDNUMO | input | UNPROT/IC/FSET | 5 | 15 | 16 | GREEN | — | — | — | operator input |
| 12 | (literal) | — | caption | ASKIP/NORM | 7 | 3 | 7 | BLUE | — | — | Tran ID | field header |
| 13 | (literal) | — | caption | ASKIP/NORM | 7 | 22 | 6 | BLUE | — | — | Amount | field header |
| 14 | (literal) | — | caption | ASKIP/NORM | 7 | 40 | 4 | BLUE | — | — | Type | field header |
| 15 | TRN1 | TRN1O | display | ASKIP/NORM | 8 | 3 | 16 | TURQUOISE | — | — | — | program-supplied display |
| 16 | AMT1 | AMT1O | display | ASKIP/NORM | 8 | 22 | 15 | TURQUOISE | — | — | — | program-supplied display |
| 17 | TYP1 | TYP1O | display | ASKIP/NORM | 8 | 40 | 2 | TURQUOISE | — | — | — | program-supplied display |
| 18 | TRN2 | TRN2O | display | ASKIP/NORM | 9 | 3 | 16 | TURQUOISE | — | — | — | program-supplied display |
| 19 | AMT2 | AMT2O | display | ASKIP/NORM | 9 | 22 | 15 | TURQUOISE | — | — | — | program-supplied display |
| 20 | TYP2 | TYP2O | display | ASKIP/NORM | 9 | 40 | 2 | TURQUOISE | — | — | — | program-supplied display |
| 21 | TRN3 | TRN3O | display | ASKIP/NORM | 10 | 3 | 16 | TURQUOISE | — | — | — | program-supplied display |
| 22 | AMT3 | AMT3O | display | ASKIP/NORM | 10 | 22 | 15 | TURQUOISE | — | — | — | program-supplied display |
| 23 | TYP3 | TYP3O | display | ASKIP/NORM | 10 | 40 | 2 | TURQUOISE | — | — | — | program-supplied display |
| 24 | TRN4 | TRN4O | display | ASKIP/NORM | 11 | 3 | 16 | TURQUOISE | — | — | — | program-supplied display |
| 25 | AMT4 | AMT4O | display | ASKIP/NORM | 11 | 22 | 15 | TURQUOISE | — | — | — | program-supplied display |
| 26 | TYP4 | TYP4O | display | ASKIP/NORM | 11 | 40 | 2 | TURQUOISE | — | — | — | program-supplied display |
| 27 | TRN5 | TRN5O | display | ASKIP/NORM | 12 | 3 | 16 | TURQUOISE | — | — | — | program-supplied display |
| 28 | AMT5 | AMT5O | display | ASKIP/NORM | 12 | 22 | 15 | TURQUOISE | — | — | — | program-supplied display |
| 29 | TYP5 | TYP5O | display | ASKIP/NORM | 12 | 40 | 2 | TURQUOISE | — | — | — | program-supplied display |
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
| 6 | CARDNUM | ○ | □ |
| 7 | TRN1 | □ | □ |
| 8 | AMT1 | □ | □ |
| 9 | TYP1 | □ | □ |
| 10 | TRN2 | □ | □ |
| 11 | AMT2 | □ | □ |
| 12 | TYP2 | □ | □ |
| 13 | TRN3 | □ | □ |
| 14 | AMT3 | □ | □ |
| 15 | TYP3 | □ | □ |
| 16 | TRN4 | □ | □ |
| 17 | AMT4 | □ | □ |
| 18 | TYP4 | □ | □ |
| 19 | TRN5 | □ | □ |
| 20 | AMT5 | □ | □ |
| 21 | TYP5 | □ | □ |
| 22 | ERRMSG | □ | □ |

## 3. Check specifications

### 3.1 MTRANLA — Transaction List (DB2)

All messages below are the literal strings the program sends to the on-screen message line, extracted from the source. Type: E=Error / W=Warning / C=Confirm / I=Information.

| No. | Timing | Condition | Type | Display | Message content (verbatim) | Notes |
|---|---|---|---|---|---|---|
| 1 | ENTER / validation | processing state | I | A (message line) | Enter card number and press ENTER. | on-screen ERRMSG line |
| 2 | ENTER / validation | processing state | I | A (message line) | Transactions listed. | on-screen ERRMSG line |
| 3 | ENTER / validation | processing state | I | A (message line) | No transactions for this card. | on-screen ERRMSG line |
| 4 | ENTER / validation | processing state | E | A (message line) | Error opening transaction cursor. | on-screen ERRMSG line |
| 5 | ENTER / validation | processing state | E | A (message line) | Error fetching transactions. | on-screen ERRMSG line |
| 6 | ENTER / validation | an unsupported key was pressed | E | A (message line) | Invalid key pressed. Please try again. | on-screen ERRMSG line |
| 7 | ENTER / validation | a required field was left blank | E | A (message line) | Please enter all required fields. | on-screen ERRMSG line |

## 4. Event specifications

### 4.1 MTRANLA — Transaction List (DB2)

Per-key and per-field behaviour, written as operator steps.

| No. | Item | Timing / condition | Action | Next screen |
|---|---|---|---|---|
| **Function key section** | | | | |
| 1 | `ENTER` | when pressed | the starting key is accepted and the first page of transaction rows is listed | stays on the screen, or continues to the main menu (OCMENU) |
| 2 | `PF3` | when pressed | cancel and return to the main menu (OCMENU) | the main menu (OCMENU) |
| 3 | `PF4` | when pressed | clear the entered values so the operator can start again | stays on the screen |
| **Data entry section** | | | | |
| 4 | Key field(s) | on ENTER | the starting key is accepted and the first page of transaction rows is listed | stays on `MTRANLA` (or shows a message) |

## 5. DB CRUD

**Update spec**

Not applicable — the Transaction List (DB2) screen only reads data; no table row is created, updated or deleted.

**Column-level CRUD (per event / business type)**

| Screen / table | Physical name | Field group | On ENTER — read |
|---|---|---|---|
| Transaction (relational) | ORION.TRAN | whole record | R |

