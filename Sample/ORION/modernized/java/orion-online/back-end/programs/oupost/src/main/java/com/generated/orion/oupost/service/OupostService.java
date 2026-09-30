package com.generated.orion.oupost.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.oupost.accessor.OupostFields;
import com.generated.orion.oupost.metadata.OupostBmsMetadata;
import com.generated.orion.oupost.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OUPOST. */
@Service
public class OupostService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OupostService.class);

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
            _0000Main(ctx);
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
        return "OUPOST";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OupostBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OupostBmsMetadata.registerFsetFields(runner);
    }

    /** COBOL paragraph: 0000-MAIN */
    private void _0000Main(TaskContext ctx) {
        ctx.f.aliasGroup("KOPS-AREA", "CA-WORK-AREA");
        _1000Initialise(ctx);
        _3000BrowseDriver(ctx);
        _9000Finalise(ctx);
        return;
    }

    /** COBOL paragraph: 1000-INITIALISE */
    private void _1000Initialise(TaskContext ctx) {
        ctx.f.setString("KO-STATUS", "O");
        ctx.f.setKoStatusMsg(" ");
        ctx.f.setKoReadCnt(0);
        ctx.f.setKoSelectCnt(0);
        ctx.f.setKoUpdateCnt(0);
        ctx.f.setKoPostedCnt(0);
        ctx.f.setKoRejectCnt(0);
        ctx.f.setKoSkipCnt(0);
        ctx.f.setKoTranCnt(0);
        ctx.f.setKoC1(0);
        ctx.f.setKoC2(0);
        ctx.f.setKoC3(0);
        ctx.f.setKoAmt1(java.math.BigDecimal.valueOf(0));
        ctx.f.setKoAmt2(java.math.BigDecimal.valueOf(0));
        ctx.f.setKoAmt3(java.math.BigDecimal.valueOf(0));
        ctx.f.setWsFilterOn("N");
        if (ctx.f.getKoParmAcct() > 0) {
            ctx.f.setWsFilterAcct(ctx.f.getKoParmAcct());
            ctx.f.setString("WS-FILTER-ON", "Y");
        }
        ctx.f.setString("WS-BR-END-SW", "N");
        ctx.f.setString("WS-BR-STARTED-SW", "N");
    }

    /** COBOL paragraph: 3000-BROWSE-DRIVER */
    private void _3000BrowseDriver(TaskContext ctx) {
        _3100StartBrowse(ctx);
        if (ctx.f.getWsBrStartedSw().equals("Y")) {
            _3200ReadNextTran(ctx);
            while (!(ctx.f.getWsBrEndSw().equals("Y") || ctx.f.getKoStatus().equals("E"))) {
                _4000ProcessTran(ctx);
            }
            _3400EndBrowse(ctx);
        }
    }

    /** COBOL paragraph: 3100-START-BROWSE */
    private void _3100StartBrowse(TaskContext ctx) {
        ctx.f.fillLowValues("TR-ID");
        ctx.appService.startBrowse(ctx.f.getWsTranfile(), String.valueOf(ctx.f.getTrId()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setString("WS-BR-STARTED-SW", "Y");
        } else if (ctx.f.getWsRespCd() == 13) {
            ctx.f.setString("WS-BR-END-SW", "Y");
            ctx.f.setKoStatusMsg("NO TRANSACTIONS ON FILE TO POST.");
        } else {
            ctx.f.setString("WS-BR-END-SW", "Y");
            ctx.f.setString("KO-STATUS", "E");
            ctx.f.setKoStatusMsg("STARTBR TRANFILE FAILED.");
        }
    }

    /** COBOL paragraph: 3200-READ-NEXT-TRAN */
    private void _3200ReadNextTran(TaskContext ctx) {
        ctx.appService.readNext(ctx.f.getWsTranfile(), ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setKoReadCnt(ctx.f.getKoReadCnt() + 1);
        } else if (ctx.f.getWsRespCd() == 20) {
            ctx.f.setString("WS-BR-END-SW", "Y");
        } else {
            ctx.f.setString("WS-BR-END-SW", "Y");
            ctx.f.setString("KO-STATUS", "E");
            ctx.f.setKoStatusMsg("READNEXT TRANFILE FAILED.");
        }
    }

    /** COBOL paragraph: 4000-PROCESS-TRAN */
    private void _4000ProcessTran(TaskContext ctx) {
        ctx.f.setString("WS-REJECT-SW", "N");
        ctx.f.setWsDcInd(" ");
        if (Utility.fieldEquals(ctx.f.getTrCardNum(), " ") || ctx.f.isAllLowValues("TR-CARD-NUM")) {
            ctx.f.setKoRejectCnt(ctx.f.getKoRejectCnt() + 1);
            ctx.f.setKoC1(ctx.f.getKoC1() + 1);
        } else {
            _4100ReadXref(ctx);
        }
        _3200ReadNextTran(ctx);
    }

    /** COBOL paragraph: 4100-READ-XREF */
    private void _4100ReadXref(TaskContext ctx) {
        ctx.f.setXrCardNum(ctx.f.getTrCardNum());
        ctx.appService.readFile(
                ctx.f.getWsXreffile(), ctx.f, String.valueOf(ctx.f.getXrCardNum()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            _4150CheckFilter(ctx);
        } else if (ctx.f.getWsRespCd() == 13) {
            ctx.f.setKoRejectCnt(ctx.f.getKoRejectCnt() + 1);
            ctx.f.setKoC1(ctx.f.getKoC1() + 1);
        } else {
            ctx.f.setKoRejectCnt(ctx.f.getKoRejectCnt() + 1);
        }
    }

    /** COBOL paragraph: 4150-CHECK-FILTER */
    private void _4150CheckFilter(TaskContext ctx) {
        if (ctx.f.getWsFilterOn().equals("Y") && ctx.f.getXrAcctId() != ctx.f.getWsFilterAcct()) {
            /* CONTINUE */
        } else {
            ctx.f.setKoSelectCnt(ctx.f.getKoSelectCnt() + 1);
            _4200ReadAcctUpd(ctx);
        }
    }

    /** COBOL paragraph: 4200-READ-ACCT-UPD */
    private void _4200ReadAcctUpd(TaskContext ctx) {
        ctx.f.setAcId(ctx.f.getXrAcctId());
        ctx.appService.readFileForUpdate(
                ctx.f.getWsAcctfile(), ctx.f, String.valueOf(ctx.f.getAcId()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            _4300Classify(ctx);
        } else if (ctx.f.getWsRespCd() == 13) {
            ctx.f.setKoRejectCnt(ctx.f.getKoRejectCnt() + 1);
            ctx.f.setKoC2(ctx.f.getKoC2() + 1);
        } else {
            ctx.f.setKoRejectCnt(ctx.f.getKoRejectCnt() + 1);
            _4250Unlock(ctx);
        }
    }

    /** COBOL paragraph: 4250-UNLOCK */
    private void _4250Unlock(TaskContext ctx) {
        /* EXEC CICS UNLOCK — file unlock */ ;
    }

    /** COBOL paragraph: 4300-CLASSIFY */
    private void _4300Classify(TaskContext ctx) {
        switch (Utility.rtrim(ctx.f.getTrTypeCd())) {
            case "PY", "CR" -> {
                ctx.f.setString("WS-DC-IND", "CR");
                _4400ApplyUpdate(ctx);
            }
            default -> {
                ctx.f.setString("WS-DC-IND", "DB");
                ctx.f.setWsProjBal(ctx.f.getAcCurrBal().add(ctx.f.getTrAmt()));
                if ((ctx.f.getWsProjBal().compareTo(ctx.f.getAcCreditLimit()) > 0)) {
                    ctx.f.setKoRejectCnt(ctx.f.getKoRejectCnt() + 1);
                    ctx.f.setKoC3(ctx.f.getKoC3() + 1);
                    _4250Unlock(ctx);
                } else {
                    _4400ApplyUpdate(ctx);
                }
            }
        }
    }

    /** COBOL paragraph: 4400-APPLY-UPDATE */
    private void _4400ApplyUpdate(TaskContext ctx) {
        if (ctx.f.getWsDcInd().equals("CR")) {
            ctx.f.setAcCurrBal(ctx.f.getAcCurrBal().subtract(ctx.f.getTrAmt()));
            ctx.f.setAcCycCredit(ctx.f.getAcCycCredit().add(ctx.f.getTrAmt()));
        } else {
            ctx.f.setAcCurrBal(ctx.f.getAcCurrBal().add(ctx.f.getTrAmt()));
            ctx.f.setAcCycDebit(ctx.f.getAcCycDebit().add(ctx.f.getTrAmt()));
        }
        ctx.appService.rewriteFile(ctx.f.getWsAcctfile(), ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setKoPostedCnt(ctx.f.getKoPostedCnt() + 1);
            ctx.f.setKoUpdateCnt(ctx.f.getKoUpdateCnt() + 1);
            if (ctx.f.getWsDcInd().equals("CR")) {
                ctx.f.setKoAmt2(ctx.f.getKoAmt2().add(ctx.f.getTrAmt()));
            } else {
                ctx.f.setKoAmt1(ctx.f.getKoAmt1().add(ctx.f.getTrAmt()));
            }
        } else {
            ctx.f.setKoRejectCnt(ctx.f.getKoRejectCnt() + 1);
        }
    }

    /** COBOL paragraph: 3400-END-BROWSE */
    private void _3400EndBrowse(TaskContext ctx) {
        if (ctx.f.getWsBrStartedSw().equals("Y")) {
            ctx.appService.endBrowse(ctx.f.getWsTranfile());
            ctx.f.setWsRespCd(ctx.appService.getEibresp());
        }
    }

    /** COBOL paragraph: 9000-FINALISE */
    private void _9000Finalise(TaskContext ctx) {
        ctx.f.setKoAmt3(ctx.f.getKoAmt1().subtract(ctx.f.getKoAmt2()));
        if (ctx.f.getKoStatus().equals("E")) {
            /* CONTINUE */
        } else {
            if (ctx.f.getKoRejectCnt() > 0) {
                ctx.f.setString("KO-STATUS", "W");
            } else {
                ctx.f.setString("KO-STATUS", "O");
            }
            ctx.f.setKoStatusMsg("TRANSACTION POSTING COMPLETE.");
        }
    }

    /**
     * Per-request execution context — analogous to CICS Task Control Block. Each HTTP request
     * creates a fresh TaskContext. Thread-safe by design.
     */
    static class TaskContext {
        final WorkingStorage ws;

        final OupostFields f;

        final AppService appService;

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OupostFields(ws);
        }
    }
}
