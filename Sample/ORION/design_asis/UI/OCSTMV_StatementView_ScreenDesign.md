# Screen Design Document — OCSTMV_StatementView

## 0. Cover

| Item | Content |
|---|---|
| System | ORION-CCMS (ORION Credit Card Management System) |
| Subsystem | On-line (CICS/BMS) |
| Function name | Statement View |
| Function ID | OCSTMV |
| CICS transaction | ORSV |
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
    OP["Operator"] --> OCSTMV["OCSTMV<br>Statement View"]
    OCSTMV -- "R" --> T_WS_STMTFILE[("Statement file<br>WS-STMTFILE")]
    OCSTMV -- "PF3 returns" --> X_OCMENU["OCMENU<br>Main Menu"]
```

### 1.2 Function overview

1. Look up the statement by its key.
2. Display the current details of the statement in read-only form.
3. Return to the calling screen when the operator is finished.

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
| 1 | Statement file | WS-STMTFILE | - | 〇 | - | - | VSAM KSDS (EXEC CICS file control) |

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
    ENTRY["Calling menu / transaction"] -- "ORSV / selection" --> S["MSTMVA<br>Statement View"]
    S -- "PF3 (return)" --> X_OCMENU["MMENUA<br>Main Menu"]
    S -- "invalid key / error" --> MSG["Message line (ERRMSG) on MSTMVA"]
```

### 2.1 MSTMVA — Statement View (OCSTMV)

**Layout**

![AS-IS mockup of MSTMVA](../Image/MSTMV_MSTMVA.png)

*This screen appears when the operator selects the view function and enters a key.* Physical size 24×80 (mapset `MSTMV`, map `MSTMVA`).

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
| 10 | (literal) | — | caption | ASKIP/NORM | 6 | 5 | 9 | BLUE | — | — | Acct ID : | field header |
| 11 | ACCTID | ACCTIDO | input | UNPROT/IC/FSET | 6 | 16 | 11 | GREEN | — | — | — | operator input |
| 12 | (literal) | — | caption | ASKIP/NORM | 7 | 5 | 9 | BLUE | — | — | Cycle   : | field header |
| 13 | STCYC | STCYCO | input | UNPROT/IC/FSET | 7 | 16 | 6 | GREEN | — | — | — | operator input |
| 14 | (literal) | — | caption | ASKIP/NORM | 9 | 5 | 9 | BLUE | — | — | Open Bal: | field header |
| 15 | STOPEN | STOPENO | display | ASKIP/NORM | 9 | 16 | 15 | TURQUOISE | — | — | — | program-supplied display |
| 16 | (literal) | — | caption | ASKIP/NORM | 10 | 5 | 9 | BLUE | — | — | Close   : | field header |
| 17 | STCLOSE | STCLOSEO | display | ASKIP/NORM | 10 | 16 | 15 | TURQUOISE | — | — | — | program-supplied display |
| 18 | (literal) | — | caption | ASKIP/NORM | 11 | 5 | 9 | BLUE | — | — | Min Due : | field header |
| 19 | STMIN | STMINO | display | ASKIP/NORM | 11 | 16 | 15 | TURQUOISE | — | — | — | program-supplied display |
| 20 | (literal) | — | caption | ASKIP/NORM | 12 | 5 | 9 | BLUE | — | — | Due Date: | field header |
| 21 | STDUE | STDUEO | display | ASKIP/NORM | 12 | 16 | 10 | TURQUOISE | — | — | — | program-supplied display |
| 22 | ERRMSG | ERRMSGO | display | ASKIP/NORM | 23 | 1 | 78 | RED | — | — | — | program-supplied display |
| 23 | (literal) | — | caption | ASKIP/NORM | 24 | 1 | 34 | TURQUOISE | — | — | ENTER=Process  PF3=Back  PF4=Clear | field header |

**Item states**

> Legend: `○`=enabled ｜ `必`=enabled (required) ｜ `□`=display only ｜ `×`=hidden ｜ `レ`=initial focus

| № | Field (BMS) | Initial display (key prompt) | Record displayed |
|---|---|---|---|
| 1 | TRNNAME | □ | □ |
| 2 | TITLE | □ | □ |
| 3 | CURDATE | □ | □ |
| 4 | PGMNAME | □ | □ |
| 5 | CURTIME | □ | □ |
| 6 | ACCTID | ○ | □ |
| 7 | STCYC | ○ | □ |
| 8 | STOPEN | □ | □ |
| 9 | STCLOSE | □ | □ |
| 10 | STMIN | □ | □ |
| 11 | STDUE | □ | □ |
| 12 | ERRMSG | □ | □ |

## 3. Check specifications

### 3.1 MSTMVA — Statement View

All messages below are the literal strings the program sends to the on-screen message line, extracted from the source. Type: E=Error / W=Warning / C=Confirm / I=Information.

| No. | Timing | Condition | Type | Display | Message content (verbatim) | Notes |
|---|---|---|---|---|---|---|
| 1 | ENTER / validation | an unsupported key was pressed | E | A (message line) | Invalid key pressed. Please try again. | on-screen ERRMSG line |

## 4. Event specifications

### 4.1 MSTMVA — Statement View

Per-key and per-field behaviour, written as operator steps.

| No. | Item | Timing / condition | Action | Next screen |
|---|---|---|---|---|
| **Function key section** | | | | |
| 1 | `ENTER` | when pressed | the keyed statement is read and its details are displayed | stays on the screen, or continues to the main menu (OCMENU) |
| 2 | `PF3` | when pressed | cancel and return to the main menu (OCMENU) | the main menu (OCMENU) |
| 3 | `PF4` | when pressed | clear the entered values so the operator can start again | stays on the screen |
| **Data entry section** | | | | |
| 4 | Key field(s) | on ENTER | the keyed statement is read and its details are displayed | stays on `MSTMVA` (or shows a message) |

## 5. DB CRUD

**Update spec**

Not applicable — the Statement View screen only reads data; no table row is created, updated or deleted.

**Column-level CRUD (per event / business type)**

| Screen / table | Physical name | Field group | On ENTER — read |
|---|---|---|---|
| Statement file | WS-STMTFILE | whole record | R |

