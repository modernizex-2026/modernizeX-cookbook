package com.sakura.ar0020.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.ar0020.domain.Ar0020FieldAccess;
import com.sakura.ar0020.domain.WorkingStorage;
import com.sakura.ar0020.runtime.Ar0020Datasets;
import com.sakura.ar0020.screen.ScreenDefs;
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

/** Business logic service generated from COBOL program AR0020. */
@Service
@Scope("prototype")
public class Ar0020Service extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final Ar0020Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Ar0020FieldAccess ws;

    public Ar0020Service(
            Ar0020Datasets fileSet,
            DateutService dateutService,
            AbortxService abortxService,
            ScreenRendererInstance renderer) {
        this.fileSet = fileSet;
        this.ws = new Ar0020FieldAccess(new WorkingStorage(), fileSet);
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
            runChain(this::dispatchFunctionKey);
        }
        runChain(this::closeFiles);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeProgram() {
        ws.setWkProgid("AR0020");
        ws.setWkTitle("AR Inquiry / Aging");
        ws.setWkFkeyLine("ENTER=Inquire  PF3=End");
        ws.setKdFunc("TODY");
        dateut(ws.getKdate());
        ws.setWkSysymd(ws.getKdDate1());
        ws.setWkSysdate(ws.getKdDate1());
        runChain(this::openInputFiles);
    }

    /** COBOL paragraph: OPENF-010 */
    private void openInputFiles() {
        fileSet.getArlf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getArlf().getFileStatus());
        if (Utility.fieldEquals(ws.getFsts(), "35") || Utility.fieldEquals(ws.getFsts(), "30")) {
            fileSet.getArlf().open(FileOpenMode.OUTPUT);
            ws.trySetString("FSTS", fileSet.getArlf().getFileStatus());
            fileSet.getArlf().close();
            ws.trySetString("FSTS", fileSet.getArlf().getFileStatus());
            fileSet.getArlf().open(FileOpenMode.INPUT);
            ws.trySetString("FSTS", fileSet.getArlf().getFileStatus());
        }
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("ARLF");
            runChain(this::abortOnFileOpenError);
        }
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
    }

    /** COBOL paragraph: MAIN-010 */
    private void dispatchFunctionKey() {
        runChain(this::acceptCustomerKey);
        switch (String.valueOf(ws.getEsts())) {
            case "03" -> {
                ws.setString("END-FLG", "1");
            }
            case "00" -> {
                runChain(this::processCustomerInquiry);
            }
            default -> {
                ws.setWkMsgLine("Invalid function key");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: GKEY-010 */
    private void acceptCustomerKey() {
        ws.setWkKeyCust(0);
        ws.setWkCustName(" ");
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEADER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-FOOTER"), ws);
        ws.setWkMsgLine("Enter customer code then ENTER");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-KEY"), ws);
        String scValWkKeyCust0 =
                Utility.acceptScreen(
                        "WK-KEY-CUST",
                        () -> renderer.acceptField(ScreenDefs.getInput("WK-KEY-CUST")));
        ws.setWkKeyCust(Utility.parseIntOr(scValWkKeyCust0.trim(), 0));
        broadcastEstsStatus();
    }

    /** COBOL paragraph: PC-010 */
    private void processCustomerInquiry() {
        if (ws.getWkKeyCust() == 0) {
            ws.setWkMsgLine("Customer code must not be zero");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setCuCode(ws.getWkKeyCust());
        String rkVal_1 = "";
        if (rkVal_1 == null || rkVal_1.trim().isEmpty()) {
            try {
                rkVal_1 = ws.getString("CU-CODE");
            } catch (Exception _e) {
            }
        }
        if (rkVal_1 == null || rkVal_1.trim().isEmpty()) {
            try {
                rkVal_1 = fileSet.getCustf().extractKeyFromCurrentRecord();
            } catch (Exception _e3) {
            }
        }
        fileSet.getCustf().readByKey(rkVal_1 != null ? rkVal_1.trim() : "");
        ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
        if (fileSet.getCustf().isInvalidKey()) {
            ws.setWkMsgLine("Customer not found");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setWkCustName(ws.getCuName());
        ws.setWkCurBal(ws.getCuBalance().longValue());
        ws.setWkCrLimit(ws.getCuCreditLimit().longValue());
        if ((ws.getCuCreditLimit().signum() > 0)
                && (ws.getCuBalance().compareTo(ws.getCuCreditLimit()) > 0)) {
            ws.setWkOver("OVER LIMIT");
        } else {
            ws.setWkOver("OK");
        }
        runChain(this::scanLedgerForCustomer);
        runChain(this::displayAgingResults);
    }

    /** COBOL paragraph: SL-010 */
    private void scanLedgerForCustomer() {
        ws.setWkB030(0);
        ws.setWkB060(0);
        ws.setWkB090(0);
        ws.setWkB90p(0);
        ws.setWkTotDr(0);
        ws.setWkTotCr(0);
        ws.setWkLcnt(0);
        ws.setEofCust(0);
        ws.setAlCust(ws.getWkKeyCust());
        ws.setAlDate(0);
        fileSet.getArlf().start("AL-CUST", "NOT LESS THAN");
        ws.trySetString("FSTS", fileSet.getArlf().getFileStatus());
        if (fileSet.getArlf().isInvalidKey()) {
            ws.setString("EOF-CUST", "1");
        }
        while ((ws.getEofCust() != 1)) {
            fileSet.getArlf().readNext();
            ws.trySetString("FSTS", fileSet.getArlf().getFileStatus());
            if (fileSet.getArlf().isAtEnd()) {
                ws.setString("EOF-CUST", "1");
            }
            if ((ws.getEofCust() != 1)) {
                if (ws.getAlCust() != ws.getWkKeyCust()) {
                    ws.setString("EOF-CUST", "1");
                } else {
                    runChain(this::accumulateLedgerEntry);
                }
            }
        }
    }

    /** COBOL paragraph: PLN-010 */
    private void accumulateLedgerEntry() {
        ws.setWkTotDr((BigDecimal.valueOf(ws.getWkTotDr()).add(ws.getAlDebit())).longValue());
        ws.setWkTotCr((BigDecimal.valueOf(ws.getWkTotCr()).add(ws.getAlCredit())).longValue());
        runChain(this::calculateAgingBucket);
        if (ws.getWkLcnt() < 500) {
            ws.setWkLcnt(ws.getWkLcnt() + 1);
            runChain(this::formatLedgerLine);
            ws.setWkAll(ws.getWkLcnt(), ws.getWkDl());
        }
    }

    /** COBOL paragraph: AGL-010 */
    private void calculateAgingBucket() {
        ws.setWkNet(ws.getAlDebit().subtract(ws.getAlCredit()).longValue());
        ws.setKdFunc("DIFF");
        ws.setKdDate1(ws.getAlDate());
        ws.setKdDate2(ws.getWkSysdate());
        ws.setKdDays(0);
        dateut(ws.getKdate());
        ws.setWkDays(ws.getKdDays());
        if (ws.getWkDays() < 0) {
            ws.setWkDays(0);
        }
        if (ws.getWkDays() <= 30) {
            ws.setWkB030(ws.getWkB030() + ws.getWkNet());
        } else if (ws.getWkDays() <= 60) {
            ws.setWkB060(ws.getWkB060() + ws.getWkNet());
        } else if (ws.getWkDays() <= 90) {
            ws.setWkB090(ws.getWkB090() + ws.getWkNet());
        } else {
            ws.setWkB90p(ws.getWkB90p() + ws.getWkNet());
        }
    }

    /** COBOL paragraph: FMT-010 */
    private void formatLedgerLine() {
        runChain(this::mapKindToLabel);
        ws.setWkDlDate(ws.getAlDate());
        ws.setWkDlKind(ws.getWkKindLbl());
        ws.setWkDlDr(ws.getAlDebit().longValue());
        ws.setWkDlCr(ws.getAlCredit().longValue());
        ws.setWkDlBal(ws.getAlBalance().longValue());
    }

    /** COBOL paragraph: KLB-010 */
    private void mapKindToLabel() {
        switch (ws.getAlKind()) {
            case 1 -> {
                ws.setWkKindLbl("Sale  ");
            }
            case 2 -> {
                ws.setWkKindLbl("Recpt ");
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
    private void displayAgingResults() {
        runChain(this::buildDisplayRows);
        ws.setWkMsgLine("Ledger scanned - press any key");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-AGE"), ws);
        broadcastEstsStatus();
        if (Utility.fieldEquals(ws.getEsts(), "03")) {
            ws.setString("END-FLG", "1");
        }
    }

    /** COBOL paragraph: LR-010 */
    private void buildDisplayRows() {
        for (ws.setWkIdx(1); ws.getWkIdx() <= 8; ws.setWkIdx(ws.getWkIdx() + 1)) {
            ws.setWkList(ws.getWkIdx(), " ");
        }
        if (ws.getWkLcnt() == 0) {
            return;
        }
        if (ws.getWkLcnt() <= 8) {
            ws.setWkStart(1);
        } else {
            ws.setWkStart((ws.getWkLcnt() - 7));
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
        fileSet.getArlf().close();
        ws.trySetString("FSTS", fileSet.getArlf().getFileStatus());
        fileSet.getCustf().close();
        ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
    }

    /** COBOL paragraph: ABEND-010 */
    private void abortOnFileOpenError() {
        ws.setKaProgid("AR0020");
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
