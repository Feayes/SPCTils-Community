package com.reveth.spctils.config;

import java.util.regex.Pattern;

public enum QuestTrackerType {
    DOJO("Dojo", Pattern.compile("(?i)Kira Daily Quest.*")),
    MAMARAT("Mamarat", Pattern.compile("(?i)Mama Rat Raid Entrance.*")),
    ANOMALY("Anomaly", Pattern.compile("(?i)Destroy The Anomaly Raid.*")),
    GUILD("Guild", Pattern.compile("(?i)Guild Daily Quest.*"));

    private final String displayName;
    private final Pattern titlePattern;

    QuestTrackerType(String displayName, Pattern titlePattern) {
        this.displayName = displayName;
        this.titlePattern = titlePattern;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Pattern getTitlePattern() {
        return titlePattern;
    }
}
