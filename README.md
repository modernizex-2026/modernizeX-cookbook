# ModernizeX — Release Documentation

**Baseline v1.0** · © 2026 EDX Organization · [Commercial License](LICENSE)

**ModernizeX** is an end-to-end platform that modernizes COBOL/CICS/JCL mainframe systems (DB2/SQL, DL/I, MQ, VSAM/QSAM) to Java (Spring Boot). It combines **deterministic static analysis** (AST, call graph, data flow) with **grounded LLM generation**: the codebase is fully analyzed first, and AI is then used to generate documentation and refactor code based on deterministically recovered facts — with full traceability from every Java component back to its original COBOL program, and support for fully **on-premise / air-gapped** deployment.

## Documents

| Document | English | 日本語 | Tiếng Việt |
|---|---|---|---|
| **Whitepaper** — architecture, seven-stage pipeline, SAKURA-SMS case study | [EN](ModernizeX_Whitepaper_v1.0.md) | [JA](ModernizeX_Whitepaper_v1.0_JA.md) | — |
| **Effort Evaluation** — measured migration effort on a real run | [EN](ModernizeX_Effort_Evaluation_v1.0.md) | [JA](ModernizeX_Effort_Evaluation_v1.0_JA.md) | — |
| **End-User Guide** — full walkthrough of the application UI | [EN](ModernizeX_EndUser_Guide_EN.md) | [JA](ModernizeX_EndUser_Guide_JA.md) | [VI](ModernizeX_EndUser_Guide_VI.md) |
| **Installation Guide** (macOS) | [EN](ModernizeX_Installation_Guide_EN.md) | [JA](ModernizeX_Installation_Guide_JA.md) | [VI](ModernizeX_Installation_Guide_VI.md) |
| **Installation video** (~64 s, EN narration) | [ModernizeX_Installation_Guide.mp4](ModernizeX_Installation_Guide.mp4) | | |
| **Pitch decks** | [Pitch/](Pitch/) | | |

All End-User Guide screenshots are captured directly from the live application (build `2.0-refactored`, guide v2.1).

## The Pipeline

```
Import Source → Assessment → AS-IS docs → TO-BE (Java conversion · refactor · UI)
             → Data Migration → Validation (Unit / Integration Tests) → Delivery
```

## Contact

For product licensing, trials, or an on-premise evaluation, contact **EDX Organization**.
