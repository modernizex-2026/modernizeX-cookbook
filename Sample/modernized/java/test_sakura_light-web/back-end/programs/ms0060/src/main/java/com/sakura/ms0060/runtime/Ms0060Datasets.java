package com.sakura.ms0060.runtime;

import com.sakura.runtime.io.*;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Shared file instances for program: MS0060 Mirrors COBOL's single FD per SELECT — ensures
 * consistent file state.
 */
@Component
@Scope("prototype")
public class Ms0060Datasets extends AbstractDatasets {

    private final CatgfDataset catgf = new CatgfDataset();

    @PostConstruct
    @Override
    public void registerFiles() {
        registerFile(catgf);
        super.registerFiles();
    }

    public CatgfDataset getCatgf() {
        return catgf;
    }
}
