# Screen Design Document — OCCUSTA_CustomerAdd

## 0. Cover

| Item | Content |
|---|---|
| System | ORION-CCMS (ORION Credit Card Management System) |
| Subsystem | On-line (CICS/BMS) |
| Function name | Customer Add |
| Function ID | OCCUSTA |
| CICS transaction | OROC |
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
    OP["Operator"] --> OCCUSTA["OCCUSTA<br>Customer Add"]
    OCCUSTA -- "C" --> T_WS_CUSTFILE[("Customer master<br>WS-CUSTFILE")]
    OCCUSTA -- "PF3 returns" --> X_OCMENU["OCMENU<br>Main Menu"]
```

### 1.2 Function overview

1. Accept the details for a new customer.
2. Validate the entered values against the business rules.
3. Create the new customer record on confirmation.

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
| 1 | Customer master | WS-CUSTFILE | 〇 | - | - | - | VSAM KSDS (EXEC CICS file control) |

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
    ENTRY["Calling menu / transaction"] -- "OROC / selection" --> S["MCUSTAA<br>Customer Add"]
    S -- "PF3 (return)" --> X_OCMENU["MMENUA<br>Main Menu"]
    S -- "invalid key / error" --> MSG["Message line (ERRMSG) on MCUSTAA"]
```

### 2.1 MCUSTAA — Customer Add (OCCUSTA)

**Layout**

![AS-IS mockup of MCUSTAA](../Image/MCUSTA_MCUSTAA.png)

*This screen appears when the operator selects the add/open function.* Physical size 24×80 (mapset `MCUSTA`, map `MCUSTAA`).

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
| 10 | (literal) | — | caption | ASKIP/NORM | 6 | 5 | 9 | BLUE | — | — | Cust ID : | field header |
| 11 | CUSTID | CUSTIDO | input | UNPROT/IC/FSET | 6 | 16 | 9 | GREEN | — | — | — | operator input |
| 12 | (literal) | — | caption | ASKIP/NORM | 7 | 5 | 9 | BLUE | — | — | First   : | field header |
| 13 | CUFNAM | CUFNAMO | input | UNPROT/IC/FSET | 7 | 16 | 25 | GREEN | — | — | — | operator input |
| 14 | (literal) | — | caption | ASKIP/NORM | 8 | 5 | 9 | BLUE | — | — | Last    : | field header |
| 15 | CULNAM | CULNAMO | input | UNPROT/IC/FSET | 8 | 16 | 25 | GREEN | — | — | — | operator input |
| 16 | (literal) | — | caption | ASKIP/NORM | 9 | 5 | 9 | BLUE | — | — | Address : | field header |
| 17 | CUADDR | CUADDRO | input | UNPROT/IC/FSET | 9 | 16 | 50 | GREEN | — | — | — | operator input |
| 18 | (literal) | — | caption | ASKIP/NORM | 10 | 5 | 9 | BLUE | — | — | City    : | field header |
| 19 | CUCITY | CUCITYO | input | UNPROT/IC/FSET | 10 | 16 | 50 | GREEN | — | — | — | operator input |
| 20 | (literal) | — | caption | ASKIP/NORM | 11 | 5 | 9 | BLUE | — | — | SSN     : | field header |
| 21 | CUSSN | CUSSNO | input | UNPROT/IC/FSET | 11 | 16 | 9 | GREEN | — | — | — | operator input |
| 22 | (literal) | — | caption | ASKIP/NORM | 12 | 5 | 9 | BLUE | — | — | FICO    : | field header |
| 23 | CUFICO | CUFICOO | input | UNPROT/IC/FSET | 12 | 16 | 3 | GREEN | — | — | — | operator input |
| 24 | ERRMSG | ERRMSGO | display | ASKIP/NORM | 23 | 1 | 78 | RED | — | — | — | program-supplied display |
| 25 | (literal) | — | caption | ASKIP/NORM | 24 | 1 | 34 | TURQUOISE | — | — | ENTER=Process  PF3=Back  PF4=Clear | field header |

