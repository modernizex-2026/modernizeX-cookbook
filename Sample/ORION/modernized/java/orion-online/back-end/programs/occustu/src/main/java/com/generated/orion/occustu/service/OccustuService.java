package com.generated.orion.occustu.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.RuntimeConstants;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.common.linkage.OccustuLinkParm;
import com.generated.orion.occustu.accessor.OccustuFields;
import com.generated.orion.occustu.metadata.OccustuBmsMetadata;
import com.generated.orion.occustu.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OCCUSTU. */
@Service
public class OccustuService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OccustuService.class);

    /** Pad length used when normalizing WS-VAL-STR for numeric validation. */
    private static final int VAL_STR_PAD_LENGTH = 256;

    /** Serialized length of ORION-COMMAREA passed between programs. */
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
        return "OCCUSTU";
    }

    @Override
    public String getTransId() {
        return "ORUU";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OccustuBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OccustuBmsMetadata.registerFsetFields(runner);
    }

    @Override
    public FieldMapping getFieldMapping(String mapName) {
        return OccustuBmsMetadata.getFieldMapping(mapName);
    }

    /** COBOL paragraph: 0000-MAIN */
    private void runMainProgram(TaskContext ctx) {
        ctx.f.setString("WS-ERR-FLG", "N");
        ctx.f.setCaErrMsg(" ");
        if (ctx.appService.getEibcalen() == 0) {
            sendInitial(ctx);
        } else {
            ctx.f.setOrionCommarea(Utility.groupToString(ctx.appService.getSerializedCommarea()));
            ctx.f.setWsStateFlag(
                    Utility.padRight(String.valueOf(ctx.f.getCaWorkArea()), 1).substring(0, 1));
            if (ctx.f.getCaPgmContext() == 0) {
                sendInitial(ctx);
            } else {
                processInput(ctx);
            }
        }
        returnTransaction(ctx);
    }

    /** COBOL paragraph: 1000-SEND-INITIAL */
    private void sendInitial(TaskContext ctx) {
        ctx.f.fillLowValues("MCUSTUAO");
        setStateFlag(ctx, "N");
        ctx.f.setCaCustId(0);
        populateHeader(ctx);
        ctx.f.setErrmsgo(ctx.f.getWsMPrompt());
        ctx.f.setCustidl((short) -1);
        ctx.appService.sendMap(
                "MCUSTUA", ctx.f, ctx.f, /* erase */ true, /* freekb */ false, /* cursor */ true);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.f.setCaPgmContext(1);
    }

    /** COBOL paragraph: 2000-PROCESS-INPUT */
    private void processInput(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.appService.getEibaid(), "3")) {
            xctlMenu(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "4")) {
            sendInitial(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "'")) {
            processEnter(ctx);
        } else {
            sendInitial(ctx);
            ctx.f.setErrmsgo(ctx.f.getWsMsgInvalidKey());
            sendDataonly(ctx);
        }
    }

    /** COBOL paragraph: 2100-PROCESS-ENTER */
    private void processEnter(TaskContext ctx) {
        ctx.appService.receiveMap("MCUSTUA", ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        editCustId(ctx);
        if (ctx.f.getWsErrFlg().equals("Y")) {
            ctx.f.setCustidl((short) -1);
            sendDataonly(ctx);
        } else {
            if (ctx.f.getWsStateFlag().equals("N")
                    || ctx.f.getWsInCustId() != ctx.f.getCaCustId()) {
                loadCust(ctx);
            } else {
                applyUpdate(ctx);
            }
        }
    }

    /** COBOL paragraph: 2200-EDIT-CUST-ID */
    private void editCustId(TaskContext ctx) {
        ctx.f.setString("WS-ERR-FLG", "N");
        ctx.f.setWsInCustId(0);
        if (Utility.fieldEquals(ctx.f.getCustidi(), " ") || ctx.f.isAllLowValues("CUSTIDI")) {
            ctx.f.setString("WS-ERR-FLG", "Y");
            ctx.f.setErrmsgo(ctx.f.getWsMIdRequired());
        } else {
            ctx.f.setWsValStr(" ");
            ctx.f.setWsValStr(ctx.f.getCustidi());
            ctx.f.setWsValLen(9);
            validateNum(ctx);
            if (ctx.f.getWsValOk().equals("Y")) {
                ctx.f.setWsInCustId(
                        Utility.parseNumeric(
                                        String.valueOf(
                                                Utility.padRight(
                                                                String.valueOf(ctx.f.getWsValStr()),
                                                                VAL_STR_PAD_LENGTH)
                                                        .substring(0, ctx.f.getWsValLen())))
                                .intValue());
            } else {
                ctx.f.setString("WS-ERR-FLG", "Y");
                ctx.f.setErrmsgo(ctx.f.getWsMIdNotnum());
            }
        }
    }

    /** COBOL paragraph: 2300-LOAD-CUST */
    private void loadCust(TaskContext ctx) {
        ctx.f.setCuId(ctx.f.getWsInCustId());
        readCust(ctx);
        if (ctx.f.getWsFoundFlg().equals("Y")) {
            populateDetail(ctx);
            setStateFlag(ctx, "L");
            ctx.f.setCaCustId(ctx.f.getWsInCustId());
            sendFnameError(ctx, ctx.f.getWsMLoaded());
        } else {
            setStateFlag(ctx, "N");
            ctx.f.setErrmsgo(ctx.f.getWsMCustNotfnd());
            ctx.f.setCustidl((short) -1);
            sendDataonly(ctx);
        }
    }

    /** COBOL paragraph: 2400-APPLY-UPDATE */
    private void applyUpdate(TaskContext ctx) {
        editFields(ctx);
        if (ctx.f.getWsEditFlag().equals("N")) {
            sendDataonly(ctx);
        } else {
            ctx.f.setCuId(ctx.f.getCaCustId());
            readForUpdate(ctx);
            if (ctx.f.getWsFoundFlg().equals("Y")) {
                moveChanges(ctx);
                rewriteCust(ctx);
            } else {
                setStateFlag(ctx, "N");
                ctx.f.setErrmsgo(ctx.f.getWsMDeleted());
                ctx.f.setCustidl((short) -1);
                sendDataonly(ctx);
            }
        }
    }

    /** COBOL paragraph: 3000-READ-CUST */
    private void readCust(TaskContext ctx) {
        readCustomerRecord(ctx, false);
    }

    /** COBOL paragraph: 4000-POPULATE-DETAIL */
    private void populateDetail(TaskContext ctx) {
        ctx.f.setCustido(String.format("%09d", ctx.f.getCuId()));
        ctx.f.setCufnamo(ctx.f.getCuFirstName());
        ctx.f.setCulnamo(ctx.f.getCuLastName());
        ctx.f.setCuaddro(ctx.f.getCuAddrLine1());
        ctx.f.setCucityo(ctx.f.getCuAddrCity());
        ctx.f.setCuphoneo(ctx.f.getCuPhone1());
    }

    /** COBOL paragraph: 5000-EDIT-FIELDS */
    private void editFields(TaskContext ctx) {
        ctx.f.setString("WS-EDIT-FLAG", "Y");
        if (Utility.fieldEquals(ctx.f.getCufnami(), " ") || ctx.f.isAllLowValues("CUFNAMI")) {
            ctx.f.setString("WS-EDIT-FLAG", "N");
            ctx.f.setErrmsgo(ctx.f.getWsMFnameReq());
            ctx.f.setCufnaml((short) -1);
        }
        if (ctx.f.getWsEditFlag().equals("Y")) {
            if (Utility.fieldEquals(ctx.f.getCulnami(), " ") || ctx.f.isAllLowValues("CULNAMI")) {
                ctx.f.setString("WS-EDIT-FLAG", "N");
                ctx.f.setErrmsgo(ctx.f.getWsMLnameReq());
                ctx.f.setCulnaml((short) -1);
            }
        }
        if (ctx.f.getWsEditFlag().equals("Y")) {
            if (Utility.fieldEquals(ctx.f.getCuaddri(), " ") || ctx.f.isAllLowValues("CUADDRI")) {
                ctx.f.setString("WS-EDIT-FLAG", "N");
                ctx.f.setErrmsgo(ctx.f.getWsMAddrReq());
                ctx.f.setCuaddrl((short) -1);
            }
        }
        if (ctx.f.getWsEditFlag().equals("Y")) {
            if (Utility.fieldEquals(ctx.f.getCucityi(), " ") || ctx.f.isAllLowValues("CUCITYI")) {
                ctx.f.setString("WS-EDIT-FLAG", "N");
                ctx.f.setErrmsgo(ctx.f.getWsMCityReq());
                ctx.f.setCucityl((short) -1);
            }
        }
        if (ctx.f.getWsEditFlag().equals("Y")) {
            if (Utility.fieldEquals(ctx.f.getCuphonei(), " ") || ctx.f.isAllLowValues("CUPHONEI")) {
                ctx.f.setString("WS-EDIT-FLAG", "N");
                ctx.f.setErrmsgo(ctx.f.getWsMPhoneReq());
                ctx.f.setCuphonel((short) -1);
            }
        }
    }

    /** COBOL paragraph: 6000-READ-FOR-UPDATE */
    private void readForUpdate(TaskContext ctx) {
        readCustomerRecord(ctx, true);
    }

    /** COBOL paragraph: 6100-MOVE-CHANGES */
    private void moveChanges(TaskContext ctx) {
        ctx.f.setCuFirstName(ctx.f.getCufnami());
        ctx.f.setCuLastName(ctx.f.getCulnami());
        ctx.f.setCuAddrLine1(ctx.f.getCuaddri());
        ctx.f.setCuAddrCity(ctx.f.getCucityi());
        ctx.f.setCuPhone1(ctx.f.getCuphonei());
    }

    /** COBOL paragraph: 6200-REWRITE-CUST */
    private void rewriteCust(TaskContext ctx) {
        ctx.appService.rewriteFile(ctx.f.getWsCustfile(), ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            populateDetail(ctx);
            setStateFlag(ctx, "L");
            sendFnameError(ctx, ctx.f.getWsMUpdated());
        } else {
            sendFnameError(ctx, ctx.f.getWsMUpdError());
        }
    }

    /** COBOL paragraph: 7000-XCTL-MENU */
    private void xctlMenu(TaskContext ctx) {
        ctx.f.setCaFromProgram(ctx.f.getWsPgmname());
        ctx.f.setCaFromTranid(ctx.f.getWsTranid());
        ctx.f.setCaPgmContext(0);
        ctx.appService.xctl(
                ctx.f.getWsMenuPgm(), ctx.f.sliceBytes("ORION-COMMAREA"), COMMAREA_LENGTH);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 7600-VALIDATE-NUM */
    private void validateNum(TaskContext ctx) {
        ctx.f.setString("WS-VAL-OK", "Y");
        ctx.f.setWsValDigits(0);
        ctx.f.setSubstring(
                "WS-VAL-STR",
                1,
                ctx.f.getWsValLen(),
                String.valueOf(
                        String.valueOf(
                                        Utility.padRight(
                                                        String.valueOf(ctx.f.getWsValStr()),
                                                        VAL_STR_PAD_LENGTH)
                                                .substring(0, ctx.f.getWsValLen()))
                                .replace(
                                        String.valueOf(RuntimeConstants.LOW_VALUES),
                                        String.valueOf(" "))));
        for (ctx.f.setWsValI(1);
                ctx.f.getWsValI() <= ctx.f.getWsValLen();
                ctx.f.setWsValI(ctx.f.getWsValI() + 1)) {
            ctx.f.setWsValCh(
                    Utility.padRight(String.valueOf(ctx.f.getWsValStr()), VAL_STR_PAD_LENGTH)
                            .substring(ctx.f.getWsValI() - 1, ctx.f.getWsValI() - 1 + 1));
            if (Utility.fieldEquals(ctx.f.getWsValCh(), " ")) {
                /* CONTINUE */
            } else {
                if ((ctx.f.getWsValCh().compareTo("0") >= 0)
                        && (ctx.f.getWsValCh().compareTo("9") <= 0)) {
                    ctx.f.setWsValDigits(ctx.f.getWsValDigits() + 1);
                } else {
                    ctx.f.setString("WS-VAL-OK", "N");
                }
            }
        }
        if (ctx.f.getWsValDigits() == 0) {
            ctx.f.setString("WS-VAL-OK", "N");
        }
    }

    /** COBOL paragraph: 8000-POPULATE-HEADER */
    private void populateHeader(TaskContext ctx) {
        ctx.f.setTrnnameo(ctx.f.getWsTranid());
        ctx.f.setPgmnameo(ctx.f.getWsPgmname());
        ctx.f.setTitleo(ctx.f.getWsHdrTitle());
        getDateTime(ctx);
        ctx.f.setCurdateo(ctx.f.getWsHdrDate());
        ctx.f.setCurtimeo(ctx.f.getWsHdrTime());
    }

    /** COBOL paragraph: 8100-SEND-DATAONLY */
    private void sendDataonly(TaskContext ctx) {
        populateHeader(ctx);
        ctx.appService.sendMap(
                "MCUSTUA", ctx.f, ctx.f, /* erase */ false, /* freekb */ false, /* cursor */ true);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 8500-GET-DATE-TIME */
    private void getDateTime(TaskContext ctx) {
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

    /** Set WS-STATE-FLAG and mirror it into byte 1 of CA-WORK-AREA. */
    private void setStateFlag(TaskContext ctx, String flag) {
        ctx.f.setString("WS-STATE-FLAG", flag);
        ctx.f.setCaWorkArea(
                Utility.setSubstring(
                        String.valueOf(ctx.f.getCaWorkArea()),
                        1,
                        1,
                        String.valueOf(ctx.f.getWsStateFlag())));
    }

    /** Report an error message and redisplay the map with the cursor on first name. */
    private void sendFnameError(TaskContext ctx, String msg) {
        ctx.f.setErrmsgo(msg);
        ctx.f.setCufnaml((short) -1);
        sendDataonly(ctx);
    }

    /** Read the customer record by CU-ID, for browse or for update. */
    private void readCustomerRecord(TaskContext ctx, boolean forUpdate) {
        ctx.f.setString("WS-FOUND-FLG", "N");
        if (forUpdate) {
            ctx.appService.readFileForUpdate(
                    ctx.f.getWsCustfile(), ctx.f, String.valueOf(ctx.f.getCuId()), 0);
        } else {
            ctx.appService.readFile(
                    ctx.f.getWsCustfile(), ctx.f, String.valueOf(ctx.f.getCuId()), 0);
        }
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setString("WS-FOUND-FLG", "Y");
        } else if (ctx.f.getWsRespCd() == 13) {
            ctx.f.setString("WS-FOUND-FLG", "N");
        } else {
            ctx.f.setString("WS-FOUND-FLG", "N");
            ctx.f.setErrmsgo(ctx.f.getWsMReadError());
        }
    }

    /**
     * Per-request execution context — analogous to CICS Task Control Block. Each HTTP request
     * creates a fresh TaskContext. Thread-safe by design.
     */
    static class TaskContext {
        final WorkingStorage ws;

        final OccustuFields f;

        final AppService appService;

        final OccustuLinkParm link = new OccustuLinkParm();

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OccustuFields(ws);
        }
    }
}
