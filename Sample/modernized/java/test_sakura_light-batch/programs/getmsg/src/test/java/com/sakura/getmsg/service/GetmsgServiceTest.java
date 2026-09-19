package com.sakura.getmsg.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.sakura.getmsg.runtime.GetmsgDatasets;
import com.sakura.runtime.DatasetEnums.FileOpenMode;
import com.sakura.runtime.io.MsgfDataset;
import com.sakura.runtime.linkage.GetmsgLinkParm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.TimeUnit;

/**
 * Unit tests for GetmsgService, converted from COBOL program GETMSG. Ground truth: GETMSG.cob
 * MAIN-000 paragraph.
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 10, unit = TimeUnit.SECONDS)
class GetmsgServiceTest {

    private MsgfDataset msgf;
    private GetmsgDatasets fileSet;
    private GetmsgService service;

    @BeforeEach
    void setUp() {
        msgf = spy(new MsgfDataset());
        GetmsgDatasets realFileSet = new GetmsgDatasets();
        fileSet = spy(realFileSet);
        doReturn(msgf).when(fileSet).getMsgf();
        service = new GetmsgService(fileSet);
    }

    private GetmsgLinkParm newParams(String kmCode, String kmText, String kmStatus) {
        GetmsgLinkParm params = new GetmsgLinkParm();
        params.getKmsg().setKmCode(kmCode);
        params.getKmsg().setKmText(kmText);
        params.getKmsg().setKmStatus(kmStatus);
        return params;
    }

    @Test
    void execute_keyFoundInMsgFile_returnsMessageTextWithStatus00() {
        // Given: COBOL - OPEN succeeds (FSTS="00"), READ finds the key (NOT INVALID KEY -> KM-TEXT
        // = MG-TEXT)
        String msgText = "E".repeat(60);
        doNothing().when(msgf).open(any());
        doReturn("00").when(msgf).getFileStatus();
        doReturn(true).when(msgf).readByKey(anyString());
        doReturn(false).when(msgf).isInvalidKey();
        doNothing().when(msgf).close();
        msgf.buffer().setString("MG-TEXT", msgText);

        GetmsgLinkParm params = newParams("MSG001", "garbage", "ZZ");

        // When
        service.execute(params);

        // Then
        assertEquals(msgText, params.getKmsg().getKmText());
        assertEquals("00", params.getKmsg().getKmStatus());
        assertEquals("MSG001", params.getKmsg().getKmCode());
        verify(msgf).open(FileOpenMode.INPUT);
        verify(msgf).readByKey("MSG001");
        verify(msgf).close();
    }

    @Test
    void execute_keyNotFoundInMsgFile_returnsCodeAsTextWithStatus01() {
        // Given: COBOL - OPEN succeeds, READ ... INVALID KEY -> KM-TEXT = KM-CODE, KM-STATUS = "01"
        doNothing().when(msgf).open(any());
        doReturn("00").when(msgf).getFileStatus();
        doReturn(false).when(msgf).readByKey(anyString());
        doReturn(true).when(msgf).isInvalidKey();
        doNothing().when(msgf).close();

        // Initial garbage in linkage params must be fully overwritten by the paragraph.
        GetmsgLinkParm params = newParams("NOTFND", "old-stale-text", "ZZ");

        // When
        service.execute(params);

        // Then
        assertEquals("NOTFND", params.getKmsg().getKmText());
        assertEquals("01", params.getKmsg().getKmStatus());
        verify(msgf).open(FileOpenMode.INPUT);
        verify(msgf).readByKey("NOTFND");
        verify(msgf).close();
    }

    @Test
    void execute_openFails_returnsCodeAsTextWithStatus99AndSkipsReadAndClose() {
        // Given: COBOL - OPEN INPUT MSGF fails (FSTS NOT = "00") ->
        // KM-TEXT = KM-CODE, KM-STATUS = "99", EXIT PROGRAM before READ/CLOSE.
        doNothing().when(msgf).open(any());
        doReturn("35").when(msgf).getFileStatus();

        GetmsgLinkParm params = newParams("MSG002", " ", "00");

        // When
        service.execute(params);

        // Then
        assertEquals("MSG002", params.getKmsg().getKmText());
        assertEquals("99", params.getKmsg().getKmStatus());
        verify(msgf).open(FileOpenMode.INPUT);
        verify(msgf, never()).readByKey(any());
        verify(msgf, never()).close();
    }

    @Test
    void execute_blankCode_invalidKey_returnsBlankAsTextWithStatus01() {
        // Edge: KM-CODE is all spaces (no message code supplied) -> still treated
        // like any other invalid-key lookup by the COBOL paragraph.
        doNothing().when(msgf).open(any());
        doReturn("00").when(msgf).getFileStatus();
        doReturn(false).when(msgf).readByKey(anyString());
        doReturn(true).when(msgf).isInvalidKey();
        doNothing().when(msgf).close();

        String blankCode = " ".repeat(6);
        GetmsgLinkParm params = newParams(blankCode, " ", "00");

        // When
        service.execute(params);

        // Then
        assertEquals(blankCode, params.getKmsg().getKmText());
        assertEquals("01", params.getKmsg().getKmStatus());
    }

    @Test
    void execute_noArg_whenOpenThrows_setsCompletionCode12AndWrapsException() {
        // Error path: an unexpected exception during the paragraph bubbles up through
        // BatchServiceBase.execute(), which sets COBOL COMPLETION-CODE = 12 and rethrows.
        doThrow(new RuntimeException("I/O failure")).when(msgf).open(any());

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> service.execute());

        assertThat(thrown.getMessage()).contains("Batch processing failed");
        assertEquals(12, service.getCompletionCode());
    }
}
