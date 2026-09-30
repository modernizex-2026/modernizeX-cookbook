package com.generated.orion.oumqreq.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.oumqreq.accessor.OumqreqFields;
import com.generated.orion.oumqreq.metadata.OumqreqBmsMetadata;
import com.generated.orion.oumqreq.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OUMQREQ. */
@Service
public class OumqreqService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OumqreqService.class);

    /**
     * Entry point: executes the CICS program main line. Called by AppRunner after transaction
     * setup.
     */
    @Override
    public void mainLine(AppService appService) {
        TaskContext ctx = new TaskContext(appService);
        if (ctx.appService.getEibcalen() > 0 && ctx.appService.getCommarea() != null) {
            Object _commarea = ctx.appService.getCommarea();
            if (_commarea instanceof Object[]) {
                Object[] _params = (Object[]) _commarea;
                if (_params.length > 0 && _params[0] instanceof byte[]) {
                    ctx.f.writeBytes("ORION-COMMAREA", (byte[]) _params[0]);
                } else if (_params.length > 0 && _params[0] != null) {
                    ctx.f.setGroup("ORION-COMMAREA", String.valueOf(_params[0]));
                }
            } else if (_commarea instanceof byte[]) {
                ctx.f.writeBytes("ORION-COMMAREA", (byte[]) _commarea);
            } else {
                ctx.f.setGroup("ORION-COMMAREA", String.valueOf(_commarea));
            }
        }
        try {
            runMainProgram(ctx);
        } finally {
            if (ctx.appService.getEibcalen() > 0) {
                if (ctx.appService.getCommarea() instanceof Object[]) {
                    Object[] _params = (Object[]) ctx.appService.getCommarea();
                    if (_params.length > 0) {
                        _params[0] =
                                _params[0] instanceof byte[]
                                        ? (Object) ctx.f.sliceBytes("ORION-COMMAREA")
                                        : (Object) ctx.f.groupToString("ORION-COMMAREA");
                    }
                } else {
                    ctx.appService.setCommarea(ctx.f.sliceBytes("ORION-COMMAREA"));
                }
            }
        }
    }

    @Override
    public String getProgramName() {
        return "OUMQREQ";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OumqreqBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OumqreqBmsMetadata.registerFsetFields(runner);
    }

    /** COBOL paragraph: 0000-MAIN */
    private void runMainProgram(TaskContext ctx) {
        ctx.f.aliasGroup("AUTH-MSG-AREA", "CA-WORK-AREA");
        initializeProgram(ctx);
        buildAuthRequest(ctx);
        performLocalAuthorization(ctx);
        putAuthRequestToQueue(ctx);
        getAuthReplyFromQueue(ctx);
        finalizeResponse(ctx);
        return;
    }

    /** COBOL paragraph: 1000-INITIALIZE */
    private void initializeProgram(TaskContext ctx) {
        ctx.f.setString("WS-MQ-DOWN-FLG", "N");
        ctx.f.setString("WS-IO-FLG", "N");
        ctx.f.setMqHconn(ctx.f.getMqHcDefHconn());
        ctx.f.setMqHobjOut(0);
        ctx.f.setMqHobjRpy(0);
        ctx.f.setCaErrMsg(" ");
    }

    /** COBOL paragraph: 2000-BUILD-REQUEST */
    private void buildAuthRequest(TaskContext ctx) {
        ctx.f.setAqMsgType("AREQ");
        getCurrentTimestamp(ctx);
        ctx.f.copyBytes("AQ-REQUESTED-TS", "WS-STAMP");
        ctx.f.setAuthResponse(" ");
        ctx.f.setAsMsgType("ARSP");
        ctx.f.setAsCardNum(ctx.f.getAqCardNum());
        ctx.f.setAsAcctId(0);
        ctx.f.setAsApprovedAmt(java.math.BigDecimal.valueOf(0));
        ctx.f.setAsAvailCredit(java.math.BigDecimal.valueOf(0));
        ctx.f.setAsReason(" ");
    }

    /** COBOL paragraph: 3000-LOCAL-AUTHORIZE */
    private void performLocalAuthorization(TaskContext ctx) {
        ctx.f.setString("WS-IO-FLG", "N");
        ctx.f.setXrCardNum(ctx.f.getAqCardNum());
        readCardXref(ctx);
        if (ctx.f.getWsIoFlg().equals("Y")) {
            ctx.f.setString("AS-DECISION", "ERROR   ");
            ctx.f.setAsReason("FILE READ ERROR");
        } else if (ctx.f.getWsFoundFlg().equals("N")) {
            ctx.f.setString("AS-DECISION", "DECLINED");
            ctx.f.setAsReason("CARD NOT FOUND");
            ctx.f.setAsApprovedAmt(java.math.BigDecimal.valueOf(0));
        } else {
            ctx.f.setAcId(ctx.f.getXrAcctId());
            ctx.f.setAqAcctId(ctx.f.getXrAcctId());
            ctx.f.setAsAcctId(ctx.f.getXrAcctId());
            readAccountRecord(ctx);
            evaluateAccountStatus(ctx);
        }
    }

    /** COBOL paragraph: 3100-READ-XREF */
    private void readCardXref(TaskContext ctx) {
        ctx.f.setString("WS-FOUND-FLG", "N");
        ctx.appService.readFile(
                ctx.f.getWsXreffile(), ctx.f, String.valueOf(ctx.f.getXrCardNum()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setString("WS-FOUND-FLG", "Y");
        } else if (ctx.f.getWsRespCd() == 13) {
            ctx.f.setString("WS-FOUND-FLG", "N");
        } else {
            ctx.f.setString("WS-FOUND-FLG", "N");
            ctx.f.setString("WS-IO-FLG", "Y");
            log.info(
                    "OUMQREQ: XREF READ RESP={}",
                    String.format("%+010d", (long) (ctx.f.getWsRespCd())));
        }
    }

    /** COBOL paragraph: 3200-READ-ACCT */
    private void readAccountRecord(TaskContext ctx) {
        ctx.f.setString("WS-FOUND-FLG", "N");
        ctx.appService.readFile(ctx.f.getWsAcctfile(), ctx.f, String.valueOf(ctx.f.getAcId()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setString("WS-FOUND-FLG", "Y");
        } else if (ctx.f.getWsRespCd() == 13) {
            ctx.f.setString("WS-FOUND-FLG", "N");
        } else {
            ctx.f.setString("WS-FOUND-FLG", "N");
            ctx.f.setString("WS-IO-FLG", "Y");
            log.info(
                    "OUMQREQ: ACCT READ RESP={}",
                    String.format("%+010d", (long) (ctx.f.getWsRespCd())));
        }
    }

    /** COBOL paragraph: 3300-EVALUATE-ACCT */
    private void evaluateAccountStatus(TaskContext ctx) {
        if (ctx.f.getWsIoFlg().equals("Y")) {
            ctx.f.setString("AS-DECISION", "ERROR   ");
            ctx.f.setAsReason("FILE READ ERROR");
        } else if (ctx.f.getWsFoundFlg().equals("N")) {
            ctx.f.setString("AS-DECISION", "DECLINED");
            ctx.f.setAsReason("ACCOUNT NOT FOUND");
            ctx.f.setAsApprovedAmt(java.math.BigDecimal.valueOf(0));
        } else {
            decideAuthorization(ctx);
        }
    }

    /** COBOL paragraph: 3600-DECIDE */
    private void decideAuthorization(TaskContext ctx) {
        ctx.f.setWsAmt(ctx.f.getAcCreditLimit().subtract(ctx.f.getAcCurrBal()));
        ctx.f.setAsAvailCredit(ctx.f.getWsAmt());
        if (!Utility.fieldEquals(ctx.f.getAcActiveStatus(), "Y")) {
            ctx.f.setString("AS-DECISION", "DECLINED");
            ctx.f.setAsReason("ACCOUNT INACTIVE");
            ctx.f.setAsApprovedAmt(java.math.BigDecimal.valueOf(0));
        } else if ((ctx.f.getAqAmount().compareTo(ctx.f.getWsAmt()) > 0)) {
            ctx.f.setString("AS-DECISION", "DECLINED");
            ctx.f.setAsReason("INSUFFICIENT CREDIT");
            ctx.f.setAsApprovedAmt(java.math.BigDecimal.valueOf(0));
        } else {
            ctx.f.setString("AS-DECISION", "APPROVED");
            ctx.f.setAsReason("APPROVED OK");
            ctx.f.setAsApprovedAmt(ctx.f.getAqAmount());
        }
    }

    /** COBOL paragraph: 4000-PUT-REQUEST */
    private void putAuthRequestToQueue(TaskContext ctx) {
        ctx.f.setMqOdObjectName(ctx.f.getWsReqQueue());
        ctx.f.setMqOpenOptions((ctx.f.getMqOoOutput() + ctx.f.getMqOoFailIfQsg()));
        ctx.f.setWsMqOp("MQOPEN  ");
        Object[] _mq0 =
                new Object[] {
                    ctx.f.getMqHconn(),
                    new com.appruntime.GroupRef(ctx.f, "MQ-OD"),
                    ctx.f.getMqOpenOptions(),
                    ctx.f.getMqHobjOut(),
                    ctx.f.getMqCc(),
                    ctx.f.getMqRc()
                };
        ctx.appService.callProgram("MQOPEN", _mq0);
        if (_mq0[3] != null) {
            ctx.f.setMqHobjOut(((Number) _mq0[3]).intValue());
        }
        if (_mq0[4] != null) {
            ctx.f.setMqCc(((Number) _mq0[4]).intValue());
        }
        if (_mq0[5] != null) {
            ctx.f.setMqRc(((Number) _mq0[5]).intValue());
        }
        checkMqStatus(ctx);
        if (ctx.f.getWsMqDownFlg().equals("N")) {
            putMqRequestMessage(ctx);
        }
    }

    /** COBOL paragraph: 4100-PUT-MSG */
    private void putMqRequestMessage(TaskContext ctx) {
        ctx.f.setMqMdMsgType(ctx.f.getMqMtRequest());
        ctx.f.setMqMdFormat("MQSTR   ");
        ctx.f.setMqMdReplyQ(ctx.f.getWsRpyQueue());
        ctx.f.setMqPmoOptions(ctx.f.getMqPmNoSyncpoint());
        ctx.f.setMqBufferLen(160);
        ctx.f.setWsMqOp("MQPUT   ");
        Object[] _mq1 =
                new Object[] {
                    ctx.f.getMqHconn(),
                    ctx.f.getMqHobjOut(),
                    new com.appruntime.GroupRef(ctx.f, "MQ-MD"),
                    new com.appruntime.GroupRef(ctx.f, "MQ-PMO"),
                    ctx.f.getMqBufferLen(),
                    new com.appruntime.GroupRef(ctx.f, "AUTH-REQUEST"),
                    ctx.f.getMqCc(),
                    ctx.f.getMqRc()
                };
        ctx.appService.callProgram("MQPUT", _mq1);
        if (_mq1[6] != null) {
            ctx.f.setMqCc(((Number) _mq1[6]).intValue());
        }
        if (_mq1[7] != null) {
            ctx.f.setMqRc(((Number) _mq1[7]).intValue());
        }
        checkMqStatus(ctx);
        ctx.f.setMqCloseOptions(ctx.f.getMqClNone());
        ctx.f.setWsMqOp("MQCLOSE ");
        Object[] _mq2 =
                new Object[] {
                    ctx.f.getMqHconn(),
                    ctx.f.getMqHobjOut(),
                    ctx.f.getMqCloseOptions(),
                    ctx.f.getMqCc(),
                    ctx.f.getMqRc()
                };
        ctx.appService.callProgram("MQCLOSE", _mq2);
        if (_mq2[3] != null) {
            ctx.f.setMqCc(((Number) _mq2[3]).intValue());
        }
        if (_mq2[4] != null) {
            ctx.f.setMqRc(((Number) _mq2[4]).intValue());
        }
        checkMqStatus(ctx);
    }

    /** COBOL paragraph: 5000-GET-REPLY */
    private void getAuthReplyFromQueue(TaskContext ctx) {
        ctx.f.setMqOdObjectName(ctx.f.getWsRpyQueue());
        ctx.f.setMqOpenOptions((ctx.f.getMqOoInputShared() + ctx.f.getMqOoFailIfQsg()));
        ctx.f.setWsMqOp("MQOPEN  ");
        Object[] _mq3 =
                new Object[] {
                    ctx.f.getMqHconn(),
                    new com.appruntime.GroupRef(ctx.f, "MQ-OD"),
                    ctx.f.getMqOpenOptions(),
                    ctx.f.getMqHobjRpy(),
                    ctx.f.getMqCc(),
                    ctx.f.getMqRc()
                };
        ctx.appService.callProgram("MQOPEN", _mq3);
        if (_mq3[3] != null) {
            ctx.f.setMqHobjRpy(((Number) _mq3[3]).intValue());
        }
        if (_mq3[4] != null) {
            ctx.f.setMqCc(((Number) _mq3[4]).intValue());
        }
        if (_mq3[5] != null) {
            ctx.f.setMqRc(((Number) _mq3[5]).intValue());
        }
        checkMqStatus(ctx);
        if (ctx.f.getWsMqDownFlg().equals("N")) {
            getMqReplyMessage(ctx);
        }
    }

    /** COBOL paragraph: 5100-GET-MSG */
    private void getMqReplyMessage(TaskContext ctx) {
        ctx.f.setMqGmoOptions((ctx.f.getMqGmWait() + ctx.f.getMqGmNoSyncpoint()));
        ctx.f.setMqBufferLen(300);
        ctx.f.fillLowValues("MQ-MD-MSG-ID");
        ctx.f.fillLowValues("MQ-MD-CORREL-ID");
        ctx.f.setWsMqOp("MQGET   ");
        Object[] _mq4 =
                new Object[] {
                    ctx.f.getMqHconn(),
                    ctx.f.getMqHobjRpy(),
                    new com.appruntime.GroupRef(ctx.f, "MQ-MD"),
                    new com.appruntime.GroupRef(ctx.f, "MQ-GMO"),
                    ctx.f.getMqBufferLen(),
                    ctx.f.getWsReplyBuf(),
                    ctx.f.getMqDataLen(),
                    ctx.f.getMqCc(),
                    ctx.f.getMqRc()
                };
        ctx.appService.callProgram("MQGET", _mq4);
        if (_mq4[5] != null) {
            ctx.f.setWsReplyBuf(String.valueOf(_mq4[5]));
        }
        if (_mq4[6] != null) {
            ctx.f.setMqDataLen(((Number) _mq4[6]).intValue());
        }
        if (_mq4[7] != null) {
            ctx.f.setMqCc(((Number) _mq4[7]).intValue());
        }
        if (_mq4[8] != null) {
            ctx.f.setMqRc(((Number) _mq4[8]).intValue());
        }
        if (ctx.f.getMqCc() == 0) {
            log.info(
                    "OUMQREQ: reply received len={}",
                    String.format("%+010d", (long) (ctx.f.getMqDataLen())));
        } else {
            if (ctx.f.getMqRc() == 2033) {
                log.info("OUMQREQ: no reply queued - local decision");
            } else {
                checkMqStatus(ctx);
            }
        }
        ctx.f.setMqCloseOptions(ctx.f.getMqClNone());
        ctx.f.setWsMqOp("MQCLOSE ");
        Object[] _mq5 =
                new Object[] {
                    ctx.f.getMqHconn(),
                    ctx.f.getMqHobjRpy(),
                    ctx.f.getMqCloseOptions(),
                    ctx.f.getMqCc(),
                    ctx.f.getMqRc()
                };
        ctx.appService.callProgram("MQCLOSE", _mq5);
        if (_mq5[3] != null) {
            ctx.f.setMqCc(((Number) _mq5[3]).intValue());
        }
        if (_mq5[4] != null) {
            ctx.f.setMqRc(((Number) _mq5[4]).intValue());
        }
        checkMqStatus(ctx);
    }

    /** COBOL paragraph: 8000-CHECK-MQ */
    private void checkMqStatus(TaskContext ctx) {
        if (ctx.f.getMqCc() == 0) {
            /* CONTINUE */
        } else {
            ctx.f.setString("WS-MQ-DOWN-FLG", "Y");
            log.info(
                    "OUMQREQ: {} FAILED CC={} RC={}",
                    ctx.f.getWsMqOp(),
                    String.format("%+010d", (long) (ctx.f.getMqCc())),
                    String.format("%+010d", (long) (ctx.f.getMqRc())));
        }
    }

    /** COBOL paragraph: 8500-GET-STAMP */
    private void getCurrentTimestamp(TaskContext ctx) {
        ctx.appService.askTime();
        com.appruntime.FormatTimeResult _ftResult =
                ctx.appService.formatTime(null, "YYYYMMDD", "-", ":");
        ctx.f.setWsStDate(_ftResult.getDate());
        ctx.f.setWsStTime(_ftResult.getTime());
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 9000-FINALIZE */
    private void finalizeResponse(TaskContext ctx) {
        getCurrentTimestamp(ctx);
        ctx.f.copyBytes("AS-RESPONSE-TS", "WS-STAMP");
        if (ctx.f.getAsDecision().equals("ERROR   ")) {
            ctx.f.setString("CA-ERR-FLG", "Y");
        } else {
            ctx.f.setString("CA-ERR-FLG", "N");
        }
        {
            StringBuilder sb = new StringBuilder();
            sb.append(String.valueOf("AUTH "));
            sb.append(String.valueOf(ctx.f.getAsDecision()));
            sb.append(String.valueOf(" - "));
            sb.append(String.valueOf(ctx.f.getAsReason()));
            ctx.f.setCaErrMsg(sb.toString());
        }
        log.info("OUMQREQ: card {} decision {}", ctx.f.getAsCardNum(), ctx.f.getAsDecision());
    }

    /**
     * Per-request execution context — analogous to CICS Task Control Block. Each HTTP request
     * creates a fresh TaskContext. Thread-safe by design.
     */
    static class TaskContext {
        final WorkingStorage ws;

        final OumqreqFields f;

        final AppService appService;

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OumqreqFields(ws);
        }
    }
}
