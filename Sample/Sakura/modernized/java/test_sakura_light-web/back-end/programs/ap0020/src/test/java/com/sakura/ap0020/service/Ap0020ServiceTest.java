package com.sakura.ap0020.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.sakura.abortx.service.AbortxService;
import com.sakura.ap0020.domain.Ap0020FieldAccess;
import com.sakura.ap0020.runtime.Ap0020Datasets;
import com.sakura.dateut.service.DateutService;
import com.sakura.runtime.ScreenModels.ScreenDef;
import com.sakura.runtime.ScreenRendererInstance;
import com.sakura.runtime.io.AplfDataset;
import com.sakura.runtime.io.SuppfDataset;
import com.sakura.runtime.record.RuntimeFieldAccess;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for Ap0020Service, converted from COBOL program AP0020 (AP inquiry). Ground truth:
 * AP0020.cob MAIN-000 and its GET-KEY / PROCESS-SUPP / SCAN-LEDGER / SHOW-RESULT / LOAD-RECENT
 * paragraphs.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Timeout(value = 10, unit = TimeUnit.SECONDS)
class Ap0020ServiceTest {

    private static final int SUPPLIER_CODE = 100200;

    @Mock private DateutService dateutService;
    @Mock private AbortxService abortxService;
    @Mock private ScreenRendererInstance renderer;

    private AplfDataset aplf;
    private SuppfDataset suppf;
    private Ap0020Datasets fileSet;
    private Ap0020FieldAccess reader;
    private Ap0020Service service;

    private final List<String> screenInteractions = new ArrayList<>();
    private Ap0020FieldAccess capturedWs;

    private record LedgerLine(
            int supp,
            int date,
            int kind,
            BigDecimal debit,
            BigDecimal credit,
            BigDecimal balance) {}

    @BeforeEach
    void setUp() {
        aplf = spy(new AplfDataset());
        suppf = spy(new SuppfDataset());

        fileSet = spy(new Ap0020Datasets());
        doReturn(aplf).when(fileSet).getAplf();
        doReturn(suppf).when(fileSet).getSuppf();

        doNothing().when(aplf).open(any());
        doNothing().when(aplf).close();
        doReturn(false).when(aplf).isInvalidKey();

        doNothing().when(suppf).open(any());
        doNothing().when(suppf).close();
        doReturn(false).when(suppf).isInvalidKey();

        reader = new Ap0020FieldAccess(null, fileSet);

        doAnswer(
                        inv -> {
                            ScreenDef def = inv.getArgument(0);
                            RuntimeFieldAccess fields = inv.getArgument(1);
                            if (capturedWs == null) {
                                capturedWs = (Ap0020FieldAccess) fields;
                            }
                            // DS-MSG's text is overwritten by later paragraphs (e.g. the next
                            // GET-KEY round),
                            // so capture the message AT DISPLAY TIME, not from final state after
                            // execute().
                            String label = "displayScreen:" + def.name;
                            if ("DS-MSG".equals(def.name)) {
                                label += ":" + capturedWs.getWkMsgLine().trim();
                            }
                            screenInteractions.add(label);
                            return null;
                        })
                .when(renderer)
                .displayScreen(any(), any());

        service = new Ap0020Service(fileSet, dateutService, abortxService, renderer);
    }

    @AfterEach
    void tearDown() {
        screenInteractions.clear();
        capturedWs = null;
    }

