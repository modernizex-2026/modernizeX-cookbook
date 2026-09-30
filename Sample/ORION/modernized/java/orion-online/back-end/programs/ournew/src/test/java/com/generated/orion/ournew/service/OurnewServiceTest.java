package com.generated.orion.ournew.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appruntime.AppService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

/**
 * Unit tests for OurnewService (converted from OURNEW.cbl). Expected values are derived from the
 * COBOL paragraphs, not from the Java implementation, per write-unit-test ground-truth rule.
 */
@ExtendWith(MockitoExtension.class)
class OurnewServiceTest {

    @Mock private AppService appService;

    private OurnewService service;
    private OurnewService.TaskContext ctx;

    @BeforeEach
    void setUp() {
        service = new OurnewService();
        ctx = new OurnewService.TaskContext(appService);
    }

    private void invoke(String methodName, Object... args) throws Exception {
        Class<?>[] paramTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            paramTypes[i] = OurnewService.TaskContext.class;
        }
        Method m = OurnewService.class.getDeclaredMethod(methodName, paramTypes);
        m.setAccessible(true);
        m.invoke(service, args);
    }

    // ───────────────────────── 1000-INITIALISE ─────────────────────────

    @Test
    void initializeProgram_defaultParmCard_countersZeroAndNoFilter() throws Exception {
        invoke("initializeProgram", ctx);

        assertEquals("O", ctx.f.getKoStatus());
        assertEquals(0, ctx.f.getKoReadCnt());
        assertEquals(0, ctx.f.getKoSelectCnt());
        assertEquals(0, ctx.f.getKoUpdateCnt());
        assertEquals(0, ctx.f.getKoPostedCnt());
        assertEquals(0, ctx.f.getKoRejectCnt());
        assertEquals(0, ctx.f.getKoC1());
        assertEquals(0, ctx.f.getKoC2());
        assertEquals(0, ctx.f.getKoC3());
        assertEquals("N", ctx.f.getWsFilterOn());
        assertEquals("N", ctx.f.getWsBrEndSw());
        assertEquals("N", ctx.f.getWsBrStartedSw());
    }

    @Test
    void initializeProgram_parmCardProvided_activatesFilter() throws Exception {
        ctx.f.setKoParmCard("1234567890123456");

        invoke("initializeProgram", ctx);

        assertEquals("Y", ctx.f.getWsFilterOn());
        assertEquals("1234567890123456", ctx.f.getWsFilterCard());
    }

    @Test
    void initializeProgram_parmCardLowValues_filterNotActivated() throws Exception {
        ctx.f.fillLowValues("KO-PARM-CARD");

        invoke("initializeProgram", ctx);

        assertEquals("N", ctx.f.getWsFilterOn());
    }

    // ───────────────────────── 1300-COMPUTE-CUTOFF ─────────────────────────

    @Test
    void computeCutoffDate_runDatePlus90Days_matchesCobolLeadDays() throws Exception {
        LocalDate expectedCutoff = LocalDate.now().plusDays(90);

        invoke("computeCutoffDate", ctx);

        assertEquals(expectedCutoff.getYear(), ctx.f.getCsYear());
        assertEquals(expectedCutoff.getMonthValue(), ctx.f.getCsMm());
        assertEquals(expectedCutoff.getDayOfMonth(), ctx.f.getCsDd());
        String expectedStr =
                String.format(
                        "%04d-%02d-%02d",
                        expectedCutoff.getYear(),
                        expectedCutoff.getMonthValue(),
                        expectedCutoff.getDayOfMonth());
        assertEquals(expectedStr, ctx.f.getWsCutoffStr());
    }

    // ───────────────────────── 3100-START-BROWSE ─────────────────────────

    @Test
    void startCardBrowse_normalResp_marksStarted() throws Exception {
        when(appService.getEibresp()).thenReturn(0);

        invoke("startCardBrowse", ctx);

        assertEquals("Y", ctx.f.getWsBrStartedSw());
        assertFalse(ctx.f.getKoStatus().equals("E"));
    }

    @Test
    void startCardBrowse_notFound_setsEndAndNoCardsMessage() throws Exception {
        when(appService.getEibresp()).thenReturn(13);

        invoke("startCardBrowse", ctx);

        assertEquals("Y", ctx.f.getWsBrEndSw());
        assertEquals("NO CARDS TO PROCESS.", ctx.f.getKoStatusMsg().trim());
        assertFalse(ctx.f.getKoStatus().equals("E"));
    }

    @Test
    void startCardBrowse_otherError_setsErrorStatus() throws Exception {
        when(appService.getEibresp()).thenReturn(99);

        invoke("startCardBrowse", ctx);

        assertEquals("Y", ctx.f.getWsBrEndSw());
        assertEquals("E", ctx.f.getKoStatus());
        assertEquals("STARTBR CARDFILE FAILED.", ctx.f.getKoStatusMsg().trim());
    }

    @Test
    void startCardBrowse_filterActive_usesFilterCardAsRidfld() throws Exception {
        ctx.f.setWsFilterOn("Y");
        ctx.f.setWsFilterCard("9999888877776666");
        when(appService.getEibresp()).thenReturn(0);

        invoke("startCardBrowse", ctx);

        assertEquals("9999888877776666", ctx.f.getCdNum());
        verify(appService).startBrowse(anyString(), eq("9999888877776666"), eq(0));
    }

    @Test
    void startCardBrowse_noFilter_usesLowValuesAsRidfld() throws Exception {
        when(appService.getEibresp()).thenReturn(0);

        invoke("startCardBrowse", ctx);

        assertTrue(ctx.f.isAllLowValues("CD-NUM"));
    }

    // ───────────────────────── 3200-READ-NEXT-CARD ─────────────────────────

    @Test
    void readNextCard_normalResp_incrementsReadCount() throws Exception {
        when(appService.getEibresp()).thenReturn(0);

        invoke("readNextCard", ctx);

        assertEquals(1, ctx.f.getKoReadCnt());
        assertEquals("N", ctx.f.getWsBrEndSw());
    }

    @Test
    void readNextCard_endOfFile_setsEndWithoutError() throws Exception {
        when(appService.getEibresp()).thenReturn(20);

        invoke("readNextCard", ctx);

        assertEquals("Y", ctx.f.getWsBrEndSw());
        assertEquals(0, ctx.f.getKoReadCnt());
        assertFalse(ctx.f.getKoStatus().equals("E"));
    }

    @Test
    void readNextCard_otherError_setsErrorStatus() throws Exception {
        when(appService.getEibresp()).thenReturn(99);

        invoke("readNextCard", ctx);

        assertEquals("Y", ctx.f.getWsBrEndSw());
        assertEquals("E", ctx.f.getKoStatus());
        assertEquals("READNEXT CARDFILE FAILED.", ctx.f.getKoStatusMsg().trim());
    }

    // ───────────────────────── 4000-PROCESS-CARD ─────────────────────────

    @Test
    void processCard_filterActiveMismatch_endsBrowseWithoutReadingNext() throws Exception {
        ctx.f.setWsFilterOn("Y");
        ctx.f.setWsFilterCard("1111222233334444");
        ctx.f.setCdNum("5555666677778888");

        invoke("processCard", ctx);

        assertEquals("Y", ctx.f.getWsBrEndSw());
        verify(appService, never()).readNext(anyString(), any());
    }

    @Test
    void processCard_inactiveCard_incrementsC2AndReadsNext() throws Exception {
        ctx.f.setCdActiveStatus("N");
        when(appService.getEibresp()).thenReturn(20);

        invoke("processCard", ctx);

        assertEquals(1, ctx.f.getKoC2());
        assertEquals(0, ctx.f.getKoC1());
        assertEquals(0, ctx.f.getKoC3());
        verify(appService, times(1)).readNext(anyString(), any());
    }

    @Test
    void processCard_activeButInvalidExpiry_incrementsC3() throws Exception {
        ctx.f.setCdActiveStatus("Y");
        ctx.f.setCdExpiryDate("ABCD-05-15");
        when(appService.getEibresp()).thenReturn(20);

        invoke("processCard", ctx);

        assertEquals(1, ctx.f.getKoC3());
        assertEquals(0, ctx.f.getKoC1());
        assertEquals(0, ctx.f.getKoC2());
    }

    @Test
    void processCard_activeValidExpiryNotDue_incrementsC1() throws Exception {
        ctx.f.setCdActiveStatus("Y");
        ctx.f.setCdExpiryDate("2099-05-15");
        ctx.f.setWsCutoffStr("2026-01-01");
        when(appService.getEibresp()).thenReturn(20);

        invoke("processCard", ctx);

        assertEquals(1, ctx.f.getKoC1());
        assertEquals(0, ctx.f.getKoC2());
        assertEquals(0, ctx.f.getKoC3());
    }

    @Test
    void processCard_activeValidExpiryDue_reissuesCard() throws Exception {
        ctx.f.setCdActiveStatus("Y");
        ctx.f.setCdExpiryDate("2026-01-01");
        ctx.f.setWsCutoffStr("2026-03-31");
        ctx.f.setCdNum("1231231231231231");
        when(appService.getEibresp()).thenReturn(0);

        invoke("processCard", ctx);

        assertEquals(1, ctx.f.getKoPostedCnt());
        assertEquals(1, ctx.f.getKoUpdateCnt());
        assertEquals(1, ctx.f.getKoSelectCnt());
        assertEquals(0, ctx.f.getKoC1());
        verify(appService).readFileForUpdate(anyString(), any(), eq("1231231231231231"), eq(0));
        verify(appService).rewriteFile(anyString(), any());
    }

    // ───────────────────────── 4100-VALIDATE-EXPIRY ─────────────────────────

    @Test
    void validateExpiryDate_wellFormedDate_staysValid() throws Exception {
        ctx.f.setCdExpiryDate("2027-05-15");

        invoke("validateExpiryDate", ctx);

        assertEquals("Y", ctx.f.getWsExpValidSw());
    }

    @Test
    void validateExpiryDate_nonNumericYear_becomesInvalid() throws Exception {
        ctx.f.setCdExpiryDate("ABCD-05-15");

        invoke("validateExpiryDate", ctx);

        assertEquals("N", ctx.f.getWsExpValidSw());
    }

    @Test
    void validateExpiryDate_nonNumericMonth_becomesInvalid() throws Exception {
        ctx.f.setCdExpiryDate("2027-XX-15");

        invoke("validateExpiryDate", ctx);

        assertEquals("N", ctx.f.getWsExpValidSw());
    }

    @Test
    void validateExpiryDate_nonNumericDay_becomesInvalid() throws Exception {
        ctx.f.setCdExpiryDate("2027-05-XX");

        invoke("validateExpiryDate", ctx);

        assertEquals("N", ctx.f.getWsExpValidSw());
    }

    // ───────────────────────── 4200-REISSUE-CARD ─────────────────────────

    @Test
    void reissueCard_nonFebruaryDate_extendsYearOnlyAndPosts() throws Exception {
        ctx.f.setWsExpYearX("2026");
        ctx.f.setWsExpMmX("05");
        ctx.f.setWsExpDdX("15");
        when(appService.getEibresp()).thenReturn(0);

        invoke("reissueCard", ctx);

        assertEquals(2029, ctx.f.getWneYear());
        assertEquals("05", ctx.f.getWneMm());
        assertEquals("15", ctx.f.getWneDd());
        assertEquals(1, ctx.f.getKoPostedCnt());
        assertEquals(1, ctx.f.getKoSelectCnt());
        assertEquals(1, ctx.f.getKoUpdateCnt());
        assertEquals(0, ctx.f.getKoRejectCnt());
    }

    @Test
    void reissueCard_feb29ExtendedIntoLeapYear_dayStays29() throws Exception {
        ctx.f.setWsExpYearX("2025"); // + 3 = 2028 (leap: div4, not div100)
        ctx.f.setWsExpMmX("02");
        ctx.f.setWsExpDdX("29");
        when(appService.getEibresp()).thenReturn(0);

        invoke("reissueCard", ctx);

        assertEquals(2028, ctx.f.getWneYear());
        assertEquals("29", ctx.f.getWneDd());
    }

    @Test
    void reissueCard_feb29ExtendedIntoNonLeapYear_dayClampedTo28() throws Exception {
        ctx.f.setWsExpYearX("2026"); // + 3 = 2029 (not div4 -> not leap)
        ctx.f.setWsExpMmX("02");
        ctx.f.setWsExpDdX("29");
        when(appService.getEibresp()).thenReturn(0);

        invoke("reissueCard", ctx);

        assertEquals(2029, ctx.f.getWneYear());
        assertEquals("28", ctx.f.getWneDd());
    }

    @Test
    void reissueCard_readForUpdateFails_rejectsWithoutRewrite() throws Exception {
        ctx.f.setWsExpYearX("2026");
        ctx.f.setWsExpMmX("05");
        ctx.f.setWsExpDdX("15");
        when(appService.getEibresp()).thenReturn(99);

        invoke("reissueCard", ctx);

        assertEquals(1, ctx.f.getKoRejectCnt());
        assertEquals(0, ctx.f.getKoPostedCnt());
        verify(appService, never()).rewriteFile(anyString(), any());
    }

    @Test
    void reissueCard_rewriteFails_rejectsAfterSuccessfulRead() throws Exception {
        ctx.f.setWsExpYearX("2026");
        ctx.f.setWsExpMmX("05");
        ctx.f.setWsExpDdX("15");
        when(appService.getEibresp()).thenReturn(0, 99);

        invoke("reissueCard", ctx);

        assertEquals(1, ctx.f.getKoRejectCnt());
        assertEquals(0, ctx.f.getKoPostedCnt());
        assertEquals(0, ctx.f.getKoSelectCnt());
        assertEquals(0, ctx.f.getKoUpdateCnt());
        verify(appService).rewriteFile(anyString(), any());
    }

    // ───────────────────────── 4250-ADJUST-LEAP ─────────────────────────

    @Test
    void adjustLeapYearDay_divisibleBy4NotBy100_staysLeapDay29() throws Exception {
        ctx.f.setWneYear(2028);
        ctx.f.setWneDd("29");

        invoke("adjustLeapYearDay", ctx);

        assertEquals("29", ctx.f.getWneDd());
    }

    @Test
    void adjustLeapYearDay_divisibleBy100NotBy400_clampedTo28() throws Exception {
        ctx.f.setWneYear(2100);
        ctx.f.setWneDd("29");

        invoke("adjustLeapYearDay", ctx);

        assertEquals("28", ctx.f.getWneDd());
    }

    @Test
    void adjustLeapYearDay_divisibleBy400_staysLeapDay29() throws Exception {
        ctx.f.setWneYear(2000);
        ctx.f.setWneDd("29");

        invoke("adjustLeapYearDay", ctx);

        assertEquals("29", ctx.f.getWneDd());
    }

    @Test
    void adjustLeapYearDay_notDivisibleBy4_clampedTo28() throws Exception {
        ctx.f.setWneYear(2029);
        ctx.f.setWneDd("29");

        invoke("adjustLeapYearDay", ctx);

        assertEquals("28", ctx.f.getWneDd());
    }

    // ───────────────────────── 3400-END-BROWSE ─────────────────────────

    @Test
    void endCardBrowse_browseStarted_callsEndBrowse() throws Exception {
        ctx.f.setWsBrStartedSw("Y");

        invoke("endCardBrowse", ctx);

        verify(appService, times(1)).endBrowse(anyString());
    }

    @Test
    void endCardBrowse_browseNotStarted_neverCallsEndBrowse() throws Exception {
        ctx.f.setWsBrStartedSw("N");

        invoke("endCardBrowse", ctx);

        verify(appService, never()).endBrowse(anyString());
    }

    // ───────────────────────── 9000-FINALISE ─────────────────────────

    @Test
    void finalizeProgramStatus_errorStatus_messageUnchanged() throws Exception {
        ctx.f.setKoStatus("E");
        ctx.f.setKoStatusMsg("STARTBR CARDFILE FAILED.");

        invoke("finalizeProgramStatus", ctx);

        assertEquals("E", ctx.f.getKoStatus());
        assertEquals("STARTBR CARDFILE FAILED.", ctx.f.getKoStatusMsg().trim());
    }

    @Test
    void finalizeProgramStatus_rejectsPresent_setsWarnStatus() throws Exception {
        ctx.f.setKoStatus("O");
        ctx.f.setKoRejectCnt(2);

        invoke("finalizeProgramStatus", ctx);

        assertEquals("W", ctx.f.getKoStatus());
        assertEquals("CARD RENEWAL PROCESSING COMPLETE.", ctx.f.getKoStatusMsg().trim());
    }

    @Test
    void finalizeProgramStatus_noRejects_setsOkStatus() throws Exception {
        ctx.f.setKoStatus("O");
        ctx.f.setKoRejectCnt(0);

        invoke("finalizeProgramStatus", ctx);

        assertEquals("O", ctx.f.getKoStatus());
        assertEquals("CARD RENEWAL PROCESSING COMPLETE.", ctx.f.getKoStatusMsg().trim());
    }

    // ───────────────────────── AppProgram metadata delegation ─────────────────────────

    @Test
    void getProgramName_returnsCobolProgramId() {
        assertEquals("OURNEW", service.getProgramName());
    }

    @Test
    void getButtonDefs_noBmsMap_returnsEmptyList() {
        assertTrue(service.getButtonDefs().isEmpty());
    }

    @Test
    void registerFsetFields_noBmsMap_completesWithoutError() {
        service.registerFsetFields(null);
    }

    // ───────────────────────── 0000-MAIN (full flow) ─────────────────────────

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void runMainProgram_singleActiveCardDue_reissuesAndCompletesOk() throws Exception {
        ctx.f.setCdNum("1231231231231231");
        ctx.f.setCdActiveStatus("Y");
        ctx.f.setCdExpiryDate("2026-01-01"); // certainly on/before today+90d cutoff
        // startBrowse(0) -> readNextCard(0, found) -> processCard reissues -> readNextCard(20, end)
        // -> endBrowse
        when(appService.getEibresp()).thenReturn(0, 0, 0, 0, 20);

        invoke("runMainProgram", ctx);

        assertEquals(1, ctx.f.getKoPostedCnt());
        assertEquals(1, ctx.f.getKoUpdateCnt());
        assertEquals(1, ctx.f.getKoSelectCnt());
        assertEquals("O", ctx.f.getKoStatus());
        assertEquals("CARD RENEWAL PROCESSING COMPLETE.", ctx.f.getKoStatusMsg().trim());
        verify(appService, times(1)).startBrowse(anyString(), anyString(), eq(0));
        verify(appService, times(1)).endBrowse(anyString());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void runMainProgram_startBrowseFails_reportsErrorWithoutReading() throws Exception {
        when(appService.getEibresp()).thenReturn(99);

        invoke("runMainProgram", ctx);

        assertEquals("E", ctx.f.getKoStatus());
        assertEquals("STARTBR CARDFILE FAILED.", ctx.f.getKoStatusMsg().trim());
        verify(appService, never()).readNext(anyString(), any());
        verify(appService, never()).endBrowse(anyString());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void runMainProgram_startBrowseNotFound_finalMessageOverwrittenToComplete() throws Exception {
        // CONVERT-GAP-CHECK: COBOL 9000-FINALISE unconditionally overwrites KO-STATUS-MSG
        // with the completion text whenever KO-STATUS is not 'E' — even after 3100-START-BROWSE
        // set "NO CARDS TO PROCESS.". This is faithfully reproduced (not a convert bug).
        when(appService.getEibresp()).thenReturn(13);

        invoke("runMainProgram", ctx);

        assertEquals("O", ctx.f.getKoStatus());
        assertEquals("CARD RENEWAL PROCESSING COMPLETE.", ctx.f.getKoStatusMsg().trim());
        verify(appService, never()).readNext(anyString(), any());
    }
}
