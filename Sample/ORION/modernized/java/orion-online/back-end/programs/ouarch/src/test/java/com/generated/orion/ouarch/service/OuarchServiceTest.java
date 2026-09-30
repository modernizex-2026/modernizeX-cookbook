package com.generated.orion.ouarch.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appruntime.AppService;
import com.generated.orion.ouarch.accessor.OuarchFields;
import com.generated.orion.ouarch.metadata.OuarchBmsMetadata;
import com.generated.orion.ouarch.model.WorkingStorage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Ground truth: OUARCH.cbl (ORION-CCMS). Every input/expected value below is derived from the COBOL
 * PROCEDURE DIVISION, not from OuarchService's current behavior.
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 10, unit = TimeUnit.SECONDS)
class OuarchServiceTest {

    private static final int RESP_NORMAL = 0;
    private static final int RESP_NOTFND = 13;
    private static final int RESP_DUPREC = 14;
    private static final int RESP_ENDFILE = 20;
    private static final int RESP_OTHER = 17;

    @Mock private AppService appService;

    private final OuarchService service = new OuarchService();
    private final AtomicInteger currentResp = new AtomicInteger(0);

    private record ReadStep(int resp, Consumer<OuarchFields> mutator) {}

    @BeforeEach
    void setUp() {
        org.mockito.Mockito.lenient().when(appService.getEibcalen()).thenReturn(1);
        org.mockito.Mockito.lenient()
                .when(appService.getEibresp())
                .thenAnswer(inv -> currentResp.get());
        org.mockito.Mockito.lenient()
                .doAnswer(
                        inv -> {
                            currentResp.set(RESP_NORMAL);
                            return null;
                        })
                .when(appService)
                .endBrowse(any());
    }

    private static OuarchFields newFields() {
        OuarchFields f = new OuarchFields(new WorkingStorage());
        f.aliasGroup("KAR-PARM", "CA-WORK-AREA");
        return f;
    }

    private byte[] buildInputCommarea(Consumer<OuarchFields> setup) {
        OuarchFields f = newFields();
        setup.accept(f);
        return f.sliceBytes("ORION-COMMAREA");
    }

    private OuarchFields runMainLine(Consumer<OuarchFields> inputSetup) {
        byte[] input = buildInputCommarea(inputSetup);
        when(appService.getCommarea()).thenReturn(input);
        service.mainLine(appService);
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(appService, times(1)).setCommarea(captor.capture());
        OuarchFields out = newFields();
        out.writeBytes("ORION-COMMAREA", (byte[]) captor.getValue());
        return out;
    }

    private void stubStartBrowse(int resp) {
        doAnswer(
                        inv -> {
                            currentResp.set(resp);
                            return null;
                        })
                .when(appService)
                .startBrowse(any(), any(), anyInt());
    }

    private void stubReadNext(List<ReadStep> steps) {
        Queue<ReadStep> queue = new ArrayDeque<>(steps);
        doAnswer(
                        inv -> {
                            ReadStep step = queue.poll();
                            OuarchFields into = (OuarchFields) inv.getArgument(1);
                            step.mutator().accept(into);
                            currentResp.set(step.resp());
                            return null;
                        })
                .when(appService)
                .readNext(any(), any());
    }

    private void stubWriteFile(int resp) {
        doAnswer(
                        inv -> {
                            currentResp.set(resp);
                            return null;
                        })
                .when(appService)
                .writeFile(any(), any(), any(), anyInt());
    }

    private void stubDeleteFile(int resp) {
        doAnswer(
                        inv -> {
                            currentResp.set(resp);
                            return null;
                        })
                .when(appService)
                .deleteFile(any(), any(), anyInt());
    }

    private static Consumer<OuarchFields> oldTransaction(
            String trId, String procDate, String amount) {
        return f -> {
            f.setTrId(trId);
            f.setTrProcTs(procDate);
            f.setTrAmt(new BigDecimal(amount));
        };
    }

    private static Consumer<OuarchFields> keptTransaction(String trId, String procDate) {
        return f -> {
            f.setTrId(trId);
            f.setTrProcTs(procDate);
            f.setTrAmt(BigDecimal.ZERO);
        };
    }

    // ---------------------------------------------------------------
    // 1000-INIT / 1100-CHK-DATE — cutoff validation (COBOL lines 96-130)
    // ---------------------------------------------------------------