**Item states**

> Legend: `○`=enabled ｜ `必`=enabled (required) ｜ `□`=display only ｜ `×`=hidden ｜ `レ`=initial focus

| № | Field (BMS) | Initial display (entry) | Validated | Confirm create |
|---|---|---|---|---|
| 1 | TRNNAME | □ | □ | □ |
| 2 | TITLE | □ | □ | □ |
| 3 | CURDATE | □ | □ | □ |
| 4 | PGMNAME | □ | □ | □ |
| 5 | CURTIME | □ | □ | □ |
| 6 | CUSTID | ○ | ○ | □ |
| 7 | CUFNAM | ○ | ○ | □ |
| 8 | CULNAM | ○ | ○ | □ |
| 9 | CUADDR | ○ | ○ | □ |
| 10 | CUCITY | ○ | ○ | □ |
| 11 | CUSSN | ○ | ○ | □ |
| 12 | CUFICO | ○ | ○ | □ |
| 13 | ERRMSG | □ | □ | □ |

## 3. Check specifications

### 3.1 MCUSTAA — Customer Add

All messages below are the literal strings the program sends to the on-screen message line, extracted from the source. Type: E=Error / W=Warning / C=Confirm / I=Information.

| No. | Timing | Condition | Type | Display | Message content (verbatim) | Notes |
|---|---|---|---|---|---|---|
| 1 | ENTER / validation | an unsupported key was pressed | E | A (message line) | Invalid key pressed. Please try again. | on-screen ERRMSG line |

## 4. Event specifications

### 4.1 MCUSTAA — Customer Add

Per-key and per-field behaviour, written as operator steps.

| No. | Item | Timing / condition | Action | Next screen |
|---|---|---|---|---|
| **Function key section** | | | | |
| 1 | `ENTER` | when pressed | the entered values are validated and, once confirmed, a new customer record is created | stays on the screen, or continues to the main menu (OCMENU) |
| 2 | `PF3` | when pressed | cancel and return to the main menu (OCMENU) | the main menu (OCMENU) |
| 3 | `PF4` | when pressed | clear the entered values so the operator can start again | stays on the screen |
| **Data entry section** | | | | |
| 4 | Key field(s) | on ENTER | the entered values are validated and, once confirmed, a new customer record is created | stays on `MCUSTAA` (or shows a message) |

## 5. DB CRUD

**Update spec**

### WS-CUSTFILE — Customer master

| No. | PK | Item name | Field | On confirm (ENTER) — create |
|---|---|---|---|---|
| 1 | ✓ | Id | CU-ID | screen input |
| 2 |  | First Name | CU-FIRST-NAME | screen input |
| 3 |  | Middle Name | CU-MIDDLE-NAME | screen input |
| 4 |  | Last Name | CU-LAST-NAME | screen input |
| 5 |  | Addr Line 1 | CU-ADDR-LINE-1 | screen input |
| 6 |  | Addr Line 2 | CU-ADDR-LINE-2 | screen input |
| 7 |  | Addr City | CU-ADDR-CITY | screen input |
| 8 |  | Addr State | CU-ADDR-STATE | screen input |
| 9 |  | Addr Country | CU-ADDR-COUNTRY | screen input |
| 10 |  | Addr Zip | CU-ADDR-ZIP | screen input |
| 11 |  | Phone 1 | CU-PHONE-1 | screen input |
| 12 |  | Phone 2 | CU-PHONE-2 | screen input |
| 13 |  | Ssn | CU-SSN | screen input |
| 14 |  | Govt Id | CU-GOVT-ID | screen input |
| 15 |  | Dob | CU-DOB | screen input |
| 16 |  | Fico Score | CU-FICO-SCORE | screen input |
| 17 |  | Filler | FILLER | screen input |

**Column-level CRUD (per event / business type)**

| Screen / table | Physical name | Field group | On confirm (ENTER) — create |
|---|---|---|---|
| Customer master | WS-CUSTFILE | whole record | C |

