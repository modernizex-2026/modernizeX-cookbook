package com.sakura.pu0030.runtime;

import com.sakura.runtime.io.*;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Shared file instances for program: PU0030 Mirrors COBOL's single FD per SELECT — ensures
 * consistent file state.
 */
@Component
@Scope("prototype")
public class Pu0030Datasets extends AbstractDatasets {

    private final RcvhfDataset rcvhf = new RcvhfDataset();
    private final RcvdfDataset rcvdf = new RcvdfDataset();
    private final PurhfDataset purhf = new PurhfDataset();
    private final PurdfDataset purdf = new PurdfDataset();
    private final AplfDataset aplf = new AplfDataset();
    private final SuppfDataset suppf = new SuppfDataset();
    private final ProdfDataset prodf = new ProdfDataset();

    @PostConstruct
    @Override
    public void registerFiles() {
        registerFile(rcvhf);
        registerFile(rcvdf);
        registerFile(purhf);
        registerFile(purdf);
        registerFile(aplf);
        registerFile(suppf);
        registerFile(prodf);
        super.registerFiles();
    }

    public RcvhfDataset getRcvhf() {
        return rcvhf;
    }

    public RcvdfDataset getRcvdf() {
        return rcvdf;
    }

    public PurhfDataset getPurhf() {
        return purhf;
    }

    public PurdfDataset getPurdf() {
        return purdf;
    }

    public AplfDataset getAplf() {
        return aplf;
    }

    public SuppfDataset getSuppf() {
        return suppf;
    }

    public ProdfDataset getProdf() {
        return prodf;
    }
}
