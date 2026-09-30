package com.generated.orion.ouabnd.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.ScreenResponse;
import com.generated.orion.ouabnd.accessor.OuabndFields;
import com.generated.orion.ouabnd.metadata.OuabndBmsMetadata;
import com.generated.orion.ouabnd.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OUABND. */
@Service
public class OuabndService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OuabndService.class);

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
                    ctx.f.writeBytes("KABND-PARM", (byte[]) _params[0]);
                } else if (_params.length > 0 && _params[0] != null) {
                    ctx.f.setGroup("KABND-PARM", String.valueOf(_params[0]));
                }
            } else if (_commarea instanceof byte[]) {
                ctx.f.writeBytes("KABND-PARM", (byte[]) _commarea);
            } else {
                ctx.f.setGroup("KABND-PARM", String.valueOf(_commarea));
            }
        }
        try {
            abendProgram(ctx);
        } finally {
            if (ctx.appService.getEibcalen() > 0) {
                if (ctx.appService.getCommarea() instanceof Object[]) {
                    Object[] _params = (Object[]) ctx.appService.getCommarea();
                    if (_params.length > 0) {
                        _params[0] =
                                _params[0] instanceof byte[]
                                        ? (Object) ctx.f.sliceBytes("KABND-PARM")
                                        : (Object) ctx.f.groupToString("KABND-PARM");
                    }
                } else {
                    ctx.appService.setCommarea(ctx.f.sliceBytes("KABND-PARM"));
                }
            }
        }
    }

    @Override
    public String getProgramName() {
        return "OUABND";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OuabndBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OuabndBmsMetadata.registerFsetFields(runner);
    }

    /** COBOL paragraph: 0000-MAIN */
    private void abendProgram(TaskContext ctx) {
        {
            StringBuilder sb = new StringBuilder();
            sb.append(String.valueOf("ABEND IN "));
            sb.append(String.valueOf(ctx.f.getKaProgram()));
            sb.append(String.valueOf(" AT "));
            sb.append(String.valueOf(ctx.f.getKaParagraph()));
            sb.append(String.valueOf(" - "));
            sb.append(String.valueOf(ctx.f.getKaDetail()));
            ctx.f.setWsAbendMsg(sb.toString());
        }
        ctx.appService.writeQueueTd("CSSL", ctx.f, 120);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.appService.abend("WS-ABCODE");
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        return;
    }

    /**
     * Per-request execution context — analogous to CICS Task Control Block. Each HTTP request
     * creates a fresh TaskContext. Thread-safe by design.
     */
    static class TaskContext {
        final WorkingStorage ws;

        final OuabndFields f;

        final AppService appService;

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OuabndFields(ws);
        }
    }
}
