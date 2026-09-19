package com.sakura.ap0010.runtime;

import com.sakura.ap0010.io.*;
import com.sakura.runtime.io.*;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Shared file instances for program: AP0010 Mirrors COBOL's single FD per SELECT — ensures
 * consistent file state.
 */
@Component
@Scope("prototype")
public class Ap0010Datasets extends AbstractDatasets {

    private final PayfDataset payf = new PayfDataset();
    private final AplfDataset aplf = new AplfDataset();
    private final SuppfDataset suppf = new SuppfDataset();
    private final BankfDataset bankf = new BankfDataset();

    @PostConstruct
    @Override
    public void registerFiles() {
        registerFile(payf);
        registerFile(aplf);
        registerFile(suppf);
        registerFile(bankf);
        super.registerFiles();
    }

    public PayfDataset getPayf() {
        return payf;
    }

    public AplfDataset getAplf() {
        return aplf;
    }

    public SuppfDataset getSuppf() {
        return suppf;
    }

    public BankfDataset getBankf() {
        return bankf;
    }
}
