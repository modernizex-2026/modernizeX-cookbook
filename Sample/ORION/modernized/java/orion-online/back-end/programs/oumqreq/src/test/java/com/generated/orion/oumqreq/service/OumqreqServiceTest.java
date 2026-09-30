package com.generated.orion.oumqreq.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appruntime.AppService;
import com.appruntime.FormatTimeResult;
import com.generated.orion.oumqreq.accessor.OumqreqFields;
import com.generated.orion.oumqreq.model.WorkingStorage;

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
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for OumqreqService, generated from COBOL program OUMQREQ.
 *
 * <p>OUMQREQ never exposes its per-request WorkingStorage directly; the only externally observable
 * result is the byte[] handed back to {@code AppService#setCommarea} (mirroring the real CICS LINK
 * commarea-out semantics). Tests therefore drive {@code mainLine} with a mocked {@link AppService},
 * capture that byte[], and decode it with a fresh {@link OumqreqFields} to assert on AS- and CA-
 * fields, the same fields the COBOL PROCEDURE DIVISION sets.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Timeout(value = 10, unit = TimeUnit.SECONDS)
class OumqreqServiceTest {

    @Mock private AppService appService;

    private final OumqreqService service = new OumqreqService();

    @BeforeEach
    void setUp() {
        when(appService.formatTime(any(), any(), any(), any()))
                .thenReturn(new FormatTimeResult("2026-09-24", "10:00:00"));
        when(appService.getEibcalen()).thenReturn(100);
    }

    /** Decodes the byte[] handed to setCommarea back into a fresh accessor over the same layout. */
    private OumqreqFields decodeCommarea(byte[] bytes) {
        OumqreqFields decoded = new OumqreqFields(new WorkingStorage());
        decoded.writeBytes("ORION-COMMAREA", bytes);
        // Mirror the SET ADDRESS OF AUTH-MSG-AREA TO ADDRESS OF CA-WORK-AREA done at 0000-MAIN
        // entry, so AS-*/AQ-* fields decode from the same CA-WORK-AREA bytes they were written to.
        decoded.aliasGroup("AUTH-MSG-AREA", "CA-WORK-AREA");
        return decoded;
    }

    private OumqreqFields runAndDecode() {
        service.mainLine(appService);
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(appService).setCommarea(captor.capture());
        return decodeCommarea((byte[]) captor.getValue());
    }

