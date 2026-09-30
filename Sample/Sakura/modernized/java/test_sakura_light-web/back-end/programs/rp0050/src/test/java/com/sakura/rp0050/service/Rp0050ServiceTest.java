package com.sakura.rp0050.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.rp0050.runtime.Rp0050Datasets;
import com.sakura.runtime.Utility;
import com.sakura.runtime.io.InvdfDataset;
import com.sakura.runtime.io.InvhfDataset;
import com.sakura.runtime.io.ProdfDataset;
import com.sakura.runtime.io.RepfDataset;
import com.sakura.runtime.io.SyscfDataset;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for Rp0050Service (COBOL RP0050 — sales summary by product), generated from {@code
 * RP0050.cob} PROCEDURE DIVISION as ground truth.
 *
 * <p>Test strategy: all five datasets (INVHF/INVDF/PRODF/SYSCF/REPF) are real dataset objects
 * wrapped with {@code spy()} so the shared record buffers (and therefore {@code Rp0050FieldAccess})
 * behave exactly as production; only I/O methods
 * (open/close/start/readNext/isAtEnd/readByKey/isInvalidKey/getFileStatus/write) are stubbed so no
 * real file access happens. {@code start()} is stubbed to seek into an in-memory row list based on
 * the key value the service wrote into the record buffer just before calling start (mirrors real
 * indexed-file KEY NOT LESS THAN semantics). DATEUT runs for real (TODY just wraps {@code
 * LocalDate.now()}, safe and deterministic enough for WK-SYSDATE, which is only echoed into the
 * report header). ABORTX is mocked. REPF output lines are captured via {@code repf.write()} by
 * reading the just-copied REP-REC bytes straight off the spy's own record buffer.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Timeout(value = 10, unit = TimeUnit.SECONDS)
class Rp0050ServiceTest {

    private record HeaderRow(long ihNo, int date, int delFlag, int status) {}

    private record DetailRow(long idNo, int idProd, BigDecimal qty, BigDecimal amt) {}

    @Mock private AbortxService abortxService;

    private DateutService dateutService;

    private Rp0050Datasets fileSet;
    private InvhfDataset invhf;
    private InvdfDataset invdf;
    private ProdfDataset prodf;
    private SyscfDataset syscf;
    private RepfDataset repf;

    private Rp0050Service service;

    private final List<HeaderRow> headerRows = new ArrayList<>();
    private final List<DetailRow> detailRows = new ArrayList<>();
    private final Map<Integer, String> productNames = new HashMap<>();
    private final List<String> reportLines = new ArrayList<>();

    private final AtomicInteger headerIndex = new AtomicInteger(0);
    private final AtomicBoolean invhfAtEnd = new AtomicBoolean(false);
    private final AtomicBoolean invhfStartInvalid = new AtomicBoolean(false);

    private final AtomicInteger detailIndex = new AtomicInteger(0);
    private final AtomicBoolean invdfAtEnd = new AtomicBoolean(false);
    private final AtomicBoolean invdfStartInvalid = new AtomicBoolean(false);

    private final AtomicBoolean prodInvalidKey = new AtomicBoolean(true);
    private final AtomicBoolean syscfInvalidKey = new AtomicBoolean(true);

    private MockedStatic<Utility> utilityMock;

