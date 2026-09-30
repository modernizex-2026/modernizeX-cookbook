package com.sakura.ms0110.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.ms0110.domain.Ms0110FieldAccess;
import com.sakura.ms0110.runtime.Ms0110Datasets;
import com.sakura.runtime.DatasetEnums.FileOpenMode;
import com.sakura.runtime.ScreenModels.InputFieldDef;
import com.sakura.runtime.ScreenModels.ScreenDef;
import com.sakura.runtime.ScreenRendererInstance;
import com.sakura.runtime.io.RegnfDataset;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Tests for Ms0110Service (COBOL MS0110 - region master maintenance). Ground truth: MS0110.cob
 * PROCEDURE DIVISION paragraphs.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Timeout(value = 10, unit = TimeUnit.SECONDS)
class Ms0110ServiceTest {

    @Mock private DateutService dateutService;
    @Mock private AbortxService abortxService;
    @Mock private ScreenRendererInstance renderer;
    @Mock private Ms0110Datasets fileSet;
    @Spy private RegnfDataset regnfFile = new RegnfDataset();

    private Ms0110Service service;

    /**
     * Wraps the same shared RG-* record buffer as the service's internal ws - lets tests seed "DB
     * record found" state.
     */
    private Ms0110FieldAccess recordProbe;

    private final List<String> screenInteractions = new ArrayList<>();
    private final Map<String, String> fieldValues = new HashMap<>();

    @BeforeEach
    void setUp() {
        when(fileSet.getRegnf()).thenReturn(regnfFile);

        doNothing().when(regnfFile).open(any());
        doNothing().when(regnfFile).close();
        doReturn("00").when(regnfFile).getFileStatus();
        doReturn(false).when(regnfFile).isInvalidKey();
        doReturn(true).when(regnfFile).readByKey(any());
        doReturn("").when(regnfFile).extractKeyFromCurrentRecord();
        doNothing().when(regnfFile).write();
        doNothing().when(regnfFile).rewrite();

        service = new Ms0110Service(fileSet, dateutService, abortxService, renderer);
        recordProbe = new Ms0110FieldAccess(null, fileSet);

        doAnswer(
                        inv -> {
                            ScreenDef def = inv.getArgument(0);
                            Ms0110FieldAccess fields = inv.getArgument(1);
                            StringBuilder sb = new StringBuilder("displayScreen:").append(def.name);
                            if ("DS-MSG".equals(def.name)) {
                                sb.append(":").append(fields.getWkMsgLine().trim());
                            }
                            screenInteractions.add(sb.toString());
                            return null;
                        })
                .when(renderer)
                .displayScreen(any(), any());

        when(renderer.acceptField(any()))
                .thenAnswer(
                        inv -> {
                            InputFieldDef field = inv.getArgument(0);
                            return fieldValues.getOrDefault(field.name, "");
                        });
    }

    @AfterEach
    void tearDown() {
        screenInteractions.clear();
        fieldValues.clear();
    }

    // ───────────────────── INIT-RTN (initializeAndOpenRegnf) ─────────────────────

    @Test
    void execute_normalOpenSuccess_opensIoModeOnceAndTerminatesOnPf3() {
        when(renderer.readEndStatus()).thenReturn("03");

        service.execute();

        verify(regnfFile, times(1)).open(FileOpenMode.IO);
        verify(regnfFile, never()).open(FileOpenMode.OUTPUT);
        verify(regnfFile, times(1)).close();
        assertThat(service.getCompletionCode()).isZero();
    }

    @Test
    void execute_fileStatus35OnOpen_reopensAsOutputThenIo() {
        doReturn("35", "00", "00", "00").when(regnfFile).getFileStatus();
        when(renderer.readEndStatus()).thenReturn("03");

        service.execute();

        verify(regnfFile, times(2)).open(FileOpenMode.IO);
        verify(regnfFile, times(1)).open(FileOpenMode.OUTPUT);
        // CLOSE called once for the OUTPUT->IO reopen sequence, once more at TERM-RTN.
        verify(regnfFile, times(2)).close();
        assertThat(service.getCompletionCode()).isZero();
    }

