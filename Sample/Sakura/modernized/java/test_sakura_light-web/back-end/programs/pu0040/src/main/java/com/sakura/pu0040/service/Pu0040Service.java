package com.sakura.pu0040.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.numgen.service.NumgenService;
import com.sakura.pu0040.domain.Pu0040FieldAccess;
import com.sakura.pu0040.domain.WorkingStorage;
import com.sakura.pu0040.runtime.Pu0040Datasets;
import com.sakura.pu0040.screen.ScreenDefs;
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

/** Business logic service generated from COBOL program PU0040. */
@Service
@Scope("prototype")
public class Pu0040Service extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final Pu0040Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL TAXCAL. */
    private TaxcalService taxcalService;

    /** Injected service for COBOL CALL NUMGEN. */
    private NumgenService numgenService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Pu0040FieldAccess ws;

    /** Divisor used to truncate a YYYYMMDD date to its YYYYMM portion. */
    private static final int DATE_DIVISOR_STRIP_DAY = 100;

    public Pu0040Service(
            Pu0040Datasets fileSet,
            DateutService dateutService,
            TaxcalService taxcalService,
            NumgenService numgenService,
            AbortxService abortxService,
            ScreenRendererInstance renderer) {
        this.fileSet = fileSet;
        this.ws = new Pu0040FieldAccess(new WorkingStorage(), fileSet);
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
            runChain(this::processReturnEntry);
        }
        runChain(this::closeAllFiles);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeProgram() {
        ws.setWkProgid("PU0040");
        ws.setWkTitle("Purchase Return Entry");
        ws.setWkFkeyLine("ENTER=Next  PF3=End/Finish  PF4=Clear line");
        ws.setKdFunc("TODY");
        dateut(ws.getKdate());
        ws.setWkSysymd(ws.getKdDate1());
        ws.setWkSysdate(ws.getKdDate1());
        ws.setWkSysYm((ws.getWkSysdate() / DATE_DIVISOR_STRIP_DAY));
        runChain(this::openAllFiles);
    }

    /** COBOL paragraph: OPENF-010 */
    private void openAllFiles() {
        runChain(this::openPurhfWithRetry);
        runChain(this::openPurdfWithRetry);
        runChain(this::openAplfWithRetry);
        runChain(this::openStokfWithRetry);
        runChain(this::openSmovfWithRetry);
        runChain(this::openSuppfWithRetry);
        fileSet.getProdf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
    }

    /** COBOL paragraph: OFVH-010 */
    private void openPurhfWithRetry() {
        openFileWithRetry(fileSet.getPurhf(), "PURHF");
    }

    /** COBOL paragraph: OFVD-010 */
    private void openPurdfWithRetry() {
        openFileWithRetry(fileSet.getPurdf(), "PURDF");
    }

    /** COBOL paragraph: OFPL-010 */
    private void openAplfWithRetry() {
        openFileWithRetry(fileSet.getAplf(), "APLF");
    }

    /** COBOL paragraph: OFSK-010 */
    private void openStokfWithRetry() {
        openFileWithRetry(fileSet.getStokf(), "STOKF");
    }

    /** COBOL paragraph: OFSM-010 */
    private void openSmovfWithRetry() {
        openFileWithRetry(fileSet.getSmovf(), "SMOVF");
    }

    /** COBOL paragraph: OFSP-010 */
    private void openSuppfWithRetry() {
        openFileWithRetry(fileSet.getSuppf(), "SUPPF");
    }

    /** COBOL paragraph: RLOOP-010 */
    private void processReturnEntry() {
        runChain(this::clearReturnHeaderFields);
        runChain(this::acceptReturnHeader);
        if ((ws.getEndFlg() == 1)) {
            return;
        }
        if ((ws.getHdrOk() != 1)) {
            return;
        }
        while ((ws.getDtlDone() != 1)) {
            runChain(this::acceptDetailLine);
        }
        if (ws.getWkLcnt() > 0) {
            runChain(this::confirmAndSaveReturn);
        } else {
            ws.setWkMsgLine("No lines entered - return discarded");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
    }

    /** COBOL paragraph: CLRR-010 */
    private void clearReturnHeaderFields() {
        fileSet.getPurhf().setRecord();
        ws.setWkDet("");
        ws.setWkLcnt(0);
        ws.setWkNetTotal(0);
        ws.setWkTaxTotal(0);
        ws.setWkGrsTotal(0);
        ws.setWkNewBal(0);
        ws.setHdrOk(0);
        ws.setDtlDone(0);
        ws.setWkVhNoD(0);
        ws.setWkSuppName(" ");
        ws.setWkConfirm(" ");
        ws.setVhDate(ws.getWkSysdate());
        ws.setVhTaxType(1);
    }

    /** COBOL paragraph: EHDR-010 */
    private void acceptReturnHeader() {
        ws.setHdrOk(0);
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEADER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-FOOTER"), ws);
        ws.setWkMsgLine("Enter return header - PF3 to quit");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEAD"), ws);
        String scValVhDate0 =
                Utility.acceptScreen(
                        "VH-DATE", () -> renderer.acceptField(ScreenDefs.getInput("VH-DATE")));
        ws.setVhDate(Utility.parseIntOr(scValVhDate0.trim(), 0));
        String scValVhSupp1 =
                Utility.acceptScreen(
                        "VH-SUPP", () -> renderer.acceptField(ScreenDefs.getInput("VH-SUPP")));
        ws.setVhSupp(Utility.parseIntOr(scValVhSupp1.trim(), 0));
        String scValVhTaxType2 =
                Utility.acceptScreen(
                        "VH-TAX-TYPE",
                        () -> renderer.acceptField(ScreenDefs.getInput("VH-TAX-TYPE")));
        ws.setVhTaxType(Utility.parseIntOr(scValVhTaxType2.trim(), 0));
        String scValVhRemark3 =
                Utility.acceptScreen(
                        "VH-REMARK", () -> renderer.acceptField(ScreenDefs.getInput("VH-REMARK")));
        ws.setVhRemark(scValVhRemark3);
        broadcastEstsStatus();
        if (Utility.fieldEquals(ws.getEsts(), "03")) {
            if (ws.getVhSupp() == 0) {
                ws.setString("END-FLG", "1");
            }
            return;
        }
        runChain(this::validateSupplierHeader);
    }

    /** COBOL paragraph: VHDR-010 */
    private void validateSupplierHeader() {
        if (ws.getVhSupp() == 0) {
            ws.setWkMsgLine("Supplier code required");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setSpCode(ws.getVhSupp());
        readSuppfBySpCode();
        if (fileSet.getSuppf().isInvalidKey()) {
            ws.setWkMsgLine("Supplier not found");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        if (ws.getSpDelFlag() == 1) {
            ws.setWkMsgLine("Supplier is deleted");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setWkSuppName(ws.getSpName());
        if (ws.getVhTaxType() == 0) {
            ws.setVhTaxType(ws.getSpTaxType());
        }
        if (ws.getVhTaxType() == 0) {
            ws.setVhTaxType(1);
        }
        ws.setHdrOk(1);
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEAD"), ws);
        ws.setWkMsgLine("Header OK - enter return lines");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
    }

    /** COBOL paragraph: DLOOP-010 */
    private void acceptDetailLine() {
        runChain(this::clearDetailLineFields);
        renderer.displayScreen(ScreenDefs.getScreen("DS-DETAIL"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-STATUS"), ws);
        String scValWkDProd5 =
                Utility.acceptScreen(
                        "WK-D-PROD", () -> renderer.acceptField(ScreenDefs.getInput("WK-D-PROD")));
        ws.setWkDProd(Utility.parseIntOr(scValWkDProd5.trim(), 0));
        String scValWkDWhse6 =
                Utility.acceptScreen(
                        "WK-D-WHSE", () -> renderer.acceptField(ScreenDefs.getInput("WK-D-WHSE")));
        ws.setWkDWhse(Utility.parseIntOr(scValWkDWhse6.trim(), 0));
        String scValWkDQty7 =
                Utility.acceptScreen(
                        "WK-D-QTY", () -> renderer.acceptField(ScreenDefs.getInput("WK-D-QTY")));
        ws.setWkDQty(Utility.parseIntOr(scValWkDQty7.trim(), 0));
        String scValWkDCost8 =
                Utility.acceptScreen(
                        "WK-D-COST", () -> renderer.acceptField(ScreenDefs.getInput("WK-D-COST")));
        try {
            ws.setWkDCost(new BigDecimal(scValWkDCost8.trim()));
        } catch (NumberFormatException _nfe) {
            ws.setWkDCost(BigDecimal.ZERO);
        }
        broadcastEstsStatus();
        switch (String.valueOf(ws.getEsts())) {
            case "03" -> {
                ws.setString("DTL-DONE", "1");
            }
            case "04" -> {
                ws.setWkMsgLine("Line cleared");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
            case "00" -> {
                runChain(this::processDetailLine);
            }
            default -> {
                ws.setWkMsgLine("Invalid key");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: CLRD-010 */
    private void clearDetailLineFields() {
        ws.setWkDProd(0);
        ws.setWkDWhse(0);
        ws.setWkDQty(0);
        ws.setWkDCost(BigDecimal.ZERO);
        ws.setWkDAmt(0);
        ws.setWkDAvail(0);
        ws.setWkDStkmng(0);
        ws.setWkDTaxcat(1);
        ws.setWkDName(" ");
    }

    /** COBOL paragraph: PDET-010 */
    private void processDetailLine() {
        if (ws.getWkDProd() == 0) {
            ws.setWkMsgLine("Product code required");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setPrCode(ws.getWkDProd());
        String rkVal_9 = "";
        if (rkVal_9 == null || rkVal_9.trim().isEmpty()) {
            try {
                rkVal_9 = ws.getString("PR-CODE");
            } catch (Exception _e) {
            }
        }
        if (rkVal_9 == null || rkVal_9.trim().isEmpty()) {
            try {
                rkVal_9 = fileSet.getProdf().extractKeyFromCurrentRecord();
            } catch (Exception _e3) {
            }
        }
        fileSet.getProdf().readByKey(rkVal_9 != null ? rkVal_9.trim() : "");
        ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
        if (fileSet.getProdf().isInvalidKey()) {
            ws.setWkMsgLine("Product not found");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        if (ws.getPrDelFlag() == 1) {
            ws.setWkMsgLine("Product is deleted");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setWkDName(ws.getPrName());
        ws.setWkDTaxcat(ws.getPrTaxCategory());
        ws.setWkDStkmng(ws.getPrStockMng());
        if (ws.getWkDWhse() == 0) {
            ws.setWkDWhse(ws.getPrDfltWhse());
        }
        if (ws.getWkDWhse() == 0) {
            ws.setWkMsgLine("Warehouse required");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        if (ws.getWkDQty() <= 0) {
            ws.setWkMsgLine("Return qty must be positive");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        if ((ws.getWkDCost().signum() <= 0)) {
            runChain(this::resolveDefaultUnitCost);
        }
        if ((ws.getWkDCost().signum() <= 0)) {
            ws.setWkMsgLine("Unit cost required");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setWkDAmt(
                (BigDecimal.valueOf(ws.getWkDQty()).multiply(ws.getWkDCost()))
                        .setScale(0, java.math.RoundingMode.HALF_UP)
                        .longValue());
        runChain(this::checkStockAvailability);
        runChain(this::addLineToReturn);
    }

    /** COBOL paragraph: RCST-010 */
    private void resolveDefaultUnitCost() {
        ws.setSkProd(ws.getWkDProd());
        ws.setSkWhse(ws.getWkDWhse());
        readStokfByProdWhse();
        if (fileSet.getStokf().isInvalidKey()) {
            ws.setWkDCost(BigDecimal.ZERO);
        }
        if (!fileSet.getStokf().isInvalidKey()) {
            if ((ws.getSkAvgCost().signum() > 0)) {
                ws.setWkDCost(ws.getSkAvgCost());
            }
        }
        if ((ws.getWkDCost().signum() <= 0)) {
            if ((ws.getPrLastCost().signum() > 0)) {
                ws.setWkDCost(ws.getPrLastCost());
            } else {
                ws.setWkDCost(ws.getPrStdCost());
            }
        }
    }

    /** COBOL paragraph: CSTK-010 */
    private void checkStockAvailability() {
        if (ws.getWkDStkmng() == 0) {
            return;
        }
        ws.setSkProd(ws.getWkDProd());
        ws.setSkWhse(ws.getWkDWhse());
        readStokfByProdWhse();
        if (fileSet.getStokf().isInvalidKey()) {
            ws.setWkDAvail(0);
        }
        if (!fileSet.getStokf().isInvalidKey()) {
            ws.setWkDAvail(ws.getSkOnhand().intValue());
        }
        if (ws.getWkDQty() > ws.getWkDAvail()) {
            ws.setWkMsgLine("Warning: return qty exceeds on-hand stock");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
    }

    /** COBOL paragraph: ADDL-010 */
    private void addLineToReturn() {
        if (ws.getWkLcnt() >= 200) {
            ws.setWkMsgLine("Maximum 200 lines reached");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setWkLcnt(ws.getWkLcnt() + 1);
        ws.setLx(ws.getWkLcnt());
        ws.setWlProd(ws.getLx(), ws.getWkDProd());
        ws.setWlWhse(ws.getLx(), ws.getWkDWhse());
        ws.setWlQty(ws.getLx(), ws.getWkDQty());
        ws.setWlCost(ws.getLx(), ws.getWkDCost());
        ws.setWlAmount(ws.getLx(), ws.getWkDAmt());
        ws.setWlTaxcat(ws.getLx(), ws.getWkDTaxcat());
        ws.setWlStkmng(ws.getLx(), ws.getWkDStkmng());
        ws.setWkNetTotal(ws.getWkNetTotal() + ws.getWkDAmt());
        renderer.displayScreen(ScreenDefs.getScreen("DS-STATUS"), ws);
        ws.setWkMsgLine("Line added");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
    }

    /** COBOL paragraph: CSAV-010 */
    private void confirmAndSaveReturn() {
        ws.setWkConfirm(" ");
        runChain(this::computeReturnTax);
        renderer.displayScreen(ScreenDefs.getScreen("DS-STATUS"), ws);
        ws.setWkMsgLine("Review totals then confirm return");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-CONFIRM"), ws);
        String scValWkConfirm12 =
                Utility.acceptScreen(
                        "WK-CONFIRM",
                        () -> renderer.acceptField(ScreenDefs.getInput("WK-CONFIRM")));
        ws.setWkConfirm(scValWkConfirm12);
        broadcastEstsStatus();
        if ((ws.getWkConfirm().equals("Y") || ws.getWkConfirm().equals("y"))) {
            runChain(this::postPurchaseReturn);
        } else {
            ws.setWkMsgLine("Return discarded");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
    }

    /** COBOL paragraph: CTT-010 */
    private void computeReturnTax() {
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

    /** COBOL paragraph: SRT-010 */
    private void postPurchaseReturn() {
        ws.setKnumKey("PURCH");
        numgen(ws.getKnum());
        if (!Utility.fieldEquals(ws.getKnumStatus(), "00")) {
            ws.setWkMsgLine("Number assignment failed");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setVhNo(ws.getKnumNumber());
        ws.setWkVhNoD(ws.getKnumNumber());
        ws.setVhSupp(ws.getVhSupp());
        ws.setVhRecvNo(0);
        ws.setVhCloseYm((ws.getVhDate() / DATE_DIVISOR_STRIP_DAY));
        ws.setVhAmount(
                (BigDecimal.ZERO.subtract(BigDecimal.valueOf(ws.getWkNetTotal())))
                        .setScale(0, java.math.RoundingMode.DOWN));
        ws.setVhTaxAmount(
                (BigDecimal.ZERO.subtract(BigDecimal.valueOf(ws.getWkTaxTotal())))
                        .setScale(0, java.math.RoundingMode.DOWN));
        ws.setVhTotal(
                (BigDecimal.ZERO.subtract(BigDecimal.valueOf(ws.getWkGrsTotal())))
                        .setScale(0, java.math.RoundingMode.DOWN));
        ws.setVhStatus(0);
        ws.setVhKind(2);
        ws.setVhLines(ws.getWkLcnt());
        ws.setVhAddDate(ws.getWkSysdate());
        ws.setVhAddUser(ws.getWkUserCode());
        ws.setVhDelFlag(0);
        fileSet.getPurhf().write();
        ws.trySetString("FSTS", fileSet.getPurhf().getFileStatus());
        if (fileSet.getPurhf().isInvalidKey()) {
            ws.setWkMsgLine("Header write failed");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        runChain(this::writeReturnDetailLines);
        runChain(this::postApLedgerEntry);
        ws.setWkMsgLine("Purchase return posted");
        renderer.displayScreen(ScreenDefs.getScreen("DS-STATUS"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
    }

    /** COBOL paragraph: WDET-010 */
    private void writeReturnDetailLines() {
        for (ws.setLx(1); ws.getLx() <= ws.getWkLcnt(); ws.setLx(ws.getLx() + 1)) {
            fileSet.getPurdf().setRecord();
            ws.setVdNo(ws.getVhNo());
            ws.setWkIdx(ws.getLx());
            ws.setVdLine(ws.getWkIdx());
            ws.setVdProd(ws.getWlProd(ws.getLx()));
            ws.setVdWhse(ws.getWlWhse(ws.getLx()));
            ws.setVdQty(
                    (BigDecimal.ZERO.subtract(BigDecimal.valueOf(ws.getWlQty(ws.getLx()))))
                            .setScale(0, java.math.RoundingMode.DOWN));
            ws.setVdUnitCost(ws.getWlCost(ws.getLx()));
            ws.setVdAmount(
                    (BigDecimal.ZERO.subtract(BigDecimal.valueOf(ws.getWlAmount(ws.getLx()))))
                            .setScale(0, java.math.RoundingMode.DOWN));
            ws.setVdTaxCategory(ws.getWlTaxcat(ws.getLx()));
            fileSet.getPurdf().write();
            ws.trySetString("FSTS", fileSet.getPurdf().getFileStatus());
            if (fileSet.getPurdf().isInvalidKey()) {
                /* CONTINUE */
            }
            if (ws.getWlStkmng(ws.getLx()) == 1) {
                runChain(this::updateStockOnHand);
                runChain(this::writeStockMovement);
            }
        }
    }

    /** COBOL paragraph: DST-010 */
    private void updateStockOnHand() {
        ws.setSkProd(ws.getWlProd(ws.getLx()));
        ws.setSkWhse(ws.getWlWhse(ws.getLx()));
        readStokfByProdWhse();
        if (fileSet.getStokf().isInvalidKey()) {
            fileSet.getStokf().setRecord();
            ws.setSkProd(ws.getWlProd(ws.getLx()));
            ws.setSkWhse(ws.getWlWhse(ws.getLx()));
            ws.setSkOnhand(
                    (BigDecimal.ZERO.subtract(BigDecimal.valueOf(ws.getWlQty(ws.getLx()))))
                            .setScale(0, java.math.RoundingMode.DOWN));
            ws.setSkAvgCost(ws.getWlCost(ws.getLx()));
            ws.setSkLastOutDate(ws.getVhDate());
            ws.setSkYtdOut(BigDecimal.valueOf(ws.getWlQty(ws.getLx())));
            fileSet.getStokf().write();
            ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
            if (fileSet.getStokf().isInvalidKey()) {
                /* CONTINUE */
            }
        }
        if (!fileSet.getStokf().isInvalidKey()) {
            ws.setSkOnhand(ws.getSkOnhand().subtract(BigDecimal.valueOf(ws.getWlQty(ws.getLx()))));
            ws.setSkLastOutDate(ws.getVhDate());
            ws.setSkYtdOut(ws.getSkYtdOut().add(BigDecimal.valueOf(ws.getWlQty(ws.getLx()))));
            fileSet.getStokf().rewrite();
            ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
            if (fileSet.getStokf().isInvalidKey()) {
                /* CONTINUE */
            }
        }
    }

    /** COBOL paragraph: WMV-010 */
    private void writeStockMovement() {
        ws.setKnumKey("STKMOV");
        numgen(ws.getKnum());
        if (!Utility.fieldEquals(ws.getKnumStatus(), "00")) {
            return;
        }
        fileSet.getSmovf().setRecord();
        ws.setSmSeq(ws.getKnumNumber());
        ws.setSmDate(ws.getVhDate());
        ws.setSmProd(ws.getWlProd(ws.getLx()));
        ws.setSmWhse(ws.getWlWhse(ws.getLx()));
        ws.setSmKind(60);
        ws.setSmQty(
                (BigDecimal.ZERO.subtract(BigDecimal.valueOf(ws.getWlQty(ws.getLx()))))
                        .setScale(0, java.math.RoundingMode.DOWN));
        ws.setSmUnitCost(ws.getWlCost(ws.getLx()));
        ws.setSmBalAfter(ws.getSkOnhand());
        ws.setSmRefType(23);
        ws.setSmRefNo(ws.getVhNo());
        ws.setSmUser(ws.getWkUserCode());
        fileSet.getSmovf().write();
        ws.trySetString("FSTS", fileSet.getSmovf().getFileStatus());
        if (fileSet.getSmovf().isInvalidKey()) {
            /* CONTINUE */
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
        ws.setSpCode(ws.getVhSupp());
        readSuppfBySpCode();
        if (fileSet.getSuppf().isInvalidKey()) {
            ws.setSpBalance(BigDecimal.ZERO);
        }
        ws.setWkNewBal(
                ws.getSpBalance().subtract(BigDecimal.valueOf(ws.getWkGrsTotal())).longValue());
        fileSet.getAplf().setRecord();
        ws.setPlSeq(ws.getKnumNumber());
        ws.setPlSupp(ws.getVhSupp());
        ws.setPlDate(ws.getVhDate());
        ws.setPlCloseYm(ws.getVhCloseYm());
        ws.setPlKind(3);
        ws.setPlRefType(23);
        ws.setPlRefNo(ws.getVhNo());
        ws.setPlDebit(BigDecimal.valueOf(ws.getWkGrsTotal()));
        ws.setPlCredit(BigDecimal.ZERO);
        ws.setPlBalance(BigDecimal.valueOf(ws.getWkNewBal()));
        ws.setPlRemark("Purchase return");
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
        ws.setSpCode(ws.getVhSupp());
        readSuppfBySpCode();
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

    /** COBOL paragraph: TERM-010 */
    private void closeAllFiles() {
        fileSet.getPurhf().close();
        ws.trySetString("FSTS", fileSet.getPurhf().getFileStatus());
        fileSet.getPurdf().close();
        ws.trySetString("FSTS", fileSet.getPurdf().getFileStatus());
        fileSet.getAplf().close();
        ws.trySetString("FSTS", fileSet.getAplf().getFileStatus());
        fileSet.getStokf().close();
        ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
        fileSet.getSmovf().close();
        ws.trySetString("FSTS", fileSet.getSmovf().getFileStatus());
        fileSet.getSuppf().close();
        ws.trySetString("FSTS", fileSet.getSuppf().getFileStatus());
        fileSet.getProdf().close();
        ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
    }

    /** COBOL paragraph: ABEND-010 */
    private void abendFileOpenError() {
        ws.setKaProgid("PU0040");
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
     * Opens a file IO, retrying via an OUTPUT/close/IO cycle on a not-found status, then aborts if
     * still bad.
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
            runChain(this::abendFileOpenError);
        }
    }

    /** Reads STOKF by the composite PROD|WHSE key already set on SK-PROD/SK-WHSE. */
    private void readStokfByProdWhse() {
        StringBuilder rkSb = new StringBuilder();
        String rkPart0 = "";
        try {
            rkPart0 = ws.getString("SK-PROD");
        } catch (Exception _e) {
        }
        rkSb.append(rkPart0 != null ? rkPart0.trim() : "");
        String rkPart1 = "";
        try {
            rkPart1 = ws.getString("SK-WHSE");
        } catch (Exception _e) {
        }
        rkSb.append('|');
        rkSb.append(rkPart1 != null ? rkPart1.trim() : "");
        fileSet.getStokf().readByKey(rkSb.toString());
        ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
    }

    /**
     * Reads SUPPF by SP-CODE, falling back to the current record's key if the field lookup is
     * empty.
     */
    private void readSuppfBySpCode() {
        String rkVal = "";
        if (rkVal == null || rkVal.trim().isEmpty()) {
            try {
                rkVal = ws.getString("SP-CODE");
            } catch (Exception _e) {
            }
        }
        if (rkVal == null || rkVal.trim().isEmpty()) {
            try {
                rkVal = fileSet.getSuppf().extractKeyFromCurrentRecord();
            } catch (Exception _e3) {
            }
        }
        fileSet.getSuppf().readByKey(rkVal != null ? rkVal.trim() : "");
        ws.trySetString("FSTS", fileSet.getSuppf().getFileStatus());
    }
}
