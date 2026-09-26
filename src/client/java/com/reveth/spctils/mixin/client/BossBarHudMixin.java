package com.reveth.spctils.mixin.client;

import com.reveth.spctils.config.SpctilsConfig;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Mixin(BossBarHud.class)
public class BossBarHudMixin {

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Ljava/util/Map;values()Ljava/util/Collection;"))
    private Collection<ClientBossBar> redirectBossBars(Map<UUID, ClientBossBar> map) {
        if (SpctilsConfig.get().boosterDisplayMode == SpctilsConfig.BoosterDisplayMode.ORIGINAL) {
            return map.values();
        }
        
        // Filter out boosters if mode is CUSTOM or HIDDEN
        return map.values().stream().filter(bar -> {
            String name = bar.getName().getString();
            return !name.contains("[Weekend Booster]") && !name.contains("[Weekday Booster]");
        }).collect(Collectors.toList());
    }
}
