package com.sakura.rp0130.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.rp0130.runtime.Rp0130Datasets;
import com.sakura.runtime.Utility;
import com.sakura.runtime.io.ArlfDataset;
import com.sakura.runtime.io.CustfDataset;
import com.sakura.runtime.io.RepfDataset;
import com.sakura.runtime.io.SyscfDataset;
import com.sakura.runtime.linkage.AbortxLinkParm;
import com.sakura.runtime.linkage.DateutLinkParm;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for {@link Rp0130Service}, generated from COBOL program RP0130 (Customer statement of
 * account). Ground truth for every expected value is RP0130.cob's PROCEDURE DIVISION.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Timeout(value = 10, unit = TimeUnit.SECONDS)
class Rp0130ServiceTest {

    // fileSet is a real Rp0130Datasets (not a plain mock) so Rp0130FieldAccess can
    // register its real record buffers; individual dataset accessors are overridden
    // to return spies so I/O (open/close/read/start/write) can be stubbed per test.
    private Rp0130Datasets fileSet;
    private CustfDataset custf;
    private ArlfDataset arlf;
    private SyscfDataset syscf;
    private RepfDataset repf;

    @Mock private DateutService dateutService;

    @Mock private AbortxService abortxService;

    private final List<String> writtenLines = new ArrayList<>();

    private Rp0130Service service;

    @BeforeEach
    void setUp() {
        fileSet = spy(new Rp0130Datasets());
        custf = spy(fileSet.getCustf());
        arlf = spy(fileSet.getArlf());
        syscf = spy(fileSet.getSyscf());
        repf = spy(fileSet.getRepf());
        doReturn(custf).when(fileSet).getCustf();
        doReturn(arlf).when(fileSet).getArlf();
        doReturn(syscf).when(fileSet).getSyscf();
        doReturn(repf).when(fileSet).getRepf();

        doNothing().when(custf).open(any());
        doNothing().when(custf).close();
        doReturn("00").when(custf).getFileStatus();
        doReturn(false).when(custf).isInvalidKey();
        doReturn(true).when(custf).start(anyString(), anyString());

        doNothing().when(arlf).open(any());
        doNothing().when(arlf).close();
        doReturn("00").when(arlf).getFileStatus();
        doReturn(false).when(arlf).isInvalidKey();
        doReturn(true).when(arlf).start(anyString(), anyString());

        doNothing().when(syscf).open(any());
        doNothing().when(syscf).close();
        doReturn("00").when(syscf).getFileStatus();
        doReturn(false).when(syscf).isInvalidKey();
        doReturn(false).when(syscf).readByKey(any());

        doNothing().when(repf).open(any());
        doNothing().when(repf).close();
        doReturn("00").when(repf).getFileStatus();
        doAnswer(
                        inv -> {
                            writtenLines.add(repf.buffer().getString("REP-REC"));
                            return null;
                        })
                .when(repf)
                .write();

        // DATEUT TODY: fixed sysdate so tests are not tied to LocalDate.now().
        doAnswer(
                        inv -> {
                            DateutLinkParm p = inv.getArgument(0);
                            p.getKdate().setKdDate1(20260919);
                            return null;
                        })
                .when(dateutService)
                .execute(any());

        doNothing().when(abortxService).execute(any());

        service = new Rp0130Service(fileSet, dateutService, abortxService);
    }

    @AfterEach
    void tearDown() {
        writtenLines.clear();
    }

    /** Runs the program with the four console prompts (customer/date range) supplied in order. */
    private void runProgram(String custFrom, String custTo, String dateFrom, String dateTo) {
        try (MockedStatic<Utility> mockedUtility = mockStatic(Utility.class, CALLS_REAL_METHODS)) {
            mockedUtility
                    .when(Utility::readStdinLine)
                    .thenReturn(custFrom, custTo, dateFrom, dateTo);
            service.execute();
        }
    }

    private void runProgram() {
        runProgram("", "", "", "");
    }

