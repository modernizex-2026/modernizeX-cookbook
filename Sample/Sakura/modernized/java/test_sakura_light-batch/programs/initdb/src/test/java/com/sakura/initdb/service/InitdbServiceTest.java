package com.sakura.initdb.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.sakura.initdb.domain.InitdbFieldAccess;
import com.sakura.initdb.io.AplfDataset;
import com.sakura.initdb.io.ArlfDataset;
import com.sakura.initdb.io.BankfDataset;
import com.sakura.initdb.io.CatgfDataset;
import com.sakura.initdb.io.CprcfDataset;
import com.sakura.initdb.io.CustfDataset;
import com.sakura.initdb.io.DeptfDataset;
import com.sakura.initdb.io.InvdfDataset;
import com.sakura.initdb.io.InvhfDataset;
import com.sakura.initdb.io.NumcfDataset;
import com.sakura.initdb.io.OrddfDataset;
import com.sakura.initdb.io.OrdhfDataset;
import com.sakura.initdb.io.PayfDataset;
import com.sakura.initdb.io.PodfDataset;
import com.sakura.initdb.io.PohfDataset;
import com.sakura.initdb.io.ProdfDataset;
import com.sakura.initdb.io.PurdfDataset;
import com.sakura.initdb.io.PurhfDataset;
import com.sakura.initdb.io.RcptfDataset;
import com.sakura.initdb.io.RcvdfDataset;
import com.sakura.initdb.io.RcvhfDataset;
import com.sakura.initdb.io.RegnfDataset;
import com.sakura.initdb.io.ShpdfDataset;
import com.sakura.initdb.io.ShphfDataset;
import com.sakura.initdb.io.SmovfDataset;
import com.sakura.initdb.io.StaffDataset;
import com.sakura.initdb.io.StokfDataset;
import com.sakura.initdb.io.SuppfDataset;
import com.sakura.initdb.io.SyscfDataset;
import com.sakura.initdb.io.TaxfDataset;
import com.sakura.initdb.io.UserfDataset;
import com.sakura.initdb.io.WhsefDataset;
import com.sakura.initdb.runtime.InitdbDatasets;
import com.sakura.runtime.DatasetEnums.FileOpenMode;
import com.sakura.runtime.io.MsgfDataset;
import com.sakura.runtime.record.RawDatasetBase;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Unit tests for InitdbService, converted from COBOL program INITDB. Ground truth: INITDB.cob
 * MAIN-000 and its LOAD-* / WRITE-* paragraphs.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Timeout(value = 10, unit = TimeUnit.SECONDS)
class InitdbServiceTest {

    private InitdbDatasets fileSet;
    private InitdbService service;
    private InitdbFieldAccess reader;

    // master / seed files (COBOL LOAD-* sections)
    private SyscfDataset syscf;
    private NumcfDataset numcf;
    private TaxfDataset taxf;
    private RegnfDataset regnf;
    private DeptfDataset deptf;
    private CatgfDataset catgf;
    private BankfDataset bankf;
    private WhsefDataset whsef;
    private StaffDataset staff;
    private UserfDataset userf;
    private CustfDataset custf;
    private SuppfDataset suppf;
    private ProdfDataset prodf;
    private CprcfDataset cprcf;
    private StokfDataset stokf;
    private MsgfDataset msgf;

    // transaction files created empty (COBOL CREATE-EMPTY-FILES section)
    private OrdhfDataset ordhf;
    private OrddfDataset orddf;
    private ShphfDataset shphf;
    private ShpdfDataset shpdf;
    private InvhfDataset invhf;
    private InvdfDataset invdf;
    private PohfDataset pohf;
    private PodfDataset podf;
    private RcvhfDataset rcvhf;
    private RcvdfDataset rcvdf;
    private PurhfDataset purhf;
    private PurdfDataset purdf;
    private ArlfDataset arlf;
    private AplfDataset aplf;
    private RcptfDataset rcptf;
    private PayfDataset payf;
    private SmovfDataset smovf;

    private final List<Object[]> stockWrites = new ArrayList<>();
    private final List<Object[]> taxWrites = new ArrayList<>();

