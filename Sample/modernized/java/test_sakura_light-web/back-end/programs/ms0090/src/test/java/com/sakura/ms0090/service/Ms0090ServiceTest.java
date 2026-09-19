package com.sakura.ms0090.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.ms0090.domain.Ms0090FieldAccess;
import com.sakura.ms0090.runtime.Ms0090Datasets;
import com.sakura.runtime.DatasetEnums.FileOpenMode;
import com.sakura.runtime.ScreenModels.ScreenDef;
import com.sakura.runtime.ScreenRendererInstance;
import com.sakura.runtime.io.TaxfDataset;
import com.sakura.runtime.record.RuntimeFieldAccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Unit tests for Ms0090Service (COBOL MS0090 — tax rate master maintenance), generated from {@code
 * MS0090.cob} PROCEDURE DIVISION as ground truth.
 *
 * <p>Test strategy: TAXF is a real dataset object wrapped with {@code spy()} so the record buffer
 * (and therefore {@code Ms0090FieldAccess}) works exactly as in production; only I/O methods
 * (open/close/readByKey/isInvalidKey/write/rewrite/ delete/getFileStatus) are stubbed so no real DB
 * access happens. DATEUT runs for real (pure calendar math, matches the COBOL copy exactly for
 * TODY/VALD) so date validation in PKEY-010 is exercised faithfully. ABORTX is mocked (irrelevant
 * to file I/O).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Timeout(value = 10, unit = TimeUnit.SECONDS)
class Ms0090ServiceTest {

    @Mock private ScreenRendererInstance renderer;
    @Mock private AbortxService abortxService;

    private DateutService dateutService;

    private Ms0090Datasets fileSet;
    private TaxfDataset taxf;

    private Ms0090Service service;

    private final Map<String, String> acceptValues = new HashMap<>();
    private final List<String> screenInteractions = new ArrayList<>();
    private final AtomicBoolean recordFound = new AtomicBoolean(false);
    private final AtomicBoolean invalidKeyFlag = new AtomicBoolean(false);

    @BeforeEach
    void setUp() {
        dateutService = new DateutService();

        Ms0090Datasets real = new Ms0090Datasets();
        fileSet = spy(real);
        taxf = spy(real.getTaxf());
        doReturn(taxf).when(fileSet).getTaxf();

        doNothing().when(taxf).open(any());
        doNothing().when(taxf).close();
        doReturn("00").when(taxf).getFileStatus();

        doAnswer(inv -> invalidKeyFlag.get()).when(taxf).isInvalidKey();

        doAnswer(
                        inv -> {
                            boolean found = recordFound.get();
                            invalidKeyFlag.set(!found);
                            return found;
                        })
                .when(taxf)
                .readByKey(any());

        doAnswer(
                        inv -> {
                            invalidKeyFlag.set(false);
                            return null;
                        })
                .when(taxf)
                .write();
        doAnswer(
                        inv -> {
                            invalidKeyFlag.set(false);
                            return null;
                        })
                .when(taxf)
                .rewrite();
        doAnswer(
                        inv -> {
                            invalidKeyFlag.set(false);
                            return null;
                        })
                .when(taxf)
                .delete();

        acceptValues.put("TX-CODE", "1");
        acceptValues.put("TX-START-DATE", "20260101");
        acceptValues.put("TX-RATE", "0.100");
        acceptValues.put("TX-NAME", "Standard Rate");
        acceptValues.put("SC-CONF", "Y");
        when(renderer.acceptField(any()))
                .thenAnswer(
                        inv -> {
                            var field =
                                    inv.getArgument(
                                            0, com.sakura.runtime.ScreenModels.InputFieldDef.class);
                            return acceptValues.getOrDefault(field.name, "");
                        });

        doAnswer(
                        inv -> {
                            ScreenDef def = inv.getArgument(0);
                            if ("DS-MSG".equals(def.name)) {
                                Ms0090FieldAccess fa = (Ms0090FieldAccess) inv.getArgument(1);
                                screenInteractions.add(
                                        "displayScreen:DS-MSG:" + fa.getWkMsgLine().trim());
                            } else {
                                screenInteractions.add("displayScreen:" + def.name);
                            }
                            return null;
                        })
                .when(renderer)
                .displayScreen(any(), any());

        service = new Ms0090Service(fileSet, dateutService, abortxService, renderer);
    }

    /**
     * Live handle onto the service's WorkingStorage/FD field accessor, obtained from the first
     * displayScreen() call. It is the SAME mutable object throughout the run, so end-of-run state
     * is visible directly on it.
     */
    private Ms0090FieldAccess capturedWs() {
        ArgumentCaptor<RuntimeFieldAccess> captor =
                ArgumentCaptor.forClass(RuntimeFieldAccess.class);
        verify(renderer, org.mockito.Mockito.atLeastOnce()).displayScreen(any(), captor.capture());
        return (Ms0090FieldAccess) captor.getAllValues().get(0);
    }

    // ───────────────────────── happy path ─────────────────────────

