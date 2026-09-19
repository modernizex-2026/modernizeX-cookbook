package com.sakura.iv0040.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.iv0040.domain.Iv0040FieldAccess;
import com.sakura.iv0040.runtime.Iv0040Datasets;
import com.sakura.runtime.DatasetEnums.FileOpenMode;
import com.sakura.runtime.ScreenModels.ScreenDef;
import com.sakura.runtime.ScreenRendererInstance;
import com.sakura.runtime.io.ProdfDataset;
import com.sakura.runtime.io.SmovfDataset;
import com.sakura.runtime.linkage.DateutLinkParm;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for Iv0040Service (COBOL IV0040 — stock movement inquiry), generated from {@code
 * IV0040.cob} PROCEDURE DIVISION as ground truth.
 *
 * <p>Test strategy: SMOVF/PRODF are real dataset objects wrapped with {@code spy()} so the record
 * buffer (and Iv0040FieldAccess, which registers those buffers) behaves exactly as in production;
 * only the I/O methods (open/close/start/readNext/readByKey/getFileStatus/isInvalidKey/isAtEnd) are
 * stubbed so no real file/DB access happens. DATEUT/ABORTX are mocked.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Timeout(value = 10, unit = TimeUnit.SECONDS)
class Iv0040ServiceTest {

    @Mock private ScreenRendererInstance renderer;
    @Mock private DateutService dateutService;
    @Mock private AbortxService abortxService;

    private Iv0040Datasets fileSet;
    private SmovfDataset smovf;
    private ProdfDataset prodf;

    private Iv0040Service service;

    private final Map<String, String> acceptValues = new HashMap<>();
    private final List<String> screenInteractions = new ArrayList<>();
    private final List<String> msgLineHistory = new ArrayList<>();

    /**
     * WK-PR-NAME snapshotted at every displayScreen() call — GKEY-010 resets it to SPACE on the
     * *next* loop iteration, so the final WS state alone can't be used to verify a value set during
     * a prior PROCESS-KEY call.
     */
    private final List<String> prNameHistory = new ArrayList<>();

    /**
     * WK-PAGE-NO snapshotted at every DS-LIST display — also reset to 0 by the next GET-KEY, for
     * the same reason as prNameHistory.
     */
    private final List<Integer> pageNoHistory = new ArrayList<>();

    /** One SMOVF record: prod, date, kind, qty, balAfter, refType, refNo. */
    private static final class Movement {
        final int prod, date, kind, refType;
        final long qty, balAfter, refNo;

        Movement(int prod, int date, int kind, long qty, long balAfter, int refType, long refNo) {
            this.prod = prod;
            this.date = date;
            this.kind = kind;
            this.qty = qty;
            this.balAfter = balAfter;
            this.refType = refType;
            this.refNo = refNo;
        }
    }

    /** Queue of SMOVF records the mocked readNext() walks through, in order. */
    private final List<Movement> smovfQueue = new ArrayList<>();

    private final AtomicInteger smovfCursor = new AtomicInteger(0);

    /** Sequenced ESTS (function key) responses; call n (1-based) -> override, default "00". */
    private final Map<Integer, String> estsOverrides = new HashMap<>();

    private final AtomicInteger estsCallCount = new AtomicInteger(0);

