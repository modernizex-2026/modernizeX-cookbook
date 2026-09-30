# Screen Design Document — OCBILLP_BillPayment

## 0. Cover

| Item | Content |
|---|---|
| System | ORION-CCMS (ORION Credit Card Management System) |
| Subsystem | On-line (CICS/BMS) |
| Function name | Bill Payment |
| Function ID | OCBILLP |
| CICS transaction | ORBP |
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
    OP["Operator"] --> OCBILLP["OCBILLP<br>Bill Payment"]
    OCBILLP -- "RU" --> T_WS_ACCTFILE[("Account master<br>WS-ACCTFILE")]
    OCBILLP -- "RU" --> T_WS_CTRLFILE[("Control file<br>WS-CTRLFILE")]
    OCBILLP -- "C" --> T_WS_BILLFILE[("Bill file<br>WS-BILLFILE")]
    OCBILLP -- "PF3 returns" --> X_OCMENU["OCMENU<br>Main Menu"]
```

### 1.2 Function overview

1. Look up the account balance for a bill payment.
2. Accept the payment amount and validate it.
3. Post the payment and record the resulting transaction on confirmation.

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
| 1 | Account master | WS-ACCTFILE | - | 〇 | 〇 | - | VSAM KSDS (EXEC CICS file control) |
| 2 | Control file | WS-CTRLFILE | - | 〇 | 〇 | - | VSAM KSDS (EXEC CICS file control) |
| 3 | Bill file | WS-BILLFILE | 〇 | - | - | - | VSAM KSDS (EXEC CICS file control) |

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
    ENTRY["Calling menu / transaction"] -- "ORBP / selection" --> S["MBILLPA<br>Bill Payment"]
    S -- "PF3 (return)" --> X_OCMENU["MMENUA<br>Main Menu"]
    S -- "invalid key / error" --> MSG["Message line (ERRMSG) on MBILLPA"]
```

### 2.1 MBILLPA — Bill Payment (OCBILLP)

**Layout**

![AS-IS mockup of MBILLPA](../Image/MBILLP_MBILLPA.png)

*This screen appears when the operator selects the bill-payment function.* Physical size 24×80 (mapset `MBILLP`, map `MBILLPA`).

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
| 10 | (literal) | — | caption | ASKIP/NORM | 6 | 5 | 12 | BLUE | — | — | Account ID : | field header |
| 11 | ACCTID | ACCTIDO | input | UNPROT/IC/FSET | 6 | 18 | 11 | GREEN | — | — | — | operator input |
| 12 | (literal) | — | caption | ASKIP/NORM | 8 | 5 | 12 | BLUE | — | — | Curr Bal   : | field header |
| 13 | BLBAL | BLBALO | display | ASKIP/NORM | 8 | 18 | 15 | TURQUOISE | — | — | — | program-supplied display |
| 14 | (literal) | — | caption | ASKIP/NORM | 10 | 5 | 12 | BLUE | — | — | Pay Amount : | field header |
| 15 | BLAMT | BLAMTO | input | UNPROT/IC/FSET | 10 | 18 | 12 | GREEN | — | — | — | operator input |
| 16 | (literal) | — | caption | ASKIP/NORM | 12 | 5 | 11 | BLUE | — | — | Confirm(Y): | field header |
| 17 | BLCONF | BLCONFO | input | UNPROT/IC/FSET | 12 | 18 | 1 | GREEN | — | — | — | operator input |
| 18 | ERRMSG | ERRMSGO | display | ASKIP/NORM | 23 | 1 | 78 | RED | — | — | — | program-supplied display |
| 19 | (literal) | — | caption | ASKIP/NORM | 24 | 1 | 34 | TURQUOISE | — | — | ENTER=Process  PF3=Back  PF4=Clear | field header |

**Item states**

> Legend: `○`=enabled ｜ `必`=enabled (required) ｜ `□`=display only ｜ `×`=hidden ｜ `レ`=initial focus

| № | Field (BMS) | Initial display | Amount entered | Confirm post |
|---|---|---|---|---|
| 1 | TRNNAME | □ | □ | □ |
| 2 | TITLE | □ | □ | □ |
| 3 | CURDATE | □ | □ | □ |
| 4 | PGMNAME | □ | □ | □ |
| 5 | CURTIME | □ | □ | □ |
| 6 | ACCTID | ○ | ○ | □ |
| 7 | BLBAL | □ | □ | □ |
| 8 | BLAMT | ○ | ○ | □ |
| 9 | BLCONF | ○ | ○ | □ |
| 10 | ERRMSG | □ | □ | □ |

## 3. Check specifications

### 3.1 MBILLPA — Bill Payment

All messages below are the literal strings the program sends to the on-screen message line, extracted from the source. Type: E=Error / W=Warning / C=Confirm / I=Information.

