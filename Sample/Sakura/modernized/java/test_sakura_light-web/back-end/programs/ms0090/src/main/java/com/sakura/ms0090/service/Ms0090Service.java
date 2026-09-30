package com.sakura.ms0090.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.ms0090.domain.Ms0090FieldAccess;
import com.sakura.ms0090.domain.WorkingStorage;
import com.sakura.ms0090.runtime.Ms0090Datasets;
import com.sakura.ms0090.screen.ScreenDefs;
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

import java.math.BigDecimal;

/** Business logic service generated from COBOL program MS0090. */
@Service
@Scope("prototype")
public class Ms0090Service extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final Ms0090Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Ms0090FieldAccess ws;

    public Ms0090Service(
            Ms0090Datasets fileSet,
            DateutService dateutService,
            AbortxService abortxService,
            ScreenRendererInstance renderer) {
        this.fileSet = fileSet;
        this.ws = new Ms0090FieldAccess(new WorkingStorage(), fileSet);
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
        runChain(this::closeTaxFile);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeProgram() {
        ws.setWkProgid("MS0090");
        ws.setWkTitle("Tax Rate Master Maintenance");
        ws.setWkFkeyLine("ENTER=Read  PF9=Delete  PF3=End");
        ws.setKdFunc("TODY");
        dateut(ws.getKdate());
        ws.setWkSysymd(ws.getKdDate1());
        ws.setWkSysdate(ws.getKdDate1());
        fileSet.getTaxf().open(FileOpenMode.IO);
        ws.trySetString("FSTS", fileSet.getTaxf().getFileStatus());
        if (Utility.fieldEquals(ws.getFsts(), "35") || Utility.fieldEquals(ws.getFsts(), "30")) {
            fileSet.getTaxf().open(FileOpenMode.OUTPUT);
            ws.trySetString("FSTS", fileSet.getTaxf().getFileStatus());
            fileSet.getTaxf().close();
            ws.trySetString("FSTS", fileSet.getTaxf().getFileStatus());
            fileSet.getTaxf().open(FileOpenMode.IO);
            ws.trySetString("FSTS", fileSet.getTaxf().getFileStatus());
        }
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("TAXF");
            runChain(this::abortOnFileOpenError);
        }
    }

    /** COBOL paragraph: MAIN-RTN-010 */
    private void processMainScreen() {
        runChain(this::clearWorkFields);
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEADER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-FOOTER"), ws);
        ws.setWkMsgLine(" ");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-KEY"), ws);
        String scValTxCode0 =
                Utility.acceptScreen(
                        "TX-CODE", () -> renderer.acceptField(ScreenDefs.getInput("TX-CODE")));
        ws.setTxCode(Utility.parseIntOr(scValTxCode0.trim(), 0));
        String scValTxStartDate1 =
                Utility.acceptScreen(
                        "TX-START-DATE",
                        () -> renderer.acceptField(ScreenDefs.getInput("TX-START-DATE")));
        ws.setTxStartDate(Utility.parseIntOr(scValTxStartDate1.trim(), 0));
        broadcastEstsStatus();
        switch (String.valueOf(ws.getEsts())) {
            case "03" -> {
                ws.setString("END-FLG", "1");
            }
            case "00" -> {
                runChain(this::validateKeyAndLookupTaxRecord);
            }
            default -> {
                ws.setWkMsgLine("Invalid function key");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: CLEAR-010 */
    private void clearWorkFields() {
        fileSet.getTaxf().setRecord();
        ws.setWkConfirm(" ");
        ws.setWkSaveCode(0);
        ws.setWkSaveDate(0);
    }

    /** COBOL paragraph: PKEY-010 */
    private void validateKeyAndLookupTaxRecord() {
        if (ws.getTxCode() == 0) {
            ws.setWkMsgLine("Tax category must be 1 to 9");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        if (ws.getTxStartDate() == 0) {
            ws.setWkMsgLine("Start date is required");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setKdFunc("VALD");
        ws.setKdDate1(ws.getTxStartDate());
        dateut(ws.getKdate());
        if (!Utility.fieldEquals(ws.getKdStatus(), "00")) {
            ws.setWkMsgLine("Start date is invalid");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setWkSaveCode(ws.getTxCode());
        ws.setWkSaveDate(ws.getTxStartDate());
        StringBuilder rkSb_2 = new StringBuilder();
        String rkPart0_2 = "";
        try {
            rkPart0_2 = ws.getString("TX-CODE");
        } catch (Exception _e) {
        }
        rkSb_2.append(rkPart0_2 != null ? rkPart0_2.trim() : "");
        String rkPart1_2 = "";
        try {
            rkPart1_2 = ws.getString("TX-START-DATE");
        } catch (Exception _e) {
        }
        rkSb_2.append('|');
        rkSb_2.append(rkPart1_2 != null ? rkPart1_2.trim() : "");
        fileSet.getTaxf().readByKey(rkSb_2.toString());
        ws.trySetString("FSTS", fileSet.getTaxf().getFileStatus());
        if (fileSet.getTaxf().isInvalidKey()) {
            runChain(this::prepareAddMode);
        }
        if (!fileSet.getTaxf().isInvalidKey()) {
            runChain(this::prepareChangeMode);
        }
        runChain(this::processEditScreen);
    }

    /** COBOL paragraph: SADD-010 */
    private void prepareAddMode() {
        ws.setString("MODE-FLG", "1");
        fileSet.getTaxf().setRecord();
        ws.setTxCode(ws.getWkSaveCode());
        ws.setTxStartDate(ws.getWkSaveDate());
        ws.setWkMsgLine("New tax rate - enter details");
    }

    /** COBOL paragraph: SCHG-010 */
    private void prepareChangeMode() {
        ws.setString("MODE-FLG", "2");
        ws.setWkMsgLine("Existing tax rate - change or PF9 delete");
    }

    /** COBOL paragraph: EDIT-010 */
    private void processEditScreen() {
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-BODY"), ws);
        String scValTxRate3 =
                Utility.acceptScreen(
                        "TX-RATE", () -> renderer.acceptField(ScreenDefs.getInput("TX-RATE")));
        try {
            ws.setTxRate(new BigDecimal(scValTxRate3.trim()));
        } catch (NumberFormatException _nfe) {
            ws.setTxRate(BigDecimal.ZERO);
        }
        String scValTxName4 =
                Utility.acceptScreen(
                        "TX-NAME", () -> renderer.acceptField(ScreenDefs.getInput("TX-NAME")));
        ws.setTxName(scValTxName4);
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
                    runChain(this::deleteTaxRecordWithConfirmation);
                } else {
                    ws.setWkMsgLine("Nothing to delete");
                    renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
                }
            }
            case "00" -> {
                runChain(this::validateTaxFields);
                if ((ws.getErrFlg() != 1)) {
                    runChain(this::saveTaxRecord);
                }
            }
            default -> {
                ws.setWkMsgLine("Invalid function key");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: VAL-010 */
    private void validateTaxFields() {
        ws.setErrFlg(0);
        if ((ws.getTxRate().signum() < 0)) {
            ws.setWkMsgLine("Rate cannot be negative");
            ws.setString("ERR-FLG", "1");
            reportValidationErrors();
            return;
        }
        if ((ws.getTxRate().compareTo(new BigDecimal("1")) >= 0)) {
            ws.setWkMsgLine("Rate must be a fraction below 1.000");
            ws.setString("ERR-FLG", "1");
            reportValidationErrors();
            return;
        }
        if (Utility.fieldEquals(ws.getTxName(), " ")) {
            ws.setWkMsgLine("Rate name is required");
            ws.setString("ERR-FLG", "1");
        }
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

    /** COBOL paragraph: SAVE-010 */
    private void saveTaxRecord() {
        if ((ws.getModeFlg() == 1)) {
            fileSet.getTaxf().write();
            ws.trySetString("FSTS", fileSet.getTaxf().getFileStatus());
            if (fileSet.getTaxf().isInvalidKey()) {
                ws.setWkMsgLine("Write failed - duplicate");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
            if (!fileSet.getTaxf().isInvalidKey()) {
                ws.setWkMsgLine("Tax rate added");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        } else {
            fileSet.getTaxf().rewrite();
            ws.trySetString("FSTS", fileSet.getTaxf().getFileStatus());
            if (fileSet.getTaxf().isInvalidKey()) {
                ws.setWkMsgLine("Update failed");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
            if (!fileSet.getTaxf().isInvalidKey()) {
                ws.setWkMsgLine("Tax rate updated");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: DEL-010 */
    private void deleteTaxRecordWithConfirmation() {
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
            fileSet.getTaxf().delete();
            ws.trySetString("FSTS", fileSet.getTaxf().getFileStatus());
            if (fileSet.getTaxf().isInvalidKey()) {
                ws.setWkMsgLine("Delete failed");
            }
            if (!fileSet.getTaxf().isInvalidKey()) {
                ws.setWkMsgLine("Tax rate deleted");
            }
        } else {
            ws.setWkMsgLine("Delete cancelled");
        }
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
    }

    /** COBOL paragraph: TERM-010 */
    private void closeTaxFile() {
        fileSet.getTaxf().close();
        ws.trySetString("FSTS", fileSet.getTaxf().getFileStatus());
    }

    /** COBOL paragraph: ABEND-010 */
    private void abortOnFileOpenError() {
        ws.setKaProgid("MS0090");
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
