# ModernizeX: A Grounded, Air-Gapped Static-Analysis-and-LLM Pipeline for End-to-End COBOL-to-Java Modernization

**Authors:** ModernizeX — Legacy Modernization Group, ModernizeX Organization
**Version:** 1.0 · **Date:** 2026-07-25
**Status:** Working Paper

---

## Abstract

Mainframe systems written in COBOL still process a large fraction of the world's financial, insurance, and government transactions, yet the population of engineers able to maintain them is shrinking and the cost of manual rewrites is prohibitive. Recent Large Language Models (LLMs) can translate code, but applied naively to legacy monoliths they hallucinate identifiers, lose global context across programs that exceed the context window, and — critically for regulated enterprises — require uploading proprietary source code to public clouds. We present **ModernizeX**, an end-to-end platform that modernizes COBOL/CICS/JCL systems to Java by combining *deterministic static analysis* with *grounded LLM generation*. ModernizeX first parses the legacy codebase into an Abstract Syntax Tree (AST) and a family of program-, data-, and control-flow graphs, then migrates the deterministic core to Java with a rule-based methodology that preserves the logic and architecture of the legacy system. The platform drives a **seven-stage pipeline** (Assessment → Reverse Engineering → Code Generation → Code Refactoring → UI/UX Modernization → Data Migration → Validation) that generates design documentation (SAD/SDD), a bidirectional traceability matrix, migrated source code, and modernized source code (refactored services and a modernized user interface), all of which retain traceability to the legacy system. We measured the pipeline end-to-end on a complete codebase — **SAKURA-SMS** (68 programs, 30,635 LOC; interactive screens + batch over indexed/sequential files) — completing the migration, through executed unit tests and an integration-test specification, in **38 operator-hours**, with the generated code compiling out of the box and a unit-test campaign completed across all 68 modules, while guaranteeing zero source-code egress. We discuss the design of the grounding IR, the security and licensing architecture required for enterprise adoption, limitations, and directions for extending the approach to other legacy languages.

**Keywords:** legacy modernization, COBOL, mainframe, program analysis, abstract syntax tree, large language models, code translation, reverse engineering, traceability, effort measurement, air-gapped deployment.

---

## 1. Introduction

Decades of business logic remain encoded in COBOL running under CICS and JCL on IBM mainframes and midrange systems. These systems are stable but increasingly *unmaintainable*: their original authors have retired, documentation has drifted from or never existed alongside the code, and the languages and platforms are absent from mainstream engineering education. Enterprises therefore face a strategic dilemma between two flawed extremes:

1. **Manual rewrite by system integrators** — slow and expensive, and gated by scarce COBOL expertise for which organizations already pay a premium simply to *maintain* existing systems [7]. Even narrowly scoped, tool-run code-conversion services are publicly priced at US$2.75 per line *before* any testing, review, or integration effort [6]; timelines routinely run 12–18 months per major system, and landmark core-platform replacements have taken far longer — Commonwealth Bank of Australia's rewrite took five years and approximately US$750 million [5].
2. **Naive LLM translation** — fast to start but unreliable at scale and insecure: general-purpose LLMs are text predictors, not compilers. Fed thousands of lines of interdependent COBOL, they lose track of global variables outside the context window, invent syntax and identifiers, and — when driven through public web or API interfaces — require the customer's proprietary code to leave its security perimeter.

The core observation motivating this work is that **the failures of the naive LLM approach are not failures of language modeling per se, but failures of *grounding and context*.** COBOL programs are tightly coupled monoliths in which a single copybook is shared across dozens of programs and control passes between programs via `CALL`, `LINK`, and `XCTL`. An engineer — or a model — cannot correctly rewrite one program without knowing its *blast radius*: what data it reads and writes, which programs invoke it, and which it invokes. This information is *deterministically recoverable* from the source through static analysis, and does not require a language model at all. Our thesis is that if we first recover this structure with classical program analysis and hand the model a **standardized intermediate representation** rather than raw text, we can keep the system architecturally valid and eliminate the model's hallucination and guesswork from the very first step.

We further observe that legacy modernization is not a single code-translation task but a *documentation and verification* task. Regulated customers will not "turn off the mainframe" on the basis of translated code alone. Instead, they require (a) design documentation that explains the as-is system in business terms, (b) a traceability matrix proving that every legacy component maps to a tested modern component, and (c) evidence that the new system reproduces the old system's outputs for the same inputs. Thus, a useful legacy modernization platform must treat understanding, translation, and verification as a single pipeline. Our measurements bear this out: effort concentrates not in translation — which the deterministic stages make near-free — but in *verification*, where human review of refactor reports, test results, and integration-test sign-off dominates the hours.

