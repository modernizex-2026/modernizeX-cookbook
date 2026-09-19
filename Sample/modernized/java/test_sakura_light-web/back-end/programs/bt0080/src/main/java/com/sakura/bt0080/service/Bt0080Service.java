package com.sakura.bt0080.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.bt0080.domain.Bt0080FieldAccess;
import com.sakura.bt0080.domain.WorkingStorage;
import com.sakura.bt0080.runtime.Bt0080Datasets;
import com.sakura.dateut.service.DateutService;
import com.sakura.runtime.BatchServiceBase;
import com.sakura.runtime.DatasetEnums.*;
import com.sakura.runtime.ProgramExitSignal;
import com.sakura.runtime.Utility;
import com.sakura.runtime.io.AbstractDatasets;
import com.sakura.runtime.linkage.AbortxLinkParm;
import com.sakura.runtime.linkage.DateutLinkParm;
import com.sakura.runtime.record.RawDatasetBase;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/** Business logic service generated from COBOL program BT0080. */
@Service
@Scope("prototype")
public class Bt0080Service extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final Bt0080Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Bt0080FieldAccess ws;

    public Bt0080Service(
            Bt0080Datasets fileSet, DateutService dateutService, AbortxService abortxService) {
        this.fileSet = fileSet;
        this.ws = new Bt0080FieldAccess(new WorkingStorage(), fileSet);
        this.dateutService = dateutService;
        this.abortxService = abortxService;
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
        if (ws.getWkAbortFlg() == 0) {
            runChain(this::reorganizeCustomerFile);
            runChain(this::reorganizeProductFile);
            runChain(this::printReorgSummary);
        }
        runChain(this::logProgramEnd);
        ws.setCompletionCode(0);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeProgram() {
        ws.setWkProgid("BT0080");
        log.info(" ");
        log.info("==============================================");
        log.info(" SAKURA-SMS  BT0080  -  REINDEX / REBUILD");
        log.info("==============================================");
        runChain(this::loadSystemDate);
        runChain(this::confirmReorgWithOperator);
    }

    /** COBOL paragraph: GTOD-010 */
    private void loadSystemDate() {
        ws.setKdFunc("TODY");
        ws.setKdDate1(0);
        dateut(ws.getKdate());
        ws.setWkSysdate(ws.getKdDate1());
        ws.setWkSysymd(ws.getKdDate1());
    }

    /** COBOL paragraph: CONF-010 */
    private void confirmReorgWithOperator() {
        log.info(" ");
        log.info(" Reorganise CUSTF and PRODF into fresh");
        log.info(" compact copies (data/CUSTF.RGN, PRODF.RGN),");
        log.info(" dropping logically-deleted rows.");
        log.info(" Proceed ? (Y/N) : ");
        ws.setWkConfirm(" ");
        String stdinValWkConfirm0 = Utility.readStdinLine();
        if (stdinValWkConfirm0 == null || stdinValWkConfirm0.trim().isEmpty()) {
            stdinValWkConfirm0 = "";
        }
        ws.setWkConfirm(stdinValWkConfirm0);
        if (!((ws.getWkConfirm().equals("Y") || ws.getWkConfirm().equals("y")))) {
            log.info(" ** cancelled by operator.");
            ws.setWkAbortFlg(1);
        }
    }

    /** COBOL paragraph: RCU-010 */
    private void reorganizeCustomerFile() {
        log.info(" ");
        log.info(" Reorganising CUSTF ...");
        openFileChecked(fileSet.getCustf(), FileOpenMode.INPUT, "CUSTF", "Open CUSTF failed");
        openFileChecked(
                fileSet.getCustn(), FileOpenMode.OUTPUT, "CUSTN", "Open reorg CUSTF.RGN failed");
        ws.setEofFlg(0);
        ws.setCuCode(0);
        fileSet.getCustf().start("CU-CODE", "NOT LESS THAN");
        ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
        if (fileSet.getCustf().isInvalidKey()) {
            ws.setString("EOF-FLG", "1");
        }
        while ((ws.getEofFlg() != 1)) {
            runChain(this::copyCustomerRecordIfActive);
        }
        closeFile(fileSet.getCustf());
        closeFile(fileSet.getCustn());
    }

    /** COBOL paragraph: RCUX-010 */
    private void copyCustomerRecordIfActive() {
        fileSet.getCustf().readNext();
        ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
        if (fileSet.getCustf().isAtEnd()) {
            ws.setString("EOF-FLG", "1");
            return;
        }
        if (!Utility.fieldEquals(ws.getFsts(), "00") && !Utility.fieldEquals(ws.getFsts(), "02")) {
            ws.setKaFile("CUSTF");
            ws.setKaDetail("READ NEXT CUSTF failed");
            runChain(this::abortProgram);
        }
        ws.setWkCuRead(ws.getWkCuRead() + 1);
        if (ws.getCuDelFlag() == 1) {
            ws.setWkCuSkip(ws.getWkCuSkip() + 1);
        } else {
            ws.copyNcRecFromCuRec();
            fileSet.getCustn().write();
            ws.trySetString("FSTS", fileSet.getCustn().getFileStatus());
            if (Utility.fieldEquals(ws.getFsts(), "00")) {
                ws.setWkCuCopy(ws.getWkCuCopy() + 1);
            } else {
                ws.setKaFile("CUSTN");
                ws.setKaDetail("WRITE reorg CUSTF.RGN failed");
                runChain(this::abortProgram);
            }
        }
    }

    /** COBOL paragraph: RPR-010 */
    private void reorganizeProductFile() {
        log.info(" Reorganising PRODF ...");
        openFileChecked(fileSet.getProdf(), FileOpenMode.INPUT, "PRODF", "Open PRODF failed");
        openFileChecked(
                fileSet.getProdn(), FileOpenMode.OUTPUT, "PRODN", "Open reorg PRODF.RGN failed");
        ws.setEofFlg(0);
        ws.setPrCode(0);
        fileSet.getProdf().start("PR-CODE", "NOT LESS THAN");
        ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
        if (fileSet.getProdf().isInvalidKey()) {
            ws.setString("EOF-FLG", "1");
        }
        while ((ws.getEofFlg() != 1)) {
            runChain(this::copyProductRecordIfActive);
        }
        closeFile(fileSet.getProdf());
        closeFile(fileSet.getProdn());
    }

    /** COBOL paragraph: RPRX-010 */
    private void copyProductRecordIfActive() {
        fileSet.getProdf().readNext();
        ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
        if (fileSet.getProdf().isAtEnd()) {
            ws.setString("EOF-FLG", "1");
            return;
        }
        if (!Utility.fieldEquals(ws.getFsts(), "00") && !Utility.fieldEquals(ws.getFsts(), "02")) {
            ws.setKaFile("PRODF");
            ws.setKaDetail("READ NEXT PRODF failed");
            runChain(this::abortProgram);
        }
        ws.setWkPrRead(ws.getWkPrRead() + 1);
        if (ws.getPrDelFlag() == 1) {
            ws.setWkPrSkip(ws.getWkPrSkip() + 1);
        } else {
            ws.copyNpRecFromPrRec();
            fileSet.getProdn().write();
            ws.trySetString("FSTS", fileSet.getProdn().getFileStatus());
            if (Utility.fieldEquals(ws.getFsts(), "00")) {
                ws.setWkPrCopy(ws.getWkPrCopy() + 1);
            } else {
                ws.setKaFile("PRODN");
                ws.setKaDetail("WRITE reorg PRODF.RGN failed");
                runChain(this::abortProgram);
            }
        }
    }

    /** COBOL paragraph: PSUM-010 */
    private void printReorgSummary() {
        log.info(" ");
        log.info("----------------------------------------------");
        log.info(" REINDEX / REBUILD SUMMARY");
        log.info("----------------------------------------------");
        ws.setWkECnt(ws.getWkCuRead());
        log.info(" CUSTF read        : {}", ws.editedDisplay("WK-E-CNT"));
        ws.setWkECnt(ws.getWkCuCopy());
        log.info(" CUSTF copied      : {}", ws.editedDisplay("WK-E-CNT"));
        ws.setWkECnt(ws.getWkCuSkip());
        log.info(" CUSTF skipped/del : {}", ws.editedDisplay("WK-E-CNT"));
        ws.setWkECnt(ws.getWkPrRead());
        log.info(" PRODF read        : {}", ws.editedDisplay("WK-E-CNT"));
        ws.setWkECnt(ws.getWkPrCopy());
        log.info(" PRODF copied      : {}", ws.editedDisplay("WK-E-CNT"));
        ws.setWkECnt(ws.getWkPrSkip());
        log.info(" PRODF skipped/del : {}", ws.editedDisplay("WK-E-CNT"));
        log.info("----------------------------------------------");
        log.info(" Reorg files written to data/*.RGN");
        log.info(" BT0080 completed normally.");
    }

    /** COBOL paragraph: TERM-010 */
    private void logProgramEnd() {
        log.info(" ");
        log.info(" BT0080 end.");
    }

    /** COBOL paragraph: ABND-010 */
    private void abortProgram() {
        ws.setKaProgid("BT0080");
        ws.setKaFsts(ws.getFsts());
        ws.setKaMsgcode("EBATCH");
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

    /** Open a file and abort the run if the resulting file status is not "00". */
    private void openFileChecked(
            RawDatasetBase file,
            FileOpenMode mode,
            String fileNameForAbort,
            String detailForAbort) {
        file.open(mode);
        ws.trySetString("FSTS", file.getFileStatus());
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile(fileNameForAbort);
            ws.setKaDetail(detailForAbort);
            runChain(this::abortProgram);
        }
    }

    /** Close a file and record its resulting file status. */
    private void closeFile(RawDatasetBase file) {
        file.close();
        ws.trySetString("FSTS", file.getFileStatus());
    }
}
