package com.reveth.spctils.mixin.client.dailyquest;

import com.reveth.spctils.config.SpctilsConfig;
import com.reveth.spctils.features.dailyquest.DailyQuestManager;
import com.reveth.spctils.features.damage.DamageTrackerHud;
import com.reveth.spctils.features.dailyquest.DailyQuestState;
import com.reveth.spctils.features.dailyquest.Quest;
import com.reveth.spctils.features.dailyquest.QuestDefinition;
import com.reveth.spctils.features.dailyquest.QuestRegistry;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    // Matches: [Level 15] Forest Golem: 180.27 (-2,350)
    private static final Pattern ACTIONBAR_DAMAGE_PATTERN = Pattern.compile("(?i)\\[Level \\d+\\] (.+?):\\s*([0-9,.]+)\\s*\\(-([0-9,.]+)\\)");
    
    // Matches: 200/200 ✦ | 790/790 ❤ | 20/20 ⚡
    private static final Pattern ACTIONBAR_RPG_PATTERN = Pattern.compile("([0-9,.]+)\\s*/\\s*([0-9,.]+)\\s*✦\\s*\\|\\s*([0-9,.]+)\\s*/\\s*([0-9,.]+)\\s*❤\\s*\\|\\s*([0-9,.]+)\\s*/\\s*([0-9,.]+)\\s*⚡");

    @Inject(method = "setOverlayMessage", at = @At("HEAD"), cancellable = true)
    private void onSetOverlayMessage(Text message, boolean tinted, CallbackInfo ci) {
        String plainText = message.getString().replaceAll("§.", "").trim();
        
        Matcher rpgMatcher = ACTIONBAR_RPG_PATTERN.matcher(plainText);
        if (rpgMatcher.find()) {
            try {
                com.reveth.spctils.features.rpghud.RpgHudState.mana = Double.parseDouble(rpgMatcher.group(1).replace(",", ""));
                com.reveth.spctils.features.rpghud.RpgHudState.maxMana = Double.parseDouble(rpgMatcher.group(2).replace(",", ""));
                com.reveth.spctils.features.rpghud.RpgHudState.health = Double.parseDouble(rpgMatcher.group(3).replace(",", ""));
                com.reveth.spctils.features.rpghud.RpgHudState.maxHealth = Double.parseDouble(rpgMatcher.group(4).replace(",", ""));
                com.reveth.spctils.features.rpghud.RpgHudState.energy = Double.parseDouble(rpgMatcher.group(5).replace(",", ""));
                com.reveth.spctils.features.rpghud.RpgHudState.maxEnergy = Double.parseDouble(rpgMatcher.group(6).replace(",", ""));
                
                if (SpctilsConfig.get().enableExperimentalRpgHud) {
                    ci.cancel(); // Hide the vanilla action bar if our RPG hud is rendering it
                    return;
                }
            } catch (NumberFormatException ignored) {}
        }
        
        Matcher m = ACTIONBAR_DAMAGE_PATTERN.matcher(plainText);
        
        if (m.find()) {
            String targetName = m.group(1).trim();
            try {
                double hp = Double.parseDouble(m.group(2).replace(",", ""));
                double dmg = Double.parseDouble(m.group(3).replace(",", ""));
                
                // DEBUG: Entity hit
                if (SpctilsConfig.get().debugEntityEnabled && net.minecraft.client.MinecraftClient.getInstance().player != null) {
                    net.minecraft.client.MinecraftClient.getInstance().player.sendMessage(Text.literal("§e[DEBUG] Hit " + targetName + " HP: " + hp + " DMG: " + dmg), false);
                }
                
                // 1. Update Damage Tracker
                DamageTrackerHud.addDamage(dmg);
                
                // 2. Check if killed for Daily Quests
                if (hp - dmg <= 0) {
                    // DEBUG: Entity kill
                    if (SpctilsConfig.get().debugEntityEnabled && net.minecraft.client.MinecraftClient.getInstance().player != null) {
                        net.minecraft.client.MinecraftClient.getInstance().player.sendMessage(Text.literal("§c[DEBUG] Killed " + targetName), false);
                    }
                    
                    String entityNameLower = targetName.toLowerCase();
                    for (Quest quest : DailyQuestState.getAllTrackedQuests()) {
                        if (quest.isCompleted()) continue;
                        QuestDefinition def = QuestRegistry.getQuest(quest.getTargetName());
                        if (def == null) continue;
                        
                        if (def.trigger == QuestDefinition.Trigger.KILL) {
                            if (entityNameLower.contains(def.triggerTarget.toLowerCase())) {
                                // Prevent mega cross-talk
                                if (entityNameLower.contains("mega") != def.triggerTarget.toLowerCase().contains("mega")) {
                                    continue;
                                }
                                quest.incrementProgress();
                                SpctilsConfig.save();
                                com.reveth.spctils.Spctils.LOGGER.info("[SPCTils] KILLED {} -> Incremented quest {} to {}", targetName, quest.getTargetName(), quest.getCurrentProgress());
                                
                                // DEBUG: Quest Incremented Message
                                if (SpctilsConfig.get().debugEntityEnabled && net.minecraft.client.MinecraftClient.getInstance().player != null) {
                                    net.minecraft.client.MinecraftClient.getInstance().player.sendMessage(
                                        Text.literal("§a[DEBUG] Daily Quest " + targetName + " + 1"), false
                                    );
                                }
                            }
                        }
                    }
                }
            } catch (NumberFormatException ignored) {}
        }
    }
}