This paper makes the following contributions:

- **A grounded, two-phase engine** in which deterministic static analysis (Phase 1) produces a standardized AST with program/data/control/screen/DB graphs and migrates the code to the target system (Java) with a rule-based methodology; grounded LLM generation (Phase 2) then performs modernization on top of this deterministic base, reducing hallucination and enforcing architectural integrity.
- **An end-to-end seven-stage pipeline** — Assessment, Reverse Engineering, Code Generation, Code Refactoring, UI/UX Modernization, Data Migration, and Validation — that unifies assessment, graph analysis, reverse-engineering of design documentation (SAD/SDD), COBOL→Java migration and modernization, screen modernization to a Vue 3 front end, relational data migration, and automated behavioral-equivalence unit and integration testing, tied together by a bidirectional traceability matrix with human-in-the-loop review.
- **A security and deployment architecture** — Bring-Your-Own-Key (BYOK) LLM routing, and cryptographically metered licensing — designed so that no proprietary source code leaves the customer's network, addressing the primary objection of banking, insurance, and government buyers.
- **A measured effort evaluation** on a complete codebase — *SAKURA-SMS* (68 programs / 30,635 LOC, interactive screens + batch over ISAM files) — migrated end-to-end in **38 h** of measured operator + pipeline time, producing a compiling multi-module Spring Boot / Spring Batch project and a unit-test campaign completed across all 68 modules.

The remainder of the paper is organized as follows. Section 2 surveys related work. Section 3 defines the success criteria for the problem and describes the ModernizeX architecture and the seven-stage methodology in detail. Section 4 presents the evaluation setup, Section 5 the results, and Section 6 discusses limitations and threats to validity. Section 7 concludes.

---

## 2. Related Work

### 2.1 Automated Legacy Code Translation

Rule-based transpilers and commercial refactoring suites have long offered "like-for-like" COBOL-to-Java conversion. They are deterministic and fast but tend to produce non-idiomatic "JOBOL" — Java that mirrors COBOL's flat, procedural structure (e.g., `PERFORM` loops and 88-level conditions rendered literally) rather than modern object-oriented services. They also struggle with dialect-specific constructs and embedded CICS/SQL. ModernizeX retains a rule-based generator for the deterministic core — in our measured runs it emits a complete multi-module Spring Boot / Spring Batch project that **compiles out of the box, without manual patching** — but delegates *refactoring toward idiomatic OOP, naming, and exception handling* to a separate LLM stage, combining the speed and determinism of rules with the flexibility of generative models.

### 2.2 Large Language Models for Code

Code LLMs have demonstrated strong performance on code generation, summarization, and program repair for mainstream languages. However, general-purpose and even code-specialized models are under-trained on COBOL and mainframe artifacts, and public benchmarks for the domain remain limited. Domain-adapted models such as XMainframe [1] address the *knowledge* gap by continued pretraining and instruction tuning on curated COBOL corpora and by releasing a dedicated benchmark (MainframeBench).

### 2.3 Reverse Engineering and Documentation Generation

Program comprehension research has produced call-graph extraction, data-flow analysis, and clone/dead-code detection tooling for legacy systems. What has typically been missing is the *last mile*: turning these graphs into business-readable design documents (SAD/SDD), decision tables, and pseudo-code that a Java developer who does not know COBOL can act on directly, and binding those documents to source and tests through a traceability matrix. ModernizeX treats documentation as a first-class, versioned, human-reviewable deliverable produced by the same grounded pipeline that performs translation, and enforces the discipline that *every* modern component trace back to a specific legacy component and design section.

---

## 3. The ModernizeX Methodology

Given a legacy source set $S_L$ — COBOL programs, copybooks, JCL, embedded SQL, and CICS BMS screen maps — ModernizeX produces a modern system $S_M$ (Java 17/21 Spring Boot / Spring Batch services, a Vue 3 front end, and a relational target schema, typically PostgreSQL) together with the evidence required to trust it: design documentation $D$ (SAD, SDD, Test Plan), a bidirectional traceability matrix $T$, and behavioral-equivalence verification results $V$.

**Success criteria.** A modernization is *complete* only when three conditions hold jointly, forming the "definition of done" that every stage of the methodology below is designed to satisfy:

