package com.sakura.oe0040.runtime;

import com.sakura.runtime.io.*;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Shared file instances for program: OE0040 Mirrors COBOL's single FD per SELECT — ensures
 * consistent file state.
 */
@Component
@Scope("prototype")
public class Oe0040Datasets extends AbstractDatasets {

    private final OrdhfDataset ordhf = new OrdhfDataset();
    private final OrddfDataset orddf = new OrddfDataset();
    private final StokfDataset stokf = new StokfDataset();
    private final CustfDataset custf = new CustfDataset();
    private final ProdfDataset prodf = new ProdfDataset();

    @PostConstruct
    @Override
    public void registerFiles() {
        registerFile(ordhf);
        registerFile(orddf);
        registerFile(stokf);
        registerFile(custf);
        registerFile(prodf);
        super.registerFiles();
    }

    public OrdhfDataset getOrdhf() {
        return ordhf;
    }

    public OrddfDataset getOrddf() {
        return orddf;
    }

    public StokfDataset getStokf() {
        return stokf;
    }

    public CustfDataset getCustf() {
        return custf;
    }

    public ProdfDataset getProdf() {
        return prodf;
    }
}
