package com.sakura.ms0040.runtime;

import com.sakura.runtime.io.*;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Shared file instances for program: MS0040 Mirrors COBOL's single FD per SELECT — ensures
 * consistent file state.
 */
@Component
@Scope("prototype")
public class Ms0040Datasets extends AbstractDatasets {

    private final WhsefDataset whsef = new WhsefDataset();
    private final StaffDataset staff = new StaffDataset();

    @PostConstruct
    @Override
    public void registerFiles() {
        registerFile(whsef);
        registerFile(staff);
        super.registerFiles();
    }

    public WhsefDataset getWhsef() {
        return whsef;
    }

    public StaffDataset getStaff() {
        return staff;
    }
}
