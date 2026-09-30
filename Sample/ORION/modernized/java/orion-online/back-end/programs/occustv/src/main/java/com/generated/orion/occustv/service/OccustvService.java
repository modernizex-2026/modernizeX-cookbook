package com.generated.orion.occustv.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.RuntimeConstants;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.common.linkage.OccustvLinkParm;
import com.generated.orion.occustv.accessor.OccustvFields;
import com.generated.orion.occustv.metadata.OccustvBmsMetadata;
import com.generated.orion.occustv.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OCCUSTV. */
@Service
public class OccustvService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OccustvService.class);

    /** Length of the ORION-COMMAREA passed across XCTL/RETURN transaction boundaries. */
    private static final int COMMAREA_LENGTH = 692;

    /** Buffer width used when right-padding fixed-length COBOL field values. */
    private static final int FIELD_BUFFER_LENGTH = 256;

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
        return "OCCUSTV";
    }

    @Override
    public String getTransId() {
        return "ORUV";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OccustvBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OccustvBmsMetadata.registerFsetFields(runner);
    }

    @Override
    public FieldMapping getFieldMapping(String mapName) {
        return OccustvBmsMetadata.getFieldMapping(mapName);
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
        ctx.appService.returnTransid(
                ctx.f.getWsTranid(), ctx.f.sliceBytes("ORION-COMMAREA"), COMMAREA_LENGTH);
    }

    /** COBOL paragraph: 1000-SEND-INITIAL */
    private void sendInitialScreen(TaskContext ctx) {
        ctx.f.fillLowValues("MCUSTVAO");
        populateScreenHeader(ctx);
        ctx.f.setErrmsgo(ctx.f.getWsMPrompt());
        ctx.appService.sendMap(
                "MCUSTVA", ctx.f, ctx.f, /* erase */ true, /* freekb */ false, /* cursor */ false);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.f.setCaPgmContext(1);
    }

    /** COBOL paragraph: 2000-PROCESS-INPUT */
    private void processInput(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.appService.getEibaid(), "3")) {
            transferToMenu(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "4")) {
            sendInitialScreen(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "'")) {
            readAndShowCustomer(ctx);
        } else {
            sendInitialScreen(ctx);
            ctx.f.setErrmsgo(ctx.f.getWsMsgInvalidKey());
            sendDataOnly(ctx);
        }
    }

    /** COBOL paragraph: 2100-READ-AND-SHOW */
    private void readAndShowCustomer(TaskContext ctx) {
        ctx.appService.receiveMap("MCUSTVA", ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        editCustomerId(ctx);
        if (ctx.f.getWsErrFlg().equals("Y")) {
            sendDataOnly(ctx);
        } else {
            ctx.f.setCuId(ctx.f.getWsInCustId());
            ctx.f.setCaCustId(ctx.f.getCuId());
            readCustomerRecord(ctx);
            if (ctx.f.getWsFoundFlg().equals("Y")) {
                populateCustomerDetail(ctx);
                ctx.f.setErrmsgo(ctx.f.getWsMCustFound());
                sendDataOnly(ctx);
            } else {
                ctx.f.setErrmsgo(ctx.f.getWsMCustNotfnd());
                sendDataOnly(ctx);
            }
        }
    }

    /** COBOL paragraph: 2200-EDIT-CUST-ID */
    private void editCustomerId(TaskContext ctx) {
        ctx.f.setString("WS-ERR-FLG", "N");
        ctx.f.setWsInCustId(0);
        if (Utility.fieldEquals(ctx.f.getCustidi(), " ") || ctx.f.isAllLowValues("CUSTIDI")) {
            ctx.f.setString("WS-ERR-FLG", "Y");
            ctx.f.setErrmsgo(ctx.f.getWsMIdRequired());
        } else {
            ctx.f.setWsValStr(" ");
            ctx.f.setWsValStr(ctx.f.getCustidi());
            ctx.f.setWsValLen(9);
            validateNumericField(ctx);
            if (ctx.f.getWsValOk().equals("Y")) {
                ctx.f.setWsInCustId(
                        Utility.parseNumeric(
                                        String.valueOf(
                                                Utility.padRight(
                                                                String.valueOf(ctx.f.getWsValStr()),
                                                                FIELD_BUFFER_LENGTH)
                                                        .substring(0, ctx.f.getWsValLen())))
                                .intValue());
            } else {
                ctx.f.setString("WS-ERR-FLG", "Y");
                ctx.f.setErrmsgo(ctx.f.getWsMIdNotnum());
            }
        }
    }

    /** COBOL paragraph: 3000-READ-CUST */
    private void readCustomerRecord(TaskContext ctx) {
        ctx.f.setString("WS-FOUND-FLG", "N");
        ctx.appService.readFile(ctx.f.getWsCustfile(), ctx.f, String.valueOf(ctx.f.getCuId()), 0);
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

    /** COBOL paragraph: 4000-POPULATE-DETAIL */
    private void populateCustomerDetail(TaskContext ctx) {
        ctx.f.setCustido(String.format("%09d", ctx.f.getCuId()));
        formatCustomerFullName(ctx);
        ctx.f.setCunameo(ctx.f.getWsFullName());
        ctx.f.setCuaddro(ctx.f.getCuAddrLine1());
        ctx.f.setCucityo(ctx.f.getCuAddrCity());
        ctx.f.setCuphoneo(ctx.f.getCuPhone1());
        ctx.f.setCuficoo(String.format("%03d", ctx.f.getCuFicoScore()));
    }

    /** COBOL paragraph: 4100-FORMAT-NAME */
    private void formatCustomerFullName(TaskContext ctx) {
        ctx.f.setWsFullName(" ");
        {
            StringBuilder sb = new StringBuilder();
            sb.append(String.valueOf(ctx.f.getCuFirstName()).split(String.valueOf(" "), 2)[0]);
            sb.append(String.valueOf(" "));
            sb.append(String.valueOf(ctx.f.getCuLastName()).split(String.valueOf(" "), 2)[0]);
            ctx.f.setWsFullName(sb.toString());
        }
    }

    /** COBOL paragraph: 7000-XCTL-MENU */
    private void transferToMenu(TaskContext ctx) {
        ctx.f.setCaFromProgram(ctx.f.getWsPgmname());
        ctx.f.setCaFromTranid(ctx.f.getWsTranid());
        ctx.f.setCaPgmContext(0);
        ctx.appService.xctl(
                ctx.f.getWsMenuPgm(), ctx.f.sliceBytes("ORION-COMMAREA"), COMMAREA_LENGTH);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 7600-VALIDATE-NUM */
    private void validateNumericField(TaskContext ctx) {
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
                                                        FIELD_BUFFER_LENGTH)
                                                .substring(0, ctx.f.getWsValLen()))
                                .replace(
                                        String.valueOf(RuntimeConstants.LOW_VALUES),
                                        String.valueOf(" "))));
        for (ctx.f.setWsValI(1);
                ctx.f.getWsValI() <= ctx.f.getWsValLen();
                ctx.f.setWsValI(ctx.f.getWsValI() + 1)) {
            ctx.f.setWsValCh(
                    Utility.padRight(String.valueOf(ctx.f.getWsValStr()), FIELD_BUFFER_LENGTH)
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
    private void populateScreenHeader(TaskContext ctx) {
        ctx.f.setTrnnameo(ctx.f.getWsTranid());
        ctx.f.setPgmnameo(ctx.f.getWsPgmname());
        ctx.f.setTitleo(ctx.f.getWsHdrTitle());
        loadCurrentDateTime(ctx);
        ctx.f.setCurdateo(ctx.f.getWsHdrDate());
        ctx.f.setCurtimeo(ctx.f.getWsHdrTime());
    }

    /** COBOL paragraph: 8100-SEND-DATAONLY */
    private void sendDataOnly(TaskContext ctx) {
        populateScreenHeader(ctx);
        ctx.appService.sendMap(
                "MCUSTVA", ctx.f, ctx.f, /* erase */ false, /* freekb */ false, /* cursor */ false);
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

    /**
     * Per-request execution context — analogous to CICS Task Control Block. Each HTTP request
     * creates a fresh TaskContext. Thread-safe by design.
     */
    static class TaskContext {
        final WorkingStorage ws;

        final OccustvFields f;

        final AppService appService;

        final OccustvLinkParm link = new OccustvLinkParm();

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OccustvFields(ws);
        }
    }
}