    @BeforeEach
    void setUp() {
        dateutService = new DateutService();

        Rp0050Datasets real = new Rp0050Datasets();
        fileSet = spy(real);
        invhf = spy(real.getInvhf());
        invdf = spy(real.getInvdf());
        prodf = spy(real.getProdf());
        syscf = spy(real.getSyscf());
        repf = spy(real.getRepf());
        doReturn(invhf).when(fileSet).getInvhf();
        doReturn(invdf).when(fileSet).getInvdf();
        doReturn(prodf).when(fileSet).getProdf();
        doReturn(syscf).when(fileSet).getSyscf();
        doReturn(repf).when(fileSet).getRepf();

        doNothing().when(invhf).open(any());
        doNothing().when(invdf).open(any());
        doNothing().when(prodf).open(any());
        doNothing().when(syscf).open(any());
        doNothing().when(repf).open(any());
        doNothing().when(invhf).close();
        doNothing().when(invdf).close();
        doNothing().when(prodf).close();
        doNothing().when(syscf).close();
        doNothing().when(repf).close();
        doReturn("00").when(invhf).getFileStatus();
        doReturn("00").when(invdf).getFileStatus();
        doReturn("00").when(prodf).getFileStatus();
        doReturn("00").when(syscf).getFileStatus();
        doReturn("00").when(repf).getFileStatus();

        // INVHF: start() seeks headerRows to first entry with IH-NO >= sought key.
        doAnswer(
                        inv -> {
                            long seek = invhf.getRecord().getLong("IH-NO");
                            int idx = 0;
                            while (idx < headerRows.size() && headerRows.get(idx).ihNo() < seek) {
                                idx++;
                            }
                            headerIndex.set(idx);
                            boolean invalid = idx >= headerRows.size();
                            invhfStartInvalid.set(invalid);
                            return !invalid;
                        })
                .when(invhf)
                .start(any(), any());
        doAnswer(inv -> invhfStartInvalid.get()).when(invhf).isInvalidKey();
        doAnswer(
                        inv -> {
                            int idx = headerIndex.getAndIncrement();
                            if (idx < headerRows.size()) {
                                HeaderRow r = headerRows.get(idx);
                                invhf.getRecord().setLong("IH-NO", r.ihNo());
                                invhf.getRecord().setInt("IH-DATE", r.date());
                                invhf.getRecord().setInt("IH-DEL-FLAG", r.delFlag());
                                invhf.getRecord().setInt("IH-STATUS", r.status());
                                invhfAtEnd.set(false);
                                return true;
                            }
                            invhfAtEnd.set(true);
                            return false;
                        })
                .when(invhf)
                .readNext();
        doAnswer(inv -> invhfAtEnd.get()).when(invhf).isAtEnd();

        // INVDF: start() seeks detailRows to first entry with ID-NO >= sought key.
        doAnswer(
                        inv -> {
                            long seek = invdf.getRecord().getLong("ID-NO");
                            int idx = 0;
                            while (idx < detailRows.size() && detailRows.get(idx).idNo() < seek) {
                                idx++;
                            }
                            detailIndex.set(idx);
                            boolean invalid = idx >= detailRows.size();
                            invdfStartInvalid.set(invalid);
                            return !invalid;
                        })
                .when(invdf)
                .start(any(), any());
        doAnswer(inv -> invdfStartInvalid.get()).when(invdf).isInvalidKey();
        doAnswer(
                        inv -> {
                            int idx = detailIndex.getAndIncrement();
                            if (idx < detailRows.size()) {
                                DetailRow r = detailRows.get(idx);
                                invdf.getRecord().setLong("ID-NO", r.idNo());
                                invdf.getRecord().setInt("ID-PROD", r.idProd());
                                invdf.getRecord().setDecimal("ID-QTY", r.qty());
                                invdf.getRecord().setDecimal("ID-AMOUNT", r.amt());
                                invdfAtEnd.set(false);
                                return true;
                            }
                            invdfAtEnd.set(true);
                            return false;
                        })
                .when(invdf)
                .readNext();
        doAnswer(inv -> invdfAtEnd.get()).when(invdf).isAtEnd();

        // PRODF: readByKey() looks up PR-CODE the service just wrote into the buffer.
        doAnswer(
                        inv -> {
                            int code = prodf.getRecord().getInt("PR-CODE");
                            String name = productNames.get(code);
                            if (name != null) {
                                prodf.getRecord().setString("PR-NAME", name);
                                prodInvalidKey.set(false);
                                return true;
                            }
                            prodInvalidKey.set(true);
                            return false;
                        })
                .when(prodf)
                .readByKey(any());
        doAnswer(inv -> prodInvalidKey.get()).when(prodf).isInvalidKey();

        // SYSCF: single fixed record (SY-KEY = 1); default not-found -> default company name.
        doAnswer(inv -> !syscfInvalidKey.get()).when(syscf).readByKey(any());
        doAnswer(inv -> syscfInvalidKey.get()).when(syscf).isInvalidKey();

        // REPF: capture REP-REC bytes exactly as written (no real file I/O).
        doAnswer(
                        inv -> {
                            reportLines.add(repf.getRecord().getString("REP-REC"));
                            return null;
                        })
                .when(repf)
                .write();

        service = new Rp0050Service(fileSet, dateutService, abortxService);
    }

