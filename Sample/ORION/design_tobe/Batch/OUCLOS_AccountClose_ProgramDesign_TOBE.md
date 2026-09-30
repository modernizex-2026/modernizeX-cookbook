# TO-BE Batch Program Design — OUCLOS_AccountClose

> **TO-BE = AS-IS in business terms.** The section order follows the AS-IS document so the two
> compare 1-to-1; what is added is the mapping onto the generated Java/Spring module. Anything
> forced to differ is stated in the section it belongs to, in business terms.

**Header**

| Item | Content |
|---|---|
| System | ORION-CCMS (ORION Credit Card Management System) |
| Source program | OUCLOS（COBOL） |
| TO-BE module | `orion-online/back-end/programs/ouclos` · package `com.generated.orion.ouclos` |
| Platform | Java 17 · Spring Boot 3.x · DB Oracle (H2 in dev) |
| Author ／ Date | modernizeX ／ 2026-09-28 |

> **Form-factor note (business impact):** in AS-IS this account-close run was a COBOL routine invoked
> from the Operations screen. The transpiler produced it as an in-process service in the on-line
> back-end (`OuclosService`), **not** as a stand-alone Spring Batch job; the business logic — which
> accounts are closed, for what reason, and which are left untouched — is unchanged. It is still
> launched from the Operations screen. See §8.

## 1. Program overview

### 1.1 TO-BE I/O diagram

```mermaid
---
config:
  layout: elk
  look: neo
  theme: base
---
flowchart LR
    IN1[("Account master<br>acctfile")] --> SVC["OuclosService<br>(account close)"]
    SVC -- "set status to closed" --> OUT1[("Account master<br>acctfile")]
    SVC -- "counts / status / amount" --> CALLER["Operations screen<br>result area"]
```

### 1.2 Function overview

Close expired or flagged accounts on demand. The run reads the account master in key order; an active
account is closed when its expiry date is present and earlier than the run date (reason: expired) or
its credit limit is zero (reason: zero-limit). Expired takes precedence, so each closed account maps
to a single reason. Accounts to be closed are re-read for update, their status set to closed, and
saved; accounts already closed are counted and left untouched, and accounts meeting neither condition
are counted as kept active. The run reports how many accounts were read, closed, updated, skipped and
kept, how many were closed for each reason, and the total balance of the closed accounts.

### 1.3 I/O table

| Business name | File ／ table | I/O |
|---|---|---|
| Account master (was VSAM KSDS) | table `acctfile` | I-O |
| Request / result area | in-memory call area (was COMMAREA) | I-O |

> The account master keyed browse (start / read-next) becomes an ordered read of `acctfile` by its
> primary key `ac_id`, so accounts are still processed in ascending account-id order. Field widths
> and money precision are preserved (see §4).

### 1.4 Called subprograms / modules

| Item | Notes |
|---|---|
| `AcctfileFileDao` | data access for the account master |
| (no sub-program) | the routine calls no external business program |

### 1.5 DB tables & SQL

| Table | Operation | Columns |
|---|---|---|
| `acctfile` | SELECT (ordered read), SELECT … FOR UPDATE, UPDATE | status column via JdbcTemplate |

### 1.6 Special notes

- Launched on demand from the Operations screen; an optional single account may be requested
  (`KO-PARM-ACCT`; 0 means every account).
- Close test: an active account is closed when its expiry date is set and earlier than the run date
  (expired), or its credit limit is zero (zero-limit); expired takes precedence.
- The run date is derived from the current system date at the start of the run.
- Money is held as `BigDecimal`. No amount is computed here — the run only accumulates the balance of
  each closed account into the closed-balance total, held at two-decimal scale (see §4).

## 2. Processing details

_One row per business step, in execution order. Paragraph and method names are intentionally absent._

| 識別番号 | Business step | What happens | Condition ／ actual value |
|---|---|---|---|
| 0.0 | The account-close run starts | the run is launched from the Operations screen and drives preparation, the account loop and the wrap-up in turn |  |
| 1.0 | Prepare the run | clears the result counters, sets the status to in-progress and captures the run date; if a single account was requested, switches on the filter for that one account | single account when the requested account is greater than zero |
| 3.0 | Read the accounts in order | positions at the first (or requested) account and reads forward until the end |  |
| 3.1 | Position the read | starts reading the account master from the first or the requested account; when there is nothing to read it reports that there are no accounts to process; a store error stops the run | not-found → "no accounts to process"; other error → run fails |
| 3.2 | Take the next account | reads the next account and counts it as read; at end of file the loop finishes; a store error stops the run | end of accounts ends the loop |
| 4.0 | Assess one account | an account already closed is counted as skipped and left as it is; otherwise the closing conditions are checked | already-closed accounts are skipped |
| 4.1 | Check the closing conditions | marks the account to close when its expiry date is present and earlier than the run date (reason: expired) or, failing that, when its credit limit is zero (reason: zero-limit); an account meeting neither is kept active and counted | expired takes precedence over zero-limit |
| 4.2 | Close the account | re-reads the account for update, sets its status to closed, saves it, counts it as closed, adds its balance to the closed-balance total and tallies the reason (expired or zero-limit); a failed read or save counts the account as rejected |  |
| 3.4 | Finish the read | ends the account read once the loop is done |  |
| 9.0 | Wrap up the run | sets the final status (complete, complete-with-warnings when any account was rejected, or failed) and returns the counts and the closed-balance total to the operator | warning status when at least one account was rejected |

## 3. Structure diagram

_The call structure of the routine as generated._

