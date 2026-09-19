package com.sakura.bt0010.runtime;

import com.sakura.runtime.io.*;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Shared file instances for program: BT0010 Mirrors COBOL's single FD per SELECT — ensures
 * consistent file state.
 */
@Component
@Scope("prototype")
public class Bt0010Datasets extends AbstractDatasets {

    private final InvhfDataset invhf = new InvhfDataset();
    private final SyscfDataset syscf = new SyscfDataset();

    @PostConstruct
    @Override
    public void registerFiles() {
        registerFile(invhf);
        registerFile(syscf);
        super.registerFiles();
    }

    public InvhfDataset getInvhf() {
        return invhf;
    }

    public SyscfDataset getSyscf() {
        return syscf;
    }
}
