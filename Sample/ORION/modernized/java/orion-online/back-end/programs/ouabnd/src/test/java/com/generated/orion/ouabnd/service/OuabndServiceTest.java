package com.generated.orion.ouabnd.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appruntime.AppService;
import com.generated.orion.ouabnd.accessor.OuabndFields;
import com.generated.orion.ouabnd.model.WorkingStorage;

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
 * Unit tests for OuabndService, generated from COBOL program OUABND (abnormal-end handler). Ground
 * truth for expected behavior is /ORION-CCMS/cbl/OUABND.cbl paragraph 0000-MAIN: STRING 'ABEND IN '
 * KA-PROGRAM ' AT ' KA-PARAGRAPH ' - ' KA-DETAIL INTO WS-ABEND-MSG EXEC CICS WRITEQ TD
 * QUEUE('CSSL') FROM(WS-ABEND-MSG) LENGTH(120) EXEC CICS ABEND ABCODE(WS-ABCODE) -- WS-ABCODE VALUE
 * 'OABN'
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Timeout(value = 10, unit = TimeUnit.SECONDS)
class OuabndServiceTest {

    private static final String QUEUE_CSSL = "CSSL";
    private static final int ABEND_MSG_LENGTH = 120;

    /** WS-ABEND-MSG PIC X(120) truncates the STRING result silently (no ON OVERFLOW clause). */
    private static final int TRUNCATE_LEN = 120;

    @Mock private AppService appService;

    private final OuabndService service = new OuabndService();

    @BeforeEach
    void setUp() {
        when(appService.getEibresp()).thenReturn(0);
    }

    /**
     * Builds the KA-PROGRAM/KA-PARAGRAPH/KA-DETAIL fields via the real layout, mirroring how a
     * caller populates LINKAGE SECTION KABND-PARM before EXEC CICS LINK.
     */
    private static OuabndFields buildParmFields(String program, String paragraph, String detail) {
        OuabndFields f = new OuabndFields(new WorkingStorage());
        if (program != null) f.setKaProgram(program);
        if (paragraph != null) f.setKaParagraph(paragraph);
        if (detail != null) f.setKaDetail(detail);
        return f;
    }

    /**
     * Expected COBOL STRING result: full-width DELIMITED SIZE concatenation truncated to
     * WS-ABEND-MSG's declared size (120) -- COBOL STRING truncates silently past target size.
     */
    private static String expectedAbendMessage(OuabndFields parm) {
        String full =
                "ABEND IN "
                        + parm.getKaProgram()
                        + " AT "
                        + parm.getKaParagraph()
                        + " - "
                        + parm.getKaDetail();
        return full.length() > TRUNCATE_LEN ? full.substring(0, TRUNCATE_LEN) : full;
    }

    // ---------------------------------------------------------------
    // 0000-MAIN happy path — byte[] COMMAREA
    // ---------------------------------------------------------------

    @Test
    void mainLine_byteArrayCommarea_buildsTruncatedAbendMessageAndPublishesUnchangedCommarea() {
        OuabndFields parm =
                buildParmFields(
                        "OCACCTA",
                        "1000-VALIDATE-INPUT",
                        "SQLCODE -803 ON UPDATE OF CUSTOMER TABLE - DUPLICATE KEY VIOLATION"
                                + " DETECTED");
        byte[] commarea = parm.sliceBytes("KABND-PARM");
        String expectedMessage = expectedAbendMessage(parm);

        when(appService.getEibcalen()).thenReturn(commarea.length);
        when(appService.getCommarea()).thenReturn(commarea);

        service.mainLine(appService);

        ArgumentCaptor<Object> fromCaptor = ArgumentCaptor.forClass(Object.class);
        verify(appService).writeQueueTd(eq(QUEUE_CSSL), fromCaptor.capture(), eq(ABEND_MSG_LENGTH));
        OuabndFields sent = (OuabndFields) fromCaptor.getValue();
        assertThat(sent.getWsAbendMsg()).isEqualTo(expectedMessage);
        assertThat(sent.getWsRespCd()).isZero();
        verify(appService, times(2)).getEibresp();
        verify(appService).abend(anyString());

        ArgumentCaptor<byte[]> publishedCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(appService).setCommarea(publishedCaptor.capture());
        // KABND-PARM (KA-PROGRAM/KA-PARAGRAPH/KA-RESP-CD/KA-REAS-CD/KA-DETAIL) is never
        // written by 0000-MAIN, so the republished COMMAREA must round-trip unchanged.
        assertThat(publishedCaptor.getValue()).containsExactly(commarea);
    }

    // ---------------------------------------------------------------
    // CONVERT-GAP: EXEC CICS ABEND ABCODE(WS-ABCODE)
    // ---------------------------------------------------------------

    @Test
    void mainLine_abendCall_shouldPassResolvedAbcodeFieldValuePerCobol() {
        // CONVERT-GAP: COBOL 'EXEC CICS ABEND ABCODE(WS-ABCODE)' resolves the data-name
        // WS-ABCODE (VALUE 'OABN') to its runtime value as the 4-char abend code. The
        // converted Java (OuabndService.abendProgram) instead calls
        // ctx.appService.abend("WS-ABCODE") -- the literal field NAME string, never reading
        // ctx.f.getWsAbcode(). Every abend raised by this program is tagged "WS-ABCODE"
        // instead of "OABN", breaking any downstream abend-code-based diagnostics/routing.
        // Expected per COBOL: "OABN". This test is intentionally failing against current Java.
        OuabndFields parm = buildParmFields("OCACCTA", "1000-VALIDATE-INPUT", "detail");
        byte[] commarea = parm.sliceBytes("KABND-PARM");
        when(appService.getEibcalen()).thenReturn(commarea.length);
        when(appService.getCommarea()).thenReturn(commarea);

        service.mainLine(appService);

        ArgumentCaptor<String> abcodeCaptor = ArgumentCaptor.forClass(String.class);
        verify(appService).abend(abcodeCaptor.capture());
        assertThat(abcodeCaptor.getValue()).isEqualTo("OABN");
    }

