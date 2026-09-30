package com.sakura.iv0030.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.iv0030.domain.Iv0030FieldAccess;
import com.sakura.iv0030.domain.WorkingStorage;
import com.sakura.iv0030.runtime.Iv0030Datasets;
import com.sakura.iv0030.screen.ScreenDefs;
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

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/** Business logic service generated from COBOL program IV0030. */
@Service
@Scope("prototype")
public class Iv0030Service extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final Iv0030Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL NUMGEN. */
    private NumgenService numgenService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Iv0030FieldAccess ws;

    public Iv0030Service(
            Iv0030Datasets fileSet,
            DateutService dateutService,
            NumgenService numgenService,
            AbortxService abortxService,
            ScreenRendererInstance renderer) {
        this.fileSet = fileSet;
        this.ws = new Iv0030FieldAccess(new WorkingStorage(), fileSet);
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
            runChain(this::processMainScreenInput);
        }
        runChain(this::closeAllFiles);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeProgram() {
        ws.setWkProgid("IV0030");
        ws.setWkTitle("Stocktaking");
        ws.setWkFkeyLine("ENTER=Save/Next PF3=Finish PF4=Skip");
        ws.setKdFunc("TODY");
        dateut(ws.getKdate());
        ws.setWkSysymd(ws.getKdDate1());
        ws.setWkSysdate(ws.getKdDate1());
        runChain(this::openProgramFiles);
    }

    /** COBOL paragraph: OPENF-010 */
    private void openProgramFiles() {
        openFileOrAbend(fileSet.getStokf(), FileOpenMode.IO, "STOKF");
        openFileOrAbend(fileSet.getSmovf(), FileOpenMode.IO, "SMOVF");
        openFileWithRetry(fileSet.getProdf(), FileOpenMode.INPUT);
        openFileWithRetry(fileSet.getWhsef(), FileOpenMode.INPUT);
    }

    /** COBOL paragraph: MAIN-010 */
    private void processMainScreenInput() {
        runChain(this::getWarehouseInput);
        switch (String.valueOf(ws.getEsts())) {
            case "03" -> {
                ws.setString("END-FLG", "1");
            }
            case "00" -> {
                runChain(this::processWarehouseSelection);
            }
            default -> {
                ws.setWkMsgLine("Invalid function key");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: GWH-010 */
    private void getWarehouseInput() {
        runChain(this::clearWarehouseWorkFields);
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEADER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-FOOTER"), ws);
        ws.setWkMsgLine("Enter warehouse to count");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-WH"), ws);
        String scValWkWhseIn0 =
                Utility.acceptScreen(
                        "WK-WHSE-IN",
                        () -> renderer.acceptField(ScreenDefs.getInput("WK-WHSE-IN")));
        ws.setWkWhseIn(Utility.parseIntOr(scValWkWhseIn0.trim(), 0));
        broadcastEstsStatus();
    }

    /** COBOL paragraph: CLRW-010 */
    private void clearWarehouseWorkFields() {
        ws.setWkWhseIn(0);
        ws.setWkTcnt(0);
        ws.setWkDcnt(0);
        ws.setWkPcnt(0);
        ws.setWkShowNo(0);
        ws.setWkWhName(" ");
        ws.setWkPrName(" ");
        ws.setWkConfirm(" ");
        ws.setCntFlg(0);
        ws.setRecFlg(0);
    }

    /** COBOL paragraph: PWH-010 */
    private void processWarehouseSelection() {
        ws.setWhCode(ws.getWkWhseIn());
        readByKeyWithFallback(fileSet.getWhsef(), "WH-CODE");
        if (fileSet.getWhsef().isInvalidKey()) {
            ws.setWkMsgLine("Warehouse not found");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        if (ws.getWhDelFlag() == 1) {
            ws.setWkMsgLine("Warehouse is deleted");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setWkWhName(ws.getWhName());
        runChain(this::scanWarehouseStockItems);
        if (ws.getWkTcnt() == 0) {
            ws.setWkMsgLine("No stock records in this warehouse");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        runChain(this::reviewStockCounts);
        runChain(this::confirmAndPostCounts);
    }

    /** COBOL paragraph: RVC-010 */
    private void reviewStockCounts() {
        ws.setWkRpage(0);
        ws.setRvwFlg(0);
        ws.setWkRstart(1);
        while ((ws.getRvwFlg() != 1)) {
            runChain(this::formatReviewPage);
            if (ws.getWkRrow() == 0) {
                ws.setString("RVW-FLG", "1");
            } else {
                ws.setWkRpage(ws.getWkRpage() + 1);
                ws.setWkMsgLine("ENTER/PF6=next page  PF3=go to post");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
                renderer.displayScreen(ScreenDefs.getScreen("DS-REVIEW"), ws);
                broadcastEstsStatus();
                switch (String.valueOf(ws.getEsts())) {
                    case "03" -> {
                        ws.setString("RVW-FLG", "1");
                    }
                    default -> {
                        ws.setWkRstart(ws.getWkRstart() + 12);
                        if (ws.getWkRstart() > ws.getWkTcnt()) {
                            ws.setString("RVW-FLG", "1");
                        }
                    }
                }
            }
        }
    }

    /** COBOL paragraph: FRP-010 */
    private void formatReviewPage() {
        for (ws.setWkIdx(1); ws.getWkIdx() <= 12; ws.setWkIdx(ws.getWkIdx() + 1)) {
            ws.setWkList(ws.getWkIdx(), " ");
        }
        ws.setWkRrow(0);
        for (ws.setWkRi(ws.getWkRstart());
                !(ws.getWkRi() > ws.getWkTcnt() || ws.getWkRrow() >= 12);
                ws.setWkRi(ws.getWkRi() + 1)) {
            ws.setWkRrow(ws.getWkRrow() + 1);
            runChain(this::formatReviewLine);
        }
    }

    /** COBOL paragraph: FRL-010 */
    private void formatReviewLine() {
        ws.setCx(ws.getWkRi());
        ws.setWkClProd(ws.getCtProd(ws.getCx()));
        ws.setWkClName(" ");
        ws.setPrCode(ws.getCtProd(ws.getCx()));
        readByKeyWithFallback(fileSet.getProdf(), "PR-CODE");
        if (fileSet.getProdf().isInvalidKey()) {
            ws.setWkClName("?");
        }
        if (!fileSet.getProdf().isInvalidKey()) {
            ws.setWkClName(ws.getPrName());
        }
        ws.setWkClBook(ws.getCtBook(ws.getCx()));
        ws.setWkClCnt(ws.getCtCounted(ws.getCx()));
        ws.setWkClDiff(ws.getCtDiff(ws.getCx()));
        ws.setWkList(ws.getWkRrow(), ws.getWkCl());
    }

    /** COBOL paragraph: CLUP-010 */
    private void scanWarehouseStockItems() {
        ws.setSkWhse(ws.getWkWhseIn());
        ws.setSkProd(0);
        fileSet.getStokf().start("SK-WHSE", "NOT LESS THAN");
        ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
        if (fileSet.getStokf().isInvalidKey()) {
            return;
        }
        ws.setCntFlg(0);
        while ((ws.getCntFlg() != 1)) {
            runChain(this::readNextStockRecord);
            if ((ws.getRecFlg() != 1)) {
                ws.setString("CNT-FLG", "1");
            } else {
                if (ws.getSkWhse() != ws.getWkWhseIn()) {
                    ws.setString("CNT-FLG", "1");
                } else {
                    runChain(this::acceptStockCountEntry);
                }
            }
        }
    }

    /** COBOL paragraph: RNS-010 */
    private void readNextStockRecord() {
        ws.setRecFlg(0);
        fileSet.getStokf().readNext();
        ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
        if (fileSet.getStokf().isAtEnd()) {
            return;
        }
        if (Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setString("REC-FLG", "1");
        }
    }

    /** COBOL paragraph: CONE-010 */
    private void acceptStockCountEntry() {
        if (ws.getWkTcnt() >= 500) {
            ws.setWkMsgLine("Maximum 500 items reached - stop");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            ws.setString("CNT-FLG", "1");
            return;
        }
        ws.setWkBook(ws.getSkOnhand().intValue());
        ws.setWkCounted(ws.getSkOnhand().intValue());
        runChain(this::lookupProductName);
        ws.setWkShowNo(ws.getWkShowNo() + 1);
        ws.setWkMsgLine("ENTER=save count PF4=skip PF3=finish");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-COUNT"), ws);
        String scValWkCounted3 =
                Utility.acceptScreen(
                        "WK-COUNTED",
                        () -> renderer.acceptField(ScreenDefs.getInput("WK-COUNTED")));
        ws.setWkCounted(Utility.parseIntOr(scValWkCounted3.trim(), 0));
        broadcastEstsStatus();
        switch (String.valueOf(ws.getEsts())) {
            case "03" -> {
                runChain(this::storeCountedItem);
                ws.setString("CNT-FLG", "1");
            }
            case "04" -> {
                ws.setWkCounted(ws.getSkOnhand().intValue());
                runChain(this::storeCountedItem);
            }
            case "00" -> {
                runChain(this::storeCountedItem);
            }
            default -> {
                ws.setWkCounted(ws.getSkOnhand().intValue());
                runChain(this::storeCountedItem);
            }
        }
    }

    /** COBOL paragraph: STOR-010 */
    private void storeCountedItem() {
        ws.setWkTcnt(ws.getWkTcnt() + 1);
        ws.setCx(ws.getWkTcnt());
        ws.setCtProd(ws.getCx(), ws.getSkProd());
        ws.setCtWhse(ws.getCx(), ws.getSkWhse());
        ws.setCtBook(ws.getCx(), ws.getWkBook());
        ws.setCtCounted(ws.getCx(), ws.getWkCounted());
        ws.setWkDiff((ws.getWkCounted() - ws.getWkBook()));
        ws.setCtDiff(ws.getCx(), ws.getWkDiff());
        if (ws.getWkDiff() != 0) {
            ws.setWkDcnt(ws.getWkDcnt() + 1);
        }
    }

    /** COBOL paragraph: LKPR-010 */
    private void lookupProductName() {
        ws.setWkPrName(" ");
        ws.setPrCode(ws.getSkProd());
        readByKeyWithFallback(fileSet.getProdf(), "PR-CODE");
        if (fileSet.getProdf().isInvalidKey()) {
            ws.setWkPrName("*** unknown product ***");
        }
        if (!fileSet.getProdf().isInvalidKey()) {
            ws.setWkPrName(ws.getPrName());
        }
    }

    /** COBOL paragraph: CPST-010 */
    private void confirmAndPostCounts() {
        ws.setWkConfirm(" ");
        ws.setWkMsgLine("Review counts then confirm posting");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-SUMMARY"), ws);
        String scValWkConfirm5 =
                Utility.acceptScreen(
                        "SC-CONF", () -> renderer.acceptField(ScreenDefs.getInput("SC-CONF")));
        ws.setWkConfirm(scValWkConfirm5);
        broadcastEstsStatus();
        if ((ws.getWkConfirm().equals("Y") || ws.getWkConfirm().equals("y"))) {
            runChain(this::postAllCountedItems);
        } else {
            ws.setWkMsgLine("Count discarded - not posted");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
    }

    /** COBOL paragraph: PC-010 */
    private void postAllCountedItems() {
        ws.setWkPcnt(0);
        for (ws.setCx(1); ws.getCx() <= ws.getWkTcnt(); ws.setCx(ws.getCx() + 1)) {
            if (ws.getCtDiff(ws.getCx()) != 0) {
                runChain(this::postStockAdjustment);
            }
        }
        ws.setWkCnt(ws.getWkPcnt());
        ws.setWkMsgLine("Stocktaking posted");
        renderer.displayScreen(ScreenDefs.getScreen("DS-SUMMARY"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
    }

    /** COBOL paragraph: PO-010 */
    private void postStockAdjustment() {
        ws.setSkProd(ws.getCtProd(ws.getCx()));
        ws.setSkWhse(ws.getCtWhse(ws.getCx()));
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
            return;
        }
        ws.setSkOnhand(BigDecimal.valueOf(ws.getCtCounted(ws.getCx())));
        if (ws.getCtDiff(ws.getCx()) > 0) {
            ws.setSkLastInDate(ws.getWkSysdate());
            ws.setSkYtdIn(ws.getSkYtdIn().add(BigDecimal.valueOf(ws.getCtDiff(ws.getCx()))));
        } else {
            ws.setSkLastOutDate(ws.getWkSysdate());
            ws.setSkYtdOut(ws.getSkYtdOut().subtract(BigDecimal.valueOf(ws.getCtDiff(ws.getCx()))));
        }
        fileSet.getStokf().rewrite();
        ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
        if (fileSet.getStokf().isInvalidKey()) {
            return;
        }
        runChain(this::writeStockMovementRecord);
        ws.setWkPcnt(ws.getWkPcnt() + 1);
    }

    /** COBOL paragraph: WM-010 */
    private void writeStockMovementRecord() {
        ws.setKnumKey("STKMOV");
        numgen(ws.getKnum());
        if (!Utility.fieldEquals(ws.getKnumStatus(), "00")) {
            return;
        }
        fileSet.getSmovf().setRecord();
        ws.setSmSeq(ws.getKnumNumber());
        ws.setSmDate(ws.getWkSysdate());
        ws.setSmProd(ws.getCtProd(ws.getCx()));
        ws.setSmWhse(ws.getCtWhse(ws.getCx()));
        ws.setSmKind(30);
        ws.setSmQty(BigDecimal.valueOf(ws.getCtDiff(ws.getCx())));
        ws.setSmUnitCost(ws.getSkAvgCost());
        ws.setSmBalAfter(ws.getSkOnhand());
        ws.setSmRefType(30);
        ws.setSmRefNo(ws.getKnumNumber());
        ws.setSmUser(ws.getWkUserCode());
        fileSet.getSmovf().write();
        ws.trySetString("FSTS", fileSet.getSmovf().getFileStatus());
        if (fileSet.getSmovf().isInvalidKey()) {
            /* CONTINUE */
        }
    }

    /** COBOL paragraph: TERM-010 */
    private void closeAllFiles() {
        fileSet.getStokf().close();
        ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
        fileSet.getSmovf().close();
        ws.trySetString("FSTS", fileSet.getSmovf().getFileStatus());
        fileSet.getProdf().close();
        ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
        fileSet.getWhsef().close();
        ws.trySetString("FSTS", fileSet.getWhsef().getFileStatus());
    }

    /** COBOL paragraph: ABEND-010 */
    private void abortWithFileOpenError() {
        ws.setKaProgid("IV0030");
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
     * Open a file, retrying as OUTPUT-create-then-reopen when the file does not yet exist (FSTS
     * 35/30).
     */
    private void openFileWithRetry(RawDatasetBase file, FileOpenMode mode) {
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
     * Open a file with retry, then abend the program if the final open status is not successful.
     */
    private void openFileOrAbend(RawDatasetBase file, FileOpenMode mode, String fileNameForAbend) {
        openFileWithRetry(file, mode);
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile(fileNameForAbend);
            runChain(this::abortWithFileOpenError);
        }
    }

    /**
     * Read a record by key, falling back to the current record's key when the screen-derived key is
     * blank.
     */
    private void readByKeyWithFallback(RawDatasetBase file, String keyField) {
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
