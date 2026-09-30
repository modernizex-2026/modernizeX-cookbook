package com.sakura.iv0030.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.iv0030.domain.Iv0030FieldAccess;
import com.sakura.iv0030.runtime.Iv0030Datasets;
import com.sakura.iv0030.screen.ScreenDefs;
import com.sakura.numgen.service.NumgenService;
import com.sakura.runtime.ScreenRendererInstance;
import com.sakura.runtime.io.ProdfDataset;
import com.sakura.runtime.io.SmovfDataset;
import com.sakura.runtime.io.StokfDataset;
import com.sakura.runtime.io.WhsefDataset;
import com.sakura.runtime.linkage.NumgenLinkParm;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
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
 * Unit tests for Iv0030Service, converted from COBOL program IV0030 (Stocktaking). Ground truth for
 * input/expected values: IV0030.cob PROCEDURE DIVISION.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class Iv0030ServiceTest {

    @Mock private Iv0030Datasets fileSet;

    @Spy private StokfDataset stokf = new StokfDataset();

    @Spy private SmovfDataset smovf = new SmovfDataset();

    @Spy private ProdfDataset prodf = new ProdfDataset();

    @Spy private WhsefDataset whsef = new WhsefDataset();

    @Mock private DateutService dateutService;

    @Mock private NumgenService numgenService;

    @Mock private AbortxService abortxService;

    @Mock private ScreenRendererInstance renderer;

    /**
     * Accessor onto the SAME file buffers as the service's own ws — lets tests poke SK-, WH-, PR-
     * fields the way a real READ would have populated them.
     */
    private Iv0030FieldAccess fileWs;

    private Iv0030Service service;

    private final List<String> screenInteractions = new ArrayList<>();

    @BeforeEach
    void setUp() {
        when(fileSet.getStokf()).thenReturn(stokf);
        when(fileSet.getSmovf()).thenReturn(smovf);
        when(fileSet.getProdf()).thenReturn(prodf);
        when(fileSet.getWhsef()).thenReturn(whsef);

        fileWs = new Iv0030FieldAccess(null, fileSet);
        service = new Iv0030Service(fileSet, dateutService, numgenService, abortxService, renderer);
        service.setRenderer(renderer);

        // Default I/O stubs: every dataset op succeeds with no real disk/DB access.
        doNothing().when(stokf).open(any());
        doNothing().when(stokf).close();
        doReturn(true).when(stokf).start(anyString(), anyString());
        doReturn(true).when(stokf).readNext();
        doReturn(false).when(stokf).isAtEnd();
        doReturn(false).when(stokf).isInvalidKey();
        doReturn("00").when(stokf).getFileStatus();
        doReturn(true).when(stokf).readByKey(any());
        doNothing().when(stokf).rewrite();

        doNothing().when(smovf).open(any());
        doNothing().when(smovf).close();
        doNothing().when(smovf).write();
        doReturn(false).when(smovf).isInvalidKey();
        doReturn("00").when(smovf).getFileStatus();

        doNothing().when(prodf).open(any());
        doNothing().when(prodf).close();
        doReturn(true).when(prodf).readByKey(any());
        doReturn(true).when(prodf).isInvalidKey();
        doReturn("23").when(prodf).getFileStatus();

        doNothing().when(whsef).open(any());
        doNothing().when(whsef).close();
        doReturn(true).when(whsef).readByKey(any());
        doReturn(false).when(whsef).isInvalidKey();
        doReturn("00").when(whsef).getFileStatus();

        doAnswer(
                        inv -> {
                            Object def = inv.getArgument(0);
                            Iv0030FieldAccess f = (Iv0030FieldAccess) inv.getArgument(1);
                            screenInteractions.add(
                                    tagFor(def)
                                            + (def == ScreenDefs.getScreen("DS-MSG")
                                                    ? ":" + f.getWkMsgLine().trim()
                                                    : ""));
                            return null;
                        })
                .when(renderer)
                .displayScreen(any(), any());
    }

    @AfterEach
    void tearDown() {
        screenInteractions.clear();
    }

    private static String tagFor(Object def) {
        if (def == ScreenDefs.getScreen("DS-MSG")) return "DS-MSG";
        if (def == ScreenDefs.getScreen("DS-HEADER")) return "DS-HEADER";
        if (def == ScreenDefs.getScreen("DS-FOOTER")) return "DS-FOOTER";
        if (def == ScreenDefs.getScreen("DS-WH")) return "DS-WH";
        if (def == ScreenDefs.getScreen("DS-COUNT")) return "DS-COUNT";
        if (def == ScreenDefs.getScreen("DS-REVIEW")) return "DS-REVIEW";
        if (def == ScreenDefs.getScreen("DS-SUMMARY")) return "DS-SUMMARY";
        return "UNKNOWN";
    }

    private void stubNumgenSuccess() {
        doAnswer(
                        inv -> {
                            NumgenLinkParm p = inv.getArgument(0);
                            p.getKnum().setKnumStatus("00");
                            p.getKnum().setKnumNumber(123456789L);
                            return null;
                        })
                .when(numgenService)
                .execute(any());
    }

    // ── MAIN-010 / GET-WHSE ─────────────────────────────────────────────

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_pf3AtWarehouseEntry_endsProgramImmediately() {
        when(renderer.readEndStatus()).thenReturn("03");
        when(renderer.acceptField(any())).thenReturn("10");

        service.execute();

        verify(stokf).open(any());
        verify(smovf).open(any());
        verify(prodf).open(any());
        verify(whsef).open(any());
        verify(stokf).close();
        verify(smovf).close();
        verify(prodf).close();
        verify(whsef).close();
        verify(whsef, never()).readByKey(any());
        assertThat(screenInteractions).contains("DS-HEADER", "DS-FOOTER", "DS-WH");
        assertThat(screenInteractions).doesNotContain("DS-MSG:Invalid function key");
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_invalidFunctionKey_showsMessageAndContinuesLoop() {
        when(renderer.readEndStatus()).thenReturn("XX", "03");
        when(renderer.acceptField(any())).thenReturn("10");

        service.execute();

        assertThat(screenInteractions).contains("DS-MSG:Invalid function key");
        verify(whsef, never()).readByKey(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_warehouseNotFound_showsMessageThenContinues() {
        doReturn(true).when(whsef).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "03");
        when(renderer.acceptField(any())).thenReturn("999");

        service.execute();

        assertThat(screenInteractions).contains("DS-MSG:Warehouse not found");
        verify(stokf, never()).start(anyString(), anyString());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_warehouseDeleted_showsMessageThenContinues() {
        fileWs.setWhDelFlag(1);
        when(renderer.readEndStatus()).thenReturn("00", "03");
        when(renderer.acceptField(any())).thenReturn("10");

        service.execute();

        assertThat(screenInteractions).contains("DS-MSG:Warehouse is deleted");
        verify(stokf, never()).start(anyString(), anyString());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_noStockRecordsInWarehouse_showsMessage() {
        fileWs.setWhDelFlag(0);
        fileWs.setWhName("MAIN WAREHOUSE");
        doReturn(true).when(stokf).isInvalidKey(); // START finds nothing
        when(renderer.readEndStatus()).thenReturn("00", "03");
        when(renderer.acceptField(any())).thenReturn("10");

        service.execute();

        assertThat(screenInteractions).contains("DS-MSG:No stock records in this warehouse");
        verify(stokf, never()).readNext();
    }

    // ── COUNT-LOOP / COUNT-ONE / STORE-ITEM / REVIEW-COUNT / CONFIRM-POST / POST-COUNT ──

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_happyPath_oneItemCounted_confirmYes_postsAndWritesMovement() {
        fileWs.setWhDelFlag(0);
        fileWs.setWhName("MAIN WAREHOUSE");
        fileWs.setSkWhse(10);
        fileWs.setSkProd(555);
        fileWs.setSkOnhand(BigDecimal.valueOf(100));
        // readNext: 1st call finds the record, 2nd hits end-of-file → COUNT-LOOP stops
        doReturn(false, true).when(stokf).isAtEnd();
        stubNumgenSuccess();

        // ESTS sequence: GET-WHSE(00) -> COUNT-ONE(00=save) -> REVIEW(03=go to post) ->
        // CONFIRM-POST(--) -> GET-WHSE#2(03=finish)
        when(renderer.readEndStatus()).thenReturn("00", "00", "03", "00", "03");
        // acceptField sequence: WK-WHSE-IN, WK-COUNTED, SC-CONF, WK-WHSE-IN(2nd loop)
        when(renderer.acceptField(any())).thenReturn("10", "120", "Y", "10");

        service.execute();

        verify(stokf).rewrite();
        verify(smovf).write();
        assertThat(screenInteractions)
                .contains("DS-COUNT", "DS-REVIEW", "DS-SUMMARY", "DS-MSG:Stocktaking posted");
        assertThat(service.getCompletionCode()).isZero();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_confirmNo_discardsCount_doesNotPost() {
        fileWs.setWhDelFlag(0);
        fileWs.setWhName("MAIN WAREHOUSE");
        fileWs.setSkWhse(10);
        fileWs.setSkProd(555);
        fileWs.setSkOnhand(BigDecimal.valueOf(100));
        doReturn(false, true).when(stokf).isAtEnd();

        when(renderer.readEndStatus()).thenReturn("00", "00", "03", "00", "03");
        when(renderer.acceptField(any())).thenReturn("10", "120", "N", "10");

        service.execute();

        verify(stokf, never()).rewrite();
        verify(smovf, never()).write();
        assertThat(screenInteractions).contains("DS-MSG:Count discarded - not posted");
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_pf4SkipDuringCount_usesBookOnHandAsCountedValue() {
        fileWs.setWhDelFlag(0);
        fileWs.setWhName("MAIN WAREHOUSE");
        fileWs.setSkWhse(10);
        fileWs.setSkProd(555);
        fileWs.setSkOnhand(BigDecimal.valueOf(200));
        doReturn(false, true).when(stokf).isAtEnd();
        stubNumgenSuccess();

        // COUNT-ONE ESTS="04" (PF4 skip): WK-COUNTED forced back to SK-ONHAND, no diff.
        when(renderer.readEndStatus()).thenReturn("00", "04", "03", "00", "03");
        // operator typed a bogus counted value that must be discarded by the "04" branch
        when(renderer.acceptField(any())).thenReturn("10", "999", "Y", "10");

        service.execute();

        // diff = 0 (counted forced back to book on-hand) -> POST-COUNT skips this item entirely
        verify(stokf, never()).rewrite();
        verify(smovf, never()).write();
        assertThat(screenInteractions).contains("DS-MSG:Stocktaking posted");
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_stockWarehouseMismatch_stopsCountLoopWithoutCounting() {
        fileWs.setWhDelFlag(0);
        fileWs.setWhName("MAIN WAREHOUSE");
        fileWs.setSkOnhand(BigDecimal.valueOf(100));
        // scanWarehouseStockItems() sets SK-WHSE = WK-WHSE-IN as the START key just before
        // the read loop, so the "record belongs to a different warehouse" condition has to
        // be simulated on the READ itself (as a real file's next record would), not preset
        // before execute() — a preset gets overwritten by that START-key assignment.
        doAnswer(
                        inv -> {
                            fileWs.setSkWhse(99);
                            fileWs.setSkProd(555);
                            return true;
                        })
                .when(stokf)
                .readNext();
        doReturn(false, true).when(stokf).isAtEnd();

        when(renderer.readEndStatus()).thenReturn("00", "03");
        when(renderer.acceptField(any())).thenReturn("10", "10");

        service.execute();

        assertThat(screenInteractions).contains("DS-MSG:No stock records in this warehouse");
        verify(renderer, never()).acceptField(ScreenDefs.getInput("WK-COUNTED"));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void acceptStockCountEntry_maxItemsReached_stopsAtFiveHundred() {
        fileWs.setWhDelFlag(0);
        fileWs.setWhName("MAIN WAREHOUSE");
        fileWs.setSkWhse(10);
        fileWs.setSkProd(555);
        fileWs.setSkOnhand(BigDecimal.valueOf(1));
        // 501 successive stock records in the target warehouse, then EOF.
        doReturn(true).when(stokf).readNext();
        java.util.concurrent.atomic.AtomicInteger reads =
                new java.util.concurrent.atomic.AtomicInteger(0);
        doAnswer(inv -> reads.incrementAndGet() > 501).when(stokf).isAtEnd();

        // ESTS stays "00" (save & continue / not-Y-confirm) through the whole run, then
        // flips to "03" once the call count is comfortably past everything the 500-item
        // warehouse pass needs (500 count-entries + ~42 review pages + 1 confirm). Any
        // later empty-warehouse re-pass this triggers costs only one more "00" call each,
        // so the exact threshold need not be exact — it just has to be reached eventually.
        java.util.concurrent.atomic.AtomicInteger estsCalls =
                new java.util.concurrent.atomic.AtomicInteger(0);
        doAnswer(inv -> estsCalls.incrementAndGet() > 600 ? "03" : "00")
                .when(renderer)
                .readEndStatus();
        when(renderer.acceptField(any())).thenReturn("10", "1");

        service.execute();

        assertThat(screenInteractions).contains("DS-MSG:Maximum 500 items reached - stop");
        // Exactly 500 items get counted -> 500 rewrites at POST-COUNT time is out of scope here;
        // the assertion focuses on the CONE-010 boundary (READ loop cut off at WK-TCNT>=500).
    }

    // ── OPEN-FILES / ABEND-RTN ──────────────────────────────────────────

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_stokfOpenFails_abendsAndSetsCompletionCode255() {
        doReturn("35").when(stokf).getFileStatus();
        // retry path re-opens with OUTPUT then IO again — still failing.

        service.execute();

        verify(abortxService).execute(any());
        assertThat(service.getCompletionCode()).isEqualTo(255);
        // ABEND happens before GET-WHSE is ever reached.
        assertThat(screenInteractions).doesNotContain("DS-HEADER");
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_prodfOpenNeedsRetry_stillCompletesNormally() {
        // PRODF/WHSEF open failures are NOT abended per COBOL OPENF-010 (only STOKF/SMOVF are).
        doReturn("35", "00").when(prodf).getFileStatus();
        when(renderer.readEndStatus()).thenReturn("03");
        when(renderer.acceptField(any())).thenReturn("10");

        service.execute();

        verify(prodf).open(com.sakura.runtime.DatasetEnums.FileOpenMode.OUTPUT);
        verify(abortxService, never()).execute(any());
        assertThat(service.getCompletionCode()).isZero();
    }
}
