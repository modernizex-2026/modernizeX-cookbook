package com.sakura.ar0010.runtime;

import com.sakura.ar0010.io.*;
import com.sakura.runtime.io.*;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Shared file instances for program: AR0010 Mirrors COBOL's single FD per SELECT — ensures
 * consistent file state.
 */
@Component
@Scope("prototype")
public class Ar0010Datasets extends AbstractDatasets {

    private final RcptfDataset rcptf = new RcptfDataset();
    private final ArlfDataset arlf = new ArlfDataset();
    private final CustfDataset custf = new CustfDataset();
    private final BankfDataset bankf = new BankfDataset();

    @PostConstruct
    @Override
    public void registerFiles() {
        registerFile(rcptf);
        registerFile(arlf);
        registerFile(custf);
        registerFile(bankf);
        super.registerFiles();
    }

    public RcptfDataset getRcptf() {
        return rcptf;
    }

    public ArlfDataset getArlf() {
        return arlf;
    }

    public CustfDataset getCustf() {
        return custf;
    }

    public BankfDataset getBankf() {
        return bankf;
    }
}