    /**
     * Stubs readFile/getEibresp for the two random reads performed by 3000-LOCAL-AUTHORIZE (XREF
     * then, if found, ACCT), and injects AQ-AMOUNT (the inbound request amount) via the same "into"
     * accessor, since the only seam into WorkingStorage before 3600-DECIDE runs.
     *
     * @param xrefEibresp EIBRESP after the XREF read: 0=found, 13=not found, other=I/O error.
     * @param acctEibresp EIBRESP after the ACCT read (only consulted when xrefEibresp==0).
     */
    private void stubReads(
            int xrefEibresp,
            long xrefAcctId,
            Integer acctEibresp,
            String activeStatus,
            BigDecimal creditLimit,
            BigDecimal currBal,
            BigDecimal requestAmount) {
        AtomicInteger readCount = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            int call = readCount.incrementAndGet();
                            OumqreqFields flds = (OumqreqFields) inv.getArgument(1);
                            if (call == 1) {
                                flds.setAqAmount(requestAmount);
                                if (xrefEibresp == 0) {
                                    flds.setXrAcctId(xrefAcctId);
                                }
                            } else if (call == 2) {
                                if (acctEibresp != null && acctEibresp == 0) {
                                    flds.setAcActiveStatus(activeStatus);
                                    flds.setAcCreditLimit(creditLimit);
                                    flds.setAcCurrBal(currBal);
                                }
                            }
                            return null;
                        })
                .when(appService)
                .readFile(any(), any(), any(), anyInt());

        List<Integer> respSeq = new ArrayList<>();
        respSeq.add(0); // 8500-GET-STAMP call inside 2000-BUILD-REQUEST
        respSeq.add(xrefEibresp);
        if (xrefEibresp == 0) {
            respSeq.add(acctEibresp);
        }
        respSeq.add(0); // 8500-GET-STAMP call inside 9000-FINALIZE
        Integer first = respSeq.remove(0);
        when(appService.getEibresp()).thenReturn(first, respSeq.toArray(new Integer[0]));
    }

    // ---------------------------------------------------------------
    // 3600-DECIDE / 3300-EVALUATE-ACCT / 3000-LOCAL-AUTHORIZE — happy paths
    // ---------------------------------------------------------------

    @Test
    void mainLine_amountWithinAvailableCredit_approvesAndPutsThenGetsOverMq() {
        stubReads(
                0,
                555L,
                0,
                "Y",
                new BigDecimal("1000.00"),
                new BigDecimal("200.00"),
                new BigDecimal("500.00"));

        OumqreqFields result = runAndDecode();

        assertEquals("APPROVED", result.getAsDecision().trim());
        assertEquals("APPROVED OK", result.getAsReason().trim());
        assertEquals(0, new BigDecimal("500.00").compareTo(result.getAsApprovedAmt()));
        assertEquals(0, new BigDecimal("800.00").compareTo(result.getAsAvailCredit()));
        assertEquals("N", result.getString("CA-ERR-FLG").trim());
        assertEquals("AUTH APPROVED - APPROVED OK", result.getCaErrMsg().trim());

        // COBOL always attempts MQ PUT + GET regardless of the local decision (best effort).
        verify(appService, times(2)).callProgram(eq("MQOPEN"), any());
        verify(appService, times(1)).callProgram(eq("MQPUT"), any());
        verify(appService, times(2)).callProgram(eq("MQCLOSE"), any());
        verify(appService, times(1)).callProgram(eq("MQGET"), any());
    }

    @Test
    void mainLine_amountEqualsAvailableCredit_approvedAtBoundary() {
        // COBOL: WHEN AQ-AMOUNT > WS-AMT declines; equality is NOT ">" so OTHER (approve) applies.
        stubReads(
                0,
                555L,
                0,
                "Y",
                new BigDecimal("1000.00"),
                new BigDecimal("200.00"),
                new BigDecimal("800.00"));

        OumqreqFields result = runAndDecode();

        assertEquals("APPROVED", result.getAsDecision().trim());
        assertEquals(0, new BigDecimal("800.00").compareTo(result.getAsApprovedAmt()));
    }

    // ---------------------------------------------------------------
    // 3600-DECIDE — decline branches
    // ---------------------------------------------------------------

    @Test
    void mainLine_amountExceedsAvailableCredit_declinesInsufficientCredit() {
        stubReads(
                0,
                555L,
                0,
                "Y",
                new BigDecimal("1000.00"),
                new BigDecimal("200.00"),
                new BigDecimal("800.01"));

        OumqreqFields result = runAndDecode();

        assertEquals("DECLINED", result.getAsDecision().trim());
        assertEquals("INSUFFICIENT CREDIT", result.getAsReason().trim());
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getAsApprovedAmt()));
        assertEquals("AUTH DECLINED - INSUFFICIENT CREDIT", result.getCaErrMsg().trim());
    }

    @Test
    void mainLine_accountInactive_declinesRegardlessOfAmount() {
        // COBOL checks AC-ACTIVE-STATUS NOT = 'Y' first in the EVALUATE, before the amount check.
        stubReads(
                0,
                555L,
                0,
                "N",
                new BigDecimal("1000.00"),
                new BigDecimal("0.00"),
                new BigDecimal("1.00"));

        OumqreqFields result = runAndDecode();

        assertEquals("DECLINED", result.getAsDecision().trim());
        assertEquals("ACCOUNT INACTIVE", result.getAsReason().trim());
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getAsApprovedAmt()));
    }

    // ---------------------------------------------------------------
    // 3300-EVALUATE-ACCT / 3000-LOCAL-AUTHORIZE — not-found branches
    // ---------------------------------------------------------------

    @Test
    void mainLine_accountNotFoundAfterXrefResolves_declinesAccountNotFound() {
        stubReads(0, 555L, 13, null, null, null, new BigDecimal("100.00"));

        OumqreqFields result = runAndDecode();

        assertEquals("DECLINED", result.getAsDecision().trim());
        assertEquals("ACCOUNT NOT FOUND", result.getAsReason().trim());
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getAsApprovedAmt()));
        assertEquals(555L, result.getAsAcctId());
    }

    @Test
    void mainLine_cardNotInXref_declinesCardNotFound() {
        stubReads(13, 0L, null, null, null, null, new BigDecimal("100.00"));

        OumqreqFields result = runAndDecode();

        assertEquals("DECLINED", result.getAsDecision().trim());
        assertEquals("CARD NOT FOUND", result.getAsReason().trim());
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getAsApprovedAmt()));
        assertEquals(0L, result.getAsAcctId());
    }

    // ---------------------------------------------------------------
    // I/O error branches — AS-ERRORED / CA-ERR-FLG, MQ still attempted
    // ---------------------------------------------------------------

    @Test
    void mainLine_xrefReadIoError_setsErrorDecisionAndCommareaErrFlag() {
        stubReads(99, 0L, null, null, null, null, new BigDecimal("100.00"));

        OumqreqFields result = runAndDecode();

        assertEquals("ERROR", result.getAsDecision().trim());
        assertEquals("FILE READ ERROR", result.getAsReason().trim());
        assertEquals("Y", result.getString("CA-ERR-FLG").trim());
        assertEquals("AUTH ERROR    - FILE READ ERROR", result.getCaErrMsg().trim());

        // COBOL 0000-MAIN performs 4000-PUT-REQUEST / 5000-GET-REPLY unconditionally after 3000.
        verify(appService, times(2)).callProgram(eq("MQOPEN"), any());
    }

    @Test
    void mainLine_acctReadIoError_setsErrorDecision() {
        stubReads(0, 555L, 99, null, null, null, new BigDecimal("100.00"));

        OumqreqFields result = runAndDecode();

        assertEquals("ERROR", result.getAsDecision().trim());
        assertEquals("FILE READ ERROR", result.getAsReason().trim());
        assertEquals("Y", result.getString("CA-ERR-FLG").trim());
    }

    // ---------------------------------------------------------------
    // 4000-PUT-REQUEST / 8000-CHECK-MQ — MQ transport failure is best-effort only
    // ---------------------------------------------------------------

    @Test
    void mainLine_mqOpenFails_putAndGetAreSkippedButLocalDecisionStands() {
        stubReads(
                0,
                555L,
                0,
                "Y",
                new BigDecimal("1000.00"),
                new BigDecimal("200.00"),
                new BigDecimal("500.00"));
        doAnswer(
                        inv -> {
                            Object[] params = inv.getArgument(1);
                            params[4] =
                                    2059; // MQ-CC: MQOPEN param shape is {hconn, OD, options, hobj,
                            // cc, rc}
                            params[5] = 2059;
                            return null;
                        })
                .when(appService)
                .callProgram(eq("MQOPEN"), any());

        OumqreqFields result = runAndDecode();

        // Local authorization is authoritative and unaffected by the MQ transport being down.
        assertEquals("APPROVED", result.getAsDecision().trim());

        verify(appService, times(2)).callProgram(eq("MQOPEN"), any());
        verify(appService, never()).callProgram(eq("MQPUT"), any());
        verify(appService, never()).callProgram(eq("MQGET"), any());
        verify(appService, never()).callProgram(eq("MQCLOSE"), any());
    }

    @Test
    void mainLine_mqGetReturnsNoMessage_logsLocalDecisionStandsAndStillCloses() {
        stubReads(
                0,
                555L,
                0,
                "Y",
                new BigDecimal("1000.00"),
                new BigDecimal("200.00"),
                new BigDecimal("500.00"));
        doAnswer(
                        inv -> {
                            Object[] params = inv.getArgument(1);
                            params[7] =
                                    2; // MQ-CC: MQGET param shape is {hconn, hobjRpy, MD, GMO, len,
                            // buf, dataLen, cc, rc}
                            params[8] =
                                    2033; // MQRC_NO_MSG_AVAILABLE - local decision already stands
                            // per COBOL 5100-GET-MSG.
                            return null;
                        })
                .when(appService)
                .callProgram(eq("MQGET"), any());

        OumqreqFields result = runAndDecode();

        assertEquals("APPROVED", result.getAsDecision().trim());
        verify(appService, times(1)).callProgram(eq("MQGET"), any());
        verify(appService, times(2)).callProgram(eq("MQCLOSE"), any());
    }

    // ---------------------------------------------------------------
    // mainLine commarea marshalling (COBOL DFHCOMMAREA in/out via LINK)
    // ---------------------------------------------------------------

    @Test
    void mainLine_commareaSuppliedAsObjectArrayWithByteArrayPayload_writesResultBackIntoArray() {
        stubReads(
                0,
                555L,
                0,
                "Y",
                new BigDecimal("1000.00"),
                new BigDecimal("200.00"),
                new BigDecimal("500.00"));
        byte[] blankCommarea = new OumqreqFields(new WorkingStorage()).sliceBytes("ORION-COMMAREA");
        Object[] commareaHolder = new Object[] {blankCommarea};
        when(appService.getEibcalen()).thenReturn(blankCommarea.length);
        when(appService.getCommarea()).thenReturn(commareaHolder);

        service.mainLine(appService);

        assertTrue(commareaHolder[0] instanceof byte[]);
        OumqreqFields result = decodeCommarea((byte[]) commareaHolder[0]);
        assertEquals("APPROVED", result.getAsDecision().trim());
        verify(appService, never()).setCommarea(any());
    }

    // ---------------------------------------------------------------
    // AppProgram contract methods
    // ---------------------------------------------------------------

    @Test
    void getProgramName_returnsOumqreq() {
        assertEquals("OUMQREQ", service.getProgramName());
    }

    @Test
    void getButtonDefs_delegatesToMetadata_returnsEmptyList() {
        assertTrue(service.getButtonDefs().isEmpty());
    }

    @Test
    void registerFsetFields_delegatesToMetadata_noOpAndNoException() {
        assertDoesNotThrow(() -> service.registerFsetFields(null));
    }
}
