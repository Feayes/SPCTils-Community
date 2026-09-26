package com.reveth.spctils.features.objective;

import com.reveth.spctils.config.SpctilsConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;

public class ObjectiveHud {

    public static void register() {
        HudRenderCallback.EVENT.register(ObjectiveHud::renderHud);
    }

    private static void renderHud(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.options.hudHidden) return;
        
        if (!SpctilsConfig.get().showObjectiveHud) return;

        ClientBossBar objectiveBar = null;
        java.util.Map<java.util.UUID, ClientBossBar> bossBars = ((com.reveth.spctils.mixin.BossBarHudAccessor) client.inGameHud.getBossBarHud()).getBossBars();
        for (ClientBossBar bar : bossBars.values()) {
            // Strip § formatting codes before checking, since server may send colored text
            String plainName = bar.getName().getString().replaceAll("§.", "");
            if (plainName.contains("OBJEKTIF")) {
                objectiveBar = bar;
                break;
            }
        }

        if (objectiveBar != null) {
            TextRenderer textRenderer = client.textRenderer;
            // Use the ordered Text (with colors) for display
            Text text = objectiveBar.getName();
            
            SpctilsConfig.HudElementPosition pos = SpctilsConfig.getActiveLayoutProfile().positions.get("objective");
            if (pos == null) pos = new SpctilsConfig.HudElementPosition(0.01, 0.9);
            
            int x = (int) (client.getWindow().getScaledWidth() * pos.xPercent);
            int y = (int) (client.getWindow().getScaledHeight() * pos.yPercent);
            
            context.drawTextWithShadow(textRenderer, text, x, y, 0xFFFFFFFF);
        }
    }
}
