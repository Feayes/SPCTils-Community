package com.reveth.spctils.features.dailyquest;

import com.reveth.spctils.config.SpctilsConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;
import java.util.ArrayList;

public class DailyQuestManager {

    // Format: "Basmi 100x Forest Skeleton" (can have color codes, but getString() strips some, better to use regex on plain string)
    private static final Pattern TARGET_PATTERN = Pattern.compile("(?i)Basmi\\s+\\d+x\\s+(.+)");
    // Format: "Progress: 46/100" or "Progress: 0/5 Farm Rat"
    private static final Pattern PROGRESS_PATTERN = Pattern.compile("(?i)Progress:\\s*(\\d+)\\s*/\\s*(\\d+)(?:\\s+(.+))?");
    // Format: "Tundukkan Mama Rat di Altar: 0/5"
    private static final Pattern MAMARAT_PATTERN = Pattern.compile("(?i)Tundukkan (Mama Rat) di Altar:\\s*(\\d+)\\s*/\\s*(\\d+)");
    // Format: "Selesaikan Destroy The Anomaly: 0/4"
    private static final Pattern ANOMALY_PATTERN = Pattern.compile("(?i)Selesaikan (Destroy The Anomaly):\\s*(\\d+)\\s*/\\s*(\\d+)");

    private static boolean wasInQuestScreen = false;
    private static long lastTimeCheck = 0;
    public static long lastInteractionTime = 0;

