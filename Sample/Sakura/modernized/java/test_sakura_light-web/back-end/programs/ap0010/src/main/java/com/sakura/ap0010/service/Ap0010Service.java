package com.sakura.ap0010.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.ap0010.domain.Ap0010FieldAccess;
import com.sakura.ap0010.domain.WorkingStorage;
import com.sakura.ap0010.runtime.Ap0010Datasets;
import com.sakura.ap0010.screen.ScreenDefs;
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

/** Business logic service generated from COBOL program AP0010. */
@Service
@Scope("prototype")
public class Ap0010Service extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final Ap0010Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL NUMGEN. */
    private NumgenService numgenService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Ap0010FieldAccess ws;

    public Ap0010Service(
            Ap0010Datasets fileSet,
            DateutService dateutService,
            NumgenService numgenService,
            AbortxService abortxService,
            ScreenRendererInstance renderer) {
        this.fileSet = fileSet;
        this.ws = new Ap0010FieldAccess(new WorkingStorage(), fileSet);
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
            runChain(this::processOnePaymentEntry);
        }
        runChain(this::closeAllFiles);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeProgram() {
        ws.setWkProgid("AP0010");
        ws.setWkTitle("Payment Entry");
        ws.setWkFkeyLine("ENTER=Confirm  PF3=End  PF4=Clear");
        ws.setKdFunc("TODY");
        dateut(ws.getKdate());
        ws.setWkSysymd(ws.getKdDate1());
        ws.setWkSysdate(ws.getKdDate1());
        runChain(this::openAllFiles);
    }

    /** COBOL paragraph: OPENF-010 */
    private void openAllFiles() {
        openFileWithRetry(fileSet.getPayf(), FileOpenMode.IO);
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("PAYF");
            runChain(this::abortOnFileOpenError);
        }
        openFileWithRetry(fileSet.getAplf(), FileOpenMode.IO);
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("APLF");
            runChain(this::abortOnFileOpenError);
        }
        openFileWithRetry(fileSet.getSuppf(), FileOpenMode.IO);
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("SUPPF");
            runChain(this::abortOnFileOpenError);
        }
        openFileWithRetry(fileSet.getBankf(), FileOpenMode.INPUT);
    }

    /** COBOL paragraph: PLUP-010 */
    private void processOnePaymentEntry() {
        runChain(this::clearEntryFields);
        runChain(this::acceptPaymentHeaderFields);
        if ((ws.getEndFlg() == 1)) {
            return;
        }
        if ((ws.getHdrOk() == 1)) {
            runChain(this::confirmAndSavePayment);
        }
    }

    /** COBOL paragraph: CLR-010 */
    private void clearEntryFields() {
        fileSet.getPayf().setRecord();
        ws.setHdrOk(0);
        ws.setWkCurBal(0);
        ws.setWkNewBal(0);
        ws.setWkPyNoD(0);
        ws.setWkCloseYm(0);
        ws.setWkSuppName(" ");
        ws.setWkBankName(" ");
        ws.setWkConfirm(" ");
        ws.setPyDate(ws.getWkSysdate());
        ws.setPyMethod(1);
    }

    /** COBOL paragraph: EHDR-010 */
    private void acceptPaymentHeaderFields() {
        ws.setHdrOk(0);
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEADER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-FOOTER"), ws);
        ws.setWkMsgLine("Enter payment details - PF3 to end");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEAD"), ws);
        String scValPyDate0 =
                Utility.acceptScreen(
                        "PY-DATE", () -> renderer.acceptField(ScreenDefs.getInput("PY-DATE")));
        ws.setPyDate(Utility.parseIntOr(scValPyDate0.trim(), 0));
        String scValPySupp1 =
                Utility.acceptScreen(
                        "PY-SUPP", () -> renderer.acceptField(ScreenDefs.getInput("PY-SUPP")));
        ws.setPySupp(Utility.parseIntOr(scValPySupp1.trim(), 0));
        String scValPyMethod2 =
                Utility.acceptScreen(
                        "PY-METHOD", () -> renderer.acceptField(ScreenDefs.getInput("PY-METHOD")));
        ws.setPyMethod(Utility.parseIntOr(scValPyMethod2.trim(), 0));
        String scValPyAmount3 =
                Utility.acceptScreen(
                        "PY-AMOUNT", () -> renderer.acceptField(ScreenDefs.getInput("PY-AMOUNT")));
        try {
            ws.setPyAmount(new BigDecimal(scValPyAmount3.trim()));
        } catch (NumberFormatException _nfe) {
            ws.setPyAmount(BigDecimal.ZERO);
        }
        String scValPyBankCode4 =
                Utility.acceptScreen(
                        "PY-BANK-CODE",
                        () -> renderer.acceptField(ScreenDefs.getInput("PY-BANK-CODE")));
        ws.setPyBankCode(Utility.parseIntOr(scValPyBankCode4.trim(), 0));
        String scValPyRemark5 =
                Utility.acceptScreen(
                        "PY-REMARK", () -> renderer.acceptField(ScreenDefs.getInput("PY-REMARK")));
        ws.setPyRemark(scValPyRemark5);
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
        runChain(this::validatePaymentHeader);
    }

    /** COBOL paragraph: VHDR-010 */
    private void validatePaymentHeader() {
        ws.setErrFlg(0);
        ws.setKdFunc("VALD");
        ws.setKdDate1(ws.getPyDate());
        dateut(ws.getKdate());
        if (!Utility.fieldEquals(ws.getKdStatus(), "00")) {
            ws.setWkMsgLine("Payment date is invalid");
            ws.setString("ERR-FLG", "1");
            redisplayErrorMessage();
            return;
        }
        if (ws.getPySupp() == 0) {
            ws.setWkMsgLine("Supplier code required");
            ws.setString("ERR-FLG", "1");
            redisplayErrorMessage();
            return;
        }
        ws.setSpCode(ws.getPySupp());
        readByKeyWithFallback(fileSet.getSuppf(), "SP-CODE");
        if (fileSet.getSuppf().isInvalidKey()) {
            ws.setWkMsgLine("Supplier not found");
            ws.setString("ERR-FLG", "1");
            redisplayErrorMessage();
            return;
        }
        if (ws.getSpDelFlag() == 1) {
            ws.setWkMsgLine("Supplier is deleted");
            ws.setString("ERR-FLG", "1");
            redisplayErrorMessage();
            return;
        }
        ws.setWkSuppName(ws.getSpName());
        ws.setWkCurBal(ws.getSpBalance().longValue());
        if ((ws.getPyAmount().signum() <= 0)) {
            ws.setWkMsgLine("Amount must be positive");
            ws.setString("ERR-FLG", "1");
            redisplayErrorMessage();
            return;
        }
        if (ws.getPyMethod() < 1 || ws.getPyMethod() > 4) {
            ws.setWkMsgLine("Method must be 1 to 4");
            ws.setString("ERR-FLG", "1");
            redisplayErrorMessage();
            return;
        }
        runChain(this::validateBankCode);
        if ((ws.getErrFlg() == 1)) {
            redisplayErrorMessage();
            return;
        }
        ws.setWkNewBal(ws.getSpBalance().subtract(ws.getPyAmount()).longValue());
        ws.setHdrOk(1);
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEAD"), ws);
        ws.setWkMsgLine("Details OK - press ENTER to confirm");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        // fall-through to next paragraph
        redisplayErrorMessage();
    }

    /** COBOL paragraph: VHDR-999 */
    private void redisplayErrorMessage() {
        if ((ws.getErrFlg() == 1)) {
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
        /* CONTINUE */
    }

    /** COBOL paragraph: VBNK-010 */
    private void validateBankCode() {
        ws.setWkBankName(" ");
        if (ws.getPyBankCode() == 0) {
            if (ws.getPyMethod() == 2) {
                ws.setWkMsgLine("Bank code required for transfer");
                ws.setString("ERR-FLG", "1");
            }
            return;
        }
        ws.setBkCode(ws.getPyBankCode());
        readByKeyWithFallback(fileSet.getBankf(), "BK-CODE");
        if (fileSet.getBankf().isInvalidKey()) {
            ws.setWkMsgLine("Bank code not found");
            ws.setString("ERR-FLG", "1");
            return;
        }
        if (ws.getBkDelFlag() == 1) {
            ws.setWkMsgLine("Bank is deleted");
            ws.setString("ERR-FLG", "1");
            return;
        }
        ws.setWkBankName(ws.getBkName());
    }

    /** COBOL paragraph: CSAV-010 */
    private void confirmAndSavePayment() {
        ws.setWkConfirm(" ");
        ws.setWkMsgLine("Confirm to post the payment");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-CONFIRM"), ws);
        String scValWkConfirm8 =
                Utility.acceptScreen(
                        "SC-CONF", () -> renderer.acceptField(ScreenDefs.getInput("SC-CONF")));
        ws.setWkConfirm(scValWkConfirm8);
        broadcastEstsStatus();
        if ((ws.getWkConfirm().equals("Y") || ws.getWkConfirm().equals("y"))) {
            runChain(this::savePaymentRecord);
        } else {
            ws.setWkMsgLine("Payment discarded");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
    }

    /** COBOL paragraph: SAV-010 */
    private void savePaymentRecord() {
        ws.setKnumKey("PAYMENT");
        numgen(ws.getKnum());
        if (!Utility.fieldEquals(ws.getKnumStatus(), "00")) {
            ws.setWkMsgLine("Payment number assignment failed");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setPyNo(ws.getKnumNumber());
        ws.setWkPyNoD(ws.getKnumNumber());
        ws.setWkCloseYm((ws.getPyDate() / 100));
        ws.setPyCloseYm(ws.getWkCloseYm());
        ws.setPyStatus(0);
        ws.setPyDelFlag(0);
        ws.setPyAddDate(ws.getWkSysdate());
        ws.setPyAddUser(ws.getWkUserCode());
        fileSet.getPayf().write();
        ws.trySetString("FSTS", fileSet.getPayf().getFileStatus());
        if (fileSet.getPayf().isInvalidKey()) {
            ws.setWkMsgLine("Payment write failed");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        runChain(this::writeLedgerEntry);
        runChain(this::updateSupplierBalance);
        ws.setWkSessCnt(ws.getWkSessCnt() + 1);
        ws.setWkSessAmt((BigDecimal.valueOf(ws.getWkSessAmt()).add(ws.getPyAmount())).longValue());
        ws.setWkMsgLine("Payment posted");
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEAD"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
    }

    /** COBOL paragraph: USB-010 */
    private void updateSupplierBalance() {
        ws.setSpCode(ws.getPySupp());
        readByKeyWithFallback(fileSet.getSuppf(), "SP-CODE");
        if (fileSet.getSuppf().isInvalidKey()) {
            ws.setWkMsgLine("Supplier balance update failed");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setSpBalance(ws.getSpBalance().subtract(ws.getPyAmount()));
        ws.setSpUpdDate(ws.getWkSysdate());
        ws.setSpUpdUser(ws.getWkUserCode());
        fileSet.getSuppf().rewrite();
        ws.trySetString("FSTS", fileSet.getSuppf().getFileStatus());
        if (fileSet.getSuppf().isInvalidKey()) {
            ws.setWkMsgLine("Supplier balance update failed");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
    }

    /** COBOL paragraph: PLG-010 */
    private void writeLedgerEntry() {
        ws.setKnumKey("APLDG");
        numgen(ws.getKnum());
        if (!Utility.fieldEquals(ws.getKnumStatus(), "00")) {
            ws.setWkMsgLine("Ledger number assignment failed");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        fileSet.getAplf().setRecord();
        ws.setPlSeq(ws.getKnumNumber());
        ws.setPlSupp(ws.getPySupp());
        ws.setPlDate(ws.getPyDate());
        ws.setPlCloseYm(ws.getWkCloseYm());
        ws.setPlKind(2);
        ws.setPlRefType(ws.getWkRefPay());
        ws.setPlRefNo(ws.getPyNo());
        ws.setPlDebit(ws.getPyAmount());
        ws.setPlCredit(BigDecimal.ZERO);
        ws.setPlBalance(BigDecimal.valueOf(ws.getWkNewBal()));
        ws.setPlRemark(ws.getPyRemark());
        ws.setPlUser(ws.getWkUserCode());
        fileSet.getAplf().write();
        ws.trySetString("FSTS", fileSet.getAplf().getFileStatus());
        if (fileSet.getAplf().isInvalidKey()) {
            ws.setWkMsgLine("Ledger write failed");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
    }

    /** COBOL paragraph: TERM-010 */
    private void closeAllFiles() {
        closeFileChecked(fileSet.getPayf());
        closeFileChecked(fileSet.getAplf());
        closeFileChecked(fileSet.getSuppf());
        closeFileChecked(fileSet.getBankf());
    }

    /** COBOL paragraph: ABEND-010 */
    private void abortOnFileOpenError() {
        ws.setKaProgid("AP0010");
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
     * Opens a file, and if the catalog status indicates it does not exist yet, creates it via an
     * OUTPUT/CLOSE/reopen cycle.
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

    /** Closes a file and records its resulting status. */
    private void closeFileChecked(RawDatasetBase file) {
        file.close();
        ws.trySetString("FSTS", file.getFileStatus());
    }

    /**
     * Resolves the read key from the screen field, falling back to the current record's key, then
     * reads by that key.
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
