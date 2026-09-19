package com.sakura.bt0020.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.bt0020.domain.Bt0020FieldAccess;
import com.sakura.bt0020.domain.WorkingStorage;
import com.sakura.bt0020.runtime.Bt0020Datasets;
import com.sakura.dateut.service.DateutService;
import com.sakura.runtime.BatchServiceBase;
import com.sakura.runtime.DatasetEnums.*;
import com.sakura.runtime.ProgramExitSignal;
import com.sakura.runtime.Utility;
import com.sakura.runtime.io.AbstractDatasets;
import com.sakura.runtime.linkage.AbortxLinkParm;
import com.sakura.runtime.linkage.DateutLinkParm;
import com.sakura.runtime.record.RawDatasetBase;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/** Business logic service generated from COBOL program BT0020. */
@Service
@Scope("prototype")
public class Bt0020Service extends BatchServiceBase {
    /** Scale factor separating the year and month parts of a YYYYMM-formatted value. */
    private static final int YM_SCALE = 100;

    /** Shared file instances for all FD files in this program. */
    private final Bt0020Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Bt0020FieldAccess ws;

    public Bt0020Service(
            Bt0020Datasets fileSet, DateutService dateutService, AbortxService abortxService) {
        this.fileSet = fileSet;
        this.ws = new Bt0020FieldAccess(new WorkingStorage(), fileSet);
        this.dateutService = dateutService;
        this.abortxService = abortxService;
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
        if (ws.getWkAbortFlg() == 0) {
            runChain(this::closeSalesInvoices);
            runChain(this::closePurchases);
            runChain(this::rollArLedger);
            runChain(this::rollApLedger);
            runChain(this::updateSystemControlRecord);
            runChain(this::printCloseSummary);
        }
        runChain(this::closeAllFiles);
        ws.setCompletionCode(0);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeProgram() {
        ws.setWkProgid("BT0020");
        log.info(" ");
        log.info("==============================================");
        log.info(" SAKURA-SMS  BT0020  -  MONTHLY CLOSE");
        log.info("==============================================");
        runChain(this::loadSystemDate);
        runChain(this::openAllFiles);
        runChain(this::readSystemControlRecord);
        ws.setWkDfltYm(ws.getSyCurrYm());
        runChain(this::acceptTargetMonth);
        if (ws.getWkAbortFlg() == 0) {
            runChain(this::computeClosePeriod);
            runChain(this::confirmMonthlyClose);
        }
    }

    /** COBOL paragraph: GTOD-010 */
    private void loadSystemDate() {
        ws.setKdFunc("TODY");
        ws.setKdDate1(0);
        dateut(ws.getKdate());
        ws.setWkSysdate(ws.getKdDate1());
        ws.setWkSysymd(ws.getKdDate1());
    }

    /** COBOL paragraph: OPEN-010 */
    private void openAllFiles() {
        runChain(this::openSyscf);
        runChain(this::openInvhf);
        runChain(this::openPurhf);
        runChain(this::openArlf);
        runChain(this::openAplf);
    }

    /** COBOL paragraph: OSYS-010 */
    private void openSyscf() {
        fileSet.getSyscf().open(FileOpenMode.IO);
        ws.trySetString("FSTS", fileSet.getSyscf().getFileStatus());
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("SYSCF");
            ws.setKaDetail("Open SYSCF failed");
            runChain(this::abortProgram);
        }
    }

    /** COBOL paragraph: OINV-010 */
    private void openInvhf() {
        openFileWithRetry(fileSet.getInvhf(), "INVHF");
    }

    /** COBOL paragraph: OPUR-010 */
    private void openPurhf() {
        openFileWithRetry(fileSet.getPurhf(), "PURHF");
    }

    /** COBOL paragraph: OARL-010 */
    private void openArlf() {
        openFileWithRetry(fileSet.getArlf(), "ARLF");
    }

    /** COBOL paragraph: OAPL-010 */
    private void openAplf() {
        openFileWithRetry(fileSet.getAplf(), "APLF");
    }

