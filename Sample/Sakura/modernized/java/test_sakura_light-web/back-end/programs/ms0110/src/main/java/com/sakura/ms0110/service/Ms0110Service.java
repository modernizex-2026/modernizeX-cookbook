package com.sakura.ms0110.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.ms0110.domain.Ms0110FieldAccess;
import com.sakura.ms0110.domain.WorkingStorage;
import com.sakura.ms0110.runtime.Ms0110Datasets;
import com.sakura.ms0110.screen.ScreenDefs;
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

/** Business logic service generated from COBOL program MS0110. */
@Service
@Scope("prototype")
public class Ms0110Service extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final Ms0110Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Ms0110FieldAccess ws;

    public Ms0110Service(
            Ms0110Datasets fileSet,
            DateutService dateutService,
            AbortxService abortxService,
            ScreenRendererInstance renderer) {
        this.fileSet = fileSet;
        this.ws = new Ms0110FieldAccess(new WorkingStorage(), fileSet);
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
        runChain(this::executeMainLoop);
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
    private void executeMainLoop() {
        runChain(this::initializeAndOpenRegnf);
        while ((ws.getEndFlg() != 1)) {
            runChain(this::processMainScreenKey);
        }
        runChain(this::closeRegnfFile);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeAndOpenRegnf() {
        ws.setWkProgid("MS0110");
        ws.setWkTitle("Region Master Maintenance");
        ws.setWkFkeyLine("ENTER=Read  PF9=Delete  PF3=End");
        ws.setKdFunc("TODY");
        dateut(ws.getKdate());
        ws.setWkSysymd(ws.getKdDate1());
        ws.setWkSysdate(ws.getKdDate1());
        fileSet.getRegnf().open(FileOpenMode.IO);
        ws.trySetString("FSTS", fileSet.getRegnf().getFileStatus());
        if (Utility.fieldEquals(ws.getFsts(), "35") || Utility.fieldEquals(ws.getFsts(), "30")) {
            fileSet.getRegnf().open(FileOpenMode.OUTPUT);
            ws.trySetString("FSTS", fileSet.getRegnf().getFileStatus());
            fileSet.getRegnf().close();
            ws.trySetString("FSTS", fileSet.getRegnf().getFileStatus());
            fileSet.getRegnf().open(FileOpenMode.IO);
            ws.trySetString("FSTS", fileSet.getRegnf().getFileStatus());
        }
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("REGNF");
            runChain(this::abendOnFileOpenError);
        }
    }

    /** COBOL paragraph: MAIN-RTN-010 */
    private void processMainScreenKey() {
        runChain(this::clearRegionRecord);
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEADER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-FOOTER"), ws);
        ws.setWkMsgLine(" ");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-KEY"), ws);
        String scValRgCode0 =
                Utility.acceptScreen(
                        "SC-KEY", () -> renderer.acceptField(ScreenDefs.getInput("SC-KEY")));
        ws.setRgCode(Utility.parseIntOr(scValRgCode0.trim(), 0));
        broadcastEstsStatus();
        switch (String.valueOf(ws.getEsts())) {
            case "03" -> {
                ws.setString("END-FLG", "1");
            }
            case "00" -> {
                runChain(this::lookupRegionByKey);
            }
            default -> {
                ws.setWkMsgLine("Invalid function key");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: CLEAR-010 */
    private void clearRegionRecord() {
        fileSet.getRegnf().setRecord();
        ws.setWkConfirm(" ");
        ws.setWkSaveCode(0);
    }

    /** COBOL paragraph: PKEY-010 */
    private void lookupRegionByKey() {
        if (ws.getRgCode() == 0) {
            ws.setWkMsgLine("Region code must not be zero");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setWkSaveCode(ws.getRgCode());
        String rkVal_1 = "";
        if (rkVal_1 == null || rkVal_1.trim().isEmpty()) {
            try {
                rkVal_1 = ws.getString("RG-CODE");
            } catch (Exception _e) {
            }
        }
        if (rkVal_1 == null || rkVal_1.trim().isEmpty()) {
            try {
                rkVal_1 = fileSet.getRegnf().extractKeyFromCurrentRecord();
            } catch (Exception _e3) {
            }
        }
        fileSet.getRegnf().readByKey(rkVal_1 != null ? rkVal_1.trim() : "");
        ws.trySetString("FSTS", fileSet.getRegnf().getFileStatus());
        if (fileSet.getRegnf().isInvalidKey()) {
            runChain(this::prepareNewRegionAdd);
        }
        if (!fileSet.getRegnf().isInvalidKey()) {
            runChain(this::determineChangeMode);
        }
        runChain(this::processRegionEditScreen);
    }

    /** COBOL paragraph: SADD-010 */
    private void prepareNewRegionAdd() {
        ws.setString("MODE-FLG", "1");
        fileSet.getRegnf().setRecord();
        ws.setRgCode(ws.getWkSaveCode());
        ws.setWkMsgLine("New region - enter details");
    }

    /** COBOL paragraph: SCHG-010 */
    private void determineChangeMode() {
        if (ws.getRgDelFlag() == 1) {
            ws.setString("MODE-FLG", "1");
            ws.setWkMsgLine("Deleted region - re-registering");
        } else {
            ws.setString("MODE-FLG", "2");
            ws.setWkMsgLine("Existing region - change or PF9 delete");
        }
    }

    /** COBOL paragraph: EDIT-010 */
    private void processRegionEditScreen() {
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-BODY"), ws);
        String scValRgName2 =
                Utility.acceptScreen(
                        "RG-NAME", () -> renderer.acceptField(ScreenDefs.getInput("RG-NAME")));
        ws.setRgName(scValRgName2);
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
                    runChain(this::confirmAndDeleteRegion);
                } else {
                    ws.setWkMsgLine("Nothing to delete");
                    renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
                }
            }
            case "00" -> {
                runChain(this::validateRegionName);
                if ((ws.getErrFlg() != 1)) {
                    runChain(this::saveRegionRecord);
                }
            }
            default -> {
                ws.setWkMsgLine("Invalid function key");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: VAL-010 */
    private void validateRegionName() {
        ws.setErrFlg(0);
        if (Utility.fieldEquals(ws.getRgName(), " ")) {
            ws.setWkMsgLine("Region name is required");
            ws.setString("ERR-FLG", "1");
        }
        // fall-through to next paragraph
        if ((ws.getErrFlg() == 1)) {
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
    }

    /** COBOL paragraph: SAVE-010 */
    private void saveRegionRecord() {
        ws.setRgDelFlag(0);
        if ((ws.getModeFlg() == 1)) {
            fileSet.getRegnf().write();
            ws.trySetString("FSTS", fileSet.getRegnf().getFileStatus());
            if (fileSet.getRegnf().isInvalidKey()) {
                ws.setWkMsgLine("Write failed - duplicate");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
            if (!fileSet.getRegnf().isInvalidKey()) {
                ws.setWkMsgLine("Region added");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        } else {
            fileSet.getRegnf().rewrite();
            ws.trySetString("FSTS", fileSet.getRegnf().getFileStatus());
            if (fileSet.getRegnf().isInvalidKey()) {
                ws.setWkMsgLine("Update failed");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
            if (!fileSet.getRegnf().isInvalidKey()) {
                ws.setWkMsgLine("Region updated");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: DEL-010 */
    private void confirmAndDeleteRegion() {
        ws.setWkConfirm(" ");
        ws.setWkMsgLine("Press Y then ENTER to delete");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-CONFIRM"), ws);
        String scValWkConfirm3 =
                Utility.acceptScreen(
                        "SC-CONF", () -> renderer.acceptField(ScreenDefs.getInput("SC-CONF")));
        ws.setWkConfirm(scValWkConfirm3);
        broadcastEstsStatus();
        if ((ws.getWkConfirm().equals("Y") || ws.getWkConfirm().equals("y"))) {
            ws.setRgDelFlag(1);
            fileSet.getRegnf().rewrite();
            ws.trySetString("FSTS", fileSet.getRegnf().getFileStatus());
            if (fileSet.getRegnf().isInvalidKey()) {
                ws.setWkMsgLine("Delete failed");
            }
            if (!fileSet.getRegnf().isInvalidKey()) {
                ws.setWkMsgLine("Region deleted");
            }
        } else {
            ws.setWkMsgLine("Delete cancelled");
        }
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
    }

    /** COBOL paragraph: TERM-010 */
    private void closeRegnfFile() {
        fileSet.getRegnf().close();
        ws.trySetString("FSTS", fileSet.getRegnf().getFileStatus());
    }

    /** COBOL paragraph: ABEND-010 */
    private void abendOnFileOpenError() {
        ws.setKaProgid("MS0110");
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
