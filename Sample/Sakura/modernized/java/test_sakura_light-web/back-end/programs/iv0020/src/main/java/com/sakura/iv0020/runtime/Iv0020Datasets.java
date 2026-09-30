package com.sakura.iv0020.runtime;

import com.sakura.runtime.io.*;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Shared file instances for program: IV0020 Mirrors COBOL's single FD per SELECT — ensures
 * consistent file state.
 */
@Component
@Scope("prototype")
public class Iv0020Datasets extends AbstractDatasets {

    private final StokfDataset stokf = new StokfDataset();
    private final SmovfDataset smovf = new SmovfDataset();
    private final ProdfDataset prodf = new ProdfDataset();
    private final WhsefDataset whsef = new WhsefDataset();

    @PostConstruct
    @Override
    public void registerFiles() {
        registerFile(stokf);
        registerFile(smovf);
        registerFile(prodf);
        registerFile(whsef);
        super.registerFiles();
    }

    public StokfDataset getStokf() {
        return stokf;
    }

    public SmovfDataset getSmovf() {
        return smovf;
    }

    public ProdfDataset getProdf() {
        return prodf;
    }

    public WhsefDataset getWhsef() {
        return whsef;
    }
}