1. **Traceability** — every legacy component (program, copybook, JCL step, CICS transaction, screen) maps to at least one modern component in the bidirectional matrix $T$, with no orphan legacy programs and no requirement lacking a test. Each mapping is navigable through the design documents $D$, so a reviewer can walk from any legacy artifact to its modern counterpart, its design rationale, and its tests — and sign off that no business logic was silently lost.
2. **Correctness** — the modernized system preserves the legacy system's logic to the maximum extent: every business rule, computation, condition branch, and edge-case behavior encoded in $S_L$ is carried into $S_M$ — restructured into idiomatic form, but never dropped, approximated, or invented. Migration may change the *shape* of the code (paragraphs into methods, copybooks into DTOs, CICS transactions into endpoints), never its *semantics*; this guarantees the migrated system is both correct and complete with respect to the original.
3. **Comparability** — the legacy and modernized systems can be executed and compared on identical inputs: behavioral-equivalence testing $V$, backed by automated tests, demonstrates field by field that the modern component reproduces legacy outputs, so acceptance rests on measured equivalence between $S_L$ and $S_M$ rather than on code inspection.

For the target market — banks, insurers, government — a fourth hard constraint is **data confidentiality**: at no point may $S_L$ traverse an untrusted network. This rules out copy-paste-into-web-LLM workflows and shapes the deployment architecture (Section 3.10).

The remainder of this section describes the architecture and each pipeline stage that together meet these criteria.

### 3.1 Architecture Overview

ModernizeX follows a **thin-client / fat-server** design. A lightweight VS Code extension (and, for team workflows, a multi-project WebApp) handles UI, local line-of-code counting, file selection, and diff review. All proprietary logic — the COBOL parser, static analyzers, IR builder, LLM gateway, code generator, builder, and test runner — lives in a backend **worker pool** orchestrated by a durable workflow engine (e.g., Temporal) that supports long-running jobs, retries, and per-stage checkpoints. The backend can be deployed as a SaaS tenant or, for confidentiality-sensitive customers, as an **air-gapped on-premise Docker deployment** (Section 3.10).

The platform executes a migration as **seven tool-driven stages**. Each stage is automated; operator effort is limited to launching, monitoring, and reviewing stage outputs:

| # | Stage | What the tool does |
| :-- | :---- | :---- |
| 1 | **Assessment** | Ingest the source tree; inventory programs/copybooks/screens/JCL; classify each program (batch / screen / online); count LOC; map `CALL` dependencies; flag unsupported constructs. |
| 2 | **Reverse Engineering** | Parse every COBOL program into a full XML AST; resolve copybooks (incl. `REPLACING`); build the symbol table / IR and code graphs; emit the application manifest and reverse-design documents (SAD/SDD). |
| 3 | **Code Generation** | Generate a complete multi-module Maven project (Spring Boot / Spring Batch) from the AST: one module per program, shared runtime (`common`), file-I/O layer with dual text/SQL mode, screen manifest, and per-screen Vue 3 SFC scaffolding. Output compiles out of the box. |
| 4 | **Code Refactoring** (Modernization) | AI-driven structural refactor of the generated services with behavioral-fingerprint preservation; runs programs in parallel; emits per-program refactor reports and diffs. |
| 5 | **UI/UX Modernization** | Replace the 24×80 terminal emulation with per-screen modern Vue components — labels harvested from the COBOL screen captions, faithful field/validation behavior, optional design-system theming. |
| 6 | **Data Migration** | Derive relational DDL from the COBOL record layouts (ISAM/VSAM → PostgreSQL), generate column mappings for divergent customer schemas, load provided sample/master data. |
| 7 | **Validation** | Generate and execute JUnit tests per module (parallel, with JaCoCo coverage aggregation) and produce/execute the integration-test specification against the running system. |

Stages 1–3 and 6 are **fully deterministic** (parser + rule-based generator): reproducible, near-zero marginal operator effort. Stages 4, 5, and 7 combine **AI agents executed in parallel per program** with human review of refactor reports, test results, and sign-off — by design, this is where the effort budget lives, because it is where equivalence is *proven*. Parallel per-program execution is also designed to keep the pipeline flat as codebases grow (a property we expect to hold at scale; see Section 7).

The pipeline advances a project through an explicit state machine, each state a resumable checkpoint:

```
DRAFT → CONFIGURED → ASSESSED → REVERSED → GENERATED
      → REFACTORED → UI_MODERNIZED → DATA_MIGRATED → VALIDATED → COMPLETED
                                  ↘ NEEDS_REVIEW (loop back)
```

Persistence is split across a relational store (project/run metadata), an object store (source snapshots and artifacts), and a graph database (Neo4j/JanusGraph) for the code graphs. The following subsections describe each pipeline stage as a methodological step.

### 3.2 Stage 1 — Assessment

