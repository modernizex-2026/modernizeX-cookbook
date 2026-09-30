package com.sakura.sl0040.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
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
import com.sakura.runtime.linkage.TaxcalLinkParm;
import com.sakura.runtime.record.RawDatasetBase;
import com.sakura.sl0040.domain.Sl0040FieldAccess;
import com.sakura.sl0040.domain.WorkingStorage;
import com.sakura.sl0040.runtime.Sl0040Datasets;
import com.sakura.sl0040.screen.ScreenDefs;
import com.sakura.taxcal.service.TaxcalService;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/** Business logic service generated from COBOL program SL0040. */
@Service
@Scope("prototype")
public class Sl0040Service extends BatchServiceBase {
    /** Scale factor separating the year from the month within a YYYYMM-encoded value. */
    private static final int DATE_YYYYMM_SCALE = 100;

    /** Shared file instances for all FD files in this program. */
    private final Sl0040Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL TAXCAL. */
    private TaxcalService taxcalService;

    /** Injected service for COBOL CALL NUMGEN. */
    private NumgenService numgenService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Sl0040FieldAccess ws;

    public Sl0040Service(
            Sl0040Datasets fileSet,
            DateutService dateutService,
            TaxcalService taxcalService,
            NumgenService numgenService,
            AbortxService abortxService,
            ScreenRendererInstance renderer) {
        this.fileSet = fileSet;
        this.ws = new Sl0040FieldAccess(new WorkingStorage(), fileSet);
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
            runChain(this::runMainScreenCycle);
        }
        runChain(this::closeAllFiles);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeProgram() {
        ws.setWkProgid("SL0040");
        ws.setWkTitle("Sales Credit Note");
        ws.setWkFkeyLine("ENTER=Read  PF3=End");
        ws.setKdFunc("TODY");
        dateut(ws.getKdate());
        ws.setWkSysymd(ws.getKdDate1());
        ws.setWkSysdate(ws.getKdDate1());
        runChain(this::openAllFiles);
    }

