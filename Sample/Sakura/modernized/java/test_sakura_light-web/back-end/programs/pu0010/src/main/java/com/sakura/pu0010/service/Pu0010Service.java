package com.sakura.pu0010.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.numgen.service.NumgenService;
import com.sakura.pu0010.domain.Pu0010FieldAccess;
import com.sakura.pu0010.domain.WorkingStorage;
import com.sakura.pu0010.runtime.Pu0010Datasets;
import com.sakura.pu0010.screen.ScreenDefs;
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
import com.sakura.runtime.linkage.TaxcalLinkParm;
import com.sakura.taxcal.service.TaxcalService;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/** Business logic service generated from COBOL program PU0010. */
@Service
@Scope("prototype")
public class Pu0010Service extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final Pu0010Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL TAXCAL. */
    private TaxcalService taxcalService;

    /** Injected service for COBOL CALL NUMGEN. */
    private NumgenService numgenService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Pu0010FieldAccess ws;

    public Pu0010Service(
            Pu0010Datasets fileSet,
            DateutService dateutService,
            TaxcalService taxcalService,
            NumgenService numgenService,
            AbortxService abortxService,
            ScreenRendererInstance renderer) {
        this.fileSet = fileSet;
        this.ws = new Pu0010FieldAccess(new WorkingStorage(), fileSet);
        this.dateutService = dateutService;
        this.taxcalService = taxcalService;
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
        if (taxcalService instanceof ScreenRendererAware rra) {
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
            runChain(this::processOnePurchaseOrder);
        }
        runChain(this::closeAllFiles);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeProgram() {
        ws.setWkProgid("PU0010");
        ws.setWkTitle("Purchase Order Entry");
        ws.setWkFkeyLine("ENTER=Next  PF3=End/Finish  PF4=Clear line");
        ws.setKdFunc("TODY");
        dateut(ws.getKdate());
        ws.setWkSysymd(ws.getKdDate1());
        ws.setWkSysdate(ws.getKdDate1());
        runChain(this::openAllFiles);
    }

    /** COBOL paragraph: OPENF-010 */
    private void openAllFiles() {
        runChain(this::openPoHeaderFile);
        runChain(this::openPoDetailFile);
        fileSet.getStokf().open(FileOpenMode.IO);
        ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
        if (Utility.fieldEquals(ws.getFsts(), "35") || Utility.fieldEquals(ws.getFsts(), "30")) {
            fileSet.getStokf().open(FileOpenMode.OUTPUT);
            ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
            fileSet.getStokf().close();
            ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
            fileSet.getStokf().open(FileOpenMode.IO);
            ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
        }
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("STOKF");
            runChain(this::abortOnFileOpenError);
        }
        fileSet.getSuppf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getSuppf().getFileStatus());
        fileSet.getProdf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
    }

    /** COBOL paragraph: OIPH-010 */
    private void openPoHeaderFile() {
        fileSet.getPohf().open(FileOpenMode.IO);
        ws.trySetString("FSTS", fileSet.getPohf().getFileStatus());
        if (Utility.fieldEquals(ws.getFsts(), "35") || Utility.fieldEquals(ws.getFsts(), "30")) {
            fileSet.getPohf().open(FileOpenMode.OUTPUT);
            ws.trySetString("FSTS", fileSet.getPohf().getFileStatus());
            fileSet.getPohf().close();
            ws.trySetString("FSTS", fileSet.getPohf().getFileStatus());
            fileSet.getPohf().open(FileOpenMode.IO);
            ws.trySetString("FSTS", fileSet.getPohf().getFileStatus());
        }
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("POHF");
            runChain(this::abortOnFileOpenError);
        }
    }

    /** COBOL paragraph: OIPD-010 */
    private void openPoDetailFile() {
        fileSet.getPodf().open(FileOpenMode.IO);
        ws.trySetString("FSTS", fileSet.getPodf().getFileStatus());
        if (Utility.fieldEquals(ws.getFsts(), "35") || Utility.fieldEquals(ws.getFsts(), "30")) {
            fileSet.getPodf().open(FileOpenMode.OUTPUT);
            ws.trySetString("FSTS", fileSet.getPodf().getFileStatus());
            fileSet.getPodf().close();
            ws.trySetString("FSTS", fileSet.getPodf().getFileStatus());
            fileSet.getPodf().open(FileOpenMode.IO);
            ws.trySetString("FSTS", fileSet.getPodf().getFileStatus());
        }
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("PODF");
            runChain(this::abortOnFileOpenError);
        }
    }

    /** COBOL paragraph: PLOOP-010 */
    private void processOnePurchaseOrder() {
        runChain(this::clearPoHeaderFields);
        runChain(this::displayAndAcceptPoHeader);
        if ((ws.getEndFlg() == 1)) {
            return;
        }
        if ((ws.getHdrOk() != 1)) {
            return;
        }
        while ((ws.getDtlDone() != 1)) {
            runChain(this::displayAndAcceptDetailLine);
        }
        if (ws.getWkLcnt() > 0) {
            runChain(this::confirmAndSaveOrder);
        } else {
            showMessage("No lines entered - PO discarded");
        }
    }

    /** COBOL paragraph: CLRP-010 */
    private void clearPoHeaderFields() {
        fileSet.getPohf().setRecord();
        ws.setWkDet("");
        ws.setWkLcnt(0);
        ws.setWkNetTotal(0);
        ws.setWkTaxTotal(0);
        ws.setWkGrsTotal(0);
        ws.setHdrOk(0);
        ws.setDtlDone(0);
        ws.setWkPoNoD(0);
        ws.setWkSuppName(" ");
        ws.setWkConfirm(" ");
        ws.setPhDate(ws.getWkSysdate());
        ws.setPhDueDate(ws.getWkSysdate());
        ws.setPhTaxType(1);
    }

    /** COBOL paragraph: EHDR-010 */
    private void displayAndAcceptPoHeader() {
        ws.setHdrOk(0);
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEADER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-FOOTER"), ws);
        showMessage("Enter PO header - PF3 to quit");
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEAD"), ws);
        String scValPhDate0 =
                Utility.acceptScreen(
                        "PH-DATE", () -> renderer.acceptField(ScreenDefs.getInput("PH-DATE")));
        ws.setPhDate(Utility.parseIntOr(scValPhDate0.trim(), 0));
        String scValPhSupp1 =
                Utility.acceptScreen(
                        "PH-SUPP", () -> renderer.acceptField(ScreenDefs.getInput("PH-SUPP")));
        ws.setPhSupp(Utility.parseIntOr(scValPhSupp1.trim(), 0));
        String scValPhWhse2 =
                Utility.acceptScreen(
                        "PH-WHSE", () -> renderer.acceptField(ScreenDefs.getInput("PH-WHSE")));
        ws.setPhWhse(Utility.parseIntOr(scValPhWhse2.trim(), 0));
        String scValPhStaff3 =
                Utility.acceptScreen(
                        "PH-STAFF", () -> renderer.acceptField(ScreenDefs.getInput("PH-STAFF")));
        ws.setPhStaff(Utility.parseIntOr(scValPhStaff3.trim(), 0));
        String scValPhDueDate4 =
                Utility.acceptScreen(
                        "PH-DUE-DATE",
                        () -> renderer.acceptField(ScreenDefs.getInput("PH-DUE-DATE")));
        ws.setPhDueDate(Utility.parseIntOr(scValPhDueDate4.trim(), 0));
        String scValPhTaxType5 =
                Utility.acceptScreen(
                        "PH-TAX-TYPE",
                        () -> renderer.acceptField(ScreenDefs.getInput("PH-TAX-TYPE")));
        ws.setPhTaxType(Utility.parseIntOr(scValPhTaxType5.trim(), 0));
        String scValPhRemark6 =
                Utility.acceptScreen(
                        "PH-REMARK", () -> renderer.acceptField(ScreenDefs.getInput("PH-REMARK")));
        ws.setPhRemark(scValPhRemark6);
        broadcastEstsStatus();
        if (Utility.fieldEquals(ws.getEsts(), "03")) {
            if (ws.getPhSupp() == 0) {
                ws.setString("END-FLG", "1");
            }
            return;
        }
        runChain(this::validatePoHeader);
    }

    /** COBOL paragraph: VHDR-010 */
    private void validatePoHeader() {
        if (ws.getPhSupp() == 0) {
            showMessage("Supplier code required");
            return;
        }
        ws.setSpCode(ws.getPhSupp());
        String rkVal_7 = "";
        if (rkVal_7 == null || rkVal_7.trim().isEmpty()) {
            try {
                rkVal_7 = ws.getString("SP-CODE");
            } catch (Exception _e) {
            }
        }
        if (rkVal_7 == null || rkVal_7.trim().isEmpty()) {
            try {
                rkVal_7 = fileSet.getSuppf().extractKeyFromCurrentRecord();
            } catch (Exception _e3) {
            }
        }
        fileSet.getSuppf().readByKey(rkVal_7 != null ? rkVal_7.trim() : "");
        ws.trySetString("FSTS", fileSet.getSuppf().getFileStatus());
        if (fileSet.getSuppf().isInvalidKey()) {
            showMessage("Supplier not found");
            return;
        }
        if (ws.getSpDelFlag() == 1) {
            showMessage("Supplier is deleted");
            return;
        }
        ws.setWkSuppName(ws.getSpName());
        if (ws.getPhTaxType() == 0) {
            ws.setPhTaxType(ws.getSpTaxType());
        }
        if (ws.getPhTaxType() == 0) {
            ws.setPhTaxType(1);
        }
        ws.setHdrOk(1);
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEAD"), ws);
        showMessage("Header OK - enter detail lines");
    }

    /** COBOL paragraph: DLOOP-010 */
    private void displayAndAcceptDetailLine() {
        runChain(this::clearDetailLineFields);
        ws.setWkDWhse(ws.getPhWhse());
        renderer.displayScreen(ScreenDefs.getScreen("DS-DETAIL"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-STATUS"), ws);
        String scValWkDProd8 =
                Utility.acceptScreen(
                        "WK-D-PROD", () -> renderer.acceptField(ScreenDefs.getInput("WK-D-PROD")));
        ws.setWkDProd(Utility.parseIntOr(scValWkDProd8.trim(), 0));
        String scValWkDWhse9 =
                Utility.acceptScreen(
                        "WK-D-WHSE", () -> renderer.acceptField(ScreenDefs.getInput("WK-D-WHSE")));
        ws.setWkDWhse(Utility.parseIntOr(scValWkDWhse9.trim(), 0));
        String scValWkDQty10 =
                Utility.acceptScreen(
                        "WK-D-QTY", () -> renderer.acceptField(ScreenDefs.getInput("WK-D-QTY")));
        ws.setWkDQty(Utility.parseIntOr(scValWkDQty10.trim(), 0));
        String scValWkDCost11 =
                Utility.acceptScreen(
                        "WK-D-COST", () -> renderer.acceptField(ScreenDefs.getInput("WK-D-COST")));
        try {
            ws.setWkDCost(new BigDecimal(scValWkDCost11.trim()));
        } catch (NumberFormatException _nfe) {
            ws.setWkDCost(BigDecimal.ZERO);
        }
        broadcastEstsStatus();
        switch (String.valueOf(ws.getEsts())) {
            case "03" -> {
                ws.setString("DTL-DONE", "1");
            }
            case "04" -> {
                showMessage("Line cleared");
            }
            case "00" -> {
                runChain(this::processDetailLine);
            }
            default -> {
                showMessage("Invalid key");
            }
        }
    }

    /** COBOL paragraph: CLRD-010 */
    private void clearDetailLineFields() {
        ws.setWkDProd(0);
        ws.setWkDWhse(0);
        ws.setWkDQty(0);
        ws.setWkDCost(BigDecimal.ZERO);
        ws.setWkDAmt(0);
        ws.setWkDStkmng(0);
        ws.setWkDName(" ");
    }

    /** COBOL paragraph: PDET-010 */
    private void processDetailLine() {
        if (ws.getWkDProd() == 0) {
            showMessage("Product code required");
            return;
        }
        ws.setPrCode(ws.getWkDProd());
        String rkVal_12 = "";
        if (rkVal_12 == null || rkVal_12.trim().isEmpty()) {
            try {
                rkVal_12 = ws.getString("PR-CODE");
            } catch (Exception _e) {
            }
        }
        if (rkVal_12 == null || rkVal_12.trim().isEmpty()) {
            try {
                rkVal_12 = fileSet.getProdf().extractKeyFromCurrentRecord();
            } catch (Exception _e3) {
            }
        }
        fileSet.getProdf().readByKey(rkVal_12 != null ? rkVal_12.trim() : "");
        ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
        if (fileSet.getProdf().isInvalidKey()) {
            showMessage("Product not found");
            return;
        }
        if (ws.getPrDelFlag() == 1) {
            showMessage("Product is deleted");
            return;
        }
        ws.setWkDName(ws.getPrName());
        ws.setWkDStkmng(ws.getPrStockMng());
        if (ws.getWkDWhse() == 0) {
            ws.setWkDWhse(ws.getPrDfltWhse());
        }
        if (ws.getWkDWhse() == 0) {
            showMessage("Warehouse required");
            return;
        }
        if (ws.getWkDQty() <= 0) {
            showMessage("Quantity must be positive");
            return;
        }
        if ((ws.getWkDCost().signum() <= 0)) {
            runChain(this::resolveDefaultUnitCost);
        }
        if ((ws.getWkDCost().signum() <= 0)) {
            showMessage("Unit cost required");
            return;
        }
        ws.setWkDAmt(
                (BigDecimal.valueOf(ws.getWkDQty()).multiply(ws.getWkDCost()))
                        .setScale(0, java.math.RoundingMode.HALF_UP)
                        .longValue());
        runChain(this::addDetailLineToOrder);
    }

    /** COBOL paragraph: RCST-010 */
    private void resolveDefaultUnitCost() {
        if ((ws.getPrLastCost().signum() > 0)) {
            ws.setWkDCost(ws.getPrLastCost());
        } else {
            ws.setWkDCost(ws.getPrStdCost());
        }
    }

    /** COBOL paragraph: ADDL-010 */
    private void addDetailLineToOrder() {
        if (ws.getWkLcnt() >= 200) {
            showMessage("Maximum 200 lines reached");
            return;
        }
        ws.setWkLcnt(ws.getWkLcnt() + 1);
        ws.setLx(ws.getWkLcnt());
        ws.setWlProd(ws.getLx(), ws.getWkDProd());
        ws.setWlWhse(ws.getLx(), ws.getWkDWhse());
        ws.setWlQty(ws.getLx(), ws.getWkDQty());
        ws.setWlCost(ws.getLx(), ws.getWkDCost());
        ws.setWlAmount(ws.getLx(), ws.getWkDAmt());
        ws.setWlStkmng(ws.getLx(), ws.getWkDStkmng());
        ws.setWkNetTotal(ws.getWkNetTotal() + ws.getWkDAmt());
        renderer.displayScreen(ScreenDefs.getScreen("DS-STATUS"), ws);
        showMessage("Line added");
    }

    /** COBOL paragraph: CSAV-010 */
    private void confirmAndSaveOrder() {
        ws.setWkConfirm(" ");
        runChain(this::calculateOrderTaxTotals);
        renderer.displayScreen(ScreenDefs.getScreen("DS-STATUS"), ws);
        showMessage("Review totals then confirm");
        renderer.displayScreen(ScreenDefs.getScreen("DS-CONFIRM"), ws);
        String scValWkConfirm13 =
                Utility.acceptScreen(
                        "WK-CONFIRM",
                        () -> renderer.acceptField(ScreenDefs.getInput("WK-CONFIRM")));
        ws.setWkConfirm(scValWkConfirm13);
        broadcastEstsStatus();
        if ((ws.getWkConfirm().equals("Y") || ws.getWkConfirm().equals("y"))) {
            runChain(this::savePurchaseOrderHeader);
        } else {
            showMessage("PO discarded");
        }
    }

    /** COBOL paragraph: CTT-010 */
    private void calculateOrderTaxTotals() {
        ws.setKtCategory(1);
        ws.setKtTaxType(ws.getPhTaxType());
        ws.setKtRound(1);
        ws.setKtDate(ws.getPhDate());
        ws.setKtAmount(BigDecimal.valueOf(ws.getWkNetTotal()));
        taxcal(ws.getKtax());
        if (Utility.fieldEquals(ws.getKtStatus(), "00")) {
            ws.setWkNetTotal(ws.getKtNet().longValue());
            ws.setWkTaxTotal(ws.getKtTax().longValue());
            ws.setWkGrsTotal(ws.getKtGross().longValue());
        } else {
            ws.setWkTaxTotal(0);
            ws.setWkGrsTotal(ws.getWkNetTotal());
        }
    }

    /** COBOL paragraph: SPO-010 */
    private void savePurchaseOrderHeader() {
        ws.setKnumKey("PO");
        numgen(ws.getKnum());
        if (!Utility.fieldEquals(ws.getKnumStatus(), "00")) {
            showMessage("Number assignment failed");
            return;
        }
        ws.setPhNo(ws.getKnumNumber());
        ws.setWkPoNoD(ws.getKnumNumber());
        ws.setPhAmount(BigDecimal.valueOf(ws.getWkNetTotal()));
        ws.setPhTaxAmount(BigDecimal.valueOf(ws.getWkTaxTotal()));
        ws.setPhTotal(BigDecimal.valueOf(ws.getWkGrsTotal()));
        ws.setPhStatus(0);
        ws.setPhLines(ws.getWkLcnt());
        ws.setPhAddDate(ws.getWkSysdate());
        ws.setPhAddUser(ws.getWkUserCode());
        ws.setPhDelFlag(0);
        fileSet.getPohf().write();
        ws.trySetString("FSTS", fileSet.getPohf().getFileStatus());
        if (fileSet.getPohf().isInvalidKey()) {
            showMessage("Header write failed");
            return;
        }
        runChain(this::writePurchaseOrderDetails);
        ws.setWkMsgLine("PO saved");
        renderer.displayScreen(ScreenDefs.getScreen("DS-STATUS"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
    }

    /** COBOL paragraph: WDET-010 */
    private void writePurchaseOrderDetails() {
        for (ws.setLx(1); ws.getLx() <= ws.getWkLcnt(); ws.setLx(ws.getLx() + 1)) {
            fileSet.getPodf().setRecord();
            ws.setPdNo(ws.getPhNo());
            ws.setWkIdx(ws.getLx());
            ws.setPdLine(ws.getWkIdx());
            ws.setPdProd(ws.getWlProd(ws.getLx()));
            ws.setPdWhse(ws.getWlWhse(ws.getLx()));
            ws.setPdQty(BigDecimal.valueOf(ws.getWlQty(ws.getLx())));
            ws.setPdUnitCost(ws.getWlCost(ws.getLx()));
            ws.setPdAmount(BigDecimal.valueOf(ws.getWlAmount(ws.getLx())));
            ws.setPdRecvQty(BigDecimal.ZERO);
            ws.setPdDueDate(ws.getPhDueDate());
            ws.setPdStatus(0);
            fileSet.getPodf().write();
            ws.trySetString("FSTS", fileSet.getPodf().getFileStatus());
            if (fileSet.getPodf().isInvalidKey()) {
                /* CONTINUE */
            }
            runChain(this::updateStockOnOrderQty);
        }
    }

    /** COBOL paragraph: UONO-010 */
    private void updateStockOnOrderQty() {
        if (ws.getWlStkmng(ws.getLx()) == 0) {
            return;
        }
        ws.setSkProd(ws.getWlProd(ws.getLx()));
        ws.setSkWhse(ws.getWlWhse(ws.getLx()));
        StringBuilder rkSb_14 = new StringBuilder();
        String rkPart0_14 = "";
        try {
            rkPart0_14 = ws.getString("SK-PROD");
        } catch (Exception _e) {
        }
        rkSb_14.append(rkPart0_14 != null ? rkPart0_14.trim() : "");
        String rkPart1_14 = "";
        try {
            rkPart1_14 = ws.getString("SK-WHSE");
        } catch (Exception _e) {
        }
        rkSb_14.append('|');
        rkSb_14.append(rkPart1_14 != null ? rkPart1_14.trim() : "");
        fileSet.getStokf().readByKey(rkSb_14.toString());
        ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
        if (fileSet.getStokf().isInvalidKey()) {
            fileSet.getStokf().setRecord();
            ws.setSkProd(ws.getWlProd(ws.getLx()));
            ws.setSkWhse(ws.getWlWhse(ws.getLx()));
            ws.setSkOnOrder(BigDecimal.valueOf(ws.getWlQty(ws.getLx())));
            fileSet.getStokf().write();
            ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
            if (fileSet.getStokf().isInvalidKey()) {
                /* CONTINUE */
            }
        }
        if (!fileSet.getStokf().isInvalidKey()) {
            ws.setSkOnOrder(ws.getSkOnOrder().add(BigDecimal.valueOf(ws.getWlQty(ws.getLx()))));
            fileSet.getStokf().rewrite();
            ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
            if (fileSet.getStokf().isInvalidKey()) {
                /* CONTINUE */
            }
        }
    }

    /** COBOL paragraph: TERM-010 */
    private void closeAllFiles() {
        fileSet.getPohf().close();
        ws.trySetString("FSTS", fileSet.getPohf().getFileStatus());
        fileSet.getPodf().close();
        ws.trySetString("FSTS", fileSet.getPodf().getFileStatus());
        fileSet.getStokf().close();
        ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
        fileSet.getSuppf().close();
        ws.trySetString("FSTS", fileSet.getSuppf().getFileStatus());
        fileSet.getProdf().close();
        ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
    }

    /** COBOL paragraph: ABEND-010 */
    private void abortOnFileOpenError() {
        ws.setKaProgid("PU0010");
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

    /** COBOL CALL TAXCAL — delegates to injected TaxcalService. */
    private void taxcal(Object... args) {
        TaxcalLinkParm params = new TaxcalLinkParm();
        params.getKtax().setKtCategory(ws.getKtCategory());
        params.getKtax().setKtTaxType(ws.getKtTaxType());
        params.getKtax().setKtRound(ws.getKtRound());
        params.getKtax().setKtDate(ws.getKtDate());
        params.getKtax().setKtAmount(ws.getKtAmount());
        params.getKtax().setKtTax(ws.getKtTax());
        params.getKtax().setKtNet(ws.getKtNet());
        params.getKtax().setKtGross(ws.getKtGross());
        params.getKtax().setKtRate(ws.getKtRate());
        params.getKtax().setKtStatus(ws.getKtStatus());
        taxcalService.execute(params);
        ws.setKtCategory(params.getKtax().getKtCategory());
        ws.setKtTaxType(params.getKtax().getKtTaxType());
        ws.setKtRound(params.getKtax().getKtRound());
        ws.setKtDate(params.getKtax().getKtDate());
        ws.setKtAmount(params.getKtax().getKtAmount());
        ws.setKtTax(params.getKtax().getKtTax());
        ws.setKtNet(params.getKtax().getKtNet());
        ws.setKtGross(params.getKtax().getKtGross());
        ws.setKtRate(params.getKtax().getKtRate());
        ws.setKtStatus(params.getKtax().getKtStatus());
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

    /** Set the status-line message and redisplay the message screen. */
    private void showMessage(String msg) {
        ws.setWkMsgLine(msg);
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
    }
}
