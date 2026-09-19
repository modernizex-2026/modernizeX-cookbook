package com.sakura.oe0040.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.credit.service.CreditService;
import com.sakura.dateut.service.DateutService;
import com.sakura.oe0040.domain.Oe0040FieldAccess;
import com.sakura.oe0040.domain.WorkingStorage;
import com.sakura.oe0040.runtime.Oe0040Datasets;
import com.sakura.oe0040.screen.ScreenDefs;
import com.sakura.runtime.BatchServiceBase;
import com.sakura.runtime.DatasetEnums.*;
import com.sakura.runtime.ProgramExitSignal;
import com.sakura.runtime.ScreenModels.*;
import com.sakura.runtime.ScreenRendererAware;
import com.sakura.runtime.ScreenRendererInstance;
import com.sakura.runtime.Utility;
import com.sakura.runtime.io.AbstractDatasets;
import com.sakura.runtime.linkage.AbortxLinkParm;
import com.sakura.runtime.linkage.CreditLinkParm;
import com.sakura.runtime.linkage.DateutLinkParm;
import com.sakura.runtime.linkage.TaxcalLinkParm;
import com.sakura.runtime.record.RawDatasetBase;
import com.sakura.taxcal.service.TaxcalService;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/** Business logic service generated from COBOL program OE0040. */
@Service
@Scope("prototype")
public class Oe0040Service extends BatchServiceBase {
    /** Maximum number of order detail lines held in working storage per order. */
    private static final int MAX_ORDER_LINES = 200;

    /** Shared file instances for all FD files in this program. */
    private final Oe0040Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL CREDIT. */
    private CreditService creditService;

    /** Injected service for COBOL CALL TAXCAL. */
    private TaxcalService taxcalService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Oe0040FieldAccess ws;

    public Oe0040Service(
            Oe0040Datasets fileSet,
            DateutService dateutService,
            CreditService creditService,
            TaxcalService taxcalService,
            AbortxService abortxService,
            ScreenRendererInstance renderer) {
        this.fileSet = fileSet;
        this.ws = new Oe0040FieldAccess(new WorkingStorage(), fileSet);
        this.dateutService = dateutService;
        this.creditService = creditService;
        this.taxcalService = taxcalService;
        this.abortxService = abortxService;
        this.renderer = renderer;
    }

    @Override
    public void setRenderer(ScreenRendererInstance renderer) {
        super.setRenderer(renderer);
        if (dateutService instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (creditService instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (taxcalService instanceof ScreenRendererAware rra) {
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
            runChain(this::displayAndAcceptOrderSelection);
        }
        runChain(this::closeAllFiles);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeProgram() {
        ws.setWkProgid("OE0040");
        ws.setWkTitle("Sales Order Maintenance");
        ws.setWkFkeyLine("ENTER=Read  PF3=End");
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
        runChain(this::openStockFile);
        fileSet.getCustf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
        if (Utility.fieldEquals(ws.getFsts(), "35") || Utility.fieldEquals(ws.getFsts(), "30")) {
            fileSet.getCustf().open(FileOpenMode.OUTPUT);
            ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
            fileSet.getCustf().close();
            ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
            fileSet.getCustf().open(FileOpenMode.INPUT);
            ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
        }
        fileSet.getProdf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
        if (Utility.fieldEquals(ws.getFsts(), "35") || Utility.fieldEquals(ws.getFsts(), "30")) {
            fileSet.getProdf().open(FileOpenMode.OUTPUT);
            ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
            fileSet.getProdf().close();
            ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
            fileSet.getProdf().open(FileOpenMode.INPUT);
            ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
        }
    }

    /** COBOL paragraph: OIOH-010 */
    private void openOrderHeaderFile() {
        fileSet.getOrdhf().open(FileOpenMode.IO);
        ws.trySetString("FSTS", fileSet.getOrdhf().getFileStatus());
        if (Utility.fieldEquals(ws.getFsts(), "35") || Utility.fieldEquals(ws.getFsts(), "30")) {
            fileSet.getOrdhf().open(FileOpenMode.OUTPUT);
            ws.trySetString("FSTS", fileSet.getOrdhf().getFileStatus());
            fileSet.getOrdhf().close();
            ws.trySetString("FSTS", fileSet.getOrdhf().getFileStatus());
            fileSet.getOrdhf().open(FileOpenMode.IO);
            ws.trySetString("FSTS", fileSet.getOrdhf().getFileStatus());
        }
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("ORDHF");
            runChain(this::abortOnFileOpenError);
        }
    }

    /** COBOL paragraph: OIOD-010 */
    private void openOrderDetailFile() {
        fileSet.getOrddf().open(FileOpenMode.IO);
        ws.trySetString("FSTS", fileSet.getOrddf().getFileStatus());
        if (Utility.fieldEquals(ws.getFsts(), "35") || Utility.fieldEquals(ws.getFsts(), "30")) {
            fileSet.getOrddf().open(FileOpenMode.OUTPUT);
            ws.trySetString("FSTS", fileSet.getOrddf().getFileStatus());
            fileSet.getOrddf().close();
            ws.trySetString("FSTS", fileSet.getOrddf().getFileStatus());
            fileSet.getOrddf().open(FileOpenMode.IO);
            ws.trySetString("FSTS", fileSet.getOrddf().getFileStatus());
        }
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("ORDDF");
            runChain(this::abortOnFileOpenError);
        }
    }

    /** COBOL paragraph: OIST-010 */
    private void openStockFile() {
        fileSet.getStokf().open(FileOpenMode.IO);
        ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
        if (Utility.fieldEquals(ws.getFsts(), "35") || Utility.fieldEquals(ws.getFsts(), "30")) {
            fileSet.getStokf().open(FileOpenMode.OUTPUT);
            ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
            fileSet.getStokf().close();
            ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
            fileSet.getStokf().open(FileOpenMode.IO);
            ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
        }
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("STOKF");
            runChain(this::abortOnFileOpenError);
        }
    }

