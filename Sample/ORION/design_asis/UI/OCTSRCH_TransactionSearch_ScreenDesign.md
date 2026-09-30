# Screen Design Document — OCTSRCH_TransactionSearch

## 0. Cover

| Item | Content |
|---|---|
| System | ORION-CCMS (ORION Credit Card Management System) |
| Subsystem | On-line (CICS/BMS) |
| Function name | Transaction Search |
| Function ID | OCTSRCH |
| CICS transaction | ORTS |
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
    OP["Operator"] --> OCTSRCH["OCTSRCH<br>Transaction Search"]
    OCTSRCH -- "R" --> T_WS_TRANFILE[("Transaction file<br>WS-TRANFILE")]
    OCTSRCH -- "PF3 returns" --> X_OCMENU["OCMENU<br>Main Menu"]
```

### 1.2 Function overview

1. Accept the search key for the transaction inquiry.
2. Retrieve and display the matching transaction information for review.
3. Let the operator refine the key and inquire again.

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
| 1 | Transaction file | WS-TRANFILE | - | 〇 | - | - | VSAM KSDS (EXEC CICS file control) |

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
    ENTRY["Calling menu / transaction"] -- "ORTS / selection" --> S["MTSRCHA<br>Transaction Search"]
    S -- "PF3 (return)" --> X_OCMENU["MMENUA<br>Main Menu"]
    S -- "invalid key / error" --> MSG["Message line (ERRMSG) on MTSRCHA"]
```

### 2.1 MTSRCHA — Transaction Search (OCTSRCH)

**Layout**

![AS-IS mockup of MTSRCHA](../Image/MTSRCH_MTSRCHA.png)

*This screen appears when the operator opens the inquiry function.* Physical size 24×80 (mapset `MTSRCH`, map `MTSRCHA`).

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
| 12 | (literal) | — | caption | ASKIP/NORM | 6 | 5 | 9 | BLUE | — | — | From Amt: | field header |
| 13 | FRAMT | FRAMTO | input | UNPROT/IC/FSET | 6 | 15 | 12 | GREEN | — | — | — | operator input |
| 14 | (literal) | — | caption | ASKIP/NORM | 6 | 40 | 7 | BLUE | — | — | To Amt: | field header |
| 15 | TOAMT | TOAMTO | input | UNPROT/IC/FSET | 6 | 48 | 12 | GREEN | — | — | — | operator input |
| 16 | SR1 | SR1O | display | ASKIP/NORM | 8 | 3 | 70 | TURQUOISE | — | — | — | program-supplied display |
| 17 | SR2 | SR2O | display | ASKIP/NORM | 9 | 3 | 70 | TURQUOISE | — | — | — | program-supplied display |
| 18 | SR3 | SR3O | display | ASKIP/NORM | 10 | 3 | 70 | TURQUOISE | — | — | — | program-supplied display |
| 19 | SR4 | SR4O | display | ASKIP/NORM | 11 | 3 | 70 | TURQUOISE | — | — | — | program-supplied display |
| 20 | SR5 | SR5O | display | ASKIP/NORM | 12 | 3 | 70 | TURQUOISE | — | — | — | program-supplied display |
| 21 | ERRMSG | ERRMSGO | display | ASKIP/NORM | 23 | 1 | 78 | RED | — | — | — | program-supplied display |
| 22 | (literal) | — | caption | ASKIP/NORM | 24 | 1 | 34 | TURQUOISE | — | — | ENTER=Process  PF3=Back  PF4=Clear | field header |

**Item states**

> Legend: `○`=enabled ｜ `必`=enabled (required) ｜ `□`=display only ｜ `×`=hidden ｜ `レ`=initial focus

| № | Field (BMS) | Initial display (key prompt) | Result displayed |
|---|---|---|---|
| 1 | TRNNAME | □ | □ |
| 2 | TITLE | □ | □ |
| 3 | CURDATE | □ | □ |
| 4 | PGMNAME | □ | □ |
| 5 | CURTIME | □ | □ |
| 6 | CARDNUM | ○ | □ |
| 7 | FRAMT | ○ | □ |
| 8 | TOAMT | ○ | □ |
| 9 | SR1 | □ | □ |
| 10 | SR2 | □ | □ |
| 11 | SR3 | □ | □ |
| 12 | SR4 | □ | □ |
| 13 | SR5 | □ | □ |
| 14 | ERRMSG | □ | □ |

## 3. Check specifications

### 3.1 MTSRCHA — Transaction Search

All messages below are the literal strings the program sends to the on-screen message line, extracted from the source. Type: E=Error / W=Warning / C=Confirm / I=Information.

| No. | Timing | Condition | Type | Display | Message content (verbatim) | Notes |
|---|---|---|---|---|---|---|
| 1 | ENTER / validation | an unsupported key was pressed | E | A (message line) | Invalid key pressed. Please try again. | on-screen ERRMSG line |

## 4. Event specifications

### 4.1 MTSRCHA — Transaction Search

Per-key and per-field behaviour, written as operator steps.

| No. | Item | Timing / condition | Action | Next screen |
|---|---|---|---|---|
| **Function key section** | | | | |
| 1 | `ENTER` | when pressed | the search key is accepted and the matching transaction information is displayed | stays on the screen, or continues to the main menu (OCMENU) |
| 2 | `PF3` | when pressed | cancel and return to the main menu (OCMENU) | the main menu (OCMENU) |
| 3 | `PF4` | when pressed | clear the entered values so the operator can start again | stays on the screen |
| **Data entry section** | | | | |
| 4 | Key field(s) | on ENTER | the search key is accepted and the matching transaction information is displayed | stays on `MTSRCHA` (or shows a message) |

## 5. DB CRUD

**Update spec**

Not applicable — the Transaction Search screen only reads data; no table row is created, updated or deleted.

**Column-level CRUD (per event / business type)**

| Screen / table | Physical name | Field group | On ENTER — read |
|---|---|---|---|
| Transaction file | WS-TRANFILE | whole record | R |

