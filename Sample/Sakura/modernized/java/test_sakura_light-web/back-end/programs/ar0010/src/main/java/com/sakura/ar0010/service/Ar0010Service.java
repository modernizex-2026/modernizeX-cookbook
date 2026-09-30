package com.sakura.ar0010.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.ar0010.domain.Ar0010FieldAccess;
import com.sakura.ar0010.domain.WorkingStorage;
import com.sakura.ar0010.runtime.Ar0010Datasets;
import com.sakura.ar0010.screen.ScreenDefs;
import com.sakura.dateut.service.DateutService;
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

/** Business logic service generated from COBOL program AR0010. */
@Service
@Scope("prototype")
public class Ar0010Service extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final Ar0010Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL NUMGEN. */
    private NumgenService numgenService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Ar0010FieldAccess ws;

    public Ar0010Service(
            Ar0010Datasets fileSet,
            DateutService dateutService,
            NumgenService numgenService,
            AbortxService abortxService,
            ScreenRendererInstance renderer) {
        this.fileSet = fileSet;
        this.ws = new Ar0010FieldAccess(new WorkingStorage(), fileSet);
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
            runChain(this::runReceiptEntryCycle);
        }
        runChain(this::closeAllFiles);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeProgram() {
        ws.setWkProgid("AR0010");
        ws.setWkTitle("Cash Receipt Entry");
        ws.setWkFkeyLine("ENTER=Confirm  PF3=End  PF4=Clear");
        ws.setKdFunc("TODY");
        dateut(ws.getKdate());
        ws.setWkSysymd(ws.getKdDate1());
        ws.setWkSysdate(ws.getKdDate1());
        runChain(this::openAllFiles);
    }

    /** COBOL paragraph: OPENF-010 */
    private void openAllFiles() {
        openFileWithRetry(fileSet.getRcptf(), FileOpenMode.IO);
        abortIfOpenFailed("RCPTF");
        openFileWithRetry(fileSet.getArlf(), FileOpenMode.IO);
        abortIfOpenFailed("ARLF");
        openFileWithRetry(fileSet.getCustf(), FileOpenMode.IO);
        abortIfOpenFailed("CUSTF");
        openFileWithRetry(fileSet.getBankf(), FileOpenMode.INPUT);
    }

    /** COBOL paragraph: RLUP-010 */
    private void runReceiptEntryCycle() {
        runChain(this::resetReceiptWorkArea);
        runChain(this::displayAndAcceptHeaderFields);
        if ((ws.getEndFlg() == 1)) {
            return;
        }
        if ((ws.getHdrOk() == 1)) {
            runChain(this::confirmAndSaveReceipt);
        }
    }

    /** COBOL paragraph: CLR-010 */
    private void resetReceiptWorkArea() {
        fileSet.getRcptf().setRecord();
        ws.setHdrOk(0);
        ws.setWkCurBal(0);
        ws.setWkNewBal(0);
        ws.setWkReNoD(0);
        ws.setWkCloseYm(0);
        ws.setWkCustName(" ");
        ws.setWkBankName(" ");
        ws.setWkConfirm(" ");
        ws.setReDate(ws.getWkSysdate());
        ws.setReMethod(1);
    }

    /** COBOL paragraph: EHDR-010 */
    private void displayAndAcceptHeaderFields() {
        ws.setHdrOk(0);
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEADER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-FOOTER"), ws);
        ws.setWkMsgLine("Enter receipt details - PF3 to end");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEAD"), ws);
        String scValReDate0 =
                Utility.acceptScreen(
                        "RE-DATE", () -> renderer.acceptField(ScreenDefs.getInput("RE-DATE")));
        ws.setReDate(Utility.parseIntOr(scValReDate0.trim(), 0));
        String scValReCust1 =
                Utility.acceptScreen(
                        "RE-CUST", () -> renderer.acceptField(ScreenDefs.getInput("RE-CUST")));
        ws.setReCust(Utility.parseIntOr(scValReCust1.trim(), 0));
        String scValReMethod2 =
                Utility.acceptScreen(
                        "RE-METHOD", () -> renderer.acceptField(ScreenDefs.getInput("RE-METHOD")));
        ws.setReMethod(Utility.parseIntOr(scValReMethod2.trim(), 0));
        String scValReAmount3 =
                Utility.acceptScreen(
                        "RE-AMOUNT", () -> renderer.acceptField(ScreenDefs.getInput("RE-AMOUNT")));
        try {
            ws.setReAmount(new BigDecimal(scValReAmount3.trim()));
        } catch (NumberFormatException _nfe) {
            ws.setReAmount(BigDecimal.ZERO);
        }
        String scValReBankCode4 =
                Utility.acceptScreen(
                        "RE-BANK-CODE",
                        () -> renderer.acceptField(ScreenDefs.getInput("RE-BANK-CODE")));
        ws.setReBankCode(Utility.parseIntOr(scValReBankCode4.trim(), 0));
        String scValReRemark5 =
                Utility.acceptScreen(
                        "RE-REMARK", () -> renderer.acceptField(ScreenDefs.getInput("RE-REMARK")));
        ws.setReRemark(scValReRemark5);
        broadcastEstsStatus();
        if (Utility.fieldEquals(ws.getEsts(), "03")) {
            ws.setString("END-FLG", "1");
            return;
        }
        if (Utility.fieldEquals(ws.getEsts(), "04")) {
            ws.setWkMsgLine("Cleared");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        runChain(this::validateHeaderFields);
    }

    /** COBOL paragraph: VHDR-010 */
    private void validateHeaderFields() {
        ws.setErrFlg(0);
        ws.setKdFunc("VALD");
        ws.setKdDate1(ws.getReDate());
        dateut(ws.getKdate());
        if (!Utility.fieldEquals(ws.getKdStatus(), "00")) {
            failHeaderValidation("Receipt date is invalid");
            return;
        }
        if (ws.getReCust() == 0) {
            failHeaderValidation("Customer code required");
            return;
        }
        ws.setCuCode(ws.getReCust());
        readByKeyField(fileSet.getCustf(), "CU-CODE");
        if (fileSet.getCustf().isInvalidKey()) {
            failHeaderValidation("Customer not found");
            return;
        }
        if (ws.getCuDelFlag() == 1) {
            failHeaderValidation("Customer is deleted");
            return;
        }
        ws.setWkCustName(ws.getCuName());
        ws.setWkCurBal(ws.getCuBalance().longValue());
        if ((ws.getReAmount().signum() <= 0)) {
            failHeaderValidation("Amount must be positive");
            return;
        }
        if (ws.getReMethod() < 1 || ws.getReMethod() > 4) {
            failHeaderValidation("Method must be 1 to 4");
            return;
        }
        runChain(this::validateBankFields);
        if ((ws.getErrFlg() == 1)) {
            displayHeaderErrorIfPresent();
            return;
        }
        ws.setWkNewBal(ws.getCuBalance().subtract(ws.getReAmount()).longValue());
        ws.setHdrOk(1);
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEAD"), ws);
        ws.setWkMsgLine("Details OK - press ENTER to confirm");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        displayHeaderErrorIfPresent();
    }

    /** COBOL paragraph: VHDR-999 */
    private void displayHeaderErrorIfPresent() {
        if ((ws.getErrFlg() == 1)) {
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
        /* CONTINUE */
    }

    /** COBOL paragraph: VBNK-010 */
    private void validateBankFields() {
        ws.setWkBankName(" ");
        if (ws.getReBankCode() == 0) {
            if (ws.getReMethod() == 2) {
                setErrorMessage("Bank code required for transfer");
            }
            return;
        }
        ws.setBkCode(ws.getReBankCode());
        readByKeyField(fileSet.getBankf(), "BK-CODE");
        if (fileSet.getBankf().isInvalidKey()) {
            setErrorMessage("Bank code not found");
            return;
        }
        if (ws.getBkDelFlag() == 1) {
            setErrorMessage("Bank is deleted");
            return;
        }
        ws.setWkBankName(ws.getBkName());
    }

    /** COBOL paragraph: CSAV-010 */
    private void confirmAndSaveReceipt() {
        ws.setWkConfirm(" ");
        ws.setWkMsgLine("Confirm to post the receipt");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-CONFIRM"), ws);
        String scValWkConfirm8 =
                Utility.acceptScreen(
                        "SC-CONF", () -> renderer.acceptField(ScreenDefs.getInput("SC-CONF")));
        ws.setWkConfirm(scValWkConfirm8);
        broadcastEstsStatus();
        if ((ws.getWkConfirm().equals("Y") || ws.getWkConfirm().equals("y"))) {
            runChain(this::saveReceiptRecord);
        } else {
            ws.setWkMsgLine("Receipt discarded");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
    }

    /** COBOL paragraph: SAV-010 */
    private void saveReceiptRecord() {
        ws.setKnumKey("RECEIPT");
        numgen(ws.getKnum());
        if (!Utility.fieldEquals(ws.getKnumStatus(), "00")) {
            ws.setWkMsgLine("Receipt number assignment failed");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setReNo(ws.getKnumNumber());
        ws.setWkReNoD(ws.getKnumNumber());
        ws.setWkCloseYm((ws.getReDate() / 100));
        ws.setReCloseYm(ws.getWkCloseYm());
        ws.setReStatus(0);
        ws.setReDelFlag(0);
        ws.setReAddDate(ws.getWkSysdate());
        ws.setReAddUser(ws.getWkUserCode());
        fileSet.getRcptf().write();
        ws.trySetString("FSTS", fileSet.getRcptf().getFileStatus());
        if (fileSet.getRcptf().isInvalidKey()) {
            ws.setWkMsgLine("Receipt write failed");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        runChain(this::postLedgerEntry);
        runChain(this::updateCustomerBalance);
        ws.setWkSessCnt(ws.getWkSessCnt() + 1);
        ws.setWkSessAmt((BigDecimal.valueOf(ws.getWkSessAmt()).add(ws.getReAmount())).longValue());
        ws.setWkMsgLine("Receipt posted");
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEAD"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
    }

    /** COBOL paragraph: UCB-010 */
    private void updateCustomerBalance() {
        ws.setCuCode(ws.getReCust());
        readByKeyField(fileSet.getCustf(), "CU-CODE");
        if (fileSet.getCustf().isInvalidKey()) {
            ws.setWkMsgLine("Customer balance update failed");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setCuBalance(ws.getCuBalance().subtract(ws.getReAmount()));
        ws.setCuUpdDate(ws.getWkSysdate());
        ws.setCuUpdUser(ws.getWkUserCode());
        fileSet.getCustf().rewrite();
        ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
        if (fileSet.getCustf().isInvalidKey()) {
            ws.setWkMsgLine("Customer balance update failed");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
    }

    /** COBOL paragraph: PLG-010 */
    private void postLedgerEntry() {
        ws.setKnumKey("ARLDG");
        numgen(ws.getKnum());
        if (!Utility.fieldEquals(ws.getKnumStatus(), "00")) {
            ws.setWkMsgLine("Ledger number assignment failed");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        fileSet.getArlf().setRecord();
        ws.setAlSeq(ws.getKnumNumber());
        ws.setAlCust(ws.getReCust());
        ws.setAlDate(ws.getReDate());
        ws.setAlCloseYm(ws.getWkCloseYm());
        ws.setAlKind(2);
        ws.setAlRefType(ws.getWkRefRcpt());
        ws.setAlRefNo(ws.getReNo());
        ws.setAlDebit(BigDecimal.ZERO);
        ws.setAlCredit(ws.getReAmount());
        ws.setAlBalance(BigDecimal.valueOf(ws.getWkNewBal()));
        ws.setAlRemark(ws.getReRemark());
        ws.setAlUser(ws.getWkUserCode());
        fileSet.getArlf().write();
        ws.trySetString("FSTS", fileSet.getArlf().getFileStatus());
        if (fileSet.getArlf().isInvalidKey()) {
            ws.setWkMsgLine("Ledger write failed");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
    }

    /** COBOL paragraph: TERM-010 */
    private void closeAllFiles() {
        fileSet.getRcptf().close();
        ws.trySetString("FSTS", fileSet.getRcptf().getFileStatus());
        fileSet.getArlf().close();
        ws.trySetString("FSTS", fileSet.getArlf().getFileStatus());
        fileSet.getCustf().close();
        ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
        fileSet.getBankf().close();
        ws.trySetString("FSTS", fileSet.getBankf().getFileStatus());
    }

    /** COBOL paragraph: ABEND-010 */
    private void abortOnFileOpenError() {
        ws.setKaProgid("AR0010");
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

    /**
     * Opens a dataset in the given mode, recreating it via OUTPUT when it has not yet been
     * initialized.
     */
    private void openFileWithRetry(RawDatasetBase file, FileOpenMode mode) {
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
    }

    /** Aborts the program via ABEND-010 when the preceding file open did not succeed. */
    private void abortIfOpenFailed(String fileName) {
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile(fileName);
            runChain(this::abortOnFileOpenError);
        }
    }

    /**
     * Reads a dataset record by its key field, falling back to the current record's key when unset.
     */
    private void readByKeyField(RawDatasetBase file, String keyFieldName) {
        String keyValue = "";
        if (keyValue == null || keyValue.trim().isEmpty()) {
            try {
                keyValue = ws.getString(keyFieldName);
            } catch (Exception _e) {
            }
        }
        if (keyValue == null || keyValue.trim().isEmpty()) {
            try {
                keyValue = file.extractKeyFromCurrentRecord();
            } catch (Exception _e3) {
            }
        }
        file.readByKey(keyValue != null ? keyValue.trim() : "");
        ws.trySetString("FSTS", file.getFileStatus());
    }

    /** Sets the screen message line and raises the header/bank error flag. */
    private void setErrorMessage(String message) {
        ws.setWkMsgLine(message);
        ws.setString("ERR-FLG", "1");
    }

    /** Reports a header-validation failure and triggers the VHDR-999 error display. */
    private void failHeaderValidation(String message) {
        setErrorMessage(message);
        displayHeaderErrorIfPresent();
    }
}
