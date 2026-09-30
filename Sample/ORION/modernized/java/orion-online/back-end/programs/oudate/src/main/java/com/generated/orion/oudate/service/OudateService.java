package com.generated.orion.oudate.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.oudate.accessor.OudateFields;
import com.generated.orion.oudate.metadata.OudateBmsMetadata;
import com.generated.orion.oudate.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OUDATE. */
@Service
public class OudateService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OudateService.class);

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
                    ctx.f.writeBytes("KDATE-PARM", (byte[]) _params[0]);
                } else if (_params.length > 0 && _params[0] != null) {
                    ctx.f.setGroup("KDATE-PARM", String.valueOf(_params[0]));
                }
            } else if (_commarea instanceof byte[]) {
                ctx.f.writeBytes("KDATE-PARM", (byte[]) _commarea);
            } else {
                ctx.f.setGroup("KDATE-PARM", String.valueOf(_commarea));
            }
        }
        try {
            dispatchByFunctionCode(ctx);
        } finally {
            if (ctx.appService.getEibcalen() > 0) {
                if (ctx.appService.getCommarea() instanceof Object[]) {
                    Object[] _params = (Object[]) ctx.appService.getCommarea();
                    if (_params.length > 0) {
                        _params[0] =
                                _params[0] instanceof byte[]
                                        ? (Object) ctx.f.sliceBytes("KDATE-PARM")
                                        : (Object) ctx.f.groupToString("KDATE-PARM");
                    }
                } else {
                    ctx.appService.setCommarea(ctx.f.sliceBytes("KDATE-PARM"));
                }
            }
        }
    }

    @Override
    public String getProgramName() {
        return "OUDATE";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OudateBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OudateBmsMetadata.registerFsetFields(runner);
    }

    /** COBOL paragraph: 0000-MAIN */
    private void dispatchByFunctionCode(TaskContext ctx) {
        ctx.f.setKdStatus("00");
        switch (Utility.rtrim(ctx.f.getKdFunc())) {
            case "TODY" -> {
                populateTodayDate(ctx);
            }
            case "VALD" -> {
                validateDateInput(ctx);
            }
            case "FMT" -> {
                ctx.f.setKdDateOut(ctx.f.getKdDateIn());
            }
            default -> {
                ctx.f.setKdStatus("99");
            }
        }
        return;
    }

    /** COBOL paragraph: 1000-TODAY */
    private void populateTodayDate(TaskContext ctx) {
        ctx.f.setWsCurr(
                (java.time.LocalDateTime.now()
                                .format(
                                        java.time.format.DateTimeFormatter.ofPattern(
                                                "yyyyMMddHHmmss"))
                        + "00+0000"));
        ctx.f.setWsEdY(
                Utility.parseNumeric(extractDateSegment(ctx.f.getWsCurr(), 4, 0, 4)).intValue());
        ctx.f.setWsEdM(
                Utility.parseNumeric(extractDateSegment(ctx.f.getWsCurr(), 6, 4, 6)).intValue());
        ctx.f.setWsEdD(
                Utility.parseNumeric(extractDateSegment(ctx.f.getWsCurr(), 8, 6, 8)).intValue());
        ctx.f.copyBytes("KD-DATE-OUT", "WS-EDIT-DATE");
    }

    /** COBOL paragraph: 2000-VALIDATE */
    private void validateDateInput(TaskContext ctx) {
        ctx.f.setKdDateOut(ctx.f.getKdDateIn());
        if (!Utility.isNumeric(extractDateSegment(ctx.f.getKdDateIn(), 4, 0, 4))
                || !Utility.isNumeric(extractDateSegment(ctx.f.getKdDateIn(), 7, 5, 7))
                || !Utility.isNumeric(extractDateSegment(ctx.f.getKdDateIn(), 10, 8, 10))) {
            ctx.f.setKdStatus("99");
            exitValidation(ctx);
            return;
        }
        ctx.f.setWsY(
                Utility.parseNumeric(extractDateSegment(ctx.f.getKdDateIn(), 4, 0, 4)).intValue());
        ctx.f.setWsM(
                Utility.parseNumeric(extractDateSegment(ctx.f.getKdDateIn(), 7, 5, 7)).intValue());
        ctx.f.setWsD(
                Utility.parseNumeric(extractDateSegment(ctx.f.getKdDateIn(), 10, 8, 10))
                        .intValue());
        if (ctx.f.getWsM() < 1 || ctx.f.getWsM() > 12) {
            ctx.f.setKdStatus("99");
            exitValidation(ctx);
            return;
        }
        switch (ctx.f.getWsM()) {
            case 2 -> {
                ctx.f.setWsDim(29);
            }
            case 4 -> {
                ctx.f.setWsDim(30);
            }
            case 6 -> {
                ctx.f.setWsDim(30);
            }
            case 9 -> {
                ctx.f.setWsDim(30);
            }
            case 11 -> {
                ctx.f.setWsDim(30);
            }
            default -> {
                ctx.f.setWsDim(31);
            }
        }
        if (ctx.f.getWsD() < 1 || ctx.f.getWsD() > ctx.f.getWsDim()) {
            ctx.f.setKdStatus("99");
        }
    }

    /** COBOL paragraph: 2000-EXIT */
    private void exitValidation(TaskContext ctx) {
        return;
    }

    /** Slice a fixed-width date segment out of a right-padded source string. */
    private String extractDateSegment(String source, int padLength, int start, int end) {
        return Utility.padRight(String.valueOf(source), padLength).substring(start, end);
    }

    /**
     * Per-request execution context — analogous to CICS Task Control Block. Each HTTP request
     * creates a fresh TaskContext. Thread-safe by design.
     */
    static class TaskContext {
        final WorkingStorage ws;

        final OudateFields f;

        final AppService appService;

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OudateFields(ws);
        }
    }
}