    @BeforeEach
    void setUp() {
        syscf = spy(new SyscfDataset());
        numcf = spy(new NumcfDataset());
        taxf = spy(new TaxfDataset());
        regnf = spy(new RegnfDataset());
        deptf = spy(new DeptfDataset());
        catgf = spy(new CatgfDataset());
        bankf = spy(new BankfDataset());
        whsef = spy(new WhsefDataset());
        staff = spy(new StaffDataset());
        userf = spy(new UserfDataset());
        custf = spy(new CustfDataset());
        suppf = spy(new SuppfDataset());
        prodf = spy(new ProdfDataset());
        cprcf = spy(new CprcfDataset());
        stokf = spy(new StokfDataset());
        msgf = spy(new MsgfDataset());

        ordhf = spy(new OrdhfDataset());
        orddf = spy(new OrddfDataset());
        shphf = spy(new ShphfDataset());
        shpdf = spy(new ShpdfDataset());
        invhf = spy(new InvhfDataset());
        invdf = spy(new InvdfDataset());
        pohf = spy(new PohfDataset());
        podf = spy(new PodfDataset());
        rcvhf = spy(new RcvhfDataset());
        rcvdf = spy(new RcvdfDataset());
        purhf = spy(new PurhfDataset());
        purdf = spy(new PurdfDataset());
        arlf = spy(new ArlfDataset());
        aplf = spy(new AplfDataset());
        rcptf = spy(new RcptfDataset());
        payf = spy(new PayfDataset());
        smovf = spy(new SmovfDataset());

        fileSet = spy(new InitdbDatasets());
        doReturn(syscf).when(fileSet).getSyscf();
        doReturn(numcf).when(fileSet).getNumcf();
        doReturn(taxf).when(fileSet).getTaxf();
        doReturn(regnf).when(fileSet).getRegnf();
        doReturn(deptf).when(fileSet).getDeptf();
        doReturn(catgf).when(fileSet).getCatgf();
        doReturn(bankf).when(fileSet).getBankf();
        doReturn(whsef).when(fileSet).getWhsef();
        doReturn(staff).when(fileSet).getStaff();
        doReturn(userf).when(fileSet).getUserf();
        doReturn(custf).when(fileSet).getCustf();
        doReturn(suppf).when(fileSet).getSuppf();
        doReturn(prodf).when(fileSet).getProdf();
        doReturn(cprcf).when(fileSet).getCprcf();
        doReturn(stokf).when(fileSet).getStokf();
        doReturn(msgf).when(fileSet).getMsgf();
        doReturn(ordhf).when(fileSet).getOrdhf();
        doReturn(orddf).when(fileSet).getOrddf();
        doReturn(shphf).when(fileSet).getShphf();
        doReturn(shpdf).when(fileSet).getShpdf();
        doReturn(invhf).when(fileSet).getInvhf();
        doReturn(invdf).when(fileSet).getInvdf();
        doReturn(pohf).when(fileSet).getPohf();
        doReturn(podf).when(fileSet).getPodf();
        doReturn(rcvhf).when(fileSet).getRcvhf();
        doReturn(rcvdf).when(fileSet).getRcvdf();
        doReturn(purhf).when(fileSet).getPurhf();
        doReturn(purdf).when(fileSet).getPurdf();
        doReturn(arlf).when(fileSet).getArlf();
        doReturn(aplf).when(fileSet).getAplf();
        doReturn(rcptf).when(fileSet).getRcptf();
        doReturn(payf).when(fileSet).getPayf();
        doReturn(smovf).when(fileSet).getSmovf();

        // Stub every file's I/O so no real disk access happens; buffer()/setRecord()
        // keep running for real so field MOVE logic under test is exercised.
        List<RawDatasetBase> all =
                List.of(
                        syscf, numcf, taxf, regnf, deptf, catgf, bankf, whsef, staff, userf, custf,
                        suppf, prodf, cprcf, stokf, msgf, ordhf, orddf, shphf, shpdf, invhf, invdf,
                        pohf, podf, rcvhf, rcvdf, purhf, purdf, arlf, aplf, rcptf, payf, smovf);
        for (RawDatasetBase d : all) {
            doNothing().when(d).open(any());
            doNothing().when(d).close();
            doReturn(false).when(d).isInvalidKey();
            doNothing().when(d).write();
        }

        // Capture the STOKF loop (the one COMPUTE-derived value in the program) and
        // the two TAXF records (each write reuses the same buffer, so the state must
        // be captured at write time, not read back afterwards).
        doAnswer(
                        inv -> {
                            stockWrites.add(
                                    new Object[] {reader.getSkProd(), reader.getSkOnhand()});
                            return null;
                        })
                .when(stokf)
                .write();
        doAnswer(
                        inv -> {
                            taxWrites.add(
                                    new Object[] {
                                        reader.getTxCode(), reader.getTxRate(), reader.getTxName()
                                    });
                            return null;
                        })
                .when(taxf)
                .write();

        service = new InitdbService(fileSet);
        reader = new InitdbFieldAccess(null, fileSet);
    }

