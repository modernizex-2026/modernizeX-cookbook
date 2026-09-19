package com.sakura.ms0080.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.ms0080.domain.Ms0080FieldAccess;
import com.sakura.ms0080.runtime.Ms0080Datasets;
import com.sakura.runtime.DatasetEnums.FileOpenMode;
import com.sakura.runtime.ScreenModels.InputFieldDef;
import com.sakura.runtime.ScreenModels.ScreenDef;
import com.sakura.runtime.ScreenRendererInstance;
import com.sakura.runtime.io.DeptfDataset;

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

import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Unit tests for Ms0080Service (COBOL MS0080 - Department master maintenance). Ground truth:
 * MS0080.cob PROCEDURE DIVISION (MAIN-RTN / PKEY / EDIT / VAL / SAVE / DEL / LOOK / INIT / ABEND).
 *
 * <p>The program is a screen-driven ACCEPT loop: after every business action MAIN-RTN-010 blanks
 * WK-MSG-LINE and redraws the screen on the NEXT iteration, so a message is only observable at the
 * moment it is displayed - assertions below check {@link #screenInteractions} (captured at
 * displayScreen() call time), never the service's final field state.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Timeout(value = 10, unit = TimeUnit.SECONDS)
class Ms0080ServiceTest {

    private static final class DeptRecord {
        final String name;
        final int parent;
        final int delFlag;

        DeptRecord(String name, int parent, int delFlag) {
            this.name = name;
            this.parent = parent;
            this.delFlag = delFlag;
        }
    }

    @Mock private ScreenRendererInstance renderer;

    @Mock private DateutService dateutService;

    @Mock private AbortxService abortxService;

    @Spy private DeptfDataset deptfSpy = new DeptfDataset();

    private Ms0080Datasets fileSet;
    private Ms0080Service service;

    private final AtomicReference<Ms0080FieldAccess> wsRef = new AtomicReference<>();
    private final Map<String, DeptRecord> fakeDb = new HashMap<>();
    private final AtomicBoolean invalidKeyFlag = new AtomicBoolean(false);
    private final AtomicBoolean writeInvalid = new AtomicBoolean(false);
    private final Deque<String> fileStatusQueue = new ArrayDeque<>();
    private final Map<String, Deque<String>> acceptQueues = new HashMap<>();
    private final Deque<String> estsQueue = new ArrayDeque<>();
    private final List<String> screenInteractions = new ArrayList<>();
    private final List<String> parentNameAtBodyDisplay = new ArrayList<>();
    private final List<Integer> delFlagAtRewrite = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        fileSet = new Ms0080Datasets();
        Field deptfField = Ms0080Datasets.class.getDeclaredField("deptf");
        deptfField.setAccessible(true);
        deptfField.set(fileSet, deptfSpy);

        service = new Ms0080Service(fileSet, dateutService, abortxService, renderer);

        doNothing().when(deptfSpy).open(any());
        doNothing().when(deptfSpy).close();

        doAnswer(
                        inv ->
                                fileStatusQueue.isEmpty()
                                        ? (invalidKeyFlag.get() ? "23" : "00")
                                        : fileStatusQueue.poll())
                .when(deptfSpy)
                .getFileStatus();
        doAnswer(inv -> invalidKeyFlag.get()).when(deptfSpy).isInvalidKey();
        doAnswer(inv -> "").when(deptfSpy).extractKeyFromCurrentRecord();

        // Simulates DEPTF: readByKey(key) looks up fakeDb and populates the
        // shared record buffer (same buffer the field accessor exposes), exactly
        // as COBOL READ DEPTF would populate DP-REC.
        doAnswer(
                        inv -> {
                            Object rawKey = inv.getArgument(0);
                            String key = String.valueOf(rawKey).trim();
                            DeptRecord rec = fakeDb.get(key);
                            Ms0080FieldAccess f = wsRef.get();
                            if (rec != null) {
                                invalidKeyFlag.set(false);
                                if (f != null) {
                                    f.setDpName(rec.name);
                                    f.setDpParent(rec.parent);
                                    f.setDpDelFlag(rec.delFlag);
                                }
                            } else {
                                invalidKeyFlag.set(true);
                            }
                            return !invalidKeyFlag.get();
                        })
                .when(deptfSpy)
                .readByKey(any());

