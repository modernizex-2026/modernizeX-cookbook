package com.sakura.sl0010.runtime;

import com.sakura.runtime.io.*;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Shared file instances for program: SL0010 Mirrors COBOL's single FD per SELECT — ensures
 * consistent file state.
 */
@Component
@Scope("prototype")
public class Sl0010Datasets extends AbstractDatasets {

    private final InvhfDataset invhf = new InvhfDataset();
    private final InvdfDataset invdf = new InvdfDataset();
    private final ShphfDataset shphf = new ShphfDataset();
    private final ShpdfDataset shpdf = new ShpdfDataset();
    private final OrdhfDataset ordhf = new OrdhfDataset();
    private final StokfDataset stokf = new StokfDataset();
    private final CustfDataset custf = new CustfDataset();
    private final ProdfDataset prodf = new ProdfDataset();
    private final CprcfDataset cprcf = new CprcfDataset();
    private final ArlfDataset arlf = new ArlfDataset();

    @PostConstruct
    @Override
    public void registerFiles() {
        registerFile(invhf);
        registerFile(invdf);
        registerFile(shphf);
        registerFile(shpdf);
        registerFile(ordhf);
        registerFile(stokf);
        registerFile(custf);
        registerFile(prodf);
        registerFile(cprcf);
        registerFile(arlf);
        super.registerFiles();
    }

    public InvhfDataset getInvhf() {
        return invhf;
    }

    public InvdfDataset getInvdf() {
        return invdf;
    }

    public ShphfDataset getShphf() {
        return shphf;
    }

    public ShpdfDataset getShpdf() {
        return shpdf;
    }

    public OrdhfDataset getOrdhf() {
        return ordhf;
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

    public CprcfDataset getCprcf() {
        return cprcf;
    }

    public ArlfDataset getArlf() {
        return arlf;
    }
}
