package com.sakura.ms0050.runtime;

import com.sakura.runtime.io.*;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Shared file instances for program: MS0050 Mirrors COBOL's single FD per SELECT — ensures
 * consistent file state.
 */
@Component
@Scope("prototype")
public class Ms0050Datasets extends AbstractDatasets {

    private final StaffDataset staff = new StaffDataset();
    private final DeptfDataset deptf = new DeptfDataset();

    @PostConstruct
    @Override
    public void registerFiles() {
        registerFile(staff);
        registerFile(deptf);
        super.registerFiles();
    }

    public StaffDataset getStaff() {
        return staff;
    }

    public DeptfDataset getDeptf() {
        return deptf;
    }
}