    @BeforeEach
    void setUp() {
        Iv0040Datasets real = new Iv0040Datasets();
        fileSet = spy(real);
        smovf = spy(real.getSmovf());
        prodf = spy(real.getProdf());
        doReturn(smovf).when(fileSet).getSmovf();
        doReturn(prodf).when(fileSet).getProdf();

        doNothing().when(smovf).open(any());
        doNothing().when(prodf).open(any());
        doNothing().when(smovf).close();
        doNothing().when(prodf).close();

        doReturn("00").when(smovf).getFileStatus();
        doReturn("00").when(prodf).getFileStatus();

        // Default: product found.
        doReturn(true).when(prodf).readByKey(any());
        doReturn(false).when(prodf).isInvalidKey();
        prodf.getRecord().setString("PR-NAME", "Default Product");

        // Default: START succeeds (no movements found unless a test queues some).
        doReturn(true).when(smovf).start(any(), any());
        doReturn(false).when(smovf).isInvalidKey();

        // SMOVF readNext() walks smovfQueue, setting SM-* fields per record.
        doAnswer(
                        inv -> {
                            int idx = smovfCursor.incrementAndGet();
                            if (idx <= smovfQueue.size()) {
                                Movement m = smovfQueue.get(idx - 1);
                                smovf.getRecord().setInt("SM-PROD", m.prod);
                                smovf.getRecord().setInt("SM-DATE", m.date);
                                smovf.getRecord().setInt("SM-KIND", m.kind);
                                smovf.getRecord().setDecimal("SM-QTY", BigDecimal.valueOf(m.qty));
                                smovf.getRecord()
                                        .setDecimal("SM-BAL-AFTER", BigDecimal.valueOf(m.balAfter));
                                smovf.getRecord().setInt("SM-REF-TYPE", m.refType);
                                smovf.getRecord().setLong("SM-REF-NO", m.refNo);
                            }
                            return true;
                        })
                .when(smovf)
                .readNext();
        doAnswer(inv -> smovfCursor.get() > smovfQueue.size()).when(smovf).isAtEnd();

        acceptValues.put("WK-KEY-PROD", "0");
        acceptValues.put("WK-KEY-DATE", "0");

        when(renderer.acceptField(any()))
                .thenAnswer(
                        inv -> {
                            var field =
                                    inv.getArgument(
                                            0, com.sakura.runtime.ScreenModels.InputFieldDef.class);
                            return acceptValues.getOrDefault(field.name, "0");
                        });

        when(renderer.readEndStatus())
                .thenAnswer(
                        inv -> {
                            int n = estsCallCount.incrementAndGet();
                            return estsOverrides.getOrDefault(n, "00");
                        });

        doAnswer(
                        inv -> {
                            ScreenDef def = inv.getArgument(0);
                            RuntimeFieldAccess w = inv.getArgument(1);
                            screenInteractions.add("displayScreen:" + def.name);
                            if (w instanceof Iv0040FieldAccess a) {
                                msgLineHistory.add(a.getWkMsgLine().trim());
                                prNameHistory.add(a.getWkPrName().trim());
                                if ("DS-LIST".equals(def.name)) {
                                    pageNoHistory.add(a.getWkPageNo());
                                }
                            }
                            return null;
                        })
                .when(renderer)
                .displayScreen(any(), any());

        // DATEUT: fixed system date 2026-03-15.
        doAnswer(
                        inv -> {
                            DateutLinkParm p = inv.getArgument(0);
                            p.getKdate().setKdDate1(20260315);
                            return null;
                        })
                .when(dateutService)
                .execute(any());

        service = new Iv0040Service(fileSet, dateutService, abortxService, renderer);
    }

    /**
     * Live handle onto the service's WorkingStorage/FD field accessor, obtained from the first
     * displayScreen() call.
     */
    private Iv0040FieldAccess capturedWs() {
        ArgumentCaptor<RuntimeFieldAccess> captor =
                ArgumentCaptor.forClass(RuntimeFieldAccess.class);
        verify(renderer, org.mockito.Mockito.atLeastOnce()).displayScreen(any(), captor.capture());
        return (Iv0040FieldAccess) captor.getAllValues().get(0);
    }

    private void queueMovement(
            int prod, int date, int kind, long qty, long balAfter, int refType, long refNo) {
        smovfQueue.add(new Movement(prod, date, kind, qty, balAfter, refType, refNo));
    }

    // ───────────────────────── happy path (ground truth: PROCESS-KEY / BROWSE-LOOP / FILL-PAGE /
    // FORMAT-LINE) ─────────────────────────

    @Test
    void execute_productFoundWithMovements_showsListPageAndAccumulatesTotals() {
        acceptValues.put("WK-KEY-PROD", "100");
        prodf.getRecord().setString("PR-NAME", "Widget Foo");
        queueMovement(100, 20260101, 10, 5, 105, 1, 123L);
        queueMovement(100, 20260102, 20, -3, 102, 2, 456L);
        estsOverrides.put(2, "03"); // browse-loop ACCEPT DS-LIST -> PF3 ends browsing
        estsOverrides.put(3, "03"); // next GET-KEY ACCEPT -> PF3 ends whole program

        service.execute();

        verify(prodf, times(1)).readByKey(any());
        verify(smovf, times(3)).readNext(); // 2 records + 1 call that hits end-of-file
        assertThat(prNameHistory).contains("Widget Foo");
        assertThat(capturedWs().getWkTotIn()).isEqualTo(5L);
        assertThat(capturedWs().getWkTotOut()).isEqualTo(3L);
        assertThat(capturedWs().getWkRow()).isEqualTo(2);
        assertThat(screenInteractions).contains("displayScreen:DS-LIST");
        assertThat(msgLineHistory).contains("ENTER/PF6=next page  PF3/PF4=re-key");
        assertThat(capturedWs().getCompletionCode()).isEqualTo(0);
    }

