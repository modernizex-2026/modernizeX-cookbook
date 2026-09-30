# Screen Design Document — OCCARDA_CardAdd

## 0. Cover

| Item | Content |
|---|---|
| System | ORION-CCMS (ORION Credit Card Management System) |
| Subsystem | On-line (CICS/BMS) |
| Function name | Card Add |
| Function ID | OCCARDA |
| CICS transaction | OROD |
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
    OP["Operator"] --> OCCARDA["OCCARDA<br>Card Add"]
    OCCARDA -- "R" --> T_WS_ACCTFILE[("Account master<br>WS-ACCTFILE")]
    OCCARDA -- "CR" --> T_WS_CARDFILE[("Card master<br>WS-CARDFILE")]
    OCCARDA -- "CR" --> T_WS_XREFFILE[("Card cross-reference<br>WS-XREFFILE")]
    OCCARDA -- "EXEC CICS LINK" --> S_OUDATE["OUDATE<br>date utility subroutine"]
    OCCARDA -- "PF3 returns" --> X_OCMENU["OCMENU<br>Main Menu"]
```

### 1.2 Function overview

1. Accept the details for a new card.
2. Validate the entered values against the business rules.
3. Create the new card record on confirmation.

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
| 2 | Card master | WS-CARDFILE | 〇 | 〇 | - | - | VSAM KSDS (EXEC CICS file control) |
| 3 | Card cross-reference | WS-XREFFILE | 〇 | 〇 | - | - | VSAM KSDS (EXEC CICS file control) |

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
    ENTRY["Calling menu / transaction"] -- "OROD / selection" --> S["MCARDAA<br>Card Add"]
    S -- "PF3 (return)" --> X_OCMENU["MMENUA<br>Main Menu"]
    S -- "EXEC CICS LINK" --> L_OUDATE["OUDATE<br>date utility subroutine"]
    S -- "invalid key / error" --> MSG["Message line (ERRMSG) on MCARDAA"]
```

### 2.1 MCARDAA — Card Add (OCCARDA)

**Layout**

![AS-IS mockup of MCARDAA](../Image/MCARDA_MCARDAA.png)

*This screen appears when the operator selects the add/open function.* Physical size 24×80 (mapset `MCARDA`, map `MCARDAA`).

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
| 10 | (literal) | — | caption | ASKIP/NORM | 6 | 5 | 10 | BLUE | — | — | Card Num : | field header |
| 11 | CARDNUM | CARDNUMO | input | UNPROT/IC/FSET | 6 | 17 | 16 | GREEN | — | — | — | operator input |
| 12 | (literal) | — | caption | ASKIP/NORM | 7 | 5 | 10 | BLUE | — | — | Acct ID  : | field header |
| 13 | CDACCT | CDACCTO | input | UNPROT/IC/FSET | 7 | 17 | 11 | GREEN | — | — | — | operator input |
| 14 | (literal) | — | caption | ASKIP/NORM | 8 | 5 | 10 | BLUE | — | — | Name     : | field header |
| 15 | CDNAME | CDNAMEO | input | UNPROT/IC/FSET | 8 | 17 | 50 | GREEN | — | — | — | operator input |
| 16 | (literal) | — | caption | ASKIP/NORM | 9 | 5 | 10 | BLUE | — | — | CVV      : | field header |
| 17 | CDCVV | CDCVVO | input | UNPROT/IC/FSET | 9 | 17 | 3 | GREEN | — | — | — | operator input |
| 18 | (literal) | — | caption | ASKIP/NORM | 10 | 5 | 10 | BLUE | — | — | Expiry   : | field header |
| 19 | CDEXP | CDEXPO | input | UNPROT/IC/FSET | 10 | 17 | 10 | GREEN | — | — | — | operator input |
| 20 | ERRMSG | ERRMSGO | display | ASKIP/NORM | 23 | 1 | 78 | RED | — | — | — | program-supplied display |
| 21 | (literal) | — | caption | ASKIP/NORM | 24 | 1 | 34 | TURQUOISE | — | — | ENTER=Process  PF3=Back  PF4=Clear | field header |

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
| 7 | CDACCT | ○ | ○ | □ |
| 8 | CDNAME | ○ | ○ | □ |
| 9 | CDCVV | ○ | ○ | □ |
| 10 | CDEXP | ○ | ○ | □ |
| 11 | ERRMSG | □ | □ | □ |

## 3. Check specifications

### 3.1 MCARDAA — Card Add

All messages below are the literal strings the program sends to the on-screen message line, extracted from the source. Type: E=Error / W=Warning / C=Confirm / I=Information.

| No. | Timing | Condition | Type | Display | Message content (verbatim) | Notes |
|---|---|---|---|---|---|---|
| 1 | ENTER / validation | an unsupported key was pressed | E | A (message line) | Invalid key pressed. Please try again. | on-screen ERRMSG line |

## 4. Event specifications

### 4.1 MCARDAA — Card Add

Per-key and per-field behaviour, written as operator steps.

| No. | Item | Timing / condition | Action | Next screen |
|---|---|---|---|---|
| **Function key section** | | | | |
| 1 | `ENTER` | when pressed | the entered values are validated and, once confirmed, a new card record is created | stays on the screen, or continues to the main menu (OCMENU) |
| 2 | `PF3` | when pressed | cancel and return to the main menu (OCMENU) | the main menu (OCMENU) |
| 3 | `PF4` | when pressed | clear the entered values so the operator can start again | stays on the screen |
| **Data entry section** | | | | |
| 4 | Key field(s) | on ENTER | the entered values are validated and, once confirmed, a new card record is created | stays on `MCARDAA` (or shows a message) |

## 5. DB CRUD

**Update spec**

### WS-CARDFILE — Card master

| No. | PK | Item name | Field | On confirm (ENTER) — create |
|---|---|---|---|---|
| 1 | ✓ | Num | CD-NUM | screen input |
| 2 |  | Acct Id | CD-ACCT-ID | screen input |
| 3 |  | Cvv | CD-CVV | screen input |
| 4 |  | Embossed Name | CD-EMBOSSED-NAME | screen input |
| 5 |  | Expiry Date | CD-EXPIRY-DATE | screen input |
| 6 |  | Active Status | CD-ACTIVE-STATUS | screen input |
| 7 |  | Filler | FILLER | screen input |

### WS-XREFFILE — Card cross-reference

| No. | PK | Item name | Field | On confirm (ENTER) — create |
|---|---|---|---|---|
| 1 | ✓ | Card Num | XR-CARD-NUM | screen input |
| 2 |  | Acct Id | XR-ACCT-ID | screen input |
| 3 |  | Cust Id | XR-CUST-ID | screen input |
| 4 |  | Filler | FILLER | screen input |

**Column-level CRUD (per event / business type)**

| Screen / table | Physical name | Field group | On confirm (ENTER) — create |
|---|---|---|---|
| Account master | WS-ACCTFILE | whole record | R |
| Card master | WS-CARDFILE | whole record | C/R |
| Card cross-reference | WS-XREFFILE | whole record | C/R |

