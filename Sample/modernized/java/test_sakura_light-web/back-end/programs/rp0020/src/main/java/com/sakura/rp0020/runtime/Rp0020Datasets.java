package com.sakura.rp0020.runtime;

import com.sakura.runtime.io.*;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Shared file instances for program: RP0020 Mirrors COBOL's single FD per SELECT — ensures
 * consistent file state.
 */
@Component
@Scope("prototype")
public class Rp0020Datasets extends AbstractDatasets {

    private final ProdfDataset prodf = new ProdfDataset();
    private final CatgfDataset catgf = new CatgfDataset();
    private final SyscfDataset syscf = new SyscfDataset();
    private final RepfDataset repf = new RepfDataset();

    @PostConstruct
    @Override
    public void registerFiles() {
        registerFile(prodf);
        registerFile(catgf);
        registerFile(syscf);
        super.registerFiles();
    }

    public ProdfDataset getProdf() {
        return prodf;
    }

    public CatgfDataset getCatgf() {
        return catgf;
    }

    public SyscfDataset getSyscf() {
        return syscf;
    }

    public RepfDataset getRepf() {
        return repf;
    }
}
