package com.sakura.pu0030.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.numgen.service.NumgenService;
import com.sakura.pu0030.domain.Pu0030FieldAccess;
import com.sakura.pu0030.domain.WorkingStorage;
import com.sakura.pu0030.runtime.Pu0030Datasets;
import com.sakura.pu0030.screen.ScreenDefs;
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
import com.sakura.runtime.linkage.TaxcalLinkParm;
import com.sakura.runtime.record.RawDatasetBase;
import com.sakura.taxcal.service.TaxcalService;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/** Business logic service generated from COBOL program PU0030. */
@Service
@Scope("prototype")
public class Pu0030Service extends BatchServiceBase {
    /** Divisor to truncate a YYYYMMDD date down to its YYYYMM portion. */
    private static final int DATE_TO_YM_DIVISOR = 100;

    /** Shared file instances for all FD files in this program. */
    private final Pu0030Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL TAXCAL. */
    private TaxcalService taxcalService;

    /** Injected service for COBOL CALL NUMGEN. */
    private NumgenService numgenService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Pu0030FieldAccess ws;

    public Pu0030Service(
            Pu0030Datasets fileSet,
            DateutService dateutService,
            TaxcalService taxcalService,
            NumgenService numgenService,
            AbortxService abortxService,
            ScreenRendererInstance renderer) {
        this.fileSet = fileSet;
        this.ws = new Pu0030FieldAccess(new WorkingStorage(), fileSet);
        this.dateutService = dateutService;
        this.taxcalService = taxcalService;
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
        if (taxcalService instanceof ScreenRendererAware rra) {
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
            runChain(this::runMainScreenCycle);
        }
        runChain(this::closeAllFiles);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeProgram() {
        ws.setWkProgid("PU0030");
        ws.setWkTitle("Purchase Entry");
        ws.setWkFkeyLine("ENTER=Next  PF3=End");
        ws.setKdFunc("TODY");
        dateut(ws.getKdate());
        ws.setWkSysymd(ws.getKdDate1());
        ws.setWkSysdate(ws.getKdDate1());
        ws.setWkSysYm((ws.getWkSysdate() / DATE_TO_YM_DIVISOR));
        runChain(this::openAllFiles);
    }

    /** COBOL paragraph: OPENF-010 */
    private void openAllFiles() {
        runChain(this::openReceivingHeaderFile);
        runChain(this::openVoucherHeaderFile);
        runChain(this::openVoucherDetailFile);
        runChain(this::openApLedgerFile);
        runChain(this::openSupplierFile);
        fileSet.getRcvdf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getRcvdf().getFileStatus());
        fileSet.getProdf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
    }

    /** COBOL paragraph: OFRH-010 */
    private void openReceivingHeaderFile() {
        openFileWithRetry(fileSet.getRcvhf(), "RCVHF");
    }

    /** COBOL paragraph: OFVH-010 */
    private void openVoucherHeaderFile() {
        openFileWithRetry(fileSet.getPurhf(), "PURHF");
    }

    /** COBOL paragraph: OFVD-010 */
    private void openVoucherDetailFile() {
        openFileWithRetry(fileSet.getPurdf(), "PURDF");
    }

    /** COBOL paragraph: OFPL-010 */
    private void openApLedgerFile() {
        openFileWithRetry(fileSet.getAplf(), "APLF");
    }

    /** COBOL paragraph: OFSP-010 */
    private void openSupplierFile() {
        openFileWithRetry(fileSet.getSuppf(), "SUPPF");
    }

