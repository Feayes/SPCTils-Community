package com.reveth.spctils.features.dailyquest;

import com.reveth.spctils.config.SpctilsConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

import java.util.List;

public class DailyQuestHud {

    public static void register() {
        HudRenderCallback.EVENT.register(DailyQuestHud::render);
    }

    private static void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options.hudHidden || client.player == null) return;
        
        SpctilsConfig.ConfigData config = SpctilsConfig.get();
        if (!config.isHudEnabled) return;

        List<Quest> quests = DailyQuestState.getActiveQuests();
        TextRenderer textRenderer = client.textRenderer;
        
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        String header = "Daily Quest [" + config.activeTracker.getDisplayName() + "]";
        int headerWidth = textRenderer.getWidth(header);

        SpctilsConfig.HudElementPosition pos = SpctilsConfig.getActiveLayoutProfile().positions.get("daily_quest");
        if (pos == null) pos = new SpctilsConfig.HudElementPosition(0.01, 0.01);
        
        int startX = (int) (screenWidth * pos.xPercent);
        int startY = (int) (screenHeight * pos.yPercent);

        if (quests.isEmpty()) {
            // Draw header and "Not Sync" state
            String notSyncText = "Not Sync";
            
            context.drawTextWithShadow(textRenderer, header, startX, startY, 0xFF55FFFF); // Aqua color
            context.drawTextWithShadow(textRenderer, notSyncText, startX, startY + 12, 0xFFFF5555); // Red color
            return;
        }

        // Calculate maximum width to align right
        int maxTextWidth = 0;
        for (Quest quest : quests) {
            String text = getQuestText(quest);
            int width = textRenderer.getWidth(text);
            if (width > maxTextWidth) {
                maxTextWidth = width;
            }
        }

        // Base coordinates (bottom right)
        // 16 pixels for icon + 4 pixels padding + text width
        int totalWidth = 16 + 4 + maxTextWidth;
        // Make sure total width covers the header if header is wider
        if (headerWidth > totalWidth) {
            totalWidth = headerWidth;
        }
        
        // Base coordinates removed, using startX and startY from Layout Profile
        context.drawTextWithShadow(textRenderer, header, startX, startY, 0xFF55FFFF); // Aqua color

        int yOffset = 12; // below header
        for (Quest quest : quests) {
            int y = startY + yOffset;
            
            // Draw Icon
            if (quest.getIcon() != null && !quest.getIcon().isEmpty()) {
                context.drawItem(quest.getIcon(), startX, y);
            }

            // Draw Text
            String text = getQuestText(quest);
            int color = quest.isCompleted() ? 0xFF55FF55 : 0xFFFFFFFF; // Green if done, else white
            context.drawTextWithShadow(textRenderer, text, startX + 20, y + 4, color);

            yOffset += 20;
        }
    }

    private static String getQuestText(Quest quest) {
        return quest.getTargetName() + ": " + quest.getCurrentProgress() + "/" + quest.getMaxProgress();
    }
}
