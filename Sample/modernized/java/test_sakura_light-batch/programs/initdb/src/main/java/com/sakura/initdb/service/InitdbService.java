package com.sakura.initdb.service;

import com.sakura.initdb.domain.InitdbFieldAccess;
import com.sakura.initdb.domain.WorkingStorage;
import com.sakura.initdb.runtime.InitdbDatasets;
import com.sakura.runtime.BatchServiceBase;
import com.sakura.runtime.DatasetEnums.*;
import com.sakura.runtime.StopRunSignal;
import com.sakura.runtime.io.AbstractDatasets;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/** Business logic service generated from COBOL program INITDB. */
@Service
@Scope("prototype")
public class InitdbService extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final InitdbDatasets fileSet;

    private final InitdbFieldAccess ws;

    public InitdbService(InitdbDatasets fileSet) {
        this.fileSet = fileSet;
        this.ws = new InitdbFieldAccess(new WorkingStorage(), fileSet);
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
        return fileSet;
    }

    /** COBOL paragraph: MAIN-000 */
    private void runMainProgram() {
        log.info("SAKURA-SMS  INITDB - creating files ...");
        runChain(this::createEmptyFiles);
        runChain(this::loadSystemControlFile);
        runChain(this::loadNumberControlFile);
        runChain(this::loadTaxFile);
        runChain(this::loadRegionFile);
        runChain(this::loadDepartmentFile);
        runChain(this::loadCategoryFile);
        runChain(this::loadBankFile);
        runChain(this::loadWarehouseFile);
        runChain(this::loadStaffFile);
        runChain(this::loadUserFile);
        runChain(this::loadCustomerFile);
        runChain(this::loadSupplierFile);
        runChain(this::loadProductFile);
        runChain(this::loadCustomerPriceFile);
        runChain(this::loadStockFile);
        runChain(this::loadMessageFile);
        log.info("SAKURA-SMS  INITDB - complete.");
        throw new StopRunSignal();
    }

    /** COBOL paragraph: CEF-010 */
    private void createEmptyFiles() {
        fileSet.getOrdhf().open(FileOpenMode.OUTPUT);
        fileSet.getOrdhf().close();
        fileSet.getOrddf().open(FileOpenMode.OUTPUT);
        fileSet.getOrddf().close();
        fileSet.getShphf().open(FileOpenMode.OUTPUT);
        fileSet.getShphf().close();
        fileSet.getShpdf().open(FileOpenMode.OUTPUT);
        fileSet.getShpdf().close();
        fileSet.getInvhf().open(FileOpenMode.OUTPUT);
        fileSet.getInvhf().close();
        fileSet.getInvdf().open(FileOpenMode.OUTPUT);
        fileSet.getInvdf().close();
        fileSet.getPohf().open(FileOpenMode.OUTPUT);
        fileSet.getPohf().close();
        fileSet.getPodf().open(FileOpenMode.OUTPUT);
        fileSet.getPodf().close();
        fileSet.getRcvhf().open(FileOpenMode.OUTPUT);
        fileSet.getRcvhf().close();
        fileSet.getRcvdf().open(FileOpenMode.OUTPUT);
        fileSet.getRcvdf().close();
        fileSet.getPurhf().open(FileOpenMode.OUTPUT);
        fileSet.getPurhf().close();
        fileSet.getPurdf().open(FileOpenMode.OUTPUT);
        fileSet.getPurdf().close();
        fileSet.getArlf().open(FileOpenMode.OUTPUT);
        fileSet.getArlf().close();
        fileSet.getAplf().open(FileOpenMode.OUTPUT);
        fileSet.getAplf().close();
        fileSet.getRcptf().open(FileOpenMode.OUTPUT);
        fileSet.getRcptf().close();
        fileSet.getPayf().open(FileOpenMode.OUTPUT);
        fileSet.getPayf().close();
        fileSet.getSmovf().open(FileOpenMode.OUTPUT);
        fileSet.getSmovf().close();
    }

    /** COBOL paragraph: LSYS-010 */
    private void loadSystemControlFile() {
        fileSet.getSyscf().open(FileOpenMode.OUTPUT);
        fileSet.getSyscf().setRecord();
        ws.setSyKey(1);
        ws.setSyCompanyName("SAKURA Trading Co., Ltd.");
        ws.setSyCompanyZip("100-0001");
        ws.setSyCompanyAddr("1-1-1 Chiyoda, Chiyoda-ku, Tokyo");
        ws.setSyCompanyTel("03-1234-5678");
        ws.setSyFiscalStart(4);
        ws.setSyCurrYm(202607);
        ws.setSyLastDayClose(20260630);
        ws.setSyLastMonClose(202606);
        ws.setSyTaxDfltRate(new BigDecimal("0.100"));
        ws.setSyDecRound(1);
        fileSet.getSyscf().write();
        if (fileSet.getSyscf().isInvalidKey()) {
            /* CONTINUE */
        }
        fileSet.getSyscf().close();
    }

    /** COBOL paragraph: LNUM-010 */
    private void loadNumberControlFile() {
        fileSet.getNumcf().open(FileOpenMode.OUTPUT);
        runChain(this::seedNumberControlRecords);
        fileSet.getNumcf().close();
    }

    /** COBOL paragraph: WNUM-010 */
    private void seedNumberControlRecords() {
        writeNumRecord("ORDER", "SO");
        writeNumRecord("INVOICE", "IV");
        writeNumRecord("SHIP", "SH");
        writeNumRecord("PO", "PO");
        writeNumRecord("RECV", "RC");
        writeNumRecord("PURCH", "PU");
        writeNumRecord("RECEIPT", "RE");
        writeNumRecord("PAYMENT", "PY");
        writeNumRecord("ARLDG", "AL");
        writeNumRecord("APLDG", "PL");
        writeNumRecord("STKMOV", "SM");
    }

    /** COBOL paragraph: LTAX-010 */
    private void loadTaxFile() {
        fileSet.getTaxf().open(FileOpenMode.OUTPUT);
        fileSet.getTaxf().setRecord();
        ws.setTxCode(1);
        ws.setTxStartDate(20191001);
        ws.setTxRate(new BigDecimal("0.100"));
        ws.setTxName("Standard 10%");
        fileSet.getTaxf().write();
        if (fileSet.getTaxf().isInvalidKey()) {
            /* CONTINUE */
        }
        fileSet.getTaxf().setRecord();
        ws.setTxCode(2);
        ws.setTxStartDate(20191001);
        ws.setTxRate(new BigDecimal("0.080"));
        ws.setTxName("Reduced 8%");
        fileSet.getTaxf().write();
        if (fileSet.getTaxf().isInvalidKey()) {
            /* CONTINUE */
        }
        fileSet.getTaxf().close();
    }

    /** COBOL paragraph: LRGN-010 */
    private void loadRegionFile() {
        fileSet.getRegnf().open(FileOpenMode.OUTPUT);
        runChain(this::seedRegionRecords);
        fileSet.getRegnf().close();
    }

    /** COBOL paragraph: WRGN-010 */
    private void seedRegionRecords() {
        writeRegionRecord(1, "Kanto (Tokyo)");
        writeRegionRecord(2, "Kansai (Osaka)");
        writeRegionRecord(3, "Chubu (Nagoya)");
        writeRegionRecord(4, "Kyushu (Fukuoka)");
    }

    /** COBOL paragraph: LDPT-010 */
    private void loadDepartmentFile() {
        fileSet.getDeptf().open(FileOpenMode.OUTPUT);
        writeDepartmentRecord(1000, "Sales Division");
        writeDepartmentRecord(2000, "Purchasing");
        writeDepartmentRecord(9000, "Administration");
        fileSet.getDeptf().close();
    }

    /** COBOL paragraph: LCAT-010 */
    private void loadCategoryFile() {
        fileSet.getCatgf().open(FileOpenMode.OUTPUT);
        writeCategoryRecord(100, "Stationery");
        writeCategoryRecord(200, "Office Equipment");
        writeCategoryRecord(300, "Furniture");
        writeCategoryRecord(400, "Consumables");
        fileSet.getCatgf().close();
    }

    /** COBOL paragraph: LBNK-010 */
    private void loadBankFile() {
        fileSet.getBankf().open(FileOpenMode.OUTPUT);
        writeBankRecord(1, "MUFG Bank", "Marunouchi");
        writeBankRecord(2, "SMBC", "Otemachi");
        writeBankRecord(3, "Mizuho Bank", "Nihonbashi");
        fileSet.getBankf().close();
    }

    /** COBOL paragraph: LWHS-010 */
    private void loadWarehouseFile() {
        fileSet.getWhsef().open(FileOpenMode.OUTPUT);
        writeWarehouseRecord(1, "Main Warehouse", 1, 1001);
        writeWarehouseRecord(2, "East Warehouse", 1, 1002);
        writeWarehouseRecord(3, "Returns Warehouse", 3, 1001);
        fileSet.getWhsef().close();
    }

    /** COBOL paragraph: LSTF-010 */
    private void loadStaffFile() {
        fileSet.getStaff().open(FileOpenMode.OUTPUT);
        writeStaffRecord(1001, "Taro Yamada", 1000, "Manager");
        writeStaffRecord(1002, "Hanako Suzuki", 1000, "Sales Rep");
        writeStaffRecord(1003, "Ichiro Tanaka", 1000, "Sales Rep");
        writeStaffRecord(2001, "Kenji Sato", 2000, "Buyer");
        fileSet.getStaff().close();
    }

    /** COBOL paragraph: LUSR-010 */
    private void loadUserFile() {
        fileSet.getUserf().open(FileOpenMode.OUTPUT);
        writeUserRecord(9999, "admin", "admin123", "Administrator", 1, 1, 1, 1, 1, 1);
        writeUserRecord(1001, "yamada", "pass1001", "Taro Yamada", 2, 1, 1, 1, 0, 1);
        writeUserRecord(1002, "suzuki", "pass1002", "Hanako Suzuki", 3, 0, 1, 1, 0, 0);
        fileSet.getUserf().close();
    }

    /** COBOL paragraph: LCUS-010 */
    private void loadCustomerFile() {
        fileSet.getCustf().open(FileOpenMode.OUTPUT);
        runChain(this::seedCustomerRecords);
        fileSet.getCustf().close();
    }

    /** COBOL paragraph: WCUS-010 */
    private void seedCustomerRecords() {
        writeCustomerRecord(
                100001,
                "Fuji Office Supplies",
                "FUJI OFFICE",
                1,
                1002,
                20,
                2,
                1,
                BigDecimal.valueOf(3000000),
                2,
                1,
                20240401);
        writeCustomerRecord(
                100002,
                "Sakura Stationers",
                "SAKURA STAT",
                2,
                1003,
                99,
                3,
                1,
                BigDecimal.valueOf(5000000),
                1,
                2,
                20230601);
        writeCustomerRecord(
                100003,
                "Nagoya Trading",
                "NAGOYA TRD",
                3,
                1002,
                25,
                2,
                2,
                BigDecimal.valueOf(2000000),
                3,
                3,
                20250101);
        writeCustomerRecord(
                100004,
                "Kyushu Distributors",
                "KYUSHU DIST",
                4,
                1003,
                20,
                2,
                1,
                BigDecimal.valueOf(4000000),
                2,
                1,
                20240901);
        writeCustomerRecord(
                100005,
                "Tokyo Mega Mart",
                "TOKYO MEGA",
                1,
                1001,
                31,
                2,
                1,
                BigDecimal.valueOf(9000000),
                1,
                1,
                20220401);
    }

    /** COBOL paragraph: LSUP-010 */
    private void loadSupplierFile() {
        fileSet.getSuppf().open(FileOpenMode.OUTPUT);
        writeSupplierRecord(200001, "Kokuyo Supply", 20, 2, 1);
        writeSupplierRecord(200002, "Pilot Wholesale", 99, 2, 2);
        writeSupplierRecord(200003, "Okamura Furniture", 25, 3, 3);
        writeSupplierRecord(200004, "General Consumables", 20, 2, 1);
        fileSet.getSuppf().close();
    }

    /** COBOL paragraph: LPRD-010 */
    private void loadProductFile() {
        fileSet.getProdf().open(FileOpenMode.OUTPUT);
        runChain(this::seedProductRecords);
        fileSet.getProdf().close();
    }

    /** COBOL paragraph: WPRD-010 */
    private void seedProductRecords() {
        writeProductRecord(
                10000001,
                "Ballpoint Pen Black",
                100,
                "PCS",
                new BigDecimal("60.00"),
                new BigDecimal("120.00"),
                new BigDecimal("110.00"),
                new BigDecimal("112.00"),
                new BigDecimal("115.00"),
                new BigDecimal("118.00"),
                new BigDecimal("120.00"),
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(200),
                BigDecimal.valueOf(500),
                200001);
        writeProductRecord(
                10000002,
                "Notebook A4 100p",
                100,
                "PCS",
                new BigDecimal("130.00"),
                new BigDecimal("250.00"),
                new BigDecimal("230.00"),
                new BigDecimal("235.00"),
                new BigDecimal("240.00"),
                new BigDecimal("245.00"),
                new BigDecimal("250.00"),
                BigDecimal.valueOf(80),
                BigDecimal.valueOf(150),
                BigDecimal.valueOf(400),
                200001);
        writeProductRecord(
                10000003,
                "Stapler Heavy Duty",
                200,
                "PCS",
                new BigDecimal("480.00"),
                new BigDecimal("900.00"),
                new BigDecimal("850.00"),
                new BigDecimal("860.00"),
                new BigDecimal("870.00"),
                new BigDecimal("885.00"),
                new BigDecimal("900.00"),
                BigDecimal.valueOf(30),
                BigDecimal.valueOf(60),
                BigDecimal.valueOf(120),
                200001);
        writeProductRecord(
                10000004,
                "Office Chair Mesh",
                300,
                "PCS",
                new BigDecimal("8000.00"),
                new BigDecimal("15800.00"),
                new BigDecimal("14800.00"),
                new BigDecimal("15000.00"),
                new BigDecimal("15200.00"),
                new BigDecimal("15500.00"),
                new BigDecimal("15800.00"),
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(40),
                200003);
        writeProductRecord(
                10000005,
                "Copy Paper A4 500",
                400,
                "REAM",
                new BigDecimal("320.00"),
                new BigDecimal("550.00"),
                new BigDecimal("500.00"),
                new BigDecimal("510.00"),
                new BigDecimal("520.00"),
                new BigDecimal("535.00"),
                new BigDecimal("550.00"),
                BigDecimal.valueOf(200),
                BigDecimal.valueOf(400),
                BigDecimal.valueOf(1000),
                200004);
        writeProductRecord(
                10000006,
                "Whiteboard Marker",
                100,
                "PCS",
                new BigDecimal("70.00"),
                new BigDecimal("150.00"),
                new BigDecimal("140.00"),
                new BigDecimal("142.00"),
                new BigDecimal("145.00"),
                new BigDecimal("148.00"),
                new BigDecimal("150.00"),
                BigDecimal.valueOf(120),
                BigDecimal.valueOf(250),
                BigDecimal.valueOf(500),
                200001);
        writeProductRecord(
                10000007,
                "Desk Organizer",
                200,
                "PCS",
                new BigDecimal("600.00"),
                new BigDecimal("1200.00"),
                new BigDecimal("1100.00"),
                new BigDecimal("1120.00"),
                new BigDecimal("1150.00"),
                new BigDecimal("1180.00"),
                new BigDecimal("1200.00"),
                BigDecimal.valueOf(40),
                BigDecimal.valueOf(80),
                BigDecimal.valueOf(160),
                200003);
        writeProductRecord(
                10000008,
                "Filing Cabinet 3D",
                300,
                "PCS",
                new BigDecimal("12000.00"),
                new BigDecimal("23000.00"),
                new BigDecimal("21500.00"),
                new BigDecimal("21800.00"),
                new BigDecimal("22200.00"),
                new BigDecimal("22600.00"),
                new BigDecimal("23000.00"),
                BigDecimal.valueOf(8),
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(30),
                200003);
    }

    /** COBOL paragraph: LCPR-010 */
    private void loadCustomerPriceFile() {
        fileSet.getCprcf().open(FileOpenMode.OUTPUT);
        fileSet.getCprcf().setRecord();
        ws.setCpCust(100005);
        ws.setCpProd(10000005);
        ws.setCpPrice(new BigDecimal("480.00"));
        ws.setCpStartDate(20250101);
        ws.setCpEndDate(20251231);
        fileSet.getCprcf().write();
        if (fileSet.getCprcf().isInvalidKey()) {
            /* CONTINUE */
        }
        fileSet.getCprcf().setRecord();
        ws.setCpCust(100002);
        ws.setCpProd(10000001);
        ws.setCpPrice(new BigDecimal("105.00"));
        ws.setCpStartDate(20250101);
        ws.setCpEndDate(0);
        fileSet.getCprcf().write();
        if (fileSet.getCprcf().isInvalidKey()) {
            /* CONTINUE */
        }
        fileSet.getCprcf().close();
    }

    /** COBOL paragraph: LSTK-010 */
    private void loadStockFile() {
        fileSet.getStokf().open(FileOpenMode.OUTPUT);
        for (ws.setWkI(1); ws.getWkI() <= 8; ws.setWkI(ws.getWkI() + 1)) {
            fileSet.getStokf().setRecord();
            ws.setSkProd((10000000 + ws.getWkI()));
            ws.setSkWhse(1);
            ws.setSkOnhand(
                    (BigDecimal.valueOf(1000)
                                    .subtract(
                                            BigDecimal.valueOf(ws.getWkI())
                                                    .multiply(BigDecimal.valueOf(50))))
                            .setScale(0, java.math.RoundingMode.DOWN));
            ws.setSkAllocated(BigDecimal.ZERO);
            ws.setSkOnOrder(BigDecimal.ZERO);
            ws.setSkLocation("A-01");
            fileSet.getStokf().write();
            if (fileSet.getStokf().isInvalidKey()) {
                /* CONTINUE */
            }
        }
        fileSet.getStokf().close();
    }

    /** COBOL paragraph: LMSG-010 */
    private void loadMessageFile() {
        fileSet.getMsgf().open(FileOpenMode.OUTPUT);
        writeMessageRecord("I0001", "Processing completed");
        writeMessageRecord("E0001", "Record not found");
        writeMessageRecord("E0002", "Duplicate key");
        writeMessageRecord("W0001", "Stock shortage");
        fileSet.getMsgf().close();
    }

    /**
     * Populate one NUMCF record with the given key/prefix (width/current are fixed seed defaults)
     * and write it.
     */
    private void writeNumRecord(String key, String prefix) {
        fileSet.getNumcf().setRecord();
        ws.setNmKey(key);
        ws.setNmPrefix(prefix);
        ws.setNmWidth(10);
        ws.setNmCurrent(0);
        fileSet.getNumcf().write();
        if (fileSet.getNumcf().isInvalidKey()) {
            /* CONTINUE */
        }
    }

    /** Populate one REGNF record with the given code/name and write it. */
    private void writeRegionRecord(int code, String name) {
        fileSet.getRegnf().setRecord();
        ws.setRgCode(code);
        ws.setRgName(name);
        fileSet.getRegnf().write();
        if (fileSet.getRegnf().isInvalidKey()) {
            /* CONTINUE */
        }
    }

    /** Populate one DEPTF record with the given code/name and write it. */
    private void writeDepartmentRecord(int code, String name) {
        fileSet.getDeptf().setRecord();
        ws.setDpCode(code);
        ws.setDpName(name);
        fileSet.getDeptf().write();
        if (fileSet.getDeptf().isInvalidKey()) {
            /* CONTINUE */
        }
    }

    /**
     * Populate one CATGF record with the given code/name (level fixed at top-level category) and
     * write it.
     */
    private void writeCategoryRecord(int code, String name) {
        fileSet.getCatgf().setRecord();
        ws.setCtCode(code);
        ws.setCtName(name);
        ws.setCtLevel(1);
        fileSet.getCatgf().write();
        if (fileSet.getCatgf().isInvalidKey()) {
            /* CONTINUE */
        }
    }

    /** Populate one BANKF record with the given code/name/branch and write it. */
    private void writeBankRecord(int code, String name, String branch) {
        fileSet.getBankf().setRecord();
        ws.setBkCode(code);
        ws.setBkName(name);
        ws.setBkBranch(branch);
        fileSet.getBankf().write();
        if (fileSet.getBankf().isInvalidKey()) {
            /* CONTINUE */
        }
    }

    /** Populate one WHSEF record with the given code/name/type/manager and write it. */
    private void writeWarehouseRecord(int code, String name, int type, int manager) {
        fileSet.getWhsef().setRecord();
        ws.setWhCode(code);
        ws.setWhName(name);
        ws.setWhType(type);
        ws.setWhManager(manager);
        fileSet.getWhsef().write();
        if (fileSet.getWhsef().isInvalidKey()) {
            /* CONTINUE */
        }
    }

    /** Populate one STAFF record with the given code/name/dept/title and write it. */
    private void writeStaffRecord(int code, String name, int dept, String title) {
        fileSet.getStaff().setRecord();
        ws.setSfCode(code);
        ws.setSfName(name);
        ws.setSfDept(dept);
        ws.setSfTitle(title);
        fileSet.getStaff().write();
        if (fileSet.getStaff().isInvalidKey()) {
            /* CONTINUE */
        }
    }

    /** Populate one USERF record with the given identity/role and authority flags and write it. */
    private void writeUserRecord(
            int code,
            String login,
            String password,
            String name,
            int role,
            int authMaster,
            int authOrder,
            int authSales,
            int authPurch,
            int authClose) {
        fileSet.getUserf().setRecord();
        ws.setUsCode(code);
        ws.setUsLogin(login);
        ws.setUsPassword(password);
        ws.setUsName(name);
        ws.setUsRole(role);
        ws.setUsAuthMaster(authMaster);
        ws.setUsAuthOrder(authOrder);
        ws.setUsAuthSales(authSales);
        ws.setUsAuthPurch(authPurch);
        ws.setUsAuthClose(authClose);
        fileSet.getUserf().write();
        if (fileSet.getUserf().isInvalidKey()) {
            /* CONTINUE */
        }
    }

    /**
     * Populate one CUSTF record with the given profile/terms fields (tax-round is a fixed seed
     * default) and write it.
     */
    private void writeCustomerRecord(
            int code,
            String name,
            String kana,
            int region,
            int staff,
            int closeDay,
            int payMethod,
            int taxType,
            BigDecimal creditLimit,
            int priceRank,
            int bankCode,
            int startDate) {
        fileSet.getCustf().setRecord();
        ws.setCuCode(code);
        ws.setCuName(name);
        ws.setCuKana(kana);
        ws.setCuRegion(region);
        ws.setCuStaff(staff);
        ws.setCuCloseDay(closeDay);
        ws.setCuPayMethod(payMethod);
        ws.setCuTaxType(taxType);
        ws.setCuTaxRound(1);
        ws.setCuCreditLimit(creditLimit);
        ws.setCuPriceRank(priceRank);
        ws.setCuBankCode(bankCode);
        ws.setCuStartDate(startDate);
        fileSet.getCustf().write();
        if (fileSet.getCustf().isInvalidKey()) {
            /* CONTINUE */
        }
    }

    /**
     * Populate one SUPPF record with the given terms (tax-type is a fixed seed default) and write
     * it.
     */
    private void writeSupplierRecord(
            int code, String name, int closeDay, int payMethod, int bankCode) {
        fileSet.getSuppf().setRecord();
        ws.setSpCode(code);
        ws.setSpName(name);
        ws.setSpCloseDay(closeDay);
        ws.setSpPayMethod(payMethod);
        ws.setSpTaxType(1);
        ws.setSpBankCode(bankCode);
        fileSet.getSuppf().write();
        if (fileSet.getSuppf().isInvalidKey()) {
            /* CONTINUE */
        }
    }

    /**
     * Populate one PRODF record (rank prices 1-5, tax-category/dflt-whse/stock-mng are fixed seed
     * defaults, last-cost mirrors std-cost) and write it.
     */
    private void writeProductRecord(
            int code,
            String name,
            int category,
            String unit,
            BigDecimal stdCost,
            BigDecimal listPrice,
            BigDecimal rankPrice1,
            BigDecimal rankPrice2,
            BigDecimal rankPrice3,
            BigDecimal rankPrice4,
            BigDecimal rankPrice5,
            BigDecimal safetyStock,
            BigDecimal reorderPoint,
            BigDecimal reorderQty,
            int dfltSupp) {
        fileSet.getProdf().setRecord();
        ws.setPrCode(code);
        ws.setPrName(name);
        ws.setPrCategory(category);
        ws.setPrUnit(unit);
        ws.setPrStdCost(stdCost);
        ws.setPrLastCost(stdCost);
        ws.setPrListPrice(listPrice);
        ws.setPrRankPrice(1, rankPrice1);
        ws.setPrRankPrice(2, rankPrice2);
        ws.setPrRankPrice(3, rankPrice3);
        ws.setPrRankPrice(4, rankPrice4);
        ws.setPrRankPrice(5, rankPrice5);
        ws.setPrTaxCategory(1);
        ws.setPrSafetyStock(safetyStock);
        ws.setPrReorderPoint(reorderPoint);
        ws.setPrReorderQty(reorderQty);
        ws.setPrDfltSupp(dfltSupp);
        ws.setPrDfltWhse(1);
        ws.setPrStockMng(1);
        fileSet.getProdf().write();
        if (fileSet.getProdf().isInvalidKey()) {
            /* CONTINUE */
        }
    }

    /** Populate one MSGF record with the given code/text and write it. */
    private void writeMessageRecord(String code, String text) {
        fileSet.getMsgf().setRecord();
        ws.setMgCode(code);
        ws.setMgText(text);
        fileSet.getMsgf().write();
        if (fileSet.getMsgf().isInvalidKey()) {
            /* CONTINUE */
        }
    }
}
