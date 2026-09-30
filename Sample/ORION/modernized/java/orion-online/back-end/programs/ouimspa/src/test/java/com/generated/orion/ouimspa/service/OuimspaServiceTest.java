package com.generated.orion.ouimspa.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.appruntime.AppService;
import com.appruntime.DliService;
import com.generated.orion.ouimspa.accessor.OuimspaFields;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Ground truth: OUIMSPA.cbl (Pending Authorization DL/I access). No convert-gap found between COBOL
 * paragraphs and OuimspaService — every test below asserts the COBOL-expected outcome and is
 * expected to PASS.
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 10, unit = TimeUnit.SECONDS)
class OuimspaServiceTest {

    private static final int COMMAREA_SIZE = 692;
    private static final int CA_ERR_FLG_OFFSET = 99;
    private static final int CA_ERR_MSG_OFFSET = 100;
    private static final int CA_ERR_MSG_SIZE = 80;
    private static final int CA_WORK_AREA_OFFSET = 180;

    private static final int WS_PL_FUNC_OFFSET = 0;
    private static final int WS_PL_KEY_OFFSET = 4;
    private static final int WS_PL_STATUS_OFFSET = 20;
    private static final int WS_PL_MSG_OFFSET = 22;
    private static final int WS_PL_DLI_STAT_OFFSET = 62;
    private static final int WS_PL_SEGMENT_OFFSET = 64;

    private static final String ODLI_GU = "GU  ";
    private static final String ODLI_GHU = "GHU ";
    private static final String ODLI_GN = "GN  ";

    @Mock private AppService appService;

    @Mock private DliService dliService;

    private final OuimspaService service = new OuimspaService();

    @BeforeEach
    void setUp() {
        given(appService.getEibcalen()).willReturn(COMMAREA_SIZE);
    }

    // ---------- field / buffer helpers (offsets from OUIMSPA_WS.xml) ----------

    private static String pad(String value, int length) {
        String v = value == null ? "" : value;
        if (v.length() >= length) {
            return v.substring(0, length);
        }
        return v + " ".repeat(length - v.length());
    }

    private static void putAt(char[] arr, int offset, String value) {
        for (int i = 0; i < value.length(); i++) {
            arr[offset + i] = value.charAt(i);
        }
    }

    private static String buildSegment(
            String authId, String cardNum, String acctId, String status) {
        char[] seg = new char[200];
        java.util.Arrays.fill(seg, ' ');
        putAt(seg, 0, pad(authId, 16));
        putAt(seg, 16, pad(cardNum, 16));
        putAt(seg, 32, pad(acctId, 11));
        putAt(seg, 130, pad(status, 1));
        return new String(seg);
    }

    private static String buildCommarea(String func, String key, String segment) {
        char[] area = new char[COMMAREA_SIZE];
        java.util.Arrays.fill(area, ' ');
        String wsPauLink =
                pad(func, 4)
                        + pad(key, 16)
                        + pad("", 2)
                        + pad("", 40)
                        + pad("", 2)
                        + pad(segment, 200);
        putAt(area, CA_WORK_AREA_OFFSET, wsPauLink);
        return new String(area);
    }

    private static String sub(String s, int offset, int len) {
        return s.substring(offset, offset + len);
    }

    private static String wsPlKey(String out) {
        return sub(out, CA_WORK_AREA_OFFSET + WS_PL_KEY_OFFSET, 16);
    }

    private static String wsPlStatus(String out) {
        return sub(out, CA_WORK_AREA_OFFSET + WS_PL_STATUS_OFFSET, 2);
    }

    private static String wsPlMsg(String out) {
        return sub(out, CA_WORK_AREA_OFFSET + WS_PL_MSG_OFFSET, 40);
    }

    private static String wsPlDliStat(String out) {
        return sub(out, CA_WORK_AREA_OFFSET + WS_PL_DLI_STAT_OFFSET, 2);
    }

    private static String wsPlSegment(String out) {
        return sub(out, CA_WORK_AREA_OFFSET + WS_PL_SEGMENT_OFFSET, 200);
    }

