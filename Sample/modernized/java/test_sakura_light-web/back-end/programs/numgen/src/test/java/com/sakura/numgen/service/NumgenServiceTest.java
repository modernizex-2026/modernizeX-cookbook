package com.sakura.numgen.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.sakura.numgen.domain.NumgenFieldAccess;
import com.sakura.numgen.io.NumcfDataset;
import com.sakura.numgen.runtime.NumgenDatasets;
import com.sakura.runtime.DatasetEnums.FileOpenMode;
import com.sakura.runtime.linkage.NumgenLinkParm;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * Unit tests for {@link NumgenService}, derived from COBOL program NUMGEN (next document-number
 * assigner). Ground truth for inputs/expected values: NUMGEN.cob PROCEDURE DIVISION (MAIN-000 /
 * OPEN-010 / ASSIGN-010 / CREATE-010).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NumgenServiceTest {

    @Spy private NumcfDataset numcfSpy = new NumcfDataset();

    private NumgenService service;
    private NumgenFieldAccess ws;

    private static class TestDatasets extends NumgenDatasets {
        private final NumcfDataset numcf;

        TestDatasets(NumcfDataset numcf) {
            this.numcf = numcf;
        }

        @Override
        public NumcfDataset getNumcf() {
            return numcf;
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        NumgenDatasets fileSet = new TestDatasets(numcfSpy);
        service = new NumgenService(fileSet);
        ws = getWs();

        doNothing().when(numcfSpy).open(any());
        doNothing().when(numcfSpy).close();
        doNothing().when(numcfSpy).write();
        doNothing().when(numcfSpy).rewrite();
        doReturn("00").when(numcfSpy).getFileStatus();
        doReturn(false).when(numcfSpy).isInvalidKey();
        doReturn(true).when(numcfSpy).readByKey(any());
    }

    @AfterEach
    void tearDown() {
        // no shared mutable capture fields to reset
    }

    /* ── reflection helpers ── */

    private NumgenFieldAccess getWs() throws Exception {
        Field f = NumgenService.class.getDeclaredField("ws");
        f.setAccessible(true);
        return (NumgenFieldAccess) f.get(service);
    }

    private void invokePrivate(String name) {
        try {
            Method m = NumgenService.class.getDeclaredMethod(name);
            m.setAccessible(true);
            m.invoke(service);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException(cause);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private NumgenLinkParm newParm(String key) {
        NumgenLinkParm p = new NumgenLinkParm();
        p.getKnum().setKnumKey(key);
        p.getKnum().setKnumNumber(0L);
        p.getKnum().setKnumStatus("  ");
        return p;
    }

    /* ── OPEN-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void openNumcfCreateIfMissing_fsts00_opensIoOnlyOnce() {
        invokePrivate("openNumcfCreateIfMissing");

        verify(numcfSpy, times(1)).open(FileOpenMode.IO);
        verify(numcfSpy, never()).open(FileOpenMode.OUTPUT);
        verify(numcfSpy, never()).close();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void openNumcfCreateIfMissing_fsts35_reopensAsOutputThenIo() {
        doReturn("35").when(numcfSpy).getFileStatus();

        invokePrivate("openNumcfCreateIfMissing");

        verify(numcfSpy, times(2)).open(FileOpenMode.IO);
        verify(numcfSpy, times(1)).open(FileOpenMode.OUTPUT);
        verify(numcfSpy, times(1)).close();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void openNumcfCreateIfMissing_fsts30_reopensAsOutputThenIo() {
        doReturn("30").when(numcfSpy).getFileStatus();

        invokePrivate("openNumcfCreateIfMissing");

        verify(numcfSpy, times(2)).open(FileOpenMode.IO);
        verify(numcfSpy, times(1)).open(FileOpenMode.OUTPUT);
        verify(numcfSpy, times(1)).close();
    }

    /* ── MAIN-000 / ASSIGN-010 (via public execute()) ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_existingSeriesFound_incrementsAndReturnsNextNumber() {
        ws.setNmCurrent(41);
        doReturn(false).when(numcfSpy).isInvalidKey(); // key found -> NOT INVALID KEY branch
        NumgenLinkParm params = newParm("SALES001");

        service.execute(params);

        assertEquals(42L, params.getKnum().getKnumNumber());
        assertEquals("00", params.getKnum().getKnumStatus());
        verify(numcfSpy, times(1)).rewrite();
        verify(numcfSpy, never()).write();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_newSeries_convertGap_expectedFirstNumberButJavaDoubleIncrements() {
        // isInvalidKey() call sequence: after READ (true=not found), after CREATE-010's
        // WRITE (false=success), and the outer NOT-INVALID-KEY re-check (false) that COBOL
        // never performs (its READ INVALID KEY / NOT INVALID KEY branches are mutually
        // exclusive on a single READ; Java re-tests isInvalidKey() as two separate ifs).
        doReturn(true, false, false).when(numcfSpy).isInvalidKey();
        NumgenLinkParm params = newParm("NEWKEY01");

        service.execute(params);

        // CONVERT-GAP: COBOL CREATE-010 writes NM-CURRENT=1 and returns KNUM-NUMBER=1 for a
        // brand-new series. Java's assignNextNumber() re-enters the "NOT INVALID KEY" branch
        // after CREATE-010 succeeds (isInvalidKey() still false), incrementing NM-CURRENT to 2
        // and issuing a spurious REWRITE that COBOL never performs.
        assertEquals(1L, params.getKnum().getKnumNumber());
        verify(numcfSpy, times(1)).write();
        verify(numcfSpy, never()).rewrite();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_openFileErrorStatus_setsStatus99AndClosesFileWithoutAssigning() {
        doReturn("93").when(numcfSpy).getFileStatus();
        NumgenLinkParm params = newParm("SALES001");

        service.execute(params);

        assertEquals("99", params.getKnum().getKnumStatus());
        verify(numcfSpy, times(1)).close();
        verify(numcfSpy, never()).readByKey(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_rewriteFailsOnExistingSeries_setsStatus99ButStillReturnsIncrementedNumber() {
        ws.setNmCurrent(5);
        doReturn(false, false, true).when(numcfSpy).isInvalidKey();
        NumgenLinkParm params = newParm("SALES001");

        service.execute(params);

        assertEquals("99", params.getKnum().getKnumStatus());
        assertEquals(6L, params.getKnum().getKnumNumber());
        verify(numcfSpy, times(1)).rewrite();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_writeFailsOnNewSeries_setsStatus99AndReturnsFirstNumber() {
        doReturn(true, true, true).when(numcfSpy).isInvalidKey();
        NumgenLinkParm params = newParm("NEWKEY01");

        service.execute(params);

        assertEquals("99", params.getKnum().getKnumStatus());
        assertEquals(1L, params.getKnum().getKnumNumber());
        verify(numcfSpy, times(1)).write();
        verify(numcfSpy, never()).rewrite();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_preservesKnumKeyAcrossCall() {
        ws.setNmCurrent(0);
        doReturn(false).when(numcfSpy).isInvalidKey();
        NumgenLinkParm params = newParm("SALES009");

        service.execute(params);

        assertEquals("SALES009", params.getKnum().getKnumKey().trim());
    }
}
