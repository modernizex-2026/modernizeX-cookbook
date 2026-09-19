package com.sakura.bt0060.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.bt0060.domain.Bt0060FieldAccess;
import com.sakura.bt0060.domain.WorkingStorage;
import com.sakura.bt0060.runtime.Bt0060Datasets;
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

/** Business logic service generated from COBOL program BT0060. */
@Service
@Scope("prototype")
public class Bt0060Service extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final Bt0060Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Bt0060FieldAccess ws;

    public Bt0060Service(
            Bt0060Datasets fileSet, DateutService dateutService, AbortxService abortxService) {
        this.fileSet = fileSet;
        this.ws = new Bt0060FieldAccess(new WorkingStorage(), fileSet);
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
            runChain(this::purgeCustomers);
            runChain(this::purgeProducts);
            runChain(this::purgeOrders);
            runChain(this::logPurgeSummary);
        }
        runChain(this::closeAllFiles);
        ws.setCompletionCode(0);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeProgram() {
        ws.setWkProgid("BT0060");
        log.info(" ");
        log.info("==============================================");
        log.info(" SAKURA-SMS  BT0060  -  PURGE / ARCHIVE");
        log.info("==============================================");
        runChain(this::loadSystemDate);
        ws.setKdFunc("ADDD");
        ws.setKdDate1(ws.getWkSysdate());
        ws.setKdDays(-365);
        dateut(ws.getKdate());
        ws.setWkDfltCut(ws.getKdDate1());
        runChain(this::openAllFiles);
        runChain(this::promptAndValidateCutoffDate);
        if (ws.getWkAbortFlg() == 0) {
            runChain(this::promptPurgeConfirmation);
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
        runChain(this::openCustfWithRetry);
        runChain(this::openProdfWithRetry);
        runChain(this::openOrdhfWithRetry);
        runChain(this::openOrddfWithRetry);
    }

    /** COBOL paragraph: OCUS-010 */
    private void openCustfWithRetry() {
        openFileWithRetry(fileSet.getCustf(), "CUSTF");
    }

    /** COBOL paragraph: OPRD-010 */
    private void openProdfWithRetry() {
        openFileWithRetry(fileSet.getProdf(), "PRODF");
    }

    /** COBOL paragraph: OORH-010 */
    private void openOrdhfWithRetry() {
        openFileWithRetry(fileSet.getOrdhf(), "ORDHF");
    }

    /** COBOL paragraph: OORD-010 */
    private void openOrddfWithRetry() {
        openFileWithRetry(fileSet.getOrddf(), "ORDDF");
    }

    /** COBOL paragraph: ACUT-010 */
    private void promptAndValidateCutoffDate() {
        ws.setWkEDate(ws.getWkDfltCut());
        log.info(" ");
        log.info(" Transactions dated before the cut-off and");
        log.info(" fully processed will be purged.");
        log.info(" Enter cut-off date YYYYMMDD");
        log.info("   (blank = {}) : ", ws.editedDisplay("WK-E-DATE"));
        ws.setWkInLine(" ");
        String stdinValWkInLine0 = Utility.readStdinLine();
        if (stdinValWkInLine0 == null || stdinValWkInLine0.trim().isEmpty()) {
            stdinValWkInLine0 = "";
        }
        ws.setWkInLine(stdinValWkInLine0);
        if (Utility.fieldEquals(ws.getWkInLine(), " ")) {
            ws.setWkCutoff(ws.getWkDfltCut());
        } else {
            if (Utility.isNumeric(
                    String.valueOf(
                            Utility.padRight(String.valueOf(ws.getWkInLine()), 8)
                                    .substring(0, 8)))) {
                ws.setWkCutoff(
                        Utility.parseNumeric(
                                        Utility.padRight(String.valueOf(ws.getWkInLine()), 8)
                                                .substring(0, 8))
                                .intValue());
            } else {
                log.info(" ** invalid date - run cancelled.");
                ws.setWkAbortFlg(1);
                return;
            }
        }
        ws.setKdFunc("VALD");
        ws.setKdDate1(ws.getWkCutoff());
        dateut(ws.getKdate());
        if (!Utility.fieldEquals(ws.getKdStatus(), "00")) {
            log.info(" ** date is not a valid calendar date.");
            ws.setWkAbortFlg(1);
        }
    }

    /** COBOL paragraph: CONF-010 */
    private void promptPurgeConfirmation() {
        ws.setWkEDate(ws.getWkCutoff());
        log.info(" ");
        log.info(" Purge deleted masters and orders < {}", ws.editedDisplay("WK-E-DATE"));
        log.info(" This physically removes records - proceed ?");
        log.info(" Confirm (Y/N) : ");
        ws.setWkConfirm(" ");
        String stdinValWkConfirm1 = Utility.readStdinLine();
        if (stdinValWkConfirm1 == null || stdinValWkConfirm1.trim().isEmpty()) {
            stdinValWkConfirm1 = "";
        }
        ws.setWkConfirm(stdinValWkConfirm1);
        if ((ws.getWkConfirm().equals("Y") || ws.getWkConfirm().equals("y"))) {
            ws.setWkPurgeFlg(1);
        } else {
            log.info(" ** cancelled by operator.");
            ws.setWkAbortFlg(1);
        }
    }

    /** COBOL paragraph: PCUS-010 */
    private void purgeCustomers() {
        log.info(" ");
        log.info(" Purging customers ...");
        ws.setEofFlg(0);
        ws.setCuCode(0);
        fileSet.getCustf().start("CU-CODE", "NOT LESS THAN");
        ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
        if (fileSet.getCustf().isInvalidKey()) {
            ws.setString("EOF-FLG", "1");
        }
        while ((ws.getEofFlg() != 1)) {
            runChain(this::purgeCustomerRecord);
        }
    }

    /** COBOL paragraph: PCUX-010 */
    private void purgeCustomerRecord() {
        fileSet.getCustf().readNext();
        ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
        if (fileSet.getCustf().isAtEnd()) {
            ws.setString("EOF-FLG", "1");
            return;
        }
        ws.setWkCuRead(ws.getWkCuRead() + 1);
        if (ws.getCuDelFlag() == 1) {
            deleteWithStatusCheck(
                    fileSet.getCustf(), "CUSTF", () -> ws.setWkCuDel(ws.getWkCuDel() + 1));
        }
    }

    /** COBOL paragraph: PPRD-010 */
    private void purgeProducts() {
        log.info(" Purging products ...");
        ws.setEofFlg(0);
        ws.setPrCode(0);
        fileSet.getProdf().start("PR-CODE", "NOT LESS THAN");
        ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
        if (fileSet.getProdf().isInvalidKey()) {
            ws.setString("EOF-FLG", "1");
        }
        while ((ws.getEofFlg() != 1)) {
            runChain(this::purgeProductRecord);
        }
    }

    /** COBOL paragraph: PPRX-010 */
    private void purgeProductRecord() {
        fileSet.getProdf().readNext();
        ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
        if (fileSet.getProdf().isAtEnd()) {
            ws.setString("EOF-FLG", "1");
            return;
        }
        ws.setWkPrRead(ws.getWkPrRead() + 1);
        if (ws.getPrDelFlag() == 1) {
            deleteWithStatusCheck(
                    fileSet.getProdf(), "PRODF", () -> ws.setWkPrDel(ws.getWkPrDel() + 1));
        }
    }

    /** COBOL paragraph: PORD-010 */
    private void purgeOrders() {
        log.info(" Purging orders ...");
        ws.setEofFlg(0);
        ws.setOhNo(0);
        fileSet.getOrdhf().start("OH-NO", "NOT LESS THAN");
        ws.trySetString("FSTS", fileSet.getOrdhf().getFileStatus());
        if (fileSet.getOrdhf().isInvalidKey()) {
            ws.setString("EOF-FLG", "1");
        }
        while ((ws.getEofFlg() != 1)) {
            runChain(this::purgeOrderRecord);
        }
    }

    /** COBOL paragraph: PORX-010 */
    private void purgeOrderRecord() {
        fileSet.getOrdhf().readNext();
        ws.trySetString("FSTS", fileSet.getOrdhf().getFileStatus());
        if (fileSet.getOrdhf().isAtEnd()) {
            ws.setString("EOF-FLG", "1");
            return;
        }
        ws.setWkOhRead(ws.getWkOhRead() + 1);
        boolean isPastCutoffAndClosed =
                ws.getOhDate() < ws.getWkCutoff()
                        && (ws.getOhStatus() == 4 || ws.getOhStatus() == 9);
        if (ws.getOhDelFlag() == 1 || isPastCutoffAndClosed) {
            runChain(this::deleteOrderAndDetails);
        }
    }

    /** COBOL paragraph: DORD-010 */
    private void deleteOrderAndDetails() {
        runChain(this::deleteOrderDetailLines);
        fileSet.getOrdhf().delete();
        ws.trySetString("FSTS", fileSet.getOrdhf().getFileStatus());
        if (fileSet.getOrdhf().isInvalidKey()) {
            ws.setKaFile("ORDHF");
            ws.setKaDetail("DELETE ORDHF failed");
            runChain(this::abortProgram);
        }
        if (Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setWkOhDel(ws.getWkOhDel() + 1);
        }
    }

    /** COBOL paragraph: DODT-010 */
    private void deleteOrderDetailLines() {
        ws.setOdNo(ws.getOhNo());
        ws.setOdLine(0);
        fileSet.getOrddf().start("OD-NO", "NOT LESS THAN");
        ws.trySetString("FSTS", fileSet.getOrddf().getFileStatus());
        if (fileSet.getOrddf().isInvalidKey()) {
            return;
        }
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            return;
        }
        while (Utility.fieldEquals(ws.getFsts(), "00") && ws.getOdNo() == ws.getOhNo()) {
            runChain(this::deleteOrderDetailRecord);
        }
    }

    /** COBOL paragraph: DODX-010 */
    private void deleteOrderDetailRecord() {
        fileSet.getOrddf().readNext();
        ws.trySetString("FSTS", fileSet.getOrddf().getFileStatus());
        if (fileSet.getOrddf().isAtEnd()) {
            ws.setFsts("10");
            return;
        }
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            return;
        }
        if (ws.getOdNo() == ws.getOhNo()) {
            deleteWithStatusCheck(
                    fileSet.getOrddf(), "ORDDF", () -> ws.setWkOdDel(ws.getWkOdDel() + 1));
        }
    }

    /** COBOL paragraph: PSUM-010 */
    private void logPurgeSummary() {
        ws.setWkEDate(ws.getWkCutoff());
        log.info(" ");
        log.info("----------------------------------------------");
        log.info(" PURGE / ARCHIVE SUMMARY");
        log.info("----------------------------------------------");
        log.info(" Cut-off date      : {}", ws.editedDisplay("WK-E-DATE"));
        logSummaryCount(" Customers read    : {}", ws.getWkCuRead());
        logSummaryCount(" Customers purged  : {}", ws.getWkCuDel());
        logSummaryCount(" Products read     : {}", ws.getWkPrRead());
        logSummaryCount(" Products purged   : {}", ws.getWkPrDel());
        logSummaryCount(" Orders read       : {}", ws.getWkOhRead());
        logSummaryCount(" Orders purged     : {}", ws.getWkOhDel());
        logSummaryCount(" Order lines purge : {}", ws.getWkOdDel());
        log.info("----------------------------------------------");
        log.info(" BT0060 completed normally.");
    }

    /** COBOL paragraph: TERM-010 */
    private void closeAllFiles() {
        fileSet.getCustf().close();
        ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
        fileSet.getProdf().close();
        ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
        fileSet.getOrdhf().close();
        ws.trySetString("FSTS", fileSet.getOrdhf().getFileStatus());
        fileSet.getOrddf().close();
        ws.trySetString("FSTS", fileSet.getOrddf().getFileStatus());
    }

    /** COBOL paragraph: ABND-010 */
    private void abortProgram() {
        ws.setKaProgid("BT0060");
        ws.setKaFsts(ws.getFsts());
        ws.setKaMsgcode("EBATCH");
        abortx(ws.getKabend());
        closeAllFiles();
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
     * Open a file for I/O, retrying via a create-then-reopen cycle when the file does not yet
     * exist; aborts on any other open failure.
     */
    private void openFileWithRetry(RawDatasetBase file, String fileCode) {
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
            ws.setKaFile(fileCode);
            ws.setKaDetail("Open " + fileCode + " failed");
            runChain(this::abortProgram);
        }
    }

    /**
     * Delete the current record and run the success callback if it succeeded, otherwise abort the
     * program.
     */
    private void deleteWithStatusCheck(RawDatasetBase file, String fileCode, Runnable onSuccess) {
        file.delete();
        ws.trySetString("FSTS", file.getFileStatus());
        if (file.isInvalidKey()) {
            /* CONTINUE */
        }
        if (Utility.fieldEquals(ws.getFsts(), "00")) {
            onSuccess.run();
        } else {
            ws.setKaFile(fileCode);
            ws.setKaDetail("DELETE " + fileCode + " failed");
            runChain(this::abortProgram);
        }
    }

    /** Set the summary count field and log it using the caller-supplied labeled format. */
    private void logSummaryCount(String format, int count) {
        ws.setWkECnt(count);
        log.info(format, ws.editedDisplay("WK-E-CNT"));
    }
}
