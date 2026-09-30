package com.sakura.sl0020.runtime;

import com.sakura.runtime.io.*;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Shared file instances for program: SL0020 Mirrors COBOL's single FD per SELECT — ensures
 * consistent file state.
 */
@Component
@Scope("prototype")
public class Sl0020Datasets extends AbstractDatasets {

    private final InvhfDataset invhf = new InvhfDataset();
    private final InvdfDataset invdf = new InvdfDataset();
    private final CustfDataset custf = new CustfDataset();
    private final ProdfDataset prodf = new ProdfDataset();

    @PostConstruct
    @Override
    public void registerFiles() {
        registerFile(invhf);
        registerFile(invdf);
        registerFile(custf);
        registerFile(prodf);
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
}