    @AfterEach
    void tearDown() {
        if (utilityMock != null) {
            utilityMock.close();
            utilityMock = null;
        }
        headerRows.clear();
        detailRows.clear();
        productNames.clear();
        reportLines.clear();
    }

    private void addHeader(long ihNo, int date, int delFlag, int status) {
        headerRows.add(new HeaderRow(ihNo, date, delFlag, status));
    }

    private void addDetail(long idNo, int idProd, long qty, long amt) {
        detailRows.add(
                new DetailRow(idNo, idProd, BigDecimal.valueOf(qty), BigDecimal.valueOf(amt)));
    }

    private void addProduct(int code, String name) {
        productNames.put(code, name);
    }

    /** Stubs console input for WK-IN-FROM / WK-IN-TO (read in that order). */
    private void withDateInputs(String from, String to) {
        utilityMock = mockStatic(Utility.class, org.mockito.Mockito.CALLS_REAL_METHODS);
        utilityMock.when(Utility::readStdinLine).thenReturn(from, to);
    }

    private static long parseEdited(String field) {
        String cleaned = field.replace(",", "").trim();
        return cleaned.isEmpty() ? 0L : Long.parseLong(cleaned);
    }

    /**
     * RD-LINE column layout: FILLER(1) RP-CODE(8) FILLER(2) RP-NAME(40) FILLER(2) RP-QTY(16)
     * FILLER(2) RP-AMT(19).
     */
    private static int rpCodeOf(String line) {
        return Integer.parseInt(line.substring(1, 9).trim());
    }

    private static String rpNameOf(String line) {
        return line.substring(11, 51).trim();
    }

    private static long rpQtyOf(String line) {
        return parseEdited(line.substring(53, 69));
    }

    private static long rpAmtOf(String line) {
        return parseEdited(line.substring(71, 90));
    }

    /** RT-LINE column layout: FILLER(1) FILLER(51) FILLER(2) RT-QTY(16) FILLER(2) RT-AMT(19). */
    private static long rtQtyOf(String line) {
        return parseEdited(line.substring(54, 70));
    }

    private static long rtAmtOf(String line) {
        return parseEdited(line.substring(72, 91));
    }

    /** RPT-H1 column layout: FILLER(1) H1-COMPANY(40) ... */
    private static String h1CompanyOf(String line) {
        return line.substring(1, 41).trim();
    }

    private List<String> productLines() {
        List<String> lines = new ArrayList<>();
        for (String l : reportLines) {
            String trimmed = l.trim();
            if (!trimmed.isEmpty() && Character.isDigit(trimmed.charAt(0)) && l.length() >= 90) {
                lines.add(l);
            }
        }
        return lines;
    }

    // ───────────────────────── happy path ─────────────────────────

    @Test
    void execute_noHeaders_writesNoSalesLineAndCompletionCodeZero() {
        service.execute();

        assertThat(reportLines)
                .anyMatch(l -> l.trim().equals("*** NO SALES IN THE SELECTED PERIOD ***"));
        assertThat(service.getCompletionCode()).isEqualTo(0);
        verify(invhf, times(1)).close();
        verify(repf, times(1)).close();
    }