```mermaid
---
config:
  layout: elk
  look: neo
  theme: base
---
flowchart TB
    OCOPS["Operations screen (OCOPS)"] --> SVC["OuclosService.mainLine"]
    SVC --> INIT["prepare run / counters"]
    SVC --> LOOP["account read loop"]
    SVC --> FIN["wrap up / status"]
    LOOP --> COND["check close conditions<br>(expired / zero-limit)"]
    LOOP --> UPD["AcctfileFileDao<br>read-for-update + save"]
    UPD --> ACCT[("acctfile")]
```

## 4. Output specifications (file / table)

The account master record written back keeps the original field widths; each field also maps to a
column of the `acctfile` table.

### 4.1 Account master (ACCT-REC) — 300 bytes

| Level | Item name | Field ／ column | Type ／ bytes | Position | Java ／ SQL type | How it is set |
|---|---|---|---|---|---|---|
| 01 | Record | ACCT-REC | group ／ 300 | 1 | row of `acctfile` | whole record |
| 05 | Account id | AC-ID | 9(11) ／ 11 | 1 | `long` ／ `NUMERIC(11)` | record key (unchanged) |
| 05 | Active status | AC-ACTIVE-STATUS | X(01) ／ 1 | 12 | `String` ／ `VARCHAR2(1)` | set to closed (`N`) for a closed account |
| 05 | Current balance | AC-CURR-BAL | S9(10)V99 ／ 12 | 13 | `BigDecimal` ／ `DECIMAL(12,2)` | unchanged |
| 05 | Credit limit | AC-CREDIT-LIMIT | S9(10)V99 ／ 12 | 25 | `BigDecimal` ／ `DECIMAL(12,2)` | unchanged |
| 05 | Cash limit | AC-CASH-LIMIT | S9(10)V99 ／ 12 | 37 | `BigDecimal` ／ `DECIMAL(12,2)` | unchanged |
| 05 | Open date | AC-OPEN-DATE | X(10) ／ 10 | 49 | `String` ／ `VARCHAR2(10)` | unchanged |
| 05 | Expiry date | AC-EXPIRY-DATE | X(10) ／ 10 | 59 | `String` ／ `VARCHAR2(10)` | unchanged |
| 05 | Reissue date | AC-REISSUE-DATE | X(10) ／ 10 | 69 | `String` ／ `VARCHAR2(10)` | unchanged |
| 05 | Cycle credit | AC-CYC-CREDIT | S9(10)V99 ／ 12 | 79 | `BigDecimal` ／ `DECIMAL(12,2)` | unchanged |
| 05 | Cycle debit | AC-CYC-DEBIT | S9(10)V99 ／ 12 | 91 | `BigDecimal` ／ `DECIMAL(12,2)` | unchanged |
| 05 | Address zip | AC-ADDR-ZIP | X(10) ／ 10 | 103 | `String` ／ `VARCHAR2(10)` | unchanged |
| 05 | Group id | AC-GROUP-ID | X(10) ／ 10 | 113 | `String` ／ `VARCHAR2(10)` | unchanged |
| 05 | Filler | FILLER | X(178) ／ 178 | 123 | — | reserved |

> COMP-3 money fields are held as `BigDecimal` and stored at the COBOL-declared two-decimal scale,
> truncated toward zero (`RoundingMode.DOWN`) — the same result as the original COBOL, which carries
> no `ROUNDED` clause. This run performs no money computation: the account balance is carried through
> unchanged, and the closed-balance total simply accumulates those balances at two-decimal scale.

## 5. In/out parameters & check specifications

### 5.1 Parameters

| Kind | Value | Notes |
|---|---|---|
| Input parameter | a single account to close, or 0 for all accounts (`KO-PARM-ACCT`) | supplied by the Operations screen |
| Return value | a status flag — complete, complete-with-warnings, or failed (`KO-STATUS`) plus a status message | tells the operator the outcome |
| Return counts | accounts read, closed, updated, skipped (already closed), kept active and rejected, plus closed-for-expired and closed-for-zero-limit | shown back on the Operations screen |
| Return amount | total balance of the closed accounts (`KO-AMT-1`) | shown back on the Operations screen |

### 5.2 Checks

| No. | Kind | Condition | Error code | Behaviour |
|---|---|---|---|---|
| 1 | Store access | the account read cannot be positioned | — | the run stops and reports "STARTBR ACCTFILE FAILED." |
| 2 | Store access | reading the next account fails | — | the run stops and reports "READNEXT ACCTFILE FAILED." |
| 3 | Business | an account cannot be re-read or saved for update | — | that account is counted as rejected and the run ends with a warning |
| 4 | Business | no accounts qualify for closing | — | the run ends normally reporting "NO ACCOUNTS TO PROCESS." |

## 6. Modification summary

| No. | Date | Title | Content |
|---|---|---|---|
| 1 | 2026-09-28 | Initial TO-BE mapping | Mapped from the AS-IS design onto the generated `OuclosService`; behaviour preserved. |

## 7. Screen

No screen — the routine is driven from the Operations screen and reports its outcome there.

## 8. TO-BE operations (replacing JCL/BAT)

| Item | Value |
|---|---|
| Trigger | launched on demand from the Operations screen; an optional single account can be entered, otherwise every account is scanned. This former batch routine now runs as an in-process on-line service, not a stand-alone Spring Batch job; the business logic is unchanged |
| Schedule | not determined (ask the team) |
| Input data | the current account statuses, expiry dates and credit limits already held in the database |
| Log | written to the application log; the outcome counts and the closed-balance total are returned to the operator on screen |
| Rerun | safe to rerun; an account already closed is counted as skipped and left untouched, so no account is closed twice |
