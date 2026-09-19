package com.sakura.oe0010.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.numgen.service.NumgenService;
import com.sakura.oe0010.domain.Oe0010FieldAccess;
import com.sakura.oe0010.runtime.Oe0010Datasets;
import com.sakura.runtime.ProgramExitSignal;
import com.sakura.runtime.ScreenRendererInstance;
import com.sakura.runtime.io.CprcfDataset;
import com.sakura.runtime.io.CustfDataset;
import com.sakura.runtime.io.OrddfDataset;
import com.sakura.runtime.io.OrdhfDataset;
import com.sakura.runtime.io.ProdfDataset;
import com.sakura.runtime.io.StokfDataset;
import com.sakura.runtime.linkage.DateutLinkParm;
import com.sakura.runtime.linkage.NumgenLinkParm;
import com.sakura.runtime.linkage.TaxcalLinkParm;
import com.sakura.taxcal.service.TaxcalService;

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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Unit tests for Oe0010Service, generated from COBOL program OE0010 (Sales Order Entry). Ground
 * truth for input/expected values is the COBOL PROCEDURE DIVISION at Sakura/main/OE0010.cob.
 *
 * <p>Private paragraph methods are invoked via reflection (test class lives in the same package).
 * The COBOL WORKING-STORAGE / file record buffers behind {@code ws} are real (backed by the
 * generated XML record schemas), so test setup manipulates them directly via {@code ws}'s public
 * typed getters/setters instead of simulating full screen I/O for every scenario.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Timeout(value = 10, unit = TimeUnit.SECONDS)
class Oe0010ServiceTest {

    @Mock private DateutService dateutService;
    @Mock private TaxcalService taxcalService;
    @Mock private NumgenService numgenService;
    @Mock private AbortxService abortxService;
    @Mock private ScreenRendererInstance renderer;

    private OrdhfDataset ordhf;
    private OrddfDataset orddf;
    private CustfDataset custf;
    private ProdfDataset prodf;
    private CprcfDataset cprcf;
    private StokfDataset stokf;

    private Oe0010Service service;
    private Oe0010FieldAccess ws;

    private final List<String> screenInteractions = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        ordhf = spy(new OrdhfDataset());
        orddf = spy(new OrddfDataset());
        custf = spy(new CustfDataset());
        prodf = spy(new ProdfDataset());
        cprcf = spy(new CprcfDataset());
        stokf = spy(new StokfDataset());

        // I/O is stubbed at the open/close/write/rewrite level so no real file ever touches
        // disk; getFileStatus()/isInvalidKey() default to the RawDatasetBase-initial "00"/false
        // unless a test overrides them to simulate an error.
        for (var f : List.of(ordhf, orddf, custf, prodf, cprcf, stokf)) {
            doNothing().when(f).open(any());
            doNothing().when(f).close();
            doNothing().when(f).write();
            doNothing().when(f).rewrite();
            doReturn(false).when(f).readByKey(any());
        }

        Oe0010Datasets fileSet = spy(new Oe0010Datasets());
        doReturn(ordhf).when(fileSet).getOrdhf();
        doReturn(orddf).when(fileSet).getOrddf();
        doReturn(custf).when(fileSet).getCustf();
        doReturn(prodf).when(fileSet).getProdf();
        doReturn(cprcf).when(fileSet).getCprcf();
        doReturn(stokf).when(fileSet).getStokf();

        service =
                new Oe0010Service(
                        fileSet,
                        dateutService,
                        taxcalService,
                        numgenService,
                        abortxService,
                        renderer);

        Field wsField = Oe0010Service.class.getDeclaredField("ws");
        wsField.setAccessible(true);
        ws = (Oe0010FieldAccess) wsField.get(service);