Assessment ingests the source tree and produces, before any code is generated, a complete inventory and risk picture of the legacy estate: program counts by type (batch / screen / online), LOC by artifact class (programs, copybooks, BMS maps, JCL), a `CALL`-dependency map, and flags for unsupported or high-risk constructs. From the same pass, ModernizeX computes an average cyclomatic complexity, a per-program complexity score (Low/Medium/High/Critical combining LOC, call depth, CICS command count, file I/O, and copybook fan-in), and risk indicators (dead/duplicate code via clone detection, `GO TO`/`ALTER`/self-modifying patterns, DB2-specific SQL, JCL utility dependencies). This yields an effort estimate and an *auto-migration coverage estimate* (percentage automatable vs. requiring manual work). The stage is fully automated; measured operator + pipeline time was **0.2 h** on the ~30K-LOC evaluation codebase.

### 3.3 Stage 2 — Grounded Reverse Engineering

The foundational design decision of ModernizeX is that **the LLM never sees raw legacy code**. Stage 2 therefore proceeds in two phases: deterministic recovery of structure, then grounded translation of that structure into documentation.

**Parsing.** Each program is parsed into a full XML AST — divisions, data items, screen sections, statements — using a COBOL grammar (ProLeap/Koopa-class parser, or an internal ANTLR grammar), with dialect awareness (IBM Enterprise COBOL, Micro Focus, GnuCOBOL) and expansion of `COPY` directives (including `REPLACING`) so copybook fields are resolved in context. Embedded `EXEC SQL` and `EXEC CICS` blocks are extracted and typed. The symbol table and IR are built from the resolved ASTs, and the stage emits an **application manifest** describing every program, screen, and data store.

**Graph construction.** From the ASTs the engine builds a family of graphs that together capture the system's structure:

| Graph | Nodes → Edges | Purpose |
|---|---|---|
| **Call graph** | program → program (`CALL`/`LINK`/`XCTL`) | Execution order, blast radius, leaf-first rewrite order |
| **Data-flow graph** | record/field → program (read/write) | Which programs own which data |
| **Copybook dependency** | program ↔ copybook | Shared data models; change-impact |
| **Job-flow graph (JCL)** | step → program → dataset | Batch triggers and sequencing |
| **DB-access graph** | program → table/view (CRUD) | Data ownership for schema design |
| **Screen-flow graph (CICS)** | BMS map → transaction → program | Screens re-expressed as API endpoints |
| **Module clusters** | community detection over the above | Candidate microservice / bounded-context boundaries |

**Standardized IR.** The AST and graphs are serialized into a single canonical, machine-readable representation (an XML/JSON IR). Every construct is explicitly typed and every cross-reference (a called program, a shared copybook, a written dataset) is a resolved link rather than an inferred one. This IR is the *grounding* that all downstream AI stages consume. Because it is produced deterministically, its outputs are reproducible: re-running analysis over unchanged source yields identical graphs, and edits trigger only incremental re-analysis.

**Grounded documentation.** Given the IR, the LLM's role is *translation of structure into language*, not recovery of structure. Prompts are assembled from templated slots filled by IR fragments — a program's resolved inputs/outputs, its paragraphs, its copybook layouts, its CICS transactions — so the model is asked to *explain and re-express known facts* rather than to infer global architecture from a text window. This grounding is what converts an unreliable text-predictor into a bounded, auditable component. The phase produces four deliverables:

- **SAD (Software Architecture Document):** context and container diagrams, deployment view, external interfaces (message queues, IMS, DB2), and cross-cutting concerns, derived from the call and DB-access graphs.
- **SDD (Software Design Document):** for each program, its inputs/outputs, business rules expressed as pseudo-code and decision tables, data structures from copybooks, and DB schema from `EXEC SQL`/DCLGEN. Per the abstraction strategy, business logic is rendered in target-neutral pseudo-code so a Java or C# developer can implement it without reading COBOL.
- **Test Plan:** scope of UT/IT/regression, data strategy, and environment requirements.
- **Traceability Matrix:** the backbone artifact (Section 3.9).

Every document and section is versioned and carries a review state (`DRAFT / IN_REVIEW / APPROVED`), supporting inline editing, comments, and diffs between versions — the human-in-the-loop discipline that makes the output trustworthy. Measured stage time: **2 h** on the ~30K-LOC evaluation codebase.

### 3.4 Stage 3 — Rule-Based Code Generation

Code generation is deliberately **deterministic**: a rule-based generator (pluggable, customer-specific rule packs — COBOL pattern → target pattern, naming rules, exception-handling rules) transpiles the IR into a complete, buildable target project rather than fragments:

