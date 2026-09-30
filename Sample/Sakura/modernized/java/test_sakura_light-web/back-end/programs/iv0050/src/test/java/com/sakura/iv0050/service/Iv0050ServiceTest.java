package com.sakura.iv0050.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.iv0050.runtime.Iv0050Datasets;
import com.sakura.numgen.service.NumgenService;
import com.sakura.runtime.DatasetEnums.FileOpenMode;
import com.sakura.runtime.ProgramExitSignal;
import com.sakura.runtime.ScreenRendererInstance;
import com.sakura.runtime.io.ProdfDataset;
import com.sakura.runtime.io.SmovfDataset;
import com.sakura.runtime.io.StokfDataset;
import com.sakura.runtime.io.WhsefDataset;
import com.sakura.runtime.linkage.NumgenLinkParm;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Timeout(value = 10, unit = TimeUnit.SECONDS)
class Iv0050ServiceTest {

    @Mock private Iv0050Datasets fileSet;
    @Mock private DateutService dateutService;
    @Mock private NumgenService numgenService;
    @Mock private AbortxService abortxService;
    @Mock private ScreenRendererInstance renderer;

    private StokfDataset stokf;
    private SmovfDataset smovf;
    private ProdfDataset prodf;
    private WhsefDataset whsef;

    private Iv0050Service service;

    private final List<String> screenInteractions = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        stokf = spy(new StokfDataset());
        smovf = spy(new SmovfDataset());
        prodf = spy(new ProdfDataset());
        whsef = spy(new WhsefDataset());

        doNothing().when(stokf).open(any());
        doNothing().when(stokf).close();
        doNothing().when(smovf).open(any());
        doNothing().when(smovf).close();
        doNothing().when(prodf).open(any());
        doNothing().when(prodf).close();
        doNothing().when(whsef).open(any());
        doNothing().when(whsef).close();

        when(fileSet.getStokf()).thenReturn(stokf);
        when(fileSet.getSmovf()).thenReturn(smovf);
        when(fileSet.getProdf()).thenReturn(prodf);
        when(fileSet.getWhsef()).thenReturn(whsef);

        service = new Iv0050Service(fileSet, dateutService, numgenService, abortxService, renderer);
        service.setRenderer(renderer);

