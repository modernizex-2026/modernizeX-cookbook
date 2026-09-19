package com.sakura.rp0110.runtime;

import com.sakura.runtime.io.*;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Shared file instances for program: RP0110 Mirrors COBOL's single FD per SELECT — ensures
 * consistent file state.
 */
@Component
@Scope("prototype")
public class Rp0110Datasets extends AbstractDatasets {

    private final ProdfDataset prodf = new ProdfDataset();
    private final StokfDataset stokf = new StokfDataset();
    private final SuppfDataset suppf = new SuppfDataset();
    private final SyscfDataset syscf = new SyscfDataset();
    private final RepfDataset repf = new RepfDataset();

    @PostConstruct
    @Override
    public void registerFiles() {
        registerFile(prodf);
        registerFile(stokf);
        registerFile(suppf);
        registerFile(syscf);
        super.registerFiles();
    }

    public ProdfDataset getProdf() {
        return prodf;
    }

    public StokfDataset getStokf() {
        return stokf;
    }

    public SuppfDataset getSuppf() {
        return suppf;
    }

    public SyscfDataset getSyscf() {
        return syscf;
    }

    public RepfDataset getRepf() {
        return repf;
    }
}
