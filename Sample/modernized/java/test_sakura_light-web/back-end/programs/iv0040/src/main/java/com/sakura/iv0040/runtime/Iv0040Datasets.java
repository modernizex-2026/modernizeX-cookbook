package com.sakura.iv0040.runtime;

import com.sakura.runtime.io.*;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Shared file instances for program: IV0040 Mirrors COBOL's single FD per SELECT — ensures
 * consistent file state.
 */
@Component
@Scope("prototype")
public class Iv0040Datasets extends AbstractDatasets {

    private final SmovfDataset smovf = new SmovfDataset();
    private final ProdfDataset prodf = new ProdfDataset();

    @PostConstruct
    @Override
    public void registerFiles() {
        registerFile(smovf);
        registerFile(prodf);
        super.registerFiles();
    }

    public SmovfDataset getSmovf() {
        return smovf;
    }

    public ProdfDataset getProdf() {
        return prodf;
    }
}
