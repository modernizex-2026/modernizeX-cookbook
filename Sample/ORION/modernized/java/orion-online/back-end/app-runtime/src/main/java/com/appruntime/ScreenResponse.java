package com.appruntime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON response representing the screen state after CICS program execution. Sent back to the
 * terminal client with field values, attributes, and button definitions.
 */
public class ScreenResponse implements java.io.Serializable {
    // Serializable: object này nằm trong HttpSession (lastScreen/mapResponse/xctlResponse) —
    // với Spring Session Redis (profile `redis`) session phải serialize được. fields/attrs/
    // cursors/buttons đều là String/Integer/POJO Serializable.
    private static final long serialVersionUID = 1L;

    private String templateName;
    private Map<String, Object> fields;
    private Map<String, String> attrs;
    private Map<String, Integer> cursors;
    private List<ButtonDef> buttons;
    private String message;
    private boolean eraseScreen;
    private boolean freekb;
    private String cursorField;

    /** Active program name — differs from request programName after XCTL. */
    private String programName;

    /** Redirect URL — set when XCTL changes program (client should navigate). */
    private String redirect;

    /** Commarea from RETURN TRANSID — server-side only, not serialized to JSON. */
    @JsonIgnore private Object returnCommarea;

    /** Declared COMMAREA length from RETURN TRANSID LENGTH() — used as EIBCALEN on next request. */
    @JsonIgnore private int returnCommareaLength;

    /** True when program did RETURN without TRANSID (CICS session end). */
    @JsonIgnore private boolean sessionEnd;

    public ScreenResponse() {
        this.fields = new HashMap<>();
        this.attrs = new HashMap<>();
        this.cursors = new HashMap<>();
        this.buttons = new ArrayList<>();
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public Map<String, Object> getFields() {
        return fields;
    }

    public void setFields(Map<String, Object> fields) {
        this.fields = fields;
    }

    public Map<String, String> getAttrs() {
        return attrs;
    }

    public void setAttrs(Map<String, String> attrs) {
        this.attrs = attrs;
    }

    public Map<String, Integer> getCursors() {
        return cursors;
    }

    public void setCursors(Map<String, Integer> cursors) {
        this.cursors = cursors;
    }

    public List<ButtonDef> getButtons() {
        return buttons;
    }

    public void setButtons(List<ButtonDef> buttons) {
        this.buttons = buttons;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public String getRedirect() {
        return redirect;
    }

    public void setRedirect(String redirect) {
        this.redirect = redirect;
    }

    @JsonIgnore
    public Object getReturnCommarea() {
        return returnCommarea;
    }

    public void setReturnCommarea(Object returnCommarea) {
        this.returnCommarea = returnCommarea;
    }

    @JsonIgnore
    public int getReturnCommareaLength() {
        return returnCommareaLength;
    }

    public void setReturnCommareaLength(int returnCommareaLength) {
        this.returnCommareaLength = returnCommareaLength;
    }

    @JsonIgnore
    public boolean isSessionEnd() {
        return sessionEnd;
    }

    public void setSessionEnd(boolean sessionEnd) {
        this.sessionEnd = sessionEnd;
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

    /** Defines a button corresponding to a CICS AID key. */
    public static class ButtonDef implements java.io.Serializable {

        private static final long serialVersionUID = 1L;

        private String aidKey;
        private String label;
        private String cssClass;

        public ButtonDef() {}

        public ButtonDef(String aidKey, String label, String cssClass) {
            this.aidKey = aidKey;
            this.label = label;
            this.cssClass = cssClass;
        }

        public String getAidKey() {
            return aidKey;
        }

        public void setAidKey(String aidKey) {
            this.aidKey = aidKey;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getCssClass() {
            return cssClass;
        }

        public void setCssClass(String cssClass) {
            this.cssClass = cssClass;
        }
    }
}
