package com.generated.orion.oucusin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appruntime.AppResp;
import com.appruntime.AppService;
import com.generated.orion.oucusin.accessor.OucusinFields;
import com.generated.orion.oucusin.model.WorkingStorage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Unit tests for OucusinService, generated from COBOL program OUCUSIN (ORION-CCMS customer-inquiry
 * browse subroutine, EXEC CICS LINK from OCCUSIN). OUCUSIN has no BMS screen: it is driven purely
 * by the KCUSIN-AREA commarea (request half in, response half + KUI-ROWS page out), so every test
 * exercises {@link OucusinService#mainLine(AppService)} through a byte[] commarea and an {@link
 * AppService} mock emulating CICS STARTBR/READNEXT/ENDBR.
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
class OucusinServiceTest {

    private static final String CUSTFILE = "CUSTFILE";

    @Mock private AppService appService;

    private OucusinService service;

    /** Tracks the EIBRESP value the next getEibresp() call should return. */
    private final AtomicInteger nextResp = new AtomicInteger(0);

    @BeforeEach
    void setUp() {
        service = new OucusinService();
        lenient().when(appService.getEibresp()).thenAnswer(inv -> nextResp.get());
    }

    private static final class CustomerRow {
        final int id;
        final String first;
        final String last;
        final int fico;
        final String state;
        final String zip;

        CustomerRow(int id, String first, String last, int fico, String state, String zip) {
            this.id = id;
            this.first = first;
            this.last = last;
            this.fico = fico;
            this.state = state;
            this.zip = zip;
        }
    }

