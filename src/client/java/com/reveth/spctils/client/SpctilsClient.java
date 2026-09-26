package com.reveth.spctils.client;

import com.reveth.spctils.Spctils;
import com.reveth.spctils.config.SpctilsConfig;
import com.reveth.spctils.gui.SpctilsSettingsScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class SpctilsClient implements ClientModInitializer {
    private static KeyBinding settingsKeyBinding;
    private static KeyBinding toggleCookingKeyBinding;

    @Override
    public void onInitializeClient() {
        Spctils.LOGGER.info("[SPCTils] Client initialized.");

        // Load config
        SpctilsConfig.load();

        // Register keybinding
        settingsKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.spctils.settings",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_EQUAL, // Default to '='
                KeyBinding.Category.create(net.minecraft.util.Identifier.of("spctils", "utilities"))
        ));

        toggleCookingKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.spctils.cooking.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_MINUS,
                KeyBinding.Category.create(net.minecraft.util.Identifier.of("spctils", "cooking"))
        ));

        com.reveth.spctils.features.objective.ObjectiveHud.register();
        com.reveth.spctils.features.damage.DamageTrackerHud.register();
        com.reveth.spctils.features.booster.BoosterHud.register();
        com.reveth.spctils.features.rpghud.CustomRpgHudRenderer.register();

        // Listen for key press
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (settingsKeyBinding.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new SpctilsSettingsScreen(null));
                }
            }
            while (toggleCookingKeyBinding.wasPressed()) {
                com.reveth.spctils.features.cooking.BotConfig.enabled = !com.reveth.spctils.features.cooking.BotConfig.enabled;
                if (client.player != null) {
                    if (com.reveth.spctils.features.cooking.BotConfig.enabled) {
                        client.player.sendMessage(Text.literal("§e[AutoKoki] §aBot diaktifkan §7(" + com.reveth.spctils.features.cooking.BotConfig.mode.getDisplayName() + ")"), true);
                    } else {
                        client.player.sendMessage(Text.literal("§e[AutoKoki] §cBot dimatikan"), true);
                    }
                }
            }
        });

        // ── Register features ────────────────────────────────────────────────
        com.reveth.spctils.features.dailyquest.DailyQuestManager.register();
        com.reveth.spctils.features.dailyquest.DailyQuestHud.register();
        new com.reveth.spctils.features.cooking.CookingMod().register();
        new com.reveth.spctils.features.debug.PlayerDebugHud().register();
        // ────────────────────────────────────────────────────────────────────
    }
}
