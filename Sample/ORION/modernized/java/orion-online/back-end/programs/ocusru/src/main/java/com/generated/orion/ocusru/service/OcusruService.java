package com.generated.orion.ocusru.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.common.linkage.OcusruLinkParm;
import com.generated.orion.ocusru.accessor.OcusruFields;
import com.generated.orion.ocusru.metadata.OcusruBmsMetadata;
import com.generated.orion.ocusru.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OCUSRU. */
@Service
public class OcusruService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OcusruService.class);

    private static final int COMMAREA_LENGTH = 692;

    /**
     * Entry point: executes the CICS program main line. Called by AppRunner after transaction
     * setup.
     */
    @Override
    public void mainLine(AppService appService) {
        TaskContext ctx = new TaskContext(appService);
        dispatchMainLine(ctx);
    }

    @Override
    public String getProgramName() {
        return "OCUSRU";
    }

    @Override
    public String getTransId() {
        return "ORUP";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OcusruBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OcusruBmsMetadata.registerFsetFields(runner);
    }

    @Override
    public FieldMapping getFieldMapping(String mapName) {
        return OcusruBmsMetadata.getFieldMapping(mapName);
    }

    /** COBOL paragraph: 0000-MAIN */
    private void dispatchMainLine(TaskContext ctx) {
        ctx.f.setString("WS-ERR-FLG", "N");
        if (ctx.appService.getEibcalen() == 0) {
            ctx.f.setOrionCommarea("");
            sendInitialScreen(ctx);
        } else {
            ctx.f.setOrionCommarea(Utility.groupToString(ctx.appService.getSerializedCommarea()));
            ctx.f.copyBytes("WS-STATE-AREA", "CA-WORK-AREA");
            ctx.f.setCaErrMsg(" ");
            if (ctx.f.getCaPgmContext() == 0) {
                sendInitialScreen(ctx);
            } else {
                processUserInput(ctx);
            }
        }
        returnToCics(ctx);
    }

    /** COBOL paragraph: 1000-SEND-INITIAL */
    private void sendInitialScreen(TaskContext ctx) {
        ctx.f.fillLowValues("MUSRUAO");
        ctx.f.setString("WS-STAGE", "0");
        ctx.f.setWsSvUserid(" ");
        populateScreenHeader(ctx);
        ctx.f.setErrmsgo("Enter a user id and press ENTER.");
        ctx.appService.sendMap(
                "MUSRUA", ctx.f, ctx.f, /* erase */ true, /* freekb */ false, /* cursor */ false);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.f.setCaPgmContext(1);
    }

    /** COBOL paragraph: 2000-PROCESS-INPUT */
    private void processUserInput(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.appService.getEibaid(), "3")) {
            transferToAdminMenu(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "4")) {
            sendInitialScreen(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "'")) {
            handleEnterKey(ctx);
        } else {
            sendInitialScreen(ctx);
            ctx.f.setErrmsgo(ctx.f.getWsMsgInvalidKey());
            sendDataOnlyScreen(ctx);
        }
    }

    /** COBOL paragraph: 2100-HANDLE-ENTER */
    private void handleEnterKey(TaskContext ctx) {
        receiveUserMap(ctx);
        if (ctx.f.getWsStage().equals("0")) {
            fetchUserRecord(ctx);
        } else if (ctx.f.getWsStage().equals("1")) {
            applyUserUpdate(ctx);
        } else {
            sendInitialScreen(ctx);
        }
    }

    /** COBOL paragraph: 2110-RECEIVE-MAP */
    private void receiveUserMap(TaskContext ctx) {
        ctx.appService.receiveMap("MUSRUA", ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 2200-FETCH-USER */
    private void fetchUserRecord(TaskContext ctx) {
        if (isFieldBlank(ctx, ctx.f.getUseridi(), "USERIDI")) {
            ctx.f.setErrmsgo(ctx.f.getWsMsgRequired());
            sendDataOnlyScreen(ctx);
        } else {
            ctx.f.setUsId(ctx.f.getUseridi());
            readUserRecord(ctx);
            if (ctx.f.getWsFoundFlg().equals("Y")) {
                showUserDetails(ctx);
            } else {
                ctx.f.setErrmsgo("User not found.");
                sendDataOnlyScreen(ctx);
            }
        }
    }

    /** COBOL paragraph: 2210-SHOW-USER */
    private void showUserDetails(TaskContext ctx) {
        ctx.f.setWsSvUserid(ctx.f.getUsId());
        ctx.f.setUserido(ctx.f.getUsId());
        ctx.f.setUsfnamo(ctx.f.getUsFirstName());
        ctx.f.setUslnamo(ctx.f.getUsLastName());
        ctx.f.setUspwdo(ctx.f.getUsPassword());
        ctx.f.setUstypeo(ctx.f.getUsType());
        ctx.f.setString("WS-STAGE", "1");
        ctx.f.setErrmsgo("Modify fields and press ENTER to update.");
        sendDataOnlyScreen(ctx);
    }

    /** COBOL paragraph: 2300-APPLY-UPDATE */
    private void applyUserUpdate(TaskContext ctx) {
        validateUserInput(ctx);
        if (ctx.f.getWsEditFlg().equals("N")) {
            repopulateScreenFields(ctx);
            sendDataOnlyScreen(ctx);
        } else {
            updateUserRecord(ctx);
            if (ctx.f.getWsErrFlg().equals("Y")) {
                repopulateScreenFields(ctx);
                sendDataOnlyScreen(ctx);
            } else {
                confirmUserUpdate(ctx);
            }
        }
    }

    /** COBOL paragraph: 2310-VALIDATE-INPUT */
    private void validateUserInput(TaskContext ctx) {
        ctx.f.setString("WS-EDIT-FLG", "Y");
        if (isFieldBlank(ctx, ctx.f.getUsfnami(), "USFNAMI")) {
            failValidation(ctx, "First name is required.");
            return;
        }
        if (isFieldBlank(ctx, ctx.f.getUslnami(), "USLNAMI")) {
            failValidation(ctx, "Last name is required.");
            return;
        }
        if (isFieldBlank(ctx, ctx.f.getUspwdi(), "USPWDI")) {
            failValidation(ctx, "Password is required.");
            return;
        }
        ctx.f.setWsTypeIn(ctx.f.getUstypei());
        if (!Utility.fieldEquals(ctx.f.getWsTypeIn(), "A")
                && !Utility.fieldEquals(ctx.f.getWsTypeIn(), "U")) {
            failValidation(ctx, "Type must be A (admin) or U (user).");
        }
    }

    /** COBOL paragraph: 3000-READ-USER */
    private void readUserRecord(TaskContext ctx) {
        ctx.f.setString("WS-FOUND-FLG", "N");
        ctx.appService.readFile(ctx.f.getWsUsrsec(), ctx.f, String.valueOf(ctx.f.getUsId()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setString("WS-FOUND-FLG", "Y");
        } else if (ctx.f.getWsRespCd() == 13) {
            ctx.f.setString("WS-FOUND-FLG", "N");
        } else {
            ctx.f.setString("WS-ERR-FLG", "Y");
            ctx.f.setErrmsgo("Error reading user file.");
        }
    }

    /** COBOL paragraph: 3100-UPDATE-USER */
    private void updateUserRecord(TaskContext ctx) {
        ctx.f.setString("WS-ERR-FLG", "N");
        ctx.f.setUsId(ctx.f.getWsSvUserid());
        ctx.appService.readFileForUpdate(
                ctx.f.getWsUsrsec(), ctx.f, String.valueOf(ctx.f.getUsId()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            rewriteUserRecord(ctx);
        } else if (ctx.f.getWsRespCd() == 13) {
            ctx.f.setString("WS-ERR-FLG", "Y");
            ctx.f.setErrmsgo("User no longer exists.");
        } else {
            ctx.f.setString("WS-ERR-FLG", "Y");
            ctx.f.setErrmsgo("Error reading user for update.");
        }
    }

    /** COBOL paragraph: 3110-REWRITE-USER */
    private void rewriteUserRecord(TaskContext ctx) {
        ctx.f.setUsFirstName(ctx.f.getUsfnami());
        ctx.f.setUsLastName(ctx.f.getUslnami());
        ctx.f.setUsPassword(ctx.f.getUspwdi());
        ctx.f.setUsType(ctx.f.getWsTypeIn());
        ctx.appService.rewriteFile(ctx.f.getWsUsrsec(), ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() != 0) {
            ctx.f.setString("WS-ERR-FLG", "Y");
            ctx.f.setErrmsgo("Error updating user file.");
        }
    }

    /** COBOL paragraph: 4000-REPOPULATE */
    private void repopulateScreenFields(TaskContext ctx) {
        ctx.f.setUserido(ctx.f.getWsSvUserid());
        ctx.f.setUsfnamo(ctx.f.getUsfnami());
        ctx.f.setUslnamo(ctx.f.getUslnami());
        ctx.f.setUspwdo(ctx.f.getUspwdi());
        ctx.f.setUstypeo(ctx.f.getUstypei());
    }

    /** COBOL paragraph: 4100-CONFIRM-UPDATE */
    private void confirmUserUpdate(TaskContext ctx) {
        ctx.f.fillLowValues("MUSRUAO");
        ctx.f.setString("WS-STAGE", "0");
        ctx.f.setWsSvUserid(" ");
        ctx.f.setErrmsgo("User updated successfully.");
        sendDataOnlyScreen(ctx);
    }

    /** COBOL paragraph: 7000-XCTL-ADMEN */
    private void transferToAdminMenu(TaskContext ctx) {
        ctx.f.setCaFromProgram(ctx.f.getWsPgmname());
        ctx.f.setCaFromTranid(ctx.f.getWsTranid());
        ctx.f.setCaPgmContext(0);
        ctx.f.copyBytes("CA-WORK-AREA", "WS-STATE-AREA");
        ctx.appService.xctl(
                ctx.f.getWsAdmenPgm(), ctx.f.sliceBytes("ORION-COMMAREA"), COMMAREA_LENGTH);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 8000-POPULATE-HEADER */
    private void populateScreenHeader(TaskContext ctx) {
        ctx.f.setTrnnameo(ctx.f.getWsTranid());
        ctx.f.setPgmnameo(ctx.f.getWsPgmname());
        ctx.f.setTitleo(ctx.f.getWsHdrTitle());
        captureCurrentDateTime(ctx);
        ctx.f.setCurdateo(ctx.f.getWsHdrDate());
        ctx.f.setCurtimeo(ctx.f.getWsHdrTime());
    }

    /** COBOL paragraph: 8100-SEND-DATAONLY */
    private void sendDataOnlyScreen(TaskContext ctx) {
        populateScreenHeader(ctx);
        ctx.appService.sendMap(
                "MUSRUA", ctx.f, ctx.f, /* erase */ false, /* freekb */ false, /* cursor */ false);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 8500-GET-DATE-TIME */
    private void captureCurrentDateTime(TaskContext ctx) {
        ctx.appService.askTime();
        com.appruntime.FormatTimeResult _ftResult =
                ctx.appService.formatTime(null, "YYYYMMDD", "-", ":");
        ctx.f.setWsHdrDate(_ftResult.getDate());
        ctx.f.setWsHdrTime(_ftResult.getTime());
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 9000-RETURN */
    private void returnToCics(TaskContext ctx) {
        ctx.f.copyBytes("CA-WORK-AREA", "WS-STATE-AREA");
        ctx.appService.returnTransid(
                ctx.f.getWsTranid(), ctx.f.sliceBytes("ORION-COMMAREA"), COMMAREA_LENGTH);
    }

    /** Check whether a screen input field was left blank (spaces or low-values). */
    private boolean isFieldBlank(TaskContext ctx, String fieldValue, String fieldName) {
        return Utility.fieldEquals(fieldValue, " ") || ctx.f.isAllLowValues(fieldName);
    }

    /** Mark validation as failed and set the error message shown to the user. */
    private void failValidation(TaskContext ctx, String message) {
        ctx.f.setString("WS-EDIT-FLG", "N");
        ctx.f.setErrmsgo(message);
    }

    /**
     * Per-request execution context — analogous to CICS Task Control Block. Each HTTP request
     * creates a fresh TaskContext. Thread-safe by design.
     */
    static class TaskContext {
        final WorkingStorage ws;

        final OcusruFields f;

        final AppService appService;

        final OcusruLinkParm link = new OcusruLinkParm();

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OcusruFields(ws);
        }
    }
}
