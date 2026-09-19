package com.sakura.pu0040.runtime;

import com.sakura.runtime.io.*;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Shared file instances for program: PU0040 Mirrors COBOL's single FD per SELECT — ensures
 * consistent file state.
 */
@Component
@Scope("prototype")
public class Pu0040Datasets extends AbstractDatasets {

    private final PurhfDataset purhf = new PurhfDataset();
    private final PurdfDataset purdf = new PurdfDataset();
    private final AplfDataset aplf = new AplfDataset();
    private final StokfDataset stokf = new StokfDataset();
    private final SmovfDataset smovf = new SmovfDataset();
    private final SuppfDataset suppf = new SuppfDataset();
    private final ProdfDataset prodf = new ProdfDataset();

    @PostConstruct
    @Override
    public void registerFiles() {
        registerFile(purhf);
        registerFile(purdf);
        registerFile(aplf);
        registerFile(stokf);
        registerFile(smovf);
        registerFile(suppf);
        registerFile(prodf);
        super.registerFiles();
    }

    public PurhfDataset getPurhf() {
        return purhf;
    }

    public PurdfDataset getPurdf() {
        return purdf;
    }

    public AplfDataset getAplf() {
        return aplf;
    }

    public StokfDataset getStokf() {
        return stokf;
    }

    public SmovfDataset getSmovf() {
        return smovf;
    }

    public SuppfDataset getSuppf() {
        return suppf;
    }

    public ProdfDataset getProdf() {
        return prodf;
    }
}
