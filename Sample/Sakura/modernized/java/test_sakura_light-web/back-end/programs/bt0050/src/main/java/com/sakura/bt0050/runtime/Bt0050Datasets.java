package com.sakura.bt0050.runtime;

import com.sakura.runtime.io.*;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Shared file instances for program: BT0050 Mirrors COBOL's single FD per SELECT — ensures
 * consistent file state.
 */
@Component
@Scope("prototype")
public class Bt0050Datasets extends AbstractDatasets {

    private final AplfDataset aplf = new AplfDataset();
    private final SuppfDataset suppf = new SuppfDataset();

    @PostConstruct
    @Override
    public void registerFiles() {
        registerFile(aplf);
        registerFile(suppf);
        super.registerFiles();
    }

    public AplfDataset getAplf() {
        return aplf;
    }

    public SuppfDataset getSuppf() {
        return suppf;
    }
}
