package com.sakura.ms0120.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.ms0120.domain.Ms0120FieldAccess;
import com.sakura.ms0120.domain.WorkingStorage;
import com.sakura.ms0120.runtime.Ms0120Datasets;
import com.sakura.ms0120.screen.ScreenDefs;
import com.sakura.runtime.BatchServiceBase;
import com.sakura.runtime.DatasetEnums.*;
import com.sakura.runtime.ProgramExitSignal;
import com.sakura.runtime.ScreenModels.*;
import com.sakura.runtime.ScreenRendererAware;
import com.sakura.runtime.ScreenRendererInstance;
import com.sakura.runtime.Utility;
import com.sakura.runtime.io.AbstractDatasets;
import com.sakura.runtime.linkage.AbortxLinkParm;
import com.sakura.runtime.linkage.DateutLinkParm;
import com.sakura.runtime.record.RawDatasetBase;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/** Business logic service generated from COBOL program MS0120. */
@Service
@Scope("prototype")
public class Ms0120Service extends BatchServiceBase {
    /** Upper bound of a valid staff code; user codes above this cannot map to a STAFF record. */
    private static final int MAX_STAFF_CODE = 9999;

    /** Shared file instances for all FD files in this program. */
    private final Ms0120Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Ms0120FieldAccess ws;

    public Ms0120Service(
            Ms0120Datasets fileSet,
            DateutService dateutService,
            AbortxService abortxService,
            ScreenRendererInstance renderer) {
        this.fileSet = fileSet;
        this.ws = new Ms0120FieldAccess(new WorkingStorage(), fileSet);
        this.dateutService = dateutService;
        this.abortxService = abortxService;
        this.renderer = renderer;
    }

    @Override
    public void setRenderer(ScreenRendererInstance renderer) {
        super.setRenderer(renderer);
        if (dateutService instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (abortxService instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
    }

    /**
     * Program entry point — runs the first COBOL paragraph via runChain. Base class run() wraps
     * this in StopRun/Abort/Exception handling.
     */
    @Override
    protected void mainProcess() {
        // Import anchors for types referenced by emitted paragraph bodies:
        // StopRunSignal, JobAbortException, ParagraphJumpSignal, ProgramExitSignal
        runChain(this::runMainProgram);
    }

    /**
     * Returns COBOL COMPLETION-CODE after run() completes. Used by Tasklet to propagate exit code
     * into StepExecutionContext.
     */
    @Override
    public int getCompletionCode() {
        return ws.getCompletionCode();
    }

    /**
     * Set COBOL COMPLETION-CODE. Called by base class run() on Abort (255) / Exception (12) paths.
     */
    @Override
    protected void setCompletionCode(int code) {
        ws.setCompletionCode(code);
    }

    /**
     * Returns this program's FileSet (or null if no files declared). Base class run() uses this for
     * commit/rollback/closeAll lifecycle.
     */
    @Override
    protected AbstractDatasets getFileSet() {
        return fileSet;
    }

    /** COBOL paragraph: MAIN-000 */
    private void runMainProgram() {
        runChain(this::initializeProgram);
        while ((ws.getEndFlg() != 1)) {
            runChain(this::runMainScreenCycle);
        }
        runChain(this::closeAllFiles);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeProgram() {
        ws.setWkProgid("MS0120");
        ws.setWkTitle("User Master Maintenance");
        ws.setWkFkeyLine("ENTER=Read  PF9=Delete  PF3=End");
        ws.setKdFunc("TODY");
        dateut(ws.getKdate());
        ws.setWkSysymd(ws.getKdDate1());
        ws.setWkSysdate(ws.getKdDate1());
        fileSet.getUserf().open(FileOpenMode.IO);
        ws.trySetString("FSTS", fileSet.getUserf().getFileStatus());
        if (Utility.fieldEquals(ws.getFsts(), "35") || Utility.fieldEquals(ws.getFsts(), "30")) {
            fileSet.getUserf().open(FileOpenMode.OUTPUT);
            ws.trySetString("FSTS", fileSet.getUserf().getFileStatus());
            fileSet.getUserf().close();
            ws.trySetString("FSTS", fileSet.getUserf().getFileStatus());
            fileSet.getUserf().open(FileOpenMode.IO);
            ws.trySetString("FSTS", fileSet.getUserf().getFileStatus());
        }
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("USERF");
            runChain(this::abortOnFileError);
        }
        fileSet.getStaff().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getStaff().getFileStatus());
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("STAFF");
            runChain(this::abortOnFileError);
        }
    }

