package com.sakura.sl0040.runtime;

import com.sakura.runtime.io.*;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Shared file instances for program: SL0040 Mirrors COBOL's single FD per SELECT — ensures
 * consistent file state.
 */
@Component
@Scope("prototype")
public class Sl0040Datasets extends AbstractDatasets {

    private final InvhfDataset invhf = new InvhfDataset();
    private final InvdfDataset invdf = new InvdfDataset();
    private final StokfDataset stokf = new StokfDataset();
    private final SmovfDataset smovf = new SmovfDataset();
    private final CustfDataset custf = new CustfDataset();
    private final ProdfDataset prodf = new ProdfDataset();
    private final ArlfDataset arlf = new ArlfDataset();

    @PostConstruct
    @Override
    public void registerFiles() {
        registerFile(invhf);
        registerFile(invdf);
        registerFile(stokf);
        registerFile(smovf);
        registerFile(custf);
        registerFile(prodf);
        registerFile(arlf);
        super.registerFiles();
    }

    public InvhfDataset getInvhf() {
        return invhf;
    }

    public InvdfDataset getInvdf() {
        return invdf;
    }

    public StokfDataset getStokf() {
        return stokf;
    }

    public SmovfDataset getSmovf() {
        return smovf;
    }

    public CustfDataset getCustf() {
        return custf;
    }

    public ProdfDataset getProdf() {
        return prodf;
    }

    public ArlfDataset getArlf() {
        return arlf;
    }
}
