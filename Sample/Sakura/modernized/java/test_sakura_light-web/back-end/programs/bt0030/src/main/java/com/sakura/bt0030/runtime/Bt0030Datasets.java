package com.sakura.bt0030.runtime;

import com.sakura.runtime.io.*;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Shared file instances for program: BT0030 Mirrors COBOL's single FD per SELECT — ensures
 * consistent file state.
 */
@Component
@Scope("prototype")
public class Bt0030Datasets extends AbstractDatasets {

    private final StokfDataset stokf = new StokfDataset();
    private final SyscfDataset syscf = new SyscfDataset();

    @PostConstruct
    @Override
    public void registerFiles() {
        registerFile(stokf);
        registerFile(syscf);
        super.registerFiles();
    }

    public StokfDataset getStokf() {
        return stokf;
    }

    public SyscfDataset getSyscf() {
        return syscf;
    }
}