| No. | Timing | Condition | Type | Display | Message content (verbatim) | Notes |
|---|---|---|---|---|---|---|
| 1 | ENTER / validation | processing state | I | A (message line) | Enter account id and press ENTER. | on-screen ERRMSG line |
| 2 | ENTER / validation | the entry passed validation | C | A (message line) | Enter amount, set confirm to Y, press ENTER. | on-screen ERRMSG line |
| 3 | ENTER / validation | the entered value failed a field rule | E | A (message line) | Amount must be greater than zero. | on-screen ERRMSG line |
| 4 | ENTER / validation | processing state | I | A (message line) | Amount exceeds current balance. | on-screen ERRMSG line |
| 5 | ENTER / validation | processing state | I | A (message line) | Please enter a payment amount. | on-screen ERRMSG line |
| 6 | ENTER / validation | processing state | I | A (message line) | Amount is not a valid number. | on-screen ERRMSG line |
| 7 | ENTER / validation | the entry passed validation | C | A (message line) | Set confirm to Y to post this payment. | on-screen ERRMSG line |
| 8 | ENTER / validation | processing state | E | A (message line) | Error reading account file. | on-screen ERRMSG line |
| 9 | ENTER / validation | processing state | I | A (message line) | Control record BILLID missing. | on-screen ERRMSG line |
| 10 | ENTER / validation | processing state | E | A (message line) | Error reading control file. | on-screen ERRMSG line |
| 11 | ENTER / validation | processing state | E | A (message line) | Error updating control file. | on-screen ERRMSG line |
| 12 | ENTER / validation | processing state | I | A (message line) | Duplicate bill id generated. | on-screen ERRMSG line |
| 13 | ENTER / validation | processing state | E | A (message line) | Error writing bill file. | on-screen ERRMSG line |
| 14 | ENTER / validation | the keyed record does not exist | E | A (message line) | Account not found on update. | on-screen ERRMSG line |
| 15 | ENTER / validation | processing state | E | A (message line) | Error reading account for update. | on-screen ERRMSG line |
| 16 | ENTER / validation | processing state | E | A (message line) | Error updating account balance. | on-screen ERRMSG line |
| 17 | ENTER / validation | an unsupported key was pressed | E | A (message line) | Invalid key pressed. Please try again. | on-screen ERRMSG line |
| 18 | ENTER / validation | a required field was left blank | E | A (message line) | Please enter all required fields. | on-screen ERRMSG line |
| 19 | ENTER / validation | the keyed record does not exist | E | A (message line) | Record not found. | on-screen ERRMSG line |

## 4. Event specifications

### 4.1 MBILLPA — Bill Payment

Per-key and per-field behaviour, written as operator steps.

| No. | Item | Timing / condition | Action | Next screen |
|---|---|---|---|---|
| **Function key section** | | | | |
| 1 | `ENTER` | when pressed | the payment amount is validated and, once confirmed, the payment is posted | stays on the screen, or continues to the main menu (OCMENU) |
| 2 | `PF3` | when pressed | cancel and return to the main menu (OCMENU) | the main menu (OCMENU) |
| 3 | `PF4` | when pressed | clear the entered values so the operator can start again | stays on the screen |
| **Data entry section** | | | | |
| 4 | Key field(s) | on ENTER | the payment amount is validated and, once confirmed, the payment is posted | stays on `MBILLPA` (or shows a message) |

## 5. DB CRUD

**Update spec**

### WS-ACCTFILE — Account master

| No. | PK | Item name | Field | On confirm — post payment |
|---|---|---|---|---|
| 1 | ✓ | Id | AC-ID | computed / screen input |
| 2 |  | Active Status | AC-ACTIVE-STATUS | computed / screen input |
| 3 |  | Curr Bal | AC-CURR-BAL | computed / screen input |
| 4 |  | Credit Limit | AC-CREDIT-LIMIT | computed / screen input |
| 5 |  | Cash Limit | AC-CASH-LIMIT | computed / screen input |
| 6 |  | Open Date | AC-OPEN-DATE | computed / screen input |
| 7 |  | Expiry Date | AC-EXPIRY-DATE | computed / screen input |
| 8 |  | Reissue Date | AC-REISSUE-DATE | computed / screen input |
| 9 |  | Cyc Credit | AC-CYC-CREDIT | computed / screen input |
| 10 |  | Cyc Debit | AC-CYC-DEBIT | computed / screen input |
| 11 |  | Addr Zip | AC-ADDR-ZIP | computed / screen input |
| 12 |  | Group Id | AC-GROUP-ID | computed / screen input |
| 13 |  | Filler | FILLER | computed / screen input |

### WS-CTRLFILE — Control file

| No. | PK | Item name | Field | On confirm — post payment |
|---|---|---|---|---|
| 1 | ✓ | Key | CT-KEY | computed / screen input |
| 2 |  | Last Value | CT-LAST-VALUE | computed / screen input |
| 3 |  | Desc | CT-DESC | computed / screen input |
| 4 |  | Filler | FILLER | computed / screen input |

### WS-BILLFILE — Bill file

| No. | PK | Item name | Field | On confirm — post payment |
|---|---|---|---|---|
| 1 | ✓ | Id | BL-ID | computed / screen input |
| 2 |  | Acct Id | BL-ACCT-ID | computed / screen input |
| 3 |  | Amount | BL-AMOUNT | computed / screen input |
| 4 |  | Pay Date | BL-PAY-DATE | computed / screen input |
| 5 |  | Confirm Num | BL-CONFIRM-NUM | computed / screen input |
| 6 |  | Status | BL-STATUS | computed / screen input |
| 7 |  | Filler | FILLER | computed / screen input |

**Column-level CRUD (per event / business type)**

| Screen / table | Physical name | Field group | On confirm — post payment |
|---|---|---|---|
| Account master | WS-ACCTFILE | whole record | R/U |
| Control file | WS-CTRLFILE | whole record | R/U |
| Bill file | WS-BILLFILE | whole record | C |

