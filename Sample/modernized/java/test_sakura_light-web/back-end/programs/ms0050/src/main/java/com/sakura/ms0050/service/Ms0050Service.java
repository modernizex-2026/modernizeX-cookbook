package com.sakura.ms0050.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.ms0050.domain.Ms0050FieldAccess;
import com.sakura.ms0050.domain.WorkingStorage;
import com.sakura.ms0050.runtime.Ms0050Datasets;
import com.sakura.ms0050.screen.ScreenDefs;
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

/** Business logic service generated from COBOL program MS0050. */
@Service
@Scope("prototype")
public class Ms0050Service extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final Ms0050Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Ms0050FieldAccess ws;

    public Ms0050Service(
            Ms0050Datasets fileSet,
            DateutService dateutService,
            AbortxService abortxService,
            ScreenRendererInstance renderer) {
        this.fileSet = fileSet;
        this.ws = new Ms0050FieldAccess(new WorkingStorage(), fileSet);
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
            runChain(this::processMainScreenCycle);
        }
        runChain(this::closeAllFiles);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeProgram() {
        ws.setWkProgid("MS0050");
        ws.setWkTitle("Staff Master Maintenance");
        ws.setWkFkeyLine("ENTER=Read  PF9=Delete  PF3=End");
        ws.setKdFunc("TODY");
        callDateutService(ws.getKdate());
        ws.setWkSysymd(ws.getKdDate1());
        ws.setWkSysdate(ws.getKdDate1());
        fileSet.getStaff().open(FileOpenMode.IO);
        ws.trySetString("FSTS", fileSet.getStaff().getFileStatus());
        if (Utility.fieldEquals(ws.getFsts(), "35") || Utility.fieldEquals(ws.getFsts(), "30")) {
            fileSet.getStaff().open(FileOpenMode.OUTPUT);
            ws.trySetString("FSTS", fileSet.getStaff().getFileStatus());
            fileSet.getStaff().close();
            ws.trySetString("FSTS", fileSet.getStaff().getFileStatus());
            fileSet.getStaff().open(FileOpenMode.IO);
            ws.trySetString("FSTS", fileSet.getStaff().getFileStatus());
        }
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("STAFF");
            runChain(this::abortProgram);
        }
        fileSet.getDeptf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getDeptf().getFileStatus());
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("DEPTF");
            runChain(this::abortProgram);
        }
    }

    /** COBOL paragraph: MAIN-RTN-010 */
    private void processMainScreenCycle() {
        runChain(this::clearWorkFields);
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEADER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-FOOTER"), ws);
        ws.setWkMsgLine(" ");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-KEY"), ws);
        String scValSfCode0 =
                Utility.acceptScreen(
                        "SC-KEY", () -> renderer.acceptField(ScreenDefs.getInput("SC-KEY")));
        ws.setSfCode(Utility.parseIntOr(scValSfCode0.trim(), 0));
        broadcastEstsStatus();
        switch (String.valueOf(ws.getEsts())) {
            case "03" -> {
                ws.setString("END-FLG", "1");
            }
            case "00" -> {
                runChain(this::processStaffCodeKey);
            }
            default -> {
                ws.setWkMsgLine("Invalid function key");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: CLEAR-010 */
    private void clearWorkFields() {
        fileSet.getStaff().setRecord();
        ws.setWkDeptName(" ");
        ws.setWkConfirm(" ");
    }

    /** COBOL paragraph: PKEY-010 */
    private void processStaffCodeKey() {
        if (ws.getSfCode() == 0) {
            ws.setWkMsgLine("Staff code must not be zero");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setWkSaveCode(ws.getSfCode());
        readByKeyWithFallback(fileSet.getStaff(), "SF-CODE");
        if (fileSet.getStaff().isInvalidKey()) {
            runChain(this::prepareAddMode);
        }
        if (!fileSet.getStaff().isInvalidKey()) {
            runChain(this::prepareChangeMode);
        }
        runChain(this::acceptStaffDetailFields);
    }

    /** COBOL paragraph: SADD-010 */
    private void prepareAddMode() {
        ws.setString("MODE-FLG", "1");
        fileSet.getStaff().setRecord();
        ws.setSfCode(ws.getWkSaveCode());
        ws.setWkMsgLine("New staff - enter details");
    }

    /** COBOL paragraph: SCHG-010 */
    private void prepareChangeMode() {
        if (ws.getSfDelFlag() == 1) {
            ws.setString("MODE-FLG", "1");
            ws.setWkMsgLine("Deleted staff - re-registering");
        } else {
            ws.setString("MODE-FLG", "2");
            ws.setWkMsgLine("Existing staff - change or PF9 delete");
        }
        runChain(this::lookupDeptName);
    }

    /** COBOL paragraph: EDIT-010 */
    private void acceptStaffDetailFields() {
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-BODY"), ws);
        String scValSfName2 =
                Utility.acceptScreen(
                        "SF-NAME", () -> renderer.acceptField(ScreenDefs.getInput("SF-NAME")));
        ws.setSfName(scValSfName2);
        String scValSfKana3 =
                Utility.acceptScreen(
                        "SF-KANA", () -> renderer.acceptField(ScreenDefs.getInput("SF-KANA")));
        ws.setSfKana(scValSfKana3);
        String scValSfDept4 =
                Utility.acceptScreen(
                        "SF-DEPT", () -> renderer.acceptField(ScreenDefs.getInput("SF-DEPT")));
        ws.setSfDept(Utility.parseIntOr(scValSfDept4.trim(), 0));
        String scValSfTel5 =
                Utility.acceptScreen(
                        "SF-TEL", () -> renderer.acceptField(ScreenDefs.getInput("SF-TEL")));
        ws.setSfTel(scValSfTel5);
        String scValSfEmail6 =
                Utility.acceptScreen(
                        "SF-EMAIL", () -> renderer.acceptField(ScreenDefs.getInput("SF-EMAIL")));
        ws.setSfEmail(scValSfEmail6);
        String scValSfTitle7 =
                Utility.acceptScreen(
                        "SF-TITLE", () -> renderer.acceptField(ScreenDefs.getInput("SF-TITLE")));
        ws.setSfTitle(scValSfTitle7);
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
                    runChain(this::confirmAndDeleteStaff);
                } else {
                    ws.setWkMsgLine("Nothing to delete");
                    renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
                }
            }
            case "00" -> {
                runChain(this::validateStaffFields);
                if ((ws.getErrFlg() != 1)) {
                    runChain(this::saveStaffRecord);
                }
            }
            default -> {
                ws.setWkMsgLine("Invalid function key");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: VAL-010 */
    private void validateStaffFields() {
        ws.setErrFlg(0);
        if (Utility.fieldEquals(ws.getSfName(), " ")) {
            ws.setWkMsgLine("Name is required");
            ws.setString("ERR-FLG", "1");
            displayValidationError();
            return;
        }
        if (ws.getSfDept() == 0) {
            ws.setWkMsgLine("Department is required");
            ws.setString("ERR-FLG", "1");
            displayValidationError();
            return;
        }
        ws.setDpCode(ws.getSfDept());
        readByKeyWithFallback(fileSet.getDeptf(), "DP-CODE");
        if (fileSet.getDeptf().isInvalidKey()) {
            ws.setWkMsgLine("Department code not found");
            ws.setString("ERR-FLG", "1");
            displayValidationError();
            return;
        }
        if (!fileSet.getDeptf().isInvalidKey()) {
            if (ws.getDpDelFlag() == 1) {
                ws.setWkMsgLine("Department is deleted");
                ws.setString("ERR-FLG", "1");
                displayValidationError();
                return;
            }
            ws.setWkDeptName(ws.getDpName());
        }
        // fall-through to next paragraph
        displayValidationError();
    }

    /** COBOL paragraph: VAL-999 */
    private void displayValidationError() {
        if ((ws.getErrFlg() == 1)) {
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
        /* CONTINUE */
    }

    /** COBOL paragraph: SAVE-010 */
    private void saveStaffRecord() {
        ws.setSfDelFlag(0);
        if ((ws.getModeFlg() == 1)) {
            fileSet.getStaff().write();
            ws.trySetString("FSTS", fileSet.getStaff().getFileStatus());
            if (fileSet.getStaff().isInvalidKey()) {
                ws.setWkMsgLine("Write failed - duplicate");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
            if (!fileSet.getStaff().isInvalidKey()) {
                ws.setWkMsgLine("Staff added");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        } else {
            fileSet.getStaff().rewrite();
            ws.trySetString("FSTS", fileSet.getStaff().getFileStatus());
            if (fileSet.getStaff().isInvalidKey()) {
                ws.setWkMsgLine("Update failed");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
            if (!fileSet.getStaff().isInvalidKey()) {
                ws.setWkMsgLine("Staff updated");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: DEL-010 */
    private void confirmAndDeleteStaff() {
        ws.setWkConfirm(" ");
        ws.setWkMsgLine("Press Y then ENTER to delete");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-CONFIRM"), ws);
        String scValWkConfirm9 =
                Utility.acceptScreen(
                        "SC-CONF", () -> renderer.acceptField(ScreenDefs.getInput("SC-CONF")));
        ws.setWkConfirm(scValWkConfirm9);
        broadcastEstsStatus();
        if ((ws.getWkConfirm().equals("Y") || ws.getWkConfirm().equals("y"))) {
            ws.setSfDelFlag(1);
            fileSet.getStaff().rewrite();
            ws.trySetString("FSTS", fileSet.getStaff().getFileStatus());
            if (fileSet.getStaff().isInvalidKey()) {
                ws.setWkMsgLine("Delete failed");
            }
            if (!fileSet.getStaff().isInvalidKey()) {
                ws.setWkMsgLine("Staff deleted");
            }
        } else {
            ws.setWkMsgLine("Delete cancelled");
        }
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
    }

    /** COBOL paragraph: LOOK-010 */
    private void lookupDeptName() {
        ws.setWkDeptName(" ");
        if (ws.getSfDept() != 0) {
            ws.setDpCode(ws.getSfDept());
            readByKeyWithFallback(fileSet.getDeptf(), "DP-CODE");
            if (fileSet.getDeptf().isInvalidKey()) {
                ws.setWkDeptName("??? unknown department");
            }
            if (!fileSet.getDeptf().isInvalidKey()) {
                ws.setWkDeptName(ws.getDpName());
            }
        }
    }

    /** COBOL paragraph: TERM-010 */
    private void closeAllFiles() {
        fileSet.getStaff().close();
        ws.trySetString("FSTS", fileSet.getStaff().getFileStatus());
        fileSet.getDeptf().close();
        ws.trySetString("FSTS", fileSet.getDeptf().getFileStatus());
    }

    /** COBOL paragraph: ABEND-010 */
    private void abortProgram() {
        ws.setKaProgid("MS0050");
        ws.setKaFsts(ws.getFsts());
        ws.setKaMsgcode("EOPEN ");
        ws.setKaDetail("File open error");
        callAbortxService(ws.getKabend());
        ws.setCompletionCode(255);
        throw new ProgramExitSignal();
    }

    /** COBOL CALL DATEUT — delegates to injected DateutService. */
    private void callDateutService(Object... args) {
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
    private void callAbortxService(Object... args) {
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

    /** Resolve the record key from WS field or the current record, then read the file by key. */
    private void readByKeyWithFallback(RawDatasetBase file, String wsKey) {
        String rkVal = "";
        if (rkVal == null || rkVal.trim().isEmpty()) {
            try {
                rkVal = ws.getString(wsKey);
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
