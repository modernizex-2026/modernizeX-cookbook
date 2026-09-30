package com.sakura.ms0120.service;

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
import com.sakura.ms0120.domain.Ms0120FieldAccess;
import com.sakura.ms0120.runtime.Ms0120Datasets;
import com.sakura.runtime.DatasetEnums.FileOpenMode;
import com.sakura.runtime.ScreenModels.ScreenDef;
import com.sakura.runtime.ScreenRendererInstance;
import com.sakura.runtime.io.StaffDataset;
import com.sakura.runtime.io.UserfDataset;
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
 * Unit tests for Ms0120Service (COBOL MS0120 — user master maintenance), generated from {@code
 * MS0120.cob} PROCEDURE DIVISION as ground truth.
 *
 * <p>Test strategy: USERF and STAFF are real dataset objects wrapped with {@code spy()} so the
 * record buffer (and therefore {@code Ms0120FieldAccess}) works exactly as in production; only I/O
 * methods (open/close/readByKey/isInvalidKey/write/rewrite/getFileStatus) are stubbed so no real DB
 * access happens. DATEUT runs for real (TODY is pure calendar math, no branch depends on its result
 * other than copying WK-SYSYMD/WK-SYSDATE). ABORTX is mocked.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Timeout(value = 10, unit = TimeUnit.SECONDS)
class Ms0120ServiceTest {

    @Mock private ScreenRendererInstance renderer;
    @Mock private AbortxService abortxService;

    private DateutService dateutService;

    private Ms0120Datasets fileSet;
    private UserfDataset userf;
    private StaffDataset staff;

    private Ms0120Service service;

    private final Map<String, String> acceptValues = new HashMap<>();
    private final List<String> screenInteractions = new ArrayList<>();
    private final AtomicBoolean userRecordFound = new AtomicBoolean(false);
    private final AtomicBoolean userInvalidKeyFlag = new AtomicBoolean(false);
    private final AtomicBoolean staffRecordFound = new AtomicBoolean(false);

