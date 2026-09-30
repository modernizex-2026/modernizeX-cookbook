package com.sakura.menu00.service;

import com.sakura.ap0010.service.Ap0010Service;
import com.sakura.ap0020.service.Ap0020Service;
import com.sakura.ar0010.service.Ar0010Service;
import com.sakura.ar0020.service.Ar0020Service;
import com.sakura.bt0010.service.Bt0010Service;
import com.sakura.bt0020.service.Bt0020Service;
import com.sakura.bt0030.service.Bt0030Service;
import com.sakura.bt0040.service.Bt0040Service;
import com.sakura.bt0050.service.Bt0050Service;
import com.sakura.bt0060.service.Bt0060Service;
import com.sakura.bt0070.service.Bt0070Service;
import com.sakura.bt0080.service.Bt0080Service;
import com.sakura.bt0090.service.Bt0090Service;
import com.sakura.chklog.service.ChklogService;
import com.sakura.dateut.service.DateutService;
import com.sakura.iv0010.service.Iv0010Service;
import com.sakura.iv0020.service.Iv0020Service;
import com.sakura.iv0030.service.Iv0030Service;
import com.sakura.iv0040.service.Iv0040Service;
import com.sakura.iv0050.service.Iv0050Service;
import com.sakura.menu00.domain.Menu00FieldAccess;
import com.sakura.menu00.domain.WorkingStorage;
import com.sakura.menu00.screen.ScreenDefs;
import com.sakura.ms0010.service.Ms0010Service;
import com.sakura.ms0020.service.Ms0020Service;
import com.sakura.ms0030.service.Ms0030Service;
import com.sakura.ms0040.service.Ms0040Service;
import com.sakura.ms0050.service.Ms0050Service;
import com.sakura.ms0060.service.Ms0060Service;
import com.sakura.ms0070.service.Ms0070Service;
import com.sakura.ms0080.service.Ms0080Service;
import com.sakura.ms0090.service.Ms0090Service;
import com.sakura.ms0100.service.Ms0100Service;
import com.sakura.ms0110.service.Ms0110Service;
import com.sakura.ms0120.service.Ms0120Service;
import com.sakura.oe0010.service.Oe0010Service;
import com.sakura.oe0020.service.Oe0020Service;
import com.sakura.oe0030.service.Oe0030Service;
import com.sakura.oe0040.service.Oe0040Service;
import com.sakura.oe0050.service.Oe0050Service;
import com.sakura.pu0010.service.Pu0010Service;
import com.sakura.pu0020.service.Pu0020Service;
import com.sakura.pu0030.service.Pu0030Service;
import com.sakura.pu0040.service.Pu0040Service;
import com.sakura.rc0010.service.Rc0010Service;
import com.sakura.rp0010.service.Rp0010Service;
import com.sakura.rp0020.service.Rp0020Service;
import com.sakura.rp0030.service.Rp0030Service;
import com.sakura.rp0040.service.Rp0040Service;
import com.sakura.rp0050.service.Rp0050Service;
import com.sakura.rp0060.service.Rp0060Service;
import com.sakura.rp0070.service.Rp0070Service;
import com.sakura.rp0080.service.Rp0080Service;
import com.sakura.rp0090.service.Rp0090Service;
import com.sakura.rp0100.service.Rp0100Service;
import com.sakura.rp0110.service.Rp0110Service;
import com.sakura.rp0120.service.Rp0120Service;
import com.sakura.rp0130.service.Rp0130Service;
import com.sakura.rp0140.service.Rp0140Service;
import com.sakura.runtime.BatchServiceBase;
import com.sakura.runtime.DatasetEnums.*;
import com.sakura.runtime.ScreenModels.*;
import com.sakura.runtime.ScreenRendererAware;
import com.sakura.runtime.ScreenRendererInstance;
import com.sakura.runtime.StopRunSignal;
import com.sakura.runtime.Utility;
import com.sakura.runtime.io.AbstractDatasets;
import com.sakura.runtime.linkage.ChklogLinkParm;
import com.sakura.runtime.linkage.DateutLinkParm;
import com.sakura.sh0010.service.Sh0010Service;
import com.sakura.sl0010.service.Sl0010Service;
import com.sakura.sl0020.service.Sl0020Service;
import com.sakura.sl0030.service.Sl0030Service;
import com.sakura.sl0040.service.Sl0040Service;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/** Business logic service generated from COBOL program MENU00. */
@Service
@Scope("prototype")
public class Menu00Service extends BatchServiceBase {
    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL CHKLOG. */
    private ChklogService chklogService;

    /** Injected service for COBOL CALL MS0010. */
    private Ms0010Service ms0010Service;

    /** Injected service for COBOL CALL MS0020. */
    private Ms0020Service ms0020Service;

    /** Injected service for COBOL CALL MS0030. */
    private Ms0030Service ms0030Service;

    /** Injected service for COBOL CALL MS0040. */
    private Ms0040Service ms0040Service;

    /** Injected service for COBOL CALL MS0050. */
    private Ms0050Service ms0050Service;

    /** Injected service for COBOL CALL MS0060. */
    private Ms0060Service ms0060Service;

    /** Injected service for COBOL CALL MS0070. */
    private Ms0070Service ms0070Service;

    /** Injected service for COBOL CALL MS0080. */
    private Ms0080Service ms0080Service;

    /** Injected service for COBOL CALL MS0090. */
    private Ms0090Service ms0090Service;

    /** Injected service for COBOL CALL MS0100. */
    private Ms0100Service ms0100Service;