        doAnswer(
                        inv -> {
                            invalidKeyFlag.set(writeInvalid.get());
                            return null;
                        })
                .when(deptfSpy)
                .write();
        doAnswer(
                        inv -> {
                            invalidKeyFlag.set(writeInvalid.get());
                            Ms0080FieldAccess f = wsRef.get();
                            if (f != null) {
                                delFlagAtRewrite.add(f.getDpDelFlag());
                            }
                            return null;
                        })
                .when(deptfSpy)
                .rewrite();

        doAnswer(
                        inv -> {
                            ScreenDef def = inv.getArgument(0);
                            Ms0080FieldAccess f = (Ms0080FieldAccess) inv.getArgument(1);
                            wsRef.compareAndSet(null, f);
                            screenInteractions.add(
                                    "displayScreen:" + def.name + ":" + f.getWkMsgLine().trim());
                            if ("DS-BODY".equals(def.name)) {
                                parentNameAtBodyDisplay.add(f.getWkParentName().trim());
                            }
                            return null;
                        })
                .when(renderer)
                .displayScreen(any(), any());

        doAnswer(
                        inv -> {
                            InputFieldDef field = inv.getArgument(0);
                            Deque<String> q = acceptQueues.get(field.name);
                            return (q != null && !q.isEmpty()) ? q.poll() : "0000";
                        })
                .when(renderer)
                .acceptField(any());