    @Test
    void execute_pf3AtFirstPrompt_endsImmediatelyWithoutLookup() {
        when(renderer.readEndStatus()).thenReturn("03");

        service.execute();

        verify(taxf, never()).readByKey(any());
        verify(taxf, times(1)).open(FileOpenMode.IO);
        verify(taxf, times(1)).close();
        assertThat(service.getCompletionCode()).isEqualTo(0);
        assertThat(screenInteractions)
                .contains("displayScreen:DS-HEADER", "displayScreen:DS-FOOTER");
    }

    @Test
    void execute_newTaxCodeAddedWithValidDetails_writesRecordAndShowsAddedMessage() {
        recordFound.set(false);
        when(renderer.readEndStatus()).thenReturn("00", "00", "03");

        service.execute();

        verify(taxf, times(1)).write();
        verify(taxf, never()).rewrite();
        assertThat(screenInteractions).contains("displayScreen:DS-MSG:Tax rate added");
        assertThat(capturedWs().getModeFlg()).isEqualTo(1);
    }

    @Test
    void execute_existingTaxCodeUpdatedWithValidDetails_rewritesRecordAndShowsUpdatedMessage() {
        recordFound.set(true);
        when(renderer.readEndStatus()).thenReturn("00", "00", "03");

        service.execute();

        verify(taxf, times(1)).rewrite();
        verify(taxf, never()).write();
        assertThat(screenInteractions).contains("displayScreen:DS-MSG:Tax rate updated");
        assertThat(capturedWs().getModeFlg()).isEqualTo(2);
    }

    @Test
    void execute_deleteExistingRecordConfirmedWithY_deletesRecordAndShowsDeletedMessage() {
        recordFound.set(true);
        acceptValues.put("SC-CONF", "Y");
        when(renderer.readEndStatus()).thenReturn("00", "09", "03");

        service.execute();

        verify(taxf, times(1)).delete();
        assertThat(screenInteractions).contains("displayScreen:DS-MSG:Tax rate deleted");
    }

    // ───────────────────────── edge cases (ground truth: PKEY-010 / VAL-010 / DEL-010)
    // ─────────────────────────

    @Test
    void execute_taxCodeZero_rejectsWithRequiredMessageWithoutReadingTaxf() {
        acceptValues.put("TX-CODE", "0");
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        verify(taxf, never()).readByKey(any());
        assertThat(screenInteractions).contains("displayScreen:DS-MSG:Tax category must be 1 to 9");
    }

    @Test
    void execute_startDateZero_rejectsWithRequiredMessageWithoutReadingTaxf() {
        acceptValues.put("TX-START-DATE", "0");
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        verify(taxf, never()).readByKey(any());
        assertThat(screenInteractions).contains("displayScreen:DS-MSG:Start date is required");
    }

    @Test
    void execute_startDateInvalidCalendarDate_rejectsWithInvalidMessageWithoutReadingTaxf() {
        // ground truth: DATEUT VALD returns KD-STATUS <> "00" for a day out of range (e.g. day 32).
        acceptValues.put("TX-START-DATE", "20260132");
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        verify(taxf, never()).readByKey(any());
        assertThat(screenInteractions).contains("displayScreen:DS-MSG:Start date is invalid");
    }

    @Test
    void execute_invalidFunctionKeyAtMainPrompt_showsInvalidKeyMessageAndContinuesLoop() {
        when(renderer.readEndStatus()).thenReturn("99", "03");

        service.execute();

        verify(taxf, never()).readByKey(any());
        assertThat(screenInteractions).contains("displayScreen:DS-MSG:Invalid function key");
    }

    @Test
    void execute_editScreenCancelled_showsCancelledMessageWithoutSaving() {
        recordFound.set(true);
        when(renderer.readEndStatus()).thenReturn("00", "04", "03");

        service.execute();

        verify(taxf, never()).write();
        verify(taxf, never()).rewrite();
        assertThat(screenInteractions).contains("displayScreen:DS-MSG:Cancelled");
    }

    @Test
    void execute_editScreenInvalidFunctionKey_showsInvalidKeyMessage() {
        recordFound.set(true);
        when(renderer.readEndStatus()).thenReturn("00", "77", "03");

        service.execute();

        verify(taxf, never()).write();
        verify(taxf, never()).rewrite();
        assertThat(screenInteractions).contains("displayScreen:DS-MSG:Invalid function key");
    }

    @Test
    void execute_deleteRequestedInAddMode_showsNothingToDeleteAndDoesNotDelete() {
        recordFound.set(false); // MODE-ADD (new record, never persisted)
        when(renderer.readEndStatus()).thenReturn("00", "09", "03");

        service.execute();

        verify(taxf, never()).delete();
        assertThat(screenInteractions).contains("displayScreen:DS-MSG:Nothing to delete");
    }