    /** Injected service for COBOL CALL MS0110. */
    private Ms0110Service ms0110Service;

    /** Injected service for COBOL CALL MS0120. */
    private Ms0120Service ms0120Service;

    /** Injected service for COBOL CALL OE0010. */
    private Oe0010Service oe0010Service;

    /** Injected service for COBOL CALL OE0020. */
    private Oe0020Service oe0020Service;

    /** Injected service for COBOL CALL OE0030. */
    private Oe0030Service oe0030Service;

    /** Injected service for COBOL CALL OE0040. */
    private Oe0040Service oe0040Service;

    /** Injected service for COBOL CALL OE0050. */
    private Oe0050Service oe0050Service;

    /** Injected service for COBOL CALL SH0010. */
    private Sh0010Service sh0010Service;

    /** Injected service for COBOL CALL SL0010. */
    private Sl0010Service sl0010Service;

    /** Injected service for COBOL CALL SL0020. */
    private Sl0020Service sl0020Service;

    /** Injected service for COBOL CALL SL0030. */
    private Sl0030Service sl0030Service;

    /** Injected service for COBOL CALL SL0040. */
    private Sl0040Service sl0040Service;

    /** Injected service for COBOL CALL PU0010. */
    private Pu0010Service pu0010Service;

    /** Injected service for COBOL CALL PU0020. */
    private Pu0020Service pu0020Service;

    /** Injected service for COBOL CALL PU0030. */
    private Pu0030Service pu0030Service;

    /** Injected service for COBOL CALL PU0040. */
    private Pu0040Service pu0040Service;

    /** Injected service for COBOL CALL RC0010. */
    private Rc0010Service rc0010Service;

    /** Injected service for COBOL CALL IV0010. */
    private Iv0010Service iv0010Service;

    /** Injected service for COBOL CALL IV0020. */
    private Iv0020Service iv0020Service;

    /** Injected service for COBOL CALL IV0030. */
    private Iv0030Service iv0030Service;

    /** Injected service for COBOL CALL IV0040. */
    private Iv0040Service iv0040Service;

    /** Injected service for COBOL CALL IV0050. */
    private Iv0050Service iv0050Service;

    /** Injected service for COBOL CALL AR0010. */
    private Ar0010Service ar0010Service;

    /** Injected service for COBOL CALL AR0020. */
    private Ar0020Service ar0020Service;

    /** Injected service for COBOL CALL AP0010. */
    private Ap0010Service ap0010Service;

    /** Injected service for COBOL CALL AP0020. */
    private Ap0020Service ap0020Service;

    /** Injected service for COBOL CALL RP0010. */
    private Rp0010Service rp0010Service;

    /** Injected service for COBOL CALL RP0020. */
    private Rp0020Service rp0020Service;

    /** Injected service for COBOL CALL RP0030. */
    private Rp0030Service rp0030Service;

    /** Injected service for COBOL CALL RP0040. */
    private Rp0040Service rp0040Service;

    /** Injected service for COBOL CALL RP0050. */
    private Rp0050Service rp0050Service;

    /** Injected service for COBOL CALL RP0060. */
    private Rp0060Service rp0060Service;

    /** Injected service for COBOL CALL RP0070. */
    private Rp0070Service rp0070Service;

    /** Injected service for COBOL CALL RP0080. */
    private Rp0080Service rp0080Service;

    /** Injected service for COBOL CALL RP0090. */
    private Rp0090Service rp0090Service;

    /** Injected service for COBOL CALL RP0100. */
    private Rp0100Service rp0100Service;

    /** Injected service for COBOL CALL RP0110. */
    private Rp0110Service rp0110Service;

    /** Injected service for COBOL CALL RP0120. */
    private Rp0120Service rp0120Service;

    /** Injected service for COBOL CALL RP0130. */
    private Rp0130Service rp0130Service;

    /** Injected service for COBOL CALL RP0140. */
    private Rp0140Service rp0140Service;

    /** Injected service for COBOL CALL BT0010. */
    private Bt0010Service bt0010Service;

    /** Injected service for COBOL CALL BT0020. */
    private Bt0020Service bt0020Service;

    /** Injected service for COBOL CALL BT0030. */
    private Bt0030Service bt0030Service;

    /** Injected service for COBOL CALL BT0040. */
    private Bt0040Service bt0040Service;

    /** Injected service for COBOL CALL BT0050. */
    private Bt0050Service bt0050Service;

    /** Injected service for COBOL CALL BT0060. */
    private Bt0060Service bt0060Service;

    /** Injected service for COBOL CALL BT0070. */
    private Bt0070Service bt0070Service;

    /** Injected service for COBOL CALL BT0080. */
    private Bt0080Service bt0080Service;

    /** Injected service for COBOL CALL BT0090. */
    private Bt0090Service bt0090Service;

    private final Menu00FieldAccess ws = new Menu00FieldAccess(new WorkingStorage());

    /** Submenu choice 1..N -> WK-CALL program code, indexed by (choice - 1). */
    private static final String[] MASTER_PROGRAM_CODES = {
        "MS0010", "MS0020", "MS0030", "MS0040", "MS0050", "MS0060",
        "MS0070", "MS0080", "MS0090", "MS0100", "MS0110", "MS0120"
    };

    /** Submenu choice 1..N -> WK-CALL program code, indexed by (choice - 1). */
    private static final String[] ORDER_PROGRAM_CODES = {
        "OE0010", "OE0020", "OE0030", "SH0010", "SL0010",
        "SL0020", "SL0030", "OE0040", "SL0040", "OE0050"
    };

