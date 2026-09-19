package com.sakura.bt0020.runtime;

import com.sakura.runtime.io.*;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Shared file instances for program: BT0020 Mirrors COBOL's single FD per SELECT — ensures
 * consistent file state.
 */
@Component
@Scope("prototype")
public class Bt0020Datasets extends AbstractDatasets {

    private final SyscfDataset syscf = new SyscfDataset();
    private final InvhfDataset invhf = new InvhfDataset();
    private final PurhfDataset purhf = new PurhfDataset();
    private final ArlfDataset arlf = new ArlfDataset();
    private final AplfDataset aplf = new AplfDataset();

    @PostConstruct
    @Override
    public void registerFiles() {
        registerFile(syscf);
        registerFile(invhf);
        registerFile(purhf);
        registerFile(arlf);
        registerFile(aplf);
        super.registerFiles();
    }

    public SyscfDataset getSyscf() {
        return syscf;
    }

    public InvhfDataset getInvhf() {
        return invhf;
    }

    public PurhfDataset getPurhf() {
        return purhf;
    }

    public ArlfDataset getArlf() {
        return arlf;
    }

    public AplfDataset getAplf() {
        return aplf;
    }
}