    /** COBOL paragraph: RSYS-010 */
    private void readSystemControlRecord() {
        ws.setSyKey(1);
        String rkVal_0 = "";
        if (rkVal_0 == null || rkVal_0.trim().isEmpty()) {
            try {
                rkVal_0 = ws.getString("SY-KEY");
            } catch (Exception _e) {
            }
        }
        if (rkVal_0 == null || rkVal_0.trim().isEmpty()) {
            try {
                rkVal_0 = fileSet.getSyscf().extractKeyFromCurrentRecord();
            } catch (Exception _e3) {
            }
        }
        fileSet.getSyscf().readByKey(rkVal_0 != null ? rkVal_0.trim() : "");
        ws.trySetString("FSTS", fileSet.getSyscf().getFileStatus());
        if (fileSet.getSyscf().isInvalidKey()) {
            ws.setKaFile("SYSCF");
            ws.setKaDetail("System control record missing");
            runChain(this::abortProgram);
        }
    }

    /** COBOL paragraph: ATGT-010 */
    private void acceptTargetMonth() {
        ws.setWkEYm(ws.getWkDfltYm());
        log.info(" ");
        log.info(
                " Current accounting month : {}", String.format("%06d", (long) (ws.getSyCurrYm())));
        log.info(
                " Last monthly close       : {}",
                String.format("%06d", (long) (ws.getSyLastMonClose())));
        log.info(" Enter month to close YYYYMM");
        log.info("   (blank = {}) : ", ws.editedDisplay("WK-E-YM"));
        ws.setWkInLine(" ");
        String stdinValWkInLine1 = Utility.readStdinLine();
        if (stdinValWkInLine1 == null || stdinValWkInLine1.trim().isEmpty()) {
            stdinValWkInLine1 = "";
        }
        ws.setWkInLine(stdinValWkInLine1);
        if (Utility.fieldEquals(ws.getWkInLine(), " ")) {
            ws.setWkTgtYm(ws.getWkDfltYm());
        } else {
            String wkInLinePadded =
                    Utility.padRight(String.valueOf(ws.getWkInLine()), 6).substring(0, 6);
            if (Utility.isNumeric(wkInLinePadded)) {
                ws.setWkTgtYm(Utility.parseNumeric(wkInLinePadded).intValue());
            } else {
                log.info(" ** invalid month - run cancelled.");
                ws.setWkAbortFlg(1);
                return;
            }
        }
        runChain(this::validateTargetMonth);
    }

    /** COBOL paragraph: VYM-010 */
    private void validateTargetMonth() {
        ws.setWkIdx(0);
        ws.setWkIdx((ws.getWkTgtYm() - ((ws.getWkTgtYm() / YM_SCALE) * YM_SCALE)));
        if (ws.getWkIdx() < 1 || ws.getWkIdx() > 12) {
            log.info(" ** month part must be 01-12.");
            ws.setWkAbortFlg(1);
        }
    }

    /** COBOL paragraph: CPER-010 */
    private void computeClosePeriod() {
        ws.setWkPerStart(((ws.getWkTgtYm() * YM_SCALE) + 1));
        ws.setKdFunc("EOM ");
        ws.setKdDate1(ws.getWkPerStart());
        dateut(ws.getKdate());
        if (!Utility.fieldEquals(ws.getKdStatus(), "00")) {
            ws.setKaFile("SYSCF");
            ws.setKaDetail("EOM calc failed");
            runChain(this::abortProgram);
        }
        ws.setWkPerEnd(ws.getKdDate1());
        ws.setKdFunc("ADDD");
        ws.setKdDate1(ws.getWkPerEnd());
        ws.setKdDays(1);
        dateut(ws.getKdate());
        ws.setWkNextYm((ws.getKdDate1() / YM_SCALE));
    }

