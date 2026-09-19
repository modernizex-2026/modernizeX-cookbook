package com.sakura.initdb.runtime;

import com.sakura.initdb.io.*;
import com.sakura.runtime.io.*;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Shared file instances for program: INITDB Mirrors COBOL's single FD per SELECT — ensures
 * consistent file state.
 */
@Component
@Scope("prototype")
public class InitdbDatasets extends AbstractDatasets {

    private final SyscfDataset syscf = new SyscfDataset();
    private final NumcfDataset numcf = new NumcfDataset();
    private final TaxfDataset taxf = new TaxfDataset();
    private final RegnfDataset regnf = new RegnfDataset();
    private final DeptfDataset deptf = new DeptfDataset();
    private final CatgfDataset catgf = new CatgfDataset();
    private final BankfDataset bankf = new BankfDataset();
    private final WhsefDataset whsef = new WhsefDataset();
    private final StaffDataset staff = new StaffDataset();
    private final UserfDataset userf = new UserfDataset();
    private final CustfDataset custf = new CustfDataset();
    private final SuppfDataset suppf = new SuppfDataset();
    private final ProdfDataset prodf = new ProdfDataset();
    private final CprcfDataset cprcf = new CprcfDataset();
    private final StokfDataset stokf = new StokfDataset();
    private final MsgfDataset msgf = new MsgfDataset();
    private final OrdhfDataset ordhf = new OrdhfDataset();
    private final OrddfDataset orddf = new OrddfDataset();
    private final ShphfDataset shphf = new ShphfDataset();
    private final ShpdfDataset shpdf = new ShpdfDataset();
    private final InvhfDataset invhf = new InvhfDataset();
    private final InvdfDataset invdf = new InvdfDataset();
    private final PohfDataset pohf = new PohfDataset();
    private final PodfDataset podf = new PodfDataset();
    private final RcvhfDataset rcvhf = new RcvhfDataset();
    private final RcvdfDataset rcvdf = new RcvdfDataset();
    private final PurhfDataset purhf = new PurhfDataset();
    private final PurdfDataset purdf = new PurdfDataset();
    private final ArlfDataset arlf = new ArlfDataset();
    private final AplfDataset aplf = new AplfDataset();
    private final RcptfDataset rcptf = new RcptfDataset();
    private final PayfDataset payf = new PayfDataset();
    private final SmovfDataset smovf = new SmovfDataset();

    @PostConstruct
    @Override
    public void registerFiles() {
        registerFile(syscf);
        registerFile(numcf);
        registerFile(taxf);
        registerFile(regnf);
        registerFile(deptf);
        registerFile(catgf);
        registerFile(bankf);
        registerFile(whsef);
        registerFile(staff);
        registerFile(userf);
        registerFile(custf);
        registerFile(suppf);
        registerFile(prodf);
        registerFile(cprcf);
        registerFile(stokf);
        registerFile(msgf);
        registerFile(ordhf);
        registerFile(orddf);
        registerFile(shphf);
        registerFile(shpdf);
        registerFile(invhf);
        registerFile(invdf);
        registerFile(pohf);
        registerFile(podf);
        registerFile(rcvhf);
        registerFile(rcvdf);
        registerFile(purhf);
        registerFile(purdf);
        registerFile(arlf);
        registerFile(aplf);
        registerFile(rcptf);
        registerFile(payf);
        registerFile(smovf);
        super.registerFiles();
    }

    public SyscfDataset getSyscf() {
        return syscf;
    }

    public NumcfDataset getNumcf() {
        return numcf;
    }

    public TaxfDataset getTaxf() {
        return taxf;
    }

    public RegnfDataset getRegnf() {
        return regnf;
    }

    public DeptfDataset getDeptf() {
        return deptf;
    }

    public CatgfDataset getCatgf() {
        return catgf;
    }

    public BankfDataset getBankf() {
        return bankf;
    }

    public WhsefDataset getWhsef() {
        return whsef;
    }

    public StaffDataset getStaff() {
        return staff;
    }

    public UserfDataset getUserf() {
        return userf;
    }

    public CustfDataset getCustf() {
        return custf;
    }

    public SuppfDataset getSuppf() {
        return suppf;
    }

    public ProdfDataset getProdf() {
        return prodf;
    }

    public CprcfDataset getCprcf() {
        return cprcf;
    }

    public StokfDataset getStokf() {
        return stokf;
    }

    public MsgfDataset getMsgf() {
        return msgf;
    }

    public OrdhfDataset getOrdhf() {
        return ordhf;
    }

    public OrddfDataset getOrddf() {
        return orddf;
    }

    public ShphfDataset getShphf() {
        return shphf;
    }

    public ShpdfDataset getShpdf() {
        return shpdf;
    }

    public InvhfDataset getInvhf() {
        return invhf;
    }

    public InvdfDataset getInvdf() {
        return invdf;
    }

    public PohfDataset getPohf() {
        return pohf;
    }

    public PodfDataset getPodf() {
        return podf;
    }

    public RcvhfDataset getRcvhf() {
        return rcvhf;
    }

    public RcvdfDataset getRcvdf() {
        return rcvdf;
    }

    public PurhfDataset getPurhf() {
        return purhf;
    }

    public PurdfDataset getPurdf() {
        return purdf;
    }

    public ArlfDataset getArlf() {
        return arlf;
    }

    public AplfDataset getAplf() {
        return aplf;
    }

    public RcptfDataset getRcptf() {
        return rcptf;
    }

    public PayfDataset getPayf() {
        return payf;
    }

    public SmovfDataset getSmovf() {
        return smovf;
    }
}