    /** COBOL paragraph: MAIN-RTN-010 */
    private void runMainScreenCycle() {
        runChain(this::clearUserWorkFields);
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEADER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-FOOTER"), ws);
        ws.setWkMsgLine(" ");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-KEY"), ws);
        String scValUsCode0 =
                Utility.acceptScreen(
                        "SC-KEY", () -> renderer.acceptField(ScreenDefs.getInput("SC-KEY")));
        ws.setUsCode(Utility.parseIntOr(scValUsCode0.trim(), 0));
        broadcastEstsStatus();
        switch (String.valueOf(ws.getEsts())) {
            case "03" -> {
                ws.setString("END-FLG", "1");
            }
            case "00" -> {
                runChain(this::resolveUserByCode);
            }
            default -> {
                ws.setWkMsgLine("Invalid function key");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: CLEAR-010 */
    private void clearUserWorkFields() {
        fileSet.getUserf().setRecord();
        ws.setWkStaffName(" ");
        ws.setWkConfirm(" ");
        ws.setWkSaveCode(0);
    }

    /** COBOL paragraph: PKEY-010 */
    private void resolveUserByCode() {
        if (ws.getUsCode() == 0) {
            ws.setWkMsgLine("User code must not be zero");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setWkSaveCode(ws.getUsCode());
        readRecordByKey(fileSet.getUserf(), "US-CODE");
        if (fileSet.getUserf().isInvalidKey()) {
            runChain(this::prepareNewUserAdd);
        }
        if (!fileSet.getUserf().isInvalidKey()) {
            runChain(this::prepareUserChange);
        }
        runChain(this::acceptAndProcessUserFields);
    }

    /** COBOL paragraph: SADD-010 */
    private void prepareNewUserAdd() {
        ws.setString("MODE-FLG", "1");
        fileSet.getUserf().setRecord();
        ws.setUsCode(ws.getWkSaveCode());
        ws.setUsRole(3);
        ws.setWkMsgLine("New user - enter details");
    }

    /** COBOL paragraph: SCHG-010 */
    private void prepareUserChange() {
        if (ws.getUsDelFlag() == 1) {
            ws.setString("MODE-FLG", "1");
            ws.setWkMsgLine("Deleted user - re-registering");
        } else {
            ws.setString("MODE-FLG", "2");
            ws.setWkMsgLine("Existing user - change or PF9 delete");
        }
        runChain(this::lookupStaffName);
    }

    /** COBOL paragraph: EDIT-010 */
    private void acceptAndProcessUserFields() {
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-BODY"), ws);
        String scValUsLogin2 =
                Utility.acceptScreen(
                        "US-LOGIN", () -> renderer.acceptField(ScreenDefs.getInput("US-LOGIN")));
        ws.setUsLogin(scValUsLogin2);
        String scValUsPassword3 =
                Utility.acceptScreen(
                        "US-PASSWORD",
                        () -> renderer.acceptField(ScreenDefs.getInput("US-PASSWORD")));
        ws.setUsPassword(scValUsPassword3);
        String scValUsName4 =
                Utility.acceptScreen(
                        "US-NAME", () -> renderer.acceptField(ScreenDefs.getInput("US-NAME")));
        ws.setUsName(scValUsName4);
        String scValUsRole5 =
                Utility.acceptScreen(
                        "US-ROLE", () -> renderer.acceptField(ScreenDefs.getInput("US-ROLE")));
        ws.setUsRole(Utility.parseIntOr(scValUsRole5.trim(), 0));
        String scValUsAuthMaster6 =
                Utility.acceptScreen(
                        "US-AUTH-MASTER",
                        () -> renderer.acceptField(ScreenDefs.getInput("US-AUTH-MASTER")));
        ws.setUsAuthMaster(Utility.parseIntOr(scValUsAuthMaster6.trim(), 0));
        String scValUsAuthOrder7 =
                Utility.acceptScreen(
                        "US-AUTH-ORDER",
                        () -> renderer.acceptField(ScreenDefs.getInput("US-AUTH-ORDER")));
        ws.setUsAuthOrder(Utility.parseIntOr(scValUsAuthOrder7.trim(), 0));
        String scValUsAuthSales8 =
                Utility.acceptScreen(
                        "US-AUTH-SALES",
                        () -> renderer.acceptField(ScreenDefs.getInput("US-AUTH-SALES")));
        ws.setUsAuthSales(Utility.parseIntOr(scValUsAuthSales8.trim(), 0));
        String scValUsAuthPurch9 =
                Utility.acceptScreen(
                        "US-AUTH-PURCH",
                        () -> renderer.acceptField(ScreenDefs.getInput("US-AUTH-PURCH")));
        ws.setUsAuthPurch(Utility.parseIntOr(scValUsAuthPurch9.trim(), 0));
        String scValUsAuthClose10 =
                Utility.acceptScreen(
                        "US-AUTH-CLOSE",
                        () -> renderer.acceptField(ScreenDefs.getInput("US-AUTH-CLOSE")));
        ws.setUsAuthClose(Utility.parseIntOr(scValUsAuthClose10.trim(), 0));
        broadcastEstsStatus();
        switch (String.valueOf(ws.getEsts())) {
            case "03" -> {
                /* CONTINUE */
            }
            case "04" -> {
                ws.setWkMsgLine("Cancelled");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
            case "09" -> {
                if ((ws.getModeFlg() == 2)) {
                    runChain(this::deleteUserWithConfirmation);
                } else {
                    ws.setWkMsgLine("Nothing to delete");
                    renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
                }
            }
            case "00" -> {
                runChain(this::validateUserInput);
                if ((ws.getErrFlg() != 1)) {
                    runChain(this::saveUserRecord);
                }
            }
            default -> {
                ws.setWkMsgLine("Invalid function key");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: VAL-010 */
    private void validateUserInput() {
        ws.setErrFlg(0);
        if (Utility.fieldEquals(ws.getUsLogin(), " ")) {
            ws.setWkMsgLine("Login ID is required");
            ws.setString("ERR-FLG", "1");
            reportValidationErrors();
            return;
        }
        if (Utility.fieldEquals(ws.getUsName(), " ")) {
            ws.setWkMsgLine("User name is required");
            ws.setString("ERR-FLG", "1");
            reportValidationErrors();
            return;
        }
        if (ws.getUsRole() < 1 || ws.getUsRole() > 3) {
            ws.setWkMsgLine("Role must be 1 admin 2 manager 3 clerk");
            ws.setString("ERR-FLG", "1");
            reportValidationErrors();
            return;
        }
        runChain(this::validateAuthorizationFlags);
        if ((ws.getErrFlg() == 1)) {
            reportValidationErrors();
            return;
        }
        runChain(this::validateStaffLinkage);
        // fall-through to next paragraph
        reportValidationErrors();
    }

    /** COBOL paragraph: VAL-999 */
    private void reportValidationErrors() {
        if ((ws.getErrFlg() == 1)) {
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
        /* CONTINUE */
    }

    /** COBOL paragraph: VAUTH-010 */
    private void validateAuthorizationFlags() {
        if (ws.getUsAuthMaster() > 1) {
            ws.setWkMsgLine("Auth Master must be 0 or 1");
            ws.setString("ERR-FLG", "1");
            return;
        }
        if (ws.getUsAuthOrder() > 1) {
            ws.setWkMsgLine("Auth Order must be 0 or 1");
            ws.setString("ERR-FLG", "1");
            return;
        }
        if (ws.getUsAuthSales() > 1) {
            ws.setWkMsgLine("Auth Sales must be 0 or 1");
            ws.setString("ERR-FLG", "1");
            return;
        }
        if (ws.getUsAuthPurch() > 1) {
            ws.setWkMsgLine("Auth Purchase must be 0 or 1");
            ws.setString("ERR-FLG", "1");
            return;
        }
        if (ws.getUsAuthClose() > 1) {
            ws.setWkMsgLine("Auth Close must be 0 or 1");
            ws.setString("ERR-FLG", "1");
        }
    }

    /** COBOL paragraph: VSTAF-010 */
    private void validateStaffLinkage() {
        if (ws.getUsRole() == 1) {
            return;
        }
        if (ws.getUsCode() > MAX_STAFF_CODE) {
            ws.setWkMsgLine("Non-admin user code must match a staff code");
            ws.setString("ERR-FLG", "1");
            return;
        }
        ws.setSfCode(ws.getUsCode());
        readRecordByKey(fileSet.getStaff(), "SF-CODE");
        if (fileSet.getStaff().isInvalidKey()) {
            ws.setWkMsgLine("Staff code not found");
            ws.setString("ERR-FLG", "1");
        }
        if (!fileSet.getStaff().isInvalidKey()) {
            ws.setWkStaffName(ws.getSfName());
        }
    }

    /** COBOL paragraph: SAVE-010 */
    private void saveUserRecord() {
        ws.setUsDelFlag(0);
        if ((ws.getModeFlg() == 1)) {
            fileSet.getUserf().write();
            ws.trySetString("FSTS", fileSet.getUserf().getFileStatus());
            if (fileSet.getUserf().isInvalidKey()) {
                ws.setWkMsgLine("Write failed - duplicate code or login");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
            if (!fileSet.getUserf().isInvalidKey()) {
                ws.setWkMsgLine("User added");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        } else {
            fileSet.getUserf().rewrite();
            ws.trySetString("FSTS", fileSet.getUserf().getFileStatus());
            if (fileSet.getUserf().isInvalidKey()) {
                ws.setWkMsgLine("Update failed");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
            if (!fileSet.getUserf().isInvalidKey()) {
                ws.setWkMsgLine("User updated");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: DEL-010 */
    private void deleteUserWithConfirmation() {
        ws.setWkConfirm(" ");
        ws.setWkMsgLine("Press Y then ENTER to delete");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-CONFIRM"), ws);
        String scValWkConfirm12 =
                Utility.acceptScreen(
                        "SC-CONF", () -> renderer.acceptField(ScreenDefs.getInput("SC-CONF")));
        ws.setWkConfirm(scValWkConfirm12);
        broadcastEstsStatus();
        if ((ws.getWkConfirm().equals("Y") || ws.getWkConfirm().equals("y"))) {
            ws.setUsDelFlag(1);
            fileSet.getUserf().rewrite();
            ws.trySetString("FSTS", fileSet.getUserf().getFileStatus());
            if (fileSet.getUserf().isInvalidKey()) {
                ws.setWkMsgLine("Delete failed");
            }
            if (!fileSet.getUserf().isInvalidKey()) {
                ws.setWkMsgLine("User deleted");
            }
        } else {
            ws.setWkMsgLine("Delete cancelled");
        }
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
    }

    /** COBOL paragraph: LOOK-010 */
    private void lookupStaffName() {
        ws.setWkStaffName(" ");
        if (ws.getUsRole() == 1) {
            ws.setWkStaffName("(administrator account)");
            return;
        }
        if (ws.getUsCode() > MAX_STAFF_CODE) {
            ws.setWkStaffName("(non-staff user code)");
            return;
        }
        ws.setSfCode(ws.getUsCode());
        readRecordByKey(fileSet.getStaff(), "SF-CODE");
        if (fileSet.getStaff().isInvalidKey()) {
            ws.setWkStaffName("??? unknown staff");
        }
        if (!fileSet.getStaff().isInvalidKey()) {
            ws.setWkStaffName(ws.getSfName());
        }
    }

    /** COBOL paragraph: TERM-010 */
    private void closeAllFiles() {
        fileSet.getUserf().close();
        ws.trySetString("FSTS", fileSet.getUserf().getFileStatus());
        fileSet.getStaff().close();
        ws.trySetString("FSTS", fileSet.getStaff().getFileStatus());
    }

    /** COBOL paragraph: ABEND-010 */
    private void abortOnFileError() {
        ws.setKaProgid("MS0120");
        ws.setKaFsts(ws.getFsts());
        ws.setKaMsgcode("EOPEN ");
        ws.setKaDetail("File open error");
        abortx(ws.getKabend());
        ws.setCompletionCode(255);
        throw new ProgramExitSignal();
    }

    /** COBOL CALL DATEUT — delegates to injected DateutService. */
    private void dateut(Object... args) {
        DateutLinkParm params = new DateutLinkParm();
        params.getKdate().setKdFunc(ws.getKdFunc());
        params.getKdate().setKdDate1(ws.getKdDate1());
        params.getKdate().setKdDate2(ws.getKdDate2());
        params.getKdate().setKdDays(ws.getKdDays());
        params.getKdate().setKdWeekday(ws.getKdWeekday());
        params.getKdate().setKdStatus(ws.getKdStatus());
        dateutService.execute(params);
        ws.setKdFunc(params.getKdate().getKdFunc());
        ws.setKdDate1(params.getKdate().getKdDate1());
        ws.setKdDate2(params.getKdate().getKdDate2());
        ws.setKdDays(params.getKdate().getKdDays());
        ws.setKdWeekday(params.getKdate().getKdWeekday());
        ws.setKdStatus(params.getKdate().getKdStatus());
    }

    /** COBOL CALL ABORTX — delegates to injected AbortxService. */
    private void abortx(Object... args) {
        AbortxLinkParm params = new AbortxLinkParm();
        params.getKabend().setKaProgid(ws.getKaProgid());
        params.getKabend().setKaFile(ws.getKaFile());
        params.getKabend().setKaFsts(ws.getKaFsts());
        params.getKabend().setKaMsgcode(ws.getKaMsgcode());
        params.getKabend().setKaDetail(ws.getKaDetail());
        abortxService.execute(params);
        ws.setKaProgid(params.getKabend().getKaProgid());
        ws.setKaFile(params.getKabend().getKaFile());
        ws.setKaFsts(params.getKabend().getKaFsts());
        ws.setKaMsgcode(params.getKabend().getKaMsgcode());
        ws.setKaDetail(params.getKabend().getKaDetail());
    }

    /** Reads CRT STATUS and broadcasts to all ESTS fields — COBOL implicit after each ACCEPT. */
    private void broadcastEstsStatus() {
        Utility.broadcastEndStatus(ws, renderer.readEndStatus(), "ESTS");
    }

    /**
     * Resolve a record by its key field, falling back to the current record's key when the field is
     * blank.
     */
    private void readRecordByKey(RawDatasetBase file, String keyFieldName) {
        String rkVal = "";
        if (rkVal == null || rkVal.trim().isEmpty()) {
            try {
                rkVal = ws.getString(keyFieldName);
            } catch (Exception _e) {
            }
        }
        if (rkVal == null || rkVal.trim().isEmpty()) {
            try {
                rkVal = file.extractKeyFromCurrentRecord();
            } catch (Exception _e3) {
            }
        }
        file.readByKey(rkVal != null ? rkVal.trim() : "");
        ws.trySetString("FSTS", file.getFileStatus());
    }
}
