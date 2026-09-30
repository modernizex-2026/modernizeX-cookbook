package com.sakura.iv0050.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.iv0050.domain.Iv0050FieldAccess;
import com.sakura.iv0050.domain.WorkingStorage;
import com.sakura.iv0050.runtime.Iv0050Datasets;
import com.sakura.iv0050.screen.ScreenDefs;
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

/** Business logic service generated from COBOL program IV0050. */
@Service
@Scope("prototype")
public class Iv0050Service extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final Iv0050Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL NUMGEN. */
    private NumgenService numgenService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Iv0050FieldAccess ws;

    public Iv0050Service(
            Iv0050Datasets fileSet,
            DateutService dateutService,
            NumgenService numgenService,
            AbortxService abortxService,
            ScreenRendererInstance renderer) {
        this.fileSet = fileSet;
        this.ws = new Iv0050FieldAccess(new WorkingStorage(), fileSet);
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
            runChain(this::processMainScreenCycle);
        }
        runChain(this::closeProgramFiles);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeProgram() {
        ws.setWkProgid("IV0050");
        ws.setWkTitle("Inter-Warehouse Transfer");
        ws.setWkFkeyLine("ENTER=Read  PF3=End  PF4=Clear");
        ws.setKdFunc("TODY");
        dateut(ws.getKdate());
        ws.setWkSysymd(ws.getKdDate1());
        ws.setWkSysdate(ws.getKdDate1());
        runChain(this::openProgramFiles);
    }

    /** COBOL paragraph: OPENF-010 */
    private void openProgramFiles() {
        openWithCreateRetry(fileSet.getStokf(), FileOpenMode.IO);
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("STOKF");
            runChain(this::abortOnFileOpenError);
        }
        openWithCreateRetry(fileSet.getSmovf(), FileOpenMode.IO);
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("SMOVF");
            runChain(this::abortOnFileOpenError);
        }
        openWithCreateRetry(fileSet.getProdf(), FileOpenMode.INPUT);
        openWithCreateRetry(fileSet.getWhsef(), FileOpenMode.INPUT);
    }

    /** COBOL paragraph: MAINR-010 */
    private void processMainScreenCycle() {
        runChain(this::acceptTransferKeyFields);
        switch (String.valueOf(ws.getEsts())) {
            case "03" -> {
                ws.setString("END-FLG", "1");
            }
            case "00" -> {
                runChain(this::processTransferKey);
            }
            default -> {
                ws.setWkMsgLine("Invalid function key");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: GKEY-010 */
    private void acceptTransferKeyFields() {
        runChain(this::clearTransferWorkFields);
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEADER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-FOOTER"), ws);
        ws.setWkMsgLine("Enter product, from/to warehouse and quantity");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-KEY"), ws);
        String scValWkKeyProd0 =
                Utility.acceptScreen(
                        "WK-KEY-PROD",
                        () -> renderer.acceptField(ScreenDefs.getInput("WK-KEY-PROD")));
        ws.setWkKeyProd(Utility.parseIntOr(scValWkKeyProd0.trim(), 0));
        String scValWkFromWhse1 =
                Utility.acceptScreen(
                        "WK-FROM-WHSE",
                        () -> renderer.acceptField(ScreenDefs.getInput("WK-FROM-WHSE")));
        ws.setWkFromWhse(Utility.parseIntOr(scValWkFromWhse1.trim(), 0));
        String scValWkToWhse2 =
                Utility.acceptScreen(
                        "WK-TO-WHSE",
                        () -> renderer.acceptField(ScreenDefs.getInput("WK-TO-WHSE")));
        ws.setWkToWhse(Utility.parseIntOr(scValWkToWhse2.trim(), 0));
        String scValWkTrQty3 =
                Utility.acceptScreen(
                        "WK-TR-QTY", () -> renderer.acceptField(ScreenDefs.getInput("WK-TR-QTY")));
        ws.setWkTrQty(Utility.parseIntOr(scValWkTrQty3.trim(), 0));
        broadcastEstsStatus();
    }

    /** COBOL paragraph: CLRW-010 */
    private void clearTransferWorkFields() {
        fileSet.getStokf().setRecord();
        ws.setWkKeyProd(0);
        ws.setWkFromWhse(0);
        ws.setWkToWhse(0);
        ws.setWkTrQty(0);
        ws.setWkAvail(0);
        ws.setWkSrcOnhand(0);
        ws.setWkSrcAlloc(0);
        ws.setWkDstOnhand(0);
        ws.setWkSrcNew(0);
        ws.setWkDstNew(0);
        ws.setWkUnitCost(BigDecimal.ZERO);
        ws.setWkStkMng(0);
        ws.setWkPrName(" ");
        ws.setWkConfirm(" ");
        ws.setWkFromName(" ");
        ws.setWkToName(" ");
    }

    /** COBOL paragraph: PKEY-010 */
    private void processTransferKey() {
        runChain(this::validateTransferInput);
        if ((ws.getErrFlg() == 1)) {
            return;
        }
        runChain(this::loadAndValidateSourceStock);
        if ((ws.getErrFlg() == 1)) {
            return;
        }
        runChain(this::loadDestinationStockOnHand);
        ws.setWkSrcNew((ws.getWkSrcOnhand() - ws.getWkTrQty()));
        ws.setWkDstNew((ws.getWkDstOnhand() + ws.getWkTrQty()));
        runChain(this::confirmAndProcessTransfer);
    }

    /** COBOL paragraph: VIN-010 */
    private void validateTransferInput() {
        ws.setErrFlg(0);
        if (!validateProductCodeEntered()) {
            displayValidationErrorIfAny();
            return;
        }
        if (!loadProduct()) {
            displayValidationErrorIfAny();
            return;
        }
        if (!validateWarehouseSelection()) {
            displayValidationErrorIfAny();
            return;
        }
        String fromName =
                validateWarehouse(
                        ws.getWkFromWhse(),
                        "From warehouse not found",
                        "From warehouse is deleted");
        if (fromName == null) {
            displayValidationErrorIfAny();
            return;
        }
        ws.setWkFromName(fromName);
        String toName =
                validateWarehouse(
                        ws.getWkToWhse(), "To warehouse not found", "To warehouse is deleted");
        if (toName == null) {
            displayValidationErrorIfAny();
            return;
        }
        ws.setWkToName(toName);
        if (ws.getWkTrQty() <= 0) {
            ws.setWkMsgLine("Transfer quantity must be positive");
            ws.setString("ERR-FLG", "1");
        }
        displayValidationErrorIfAny();
    }

    /** COBOL paragraph: VIN-999 */
    private void displayValidationErrorIfAny() {
        if ((ws.getErrFlg() == 1)) {
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
        /* CONTINUE */
    }

    /** COBOL paragraph: RSS-010 */
    private void loadAndValidateSourceStock() {
        ws.setErrFlg(0);
        ws.setSkProd(ws.getWkKeyProd());
        ws.setSkWhse(ws.getWkFromWhse());
        readStockByProdWhse();
        if (fileSet.getStokf().isInvalidKey()) {
            ws.setWkMsgLine("No stock at source warehouse");
            ws.setString("ERR-FLG", "1");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setWkSrcOnhand(ws.getSkOnhand().intValue());
        ws.setWkSrcAlloc(ws.getSkAllocated().intValue());
        ws.setWkUnitCost(ws.getSkAvgCost());
        ws.setWkAvail(ws.getSkOnhand().subtract(ws.getSkAllocated()).intValue());
        if (ws.getWkTrQty() > ws.getWkAvail()) {
            ws.setWkMsgLine("Quantity exceeds available at source");
            ws.setString("ERR-FLG", "1");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
    }

    /** COBOL paragraph: RDS-010 */
    private void loadDestinationStockOnHand() {
        ws.setWkDstOnhand(0);
        ws.setSkProd(ws.getWkKeyProd());
        ws.setSkWhse(ws.getWkToWhse());
        readStockByProdWhse();
        if (fileSet.getStokf().isInvalidKey()) {
            ws.setWkDstOnhand(0);
        }
        if (!fileSet.getStokf().isInvalidKey()) {
            ws.setWkDstOnhand(ws.getSkOnhand().intValue());
        }
    }

    /** COBOL paragraph: CFT-010 */
    private void confirmAndProcessTransfer() {
        ws.setWkConfirm(" ");
        renderer.displayScreen(ScreenDefs.getScreen("DS-SHOW"), ws);
        ws.setWkMsgLine("Confirm the transfer (Y/N)");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-CONFIRM"), ws);
        String scValWkConfirm9 =
                Utility.acceptScreen(
                        "SC-CONF", () -> renderer.acceptField(ScreenDefs.getInput("SC-CONF")));
        ws.setWkConfirm(scValWkConfirm9);
        broadcastEstsStatus();
        if ((ws.getWkConfirm().equals("Y") || ws.getWkConfirm().equals("y"))) {
            runChain(this::executeStockTransfer);
        } else {
            ws.setWkMsgLine("Transfer cancelled");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
    }

    /** COBOL paragraph: PT-010 */
    private void executeStockTransfer() {
        runChain(this::decrementSourceStock);
        if ((ws.getErrFlg() == 1)) {
            return;
        }
        ws.setWkMvWhse(ws.getWkFromWhse());
        ws.setWkMvQty((0 - ws.getWkTrQty()));
        ws.setWkMvBal(ws.getWkSrcNew());
        runChain(this::writeStockMovementRecord);
        runChain(this::incrementDestinationStock);
        ws.setWkMvWhse(ws.getWkToWhse());
        ws.setWkMvQty(ws.getWkTrQty());
        ws.setWkMvBal(ws.getWkDstNew());
        runChain(this::writeStockMovementRecord);
        runChain(this::displayTransferResult);
    }

    /** COBOL paragraph: DSR-010 */
    private void decrementSourceStock() {
        ws.setErrFlg(0);
        ws.setSkProd(ws.getWkKeyProd());
        ws.setSkWhse(ws.getWkFromWhse());
        readStockByProdWhse();
        if (fileSet.getStokf().isInvalidKey()) {
            ws.setWkMsgLine("Source stock disappeared");
            ws.setString("ERR-FLG", "1");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setSkOnhand(ws.getSkOnhand().subtract(BigDecimal.valueOf(ws.getWkTrQty())));
        ws.setSkYtdOut(ws.getSkYtdOut().add(BigDecimal.valueOf(ws.getWkTrQty())));
        ws.setSkLastOutDate(ws.getWkSysdate());
        ws.setWkSrcNew(ws.getSkOnhand().intValue());
        ws.setWkUnitCost(ws.getSkAvgCost());
        fileSet.getStokf().rewrite();
        ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
        if (fileSet.getStokf().isInvalidKey()) {
            ws.setWkMsgLine("Source stock update failed");
            ws.setString("ERR-FLG", "1");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
    }

    /** COBOL paragraph: IND-010 */
    private void incrementDestinationStock() {
        ws.setSkProd(ws.getWkKeyProd());
        ws.setSkWhse(ws.getWkToWhse());
        readStockByProdWhse();
        if (fileSet.getStokf().isInvalidKey()) {
            runChain(this::createDestinationStockRecord);
            return;
        }
        ws.setSkOnhand(ws.getSkOnhand().add(BigDecimal.valueOf(ws.getWkTrQty())));
        ws.setSkYtdIn(ws.getSkYtdIn().add(BigDecimal.valueOf(ws.getWkTrQty())));
        ws.setSkLastInDate(ws.getWkSysdate());
        ws.setWkDstNew(ws.getSkOnhand().intValue());
        fileSet.getStokf().rewrite();
        ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
        if (fileSet.getStokf().isInvalidKey()) {
            ws.setWkMsgLine("Dest stock update failed");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
    }

    /** COBOL paragraph: CRD-010 */
    private void createDestinationStockRecord() {
        fileSet.getStokf().setRecord();
        ws.setSkProd(ws.getWkKeyProd());
        ws.setSkWhse(ws.getWkToWhse());
        ws.setSkOnhand(BigDecimal.valueOf(ws.getWkTrQty()));
        ws.setSkAllocated(BigDecimal.ZERO);
        ws.setSkOnOrder(BigDecimal.ZERO);
        ws.setSkAvgCost(ws.getWkUnitCost());
        ws.setSkLastInDate(ws.getWkSysdate());
        ws.setSkYtdIn(BigDecimal.valueOf(ws.getWkTrQty()));
        ws.setWkDstNew(ws.getWkTrQty());
        fileSet.getStokf().write();
        ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
        if (fileSet.getStokf().isInvalidKey()) {
            ws.setWkMsgLine("Dest stock create failed");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
    }

    /** COBOL paragraph: WMV-010 */
    private void writeStockMovementRecord() {
        ws.setKnumKey("STKMOV");
        numgen(ws.getKnum());
        if (!Utility.fieldEquals(ws.getKnumStatus(), "00")) {
            ws.setWkMsgLine("Movement number assignment failed");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        fileSet.getSmovf().setRecord();
        ws.setSmSeq(ws.getKnumNumber());
        ws.setSmDate(ws.getWkSysdate());
        ws.setSmProd(ws.getWkKeyProd());
        ws.setSmWhse(ws.getWkMvWhse());
        ws.setSmKind(40);
        ws.setSmQty(BigDecimal.valueOf(ws.getWkMvQty()));
        ws.setSmUnitCost(ws.getWkUnitCost());
        ws.setSmBalAfter(BigDecimal.valueOf(ws.getWkMvBal()));
        ws.setSmRefType(40);
        ws.setSmRefNo(ws.getKnumNumber());
        ws.setSmUser(ws.getWkUserCode());
        fileSet.getSmovf().write();
        ws.trySetString("FSTS", fileSet.getSmovf().getFileStatus());
        if (fileSet.getSmovf().isInvalidKey()) {
            ws.setWkMsgLine("Movement write failed");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
    }

    /** COBOL paragraph: SHR-010 */
    private void displayTransferResult() {
        renderer.displayScreen(ScreenDefs.getScreen("DS-SHOW"), ws);
        ws.setWkEdQty(ws.getWkTrQty());
        ws.setWkMsgLine(
                "Transferred " + String.format("%012d", (long) ws.getWkEdQty()) + " units - done");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
    }

    /** COBOL paragraph: TERM-010 */
    private void closeProgramFiles() {
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
    private void abortOnFileOpenError() {
        ws.setKaProgid("IV0050");
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

    /** Validate that a product code was entered before loading the product master. */
    private boolean validateProductCodeEntered() {
        if (ws.getWkKeyProd() == 0) {
            ws.setWkMsgLine("Product code must not be zero");
            ws.setString("ERR-FLG", "1");
            return false;
        }
        return true;
    }

    /** Read the product master by code and confirm it is active and stock-managed. */
    private boolean loadProduct() {
        ws.setPrCode(ws.getWkKeyProd());
        String rkVal_4 = "";
        if (rkVal_4 == null || rkVal_4.trim().isEmpty()) {
            try {
                rkVal_4 = ws.getString("PR-CODE");
            } catch (Exception _e) {
            }
        }
        if (rkVal_4 == null || rkVal_4.trim().isEmpty()) {
            try {
                rkVal_4 = fileSet.getProdf().extractKeyFromCurrentRecord();
            } catch (Exception _e3) {
            }
        }
        fileSet.getProdf().readByKey(rkVal_4 != null ? rkVal_4.trim() : "");
        ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
        if (fileSet.getProdf().isInvalidKey()) {
            ws.setWkMsgLine("Product not found");
            ws.setString("ERR-FLG", "1");
            return false;
        }
        if (ws.getPrDelFlag() == 1) {
            ws.setWkMsgLine("Product is deleted");
            ws.setString("ERR-FLG", "1");
            return false;
        }
        ws.setWkPrName(ws.getPrName());
        ws.setWkStkMng(ws.getPrStockMng());
        if (ws.getWkStkMng() == 0) {
            ws.setWkMsgLine("Product is not stock-managed");
            ws.setString("ERR-FLG", "1");
            return false;
        }
        return true;
    }

    /** Validate that both warehouses were entered and are not the same. */
    private boolean validateWarehouseSelection() {
        if (ws.getWkFromWhse() == 0 || ws.getWkToWhse() == 0) {
            ws.setWkMsgLine("Both warehouses are required");
            ws.setString("ERR-FLG", "1");
            return false;
        }
        if (ws.getWkFromWhse() == ws.getWkToWhse()) {
            ws.setWkMsgLine("From and to warehouse must differ");
            ws.setString("ERR-FLG", "1");
            return false;
        }
        return true;
    }

    /** Read the warehouse master by code and return its name, or null with an error message set. */
    private String validateWarehouse(int whCode, String notFoundMsg, String deletedMsg) {
        ws.setWhCode(whCode);
        String rkVal = "";
        if (rkVal == null || rkVal.trim().isEmpty()) {
            try {
                rkVal = ws.getString("WH-CODE");
            } catch (Exception _e) {
            }
        }
        if (rkVal == null || rkVal.trim().isEmpty()) {
            try {
                rkVal = fileSet.getWhsef().extractKeyFromCurrentRecord();
            } catch (Exception _e3) {
            }
        }
        fileSet.getWhsef().readByKey(rkVal != null ? rkVal.trim() : "");
        ws.trySetString("FSTS", fileSet.getWhsef().getFileStatus());
        if (fileSet.getWhsef().isInvalidKey()) {
            ws.setWkMsgLine(notFoundMsg);
            ws.setString("ERR-FLG", "1");
            return null;
        }
        if (ws.getWhDelFlag() == 1) {
            ws.setWkMsgLine(deletedMsg);
            ws.setString("ERR-FLG", "1");
            return null;
        }
        return ws.getWhName();
    }

    /** Read STOKF by the product/warehouse composite key already set in SK-PROD / SK-WHSE. */
    private void readStockByProdWhse() {
        StringBuilder rkSb = new StringBuilder();
        String rkPart0 = "";
        try {
            rkPart0 = ws.getString("SK-PROD");
        } catch (Exception _e) {
        }
        rkSb.append(rkPart0 != null ? rkPart0.trim() : "");
        String rkPart1 = "";
        try {
            rkPart1 = ws.getString("SK-WHSE");
        } catch (Exception _e) {
        }
        rkSb.append('|');
        rkSb.append(rkPart1 != null ? rkPart1.trim() : "");
        fileSet.getStokf().readByKey(rkSb.toString());
        ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
    }

    /** Open a file for I/O, creating it first via an OUTPUT open/close if it does not yet exist. */
    private void openWithCreateRetry(RawDatasetBase file, FileOpenMode mode) {
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
}
