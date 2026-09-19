package com.sakura.bt0090.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.bt0090.domain.Bt0090FieldAccess;
import com.sakura.bt0090.domain.WorkingStorage;
import com.sakura.bt0090.runtime.Bt0090Datasets;
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

/** Business logic service generated from COBOL program BT0090. */
@Service
@Scope("prototype")
public class Bt0090Service extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final Bt0090Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Bt0090FieldAccess ws;

    public Bt0090Service(
            Bt0090Datasets fileSet, DateutService dateutService, AbortxService abortxService) {
        this.fileSet = fileSet;
        this.ws = new Bt0090FieldAccess(new WorkingStorage(), fileSet);
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
            runChain(this::writeReportHeader);
            runChain(this::checkOrphanOrderDetails);
            runChain(this::checkOrphanInvoiceDetails);
            runChain(this::checkStockMissingProduct);
            runChain(this::checkStockNegativeOnHand);
            runChain(this::printSummary);
        }
        runChain(this::terminateProgram);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeProgram() {
        ws.setWkProgid("BT0090");
        log.info(" ");
        log.info("==============================================");
        log.info(" SAKURA-SMS  BT0090  -  DATA INTEGRITY CHECK");
        log.info("==============================================");
        runChain(this::loadSystemDate);
        runChain(this::openAllFiles);
        runChain(this::confirmScanProceed);
    }

    /** COBOL paragraph: GTOD-010 */
    private void loadSystemDate() {
        ws.setKdFunc("TODY");
        ws.setKdDate1(0);
        dateut(ws.getKdate());
        ws.setWkSysdate(ws.getKdDate1());
    }

    /** COBOL paragraph: OPEN-010 */
    private void openAllFiles() {
        ws.setWkCompany("SAKURA Sales Management System");
        loadCompanyNameFromSyscf();
        openFileWithRetry(fileSet.getOrddf(), "ORDDF");
        openFileWithRetry(fileSet.getOrdhf(), "ORDHF");
        openFileWithRetry(fileSet.getInvdf(), "INVDF");
        openFileWithRetry(fileSet.getInvhf(), "INVHF");
        openFileWithRetry(fileSet.getStokf(), "STOKF");
        openFileWithRetry(fileSet.getProdf(), "PRODF");
        fileSet.getRepf().open(FileOpenMode.OUTPUT);
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("REPF");
            ws.setKaDetail("Open report file failed");
            runChain(this::abendProgram);
        }
    }

    /** COBOL paragraph: CONF-010 */
    private void confirmScanProceed() {
        log.info(" ");
        log.info(" Scan transaction / stock files for integrity");
        log.info(" problems (read-only).  Proceed ? (Y/N) [Y] : ");
        ws.setWkConfirm(" ");
        String stdinValWkConfirm1 = Utility.readStdinLine();
        if (stdinValWkConfirm1 == null || stdinValWkConfirm1.trim().isEmpty()) {
            stdinValWkConfirm1 = "";
        }
        ws.setWkConfirm(stdinValWkConfirm1);
        if ((ws.getWkConfirm().equals("N") || ws.getWkConfirm().equals("n"))) {
            log.info(" ** cancelled by operator.");
            ws.setWkAbortFlg(1);
        }
    }

    /** COBOL paragraph: WRH-010 */
    private void writeReportHeader() {
        ws.setWkLine(99);
        runChain(this::printPageHeader);
    }

    /** COBOL paragraph: COD-010 */
    private void checkOrphanOrderDetails() {
        scanFileForExceptions(
                "(A) ORPHAN ORDER-DETAIL LINES  (ORDDF vs ORDHF)",
                fileSet.getOrddf(),
                "OD-NO",
                () -> {
                    ws.setOdNo(0);
                    ws.setOdLine(0);
                },
                () -> runChain(this::readOrderDetailCheckHeader));
    }

    /** COBOL paragraph: CODN-010 */
    private void readOrderDetailCheckHeader() {
        readNextAndCrossCheck(
                fileSet.getOrddf(),
                "ORDDF",
                () -> ws.setWkOrddCnt(ws.getWkOrddCnt() + 1),
                () -> ws.setOhNo(ws.getOdNo()),
                "OH-NO",
                fileSet.getOrdhf(),
                () -> runChain(this::reportOrphanOrderDetail));
    }

    /** COBOL paragraph: ROD-010 */
    private void reportOrphanOrderDetail() {
        writeExceptionReport(
                () -> ws.setWkExOrphOd(ws.getWkExOrphOd() + 1),
                "ORPHAN ORDER-DTL",
                "DOC#  ",
                ws.getOdNo(),
                "LINE  ",
                ws.getOdLine(),
                "order header not found in ORDHF");
    }

    /** COBOL paragraph: CID-010 */
    private void checkOrphanInvoiceDetails() {
        scanFileForExceptions(
                "(B) ORPHAN INVOICE-DETAIL LINES  (INVDF vs INVHF)",
                fileSet.getInvdf(),
                "ID-NO",
                () -> {
                    ws.setIdNo(0);
                    ws.setIdLine(0);
                },
                () -> runChain(this::readInvoiceDetailCheckHeader));
    }

    /** COBOL paragraph: CIDN-010 */
    private void readInvoiceDetailCheckHeader() {
        readNextAndCrossCheck(
                fileSet.getInvdf(),
                "INVDF",
                () -> ws.setWkInvdCnt(ws.getWkInvdCnt() + 1),
                () -> ws.setIhNo(ws.getIdNo()),
                "IH-NO",
                fileSet.getInvhf(),
                () -> runChain(this::reportOrphanInvoiceDetail));
    }

    /** COBOL paragraph: RID-010 */
    private void reportOrphanInvoiceDetail() {
        writeExceptionReport(
                () -> ws.setWkExOrphId(ws.getWkExOrphId() + 1),
                "ORPHAN INV-DTL",
                "DOC#  ",
                ws.getIdNo(),
                "LINE  ",
                ws.getIdLine(),
                "invoice header not found in INVHF");
    }

    /** COBOL paragraph: CSP-010 */
    private void checkStockMissingProduct() {
        scanFileForExceptions(
                "(C) STOCK ROWS WITH MISSING PRODUCT  (STOKF vs PRODF)",
                fileSet.getStokf(),
                "SK-PROD",
                () -> {
                    ws.setSkProd(0);
                    ws.setSkWhse(0);
                },
                () -> runChain(this::readStockCheckProduct));
    }

    /** COBOL paragraph: CSPN-010 */
    private void readStockCheckProduct() {
        readNextAndCrossCheck(
                fileSet.getStokf(),
                "STOKF",
                () -> ws.setWkStokCnt(ws.getWkStokCnt() + 1),
                () -> ws.setPrCode(ws.getSkProd()),
                "PR-CODE",
                fileSet.getProdf(),
                () -> runChain(this::reportMissingProduct));
    }

    /** COBOL paragraph: RMP-010 */
    private void reportMissingProduct() {
        writeExceptionReport(
                () -> ws.setWkExProd(ws.getWkExProd() + 1),
                "MISSING PRODUCT",
                "PROD  ",
                ws.getSkProd(),
                "WHSE  ",
                ws.getSkWhse(),
                "product code not found in PRODF");
    }

    /** COBOL paragraph: CSN-010 */
    private void checkStockNegativeOnHand() {
        scanFileForExceptions(
                "(D) STOCK ROWS WITH NEGATIVE ON-HAND  (STOKF)",
                fileSet.getStokf(),
                "SK-PROD",
                () -> {
                    ws.setSkProd(0);
                    ws.setSkWhse(0);
                },
                () -> runChain(this::readStockCheckNegative));
    }

    /** COBOL paragraph: CSNN-010 */
    private void readStockCheckNegative() {
        if (!readNextRecordChecked(fileSet.getStokf(), "STOKF")) {
            return;
        }
        if ((ws.getSkOnhand().signum() < 0)) {
            runChain(this::reportNegativeOnHand);
        }
    }

    /** COBOL paragraph: RNG-010 */
    private void reportNegativeOnHand() {
        ws.setWkMsgtxt(" ");
        ws.setWkEn(ws.getSkOnhand().longValue());
        {
            StringBuilder sb = new StringBuilder();
            sb.append(String.valueOf("on-hand qty = "));
            sb.append(String.valueOf(ws.getString("WK-EN")));
            ws.setWkMsgtxt(sb.toString());
        }
        writeExceptionReport(
                () -> ws.setWkExNeg(ws.getWkExNeg() + 1),
                "NEGATIVE ON-HAND",
                "PROD  ",
                ws.getSkProd(),
                "WHSE  ",
                ws.getSkWhse(),
                ws.getWkMsgtxt());
    }

    /** COBOL paragraph: WEX-010 */
    private void writeExceptionLine() {
        runChain(this::checkPageBreak);
        ws.copyRepRecFromRdEx();
        writeReportLine();
        log.info("{}", ws.getRdEx());
    }

    /** COBOL paragraph: WS-010 */
    private void writeSectionHeader() {
        runChain(this::checkPageBreak);
        ws.setRepRec(" ");
        writeReportLine();
        ws.copyRepRecFromRdSect();
        writeReportLine();
        ws.setRepRec(String.valueOf(ws.getRptRule()));
        writeReportLine();
        log.info(" ");
        log.info("{}", ws.getSectTxt());
    }

    /** COBOL paragraph: CP-010 */
    private void checkPageBreak() {
        if (ws.getWkLine() >= 55) {
            runChain(this::printPageHeader);
        }
    }

    /** COBOL paragraph: PH-010 */
    private void printPageHeader() {
        ws.setWkPage(ws.getWkPage() + 1);
        ws.setH1Company(ws.getWkCompany());
        ws.setH1Title("DATA INTEGRITY CHECK");
        ws.setH1Page(ws.getWkPage());
        ws.copyRepRecFromRptH1();
        writeReportRecord();
        ws.setH2Date(ws.getWkSysdate());
        ws.setH2Info("BT0090 EXCEPTION REPORT");
        ws.copyRepRecFromRptH2();
        writeReportRecord();
        ws.setRepRec(" ");
        writeReportRecord();
        ws.setWkLine(4);
    }

    /** COBOL paragraph: PSUM-010 */
    private void printSummary() {
        ws.setSmLbl("(A) orphan order-detail lines   :");
        ws.setSmVal(ws.getWkExOrphOd());
        runChain(this::writeCountLine);
        ws.setSmLbl("(B) orphan invoice-detail lines :");
        ws.setSmVal(ws.getWkExOrphId());
        runChain(this::writeCountLine);
        ws.setSmLbl("(C) stock rows missing product  :");
        ws.setSmVal(ws.getWkExProd());
        runChain(this::writeCountLine);
        ws.setSmLbl("(D) stock rows negative on-hand :");
        ws.setSmVal(ws.getWkExNeg());
        runChain(this::writeCountLine);
        ws.setSmLbl("    TOTAL EXCEPTIONS             :");
        ws.setSmVal(ws.getWkExTotal());
        runChain(this::writeCountLine);
        runChain(this::writeVerdictLine);
    }

    /** COBOL paragraph: WC-010 */
    private void writeCountLine() {
        runChain(this::checkPageBreak);
        ws.copyRepRecFromRdCnt();
        writeReportLine();
        ws.setWkECnt(ws.getSmVal());
        log.info("{} {}", ws.getSmLbl(), ws.editedDisplay("WK-E-CNT"));
    }

    /** COBOL paragraph: WV-010 */
    private void writeVerdictLine() {
        runChain(this::checkPageBreak);
        ws.setRepRec(" ");
        writeReportRecord();
        ws.setRepRec(String.valueOf(ws.getRptRule()));
        writeReportRecord();
        if (ws.getWkExTotal() == 0) {
            ws.setVerdTxt(" VERDICT : CLEAN - no integrity problems found");
            ws.setCompletionCode(0);
        } else {
            ws.setVerdTxt(" VERDICT : DIRTY - integrity problems detected");
            ws.setCompletionCode(4);
        }
        ws.copyRepRecFromRdVerd();
        writeReportRecord();
        log.info(" ");
        log.info("{}", ws.getVerdTxt());
    }

    /** COBOL paragraph: TERM-010 */
    private void terminateProgram() {
        if (ws.getWkAbortFlg() == 0) {
            runChain(this::logRecordsScannedSummary);
            closeFile(fileSet.getRepf());
        }
        closeFile(fileSet.getOrddf());
        closeFile(fileSet.getOrdhf());
        closeFile(fileSet.getInvdf());
        closeFile(fileSet.getInvhf());
        closeFile(fileSet.getStokf());
        closeFile(fileSet.getProdf());
    }

    /** COBOL paragraph: DST-010 */
    private void logRecordsScannedSummary() {
        log.info(" ");
        log.info("----------------------------------------------");
        log.info(" RECORDS SCANNED");
        ws.setWkECnt(ws.getWkOrddCnt());
        log.info("   order details   : {}", ws.editedDisplay("WK-E-CNT"));
        ws.setWkECnt(ws.getWkInvdCnt());
        log.info("   invoice details : {}", ws.editedDisplay("WK-E-CNT"));
        ws.setWkECnt(ws.getWkStokCnt());
        log.info("   stock rows      : {}", ws.editedDisplay("WK-E-CNT"));
        log.info("----------------------------------------------");
        log.info(" BT0090 completed.  See reports/BT0090.TXT");
    }

    /** COBOL paragraph: ABND-010 */
    private void abendProgram() {
        ws.setKaProgid("BT0090");
        ws.setKaFsts(ws.getFsts());
        ws.setKaMsgcode("EBATCH");
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

    /**
     * Look up the company name from SYSCF (falls back to the default when the key is not found).
     */
    private void loadCompanyNameFromSyscf() {
        fileSet.getSyscf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getSyscf().getFileStatus());
        if (Utility.fieldEquals(ws.getFsts(), "00")) {
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
                /* CONTINUE */
            }
            if (!fileSet.getSyscf().isInvalidKey()) {
                ws.setWkCompany(ws.getSyCompanyName());
            }
            fileSet.getSyscf().close();
            ws.trySetString("FSTS", fileSet.getSyscf().getFileStatus());
        }
    }

    /**
     * Open a file for input, retrying via a create-then-reopen cycle when not found, then abend on
     * persistent failure.
     */
    private void openFileWithRetry(RawDatasetBase file, String fileLabel) {
        file.open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", file.getFileStatus());
        if (Utility.fieldEquals(ws.getFsts(), "35") || Utility.fieldEquals(ws.getFsts(), "30")) {
            file.open(FileOpenMode.OUTPUT);
            ws.trySetString("FSTS", file.getFileStatus());
            file.close();
            ws.trySetString("FSTS", file.getFileStatus());
            file.open(FileOpenMode.INPUT);
            ws.trySetString("FSTS", file.getFileStatus());
        }
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile(fileLabel);
            ws.setKaDetail("Open " + fileLabel + " failed");
            runChain(this::abendProgram);
        }
    }

    /** Close a file and capture its final file status. */
    private void closeFile(RawDatasetBase file) {
        file.close();
        ws.trySetString("FSTS", file.getFileStatus());
    }

    /** Write the current REPF record and capture its file status. */
    private void writeReportRecord() {
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
    }

    /** Write the current REPF record, capture status, and advance the line counter. */
    private void writeReportLine() {
        writeReportRecord();
        ws.setWkLine(ws.getWkLine() + 1);
    }

    /** Position a file at its starting key and drive the per-record exception scan until EOF. */
    private void scanFileForExceptions(
            String sectionText,
            RawDatasetBase file,
            String startKeyName,
            Runnable resetScanKeys,
            Runnable detailStep) {
        ws.setSectTxt(sectionText);
        runChain(this::writeSectionHeader);
        ws.setWkEof(0);
        resetScanKeys.run();
        file.start(startKeyName, "NOT LESS THAN");
        ws.trySetString("FSTS", file.getFileStatus());
        if (file.isInvalidKey()) {
            ws.setWkEof(1);
        }
        while (ws.getWkEof() != 1) {
            detailStep.run();
        }
    }

    /**
     * Read the next record, flagging EOF or abending on an unexpected read failure. Returns false
     * when EOF was hit.
     */
    private boolean readNextRecordChecked(RawDatasetBase file, String fileLabel) {
        file.readNext();
        ws.trySetString("FSTS", file.getFileStatus());
        if (file.isAtEnd()) {
            ws.setWkEof(1);
            return false;
        }
        if (!Utility.fieldEquals(ws.getFsts(), "00") && !Utility.fieldEquals(ws.getFsts(), "02")) {
            ws.setKaFile(fileLabel);
            ws.setKaDetail("READ NEXT " + fileLabel + " failed");
            runChain(this::abendProgram);
        }
        return true;
    }

    /** Read the next detail record and cross-check its parent/header record by key. */
    private void readNextAndCrossCheck(
            RawDatasetBase detailFile,
            String detailLabel,
            Runnable incrementReadCount,
            Runnable setLookupKeyField,
            String lookupKeyName,
            RawDatasetBase headerFile,
            Runnable onHeaderMissing) {
        if (!readNextRecordChecked(detailFile, detailLabel)) {
            return;
        }
        incrementReadCount.run();
        setLookupKeyField.run();
        String rkVal = "";
        if (rkVal == null || rkVal.trim().isEmpty()) {
            try {
                rkVal = ws.getString(lookupKeyName);
            } catch (Exception _e) {
            }
        }
        if (rkVal == null || rkVal.trim().isEmpty()) {
            try {
                rkVal = headerFile.extractKeyFromCurrentRecord();
            } catch (Exception _e3) {
            }
        }
        headerFile.readByKey(rkVal != null ? rkVal.trim() : "");
        ws.trySetString("FSTS", headerFile.getFileStatus());
        if (headerFile.isInvalidKey()) {
            onHeaderMissing.run();
        }
    }

    /** Record one exception row's counters/key fields and write it to the report. */
    private void writeExceptionReport(
            Runnable incrementCounter,
            String tag,
            String key1Label,
            long key1Value,
            String key2Label,
            int key2Value,
            String message) {
        incrementCounter.run();
        ws.setWkExTotal(ws.getWkExTotal() + 1);
        ws.setExTag(tag);
        ws.setExKey1lbl(key1Label);
        ws.setExKey1(key1Value);
        ws.setExKey2lbl(key2Label);
        ws.setExKey2(key2Value);
        ws.setExMsg(message);
        runChain(this::writeExceptionLine);
    }
}
