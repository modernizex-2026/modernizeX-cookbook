package com.sakura.ms0100.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.ms0100.domain.Ms0100FieldAccess;
import com.sakura.ms0100.domain.WorkingStorage;
import com.sakura.ms0100.runtime.Ms0100Datasets;
import com.sakura.ms0100.screen.ScreenDefs;
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

/** Business logic service generated from COBOL program MS0100. */
@Service
@Scope("prototype")
public class Ms0100Service extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final Ms0100Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Ms0100FieldAccess ws;

    public Ms0100Service(
            Ms0100Datasets fileSet,
            DateutService dateutService,
            AbortxService abortxService,
            ScreenRendererInstance renderer) {
        this.fileSet = fileSet;
        this.ws = new Ms0100FieldAccess(new WorkingStorage(), fileSet);
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
        runChain(this::initializeAndOpenBankFile);
        while ((ws.getEndFlg() != 1)) {
            runChain(this::displayMainScreenAndDispatch);
        }
        runChain(this::closeBankFile);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeAndOpenBankFile() {
        ws.setWkProgid("MS0100");
        ws.setWkTitle("Bank Master Maintenance");
        ws.setWkFkeyLine("ENTER=Read  PF9=Delete  PF3=End");
        ws.setKdFunc("TODY");
        dateut(ws.getKdate());
        ws.setWkSysymd(ws.getKdDate1());
        ws.setWkSysdate(ws.getKdDate1());
        fileSet.getBankf().open(FileOpenMode.IO);
        ws.trySetString("FSTS", fileSet.getBankf().getFileStatus());
        if (Utility.fieldEquals(ws.getFsts(), "35") || Utility.fieldEquals(ws.getFsts(), "30")) {
            fileSet.getBankf().open(FileOpenMode.OUTPUT);
            ws.trySetString("FSTS", fileSet.getBankf().getFileStatus());
            fileSet.getBankf().close();
            ws.trySetString("FSTS", fileSet.getBankf().getFileStatus());
            fileSet.getBankf().open(FileOpenMode.IO);
            ws.trySetString("FSTS", fileSet.getBankf().getFileStatus());
        }
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("BANKF");
            runChain(this::abortOnFileOpenError);
        }
    }

    /** COBOL paragraph: MAIN-RTN-010 */
    private void displayMainScreenAndDispatch() {
        runChain(this::resetBankRecord);
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEADER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-FOOTER"), ws);
        ws.setWkMsgLine(" ");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-KEY"), ws);
        String scValBkCode0 =
                Utility.acceptScreen(
                        "SC-KEY", () -> renderer.acceptField(ScreenDefs.getInput("SC-KEY")));
        ws.setBkCode(Utility.parseIntOr(scValBkCode0.trim(), 0));
        broadcastEstsStatus();
        switch (String.valueOf(ws.getEsts())) {
            case "03" -> {
                ws.setString("END-FLG", "1");
            }
            case "00" -> {
                runChain(this::readBankByKeyAndDispatch);
            }
            default -> {
                ws.setWkMsgLine("Invalid function key");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: CLEAR-010 */
    private void resetBankRecord() {
        fileSet.getBankf().setRecord();
        ws.setWkConfirm(" ");
        ws.setWkSaveCode(0);
    }

    /** COBOL paragraph: PKEY-010 */
    private void readBankByKeyAndDispatch() {
        if (ws.getBkCode() == 0) {
            ws.setWkMsgLine("Bank code must not be zero");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setWkSaveCode(ws.getBkCode());
        String rkVal_1 = "";
        if (rkVal_1 == null || rkVal_1.trim().isEmpty()) {
            try {
                rkVal_1 = ws.getString("BK-CODE");
            } catch (Exception _e) {
            }
        }
        if (rkVal_1 == null || rkVal_1.trim().isEmpty()) {
            try {
                rkVal_1 = fileSet.getBankf().extractKeyFromCurrentRecord();
            } catch (Exception _e3) {
            }
        }
        fileSet.getBankf().readByKey(rkVal_1 != null ? rkVal_1.trim() : "");
        ws.trySetString("FSTS", fileSet.getBankf().getFileStatus());
        if (fileSet.getBankf().isInvalidKey()) {
            runChain(this::prepareNewBankEntry);
        }
        if (!fileSet.getBankf().isInvalidKey()) {
            runChain(this::prepareExistingBankChange);
        }
        runChain(this::acceptBankDetailsAndDispatch);
    }

    /** COBOL paragraph: SADD-010 */
    private void prepareNewBankEntry() {
        ws.setString("MODE-FLG", "1");
        fileSet.getBankf().setRecord();
        ws.setBkCode(ws.getWkSaveCode());
        ws.setWkMsgLine("New bank - enter details");
    }

    /** COBOL paragraph: SCHG-010 */
    private void prepareExistingBankChange() {
        if (ws.getBkDelFlag() == 1) {
            ws.setString("MODE-FLG", "1");
            ws.setWkMsgLine("Deleted bank - re-registering");
        } else {
            ws.setString("MODE-FLG", "2");
            ws.setWkMsgLine("Existing bank - change or PF9 delete");
        }
    }

    /** COBOL paragraph: EDIT-010 */
    private void acceptBankDetailsAndDispatch() {
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-BODY"), ws);
        String scValBkName2 =
                Utility.acceptScreen(
                        "BK-NAME", () -> renderer.acceptField(ScreenDefs.getInput("BK-NAME")));
        ws.setBkName(scValBkName2);
        String scValBkBranch3 =
                Utility.acceptScreen(
                        "BK-BRANCH", () -> renderer.acceptField(ScreenDefs.getInput("BK-BRANCH")));
        ws.setBkBranch(scValBkBranch3);
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
                    runChain(this::confirmAndDeleteBank);
                } else {
                    ws.setWkMsgLine("Nothing to delete");
                    renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
                }
            }
            case "00" -> {
                runChain(this::validateBankName);
                if ((ws.getErrFlg() != 1)) {
                    runChain(this::saveBankRecord);
                }
            }
            default -> {
                ws.setWkMsgLine("Invalid function key");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: VAL-010 */
    private void validateBankName() {
        ws.setErrFlg(0);
        if (Utility.fieldEquals(ws.getBkName(), " ")) {
            ws.setWkMsgLine("Bank name is required");
            ws.setString("ERR-FLG", "1");
        }
        if ((ws.getErrFlg() == 1)) {
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
    }

    /** COBOL paragraph: SAVE-010 */
    private void saveBankRecord() {
        ws.setBkDelFlag(0);
        if ((ws.getModeFlg() == 1)) {
            fileSet.getBankf().write();
            ws.trySetString("FSTS", fileSet.getBankf().getFileStatus());
            if (fileSet.getBankf().isInvalidKey()) {
                ws.setWkMsgLine("Write failed - duplicate");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
            if (!fileSet.getBankf().isInvalidKey()) {
                ws.setWkMsgLine("Bank added");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        } else {
            fileSet.getBankf().rewrite();
            ws.trySetString("FSTS", fileSet.getBankf().getFileStatus());
            if (fileSet.getBankf().isInvalidKey()) {
                ws.setWkMsgLine("Update failed");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
            if (!fileSet.getBankf().isInvalidKey()) {
                ws.setWkMsgLine("Bank updated");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: DEL-010 */
    private void confirmAndDeleteBank() {
        ws.setWkConfirm(" ");
        ws.setWkMsgLine("Press Y then ENTER to delete");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-CONFIRM"), ws);
        String scValWkConfirm4 =
                Utility.acceptScreen(
                        "SC-CONF", () -> renderer.acceptField(ScreenDefs.getInput("SC-CONF")));
        ws.setWkConfirm(scValWkConfirm4);
        broadcastEstsStatus();
        if ((ws.getWkConfirm().equals("Y") || ws.getWkConfirm().equals("y"))) {
            ws.setBkDelFlag(1);
            fileSet.getBankf().rewrite();
            ws.trySetString("FSTS", fileSet.getBankf().getFileStatus());
            if (fileSet.getBankf().isInvalidKey()) {
                ws.setWkMsgLine("Delete failed");
            }
            if (!fileSet.getBankf().isInvalidKey()) {
                ws.setWkMsgLine("Bank deleted");
            }
        } else {
            ws.setWkMsgLine("Delete cancelled");
        }
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
    }

    /** COBOL paragraph: TERM-010 */
    private void closeBankFile() {
        fileSet.getBankf().close();
        ws.trySetString("FSTS", fileSet.getBankf().getFileStatus());
    }

    /** COBOL paragraph: ABEND-010 */
    private void abortOnFileOpenError() {
        ws.setKaProgid("MS0100");
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
}