    @Test
    void mainLine_cutoffMissingDashSeparators_returnsStatus99() {
        OuarchFields out =
                runMainLine(
                        f -> {
                            f.setKarCutoff("20260115  ");
                            f.setKarMax(0);
                        });

        assertEquals("99", out.getKarStatus());
        assertEquals("INVALID CUTOFF - USE YYYY-MM-DD", out.getKarMsg().trim());
        verify(appService, never()).startBrowse(any(), any(), anyInt());
    }

    @Test
    void mainLine_cutoffNonNumericParts_returnsStatus99() {
        OuarchFields out =
                runMainLine(
                        f -> {
                            f.setKarCutoff("20XX-01-15");
                            f.setKarMax(0);
                        });

        assertEquals("99", out.getKarStatus());
        assertEquals("INVALID CUTOFF - USE YYYY-MM-DD", out.getKarMsg().trim());
        verify(appService, never()).startBrowse(any(), any(), anyInt());
    }

    // ---------------------------------------------------------------
    // 2000-POSITION — STARTBR outcomes (COBOL lines 134-156)
    // ---------------------------------------------------------------

    @Test
    void mainLine_startBrowseNotFound_skipsLoopAndReturnsStatus10() {
        stubStartBrowse(RESP_NOTFND);

        OuarchFields out =
                runMainLine(
                        f -> {
                            f.setKarCutoff("2026-01-15");
                            f.setKarMax(0);
                        });

        assertEquals("10", out.getKarStatus());
        assertEquals("NO TRANSACTIONS PROCESSED", out.getKarMsg().trim());
        assertEquals(0, out.getKarRead());
        verify(appService, never()).readNext(any(), any());
        verify(appService, never()).endBrowse(any());
    }

    @Test
    void mainLine_startBrowseEndfile_skipsLoopAndReturnsStatus10() {
        stubStartBrowse(RESP_ENDFILE);

        OuarchFields out =
                runMainLine(
                        f -> {
                            f.setKarCutoff("2026-01-15");
                            f.setKarMax(0);
                        });

        assertEquals("10", out.getKarStatus());
        assertEquals("NO TRANSACTIONS PROCESSED", out.getKarMsg().trim());
        verify(appService, never()).readNext(any(), any());
    }

    @Test
    void mainLine_startBrowseOtherError_returnsStatus99WithMessage() {
        stubStartBrowse(RESP_OTHER);

        OuarchFields out =
                runMainLine(
                        f -> {
                            f.setKarCutoff("2026-01-15");
                            f.setKarMax(0);
                        });

        assertEquals("99", out.getKarStatus());
        assertEquals("TRANFILE STARTBR FAILED", out.getKarMsg().trim());
        verify(appService, never()).readNext(any(), any());
    }

    @Test
    void mainLine_startTranProvided_usesGivenKeyNotLowValues() {
        stubStartBrowse(RESP_ENDFILE);
        ArgumentCaptor<String> ridfldCaptor = ArgumentCaptor.forClass(String.class);

        runMainLine(
                f -> {
                    f.setKarCutoff("2026-01-15");
                    f.setKarMax(0);
                    f.setKarStartTran("TRAN0000000005");
                });

        verify(appService).startBrowse(any(), ridfldCaptor.capture(), anyInt());
        assertEquals("TRAN0000000005", ridfldCaptor.getValue().trim());
    }

    // ---------------------------------------------------------------
    // 3000/3100/3200 — scan loop, read errors, classify (COBOL 158-208)
    // ---------------------------------------------------------------

    @Test
    void mainLine_happyPath_archivesOldKeepsNew_returnsStatus00() {
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(
                List.of(
                        new ReadStep(
                                RESP_NORMAL,
                                oldTransaction("TRAN0000000001", "2025-06-01", "100.50")),
                        new ReadStep(RESP_NORMAL, keptTransaction("TRAN0000000002", "2026-02-01")),
                        new ReadStep(RESP_ENDFILE, f -> {})));
        stubWriteFile(RESP_NORMAL);
        stubDeleteFile(RESP_NORMAL);

        OuarchFields out =
                runMainLine(
                        f -> {
                            f.setKarCutoff("2026-01-15");
                            f.setKarMax(0);
                        });

        assertEquals("00", out.getKarStatus());
        assertEquals("TRANSACTION ARCHIVE COMPLETE", out.getKarMsg().trim());
        assertEquals(2, out.getKarRead());
        assertEquals(1, out.getKarKept());
        assertEquals(1, out.getKarArchived());
        assertEquals(1, out.getKarDeleted());
        assertEquals(0, out.getKarErrors());
        assertEquals(0, new BigDecimal("100.50").compareTo(out.getKarArchAmt()));
        assertEquals("N", out.getKarMore().trim());
        verify(appService, times(3)).readNext(any(), any());
        verify(appService, times(1)).writeFile(any(), any(), any(), anyInt());
        verify(appService, times(1)).deleteFile(any(), any(), anyInt());
    }