    /** COBOL paragraph: CONF-010 */
    private void confirmMonthlyClose() {
        ws.setWkEYm(ws.getWkTgtYm());
        ws.setWkEDate(ws.getWkPerStart());
        log.info(" ");
        log.info(
                " Close month {} ({} - {})",
                ws.editedDisplay("WK-E-YM"),
                String.format("%08d", (long) (ws.getWkPerStart())),
                String.format("%08d", (long) (ws.getWkPerEnd())));
        ws.setWkEYm(ws.getWkNextYm());
        log.info(" New current month will be {}", ws.editedDisplay("WK-E-YM"));
        log.info(" Proceed ? (Y/N) : ");
        ws.setWkConfirm(" ");
        String stdinValWkConfirm2 = Utility.readStdinLine();
        if (stdinValWkConfirm2 == null || stdinValWkConfirm2.trim().isEmpty()) {
            stdinValWkConfirm2 = "";
        }
        ws.setWkConfirm(stdinValWkConfirm2);
        boolean confirmed = ws.getWkConfirm().equals("Y") || ws.getWkConfirm().equals("y");
        if (!confirmed) {
            log.info(" ** cancelled by operator.");
            ws.setWkAbortFlg(1);
        }
    }

    /** COBOL paragraph: CINV-010 */
    private void closeSalesInvoices() {
        log.info(" ");
        log.info(" Closing sales invoices ...");
        ws.setEofFlg(0);
        ws.setIhDate(ws.getWkPerStart());
        ws.setIhNo(0);
        fileSet.getInvhf().start("IH-DATE", "NOT LESS THAN");
        ws.trySetString("FSTS", fileSet.getInvhf().getFileStatus());
        if (fileSet.getInvhf().isInvalidKey()) {
            ws.setString("EOF-FLG", "1");
        }
        while ((ws.getEofFlg() != 1)) {
            runChain(this::closeInvoiceRecord);
        }
    }

    /** COBOL paragraph: CINX-010 */
    private void closeInvoiceRecord() {
        fileSet.getInvhf().readNext();
        ws.trySetString("FSTS", fileSet.getInvhf().getFileStatus());
        if (fileSet.getInvhf().isAtEnd()) {
            ws.setString("EOF-FLG", "1");
            return;
        }
        if (ws.getIhDate() > ws.getWkPerEnd()) {
            ws.setString("EOF-FLG", "1");
            return;
        }
        ws.setWkInvRead(ws.getWkInvRead() + 1);
        if (ws.getIhStatus() == 1 && ws.getIhCloseYm() == 0) {
            ws.setIhCloseYm(ws.getWkTgtYm());
            ws.setIhStatus(2);
            ws.setIhUpdDate(ws.getWkSysdate());
            ws.setIhUpdUser(ws.getWkUserCode());
            fileSet.getInvhf().rewrite();
            ws.trySetString("FSTS", fileSet.getInvhf().getFileStatus());
            if (fileSet.getInvhf().isInvalidKey()) {
                ws.setKaFile("INVHF");
                ws.setKaDetail("REWRITE INVHF failed");
                runChain(this::abortProgram);
            }
            ws.setWkInvClose(ws.getWkInvClose() + 1);
            if (ws.getIhKind() == 2) {
                ws.setWkInvAmt(
                        (BigDecimal.valueOf(ws.getWkInvAmt()).subtract(ws.getIhAmount()))
                                .longValue());
                ws.setWkInvTax(
                        (BigDecimal.valueOf(ws.getWkInvTax()).subtract(ws.getIhTaxAmount()))
                                .longValue());
            } else {
                ws.setWkInvAmt(
                        (BigDecimal.valueOf(ws.getWkInvAmt()).add(ws.getIhAmount())).longValue());
                ws.setWkInvTax(
                        (BigDecimal.valueOf(ws.getWkInvTax()).add(ws.getIhTaxAmount()))
                                .longValue());
            }
        } else {
            ws.setWkInvSkip(ws.getWkInvSkip() + 1);
        }
    }

