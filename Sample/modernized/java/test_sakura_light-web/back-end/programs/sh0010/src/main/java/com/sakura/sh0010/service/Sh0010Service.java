package com.sakura.sh0010.service;

import com.sakura.abortx.service.AbortxService;
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
import com.sakura.sh0010.domain.Sh0010FieldAccess;
import com.sakura.sh0010.domain.WorkingStorage;
import com.sakura.sh0010.runtime.Sh0010Datasets;
import com.sakura.sh0010.screen.ScreenDefs;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/** Business logic service generated from COBOL program SH0010. */
@Service
@Scope("prototype")
public class Sh0010Service extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final Sh0010Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL NUMGEN. */
    private NumgenService numgenService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Sh0010FieldAccess ws;

    public Sh0010Service(
            Sh0010Datasets fileSet,
            DateutService dateutService,
            NumgenService numgenService,
            AbortxService abortxService,
            ScreenRendererInstance renderer) {
        this.fileSet = fileSet;
        this.ws = new Sh0010FieldAccess(new WorkingStorage(), fileSet);
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
            runChain(this::processMainScreen);
        }
        runChain(this::closeAllFiles);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeProgram() {
        ws.setWkProgid("SH0010");
        ws.setWkTitle("Shipping Entry");
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
        runChain(this::openShipmentHeaderFile);
        runChain(this::openShipmentDetailFile);
        runChain(this::openStockFile);
        runChain(this::openStockMovementFile);
        fileSet.getCustf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
        fileSet.getProdf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
    }

    /** COBOL paragraph: OIOH-010 */
    private void openOrderHeaderFile() {
        openIndexedFileWithRetry(fileSet.getOrdhf(), "ORDHF");
    }

    /** COBOL paragraph: OIOD-010 */
    private void openOrderDetailFile() {
        openIndexedFileWithRetry(fileSet.getOrddf(), "ORDDF");
    }

    /** COBOL paragraph: OISH-010 */
    private void openShipmentHeaderFile() {
        openIndexedFileWithRetry(fileSet.getShphf(), "SHPHF");
    }

    /** COBOL paragraph: OISD-010 */
    private void openShipmentDetailFile() {
        openIndexedFileWithRetry(fileSet.getShpdf(), "SHPDF");
    }

    /** COBOL paragraph: OIST-010 */
    private void openStockFile() {
        openIndexedFileWithRetry(fileSet.getStokf(), "STOKF");
    }

    /** COBOL paragraph: OISM-010 */
    private void openStockMovementFile() {
        openIndexedFileWithRetry(fileSet.getSmovf(), "SMOVF");
    }

    /** COBOL paragraph: MAINR-010 */
    private void processMainScreen() {
        runChain(this::clearWorkingFields);
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEADER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-FOOTER"), ws);
        ws.setWkMsgLine("Enter the order number to ship");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-KEY"), ws);
        String scValWkSelNo0 =
                Utility.acceptScreen(
                        "WK-SEL-NO", () -> renderer.acceptField(ScreenDefs.getInput("WK-SEL-NO")));
        ws.setWkSelNo(Utility.parseLongOr(scValWkSelNo0.trim(), 0L));
        broadcastEstsStatus();
        switch (String.valueOf(ws.getEsts())) {
            case "03" -> {
                ws.setString("END-FLG", "1");
            }
            case "00" -> {
                runChain(this::processOrderSelection);
            }
            default -> {
                ws.setWkMsgLine("Invalid function key");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: CLRW-010 */
    private void clearWorkingFields() {
        ws.setWkSelNo(0);
        ws.setWkOrderNo(0);
        ws.setWkShipNo(0);
        ws.setWkDcnt(0);
        ws.setWkTotShip(0);
        ws.setWkTotAmt(0);
        ws.setWkPageTop(1);
        ws.setWkEditLn(0);
        ws.setWkEditQty(0);
        ws.setWkCustName(" ");
        ws.setWkConfirm(" ");
    }

    /** COBOL paragraph: PORD-010 */
    private void processOrderSelection() {
        if (ws.getWkSelNo() == 0) {
            ws.setWkMsgLine("Order number must not be zero");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setOhNo(ws.getWkSelNo());
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
        if (ws.getOhStatus() == 0) {
            ws.setWkMsgLine("Order is not allocated yet - run OE0030");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        if (ws.getOhStatus() != 1 && ws.getOhStatus() != 2) {
            ws.setWkMsgLine("Order cannot be shipped in its status");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setWkOrderNo(ws.getOhNo());
        runChain(this::loadCustomerName);
        runChain(this::setStatusText);
        runChain(this::startOrderLineScan);
        if (ws.getWkDcnt() == 0) {
            ws.setWkMsgLine("Order has no detail lines");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        runChain(this::recalcTotalShipQty);
        runChain(this::computePageCount);
        ws.setWkPageTop(1);
        runChain(this::buildScreenWindow);
        runChain(this::runShipLineEditLoop);
    }

    /** COBOL paragraph: LKC-010 */
    private void loadCustomerName() {
        ws.setWkCustName(" ");
        ws.setCuCode(ws.getOhCust());
        readByFallbackKey(fileSet.getCustf(), "CU-CODE");
        if (fileSet.getCustf().isInvalidKey()) {
            ws.setWkCustName("??? unknown customer");
        }
        if (!fileSet.getCustf().isInvalidKey()) {
            ws.setWkCustName(ws.getCuName());
        }
    }

    /** COBOL paragraph: SST-010 */
    private void setStatusText() {
        switch (ws.getOhStatus()) {
            case 0 -> {
                ws.setWkStatText("Entered");
            }
            case 1 -> {
                ws.setWkStatText("Allocated");
            }
            case 2 -> {
                ws.setWkStatText("Part-ship");
            }
            case 3 -> {
                ws.setWkStatText("Shipped");
            }
            case 4 -> {
                ws.setWkStatText("Invoiced");
            }
            case 9 -> {
                ws.setWkStatText("Cancelled");
            }
            default -> {
                ws.setWkStatText("Unknown");
            }
        }
    }

    /** COBOL paragraph: LOL-010 */
    private void startOrderLineScan() {
        ws.setWkDcnt(0);
        ws.setOdNo(ws.getWkOrderNo());
        ws.setOdLine(0);
        fileSet.getOrddf().start("OD-NO", "NOT LESS THAN");
        ws.trySetString("FSTS", fileSet.getOrddf().getFileStatus());
        if (fileSet.getOrddf().isInvalidKey()) {
            return;
        }
        // fall-through to next paragraph
        loadOrderLines();
    }

    /** COBOL paragraph: LOL-020 */
    private void loadOrderLines() {
        while (true) {
            fileSet.getOrddf().readNext();
            ws.trySetString("FSTS", fileSet.getOrddf().getFileStatus());
            if (fileSet.getOrddf().isAtEnd()) {
                return;
            }
            if (ws.getOdNo() != ws.getWkOrderNo()) {
                return;
            }
            if (ws.getWkDcnt() >= 200) {
                return;
            }
            ws.setWkDcnt(ws.getWkDcnt() + 1);
            ws.setDrLine(ws.getWkDcnt(), ws.getOdLine());
            ws.setDrProd(ws.getWkDcnt(), ws.getOdProd());
            ws.setDrWhse(ws.getWkDcnt(), ws.getOdWhse());
            ws.setDrOrd(ws.getWkDcnt(), ws.getOdQty().intValue());
            ws.setDrShipped(ws.getWkDcnt(), ws.getOdShippedQty().intValue());
            ws.setDrAlloc(ws.getWkDcnt(), ws.getOdAllocQty().intValue());
            ws.setDrPrice(ws.getWkDcnt(), ws.getOdUnitPrice());
            runChain(this::loadProductName);
            ws.setDrName(ws.getWkDcnt(), ws.getWkProdName());
            runChain(this::computeShipQuantity);
            ws.setDrShip(ws.getWkDcnt(), ws.getWkShipq());
        }
    }

    /** COBOL paragraph: PRS-010 */
    private void computeShipQuantity() {
        ws.setWkRemain(ws.getOdQty().subtract(ws.getOdShippedQty()).intValue());
        if (ws.getWkRemain() < 0) {
            ws.setWkRemain(0);
        }
        if ((ws.getOdAllocQty().compareTo(BigDecimal.valueOf((long) (ws.getWkRemain()))) < 0)) {
            ws.setWkShipq(ws.getOdAllocQty().intValue());
        } else {
            ws.setWkShipq(ws.getWkRemain());
        }
        if (ws.getWkShipq() < 0) {
            ws.setWkShipq(0);
        }
    }

    /** COBOL paragraph: LKP-010 */
    private void loadProductName() {
        ws.setWkProdName(" ");
        ws.setWkIdx(1);
        ws.setPrCode(ws.getOdProd());
        readByFallbackKey(fileSet.getProdf(), "PR-CODE");
        if (fileSet.getProdf().isInvalidKey()) {
            ws.setWkProdName("??");
            ws.setWkIdx(1);
        }
        if (!fileSet.getProdf().isInvalidKey()) {
            ws.setWkProdName(ws.getPrName());
            ws.setWkIdx(ws.getPrStockMng());
        }
        ws.setDrStkmng(ws.getWkDcnt(), ws.getWkIdx());
    }

    /** COBOL paragraph: RCT-010 */
    private void recalcTotalShipQty() {
        ws.setWkTotShip(0);
        for (ws.setWkIdx(1); ws.getWkIdx() <= ws.getWkDcnt(); ws.setWkIdx(ws.getWkIdx() + 1)) {
            ws.setWkTotShip(ws.getWkTotShip() + ws.getDrShip(ws.getWkIdx()));
        }
    }

    /** COBOL paragraph: SL-010 */
    private void runShipLineEditLoop() {
        ws.setWkShipEnd(0);
        ws.setWkFkeyLine("ENTER=set  PF4=zero-all  PF6/PF12=page  PF3=post");
        while ((ws.getWkShipEnd() != 1)) {
            runChain(this::displayOrderScreens);
            ws.setWkMsgLine("Adjust ship qty by line, PF3 to post");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            ws.setWkEditLn(0);
            ws.setWkEditQty(0);
            renderer.displayScreen(ScreenDefs.getScreen("DS-EDIT"), ws);
            String scValWkEditLn4 =
                    Utility.acceptScreen(
                            "WK-EDIT-LN",
                            () -> renderer.acceptField(ScreenDefs.getInput("WK-EDIT-LN")));
            ws.setWkEditLn(Utility.parseIntOr(scValWkEditLn4.trim(), 0));
            String scValWkEditQty5 =
                    Utility.acceptScreen(
                            "WK-EDIT-QTY",
                            () -> renderer.acceptField(ScreenDefs.getInput("WK-EDIT-QTY")));
            ws.setWkEditQty(Utility.parseIntOr(scValWkEditQty5.trim(), 0));
            broadcastEstsStatus();
            switch (String.valueOf(ws.getEsts())) {
                case "03" -> {
                    ws.setString("WK-SHIP-END", "1");
                }
                case "04" -> {
                    runChain(this::zeroAllShipQuantities);
                }
                case "06" -> {
                    runChain(this::pageDown);
                }
                case "12" -> {
                    runChain(this::pageUp);
                }
                case "00" -> {
                    runChain(this::applyLineEdit);
                }
                default -> {
                    ws.setWkMsgLine("Invalid key");
                    renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
                }
            }
        }
        ws.setWkFkeyLine("ENTER=Read  PF3=End");
        if (ws.getWkTotShip() <= 0) {
            ws.setWkMsgLine("Nothing to ship - shipment discarded");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        } else {
            runChain(this::confirmShipment);
        }
    }

    /** COBOL paragraph: ZA-010 */
    private void zeroAllShipQuantities() {
        for (ws.setWkIdx(1); ws.getWkIdx() <= ws.getWkDcnt(); ws.setWkIdx(ws.getWkIdx() + 1)) {
            ws.setDrShip(ws.getWkIdx(), 0);
        }
        runChain(this::recalcTotalShipQty);
        runChain(this::buildScreenWindow);
    }

    /** COBOL paragraph: AE-010 */
    private void applyLineEdit() {
        if (ws.getWkEditLn() == 0) {
            ws.setWkMsgLine("Enter a line number to change");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setFoundFlg(0);
        for (ws.setWkIdx(1); ws.getWkIdx() <= ws.getWkDcnt(); ws.setWkIdx(ws.getWkIdx() + 1)) {
            if (ws.getDrLine(ws.getWkIdx()) == ws.getWkEditLn()) {
                ws.setString("FOUND-FLG", "1");
                ws.setWkIdx2(ws.getWkIdx());
            }
        }
        if ((ws.getFoundFlg() != 1)) {
            ws.setWkMsgLine("Line number not found");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        if (ws.getWkEditQty() < 0) {
            ws.setWkMsgLine("Quantity cannot be negative");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setWkRemain((ws.getDrOrd(ws.getWkIdx2()) - ws.getDrShipped(ws.getWkIdx2())));
        if (ws.getWkRemain() < 0) {
            ws.setWkRemain(0);
        }
        ws.setWkCap(ws.getWkRemain());
        if (ws.getDrStkmng(ws.getWkIdx2()) == 1) {
            if (ws.getDrAlloc(ws.getWkIdx2()) < ws.getWkCap()) {
                ws.setWkCap(ws.getDrAlloc(ws.getWkIdx2()));
            }
        }
        if (ws.getWkEditQty() > ws.getWkCap()) {
            ws.setDrShip(ws.getWkIdx2(), ws.getWkCap());
            ws.setWkMsgLine("Quantity capped to allocated / outstanding");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        } else {
            ws.setDrShip(ws.getWkIdx2(), ws.getWkEditQty());
            ws.setWkMsgLine("Line updated");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
        runChain(this::recalcTotalShipQty);
        runChain(this::buildScreenWindow);
    }

    /** COBOL paragraph: CS-010 */
    private void confirmShipment() {
        ws.setWkConfirm(" ");
        runChain(this::displayOrderScreens);
        ws.setWkMsgLine("Confirm shipment (Y) or PF3 to cancel");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-CONFIRM"), ws);
        String scValWkConfirm6 =
                Utility.acceptScreen(
                        "WK-CONFIRM",
                        () -> renderer.acceptField(ScreenDefs.getInput("WK-CONFIRM")));
        ws.setWkConfirm(scValWkConfirm6);
        broadcastEstsStatus();
        if (Utility.fieldEquals(ws.getEsts(), "03")) {
            ws.setWkMsgLine("Shipment cancelled");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        if ((ws.getWkConfirm().equals("Y") || ws.getWkConfirm().equals("y"))) {
            runChain(this::postShipment);
        } else {
            ws.setWkMsgLine("Shipment cancelled");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
    }

    /** COBOL paragraph: PS-010 */
    private void postShipment() {
        ws.setKnumKey("SHIP");
        numgen(ws.getKnum());
        if (!Utility.fieldEquals(ws.getKnumStatus(), "00")) {
            ws.setWkMsgLine("Ship number assignment failed");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setWkShipNo(ws.getKnumNumber());
        runChain(this::writeShipmentHeader);
        ws.setWkXline(0);
        for (ws.setWkIdx(1); ws.getWkIdx() <= ws.getWkDcnt(); ws.setWkIdx(ws.getWkIdx() + 1)) {
            if (ws.getDrShip(ws.getWkIdx()) > 0) {
                runChain(this::postShipmentLine);
            }
        }
        ws.setWkShipCnt(ws.getWkXline());
        runChain(this::finalizeShipmentHeader);
        runChain(this::updateOrderStatus);
        ws.setWkMsgLine(" ");
        {
            StringBuilder sb = new StringBuilder();
            sb.append(String.valueOf("Shipment "));
            sb.append(String.valueOf(String.format("%010d", (long) (ws.getWkShipNo()))));
            sb.append(String.valueOf(" created - "));
            sb.append(String.valueOf(String.format("%03d", (long) (ws.getWkShipCnt()))));
            sb.append(String.valueOf(" line(s)"));
            ws.setWkMsgLine(sb.toString());
        }
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
    }

    /** COBOL paragraph: WSH-010 */
    private void writeShipmentHeader() {
        fileSet.getShphf().setRecord();
        ws.setXhNo(ws.getWkShipNo());
        ws.setXhDate(ws.getWkSysdate());
        ws.setXhOrder(ws.getWkOrderNo());
        ws.setXhCust(ws.getOhCust());
        ws.setXhWhse(ws.getOhWhse());
        ws.setXhStaff(ws.getOhStaff());
        ws.setXhStatus(1);
        ws.setXhLines(0);
        ws.setXhRemark(ws.getOhRemark());
        ws.setXhAddDate(ws.getWkSysdate());
        ws.setXhAddUser(ws.getWkUserCode());
        ws.setXhDelFlag(0);
        fileSet.getShphf().write();
        ws.trySetString("FSTS", fileSet.getShphf().getFileStatus());
        if (fileSet.getShphf().isInvalidKey()) {
            ws.setWkMsgLine("Shipment header write failed");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
    }

    /** COBOL paragraph: FSH-010 */
    private void finalizeShipmentHeader() {
        ws.setXhNo(ws.getWkShipNo());
        readByFallbackKey(fileSet.getShphf(), "XH-NO");
        if (fileSet.getShphf().isInvalidKey()) {
            return;
        }
        ws.setXhLines(ws.getWkShipCnt());
        fileSet.getShphf().rewrite();
        ws.trySetString("FSTS", fileSet.getShphf().getFileStatus());
        if (fileSet.getShphf().isInvalidKey()) {
            /* CONTINUE */
        }
    }

    /** COBOL paragraph: SOL-010 */
    private void postShipmentLine() {
        ws.setWkShipq(ws.getDrShip(ws.getWkIdx()));
        ws.setWkCost(BigDecimal.ZERO);
        runChain(this::updateStock);
        ws.setWkXline(ws.getWkXline() + 1);
        runChain(this::writeShipmentDetail);
        runChain(this::writeStockMovement);
        runChain(this::updateOrderLine);
        ws.setWkTotAmt(
                BigDecimal.valueOf(ws.getWkTotAmt())
                        .add(
                                BigDecimal.valueOf(ws.getWkShipq())
                                        .multiply(ws.getDrPrice(ws.getWkIdx())))
                        .longValue());
    }

    /** COBOL paragraph: US-010 */
    private void updateStock() {
        ws.setWkStkFound(0);
        if (ws.getDrStkmng(ws.getWkIdx()) == 0) {
            return;
        }
        ws.setSkProd(ws.getDrProd(ws.getWkIdx()));
        ws.setSkWhse(ws.getDrWhse(ws.getWkIdx()));
        StringBuilder rkSb_8 = new StringBuilder();
        String rkPart0_8 = "";
        try {
            rkPart0_8 = ws.getString("SK-PROD");
        } catch (Exception _e) {
        }
        rkSb_8.append(rkPart0_8 != null ? rkPart0_8.trim() : "");
        String rkPart1_8 = "";
        try {
            rkPart1_8 = ws.getString("SK-WHSE");
        } catch (Exception _e) {
        }
        rkSb_8.append('|');
        rkSb_8.append(rkPart1_8 != null ? rkPart1_8.trim() : "");
        fileSet.getStokf().readByKey(rkSb_8.toString());
        ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
        if (fileSet.getStokf().isInvalidKey()) {
            ws.setWkCost(BigDecimal.ZERO);
            return;
        }
        ws.setWkStkFound(1);
        ws.setWkCost(ws.getSkAvgCost());
        ws.setSkOnhand(ws.getSkOnhand().subtract(BigDecimal.valueOf(ws.getWkShipq())));
        ws.setSkAllocated(ws.getSkAllocated().subtract(BigDecimal.valueOf(ws.getWkShipq())));
        if ((ws.getSkAllocated().signum() < 0)) {
            ws.setSkAllocated(BigDecimal.ZERO);
        }
        ws.setSkLastOutDate(ws.getWkSysdate());
        ws.setSkYtdOut(ws.getSkYtdOut().add(BigDecimal.valueOf(ws.getWkShipq())));
        fileSet.getStokf().rewrite();
        ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
        if (fileSet.getStokf().isInvalidKey()) {
            ws.setWkMsgLine("Stock update failed");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
    }

    /** COBOL paragraph: WSD-010 */
    private void writeShipmentDetail() {
        fileSet.getShpdf().setRecord();
        ws.setXdNo(ws.getWkShipNo());
        ws.setXdLine(ws.getWkXline());
        ws.setXdOrder(ws.getWkOrderNo());
        ws.setXdOrderLine(ws.getDrLine(ws.getWkIdx()));
        ws.setXdProd(ws.getDrProd(ws.getWkIdx()));
        ws.setXdWhse(ws.getDrWhse(ws.getWkIdx()));
        ws.setXdQty(BigDecimal.valueOf(ws.getWkShipq()));
        ws.setXdUnitPrice(ws.getDrPrice(ws.getWkIdx()));
        ws.setXdAmount(
                (BigDecimal.valueOf(ws.getWkShipq()).multiply(ws.getDrPrice(ws.getWkIdx())))
                        .setScale(0, java.math.RoundingMode.DOWN));
        ws.setXdUnitCost(ws.getWkCost());
        fileSet.getShpdf().write();
        ws.trySetString("FSTS", fileSet.getShpdf().getFileStatus());
        if (fileSet.getShpdf().isInvalidKey()) {
            ws.setWkMsgLine("Shipment line write failed");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
    }

    /** COBOL paragraph: WM-010 */
    private void writeStockMovement() {
        if (ws.getDrStkmng(ws.getWkIdx()) == 0 || ws.getWkStkFound() == 0) {
            return;
        }
        ws.setKnumKey("STKMOV");
        numgen(ws.getKnum());
        if (!Utility.fieldEquals(ws.getKnumStatus(), "00")) {
            ws.setWkMsgLine("Movement number assignment failed");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        fileSet.getSmovf().setRecord();
        ws.setSmSeq(ws.getKnumNumber());
        ws.setSmDate(ws.getWkSysdate());
        ws.setSmProd(ws.getDrProd(ws.getWkIdx()));
        ws.setSmWhse(ws.getDrWhse(ws.getWkIdx()));
        ws.setSmKind(10);
        ws.setSmQty(
                (BigDecimal.ZERO.subtract(BigDecimal.valueOf(ws.getWkShipq())))
                        .setScale(0, java.math.RoundingMode.DOWN));
        ws.setSmUnitCost(ws.getWkCost());
        ws.setSmBalAfter(ws.getSkOnhand());
        ws.setSmRefType(2);
        ws.setSmRefNo(ws.getWkShipNo());
        ws.setSmUser(ws.getWkUserCode());
        fileSet.getSmovf().write();
        ws.trySetString("FSTS", fileSet.getSmovf().getFileStatus());
        if (fileSet.getSmovf().isInvalidKey()) {
            ws.setWkMsgLine("Movement write failed");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
    }

    /** COBOL paragraph: UOL-010 */
    private void updateOrderLine() {
        ws.setOdNo(ws.getWkOrderNo());
        ws.setOdLine(ws.getDrLine(ws.getWkIdx()));
        StringBuilder rkSb_9 = new StringBuilder();
        String rkPart0_9 = "";
        try {
            rkPart0_9 = ws.getString("OD-NO");
        } catch (Exception _e) {
        }
        rkSb_9.append(rkPart0_9 != null ? rkPart0_9.trim() : "");
        String rkPart1_9 = "";
        try {
            rkPart1_9 = ws.getString("OD-LINE");
        } catch (Exception _e) {
        }
        rkSb_9.append('|');
        rkSb_9.append(rkPart1_9 != null ? rkPart1_9.trim() : "");
        fileSet.getOrddf().readByKey(rkSb_9.toString());
        ws.trySetString("FSTS", fileSet.getOrddf().getFileStatus());
        if (fileSet.getOrddf().isInvalidKey()) {
            return;
        }
        ws.setOdShippedQty(ws.getOdShippedQty().add(BigDecimal.valueOf(ws.getWkShipq())));
        ws.setOdAllocQty(ws.getOdAllocQty().subtract(BigDecimal.valueOf(ws.getWkShipq())));
        if ((ws.getOdAllocQty().signum() < 0)) {
            ws.setOdAllocQty(BigDecimal.ZERO);
        }
        if ((ws.getOdShippedQty().compareTo(ws.getOdQty()) >= 0)) {
            ws.setOdStatus(3);
        } else {
            ws.setOdStatus(2);
        }
        ws.setDrShipped(ws.getWkIdx(), ws.getOdShippedQty().intValue());
        ws.setDrAlloc(ws.getWkIdx(), ws.getOdAllocQty().intValue());
        fileSet.getOrddf().rewrite();
        ws.trySetString("FSTS", fileSet.getOrddf().getFileStatus());
        if (fileSet.getOrddf().isInvalidKey()) {
            ws.setWkMsgLine("Order line update failed");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
    }

    /** COBOL paragraph: UOS-010 */
    private void updateOrderStatus() {
        runChain(this::startOrderCompletionCheck);
        ws.setOhNo(ws.getWkOrderNo());
        readByFallbackKey(fileSet.getOrdhf(), "OH-NO");
        if (fileSet.getOrdhf().isInvalidKey()) {
            return;
        }
        if (ws.getWkComplete() == 1) {
            ws.setOhStatus(3);
        } else {
            ws.setOhStatus(2);
        }
        ws.setOhUpdDate(ws.getWkSysdate());
        ws.setOhUpdUser(ws.getWkUserCode());
        fileSet.getOrdhf().rewrite();
        ws.trySetString("FSTS", fileSet.getOrdhf().getFileStatus());
        if (fileSet.getOrdhf().isInvalidKey()) {
            ws.setWkMsgLine("Order status update failed");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
        runChain(this::setStatusText);
    }

    /** COBOL paragraph: CC-010 */
    private void startOrderCompletionCheck() {
        ws.setWkComplete(1);
        ws.setOdNo(ws.getWkOrderNo());
        ws.setOdLine(0);
        fileSet.getOrddf().start("OD-NO", "NOT LESS THAN");
        ws.trySetString("FSTS", fileSet.getOrddf().getFileStatus());
        if (fileSet.getOrddf().isInvalidKey()) {
            return;
        }
        // fall-through to next paragraph
        checkOrderCompletion();
    }

    /** COBOL paragraph: CC-020 */
    private void checkOrderCompletion() {
        while (true) {
            fileSet.getOrddf().readNext();
            ws.trySetString("FSTS", fileSet.getOrddf().getFileStatus());
            if (fileSet.getOrddf().isAtEnd()) {
                return;
            }
            if (ws.getOdNo() != ws.getWkOrderNo()) {
                return;
            }
            if ((ws.getOdShippedQty().compareTo(ws.getOdQty()) < 0)) {
                ws.setWkComplete(0);
                return;
            }
        }
    }

    /** COBOL paragraph: CLP-010 */
    private void computePageCount() {
        if (ws.getWkDcnt() == 0) {
            ws.setWkPageCnt(1);
        } else {
            ws.setWkPageCnt((((ws.getWkDcnt() + ws.getWkPgsize()) - 1) / ws.getWkPgsize()));
        }
    }

    /** COBOL paragraph: BW-010 */
    private void buildScreenWindow() {
        for (ws.setWkIdx(1); ws.getWkIdx() <= ws.getWkPgsize(); ws.setWkIdx(ws.getWkIdx() + 1)) {
            ws.setWkIdx2(((ws.getWkPageTop() + ws.getWkIdx()) - 1));
            if (ws.getWkIdx2() <= ws.getWkDcnt() && ws.getWkIdx2() >= 1) {
                ws.setWwLine(ws.getWkIdx(), ws.getDrLine(ws.getWkIdx2()));
                ws.setWwProd(ws.getWkIdx(), ws.getDrProd(ws.getWkIdx2()));
                ws.setWwName(ws.getWkIdx(), ws.getDrName(ws.getWkIdx2()));
                ws.setWwOrd(ws.getWkIdx(), ws.getDrOrd(ws.getWkIdx2()));
                ws.setWwShipped(ws.getWkIdx(), ws.getDrShipped(ws.getWkIdx2()));
                ws.setWwAlloc(ws.getWkIdx(), ws.getDrAlloc(ws.getWkIdx2()));
                ws.setWwShip(ws.getWkIdx(), ws.getDrShip(ws.getWkIdx2()));
            } else {
                ws.setWwLine(ws.getWkIdx(), 0);
                ws.setWwProd(ws.getWkIdx(), 0);
                ws.setWwName(ws.getWkIdx(), " ");
                ws.setWwOrd(ws.getWkIdx(), 0);
                ws.setWwShipped(ws.getWkIdx(), 0);
                ws.setWwAlloc(ws.getWkIdx(), 0);
                ws.setWwShip(ws.getWkIdx(), 0);
            }
        }
        ws.setWkPageNo((((ws.getWkPageTop() + ws.getWkPgsize()) - 1) / ws.getWkPgsize()));
    }

    /** COBOL paragraph: PO-010 */
    private void displayOrderScreens() {
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEADER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-FOOTER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-ORDER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-STATUS"), ws);
    }

    /** COBOL paragraph: PD-010 */
    private void pageDown() {
        if ((ws.getWkPageTop() + ws.getWkPgsize()) > ws.getWkDcnt()) {
            ws.setWkMsgLine("Already at last page");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setWkPageTop(ws.getWkPageTop() + ws.getWkPgsize());
        runChain(this::buildScreenWindow);
    }

    /** COBOL paragraph: PU-010 */
    private void pageUp() {
        if (ws.getWkPageTop() <= 1) {
            ws.setWkMsgLine("Already at first page");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        if (ws.getWkPageTop() > ws.getWkPgsize()) {
            ws.setWkPageTop(ws.getWkPageTop() - (ws.getWkPgsize()));
        } else {
            ws.setWkPageTop(1);
        }
        runChain(this::buildScreenWindow);
    }

    /** COBOL paragraph: TERM-010 */
    private void closeAllFiles() {
        fileSet.getOrdhf().close();
        ws.trySetString("FSTS", fileSet.getOrdhf().getFileStatus());
        fileSet.getOrddf().close();
        ws.trySetString("FSTS", fileSet.getOrddf().getFileStatus());
        fileSet.getShphf().close();
        ws.trySetString("FSTS", fileSet.getShphf().getFileStatus());
        fileSet.getShpdf().close();
        ws.trySetString("FSTS", fileSet.getShpdf().getFileStatus());
        fileSet.getStokf().close();
        ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
        fileSet.getSmovf().close();
        ws.trySetString("FSTS", fileSet.getSmovf().getFileStatus());
        fileSet.getCustf().close();
        ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
        fileSet.getProdf().close();
        ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
    }

    /** COBOL paragraph: ABEND-010 */
    private void abortOnFileOpenError() {
        ws.setKaProgid("SH0010");
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
     * Open an indexed file for I/O; if not-found (35/30), create it via an OUTPUT-mode open/close
     * cycle then retry the IO open. Abends the program on any other file error.
     */
    private void openIndexedFileWithRetry(RawDatasetBase file, String fileName) {
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
            runChain(this::abortOnFileOpenError);
        }
    }

    /**
     * Read a record by key: prefer the field's current value, falling back to the dataset's
     * current-record key when the field is blank.
     */
    private void readByFallbackKey(RawDatasetBase file, String fieldKey) {
        String rkVal = "";
        if (rkVal == null || rkVal.trim().isEmpty()) {
            try {
                rkVal = ws.getString(fieldKey);
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