    @Test
    void execute_singleHeaderSingleDetail_writesOneProductLineWithQtyAndAmt() {
        addHeader(1, 20240115, 0, 0);
        addDetail(1, 5001, 10, 1000);
        addProduct(5001, "Widget");

        service.execute();

        List<String> lines = productLines();
        assertThat(lines).hasSize(1);
        assertThat(rpCodeOf(lines.get(0))).isEqualTo(5001);
        assertThat(rpNameOf(lines.get(0))).isEqualTo("Widget");
        assertThat(rpQtyOf(lines.get(0))).isEqualTo(10L);
        assertThat(rpAmtOf(lines.get(0))).isEqualTo(1000L);
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }

    @Test
    void execute_multipleDetailLinesSameProduct_sumsQuantityAndAmount() {
        addHeader(1, 20240115, 0, 0);
        addDetail(1, 5001, 10, 1000);
        addDetail(1, 5001, 5, 500);
        addProduct(5001, "Widget");

        service.execute();

        List<String> lines = productLines();
        assertThat(lines).hasSize(1);
        assertThat(rpQtyOf(lines.get(0))).isEqualTo(15L);
        assertThat(rpAmtOf(lines.get(0))).isEqualTo(1500L);
    }

    @Test
    void execute_multipleProducts_sortedAscendingByProductCode() {
        addHeader(1, 20240115, 0, 0);
        addDetail(1, 9000, 1, 100);
        addDetail(1, 3000, 2, 200);
        addDetail(1, 6000, 3, 300);
        addProduct(9000, "Zeta");
        addProduct(3000, "Alpha");
        addProduct(6000, "Mid");

        service.execute();

        List<String> lines = productLines();
        assertThat(lines).hasSize(3);
        assertThat(rpCodeOf(lines.get(0))).isEqualTo(3000);
        assertThat(rpCodeOf(lines.get(1))).isEqualTo(6000);
        assertThat(rpCodeOf(lines.get(2))).isEqualTo(9000);
    }

    @Test
    void execute_headerAcrossTwoInvoices_accumulatesSameProductAcrossHeaders() {
        addHeader(1, 20240115, 0, 0);
        addHeader(2, 20240116, 0, 0);
        addDetail(1, 5001, 10, 1000);
        addDetail(2, 5001, 20, 2000);
        addProduct(5001, "Widget");

        service.execute();

        List<String> lines = productLines();
        assertThat(lines).hasSize(1);
        assertThat(rpQtyOf(lines.get(0))).isEqualTo(30L);
        assertThat(rpAmtOf(lines.get(0))).isEqualTo(3000L);
    }

    @Test
    void execute_headerDeletedFlagSet_excludedFromReport() {
        addHeader(1, 20240115, 1, 0);
        addDetail(1, 5001, 10, 1000);
        addProduct(5001, "Widget");

        service.execute();

        assertThat(productLines()).isEmpty();
        assertThat(reportLines)
                .anyMatch(l -> l.trim().equals("*** NO SALES IN THE SELECTED PERIOD ***"));
    }

    @Test
    void execute_headerStatusNine_excludedFromReport() {
        addHeader(1, 20240115, 0, 9);
        addDetail(1, 5001, 10, 1000);
        addProduct(5001, "Widget");

        service.execute();

        assertThat(productLines()).isEmpty();
    }

    @Test
    void execute_headerDateOutsideFilterRange_excluded() {
        withDateInputs("20240101", "20240131");
        addHeader(1, 20240201, 0, 0);
        addDetail(1, 5001, 10, 1000);
        addProduct(5001, "Widget");

        service.execute();

        assertThat(productLines()).isEmpty();
    }

    @Test
    void execute_headerDateWithinFilterRange_included() {
        withDateInputs("20240101", "20240131");
        addHeader(1, 20240115, 0, 0);
        addDetail(1, 5001, 10, 1000);
        addProduct(5001, "Widget");

        service.execute();

        assertThat(productLines()).hasSize(1);
    }

