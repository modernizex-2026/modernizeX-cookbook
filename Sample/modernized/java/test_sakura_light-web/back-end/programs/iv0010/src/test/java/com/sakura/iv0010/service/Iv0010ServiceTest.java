package com.sakura.iv0010.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.iv0010.runtime.Iv0010Datasets;
import com.sakura.runtime.ScreenRendererInstance;
import com.sakura.runtime.io.ProdfDataset;
import com.sakura.runtime.io.StokfDataset;
import com.sakura.runtime.io.WhsefDataset;
import com.sakura.runtime.linkage.AbortxLinkParm;
import com.sakura.runtime.record.RuntimeFieldAccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Unit tests for Iv0010Service, generated from COBOL program IV0010 (Stock balance inquiry). Test
 * cases and expected values are derived from the COBOL PROCEDURE DIVISION logic (IV0010.cob), not
 * from the Java implementation.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Timeout(value = 10, unit = TimeUnit.SECONDS)
class Iv0010ServiceTest {

    @Mock private Iv0010Datasets fileSet;

    @Spy private StokfDataset stokf = new StokfDataset();

    @Spy private ProdfDataset prodf = new ProdfDataset();

    @Spy private WhsefDataset whsef = new WhsefDataset();

    @Mock private DateutService dateutService;

    @Mock private AbortxService abortxService;

    @Mock private ScreenRendererInstance renderer;

    private final List<String> screenInteractions = new ArrayList<>();

    private Iv0010Service service;

    @BeforeEach
    void setUp() {
        when(fileSet.getStokf()).thenReturn(stokf);
        when(fileSet.getProdf()).thenReturn(prodf);
        when(fileSet.getWhsef()).thenReturn(whsef);

        doNothing().when(stokf).open(any());
        doNothing().when(stokf).close();
        doReturn(false).when(stokf).start(any(), any());
        doReturn(false).when(stokf).readNext();

        doNothing().when(prodf).open(any());
        doNothing().when(prodf).close();
        doReturn(false).when(prodf).readByKey(any());

        doNothing().when(whsef).open(any());
        doNothing().when(whsef).close();
        doReturn(false).when(whsef).readByKey(any());

        doAnswer(
                        inv -> {
                            com.sakura.runtime.ScreenModels.ScreenDef def = inv.getArgument(0);
                            RuntimeFieldAccess fields = inv.getArgument(1);
                            StringBuilder sb = new StringBuilder("displayScreen:").append(def.name);
                            try {
                                sb.append(":msg=").append(fields.getString("WK-MSG-LINE").trim());
                            } catch (Exception ignored) {
                                // not every screen renders WK-MSG-LINE
                            }
                            if ("DS-DETAIL".equals(def.name)) {
                                try {
                                    sb.append(":pr=")
                                            .append(fields.getString("WK-PR-NAME").trim())
                                            .append(":wh=")
                                            .append(fields.getString("WK-WH-NAME").trim())
                                            .append(":stat=")
                                            .append(fields.getString("WK-STAT").trim());
                                } catch (Exception ignored) {
                                }
                            }
                            screenInteractions.add(sb.toString());
                            return null;
                        })
                .when(renderer)
                .displayScreen(any(), any());

        service = new Iv0010Service(fileSet, dateutService, abortxService, renderer);
        service.setRenderer(renderer);
    }

    /** GKEY-010 ACCEPT DS-KEY reads WK-ORDER, WK-KEY-PROD, WK-KEY-WHSE in that order. */
    private void stubKeyAccept(String order, String prod, String whse) {
        when(renderer.acceptField(any())).thenReturn(order, prod, whse);
    }

    // === MAIN-RTN / GET-KEY happy path =====================================

    @Test
    void mainProgram_singleRecordBrowse_pf3ExitsBrowseThenProgram_happyPath() {
        // GET-KEY #1: order=1, ESTS="00" -> START-BROWSE by SK-PROD, one record found (OK status)
        // BROWSE-LOOP #1: ESTS="03" -> BRW-END
        // GET-KEY #2: ESTS="03" -> END-YES
        when(renderer.acceptField(any())).thenReturn("1", "20030001", "101");
        when(renderer.readEndStatus()).thenReturn("00", "03", "03");

        stokf.buffer().setInt("SK-PROD", 20030001);
        stokf.buffer().setInt("SK-WHSE", 101);
        stokf.buffer().setDecimal("SK-ONHAND", new BigDecimal("100"));
        stokf.buffer().setDecimal("SK-ALLOCATED", new BigDecimal("10"));
        prodf.buffer().setString("PR-NAME", "WIDGET");
        prodf.buffer().setDecimal("PR-SAFETY-STOCK", new BigDecimal("20"));
        prodf.buffer().setDecimal("PR-REORDER-POINT", new BigDecimal("50"));
        whsef.buffer().setString("WH-NAME", "MAIN WAREHOUSE");

        service.execute();

        verify(stokf, times(1)).start(eq("SK-PROD"), eq("NOT LESS THAN"));
        verify(prodf, times(1)).readByKey(any());
        verify(whsef, times(1)).readByKey(any());
        assertThat(service.getCompletionCode()).isEqualTo(0);
        assertThat(screenInteractions)
                .anyMatch(
                        s ->
                                s.contains("DS-DETAIL")
                                        && s.contains("pr=WIDGET")
                                        && s.contains("wh=MAIN WAREHOUSE")
                                        && s.contains("stat=OK"));
        assertThat(screenInteractions)
                .filteredOn(s -> s.startsWith("displayScreen:DS-KEY"))
                .hasSize(2);
    }

