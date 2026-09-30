package com.sakura.bt0070.runtime;

import com.sakura.runtime.io.*;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Shared file instances for program: BT0070 Mirrors COBOL's single FD per SELECT — ensures
 * consistent file state.
 */
@Component
@Scope("prototype")
public class Bt0070Datasets extends AbstractDatasets {

    private final SyscfDataset syscf = new SyscfDataset();
    private final StokfDataset stokf = new StokfDataset();

    @PostConstruct
    @Override
    public void registerFiles() {
        registerFile(syscf);
        registerFile(stokf);
        super.registerFiles();
    }

    public SyscfDataset getSyscf() {
        return syscf;
    }

    public StokfDataset getStokf() {
        return stokf;
    }
}
