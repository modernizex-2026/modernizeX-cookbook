package com.sakura.pu0020.screen;

import com.sakura.runtime.ScreenManifestParser;
import com.sakura.runtime.ScreenModels.*;

/**
 * Screen definitions for program PU0020.
 *
 * <p>Auto-generated — do not edit. Backing data: {@code /screens/pu0020/manifest.json}.
 *
 * <p>This class is intentionally THIN: all screen metadata (rows, fields, input field positions,
 * window dimensions) lives in manifest.json. Class init loads + parses the manifest once via {@link
 * ScreenManifestParser#load}. Service code references the static API ({@code getScreen} / {@code
 * getInput} / {@code getWindow}) exactly as before — only the backing implementation changed.
 */
public final class ScreenDefs {

    private static final String MANIFEST_PATH = "/static/screens/pu0020/manifest.json";

    private static final ScreenManifestParser.ParsedManifest DATA =
            ScreenManifestParser.load(MANIFEST_PATH);

    private ScreenDefs() {
        /* static accessor only */
    }

    public static ScreenDef getScreen(String name) {
        return DATA.screens.get(name.toUpperCase());
    }

    public static InputFieldDef getInput(String name) {
        return DATA.inputs.get(name.toUpperCase());
    }

    public static boolean isScreenName(String name) {
        return DATA.screens.containsKey(name.toUpperCase());
    }

    public static WindowDef getWindow(String name) {
        return DATA.windows.get(name.toUpperCase());
    }

    public static boolean isInputField(String name) {
        return DATA.inputs.containsKey(name.toUpperCase());
    }
}