    @Test
    void execute_kindCodes_resolveToExpectedLabels() {
        acceptValues.put("WK-KEY-PROD", "200");
        queueMovement(200, 1, 10, 0, 0, 0, 0L);
        queueMovement(200, 2, 20, 0, 0, 0, 0L);
        queueMovement(200, 3, 30, 0, 0, 0, 0L);
        queueMovement(200, 4, 40, 0, 0, 0, 0L);
        queueMovement(200, 5, 50, 0, 0, 0, 0L);
        queueMovement(200, 6, 60, 0, 0, 0, 0L);
        queueMovement(200, 7, 99, 0, 0, 0, 0L);
        estsOverrides.put(2, "03");
        estsOverrides.put(3, "03");

        service.execute();

        Iv0040FieldAccess ws = capturedWs();
        assertThat(ws.getWkList(1)).contains("SaleOut");
        assertThat(ws.getWkList(2)).contains("PurchIn");
        assertThat(ws.getWkList(3)).contains("Adjust");
        assertThat(ws.getWkList(4)).contains("Transfer");
        assertThat(ws.getWkList(5)).contains("RetIn");
        assertThat(ws.getWkList(6)).contains("RetOut");
        assertThat(ws.getWkList(7)).contains("Other");
    }

    // ───────────────────────── edge cases (ground truth: PKEY-010 / LKPR-010 / SBRW-010 / FPG-010)
    // ─────────────────────────

    @Test
    void execute_productCodeZero_showsMessageAndSkipsLookupAndBrowse() {
        // acceptValues default WK-KEY-PROD = "0"
        estsOverrides.put(2, "03");

        service.execute();

        verify(prodf, never()).readByKey(any());
        verify(smovf, never()).start(any(), any());
        assertThat(msgLineHistory).contains("Product code must not be zero");
    }

    @Test
    void execute_invalidFunctionKeyAtSearchPrompt_showsInvalidKeyMessageAndLoopsAgain() {
        estsOverrides.put(1, "09"); // invalid key -> OTHER branch
        estsOverrides.put(2, "03"); // second GET-KEY -> ends

        service.execute();

        verify(prodf, never()).readByKey(any());
        assertThat(msgLineHistory).contains("Invalid function key");
    }

    @Test
    void execute_productNotFound_showsUnknownProductAndNoMovementsMessage() {
        acceptValues.put("WK-KEY-PROD", "999");
        doReturn(true).when(prodf).isInvalidKey();
        doReturn(true).when(smovf).isInvalidKey(); // no movements -> BRW-END immediately, skip loop
        estsOverrides.put(2, "03");

        service.execute();

        assertThat(prNameHistory).contains("*** unknown product ***");
        assertThat(msgLineHistory).contains("No movements for that product");
        verify(smovf, never()).readNext();
    }

    @Test
    void execute_endOfMovementsOnFirstPage_showsEndMessageWithoutDisplayingList() {
        acceptValues.put("WK-KEY-PROD", "300");
        // smovfQueue empty -> readNext() immediately hits end-of-file, WK-ROW stays 0
        estsOverrides.put(2, "03");

        service.execute();

        assertThat(msgLineHistory).contains("End of movements - PF3/PF4");
        assertThat(screenInteractions).doesNotContain("displayScreen:DS-LIST");
        verify(smovf, times(1)).readNext();
    }

    @Test
    void execute_smovfFileStatusNotOkMidRead_stopsPageEarlyWithoutError() {
        acceptValues.put("WK-KEY-PROD", "400");
        queueMovement(400, 1, 10, 1, 1, 0, 0L);
        queueMovement(400, 2, 10, 1, 1, 0, 0L);
        // call#1: SMOVF open "00"; call#2: START "00"; call#3: 1st readNext "00" (row counted);
        // call#4: 2nd readNext "23" (non-"00") -> FILL-PAGE stops before counting this row.
        doReturn("00", "00", "00", "23").when(smovf).getFileStatus();
        estsOverrides.put(2, "03"); // list-page ACCEPT -> PF3 ends browsing
        estsOverrides.put(3, "03"); // next GET-KEY -> ends whole program

        service.execute();

        verify(smovf, times(2)).readNext();
        assertThat(capturedWs().getWkRow()).isEqualTo(1);
    }

