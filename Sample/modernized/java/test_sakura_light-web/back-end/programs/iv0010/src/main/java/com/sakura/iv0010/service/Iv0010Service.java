package com.sakura.iv0010.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.iv0010.domain.Iv0010FieldAccess;
import com.sakura.iv0010.domain.WorkingStorage;
import com.sakura.iv0010.runtime.Iv0010Datasets;
import com.sakura.iv0010.screen.ScreenDefs;
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

/** Business logic service generated from COBOL program IV0010. */
@Service
@Scope("prototype")
public class Iv0010Service extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final Iv0010Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Iv0010FieldAccess ws;

    public Iv0010Service(
            Iv0010Datasets fileSet,
            DateutService dateutService,
            AbortxService abortxService,
            ScreenRendererInstance renderer) {
        this.fileSet = fileSet;
        this.ws = new Iv0010FieldAccess(new WorkingStorage(), fileSet);
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
            runChain(this::processMainScreen);
        }
        runChain(this::closeProgramFiles);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeProgram() {
        ws.setWkProgid("IV0010");
        ws.setWkTitle("Stock Balance Inquiry");
        ws.setWkFkeyLine("ENTER=Search PF6=Next PF4=New PF3=End");
        ws.setKdFunc("TODY");
        dateut(ws.getKdate());
        ws.setWkSysymd(ws.getKdDate1());
        ws.setWkSysdate(ws.getKdDate1());
        runChain(this::openProgramFiles);
    }

    /** COBOL paragraph: OPENF-010 */
    private void openProgramFiles() {
        openFileWithRetry(fileSet.getStokf());
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("STOKF");
            runChain(this::abortOnFileOpenError);
        }
        openFileWithRetry(fileSet.getProdf());
        openFileWithRetry(fileSet.getWhsef());
    }

    /** COBOL paragraph: MAIN-010 */
    private void processMainScreen() {
        runChain(this::acceptSearchKeyScreen);
        switch (String.valueOf(ws.getEsts())) {
            case "03" -> {
                ws.setString("END-FLG", "1");
            }
            case "00" -> {
                runChain(this::startStockBrowse);
                if ((ws.getRecFlg() == 1)) {
                    while ((ws.getBrwFlg() != 1)) {
                        runChain(this::browseNextStockRecord);
                    }
                }
            }
            default -> {
                ws.setWkMsgLine("Invalid function key");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: GKEY-010 */
    private void acceptSearchKeyScreen() {
        runChain(this::clearWorkFields);
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEADER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-FOOTER"), ws);
        ws.setWkMsgLine("Enter search key then ENTER");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-KEY"), ws);
        String scValWkOrder0 =
                Utility.acceptScreen(
                        "WK-ORDER", () -> renderer.acceptField(ScreenDefs.getInput("WK-ORDER")));
        ws.setWkOrder(Utility.parseIntOr(scValWkOrder0.trim(), 0));
        String scValWkKeyProd1 =
                Utility.acceptScreen(
                        "WK-KEY-PROD",
                        () -> renderer.acceptField(ScreenDefs.getInput("WK-KEY-PROD")));
        ws.setWkKeyProd(Utility.parseIntOr(scValWkKeyProd1.trim(), 0));
        String scValWkKeyWhse2 =
                Utility.acceptScreen(
                        "WK-KEY-WHSE",
                        () -> renderer.acceptField(ScreenDefs.getInput("WK-KEY-WHSE")));
        ws.setWkKeyWhse(Utility.parseIntOr(scValWkKeyWhse2.trim(), 0));
        broadcastEstsStatus();
        if (ws.getWkOrder() != 1 && ws.getWkOrder() != 2) {
            ws.setWkOrder(1);
        }
    }

    /** COBOL paragraph: CLRW-010 */
    private void clearWorkFields() {
        ws.setBrwFlg(0);
        ws.setRecFlg(0);
        ws.setWkPrName(" ");
        ws.setWkWhName(" ");
        ws.setWkAvail(0);
        ws.setWkSeen(0);
        ws.setWkSafety(0);
        ws.setWkReorder(0);
        ws.setWkStat(" ");
    }

    /** COBOL paragraph: SBRW-010 */
    private void startStockBrowse() {
        ws.setRecFlg(0);
        ws.setSkProd(ws.getWkKeyProd());
        ws.setSkWhse(ws.getWkKeyWhse());
        if ((ws.getWkOrder() == 1)) {
            fileSet.getStokf().start("SK-PROD", "NOT LESS THAN");
            ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
            if (fileSet.getStokf().isInvalidKey()) {
                ws.setWkMsgLine("No stock records from that key");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
                return;
            }
        } else {
            fileSet.getStokf().start("SK-WHSE", "NOT LESS THAN");
            ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
            if (fileSet.getStokf().isInvalidKey()) {
                ws.setWkMsgLine("No stock records from that key");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
                return;
            }
        }
        runChain(this::readNextStockRecord);
        if ((ws.getRecFlg() != 1)) {
            ws.setWkMsgLine("No stock records found");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
    }

    /** COBOL paragraph: BLUP-010 */
    private void browseNextStockRecord() {
        runChain(this::displayStockDetailScreen);
        broadcastEstsStatus();
        switch (String.valueOf(ws.getEsts())) {
            case "03", "04" -> {
                ws.setString("BRW-FLG", "1");
            }
            case "00", "06" -> {
                runChain(this::readNextStockRecord);
                if ((ws.getRecFlg() != 1)) {
                    ws.setWkMsgLine("End of list - PF3 to re-enter key");
                    renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
                    ws.setString("BRW-FLG", "1");
                }
            }
            default -> {
                ws.setWkMsgLine("ENTER/PF6=next  PF3/PF4=re-key");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: RNXT-010 */
    private void readNextStockRecord() {
        ws.setRecFlg(0);
        fileSet.getStokf().readNext();
        ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
        if (fileSet.getStokf().isAtEnd()) {
            return;
        }
        if (Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setString("REC-FLG", "1");
            ws.setWkSeen(ws.getWkSeen() + 1);
            ws.setWkAvail(ws.getSkOnhand().subtract(ws.getSkAllocated()).intValue());
            runChain(this::lookupProductName);
            runChain(this::lookupWarehouseName);
            runChain(this::computeStockStatus);
        }
    }

    /** COBOL paragraph: CSTA-010 */
    private void computeStockStatus() {
        if (ws.getWkAvail() < ws.getWkSafety()) {
            ws.setWkStat("LOW STOCK");
        } else if (ws.getWkAvail() <= ws.getWkReorder()) {
            ws.setWkStat("REORDER");
        } else {
            ws.setWkStat("OK");
        }
    }

    /** COBOL paragraph: SDET-010 */
    private void displayStockDetailScreen() {
        ws.setWkMsgLine("Record shown - ENTER/PF6 for next");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-DETAIL"), ws);
    }

    /** COBOL paragraph: LKPR-010 */
    private void lookupProductName() {
        ws.setWkPrName(" ");
        ws.setWkSafety(0);
        ws.setWkReorder(0);
        ws.setPrCode(ws.getSkProd());
        String rkVal_3 = "";
        if (rkVal_3 == null || rkVal_3.trim().isEmpty()) {
            try {
                rkVal_3 = ws.getString("PR-CODE");
            } catch (Exception _e) {
            }
        }
        if (rkVal_3 == null || rkVal_3.trim().isEmpty()) {
            try {
                rkVal_3 = fileSet.getProdf().extractKeyFromCurrentRecord();
            } catch (Exception _e3) {
            }
        }
        fileSet.getProdf().readByKey(rkVal_3 != null ? rkVal_3.trim() : "");
        ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
        if (fileSet.getProdf().isInvalidKey()) {
            ws.setWkPrName("*** unknown product ***");
        }
        if (!fileSet.getProdf().isInvalidKey()) {
            ws.setWkPrName(ws.getPrName());
            ws.setWkSafety(ws.getPrSafetyStock().intValue());
            ws.setWkReorder(ws.getPrReorderPoint().intValue());
        }
    }

    /** COBOL paragraph: LKWH-010 */
    private void lookupWarehouseName() {
        ws.setWkWhName(" ");
        ws.setWhCode(ws.getSkWhse());
        String rkVal_4 = "";
        if (rkVal_4 == null || rkVal_4.trim().isEmpty()) {
            try {
                rkVal_4 = ws.getString("WH-CODE");
            } catch (Exception _e) {
            }
        }
        if (rkVal_4 == null || rkVal_4.trim().isEmpty()) {
            try {
                rkVal_4 = fileSet.getWhsef().extractKeyFromCurrentRecord();
            } catch (Exception _e3) {
            }
        }
        fileSet.getWhsef().readByKey(rkVal_4 != null ? rkVal_4.trim() : "");
        ws.trySetString("FSTS", fileSet.getWhsef().getFileStatus());
        if (fileSet.getWhsef().isInvalidKey()) {
            ws.setWkWhName("*** unknown warehouse ***");
        }
        if (!fileSet.getWhsef().isInvalidKey()) {
            ws.setWkWhName(ws.getWhName());
        }
    }

    /** COBOL paragraph: TERM-010 */
    private void closeProgramFiles() {
        fileSet.getStokf().close();
        ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
        fileSet.getProdf().close();
        ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
        fileSet.getWhsef().close();
        ws.trySetString("FSTS", fileSet.getWhsef().getFileStatus());
    }

    /** COBOL paragraph: ABEND-010 */
    private void abortOnFileOpenError() {
        ws.setKaProgid("IV0010");
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

    /** Open a dataset for input, reopening it (output then input) on a not-found/no-file status. */
    private void openFileWithRetry(RawDatasetBase file) {
        file.open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", file.getFileStatus());
        if (Utility.fieldEquals(ws.getFsts(), "35") || Utility.fieldEquals(ws.getFsts(), "30")) {
            file.open(FileOpenMode.OUTPUT);
            ws.trySetString("FSTS", file.getFileStatus());
            file.close();
            ws.trySetString("FSTS", file.getFileStatus());
            file.open(FileOpenMode.INPUT);
            ws.trySetString("FSTS", file.getFileStatus());
        }
    }
}