    /** COBOL paragraph: OPENF-010 */
    private void openAllFiles() {
        runChain(this::oiihZ010);
        runChain(this::oiidZ010);
        runChain(this::oistZ010);
        runChain(this::oismZ010);
        runChain(this::oicuZ010);
        runChain(this::oiarZ010);
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

    /** COBOL paragraph: OIIH-010 */
    private void oiihZ010() {
        openFileWithRetryOrAbend(fileSet.getInvhf(), "INVHF");
    }

    /** COBOL paragraph: OIID-010 */
    private void oiidZ010() {
        openFileWithRetryOrAbend(fileSet.getInvdf(), "INVDF");
    }

    /** COBOL paragraph: OIST-010 */
    private void oistZ010() {
        openFileWithRetryOrAbend(fileSet.getStokf(), "STOKF");
    }

    /** COBOL paragraph: OISM-010 */
    private void oismZ010() {
        openFileWithRetryOrAbend(fileSet.getSmovf(), "SMOVF");
    }

    /** COBOL paragraph: OICU-010 */
    private void oicuZ010() {
        openFileWithRetryOrAbend(fileSet.getCustf(), "CUSTF");
    }

    /** COBOL paragraph: OIAR-010 */
    private void oiarZ010() {
        openFileWithRetryOrAbend(fileSet.getArlf(), "ARLF");
    }

    /** COBOL paragraph: MAINR-010 */
    private void runMainScreenCycle() {
        runChain(this::clearCreditNoteFields);
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEADER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-FOOTER"), ws);
        ws.setWkMsgLine("Enter the original invoice number to credit");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-KEY"), ws);
        String scValWkOrigInv0 =
                Utility.acceptScreen(
                        "WK-ORIG-INV",
                        () -> renderer.acceptField(ScreenDefs.getInput("WK-ORIG-INV")));
        ws.setWkOrigInv(Utility.parseLongOr(scValWkOrigInv0.trim(), 0L));
        broadcastEstsStatus();
        switch (String.valueOf(ws.getEsts())) {
            case "03" -> {
                ws.setString("END-FLG", "1");
            }
            case "00" -> {
                runChain(this::processCreditNote);
            }
            default -> {
                ws.setWkMsgLine("Invalid function key");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: CLRC-010 */
    private void clearCreditNoteFields() {
        ws.setWkOrigInv(0);
        ws.setWkSelCust(0);
        ws.setWkInvNo(0);
        ws.setWkScnt(0);
        ws.setWkCrLines(0);
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

    /** COBOL paragraph: PC-010 */
    private void processCreditNote() {
        if (ws.getWkOrigInv() == 0) {
            ws.setWkMsgLine("Invoice number required");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        runChain(this::readSourceInvoice);
        if ((ws.getErrFlg() == 1)) {
            return;
        }
        runChain(this::readCustomer);
        if ((ws.getFoundFlg() != 1)) {
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        runChain(this::startSourceLineList);
        if (ws.getWkScnt() == 0) {
            ws.setWkMsgLine("Source invoice has no lines");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        runChain(this::runPickLoop);
        runChain(this::computeCreditTotals);
        if (ws.getWkCrLines() == 0) {
            ws.setWkMsgLine("No quantities picked - credit discarded");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        runChain(this::confirmAndPostCredit);
    }

    /** COBOL paragraph: RSI-010 */
    private void readSourceInvoice() {
        ws.setErrFlg(0);
        ws.setIhNo(ws.getWkOrigInv());
        fileSet.getInvhf().readByKey(resolveFallbackKey("IH-NO", fileSet.getInvhf()));
        ws.trySetString("FSTS", fileSet.getInvhf().getFileStatus());
        if (fileSet.getInvhf().isInvalidKey()) {
            ws.setWkMsgLine("Original invoice not found");
            ws.setString("ERR-FLG", "1");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        if (ws.getIhDelFlag() == 1) {
            ws.setWkMsgLine("Original invoice is deleted");
            ws.setString("ERR-FLG", "1");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        if (ws.getIhKind() != 1) {
            ws.setWkMsgLine("Source is not a sale - cannot credit");
            ws.setString("ERR-FLG", "1");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        if (ws.getIhStatus() == 9) {
            ws.setWkMsgLine("Original invoice is cancelled");
            ws.setString("ERR-FLG", "1");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setWkSelCust(ws.getIhCust());
        ws.setWkStaffIn(ws.getIhStaff());
        ws.setWkTaxtypeIn(ws.getIhTaxType());
    }

    /** COBOL paragraph: RC-010 */
    private void readCustomer() {
        ws.setFoundFlg(0);
        ws.setWkCustName(" ");
        ws.setCuCode(ws.getWkSelCust());
        fileSet.getCustf().readByKey(resolveFallbackKey("CU-CODE", fileSet.getCustf()));
        ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
        if (fileSet.getCustf().isInvalidKey()) {
            ws.setWkMsgLine("Customer of invoice not found");
            return;
        }
        ws.setWkCustName(ws.getCuName());
        if (ws.getWkTaxtypeIn() < 1 || ws.getWkTaxtypeIn() > 3) {
            ws.setWkTaxtypeIn(ws.getCuTaxType());
        }
        ws.setString("FOUND-FLG", "1");
    }

    /** COBOL paragraph: LSL-010 */
    private void startSourceLineList() {
        ws.setWkScnt(0);
        ws.setIdNo(ws.getWkOrigInv());
        ws.setIdLine(0);
        fileSet.getInvdf().start("ID-NO", "NOT LESS THAN");
        ws.trySetString("FSTS", fileSet.getInvdf().getFileStatus());
        if (fileSet.getInvdf().isInvalidKey()) {
            return;
        }
        // fall-through to next paragraph
        loadSourceLineList();
    }

    /** COBOL paragraph: LSL-020 */
    private void loadSourceLineList() {
        while (true) {
            fileSet.getInvdf().readNext();
            ws.trySetString("FSTS", fileSet.getInvdf().getFileStatus());
            if (fileSet.getInvdf().isAtEnd()) {
                return;
            }
            if (ws.getIdNo() != ws.getWkOrigInv()) {
                return;
            }
            if (ws.getWkScnt() >= 200) {
                return;
            }
            ws.setWkScnt(ws.getWkScnt() + 1);
            ws.setSrLine(ws.getWkScnt(), ws.getIdLine());
            ws.setSrProd(ws.getWkScnt(), ws.getIdProd());
            ws.setSrWhse(ws.getWkScnt(), ws.getIdWhse());
            if ((ws.getIdQty().signum() < 0)) {
                ws.setSrOqty(ws.getWkScnt(), BigDecimal.ZERO.subtract(ws.getIdQty()).intValue());
            } else {
                ws.setSrOqty(ws.getWkScnt(), ws.getIdQty().intValue());
            }
            ws.setSrCrQty(ws.getWkScnt(), 0);
            ws.setSrPrice(ws.getWkScnt(), ws.getIdUnitPrice());
            ws.setSrCost(ws.getWkScnt(), ws.getIdUnitCost());
            ws.setSrTaxcat(ws.getWkScnt(), ws.getIdTaxCategory());
            ws.setPrCode(ws.getIdProd());
            fileSet.getProdf().readByKey(resolveFallbackKey("PR-CODE", fileSet.getProdf()));
            ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
            if (fileSet.getProdf().isInvalidKey()) {
                ws.setSrName(ws.getWkScnt(), "(unknown)");
                ws.setSrStkmng(ws.getWkScnt(), 0);
            }
            if (!fileSet.getProdf().isInvalidKey()) {
                ws.setSrName(ws.getWkScnt(), ws.getPrName());
                ws.setSrStkmng(ws.getWkScnt(), ws.getPrStockMng());
            }
        }
    }

    /** COBOL paragraph: PKL-010 */
    private void runPickLoop() {
        ws.setWkPickEnd(0);
        ws.setWkPageTop(1);
        ws.setWkFkeyLine("ENTER=Set  PF4=ClrLine  PF6/PF12=Page  PF3=Done");
        runChain(this::countCreditLines);
        runChain(this::computeCreditTotals);
        runChain(this::computeListPaging);
        runChain(this::buildPickListWindow);
        ws.setWkMsgLine("Pick a line and its credit quantity");
        while (!((ws.getWkPickEnd() == 1) || (ws.getEndFlg() == 1))) {
            runChain(this::displayPickScreens);
            ws.setWkPickLine(0);
            ws.setWkPickQty(0);
            renderer.displayScreen(ScreenDefs.getScreen("DS-PICK"), ws);
            String scValWkPickLine4 =
                    Utility.acceptScreen(
                            "WK-PICK-LINE",
                            () -> renderer.acceptField(ScreenDefs.getInput("WK-PICK-LINE")));
            ws.setWkPickLine(Utility.parseIntOr(scValWkPickLine4.trim(), 0));
            String scValWkPickQty5 =
                    Utility.acceptScreen(
                            "WK-PICK-QTY",
                            () -> renderer.acceptField(ScreenDefs.getInput("WK-PICK-QTY")));
            ws.setWkPickQty(Utility.parseIntOr(scValWkPickQty5.trim(), 0));
            broadcastEstsStatus();
            switch (String.valueOf(ws.getEsts())) {
                case "03" -> {
                    ws.setString("WK-PICK-END", "1");
                }
                case "06" -> {
                    runChain(this::pageDown);
                }
                case "12" -> {
                    runChain(this::pageUp);
                }
                case "04" -> {
                    runChain(this::clearPickedQuantity);
                }
                case "00" -> {
                    runChain(this::setPickedQuantity);
                }
                default -> {
                    ws.setWkMsgLine("Invalid function key");
                }
            }
            runChain(this::countCreditLines);
            runChain(this::computeCreditTotals);
            runChain(this::buildPickListWindow);
        }
        ws.setWkFkeyLine("ENTER=Read  PF3=End");
    }

    /** COBOL paragraph: SPQ-010 */
    private void setPickedQuantity() {
        if (ws.getWkPickLine() == 0 || ws.getWkPickLine() > ws.getWkScnt()) {
            ws.setWkMsgLine("Enter a valid source line number");
            return;
        }
        ws.setWkIdx(ws.getWkPickLine());
        if (ws.getWkPickQty() <= 0) {
            ws.setWkMsgLine("Credit qty must be positive");
            return;
        }
        if (ws.getWkPickQty() > ws.getSrOqty(ws.getWkIdx())) {
            ws.setWkMsgLine("Credit qty exceeds invoiced qty");
            return;
        }
        ws.setSrCrQty(ws.getWkIdx(), ws.getWkPickQty());
        ws.setWkMsgLine("Credit quantity set");
    }

    /** COBOL paragraph: CPL-010 */
    private void clearPickedQuantity() {
        if (ws.getWkPickLine() == 0 || ws.getWkPickLine() > ws.getWkScnt()) {
            ws.setWkMsgLine("Enter a valid source line number");
            return;
        }
        ws.setWkIdx(ws.getWkPickLine());
        ws.setSrCrQty(ws.getWkIdx(), 0);
        ws.setWkMsgLine("Credit quantity cleared");
    }

    /** COBOL paragraph: CCR-010 */
    private void countCreditLines() {
        ws.setWkCrLines(0);
        for (ws.setWkIdx(1); ws.getWkIdx() <= ws.getWkScnt(); ws.setWkIdx(ws.getWkIdx() + 1)) {
            if (ws.getSrCrQty(ws.getWkIdx()) > 0) {
                ws.setWkCrLines(ws.getWkCrLines() + 1);
            }
        }
    }

    /** COBOL paragraph: CT-010 */
    private void computeCreditTotals() {
        ws.setWkNetTotal(0);
        ws.setWkCostTotal(0);
        for (ws.setWkIdx(1); ws.getWkIdx() <= ws.getWkScnt(); ws.setWkIdx(ws.getWkIdx() + 1)) {
            if (ws.getSrCrQty(ws.getWkIdx()) > 0) {
                ws.setWkNetTotal(
                        BigDecimal.valueOf(ws.getWkNetTotal())
                                .add(
                                        BigDecimal.valueOf(ws.getSrCrQty(ws.getWkIdx()))
                                                .multiply(ws.getSrPrice(ws.getWkIdx())))
                                .longValue());
                ws.setWkCostTotal(
                        BigDecimal.valueOf(ws.getWkCostTotal())
                                .add(
                                        BigDecimal.valueOf(ws.getSrCrQty(ws.getWkIdx()))
                                                .multiply(ws.getSrCost(ws.getWkIdx())))
                                .longValue());
            }
        }
        ws.setKtCategory(1);
        ws.setKtTaxType(ws.getWkTaxtypeIn());
        if (ws.getKtTaxType() < 1 || ws.getKtTaxType() > 3) {
            ws.setKtTaxType(1);
        }
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

    /** COBOL paragraph: AC-010 */
    private void confirmAndPostCredit() {
        ws.setWkConfirm(" ");
        runChain(this::computeListPaging);
        runChain(this::buildPickListWindow);
        runChain(this::displayPickScreens);
        ws.setWkMsgLine("Confirm to post the credit note");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-CONFIRM"), ws);
        String scValWkConfirm6 =
                Utility.acceptScreen(
                        "SC-CONF", () -> renderer.acceptField(ScreenDefs.getInput("SC-CONF")));
        ws.setWkConfirm(scValWkConfirm6);
        broadcastEstsStatus();
        if ((ws.getWkConfirm().equals("Y") || ws.getWkConfirm().equals("y"))) {
            runChain(this::postCreditNote);
        } else {
            ws.setWkMsgLine("Credit note discarded");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
    }

    /** COBOL paragraph: PST-010 */
    private void postCreditNote() {
        ws.setKnumKey("INVOICE");
        numgen(ws.getKnum());
        if (!Utility.fieldEquals(ws.getKnumStatus(), "00")) {
            ws.setWkMsgLine("Credit number assignment failed");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setWkInvNo(ws.getKnumNumber());
        runChain(this::computeCloseYearMonth);
        runChain(this::writeCreditHeader);
        runChain(this::writeCreditLines);
        runChain(this::writeArLedgerEntry);
        runChain(this::updateCustomerBalance);
        ws.setWkMsgLine(
                "Credit note " + String.format("%010d", (long) ws.getWkInvNo()) + " posted");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
    }

    /** COBOL paragraph: CCY-010 */
    private void computeCloseYearMonth() {
        ws.setWkYm((ws.getWkSysdate() / DATE_YYYYMM_SCALE));
        ws.setWkDay((ws.getWkSysdate() - (ws.getWkYm() * DATE_YYYYMM_SCALE)));
        ws.setWkCloseYm(ws.getWkYm());
        if (ws.getCuCloseDay() > 0 && ws.getCuCloseDay() < 99) {
            if (ws.getWkDay() > ws.getCuCloseDay()) {
                ws.setWkYyyy((ws.getWkYm() / DATE_YYYYMM_SCALE));
                ws.setWkMm((ws.getWkYm() - (ws.getWkYyyy() * DATE_YYYYMM_SCALE)));
                ws.setWkMm(ws.getWkMm() + 1);
                if (ws.getWkMm() > 12) {
                    ws.setWkMm(1);
                    ws.setWkYyyy(ws.getWkYyyy() + 1);
                }
                ws.setWkCloseYm(((ws.getWkYyyy() * DATE_YYYYMM_SCALE) + ws.getWkMm()));
            }
        }
    }

    /** COBOL paragraph: WCH-010 */
    private void writeCreditHeader() {
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
        ws.setIhLines(ws.getWkCrLines());
        ws.setIhRemark("Credit vs invoice " + String.format("%010d", (long) ws.getWkOrigInv()));
        ws.setIhAddDate(ws.getWkSysdate());
        ws.setIhUpdDate(ws.getWkSysdate());
        ws.setIhAddUser(ws.getWkUserCode());
        ws.setIhUpdUser(ws.getWkUserCode());
        ws.setIhDelFlag(0);
        fileSet.getInvhf().write();
        ws.trySetString("FSTS", fileSet.getInvhf().getFileStatus());
        if (fileSet.getInvhf().isInvalidKey()) {
            ws.setWkMsgLine("Credit header write failed");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
    }

    /** COBOL paragraph: WCD-010 */
    private void writeCreditLines() {
        ws.setWkOutLine(0);
        for (ws.setWkIdx(1); ws.getWkIdx() <= ws.getWkScnt(); ws.setWkIdx(ws.getWkIdx() + 1)) {
            if (ws.getSrCrQty(ws.getWkIdx()) > 0) {
                runChain(this::writeOneCreditLine);
            }
        }
    }

    /** COBOL paragraph: WOC-010 */
    private void writeOneCreditLine() {
        ws.setWkOutLine(ws.getWkOutLine() + 1);
        fileSet.getInvdf().setRecord();
        ws.setIdNo(ws.getWkInvNo());
        ws.setIdLine(ws.getWkOutLine());
        ws.setIdProd(ws.getSrProd(ws.getWkIdx()));
        ws.setIdWhse(ws.getSrWhse(ws.getWkIdx()));
        ws.setIdQty(
                (BigDecimal.ZERO.subtract(BigDecimal.valueOf(ws.getSrCrQty(ws.getWkIdx()))))
                        .setScale(0, java.math.RoundingMode.DOWN));
        ws.setIdUnitPrice(ws.getSrPrice(ws.getWkIdx()));
        ws.setIdAmount(
                (BigDecimal.ZERO.subtract(
                                BigDecimal.valueOf(ws.getSrCrQty(ws.getWkIdx()))
                                        .multiply(ws.getSrPrice(ws.getWkIdx()))))
                        .setScale(0, java.math.RoundingMode.DOWN));
        ws.setIdUnitCost(ws.getSrCost(ws.getWkIdx()));
        ws.setIdCostAmount(
                (BigDecimal.ZERO.subtract(
                                BigDecimal.valueOf(ws.getSrCrQty(ws.getWkIdx()))
                                        .multiply(ws.getSrCost(ws.getWkIdx()))))
                        .setScale(0, java.math.RoundingMode.DOWN));
        ws.setIdTaxCategory(ws.getSrTaxcat(ws.getWkIdx()));
        ws.setIdRemark(" ");
        fileSet.getInvdf().write();
        ws.trySetString("FSTS", fileSet.getInvdf().getFileStatus());
        if (fileSet.getInvdf().isInvalidKey()) {
            /* CONTINUE */
        }
        runChain(this::adjustStockOnHand);
        runChain(this::writeStockMovement);
    }

    /** COBOL paragraph: IS-010 */
    private void adjustStockOnHand() {
        ws.setWkStkFound(0);
        ws.setWkLineQty(ws.getSrCrQty(ws.getWkIdx()));
        ws.setWkLineCost(ws.getSrCost(ws.getWkIdx()));
        if (ws.getSrStkmng(ws.getWkIdx()) == 0) {
            return;
        }
        ws.setSkProd(ws.getSrProd(ws.getWkIdx()));
        ws.setSkWhse(ws.getSrWhse(ws.getWkIdx()));
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
            ws.setWkMsgLine("Stock update failed");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
    }

    /** COBOL paragraph: CRS-010 */
    private void createStockRecord() {
        fileSet.getStokf().setRecord();
        ws.setSkProd(ws.getSrProd(ws.getWkIdx()));
        ws.setSkWhse(ws.getSrWhse(ws.getWkIdx()));
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
            ws.setWkMsgLine("Stock create failed");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
        if (!fileSet.getStokf().isInvalidKey()) {
            ws.setWkStkFound(1);
        }
    }

    /** COBOL paragraph: WM-010 */
    private void writeStockMovement() {
        if (ws.getSrStkmng(ws.getWkIdx()) == 0 || ws.getWkStkFound() == 0) {
            return;
        }
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
        ws.setSmProd(ws.getSrProd(ws.getWkIdx()));
        ws.setSmWhse(ws.getSrWhse(ws.getWkIdx()));
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
            ws.setWkMsgLine("Movement write failed");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
    }

    /** COBOL paragraph: PAL-010 */
    private void writeArLedgerEntry() {
        ws.setKnumKey("ARLDG");
        numgen(ws.getKnum());
        if (!Utility.fieldEquals(ws.getKnumStatus(), "00")) {
            ws.setWkMsgLine("AR ledger number assignment failed");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
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
        ws.setAlRemark("Sales credit note");
        ws.setAlUser(ws.getWkUserCode());
        fileSet.getArlf().write();
        ws.trySetString("FSTS", fileSet.getArlf().getFileStatus());
        if (fileSet.getArlf().isInvalidKey()) {
            ws.setWkMsgLine("AR ledger write failed");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
    }

    /** COBOL paragraph: UCB-010 */
    private void updateCustomerBalance() {
        ws.setCuCode(ws.getWkSelCust());
        fileSet.getCustf().readByKey(resolveFallbackKey("CU-CODE", fileSet.getCustf()));
        ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
        if (fileSet.getCustf().isInvalidKey()) {
            return;
        }
        ws.setCuBalance(ws.getCuBalance().subtract(BigDecimal.valueOf(ws.getWkGrsTotal())));
        ws.setCuUpdDate(ws.getWkSysdate());
        ws.setCuUpdUser(ws.getWkUserCode());
        fileSet.getCustf().rewrite();
        ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
        if (fileSet.getCustf().isInvalidKey()) {
            ws.setWkMsgLine("Customer balance update failed");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
    }

    /** COBOL paragraph: CLP-010 */
    private void computeListPaging() {
        if (ws.getWkScnt() == 0) {
            ws.setWkPageCnt(1);
        } else {
            ws.setWkPageCnt((((ws.getWkScnt() + ws.getWkPgsize()) - 1) / ws.getWkPgsize()));
        }
        if (ws.getWkPageTop() > ws.getWkScnt()) {
            ws.setWkPageTop(1);
        }
    }

    /** COBOL paragraph: BLW-010 */
    private void buildPickListWindow() {
        for (ws.setWkIdx(1); ws.getWkIdx() <= ws.getWkPgsize(); ws.setWkIdx(ws.getWkIdx() + 1)) {
            ws.setWkIdx2(((ws.getWkPageTop() + ws.getWkIdx()) - 1));
            if (ws.getWkIdx2() <= ws.getWkScnt() && ws.getWkIdx2() >= 1) {
                ws.setWwLine(ws.getWkIdx(), ws.getWkIdx2());
                ws.setWwProd(ws.getWkIdx(), ws.getSrProd(ws.getWkIdx2()));
                ws.setWwName(ws.getWkIdx(), ws.getSrName(ws.getWkIdx2()));
                ws.setWwOqty(ws.getWkIdx(), ws.getSrOqty(ws.getWkIdx2()));
                ws.setWwCqty(ws.getWkIdx(), ws.getSrCrQty(ws.getWkIdx2()));
                ws.setWwPrice(ws.getWkIdx(), ws.getSrPrice(ws.getWkIdx2()));
                ws.setWwAmt(
                        ws.getWkIdx(),
                        BigDecimal.valueOf(ws.getSrCrQty(ws.getWkIdx2()))
                                .multiply(ws.getSrPrice(ws.getWkIdx2()))
                                .longValue());
            } else {
                ws.setWwLine(ws.getWkIdx(), 0);
                ws.setWwProd(ws.getWkIdx(), 0);
                ws.setWwName(ws.getWkIdx(), " ");
                ws.setWwOqty(ws.getWkIdx(), 0);
                ws.setWwCqty(ws.getWkIdx(), 0);
                ws.setWwPrice(ws.getWkIdx(), BigDecimal.ZERO);
                ws.setWwAmt(ws.getWkIdx(), 0);
            }
        }
        ws.setWkPageNo((((ws.getWkPageTop() + ws.getWkPgsize()) - 1) / ws.getWkPgsize()));
    }

    /** COBOL paragraph: PGD-010 */
    private void pageDown() {
        if ((ws.getWkPageTop() + ws.getWkPgsize()) > ws.getWkScnt()) {
            ws.setWkMsgLine("Last page");
            return;
        }
        ws.setWkPageTop(ws.getWkPageTop() + ws.getWkPgsize());
        ws.setWkMsgLine(" ");
    }

    /** COBOL paragraph: PGU-010 */
    private void pageUp() {
        if (ws.getWkPageTop() <= 1) {
            ws.setWkMsgLine("First page");
            return;
        }
        if (ws.getWkPageTop() > ws.getWkPgsize()) {
            ws.setWkPageTop(ws.getWkPageTop() - (ws.getWkPgsize()));
        } else {
            ws.setWkPageTop(1);
        }
        ws.setWkMsgLine(" ");
    }

    /** COBOL paragraph: PNT-010 */
    private void displayPickScreens() {
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEADER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-FOOTER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-INFO"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-LIST"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-PSTAT"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
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
    }

    /** COBOL paragraph: ABEND-010 */
    private void abortProgram() {
        ws.setKaProgid("SL0040");
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
     * Resolves a fallback-style read key: prefer the WS field value, else fall back to the file's
     * current-record key.
     */
    private String resolveFallbackKey(String wsFieldName, RawDatasetBase file) {
        String key = "";
        if (key == null || key.trim().isEmpty()) {
            try {
                key = ws.getString(wsFieldName);
            } catch (Exception e) {
            }
        }
        if (key == null || key.trim().isEmpty()) {
            try {
                key = file.extractKeyFromCurrentRecord();
            } catch (Exception e) {
            }
        }
        return key != null ? key.trim() : "";
    }

    /**
     * Opens a file for I/O, retrying via a create-then-reopen cycle if it does not yet exist (FSTS
     * 35/30), then aborts the program if the file is still unusable.
     */
    private void openFileWithRetryOrAbend(RawDatasetBase file, String fileName) {
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
}
