package com.sakura.oe0050.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.oe0050.domain.Oe0050FieldAccess;
import com.sakura.oe0050.domain.WorkingStorage;
import com.sakura.oe0050.runtime.Oe0050Datasets;
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

/** Business logic service generated from COBOL program OE0050. */
@Service
@Scope("prototype")
public class Oe0050Service extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final Oe0050Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Oe0050FieldAccess ws;

    public Oe0050Service(
            Oe0050Datasets fileSet, DateutService dateutService, AbortxService abortxService) {
        this.fileSet = fileSet;
        this.ws = new Oe0050FieldAccess(new WorkingStorage(), fileSet);
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
        runChain(this::readOrderRangeParameters);
        runChain(this::printOrderReport);
        runChain(this::finalizeProgram);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeProgram() {
        ws.setWkProgid("OE0050");
        ws.setWkTitle("ORDER ACKNOWLEDGEMENT / PICKING LIST");
        ws.setKdFunc("TODY");
        dateut(ws.getKdate());
        ws.setWkSysdate(ws.getKdDate1());
        ws.setWkCompany("SAKURA Sales Management System");
        openFileChecked(fileSet.getSyscf(), FileOpenMode.INPUT);
        if (Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setSyKey(1);
            readByFallbackKey(fileSet.getSyscf(), "SY-KEY");
            if (!fileSet.getSyscf().isInvalidKey()) {
                ws.setWkCompany(ws.getSyCompanyName());
            }
            closeFileChecked(fileSet.getSyscf());
        }
        openFileChecked(fileSet.getOrdhf(), FileOpenMode.INPUT);
        if (!Utility.fieldEquals(ws.getFsts(), "00")
                && !Utility.fieldEquals(ws.getFsts(), "35")
                && !Utility.fieldEquals(ws.getFsts(), "30")) {
            ws.setKaFile("ORDHF");
            runChain(this::abortOnFileOpenError);
        }
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setWkMainEof(1);
        }
        openFileChecked(fileSet.getOrddf(), FileOpenMode.INPUT);
        openFileChecked(fileSet.getCustf(), FileOpenMode.INPUT);
        openFileChecked(fileSet.getProdf(), FileOpenMode.INPUT);
        openFileChecked(fileSet.getRepf(), FileOpenMode.OUTPUT);
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("REPF");
            runChain(this::abortOnFileOpenError);
        }
    }

    /** COBOL paragraph: PARM-010 */
    private void readOrderRangeParameters() {
        log.info("OE0050 - Order acknowledgement / picking list");
        log.info("From order number (blank = lowest) : ");
        String stdinValWkInFrom1 = Utility.readStdinLine();
        if (stdinValWkInFrom1 == null || stdinValWkInFrom1.trim().isEmpty()) {
            stdinValWkInFrom1 = "";
        }
        ws.setWkInFrom(stdinValWkInFrom1);
        log.info("To   order number (blank = highest): ");
        String stdinValWkInTo2 = Utility.readStdinLine();
        if (stdinValWkInTo2 == null || stdinValWkInTo2.trim().isEmpty()) {
            stdinValWkInTo2 = "";
        }
        ws.setWkInTo(stdinValWkInTo2);
        if (Utility.isNumeric(String.valueOf(ws.getWkInFrom()))
                && !Utility.fieldEquals(ws.getWkInFrom(), " ")) {
            ws.setWkOrdFrom(Utility.parseNumeric(ws.getWkInFrom()).longValue());
        } else {
            ws.setWkOrdFrom(0);
        }
        if (Utility.isNumeric(String.valueOf(ws.getWkInTo()))
                && !Utility.fieldEquals(ws.getWkInTo(), " ")) {
            ws.setWkOrdTo(Utility.parseNumeric(ws.getWkInTo()).longValue());
        } else {
            ws.setWkOrdTo(9999999999L);
        }
    }

    /** COBOL paragraph: PRT-010 */
    private void printOrderReport() {
        ws.setWkOrdCnt(0);
        ws.setWkLinCnt(0);
        ws.setWkLine(99);
        if ((ws.getWkMainEof() != 1)) {
            ws.setOhNo(ws.getWkOrdFrom());
            fileSet.getOrdhf().start("OH-NO", "NOT LESS THAN");
            ws.trySetString("FSTS", fileSet.getOrdhf().getFileStatus());
            if (fileSet.getOrdhf().isInvalidKey()) {
                ws.setWkMainEof(1);
            }
        }
        while ((ws.getWkMainEof() != 1)) {
            runChain(this::readNextOrderHeader);
        }
        runChain(this::printReportGrandTotal);
    }

    /** COBOL paragraph: RH-010 */
    private void readNextOrderHeader() {
        fileSet.getOrdhf().readNext();
        ws.trySetString("FSTS", fileSet.getOrdhf().getFileStatus());
        if (fileSet.getOrdhf().isAtEnd()) {
            ws.setWkMainEof(1);
        }
        if (!fileSet.getOrdhf().isAtEnd()) {
            if (ws.getOhNo() > ws.getWkOrdTo()) {
                ws.setWkMainEof(1);
            } else {
                runChain(this::processOrderHeader);
            }
        }
    }

    /** COBOL paragraph: PO-010 */
    private void processOrderHeader() {
        if (ws.getOhDelFlag() == 1) {
            return;
        }
        runChain(this::checkPageBreakForHeader);
        runChain(this::lookupCustomerName);
        runChain(this::mapOrderStatusText);
        ws.setWkOrdNet(BigDecimal.ZERO);
        ws.setO1No(ws.getOhNo());
        ws.setO1Date(ws.getOhDate());
        ws.setO1Due(ws.getOhDueDate());
        ws.setO1Stat(ws.getWkStatTxt());
        ws.copyRepRecFromRdOrd1();
        writeReportLine();
        ws.setO2Cust(ws.getOhCust());
        ws.setO2Cname(ws.getWkCustName());
        ws.setO2Staff(ws.getOhStaff());
        ws.setO2Whse(ws.getOhWhse());
        ws.setO2Po(ws.getOhCustPo());
        ws.copyRepRecFromRdOrd2();
        writeReportLine();
        ws.copyRepRecFromRdColh();
        writeReportLine();
        ws.setWkOrdCnt(ws.getWkOrdCnt() + 1);
        runChain(this::processOrderDetails);
        runChain(this::writeOrderTotalLine);
        ws.setWkGNet(ws.getWkGNet().add(ws.getWkOrdNet()));
    }

    /** COBOL paragraph: PD-010 */
    private void processOrderDetails() {
        ws.setWkDtlEof(0);
        ws.setOdNo(ws.getOhNo());
        ws.setOdLine(0);
        fileSet.getOrddf().start("OD-NO", "NOT LESS THAN");
        ws.trySetString("FSTS", fileSet.getOrddf().getFileStatus());
        if (fileSet.getOrddf().isInvalidKey()) {
            ws.setWkDtlEof(1);
        }
        while ((ws.getWkDtlEof() != 1)) {
            runChain(this::readNextOrderDetail);
        }
    }

    /** COBOL paragraph: RD-010 */
    private void readNextOrderDetail() {
        fileSet.getOrddf().readNext();
        ws.trySetString("FSTS", fileSet.getOrddf().getFileStatus());
        if (fileSet.getOrddf().isAtEnd()) {
            ws.setWkDtlEof(1);
        }
        if (!fileSet.getOrddf().isAtEnd()) {
            if (ws.getOdNo() != ws.getOhNo()) {
                ws.setWkDtlEof(1);
            } else {
                runChain(this::writeOrderDetailLine);
            }
        }
    }

    /** COBOL paragraph: POD-010 */
    private void writeOrderDetailLine() {
        runChain(this::checkPageBreak);
        runChain(this::lookupProductName);
        ws.setWkOutstand(ws.getOdQty().subtract(ws.getOdShippedQty()).intValue());
        ws.setDLine(ws.getOdLine());
        ws.setDProd(ws.getOdProd());
        ws.setDName(ws.getWkProdName());
        ws.setDQty(ws.getOdQty().longValue());
        ws.setDPrice(ws.getOdUnitPrice());
        ws.setDAmt(ws.getOdAmount().longValue());
        ws.setDOut(ws.getWkOutstand());
        ws.copyRepRecFromRdDtl();
        writeReportLine();
        ws.setWkLinCnt(ws.getWkLinCnt() + 1);
        ws.setWkOrdNet(ws.getWkOrdNet().add(ws.getOdAmount()));
    }

    /** COBOL paragraph: POT-010 */
    private void writeOrderTotalLine() {
        runChain(this::checkPageBreak);
        ws.setOtNet(ws.getWkOrdNet().longValue());
        ws.copyRepRecFromRdOtot();
        writeReportLine();
        ws.setRepRec(" ");
        writeReportLine();
    }

    /** COBOL paragraph: LKC-010 */
    private void lookupCustomerName() {
        ws.setWkCustName(" ");
        ws.setCuCode(ws.getOhCust());
        readByFallbackKey(fileSet.getCustf(), "CU-CODE");
        if (fileSet.getCustf().isInvalidKey()) {
            ws.setWkCustName("(unknown)");
        }
        if (!fileSet.getCustf().isInvalidKey()) {
            ws.setWkCustName(ws.getCuName());
        }
    }

    /** COBOL paragraph: LKP-010 */
    private void lookupProductName() {
        ws.setWkProdName(" ");
        ws.setPrCode(ws.getOdProd());
        readByFallbackKey(fileSet.getProdf(), "PR-CODE");
        if (fileSet.getProdf().isInvalidKey()) {
            ws.setWkProdName("(unknown)");
        }
        if (!fileSet.getProdf().isInvalidKey()) {
            ws.setWkProdName(ws.getPrName());
        }
    }

    /** COBOL paragraph: SST-010 */
    private void mapOrderStatusText() {
        switch (ws.getOhStatus()) {
            case 0 -> {
                ws.setWkStatTxt("Entered");
            }
            case 1 -> {
                ws.setWkStatTxt("Allocated");
            }
            case 2 -> {
                ws.setWkStatTxt("Part-ship");
            }
            case 3 -> {
                ws.setWkStatTxt("Shipped");
            }
            case 4 -> {
                ws.setWkStatTxt("Invoiced");
            }
            case 9 -> {
                ws.setWkStatTxt("Cancelled");
            }
            default -> {
                ws.setWkStatTxt("?");
            }
        }
    }

    /** COBOL paragraph: CPH-010 */
    private void checkPageBreakForHeader() {
        if (ws.getWkLine() >= 50) {
            runChain(this::printReportHeader);
        }
    }

    /** COBOL paragraph: CP-010 */
    private void checkPageBreak() {
        if (ws.getWkLine() >= 55) {
            runChain(this::printReportHeader);
        }
    }

    /** COBOL paragraph: PH-010 */
    private void printReportHeader() {
        ws.setWkPage(ws.getWkPage() + 1);
        ws.setH1Company(ws.getWkCompany());
        ws.setH1Title(ws.getWkTitle());
        ws.setH1Page(ws.getWkPage());
        ws.copyRepRecFromRptH1();
        writeReportRecord();
        ws.setH2Date(ws.getWkSysdate());
        ws.setH2Info(" ");
        {
            StringBuilder sb = new StringBuilder();
            sb.append(String.valueOf("ORDER RANGE "));
            sb.append(String.valueOf(String.format("%010d", (long) (ws.getWkOrdFrom()))));
            sb.append(String.valueOf(" - "));
            sb.append(String.valueOf(String.format("%010d", (long) (ws.getWkOrdTo()))));
            ws.setH2Info(sb.toString());
        }
        ws.copyRepRecFromRptH2();
        writeReportRecord();
        ws.setRepRec(String.valueOf(ws.getRptRule()));
        writeReportRecord();
        ws.setWkLine(5);
    }

    /** COBOL paragraph: PG-010 */
    private void printReportGrandTotal() {
        if (ws.getWkOrdCnt() == 0) {
            runChain(this::checkPageBreakForHeader);
            ws.setRepRec("*** NO ORDERS IN THE SELECTED RANGE ***");
            writeReportRecord();
            return;
        }
        runChain(this::checkPageBreak);
        ws.setRepRec(String.valueOf(ws.getRptRule()));
        writeReportRecord();
        ws.setGtNet(ws.getWkGNet().longValue());
        ws.setGtOrd(ws.getWkOrdCnt());
        ws.copyRepRecFromRdGrand();
        writeReportRecord();
    }

    /** COBOL paragraph: TERM-010 */
    private void finalizeProgram() {
        closeFileChecked(fileSet.getOrdhf());
        closeFileChecked(fileSet.getOrddf());
        closeFileChecked(fileSet.getCustf());
        closeFileChecked(fileSet.getProdf());
        closeFileChecked(fileSet.getRepf());
        log.info(
                "OE0050 complete.  Orders = {}  Lines = {}",
                String.format("%07d", (long) (ws.getWkOrdCnt())),
                String.format("%07d", (long) (ws.getWkLinCnt())));
        ws.setCompletionCode(0);
    }

    /** COBOL paragraph: AB-010 */
    private void abortOnFileOpenError() {
        ws.setKaProgid("OE0050");
        ws.setKaFsts(ws.getFsts());
        ws.setKaMsgcode("EOPEN ");
        ws.setKaDetail("Report file error");
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

    /** Open a dataset in the given mode and shadow its file status into FSTS. */
    private void openFileChecked(RawDatasetBase file, FileOpenMode mode) {
        file.open(mode);
        ws.trySetString("FSTS", file.getFileStatus());
    }

    /** Close a dataset and shadow its file status into FSTS. */
    private void closeFileChecked(RawDatasetBase file) {
        file.close();
        ws.trySetString("FSTS", file.getFileStatus());
    }

    /**
     * Resolve the fallback lookup key (screen value, else current record key) and read the file by
     * it.
     */
    private void readByFallbackKey(RawDatasetBase file, String keyField) {
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

    /** Write the current report record and advance the line counter. */
    private void writeReportLine() {
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        ws.setWkLine(ws.getWkLine() + 1);
    }

    /** Write the current report record without advancing the line counter. */
    private void writeReportRecord() {
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
    }
}
