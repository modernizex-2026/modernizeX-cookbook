package com.generated.orion.ouimspa.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.common.linkage.OuimspaLinkParm;
import com.generated.orion.ouimspa.accessor.OuimspaFields;
import com.generated.orion.ouimspa.metadata.OuimspaBmsMetadata;
import com.generated.orion.ouimspa.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OUIMSPA. */
@Service
public class OuimspaService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OuimspaService.class);

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
                if (_params.length > 1 && _params[1] instanceof byte[]) {
                    ctx.f.writeBytes("ORION-COMMAREA", (byte[]) _params[1]);
                } else if (_params.length > 1 && _params[1] != null) {
                    ctx.f.setGroup("ORION-COMMAREA", String.valueOf(_params[1]));
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
                    if (_params.length > 1) {
                        _params[1] =
                                _params[1] instanceof byte[]
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
        return "OUIMSPA";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OuimspaBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OuimspaBmsMetadata.registerFsetFields(runner);
    }

    /** COBOL paragraph: 0000-MAIN */
    private void _0000Main(TaskContext ctx) {
        _1000Initialize(ctx);
        _1100ValidateRequest(ctx);
        if (ctx.f.getWsPlStatus().equals("BF")) {
            _9000Finalize(ctx);
            return;
        }
        if (ctx.f.getWsPlFunc().equals("INQ ")) {
            _2000Inquire(ctx);
        } else if (ctx.f.getWsPlFunc().equals("NXT ")) {
            _2100BrowseNext(ctx);
        } else if (ctx.f.getWsPlFunc().equals("ADD ")) {
            _2200Insert(ctx);
        } else if (ctx.f.getWsPlFunc().equals("UPD ")) {
            _2300Update(ctx);
        } else if (ctx.f.getWsPlFunc().equals("DEL ")) {
            _2400Delete(ctx);
        } else {
            _2900BadFunction(ctx);
        }
        _9000Finalize(ctx);
        return;
    }

    /** COBOL paragraph: 1000-INITIALIZE */
    private void _1000Initialize(TaskContext ctx) {
        ctx.f.setWsPauLink(String.valueOf(ctx.f.getCaWorkArea()));
        ctx.f.setWsPlStatus(" ");
        ctx.f.setWsPlMsg(" ");
        ctx.f.setWsDliStatus(" ");
        ctx.f.setWsCallCount(0);
    }

    /** COBOL paragraph: 1100-VALIDATE-REQUEST */
    private void _1100ValidateRequest(TaskContext ctx) {
        if (!(ctx.f.getWsPlFunc().equals("INQ ")
                || ctx.f.getWsPlFunc().equals("NXT ")
                || ctx.f.getWsPlFunc().equals("ADD ")
                || ctx.f.getWsPlFunc().equals("UPD ")
                || ctx.f.getWsPlFunc().equals("DEL "))) {
            ctx.f.setWsPlStatus("BF");
            ctx.f.setWsPlMsg("Unknown DL/I request function code.");
            return;
        }
        if (ctx.f.getWsPlFunc().equals("INQ ")
                || ctx.f.getWsPlFunc().equals("ADD ")
                || ctx.f.getWsPlFunc().equals("UPD ")
                || ctx.f.getWsPlFunc().equals("DEL ")) {
            if (Utility.fieldEquals(ctx.f.getWsPlKey(), " ") || ctx.f.isAllLowValues("WS-PL-KEY")) {
                ctx.f.setWsPlStatus("BF");
                ctx.f.setWsPlMsg("Authorization id is required.");
            }
        }
    }

    /** COBOL paragraph: 2000-INQUIRE */
    private void _2000Inquire(TaskContext ctx) {
        ctx.f.setOdliSsaKeyval(ctx.f.getWsPlKey());
        ctx.f.setOdliSsaRelop(ctx.f.getOdliOpEq());
        ctx.f.setWsDliFunc(ctx.f.getOdliGu());
        _5000CallGetQual(ctx);
        switch (Utility.rtrim(ctx.f.getWsDliStatus())) {
            case "" -> {
                _6000ReturnSegment(ctx);
            }
            case "GE" -> {
                _6100NotFound(ctx);
            }
            default -> {
                _6900DliError(ctx);
            }
        }
    }

    /** COBOL paragraph: 2100-BROWSE-NEXT */
    private void _2100BrowseNext(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.f.getWsPlKey(), " ") || ctx.f.isAllLowValues("WS-PL-KEY")) {
            _5400CallGetNext(ctx);
        } else {
            _2110PositionThenNext(ctx);
        }
        switch (Utility.rtrim(ctx.f.getWsDliStatus())) {
            case "" -> {
                _6000ReturnSegment(ctx);
            }
            case "GE" -> {
                _6200EndOfList(ctx);
            }
            case "GB" -> {
                _6200EndOfList(ctx);
            }
            default -> {
                _6900DliError(ctx);
            }
        }
    }

    /** COBOL paragraph: 2110-POSITION-THEN-NEXT */
    private void _2110PositionThenNext(TaskContext ctx) {
        ctx.f.setOdliSsaKeyval(ctx.f.getWsPlKey());
        ctx.f.setOdliSsaRelop(ctx.f.getOdliOpEq());
        ctx.f.setWsDliFunc(ctx.f.getOdliGu());
        _5000CallGetQual(ctx);
        switch (Utility.rtrim(ctx.f.getWsDliStatus())) {
            case "" -> {
                _5400CallGetNext(ctx);
            }
            case "GE" -> {
                ctx.f.setOdliSsaKeyval(ctx.f.getWsPlKey());
                ctx.f.setOdliSsaRelop(ctx.f.getOdliOpGt());
                ctx.f.setWsDliFunc(ctx.f.getOdliGu());
                _5000CallGetQual(ctx);
            }
            default -> {
                /* CONTINUE */
            }
        }
    }

    /** COBOL paragraph: 2200-INSERT */
    private void _2200Insert(TaskContext ctx) {
        ctx.f.copyBytes("PAUSEG-REC", "WS-PL-SEGMENT");
        ctx.f.setPaAuthId(ctx.f.getWsPlKey());
        _2210ValidateInsert(ctx);
        if (ctx.f.getWsPlStatus().equals("BF")) {
            return;
        }
        ctx.f.setWsDliFunc(ctx.f.getOdliIsrt());
        _5100CallInsert(ctx);
        switch (Utility.rtrim(ctx.f.getWsDliStatus())) {
            case "" -> {
                ctx.f.setWsPlStatus(" ");
                ctx.f.setWsPlMsg("Pending authorization inserted.");
                ctx.f.copyBytes("WS-PL-SEGMENT", "PAUSEG-REC");
            }
            case "II" -> {
                ctx.f.setWsPlStatus("II");
                ctx.f.setWsPlMsg("Authorization id already exists.");
            }
            default -> {
                _6900DliError(ctx);
            }
        }
    }

    /** COBOL paragraph: 2210-VALIDATE-INSERT */
    private void _2210ValidateInsert(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.f.getPaCardNum(), " ") || ctx.f.isAllLowValues("PA-CARD-NUM")) {
            ctx.f.setWsPlStatus("BF");
            ctx.f.setWsPlMsg("Card number is required to insert.");
            _2210Exit(ctx);
            return;
        }
        if (!Utility.isNumeric(ctx.f.getRawString("PA-ACCT-ID"))) {
            ctx.f.setWsPlStatus("BF");
            ctx.f.setWsPlMsg("Account id must be numeric.");
            _2210Exit(ctx);
            return;
        }
        if (Utility.fieldEquals(ctx.f.getPaStatus(), " ") || ctx.f.isAllLowValues("PA-STATUS")) {
            ctx.f.setPaStatus("P");
        }
    }

    /** COBOL paragraph: 2210-EXIT */
    private void _2210Exit(TaskContext ctx) {
        return;
    }

    /** COBOL paragraph: 2300-UPDATE */
    private void _2300Update(TaskContext ctx) {
        ctx.f.setOdliSsaKeyval(ctx.f.getWsPlKey());
        ctx.f.setOdliSsaRelop(ctx.f.getOdliOpEq());
        ctx.f.setWsDliFunc(ctx.f.getOdliGhu());
        _5000CallGetQual(ctx);
        switch (Utility.rtrim(ctx.f.getWsDliStatus())) {
            case "" -> {
                _2310ApplyReplace(ctx);
            }
            case "GE" -> {
                _6100NotFound(ctx);
            }
            default -> {
                _6900DliError(ctx);
            }
        }
    }

    /** COBOL paragraph: 2310-APPLY-REPLACE */
    private void _2310ApplyReplace(TaskContext ctx) {
        ctx.f.copyBytes("PAUSEG-REC", "WS-PL-SEGMENT");
        ctx.f.setPaAuthId(ctx.f.getWsPlKey());
        ctx.f.setWsDliFunc(ctx.f.getOdliRepl());
        _5200CallReplace(ctx);
        switch (Utility.rtrim(ctx.f.getWsDliStatus())) {
            case "" -> {
                ctx.f.setWsPlStatus(" ");
                ctx.f.setWsPlMsg("Pending authorization updated.");
                ctx.f.copyBytes("WS-PL-SEGMENT", "PAUSEG-REC");
            }
            default -> {
                _6900DliError(ctx);
            }
        }
    }

    /** COBOL paragraph: 2400-DELETE */
    private void _2400Delete(TaskContext ctx) {
        ctx.f.setOdliSsaKeyval(ctx.f.getWsPlKey());
        ctx.f.setOdliSsaRelop(ctx.f.getOdliOpEq());
        ctx.f.setWsDliFunc(ctx.f.getOdliGhu());
        _5000CallGetQual(ctx);
        switch (Utility.rtrim(ctx.f.getWsDliStatus())) {
            case "" -> {
                _2410ApplyDelete(ctx);
            }
            case "GE" -> {
                _6100NotFound(ctx);
            }
            default -> {
                _6900DliError(ctx);
            }
        }
    }

    /** COBOL paragraph: 2410-APPLY-DELETE */
    private void _2410ApplyDelete(TaskContext ctx) {
        ctx.f.setWsDliFunc(ctx.f.getOdliDlet());
        _5300CallDelete(ctx);
        switch (Utility.rtrim(ctx.f.getWsDliStatus())) {
            case "" -> {
                ctx.f.setWsPlStatus(" ");
                ctx.f.setWsPlMsg("Pending authorization purged.");
                ctx.f.setWsPlSegment(" ");
            }
            default -> {
                _6900DliError(ctx);
            }
        }
    }

    /** COBOL paragraph: 2900-BAD-FUNCTION */
    private void _2900BadFunction(TaskContext ctx) {
        ctx.f.setWsPlStatus("BF");
        ctx.f.setWsPlMsg("Unknown DL/I request function code.");
    }

    /** COBOL paragraph: 5000-CALL-GET-QUAL */
    private void _5000CallGetQual(TaskContext ctx) {
        callDliQualified(ctx, "ODLI-SSA-PAUSEG");
    }

    /** COBOL paragraph: 5400-CALL-GET-NEXT */
    private void _5400CallGetNext(TaskContext ctx) {
        ctx.f.setWsDliFunc(ctx.f.getOdliGn());
        callDliQualified(ctx, "ODLI-SSA-UNQUAL");
    }

    /** COBOL paragraph: 5100-CALL-INSERT */
    private void _5100CallInsert(TaskContext ctx) {
        callDliQualified(ctx, "ODLI-SSA-UNQUAL");
    }

    /** COBOL paragraph: 5200-CALL-REPLACE */
    private void _5200CallReplace(TaskContext ctx) {
        callDliUnqualified(ctx);
    }

    /** COBOL paragraph: 5300-CALL-DELETE */
    private void _5300CallDelete(TaskContext ctx) {
        callDliUnqualified(ctx);
    }

    /** Issue a DL/I call qualified by an SSA segment group and record the returned PCB status. */
    private void callDliQualified(TaskContext ctx, String ssaGroupName) {
        ctx.f.setWsCallCount(ctx.f.getWsCallCount() + 1);
        String dliStatus =
                ctx.appService
                        .getDliService()
                        .cbltdli(
                                String.valueOf(ctx.f.getWsDliFunc()),
                                ctx.f,
                                ctx.f.groupToString(ssaGroupName));
        ctx.link.getLkPauPcb().setLkPcbStatus(dliStatus);
        ctx.f.setWsDliStatus(ctx.link.getLkPauPcb().getLkPcbStatus());
    }

    /** Issue a DL/I call with no SSA argument and record the returned PCB status. */
    private void callDliUnqualified(TaskContext ctx) {
        ctx.f.setWsCallCount(ctx.f.getWsCallCount() + 1);
        String dliStatus =
                ctx.appService.getDliService().cbltdli(String.valueOf(ctx.f.getWsDliFunc()), ctx.f);
        ctx.link.getLkPauPcb().setLkPcbStatus(dliStatus);
        ctx.f.setWsDliStatus(ctx.link.getLkPauPcb().getLkPcbStatus());
    }

    /** COBOL paragraph: 6000-RETURN-SEGMENT */
    private void _6000ReturnSegment(TaskContext ctx) {
        ctx.f.copyBytes("WS-PL-SEGMENT", "PAUSEG-REC");
        ctx.f.setWsPlKey(ctx.f.getPaAuthId());
        ctx.f.setWsPlStatus(" ");
        ctx.f.setWsPlMsg("Pending authorization retrieved.");
    }

    /** COBOL paragraph: 6100-NOT-FOUND */
    private void _6100NotFound(TaskContext ctx) {
        ctx.f.setWsPlStatus("GE");
        ctx.f.setWsPlMsg("No pending authorization for that id.");
        ctx.f.setWsPlSegment(" ");
    }

    /** COBOL paragraph: 6200-END-OF-LIST */
    private void _6200EndOfList(TaskContext ctx) {
        ctx.f.setWsPlStatus("GB");
        ctx.f.setWsPlMsg("End of pending authorization list.");
        ctx.f.setWsPlSegment(" ");
    }

    /** COBOL paragraph: 6900-DLI-ERROR */
    private void _6900DliError(TaskContext ctx) {
        ctx.f.setWsPlStatus("ER");
        ctx.f.setWsPlMsg(" ");
        ctx.f.setWsPlMsg(
                "DL/I error status=" + ctx.f.getWsDliStatus() + " func=" + ctx.f.getWsDliFunc());
        log.info(
                "OUIMSPA DL/I ERROR - STATUS={} FUNC={} KEY={}",
                ctx.f.getWsDliStatus(),
                ctx.f.getWsDliFunc(),
                ctx.f.getWsPlKey());
    }

    /** COBOL paragraph: 9000-FINALIZE */
    private void _9000Finalize(TaskContext ctx) {
        ctx.f.setWsPlDliStat(ctx.f.getWsDliStatus());
        if (Utility.fieldEquals(ctx.f.getWsPlStatus(), ctx.f.getOdliStOk())) {
            ctx.f.setString("CA-ERR-FLG", "N");
            ctx.f.setCaErrMsg(" ");
        } else {
            ctx.f.setString("CA-ERR-FLG", "Y");
            ctx.f.setCaErrMsg(ctx.f.getWsPlMsg());
        }
        ctx.f.setCaWorkArea(ctx.f.getWsPauLink());
    }

    /**
     * Per-request execution context — analogous to CICS Task Control Block. Each HTTP request
     * creates a fresh TaskContext. Thread-safe by design.
     */
    static class TaskContext {
        final WorkingStorage ws;

        final OuimspaFields f;

        final AppService appService;

        final OuimspaLinkParm link = new OuimspaLinkParm();

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OuimspaFields(ws);
        }
    }
}
