package com.sakura.rp0010.runtime;

import com.sakura.runtime.io.*;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Shared file instances for program: RP0010 Mirrors COBOL's single FD per SELECT — ensures
 * consistent file state.
 */
@Component
@Scope("prototype")
public class Rp0010Datasets extends AbstractDatasets {

    private final CustfDataset custf = new CustfDataset();
    private final RegnfDataset regnf = new RegnfDataset();
    private final StaffDataset staff = new StaffDataset();
    private final SyscfDataset syscf = new SyscfDataset();
    private final RepfDataset repf = new RepfDataset();

    @PostConstruct
    @Override
    public void registerFiles() {
        registerFile(custf);
        registerFile(regnf);
        registerFile(staff);
        registerFile(syscf);
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

    public SyscfDataset getSyscf() {
        return syscf;
    }

    public RepfDataset getRepf() {
        return repf;
    }
}
