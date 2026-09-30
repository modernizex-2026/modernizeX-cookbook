package com.sakura.ap0010.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import com.sakura.ap0010.domain.Ap0010FieldAccess;
import com.sakura.ap0010.io.PayfDataset;
import com.sakura.ap0010.runtime.Ap0010Datasets;
import com.sakura.dateut.service.DateutService;
import com.sakura.numgen.service.NumgenService;
import com.sakura.runtime.DatasetEnums.FileOpenMode;
import com.sakura.runtime.ProgramExitSignal;
import com.sakura.runtime.ScreenModels;
import com.sakura.runtime.ScreenRendererInstance;
import com.sakura.runtime.io.AplfDataset;
import com.sakura.runtime.io.BankfDataset;
import com.sakura.runtime.io.SuppfDataset;
import com.sakura.runtime.linkage.DateutLinkParm;
import com.sakura.runtime.linkage.NumgenLinkParm;

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
 * Unit tests for {@link Ap0010Service}, derived from COBOL program AP0010 (payment entry). Ground
 * truth for inputs/expected values: AP0010.cob PROCEDURE DIVISION.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class Ap0010ServiceTest {

    @Spy private PayfDataset payfSpy = new PayfDataset();
    @Spy private AplfDataset aplfSpy = new AplfDataset();
    @Spy private SuppfDataset suppfSpy = new SuppfDataset();
    @Spy private BankfDataset bankfSpy = new BankfDataset();

    @Mock private DateutService dateutService;
    @Mock private NumgenService numgenService;
    @Mock private AbortxService abortxService;
    @Mock private ScreenRendererInstance renderer;

    private Ap0010Service service;
    private Ap0010FieldAccess ws;

    private final List<String> screenInteractions = new ArrayList<>();
    private final Map<String, String> acceptFieldValues = new HashMap<>();

    /** Subclass swapping in the spy datasets since Ap0010Datasets builds its own real files. */
    private static class TestDatasets extends Ap0010Datasets {
        private final PayfDataset payf;
        private final AplfDataset aplf;
        private final SuppfDataset suppf;
        private final BankfDataset bankf;

        TestDatasets(PayfDataset payf, AplfDataset aplf, SuppfDataset suppf, BankfDataset bankf) {
            this.payf = payf;
            this.aplf = aplf;
            this.suppf = suppf;
            this.bankf = bankf;
        }

        @Override
        public PayfDataset getPayf() {
            return payf;
        }

        @Override
        public AplfDataset getAplf() {
            return aplf;
        }

        @Override
        public SuppfDataset getSuppf() {
            return suppf;
        }

        @Override
        public BankfDataset getBankf() {
            return bankf;
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        Ap0010Datasets fileSet = new TestDatasets(payfSpy, aplfSpy, suppfSpy, bankfSpy);
        service = new Ap0010Service(fileSet, dateutService, numgenService, abortxService, renderer);
        service.setRenderer(renderer);
        ws = getWs();

        for (var spy :
                new com.sakura.runtime.record.RawDatasetBase[] {
                    payfSpy, aplfSpy, suppfSpy, bankfSpy
                }) {
            doNothing().when(spy).open(any());
            doNothing().when(spy).close();
            doNothing().when(spy).write();
            doNothing().when(spy).rewrite();
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

        doAnswer(
                        inv -> {
                            NumgenLinkParm p = inv.getArgument(0);
                            p.getKnum().setKnumStatus("00");
                            p.getKnum().setKnumNumber(1001L);
                            return null;
                        })
                .when(numgenService)
                .execute(any());
    }

    @AfterEach
    void tearDown() {
        screenInteractions.clear();
        acceptFieldValues.clear();
    }

    /* ── reflection helpers ── */

    private Ap0010FieldAccess getWs() throws Exception {
        Field f = Ap0010Service.class.getDeclaredField("ws");
        f.setAccessible(true);
        return (Ap0010FieldAccess) f.get(service);
    }

    private void invokePrivate(String name) {
        try {
            Method m = Ap0010Service.class.getDeclaredMethod(name);
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

    /* ── INIT-010 / OPENF-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeProgram_setsHeaderFieldsAndSysDate_success() {
        invokePrivate("initializeProgram");

        assertEquals("AP0010", ws.getWkProgid());
        assertEquals("Payment Entry", ws.getWkTitle().trim());
        assertEquals("ENTER=Confirm  PF3=End  PF4=Clear", ws.getWkFkeyLine().trim());
        assertEquals(20260918, ws.getWkSysymd());
        assertEquals(20260918, ws.getWkSysdate());
        verify(payfSpy).open(FileOpenMode.IO);
        verify(aplfSpy).open(FileOpenMode.IO);
        verify(suppfSpy).open(FileOpenMode.IO);
        verify(bankfSpy).open(FileOpenMode.INPUT);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void openAllFiles_allStatus00_opensAllFourFilesWithoutAbort() {
        invokePrivate("openAllFiles");

        verify(payfSpy, times(1)).open(FileOpenMode.IO);
        verify(aplfSpy, times(1)).open(FileOpenMode.IO);
        verify(suppfSpy, times(1)).open(FileOpenMode.IO);
        verify(bankfSpy, times(1)).open(FileOpenMode.INPUT);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void openAllFiles_payfStatus35_reopensAsOutputThenIO() {
        doReturn("35", "00", "00", "00").when(payfSpy).getFileStatus();

        invokePrivate("openAllFiles");

        verify(payfSpy, times(2)).open(FileOpenMode.IO);
        verify(payfSpy, times(1)).open(FileOpenMode.OUTPUT);
        verify(payfSpy, times(1)).close();
        verify(aplfSpy, times(1)).open(FileOpenMode.IO);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void openAllFiles_payfErrorStatus_abortsAndSkipsRemainingFiles() {
        doReturn("23").when(payfSpy).getFileStatus();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("openAllFiles"));

        assertEquals("PAYF", ws.getKaFile().trim());
        assertEquals(255, ws.getCompletionCode());
        verify(abortxService).execute(any());
        verify(aplfSpy, never()).open(any());
        verify(suppfSpy, never()).open(any());
        verify(bankfSpy, never()).open(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void abortOnFileOpenError_setsAbendFieldsAndCompletionCodeAndThrows() {
        ws.setFsts("23");

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("abortOnFileOpenError"));

        assertEquals("AP0010", ws.getKaProgid());
        assertEquals("23", ws.getKaFsts());
        assertEquals("EOPEN ", ws.getKaMsgcode());
        assertEquals(255, ws.getCompletionCode());
        verify(abortxService).execute(any());
    }

    /* ── MAIN-000 loop ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void runMainProgram_endFlgOnFirstEntry_closesFilesAndThrowsProgramExitSignal() {
        acceptFieldValues.put("PY-DATE", "20260101");
        doReturn("03").when(renderer).readEndStatus();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("runMainProgram"));

        assertEquals(1, ws.getEndFlg());
        verify(payfSpy).close();
        verify(aplfSpy).close();
        verify(suppfSpy).close();
        verify(bankfSpy).close();
    }

    /* ── PLUP-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processOnePaymentEntry_endFlgSetByHeader_doesNotCallConfirm() {
        doReturn("03").when(renderer).readEndStatus();

        invokePrivate("processOnePaymentEntry");

        assertEquals(1, ws.getEndFlg());
        verify(renderer, never()).acceptField(argThatNameEquals("SC-CONF"));
    }

    private ScreenModels.InputFieldDef argThatNameEquals(String name) {
        return org.mockito.ArgumentMatchers.argThat(f -> f != null && name.equals(f.name));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processOnePaymentEntry_hdrOk_callsConfirmAndSavePayment() {
        setupValidSupplierRecord();
        acceptFieldValues.put("PY-SUPP", "500");
        acceptFieldValues.put("PY-METHOD", "1");
        acceptFieldValues.put("PY-AMOUNT", "1000");
        acceptFieldValues.put("PY-BANK-CODE", "0");
        acceptFieldValues.put("PY-DATE", "20260101");
        acceptFieldValues.put("SC-CONF", "N");

        invokePrivate("processOnePaymentEntry");

        assertEquals(1, ws.getHdrOk());
        assertTrue(screenInteractions.contains("displayScreen:DS-CONFIRM"));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processOnePaymentEntry_hdrNotOk_neverDisplaysConfirmScreen() {
        acceptFieldValues.put("PY-DATE", "20260101");
        acceptFieldValues.put("PY-SUPP", "0");

        invokePrivate("processOnePaymentEntry");

        assertEquals(0, ws.getHdrOk());
        assertTrue(
                screenInteractions.stream().noneMatch(s -> s.equals("displayScreen:DS-CONFIRM")));
    }

    /* ── CLR-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void clearEntryFields_resetsWorkFieldsToDefaults() {
        ws.setWkSysdate(20260101);
        ws.setHdrOk(1);
        ws.setWkCurBal(999);
        ws.setWkNewBal(999);
        ws.setPyMethod(4);

        invokePrivate("clearEntryFields");

        assertEquals(0, ws.getHdrOk());
        assertEquals(0, ws.getWkCurBal());
        assertEquals(0, ws.getWkNewBal());
        assertEquals(0, ws.getWkPyNoD());
        assertEquals(0, ws.getWkCloseYm());
        assertEquals(20260101, ws.getPyDate());
        assertEquals(1, ws.getPyMethod());
    }

    /* ── EHDR-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void acceptPaymentHeaderFields_ests03_setsEndFlgAndSkipsValidation() {
        acceptFieldValues.put("PY-DATE", "20260101");
        doReturn("03").when(renderer).readEndStatus();

        invokePrivate("acceptPaymentHeaderFields");

        assertEquals(1, ws.getEndFlg());
        assertEquals(0, ws.getHdrOk());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void acceptPaymentHeaderFields_ests04_showsClearedMessageAndSkipsValidation() {
        acceptFieldValues.put("PY-DATE", "20260101");
        doReturn("04").when(renderer).readEndStatus();

        invokePrivate("acceptPaymentHeaderFields");

        assertEquals("Cleared", ws.getWkMsgLine().trim());
        assertEquals(0, ws.getHdrOk());
        assertEquals(0, ws.getEndFlg());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void acceptPaymentHeaderFields_normalEnter_readsFieldsAndValidates() {
        setupValidSupplierRecord();
        acceptFieldValues.put("PY-DATE", "20260101");
        acceptFieldValues.put("PY-SUPP", "500");
        acceptFieldValues.put("PY-METHOD", "1");
        acceptFieldValues.put("PY-AMOUNT", "2500");
        acceptFieldValues.put("PY-BANK-CODE", "0");
        acceptFieldValues.put("PY-REMARK", "Invoice 42");
        doReturn("00").when(renderer).readEndStatus();

        invokePrivate("acceptPaymentHeaderFields");

        assertEquals(20260101, ws.getPyDate());
        assertEquals(500, ws.getPySupp());
        assertEquals(1, ws.getPyMethod());
        assertEquals(new BigDecimal("2500"), ws.getPyAmount());
        assertEquals("Invoice 42", ws.getPyRemark().trim());
        assertEquals(1, ws.getHdrOk());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void acceptPaymentHeaderFields_nonNumericAmount_defaultsToZero() {
        setupValidSupplierRecord();
        acceptFieldValues.put("PY-DATE", "20260101");
        acceptFieldValues.put("PY-SUPP", "500");
        acceptFieldValues.put("PY-METHOD", "1");
        acceptFieldValues.put("PY-AMOUNT", "abc");
        doReturn("00").when(renderer).readEndStatus();

        invokePrivate("acceptPaymentHeaderFields");

        assertEquals(BigDecimal.ZERO, ws.getPyAmount());
        assertEquals(0, ws.getHdrOk());
        assertEquals("Amount must be positive", ws.getWkMsgLine().trim());
    }

    /* ── VHDR-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validatePaymentHeader_invalidDate_setsErrorAndDisplaysMessage() {
        doAnswer(
                        inv -> {
                            DateutLinkParm p = inv.getArgument(0);
                            p.getKdate().setKdStatus("99");
                            return null;
                        })
                .when(dateutService)
                .execute(any());
        ws.setPyDate(0);

        invokePrivate("validatePaymentHeader");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Payment date is invalid", ws.getWkMsgLine().trim());
        assertEquals(0, ws.getHdrOk());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validatePaymentHeader_supplierCodeZero_setsError() {
        ws.setPyDate(20260101);
        ws.setPySupp(0);

        invokePrivate("validatePaymentHeader");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Supplier code required", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validatePaymentHeader_supplierNotFound_setsError() {
        ws.setPyDate(20260101);
        ws.setPySupp(999);
        doReturn(true).when(suppfSpy).isInvalidKey();

        invokePrivate("validatePaymentHeader");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Supplier not found", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validatePaymentHeader_supplierDeleted_setsError() {
        ws.setPyDate(20260101);
        ws.setPySupp(500);
        ws.setSpDelFlag(1);

        invokePrivate("validatePaymentHeader");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Supplier is deleted", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validatePaymentHeader_amountNotPositive_setsError() {
        setupValidSupplierRecord();
        ws.setPyDate(20260101);
        ws.setPySupp(500);
        ws.setPyAmount(BigDecimal.ZERO);

        invokePrivate("validatePaymentHeader");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Amount must be positive", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validatePaymentHeader_methodOutOfRange_setsError() {
        setupValidSupplierRecord();
        ws.setPyDate(20260101);
        ws.setPySupp(500);
        ws.setPyAmount(new BigDecimal("100"));
        ws.setPyMethod(5);

        invokePrivate("validatePaymentHeader");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Method must be 1 to 4", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validatePaymentHeader_bankValidationFails_propagatesErrorAndRedisplays() {
        setupValidSupplierRecord();
        ws.setPyDate(20260101);
        ws.setPySupp(500);
        ws.setPyAmount(new BigDecimal("100"));
        ws.setPyMethod(2);
        ws.setPyBankCode(0);

        invokePrivate("validatePaymentHeader");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Bank code required for transfer", ws.getWkMsgLine().trim());
        assertEquals(0, ws.getHdrOk());
        assertTrue(screenInteractions.contains("displayScreen:DS-MSG"));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validatePaymentHeader_success_setsHdrOkAndComputesNewBalance() {
        setupValidSupplierRecord();
        ws.setSpBalance(new BigDecimal("10000"));
        ws.setPyDate(20260101);
        ws.setPySupp(500);
        ws.setPyAmount(new BigDecimal("1500"));
        ws.setPyMethod(1);
        ws.setPyBankCode(0);

        invokePrivate("validatePaymentHeader");

        assertEquals(0, ws.getErrFlg());
        assertEquals(1, ws.getHdrOk());
        assertEquals(8500L, ws.getWkNewBal());
        assertEquals("Details OK - press ENTER to confirm", ws.getWkMsgLine().trim());
    }

    /* ── VBNK-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateBankCode_zeroCodeMethodTransfer_setsError() {
        ws.setPyBankCode(0);
        ws.setPyMethod(2);

        invokePrivate("validateBankCode");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Bank code required for transfer", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateBankCode_zeroCodeMethodNotTransfer_noError() {
        ws.setPyBankCode(0);
        ws.setPyMethod(1);

        invokePrivate("validateBankCode");

        assertEquals(0, ws.getErrFlg());
        assertEquals(" ", ws.getWkBankName().trim().isEmpty() ? " " : ws.getWkBankName());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateBankCode_bankNotFound_setsError() {
        ws.setPyBankCode(10);
        doReturn(true).when(bankfSpy).isInvalidKey();

        invokePrivate("validateBankCode");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Bank code not found", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateBankCode_bankDeleted_setsError() {
        ws.setPyBankCode(10);
        ws.setBkDelFlag(1);

        invokePrivate("validateBankCode");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Bank is deleted", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateBankCode_success_setsBankName() {
        ws.setPyBankCode(10);
        ws.setBkDelFlag(0);
        ws.setBkName("Bank of Sakura");

        invokePrivate("validateBankCode");

        assertEquals(0, ws.getErrFlg());
        assertEquals("Bank of Sakura", ws.getWkBankName().trim());
    }

    /* ── CSAV-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void confirmAndSavePayment_confirmY_savesPayment() {
        setupValidSupplierRecord();
        ws.setPySupp(500);
        ws.setPyDate(20260101);
        ws.setPyAmount(new BigDecimal("100"));
        acceptFieldValues.put("SC-CONF", "Y");

        invokePrivate("confirmAndSavePayment");

        verify(payfSpy).write();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void confirmAndSavePayment_confirmLowercaseY_savesPayment() {
        setupValidSupplierRecord();
        ws.setPySupp(500);
        ws.setPyDate(20260101);
        ws.setPyAmount(new BigDecimal("100"));
        acceptFieldValues.put("SC-CONF", "y");

        invokePrivate("confirmAndSavePayment");

        verify(payfSpy).write();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void confirmAndSavePayment_confirmN_discardsPaymentWithoutWrite() {
        acceptFieldValues.put("SC-CONF", "N");

        invokePrivate("confirmAndSavePayment");

        assertEquals("Payment discarded", ws.getWkMsgLine().trim());
        verify(payfSpy, never()).write();
    }

    /* ── SAV-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void savePaymentRecord_numgenAssignmentFails_showsMessageAndReturns() {
        doAnswer(
                        inv -> {
                            NumgenLinkParm p = inv.getArgument(0);
                            p.getKnum().setKnumStatus("XX");
                            return null;
                        })
                .when(numgenService)
                .execute(any());

        invokePrivate("savePaymentRecord");

        assertEquals("Payment number assignment failed", ws.getWkMsgLine().trim());
        verify(payfSpy, never()).write();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void savePaymentRecord_writeInvalidKey_showsMessageAndSkipsLedgerAndBalance() {
        doReturn(true).when(payfSpy).isInvalidKey();

        invokePrivate("savePaymentRecord");

        assertEquals("Payment write failed", ws.getWkMsgLine().trim());
        verify(aplfSpy, never()).write();
        verify(suppfSpy, never()).rewrite();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void savePaymentRecord_success_writesLedgerUpdatesBalanceAndIncrementsSessionCounters() {
        setupValidSupplierRecord();
        ws.setPySupp(500);
        ws.setPyDate(20260315);
        ws.setPyAmount(new BigDecimal("1000"));
        ws.setWkUserCode(42);
        ws.setWkSysdate(20260315);
        ws.setWkSessCnt(3);
        ws.setWkSessAmt(5000);

        invokePrivate("savePaymentRecord");

        assertEquals(1001L, ws.getPyNo());
        assertEquals(1001L, ws.getWkPyNoD());
        assertEquals(202603, ws.getWkCloseYm());
        assertEquals(202603, ws.getPyCloseYm());
        assertEquals(0, ws.getPyStatus());
        assertEquals(0, ws.getPyDelFlag());
        assertEquals(20260315, ws.getPyAddDate());
        assertEquals(42, ws.getPyAddUser());
        assertEquals(4, ws.getWkSessCnt());
        assertEquals(6000L, ws.getWkSessAmt());
        assertEquals("Payment posted", ws.getWkMsgLine().trim());
        verify(payfSpy).write();
        verify(aplfSpy).write();
        verify(suppfSpy).rewrite();
    }

    /* ── USB-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void updateSupplierBalance_invalidKey_showsMessageWithoutRewrite() {
        doReturn(true).when(suppfSpy).isInvalidKey();

        invokePrivate("updateSupplierBalance");

        assertEquals("Supplier balance update failed", ws.getWkMsgLine().trim());
        verify(suppfSpy, never()).rewrite();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void updateSupplierBalance_success_subtractsAmountAndRewrites() {
        ws.setPySupp(500);
        ws.setPyAmount(new BigDecimal("300"));
        ws.setSpBalance(new BigDecimal("1000"));
        ws.setWkUserCode(7);
        ws.setWkSysdate(20260315);

        invokePrivate("updateSupplierBalance");

        assertEquals(new BigDecimal("700"), ws.getSpBalance());
        assertEquals(20260315, ws.getSpUpdDate());
        assertEquals(7, ws.getSpUpdUser());
        verify(suppfSpy).rewrite();
    }

    /* ── PLG-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void writeLedgerEntry_numgenAssignmentFails_showsMessageAndSkipsWrite() {
        doAnswer(
                        inv -> {
                            NumgenLinkParm p = inv.getArgument(0);
                            p.getKnum().setKnumStatus("XX");
                            return null;
                        })
                .when(numgenService)
                .execute(any());

        invokePrivate("writeLedgerEntry");

        assertEquals("Ledger number assignment failed", ws.getWkMsgLine().trim());
        verify(aplfSpy, never()).write();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void writeLedgerEntry_writeInvalidKey_showsMessage() {
        doReturn(true).when(aplfSpy).isInvalidKey();

        invokePrivate("writeLedgerEntry");

        assertEquals("Ledger write failed", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void writeLedgerEntry_success_writesRecordWithComputedFields() {
        ws.setPySupp(500);
        ws.setPyDate(20260315);
        ws.setWkCloseYm(202603);
        ws.setPyNo(1001L);
        ws.setPyAmount(new BigDecimal("1000"));
        ws.setWkNewBal(8500);
        ws.setPyRemark("Invoice 42");
        ws.setWkUserCode(42);

        invokePrivate("writeLedgerEntry");

        assertEquals(1001L, ws.getPlSeq());
        assertEquals(500, ws.getPlSupp());
        assertEquals(20260315, ws.getPlDate());
        assertEquals(202603, ws.getPlCloseYm());
        assertEquals(2, ws.getPlKind());
        assertEquals(ws.getWkRefPay(), ws.getPlRefType());
        assertEquals(1001L, ws.getPlRefNo());
        assertEquals(new BigDecimal("1000"), ws.getPlDebit());
        assertEquals(BigDecimal.ZERO, ws.getPlCredit());
        assertEquals(8500L, ws.getPlBalance().longValue());
        assertEquals("Invoice 42", ws.getPlRemark().trim());
        assertEquals(42, ws.getPlUser());
        verify(aplfSpy).write();
    }

    /* ── TERM-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void closeAllFiles_closesAllFourFilesAndRecordsStatus() {
        invokePrivate("closeAllFiles");

        verify(payfSpy).close();
        verify(aplfSpy).close();
        verify(suppfSpy).close();
        verify(bankfSpy).close();
    }

    /* ── helpers ── */

    private void setupValidSupplierRecord() {
        ws.setSpDelFlag(0);
        ws.setSpName("Acme Supply Co");
        ws.setSpBalance(new BigDecimal("10000"));
    }
}
