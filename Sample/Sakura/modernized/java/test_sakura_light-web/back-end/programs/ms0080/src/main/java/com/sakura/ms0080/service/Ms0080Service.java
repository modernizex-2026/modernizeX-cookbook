package com.sakura.ms0080.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.ms0080.domain.Ms0080FieldAccess;
import com.sakura.ms0080.domain.WorkingStorage;
import com.sakura.ms0080.runtime.Ms0080Datasets;
import com.sakura.ms0080.screen.ScreenDefs;
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

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/** Business logic service generated from COBOL program MS0080. */
@Service
@Scope("prototype")
public class Ms0080Service extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final Ms0080Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Ms0080FieldAccess ws;

    public Ms0080Service(
            Ms0080Datasets fileSet,
            DateutService dateutService,
            AbortxService abortxService,
            ScreenRendererInstance renderer) {
        this.fileSet = fileSet;
        this.ws = new Ms0080FieldAccess(new WorkingStorage(), fileSet);
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
            runChain(this::processMainScreen);
        }
        runChain(this::closeFilesAndTerminate);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeProgram() {
        ws.setWkProgid("MS0080");
        ws.setWkTitle("Department Master Maintenance");
        ws.setWkFkeyLine("ENTER=Read  PF9=Delete  PF3=End");
        ws.setKdFunc("TODY");
        dateut(ws.getKdate());
        ws.setWkSysymd(ws.getKdDate1());
        ws.setWkSysdate(ws.getKdDate1());
        fileSet.getDeptf().open(FileOpenMode.IO);
        ws.trySetString("FSTS", fileSet.getDeptf().getFileStatus());
        if (Utility.fieldEquals(ws.getFsts(), "35") || Utility.fieldEquals(ws.getFsts(), "30")) {
            fileSet.getDeptf().open(FileOpenMode.OUTPUT);
            ws.trySetString("FSTS", fileSet.getDeptf().getFileStatus());
            fileSet.getDeptf().close();
            ws.trySetString("FSTS", fileSet.getDeptf().getFileStatus());
            fileSet.getDeptf().open(FileOpenMode.IO);
            ws.trySetString("FSTS", fileSet.getDeptf().getFileStatus());
        }
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("DEPTF");
            runChain(this::abortOnFileError);
        }
    }

    /** COBOL paragraph: MAIN-RTN-010 */
    private void processMainScreen() {
        runChain(this::clearWorkingRecord);
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEADER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-FOOTER"), ws);
        ws.setWkMsgLine(" ");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-KEY"), ws);
        String scValDpCode0 =
                Utility.acceptScreen(
                        "SC-KEY", () -> renderer.acceptField(ScreenDefs.getInput("SC-KEY")));
        ws.setDpCode(Utility.parseIntOr(scValDpCode0.trim(), 0));
        broadcastEstsStatus();
        switch (String.valueOf(ws.getEsts())) {
            case "03" -> {
                ws.setString("END-FLG", "1");
            }
            case "00" -> {
                runChain(this::processDeptCodeKey);
            }
            default -> {
                ws.setWkMsgLine("Invalid function key");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: CLEAR-010 */
    private void clearWorkingRecord() {
        fileSet.getDeptf().setRecord();
        ws.setWkParentName(" ");
        ws.setWkConfirm(" ");
        ws.setWkSaveCode(0);
    }

    /** COBOL paragraph: PKEY-010 */
    private void processDeptCodeKey() {
        if (ws.getDpCode() == 0) {
            ws.setWkMsgLine("Department code must not be zero");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setWkSaveCode(ws.getDpCode());
        readDeptfByCode();
        if (fileSet.getDeptf().isInvalidKey()) {
            runChain(this::prepareNewDeptRecord);
        }
        if (!fileSet.getDeptf().isInvalidKey()) {
            runChain(this::prepareExistingDeptRecord);
        }
        runChain(this::acceptDeptDetailFields);
    }

    /** COBOL paragraph: SADD-010 */
    private void prepareNewDeptRecord() {
        ws.setString("MODE-FLG", "1");
        fileSet.getDeptf().setRecord();
        ws.setDpCode(ws.getWkSaveCode());
        ws.setWkMsgLine("New department - enter details");
    }

    /** COBOL paragraph: SCHG-010 */
    private void prepareExistingDeptRecord() {
        if (ws.getDpDelFlag() == 1) {
            ws.setString("MODE-FLG", "1");
            ws.setWkMsgLine("Deleted department - re-registering");
        } else {
            ws.setString("MODE-FLG", "2");
            ws.setWkMsgLine("Existing department - change or PF9 delete");
        }
        runChain(this::lookupParentDeptName);
    }

    /** COBOL paragraph: EDIT-010 */
    private void acceptDeptDetailFields() {
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-BODY"), ws);
        String scValDpName2 =
                Utility.acceptScreen(
                        "DP-NAME", () -> renderer.acceptField(ScreenDefs.getInput("DP-NAME")));
        ws.setDpName(scValDpName2);
        String scValDpParent3 =
                Utility.acceptScreen(
                        "DP-PARENT", () -> renderer.acceptField(ScreenDefs.getInput("DP-PARENT")));
        ws.setDpParent(Utility.parseIntOr(scValDpParent3.trim(), 0));
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
                    runChain(this::deleteDeptRecordWithConfirm);
                } else {
                    ws.setWkMsgLine("Nothing to delete");
                    renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
                }
            }
            case "00" -> {
                runChain(this::validateDeptFields);
                if ((ws.getErrFlg() != 1)) {
                    runChain(this::saveDeptRecord);
                }
            }
            default -> {
                ws.setWkMsgLine("Invalid function key");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: VAL-010 */
    private void validateDeptFields() {
        ws.setErrFlg(0);
        if (Utility.fieldEquals(ws.getDpName(), " ")) {
            ws.setWkMsgLine("Name is required");
            ws.setString("ERR-FLG", "1");
            showValidationErrorMessage();
            return;
        }
        if (ws.getDpParent() != 0) {
            if (ws.getDpParent() == ws.getDpCode()) {
                ws.setWkMsgLine("Parent cannot be the department itself");
                ws.setString("ERR-FLG", "1");
                showValidationErrorMessage();
                return;
            }
            ws.copyBytes("WK-DEPT-SAVE", "DP-REC");
            ws.setDpCode(ws.getDpParent());
            readDeptfByCode();
            if (fileSet.getDeptf().isInvalidKey()) {
                ws.copyBytes("DP-REC", "WK-DEPT-SAVE");
                ws.setWkMsgLine("Parent department not found");
                ws.setString("ERR-FLG", "1");
                showValidationErrorMessage();
                return;
            }
            if (!fileSet.getDeptf().isInvalidKey()) {
                ws.copyBytes("DP-REC", "WK-DEPT-SAVE");
            }
        }
        // fall-through to next paragraph
        showValidationErrorMessage();
    }

    /** COBOL paragraph: VAL-999 */
    private void showValidationErrorMessage() {
        if ((ws.getErrFlg() == 1)) {
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
        /* CONTINUE */
    }

    /** COBOL paragraph: SAVE-010 */
    private void saveDeptRecord() {
        ws.setDpDelFlag(0);
        if ((ws.getModeFlg() == 1)) {
            fileSet.getDeptf().write();
            ws.trySetString("FSTS", fileSet.getDeptf().getFileStatus());
            if (fileSet.getDeptf().isInvalidKey()) {
                ws.setWkMsgLine("Write failed - duplicate");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
            if (!fileSet.getDeptf().isInvalidKey()) {
                ws.setWkMsgLine("Department added");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        } else {
            fileSet.getDeptf().rewrite();
            ws.trySetString("FSTS", fileSet.getDeptf().getFileStatus());
            if (fileSet.getDeptf().isInvalidKey()) {
                ws.setWkMsgLine("Update failed");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
            if (!fileSet.getDeptf().isInvalidKey()) {
                ws.setWkMsgLine("Department updated");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: DEL-010 */
    private void deleteDeptRecordWithConfirm() {
        ws.setWkConfirm(" ");
        ws.setWkMsgLine("Press Y then ENTER to delete");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-CONFIRM"), ws);
        String scValWkConfirm5 =
                Utility.acceptScreen(
                        "SC-CONF", () -> renderer.acceptField(ScreenDefs.getInput("SC-CONF")));
        ws.setWkConfirm(scValWkConfirm5);
        broadcastEstsStatus();
        if ((ws.getWkConfirm().equals("Y") || ws.getWkConfirm().equals("y"))) {
            ws.setDpDelFlag(1);
            fileSet.getDeptf().rewrite();
            ws.trySetString("FSTS", fileSet.getDeptf().getFileStatus());
            if (fileSet.getDeptf().isInvalidKey()) {
                ws.setWkMsgLine("Delete failed");
            }
            if (!fileSet.getDeptf().isInvalidKey()) {
                ws.setWkMsgLine("Department deleted");
            }
        } else {
            ws.setWkMsgLine("Delete cancelled");
        }
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
    }

    /** COBOL paragraph: LOOK-010 */
    private void lookupParentDeptName() {
        ws.setWkParentName(" ");
        if (ws.getDpParent() != 0) {
            ws.copyBytes("WK-DEPT-SAVE", "DP-REC");
            ws.setDpCode(ws.getDpParent());
            readDeptfByCode();
            if (fileSet.getDeptf().isInvalidKey()) {
                ws.setWkParentName("??? unknown parent");
            }
            if (!fileSet.getDeptf().isInvalidKey()) {
                ws.setWkParentName(ws.getDpName());
            }
            ws.copyBytes("DP-REC", "WK-DEPT-SAVE");
        }
    }

    /** COBOL paragraph: TERM-010 */
    private void closeFilesAndTerminate() {
        fileSet.getDeptf().close();
        ws.trySetString("FSTS", fileSet.getDeptf().getFileStatus());
    }

    /** COBOL paragraph: ABEND-010 */
    private void abortOnFileError() {
        ws.setKaProgid("MS0080");
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

    /** Reads DEPTF by DP-CODE, falling back to the current record's key if unset. */
    private void readDeptfByCode() {
        String rkVal = "";
        if (rkVal == null || rkVal.trim().isEmpty()) {
            try {
                rkVal = ws.getString("DP-CODE");
            } catch (Exception _e) {
            }
        }
        if (rkVal == null || rkVal.trim().isEmpty()) {
            try {
                rkVal = fileSet.getDeptf().extractKeyFromCurrentRecord();
            } catch (Exception _e3) {
            }
        }
        fileSet.getDeptf().readByKey(rkVal != null ? rkVal.trim() : "");
        ws.trySetString("FSTS", fileSet.getDeptf().getFileStatus());
    }
}
