package com.sakura.ms0010.runtime;

import com.sakura.runtime.io.*;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Shared file instances for program: MS0010 Mirrors COBOL's single FD per SELECT — ensures
 * consistent file state.
 */
@Component
@Scope("prototype")
public class Ms0010Datasets extends AbstractDatasets {

    private final CustfDataset custf = new CustfDataset();
    private final RegnfDataset regnf = new RegnfDataset();
    private final StaffDataset staff = new StaffDataset();
    private final BankfDataset bankf = new BankfDataset();

    @PostConstruct
    @Override
    public void registerFiles() {
        registerFile(custf);
        registerFile(regnf);
        registerFile(staff);
        registerFile(bankf);
        super.registerFiles();
    }

    public CustfDataset getCustf() {
        return custf;
    }

    public RegnfDataset getRegnf() {
        return regnf;
    }

    public StaffDataset getStaff() {
        return staff;
    }

    public BankfDataset getBankf() {
        return bankf;
    }
}