    /** COBOL paragraph: CPUR-010 */
    private void closePurchases() {
        log.info(" Closing purchases ...");
        ws.setEofFlg(0);
        ws.setVhNo(0);
        fileSet.getPurhf().start("VH-NO", "NOT LESS THAN");
        ws.trySetString("FSTS", fileSet.getPurhf().getFileStatus());
        if (fileSet.getPurhf().isInvalidKey()) {
            ws.setString("EOF-FLG", "1");
        }
        while ((ws.getEofFlg() != 1)) {
            runChain(this::closePurchaseRecord);
        }
    }

    /** COBOL paragraph: CPUX-010 */
    private void closePurchaseRecord() {
        fileSet.getPurhf().readNext();
        ws.trySetString("FSTS", fileSet.getPurhf().getFileStatus());
        if (fileSet.getPurhf().isAtEnd()) {
            ws.setString("EOF-FLG", "1");
            return;
        }
        if (ws.getVhDate() < ws.getWkPerStart() || ws.getVhDate() > ws.getWkPerEnd()) {
            return;
        }
        ws.setWkPurRead(ws.getWkPurRead() + 1);
        if (ws.getVhStatus() == 1 && ws.getVhCloseYm() == 0) {
            ws.setVhCloseYm(ws.getWkTgtYm());
            ws.setVhStatus(2);
            fileSet.getPurhf().rewrite();
            ws.trySetString("FSTS", fileSet.getPurhf().getFileStatus());
            if (fileSet.getPurhf().isInvalidKey()) {
                ws.setKaFile("PURHF");
                ws.setKaDetail("REWRITE PURHF failed");
                runChain(this::abortProgram);
            }
            ws.setWkPurClose(ws.getWkPurClose() + 1);
            if (ws.getVhKind() == 2) {
                ws.setWkPurAmt(
                        (BigDecimal.valueOf(ws.getWkPurAmt()).subtract(ws.getVhAmount()))
                                .longValue());
                ws.setWkPurTax(
                        (BigDecimal.valueOf(ws.getWkPurTax()).subtract(ws.getVhTaxAmount()))
                                .longValue());
            } else {
                ws.setWkPurAmt(
                        (BigDecimal.valueOf(ws.getWkPurAmt()).add(ws.getVhAmount())).longValue());
                ws.setWkPurTax(
                        (BigDecimal.valueOf(ws.getWkPurTax()).add(ws.getVhTaxAmount()))
                                .longValue());
            }
        } else {
            ws.setWkPurSkip(ws.getWkPurSkip() + 1);
        }
    }

    /** COBOL paragraph: RAR-010 */
    private void rollArLedger() {
        log.info(" Rolling AR ledger ...");
        ws.setEofFlg(0);
        ws.setWkFirstFlg(1);
        ws.setWkPrevCust(0);
        ws.setWkCustBal(0);
        ws.setAlCust(0);
        ws.setAlDate(0);
        fileSet.getArlf().start("AL-CUST", "NOT LESS THAN");
        ws.trySetString("FSTS", fileSet.getArlf().getFileStatus());
        if (fileSet.getArlf().isInvalidKey()) {
            ws.setString("EOF-FLG", "1");
        }
        while ((ws.getEofFlg() != 1)) {
            runChain(this::rollArRecord);
        }
        if (ws.getWkFirstFlg() == 0) {
            runChain(this::accumulateArCustomerBreak);
        }
    }

