package com.sakura.taxcal.service;

import com.sakura.runtime.BatchServiceBase;
import com.sakura.runtime.DatasetEnums.*;
import com.sakura.runtime.ProgramExitSignal;
import com.sakura.runtime.Utility;
import com.sakura.runtime.io.AbstractDatasets;
import com.sakura.runtime.linkage.TaxcalLinkParm;
import com.sakura.taxcal.domain.TaxcalFieldAccess;
import com.sakura.taxcal.domain.WorkingStorage;
import com.sakura.taxcal.runtime.TaxcalDatasets;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/** Business logic service generated from COBOL program TAXCAL. */
@Service
@Scope("prototype")
public class TaxcalService extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final TaxcalDatasets fileSet;

    private final TaxcalLinkParm link = new TaxcalLinkParm();

    private final TaxcalFieldAccess ws;

    public TaxcalService(TaxcalDatasets fileSet) {
        this.fileSet = fileSet;
        this.ws = new TaxcalFieldAccess(new WorkingStorage(), fileSet);
    }

    /**
     * Program entry point — runs the first COBOL paragraph via runChain. Base class run() wraps
     * this in StopRun/Abort/Exception handling.
     */
    @Override
    protected void mainProcess() {
        // Import anchors for types referenced by emitted paragraph bodies:
        // StopRunSignal, JobAbortException, ParagraphJumpSignal, ProgramExitSignal
        runChain(this::calculateTax);
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
    private void calculateTax() {
        var ktax = link.getKtax();
        ktax.setKtStatus("00");
        ktax.setKtTax(BigDecimal.ZERO);
        ktax.setKtNet(BigDecimal.ZERO);
        ktax.setKtGross(BigDecimal.ZERO);
        if (ktax.getKtCategory() == 3 || ktax.getKtTaxType() == 3) {
            ktax.setKtNet(ktax.getKtAmount());
            ktax.setKtGross(ktax.getKtAmount());
            ktax.setKtRate(BigDecimal.ZERO);
            throw new ProgramExitSignal();
        }
        runChain(this::findApplicableTaxRate);
        ktax.setKtRate(ws.getWkRate());
        runChain(this::computeTaxAmounts);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: FIND-010 */
    private void findApplicableTaxRate() {
        ws.setWkFound(0);
        ws.setWkRate(BigDecimal.ZERO);
        fileSet.getTaxf().open(FileOpenMode.INPUT);
        if (!Utility.fieldEquals(fileSet.getTaxf().getFileStatus(), "00")) {
            runChain(this::applyDefaultTaxRate);
            return;
        }
        ws.setTxCode(link.getKtax().getKtCategory());
        ws.setTxStartDate(0);
        fileSet.getTaxf().start("TX-CODE TX-START-DATE", "NOT LESS THAN");
        if (fileSet.getTaxf().isInvalidKey()) {
            /* CONTINUE */
        }
        if (Utility.fieldEquals(fileSet.getTaxf().getFileStatus(), "00")) {
            runChain(this::scanForEffectiveRate);
        }
        fileSet.getTaxf().close();
        if (ws.getWkFound() == 0) {
            runChain(this::applyDefaultTaxRate);
        }
    }

    /** COBOL paragraph: SCAN-010 */
    private void scanForEffectiveRate() {
        var ktax = link.getKtax();
        while (true) {
            fileSet.getTaxf().readNext();
            if (fileSet.getTaxf().isAtEnd()) {
                return;
            }
            if (ws.getTxCode() != ktax.getKtCategory()) {
                return;
            }
            if (ws.getTxStartDate() <= ktax.getKtDate()) {
                ws.setWkRate(ws.getTxRate());
                ws.setWkFound(1);
            }
        }
    }

    /** COBOL paragraph: DEF-010 */
    private void applyDefaultTaxRate() {
        if (link.getKtax().getKtCategory() == 2) {
            ws.setWkRate(new BigDecimal("0.080"));
        } else {
            ws.setWkRate(new BigDecimal("0.100"));
        }
    }

    /** COBOL paragraph: CT-010 */
    private void computeTaxAmounts() {
        var ktax = link.getKtax();
        if (ktax.getKtTaxType() == 2) {
            ktax.setKtGross(ktax.getKtAmount());
            ws.setWkRaw(
                    (ktax.getKtGross()
                                    .multiply(ws.getWkRate())
                                    .divide(
                                            BigDecimal.valueOf(1).add(ws.getWkRate()),
                                            12,
                                            java.math.RoundingMode.HALF_UP))
                            .setScale(5, java.math.RoundingMode.DOWN));
            runChain(this::roundTaxAmount);
            ktax.setKtTax(BigDecimal.valueOf(ws.getWkInt()));
            ktax.setKtNet(
                    (ktax.getKtGross().subtract(ktax.getKtTax()))
                            .setScale(0, java.math.RoundingMode.DOWN));
        } else {
            ktax.setKtNet(ktax.getKtAmount());
            ws.setWkRaw(
                    (ktax.getKtNet().multiply(ws.getWkRate()))
                            .setScale(5, java.math.RoundingMode.DOWN));
            runChain(this::roundTaxAmount);
            ktax.setKtTax(BigDecimal.valueOf(ws.getWkInt()));
            ktax.setKtGross(
                    (ktax.getKtNet().add(ktax.getKtTax()))
                            .setScale(0, java.math.RoundingMode.DOWN));
        }
    }

    /** COBOL paragraph: AR-010 */
    private void roundTaxAmount() {
        ws.setWkInt(ws.getWkRaw().longValue());
        ws.setWkFrac(
                (ws.getWkRaw().subtract(BigDecimal.valueOf(ws.getWkInt())))
                        .setScale(5, java.math.RoundingMode.DOWN));
        switch (link.getKtax().getKtRound()) {
            case 2 -> {
                /* CONTINUE */
            }
            case 3 -> {
                if ((ws.getWkFrac().signum() > 0)) {
                    ws.setWkInt(ws.getWkInt() + 1);
                }
            }
            default -> {
                if ((ws.getWkFrac().compareTo(new BigDecimal("0.5")) >= 0)) {
                    ws.setWkInt(ws.getWkInt() + 1);
                }
            }
        }
    }

    /**
     * Execute this program as a COBOL CALL callee. Copies params into Linkage section, runs
     * business logic, copies back.
     */
    public void execute(TaxcalLinkParm params) {
        copyKtax(params.getKtax(), link.getKtax());
        executeSubprogram(this::calculateTax);
        copyKtax(link.getKtax(), params.getKtax());
    }

    /** Copy all KTAX linkage fields from src into dest. */
    private static void copyKtax(TaxcalLinkParm.Ktax src, TaxcalLinkParm.Ktax dest) {
        dest.setKtCategory(src.getKtCategory());
        dest.setKtTaxType(src.getKtTaxType());
        dest.setKtRound(src.getKtRound());
        dest.setKtDate(src.getKtDate());
        dest.setKtAmount(src.getKtAmount());
        dest.setKtTax(src.getKtTax());
        dest.setKtNet(src.getKtNet());
        dest.setKtGross(src.getKtGross());
        dest.setKtRate(src.getKtRate());
        dest.setKtStatus(src.getKtStatus());
    }
}
