package com.sakura.ms0110.runtime;

import com.sakura.runtime.io.*;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Shared file instances for program: MS0110 Mirrors COBOL's single FD per SELECT — ensures
 * consistent file state.
 */
@Component
@Scope("prototype")
public class Ms0110Datasets extends AbstractDatasets {

    private final RegnfDataset regnf = new RegnfDataset();

    @PostConstruct
    @Override
    public void registerFiles() {
        registerFile(regnf);
        super.registerFiles();
    }

    public RegnfDataset getRegnf() {
        return regnf;
    }
}