    /** Stubs CUSTF's READ NEXT loop to hand back one canned customer record per call, then EOF. */
    private void stubCustomers(List<Runnable> loaders) {
        AtomicInteger idx = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            int i = idx.getAndIncrement();
                            if (i < loaders.size()) {
                                loaders.get(i).run();
                                return true;
                            }
                            return false;
                        })
                .when(custf)
                .readNext();
        doAnswer(inv -> idx.get() > loaders.size()).when(custf).isAtEnd();
    }

    /** Stubs ARLF's READ NEXT loop to hand back one canned ledger record per call, then EOF. */
    private void stubLedger(List<Runnable> loaders) {
        AtomicInteger idx = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            int i = idx.getAndIncrement();
                            if (i < loaders.size()) {
                                loaders.get(i).run();
                                return true;
                            }
                            return false;
                        })
                .when(arlf)
                .readNext();
        doAnswer(inv -> idx.get() > loaders.size()).when(arlf).isAtEnd();
    }

    private Runnable customer(int code, String name, int delFlag) {
        return () -> {
            custf.buffer().setInt("CU-CODE", code);
            custf.buffer().setString("CU-NAME", name);
            custf.buffer().setInt("CU-DEL-FLAG", delFlag);
        };
    }

    private Runnable ledger(
            int custCode,
            int date,
            String debit,
            String credit,
            int kind,
            long refNo,
            String remark) {
        return () -> {
            arlf.buffer().setInt("AL-CUST", custCode);
            arlf.buffer().setInt("AL-DATE", date);
            arlf.buffer().setDecimal("AL-DEBIT", new BigDecimal(debit));
            arlf.buffer().setDecimal("AL-CREDIT", new BigDecimal(credit));
            arlf.buffer().setInt("AL-KIND", kind);
            arlf.buffer().setLong("AL-REF-NO", refNo);
            arlf.buffer().setString("AL-REMARK", remark);
        };
    }

    // ── INIT-RTN ────────────────────────────────────────────────────────

    @Test
    void execute_syscfFound_usesCompanyNameFromSyscf() {
        doReturn(true).when(syscf).readByKey(any());
        doReturn(false).when(syscf).isInvalidKey();
        syscf.buffer().setString("SY-COMPANY-NAME", "ACME HOLDINGS CO LTD");
        stubCustomers(List.of());

        runProgram();

        assertThat(writtenLines).anyMatch(l -> l.contains("ACME HOLDINGS CO LTD"));
        assertThat(service.getCompletionCode()).isZero();
    }

    @Test
    void execute_syscfInvalidKey_keepsDefaultCompanyName() {
        doReturn(true).when(syscf).readByKey(any());
        doReturn(true).when(syscf).isInvalidKey();
        stubCustomers(List.of());

        runProgram();

        assertThat(writtenLines).anyMatch(l -> l.contains("SAKURA Sales Management System"));
    }

    @Test
    void execute_custfOpenFails_abortsWithCustfError() {
        doReturn("99").when(custf).getFileStatus();

        runProgram();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        var captor = org.mockito.ArgumentCaptor.forClass(AbortxLinkParm.class);
        verify(abortxService).execute(captor.capture());
        assertThat(captor.getValue().getKabend().getKaFile().trim()).isEqualTo("CUSTF");
    }

    @Test
    void execute_custfFileStatus35_setsMainEofAndPrintsNoStatements() {
        doReturn("35").when(custf).getFileStatus();

        runProgram();

        assertThat(service.getCompletionCode()).isZero();
        assertThat(writtenLines)
                .anyMatch(l -> l.contains("*** NO CUSTOMER STATEMENTS TO PRINT ***"));
        verify(custf, times(0)).readNext();
    }

    @Test
    void execute_repfOpenFails_abortsWithRepfError() {
        doReturn("99").when(repf).getFileStatus();
        stubCustomers(List.of());

        runProgram();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        var captor = org.mockito.ArgumentCaptor.forClass(AbortxLinkParm.class);
        verify(abortxService).execute(captor.capture());
        assertThat(captor.getValue().getKabend().getKaFile().trim()).isEqualTo("REPF");
    }

    // ── PARM-RTN ────────────────────────────────────────────────────────

    @Test
    void execute_parmInputsNumeric_parsedIntoCustomerLoop() {
        stubCustomers(List.of());

        runProgram("000100", "000200", "20260101", "20261231");

        // customer loop uses WK-CUST-FROM parsed from stdin as the START key.
        verify(custf)
                .start(
                        org.mockito.ArgumentMatchers.eq("CU-CODE"),
                        org.mockito.ArgumentMatchers.eq("NOT LESS THAN"));
        assertThat(custf.buffer().getInt("CU-CODE")).isEqualTo(100);
    }

    @Test
    void execute_parmInputsBlank_defaultsCustFromToZero() {
        stubCustomers(List.of());

        runProgram("", "", "", "");

        assertThat(custf.buffer().getInt("CU-CODE")).isZero();
    }

    // ── PRINT-RTN / READ-CUST ──────────────────────────────────────────

    @Test
    void execute_oneCustomerOneLedgerLineInPeriod_printsOpeningLedgerAndClosing() {
        stubCustomers(List.of(customer(100, "ACME CORP", 0)));
        stubLedger(List.of(ledger(100, 20260615, "50000", "0", 1, 12345L, "INVOICE 1")));

        runProgram("", "", "20260101", "20261231");

        assertThat(writtenLines).anyMatch(l -> l.contains("CUSTOMER:") && l.contains("ACME CORP"));
        assertThat(writtenLines).anyMatch(l -> l.contains("OPENING BALANCE"));
        assertThat(writtenLines).anyMatch(l -> l.contains("SALE"));
        assertThat(writtenLines).anyMatch(l -> l.contains("CLOSING BALANCE"));
    }

    @Test
    void execute_customerSoftDeleted_skipsStatement() {
        stubCustomers(List.of(customer(100, "DELETED CO", 1)));

        runProgram();

        assertThat(writtenLines).noneMatch(l -> l.contains("DELETED CO"));
        assertThat(writtenLines)
                .anyMatch(l -> l.contains("*** NO CUSTOMER STATEMENTS TO PRINT ***"));
    }

    @Test
    void execute_customerCodeAboveRange_stopsCustomerLoop() {
        // second customer (code 999) is beyond WK-CUST-TO=200: RDC-010 sets WK-MAIN-EOF
        // and the third canned customer must never be read.
        stubCustomers(List.of(customer(100, "IN RANGE", 0), customer(999, "OUT OF RANGE", 0)));
        stubLedger(List.of(ledger(100, 20260615, "5000", "0", 1, 1L, "R1")));

        runProgram("000000", "000200", "20260101", "20261231");

        assertThat(writtenLines).anyMatch(l -> l.contains("IN RANGE"));
        assertThat(writtenLines).noneMatch(l -> l.contains("OUT OF RANGE"));
    }

    // ── SCAN-CUST / CLASSIFY-LINE ───────────────────────────────────────

    @Test
    void execute_ledgerEntryBeforePeriod_accumulatesIntoOpeningBalanceOnlyNoLine() {
        stubCustomers(List.of(customer(100, "ACME CORP", 0)));
        stubLedger(List.of(ledger(100, 20250101, "30000", "0", 1, 1L, "OLD INVOICE")));

        runProgram("", "", "20260101", "20261231");

        // Entry dated before WK-DATE-FROM: no ledger detail line, but opening balance
        // is nonzero so FINISH-CUST prints opening + no-activity + closing.
        assertThat(writtenLines).anyMatch(l -> l.contains("OPENING BALANCE"));
        assertThat(writtenLines).anyMatch(l -> l.contains("(no ledger activity in this period)"));
        assertThat(writtenLines).noneMatch(l -> l.contains("OLD INVOICE"));
    }

    @Test
    void execute_ledgerEntryAfterPeriod_stopsReadingFurtherEntries() {
        stubCustomers(List.of(customer(100, "ACME CORP", 0)));
        stubLedger(
                List.of(
                        ledger(100, 20260615, "10000", "0", 1, 1L, "IN PERIOD"),
                        ledger(100, 20270101, "99999", "0", 1, 2L, "TOO LATE"),
                        ledger(100, 20260101, "11111", "0", 1, 3L, "NEVER READ")));

        runProgram("", "", "20260101", "20261231");

        assertThat(writtenLines).anyMatch(l -> l.contains("IN PERIOD"));
        assertThat(writtenLines).noneMatch(l -> l.contains("TOO LATE"));
        assertThat(writtenLines).noneMatch(l -> l.contains("NEVER READ"));
        // AL-DATE > WK-DATE-TO sets WK-ARL-EOF=1 directly — the third canned entry
        // (which would still be within a physical file's next-record order) is skipped.
        verify(arlf, times(2)).readNext();
    }

    @Test
    void execute_ledgerReachesNextCustomer_stopsScanWithoutPhysicalEof() {
        // ARLF READ NEXT succeeds (not at end) but AL-CUST no longer matches CU-CODE —
        // SCR-010's "else" branch (distinct from the AT END branch).
        stubCustomers(List.of(customer(100, "ACME CORP", 0)));
        doAnswer(
                        inv -> {
                            arlf.buffer().setInt("AL-CUST", 999);
                            arlf.buffer().setInt("AL-DATE", 20260101);
                            arlf.buffer().setDecimal("AL-DEBIT", BigDecimal.ZERO);
                            arlf.buffer().setDecimal("AL-CREDIT", BigDecimal.ZERO);
                            return true;
                        })
                .when(arlf)
                .readNext();
        doReturn(false).when(arlf).isAtEnd();

        runProgram();

        assertThat(writtenLines)
                .anyMatch(l -> l.contains("*** NO CUSTOMER STATEMENTS TO PRINT ***"));
        verify(arlf, times(1)).readNext();
    }

    @Test
    void execute_kindCodes_resolveToCorrectLabels() {
        stubCustomers(List.of(customer(100, "ACME CORP", 0)));
        stubLedger(
                List.of(
                        ledger(100, 20260601, "1000", "0", 1, 1L, "R1"),
                        ledger(100, 20260602, "1000", "0", 2, 2L, "R2"),
                        ledger(100, 20260603, "1000", "0", 3, 3L, "R3"),
                        ledger(100, 20260604, "1000", "0", 4, 4L, "R4"),
                        ledger(100, 20260605, "1000", "0", 7, 5L, "R5")));

        runProgram("", "", "20260101", "20261231");

        assertThat(writtenLines).anyMatch(l -> l.contains("SALE"));
        assertThat(writtenLines).anyMatch(l -> l.contains("RECEIPT"));
        assertThat(writtenLines).anyMatch(l -> l.contains("RETURN"));
        assertThat(writtenLines).anyMatch(l -> l.contains("ADJUST"));
        assertThat(writtenLines).anyMatch(l -> l.contains("OTHER"));
    }

    // ── FINISH-CUST ─────────────────────────────────────────────────────

    @Test
    void execute_noLedgerActivityZeroOpening_printsNothingForCustomer() {
        stubCustomers(List.of(customer(100, "ACME CORP", 0)));
        stubLedger(List.of());

        runProgram();

        assertThat(writtenLines).noneMatch(l -> l.contains("ACME CORP"));
        assertThat(writtenLines)
                .anyMatch(l -> l.contains("*** NO CUSTOMER STATEMENTS TO PRINT ***"));
    }

    @Test
    void execute_noCustomersMatchRange_printsNoStatementsMessage() {
        stubCustomers(List.of());

        runProgram();

        assertThat(writtenLines)
                .anyMatch(l -> l.contains("*** NO CUSTOMER STATEMENTS TO PRINT ***"));
    }

    // ── CHECK-PAGE / PAGE-HEAD ──────────────────────────────────────────

    @Test
    void execute_pageOverflow_reprintsHeaderMidStatement() {
        stubCustomers(List.of(customer(100, "ACME CORP", 0)));
        List<Runnable> lines = new ArrayList<>();
        for (int i = 0; i < 47; i++) {
            lines.add(ledger(100, 20260601 + i, "1000", "0", 1, i + 1L, "R" + i));
        }
        stubLedger(lines);

        runProgram("", "", "20260101", "20261231");

        long headerCount = writtenLines.stream().filter(l -> l.contains("RUN DATE:")).count();
        assertThat(headerCount).isEqualTo(2);
    }

    // ── TERM-RTN ────────────────────────────────────────────────────────

    @Test
    void execute_completesNormally_closesFilesAndSetsCompletionCodeZero() {
        stubCustomers(List.of());

        runProgram();

        verify(custf).close();
        verify(arlf).close();
        verify(repf).close();
        assertThat(service.getCompletionCode()).isZero();
    }

    // ── CONVERT-GAP: converted layout / MOVE semantics disagree with COBOL ──

    @Test
    void execute_ruleLine_shouldBe106DashesPerCobol() {
        // CONVERT-GAP: COBOL WK-RULE-equivalent RPT-RULE is "01 RPT-RULE PIC X(106)
        // VALUE ALL "-"." — 106 dashes. The generated RP0130_WS.xml carries
        // value="ALL&quot;-&quot;" verbatim with no runtime expansion of the ALL
        // figurative constant (no "ALL" handling anywhere under
        // com.sakura.runtime.record), so the field's initial content is the literal
        // text ALL"-" padded with spaces instead of 106 dashes. Every RPT-RULE line
        // the report prints inherits this wrong content.
        stubCustomers(List.of(customer(100, "ACME CORP", 0)));
        stubLedger(List.of(ledger(100, 20260601, "1000", "0", 1, 1L, "R1")));

        runProgram("", "", "20260101", "20261231");

        String ruleLine =
                writtenLines.stream()
                        .filter(l -> l.startsWith("-") || l.contains("ALL"))
                        .findFirst()
                        .orElse("");
        assertThat(ruleLine).startsWith("-".repeat(106));
    }

    @Test
    void execute_zeroDebitLedgerLine_shouldBeBlankPerCobol() {
        // CONVERT-GAP: COBOL RL-DEBIT is "PIC ---,---,---,--9 BLANK WHEN ZERO" — a
        // zero debit renders as all spaces. The generated RP0130_WS.xml field for
        // RL-DEBIT carries editFormat="SIGN_SUPPRESS" with no blank-when-zero flag,
        // so a zero amount renders as an edited "0" instead of blanks.
        stubCustomers(List.of(customer(100, "ACME CORP", 0)));
        stubLedger(List.of(ledger(100, 20260601, "0", "5000", 2, 1L, "RECEIPT ONLY")));

        runProgram("", "", "20260101", "20261231");

        String ledgerLine =
                writtenLines.stream()
                        .filter(l -> l.contains("RECEIPT ONLY"))
                        .findFirst()
                        .orElse("");
        assertThat(ledgerLine).isNotEmpty();
        // RL-DEBIT occupies columns 60-74 (1-based) of REP-REC per RD-LINE's layout.
        String debitColumn = ledgerLine.substring(59, 74);
        assertThat(debitColumn).isBlank();
    }

    @Test
    void execute_shorterHeaderMove_shouldClearTrailingBytesPerCobol() {
        // CONVERT-GAP: COBOL "MOVE RPT-H2 TO REP-REC" space-fills the whole 132-byte
        // REP-REC (RPT-H2 is 95 bytes); RuntimeFieldAccess.copyBytes only overwrites
        // the source's byte range (toBuf.writeBytes(toName, fromBuf.sliceBytes(...)))
        // and never clears the receiving field's remaining bytes, so the prior RPT-H1
        // write's trailing "PAGE: nnnn" (columns 101-110) survives into the RUN DATE
        // line's tail.
        stubCustomers(List.of(customer(100, "ACME CORP", 0)));
        stubLedger(List.of(ledger(100, 20260601, "1000", "0", 1, 1L, "R1")));

        runProgram("", "", "20260101", "20261231");

        String runDateLine =
                writtenLines.stream().filter(l -> l.contains("RUN DATE:")).findFirst().orElse("");
        assertThat(runDateLine).isNotEmpty();
        assertThat(runDateLine).doesNotContain("PAGE:");
    }
}