    @AfterEach
    void tearDown() {
        stockWrites.clear();
        taxWrites.clear();
    }

    @Test
    void execute_happyPath_createsAllFilesAndSeedsMasterData() {
        // Given: COBOL MAIN-000 - every LOAD-* section runs unconditionally, top to bottom.

        // When
        assertDoesNotThrow(() -> service.execute());

        // Then: normal STOP RUN completion, no COMPLETION-CODE set.
        assertEquals(0, service.getCompletionCode());

        // CREATE-EMPTY-FILES: 17 transaction files opened OUTPUT then closed, never written.
        List<RawDatasetBase> emptyFiles =
                List.of(
                        ordhf, orddf, shphf, shpdf, invhf, invdf, pohf, podf, rcvhf, rcvdf, purhf,
                        purdf, arlf, aplf, rcptf, payf, smovf);
        for (RawDatasetBase d : emptyFiles) {
            verify(d).open(FileOpenMode.OUTPUT);
            verify(d).close();
            verify(d, never()).write();
        }

        // LOAD-SYSTEM: single SYSCF record.
        verify(syscf).write();
        assertEquals(1, reader.getSyKey());
        assertThat(reader.getSyCompanyName().trim()).isEqualTo("SAKURA Trading Co., Ltd.");
        assertEquals(4, reader.getSyFiscalStart());
        assertEquals(202607, reader.getSyCurrYm());
        assertEquals(20260630, reader.getSyLastDayClose());
        assertEquals(0, new BigDecimal("0.100").compareTo(reader.getSyTaxDfltRate()));

        // LOAD-NUMBERS: 11 NM-KEY seed rows; last one written is STKMOV/SM.
        verify(numcf, times(11)).write();
        assertThat(reader.getNmKey().trim()).isEqualTo("STKMOV");
        assertThat(reader.getNmPrefix().trim()).isEqualTo("SM");
        assertEquals(10, reader.getNmWidth());

        // LOAD-TAX: 2 records, captured at write time (10% standard, 8% reduced).
        verify(taxf, times(2)).write();
        assertThat(taxWrites).hasSize(2);
        assertEquals(1, taxWrites.get(0)[0]);
        assertEquals(0, new BigDecimal("0.100").compareTo((BigDecimal) taxWrites.get(0)[1]));
        assertThat(((String) taxWrites.get(0)[2]).trim()).isEqualTo("Standard 10%");
        assertEquals(2, taxWrites.get(1)[0]);
        assertEquals(0, new BigDecimal("0.080").compareTo((BigDecimal) taxWrites.get(1)[1]));
        assertThat(((String) taxWrites.get(1)[2]).trim()).isEqualTo("Reduced 8%");

        // LOAD-REGION: 4 rows, last is Kyushu.
        verify(regnf, times(4)).write();
        assertEquals(4, reader.getRgCode());
        assertThat(reader.getRgName().trim()).isEqualTo("Kyushu (Fukuoka)");

        // LOAD-DEPT: 3 rows, last is Administration/9000.
        verify(deptf, times(3)).write();
        assertEquals(9000, reader.getDpCode());
        assertThat(reader.getDpName().trim()).isEqualTo("Administration");

        // LOAD-CATEGORY: 4 rows, last is Consumables/400, level 1.
        verify(catgf, times(4)).write();
        assertEquals(400, reader.getCtCode());
        assertThat(reader.getCtName().trim()).isEqualTo("Consumables");
        assertEquals(1, reader.getCtLevel());

        // LOAD-BANK: 3 rows, last is Mizuho Bank/3.
        verify(bankf, times(3)).write();
        assertEquals(3, reader.getBkCode());
        assertThat(reader.getBkName().trim()).isEqualTo("Mizuho Bank");
        assertThat(reader.getBkBranch().trim()).isEqualTo("Nihonbashi");

        // LOAD-WAREHOUSE: 3 rows, last is Returns Warehouse/3, type 3, manager 1001.
        verify(whsef, times(3)).write();
        assertEquals(3, reader.getWhCode());
        assertThat(reader.getWhName().trim()).isEqualTo("Returns Warehouse");
        assertEquals(3, reader.getWhType());
        assertEquals(1001, reader.getWhManager());

        // LOAD-STAFF: 4 rows, last is Kenji Sato/2001, dept 2000, Buyer.
        verify(staff, times(4)).write();
        assertEquals(2001, reader.getSfCode());
        assertThat(reader.getSfName().trim()).isEqualTo("Kenji Sato");
        assertEquals(2000, reader.getSfDept());
        assertThat(reader.getSfTitle().trim()).isEqualTo("Buyer");

        // LOAD-USER: 3 rows, last is suzuki/1002 with only ORDER+SALES authority.
        verify(userf, times(3)).write();
        assertEquals(1002, reader.getUsCode());
        assertThat(reader.getUsLogin().trim()).isEqualTo("suzuki");
        assertEquals(3, reader.getUsRole());
        assertEquals(0, reader.getUsAuthMaster());
        assertEquals(1, reader.getUsAuthOrder());
        assertEquals(1, reader.getUsAuthSales());
        assertEquals(0, reader.getUsAuthPurch());
        assertEquals(0, reader.getUsAuthClose());

        // LOAD-CUSTOMER: 5 rows, last is Tokyo Mega Mart/100005.
        verify(custf, times(5)).write();
        assertEquals(100005, reader.getCuCode());
        assertThat(reader.getCuName().trim()).isEqualTo("Tokyo Mega Mart");
        assertEquals(0, BigDecimal.valueOf(9000000).compareTo(reader.getCuCreditLimit()));
        assertEquals(1, reader.getCuTaxRound());

        // LOAD-SUPPLIER: 4 rows, last is General Consumables/200004.
        verify(suppf, times(4)).write();
        assertEquals(200004, reader.getSpCode());
        assertThat(reader.getSpName().trim()).isEqualTo("General Consumables");
        assertEquals(1, reader.getSpTaxType());

        // LOAD-PRODUCT: 8 rows, last is Filing Cabinet 3D/10000008, rank price(5)=23000.00.
        verify(prodf, times(8)).write();
        assertEquals(10000008, reader.getPrCode());
        assertThat(reader.getPrName().trim()).isEqualTo("Filing Cabinet 3D");
        assertEquals(0, new BigDecimal("23000.00").compareTo(reader.getPrRankPrice(5)));
        assertEquals(0, new BigDecimal("12000.00").compareTo(reader.getPrLastCost()));
        assertEquals(1, reader.getPrTaxCategory());
        assertEquals(1, reader.getPrDfltWhse());
        assertEquals(1, reader.getPrStockMng());

        // LOAD-CPRICE: 2 rows, last is cust 100002 / prod 10000001, open-ended (end date 0).
        verify(cprcf, times(2)).write();
        assertEquals(100002, reader.getCpCust());
        assertEquals(10000001, reader.getCpProd());
        assertEquals(0, reader.getCpEndDate());

        // LOAD-STOCK: PERFORM VARYING WK-I FROM 1 BY 1 UNTIL WK-I > 8;
        // SK-ONHAND = 1000 - WK-I * 50.
        verify(stokf, times(8)).write();
        assertThat(stockWrites).hasSize(8);
        for (int i = 1; i <= 8; i++) {
            Object[] row = stockWrites.get(i - 1);
            assertEquals(10000000 + i, row[0]);
            assertEquals(0, BigDecimal.valueOf(1000 - i * 50).compareTo((BigDecimal) row[1]));
        }

        // LOAD-MESSAGE: 4 rows, last is W0001/Stock shortage.
        verify(msgf, times(4)).write();
        assertThat(reader.getMgCode().trim()).isEqualTo("W0001");
        assertThat(reader.getMgText().trim()).isEqualTo("Stock shortage");
    }

    @Test
    void execute_invalidKeyOnWrite_isIgnoredAndProcessingContinues() {
        // Edge: COBOL WRITE ... INVALID KEY CONTINUE -> the duplicate/invalid key is
        // swallowed and the program keeps going, exactly like every other write.
        doReturn(true).when(syscf).isInvalidKey();
        doReturn(true).when(stokf).isInvalidKey();

        assertDoesNotThrow(() -> service.execute());

        assertEquals(0, service.getCompletionCode());
        verify(syscf).write();
        verify(stokf, times(8)).write();
        verify(msgf, times(4)).write();
    }

    @Test
    void execute_whenFileOpenThrows_setsCompletionCode12AndWrapsException() {
        // Error path: an unexpected exception (e.g. disk I/O failure) during
        // CREATE-EMPTY-FILES bubbles up through BatchServiceBase.execute(), which sets
        // COBOL COMPLETION-CODE = 12, rolls back and rethrows.
        doThrow(new RuntimeException("disk full")).when(ordhf).open(any());

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> service.execute());

        assertThat(thrown.getMessage()).contains("Batch processing failed");
        assertEquals(12, service.getCompletionCode());
        verify(ordhf, never()).close();
        verify(syscf, never()).open(any());
    }
}
