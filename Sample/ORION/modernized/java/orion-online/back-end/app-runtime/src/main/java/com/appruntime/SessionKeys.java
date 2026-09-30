package com.appruntime;

/**
 * Centralized session key construction for CICS pseudo-conversational state. All keys are scoped by
 * (tabId, programName) to support multi-tab isolation. Each browser tab = one virtual CICS terminal
 * with independent state.
 */
public final class SessionKeys {

    private static final String DEFAULT_TAB = "main";

    private SessionKeys() {}

    private static String prefix(String tabId, String programName) {
        String tab = (tabId != null && !tabId.isEmpty()) ? tabId : DEFAULT_TAB;
        return tab + ":" + programName.toUpperCase();
    }

    // --- Tab-scoped keys (multi-tab safe) ---

    public static String commarea(String programName) {
        return commarea(programName, null);
    }

    public static String commarea(String programName, String tabId) {
        return "commarea:" + prefix(tabId, programName);
    }

    public static String commareaLength(String programName) {
        return commareaLength(programName, null);
    }

    public static String commareaLength(String programName, String tabId) {
        return "commarea-length:" + prefix(tabId, programName);
    }

    public static String xctlResponse(String programName) {
        return xctlResponse(programName, null);
    }

    public static String xctlResponse(String programName, String tabId) {
        return "xctl-response:" + prefix(tabId, programName);
    }

    public static String mapResponse(String programName) {
        return mapResponse(programName, null);
    }

    public static String mapResponse(String programName, String tabId) {
        return "map-response:" + prefix(tabId, programName);
    }

    public static String lastFields(String programName, String mapName) {
        return lastFields(programName, mapName, null);
    }

    public static String lastFields(String programName, String mapName, String tabId) {
        return "last-fields:" + prefix(tabId, programName) + ":" + mapName.toUpperCase();
    }

    public static String lastScreen(String programName) {
        return lastScreen(programName, null);
    }

    public static String lastScreen(String programName, String tabId) {
        return "last-screen:" + prefix(tabId, programName);
    }
}
