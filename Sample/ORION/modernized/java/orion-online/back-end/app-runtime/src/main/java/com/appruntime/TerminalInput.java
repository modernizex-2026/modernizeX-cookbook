package com.appruntime;

import java.util.HashMap;
import java.util.Map;

/**
 * JSON request representing terminal input from the client. Contains the program to execute, the
 * AID key pressed, field values, and optional COMMAREA.
 */
public class TerminalInput {

    private String programName;
    private String aidKey;
    private Map<String, String> fields;
    private Object commarea;
    private String currentTemplate;

    /** Declared COMMAREA length from previous RETURN TRANSID — used as EIBCALEN. */
    private int commareaLength;

    /** Unique request ID for server-side deduplication (prevents double-submit). */
    private String requestId;

    /** Tab identifier for multi-tab session isolation. Each browser tab = 1 virtual terminal. */
    private String tabId;

    public TerminalInput() {
        this.fields = new HashMap<>();
    }

    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public String getAidKey() {
        return aidKey;
    }

    public void setAidKey(String aidKey) {
        this.aidKey = aidKey;
    }

    public Map<String, String> getFields() {
        return fields;
    }

    public void setFields(Map<String, String> fields) {
        this.fields = fields;
    }

    public Object getCommarea() {
        return commarea;
    }

    public void setCommarea(Object commarea) {
        this.commarea = commarea;
    }

    public String getCurrentTemplate() {
        return currentTemplate;
    }

    public void setCurrentTemplate(String currentTemplate) {
        this.currentTemplate = currentTemplate;
    }

    public int getCommareaLength() {
        return commareaLength;
    }

    public void setCommareaLength(int commareaLength) {
        this.commareaLength = commareaLength;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getTabId() {
        return tabId;
    }

    public void setTabId(String tabId) {
        this.tabId = tabId;
    }
}
