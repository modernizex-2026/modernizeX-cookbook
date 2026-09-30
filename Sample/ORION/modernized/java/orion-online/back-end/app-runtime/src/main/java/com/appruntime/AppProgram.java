package com.appruntime;

import java.util.List;

/** Interface that each converted CICS program must implement. */
public interface AppProgram {

    /**
     * Returns the COBOL program name (e.g., "CUSTPGM"). Used by AppRunner to index programs for
     * XCTL/LINK lookup.
     */
    String getProgramName();

    /**
     * Main entry point — corresponds to the COBOL PROCEDURE DIVISION.
     *
     * @param appService the CICS service providing all EXEC CICS operations
     */
    void mainLine(AppService appService);

    /**
     * Returns the button definitions for this program's screen. Each program may expose a different
     * set of PF-key buttons.
     */
    List<ScreenResponse.ButtonDef> getButtonDefs();

    /**
     * Registers FSET fields for each map this program uses. Called once during program registration
     * so that MapBinder knows which fields should always be transmitted.
     *
     * @param runner the AppRunner to register FSET fields with
     */
    void registerFsetFields(AppRunner runner);

    /**
     * Returns structured field mapping for the specified BMS map. Contains explicit role
     * classification (dataIn, dataOut, attr, length) so MapBinder does not need to inspect field
     * name suffixes.
     *
     * <p>The generator produces this mapping authoritatively.
     *
     * @param mapName the BMS map name (e.g., "CUSTSCR")
     * @return FieldMapping with role-classified BMS↔Java field mappings, or empty if unknown
     */
    default FieldMapping getFieldMapping(String mapName) {
        return FieldMapping.empty();
    }

    /** CICS Transaction ID from RETURN TRANSID. Null if unknown. */
    default String getTransId() {
        return null;
    }
}
