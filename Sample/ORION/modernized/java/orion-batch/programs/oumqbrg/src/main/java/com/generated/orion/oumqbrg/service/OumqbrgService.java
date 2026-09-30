package com.generated.orion.oumqbrg.service;

import com.appruntime.MqService;
import com.generated.orion.oumqbrg.domain.OumqbrgFieldAccess;
import com.generated.orion.oumqbrg.domain.WorkingStorage;
import com.generated.orion.oumqbrg.runtime.OumqbrgDatasets;
import com.generated.orion.runtime.BatchServiceBase;
import com.generated.orion.runtime.DatasetEnums.*;
import com.generated.orion.runtime.ProgramExitSignal;
import com.generated.orion.runtime.Utility;
import com.generated.orion.runtime.io.AbstractDatasets;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/** Business logic service generated from COBOL program OUMQBRG. */
@Service
@Scope("prototype")
public class OumqbrgService extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final OumqbrgDatasets fileSet;

    /**
     * IBM MQ verbs (CALL 'MQOPEN'/'MQGET'/'MQPUT'/'MQCLOSE') — JMS-backed at runtime, stub without
     * a broker.
     */
    private final MqService mq;

    private final OumqbrgFieldAccess ws;

    public OumqbrgService(OumqbrgDatasets fileSet, MqService mq) {
        this.fileSet = fileSet;
        this.ws = new OumqbrgFieldAccess(new WorkingStorage(), fileSet);
        this.mq = mq;
    }

    /**
     * Program entry point — runs the first COBOL paragraph via runChain. Base class run() wraps
     * this in StopRun/Abort/Exception handling.
     */
    @Override
    protected void mainProcess() {
        // Import anchors for types referenced by emitted paragraph bodies:
        // StopRunSignal, JobAbortException, ParagraphJumpSignal, ProgramExitSignal
        runChain(this::_0000Main);
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

    /** COBOL paragraph: 0000-MAIN */
    private void _0000Main() {
        runChain(this::_1000Initialize);
        while (!ws.getWsEofFlg().equals("Y")) {
            runChain(this::_2000BridgeTrans);
        }
        runChain(this::_3000Finalize);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: 1000-INITIALIZE */
    private void _1000Initialize() {
        log.info("OUMQBRG: VSAM-to-MQ transaction bridge started.");
        ws.setString("WS-MQ-DOWN-FLG", "N");
        fileSet.getTranFile().open(FileOpenMode.INPUT);
        ws.trySetString("WS-TRAN-FS", fileSet.getTranFile().getFileStatus());
        if (Utility.fieldEquals(ws.getWsTranFs(), "00")) {
            ws.setString("WS-TRAN-OPEN-FLG", "Y");
        } else {
            log.info("OUMQBRG: OPEN TRANFILE FS={}", ws.getWsTranFs());
            ws.setString("WS-EOF-FLG", "Y");
        }
        runChain(this::_1100MqConnect);
        if (ws.getWsMqDownFlg().equals("N")) {
            runChain(this::_1200MqOpenOut);
        }
        if (ws.getWsTranOpenFlg().equals("Y")) {
            runChain(this::_2100ReadTran);
        }
    }

    /** COBOL paragraph: 1100-MQ-CONNECT */
    private void _1100MqConnect() {
        ws.setWsMqOp("MQCONN  ");
        mqconn(ws.getWsQmgrName(), ws.getMqHconn(), ws.getMqCc(), ws.getMqRc());
        if ((ws.getMqCc() == 0)) {
            ws.setString("WS-MQ-CONN-FLG", "Y");
            log.info("OUMQBRG: connected to {}", ws.getWsQmgrName());
        } else {
            ws.setString("WS-MQ-DOWN-FLG", "Y");
            log.info(
                    "OUMQBRG: MQCONN FAILED CC={} RC={}",
                    String.format("%+010d", (long) (ws.getMqCc())),
                    String.format("%+010d", (long) (ws.getMqRc())));
            log.info("OUMQBRG: continuing in read-only mode.");
        }
    }

    /** COBOL paragraph: 1200-MQ-OPEN-OUT */
    private void _1200MqOpenOut() {
        ws.setMqOdObjectName(ws.getWsOutQueue());
        ws.setMqOpenOptions((ws.getMqOoOutput() + ws.getMqOoFailIfQsg()));
        ws.setWsMqOp("MQOPEN  ");
        Object[] _mq0 =
                new Object[] {
                    ws.getMqHconn(),
                    new com.appruntime.GroupRef(ws, "MQ-OD"),
                    ws.getMqOpenOptions(),
                    ws.getMqHobjOut(),
                    ws.getMqCc(),
                    ws.getMqRc()
                };
        mq.mqOpen(_mq0);
        if (_mq0[3] != null) {
            ws.setMqHobjOut(((Number) _mq0[3]).intValue());
        }
        if (_mq0[4] != null) {
            ws.setMqCc(((Number) _mq0[4]).intValue());
        }
        if (_mq0[5] != null) {
            ws.setMqRc(((Number) _mq0[5]).intValue());
        }
        if ((ws.getMqCc() == 0)) {
            ws.setString("WS-MQ-OPEN-FLG", "Y");
            log.info("OUMQBRG: opened {}", ws.getWsOutQueue());
        } else {
            ws.setString("WS-MQ-DOWN-FLG", "Y");
            log.info(
                    "OUMQBRG: MQOPEN FAILED CC={} RC={}",
                    String.format("%+010d", (long) (ws.getMqCc())),
                    String.format("%+010d", (long) (ws.getMqRc())));
        }
    }

    /** COBOL paragraph: 2000-BRIDGE-TRANS */
    private void _2000BridgeTrans() {
        ws.setWsReadCnt(ws.getWsReadCnt() + 1);
        if (ws.getWsMqDownFlg().equals("N") && ws.getWsMqOpenFlg().equals("Y")) {
            runChain(this::_2200PutTran);
        } else {
            ws.setWsSkipCnt(ws.getWsSkipCnt() + 1);
        }
        runChain(this::_2100ReadTran);
    }

    /** COBOL paragraph: 2100-READ-TRAN */
    private void _2100ReadTran() {
        String rkVal_0 = "";
        if (rkVal_0 == null || rkVal_0.trim().isEmpty()) {
            try {
                rkVal_0 = ws.getString("TR-ID");
            } catch (Exception _e) {
            }
        }
        if (rkVal_0 == null || rkVal_0.trim().isEmpty()) {
            try {
                rkVal_0 = fileSet.getTranFile().extractKeyFromCurrentRecord();
            } catch (Exception _e3) {
            }
        }
        fileSet.getTranFile().readByKey(rkVal_0 != null ? rkVal_0.trim() : "");
        ws.trySetString("WS-TRAN-FS", fileSet.getTranFile().getFileStatus());
        if (fileSet.getTranFile().isAtEnd()) {
            ws.setString("WS-EOF-FLG", "Y");
        }
        if (!Utility.fieldEquals(ws.getWsTranFs(), "00")
                && !Utility.fieldEquals(ws.getWsTranFs(), "10")) {
            log.info("OUMQBRG: READ TRANFILE FS={}", ws.getWsTranFs());
            ws.setString("WS-EOF-FLG", "Y");
        }
    }

    /** COBOL paragraph: 2200-PUT-TRAN */
    private void _2200PutTran() {
        ws.setOmRecType("TRAN");
        ws.setOmSrcSystem("ORION   ");
        ws.setOmTranData(Utility.groupToString(fileSet.getTranFile().getRecord()));
        ws.setMqMdMsgType(ws.getMqMtDatagram());
        ws.setMqMdFormat("MQSTR   ");
        ws.setMqPmoOptions(ws.getMqPmNoSyncpoint());
        ws.setMqBufferLen(362);
        ws.fillLowValues("MQ-MD-MSG-ID");
        ws.fillLowValues("MQ-MD-CORREL-ID");
        ws.setWsMqOp("MQPUT   ");
        Object[] _mq1 =
                new Object[] {
                    ws.getMqHconn(),
                    ws.getMqHobjOut(),
                    new com.appruntime.GroupRef(ws, "MQ-MD"),
                    new com.appruntime.GroupRef(ws, "MQ-PMO"),
                    ws.getMqBufferLen(),
                    new com.appruntime.GroupRef(ws, "WS-OUT-MSG"),
                    ws.getMqCc(),
                    ws.getMqRc()
                };
        mq.mqPut(_mq1);
        if (_mq1[6] != null) {
            ws.setMqCc(((Number) _mq1[6]).intValue());
        }
        if (_mq1[7] != null) {
            ws.setMqRc(((Number) _mq1[7]).intValue());
        }
        if ((ws.getMqCc() == 0)) {
            ws.setWsPutCnt(ws.getWsPutCnt() + 1);
        } else {
            ws.setWsPuterrCnt(ws.getWsPuterrCnt() + 1);
            log.info(
                    "OUMQBRG: MQPUT id={} CC={} RC={}",
                    ws.getTrId(),
                    String.format("%+010d", (long) (ws.getMqCc())),
                    String.format("%+010d", (long) (ws.getMqRc())));
        }
    }

    /** COBOL paragraph: 3000-FINALIZE */
    private void _3000Finalize() {
        if (ws.getWsMqOpenFlg().equals("Y")) {
            runChain(this::_3100MqCloseOut);
        }
        if (ws.getWsMqConnFlg().equals("Y")) {
            runChain(this::_3200MqDisconnect);
        }
        if (ws.getWsTranOpenFlg().equals("Y")) {
            fileSet.getTranFile().close();
            ws.trySetString("WS-TRAN-FS", fileSet.getTranFile().getFileStatus());
            if (!Utility.fieldEquals(ws.getWsTranFs(), "00")) {
                log.info("OUMQBRG: CLOSE TRANFILE FS={}", ws.getWsTranFs());
            }
        }
        runChain(this::_3900DisplaySummary);
    }

    /** COBOL paragraph: 3100-MQ-CLOSE-OUT */
    private void _3100MqCloseOut() {
        ws.setMqCloseOptions(ws.getMqClNone());
        ws.setWsMqOp("MQCLOSE ");
        Object[] _mq2 =
                new Object[] {
                    ws.getMqHconn(),
                    ws.getMqHobjOut(),
                    ws.getMqCloseOptions(),
                    ws.getMqCc(),
                    ws.getMqRc()
                };
        mq.mqClose(_mq2);
        if (_mq2[3] != null) {
            ws.setMqCc(((Number) _mq2[3]).intValue());
        }
        if (_mq2[4] != null) {
            ws.setMqRc(((Number) _mq2[4]).intValue());
        }
        if ((ws.getMqCc() != 0)) {
            log.info(
                    "OUMQBRG: MQCLOSE CC={} RC={}",
                    String.format("%+010d", (long) (ws.getMqCc())),
                    String.format("%+010d", (long) (ws.getMqRc())));
        }
    }

    /** COBOL paragraph: 3200-MQ-DISCONNECT */
    private void _3200MqDisconnect() {
        ws.setWsMqOp("MQDISC  ");
        mqdisc(ws.getMqHconn(), ws.getMqCc(), ws.getMqRc());
        if ((ws.getMqCc() != 0)) {
            log.info(
                    "OUMQBRG: MQDISC CC={} RC={}",
                    String.format("%+010d", (long) (ws.getMqCc())),
                    String.format("%+010d", (long) (ws.getMqRc())));
        }
    }

    /** COBOL paragraph: 3900-DISPLAY-SUMMARY */
    private void _3900DisplaySummary() {
        log.info(
                "OUMQBRG: records read    = {}", String.format("%09d", (long) (ws.getWsReadCnt())));
        log.info("OUMQBRG: messages put    = {}", String.format("%09d", (long) (ws.getWsPutCnt())));
        log.info(
                "OUMQBRG: put errors      = {}",
                String.format("%09d", (long) (ws.getWsPuterrCnt())));
        log.info(
                "OUMQBRG: skipped (no MQ) = {}", String.format("%09d", (long) (ws.getWsSkipCnt())));
        log.info("OUMQBRG: bridge complete.");
    }

    /** Stub: COBOL CALL MQCONN — inter-program call placeholder. */
    private void mqconn(Object... args) {
        /* CALL MQCONN: inter-program call — implement wiring if needed */ ;
    }

    /** Stub: COBOL CALL MQDISC — inter-program call placeholder. */
    private void mqdisc(Object... args) {
        /* CALL MQDISC: inter-program call — implement wiring if needed */ ;
    }
}
