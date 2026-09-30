package com.sakura.oe0030.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.oe0030.domain.Oe0030FieldAccess;
import com.sakura.oe0030.domain.WorkingStorage;
import com.sakura.oe0030.runtime.Oe0030Datasets;
import com.sakura.oe0030.screen.ScreenDefs;
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
import com.sakura.runtime.record.RawDatasetBase;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/** Business logic service generated from COBOL program OE0030. */
@Service
@Scope("prototype")
public class Oe0030Service extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final Oe0030Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Oe0030FieldAccess ws;

    public Oe0030Service(
            Oe0030Datasets fileSet,
            DateutService dateutService,
            AbortxService abortxService,
            ScreenRendererInstance renderer) {
        this.fileSet = fileSet;
        this.ws = new Oe0030FieldAccess(new WorkingStorage(), fileSet);
        this.dateutService = dateutService;
        this.abortxService = abortxService;
        this.renderer = renderer;
    }

    @Override
    public void setRenderer(ScreenRendererInstance renderer) {
        super.setRenderer(renderer);
        if (dateutService instanceof ScreenRendererAware rra) {
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
            runChain(this::processOrderSelection);
        }
        runChain(this::closeAllFiles);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeProgram() {
        ws.setWkProgid("OE0030");
        ws.setWkTitle("Sales Order Allocation");
        ws.setWkFkeyLine("ENTER=Read  PF3=End");
        ws.setKdFunc("TODY");
        dateut(ws.getKdate());
        ws.setWkSysymd(ws.getKdDate1());
        ws.setWkSysdate(ws.getKdDate1());
        runChain(this::openAllFiles);
    }

    /** COBOL paragraph: OPENF-010 */
    private void openAllFiles() {
        openFileWithRetry(fileSet.getOrdhf(), "ORDHF");
        openFileWithRetry(fileSet.getOrddf(), "ORDDF");
        openFileWithRetry(fileSet.getStokf(), "STOKF");
        fileSet.getCustf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
        fileSet.getProdf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
    }

    /** COBOL paragraph: MAINR-010 */
    private void processOrderSelection() {
        runChain(this::clearWorkingStorage);
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEADER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-FOOTER"), ws);
        ws.setWkMsgLine("Enter the order number to allocate");
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
                runChain(this::processSelectedOrder);
            }
            default -> {
                ws.setWkMsgLine("Invalid function key");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: CLRW-010 */
    private void clearWorkingStorage() {
        ws.setWkSelNo(0);
        ws.setWkCurNo(0);
        ws.setWkDcnt(0);
        ws.setWkAllocDone(0);
        ws.setWkTotAlloc(0);
        ws.setWkTotShort(0);
        ws.setWkPageTop(1);
        ws.setWkCustName(" ");
        ws.setWkConfirm(" ");
    }

    /** COBOL paragraph: PORD-010 */
    private void processSelectedOrder() {
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
        if (ws.getOhStatus() != 0 && ws.getOhStatus() != 1) {
            ws.setWkMsgLine("Order cannot be allocated in its status");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setWkCurNo(ws.getOhNo());
        runChain(this::lookupCustomerName);
        runChain(this::mapOrderStatusText);
        runChain(this::startOrderDetailBrowse);
        if (ws.getWkDcnt() == 0) {
            ws.setWkMsgLine("Order has no detail lines");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        runChain(this::computePageCount);
        ws.setWkPageTop(1);
        runChain(this::buildDetailPageWindow);
        runChain(this::displayOrderScreen);
        runChain(this::confirmAllocation);
    }

    /** COBOL paragraph: LKC-010 */
    private void lookupCustomerName() {
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
    private void mapOrderStatusText() {
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

    /** COBOL paragraph: PVD-010 */
    private void startOrderDetailBrowse() {
        ws.setWkDcnt(0);
        ws.setWkTotAlloc(0);
        ws.setWkTotShort(0);
        ws.setOdNo(ws.getWkCurNo());
        ws.setOdLine(0);
        fileSet.getOrddf().start("OD-NO", "NOT LESS THAN");
        ws.trySetString("FSTS", fileSet.getOrddf().getFileStatus());
        if (fileSet.getOrddf().isInvalidKey()) {
            return;
        }
        // fall-through to next paragraph
        loadOrderDetailRows();
    }

    /** COBOL paragraph: PVD-020 */
    private void loadOrderDetailRows() {
        while (true) {
            fileSet.getOrddf().readNext();
            ws.trySetString("FSTS", fileSet.getOrddf().getFileStatus());
            if (fileSet.getOrddf().isAtEnd()) {
                return;
            }
            if (ws.getOdNo() != ws.getWkCurNo()) {
                return;
            }
            if (ws.getWkDcnt() >= 200) {
                return;
            }
            ws.setWkDcnt(ws.getWkDcnt() + 1);
            ws.setDrLine(ws.getWkDcnt(), ws.getOdLine());
            ws.setDrProd(ws.getWkDcnt(), ws.getOdProd());
            ws.setDrWhse(ws.getWkDcnt(), ws.getOdWhse());
            runChain(this::lookupProductInfo);
            ws.setDrName(ws.getWkDcnt(), ws.getWkProdName());
            ws.setWkWant(ws.getOdQty().subtract(ws.getOdShippedQty()).intValue());
            if (ws.getWkWant() < 0) {
                ws.setWkWant(0);
            }
            ws.setDrOrd(ws.getWkDcnt(), ws.getWkWant());
            ws.setDrAlloc(ws.getWkDcnt(), ws.getOdAllocQty().intValue());
            runChain(this::computeStockAvailability);
            ws.setDrAvail(ws.getWkDcnt(), ws.getWkAvail());
            ws.setWkNeed(
                    BigDecimal.valueOf(ws.getWkWant()).subtract(ws.getOdAllocQty()).intValue());
            if (ws.getWkNeed() < 0) {
                ws.setWkNeed(0);
            }
            ws.setDrShort(ws.getWkDcnt(), ws.getWkNeed());
        }
    }

    /** COBOL paragraph: LKP-010 */
    private void lookupProductInfo() {
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

    /** COBOL paragraph: RSA-010 */
    private void computeStockAvailability() {
        ws.setWkAvail(0);
        if (ws.getDrStkmng(ws.getWkDcnt()) == 0) {
            ws.setWkAvail(ws.getWkWant());
            return;
        }
        ws.setSkProd(ws.getOdProd());
        ws.setSkWhse(ws.getOdWhse());
        StringBuilder rkSb_4 = new StringBuilder();
        String rkPart0_4 = "";
        try {
            rkPart0_4 = ws.getString("SK-PROD");
        } catch (Exception _e) {
        }
        rkSb_4.append(rkPart0_4 != null ? rkPart0_4.trim() : "");
        String rkPart1_4 = "";
        try {
            rkPart1_4 = ws.getString("SK-WHSE");
        } catch (Exception _e) {
        }
        rkSb_4.append('|');
        rkSb_4.append(rkPart1_4 != null ? rkPart1_4.trim() : "");
        fileSet.getStokf().readByKey(rkSb_4.toString());
        ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
        if (fileSet.getStokf().isInvalidKey()) {
            ws.setWkAvail(0);
        }
        if (!fileSet.getStokf().isInvalidKey()) {
            ws.setWkAvail(ws.getSkOnhand().subtract(ws.getSkAllocated()).intValue());
        }
        if (ws.getWkAvail() < 0) {
            ws.setWkAvail(0);
        }
    }

    /** COBOL paragraph: ASKC-010 */
    private void confirmAllocation() {
        ws.setWkConfirm(" ");
        ws.setWkMsgLine("Confirm allocation (Y) or PF3 to cancel");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-CONFIRM"), ws);
        String scValWkConfirm5 =
                Utility.acceptScreen(
                        "WK-CONFIRM",
                        () -> renderer.acceptField(ScreenDefs.getInput("WK-CONFIRM")));
        ws.setWkConfirm(scValWkConfirm5);
        broadcastEstsStatus();
        if (Utility.fieldEquals(ws.getEsts(), "03")) {
            ws.setWkMsgLine("Allocation cancelled");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        if ((ws.getWkConfirm().equals("Y") || ws.getWkConfirm().equals("y"))) {
            runChain(this::startAllocationPass);
            runChain(this::reviewOrderDetailPages);
        } else {
            ws.setWkMsgLine("Allocation cancelled");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
    }

    /** COBOL paragraph: DOA-010 */
    private void startAllocationPass() {
        ws.setWkTotAlloc(0);
        ws.setWkTotShort(0);
        ws.setOdNo(ws.getWkCurNo());
        ws.setOdLine(0);
        ws.setWkIdx(0);
        fileSet.getOrddf().start("OD-NO", "NOT LESS THAN");
        ws.trySetString("FSTS", fileSet.getOrddf().getFileStatus());
        if (fileSet.getOrddf().isInvalidKey()) {
            throw new ParagraphJumpSignal(this::finalizeAllocation);
        }
        // fall-through to next paragraph
        processAllocationDetailLoop();
    }

    /** COBOL paragraph: DOA-020 */
    private void processAllocationDetailLoop() {
        while (true) {
            fileSet.getOrddf().readNext();
            ws.trySetString("FSTS", fileSet.getOrddf().getFileStatus());
            if (fileSet.getOrddf().isAtEnd()) {
                throw new ParagraphJumpSignal(this::finalizeAllocation);
            }
            if (ws.getOdNo() != ws.getWkCurNo()) {
                throw new ParagraphJumpSignal(this::finalizeAllocation);
            }
            ws.setWkIdx(ws.getWkIdx() + 1);
            if (ws.getWkIdx() > ws.getWkDcnt()) {
                throw new ParagraphJumpSignal(this::finalizeAllocation);
            }
            runChain(this::allocateOrderLine);
        }
    }

    /** COBOL paragraph: DOA-900 */
    private void finalizeAllocation() {
        ws.setOhStatus(1);
        ws.setOhUpdDate(ws.getWkSysdate());
        ws.setOhUpdUser(ws.getWkUserCode());
        fileSet.getOrdhf().rewrite();
        ws.trySetString("FSTS", fileSet.getOrdhf().getFileStatus());
        if (fileSet.getOrdhf().isInvalidKey()) {
            ws.setWkMsgLine("Order header update failed");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
        runChain(this::mapOrderStatusText);
        ws.setWkAllocDone(1);
        ws.setWkPageTop(1);
        runChain(this::buildDetailPageWindow);
        runChain(this::displayOrderScreen);
        ws.setWkMsgLine("Allocation complete");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
    }

    /** COBOL paragraph: AOL-010 */
    private void allocateOrderLine() {
        ws.setWkWant(ws.getOdQty().subtract(ws.getOdShippedQty()).intValue());
        if (ws.getWkWant() < 0) {
            ws.setWkWant(0);
        }
        ws.setWkNeed(BigDecimal.valueOf(ws.getWkWant()).subtract(ws.getOdAllocQty()).intValue());
        if (ws.getWkNeed() <= 0) {
            ws.setWkGive(0);
            runChain(this::accumulateAllocationResult);
            return;
        }
        if (ws.getDrStkmng(ws.getWkIdx()) == 0) {
            ws.setWkGive(ws.getWkNeed());
            ws.setOdAllocQty(ws.getOdAllocQty().add(BigDecimal.valueOf(ws.getWkGive())));
            runChain(this::rewriteOrderDetail);
            runChain(this::accumulateAllocationResult);
            return;
        }
        ws.setSkProd(ws.getOdProd());
        ws.setSkWhse(ws.getOdWhse());
        StringBuilder rkSb_6 = new StringBuilder();
        String rkPart0_6 = "";
        try {
            rkPart0_6 = ws.getString("SK-PROD");
        } catch (Exception _e) {
        }
        rkSb_6.append(rkPart0_6 != null ? rkPart0_6.trim() : "");
        String rkPart1_6 = "";
        try {
            rkPart1_6 = ws.getString("SK-WHSE");
        } catch (Exception _e) {
        }
        rkSb_6.append('|');
        rkSb_6.append(rkPart1_6 != null ? rkPart1_6.trim() : "");
        fileSet.getStokf().readByKey(rkSb_6.toString());
        ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
        if (fileSet.getStokf().isInvalidKey()) {
            ws.setWkGive(0);
            runChain(this::accumulateAllocationResult);
            return;
        }
        ws.setWkAvail(ws.getSkOnhand().subtract(ws.getSkAllocated()).intValue());
        if (ws.getWkAvail() < 0) {
            ws.setWkAvail(0);
        }
        if (ws.getWkAvail() >= ws.getWkNeed()) {
            ws.setWkGive(ws.getWkNeed());
        } else {
            ws.setWkGive(ws.getWkAvail());
        }
        if (ws.getWkGive() > 0) {
            ws.setSkAllocated(ws.getSkAllocated().add(BigDecimal.valueOf(ws.getWkGive())));
            fileSet.getStokf().rewrite();
            ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
            if (fileSet.getStokf().isInvalidKey()) {
                ws.setWkMsgLine("Stock update failed");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
            ws.setOdAllocQty(ws.getOdAllocQty().add(BigDecimal.valueOf(ws.getWkGive())));
            runChain(this::rewriteOrderDetail);
        }
        runChain(this::accumulateAllocationResult);
    }

    /** COBOL paragraph: ROD-010 */
    private void rewriteOrderDetail() {
        if ((BigDecimal.valueOf((long) (ws.getWkWant())).compareTo(ws.getOdAllocQty()) <= 0)) {
            ws.setOdStatus(1);
        }
        fileSet.getOrddf().rewrite();
        ws.trySetString("FSTS", fileSet.getOrddf().getFileStatus());
        if (fileSet.getOrddf().isInvalidKey()) {
            ws.setWkMsgLine("Line update failed");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
    }

    /** COBOL paragraph: AOR-010 */
    private void accumulateAllocationResult() {
        ws.setWkNeed(BigDecimal.valueOf(ws.getWkWant()).subtract(ws.getOdAllocQty()).intValue());
        if (ws.getWkNeed() < 0) {
            ws.setWkNeed(0);
        }
        ws.setDrOrd(ws.getWkIdx(), ws.getWkWant());
        ws.setDrAlloc(ws.getWkIdx(), ws.getOdAllocQty().intValue());
        ws.setDrShort(ws.getWkIdx(), ws.getWkNeed());
        ws.setWkTotAlloc(
                (BigDecimal.valueOf(ws.getWkTotAlloc()).add(ws.getOdAllocQty())).longValue());
        ws.setWkTotShort(ws.getWkTotShort() + ws.getWkNeed());
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
    private void buildDetailPageWindow() {
        for (ws.setWkIdx(1); ws.getWkIdx() <= ws.getWkPgsize(); ws.setWkIdx(ws.getWkIdx() + 1)) {
            ws.setWkIdx2(((ws.getWkPageTop() + ws.getWkIdx()) - 1));
            if (ws.getWkIdx2() <= ws.getWkDcnt() && ws.getWkIdx2() >= 1) {
                ws.setWwLine(ws.getWkIdx(), ws.getDrLine(ws.getWkIdx2()));
                ws.setWwProd(ws.getWkIdx(), ws.getDrProd(ws.getWkIdx2()));
                ws.setWwName(ws.getWkIdx(), ws.getDrName(ws.getWkIdx2()));
                ws.setWwOrd(ws.getWkIdx(), ws.getDrOrd(ws.getWkIdx2()));
                ws.setWwAvail(ws.getWkIdx(), ws.getDrAvail(ws.getWkIdx2()));
                ws.setWwAlloc(ws.getWkIdx(), ws.getDrAlloc(ws.getWkIdx2()));
                ws.setWwShort(ws.getWkIdx(), ws.getDrShort(ws.getWkIdx2()));
            } else {
                ws.setWwLine(ws.getWkIdx(), 0);
                ws.setWwProd(ws.getWkIdx(), 0);
                ws.setWwName(ws.getWkIdx(), " ");
                ws.setWwOrd(ws.getWkIdx(), 0);
                ws.setWwAvail(ws.getWkIdx(), 0);
                ws.setWwAlloc(ws.getWkIdx(), 0);
                ws.setWwShort(ws.getWkIdx(), 0);
            }
        }
        ws.setWkPageNo((((ws.getWkPageTop() + ws.getWkPgsize()) - 1) / ws.getWkPgsize()));
    }

    /** COBOL paragraph: PO-010 */
    private void displayOrderScreen() {
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEADER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-FOOTER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-ORDER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-PAGE"), ws);
    }

    /** COBOL paragraph: RL-010 */
    private void reviewOrderDetailPages() {
        ws.setWkReviewEnd(0);
        ws.setWkFkeyLine("PF6=NextPage PF12=PrevPage PF3=Done");
        while ((ws.getWkReviewEnd() != 1)) {
            renderer.displayScreen(ScreenDefs.getScreen("DS-FOOTER"), ws);
            renderer.displayScreen(ScreenDefs.getScreen("DS-BROWSE"), ws);
            String scValWkDummy7 =
                    Utility.acceptScreen(
                            "WK-DUMMY",
                            () -> renderer.acceptField(ScreenDefs.getInput("WK-DUMMY")));
            ws.setWkDummy(scValWkDummy7);
            broadcastEstsStatus();
            switch (String.valueOf(ws.getEsts())) {
                case "03" -> {
                    ws.setString("WK-REVIEW-END", "1");
                }
                case "06" -> {
                    runChain(this::showNextPage);
                }
                case "12" -> {
                    runChain(this::showPreviousPage);
                }
                default -> {
                    /* CONTINUE */
                }
            }
        }
        ws.setWkFkeyLine("ENTER=Read  PF3=End");
    }

    /** COBOL paragraph: PD-010 */
    private void showNextPage() {
        if ((ws.getWkPageTop() + ws.getWkPgsize()) > ws.getWkDcnt()) {
            return;
        }
        ws.setWkPageTop(ws.getWkPageTop() + ws.getWkPgsize());
        runChain(this::buildDetailPageWindow);
        runChain(this::displayOrderScreen);
    }

    /** COBOL paragraph: PU-010 */
    private void showPreviousPage() {
        if (ws.getWkPageTop() <= 1) {
            return;
        }
        if (ws.getWkPageTop() > ws.getWkPgsize()) {
            ws.setWkPageTop(ws.getWkPageTop() - (ws.getWkPgsize()));
        } else {
            ws.setWkPageTop(1);
        }
        runChain(this::buildDetailPageWindow);
        runChain(this::displayOrderScreen);
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
    private void abortOnFileError() {
        ws.setKaProgid("OE0030");
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
     * Open a file for I-O, retrying via OUTPUT/close/re-open on a not-found status, then abend if
     * still failing.
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

    /**
     * Resolve a single-field key from working-storage (falling back to the current record) then
     * read by key.
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
