package com.sakura.oe0020.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.oe0020.domain.Oe0020FieldAccess;
import com.sakura.oe0020.domain.WorkingStorage;
import com.sakura.oe0020.runtime.Oe0020Datasets;
import com.sakura.oe0020.screen.ScreenDefs;
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
import com.sakura.runtime.record.RawDatasetBase;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/** Business logic service generated from COBOL program OE0020. */
@Service
@Scope("prototype")
public class Oe0020Service extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final Oe0020Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Oe0020FieldAccess ws;

    public Oe0020Service(
            Oe0020Datasets fileSet,
            DateutService dateutService,
            AbortxService abortxService,
            ScreenRendererInstance renderer) {
        this.fileSet = fileSet;
        this.ws = new Oe0020FieldAccess(new WorkingStorage(), fileSet);
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
        runChain(this::runMainProgramLoop);
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
    private void runMainProgramLoop() {
        runChain(this::initializeProgram);
        while ((ws.getEndFlg() != 1)) {
            runChain(this::processMainScreenInput);
        }
        runChain(this::closeAllFiles);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeProgram() {
        ws.setWkProgid("OE0020");
        ws.setWkTitle("Sales Order Inquiry");
        ws.setWkFkeyLine("ENTER=NextPage PF12=PrevPage PF5/6=Prev/Next PF3=End");
        ws.setKdFunc("TODY");
        dateut(ws.getKdate());
        ws.setWkSysymd(ws.getKdDate1());
        ws.setWkSysdate(ws.getKdDate1());
        runChain(this::openAllFiles);
    }

    /** COBOL paragraph: OPENF-010 */
    private void openAllFiles() {
        openFileOrAbend(fileSet.getOrdhf(), "ORDHF");
        openFileOrAbend(fileSet.getOrddf(), "ORDDF");
        openFileOrAbend(fileSet.getCustf(), "CUSTF");
        openFileOrAbend(fileSet.getProdf(), "PRODF");
    }

    /** COBOL paragraph: MAINR-010 */
    private void processMainScreenInput() {
        runChain(this::clearSelectionFields);
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEADER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-FOOTER"), ws);
        ws.setWkMsgLine("Enter order number or customer, then ENTER");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-KEY"), ws);
        String scValWkSelNo0 =
                Utility.acceptScreen(
                        "WK-SEL-NO", () -> renderer.acceptField(ScreenDefs.getInput("WK-SEL-NO")));
        ws.setWkSelNo(Utility.parseLongOr(scValWkSelNo0.trim(), 0L));
        String scValWkSelCust1 =
                Utility.acceptScreen(
                        "WK-SEL-CUST",
                        () -> renderer.acceptField(ScreenDefs.getInput("WK-SEL-CUST")));
        ws.setWkSelCust(Utility.parseIntOr(scValWkSelCust1.trim(), 0));
        String scValWkSelDate2 =
                Utility.acceptScreen(
                        "WK-SEL-DATE",
                        () -> renderer.acceptField(ScreenDefs.getInput("WK-SEL-DATE")));
        ws.setWkSelDate(Utility.parseIntOr(scValWkSelDate2.trim(), 0));
        broadcastEstsStatus();
        switch (String.valueOf(ws.getEsts())) {
            case "03" -> {
                ws.setString("END-FLG", "1");
            }
            case "00" -> {
                runChain(this::dispatchSelectionKey);
            }
            default -> {
                ws.setWkMsgLine("Invalid function key");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: CLRS-010 */
    private void clearSelectionFields() {
        ws.setWkSelNo(0);
        ws.setWkSelCust(0);
        ws.setWkSelDate(0);
        ws.setWkMode(0);
        ws.setWkCurNo(0);
        ws.setWkHcnt(0);
        ws.setWkHpos(0);
        ws.setWkCustName(" ");
    }

    /** COBOL paragraph: PKEY-010 */
    private void dispatchSelectionKey() {
        if (ws.getWkSelNo() != 0) {
            runChain(this::findOrderByNumber);
        } else {
            if (ws.getWkSelCust() != 0) {
                runChain(this::findOrdersByCustomer);
            } else {
                ws.setWkMsgLine("Enter an order number or a customer code");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
                return;
            }
        }
        if ((ws.getFoundFlg() == 1)) {
            runChain(this::initOrderHistory);
            runChain(this::displayOrderDetails);
            runChain(this::runBrowseLoop);
        }
    }

    /** COBOL paragraph: FBN-010 */
    private void findOrderByNumber() {
        ws.setFoundFlg(0);
        ws.setString("WK-MODE", "1");
        ws.setOhNo(ws.getWkSelNo());
        readByKeyOrCurrent(fileSet.getOrdhf(), "OH-NO");
        if (fileSet.getOrdhf().isInvalidKey()) {
            ws.setWkMsgLine("Order number not found");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        if (ws.getOhDelFlag() == 1) {
            ws.setWkMsgLine("Order is cancelled / deleted");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setString("FOUND-FLG", "1");
    }

    /** COBOL paragraph: FBC-010 */
    private void findOrdersByCustomer() {
        ws.setFoundFlg(0);
        ws.setString("WK-MODE", "2");
        ws.setCuCode(ws.getWkSelCust());
        readByKeyOrCurrent(fileSet.getCustf(), "CU-CODE");
        if (fileSet.getCustf().isInvalidKey()) {
            ws.setWkMsgLine("Customer not found");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setWkCustName(ws.getCuName());
        ws.setOhCust(ws.getWkSelCust());
        ws.setOhDate(ws.getWkSelDate());
        fileSet.getOrdhf().start("OH-CUST", "NOT LESS THAN");
        ws.trySetString("FSTS", fileSet.getOrdhf().getFileStatus());
        if (fileSet.getOrdhf().isInvalidKey()) {
            ws.setWkMsgLine("No orders for this customer");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        runChain(this::readNextActiveOrder);
        if ((ws.getEofFlg() == 1) || ws.getOhCust() != ws.getWkSelCust()) {
            ws.setWkMsgLine("No orders for this customer");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setString("FOUND-FLG", "1");
    }

    /** COBOL paragraph: RNL-010 */
    private void readNextActiveOrder() {
        ws.setEofFlg(0);
        ws.setFoundFlg(0);
        while (!((ws.getFoundFlg() == 1) || (ws.getEofFlg() == 1))) {
            fileSet.getOrdhf().readNext();
            ws.trySetString("FSTS", fileSet.getOrdhf().getFileStatus());
            if (fileSet.getOrdhf().isAtEnd()) {
                ws.setString("EOF-FLG", "1");
            }
            if (!fileSet.getOrdhf().isAtEnd()) {
                if (ws.getOhDelFlag() == 0) {
                    ws.setString("FOUND-FLG", "1");
                }
            }
        }
    }

    /** COBOL paragraph: SHOW-010 */
    private void displayOrderDetails() {
        ws.setWkCurNo(ws.getOhNo());
        runChain(this::lookupCustomerName);
        runChain(this::mapOrderStatusText);
        runChain(this::startOrderDetailScan);
        ws.setWkPageTop(1);
        runChain(this::computePageCount);
        runChain(this::buildDisplayWindow);
        runChain(this::displayOrderScreen);
    }

    /** COBOL paragraph: LKC-010 */
    private void lookupCustomerName() {
        ws.setWkCustName(" ");
        ws.setCuCode(ws.getOhCust());
        readByKeyOrCurrent(fileSet.getCustf(), "CU-CODE");
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

    /** COBOL paragraph: LDD-010 */
    private void startOrderDetailScan() {
        ws.setWkDcnt(0);
        ws.setOdNo(ws.getWkCurNo());
        ws.setOdLine(0);
        fileSet.getOrddf().start("OD-NO", "NOT LESS THAN");
        ws.trySetString("FSTS", fileSet.getOrddf().getFileStatus());
        if (fileSet.getOrddf().isInvalidKey()) {
            return;
        }
        // fall-through to next paragraph
        loadOrderDetailLines();
    }

    /** COBOL paragraph: LDD-020 */
    private void loadOrderDetailLines() {
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
            runChain(this::lookupProductName);
            ws.setDrName(ws.getWkDcnt(), ws.getWkProdName());
            ws.setDrQty(ws.getWkDcnt(), ws.getOdQty().intValue());
            ws.setDrPrice(ws.getWkDcnt(), ws.getOdUnitPrice());
            ws.setDrAmt(ws.getWkDcnt(), ws.getOdAmount().longValue());
            ws.setDrShip(ws.getWkDcnt(), ws.getOdShippedQty().intValue());
            ws.setDrAlloc(ws.getWkDcnt(), ws.getOdAllocQty().intValue());
        }
    }

    /** COBOL paragraph: LKP-010 */
    private void lookupProductName() {
        ws.setWkProdName(" ");
        ws.setPrCode(ws.getOdProd());
        readByKeyOrCurrent(fileSet.getProdf(), "PR-CODE");
        if (fileSet.getProdf().isInvalidKey()) {
            ws.setWkProdName("??");
        }
        if (!fileSet.getProdf().isInvalidKey()) {
            ws.setWkProdName(ws.getPrName());
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
    private void buildDisplayWindow() {
        for (ws.setWkIdx(1); ws.getWkIdx() <= ws.getWkPgsize(); ws.setWkIdx(ws.getWkIdx() + 1)) {
            ws.setWkIdx2(((ws.getWkPageTop() + ws.getWkIdx()) - 1));
            if (ws.getWkIdx2() <= ws.getWkDcnt() && ws.getWkIdx2() >= 1) {
                ws.setWwLine(ws.getWkIdx(), ws.getDrLine(ws.getWkIdx2()));
                ws.setWwProd(ws.getWkIdx(), ws.getDrProd(ws.getWkIdx2()));
                ws.setWwName(ws.getWkIdx(), ws.getDrName(ws.getWkIdx2()));
                ws.setWwQty(ws.getWkIdx(), ws.getDrQty(ws.getWkIdx2()));
                ws.setWwPrice(ws.getWkIdx(), ws.getDrPrice(ws.getWkIdx2()));
                ws.setWwAmt(ws.getWkIdx(), ws.getDrAmt(ws.getWkIdx2()));
                ws.setWwShip(ws.getWkIdx(), ws.getDrShip(ws.getWkIdx2()));
                ws.setWwAlloc(ws.getWkIdx(), ws.getDrAlloc(ws.getWkIdx2()));
            } else {
                ws.setWwLine(ws.getWkIdx(), 0);
                ws.setWwProd(ws.getWkIdx(), 0);
                ws.setWwName(ws.getWkIdx(), " ");
                ws.setWwQty(ws.getWkIdx(), 0);
                ws.setWwPrice(ws.getWkIdx(), BigDecimal.ZERO);
                ws.setWwAmt(ws.getWkIdx(), 0);
                ws.setWwShip(ws.getWkIdx(), 0);
                ws.setWwAlloc(ws.getWkIdx(), 0);
            }
        }
        ws.setWkPageNo((((ws.getWkPageTop() + ws.getWkPgsize()) - 1) / ws.getWkPgsize()));
    }

    /** COBOL paragraph: PO-010 */
    private void displayOrderScreen() {
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEADER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-FOOTER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-ORDER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-STATUS"), ws);
        ws.setWkMsgLine(" ");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
    }

    /** COBOL paragraph: BL-010 */
    private void runBrowseLoop() {
        ws.setWkBrowseEnd(0);
        while ((ws.getWkBrowseEnd() != 1)) {
            renderer.displayScreen(ScreenDefs.getScreen("DS-BROWSE"), ws);
            String scValWkDummy7 =
                    Utility.acceptScreen(
                            "WK-DUMMY",
                            () -> renderer.acceptField(ScreenDefs.getInput("WK-DUMMY")));
            ws.setWkDummy(scValWkDummy7);
            broadcastEstsStatus();
            switch (String.valueOf(ws.getEsts())) {
                case "03" -> {
                    ws.setString("WK-BROWSE-END", "1");
                }
                case "06" -> {
                    runChain(this::browseNextOrder);
                }
                case "05" -> {
                    runChain(this::browsePreviousOrder);
                }
                case "00" -> {
                    runChain(this::pageDown);
                }
                case "12" -> {
                    runChain(this::pageUp);
                }
                default -> {
                    ws.setWkMsgLine("PF5/6 order  ENTER/PF12 page  PF3 back");
                    renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
                }
            }
        }
    }

    /** COBOL paragraph: PD-010 */
    private void pageDown() {
        if ((ws.getWkPageTop() + ws.getWkPgsize()) > ws.getWkDcnt()) {
            ws.setWkMsgLine("Already at last page - PF6 for next order");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setWkPageTop(ws.getWkPageTop() + ws.getWkPgsize());
        runChain(this::buildDisplayWindow);
        runChain(this::displayOrderScreen);
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
        runChain(this::buildDisplayWindow);
        runChain(this::displayOrderScreen);
    }

    /** COBOL paragraph: INHI-010 */
    private void initOrderHistory() {
        ws.setWkHcnt(1);
        ws.setWkHpos(1);
        ws.setWhNo(1, ws.getOhNo());
    }

    /** COBOL paragraph: PH-010 */
    private void pushOrderHistory() {
        if (ws.getWkHpos() < 100) {
            ws.setWkHpos(ws.getWkHpos() + 1);
            ws.setWkHcnt(ws.getWkHpos());
            ws.setWhNo(ws.getWkHpos(), ws.getOhNo());
        }
    }

    /** COBOL paragraph: NO-010 */
    private void browseNextOrder() {
        ws.setOhNo(ws.getWkCurNo());
        readByKeyOrCurrent(fileSet.getOrdhf(), "OH-NO");
        if (fileSet.getOrdhf().isInvalidKey()) {
            ws.setWkMsgLine("Cannot reposition on current order");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        runChain(this::readNextActiveOrder);
        if ((ws.getEofFlg() == 1)) {
            ws.setWkMsgLine("No further orders");
            ws.setOhNo(ws.getWkCurNo());
            readByKeyOrCurrent(fileSet.getOrdhf(), "OH-NO");
            if (fileSet.getOrdhf().isInvalidKey()) {
                /* CONTINUE */
            }
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        runChain(this::displayOrderDetails);
        runChain(this::pushOrderHistory);
    }

    /** COBOL paragraph: PRO-010 */
    private void browsePreviousOrder() {
        if (ws.getWkHpos() <= 1) {
            ws.setWkMsgLine("No previous order in this browse");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setWkHpos(ws.getWkHpos() - (1));
        ws.setOhNo(ws.getWhNo(ws.getWkHpos()));
        readByKeyOrCurrent(fileSet.getOrdhf(), "OH-NO");
        if (fileSet.getOrdhf().isInvalidKey()) {
            ws.setWkMsgLine("Previous order no longer available");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        runChain(this::displayOrderDetails);
    }

    /** COBOL paragraph: TERM-010 */
    private void closeAllFiles() {
        closeFileTracked(fileSet.getOrdhf());
        closeFileTracked(fileSet.getOrddf());
        closeFileTracked(fileSet.getCustf());
        closeFileTracked(fileSet.getProdf());
    }

    /** COBOL paragraph: ABEND-010 */
    private void abortOnFileOpenError() {
        ws.setKaProgid("OE0020");
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

    /** Open one FD file for input and route to the abend handler on a non-zero file status. */
    private void openFileOrAbend(RawDatasetBase file, String fileName) {
        file.open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", file.getFileStatus());
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile(fileName);
            runChain(this::abortOnFileOpenError);
        }
    }

    /** Close one FD file and record its final file status. */
    private void closeFileTracked(RawDatasetBase file) {
        file.close();
        ws.trySetString("FSTS", file.getFileStatus());
    }

    /**
     * Re-read a record by key, falling back to the current record's key when the field is
     * unavailable.
     */
    private void readByKeyOrCurrent(RawDatasetBase file, String keyFieldName) {
        String rkVal = "";
        if (rkVal == null || rkVal.trim().isEmpty()) {
            try {
                rkVal = ws.getString(keyFieldName);
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
