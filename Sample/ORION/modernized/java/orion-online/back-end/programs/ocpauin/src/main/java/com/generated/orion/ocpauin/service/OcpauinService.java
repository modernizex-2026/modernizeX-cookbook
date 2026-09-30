package com.generated.orion.ocpauin.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.common.linkage.OcpauinLinkParm;
import com.generated.orion.ocpauin.accessor.OcpauinFields;
import com.generated.orion.ocpauin.metadata.OcpauinBmsMetadata;
import com.generated.orion.ocpauin.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OCPAUIN. */
@Service
public class OcpauinService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OcpauinService.class);

    /** Length in bytes of the ORION-COMMAREA passed to link/xctl/return calls. */
    private static final int COMMAREA_LENGTH = 692;

    /**
     * Entry point: executes the CICS program main line. Called by AppRunner after transaction
     * setup.
     */
    @Override
    public void mainLine(AppService appService) {
        TaskContext ctx = new TaskContext(appService);
        runMainProgram(ctx);
    }

    @Override
    public String getProgramName() {
        return "OCPAUIN";
    }

    @Override
    public String getTransId() {
        return "ORPI";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OcpauinBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OcpauinBmsMetadata.registerFsetFields(runner);
    }

    @Override
    public FieldMapping getFieldMapping(String mapName) {
        return OcpauinBmsMetadata.getFieldMapping(mapName);
    }

    /** COBOL paragraph: 0000-MAIN */
    private void runMainProgram(TaskContext ctx) {
        ctx.f.setString("WS-ERR-FLG", "N");
        ctx.f.setCaErrMsg(" ");
        if (ctx.appService.getEibcalen() == 0) {
            sendInitialScreen(ctx);
        } else {
            ctx.f.setOrionCommarea(Utility.groupToString(ctx.appService.getSerializedCommarea()));
            if (ctx.f.getCaPgmContext() == 0) {
                sendInitialScreen(ctx);
            } else {
                processInput(ctx);
            }
        }
        returnTransaction(ctx);
    }

    /** COBOL paragraph: 1000-SEND-INITIAL */
    private void sendInitialScreen(TaskContext ctx) {
        ctx.f.fillLowValues("MPAUINAO");
        populateScreenHeader(ctx);
        ctx.f.setErrmsgo("Enter an authorization id and press ENTER.");
        ctx.appService.sendMap(
                "MPAUINA", ctx.f, ctx.f, /* erase */ true, /* freekb */ false, /* cursor */ false);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.f.setCaPgmContext(1);
    }

    /** COBOL paragraph: 2000-PROCESS-INPUT */
    private void processInput(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.appService.getEibaid(), "3")) {
            transferToMenuProgram(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "4")) {
            sendInitialScreen(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "8")) {
            browseNextAuthorization(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "'")) {
            inquireAuthorization(ctx);
        } else {
            sendInitialScreen(ctx);
            ctx.f.setErrmsgo(ctx.f.getWsMsgInvalidKey());
            sendDataOnlyScreen(ctx);
        }
    }

    /** COBOL paragraph: 2100-INQUIRE */
    private void inquireAuthorization(TaskContext ctx) {
        ctx.appService.receiveMap("MPAUINA", ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (Utility.fieldEquals(ctx.f.getAuthidi(), " ") || ctx.f.isAllLowValues("AUTHIDI")) {
            ctx.f.setErrmsgo(ctx.f.getWsMsgRequired());
            sendDataOnlyScreen(ctx);
        } else {
            ctx.f.setWsPauLink(" ");
            ctx.f.setWsPlKey(ctx.f.getAuthidi());
            ctx.f.setCaTranId(ctx.f.getAuthidi());
            ctx.f.setString("WS-PL-FUNC", "INQ ");
            linkToAuthLookupProgram(ctx);
            if (ctx.f.getWsPlStatus().equals("  ")) {
                populateAuthDetail(ctx);
                ctx.f.setErrmsgo(ctx.f.getWsPlMsg());
                sendDataOnlyScreen(ctx);
            } else {
                ctx.f.setErrmsgo(ctx.f.getCaErrMsg());
                sendDataOnlyScreen(ctx);
            }
        }
    }

    /** COBOL paragraph: 2200-BROWSE-NEXT */
    private void browseNextAuthorization(TaskContext ctx) {
        ctx.appService.receiveMap("MPAUINA", ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.f.setWsPauLink(" ");
        ctx.f.setWsPlKey(ctx.f.getAuthidi());
        ctx.f.setString("WS-PL-FUNC", "NXT ");
        linkToAuthLookupProgram(ctx);
        if (ctx.f.getWsPlStatus().equals("  ")) {
            populateAuthDetail(ctx);
            ctx.f.setAuthido(ctx.f.getWsPlKey());
            ctx.f.setErrmsgo(ctx.f.getWsPlMsg());
            sendDataOnlyScreen(ctx);
        } else {
            ctx.f.setErrmsgo(ctx.f.getCaErrMsg());
            sendDataOnlyScreen(ctx);
        }
    }

    /** COBOL paragraph: 3000-CALL-SUB */
    private void linkToAuthLookupProgram(TaskContext ctx) {
        ctx.f.setCaWorkArea(ctx.f.getWsPauLink());
        ctx.f.setCaFromProgram(ctx.f.getWsPgmname());
        ctx.f.setCaToProgram(ctx.f.getWsSubPgm());
        {
            byte[] _linkCa = ctx.f.sliceBytes("ORION-COMMAREA");
            ctx.appService.link(ctx.f.getWsSubPgm(), _linkCa, COMMAREA_LENGTH);
            ctx.f.writeBytes("ORION-COMMAREA", _linkCa);
        }
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.f.setWsPauLink(String.valueOf(ctx.f.getCaWorkArea()));
        if (ctx.f.getWsRespCd() != 0) {
            ctx.f.setCaErrMsg("Unable to link to IMS module OUIMSPA.");
            ctx.f.setWsPlStatus("ER");
        }
    }

    /** COBOL paragraph: 4000-POPULATE-DETAIL */
    private void populateAuthDetail(TaskContext ctx) {
        ctx.f.copyBytes("PAUSEG-REC", "WS-PL-SEGMENT");
        ctx.f.setAuthido(ctx.f.getPaAuthId());
        ctx.f.setPacardo(ctx.f.getPaCardNum());
        ctx.f.setPaaccto(String.format("%011d", ctx.f.getPaAcctId()));
        ctx.f.setWsEdAmt(ctx.f.getPaAmount());
        ctx.f.setPaamto(ctx.f.getString("WS-ED-AMT"));
        ctx.f.setPamercho(ctx.f.getPaMerchant());
        ctx.f.setPareqtso(ctx.f.getPaRequestTs());
        resolveStatusWord(ctx);
        ctx.f.setPastato(ctx.f.getWsStatusWord());
        ctx.f.setPadeco(ctx.f.getPaDecReason());
    }

    /** COBOL paragraph: 4100-STATUS-WORD */
    private void resolveStatusWord(TaskContext ctx) {
        switch (Utility.rtrim(ctx.f.getPaStatus())) {
            case "P" -> {
                ctx.f.setWsStatusWord("PENDING");
            }
            case "A" -> {
                ctx.f.setWsStatusWord("APPROVED");
            }
            case "D" -> {
                ctx.f.setWsStatusWord("DECLINED");
            }
            case "X" -> {
                ctx.f.setWsStatusWord("PURGED");
            }
            default -> {
                ctx.f.setWsStatusWord("UNKNOWN");
            }
        }
    }

    /** COBOL paragraph: 7000-XCTL-MENU */
    private void transferToMenuProgram(TaskContext ctx) {
        ctx.f.setCaFromProgram(ctx.f.getWsPgmname());
        ctx.f.setCaFromTranid(ctx.f.getWsTranid());
        ctx.f.setCaPgmContext(0);
        ctx.appService.xctl(
                ctx.f.getWsMenuPgm(), ctx.f.sliceBytes("ORION-COMMAREA"), COMMAREA_LENGTH);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 8000-POPULATE-HEADER */
    private void populateScreenHeader(TaskContext ctx) {
        ctx.f.setTrnnameo(ctx.f.getWsTranid());
        ctx.f.setPgmnameo(ctx.f.getWsPgmname());
        ctx.f.setTitleo(ctx.f.getWsHdrTitle());
        loadCurrentDateTime(ctx);
        ctx.f.setCurdateo(ctx.f.getWsHdrDate());
        ctx.f.setCurtimeo(ctx.f.getWsHdrTime());
    }

    /** COBOL paragraph: 8100-SEND-DATAONLY */
    private void sendDataOnlyScreen(TaskContext ctx) {
        populateScreenHeader(ctx);
        ctx.appService.sendMap(
                "MPAUINA", ctx.f, ctx.f, /* erase */ false, /* freekb */ false, /* cursor */ false);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 8500-GET-DATE-TIME */
    private void loadCurrentDateTime(TaskContext ctx) {
        ctx.appService.askTime();
        com.appruntime.FormatTimeResult _ftResult =
                ctx.appService.formatTime(null, "YYYYMMDD", "-", ":");
        ctx.f.setWsHdrDate(_ftResult.getDate());
        ctx.f.setWsHdrTime(_ftResult.getTime());
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 9000-RETURN */
    private void returnTransaction(TaskContext ctx) {
        ctx.appService.returnTransid(
                ctx.f.getWsTranid(), ctx.f.sliceBytes("ORION-COMMAREA"), COMMAREA_LENGTH);
    }

    /**
     * Per-request execution context — analogous to CICS Task Control Block. Each HTTP request
     * creates a fresh TaskContext. Thread-safe by design.
     */
    static class TaskContext {
        final WorkingStorage ws;

        final OcpauinFields f;

        final AppService appService;

        final OcpauinLinkParm link = new OcpauinLinkParm();

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OcpauinFields(ws);
        }
    }
}
