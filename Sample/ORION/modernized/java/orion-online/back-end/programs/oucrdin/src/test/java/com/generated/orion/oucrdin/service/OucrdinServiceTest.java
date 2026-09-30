package com.generated.orion.oucrdin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appruntime.AppService;
import com.generated.orion.oucrdin.accessor.OucrdinFields;
import com.generated.orion.oucrdin.model.WorkingStorage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for OucrdinService, generated from COBOL program OUCRDIN. All business logic is
 * private and is exercised solely through the public {@link OucrdinService#mainLine(AppService)}
 * entry point, driven by an {@link AppService} mock that emulates CICS STARTBR/READNEXT/ENDBR/LINK.
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
class OucrdinServiceTest {

    private static final String CARDFILE = "CARDFILE";
    private static final DateTimeFormatter ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Mock private AppService appService;

    private OucrdinService service;

    /** Tracks the EIBRESP value the next getEibresp() call should return. */
    private final AtomicInteger nextResp = new AtomicInteger(0);

    @BeforeEach
    void setUp() {
        service = new OucrdinService();
        lenient().when(appService.getEibresp()).thenAnswer(inv -> nextResp.get());
    }

    @AfterEach
    void tearDown() {
        nextResp.set(0);
    }

    /**
     * Builds the serialized KCRDIN-AREA request (filter / start key / want-kpi flag). A null
     * startKey means "first page" -> KCI-START-KEY must be LOW-VALUES (the field's XML-layout
     * default is SPACES, not LOW-VALUES, so it is filled explicitly here to match what a real
     * caller does per the COBOL comment on KCI-START-KEY NOT = LOW-VALUES).
     */
    private String commarea(String filter, String startKey, String wantKpi) {
        OucrdinFields helper = new OucrdinFields(new WorkingStorage());
        helper.setKciFilter(filter);
        if (startKey != null) {
            helper.setKciStartKey(startKey);
        } else {
            helper.fillLowValues("KCI-START-KEY");
        }
        helper.setKciWantKpi(wantKpi);
        return helper.getKcrdinArea();
    }

    private void givenRequest(String filter, String startKey, String wantKpi) {
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getCommarea()).thenReturn(commarea(filter, startKey, wantKpi));
    }

    /** Decodes the KCRDIN-AREA bytes handed back via setCommarea() into a fresh accessor. */
    private OucrdinFields captureResponse() {
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(appService).setCommarea(captor.capture());
        byte[] bytes = (byte[]) captor.getValue();
        OucrdinFields out = new OucrdinFields(new WorkingStorage());
        out.writeBytes("KCRDIN-AREA", bytes);
        return out;
    }

    /** A single card row: number, account id, name, expiry (yyyy-MM-dd), active status. */
    private record Card(String num, long acct, String name, String expiry, String status) {}

    /**
     * Stubs STARTBR on CARDFILE to return the given EIBRESP for the NEXT call only (0=NORMAL,
     * 13=NOTFND, 20=ENDFILE, other=unexpected error).
     */
    private void givenStartBrowseResp(int resp) {
        doAnswer(
                        inv -> {
                            nextResp.set(resp);
                            return null;
                        })
                .when(appService)
                .startBrowse(eq(CARDFILE), anyString(), eq(0));
    }

    /** Stubs READNEXT on CARDFILE to hand back the given cards in order, then ENDFILE (resp 20). */
    private void givenReadNextCards(Card... cards) {
        AtomicInteger idx = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            OucrdinFields into = inv.getArgument(1);
                            int i = idx.getAndIncrement();
                            if (i < cards.length) {
                                Card c = cards[i];
                                into.setCdNum(c.num());
                                into.setCdAcctId(c.acct());
                                into.setCdEmbossedName(c.name());
                                into.setCdExpiryDate(c.expiry());
                                into.setCdActiveStatus(c.status());
                                nextResp.set(0);
                            } else {
                                nextResp.set(20);
                            }
                            return null;
                        })
                .when(appService)
                .readNext(eq(CARDFILE), any());
    }

    /**
     * Stubs STARTBR (always resp 0) + READNEXT over the SAME card list for every browse session,
     * resetting the read cursor on each STARTBR -- needed when a test drives two independent
     * full-file passes (the page browse and the 6000-COMPUTE-KPI population browse), which a single
     * shared-cursor stub cannot serve correctly.
     */
    private void givenCardFileRescannedOnEachBrowse(Card... cards) {
        AtomicInteger idx = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            idx.set(0);
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .startBrowse(eq(CARDFILE), anyString(), eq(0));
        doAnswer(
                        inv -> {
                            OucrdinFields into = inv.getArgument(1);
                            int i = idx.getAndIncrement();
                            if (i < cards.length) {
                                Card c = cards[i];
                                into.setCdNum(c.num());
                                into.setCdAcctId(c.acct());
                                into.setCdEmbossedName(c.name());
                                into.setCdExpiryDate(c.expiry());
                                into.setCdActiveStatus(c.status());
                                nextResp.set(0);
                            } else {
                                nextResp.set(20);
                            }
                            return null;
                        })
                .when(appService)
                .readNext(eq(CARDFILE), any());
    }

    private static String today(int offsetDays) {
        return LocalDate.now().plusDays(offsetDays).format(ISO);
    }

    // ───────────────────────── 1000-INITIALISE / 2000-COMPUTE-CUTOFF ─────────────────────────

    @Test
    void mainLine_noPriorState_initialisesReturnCodeAndCounters() {
        givenRequest("A", null, "N");
        givenStartBrowseResp(13);

        service.mainLine(appService);

        OucrdinFields out = captureResponse();
        assertThat(out.getKciReturnCd()).isEqualTo("N");
        assertThat(out.getKciRowCount()).isZero();
        assertThat(out.getKciScanCount()).isZero();
        assertThat(out.getKciActiveCnt()).isZero();
        assertThat(out.getKciInactiveCnt()).isZero();
        assertThat(out.getString("KCI-MORE-SW")).isEqualTo("N");
    }

    // ───────────────────────── 4500/4510 filter=ALL ─────────────────────────

    @Test
    void mainLine_filterAll_matchesEveryCardAndReportsCounts() {
        givenRequest("A", null, "N");
        givenStartBrowseResp(0);
        givenReadNextCards(
                new Card("1111222233334444", 100L, "ALICE", today(10), "Y"),
                new Card("1111222233335555", 200L, "BOB", today(-5), "N"));

        service.mainLine(appService);

        OucrdinFields out = captureResponse();
        assertThat(out.getKciRowCount()).isEqualTo(2);
        assertThat(out.getKciScanCount()).isEqualTo(2);
        assertThat(out.getKciRNum(1)).isEqualTo("1111222233334444");
        assertThat(out.getKciRAcct(1)).isEqualTo(100L);
        assertThat(out.getKciRName(1).trim()).isEqualTo("ALICE");
        assertThat(out.getKciRStatus(1)).isEqualTo("Y");
        assertThat(out.getKciRNum(2)).isEqualTo("1111222233335555");
        assertThat(out.getString("KCI-MORE-SW")).isEqualTo("N");
        assertThat(out.getKciNextKey().trim()).isEqualTo("1111222233335555");
        assertThat(out.getKciReturnCd()).isEqualTo("N");
        verify(appService).endBrowse(eq(CARDFILE));
    }

    // ───────────────────────── 4520 filter=ACTIVE ─────────────────────────

    @Test
    void mainLine_filterActive_onlyKeepsActiveStatusCards() {
        givenRequest("Y", null, "N");
        givenStartBrowseResp(0);
        givenReadNextCards(
                new Card("1111222233334444", 100L, "ALICE", today(10), "Y"),
                new Card("1111222233335555", 200L, "BOB", today(10), "N"));

        service.mainLine(appService);

        OucrdinFields out = captureResponse();
        assertThat(out.getKciRowCount()).isEqualTo(1);
        assertThat(out.getKciRNum(1)).isEqualTo("1111222233334444");
        assertThat(out.getKciScanCount()).isEqualTo(2);
    }

    // ───────────────────────── 4530 filter=INACTIVE ─────────────────────────

    @Test
    void mainLine_filterInactive_keepsAnyNonYStatus() {
        givenRequest("N", null, "N");
        givenStartBrowseResp(0);
        givenReadNextCards(
                new Card("1111222233334444", 100L, "ALICE", today(10), "Y"),
                new Card("1111222233335555", 200L, "BOB", today(10), "N"));

        service.mainLine(appService);

        OucrdinFields out = captureResponse();
        assertThat(out.getKciRowCount()).isEqualTo(1);
        assertThat(out.getKciRNum(1)).isEqualTo("1111222233335555");
    }

    // ───────────────────────── 4540 filter=EXPIRING-SOON ─────────────────────────

    @Test
    void mainLine_filterExpiring_matchesWithinNext60DaysInclusive() {
        givenRequest("X", null, "N");
        givenStartBrowseResp(0);
        givenReadNextCards(
                new Card("1111222233330001", 1L, "TODAY", today(0), "Y"),
                new Card("1111222233330002", 2L, "SIXTY", today(60), "Y"),
                new Card("1111222233330003", 3L, "PAST", today(-1), "Y"),
                new Card("1111222233330004", 4L, "TOOFAR", today(61), "Y"),
                new Card("1111222233330005", 5L, "BADDATE", "20XX-01-01", "Y"));

        service.mainLine(appService);

        OucrdinFields out = captureResponse();
        assertThat(out.getKciRowCount()).isEqualTo(2);
        assertThat(out.getKciRNum(1)).isEqualTo("1111222233330001");
        assertThat(out.getKciRNum(2)).isEqualTo("1111222233330002");
        assertThat(out.getKciScanCount()).isEqualTo(5);
    }

    // ───────────────────────── 4500 unknown filter defaults to ALL ─────────────────────────

    @Test
    void mainLine_unknownFilterValue_defaultsToMatchAll() {
        givenRequest("Z", null, "N");
        givenStartBrowseResp(0);
        givenReadNextCards(new Card("1111222233334444", 100L, "ALICE", today(10), "N"));

        service.mainLine(appService);

        OucrdinFields out = captureResponse();
        assertThat(out.getKciRowCount()).isEqualTo(1);
    }

    // ───────────────────────── 3000-BROWSE-DRIVER: page cap ─────────────────────────

    @Test
    void mainLine_moreThanMaxRowsAvailable_stopsAtThirteenAndSetsMoreSwY() {
        givenRequest("A", null, "N");
        givenStartBrowseResp(0);
        Card[] cards = new Card[15];
        for (int i = 0; i < cards.length; i++) {
            cards[i] = new Card(String.format("11112222333%05d", i), i, "NAME" + i, today(10), "Y");
        }
        givenReadNextCards(cards);

        service.mainLine(appService);

        OucrdinFields out = captureResponse();
        assertThat(out.getKciRowCount()).isEqualTo(13);
        assertThat(out.getString("KCI-MORE-SW")).isEqualTo("Y");
        assertThat(out.getKciNextKey().trim()).isEqualTo(cards[12].num());
        verify(appService, times(13)).readNext(eq(CARDFILE), any());
        verify(appService).endBrowse(eq(CARDFILE));
    }

    // ───────────────────────── 3000-BROWSE-DRIVER: resume paging skips first read
    // ─────────────────────────

    @Test
    void mainLine_resumeKeyProvided_skipsAlreadySeenRowBeforeGathering() {
        givenRequest("A", "1111222233330000", "N");
        givenStartBrowseResp(0);
        givenReadNextCards(
                new Card("1111222233330000", 1L, "SEEN", today(10), "Y"),
                new Card("1111222233330001", 2L, "NEW", today(10), "Y"));

        service.mainLine(appService);

        OucrdinFields out = captureResponse();
        // Per COBOL 3000-BROWSE-DRIVER: KCI-START-KEY not LOW-VALUES -> one extra READNEXT
        // (3300-READ-NEXT) re-reads the already-shown row before the gather loop starts.
        assertThat(out.getKciRowCount()).isEqualTo(1);
        assertThat(out.getKciRNum(1)).isEqualTo("1111222233330001");
        verify(appService, times(3)).readNext(eq(CARDFILE), any());
    }

    // ───────────────────────── 3100-START-BROWSE: NOTFND / ENDFILE ─────────────────────────

    @Test
    void mainLine_startBrowseNotFound_skipsGatherAndReturnsNoRowsWithoutError() {
        givenRequest("A", null, "N");
        givenStartBrowseResp(13);

        service.mainLine(appService);

        OucrdinFields out = captureResponse();
        assertThat(out.getKciRowCount()).isZero();
        assertThat(out.getKciReturnCd()).isEqualTo("N");
        verify(appService, never()).readNext(anyString(), any());
        verify(appService, never()).endBrowse(anyString());
    }

    @Test
    void mainLine_startBrowseEndfile_skipsGatherAndReturnsNoRowsWithoutError() {
        givenRequest("A", null, "N");
        givenStartBrowseResp(20);

        service.mainLine(appService);

        OucrdinFields out = captureResponse();
        assertThat(out.getKciRowCount()).isZero();
        assertThat(out.getKciReturnCd()).isEqualTo("N");
        verify(appService, never()).endBrowse(anyString());
    }

    // ───────────────────────── 3100-START-BROWSE: unexpected error ─────────────────────────

    @Test
    void mainLine_startBrowseUnexpectedError_setsErrorReturnCodeAndSkipsBrowse() {
        givenRequest("A", null, "N");
        givenStartBrowseResp(99);

        service.mainLine(appService);

        OucrdinFields out = captureResponse();
        assertThat(out.getKciReturnCd()).isEqualTo("E");
        assertThat(out.getKciRowCount()).isZero();
        verify(appService, never()).readNext(anyString(), any());
        verify(appService, never()).endBrowse(anyString());
    }

    // ───────────────────────── 3300-READ-NEXT: unexpected error ─────────────────────────

    @Test
    void mainLine_readNextUnexpectedError_setsErrorReturnCodeButStillEndsBrowse() {
        givenRequest("A", null, "N");
        givenStartBrowseResp(0);
        doAnswer(
                        inv -> {
                            nextResp.set(99);
                            return null;
                        })
                .when(appService)
                .readNext(eq(CARDFILE), any());

        service.mainLine(appService);

        OucrdinFields out = captureResponse();
        assertThat(out.getKciReturnCd()).isEqualTo("E");
        assertThat(out.getKciRowCount()).isZero();
        // Per COBOL 3000-BROWSE-DRIVER: 3400-END-BROWSE runs whenever BR-STARTED, regardless
        // of how the gather loop terminated (including via a READNEXT error).
        verify(appService).endBrowse(eq(CARDFILE));
    }

    // ───────────────────────── 6000-COMPUTE-KPI ─────────────────────────

    @Test
    void mainLine_kpiRequested_talliesActiveAndInactiveOverWholeFileSeparatelyFromPageBrowse() {
        givenRequest("A", null, "Y");
        givenCardFileRescannedOnEachBrowse(
                new Card("1111222233330001", 1L, "ONE", today(10), "Y"),
                new Card("1111222233330002", 2L, "TWO", today(10), "N"),
                new Card("1111222233330003", 3L, "THREE", today(10), "Y"));

        service.mainLine(appService);

        OucrdinFields out = captureResponse();
        // filter=A -> KPI pass (also filter=A) matches every scanned row: 2 active, 1 inactive.
        assertThat(out.getKciActiveCnt()).isEqualTo(2);
        assertThat(out.getKciInactiveCnt()).isEqualTo(1);
        // Page browse + KPI browse are two independent STARTBR/ENDBR pairs.
        verify(appService, times(2)).startBrowse(eq(CARDFILE), anyString(), eq(0));
        verify(appService, times(2)).endBrowse(eq(CARDFILE));
    }

    @Test
    void mainLine_kpiNotRequested_skipsKpiPassAndLeavesTalliesZero() {
        givenRequest("A", null, "N");
        givenStartBrowseResp(0);
        givenReadNextCards(new Card("1111222233330001", 1L, "ONE", today(10), "Y"));

        service.mainLine(appService);

        OucrdinFields out = captureResponse();
        assertThat(out.getKciActiveCnt()).isZero();
        assertThat(out.getKciInactiveCnt()).isZero();
        verify(appService, times(1)).startBrowse(eq(CARDFILE), anyString(), eq(0));
        verify(appService, times(1)).endBrowse(eq(CARDFILE));
    }

    @Test
    void mainLine_kpiRequestedButKpiStartBrowseFails_leavesTalliesZeroWithoutError() {
        givenRequest("A", null, "Y");
        // Page browse succeeds with zero rows (NOTFND); KPI browse (2nd startBrowse call) fails.
        AtomicInteger callCount = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            nextResp.set(callCount.getAndIncrement() == 0 ? 13 : 99);
                            return null;
                        })
                .when(appService)
                .startBrowse(eq(CARDFILE), anyString(), eq(0));

        service.mainLine(appService);

        OucrdinFields out = captureResponse();
        assertThat(out.getKciActiveCnt()).isZero();
        assertThat(out.getKciInactiveCnt()).isZero();
        // Per COBOL 6100-KPI-START: WHEN OTHER only sets BR-END, no KCI-ERROR -- unlike
        // 3100-START-BROWSE's page browse, a failed KPI browse does not flag KCI-RETURN-CD.
        assertThat(out.getKciReturnCd()).isEqualTo("N");
        verify(appService, never()).endBrowse(eq(CARDFILE));
    }

    // ───────────────────────── trivial metadata accessors ─────────────────────────

    @Test
    void getProgramName_returnsOucrdin() {
        assertEquals("OUCRDIN", service.getProgramName());
    }

    @Test
    void getButtonDefs_delegatesToMetadataWithoutError() {
        assertNotNull(service.getButtonDefs());
    }
}
