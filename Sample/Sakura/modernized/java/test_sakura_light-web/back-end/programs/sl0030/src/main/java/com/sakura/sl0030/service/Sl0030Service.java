package com.sakura.sl0030.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.numgen.service.NumgenService;
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
import com.sakura.runtime.linkage.TaxcalLinkParm;
import com.sakura.runtime.record.RawDatasetBase;
import com.sakura.sl0030.domain.Sl0030FieldAccess;
import com.sakura.sl0030.domain.WorkingStorage;
import com.sakura.sl0030.runtime.Sl0030Datasets;
import com.sakura.sl0030.screen.ScreenDefs;
import com.sakura.taxcal.service.TaxcalService;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/** Business logic service generated from COBOL program SL0030. */
@Service
@Scope("prototype")
public class Sl0030Service extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final Sl0030Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL TAXCAL. */
    private TaxcalService taxcalService;

    /** Injected service for COBOL CALL NUMGEN. */
    private NumgenService numgenService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Sl0030FieldAccess ws;

    public Sl0030Service(
            Sl0030Datasets fileSet,
            DateutService dateutService,
            TaxcalService taxcalService,
            NumgenService numgenService,
            AbortxService abortxService,
            ScreenRendererInstance renderer) {
        this.fileSet = fileSet;
        this.ws = new Sl0030FieldAccess(new WorkingStorage(), fileSet);
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
            runChain(this::acceptReturnKeyEntry);
        }
        runChain(this::closeAllFiles);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeProgram() {
        ws.setWkProgid("SL0030");
        ws.setWkTitle("Sales Return Entry");
        ws.setWkFkeyLine("ENTER=Next  PF3=End");
        ws.setKdFunc("TODY");
        dateut(ws.getKdate());
        ws.setWkSysymd(ws.getKdDate1());
        ws.setWkSysdate(ws.getKdDate1());
        runChain(this::openAllFiles);
    }

    /** COBOL paragraph: OPENF-010 */
    private void openAllFiles() {
        runChain(this::openInvoiceHeaderFile);
        runChain(this::openInvoiceDetailFile);
        runChain(this::openStockFile);
        runChain(this::openStockMovementFile);
        runChain(this::openCustomerFile);
        runChain(this::openArLedgerFile);
        fileSet.getProdf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
        fileSet.getCprcf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getCprcf().getFileStatus());
    }

    /** COBOL paragraph: OIIH-010 */
    private void openInvoiceHeaderFile() {
        openFileWithRetry(fileSet.getInvhf(), "INVHF");
    }

    /** COBOL paragraph: OIID-010 */
    private void openInvoiceDetailFile() {
        openFileWithRetry(fileSet.getInvdf(), "INVDF");
    }

    /** COBOL paragraph: OIST-010 */
    private void openStockFile() {
        openFileWithRetry(fileSet.getStokf(), "STOKF");
    }

    /** COBOL paragraph: OISM-010 */
    private void openStockMovementFile() {
        openFileWithRetry(fileSet.getSmovf(), "SMOVF");
    }

    /** COBOL paragraph: OICU-010 */
    private void openCustomerFile() {
        openFileWithRetry(fileSet.getCustf(), "CUSTF");
    }

    /** COBOL paragraph: OIAR-010 */
    private void openArLedgerFile() {
        openFileWithRetry(fileSet.getArlf(), "ARLF");
    }

    /** COBOL paragraph: MAINR-010 */
    private void acceptReturnKeyEntry() {
        runChain(this::clearReturnWorkArea);
        displayHeaderFooter();
        showMessage("Enter the customer code for the return");
        renderer.displayScreen(ScreenDefs.getScreen("DS-KEY"), ws);
        String scValWkSelCust0 =
                Utility.acceptScreen(
                        "WK-SEL-CUST",
                        () -> renderer.acceptField(ScreenDefs.getInput("WK-SEL-CUST")));
        ws.setWkSelCust(Utility.parseIntOr(scValWkSelCust0.trim(), 0));
        String scValWkOrigInv1 =
                Utility.acceptScreen(
                        "WK-ORIG-INV",
                        () -> renderer.acceptField(ScreenDefs.getInput("WK-ORIG-INV")));
        ws.setWkOrigInv(Utility.parseLongOr(scValWkOrigInv1.trim(), 0L));
        broadcastEstsStatus();
        switch (String.valueOf(ws.getEsts())) {
            case "03" -> {
                ws.setString("END-FLG", "1");
            }
            case "00" -> {
                runChain(this::processReturnEntry);
            }
            default -> {
                showMessage("Invalid function key");
            }
        }
    }

    /** COBOL paragraph: CLRR-010 */
    private void clearReturnWorkArea() {
        ws.setWkSelCust(0);
        ws.setWkOrigInv(0);
        ws.setWkInvNo(0);
        ws.setWkDcnt(0);
        ws.setWkNetTotal(0);
        ws.setWkTaxTotal(0);
        ws.setWkGrsTotal(0);
        ws.setWkCostTotal(0);
        ws.setWkStaffIn(0);
        ws.setWkTaxtypeIn(0);
        ws.setWkPageTop(1);
        ws.setWkCustName(" ");
        ws.setWkConfirm(" ");
    }

    /** COBOL paragraph: PRT-010 */
    private void processReturnEntry() {
        if (ws.getWkSelCust() == 0) {
            showMessage("Customer code required");
            return;
        }
        runChain(this::readCustomerByCode);
        if ((ws.getFoundFlg() != 1)) {
            displayMessage();
            return;
        }
        ws.setWkStaffIn(ws.getCuStaff());
        ws.setWkTaxtypeIn(ws.getCuTaxType());
        runChain(this::editReturnHeader);
        if ((ws.getErrFlg() == 1)) {
            return;
        }
        runChain(this::acceptReturnDetailLines);
        if (ws.getWkDcnt() == 0) {
            showMessage("No lines entered - return discarded");
            return;
        }
        runChain(this::computeReturnTotals);
        runChain(this::reviewAndPostReturn);
    }

    /** COBOL paragraph: RC-010 */
    private void readCustomerByCode() {
        ws.setFoundFlg(0);
        ws.setWkCustName(" ");
        ws.setCuCode(ws.getWkSelCust());
        readByFallbackKey(fileSet.getCustf(), "CU-CODE");
        if (fileSet.getCustf().isInvalidKey()) {
            ws.setWkMsgLine("Customer not found");
            return;
        }
        if (ws.getCuDelFlag() == 1) {
            ws.setWkMsgLine("Customer is deleted");
            return;
        }
        ws.setWkCustName(ws.getCuName());
        ws.setString("FOUND-FLG", "1");
    }

    /** COBOL paragraph: EH-010 */
    private void editReturnHeader() {
        ws.setErrFlg(0);
        displayHeaderFooter();
        showMessage("Enter return header - PF3 to cancel");
        renderer.displayScreen(ScreenDefs.getScreen("DS-DHEAD"), ws);
        String scValWkStaffIn3 =
                Utility.acceptScreen(
                        "WK-STAFF-IN",
                        () -> renderer.acceptField(ScreenDefs.getInput("WK-STAFF-IN")));
        ws.setWkStaffIn(Utility.parseIntOr(scValWkStaffIn3.trim(), 0));
        String scValWkTaxtypeIn4 =
                Utility.acceptScreen(
                        "WK-TAXTYPE-IN",
                        () -> renderer.acceptField(ScreenDefs.getInput("WK-TAXTYPE-IN")));
        ws.setWkTaxtypeIn(Utility.parseIntOr(scValWkTaxtypeIn4.trim(), 0));
        broadcastEstsStatus();
        if (Utility.fieldEquals(ws.getEsts(), "03")) {
            ws.setString("ERR-FLG", "1");
            return;
        }
        if (ws.getWkTaxtypeIn() == 0) {
            ws.setWkTaxtypeIn(ws.getCuTaxType());
        }
        if (ws.getWkTaxtypeIn() < 1 || ws.getWkTaxtypeIn() > 3) {
            showMessage("Tax type must be 1, 2 or 3");
            ws.setString("ERR-FLG", "1");
        }
    }

    /** COBOL paragraph: DEL-010 */
    private void acceptReturnDetailLines() {
        ws.setWkDtlDone(0);
        while ((ws.getWkDtlDone() != 1)) {
            runChain(this::clearDetailWorkArea);
            renderer.displayScreen(ScreenDefs.getScreen("DS-DETAIL"), ws);
            renderer.displayScreen(ScreenDefs.getScreen("DS-ESTAT"), ws);
            String scValWkDProd5 =
                    Utility.acceptScreen(
                            "WK-D-PROD",
                            () -> renderer.acceptField(ScreenDefs.getInput("WK-D-PROD")));
            ws.setWkDProd(Utility.parseIntOr(scValWkDProd5.trim(), 0));
            String scValWkDWhse6 =
                    Utility.acceptScreen(
                            "WK-D-WHSE",
                            () -> renderer.acceptField(ScreenDefs.getInput("WK-D-WHSE")));
            ws.setWkDWhse(Utility.parseIntOr(scValWkDWhse6.trim(), 0));
            String scValWkDQty7 =
                    Utility.acceptScreen(
                            "WK-D-QTY",
                            () -> renderer.acceptField(ScreenDefs.getInput("WK-D-QTY")));
            ws.setWkDQty(Utility.parseIntOr(scValWkDQty7.trim(), 0));
            String scValWkDPrice8 =
                    Utility.acceptScreen(
                            "WK-D-PRICE",
                            () -> renderer.acceptField(ScreenDefs.getInput("WK-D-PRICE")));
            try {
                ws.setWkDPrice(new BigDecimal(scValWkDPrice8.trim()));
            } catch (NumberFormatException _nfe) {
                ws.setWkDPrice(BigDecimal.ZERO);
            }
            broadcastEstsStatus();
            switch (String.valueOf(ws.getEsts())) {
                case "03" -> {
                    ws.setString("WK-DTL-DONE", "1");
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
    }

    /** COBOL paragraph: CLRD-010 */
    private void clearDetailWorkArea() {
        ws.setWkDProd(0);
        ws.setWkDWhse(0);
        ws.setWkDQty(0);
        ws.setWkDPrice(BigDecimal.ZERO);
        ws.setWkDAmt(0);
        ws.setWkDCost(BigDecimal.ZERO);
        ws.setWkDCostamt(0);
        ws.setWkDTaxcat(1);
        ws.setWkDName(" ");
    }

    /** COBOL paragraph: PDET-010 */
    private void processDetailLine() {
        if (ws.getWkDProd() == 0) {
            showMessage("Product code required");
            return;
        }
        runChain(this::lookupProductByCode);
        if ((ws.getFoundFlg() != 1)) {
            showMessage("Product not found");
            return;
        }
        if (ws.getWkDWhse() == 0) {
            ws.setWkDWhse(ws.getPrDfltWhse());
        }
        if (ws.getWkDQty() <= 0) {
            showMessage("Return qty must be positive");
            return;
        }
        if ((ws.getWkDPrice().signum() <= 0)) {
            runChain(this::resolveCustomerPrice);
        }
        runChain(this::resolveUnitCost);
        ws.setWkDAmt(BigDecimal.valueOf(ws.getWkDQty()).multiply(ws.getWkDPrice()).longValue());
        ws.setWkDCostamt(BigDecimal.valueOf(ws.getWkDQty()).multiply(ws.getWkDCost()).longValue());
        runChain(this::addDetailLineToReturn);
    }

    /** COBOL paragraph: LKP-010 */
    private void lookupProductByCode() {
        ws.setFoundFlg(0);
        ws.setWkDName(" ");
        ws.setWkDTaxcat(1);
        ws.setPrCode(ws.getWkDProd());
        readByFallbackKey(fileSet.getProdf(), "PR-CODE");
        if (fileSet.getProdf().isInvalidKey()) {
            return;
        }
        if (ws.getPrDelFlag() == 1) {
            return;
        }
        ws.setWkDName(ws.getPrName());
        ws.setWkDTaxcat(ws.getPrTaxCategory());
        ws.setString("FOUND-FLG", "1");
    }

    /** COBOL paragraph: RPRC-010 */
    private void resolveCustomerPrice() {
        ws.setCpCust(ws.getWkSelCust());
        ws.setCpProd(ws.getWkDProd());
        readByCompositeKey(fileSet.getCprcf(), "CP-CUST", "CP-PROD");
        if (fileSet.getCprcf().isInvalidKey()) {
            runChain(this::applyPriceRankFallback);
        }
        if (!fileSet.getCprcf().isInvalidKey()) {
            if (ws.getCpDelFlag() == 0
                    && ws.getCpStartDate() <= ws.getWkSysdate()
                    && (ws.getCpEndDate() == 0 || ws.getCpEndDate() >= ws.getWkSysdate())) {
                ws.setWkDPrice(ws.getCpPrice());
            } else {
                runChain(this::applyPriceRankFallback);
            }
        }
    }

    /** COBOL paragraph: RANK-010 */
    private void applyPriceRankFallback() {
        if (ws.getCuPriceRank() >= 1 && ws.getCuPriceRank() <= 5) {
            ws.setWkDPrice(ws.getPrRankPrice(ws.getCuPriceRank()));
        }
        if ((ws.getWkDPrice().signum() <= 0)) {
            ws.setWkDPrice(ws.getPrListPrice());
        }
    }

    /** COBOL paragraph: RCST-010 */
    private void resolveUnitCost() {
        ws.setWkDCost(BigDecimal.ZERO);
        ws.setSkProd(ws.getWkDProd());
        ws.setSkWhse(ws.getWkDWhse());
        readByCompositeKey(fileSet.getStokf(), "SK-PROD", "SK-WHSE");
        if (fileSet.getStokf().isInvalidKey()) {
            ws.setWkDCost(BigDecimal.ZERO);
        }
        if (!fileSet.getStokf().isInvalidKey()) {
            ws.setWkDCost(ws.getSkAvgCost());
        }
        if ((ws.getWkDCost().signum() <= 0)) {
            ws.setPrCode(ws.getWkDProd());
            readByFallbackKey(fileSet.getProdf(), "PR-CODE");
            if (fileSet.getProdf().isInvalidKey()) {
                /* CONTINUE */
            }
            if (!fileSet.getProdf().isInvalidKey()) {
                ws.setWkDCost(ws.getPrStdCost());
            }
        }
    }

    /** COBOL paragraph: ADDL-010 */
    private void addDetailLineToReturn() {
        if (ws.getWkDcnt() >= MAX_RETURN_LINES) {
            showMessage("Maximum 200 lines reached");
            return;
        }
        ws.setWkDcnt(ws.getWkDcnt() + 1);
        ws.setDrProd(ws.getWkDcnt(), ws.getWkDProd());
        ws.setDrWhse(ws.getWkDcnt(), ws.getWkDWhse());
        ws.setDrName(ws.getWkDcnt(), ws.getWkDName());
        ws.setDrQty(ws.getWkDcnt(), ws.getWkDQty());
        ws.setDrPrice(ws.getWkDcnt(), ws.getWkDPrice());
        ws.setDrAmt(ws.getWkDcnt(), ws.getWkDAmt());
        ws.setDrCost(ws.getWkDcnt(), ws.getWkDCost());
        ws.setDrCostamt(ws.getWkDcnt(), ws.getWkDCostamt());
        ws.setDrTaxcat(ws.getWkDcnt(), ws.getWkDTaxcat());
        ws.setDrStkmng(ws.getWkDcnt(), ws.getPrStockMng());
        ws.setWkNetTotal(ws.getWkNetTotal() + ws.getWkDAmt());
        ws.setWkMsgLine("Line added");
        renderer.displayScreen(ScreenDefs.getScreen("DS-ESTAT"), ws);
        displayMessage();
    }

    /** COBOL paragraph: CT-010 */
    private void computeReturnTotals() {
        ws.setWkNetTotal(0);
        ws.setWkCostTotal(0);
        for (ws.setWkIdx(1); ws.getWkIdx() <= ws.getWkDcnt(); ws.setWkIdx(ws.getWkIdx() + 1)) {
            ws.setWkNetTotal(ws.getWkNetTotal() + ws.getDrAmt(ws.getWkIdx()));
            ws.setWkCostTotal(ws.getWkCostTotal() + ws.getDrCostamt(ws.getWkIdx()));
        }
        ws.setKtCategory(1);
        ws.setKtTaxType(ws.getWkTaxtypeIn());
        ws.setKtRound(ws.getCuTaxRound());
        if (ws.getKtRound() == 0) {
            ws.setKtRound(1);
        }
        ws.setKtDate(ws.getWkSysdate());
        ws.setKtAmount(BigDecimal.valueOf(ws.getWkNetTotal()));
        taxcal(ws.getKtax());
        ws.setWkNetTotal(ws.getKtNet().longValue());
        ws.setWkTaxTotal(ws.getKtTax().longValue());
        ws.setWkGrsTotal(ws.getKtGross().longValue());
    }

    /** COBOL paragraph: RAP-010 */
    private void reviewAndPostReturn() {
        runChain(this::calculatePageCount);
        ws.setWkPageTop(1);
        runChain(this::buildBrowseWindow);
        runChain(this::runReviewBrowseLoop);
        if ((ws.getWkReviewEnd() == 1)) {
            runChain(this::confirmAndPostOrDiscard);
        }
    }

    /** COBOL paragraph: RVL-010 */
    private void runReviewBrowseLoop() {
        ws.setWkReviewEnd(0);
        ws.setWkFkeyLine("PF6/PF12=page  ENTER=confirm  PF3=cancel");
        while (!((ws.getWkReviewEnd() == 1) || (ws.getEndFlg() == 1))) {
            runChain(this::renderReturnSummaryScreens);
            renderer.displayScreen(ScreenDefs.getScreen("DS-BROWSE"), ws);
            String scValWkDummy13 =
                    Utility.acceptScreen(
                            "WK-DUMMY",
                            () -> renderer.acceptField(ScreenDefs.getInput("WK-DUMMY")));
            ws.setWkDummy(scValWkDummy13);
            broadcastEstsStatus();
            switch (String.valueOf(ws.getEsts())) {
                case "03" -> {
                    showMessage("Return cancelled");
                    throw new ParagraphJumpSignal(this::restoreMainFkeyLine);
                }
                case "00" -> {
                    ws.setString("WK-REVIEW-END", "1");
                }
                case "06" -> {
                    runChain(this::pageDownBrowse);
                }
                case "12" -> {
                    runChain(this::pageUpBrowse);
                }
                default -> {
                    /* CONTINUE */
                }
            }
        }
        // fall-through to next paragraph
        restoreMainFkeyLine();
    }

    /** COBOL paragraph: RVL-020 */
    private void restoreMainFkeyLine() {
        ws.setWkFkeyLine("ENTER=Next  PF3=End");
    }

    /** COBOL paragraph: AC-010 */
    private void confirmAndPostOrDiscard() {
        ws.setWkConfirm(" ");
        runChain(this::renderReturnSummaryScreens);
        showMessage("Confirm to post the return");
        renderer.displayScreen(ScreenDefs.getScreen("DS-CONFIRM"), ws);
        String scValWkConfirm14 =
                Utility.acceptScreen(
                        "WK-CONFIRM",
                        () -> renderer.acceptField(ScreenDefs.getInput("WK-CONFIRM")));
        ws.setWkConfirm(scValWkConfirm14);
        broadcastEstsStatus();
        if ((ws.getWkConfirm().equals("Y") || ws.getWkConfirm().equals("y"))) {
            runChain(this::postReturnTransaction);
        } else {
            showMessage("Return discarded");
        }
    }

    /** COBOL paragraph: POSR-010 */
    private void postReturnTransaction() {
        ws.setKnumKey("INVOICE");
        numgen(ws.getKnum());
        if (!Utility.fieldEquals(ws.getKnumStatus(), "00")) {
            showMessage("Return number assignment failed");
            return;
        }
        ws.setWkInvNo(ws.getKnumNumber());
        runChain(this::computeCloseYearMonth);
        runChain(this::writeInvoiceHeaderRecord);
        runChain(this::writeInvoiceDetailRecords);
        runChain(this::postArLedgerEntry);
        runChain(this::updateCustomerBalance);
        ws.setWkMsgLine(" ");
        {
            StringBuilder sb = new StringBuilder();
            sb.append(String.valueOf("Return "));
            sb.append(String.valueOf(String.format("%010d", (long) (ws.getWkInvNo()))));
            sb.append(String.valueOf(" posted"));
            ws.setWkMsgLine(sb.toString());
        }
        displayMessage();
    }

    /** COBOL paragraph: CCY-010 */
    private void computeCloseYearMonth() {
        ws.setWkYm((ws.getWkSysdate() / DATE_COMPONENT_DIVISOR));
        ws.setWkDay((ws.getWkSysdate() - (ws.getWkYm() * DATE_COMPONENT_DIVISOR)));
        ws.setWkCloseYm(ws.getWkYm());
        if (ws.getCuCloseDay() > 0 && ws.getCuCloseDay() < 99) {
            if (ws.getWkDay() > ws.getCuCloseDay()) {
                ws.setWkYyyy((ws.getWkYm() / DATE_COMPONENT_DIVISOR));
                ws.setWkMm((ws.getWkYm() - (ws.getWkYyyy() * DATE_COMPONENT_DIVISOR)));
                ws.setWkMm(ws.getWkMm() + 1);
                if (ws.getWkMm() > 12) {
                    ws.setWkMm(1);
                    ws.setWkYyyy(ws.getWkYyyy() + 1);
                }
                ws.setWkCloseYm(((ws.getWkYyyy() * DATE_COMPONENT_DIVISOR) + ws.getWkMm()));
            }
        }
    }

    /** COBOL paragraph: WRH-010 */
    private void writeInvoiceHeaderRecord() {
        fileSet.getInvhf().setRecord();
        ws.setIhNo(ws.getWkInvNo());
        ws.setIhDate(ws.getWkSysdate());
        ws.setIhCust(ws.getWkSelCust());
        ws.setIhStaff(ws.getWkStaffIn());
        ws.setIhShipNo(0);
        ws.setIhCloseYm(ws.getWkCloseYm());
        ws.setIhTaxType(ws.getWkTaxtypeIn());
        ws.setIhAmount(
                (BigDecimal.ZERO.subtract(BigDecimal.valueOf(ws.getWkNetTotal())))
                        .setScale(0, java.math.RoundingMode.DOWN));
        ws.setIhTaxAmount(
                (BigDecimal.ZERO.subtract(BigDecimal.valueOf(ws.getWkTaxTotal())))
                        .setScale(0, java.math.RoundingMode.DOWN));
        ws.setIhTotal(
                (BigDecimal.ZERO.subtract(BigDecimal.valueOf(ws.getWkGrsTotal())))
                        .setScale(0, java.math.RoundingMode.DOWN));
        ws.setIhCostTotal(
                (BigDecimal.ZERO.subtract(BigDecimal.valueOf(ws.getWkCostTotal())))
                        .setScale(0, java.math.RoundingMode.DOWN));
        ws.setIhStatus(1);
        ws.setIhKind(2);
        ws.setIhLines(ws.getWkDcnt());
        ws.setIhRemark(" ");
        if (ws.getWkOrigInv() != 0) {
            {
                StringBuilder sb = new StringBuilder();
                sb.append(String.valueOf("Return ref inv "));
                sb.append(String.valueOf(String.format("%010d", (long) (ws.getWkOrigInv()))));
                ws.setIhRemark(sb.toString());
            }
        }
        ws.setIhAddDate(ws.getWkSysdate());
        ws.setIhUpdDate(ws.getWkSysdate());
        ws.setIhAddUser(ws.getWkUserCode());
        ws.setIhUpdUser(ws.getWkUserCode());
        ws.setIhDelFlag(0);
        fileSet.getInvhf().write();
        ws.trySetString("FSTS", fileSet.getInvhf().getFileStatus());
        if (fileSet.getInvhf().isInvalidKey()) {
            showMessage("Return header write failed");
        }
    }

    /** COBOL paragraph: WRD-010 */
    private void writeInvoiceDetailRecords() {
        for (ws.setWkIdx(1); ws.getWkIdx() <= ws.getWkDcnt(); ws.setWkIdx(ws.getWkIdx() + 1)) {
            fileSet.getInvdf().setRecord();
            ws.setIdNo(ws.getWkInvNo());
            ws.setIdLine(ws.getWkIdx());
            ws.setIdProd(ws.getDrProd(ws.getWkIdx()));
            ws.setIdWhse(ws.getDrWhse(ws.getWkIdx()));
            ws.setIdQty(
                    (BigDecimal.ZERO.subtract(BigDecimal.valueOf(ws.getDrQty(ws.getWkIdx()))))
                            .setScale(0, java.math.RoundingMode.DOWN));
            ws.setIdUnitPrice(ws.getDrPrice(ws.getWkIdx()));
            ws.setIdAmount(
                    (BigDecimal.ZERO.subtract(BigDecimal.valueOf(ws.getDrAmt(ws.getWkIdx()))))
                            .setScale(0, java.math.RoundingMode.DOWN));
            ws.setIdUnitCost(ws.getDrCost(ws.getWkIdx()));
            ws.setIdCostAmount(
                    (BigDecimal.ZERO.subtract(BigDecimal.valueOf(ws.getDrCostamt(ws.getWkIdx()))))
                            .setScale(0, java.math.RoundingMode.DOWN));
            ws.setIdTaxCategory(ws.getDrTaxcat(ws.getWkIdx()));
            ws.setIdRemark(" ");
            fileSet.getInvdf().write();
            ws.trySetString("FSTS", fileSet.getInvdf().getFileStatus());
            if (fileSet.getInvdf().isInvalidKey()) {
                /* CONTINUE */
            }
            runChain(this::updateStockForReturnLine);
            runChain(this::writeStockMovementRecord);
        }
    }

    /** COBOL paragraph: IS-010 */
    private void updateStockForReturnLine() {
        ws.setWkStkFound(0);
        ws.setWkLineQty(ws.getDrQty(ws.getWkIdx()));
        ws.setWkLineCost(ws.getDrCost(ws.getWkIdx()));
        if (ws.getDrStkmng(ws.getWkIdx()) == 0) {
            return;
        }
        ws.setSkProd(ws.getDrProd(ws.getWkIdx()));
        ws.setSkWhse(ws.getDrWhse(ws.getWkIdx()));
        readByCompositeKey(fileSet.getStokf(), "SK-PROD", "SK-WHSE");
        if (fileSet.getStokf().isInvalidKey()) {
            runChain(this::createStockRecord);
            return;
        }
        ws.setWkStkFound(1);
        ws.setSkOnhand(ws.getSkOnhand().add(BigDecimal.valueOf(ws.getWkLineQty())));
        ws.setSkYtdIn(ws.getSkYtdIn().add(BigDecimal.valueOf(ws.getWkLineQty())));
        ws.setSkLastInDate(ws.getWkSysdate());
        ws.setWkNewbal(ws.getSkOnhand().intValue());
        fileSet.getStokf().rewrite();
        ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
        if (fileSet.getStokf().isInvalidKey()) {
            showMessage("Stock update failed");
        }
    }

    /** COBOL paragraph: CRS-010 */
    private void createStockRecord() {
        fileSet.getStokf().setRecord();
        ws.setSkProd(ws.getDrProd(ws.getWkIdx()));
        ws.setSkWhse(ws.getDrWhse(ws.getWkIdx()));
        ws.setSkOnhand(BigDecimal.valueOf(ws.getWkLineQty()));
        ws.setSkAllocated(BigDecimal.ZERO);
        ws.setSkOnOrder(BigDecimal.ZERO);
        ws.setSkAvgCost(ws.getWkLineCost());
        ws.setSkLastInDate(ws.getWkSysdate());
        ws.setSkYtdIn(BigDecimal.valueOf(ws.getWkLineQty()));
        ws.setWkNewbal(ws.getWkLineQty());
        fileSet.getStokf().write();
        ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
        if (fileSet.getStokf().isInvalidKey()) {
            showMessage("Stock create failed");
        }
        if (!fileSet.getStokf().isInvalidKey()) {
            ws.setWkStkFound(1);
        }
    }

    /** COBOL paragraph: WM-010 */
    private void writeStockMovementRecord() {
        if (ws.getDrStkmng(ws.getWkIdx()) == 0 || ws.getWkStkFound() == 0) {
            return;
        }
        ws.setKnumKey("STKMOV");
        numgen(ws.getKnum());
        if (!Utility.fieldEquals(ws.getKnumStatus(), "00")) {
            showMessage("Movement number assignment failed");
            return;
        }
        fileSet.getSmovf().setRecord();
        ws.setSmSeq(ws.getKnumNumber());
        ws.setSmDate(ws.getWkSysdate());
        ws.setSmProd(ws.getDrProd(ws.getWkIdx()));
        ws.setSmWhse(ws.getDrWhse(ws.getWkIdx()));
        ws.setSmKind(50);
        ws.setSmQty(BigDecimal.valueOf(ws.getWkLineQty()));
        ws.setSmUnitCost(ws.getWkLineCost());
        ws.setSmBalAfter(BigDecimal.valueOf(ws.getWkNewbal()));
        ws.setSmRefType(3);
        ws.setSmRefNo(ws.getWkInvNo());
        ws.setSmUser(ws.getWkUserCode());
        fileSet.getSmovf().write();
        ws.trySetString("FSTS", fileSet.getSmovf().getFileStatus());
        if (fileSet.getSmovf().isInvalidKey()) {
            showMessage("Movement write failed");
        }
    }

    /** COBOL paragraph: PAL-010 */
    private void postArLedgerEntry() {
        ws.setKnumKey("ARLDG");
        numgen(ws.getKnum());
        if (!Utility.fieldEquals(ws.getKnumStatus(), "00")) {
            showMessage("AR ledger number assignment failed");
            return;
        }
        fileSet.getArlf().setRecord();
        ws.setAlSeq(ws.getKnumNumber());
        ws.setAlCust(ws.getWkSelCust());
        ws.setAlDate(ws.getWkSysdate());
        ws.setAlCloseYm(ws.getWkCloseYm());
        ws.setAlKind(3);
        ws.setAlRefType(3);
        ws.setAlRefNo(ws.getWkInvNo());
        ws.setAlDebit(BigDecimal.ZERO);
        ws.setAlCredit(BigDecimal.valueOf(ws.getWkGrsTotal()));
        ws.setAlBalance(
                (ws.getCuBalance().subtract(BigDecimal.valueOf(ws.getWkGrsTotal())))
                        .setScale(0, java.math.RoundingMode.DOWN));
        ws.setAlRemark("Sales return");
        ws.setAlUser(ws.getWkUserCode());
        fileSet.getArlf().write();
        ws.trySetString("FSTS", fileSet.getArlf().getFileStatus());
        if (fileSet.getArlf().isInvalidKey()) {
            showMessage("AR ledger write failed");
        }
    }

    /** COBOL paragraph: UCB-010 */
    private void updateCustomerBalance() {
        ws.setCuCode(ws.getWkSelCust());
        readByFallbackKey(fileSet.getCustf(), "CU-CODE");
        if (fileSet.getCustf().isInvalidKey()) {
            return;
        }
        ws.setCuBalance(ws.getCuBalance().subtract(BigDecimal.valueOf(ws.getWkGrsTotal())));
        ws.setCuUpdDate(ws.getWkSysdate());
        ws.setCuUpdUser(ws.getWkUserCode());
        fileSet.getCustf().rewrite();
        ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
        if (fileSet.getCustf().isInvalidKey()) {
            showMessage("Customer balance update failed");
        }
    }

    /** COBOL paragraph: CLP-010 */
    private void calculatePageCount() {
        if (ws.getWkDcnt() == 0) {
            ws.setWkPageCnt(1);
        } else {
            ws.setWkPageCnt((((ws.getWkDcnt() + ws.getWkPgsize()) - 1) / ws.getWkPgsize()));
        }
    }

    /** COBOL paragraph: BW-010 */
    private void buildBrowseWindow() {
        for (ws.setWkIdx(1); ws.getWkIdx() <= ws.getWkPgsize(); ws.setWkIdx(ws.getWkIdx() + 1)) {
            ws.setWkIdx2(((ws.getWkPageTop() + ws.getWkIdx()) - 1));
            if (ws.getWkIdx2() <= ws.getWkDcnt() && ws.getWkIdx2() >= 1) {
                ws.setWwProd(ws.getWkIdx(), ws.getDrProd(ws.getWkIdx2()));
                ws.setWwName(ws.getWkIdx(), ws.getDrName(ws.getWkIdx2()));
                ws.setWwQty(ws.getWkIdx(), ws.getDrQty(ws.getWkIdx2()));
                ws.setWwPrice(ws.getWkIdx(), ws.getDrPrice(ws.getWkIdx2()));
                ws.setWwAmt(ws.getWkIdx(), ws.getDrAmt(ws.getWkIdx2()));
                ws.setWwCostamt(ws.getWkIdx(), ws.getDrCostamt(ws.getWkIdx2()));
            } else {
                ws.setWwProd(ws.getWkIdx(), 0);
                ws.setWwName(ws.getWkIdx(), " ");
                ws.setWwQty(ws.getWkIdx(), 0);
                ws.setWwPrice(ws.getWkIdx(), BigDecimal.ZERO);
                ws.setWwAmt(ws.getWkIdx(), 0);
                ws.setWwCostamt(ws.getWkIdx(), 0);
            }
        }
        ws.setWkPageNo((((ws.getWkPageTop() + ws.getWkPgsize()) - 1) / ws.getWkPgsize()));
    }

    /** COBOL paragraph: PRN-010 */
    private void renderReturnSummaryScreens() {
        displayHeaderFooter();
        renderer.displayScreen(ScreenDefs.getScreen("DS-INV"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-TOTAL"), ws);
    }

    /** COBOL paragraph: PD-010 */
    private void pageDownBrowse() {
        if ((ws.getWkPageTop() + ws.getWkPgsize()) > ws.getWkDcnt()) {
            return;
        }
        ws.setWkPageTop(ws.getWkPageTop() + ws.getWkPgsize());
        runChain(this::buildBrowseWindow);
    }

    /** COBOL paragraph: PU-010 */
    private void pageUpBrowse() {
        if (ws.getWkPageTop() <= 1) {
            return;
        }
        if (ws.getWkPageTop() > ws.getWkPgsize()) {
            ws.setWkPageTop(ws.getWkPageTop() - (ws.getWkPgsize()));
        } else {
            ws.setWkPageTop(1);
        }
        runChain(this::buildBrowseWindow);
    }

    /** COBOL paragraph: TERM-010 */
    private void closeAllFiles() {
        fileSet.getInvhf().close();
        ws.trySetString("FSTS", fileSet.getInvhf().getFileStatus());
        fileSet.getInvdf().close();
        ws.trySetString("FSTS", fileSet.getInvdf().getFileStatus());
        fileSet.getStokf().close();
        ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
        fileSet.getSmovf().close();
        ws.trySetString("FSTS", fileSet.getSmovf().getFileStatus());
        fileSet.getCustf().close();
        ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
        fileSet.getArlf().close();
        ws.trySetString("FSTS", fileSet.getArlf().getFileStatus());
        fileSet.getProdf().close();
        ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
        fileSet.getCprcf().close();
        ws.trySetString("FSTS", fileSet.getCprcf().getFileStatus());
    }

    /** COBOL paragraph: ABEND-010 */
    private void abortProgram() {
        ws.setKaProgid("SL0030");
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

    /**
     * Divides a date/year-month field into its parent and its trailing 2-digit component (COBOL
     * implicit PIC 9(2) shift).
     */
    private static final int DATE_COMPONENT_DIVISOR = 100;

    /** Maximum number of return detail lines a single transaction may carry. */
    private static final int MAX_RETURN_LINES = 200;

    /**
     * Opens a file for I/O, recreating it via an OUTPUT/CLOSE cycle when it does not exist yet
     * (status 35/30), then aborts the program if the file still cannot be opened.
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
            runChain(this::abortProgram);
        }
    }

    /**
     * Reads a record by its primary key, falling back to the key already in the record buffer when
     * the working-storage field is blank.
     */
    private void readByFallbackKey(RawDatasetBase file, String keyFieldName) {
        String rkVal = "";
        if (rkVal == null || rkVal.trim().isEmpty()) {
            try {
                rkVal = ws.getString(keyFieldName);
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

    /**
     * Reads a record by a two-part composite key, its parts pipe-joined per the FieldBinding key
     * convention.
     */
    private void readByCompositeKey(RawDatasetBase file, String keyField1, String keyField2) {
        StringBuilder rkSb = new StringBuilder();
        String rkPart0 = "";
        try {
            rkPart0 = ws.getString(keyField1);
        } catch (Exception _e) {
        }
        rkSb.append(rkPart0 != null ? rkPart0.trim() : "");
        String rkPart1 = "";
        try {
            rkPart1 = ws.getString(keyField2);
        } catch (Exception _e) {
        }
        rkSb.append('|');
        rkSb.append(rkPart1 != null ? rkPart1.trim() : "");
        file.readByKey(rkSb.toString());
        ws.trySetString("FSTS", file.getFileStatus());
    }

    /** Redisplays the shared header and footer screens. */
    private void displayHeaderFooter() {
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEADER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-FOOTER"), ws);
    }

    /** Sets the message line and redisplays the message screen. */
    private void showMessage(String message) {
        ws.setWkMsgLine(message);
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
    }

    /** Redisplays the message screen using the message already set on WK-MSG-LINE. */
    private void displayMessage() {
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
    }
}
