package com.sakura.ms0010.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.ms0010.domain.Ms0010FieldAccess;
import com.sakura.ms0010.domain.WorkingStorage;
import com.sakura.ms0010.runtime.Ms0010Datasets;
import com.sakura.ms0010.screen.ScreenDefs;
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

/** Business logic service generated from COBOL program MS0010. */
@Service
@Scope("prototype")
public class Ms0010Service extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final Ms0010Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Ms0010FieldAccess ws;

    public Ms0010Service(
            Ms0010Datasets fileSet,
            DateutService dateutService,
            AbortxService abortxService,
            ScreenRendererInstance renderer) {
        this.fileSet = fileSet;
        this.ws = new Ms0010FieldAccess(new WorkingStorage(), fileSet);
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
            runChain(this::processMainScreenCycle);
        }
        runChain(this::closeAllFiles);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeProgram() {
        ws.setWkProgid("MS0010");
        ws.setWkTitle("Customer Master Maintenance");
        ws.setWkFkeyLine("ENTER=Read  PF9=Delete  PF3=End");
        ws.setKdFunc("TODY");
        dateut(ws.getKdate());
        ws.setWkSysymd(ws.getKdDate1());
        ws.setWkSysdate(ws.getKdDate1());
        fileSet.getCustf().open(FileOpenMode.IO);
        ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
        if (Utility.fieldEquals(ws.getFsts(), "35") || Utility.fieldEquals(ws.getFsts(), "30")) {
            fileSet.getCustf().open(FileOpenMode.OUTPUT);
            ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
            fileSet.getCustf().close();
            ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
            fileSet.getCustf().open(FileOpenMode.IO);
            ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
        }
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("CUSTF");
            runChain(this::abortOnFileOpenError);
        }
        fileSet.getRegnf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getRegnf().getFileStatus());
        fileSet.getStaff().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getStaff().getFileStatus());
        fileSet.getBankf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getBankf().getFileStatus());
    }

    /** COBOL paragraph: MAIN-RTN-010 */
    private void processMainScreenCycle() {
        runChain(this::clearCustomerWorkFields);
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEADER"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-FOOTER"), ws);
        ws.setWkMsgLine(" ");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-KEY"), ws);
        String scValCuCode0 =
                Utility.acceptScreen(
                        "SC-KEY", () -> renderer.acceptField(ScreenDefs.getInput("SC-KEY")));
        ws.setCuCode(Utility.parseIntOr(scValCuCode0.trim(), 0));
        broadcastEstsStatus();
        switch (String.valueOf(ws.getEsts())) {
            case "03" -> {
                ws.setString("END-FLG", "1");
            }
            case "00" -> {
                runChain(this::lookupCustomerByCode);
            }
            default -> {
                ws.setWkMsgLine("Invalid function key");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: CLEAR-010 */
    private void clearCustomerWorkFields() {
        fileSet.getCustf().setRecord();
        ws.setWkRegionName(" ");
        ws.setWkStaffName(" ");
        ws.setWkBankName(" ");
        ws.setWkConfirm(" ");
    }

    /** COBOL paragraph: PKEY-010 */
    private void lookupCustomerByCode() {
        if (ws.getCuCode() == 0) {
            ws.setWkMsgLine("Customer code must not be zero");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        ws.setWkSaveCode(ws.getCuCode());
        readMasterByKeyWithFallback(fileSet.getCustf(), "CU-CODE");
        if (fileSet.getCustf().isInvalidKey()) {
            runChain(this::prepareNewCustomerAdd);
        } else {
            runChain(this::prepareCustomerChange);
        }
        runChain(this::acceptCustomerDetailFields);
    }

    /** COBOL paragraph: SADD-010 */
    private void prepareNewCustomerAdd() {
        ws.setString("MODE-FLG", "1");
        fileSet.getCustf().setRecord();
        ws.setCuCode(ws.getWkSaveCode());
        ws.setWkMsgLine("New customer - enter details");
    }

    /** COBOL paragraph: SCHG-010 */
    private void prepareCustomerChange() {
        if (ws.getCuDelFlag() == 1) {
            ws.setString("MODE-FLG", "1");
            ws.setWkMsgLine("Deleted customer - re-registering");
        } else {
            ws.setString("MODE-FLG", "2");
            ws.setWkMsgLine("Existing customer - change or PF9 delete");
        }
        runChain(this::lookupRelatedNames);
    }

    /** COBOL paragraph: EDIT-010 */
    private void acceptCustomerDetailFields() {
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-BODY"), ws);
        String scValCuName2 =
                Utility.acceptScreen(
                        "CU-NAME", () -> renderer.acceptField(ScreenDefs.getInput("CU-NAME")));
        ws.setCuName(scValCuName2);
        String scValCuKana3 =
                Utility.acceptScreen(
                        "CU-KANA", () -> renderer.acceptField(ScreenDefs.getInput("CU-KANA")));
        ws.setCuKana(scValCuKana3);
        String scValCuZip4 =
                Utility.acceptScreen(
                        "CU-ZIP", () -> renderer.acceptField(ScreenDefs.getInput("CU-ZIP")));
        ws.setCuZip(scValCuZip4);
        String scValCuAddr15 =
                Utility.acceptScreen(
                        "CU-ADDR1", () -> renderer.acceptField(ScreenDefs.getInput("CU-ADDR1")));
        ws.setCuAddr1(scValCuAddr15);
        String scValCuAddr26 =
                Utility.acceptScreen(
                        "CU-ADDR2", () -> renderer.acceptField(ScreenDefs.getInput("CU-ADDR2")));
        ws.setCuAddr2(scValCuAddr26);
        String scValCuTel7 =
                Utility.acceptScreen(
                        "CU-TEL", () -> renderer.acceptField(ScreenDefs.getInput("CU-TEL")));
        ws.setCuTel(scValCuTel7);
        String scValCuFax8 =
                Utility.acceptScreen(
                        "CU-FAX", () -> renderer.acceptField(ScreenDefs.getInput("CU-FAX")));
        ws.setCuFax(scValCuFax8);
        String scValCuRegion9 =
                Utility.acceptScreen(
                        "CU-REGION", () -> renderer.acceptField(ScreenDefs.getInput("CU-REGION")));
        ws.setCuRegion(Utility.parseIntOr(scValCuRegion9.trim(), 0));
        String scValCuStaff10 =
                Utility.acceptScreen(
                        "CU-STAFF", () -> renderer.acceptField(ScreenDefs.getInput("CU-STAFF")));
        ws.setCuStaff(Utility.parseIntOr(scValCuStaff10.trim(), 0));
        String scValCuCloseDay11 =
                Utility.acceptScreen(
                        "CU-CLOSE-DAY",
                        () -> renderer.acceptField(ScreenDefs.getInput("CU-CLOSE-DAY")));
        ws.setCuCloseDay(Utility.parseIntOr(scValCuCloseDay11.trim(), 0));
        String scValCuPayMethod12 =
                Utility.acceptScreen(
                        "CU-PAY-METHOD",
                        () -> renderer.acceptField(ScreenDefs.getInput("CU-PAY-METHOD")));
        ws.setCuPayMethod(Utility.parseIntOr(scValCuPayMethod12.trim(), 0));
        String scValCuTaxType13 =
                Utility.acceptScreen(
                        "CU-TAX-TYPE",
                        () -> renderer.acceptField(ScreenDefs.getInput("CU-TAX-TYPE")));
        ws.setCuTaxType(Utility.parseIntOr(scValCuTaxType13.trim(), 0));
        String scValCuCreditLimit14 =
                Utility.acceptScreen(
                        "CU-CREDIT-LIMIT",
                        () -> renderer.acceptField(ScreenDefs.getInput("CU-CREDIT-LIMIT")));
        try {
            ws.setCuCreditLimit(new BigDecimal(scValCuCreditLimit14.trim()));
        } catch (NumberFormatException _nfe) {
            ws.setCuCreditLimit(BigDecimal.ZERO);
        }
        String scValCuPriceRank15 =
                Utility.acceptScreen(
                        "CU-PRICE-RANK",
                        () -> renderer.acceptField(ScreenDefs.getInput("CU-PRICE-RANK")));
        ws.setCuPriceRank(Utility.parseIntOr(scValCuPriceRank15.trim(), 0));
        String scValCuBankCode16 =
                Utility.acceptScreen(
                        "CU-BANK-CODE",
                        () -> renderer.acceptField(ScreenDefs.getInput("CU-BANK-CODE")));
        ws.setCuBankCode(Utility.parseIntOr(scValCuBankCode16.trim(), 0));
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
                    runChain(this::deleteCustomerWithConfirm);
                } else {
                    ws.setWkMsgLine("Nothing to delete");
                    renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
                }
            }
            case "00" -> {
                runChain(this::validateCustomerFields);
                if ((ws.getErrFlg() != 1)) {
                    runChain(this::saveCustomerRecord);
                }
            }
            default -> {
                ws.setWkMsgLine("Invalid function key");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: VAL-010 */
    private void validateCustomerFields() {
        ws.setErrFlg(0);
        if (Utility.fieldEquals(ws.getCuName(), " ")) {
            ws.setWkMsgLine("Name is required");
            ws.setString("ERR-FLG", "1");
            reportValidationError();
            return;
        }
        if (ws.getCuRegion() != 0) {
            ws.setRgCode(ws.getCuRegion());
            readMasterByKeyWithFallback(fileSet.getRegnf(), "RG-CODE");
            if (fileSet.getRegnf().isInvalidKey()) {
                ws.setWkMsgLine("Region code not found");
                ws.setString("ERR-FLG", "1");
                reportValidationError();
                return;
            }
        }
        if (ws.getCuStaff() != 0) {
            ws.setSfCode(ws.getCuStaff());
            readMasterByKeyWithFallback(fileSet.getStaff(), "SF-CODE");
            if (fileSet.getStaff().isInvalidKey()) {
                ws.setWkMsgLine("Sales rep not found");
                ws.setString("ERR-FLG", "1");
                reportValidationError();
                return;
            }
        }
        if (ws.getCuBankCode() != 0) {
            ws.setBkCode(ws.getCuBankCode());
            readMasterByKeyWithFallback(fileSet.getBankf(), "BK-CODE");
            if (fileSet.getBankf().isInvalidKey()) {
                ws.setWkMsgLine("Bank code not found");
                ws.setString("ERR-FLG", "1");
                reportValidationError();
                return;
            }
        }
        if ((ws.getCuCreditLimit().signum() < 0)) {
            ws.setWkMsgLine("Credit limit cannot be negative");
            ws.setString("ERR-FLG", "1");
        }
        // fall-through to next paragraph
        reportValidationError();
    }

    /** COBOL paragraph: VAL-999 */
    private void reportValidationError() {
        if ((ws.getErrFlg() == 1)) {
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        }
        /* CONTINUE */
    }

    /** COBOL paragraph: SAVE-010 */
    private void saveCustomerRecord() {
        ws.setCuUpdDate(ws.getWkSysdate());
        ws.setCuUpdUser(ws.getWkUserCode());
        ws.setCuDelFlag(0);
        if ((ws.getModeFlg() == 1)) {
            ws.setCuAddDate(ws.getWkSysdate());
            ws.setCuAddUser(ws.getWkUserCode());
            if (ws.getCuStartDate() == 0) {
                ws.setCuStartDate(ws.getWkSysdate());
            }
            fileSet.getCustf().write();
            ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
            if (fileSet.getCustf().isInvalidKey()) {
                ws.setWkMsgLine("Write failed - duplicate");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            } else {
                ws.setWkMsgLine("Customer added");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        } else {
            fileSet.getCustf().rewrite();
            ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
            if (fileSet.getCustf().isInvalidKey()) {
                ws.setWkMsgLine("Update failed");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            } else {
                ws.setWkMsgLine("Customer updated");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: DEL-010 */
    private void deleteCustomerWithConfirm() {
        ws.setWkConfirm(" ");
        ws.setWkMsgLine("Press Y then ENTER to delete");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-CONFIRM"), ws);
        String scValWkConfirm20 =
                Utility.acceptScreen(
                        "SC-CONF", () -> renderer.acceptField(ScreenDefs.getInput("SC-CONF")));
        ws.setWkConfirm(scValWkConfirm20);
        broadcastEstsStatus();
        if ((ws.getWkConfirm().equals("Y") || ws.getWkConfirm().equals("y"))) {
            ws.setCuDelFlag(1);
            ws.setCuUpdDate(ws.getWkSysdate());
            ws.setCuUpdUser(ws.getWkUserCode());
            fileSet.getCustf().rewrite();
            ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
            if (fileSet.getCustf().isInvalidKey()) {
                ws.setWkMsgLine("Delete failed");
            } else {
                ws.setWkMsgLine("Customer deleted");
            }
        } else {
            ws.setWkMsgLine("Delete cancelled");
        }
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
    }

    /** COBOL paragraph: LOOK-010 */
    private void lookupRelatedNames() {
        ws.setWkRegionName(" ");
        if (ws.getCuRegion() != 0) {
            ws.setRgCode(ws.getCuRegion());
            readMasterByKeyWithFallback(fileSet.getRegnf(), "RG-CODE");
            if (fileSet.getRegnf().isInvalidKey()) {
                ws.setWkRegionName("??? unknown region");
            } else {
                ws.setWkRegionName(ws.getRgName());
            }
        }
        ws.setWkStaffName(" ");
        if (ws.getCuStaff() != 0) {
            ws.setSfCode(ws.getCuStaff());
            readMasterByKeyWithFallback(fileSet.getStaff(), "SF-CODE");
            if (fileSet.getStaff().isInvalidKey()) {
                ws.setWkStaffName("??? unknown staff");
            } else {
                ws.setWkStaffName(ws.getSfName());
            }
        }
        ws.setWkBankName(" ");
        if (ws.getCuBankCode() != 0) {
            ws.setBkCode(ws.getCuBankCode());
            readMasterByKeyWithFallback(fileSet.getBankf(), "BK-CODE");
            if (fileSet.getBankf().isInvalidKey()) {
                ws.setWkBankName("??? unknown bank");
            } else {
                ws.setWkBankName(ws.getBkName());
            }
        }
    }

    /** COBOL paragraph: TERM-010 */
    private void closeAllFiles() {
        fileSet.getCustf().close();
        ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
        fileSet.getRegnf().close();
        ws.trySetString("FSTS", fileSet.getRegnf().getFileStatus());
        fileSet.getStaff().close();
        ws.trySetString("FSTS", fileSet.getStaff().getFileStatus());
        fileSet.getBankf().close();
        ws.trySetString("FSTS", fileSet.getBankf().getFileStatus());
    }

    /** COBOL paragraph: ABEND-010 */
    private void abortOnFileOpenError() {
        ws.setKaProgid("MS0010");
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

    /**
     * Reads a master file by key, preferring the screen-entered value and falling back to the
     * current record's key when the screen value is blank.
     */
    private void readMasterByKeyWithFallback(RawDatasetBase file, String keyFieldName) {
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
}
