package com.sakura.pu0020.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.pu0020.domain.Pu0020FieldAccess;
import com.sakura.pu0020.domain.WorkingStorage;
import com.sakura.pu0020.runtime.Pu0020Datasets;
import com.sakura.pu0020.screen.ScreenDefs;
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

/** Business logic service generated from COBOL program PU0020. */
@Service
@Scope("prototype")
public class Pu0020Service extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final Pu0020Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Pu0020FieldAccess ws;

    public Pu0020Service(
            Pu0020Datasets fileSet,
            DateutService dateutService,
            AbortxService abortxService,
            ScreenRendererInstance renderer) {
        this.fileSet = fileSet;
        this.ws = new Pu0020FieldAccess(new WorkingStorage(), fileSet);
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
        runChain(this::executeProgramFlow);
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
    private void executeProgramFlow() {
        runChain(this::initializeProgram);
        while ((ws.getEndFlg() != 1)) {
            runChain(this::processMainScreen);
        }
        runChain(this::closeAllFiles);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeProgram() {
        ws.setWkProgid("PU0020");
        ws.setWkTitle("Purchase Order Inquiry");
        ws.setWkFkeyLine("ENTER=Search  PF3=End");
        ws.setKdFunc("TODY");
        dateut(ws.getKdate());
        ws.setWkSysymd(ws.getKdDate1());
        ws.setWkSysdate(ws.getKdDate1());
        fileSet.getPohf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getPohf().getFileStatus());
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("POHF");
            runChain(this::abortOnFileOpenError);
        }
        fileSet.getPodf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getPodf().getFileStatus());
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("PODF");
            runChain(this::abortOnFileOpenError);
        }
        fileSet.getSuppf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getSuppf().getFileStatus());
        fileSet.getProdf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
    }

    /** COBOL paragraph: MAIN-010 */
    private void processMainScreen() {
        runChain(this::clearSearchCriteria);
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEADER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-FOOTER"), ws);
        ws.setWkMsgLine("Enter a PO number, or a supplier to browse");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-SEARCH"), ws);
        String scValWkSrchNo0 =
                Utility.acceptScreen(
                        "WK-SRCH-NO",
                        () -> renderer.acceptField(ScreenDefs.getInput("WK-SRCH-NO")));
        ws.setWkSrchNo(Utility.parseLongOr(scValWkSrchNo0.trim(), 0L));
        String scValWkSrchSupp1 =
                Utility.acceptScreen(
                        "WK-SRCH-SUPP",
                        () -> renderer.acceptField(ScreenDefs.getInput("WK-SRCH-SUPP")));
        ws.setWkSrchSupp(Utility.parseIntOr(scValWkSrchSupp1.trim(), 0));
        String scValWkSrchDate2 =
                Utility.acceptScreen(
                        "WK-SRCH-DATE",
                        () -> renderer.acceptField(ScreenDefs.getInput("WK-SRCH-DATE")));
        ws.setWkSrchDate(Utility.parseIntOr(scValWkSrchDate2.trim(), 0));
        broadcastEstsStatus();
        switch (String.valueOf(ws.getEsts())) {
            case "03" -> {
                ws.setString("END-FLG", "1");
            }
            case "00" -> {
                runChain(this::dispatchSearchByCriteria);
            }
            default -> {
                ws.setWkMsgLine("Invalid function key");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: CLRS-010 */
    private void clearSearchCriteria() {
        ws.setWkSrchNo(0);
        ws.setWkSrchSupp(0);
        ws.setWkSrchDate(0);
        ws.setWkMode(0);
    }

    /** COBOL paragraph: DSR-010 */
    private void dispatchSearchByCriteria() {
        if (ws.getWkSrchNo() != 0) {
            ws.setString("WK-MODE", "1");
            ws.setPhNo(ws.getWkSrchNo());
            readByKeyWithFallback(fileSet.getPohf(), "PH-NO");
            if (fileSet.getPohf().isInvalidKey()) {
                ws.setWkMsgLine("PO number not found");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            } else {
                runChain(this::viewPoDetailLoop);
            }
            return;
        }
        if (ws.getWkSrchSupp() != 0) {
            ws.setString("WK-MODE", "2");
            runChain(this::searchPoBySupplier);
            return;
        }
        ws.setWkMsgLine("Enter a PO number or a supplier code");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
    }

    /** COBOL paragraph: SBS-010 */
    private void searchPoBySupplier() {
        ws.setPhSupp(ws.getWkSrchSupp());
        ws.setPhDate(ws.getWkSrchDate());
        fileSet.getPohf().start("PH-SUPP", "NOT LESS THAN");
        ws.trySetString("FSTS", fileSet.getPohf().getFileStatus());
        if (fileSet.getPohf().isInvalidKey()) {
            ws.setWkMsgLine("No PO found for supplier");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        runChain(this::readNextMatchingPoHeader);
        if ((ws.getFoundFlg() == 1)) {
            runChain(this::viewPoDetailLoop);
        } else {
            ws.setWkMsgLine("No PO found for supplier");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
    }

    /** COBOL paragraph: RNS-010 */
    private void readNextMatchingPoHeader() {
        ws.setFoundFlg(0);
        ws.setEofFlg(0);
        while (!((ws.getFoundFlg() == 1) || (ws.getEofFlg() == 1))) {
            fileSet.getPohf().readNext();
            ws.trySetString("FSTS", fileSet.getPohf().getFileStatus());
            if (fileSet.getPohf().isAtEnd()) {
                ws.setString("EOF-FLG", "1");
            } else if (ws.getPhSupp() != ws.getWkSrchSupp()) {
                ws.setString("EOF-FLG", "1");
            } else if (ws.getWkSrchDate() == 0) {
                ws.setString("FOUND-FLG", "1");
            } else if (ws.getPhDate() >= ws.getWkSrchDate()) {
                ws.setString("FOUND-FLG", "1");
            }
        }
    }

    /** COBOL paragraph: VL-010 */
    private void viewPoDetailLoop() {
        ws.setWkViewDone(0);
        while (ws.getWkViewDone() != 1) {
            runChain(this::renderPoDetailScreen);
            String scValWkNav4 =
                    Utility.acceptScreen(
                            "WK-NAV", () -> renderer.acceptField(ScreenDefs.getInput("WK-NAV")));
            ws.setWkNav(scValWkNav4);
            broadcastEstsStatus();
            switch (String.valueOf(ws.getEsts())) {
                case "03" -> {
                    ws.setWkViewDone(1);
                }
                case "06" -> {
                    runChain(this::pageToNextPoHeader);
                }
                default -> {
                    ws.setWkMsgLine("PF6=Next  PF3=Back");
                    renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
                }
            }
        }
    }

    /** COBOL paragraph: PGN-010 */
    private void pageToNextPoHeader() {
        if ((ws.getWkMode() == 1)) {
            ws.setWkMsgLine("Single PO - PF3 to go back");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setWkSaveRec(ws.getPhRec());
        runChain(this::readNextMatchingPoHeader);
        if ((ws.getFoundFlg() != 1)) {
            ws.setPhRec(String.valueOf(ws.getWkSaveRec()));
            ws.setWkMsgLine("No more POs for this supplier");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
    }

    /** COBOL paragraph: SC-010 */
    private void renderPoDetailScreen() {
        runChain(this::lookupSupplierName);
        runChain(this::resolvePoStatusText);
        runChain(this::loadPoDetailLines);
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEADER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-FOOTER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-HDRVIEW"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-COLHDR"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-ROWS"), ws);
        if (ws.getWkMoreFlg() == 1) {
            ws.setWkMsgLine("More lines exist - only first 9 shown");
        } else {
            ws.setWkMsgLine(" ");
        }
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
    }

    /** COBOL paragraph: LSN-010 */
    private void lookupSupplierName() {
        ws.setWkSuppName(" ");
        ws.setSpCode(ws.getPhSupp());
        readByKeyWithFallback(fileSet.getSuppf(), "SP-CODE");
        if (fileSet.getSuppf().isInvalidKey()) {
            ws.setWkSuppName("??? unknown supplier");
        } else {
            ws.setWkSuppName(ws.getSpName());
        }
    }

    /** COBOL paragraph: SST-010 */
    private void resolvePoStatusText() {
        switch (ws.getPhStatus()) {
            case 0 -> {
                ws.setWkStatText("Entered");
            }
            case 1 -> {
                ws.setWkStatText("Part-recv");
            }
            case 2 -> {
                ws.setWkStatText("Received");
            }
            case 3 -> {
                ws.setWkStatText("Invoiced");
            }
            case 9 -> {
                ws.setWkStatText("Cancelled");
            }
            default -> {
                ws.setWkStatText("?");
            }
        }
    }

    /** COBOL paragraph: LL-010 */
    private void loadPoDetailLines() {
        for (ws.setWkIdx(1); ws.getWkIdx() <= 9; ws.setWkIdx(ws.getWkIdx() + 1)) {
            ws.setWrBuf(ws.getWkIdx(), " ");
        }
        ws.setWkRowCnt(0);
        ws.setWkMoreFlg(0);
        ws.setPdNo(ws.getPhNo());
        ws.setPdLine(0);
        fileSet.getPodf().start("PD-NO", "NOT LESS THAN");
        ws.trySetString("FSTS", fileSet.getPodf().getFileStatus());
        if (fileSet.getPodf().isInvalidKey()) {
            return;
        }
        ws.setEofFlg(0);
        while ((ws.getEofFlg() != 1)) {
            fileSet.getPodf().readNext();
            ws.trySetString("FSTS", fileSet.getPodf().getFileStatus());
            if (fileSet.getPodf().isAtEnd()) {
                ws.setString("EOF-FLG", "1");
            } else if (ws.getPdNo() != ws.getPhNo()) {
                ws.setString("EOF-FLG", "1");
            } else if (ws.getWkRowCnt() >= 9) {
                ws.setWkMoreFlg(1);
                ws.setString("EOF-FLG", "1");
            } else {
                runChain(this::formatPoLineRow);
            }
        }
    }

    /** COBOL paragraph: FR-010 */
    private void formatPoLineRow() {
        ws.setRbName(" ");
        ws.setPrCode(ws.getPdProd());
        readByKeyWithFallback(fileSet.getProdf(), "PR-CODE");
        if (fileSet.getProdf().isInvalidKey()) {
            ws.setRbName("??? unknown");
        } else {
            ws.setRbName(ws.getPrName());
        }
        ws.setRbLine(ws.getPdLine());
        ws.setRbProd(ws.getPdProd());
        ws.setRbQty(ws.getPdQty().intValue());
        ws.setRbRecv(ws.getPdRecvQty().intValue());
        ws.setRbAmt(ws.getPdAmount().longValue());
        ws.setWkRowCnt(ws.getWkRowCnt() + 1);
        ws.setWrBuf(ws.getWkRowCnt(), ws.getWkRowBuf());
    }

    /** COBOL paragraph: TERM-010 */
    private void closeAllFiles() {
        fileSet.getPohf().close();
        ws.trySetString("FSTS", fileSet.getPohf().getFileStatus());
        fileSet.getPodf().close();
        ws.trySetString("FSTS", fileSet.getPodf().getFileStatus());
        fileSet.getSuppf().close();
        ws.trySetString("FSTS", fileSet.getSuppf().getFileStatus());
        fileSet.getProdf().close();
        ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
    }

    /** COBOL paragraph: ABEND-010 */
    private void abortOnFileOpenError() {
        ws.setKaProgid("PU0020");
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

    /** Read a record by key, falling back from the screen field to the current record's key. */
    private void readByKeyWithFallback(RawDatasetBase file, String keyField) {
        String rkVal = "";
        if (rkVal == null || rkVal.trim().isEmpty()) {
            try {
                rkVal = ws.getString(keyField);
            } catch (Exception e) {
            }
        }
        if (rkVal == null || rkVal.trim().isEmpty()) {
            try {
                rkVal = file.extractKeyFromCurrentRecord();
            } catch (Exception e) {
            }
        }
        file.readByKey(rkVal != null ? rkVal.trim() : "");
        ws.trySetString("FSTS", file.getFileStatus());
    }
}