    @Test
    void acceptSearchKey_orderTwo_startsByWarehouseKey() {
        // WK-ORDER=2 (ORDER-WP) -> START STOKF KEY IS >= SK-WHSE
        stubKeyAccept("2", "20030001", "101");
        when(renderer.readEndStatus()).thenReturn("00", "03");
        doReturn(true).when(stokf).isInvalidKey();

        service.execute();

        verify(stokf, times(1)).start(eq("SK-WHSE"), eq("NOT LESS THAN"));
        assertThat(screenInteractions)
                .anyMatch(s -> s.contains("msg=No stock records from that key"));
    }

    @Test
    void acceptSearchKey_invalidOrderValue_correctedToProdOrder() {
        // WK-ORDER=9 is neither 1 nor 2 -> COBOL forces WK-ORDER back to 1 (ORDER-PW)
        stubKeyAccept("9", "20030001", "101");
        when(renderer.readEndStatus()).thenReturn("00", "03");
        doReturn(true).when(stokf).isInvalidKey();

        service.execute();

        verify(stokf, times(1)).start(eq("SK-PROD"), eq("NOT LESS THAN"));
    }

    // === START-BROWSE ========================================================

    @Test
    void startBrowse_invalidKey_showsNoRecordsFromKeyMessage() {
        stubKeyAccept("1", "99999999", "999");
        when(renderer.readEndStatus()).thenReturn("00", "03");
        doReturn(true).when(stokf).isInvalidKey();

        service.execute();

        assertThat(screenInteractions)
                .anyMatch(s -> s.contains("msg=No stock records from that key"));
        verify(stokf, never()).readNext();
    }

    @Test
    void startBrowse_endOfFileImmediately_showsNoRecordsFoundMessage() {
        stubKeyAccept("1", "20030001", "101");
        when(renderer.readEndStatus()).thenReturn("00", "03");
        doReturn(true).when(stokf).isAtEnd();

        service.execute();

        assertThat(screenInteractions).anyMatch(s -> s.contains("msg=No stock records found"));
        assertThat(screenInteractions).noneMatch(s -> s.contains("DS-DETAIL"));
    }

    // === BROWSE-LOOP =========================================================

    @Test
    void browseLoop_pf4_endsBrowseForReKey() {
        stubKeyAccept("1", "20030001", "101");
        when(renderer.readEndStatus()).thenReturn("00", "04", "03");
        setupFoundRecord(
                new BigDecimal("100"),
                new BigDecimal("10"),
                new BigDecimal("20"),
                new BigDecimal("50"));

        service.execute();

        assertThat(screenInteractions).anyMatch(s -> s.contains("DS-DETAIL"));
        // only one browse iteration: ESTS=04 sets BRW-END immediately, no second READ-NEXT-REC
        verify(stokf, times(1)).readNext();
    }

    @Test
    void browseLoop_pf6NextThenEndOfList_showsEndOfListMessage() {
        stubKeyAccept("1", "20030001", "101");
        when(renderer.readEndStatus()).thenReturn("00", "06", "03");
        setupFoundRecord(
                new BigDecimal("10"),
                new BigDecimal("5"),
                new BigDecimal("20"),
                new BigDecimal("50"));
        // first READ-NEXT-REC (from START-BROWSE) finds the record; the second
        // (triggered by ESTS="06" inside BROWSE-LOOP) hits end of file.
        doReturn(false, true).when(stokf).isAtEnd();

        service.execute();

        verify(stokf, times(2)).readNext();
        assertThat(screenInteractions)
                .anyMatch(s -> s.contains("msg=End of list - PF3 to re-enter key"));
    }

    @Test
    void browseLoop_unknownFunctionKey_showsHelpMessageAndContinues() {
        stubKeyAccept("1", "20030001", "101");
        // ESTS="99" on first browse display falls into WHEN OTHER (loop continues),
        // ESTS="03" on the second iteration ends the browse.
        when(renderer.readEndStatus()).thenReturn("00", "99", "03", "03");
        setupFoundRecord(
                new BigDecimal("100"),
                new BigDecimal("10"),
                new BigDecimal("20"),
                new BigDecimal("50"));

        service.execute();

        assertThat(screenInteractions)
                .anyMatch(s -> s.contains("msg=ENTER/PF6=next  PF3/PF4=re-key"));
        // WHEN OTHER never re-reads the file
        verify(stokf, times(1)).readNext();
    }