- a **multi-module Maven project** (Spring Boot / Spring Batch) with **one module per COBOL program** plus a shared runtime module (`common`);
- a **file-I/O layer with dual text/SQL mode**, so generated programs can run against flat files first and a relational store after data migration (Stage 6);
- a **screen manifest** and **per-screen Vue 3 SFC scaffolding** for every screen program, consumed by Stage 5;
- provenance annotations on every generated unit (`@cobol-origin: PROGRAM-X line 123-145`) and a multi-level **old↔new mapping** (program↔service, paragraph↔method, copybook↔DTO, JCL step↔job, CICS transaction↔endpoint, VSAM↔table) shown in a side-by-side viewer.

The output **compiles out of the box** — a property verified on every measured run, and the reason this stage costs **0.3 h** at ~30K LOC. Migration is **incremental**: customers can migrate module by module, and mappings support 1-to-N splits and N-to-1 consolidation.

### 3.5 Stage 4 — AI Code Refactoring (Modernization)

Rule-generated code is correct but non-idiomatic. Stage 4 applies an **AI-driven structural refactor** to the generated services: deduplicating near-identical paragraphs, decomposing long methods into cohesive units, mapping copybooks to DTOs/entities, re-expressing CICS transactions as REST endpoints and JCL steps as scheduled jobs, and renaming to intent-revealing names. Two properties make this stage safe and scalable:

- **Behavioral-fingerprint preservation.** Each refactor is checked against a behavioral fingerprint of the pre-refactor unit, so restructuring cannot silently change semantics — the LLM improves *shape*, never *meaning*.
- **Parallel per-program execution** with automatic job sizing, emitting a per-program refactor report and diff for human review.

**Build-and-verify loop.** Refactored code is compiled automatically (`mvn compile`) with optional static analysis (SpotBugs/SonarQube). Failures and low-confidence spans feed a **Review Report** listing exactly what a human must resolve — un-mappable patterns (`ALTER`, complex `REDEFINES`, exotic CICS), residual build/lint errors, missing dependencies — each with a suggested LLM patch assignable in one click, with diffs tracked across fix iterations. Measured stage time: **10 h** on the ~30K-LOC evaluation codebase — the largest AI-stage cost, dominated by human review of refactor reports.

### 3.6 Stage 5 — UI/UX Modernization

Stage 5 replaces the 24×80 terminal emulation with **per-screen modern Vue components**: labels are harvested from the COBOL screens' own literal captions, field and validation behavior is reproduced faithfully from the SCREEN SECTION / BMS definitions, and an optional design-system theme can be applied. Because the screen manifest and SFC scaffolding already exist from Stage 3, this stage is cheap in pipeline terms (**~0.5 h** at ~30K LOC); the deeper UI/UX *redesign* — iterating with the customer's users in confirmation cycles — is downstream delivery work, deliberately kept outside the automated pipeline.

### 3.7 Stage 6 — Data Migration

Stage 6 derives relational DDL directly from the COBOL record layouts (ISAM/VSAM → PostgreSQL tables), generates column mappings where the customer's target schema diverges from the derived one, and loads provided sample/master data. Combined with the dual-mode file-I/O layer from Stage 3, this lets the migrated system switch from file-based to SQL-based persistence without touching business logic. Measured stage time: **~1 h** at ~30K LOC.

### 3.8 Stage 7 — Validation (Behavioral Equivalence)

Trust ultimately rests on showing that $S_M$ behaves like $S_L$. Validation has two measured sub-stages:

- **Unit testing.** JUnit tests are generated per module from the reverse-engineered behavior and any available sample I/O, executed **in parallel across modules**, with JaCoCo coverage aggregated into a single report against a configurable coverage floor.
- **Integration testing.** The pipeline produces an integration-test specification from the job-flow and screen-flow graphs and executes it against the running system.

For equivalence comparison, ModernizeX classifies test cases as **auto-runnable** (batch programs with well-defined input/output datasets, UI-independent services, SQL stored procedures) or **manual-only** (cases depending on 3270 screens or un-stubbable external systems), with user override. For auto-runnable cases, a harness executes the legacy environment (mainframe or emulator) and the modern service on identical inputs and compares outputs under configurable rules:

- **bit-exact** (fixed-width/binary records),
- **field-level** (parsed against copybook layout, compared field by field),
- **numeric tolerance** (±ε for floating point), and
- **order-insensitive** (unordered datasets).

