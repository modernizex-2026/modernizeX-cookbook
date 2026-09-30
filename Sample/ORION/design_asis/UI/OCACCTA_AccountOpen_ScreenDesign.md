# Screen Design Document — OCACCTA_AccountOpen

## 0. Cover

| Item | Content |
|---|---|
| System | ORION-CCMS (ORION Credit Card Management System) |
| Subsystem | On-line (CICS/BMS) |
| Function name | Account Open |
| Function ID | OCACCTA |
| CICS transaction | OROA |
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
    OP["Operator"] --> OCACCTA["OCACCTA<br>Account Open"]
    OCACCTA -- "R" --> T_WS_CUSTFILE[("Customer master<br>WS-CUSTFILE")]
    OCACCTA -- "CR" --> T_WS_ACCTFILE[("Account master<br>WS-ACCTFILE")]
    OCACCTA -- "C" --> T_WS_XREFFILE[("Card cross-reference<br>WS-XREFFILE")]
    OCACCTA -- "EXEC CICS LINK" --> S_OUDATE["OUDATE<br>date utility subroutine"]
    OCACCTA -- "PF3 returns" --> X_OCMENU["OCMENU<br>Main Menu"]
```

### 1.2 Function overview

1. Accept the details for a new account.
2. Validate the entered values against the business rules.
3. Create the new account record on confirmation.

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
| 2 | Account master | WS-ACCTFILE | 〇 | 〇 | - | - | VSAM KSDS (EXEC CICS file control) |
| 3 | Card cross-reference | WS-XREFFILE | 〇 | - | - | - | VSAM KSDS (EXEC CICS file control) |

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
    ENTRY["Calling menu / transaction"] -- "OROA / selection" --> S["MACCTAA<br>Account Open"]
    S -- "PF3 (return)" --> X_OCMENU["MMENUA<br>Main Menu"]
    S -- "EXEC CICS LINK" --> L_OUDATE["OUDATE<br>date utility subroutine"]
    S -- "invalid key / error" --> MSG["Message line (ERRMSG) on MACCTAA"]
```

### 2.1 MACCTAA — Account Open (OCACCTA)

**Layout**

![AS-IS mockup of MACCTAA](../Image/MACCTA_MACCTAA.png)

*This screen appears when the operator selects the add/open function.* Physical size 24×80 (mapset `MACCTA`, map `MACCTAA`).

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
| 10 | (literal) | — | caption | ASKIP/NORM | 6 | 5 | 11 | BLUE | — | — | Account ID: | field header |
| 11 | ACCTID | ACCTIDO | input | UNPROT/IC/FSET | 6 | 18 | 11 | GREEN | — | — | — | operator input |
| 12 | (literal) | — | caption | ASKIP/NORM | 7 | 5 | 11 | BLUE | — | — | Cust ID   : | field header |
| 13 | CUSTID | CUSTIDO | input | UNPROT/IC/FSET | 7 | 18 | 9 | GREEN | — | — | — | operator input |
| 14 | (literal) | — | caption | ASKIP/NORM | 8 | 5 | 11 | BLUE | — | — | Credit Lim: | field header |
| 15 | ACCRLIM | ACCRLIMO | input | UNPROT/IC/FSET | 8 | 18 | 13 | GREEN | — | — | — | operator input |
| 16 | (literal) | — | caption | ASKIP/NORM | 9 | 5 | 11 | BLUE | — | — | Cash Lim  : | field header |
| 17 | ACCSLIM | ACCSLIMO | input | UNPROT/IC/FSET | 9 | 18 | 13 | GREEN | — | — | — | operator input |
| 18 | (literal) | — | caption | ASKIP/NORM | 10 | 5 | 11 | BLUE | — | — | Open Date : | field header |
| 19 | ACOPEN | ACOPENO | input | UNPROT/IC/FSET | 10 | 18 | 10 | GREEN | — | — | — | operator input |
| 20 | (literal) | — | caption | ASKIP/NORM | 11 | 5 | 11 | BLUE | — | — | Group ID  : | field header |
| 21 | ACGRP | ACGRPO | input | UNPROT/IC/FSET | 11 | 18 | 10 | GREEN | — | — | — | operator input |
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
| 6 | ACCTID | ○ | ○ | □ |
| 7 | CUSTID | ○ | ○ | □ |
| 8 | ACCRLIM | ○ | ○ | □ |
| 9 | ACCSLIM | ○ | ○ | □ |
| 10 | ACOPEN | ○ | ○ | □ |
| 11 | ACGRP | ○ | ○ | □ |
| 12 | ERRMSG | □ | □ | □ |

## 3. Check specifications

### 3.1 MACCTAA — Account Open

All messages below are the literal strings the program sends to the on-screen message line, extracted from the source. Type: E=Error / W=Warning / C=Confirm / I=Information.

| No. | Timing | Condition | Type | Display | Message content (verbatim) | Notes |
|---|---|---|---|---|---|---|
| 1 | ENTER / validation | an unsupported key was pressed | E | A (message line) | Invalid key pressed. Please try again. | on-screen ERRMSG line |

## 4. Event specifications

### 4.1 MACCTAA — Account Open

Per-key and per-field behaviour, written as operator steps.

| No. | Item | Timing / condition | Action | Next screen |
|---|---|---|---|---|
| **Function key section** | | | | |
| 1 | `ENTER` | when pressed | the entered values are validated and, once confirmed, a new account record is created | stays on the screen, or continues to the main menu (OCMENU) |
| 2 | `PF3` | when pressed | cancel and return to the main menu (OCMENU) | the main menu (OCMENU) |
| 3 | `PF4` | when pressed | clear the entered values so the operator can start again | stays on the screen |
| **Data entry section** | | | | |
| 4 | Key field(s) | on ENTER | the entered values are validated and, once confirmed, a new account record is created | stays on `MACCTAA` (or shows a message) |

## 5. DB CRUD

**Update spec**

### WS-ACCTFILE — Account master

| No. | PK | Item name | Field | On confirm (ENTER) — create |
|---|---|---|---|---|
| 1 | ✓ | Id | AC-ID | screen input |
| 2 |  | Active Status | AC-ACTIVE-STATUS | screen input |
| 3 |  | Curr Bal | AC-CURR-BAL | screen input |
| 4 |  | Credit Limit | AC-CREDIT-LIMIT | screen input |
| 5 |  | Cash Limit | AC-CASH-LIMIT | screen input |
| 6 |  | Open Date | AC-OPEN-DATE | screen input |
| 7 |  | Expiry Date | AC-EXPIRY-DATE | screen input |
| 8 |  | Reissue Date | AC-REISSUE-DATE | screen input |
| 9 |  | Cyc Credit | AC-CYC-CREDIT | screen input |
| 10 |  | Cyc Debit | AC-CYC-DEBIT | screen input |
| 11 |  | Addr Zip | AC-ADDR-ZIP | screen input |
| 12 |  | Group Id | AC-GROUP-ID | screen input |
| 13 |  | Filler | FILLER | screen input |

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
| Customer master | WS-CUSTFILE | whole record | R |
| Account master | WS-ACCTFILE | whole record | C/R |
| Card cross-reference | WS-XREFFILE | whole record | C |

