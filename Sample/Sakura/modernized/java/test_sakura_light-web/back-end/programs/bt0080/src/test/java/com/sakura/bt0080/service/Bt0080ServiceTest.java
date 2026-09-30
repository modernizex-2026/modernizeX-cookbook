package com.sakura.bt0080.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.sakura.abortx.service.AbortxService;
import com.sakura.bt0080.io.CustnDataset;
import com.sakura.bt0080.io.ProdnDataset;
import com.sakura.bt0080.runtime.Bt0080Datasets;
import com.sakura.dateut.service.DateutService;
import com.sakura.runtime.DatasetEnums.FileOpenMode;
import com.sakura.runtime.Utility;
import com.sakura.runtime.io.CustfDataset;
import com.sakura.runtime.io.ProdfDataset;
import com.sakura.runtime.linkage.AbortxLinkParm;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for Bt0080Service (COBOL BT0080 — reindex/rebuild reorg), generated from {@code
 * BT0080.cob} PROCEDURE DIVISION as ground truth.
 *
 * <p>Test strategy: CUSTF/PRODF/CUSTN/PRODN are real dataset objects wrapped with {@code spy()} so
 * the record buffer (and therefore {@code Bt0080FieldAccess}, which registers those buffers at
 * construction time) works exactly as in production; only I/O methods
 * (open/close/start/readNext/isAtEnd/isInvalidKey/getFileStatus/write) are stubbed so no real file
 * access happens. DATEUT and ABORTX are plain {@code @Mock}s (void no-ops) — BT0080 never inspects
 * the DATEUT result for control flow, and ABORTX is only verified for the correct KABEND fields.
 * Console input (Y/N confirm prompt) goes through the static {@code Utility.readStdinLine()} —
 * mocked via MockedStatic with CALLS_REAL_METHODS so every other Utility helper (fieldEquals, ...)
 * still runs for real.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Timeout(value = 10, unit = TimeUnit.SECONDS)
class Bt0080ServiceTest {

    @Mock private DateutService dateutService;
    @Mock private AbortxService abortxService;

    private Bt0080Datasets fileSet;
    private CustfDataset custf;
    private ProdfDataset prodf;
    private CustnDataset custn;
    private ProdnDataset prodn;

    private Bt0080Service service;

    private MockedStatic<Utility> mockedUtility;

    @BeforeEach
    void setUp() {
        Bt0080Datasets real = new Bt0080Datasets();
        fileSet = spy(real);
        custf = spy(real.getCustf());
        prodf = spy(real.getProdf());
        custn = spy(real.getCustn());
        prodn = spy(real.getProdn());
        doReturn(custf).when(fileSet).getCustf();
        doReturn(prodf).when(fileSet).getProdf();
        doReturn(custn).when(fileSet).getCustn();
        doReturn(prodn).when(fileSet).getProdn();

        for (var f : List.of(custf, prodf, custn, prodn)) {
            doNothing().when(f).open(any());
            doNothing().when(f).close();
            doReturn("00").when(f).getFileStatus();
            doReturn(false).when(f).isInvalidKey();
        }
        doReturn(true).when(custf).start(any(), any());
        doReturn(true).when(prodf).start(any(), any());
        doNothing().when(custn).write();
        doNothing().when(prodn).write();

        stubCustomers(List.of());
        stubProducts(List.of());

        mockedUtility = mockStatic(Utility.class, CALLS_REAL_METHODS);
        stubConfirm("Y");

        service = new Bt0080Service(fileSet, dateutService, abortxService);
    }

    @AfterEach
    void tearDown() {
        mockedUtility.close();
    }

    // ───────────────────────── console / fixtures ─────────────────────────

    private void stubConfirm(String confirmInput) {
        mockedUtility.when(Utility::readStdinLine).thenReturn(confirmInput);
    }

    private record CustRec(int code, int delFlag) {}

    private record ProdRec(int code, int delFlag) {}

