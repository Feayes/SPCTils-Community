package com.reveth.spctils.features.rpghud;

import com.reveth.spctils.config.SpctilsConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public class CustomRpgHudRenderer {

    public static void register() {
        HudRenderCallback.EVENT.register(CustomRpgHudRenderer::renderHud);
    }

    private static void renderHud(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.options.hudHidden) return;
        
        if (!SpctilsConfig.get().enableExperimentalRpgHud) return;

        // Validating data, fallback to vanilla if 0 (e.g., haven't received action bar yet)
        double health = RpgHudState.maxHealth > 0 ? RpgHudState.health : client.player.getHealth();
        double maxHealth = RpgHudState.maxHealth > 0 ? RpgHudState.maxHealth : client.player.getMaxHealth();
        
        double mana = RpgHudState.mana;
        double maxMana = RpgHudState.maxMana;
        
        double energy = RpgHudState.energy;
        double maxEnergy = RpgHudState.maxEnergy;
        
        double hunger = client.player.getHungerManager().getFoodLevel();
        double maxHunger = 20.0;
        
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        
        TextRenderer textRenderer = client.textRenderer;
        
        int barWidth = 86;
        int barHeight = 9;
        
        int leftX = screenWidth / 2 - 91;
        int rightX = screenWidth / 2 + 5;
        
        int topY = screenHeight - 50;
        int bottomY = screenHeight - 39;
        
        // Colors
        int bgCol = 0x80000000;
        int manaCol = 0xFF00AAFF; // Blue
        int energyCol = 0xFFFFFF55; // Yellow
        int healthCol = 0xFFFF5555; // Red
        int hungerCol = 0xFFFFAA00; // Orange
        
        // Draw Top-Left: Mana
        drawBar(context, textRenderer, leftX, topY, barWidth, barHeight, mana, maxMana, manaCol, bgCol, "Mana");
        // Draw Top-Right: Energy
        drawBar(context, textRenderer, rightX, topY, barWidth, barHeight, energy, maxEnergy, energyCol, bgCol, "Energy");
        // Draw Bottom-Left: Health
        drawBar(context, textRenderer, leftX, bottomY, barWidth, barHeight, health, maxHealth, healthCol, bgCol, "Health");
        // Draw Bottom-Right: Hunger
        drawBar(context, textRenderer, rightX, bottomY, barWidth, barHeight, hunger, maxHunger, hungerCol, bgCol, "Hunger");
    }
    
    private static void drawBar(DrawContext context, TextRenderer textRenderer, int x, int y, int width, int height, double current, double max, int color, int bgColor, String label) {
        // Background
        context.fill(x, y, x + width, y + height, bgColor);
        
        // Fill
        double ratio = max > 0 ? Math.max(0, Math.min(1.0, current / max)) : 0;
        int fillWidth = (int) (width * ratio);
        if (fillWidth > 0) {
            context.fill(x, y, x + fillWidth, y + height, color);
        }
        
        // Text
        String text = label + " " + (int)current + "/" + (int)max;
        int textWidth = textRenderer.getWidth(text);
        
        // Draw text slightly shadowed for visibility
        context.drawText(textRenderer, text, x + (width - textWidth) / 2, y + 1, 0xFFFFFF, false);
    }
}
