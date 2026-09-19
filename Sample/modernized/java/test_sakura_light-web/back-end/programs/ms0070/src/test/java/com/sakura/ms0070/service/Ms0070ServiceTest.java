package com.sakura.ms0070.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.ms0070.domain.Ms0070FieldAccess;
import com.sakura.ms0070.runtime.Ms0070Datasets;
import com.sakura.runtime.DatasetEnums.FileOpenMode;
import com.sakura.runtime.ProgramExitSignal;
import com.sakura.runtime.ScreenModels;
import com.sakura.runtime.ScreenRendererInstance;
import com.sakura.runtime.io.CprcfDataset;
import com.sakura.runtime.io.CustfDataset;
import com.sakura.runtime.io.ProdfDataset;
import com.sakura.runtime.linkage.DateutLinkParm;
import com.sakura.runtime.record.RawDatasetBase;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Unit tests for {@link Ms0070Service}, derived from COBOL program MS0070 (customer price master
 * maintenance). Ground truth for inputs/expected values: MS0070.cob PROCEDURE DIVISION.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class Ms0070ServiceTest {

    @Spy private CprcfDataset cprcfSpy = new CprcfDataset();
    @Spy private CustfDataset custfSpy = new CustfDataset();
    @Spy private ProdfDataset prodfSpy = new ProdfDataset();

    @Mock private DateutService dateutService;
    @Mock private AbortxService abortxService;
    @Mock private ScreenRendererInstance renderer;

    private Ms0070Service service;
    private Ms0070FieldAccess ws;

    private final List<String> screenInteractions = new ArrayList<>();
    private final Map<String, String> acceptFieldValues = new HashMap<>();

    /** Subclass swapping in the spy datasets since Ms0070Datasets builds its own real files. */
    private static class TestDatasets extends Ms0070Datasets {
        private final CprcfDataset cprcf;
        private final CustfDataset custf;
        private final ProdfDataset prodf;

        TestDatasets(CprcfDataset cprcf, CustfDataset custf, ProdfDataset prodf) {
            this.cprcf = cprcf;
            this.custf = custf;
            this.prodf = prodf;
        }

        @Override
        public CprcfDataset getCprcf() {
            return cprcf;
        }

        @Override
        public CustfDataset getCustf() {
            return custf;
        }

        @Override
        public ProdfDataset getProdf() {
            return prodf;
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        Ms0070Datasets fileSet = new TestDatasets(cprcfSpy, custfSpy, prodfSpy);
        service = new Ms0070Service(fileSet, dateutService, abortxService, renderer);
        service.setRenderer(renderer);
        ws = getWs();

        for (RawDatasetBase spy : new RawDatasetBase[] {cprcfSpy, custfSpy, prodfSpy}) {
            doNothing().when(spy).open(any());
            doNothing().when(spy).close();
            doNothing().when(spy).write();
            doNothing().when(spy).rewrite();
            doNothing().when(spy).setRecord();
            doReturn("00").when(spy).getFileStatus();
            doReturn(false).when(spy).isInvalidKey();
            doReturn(true).when(spy).readByKey(any());
        }

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
                            return acceptFieldValues.getOrDefault(field.name, "");
                        })
                .when(renderer)
                .acceptField(any());

        doReturn("00").when(renderer).readEndStatus();

        doAnswer(
                        inv -> {
                            DateutLinkParm p = inv.getArgument(0);
                            p.getKdate().setKdStatus("00");
                            p.getKdate().setKdDate1(20260918);
                            return null;
                        })
                .when(dateutService)
                .execute(any());

        doAnswer(inv -> null).when(abortxService).execute(any());
    }

    @AfterEach
    void tearDown() {
        screenInteractions.clear();
        acceptFieldValues.clear();
    }

    /* ── reflection helpers ── */

    private Ms0070FieldAccess getWs() throws Exception {
        Field f = Ms0070Service.class.getDeclaredField("ws");
        f.setAccessible(true);
        return (Ms0070FieldAccess) f.get(service);
    }

    private void invokePrivate(String name) {
        try {
            Method m = Ms0070Service.class.getDeclaredMethod(name);
            m.setAccessible(true);
            m.invoke(service);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException(cause);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private void invokeProtectedWithArg(String name, Class<?> paramType, Object arg) {
        try {
            Method m = Ms0070Service.class.getDeclaredMethod(name, paramType);
            m.setAccessible(true);
            m.invoke(service, arg);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    /* ── mainProcess / getCompletionCode / setCompletionCode / getFileSet ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void getCompletionCode_delegatesToWorkingStorage() {
        ws.setCompletionCode(42);
        assertEquals(42, service.getCompletionCode());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void setCompletionCode_delegatesToWorkingStorage() {
        invokeProtectedWithArg("setCompletionCode", int.class, 77);
        assertEquals(77, ws.getCompletionCode());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void getFileSet_returnsRegisteredDatasets() throws Exception {
        Method m = Ms0070Service.class.getDeclaredMethod("getFileSet");
        m.setAccessible(true);
        Object result = m.invoke(service);
        assertTrue(result instanceof Ms0070Datasets);
    }

    /* ── INIT-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeProgram_success_setsHeaderFieldsAndOpensAllFiles() {
        invokePrivate("initializeProgram");

        assertEquals("MS0070", ws.getWkProgid().trim());
        assertEquals("Customer Price Master Maintenance", ws.getWkTitle().trim());
        assertEquals("ENTER=Read  PF9=Delete  PF3=End", ws.getWkFkeyLine().trim());
        assertEquals(20260918, ws.getWkSysymd());
        assertEquals(20260918, ws.getWkSysdate());
        verify(cprcfSpy).open(FileOpenMode.IO);
        verify(custfSpy).open(FileOpenMode.INPUT);
        verify(prodfSpy).open(FileOpenMode.INPUT);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeProgram_cprcfStatus35_reopensAsOutputThenIO() {
        doReturn("35", "00", "00", "00").when(cprcfSpy).getFileStatus();

        invokePrivate("initializeProgram");

        verify(cprcfSpy, times(2)).open(FileOpenMode.IO);
        verify(cprcfSpy, times(1)).open(FileOpenMode.OUTPUT);
        verify(cprcfSpy, times(1)).close();
        verify(custfSpy).open(FileOpenMode.INPUT);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeProgram_cprcfStatus30_reopensAsOutputThenIO() {
        doReturn("30", "00", "00", "00").when(cprcfSpy).getFileStatus();

        invokePrivate("initializeProgram");

        verify(cprcfSpy, times(2)).open(FileOpenMode.IO);
        verify(cprcfSpy, times(1)).open(FileOpenMode.OUTPUT);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeProgram_cprcfErrorStatus_abortsAndSkipsCustfProdf() {
        doReturn("23").when(cprcfSpy).getFileStatus();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("initializeProgram"));

        assertEquals("CPRCF", ws.getKaFile().trim());
        assertEquals(255, ws.getCompletionCode());
        verify(abortxService).execute(any());
        verify(custfSpy, never()).open(any());
        verify(prodfSpy, never()).open(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeProgram_custfErrorStatus_abortsAndSkipsProdf() {
        doReturn("00").when(cprcfSpy).getFileStatus();
        doReturn("23").when(custfSpy).getFileStatus();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("initializeProgram"));

        assertEquals("CUSTF", ws.getKaFile().trim());
        assertEquals(255, ws.getCompletionCode());
        verify(abortxService).execute(any());
        verify(prodfSpy, never()).open(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeProgram_prodfErrorStatus_aborts() {
        doReturn("00").when(cprcfSpy).getFileStatus();
        doReturn("00").when(custfSpy).getFileStatus();
        doReturn("23").when(prodfSpy).getFileStatus();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("initializeProgram"));

        assertEquals("PRODF", ws.getKaFile().trim());
        assertEquals(255, ws.getCompletionCode());
        verify(abortxService).execute(any());
    }

    /* ── ABEND-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void abortOnFileOpenError_setsAbendFieldsAndCompletionCodeAndThrows() {
        ws.setFsts("23");

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("abortOnFileOpenError"));

        assertEquals("MS0070", ws.getKaProgid().trim());
        assertEquals("23", ws.getKaFsts().trim());
        assertEquals("EOPEN ", ws.getKaMsgcode());
        assertEquals(255, ws.getCompletionCode());
        verify(abortxService).execute(any());
    }

    /* ── MAIN-000 loop ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void runMainProgram_immediateEnd_closesAllFilesAndThrowsProgramExitSignal() {
        doReturn("03").when(renderer).readEndStatus();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("runMainProgram"));

        assertEquals(1, ws.getEndFlg());
        verify(cprcfSpy).close();
        verify(custfSpy).close();
        verify(prodfSpy).close();
    }

    /* ── MAIN-RTN-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processMainScreenInput_ests03_setsEndFlg() {
        doReturn("03").when(renderer).readEndStatus();

        invokePrivate("processMainScreenInput");

        assertEquals(1, ws.getEndFlg());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processMainScreenInput_estsOther_showsInvalidKeyMessage() {
        doReturn("07").when(renderer).readEndStatus();

        invokePrivate("processMainScreenInput");

        assertEquals(0, ws.getEndFlg());
        assertEquals("Invalid function key", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processMainScreenInput_ests00_custCodeZero_showsMustNotBeZeroMessage() {
        acceptFieldValues.put("CP-CUST", "0");
        doReturn("00").when(renderer).readEndStatus();

        invokePrivate("processMainScreenInput");

        assertEquals("Customer code must not be zero", ws.getWkMsgLine().trim());
        verify(custfSpy, never()).readByKey(any());
    }

    /* ── CLEAR-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void resetScreenAndWorkArea_resetsWorkFields() {
        ws.setWkCustName("dirty");
        ws.setWkProdName("dirty");
        ws.setWkConfirm("Y");
        ws.setWkSaveCust(999);
        ws.setWkSaveProd(999);

        invokePrivate("resetScreenAndWorkArea");

        verify(cprcfSpy).setRecord();
        assertEquals("", ws.getWkCustName().trim());
        assertEquals("", ws.getWkProdName().trim());
        assertEquals("", ws.getWkConfirm().trim());
        assertEquals(0, ws.getWkSaveCust());
        assertEquals(0, ws.getWkSaveProd());
    }

    /* ── PKEY-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateKeysAndLoadRecords_cpCustZero_showsMessageAndReturnsWithoutRead() {
        ws.setCpCust(0);
        ws.setCpProd(100);

        invokePrivate("validateKeysAndLoadRecords");

        assertEquals("Customer code must not be zero", ws.getWkMsgLine().trim());
        verify(custfSpy, never()).readByKey(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateKeysAndLoadRecords_cpProdZero_showsMessageAndReturnsWithoutRead() {
        ws.setCpCust(100);
        ws.setCpProd(0);

        invokePrivate("validateKeysAndLoadRecords");

        assertEquals("Product code must not be zero", ws.getWkMsgLine().trim());
        verify(custfSpy, never()).readByKey(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateKeysAndLoadRecords_customerNotFound_showsMessage() {
        ws.setCpCust(100);
        ws.setCpProd(200);
        doReturn(true).when(custfSpy).isInvalidKey();

        invokePrivate("validateKeysAndLoadRecords");

        assertEquals("Customer code not found", ws.getWkMsgLine().trim());
        verify(prodfSpy, never()).readByKey(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateKeysAndLoadRecords_customerDeleted_showsMessage() {
        ws.setCpCust(100);
        ws.setCpProd(200);
        doReturn(false).when(custfSpy).isInvalidKey();
        ws.setCuDelFlag(1);

        invokePrivate("validateKeysAndLoadRecords");

        assertEquals("Customer is deleted", ws.getWkMsgLine().trim());
        verify(prodfSpy, never()).readByKey(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateKeysAndLoadRecords_productNotFound_showsMessage() {
        ws.setCpCust(100);
        ws.setCpProd(200);
        doReturn(false).when(custfSpy).isInvalidKey();
        ws.setCuDelFlag(0);
        ws.setCuName("Acme Corp");
        doReturn(true).when(prodfSpy).isInvalidKey();

        invokePrivate("validateKeysAndLoadRecords");

        assertEquals("Product code not found", ws.getWkMsgLine().trim());
        assertEquals("Acme Corp", ws.getWkCustName().trim());
        verify(cprcfSpy, never()).readByKey(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateKeysAndLoadRecords_productDeleted_showsMessage() {
        ws.setCpCust(100);
        ws.setCpProd(200);
        doReturn(false).when(custfSpy).isInvalidKey();
        ws.setCuDelFlag(0);
        doReturn(false).when(prodfSpy).isInvalidKey();
        ws.setPrDelFlag(1);

        invokePrivate("validateKeysAndLoadRecords");

        assertEquals("Product is deleted", ws.getWkMsgLine().trim());
        verify(cprcfSpy, never()).readByKey(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateKeysAndLoadRecords_cprcfInvalidKey_setsUpAddModeAndAccepts() {
        ws.setCpCust(100);
        ws.setCpProd(200);
        doReturn(false).when(custfSpy).isInvalidKey();
        ws.setCuDelFlag(0);
        ws.setCuName("Acme Corp");
        doReturn(false).when(prodfSpy).isInvalidKey();
        ws.setPrDelFlag(0);
        ws.setPrName("Widget");
        doReturn(true).when(cprcfSpy).isInvalidKey();
        doReturn("03").when(renderer).readEndStatus();

        invokePrivate("validateKeysAndLoadRecords");

        assertEquals(1, ws.getModeFlg());
        assertEquals(100, ws.getWkSaveCust());
        assertEquals(200, ws.getWkSaveProd());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateKeysAndLoadRecords_cprcfValidKey_setsUpChangeMode() {
        ws.setCpCust(100);
        ws.setCpProd(200);
        doReturn(false).when(custfSpy).isInvalidKey();
        ws.setCuDelFlag(0);
        ws.setCuName("Acme Corp");
        doReturn(false).when(prodfSpy).isInvalidKey();
        ws.setPrDelFlag(0);
        ws.setPrName("Widget");
        doReturn(false).when(cprcfSpy).isInvalidKey();
        ws.setCpDelFlag(0);
        doReturn("03").when(renderer).readEndStatus();

        invokePrivate("validateKeysAndLoadRecords");

        assertEquals(2, ws.getModeFlg());
    }

    /* ── SADD-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void prepareNewContractPrice_setsModeAddKeysAndMessage() {
        ws.setWkSaveCust(555);
        ws.setWkSaveProd(777);
        ws.setWkSysdate(20260918);

        invokePrivate("prepareNewContractPrice");

        assertEquals(1, ws.getModeFlg());
        assertEquals(555, ws.getCpCust());
        assertEquals(777, ws.getCpProd());
        assertEquals(20260918, ws.getCpStartDate());
        assertEquals("New contract price - enter details", ws.getWkMsgLine().trim());
        verify(cprcfSpy).setRecord();
    }

    /* ── SCHG-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void prepareContractPriceChange_deletedPrice_setsModeAddAndReregisterMessage() {
        ws.setCpDelFlag(1);
        doReturn(true).when(custfSpy).isInvalidKey();
        doReturn(true).when(prodfSpy).isInvalidKey();

        invokePrivate("prepareContractPriceChange");

        assertEquals(1, ws.getModeFlg());
        assertEquals("Deleted price - re-registering", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void prepareContractPriceChange_notDeleted_setsModeChgAndChangeMessage() {
        ws.setCpDelFlag(0);
        doReturn(true).when(custfSpy).isInvalidKey();
        doReturn(true).when(prodfSpy).isInvalidKey();

        invokePrivate("prepareContractPriceChange");

        assertEquals(2, ws.getModeFlg());
        assertEquals("Existing price - change or PF9 delete", ws.getWkMsgLine().trim());
    }

    /* ── EDIT-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void acceptAndDispatchPriceEdit_ests03_acceptsFieldsAndContinues() {
        acceptFieldValues.put("CP-PRICE", "100.00");
        doReturn("03").when(renderer).readEndStatus();

        invokePrivate("acceptAndDispatchPriceEdit");

        assertEquals(new BigDecimal("100.00"), ws.getCpPrice());
        assertFalse(
                screenInteractions.contains("displayScreen:DS-MSG")
                        && ws.getWkMsgLine().trim().equals("Cancelled"));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void acceptAndDispatchPriceEdit_ests04_showsCancelledMessage() {
        doReturn("04").when(renderer).readEndStatus();

        invokePrivate("acceptAndDispatchPriceEdit");

        assertEquals("Cancelled", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void acceptAndDispatchPriceEdit_ests09_modeChg_callsDeleteWithConfirm() {
        ws.setModeFlg(2);
        acceptFieldValues.put("SC-CONF", "N");
        doReturn("09").when(renderer).readEndStatus();

        invokePrivate("acceptAndDispatchPriceEdit");

        assertEquals("Delete cancelled", ws.getWkMsgLine().trim());
        assertTrue(screenInteractions.contains("displayScreen:DS-CONFIRM"));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void acceptAndDispatchPriceEdit_ests09_modeAdd_showsNothingToDeleteMessage() {
        ws.setModeFlg(1);
        doReturn("09").when(renderer).readEndStatus();

        invokePrivate("acceptAndDispatchPriceEdit");

        assertEquals("Nothing to delete", ws.getWkMsgLine().trim());
        assertFalse(screenInteractions.contains("displayScreen:DS-CONFIRM"));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void acceptAndDispatchPriceEdit_ests00_validationFails_doesNotSave() {
        acceptFieldValues.put("CP-PRICE", "-5.00");
        doReturn("00").when(renderer).readEndStatus();

        invokePrivate("acceptAndDispatchPriceEdit");

        assertEquals(1, ws.getErrFlg());
        verify(cprcfSpy, never()).write();
        verify(cprcfSpy, never()).rewrite();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void acceptAndDispatchPriceEdit_ests00_validationPasses_savesAddedPrice() {
        ws.setModeFlg(1);
        acceptFieldValues.put("CP-PRICE", "100.00");
        acceptFieldValues.put("CP-START-DATE", "20260101");
        doReturn("00").when(renderer).readEndStatus();

        invokePrivate("acceptAndDispatchPriceEdit");

        assertEquals(0, ws.getErrFlg());
        verify(cprcfSpy).write();
        assertEquals("Contract price added", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void acceptAndDispatchPriceEdit_estsOther_showsInvalidKeyMessage() {
        doReturn("07").when(renderer).readEndStatus();

        invokePrivate("acceptAndDispatchPriceEdit");

        assertEquals("Invalid function key", ws.getWkMsgLine().trim());
    }

    /* ── VAL-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validatePriceAndDates_priceNegative_setsErrorAndReturns() {
        ws.setCpPrice(new BigDecimal("-1.00"));

        invokePrivate("validatePriceAndDates");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Price cannot be negative", ws.getWkMsgLine().trim());
        assertTrue(screenInteractions.contains("displayScreen:DS-MSG"));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validatePriceAndDates_startDateZero_setsErrorAndReturns() {
        ws.setCpPrice(BigDecimal.ZERO);
        ws.setCpStartDate(0);

        invokePrivate("validatePriceAndDates");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Start date is required", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validatePriceAndDates_startDateInvalid_setsError() {
        ws.setCpPrice(BigDecimal.ZERO);
        ws.setCpStartDate(20260101);
        doAnswer(
                        inv -> {
                            DateutLinkParm p = inv.getArgument(0);
                            p.getKdate().setKdStatus("99");
                            return null;
                        })
                .when(dateutService)
                .execute(any());

        invokePrivate("validatePriceAndDates");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Start date is invalid", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validatePriceAndDates_endDateInvalid_setsError() {
        ws.setCpPrice(BigDecimal.ZERO);
        ws.setCpStartDate(20260101);
        ws.setCpEndDate(20260201);
        doAnswer(
                        inv -> {
                            DateutLinkParm p = inv.getArgument(0);
                            int date1 = p.getKdate().getKdDate1();
                            p.getKdate().setKdStatus(date1 == 20260201 ? "99" : "00");
                            return null;
                        })
                .when(dateutService)
                .execute(any());

        invokePrivate("validatePriceAndDates");

        assertEquals(1, ws.getErrFlg());
        assertEquals("End date is invalid", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validatePriceAndDates_endDateBeforeStartDate_setsError() {
        ws.setCpPrice(BigDecimal.ZERO);
        ws.setCpStartDate(20260201);
        ws.setCpEndDate(20260101);

        invokePrivate("validatePriceAndDates");

        assertEquals(1, ws.getErrFlg());
        assertEquals("End date is before start date", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validatePriceAndDates_allValid_noErrorAndOpenEndDateAllowed() {
        ws.setCpPrice(BigDecimal.ZERO);
        ws.setCpStartDate(20260101);
        ws.setCpEndDate(0);

        invokePrivate("validatePriceAndDates");

        assertEquals(0, ws.getErrFlg());
    }

    /* ── VAL-999 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void displayValidationErrorIfAny_errFlgSet_displaysMessage() {
        ws.setErrFlg(1);

        invokePrivate("displayValidationErrorIfAny");

        assertTrue(screenInteractions.contains("displayScreen:DS-MSG"));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void displayValidationErrorIfAny_errFlgNotSet_noDisplay() {
        ws.setErrFlg(0);

        invokePrivate("displayValidationErrorIfAny");

        assertFalse(screenInteractions.contains("displayScreen:DS-MSG"));
    }

    /* ── SAVE-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void saveContractPrice_modeAdd_writeSuccess_showsAddedMessage() {
        ws.setModeFlg(1);

        invokePrivate("saveContractPrice");

        assertEquals(0, ws.getCpDelFlag());
        verify(cprcfSpy).write();
        assertEquals("Contract price added", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void saveContractPrice_modeAdd_writeInvalidKey_showsDuplicateMessage() {
        ws.setModeFlg(1);
        doReturn(true).when(cprcfSpy).isInvalidKey();

        invokePrivate("saveContractPrice");

        assertEquals("Write failed - duplicate", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void saveContractPrice_modeChg_rewriteSuccess_showsUpdatedMessage() {
        ws.setModeFlg(2);

        invokePrivate("saveContractPrice");

        verify(cprcfSpy).rewrite();
        assertEquals("Contract price updated", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void saveContractPrice_modeChg_rewriteInvalidKey_showsUpdateFailedMessage() {
        ws.setModeFlg(2);
        doReturn(true).when(cprcfSpy).isInvalidKey();

        invokePrivate("saveContractPrice");

        assertEquals("Update failed", ws.getWkMsgLine().trim());
    }

    /* ── DEL-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void confirmAndDeleteContractPrice_confirmY_deletesAndRewrites() {
        acceptFieldValues.put("SC-CONF", "Y");

        invokePrivate("confirmAndDeleteContractPrice");

        assertEquals(1, ws.getCpDelFlag());
        verify(cprcfSpy).rewrite();
        assertEquals("Contract price deleted", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void confirmAndDeleteContractPrice_confirmLowercaseY_deletesAndRewrites() {
        acceptFieldValues.put("SC-CONF", "y");

        invokePrivate("confirmAndDeleteContractPrice");

        assertEquals(1, ws.getCpDelFlag());
        verify(cprcfSpy).rewrite();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void confirmAndDeleteContractPrice_confirmN_cancelsWithoutRewrite() {
        acceptFieldValues.put("SC-CONF", "N");

        invokePrivate("confirmAndDeleteContractPrice");

        assertEquals("Delete cancelled", ws.getWkMsgLine().trim());
        verify(cprcfSpy, never()).rewrite();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void confirmAndDeleteContractPrice_confirmYRewriteInvalidKey_showsDeleteFailed() {
        acceptFieldValues.put("SC-CONF", "Y");
        doReturn(true).when(cprcfSpy).isInvalidKey();

        invokePrivate("confirmAndDeleteContractPrice");

        assertEquals("Delete failed", ws.getWkMsgLine().trim());
    }

    /* ── LOOK-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void refreshCustomerAndProductNames_bothFound_setsBothNames() {
        ws.setCpCust(100);
        ws.setCpProd(200);
        doReturn(false).when(custfSpy).isInvalidKey();
        ws.setCuName("Acme Corp");
        doReturn(false).when(prodfSpy).isInvalidKey();
        ws.setPrName("Widget");

        invokePrivate("refreshCustomerAndProductNames");

        assertEquals("Acme Corp", ws.getWkCustName().trim());
        assertEquals("Widget", ws.getWkProdName().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void refreshCustomerAndProductNames_notFound_setsUnknownNames() {
        ws.setCpCust(100);
        ws.setCpProd(200);
        doReturn(true).when(custfSpy).isInvalidKey();
        doReturn(true).when(prodfSpy).isInvalidKey();

        invokePrivate("refreshCustomerAndProductNames");

        assertEquals("??? unknown customer", ws.getWkCustName().trim());
        assertEquals("??? unknown product", ws.getWkProdName().trim());
    }

    /* ── TERM-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void closeAllFiles_closesAllThreeFiles() {
        invokePrivate("closeAllFiles");

        verify(cprcfSpy).close();
        verify(custfSpy).close();
        verify(prodfSpy).close();
    }
}
