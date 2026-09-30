package com.sakura.rp0090.runtime;

import com.sakura.runtime.io.*;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Shared file instances for program: RP0090 Mirrors COBOL's single FD per SELECT — ensures
 * consistent file state.
 */
@Component
@Scope("prototype")
public class Rp0090Datasets extends AbstractDatasets {

    private final OrdhfDataset ordhf = new OrdhfDataset();
    private final OrddfDataset orddf = new OrddfDataset();
    private final CustfDataset custf = new CustfDataset();
    private final ProdfDataset prodf = new ProdfDataset();
    private final SyscfDataset syscf = new SyscfDataset();
    private final RepfDataset repf = new RepfDataset();

    @PostConstruct
    @Override
    public void registerFiles() {
        registerFile(ordhf);
        registerFile(orddf);
        registerFile(custf);
        registerFile(prodf);
        registerFile(syscf);
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

    public SyscfDataset getSyscf() {
        return syscf;
    }

    public RepfDataset getRepf() {
        return repf;
    }
}