    private static String caErrFlg(String out) {
        return sub(out, CA_ERR_FLG_OFFSET, 1);
    }

    private static String caErrMsg(String out) {
        return sub(out, CA_ERR_MSG_OFFSET, CA_ERR_MSG_SIZE);
    }

    private static void assertMsg(String out, String expected) {
        assertEquals(pad(expected, 40), wsPlMsg(out));
    }

    private static void assertOkResult(String out) {
        assertEquals("N", caErrFlg(out));
        assertEquals(pad("", 80), caErrMsg(out));
    }

    private static void assertFailedResult(String out, String expectedMsg) {
        assertEquals("Y", caErrFlg(out));
        assertEquals(pad(expectedMsg, 80), caErrMsg(out));
    }

    // ---------- DL/I call stubbing (matches any function / SSA vararg length) ----------

    /** status only; no simulated segment data written back into the IO area. */
    private void stubDli(String... statuses) {
        AtomicInteger idx = new AtomicInteger(0);
        given(appService.getDliService()).willReturn(dliService);
        given(dliService.cbltdli(anyString(), any(), any(String[].class)))
                .willAnswer(inv -> statuses[idx.getAndIncrement()]);
    }

    /** single successful GU/GHU call that also simulates DL/I populating the segment. */
    private void stubDliWithSegment(String status, String authId, String cardNum) {
        given(appService.getDliService()).willReturn(dliService);
        given(dliService.cbltdli(anyString(), any(), any(String[].class)))
                .willAnswer(
                        inv -> {
                            OuimspaFields f = (OuimspaFields) inv.getArgument(1);
                            f.setPaAuthId(authId);
                            f.setPaCardNum(cardNum);
                            return status;
                        });
    }

    // ---------- invocation ----------

    private String runMainLine(String commareaIn) {
        given(appService.getCommarea()).willReturn(commareaIn);
        service.mainLine(appService);
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(appService).setCommarea(captor.capture());
        return new String((byte[]) captor.getValue(), StandardCharsets.ISO_8859_1);
    }

    // ==================== 2000-INQUIRE ====================

    @Test
    void inquire_found_returnsSegmentAndOkStatus() {
        String key = pad("AUTH00000001", 16);
        stubDliWithSegment("  ", key, pad("CARD0000000000001", 16));

        String out = runMainLine(buildCommarea("INQ ", key, ""));

        assertEquals("  ", wsPlStatus(out));
        assertMsg(out, "Pending authorization retrieved.");
        assertEquals(key, wsPlKey(out));
        assertEquals("  ", wsPlDliStat(out));
        assertOkResult(out);
        verify(dliService, times(1)).cbltdli(anyString(), any(), any(String[].class));
    }

    @Test
    void inquire_notFound_returnsGeStatus() {
        String key = pad("AUTH00000002", 16);
        stubDli("GE");

        String out = runMainLine(buildCommarea("INQ ", key, ""));

        assertEquals("GE", wsPlStatus(out));
        assertMsg(out, "No pending authorization for that id.");
        assertEquals(pad("", 200), wsPlSegment(out));
        assertFailedResult(out, "No pending authorization for that id.");
    }

    @Test
    void inquire_dliError_returnsErStatusWithMessage() {
        String key = pad("AUTH00000003", 16);
        stubDli("AD");

        String out = runMainLine(buildCommarea("INQ ", key, ""));

        assertEquals("ER", wsPlStatus(out));
        assertMsg(out, "DL/I error status=AD func=" + ODLI_GU);
        assertEquals("AD", wsPlDliStat(out));
        assertFailedResult(out, "DL/I error status=AD func=" + ODLI_GU);
    }

    @Test
    void inquire_missingKey_returnsBadFunctionWithoutDliCall() {
        String out = runMainLine(buildCommarea("INQ ", "", ""));

        assertEquals("BF", wsPlStatus(out));
        assertMsg(out, "Authorization id is required.");
        assertFailedResult(out, "Authorization id is required.");
        verifyNoInteractions(dliService);
    }

