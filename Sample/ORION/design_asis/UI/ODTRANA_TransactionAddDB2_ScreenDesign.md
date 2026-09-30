# Screen Design Document — ODTRANA_TransactionAddDB2

## 0. Cover

| Item | Content |
|---|---|
| System | ORION-CCMS (ORION Credit Card Management System) |
| Subsystem | On-line (CICS/BMS) — DB2 variant |
| Function name | Transaction Add (DB2) |
| Function ID | ODTRANA |
| CICS transaction | OD06 |
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
    OP["Operator"] --> ODTRANA["ODTRANA<br>Transaction Add (DB2)"]
    ODTRANA -- "R" --> T_ORION_CARD[("Card master (relational)<br>ORION.CARD")]
    ODTRANA -- "R" --> T_ORION_TTYP[("Transaction type (relational)<br>ORION.TTYP")]
    ODTRANA -- "RU" --> T_ORION_CTRL[("Control (relational)<br>ORION.CTRL")]
    ODTRANA -- "C" --> T_ORION_TRAN[("Transaction (relational)<br>ORION.TRAN")]
    ODTRANA -- "PF3 returns" --> X_OCMENU["OCMENU<br>Main Menu"]
```

### 1.2 Function overview

1. Accept the details for a new transaction.
2. Validate the entered values against the business rules.
3. Create the new transaction record on confirmation.

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
| 1 | Card master (relational) | ORION.CARD | - | 〇 | - | - | DB2 relational table (EXEC SQL) |
| 2 | Transaction type (relational) | ORION.TTYP | - | 〇 | - | - | DB2 relational table (EXEC SQL) |
| 3 | Control (relational) | ORION.CTRL | - | 〇 | 〇 | - | DB2 relational table (EXEC SQL) |
| 4 | Transaction (relational) | ORION.TRAN | 〇 | - | - | - | DB2 relational table (EXEC SQL) |

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
    ENTRY["Calling menu / transaction"] -- "OD06 / selection" --> S["MTRANAA<br>Transaction Add (DB2)"]
    S -- "PF3 (return)" --> X_OCMENU["MMENUA<br>Main Menu"]
    S -- "invalid key / error" --> MSG["Message line (ERRMSG) on MTRANAA"]
```

### 2.1 MTRANAA — Transaction Add (DB2) (ODTRANA)

**Layout**

![AS-IS mockup of MTRANAA](../Image/MTRANA_MTRANAA.png)

*This screen appears when the operator selects the add/open function.* Physical size 24×80 (mapset `MTRANA`, map `MTRANAA`).

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
| 10 | (literal) | — | caption | ASKIP/NORM | 6 | 5 | 9 | BLUE | — | — | Card Num: | field header |
| 11 | CARDNUM | CARDNUMO | input | UNPROT/IC/FSET | 6 | 16 | 16 | GREEN | — | — | — | operator input |
| 12 | (literal) | — | caption | ASKIP/NORM | 7 | 5 | 9 | BLUE | — | — | Type    : | field header |
| 13 | TRTYPE | TRTYPEO | input | UNPROT/IC/FSET | 7 | 16 | 2 | GREEN | — | — | — | operator input |
| 14 | (literal) | — | caption | ASKIP/NORM | 8 | 5 | 9 | BLUE | — | — | Category: | field header |
| 15 | TRCAT | TRCATO | input | UNPROT/IC/FSET | 8 | 16 | 4 | GREEN | — | — | — | operator input |
| 16 | (literal) | — | caption | ASKIP/NORM | 9 | 5 | 9 | BLUE | — | — | Amount  : | field header |
| 17 | TRAMT | TRAMTO | input | UNPROT/IC/FSET | 9 | 16 | 12 | GREEN | — | — | — | operator input |
| 18 | (literal) | — | caption | ASKIP/NORM | 10 | 5 | 9 | BLUE | — | — | Merchant: | field header |
| 19 | TRMERCH | TRMERCHO | input | UNPROT/IC/FSET | 10 | 16 | 50 | GREEN | — | — | — | operator input |
| 20 | (literal) | — | caption | ASKIP/NORM | 11 | 5 | 9 | BLUE | — | — | Desc    : | field header |
| 21 | TRDESC | TRDESCO | input | UNPROT/IC/FSET | 11 | 16 | 50 | GREEN | — | — | — | operator input |
| 22 | ERRMSG | ERRMSGO | display | ASKIP/NORM | 23 | 1 | 78 | RED | — | — | — | program-supplied display |
| 23 | (literal) | — | caption | ASKIP/NORM | 24 | 1 | 34 | TURQUOISE | — | — | ENTER=Process  PF3=Back  PF4=Clear | field header |

