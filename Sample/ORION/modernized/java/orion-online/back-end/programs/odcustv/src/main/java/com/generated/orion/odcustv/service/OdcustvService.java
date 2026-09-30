package com.generated.orion.odcustv.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.common.linkage.OdcustvLinkParm;
import com.generated.orion.odcustv.accessor.OdcustvFields;
import com.generated.orion.odcustv.dao.OdcustvDao;
import com.generated.orion.odcustv.metadata.OdcustvBmsMetadata;
import com.generated.orion.odcustv.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program ODCUSTV. */
@Service
public class OdcustvService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OdcustvService.class);

    /** ORION-COMMAREA length shared by XCTL and RETURN. */
    private static final int COMMAREA_LENGTH = 692;

    @Autowired private OdcustvDao dao;

    /**
     * Entry point: executes the CICS program main line. Called by AppRunner after transaction
     * setup.
     */
    @Override
    public void mainLine(AppService appService) {
        TaskContext ctx = new TaskContext(appService);
        ctx.dao = this.dao;
        runMainProgram(ctx);
    }

    @Override
    public String getProgramName() {
        return "ODCUSTV";
    }

    @Override
    public String getTransId() {
        return "OD04";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OdcustvBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OdcustvBmsMetadata.registerFsetFields(runner);
    }

    @Override
    public FieldMapping getFieldMapping(String mapName) {
        return OdcustvBmsMetadata.getFieldMapping(mapName);
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
        populateHeader(ctx);
        ctx.f.setErrmsgo("Enter customer id and press ENTER.");
        ctx.appService.sendMap(
                "MCUSTVA", ctx.f, ctx.f, /* erase */ true, /* freekb */ false, /* cursor */ false);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.f.setCaPgmContext(1);
    }

    /** COBOL paragraph: 2000-PROCESS-INPUT */
    private void processInput(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.appService.getEibaid(), "3")) {
            transferToMenuProgram(ctx);
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
        if (Utility.fieldEquals(ctx.f.getCustidi(), " ") || ctx.f.isAllLowValues("CUSTIDI")) {
            ctx.f.setErrmsgo(ctx.f.getWsMsgRequired());
            sendDataOnly(ctx);
        } else {
            if (Utility.isNumeric(ctx.f.getRawString("CUSTIDI"))) {
                ctx.f.setCuId(Utility.parseNumeric(ctx.f.getCustidi()).intValue());
                ctx.f.setCaCustId(ctx.f.getCuId());
                readCustomer(ctx);
                if (ctx.f.getWsFoundFlg().equals("Y")) {
                    populateCustomerDetail(ctx);
                    ctx.f.setErrmsgo("Customer displayed.");
                    sendDataOnly(ctx);
                } else {
                    ctx.f.setErrmsgo(ctx.f.getWsMsgNotfnd());
                    sendDataOnly(ctx);
                }
            } else {
                ctx.f.setErrmsgo("Customer id must be numeric.");
                sendDataOnly(ctx);
            }
        }
    }

    /** COBOL paragraph: 3000-READ-CUST */
    private void readCustomer(TaskContext ctx) {
        ctx.f.setString("WS-FOUND-FLG", "N");
        // DB2 SELECT INTO — ORION.CUST
        {
            java.util.Map<String, Object> _sqlResult = dao.selectOrionCust(ctx.f.getCuId());
            if (!_sqlResult.containsKey("SQLCODE")) {
                ctx.f.setCuFirstName(
                        (_sqlResult.get("CU_FIRST_NAME") == null
                                ? ""
                                : String.valueOf(_sqlResult.get("CU_FIRST_NAME"))));
                ctx.f.setCuMiddleName(
                        (_sqlResult.get("CU_MIDDLE_NAME") == null
                                ? ""
                                : String.valueOf(_sqlResult.get("CU_MIDDLE_NAME"))));
                ctx.f.setCuLastName(
                        (_sqlResult.get("CU_LAST_NAME") == null
                                ? ""
                                : String.valueOf(_sqlResult.get("CU_LAST_NAME"))));
                ctx.f.setCuAddrLine1(
                        (_sqlResult.get("CU_ADDR_LINE_1") == null
                                ? ""
                                : String.valueOf(_sqlResult.get("CU_ADDR_LINE_1"))));
                ctx.f.setCuAddrCity(
                        (_sqlResult.get("CU_ADDR_CITY") == null
                                ? ""
                                : String.valueOf(_sqlResult.get("CU_ADDR_CITY"))));
                ctx.f.setCuPhone1(
                        (_sqlResult.get("CU_PHONE_1") == null
                                ? ""
                                : String.valueOf(_sqlResult.get("CU_PHONE_1"))));
                ctx.f.setCuFicoScore(
                        (_sqlResult.get("CU_FICO_SCORE") == null
                                ? 0
                                : ((Number) _sqlResult.get("CU_FICO_SCORE")).intValue()));
                ctx.f.setSqlcode(0);
            } else {
                ctx.f.setSqlcode(
                        _sqlResult.get("SQLCODE") != null
                                ? (Integer) _sqlResult.get("SQLCODE")
                                : 0);
            }
        }
        switch (ctx.f.getSqlcode()) {
            case 0 -> {
                ctx.f.setString("WS-FOUND-FLG", "Y");
            }
            case 100 -> {
                ctx.f.setString("WS-FOUND-FLG", "N");
            }
            default -> {
                ctx.f.setString("WS-FOUND-FLG", "N");
                ctx.f.setErrmsgo("Error reading customer table.");
            }
        }
    }

    /** COBOL paragraph: 4000-POPULATE-DETAIL */
    private void populateCustomerDetail(TaskContext ctx) {
        ctx.f.setWsFullName(" ");
        {
            StringBuilder sb = new StringBuilder();
            sb.append(String.valueOf(ctx.f.getCuFirstName()).split(String.valueOf(" "), 2)[0]);
            sb.append(String.valueOf(" "));
            sb.append(String.valueOf(ctx.f.getCuMiddleName()).split(String.valueOf(" "), 2)[0]);
            sb.append(String.valueOf(" "));
            sb.append(String.valueOf(ctx.f.getCuLastName()).split(String.valueOf(" "), 2)[0]);
            ctx.f.setWsFullName(sb.toString());
            /* ON OVERFLOW */ ;
            /* CONTINUE */
        }
        ctx.f.setCustido(String.format("%09d", ctx.f.getCuId()));
        ctx.f.setCunameo(ctx.f.getWsFullName());
        ctx.f.setCuaddro(ctx.f.getCuAddrLine1());
        ctx.f.setCucityo(ctx.f.getCuAddrCity());
        ctx.f.setCuphoneo(ctx.f.getCuPhone1());
        ctx.f.setCuficoo(String.format("%03d", ctx.f.getCuFicoScore()));
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
    private void populateHeader(TaskContext ctx) {
        ctx.f.setTrnnameo(ctx.f.getWsTranid());
        ctx.f.setPgmnameo(ctx.f.getWsPgmname());
        ctx.f.setTitleo(ctx.f.getWsHdrTitle());
        fetchCurrentDateTime(ctx);
        ctx.f.setCurdateo(ctx.f.getWsHdrDate());
        ctx.f.setCurtimeo(ctx.f.getWsHdrTime());
    }

    /** COBOL paragraph: 8100-SEND-DATAONLY */
    private void sendDataOnly(TaskContext ctx) {
        populateHeader(ctx);
        ctx.appService.sendMap(
                "MCUSTVA", ctx.f, ctx.f, /* erase */ false, /* freekb */ false, /* cursor */ false);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 8500-GET-DATE-TIME */
    private void fetchCurrentDateTime(TaskContext ctx) {
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

        final OdcustvFields f;

        final AppService appService;

        final OdcustvLinkParm link = new OdcustvLinkParm();

        OdcustvDao dao;

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OdcustvFields(ws);
        }
    }
}
