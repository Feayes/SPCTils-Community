package com.reveth.spctils.features.debug;

import com.reveth.spctils.config.SpctilsConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PlayerDebugHud {
    private static final int BG_COLOR = 0xB0000000;
    private static final int PADDING = 6;
    private static final int LINE_HEIGHT = 10;
    private static final int UPDATE_INTERVAL = 20; // Update every 20 ticks (1 sec)
    private int tickCounter = 0;

    private final Map<String, Double> aggregatedStats = new LinkedHashMap<>();
    private final Map<String, Double> percentageStats = new LinkedHashMap<>();
    private final List<String> unparsedLines = new ArrayList<>(); // To see raw strings for debug

    public static void register() {
        PlayerDebugHud hud = new PlayerDebugHud();
        HudRenderCallback.EVENT.register(hud::onHudRender);
    }

    private void onHudRender(DrawContext context, RenderTickCounter tick) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.options.hudHidden) return;
        if (!SpctilsConfig.get().debugPlayerEnabled) return;

        tickCounter++;
        if (tickCounter >= UPDATE_INTERVAL) {
            tickCounter = 0;
            scanInventory(client);
        }

        renderPanel(context, client.textRenderer);
    }

    private void scanInventory(MinecraftClient client) {
        aggregatedStats.clear();
        percentageStats.clear();
        unparsedLines.clear();

        PlayerInventory inventory = client.player.getInventory();
        List<ItemStack> allStacks = new ArrayList<>();
        for (int i = 0; i < inventory.size(); i++) {
            allStacks.add(inventory.getStack(i));
        }

        for (ItemStack stack : allStacks) {
            if (stack == null || stack.isEmpty()) continue;
            LoreComponent lore = stack.get(DataComponentTypes.LORE);
            if (lore == null || lore.lines().isEmpty()) continue;

            for (Text line : lore.lines()) {
                String raw = line.getString().trim();
                if (raw.isEmpty()) continue;

                // Typical stat line: "Damage: 4,817.5" or "Weapon Damage: 59.6%"
                // Format: [Stat Name]: [Value]
                if (raw.contains(":")) {
                    String[] parts = raw.split(":", 2);
                    if (parts.length == 2) {
                        String statName = parts[0].replaceAll("§.", "").trim();
                        String statValStr = parts[1].replaceAll("§.", "").trim();
                        
                        // Try to parse the value (handle comma as thousands separator or decimal? Java Double expects dot. In indo comma could be decimal. Let's just remove commas entirely, assuming 4,817.5 means 4817.5)
                        boolean isPercent = statValStr.contains("%");
                        String numStr = statValStr.replaceAll(",", "").replaceAll("[^0-9.-]", "");
                        if (!numStr.isEmpty() && !numStr.equals("-") && !numStr.equals(".")) {
                            try {
                                double val = Double.parseDouble(numStr);
                                if (isPercent) {
                                    percentageStats.put(statName, percentageStats.getOrDefault(statName, 0.0) + val);
                                } else {
                                    aggregatedStats.put(statName, aggregatedStats.getOrDefault(statName, 0.0) + val);
                                }
                            } catch (NumberFormatException ignored) {}
                        } else {
                            unparsedLines.add(raw);
                        }
                    }
                } else if (raw.matches(".*\\d.*")) { // Has numbers but no colon? Maybe like "+200 Health"
                    unparsedLines.add(raw);
                }
            }
        }
    }

    private void renderPanel(DrawContext context, TextRenderer tr) {
        if (aggregatedStats.isEmpty() && percentageStats.isEmpty() && unparsedLines.isEmpty()) return;

        List<String> displayLines = new ArrayList<>();
        displayLines.add("§a§l[Player Stats Summary]");
        displayLines.add(""); // separator

        if (!aggregatedStats.isEmpty()) {
            displayLines.add("§e§lFlat Stats:");
            for (Map.Entry<String, Double> entry : aggregatedStats.entrySet()) {
                String val = String.format("%,.1f", entry.getValue());
                if (val.endsWith(".0")) val = val.substring(0, val.length() - 2);
                displayLines.add("§7- " + entry.getKey() + ": §f" + val);
            }
            displayLines.add(""); // separator
        }

        if (!percentageStats.isEmpty()) {
            displayLines.add("§6§lPercentage Stats:");
            for (Map.Entry<String, Double> entry : percentageStats.entrySet()) {
                String val = String.format("%,.1f", entry.getValue());
                if (val.endsWith(".0")) val = val.substring(0, val.length() - 2);
                displayLines.add("§7- " + entry.getKey() + ": §f" + val + "%");
            }
            displayLines.add(""); // separator
        }

        if (!unparsedLines.isEmpty() && displayLines.size() < 20) { // Show max 5 unparsed
            displayLines.add("§cUnparsed/Text lines:");
            int count = 0;
            for (String line : unparsedLines) {
                if (count++ >= 5) break;
                displayLines.add("§8> " + line.replaceAll("§.", ""));
            }
        }

        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();
        int maxWidth = 0;
        for (String line : displayLines) {
            int w = tr.getWidth(line.replaceAll("§.", ""));
            if (w > maxWidth) maxWidth = w;
        }

        SpctilsConfig.HudElementPosition pos = SpctilsConfig.getActiveLayoutProfile().positions.get("player_debug");
        if (pos == null) pos = new SpctilsConfig.HudElementPosition(0.8, 0.2);

        int panelX = (int) (screenWidth * pos.xPercent);
        int panelY = (int) (screenHeight * pos.yPercent);

        int curY = panelY + PADDING;
        int textX = panelX + PADDING;

        for (String line : displayLines) {
            if (line.isEmpty()) {
                curY += LINE_HEIGHT / 2;
            } else {
                context.drawTextWithShadow(tr, line, textX, curY, 0xFFFFFFFF);
                curY += LINE_HEIGHT;
            }
        }
    }
}
