package com.generated.orion.octtyp.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.RuntimeConstants;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.common.linkage.OcttypLinkParm;
import com.generated.orion.octtyp.accessor.OcttypFields;
import com.generated.orion.octtyp.metadata.OcttypBmsMetadata;
import com.generated.orion.octtyp.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OCTTYP. */
@Service
public class OcttypService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OcttypService.class);

    private static final int ORION_COMMAREA_LENGTH = 692;

    /**
     * Entry point: executes the CICS program main line. Called by AppRunner after transaction
     * setup.
     */
    @Override
    public void mainLine(AppService appService) {
        TaskContext ctx = new TaskContext(appService);
        runMainLine(ctx);
    }

    @Override
    public String getProgramName() {
        return "OCTTYP";
    }

    @Override
    public String getTransId() {
        return "ORTT";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OcttypBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OcttypBmsMetadata.registerFsetFields(runner);
    }

    @Override
    public FieldMapping getFieldMapping(String mapName) {
        return OcttypBmsMetadata.getFieldMapping(mapName);
    }

    /** COBOL paragraph: 0000-MAIN */
    private void runMainLine(TaskContext ctx) {
        ctx.f.setString("WS-ERR-FLG", "N");
        ctx.f.setCaErrMsg(" ");
        if (ctx.appService.getEibcalen() == 0) {
            ctx.f.setOrionCommarea("");
            sendInitialScreen(ctx);
        } else {
            ctx.f.setOrionCommarea(Utility.groupToString(ctx.appService.getSerializedCommarea()));
            if (ctx.f.getCaPgmContext() == 0) {
                sendInitialScreen(ctx);
            } else {
                processInput(ctx);
            }
        }
        ctx.appService.returnTransid(
                ctx.f.getWsTranid(), ctx.f.sliceBytes("ORION-COMMAREA"), ORION_COMMAREA_LENGTH);
    }

    /** COBOL paragraph: 1000-SEND-INITIAL */
    private void sendInitialScreen(TaskContext ctx) {
        ctx.f.fillLowValues("MTTYPAO");
        populateScreenHeader(ctx);
        ctx.f.setCaWorkArea(
                Utility.setSubstring(
                        String.valueOf(ctx.f.getCaWorkArea()),
                        1,
                        1,
                        String.valueOf(ctx.f.getWsStKey())));
        ctx.f.setErrmsgo(ctx.f.getWsMPrompt());
        ctx.f.setTtcdl((short) -1);
        ctx.appService.sendMap(
                "MTTYPA", ctx.f, ctx.f, /* erase */ true, /* freekb */ false, /* cursor */ true);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.f.setCaPgmContext(1);
    }

    /** COBOL paragraph: 2000-PROCESS-INPUT */
    private void processInput(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.appService.getEibaid(), "3")) {
            transferToMenuProgram(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "4")) {
            sendInitialScreen(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "5")) {
            saveTicketType(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "'")) {
            lookupTicketType(ctx);
        } else {
            ctx.f.setErrmsgo(ctx.f.getWsMsgInvalidKey());
            sendDataOnlyScreen(ctx);
        }
    }

    /** COBOL paragraph: 2050-RECEIVE */
    private void receiveInputMap(TaskContext ctx) {
        ctx.appService.receiveMap("MTTYPA", ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 36) {
            ctx.f.fillLowValues("MTTYPAI");
        }
    }

    /** COBOL paragraph: 2100-LOOKUP */
    private void lookupTicketType(TaskContext ctx) {
        receiveInputMap(ctx);
        if (Utility.fieldEquals(ctx.f.getTtcdi(), " ") || ctx.f.isAllLowValues("TTCDI")) {
            ctx.f.setErrmsgo(ctx.f.getWsMCdReq());
            ctx.f.setTtcdl((short) -1);
            sendDataOnlyScreen(ctx);
        } else {
            ctx.f.setWsTtCd(ctx.f.getTtcdi());
            ctx.f.setWsTtCd(
                    String.valueOf(ctx.f.getWsTtCd())
                            .replace(
                                    String.valueOf(RuntimeConstants.LOW_VALUES),
                                    String.valueOf(" ")));
            readTicketTypeRecord(ctx);
            if (ctx.f.getWsFoundFlg().equals("Y")) {
                ctx.f.setTtcdo(ctx.f.getTtCd());
                ctx.f.setTtdesco(ctx.f.getTtDesc());
                ctx.f.setCaWorkArea(
                        Utility.setSubstring(
                                String.valueOf(ctx.f.getCaWorkArea()),
                                1,
                                1,
                                String.valueOf(ctx.f.getWsStExist())));
                ctx.f.setErrmsgo(ctx.f.getWsMFound());
            } else {
                ctx.f.setTtcdo(ctx.f.getWsTtCd());
                ctx.f.setTtdesco(" ");
                ctx.f.setCaWorkArea(
                        Utility.setSubstring(
                                String.valueOf(ctx.f.getCaWorkArea()),
                                1,
                                1,
                                String.valueOf(ctx.f.getWsStNew())));
                ctx.f.setErrmsgo(ctx.f.getWsMNew());
            }
            ctx.f.setTtdescl((short) -1);
            sendDataOnlyScreen(ctx);
        }
    }

    /** COBOL paragraph: 2300-SAVE */
    private void saveTicketType(TaskContext ctx) {
        String workAreaStatus =
                Utility.padRight(String.valueOf(ctx.f.getCaWorkArea()), 1).substring(0, 1);
        if (!Utility.fieldEquals(workAreaStatus, ctx.f.getWsStExist())
                && !Utility.fieldEquals(workAreaStatus, ctx.f.getWsStNew())) {
            ctx.f.setErrmsgo(ctx.f.getWsMKeyFirst());
            ctx.f.setTtcdl((short) -1);
            sendDataOnlyScreen(ctx);
        } else {
            receiveInputMap(ctx);
            if (Utility.fieldEquals(ctx.f.getTtdesci(), " ") || ctx.f.isAllLowValues("TTDESCI")) {
                ctx.f.setErrmsgo(ctx.f.getWsMDescReq());
                ctx.f.setTtdescl((short) -1);
                sendDataOnlyScreen(ctx);
            } else {
                if (Utility.fieldEquals(workAreaStatus, ctx.f.getWsStExist())) {
                    updateTicketTypeRecord(ctx);
                } else {
                    addTicketTypeRecord(ctx);
                }
                sendDataOnlyScreen(ctx);
            }
        }
    }

    /** COBOL paragraph: 3000-READ-TTYP */
    private void readTicketTypeRecord(TaskContext ctx) {
        ctx.f.setString("WS-FOUND-FLG", "N");
        ctx.f.setTtCd(ctx.f.getWsTtCd());
        ctx.appService.readFile(ctx.f.getWsTtypfile(), ctx.f, String.valueOf(ctx.f.getTtCd()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setString("WS-FOUND-FLG", "Y");
        } else if (ctx.f.getWsRespCd() == 13) {
            ctx.f.setString("WS-FOUND-FLG", "N");
        } else {
            abendUnrecoverableFileError(ctx);
        }
    }

    /** COBOL paragraph: 3500-UPDATE-TTYP */
    private void updateTicketTypeRecord(TaskContext ctx) {
        ctx.f.setTtCd(ctx.f.getWsTtCd());
        ctx.appService.readFileForUpdate(
                ctx.f.getWsTtypfile(), ctx.f, String.valueOf(ctx.f.getTtCd()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setTtDesc(ctx.f.getTtdesci());
            ctx.appService.rewriteFile(ctx.f.getWsTtypfile(), ctx.f);
            ctx.f.setWsRespCd(ctx.appService.getEibresp());
            if (ctx.f.getWsRespCd() == 0) {
                ctx.f.setErrmsgo(ctx.f.getWsMUpdated());
            } else {
                ctx.f.setErrmsgo(ctx.f.getWsMSaveErr());
            }
        } else if (ctx.f.getWsRespCd() == 13) {
            addTicketTypeRecord(ctx);
        } else {
            abendUnrecoverableFileError(ctx);
        }
    }

    /** COBOL paragraph: 3600-ADD-TTYP */
    private void addTicketTypeRecord(TaskContext ctx) {
        ctx.f.setTtypRec("");
        ctx.f.setTtCd(ctx.f.getWsTtCd());
        ctx.f.setTtDesc(ctx.f.getTtdesci());
        ctx.appService.writeFile(ctx.f.getWsTtypfile(), ctx.f, String.valueOf(ctx.f.getTtCd()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setErrmsgo(ctx.f.getWsMAdded());
            ctx.f.setCaWorkArea(
                    Utility.setSubstring(
                            String.valueOf(ctx.f.getCaWorkArea()),
                            1,
                            1,
                            String.valueOf(ctx.f.getWsStExist())));
        } else if (ctx.f.getWsRespCd() == 14) {
            ctx.f.setErrmsgo(ctx.f.getWsMSaveErr());
        } else {
            ctx.f.setErrmsgo(ctx.f.getWsMSaveErr());
        }
    }

    /** COBOL paragraph: 7000-XCTL-MENU */
    private void transferToMenuProgram(TaskContext ctx) {
        ctx.f.setCaFromProgram(ctx.f.getWsPgmname());
        ctx.f.setCaFromTranid(ctx.f.getWsTranid());
        ctx.f.setCaPgmContext(0);
        ctx.appService.xctl(
                ctx.f.getWsMenuPgm(), ctx.f.sliceBytes("ORION-COMMAREA"), ORION_COMMAREA_LENGTH);
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
                "MTTYPA", ctx.f, ctx.f, /* erase */ false, /* freekb */ false, /* cursor */ true);
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

    /** COBOL paragraph: 9500-ABEND-RTN */
    private void abendUnrecoverableFileError(TaskContext ctx) {
        ctx.f.setWsMsgText("OCTTYP: unrecoverable file error. Contact support.");
        ctx.appService.sendText(String.valueOf(ctx.f), true, true);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.appService.returnProgram();
    }

    /**
     * Per-request execution context — analogous to CICS Task Control Block. Each HTTP request
     * creates a fresh TaskContext. Thread-safe by design.
     */
    static class TaskContext {
        final WorkingStorage ws;

        final OcttypFields f;

        final AppService appService;

        final OcttypLinkParm link = new OcttypLinkParm();

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OcttypFields(ws);
        }
    }
}
