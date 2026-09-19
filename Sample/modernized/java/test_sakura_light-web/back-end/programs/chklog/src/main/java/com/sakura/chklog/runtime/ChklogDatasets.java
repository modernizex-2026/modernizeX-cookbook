package com.sakura.chklog.runtime;

import com.sakura.runtime.io.*;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Shared file instances for program: CHKLOG Mirrors COBOL's single FD per SELECT — ensures
 * consistent file state.
 */
@Component
@Scope("prototype")
public class ChklogDatasets extends AbstractDatasets {

    private final UserfDataset userf = new UserfDataset();

    @PostConstruct
    @Override
    public void registerFiles() {
        registerFile(userf);
        super.registerFiles();
    }

    public UserfDataset getUserf() {
        return userf;
    }
}