    @Test
    void execute_smProdMismatchMidRead_stopsPageWithoutCountingMismatchedRow() {
        acceptValues.put("WK-KEY-PROD", "500");
        queueMovement(500, 1, 10, 1, 1, 0, 0L);
        queueMovement(600, 2, 10, 1, 1, 0, 0L); // different product -> FILL-PAGE stops here
        estsOverrides.put(2, "03");
        estsOverrides.put(3, "03");

        service.execute();

        verify(smovf, times(2)).readNext();
        assertThat(capturedWs().getWkRow()).isEqualTo(1);
    }

    @Test
    void execute_thirteenMovements_paginatesAcrossTwoListPages() {
        acceptValues.put("WK-KEY-PROD", "700");
        for (int i = 0; i < 13; i++) {
            queueMovement(700, 20260101 + i, 10, 1, i, 0, 0L);
        }
        estsOverrides.put(2, "00"); // page 1 ACCEPT -> continue
        estsOverrides.put(3, "03"); // page 2 ACCEPT -> PF3 ends browsing
        estsOverrides.put(4, "03"); // next GET-KEY -> ends whole program

        service.execute();

        long listPages =
                screenInteractions.stream().filter(s -> s.equals("displayScreen:DS-LIST")).count();
        assertThat(listPages).isEqualTo(2);
        assertThat(pageNoHistory).containsExactly(1, 2);
        assertThat(capturedWs().getWkTotIn()).isEqualTo(13L);
    }

    @Test
    void execute_invalidFunctionKeyDuringBrowse_showsRekeyMessageAndContinuesPaging() {
        acceptValues.put("WK-KEY-PROD", "800");
        queueMovement(800, 1, 10, 1, 1, 0, 0L);
        estsOverrides.put(
                2, "09"); // invalid key at DS-LIST ACCEPT -> OTHER branch, message, continue loop
        estsOverrides.put(3, "03"); // second FILL-PAGE call: end-of-movements now -> BRW-END
        estsOverrides.put(4, "03"); // next GET-KEY -> ends whole program

        service.execute();

        assertThat(msgLineHistory).contains("ENTER/PF6=next  PF3/PF4=re-key");
        assertThat(msgLineHistory).contains("End of movements - PF3/PF4");
    }

    // ───────────────────────── file open lifecycle (ground truth: OPEN-FILES / ABEND-RTN)
    // ─────────────────────────

    @Test
    void execute_smovfOpenFailsPersistently_abortsWithCompletionCode255() {
        doReturn("99").when(smovf).getFileStatus();

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        verify(abortxService, times(1)).execute(any());
        verify(smovf, times(1)).open(FileOpenMode.INPUT);
        verify(smovf, never()).open(FileOpenMode.OUTPUT);
        verify(prodf, never()).open(any());
    }

    @Test
    void execute_smovfOpenStatus35_reopensAsOutputThenReopensAsInput() {
        doReturn("35", "00", "00", "00").when(smovf).getFileStatus();
        estsOverrides.put(1, "03");

        service.execute();

        verify(smovf, times(2)).open(FileOpenMode.INPUT);
        verify(smovf, times(1)).open(FileOpenMode.OUTPUT);
        // 1x from the OPENF-010 "35" reopen sequence + 1x from TERM-010 at normal program end.
        verify(smovf, times(2)).close();
        verify(abortxService, never()).execute(any());
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }

    @Test
    void execute_prodfOpenStatus35Persistently_reopensButNeverAborts() {
        // Ground truth: OPENF-010 has NO "IF FSTS NOT = 00" check after opening PRODF at all,
        // unlike SMOVF -> even a persistently bad PRODF file status must never abort the program.
        doReturn("35").when(prodf).getFileStatus();
        estsOverrides.put(1, "03");

        service.execute();

        verify(prodf, times(2)).open(FileOpenMode.INPUT);
        verify(prodf, times(1)).open(FileOpenMode.OUTPUT);
        // 1x from the OPENF-010 "35" reopen sequence + 1x from TERM-010 at normal program end.
        verify(prodf, times(2)).close();
        verify(abortxService, never()).execute(any());
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }
}
