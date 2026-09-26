package com.reveth.spctils.features.cooking;

/**
 * Tiga mode operasi AutoKoki Bot.
 */
public enum BotMode {
    /** Highlight slot target, player klik manual */
    MANUAL("Manual"),

    /** Auto-klik item di minigame, masuk minigame dilakukan manual oleh player */
    AUTO("Auto"),

    /**
     * Sepenuhnya otomatis:
     *  1. Right-click block/entity untuk membuka cooking menu
     *  2. Klik slot 0 dari cooking menu → masuk minigame
     *  3. Auto-klik item sampai selesai
     *  4. Loop ke langkah 1
     */
    FULL_AUTO("Full-Auto");

    private final String displayName;

    BotMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
