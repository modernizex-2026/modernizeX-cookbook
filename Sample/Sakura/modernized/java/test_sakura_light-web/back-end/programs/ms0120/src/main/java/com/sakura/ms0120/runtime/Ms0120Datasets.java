package com.sakura.ms0120.runtime;

import com.sakura.runtime.io.*;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Shared file instances for program: MS0120 Mirrors COBOL's single FD per SELECT — ensures
 * consistent file state.
 */
@Component
@Scope("prototype")
public class Ms0120Datasets extends AbstractDatasets {

    private final UserfDataset userf = new UserfDataset();
    private final StaffDataset staff = new StaffDataset();

    @PostConstruct
    @Override
    public void registerFiles() {
        registerFile(userf);
        registerFile(staff);
        super.registerFiles();
    }

    public UserfDataset getUserf() {
        return userf;
    }

    public StaffDataset getStaff() {
        return staff;
    }
}