    /** COBOL paragraph: RARX-010 */
    private void rollArRecord() {
        fileSet.getArlf().readNext();
        ws.trySetString("FSTS", fileSet.getArlf().getFileStatus());
        if (fileSet.getArlf().isAtEnd()) {
            ws.setString("EOF-FLG", "1");
            return;
        }
        if (ws.getWkFirstFlg() == 1) {
            ws.setWkFirstFlg(0);
            ws.setWkPrevCust(ws.getAlCust());
            ws.setWkCustBal(0);
        }
        if (ws.getAlCust() != ws.getWkPrevCust()) {
            runChain(this::accumulateArCustomerBreak);
            ws.setWkPrevCust(ws.getAlCust());
            ws.setWkCustBal(0);
        }
        ws.setWkArRead(ws.getWkArRead() + 1);
        ws.setWkCustBal(ws.getAlBalance().longValue());
        if (ws.getAlDate() >= ws.getWkPerStart() && ws.getAlDate() <= ws.getWkPerEnd()) {
            ws.setWkArDebit(
                    (BigDecimal.valueOf(ws.getWkArDebit()).add(ws.getAlDebit())).longValue());
            ws.setWkArCredit(
                    (BigDecimal.valueOf(ws.getWkArCredit()).add(ws.getAlCredit())).longValue());
            if (ws.getAlCloseYm() == 0) {
                ws.setAlCloseYm(ws.getWkTgtYm());
                fileSet.getArlf().rewrite();
                ws.trySetString("FSTS", fileSet.getArlf().getFileStatus());
                if (fileSet.getArlf().isInvalidKey()) {
                    ws.setKaFile("ARLF");
                    ws.setKaDetail("REWRITE ARLF failed");
                    runChain(this::abortProgram);
                }
                ws.setWkArStamp(ws.getWkArStamp() + 1);
            }
        }
    }

    /** COBOL paragraph: ARBK-010 */
    private void accumulateArCustomerBreak() {
        ws.setWkArCust(ws.getWkArCust() + 1);
        ws.setWkArClosebal(ws.getWkArClosebal() + ws.getWkCustBal());
    }

    /** COBOL paragraph: RAP-010 */
    private void rollApLedger() {
        log.info(" Rolling AP ledger ...");
        ws.setEofFlg(0);
        ws.setWkFirstFlg(1);
        ws.setWkPrevSupp(0);
        ws.setWkSuppBal(0);
        ws.setPlSupp(0);
        ws.setPlDate(0);
        fileSet.getAplf().start("PL-SUPP", "NOT LESS THAN");
        ws.trySetString("FSTS", fileSet.getAplf().getFileStatus());
        if (fileSet.getAplf().isInvalidKey()) {
            ws.setString("EOF-FLG", "1");
        }
        while ((ws.getEofFlg() != 1)) {
            runChain(this::rollApRecord);
        }
        if (ws.getWkFirstFlg() == 0) {
            runChain(this::accumulateApSupplierBreak);
        }
    }

    /** COBOL paragraph: RAPX-010 */
    private void rollApRecord() {
        fileSet.getAplf().readNext();
        ws.trySetString("FSTS", fileSet.getAplf().getFileStatus());
        if (fileSet.getAplf().isAtEnd()) {
            ws.setString("EOF-FLG", "1");
            return;
        }
        if (ws.getWkFirstFlg() == 1) {
            ws.setWkFirstFlg(0);
            ws.setWkPrevSupp(ws.getPlSupp());
            ws.setWkSuppBal(0);
        }
        if (ws.getPlSupp() != ws.getWkPrevSupp()) {
            runChain(this::accumulateApSupplierBreak);
            ws.setWkPrevSupp(ws.getPlSupp());
            ws.setWkSuppBal(0);
        }
        ws.setWkApRead(ws.getWkApRead() + 1);
        ws.setWkSuppBal(ws.getPlBalance().longValue());
        if (ws.getPlDate() >= ws.getWkPerStart() && ws.getPlDate() <= ws.getWkPerEnd()) {
            ws.setWkApDebit(
                    (BigDecimal.valueOf(ws.getWkApDebit()).add(ws.getPlDebit())).longValue());
            ws.setWkApCredit(
                    (BigDecimal.valueOf(ws.getWkApCredit()).add(ws.getPlCredit())).longValue());
            if (ws.getPlCloseYm() == 0) {
                ws.setPlCloseYm(ws.getWkTgtYm());
                fileSet.getAplf().rewrite();
                ws.trySetString("FSTS", fileSet.getAplf().getFileStatus());
                if (fileSet.getAplf().isInvalidKey()) {
                    ws.setKaFile("APLF");
                    ws.setKaDetail("REWRITE APLF failed");
                    runChain(this::abortProgram);
                }
                ws.setWkApStamp(ws.getWkApStamp() + 1);
            }
        }
    }