    /** Stubs a successful START + a finite sequence of ledger lines, ending with AT END. */
    private void stubLedgerLines(List<LedgerLine> lines) {
        doReturn(true).when(aplf).start(anyString(), anyString());
        AtomicInteger idx = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            int i = idx.getAndIncrement();
                            if (i < lines.size()) {
                                LedgerLine l = lines.get(i);
                                reader.setPlSupp(l.supp());
                                reader.setPlDate(l.date());
                                reader.setPlKind(l.kind());
                                reader.setPlDebit(l.debit());
                                reader.setPlCredit(l.credit());
                                reader.setPlBalance(l.balance());
                            }
                            return null;
                        })
                .when(aplf)
                .readNext();
        doAnswer(inv -> idx.get() > lines.size()).when(aplf).isAtEnd();
    }

    private void stubSupplierFound(String name, long balance) {
        doReturn(true).when(suppf).readByKey(anyString());
        reader.setSpName(name);
        reader.setSpBalance(BigDecimal.valueOf(balance));
    }

    @Test
    void execute_supplierFoundWithFewLedgerLines_showsBalanceAndTotalsAndEndsOnPf3() {
        // Given: supplier 100200 exists with 3 ledger lines (Purch/Paymnt/Retn), ending via PF3
        doReturn("100200").when(renderer).acceptField(any());
        doReturn("00", "03").when(renderer).readEndStatus();
        stubSupplierFound("ACME SUPPLIES", 5000L);
        stubLedgerLines(
                List.of(
                        new LedgerLine(
                                SUPPLIER_CODE,
                                20260101,
                                1,
                                BigDecimal.valueOf(1000),
                                BigDecimal.ZERO,
                                BigDecimal.valueOf(4000)),
                        new LedgerLine(
                                SUPPLIER_CODE,
                                20260105,
                                2,
                                BigDecimal.ZERO,
                                BigDecimal.valueOf(500),
                                BigDecimal.valueOf(4500)),
                        new LedgerLine(
                                SUPPLIER_CODE,
                                20260110,
                                3,
                                BigDecimal.valueOf(200),
                                BigDecimal.ZERO,
                                BigDecimal.valueOf(4300))));

        // When
        assertDoesNotThrow(() -> service.execute());

        // Then
        assertThat(capturedWs.getWkCurBal()).isEqualTo(5000L);
        assertThat(capturedWs.getWkTotDr()).isEqualTo(1200L);
        assertThat(capturedWs.getWkTotCr()).isEqualTo(500L);
        assertThat(capturedWs.getWkLcnt()).isEqualTo(3);
        assertThat(capturedWs.getWkSuppName().trim()).isEqualTo("ACME SUPPLIES");
        assertThat(screenInteractions)
                .containsSubsequence(
                        "displayScreen:DS-HEADER", "displayScreen:DS-FOOTER",
                        "displayScreen:DS-KEY", "displayScreen:DS-VIEW");
        verify(aplf, times(1)).start("PL-SUPP", "NOT LESS THAN");
        verify(suppf, times(1)).readByKey(anyString());
    }

    @Test
    void execute_supplierCodeZero_showsErrorAndReturnsToKeyEntryWithoutReadingSupplier() {
        // Given: user enters 0 as supplier code (COBOL: "Supplier code must not be zero")
        doReturn("000000", "100200").when(renderer).acceptField(any());
        doReturn("00", "03").when(renderer).readEndStatus();

        // When
        assertDoesNotThrow(() -> service.execute());

        // Then
        assertThat(screenInteractions)
                .contains("displayScreen:DS-MSG:Supplier code must not be zero");
        verify(suppf, never()).readByKey(anyString());
        verify(aplf, never()).start(anyString(), anyString());
    }

    @Test
    void execute_supplierNotFound_showsErrorAndSkipsLedgerScan() {
        // Given: SUPPF READ INVALID KEY -> "Supplier not found", no SCAN-LEDGER
        doReturn("999999").when(renderer).acceptField(any());
        doReturn("00", "03").when(renderer).readEndStatus();
        doReturn(true).when(suppf).readByKey(anyString());
        doReturn(true).when(suppf).isInvalidKey();

        // When
        assertDoesNotThrow(() -> service.execute());

        // Then
        assertThat(screenInteractions).contains("displayScreen:DS-MSG:Supplier not found");
        verify(aplf, never()).start(anyString(), anyString());
    }

    @Test
    void execute_invalidFunctionKey_showsInvalidFunctionKeyMessageAndContinuesLoop() {
        // Given: ESTS neither "03" nor "00" on the first round (COBOL MAIN-RTN WHEN OTHER)
        doReturn("100200").when(renderer).acceptField(any());
        doReturn("P9", "03").when(renderer).readEndStatus();

        // When
        assertDoesNotThrow(() -> service.execute());

        // Then
        assertThat(screenInteractions).contains("displayScreen:DS-MSG:Invalid function key");
        verify(suppf, never()).readByKey(anyString());
    }

    @Test
    void execute_supplierWithNoLedgerLines_leavesRecentListBlank() {
        // Given: supplier found, START positions past EOF for this key immediately (SUPP-EOF)
        doReturn("100200").when(renderer).acceptField(any());
        doReturn("00", "03").when(renderer).readEndStatus();
        stubSupplierFound("EMPTY LEDGER CO", 0L);
        doReturn(true).when(aplf).start(anyString(), anyString());
        doReturn(true).when(aplf).isInvalidKey();

        // When
        assertDoesNotThrow(() -> service.execute());

        // Then
        assertThat(capturedWs.getWkLcnt()).isEqualTo(0);
        assertThat(capturedWs.getWkTotDr()).isEqualTo(0L);
        assertThat(capturedWs.getWkTotCr()).isEqualTo(0L);
        assertThat(capturedWs.getWkList(1).trim()).isEmpty();
        verify(aplf, never()).readNext();
    }

    @Test
    void execute_ledgerLineKindMapping_allFiveEvaluateBranchesLabeled() {
        // Given: 5 lines covering PL-KIND 1..4 and OTHER (COBOL KIND-LABEL EVALUATE)
        doReturn("100200").when(renderer).acceptField(any());
        doReturn("00", "03").when(renderer).readEndStatus();
        stubSupplierFound("KIND TEST CO", 100L);
        stubLedgerLines(
                List.of(
                        new LedgerLine(
                                SUPPLIER_CODE,
                                20260101,
                                1,
                                BigDecimal.ONE,
                                BigDecimal.ZERO,
                                BigDecimal.ZERO),
                        new LedgerLine(
                                SUPPLIER_CODE,
                                20260102,
                                2,
                                BigDecimal.ONE,
                                BigDecimal.ZERO,
                                BigDecimal.ZERO),
                        new LedgerLine(
                                SUPPLIER_CODE,
                                20260103,
                                3,
                                BigDecimal.ONE,
                                BigDecimal.ZERO,
                                BigDecimal.ZERO),
                        new LedgerLine(
                                SUPPLIER_CODE,
                                20260104,
                                4,
                                BigDecimal.ONE,
                                BigDecimal.ZERO,
                                BigDecimal.ZERO),
                        new LedgerLine(
                                SUPPLIER_CODE,
                                20260105,
                                9,
                                BigDecimal.ONE,
                                BigDecimal.ZERO,
                                BigDecimal.ZERO)));

        // When
        assertDoesNotThrow(() -> service.execute());

        // Then
        assertThat(capturedWs.getWkList(1)).contains("Purch ");
        assertThat(capturedWs.getWkList(2)).contains("Paymnt");
        assertThat(capturedWs.getWkList(3)).contains("Retn  ");
        assertThat(capturedWs.getWkList(4)).contains("Adjust");
        assertThat(capturedWs.getWkList(5)).contains("Other ");
    }

    @Test
    void execute_moreThan12LedgerLines_recentListShowsOnlyLastTwelve() {
        // Given: 15 matching lines; COBOL LOAD-RECENT keeps only the last 12 (start = LCNT-11)
        doReturn("100200").when(renderer).acceptField(any());
        doReturn("00", "03").when(renderer).readEndStatus();
        stubSupplierFound("BIG LEDGER CO", 999L);
        List<LedgerLine> lines = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            int kind = ((i - 1) % 4) + 1;
            lines.add(
                    new LedgerLine(
                            SUPPLIER_CODE,
                            20260100 + i,
                            kind,
                            BigDecimal.ONE,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO));
        }
        stubLedgerLines(lines);

        // When
        assertDoesNotThrow(() -> service.execute());

        // Then: displayed window is original lines 4..15 -> WK-LIST(1) is line 4 (kind 4 ->
        // Adjust),
        // WK-LIST(12) is line 15 (kind 3 -> Retn)
        assertThat(capturedWs.getWkLcnt()).isEqualTo(15);
        assertThat(capturedWs.getWkList(1)).contains("Adjust").doesNotContain("Purch ");
        assertThat(capturedWs.getWkList(12)).contains("Retn  ");
    }

    @Test
    void execute_moreThan500LedgerLines_totalsIncludeAllButRecentTableCapsAt500() {
        // Given: 501 matching lines; COBOL PROCESS-LINE adds to totals unconditionally but only
        // stores into WK-ALL while WK-LCNT < 500
        doReturn("100200").when(renderer).acceptField(any());
        doReturn("00", "03").when(renderer).readEndStatus();
        stubSupplierFound("HIGH VOLUME CO", 1L);
        List<LedgerLine> lines = new ArrayList<>();
        for (int i = 1; i <= 501; i++) {
            lines.add(
                    new LedgerLine(
                            SUPPLIER_CODE,
                            20260100 + i,
                            1,
                            BigDecimal.ONE,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO));
        }
        stubLedgerLines(lines);

        // When
        assertDoesNotThrow(() -> service.execute());

        // Then
        assertThat(capturedWs.getWkTotDr()).isEqualTo(501L);
        assertThat(capturedWs.getWkLcnt()).isEqualTo(500);
    }

    @Test
    void execute_differentSupplierEncounteredMidScan_stopsBeforeProcessingIt() {
        // Given: 2 lines for the key supplier, then a 3rd for a different supplier (PL-SUPP
        // mismatch)
        doReturn("100200").when(renderer).acceptField(any());
        doReturn("00", "03").when(renderer).readEndStatus();
        stubSupplierFound("MID SCAN CO", 10L);
        stubLedgerLines(
                List.of(
                        new LedgerLine(
                                SUPPLIER_CODE,
                                20260101,
                                1,
                                BigDecimal.TEN,
                                BigDecimal.ZERO,
                                BigDecimal.ZERO),
                        new LedgerLine(
                                SUPPLIER_CODE,
                                20260102,
                                1,
                                BigDecimal.TEN,
                                BigDecimal.ZERO,
                                BigDecimal.ZERO),
                        new LedgerLine(
                                SUPPLIER_CODE + 1,
                                20260103,
                                1,
                                BigDecimal.valueOf(999),
                                BigDecimal.ZERO,
                                BigDecimal.ZERO)));

        // When
        assertDoesNotThrow(() -> service.execute());

        // Then: only the first 2 lines belong to the key supplier
        assertThat(capturedWs.getWkLcnt()).isEqualTo(2);
        assertThat(capturedWs.getWkTotDr()).isEqualTo(20L);
    }

    @Test
    void execute_secondRoundAfterEnterOnResultView_continuesLoopThenEndsOnPf3() {
        // Given: supplier found, ENTER on the result view (COBOL: ESTS <> "03" keeps looping),
        // then a second GET-KEY round ends via PF3
        doReturn("100200", "100200").when(renderer).acceptField(any());
        doReturn("00", "00", "03").when(renderer).readEndStatus();
        stubSupplierFound("REPEAT CO", 42L);
        doReturn(true).when(aplf).start(anyString(), anyString());
        doReturn(true).when(aplf).isInvalidKey();

        // When
        assertDoesNotThrow(() -> service.execute());

        // Then: two full GET-KEY rounds happened, but the second round's ESTS is "03" so
        // MAIN-RTN takes the END-YES branch directly without re-entering PROCESS-SUPP
        verify(renderer, times(2)).acceptField(any());
        verify(suppf, times(1)).readByKey(anyString());
    }

    @Test
    void execute_aplfOpenFileStatusNotOk_abortsWithCompletionCode255() {
        // Given: OPEN INPUT APLF fails with a status other than "00"/"35"/"30" (COBOL ABEND-RTN)
        doReturn("05").when(aplf).getFileStatus();

        // When
        assertDoesNotThrow(() -> service.execute());

        // Then
        assertThat(service.getCompletionCode()).isEqualTo(255);
        verify(abortxService, times(1)).execute(any());
        verify(aplf, times(1)).open(any());
        verify(suppf, never()).open(any());
    }

    @Test
    void execute_aplfOpenFileStatus35_reopensAsOutputThenInputAndContinues() {
        // Given: OPEN INPUT APLF returns "35" (file not found) -> COBOL reopens OUTPUT then INPUT
        doReturn("35", "00", "00", "00").when(aplf).getFileStatus();
        doReturn("00").when(suppf).getFileStatus();
        doReturn("100200").when(renderer).acceptField(any());
        doReturn("03").when(renderer).readEndStatus();

        // When
        assertDoesNotThrow(() -> service.execute());

        // Then: APLF opened 3 times (INPUT, OUTPUT, INPUT reopen) and closed twice
        // (once during the reopen sequence, once at TERM-RTN); no abort
        verify(aplf, times(3)).open(any());
        verify(aplf, times(2)).close();
        verify(abortxService, never()).execute(any());
        verify(suppf, times(1)).open(any());
    }
}
