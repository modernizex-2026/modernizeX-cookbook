package com.sakura.taxcal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.sakura.runtime.io.TaxfDataset;
import com.sakura.runtime.linkage.TaxcalLinkParm;
import com.sakura.taxcal.domain.TaxcalFieldAccess;
import com.sakura.taxcal.runtime.TaxcalDatasets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for {@link TaxcalService}, derived from COBOL program TAXCAL (consumption-tax
 * calculator). Ground truth for inputs/expected values: TAXCAL.cob PROCEDURE DIVISION (MAIN-000 /
 * FIND-010 / SCAN-010 / DEF-010 / CT-010 / AR-010).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TaxcalServiceTest {

    private record TaxRow(int code, int startDate, String rate) {}

    @Spy private TaxfDataset taxfSpy = new TaxfDataset();

    private TaxcalService service;
    private TaxcalFieldAccess ws;

    private static class TestDatasets extends TaxcalDatasets {
        private final TaxfDataset taxf;

        TestDatasets(TaxfDataset taxf) {
            this.taxf = taxf;
        }

        @Override
        public TaxfDataset getTaxf() {
            return taxf;
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        TaxcalDatasets fileSet = new TestDatasets(taxfSpy);
        service = new TaxcalService(fileSet);
        ws = getWs();

        doNothing().when(taxfSpy).open(any());
        doNothing().when(taxfSpy).close();
        doReturn(true).when(taxfSpy).start(any(), any());
        doReturn("00").when(taxfSpy).getFileStatus();
        doReturn(false).when(taxfSpy).isInvalidKey();
    }

    private TaxcalFieldAccess getWs() throws Exception {
        Field f = TaxcalService.class.getDeclaredField("ws");
        f.setAccessible(true);
        return (TaxcalFieldAccess) f.get(service);
    }

    private TaxcalLinkParm newParm(int category, int taxType, int round, int date, long amount) {
        TaxcalLinkParm p = new TaxcalLinkParm();
        var k = p.getKtax();
        k.setKtCategory(category);
        k.setKtTaxType(taxType);
        k.setKtRound(round);
        k.setKtDate(date);
        k.setKtAmount(BigDecimal.valueOf(amount));
        k.setKtStatus("XX"); // pre-existing garbage status - COBOL always resets to "00"
        return p;
    }

    /**
     * Stubs readNext() to feed {@code rows} into the shared TX-CODE/TX-START-DATE/TX-RATE fields,
     * one per call; scan is expected to terminate via a category mismatch inside {@code rows}
     * (isAtEnd() always false), matching SCAN-010's GO TO SCAN-999 on mismatch.
     */
    private void stubScanRowsTerminatedByMismatch(List<TaxRow> rows) {
        AtomicInteger idx = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            TaxRow r = rows.get(idx.getAndIncrement());
                            ws.setTxCode(r.code());
                            ws.setTxStartDate(r.startDate());
                            ws.setTxRate(new BigDecimal(r.rate()));
                            return true;
                        })
                .when(taxfSpy)
                .readNext();
        doReturn(false).when(taxfSpy).isAtEnd();
    }

    /**
     * Stubs readNext()/isAtEnd() so {@code rows} are fed in order and AT END fires right after the
     * last row - matching SCAN-010's READ TAXF NEXT ... AT END path.
     */
    private void stubScanRowsTerminatedByEof(List<TaxRow> rows) {
        AtomicInteger idx = new AtomicInteger(0);
        boolean[] atEnd = {false};
        doAnswer(
                        inv -> {
                            int i = idx.getAndIncrement();
                            if (i < rows.size()) {
                                TaxRow r = rows.get(i);
                                ws.setTxCode(r.code());
                                ws.setTxStartDate(r.startDate());
                                ws.setTxRate(new BigDecimal(r.rate()));
                                atEnd[0] = false;
                            } else {
                                atEnd[0] = true;
                            }
                            return !atEnd[0];
                        })
                .when(taxfSpy)
                .readNext();
        doAnswer(inv -> atEnd[0]).when(taxfSpy).isAtEnd();
    }

    /* ── MAIN-000: exempt category / tax-type short-circuit ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_exemptCategory3_setsNetGrossToAmountZeroRateNoFileAccess() {
        TaxcalLinkParm params = newParm(3, 1, 1, 20240101, 100000);

        service.execute(params);

        var k = params.getKtax();
        assertEquals("00", k.getKtStatus());
        assertEquals(0, k.getKtNet().compareTo(BigDecimal.valueOf(100000)));
        assertEquals(0, k.getKtGross().compareTo(BigDecimal.valueOf(100000)));
        assertEquals(0, k.getKtTax().compareTo(BigDecimal.ZERO));
        assertEquals(0, k.getKtRate().compareTo(BigDecimal.ZERO));
        verify(taxfSpy, never()).open(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_exemptTaxType3_setsNetGrossToAmountZeroRateNoFileAccess() {
        TaxcalLinkParm params = newParm(1, 3, 1, 20240101, 50000);

        service.execute(params);

        var k = params.getKtax();
        assertEquals(0, k.getKtNet().compareTo(BigDecimal.valueOf(50000)));
        assertEquals(0, k.getKtGross().compareTo(BigDecimal.valueOf(50000)));
        assertEquals(0, k.getKtRate().compareTo(BigDecimal.ZERO));
        verify(taxfSpy, never()).open(any());
    }

    /* ── FIND-010 / DEF-010: file open failure falls back to statutory defaults ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_fileOpenFails_category2_defaultsToPoint080() {
        doReturn("35").when(taxfSpy).getFileStatus();
        TaxcalLinkParm params = newParm(2, 1, 1, 20240101, 100000);

        service.execute(params);

        var k = params.getKtax();
        assertEquals(0, k.getKtRate().compareTo(new BigDecimal("0.080")));
        assertEquals(0, k.getKtTax().compareTo(BigDecimal.valueOf(8000)));
        assertEquals(0, k.getKtGross().compareTo(BigDecimal.valueOf(108000)));
        verify(taxfSpy, never()).start(any(), any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_fileOpenFails_categoryOther_defaultsToPoint100() {
        doReturn("35").when(taxfSpy).getFileStatus();
        TaxcalLinkParm params = newParm(1, 1, 1, 20240101, 100000);

        service.execute(params);

        var k = params.getKtax();
        assertEquals(0, k.getKtRate().compareTo(new BigDecimal("0.100")));
        assertEquals(0, k.getKtTax().compareTo(BigDecimal.valueOf(10000)));
        assertEquals(0, k.getKtGross().compareTo(BigDecimal.valueOf(110000)));
    }

    /* ── SCAN-010: rate lookup from TAXF ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_scanFindsLatestApplicableRate_lastMatchWithinCategoryWins() {
        stubScanRowsTerminatedByMismatch(
                List.of(
                        new TaxRow(1, 20200101, "0.080"),
                        new TaxRow(1, 20230101, "0.100"),
                        new TaxRow(9, 0, "0.000")));
        TaxcalLinkParm params = newParm(1, 1, 1, 20240101, 100000);

        service.execute(params);

        var k = params.getKtax();
        assertEquals(0, k.getKtRate().compareTo(new BigDecimal("0.100")));
        assertEquals(0, k.getKtTax().compareTo(BigDecimal.valueOf(10000)));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_scanRowWithFutureStartDateIgnored_fallsBackToDefault() {
        stubScanRowsTerminatedByMismatch(
                List.of(new TaxRow(1, 20230101, "0.150"), new TaxRow(9, 0, "0.000")));
        TaxcalLinkParm params = newParm(1, 1, 1, 20200101, 100000);

        service.execute(params);

        var k = params.getKtax();
        assertEquals(0, k.getKtRate().compareTo(new BigDecimal("0.100")));
        assertEquals(0, k.getKtTax().compareTo(BigDecimal.valueOf(10000)));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_scanReachesEndOfFileAfterMatch_usesLastRateFoundBeforeEof() {
        stubScanRowsTerminatedByEof(List.of(new TaxRow(1, 20210101, "0.090")));
        TaxcalLinkParm params = newParm(1, 1, 1, 20240101, 100000);

        service.execute(params);

        var k = params.getKtax();
        assertEquals(0, k.getKtRate().compareTo(new BigDecimal("0.090")));
        assertEquals(0, k.getKtTax().compareTo(BigDecimal.valueOf(9000)));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_scanImmediateEndOfFile_noRowsFound_fallsBackToDefault() {
        stubScanRowsTerminatedByEof(List.of());
        TaxcalLinkParm params = newParm(2, 1, 1, 20240101, 100000);

        service.execute(params);

        var k = params.getKtax();
        assertEquals(0, k.getKtRate().compareTo(new BigDecimal("0.080")));
        assertEquals(0, k.getKtTax().compareTo(BigDecimal.valueOf(8000)));
    }

    /* ── CT-010: tax-inclusive (back out) vs tax-exclusive (add on top) ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_taxInclusive_backsOutTaxFromGrossAmount() {
        doReturn("35").when(taxfSpy).getFileStatus(); // default rate 0.100 (category 1)
        TaxcalLinkParm params = newParm(1, 2, 1, 20240101, 110000);

        service.execute(params);

        var k = params.getKtax();
        assertEquals(0, k.getKtGross().compareTo(BigDecimal.valueOf(110000)));
        assertEquals(0, k.getKtTax().compareTo(BigDecimal.valueOf(10000)));
        assertEquals(0, k.getKtNet().compareTo(BigDecimal.valueOf(100000)));
    }

    /* ── AR-010: rounding rules ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_roundFloor_truncatesExactHalfFraction() {
        doReturn("35").when(taxfSpy).getFileStatus(); // default rate 0.100 (category 1)
        TaxcalLinkParm params = newParm(1, 1, 2, 20240101, 100005); // raw tax = 10000.5

        service.execute(params);

        var k = params.getKtax();
        assertEquals(0, k.getKtTax().compareTo(BigDecimal.valueOf(10000)));
        assertEquals(0, k.getKtGross().compareTo(BigDecimal.valueOf(110005)));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_roundHalfUpDefault_bumpsUpExactHalfFraction() {
        doReturn("35").when(taxfSpy).getFileStatus(); // default rate 0.100 (category 1)
        TaxcalLinkParm params = newParm(1, 1, 1, 20240101, 100005); // raw tax = 10000.5

        service.execute(params);

        var k = params.getKtax();
        assertEquals(0, k.getKtTax().compareTo(BigDecimal.valueOf(10001)));
        assertEquals(0, k.getKtGross().compareTo(BigDecimal.valueOf(110006)));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_roundCeil_bumpsUpEvenBelowHalfFraction() {
        doReturn("35").when(taxfSpy).getFileStatus(); // default rate 0.100 (category 1)
        TaxcalLinkParm params = newParm(1, 1, 3, 20240101, 100003); // raw tax = 10000.3

        service.execute(params);

        var k = params.getKtax();
        assertEquals(0, k.getKtTax().compareTo(BigDecimal.valueOf(10001)));
        assertEquals(0, k.getKtGross().compareTo(BigDecimal.valueOf(110004)));
    }

    /* ── edge: negative amount ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_negativeAmountHalfUpDefault_neverRoundsUpNegativeFraction() {
        doReturn("35").when(taxfSpy).getFileStatus(); // default rate 0.100 (category 1)
        TaxcalLinkParm params = newParm(1, 1, 1, 20240101, -100005); // raw tax = -10000.5

        service.execute(params);

        var k = params.getKtax();
        assertEquals(0, k.getKtTax().compareTo(BigDecimal.valueOf(-10000)));
        assertEquals(0, k.getKtNet().compareTo(BigDecimal.valueOf(-100005)));
        assertEquals(0, k.getKtGross().compareTo(BigDecimal.valueOf(-110005)));
    }
}