    // ---------------------------------------------------------------
    // COMMAREA marshaling edge cases (Java CICS-commarea framework plumbing,
    // no direct COBOL correspondence -- COBOL LINKAGE SECTION is typed, not polymorphic)
    // ---------------------------------------------------------------

    @Test
    void mainLine_objectArrayCommareaWithByteElement_marshalsViaWriteBytesAndMutatesArrayInPlace() {
        OuabndFields parm = buildParmFields("OCACCTL", "2000-UPDATE", "detail message");
        byte[] commarea = parm.sliceBytes("KABND-PARM");
        Object[] params = new Object[] {commarea};
        when(appService.getEibcalen()).thenReturn(commarea.length);
        when(appService.getCommarea()).thenReturn(params);

        service.mainLine(appService);

        ArgumentCaptor<Object> fromCaptor = ArgumentCaptor.forClass(Object.class);
        verify(appService).writeQueueTd(eq(QUEUE_CSSL), fromCaptor.capture(), eq(ABEND_MSG_LENGTH));
        OuabndFields sent = (OuabndFields) fromCaptor.getValue();
        assertThat(sent.getWsAbendMsg()).isEqualTo(expectedAbendMessage(parm));

        assertThat(params[0]).isInstanceOf(byte[].class);
        assertThat((byte[]) params[0]).containsExactly(commarea);
        verify(appService, never()).setCommarea(any());
    }

    @Test
    void
            mainLine_objectArrayCommareaWithNonByteElement_marshalsViaSetGroupAndMutatesArrayInPlace() {
        Object[] params = new Object[] {"RAW-STRING-COMMAREA"};
        when(appService.getEibcalen()).thenReturn(126);
        when(appService.getCommarea()).thenReturn(params);

        service.mainLine(appService);

        verify(appService).writeQueueTd(eq(QUEUE_CSSL), any(), eq(ABEND_MSG_LENGTH));
        verify(appService).abend(anyString());
        // finally: original element was not byte[], so it is republished via groupToString.
        assertThat(params[0]).isInstanceOf(String.class);
        verify(appService, never()).setCommarea(any());
    }

    @Test
    void mainLine_stringCommarea_marshalsViaSetGroupPathAndPublishesBytes() {
        when(appService.getEibcalen()).thenReturn(126);
        when(appService.getCommarea()).thenReturn("PLAIN-STRING-COMMAREA");

        service.mainLine(appService);

        ArgumentCaptor<Object> fromCaptor = ArgumentCaptor.forClass(Object.class);
        verify(appService).writeQueueTd(eq(QUEUE_CSSL), fromCaptor.capture(), eq(ABEND_MSG_LENGTH));
        OuabndFields sent = (OuabndFields) fromCaptor.getValue();
        assertThat(sent.getWsAbendMsg()).startsWith("ABEND IN ");

        ArgumentCaptor<byte[]> publishedCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(appService).setCommarea(publishedCaptor.capture());
        assertThat(publishedCaptor.getValue()).hasSize(126);
    }

    @Test
    void mainLine_eibcalenZero_skipsCommareaMarshalAndRepublish() {
        when(appService.getEibcalen()).thenReturn(0);

        OuabndFields blank = new OuabndFields(new WorkingStorage());
        String expectedMessage = expectedAbendMessage(blank);

        service.mainLine(appService);

        ArgumentCaptor<Object> fromCaptor = ArgumentCaptor.forClass(Object.class);
        verify(appService).writeQueueTd(eq(QUEUE_CSSL), fromCaptor.capture(), eq(ABEND_MSG_LENGTH));
        OuabndFields sent = (OuabndFields) fromCaptor.getValue();
        assertThat(sent.getWsAbendMsg()).isEqualTo(expectedMessage);
        verify(appService).abend(anyString());

        verify(appService, never()).getCommarea();
        verify(appService, never()).setCommarea(any());
    }

    @Test
    void mainLine_eibcalenPositiveButCommareaNull_skipsMarshalInButStillRepublishesOnExit() {
        when(appService.getEibcalen()).thenReturn(10);
        when(appService.getCommarea()).thenReturn(null);

        service.mainLine(appService);

        // Outer guard (eibcalen>0 && commarea!=null) is false -> no writeBytes/setGroup in.
        // Finally guard only checks eibcalen>0 -> still republishes (asymmetric by design).
        verify(appService).writeQueueTd(eq(QUEUE_CSSL), any(), eq(ABEND_MSG_LENGTH));
        verify(appService).setCommarea(any(byte[].class));
    }

    // ---------------------------------------------------------------
    // Trivial AppProgram delegate methods
    // ---------------------------------------------------------------

    @Test
    void getProgramName_returnsOuabnd() {
        assertThat(service.getProgramName()).isEqualTo("OUABND");
    }

    @Test
    void getButtonDefsAndRegisterFsetFields_delegateToBmsMetadata() {
        assertThat(service.getButtonDefs()).isEmpty();
        service.registerFsetFields(null);
    }
}