    /** COBOL paragraph: MAIN-010 */
    private void runMainScreenCycle() {
        runChain(this::clearWorkingFields);
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEADER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-FOOTER"), ws);
        ws.setWkMsgLine("Enter the receiving number to book as purchase");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-KEY"), ws);
        String scValWkRecvKey0 =
                Utility.acceptScreen(
                        "WK-RECV-KEY",
                        () -> renderer.acceptField(ScreenDefs.getInput("WK-RECV-KEY")));
        ws.setWkRecvKey(Utility.parseLongOr(scValWkRecvKey0.trim(), 0L));
        broadcastEstsStatus();
        switch (String.valueOf(ws.getEsts())) {
            case "03" -> {
                ws.setString("END-FLG", "1");
            }
            case "00" -> {
                runChain(this::processReceivingBooking);
            }
            default -> {
                ws.setWkMsgLine("Invalid function key");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: CLRP-010 */
    private void clearWorkingFields() {
        fileSet.getPurhf().setRecord();
        ws.setWkRecvKey(0);
        ws.setWkVhNoD(0);
        ws.setWkLcnt(0);
        ws.setWkNetTotal(0);
        ws.setWkTaxTotal(0);
        ws.setWkGrsTotal(0);
        ws.setWkNewBal(0);
        ws.setRcvOk(0);
        ws.setWkMoreFlg(0);
        ws.setWkSuppName(" ");
        ws.setWkConfirm(" ");
        ws.setVhDate(ws.getWkSysdate());
        ws.setVhTaxType(1);
    }

    /** COBOL paragraph: PRC-010 */
    private void processReceivingBooking() {
        if (ws.getWkRecvKey() == 0) {
            ws.setWkMsgLine("Receiving number required");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setRhNo(ws.getWkRecvKey());
        readByKeyField(fileSet.getRcvhf(), "RH-NO");
        if (fileSet.getRcvhf().isInvalidKey()) {
            ws.setWkMsgLine("Receiving not found");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        if (ws.getRhDelFlag() == 1) {
            ws.setWkMsgLine("Receiving is deleted");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        if (ws.getRhStatus() == 9) {
            ws.setWkMsgLine("Receiving is cancelled");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        if (ws.getRhStatus() == 1) {
            ws.setWkMsgLine("Receiving already booked");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        runChain(this::loadSupplierName);
        runChain(this::loadReceivingLines);
        if (ws.getWkLcnt() == 0) {
            ws.setWkMsgLine("No detail lines on this receiving");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setRcvOk(1);
        runChain(this::setVoucherHeaderDefaults);
        runChain(this::acceptVoucherHeaderFields);
        if ((ws.getRcvOk() != 1)) {
            return;
        }
        runChain(this::computeVoucherTax);
        runChain(this::confirmAndSavePurchase);
    }

    /** COBOL paragraph: LSN-010 */
    private void loadSupplierName() {
        ws.setWkSuppName(" ");
        ws.setSpCode(ws.getRhSupp());
        readByKeyField(fileSet.getSuppf(), "SP-CODE");
        if (fileSet.getSuppf().isInvalidKey()) {
            ws.setWkSuppName("??? unknown supplier");
        }
        if (!fileSet.getSuppf().isInvalidKey()) {
            ws.setWkSuppName(ws.getSpName());
            if (ws.getSpTaxType() != 0) {
                ws.setVhTaxType(ws.getSpTaxType());
            }
        }
    }

    /** COBOL paragraph: LRL-010 */
    private void loadReceivingLines() {
        ws.setWkLcnt(0);
        ws.setWkNetTotal(0);
        ws.setRdNo(ws.getRhNo());
        ws.setRdLine(0);
        fileSet.getRcvdf().start("RD-NO", "NOT LESS THAN");
        ws.trySetString("FSTS", fileSet.getRcvdf().getFileStatus());
        if (fileSet.getRcvdf().isInvalidKey()) {
            return;
        }
        ws.setEofFlg(0);
        while ((ws.getEofFlg() != 1)) {
            fileSet.getRcvdf().readNext();
            ws.trySetString("FSTS", fileSet.getRcvdf().getFileStatus());
            if (fileSet.getRcvdf().isAtEnd()) {
                ws.setString("EOF-FLG", "1");
            }
            if (!fileSet.getRcvdf().isAtEnd()) {
                if (ws.getRdNo() != ws.getRhNo()) {
                    ws.setString("EOF-FLG", "1");
                } else {
                    runChain(this::appendReceivingLine);
                }
            }
        }
    }

    /** COBOL paragraph: APL2-010 */
    private void appendReceivingLine() {
        if (ws.getWkLcnt() >= 200) {
            return;
        }
        if (ws.getRdQty().signum() == 0) {
            return;
        }
        ws.setWkLcnt(ws.getWkLcnt() + 1);
        ws.setWlProd(ws.getWkLcnt(), ws.getRdProd());
        ws.setWlWhse(ws.getWkLcnt(), ws.getRdWhse());
        ws.setWlQty(ws.getWkLcnt(), ws.getRdQty().intValue());
        ws.setWlCost(ws.getWkLcnt(), ws.getRdUnitCost());
        ws.setWlAmt(ws.getWkLcnt(), ws.getRdAmount().longValue());
        ws.setWkNetTotal(
                (BigDecimal.valueOf(ws.getWkNetTotal()).add(ws.getRdAmount())).longValue());
        runChain(this::lookupProductName);
    }

    /** COBOL paragraph: LKP-010 */
    private void lookupProductName() {
        ws.setWlName(ws.getWkLcnt(), " ");
        ws.setWlTaxcat(ws.getWkLcnt(), 1);
        ws.setPrCode(ws.getRdProd());
        readByKeyField(fileSet.getProdf(), "PR-CODE");
        if (fileSet.getProdf().isInvalidKey()) {
            ws.setWlName(ws.getWkLcnt(), "??? unknown product");
        }
        if (!fileSet.getProdf().isInvalidKey()) {
            ws.setWlName(ws.getWkLcnt(), ws.getPrName());
            ws.setWlTaxcat(ws.getWkLcnt(), ws.getPrTaxCategory());
        }
    }

    /** COBOL paragraph: SHD-010 */
    private void setVoucherHeaderDefaults() {
        if (ws.getVhTaxType() == 0) {
            ws.setVhTaxType(1);
        }
        ws.setVhDate(ws.getWkSysdate());
    }

    /** COBOL paragraph: AVH-010 */
    private void acceptVoucherHeaderFields() {
        displayReceivingLinesScreen();
        ws.setWkMsgLine("Enter purchase date / tax type - PF3 cancel");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-VHEAD"), ws);
        String scValVhDate4 =
                Utility.acceptScreen(
                        "VH-DATE", () -> renderer.acceptField(ScreenDefs.getInput("VH-DATE")));
        ws.setVhDate(Utility.parseIntOr(scValVhDate4.trim(), 0));
        String scValVhTaxType5 =
                Utility.acceptScreen(
                        "VH-TAX-TYPE",
                        () -> renderer.acceptField(ScreenDefs.getInput("VH-TAX-TYPE")));
        ws.setVhTaxType(Utility.parseIntOr(scValVhTaxType5.trim(), 0));
        broadcastEstsStatus();
        if (Utility.fieldEquals(ws.getEsts(), "03")) {
            ws.setRcvOk(0);
            ws.setWkMsgLine("Cancelled");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
    }

    /** COBOL paragraph: LRV-010 */
    private void buildLineRowBuffers() {
        for (ws.setWkIdx(1); ws.getWkIdx() <= 9; ws.setWkIdx(ws.getWkIdx() + 1)) {
            ws.setWrBuf(ws.getWkIdx(), " ");
        }
        ws.setWkMoreFlg(0);
        for (ws.setWkIdx(1); ws.getWkIdx() <= ws.getWkLcnt(); ws.setWkIdx(ws.getWkIdx() + 1)) {
            if (ws.getWkIdx() > 9) {
                ws.setWkMoreFlg(1);
            } else {
                ws.setRbProd(ws.getWlProd(ws.getWkIdx()));
                ws.setRbName(ws.getWlName(ws.getWkIdx()));
                ws.setRbQty(ws.getWlQty(ws.getWkIdx()));
                ws.setRbCost(ws.getWlCost(ws.getWkIdx()));
                ws.setRbAmt(ws.getWlAmt(ws.getWkIdx()));
                ws.setWrBuf(ws.getWkIdx(), ws.getWkRowBuf());
            }
        }
    }

    /** COBOL paragraph: CTT-010 */
    private void computeVoucherTax() {
        ws.setKtCategory(1);
        ws.setKtTaxType(ws.getVhTaxType());
        ws.setKtRound(1);
        ws.setKtDate(ws.getVhDate());
        ws.setKtAmount(BigDecimal.valueOf(ws.getWkNetTotal()));
        taxcal(ws.getKtax());
        if (Utility.fieldEquals(ws.getKtStatus(), "00")) {
            ws.setWkNetTotal(ws.getKtNet().longValue());
            ws.setWkTaxTotal(ws.getKtTax().longValue());
            ws.setWkGrsTotal(ws.getKtGross().longValue());
        } else {
            ws.setWkTaxTotal(0);
            ws.setWkGrsTotal(ws.getWkNetTotal());
        }
    }

    /** COBOL paragraph: CSAV-010 */
    private void confirmAndSavePurchase() {
        ws.setWkConfirm(" ");
        displayReceivingLinesScreen();
        renderer.displayScreen(ScreenDefs.getScreen("DS-TOTAL"), ws);
        ws.setWkMsgLine("Confirm to book the purchase");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-CONFIRM"), ws);
        String scValWkConfirm6 =
                Utility.acceptScreen(
                        "WK-CONFIRM",
                        () -> renderer.acceptField(ScreenDefs.getInput("WK-CONFIRM")));
        ws.setWkConfirm(scValWkConfirm6);
        broadcastEstsStatus();
        if ((ws.getWkConfirm().equals("Y") || ws.getWkConfirm().equals("y"))) {
            runChain(this::savePurchaseVoucher);
        } else {
            ws.setWkMsgLine("Purchase discarded");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
    }

    /** COBOL paragraph: SPU-010 */
    private void savePurchaseVoucher() {
        ws.setKnumKey("PURCH");
        numgen(ws.getKnum());
        if (!Utility.fieldEquals(ws.getKnumStatus(), "00")) {
            ws.setWkMsgLine("Purchase number assignment failed");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setVhNo(ws.getKnumNumber());
        ws.setWkVhNoD(ws.getKnumNumber());
        ws.setVhSupp(ws.getRhSupp());
        ws.setVhRecvNo(ws.getRhNo());
        ws.setVhCloseYm((ws.getVhDate() / DATE_TO_YM_DIVISOR));
        ws.setVhAmount(BigDecimal.valueOf(ws.getWkNetTotal()));
        ws.setVhTaxAmount(BigDecimal.valueOf(ws.getWkTaxTotal()));
        ws.setVhTotal(BigDecimal.valueOf(ws.getWkGrsTotal()));
        ws.setVhStatus(0);
        ws.setVhKind(1);
        ws.setVhLines(ws.getWkLcnt());
        ws.setVhRemark(" ");
        ws.setVhAddDate(ws.getWkSysdate());
        ws.setVhAddUser(ws.getWkUserCode());
        ws.setVhDelFlag(0);
        fileSet.getPurhf().write();
        ws.trySetString("FSTS", fileSet.getPurhf().getFileStatus());
        if (fileSet.getPurhf().isInvalidKey()) {
            ws.setWkMsgLine("Purchase header write failed");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        runChain(this::writeVoucherDetailLines);
        runChain(this::postApLedgerEntry);
        runChain(this::markReceivingBooked);
        ws.setWkMsgLine("Purchase booked");
        renderer.displayScreen(ScreenDefs.getScreen("DS-TOTAL"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
    }

    /** COBOL paragraph: WPD-010 */
    private void writeVoucherDetailLines() {
        for (ws.setWkIdx(1); ws.getWkIdx() <= ws.getWkLcnt(); ws.setWkIdx(ws.getWkIdx() + 1)) {
            fileSet.getPurdf().setRecord();
            ws.setVdNo(ws.getVhNo());
            ws.setVdLine(ws.getWkIdx());
            ws.setVdProd(ws.getWlProd(ws.getWkIdx()));
            ws.setVdWhse(ws.getWlWhse(ws.getWkIdx()));
            ws.setVdQty(BigDecimal.valueOf(ws.getWlQty(ws.getWkIdx())));
            ws.setVdUnitCost(ws.getWlCost(ws.getWkIdx()));
            ws.setVdAmount(BigDecimal.valueOf(ws.getWlAmt(ws.getWkIdx())));
            ws.setVdTaxCategory(ws.getWlTaxcat(ws.getWkIdx()));
            fileSet.getPurdf().write();
            ws.trySetString("FSTS", fileSet.getPurdf().getFileStatus());
            if (fileSet.getPurdf().isInvalidKey()) {
                /* CONTINUE */
            }
        }
    }

    /** COBOL paragraph: PAP-010 */
    private void postApLedgerEntry() {
        ws.setKnumKey("APLDG");
        numgen(ws.getKnum());
        if (!Utility.fieldEquals(ws.getKnumStatus(), "00")) {
            ws.setWkMsgLine("AP ledger number assignment failed");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setSpCode(ws.getRhSupp());
        readByKeyField(fileSet.getSuppf(), "SP-CODE");
        if (fileSet.getSuppf().isInvalidKey()) {
            ws.setSpBalance(BigDecimal.ZERO);
        }
        ws.setWkNewBal(ws.getSpBalance().add(BigDecimal.valueOf(ws.getWkGrsTotal())).longValue());
        fileSet.getAplf().setRecord();
        ws.setPlSeq(ws.getKnumNumber());
        ws.setPlSupp(ws.getRhSupp());
        ws.setPlDate(ws.getVhDate());
        ws.setPlCloseYm(ws.getVhCloseYm());
        ws.setPlKind(1);
        ws.setPlRefType(23);
        ws.setPlRefNo(ws.getVhNo());
        ws.setPlDebit(BigDecimal.ZERO);
        ws.setPlCredit(BigDecimal.valueOf(ws.getWkGrsTotal()));
        ws.setPlBalance(BigDecimal.valueOf(ws.getWkNewBal()));
        ws.setPlRemark("Purchase");
        ws.setPlUser(ws.getWkUserCode());
        fileSet.getAplf().write();
        ws.trySetString("FSTS", fileSet.getAplf().getFileStatus());
        if (fileSet.getAplf().isInvalidKey()) {
            ws.setWkMsgLine("AP ledger write failed");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
        runChain(this::updateSupplierBalance);
    }

    /** COBOL paragraph: USB-010 */
    private void updateSupplierBalance() {
        ws.setSpCode(ws.getRhSupp());
        readByKeyField(fileSet.getSuppf(), "SP-CODE");
        if (fileSet.getSuppf().isInvalidKey()) {
            return;
        }
        ws.setSpBalance(BigDecimal.valueOf(ws.getWkNewBal()));
        ws.setSpUpdDate(ws.getWkSysdate());
        ws.setSpUpdUser(ws.getWkUserCode());
        fileSet.getSuppf().rewrite();
        ws.trySetString("FSTS", fileSet.getSuppf().getFileStatus());
        if (fileSet.getSuppf().isInvalidKey()) {
            /* CONTINUE */
        }
    }

    /** COBOL paragraph: MRB-010 */
    private void markReceivingBooked() {
        ws.setRhNo(ws.getWkRecvKey());
        readByKeyField(fileSet.getRcvhf(), "RH-NO");
        if (fileSet.getRcvhf().isInvalidKey()) {
            return;
        }
        ws.setRhStatus(1);
        fileSet.getRcvhf().rewrite();
        ws.trySetString("FSTS", fileSet.getRcvhf().getFileStatus());
        if (fileSet.getRcvhf().isInvalidKey()) {
            /* CONTINUE */
        }
    }

    /** COBOL paragraph: TERM-010 */
    private void closeAllFiles() {
        fileSet.getRcvhf().close();
        ws.trySetString("FSTS", fileSet.getRcvhf().getFileStatus());
        fileSet.getRcvdf().close();
        ws.trySetString("FSTS", fileSet.getRcvdf().getFileStatus());
        fileSet.getPurhf().close();
        ws.trySetString("FSTS", fileSet.getPurhf().getFileStatus());
        fileSet.getPurdf().close();
        ws.trySetString("FSTS", fileSet.getPurdf().getFileStatus());
        fileSet.getAplf().close();
        ws.trySetString("FSTS", fileSet.getAplf().getFileStatus());
        fileSet.getSuppf().close();
        ws.trySetString("FSTS", fileSet.getSuppf().getFileStatus());
        fileSet.getProdf().close();
        ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
    }

    /** COBOL paragraph: ABEND-010 */
    private void abortOnFileError() {
        ws.setKaProgid("PU0030");
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

    /** COBOL CALL TAXCAL — delegates to injected TaxcalService. */
    private void taxcal(Object... args) {
        TaxcalLinkParm params = new TaxcalLinkParm();
        params.getKtax().setKtCategory(ws.getKtCategory());
        params.getKtax().setKtTaxType(ws.getKtTaxType());
        params.getKtax().setKtRound(ws.getKtRound());
        params.getKtax().setKtDate(ws.getKtDate());
        params.getKtax().setKtAmount(ws.getKtAmount());
        params.getKtax().setKtTax(ws.getKtTax());
        params.getKtax().setKtNet(ws.getKtNet());
        params.getKtax().setKtGross(ws.getKtGross());
        params.getKtax().setKtRate(ws.getKtRate());
        params.getKtax().setKtStatus(ws.getKtStatus());
        taxcalService.execute(params);
        ws.setKtCategory(params.getKtax().getKtCategory());
        ws.setKtTaxType(params.getKtax().getKtTaxType());
        ws.setKtRound(params.getKtax().getKtRound());
        ws.setKtDate(params.getKtax().getKtDate());
        ws.setKtAmount(params.getKtax().getKtAmount());
        ws.setKtTax(params.getKtax().getKtTax());
        ws.setKtNet(params.getKtax().getKtNet());
        ws.setKtGross(params.getKtax().getKtGross());
        ws.setKtRate(params.getKtax().getKtRate());
        ws.setKtStatus(params.getKtax().getKtStatus());
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
     * Opens a file for I/O, creating it first if not-found, then aborts the program on any other
     * failure.
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
            runChain(this::abortOnFileError);
        }
    }

    /** Resolves a key value from ws or the current record, then reads the file by that key. */
    private void readByKeyField(RawDatasetBase file, String keyFieldName) {
        String keyVal = "";
        if (keyVal == null || keyVal.trim().isEmpty()) {
            try {
                keyVal = ws.getString(keyFieldName);
            } catch (Exception _e) {
            }
        }
        if (keyVal == null || keyVal.trim().isEmpty()) {
            try {
                keyVal = file.extractKeyFromCurrentRecord();
            } catch (Exception _e3) {
            }
        }
        file.readByKey(keyVal != null ? keyVal.trim() : "");
        ws.trySetString("FSTS", file.getFileStatus());
    }

    /** Renders the header, footer, receiving-info banner, and line-item grid. */
    private void displayReceivingLinesScreen() {
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEADER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-FOOTER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-RINFO"), ws);
        runChain(this::buildLineRowBuffers);
        renderer.displayScreen(ScreenDefs.getScreen("DS-COLHDR"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-ROWS"), ws);
    }
}
