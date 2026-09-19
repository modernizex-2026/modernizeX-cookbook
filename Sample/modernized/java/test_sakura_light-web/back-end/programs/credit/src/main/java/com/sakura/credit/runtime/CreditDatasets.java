package com.sakura.credit.runtime;

import com.sakura.runtime.io.*;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Shared file instances for program: CREDIT Mirrors COBOL's single FD per SELECT — ensures
 * consistent file state.
 */
@Component
@Scope("prototype")
public class CreditDatasets extends AbstractDatasets {

    private final CustfDataset custf = new CustfDataset();

    @PostConstruct
    @Override
    public void registerFiles() {
        registerFile(custf);
        super.registerFiles();
    }

    public CustfDataset getCustf() {
        return custf;
    }
}
