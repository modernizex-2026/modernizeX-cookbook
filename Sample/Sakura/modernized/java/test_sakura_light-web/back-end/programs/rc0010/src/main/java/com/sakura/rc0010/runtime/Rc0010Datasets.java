package com.sakura.rc0010.runtime;

import com.sakura.runtime.io.*;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Shared file instances for program: RC0010 Mirrors COBOL's single FD per SELECT — ensures
 * consistent file state.
 */
@Component
@Scope("prototype")
public class Rc0010Datasets extends AbstractDatasets {

    private final PohfDataset pohf = new PohfDataset();
    private final PodfDataset podf = new PodfDataset();
    private final RcvhfDataset rcvhf = new RcvhfDataset();
    private final RcvdfDataset rcvdf = new RcvdfDataset();
    private final StokfDataset stokf = new StokfDataset();
    private final SmovfDataset smovf = new SmovfDataset();
    private final SuppfDataset suppf = new SuppfDataset();
    private final ProdfDataset prodf = new ProdfDataset();

    @PostConstruct
    @Override
    public void registerFiles() {
        registerFile(pohf);
        registerFile(podf);
        registerFile(rcvhf);
        registerFile(rcvdf);
        registerFile(stokf);
        registerFile(smovf);
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

    public RcvhfDataset getRcvhf() {
        return rcvhf;
    }

    public RcvdfDataset getRcvdf() {
        return rcvdf;
    }

    public StokfDataset getStokf() {
        return stokf;
    }

    public SmovfDataset getSmovf() {
        return smovf;
    }

    public SuppfDataset getSuppf() {
        return suppf;
    }

    public ProdfDataset getProdf() {
        return prodf;
    }
}
