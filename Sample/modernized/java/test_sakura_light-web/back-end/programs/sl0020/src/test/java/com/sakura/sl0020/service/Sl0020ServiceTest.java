package com.sakura.sl0020.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.runtime.DatasetEnums.FileOpenMode;
import com.sakura.runtime.ScreenModels.ScreenDef;
import com.sakura.runtime.ScreenRendererInstance;
import com.sakura.runtime.io.CustfDataset;
import com.sakura.runtime.io.InvdfDataset;
import com.sakura.runtime.io.InvhfDataset;
import com.sakura.runtime.io.ProdfDataset;
import com.sakura.sl0020.domain.Sl0020FieldAccess;
import com.sakura.sl0020.domain.WorkingStorage;
import com.sakura.sl0020.runtime.Sl0020Datasets;
import com.sakura.sl0020.screen.ScreenDefs;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Unit tests for {@link Sl0020Service}, generated from COBOL SL0020 (Sales Invoice Inquiry). Ground
 * truth for input/expected values is SL0020.cob PROCEDURE DIVISION.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Timeout(value = 10, unit = TimeUnit.SECONDS)
class Sl0020ServiceTest {

    @Mock private DateutService dateutService;
    @Mock private AbortxService abortxService;
    @Mock private ScreenRendererInstance renderer;

    private InvhfDataset invhfSpy;
    private InvdfDataset invdfSpy;
    private CustfDataset custfSpy;
    private ProdfDataset prodfSpy;
    private Sl0020Datasets fileSet;
    private Sl0020FieldAccess arrangeWs;

    private final List<String> screenInteractions = new ArrayList<>();
    private final List<String> msgLineHistory = new ArrayList<>();
    // WK-CUST-NAME is reset to blank by CLEAR-SEARCH on the next MAIN-RTN cycle, so it
    // must be snapshotted (by value, not by ws reference) at the moment DS-INV paints.
    private final List<String> custNameAtInvDisplay = new ArrayList<>();
    private final AtomicReference<Sl0020FieldAccess> capturedWs = new AtomicReference<>();

    @BeforeEach
    void setUp() {
        invhfSpy = spy(new InvhfDataset());
        invdfSpy = spy(new InvdfDataset());
        custfSpy = spy(new CustfDataset());
        prodfSpy = spy(new ProdfDataset());

        Sl0020Datasets realFileSet = new Sl0020Datasets();
        fileSet = spy(realFileSet);
        doReturn(invhfSpy).when(fileSet).getInvhf();
        doReturn(invdfSpy).when(fileSet).getInvdf();
        doReturn(custfSpy).when(fileSet).getCustf();
        doReturn(prodfSpy).when(fileSet).getProdf();

        // Default: open/close succeed with no real file I/O; file status stays "00".
        doNothing().when(invhfSpy).open(any());
        doNothing().when(invdfSpy).open(any());
        doNothing().when(custfSpy).open(any());
        doNothing().when(prodfSpy).open(any());
        doNothing().when(invhfSpy).close();
        doNothing().when(invdfSpy).close();
        doNothing().when(custfSpy).close();
        doNothing().when(prodfSpy).close();

        // Default: no record found for any keyed read; overridden per-test.
        doReturn(false).when(invhfSpy).readByKey(any());
        doReturn(true).when(invhfSpy).isInvalidKey();
        doReturn(false).when(custfSpy).readByKey(any());
        doReturn(true).when(custfSpy).isInvalidKey();
        doReturn(false).when(prodfSpy).readByKey(any());
        doReturn(true).when(prodfSpy).isInvalidKey();
        doReturn(false).when(invhfSpy).start(any(), any());
        doReturn(false).when(invdfSpy).start(any(), any());
        doReturn(false).when(invdfSpy).isAtEnd();
        doReturn(false).when(invhfSpy).isAtEnd();

        // Working-storage accessor sharing the SAME record buffers as the service's
        // internal ws, so tests can arrange record field values before execute().
        arrangeWs = new Sl0020FieldAccess(new WorkingStorage(), fileSet);

        // acceptField default: no operator input typed -> empty string for every field.
        doReturn("").when(renderer).acceptField(any());

        // Capture every displayScreen call: which screen, and (for DS-MSG) the message text.
        doAnswer(
                        inv -> {
                            ScreenDef def = inv.getArgument(0);
                            Sl0020FieldAccess fields = (Sl0020FieldAccess) inv.getArgument(1);
                            capturedWs.set(fields);
                            screenInteractions.add(
                                    "displayScreen:" + (def != null ? def.name : null));
                            if (def != null && "DS-MSG".equals(def.name)) {
                                msgLineHistory.add(fields.getWkMsgLine().trim());
                            }
                            if (def != null && "DS-INV".equals(def.name)) {
                                custNameAtInvDisplay.add(fields.getWkCustName().trim());
                            }
                            return null;
                        })
                .when(renderer)
                .displayScreen(any(), any());
    }

