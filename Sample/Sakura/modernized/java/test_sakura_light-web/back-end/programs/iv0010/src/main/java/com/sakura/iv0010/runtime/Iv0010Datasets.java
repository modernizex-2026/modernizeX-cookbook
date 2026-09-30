package com.sakura.iv0010.runtime;

import com.sakura.runtime.io.*;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Shared file instances for program: IV0010 Mirrors COBOL's single FD per SELECT — ensures
 * consistent file state.
 */
@Component
@Scope("prototype")
public class Iv0010Datasets extends AbstractDatasets {

    private final StokfDataset stokf = new StokfDataset();
    private final ProdfDataset prodf = new ProdfDataset();
    private final WhsefDataset whsef = new WhsefDataset();

    @PostConstruct
    @Override
    public void registerFiles() {
        registerFile(stokf);
        registerFile(prodf);
        registerFile(whsef);
        super.registerFiles();
    }

    public StokfDataset getStokf() {
        return stokf;
    }

    public ProdfDataset getProdf() {
        return prodf;
    }

    public WhsefDataset getWhsef() {
        return whsef;
    }
}
