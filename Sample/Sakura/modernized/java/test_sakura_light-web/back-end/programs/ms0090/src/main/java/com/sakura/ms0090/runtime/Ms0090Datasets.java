package com.sakura.ms0090.runtime;

import com.sakura.runtime.io.*;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Shared file instances for program: MS0090 Mirrors COBOL's single FD per SELECT — ensures
 * consistent file state.
 */
@Component
@Scope("prototype")
public class Ms0090Datasets extends AbstractDatasets {

    private final TaxfDataset taxf = new TaxfDataset();

    @PostConstruct
    @Override
    public void registerFiles() {
        registerFile(taxf);
        super.registerFiles();
    }

    public TaxfDataset getTaxf() {
        return taxf;
    }
}