    @AfterEach
    void tearDown() {
        screenInteractions.clear();
        msgLineHistory.clear();
        custNameAtInvDisplay.clear();
        capturedWs.set(null);
    }

    private Sl0020Service newService() {
        return new Sl0020Service(fileSet, dateutService, abortxService, renderer);
    }

    private void stubEndStatusSequence(String... statuses) {
        org.mockito.stubbing.OngoingStubbing<String> stubbing = null;
        for (String s : statuses) {
            stubbing =
                    (stubbing == null)
                            ? org.mockito.Mockito.when(renderer.readEndStatus()).thenReturn(s)
                            : stubbing.thenReturn(s);
        }
    }

    private void stubAcceptField(String fieldName, String value) {
        doReturn(value).when(renderer).acceptField(ScreenDefs.getInput(fieldName));
    }

    // ── INIT-RTN / OPEN-FILES ────────────────────────────────────────────

    @Test
    void execute_pf3AtMainScreen_opensAllFilesShowsPromptAndClosesCleanly() {
        stubEndStatusSequence("03");

        Sl0020Service service = newService();
        service.execute();

        verify(invhfSpy, times(1)).open(FileOpenMode.INPUT);
        verify(invdfSpy, times(1)).open(FileOpenMode.INPUT);
        verify(custfSpy, times(1)).open(FileOpenMode.INPUT);
        verify(prodfSpy, times(1)).open(FileOpenMode.INPUT);
        verify(invhfSpy, times(1)).close();
        verify(invdfSpy, times(1)).close();
        verify(custfSpy, times(1)).close();
        verify(prodfSpy, times(1)).close();
        verify(dateutService, times(1)).execute(any());
        assertThat(service.getCompletionCode()).isEqualTo(0);
        assertThat(screenInteractions)
                .contains("displayScreen:DS-HEADER", "displayScreen:DS-FOOTER");
        assertThat(msgLineHistory).contains("Enter invoice number or customer, then ENTER");
    }

    @Test
    void execute_invhfOpenFails_abendsAbortsAndSkipsRemainingOpens() {
        doReturn("91").when(invhfSpy).getFileStatus();

        Sl0020Service service = newService();
        service.execute();

        verify(abortxService, times(1)).execute(any());
        verify(invdfSpy, never()).open(any());
        verify(custfSpy, never()).open(any());
        verify(prodfSpy, never()).open(any());
        assertThat(service.getCompletionCode()).isEqualTo(255);
    }

    // ── MAIN-RTN / PROCESS-KEY dispatch ──────────────────────────────────

    @Test
    void execute_invalidFunctionKeyThenPf3_showsInvalidKeyMessage() {
        stubEndStatusSequence("09", "03");

        Sl0020Service service = newService();
        service.execute();

        assertThat(msgLineHistory).contains("Invalid function key");
    }

    @Test
    void execute_enterWithNoSelectionEntered_showsEnterSelectionMessage() {
        stubEndStatusSequence("00", "03");

        Sl0020Service service = newService();
        service.execute();

        assertThat(msgLineHistory).contains("Enter an invoice number or a customer");
    }

    // ── FIND-BY-NUMBER ────────────────────────────────────────────────────

    @Test
    void execute_findByNumberFoundAndNotDeleted_entersBrowseLoop() {
        arrangeWs.setIhDelFlag(0);
        arrangeWs.setIhCust(500);
        arrangeWs.setIhAmount(new BigDecimal("1000"));
        arrangeWs.setIhCostTotal(new BigDecimal("400"));
        arrangeWs.setIhKind(1);
        arrangeWs.setIhStatus(1);
        doReturn(false).when(invhfSpy).isInvalidKey();
        doReturn(true).when(custfSpy).isInvalidKey(); // customer lookup for header text: not found

        stubAcceptField("WK-SEL-NO", "1001");
        // Main cycle ESTS=00 (find by number) -> browse loop ESTS=03 (exit browse)
        // -> next main cycle ESTS=03 (program end).
        stubEndStatusSequence("00", "03", "03");

        Sl0020Service service = newService();
        service.execute();

        verify(invhfSpy, times(1)).readByKey(any());
        Sl0020FieldAccess ws = capturedWs.get();
        assertThat(ws.getWkKindText().trim()).isEqualTo("Sale");
        assertThat(ws.getWkStatText().trim()).isEqualTo("Posted");
        assertThat(ws.getWkMarginTot()).isEqualTo(600L);
        assertThat(custNameAtInvDisplay).contains("??? unknown customer");
        assertThat(screenInteractions).contains("displayScreen:DS-INV", "displayScreen:DS-BROWSE");
    }

