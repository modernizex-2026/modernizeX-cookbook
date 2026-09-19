package com.sakura.ms0040.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.ms0040.domain.Ms0040FieldAccess;
import com.sakura.ms0040.domain.WorkingStorage;
import com.sakura.ms0040.runtime.Ms0040Datasets;
import com.sakura.ms0040.screen.ScreenDefs;
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

/** Business logic service generated from COBOL program MS0040. */
@Service
@Scope("prototype")
public class Ms0040Service extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final Ms0040Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Ms0040FieldAccess ws;

    public Ms0040Service(
            Ms0040Datasets fileSet,
            DateutService dateutService,
            AbortxService abortxService,
            ScreenRendererInstance renderer) {
        this.fileSet = fileSet;
        this.ws = new Ms0040FieldAccess(new WorkingStorage(), fileSet);
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
        runChain(this::closeAllFiles);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeProgram() {
        ws.setWkProgid("MS0040");
        ws.setWkTitle("Warehouse Master Maintenance");
        ws.setWkFkeyLine("ENTER=Read  PF9=Delete  PF3=End");
        ws.setKdFunc("TODY");
        dateut(ws.getKdate());
        ws.setWkSysymd(ws.getKdDate1());
        ws.setWkSysdate(ws.getKdDate1());
        fileSet.getWhsef().open(FileOpenMode.IO);
        ws.trySetString("FSTS", fileSet.getWhsef().getFileStatus());
        if (Utility.fieldEquals(ws.getFsts(), "35") || Utility.fieldEquals(ws.getFsts(), "30")) {
            fileSet.getWhsef().open(FileOpenMode.OUTPUT);
            ws.trySetString("FSTS", fileSet.getWhsef().getFileStatus());
            fileSet.getWhsef().close();
            ws.trySetString("FSTS", fileSet.getWhsef().getFileStatus());
            fileSet.getWhsef().open(FileOpenMode.IO);
            ws.trySetString("FSTS", fileSet.getWhsef().getFileStatus());
        }
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("WHSEF");
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
    private void processMainScreen() {
        runChain(this::clearWarehouseWorkArea);
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEADER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-FOOTER"), ws);
        ws.setWkMsgLine(" ");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-KEY"), ws);
        String scValWhCode0 =
                Utility.acceptScreen(
                        "SC-KEY", () -> renderer.acceptField(ScreenDefs.getInput("SC-KEY")));
        ws.setWhCode(Utility.parseIntOr(scValWhCode0.trim(), 0));
        broadcastEstsStatus();
        switch (String.valueOf(ws.getEsts())) {
            case "03" -> {
                ws.setString("END-FLG", "1");
            }
            case "00" -> {
                runChain(this::processPrimaryKeyEntry);
            }
            default -> {
                ws.setWkMsgLine("Invalid function key");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: CLEAR-010 */
    private void clearWarehouseWorkArea() {
        fileSet.getWhsef().setRecord();
        ws.setWkMgrName(" ");
        ws.setWkConfirm(" ");
    }

    /** COBOL paragraph: PKEY-010 */
    private void processPrimaryKeyEntry() {
        if (ws.getWhCode() == 0) {
            ws.setWkMsgLine("Warehouse code must not be zero");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setWkSaveCode(ws.getWhCode());
        readByKeyWithFallback(fileSet.getWhsef(), "WH-CODE");
        if (fileSet.getWhsef().isInvalidKey()) {
            runChain(this::prepareNewWarehouseRecord);
        }
        if (!fileSet.getWhsef().isInvalidKey()) {
            runChain(this::prepareChangeWarehouseRecord);
        }
        runChain(this::processWarehouseDetailScreen);
    }

    /** COBOL paragraph: SADD-010 */
    private void prepareNewWarehouseRecord() {
        ws.setString("MODE-FLG", "1");
        fileSet.getWhsef().setRecord();
        ws.setWhCode(ws.getWkSaveCode());
        ws.setWhType(1);
        ws.setWkMsgLine("New warehouse - enter details");
    }

    /** COBOL paragraph: SCHG-010 */
    private void prepareChangeWarehouseRecord() {
        if (ws.getWhDelFlag() == 1) {
            ws.setString("MODE-FLG", "1");
            ws.setWkMsgLine("Deleted warehouse - re-registering");
        } else {
            ws.setString("MODE-FLG", "2");
            ws.setWkMsgLine("Existing warehouse - change or PF9 delete");
        }
        runChain(this::lookupManagerName);
    }

    /** COBOL paragraph: EDIT-010 */
    private void processWarehouseDetailScreen() {
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-BODY"), ws);
        String scValWhName2 =
                Utility.acceptScreen(
                        "WH-NAME", () -> renderer.acceptField(ScreenDefs.getInput("WH-NAME")));
        ws.setWhName(scValWhName2);
        String scValWhZip3 =
                Utility.acceptScreen(
                        "WH-ZIP", () -> renderer.acceptField(ScreenDefs.getInput("WH-ZIP")));
        ws.setWhZip(scValWhZip3);
        String scValWhAddr4 =
                Utility.acceptScreen(
                        "WH-ADDR", () -> renderer.acceptField(ScreenDefs.getInput("WH-ADDR")));
        ws.setWhAddr(scValWhAddr4);
        String scValWhTel5 =
                Utility.acceptScreen(
                        "WH-TEL", () -> renderer.acceptField(ScreenDefs.getInput("WH-TEL")));
        ws.setWhTel(scValWhTel5);
        String scValWhType6 =
                Utility.acceptScreen(
                        "WH-TYPE", () -> renderer.acceptField(ScreenDefs.getInput("WH-TYPE")));
        ws.setWhType(Utility.parseIntOr(scValWhType6.trim(), 0));
        String scValWhManager7 =
                Utility.acceptScreen(
                        "WH-MANAGER",
                        () -> renderer.acceptField(ScreenDefs.getInput("WH-MANAGER")));
        ws.setWhManager(Utility.parseIntOr(scValWhManager7.trim(), 0));
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
                    runChain(this::processWarehouseDeletion);
                } else {
                    ws.setWkMsgLine("Nothing to delete");
                    renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
                }
            }
            case "00" -> {
                runChain(this::validateWarehouseFields);
                if ((ws.getErrFlg() != 1)) {
                    runChain(this::saveWarehouseRecord);
                }
            }
            default -> {
                ws.setWkMsgLine("Invalid function key");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: VAL-010 */
    private void validateWarehouseFields() {
        ws.setErrFlg(0);
        if (Utility.fieldEquals(ws.getWhName(), " ")) {
            ws.setWkMsgLine("Name is required");
            ws.setString("ERR-FLG", "1");
            showValidationMessage();
            return;
        }
        if (ws.getWhType() < 1 || ws.getWhType() > 3) {
            ws.setWkMsgLine("Type must be 1-3");
            ws.setString("ERR-FLG", "1");
            showValidationMessage();
            return;
        }
        if (ws.getWhManager() != 0) {
            ws.setSfCode(ws.getWhManager());
            readByKeyWithFallback(fileSet.getStaff(), "SF-CODE");
            if (fileSet.getStaff().isInvalidKey()) {
                ws.setWkMsgLine("Manager code not found");
                ws.setString("ERR-FLG", "1");
                showValidationMessage();
                return;
            }
            if (!fileSet.getStaff().isInvalidKey()) {
                ws.setWkMgrName(ws.getSfName());
            }
        }
        // fall-through to next paragraph
        showValidationMessage();
    }

    /** COBOL paragraph: VAL-999 */
    private void showValidationMessage() {
        if ((ws.getErrFlg() == 1)) {
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
        /* CONTINUE */
    }

    /** COBOL paragraph: SAVE-010 */
    private void saveWarehouseRecord() {
        ws.setWhDelFlag(0);
        if ((ws.getModeFlg() == 1)) {
            fileSet.getWhsef().write();
            ws.trySetString("FSTS", fileSet.getWhsef().getFileStatus());
            if (fileSet.getWhsef().isInvalidKey()) {
                ws.setWkMsgLine("Write failed - duplicate");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
            if (!fileSet.getWhsef().isInvalidKey()) {
                ws.setWkMsgLine("Warehouse added");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        } else {
            fileSet.getWhsef().rewrite();
            ws.trySetString("FSTS", fileSet.getWhsef().getFileStatus());
            if (fileSet.getWhsef().isInvalidKey()) {
                ws.setWkMsgLine("Update failed");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
            if (!fileSet.getWhsef().isInvalidKey()) {
                ws.setWkMsgLine("Warehouse updated");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: DEL-010 */
    private void processWarehouseDeletion() {
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
            ws.setWhDelFlag(1);
            fileSet.getWhsef().rewrite();
            ws.trySetString("FSTS", fileSet.getWhsef().getFileStatus());
            if (fileSet.getWhsef().isInvalidKey()) {
                ws.setWkMsgLine("Delete failed");
            }
            if (!fileSet.getWhsef().isInvalidKey()) {
                ws.setWkMsgLine("Warehouse deleted");
            }
        } else {
            ws.setWkMsgLine("Delete cancelled");
        }
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
    }

    /** COBOL paragraph: LOOK-010 */
    private void lookupManagerName() {
        ws.setWkMgrName(" ");
        if (ws.getWhManager() != 0) {
            ws.setSfCode(ws.getWhManager());
            readByKeyWithFallback(fileSet.getStaff(), "SF-CODE");
            if (fileSet.getStaff().isInvalidKey()) {
                ws.setWkMgrName("??? unknown manager");
            }
            if (!fileSet.getStaff().isInvalidKey()) {
                ws.setWkMgrName(ws.getSfName());
            }
        }
    }

    /** COBOL paragraph: TERM-010 */
    private void closeAllFiles() {
        fileSet.getWhsef().close();
        ws.trySetString("FSTS", fileSet.getWhsef().getFileStatus());
        fileSet.getStaff().close();
        ws.trySetString("FSTS", fileSet.getStaff().getFileStatus());
    }

    /** COBOL paragraph: ABEND-010 */
    private void abortOnFileError() {
        ws.setKaProgid("MS0040");
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
     * Resolves the record key (WS field, else the file's current-record key), reads it, and updates
     * FSTS from the resulting file status.
     */
    private void readByKeyWithFallback(RawDatasetBase file, String keyField) {
        String rkVal = "";
        if (rkVal == null || rkVal.trim().isEmpty()) {
            try {
                rkVal = ws.getString(keyField);
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