    @Test
    void execute_detailRecordDifferentInvoiceNo_stopsDetailScanForHeader() {
        addHeader(1, 20240115, 0, 0);
        addDetail(1, 5001, 10, 1000);
        // ID-NO=2 belongs to a different (absent) header — RD-010 sees the key
        // mismatch and stops scanning for header 1 without consuming/counting it.
        addDetail(2, 5002, 5, 500);
        addProduct(5001, "Widget");
        addProduct(5002, "Gadget");

        service.execute();

        List<String> lines = productLines();
        assertThat(lines).hasSize(1);
        assertThat(rpCodeOf(lines.get(0))).isEqualTo(5001);
    }

    @Test
    void execute_productKnownInProdf_usesProductNameFromMaster() {
        addHeader(1, 20240115, 0, 0);
        addDetail(1, 5001, 10, 1000);
        addProduct(5001, "Widget De Luxe");

        service.execute();

        assertThat(rpNameOf(productLines().get(0))).isEqualTo("Widget De Luxe");
    }

    @Test
    void execute_productUnknownInProdf_usesUnknownPlaceholder() {
        addHeader(1, 20240115, 0, 0);
        addDetail(1, 5001, 10, 1000);
        // No addProduct(...) call -> PRODF readByKey misses -> "(unknown)".

        service.execute();

        assertThat(rpNameOf(productLines().get(0))).isEqualTo("(unknown)");
    }

    @Test
    void execute_grandTotalLine_sumsAllProductQuantitiesAndAmounts() {
        addHeader(1, 20240115, 0, 0);
        addDetail(1, 5001, 10, 1000);
        addDetail(1, 5002, 20, 2000);
        addProduct(5001, "Widget");
        addProduct(5002, "Gadget");

        service.execute();

        String grandTotalLine =
                reportLines.stream()
                        .filter(l -> l.contains("GRAND TOTAL"))
                        .findFirst()
                        .orElseThrow();
        assertThat(rtQtyOf(grandTotalLine)).isEqualTo(30L);
        assertThat(rtAmtOf(grandTotalLine)).isEqualTo(3000L);
    }

    // ───────────────────────── edge cases (ground truth: PARM-RTN / INIT-RTN)
    // ─────────────────────────

    @Test
    void execute_blankDateInputs_defaultsToFullRange() {
        withDateInputs("", "");
        addHeader(1, 19990101, 0, 0);
        addHeader(2, 99999999 - 1, 0, 0);
        addDetail(1, 5001, 1, 100);
        addDetail(2, 5002, 1, 100);
        addProduct(5001, "Widget");
        addProduct(5002, "Gadget");

        service.execute();

        assertThat(productLines()).hasSize(2);
    }

    @Test
    void execute_nonNumericDateInput_defaultsToFullRange() {
        withDateInputs("ABCDEFGH", "ABCDEFGH");
        addHeader(1, 20240115, 0, 0);
        addDetail(1, 5001, 10, 1000);
        addProduct(5001, "Widget");

        service.execute();

        assertThat(productLines()).hasSize(1);
    }

    @Test
    void execute_syscfCompanyFound_usesCompanyNameFromSyscf() {
        syscfInvalidKey.set(false);
        syscf.getRecord().setString("SY-COMPANY-NAME", "Sakura Trading Co");

        service.execute();

        String headerLine = reportLines.get(0);
        assertThat(h1CompanyOf(headerLine)).isEqualTo("Sakura Trading Co");
    }

    @Test
    void execute_syscfCompanyNotFound_usesDefaultCompanyName() {
        syscfInvalidKey.set(true);

        service.execute();

        String headerLine = reportLines.get(0);
        assertThat(h1CompanyOf(headerLine)).isEqualTo("SAKURA Sales Management System");
    }

