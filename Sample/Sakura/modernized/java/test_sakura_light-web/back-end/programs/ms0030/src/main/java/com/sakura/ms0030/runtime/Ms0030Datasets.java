package com.sakura.ms0030.runtime;

import com.sakura.runtime.io.*;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Shared file instances for program: MS0030 Mirrors COBOL's single FD per SELECT — ensures
 * consistent file state.
 */
@Component
@Scope("prototype")
public class Ms0030Datasets extends AbstractDatasets {

    private final ProdfDataset prodf = new ProdfDataset();
    private final CatgfDataset catgf = new CatgfDataset();
    private final SuppfDataset suppf = new SuppfDataset();
    private final WhsefDataset whsef = new WhsefDataset();

    @PostConstruct
    @Override
    public void registerFiles() {
        registerFile(prodf);
        registerFile(catgf);
        registerFile(suppf);
        registerFile(whsef);
        super.registerFiles();
    }

    public ProdfDataset getProdf() {
        return prodf;
    }

    public CatgfDataset getCatgf() {
        return catgf;
    }

    public SuppfDataset getSuppf() {
        return suppf;
    }

    public WhsefDataset getWhsef() {
        return whsef;
    }
}