    /** Submenu choice 1..N -> WK-CALL program code, indexed by (choice - 1). */
    private static final String[] PURCHASE_PROGRAM_CODES = {
        "PU0010", "PU0020", "RC0010", "PU0030", "PU0040"
    };

    /** Submenu choice 1..N -> WK-CALL program code, indexed by (choice - 1). */
    private static final String[] INVOICE_PROGRAM_CODES = {
        "IV0010", "IV0020", "IV0030", "IV0040", "AR0010", "AR0020", "AP0010", "AP0020", "IV0050"
    };

    /** Submenu choice 1..N -> WK-CALL program code, indexed by (choice - 1). */
    private static final String[] REPORT_PROGRAM_CODES = {
        "RP0010", "RP0020", "RP0030", "RP0040", "RP0050", "RP0060", "RP0070",
        "RP0080", "RP0090", "RP0100", "RP0110", "RP0120", "RP0130", "RP0140"
    };

    /** Submenu choice 1..N -> WK-CALL program code, indexed by (choice - 1). */
    private static final String[] BATCH_PROGRAM_CODES = {
        "BT0010", "BT0020", "BT0030", "BT0040", "BT0050", "BT0060", "BT0070", "BT0080", "BT0090"
    };

    public Menu00Service(
            DateutService dateutService,
            ChklogService chklogService,
            Ms0010Service ms0010Service,
            Ms0020Service ms0020Service,
            Ms0030Service ms0030Service,
            Ms0040Service ms0040Service,
            Ms0050Service ms0050Service,
            Ms0060Service ms0060Service,
            Ms0070Service ms0070Service,
            Ms0080Service ms0080Service,
            Ms0090Service ms0090Service,
            Ms0100Service ms0100Service,
            Ms0110Service ms0110Service,
            Ms0120Service ms0120Service,
            Oe0010Service oe0010Service,
            Oe0020Service oe0020Service,
            Oe0030Service oe0030Service,
            Oe0040Service oe0040Service,
            Oe0050Service oe0050Service,
            Sh0010Service sh0010Service,
            Sl0010Service sl0010Service,
            Sl0020Service sl0020Service,
            Sl0030Service sl0030Service,
            Sl0040Service sl0040Service,
            Pu0010Service pu0010Service,
            Pu0020Service pu0020Service,
            Pu0030Service pu0030Service,
            Pu0040Service pu0040Service,
            Rc0010Service rc0010Service,
            Iv0010Service iv0010Service,
            Iv0020Service iv0020Service,
            Iv0030Service iv0030Service,
            Iv0040Service iv0040Service,
            Iv0050Service iv0050Service,
            Ar0010Service ar0010Service,
            Ar0020Service ar0020Service,
            Ap0010Service ap0010Service,
            Ap0020Service ap0020Service,
            Rp0010Service rp0010Service,
            Rp0020Service rp0020Service,
            Rp0030Service rp0030Service,
            Rp0040Service rp0040Service,
            Rp0050Service rp0050Service,
            Rp0060Service rp0060Service,
            Rp0070Service rp0070Service,
            Rp0080Service rp0080Service,
            Rp0090Service rp0090Service,
            Rp0100Service rp0100Service,
            Rp0110Service rp0110Service,
            Rp0120Service rp0120Service,
            Rp0130Service rp0130Service,
            Rp0140Service rp0140Service,
            Bt0010Service bt0010Service,
            Bt0020Service bt0020Service,
            Bt0030Service bt0030Service,
            Bt0040Service bt0040Service,
            Bt0050Service bt0050Service,
            Bt0060Service bt0060Service,
            Bt0070Service bt0070Service,
            Bt0080Service bt0080Service,
            Bt0090Service bt0090Service,
            ScreenRendererInstance renderer) {
        this.dateutService = dateutService;
        this.chklogService = chklogService;
        this.ms0010Service = ms0010Service;
        this.ms0020Service = ms0020Service;
        this.ms0030Service = ms0030Service;
        this.ms0040Service = ms0040Service;
        this.ms0050Service = ms0050Service;
        this.ms0060Service = ms0060Service;
        this.ms0070Service = ms0070Service;
        this.ms0080Service = ms0080Service;
        this.ms0090Service = ms0090Service;
        this.ms0100Service = ms0100Service;
        this.ms0110Service = ms0110Service;
        this.ms0120Service = ms0120Service;
        this.oe0010Service = oe0010Service;
        this.oe0020Service = oe0020Service;
        this.oe0030Service = oe0030Service;
        this.oe0040Service = oe0040Service;
        this.oe0050Service = oe0050Service;
        this.sh0010Service = sh0010Service;
        this.sl0010Service = sl0010Service;
        this.sl0020Service = sl0020Service;
        this.sl0030Service = sl0030Service;
        this.sl0040Service = sl0040Service;
        this.pu0010Service = pu0010Service;
        this.pu0020Service = pu0020Service;
        this.pu0030Service = pu0030Service;
        this.pu0040Service = pu0040Service;
        this.rc0010Service = rc0010Service;
        this.iv0010Service = iv0010Service;
        this.iv0020Service = iv0020Service;
        this.iv0030Service = iv0030Service;
        this.iv0040Service = iv0040Service;
        this.iv0050Service = iv0050Service;
        this.ar0010Service = ar0010Service;
        this.ar0020Service = ar0020Service;
        this.ap0010Service = ap0010Service;
        this.ap0020Service = ap0020Service;
        this.rp0010Service = rp0010Service;
        this.rp0020Service = rp0020Service;
        this.rp0030Service = rp0030Service;
        this.rp0040Service = rp0040Service;
        this.rp0050Service = rp0050Service;
        this.rp0060Service = rp0060Service;
        this.rp0070Service = rp0070Service;
        this.rp0080Service = rp0080Service;
        this.rp0090Service = rp0090Service;
        this.rp0100Service = rp0100Service;
        this.rp0110Service = rp0110Service;
        this.rp0120Service = rp0120Service;
        this.rp0130Service = rp0130Service;
        this.rp0140Service = rp0140Service;
        this.bt0010Service = bt0010Service;
        this.bt0020Service = bt0020Service;
        this.bt0030Service = bt0030Service;
        this.bt0040Service = bt0040Service;
        this.bt0050Service = bt0050Service;
        this.bt0060Service = bt0060Service;
        this.bt0070Service = bt0070Service;
        this.bt0080Service = bt0080Service;
        this.bt0090Service = bt0090Service;
        this.renderer = renderer;
    }

