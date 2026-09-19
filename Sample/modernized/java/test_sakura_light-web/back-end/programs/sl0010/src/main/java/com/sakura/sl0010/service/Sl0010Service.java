package com.sakura.sl0010.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.numgen.service.NumgenService;
import com.sakura.runtime.BatchServiceBase;
import com.sakura.runtime.DatasetEnums.*;
import com.sakura.runtime.ParagraphJumpSignal;
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
import com.sakura.sl0010.domain.Sl0010FieldAccess;
import com.sakura.sl0010.domain.WorkingStorage;
import com.sakura.sl0010.runtime.Sl0010Datasets;
import com.sakura.sl0010.screen.ScreenDefs;
import com.sakura.taxcal.service.TaxcalService;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/** Business logic service generated from COBOL program SL0010. */
@Service
@Scope("prototype")
public class Sl0010Service extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final Sl0010Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL TAXCAL. */
    private TaxcalService taxcalService;

    /** Injected service for COBOL CALL NUMGEN. */
    private NumgenService numgenService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Sl0010FieldAccess ws;

    public Sl0010Service(
            Sl0010Datasets fileSet,
            DateutService dateutService,
            TaxcalService taxcalService,
            NumgenService numgenService,
            AbortxService abortxService,
            ScreenRendererInstance renderer) {
        this.fileSet = fileSet;
        this.ws = new Sl0010FieldAccess(new WorkingStorage(), fileSet);
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
            runChain(this::acceptMainSelection);
        }
        runChain(this::closeAllFiles);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeProgram() {
        ws.setWkProgid("SL0010");
        ws.setWkTitle("Sales / Invoice Entry");
        ws.setWkFkeyLine("ENTER=Next  PF3=End");
        ws.setKdFunc("TODY");
        dateut(ws.getKdate());
        ws.setWkSysymd(ws.getKdDate1());
        ws.setWkSysdate(ws.getKdDate1());
        runChain(this::openAllFiles);
    }

    /** COBOL paragraph: OPENF-010 */
    private void openAllFiles() {
        runChain(this::openInvoiceHeaderFile);
        runChain(this::openInvoiceDetailFile);
        runChain(this::openArLedgerFile);
        runChain(this::openShipmentHeaderFile);
        runChain(this::openCustomerFile);
        fileSet.getShpdf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getShpdf().getFileStatus());
        openWithRetry(fileSet.getOrdhf(), FileOpenMode.IO);
        openWithRetry(fileSet.getStokf(), FileOpenMode.INPUT);
        fileSet.getProdf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
        fileSet.getCprcf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getCprcf().getFileStatus());
    }

    /** COBOL paragraph: OIIH-010 */
    private void openInvoiceHeaderFile() {
        openIoOrAbend(fileSet.getInvhf(), "INVHF");
    }

    /** COBOL paragraph: OIID-010 */
    private void openInvoiceDetailFile() {
        openIoOrAbend(fileSet.getInvdf(), "INVDF");
    }

    /** COBOL paragraph: OIAR-010 */
    private void openArLedgerFile() {
        openIoOrAbend(fileSet.getArlf(), "ARLF");
    }

    /** COBOL paragraph: OISH-010 */
    private void openShipmentHeaderFile() {
        openIoOrAbend(fileSet.getShphf(), "SHPHF");
    }

    /** COBOL paragraph: OICU-010 */
    private void openCustomerFile() {
        openIoOrAbend(fileSet.getCustf(), "CUSTF");
    }

    /** COBOL paragraph: MAINR-010 */
    private void acceptMainSelection() {
        runChain(this::clearInvoiceWorkFields);
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEADER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-FOOTER"), ws);
        ws.setWkMsgLine("Enter a shipment number or a customer code");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-KEY"), ws);
        String scValWkSelShip0 =
                Utility.acceptScreen(
                        "WK-SEL-SHIP",
                        () -> renderer.acceptField(ScreenDefs.getInput("WK-SEL-SHIP")));
        ws.setWkSelShip(Utility.parseLongOr(scValWkSelShip0.trim(), 0L));
        String scValWkSelCust1 =
                Utility.acceptScreen(
                        "WK-SEL-CUST",
                        () -> renderer.acceptField(ScreenDefs.getInput("WK-SEL-CUST")));
        ws.setWkSelCust(Utility.parseIntOr(scValWkSelCust1.trim(), 0));
        broadcastEstsStatus();
        switch (String.valueOf(ws.getEsts())) {
            case "03" -> {
                ws.setString("END-FLG", "1");
            }
            case "00" -> {
                runChain(this::dispatchByShipOrCustomer);
            }
            default -> {
                ws.setWkMsgLine("Invalid function key");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: CLRI-010 */
    private void clearInvoiceWorkFields() {
        ws.setWkSelShip(0);
        ws.setWkSelCust(0);
        ws.setWkMode(0);
        ws.setWkInvNo(0);
        ws.setWkDcnt(0);
        ws.setWkNetTotal(0);
        ws.setWkTaxTotal(0);
        ws.setWkGrsTotal(0);
        ws.setWkCostTotal(0);
        ws.setWkStaffIn(0);
        ws.setWkTaxtypeIn(0);
        ws.setWkPageTop(1);
        ws.setWkCustName(" ");
        ws.setWkConfirm(" ");
    }

    /** COBOL paragraph: DK-010 */
    private void dispatchByShipOrCustomer() {
        if (ws.getWkSelShip() != 0) {
            ws.setString("WK-MODE", "1");
            runChain(this::createInvoiceFromShipment);
        } else {
            if (ws.getWkSelCust() != 0) {
                ws.setString("WK-MODE", "2");
                runChain(this::createDirectInvoice);
            } else {
                ws.setWkMsgLine("Enter shipment number or customer code");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: IFS-010 */
    private void createInvoiceFromShipment() {
        ws.setXhNo(ws.getWkSelShip());
        String rkVal_2 = "";
        if (rkVal_2 == null || rkVal_2.trim().isEmpty()) {
            try {
                rkVal_2 = ws.getString("XH-NO");
            } catch (Exception _e) {
            }
        }
        if (rkVal_2 == null || rkVal_2.trim().isEmpty()) {
            try {
                rkVal_2 = fileSet.getShphf().extractKeyFromCurrentRecord();
            } catch (Exception _e3) {
            }
        }
        fileSet.getShphf().readByKey(rkVal_2 != null ? rkVal_2.trim() : "");
        ws.trySetString("FSTS", fileSet.getShphf().getFileStatus());
        if (fileSet.getShphf().isInvalidKey()) {
            ws.setWkMsgLine("Shipment not found");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        if (ws.getXhDelFlag() == 1) {
            ws.setWkMsgLine("Shipment is deleted");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        if (ws.getXhStatus() == 2) {
            ws.setWkMsgLine("Shipment is already invoiced");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        if (ws.getXhStatus() == 9) {
            ws.setWkMsgLine("Shipment is cancelled");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setWkSelCust(ws.getXhCust());
        ws.setWkStaffIn(ws.getXhStaff());
        ws.setWkShipDate(ws.getXhDate());
        runChain(this::readCustomerRecord);
        if ((ws.getFoundFlg() != 1)) {
            ws.setWkMsgLine("Customer of shipment not found");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setWkTaxtypeIn(ws.getCuTaxType());
        runChain(this::startShipmentLineScan);
        if (ws.getWkDcnt() == 0) {
            ws.setWkMsgLine("Shipment has no lines");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        runChain(this::computeInvoiceTotals);
        runChain(this::reviewAndPostInvoice);
    }

    /** COBOL paragraph: RC-010 */
    private void readCustomerRecord() {
        ws.setFoundFlg(0);
        ws.setWkCustName(" ");
        ws.setCuCode(ws.getWkSelCust());
        String rkVal_3 = "";
        if (rkVal_3 == null || rkVal_3.trim().isEmpty()) {
            try {
                rkVal_3 = ws.getString("CU-CODE");
            } catch (Exception _e) {
            }
        }
        if (rkVal_3 == null || rkVal_3.trim().isEmpty()) {
            try {
                rkVal_3 = fileSet.getCustf().extractKeyFromCurrentRecord();
            } catch (Exception _e3) {
            }
        }
        fileSet.getCustf().readByKey(rkVal_3 != null ? rkVal_3.trim() : "");
        ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
        if (fileSet.getCustf().isInvalidKey()) {
            ws.setWkMsgLine("Customer not found");
            return;
        }
        if (ws.getCuDelFlag() == 1) {
            ws.setWkMsgLine("Customer is deleted");
            return;
        }
        ws.setWkCustName(ws.getCuName());
        ws.setString("FOUND-FLG", "1");
    }

    /** COBOL paragraph: LSL-010 */
    private void startShipmentLineScan() {
        ws.setWkDcnt(0);
        ws.setXdNo(ws.getWkSelShip());
        ws.setXdLine(0);
        fileSet.getShpdf().start("XD-NO", "NOT LESS THAN");
        ws.trySetString("FSTS", fileSet.getShpdf().getFileStatus());
        if (fileSet.getShpdf().isInvalidKey()) {
            return;
        }
        // fall-through to next paragraph
        loadShipmentLines();
    }

    /** COBOL paragraph: LSL-020 */
    private void loadShipmentLines() {
        while (true) {
            fileSet.getShpdf().readNext();
            ws.trySetString("FSTS", fileSet.getShpdf().getFileStatus());
            if (fileSet.getShpdf().isAtEnd()) {
                return;
            }
            if (ws.getXdNo() != ws.getWkSelShip()) {
                return;
            }
            if (ws.getWkDcnt() >= 200) {
                return;
            }
            ws.setWkDcnt(ws.getWkDcnt() + 1);
            ws.setDrProd(ws.getWkDcnt(), ws.getXdProd());
            ws.setDrWhse(ws.getWkDcnt(), ws.getXdWhse());
            ws.setDrQty(ws.getWkDcnt(), ws.getXdQty().intValue());
            ws.setDrPrice(ws.getWkDcnt(), ws.getXdUnitPrice());
            ws.setDrOrder(ws.getWkDcnt(), ws.getXdOrder());
            ws.setDrOrdline(ws.getWkDcnt(), ws.getXdOrderLine());
            ws.setWkDProd(ws.getXdProd());
            runChain(this::lookupProduct);
            ws.setDrName(ws.getWkDcnt(), ws.getWkDName());
            ws.setDrTaxcat(ws.getWkDcnt(), ws.getWkDTaxcat());
            ws.setWkDWhse(ws.getXdWhse());
            runChain(this::resolveProductCost);
            if ((ws.getWkDCost().signum() <= 0) && (ws.getXdUnitCost().signum() > 0)) {
                ws.setWkDCost(ws.getXdUnitCost());
            }
            ws.setDrCost(ws.getWkDcnt(), ws.getWkDCost());
            ws.setDrAmt(ws.getWkDcnt(), ws.getXdQty().multiply(ws.getXdUnitPrice()).longValue());
            ws.setDrCostamt(ws.getWkDcnt(), ws.getXdQty().multiply(ws.getWkDCost()).longValue());
        }
    }

    /** COBOL paragraph: IDR-010 */
    private void createDirectInvoice() {
        runChain(this::readCustomerRecord);
        if ((ws.getFoundFlg() != 1)) {
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setWkStaffIn(ws.getCuStaff());
        ws.setWkTaxtypeIn(ws.getCuTaxType());
        ws.setWkShipDate(0);
        runChain(this::acceptAndValidateHeaderFields);
        if ((ws.getErrFlg() == 1)) {
            return;
        }
        runChain(this::acceptDetailLinesLoop);
        if (ws.getWkDcnt() == 0) {
            ws.setWkMsgLine("No lines entered - invoice discarded");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        runChain(this::computeInvoiceTotals);
        runChain(this::reviewAndPostInvoice);
    }

    /** COBOL paragraph: EDH-010 */
    private void acceptAndValidateHeaderFields() {
        ws.setErrFlg(0);
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEADER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-FOOTER"), ws);
        ws.setWkMsgLine("Enter sale header - PF3 to cancel");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-DHEAD"), ws);
        String scValWkStaffIn4 =
                Utility.acceptScreen(
                        "WK-STAFF-IN",
                        () -> renderer.acceptField(ScreenDefs.getInput("WK-STAFF-IN")));
        ws.setWkStaffIn(Utility.parseIntOr(scValWkStaffIn4.trim(), 0));
        String scValWkTaxtypeIn5 =
                Utility.acceptScreen(
                        "WK-TAXTYPE-IN",
                        () -> renderer.acceptField(ScreenDefs.getInput("WK-TAXTYPE-IN")));
        ws.setWkTaxtypeIn(Utility.parseIntOr(scValWkTaxtypeIn5.trim(), 0));
        broadcastEstsStatus();
        if (Utility.fieldEquals(ws.getEsts(), "03")) {
            ws.setString("ERR-FLG", "1");
            return;
        }
        if (ws.getWkTaxtypeIn() == 0) {
            ws.setWkTaxtypeIn(ws.getCuTaxType());
        }
        if (ws.getWkTaxtypeIn() < 1 || ws.getWkTaxtypeIn() > 3) {
            ws.setWkMsgLine("Tax type must be 1, 2 or 3");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            ws.setString("ERR-FLG", "1");
        }
    }

    /** COBOL paragraph: DEL-010 */
    private void acceptDetailLinesLoop() {
        ws.setWkDtlDone(0);
        while ((ws.getWkDtlDone() != 1)) {
            runChain(this::clearDetailWorkFields);
            renderer.displayScreen(ScreenDefs.getScreen("DS-DETAIL"), ws);
            renderer.displayScreen(ScreenDefs.getScreen("DS-ESTAT"), ws);
            String scValWkDProd6 =
                    Utility.acceptScreen(
                            "WK-D-PROD",
                            () -> renderer.acceptField(ScreenDefs.getInput("WK-D-PROD")));
            ws.setWkDProd(Utility.parseIntOr(scValWkDProd6.trim(), 0));
            String scValWkDWhse7 =
                    Utility.acceptScreen(
                            "WK-D-WHSE",
                            () -> renderer.acceptField(ScreenDefs.getInput("WK-D-WHSE")));
            ws.setWkDWhse(Utility.parseIntOr(scValWkDWhse7.trim(), 0));
            String scValWkDQty8 =
                    Utility.acceptScreen(
                            "WK-D-QTY",
                            () -> renderer.acceptField(ScreenDefs.getInput("WK-D-QTY")));
            ws.setWkDQty(Utility.parseIntOr(scValWkDQty8.trim(), 0));
            String scValWkDPrice9 =
                    Utility.acceptScreen(
                            "WK-D-PRICE",
                            () -> renderer.acceptField(ScreenDefs.getInput("WK-D-PRICE")));
            try {
                ws.setWkDPrice(new BigDecimal(scValWkDPrice9.trim()));
            } catch (NumberFormatException _nfe) {
                ws.setWkDPrice(BigDecimal.ZERO);
            }
            broadcastEstsStatus();
            switch (String.valueOf(ws.getEsts())) {
                case "03" -> {
                    ws.setString("WK-DTL-DONE", "1");
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
    }

    /** COBOL paragraph: CLRD-010 */
    private void clearDetailWorkFields() {
        ws.setWkDProd(0);
        ws.setWkDWhse(0);
        ws.setWkDQty(0);
        ws.setWkDPrice(BigDecimal.ZERO);
        ws.setWkDAmt(0);
        ws.setWkDCost(BigDecimal.ZERO);
        ws.setWkDCostamt(0);
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
        runChain(this::lookupProduct);
        if ((ws.getFoundFlg() != 1)) {
            ws.setWkMsgLine("Product not found");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        if (ws.getWkDWhse() == 0) {
            ws.setWkDWhse(ws.getPrDfltWhse());
        }
        if (ws.getWkDQty() <= 0) {
            ws.setWkMsgLine("Quantity must be positive");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        if ((ws.getWkDPrice().signum() <= 0)) {
            runChain(this::resolveCustomerProductPrice);
        }
        runChain(this::resolveProductCost);
        ws.setWkDAmt(BigDecimal.valueOf(ws.getWkDQty()).multiply(ws.getWkDPrice()).longValue());
        ws.setWkDCostamt(BigDecimal.valueOf(ws.getWkDQty()).multiply(ws.getWkDCost()).longValue());
        runChain(this::addDetailLine);
    }

    /** COBOL paragraph: LKP-010 */
    private void lookupProduct() {
        ws.setFoundFlg(0);
        ws.setWkDName(" ");
        ws.setPrCode(ws.getWkDProd());
        String rkVal_10 = "";
        if (rkVal_10 == null || rkVal_10.trim().isEmpty()) {
            try {
                rkVal_10 = ws.getString("PR-CODE");
            } catch (Exception _e) {
            }
        }
        if (rkVal_10 == null || rkVal_10.trim().isEmpty()) {
            try {
                rkVal_10 = fileSet.getProdf().extractKeyFromCurrentRecord();
            } catch (Exception _e3) {
            }
        }
        fileSet.getProdf().readByKey(rkVal_10 != null ? rkVal_10.trim() : "");
        ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
        if (fileSet.getProdf().isInvalidKey()) {
            return;
        }
        if (ws.getPrDelFlag() == 1) {
            return;
        }
        ws.setWkDName(ws.getPrName());
        ws.setWkDTaxcat(ws.getPrTaxCategory());
        ws.setString("FOUND-FLG", "1");
    }

    /** COBOL paragraph: RPRC-010 */
    private void resolveCustomerProductPrice() {
        ws.setCpCust(ws.getWkSelCust());
        ws.setCpProd(ws.getWkDProd());
        StringBuilder rkSb_11 = new StringBuilder();
        String rkPart0_11 = "";
        try {
            rkPart0_11 = ws.getString("CP-CUST");
        } catch (Exception _e) {
        }
        rkSb_11.append(rkPart0_11 != null ? rkPart0_11.trim() : "");
        String rkPart1_11 = "";
        try {
            rkPart1_11 = ws.getString("CP-PROD");
        } catch (Exception _e) {
        }
        rkSb_11.append('|');
        rkSb_11.append(rkPart1_11 != null ? rkPart1_11.trim() : "");
        fileSet.getCprcf().readByKey(rkSb_11.toString());
        ws.trySetString("FSTS", fileSet.getCprcf().getFileStatus());
        if (fileSet.getCprcf().isInvalidKey()) {
            runChain(this::applyRankOrListPrice);
        }
        if (!fileSet.getCprcf().isInvalidKey()) {
            if (ws.getCpDelFlag() == 0
                    && ws.getCpStartDate() <= ws.getWkSysdate()
                    && (ws.getCpEndDate() == 0 || ws.getCpEndDate() >= ws.getWkSysdate())) {
                ws.setWkDPrice(ws.getCpPrice());
            } else {
                runChain(this::applyRankOrListPrice);
            }
        }
    }

    /** COBOL paragraph: RANK-010 */
    private void applyRankOrListPrice() {
        if (ws.getCuPriceRank() >= 1 && ws.getCuPriceRank() <= 5) {
            ws.setWkDPrice(ws.getPrRankPrice(ws.getCuPriceRank()));
        }
        if ((ws.getWkDPrice().signum() <= 0)) {
            ws.setWkDPrice(ws.getPrListPrice());
        }
    }

    /** COBOL paragraph: RCST-010 */
    private void resolveProductCost() {
        ws.setWkDCost(BigDecimal.ZERO);
        ws.setSkProd(ws.getWkDProd());
        ws.setSkWhse(ws.getWkDWhse());
        StringBuilder rkSb_12 = new StringBuilder();
        String rkPart0_12 = "";
        try {
            rkPart0_12 = ws.getString("SK-PROD");
        } catch (Exception _e) {
        }
        rkSb_12.append(rkPart0_12 != null ? rkPart0_12.trim() : "");
        String rkPart1_12 = "";
        try {
            rkPart1_12 = ws.getString("SK-WHSE");
        } catch (Exception _e) {
        }
        rkSb_12.append('|');
        rkSb_12.append(rkPart1_12 != null ? rkPart1_12.trim() : "");
        fileSet.getStokf().readByKey(rkSb_12.toString());
        ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
        if (fileSet.getStokf().isInvalidKey()) {
            ws.setWkDCost(BigDecimal.ZERO);
        }
        if (!fileSet.getStokf().isInvalidKey()) {
            ws.setWkDCost(ws.getSkAvgCost());
        }
        if ((ws.getWkDCost().signum() <= 0)) {
            ws.setPrCode(ws.getWkDProd());
            String rkVal_13 = "";
            if (rkVal_13 == null || rkVal_13.trim().isEmpty()) {
                try {
                    rkVal_13 = ws.getString("PR-CODE");
                } catch (Exception _e) {
                }
            }
            if (rkVal_13 == null || rkVal_13.trim().isEmpty()) {
                try {
                    rkVal_13 = fileSet.getProdf().extractKeyFromCurrentRecord();
                } catch (Exception _e3) {
                }
            }
            fileSet.getProdf().readByKey(rkVal_13 != null ? rkVal_13.trim() : "");
            ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
            if (fileSet.getProdf().isInvalidKey()) {
                /* CONTINUE */
            }
            if (!fileSet.getProdf().isInvalidKey()) {
                ws.setWkDCost(ws.getPrStdCost());
            }
        }
    }

    /** COBOL paragraph: ADDL-010 */
    private void addDetailLine() {
        if (ws.getWkDcnt() >= 200) {
            ws.setWkMsgLine("Maximum 200 lines reached");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setWkDcnt(ws.getWkDcnt() + 1);
        ws.setDrProd(ws.getWkDcnt(), ws.getWkDProd());
        ws.setDrWhse(ws.getWkDcnt(), ws.getWkDWhse());
        ws.setDrName(ws.getWkDcnt(), ws.getWkDName());
        ws.setDrQty(ws.getWkDcnt(), ws.getWkDQty());
        ws.setDrPrice(ws.getWkDcnt(), ws.getWkDPrice());
        ws.setDrAmt(ws.getWkDcnt(), ws.getWkDAmt());
        ws.setDrCost(ws.getWkDcnt(), ws.getWkDCost());
        ws.setDrCostamt(ws.getWkDcnt(), ws.getWkDCostamt());
        ws.setDrTaxcat(ws.getWkDcnt(), ws.getWkDTaxcat());
        ws.setDrOrder(ws.getWkDcnt(), 0);
        ws.setDrOrdline(ws.getWkDcnt(), 0);
        ws.setWkNetTotal(ws.getWkNetTotal() + ws.getWkDAmt());
        ws.setWkMsgLine("Line added");
        renderer.displayScreen(ScreenDefs.getScreen("DS-ESTAT"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
    }

    /** COBOL paragraph: CT-010 */
    private void computeInvoiceTotals() {
        ws.setWkNetTotal(0);
        ws.setWkCostTotal(0);
        for (ws.setWkIdx(1); ws.getWkIdx() <= ws.getWkDcnt(); ws.setWkIdx(ws.getWkIdx() + 1)) {
            ws.setWkNetTotal(ws.getWkNetTotal() + ws.getDrAmt(ws.getWkIdx()));
            ws.setWkCostTotal(ws.getWkCostTotal() + ws.getDrCostamt(ws.getWkIdx()));
        }
        ws.setKtCategory(1);
        ws.setKtTaxType(ws.getWkTaxtypeIn());
        ws.setKtRound(ws.getCuTaxRound());
        if (ws.getKtRound() == 0) {
            ws.setKtRound(1);
        }
        ws.setKtDate(ws.getWkSysdate());
        ws.setKtAmount(BigDecimal.valueOf(ws.getWkNetTotal()));
        taxcal(ws.getKtax());
        ws.setWkNetTotal(ws.getKtNet().longValue());
        ws.setWkTaxTotal(ws.getKtTax().longValue());
        ws.setWkGrsTotal(ws.getKtGross().longValue());
    }

    /** COBOL paragraph: RAP-010 */
    private void reviewAndPostInvoice() {
        runChain(this::calculatePageCount);
        ws.setWkPageTop(1);
        runChain(this::buildReviewWindow);
        runChain(this::runReviewLoop);
        if ((ws.getWkReviewEnd() == 1)) {
            runChain(this::confirmAndPostInvoice);
        }
    }

    /** COBOL paragraph: RVL-010 */
    private void runReviewLoop() {
        ws.setWkReviewEnd(0);
        ws.setWkFkeyLine("PF6/PF12=page  ENTER=confirm  PF3=cancel");
        while (!((ws.getWkReviewEnd() == 1) || (ws.getEndFlg() == 1))) {
            runChain(this::displayInvoiceReviewScreen);
            renderer.displayScreen(ScreenDefs.getScreen("DS-BROWSE"), ws);
            String scValWkDummy14 =
                    Utility.acceptScreen(
                            "WK-DUMMY",
                            () -> renderer.acceptField(ScreenDefs.getInput("WK-DUMMY")));
            ws.setWkDummy(scValWkDummy14);
            broadcastEstsStatus();
            switch (String.valueOf(ws.getEsts())) {
                case "03" -> {
                    ws.setWkMsgLine("Invoice cancelled");
                    renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
                    throw new ParagraphJumpSignal(this::restoreEntryFkeyLine);
                }
                case "00" -> {
                    ws.setString("WK-REVIEW-END", "1");
                }
                case "06" -> {
                    runChain(this::pageDown);
                }
                case "12" -> {
                    runChain(this::pageUp);
                }
                default -> {
                    /* CONTINUE */
                }
            }
        }
        // fall-through to next paragraph
        restoreEntryFkeyLine();
    }

    /** COBOL paragraph: RVL-020 */
    private void restoreEntryFkeyLine() {
        ws.setWkFkeyLine("ENTER=Next  PF3=End");
    }

    /** COBOL paragraph: AC-010 */
    private void confirmAndPostInvoice() {
        ws.setWkConfirm(" ");
        runChain(this::displayInvoiceReviewScreen);
        ws.setWkMsgLine("Confirm to post the invoice");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-CONFIRM"), ws);
        String scValWkConfirm15 =
                Utility.acceptScreen(
                        "WK-CONFIRM",
                        () -> renderer.acceptField(ScreenDefs.getInput("WK-CONFIRM")));
        ws.setWkConfirm(scValWkConfirm15);
        broadcastEstsStatus();
        if ((ws.getWkConfirm().equals("Y") || ws.getWkConfirm().equals("y"))) {
            runChain(this::postInvoice);
        } else {
            ws.setWkMsgLine("Invoice discarded");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
    }

    /** COBOL paragraph: PI-010 */
    private void postInvoice() {
        ws.setKnumKey("INVOICE");
        numgen(ws.getKnum());
        if (!Utility.fieldEquals(ws.getKnumStatus(), "00")) {
            ws.setWkMsgLine("Invoice number assignment failed");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setWkInvNo(ws.getKnumNumber());
        runChain(this::computeCloseYearMonth);
        runChain(this::writeInvoiceHeader);
        runChain(this::writeInvoiceDetailLines);
        runChain(this::writeArLedgerEntry);
        runChain(this::updateCustomerBalance);
        if ((ws.getWkMode() == 1)) {
            runChain(this::markShipmentInvoiced);
            runChain(this::markOrderInvoiced);
        }
        ws.setWkMsgLine(" ");
        {
            StringBuilder sb = new StringBuilder();
            sb.append(String.valueOf("Invoice "));
            sb.append(String.valueOf(String.format("%010d", (long) (ws.getWkInvNo()))));
            sb.append(String.valueOf(" posted"));
            ws.setWkMsgLine(sb.toString());
        }
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
    }

    /** COBOL paragraph: CCY-010 */
    private void computeCloseYearMonth() {
        ws.setWkYm((ws.getWkSysdate() / 100));
        ws.setWkDay((ws.getWkSysdate() - (ws.getWkYm() * 100)));
        ws.setWkCloseYm(ws.getWkYm());
        if (ws.getCuCloseDay() > 0 && ws.getCuCloseDay() < 99) {
            if (ws.getWkDay() > ws.getCuCloseDay()) {
                ws.setWkYyyy((ws.getWkYm() / 100));
                ws.setWkMm((ws.getWkYm() - (ws.getWkYyyy() * 100)));
                ws.setWkMm(ws.getWkMm() + 1);
                if (ws.getWkMm() > 12) {
                    ws.setWkMm(1);
                    ws.setWkYyyy(ws.getWkYyyy() + 1);
                }
                ws.setWkCloseYm(((ws.getWkYyyy() * 100) + ws.getWkMm()));
            }
        }
    }

    /** COBOL paragraph: WIH-010 */
    private void writeInvoiceHeader() {
        fileSet.getInvhf().setRecord();
        ws.setIhNo(ws.getWkInvNo());
        ws.setIhDate(ws.getWkSysdate());
        ws.setIhCust(ws.getWkSelCust());
        ws.setIhStaff(ws.getWkStaffIn());
        ws.setIhShipNo(ws.getWkSelShip());
        ws.setIhCloseYm(ws.getWkCloseYm());
        ws.setIhTaxType(ws.getWkTaxtypeIn());
        ws.setIhAmount(BigDecimal.valueOf(ws.getWkNetTotal()));
        ws.setIhTaxAmount(BigDecimal.valueOf(ws.getWkTaxTotal()));
        ws.setIhTotal(BigDecimal.valueOf(ws.getWkGrsTotal()));
        ws.setIhCostTotal(BigDecimal.valueOf(ws.getWkCostTotal()));
        ws.setIhStatus(1);
        ws.setIhKind(1);
        ws.setIhLines(ws.getWkDcnt());
        ws.setIhAddDate(ws.getWkSysdate());
        ws.setIhUpdDate(ws.getWkSysdate());
        ws.setIhAddUser(ws.getWkUserCode());
        ws.setIhUpdUser(ws.getWkUserCode());
        ws.setIhDelFlag(0);
        fileSet.getInvhf().write();
        ws.trySetString("FSTS", fileSet.getInvhf().getFileStatus());
        if (fileSet.getInvhf().isInvalidKey()) {
            ws.setWkMsgLine("Invoice header write failed");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
    }

    /** COBOL paragraph: WID-010 */
    private void writeInvoiceDetailLines() {
        for (ws.setWkIdx(1); ws.getWkIdx() <= ws.getWkDcnt(); ws.setWkIdx(ws.getWkIdx() + 1)) {
            fileSet.getInvdf().setRecord();
            ws.setIdNo(ws.getWkInvNo());
            ws.setIdLine(ws.getWkIdx());
            ws.setIdProd(ws.getDrProd(ws.getWkIdx()));
            ws.setIdWhse(ws.getDrWhse(ws.getWkIdx()));
            ws.setIdQty(BigDecimal.valueOf(ws.getDrQty(ws.getWkIdx())));
            ws.setIdUnitPrice(ws.getDrPrice(ws.getWkIdx()));
            ws.setIdAmount(BigDecimal.valueOf(ws.getDrAmt(ws.getWkIdx())));
            ws.setIdUnitCost(ws.getDrCost(ws.getWkIdx()));
            ws.setIdCostAmount(BigDecimal.valueOf(ws.getDrCostamt(ws.getWkIdx())));
            ws.setIdTaxCategory(ws.getDrTaxcat(ws.getWkIdx()));
            ws.setIdRemark(" ");
            fileSet.getInvdf().write();
            ws.trySetString("FSTS", fileSet.getInvdf().getFileStatus());
            if (fileSet.getInvdf().isInvalidKey()) {
                /* CONTINUE */
            }
        }
    }

    /** COBOL paragraph: PAL-010 */
    private void writeArLedgerEntry() {
        ws.setKnumKey("ARLDG");
        numgen(ws.getKnum());
        if (!Utility.fieldEquals(ws.getKnumStatus(), "00")) {
            ws.setWkMsgLine("AR ledger number assignment failed");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        fileSet.getArlf().setRecord();
        ws.setAlSeq(ws.getKnumNumber());
        ws.setAlCust(ws.getWkSelCust());
        ws.setAlDate(ws.getWkSysdate());
        ws.setAlCloseYm(ws.getWkCloseYm());
        ws.setAlKind(1);
        ws.setAlRefType(3);
        ws.setAlRefNo(ws.getWkInvNo());
        ws.setAlDebit(BigDecimal.valueOf(ws.getWkGrsTotal()));
        ws.setAlCredit(BigDecimal.ZERO);
        ws.setAlBalance(
                (ws.getCuBalance().add(BigDecimal.valueOf(ws.getWkGrsTotal())))
                        .setScale(0, java.math.RoundingMode.DOWN));
        ws.setAlRemark("Sales invoice");
        ws.setAlUser(ws.getWkUserCode());
        fileSet.getArlf().write();
        ws.trySetString("FSTS", fileSet.getArlf().getFileStatus());
        if (fileSet.getArlf().isInvalidKey()) {
            ws.setWkMsgLine("AR ledger write failed");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
    }

    /** COBOL paragraph: UCB-010 */
    private void updateCustomerBalance() {
        ws.setCuCode(ws.getWkSelCust());
        String rkVal_16 = "";
        if (rkVal_16 == null || rkVal_16.trim().isEmpty()) {
            try {
                rkVal_16 = ws.getString("CU-CODE");
            } catch (Exception _e) {
            }
        }
        if (rkVal_16 == null || rkVal_16.trim().isEmpty()) {
            try {
                rkVal_16 = fileSet.getCustf().extractKeyFromCurrentRecord();
            } catch (Exception _e3) {
            }
        }
        fileSet.getCustf().readByKey(rkVal_16 != null ? rkVal_16.trim() : "");
        ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
        if (fileSet.getCustf().isInvalidKey()) {
            return;
        }
        ws.setCuBalance(ws.getCuBalance().add(BigDecimal.valueOf(ws.getWkGrsTotal())));
        ws.setCuUpdDate(ws.getWkSysdate());
        ws.setCuUpdUser(ws.getWkUserCode());
        fileSet.getCustf().rewrite();
        ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
        if (fileSet.getCustf().isInvalidKey()) {
            ws.setWkMsgLine("Customer balance update failed");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
    }

    /** COBOL paragraph: USS-010 */
    private void markShipmentInvoiced() {
        ws.setXhNo(ws.getWkSelShip());
        String rkVal_17 = "";
        if (rkVal_17 == null || rkVal_17.trim().isEmpty()) {
            try {
                rkVal_17 = ws.getString("XH-NO");
            } catch (Exception _e) {
            }
        }
        if (rkVal_17 == null || rkVal_17.trim().isEmpty()) {
            try {
                rkVal_17 = fileSet.getShphf().extractKeyFromCurrentRecord();
            } catch (Exception _e3) {
            }
        }
        fileSet.getShphf().readByKey(rkVal_17 != null ? rkVal_17.trim() : "");
        ws.trySetString("FSTS", fileSet.getShphf().getFileStatus());
        if (fileSet.getShphf().isInvalidKey()) {
            return;
        }
        ws.setXhStatus(2);
        fileSet.getShphf().rewrite();
        ws.trySetString("FSTS", fileSet.getShphf().getFileStatus());
        if (fileSet.getShphf().isInvalidKey()) {
            /* CONTINUE */
        }
    }

    /** COBOL paragraph: UOS-010 */
    private void markOrderInvoiced() {
        if (ws.getXhOrder() == 0) {
            return;
        }
        ws.setOhNo(ws.getXhOrder());
        String rkVal_18 = "";
        if (rkVal_18 == null || rkVal_18.trim().isEmpty()) {
            try {
                rkVal_18 = ws.getString("OH-NO");
            } catch (Exception _e) {
            }
        }
        if (rkVal_18 == null || rkVal_18.trim().isEmpty()) {
            try {
                rkVal_18 = fileSet.getOrdhf().extractKeyFromCurrentRecord();
            } catch (Exception _e3) {
            }
        }
        fileSet.getOrdhf().readByKey(rkVal_18 != null ? rkVal_18.trim() : "");
        ws.trySetString("FSTS", fileSet.getOrdhf().getFileStatus());
        if (fileSet.getOrdhf().isInvalidKey()) {
            return;
        }
        if (ws.getOhStatus() == 3 || ws.getOhStatus() == 2) {
            ws.setOhStatus(4);
            ws.setOhUpdDate(ws.getWkSysdate());
            ws.setOhUpdUser(ws.getWkUserCode());
            fileSet.getOrdhf().rewrite();
            ws.trySetString("FSTS", fileSet.getOrdhf().getFileStatus());
            if (fileSet.getOrdhf().isInvalidKey()) {
                /* CONTINUE */
            }
        }
    }

    /** COBOL paragraph: CLP-010 */
    private void calculatePageCount() {
        if (ws.getWkDcnt() == 0) {
            ws.setWkPageCnt(1);
        } else {
            ws.setWkPageCnt((((ws.getWkDcnt() + ws.getWkPgsize()) - 1) / ws.getWkPgsize()));
        }
    }

    /** COBOL paragraph: BW-010 */
    private void buildReviewWindow() {
        for (ws.setWkIdx(1); ws.getWkIdx() <= ws.getWkPgsize(); ws.setWkIdx(ws.getWkIdx() + 1)) {
            ws.setWkIdx2(((ws.getWkPageTop() + ws.getWkIdx()) - 1));
            if (ws.getWkIdx2() <= ws.getWkDcnt() && ws.getWkIdx2() >= 1) {
                ws.setWwProd(ws.getWkIdx(), ws.getDrProd(ws.getWkIdx2()));
                ws.setWwName(ws.getWkIdx(), ws.getDrName(ws.getWkIdx2()));
                ws.setWwQty(ws.getWkIdx(), ws.getDrQty(ws.getWkIdx2()));
                ws.setWwPrice(ws.getWkIdx(), ws.getDrPrice(ws.getWkIdx2()));
                ws.setWwAmt(ws.getWkIdx(), ws.getDrAmt(ws.getWkIdx2()));
                ws.setWwCostamt(ws.getWkIdx(), ws.getDrCostamt(ws.getWkIdx2()));
            } else {
                ws.setWwProd(ws.getWkIdx(), 0);
                ws.setWwName(ws.getWkIdx(), " ");
                ws.setWwQty(ws.getWkIdx(), 0);
                ws.setWwPrice(ws.getWkIdx(), BigDecimal.ZERO);
                ws.setWwAmt(ws.getWkIdx(), 0);
                ws.setWwCostamt(ws.getWkIdx(), 0);
            }
        }
        ws.setWkPageNo((((ws.getWkPageTop() + ws.getWkPgsize()) - 1) / ws.getWkPgsize()));
    }

    /** COBOL paragraph: PIV-010 */
    private void displayInvoiceReviewScreen() {
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEADER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-FOOTER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-INV"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-TOTAL"), ws);
    }

    /** COBOL paragraph: PD-010 */
    private void pageDown() {
        if ((ws.getWkPageTop() + ws.getWkPgsize()) > ws.getWkDcnt()) {
            return;
        }
        ws.setWkPageTop(ws.getWkPageTop() + ws.getWkPgsize());
        runChain(this::buildReviewWindow);
    }

    /** COBOL paragraph: PU-010 */
    private void pageUp() {
        if (ws.getWkPageTop() <= 1) {
            return;
        }
        if (ws.getWkPageTop() > ws.getWkPgsize()) {
            ws.setWkPageTop(ws.getWkPageTop() - (ws.getWkPgsize()));
        } else {
            ws.setWkPageTop(1);
        }
        runChain(this::buildReviewWindow);
    }

    /** COBOL paragraph: TERM-010 */
    private void closeAllFiles() {
        fileSet.getInvhf().close();
        ws.trySetString("FSTS", fileSet.getInvhf().getFileStatus());
        fileSet.getInvdf().close();
        ws.trySetString("FSTS", fileSet.getInvdf().getFileStatus());
        fileSet.getShphf().close();
        ws.trySetString("FSTS", fileSet.getShphf().getFileStatus());
        fileSet.getShpdf().close();
        ws.trySetString("FSTS", fileSet.getShpdf().getFileStatus());
        fileSet.getOrdhf().close();
        ws.trySetString("FSTS", fileSet.getOrdhf().getFileStatus());
        fileSet.getStokf().close();
        ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
        fileSet.getCustf().close();
        ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
        fileSet.getProdf().close();
        ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
        fileSet.getCprcf().close();
        ws.trySetString("FSTS", fileSet.getCprcf().getFileStatus());
        fileSet.getArlf().close();
        ws.trySetString("FSTS", fileSet.getArlf().getFileStatus());
    }

    /** COBOL paragraph: ABEND-010 */
    private void abortWithFileOpenError() {
        ws.setKaProgid("SL0010");
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
     * Opens a file in the given mode, recreating it via OUTPUT+CLOSE+reopen if it does not yet
     * exist (status 35/30).
     */
    private void openWithRetry(RawDatasetBase file, FileOpenMode mode) {
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

    /**
     * Opens a file for I/O with retry, then aborts the program if the file still failed to open.
     */
    private void openIoOrAbend(RawDatasetBase file, String fileMei) {
        openWithRetry(file, FileOpenMode.IO);
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile(fileMei);
            runChain(this::abortWithFileOpenError);
        }
    }
}
