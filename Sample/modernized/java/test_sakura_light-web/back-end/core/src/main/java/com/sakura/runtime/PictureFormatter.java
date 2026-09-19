package com.sakura.runtime;

/**
 * COBOL numeric PIC editing (MOVE-to-edited). Byte-verified vs GnuCOBOL. Shared by ScreenModels
 * (screen) + service MOVE-to-edited-field (data).
 */
public final class PictureFormatter {
    private PictureFormatter() {}

    /**
     * Edit numeric {@code value} into COBOL edited {@code picture}. Handles zero-suppression (Z),
     * insertion (, B 0 /), floating + trailing sign (-/+), CR/DB, repeat (n), decimal alignment,
     * overflow truncation.
     */
    public static String edit(String value, String picture) {
        if (value == null) {
            value = "";
        }
        if (picture == null || picture.isEmpty()) {
            return value;
        }
        String pic = expand(picture.toUpperCase());
        String trailing = "";
        boolean leadSign = !pic.isEmpty() && (pic.charAt(0) == '-' || pic.charAt(0) == '+');
        if (pic.endsWith("CR")) {
            trailing = "CR";
            pic = pic.substring(0, pic.length() - 2);
        } else if (pic.endsWith("DB")) {
            trailing = "DB";
            pic = pic.substring(0, pic.length() - 2);
        } else if (!pic.isEmpty() && (pic.endsWith("-") || pic.endsWith("+")) && !leadSign) {
            trailing = pic.substring(pic.length() - 1);
            pic = pic.substring(0, pic.length() - 1);
        }
        int dot = pic.indexOf('.');
        String it;
        String ft;
        if (dot >= 0) {
            it = pic.substring(0, dot);
            ft = pic.substring(dot + 1);
        } else {
            it = pic;
            ft = "";
        }
        char floatSign =
                (!it.isEmpty() && (it.charAt(0) == '-' || it.charAt(0) == '+')) ? it.charAt(0) : 0;
        int dCount = 0;
        for (char c : it.toCharArray()) {
            if (c == '9' || c == 'Z' || (floatSign != 0 && c == floatSign)) {
                dCount++;
            }
        }
        int capacity = dCount - (floatSign != 0 ? 1 : 0);
        if (capacity < 0) {
            capacity = 0;
        }
        int fd = 0;
        for (char c : ft.toCharArray()) {
            if (c == '9' || c == 'Z' || (floatSign != 0 && c == floatSign)) {
                fd++;
            }
        }
        boolean neg = value.indexOf('-') >= 0;
        String num = value.replaceAll("[^0-9.]", "");
        String vi;
        String vf;
        int vd = num.indexOf('.');
        if (vd >= 0) {
            vi = num.substring(0, vd);
            vf = num.substring(vd + 1);
        } else {
            vi = num;
            vf = "";
        }
        if (vi.isEmpty()) {
            vi = "0";
        }
        if (vf.length() > fd) {
            vf = vf.substring(0, fd);
        } else {
            vf = vf + "0".repeat(Math.max(0, fd - vf.length()));
        }
        if (vi.length() > capacity) {
            vi = vi.substring(vi.length() - capacity);
        } else {
            vi = "0".repeat(Math.max(0, capacity - vi.length())) + vi;
        }
        java.util.List<Integer> dpos = new java.util.ArrayList<>();
        for (int i = 0; i < it.length(); i++) {
            char t = it.charAt(i);
            if (t == '9' || t == 'Z' || (floatSign != 0 && t == floatSign)) {
                dpos.add(i);
            }
        }
        char[] cellDigit = new char[it.length()];
        int firstDigit = dCount - capacity;
        for (int j = 0; j < dpos.size(); j++) {
            if (j >= firstDigit) {
                cellDigit[dpos.get(j)] = vi.charAt(j - firstDigit);
            }
        }
        char[] outc = new char[it.length()];
        boolean sup = true;
        for (int i = 0; i < it.length(); i++) {
            char t = it.charAt(i);
            boolean isDp = (t == '9' || t == 'Z' || (floatSign != 0 && t == floatSign));
            if (isDp) {
                char d = cellDigit[i];
                if (t == '9') {
                    sup = false;
                    outc[i] = (d == 0 ? '0' : d);
                } else {
                    if (d == 0) {
                        outc[i] = ' ';
                    } else if (sup && d == '0') {
                        outc[i] = ' ';
                    } else {
                        sup = false;
                        outc[i] = d;
                    }
                }
            } else if (t == ',') {
                outc[i] = sup ? ' ' : ',';
            } else if (t == 'B') {
                outc[i] = ' ';
            } else if (t == '0') {
                outc[i] = '0';
            } else if (t == '/') {
                outc[i] = '/';
            } else {
                outc[i] = t;
            }
        }
        StringBuilder o = new StringBuilder();
        o.append(outc);
        if (floatSign != 0) {
            int fs = -1;
            for (int i = 0; i < o.length(); i++) {
                char c = o.charAt(i);
                if (c >= '0' && c <= '9') {
                    fs = i;
                    break;
                }
            }
            char innerTernary = floatSign == '+' ? '+' : ' ';
            char s = neg ? '-' : innerTernary;
            if (fs > 0 && s != ' ') {
                o.setCharAt(fs - 1, s);
            }
        }
        if (dot >= 0) {
            o.append('.');
            int fi = 0;
            for (int k = 0; k < ft.length(); k++) {
                char t = ft.charAt(k);
                if (t == '9' || t == 'Z' || (floatSign != 0 && t == floatSign)) {
                    o.append(vf.charAt(fi++));
                } else if (t == 'B') {
                    o.append(' ');
                } else if (t == '0') {
                    o.append('0');
                } else {
                    o.append(t);
                }
            }
        }
        if (trailing.equals("CR")) {
            o.append(neg ? "CR" : "  ");
        } else if (trailing.equals("DB")) {
            o.append(neg ? "DB" : "  ");
        } else if (trailing.equals("-")) {
            o.append(neg ? '-' : ' ');
        } else if (trailing.equals("+")) {
            o.append(neg ? '-' : '+');
        }
        boolean has9 = (it + ft).indexOf('9') >= 0;
        boolean allZero = (vi + vf).replace("0", "").isEmpty();
        if (allZero && !has9) {
            return " ".repeat(o.length());
        }
        return o.toString();
    }

    /** Expand COBOL repeat notation: Z(11)9- -> ZZZZZZZZZZZ9- . */
    private static String expand(String pic) {
        StringBuilder o = new StringBuilder();
        int i = 0;
        while (i < pic.length()) {
            char c = pic.charAt(i);
            if (i + 1 < pic.length() && pic.charAt(i + 1) == '(') {
                int cl = pic.indexOf(')', i + 1);
                int n = Integer.parseInt(pic.substring(i + 2, cl));
                for (int k = 0; k < n; k++) {
                    o.append(c);
                }
                i = cl + 1;
            } else {
                o.append(c);
                i++;
            }
        }
        return o.toString();
    }
}
