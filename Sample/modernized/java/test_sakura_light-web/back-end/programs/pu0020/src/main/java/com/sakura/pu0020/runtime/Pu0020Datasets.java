package com.sakura.pu0020.runtime;

import com.sakura.runtime.io.*;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Shared file instances for program: PU0020 Mirrors COBOL's single FD per SELECT — ensures
 * consistent file state.
 */
@Component
@Scope("prototype")
public class Pu0020Datasets extends AbstractDatasets {

    private final PohfDataset pohf = new PohfDataset();
    private final PodfDataset podf = new PodfDataset();
    private final SuppfDataset suppf = new SuppfDataset();
    private final ProdfDataset prodf = new ProdfDataset();

    @PostConstruct
    @Override
    public void registerFiles() {
        registerFile(pohf);
        registerFile(podf);
        registerFile(suppf);
        registerFile(prodf);
        super.registerFiles();
    }

    public PohfDataset getPohf() {
        return pohf;
    }

    public PodfDataset getPodf() {
        return podf;
    }

    public SuppfDataset getSuppf() {
        return suppf;
    }

    public ProdfDataset getProdf() {
        return prodf;
    }
}