    @Test
    void execute_deleteConfirmedWithLowercaseY_deletesRecord() {
        recordFound.set(true);
        acceptValues.put("SC-CONF", "y");
        when(renderer.readEndStatus()).thenReturn("00", "09", "03");

        service.execute();

        verify(taxf, times(1)).delete();
        assertThat(screenInteractions).contains("displayScreen:DS-MSG:Tax rate deleted");
    }

    @Test
    void execute_deleteNotConfirmed_showsCancelledAndDoesNotDelete() {
        recordFound.set(true);
        acceptValues.put("SC-CONF", "N");
        when(renderer.readEndStatus()).thenReturn("00", "09", "03");

        service.execute();

        verify(taxf, never()).delete();
        assertThat(screenInteractions).contains("displayScreen:DS-MSG:Delete cancelled");
    }

    @Test
    void execute_deleteFails_showsDeleteFailedMessage() {
        recordFound.set(true);
        acceptValues.put("SC-CONF", "Y");
        doAnswer(
                        inv -> {
                            invalidKeyFlag.set(true);
                            return null;
                        })
                .when(taxf)
                .delete();
        when(renderer.readEndStatus()).thenReturn("00", "09", "03");

        service.execute();

        verify(taxf, times(1)).delete();
        assertThat(screenInteractions).contains("displayScreen:DS-MSG:Delete failed");
    }

    @Test
    void execute_rateNegative_rejectsWithoutSaving() {
        recordFound.set(false);
        acceptValues.put("TX-RATE", "-0.010");
        when(renderer.readEndStatus()).thenReturn("00", "00", "03");

        service.execute();

        verify(taxf, never()).write();
        assertThat(screenInteractions).contains("displayScreen:DS-MSG:Rate cannot be negative");
    }

    @Test
    void execute_rateAtOrAboveOne_rejectsWithoutSaving() {
        recordFound.set(false);
        acceptValues.put("TX-RATE", "1.000");
        when(renderer.readEndStatus()).thenReturn("00", "00", "03");

        service.execute();

        verify(taxf, never()).write();
        assertThat(screenInteractions)
                .contains("displayScreen:DS-MSG:Rate must be a fraction below 1.000");
    }

    @Test
    void execute_rateNameBlank_rejectsWithRateNameRequiredMessage() {
        recordFound.set(false);
        acceptValues.put("TX-NAME", "");
        when(renderer.readEndStatus()).thenReturn("00", "00", "03");

        service.execute();

        verify(taxf, never()).write();
        assertThat(screenInteractions).contains("displayScreen:DS-MSG:Rate name is required");
    }

    @Test
    void execute_writeFailsDuplicateKey_showsWriteFailedMessage() {
        recordFound.set(false);
        doAnswer(
                        inv -> {
                            invalidKeyFlag.set(true);
                            return null;
                        })
                .when(taxf)
                .write();
        when(renderer.readEndStatus()).thenReturn("00", "00", "03");

        service.execute();

        assertThat(screenInteractions).contains("displayScreen:DS-MSG:Write failed - duplicate");
    }

    @Test
    void execute_rewriteFails_showsUpdateFailedMessage() {
        recordFound.set(true);
        doAnswer(
                        inv -> {
                            invalidKeyFlag.set(true);
                            return null;
                        })
                .when(taxf)
                .rewrite();
        when(renderer.readEndStatus()).thenReturn("00", "00", "03");

        service.execute();

        assertThat(screenInteractions).contains("displayScreen:DS-MSG:Update failed");
    }

    // ───────────────────────── file-open lifecycle (ground truth: INIT-RTN / ABEND-RTN)
    // ─────────────────────────

    @Test
    void execute_taxfOpenFailsPersistently_abortsWithCompletionCode255() {
        doReturn("99").when(taxf).getFileStatus();
        when(renderer.readEndStatus()).thenReturn("03");

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        verify(abortxService, times(1)).execute(any());
    }

    @Test
    void execute_taxfOpenStatus35_reopensAsOutputThenReopensAsInput() {
        when(taxf.getFileStatus()).thenReturn("35", "00", "00");
        when(renderer.readEndStatus()).thenReturn("03");

        service.execute();

        verify(taxf, times(3)).open(any());
        verify(taxf, times(1)).open(FileOpenMode.OUTPUT);
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }

    @Test
    void execute_taxfOpenStatus30_reopensAsOutputThenReopensAsInput() {
        when(taxf.getFileStatus()).thenReturn("30", "00", "00");
        when(renderer.readEndStatus()).thenReturn("03");

        service.execute();

        verify(taxf, times(3)).open(any());
        verify(taxf, times(1)).open(FileOpenMode.OUTPUT);
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }

    // ───────────────────────── error path ─────────────────────────

    @Test
    void execute_unexpectedRuntimeExceptionDuringProcessing_wrapsAndSetsCompletionCode12() {
        doReturn(true).when(taxf).readByKey(any());
        doThrow(new RuntimeException("simulated DB failure")).when(taxf).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "03");

        assertThrows(RuntimeException.class, () -> service.execute());

        assertThat(service.getCompletionCode()).isEqualTo(12);
    }
}
