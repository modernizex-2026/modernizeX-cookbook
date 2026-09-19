package com.sakura.numgen.runtime;

import com.sakura.numgen.io.*;
import com.sakura.runtime.io.*;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Shared file instances for program: NUMGEN Mirrors COBOL's single FD per SELECT — ensures
 * consistent file state.
 */
@Component
@Scope("prototype")
public class NumgenDatasets extends AbstractDatasets {

    private final NumcfDataset numcf = new NumcfDataset();

    @PostConstruct
    @Override
    public void registerFiles() {
        registerFile(numcf);
        super.registerFiles();
    }

    public NumcfDataset getNumcf() {
        return numcf;
    }
}
