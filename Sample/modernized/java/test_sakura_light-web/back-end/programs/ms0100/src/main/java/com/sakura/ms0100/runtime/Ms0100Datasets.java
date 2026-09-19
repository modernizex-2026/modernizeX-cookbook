package com.sakura.ms0100.runtime;

import com.sakura.runtime.io.*;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Shared file instances for program: MS0100 Mirrors COBOL's single FD per SELECT — ensures
 * consistent file state.
 */
@Component
@Scope("prototype")
public class Ms0100Datasets extends AbstractDatasets {

    private final BankfDataset bankf = new BankfDataset();

    @PostConstruct
    @Override
    public void registerFiles() {
        registerFile(bankf);
        super.registerFiles();
    }

    public BankfDataset getBankf() {
        return bankf;
    }
}