        doAnswerCaptureScreen();
    }

    @AfterEach
    void tearDown() {
        screenInteractions.clear();
    }

    private void doAnswerCaptureScreen() {
        org.mockito.Mockito.doAnswer(
                        inv -> {
                            Object def = inv.getArgument(0);
                            screenInteractions.add("displayScreen:" + def);
                            return null;
                        })
                .when(renderer)
                .displayScreen(any(), any());
    }

    /**
     * Invokes a private/no-arg paragraph method via reflection — these are the COBOL PERFORM
     * targets.
     */
    private void invoke(String methodName) throws Exception {
        Method m = Iv0050Service.class.getDeclaredMethod(methodName);
        m.setAccessible(true);
        m.invoke(service);
    }

    private com.sakura.iv0050.domain.Iv0050FieldAccess wsField() throws Exception {
        var f = Iv0050Service.class.getDeclaredField("ws");
        f.setAccessible(true);
        return (com.sakura.iv0050.domain.Iv0050FieldAccess) f.get(service);
    }

    // ---------- INIT-010 / OPENF-010 ----------

    @Test
    void initializeProgram_happyPath_setsProgramIdentityAndOpensFiles() throws Exception {
        var ws = wsField();
        invoke("initializeProgram");

        assertEquals("IV0050", ws.getWkProgid());
        assertEquals("Inter-Warehouse Transfer", ws.getWkTitle().trim());
        verify(dateutService, times(1)).execute(any());
        verify(stokf, times(1)).open(FileOpenMode.IO);
        verify(smovf, times(1)).open(FileOpenMode.IO);
        verify(prodf, times(1)).open(FileOpenMode.INPUT);
        verify(whsef, times(1)).open(FileOpenMode.INPUT);
    }

    @Test
    void openProgramFiles_fileStatusOk_opensAllFourFilesWithoutAbort() throws Exception {
        invoke("openProgramFiles");

        verify(stokf, times(1)).open(FileOpenMode.IO);
        verify(smovf, times(1)).open(FileOpenMode.IO);
        verify(prodf, times(1)).open(FileOpenMode.INPUT);
        verify(whsef, times(1)).open(FileOpenMode.INPUT);
    }

    @Test
    void openProgramFiles_stokfOpenFails_abortsWithFileOpenError() throws Exception {
        doReturn("99").when(stokf).getFileStatus();

        try {
            invoke("openProgramFiles");
        } catch (Exception e) {
            // reflection wraps ProgramExitSignal in InvocationTargetException
            assertThat(e.getCause()).isInstanceOf(ProgramExitSignal.class);
        }
        var ws = wsField();
        assertEquals("STOKF", ws.getKaFile().trim());
        assertEquals(255, ws.getCompletionCode());
        verify(abortxService, times(1)).execute(any());
    }

    @Test
    void openWithCreateRetry_fileStatus35_reopensViaOutputThenIoMode() throws Exception {
        // First read of FSTS after initial OPEN I-O sees "35" (file not found) -> retry;
        // subsequent reads (after OUTPUT/CLOSE/re-OPEN I-O) see "00".
        when(stokf.getFileStatus()).thenReturn("35", "00", "00", "00");

        invoke("openProgramFiles");

        verify(stokf, times(1)).open(FileOpenMode.OUTPUT);
        verify(stokf, times(2)).open(FileOpenMode.IO);
        verify(stokf, times(1)).close();
    }

    // ---------- MAINR-010 ----------

    @Test
    void processMainScreenCycle_estsIs03_setsEndFlgToStop() throws Exception {
        var ws = wsField();
        // ESTS is overwritten by GET-KEY's own ACCEPT (broadcastEstsStatus reads
        // renderer.readEndStatus()), so the switch's decision comes from the stub, not
        // any value set on ws beforehand.
        when(renderer.readEndStatus()).thenReturn("03");

        invoke("processMainScreenCycle");

        assertEquals(1, ws.getEndFlg());
    }

    @Test
    void processMainScreenCycle_estsOther_showsInvalidFunctionKeyMessage() throws Exception {
        var ws = wsField();
        when(renderer.readEndStatus()).thenReturn("P9");

        invoke("processMainScreenCycle");

        assertEquals("Invalid function key", ws.getWkMsgLine().trim());
        assertThat(screenInteractions).isNotEmpty();
    }

    // ---------- VALIDATE-INPUT (VIN-010) ----------

    @Test
    void validateTransferInput_productCodeZero_setsErrorAndDoesNotReadProduct() throws Exception {
        var ws = wsField();
        ws.setWkKeyProd(0);

        invoke("validateTransferInput");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Product code must not be zero", ws.getWkMsgLine().trim());
        verify(prodf, times(0)).readByKey(any());
    }

    @Test
    void validateTransferInput_productNotFound_setsError() throws Exception {
        var ws = wsField();
        ws.setWkKeyProd(1234);
        doReturn(true).when(prodf).isInvalidKey();

        invoke("validateTransferInput");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Product not found", ws.getWkMsgLine().trim());
    }

    @Test
    void validateTransferInput_productDeleted_setsError() throws Exception {
        var ws = wsField();
        ws.setWkKeyProd(1234);
        doReturn(false).when(prodf).isInvalidKey();
        ws.setPrDelFlag(1);

        invoke("validateTransferInput");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Product is deleted", ws.getWkMsgLine().trim());
    }

    @Test
    void validateTransferInput_productNotStockManaged_setsError() throws Exception {
        var ws = wsField();
        ws.setWkKeyProd(1234);
        doReturn(false).when(prodf).isInvalidKey();
        ws.setPrDelFlag(0);
        ws.setPrStockMng(0);

        invoke("validateTransferInput");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Product is not stock-managed", ws.getWkMsgLine().trim());
    }

    @Test
    void validateTransferInput_warehousesMissing_setsError() throws Exception {
        var ws = wsField();
        setUpValidProduct(ws);
        ws.setWkFromWhse(0);
        ws.setWkToWhse(10);

        invoke("validateTransferInput");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Both warehouses are required", ws.getWkMsgLine().trim());
    }

    @Test
    void validateTransferInput_sameFromAndToWarehouse_setsError() throws Exception {
        var ws = wsField();
        setUpValidProduct(ws);
        ws.setWkFromWhse(10);
        ws.setWkToWhse(10);

        invoke("validateTransferInput");

        assertEquals(1, ws.getErrFlg());
        assertEquals("From and to warehouse must differ", ws.getWkMsgLine().trim());
    }

    @Test
    void validateTransferInput_fromWarehouseNotFound_setsError() throws Exception {
        var ws = wsField();
        setUpValidProduct(ws);
        ws.setWkFromWhse(10);
        ws.setWkToWhse(20);
        doReturn(true).when(whsef).isInvalidKey();

        invoke("validateTransferInput");

        assertEquals(1, ws.getErrFlg());
        assertEquals("From warehouse not found", ws.getWkMsgLine().trim());
    }

    @Test
    void validateTransferInput_toWarehouseDeleted_setsError() throws Exception {
        var ws = wsField();
        setUpValidProduct(ws);
        ws.setWkFromWhse(10);
        ws.setWkToWhse(20);
        // From warehouse read: valid, not deleted. To warehouse read: valid, deleted.
        when(whsef.isInvalidKey()).thenReturn(false, false);
        ws.setWhDelFlag(0);
        invokeSetWhDelFlagSequenceForToWarehouse(ws);

        invoke("validateTransferInput");

        assertEquals(1, ws.getErrFlg());
        assertEquals("To warehouse is deleted", ws.getWkMsgLine().trim());
    }

    /**
     * WH-DEL-FLAG is a single shared buffer field re-read after each keyed READ WHSEF; stub the
     * dataset to flip it between the From and To reads via an answer on readByKey.
     */
    private void invokeSetWhDelFlagSequenceForToWarehouse(
            com.sakura.iv0050.domain.Iv0050FieldAccess ws) {
        final int[] callCount = {0};
        org.mockito.Mockito.doAnswer(
                        inv -> {
                            callCount[0]++;
                            if (callCount[0] == 1) {
                                ws.setWhDelFlag(0); // From warehouse: not deleted
                            } else {
                                ws.setWhDelFlag(1); // To warehouse: deleted
                            }
                            return true;
                        })
                .when(whsef)
                .readByKey(any());
    }

    @Test
    void validateTransferInput_quantityNotPositive_setsError() throws Exception {
        var ws = wsField();
        setUpValidProductAndWarehouses(ws);
        ws.setWkTrQty(0);

        invoke("validateTransferInput");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Transfer quantity must be positive", ws.getWkMsgLine().trim());
    }

    @Test
    void validateTransferInput_allValid_noError() throws Exception {
        var ws = wsField();
        setUpValidProductAndWarehouses(ws);
        ws.setWkTrQty(5);

        invoke("validateTransferInput");

        assertEquals(0, ws.getErrFlg());
        assertEquals("From WH", ws.getWkFromName().trim());
        assertEquals("To WH", ws.getWkToName().trim());
    }

    private void setUpValidProduct(com.sakura.iv0050.domain.Iv0050FieldAccess ws) {
        ws.setWkKeyProd(1234);
        doReturn(false).when(prodf).isInvalidKey();
        ws.setPrDelFlag(0);
        ws.setPrStockMng(1);
        ws.setPrName("Widget");
    }

    private void setUpValidProductAndWarehouses(com.sakura.iv0050.domain.Iv0050FieldAccess ws)
            throws Exception {
        setUpValidProduct(ws);
        ws.setWkFromWhse(10);
        ws.setWkToWhse(20);
        doReturn(false).when(whsef).isInvalidKey();
        final int[] callCount = {0};
        org.mockito.Mockito.doAnswer(
                        inv -> {
                            callCount[0]++;
                            ws.setWhDelFlag(0);
                            ws.setWhName(callCount[0] == 1 ? "From WH" : "To WH");
                            return true;
                        })
                .when(whsef)
                .readByKey(any());
    }

    // ---------- READ-SOURCE-STOCK (RSS-010) ----------

    @Test
    void loadAndValidateSourceStock_noStockAtSource_setsError() throws Exception {
        var ws = wsField();
        doReturn(true).when(stokf).isInvalidKey();

        invoke("loadAndValidateSourceStock");

        assertEquals(1, ws.getErrFlg());
        assertEquals("No stock at source warehouse", ws.getWkMsgLine().trim());
    }

    @Test
    void loadAndValidateSourceStock_qtyExceedsAvailable_setsError() throws Exception {
        var ws = wsField();
        doReturn(false).when(stokf).isInvalidKey();
        ws.setSkOnhand(BigDecimal.valueOf(100));
        ws.setSkAllocated(BigDecimal.valueOf(90));
        ws.setWkTrQty(20); // available = 10, requested = 20

        invoke("loadAndValidateSourceStock");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Quantity exceeds available at source", ws.getWkMsgLine().trim());
    }

    @Test
    void loadAndValidateSourceStock_withinAvailable_noError() throws Exception {
        var ws = wsField();
        doReturn(false).when(stokf).isInvalidKey();
        ws.setSkOnhand(BigDecimal.valueOf(100));
        ws.setSkAllocated(BigDecimal.valueOf(20));
        ws.setSkAvgCost(BigDecimal.valueOf(9.5));
        ws.setWkTrQty(50);

        invoke("loadAndValidateSourceStock");

        assertEquals(0, ws.getErrFlg());
        assertEquals(100, ws.getWkSrcOnhand());
        assertEquals(80, ws.getWkAvail());
    }

    // ---------- READ-DEST-STOCK (RDS-010) ----------

    @Test
    void loadDestinationStockOnHand_noExistingRecord_zero() throws Exception {
        var ws = wsField();
        doReturn(true).when(stokf).isInvalidKey();

        invoke("loadDestinationStockOnHand");

        assertEquals(0, ws.getWkDstOnhand());
    }

    @Test
    void loadDestinationStockOnHand_existingRecord_copiesOnhand() throws Exception {
        var ws = wsField();
        doReturn(false).when(stokf).isInvalidKey();
        ws.setSkOnhand(BigDecimal.valueOf(42));

        invoke("loadDestinationStockOnHand");

        assertEquals(42, ws.getWkDstOnhand());
    }

    // ---------- DECREASE-SOURCE (DSR-010) ----------

    @Test
    void decrementSourceStock_sourceDisappeared_setsError() throws Exception {
        var ws = wsField();
        doReturn(true).when(stokf).isInvalidKey();

        invoke("decrementSourceStock");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Source stock disappeared", ws.getWkMsgLine().trim());
        verify(stokf, times(0)).rewrite();
    }

    @Test
    void decrementSourceStock_rewriteFails_setsError() throws Exception {
        var ws = wsField();
        ws.setSkOnhand(BigDecimal.valueOf(100));
        ws.setSkYtdOut(BigDecimal.ZERO);
        ws.setWkTrQty(10);
        ws.setWkSysdate(20250101);
        // read READ ok (false), then REWRITE ... INVALID KEY fires (true)
        when(stokf.isInvalidKey()).thenReturn(false, true);
        doNothing().when(stokf).rewrite();

        invoke("decrementSourceStock");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Source stock update failed", ws.getWkMsgLine().trim());
    }

    @Test
    void decrementSourceStock_happyPath_decreasesOnhandAndTracksYtdOut() throws Exception {
        var ws = wsField();
        ws.setSkOnhand(BigDecimal.valueOf(100));
        ws.setSkYtdOut(BigDecimal.valueOf(5));
        ws.setSkAvgCost(BigDecimal.valueOf(3.5));
        ws.setWkTrQty(30);
        ws.setWkSysdate(20250115);
        when(stokf.isInvalidKey()).thenReturn(false, false); // read ok, rewrite ok
        doNothing().when(stokf).rewrite();

        invoke("decrementSourceStock");

        assertEquals(0, ws.getErrFlg());
        assertEquals(70, ws.getSkOnhand().intValue());
        assertEquals(35, ws.getSkYtdOut().intValue());
        assertEquals(20250115, ws.getSkLastOutDate());
        assertEquals(70, ws.getWkSrcNew());
        verify(stokf, times(1)).rewrite();
    }

    // ---------- INCREASE-DEST (IND-010) / CREATE-DEST (CRD-010) ----------

    @Test
    void incrementDestinationStock_noExistingRecord_createsNewRecord() throws Exception {
        var ws = wsField();
        ws.setWkTrQty(15);
        ws.setWkUnitCost(BigDecimal.valueOf(2.5));
        ws.setWkSysdate(20250101);
        doReturn(true).when(stokf).isInvalidKey();
        doNothing().when(stokf).write();

        invoke("incrementDestinationStock");

        assertEquals(15, ws.getSkOnhand().intValue());
        assertEquals(15, ws.getWkDstNew());
        verify(stokf, times(1)).write();
        verify(stokf, times(0)).rewrite();
    }

    @Test
    void incrementDestinationStock_existingRecord_increasesOnhandAndTracksYtdIn() throws Exception {
        var ws = wsField();
        ws.setSkOnhand(BigDecimal.valueOf(50));
        ws.setSkYtdIn(BigDecimal.valueOf(10));
        ws.setWkTrQty(15);
        ws.setWkSysdate(20250115);
        when(stokf.isInvalidKey()).thenReturn(false, false);
        doNothing().when(stokf).rewrite();

        invoke("incrementDestinationStock");

        assertEquals(65, ws.getSkOnhand().intValue());
        assertEquals(25, ws.getSkYtdIn().intValue());
        assertEquals(20250115, ws.getSkLastInDate());
        assertEquals(65, ws.getWkDstNew());
        verify(stokf, times(1)).rewrite();
    }

    @Test
    void incrementDestinationStock_rewriteFails_showsErrorButKeepsErrFlgUntouched()
            throws Exception {
        // CONVERT-GAP watch: COBOL's IND-010 has NO "SET ERR-YES" on this REWRITE failure
        // (unlike DSR-010) — it only DISPLAYs the message. Java correctly mirrors this.
        var ws = wsField();
        ws.setErrFlg(0);
        ws.setSkOnhand(BigDecimal.valueOf(50));
        ws.setSkYtdIn(BigDecimal.ZERO);
        ws.setWkTrQty(5);
        when(stokf.isInvalidKey()).thenReturn(false, true);
        doNothing().when(stokf).rewrite();

        invoke("incrementDestinationStock");

        assertEquals("Dest stock update failed", ws.getWkMsgLine().trim());
        assertEquals(0, ws.getErrFlg());
    }

    @Test
    void createDestinationStockRecord_writeFails_showsError() throws Exception {
        var ws = wsField();
        ws.setWkTrQty(9);
        ws.setWkUnitCost(BigDecimal.valueOf(1.1));
        doReturn(true).when(stokf).isInvalidKey();
        doNothing().when(stokf).write();

        invoke("createDestinationStockRecord");

        assertEquals("Dest stock create failed", ws.getWkMsgLine().trim());
    }

    // ---------- WRITE-MOVE (WMV-010) ----------

    @Test
    void writeStockMovementRecord_numgenStatusNotOk_skipsWriteAndShowsError() throws Exception {
        var ws = wsField();
        doAnswerNumgenStatus("99", 0L);

        invoke("writeStockMovementRecord");

        assertEquals("Movement number assignment failed", ws.getWkMsgLine().trim());
        verify(smovf, times(0)).write();
    }

    @Test
    void writeStockMovementRecord_happyPath_writesMovementFields() throws Exception {
        var ws = wsField();
        ws.setWkKeyProd(1234);
        ws.setWkMvWhse(10);
        ws.setWkMvQty(-30);
        ws.setWkMvBal(70);
        ws.setWkUnitCost(BigDecimal.valueOf(3.5));
        ws.setWkUserCode(1001);
        ws.setWkSysdate(20250115);
        doAnswerNumgenStatus("00", 555L);
        doReturn(false).when(smovf).isInvalidKey();
        doNothing().when(smovf).write();

        invoke("writeStockMovementRecord");

        assertEquals(555L, ws.getSmSeq());
        assertEquals(20250115, ws.getSmDate());
        assertEquals(1234, ws.getSmProd());
        assertEquals(10, ws.getSmWhse());
        assertEquals(40, ws.getSmKind());
        assertEquals(-30, ws.getSmQty().intValue());
        assertEquals(70, ws.getSmBalAfter().intValue());
        assertEquals(40, ws.getSmRefType());
        assertEquals(555L, ws.getSmRefNo());
        assertEquals(1001, ws.getSmUser());
        verify(smovf, times(1)).write();
    }

    @Test
    void writeStockMovementRecord_writeFails_showsError() throws Exception {
        var ws = wsField();
        doAnswerNumgenStatus("00", 1L);
        doReturn(true).when(smovf).isInvalidKey();
        doNothing().when(smovf).write();

        invoke("writeStockMovementRecord");

        assertEquals("Movement write failed", ws.getWkMsgLine().trim());
    }

    private void doAnswerNumgenStatus(String status, long number) {
        org.mockito.Mockito.doAnswer(
                        inv -> {
                            NumgenLinkParm p = inv.getArgument(0);
                            p.getKnum().setKnumStatus(status);
                            p.getKnum().setKnumNumber(number);
                            return null;
                        })
                .when(numgenService)
                .execute(any());
    }

    // ---------- CONFIRM-TRANSFER (CFT-010) / POST-TRANSFER (PT-010) ----------

    @Test
    void confirmAndProcessTransfer_confirmN_cancelsWithoutTransfer() throws Exception {
        var ws = wsField();
        when(renderer.acceptField(any())).thenReturn("N");
        when(renderer.readEndStatus()).thenReturn("00");

        invoke("confirmAndProcessTransfer");

        assertEquals("Transfer cancelled", ws.getWkMsgLine().trim());
        verify(stokf, times(0)).rewrite();
        verify(smovf, times(0)).write();
    }

    @Test
    void confirmAndProcessTransfer_confirmLowercaseY_executesTransfer() throws Exception {
        var ws = wsField();
        ws.setWkKeyProd(1234);
        ws.setWkFromWhse(10);
        ws.setWkToWhse(20);
        ws.setWkTrQty(5);
        ws.setWkSysdate(20250101);
        ws.setSkOnhand(BigDecimal.valueOf(100));
        ws.setSkYtdOut(BigDecimal.ZERO);
        ws.setSkYtdIn(BigDecimal.ZERO);
        when(renderer.acceptField(any())).thenReturn("y");
        when(renderer.readEndStatus()).thenReturn("00");
        when(stokf.isInvalidKey()).thenReturn(false, false, false, false);
        doNothing().when(stokf).rewrite();
        doAnswerNumgenStatus("00", 1L);
        doReturn(false).when(smovf).isInvalidKey();
        doNothing().when(smovf).write();

        invoke("confirmAndProcessTransfer");

        verify(stokf, times(2)).rewrite();
        verify(smovf, times(2)).write();
    }

    @Test
    void executeStockTransfer_decrementFails_stopsBeforeAnyMovementWrite() throws Exception {
        doReturn(true).when(stokf).isInvalidKey();

        invoke("executeStockTransfer");

        verify(smovf, times(0)).write();
        verify(stokf, times(0)).rewrite();
    }

    // ---------- SHOW-RESULT (SHR-010) — CONVERT-GAP ----------

    @Test
    void displayTransferResult_smallQuantity_cobolEditedPictureVsJavaZeroPad() throws Exception {
        // CONVERT-GAP: COBOL WK-ED-QTY is PIC ----,---,--9 (a suppressed-zero, floating-sign
        // numeric-edited field), so MOVE 5 TO WK-ED-QTY / STRING ... produces
        // "Transferred            5 units - done" (11 leading spaces + the digit).
        // Java instead does String.format("%012d", ...), zero-padding to
        // "Transferred 000000000005 units - done". Both differ from the COBOL ground truth.
        var ws = wsField();
        ws.setWkTrQty(5);

        invoke("displayTransferResult");

        String cobolExpected = "Transferred            5 units - done";
        assertEquals(cobolExpected, ws.getWkMsgLine().trim());
    }

    @Test
    void displayTransferResult_setsWkEdQtyAndDisplaysShowThenMsg() throws Exception {
        var ws = wsField();
        ws.setWkTrQty(123);

        invoke("displayTransferResult");

        assertEquals(123L, ws.getWkEdQty());
        assertThat(screenInteractions).hasSizeGreaterThanOrEqualTo(1);
    }

    // ---------- TERM-010 ----------

    @Test
    void closeProgramFiles_closesAllFourFiles() throws Exception {
        invoke("closeProgramFiles");

        verify(stokf, times(1)).close();
        verify(smovf, times(1)).close();
        verify(prodf, times(1)).close();
        verify(whsef, times(1)).close();
    }
}
