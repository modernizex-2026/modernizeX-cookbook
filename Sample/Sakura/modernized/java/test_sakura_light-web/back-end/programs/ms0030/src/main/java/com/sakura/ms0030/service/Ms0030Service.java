package com.sakura.ms0030.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.ms0030.domain.Ms0030FieldAccess;
import com.sakura.ms0030.domain.WorkingStorage;
import com.sakura.ms0030.runtime.Ms0030Datasets;
import com.sakura.ms0030.screen.ScreenDefs;
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

/** Business logic service generated from COBOL program MS0030. */
@Service
@Scope("prototype")
public class Ms0030Service extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final Ms0030Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Ms0030FieldAccess ws;

    public Ms0030Service(
            Ms0030Datasets fileSet,
            DateutService dateutService,
            AbortxService abortxService,
            ScreenRendererInstance renderer) {
        this.fileSet = fileSet;
        this.ws = new Ms0030FieldAccess(new WorkingStorage(), fileSet);
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
            runChain(this::displayMainScreenAndDispatch);
        }
        runChain(this::closeAllFiles);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeProgram() {
        ws.setWkProgid("MS0030");
        ws.setWkTitle("Product Master Maintenance");
        ws.setWkFkeyLine("ENTER=Read  PF9=Delete  PF3=End");
        ws.setKdFunc("TODY");
        callDateutForCurrentDate(ws.getKdate());
        ws.setWkSysymd(ws.getKdDate1());
        ws.setWkSysdate(ws.getKdDate1());
        fileSet.getProdf().open(FileOpenMode.IO);
        ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
        if (Utility.fieldEquals(ws.getFsts(), "35") || Utility.fieldEquals(ws.getFsts(), "30")) {
            fileSet.getProdf().open(FileOpenMode.OUTPUT);
            ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
            fileSet.getProdf().close();
            ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
            fileSet.getProdf().open(FileOpenMode.IO);
            ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
        }
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("PRODF");
            runChain(this::abortWithFileOpenError);
        }
        fileSet.getCatgf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getCatgf().getFileStatus());
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("CATGF");
            runChain(this::abortWithFileOpenError);
        }
        fileSet.getSuppf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getSuppf().getFileStatus());
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("SUPPF");
            runChain(this::abortWithFileOpenError);
        }
        fileSet.getWhsef().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getWhsef().getFileStatus());
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("WHSEF");
            runChain(this::abortWithFileOpenError);
        }
    }

    /** COBOL paragraph: MAIN-RTN-010 */
    private void displayMainScreenAndDispatch() {
        runChain(this::clearScreenFields);
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEADER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-FOOTER"), ws);
        ws.setWkMsgLine(" ");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-KEY"), ws);
        String scValPrCode0 =
                Utility.acceptScreen(
                        "SC-KEY", () -> renderer.acceptField(ScreenDefs.getInput("SC-KEY")));
        ws.setPrCode(Utility.parseIntOr(scValPrCode0.trim(), 0));
        broadcastEstsStatus();
        switch (String.valueOf(ws.getEsts())) {
            case "03" -> {
                ws.setString("END-FLG", "1");
            }
            case "00" -> {
                runChain(this::processProductKeyEntry);
            }
            default -> {
                ws.setWkMsgLine("Invalid function key");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: CLEAR-010 */
    private void clearScreenFields() {
        fileSet.getProdf().setRecord();
        ws.setWkCatgName(" ");
        ws.setWkSuppName(" ");
        ws.setWkWhseName(" ");
        ws.setWkConfirm(" ");
    }

    /** COBOL paragraph: PKEY-010 */
    private void processProductKeyEntry() {
        if (ws.getPrCode() == 0) {
            ws.setWkMsgLine("Product code must not be zero");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setWkSaveCode(ws.getPrCode());
        fileSet.getProdf().readByKey(resolveReadKey("PR-CODE", fileSet.getProdf()));
        ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
        if (fileSet.getProdf().isInvalidKey()) {
            runChain(this::initializeNewProductDefaults);
        }
        if (!fileSet.getProdf().isInvalidKey()) {
            runChain(this::prepareExistingProductChange);
        }
        runChain(this::displayEditScreenAndDispatch);
    }

    /** COBOL paragraph: SADD-010 */
    private void initializeNewProductDefaults() {
        ws.setString("MODE-FLG", "1");
        fileSet.getProdf().setRecord();
        ws.setPrCode(ws.getWkSaveCode());
        ws.setPrTaxCategory(1);
        ws.setPrStockMng(1);
        ws.setWkMsgLine("New product - enter details");
    }

    /** COBOL paragraph: SCHG-010 */
    private void prepareExistingProductChange() {
        if (ws.getPrDelFlag() == 1) {
            ws.setString("MODE-FLG", "1");
            ws.setWkMsgLine("Deleted product - re-registering");
        } else {
            ws.setString("MODE-FLG", "2");
            ws.setWkMsgLine("Existing product - change or PF9 delete");
        }
        runChain(this::refreshRelatedNameFields);
    }

    /** COBOL paragraph: EDIT-010 */
    private void displayEditScreenAndDispatch() {
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-BODY"), ws);
        acceptProductDetailFields();
        broadcastEstsStatus();
        switch (String.valueOf(ws.getEsts())) {
            case "03" -> {
                /* CONTINUE */
            }
            case "04" -> {
                ws.setWkMsgLine("Cancelled");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
            case "09" -> {
                if ((ws.getModeFlg() == 2)) {
                    runChain(this::confirmAndDeleteProduct);
                } else {
                    ws.setWkMsgLine("Nothing to delete");
                    renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
                }
            }
            case "00" -> {
                runChain(this::validateProductFields);
                if ((ws.getErrFlg() != 1)) {
                    runChain(this::saveProductRecord);
                }
            }
            default -> {
                ws.setWkMsgLine("Invalid function key");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** Accept all product detail fields entered on the edit screen into working storage. */
    private void acceptProductDetailFields() {
        String scValPrName2 =
                Utility.acceptScreen(
                        "PR-NAME", () -> renderer.acceptField(ScreenDefs.getInput("PR-NAME")));
        ws.setPrName(scValPrName2);
        String scValPrKana3 =
                Utility.acceptScreen(
                        "PR-KANA", () -> renderer.acceptField(ScreenDefs.getInput("PR-KANA")));
        ws.setPrKana(scValPrKana3);
        String scValPrSpec4 =
                Utility.acceptScreen(
                        "PR-SPEC", () -> renderer.acceptField(ScreenDefs.getInput("PR-SPEC")));
        ws.setPrSpec(scValPrSpec4);
        String scValPrCategory5 =
                Utility.acceptScreen(
                        "PR-CATEGORY",
                        () -> renderer.acceptField(ScreenDefs.getInput("PR-CATEGORY")));
        ws.setPrCategory(Utility.parseIntOr(scValPrCategory5.trim(), 0));
        String scValPrBarcode6 =
                Utility.acceptScreen(
                        "PR-BARCODE",
                        () -> renderer.acceptField(ScreenDefs.getInput("PR-BARCODE")));
        ws.setPrBarcode(scValPrBarcode6);
        String scValPrUnit7 =
                Utility.acceptScreen(
                        "PR-UNIT", () -> renderer.acceptField(ScreenDefs.getInput("PR-UNIT")));
        ws.setPrUnit(scValPrUnit7);
        String scValPrStdCost8 =
                Utility.acceptScreen(
                        "PR-STD-COST",
                        () -> renderer.acceptField(ScreenDefs.getInput("PR-STD-COST")));
        try {
            ws.setPrStdCost(new BigDecimal(scValPrStdCost8.trim()));
        } catch (NumberFormatException _nfe) {
            ws.setPrStdCost(BigDecimal.ZERO);
        }
        String scValPrListPrice9 =
                Utility.acceptScreen(
                        "PR-LIST-PRICE",
                        () -> renderer.acceptField(ScreenDefs.getInput("PR-LIST-PRICE")));
        try {
            ws.setPrListPrice(new BigDecimal(scValPrListPrice9.trim()));
        } catch (NumberFormatException _nfe) {
            ws.setPrListPrice(BigDecimal.ZERO);
        }
        String scValPrLastCost10 =
                Utility.acceptScreen(
                        "PR-LAST-COST",
                        () -> renderer.acceptField(ScreenDefs.getInput("PR-LAST-COST")));
        try {
            ws.setPrLastCost(new BigDecimal(scValPrLastCost10.trim()));
        } catch (NumberFormatException _nfe) {
            ws.setPrLastCost(BigDecimal.ZERO);
        }
        String scValPrTaxCategory11 =
                Utility.acceptScreen(
                        "PR-TAX-CATEGORY",
                        () -> renderer.acceptField(ScreenDefs.getInput("PR-TAX-CATEGORY")));
        ws.setPrTaxCategory(Utility.parseIntOr(scValPrTaxCategory11.trim(), 0));
        String scValPrRankPrice112 =
                Utility.acceptScreen(
                        "PR-RANK-PRICE(1)",
                        () -> renderer.acceptField(ScreenDefs.getInput("PR-RANK-PRICE(1)")));
        try {
            ws.setPrRankPrice(1, new BigDecimal(scValPrRankPrice112.trim()));
        } catch (NumberFormatException _nfe) {
            ws.setPrRankPrice(1, BigDecimal.ZERO);
        }
        String scValPrRankPrice213 =
                Utility.acceptScreen(
                        "PR-RANK-PRICE(2)",
                        () -> renderer.acceptField(ScreenDefs.getInput("PR-RANK-PRICE(2)")));
        try {
            ws.setPrRankPrice(2, new BigDecimal(scValPrRankPrice213.trim()));
        } catch (NumberFormatException _nfe) {
            ws.setPrRankPrice(2, BigDecimal.ZERO);
        }
        String scValPrRankPrice314 =
                Utility.acceptScreen(
                        "PR-RANK-PRICE(3)",
                        () -> renderer.acceptField(ScreenDefs.getInput("PR-RANK-PRICE(3)")));
        try {
            ws.setPrRankPrice(3, new BigDecimal(scValPrRankPrice314.trim()));
        } catch (NumberFormatException _nfe) {
            ws.setPrRankPrice(3, BigDecimal.ZERO);
        }
        String scValPrRankPrice415 =
                Utility.acceptScreen(
                        "PR-RANK-PRICE(4)",
                        () -> renderer.acceptField(ScreenDefs.getInput("PR-RANK-PRICE(4)")));
        try {
            ws.setPrRankPrice(4, new BigDecimal(scValPrRankPrice415.trim()));
        } catch (NumberFormatException _nfe) {
            ws.setPrRankPrice(4, BigDecimal.ZERO);
        }
        String scValPrRankPrice516 =
                Utility.acceptScreen(
                        "PR-RANK-PRICE(5)",
                        () -> renderer.acceptField(ScreenDefs.getInput("PR-RANK-PRICE(5)")));
        try {
            ws.setPrRankPrice(5, new BigDecimal(scValPrRankPrice516.trim()));
        } catch (NumberFormatException _nfe) {
            ws.setPrRankPrice(5, BigDecimal.ZERO);
        }
        String scValPrSafetyStock17 =
                Utility.acceptScreen(
                        "PR-SAFETY-STOCK",
                        () -> renderer.acceptField(ScreenDefs.getInput("PR-SAFETY-STOCK")));
        try {
            ws.setPrSafetyStock(new BigDecimal(scValPrSafetyStock17.trim()));
        } catch (NumberFormatException _nfe) {
            ws.setPrSafetyStock(BigDecimal.ZERO);
        }
        String scValPrReorderPoint18 =
                Utility.acceptScreen(
                        "PR-REORDER-POINT",
                        () -> renderer.acceptField(ScreenDefs.getInput("PR-REORDER-POINT")));
        try {
            ws.setPrReorderPoint(new BigDecimal(scValPrReorderPoint18.trim()));
        } catch (NumberFormatException _nfe) {
            ws.setPrReorderPoint(BigDecimal.ZERO);
        }
        String scValPrReorderQty19 =
                Utility.acceptScreen(
                        "PR-REORDER-QTY",
                        () -> renderer.acceptField(ScreenDefs.getInput("PR-REORDER-QTY")));
        try {
            ws.setPrReorderQty(new BigDecimal(scValPrReorderQty19.trim()));
        } catch (NumberFormatException _nfe) {
            ws.setPrReorderQty(BigDecimal.ZERO);
        }
        String scValPrLeadDays20 =
                Utility.acceptScreen(
                        "PR-LEAD-DAYS",
                        () -> renderer.acceptField(ScreenDefs.getInput("PR-LEAD-DAYS")));
        ws.setPrLeadDays(Utility.parseIntOr(scValPrLeadDays20.trim(), 0));
        String scValPrDfltSupp21 =
                Utility.acceptScreen(
                        "PR-DFLT-SUPP",
                        () -> renderer.acceptField(ScreenDefs.getInput("PR-DFLT-SUPP")));
        ws.setPrDfltSupp(Utility.parseIntOr(scValPrDfltSupp21.trim(), 0));
        String scValPrDfltWhse22 =
                Utility.acceptScreen(
                        "PR-DFLT-WHSE",
                        () -> renderer.acceptField(ScreenDefs.getInput("PR-DFLT-WHSE")));
        ws.setPrDfltWhse(Utility.parseIntOr(scValPrDfltWhse22.trim(), 0));
        String scValPrStockMng23 =
                Utility.acceptScreen(
                        "PR-STOCK-MNG",
                        () -> renderer.acceptField(ScreenDefs.getInput("PR-STOCK-MNG")));
        ws.setPrStockMng(Utility.parseIntOr(scValPrStockMng23.trim(), 0));
    }

    /** COBOL paragraph: VAL-010 */
    private void validateProductFields() {
        ws.setErrFlg(0);
        if (Utility.fieldEquals(ws.getPrName(), " ")) {
            ws.setWkMsgLine("Name is required");
            ws.setString("ERR-FLG", "1");
            displayValidationError();
            return;
        }
        if (ws.getPrCategory() == 0) {
            ws.setWkMsgLine("Category is required");
            ws.setString("ERR-FLG", "1");
            displayValidationError();
            return;
        }
        ws.setCtCode(ws.getPrCategory());
        fileSet.getCatgf().readByKey(resolveReadKey("CT-CODE", fileSet.getCatgf()));
        ws.trySetString("FSTS", fileSet.getCatgf().getFileStatus());
        if (fileSet.getCatgf().isInvalidKey()) {
            ws.setWkMsgLine("Category code not found");
            ws.setString("ERR-FLG", "1");
            displayValidationError();
            return;
        }
        if (!fileSet.getCatgf().isInvalidKey()) {
            ws.setWkCatgName(ws.getCtName());
        }
        if (ws.getPrTaxCategory() < 1 || ws.getPrTaxCategory() > 3) {
            ws.setWkMsgLine("Tax category must be 1-3");
            ws.setString("ERR-FLG", "1");
            displayValidationError();
            return;
        }
        if (ws.getPrStockMng() > 1) {
            ws.setWkMsgLine("Stock mgmt must be 0 or 1");
            ws.setString("ERR-FLG", "1");
            displayValidationError();
            return;
        }
        if ((ws.getPrStdCost().signum() < 0) || (ws.getPrLastCost().signum() < 0)) {
            ws.setWkMsgLine("Cost cannot be negative");
            ws.setString("ERR-FLG", "1");
            displayValidationError();
            return;
        }
        if ((ws.getPrListPrice().signum() < 0)) {
            ws.setWkMsgLine("List price cannot be negative");
            ws.setString("ERR-FLG", "1");
            displayValidationError();
            return;
        }
        for (ws.setWkIdx(1); ws.getWkIdx() <= 5; ws.setWkIdx(ws.getWkIdx() + 1)) {
            if ((ws.getPrRankPrice(ws.getWkIdx()).signum() < 0)) {
                ws.setWkMsgLine("Rank price cannot be negative");
                ws.setString("ERR-FLG", "1");
            }
        }
        if ((ws.getErrFlg() == 1)) {
            displayValidationError();
            return;
        }
        if ((ws.getPrSafetyStock().signum() < 0) || (ws.getPrReorderPoint().signum() < 0)) {
            ws.setWkMsgLine("Stock levels cannot be negative");
            ws.setString("ERR-FLG", "1");
            displayValidationError();
            return;
        }
        if ((ws.getPrReorderQty().signum() < 0)) {
            ws.setWkMsgLine("Reorder qty cannot be negative");
            ws.setString("ERR-FLG", "1");
            displayValidationError();
            return;
        }
        runChain(this::validateDefaultSupplier);
        if ((ws.getErrFlg() == 1)) {
            displayValidationError();
            return;
        }
        runChain(this::validateDefaultWarehouse);
        // fall-through to next paragraph
        displayValidationError();
    }

    /** COBOL paragraph: VAL-999 */
    private void displayValidationError() {
        if ((ws.getErrFlg() == 1)) {
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
        /* CONTINUE */
    }

    /** COBOL paragraph: CHKS-010 */
    private void validateDefaultSupplier() {
        if (ws.getPrDfltSupp() == 0) {
            return;
        }
        ws.setSpCode(ws.getPrDfltSupp());
        fileSet.getSuppf().readByKey(resolveReadKey("SP-CODE", fileSet.getSuppf()));
        ws.trySetString("FSTS", fileSet.getSuppf().getFileStatus());
        if (fileSet.getSuppf().isInvalidKey()) {
            ws.setWkMsgLine("Default supplier not found");
            ws.setString("ERR-FLG", "1");
        }
        if (!fileSet.getSuppf().isInvalidKey()) {
            ws.setWkSuppName(ws.getSpName());
        }
    }

    /** COBOL paragraph: CHKW-010 */
    private void validateDefaultWarehouse() {
        if (ws.getPrDfltWhse() == 0) {
            return;
        }
        ws.setWhCode(ws.getPrDfltWhse());
        fileSet.getWhsef().readByKey(resolveReadKey("WH-CODE", fileSet.getWhsef()));
        ws.trySetString("FSTS", fileSet.getWhsef().getFileStatus());
        if (fileSet.getWhsef().isInvalidKey()) {
            ws.setWkMsgLine("Default warehouse not found");
            ws.setString("ERR-FLG", "1");
        }
        if (!fileSet.getWhsef().isInvalidKey()) {
            ws.setWkWhseName(ws.getWhName());
        }
    }

    /** COBOL paragraph: SAVE-010 */
    private void saveProductRecord() {
        ws.setPrUpdDate(ws.getWkSysdate());
        ws.setPrUpdUser(ws.getWkUserCode());
        ws.setPrDelFlag(0);
        if ((ws.getModeFlg() == 1)) {
            ws.setPrAddDate(ws.getWkSysdate());
            ws.setPrAddUser(ws.getWkUserCode());
            fileSet.getProdf().write();
            ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
            if (fileSet.getProdf().isInvalidKey()) {
                ws.setWkMsgLine("Write failed - duplicate");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
            if (!fileSet.getProdf().isInvalidKey()) {
                ws.setWkMsgLine("Product added");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        } else {
            fileSet.getProdf().rewrite();
            ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
            if (fileSet.getProdf().isInvalidKey()) {
                ws.setWkMsgLine("Update failed");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
            if (!fileSet.getProdf().isInvalidKey()) {
                ws.setWkMsgLine("Product updated");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: DEL-010 */
    private void confirmAndDeleteProduct() {
        ws.setWkConfirm(" ");
        ws.setWkMsgLine("Press Y then ENTER to delete");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-CONFIRM"), ws);
        String scValWkConfirm27 =
                Utility.acceptScreen(
                        "SC-CONF", () -> renderer.acceptField(ScreenDefs.getInput("SC-CONF")));
        ws.setWkConfirm(scValWkConfirm27);
        broadcastEstsStatus();
        if ((ws.getWkConfirm().equals("Y") || ws.getWkConfirm().equals("y"))) {
            ws.setPrDelFlag(1);
            ws.setPrUpdDate(ws.getWkSysdate());
            ws.setPrUpdUser(ws.getWkUserCode());
            fileSet.getProdf().rewrite();
            ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
            if (fileSet.getProdf().isInvalidKey()) {
                ws.setWkMsgLine("Delete failed");
            }
            if (!fileSet.getProdf().isInvalidKey()) {
                ws.setWkMsgLine("Product deleted");
            }
        } else {
            ws.setWkMsgLine("Delete cancelled");
        }
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
    }

    /** COBOL paragraph: LOOK-010 */
    private void refreshRelatedNameFields() {
        ws.setWkCatgName(" ");
        if (ws.getPrCategory() != 0) {
            ws.setCtCode(ws.getPrCategory());
            fileSet.getCatgf().readByKey(resolveReadKey("CT-CODE", fileSet.getCatgf()));
            ws.trySetString("FSTS", fileSet.getCatgf().getFileStatus());
            if (fileSet.getCatgf().isInvalidKey()) {
                ws.setWkCatgName("??? unknown category");
            }
            if (!fileSet.getCatgf().isInvalidKey()) {
                ws.setWkCatgName(ws.getCtName());
            }
        }
        ws.setWkSuppName(" ");
        if (ws.getPrDfltSupp() != 0) {
            ws.setSpCode(ws.getPrDfltSupp());
            fileSet.getSuppf().readByKey(resolveReadKey("SP-CODE", fileSet.getSuppf()));
            ws.trySetString("FSTS", fileSet.getSuppf().getFileStatus());
            if (fileSet.getSuppf().isInvalidKey()) {
                ws.setWkSuppName("??? unknown supplier");
            }
            if (!fileSet.getSuppf().isInvalidKey()) {
                ws.setWkSuppName(ws.getSpName());
            }
        }
        ws.setWkWhseName(" ");
        if (ws.getPrDfltWhse() != 0) {
            ws.setWhCode(ws.getPrDfltWhse());
            fileSet.getWhsef().readByKey(resolveReadKey("WH-CODE", fileSet.getWhsef()));
            ws.trySetString("FSTS", fileSet.getWhsef().getFileStatus());
            if (fileSet.getWhsef().isInvalidKey()) {
                ws.setWkWhseName("??? unknown warehouse");
            }
            if (!fileSet.getWhsef().isInvalidKey()) {
                ws.setWkWhseName(ws.getWhName());
            }
        }
    }

    /** COBOL paragraph: TERM-010 */
    private void closeAllFiles() {
        fileSet.getProdf().close();
        ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
        fileSet.getCatgf().close();
        ws.trySetString("FSTS", fileSet.getCatgf().getFileStatus());
        fileSet.getSuppf().close();
        ws.trySetString("FSTS", fileSet.getSuppf().getFileStatus());
        fileSet.getWhsef().close();
        ws.trySetString("FSTS", fileSet.getWhsef().getFileStatus());
    }

    /** COBOL paragraph: ABEND-010 */
    private void abortWithFileOpenError() {
        ws.setKaProgid("MS0030");
        ws.setKaFsts(ws.getFsts());
        ws.setKaMsgcode("EOPEN ");
        ws.setKaDetail("File open error");
        callAbortxService(ws.getKabend());
        ws.setCompletionCode(255);
        throw new ProgramExitSignal();
    }

    /** COBOL CALL DATEUT — delegates to injected DateutService. */
    private void callDateutForCurrentDate(Object... args) {
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
    private void callAbortxService(Object... args) {
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
     * Resolve a record key from the current screen value, falling back to the current record's key.
     */
    private String resolveReadKey(String wsKey, RawDatasetBase file) {
        String rkVal = "";
        if (rkVal == null || rkVal.trim().isEmpty()) {
            try {
                rkVal = ws.getString(wsKey);
            } catch (Exception _e) {
            }
        }
        if (rkVal == null || rkVal.trim().isEmpty()) {
            try {
                rkVal = file.extractKeyFromCurrentRecord();
            } catch (Exception _e3) {
            }
        }
        return rkVal != null ? rkVal.trim() : "";
    }
}
