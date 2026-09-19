package com.sakura.runtime;

import com.sakura.runtime.ScreenModels.*;
import com.sakura.runtime.record.RuntimeFieldAccess;

/**
 * Interface for COBOL SCREEN SECTION rendering. Implementations: ConsoleScreenRenderer (ANSI
 * terminal), WebSocketScreenRenderer (browser). Injected into Service classes that have SCREEN
 * SECTION.
 */
public interface ScreenRendererInstance {

    /** Render a full screen definition. */
    void displayScreen(ScreenDef def, RuntimeFieldAccess fields);

    /** Update a single field on the current screen. */
    void displayField(InputFieldDef field, String value);

    /** Show a message on the status line. */
    void showMessage(String msg);

    /**
     * COBOL {@code STOP 'literal'} / {@code DISPLAY ... UPON GDD} → Guide Display Window: show
     * {@code text} in a blocking dialog and wait until the operator acknowledges (OK/Esc), then
     * return so execution continues with the next statement. Distinct from {@link #showMessage}
     * (non-blocking inline message) and from program termination ({@code STOP RUN} →
     * StopRunSignal).
     */
    void guideDisplay(String text);

    /** Accept input for a specific field. Returns user input string. */
    String acceptField(InputFieldDef field);

    /** Accept input at an arbitrary screen position. */
    String acceptAt(int line, int col, int width);

    /** Read PF key / end status. Returns status code ("00"=Enter, "P9"=PF9, etc.) */
    String readEndStatus();

    /** Open a bordered dialog window overlay. */
    void openWindow(WindowDef win);

    /** Close the active window overlay. */
    void closeWindow();

    /** Get active window line offset for content positioning. */
    int getWindowLineOffset();

    /** Get active window column offset. */
    int getWindowColOffset();

    /** Check if a window is currently active. */
    boolean hasActiveWindow();

    /** Clear the screen. */
    void clearScreen();

    /** Move cursor to bottom of screen. */
    void cursorToBottom();

    /** Session-scoped property access (e.g., SC_ESTS for batch mode). */
    default String getProperty(String key) {
        return System.getProperty(key);
    }

    /**
     * Per-session workstation/terminal name backing the COBOL @WSNAME intrinsic (key for the ALOGIN
     * station lookup). Web clients supply it on WebSocket connect; empty string means "no station"
     * (auth check is skipped).
     */
    default String getStationName() {
        return "";
    }
}
