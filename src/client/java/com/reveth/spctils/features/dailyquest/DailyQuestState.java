package com.reveth.spctils.features.dailyquest;

import com.reveth.spctils.config.SpctilsConfig;

import java.util.ArrayList;
import java.util.List;

public class DailyQuestState {
    private static final List<Quest> activeQuests = new ArrayList<>();

    public static void loadFromConfig() {
        activeQuests.clear();
        SpctilsConfig.ConfigData config = SpctilsConfig.get();
        if (config.savedQuestsMap == null) {
            config.savedQuestsMap = new java.util.HashMap<>();
        }
        List<Quest> quests = config.savedQuestsMap.get(config.activeTracker);
        if (quests != null) {
            activeQuests.addAll(quests);
        }
    }

    public static void clearQuests() {
        activeQuests.clear();
        SpctilsConfig.ConfigData config = SpctilsConfig.get();
        if (config.savedQuestsMap != null) {
            config.savedQuestsMap.put(config.activeTracker, new ArrayList<>());
        }
        SpctilsConfig.save();
    }

    public static void clearAllQuests() {
        activeQuests.clear();
        SpctilsConfig.ConfigData config = SpctilsConfig.get();
        if (config.savedQuestsMap != null) {
            config.savedQuestsMap.clear();
        }
        SpctilsConfig.save();
    }

    /**
     * Resets progress of all quests to 0 without removing them.
     * Used on daily reset so user doesn't need to re-sync.
     */
    public static void resetAllQuestProgress() {
        for (Quest quest : activeQuests) {
            quest.resetProgress();
        }
        // Also reset in the savedQuestsMap
        SpctilsConfig.ConfigData config = SpctilsConfig.get();
        if (config.savedQuestsMap != null) {
            for (java.util.List<Quest> quests : config.savedQuestsMap.values()) {
                if (quests != null) {
                    for (Quest quest : quests) {
                        quest.resetProgress();
                    }
                }
            }
        }
        SpctilsConfig.save();
    }

    public static void addQuest(Quest quest) {
        activeQuests.add(quest);
        SpctilsConfig.ConfigData config = SpctilsConfig.get();
        if (config.savedQuestsMap == null) {
            config.savedQuestsMap = new java.util.HashMap<>();
        }
        config.savedQuestsMap.computeIfAbsent(config.activeTracker, k -> new ArrayList<>()).add(quest);
        SpctilsConfig.save();
    }

    public static List<Quest> getActiveQuests() {
        return new ArrayList<>(activeQuests); // Return copy to prevent concurrent modification
    }

    public static List<Quest> getAllTrackedQuests() {
        List<Quest> allQuests = new ArrayList<>();
        SpctilsConfig.ConfigData config = SpctilsConfig.get();
        if (config.savedQuestsMap != null) {
            for (List<Quest> quests : config.savedQuestsMap.values()) {
                if (quests != null) {
                    allQuests.addAll(quests);
                }
            }
        }
        return allQuests;
    }
}
