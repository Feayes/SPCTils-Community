package com.reveth.spctils.gui;

import com.reveth.spctils.config.QuestTrackerType;
import com.reveth.spctils.config.SpctilsConfig;
import com.reveth.spctils.features.cooking.BotConfig;
import com.reveth.spctils.features.cooking.BotMode;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class SpctilsSettingsScreen extends Screen {
    private final Screen parent;
    private Tab currentTab = Tab.UTILITIES;

    private final int modalWidth = 260;
    private final int modalHeight = 210;

    public enum Tab { UTILITIES, COOKING, DEBUG, LAYOUT }

    public SpctilsSettingsScreen(Screen parent) {
        super(Text.literal("SPCTils Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.clearChildren();
        
        int modalX = (this.width - modalWidth) / 2;
        int modalY = (this.height - modalHeight) / 2;
        
        int tabBtnWidth = 80;
        int btnWidth = 160;
        int centerX = modalX + modalWidth / 2;
        int centerY = modalY + modalHeight / 2;

        SpctilsConfig.ConfigData config = SpctilsConfig.get();

        // Tabs
        ButtonWidget utilBtn = ButtonWidget.builder(Text.literal("Utilities"), b -> { this.currentTab = Tab.UTILITIES; this.init(); })
                .dimensions(modalX + 10, modalY + 30, tabBtnWidth, 20).build();
        utilBtn.active = (this.currentTab != Tab.UTILITIES);
        this.addDrawableChild(utilBtn);

        ButtonWidget cookBtn = ButtonWidget.builder(Text.literal("Cooking"), b -> { this.currentTab = Tab.COOKING; this.init(); })
                .dimensions(modalX + 10, modalY + 55, tabBtnWidth, 20).build();
        cookBtn.active = (this.currentTab != Tab.COOKING);
        this.addDrawableChild(cookBtn);

        ButtonWidget dbgBtn = ButtonWidget.builder(Text.literal("Debug"), b -> { this.currentTab = Tab.DEBUG; this.init(); })
                .dimensions(modalX + 10, modalY + 80, tabBtnWidth, 20).build();
        dbgBtn.active = (this.currentTab != Tab.DEBUG);
        this.addDrawableChild(dbgBtn);

        ButtonWidget layoutBtn = ButtonWidget.builder(Text.literal("Layout"), b -> { this.currentTab = Tab.LAYOUT; this.init(); })
                .dimensions(modalX + 10, modalY + 105, tabBtnWidth, 20).build();
        layoutBtn.active = (this.currentTab != Tab.LAYOUT);
        this.addDrawableChild(layoutBtn);

        // Content
        int contentX = modalX + 100;

        if (this.currentTab == Tab.UTILITIES) {
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Daily Quest HUD: " + (config.isHudEnabled ? "ON" : "OFF")),
                    button -> {
                        config.isHudEnabled = !config.isHudEnabled;
                        button.setMessage(Text.literal("Daily Quest HUD: " + (config.isHudEnabled ? "ON" : "OFF")));
                        SpctilsConfig.save();
                    })
                    .dimensions(contentX, modalY + 30, btnWidth - 10, 20)
                    .build());

            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Tracker: " + config.activeTracker.getDisplayName()),
                    button -> {
                        QuestTrackerType[] values = QuestTrackerType.values();
                        int nextIndex = (config.activeTracker.ordinal() + 1) % values.length;
                        config.activeTracker = values[nextIndex];
                        button.setMessage(Text.literal("Tracker: " + config.activeTracker.getDisplayName()));
                        SpctilsConfig.save();
                        com.reveth.spctils.features.dailyquest.DailyQuestState.loadFromConfig(); // Reload instead of clear
                    })
                    .dimensions(contentX, modalY + 55, btnWidth - 10, 20)
                    .build());

            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Objective HUD: " + (config.showObjectiveHud ? "ON" : "OFF")),
                    button -> {
                        config.showObjectiveHud = !config.showObjectiveHud;
                        button.setMessage(Text.literal("Objective HUD: " + (config.showObjectiveHud ? "ON" : "OFF")));
                        SpctilsConfig.save();
                    })
                    .dimensions(contentX, modalY + 80, btnWidth - 10, 20)
                    .build());

            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Damage Counter: " + (config.showDamageCounter ? "ON" : "OFF")),
                    button -> {
                        config.showDamageCounter = !config.showDamageCounter;
                        button.setMessage(Text.literal("Damage Counter: " + (config.showDamageCounter ? "ON" : "OFF")));
                        SpctilsConfig.save();
                    })
                    .dimensions(contentX, modalY + 105, btnWidth - 10, 20)
                    .build());

            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Booster UI: " + config.boosterDisplayMode.name()),
                    button -> {
                        SpctilsConfig.BoosterDisplayMode[] values = SpctilsConfig.BoosterDisplayMode.values();
                        int nextIndex = (config.boosterDisplayMode.ordinal() + 1) % values.length;
                        config.boosterDisplayMode = values[nextIndex];
                        button.setMessage(Text.literal("Booster UI: " + config.boosterDisplayMode.name()));
                        SpctilsConfig.save();
                    })
                    .dimensions(contentX, modalY + 130, btnWidth - 10, 20)
                    .build());

            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Reset Quest Data"),
                    button -> {
                        com.reveth.spctils.features.dailyquest.DailyQuestState.clearQuests();
                        SpctilsConfig.save();
                        button.setMessage(Text.literal("Quests Reset!"));
                    })
                    .dimensions(contentX, modalY + 155, btnWidth - 10, 20)
                    .build());
        } else if (this.currentTab == Tab.COOKING) {
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Bot Mode: " + BotConfig.mode.getDisplayName()),
                    button -> {
                        BotMode[] values = BotMode.values();
                        int nextIndex = (BotConfig.mode.ordinal() + 1) % values.length;
                        BotConfig.mode = values[nextIndex];
                        config.autoCookingMode = BotConfig.mode;
                        button.setMessage(Text.literal("Bot Mode: " + BotConfig.mode.getDisplayName()));
                        SpctilsConfig.save();
                    })
                    .dimensions(contentX, modalY + 30, btnWidth - 10, 20)
                    .build());

            // Ticks adjuster with Arrows
            int tickCenterY = modalY + 55;
            
            // Decrease Button
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("<"),
                    button -> {
                        long handle = net.minecraft.client.MinecraftClient.getInstance().getWindow().getHandle();
                        int amount = (org.lwjgl.glfw.GLFW.glfwGetKey(handle, org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT) == org.lwjgl.glfw.GLFW.GLFW_PRESS || org.lwjgl.glfw.GLFW.glfwGetKey(handle, org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT) == org.lwjgl.glfw.GLFW.GLFW_PRESS) ? 10 : 1;
                        BotConfig.clickDelayTicks -= amount;
                        if (BotConfig.clickDelayTicks < 1) BotConfig.clickDelayTicks = 100;
                        config.autoCookingDelayTicks = BotConfig.clickDelayTicks;
                        SpctilsConfig.save();
                        this.init(); // Refresh screen
                    })
                    .dimensions(contentX, tickCenterY, 20, 20)
                    .build());

            // Increase Button
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal(">"),
                    button -> {
                        long handle = net.minecraft.client.MinecraftClient.getInstance().getWindow().getHandle();
                        int amount = (org.lwjgl.glfw.GLFW.glfwGetKey(handle, org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT) == org.lwjgl.glfw.GLFW.GLFW_PRESS || org.lwjgl.glfw.GLFW.glfwGetKey(handle, org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT) == org.lwjgl.glfw.GLFW.GLFW_PRESS) ? 10 : 1;
                        BotConfig.clickDelayTicks += amount;
                        if (BotConfig.clickDelayTicks > 100) BotConfig.clickDelayTicks = 1;
                        config.autoCookingDelayTicks = BotConfig.clickDelayTicks;
                        SpctilsConfig.save();
                        this.init(); // Refresh screen
                    })
                    .dimensions(contentX + btnWidth - 30, tickCenterY, 20, 20)
                    .build());

            // Minigame Selector with Arrows
            int minigameCenterY = modalY + 80;
            
            // Decrease Button
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("<"),
                    button -> {
                        config.autoCookingMinigameSlot = (config.autoCookingMinigameSlot - 1 + 5) % 5;
                        SpctilsConfig.save();
                        this.init(); // Refresh screen
                    })
                    .dimensions(contentX, minigameCenterY, 20, 20)
                    .build());

            // Increase Button
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal(">"),
                    button -> {
                        config.autoCookingMinigameSlot = (config.autoCookingMinigameSlot + 1) % 5;
                        SpctilsConfig.save();
                        this.init(); // Refresh screen
                    })
                    .dimensions(contentX + btnWidth - 30, minigameCenterY, 20, 20)
                    .build());

            // Highlight Color Selector
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Highlight: " + config.cookingHighlightColor.getDisplayName()),
                    button -> {
                        com.reveth.spctils.config.HighlightColor[] values = com.reveth.spctils.config.HighlightColor.values();
                        int nextIndex = (config.cookingHighlightColor.ordinal() + 1) % values.length;
                        config.cookingHighlightColor = values[nextIndex];
                        button.setMessage(Text.literal("Highlight: " + config.cookingHighlightColor.getDisplayName()));
                        SpctilsConfig.save();
                    })
                    .dimensions(contentX, modalY + 105, btnWidth - 10, 20)
                    .build());
        } else if (this.currentTab == Tab.DEBUG) {
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Entity Debug: " + (config.debugEntityEnabled ? "\u00a7aON" : "\u00a7cOFF")),
                    button -> {
                        config.debugEntityEnabled = !config.debugEntityEnabled;
                        button.setMessage(Text.literal("Entity Debug: " + (config.debugEntityEnabled ? "\u00a7aON" : "\u00a7cOFF")));
                        SpctilsConfig.save();
                    })
                    .dimensions(contentX, modalY + 30, btnWidth - 10, 20)
                    .build());

            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Player Debug: " + (config.debugPlayerEnabled ? "\u00a7aON" : "\u00a7cOFF")),
                    button -> {
                        config.debugPlayerEnabled = !config.debugPlayerEnabled;
                        button.setMessage(Text.literal("Player Debug: " + (config.debugPlayerEnabled ? "\u00a7aON" : "\u00a7cOFF")));
                        SpctilsConfig.save();
                    })
                    .dimensions(contentX, modalY + 55, btnWidth - 10, 20)
                    .build());

            boolean rec = com.reveth.spctils.features.debug.PacketRecorder.isRecording();
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal(rec ? "\u00a7cRecording... (click to stop)" : "Record Packets (60s)"),
                    button -> {
                        if (com.reveth.spctils.features.debug.PacketRecorder.isRecording()) {
                            com.reveth.spctils.features.debug.PacketRecorder.stopAndSave();
                            button.setMessage(Text.literal("Record Packets (60s)"));
                        } else {
                            com.reveth.spctils.features.debug.PacketRecorder.start();
                            button.setMessage(Text.literal("\u00a7cRecording... (click to stop)"));
                        }
                    })
                    .dimensions(contentX, modalY + 80, btnWidth - 10, 20)
                    .build());
        } else if (this.currentTab == Tab.LAYOUT) {
            // Profile selector
            int profileCenterY = modalY + 30;
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("<"),
                    button -> {
                        config.activeLayoutProfileIndex = (config.activeLayoutProfileIndex - 1 + 3) % 3;
                        SpctilsConfig.save();
                        this.init(); // Refresh screen
                    })
                    .dimensions(contentX, profileCenterY, 20, 20)
                    .build());

            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Profile " + (config.activeLayoutProfileIndex + 1)),
                    button -> {})
                    .dimensions(contentX + 20, profileCenterY, btnWidth - 50, 20)
                    .build()).active = false; // Just for display

            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal(">"),
                    button -> {
                        config.activeLayoutProfileIndex = (config.activeLayoutProfileIndex + 1) % 3;
                        SpctilsConfig.save();
                        this.init(); // Refresh screen
                    })
                    .dimensions(contentX + btnWidth - 30, profileCenterY, 20, 20)
                    .build());
                    
            // Edit Profile Button
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Edit Profile"),
                    button -> {
                        this.client.setScreen(new SpctilsLayoutEditScreen(this));
                    })
                    .dimensions(contentX, modalY + 55, btnWidth - 10, 20)
                    .build());
        }

        // Done Button
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Done"),
                button -> this.client.setScreen(this.parent))
                .dimensions(centerX - 40, modalY + modalHeight - 30, 80, 20)
                .build());
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        
        int modalX = (this.width - modalWidth) / 2;
        int modalY = (this.height - modalHeight) / 2;
        
        // Compact modal background (Minecraft tooltip style)
        context.fillGradient(modalX, modalY, modalX + modalWidth, modalY + modalHeight, 0xF0101010, 0xF0101010);
        
        // Border using fill (top, bottom, left, right)
        context.fill(modalX - 1, modalY - 1, modalX + modalWidth + 1, modalY, 0xFF555555); // Top
        context.fill(modalX - 1, modalY + modalHeight, modalX + modalWidth + 1, modalY + modalHeight + 1, 0xFF555555); // Bottom
        context.fill(modalX - 1, modalY, modalX, modalY + modalHeight, 0xFF555555); // Left
        context.fill(modalX + modalWidth, modalY, modalX + modalWidth + 1, modalY + modalHeight, 0xFF555555); // Right
        
        // Vertical separator for tabs
        context.fill(modalX + 95, modalY + 25, modalX + 96, modalY + modalHeight - 35, 0xFF555555);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        
        int modalX = (this.width - modalWidth) / 2;
        int modalY = (this.height - modalHeight) / 2;
        
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, modalY + 10, 0xFFFFFF);
        
        if (this.currentTab == Tab.COOKING) {
            int contentX = modalX + 100;
            int tickCenterY = modalY + 55;
            int minigameCenterY = modalY + 80;
            int btnWidth = 160;
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Delay: " + BotConfig.clickDelayTicks + " ticks"), contentX + (btnWidth - 10) / 2, tickCenterY + 6, 0xFFFFFFFF);
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(getMinigameName(SpctilsConfig.get().autoCookingMinigameSlot)), contentX + (btnWidth - 10) / 2, minigameCenterY + 6, 0xFFFFFFFF);
        }
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    private String getMinigameName(int slot) {
        switch(slot) {
            case 0: return "Bread";
            case 1: return "Herbal Broth";
            case 2: return "Hunter's Roast";
            case 3: return "Nectar Stew";
            case 4: return "Cathy Steak";
            default: return "Slot " + (slot + 1);
        }
    }
}
