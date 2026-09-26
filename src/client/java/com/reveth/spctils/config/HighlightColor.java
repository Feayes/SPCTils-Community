package com.reveth.spctils.config;

public enum HighlightColor {
    GREEN  ("Hijau",   0x00FF44),
    RED    ("Merah",   0xFF3333),
    MAGENTA("Magenta", 0xFF22FF);

    private final String displayName;
    /** RGB value (no alpha component) */
    private final int rgb;

    HighlightColor(String displayName, int rgb) {
        this.displayName = displayName;
        this.rgb = rgb;
    }

    public String getDisplayName() { return displayName; }

    /** Returns a fully-opaque ARGB color (0xFF______) */
    public int solidArgb() {
        return 0xFF000000 | rgb;
    }

    /** Returns a semi-transparent ARGB fill color (0xCC______) */
    public int fillArgb() {
        return 0xCC000000 | rgb;
    }
}
