package com.sakura.bt0040.runtime;

import com.sakura.runtime.io.*;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Shared file instances for program: BT0040 Mirrors COBOL's single FD per SELECT — ensures
 * consistent file state.
 */
@Component
@Scope("prototype")
public class Bt0040Datasets extends AbstractDatasets {

    private final ArlfDataset arlf = new ArlfDataset();
    private final CustfDataset custf = new CustfDataset();

    @PostConstruct
    @Override
    public void registerFiles() {
        registerFile(arlf);
        registerFile(custf);
        super.registerFiles();
    }

    public ArlfDataset getArlf() {
        return arlf;
    }

    public CustfDataset getCustf() {
        return custf;
    }
}
