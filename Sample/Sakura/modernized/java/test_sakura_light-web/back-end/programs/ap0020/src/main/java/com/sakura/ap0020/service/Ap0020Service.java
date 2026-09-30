package com.sakura.ap0020.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.ap0020.domain.Ap0020FieldAccess;
import com.sakura.ap0020.domain.WorkingStorage;
import com.sakura.ap0020.runtime.Ap0020Datasets;
import com.sakura.ap0020.screen.ScreenDefs;
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

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/** Business logic service generated from COBOL program AP0020. */
@Service
@Scope("prototype")
public class Ap0020Service extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final Ap0020Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Ap0020FieldAccess ws;

    public Ap0020Service(
            Ap0020Datasets fileSet,
            DateutService dateutService,
            AbortxService abortxService,
            ScreenRendererInstance renderer) {
        this.fileSet = fileSet;
        this.ws = new Ap0020FieldAccess(new WorkingStorage(), fileSet);
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
        runChain(this::closeFiles);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeProgram() {
        ws.setWkProgid("AP0020");
        ws.setWkTitle("AP Inquiry");
        ws.setWkFkeyLine("ENTER=Inquire  PF3=End");
        ws.setKdFunc("TODY");
        dateut(ws.getKdate());
        ws.setWkSysymd(ws.getKdDate1());
        ws.setWkSysdate(ws.getKdDate1());
        runChain(this::openInputFiles);
    }

    /** COBOL paragraph: OPENF-010 */
    private void openInputFiles() {
        fileSet.getAplf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getAplf().getFileStatus());
        if (Utility.fieldEquals(ws.getFsts(), "35") || Utility.fieldEquals(ws.getFsts(), "30")) {
            fileSet.getAplf().open(FileOpenMode.OUTPUT);
            ws.trySetString("FSTS", fileSet.getAplf().getFileStatus());
            fileSet.getAplf().close();
            ws.trySetString("FSTS", fileSet.getAplf().getFileStatus());
            fileSet.getAplf().open(FileOpenMode.INPUT);
            ws.trySetString("FSTS", fileSet.getAplf().getFileStatus());
        }
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("APLF");
            runChain(this::abortOnFileOpenError);
        }
        fileSet.getSuppf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getSuppf().getFileStatus());
        if (Utility.fieldEquals(ws.getFsts(), "35") || Utility.fieldEquals(ws.getFsts(), "30")) {
            fileSet.getSuppf().open(FileOpenMode.OUTPUT);
            ws.trySetString("FSTS", fileSet.getSuppf().getFileStatus());
            fileSet.getSuppf().close();
            ws.trySetString("FSTS", fileSet.getSuppf().getFileStatus());
            fileSet.getSuppf().open(FileOpenMode.INPUT);
            ws.trySetString("FSTS", fileSet.getSuppf().getFileStatus());
        }
    }

    /** COBOL paragraph: MAIN-010 */
    private void processMainScreen() {
        runChain(this::acceptSupplierKey);
        switch (String.valueOf(ws.getEsts())) {
            case "03" -> {
                ws.setString("END-FLG", "1");
            }
            case "00" -> {
                runChain(this::processSupplierInquiry);
            }
            default -> {
                ws.setWkMsgLine("Invalid function key");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: GKEY-010 */
    private void acceptSupplierKey() {
        ws.setWkKeySupp(0);
        ws.setWkSuppName(" ");
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEADER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-FOOTER"), ws);
        ws.setWkMsgLine("Enter supplier code then ENTER");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-KEY"), ws);
        String scValWkKeySupp0 =
                Utility.acceptScreen(
                        "WK-KEY-SUPP",
                        () -> renderer.acceptField(ScreenDefs.getInput("WK-KEY-SUPP")));
        ws.setWkKeySupp(Utility.parseIntOr(scValWkKeySupp0.trim(), 0));
        broadcastEstsStatus();
    }

    /** COBOL paragraph: PS-010 */
    private void processSupplierInquiry() {
        if (ws.getWkKeySupp() == 0) {
            ws.setWkMsgLine("Supplier code must not be zero");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setSpCode(ws.getWkKeySupp());
        String rkVal_1 = "";
        if (rkVal_1 == null || rkVal_1.trim().isEmpty()) {
            try {
                rkVal_1 = ws.getString("SP-CODE");
            } catch (Exception _e) {
            }
        }
        if (rkVal_1 == null || rkVal_1.trim().isEmpty()) {
            try {
                rkVal_1 = fileSet.getSuppf().extractKeyFromCurrentRecord();
            } catch (Exception _e3) {
            }
        }
        fileSet.getSuppf().readByKey(rkVal_1 != null ? rkVal_1.trim() : "");
        ws.trySetString("FSTS", fileSet.getSuppf().getFileStatus());
        if (fileSet.getSuppf().isInvalidKey()) {
            ws.setWkMsgLine("Supplier not found");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setWkSuppName(ws.getSpName());
        ws.setWkCurBal(ws.getSpBalance().longValue());
        runChain(this::scanLedgerEntries);
        runChain(this::displayLedgerResults);
    }

    /** COBOL paragraph: SL-010 */
    private void scanLedgerEntries() {
        ws.setWkTotDr(0);
        ws.setWkTotCr(0);
        ws.setWkLcnt(0);
        ws.setEofSupp(0);
        ws.setPlSupp(ws.getWkKeySupp());
        ws.setPlDate(0);
        fileSet.getAplf().start("PL-SUPP", "NOT LESS THAN");
        ws.trySetString("FSTS", fileSet.getAplf().getFileStatus());
        if (fileSet.getAplf().isInvalidKey()) {
            ws.setString("EOF-SUPP", "1");
        }
        while ((ws.getEofSupp() != 1)) {
            fileSet.getAplf().readNext();
            ws.trySetString("FSTS", fileSet.getAplf().getFileStatus());
            if (fileSet.getAplf().isAtEnd()) {
                ws.setString("EOF-SUPP", "1");
            }
            if ((ws.getEofSupp() != 1)) {
                if (ws.getPlSupp() != ws.getWkKeySupp()) {
                    ws.setString("EOF-SUPP", "1");
                } else {
                    runChain(this::accumulateLedgerLine);
                }
            }
        }
    }

    /** COBOL paragraph: PLN-010 */
    private void accumulateLedgerLine() {
        ws.setWkTotDr((BigDecimal.valueOf(ws.getWkTotDr()).add(ws.getPlDebit())).longValue());
        ws.setWkTotCr((BigDecimal.valueOf(ws.getWkTotCr()).add(ws.getPlCredit())).longValue());
        if (ws.getWkLcnt() < 500) {
            ws.setWkLcnt(ws.getWkLcnt() + 1);
            runChain(this::formatLedgerLine);
            ws.setWkAll(ws.getWkLcnt(), ws.getWkDl());
        }
    }

    /** COBOL paragraph: FMT-010 */
    private void formatLedgerLine() {
        runChain(this::resolveKindLabel);
        ws.setWkDlDate(ws.getPlDate());
        ws.setWkDlKind(ws.getWkKindLbl());
        ws.setWkDlDr(ws.getPlDebit().longValue());
        ws.setWkDlCr(ws.getPlCredit().longValue());
        ws.setWkDlBal(ws.getPlBalance().longValue());
    }

    /** COBOL paragraph: KLB-010 */
    private void resolveKindLabel() {
        switch (ws.getPlKind()) {
            case 1 -> {
                ws.setWkKindLbl("Purch ");
            }
            case 2 -> {
                ws.setWkKindLbl("Paymnt");
            }
            case 3 -> {
                ws.setWkKindLbl("Retn  ");
            }
            case 4 -> {
                ws.setWkKindLbl("Adjust");
            }
            default -> {
                ws.setWkKindLbl("Other ");
            }
        }
    }

    /** COBOL paragraph: SR-010 */
    private void displayLedgerResults() {
        runChain(this::buildLedgerDisplayList);
        ws.setWkMsgLine("Ledger scanned - press any key");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-VIEW"), ws);
        broadcastEstsStatus();
        if (Utility.fieldEquals(ws.getEsts(), "03")) {
            ws.setString("END-FLG", "1");
        }
    }

    /** COBOL paragraph: LR-010 */
    private void buildLedgerDisplayList() {
        for (ws.setWkIdx(1); ws.getWkIdx() <= 12; ws.setWkIdx(ws.getWkIdx() + 1)) {
            ws.setWkList(ws.getWkIdx(), " ");
        }
        if (ws.getWkLcnt() == 0) {
            return;
        }
        if (ws.getWkLcnt() <= 12) {
            ws.setWkStart(1);
        } else {
            ws.setWkStart((ws.getWkLcnt() - 11));
        }
        ws.setWkRow(0);
        for (ws.setWkIdx(ws.getWkStart());
                ws.getWkIdx() <= ws.getWkLcnt();
                ws.setWkIdx(ws.getWkIdx() + 1)) {
            ws.setWkRow(ws.getWkRow() + 1);
            ws.setWkList(ws.getWkRow(), ws.getWkAll(ws.getWkIdx()));
        }
    }

    /** COBOL paragraph: TERM-010 */
    private void closeFiles() {
        fileSet.getAplf().close();
        ws.trySetString("FSTS", fileSet.getAplf().getFileStatus());
        fileSet.getSuppf().close();
        ws.trySetString("FSTS", fileSet.getSuppf().getFileStatus());
    }

    /** COBOL paragraph: ABEND-010 */
    private void abortOnFileOpenError() {
        ws.setKaProgid("AP0020");
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
