package com.sakura.oe0010.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.numgen.service.NumgenService;
import com.sakura.oe0010.domain.Oe0010FieldAccess;
import com.sakura.oe0010.domain.WorkingStorage;
import com.sakura.oe0010.runtime.Oe0010Datasets;
import com.sakura.oe0010.screen.ScreenDefs;
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

/** Business logic service generated from COBOL program OE0010. */
@Service
@Scope("prototype")
public class Oe0010Service extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final Oe0010Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL TAXCAL. */
    private TaxcalService taxcalService;

    /** Injected service for COBOL CALL NUMGEN. */
    private NumgenService numgenService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Oe0010FieldAccess ws;

    public Oe0010Service(
            Oe0010Datasets fileSet,
            DateutService dateutService,
            TaxcalService taxcalService,
            NumgenService numgenService,
            AbortxService abortxService,
            ScreenRendererInstance renderer) {
        this.fileSet = fileSet;
        this.ws = new Oe0010FieldAccess(new WorkingStorage(), fileSet);
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
            runChain(this::processOrderEntryLoop);
        }
        runChain(this::closeAllFiles);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeProgram() {
        ws.setWkProgid("OE0010");
        ws.setWkTitle("Sales Order Entry");
        ws.setWkFkeyLine("ENTER=Next  PF3=End/Finish  PF4=Clear line");
        ws.setKdFunc("TODY");
        dateut(ws.getKdate());
        ws.setWkSysymd(ws.getKdDate1());
        ws.setWkSysdate(ws.getKdDate1());
        runChain(this::openAllFiles);
    }

    /** COBOL paragraph: OPENF-010 */
    private void openAllFiles() {
        runChain(this::openOrderHeaderFile);
        runChain(this::openOrderDetailFile);
        openFileWithRetry(fileSet.getStokf());
        fileSet.getCustf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
        fileSet.getProdf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
        fileSet.getCprcf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getCprcf().getFileStatus());
    }

    /** COBOL paragraph: OIOH-010 */
    private void openOrderHeaderFile() {
        openFileWithRetry(fileSet.getOrdhf());
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("ORDHF");
            runChain(this::abendFileOpenError);
        }
    }

    /** COBOL paragraph: OIOD-010 */
    private void openOrderDetailFile() {
        openFileWithRetry(fileSet.getOrddf());
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("ORDDF");
            runChain(this::abendFileOpenError);
        }
    }

    /** COBOL paragraph: OLOOP-010 */
    private void processOrderEntryLoop() {
        runChain(this::clearOrderHeaderWorkArea);
        runChain(this::editOrderHeader);
        if ((ws.getEndFlg() == 1)) {
            return;
        }
        if ((ws.getHdrOk() != 1)) {
            return;
        }
        while ((ws.getDtlDone() != 1)) {
            runChain(this::processDetailLineLoop);
        }
        if (ws.getWkLcnt() > 0) {
            runChain(this::confirmAndSaveOrder);
        } else {
            ws.setWkMsgLine("No lines entered - order discarded");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
    }

    /** COBOL paragraph: CLRO-010 */
    private void clearOrderHeaderWorkArea() {
        fileSet.getOrdhf().setRecord();
        ws.setWkDet("");
        ws.setWkLcnt(0);
        ws.setWkNetTotal(0);
        ws.setWkTaxTotal(0);
        ws.setWkGrsTotal(0);
        ws.setHdrOk(0);
        ws.setDtlDone(0);
        ws.setWkOrdNoD(0);
        ws.setWkCustName(" ");
        ws.setWkConfirm(" ");
        ws.setOhDate(ws.getWkSysdate());
        ws.setOhDueDate(ws.getWkSysdate());
        ws.setOhTaxType(1);
    }

    /** COBOL paragraph: EHDR-010 */
    private void editOrderHeader() {
        ws.setHdrOk(0);
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEADER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-FOOTER"), ws);
        ws.setWkMsgLine("Enter order header - PF3 to quit");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEAD"), ws);
        String scValOhDate0 =
                Utility.acceptScreen(
                        "OH-DATE", () -> renderer.acceptField(ScreenDefs.getInput("OH-DATE")));
        ws.setOhDate(Utility.parseIntOr(scValOhDate0.trim(), 0));
        String scValOhCust1 =
                Utility.acceptScreen(
                        "OH-CUST", () -> renderer.acceptField(ScreenDefs.getInput("OH-CUST")));
        ws.setOhCust(Utility.parseIntOr(scValOhCust1.trim(), 0));
        String scValOhStaff2 =
                Utility.acceptScreen(
                        "OH-STAFF", () -> renderer.acceptField(ScreenDefs.getInput("OH-STAFF")));
        ws.setOhStaff(Utility.parseIntOr(scValOhStaff2.trim(), 0));
        String scValOhWhse3 =
                Utility.acceptScreen(
                        "OH-WHSE", () -> renderer.acceptField(ScreenDefs.getInput("OH-WHSE")));
        ws.setOhWhse(Utility.parseIntOr(scValOhWhse3.trim(), 0));
        String scValOhDueDate4 =
                Utility.acceptScreen(
                        "OH-DUE-DATE",
                        () -> renderer.acceptField(ScreenDefs.getInput("OH-DUE-DATE")));
        ws.setOhDueDate(Utility.parseIntOr(scValOhDueDate4.trim(), 0));
        String scValOhCustPo5 =
                Utility.acceptScreen(
                        "OH-CUST-PO",
                        () -> renderer.acceptField(ScreenDefs.getInput("OH-CUST-PO")));
        ws.setOhCustPo(scValOhCustPo5);
        String scValOhTaxType6 =
                Utility.acceptScreen(
                        "OH-TAX-TYPE",
                        () -> renderer.acceptField(ScreenDefs.getInput("OH-TAX-TYPE")));
        ws.setOhTaxType(Utility.parseIntOr(scValOhTaxType6.trim(), 0));
        broadcastEstsStatus();
        if (Utility.fieldEquals(ws.getEsts(), "03")) {
            if (ws.getOhCust() == 0) {
                ws.setString("END-FLG", "1");
            }
            return;
        }
        runChain(this::validateOrderHeader);
    }

    /** COBOL paragraph: VHDR-010 */
    private void validateOrderHeader() {
        if (ws.getOhCust() == 0) {
            ws.setWkMsgLine("Customer code required");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setCuCode(ws.getOhCust());
        String rkVal_7 = "";
        if (rkVal_7 == null || rkVal_7.trim().isEmpty()) {
            try {
                rkVal_7 = ws.getString("CU-CODE");
            } catch (Exception _e) {
            }
        }
        if (rkVal_7 == null || rkVal_7.trim().isEmpty()) {
            try {
                rkVal_7 = fileSet.getCustf().extractKeyFromCurrentRecord();
            } catch (Exception _e3) {
            }
        }
        fileSet.getCustf().readByKey(rkVal_7 != null ? rkVal_7.trim() : "");
        ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
        if (fileSet.getCustf().isInvalidKey()) {
            ws.setWkMsgLine("Customer not found");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        if (ws.getCuDelFlag() == 1) {
            ws.setWkMsgLine("Customer is deleted");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setWkCustName(ws.getCuName());
        if (ws.getOhStaff() == 0) {
            ws.setOhStaff(ws.getCuStaff());
        }
        if (ws.getOhTaxType() == 0) {
            ws.setOhTaxType(ws.getCuTaxType());
        }
        ws.setHdrOk(1);
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEAD"), ws);
        ws.setWkMsgLine("Header OK - enter detail lines");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
    }

    /** COBOL paragraph: DLOOP-010 */
    private void processDetailLineLoop() {
        runChain(this::clearDetailLineWorkArea);
        ws.setWkDWhse(ws.getOhWhse());
        renderer.displayScreen(ScreenDefs.getScreen("DS-DETAIL"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-STATUS"), ws);
        String scValWkDProd8 =
                Utility.acceptScreen(
                        "WK-D-PROD", () -> renderer.acceptField(ScreenDefs.getInput("WK-D-PROD")));
        ws.setWkDProd(Utility.parseIntOr(scValWkDProd8.trim(), 0));
        String scValWkDWhse9 =
                Utility.acceptScreen(
                        "WK-D-WHSE", () -> renderer.acceptField(ScreenDefs.getInput("WK-D-WHSE")));
        ws.setWkDWhse(Utility.parseIntOr(scValWkDWhse9.trim(), 0));
        String scValWkDQty10 =
                Utility.acceptScreen(
                        "WK-D-QTY", () -> renderer.acceptField(ScreenDefs.getInput("WK-D-QTY")));
        ws.setWkDQty(Utility.parseIntOr(scValWkDQty10.trim(), 0));
        String scValWkDPrice11 =
                Utility.acceptScreen(
                        "WK-D-PRICE",
                        () -> renderer.acceptField(ScreenDefs.getInput("WK-D-PRICE")));
        try {
            ws.setWkDPrice(new BigDecimal(scValWkDPrice11.trim()));
        } catch (NumberFormatException _nfe) {
            ws.setWkDPrice(BigDecimal.ZERO);
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
    private void clearDetailLineWorkArea() {
        ws.setWkDProd(0);
        ws.setWkDWhse(0);
        ws.setWkDQty(0);
        ws.setWkDPrice(BigDecimal.ZERO);
        ws.setWkDAmt(0);
        ws.setWkDAvail(0);
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
        String rkVal_12 = "";
        if (rkVal_12 == null || rkVal_12.trim().isEmpty()) {
            try {
                rkVal_12 = ws.getString("PR-CODE");
            } catch (Exception _e) {
            }
        }
        if (rkVal_12 == null || rkVal_12.trim().isEmpty()) {
            try {
                rkVal_12 = fileSet.getProdf().extractKeyFromCurrentRecord();
            } catch (Exception _e3) {
            }
        }
        fileSet.getProdf().readByKey(rkVal_12 != null ? rkVal_12.trim() : "");
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
        if (ws.getWkDQty() <= 0) {
            ws.setWkMsgLine("Quantity must be positive");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        if ((ws.getWkDPrice().signum() <= 0)) {
            runChain(this::resolveDetailPrice);
        }
        ws.setWkDAmt(BigDecimal.valueOf(ws.getWkDQty()).multiply(ws.getWkDPrice()).longValue());
        runChain(this::checkStockAvailability);
        runChain(this::addDetailLineToOrder);
    }

    /** COBOL paragraph: RPRC-010 */
    private void resolveDetailPrice() {
        ws.setCpCust(ws.getOhCust());
        ws.setCpProd(ws.getWkDProd());
        StringBuilder rkSb_13 = new StringBuilder();
        String rkPart0_13 = "";
        try {
            rkPart0_13 = ws.getString("CP-CUST");
        } catch (Exception _e) {
        }
        rkSb_13.append(rkPart0_13 != null ? rkPart0_13.trim() : "");
        String rkPart1_13 = "";
        try {
            rkPart1_13 = ws.getString("CP-PROD");
        } catch (Exception _e) {
        }
        rkSb_13.append('|');
        rkSb_13.append(rkPart1_13 != null ? rkPart1_13.trim() : "");
        fileSet.getCprcf().readByKey(rkSb_13.toString());
        ws.trySetString("FSTS", fileSet.getCprcf().getFileStatus());
        if (fileSet.getCprcf().isInvalidKey()) {
            runChain(this::applyRankPrice);
        }
        if (!fileSet.getCprcf().isInvalidKey()) {
            if (ws.getCpDelFlag() == 0
                    && ws.getCpStartDate() <= ws.getOhDate()
                    && (ws.getCpEndDate() == 0 || ws.getCpEndDate() >= ws.getOhDate())) {
                ws.setWkDPrice(ws.getCpPrice());
            } else {
                runChain(this::applyRankPrice);
            }
        }
    }

    /** COBOL paragraph: RANK-010 */
    private void applyRankPrice() {
        if (ws.getCuPriceRank() >= 1 && ws.getCuPriceRank() <= 5) {
            ws.setWkDPrice(ws.getPrRankPrice(ws.getCuPriceRank()));
        }
        if ((ws.getWkDPrice().signum() <= 0)) {
            ws.setWkDPrice(ws.getPrListPrice());
        }
    }

    /** COBOL paragraph: CSTK-010 */
    private void checkStockAvailability() {
        if (ws.getPrStockMng() == 0) {
            return;
        }
        ws.setSkProd(ws.getWkDProd());
        ws.setSkWhse(ws.getWkDWhse());
        readStokByProdWhseKey();
        if (fileSet.getStokf().isInvalidKey()) {
            ws.setWkDAvail(0);
        }
        if (!fileSet.getStokf().isInvalidKey()) {
            ws.setWkDAvail(ws.getSkOnhand().subtract(ws.getSkAllocated()).intValue());
        }
        if (ws.getWkDQty() > ws.getWkDAvail()) {
            ws.setWkMsgLine("Warning: quantity exceeds available stock");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
    }

    /** COBOL paragraph: ADDL-010 */
    private void addDetailLineToOrder() {
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
        ws.setWlPrice(ws.getLx(), ws.getWkDPrice());
        ws.setWlAmount(ws.getLx(), ws.getWkDAmt());
        ws.setWlTaxcat(ws.getLx(), ws.getWkDTaxcat());
        ws.setWkNetTotal(ws.getWkNetTotal() + ws.getWkDAmt());
        renderer.displayScreen(ScreenDefs.getScreen("DS-STATUS"), ws);
        ws.setWkMsgLine("Line added");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
    }

    /** COBOL paragraph: CSAV-010 */
    private void confirmAndSaveOrder() {
        ws.setWkConfirm(" ");
        runChain(this::calculateOrderTax);
        renderer.displayScreen(ScreenDefs.getScreen("DS-STATUS"), ws);
        ws.setWkMsgLine("Review totals then confirm");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-CONFIRM"), ws);
        String scValWkConfirm15 =
                Utility.acceptScreen(
                        "WK-CONFIRM",
                        () -> renderer.acceptField(ScreenDefs.getInput("WK-CONFIRM")));
        ws.setWkConfirm(scValWkConfirm15);
        broadcastEstsStatus();
        if ((ws.getWkConfirm().equals("Y") || ws.getWkConfirm().equals("y"))) {
            runChain(this::saveOrderHeader);
        } else {
            ws.setWkMsgLine("Order discarded");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
    }

    /** COBOL paragraph: CTT-010 */
    private void calculateOrderTax() {
        ws.setKtCategory(1);
        ws.setKtTaxType(ws.getOhTaxType());
        ws.setKtRound(ws.getCuTaxRound());
        if (ws.getKtRound() == 0) {
            ws.setKtRound(1);
        }
        ws.setKtDate(ws.getOhDate());
        ws.setKtAmount(BigDecimal.valueOf(ws.getWkNetTotal()));
        taxcal(ws.getKtax());
        ws.setWkNetTotal(ws.getKtNet().longValue());
        ws.setWkTaxTotal(ws.getKtTax().longValue());
        ws.setWkGrsTotal(ws.getKtGross().longValue());
    }

    /** COBOL paragraph: SORD-010 */
    private void saveOrderHeader() {
        ws.setKnumKey("ORDER");
        numgen(ws.getKnum());
        if (!Utility.fieldEquals(ws.getKnumStatus(), "00")) {
            ws.setWkMsgLine("Number assignment failed");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setOhNo(ws.getKnumNumber());
        ws.setWkOrdNoD(ws.getKnumNumber());
        ws.setOhAmount(BigDecimal.valueOf(ws.getWkNetTotal()));
        ws.setOhTaxAmount(BigDecimal.valueOf(ws.getWkTaxTotal()));
        ws.setOhTotal(BigDecimal.valueOf(ws.getWkGrsTotal()));
        ws.setOhStatus(0);
        ws.setOhLines(ws.getWkLcnt());
        ws.setOhAddDate(ws.getWkSysdate());
        ws.setOhUpdDate(ws.getWkSysdate());
        ws.setOhAddUser(ws.getWkUserCode());
        ws.setOhUpdUser(ws.getWkUserCode());
        ws.setOhDelFlag(0);
        fileSet.getOrdhf().write();
        ws.trySetString("FSTS", fileSet.getOrdhf().getFileStatus());
        if (fileSet.getOrdhf().isInvalidKey()) {
            ws.setWkMsgLine("Header write failed");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        runChain(this::writeOrderDetailLines);
        ws.setWkMsgLine("Order saved");
        renderer.displayScreen(ScreenDefs.getScreen("DS-STATUS"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
    }

    /** COBOL paragraph: WDET-010 */
    private void writeOrderDetailLines() {
        for (ws.setLx(1); ws.getLx() <= ws.getWkLcnt(); ws.setLx(ws.getLx() + 1)) {
            fileSet.getOrddf().setRecord();
            ws.setOdNo(ws.getOhNo());
            ws.setWkIdx(ws.getLx());
            ws.setOdLine(ws.getWkIdx());
            ws.setOdProd(ws.getWlProd(ws.getLx()));
            ws.setOdWhse(ws.getWlWhse(ws.getLx()));
            ws.setOdQty(BigDecimal.valueOf(ws.getWlQty(ws.getLx())));
            ws.setOdUnitPrice(ws.getWlPrice(ws.getLx()));
            ws.setOdAmount(BigDecimal.valueOf(ws.getWlAmount(ws.getLx())));
            ws.setOdTaxCategory(ws.getWlTaxcat(ws.getLx()));
            ws.setOdShippedQty(BigDecimal.ZERO);
            ws.setOdAllocQty(BigDecimal.ZERO);
            ws.setOdDueDate(ws.getOhDueDate());
            ws.setOdStatus(0);
            fileSet.getOrddf().write();
            ws.trySetString("FSTS", fileSet.getOrddf().getFileStatus());
            if (fileSet.getOrddf().isInvalidKey()) {
                /* CONTINUE */
            }
            runChain(this::allocateStockForLine);
        }
    }

    /** COBOL paragraph: ALOC-010 */
    private void allocateStockForLine() {
        ws.setSkProd(ws.getWlProd(ws.getLx()));
        ws.setSkWhse(ws.getWlWhse(ws.getLx()));
        readStokByProdWhseKey();
        if (fileSet.getStokf().isInvalidKey()) {
            fileSet.getStokf().setRecord();
            ws.setSkProd(ws.getWlProd(ws.getLx()));
            ws.setSkWhse(ws.getWlWhse(ws.getLx()));
            ws.setSkAllocated(BigDecimal.valueOf(ws.getWlQty(ws.getLx())));
            fileSet.getStokf().write();
            ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
            if (fileSet.getStokf().isInvalidKey()) {
                /* CONTINUE */
            }
        }
        if (!fileSet.getStokf().isInvalidKey()) {
            ws.setSkAllocated(ws.getSkAllocated().add(BigDecimal.valueOf(ws.getWlQty(ws.getLx()))));
            fileSet.getStokf().rewrite();
            ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
            if (fileSet.getStokf().isInvalidKey()) {
                /* CONTINUE */
            }
        }
    }

    /** COBOL paragraph: TERM-010 */
    private void closeAllFiles() {
        fileSet.getOrdhf().close();
        ws.trySetString("FSTS", fileSet.getOrdhf().getFileStatus());
        fileSet.getOrddf().close();
        ws.trySetString("FSTS", fileSet.getOrddf().getFileStatus());
        fileSet.getStokf().close();
        ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
        fileSet.getCustf().close();
        ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
        fileSet.getProdf().close();
        ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
        fileSet.getCprcf().close();
        ws.trySetString("FSTS", fileSet.getCprcf().getFileStatus());
    }

    /** COBOL paragraph: ABEND-010 */
    private void abendFileOpenError() {
        ws.setKaProgid("OE0010");
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
     * Open a file I/O, retrying as OUTPUT then reopening I/O if the file did not previously exist
     * (FSTS 35/30).
     */
    private void openFileWithRetry(RawDatasetBase file) {
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
    }

    /** Read STOKF by the SK-PROD/SK-WHSE composite key already set on ws. */
    private void readStokByProdWhseKey() {
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
}
