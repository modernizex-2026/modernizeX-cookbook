package com.sakura.ms0020.runtime;

import com.sakura.runtime.io.*;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Shared file instances for program: MS0020 Mirrors COBOL's single FD per SELECT — ensures
 * consistent file state.
 */
@Component
@Scope("prototype")
public class Ms0020Datasets extends AbstractDatasets {

    private final SuppfDataset suppf = new SuppfDataset();
    private final BankfDataset bankf = new BankfDataset();

    @PostConstruct
    @Override
    public void registerFiles() {
        registerFile(suppf);
        registerFile(bankf);
        super.registerFiles();
    }

    public SuppfDataset getSuppf() {
        return suppf;
    }

    public BankfDataset getBankf() {
        return bankf;
    }
}
