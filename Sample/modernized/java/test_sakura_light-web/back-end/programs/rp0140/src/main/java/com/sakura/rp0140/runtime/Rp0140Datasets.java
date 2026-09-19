package com.sakura.rp0140.runtime;

import com.sakura.runtime.io.*;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Shared file instances for program: RP0140 Mirrors COBOL's single FD per SELECT — ensures
 * consistent file state.
 */
@Component
@Scope("prototype")
public class Rp0140Datasets extends AbstractDatasets {

    private final StokfDataset stokf = new StokfDataset();
    private final ProdfDataset prodf = new ProdfDataset();
    private final WhsefDataset whsef = new WhsefDataset();
    private final SyscfDataset syscf = new SyscfDataset();
    private final RepfDataset repf = new RepfDataset();

    @PostConstruct
    @Override
    public void registerFiles() {
        registerFile(stokf);
        registerFile(prodf);
        registerFile(whsef);
        registerFile(syscf);
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

    public SyscfDataset getSyscf() {
        return syscf;
    }

    public RepfDataset getRepf() {
        return repf;
    }
}