    @Test
    void execute_findByNumberNotFound_showsNotFoundMessageAndSkipsBrowse() {
        doReturn(true).when(invhfSpy).isInvalidKey();
        stubAcceptField("WK-SEL-NO", "9999");
        stubEndStatusSequence("00", "03");

        Sl0020Service service = newService();
        service.execute();

        assertThat(msgLineHistory).contains("Invoice number not found");
        assertThat(screenInteractions).doesNotContain("displayScreen:DS-BROWSE");
    }

    @Test
    void execute_findByNumberDeleted_showsDeletedMessageAndSkipsBrowse() {
        arrangeWs.setIhDelFlag(1);
        doReturn(false).when(invhfSpy).isInvalidKey();
        stubAcceptField("WK-SEL-NO", "1001");
        stubEndStatusSequence("00", "03");

        Sl0020Service service = newService();
        service.execute();

        assertThat(msgLineHistory).contains("Invoice is deleted");
        assertThat(screenInteractions).doesNotContain("displayScreen:DS-BROWSE");
    }

    // ── FIND-BY-CUSTOMER / READ-NEXT-LIVE ────────────────────────────────

    @Test
    void execute_findByCustomerFound_locatesFirstLiveInvoiceAndEntersBrowse() {
        arrangeWs.setCuName("ACME Corp");
        doReturn(false).when(custfSpy).isInvalidKey();
        doReturn(false).when(invhfSpy).start(any(), any());
        doReturn(false).when(invhfSpy).isInvalidKey();

        // First READ NEXT hits a deleted record, second hits a live one matching the customer.
        // READ-NEXT-LIVE's loop checks isAtEnd() TWICE per iteration (AT END / NOT AT END are
        // two separate "if"s in the converted code) -- a fixed Mockito call-sequence would
        // desync across the two checks, so key isAtEnd() off a flag the readNext() stub owns.
        arrangeWs.setIhCust(500);
        boolean[] atEnd = {false};
        doAnswer(
                        inv -> {
                            arrangeWs.setIhDelFlag(1);
                            return true;
                        })
                .doAnswer(
                        inv -> {
                            arrangeWs.setIhDelFlag(0);
                            return true;
                        })
                .when(invhfSpy)
                .readNext();
        doAnswer(inv -> atEnd[0]).when(invhfSpy).isAtEnd();

        stubAcceptField("WK-SEL-CUST", "500");
        stubEndStatusSequence("00", "03", "03");

        Sl0020Service service = newService();
        service.execute();

        verify(invhfSpy, times(2)).readNext();
        assertThat(custNameAtInvDisplay).contains("ACME Corp");
        assertThat(screenInteractions).contains("displayScreen:DS-BROWSE");
    }

    @Test
    void execute_findByCustomerNotFound_showsCustomerNotFoundMessage() {
        doReturn(true).when(custfSpy).isInvalidKey();
        stubAcceptField("WK-SEL-CUST", "999");
        stubEndStatusSequence("00", "03");

        Sl0020Service service = newService();
        service.execute();

        assertThat(msgLineHistory).contains("Customer not found");
    }

    @Test
    void execute_findByCustomerStartInvalidKey_showsNoInvoicesMessage() {
        doReturn(false).when(custfSpy).isInvalidKey();
        doReturn(true).when(invhfSpy).isInvalidKey(); // START ... INVALID KEY
        stubAcceptField("WK-SEL-CUST", "500");
        stubEndStatusSequence("00", "03");

        Sl0020Service service = newService();
        service.execute();

        assertThat(msgLineHistory).contains("No invoices for this customer");
        verify(invhfSpy, never()).readNext();
    }