    @Test
    void mainLine_readNextError_setsEofIncrementsErrorsAndOverwritesMsg() {
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(List.of(new ReadStep(RESP_OTHER, f -> {})));

        OuarchFields out =
                runMainLine(
                        f -> {
                            f.setKarCutoff("2026-01-15");
                            f.setKarMax(0);
                        });

        // 3100-READ-TRAN sets KAR-MSG to the READNEXT failure, but 6000-SET-STATUS
        // unconditionally overwrites it when KAR-READ is still zero (COBOL lines 189-192, 296-298).
        assertEquals("10", out.getKarStatus());
        assertEquals("NO TRANSACTIONS PROCESSED", out.getKarMsg().trim());
        assertEquals(0, out.getKarRead());
        assertEquals(1, out.getKarErrors());
        verify(appService, times(1)).readNext(any(), any());
    }

    @Test
    void mainLine_browseOnButImmediateEndfile_endsBrowseAndReturnsStatus10() {
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(List.of(new ReadStep(RESP_ENDFILE, f -> {})));

        OuarchFields out =
                runMainLine(
                        f -> {
                            f.setKarCutoff("2026-01-15");
                            f.setKarMax(0);
                        });

        assertEquals("10", out.getKarStatus());
        assertEquals(0, out.getKarRead());
        verify(appService, times(1)).endBrowse(any());
    }

    // ---------------------------------------------------------------
    // WS-CAP / WS-MAX capping + 4000-PEEK-NEXT (COBOL 160-172, 210-235)
    // ---------------------------------------------------------------

    @Test
    void mainLine_maxCapReached_peeksNextAndPublishesResumeKey() {
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(
                List.of(
                        new ReadStep(
                                RESP_NORMAL,
                                oldTransaction("TRAN0000000001", "2025-06-01", "10.00")),
                        new ReadStep(
                                RESP_NORMAL, keptTransaction("TRAN0000000099", "2099-01-01"))));
        stubWriteFile(RESP_NORMAL);
        stubDeleteFile(RESP_NORMAL);

        OuarchFields out =
                runMainLine(
                        f -> {
                            f.setKarCutoff("2026-01-15");
                            f.setKarMax(1);
                        });

        assertEquals("Y", out.getKarMore().trim());
        assertEquals("TRAN0000000099", out.getKarNextTran().trim());
        assertEquals(1, out.getKarRead());
        assertEquals(1, out.getKarArchived());
        verify(appService, times(2)).readNext(any(), any());
    }

    @Test
    void mainLine_peekNextOtherError_setsNoMoreAndIncrementsErrors() {
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(
                List.of(
                        new ReadStep(
                                RESP_NORMAL,
                                oldTransaction("TRAN0000000001", "2025-06-01", "10.00")),
                        new ReadStep(RESP_OTHER, f -> {})));
        stubWriteFile(RESP_NORMAL);
        stubDeleteFile(RESP_NORMAL);

        OuarchFields out =
                runMainLine(
                        f -> {
                            f.setKarCutoff("2026-01-15");
                            f.setKarMax(1);
                        });

        assertEquals("N", out.getKarMore().trim());
        assertEquals(1, out.getKarErrors());
    }

    @Test
    void mainLine_peekNextEndfile_setsNoMoreWithoutErrors() {
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(
                List.of(
                        new ReadStep(
                                RESP_NORMAL,
                                oldTransaction("TRAN0000000001", "2025-06-01", "10.00")),
                        new ReadStep(RESP_ENDFILE, f -> {})));
        stubWriteFile(RESP_NORMAL);
        stubDeleteFile(RESP_NORMAL);

        OuarchFields out =
                runMainLine(
                        f -> {
                            f.setKarCutoff("2026-01-15");
                            f.setKarMax(1);
                        });

        assertEquals("N", out.getKarMore().trim());
        assertEquals(0, out.getKarErrors());
    }