    /** COBOL paragraph: MAINR-010 */
    private void displayAndAcceptOrderSelection() {
        runChain(this::clearOrderWorkArea);
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEADER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-FOOTER"), ws);
        ws.setWkMsgLine("Enter the order number to maintain");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-KEY"), ws);
        String scValWkSelOrd0 =
                Utility.acceptScreen(
                        "WK-SEL-ORD",
                        () -> renderer.acceptField(ScreenDefs.getInput("WK-SEL-ORD")));
        ws.setWkSelOrd(Utility.parseLongOr(scValWkSelOrd0.trim(), 0L));
        broadcastEstsStatus();
        switch (String.valueOf(ws.getEsts())) {
            case "03" -> {
                ws.setString("END-FLG", "1");
            }
            case "00" -> {
                runChain(this::recallOrder);
            }
            default -> {
                ws.setWkMsgLine("Invalid function key");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: CLRO-010 */
    private void clearOrderWorkArea() {
        fileSet.getOrdhf().setRecord();
        ws.setWkSelOrd(0);
        ws.setWkDcnt(0);
        ws.setWkDelcnt(0);
        ws.setWkNetTotal(0);
        ws.setWkTaxTotal(0);
        ws.setWkGrsTotal(0);
        ws.setWkOrigTotal(0);
        ws.setWkCancelFlg(0);
        ws.setWkAllocated(0);
        ws.setWkDirty(0);
        ws.setWkNextLine(0);
        ws.setWkPageTop(1);
        ws.setWkCustName(" ");
        ws.setWkStatTxt(" ");
        ws.setWkCmd(" ");
        ws.setWkConfirm(" ");
        ws.setWkCmdArg(0);
    }

    /** COBOL paragraph: RCL-010 */
    private void recallOrder() {
        if (ws.getWkSelOrd() == 0) {
            ws.setWkMsgLine("Order number required");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setOhNo(ws.getWkSelOrd());
        readByFallbackKey(fileSet.getOrdhf(), "OH-NO");
        if (fileSet.getOrdhf().isInvalidKey()) {
            ws.setWkMsgLine("Order not found");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        if (ws.getOhDelFlag() == 1) {
            ws.setWkMsgLine("Order is deleted");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        if (ws.getOhStatus() != 0 && ws.getOhStatus() != 1) {
            runChain(this::setStatusText);
            ws.setWkMsgLine(" ");
            ws.setWkMsgLine("Order status " + ws.getWkStatTxt() + " - cannot change");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setWkOrigTotal(ws.getOhTotal().longValue());
        if (ws.getOhStatus() == 1) {
            ws.setWkAllocated(1);
        } else {
            ws.setWkAllocated(0);
        }
        runChain(this::lookupCustomerName);
        runChain(this::setStatusText);
        runChain(this::startOrderDetailBrowse);
        runChain(this::computeOrderTotals);
        runChain(this::runMaintenanceLoop);
    }

    /** COBOL paragraph: LKC-010 */
    private void lookupCustomerName() {
        ws.setWkCustName(" ");
        ws.setCuCode(ws.getOhCust());
        readByFallbackKey(fileSet.getCustf(), "CU-CODE");
        if (fileSet.getCustf().isInvalidKey()) {
            ws.setWkCustName("(unknown customer)");
        }
        if (!fileSet.getCustf().isInvalidKey()) {
            ws.setWkCustName(ws.getCuName());
        }
    }

    /** COBOL paragraph: SST-010 */
    private void setStatusText() {
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

    /** COBOL paragraph: LDT-010 */
    private void startOrderDetailBrowse() {
        ws.setWkDcnt(0);
        ws.setWkDelcnt(0);
        ws.setWkNextLine(1);
        ws.setOdNo(ws.getWkSelOrd());
        ws.setOdLine(0);
        fileSet.getOrddf().start("OD-NO", "NOT LESS THAN");
        ws.trySetString("FSTS", fileSet.getOrddf().getFileStatus());
        if (fileSet.getOrddf().isInvalidKey()) {
            return;
        }
        // fall-through to next paragraph
        loadOrderDetailLines();
    }

    /** COBOL paragraph: LDT-020 */
    private void loadOrderDetailLines() {
        while (true) {
            fileSet.getOrddf().readNext();
            ws.trySetString("FSTS", fileSet.getOrddf().getFileStatus());
            if (fileSet.getOrddf().isAtEnd()) {
                return;
            }
            if (ws.getOdNo() != ws.getWkSelOrd()) {
                return;
            }
            if (ws.getWkDcnt() >= MAX_ORDER_LINES) {
                return;
            }
            ws.setWkDcnt(ws.getWkDcnt() + 1);
            ws.setDrLine(ws.getWkDcnt(), ws.getOdLine());
            ws.setDrProd(ws.getWkDcnt(), ws.getOdProd());
            ws.setDrWhse(ws.getWkDcnt(), ws.getOdWhse());
            ws.setDrQty(ws.getWkDcnt(), ws.getOdQty().intValue());
            ws.setDrPrice(ws.getWkDcnt(), ws.getOdUnitPrice());
            ws.setDrAmt(ws.getWkDcnt(), ws.getOdAmount().longValue());
            ws.setDrTaxcat(ws.getWkDcnt(), ws.getOdTaxCategory());
            ws.setDrAlloc(ws.getWkDcnt(), ws.getOdAllocQty().intValue());
            ws.setDrStatus(ws.getWkDcnt(), ws.getOdStatus());
            ws.setDrNew(ws.getWkDcnt(), 0);
            if (ws.getOdLine() >= ws.getWkNextLine()) {
                ws.setWkNextLine((ws.getOdLine() + 1));
            }
            ws.setPrCode(ws.getOdProd());
            readByFallbackKey(fileSet.getProdf(), "PR-CODE");
            if (fileSet.getProdf().isInvalidKey()) {
                ws.setDrName(ws.getWkDcnt(), "(unknown)");
                ws.setDrStkmng(ws.getWkDcnt(), 0);
            }
            if (!fileSet.getProdf().isInvalidKey()) {
                ws.setDrName(ws.getWkDcnt(), ws.getPrName());
                ws.setDrStkmng(ws.getWkDcnt(), ws.getPrStockMng());
            }
        }
    }

    /** COBOL paragraph: MNT-010 */
    private void runMaintenanceLoop() {
        ws.setWkMaintEnd(0);
        ws.setWkPageTop(1);
        ws.setWkFkeyLine("ENTER=Cmd  PF6/PF12=Page  PF3=Quit");
        runChain(this::computeLinePageCount);
        runChain(this::buildLineListWindow);
        ws.setWkMsgLine("Order loaded - enter a command");
        while (!((ws.getWkMaintEnd() == 1) || (ws.getEndFlg() == 1))) {
            runChain(this::displayMaintenanceScreen);
            ws.setWkCmd(" ");
            ws.setWkCmdArg(0);
            renderer.displayScreen(ScreenDefs.getScreen("DS-CMD"), ws);
            String scValWkCmd4 =
                    Utility.acceptScreen(
                            "WK-CMD", () -> renderer.acceptField(ScreenDefs.getInput("WK-CMD")));
            ws.setWkCmd(scValWkCmd4);
            String scValWkCmdArg5 =
                    Utility.acceptScreen(
                            "WK-CMD-ARG",
                            () -> renderer.acceptField(ScreenDefs.getInput("WK-CMD-ARG")));
            ws.setWkCmdArg(Utility.parseIntOr(scValWkCmdArg5.trim(), 0));
            broadcastEstsStatus();
            switch (String.valueOf(ws.getEsts())) {
                case "03" -> {
                    runChain(this::confirmQuitWithoutSaving);
                }
                case "06" -> {
                    runChain(this::pageDown);
                }
                case "12" -> {
                    runChain(this::pageUp);
                }
                case "00" -> {
                    runChain(this::dispatchMaintenanceCommand);
                }
                default -> {
                    ws.setWkMsgLine("Invalid function key");
                }
            }
        }
        ws.setWkFkeyLine("ENTER=Read  PF3=End");
    }

    /** COBOL paragraph: DSP-010 */
    private void dispatchMaintenanceCommand() {
        switch (String.valueOf(ws.getWkCmd())) {
                // COBOL stacked WHEN fall-through
            case "A", "a" -> {
                runChain(this::addOrderLine);
            }
                // COBOL stacked WHEN fall-through
            case "C", "c" -> {
                runChain(this::changeOrderLine);
            }
                // COBOL stacked WHEN fall-through
            case "D", "d" -> {
                runChain(this::deleteOrderLine);
            }
                // COBOL stacked WHEN fall-through
            case "H", "h" -> {
                runChain(this::changeOrderHeader);
            }
                // COBOL stacked WHEN fall-through
            case "X", "x" -> {
                runChain(this::cancelOrder);
            }
                // COBOL stacked WHEN fall-through
            case "S", "s" -> {
                runChain(this::confirmAndSaveOrder);
            }
            default -> {
                ws.setWkMsgLine("Enter A C D H X or S");
            }
        }
        runChain(this::computeOrderTotals);
        runChain(this::computeLinePageCount);
        runChain(this::buildLineListWindow);
    }

    /** COBOL paragraph: QC-010 */
    private void confirmQuitWithoutSaving() {
        if (ws.getWkDirty() == 0) {
            ws.setString("WK-MAINT-END", "1");
            ws.setWkMsgLine("No changes - order unchanged");
            return;
        }
        ws.setWkConfirm(" ");
        ws.setWkMsgLine("Discard unsaved changes ? (Y/N)");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-CONFIRM"), ws);
        String scValWkConfirm6 =
                Utility.acceptScreen(
                        "SC-CONF", () -> renderer.acceptField(ScreenDefs.getInput("SC-CONF")));
        ws.setWkConfirm(scValWkConfirm6);
        broadcastEstsStatus();
        if (isConfirmYes()) {
            ws.setString("WK-MAINT-END", "1");
            ws.setWkMsgLine("Changes discarded");
        } else {
            ws.setWkMsgLine("Continue editing");
        }
    }

    /** COBOL paragraph: ADL-010 */
    private void addOrderLine() {
        if (ws.getWkDcnt() >= MAX_ORDER_LINES) {
            ws.setWkMsgLine("Maximum 200 lines reached");
            return;
        }
        ws.setWkDProd(0);
        ws.setWkDWhse(0);
        ws.setWkDQty(0);
        ws.setWkDPrice(BigDecimal.ZERO);
        ws.setWkDAmt(0);
        ws.setWkDAvail(0);
        ws.setWkDTaxcat(1);
        ws.setWkDStkmng(0);
        ws.setWkDWhse(ws.getOhWhse());
        ws.setWkDName(" ");
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEADER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-FOOTER"), ws);
        ws.setWkMsgLine("Enter the new line - PF3 to abandon");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-DETAIL"), ws);
        String scValWkDProd7 =
                Utility.acceptScreen(
                        "WK-D-PROD", () -> renderer.acceptField(ScreenDefs.getInput("WK-D-PROD")));
        ws.setWkDProd(Utility.parseIntOr(scValWkDProd7.trim(), 0));
        String scValWkDWhse8 =
                Utility.acceptScreen(
                        "WK-D-WHSE", () -> renderer.acceptField(ScreenDefs.getInput("WK-D-WHSE")));
        ws.setWkDWhse(Utility.parseIntOr(scValWkDWhse8.trim(), 0));
        String scValWkDQty9 =
                Utility.acceptScreen(
                        "WK-D-QTY", () -> renderer.acceptField(ScreenDefs.getInput("WK-D-QTY")));
        ws.setWkDQty(Utility.parseIntOr(scValWkDQty9.trim(), 0));
        String scValWkDPrice10 =
                Utility.acceptScreen(
                        "WK-D-PRICE",
                        () -> renderer.acceptField(ScreenDefs.getInput("WK-D-PRICE")));
        try {
            ws.setWkDPrice(new BigDecimal(scValWkDPrice10.trim()));
        } catch (NumberFormatException _nfe) {
            ws.setWkDPrice(BigDecimal.ZERO);
        }
        broadcastEstsStatus();
        if (Utility.fieldEquals(ws.getEsts(), "03")) {
            ws.setWkMsgLine("Add abandoned");
            return;
        }
        runChain(this::validateNewLine);
        if ((ws.getErrFlg() == 1)) {
            return;
        }
        ws.setWkDcnt(ws.getWkDcnt() + 1);
        ws.setDrLine(ws.getWkDcnt(), 0);
        ws.setDrProd(ws.getWkDcnt(), ws.getWkDProd());
        ws.setDrWhse(ws.getWkDcnt(), ws.getWkDWhse());
        ws.setDrName(ws.getWkDcnt(), ws.getWkDName());
        ws.setDrQty(ws.getWkDcnt(), ws.getWkDQty());
        ws.setDrPrice(ws.getWkDcnt(), ws.getWkDPrice());
        ws.setDrAmt(ws.getWkDcnt(), ws.getWkDAmt());
        ws.setDrTaxcat(ws.getWkDcnt(), ws.getWkDTaxcat());
        ws.setDrAlloc(ws.getWkDcnt(), 0);
        ws.setDrStkmng(ws.getWkDcnt(), ws.getWkDStkmng());
        ws.setDrStatus(ws.getWkDcnt(), 0);
        ws.setDrNew(ws.getWkDcnt(), 1);
        ws.setWkDirty(1);
        ws.setWkMsgLine("Line added");
    }

    /** COBOL paragraph: VNL-010 */
    private void validateNewLine() {
        ws.setErrFlg(0);
        if (ws.getWkDProd() == 0) {
            ws.setWkMsgLine("Product code required");
            ws.setString("ERR-FLG", "1");
            displayLineValidationError();
            return;
        }
        ws.setPrCode(ws.getWkDProd());
        readByFallbackKey(fileSet.getProdf(), "PR-CODE");
        if (fileSet.getProdf().isInvalidKey()) {
            ws.setWkMsgLine("Product not found");
            ws.setString("ERR-FLG", "1");
            displayLineValidationError();
            return;
        }
        if (ws.getPrDelFlag() == 1) {
            ws.setWkMsgLine("Product is deleted");
            ws.setString("ERR-FLG", "1");
            displayLineValidationError();
            return;
        }
        ws.setWkDName(ws.getPrName());
        ws.setWkDTaxcat(ws.getPrTaxCategory());
        ws.setWkDStkmng(ws.getPrStockMng());
        if (ws.getWkDWhse() == 0) {
            ws.setWkDWhse(ws.getPrDfltWhse());
        }
        if (ws.getWkDQty() <= 0) {
            ws.setWkMsgLine("Quantity must be positive");
            ws.setString("ERR-FLG", "1");
            displayLineValidationError();
            return;
        }
        if ((ws.getWkDPrice().signum() <= 0)) {
            runChain(this::applyDefaultPrice);
        }
        ws.setWkDAmt(BigDecimal.valueOf(ws.getWkDQty()).multiply(ws.getWkDPrice()).longValue());
        // fall-through to next paragraph
        displayLineValidationError();
    }

    /** COBOL paragraph: VNL-999 */
    private void displayLineValidationError() {
        if ((ws.getErrFlg() == 1)) {
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
        /* CONTINUE */
    }

    /** COBOL paragraph: RPR-010 */
    private void applyDefaultPrice() {
        ws.setWkDPrice(BigDecimal.ZERO);
        if (ws.getCuPriceRank() >= 1 && ws.getCuPriceRank() <= 5) {
            ws.setWkDPrice(ws.getPrRankPrice(ws.getCuPriceRank()));
        }
        if ((ws.getWkDPrice().signum() <= 0)) {
            ws.setWkDPrice(ws.getPrListPrice());
        }
    }

    /** COBOL paragraph: CHL-010 */
    private void changeOrderLine() {
        if (ws.getWkCmdArg() == 0 || ws.getWkCmdArg() > ws.getWkDcnt()) {
            ws.setWkMsgLine("Enter a valid line number");
            return;
        }
        ws.setWkIdx(ws.getWkCmdArg());
        ws.setWkDProd(ws.getDrProd(ws.getWkIdx()));
        ws.setWkDWhse(ws.getDrWhse(ws.getWkIdx()));
        ws.setWkDName(ws.getDrName(ws.getWkIdx()));
        ws.setWkDQty(ws.getDrQty(ws.getWkIdx()));
        ws.setWkDPrice(ws.getDrPrice(ws.getWkIdx()));
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEADER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-FOOTER"), ws);
        ws.setWkMsgLine("Change quantity / price - PF3 to abandon");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-CHGLINE"), ws);
        String scValWkDQty12 =
                Utility.acceptScreen(
                        "WK-D-QTY", () -> renderer.acceptField(ScreenDefs.getInput("WK-D-QTY")));
        ws.setWkDQty(Utility.parseIntOr(scValWkDQty12.trim(), 0));
        String scValWkDPrice13 =
                Utility.acceptScreen(
                        "WK-D-PRICE",
                        () -> renderer.acceptField(ScreenDefs.getInput("WK-D-PRICE")));
        try {
            ws.setWkDPrice(new BigDecimal(scValWkDPrice13.trim()));
        } catch (NumberFormatException _nfe) {
            ws.setWkDPrice(BigDecimal.ZERO);
        }
        broadcastEstsStatus();
        if (Utility.fieldEquals(ws.getEsts(), "03")) {
            ws.setWkMsgLine("Change abandoned");
            return;
        }
        if (ws.getWkDQty() <= 0) {
            ws.setWkMsgLine("Quantity must be positive");
            return;
        }
        if ((ws.getWkDPrice().signum() < 0)) {
            ws.setWkMsgLine("Price cannot be negative");
            return;
        }
        ws.setDrQty(ws.getWkIdx(), ws.getWkDQty());
        ws.setDrPrice(ws.getWkIdx(), ws.getWkDPrice());
        ws.setDrAmt(
                ws.getWkIdx(),
                BigDecimal.valueOf(ws.getWkDQty()).multiply(ws.getWkDPrice()).longValue());
        ws.setWkDirty(1);
        ws.setWkMsgLine("Line changed");
    }

    /** COBOL paragraph: DLL-010 */
    private void deleteOrderLine() {
        if (ws.getWkCmdArg() == 0 || ws.getWkCmdArg() > ws.getWkDcnt()) {
            ws.setWkMsgLine("Enter a valid line number");
            return;
        }
        ws.setWkIdx(ws.getWkCmdArg());
        if (ws.getDrNew(ws.getWkIdx()) == 0) {
            ws.setWkDelcnt(ws.getWkDelcnt() + 1);
            ws.setDlLine(ws.getWkDelcnt(), ws.getDrLine(ws.getWkIdx()));
            ws.setDlProd(ws.getWkDelcnt(), ws.getDrProd(ws.getWkIdx()));
            ws.setDlWhse(ws.getWkDelcnt(), ws.getDrWhse(ws.getWkIdx()));
            ws.setDlAlloc(ws.getWkDelcnt(), ws.getDrAlloc(ws.getWkIdx()));
            ws.setDlStkmng(ws.getWkDelcnt(), ws.getDrStkmng(ws.getWkIdx()));
        }
        runChain(this::compactOrderLines);
        ws.setWkDirty(1);
        ws.setWkMsgLine("Line deleted");
    }

    /** COBOL paragraph: CMP-010 */
    private void compactOrderLines() {
        for (ws.setWkIdx2(ws.getWkIdx());
                ws.getWkIdx2() < ws.getWkDcnt();
                ws.setWkIdx2(ws.getWkIdx2() + 1)) {
            ws.setWkCnt((ws.getWkIdx2() + 1));
            ws.setDrLine(ws.getWkIdx2(), ws.getDrLine(ws.getWkCnt()));
            ws.setDrProd(ws.getWkIdx2(), ws.getDrProd(ws.getWkCnt()));
            ws.setDrWhse(ws.getWkIdx2(), ws.getDrWhse(ws.getWkCnt()));
            ws.setDrName(ws.getWkIdx2(), ws.getDrName(ws.getWkCnt()));
            ws.setDrQty(ws.getWkIdx2(), ws.getDrQty(ws.getWkCnt()));
            ws.setDrPrice(ws.getWkIdx2(), ws.getDrPrice(ws.getWkCnt()));
            ws.setDrAmt(ws.getWkIdx2(), ws.getDrAmt(ws.getWkCnt()));
            ws.setDrTaxcat(ws.getWkIdx2(), ws.getDrTaxcat(ws.getWkCnt()));
            ws.setDrAlloc(ws.getWkIdx2(), ws.getDrAlloc(ws.getWkCnt()));
            ws.setDrStkmng(ws.getWkIdx2(), ws.getDrStkmng(ws.getWkCnt()));
            ws.setDrStatus(ws.getWkIdx2(), ws.getDrStatus(ws.getWkCnt()));
            ws.setDrNew(ws.getWkIdx2(), ws.getDrNew(ws.getWkCnt()));
        }
        if (ws.getWkDcnt() > 0) {
            ws.setWkDcnt(ws.getWkDcnt() - (1));
        }
    }

    /** COBOL paragraph: CHH-010 */
    private void changeOrderHeader() {
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEADER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-FOOTER"), ws);
        ws.setWkMsgLine("Change header fields - PF3 to abandon");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-HDR"), ws);
        String scValOhDate14 =
                Utility.acceptScreen(
                        "OH-DATE", () -> renderer.acceptField(ScreenDefs.getInput("OH-DATE")));
        ws.setOhDate(Utility.parseIntOr(scValOhDate14.trim(), 0));
        String scValOhCust15 =
                Utility.acceptScreen(
                        "OH-CUST", () -> renderer.acceptField(ScreenDefs.getInput("OH-CUST")));
        ws.setOhCust(Utility.parseIntOr(scValOhCust15.trim(), 0));
        String scValOhStaff16 =
                Utility.acceptScreen(
                        "OH-STAFF", () -> renderer.acceptField(ScreenDefs.getInput("OH-STAFF")));
        ws.setOhStaff(Utility.parseIntOr(scValOhStaff16.trim(), 0));
        String scValOhWhse17 =
                Utility.acceptScreen(
                        "OH-WHSE", () -> renderer.acceptField(ScreenDefs.getInput("OH-WHSE")));
        ws.setOhWhse(Utility.parseIntOr(scValOhWhse17.trim(), 0));
        String scValOhDueDate18 =
                Utility.acceptScreen(
                        "OH-DUE-DATE",
                        () -> renderer.acceptField(ScreenDefs.getInput("OH-DUE-DATE")));
        ws.setOhDueDate(Utility.parseIntOr(scValOhDueDate18.trim(), 0));
        String scValOhCustPo19 =
                Utility.acceptScreen(
                        "OH-CUST-PO",
                        () -> renderer.acceptField(ScreenDefs.getInput("OH-CUST-PO")));
        ws.setOhCustPo(scValOhCustPo19);
        String scValOhTaxType20 =
                Utility.acceptScreen(
                        "OH-TAX-TYPE",
                        () -> renderer.acceptField(ScreenDefs.getInput("OH-TAX-TYPE")));
        ws.setOhTaxType(Utility.parseIntOr(scValOhTaxType20.trim(), 0));
        String scValOhRemark21 =
                Utility.acceptScreen(
                        "OH-REMARK", () -> renderer.acceptField(ScreenDefs.getInput("OH-REMARK")));
        ws.setOhRemark(scValOhRemark21);
        broadcastEstsStatus();
        if (Utility.fieldEquals(ws.getEsts(), "03")) {
            ws.setWkMsgLine("Header change abandoned");
            runChain(this::lookupCustomerName);
            return;
        }
        runChain(this::validateHeaderChanges);
        if ((ws.getErrFlg() == 1)) {
            return;
        }
        ws.setWkDirty(1);
        ws.setWkMsgLine("Header changed");
    }

    /** COBOL paragraph: VHD-010 */
    private void validateHeaderChanges() {
        ws.setErrFlg(0);
        if (ws.getOhCust() == 0) {
            ws.setWkMsgLine("Customer code required");
            ws.setString("ERR-FLG", "1");
            displayHeaderValidationError();
            return;
        }
        ws.setCuCode(ws.getOhCust());
        readByFallbackKey(fileSet.getCustf(), "CU-CODE");
        if (fileSet.getCustf().isInvalidKey()) {
            ws.setWkMsgLine("Customer not found");
            ws.setString("ERR-FLG", "1");
            displayHeaderValidationError();
            return;
        }
        if (ws.getCuDelFlag() == 1) {
            ws.setWkMsgLine("Customer is deleted");
            ws.setString("ERR-FLG", "1");
            displayHeaderValidationError();
            return;
        }
        ws.setWkCustName(ws.getCuName());
        if (ws.getOhTaxType() < 1 || ws.getOhTaxType() > 3) {
            ws.setWkMsgLine("Tax type must be 1, 2 or 3");
            ws.setString("ERR-FLG", "1");
        }
        // fall-through to next paragraph
        displayHeaderValidationError();
    }

    /** COBOL paragraph: VHD-999 */
    private void displayHeaderValidationError() {
        if ((ws.getErrFlg() == 1)) {
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
        /* CONTINUE */
    }

    /** COBOL paragraph: CXO-010 */
    private void cancelOrder() {
        ws.setWkConfirm(" ");
        ws.setWkMsgLine("Cancel the WHOLE order ? (Y/N)");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-CONFIRM"), ws);
        String scValWkConfirm23 =
                Utility.acceptScreen(
                        "SC-CONF", () -> renderer.acceptField(ScreenDefs.getInput("SC-CONF")));
        ws.setWkConfirm(scValWkConfirm23);
        broadcastEstsStatus();
        if (!isConfirmYes()) {
            ws.setWkMsgLine("Cancel not confirmed");
            return;
        }
        ws.setWkCancelFlg(1);
        ws.setWkDirty(1);
        runChain(this::saveOrder);
        if (ws.getWkCancelFlg() == 1) {
            ws.setString("WK-MAINT-END", "1");
        }
    }

    /** COBOL paragraph: SVC-010 */
    private void confirmAndSaveOrder() {
        if (ws.getWkDirty() == 0) {
            ws.setWkMsgLine("Nothing changed - nothing to save");
            return;
        }
        if (ws.getWkDcnt() == 0) {
            ws.setWkMsgLine("No lines - use X to cancel the order");
            return;
        }
        ws.setWkCancelFlg(0);
        ws.setWkConfirm(" ");
        runChain(this::computeOrderTotals);
        ws.setWkMsgLine("Save the changes ? (Y/N)");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-CONFIRM"), ws);
        String scValWkConfirm24 =
                Utility.acceptScreen(
                        "SC-CONF", () -> renderer.acceptField(ScreenDefs.getInput("SC-CONF")));
        ws.setWkConfirm(scValWkConfirm24);
        broadcastEstsStatus();
        if (isConfirmYes()) {
            runChain(this::saveOrder);
            ws.setString("WK-MAINT-END", "1");
        } else {
            ws.setWkMsgLine("Save cancelled");
        }
    }

    /** COBOL paragraph: SVO-010 */
    private void saveOrder() {
        runChain(this::computeOrderTotals);
        runChain(this::checkCustomerCredit);
        for (ws.setWkIdx(1); ws.getWkIdx() <= ws.getWkDelcnt(); ws.setWkIdx(ws.getWkIdx() + 1)) {
            if (ws.getDlStkmng(ws.getWkIdx()) == 1 && ws.getDlAlloc(ws.getWkIdx()) != 0) {
                ws.setWkApProd(ws.getDlProd(ws.getWkIdx()));
                ws.setWkApWhse(ws.getDlWhse(ws.getWkIdx()));
                ws.setWkApDelta((0 - ws.getDlAlloc(ws.getWkIdx())));
                runChain(this::adjustStockAllocation);
            }
            runChain(this::deleteOrderDetailRecord);
        }
        ws.setWkDelcnt(0);
        for (ws.setWkIdx(1); ws.getWkIdx() <= ws.getWkDcnt(); ws.setWkIdx(ws.getWkIdx() + 1)) {
            runChain(this::saveOrderDetailLine);
        }
        runChain(this::rewriteOrderHeader);
        if (ws.getWkCancelFlg() == 1) {
            ws.setWkMsgLine("Order cancelled");
        } else {
            ws.setWkMsgLine("Order saved");
        }
        ws.setWkDirty(0);
    }

    /** COBOL paragraph: SOD-010 */
    private void saveOrderDetailLine() {
        if (ws.getWkCancelFlg() == 1) {
            ws.setWkApTarget(0);
        } else {
            if (ws.getWkAllocated() == 1 && ws.getDrStkmng(ws.getWkIdx()) == 1) {
                ws.setWkApTarget(ws.getDrQty(ws.getWkIdx()));
            } else {
                ws.setWkApTarget(ws.getDrAlloc(ws.getWkIdx()));
            }
        }
        if (ws.getDrStkmng(ws.getWkIdx()) == 1) {
            ws.setWkApDelta((ws.getWkApTarget() - ws.getDrAlloc(ws.getWkIdx())));
            if (ws.getWkApDelta() != 0) {
                ws.setWkApProd(ws.getDrProd(ws.getWkIdx()));
                ws.setWkApWhse(ws.getDrWhse(ws.getWkIdx()));
                runChain(this::adjustStockAllocation);
            }
        }
        ws.setDrAlloc(ws.getWkIdx(), ws.getWkApTarget());
        runChain(this::buildOrderDetailRecord);
        if (ws.getDrNew(ws.getWkIdx()) == 1) {
            ws.setOdLine(ws.getWkNextLine());
            ws.setDrLine(ws.getWkIdx(), ws.getWkNextLine());
            ws.setWkNextLine(ws.getWkNextLine() + 1);
            ws.setDrNew(ws.getWkIdx(), 0);
            fileSet.getOrddf().write();
            ws.trySetString("FSTS", fileSet.getOrddf().getFileStatus());
            if (fileSet.getOrddf().isInvalidKey()) {
                ws.setWkMsgLine("Detail line write failed");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        } else {
            fileSet.getOrddf().rewrite();
            ws.trySetString("FSTS", fileSet.getOrddf().getFileStatus());
            if (fileSet.getOrddf().isInvalidKey()) {
                fileSet.getOrddf().write();
                ws.trySetString("FSTS", fileSet.getOrddf().getFileStatus());
                if (fileSet.getOrddf().isInvalidKey()) {
                    ws.setWkMsgLine("Detail rewrite failed");
                    renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
                }
            }
        }
    }

    /** COBOL paragraph: BOD-010 */
    private void buildOrderDetailRecord() {
        fileSet.getOrddf().setRecord();
        ws.setOdNo(ws.getWkSelOrd());
        ws.setOdLine(ws.getDrLine(ws.getWkIdx()));
        ws.setOdProd(ws.getDrProd(ws.getWkIdx()));
        ws.setOdWhse(ws.getDrWhse(ws.getWkIdx()));
        ws.setOdQty(BigDecimal.valueOf(ws.getDrQty(ws.getWkIdx())));
        ws.setOdUnitPrice(ws.getDrPrice(ws.getWkIdx()));
        ws.setOdAmount(BigDecimal.valueOf(ws.getDrAmt(ws.getWkIdx())));
        ws.setOdTaxCategory(ws.getDrTaxcat(ws.getWkIdx()));
        ws.setOdShippedQty(BigDecimal.ZERO);
        ws.setOdAllocQty(BigDecimal.valueOf(ws.getDrAlloc(ws.getWkIdx())));
        ws.setOdDueDate(ws.getOhDueDate());
        if (ws.getWkCancelFlg() == 1) {
            ws.setOdStatus(9);
        } else {
            ws.setOdStatus(ws.getDrStatus(ws.getWkIdx()));
        }
        ws.setOdRemark(" ");
    }

    /** COBOL paragraph: DOD-010 */
    private void deleteOrderDetailRecord() {
        ws.setOdNo(ws.getWkSelOrd());
        ws.setOdLine(ws.getDlLine(ws.getWkIdx()));
        fileSet.getOrddf().delete();
        ws.trySetString("FSTS", fileSet.getOrddf().getFileStatus());
        if (fileSet.getOrddf().isInvalidKey()) {
            /* CONTINUE */
        }
    }

    /** COBOL paragraph: AJA-010 */
    private void adjustStockAllocation() {
        if (ws.getWkApDelta() == 0) {
            return;
        }
        ws.setSkProd(ws.getWkApProd());
        ws.setSkWhse(ws.getWkApWhse());
        StringBuilder rkSb_25 = new StringBuilder();
        String rkPart0_25 = "";
        try {
            rkPart0_25 = ws.getString("SK-PROD");
        } catch (Exception _e) {
        }
        rkSb_25.append(rkPart0_25 != null ? rkPart0_25.trim() : "");
        String rkPart1_25 = "";
        try {
            rkPart1_25 = ws.getString("SK-WHSE");
        } catch (Exception _e) {
        }
        rkSb_25.append('|');
        rkSb_25.append(rkPart1_25 != null ? rkPart1_25.trim() : "");
        fileSet.getStokf().readByKey(rkSb_25.toString());
        ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
        if (fileSet.getStokf().isInvalidKey()) {
            if (ws.getWkApDelta() > 0) {
                fileSet.getStokf().setRecord();
                ws.setSkProd(ws.getWkApProd());
                ws.setSkWhse(ws.getWkApWhse());
                ws.setSkAllocated(BigDecimal.valueOf(ws.getWkApDelta()));
                fileSet.getStokf().write();
                ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
                if (fileSet.getStokf().isInvalidKey()) {
                    /* CONTINUE */
                }
            }
            return;
        }
        ws.setSkAllocated(ws.getSkAllocated().add(BigDecimal.valueOf(ws.getWkApDelta())));
        if ((ws.getSkAllocated().signum() < 0)) {
            ws.setSkAllocated(BigDecimal.ZERO);
        }
        fileSet.getStokf().rewrite();
        ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
        if (fileSet.getStokf().isInvalidKey()) {
            /* CONTINUE */
        }
    }

    /** COBOL paragraph: RWH-010 */
    private void rewriteOrderHeader() {
        ws.setOhNo(ws.getWkSelOrd());
        ws.setOhAmount(BigDecimal.valueOf(ws.getWkNetTotal()));
        ws.setOhTaxAmount(BigDecimal.valueOf(ws.getWkTaxTotal()));
        ws.setOhTotal(BigDecimal.valueOf(ws.getWkGrsTotal()));
        ws.setOhLines(ws.getWkDcnt());
        if (ws.getWkCancelFlg() == 1) {
            ws.setOhStatus(9);
        }
        ws.setOhUpdDate(ws.getWkSysdate());
        ws.setOhUpdUser(ws.getWkUserCode());
        fileSet.getOrdhf().rewrite();
        ws.trySetString("FSTS", fileSet.getOrdhf().getFileStatus());
        if (fileSet.getOrdhf().isInvalidKey()) {
            ws.setWkMsgLine("Header rewrite failed");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
    }

    /** COBOL paragraph: CRC-010 */
    private void checkCustomerCredit() {
        if (ws.getWkCancelFlg() == 1) {
            return;
        }
        ws.setKcCust(ws.getOhCust());
        ws.setKcAmount(
                (BigDecimal.valueOf(ws.getWkGrsTotal())
                                .subtract(BigDecimal.valueOf(ws.getWkOrigTotal())))
                        .setScale(0, java.math.RoundingMode.DOWN));
        credit(ws.getKcred());
        if (Utility.fieldEquals(ws.getKcStatus(), "00") && ws.getKcExceed() == 1) {
            ws.setWkMsgLine("Warning: customer credit limit exceeded");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
    }

    /** COBOL paragraph: CTT-010 */
    private void computeOrderTotals() {
        ws.setWkNetTotal(0);
        for (ws.setWkIdx(1); ws.getWkIdx() <= ws.getWkDcnt(); ws.setWkIdx(ws.getWkIdx() + 1)) {
            ws.setWkNetTotal(ws.getWkNetTotal() + ws.getDrAmt(ws.getWkIdx()));
        }
        ws.setKtCategory(1);
        ws.setKtTaxType(ws.getOhTaxType());
        if (ws.getKtTaxType() < 1 || ws.getKtTaxType() > 3) {
            ws.setKtTaxType(1);
        }
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

    /** COBOL paragraph: CLP-010 */
    private void computeLinePageCount() {
        if (ws.getWkDcnt() == 0) {
            ws.setWkPageCnt(1);
        } else {
            ws.setWkPageCnt((((ws.getWkDcnt() + ws.getWkPgsize()) - 1) / ws.getWkPgsize()));
        }
        if (ws.getWkPageTop() > ws.getWkDcnt()) {
            ws.setWkPageTop(1);
        }
    }

    /** COBOL paragraph: BLW-010 */
    private void buildLineListWindow() {
        for (ws.setWkIdx(1); ws.getWkIdx() <= ws.getWkPgsize(); ws.setWkIdx(ws.getWkIdx() + 1)) {
            ws.setWkIdx2(((ws.getWkPageTop() + ws.getWkIdx()) - 1));
            if (ws.getWkIdx2() <= ws.getWkDcnt() && ws.getWkIdx2() >= 1) {
                ws.setWwLine(ws.getWkIdx(), ws.getWkIdx2());
                ws.setWwProd(ws.getWkIdx(), ws.getDrProd(ws.getWkIdx2()));
                ws.setWwName(ws.getWkIdx(), ws.getDrName(ws.getWkIdx2()));
                ws.setWwQty(ws.getWkIdx(), ws.getDrQty(ws.getWkIdx2()));
                ws.setWwPrice(ws.getWkIdx(), ws.getDrPrice(ws.getWkIdx2()));
                ws.setWwAmt(ws.getWkIdx(), ws.getDrAmt(ws.getWkIdx2()));
                ws.setWwAlloc(ws.getWkIdx(), ws.getDrAlloc(ws.getWkIdx2()));
            } else {
                ws.setWwLine(ws.getWkIdx(), 0);
                ws.setWwProd(ws.getWkIdx(), 0);
                ws.setWwName(ws.getWkIdx(), " ");
                ws.setWwQty(ws.getWkIdx(), 0);
                ws.setWwPrice(ws.getWkIdx(), BigDecimal.ZERO);
                ws.setWwAmt(ws.getWkIdx(), 0);
                ws.setWwAlloc(ws.getWkIdx(), 0);
            }
        }
        ws.setWkPageNo((((ws.getWkPageTop() + ws.getWkPgsize()) - 1) / ws.getWkPgsize()));
    }

    /** COBOL paragraph: PGD-010 */
    private void pageDown() {
        if ((ws.getWkPageTop() + ws.getWkPgsize()) > ws.getWkDcnt()) {
            ws.setWkMsgLine("Last page");
            return;
        }
        ws.setWkPageTop(ws.getWkPageTop() + ws.getWkPgsize());
        runChain(this::buildLineListWindow);
        ws.setWkMsgLine(" ");
    }

    /** COBOL paragraph: PGU-010 */
    private void pageUp() {
        if (ws.getWkPageTop() <= 1) {
            ws.setWkMsgLine("First page");
            return;
        }
        if (ws.getWkPageTop() > ws.getWkPgsize()) {
            ws.setWkPageTop(ws.getWkPageTop() - (ws.getWkPgsize()));
        } else {
            ws.setWkPageTop(1);
        }
        runChain(this::buildLineListWindow);
        ws.setWkMsgLine(" ");
    }

    /** COBOL paragraph: PNT-010 */
    private void displayMaintenanceScreen() {
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEADER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-FOOTER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-INFO"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-LIST"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-TOTAL"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
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
    }

    /** COBOL paragraph: ABEND-010 */
    private void abortOnFileOpenError() {
        ws.setKaProgid("OE0040");
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

    /** COBOL CALL CREDIT — delegates to injected CreditService. */
    private void credit(Object... args) {
        CreditLinkParm params = new CreditLinkParm();
        params.getKcred().setKcCust(ws.getKcCust());
        params.getKcred().setKcAmount(ws.getKcAmount());
        params.getKcred().setKcLimit(ws.getKcLimit());
        params.getKcred().setKcBalance(ws.getKcBalance());
        params.getKcred().setKcNewbal(ws.getKcNewbal());
        params.getKcred().setKcExceed(ws.getKcExceed());
        params.getKcred().setKcStatus(ws.getKcStatus());
        creditService.execute(params);
        ws.setKcCust(params.getKcred().getKcCust());
        ws.setKcAmount(params.getKcred().getKcAmount());
        ws.setKcLimit(params.getKcred().getKcLimit());
        ws.setKcBalance(params.getKcred().getKcBalance());
        ws.setKcNewbal(params.getKcred().getKcNewbal());
        ws.setKcExceed(params.getKcred().getKcExceed());
        ws.setKcStatus(params.getKcred().getKcStatus());
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

    /** True when the operator confirmed a Y/N prompt with Y (either case). */
    private boolean isConfirmYes() {
        return ws.getWkConfirm().equals("Y") || ws.getWkConfirm().equals("y");
    }

    /**
     * Reads a record by falling back from the ws key value to the file's current key, then
     * propagates FSTS.
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
}
