package com.appruntime;

import java.util.Map;

/**
 * Per-request mutable state representing the CICS execution context. Holds EIB fields, COMMAREA,
 * input fields from terminal, and last SEND MAP data.
 */
public class AppContext {

    private String eibaid;
    private int eibresp;
    private int eibresp2;
    private int eibcalen;
    private String eibfn = "";
    private String eibtrmid = "";
    private int eibdate;
    private int eibtime;
    private String eibtrnid = "";
    private Object commarea;
    private String mapName;
    private Object lastMapOutput;

    /**
     * Symbolic map input object — carries attr/length fields (COBOL REDEFINES maps I↔O to same
     * memory)
     */
    private Object lastMapInput;

    private boolean eraseScreen;
    private boolean freekb;
    private String cursorField;

    /** SEND TEXT/FROM content — raw text sent to terminal (non-BMS) */
    private String sendTextContent;

    /** Input fields from terminal (populated from TerminalInput before mainLine) */
    private Map<String, String> inputFields;

    /**
     * HANDLE ABEND LABEL handler — invoked by the runtime when an AbendException is raised during
     * program execution. Registered via AppService.registerAbendHandler(). null = no active handler
     * (CICS default: propagate abend).
     */
    private Runnable abendHandler;

    public AppContext() {
        this.eibresp = AppResp.NORMAL;
        this.eibcalen = 0;
    }

    public Map<String, String> getInputFields() {
        return inputFields;
    }

    public void setInputFields(Map<String, String> inputFields) {
        this.inputFields = inputFields;
    }

    public String getEibaid() {
        return eibaid;
    }

    public void setEibaid(String eibaid) {
        this.eibaid = eibaid;
    }

    public int getEibresp() {
        return eibresp;
    }

    public void setEibresp(int eibresp) {
        this.eibresp = eibresp;
    }

    public int getEibresp2() {
        return eibresp2;
    }

    public void setEibresp2(int eibresp2) {
        this.eibresp2 = eibresp2;
    }

    public int getEibcalen() {
        return eibcalen;
    }

    public void setEibcalen(int eibcalen) {
        this.eibcalen = eibcalen;
    }

    public Object getCommarea() {
        return commarea;
    }

    public void setCommarea(Object commarea) {
        this.commarea = commarea;
    }

    public String getMapName() {
        return mapName;
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

    public Object getLastMapOutput() {
        return lastMapOutput;
    }

    public void setLastMapOutput(Object lastMapOutput) {
        this.lastMapOutput = lastMapOutput;
    }

    public Object getLastMapInput() {
        return lastMapInput;
    }

    public void setLastMapInput(Object lastMapInput) {
        this.lastMapInput = lastMapInput;
    }

    public boolean isEraseScreen() {
        return eraseScreen;
    }

    public void setEraseScreen(boolean eraseScreen) {
        this.eraseScreen = eraseScreen;
    }

    public boolean isFreekb() {
        return freekb;
    }

    public void setFreekb(boolean freekb) {
        this.freekb = freekb;
    }

    public String getCursorField() {
        return cursorField;
    }

    public void setCursorField(String cursorField) {
        this.cursorField = cursorField;
    }

    public String getEibfn() {
        return eibfn;
    }

    public void setEibfn(String eibfn) {
        this.eibfn = eibfn;
    }

    public String getEibtrmid() {
        return eibtrmid;
    }

    public void setEibtrmid(String eibtrmid) {
        this.eibtrmid = eibtrmid;
    }

    public int getEibdate() {
        return eibdate;
    }

    public void setEibdate(int eibdate) {
        this.eibdate = eibdate;
    }

    public int getEibtime() {
        return eibtime;
    }

    public void setEibtime(int eibtime) {
        this.eibtime = eibtime;
    }

    public String getEibtrnid() {
        return eibtrnid;
    }

    public void setEibtrnid(String eibtrnid) {
        this.eibtrnid = eibtrnid;
    }

    public String getSendTextContent() {
        return sendTextContent;
    }

    public void setSendTextContent(String sendTextContent) {
        this.sendTextContent = sendTextContent;
    }

    public Runnable getAbendHandler() {
        return abendHandler;
    }

    public void setAbendHandler(Runnable abendHandler) {
        this.abendHandler = abendHandler;
    }
}