    public static void register() {
        DailyQuestState.loadFromConfig();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Check for daily reset every few seconds to save performance
            long now = System.currentTimeMillis();
            if (now - lastTimeCheck > 5000) {
                lastTimeCheck = now;
                checkDailyReset();
            }

            if (client.options.attackKey.isPressed() || client.options.useKey.isPressed()) {
                lastInteractionTime = now;
            }

            if (client.currentScreen instanceof GenericContainerScreen screen) {
                String title = screen.getTitle().getString();
                Pattern activeTitlePattern = SpctilsConfig.get().activeTracker.getTitlePattern();
                if (activeTitlePattern.matcher(title).matches()) {
                    if (!wasInQuestScreen) {
                        boolean parsedSuccessfully = parseQuestScreen(screen);
                        if (parsedSuccessfully) {
                            wasInQuestScreen = true;
                        }
                    }
                } else {
                    wasInQuestScreen = false;
                }
            } else {
                wasInQuestScreen = false;
            }
        });

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String msg = message.getString().toLowerCase();
            for (Quest quest : DailyQuestState.getAllTrackedQuests()) {
                if (quest.isCompleted()) continue;
                QuestDefinition def = QuestRegistry.getQuest(quest.getTargetName());
                if (def == null) continue;
                
                if (def.trigger == QuestDefinition.Trigger.CHAT_REWARD || def.trigger == QuestDefinition.Trigger.CHAT_RAID) {
                    if (msg.contains(def.triggerTarget.toLowerCase())) {
                        quest.incrementProgress();
                        SpctilsConfig.save();
                        com.reveth.spctils.Spctils.LOGGER.info("[SPCTils] Incremented quest {} to {} via chat", quest.getTargetName(), quest.getCurrentProgress());
                    }
                }
            }
        });
    }

    private static void checkDailyReset() {
        LocalDate currentDate = LocalDate.now(ZoneId.of("Asia/Jakarta"));
        String dateString = currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
        
        SpctilsConfig.ConfigData config = SpctilsConfig.get();
        if (config.lastResetDate == null || config.lastResetDate.isEmpty()) {
            config.lastResetDate = dateString;
            SpctilsConfig.save();
        } else if (!config.lastResetDate.equals(dateString)) {
            // Day has changed - reset progress to 0 but keep quest list
            DailyQuestState.resetAllQuestProgress();
            config.lastResetDate = dateString;
            SpctilsConfig.save();
            com.reveth.spctils.Spctils.LOGGER.info("[SPCTils] Daily quest progress has been reset for the new day (WIB).");
            if (MinecraftClient.getInstance().player != null) {
                MinecraftClient.getInstance().player.sendMessage(Text.literal("§e[SPCTils] Daily Quest progress has been reset for the new day! (Quest list kept)"), false);
            }
        }
    }

    private static Quest getPreviousQuest(String targetName, String itemName) {
        String lowerTarget = targetName.toLowerCase();
        String lowerItem = itemName.toLowerCase();
        
        for (Quest q : DailyQuestState.getActiveQuests()) {
            String qTarget = q.getTargetName().toLowerCase();
            if (qTarget.equals(lowerTarget) || lowerItem.contains(qTarget) || qTarget.contains(lowerTarget)) {
                return q;
            }
        }
        return null;
    }

    private static boolean parseQuestScreen(GenericContainerScreen screen) {
        List<Quest> foundQuests = new ArrayList<>();
        
        // Loop through the inventory slots
        for (int i = 0; i < screen.getScreenHandler().getStacks().size(); i++) {
            ItemStack stack = screen.getScreenHandler().getStacks().get(i);
            if (stack == null || stack.isEmpty()) continue;

            int currentProgress = 0;
            int maxProgress = 0;
            boolean parsedLore = false;
            String targetName = "";
            boolean isAlreadyCompleted = false;

            net.minecraft.component.type.LoreComponent loreComponent = stack.get(net.minecraft.component.DataComponentTypes.LORE);
            
            if (loreComponent != null) {
                List<Text> loreLines = loreComponent.lines();
                for (Text line : loreLines) {
                    String lineStr = line.getString();

                    if (lineStr.contains("QUEST SUDAH DIKERJAKAN")) {
                        isAlreadyCompleted = true;
                        parsedLore = true;
                    }

                    Matcher progressMatcher = PROGRESS_PATTERN.matcher(lineStr);
                    if (progressMatcher.find()) {
                        try {
                            currentProgress = Integer.parseInt(progressMatcher.group(1));
                            maxProgress = Integer.parseInt(progressMatcher.group(2));
                            parsedLore = true;
                            if (progressMatcher.group(3) != null) {
                                targetName = progressMatcher.group(3).trim();
                            }
                            break; 
                        } catch (NumberFormatException ignored) {}
                    }

                    Matcher mamaratMatcher = MAMARAT_PATTERN.matcher(lineStr);
                    if (mamaratMatcher.find()) {
                        try {
                            currentProgress = Integer.parseInt(mamaratMatcher.group(2));
                            maxProgress = Integer.parseInt(mamaratMatcher.group(3));
                            parsedLore = true;
                            targetName = mamaratMatcher.group(1).trim();
                            break;
                        } catch (NumberFormatException ignored) {}
                    }

                    Matcher anomalyMatcher = ANOMALY_PATTERN.matcher(lineStr);
                    if (anomalyMatcher.find()) {
                        try {
                            currentProgress = Integer.parseInt(anomalyMatcher.group(2));
                            maxProgress = Integer.parseInt(anomalyMatcher.group(3));
                            parsedLore = true;
                            targetName = anomalyMatcher.group(1).trim();
                            break;
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }

            // If it has a Progress lore, it MUST be a quest item!
            if (parsedLore) {
                String itemName = stack.getName().getString();
                String extractedName = null;

                // DEBUG: dump all lore lines for this item (gated by debugEntityEnabled)
                if (com.reveth.spctils.config.SpctilsConfig.get().debugEntityEnabled
                        && MinecraftClient.getInstance().player != null) {
                    MinecraftClient.getInstance().player.sendMessage(
                        net.minecraft.text.Text.literal("\u00a7e[LORE] Item: " + itemName), false);
                    if (loreComponent != null) {
                        for (Text line : loreComponent.lines()) {
                            MinecraftClient.getInstance().player.sendMessage(
                                net.minecraft.text.Text.literal("\u00a78  | " + line.getString()), false);
                        }
                    }
                }

                // If targetName wasn't found from special lore rules like Mamarat, we extract from itemName
                if (targetName.isEmpty()) {
                    Matcher nameMatcher = Pattern.compile("(?i).*?\\d+x\\s+(.+)").matcher(itemName);
                    if (nameMatcher.find()) {
                        extractedName = nameMatcher.group(1).trim();
                    }
                }
                
                // Validate against QuestRegistry to get the OFFICIAL registered name
                String searchName = extractedName != null ? extractedName : targetName;
                QuestDefinition def = QuestRegistry.findQuestByGuiItemName(itemName, searchName);
                if (def != null) {
                    targetName = def.loreName; // Use official name from registry
                } else if (targetName.isEmpty()) {
                    // Fallback if not registered at all
                    targetName = extractedName != null ? extractedName : itemName;
                }

                if (isAlreadyCompleted) {
                    Quest previousQuest = getPreviousQuest(targetName, itemName);
                    if (previousQuest != null) {
                        targetName = previousQuest.getTargetName();
                        maxProgress = previousQuest.getMaxProgress();
                        currentProgress = maxProgress; // Set completed
                    } else {
                        maxProgress = 1;
                        currentProgress = 1; // Fallback if we don't know the exact max progress
                    }
                }

                Quest quest = new Quest(stack.copy(), targetName, currentProgress, maxProgress);
                foundQuests.add(quest);
            }
        }
        
        // Only override if we actually found something.
        // If we found 0, it means the GUI is still loading from the server (or empty).
        if (!foundQuests.isEmpty()) {
            DailyQuestState.clearQuests();
            for (Quest q : foundQuests) {
                DailyQuestState.addQuest(q);
            }
            MinecraftClient.getInstance().player.sendMessage(Text.literal("§a[SPCTils] Successfully loaded " + foundQuests.size() + " quests!"), false);
            for (Quest q : foundQuests) {
                MinecraftClient.getInstance().player.sendMessage(Text.literal("§7- Tracking: " + q.getTargetName()), false);
            }
            return true;
        }
        return false;
    }


}
