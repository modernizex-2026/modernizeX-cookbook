package com.sakura.menu00.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
import com.sakura.runtime.ScreenModels;
import com.sakura.runtime.ScreenRendererInstance;
import com.sakura.runtime.StopRunSignal;
import com.sakura.runtime.linkage.ChklogLinkParm;
import com.sakura.runtime.linkage.DateutLinkParm;
import com.sakura.sh0010.service.Sh0010Service;
import com.sakura.sl0010.service.Sl0010Service;
import com.sakura.sl0020.service.Sl0020Service;
import com.sakura.sl0030.service.Sl0030Service;
import com.sakura.sl0040.service.Sl0040Service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for {@link Menu00Service}, derived from COBOL program MENU00 (sign-on and two-level
 * main menu dispatcher). Ground truth for inputs/expected values: MENU00.cob PROCEDURE DIVISION
 * (MAIN-000 / SIGN-ON / MENU-LOOP / SUB-* / CALL-PROG / SIGN-OFF).
 *
 * <p>Every CALLed subprogram (DATEUT, CHKLOG, MS0010..BT0090) is a plain {@code @Mock}; none of
 * them declare {@code ScreenRendererAware}-relevant behavior needed here, so a bare Mockito mock
 * (which does nothing and returns nulls/defaults) faithfully stands in for "some other program
 * ran". The screen renderer is mocked and every {@code displayScreen}/{@code acceptField}/{@code
 * readEndStatus()} call is captured so the exact menu-navigation sequence can be asserted.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Timeout(value = 10, unit = TimeUnit.SECONDS)
class Menu00ServiceTest {

    @Mock private DateutService dateutService;
    @Mock private ChklogService chklogService;
    @Mock private Ms0010Service ms0010Service;
    @Mock private Ms0020Service ms0020Service;
    @Mock private Ms0030Service ms0030Service;
    @Mock private Ms0040Service ms0040Service;
    @Mock private Ms0050Service ms0050Service;
    @Mock private Ms0060Service ms0060Service;
    @Mock private Ms0070Service ms0070Service;
    @Mock private Ms0080Service ms0080Service;
    @Mock private Ms0090Service ms0090Service;
    @Mock private Ms0100Service ms0100Service;
    @Mock private Ms0110Service ms0110Service;
    @Mock private Ms0120Service ms0120Service;
    @Mock private Oe0010Service oe0010Service;
    @Mock private Oe0020Service oe0020Service;
    @Mock private Oe0030Service oe0030Service;
    @Mock private Oe0040Service oe0040Service;
    @Mock private Oe0050Service oe0050Service;
    @Mock private Sh0010Service sh0010Service;
    @Mock private Sl0010Service sl0010Service;
    @Mock private Sl0020Service sl0020Service;
    @Mock private Sl0030Service sl0030Service;
    @Mock private Sl0040Service sl0040Service;
    @Mock private Pu0010Service pu0010Service;
    @Mock private Pu0020Service pu0020Service;
    @Mock private Pu0030Service pu0030Service;
    @Mock private Pu0040Service pu0040Service;
    @Mock private Rc0010Service rc0010Service;
    @Mock private Iv0010Service iv0010Service;
    @Mock private Iv0020Service iv0020Service;
    @Mock private Iv0030Service iv0030Service;
    @Mock private Iv0040Service iv0040Service;
    @Mock private Iv0050Service iv0050Service;
    @Mock private Ar0010Service ar0010Service;
    @Mock private Ar0020Service ar0020Service;
    @Mock private Ap0010Service ap0010Service;
    @Mock private Ap0020Service ap0020Service;
    @Mock private Rp0010Service rp0010Service;
    @Mock private Rp0020Service rp0020Service;
    @Mock private Rp0030Service rp0030Service;
    @Mock private Rp0040Service rp0040Service;
    @Mock private Rp0050Service rp0050Service;
    @Mock private Rp0060Service rp0060Service;
    @Mock private Rp0070Service rp0070Service;
    @Mock private Rp0080Service rp0080Service;
    @Mock private Rp0090Service rp0090Service;
    @Mock private Rp0100Service rp0100Service;
    @Mock private Rp0110Service rp0110Service;
    @Mock private Rp0120Service rp0120Service;
    @Mock private Rp0130Service rp0130Service;
    @Mock private Rp0140Service rp0140Service;
    @Mock private Bt0010Service bt0010Service;
    @Mock private Bt0020Service bt0020Service;
    @Mock private Bt0030Service bt0030Service;
    @Mock private Bt0040Service bt0040Service;
    @Mock private Bt0050Service bt0050Service;
    @Mock private Bt0060Service bt0060Service;
    @Mock private Bt0070Service bt0070Service;
    @Mock private Bt0080Service bt0080Service;
    @Mock private Bt0090Service bt0090Service;
    @Mock private ScreenRendererInstance renderer;

