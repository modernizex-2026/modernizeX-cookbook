package com.sakura.sl0020.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
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
import com.sakura.sl0020.domain.Sl0020FieldAccess;
import com.sakura.sl0020.domain.WorkingStorage;
import com.sakura.sl0020.runtime.Sl0020Datasets;
import com.sakura.sl0020.screen.ScreenDefs;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/** Business logic service generated from COBOL program SL0020. */
@Service
@Scope("prototype")
public class Sl0020Service extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final Sl0020Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Sl0020FieldAccess ws;

    public Sl0020Service(
            Sl0020Datasets fileSet,
            DateutService dateutService,
            AbortxService abortxService,
            ScreenRendererInstance renderer) {
        this.fileSet = fileSet;
        this.ws = new Sl0020FieldAccess(new WorkingStorage(), fileSet);
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
            runChain(this::processMainScreenCycle);
        }
        runChain(this::closeProgramFiles);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeProgram() {
        ws.setWkProgid("SL0020");
        ws.setWkTitle("Sales Invoice Inquiry");
        ws.setWkFkeyLine("ENTER=Page PF12=PrevPage PF5/6=Prev/Next PF3=End");
        ws.setKdFunc("TODY");
        dateut(ws.getKdate());
        ws.setWkSysymd(ws.getKdDate1());
        ws.setWkSysdate(ws.getKdDate1());
        runChain(this::openProgramFiles);
    }

    /** COBOL paragraph: OPENF-010 */
    private void openProgramFiles() {
        openFileOrAbend(fileSet.getInvhf(), "INVHF");
        openFileOrAbend(fileSet.getInvdf(), "INVDF");
        openFileOrAbend(fileSet.getCustf(), "CUSTF");
        openFileOrAbend(fileSet.getProdf(), "PRODF");
    }

    /** COBOL paragraph: MAINR-010 */
    private void processMainScreenCycle() {
        runChain(this::clearSelectionFields);
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEADER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-FOOTER"), ws);
        ws.setWkMsgLine("Enter invoice number or customer, then ENTER");
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
                runChain(this::processPrimaryKeySelection);
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
    private void processPrimaryKeySelection() {
        if (ws.getWkSelNo() != 0) {
            runChain(this::findInvoiceByNumber);
        } else {
            if (ws.getWkSelCust() != 0) {
                runChain(this::findInvoiceByCustomer);
            } else {
                ws.setWkMsgLine("Enter an invoice number or a customer");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
                return;
            }
        }
        if ((ws.getFoundFlg() == 1)) {
            runChain(this::initInvoiceHistory);
            runChain(this::displayInvoiceDetail);
            runChain(this::runBrowseLoop);
        }
    }

    /** COBOL paragraph: FBN-010 */
    private void findInvoiceByNumber() {
        ws.setFoundFlg(0);
        ws.setString("WK-MODE", "1");
        ws.setIhNo(ws.getWkSelNo());
        readByKeyWithFallback(fileSet.getInvhf(), "IH-NO");
        if (fileSet.getInvhf().isInvalidKey()) {
            ws.setWkMsgLine("Invoice number not found");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        if (ws.getIhDelFlag() == 1) {
            ws.setWkMsgLine("Invoice is deleted");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setString("FOUND-FLG", "1");
    }

    /** COBOL paragraph: FBC-010 */
    private void findInvoiceByCustomer() {
        ws.setFoundFlg(0);
        ws.setString("WK-MODE", "2");
        ws.setCuCode(ws.getWkSelCust());
        readByKeyWithFallback(fileSet.getCustf(), "CU-CODE");
        if (fileSet.getCustf().isInvalidKey()) {
            ws.setWkMsgLine("Customer not found");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setWkCustName(ws.getCuName());
        ws.setIhCust(ws.getWkSelCust());
        ws.setIhDate(ws.getWkSelDate());
        fileSet.getInvhf().start("IH-CUST", "NOT LESS THAN");
        ws.trySetString("FSTS", fileSet.getInvhf().getFileStatus());
        if (fileSet.getInvhf().isInvalidKey()) {
            ws.setWkMsgLine("No invoices for this customer");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        runChain(this::readNextValidInvoice);
        if ((ws.getEofFlg() == 1) || ws.getIhCust() != ws.getWkSelCust()) {
            ws.setWkMsgLine("No invoices for this customer");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setString("FOUND-FLG", "1");
    }

    /** COBOL paragraph: RNL-010 */
    private void readNextValidInvoice() {
        ws.setEofFlg(0);
        ws.setFoundFlg(0);
        while (!((ws.getFoundFlg() == 1) || (ws.getEofFlg() == 1))) {
            fileSet.getInvhf().readNext();
            ws.trySetString("FSTS", fileSet.getInvhf().getFileStatus());
            if (fileSet.getInvhf().isAtEnd()) {
                ws.setString("EOF-FLG", "1");
            }
            if (!fileSet.getInvhf().isAtEnd()) {
                if (ws.getIhDelFlag() == 0) {
                    ws.setString("FOUND-FLG", "1");
                }
            }
        }
    }

    /** COBOL paragraph: SHOW-010 */
    private void displayInvoiceDetail() {
        ws.setWkCurNo(ws.getIhNo());
        runChain(this::lookupCustomerName);
        runChain(this::resolveKindAndStatusText);
        runChain(this::startInvoiceDetailBrowse);
        ws.setWkMarginTot(ws.getIhAmount().subtract(ws.getIhCostTotal()).longValue());
        ws.setWkPageTop(1);
        runChain(this::computePageCount);
        runChain(this::buildDetailWindow);
        runChain(this::renderInvoiceScreen);
    }

    /** COBOL paragraph: LKC-010 */
    private void lookupCustomerName() {
        ws.setWkCustName(" ");
        ws.setCuCode(ws.getIhCust());
        readByKeyWithFallback(fileSet.getCustf(), "CU-CODE");
        if (fileSet.getCustf().isInvalidKey()) {
            ws.setWkCustName("??? unknown customer");
        }
        if (!fileSet.getCustf().isInvalidKey()) {
            ws.setWkCustName(ws.getCuName());
        }
    }

    /** COBOL paragraph: STX-010 */
    private void resolveKindAndStatusText() {
        switch (ws.getIhKind()) {
            case 1 -> {
                ws.setWkKindText("Sale");
            }
            case 2 -> {
                ws.setWkKindText("Return");
            }
            default -> {
                ws.setWkKindText("?");
            }
        }
        switch (ws.getIhStatus()) {
            case 0 -> {
                ws.setWkStatText("Entered");
            }
            case 1 -> {
                ws.setWkStatText("Posted");
            }
            case 2 -> {
                ws.setWkStatText("Closed");
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
    private void startInvoiceDetailBrowse() {
        ws.setWkDcnt(0);
        ws.setIdNo(ws.getWkCurNo());
        ws.setIdLine(0);
        fileSet.getInvdf().start("ID-NO", "NOT LESS THAN");
        ws.trySetString("FSTS", fileSet.getInvdf().getFileStatus());
        if (fileSet.getInvdf().isInvalidKey()) {
            return;
        }
        // fall-through to next paragraph
        loadInvoiceDetailLines();
    }

    /** COBOL paragraph: LDD-020 */
    private void loadInvoiceDetailLines() {
        while (true) {
            fileSet.getInvdf().readNext();
            ws.trySetString("FSTS", fileSet.getInvdf().getFileStatus());
            if (fileSet.getInvdf().isAtEnd()) {
                return;
            }
            if (ws.getIdNo() != ws.getWkCurNo()) {
                return;
            }
            if (ws.getWkDcnt() >= 200) {
                return;
            }
            ws.setWkDcnt(ws.getWkDcnt() + 1);
            ws.setDrLine(ws.getWkDcnt(), ws.getIdLine());
            ws.setDrProd(ws.getWkDcnt(), ws.getIdProd());
            runChain(this::lookupProductName);
            ws.setDrName(ws.getWkDcnt(), ws.getWkProdName());
            ws.setDrQty(ws.getWkDcnt(), ws.getIdQty().intValue());
            ws.setDrAmt(ws.getWkDcnt(), ws.getIdAmount().longValue());
            ws.setDrCostamt(ws.getWkDcnt(), ws.getIdCostAmount().longValue());
            ws.setDrMargin(
                    ws.getWkDcnt(), ws.getIdAmount().subtract(ws.getIdCostAmount()).longValue());
        }
    }

    /** COBOL paragraph: LKP-010 */
    private void lookupProductName() {
        ws.setWkProdName(" ");
        ws.setPrCode(ws.getIdProd());
        readByKeyWithFallback(fileSet.getProdf(), "PR-CODE");
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
    private void buildDetailWindow() {
        for (ws.setWkIdx(1); ws.getWkIdx() <= ws.getWkPgsize(); ws.setWkIdx(ws.getWkIdx() + 1)) {
            ws.setWkIdx2(((ws.getWkPageTop() + ws.getWkIdx()) - 1));
            if (ws.getWkIdx2() <= ws.getWkDcnt() && ws.getWkIdx2() >= 1) {
                ws.setWwLine(ws.getWkIdx(), ws.getDrLine(ws.getWkIdx2()));
                ws.setWwProd(ws.getWkIdx(), ws.getDrProd(ws.getWkIdx2()));
                ws.setWwName(ws.getWkIdx(), ws.getDrName(ws.getWkIdx2()));
                ws.setWwQty(ws.getWkIdx(), ws.getDrQty(ws.getWkIdx2()));
                ws.setWwAmt(ws.getWkIdx(), ws.getDrAmt(ws.getWkIdx2()));
                ws.setWwCostamt(ws.getWkIdx(), ws.getDrCostamt(ws.getWkIdx2()));
                ws.setWwMargin(
                        ws.getWkIdx(), Utility.toCobolInt(ws.getDrMargin(ws.getWkIdx2()), 1));
            } else {
                ws.setWwLine(ws.getWkIdx(), 0);
                ws.setWwProd(ws.getWkIdx(), 0);
                ws.setWwName(ws.getWkIdx(), " ");
                ws.setWwQty(ws.getWkIdx(), 0);
                ws.setWwAmt(ws.getWkIdx(), 0);
                ws.setWwCostamt(ws.getWkIdx(), 0);
                ws.setWwMargin(ws.getWkIdx(), 0);
            }
        }
        ws.setWkPageNo((((ws.getWkPageTop() + ws.getWkPgsize()) - 1) / ws.getWkPgsize()));
    }

    /** COBOL paragraph: PIV-010 */
    private void renderInvoiceScreen() {
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEADER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-FOOTER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-INV"), ws);
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
                    runChain(this::nextInvoice);
                }
                case "05" -> {
                    runChain(this::previousInvoice);
                }
                case "00" -> {
                    runChain(this::pageDown);
                }
                case "12" -> {
                    runChain(this::pageUp);
                }
                default -> {
                    ws.setWkMsgLine("PF5/6 invoice  ENTER/PF12 page  PF3 back");
                    renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
                }
            }
        }
    }

    /** COBOL paragraph: PD-010 */
    private void pageDown() {
        if ((ws.getWkPageTop() + ws.getWkPgsize()) > ws.getWkDcnt()) {
            ws.setWkMsgLine("Already at last page - PF6 for next invoice");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setWkPageTop(ws.getWkPageTop() + ws.getWkPgsize());
        runChain(this::buildDetailWindow);
        runChain(this::renderInvoiceScreen);
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
        runChain(this::buildDetailWindow);
        runChain(this::renderInvoiceScreen);
    }

    /** COBOL paragraph: INHI-010 */
    private void initInvoiceHistory() {
        ws.setWkHcnt(1);
        ws.setWkHpos(1);
        ws.setWhNo(1, ws.getIhNo());
    }

    /** COBOL paragraph: PH-010 */
    private void pushInvoiceHistory() {
        if (ws.getWkHpos() < 100) {
            ws.setWkHpos(ws.getWkHpos() + 1);
            ws.setWkHcnt(ws.getWkHpos());
            ws.setWhNo(ws.getWkHpos(), ws.getIhNo());
        }
    }

    /** COBOL paragraph: NI-010 */
    private void nextInvoice() {
        ws.setIhNo(ws.getWkCurNo());
        readByKeyWithFallback(fileSet.getInvhf(), "IH-NO");
        if (fileSet.getInvhf().isInvalidKey()) {
            ws.setWkMsgLine("Cannot reposition on current invoice");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        runChain(this::readNextValidInvoice);
        if ((ws.getEofFlg() == 1)) {
            ws.setWkMsgLine("No further invoices");
            ws.setIhNo(ws.getWkCurNo());
            readByKeyWithFallback(fileSet.getInvhf(), "IH-NO");
            if (fileSet.getInvhf().isInvalidKey()) {
                /* CONTINUE */
            }
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        runChain(this::displayInvoiceDetail);
        runChain(this::pushInvoiceHistory);
    }

    /** COBOL paragraph: PRI-010 */
    private void previousInvoice() {
        if (ws.getWkHpos() <= 1) {
            ws.setWkMsgLine("No previous invoice in this browse");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setWkHpos(ws.getWkHpos() - (1));
        ws.setIhNo(ws.getWhNo(ws.getWkHpos()));
        readByKeyWithFallback(fileSet.getInvhf(), "IH-NO");
        if (fileSet.getInvhf().isInvalidKey()) {
            ws.setWkMsgLine("Previous invoice no longer available");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        runChain(this::displayInvoiceDetail);
    }

    /** COBOL paragraph: TERM-010 */
    private void closeProgramFiles() {
        closeFileWithStatus(fileSet.getInvhf());
        closeFileWithStatus(fileSet.getInvdf());
        closeFileWithStatus(fileSet.getCustf());
        closeFileWithStatus(fileSet.getProdf());
    }

    /** COBOL paragraph: ABEND-010 */
    private void abortOnFileOpenError() {
        ws.setKaProgid("SL0020");
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
     * Open a dataset for input and abort the program when the resulting file status is not "00".
     */
    private void openFileOrAbend(RawDatasetBase file, String fileName) {
        file.open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", file.getFileStatus());
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile(fileName);
            runChain(this::abortOnFileOpenError);
        }
    }

    /** Close a dataset and record its resulting file status. */
    private void closeFileWithStatus(RawDatasetBase file) {
        file.close();
        ws.trySetString("FSTS", file.getFileStatus());
    }

    /** Resolve a record key from working storage, falling back to the current record's key. */
    private String resolveKeyWithFallback(String keyField, RawDatasetBase file) {
        String rkVal = "";
        if (rkVal == null || rkVal.trim().isEmpty()) {
            try {
                rkVal = ws.getString(keyField);
            } catch (Exception e) {
                /* ignore */
            }
        }
        if (rkVal == null || rkVal.trim().isEmpty()) {
            try {
                rkVal = file.extractKeyFromCurrentRecord();
            } catch (Exception e) {
                /* ignore */
            }
        }
        return rkVal;
    }

    /** Read a dataset by key (resolved with fallback) and record the resulting file status. */
    private void readByKeyWithFallback(RawDatasetBase file, String keyField) {
        String rkVal = resolveKeyWithFallback(keyField, file);
        file.readByKey(rkVal != null ? rkVal.trim() : "");
        ws.trySetString("FSTS", file.getFileStatus());
    }
}
