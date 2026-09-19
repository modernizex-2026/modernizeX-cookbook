package com.sakura.ms0060.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.ms0060.domain.Ms0060FieldAccess;
import com.sakura.ms0060.domain.WorkingStorage;
import com.sakura.ms0060.runtime.Ms0060Datasets;
import com.sakura.ms0060.screen.ScreenDefs;
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

/** Business logic service generated from COBOL program MS0060. */
@Service
@Scope("prototype")
public class Ms0060Service extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final Ms0060Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Ms0060FieldAccess ws;

    public Ms0060Service(
            Ms0060Datasets fileSet,
            DateutService dateutService,
            AbortxService abortxService,
            ScreenRendererInstance renderer) {
        this.fileSet = fileSet;
        this.ws = new Ms0060FieldAccess(new WorkingStorage(), fileSet);
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
        runChain(this::closeCategoryFile);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeProgram() {
        ws.setWkProgid("MS0060");
        ws.setWkTitle("Category Master Maintenance");
        ws.setWkFkeyLine("ENTER=Read  PF9=Delete  PF3=End");
        ws.setKdFunc("TODY");
        callDateutService(ws.getKdate());
        ws.setWkSysymd(ws.getKdDate1());
        ws.setWkSysdate(ws.getKdDate1());
        fileSet.getCatgf().open(FileOpenMode.IO);
        ws.trySetString("FSTS", fileSet.getCatgf().getFileStatus());
        if (Utility.fieldEquals(ws.getFsts(), "35") || Utility.fieldEquals(ws.getFsts(), "30")) {
            fileSet.getCatgf().open(FileOpenMode.OUTPUT);
            ws.trySetString("FSTS", fileSet.getCatgf().getFileStatus());
            fileSet.getCatgf().close();
            ws.trySetString("FSTS", fileSet.getCatgf().getFileStatus());
            fileSet.getCatgf().open(FileOpenMode.IO);
            ws.trySetString("FSTS", fileSet.getCatgf().getFileStatus());
        }
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("CATGF");
            runChain(this::abortOnFileOpenError);
        }
    }

    /** COBOL paragraph: MAIN-RTN-010 */
    private void runMainScreenCycle() {
        runChain(this::clearWorkingRecord);
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEADER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-FOOTER"), ws);
        ws.setWkMsgLine(" ");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-KEY"), ws);
        String scValCtCode0 =
                Utility.acceptScreen(
                        "SC-KEY", () -> renderer.acceptField(ScreenDefs.getInput("SC-KEY")));
        ws.setCtCode(Utility.parseIntOr(scValCtCode0.trim(), 0));
        broadcastEstsStatus();
        switch (String.valueOf(ws.getEsts())) {
            case "03" -> {
                ws.setString("END-FLG", "1");
            }
            case "00" -> {
                runChain(this::processCategoryKeyEntry);
            }
            default -> {
                ws.setWkMsgLine("Invalid function key");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: CLEAR-010 */
    private void clearWorkingRecord() {
        fileSet.getCatgf().setRecord();
        ws.setWkParentName(" ");
        ws.setWkConfirm(" ");
    }

    /** COBOL paragraph: PKEY-010 */
    private void processCategoryKeyEntry() {
        if (ws.getCtCode() == 0) {
            ws.setWkMsgLine("Category code must not be zero");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setWkSaveCode(ws.getCtCode());
        readCategoryByCode();
        if (fileSet.getCatgf().isInvalidKey()) {
            runChain(this::prepareNewCategoryEntry);
        }
        if (!fileSet.getCatgf().isInvalidKey()) {
            runChain(this::prepareExistingCategoryEntry);
        }
        runChain(this::processCategoryEditScreen);
    }

    /** COBOL paragraph: SADD-010 */
    private void prepareNewCategoryEntry() {
        ws.setString("MODE-FLG", "1");
        fileSet.getCatgf().setRecord();
        ws.setCtCode(ws.getWkSaveCode());
        ws.setCtLevel(1);
        ws.setWkMsgLine("New category - enter details");
    }

    /** COBOL paragraph: SCHG-010 */
    private void prepareExistingCategoryEntry() {
        if (ws.getCtDelFlag() == 1) {
            ws.setString("MODE-FLG", "1");
            ws.setWkMsgLine("Deleted category - re-registering");
        } else {
            ws.setString("MODE-FLG", "2");
            ws.setWkMsgLine("Existing category - change or PF9 delete");
        }
        runChain(this::lookupParentCategoryName);
    }

    /** COBOL paragraph: EDIT-010 */
    private void processCategoryEditScreen() {
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-BODY"), ws);
        String scValCtName2 =
                Utility.acceptScreen(
                        "CT-NAME", () -> renderer.acceptField(ScreenDefs.getInput("CT-NAME")));
        ws.setCtName(scValCtName2);
        String scValCtParent3 =
                Utility.acceptScreen(
                        "CT-PARENT", () -> renderer.acceptField(ScreenDefs.getInput("CT-PARENT")));
        ws.setCtParent(Utility.parseIntOr(scValCtParent3.trim(), 0));
        String scValCtLevel4 =
                Utility.acceptScreen(
                        "CT-LEVEL", () -> renderer.acceptField(ScreenDefs.getInput("CT-LEVEL")));
        ws.setCtLevel(Utility.parseIntOr(scValCtLevel4.trim(), 0));
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
                    runChain(this::confirmAndDeleteCategory);
                } else {
                    ws.setWkMsgLine("Nothing to delete");
                    renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
                }
            }
            case "00" -> {
                runChain(this::validateCategoryFields);
                if ((ws.getErrFlg() != 1)) {
                    runChain(this::saveCategoryRecord);
                }
            }
            default -> {
                ws.setWkMsgLine("Invalid function key");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: VAL-010 */
    private void validateCategoryFields() {
        ws.setErrFlg(0);
        if (Utility.fieldEquals(ws.getCtName(), " ")) {
            ws.setWkMsgLine("Name is required");
            ws.setString("ERR-FLG", "1");
            displayValidationMessage();
            return;
        }
        runChain(this::validateParentCategory);
        // fall-through to next paragraph
        displayValidationMessage();
    }

    /** COBOL paragraph: VAL-999 */
    private void displayValidationMessage() {
        if ((ws.getErrFlg() == 1)) {
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
        /* CONTINUE */
    }

    /** COBOL paragraph: CHKP-010 */
    private void validateParentCategory() {
        if (ws.getCtParent() == 0) {
            return;
        }
        if (ws.getCtParent() == ws.getCtCode()) {
            ws.setWkMsgLine("Parent cannot be itself");
            ws.setString("ERR-FLG", "1");
            return;
        }
        ws.copyBytes("WK-SAVE-CTREC", "CT-REC");
        ws.setCtCode(ws.getCtParent());
        readCategoryByCode();
        if (fileSet.getCatgf().isInvalidKey()) {
            ws.setWkMsgLine("Parent category not found");
            ws.setString("ERR-FLG", "1");
        }
        if (!fileSet.getCatgf().isInvalidKey()) {
            if (ws.getCtDelFlag() == 1) {
                ws.setWkMsgLine("Parent category is deleted");
                ws.setString("ERR-FLG", "1");
            } else {
                ws.setWkParentName(ws.getCtName());
            }
        }
        ws.copyBytes("CT-REC", "WK-SAVE-CTREC");
    }

    /** COBOL paragraph: SAVE-010 */
    private void saveCategoryRecord() {
        ws.setCtDelFlag(0);
        if ((ws.getModeFlg() == 1)) {
            fileSet.getCatgf().write();
            ws.trySetString("FSTS", fileSet.getCatgf().getFileStatus());
            if (fileSet.getCatgf().isInvalidKey()) {
                ws.setWkMsgLine("Write failed - duplicate");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
            if (!fileSet.getCatgf().isInvalidKey()) {
                ws.setWkMsgLine("Category added");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        } else {
            fileSet.getCatgf().rewrite();
            ws.trySetString("FSTS", fileSet.getCatgf().getFileStatus());
            if (fileSet.getCatgf().isInvalidKey()) {
                ws.setWkMsgLine("Update failed");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
            if (!fileSet.getCatgf().isInvalidKey()) {
                ws.setWkMsgLine("Category updated");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: DEL-010 */
    private void confirmAndDeleteCategory() {
        ws.setWkConfirm(" ");
        ws.setWkMsgLine("Press Y then ENTER to delete");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-CONFIRM"), ws);
        String scValWkConfirm6 =
                Utility.acceptScreen(
                        "SC-CONF", () -> renderer.acceptField(ScreenDefs.getInput("SC-CONF")));
        ws.setWkConfirm(scValWkConfirm6);
        broadcastEstsStatus();
        if ((ws.getWkConfirm().equals("Y") || ws.getWkConfirm().equals("y"))) {
            ws.setCtDelFlag(1);
            fileSet.getCatgf().rewrite();
            ws.trySetString("FSTS", fileSet.getCatgf().getFileStatus());
            if (fileSet.getCatgf().isInvalidKey()) {
                ws.setWkMsgLine("Delete failed");
            }
            if (!fileSet.getCatgf().isInvalidKey()) {
                ws.setWkMsgLine("Category deleted");
            }
        } else {
            ws.setWkMsgLine("Delete cancelled");
        }
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
    }

    /** COBOL paragraph: LOOK-010 */
    private void lookupParentCategoryName() {
        ws.setWkParentName(" ");
        if (ws.getCtParent() == 0) {
            return;
        }
        ws.copyBytes("WK-SAVE-CTREC", "CT-REC");
        ws.setCtCode(ws.getCtParent());
        readCategoryByCode();
        if (fileSet.getCatgf().isInvalidKey()) {
            ws.setWkParentName("??? unknown parent");
        }
        if (!fileSet.getCatgf().isInvalidKey()) {
            ws.setWkParentName(ws.getCtName());
        }
        ws.copyBytes("CT-REC", "WK-SAVE-CTREC");
    }

    /** COBOL paragraph: TERM-010 */
    private void closeCategoryFile() {
        fileSet.getCatgf().close();
        ws.trySetString("FSTS", fileSet.getCatgf().getFileStatus());
    }

    /** COBOL paragraph: ABEND-010 */
    private void abortOnFileOpenError() {
        ws.setKaProgid("MS0060");
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

    /** Reads the category file by CT-CODE, falling back to the current record's key. */
    private void readCategoryByCode() {
        String rkVal = "";
        if (rkVal == null || rkVal.trim().isEmpty()) {
            try {
                rkVal = ws.getString("CT-CODE");
            } catch (Exception _e) {
            }
        }
        if (rkVal == null || rkVal.trim().isEmpty()) {
            try {
                rkVal = fileSet.getCatgf().extractKeyFromCurrentRecord();
            } catch (Exception _e3) {
            }
        }
        fileSet.getCatgf().readByKey(rkVal != null ? rkVal.trim() : "");
        ws.trySetString("FSTS", fileSet.getCatgf().getFileStatus());
    }
}
