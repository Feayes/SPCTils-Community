package com.reveth.spctils.features.booster;

import com.reveth.spctils.config.SpctilsConfig;
import com.reveth.spctils.mixin.client.BossBarHudAccessor;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.client.render.RenderTickCounter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BoosterHud {

    public static void register() {
        HudRenderCallback.EVENT.register(BoosterHud::renderHud);
    }

    private static void renderHud(DrawContext context, RenderTickCounter tick) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.options.hudHidden) return;
        
        SpctilsConfig.ConfigData config = SpctilsConfig.get();
        if (config.boosterDisplayMode != SpctilsConfig.BoosterDisplayMode.CUSTOM) {
            return;
        }

        Map<UUID, ClientBossBar> bossBars = ((BossBarHudAccessor) client.inGameHud.getBossBarHud()).getBossBars();
        List<ClientBossBar> boosters = new ArrayList<>();
        
        for (ClientBossBar bar : bossBars.values()) {
            String name = bar.getName().getString();
            if (name.contains("[Weekend Booster]") || name.contains("[Weekday Booster]")) {
                boosters.add(bar);
            }
        }
        
        if (boosters.isEmpty()) return;

        TextRenderer tr = client.textRenderer;
        int padding = 4;
        int lineHeight = 10;
        int boxHeight = 24; // Fits text nicely, like a potion effect box
        
        int screenW = context.getScaledWindowWidth();
        int screenH = context.getScaledWindowHeight();
        
        SpctilsConfig.HudElementPosition pos = SpctilsConfig.getActiveLayoutProfile().positions.get("booster");
        if (pos == null) pos = new SpctilsConfig.HudElementPosition(0.99, 0.95);
        
        int panelX = (int) (screenW * pos.xPercent);
        int curY = (int) (screenH * pos.yPercent);
        
        int maxBoxWidth = 0;
        List<String> displayStrings = new ArrayList<>();
        
        for (ClientBossBar bar : boosters) {
            String fullName = bar.getName().getString().replaceAll("§.", "");
            String displayString = fullName;
            if (fullName.contains("] ")) {
                displayString = fullName.substring(fullName.indexOf("]") + 2).trim();
            }
            
            // Shorten text
            displayString = displayString.replace(" Booster Aktif!", "");
            displayString = displayString.replace("Regeneration", "Regen");
            displayString = displayString.replace(" / 6 Menit", "/6m");
            displayString = displayString.replace(" / 6 menit", "/6m");
            
            // Colors
            if (displayString.toLowerCase().contains("magic find")) {
                displayString = "§d" + displayString; // Pink/Magenta
            } else if (displayString.toLowerCase().contains("resin")) {
                displayString = "§b" + displayString.replace("(+", "§a(+").replace(" (-", "§c(-"); // Light Blue with Green numbers
            }
            
            displayStrings.add(displayString);
            
            int textW = tr.getWidth(displayString);
            int boxWidth = textW + padding * 4; // Extra padding for aesthetics
            if (boxWidth > maxBoxWidth) {
                maxBoxWidth = boxWidth;
            }
        }
        
        for (String displayString : displayStrings) {
            // Adjust X if aligned to the right
            int actualX = panelX;
            if (pos.xPercent > 0.5) {
                actualX -= maxBoxWidth;
            }
            
            // Background
            context.fill(actualX, curY, actualX + maxBoxWidth, curY + boxHeight, 0x90000000);
            
            // Border (Dark Gray)
            int borderColor = 0xFF555555;
            context.fill(actualX, curY, actualX + maxBoxWidth, curY + 1, borderColor); // Top
            context.fill(actualX, curY + boxHeight - 1, actualX + maxBoxWidth, curY + boxHeight, borderColor); // Bottom
            context.fill(actualX, curY, actualX + 1, curY + boxHeight, borderColor); // Left
            context.fill(actualX + maxBoxWidth - 1, curY, actualX + maxBoxWidth, curY + boxHeight, borderColor); // Right
            
            // Text
            int textW = tr.getWidth(displayString);
            int textX = actualX + (maxBoxWidth - textW) / 2; // Center text in the box
            int textY = curY + (boxHeight - 8) / 2;
            
            context.drawTextWithShadow(tr, displayString, textX, textY, 0xFFFFFFFF);
            
            // Stack them vertically upwards if near the bottom, downwards if near the top
            if (pos.yPercent > 0.5) {
                curY -= (boxHeight + 2); // Move up for the next one
            } else {
                curY += (boxHeight + 2); // Move down for the next one
            }
        }
    }
}