    @Test
    void execute_fileStatusNotZeroAfterOpen_abendsWithCompletionCode255() {
        doReturn("99").when(regnfFile).getFileStatus();

        service.execute();

        verify(abortxService, times(1)).execute(any());
        assertThat(service.getCompletionCode()).isEqualTo(255);
        verify(renderer, never()).displayScreen(any(), any());
    }

    // ───────────────────── MAIN-RTN-010 (processMainScreenKey dispatch) ─────────────────────

    @Test
    void execute_pf3OnKeyScreen_setsEndFlagAndClosesFile() {
        when(renderer.readEndStatus()).thenReturn("03");

        service.execute();

        assertThat(screenInteractions).doesNotContain("displayScreen:DS-MSG:Invalid function key");
        verify(regnfFile, times(1)).close();
        assertThat(service.getCompletionCode()).isZero();
    }

    @Test
    void execute_invalidFunctionKeyOnMainScreen_showsMessageAndLoopsAgain() {
        when(renderer.readEndStatus()).thenReturn("09", "03");

        service.execute();

        assertThat(screenInteractions).contains("displayScreen:DS-MSG:Invalid function key");
    }

    // ───────────────────── PKEY-010 (lookupRegionByKey) ─────────────────────

    @Test
    void execute_regionCodeZero_showsErrorAndSkipsRead() {
        fieldValues.put("SC-KEY", "0");
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        assertThat(screenInteractions)
                .contains("displayScreen:DS-MSG:Region code must not be zero");
        verify(regnfFile, never()).readByKey(any());
    }

    // ───────────────────── SADD-010 / SAVE-010 (add flow) ─────────────────────

    @Test
    void execute_newRegionValidName_writesRecordAndShowsAdded() {
        fieldValues.put("SC-KEY", "7");
        fieldValues.put("RG-NAME", "Test Region");
        doReturn(true, true, false, false).when(regnfFile).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "00", "03");

        service.execute();

