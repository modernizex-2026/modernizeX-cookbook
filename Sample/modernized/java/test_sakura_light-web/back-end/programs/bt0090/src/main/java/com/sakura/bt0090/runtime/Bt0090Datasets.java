package com.sakura.bt0090.runtime;

import com.sakura.runtime.io.*;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Shared file instances for program: BT0090 Mirrors COBOL's single FD per SELECT — ensures
 * consistent file state.
 */
@Component
@Scope("prototype")
public class Bt0090Datasets extends AbstractDatasets {

    private final OrddfDataset orddf = new OrddfDataset();
    private final OrdhfDataset ordhf = new OrdhfDataset();
    private final InvdfDataset invdf = new InvdfDataset();
    private final InvhfDataset invhf = new InvhfDataset();
    private final StokfDataset stokf = new StokfDataset();
    private final ProdfDataset prodf = new ProdfDataset();
    private final SyscfDataset syscf = new SyscfDataset();
    private final RepfDataset repf = new RepfDataset();

    @PostConstruct
    @Override
    public void registerFiles() {
        registerFile(orddf);
        registerFile(ordhf);
        registerFile(invdf);
        registerFile(invhf);
        registerFile(stokf);
        registerFile(prodf);
        registerFile(syscf);
        super.registerFiles();
    }

    public OrddfDataset getOrddf() {
        return orddf;
    }

    public OrdhfDataset getOrdhf() {
        return ordhf;
    }

    public InvdfDataset getInvdf() {
        return invdf;
    }

    public InvhfDataset getInvhf() {
        return invhf;
    }

    public StokfDataset getStokf() {
        return stokf;
    }

    public ProdfDataset getProdf() {
        return prodf;
    }

    public SyscfDataset getSyscf() {
        return syscf;
    }

    public RepfDataset getRepf() {
        return repf;
    }
}