    /** COBOL paragraph: APBK-010 */
    private void accumulateApSupplierBreak() {
        ws.setWkApSupp(ws.getWkApSupp() + 1);
        ws.setWkApClosebal(ws.getWkApClosebal() + ws.getWkSuppBal());
    }

    /** COBOL paragraph: USYS-010 */
    private void updateSystemControlRecord() {
        ws.setSyKey(1);
        String rkVal_3 = "";
        if (rkVal_3 == null || rkVal_3.trim().isEmpty()) {
            try {
                rkVal_3 = ws.getString("SY-KEY");
            } catch (Exception _e) {
            }
        }
        if (rkVal_3 == null || rkVal_3.trim().isEmpty()) {
            try {
                rkVal_3 = fileSet.getSyscf().extractKeyFromCurrentRecord();
            } catch (Exception _e3) {
            }
        }
        fileSet.getSyscf().readByKey(rkVal_3 != null ? rkVal_3.trim() : "");
        ws.trySetString("FSTS", fileSet.getSyscf().getFileStatus());
        if (fileSet.getSyscf().isInvalidKey()) {
            ws.setKaFile("SYSCF");
            ws.setKaDetail("Re-read SYSCF failed");
            runChain(this::abortProgram);
        }
        ws.setSyLastMonClose(ws.getWkTgtYm());
        ws.setSyCurrYm(ws.getWkNextYm());
        fileSet.getSyscf().rewrite();
        ws.trySetString("FSTS", fileSet.getSyscf().getFileStatus());
        if (fileSet.getSyscf().isInvalidKey()) {
            ws.setKaFile("SYSCF");
            ws.setKaDetail("REWRITE SYSCF failed");
            runChain(this::abortProgram);
        }
    }

    /** COBOL paragraph: PSUM-010 */
    private void printCloseSummary() {
        ws.setWkEYm(ws.getWkTgtYm());
        log.info(" ");
        log.info("----------------------------------------------");
        log.info(" MONTHLY CLOSE SUMMARY  month {}", ws.editedDisplay("WK-E-YM"));
        log.info("----------------------------------------------");
        runChain(this::printInvoiceSummary);
        runChain(this::printPurchaseSummary);
        runChain(this::printArSummary);
        runChain(this::printApSummary);
        ws.setWkEYm(ws.getWkNextYm());
        log.info("----------------------------------------------");
        log.info(" New current month : {}", ws.editedDisplay("WK-E-YM"));
        log.info(" BT0020 completed normally.");
    }

    /** COBOL paragraph: PSIN-010 */
    private void printInvoiceSummary() {
        ws.setWkECnt(ws.getWkInvRead());
        log.info(" Invoices scanned  : {}", ws.editedDisplay("WK-E-CNT"));
        ws.setWkECnt(ws.getWkInvClose());
        log.info(" Invoices closed   : {}", ws.editedDisplay("WK-E-CNT"));
        ws.setWkEAmt(ws.getWkInvAmt());
        log.info(" Sales net amount  : {}", ws.editedDisplay("WK-E-AMT"));
        ws.setWkEAmt(ws.getWkInvTax());
        log.info(" Sales tax amount  : {}", ws.editedDisplay("WK-E-AMT"));
    }

    /** COBOL paragraph: PSPU-010 */
    private void printPurchaseSummary() {
        ws.setWkECnt(ws.getWkPurRead());
        log.info(" Purchases scanned : {}", ws.editedDisplay("WK-E-CNT"));
        ws.setWkECnt(ws.getWkPurClose());
        log.info(" Purchases closed  : {}", ws.editedDisplay("WK-E-CNT"));
        ws.setWkEAmt(ws.getWkPurAmt());
        log.info(" Purch net amount  : {}", ws.editedDisplay("WK-E-AMT"));
        ws.setWkEAmt(ws.getWkPurTax());
        log.info(" Purch tax amount  : {}", ws.editedDisplay("WK-E-AMT"));
    }

