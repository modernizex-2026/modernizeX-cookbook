package com.sakura.abortx.runtime;

import com.sakura.abortx.io.*;
import com.sakura.runtime.io.*;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Shared file instances for program: ABORTX Mirrors COBOL's single FD per SELECT — ensures
 * consistent file state.
 */
@Component
@Scope("prototype")
public class AbortxDatasets extends AbstractDatasets {

    private final LogfDataset logf = new LogfDataset();

    @PostConstruct
    @Override
    public void registerFiles() {
        super.registerFiles();
    }

    public LogfDataset getLogf() {
        return logf;
    }
}