    @Override
    public void setRenderer(ScreenRendererInstance renderer) {
        super.setRenderer(renderer);
        if (dateutService instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (chklogService instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (ms0010Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (ms0020Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (ms0030Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (ms0040Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (ms0050Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (ms0060Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (ms0070Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (ms0080Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (ms0090Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (ms0100Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (ms0110Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (ms0120Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (oe0010Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (oe0020Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (oe0030Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (oe0040Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (oe0050Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (sh0010Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (sl0010Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (sl0020Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (sl0030Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (sl0040Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (pu0010Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (pu0020Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (pu0030Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (pu0040Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (rc0010Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (iv0010Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (iv0020Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (iv0030Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (iv0040Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (iv0050Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (ar0010Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (ar0020Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (ap0010Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (ap0020Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (rp0010Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (rp0020Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (rp0030Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (rp0040Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (rp0050Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (rp0060Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (rp0070Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (rp0080Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (rp0090Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (rp0100Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (rp0110Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (rp0120Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (rp0130Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (rp0140Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (bt0010Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (bt0020Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (bt0030Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (bt0040Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (bt0050Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (bt0060Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (bt0070Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (bt0080Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
        if (bt0090Service instanceof ScreenRendererAware rra) {
            rra.setRenderer(renderer);
        }
    }

    /**
     * Program entry point — runs the first COBOL paragraph via runChain. Base class run() wraps
     * this in StopRun/Abort/Exception handling.
     */
    @Override
    protected void mainProcess() {
        // Import anchors for types referenced by emitted paragraph bodies:
        // StopRunSignal, JobAbortException, ParagraphJumpSignal, ProgramExitSignal
        runChain(this::runMainProgram);
    }

    /**
     * Returns COBOL COMPLETION-CODE after run() completes. Used by Tasklet to propagate exit code
     * into StepExecutionContext.
     */
    @Override
    public int getCompletionCode() {
        return ws.getCompletionCode();
    }

    /**
     * Set COBOL COMPLETION-CODE. Called by base class run() on Abort (255) / Exception (12) paths.
     */
    @Override
    protected void setCompletionCode(int code) {
        ws.setCompletionCode(code);
    }

    /**
     * Returns this program's FileSet (or null if no files declared). Base class run() uses this for
     * commit/rollback/closeAll lifecycle.
     */
    @Override
    protected AbstractDatasets getFileSet() {
        return null;
    }

    /** COBOL paragraph: MAIN-000 */
    private void runMainProgram() {
        runChain(this::initializeProgram);
        runChain(this::startSignOn);
        if ((ws.getEndFlg() != 1)) {
            while ((ws.getEndFlg() != 1)) {
                runChain(this::displayMainMenuAndDispatch);
            }
        }
        runChain(this::displaySignOffMessage);
        throw new StopRunSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeProgram() {
        ws.setWkProgid("MENU00");
        ws.setWkTitle("SAKURA Sales Management System");
        ws.setKdFunc("TODY");
        dateut(ws.getKdate());
        ws.setWkSysymd(ws.getKdDate1());
    }

    /** COBOL paragraph: SON-010 */
    private void startSignOn() {
        ws.setWkLoginTry(0);
        // fall-through to next paragraph
        authenticateUser();
    }

    /** COBOL paragraph: SON-020 */
    private void authenticateUser() {
        while (true) {
            ws.setWkLoginTry(ws.getWkLoginTry() + 1);
            ws.setKlLogin(" ");
            ws.setKlPassword(" ");
            renderer.displayScreen(ScreenDefs.getScreen("DS-HEADER"), ws);
            ws.setWkMsgLine("Enter login - PF3 to quit");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            renderer.displayScreen(ScreenDefs.getScreen("DS-LOGIN"), ws);
            String scValKlLogin0 =
                    Utility.acceptScreen(
                            "KL-LOGIN",
                            () -> renderer.acceptField(ScreenDefs.getInput("KL-LOGIN")));
            ws.setKlLogin(scValKlLogin0);
            String scValKlPassword1 =
                    Utility.acceptScreen(
                            "KL-PASSWORD",
                            () -> renderer.acceptField(ScreenDefs.getInput("KL-PASSWORD")));
            ws.setKlPassword(scValKlPassword1);
            broadcastEstsStatus();
            if (isQuitKeyPressed()) {
                ws.setString("END-FLG", "1");
                return;
            }
            chklog(ws.getKlogin());
            if (Utility.fieldEquals(ws.getKlStatus(), "00")) {
                ws.setWkUserCode(ws.getKlUserCode());
                ws.setWkUserName(ws.getKlUserName());
                ws.setWkUserRole(ws.getKlRole());
                ws.setWkUserAuth(ws.getKlAuth());
                return;
            }
            ws.setWkMsgLine("Invalid login or password");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            if (ws.getWkLoginTry() >= 3) {
                ws.setWkMsgLine("Too many attempts - exiting");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
                ws.setString("END-FLG", "1");
                return;
            }
        }
    }

    /** COBOL paragraph: MLOOP-010 */
    private void displayMainMenuAndDispatch() {
        ws.setWkChoice(0);
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEADER"), ws);
        ws.setWkMsgLine(ws.getWkUserName());
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
        renderer.displayScreen(ScreenDefs.getScreen("DS-MAIN"), ws);
        String scValWkChoice2 =
                Utility.acceptScreen(
                        "WK-CHOICE", () -> renderer.acceptField(ScreenDefs.getInput("WK-CHOICE")));
        ws.setWkChoice(Utility.parseIntOr(scValWkChoice2.trim(), 0));
        broadcastEstsStatus();
        if (isQuitKeyPressed()) {
            ws.setString("END-FLG", "1");
            return;
        }
        switch (ws.getWkChoice()) {
            case 0 -> {
                ws.setString("END-FLG", "1");
            }
            case 1 -> {
                runChain(this::runMasterMenuLoop);
            }
            case 2 -> {
                runChain(this::runOrderMenuLoop);
            }
            case 3 -> {
                runChain(this::runPurchaseMenuLoop);
            }
            case 4 -> {
                runChain(this::runInvoiceMenuLoop);
            }
            case 5 -> {
                runChain(this::runInvoiceMenuLoop);
            }
            case 6 -> {
                runChain(this::runReportMenuLoop);
            }
            case 7 -> {
                runChain(this::runBatchMenuLoop);
            }
            default -> {
                ws.setWkMsgLine("Invalid selection");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: SMST-010 */
    private void runMasterMenuLoop() {
        runSubmenuLoop("DS-MENU-MST", MASTER_PROGRAM_CODES);
    }

    /** COBOL paragraph: SORD-010 */
    private void runOrderMenuLoop() {
        runSubmenuLoop("DS-MENU-ORD", ORDER_PROGRAM_CODES);
    }

    /** COBOL paragraph: SPUR-010 */
    private void runPurchaseMenuLoop() {
        runSubmenuLoop("DS-MENU-PUR", PURCHASE_PROGRAM_CODES);
    }

    /** COBOL paragraph: SINV-010 */
    private void runInvoiceMenuLoop() {
        runSubmenuLoop("DS-MENU-INV", INVOICE_PROGRAM_CODES);
    }

    /** COBOL paragraph: SRPT-010 */
    private void runReportMenuLoop() {
        runSubmenuLoop("DS-MENU-RPT", REPORT_PROGRAM_CODES);
    }

    /** COBOL paragraph: SBAT-010 */
    private void runBatchMenuLoop() {
        while (true) {
            if (!Utility.fieldEquals(
                    Utility.padRight(ws.getWkUserAuth(), 5).substring(4, 5), "1")) {
                ws.setWkMsgLine("Not authorised for batch/closing");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
                return;
            }
            ws.setWkChoice(0);
            renderer.displayScreen(ScreenDefs.getScreen("DS-HEADER"), ws);
            renderer.displayScreen(ScreenDefs.getScreen("DS-MENU-BAT"), ws);
            String scValWkChoice8 =
                    Utility.acceptScreen(
                            "WK-CHOICE",
                            () -> renderer.acceptField(ScreenDefs.getInput("WK-CHOICE")));
            ws.setWkChoice(Utility.parseIntOr(scValWkChoice8.trim(), 0));
            broadcastEstsStatus();
            if (isMenuExitRequested()) {
                return;
            }
            ws.setWkCall(resolveProgramCode(BATCH_PROGRAM_CODES));
            runChain(this::callSelectedProgram);
        }
    }

    /** COBOL paragraph: CPRG-010 */
    private void callSelectedProgram() {
        if (Utility.fieldEquals(ws.getWkCall(), " ")) {
            ws.setWkMsgLine("Invalid selection");
            renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            return;
        }
        switch (ws.getWkCall()) {
            case "MS0010" -> {
                ms0010();
            }
            case "MS0020" -> {
                ms0020();
            }
            case "MS0030" -> {
                ms0030();
            }
            case "MS0040" -> {
                ms0040();
            }
            case "MS0050" -> {
                ms0050();
            }
            case "MS0060" -> {
                ms0060();
            }
            case "MS0070" -> {
                ms0070();
            }
            case "MS0080" -> {
                ms0080();
            }
            case "MS0090" -> {
                ms0090();
            }
            case "MS0100" -> {
                ms0100();
            }
            case "MS0110" -> {
                ms0110();
            }
            case "MS0120" -> {
                ms0120();
            }
            case "OE0010" -> {
                oe0010();
            }
            case "OE0020" -> {
                oe0020();
            }
            case "OE0030" -> {
                oe0030();
            }
            case "OE0040" -> {
                oe0040();
            }
            case "OE0050" -> {
                oe0050();
            }
            case "SH0010" -> {
                sh0010();
            }
            case "SL0010" -> {
                sl0010();
            }
            case "SL0020" -> {
                sl0020();
            }
            case "SL0030" -> {
                sl0030();
            }
            case "SL0040" -> {
                sl0040();
            }
            case "PU0010" -> {
                pu0010();
            }
            case "PU0020" -> {
                pu0020();
            }
            case "PU0030" -> {
                pu0030();
            }
            case "PU0040" -> {
                pu0040();
            }
            case "RC0010" -> {
                rc0010();
            }
            case "IV0010" -> {
                iv0010();
            }
            case "IV0020" -> {
                iv0020();
            }
            case "IV0030" -> {
                iv0030();
            }
            case "IV0040" -> {
                iv0040();
            }
            case "IV0050" -> {
                iv0050();
            }
            case "AR0010" -> {
                ar0010();
            }
            case "AR0020" -> {
                ar0020();
            }
            case "AP0010" -> {
                ap0010();
            }
            case "AP0020" -> {
                ap0020();
            }
            case "RP0010" -> {
                rp0010();
            }
            case "RP0020" -> {
                rp0020();
            }
            case "RP0030" -> {
                rp0030();
            }
            case "RP0040" -> {
                rp0040();
            }
            case "RP0050" -> {
                rp0050();
            }
            case "RP0060" -> {
                rp0060();
            }
            case "RP0070" -> {
                rp0070();
            }
            case "RP0080" -> {
                rp0080();
            }
            case "RP0090" -> {
                rp0090();
            }
            case "RP0100" -> {
                rp0100();
            }
            case "RP0110" -> {
                rp0110();
            }
            case "RP0120" -> {
                rp0120();
            }
            case "RP0130" -> {
                rp0130();
            }
            case "RP0140" -> {
                rp0140();
            }
            case "BT0010" -> {
                bt0010();
            }
            case "BT0020" -> {
                bt0020();
            }
            case "BT0030" -> {
                bt0030();
            }
            case "BT0040" -> {
                bt0040();
            }
            case "BT0050" -> {
                bt0050();
            }
            case "BT0060" -> {
                bt0060();
            }
            case "BT0070" -> {
                bt0070();
            }
            case "BT0080" -> {
                bt0080();
            }
            case "BT0090" -> {
                bt0090();
            }
            default -> {
                ws.setWkMsgLine("Program not available yet");
                renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
            }
        }
    }

    /** COBOL paragraph: SOFF-010 */
    private void displaySignOffMessage() {
        renderer.displayScreen(ScreenDefs.getScreen("DS-HEADER"), ws);
        ws.setWkMsgLine("Signed off - thank you");
        renderer.displayScreen(ScreenDefs.getScreen("DS-MSG"), ws);
    }

    /** COBOL CALL DATEUT — delegates to injected DateutService. */
    private void dateut(Object... args) {
        DateutLinkParm params = new DateutLinkParm();
        params.getKdate().setKdFunc(ws.getKdFunc());
        params.getKdate().setKdDate1(ws.getKdDate1());
        params.getKdate().setKdDate2(ws.getKdDate2());
        params.getKdate().setKdDays(ws.getKdDays());
        params.getKdate().setKdWeekday(ws.getKdWeekday());
        params.getKdate().setKdStatus(ws.getKdStatus());
        dateutService.execute(params);
        ws.setKdFunc(params.getKdate().getKdFunc());
        ws.setKdDate1(params.getKdate().getKdDate1());
        ws.setKdDate2(params.getKdate().getKdDate2());
        ws.setKdDays(params.getKdate().getKdDays());
        ws.setKdWeekday(params.getKdate().getKdWeekday());
        ws.setKdStatus(params.getKdate().getKdStatus());
    }

    /** COBOL CALL CHKLOG — delegates to injected ChklogService. */
    private void chklog(Object... args) {
        ChklogLinkParm params = new ChklogLinkParm();
        params.getKlogin().setKlLogin(ws.getKlLogin());
        params.getKlogin().setKlPassword(ws.getKlPassword());
        params.getKlogin().setKlUserCode(ws.getKlUserCode());
        params.getKlogin().setKlUserName(ws.getKlUserName());
        params.getKlogin().setKlRole(ws.getKlRole());
        params.getKlogin().setKlAuth(ws.getKlAuth());
        params.getKlogin().setKlStatus(ws.getKlStatus());
        chklogService.execute(params);
        ws.setKlLogin(params.getKlogin().getKlLogin());
        ws.setKlPassword(params.getKlogin().getKlPassword());
        ws.setKlUserCode(params.getKlogin().getKlUserCode());
        ws.setKlUserName(params.getKlogin().getKlUserName());
        ws.setKlRole(params.getKlogin().getKlRole());
        ws.setKlAuth(params.getKlogin().getKlAuth());
        ws.setKlStatus(params.getKlogin().getKlStatus());
    }

    /** COBOL CALL MS0010 — runs injected Ms0010Service in-process (no USING params). */
    private void ms0010(Object... args) {
        ms0010Service.execute();
    }

    /** COBOL CALL MS0020 — runs injected Ms0020Service in-process (no USING params). */
    private void ms0020(Object... args) {
        ms0020Service.execute();
    }

    /** COBOL CALL MS0030 — runs injected Ms0030Service in-process (no USING params). */
    private void ms0030(Object... args) {
        ms0030Service.execute();
    }

    /** COBOL CALL MS0040 — runs injected Ms0040Service in-process (no USING params). */
    private void ms0040(Object... args) {
        ms0040Service.execute();
    }

    /** COBOL CALL MS0050 — runs injected Ms0050Service in-process (no USING params). */
    private void ms0050(Object... args) {
        ms0050Service.execute();
    }

    /** COBOL CALL MS0060 — runs injected Ms0060Service in-process (no USING params). */
    private void ms0060(Object... args) {
        ms0060Service.execute();
    }

    /** COBOL CALL MS0070 — runs injected Ms0070Service in-process (no USING params). */
    private void ms0070(Object... args) {
        ms0070Service.execute();
    }

    /** COBOL CALL MS0080 — runs injected Ms0080Service in-process (no USING params). */
    private void ms0080(Object... args) {
        ms0080Service.execute();
    }

    /** COBOL CALL MS0090 — runs injected Ms0090Service in-process (no USING params). */
    private void ms0090(Object... args) {
        ms0090Service.execute();
    }

    /** COBOL CALL MS0100 — runs injected Ms0100Service in-process (no USING params). */
    private void ms0100(Object... args) {
        ms0100Service.execute();
    }

    /** COBOL CALL MS0110 — runs injected Ms0110Service in-process (no USING params). */
    private void ms0110(Object... args) {
        ms0110Service.execute();
    }

    /** COBOL CALL MS0120 — runs injected Ms0120Service in-process (no USING params). */
    private void ms0120(Object... args) {
        ms0120Service.execute();
    }

    /** COBOL CALL OE0010 — runs injected Oe0010Service in-process (no USING params). */
    private void oe0010(Object... args) {
        oe0010Service.execute();
    }

    /** COBOL CALL OE0020 — runs injected Oe0020Service in-process (no USING params). */
    private void oe0020(Object... args) {
        oe0020Service.execute();
    }

    /** COBOL CALL OE0030 — runs injected Oe0030Service in-process (no USING params). */
    private void oe0030(Object... args) {
        oe0030Service.execute();
    }

    /** COBOL CALL OE0040 — runs injected Oe0040Service in-process (no USING params). */
    private void oe0040(Object... args) {
        oe0040Service.execute();
    }

    /** COBOL CALL OE0050 — runs injected Oe0050Service in-process (no USING params). */
    private void oe0050(Object... args) {
        oe0050Service.execute();
    }

    /** COBOL CALL SH0010 — runs injected Sh0010Service in-process (no USING params). */
    private void sh0010(Object... args) {
        sh0010Service.execute();
    }

    /** COBOL CALL SL0010 — runs injected Sl0010Service in-process (no USING params). */
    private void sl0010(Object... args) {
        sl0010Service.execute();
    }

    /** COBOL CALL SL0020 — runs injected Sl0020Service in-process (no USING params). */
    private void sl0020(Object... args) {
        sl0020Service.execute();
    }

    /** COBOL CALL SL0030 — runs injected Sl0030Service in-process (no USING params). */
    private void sl0030(Object... args) {
        sl0030Service.execute();
    }

    /** COBOL CALL SL0040 — runs injected Sl0040Service in-process (no USING params). */
    private void sl0040(Object... args) {
        sl0040Service.execute();
    }

    /** COBOL CALL PU0010 — runs injected Pu0010Service in-process (no USING params). */
    private void pu0010(Object... args) {
        pu0010Service.execute();
    }

    /** COBOL CALL PU0020 — runs injected Pu0020Service in-process (no USING params). */
    private void pu0020(Object... args) {
        pu0020Service.execute();
    }

    /** COBOL CALL PU0030 — runs injected Pu0030Service in-process (no USING params). */
    private void pu0030(Object... args) {
        pu0030Service.execute();
    }

    /** COBOL CALL PU0040 — runs injected Pu0040Service in-process (no USING params). */
    private void pu0040(Object... args) {
        pu0040Service.execute();
    }

    /** COBOL CALL RC0010 — runs injected Rc0010Service in-process (no USING params). */
    private void rc0010(Object... args) {
        rc0010Service.execute();
    }

    /** COBOL CALL IV0010 — runs injected Iv0010Service in-process (no USING params). */
    private void iv0010(Object... args) {
        iv0010Service.execute();
    }

    /** COBOL CALL IV0020 — runs injected Iv0020Service in-process (no USING params). */
    private void iv0020(Object... args) {
        iv0020Service.execute();
    }

    /** COBOL CALL IV0030 — runs injected Iv0030Service in-process (no USING params). */
    private void iv0030(Object... args) {
        iv0030Service.execute();
    }

    /** COBOL CALL IV0040 — runs injected Iv0040Service in-process (no USING params). */
    private void iv0040(Object... args) {
        iv0040Service.execute();
    }

    /** COBOL CALL IV0050 — runs injected Iv0050Service in-process (no USING params). */
    private void iv0050(Object... args) {
        iv0050Service.execute();
    }

    /** COBOL CALL AR0010 — runs injected Ar0010Service in-process (no USING params). */
    private void ar0010(Object... args) {
        ar0010Service.execute();
    }

    /** COBOL CALL AR0020 — runs injected Ar0020Service in-process (no USING params). */
    private void ar0020(Object... args) {
        ar0020Service.execute();
    }

    /** COBOL CALL AP0010 — runs injected Ap0010Service in-process (no USING params). */
    private void ap0010(Object... args) {
        ap0010Service.execute();
    }

    /** COBOL CALL AP0020 — runs injected Ap0020Service in-process (no USING params). */
    private void ap0020(Object... args) {
        ap0020Service.execute();
    }

    /** COBOL CALL RP0010 — runs injected Rp0010Service in-process (no USING params). */
    private void rp0010(Object... args) {
        rp0010Service.execute();
    }

    /** COBOL CALL RP0020 — runs injected Rp0020Service in-process (no USING params). */
    private void rp0020(Object... args) {
        rp0020Service.execute();
    }

    /** COBOL CALL RP0030 — runs injected Rp0030Service in-process (no USING params). */
    private void rp0030(Object... args) {
        rp0030Service.execute();
    }

    /** COBOL CALL RP0040 — runs injected Rp0040Service in-process (no USING params). */
    private void rp0040(Object... args) {
        rp0040Service.execute();
    }

    /** COBOL CALL RP0050 — runs injected Rp0050Service in-process (no USING params). */
    private void rp0050(Object... args) {
        rp0050Service.execute();
    }

    /** COBOL CALL RP0060 — runs injected Rp0060Service in-process (no USING params). */
    private void rp0060(Object... args) {
        rp0060Service.execute();
    }

    /** COBOL CALL RP0070 — runs injected Rp0070Service in-process (no USING params). */
    private void rp0070(Object... args) {
        rp0070Service.execute();
    }

    /** COBOL CALL RP0080 — runs injected Rp0080Service in-process (no USING params). */
    private void rp0080(Object... args) {
        rp0080Service.execute();
    }

    /** COBOL CALL RP0090 — runs injected Rp0090Service in-process (no USING params). */
    private void rp0090(Object... args) {
        rp0090Service.execute();
    }

    /** COBOL CALL RP0100 — runs injected Rp0100Service in-process (no USING params). */
    private void rp0100(Object... args) {
        rp0100Service.execute();
    }

    /** COBOL CALL RP0110 — runs injected Rp0110Service in-process (no USING params). */
    private void rp0110(Object... args) {
        rp0110Service.execute();
    }

    /** COBOL CALL RP0120 — runs injected Rp0120Service in-process (no USING params). */
    private void rp0120(Object... args) {
        rp0120Service.execute();
    }

    /** COBOL CALL RP0130 — runs injected Rp0130Service in-process (no USING params). */
    private void rp0130(Object... args) {
        rp0130Service.execute();
    }

    /** COBOL CALL RP0140 — runs injected Rp0140Service in-process (no USING params). */
    private void rp0140(Object... args) {
        rp0140Service.execute();
    }

    /** COBOL CALL BT0010 — runs injected Bt0010Service in-process (no USING params). */
    private void bt0010(Object... args) {
        bt0010Service.execute();
    }

    /** COBOL CALL BT0020 — runs injected Bt0020Service in-process (no USING params). */
    private void bt0020(Object... args) {
        bt0020Service.execute();
    }

    /** COBOL CALL BT0030 — runs injected Bt0030Service in-process (no USING params). */
    private void bt0030(Object... args) {
        bt0030Service.execute();
    }

    /** COBOL CALL BT0040 — runs injected Bt0040Service in-process (no USING params). */
    private void bt0040(Object... args) {
        bt0040Service.execute();
    }

    /** COBOL CALL BT0050 — runs injected Bt0050Service in-process (no USING params). */
    private void bt0050(Object... args) {
        bt0050Service.execute();
    }

    /** COBOL CALL BT0060 — runs injected Bt0060Service in-process (no USING params). */
    private void bt0060(Object... args) {
        bt0060Service.execute();
    }

    /** COBOL CALL BT0070 — runs injected Bt0070Service in-process (no USING params). */
    private void bt0070(Object... args) {
        bt0070Service.execute();
    }

    /** COBOL CALL BT0080 — runs injected Bt0080Service in-process (no USING params). */
    private void bt0080(Object... args) {
        bt0080Service.execute();
    }

    /** COBOL CALL BT0090 — runs injected Bt0090Service in-process (no USING params). */
    private void bt0090(Object... args) {
        bt0090Service.execute();
    }

    /** Reads CRT STATUS and broadcasts to all ESTS fields — COBOL implicit after each ACCEPT. */
    private void broadcastEstsStatus() {
        Utility.broadcastEndStatus(ws, renderer.readEndStatus(), "ESTS");
    }

    /** True when the terminal signaled PF3 (quit) after the last screen ACCEPT. */
    private boolean isQuitKeyPressed() {
        return Utility.fieldEquals(ws.getEsts(), "03");
    }

    /** True when the user pressed PF3 or chose "0" to leave the current submenu. */
    private boolean isMenuExitRequested() {
        return isQuitKeyPressed() || ws.getWkChoice() == 0;
    }

    /** Maps the current WK-CHOICE to its program code, or blank if out of range. */
    private String resolveProgramCode(String[] programCodes) {
        int choice = ws.getWkChoice();
        return (choice >= 1 && choice <= programCodes.length) ? programCodes[choice - 1] : " ";
    }

    /**
     * Runs a submenu loop: display the header and submenu screen, accept the user's numeric choice,
     * translate it to a program code, and invoke the selected program until the user exits (PF3 or
     * choice 0).
     */
    private void runSubmenuLoop(String menuScreenKey, String[] programCodes) {
        while (true) {
            ws.setWkChoice(0);
            renderer.displayScreen(ScreenDefs.getScreen("DS-HEADER"), ws);
            renderer.displayScreen(ScreenDefs.getScreen(menuScreenKey), ws);
            String scValWkChoice =
                    Utility.acceptScreen(
                            "WK-CHOICE",
                            () -> renderer.acceptField(ScreenDefs.getInput("WK-CHOICE")));
            ws.setWkChoice(Utility.parseIntOr(scValWkChoice.trim(), 0));
            broadcastEstsStatus();
            if (isMenuExitRequested()) {
                return;
            }
            ws.setWkCall(resolveProgramCode(programCodes));
            runChain(this::callSelectedProgram);
        }
    }
}