    @Test
    void mainLine_unknownFunction_returnsBadFunctionWithoutDliCall() {
        String out = runMainLine(buildCommarea("XXX ", pad("AUTH00000001", 16), ""));

        assertEquals("BF", wsPlStatus(out));
        assertMsg(out, "Unknown DL/I request function code.");
        assertFailedResult(out, "Unknown DL/I request function code.");
        verifyNoInteractions(dliService);
    }

    // ==================== 2100-BROWSE-NEXT ====================

    @Test
    void browseNext_emptyKey_callsGetNextOnly() {
        String returnedKey = pad("AUTH00000010", 16);
        stubDliWithSegment("  ", returnedKey, pad("CARDNEXT", 16));

        String out = runMainLine(buildCommarea("NXT ", "", ""));

        assertEquals("  ", wsPlStatus(out));
        assertMsg(out, "Pending authorization retrieved.");
        assertEquals(returnedKey, wsPlKey(out));
        verify(dliService, times(1)).cbltdli(anyString(), any(), any(String[].class));
    }

    @Test
    void browseNext_keyFound_repositionsThenGetsNext() {
        String anchorKey = pad("AUTH00000020", 16);
        String nextKey = pad("AUTH00000021", 16);
        given(appService.getDliService()).willReturn(dliService);
        AtomicInteger idx = new AtomicInteger(0);
        given(dliService.cbltdli(anyString(), any(), any(String[].class)))
                .willAnswer(
                        inv -> {
                            OuimspaFields f = (OuimspaFields) inv.getArgument(1);
                            if (idx.getAndIncrement() == 0) {
                                return "  ";
                            }
                            f.setPaAuthId(nextKey);
                            f.setPaCardNum(pad("CARDX", 16));
                            return "  ";
                        });

        String out = runMainLine(buildCommarea("NXT ", anchorKey, ""));

        assertEquals("  ", wsPlStatus(out));
        assertEquals(nextKey, wsPlKey(out));
        verify(dliService, times(2)).cbltdli(anyString(), any(), any(String[].class));
    }

    @Test
    void browseNext_keyPurged_repositionsWithGreaterThanThenReturnsSegment() {
        String anchorKey = pad("AUTH00000030", 16);
        String repositionedKey = pad("AUTH00000031", 16);
        given(appService.getDliService()).willReturn(dliService);
        AtomicInteger idx = new AtomicInteger(0);
        given(dliService.cbltdli(anyString(), any(), any(String[].class)))
                .willAnswer(
                        inv -> {
                            OuimspaFields f = (OuimspaFields) inv.getArgument(1);
                            if (idx.getAndIncrement() == 0) {
                                return "GE";
                            }
                            f.setPaAuthId(repositionedKey);
                            f.setPaCardNum(pad("CARDY", 16));
                            return "  ";
                        });

        String out = runMainLine(buildCommarea("NXT ", anchorKey, ""));

        assertEquals("  ", wsPlStatus(out));
        assertEquals(repositionedKey, wsPlKey(out));
        verify(dliService, times(2)).cbltdli(anyString(), any(), any(String[].class));
    }

    @Test
    void browseNext_getNextEndOfList_returnsGbStatus() {
        stubDli("GB");

        String out = runMainLine(buildCommarea("NXT ", "", ""));

        assertEquals("GB", wsPlStatus(out));
        assertMsg(out, "End of pending authorization list.");
        assertEquals(pad("", 200), wsPlSegment(out));
    }

    @Test
    void browseNext_getNextNotFound_alsoReturnsGbStatus() {
        stubDli("GE");

        String out = runMainLine(buildCommarea("NXT ", "", ""));

        assertEquals("GB", wsPlStatus(out));
        assertMsg(out, "End of pending authorization list.");
    }

    // ==================== 2200-INSERT ====================