    /** Builds the request half of KCUSIN-AREA and stubs it as the incoming commarea. */
    private void givenRequest(Consumer<OucusinFields> populate) {
        OucusinFields helper = new OucusinFields(new WorkingStorage());
        populate.accept(helper);
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getCommarea()).thenReturn(helper.sliceBytes("KCUSIN-AREA"));
    }

    /** Decodes the KCUSIN-AREA response written back via appService.setCommarea(). */
    private OucusinFields captureResponse() {
        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        verify(appService).setCommarea(captor.capture());
        OucusinFields out = new OucusinFields(new WorkingStorage());
        out.writeBytes("KCUSIN-AREA", captor.getValue());
        return out;
    }

    /**
     * Simulates CUSTFILE STARTBR(GTEQ)/READNEXT/ENDBR over an in-memory, key-ordered dataset:
     * STARTBR positions at the first row whose CU-ID >= the requested key (NOTFND when none
     * qualify); READNEXT streams rows forward until exhausted (ENDFILE).
     */
    private void givenCustomerFile(List<CustomerRow> rows) {
        AtomicInteger cursor = new AtomicInteger(0);
        lenient()
                .doAnswer(
                        inv -> {
                            int startKey = Integer.parseInt((String) inv.getArgument(1));
                            int pos = 0;
                            while (pos < rows.size() && rows.get(pos).id < startKey) {
                                pos++;
                            }
                            cursor.set(pos);
                            nextResp.set(pos < rows.size() ? AppResp.NORMAL : AppResp.NOTFND);
                            return null;
                        })
                .when(appService)
                .startBrowse(eq(CUSTFILE), anyString(), anyInt());

        lenient()
                .doAnswer(
                        inv -> {
                            OucusinFields into = inv.getArgument(1);
                            int pos = cursor.get();
                            if (pos < rows.size()) {
                                CustomerRow r = rows.get(pos);
                                into.setCuId(r.id);
                                into.setCuFirstName(r.first);
                                into.setCuLastName(r.last);
                                into.setCuFicoScore(r.fico);
                                into.setCuAddrState(r.state);
                                into.setCuAddrZip(r.zip);
                                cursor.set(pos + 1);
                                nextResp.set(AppResp.NORMAL);
                            } else {
                                nextResp.set(AppResp.ENDFILE);
                            }
                            return null;
                        })
                .when(appService)
                .readNext(eq(CUSTFILE), any());

        lenient()
                .doAnswer(
                        inv -> {
                            nextResp.set(AppResp.NORMAL);
                            return null;
                        })
                .when(appService)
                .endBrowse(eq(CUSTFILE));
    }

    // ───────────────────────── trivial metadata accessors ─────────────────────────

    @Test
    void getProgramName_returnsOucusin() {
        assertEquals("OUCUSIN", service.getProgramName());
    }

    @Test
    void getButtonDefs_returnsEmptyList_noScreenForLinkSubroutine() {
        assertThat(service.getButtonDefs()).isEmpty();
    }

    @Test
    void registerFsetFields_delegatesToBmsMetadataWithoutError() {
        com.appruntime.AppRunner runner =
                new com.appruntime.AppRunner(java.util.Map.of(), java.util.Map.of());
        service.registerFsetFields(runner);
    }

    // ───────────────────────── commarea marshalling (Object[] vs byte[] vs String)
    // ─────────────────────────

    @Test
    void mainLine_commareaAsObjectArrayWithByteArrayPayload_writesAndReadsBackInPlace() {
        OucusinFields request = new OucusinFields(new WorkingStorage());
        request.setKuiFilter("I");
        request.setKuiIdFrom(100);
        request.setKuiIdTo(200);
        Object[] commarea = new Object[] {request.sliceBytes("KCUSIN-AREA")};
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getCommarea()).thenReturn(commarea);
        givenCustomerFile(List.of(new CustomerRow(150, "A", "ONE", 600, "CA", "90001")));

        service.mainLine(appService);

        assertThat(commarea[0]).isInstanceOf(byte[].class);
        OucusinFields out = new OucusinFields(new WorkingStorage());
        out.writeBytes("KCUSIN-AREA", (byte[]) commarea[0]);
        assertThat(out.getKuiMatchCount()).isEqualTo(1);
        verify(appService, never()).setCommarea(any());
    }

    @Test
    void mainLine_commareaAsObjectArrayWithStringPayload_decodesAndReencodesAsString() {
        OucusinFields request = new OucusinFields(new WorkingStorage());
        request.setKuiFilter("I");
        request.setKuiIdFrom(1);
        request.setKuiIdTo(1);
        Object[] commarea = new Object[] {request.groupToString("KCUSIN-AREA")};
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getCommarea()).thenReturn(commarea);
        doAnswer(
                        inv -> {
                            nextResp.set(AppResp.NOTFND);
                            return null;
                        })
                .when(appService)
                .startBrowse(eq(CUSTFILE), anyString(), anyInt());

        service.mainLine(appService);

        assertThat(commarea[0]).isInstanceOf(String.class);
    }

    @Test
    void
            mainLine_commareaAsPlainStringNotArrayOrBytes_decodesViaSetGroupAndRespondsViaSetCommarea() {
        OucusinFields request = new OucusinFields(new WorkingStorage());
        request.setKuiFilter("I");
        request.setKuiIdFrom(1);
        request.setKuiIdTo(1);
        String commarea = request.groupToString("KCUSIN-AREA");
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getCommarea()).thenReturn(commarea);
        doAnswer(
                        inv -> {
                            nextResp.set(AppResp.NOTFND);
                            return null;
                        })
                .when(appService)
                .startBrowse(eq(CUSTFILE), anyString(), anyInt());

        service.mainLine(appService);

        verify(appService).setCommarea(any(byte[].class));
    }

    // ───────────────────────── BMS metadata pass-throughs ─────────────────────────

    @Test
    void getMapNames_returnsEmptyList_noScreenForLinkSubroutine() {
        assertThat(com.generated.orion.oucusin.metadata.OucusinBmsMetadata.getMapNames()).isEmpty();
    }

    @Test
    void getLayoutResource_returnsWorkingStorageXmlPath() {
        assertThat(com.generated.orion.oucusin.metadata.OucusinBmsMetadata.getLayoutResource())
                .isEqualTo("layout/OUCUSIN_WS.xml");
    }

    @Test
    void getFieldMapping_anyMapName_returnsEmptyMapping() {
        assertThat(
                        com.generated.orion.oucusin.metadata.OucusinBmsMetadata.getFieldMapping(
                                        "ANY")
                                .isEmpty())
                .isTrue();
    }

    // ───────────────────────── filter "I" - customer id range ─────────────────────────

    @Test
    void mainLine_idRangeFilter_allWithinRange_storesRowsAndAggregates() {
        givenRequest(
                f -> {
                    f.setKuiFilter("I");
                    f.setKuiIdFrom(100);
                    f.setKuiIdTo(400);
                });
        givenCustomerFile(
                List.of(
                        new CustomerRow(100, "JOHN", "SMITH", 650, "CA", "90001"),
                        new CustomerRow(200, "MARY", "JONES", 700, "NY", "10001"),
                        new CustomerRow(300, "PAUL", "BROWN", 600, "TX", "73301"),
                        new CustomerRow(400, "ANNE", "WHITE", 750, "FL", "33101")));

        service.mainLine(appService);

        OucusinFields out = captureResponse();
        assertThat(out.getString("KUI-RETURN-CD")).isEqualTo("N");
        assertThat(out.getKuiScanCount()).isEqualTo(4);
        assertThat(out.getKuiMatchCount()).isEqualTo(4);
        assertThat(out.getKuiRowCount()).isEqualTo(4);
        assertThat(out.getKuiFicoTot()).isEqualTo(2700);
        assertThat(out.getKuiFicoMin()).isEqualTo(600);
        assertThat(out.getKuiFicoMax()).isEqualTo(750);
        assertThat(out.getKuiFicoAvg()).isEqualTo(675);
        assertThat(out.getKuiNextKey()).isEqualTo(400);
        assertThat(out.getKurId(1)).isEqualTo(100);
        assertThat(out.getKurName(1).trim()).isEqualTo("JOHN SMITH");
        assertThat(out.getKurState(1).trim()).isEqualTo("CA");
        assertThat(out.getKurZip(1).trim()).isEqualTo("90001");
        assertThat(out.getKurFico(1)).isEqualTo(650);
        verify(appService).endBrowse(eq(CUSTFILE));
    }

    @Test
    void mainLine_idRangeFilter_recordExceedsIdTo_stopsBrowseEarly() {
        givenRequest(
                f -> {
                    f.setKuiFilter("I");
                    f.setKuiIdFrom(100);
                    f.setKuiIdTo(300);
                });
        givenCustomerFile(
                List.of(
                        new CustomerRow(100, "JOHN", "SMITH", 650, "CA", "90001"),
                        new CustomerRow(600, "MARY", "JONES", 700, "NY", "10001")));

        service.mainLine(appService);

        OucusinFields out = captureResponse();
        assertThat(out.getKuiScanCount()).isEqualTo(2);
        assertThat(out.getKuiMatchCount()).isEqualTo(1);
        assertThat(out.getKuiRowCount()).isEqualTo(1);
        verify(appService, times(2)).readNext(eq(CUSTFILE), any());
        verify(appService).endBrowse(eq(CUSTFILE));
    }

    // ───────────────────────── filter "F" - FICO range ─────────────────────────

    @Test
    void mainLine_ficoFilter_boundaryInclusive_matchesWithinRange() {
        givenRequest(
                f -> {
                    f.setKuiFilter("F");
                    f.setKuiFicoFrom(600);
                    f.setKuiFicoTo(700);
                });
        givenCustomerFile(
                List.of(
                        new CustomerRow(10, "A", "ONE", 650, "CA", "90001"),
                        new CustomerRow(20, "B", "TWO", 750, "CA", "90002"),
                        new CustomerRow(30, "C", "THREE", 600, "CA", "90003"),
                        new CustomerRow(40, "D", "FOUR", 700, "CA", "90004")));

        service.mainLine(appService);

        OucusinFields out = captureResponse();
        assertThat(out.getKuiMatchCount()).isEqualTo(3);
        assertThat(out.getKuiRowCount()).isEqualTo(3);
        assertThat(out.getKuiFicoTot()).isEqualTo(1950);
        assertThat(out.getKuiFicoAvg()).isEqualTo(650);
        assertThat(out.getKurId(1)).isEqualTo(10);
        assertThat(out.getKurId(2)).isEqualTo(30);
        assertThat(out.getKurId(3)).isEqualTo(40);
        assertThat(out.getKuiNextKey()).isEqualTo(40);
    }

    // ───────────────────────── filter "S" - state + zip prefix ─────────────────────────

    @Test
    void mainLine_stateZipFilter_zipBlank_matchesOnStateAlone() {
        givenRequest(
                f -> {
                    f.setKuiFilter("S");
                    f.setKuiState("CA");
                    f.setKuiZip("");
                });
        givenCustomerFile(
                List.of(
                        new CustomerRow(1, "A", "ONE", 600, "CA", "90001"),
                        new CustomerRow(2, "B", "TWO", 610, "CA", "10999"),
                        new CustomerRow(3, "C", "THREE", 620, "TX", "90001")));

        service.mainLine(appService);

        OucusinFields out = captureResponse();
        assertThat(out.getKuiMatchCount()).isEqualTo(2);
        assertThat(out.getKurId(1)).isEqualTo(1);
        assertThat(out.getKurId(2)).isEqualTo(2);
    }

    @Test
    void mainLine_stateZipFilter_stateMismatch_neverMatchesRegardlessOfZip() {
        givenRequest(
                f -> {
                    f.setKuiFilter("S");
                    f.setKuiState("CA");
                    f.setKuiZip("900");
                });
        givenCustomerFile(List.of(new CustomerRow(1, "A", "ONE", 600, "NY", "90001")));

        service.mainLine(appService);

        OucusinFields out = captureResponse();
        assertThat(out.getKuiMatchCount()).isEqualTo(0);
        assertThat(out.getKuiRowCount()).isEqualTo(0);
    }

    @Test
    void mainLine_stateZipFilter_zipPrefixMatch_comparesOnlyKeyedPrefixLength() {
        givenRequest(
                f -> {
                    f.setKuiFilter("S");
                    f.setKuiState("CA");
                    f.setKuiZip("900");
                });
        givenCustomerFile(
                List.of(
                        new CustomerRow(1, "A", "ONE", 600, "CA", "90001"),
                        new CustomerRow(2, "B", "TWO", 610, "CA", "90210"),
                        new CustomerRow(3, "C", "THREE", 620, "CA", "90099")));

        service.mainLine(appService);

        OucusinFields out = captureResponse();
        assertThat(out.getKuiMatchCount()).isEqualTo(2);
        assertThat(out.getKurId(1)).isEqualTo(1);
        assertThat(out.getKurId(2)).isEqualTo(3);
    }

    // ───────────────────────── KUI-START-KEY resume / paging ─────────────────────────

    @Test
    void mainLine_startKeyResume_matchedRecordsAtOrBelowStartKeyCountedButNotStored() {
        givenRequest(
                f -> {
                    f.setKuiFilter("I");
                    f.setKuiIdFrom(100);
                    f.setKuiIdTo(500);
                    f.setKuiStartKey(250);
                });
        givenCustomerFile(
                List.of(
                        new CustomerRow(100, "A", "ONE", 600, "CA", "90001"),
                        new CustomerRow(200, "B", "TWO", 610, "CA", "90002"),
                        new CustomerRow(300, "C", "THREE", 620, "CA", "90003"),
                        new CustomerRow(400, "D", "FOUR", 630, "CA", "90004")));

        service.mainLine(appService);

        OucusinFields out = captureResponse();
        assertThat(out.getKuiMatchCount()).isEqualTo(4);
        assertThat(out.getKuiRowCount()).isEqualTo(2);
        assertThat(out.getKurId(1)).isEqualTo(300);
        assertThat(out.getKurId(2)).isEqualTo(400);
        assertThat(out.getKuiNextKey()).isEqualTo(400);
    }

    @Test
    void mainLine_moreThanMaxRowsMatched_capsPageAtWsMaxRowsAndSetsMoreSwitch() {
        List<CustomerRow> rows = new java.util.ArrayList<>();
        for (int id = 1; id <= 15; id++) {
            rows.add(new CustomerRow(id, "F" + id, "L" + id, 600, "CA", "90001"));
        }
        givenRequest(
                f -> {
                    f.setKuiFilter("I");
                    f.setKuiIdFrom(1);
                    f.setKuiIdTo(20);
                });
        givenCustomerFile(rows);

        service.mainLine(appService);

        OucusinFields out = captureResponse();
        assertThat(out.getKuiMatchCount()).isEqualTo(15);
        assertThat(out.getKuiRowCount()).isEqualTo(13);
        assertThat(out.getString("KUI-MORE-SW")).isEqualTo("Y");
        assertThat(out.getKurId(13)).isEqualTo(13);
        assertThat(out.getKuiNextKey()).isEqualTo(13);
        assertThat(out.getKuiFicoTot()).isEqualTo(9000);
        assertThat(out.getKuiFicoAvg()).isEqualTo(600);
    }

    // ───────────────────────── no matches / empty result ─────────────────────────

    @Test
    void mainLine_noMatches_ficoAvgAndFicoMinZeroedByFinalize() {
        givenRequest(
                f -> {
                    f.setKuiFilter("I");
                    f.setKuiIdFrom(900);
                    f.setKuiIdTo(999);
                });
        givenCustomerFile(List.of(new CustomerRow(100, "A", "ONE", 600, "CA", "90001")));

        service.mainLine(appService);

        OucusinFields out = captureResponse();
        assertThat(out.getKuiMatchCount()).isEqualTo(0);
        assertThat(out.getKuiRowCount()).isEqualTo(0);
        assertThat(out.getKuiFicoAvg()).isEqualTo(0);
        assertThat(out.getKuiFicoMin()).isEqualTo(0);
        assertThat(out.getKuiNextKey()).isEqualTo(0);
        assertThat(out.getString("KUI-RETURN-CD")).isEqualTo("N");
    }

    // ───────────────────────── STARTBR RESP handling ─────────────────────────

    @Test
    void mainLine_startBrowseNotFound_endsImmediatelyWithoutError() {
        givenRequest(
                f -> {
                    f.setKuiFilter("I");
                    f.setKuiIdFrom(1);
                    f.setKuiIdTo(1);
                });
        doAnswer(
                        inv -> {
                            nextResp.set(AppResp.NOTFND);
                            return null;
                        })
                .when(appService)
                .startBrowse(eq(CUSTFILE), anyString(), anyInt());

        service.mainLine(appService);

        OucusinFields out = captureResponse();
        assertThat(out.getString("KUI-RETURN-CD")).isEqualTo("N");
        assertThat(out.getKuiRowCount()).isEqualTo(0);
        verify(appService, never()).readNext(anyString(), any());
        verify(appService, never()).endBrowse(anyString());
    }

    @Test
    void mainLine_startBrowseEndfile_endsImmediatelyWithoutError() {
        givenRequest(f -> f.setKuiFilter(""));
        doAnswer(
                        inv -> {
                            nextResp.set(AppResp.ENDFILE);
                            return null;
                        })
                .when(appService)
                .startBrowse(eq(CUSTFILE), anyString(), anyInt());

        service.mainLine(appService);

        OucusinFields out = captureResponse();
        assertThat(out.getString("KUI-RETURN-CD")).isEqualTo("N");
        verify(appService, never()).readNext(anyString(), any());
        verify(appService, never()).endBrowse(anyString());
    }

    @Test
    void mainLine_startBrowseOtherError_setsErrorReturnCode() {
        givenRequest(f -> f.setKuiFilter(""));
        doAnswer(
                        inv -> {
                            nextResp.set(99);
                            return null;
                        })
                .when(appService)
                .startBrowse(eq(CUSTFILE), anyString(), anyInt());

        service.mainLine(appService);

        OucusinFields out = captureResponse();
        assertThat(out.getString("KUI-RETURN-CD")).isEqualTo("E");
        verify(appService, never()).readNext(anyString(), any());
        verify(appService, never()).endBrowse(anyString());
    }

    // ───────────────────────── READNEXT RESP handling ─────────────────────────

    @Test
    void mainLine_readNextOtherError_stopsBrowseButKeepsRowsAlreadyRead() {
        givenRequest(
                f -> {
                    f.setKuiFilter("I");
                    f.setKuiIdFrom(1);
                    f.setKuiIdTo(999);
                });
        doAnswer(
                        inv -> {
                            nextResp.set(AppResp.NORMAL);
                            return null;
                        })
                .when(appService)
                .startBrowse(eq(CUSTFILE), anyString(), anyInt());
        AtomicInteger callCount = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            OucusinFields into = inv.getArgument(1);
                            if (callCount.getAndIncrement() == 0) {
                                into.setCuId(1);
                                into.setCuFirstName("SUE");
                                into.setCuLastName("LEE");
                                into.setCuFicoScore(610);
                                nextResp.set(AppResp.NORMAL);
                            } else {
                                nextResp.set(77);
                            }
                            return null;
                        })
                .when(appService)
                .readNext(eq(CUSTFILE), any());

        service.mainLine(appService);

        OucusinFields out = captureResponse();
        assertThat(out.getString("KUI-RETURN-CD")).isEqualTo("E");
        assertThat(out.getKuiRowCount()).isEqualTo(1);
        assertThat(out.getKurId(1)).isEqualTo(1);
        verify(appService).endBrowse(eq(CUSTFILE));
    }

    // ───────────────────────── CONVERT-GAP ─────────────────────────

    /**
     * CONVERT-GAP: COBOL 3000-FINALIZE does COMPUTE KUI-FICO-AVG ROUNDED = KUI-FICO-TOT /
     * KUI-MATCH-COUNT i.e. the division is evaluated in decimal and then rounded (1007/4 = 251.75
     * -> 252). The Java finalizeSummary() computes ctx.f.getKuiFicoTot() / ctx.f.getKuiMatchCount()
     * first — both long/int operands — which TRUNCATES to 251 in integer arithmetic before
     * Math.round() ever sees a fractional value, so Math.round() is a no-op and the average comes
     * out as 251 instead of the COBOL-correct 252. Expected value below is the COBOL ground truth;
     * this test is expected to FAIL against the current Java implementation, which is the intended
     * signal of the gap (not a broken test).
     */
    @Test
    void mainLine_ficoAverageRounding_cobolRoundsHalfUpButJavaTruncatesBeforeRounding() {
        givenRequest(
                f -> {
                    f.setKuiFilter("F");
                    f.setKuiFicoFrom(0);
                    f.setKuiFicoTo(999);
                });
        givenCustomerFile(
                List.of(
                        new CustomerRow(1, "A", "ONE", 250, "CA", "90001"),
                        new CustomerRow(2, "B", "TWO", 252, "CA", "90002"),
                        new CustomerRow(3, "C", "THREE", 253, "CA", "90003"),
                        new CustomerRow(4, "D", "FOUR", 252, "CA", "90004")));

        service.mainLine(appService);

        OucusinFields out = captureResponse();
        assertThat(out.getKuiFicoTot()).isEqualTo(1007);
        assertThat(out.getKuiMatchCount()).isEqualTo(4);
        // CONVERT-GAP: COBOL ROUNDED gives 252; Java's integer-truncating divide gives 251.
        assertThat(out.getKuiFicoAvg()).isEqualTo(252);
    }
}
