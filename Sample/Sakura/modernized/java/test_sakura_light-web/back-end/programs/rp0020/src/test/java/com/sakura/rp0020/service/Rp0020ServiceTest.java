package com.sakura.rp0020.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
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
import com.sakura.rp0020.domain.Rp0020FieldAccess;
import com.sakura.rp0020.runtime.Rp0020Datasets;
import com.sakura.runtime.Utility;
import com.sakura.runtime.io.CatgfDataset;
import com.sakura.runtime.io.ProdfDataset;
import com.sakura.runtime.io.RepfDataset;
import com.sakura.runtime.io.SyscfDataset;
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

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for Rp0020Service (COBOL RP0020 — product master list report), generated from {@code
 * RP0020.cob} PROCEDURE DIVISION as ground truth.
 *
 * <p>Test strategy: PRODF/CATGF/SYSCF/REPF are real dataset objects wrapped with {@code spy()} so
 * the record buffer (and therefore {@code Rp0020FieldAccess}, which registers those buffers) works
 * exactly as in production; only I/O methods
 * (open/close/write/start/readNext/readByKey/getFileStatus/isInvalidKey) are stubbed so no real
 * file/DB access happens. DATEUT is mocked to return a fixed "today" (its TODY function reads the
 * real system clock, which would make WK-SYSDATE non-deterministic). ABORTX is mocked. Console
 * input (from/to product code prompts) goes through the static {@code Utility.readStdinLine()} —
 * mocked via MockedStatic with CALLS_REAL_METHODS so every other Utility helper (fieldEquals,
 * isNumeric, ...) still runs for real.
 *
 * <p>Report output (REP-REC) is a single flat buffer reused for every WRITE. A separate {@code
 * Rp0020FieldAccess} (resultWs), wired to the same fileSet, decodes it. Because WK-LINE starts at
 * 99 (always {@literal >}= 55), the very first product record always triggers a page-header write
 * first, so write-call indices are deterministic: 0-4 = header lines, 5.. = one write per
 * non-deleted product detail line (in input order), then 1-2 more writes for the totals section. A
 * snapshot of the decoded fields is captured at every write() call (into {@link #writeSnapshots})
 * so per-product detail lines can be inspected even though later writes overwrite the shared
 * buffer.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Timeout(value = 10, unit = TimeUnit.SECONDS)
class Rp0020ServiceTest {

    private static final int FIXED_SYSDATE = 20260315;

    @Mock private DateutService dateutService;
    @Mock private AbortxService abortxService;

    private Rp0020Datasets fileSet;
    private ProdfDataset prodf;
    private CatgfDataset catgf;
    private SyscfDataset syscf;
    private RepfDataset repf;

    private Rp0020Service service;
    private Rp0020FieldAccess resultWs;

    private MockedStatic<Utility> mockedUtility;

    private final List<Map<String, Object>> writeSnapshots = new ArrayList<>();
    private final AtomicInteger startPrCode = new AtomicInteger(Integer.MIN_VALUE);

    @BeforeEach
    void setUp() {
        Rp0020Datasets real = new Rp0020Datasets();
        fileSet = spy(real);
        prodf = spy(real.getProdf());
        catgf = spy(real.getCatgf());
        syscf = spy(real.getSyscf());
        repf = spy(real.getRepf());
        doReturn(prodf).when(fileSet).getProdf();
        doReturn(catgf).when(fileSet).getCatgf();
        doReturn(syscf).when(fileSet).getSyscf();
        doReturn(repf).when(fileSet).getRepf();

        doNothing().when(prodf).open(any());
        doNothing().when(catgf).open(any());
        doNothing().when(syscf).open(any());
        doNothing().when(repf).open(any());
        doNothing().when(prodf).close();
        doNothing().when(catgf).close();
        doNothing().when(syscf).close();
        doNothing().when(repf).close();

        doReturn("00").when(prodf).getFileStatus();
        doReturn("00").when(catgf).getFileStatus();
        doReturn("00").when(syscf).getFileStatus();
        doReturn("00").when(repf).getFileStatus();

        doReturn(false).when(prodf).isInvalidKey();
        doReturn(false).when(catgf).isInvalidKey();
        doReturn(false).when(syscf).isInvalidKey();

        doReturn(true).when(syscf).readByKey(any());

        // Captures the PR-CODE moved into the buffer immediately before START (ground
        // truth: PRINT-010 "MOVE WK-PROD-FROM TO PR-CODE; START PRODF KEY NOT < PR-CODE").
        doAnswer(
                        inv -> {
                            startPrCode.set(prodf.getRecord().getInt("PR-CODE"));
                            return true;
                        })
                .when(prodf)
                .start(any(), any());

        // DATEUT stub: only TODY is used by RP0020 (INIT-010), fixed "today".
        doAnswer(
                        inv -> {
                            DateutLinkParm p = inv.getArgument(0);
                            p.getKdate().setKdDate1(FIXED_SYSDATE);
                            p.getKdate().setKdStatus("00");
                            return null;
                        })
                .when(dateutService)
                .execute(any());

        mockedUtility = mockStatic(Utility.class, CALLS_REAL_METHODS);

        service = new Rp0020Service(fileSet, dateutService, abortxService);
        // Reuse the service's own field accessor (not a fresh one) so WORKING-STORAGE
        // fields like H1-COMPANY — which live only in Rp0020Service's private `ws`,
        // backed by a WorkingStorage buffer this test never constructs — resolve
        // correctly; a separately-constructed Rp0020FieldAccess only registers the
        // FD file buffers and throws "Field not in any scope" for WS-only fields.
        resultWs = getServiceFieldAccess(service);

        doAnswer(
                        inv -> {
                            Map<String, Object> snap = new LinkedHashMap<>();
                            snap.put("H1-COMPANY", resultWs.getH1Company().trim());
                            snap.put("H1-PAGE", resultWs.getH1Page());
                            snap.put("RP-CODE", resultWs.getRpCode());
                            snap.put("RP-NAME", resultWs.getRpName().trim());
                            snap.put("RP-CATEGORY", resultWs.getRpCategory().trim());
                            snap.put("RP-UNIT", resultWs.getRpUnit().trim());
                            snap.put("RP-COST", resultWs.getRpCost());
                            snap.put("RP-PRICE", resultWs.getRpPrice());
                            snap.put("RT-COUNT", resultWs.getRtCount());
                            snap.put("REP-REC", resultWs.getRepRec().trim());
                            writeSnapshots.add(snap);
                            return null;
                        })
                .when(repf)
                .write();

        stubConsole("", "");
        stubProducts(List.of());
    }

    @AfterEach
    void tearDown() {
        mockedUtility.close();
        writeSnapshots.clear();
    }

    private static Rp0020FieldAccess getServiceFieldAccess(Rp0020Service service) {
        try {
            Field f = Rp0020Service.class.getDeclaredField("ws");
            f.setAccessible(true);
            return (Rp0020FieldAccess) f.get(service);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    /** Stubs the two console prompts (from product code, then to product code) in order. */
    private void stubConsole(String fromInput, String toInput) {
        mockedUtility.when(Utility::readStdinLine).thenReturn(fromInput, toInput);
    }

    private static final class Product {
        final int code;
        final String name;
        final int category;
        final String unit;
        final BigDecimal stdCost;
        final BigDecimal listPrice;
        final int delFlag;

        Product(
                int code,
                String name,
                int category,
                String unit,
                BigDecimal stdCost,
                BigDecimal listPrice,
                int delFlag) {
            this.code = code;
            this.name = name;
            this.category = category;
            this.unit = unit;
            this.stdCost = stdCost;
            this.listPrice = listPrice;
            this.delFlag = delFlag;
        }
    }

    /** Feeds {@code products} through PRODF.readNext()/isAtEnd() in order, then AT END. */
    private void stubProducts(List<Product> products) {
        AtomicInteger idx = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            int i = idx.getAndIncrement();
                            if (i < products.size()) {
                                Product p = products.get(i);
                                prodf.getRecord().setInt("PR-CODE", p.code);
                                prodf.getRecord().setString("PR-NAME", p.name);
                                prodf.getRecord().setInt("PR-CATEGORY", p.category);
                                prodf.getRecord().setString("PR-UNIT", p.unit);
                                prodf.getRecord().setDecimal("PR-STD-COST", p.stdCost);
                                prodf.getRecord().setDecimal("PR-LIST-PRICE", p.listPrice);
                                prodf.getRecord().setInt("PR-DEL-FLAG", p.delFlag);
                                return true;
                            }
                            return false;
                        })
                .when(prodf)
                .readNext();
        doAnswer(inv -> idx.get() > products.size()).when(prodf).isAtEnd();
    }

    // ───────────────────────── happy path ─────────────────────────

    @Test
    void execute_twoProductsOneDeleted_writesDetailLineForActiveProductAndAccumulatesCount() {
        catgf.getRecord().setString("CT-NAME", "Widgets");
        stubProducts(
                List.of(
                        new Product(
                                100,
                                "Alpha Widget",
                                10,
                                "EA",
                                new BigDecimal("5.50"),
                                new BigDecimal("9.99"),
                                0),
                        new Product(
                                200,
                                "Beta Gadget",
                                0,
                                "BX",
                                new BigDecimal("1.00"),
                                new BigDecimal("2.00"),
                                1)));

        service.execute();

        // 5 header writes (idx0-4) + 1 detail (idx5, active product only) + 2 totals
        // writes (idx6 = RPT-RULE separator, idx7 = RT-PROD count line).
        assertThat(writeSnapshots).hasSize(8);
        Map<String, Object> detail = writeSnapshots.get(5);
        assertThat(detail.get("RP-CODE")).isEqualTo(100);
        assertThat(detail.get("RP-NAME")).isEqualTo("Alpha Widget");
        assertThat(detail.get("RP-CATEGORY")).isEqualTo("Widgets");
        assertThat(detail.get("RP-UNIT")).isEqualTo("EA");
        assertThat(detail.get("RP-COST")).isEqualTo(new BigDecimal("5.50"));
        assertThat(detail.get("RP-PRICE")).isEqualTo(new BigDecimal("9.99"));

        Map<String, Object> totals = writeSnapshots.get(7);
        assertThat(totals.get("RT-COUNT")).isEqualTo(1);
        verify(catgf, times(1)).readByKey(any());
        verify(prodf, times(1)).close();
        verify(catgf, times(1)).close();
        verify(repf, times(1)).close();
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }

    @Test
    void execute_multipleActiveProducts_accumulatesCountAcrossAllOfThem() {
        stubProducts(
                List.of(
                        new Product(
                                100,
                                "A",
                                0,
                                "EA",
                                new BigDecimal("1.00"),
                                new BigDecimal("2.00"),
                                0),
                        new Product(
                                101,
                                "B",
                                0,
                                "EA",
                                new BigDecimal("3.00"),
                                new BigDecimal("4.00"),
                                0)));

        service.execute();

        // 5 header writes + 2 details + 2 totals writes (RPT-RULE + RT-PROD) = 9.
        verify(repf, times(9)).write();
        Map<String, Object> totals = writeSnapshots.get(writeSnapshots.size() - 1);
        assertThat(totals.get("RT-COUNT")).isEqualTo(2);
    }

    // ───────────────────────── SYSCF company-name lookup (ground truth: INIT-010)
    // ─────────────────────────

    @Test
    void execute_syscfInvalidKey_keepsDefaultCompanyName() {
        doReturn(true).when(syscf).isInvalidKey();

        service.execute();

        assertThat(writeSnapshots.get(0).get("H1-COMPANY"))
                .isEqualTo("SAKURA Sales Management System");
        verify(syscf, times(1)).readByKey(any());
        verify(syscf, times(1)).close();
    }

    @Test
    void execute_syscfFound_overridesCompanyNameFromSyCompanyName() {
        syscf.getRecord().setString("SY-COMPANY-NAME", "Sakura HQ Trading");

        service.execute();

        assertThat(writeSnapshots.get(0).get("H1-COMPANY")).isEqualTo("Sakura HQ Trading");
    }

    // ───────────────────────── product range parsing (ground truth: PARM-010)
    // ─────────────────────────

    @Test
    void execute_numericRangeFromConsole_parsesFromCodeAndAppliesToCodeBoundary() {
        stubConsole("00000150", "00000300");
        stubProducts(
                List.of(
                        new Product(
                                300,
                                "AtBoundary",
                                0,
                                "EA",
                                new BigDecimal("1.00"),
                                new BigDecimal("2.00"),
                                0),
                        new Product(
                                301,
                                "PastBoundary",
                                0,
                                "EA",
                                new BigDecimal("9.00"),
                                new BigDecimal("9.00"),
                                0)));

        service.execute();

        assertThat(startPrCode.get()).isEqualTo(150);
        verify(prodf, times(2)).readNext();
        Map<String, Object> totals = writeSnapshots.get(writeSnapshots.size() - 1);
        assertThat(totals.get("RT-COUNT")).isEqualTo(1);
    }

    @Test
    void execute_nonNumericFromInput_defaultsProdFromToZero() {
        stubConsole("ABC123", "");
        stubProducts(
                List.of(
                        new Product(
                                1,
                                "First",
                                0,
                                "EA",
                                new BigDecimal("1.00"),
                                new BigDecimal("1.00"),
                                0)));

        service.execute();

        assertThat(startPrCode.get()).isEqualTo(0);
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }

    // ───────────────────────── category lookup (ground truth: LOOKUP-CATG)
    // ─────────────────────────

    @Test
    void execute_categoryZero_leavesCategoryNameBlank() {
        stubProducts(
                List.of(
                        new Product(
                                100,
                                "NoCategory",
                                0,
                                "EA",
                                new BigDecimal("1.00"),
                                new BigDecimal("1.00"),
                                0)));

        service.execute();

        Map<String, Object> detail = writeSnapshots.get(5);
        assertThat(detail.get("RP-CATEGORY")).isEqualTo("");
        verify(catgf, never()).readByKey(any());
    }

    @Test
    void execute_categoryNotFound_setsUnknownCategoryMarker() {
        doReturn(true).when(catgf).isInvalidKey();
        stubProducts(
                List.of(
                        new Product(
                                100,
                                "Widget",
                                99,
                                "EA",
                                new BigDecimal("1.00"),
                                new BigDecimal("1.00"),
                                0)));

        service.execute();

        Map<String, Object> detail = writeSnapshots.get(5);
        assertThat(detail.get("RP-CATEGORY")).isEqualTo("(unknown)");
        verify(catgf, times(1)).readByKey(any());
    }

    // ───────────────────────── page break (ground truth: CHECK-PAGE / PAGE-HEAD)
    // ─────────────────────────

    @Test
    void execute_manyProducts_emitsSecondPageHeaderAfterLineThreshold() {
        List<Product> products = new ArrayList<>();
        // WK-LINE starts at 99 (forces header on the first product); each detail line
        // adds 1 to WK-LINE. A header resets WK-LINE to 6, so 49 more detail lines
        // (6..54) keep it under 55, and the 50th product triggers a second header.
        for (int i = 0; i < 50; i++) {
            products.add(
                    new Product(
                            100 + i,
                            "P" + i,
                            0,
                            "EA",
                            new BigDecimal("1.00"),
                            new BigDecimal("1.00"),
                            0));
        }
        stubProducts(products);

        service.execute();

        // 5(header#1) + 49 details(idx5-53) + 5(header#2, starts idx54) + 1 detail(idx59)
        // + 2 totals(idx60-61) = 62 writes total; second header triggers on the 50th product.
        assertThat(writeSnapshots).hasSize(62);
        assertThat(writeSnapshots.get(0).get("H1-PAGE")).isEqualTo(1);
        assertThat(writeSnapshots.get(54).get("H1-PAGE")).isEqualTo(2);
        Map<String, Object> totals = writeSnapshots.get(61);
        assertThat(totals.get("RT-COUNT")).isEqualTo(50);
    }

    // ───────────────────────── no products selected (ground truth: PRINT-TOTALS)
    // ─────────────────────────

    @Test
    void execute_startInvalidKey_noProductsFoundAndReadNextNeverCalled() {
        doReturn(true).when(prodf).isInvalidKey();

        service.execute();

        verify(prodf, never()).readNext();
        assertThat(writeSnapshots.get(writeSnapshots.size() - 1).get("REP-REC"))
                .isEqualTo("*** NO PRODUCTS SELECTED ***");
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }

    // ───────────────────────── file open lifecycle (ground truth: INIT-RTN / ABEND-RTN)
    // ─────────────────────────

    @Test
    void execute_prodfOpenFailsHardStatus_abortsWithCompletionCode255AndSkipsRemainingOpens() {
        doReturn("99").when(prodf).getFileStatus();

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        verify(abortxService, times(1)).execute(any());
        verify(prodf, times(1)).open(any());
        verify(catgf, never()).open(any());
        verify(repf, never()).open(any());
    }

    @Test
    void execute_prodfOpenStatus35_allowedNoAbortButMarksEofAndSkipsProductLoop() {
        doReturn("35").when(prodf).getFileStatus();

        service.execute();

        verify(abortxService, never()).execute(any());
        verify(catgf, times(1)).open(any());
        verify(repf, times(1)).open(any());
        verify(prodf, never()).start(any(), any());
        verify(prodf, never()).readNext();
        assertThat(writeSnapshots.get(writeSnapshots.size() - 1).get("REP-REC"))
                .isEqualTo("*** NO PRODUCTS SELECTED ***");
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }

    @Test
    void execute_prodfOpenStatus30_allowedNoAbortButMarksEofAndSkipsProductLoop() {
        doReturn("30").when(prodf).getFileStatus();

        service.execute();

        verify(abortxService, never()).execute(any());
        verify(prodf, never()).start(any(), any());
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }

    @Test
    void execute_repfOpenFails_abortsAfterOpeningProdfAndCatgf() {
        doReturn("99").when(repf).getFileStatus();

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        verify(abortxService, times(1)).execute(any());
        verify(prodf, times(1)).open(any());
        verify(catgf, times(1)).open(any());
        verify(repf, never()).write();
    }
}
