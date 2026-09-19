package com.sakura.abortx.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sakura.abortx.io.LogfDataset;
import com.sakura.abortx.runtime.AbortxDatasets;
import com.sakura.runtime.DatasetEnums.FileOpenMode;
import com.sakura.runtime.linkage.AbortxLinkParm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * Ground truth: Sakura/sub/ABORTX.cob — MAIN-000 paragraph. MOVE KA-* TO WL-*; DISPLAY message;
 * OPEN EXTEND LOGF; IF FSTS = "35" OR "30" THEN OPEN OUTPUT LOGF; IF FSTS = "00" THEN WRITE LOG-REC
 * and CLOSE LOGF; EXIT PROGRAM.
 */
@ExtendWith(MockitoExtension.class)
class AbortxServiceTest {

    @Mock private AbortxDatasets fileSet;

    private LogfDataset logfSpy;
    private AbortxService service;

    @TempDir Path tempDir;

    @BeforeEach
    void setUp() {
        logfSpy = Mockito.spy(new LogfDataset());
        when(fileSet.getLogf()).thenReturn(logfSpy);
        service = new AbortxService(fileSet);
    }

    private AbortxLinkParm buildParams(
            String progid, String file, String fsts, String msgcode, String detail) {
        AbortxLinkParm params = new AbortxLinkParm();
        params.getKabend().setKaProgid(progid);
        params.getKabend().setKaFile(file);
        params.getKabend().setKaFsts(fsts);
        params.getKabend().setKaMsgcode(msgcode);
        params.getKabend().setKaDetail(detail);
        return params;
    }

    private static String expectedWkLine(
            String progid, String file, String fsts, String msgcode, String detail) {
        return "ABEND " + progid + " F" + file + " S" + fsts + " M" + msgcode + " " + detail;
    }

    private String readLoggedLine() throws IOException {
        Path logFile = tempDir.resolve("ABEND.LOG");
        byte[] raw = Files.readAllBytes(logFile);
        return new String(raw, StandardCharsets.US_ASCII);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_fileStatusOk_writesAbendLineAndEchoesParamsBack() throws IOException {
        logfSpy.setPath(tempDir.resolve("ABEND.LOG").toString());
        AbortxLinkParm params =
                buildParams(
                        "PROGID",
                        "FILENAME",
                        "30",
                        "MSGC01",
                        "0123456789012345678901234567890123456789");

        service.execute(params);

        String expectedLine =
                expectedWkLine(
                        "PROGID",
                        "FILENAME",
                        "30",
                        "MSGC01",
                        "0123456789012345678901234567890123456789");
        assertThat(readLoggedLine()).contains(expectedLine);
        assertThat(logfSpy.getFileStatus()).isEqualTo("00");
        assertThat(service.getCompletionCode()).isEqualTo(0);

        // CALL semantics: KABEND fields are echoed back to the caller's buffer unchanged.
        assertThat(params.getKabend().getKaProgid()).isEqualTo("PROGID");
        assertThat(params.getKabend().getKaFile()).isEqualTo("FILENAME");
        assertThat(params.getKabend().getKaFsts()).isEqualTo("30");
        assertThat(params.getKabend().getKaMsgcode()).isEqualTo("MSGC01");
        assertThat(params.getKabend().getKaDetail())
                .isEqualTo("0123456789012345678901234567890123456789");
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_fieldMapping_assemblesLineFromEachKabendField() throws IOException {
        logfSpy.setPath(tempDir.resolve("ABEND.LOG").toString());
        AbortxLinkParm params =
                buildParams(
                        "ABCDEF",
                        "GHIJKLMN",
                        "05",
                        "MSG777",
                        "DETAIL-TEXT-0123456789ABCDEFGHIJKLMNOPQ");

        service.execute(params);

        String expectedLine =
                expectedWkLine(
                        "ABCDEF",
                        "GHIJKLMN",
                        "05",
                        "MSG777",
                        "DETAIL-TEXT-0123456789ABCDEFGHIJKLMNOPQ");
        assertThat(readLoggedLine()).contains(expectedLine);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_openExtendFails35_retriesWithOpenOutputThenWrites() {
        doNothing().when(logfSpy).open(any());
        doNothing().when(logfSpy).write();
        doNothing().when(logfSpy).close();
        doReturn("35", "00").when(logfSpy).getFileStatus();

        AbortxLinkParm params =
                buildParams(
                        "PROGID",
                        "FILENAME",
                        "35",
                        "MSGC01",
                        "0123456789012345678901234567890123456789");
        service.execute(params);

        verify(logfSpy).open(FileOpenMode.EXTEND);
        verify(logfSpy).open(FileOpenMode.OUTPUT);
        verify(logfSpy, times(1)).write();
        verify(logfSpy, times(1)).close();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_openExtendFails30_retriesWithOpenOutputThenWrites() {
        doNothing().when(logfSpy).open(any());
        doNothing().when(logfSpy).write();
        doNothing().when(logfSpy).close();
        // fieldEquals("35") false -> fieldEquals("30") true -> retry; final check -> "00" -> write
        // path.
        doReturn("30", "30", "00").when(logfSpy).getFileStatus();

        AbortxLinkParm params =
                buildParams(
                        "PROGID",
                        "FILENAME",
                        "30",
                        "MSGC01",
                        "0123456789012345678901234567890123456789");
        service.execute(params);

        verify(logfSpy).open(FileOpenMode.EXTEND);
        verify(logfSpy).open(FileOpenMode.OUTPUT);
        verify(logfSpy, times(1)).write();
        verify(logfSpy, times(1)).close();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_fileStatusStaysBad_skipsWriteAndCloseButStillReturnsToCaller() {
        doNothing().when(logfSpy).open(any());
        // Every check keeps seeing "30": retry triggers, final IF FSTS="00" is false -> no
        // write/close.
        doReturn("30").when(logfSpy).getFileStatus();

        AbortxLinkParm params =
                buildParams(
                        "PROGID",
                        "FILENAME",
                        "30",
                        "MSGC01",
                        "0123456789012345678901234567890123456789");
        service.execute(params);

        verify(logfSpy).open(FileOpenMode.EXTEND);
        verify(logfSpy).open(FileOpenMode.OUTPUT);
        verify(logfSpy, never()).write();
        verify(logfSpy, never()).close();
        verify(fileSet, times(1)).commit();
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_noArgEntryPoint_runsMainProcessWithDefaultBlankKabend() throws IOException {
        logfSpy.setPath(tempDir.resolve("ABEND.LOG").toString());

        service.execute();

        String expectedLine =
                expectedWkLine(
                        "      ",
                        "        ",
                        "  ",
                        "      ",
                        "                                        ");
        assertThat(readLoggedLine()).contains(expectedLine);
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }
}
