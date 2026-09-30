package com.generated.orion.oumqbrg.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.appruntime.MqService;
import com.generated.orion.oumqbrg.domain.OumqbrgFieldAccess;
import com.generated.orion.oumqbrg.io.TranFileDataset;
import com.generated.orion.oumqbrg.runtime.OumqbrgDatasets;
import com.generated.orion.runtime.DatasetEnums.FileOpenMode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for OumqbrgService, generated from COBOL program OUMQBRG (VSAM-to-MQ transaction
 * bridge).
 *
 * <p>IMPORTANT — control-flow CONVERT-GAP: every generated paragraph method ends with an
 * unconditional "fall-through to next paragraph" direct call (not a COBOL construct — COBOL invokes
 * every paragraph here via explicit PERFORM and returns normally). Because the converter applied
 * this fall-through pattern uniformly, paragraphs that COBOL invokes exactly once via PERFORM
 * instead cascade and re-execute multiple times per run, and paragraphs COBOL never reaches on
 * certain branches (e.g. MQPUT when the file failed to open, MQCLOSE when the queue was never
 * opened) get invoked anyway. See CONVERT-GAP tests below and the findings note for details.
 * Assertions on cascading interactions therefore use never()/atLeastOnce() rather than exact
 * counts, since exact counts are an artifact of the bug, not a stable contract.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OumqbrgServiceTest {

    @Mock private MqService mq;
    @Mock private OumqbrgDatasets fileSet;
    @Spy private TranFileDataset tranFile = new TranFileDataset();

    private OumqbrgService service;

    @BeforeEach
    void setUp() {
        lenient().when(fileSet.getTranFile()).thenReturn(tranFile);

        doNothing().when(tranFile).open(any());
        doNothing().when(tranFile).close();
        doReturn(false).when(tranFile).readByKey(any());
        lenient().doReturn("00").when(tranFile).getFileStatus();

        // First READ finds a record, every subsequent READ hits end-of-file — this
        // latches WS-EOF-FLG to 'Y' quickly so the (already-buggy) cascade of
        // duplicate reads terminates instead of driving OUMQBRG's own outer
        // "PERFORM UNTIL END-OF-TRAN" loop indefinitely.
        AtomicInteger readCalls = new AtomicInteger(0);
        lenient().doAnswer(inv -> readCalls.getAndIncrement() >= 1).when(tranFile).isAtEnd();

        service = new OumqbrgService(fileSet, mq);
    }

    private static OumqbrgFieldAccess ws(OumqbrgService svc) throws Exception {
        Field f = OumqbrgService.class.getDeclaredField("ws");
        f.setAccessible(true);
        return (OumqbrgFieldAccess) f.get(svc);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_happyPath_bridgesTransactionToOutboundQueue() throws Exception {
        service.execute();

        verify(tranFile).open(FileOpenMode.INPUT);
        verify(tranFile, atLeastOnce())
                .close(); // file opened OK (FS='00') -> WS-TRAN-OPEN-FLG='Y' -> 3000-FINALIZE
        // closes it
        verify(mq, atLeastOnce()).mqOpen(any());
        verify(mq, atLeastOnce()).mqPut(any());

        OumqbrgFieldAccess accessed = ws(service);
        assertEquals("TRAN", accessed.getOmRecType());
        assertEquals("ORION   ", accessed.getOmSrcSystem());
        assertEquals(362, accessed.getMqBufferLen());
        assertEquals(0, service.getCompletionCode());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_tranFileOpenFails_convertGapStillPutsToQueue() throws Exception {
        // COBOL: OPEN INPUT TRAN-FILE fails (FS<>'00') -> SET END-OF-TRAN TO TRUE and
        // TRAN-OPEN is never set. 0000-MAIN's "PERFORM 2000-BRIDGE-TRANS UNTIL
        // END-OF-TRAN" therefore runs ZERO times and 2200-PUT-TRAN (MQPUT) is never
        // reached for this run.
        lenient().doReturn("35").when(tranFile).getFileStatus();

        service.execute();

        // CONVERT-GAP: the Java service's unconditional paragraph fall-through
        // (see class Javadoc) reaches 2200-PUT-TRAN's translation regardless of
        // WS-TRAN-OPEN-FLG, so mq.mqPut(...) IS invoked here even though the file
        // never opened. This assertion encodes the correct COBOL behavior and is
        // expected to FAIL against the current Java implementation.
        verify(mq, never()).mqPut(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_mqOpenFails_convertGapStillPutsTransactions() throws Exception {
        // COBOL: MQOPEN fails -> SET MQ-IS-DOWN TO TRUE (WS-MQ-DOWN-FLG='Y'), so
        // 2000-BRIDGE-TRANS's "IF MQ-IS-UP AND MQ-Q-OPEN" guard is false for the
        // whole run and 2200-PUT-TRAN (MQPUT) is never performed — every record is
        // counted as skipped instead.
        doAnswer(
                        inv -> {
                            Object[] params = inv.getArgument(0);
                            params[4] = MqService.MQCC_FAILED;
                            params[5] = 2059; // MQRC_Q_MGR_NOT_AVAILABLE
                            return null;
                        })
                .when(mq)
                .mqOpen(any());

        service.execute();

        // CONVERT-GAP: 2100-READ-TRAN's translation ends with an unconditional
        // fall-through call into 2200-PUT-TRAN's method — a path COBOL never has,
        // since 2200-PUT-TRAN is reachable only through 2000-BRIDGE-TRANS's guarded
        // PERFORM. So mq.mqPut(...) IS invoked here even though MQ is down.
        // Expected to FAIL against the current implementation.
        verify(mq, never()).mqPut(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_mqOpenFails_convertGapStillClosesUnopenedQueue() throws Exception {
        // Same MQOPEN failure as above. COBOL guards 3100-MQ-CLOSE-OUT with
        // "IF MQ-Q-OPEN", which stays false (the queue never opened), so
        // 3100-MQ-CLOSE-OUT (MQCLOSE) must never run.
        doAnswer(
                        inv -> {
                            Object[] params = inv.getArgument(0);
                            params[4] = MqService.MQCC_FAILED;
                            params[5] = 2059;
                            return null;
                        })
                .when(mq)
                .mqOpen(any());

        service.execute();

        // CONVERT-GAP: 3000-FINALIZE's translation ends with an unconditional
        // fall-through call to 3100-MQ-CLOSE-OUT's method (outside the "IF
        // MQ-Q-OPEN" guard that protects the runChain(...) call earlier in the
        // same method), so mq.mqClose(...) IS invoked even though the queue was
        // never opened. Expected to FAIL against the current implementation.
        verify(mq, never()).mqClose(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_readThrowsException_wrapsAndSetsCompletionCode12() {
        doThrow(new RuntimeException("VSAM I/O error")).when(tranFile).readByKey(any());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.execute());
        assertThat(ex.getMessage()).contains("Batch processing failed");
        assertEquals(12, service.getCompletionCode());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_mqPutFails_incrementsPutErrorCountOnly() throws Exception {
        // COBOL: "IF MQ-CC-OK ADD 1 TO WS-PUT-CNT ELSE ADD 1 TO WS-PUTERR-CNT".
        // Every simulated MQPUT fails here, so WS-PUT-CNT must stay 0 regardless of
        // how many times the (buggy) cascade re-enters 2200-PUT-TRAN, while
        // WS-PUTERR-CNT must be incremented at least once.
        doAnswer(
                        inv -> {
                            Object[] params = inv.getArgument(0);
                            params[6] = MqService.MQCC_FAILED;
                            params[7] = 2009; // MQRC_CONNECTION_BROKEN
                            return null;
                        })
                .when(mq)
                .mqPut(any());

        service.execute();

        OumqbrgFieldAccess accessed = ws(service);
        assertEquals(0, accessed.getWsPutCnt());
        assertThat(accessed.getWsPuterrCnt()).isGreaterThanOrEqualTo(1);
    }
}
