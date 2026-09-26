package com.reveth.spctils.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class SpctilsConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "spctils.json");

    private static ConfigData data = new ConfigData();
    
    public enum BoosterDisplayMode {
        CUSTOM, HIDDEN, ORIGINAL
    }

    public static class ConfigData {
        public boolean isHudEnabled = true;
        public QuestTrackerType activeTracker = QuestTrackerType.GUILD;
        public String lastResetDate = "";
        public java.util.Map<QuestTrackerType, java.util.List<com.reveth.spctils.features.dailyquest.Quest>> savedQuestsMap = new java.util.HashMap<>();
        public com.reveth.spctils.features.cooking.BotMode autoCookingMode = com.reveth.spctils.features.cooking.BotMode.AUTO;
        public int autoCookingDelayTicks = 10;
        public int autoCookingMinigameSlot = 1;
        
        public boolean showObjectiveHud = true;
        public HighlightColor cookingHighlightColor = HighlightColor.GREEN;
        public boolean debugEntityEnabled = false;
        public boolean debugPlayerEnabled = false;
        public boolean showDamageCounter = true;
        public BoosterDisplayMode boosterDisplayMode = BoosterDisplayMode.CUSTOM;
        
        public int activeLayoutProfileIndex = 0;
        public java.util.List<LayoutProfile> layoutProfiles = new java.util.ArrayList<>();
        
        public boolean enableExperimentalRpgHud = false;
    }

    public static class HudElementPosition {
        public double xPercent;
        public double yPercent;
        
        public HudElementPosition() {}
        public HudElementPosition(double x, double y) {
            this.xPercent = x;
            this.yPercent = y;
        }
    }
    
    public static class LayoutProfile {
        public java.util.Map<String, HudElementPosition> positions = new java.util.HashMap<>();
    }

    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                data = GSON.fromJson(reader, ConfigData.class);
                com.reveth.spctils.features.cooking.BotConfig.mode = data.autoCookingMode;
                com.reveth.spctils.features.cooking.BotConfig.clickDelayTicks = data.autoCookingDelayTicks;
            } catch (IOException e) {
                System.err.println("Failed to load SPCTils config");
                e.printStackTrace();
            }
        }
        
        // Ensure we have exactly 3 layout profiles
        while (data.layoutProfiles.size() < 3) {
            LayoutProfile p = new LayoutProfile();
            // Default positions
            p.positions.put("daily_quest", new HudElementPosition(0.01, 0.01));
            p.positions.put("objective", new HudElementPosition(0.01, 0.9));
            p.positions.put("damage", new HudElementPosition(0.55, 0.55));
            p.positions.put("booster", new HudElementPosition(0.99, 0.95));
            data.layoutProfiles.add(p);
        }
        
        save();
    }
    
    public static LayoutProfile getActiveLayoutProfile() {
        if (data.activeLayoutProfileIndex < 0 || data.activeLayoutProfileIndex >= data.layoutProfiles.size()) {
            data.activeLayoutProfileIndex = 0;
        }
        return data.layoutProfiles.get(data.activeLayoutProfileIndex);
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(data, writer);
        } catch (IOException e) {
            System.err.println("Failed to save SPCTils config");
            e.printStackTrace();
        }
    }

    public static ConfigData get() {
        return data;
    }
}