    @Test
    void insert_valid_insertsDefaultsStatusAndReturnsOk() {
        String key = pad("AUTH00000040", 16);
        String segment =
                buildSegment(pad("", 16), pad("CARD0000000000099", 16), "00000012345", " ");
        stubDli("  ");

        String out = runMainLine(buildCommarea("ADD ", key, segment));

        assertEquals("  ", wsPlStatus(out));
        assertMsg(out, "Pending authorization inserted.");
        String outSegment = wsPlSegment(out);
        assertEquals(key, sub(outSegment, 0, 16));
        assertEquals("P", sub(outSegment, 130, 1));
        assertOkResult(out);
        verify(dliService, times(1)).cbltdli(anyString(), any(), any(String[].class));
    }

    @Test
    void insert_duplicateKey_returnsIiStatus() {
        String key = pad("AUTH00000041", 16);
        String segment =
                buildSegment(pad("", 16), pad("CARD0000000000099", 16), "00000012345", "P");
        stubDli("II");

        String out = runMainLine(buildCommarea("ADD ", key, segment));

        assertEquals("II", wsPlStatus(out));
        assertMsg(out, "Authorization id already exists.");
        assertFailedResult(out, "Authorization id already exists.");
    }

    @Test
    void insert_missingCardNumber_returnsBadFunctionWithoutDliCall() {
        String key = pad("AUTH00000042", 16);
        String segment = buildSegment(pad("", 16), "", "00000012345", "P");

        String out = runMainLine(buildCommarea("ADD ", key, segment));

        assertEquals("BF", wsPlStatus(out));
        assertMsg(out, "Card number is required to insert.");
        verifyNoInteractions(dliService);
    }

    @Test
    void insert_nonNumericAccountId_returnsBadFunctionWithoutDliCall() {
        String key = pad("AUTH00000043", 16);
        String segment =
                buildSegment(pad("", 16), pad("CARD0000000000099", 16), "ABCDEFGHJKL", "P");

        String out = runMainLine(buildCommarea("ADD ", key, segment));

        assertEquals("BF", wsPlStatus(out));
        assertMsg(out, "Account id must be numeric.");
        verifyNoInteractions(dliService);
    }

    // ==================== 2300-UPDATE ====================

    @Test
    void update_found_appliesReplaceAndReturnsOk() {
        String key = pad("AUTH00000050", 16);
        String segment =
                buildSegment(pad("", 16), pad("CARD0000000000050", 16), "00000099999", "A");
        stubDli("  ", "  ");

        String out = runMainLine(buildCommarea("UPD ", key, segment));

        assertEquals("  ", wsPlStatus(out));
        assertMsg(out, "Pending authorization updated.");
        assertEquals(key, sub(wsPlSegment(out), 0, 16));
        assertOkResult(out);
        verify(dliService, times(2)).cbltdli(anyString(), any(), any(String[].class));
    }

    @Test
    void update_notFound_returnsGeStatusWithoutReplace() {
        String key = pad("AUTH00000051", 16);
        String segment =
                buildSegment(pad("", 16), pad("CARD0000000000051", 16), "00000099999", "A");
        stubDli("GE");

        String out = runMainLine(buildCommarea("UPD ", key, segment));

        assertEquals("GE", wsPlStatus(out));
        assertMsg(out, "No pending authorization for that id.");
        verify(dliService, times(1)).cbltdli(anyString(), any(), any(String[].class));
    }

    // ==================== 2400-DELETE ====================

    @Test
    void delete_found_purgesAndReturnsOk() {
        String key = pad("AUTH00000060", 16);
        stubDli("  ", "  ");

        String out = runMainLine(buildCommarea("DEL ", key, ""));

        assertEquals("  ", wsPlStatus(out));
        assertMsg(out, "Pending authorization purged.");
        assertEquals(pad("", 200), wsPlSegment(out));
        assertOkResult(out);
        verify(dliService, times(2)).cbltdli(anyString(), any(), any(String[].class));
    }

    @Test
    void delete_notFound_returnsGeStatusWithoutDelete() {
        String key = pad("AUTH00000061", 16);
        stubDli("GE");

        String out = runMainLine(buildCommarea("DEL ", key, ""));

        assertEquals("GE", wsPlStatus(out));
        assertMsg(out, "No pending authorization for that id.");
        verify(dliService, times(1)).cbltdli(anyString(), any(), any(String[].class));
    }
}
