package com.generated.orion.oumqbrg.runtime;

import com.generated.orion.oumqbrg.io.*;
import com.generated.orion.runtime.io.*;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Shared file instances for program: OUMQBRG Mirrors COBOL's single FD per SELECT — ensures
 * consistent file state.
 */
@Component
@Scope("prototype")
public class OumqbrgDatasets extends AbstractDatasets {

    private final TranFileDataset tranFile = new TranFileDataset();

    @PostConstruct
    @Override
    public void registerFiles() {
        registerFile(tranFile);
        super.registerFiles();
    }

    public TranFileDataset getTranFile() {
        return tranFile;
    }
}
