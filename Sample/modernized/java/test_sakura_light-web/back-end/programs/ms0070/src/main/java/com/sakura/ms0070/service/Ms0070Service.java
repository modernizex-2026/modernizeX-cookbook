package com.sakura.ms0070.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.ms0070.domain.Ms0070FieldAccess;
import com.sakura.ms0070.domain.WorkingStorage;
import com.sakura.ms0070.runtime.Ms0070Datasets;
import com.sakura.ms0070.screen.ScreenDefs;
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

import java.math.BigDecimal;

/** Business logic service generated from COBOL program MS0070. */
@Service
@Scope("prototype")
public class Ms0070Service extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final Ms0070Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Ms0070FieldAccess ws;

    public Ms0070Service(
            Ms0070Datasets fileSet,
            DateutService dateutService,
            AbortxService abortxService,
            ScreenRendererInstance renderer) {
        this.fileSet = fileSet;
        this.ws = new Ms0070FieldAccess(new WorkingStorage(), fileSet);
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
            runChain(this::processMainScreenInput);
        }
        runChain(this::closeAllFiles);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeProgram() {
        ws.setWkProgid("MS0070");
        ws.setWkTitle("Customer Price Master Maintenance");
        ws.setWkFkeyLine("ENTER=Read  PF9=Delete  PF3=End");
        ws.setKdFunc("TODY");
        dateut(ws.getKdate());
        ws.setWkSysymd(ws.getKdDate1());
        ws.setWkSysdate(ws.getKdDate1());
        fileSet.getCprcf().open(FileOpenMode.IO);
        ws.trySetString("FSTS", fileSet.getCprcf().getFileStatus());
        if (Utility.fieldEquals(ws.getFsts(), "35") || Utility.fieldEquals(ws.getFsts(), "30")) {
            fileSet.getCprcf().open(FileOpenMode.OUTPUT);
            ws.trySetString("FSTS", fileSet.getCprcf().getFileStatus());
            fileSet.getCprcf().close();
            ws.trySetString("FSTS", fileSet.getCprcf().getFileStatus());
            fileSet.getCprcf().open(FileOpenMode.IO);
            ws.trySetString("FSTS", fileSet.getCprcf().getFileStatus());
        }
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("CPRCF");
            runChain(this::abortOnFileOpenError);
        }
        fileSet.getCustf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("CUSTF");
            runChain(this::abortOnFileOpenError);
        }
        fileSet.getProdf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("PRODF");
            runChain(this::abortOnFileOpenError);
        }
    }

    /** COBOL paragraph: MAIN-RTN-010 */
    private void processMainScreenInput() {
        runChain(this::resetScreenAndWorkArea);
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEADER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-FOOTER"), ws);
        ws.setWkMsgLine(" ");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-KEY"), ws);
        String scValCpCust0 =
                Utility.acceptScreen(
                        "CP-CUST", () -> renderer.acceptField(ScreenDefs.getInput("CP-CUST")));
        ws.setCpCust(Utility.parseIntOr(scValCpCust0.trim(), 0));
        String scValCpProd1 =
                Utility.acceptScreen(
                        "CP-PROD", () -> renderer.acceptField(ScreenDefs.getInput("CP-PROD")));
        ws.setCpProd(Utility.parseIntOr(scValCpProd1.trim(), 0));
        broadcastEstsStatus();
        switch (String.valueOf(ws.getEsts())) {
            case "03" -> {
                ws.setString("END-FLG", "1");
            }
            case "00" -> {
                runChain(this::validateKeysAndLoadRecords);
            }
            default -> {
                ws.setWkMsgLine("Invalid function key");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: CLEAR-010 */
    private void resetScreenAndWorkArea() {
        fileSet.getCprcf().setRecord();
        ws.setWkCustName(" ");
        ws.setWkProdName(" ");
        ws.setWkConfirm(" ");
        ws.setWkSaveCust(0);
        ws.setWkSaveProd(0);
    }

    /** COBOL paragraph: PKEY-010 */
    private void validateKeysAndLoadRecords() {
        if (ws.getCpCust() == 0) {
            ws.setWkMsgLine("Customer code must not be zero");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        if (ws.getCpProd() == 0) {
            ws.setWkMsgLine("Product code must not be zero");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setCuCode(ws.getCpCust());
        readByResolvedKey(fileSet.getCustf(), "CU-CODE");
        if (fileSet.getCustf().isInvalidKey()) {
            ws.setWkMsgLine("Customer code not found");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        if (!fileSet.getCustf().isInvalidKey()) {
            if (ws.getCuDelFlag() == 1) {
                ws.setWkMsgLine("Customer is deleted");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
                return;
            }
            ws.setWkCustName(ws.getCuName());
        }
        ws.setPrCode(ws.getCpProd());
        readByResolvedKey(fileSet.getProdf(), "PR-CODE");
        if (fileSet.getProdf().isInvalidKey()) {
            ws.setWkMsgLine("Product code not found");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        if (!fileSet.getProdf().isInvalidKey()) {
            if (ws.getPrDelFlag() == 1) {
                ws.setWkMsgLine("Product is deleted");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
                return;
            }
            ws.setWkProdName(ws.getPrName());
        }
        ws.setWkSaveCust(ws.getCpCust());
        ws.setWkSaveProd(ws.getCpProd());
        StringBuilder rkSb_4 = new StringBuilder();
        String rkPart0_4 = "";
        try {
            rkPart0_4 = ws.getString("CP-CUST");
        } catch (Exception _e) {
        }
        rkSb_4.append(rkPart0_4 != null ? rkPart0_4.trim() : "");
        String rkPart1_4 = "";
        try {
            rkPart1_4 = ws.getString("CP-PROD");
        } catch (Exception _e) {
        }
        rkSb_4.append('|');
        rkSb_4.append(rkPart1_4 != null ? rkPart1_4.trim() : "");
        fileSet.getCprcf().readByKey(rkSb_4.toString());
        ws.trySetString("FSTS", fileSet.getCprcf().getFileStatus());
        if (fileSet.getCprcf().isInvalidKey()) {
            runChain(this::prepareNewContractPrice);
        }
        if (!fileSet.getCprcf().isInvalidKey()) {
            runChain(this::prepareContractPriceChange);
        }
        runChain(this::acceptAndDispatchPriceEdit);
    }

    /** COBOL paragraph: SADD-010 */
    private void prepareNewContractPrice() {
        ws.setString("MODE-FLG", "1");
        fileSet.getCprcf().setRecord();
        ws.setCpCust(ws.getWkSaveCust());
        ws.setCpProd(ws.getWkSaveProd());
        ws.setCpStartDate(ws.getWkSysdate());
        ws.setWkMsgLine("New contract price - enter details");
    }

    /** COBOL paragraph: SCHG-010 */
    private void prepareContractPriceChange() {
        if (ws.getCpDelFlag() == 1) {
            ws.setString("MODE-FLG", "1");
            ws.setWkMsgLine("Deleted price - re-registering");
        } else {
            ws.setString("MODE-FLG", "2");
            ws.setWkMsgLine("Existing price - change or PF9 delete");
        }
        runChain(this::refreshCustomerAndProductNames);
    }

    /** COBOL paragraph: EDIT-010 */
    private void acceptAndDispatchPriceEdit() {
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-BODY"), ws);
        String scValCpPrice5 =
                Utility.acceptScreen(
                        "CP-PRICE", () -> renderer.acceptField(ScreenDefs.getInput("CP-PRICE")));
        try {
            ws.setCpPrice(new BigDecimal(scValCpPrice5.trim()));
        } catch (NumberFormatException _nfe) {
            ws.setCpPrice(BigDecimal.ZERO);
        }
        String scValCpStartDate6 =
                Utility.acceptScreen(
                        "CP-START-DATE",
                        () -> renderer.acceptField(ScreenDefs.getInput("CP-START-DATE")));
        ws.setCpStartDate(Utility.parseIntOr(scValCpStartDate6.trim(), 0));
        String scValCpEndDate7 =
                Utility.acceptScreen(
                        "CP-END-DATE",
                        () -> renderer.acceptField(ScreenDefs.getInput("CP-END-DATE")));
        ws.setCpEndDate(Utility.parseIntOr(scValCpEndDate7.trim(), 0));
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
                    runChain(this::confirmAndDeleteContractPrice);
                } else {
                    ws.setWkMsgLine("Nothing to delete");
                    renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
                }
            }
            case "00" -> {
                runChain(this::validatePriceAndDates);
                if ((ws.getErrFlg() != 1)) {
                    runChain(this::saveContractPrice);
                }
            }
            default -> {
                ws.setWkMsgLine("Invalid function key");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: VAL-010 */
    private void validatePriceAndDates() {
        ws.setErrFlg(0);
        if ((ws.getCpPrice().signum() < 0)) {
            ws.setWkMsgLine("Price cannot be negative");
            ws.setString("ERR-FLG", "1");
            displayValidationErrorIfAny();
            return;
        }
        if (ws.getCpStartDate() == 0) {
            ws.setWkMsgLine("Start date is required");
            ws.setString("ERR-FLG", "1");
            displayValidationErrorIfAny();
            return;
        }
        ws.setKdFunc("VALD");
        ws.setKdDate1(ws.getCpStartDate());
        dateut(ws.getKdate());
        if (!Utility.fieldEquals(ws.getKdStatus(), "00")) {
            ws.setWkMsgLine("Start date is invalid");
            ws.setString("ERR-FLG", "1");
            displayValidationErrorIfAny();
            return;
        }
        if (ws.getCpEndDate() != 0) {
            ws.setKdFunc("VALD");
            ws.setKdDate1(ws.getCpEndDate());
            dateut(ws.getKdate());
            if (!Utility.fieldEquals(ws.getKdStatus(), "00")) {
                ws.setWkMsgLine("End date is invalid");
                ws.setString("ERR-FLG", "1");
                displayValidationErrorIfAny();
                return;
            }
            if (ws.getCpEndDate() < ws.getCpStartDate()) {
                ws.setWkMsgLine("End date is before start date");
                ws.setString("ERR-FLG", "1");
                displayValidationErrorIfAny();
                return;
            }
        }
        // fall-through to next paragraph
        displayValidationErrorIfAny();
    }

    /** COBOL paragraph: VAL-999 */
    private void displayValidationErrorIfAny() {
        if ((ws.getErrFlg() == 1)) {
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
        /* CONTINUE */
    }

    /** COBOL paragraph: SAVE-010 */
    private void saveContractPrice() {
        ws.setCpDelFlag(0);
        if ((ws.getModeFlg() == 1)) {
            fileSet.getCprcf().write();
            ws.trySetString("FSTS", fileSet.getCprcf().getFileStatus());
            if (fileSet.getCprcf().isInvalidKey()) {
                ws.setWkMsgLine("Write failed - duplicate");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
            if (!fileSet.getCprcf().isInvalidKey()) {
                ws.setWkMsgLine("Contract price added");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        } else {
            fileSet.getCprcf().rewrite();
            ws.trySetString("FSTS", fileSet.getCprcf().getFileStatus());
            if (fileSet.getCprcf().isInvalidKey()) {
                ws.setWkMsgLine("Update failed");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
            if (!fileSet.getCprcf().isInvalidKey()) {
                ws.setWkMsgLine("Contract price updated");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: DEL-010 */
    private void confirmAndDeleteContractPrice() {
        ws.setWkConfirm(" ");
        ws.setWkMsgLine("Press Y then ENTER to delete");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-CONFIRM"), ws);
        String scValWkConfirm8 =
                Utility.acceptScreen(
                        "SC-CONF", () -> renderer.acceptField(ScreenDefs.getInput("SC-CONF")));
        ws.setWkConfirm(scValWkConfirm8);
        broadcastEstsStatus();
        if ((ws.getWkConfirm().equals("Y") || ws.getWkConfirm().equals("y"))) {
            ws.setCpDelFlag(1);
            fileSet.getCprcf().rewrite();
            ws.trySetString("FSTS", fileSet.getCprcf().getFileStatus());
            if (fileSet.getCprcf().isInvalidKey()) {
                ws.setWkMsgLine("Delete failed");
            }
            if (!fileSet.getCprcf().isInvalidKey()) {
                ws.setWkMsgLine("Contract price deleted");
            }
        } else {
            ws.setWkMsgLine("Delete cancelled");
        }
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
    }

    /** COBOL paragraph: LOOK-010 */
    private void refreshCustomerAndProductNames() {
        ws.setWkCustName(" ");
        ws.setCuCode(ws.getCpCust());
        readByResolvedKey(fileSet.getCustf(), "CU-CODE");
        if (fileSet.getCustf().isInvalidKey()) {
            ws.setWkCustName("??? unknown customer");
        }
        if (!fileSet.getCustf().isInvalidKey()) {
            ws.setWkCustName(ws.getCuName());
        }
        ws.setWkProdName(" ");
        ws.setPrCode(ws.getCpProd());
        readByResolvedKey(fileSet.getProdf(), "PR-CODE");
        if (fileSet.getProdf().isInvalidKey()) {
            ws.setWkProdName("??? unknown product");
        }
        if (!fileSet.getProdf().isInvalidKey()) {
            ws.setWkProdName(ws.getPrName());
        }
    }

    /** COBOL paragraph: TERM-010 */
    private void closeAllFiles() {
        fileSet.getCprcf().close();
        ws.trySetString("FSTS", fileSet.getCprcf().getFileStatus());
        fileSet.getCustf().close();
        ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
        fileSet.getProdf().close();
        ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
    }

    /** COBOL paragraph: ABEND-010 */
    private void abortOnFileOpenError() {
        ws.setKaProgid("MS0070");
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
     * Resolve the record key (screen field, falling back to the current record) and read the file
     * by it, mirroring FSTS with the file's status.
     */
    private void readByResolvedKey(RawDatasetBase file, String keyField) {
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