    /**
     * Ground truth: RCU-NEXT reads sequentially; EOF-FLG set once readNext runs past the fixture
     * list.
     */
    private void stubCustomers(List<CustRec> recs) {
        AtomicInteger idx = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            int i = idx.getAndIncrement();
                            if (i < recs.size()) {
                                CustRec r = recs.get(i);
                                custf.getRecord().setInt("CU-CODE", r.code());
                                custf.getRecord().setInt("CU-DEL-FLAG", r.delFlag());
                            }
                            return true;
                        })
                .when(custf)
                .readNext();
        doAnswer(inv -> idx.get() > recs.size()).when(custf).isAtEnd();
    }

    /**
     * Ground truth: RPR-NEXT reads sequentially; EOF-FLG set once readNext runs past the fixture
     * list.
     */
    private void stubProducts(List<ProdRec> recs) {
        AtomicInteger idx = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            int i = idx.getAndIncrement();
                            if (i < recs.size()) {
                                ProdRec r = recs.get(i);
                                prodf.getRecord().setInt("PR-CODE", r.code());
                                prodf.getRecord().setInt("PR-DEL-FLAG", r.delFlag());
                            }
                            return true;
                        })
                .when(prodf)
                .readNext();
        doAnswer(inv -> idx.get() > recs.size()).when(prodf).isAtEnd();
    }

    private ArgumentCaptor<AbortxLinkParm> captureAbortx() {
        ArgumentCaptor<AbortxLinkParm> captor = ArgumentCaptor.forClass(AbortxLinkParm.class);
        verify(abortxService).execute(captor.capture());
        return captor;
    }

    // ───────────────────────── happy path (ground truth: RCU-010/RCUX-010, RPR-010/RPRX-010)
    // ─────────────────────────

    @Test
    void execute_confirmYesCleanRecords_copiesAllAndCompletionCodeZero() {
        stubCustomers(List.of(new CustRec(100, 0), new CustRec(200, 0)));
        stubProducts(List.of(new ProdRec(10, 0)));

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(0);
        verify(abortxService, never()).execute(any());
        verify(custf).open(FileOpenMode.INPUT);
        verify(custn).open(FileOpenMode.OUTPUT);
        verify(custn, times(2)).write();
        verify(prodn, times(1)).write();
        verify(custf, times(1)).close();
        verify(custn, times(1)).close();
        verify(prodf, times(1)).close();
        verify(prodn, times(1)).close();
    }

    @Test
    void execute_confirmYesWithDeletedRecords_skipsDeletedIncrementsSkipCounters() {
        stubCustomers(List.of(new CustRec(100, 0), new CustRec(101, 1)));
        stubProducts(List.of(new ProdRec(10, 1), new ProdRec(20, 0)));

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(0);
        verify(custn, times(1)).write();
        verify(prodn, times(1)).write();
    }

    @Test
    void execute_confirmLowercaseY_stillProceedsToReorgProcessing() {
        stubConfirm("y");
        stubCustomers(List.of(new CustRec(100, 0)));

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(0);
        verify(custn, times(1)).write();
        verify(custf, times(1)).open(any());
    }

    @Test
    void execute_emptyCustfAndProdf_zeroCountsCompletionZero() {
        // CUSTF/PRODF START INVALID KEY (empty files) — default doReturn(false).isInvalidKey()
        // means EOF is signalled immediately by the isAtEnd() stub above (idx 0 > size 0).
        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(0);
        verify(custn, never()).write();
        verify(prodn, never()).write();
        verify(custf, times(1)).close();
        verify(prodf, times(1)).close();
    }

    @Test
    void execute_custfStartInvalidKey_treatsAsEmptyFileWithoutAbort() {
        doReturn(true).when(custf).isInvalidKey();
        stubProducts(List.of(new ProdRec(10, 0)));

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(0);
        verify(custf, never()).readNext();
        verify(custn, never()).write();
        verify(prodn, times(1)).write();
        verify(abortxService, never()).execute(any());
    }

    // ───────────────────────── confirm cancellation (ground truth: CONF-010)
    // ─────────────────────────

    @Test
    void execute_confirmNo_cancelsRunWithoutOpeningAnyFile() {
        stubConfirm("N");

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(0);
        verify(custf, never()).open(any());
        verify(prodf, never()).open(any());
        verify(abortxService, never()).execute(any());
    }

    @Test
    void execute_confirmBlankInput_treatedAsCancelled() {
        stubConfirm("");

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(0);
        verify(custf, never()).open(any());
    }

    // ───────────────────────── CUSTF open/read/write error paths (ground truth:
    // RCU-010/RCUX-010/ABND-010) ─────────────────────────

    @Test
    void execute_openCustfFails_abortsWithCustfDetail() {
        doReturn("99").when(custf).getFileStatus();

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        ArgumentCaptor<AbortxLinkParm> captor = captureAbortx();
        AbortxLinkParm.Kabend kabend = captor.getValue().getKabend();
        assertThat(kabend.getKaProgid().trim()).isEqualTo("BT0080");
        assertThat(kabend.getKaMsgcode().trim()).isEqualTo("EBATCH");
        assertThat(kabend.getKaFsts().trim()).isEqualTo("99");
        assertThat(kabend.getKaFile().trim()).isEqualTo("CUSTF");
        assertThat(kabend.getKaDetail().trim()).isEqualTo("Open CUSTF failed");
        verify(prodf, never()).open(any());
    }

    @Test
    void execute_openCustnFails_abortsWithCustnDetail() {
        doReturn("99").when(custn).getFileStatus();

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        ArgumentCaptor<AbortxLinkParm> captor = captureAbortx();
        AbortxLinkParm.Kabend kabend = captor.getValue().getKabend();
        assertThat(kabend.getKaFile().trim()).isEqualTo("CUSTN");
        assertThat(kabend.getKaDetail().trim()).isEqualTo("Open reorg CUSTF.RGN failed");
        verify(prodf, never()).open(any());
    }

    @Test
    void execute_readNextCustfFails_abortsWithReadDetail() {
        // 1st getFileStatus() call = OPEN CUSTF check (must pass); 2nd = post-READ-NEXT check
        // (fails).
        doReturn("00", "99").when(custf).getFileStatus();
        stubCustomers(List.of(new CustRec(100, 0)));

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        ArgumentCaptor<AbortxLinkParm> captor = captureAbortx();
        AbortxLinkParm.Kabend kabend = captor.getValue().getKabend();
        assertThat(kabend.getKaFile().trim()).isEqualTo("CUSTF");
        assertThat(kabend.getKaDetail().trim()).isEqualTo("READ NEXT CUSTF failed");
    }

    @Test
    void execute_writeCustnFails_abortsWithWriteDetail() {
        // 1st getFileStatus() call = OPEN CUSTN check (must pass); 2nd = post-WRITE check (fails).
        doReturn("00", "99").when(custn).getFileStatus();
        stubCustomers(List.of(new CustRec(100, 0)));

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        ArgumentCaptor<AbortxLinkParm> captor = captureAbortx();
        AbortxLinkParm.Kabend kabend = captor.getValue().getKabend();
        assertThat(kabend.getKaFile().trim()).isEqualTo("CUSTN");
        assertThat(kabend.getKaDetail().trim()).isEqualTo("WRITE reorg CUSTF.RGN failed");
    }

    // ───────────────────────── PRODF open/read/write error paths (ground truth:
    // RPR-010/RPRX-010/ABND-010) ─────────────────────────

    @Test
    void execute_openProdfFails_abortsWithProdfDetail() {
        doReturn("99").when(prodf).getFileStatus();

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        ArgumentCaptor<AbortxLinkParm> captor = captureAbortx();
        AbortxLinkParm.Kabend kabend = captor.getValue().getKabend();
        assertThat(kabend.getKaFile().trim()).isEqualTo("PRODF");
        assertThat(kabend.getKaDetail().trim()).isEqualTo("Open PRODF failed");
        verify(prodn, never()).open(any());
    }

    @Test
    void execute_openProdnFails_abortsWithProdnDetail() {
        doReturn("99").when(prodn).getFileStatus();

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        ArgumentCaptor<AbortxLinkParm> captor = captureAbortx();
        AbortxLinkParm.Kabend kabend = captor.getValue().getKabend();
        assertThat(kabend.getKaFile().trim()).isEqualTo("PRODN");
        assertThat(kabend.getKaDetail().trim()).isEqualTo("Open reorg PRODF.RGN failed");
    }

    @Test
    void execute_readNextProdfFails_abortsWithReadDetail() {
        // 1st getFileStatus() call = OPEN PRODF check (must pass); 2nd = post-READ-NEXT check
        // (fails).
        doReturn("00", "99").when(prodf).getFileStatus();
        stubProducts(List.of(new ProdRec(10, 0)));

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        ArgumentCaptor<AbortxLinkParm> captor = captureAbortx();
        AbortxLinkParm.Kabend kabend = captor.getValue().getKabend();
        assertThat(kabend.getKaFile().trim()).isEqualTo("PRODF");
        assertThat(kabend.getKaDetail().trim()).isEqualTo("READ NEXT PRODF failed");
    }

    @Test
    void execute_writeProdnFails_abortsWithWriteDetail() {
        // 1st getFileStatus() call = OPEN PRODN check (must pass); 2nd = post-WRITE check (fails).
        doReturn("00", "99").when(prodn).getFileStatus();
        stubProducts(List.of(new ProdRec(10, 0)));

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        ArgumentCaptor<AbortxLinkParm> captor = captureAbortx();
        AbortxLinkParm.Kabend kabend = captor.getValue().getKabend();
        assertThat(kabend.getKaFile().trim()).isEqualTo("PRODN");
        assertThat(kabend.getKaDetail().trim()).isEqualTo("WRITE reorg PRODF.RGN failed");
    }
}
