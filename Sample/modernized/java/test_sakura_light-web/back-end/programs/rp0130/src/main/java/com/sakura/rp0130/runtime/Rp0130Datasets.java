package com.sakura.rp0130.runtime;

import com.sakura.runtime.io.*;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Shared file instances for program: RP0130 Mirrors COBOL's single FD per SELECT — ensures
 * consistent file state.
 */
@Component
@Scope("prototype")
public class Rp0130Datasets extends AbstractDatasets {

    private final CustfDataset custf = new CustfDataset();
    private final ArlfDataset arlf = new ArlfDataset();
    private final SyscfDataset syscf = new SyscfDataset();
    private final RepfDataset repf = new RepfDataset();

    @PostConstruct
    @Override
    public void registerFiles() {
        registerFile(custf);
        registerFile(arlf);
        registerFile(syscf);
        super.registerFiles();
    }

    public CustfDataset getCustf() {
        return custf;
    }

    public ArlfDataset getArlf() {
        return arlf;
    }

    public SyscfDataset getSyscf() {
        return syscf;
    }

    public RepfDataset getRepf() {
        return repf;
    }
}