    /** COBOL paragraph: PSAR-010 */
    private void printArSummary() {
        ws.setWkECnt(ws.getWkArRead());
        log.info(" AR entries read   : {}", ws.editedDisplay("WK-E-CNT"));
        ws.setWkECnt(ws.getWkArStamp());
        log.info(" AR entries closed : {}", ws.editedDisplay("WK-E-CNT"));
        ws.setWkECnt(ws.getWkArCust());
        log.info(" AR customers      : {}", ws.editedDisplay("WK-E-CNT"));
        ws.setWkEAmt(ws.getWkArDebit());
        log.info(" AR debit total    : {}", ws.editedDisplay("WK-E-AMT"));
        ws.setWkEAmt(ws.getWkArCredit());
        log.info(" AR credit total   : {}", ws.editedDisplay("WK-E-AMT"));
        ws.setWkEAmt(ws.getWkArClosebal());
        log.info(" AR closing bal    : {}", ws.editedDisplay("WK-E-AMT"));
    }

    /** COBOL paragraph: PSAP-010 */
    private void printApSummary() {
        ws.setWkECnt(ws.getWkApRead());
        log.info(" AP entries read   : {}", ws.editedDisplay("WK-E-CNT"));
        ws.setWkECnt(ws.getWkApStamp());
        log.info(" AP entries closed : {}", ws.editedDisplay("WK-E-CNT"));
        ws.setWkECnt(ws.getWkApSupp());
        log.info(" AP suppliers      : {}", ws.editedDisplay("WK-E-CNT"));
        ws.setWkEAmt(ws.getWkApDebit());
        log.info(" AP debit total    : {}", ws.editedDisplay("WK-E-AMT"));
        ws.setWkEAmt(ws.getWkApCredit());
        log.info(" AP credit total   : {}", ws.editedDisplay("WK-E-AMT"));
        ws.setWkEAmt(ws.getWkApClosebal());
        log.info(" AP closing bal    : {}", ws.editedDisplay("WK-E-AMT"));
    }

    /** COBOL paragraph: TERM-010 */
    private void closeAllFiles() {
        fileSet.getSyscf().close();
        ws.trySetString("FSTS", fileSet.getSyscf().getFileStatus());
        fileSet.getInvhf().close();
        ws.trySetString("FSTS", fileSet.getInvhf().getFileStatus());
        fileSet.getPurhf().close();
        ws.trySetString("FSTS", fileSet.getPurhf().getFileStatus());
        fileSet.getArlf().close();
        ws.trySetString("FSTS", fileSet.getArlf().getFileStatus());
        fileSet.getAplf().close();
        ws.trySetString("FSTS", fileSet.getAplf().getFileStatus());
    }

    /** COBOL paragraph: ABND-010 */
    private void abortProgram() {
        ws.setKaProgid("BT0020");
        ws.setKaFsts(ws.getFsts());
        ws.setKaMsgcode("EBATCH");
        abortx(ws.getKabend());
        runChain(this::closeAllFiles);
        ws.setCompletionCode(255);
        throw new ProgramExitSignal();
    }

    /**
     * Open a data file I/O; if it does not exist yet (status 35/30), create it via an OUTPUT
     * open/close cycle and retry the I/O open. Aborts the program on any other failure.
     */
    private void openFileWithRetry(RawDatasetBase file, String fileName) {
        file.open(FileOpenMode.IO);
        ws.trySetString("FSTS", file.getFileStatus());
        if (Utility.fieldEquals(ws.getFsts(), "35") || Utility.fieldEquals(ws.getFsts(), "30")) {
            file.open(FileOpenMode.OUTPUT);
            ws.trySetString("FSTS", file.getFileStatus());
            file.close();
            ws.trySetString("FSTS", file.getFileStatus());
            file.open(FileOpenMode.IO);
            ws.trySetString("FSTS", file.getFileStatus());
        }
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile(fileName);
            ws.setKaDetail("Open " + fileName + " failed");
            runChain(this::abortProgram);
        }
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
}
