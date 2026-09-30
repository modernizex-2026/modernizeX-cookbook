package com.sakura.ar0020.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sakura.abortx.service.AbortxService;
import com.sakura.ar0020.domain.Ar0020FieldAccess;
import com.sakura.ar0020.runtime.Ar0020Datasets;
import com.sakura.dateut.service.DateutService;
import com.sakura.runtime.ScreenModels.ScreenDef;
import com.sakura.runtime.ScreenRendererInstance;
import com.sakura.runtime.io.ArlfDataset;
import com.sakura.runtime.io.CustfDataset;
import com.sakura.runtime.record.RuntimeFieldAccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for Ar0020Service (COBOL AR0020 — AR inquiry / aging), generated from {@code
 * AR0020.cob} PROCEDURE DIVISION as ground truth.
 *
 * <p>Test strategy: ARLF/CUSTF are real dataset objects wrapped with {@code spy()} so the record
 * buffer (and therefore {@code Ar0020FieldAccess}) works exactly as in production; only I/O methods
 * (open/close/start/readNext/isAtEnd/readByKey/ isInvalidKey/getFileStatus) are stubbed so no real
 * file/DB access happens. DATEUT runs for real (pure calendar math, safe and exactly matches the
 * COBOL copy) so aging bucket boundaries are computed against the live "today" via LocalDate.now(),
 * matching AGE-LINE's DIFF(AL-DATE, WK-SYSDATE) exactly. ABORTX is mocked (irrelevant file I/O).
 *
 * <p>ARLF ledger rows are fed to {@code readNext()} from the mutable {@link #ledgerRows} queue via
 * a {@code doAnswer} that writes each row into the buffer and flips {@link #arlfAtEnd}; {@code
 * start()} flips {@link #arlfStartInvalid} for START INVALID KEY scenarios.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Timeout(value = 10, unit = TimeUnit.SECONDS)
class Ar0020ServiceTest {

    private record LedgerRow(
            int cust,
            int date,
            BigDecimal debit,
            BigDecimal credit,
            BigDecimal balance,
            int kind) {}

    @Mock private ScreenRendererInstance renderer;
    @Mock private AbortxService abortxService;

    private DateutService dateutService;

    private Ar0020Datasets fileSet;
    private ArlfDataset arlf;
    private CustfDataset custf;

    private Ar0020Service service;

    private final Map<String, String> acceptValues = new HashMap<>();
    private final List<String> screenInteractions = new ArrayList<>();
    private final List<LedgerRow> ledgerRows = new ArrayList<>();
    private final AtomicInteger ledgerIndex = new AtomicInteger(0);
    private final AtomicBoolean arlfAtEnd = new AtomicBoolean(false);
    private final AtomicBoolean arlfStartInvalid = new AtomicBoolean(false);

    private static int ymd(LocalDate d) {
        return d.getYear() * 10000 + d.getMonthValue() * 100 + d.getDayOfMonth();
    }

    private static final LocalDate TODAY = LocalDate.now();

    @BeforeEach
    void setUp() {
        dateutService = new DateutService();

        Ar0020Datasets real = new Ar0020Datasets();
        fileSet = spy(real);
        arlf = spy(real.getArlf());
        custf = spy(real.getCustf());
        doReturn(arlf).when(fileSet).getArlf();
        doReturn(custf).when(fileSet).getCustf();

        doNothing().when(arlf).open(any());
        doNothing().when(custf).open(any());
        doNothing().when(arlf).close();
        doNothing().when(custf).close();
        doReturn("00").when(arlf).getFileStatus();
        doReturn("00").when(custf).getFileStatus();

        doAnswer(inv -> arlfStartInvalid.get()).when(arlf).isInvalidKey();
        doReturn(true).when(arlf).start(any(), any());
        doAnswer(
                        inv -> {
                            int idx = ledgerIndex.getAndIncrement();
                            if (idx < ledgerRows.size()) {
                                LedgerRow r = ledgerRows.get(idx);
                                arlf.getRecord().setInt("AL-CUST", r.cust());
                                arlf.getRecord().setInt("AL-DATE", r.date());
                                arlf.getRecord().setDecimal("AL-DEBIT", r.debit());
                                arlf.getRecord().setDecimal("AL-CREDIT", r.credit());
                                arlf.getRecord().setDecimal("AL-BALANCE", r.balance());
                                arlf.getRecord().setInt("AL-KIND", r.kind());
                                arlfAtEnd.set(false);
                                return true;
                            }
                            arlfAtEnd.set(true);
                            return false;
                        })
                .when(arlf)
                .readNext();
        doAnswer(inv -> arlfAtEnd.get()).when(arlf).isAtEnd();

        // Default: customer found, name/balance/credit-limit set per test.
        doReturn(true).when(custf).readByKey(any());
        doReturn(false).when(custf).isInvalidKey();
        custf.getRecord().setString("CU-NAME", "Acme Corp");
        custf.getRecord().setDecimal("CU-BALANCE", new BigDecimal("1000"));
        custf.getRecord().setDecimal("CU-CREDIT-LIMIT", new BigDecimal("5000"));

        acceptValues.put("WK-KEY-CUST", "100");
        when(renderer.acceptField(any()))
                .thenAnswer(
                        inv -> {
                            var field =
                                    inv.getArgument(
                                            0, com.sakura.runtime.ScreenModels.InputFieldDef.class);
                            return acceptValues.getOrDefault(field.name, "");
                        });

        doAnswer(
                        inv -> {
                            ScreenDef def = inv.getArgument(0);
                            // WK-MSG-LINE is reset to "Enter customer code then ENTER" at the START
                            // of
                            // the NEXT GET-KEY cycle (GKEY-010), before that cycle's ESTS is even
                            // read.
                            // For messages set on a cycle that loops back around (no PF3 in the
                            // same
                            // cycle) the final post-execute() ws state no longer holds them, so the
                            // message is snapshotted here at the moment DS-MSG is actually
                            // displayed.
                            if ("DS-MSG".equals(def.name)) {
                                Ar0020FieldAccess fa = (Ar0020FieldAccess) inv.getArgument(1);
                                screenInteractions.add(
                                        "displayScreen:DS-MSG:" + fa.getWkMsgLine().trim());
                            } else {
                                screenInteractions.add("displayScreen:" + def.name);
                            }
                            return null;
                        })
                .when(renderer)
                .displayScreen(any(), any());

        service = new Ar0020Service(fileSet, dateutService, abortxService, renderer);
    }

    /**
     * Live handle onto the service's WorkingStorage/FD field accessor, obtained from the first
     * displayScreen() call. It is the SAME mutable object throughout the run, so end-of-run state
     * is visible directly on it.
     */
    private Ar0020FieldAccess capturedWs() {
        ArgumentCaptor<RuntimeFieldAccess> captor =
                ArgumentCaptor.forClass(RuntimeFieldAccess.class);
        verify(renderer, org.mockito.Mockito.atLeastOnce()).displayScreen(any(), captor.capture());
        return (Ar0020FieldAccess) captor.getAllValues().get(0);
    }

    private void addLedgerLine(
            int cust,
            LocalDate date,
            BigDecimal debit,
            BigDecimal credit,
            BigDecimal balance,
            int kind) {
        ledgerRows.add(new LedgerRow(cust, ymd(date), debit, credit, balance, kind));
    }

    // ───────────────────────── happy path ─────────────────────────

    @Test
    void execute_pf3AtFirstPrompt_endsImmediatelyWithoutInquiry() {
        when(renderer.readEndStatus()).thenReturn("03");

        service.execute();

        verify(custf, never()).readByKey(any());
        verify(arlf, times(1)).open(any());
        verify(arlf, times(1)).close();
        verify(custf, times(1)).open(any());
        verify(custf, times(1)).close();
        assertThat(service.getCompletionCode()).isEqualTo(0);
        assertThat(screenInteractions)
                .contains("displayScreen:DS-HEADER", "displayScreen:DS-FOOTER");
    }

    @Test
    void execute_customerFoundWithLedgerAcrossAllAgingBuckets_computesBucketsAndLastLineFields() {
        addLedgerLine(
                100,
                TODAY.minusDays(10),
                new BigDecimal("100"),
                BigDecimal.ZERO,
                new BigDecimal("900"),
                1);
        addLedgerLine(
                100,
                TODAY.minusDays(45),
                BigDecimal.ZERO,
                new BigDecimal("50"),
                new BigDecimal("850"),
                2);
        addLedgerLine(
                100,
                TODAY.minusDays(75),
                new BigDecimal("30"),
                BigDecimal.ZERO,
                new BigDecimal("880"),
                3);
        addLedgerLine(
                100,
                TODAY.minusDays(100),
                new BigDecimal("20"),
                BigDecimal.ZERO,
                new BigDecimal("900"),
                4);
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        Ar0020FieldAccess ws = capturedWs();
        assertThat(ws.getWkB030()).isEqualTo(100L);
        assertThat(ws.getWkB060()).isEqualTo(-50L);
        assertThat(ws.getWkB090()).isEqualTo(30L);
        assertThat(ws.getWkB90p()).isEqualTo(20L);
        assertThat(ws.getWkTotDr()).isEqualTo(150L);
        assertThat(ws.getWkTotCr()).isEqualTo(50L);
        assertThat(ws.getWkLcnt()).isEqualTo(4);
        // last line processed is the 4th (kind=4 -> "Adjust")
        assertThat(ws.getWkDlKind()).isEqualTo("Adjust");
        assertThat(ws.getWkDlDr()).isEqualTo(20L);
        assertThat(ws.getWkDlBal()).isEqualTo(900L);
        assertThat(ws.getWkMsgLine().trim()).isEqualTo("Ledger scanned - press any key");
        assertThat(screenInteractions).contains("displayScreen:DS-AGE");
    }

    @Test
    void execute_customerFoundNoLedgerEntries_showsZeroBucketsAndDefaultWkStart() {
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        Ar0020FieldAccess ws = capturedWs();
        assertThat(ws.getWkLcnt()).isEqualTo(0);
        assertThat(ws.getWkB030()).isEqualTo(0L);
        assertThat(ws.getWkB060()).isEqualTo(0L);
        assertThat(ws.getWkB090()).isEqualTo(0L);
        assertThat(ws.getWkB90p()).isEqualTo(0L);
        // LR-010: WK-LCNT = 0 returns before WK-START is ever set.
        assertThat(ws.getWkStart()).isEqualTo(0);
    }

    @Test
    void execute_customerOverCreditLimit_showsOverLimitStatus() {
        custf.getRecord().setDecimal("CU-BALANCE", new BigDecimal("6000"));
        custf.getRecord().setDecimal("CU-CREDIT-LIMIT", new BigDecimal("5000"));
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        Ar0020FieldAccess ws = capturedWs();
        assertThat(ws.getWkOver()).isEqualTo("OVER LIMIT");
        assertThat(ws.getWkCurBal()).isEqualTo(6000L);
        assertThat(ws.getWkCrLimit()).isEqualTo(5000L);
    }

    @Test
    void execute_customerCreditLimitZero_notOverLimitEvenWithPositiveBalance() {
        custf.getRecord().setDecimal("CU-BALANCE", new BigDecimal("999999"));
        custf.getRecord().setDecimal("CU-CREDIT-LIMIT", BigDecimal.ZERO);
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        assertThat(capturedWs().getWkOver().trim()).isEqualTo("OK");
    }

    @Test
    void execute_moreThanEightLedgerLines_windowsStartToLastEightRows() {
        for (int i = 0; i < 10; i++) {
            addLedgerLine(
                    100,
                    TODAY.minusDays(1),
                    new BigDecimal("10"),
                    BigDecimal.ZERO,
                    new BigDecimal("10"),
                    1);
        }
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        Ar0020FieldAccess ws = capturedWs();
        assertThat(ws.getWkLcnt()).isEqualTo(10);
        // LR-010: WK-LCNT(10) > 8 -> WK-START = WK-LCNT - 7 = 3
        assertThat(ws.getWkStart()).isEqualTo(3);
        assertThat(ws.getWkRow()).isEqualTo(8);
    }

    @Test
    void execute_ledgerFirstRecordCustomerMismatch_stopsScanImmediatelyWithZeroLines() {
        addLedgerLine(
                999,
                TODAY.minusDays(5),
                new BigDecimal("10"),
                BigDecimal.ZERO,
                new BigDecimal("10"),
                1);
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        assertThat(capturedWs().getWkLcnt()).isEqualTo(0);
    }

    @Test
    void execute_startInvalidKey_noLedgerLinesFound() {
        arlfStartInvalid.set(true);
        addLedgerLine(
                100,
                TODAY.minusDays(5),
                new BigDecimal("10"),
                BigDecimal.ZERO,
                new BigDecimal("10"),
                1);
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        verify(arlf, never()).readNext();
        assertThat(capturedWs().getWkLcnt()).isEqualTo(0);
    }

    // ───────────────────────── edge cases (ground truth: MAIN-RTN / PROCESS-CUST / KIND-LABEL)
    // ─────────────────────────

    @Test
    void execute_customerCodeZero_rejectsWithRequiredMessageWithoutReadingCustf() {
        acceptValues.put("WK-KEY-CUST", "0");
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        verify(custf, never()).readByKey(any());
        assertThat(screenInteractions)
                .contains("displayScreen:DS-MSG:Customer code must not be zero");
    }

    @Test
    void execute_customerNotFound_rejectsWithNotFoundMessage() {
        doReturn(true).when(custf).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        verify(arlf, never()).start(any(), any());
        assertThat(screenInteractions).contains("displayScreen:DS-MSG:Customer not found");
    }

    @Test
    void execute_invalidFunctionKey_showsInvalidKeyMessageAndContinuesLoop() {
        when(renderer.readEndStatus()).thenReturn("99", "03");

        service.execute();

        verify(custf, never()).readByKey(any());
        assertThat(screenInteractions).contains("displayScreen:DS-MSG:Invalid function key");
    }

    @Test
    void execute_kindLabelUnknownValue_mapsToOtherLabel() {
        addLedgerLine(
                100,
                TODAY.minusDays(5),
                new BigDecimal("10"),
                BigDecimal.ZERO,
                new BigDecimal("10"),
                9);
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        assertThat(capturedWs().getWkDlKind()).isEqualTo("Other ");
    }

    @Test
    void execute_agingBoundaryDays_assignsToCorrectBucketAtEachEdge() {
        addLedgerLine(
                100,
                TODAY.minusDays(30),
                new BigDecimal("1"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                1); // <=30 -> B030
        addLedgerLine(
                100,
                TODAY.minusDays(31),
                new BigDecimal("2"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                1); // <=60 -> B060
        addLedgerLine(
                100,
                TODAY.minusDays(60),
                new BigDecimal("4"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                1); // <=60 -> B060
        addLedgerLine(
                100,
                TODAY.minusDays(61),
                new BigDecimal("8"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                1); // <=90 -> B090
        addLedgerLine(
                100,
                TODAY.minusDays(90),
                new BigDecimal("16"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                1); // <=90 -> B090
        addLedgerLine(
                100,
                TODAY.minusDays(91),
                new BigDecimal("32"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                1); // >90 -> B90P
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        Ar0020FieldAccess ws = capturedWs();
        assertThat(ws.getWkB030()).isEqualTo(1L);
        assertThat(ws.getWkB060()).isEqualTo(6L);
        assertThat(ws.getWkB090()).isEqualTo(24L);
        assertThat(ws.getWkB90p()).isEqualTo(32L);
    }

    @Test
    void execute_futureLedgerDate_clampsNegativeDaysToZeroInBucket030() {
        addLedgerLine(
                100, TODAY.plusDays(5), new BigDecimal("7"), BigDecimal.ZERO, BigDecimal.ZERO, 1);
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        // AGL-010: WK-DAYS < 0 is clamped to 0, so a future ledger date lands in
        // the 0-30 bucket rather than causing a negative-day (unbucketed) result.
        assertThat(capturedWs().getWkB030()).isEqualTo(7L);
    }

    // ───────────────────────── file-open lifecycle (ground truth: OPEN-FILES / ABEND-RTN)
    // ─────────────────────────

    @Test
    void execute_arlfOpenFailsPersistently_abortsWithCompletionCode255() {
        doReturn("99").when(arlf).getFileStatus();
        when(renderer.readEndStatus()).thenReturn("03");

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        verify(abortxService, times(1)).execute(any());
        verify(custf, never()).open(any());
    }

    @Test
    void execute_arlfOpenStatus35_reopensAsOutputThenReopensAsInput() {
        when(arlf.getFileStatus()).thenReturn("35", "00");
        when(renderer.readEndStatus()).thenReturn("03");

        service.execute();

        verify(arlf, times(3)).open(any());
        verify(custf, times(1)).open(any());
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }

    @Test
    void execute_custfOpenStatus35_reopensAsOutputThenReopensAsInput() {
        when(custf.getFileStatus()).thenReturn("35", "00");
        when(renderer.readEndStatus()).thenReturn("03");

        service.execute();

        verify(custf, times(3)).open(any());
        verify(custf, times(1)).open(com.sakura.runtime.DatasetEnums.FileOpenMode.OUTPUT);
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }

    @Test
    void execute_unexpectedRuntimeExceptionDuringProcessing_wrapsAndSetsCompletionCode12() {
        doReturn(true).when(custf).readByKey(any());
        doThrow(new RuntimeException("simulated DB failure")).when(custf).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "03");

        assertThrows(RuntimeException.class, () -> service.execute());

        assertThat(service.getCompletionCode()).isEqualTo(12);
    }
}
