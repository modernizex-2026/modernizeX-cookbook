package com.sakura.getmsg.runtime;

import com.sakura.runtime.io.*;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Shared file instances for program: GETMSG Mirrors COBOL's single FD per SELECT — ensures
 * consistent file state.
 */
@Component
@Scope("prototype")
public class GetmsgDatasets extends AbstractDatasets {

    private final MsgfDataset msgf = new MsgfDataset();

    @PostConstruct
    @Override
    public void registerFiles() {
        registerFile(msgf);
        super.registerFiles();
    }

    public MsgfDataset getMsgf() {
        return msgf;
    }
}
