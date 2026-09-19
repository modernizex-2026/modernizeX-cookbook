package com.sakura.oe0010.runtime;

import com.sakura.runtime.io.*;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Shared file instances for program: OE0010 Mirrors COBOL's single FD per SELECT — ensures
 * consistent file state.
 */
@Component
@Scope("prototype")
public class Oe0010Datasets extends AbstractDatasets {

    private final OrdhfDataset ordhf = new OrdhfDataset();
    private final OrddfDataset orddf = new OrddfDataset();
    private final CustfDataset custf = new CustfDataset();
    private final ProdfDataset prodf = new ProdfDataset();
    private final CprcfDataset cprcf = new CprcfDataset();
    private final StokfDataset stokf = new StokfDataset();

    @PostConstruct
    @Override
    public void registerFiles() {
        registerFile(ordhf);
        registerFile(orddf);
        registerFile(custf);
        registerFile(prodf);
        registerFile(cprcf);
        registerFile(stokf);
        super.registerFiles();
    }

    public OrdhfDataset getOrdhf() {
        return ordhf;
    }

    public OrddfDataset getOrddf() {
        return orddf;
    }

    public CustfDataset getCustf() {
        return custf;
    }

    public ProdfDataset getProdf() {
        return prodf;
    }

    public CprcfDataset getCprcf() {
        return cprcf;
    }

    public StokfDataset getStokf() {
        return stokf;
    }
}