    @Test
    void execute_findByCustomerReadNextHitsDifferentCustomer_showsNoInvoicesMessage() {
        doReturn(false).when(custfSpy).isInvalidKey();
        doReturn(false).when(invhfSpy).isInvalidKey();
        arrangeWs.setIhCust(999); // record found belongs to a different customer
        doReturn(true).when(invhfSpy).readNext();
        doReturn(false, true).when(invhfSpy).isAtEnd();

        stubAcceptField("WK-SEL-CUST", "500");
        stubEndStatusSequence("00", "03");

        Sl0020Service service = newService();
        service.execute();

        assertThat(msgLineHistory).contains("No invoices for this customer");
    }

    // ── LOAD-DETAILS / LOOKUP-PRODUCT / CALC-PAGES / BUILD-WINDOW ────────

    @Test
    void execute_invoiceWithDetailLines_computesPageCountAndWindowRows() {
        arrangeWs.setIhDelFlag(0);
        doReturn(false).when(invhfSpy).isInvalidKey();
        doReturn(false).when(invdfSpy).start(any(), any());

        // Two detail lines: first product found in PRODF, second not found.
        int[] callCount = {0};
        doAnswer(
                        inv -> {
                            callCount[0]++;
                            if (callCount[0] == 1) {
                                arrangeWs.setIdLine(10);
                                arrangeWs.setIdProd(100);
                                arrangeWs.setIdQty(new BigDecimal("5"));
                                arrangeWs.setIdAmount(new BigDecimal("200"));
                                arrangeWs.setIdCostAmount(new BigDecimal("80"));
                            } else {
                                arrangeWs.setIdLine(20);
                                arrangeWs.setIdProd(200);
                                arrangeWs.setIdQty(new BigDecimal("1"));
                                arrangeWs.setIdAmount(new BigDecimal("50"));
                                arrangeWs.setIdCostAmount(new BigDecimal("30"));
                            }
                            return true;
                        })
                .when(invdfSpy)
                .readNext();
        doReturn(false, false, true).when(invdfSpy).isAtEnd();
        // ID-NO must equal WK-CUR-NO (IH-NO) on both rows for the loop to accept them.
        arrangeWs.setIdNo(0);
        doAnswer(
                        inv -> {
                            arrangeWs.setIdNo(arrangeWs.getIhNo());
                            return true;
                        })
                .when(invhfSpy)
                .readByKey(any());

        // lookupProductName() checks isInvalidKey() TWICE per call (INVALID KEY / NOT
        // INVALID KEY are two separate "if"s in the converted code, not one branch) -- a
        // fixed Mockito call-sequence would desync across the two checks, so key the
        // answer off the current PR-CODE (stable within one lookupProductName call)
        // instead: product 100 (row 1) is not found, product 200 (row 2) is found.
        doAnswer(inv -> arrangeWs.getPrCode() == 100).when(prodfSpy).isInvalidKey();
        arrangeWs.setPrName("Widget");

        stubAcceptField("WK-SEL-NO", "1001");
        stubEndStatusSequence("00", "03", "03");

        Sl0020Service service = newService();
        service.execute();

        Sl0020FieldAccess ws = capturedWs.get();
        assertThat(ws.getWkDcnt()).isEqualTo(2);
        assertThat(ws.getWkPageCnt()).isEqualTo(1); // (2 + 11 - 1) / 11 = 1
        assertThat(ws.getWwLine(1)).isEqualTo(10);
        assertThat(ws.getWwName(1).trim()).isEqualTo("??");
        assertThat(ws.getWwLine(2)).isEqualTo(20);
        assertThat(ws.getWwName(2).trim()).isEqualTo("Widget");
        // Slot 3 has no backing detail row (WK-IDX2 > WK-DCNT) -> zeroed out.
        assertThat(ws.getWwLine(3)).isEqualTo(0);
        assertThat(ws.getWwName(3).trim()).isEmpty();
    }

    @Test
    void execute_invoiceWithNoDetailLines_pageCountIsOne() {
        arrangeWs.setIhDelFlag(0);
        doReturn(false).when(invhfSpy).isInvalidKey();
        doReturn(true).when(invdfSpy).isInvalidKey(); // START ... INVALID KEY -> LDD-999 directly

        stubAcceptField("WK-SEL-NO", "1001");
        stubEndStatusSequence("00", "03", "03");

        Sl0020Service service = newService();
        service.execute();

        Sl0020FieldAccess ws = capturedWs.get();
        assertThat(ws.getWkDcnt()).isEqualTo(0);
        assertThat(ws.getWkPageCnt()).isEqualTo(1);
        verify(invdfSpy, never()).readNext();
    }

    // ── SET-TEXTS branches ────────────────────────────────────────────────

