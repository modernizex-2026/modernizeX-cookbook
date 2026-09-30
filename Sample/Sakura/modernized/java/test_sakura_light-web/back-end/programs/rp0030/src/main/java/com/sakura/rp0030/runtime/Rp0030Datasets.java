package com.sakura.rp0030.runtime;

import com.sakura.runtime.io.*;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Shared file instances for program: RP0030 Mirrors COBOL's single FD per SELECT — ensures
 * consistent file state.
 */
@Component
@Scope("prototype")
public class Rp0030Datasets extends AbstractDatasets {

    private final InvhfDataset invhf = new InvhfDataset();
    private final InvdfDataset invdf = new InvdfDataset();
    private final CustfDataset custf = new CustfDataset();
    private final ProdfDataset prodf = new ProdfDataset();
    private final SyscfDataset syscf = new SyscfDataset();
    private final RepfDataset repf = new RepfDataset();

    @PostConstruct
    @Override
    public void registerFiles() {
        registerFile(invhf);
        registerFile(invdf);
        registerFile(custf);
        registerFile(prodf);
        registerFile(syscf);
        super.registerFiles();
    }

    public InvhfDataset getInvhf() {
        return invhf;
    }

    public InvdfDataset getInvdf() {
        return invdf;
    }

    public CustfDataset getCustf() {
        return custf;
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