A field-by-field diff viewer highlights mismatches, and reports distinguish *"fail because the new code is wrong"* from *"fail because the test data differs"* — a distinction that is essential for productive debugging. Results are versioned across runs and linked back into the traceability matrix. Validation is deliberately the most expensive stage — **24 h** at ~30K LOC — because the hours are spent *proving* equivalence, not *writing* code.

### 3.9 Traceability and Human-in-the-Loop

The traceability matrix is the artifact that guarantees no business logic is silently lost — the failure mode that most often sinks migration projects during user-acceptance testing. ModernizeX maintains it as a searchable, filterable structure rather than a static file, linking SAD requirements ↔ SDD sections ↔ source programs ↔ unit tests ↔ integration tests bidirectionally. Cells are clickable to the underlying artifact; the system warns on requirements without tests and source without an SDD entry. This operationalizes the definition of done: a legacy program is complete only when equivalent modern logic exists, automated tests prove it, and a business analyst signs off that outputs match.

### 3.10 Security, Deployment, and Licensing

Because the core value proposition to regulated buyers is *"your source code never leaves your network,"* the security architecture is a first-class part of the methodology, not an afterthought:

- **BYOK / model-agnostic routing.** Customers supply their own LLM keys or endpoints. When a local or self-hosted model is used, source-derived IR never leaves the customer's infrastructure — a "no-LLM-egress" mode.
- **Air-gapped Docker deployment.** For the strictest customers, the backend ships as a Docker image running entirely on customer infrastructure; the VS Code extension points at an internal URL.
- **IP protection.** The proprietary engine is compiled to an obfuscated binary inside the image, so the algorithm is not recoverable even with container access.
- **Cryptographic licensing and metered billing.** Entitlements (LOC limit, expiry) are issued as signed license files verified by a public key embedded in the binary; offline usage is metered in a tamper-evident, encrypted local store (hash-chained to detect rollback), enabling per-executable-LOC pricing without phoning home.

Non-functional targets round out the platform: OAuth2/OIDC SSO, RBAC, immutable audit logging, AES-256 at rest and TLS 1.3 in transit, structured logs/metrics/tracing, and analysis throughput of ~500K LOC in ≤30 minutes.

---

## 4. Evaluation Setup

We evaluate ModernizeX with **measured effort on a real, complete migration** rather than projections: per-stage hours were recorded on an actual pipeline run (operator time to launch, monitor, and review each stage, plus pipeline execution time), and all source-code statistics were measured directly from the repository. The evaluation comprises one complete codebase migrated end-to-end through validation.

### 4.1 Subject: SAKURA-SMS — Sales Management (Interactive Screen + Batch)

*SAKURA-SMS* is a complete sales-management system built as interactive terminal programs plus batch processing over indexed/sequential files:

| Metric | Value |
|---|---|
| COBOL programs | **68** (.cob) — 60 main + 1 menu + 7 sub |
| COBOL LOC | **30,635** |
| Copybooks | **72** (1,106 LOC) |
| Screens | **46 programs with SCREEN SECTION** (terminal UI) |
| Batch / callable subs | 22 programs |
| Database | Indexed/sequential files (ISAM); no CICS, no embedded SQL |

SAKURA-SMS exercises the SCREEN SECTION interactive-UI path and ISAM (indexed/sequential) file semantics, with no CICS and no embedded SQL — the interactive-plus-batch profile common to midrange COBOL estates.

### 4.2 Measurement Methodology

- **Measured, not projected.** Per-stage hours come from an actual pipeline run recorded by the application team; they include operator time to launch, monitor, and review each stage. Source statistics (program counts, LOC, copybooks, screens) were measured directly on the repository.
- **Verified outputs.** Build status, module inventories, and test-campaign results reported in Section 5.2 are verified from the generated repository, not from tool logs alone.
- **Baseline.** No team manually migrated the same codebase in parallel; the fully manual figure for SAKURA-SMS is a rough, owner-adjustable estimate (> 12 man-months), not a measurement.
- **Man-month conversion:** 1 MM ≈ 21 working days × 8 h = 168 h.

---

## 5. Results

### 5.1 Measured Effort per Stage

Hours below are actual measured effort (operator + pipeline time per stage) using ModernizeX with the Cobol2Java generator:

| Stage | SAKURA-SMS |
| :---- | ---: |
| 1. Assessment | 0.2 h |
| 2. Reverse Engineering | 2 h |
| 3. Code Generation | 0.3 h |
| 4. Code Refactoring | 10 h |
| 5. UI/UX Modernization | 0.5 h |
| 6. Data Migration | 1 h |
| 7a. Validation — Unit testing | 12 h |
| 7b. Validation — IT spec | 12 h |
| **Total** | **38 h** |

