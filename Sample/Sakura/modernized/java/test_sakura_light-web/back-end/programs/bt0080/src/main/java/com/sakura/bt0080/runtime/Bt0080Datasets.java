package com.sakura.bt0080.runtime;

import com.sakura.bt0080.io.*;
import com.sakura.runtime.io.*;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Shared file instances for program: BT0080 Mirrors COBOL's single FD per SELECT — ensures
 * consistent file state.
 */
@Component
@Scope("prototype")
public class Bt0080Datasets extends AbstractDatasets {

    private final CustfDataset custf = new CustfDataset();
    private final ProdfDataset prodf = new ProdfDataset();
    private final CustnDataset custn = new CustnDataset();
    private final ProdnDataset prodn = new ProdnDataset();

    @PostConstruct
    @Override
    public void registerFiles() {
        registerFile(custf);
        registerFile(prodf);
        super.registerFiles();
    }

    public CustfDataset getCustf() {
        return custf;
    }

    public ProdfDataset getProdf() {
        return prodf;
    }

    public CustnDataset getCustn() {
        return custn;
    }

    public ProdnDataset getProdn() {
        return prodn;
    }
}
