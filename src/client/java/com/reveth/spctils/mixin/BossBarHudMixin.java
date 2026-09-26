package com.reveth.spctils.mixin;

import com.reveth.spctils.config.SpctilsConfig;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collectors;

@Mixin(BossBarHud.class)
public class BossBarHudMixin {
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Ljava/util/Collection;iterator()Ljava/util/Iterator;"))
    private Iterator<ClientBossBar> filterBossBars(Collection<ClientBossBar> collection) {
        // Filter out the OBJEKTIF boss bar so it doesn't render natively
        // Strip § formatting codes before checking since server may send colored text
        return collection.stream()
                .filter(bar -> {
                    String plainName = bar.getName().getString().replaceAll("§.", "");
                    return !plainName.contains("OBJEKTIF");
                })
                .collect(Collectors.toList())
                .iterator();
    }
}
