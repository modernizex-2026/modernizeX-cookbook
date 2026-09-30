package com.generated.orion.ocusra.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.common.linkage.OcusraLinkParm;
import com.generated.orion.ocusra.accessor.OcusraFields;
import com.generated.orion.ocusra.metadata.OcusraBmsMetadata;
import com.generated.orion.ocusra.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OCUSRA. */
@Service
public class OcusraService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OcusraService.class);

    /** Byte length of the ORION-COMMAREA passed across XCTL/RETURN TRANSID. */
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
        return "OCUSRA";
    }

    @Override
    public String getTransId() {
        return "ORUA";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OcusraBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OcusraBmsMetadata.registerFsetFields(runner);
    }

    @Override
    public FieldMapping getFieldMapping(String mapName) {
        return OcusraBmsMetadata.getFieldMapping(mapName);
    }

    /** COBOL paragraph: 0000-MAIN */
    private void runMainProgram(TaskContext ctx) {
        ctx.f.setString("WS-ERR-FLG", "N");
        if (ctx.appService.getEibcalen() == 0) {
            ctx.f.setOrionCommarea("");
            sendInitialScreen(ctx);
        } else {
            ctx.f.setOrionCommarea(Utility.groupToString(ctx.appService.getSerializedCommarea()));
            ctx.f.setCaErrMsg(" ");
            if (ctx.f.getCaPgmContext() == 0) {
                sendInitialScreen(ctx);
            } else {
                dispatchInputByAid(ctx);
            }
        }
        returnToCics(ctx);
    }

    /** COBOL paragraph: 1000-SEND-INITIAL */
    private void sendInitialScreen(TaskContext ctx) {
        ctx.f.fillLowValues("MUSRAAO");
        populateScreenHeader(ctx);
        ctx.f.setErrmsgo("Enter new user details and press ENTER.");
        ctx.appService.sendMap(
                "MUSRAA", ctx.f, ctx.f, /* erase */ true, /* freekb */ false, /* cursor */ false);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.f.setCaPgmContext(1);
    }

    /** COBOL paragraph: 2000-PROCESS-INPUT */
    private void dispatchInputByAid(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.appService.getEibaid(), "3")) {
            transferToAdminMenu(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "4")) {
            sendInitialScreen(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "'")) {
            addNewUser(ctx);
        } else {
            sendInitialScreen(ctx);
            ctx.f.setErrmsgo(ctx.f.getWsMsgInvalidKey());
            sendDataOnlyScreen(ctx);
        }
    }

    /** COBOL paragraph: 2100-ADD-USER */
    private void addNewUser(TaskContext ctx) {
        receiveUserMapInput(ctx);
        validateUserInput(ctx);
        if (ctx.f.getWsEditFlg().equals("N")) {
            repopulateUserFields(ctx);
            sendDataOnlyScreen(ctx);
        } else {
            writeUserRecord(ctx);
            if (ctx.f.getWsErrFlg().equals("Y")) {
                repopulateUserFields(ctx);
                sendDataOnlyScreen(ctx);
            } else {
                confirmUserAdded(ctx);
            }
        }
    }

    /** COBOL paragraph: 2110-RECEIVE-MAP */
    private void receiveUserMapInput(TaskContext ctx) {
        ctx.appService.receiveMap("MUSRAA", ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 2120-VALIDATE-INPUT */
    private void validateUserInput(TaskContext ctx) {
        ctx.f.setString("WS-EDIT-FLG", "Y");
        if (checkRequiredField(
                ctx,
                Utility.fieldEquals(ctx.f.getUseridi(), " ") || ctx.f.isAllLowValues("USERIDI"),
                "User id is required.")) {
            return;
        }
        if (checkRequiredField(
                ctx,
                Utility.fieldEquals(ctx.f.getUsfnami(), " ") || ctx.f.isAllLowValues("USFNAMI"),
                "First name is required.")) {
            return;
        }
        if (checkRequiredField(
                ctx,
                Utility.fieldEquals(ctx.f.getUslnami(), " ") || ctx.f.isAllLowValues("USLNAMI"),
                "Last name is required.")) {
            return;
        }
        if (checkRequiredField(
                ctx,
                Utility.fieldEquals(ctx.f.getUspwdi(), " ") || ctx.f.isAllLowValues("USPWDI"),
                "Password is required.")) {
            return;
        }
        ctx.f.setWsTypeIn(ctx.f.getUstypei());
        if (!Utility.fieldEquals(ctx.f.getWsTypeIn(), "A")
                && !Utility.fieldEquals(ctx.f.getWsTypeIn(), "U")) {
            ctx.f.setString("WS-EDIT-FLG", "N");
            ctx.f.setErrmsgo("Type must be A (admin) or U (user).");
        }
    }

    /** COBOL paragraph: 3000-WRITE-USER */
    private void writeUserRecord(TaskContext ctx) {
        ctx.f.setString("WS-ERR-FLG", "N");
        ctx.f.setUserRec(" ");
        ctx.f.setUsId(ctx.f.getUseridi());
        ctx.f.setUsFirstName(ctx.f.getUsfnami());
        ctx.f.setUsLastName(ctx.f.getUslnami());
        ctx.f.setUsPassword(ctx.f.getUspwdi());
        ctx.f.setUsType(ctx.f.getWsTypeIn());
        ctx.appService.writeFile(ctx.f.getWsUsrsec(), ctx.f, String.valueOf(ctx.f.getUsId()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            /* CONTINUE */
        } else if (ctx.f.getWsRespCd() == 14) {
            ctx.f.setString("WS-ERR-FLG", "Y");
            ctx.f.setErrmsgo("User id already exists.");
        } else {
            ctx.f.setString("WS-ERR-FLG", "Y");
            ctx.f.setErrmsgo("Error writing user file.");
        }
    }

    /** COBOL paragraph: 4000-REPOPULATE */
    private void repopulateUserFields(TaskContext ctx) {
        ctx.f.setUserido(ctx.f.getUseridi());
        ctx.f.setUsfnamo(ctx.f.getUsfnami());
        ctx.f.setUslnamo(ctx.f.getUslnami());
        ctx.f.setUspwdo(ctx.f.getUspwdi());
        ctx.f.setUstypeo(ctx.f.getUstypei());
    }

    /** COBOL paragraph: 4100-CONFIRM-ADD */
    private void confirmUserAdded(TaskContext ctx) {
        ctx.f.fillLowValues("MUSRAAO");
        ctx.f.setErrmsgo("User added successfully.");
        sendDataOnlyScreen(ctx);
    }

    /** COBOL paragraph: 7000-XCTL-ADMEN */
    private void transferToAdminMenu(TaskContext ctx) {
        ctx.f.setCaFromProgram(ctx.f.getWsPgmname());
        ctx.f.setCaFromTranid(ctx.f.getWsTranid());
        ctx.f.setCaPgmContext(0);
        ctx.appService.xctl(
                ctx.f.getWsAdmenPgm(), ctx.f.sliceBytes("ORION-COMMAREA"), COMMAREA_LENGTH);
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
                "MUSRAA", ctx.f, ctx.f, /* erase */ false, /* freekb */ false, /* cursor */ false);
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
    private void returnToCics(TaskContext ctx) {
        ctx.appService.returnTransid(
                ctx.f.getWsTranid(), ctx.f.sliceBytes("ORION-COMMAREA"), COMMAREA_LENGTH);
    }

    /** Flag a required screen field as missing: set the edit-failed state and error message. */
    private boolean checkRequiredField(TaskContext ctx, boolean isMissing, String message) {
        if (isMissing) {
            ctx.f.setString("WS-EDIT-FLG", "N");
            ctx.f.setErrmsgo(message);
            return true;
        }
        return false;
    }

    /**
     * Per-request execution context — analogous to CICS Task Control Block. Each HTTP request
     * creates a fresh TaskContext. Thread-safe by design.
     */
    static class TaskContext {
        final WorkingStorage ws;

        final OcusraFields f;

        final AppService appService;

        final OcusraLinkParm link = new OcusraLinkParm();

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OcusraFields(ws);
        }
    }
}
