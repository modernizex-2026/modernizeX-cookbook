package com.sakura.runtime.conventions;

/**
 * NEC WEBCOBOL AID code registry — single source of truth for COBOL `EVALUATE END-STATUS` matching
 * across generated services and runtime.
 *
 * <p>Categories:
 *
 * <ul>
 *   <li>Enter ({@code ENTER}) — the plain Enter keypress, ESTS={@code '00'}.
 *   <li>PF keys ({@code P1}..{@code PC}) — standard function keys F1..F12.
 *   <li>NEC ROLL ({@code BW}, {@code FW}) — page back / forward, bound to F7/F8 on modern keyboards
 *       (translated by frontend composable, see {@code aidCodes.ts}).
 *   <li>Action codes ({@code ADV}, {@code ESTS_I}, {@code ESTS_II}, {@code BTB}) — from the CWK001
 *       copybook used by every screen-driving program.
 * </ul>
 *
 * <p>Auto-generated — do not edit. Source of generator: {@code
 * JavaScreenRendererGenerator.generateWebAidCodes}. Frontend mirror: {@code
 * frontend-vue/src/composables/aidCodes.ts}.
 */
public final class WebAidCodes {

    private WebAidCodes() {
        /* constants only */
    }

    // ── Enter ──
    /** Plain Enter keypress. COBOL `EVALUATE END-STATUS WHEN ESTS-IT0`. */
    public static final String ENTER = "00";

    // ── Standard PF keys (F1..F12 → P1..PC) ──
    public static final String P1 = "P1";
    public static final String P2 = "P2";
    public static final String P3 = "P3";
    public static final String P4 = "P4";
    public static final String P5 = "P5";
    public static final String P6 = "P6";
    public static final String P7 = "P7";
    public static final String P8 = "P8";
    public static final String P9 = "P9";
    public static final String PA = "PA"; // F10
    public static final String PB = "PB"; // F11
    public static final String PC = "PC"; // F12

    // ── NEC WEBCOBOL ROLL convention (separate physical keys on legacy terminal) ──
    /** ROLL-DOWN / backward page. Bound to F7 on modern keyboards. */
    public static final String BW = "BW";

    /** ROLL-UP / forward page. Bound to F8 on modern keyboards. */
    public static final String FW = "FW";

    // ── Action codes (CWK001 copybook) ──
    /** `ESTS-ADV` — `一括印刷` / `確定` advance buttons. */
    public static final String ADV = "04";

    /** `ESTS-I` — `承認` (approve) button. */
    public static final String ESTS_I = "02";

    /** `ESTS-II` — `否認` / `取消` / `削除` (deny / cancel / delete-mode toggle, e.g. F4). */
    public static final String ESTS_II = "03";

    /** `ESTS-BTB` — `戻る` (back-tab to previous screen). */
    public static final String BTB = "09";

    // ── Helpers ──

    /**
     * Apply NEC ROLL convention translation. P7 → BW, P8 → FW; otherwise pass-through. Symmetric
     * with frontend {@code translateAidKey} in {@code aidCodes.ts}.
     */
    public static String translatePf(String raw) {
        if (raw == null) {
            return null;
        }
        switch (raw) {
            case "P7":
                return BW;
            case "P8":
                return FW;
            default:
                return raw;
        }
    }
}
