package com.sakura.iv0040.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.iv0040.domain.Iv0040FieldAccess;
import com.sakura.iv0040.domain.WorkingStorage;
import com.sakura.iv0040.runtime.Iv0040Datasets;
import com.sakura.iv0040.screen.ScreenDefs;
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

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/** Business logic service generated from COBOL program IV0040. */
@Service
@Scope("prototype")
public class Iv0040Service extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final Iv0040Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Iv0040FieldAccess ws;

    public Iv0040Service(
            Iv0040Datasets fileSet,
            DateutService dateutService,
            AbortxService abortxService,
            ScreenRendererInstance renderer) {
        this.fileSet = fileSet;
        this.ws = new Iv0040FieldAccess(new WorkingStorage(), fileSet);
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
            runChain(this::processMainScreenKey);
        }
        runChain(this::closeAllFiles);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeProgram() {
        ws.setWkProgid("IV0040");
        ws.setWkTitle("Stock Movement Inquiry");
        ws.setWkFkeyLine("ENTER/PF6=Next page PF4=New PF3=End");
        ws.setKdFunc("TODY");
        dateut(ws.getKdate());
        ws.setWkSysymd(ws.getKdDate1());
        ws.setWkSysdate(ws.getKdDate1());
        runChain(this::openInputFiles);
    }

    /** COBOL paragraph: OPENF-010 */
    private void openInputFiles() {
        fileSet.getSmovf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getSmovf().getFileStatus());
        if (Utility.fieldEquals(ws.getFsts(), "35") || Utility.fieldEquals(ws.getFsts(), "30")) {
            fileSet.getSmovf().open(FileOpenMode.OUTPUT);
            ws.trySetString("FSTS", fileSet.getSmovf().getFileStatus());
            fileSet.getSmovf().close();
            ws.trySetString("FSTS", fileSet.getSmovf().getFileStatus());
            fileSet.getSmovf().open(FileOpenMode.INPUT);
            ws.trySetString("FSTS", fileSet.getSmovf().getFileStatus());
        }
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("SMOVF");
            runChain(this::abortOnFileOpenError);
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

    /** COBOL paragraph: MAIN-010 */
    private void processMainScreenKey() {
        runChain(this::acceptSearchKeyScreen);
        switch (String.valueOf(ws.getEsts())) {
            case "03" -> {
                ws.setString("END-FLG", "1");
            }
            case "00" -> {
                runChain(this::processProductKeyAndBrowse);
            }
            default -> {
                ws.setWkMsgLine("Invalid function key");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: GKEY-010 */
    private void acceptSearchKeyScreen() {
        ws.setWkKeyProd(0);
        ws.setWkKeyDate(0);
        ws.setWkPrName(" ");
        ws.setWkPageNo(0);
        ws.setBrwFlg(0);
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEADER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-FOOTER"), ws);
        ws.setWkMsgLine("Enter product code then ENTER");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-KEY"), ws);
        String scValWkKeyProd0 =
                Utility.acceptScreen(
                        "WK-KEY-PROD",
                        () -> renderer.acceptField(ScreenDefs.getInput("WK-KEY-PROD")));
        ws.setWkKeyProd(Utility.parseIntOr(scValWkKeyProd0.trim(), 0));
        String scValWkKeyDate1 =
                Utility.acceptScreen(
                        "WK-KEY-DATE",
                        () -> renderer.acceptField(ScreenDefs.getInput("WK-KEY-DATE")));
        ws.setWkKeyDate(Utility.parseIntOr(scValWkKeyDate1.trim(), 0));
        broadcastEstsStatus();
    }

    /** COBOL paragraph: PKEY-010 */
    private void processProductKeyAndBrowse() {
        if (ws.getWkKeyProd() == 0) {
            ws.setWkMsgLine("Product code must not be zero");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        runChain(this::lookupProductName);
        runChain(this::startMovementBrowse);
        if ((ws.getBrwFlg() == 1)) {
            return;
        }
        while ((ws.getBrwFlg() != 1)) {
            runChain(this::displayMovementListPage);
        }
    }

    /** COBOL paragraph: LKPR-010 */
    private void lookupProductName() {
        ws.setWkPrName(" ");
        ws.setPrCode(ws.getWkKeyProd());
        String rkVal_2 = "";
        if (rkVal_2 == null || rkVal_2.trim().isEmpty()) {
            try {
                rkVal_2 = ws.getString("PR-CODE");
            } catch (Exception _e) {
            }
        }
        if (rkVal_2 == null || rkVal_2.trim().isEmpty()) {
            try {
                rkVal_2 = fileSet.getProdf().extractKeyFromCurrentRecord();
            } catch (Exception _e3) {
            }
        }
        fileSet.getProdf().readByKey(rkVal_2 != null ? rkVal_2.trim() : "");
        ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
        if (fileSet.getProdf().isInvalidKey()) {
            ws.setWkPrName("*** unknown product ***");
        }
        if (!fileSet.getProdf().isInvalidKey()) {
            ws.setWkPrName(ws.getPrName());
        }
    }

    /** COBOL paragraph: SBRW-010 */
    private void startMovementBrowse() {
        ws.setBrwFlg(0);
        ws.setWkTotIn(0);
        ws.setWkTotOut(0);
        ws.setSmProd(ws.getWkKeyProd());
        ws.setSmDate(ws.getWkKeyDate());
        fileSet.getSmovf().start("SM-PROD", "NOT LESS THAN");
        ws.trySetString("FSTS", fileSet.getSmovf().getFileStatus());
        if (fileSet.getSmovf().isInvalidKey()) {
            ws.setWkMsgLine("No movements for that product");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            ws.setString("BRW-FLG", "1");
        }
    }

    /** COBOL paragraph: BLUP-010 */
    private void displayMovementListPage() {
        runChain(this::loadMovementPageRows);
        if (ws.getWkRow() == 0) {
            ws.setWkMsgLine("End of movements - PF3/PF4");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            ws.setString("BRW-FLG", "1");
            return;
        }
        ws.setWkPageNo(ws.getWkPageNo() + 1);
        ws.setWkMsgLine("ENTER/PF6=next page  PF3/PF4=re-key");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-LIST"), ws);
        broadcastEstsStatus();
        switch (String.valueOf(ws.getEsts())) {
            case "03" -> {
                ws.setString("BRW-FLG", "1");
            }
            case "04" -> {
                ws.setString("BRW-FLG", "1");
            }
            case "00" -> {
                /* CONTINUE */
            }
            case "06" -> {
                /* CONTINUE */
            }
            default -> {
                ws.setWkMsgLine("ENTER/PF6=next  PF3/PF4=re-key");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: FPG-010 */
    private void loadMovementPageRows() {
        for (ws.setWkIdx(1); ws.getWkIdx() <= 12; ws.setWkIdx(ws.getWkIdx() + 1)) {
            ws.setWkList(ws.getWkIdx(), " ");
        }
        ws.setWkRow(0);
        while (ws.getWkRow() < 12) {
            fileSet.getSmovf().readNext();
            ws.trySetString("FSTS", fileSet.getSmovf().getFileStatus());
            if (fileSet.getSmovf().isAtEnd()) {
                return;
            }
            if (!Utility.fieldEquals(ws.getFsts(), "00")) {
                return;
            }
            if (ws.getSmProd() != ws.getWkKeyProd()) {
                return;
            }
            ws.setWkRow(ws.getWkRow() + 1);
            runChain(this::formatMovementRow);
        }
    }

    /** COBOL paragraph: FMT-010 */
    private void formatMovementRow() {
        runChain(this::resolveMovementKindLabel);
        ws.setWkDlDate(ws.getSmDate());
        ws.setWkDlKind(ws.getWkKindLbl());
        ws.setWkDlQty(ws.getSmQty().intValue());
        ws.setWkDlBal(ws.getSmBalAfter().intValue());
        ws.setWkDlRtype(ws.getSmRefType());
        ws.setWkDlRno(ws.getSmRefNo());
        ws.setWkList(ws.getWkRow(), ws.getWkDl());
        if ((ws.getSmQty().signum() >= 0)) {
            ws.setWkTotIn((BigDecimal.valueOf(ws.getWkTotIn()).add(ws.getSmQty())).longValue());
        } else {
            ws.setWkTotOut(
                    (BigDecimal.valueOf(ws.getWkTotOut()).subtract(ws.getSmQty())).longValue());
        }
    }

    /** COBOL paragraph: KLB-010 */
    private void resolveMovementKindLabel() {
        switch (ws.getSmKind()) {
            case 10 -> {
                ws.setWkKindLbl("SaleOut ");
            }
            case 20 -> {
                ws.setWkKindLbl("PurchIn ");
            }
            case 30 -> {
                ws.setWkKindLbl("Adjust  ");
            }
            case 40 -> {
                ws.setWkKindLbl("Transfer");
            }
            case 50 -> {
                ws.setWkKindLbl("RetIn   ");
            }
            case 60 -> {
                ws.setWkKindLbl("RetOut  ");
            }
            default -> {
                ws.setWkKindLbl("Other   ");
            }
        }
    }

    /** COBOL paragraph: TERM-010 */
    private void closeAllFiles() {
        fileSet.getSmovf().close();
        ws.trySetString("FSTS", fileSet.getSmovf().getFileStatus());
        fileSet.getProdf().close();
        ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
    }

    /** COBOL paragraph: ABEND-010 */
    private void abortOnFileOpenError() {
        ws.setKaProgid("IV0040");
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
}
