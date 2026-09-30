package com.generated.orion.occardu.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.RuntimeConstants;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.common.linkage.OccarduLinkParm;
import com.generated.orion.occardu.accessor.OccarduFields;
import com.generated.orion.occardu.metadata.OccarduBmsMetadata;
import com.generated.orion.occardu.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OCCARDU. */
@Service
public class OccarduService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OccarduService.class);

    /** Byte length of the ORION-COMMAREA passed across XCTL/RETURN TRANSID. */
    private static final int COMMAREA_LENGTH = 692;

    /**
     * Entry point: executes the CICS program main line. Called by AppRunner after transaction
     * setup.
     */
    @Override
    public void mainLine(AppService appService) {
        TaskContext ctx = new TaskContext(appService);
        processMainLine(ctx);
    }

    @Override
    public String getProgramName() {
        return "OCCARDU";
    }

    @Override
    public String getTransId() {
        return "ORCU";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OccarduBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OccarduBmsMetadata.registerFsetFields(runner);
    }

    @Override
    public FieldMapping getFieldMapping(String mapName) {
        return OccarduBmsMetadata.getFieldMapping(mapName);
    }

    /** COBOL paragraph: 0000-MAIN */
    private void processMainLine(TaskContext ctx) {
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
                processInputByAid(ctx);
            }
        }
        returnToCics(ctx);
    }

    /** COBOL paragraph: 1000-SEND-INITIAL */
    private void sendInitialScreen(TaskContext ctx) {
        ctx.f.fillLowValues("MCARDUAO");
        populateScreenHeader(ctx);
        ctx.f.setCaWorkArea(
                Utility.setSubstring(
                        String.valueOf(ctx.f.getCaWorkArea()),
                        1,
                        1,
                        String.valueOf(ctx.f.getWsStKey())));
        if (Utility.fieldEquals(ctx.f.getCaCardNum(), " ") || ctx.f.isAllLowValues("CA-CARD-NUM")) {
            ctx.f.setErrmsgo("Enter card number and press ENTER.");
        } else {
            ctx.f.setCardnumo(ctx.f.getCaCardNum());
            ctx.f.setErrmsgo("Press ENTER to load the selected card.");
        }
        ctx.appService.sendMap(
                "MCARDUA", ctx.f, ctx.f, /* erase */ true, /* freekb */ false, /* cursor */ false);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.f.setCaPgmContext(1);
    }

    /** COBOL paragraph: 2000-PROCESS-INPUT */
    private void processInputByAid(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.appService.getEibaid(), "3")) {
            xctlToMenu(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "@")) {
            xctlToMenu(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "4")) {
            sendInitialScreen(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "_")) {
            sendInitialScreen(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "5")) {
            saveCardChanges(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "'")) {
            handleEnterKey(ctx);
        } else {
            ctx.f.setErrmsgo(ctx.f.getWsMsgInvalidKey());
            sendDataOnlyScreen(ctx);
        }
    }

    /** COBOL paragraph: 2050-RECEIVE */
    private void receiveScreenInput(TaskContext ctx) {
        ctx.appService.receiveMap("MCARDUA", ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 36) {
            ctx.f.fillLowValues("MCARDUAI");
        }
    }

    /** COBOL paragraph: 2100-ENTER */
    private void handleEnterKey(TaskContext ctx) {
        receiveScreenInput(ctx);
        switch (Utility.rtrim(
                Utility.padRight(String.valueOf(ctx.f.getCaWorkArea()), 1).substring(0, 1))) {
            case "E" -> {
                validateOnlyChanges(ctx);
            }
            default -> {
                lookupCard(ctx);
            }
        }
    }

    /** COBOL paragraph: 2200-LOOKUP */
    private void lookupCard(TaskContext ctx) {
        validateCardNumber(ctx);
        if (isValidationFailed(ctx)) {
            sendDataOnlyScreen(ctx);
        } else {
            ctx.f.setCdNum(ctx.f.getWsCnIn());
            ctx.f.setCaCardNum(ctx.f.getCdNum());
            readCardRecord(ctx);
            if (ctx.f.getWsFoundFlg().equals("Y")) {
                populateCardDetail(ctx);
                ctx.f.setCaWorkArea(
                        Utility.setSubstring(
                                String.valueOf(ctx.f.getCaWorkArea()),
                                1,
                                1,
                                String.valueOf(ctx.f.getWsStEdit())));
                ctx.f.setErrmsgo("Card found. Change fields, PF5 to save.");
                sendDataOnlyScreen(ctx);
            } else {
                ctx.f.setErrmsgo(ctx.f.getWsMsgNotfnd());
                sendDataOnlyScreen(ctx);
            }
        }
    }

    /** COBOL paragraph: 2250-VALIDATE-ONLY */
    private void validateOnlyChanges(TaskContext ctx) {
        validateAllFields(ctx);
        if (isValidationFailed(ctx)) {
            sendDataOnlyScreen(ctx);
        } else {
            ctx.f.setErrmsgo("Changes are valid.  Press PF5 to confirm save.");
            sendDataOnlyScreen(ctx);
        }
    }

    /** COBOL paragraph: 2300-SAVE */
    private void saveCardChanges(TaskContext ctx) {
        if (!Utility.fieldEquals(
                Utility.padRight(String.valueOf(ctx.f.getCaWorkArea()), 1).substring(0, 1),
                ctx.f.getWsStEdit())) {
            ctx.f.setErrmsgo("Enter a card number and press ENTER first.");
            sendDataOnlyScreen(ctx);
        } else {
            receiveScreenInput(ctx);
            validateAllFields(ctx);
            if (isValidationFailed(ctx)) {
                sendDataOnlyScreen(ctx);
            } else {
                updateCardRecord(ctx);
                if (ctx.f.getWsUpdSw().equals("Y")) {
                    populateCardDetail(ctx);
                }
                sendDataOnlyScreen(ctx);
            }
        }
    }

    /** COBOL paragraph: 3000-READ-CARD */
    private void readCardRecord(TaskContext ctx) {
        ctx.f.setString("WS-FOUND-FLG", "N");
        ctx.appService.readFile(ctx.f.getWsCardfile(), ctx.f, String.valueOf(ctx.f.getCdNum()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setString("WS-FOUND-FLG", "Y");
        } else if (ctx.f.getWsRespCd() == 13) {
            ctx.f.setString("WS-FOUND-FLG", "N");
        } else {
            handleAbend(ctx);
        }
    }

    /** COBOL paragraph: 3500-UPDATE-CARD */
    private void updateCardRecord(TaskContext ctx) {
        ctx.f.setString("WS-UPD-SW", "N");
        ctx.f.setCdNum(ctx.f.getCaCardNum());
        ctx.appService.readFileForUpdate(
                ctx.f.getWsCardfile(), ctx.f, String.valueOf(ctx.f.getCdNum()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            applyCardUpdate(ctx);
        } else if (ctx.f.getWsRespCd() == 13) {
            ctx.f.setErrmsgo("Card no longer exists. Update aborted.");
        } else {
            handleAbend(ctx);
        }
    }

    /** COBOL paragraph: 3600-APPLY-UPDATE */
    private void applyCardUpdate(TaskContext ctx) {
        ctx.f.setCdEmbossedName(ctx.f.getWsNewName());
        ctx.f.setCdExpiryDate(ctx.f.getWsNewExpiry());
        ctx.f.setCdActiveStatus(ctx.f.getWsNewStatus());
        ctx.appService.rewriteFile(ctx.f.getWsCardfile(), ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setString("WS-UPD-SW", "Y");
            ctx.f.setErrmsgo("Card updated successfully.");
        } else {
            ctx.f.setErrmsgo("Update failed during REWRITE.");
        }
    }

    /** COBOL paragraph: 4000-POPULATE-DETAIL */
    private void populateCardDetail(TaskContext ctx) {
        ctx.f.setCardnumo(ctx.f.getCdNum());
        ctx.f.setCdnameo(ctx.f.getCdEmbossedName());
        ctx.f.setCdexpo(ctx.f.getCdExpiryDate());
        ctx.f.setCdstato(ctx.f.getCdActiveStatus());
    }

    /** COBOL paragraph: 5000-VALIDATE-ALL */
    private void validateAllFields(TaskContext ctx) {
        ctx.f.setString("WS-VALID-SW", "Y");
        ctx.f.setErrmsgo(" ");
        if (ctx.f.getWsValidSw().equals("Y")) {
            validateEmbossedName(ctx);
        }
        if (ctx.f.getWsValidSw().equals("Y")) {
            validateExpiryDate(ctx);
        }
        if (ctx.f.getWsValidSw().equals("Y")) {
            validateActiveStatus(ctx);
        }
    }

    /** COBOL paragraph: 5010-VAL-NAME */
    private void validateEmbossedName(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.f.getCdnamei(), " ") || ctx.f.isAllLowValues("CDNAMEI")) {
            ctx.f.setString("WS-VALID-SW", "N");
            ctx.f.setErrmsgo("Embossed name is required.");
        } else {
            ctx.f.setWsNewName(ctx.f.getCdnamei());
        }
    }

    /** COBOL paragraph: 5020-VAL-EXPIRY */
    private void validateExpiryDate(TaskContext ctx) {
        ctx.f.setKdFunc("VALD");
        ctx.f.setKdDateIn(ctx.f.getCdexpi());
        ctx.f.setKdDateOut(" ");
        ctx.f.setKdStatus("00");
        Object[] _ca0 = new Object[] {ctx.f.getKdateParm()};
        ctx.appService.callProgram("OUDATE", _ca0);
        ctx.f.setKdateParm(String.valueOf(_ca0[0]));
        if (Utility.fieldEquals(ctx.f.getKdStatus(), "00")) {
            ctx.f.setWsNewExpiry(ctx.f.getCdexpi());
        } else {
            ctx.f.setString("WS-VALID-SW", "N");
            ctx.f.setErrmsgo("Expiry date invalid, use YYYY-MM-DD.");
        }
    }

    /** COBOL paragraph: 5030-VAL-STATUS */
    private void validateActiveStatus(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.f.getCdstati(), "Y")
                || Utility.fieldEquals(ctx.f.getCdstati(), "N")) {
            ctx.f.setWsNewStatus(ctx.f.getCdstati());
        } else {
            ctx.f.setString("WS-VALID-SW", "N");
            ctx.f.setErrmsgo("Active status must be Y or N.");
        }
    }

    /** COBOL paragraph: 6000-VALIDATE-CARDNUM */
    private void validateCardNumber(TaskContext ctx) {
        ctx.f.setString("WS-VALID-SW", "Y");
        ctx.f.setWsCnIn(ctx.f.getCardnumi());
        ctx.f.setWsCnIn(
                String.valueOf(ctx.f.getWsCnIn())
                        .replace(String.valueOf(RuntimeConstants.LOW_VALUES), String.valueOf(" ")));
        if (Utility.fieldEquals(ctx.f.getWsCnIn(), " ")) {
            ctx.f.setString("WS-VALID-SW", "N");
            ctx.f.setErrmsgo(ctx.f.getWsMsgRequired());
            return;
        }
        ctx.f.setWsCnDigits(0);
        for (ctx.f.setWsCnPos(1);
                ctx.f.getWsCnPos() <= 16;
                ctx.f.setWsCnPos(ctx.f.getWsCnPos() + 1)) {
            ctx.f.setWsCnChar(
                    Utility.padRight(String.valueOf(ctx.f.getWsCnIn()), 256)
                            .substring(ctx.f.getWsCnPos() - 1, ctx.f.getWsCnPos() - 1 + 1));
            if ((ctx.f.getWsCnChar().compareTo("0") >= 0)
                    && (ctx.f.getWsCnChar().compareTo("9") <= 0)) {
                ctx.f.setWsCnDigits(ctx.f.getWsCnDigits() + 1);
            } else {
                ctx.f.setString("WS-VALID-SW", "N");
            }
        }
        if (isValidationFailed(ctx) || ctx.f.getWsCnDigits() != 16) {
            ctx.f.setString("WS-VALID-SW", "N");
            ctx.f.setErrmsgo("Card number must be exactly 16 digits.");
        }
    }

    /** COBOL paragraph: 7000-XCTL-MENU */
    private void xctlToMenu(TaskContext ctx) {
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
                "MCARDUA", ctx.f, ctx.f, /* erase */ false, /* freekb */ false, /* cursor */ false);
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

    /** COBOL paragraph: 9500-ABEND-RTN */
    private void handleAbend(TaskContext ctx) {
        ctx.f.setWsMsgText("OCCARDU: unrecoverable file error. Contact support.");
        ctx.appService.sendText(String.valueOf(ctx.f), true, true);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.appService.returnProgram();
    }

    /** Whether the last validation pass (5000-VALIDATE-ALL / 6000-VALIDATE-CARDNUM) failed. */
    private boolean isValidationFailed(TaskContext ctx) {
        return ctx.f.getWsValidSw().equals("N");
    }

    /**
     * Per-request execution context — analogous to CICS Task Control Block. Each HTTP request
     * creates a fresh TaskContext. Thread-safe by design.
     */
    static class TaskContext {
        final WorkingStorage ws;

        final OccarduFields f;

        final AppService appService;

        final OccarduLinkParm link = new OccarduLinkParm();

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OccarduFields(ws);
        }
    }
}