    private Menu00Service service;
    private Menu00FieldAccess ws;

    private final List<String> screenInteractions = new ArrayList<>();

    /** Field name -> queue of values to return on successive acceptField() calls for that field. */
    private final Map<String, List<String>> acceptFieldQueues = new HashMap<>();

    private final Map<String, AtomicInteger> acceptFieldIdx = new HashMap<>();

    @BeforeEach
    void setUp() throws Exception {
        service =
                new Menu00Service(
                        dateutService,
                        chklogService,
                        ms0010Service,
                        ms0020Service,
                        ms0030Service,
                        ms0040Service,
                        ms0050Service,
                        ms0060Service,
                        ms0070Service,
                        ms0080Service,
                        ms0090Service,
                        ms0100Service,
                        ms0110Service,
                        ms0120Service,
                        oe0010Service,
                        oe0020Service,
                        oe0030Service,
                        oe0040Service,
                        oe0050Service,
                        sh0010Service,
                        sl0010Service,
                        sl0020Service,
                        sl0030Service,
                        sl0040Service,
                        pu0010Service,
                        pu0020Service,
                        pu0030Service,
                        pu0040Service,
                        rc0010Service,
                        iv0010Service,
                        iv0020Service,
                        iv0030Service,
                        iv0040Service,
                        iv0050Service,
                        ar0010Service,
                        ar0020Service,
                        ap0010Service,
                        ap0020Service,
                        rp0010Service,
                        rp0020Service,
                        rp0030Service,
                        rp0040Service,
                        rp0050Service,
                        rp0060Service,
                        rp0070Service,
                        rp0080Service,
                        rp0090Service,
                        rp0100Service,
                        rp0110Service,
                        rp0120Service,
                        rp0130Service,
                        rp0140Service,
                        bt0010Service,
                        bt0020Service,
                        bt0030Service,
                        bt0040Service,
                        bt0050Service,
                        bt0060Service,
                        bt0070Service,
                        bt0080Service,
                        bt0090Service,
                        renderer);
        service.setRenderer(renderer);
        ws = getWs();

        doAnswer(
                        inv -> {
                            ScreenModels.ScreenDef def = inv.getArgument(0);
                            screenInteractions.add("displayScreen:" + def.name);
                            return null;
                        })
                .when(renderer)
                .displayScreen(any(), any());

        doAnswer(
                        inv -> {
                            ScreenModels.InputFieldDef field = inv.getArgument(0);
                            List<String> queue = acceptFieldQueues.get(field.name);
                            if (queue == null || queue.isEmpty()) {
                                return "";
                            }
                            AtomicInteger idx =
                                    acceptFieldIdx.computeIfAbsent(
                                            field.name, k -> new AtomicInteger(0));
                            int i = Math.min(idx.getAndIncrement(), queue.size() - 1);
                            return queue.get(i);
                        })
                .when(renderer)
                .acceptField(any());

        doReturn("00").when(renderer).readEndStatus();
    }