    // === MAIN-RTN default branch ============================================

    @Test
    void mainScreen_invalidFunctionKey_showsInvalidMessageAndRetries() {
        stubKeyAccept("1", "20030001", "101");
        when(renderer.readEndStatus()).thenReturn("05", "03");

        service.execute();

        assertThat(screenInteractions).anyMatch(s -> s.contains("msg=Invalid function key"));
        verify(stokf, never()).start(any(), any());
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }

    // === LOOKUP-PROD / LOOKUP-WHSE ==========================================

    @Test
    void lookupProductName_unknownProduct_setsUnknownProductMessage() {
        stubKeyAccept("1", "20030001", "101");
        when(renderer.readEndStatus()).thenReturn("00", "03", "03");
        setupFoundRecord(
                new BigDecimal("100"),
                new BigDecimal("10"),
                new BigDecimal("20"),
                new BigDecimal("50"));
        doReturn(true).when(prodf).isInvalidKey();

        service.execute();

        assertThat(screenInteractions)
                .anyMatch(s -> s.contains("DS-DETAIL") && s.contains("pr=*** unknown product ***"));
    }

    @Test
    void lookupWarehouseName_unknownWarehouse_setsUnknownWarehouseMessage() {
        stubKeyAccept("1", "20030001", "101");
        when(renderer.readEndStatus()).thenReturn("00", "03", "03");
        setupFoundRecord(
                new BigDecimal("100"),
                new BigDecimal("10"),
                new BigDecimal("20"),
                new BigDecimal("50"));
        doReturn(true).when(whsef).isInvalidKey();

        service.execute();

        assertThat(screenInteractions)
                .anyMatch(
                        s -> s.contains("DS-DETAIL") && s.contains("wh=*** unknown warehouse ***"));
    }

    // === CALC-STATUS =========================================================

    @Test
    void computeStockStatus_reorderBoundary_setsReorderStatus() {
        // WK-AVAIL(70) < WK-SAFETY(50) is false; WK-AVAIL(70) <= WK-REORDER(70) is true -> REORDER
        stubKeyAccept("1", "20030001", "101");
        when(renderer.readEndStatus()).thenReturn("00", "03", "03");
        setupFoundRecord(
                new BigDecimal("80"),
                new BigDecimal("10"),
                new BigDecimal("50"),
                new BigDecimal("70"));

        service.execute();

        assertThat(screenInteractions)
                .anyMatch(s -> s.contains("DS-DETAIL") && s.contains("stat=REORDER"));
    }

    @Test
    void computeStockStatus_belowSafety_setsLowStockStatus() {
        // WK-AVAIL(5) < WK-SAFETY(20) -> LOW STOCK
        stubKeyAccept("1", "20030001", "101");
        when(renderer.readEndStatus()).thenReturn("00", "03", "03");
        setupFoundRecord(
                new BigDecimal("10"),
                new BigDecimal("5"),
                new BigDecimal("20"),
                new BigDecimal("50"));

        service.execute();

        assertThat(screenInteractions)
                .anyMatch(s -> s.contains("DS-DETAIL") && s.contains("stat=LOW STOCK"));
    }

    // === OPEN-FILES / ABEND-RTN =============================================

    @Test
    void openProgramFiles_stokfOpenFails_abortsProgramWithCompletionCode255() {
        doReturn("35").when(stokf).getFileStatus();

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        verify(stokf, times(3)).open(any()); // INPUT, OUTPUT (retry), INPUT
        verify(stokf, times(1)).close();
        verify(prodf, never()).open(any());
        verify(whsef, never()).open(any());

        ArgumentCaptor<AbortxLinkParm> captor = ArgumentCaptor.forClass(AbortxLinkParm.class);
        verify(abortxService, times(1)).execute(captor.capture());
        AbortxLinkParm.Kabend kabend = captor.getValue().getKabend();
        assertThat(kabend.getKaFile().trim()).isEqualTo("STOKF");
        assertThat(kabend.getKaMsgcode()).isEqualTo("EOPEN ");
        assertThat(kabend.getKaDetail().trim()).isEqualTo("File open error");
    }

    // === helpers =============================================================

    private void setupFoundRecord(
            BigDecimal onhand, BigDecimal allocated, BigDecimal safety, BigDecimal reorder) {
        stokf.buffer().setInt("SK-PROD", 20030001);
        stokf.buffer().setInt("SK-WHSE", 101);
        stokf.buffer().setDecimal("SK-ONHAND", onhand);
        stokf.buffer().setDecimal("SK-ALLOCATED", allocated);
        prodf.buffer().setString("PR-NAME", "WIDGET");
        prodf.buffer().setDecimal("PR-SAFETY-STOCK", safety);
        prodf.buffer().setDecimal("PR-REORDER-POINT", reorder);
        whsef.buffer().setString("WH-NAME", "MAIN WAREHOUSE");
    }
}
