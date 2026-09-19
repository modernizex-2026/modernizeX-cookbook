package com.sakura.rp0070.runtime;

import com.sakura.runtime.io.*;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Shared file instances for program: RP0070 Mirrors COBOL's single FD per SELECT — ensures
 * consistent file state.
 */
@Component
@Scope("prototype")
public class Rp0070Datasets extends AbstractDatasets {

    private final ArlfDataset arlf = new ArlfDataset();
    private final CustfDataset custf = new CustfDataset();
    private final SyscfDataset syscf = new SyscfDataset();
    private final RepfDataset repf = new RepfDataset();

    @PostConstruct
    @Override
    public void registerFiles() {
        registerFile(arlf);
        registerFile(custf);
        registerFile(syscf);
        super.registerFiles();
    }

    public ArlfDataset getArlf() {
        return arlf;
    }

    public CustfDataset getCustf() {
        return custf;
    }

    public SyscfDataset getSyscf() {
        return syscf;
    }

    public RepfDataset getRepf() {
        return repf;
    }
}
