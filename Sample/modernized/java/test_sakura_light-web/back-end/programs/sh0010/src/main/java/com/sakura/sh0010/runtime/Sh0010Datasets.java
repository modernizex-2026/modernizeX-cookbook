package com.sakura.sh0010.runtime;

import com.sakura.runtime.io.*;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Shared file instances for program: SH0010 Mirrors COBOL's single FD per SELECT — ensures
 * consistent file state.
 */
@Component
@Scope("prototype")
public class Sh0010Datasets extends AbstractDatasets {

    private final OrdhfDataset ordhf = new OrdhfDataset();
    private final OrddfDataset orddf = new OrddfDataset();
    private final ShphfDataset shphf = new ShphfDataset();
    private final ShpdfDataset shpdf = new ShpdfDataset();
    private final StokfDataset stokf = new StokfDataset();
    private final SmovfDataset smovf = new SmovfDataset();
    private final CustfDataset custf = new CustfDataset();
    private final ProdfDataset prodf = new ProdfDataset();

    @PostConstruct
    @Override
    public void registerFiles() {
        registerFile(ordhf);
        registerFile(orddf);
        registerFile(shphf);
        registerFile(shpdf);
        registerFile(stokf);
        registerFile(smovf);
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

    public ShphfDataset getShphf() {
        return shphf;
    }

    public ShpdfDataset getShpdf() {
        return shpdf;
    }

    public StokfDataset getStokf() {
        return stokf;
    }

    public SmovfDataset getSmovf() {
        return smovf;
    }

    public CustfDataset getCustf() {
        return custf;
    }

    public ProdfDataset getProdf() {
        return prodf;
    }
}