    @BeforeEach
    void setUp() {
        dateutService = new DateutService();

        Ms0120Datasets real = new Ms0120Datasets();
        fileSet = spy(real);
        userf = spy(real.getUserf());
        staff = spy(real.getStaff());
        doReturn(userf).when(fileSet).getUserf();
        doReturn(staff).when(fileSet).getStaff();

        doNothing().when(userf).open(any());
        doNothing().when(userf).close();
        doReturn("00").when(userf).getFileStatus();
        doAnswer(inv -> userInvalidKeyFlag.get()).when(userf).isInvalidKey();
        doAnswer(
                        inv -> {
                            boolean found = userRecordFound.get();
                            userInvalidKeyFlag.set(!found);
                            return found;
                        })
                .when(userf)
                .readByKey(any());
        doAnswer(
                        inv -> {
                            userInvalidKeyFlag.set(false);
                            return null;
                        })
                .when(userf)
                .write();
        doAnswer(
                        inv -> {
                            userInvalidKeyFlag.set(false);
                            return null;
                        })
                .when(userf)
                .rewrite();

        doNothing().when(staff).open(any());
        doNothing().when(staff).close();
        doReturn("00").when(staff).getFileStatus();
        doAnswer(inv -> !staffRecordFound.get()).when(staff).isInvalidKey();
        doAnswer(
                        inv -> {
                            boolean found = staffRecordFound.get();
                            if (found) {
                                capturedWs().setSfName("Staff Person");
                            }
                            return found;
                        })
                .when(staff)
                .readByKey(any());

        acceptValues.put("US-LOGIN", "jdoe");
        acceptValues.put("US-PASSWORD", "secret1234");
        acceptValues.put("US-NAME", "John Doe");
        acceptValues.put("US-ROLE", "2");
        acceptValues.put("US-AUTH-MASTER", "0");
        acceptValues.put("US-AUTH-ORDER", "1");
        acceptValues.put("US-AUTH-SALES", "0");
        acceptValues.put("US-AUTH-PURCH", "0");
        acceptValues.put("US-AUTH-CLOSE", "0");
        acceptValues.put("SC-KEY", "100");
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
                                Ms0120FieldAccess fa = (Ms0120FieldAccess) inv.getArgument(1);
                                screenInteractions.add(
                                        "displayScreen:DS-MSG:" + fa.getWkMsgLine().trim());
                            } else {
                                screenInteractions.add("displayScreen:" + def.name);
                            }
                            return null;
                        })
                .when(renderer)
                .displayScreen(any(), any());

        service = new Ms0120Service(fileSet, dateutService, abortxService, renderer);
    }

    /**
     * Live handle onto the service's WorkingStorage/FD field accessor, obtained from the first
     * displayScreen() call. It is the SAME mutable object throughout the run.
     */
    private Ms0120FieldAccess capturedWs() {
        ArgumentCaptor<RuntimeFieldAccess> captor =
                ArgumentCaptor.forClass(RuntimeFieldAccess.class);
        verify(renderer, org.mockito.Mockito.atLeastOnce()).displayScreen(any(), captor.capture());
        return (Ms0120FieldAccess) captor.getAllValues().get(0);
    }

    // ───────────────────────── happy path ─────────────────────────

    @Test
    void execute_pf3AtKeyPrompt_endsImmediatelyWithoutLookup() {
        when(renderer.readEndStatus()).thenReturn("03");

        service.execute();

        verify(userf, never()).readByKey(any());
        verify(userf, times(1)).open(FileOpenMode.IO);
        verify(userf, times(1)).close();
        verify(staff, times(1)).open(FileOpenMode.INPUT);
        verify(staff, times(1)).close();
        assertThat(service.getCompletionCode()).isEqualTo(0);
        assertThat(screenInteractions)
                .contains("displayScreen:DS-HEADER", "displayScreen:DS-FOOTER");
    }

    @Test
    void execute_newUserAddedWithValidDetails_writesRecordAndShowsAddedMessage() {
        userRecordFound.set(false);
        staffRecordFound.set(true);
        acceptValues.put("US-ROLE", "2");
        when(renderer.readEndStatus()).thenReturn("00", "00", "03");

        service.execute();

        verify(userf, times(1)).write();
        verify(userf, never()).rewrite();
        assertThat(screenInteractions).contains("displayScreen:DS-MSG:User added");
        assertThat(capturedWs().getModeFlg()).isEqualTo(1);
    }

    @Test
    void execute_existingUserUpdatedWithValidDetails_rewritesRecordAndShowsUpdatedMessage() {
        userRecordFound.set(true);
        staffRecordFound.set(true);
        acceptValues.put("US-ROLE", "2");
        when(renderer.readEndStatus()).thenReturn("00", "00", "03");

        service.execute();

        verify(userf, times(1)).rewrite();
        verify(userf, never()).write();
        assertThat(screenInteractions).contains("displayScreen:DS-MSG:User updated");
        assertThat(capturedWs().getModeFlg()).isEqualTo(2);
    }

    @Test
    void execute_deleteExistingUserConfirmedWithY_rewritesWithDeleteFlagAndShowsDeletedMessage() {
        userRecordFound.set(true);
        acceptValues.put("SC-CONF", "Y");
        // Snapshot US-DEL-FLAG at the moment of rewrite(): the loop's next iteration
        // re-runs CLEAR-010 (INITIALIZE US-REC), which would zero it again before assertion.
        final int[] delFlagAtRewrite = {-1};
        doAnswer(
                        inv -> {
                            delFlagAtRewrite[0] = capturedWs().getUsDelFlag();
                            userInvalidKeyFlag.set(false);
                            return null;
                        })
                .when(userf)
                .rewrite();
        when(renderer.readEndStatus()).thenReturn("00", "09", "03");

        service.execute();

        verify(userf, times(1)).rewrite();
        assertThat(screenInteractions).contains("displayScreen:DS-MSG:User deleted");
        assertThat(delFlagAtRewrite[0]).isEqualTo(1);
    }

    @Test
    void execute_adminRoleSkipsStaffLinkageAndSaves() {
        userRecordFound.set(false);
        acceptValues.put("US-ROLE", "1");
        when(renderer.readEndStatus()).thenReturn("00", "00", "03");

        service.execute();

        verify(staff, never()).readByKey(any());
        verify(userf, times(1)).write();
        assertThat(screenInteractions).contains("displayScreen:DS-MSG:User added");
    }

    // ───────────────────── edge cases (ground truth: PKEY-010/SCHG-010/LOOK-010/VAL-010)
    // ─────────────────────

    @Test
    void execute_userCodeZero_rejectsWithRequiredMessageWithoutReadingUserf() {
        acceptValues.put("SC-KEY", "0");
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        verify(userf, never()).readByKey(any());
        assertThat(screenInteractions).contains("displayScreen:DS-MSG:User code must not be zero");
    }

    @Test
    void execute_invalidFunctionKeyAtKeyPrompt_showsInvalidKeyMessageAndContinuesLoop() {
        when(renderer.readEndStatus()).thenReturn("99", "03");

        service.execute();

        verify(userf, never()).readByKey(any());
        assertThat(screenInteractions).contains("displayScreen:DS-MSG:Invalid function key");
    }

    @Test
    void execute_existingDeletedUser_reregistersAsAddModeWithStaffLookup() {
        userRecordFound.set(true);
        staffRecordFound.set(true);
        // ground truth SCHG-010: US-DEL-FLAG = 1 -> SET MODE-ADD, "Deleted user - re-registering"
        doAnswer(
                        inv -> {
                            boolean found = userRecordFound.get();
                            userInvalidKeyFlag.set(!found);
                            if (found) {
                                capturedWs().setUsDelFlag(1);
                            }
                            return found;
                        })
                .when(userf)
                .readByKey(any());
        acceptValues.put("US-ROLE", "2");
        when(renderer.readEndStatus()).thenReturn("00", "00", "03");

        service.execute();

        assertThat(screenInteractions)
                .contains("displayScreen:DS-MSG:Deleted user - re-registering");
        verify(userf, times(1)).write();
    }

    @Test
    void execute_editScreenCancelled_showsCancelledMessageWithoutSaving() {
        userRecordFound.set(true);
        staffRecordFound.set(true);
        when(renderer.readEndStatus()).thenReturn("00", "04", "03");

        service.execute();

        verify(userf, never()).write();
        verify(userf, never()).rewrite();
        assertThat(screenInteractions).contains("displayScreen:DS-MSG:Cancelled");
    }

    @Test
    void execute_editScreenInvalidFunctionKey_showsInvalidKeyMessage() {
        userRecordFound.set(true);
        staffRecordFound.set(true);
        when(renderer.readEndStatus()).thenReturn("00", "77", "03");

        service.execute();

        verify(userf, never()).write();
        verify(userf, never()).rewrite();
        assertThat(screenInteractions).contains("displayScreen:DS-MSG:Invalid function key");
    }

    @Test
    void execute_deleteRequestedInAddMode_showsNothingToDeleteAndDoesNotRewrite() {
        userRecordFound.set(false); // MODE-ADD (new record, never persisted)
        when(renderer.readEndStatus()).thenReturn("00", "09", "03");

        service.execute();

        verify(userf, never()).rewrite();
        assertThat(screenInteractions).contains("displayScreen:DS-MSG:Nothing to delete");
    }

    @Test
    void execute_deleteConfirmedWithLowercaseY_deletesRecord() {
        userRecordFound.set(true);
        acceptValues.put("SC-CONF", "y");
        when(renderer.readEndStatus()).thenReturn("00", "09", "03");

        service.execute();

        verify(userf, times(1)).rewrite();
        assertThat(screenInteractions).contains("displayScreen:DS-MSG:User deleted");
    }

    @Test
    void execute_deleteNotConfirmed_showsCancelledAndDoesNotRewrite() {
        userRecordFound.set(true);
        acceptValues.put("SC-CONF", "N");
        when(renderer.readEndStatus()).thenReturn("00", "09", "03");

        service.execute();

        verify(userf, never()).rewrite();
        assertThat(screenInteractions).contains("displayScreen:DS-MSG:Delete cancelled");
    }

    @Test
    void execute_deleteFails_showsDeleteFailedMessage() {
        userRecordFound.set(true);
        acceptValues.put("SC-CONF", "Y");
        doAnswer(
                        inv -> {
                            userInvalidKeyFlag.set(true);
                            return null;
                        })
                .when(userf)
                .rewrite();
        when(renderer.readEndStatus()).thenReturn("00", "09", "03");

        service.execute();

        verify(userf, times(1)).rewrite();
        assertThat(screenInteractions).contains("displayScreen:DS-MSG:Delete failed");
    }

    @Test
    void execute_loginBlank_rejectsWithRequiredMessageWithoutSaving() {
        userRecordFound.set(false);
        acceptValues.put("US-LOGIN", "");
        when(renderer.readEndStatus()).thenReturn("00", "00", "03");

        service.execute();

        verify(userf, never()).write();
        assertThat(screenInteractions).contains("displayScreen:DS-MSG:Login ID is required");
    }

    @Test
    void execute_nameBlank_rejectsWithRequiredMessageWithoutSaving() {
        userRecordFound.set(false);
        acceptValues.put("US-NAME", "");
        when(renderer.readEndStatus()).thenReturn("00", "00", "03");

        service.execute();

        verify(userf, never()).write();
        assertThat(screenInteractions).contains("displayScreen:DS-MSG:User name is required");
    }

    @Test
    void execute_roleOutOfRange_rejectsWithRoleMessageWithoutSaving() {
        userRecordFound.set(false);
        acceptValues.put("US-ROLE", "4");
        when(renderer.readEndStatus()).thenReturn("00", "00", "03");

        service.execute();

        verify(userf, never()).write();
        assertThat(screenInteractions)
                .contains("displayScreen:DS-MSG:Role must be 1 admin 2 manager 3 clerk");
    }

    @Test
    void execute_authMasterGreaterThanOne_rejectsWithAuthMasterMessage() {
        userRecordFound.set(false);
        acceptValues.put("US-AUTH-MASTER", "2");
        when(renderer.readEndStatus()).thenReturn("00", "00", "03");

        service.execute();

        verify(userf, never()).write();
        assertThat(screenInteractions).contains("displayScreen:DS-MSG:Auth Master must be 0 or 1");
    }

    @Test
    void execute_authCloseGreaterThanOne_rejectsWithAuthCloseMessage() {
        userRecordFound.set(false);
        acceptValues.put("US-AUTH-CLOSE", "9");
        when(renderer.readEndStatus()).thenReturn("00", "00", "03");

        service.execute();

        verify(userf, never()).write();
        assertThat(screenInteractions).contains("displayScreen:DS-MSG:Auth Close must be 0 or 1");
    }

    @Test
    void execute_nonAdminUserCodeAboveStaffRange_rejectsWithStaffRangeMessage() {
        userRecordFound.set(false);
        acceptValues.put("SC-KEY", "10000");
        acceptValues.put("US-ROLE", "2");
        when(renderer.readEndStatus()).thenReturn("00", "00", "03");

        service.execute();

        verify(staff, never()).readByKey(any());
        verify(userf, never()).write();
        assertThat(screenInteractions)
                .contains("displayScreen:DS-MSG:Non-admin user code must match a staff code");
    }

    @Test
    void execute_nonAdminStaffCodeNotFound_rejectsWithStaffNotFoundMessage() {
        userRecordFound.set(false);
        staffRecordFound.set(false);
        acceptValues.put("US-ROLE", "2");
        when(renderer.readEndStatus()).thenReturn("00", "00", "03");

        service.execute();

        verify(userf, never()).write();
        assertThat(screenInteractions).contains("displayScreen:DS-MSG:Staff code not found");
    }

    @Test
    void execute_writeFailsDuplicateKey_showsWriteFailedMessage() {
        userRecordFound.set(false);
        staffRecordFound.set(true);
        doAnswer(
                        inv -> {
                            userInvalidKeyFlag.set(true);
                            return null;
                        })
                .when(userf)
                .write();
        when(renderer.readEndStatus()).thenReturn("00", "00", "03");

        service.execute();

        assertThat(screenInteractions)
                .contains("displayScreen:DS-MSG:Write failed - duplicate code or login");
    }

    @Test
    void execute_rewriteFails_showsUpdateFailedMessage() {
        userRecordFound.set(true);
        staffRecordFound.set(true);
        doAnswer(
                        inv -> {
                            userInvalidKeyFlag.set(true);
                            return null;
                        })
                .when(userf)
                .rewrite();
        when(renderer.readEndStatus()).thenReturn("00", "00", "03");

        service.execute();

        assertThat(screenInteractions).contains("displayScreen:DS-MSG:Update failed");
    }

    // ───────────────────── file-open lifecycle (ground truth: INIT-RTN/ABEND-RTN)
    // ─────────────────────

    @Test
    void execute_userfOpenFailsPersistently_abortsWithCompletionCode255() {
        doReturn("99").when(userf).getFileStatus();
        when(renderer.readEndStatus()).thenReturn("03");

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        verify(abortxService, times(1)).execute(any());
    }

    @Test
    void execute_userfOpenStatus35_reopensAsOutputThenReopensAsInputOutput() {
        when(userf.getFileStatus()).thenReturn("35", "00", "00");
        when(renderer.readEndStatus()).thenReturn("03");

        service.execute();

        verify(userf, times(3)).open(any());
        verify(userf, times(1)).open(FileOpenMode.OUTPUT);
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }

    @Test
    void execute_userfOpenStatus30_reopensAsOutputThenReopensAsInputOutput() {
        when(userf.getFileStatus()).thenReturn("30", "00", "00");
        when(renderer.readEndStatus()).thenReturn("03");

        service.execute();

        verify(userf, times(3)).open(any());
        verify(userf, times(1)).open(FileOpenMode.OUTPUT);
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }

    @Test
    void execute_staffOpenFails_abortsWithCompletionCode255AfterUserfOpenOk() {
        doReturn("99").when(staff).getFileStatus();
        when(renderer.readEndStatus()).thenReturn("03");

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        verify(abortxService, times(1)).execute(any());
    }

    // ───────────────────────── error path ─────────────────────────

    @Test
    void execute_unexpectedRuntimeExceptionDuringProcessing_wrapsAndSetsCompletionCode12() {
        doReturn(true).when(userf).readByKey(any());
        doThrow(new RuntimeException("simulated DB failure")).when(userf).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "03");

        assertThrows(RuntimeException.class, () -> service.execute());

        assertThat(service.getCompletionCode()).isEqualTo(12);
    }
}
