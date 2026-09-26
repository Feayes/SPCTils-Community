package com.reveth.spctils.features.damage;

import com.reveth.spctils.config.SpctilsConfig;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DamageTrackerHud {
    private static final int BG_COLOR = 0xB0000000;
    
    private static double totalDamage = 0;
    private static final List<DamageRecord> recentHits = new ArrayList<>();
    private static long lastHitTime = 0;
    
    public static void register() {
        HudRenderCallback.EVENT.register(DamageTrackerHud::renderHud);
    }
    
    public static void addDamage(double dmg) {
        if (!SpctilsConfig.get().showDamageCounter) return;
        
        long now = System.currentTimeMillis();
        
        // Auto-reset if it's been more than 10 seconds (10000 ms) since the last hit
        if (now - lastHitTime > 10000) {
            reset();
        }
        
        recentHits.add(new DamageRecord(now, dmg));
        totalDamage += dmg;
        lastHitTime = now;
    }
    
    public static void reset() {
        totalDamage = 0;
        recentHits.clear();
    }

    private static void renderHud(DrawContext context, RenderTickCounter tick) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.options.hudHidden) return;
        if (!SpctilsConfig.get().showDamageCounter) return;
        
        long now = System.currentTimeMillis();
        // Auto-reset check in render in case no new messages are received to trigger the reset
        if (lastHitTime > 0 && now - lastHitTime > 10000) {
            reset();
        }
        
        // Remove older than 60 seconds (60000 ms) for DPM calculation
        recentHits.removeIf(r -> now - r.timestamp > 60000);

        double dmgLast60s = 0;
        double dmgLast3s = 0;
        
        for (DamageRecord r : recentHits) {
            dmgLast60s += r.damage;
            if (now - r.timestamp <= 3000) {
                dmgLast3s += r.damage;
            }
        }
        
        // DPS is damage in the last 3 seconds divided by 3
        double dps = dmgLast3s / 3.0;
        // DPM is damage in the last 60 seconds
        double dpm = dmgLast60s; 
        
        TextRenderer tr = client.textRenderer;
        
        List<String> lines = new ArrayList<>();
        lines.add(String.format("§fTotal : §e%,.1f", totalDamage));
        lines.add(String.format("§fDPS : §a%,.1f", dps));
        lines.add(String.format("§fDPM : §b%,.1f", dpm));

        int maxWidth = 0;
        for (String line : lines) {
            int w = tr.getWidth(line.replaceAll("§.", ""));
            if (w > maxWidth) maxWidth = w;
        }

        int padding = 4;
        int panelW = maxWidth + padding * 2;
        int panelH = lines.size() * 10 + padding * 2;
        
        int screenW = context.getScaledWindowWidth();
        int screenH = context.getScaledWindowHeight();
        
        SpctilsConfig.HudElementPosition pos = SpctilsConfig.getActiveLayoutProfile().positions.get("damage");
        if (pos == null) pos = new SpctilsConfig.HudElementPosition(0.55, 0.55);
        
        int panelX = (int) (screenW * pos.xPercent);
        int panelY = (int) (screenH * pos.yPercent);
        
        int curY = panelY + padding;
        int textX = panelX + padding;
        
        for (String line : lines) {
            context.drawTextWithShadow(tr, line, textX, curY, 0xFFFFFFFF);
            curY += 10;
        }
    }
    
    private static class DamageRecord {
        long timestamp;
        double damage;
        DamageRecord(long ts, double d) {
            this.timestamp = ts;
            this.damage = d;
        }
    }
}