        assertThat(screenInteractions)
                .contains(
                        "displayScreen:DS-MSG:New region - enter details",
                        "displayScreen:DS-MSG:Region added");
        verify(regnfFile, times(1)).write();
        verify(regnfFile, never()).rewrite();
    }

    @Test
    void execute_newRegionWriteDuplicateKey_showsWriteFailed() {
        fieldValues.put("SC-KEY", "7");
        fieldValues.put("RG-NAME", "Test Region");
        doReturn(true, true, true, true).when(regnfFile).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "00", "03");

        service.execute();

        assertThat(screenInteractions).contains("displayScreen:DS-MSG:Write failed - duplicate");
        verify(regnfFile, times(1)).write();
    }

    // ───────────────────── SCHG-010 / SAVE-010 (change flow) ─────────────────────

    @Test
    void execute_existingActiveRecord_setsChangeModeAndUpdatesOnSave() {
        fieldValues.put("SC-KEY", "7");
        fieldValues.put("RG-NAME", "Updated Name");
        // read: found + active (RG-DEL-FLAG defaults to 0 after CLEAR-010's setRecord()).
        doReturn(false, false, false, false).when(regnfFile).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "00", "03");

        service.execute();

        assertThat(screenInteractions)
                .contains(
                        "displayScreen:DS-MSG:Existing region - change or PF9 delete",
                        "displayScreen:DS-MSG:Region updated");
        verify(regnfFile, times(1)).rewrite();
        verify(regnfFile, never()).write();
    }

    @Test
    void execute_existingActiveRecordRewriteFails_showsUpdateFailed() {
        fieldValues.put("SC-KEY", "7");
        fieldValues.put("RG-NAME", "Updated Name");
        doReturn(false, false, true, true).when(regnfFile).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "00", "03");

        service.execute();

        assertThat(screenInteractions).contains("displayScreen:DS-MSG:Update failed");
        verify(regnfFile, times(1)).rewrite();
    }

    @Test
    void execute_previouslyDeletedRecordFound_reregistersViaWriteNotRewrite() {
        fieldValues.put("SC-KEY", "7");
        fieldValues.put("RG-NAME", "Reborn Region");
        // Seed the shared record buffer with RG-DEL-FLAG=1 at the exact moment READ succeeds.
        doAnswer(
                        inv -> {
                            recordProbe.setRgDelFlag(1);
                            return true;
                        })
                .when(regnfFile)
                .readByKey(any());
        doReturn(false, false, false, false).when(regnfFile).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "00", "03");

        service.execute();

        assertThat(screenInteractions)
                .contains(
                        "displayScreen:DS-MSG:Deleted region - re-registering",
                        "displayScreen:DS-MSG:Region added");
        verify(regnfFile, times(1)).write();
        verify(regnfFile, never()).rewrite();
    }

    // ───────────────────── DEL-010 (confirmAndDeleteRegion) ─────────────────────

    @Test
    void execute_deleteConfirmedWithY_setsDelFlagAndRewrites() {
        fieldValues.put("SC-KEY", "7");
        fieldValues.put("RG-NAME", "Any");
        fieldValues.put("SC-CONF", "Y");
        // read: found + active -> MODE-CHG; rewrite: success.
        doReturn(false, false, false, false).when(regnfFile).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "09", "00", "03");

        service.execute();

        assertThat(screenInteractions)
                .contains(
                        "displayScreen:DS-MSG:Press Y then ENTER to delete",
                        "displayScreen:DS-MSG:Region deleted");
        verify(regnfFile, times(1)).rewrite();
        verify(regnfFile, never()).write();
    }

    @Test
    void execute_deleteConfirmedWithLowercaseY_setsDelFlagAndRewrites() {
        fieldValues.put("SC-KEY", "7");
        fieldValues.put("RG-NAME", "Any");
        fieldValues.put("SC-CONF", "y");
        doReturn(false, false, false, false).when(regnfFile).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "09", "00", "03");

        service.execute();

        assertThat(screenInteractions).contains("displayScreen:DS-MSG:Region deleted");
        verify(regnfFile, times(1)).rewrite();
    }

    @Test
    void execute_deleteCancelledWithN_leavesRecordUntouched() {
        fieldValues.put("SC-KEY", "7");
        fieldValues.put("RG-NAME", "Any");
        fieldValues.put("SC-CONF", "N");
        doReturn(false, false).when(regnfFile).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "09", "00", "03");

        service.execute();

        assertThat(screenInteractions).contains("displayScreen:DS-MSG:Delete cancelled");
        verify(regnfFile, never()).rewrite();
    }

    @Test
    void execute_deletePressedInAddMode_showsNothingToDelete() {
        fieldValues.put("SC-KEY", "7");
        fieldValues.put("RG-NAME", "Any");
        // read: not found -> MODE-ADD, so PF9 in EDIT-010 must not attempt delete.
        doReturn(true, true).when(regnfFile).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "09", "03");

        service.execute();

        assertThat(screenInteractions).contains("displayScreen:DS-MSG:Nothing to delete");
        verify(regnfFile, never()).rewrite();
    }

    // ───────────────────── EDIT-010 (processRegionEditScreen dispatch) ─────────────────────

    @Test
    void execute_editScreenEsts04_showsCancelledAndSkipsSave() {
        fieldValues.put("SC-KEY", "7");
        fieldValues.put("RG-NAME", "Any");
        doReturn(false, false).when(regnfFile).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "04", "03");

        service.execute();

        assertThat(screenInteractions).contains("displayScreen:DS-MSG:Cancelled");
        verify(regnfFile, never()).write();
        verify(regnfFile, never()).rewrite();
    }

    @Test
    void execute_editScreenUnknownEsts_showsInvalidFunctionKey() {
        fieldValues.put("SC-KEY", "7");
        fieldValues.put("RG-NAME", "Any");
        doReturn(false, false).when(regnfFile).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "77", "03");

        service.execute();

        assertThat(screenInteractions).contains("displayScreen:DS-MSG:Invalid function key");
        verify(regnfFile, never()).write();
        verify(regnfFile, never()).rewrite();
    }

    // ───────────────────── VAL-010 (validateRegionName) ─────────────────────

    @Test
    void execute_blankRegionName_showsErrorAndSkipsSave() {
        fieldValues.put("SC-KEY", "7");
        fieldValues.put("RG-NAME", "   ");
        doReturn(true, true).when(regnfFile).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "00", "03");

        service.execute();

        assertThat(screenInteractions).contains("displayScreen:DS-MSG:Region name is required");
        verify(regnfFile, never()).write();
        verify(regnfFile, never()).rewrite();
    }
}
