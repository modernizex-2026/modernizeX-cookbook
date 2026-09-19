package com.sakura.iv0020.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.iv0020.domain.Iv0020FieldAccess;
import com.sakura.iv0020.domain.WorkingStorage;
import com.sakura.iv0020.runtime.Iv0020Datasets;
import com.sakura.iv0020.screen.ScreenDefs;
import com.sakura.numgen.service.NumgenService;
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
import com.sakura.runtime.linkage.NumgenLinkParm;
import com.sakura.runtime.record.RawDatasetBase;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/** Business logic service generated from COBOL program IV0020. */
@Service
@Scope("prototype")
public class Iv0020Service extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final Iv0020Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL NUMGEN. */
    private NumgenService numgenService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Iv0020FieldAccess ws;

    public Iv0020Service(
            Iv0020Datasets fileSet,
            DateutService dateutService,
            NumgenService numgenService,
            AbortxService abortxService,
            ScreenRendererInstance renderer) {
        this.fileSet = fileSet;
        this.ws = new Iv0020FieldAccess(new WorkingStorage(), fileSet);
        this.dateutService = dateutService;
        this.numgenService = numgenService;
        this.abortxService = abortxService;
        this.renderer = renderer;
    }

    @Override
    public void setRenderer(ScreenRendererInstance renderer) {
        super.setRenderer(renderer);
        if (dateutService instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (numgenService instanceof ScreenRendererAware rra) {
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
            runChain(this::processMainScreenInput);
        }
        runChain(this::closeProgramFiles);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeProgram() {
        ws.setWkProgid("IV0020");
        ws.setWkTitle("Stock Adjustment");
        ws.setWkFkeyLine("ENTER=Read  PF3=End  PF4=Clear");
        ws.setKdFunc("TODY");
        dateut(ws.getKdate());
        ws.setWkSysymd(ws.getKdDate1());
        ws.setWkSysdate(ws.getKdDate1());
        runChain(this::openProgramFiles);
    }

    /** COBOL paragraph: OPENF-010 */
    private void openProgramFiles() {
        openFileWithRetry(fileSet.getStokf(), FileOpenMode.IO, "STOKF", true);
        openFileWithRetry(fileSet.getSmovf(), FileOpenMode.IO, "SMOVF", true);
        openFileWithRetry(fileSet.getProdf(), FileOpenMode.INPUT, "PRODF", false);
        openFileWithRetry(fileSet.getWhsef(), FileOpenMode.INPUT, "WHSEF", false);
    }

    /** COBOL paragraph: MAIN-010 */
    private void processMainScreenInput() {
        runChain(this::acceptProductWarehouseKeys);
        switch (String.valueOf(ws.getEsts())) {
            case "03" -> {
                ws.setString("END-FLG", "1");
            }
            case "00" -> {
                runChain(this::processProductKeyEntry);
            }
            default -> {
                ws.setWkMsgLine("Invalid function key");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: GKEY-010 */
    private void acceptProductWarehouseKeys() {
        runChain(this::resetWorkAreaFields);
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEADER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-FOOTER"), ws);
        ws.setWkMsgLine("Enter product and warehouse");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-KEY"), ws);
        String scValWkKeyProd0 =
                Utility.acceptScreen(
                        "WK-KEY-PROD",
                        () -> renderer.acceptField(ScreenDefs.getInput("WK-KEY-PROD")));
        ws.setWkKeyProd(Utility.parseIntOr(scValWkKeyProd0.trim(), 0));
        String scValWkKeyWhse1 =
                Utility.acceptScreen(
                        "WK-KEY-WHSE",
                        () -> renderer.acceptField(ScreenDefs.getInput("WK-KEY-WHSE")));
        ws.setWkKeyWhse(Utility.parseIntOr(scValWkKeyWhse1.trim(), 0));
        broadcastEstsStatus();
    }

    /** COBOL paragraph: CLRW-010 */
    private void resetWorkAreaFields() {
        fileSet.getStokf().setRecord();
        ws.setWkKeyProd(0);
        ws.setWkKeyWhse(0);
        ws.setWkAdjQty(0);
        ws.setWkNewOnhand(0);
        ws.setWkReason(" ");
        ws.setWkConfirm(" ");
        ws.setWkPrName(" ");
        ws.setWkWhName(" ");
        ws.setStkFlg(0);
    }

    /** COBOL paragraph: PKEY-010 */
    private void processProductKeyEntry() {
        if (ws.getWkKeyProd() == 0) {
            ws.setWkMsgLine("Product code must not be zero");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        runChain(this::validateProductAndWarehouse);
        if ((ws.getErrFlg() == 1)) {
            return;
        }
        runChain(this::readStockOrPrepareNew);
        runChain(this::acceptAdjustmentEntry);
    }

    /** COBOL paragraph: VMST-010 */
    private void validateProductAndWarehouse() {
        ws.setErrFlg(0);
        ws.setPrCode(ws.getWkKeyProd());
        String rkVal_2 = "";
        if (rkVal_2 == null || rkVal_2.trim().isEmpty()) {
            try {
                rkVal_2 = ws.getString("PR-CODE");
            } catch (Exception _e) {
            }
        }
        if (rkVal_2 == null || rkVal_2.trim().isEmpty()) {
            try {
                rkVal_2 = fileSet.getProdf().extractKeyFromCurrentRecord();
            } catch (Exception _e3) {
            }
        }
        fileSet.getProdf().readByKey(rkVal_2 != null ? rkVal_2.trim() : "");
        ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
        if (fileSet.getProdf().isInvalidKey()) {
            setValidationError("Product not found");
            displayMasterError();
            return;
        }
        if (ws.getPrDelFlag() == 1) {
            setValidationError("Product is deleted");
            displayMasterError();
            return;
        }
        ws.setWkPrName(ws.getPrName());
        ws.setWhCode(ws.getWkKeyWhse());
        String rkVal_3 = "";
        if (rkVal_3 == null || rkVal_3.trim().isEmpty()) {
            try {
                rkVal_3 = ws.getString("WH-CODE");
            } catch (Exception _e) {
            }
        }
        if (rkVal_3 == null || rkVal_3.trim().isEmpty()) {
            try {
                rkVal_3 = fileSet.getWhsef().extractKeyFromCurrentRecord();
            } catch (Exception _e3) {
            }
        }
        fileSet.getWhsef().readByKey(rkVal_3 != null ? rkVal_3.trim() : "");
        ws.trySetString("FSTS", fileSet.getWhsef().getFileStatus());
        if (fileSet.getWhsef().isInvalidKey()) {
            setValidationError("Warehouse not found");
            displayMasterError();
            return;
        }
        if (ws.getWhDelFlag() == 1) {
            setValidationError("Warehouse is deleted");
            displayMasterError();
            return;
        }
        ws.setWkWhName(ws.getWhName());
        // fall-through to next paragraph
        displayMasterError();
    }

    /** COBOL paragraph: VMST-999 */
    private void displayMasterError() {
        if ((ws.getErrFlg() == 1)) {
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
        /* CONTINUE */
    }

    /** COBOL paragraph: RSTK-010 */
    private void readStockOrPrepareNew() {
        ws.setSkProd(ws.getWkKeyProd());
        ws.setSkWhse(ws.getWkKeyWhse());
        StringBuilder rkSb_4 = new StringBuilder();
        String rkPart0_4 = "";
        try {
            rkPart0_4 = ws.getString("SK-PROD");
        } catch (Exception _e) {
        }
        rkSb_4.append(rkPart0_4 != null ? rkPart0_4.trim() : "");
        String rkPart1_4 = "";
        try {
            rkPart1_4 = ws.getString("SK-WHSE");
        } catch (Exception _e) {
        }
        rkSb_4.append('|');
        rkSb_4.append(rkPart1_4 != null ? rkPart1_4.trim() : "");
        fileSet.getStokf().readByKey(rkSb_4.toString());
        ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
        if (fileSet.getStokf().isInvalidKey()) {
            runChain(this::initializeNewStockRecord);
        }
        if (!fileSet.getStokf().isInvalidKey()) {
            ws.setString("STK-FLG", "2");
            ws.setWkMsgLine("Stock found - enter adjustment");
        }
    }

    /** COBOL paragraph: SNEW-010 */
    private void initializeNewStockRecord() {
        ws.setString("STK-FLG", "1");
        fileSet.getStokf().setRecord();
        ws.setSkProd(ws.getWkKeyProd());
        ws.setSkWhse(ws.getWkKeyWhse());
        ws.setSkAvgCost(ws.getPrStdCost());
        ws.setWkMsgLine("No stock record - will be created");
    }

    /** COBOL paragraph: EADJ-010 */
    private void acceptAdjustmentEntry() {
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-ADJ"), ws);
        String scValWkAdjQty5 =
                Utility.acceptScreen(
                        "WK-ADJ-QTY",
                        () -> renderer.acceptField(ScreenDefs.getInput("WK-ADJ-QTY")));
        ws.setWkAdjQty(Utility.parseIntOr(scValWkAdjQty5.trim(), 0));
        String scValWkReason6 =
                Utility.acceptScreen(
                        "WK-REASON", () -> renderer.acceptField(ScreenDefs.getInput("WK-REASON")));
        ws.setWkReason(scValWkReason6);
        broadcastEstsStatus();
        switch (String.valueOf(ws.getEsts())) {
            case "03" -> {
                /* CONTINUE */
            }
            case "04" -> {
                ws.setWkMsgLine("Cancelled");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
            case "00" -> {
                runChain(this::validateAdjustmentInput);
                if ((ws.getErrFlg() != 1)) {
                    runChain(this::confirmAndPostAdjustment);
                }
            }
            default -> {
                ws.setWkMsgLine("Invalid function key");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: VADJ-010 */
    private void validateAdjustmentInput() {
        ws.setErrFlg(0);
        if (ws.getWkAdjQty() == 0) {
            setValidationError("Adjustment quantity must not be zero");
            displayAdjustmentError();
            return;
        }
        ws.setWkNewOnhand(ws.getSkOnhand().add(BigDecimal.valueOf(ws.getWkAdjQty())).intValue());
        if (ws.getWkNewOnhand() < 0) {
            setValidationError("Result would be negative - not allowed");
            displayAdjustmentError();
            return;
        }
        if (Utility.fieldEquals(ws.getWkReason(), " ")) {
            setValidationError("Reason is required");
        }
        // fall-through to next paragraph
        displayAdjustmentError();
    }

    /** COBOL paragraph: VADJ-999 */
    private void displayAdjustmentError() {
        if ((ws.getErrFlg() == 1)) {
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
        /* CONTINUE */
    }

    /** COBOL paragraph: CADJ-010 */
    private void confirmAndPostAdjustment() {
        ws.setWkConfirm(" ");
        ws.setWkMsgLine("Confirm the new on-hand (Y/N)");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-CONFIRM"), ws);
        String scValWkConfirm7 =
                Utility.acceptScreen(
                        "SC-CONF", () -> renderer.acceptField(ScreenDefs.getInput("SC-CONF")));
        ws.setWkConfirm(scValWkConfirm7);
        broadcastEstsStatus();
        if ((ws.getWkConfirm().equals("Y") || ws.getWkConfirm().equals("y"))) {
            runChain(this::postStockAdjustment);
        } else {
            ws.setWkMsgLine("Adjustment cancelled");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
    }

    /** COBOL paragraph: PADJ-010 */
    private void postStockAdjustment() {
        ws.setSkOnhand(BigDecimal.valueOf(ws.getWkNewOnhand()));
        if (ws.getWkAdjQty() > 0) {
            ws.setSkLastInDate(ws.getWkSysdate());
            ws.setSkYtdIn(ws.getSkYtdIn().add(BigDecimal.valueOf(ws.getWkAdjQty())));
        } else {
            ws.setSkLastOutDate(ws.getWkSysdate());
            ws.setSkYtdOut(ws.getSkYtdOut().subtract(BigDecimal.valueOf(ws.getWkAdjQty())));
        }
        if ((ws.getStkFlg() == 1)) {
            fileSet.getStokf().write();
            ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
            if (fileSet.getStokf().isInvalidKey()) {
                ws.setWkMsgLine("Stock write failed");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
                return;
            }
        } else {
            fileSet.getStokf().rewrite();
            ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
            if (fileSet.getStokf().isInvalidKey()) {
                ws.setWkMsgLine("Stock update failed");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
                return;
            }
        }
        runChain(this::writeStockMovementRecord);
        ws.setWkMsgLine("Adjustment posted");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
    }

    /** COBOL paragraph: WMOV-010 */
    private void writeStockMovementRecord() {
        ws.setKnumKey("STKMOV");
        numgen(ws.getKnum());
        if (!Utility.fieldEquals(ws.getKnumStatus(), "00")) {
            ws.setWkMsgLine("Movement number assignment failed");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        fileSet.getSmovf().setRecord();
        ws.setSmSeq(ws.getKnumNumber());
        ws.setSmDate(ws.getWkSysdate());
        ws.setSmProd(ws.getSkProd());
        ws.setSmWhse(ws.getSkWhse());
        ws.setSmKind(30);
        ws.setSmQty(BigDecimal.valueOf(ws.getWkAdjQty()));
        ws.setSmUnitCost(ws.getSkAvgCost());
        ws.setSmBalAfter(ws.getSkOnhand());
        ws.setSmRefType(30);
        ws.setSmRefNo(ws.getKnumNumber());
        ws.setSmUser(ws.getWkUserCode());
        fileSet.getSmovf().write();
        ws.trySetString("FSTS", fileSet.getSmovf().getFileStatus());
        if (fileSet.getSmovf().isInvalidKey()) {
            ws.setWkMsgLine("Movement write failed");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
    }

    /** COBOL paragraph: TERM-010 */
    private void closeProgramFiles() {
        closeFileTracked(fileSet.getStokf());
        closeFileTracked(fileSet.getSmovf());
        closeFileTracked(fileSet.getProdf());
        closeFileTracked(fileSet.getWhsef());
    }

    /** COBOL paragraph: ABEND-010 */
    private void abortOnFileOpenError() {
        ws.setKaProgid("IV0020");
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

    /** COBOL CALL NUMGEN — delegates to injected NumgenService. */
    private void numgen(Object... args) {
        NumgenLinkParm params = new NumgenLinkParm();
        params.getKnum().setKnumKey(ws.getKnumKey());
        params.getKnum().setKnumNumber(ws.getKnumNumber());
        params.getKnum().setKnumStatus(ws.getKnumStatus());
        numgenService.execute(params);
        ws.setKnumKey(params.getKnum().getKnumKey());
        ws.setKnumNumber(params.getKnum().getKnumNumber());
        ws.setKnumStatus(params.getKnum().getKnumStatus());
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

    /** Opens a file, recovering via a create-then-reopen retry when the file does not yet exist. */
    private void openFileWithRetry(
            RawDatasetBase file, FileOpenMode mode, String fileName, boolean abendOnFailure) {
        file.open(mode);
        ws.trySetString("FSTS", file.getFileStatus());
        if (Utility.fieldEquals(ws.getFsts(), "35") || Utility.fieldEquals(ws.getFsts(), "30")) {
            file.open(FileOpenMode.OUTPUT);
            ws.trySetString("FSTS", file.getFileStatus());
            file.close();
            ws.trySetString("FSTS", file.getFileStatus());
            file.open(mode);
            ws.trySetString("FSTS", file.getFileStatus());
        }
        if (abendOnFailure && !Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile(fileName);
            runChain(this::abortOnFileOpenError);
        }
    }

    /** Closes a file and records its resulting file status. */
    private void closeFileTracked(RawDatasetBase file) {
        file.close();
        ws.trySetString("FSTS", file.getFileStatus());
    }

    /** Sets the shared error message and marks the current validation as failed. */
    private void setValidationError(String message) {
        ws.setWkMsgLine(message);
        ws.setString("ERR-FLG", "1");
    }
}