        doAnswer(
                        inv -> {
                            Object screenDef = inv.getArgument(0);
                            String name =
                                    (String) screenDef.getClass().getField("name").get(screenDef);
                            screenInteractions.add("displayScreen:" + name);
                            return null;
                        })
                .when(renderer)
                .displayScreen(any(), any());
    }

    @AfterEach
    void tearDown() {
        screenInteractions.clear();
    }

    /**
     * Invokes a zero-arg private paragraph method, unwrapping the reflection exception so runtime
     * signals (ProgramExitSignal, ParagraphJumpSignal, ...) surface to assertThrows.
     */
    private void invoke(String methodName) {
        try {
            Method m = Oe0010Service.class.getDeclaredMethod(methodName);
            m.setAccessible(true);
            m.invoke(service);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException(e.getCause());
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private void stubAccept(String fieldName, String value) {
        lenient()
                .when(renderer.acceptField(argThat(f -> f != null && fieldName.equals(f.name))))
                .thenReturn(value);
    }

    private void stubDateutToday(int returnedDate) {
        doAnswer(
                        inv -> {
                            DateutLinkParm p = inv.getArgument(0);
                            p.getKdate().setKdDate1(returnedDate);
                            p.getKdate().setKdStatus("00");
                            return null;
                        })
                .when(dateutService)
                .execute(any());
    }

    // ───────────────────────── INIT-010 / OPENF-010 / OIOH-010 / OIOD-010
    // ─────────────────────────

    @Test
    void initializeProgram_success_setsWorkingStorageAndOpensAllFiles() {
        stubDateutToday(20250115);

        invoke("initializeProgram");

        assertThat(ws.getWkProgid()).isEqualTo("OE0010");
        assertThat(ws.getWkTitle().trim()).isEqualTo("Sales Order Entry");
        assertThat(ws.getWkSysymd()).isEqualTo(20250115);
        assertThat(ws.getWkSysdate()).isEqualTo(20250115);
        verify(ordhf, times(1)).open(any());
        verify(orddf, times(1)).open(any());
        verify(stokf, times(1)).open(any());
        verify(custf, times(1)).open(any());
        verify(prodf, times(1)).open(any());
        verify(cprcf, times(1)).open(any());
        verify(abortxService, never()).execute(any());
    }

    @Test
    void openOrderHeaderFile_fileStatusStaysError_abendsWithCompletionCode255() {
        // CONVERT-GAP: none — Java mirrors OIOH-010's ABEND-RTN exactly.
        doReturn("35").when(ordhf).getFileStatus();

        assertThrows(ProgramExitSignal.class, () -> invoke("openOrderHeaderFile"));

        assertThat(service.getCompletionCode()).isEqualTo(255);
        assertThat(ws.getKaFile().trim()).isEqualTo("ORDHF");
        verify(abortxService, times(1)).execute(any());
    }

    @Test
    void openOrderDetailFile_fileStatusStaysError_abendsWithCompletionCode255() {
        doReturn("30").when(orddf).getFileStatus();

        assertThrows(ProgramExitSignal.class, () -> invoke("openOrderDetailFile"));

        assertThat(service.getCompletionCode()).isEqualTo(255);
        assertThat(ws.getKaFile().trim()).isEqualTo("ORDDF");
    }

    @Test
    void openOrderHeaderFile_retriesAsOutputThenSucceeds_doesNotAbend() {
        // FSTS "35" on first OPEN I-O triggers OPEN OUTPUT / CLOSE / OPEN I-O retry (OPENF-010);
        // the retry sequence then reports "00".
        doReturn("35", "00", "00", "00").when(ordhf).getFileStatus();

        invoke("openOrderHeaderFile");

        verify(abortxService, never()).execute(any());
        assertThat(ws.getFsts()).isEqualTo("00");
    }

    // ───────────────────────── CLRO-010 ─────────────────────────

    @Test
    void clearOrderHeaderWorkArea_resetsWorkFieldsToDefaults() {
        ws.setWkSysdate(20250101);
        ws.setWkLcnt(5);
        ws.setWkNetTotal(1000);
        ws.setHdrOk(1);
        ws.setDtlDone(1);

        invoke("clearOrderHeaderWorkArea");

        assertThat(ws.getWkLcnt()).isZero();
        assertThat(ws.getWkNetTotal()).isZero();
        assertThat(ws.getWkTaxTotal()).isZero();
        assertThat(ws.getWkGrsTotal()).isZero();
        assertThat(ws.getHdrOk()).isZero();
        assertThat(ws.getDtlDone()).isZero();
        assertThat(ws.getWkOrdNoD()).isZero();
        assertThat(ws.getOhDate()).isEqualTo(20250101);
        assertThat(ws.getOhDueDate()).isEqualTo(20250101);
        assertThat(ws.getOhTaxType()).isEqualTo(1);
    }

    // ───────────────────────── EHDR-010 ─────────────────────────

    @Test
    void editOrderHeader_pf3WithNoCustomer_setsEndFlag() {
        stubAccept("OH-DATE", "");
        stubAccept("OH-CUST", "");
        stubAccept("OH-STAFF", "");
        stubAccept("OH-WHSE", "");
        stubAccept("OH-DUE-DATE", "");
        stubAccept("OH-CUST-PO", "");
        stubAccept("OH-TAX-TYPE", "");
        when(renderer.readEndStatus()).thenReturn("03");

        invoke("editOrderHeader");

        assertThat(ws.getEndFlg()).isEqualTo(1);
        assertThat(screenInteractions).contains("displayScreen:DS-HEADER", "displayScreen:DS-HEAD");
    }

    @Test
    void editOrderHeader_pf3WithCustomerEntered_doesNotSetEndFlagOrValidate() {
        stubAccept("OH-DATE", "");
        stubAccept("OH-CUST", "100");
        stubAccept("OH-STAFF", "");
        stubAccept("OH-WHSE", "");
        stubAccept("OH-DUE-DATE", "");
        stubAccept("OH-CUST-PO", "");
        stubAccept("OH-TAX-TYPE", "");
        when(renderer.readEndStatus()).thenReturn("03");

        invoke("editOrderHeader");

        assertThat(ws.getEndFlg()).isZero();
        assertThat(ws.getHdrOk()).isZero();
        verify(custf, never()).readByKey(any());
    }

    @Test
    void editOrderHeader_normalEntryWithValidCustomer_marksHeaderOk() {
        stubAccept("OH-DATE", "20250101");
        stubAccept("OH-CUST", "100");
        stubAccept("OH-STAFF", "0");
        stubAccept("OH-WHSE", "1");
        stubAccept("OH-DUE-DATE", "20250201");
        stubAccept("OH-CUST-PO", "PO-1");
        stubAccept("OH-TAX-TYPE", "0");
        when(renderer.readEndStatus()).thenReturn("00");
        doReturn(false).when(custf).isInvalidKey();
        ws.setCuDelFlag(0);
        ws.setCuName("Acme Corp");
        ws.setCuStaff(7);
        ws.setCuTaxType(2);

        invoke("editOrderHeader");

        assertThat(ws.getHdrOk()).isEqualTo(1);
        assertThat(ws.getOhStaff()).isEqualTo(7);
        assertThat(ws.getOhTaxType()).isEqualTo(2);
        assertThat(ws.getWkCustName().trim()).isEqualTo("Acme Corp");
    }

    // ───────────────────────── VHDR-010 ─────────────────────────

    @Test
    void validateOrderHeader_customerCodeZero_setsMessageAndHdrOkStaysZero() {
        ws.setOhCust(0);

        invoke("validateOrderHeader");

        assertThat(ws.getWkMsgLine().trim()).isEqualTo("Customer code required");
        assertThat(ws.getHdrOk()).isZero();
        verify(custf, never()).readByKey(any());
    }

    @Test
    void validateOrderHeader_customerNotFound_setsMessageAndHdrOkStaysZero() {
        ws.setOhCust(999);
        doReturn(true).when(custf).isInvalidKey();

        invoke("validateOrderHeader");

        assertThat(ws.getWkMsgLine().trim()).isEqualTo("Customer not found");
        assertThat(ws.getHdrOk()).isZero();
    }

    @Test
    void validateOrderHeader_customerDeleted_setsMessageAndHdrOkStaysZero() {
        ws.setOhCust(100);
        doReturn(false).when(custf).isInvalidKey();
        ws.setCuDelFlag(1);

        invoke("validateOrderHeader");

        assertThat(ws.getWkMsgLine().trim()).isEqualTo("Customer is deleted");
        assertThat(ws.getHdrOk()).isZero();
    }

    @Test
    void validateOrderHeader_staffAndTaxTypeZero_defaultFromCustomerRecord() {
        ws.setOhCust(100);
        ws.setOhStaff(0);
        ws.setOhTaxType(0);
        doReturn(false).when(custf).isInvalidKey();
        ws.setCuDelFlag(0);
        ws.setCuName("Beta LLC");
        ws.setCuStaff(9);
        ws.setCuTaxType(3);

        invoke("validateOrderHeader");

        assertThat(ws.getHdrOk()).isEqualTo(1);
        assertThat(ws.getOhStaff()).isEqualTo(9);
        assertThat(ws.getOhTaxType()).isEqualTo(3);
        assertThat(ws.getWkMsgLine().trim()).isEqualTo("Header OK - enter detail lines");
    }

    // ───────────────────────── DLOOP-010 ─────────────────────────

    private void stubDetailAccept(String prod, String whse, String qty, String price) {
        stubAccept("WK-D-PROD", prod);
        stubAccept("WK-D-WHSE", whse);
        stubAccept("WK-D-QTY", qty);
        stubAccept("WK-D-PRICE", price);
    }

    @Test
    void processDetailLineLoop_ests03_setsDtlDone() {
        stubDetailAccept("", "", "", "");
        when(renderer.readEndStatus()).thenReturn("03");

        invoke("processDetailLineLoop");

        assertThat(ws.getDtlDone()).isEqualTo(1);
    }

    @Test
    void processDetailLineLoop_ests04_showsLineClearedMessage() {
        stubDetailAccept("", "", "", "");
        when(renderer.readEndStatus()).thenReturn("04");

        invoke("processDetailLineLoop");

        assertThat(ws.getWkMsgLine().trim()).isEqualTo("Line cleared");
        assertThat(ws.getDtlDone()).isZero();
    }

    @Test
    void processDetailLineLoop_estsUnrecognized_showsInvalidKeyMessage() {
        stubDetailAccept("", "", "", "");
        when(renderer.readEndStatus()).thenReturn("99");

        invoke("processDetailLineLoop");

        assertThat(ws.getWkMsgLine().trim()).isEqualTo("Invalid key");
    }

    // ───────────────────────── PDET-010 ─────────────────────────

    @Test
    void processDetailLine_productCodeZero_setsMessage() {
        ws.setWkDProd(0);

        invoke("processDetailLine");

        assertThat(ws.getWkMsgLine().trim()).isEqualTo("Product code required");
        verify(prodf, never()).readByKey(any());
    }

    @Test
    void processDetailLine_productNotFound_setsMessage() {
        ws.setWkDProd(500);
        doReturn(true).when(prodf).isInvalidKey();

        invoke("processDetailLine");

        assertThat(ws.getWkMsgLine().trim()).isEqualTo("Product not found");
    }

    @Test
    void processDetailLine_productDeleted_setsMessage() {
        ws.setWkDProd(500);
        doReturn(false).when(prodf).isInvalidKey();
        ws.setPrDelFlag(1);

        invoke("processDetailLine");

        assertThat(ws.getWkMsgLine().trim()).isEqualTo("Product is deleted");
    }

    @Test
    void processDetailLine_quantityNotPositive_setsMessage() {
        ws.setWkDProd(500);
        doReturn(false).when(prodf).isInvalidKey();
        ws.setPrDelFlag(0);
        ws.setPrName("Widget");
        ws.setPrTaxCategory(1);
        ws.setWkDQty(0);

        invoke("processDetailLine");

        assertThat(ws.getWkMsgLine().trim()).isEqualTo("Quantity must be positive");
    }

    @Test
    void processDetailLine_priceAlreadySet_skipsResolveAndAddsLine() {
        ws.setWkDProd(500);
        doReturn(false).when(prodf).isInvalidKey();
        ws.setPrDelFlag(0);
        ws.setPrName("Widget");
        ws.setPrTaxCategory(2);
        ws.setPrStockMng(0);
        ws.setWkDQty(3);
        ws.setWkDPrice(new BigDecimal("10.00"));
        ws.setWkLcnt(0);
        ws.setWkNetTotal(0);

        invoke("processDetailLine");

        verify(cprcf, never()).readByKey(any());
        assertThat(ws.getWkDAmt()).isEqualTo(30L);
        assertThat(ws.getWkLcnt()).isEqualTo(1);
        assertThat(ws.getWlProd(1)).isEqualTo(500);
        assertThat(ws.getWkNetTotal()).isEqualTo(30L);
        assertThat(ws.getWkMsgLine().trim()).isEqualTo("Line added");
    }

    @Test
    void processDetailLine_priceZeroWithValidContractPrice_usesContractPrice() {
        ws.setOhCust(100);
        ws.setWkDProd(500);
        doReturn(false).when(prodf).isInvalidKey();
        ws.setPrDelFlag(0);
        ws.setPrName("Widget");
        ws.setPrTaxCategory(1);
        ws.setPrStockMng(0);
        ws.setWkDQty(2);
        ws.setWkDPrice(BigDecimal.ZERO);
        ws.setOhDate(20250110);
        doReturn(false).when(cprcf).isInvalidKey();
        ws.setCpDelFlag(0);
        ws.setCpStartDate(20250101);
        ws.setCpEndDate(0);
        ws.setCpPrice(new BigDecimal("12.50"));

        invoke("processDetailLine");

        assertThat(ws.getWkDPrice()).isEqualByComparingTo("12.50");
        assertThat(ws.getWkDAmt()).isEqualTo(25L);
    }

    // ───────────────────────── RPRC-010 / RANK-010 ─────────────────────────

    @Test
    void resolveDetailPrice_contractPriceNotFound_fallsBackToRankPrice() {
        doReturn(true).when(cprcf).isInvalidKey();
        ws.setCuPriceRank(2);
        ws.setPrRankPrice(2, new BigDecimal("8.00"));

        invoke("resolveDetailPrice");

        assertThat(ws.getWkDPrice()).isEqualByComparingTo("8.00");
    }

    @Test
    void resolveDetailPrice_contractPriceDeletedOrExpired_fallsBackToRankPrice() {
        doReturn(false).when(cprcf).isInvalidKey();
        ws.setCpDelFlag(1); // deleted contract price -> ignored even though found
        ws.setCuPriceRank(1);
        ws.setPrRankPrice(1, new BigDecimal("9.00"));

        invoke("resolveDetailPrice");

        assertThat(ws.getWkDPrice()).isEqualByComparingTo("9.00");
    }

    @Test
    void applyRankPrice_rankOutOfRange_usesListPrice() {
        ws.setCuPriceRank(0);
        ws.setPrListPrice(new BigDecimal("15.00"));
        ws.setWkDPrice(BigDecimal.ZERO);

        invoke("applyRankPrice");

        assertThat(ws.getWkDPrice()).isEqualByComparingTo("15.00");
    }

    // ───────────────────────── CSTK-010 ─────────────────────────

    @Test
    void checkStockAvailability_stockNotManaged_returnsEarlyWithoutReadingStock() {
        ws.setPrStockMng(0);

        invoke("checkStockAvailability");

        verify(stokf, never()).readByKey(any());
    }

    @Test
    void checkStockAvailability_recordNotFound_availabilityZeroAndWarningWhenQtyPositive() {
        ws.setPrStockMng(1);
        ws.setWkDQty(5);
        doReturn(true).when(stokf).isInvalidKey();

        invoke("checkStockAvailability");

        assertThat(ws.getWkDAvail()).isZero();
        assertThat(ws.getWkMsgLine().trim()).isEqualTo("Warning: quantity exceeds available stock");
    }

    @Test
    void checkStockAvailability_sufficientStock_noWarning() {
        ws.setPrStockMng(1);
        ws.setWkDQty(5);
        doReturn(false).when(stokf).isInvalidKey();
        ws.setSkOnhand(new BigDecimal("20"));
        ws.setSkAllocated(new BigDecimal("5"));

        invoke("checkStockAvailability");

        assertThat(ws.getWkDAvail()).isEqualTo(15);
        assertThat(ws.getWkMsgLine().trim())
                .isNotEqualTo("Warning: quantity exceeds available stock");
    }

    @Test
    void checkStockAvailability_insufficientStock_warningMessage() {
        ws.setPrStockMng(1);
        ws.setWkDQty(20);
        doReturn(false).when(stokf).isInvalidKey();
        ws.setSkOnhand(new BigDecimal("10"));
        ws.setSkAllocated(new BigDecimal("2"));

        invoke("checkStockAvailability");

        assertThat(ws.getWkDAvail()).isEqualTo(8);
        assertThat(ws.getWkMsgLine().trim()).isEqualTo("Warning: quantity exceeds available stock");
    }

    // ───────────────────────── ADDL-010 ─────────────────────────

    @Test
    void addDetailLineToOrder_maxLinesReached_setsMessageAndDoesNotAdd() {
        ws.setWkLcnt(200);

        invoke("addDetailLineToOrder");

        assertThat(ws.getWkMsgLine().trim()).isEqualTo("Maximum 200 lines reached");
        assertThat(ws.getWkLcnt()).isEqualTo(200);
    }

    @Test
    void addDetailLineToOrder_underLimit_addsLineAndAccumulatesTotal() {
        ws.setWkLcnt(0);
        ws.setWkNetTotal(100);
        ws.setWkDProd(500);
        ws.setWkDWhse(1);
        ws.setWkDQty(4);
        ws.setWkDPrice(new BigDecimal("2.50"));
        ws.setWkDAmt(10);
        ws.setWkDTaxcat(1);

        invoke("addDetailLineToOrder");

        assertThat(ws.getWkLcnt()).isEqualTo(1);
        assertThat(ws.getWlProd(1)).isEqualTo(500);
        assertThat(ws.getWlWhse(1)).isEqualTo(1);
        assertThat(ws.getWlQty(1)).isEqualTo(4);
        assertThat(ws.getWlAmount(1)).isEqualTo(10L);
        assertThat(ws.getWkNetTotal()).isEqualTo(110L);
        assertThat(ws.getWkMsgLine().trim()).isEqualTo("Line added");
    }

    // ───────────────────────── CSAV-010 / CTT-010 / SORD-010 / WDET-010 / ALOC-010
    // ─────────────────────────

    @Test
    void confirmAndSaveOrder_confirmY_savesOrderHeaderAndDetails() {
        ws.setWkLcnt(1);
        ws.setWlProd(1, 500);
        ws.setWlWhse(1, 1);
        ws.setWlQty(1, 4);
        ws.setWlPrice(1, new BigDecimal("2.50"));
        ws.setWlAmount(1, 10L);
        ws.setWlTaxcat(1, 1);
        ws.setWkNetTotal(10);
        ws.setOhCust(100);
        ws.setCuTaxRound(1);
        ws.setOhDate(20250101);
        ws.setOhDueDate(20250201);

        stubAccept("WK-CONFIRM", "Y");
        when(renderer.readEndStatus()).thenReturn("00");
        stubTaxcalPassthrough();
        doAnswer(
                        inv -> {
                            NumgenLinkParm p = inv.getArgument(0);
                            p.getKnum().setKnumNumber(123L);
                            p.getKnum().setKnumStatus("00");
                            return null;
                        })
                .when(numgenService)
                .execute(any());
        doReturn(true).when(stokf).isInvalidKey();

        invoke("confirmAndSaveOrder");

        assertThat(ws.getOhNo()).isEqualTo(123L);
        assertThat(ws.getWkMsgLine().trim()).isEqualTo("Order saved");
        verify(ordhf, times(1)).write();
        verify(orddf, times(1)).write();
    }

    @Test
    void confirmAndSaveOrder_confirmN_discardsOrderWithoutSaving() {
        stubAccept("WK-CONFIRM", "N");
        when(renderer.readEndStatus()).thenReturn("00");

        invoke("confirmAndSaveOrder");

        assertThat(ws.getWkMsgLine().trim()).isEqualTo("Order discarded");
        verify(numgenService, never()).execute(any());
        verify(ordhf, never()).write();
    }

    private void stubTaxcalPassthrough() {
        doAnswer(
                        inv -> {
                            TaxcalLinkParm p = inv.getArgument(0);
                            p.getKtax().setKtNet(p.getKtax().getKtAmount());
                            p.getKtax().setKtTax(BigDecimal.ZERO);
                            p.getKtax().setKtGross(p.getKtax().getKtAmount());
                            p.getKtax().setKtStatus("00");
                            return null;
                        })
                .when(taxcalService)
                .execute(any());
    }

    @Test
    void calculateOrderTax_zeroCustomerTaxRound_defaultsRoundToOneAndAppliesTaxcal() {
        ws.setWkNetTotal(1000);
        ws.setCuTaxRound(0);
        ws.setOhTaxType(1);
        ws.setOhDate(20250101);
        doAnswer(
                        inv -> {
                            TaxcalLinkParm p = inv.getArgument(0);
                            assertThat(p.getKtax().getKtRound()).isEqualTo(1);
                            p.getKtax().setKtNet(new BigDecimal("900"));
                            p.getKtax().setKtTax(new BigDecimal("100"));
                            p.getKtax().setKtGross(new BigDecimal("1000"));
                            return null;
                        })
                .when(taxcalService)
                .execute(any());

        invoke("calculateOrderTax");

        assertThat(ws.getWkNetTotal()).isEqualTo(900L);
        assertThat(ws.getWkTaxTotal()).isEqualTo(100L);
        assertThat(ws.getWkGrsTotal()).isEqualTo(1000L);
    }

    @Test
    void saveOrderHeader_numgenFails_setsMessageAndDoesNotWriteHeader() {
        doAnswer(
                        inv -> {
                            NumgenLinkParm p = inv.getArgument(0);
                            p.getKnum().setKnumStatus("99");
                            return null;
                        })
                .when(numgenService)
                .execute(any());

        invoke("saveOrderHeader");

        assertThat(ws.getWkMsgLine().trim()).isEqualTo("Number assignment failed");
        verify(ordhf, never()).write();
    }

    @Test
    void saveOrderHeader_headerWriteFails_setsMessageAndSkipsDetailWrite() {
        doAnswer(
                        inv -> {
                            NumgenLinkParm p = inv.getArgument(0);
                            p.getKnum().setKnumNumber(50L);
                            p.getKnum().setKnumStatus("00");
                            return null;
                        })
                .when(numgenService)
                .execute(any());
        doReturn(true).when(ordhf).isInvalidKey();
        ws.setWkLcnt(1);

        invoke("saveOrderHeader");

        assertThat(ws.getWkMsgLine().trim()).isEqualTo("Header write failed");
        verify(orddf, never()).write();
    }

    @Test
    void writeOrderDetailLines_writesEachLineAndAllocatesStock() {
        ws.setOhNo(77L);
        ws.setOhDueDate(20250301);
        ws.setWkLcnt(2);
        ws.setWlProd(1, 500);
        ws.setWlWhse(1, 1);
        ws.setWlQty(1, 3);
        ws.setWlPrice(1, new BigDecimal("2.00"));
        ws.setWlAmount(1, 6L);
        ws.setWlTaxcat(1, 1);
        ws.setWlProd(2, 600);
        ws.setWlWhse(2, 2);
        ws.setWlQty(2, 5);
        ws.setWlPrice(2, new BigDecimal("3.00"));
        ws.setWlAmount(2, 15L);
        ws.setWlTaxcat(2, 1);
        doReturn(true).when(stokf).isInvalidKey();

        invoke("writeOrderDetailLines");

        verify(orddf, times(2)).write();
        verify(stokf, times(2)).write();
    }

    @Test
    void allocateStockForLine_existingStockRecord_addsQuantityToAllocated() {
        ws.setLx(1);
        ws.setWlProd(1, 500);
        ws.setWlWhse(1, 1);
        ws.setWlQty(1, 4);
        doReturn(false).when(stokf).isInvalidKey();
        ws.setSkAllocated(new BigDecimal("10"));

        invoke("allocateStockForLine");

        assertThat(ws.getSkAllocated()).isEqualByComparingTo("14");
        verify(stokf, times(1)).rewrite();
        verify(stokf, never()).write();
    }

    @Test
    void allocateStockForLine_noExistingStockRecord_writesNewStockRecord() {
        ws.setLx(1);
        ws.setWlProd(1, 500);
        ws.setWlWhse(1, 1);
        ws.setWlQty(1, 4);
        doReturn(true).when(stokf).isInvalidKey();

        invoke("allocateStockForLine");

        verify(stokf, times(1)).write();
        verify(stokf, never()).rewrite();
    }
}