    @Test
    void execute_invoiceKindReturnAndStatusCancelled_resolvesCorrectText() {
        arrangeWs.setIhDelFlag(0);
        arrangeWs.setIhKind(2);
        arrangeWs.setIhStatus(9);
        doReturn(false).when(invhfSpy).isInvalidKey();

        stubAcceptField("WK-SEL-NO", "1001");
        stubEndStatusSequence("00", "03", "03");

        Sl0020Service service = newService();
        service.execute();

        Sl0020FieldAccess ws = capturedWs.get();
        assertThat(ws.getWkKindText().trim()).isEqualTo("Return");
        assertThat(ws.getWkStatText().trim()).isEqualTo("Cancelled");
    }

    @Test
    void execute_invoiceKindAndStatusUnknown_resolvesDefaultText() {
        arrangeWs.setIhDelFlag(0);
        arrangeWs.setIhKind(9);
        arrangeWs.setIhStatus(5);
        doReturn(false).when(invhfSpy).isInvalidKey();

        stubAcceptField("WK-SEL-NO", "1001");
        stubEndStatusSequence("00", "03", "03");

        Sl0020Service service = newService();
        service.execute();

        Sl0020FieldAccess ws = capturedWs.get();
        assertThat(ws.getWkKindText().trim()).isEqualTo("?");
        assertThat(ws.getWkStatText().trim()).isEqualTo("Unknown");
    }

    // ── BROWSE-LOOP: PAGE-DOWN / PAGE-UP boundaries ──────────────────────

    @Test
    void execute_pageDownAtLastPage_showsAlreadyAtLastPageMessage() {
        arrangeWs.setIhDelFlag(0);
        doReturn(false).when(invhfSpy).isInvalidKey();
        doReturn(true).when(invdfSpy).isInvalidKey(); // no detail rows -> WK-DCNT stays 0

        stubAcceptField("WK-SEL-NO", "1001");
        // main ESTS=00 (find) -> browse ESTS=00 (ENTER -> PAGE-DOWN) -> browse ESTS=03 (exit) ->
        // main ESTS=03 (end)
        stubEndStatusSequence("00", "00", "03", "03");

        Sl0020Service service = newService();
        service.execute();

        assertThat(msgLineHistory).contains("Already at last page - PF6 for next invoice");
    }

    @Test
    void execute_pageUpAtFirstPage_showsAlreadyAtFirstPageMessage() {
        arrangeWs.setIhDelFlag(0);
        doReturn(false).when(invhfSpy).isInvalidKey();
        doReturn(true).when(invdfSpy).isInvalidKey();

        stubAcceptField("WK-SEL-NO", "1001");
        // browse ESTS="12" -> PAGE-UP, WK-PAGE-TOP starts at 1 -> "already at first page"
        stubEndStatusSequence("00", "12", "03", "03");

        Sl0020Service service = newService();
        service.execute();

        assertThat(msgLineHistory).contains("Already at first page");
    }

    // ── NEXT-INVOICE / PREV-INVOICE ───────────────────────────────────────

    @Test
    void execute_nextInvoiceNoFurtherInvoices_showsNoFurtherInvoicesMessage() {
        arrangeWs.setIhDelFlag(0);
        doReturn(false).when(invhfSpy).isInvalidKey();
        doReturn(true).when(invdfSpy).isInvalidKey();
        doReturn(true).when(invhfSpy).readNext();
        doReturn(true).when(invhfSpy).isAtEnd(); // READ-NEXT-LIVE hits EOF immediately

        stubAcceptField("WK-SEL-NO", "1001");
        // main ESTS=00 (find) -> browse ESTS="06" (NEXT-INVOICE) -> browse ESTS=03 -> main ESTS=03
        stubEndStatusSequence("00", "06", "03", "03");

        Sl0020Service service = newService();
        service.execute();

        assertThat(msgLineHistory).contains("No further invoices");
    }

    @Test
    void execute_prevInvoiceWithNoHistory_showsNoPreviousInvoiceMessage() {
        arrangeWs.setIhDelFlag(0);
        doReturn(false).when(invhfSpy).isInvalidKey();
        doReturn(true).when(invdfSpy).isInvalidKey();

        stubAcceptField("WK-SEL-NO", "1001");
        // WK-HPOS is 1 right after INIT-HISTORY, so PREV-INVOICE immediately reports none.
        stubEndStatusSequence("00", "05", "03", "03");

        Sl0020Service service = newService();
        service.execute();

        assertThat(msgLineHistory).contains("No previous invoice in this browse");
    }
}
