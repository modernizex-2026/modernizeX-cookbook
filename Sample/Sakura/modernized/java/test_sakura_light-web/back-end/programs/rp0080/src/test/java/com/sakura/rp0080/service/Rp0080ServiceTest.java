package com.sakura.rp0080.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.rp0080.domain.Rp0080FieldAccess;
import com.sakura.rp0080.domain.WorkingStorage;
import com.sakura.rp0080.runtime.Rp0080Datasets;
import com.sakura.runtime.DatasetEnums.FileOpenMode;
import com.sakura.runtime.io.AplfDataset;
import com.sakura.runtime.io.RepfDataset;
import com.sakura.runtime.io.SuppfDataset;
import com.sakura.runtime.io.SyscfDataset;
import com.sakura.runtime.linkage.AbortxLinkParm;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for Rp0080Service (COBOL RP0080 — Accounts-payable balance list). Ground truth:
 * RP0080.cob (MAIN-000 / INIT-RTN / PRINT-RTN / READ-NEXT / PROCESS-ONE / RESET-ACCUM / FLUSH-SUPP
 * / CHECK-PAGE / PAGE-HEAD / PRINT-GRAND / TERM-RTN / ABEND-RTN).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class Rp0080ServiceTest {

    private static final String NO_PAYABLES_MSG = "*** NO PAYABLES ON FILE ***";

    @Mock private Rp0080Datasets fileSet;
    @Mock private DateutService dateutService;
    @Mock private AbortxService abortxService;

    private AplfDataset aplf;
    private SuppfDataset suppf;
    private SyscfDataset syscf;
    private RepfDataset repf;

    /**
     * Second field-access view over the SAME dataset buffers, used only to inject fake record data.
     */
    private Rp0080FieldAccess fixture;

    private final List<String> repLines = new ArrayList<>();
    private final Map<Integer, String> supplierNames = new HashMap<>();
    private boolean suppfInvalidKey;
    private boolean syscfInvalidKey = true;

    private Rp0080Service service;

    @BeforeEach
    void setUp() {
        aplf = spy(new AplfDataset());
        suppf = spy(new SuppfDataset());
        syscf = spy(new SyscfDataset());
        repf = spy(new RepfDataset());

        doReturn(aplf).when(fileSet).getAplf();
        doReturn(suppf).when(fileSet).getSuppf();
        doReturn(syscf).when(fileSet).getSyscf();
        doReturn(repf).when(fileSet).getRepf();

        // Default: every open/close succeeds with FSTS "00" and every I/O op is a no-op
        // unless a test overrides it below.
        doNothing().when(aplf).open(any());
        doNothing().when(suppf).open(any());
        doNothing().when(syscf).open(any());
        doNothing().when(repf).open(any());
        doReturn("00").when(aplf).getFileStatus();
        doReturn("00").when(suppf).getFileStatus();
        doReturn("00").when(syscf).getFileStatus();
        doReturn("00").when(repf).getFileStatus();
        doNothing().when(aplf).close();
        doNothing().when(suppf).close();
        doNothing().when(syscf).close();
        doNothing().when(repf).close();
        doReturn(false).when(aplf).isInvalidKey();
        doReturn(true).when(aplf).isAtEnd();
        doReturn(true).when(aplf).readNext();
        doReturn(true).when(aplf).start(anyString(), anyString());
        doReturn(true).when(syscf).readByKey(any());

        doAnswer(
                        inv -> {
                            repLines.add(repf.buffer().getString("REP-REC"));
                            return null;
                        })
                .when(repf)
                .write();

        doAnswer(inv -> syscfInvalidKey).when(syscf).isInvalidKey();

        doAnswer(
                        inv -> {
                            String key = inv.getArgument(0, String.class).trim();
                            int code = key.isEmpty() ? 0 : Integer.parseInt(key);
                            String name = supplierNames.get(code);
                            suppfInvalidKey = (name == null);
                            if (name != null) {
                                fixture.setSpName(name);
                            }
                            return name != null;
                        })
                .when(suppf)
                .readByKey(any());
        doAnswer(inv -> suppfInvalidKey).when(suppf).isInvalidKey();

        fileSet = fileSet; // keep field explicit
        fixture = new Rp0080FieldAccess(new WorkingStorage(), fileSet);

        service = new Rp0080Service(fileSet, dateutService, abortxService);
    }

    /** Stubs APLF READ NEXT to hand back the given records in order, then hit EOF. */
    private void stubAplfRecords(List<int[]> supplierAndDebitCredit) {
        AtomicInteger idx = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            int i = idx.getAndIncrement();
                            if (i < supplierAndDebitCredit.size()) {
                                int[] rec = supplierAndDebitCredit.get(i);
                                fixture.setPlSupp(rec[0]);
                                fixture.setPlDebit(BigDecimal.valueOf(rec[1]));
                                fixture.setPlCredit(BigDecimal.valueOf(rec[2]));
                            }
                            return true;
                        })
                .when(aplf)
                .readNext();

        doAnswer(inv -> idx.get() > supplierAndDebitCredit.size()).when(aplf).isAtEnd();
    }

    // ───────────────────────── happy path ─────────────────────────

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_singleSupplierSingleRecord_writesSummaryAndGrandTotal() {
        stubAplfRecords(List.of(new int[] {100, 500, 900}));
        supplierNames.put(100, "ACME CORP");

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(0);
        // header + supplier line + grand-total rule + grand total line
        assertThat(repLines).anyMatch(l -> l.contains("ACME CORP"));
        assertThat(repLines).anyMatch(l -> l.contains("GRAND TOTAL"));
        assertThat(repLines).noneMatch(l -> l.contains(NO_PAYABLES_MSG));
        verify(repf, times(1)).open(FileOpenMode.OUTPUT);
        verify(aplf).close();
        verify(suppf).close();
        verify(repf).close();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_twoRecordsSameSupplier_accumulatesBeforeFlush() {
        stubAplfRecords(List.of(new int[] {200, 100, 300}, new int[] {200, 50, 20}));
        supplierNames.put(200, "GLOBEX LTD");

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(0);
        // DR total 150, CR total 320 -> balance 170
        assertThat(repLines).anyMatch(l -> l.contains("GLOBEX LTD") && l.contains("170"));
        verify(suppf, times(1)).readByKey(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_twoDifferentSuppliers_flushesEachSeparately() {
        stubAplfRecords(List.of(new int[] {100, 10, 60}, new int[] {200, 5, 25}));
        supplierNames.put(100, "SUPP ONE");
        supplierNames.put(200, "SUPP TWO");

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(0);
        assertThat(repLines).anyMatch(l -> l.contains("SUPP ONE"));
        assertThat(repLines).anyMatch(l -> l.contains("SUPP TWO"));
        verify(suppf, times(2)).readByKey(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_syscfOpensSuccessfullyWithCompanyMatch_usesSyscfCompanyName() {
        syscfInvalidKey = false;
        doAnswer(
                        inv -> {
                            fixture.setSyCompanyName("SYSCF COMPANY NAME");
                            return true;
                        })
                .when(syscf)
                .readByKey(any());
        stubAplfRecords(List.of(new int[] {100, 1, 2}));
        supplierNames.put(100, "ANY SUPP");

        service.execute();

        assertThat(repLines).anyMatch(l -> l.contains("SYSCF COMPANY NAME"));
    }

    // ───────────────────────── edge cases ─────────────────────────

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_noApRecords_writesNoPayablesMessage() {
        // START succeeds but READ-NEXT hits EOF immediately (no records staged).
        stubAplfRecords(List.of());

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(0);
        assertThat(repLines).anyMatch(l -> l.contains(NO_PAYABLES_MSG));
        assertThat(repLines).noneMatch(l -> l.contains("GRAND TOTAL"));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_startAplfInvalidKey_setsEofAndSkipsReadLoop() {
        doReturn(true).when(aplf).isInvalidKey();

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(0);
        assertThat(repLines).anyMatch(l -> l.contains(NO_PAYABLES_MSG));
        verify(aplf, never()).readNext();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_aplfOpenFsts35_treatedAsEofNotAbort() {
        doReturn("35").when(aplf).getFileStatus();

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(0);
        assertThat(repLines).anyMatch(l -> l.contains(NO_PAYABLES_MSG));
        verify(abortxService, never()).execute(any());
        verify(aplf, never()).readNext();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_aplfOpenFsts30_treatedAsEofNotAbort() {
        doReturn("30").when(aplf).getFileStatus();

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(0);
        assertThat(repLines).anyMatch(l -> l.contains(NO_PAYABLES_MSG));
        verify(abortxService, never()).execute(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_syscfOpenFailsNot00_skipsSyscfReadKeepsDefaultCompany() {
        doReturn("35").when(syscf).getFileStatus();
        stubAplfRecords(List.of(new int[] {100, 1, 2}));
        supplierNames.put(100, "DEFAULT CO SUPP");

        service.execute();

        verify(syscf, never()).readByKey(any());
        assertThat(repLines).anyMatch(l -> l.contains("SAKURA Sales Management System"));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_syscfReadInvalidKey_keepsDefaultCompanyName() {
        syscfInvalidKey = true;
        doReturn(true).when(syscf).isInvalidKey();
        stubAplfRecords(List.of(new int[] {100, 1, 2}));
        supplierNames.put(100, "SOME SUPP");

        service.execute();

        assertThat(repLines).anyMatch(l -> l.contains("SAKURA Sales Management System"));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_suppfInvalidKey_setsUnknownSupplierName() {
        stubAplfRecords(List.of(new int[] {999, 10, 20}));
        // supplierNames has no entry for 999 -> readByKey stub reports invalid key.

        service.execute();

        assertThat(repLines).anyMatch(l -> l.contains("(unknown)"));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_lineCountReaches55_printsNewPageHeader() {
        // WK-LINE starts at 99, so the very first FLUSH-SUPP already forces a page
        // break (COBOL CHECK-PAGE: IF WK-LINE >= 55 PERFORM PAGE-HEAD). Confirms the
        // header (company name + "PAGE:") is emitted ahead of the supplier line.
        stubAplfRecords(List.of(new int[] {100, 1, 2}));
        supplierNames.put(100, "PAGED SUPP");

        service.execute();

        assertThat(repLines).anyMatch(l -> l.contains("PAGE:"));
        int headerIdx = indexOfFirstContaining(repLines, "PAGE:");
        int supplierIdx = indexOfFirstContaining(repLines, "PAGED SUPP");
        assertThat(headerIdx).isGreaterThanOrEqualTo(0);
        assertThat(supplierIdx).isGreaterThan(headerIdx);
    }

    private static int indexOfFirstContaining(List<String> lines, String needle) {
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains(needle)) {
                return i;
            }
        }
        return -1;
    }

    // ───────────────────────── error / abort paths ─────────────────────────

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_aplfOpenUnexpectedStatus_abortsWithAplfFile() {
        doReturn("99").when(aplf).getFileStatus();

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        ArgumentCaptor<AbortxLinkParm> captor = ArgumentCaptor.forClass(AbortxLinkParm.class);
        verify(abortxService).execute(captor.capture());
        assertThat(captor.getValue().getKabend().getKaFile().trim()).isEqualTo("APLF");
        assertThat(captor.getValue().getKabend().getKaFsts().trim()).isEqualTo("99");
        verify(repf, never()).open(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_repfOpenFails_abortsWithRepfFile() {
        doReturn("99").when(repf).getFileStatus();

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        ArgumentCaptor<AbortxLinkParm> captor = ArgumentCaptor.forClass(AbortxLinkParm.class);
        verify(abortxService).execute(captor.capture());
        assertThat(captor.getValue().getKabend().getKaFile().trim()).isEqualTo("REPF");
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_dateutServiceThrowsUnexpectedException_setsCompletionCode12() {
        doThrow(new RuntimeException("DATEUT failure")).when(dateutService).execute(any());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.execute());

        assertThat(ex.getMessage()).contains("Batch processing failed");
        assertThat(service.getCompletionCode()).isEqualTo(12);
        verify(abortxService, never()).execute(any());
    }
}
