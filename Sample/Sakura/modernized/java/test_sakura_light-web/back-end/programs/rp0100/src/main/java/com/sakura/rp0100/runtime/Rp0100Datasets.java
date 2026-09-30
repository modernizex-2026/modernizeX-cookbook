package com.sakura.rp0100.runtime;

import com.sakura.runtime.io.*;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Shared file instances for program: RP0100 Mirrors COBOL's single FD per SELECT — ensures
 * consistent file state.
 */
@Component
@Scope("prototype")
public class Rp0100Datasets extends AbstractDatasets {

    private final PurhfDataset purhf = new PurhfDataset();
    private final PurdfDataset purdf = new PurdfDataset();
    private final SuppfDataset suppf = new SuppfDataset();
    private final ProdfDataset prodf = new ProdfDataset();
    private final SyscfDataset syscf = new SyscfDataset();
    private final RepfDataset repf = new RepfDataset();

    @PostConstruct
    @Override
    public void registerFiles() {
        registerFile(purhf);
        registerFile(purdf);
        registerFile(suppf);
        registerFile(prodf);
        registerFile(syscf);
        super.registerFiles();
    }

    public PurhfDataset getPurhf() {
        return purhf;
    }

    public PurdfDataset getPurdf() {
        return purdf;
    }

    public SuppfDataset getSuppf() {
        return suppf;
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
