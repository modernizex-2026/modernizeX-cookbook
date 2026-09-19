package com.sakura.rp0080.runtime;

import com.sakura.runtime.io.*;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Shared file instances for program: RP0080 Mirrors COBOL's single FD per SELECT — ensures
 * consistent file state.
 */
@Component
@Scope("prototype")
public class Rp0080Datasets extends AbstractDatasets {

    private final AplfDataset aplf = new AplfDataset();
    private final SuppfDataset suppf = new SuppfDataset();
    private final SyscfDataset syscf = new SyscfDataset();
    private final RepfDataset repf = new RepfDataset();

    @PostConstruct
    @Override
    public void registerFiles() {
        registerFile(aplf);
        registerFile(suppf);
        registerFile(syscf);
        super.registerFiles();
    }

    public AplfDataset getAplf() {
        return aplf;
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
