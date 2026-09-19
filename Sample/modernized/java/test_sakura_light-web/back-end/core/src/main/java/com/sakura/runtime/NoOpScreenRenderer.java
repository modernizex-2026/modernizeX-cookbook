package com.sakura.runtime;

import com.sakura.runtime.ScreenModels.*;
import com.sakura.runtime.record.RuntimeFieldAccess;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Stub ScreenRendererInstance bean. Spring DI default — the real renderer (WebSocketScreenRenderer)
 * is bound per session at WebSocket connect time via ScreenRendererAware.setRenderer(). Tests
 * inject CapturingScreenRenderer the same way.
 *
 * <p>All methods are no-ops so a service running before setRenderer() (or in an environment without
 * screen output, e.g. a bare unit test) will not NPE.
 */
@Component
@Scope("prototype")
public class NoOpScreenRenderer implements ScreenRendererInstance {

    @Override
    public void displayScreen(ScreenDef def, RuntimeFieldAccess fields) {}

    @Override
    public void displayField(InputFieldDef field, String value) {}

    @Override
    public void showMessage(String msg) {}

    @Override
    public void guideDisplay(String text) {}

    @Override
    public String acceptField(InputFieldDef field) {
        return "";
    }

    @Override
    public String acceptAt(int line, int col, int width) {
        return "";
    }

    @Override
    public String readEndStatus() {
        return "00";
    }

    @Override
    public void openWindow(WindowDef win) {}

    @Override
    public void closeWindow() {}

    @Override
    public int getWindowLineOffset() {
        return 0;
    }

    @Override
    public int getWindowColOffset() {
        return 0;
    }

    @Override
    public boolean hasActiveWindow() {
        return false;
    }

    @Override
    public void clearScreen() {}

    @Override
    public void cursorToBottom() {}
}
