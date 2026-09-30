package com.sakura.ms0020.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.ms0020.domain.Ms0020FieldAccess;
import com.sakura.ms0020.domain.WorkingStorage;
import com.sakura.ms0020.runtime.Ms0020Datasets;
import com.sakura.ms0020.screen.ScreenDefs;
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

/** Business logic service generated from COBOL program MS0020. */
@Service
@Scope("prototype")
public class Ms0020Service extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final Ms0020Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Ms0020FieldAccess ws;

    public Ms0020Service(
            Ms0020Datasets fileSet,
            DateutService dateutService,
            AbortxService abortxService,
            ScreenRendererInstance renderer) {
        this.fileSet = fileSet;
        this.ws = new Ms0020FieldAccess(new WorkingStorage(), fileSet);
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
        ws.setWkProgid("MS0020");
        ws.setWkTitle("Supplier Master Maintenance");
        ws.setWkFkeyLine("ENTER=Read  PF9=Delete  PF3=End");
        ws.setKdFunc("TODY");
        callDateUtility(ws.getKdate());
        ws.setWkSysymd(ws.getKdDate1());
        ws.setWkSysdate(ws.getKdDate1());
        fileSet.getSuppf().open(FileOpenMode.IO);
        ws.trySetString("FSTS", fileSet.getSuppf().getFileStatus());
        if (Utility.fieldEquals(ws.getFsts(), "35") || Utility.fieldEquals(ws.getFsts(), "30")) {
            fileSet.getSuppf().open(FileOpenMode.OUTPUT);
            ws.trySetString("FSTS", fileSet.getSuppf().getFileStatus());
            fileSet.getSuppf().close();
            ws.trySetString("FSTS", fileSet.getSuppf().getFileStatus());
            fileSet.getSuppf().open(FileOpenMode.IO);
            ws.trySetString("FSTS", fileSet.getSuppf().getFileStatus());
        }
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("SUPPF");
            runChain(this::abortOnFileOpenError);
        }
        fileSet.getBankf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getBankf().getFileStatus());
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("BANKF");
            runChain(this::abortOnFileOpenError);
        }
    }

    /** COBOL paragraph: MAIN-RTN-010 */
    private void processMainScreenInput() {
        runChain(this::clearSupplierWorkFields);
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEADER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-FOOTER"), ws);
        ws.setWkMsgLine(" ");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-KEY"), ws);
        String scValSpCode0 =
                Utility.acceptScreen(
                        "SC-KEY", () -> renderer.acceptField(ScreenDefs.getInput("SC-KEY")));
        ws.setSpCode(Utility.parseIntOr(scValSpCode0.trim(), 0));
        broadcastEstsStatus();
        switch (String.valueOf(ws.getEsts())) {
            case "03" -> {
                ws.setString("END-FLG", "1");
            }
            case "00" -> {
                runChain(this::processSupplierKeyEntry);
            }
            default -> {
                ws.setWkMsgLine("Invalid function key");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: CLEAR-010 */
    private void clearSupplierWorkFields() {
        fileSet.getSuppf().setRecord();
        ws.setWkBankName(" ");
        ws.setWkConfirm(" ");
    }

    /** COBOL paragraph: PKEY-010 */
    private void processSupplierKeyEntry() {
        if (ws.getSpCode() == 0) {
            ws.setWkMsgLine("Supplier code must not be zero");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setWkSaveCode(ws.getSpCode());
        fileSet.getSuppf().readByKey(resolveReadKey("SP-CODE", fileSet.getSuppf()));
        ws.trySetString("FSTS", fileSet.getSuppf().getFileStatus());
        if (fileSet.getSuppf().isInvalidKey()) {
            runChain(this::prepareNewSupplierRecord);
        }
        if (!fileSet.getSuppf().isInvalidKey()) {
            runChain(this::prepareExistingSupplierRecord);
        }
        runChain(this::acceptSupplierDetailFields);
    }

    /** COBOL paragraph: SADD-010 */
    private void prepareNewSupplierRecord() {
        ws.setString("MODE-FLG", "1");
        fileSet.getSuppf().setRecord();
        ws.setSpCode(ws.getWkSaveCode());
        ws.setWkMsgLine("New supplier - enter details");
    }

    /** COBOL paragraph: SCHG-010 */
    private void prepareExistingSupplierRecord() {
        if (ws.getSpDelFlag() == 1) {
            ws.setString("MODE-FLG", "1");
            ws.setWkMsgLine("Deleted supplier - re-registering");
        } else {
            ws.setString("MODE-FLG", "2");
            ws.setWkMsgLine("Existing supplier - change or PF9 delete");
        }
        runChain(this::lookupBankName);
    }

    /** COBOL paragraph: EDIT-010 */
    private void acceptSupplierDetailFields() {
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-BODY"), ws);
        String scValSpName2 =
                Utility.acceptScreen(
                        "SP-NAME", () -> renderer.acceptField(ScreenDefs.getInput("SP-NAME")));
        ws.setSpName(scValSpName2);
        String scValSpKana3 =
                Utility.acceptScreen(
                        "SP-KANA", () -> renderer.acceptField(ScreenDefs.getInput("SP-KANA")));
        ws.setSpKana(scValSpKana3);
        String scValSpZip4 =
                Utility.acceptScreen(
                        "SP-ZIP", () -> renderer.acceptField(ScreenDefs.getInput("SP-ZIP")));
        ws.setSpZip(scValSpZip4);
        String scValSpAddr15 =
                Utility.acceptScreen(
                        "SP-ADDR1", () -> renderer.acceptField(ScreenDefs.getInput("SP-ADDR1")));
        ws.setSpAddr1(scValSpAddr15);
        String scValSpAddr26 =
                Utility.acceptScreen(
                        "SP-ADDR2", () -> renderer.acceptField(ScreenDefs.getInput("SP-ADDR2")));
        ws.setSpAddr2(scValSpAddr26);
        String scValSpTel7 =
                Utility.acceptScreen(
                        "SP-TEL", () -> renderer.acceptField(ScreenDefs.getInput("SP-TEL")));
        ws.setSpTel(scValSpTel7);
        String scValSpFax8 =
                Utility.acceptScreen(
                        "SP-FAX", () -> renderer.acceptField(ScreenDefs.getInput("SP-FAX")));
        ws.setSpFax(scValSpFax8);
        String scValSpCloseDay9 =
                Utility.acceptScreen(
                        "SP-CLOSE-DAY",
                        () -> renderer.acceptField(ScreenDefs.getInput("SP-CLOSE-DAY")));
        ws.setSpCloseDay(Utility.parseIntOr(scValSpCloseDay9.trim(), 0));
        String scValSpPayMonth10 =
                Utility.acceptScreen(
                        "SP-PAY-MONTH",
                        () -> renderer.acceptField(ScreenDefs.getInput("SP-PAY-MONTH")));
        ws.setSpPayMonth(Utility.parseIntOr(scValSpPayMonth10.trim(), 0));
        String scValSpPayDay11 =
                Utility.acceptScreen(
                        "SP-PAY-DAY",
                        () -> renderer.acceptField(ScreenDefs.getInput("SP-PAY-DAY")));
        ws.setSpPayDay(Utility.parseIntOr(scValSpPayDay11.trim(), 0));
        String scValSpPayMethod12 =
                Utility.acceptScreen(
                        "SP-PAY-METHOD",
                        () -> renderer.acceptField(ScreenDefs.getInput("SP-PAY-METHOD")));
        ws.setSpPayMethod(Utility.parseIntOr(scValSpPayMethod12.trim(), 0));
        String scValSpTaxType13 =
                Utility.acceptScreen(
                        "SP-TAX-TYPE",
                        () -> renderer.acceptField(ScreenDefs.getInput("SP-TAX-TYPE")));
        ws.setSpTaxType(Utility.parseIntOr(scValSpTaxType13.trim(), 0));
        String scValSpBankCode14 =
                Utility.acceptScreen(
                        "SP-BANK-CODE",
                        () -> renderer.acceptField(ScreenDefs.getInput("SP-BANK-CODE")));
        ws.setSpBankCode(Utility.parseIntOr(scValSpBankCode14.trim(), 0));
        String scValSpBankAcct15 =
                Utility.acceptScreen(
                        "SP-BANK-ACCT",
                        () -> renderer.acceptField(ScreenDefs.getInput("SP-BANK-ACCT")));
        ws.setSpBankAcct(scValSpBankAcct15);
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
                    runChain(this::deleteSupplierWithConfirmation);
                } else {
                    ws.setWkMsgLine("Nothing to delete");
                    renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
                }
            }
            case "00" -> {
                runChain(this::validateSupplierFields);
                if ((ws.getErrFlg() != 1)) {
                    runChain(this::saveSupplierRecord);
                }
            }
            default -> {
                ws.setWkMsgLine("Invalid function key");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: VAL-010 */
    private void validateSupplierFields() {
        ws.setErrFlg(0);
        if (Utility.fieldEquals(ws.getSpName(), " ")) {
            ws.setWkMsgLine("Name is required");
            ws.setString("ERR-FLG", "1");
            showValidationMessageIfError();
            return;
        }
        if (ws.getSpCloseDay() != 99) {
            if (ws.getSpCloseDay() < 1 || ws.getSpCloseDay() > 31) {
                ws.setWkMsgLine("Closing day must be 1-31 or 99");
                ws.setString("ERR-FLG", "1");
                showValidationMessageIfError();
                return;
            }
        }
        if (ws.getSpPayDay() != 99) {
            if (ws.getSpPayDay() < 1 || ws.getSpPayDay() > 31) {
                ws.setWkMsgLine("Pay day must be 1-31 or 99");
                ws.setString("ERR-FLG", "1");
                showValidationMessageIfError();
                return;
            }
        }
        if (ws.getSpPayMethod() < 1 || ws.getSpPayMethod() > 3) {
            ws.setWkMsgLine("Pay method must be 1-3");
            ws.setString("ERR-FLG", "1");
            showValidationMessageIfError();
            return;
        }
        if (ws.getSpTaxType() < 1 || ws.getSpTaxType() > 3) {
            ws.setWkMsgLine("Tax type must be 1-3");
            ws.setString("ERR-FLG", "1");
            showValidationMessageIfError();
            return;
        }
        if (ws.getSpBankCode() != 0) {
            ws.setBkCode(ws.getSpBankCode());
            fileSet.getBankf().readByKey(resolveReadKey("BK-CODE", fileSet.getBankf()));
            ws.trySetString("FSTS", fileSet.getBankf().getFileStatus());
            if (fileSet.getBankf().isInvalidKey()) {
                ws.setWkMsgLine("Bank code not found");
                ws.setString("ERR-FLG", "1");
                showValidationMessageIfError();
                return;
            }
            if (!fileSet.getBankf().isInvalidKey()) {
                ws.setWkBankName(ws.getBkName());
            }
        }
        // fall-through to next paragraph
        showValidationMessageIfError();
    }

    /** COBOL paragraph: VAL-999 */
    private void showValidationMessageIfError() {
        if ((ws.getErrFlg() == 1)) {
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
        /* CONTINUE */
    }

    /** COBOL paragraph: SAVE-010 */
    private void saveSupplierRecord() {
        ws.setSpUpdDate(ws.getWkSysdate());
        ws.setSpUpdUser(ws.getWkUserCode());
        ws.setSpDelFlag(0);
        if ((ws.getModeFlg() == 1)) {
            ws.setSpAddDate(ws.getWkSysdate());
            ws.setSpAddUser(ws.getWkUserCode());
            fileSet.getSuppf().write();
            reportSuppfSaveResult("Write failed - duplicate", "Supplier added");
        } else {
            fileSet.getSuppf().rewrite();
            reportSuppfSaveResult("Update failed", "Supplier updated");
        }
    }

    /** Check FSTS and show the write/rewrite outcome message for SUPPF. */
    private void reportSuppfSaveResult(String failMsg, String okMsg) {
        ws.trySetString("FSTS", fileSet.getSuppf().getFileStatus());
        if (fileSet.getSuppf().isInvalidKey()) {
            ws.setWkMsgLine(failMsg);
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
        if (!fileSet.getSuppf().isInvalidKey()) {
            ws.setWkMsgLine(okMsg);
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
    }

    /** COBOL paragraph: DEL-010 */
    private void deleteSupplierWithConfirmation() {
        ws.setWkConfirm(" ");
        ws.setWkMsgLine("Press Y then ENTER to delete");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-CONFIRM"), ws);
        String scValWkConfirm17 =
                Utility.acceptScreen(
                        "SC-CONF", () -> renderer.acceptField(ScreenDefs.getInput("SC-CONF")));
        ws.setWkConfirm(scValWkConfirm17);
        broadcastEstsStatus();
        if ((ws.getWkConfirm().equals("Y") || ws.getWkConfirm().equals("y"))) {
            ws.setSpDelFlag(1);
            ws.setSpUpdDate(ws.getWkSysdate());
            ws.setSpUpdUser(ws.getWkUserCode());
            fileSet.getSuppf().rewrite();
            ws.trySetString("FSTS", fileSet.getSuppf().getFileStatus());
            if (fileSet.getSuppf().isInvalidKey()) {
                ws.setWkMsgLine("Delete failed");
            }
            if (!fileSet.getSuppf().isInvalidKey()) {
                ws.setWkMsgLine("Supplier deleted");
            }
        } else {
            ws.setWkMsgLine("Delete cancelled");
        }
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
    }

    /** COBOL paragraph: LOOK-010 */
    private void lookupBankName() {
        ws.setWkBankName(" ");
        if (ws.getSpBankCode() != 0) {
            ws.setBkCode(ws.getSpBankCode());
            fileSet.getBankf().readByKey(resolveReadKey("BK-CODE", fileSet.getBankf()));
            ws.trySetString("FSTS", fileSet.getBankf().getFileStatus());
            if (fileSet.getBankf().isInvalidKey()) {
                ws.setWkBankName("??? unknown bank");
            }
            if (!fileSet.getBankf().isInvalidKey()) {
                ws.setWkBankName(ws.getBkName());
            }
        }
    }

    /** COBOL paragraph: TERM-010 */
    private void closeAllFiles() {
        fileSet.getSuppf().close();
        ws.trySetString("FSTS", fileSet.getSuppf().getFileStatus());
        fileSet.getBankf().close();
        ws.trySetString("FSTS", fileSet.getBankf().getFileStatus());
    }

    /** COBOL paragraph: ABEND-010 */
    private void abortOnFileOpenError() {
        ws.setKaProgid("MS0020");
        ws.setKaFsts(ws.getFsts());
        ws.setKaMsgcode("EOPEN ");
        ws.setKaDetail("File open error");
        callAbortxUtility(ws.getKabend());
        ws.setCompletionCode(255);
        throw new ProgramExitSignal();
    }

    /** COBOL CALL DATEUT — delegates to injected DateutService. */
    private void callDateUtility(Object... args) {
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
    private void callAbortxUtility(Object... args) {
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

    /** Resolve the read key from the screen field, falling back to the current record's key. */
    private String resolveReadKey(String fieldName, RawDatasetBase file) {
        String key = "";
        if (key == null || key.trim().isEmpty()) {
            try {
                key = ws.getString(fieldName);
            } catch (Exception _e) {
            }
        }
        if (key == null || key.trim().isEmpty()) {
            try {
                key = file.extractKeyFromCurrentRecord();
            } catch (Exception _e3) {
            }
        }
        return key != null ? key.trim() : "";
    }
}
