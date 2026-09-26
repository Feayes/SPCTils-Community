package com.reveth.spctils.gui;

import com.reveth.spctils.config.SpctilsConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import java.util.Map;
import java.util.HashMap;

public class SpctilsLayoutEditScreen extends Screen {
    private final Screen parent;
    private SpctilsConfig.LayoutProfile profile;
    private String draggingElement = null;
    
    // For hitboxes
    private final Map<String, Rect> bounds = new HashMap<>();

    public SpctilsLayoutEditScreen(Screen parent) {
        super(Text.literal("Edit Layout"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.clearChildren();
        this.profile = SpctilsConfig.getActiveLayoutProfile();

        // Save Button
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Save"),
                button -> {
                    SpctilsConfig.save();
                    this.client.setScreen(this.parent);
                })
                .dimensions(this.width / 2 - 40, this.height / 2 - 10, 80, 20)
                .build());
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Semi-transparent background
        context.fillGradient(0, 0, this.width, this.height, 0xC0101010, 0xC0101010);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        bounds.clear();
        
        // Draw instructions
        context.drawCenteredTextWithShadow(this.textRenderer, "Drag to move HUD elements", this.width / 2, this.height / 2 - 25, 0xFFFF55);

        int screenW = this.width;
        int screenH = this.height;

        // Render dummy Daily Quest
        SpctilsConfig.HudElementPosition questPos = profile.positions.computeIfAbsent("daily_quest", k -> new SpctilsConfig.HudElementPosition(0.01, 0.01));
        int questX = (int) (screenW * questPos.xPercent);
        int questY = (int) (screenH * questPos.yPercent);
        int questW = 120;
        int questH = 40;
        context.fill(questX, questY, questX + questW, questY + questH, 0x800000AA);
        context.drawTextWithShadow(this.textRenderer, "Daily Quest HUD", questX + 4, questY + 4, 0xFFFFFFFF);
        bounds.put("daily_quest", new Rect(questX, questY, questW, questH));

        // Render dummy Objective
        SpctilsConfig.HudElementPosition objPos = profile.positions.computeIfAbsent("objective", k -> new SpctilsConfig.HudElementPosition(0.01, 0.9));
        int objX = (int) (screenW * objPos.xPercent);
        int objY = (int) (screenH * objPos.yPercent);
        int objW = 100;
        int objH = 20;
        context.fill(objX, objY, objX + objW, objY + objH, 0x8000AA00);
        context.drawTextWithShadow(this.textRenderer, "Objective HUD", objX + 4, objY + 4, 0xFFFFFFFF);
        bounds.put("objective", new Rect(objX, objY, objW, objH));

        // Render dummy Damage Counter
        SpctilsConfig.HudElementPosition dmgPos = profile.positions.computeIfAbsent("damage", k -> new SpctilsConfig.HudElementPosition(0.55, 0.55));
        int dmgX = (int) (screenW * dmgPos.xPercent);
        int dmgY = (int) (screenH * dmgPos.yPercent);
        int dmgW = 100;
        int dmgH = 40;
        context.fill(dmgX, dmgY, dmgX + dmgW, dmgY + dmgH, 0x80AA0000);
        context.drawTextWithShadow(this.textRenderer, "Damage HUD", dmgX + 4, dmgY + 4, 0xFFFFFFFF);
        bounds.put("damage", new Rect(dmgX, dmgY, dmgW, dmgH));

        // Render dummy Player Debug
        SpctilsConfig.HudElementPosition pdPos = profile.positions.computeIfAbsent("player_debug", k -> new SpctilsConfig.HudElementPosition(0.8, 0.2));
        int pdX = (int) (screenW * pdPos.xPercent);
        int pdY = (int) (screenH * pdPos.yPercent);
        int pdW = 100;
        int pdH = 100;
        context.fill(pdX, pdY, pdX + pdW, pdY + pdH, 0x80AAAA00);
        context.drawTextWithShadow(this.textRenderer, "Player Debug", pdX + 4, pdY + 4, 0xFFFFFFFF);
        bounds.put("player_debug", new Rect(pdX, pdY, pdW, pdH));

        // Render dummy Booster HUD
        SpctilsConfig.HudElementPosition bstPos = profile.positions.computeIfAbsent("booster", k -> new SpctilsConfig.HudElementPosition(0.99, 0.95));
        int bstW = 150;
        int bstH = 24;
        int bstX = (int) (screenW * bstPos.xPercent);
        int bstY = (int) (screenH * bstPos.yPercent);
        if (bstPos.xPercent > 0.5) bstX -= bstW;
        context.fill(bstX, bstY, bstX + bstW, bstY + bstH, 0x805500AA);
        context.drawTextWithShadow(this.textRenderer, "Booster HUD", bstX + 4, bstY + 8, 0xFFFFFFFF);
        bounds.put("booster", new Rect(bstX, bstY, bstW, bstH));
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.gui.Click click, boolean bl) {
        if (click.button() == 0) { // Left click
            for (Map.Entry<String, Rect> entry : bounds.entrySet()) {
                if (entry.getValue().contains((int) click.x(), (int) click.y())) {
                    draggingElement = entry.getKey();
                    return true; // Consume event
                }
            }
        }
        return super.mouseClicked(click, bl);
    }

    @Override
    public boolean mouseDragged(net.minecraft.client.gui.Click click, double deltaX, double deltaY) {
        if (draggingElement != null && click.button() == 0) {
            SpctilsConfig.HudElementPosition pos = profile.positions.get(draggingElement);
            if (pos != null) {
                Rect r = bounds.get(draggingElement);
                if (r != null) {
                    double newXPercent = pos.xPercent + (deltaX / (double) this.width);
                    double newYPercent = pos.yPercent + (deltaY / (double) this.height);
                    
                    // Clamp
                    pos.xPercent = Math.max(0.0, Math.min(1.0, newXPercent));
                    pos.yPercent = Math.max(0.0, Math.min(1.0, newYPercent));
                }
            }
            return true;
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(net.minecraft.client.gui.Click click) {
        if (click.button() == 0) {
            draggingElement = null;
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private static class Rect {
        int x, y, w, h;
        Rect(int x, int y, int w, int h) {
            this.x = x; this.y = y; this.w = w; this.h = h;
        }
        boolean contains(int mx, int my) {
            return mx >= x && mx <= x + w && my >= y && my <= y + h;
        }
    }
}