    @AfterEach
    void tearDown() {
        screenInteractions.clear();
        acceptFieldQueues.clear();
        acceptFieldIdx.clear();
    }

    /* ── reflection helpers ── */

    private Menu00FieldAccess getWs() throws Exception {
        Field f = Menu00Service.class.getDeclaredField("ws");
        f.setAccessible(true);
        return (Menu00FieldAccess) f.get(service);
    }

    private void invokePrivate(String name) {
        try {
            Method m = Menu00Service.class.getDeclaredMethod(name);
            m.setAccessible(true);
            m.invoke(service);
        } catch (InvocationTargetException e) {
            unwrapAndRethrow(e);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private void invokePrivate(String name, Class<?>[] paramTypes, Object[] args) {
        try {
            Method m = Menu00Service.class.getDeclaredMethod(name, paramTypes);
            m.setAccessible(true);
            m.invoke(service, args);
        } catch (InvocationTargetException e) {
            unwrapAndRethrow(e);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private void unwrapAndRethrow(InvocationTargetException e) {
        Throwable cause = e.getCause();
        if (cause instanceof RuntimeException re) {
            throw re;
        }
        throw new RuntimeException(cause);
    }

    /** Queues successive WK-CHOICE (or any field) values returned by acceptField(). */
    private void stubField(String fieldName, String... values) {
        acceptFieldQueues.put(fieldName, List.of(values));
        acceptFieldIdx.put(fieldName, new AtomicInteger(0));
    }

    /** Queues successive ESTS values returned by readEndStatus() across ACCEPT cycles. */
    private void stubEsts(String... values) {
        Object[] rest = new Object[values.length - 1];
        for (int i = 1; i < values.length; i++) {
            rest[i - 1] = values[i];
        }
        doReturn(values[0], rest).when(renderer).readEndStatus();
    }

    /**
     * Number of execute() invocations recorded on a mocked sub-program service. Ignores the
     * incidental setRenderer(...) call every mock receives via {@code
     * service.setRenderer(renderer)} in setUp() (Mockito mocks of classes extending
     * BatchServiceBase satisfy {@code instanceof ScreenRendererAware}).
     */
    private long executeCallCount(Object mock) {
        return mockingDetails(mock).getInvocations().stream()
                .filter(inv -> inv.getMethod().getName().equals("execute"))
                .count();
    }

    private void stubChklog(String... statuses) {
        Object[] rest = new Object[statuses.length - 1];
        for (int i = 1; i < statuses.length; i++) {
            rest[i - 1] = statuses[i];
        }
        AtomicInteger idx = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            ChklogLinkParm p = inv.getArgument(0);
                            int i = Math.min(idx.getAndIncrement(), statuses.length - 1);
                            p.getKlogin().setKlStatus(statuses[i]);
                            p.getKlogin().setKlUserCode(1);
                            p.getKlogin().setKlUserName("Taro");
                            p.getKlogin().setKlRole(1);
                            p.getKlogin().setKlAuth("00001");
                            return null;
                        })
                .when(chklogService)
                .execute(any());
    }

    /* ── getCompletionCode / setCompletionCode / getFileSet ── */

    @Test
    void getCompletionCode_delegatesToWorkingStorage() {
        ws.setCompletionCode(42);
        assertEquals(42, service.getCompletionCode());
    }

    @Test
    void setCompletionCode_delegatesToWorkingStorage() {
        invokePrivate("setCompletionCode", new Class<?>[] {int.class}, new Object[] {77});
        assertEquals(77, ws.getCompletionCode());
    }

    @Test
    void getFileSet_returnsNull() throws Exception {
        Method m = Menu00Service.class.getDeclaredMethod("getFileSet");
        m.setAccessible(true);
        Object result = m.invoke(service);
        assertEquals(null, result);
        assertTrue(result == null);
    }

    /* ── INIT-010 ── */

    @Test
    void initializeProgram_success_setsHeaderFieldsAndSysDate() {
        doAnswer(
                        inv -> {
                            DateutLinkParm p = inv.getArgument(0);
                            p.getKdate().setKdDate1(20260918);
                            p.getKdate().setKdStatus("00");
                            return null;
                        })
                .when(dateutService)
                .execute(any());

        invokePrivate("initializeProgram");

        assertEquals("MENU00", ws.getWkProgid().trim());
        assertEquals("SAKURA Sales Management System", ws.getWkTitle().trim());
        assertEquals("TODY", ws.getKdFunc().trim());
        assertEquals(20260918, ws.getWkSysymd());
        verify(dateutService).execute(any());
    }

    /* ── SON-010 / SON-020 ── */

    @Test
    void startSignOn_quitImmediately_setsEndFlgWithoutCallingChklog() {
        stubEsts("03");

        invokePrivate("startSignOn");

        assertEquals(1, ws.getEndFlg());
        assertEquals(1, ws.getWkLoginTry());
        verify(chklogService, never()).execute(any());
        assertTrue(screenInteractions.contains("displayScreen:DS-LOGIN"));
    }

    @Test
    void startSignOn_validLoginFirstTry_setsUserFieldsWithoutEndFlg() {
        stubEsts("00");
        stubChklog("00");

        invokePrivate("startSignOn");

        assertEquals(0, ws.getEndFlg());
        assertEquals(1, ws.getWkLoginTry());
        assertEquals(1, ws.getWkUserCode());
        assertEquals("Taro", ws.getWkUserName().trim());
        assertEquals(1, ws.getWkUserRole());
        assertEquals("00001", ws.getWkUserAuth().trim());
        verify(chklogService, times(1)).execute(any());
    }

    @Test
    void startSignOn_failsOnceThenSucceeds_retriesAndSetsUserFields() {
        stubEsts("00", "00");
        stubChklog("01", "00");

        invokePrivate("startSignOn");

        assertEquals(0, ws.getEndFlg());
        assertEquals(2, ws.getWkLoginTry());
        verify(chklogService, times(2)).execute(any());
        assertEquals(1, ws.getWkUserCode());
    }

    @Test
    void startSignOn_threeFailedAttempts_setsEndFlgWithTooManyAttemptsMessage() {
        stubEsts("00", "00", "00");
        stubChklog("01", "01", "01");

        invokePrivate("startSignOn");

        assertEquals(1, ws.getEndFlg());
        assertEquals(3, ws.getWkLoginTry());
        verify(chklogService, times(3)).execute(any());
        assertEquals("Too many attempts - exiting", ws.getWkMsgLine().trim());
    }

    /* ── MLOOP-010 ── */

    @Test
    void displayMainMenuAndDispatch_ests03_setsEndFlg() {
        stubEsts("03");

        invokePrivate("displayMainMenuAndDispatch");

        assertEquals(1, ws.getEndFlg());
    }

    @Test
    void displayMainMenuAndDispatch_choiceZero_setsEndFlg() {
        stubField("WK-CHOICE", "0");

        invokePrivate("displayMainMenuAndDispatch");

        assertEquals(1, ws.getEndFlg());
    }

    @Test
    void displayMainMenuAndDispatch_choiceOne_dispatchesMasterMenuLoop() {
        // choice=1 enters the master submenu loop; second submenu ACCEPT returns choice=0 to exit
        // it.
        stubField("WK-CHOICE", "1", "0");

        invokePrivate("displayMainMenuAndDispatch");

        assertTrue(screenInteractions.contains("displayScreen:DS-MENU-MST"));
    }

    @Test
    void displayMainMenuAndDispatch_choiceFive_dispatchesInvoiceMenuLoopSameAsChoiceFour() {
        // COBOL: WHEN 4 PERFORM SUB-INV, WHEN 5 PERFORM SUB-INV (both map to the same submenu).
        stubField("WK-CHOICE", "5", "0");

        invokePrivate("displayMainMenuAndDispatch");

        assertTrue(screenInteractions.contains("displayScreen:DS-MENU-INV"));
    }

    @Test
    void displayMainMenuAndDispatch_invalidChoice_showsInvalidSelectionMessage() {
        stubField("WK-CHOICE", "99");

        invokePrivate("displayMainMenuAndDispatch");

        assertEquals("Invalid selection", ws.getWkMsgLine().trim());
        assertEquals(0, ws.getEndFlg());
    }

    /* ── SMST-010 / SORD-010 / SPUR-010 / SINV-010 / SRPT-010 via runSubmenuLoop ── */

    @Test
    void runSubmenuLoop_ests03_exitsWithoutCallingProgram() {
        stubEsts("03");

        invokePrivate(
                "runSubmenuLoop",
                new Class<?>[] {String.class, String[].class},
                new Object[] {"DS-MENU-MST", new String[] {"MS0010"}});

        assertEquals(0, executeCallCount(ms0010Service));
    }

    @Test
    void runSubmenuLoop_choiceZero_exitsWithoutCallingProgram() {
        stubField("WK-CHOICE", "0");

        invokePrivate(
                "runSubmenuLoop",
                new Class<?>[] {String.class, String[].class},
                new Object[] {"DS-MENU-MST", new String[] {"MS0010"}});

        assertEquals(0, executeCallCount(ms0010Service));
    }

    @Test
    void runSubmenuLoop_validChoice_callsMappedProgramThenLoopsAndExits() {
        stubField("WK-CHOICE", "1", "0");

        invokePrivate(
                "runSubmenuLoop",
                new Class<?>[] {String.class, String[].class},
                new Object[] {"DS-MENU-MST", new String[] {"MS0010"}});

        assertEquals(1, executeCallCount(ms0010Service));
    }

    @Test
    void runSubmenuLoop_choiceOutOfRange_showsInvalidSelectionThenExits() {
        stubField("WK-CHOICE", "9", "0");

        invokePrivate(
                "runSubmenuLoop",
                new Class<?>[] {String.class, String[].class},
                new Object[] {"DS-MENU-MST", new String[] {"MS0010"}});

        assertEquals("Invalid selection", ws.getWkMsgLine().trim());
        assertEquals(0, executeCallCount(ms0010Service));
    }

    @Test
    void runMasterMenuLoop_delegatesToRunSubmenuLoopWithMasterCodes() {
        stubField("WK-CHOICE", "0");

        invokePrivate("runMasterMenuLoop");

        assertTrue(screenInteractions.contains("displayScreen:DS-MENU-MST"));
    }

    @Test
    void runOrderMenuLoop_delegatesToRunSubmenuLoopWithOrderCodes() {
        stubField("WK-CHOICE", "0");

        invokePrivate("runOrderMenuLoop");

        assertTrue(screenInteractions.contains("displayScreen:DS-MENU-ORD"));
    }

    @Test
    void runPurchaseMenuLoop_delegatesToRunSubmenuLoopWithPurchaseCodes() {
        stubField("WK-CHOICE", "0");

        invokePrivate("runPurchaseMenuLoop");

        assertTrue(screenInteractions.contains("displayScreen:DS-MENU-PUR"));
    }

    @Test
    void runInvoiceMenuLoop_delegatesToRunSubmenuLoopWithInvoiceCodes() {
        stubField("WK-CHOICE", "0");

        invokePrivate("runInvoiceMenuLoop");

        assertTrue(screenInteractions.contains("displayScreen:DS-MENU-INV"));
    }

    @Test
    void runReportMenuLoop_delegatesToRunSubmenuLoopWithReportCodes() {
        stubField("WK-CHOICE", "0");

        invokePrivate("runReportMenuLoop");

        assertTrue(screenInteractions.contains("displayScreen:DS-MENU-RPT"));
    }

    /* ── SBAT-010 ── */

    @Test
    void runBatchMenuLoop_notAuthorised_showsMessageAndReturnsWithoutDisplayingMenu() {
        ws.setWkUserAuth("00000");

        invokePrivate("runBatchMenuLoop");

        assertEquals("Not authorised for batch/closing", ws.getWkMsgLine().trim());
        assertFalse(screenInteractions.contains("displayScreen:DS-MENU-BAT"));
    }

    @Test
    void runBatchMenuLoop_authorised_choiceZero_exitsWithoutCallingProgram() {
        ws.setWkUserAuth("00001");
        stubField("WK-CHOICE", "0");

        invokePrivate("runBatchMenuLoop");

        assertTrue(screenInteractions.contains("displayScreen:DS-MENU-BAT"));
        assertEquals(0, executeCallCount(bt0010Service));
    }

    @Test
    void runBatchMenuLoop_authorised_validChoice_callsMappedProgramThenExits() {
        ws.setWkUserAuth("00001");
        stubField("WK-CHOICE", "1", "0");

        invokePrivate("runBatchMenuLoop");

        assertEquals(1, executeCallCount(bt0010Service));
    }

    @Test
    void runBatchMenuLoop_ests03_exitsImmediately() {
        ws.setWkUserAuth("00001");
        stubEsts("03");

        invokePrivate("runBatchMenuLoop");

        assertEquals(0, executeCallCount(bt0010Service));
    }

    /* ── CPRG-010 ── */

    @Test
    void callSelectedProgram_blankWkCall_showsInvalidSelectionAndCallsNothing() {
        ws.setWkCall(" ");

        invokePrivate("callSelectedProgram");

        assertEquals("Invalid selection", ws.getWkMsgLine().trim());
    }

    @Test
    void callSelectedProgram_unknownCode_showsProgramNotAvailableYet() {
        // Ground truth EVALUATE WK-CALL WHEN OTHER branch — reachable if WK-CALL somehow
        // holds a code outside the known set (defensive branch; resolveProgramCode never
        // itself produces such a value, but the EVALUATE OTHER exists in the COBOL source).
        ws.setWkCall("ZZ9999");

        invokePrivate("callSelectedProgram");

        assertEquals("Program not available yet", ws.getWkMsgLine().trim());
    }

    @Test
    void callSelectedProgram_everyKnownCode_dispatchesToExactlyOneMatchingService() {
        Map<String, Object> codeToService = new LinkedHashMap<>();
        codeToService.put("MS0010", ms0010Service);
        codeToService.put("MS0020", ms0020Service);
        codeToService.put("MS0030", ms0030Service);
        codeToService.put("MS0040", ms0040Service);
        codeToService.put("MS0050", ms0050Service);
        codeToService.put("MS0060", ms0060Service);
        codeToService.put("MS0070", ms0070Service);
        codeToService.put("MS0080", ms0080Service);
        codeToService.put("MS0090", ms0090Service);
        codeToService.put("MS0100", ms0100Service);
        codeToService.put("MS0110", ms0110Service);
        codeToService.put("MS0120", ms0120Service);
        codeToService.put("OE0010", oe0010Service);
        codeToService.put("OE0020", oe0020Service);
        codeToService.put("OE0030", oe0030Service);
        codeToService.put("OE0040", oe0040Service);
        codeToService.put("OE0050", oe0050Service);
        codeToService.put("SH0010", sh0010Service);
        codeToService.put("SL0010", sl0010Service);
        codeToService.put("SL0020", sl0020Service);
        codeToService.put("SL0030", sl0030Service);
        codeToService.put("SL0040", sl0040Service);
        codeToService.put("PU0010", pu0010Service);
        codeToService.put("PU0020", pu0020Service);
        codeToService.put("PU0030", pu0030Service);
        codeToService.put("PU0040", pu0040Service);
        codeToService.put("RC0010", rc0010Service);
        codeToService.put("IV0010", iv0010Service);
        codeToService.put("IV0020", iv0020Service);
        codeToService.put("IV0030", iv0030Service);
        codeToService.put("IV0040", iv0040Service);
        codeToService.put("IV0050", iv0050Service);
        codeToService.put("AR0010", ar0010Service);
        codeToService.put("AR0020", ar0020Service);
        codeToService.put("AP0010", ap0010Service);
        codeToService.put("AP0020", ap0020Service);
        codeToService.put("RP0010", rp0010Service);
        codeToService.put("RP0020", rp0020Service);
        codeToService.put("RP0030", rp0030Service);
        codeToService.put("RP0040", rp0040Service);
        codeToService.put("RP0050", rp0050Service);
        codeToService.put("RP0060", rp0060Service);
        codeToService.put("RP0070", rp0070Service);
        codeToService.put("RP0080", rp0080Service);
        codeToService.put("RP0090", rp0090Service);
        codeToService.put("RP0100", rp0100Service);
        codeToService.put("RP0110", rp0110Service);
        codeToService.put("RP0120", rp0120Service);
        codeToService.put("RP0130", rp0130Service);
        codeToService.put("RP0140", rp0140Service);
        codeToService.put("BT0010", bt0010Service);
        codeToService.put("BT0020", bt0020Service);
        codeToService.put("BT0030", bt0030Service);
        codeToService.put("BT0040", bt0040Service);
        codeToService.put("BT0050", bt0050Service);
        codeToService.put("BT0060", bt0060Service);
        codeToService.put("BT0070", bt0070Service);
        codeToService.put("BT0080", bt0080Service);
        codeToService.put("BT0090", bt0090Service);

        for (Map.Entry<String, Object> entry : codeToService.entrySet()) {
            ws.setWkCall(entry.getKey());

            invokePrivate("callSelectedProgram");

            assertEquals(
                    1,
                    executeCallCount(entry.getValue()),
                    entry.getKey() + " should dispatch to exactly one execute() call");
        }
    }

    /* ── SOFF-010 ── */

    @Test
    void displaySignOffMessage_displaysHeaderAndSignOffMessage() {
        invokePrivate("displaySignOffMessage");

        assertTrue(screenInteractions.contains("displayScreen:DS-HEADER"));
        assertTrue(screenInteractions.contains("displayScreen:DS-MSG"));
        assertEquals("Signed off - thank you", ws.getWkMsgLine().trim());
    }

    /* ── MAIN-000 (full top-level flow) ── */

    @Test
    void runMainProgram_quitAtSignOn_skipsMenuLoopAndThrowsStopRunSignal() {
        stubEsts("03");

        assertThrows(StopRunSignal.class, () -> invokePrivate("runMainProgram"));

        assertEquals(1, ws.getEndFlg());
        assertEquals("Signed off - thank you", ws.getWkMsgLine().trim());
        assertFalse(screenInteractions.contains("displayScreen:DS-MAIN"));
    }

    @Test
    void runMainProgram_loginThenImmediateSignOff_runsOneMenuIterationThenThrowsStopRunSignal() {
        // Login screen ESTS=00 (not quit) then main-menu ACCEPT ESTS=00 with choice 0 to sign off.
        stubEsts("00", "00");
        stubChklog("00");
        stubField("WK-CHOICE", "0");

        assertThrows(StopRunSignal.class, () -> invokePrivate("runMainProgram"));

        assertEquals(1, ws.getEndFlg());
        assertTrue(screenInteractions.contains("displayScreen:DS-MAIN"));
        assertEquals("Signed off - thank you", ws.getWkMsgLine().trim());
    }

    /* ── execute() top-level integration (StopRunSignal caught by BatchServiceBase) ── */

    @Test
    void execute_quitAtSignOn_completesWithoutThrowingAndDefaultCompletionCode() {
        stubEsts("03");

        service.execute();

        assertEquals(0, service.getCompletionCode());
        assertEquals(1, ws.getEndFlg());
    }
}