        doAnswer(inv -> estsQueue.isEmpty() ? "03" : estsQueue.poll())
                .when(renderer)
                .readEndStatus();
    }

    @AfterEach
    void tearDown() {
        fakeDb.clear();
        acceptQueues.clear();
        estsQueue.clear();
        screenInteractions.clear();
        parentNameAtBodyDisplay.clear();
        delFlagAtRewrite.clear();
        fileStatusQueue.clear();
        invalidKeyFlag.set(false);
        writeInvalid.set(false);
        wsRef.set(null);
    }

    private void queueAccept(String fieldName, String... values) {
        Deque<String> q = acceptQueues.computeIfAbsent(fieldName, k -> new ArrayDeque<>());
        for (String v : values) {
            q.add(v);
        }
    }

    /**
     * Asserts that WK-MSG-LINE held {@code expected} at some DS-MSG display, before the next loop
     * iteration's CLEAR-WORK blanked it back out.
     */
    private void assertMessageDisplayed(String expected) {
        assertThat(screenInteractions).contains("displayScreen:DS-MSG:" + expected);
    }

    // ---------------------------------------------------------------
    // INIT-RTN
    // ---------------------------------------------------------------

    @Test
    void execute_fileStatus35OnOpen_reopensAsOutputThenIoAndContinues() {
        fileStatusQueue.add("35");
        queueAccept("SC-KEY", "0000");
        // ESTS queue empty -> defaults to "03" on first key screen -> ends loop immediately.

        service.execute();

        verify(deptfSpy, times(2)).open(FileOpenMode.IO);
        verify(deptfSpy, times(1)).open(FileOpenMode.OUTPUT);
        verify(abortxService, never()).execute(any());
        assertThat(service.getCompletionCode()).isNotEqualTo(255);
    }

    @Test
    void execute_fileStatusErrorOnOpen_abortsWithCompletionCode255() {
        fileStatusQueue.add("99");

        service.execute();

        verify(abortxService, times(1)).execute(any());
        verify(deptfSpy, never()).close();
        assertThat(service.getCompletionCode()).isEqualTo(255);
        verify(renderer, never()).displayScreen(argThat(d -> d.name.equals("DS-HEADER")), any());
    }

    // ---------------------------------------------------------------
    // MAIN-RTN / PKEY
    // ---------------------------------------------------------------

    @Test
    void execute_dpCodeZero_showsErrorAndNeverReadsFile() {
        queueAccept("SC-KEY", "0000");
        estsQueue.add("00");

        service.execute();

        assertMessageDisplayed("Department code must not be zero");
        verify(deptfSpy, never()).readByKey(any());
    }

    @Test
    void execute_invalidFunctionKeyAtKeyScreen_showsInvalidFunctionKeyMessage() {
        queueAccept("SC-KEY", "0000");
        estsQueue.add("05");

        service.execute();

        assertMessageDisplayed("Invalid function key");
        verify(deptfSpy, never()).readByKey(any());
    }

    // ---------------------------------------------------------------
    // Add flow (SADD-010 / EDIT-010 / VAL-010 / SAVE-010)
    // ---------------------------------------------------------------

    @Test
    void execute_addNewDepartmentNoParent_writesRecordAndShowsAddedMessage() {
        queueAccept("SC-KEY", "0100", "0000");
        queueAccept("DP-NAME", "Sales Dept");
        queueAccept("DP-PARENT", "0000");
        estsQueue.add("00"); // key accept -> processDeptCodeKey
        estsQueue.add("00"); // body accept -> save

        service.execute();

        verify(deptfSpy, times(1)).write();
        verify(deptfSpy, never()).rewrite();
        assertMessageDisplayed("Department added");
    }

    @Test
    void execute_addWithValidParent_looksUpParentAndSaves() {
        fakeDb.put("5", new DeptRecord("Parent Dept", 0, 0));
        queueAccept("SC-KEY", "0100", "0000");
        queueAccept("DP-NAME", "Child Dept");
        queueAccept("DP-PARENT", "0005");
        estsQueue.add("00");
        estsQueue.add("00");

        service.execute();

        verify(deptfSpy, times(1)).write();
        assertMessageDisplayed("Department added");
    }

    @Test
    void execute_validateNameBlank_showsErrorAndDoesNotSave() {
        queueAccept("SC-KEY", "0200", "0000");
        queueAccept("DP-NAME", "");
        queueAccept("DP-PARENT", "0000");
        estsQueue.add("00");
        estsQueue.add("00");

        service.execute();

        assertMessageDisplayed("Name is required");
        verify(deptfSpy, never()).write();
        verify(deptfSpy, never()).rewrite();
    }

    @Test
    void execute_validateParentEqualsSelf_showsErrorAndDoesNotSave() {
        queueAccept("SC-KEY", "0100", "0000");
        queueAccept("DP-NAME", "Test Dept");
        queueAccept("DP-PARENT", "0100");
        estsQueue.add("00");
        estsQueue.add("00");

        service.execute();

        assertMessageDisplayed("Parent cannot be the department itself");
        verify(deptfSpy, never()).write();
    }

    @Test
    void execute_validateParentNotFound_showsErrorAndDoesNotSave() {
        queueAccept("SC-KEY", "0100", "0000");
        queueAccept("DP-NAME", "Test Dept");
        queueAccept("DP-PARENT", "0009");
        estsQueue.add("00");
        estsQueue.add("00");

        service.execute();

        assertMessageDisplayed("Parent department not found");
        verify(deptfSpy, never()).write();
    }

    @Test
    void execute_writeInvalidKey_showsWriteFailedMessage() {
        writeInvalid.set(true);
        queueAccept("SC-KEY", "0300", "0000");
        queueAccept("DP-NAME", "Dup Dept");
        queueAccept("DP-PARENT", "0000");
        estsQueue.add("00");
        estsQueue.add("00");

        service.execute();

        verify(deptfSpy, times(1)).write();
        assertMessageDisplayed("Write failed - duplicate");
    }

    @Test
    void execute_cancelEdit_showsCancelledMessageAndDoesNotSave() {
        fakeDb.put("100", new DeptRecord("Existing", 0, 0));
        queueAccept("SC-KEY", "0100", "0000");
        queueAccept("DP-NAME", "Existing");
        queueAccept("DP-PARENT", "0000");
        estsQueue.add("00");
        estsQueue.add("04");

        service.execute();

        assertMessageDisplayed("Cancelled");
        verify(deptfSpy, never()).write();
        verify(deptfSpy, never()).rewrite();
    }

    @Test
    void execute_invalidFunctionKeyAtBodyScreen_showsInvalidFunctionKeyMessage() {
        queueAccept("SC-KEY", "0100", "0000");
        queueAccept("DP-NAME", "Test Dept");
        queueAccept("DP-PARENT", "0000");
        estsQueue.add("00");
        estsQueue.add("07");

        service.execute();

        assertMessageDisplayed("Invalid function key");
    }

    // ---------------------------------------------------------------
    // Change flow (SCHG-010 / LOOK-010)
    // ---------------------------------------------------------------

    @Test
    void execute_changeExistingRecord_displaysParentNameAndRewrites() {
        fakeDb.put("5", new DeptRecord("Parent", 0, 0));
        fakeDb.put("100", new DeptRecord("Existing Dept", 5, 0));
        queueAccept("SC-KEY", "0100", "0000");
        queueAccept("DP-NAME", "Updated Dept");
        queueAccept("DP-PARENT", "0005");
        estsQueue.add("00");
        estsQueue.add("00");

        service.execute();

        assertThat(parentNameAtBodyDisplay).contains("Parent");
        verify(deptfSpy, times(1)).rewrite();
        verify(deptfSpy, never()).write();
        assertMessageDisplayed("Department updated");
    }

    @Test
    void execute_changeExistingRecordUnknownParent_marksUnknownParent() {
        fakeDb.put("100", new DeptRecord("Existing Dept", 9, 0));
        queueAccept("SC-KEY", "0100", "0000");
        queueAccept("DP-NAME", "Existing Dept");
        queueAccept("DP-PARENT", "0009");
        estsQueue.add("00");
        estsQueue.add("00");

        service.execute();

        assertThat(parentNameAtBodyDisplay).contains("??? unknown parent");
    }

    @Test
    void execute_deletedRecordReRegister_setsModeAddAndWrites() {
        fakeDb.put("100", new DeptRecord("Old Dept", 0, 1));
        queueAccept("SC-KEY", "0100", "0000");
        queueAccept("DP-NAME", "New Dept");
        queueAccept("DP-PARENT", "0000");
        estsQueue.add("00");
        estsQueue.add("00");

        service.execute();

        verify(deptfSpy, times(1)).write();
        verify(deptfSpy, never()).rewrite();
        assertMessageDisplayed("Department added");
    }

    @Test
    void execute_rewriteInvalidKey_showsUpdateFailedMessage() {
        fakeDb.put("100", new DeptRecord("Existing", 0, 0));
        writeInvalid.set(true);
        queueAccept("SC-KEY", "0100", "0000");
        queueAccept("DP-NAME", "Existing");
        queueAccept("DP-PARENT", "0000");
        estsQueue.add("00");
        estsQueue.add("00");

        service.execute();

        verify(deptfSpy, times(1)).rewrite();
        assertMessageDisplayed("Update failed");
    }

    // ---------------------------------------------------------------
    // Delete flow (DEL-010)
    // ---------------------------------------------------------------

    @Test
    void execute_deleteWithConfirmYes_marksDeletedAndRewrites() {
        fakeDb.put("100", new DeptRecord("ToDelete", 0, 0));
        queueAccept("SC-KEY", "0100", "0000");
        queueAccept("DP-NAME", "ToDelete");
        queueAccept("DP-PARENT", "0000");
        queueAccept("SC-CONF", "Y");
        estsQueue.add("00"); // key accept
        estsQueue.add("09"); // body accept -> delete
        estsQueue.add("00"); // confirm broadcast

        service.execute();

        verify(deptfSpy, times(1)).rewrite();
        assertThat(delFlagAtRewrite).containsExactly(1);
        assertMessageDisplayed("Department deleted");
    }

    @Test
    void execute_deleteWithConfirmNo_cancelsDelete() {
        fakeDb.put("100", new DeptRecord("ToDelete", 0, 0));
        queueAccept("SC-KEY", "0100", "0000");
        queueAccept("DP-NAME", "ToDelete");
        queueAccept("DP-PARENT", "0000");
        queueAccept("SC-CONF", "N");
        estsQueue.add("00");
        estsQueue.add("09");
        estsQueue.add("00");

        service.execute();

        verify(deptfSpy, never()).rewrite();
        assertMessageDisplayed("Delete cancelled");
    }

    @Test
    void execute_deleteRewriteInvalidKey_showsDeleteFailedMessage() {
        fakeDb.put("100", new DeptRecord("ToDelete", 0, 0));
        writeInvalid.set(true);
        queueAccept("SC-KEY", "0100", "0000");
        queueAccept("DP-NAME", "ToDelete");
        queueAccept("DP-PARENT", "0000");
        queueAccept("SC-CONF", "Y");
        estsQueue.add("00");
        estsQueue.add("09");
        estsQueue.add("00");

        service.execute();

        verify(deptfSpy, times(1)).rewrite();
        assertMessageDisplayed("Delete failed");
    }

    @Test
    void execute_deleteWhenModeAdd_showsNothingToDeleteAndNeverPromptsConfirm() {
        // New (not-found) record -> MODE-ADD -> PF9 delete key is a no-op per COBOL EDIT-010.
        queueAccept("SC-KEY", "0400", "0000");
        queueAccept("DP-NAME", "Draft");
        queueAccept("DP-PARENT", "0000");
        estsQueue.add("00");
        estsQueue.add("09");

        service.execute();

        assertMessageDisplayed("Nothing to delete");
        verify(renderer, never()).acceptField(argThat(f -> f.name.equals("SC-CONF")));
        verify(deptfSpy, never()).rewrite();
    }
}
