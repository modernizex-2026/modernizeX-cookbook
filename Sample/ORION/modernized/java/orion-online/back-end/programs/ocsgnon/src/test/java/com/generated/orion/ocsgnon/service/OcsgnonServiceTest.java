package com.generated.orion.ocsgnon.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appruntime.AppService;
import com.appruntime.FormatTimeResult;
import com.generated.orion.ocsgnon.accessor.OcsgnonFields;
import com.generated.orion.ocsgnon.model.WorkingStorage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.concurrent.TimeUnit;

/**
 * Unit tests for OcsgnonService, generated from COBOL program OCSGNON. Ground truth for expected
 * behavior is /ORION-CCMS/cbl/OCSGNON.cbl.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Timeout(value = 10, unit = TimeUnit.SECONDS)
class OcsgnonServiceTest {

    private static final int COMMAREA_LENGTH = 692;
    private static final String MSG_THANK_YOU = "Thank you for using ORION-CCMS.";
    private static final String MSG_INVALID_KEY = "Invalid key pressed. Please try again.";
    private static final String MSG_REQUIRED = "Please enter all required fields.";

    @Mock private AppService appService;

    private final OcsgnonService service = new OcsgnonService();

    @BeforeEach
    void setUp() {
        when(appService.formatTime(any(), any(), any(), any()))
                .thenReturn(new FormatTimeResult("20260923", "10:15:30"));
    }

    /**
     * Builds a serialized ORION-COMMAREA payload with the given CA-PGM-CONTEXT, as WS-USRSEC would
     * emit it.
     */
    private String buildCommarea(int pgmContext) {
        OcsgnonFields helper = new OcsgnonFields(new WorkingStorage());
        helper.setCaPgmContext(pgmContext);
        return helper.getOrionCommarea();
    }

    // ---------------------------------------------------------------
    // 0000-MAIN / 1000-SEND-INITIAL
    // ---------------------------------------------------------------

    @Test
    void mainLine_firstCallNoCommarea_sendsInitialScreenAndSetsContext() {
        when(appService.getEibcalen()).thenReturn(0);

        service.mainLine(appService);

        ArgumentCaptor<Object> mapOutCaptor = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq("MSGNONA"),
                        mapOutCaptor.capture(),
                        any(),
                        eq(true),
                        eq(false),
                        eq(false));
        OcsgnonFields sent = (OcsgnonFields) mapOutCaptor.getValue();
        assertThat(sent.getErrmsgo().trim()).isEqualTo("Please sign on.");
        assertThat(sent.getTrnnameo().trim()).isEqualTo("ORSN");
        assertThat(sent.getPgmnameo().trim()).isEqualTo("OCSGNON");
        assertThat(sent.getTitleo().trim()).isEqualTo("ORION CREDIT CARD MANAGEMENT SYSTEM");
        assertThat(sent.getCurdateo().trim()).isEqualTo("20260923");
        assertThat(sent.getCurtimeo().trim()).isEqualTo("10:15:30");

        verify(appService).returnTransid(eq("ORSN"), any(), eq(COMMAREA_LENGTH));
    }

    @Test
    void mainLine_firstEnterWithCommarea_sendsInitialScreen() {
        when(appService.getEibcalen()).thenReturn(COMMAREA_LENGTH);
        when(appService.getSerializedCommarea()).thenReturn(buildCommarea(0));

        service.mainLine(appService);

        ArgumentCaptor<Object> mapOutCaptor = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq("MSGNONA"),
                        mapOutCaptor.capture(),
                        any(),
                        eq(true),
                        eq(false),
                        eq(false));
        OcsgnonFields sent = (OcsgnonFields) mapOutCaptor.getValue();
        assertThat(sent.getErrmsgo().trim()).isEqualTo("Please sign on.");
    }

    // ---------------------------------------------------------------
    // 2000-PROCESS-INPUT — invalid key branch
    // ---------------------------------------------------------------

    @Test
    void mainLine_invalidKey_resendsInitialScreenThenInvalidKeyMessage() {
        when(appService.getEibcalen()).thenReturn(COMMAREA_LENGTH);
        when(appService.getSerializedCommarea()).thenReturn(buildCommarea(1));
        when(appService.getEibaid()).thenReturn("X");

        service.mainLine(appService);

        ArgumentCaptor<Boolean> eraseCaptor = ArgumentCaptor.forClass(Boolean.class);
        ArgumentCaptor<Object> mapOutCaptor = ArgumentCaptor.forClass(Object.class);
        verify(appService, times(2))
                .sendMap(
                        eq("MSGNONA"),
                        mapOutCaptor.capture(),
                        any(),
                        eraseCaptor.capture(),
                        eq(false),
                        eq(false));

        assertThat(eraseCaptor.getAllValues()).containsExactly(true, false);
        OcsgnonFields secondSend = (OcsgnonFields) mapOutCaptor.getAllValues().get(1);
        assertThat(secondSend.getErrmsgo().trim()).isEqualTo(MSG_INVALID_KEY);
    }

    // ---------------------------------------------------------------
    // 7000-SIGN-OFF (PF3) — includes CONVERT-GAP checks
    // ---------------------------------------------------------------

    @Test
    void mainLine_signOff_sendsThankYouMessage_convertGap() {
        // CONVERT-GAP: COBOL 7000-SIGN-OFF sends WS-MSG-THANKYOU ("Thank you for using
        // ORION-CCMS.") via EXEC CICS SEND TEXT FROM(WS-MSG-THANKYOU). The Java signOff()
        // instead calls appService.sendText(String.valueOf(ctx.f), ...) — String.valueOf on
        // the OcsgnonFields accessor yields its default Object#toString() (class name + hash),
        // never the actual thank-you text. Expected value below is per COBOL; this assertion
        // is expected to FAIL against current Java code.
        when(appService.getEibcalen()).thenReturn(COMMAREA_LENGTH);
        when(appService.getSerializedCommarea()).thenReturn(buildCommarea(1));
        when(appService.getEibaid()).thenReturn("3");

        service.mainLine(appService);

        ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);
        verify(appService).sendText(textCaptor.capture(), eq(true), eq(true));
        assertThat(textCaptor.getValue().trim()).isEqualTo(MSG_THANK_YOU);
    }

    @Test
    void mainLine_signOff_doesNotAlsoReturnTransid_convertGap() {
        // CONVERT-GAP: in COBOL, 7000-SIGN-OFF issues its own "EXEC CICS RETURN RESP(...)"
        // (no TRANSID) which ends the CICS task immediately — control never falls back to
        // 0000-MAIN's trailing "PERFORM 9000-RETURN". The Java executeMainLine() calls
        // signOff() (-> appService.returnProgram()) and then unconditionally calls
        // returnTransaction() (-> appService.returnTransid()) afterwards, issuing an extra
        // RETURN not present in the original COBOL control flow. Expected (per COBOL):
        // returnTransid must NOT be called after sign-off. Expected to FAIL against current code.
        when(appService.getEibcalen()).thenReturn(COMMAREA_LENGTH);
        when(appService.getSerializedCommarea()).thenReturn(buildCommarea(1));
        when(appService.getEibaid()).thenReturn("3");

        service.mainLine(appService);

        verify(appService).returnProgram();
        verify(appService, never()).returnTransid(any(), any(), anyInt());
    }

    // ---------------------------------------------------------------
    // 2100-VALIDATE-USER
    // ---------------------------------------------------------------

    @Test
    void mainLine_enterWithBlankUserId_showsRequiredMessage() {
        when(appService.getEibcalen()).thenReturn(COMMAREA_LENGTH);
        when(appService.getSerializedCommarea()).thenReturn(buildCommarea(1));
        when(appService.getEibaid()).thenReturn("'");
        // receiveMap left as no-op: USERIDI stays at its default (low-values) buffer content.

        service.mainLine(appService);

        ArgumentCaptor<Object> mapOutCaptor = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq("MSGNONA"),
                        mapOutCaptor.capture(),
                        any(),
                        eq(false),
                        eq(false),
                        eq(false));
        OcsgnonFields sent = (OcsgnonFields) mapOutCaptor.getValue();
        assertThat(sent.getErrmsgo().trim()).isEqualTo(MSG_REQUIRED);
        verify(appService, never()).readFile(any(), any(), any(), anyInt());
    }

    @Test
    void mainLine_enterUserNotFound_showsUserNotFoundMessage() {
        when(appService.getEibcalen()).thenReturn(COMMAREA_LENGTH);
        when(appService.getSerializedCommarea()).thenReturn(buildCommarea(1));
        when(appService.getEibaid()).thenReturn("'");
        doAnswer(
                        inv -> {
                            OcsgnonFields into = inv.getArgument(1);
                            into.setUseridi("TESTUSR ");
                            return null;
                        })
                .when(appService)
                .receiveMap(eq("MSGNONA"), any());
        when(appService.getEibresp()).thenReturn(12);

        service.mainLine(appService);

        verify(appService).readFile(any(), any(), eq("TESTUSR "), eq(0));
        ArgumentCaptor<Object> mapOutCaptor = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq("MSGNONA"),
                        mapOutCaptor.capture(),
                        any(),
                        eq(false),
                        eq(false),
                        eq(false));
        OcsgnonFields sent = (OcsgnonFields) mapOutCaptor.getValue();
        assertThat(sent.getErrmsgo().trim()).isEqualTo("User not found.");
    }

    @Test
    void mainLine_enterWrongPassword_showsInvalidPasswordMessage() {
        when(appService.getEibcalen()).thenReturn(COMMAREA_LENGTH);
        when(appService.getSerializedCommarea()).thenReturn(buildCommarea(1));
        when(appService.getEibaid()).thenReturn("'");
        doAnswer(
                        inv -> {
                            OcsgnonFields into = inv.getArgument(1);
                            into.setUseridi("TESTUSR ");
                            into.setPasswdi("WRONGPW ");
                            return null;
                        })
                .when(appService)
                .receiveMap(eq("MSGNONA"), any());
        doAnswer(
                        inv -> {
                            OcsgnonFields into = inv.getArgument(1);
                            into.setUsPassword("CORRECTPW");
                            into.setUsType("U");
                            return null;
                        })
                .when(appService)
                .readFile(any(), any(), any(), anyInt());
        when(appService.getEibresp()).thenReturn(0);

        service.mainLine(appService);

        ArgumentCaptor<Object> mapOutCaptor = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq("MSGNONA"),
                        mapOutCaptor.capture(),
                        any(),
                        eq(false),
                        eq(false),
                        eq(false));
        OcsgnonFields sent = (OcsgnonFields) mapOutCaptor.getValue();
        assertThat(sent.getErrmsgo().trim()).isEqualTo("Invalid password.");
        verify(appService, never()).xctl(any(), any(), anyInt());
    }

    @Test
    void mainLine_correctPasswordNonAdmin_transfersToMenuProgram() {
        when(appService.getEibcalen()).thenReturn(COMMAREA_LENGTH);
        when(appService.getSerializedCommarea()).thenReturn(buildCommarea(1));
        when(appService.getEibaid()).thenReturn("'");
        doAnswer(
                        inv -> {
                            OcsgnonFields into = inv.getArgument(1);
                            into.setUseridi("TESTUSR ");
                            into.setPasswdi("PASS1234");
                            return null;
                        })
                .when(appService)
                .receiveMap(eq("MSGNONA"), any());
        doAnswer(
                        inv -> {
                            OcsgnonFields into = inv.getArgument(1);
                            into.setUsPassword("PASS1234");
                            into.setUsType("U");
                            return null;
                        })
                .when(appService)
                .readFile(any(), any(), any(), anyInt());
        when(appService.getEibresp()).thenReturn(0);

        service.mainLine(appService);

        verify(appService)
                .xctl(
                        argThat(program -> program.trim().equals("OCMENU")),
                        any(),
                        eq(COMMAREA_LENGTH));
        verify(appService, never())
                .sendMap(eq("MSGNONA"), any(), any(), eq(false), eq(false), eq(false));
    }

    @Test
    void mainLine_correctPasswordAdmin_transfersToAdminMenuProgram() {
        when(appService.getEibcalen()).thenReturn(COMMAREA_LENGTH);
        when(appService.getSerializedCommarea()).thenReturn(buildCommarea(1));
        when(appService.getEibaid()).thenReturn("'");
        doAnswer(
                        inv -> {
                            OcsgnonFields into = inv.getArgument(1);
                            into.setUseridi("ADMINUSR");
                            into.setPasswdi("ADMPW123");
                            return null;
                        })
                .when(appService)
                .receiveMap(eq("MSGNONA"), any());
        doAnswer(
                        inv -> {
                            OcsgnonFields into = inv.getArgument(1);
                            into.setUsPassword("ADMPW123");
                            into.setUsType("A");
                            return null;
                        })
                .when(appService)
                .readFile(any(), any(), any(), anyInt());
        when(appService.getEibresp()).thenReturn(0);

        service.mainLine(appService);

        verify(appService)
                .xctl(
                        argThat(program -> program.trim().equals("OCADMEN")),
                        any(),
                        eq(COMMAREA_LENGTH));
    }

    // ---------------------------------------------------------------
    // AppProgram metadata methods
    // ---------------------------------------------------------------

    @Test
    void getProgramName_returnsProgramId() {
        assertThat(service.getProgramName()).isEqualTo("OCSGNON");
    }

    @Test
    void getTransId_returnsEntryTransactionId() {
        assertThat(service.getTransId()).isEqualTo("ORSN");
    }
}
