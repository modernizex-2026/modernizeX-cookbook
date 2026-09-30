package com.sakura.rc0010.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.numgen.service.NumgenService;
import com.sakura.rc0010.domain.Rc0010FieldAccess;
import com.sakura.rc0010.domain.WorkingStorage;
import com.sakura.rc0010.runtime.Rc0010Datasets;
import com.sakura.rc0010.screen.ScreenDefs;
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
import com.sakura.runtime.linkage.NumgenLinkParm;
import com.sakura.runtime.record.RawDatasetBase;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/** Business logic service generated from COBOL program RC0010. */
@Service
@Scope("prototype")
public class Rc0010Service extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final Rc0010Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL NUMGEN. */
    private NumgenService numgenService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Rc0010FieldAccess ws;

    public Rc0010Service(
            Rc0010Datasets fileSet,
            DateutService dateutService,
            NumgenService numgenService,
            AbortxService abortxService,
            ScreenRendererInstance renderer) {
        this.fileSet = fileSet;
        this.ws = new Rc0010FieldAccess(new WorkingStorage(), fileSet);
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
        ws.setWkProgid("RC0010");
        ws.setWkTitle("Goods Receiving Entry");
        ws.setWkFkeyLine("ENTER=Accept  PF4=Skip line  PF3=End");
        ws.setKdFunc("TODY");
        dateut(ws.getKdate());
        ws.setWkSysymd(ws.getKdDate1());
        ws.setWkSysdate(ws.getKdDate1());
        runChain(this::openAllFiles);
    }

    /** COBOL paragraph: OPENF-010 */
    private void openAllFiles() {
        runChain(this::openPohfWithRetry);
        runChain(this::openPodfWithRetry);
        runChain(this::openRcvhfWithRetry);
        runChain(this::openRcvdfWithRetry);
        runChain(this::openStokfWithRetry);
        runChain(this::openSmovfWithRetry);
        fileSet.getSuppf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getSuppf().getFileStatus());
        fileSet.getProdf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
    }

    /** COBOL paragraph: OFPH-010 */
    private void openPohfWithRetry() {
        openFileWithRetry(fileSet.getPohf(), "POHF");
    }

    /** COBOL paragraph: OFPD-010 */
    private void openPodfWithRetry() {
        openFileWithRetry(fileSet.getPodf(), "PODF");
    }

    /** COBOL paragraph: OFRH-010 */
    private void openRcvhfWithRetry() {
        openFileWithRetry(fileSet.getRcvhf(), "RCVHF");
    }

    /** COBOL paragraph: OFRD-010 */
    private void openRcvdfWithRetry() {
        openFileWithRetry(fileSet.getRcvdf(), "RCVDF");
    }

    /** COBOL paragraph: OFSK-010 */
    private void openStokfWithRetry() {
        openFileWithRetry(fileSet.getStokf(), "STOKF");
    }

    /** COBOL paragraph: OFSM-010 */
    private void openSmovfWithRetry() {
        openFileWithRetry(fileSet.getSmovf(), "SMOVF");
    }

    /** COBOL paragraph: MAIN-010 */
    private void processMainScreen() {
        runChain(this::clearReceivingWorkArea);
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEADER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-FOOTER"), ws);
        ws.setWkMsgLine("Enter the purchase order number to receive");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-KEY"), ws);
        String scValWkPoKey0 =
                Utility.acceptScreen(
                        "WK-PO-KEY", () -> renderer.acceptField(ScreenDefs.getInput("WK-PO-KEY")));
        ws.setWkPoKey(Utility.parseLongOr(scValWkPoKey0.trim(), 0L));
        broadcastEstsStatus();
        switch (String.valueOf(ws.getEsts())) {
            case "03" -> {
                ws.setString("END-FLG", "1");
            }
            case "00" -> {
                runChain(this::processPurchaseOrder);
            }
            default -> {
                ws.setWkMsgLine("Invalid function key");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: CLRR-010 */
    private void clearReceivingWorkArea() {
        ws.setWkPoKey(0);
        ws.setWkRhNoD(0);
        ws.setWkLcnt(0);
        ws.setWkRecvCnt(0);
        ws.setPoOk(0);
        ws.setWkEntryDone(0);
        ws.setWkSuppName(" ");
        ws.setWkConfirm(" ");
        ws.setRhDate(ws.getWkSysdate());
    }

    /** COBOL paragraph: PPO-010 */
    private void processPurchaseOrder() {
        if (ws.getWkPoKey() == 0) {
            ws.setWkMsgLine("PO number required");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setPhNo(ws.getWkPoKey());
        readByKeyWithFallback(fileSet.getPohf(), "PH-NO");
        if (fileSet.getPohf().isInvalidKey()) {
            ws.setWkMsgLine("PO not found");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        if (ws.getPhDelFlag() == 1) {
            ws.setWkMsgLine("PO is deleted");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        if (ws.getPhStatus() == 9) {
            ws.setWkMsgLine("PO is cancelled");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        if (ws.getPhStatus() == 2 || ws.getPhStatus() == 3) {
            ws.setWkMsgLine("PO already fully received");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        runChain(this::loadSupplierName);
        runChain(this::loadPoLines);
        if (ws.getWkLcnt() == 0) {
            ws.setWkMsgLine("No outstanding lines on this PO");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setPoOk(1);
        runChain(this::confirmReceivingDate);
        if ((ws.getPoOk() != 1)) {
            return;
        }
        runChain(this::enterReceivingLines);
        runChain(this::countReceivingLines);
        if (ws.getWkRecvCnt() > 0) {
            runChain(this::confirmAndSaveReceiving);
        } else {
            ws.setWkMsgLine("Nothing received - discarded");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
    }

    /** COBOL paragraph: LSN-010 */
    private void loadSupplierName() {
        ws.setWkSuppName(" ");
        ws.setSpCode(ws.getPhSupp());
        readByKeyWithFallback(fileSet.getSuppf(), "SP-CODE");
        if (fileSet.getSuppf().isInvalidKey()) {
            ws.setWkSuppName("??? unknown supplier");
        }
        if (!fileSet.getSuppf().isInvalidKey()) {
            ws.setWkSuppName(ws.getSpName());
        }
    }

    /** COBOL paragraph: LPL-010 */
    private void loadPoLines() {
        ws.setWkLcnt(0);
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
            }
            if (!fileSet.getPodf().isAtEnd()) {
                if (ws.getPdNo() != ws.getPhNo()) {
                    ws.setString("EOF-FLG", "1");
                } else {
                    runChain(this::addOutstandingLine);
                }
            }
        }
    }

    /** COBOL paragraph: AOS-010 */
    private void addOutstandingLine() {
        if (ws.getPdStatus() == 9) {
            return;
        }
        if ((ws.getPdRecvQty().compareTo(ws.getPdQty()) >= 0)) {
            return;
        }
        if (ws.getWkLcnt() >= 200) {
            return;
        }
        ws.setWkLcnt(ws.getWkLcnt() + 1);
        ws.setWlPoline(ws.getWkLcnt(), ws.getPdLine());
        ws.setWlProd(ws.getWkLcnt(), ws.getPdProd());
        ws.setWlWhse(ws.getWkLcnt(), ws.getPdWhse());
        ws.setWlOrd(ws.getWkLcnt(), ws.getPdQty().intValue());
        ws.setWlPrecv(ws.getWkLcnt(), ws.getPdRecvQty().intValue());
        ws.setWlOut(ws.getWkLcnt(), ws.getPdQty().subtract(ws.getPdRecvQty()).intValue());
        ws.setWlCost(ws.getWkLcnt(), ws.getPdUnitCost());
        ws.setWlRecv(ws.getWkLcnt(), ws.getWlOut(ws.getWkLcnt()));
        runChain(this::loadProductName);
    }

    /** COBOL paragraph: LPN-010 */
    private void loadProductName() {
        ws.setWlName(ws.getWkLcnt(), " ");
        ws.setWlStkmng(ws.getWkLcnt(), 0);
        ws.setPrCode(ws.getPdProd());
        readByKeyWithFallback(fileSet.getProdf(), "PR-CODE");
        if (fileSet.getProdf().isInvalidKey()) {
            ws.setWlName(ws.getWkLcnt(), "??? unknown product");
        }
        if (!fileSet.getProdf().isInvalidKey()) {
            ws.setWlName(ws.getWkLcnt(), ws.getPrName());
            ws.setWlStkmng(ws.getWkLcnt(), ws.getPrStockMng());
        }
    }

    /** COBOL paragraph: ARH-010 */
    private void confirmReceivingDate() {
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEADER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-FOOTER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-PINFO"), ws);
        ws.setWkMsgLine("Confirm receiving date - PF3 to cancel");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-RDATE"), ws);
        String scValRhDate4 =
                Utility.acceptScreen(
                        "RH-DATE", () -> renderer.acceptField(ScreenDefs.getInput("RH-DATE")));
        ws.setRhDate(Utility.parseIntOr(scValRhDate4.trim(), 0));
        broadcastEstsStatus();
        if (Utility.fieldEquals(ws.getEsts(), "03")) {
            ws.setPoOk(0);
            ws.setWkMsgLine("Cancelled");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
    }

    /** COBOL paragraph: ERL-010 */
    private void enterReceivingLines() {
        ws.setWkCurline(1);
        ws.setWkEntryDone(0);
        while (!(ws.getWkEntryDone() == 1 || ws.getWkCurline() > ws.getWkLcnt())) {
            runChain(this::showReceivingLine);
            String scValWkDRecv5 =
                    Utility.acceptScreen(
                            "WK-D-RECV",
                            () -> renderer.acceptField(ScreenDefs.getInput("WK-D-RECV")));
            ws.setWkDRecv(Utility.parseIntOr(scValWkDRecv5.trim(), 0));
            broadcastEstsStatus();
            switch (String.valueOf(ws.getEsts())) {
                case "03" -> {
                    ws.setWkEntryDone(1);
                }
                case "04" -> {
                    ws.setWlRecv(ws.getWkCurline(), 0);
                    ws.setWkMsgLine("Line skipped");
                    renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
                    ws.setWkCurline(ws.getWkCurline() + 1);
                }
                case "00" -> {
                    runChain(this::validateReceivingQty);
                    if ((ws.getErrFlg() != 1)) {
                        ws.setWkCurline(ws.getWkCurline() + 1);
                    }
                }
                default -> {
                    ws.setWkMsgLine("Invalid key");
                    renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
                }
            }
        }
    }

    /** COBOL paragraph: SRL-010 */
    private void showReceivingLine() {
        ws.setWkLPos(ws.getWkCurline());
        ws.setWkLPoline(ws.getWlPoline(ws.getWkCurline()));
        ws.setWkLProd(ws.getWlProd(ws.getWkCurline()));
        ws.setWkLName(ws.getWlName(ws.getWkCurline()));
        ws.setWkLOrd(ws.getWlOrd(ws.getWkCurline()));
        ws.setWkLPrecv(ws.getWlPrecv(ws.getWkCurline()));
        ws.setWkLOut(ws.getWlOut(ws.getWkCurline()));
        ws.setWkLCost(ws.getWlCost(ws.getWkCurline()));
        ws.setWkDRecv(ws.getWlRecv(ws.getWkCurline()));
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEADER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-FOOTER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-PINFO"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-LINE"), ws);
        ws.setWkMsgLine("Enter received qty (0 to skip)");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
    }

    /** COBOL paragraph: VRL-010 */
    private void validateReceivingQty() {
        ws.setErrFlg(0);
        if (ws.getWkDRecv() < 0) {
            ws.setWkMsgLine("Received qty cannot be negative");
            ws.setString("ERR-FLG", "1");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        if (ws.getWkDRecv() > ws.getWlOut(ws.getWkCurline())) {
            ws.setWkMsgLine("Received qty exceeds outstanding");
            ws.setString("ERR-FLG", "1");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setWlRecv(ws.getWkCurline(), ws.getWkDRecv());
    }

    /** COBOL paragraph: CRL-010 */
    private void countReceivingLines() {
        ws.setWkRecvCnt(0);
        for (ws.setWkIdx(1); ws.getWkIdx() <= ws.getWkLcnt(); ws.setWkIdx(ws.getWkIdx() + 1)) {
            if (ws.getWlRecv(ws.getWkIdx()) > 0) {
                ws.setWkRecvCnt(ws.getWkRecvCnt() + 1);
            }
        }
    }

    /** COBOL paragraph: CSAV-010 */
    private void confirmAndSaveReceiving() {
        ws.setWkConfirm(" ");
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEADER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-FOOTER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-PINFO"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-CONFIRM"), ws);
        ws.setWkMsgLine("Confirm to post receiving");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        String scValWkConfirm6 =
                Utility.acceptScreen(
                        "WK-CONFIRM",
                        () -> renderer.acceptField(ScreenDefs.getInput("WK-CONFIRM")));
        ws.setWkConfirm(scValWkConfirm6);
        broadcastEstsStatus();
        if ((ws.getWkConfirm().equals("Y") || ws.getWkConfirm().equals("y"))) {
            runChain(this::saveReceivingRecord);
        } else {
            ws.setWkMsgLine("Receiving discarded");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
    }

    /** COBOL paragraph: SRV-010 */
    private void saveReceivingRecord() {
        ws.setKnumKey("RECV");
        numgen(ws.getKnum());
        if (!Utility.fieldEquals(ws.getKnumStatus(), "00")) {
            ws.setWkMsgLine("Receiving number assignment failed");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setWkRecvDate(ws.getRhDate());
        fileSet.getRcvhf().setRecord();
        ws.setRhNo(ws.getKnumNumber());
        ws.setWkRhNoD(ws.getKnumNumber());
        ws.setRhDate(ws.getWkRecvDate());
        ws.setRhPo(ws.getPhNo());
        ws.setRhSupp(ws.getPhSupp());
        ws.setRhWhse(ws.getPhWhse());
        ws.setRhStatus(0);
        ws.setRhLines(ws.getWkRecvCnt());
        ws.setRhRemark(" ");
        ws.setRhAddDate(ws.getWkSysdate());
        ws.setRhAddUser(ws.getWkUserCode());
        ws.setRhDelFlag(0);
        fileSet.getRcvhf().write();
        ws.trySetString("FSTS", fileSet.getRcvhf().getFileStatus());
        if (fileSet.getRcvhf().isInvalidKey()) {
            ws.setWkMsgLine("Receiving header write failed");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setWkRdLine(0);
        for (ws.setWkIdx(1); ws.getWkIdx() <= ws.getWkLcnt(); ws.setWkIdx(ws.getWkIdx() + 1)) {
            if (ws.getWlRecv(ws.getWkIdx()) > 0) {
                runChain(this::postReceivingLine);
            }
        }
        runChain(this::scanPoLinesForOutstanding);
        ws.setWkMsgLine("Receiving posted");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
    }

    /** COBOL paragraph: POL-010 */
    private void postReceivingLine() {
        ws.setWkRdLine(ws.getWkRdLine() + 1);
        fileSet.getRcvdf().setRecord();
        ws.setRdNo(ws.getRhNo());
        ws.setRdLine(ws.getWkRdLine());
        ws.setRdPo(ws.getPhNo());
        ws.setRdPoLine(ws.getWlPoline(ws.getWkIdx()));
        ws.setRdProd(ws.getWlProd(ws.getWkIdx()));
        ws.setRdWhse(ws.getWlWhse(ws.getWkIdx()));
        ws.setRdQty(BigDecimal.valueOf(ws.getWlRecv(ws.getWkIdx())));
        ws.setRdUnitCost(ws.getWlCost(ws.getWkIdx()));
        ws.setRdAmount(
                (BigDecimal.valueOf(ws.getWlRecv(ws.getWkIdx()))
                                .multiply(ws.getWlCost(ws.getWkIdx())))
                        .setScale(0, java.math.RoundingMode.HALF_UP));
        fileSet.getRcvdf().write();
        ws.trySetString("FSTS", fileSet.getRcvdf().getFileStatus());
        if (fileSet.getRcvdf().isInvalidKey()) {
            /* CONTINUE */
        }
        if (ws.getWlStkmng(ws.getWkIdx()) == 1) {
            runChain(this::updateStockInventory);
            runChain(this::writeStockMovement);
        }
        runChain(this::updatePoLineReceived);
    }

    /** COBOL paragraph: USI-010 */
    private void updateStockInventory() {
        ws.setSkProd(ws.getWlProd(ws.getWkIdx()));
        ws.setSkWhse(ws.getWlWhse(ws.getWkIdx()));
        StringBuilder rkSb_7 = new StringBuilder();
        String rkPart0_7 = "";
        try {
            rkPart0_7 = ws.getString("SK-PROD");
        } catch (Exception _e) {
        }
        rkSb_7.append(rkPart0_7 != null ? rkPart0_7.trim() : "");
        String rkPart1_7 = "";
        try {
            rkPart1_7 = ws.getString("SK-WHSE");
        } catch (Exception _e) {
        }
        rkSb_7.append('|');
        rkSb_7.append(rkPart1_7 != null ? rkPart1_7.trim() : "");
        fileSet.getStokf().readByKey(rkSb_7.toString());
        ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
        if (fileSet.getStokf().isInvalidKey()) {
            runChain(this::createStockRecord);
        }
        if (!fileSet.getStokf().isInvalidKey()) {
            runChain(this::adjustStockOnhand);
        }
    }

    /** COBOL paragraph: CST-010 */
    private void createStockRecord() {
        fileSet.getStokf().setRecord();
        ws.setSkProd(ws.getWlProd(ws.getWkIdx()));
        ws.setSkWhse(ws.getWlWhse(ws.getWkIdx()));
        ws.setSkOnhand(BigDecimal.valueOf(ws.getWlRecv(ws.getWkIdx())));
        ws.setSkAllocated(BigDecimal.ZERO);
        ws.setSkOnOrder(
                (BigDecimal.ZERO.subtract(BigDecimal.valueOf(ws.getWlRecv(ws.getWkIdx()))))
                        .setScale(0, java.math.RoundingMode.DOWN));
        if ((ws.getSkOnOrder().signum() < 0)) {
            ws.setSkOnOrder(BigDecimal.ZERO);
        }
        ws.setSkAvgCost(ws.getWlCost(ws.getWkIdx()));
        ws.setSkLastInDate(ws.getRhDate());
        ws.setSkYtdIn(BigDecimal.valueOf(ws.getWlRecv(ws.getWkIdx())));
        fileSet.getStokf().write();
        ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
        if (fileSet.getStokf().isInvalidKey()) {
            /* CONTINUE */
        }
    }

    /** COBOL paragraph: ADJ-010 */
    private void adjustStockOnhand() {
        ws.setWkNewOnhand(
                ws.getSkOnhand().add(BigDecimal.valueOf(ws.getWlRecv(ws.getWkIdx()))).intValue());
        if (ws.getWkNewOnhand() > 0) {
            ws.setWkAvgNum(
                    (ws.getSkOnhand()
                                    .multiply(ws.getSkAvgCost())
                                    .add(
                                            BigDecimal.valueOf(ws.getWlRecv(ws.getWkIdx()))
                                                    .multiply(ws.getWlCost(ws.getWkIdx()))))
                            .setScale(2, java.math.RoundingMode.HALF_UP));
            ws.setSkAvgCost(
                    (ws.getWkAvgNum()
                                    .divide(
                                            BigDecimal.valueOf(ws.getWkNewOnhand()),
                                            12,
                                            java.math.RoundingMode.HALF_UP))
                            .setScale(2, java.math.RoundingMode.HALF_UP));
        }
        ws.setSkOnhand(BigDecimal.valueOf(ws.getWkNewOnhand()));
        ws.setSkOnOrder(
                ws.getSkOnOrder().subtract(BigDecimal.valueOf(ws.getWlRecv(ws.getWkIdx()))));
        ws.setSkLastInDate(ws.getRhDate());
        ws.setSkYtdIn(ws.getSkYtdIn().add(BigDecimal.valueOf(ws.getWlRecv(ws.getWkIdx()))));
        fileSet.getStokf().rewrite();
        ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
        if (fileSet.getStokf().isInvalidKey()) {
            /* CONTINUE */
        }
    }

    /** COBOL paragraph: WMV-010 */
    private void writeStockMovement() {
        ws.setKnumKey("STKMOV");
        numgen(ws.getKnum());
        if (!Utility.fieldEquals(ws.getKnumStatus(), "00")) {
            return;
        }
        fileSet.getSmovf().setRecord();
        ws.setSmSeq(ws.getKnumNumber());
        ws.setSmDate(ws.getRhDate());
        ws.setSmProd(ws.getWlProd(ws.getWkIdx()));
        ws.setSmWhse(ws.getWlWhse(ws.getWkIdx()));
        ws.setSmKind(20);
        ws.setSmQty(BigDecimal.valueOf(ws.getWlRecv(ws.getWkIdx())));
        ws.setSmUnitCost(ws.getWlCost(ws.getWkIdx()));
        ws.setSmBalAfter(ws.getSkOnhand());
        ws.setSmRefType(22);
        ws.setSmRefNo(ws.getRhNo());
        ws.setSmUser(ws.getWkUserCode());
        fileSet.getSmovf().write();
        ws.trySetString("FSTS", fileSet.getSmovf().getFileStatus());
        if (fileSet.getSmovf().isInvalidKey()) {
            /* CONTINUE */
        }
    }

    /** COBOL paragraph: UPL-010 */
    private void updatePoLineReceived() {
        ws.setPdNo(ws.getPhNo());
        ws.setPdLine(ws.getWlPoline(ws.getWkIdx()));
        StringBuilder rkSb_8 = new StringBuilder();
        String rkPart0_8 = "";
        try {
            rkPart0_8 = ws.getString("PD-NO");
        } catch (Exception _e) {
        }
        rkSb_8.append(rkPart0_8 != null ? rkPart0_8.trim() : "");
        String rkPart1_8 = "";
        try {
            rkPart1_8 = ws.getString("PD-LINE");
        } catch (Exception _e) {
        }
        rkSb_8.append('|');
        rkSb_8.append(rkPart1_8 != null ? rkPart1_8.trim() : "");
        fileSet.getPodf().readByKey(rkSb_8.toString());
        ws.trySetString("FSTS", fileSet.getPodf().getFileStatus());
        if (fileSet.getPodf().isInvalidKey()) {
            return;
        }
        ws.setPdRecvQty(ws.getPdRecvQty().add(BigDecimal.valueOf(ws.getWlRecv(ws.getWkIdx()))));
        if ((ws.getPdRecvQty().compareTo(ws.getPdQty()) >= 0)) {
            ws.setPdStatus(2);
        } else {
            ws.setPdStatus(1);
        }
        fileSet.getPodf().rewrite();
        ws.trySetString("FSTS", fileSet.getPodf().getFileStatus());
        if (fileSet.getPodf().isInvalidKey()) {
            /* CONTINUE */
        }
    }

    /** COBOL paragraph: UPS-010 */
    private void scanPoLinesForOutstanding() {
        ws.setWkOutFlg(0);
        ws.setPdNo(ws.getPhNo());
        ws.setPdLine(0);
        fileSet.getPodf().start("PD-NO", "NOT LESS THAN");
        ws.trySetString("FSTS", fileSet.getPodf().getFileStatus());
        if (fileSet.getPodf().isInvalidKey()) {
            throw new ParagraphJumpSignal(this::updatePoHeaderStatus);
        }
        ws.setEofFlg(0);
        while ((ws.getEofFlg() != 1)) {
            fileSet.getPodf().readNext();
            ws.trySetString("FSTS", fileSet.getPodf().getFileStatus());
            if (fileSet.getPodf().isAtEnd()) {
                ws.setString("EOF-FLG", "1");
            }
            if (!fileSet.getPodf().isAtEnd()) {
                if (ws.getPdNo() != ws.getPhNo()) {
                    ws.setString("EOF-FLG", "1");
                } else {
                    if (ws.getPdStatus() != 9 && (ws.getPdRecvQty().compareTo(ws.getPdQty()) < 0)) {
                        ws.setWkOutFlg(1);
                    }
                }
            }
        }
        // fall-through to next paragraph
        updatePoHeaderStatus();
    }

    /** COBOL paragraph: UPS-020 */
    private void updatePoHeaderStatus() {
        if (ws.getWkOutFlg() == 1) {
            ws.setWkNewStatus(1);
        } else {
            ws.setWkNewStatus(2);
        }
        ws.setPhNo(ws.getWkPoKey());
        readByKeyWithFallback(fileSet.getPohf(), "PH-NO");
        if (fileSet.getPohf().isInvalidKey()) {
            return;
        }
        ws.setPhStatus(ws.getWkNewStatus());
        fileSet.getPohf().rewrite();
        ws.trySetString("FSTS", fileSet.getPohf().getFileStatus());
        if (fileSet.getPohf().isInvalidKey()) {
            /* CONTINUE */
        }
    }

    /** COBOL paragraph: TERM-010 */
    private void closeAllFiles() {
        fileSet.getPohf().close();
        ws.trySetString("FSTS", fileSet.getPohf().getFileStatus());
        fileSet.getPodf().close();
        ws.trySetString("FSTS", fileSet.getPodf().getFileStatus());
        fileSet.getRcvhf().close();
        ws.trySetString("FSTS", fileSet.getRcvhf().getFileStatus());
        fileSet.getRcvdf().close();
        ws.trySetString("FSTS", fileSet.getRcvdf().getFileStatus());
        fileSet.getStokf().close();
        ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
        fileSet.getSmovf().close();
        ws.trySetString("FSTS", fileSet.getSmovf().getFileStatus());
        fileSet.getSuppf().close();
        ws.trySetString("FSTS", fileSet.getSuppf().getFileStatus());
        fileSet.getProdf().close();
        ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
    }

    /** COBOL paragraph: ABEND-010 */
    private void abortOnFileOpenError() {
        ws.setKaProgid("RC0010");
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
     * Opens a file for I/O, recreating it via an OUTPUT pass if it does not yet exist, then aborts
     * the program on any other failure.
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
            runChain(this::abortOnFileOpenError);
        }
    }

    /**
     * Reads a file by key, falling back to the screen-bound key field then the current record's key
     * when the working-storage value is blank.
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