    @Test
    void execute_moreThan49Products_printsSecondPageHeader() {
        addHeader(1, 20240115, 0, 0);
        // PR-010 sets WK-LINE=99 so the very first product always triggers PAGE-HEAD
        // (WK-LINE reset to 6). A second PAGE-HEAD fires once WK-LINE reaches 55,
        // i.e. after 49 more product lines (6 + 49 = 55).
        for (int i = 1; i <= 50; i++) {
            addDetail(1, 1000 + i, 1, 10);
            addProduct(1000 + i, "Product " + i);
        }

        service.execute();

        long pageHeaderCount =
                reportLines.stream().filter(l -> l.contains("SALES SUMMARY BY PRODUCT")).count();
        assertThat(pageHeaderCount).isEqualTo(2);
        assertThat(productLines()).hasSize(50);
    }

    @Test
    void execute_shortReportLineAfterLongerLine_leavesStaleBytesInsteadOfSpaces_convertGap() {
        // CONVERT-GAP: COBOL MOVE RPT-H2 TO REP-REC space-fills the whole REP-REC(132)
        // destination, since RPT-H2 (95 bytes) is shorter than REP-REC. RecordImage
        // .writeBytes (RecordImage.java ~line 643) only copies min(src.length,
        // dest.length) bytes and never clears the remainder, so bytes 96-132 of REP-REC
        // still hold leftover text from the previous (longer) write -- here, "PAGE: n"
        // leaking in from the immediately preceding RPT-H1 line. This is a shared-runtime
        // gap (RecordImage.writeBytes), not specific to RP0050's generated code; the
        // expected value below is per-COBOL ground truth, so this test intentionally FAILS.
        addHeader(1, 20240115, 0, 0);
        addDetail(1, 5001, 10, 1000);
        addProduct(5001, "Widget");

        service.execute();

        String h2Line = reportLines.get(1);
        assertThat(h2Line.substring(95).trim()).isEmpty();
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void execute_productTableLimit2000_ignoresProductsBeyondLimit() {
        addHeader(1, 20240115, 0, 0);
        for (int i = 1; i <= 2001; i++) {
            addDetail(1, i, 1, 1);
            addProduct(i, "P" + i);
        }

        service.execute();

        // AD-010: WK-PN < 2000 guard — the 2001st distinct product is silently dropped.
        assertThat(productLines()).hasSize(2000);
    }

    // ───────────────────────── error / abort paths (ground truth: INIT-RTN / ABEND-RTN)
    // ─────────────────────────

    @Test
    void execute_invhfOpenFailsWithBadStatus_abortsWithCompletionCode255() {
        doReturn("99").when(invhf).getFileStatus();

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        verify(abortxService, times(1)).execute(any());
        verify(invdf, never()).open(any());
    }

    @Test
    void execute_invhfOpenStatus35_treatedAsEofAndProducesNoSalesReport() {
        doReturn("35").when(invhf).getFileStatus();
        addHeader(1, 20240115, 0, 0);
        addDetail(1, 5001, 10, 1000);

        service.execute();

        // INIT-010: FSTS=35 does NOT abort (35/30 tolerated) but sets WK-MAIN-EOF=1,
        // so ACCUM-RTN's READ-HDR loop never runs and no headers are scanned.
        assertThat(productLines()).isEmpty();
        assertThat(reportLines)
                .anyMatch(l -> l.trim().equals("*** NO SALES IN THE SELECTED PERIOD ***"));
        assertThat(service.getCompletionCode()).isEqualTo(0);
        verify(abortxService, never()).execute(any());
    }

    @Test
    void execute_repfOpenFails_abortsWithCompletionCode255() {
        doReturn("99").when(repf).getFileStatus();

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        verify(abortxService, times(1)).execute(any());
    }

    @Test
    void execute_unexpectedRuntimeExceptionDuringProcessing_setsCompletionCode12() {
        addHeader(1, 20240115, 0, 0);
        addDetail(1, 5001, 10, 1000);
        org.mockito.Mockito.doThrow(new RuntimeException("simulated I/O failure"))
                .when(prodf)
                .readByKey(any());

        org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class, () -> service.execute());

        assertThat(service.getCompletionCode()).isEqualTo(12);
    }
}