Two observations structure the reading of these numbers:

- **The deterministic stages are near-free.** Assessment, reverse engineering, generation, and data migration together take **≤ 3.5 h**, because they are fully automated (parser + generator); the output of Stage 3 compiles without manual patching.
- **Effort concentrates where verification lives.** Refactoring (10 h) and validation (24 h) dominate — these stages run AI agents in parallel but retain human review of refactor reports, test results, and IT-spec sign-off. This is by design: the hours are spent *proving* equivalence, not *writing* code. Parallel per-program execution is designed to keep this profile flat as codebases grow; quantifying that scaling on larger estates is future work (Section 7).

### 5.2 Verified Stage Outputs

**SAKURA-SMS.**

- *Generation:* 68 programs → Maven reactors `batch-app-web` (**66 program modules** + shared `common`) and `batch-app-batch` (**2 batch modules**). The full build **compiles clean**.
- *UI:* screen programs regenerated as per-program Vue 3 SFC folders (36 program UI folders), with labels taken from the COBOL screens' own literal captions.
- *Validation:* unit-test campaign completed for **68/68 modules**, with per-module reports and an aggregated JaCoCo coverage report.

### 5.3 Comparison Against the Manual Baseline

Because no team manually re-migrated SAKURA-SMS in parallel, the comparison below is between measured pipeline effort and an *estimated* manual baseline, and should be read as indicative rather than a controlled result:

| Codebase | Size | Fully manual (estimated, owner-adjustable) | ModernizeX pipeline (measured) |
| :---- | :---- | :---- | :---- |
| SAKURA-SMS | 30,635 LOC | **> 12 man-months** (≈ 4 people × 3 months) | **38 h** |

The 38 h covers the pipeline stages **through validation** (generated UT executed, IT spec run); downstream acceptance, UAT, and change-request effort depend on each project's scope and are not included in this measurement.

**Cost implications.** Unit rates (compute, engineering) are deployment-specific and intentionally left out of this paper. As an external market anchor, conversion-only tooling is publicly priced at US$2.75 per line [6] — a figure that covers code conversion alone, *before* any testing, review, or integration — whereas the entire measured ModernizeX pipeline additionally includes executed unit tests and an integration-test specification.

---

## 6. Discussion, Limitations, and Threats to Validity

**Grounding is the mechanism, not the model.** The measured effort profile supports the central thesis. The stages in which no model is asked to guess structure — assessment, reverse engineering, generation, data migration — cost ≤ 3.5 h on a ~30K-LOC codebase and produce output that compiles without manual patching, precisely because they are deterministic transformations of the IR. The AI-assisted stages spend their hours not on writing code but on *human review of equivalence evidence* (refactor reports, test results, IT-spec sign-off). This division of labor makes ModernizeX robust to model choice and able to benefit directly from domain-adapted backends such as XMainframe [1].

**Threats to validity.** The comparison against manual effort rests on an *estimate*, not a parallel measurement: no team manually migrated SAKURA-SMS, and the > 12 man-month figure is a rough, owner-adjustable estimate. The measured evaluation contains no controlled vanilla-LLM arm, so the advantage over naive LLM translation is argued architecturally rather than measured here. The evaluation rests on a single ~30K-LOC codebase, which limits generalization; behavior on much larger estates and on other dialects is not established by these measurements. Operator hours depend on the team's familiarity with the tooling, and the 38 h total excludes downstream acceptance, UAT, and change-request effort, which varies with each project's scope. Absolute effort will also vary with dialect and the density of hard-to-map constructs (`ALTER`, self-modifying code, complex `REDEFINES`, exotic CICS).

**Intrinsic limitations.** Full automation is not the goal or the outcome: un-mappable patterns and low-confidence spans are routed to the Review Report for human resolution; refactoring and validation deliberately retain human review and sign-off; and behavioral-equivalence testing cannot cover screen- or hardware-dependent cases without manual effort. Deep UI/UX *redesign* — iterating with end users — remains downstream delivery work outside the pipeline. Test Compare depends on access to a legacy execution environment or faithful emulator and on representative test data. Finally, on-prem air-gapped deployment complicates licensing and update delivery, mitigated by the cryptographic metering scheme but not eliminated.

**Scope.** The current version targets COBOL→Java (Spring Boot / Spring Batch + Vue 3); other legacy languages (RPG, C/C++, PL/I, Natural, Easytrieve) and additional target platforms (.NET) are roadmap items (Section 7).

---

## 7. Conclusion and Future Work

