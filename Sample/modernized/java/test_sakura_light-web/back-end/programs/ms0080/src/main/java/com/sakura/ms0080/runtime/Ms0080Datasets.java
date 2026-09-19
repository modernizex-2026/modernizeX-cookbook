package com.sakura.ms0080.runtime;

import com.sakura.runtime.io.*;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Shared file instances for program: MS0080 Mirrors COBOL's single FD per SELECT — ensures
 * consistent file state.
 */
@Component
@Scope("prototype")
public class Ms0080Datasets extends AbstractDatasets {

    private final DeptfDataset deptf = new DeptfDataset();

    @PostConstruct
    @Override
    public void registerFiles() {
        registerFile(deptf);
        super.registerFiles();
    }

    public DeptfDataset getDeptf() {
        return deptf;
    }
}