    // ---------------------------------------------------------------
    // 5600-ARCHIVE-ONE / 5700-DELETE-ONE (COBOL 257-288)
    // ---------------------------------------------------------------

    @Test
    void mainLine_archiveWriteDuprec_stillDeletesWithoutCountingArchived() {
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(
                List.of(
                        new ReadStep(
                                RESP_NORMAL,
                                oldTransaction("TRAN0000000001", "2025-06-01", "10.00")),
                        new ReadStep(RESP_ENDFILE, f -> {})));
        stubWriteFile(RESP_DUPREC);
        stubDeleteFile(RESP_NORMAL);

        OuarchFields out =
                runMainLine(
                        f -> {
                            f.setKarCutoff("2026-01-15");
                            f.setKarMax(0);
                        });

        // COBOL 5600-ARCHIVE-ONE: WHEN DFHRESP(DUPREC) skips the ARCHIVED/ARCH-AMT bump
        // but still performs the delete.
        assertEquals(0, out.getKarArchived());
        assertEquals(1, out.getKarDeleted());
        assertEquals(0, new BigDecimal("0").compareTo(out.getKarArchAmt()));
    }

    @Test
    void mainLine_archiveWriteOtherError_incrementsErrorsAndSkipsDelete() {
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(
                List.of(
                        new ReadStep(
                                RESP_NORMAL,
                                oldTransaction("TRAN0000000001", "2025-06-01", "10.00")),
                        new ReadStep(RESP_ENDFILE, f -> {})));
        stubWriteFile(RESP_OTHER);

        OuarchFields out =
                runMainLine(
                        f -> {
                            f.setKarCutoff("2026-01-15");
                            f.setKarMax(0);
                        });

        assertEquals(0, out.getKarArchived());
        assertEquals(0, out.getKarDeleted());
        assertEquals(1, out.getKarErrors());
        verify(appService, never()).deleteFile(any(), any(), anyInt());
    }

    @Test
    void mainLine_deleteOtherError_keepsArchivedCountButIncrementsErrors() {
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(
                List.of(
                        new ReadStep(
                                RESP_NORMAL,
                                oldTransaction("TRAN0000000001", "2025-06-01", "10.00")),
                        new ReadStep(RESP_ENDFILE, f -> {})));
        stubWriteFile(RESP_NORMAL);
        stubDeleteFile(RESP_OTHER);

        OuarchFields out =
                runMainLine(
                        f -> {
                            f.setKarCutoff("2026-01-15");
                            f.setKarMax(0);
                        });

        assertEquals(1, out.getKarArchived());
        assertEquals(0, out.getKarDeleted());
        assertEquals(1, out.getKarErrors());
    }

    // ---------------------------------------------------------------
    // mainLine commarea marshalling — CALL BY REFERENCE transport (Object[] wrapping
    // a byte[] element), an alternate shape alongside the plain byte[] COMMAREA used
    // by every other test in this class.
    // ---------------------------------------------------------------

    @Test
    void mainLine_commareaAsObjectArrayOfBytes_marshalsInAndOutViaSameArray() {
        stubStartBrowse(RESP_ENDFILE);
        byte[] input =
                buildInputCommarea(
                        f -> {
                            f.setKarCutoff("2026-01-15");
                            f.setKarMax(0);
                        });
        Object[] params = new Object[] {input};
        when(appService.getCommarea()).thenReturn(params);

        service.mainLine(appService);

        // Object[] transport mutates params[0] in place instead of calling setCommarea.
        verify(appService, never()).setCommarea(any());
        OuarchFields out = newFields();
        out.writeBytes("ORION-COMMAREA", (byte[]) params[0]);
        assertEquals("10", out.getKarStatus());
        assertEquals("NO TRANSACTIONS PROCESSED", out.getKarMsg().trim());
    }

    // ---------------------------------------------------------------
    // AppProgram plumbing
    // ---------------------------------------------------------------

    @Test
    void getProgramName_returnsOuarch() {
        assertEquals("OUARCH", service.getProgramName());
    }

    @Test
    void getButtonDefs_delegatesToMetadata() {
        assertEquals(OuarchBmsMetadata.getButtonDefs(), service.getButtonDefs());
    }

    @Test
    void registerFsetFields_delegatesToMetadataWithoutThrowing() {
        // OuarchBmsMetadata.registerFsetFields is a no-op that never dereferences the
        // runner, so a real AppRunner instance is unnecessary here.
        assertDoesNotThrow(() -> service.registerFsetFields(null));
    }
}