We presented ModernizeX, an end-to-end platform that reframes COBOL modernization as a *grounded* problem: deterministic static analysis recovers the structure that a language model cannot reliably infer, and the model is then confined to refactoring, screen modernization, and validation on top of a rule-generated, compiling base — all inside an air-gapped deployment that keeps proprietary source within the customer's perimeter. The measured evidence is concrete: a complete ~30K-LOC codebase — SAKURA-SMS (interactive screens + batch over ISAM files) — was migrated end-to-end in **38 h**, producing a compiling multi-module Spring Boot / Spring Batch project, a modernized Vue front end, a unit-test campaign executed across all 68 modules with aggregated coverage, and an integration-test specification. Effort concentrated where it should: on *proving* behavioral equivalence rather than on writing code, satisfying the traceability, correctness, and comparability criteria that regulated modernization demands.

Future work spans both research directions and the product roadmap.

**Research directions:** (1) a controlled, same-codebase comparison against manual and vanilla-LLM translation to convert the estimated baseline of Section 5.3 into a parallel measurement with formal accuracy metrics; (2) a scaling study across larger, multi-hundred-KLOC estates to characterize how pipeline effort grows with size; (3) plugging a domain-adapted mainframe LLM into the refactoring and validation stages and measuring the marginal gain over general models under identical grounding; (4) richer module-clustering to propose bounded-context microservice boundaries automatically; and (5) publishing per-eLOC cost figures once unit rates are fixed per deployment model.

**Product roadmap.** The platform vision extends ModernizeX along three axes:

1. **Interoperability with traditional SDLC toolchains.** All generated deliverables — SAD/SDD, the traceability matrix, test plans, and test reports — will be exportable in the standard formats customers already work with: office documents (DOCX, Excel, etc.) for review and sign-off workflows, and structured exchange formats importable into ALM and modeling tools such as **Codebeamer** and **Enterprise Architect** (e.g., ReqIF for requirements/traceability, XMI for design models). This lets the pipeline's outputs plug directly into a customer's existing requirement-management, review, and audit processes instead of living only inside the platform.
2. **Additional legacy source languages.** Extending the grounded IR pipeline beyond COBOL to other legacy estates — **RPG** (IBM i) and **C/C++** — reusing the same architecture: deterministic parsing and graph construction per language front-end, a shared IR, and the same generation/refactoring/validation stages.
3. **Additional modernization targets.** Generating **.NET** (C#/ASP.NET Core) alongside Java from the same IR and customer rule packs, so the target platform becomes a configuration choice rather than a separate product.

---

## References

[1] XMainframe: A Large Language Model for Mainframe Modernization. arXiv:2408.04660. https://arxiv.org/html/2408.04660v1

[2] ModernizeX internal documents — *WebApp Requirement Specification (COBOL Migration Platform), v1.0*; *Design & Traceability Strategy*; *ModernizeX Effort Evaluation v1.0* (measured per-stage effort, source profile, and verified outputs for the SAKURA-SMS migration).

[3] ProLeap / Koopa COBOL parsers; ANTLR. Open-source COBOL parsing tooling referenced by the analysis engine.

[4] Temporal — durable workflow orchestration for long-running, checkpointed pipelines.

[5] A. Irrera, "Banks scramble to fix old systems as IT 'cowboys' ride into sunset." *Reuters*, April 10, 2017. https://www.reuters.com/article/us-usa-banks-cobol-idUSKBN17C0D8 — Commonwealth Bank of Australia's core COBOL platform replacement (begun 2012) took five years and cost ≈US$749.9 million; an estimated 43% of U.S. banking systems and 220 billion lines of COBOL remain in use.

[6] Amazon Web Services, "AWS Mainframe Modernization — Pricing." https://aws.amazon.com/mainframe-modernization/pricing/ (accessed July 2026) — partner-run code conversion listed at US$2.75 per line of code; the price covers the conversion service only, with testing, review, integration, and runtime priced separately. Cited here solely as an external market price anchor.

[7] U.S. Government Accountability Office, "Information Technology: Federal Agencies Need to Address Aging Legacy Systems." GAO-16-468, May 2016. https://www.gao.gov/assets/gao-16-468.pdf — documents multi-billion-dollar operations-and-maintenance spend on legacy systems and the scarcity (and cost premium) of COBOL-skilled staff.

---

*Working paper — figures (architecture diagram, pipeline state machine, dependency-graph examples) to be inserted from `docs/design_docs/` SVG assets in the camera-ready version. Repository links for the generated SAKURA-SMS target project are to be added before publication.*