**Item states**

> Legend: `○`=enabled ｜ `必`=enabled (required) ｜ `□`=display only ｜ `×`=hidden ｜ `レ`=initial focus

| № | Field (BMS) | Initial display (entry) | Validated | Confirm create |
|---|---|---|---|---|
| 1 | TRNNAME | □ | □ | □ |
| 2 | TITLE | □ | □ | □ |
| 3 | CURDATE | □ | □ | □ |
| 4 | PGMNAME | □ | □ | □ |
| 5 | CURTIME | □ | □ | □ |
| 6 | CARDNUM | ○ | ○ | □ |
| 7 | TRTYPE | ○ | ○ | □ |
| 8 | TRCAT | ○ | ○ | □ |
| 9 | TRAMT | ○ | ○ | □ |
| 10 | TRMERCH | ○ | ○ | □ |
| 11 | TRDESC | ○ | ○ | □ |
| 12 | ERRMSG | □ | □ | □ |

## 3. Check specifications

### 3.1 MTRANAA — Transaction Add (DB2)

All messages below are the literal strings the program sends to the on-screen message line, extracted from the source. Type: E=Error / W=Warning / C=Confirm / I=Information.

| No. | Timing | Condition | Type | Display | Message content (verbatim) | Notes |
|---|---|---|---|---|---|---|
| 1 | ENTER / validation | processing state | I | A (message line) | Enter transaction detail and press ENTER. | on-screen ERRMSG line |
| 2 | ENTER / validation | the keyed record does not exist | E | A (message line) | Transaction type not found. | on-screen ERRMSG line |
| 3 | ENTER / validation | the keyed record does not exist | E | A (message line) | Card number not found. | on-screen ERRMSG line |
| 4 | ENTER / validation | a required field was left blank | E | A (message line) | Card number is required. | on-screen ERRMSG line |
| 5 | ENTER / validation | a required field was left blank | E | A (message line) | Transaction type is required. | on-screen ERRMSG line |
| 6 | ENTER / validation | a numeric field held non-numeric data | E | A (message line) | Category must be numeric. | on-screen ERRMSG line |
| 7 | ENTER / validation | a required field was left blank | E | A (message line) | Amount is required. | on-screen ERRMSG line |
| 8 | ENTER / validation | processing state | E | A (message line) | Error reading card table. | on-screen ERRMSG line |
| 9 | ENTER / validation | processing state | E | A (message line) | Error reading type table. | on-screen ERRMSG line |
| 10 | ENTER / validation | processing state | E | A (message line) | Error inserting transaction row. | on-screen ERRMSG line |
| 11 | ENTER / validation | an unsupported key was pressed | E | A (message line) | Invalid key pressed. Please try again. | on-screen ERRMSG line |

## 4. Event specifications

### 4.1 MTRANAA — Transaction Add (DB2)

Per-key and per-field behaviour, written as operator steps.

| No. | Item | Timing / condition | Action | Next screen |
|---|---|---|---|---|
| **Function key section** | | | | |
| 1 | `ENTER` | when pressed | the entered values are validated and, once confirmed, a new transaction record is created | stays on the screen, or continues to the main menu (OCMENU) |
| 2 | `PF3` | when pressed | cancel and return to the main menu (OCMENU) | the main menu (OCMENU) |
| 3 | `PF4` | when pressed | clear the entered values so the operator can start again | stays on the screen |
| **Data entry section** | | | | |
| 4 | Key field(s) | on ENTER | the entered values are validated and, once confirmed, a new transaction record is created | stays on `MTRANAA` (or shows a message) |

## 5. DB CRUD

**Update spec**

### ORION.CTRL — Control (relational)

| No. | PK | Item name | Field | On confirm (ENTER) — create |
|---|---|---|---|---|
| 1 | ✓ | key field | (see record layout) | set from the screen / program |

### ORION.TRAN — Transaction (relational)

| No. | PK | Item name | Field | On confirm (ENTER) — create |
|---|---|---|---|---|
| 1 | ✓ | key field | (see record layout) | set from the screen / program |

**Column-level CRUD (per event / business type)**

| Screen / table | Physical name | Field group | On confirm (ENTER) — create |
|---|---|---|---|
| Card master (relational) | ORION.CARD | whole record | R |
| Transaction type (relational) | ORION.TTYP | whole record | R |
| Control (relational) | ORION.CTRL | whole record | R/U |
| Transaction (relational) | ORION.TRAN | whole record | C |

