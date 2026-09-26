package com.reveth.spctils.features.debug;

import java.util.HashMap;
import java.util.Map;

/**
 * Pre-built lookup table for common Minecraft network packet class names.
 * Translates intermediary names (class_XXXX) to human-readable Yarn names.
 * These are the packets seen in a typical play session on a Paper/Spigot server.
 */
public class PacketMappings {

    // Map: intermediary simple name -> readable name
    private static final Map<String, String> CLASS_MAP = new HashMap<>();

    static {
        // === S2C Play Packets ===
        CLASS_MAP.put("class_2604", "EntitySpawnS2CPacket");
        CLASS_MAP.put("class_2606", "ExperienceOrbSpawnS2CPacket");
        CLASS_MAP.put("class_2609", "AnimationS2CPacket");
        CLASS_MAP.put("class_2617", "BlockUpdateS2CPacket");
        CLASS_MAP.put("class_2620", "BossBarS2CPacket");
        CLASS_MAP.put("class_2629", "BossBarS2CPacket");
        CLASS_MAP.put("class_2631", "DifficultyS2CPacket");
        CLASS_MAP.put("class_2641", "CommandTreeS2CPacket");
        CLASS_MAP.put("class_2645", "CloseScreenS2CPacket");
        CLASS_MAP.put("class_2647", "InventoryS2CPacket");
        CLASS_MAP.put("class_2649", "ScreenHandlerPropertyUpdateS2CPacket");
        CLASS_MAP.put("class_2653", "ScreenHandlerSlotUpdateS2CPacket");
        CLASS_MAP.put("class_2655", "CooldownUpdateS2CPacket");
        CLASS_MAP.put("class_2657", "ChatSuggestionsS2CPacket");
        CLASS_MAP.put("class_2659", "CustomPayloadS2CPacket");
        CLASS_MAP.put("class_2661", "RemoveMessageS2CPacket");
        CLASS_MAP.put("class_2663", "EntityStatusS2CPacket");
        CLASS_MAP.put("class_2665", "ExplosionS2CPacket");
        CLASS_MAP.put("class_2667", "UnloadChunkS2CPacket");
        CLASS_MAP.put("class_2669", "GameStateChangeS2CPacket");
        CLASS_MAP.put("class_2671", "OpenHorseScreenS2CPacket");
        CLASS_MAP.put("class_2673", "DamageTiltS2CPacket");
        CLASS_MAP.put("class_2675", "ParticleS2CPacket");
        CLASS_MAP.put("class_2677", "StartChunkSendS2CPacket");
        CLASS_MAP.put("class_2679", "ChunkDataS2CPacket");
        CLASS_MAP.put("class_2681", "WorldEventS2CPacket");
        CLASS_MAP.put("class_2683", "ChunkDeltaUpdateS2CPacket");
        CLASS_MAP.put("class_2684", "EntityS2CPacket");
        CLASS_MAP.put("class_2685", "EntityS2CPacket$MoveRelative");
        CLASS_MAP.put("class_2686", "EntityS2CPacket$RotateAndMoveRelative");
        CLASS_MAP.put("class_2687", "EntityS2CPacket$Rotate");
        CLASS_MAP.put("class_2689", "GameJoinS2CPacket");
        CLASS_MAP.put("class_2696", "MapUpdateS2CPacket");
        CLASS_MAP.put("class_2700", "OpenScreenS2CPacket");
        CLASS_MAP.put("class_2703", "PlayerListS2CPacket");
        CLASS_MAP.put("class_2707", "LookAtS2CPacket");
        CLASS_MAP.put("class_2709", "PlayerPositionLookS2CPacket");
        CLASS_MAP.put("class_2711", "UnlockRecipesS2CPacket");
        CLASS_MAP.put("class_2714", "RemoveEntitiesS2CPacket");
        CLASS_MAP.put("class_2716", "EntitiesDestroyS2CPacket");
        CLASS_MAP.put("class_2718", "RemoveEntityStatusEffectS2CPacket");
        CLASS_MAP.put("class_2720", "ResourcePackSendS2CPacket");
        CLASS_MAP.put("class_2722", "PlayerRespawnS2CPacket");
        CLASS_MAP.put("class_2724", "EntitySetHeadYawS2CPacket");
        CLASS_MAP.put("class_2726", "EntitySetHeadYawS2CPacket");
        CLASS_MAP.put("class_2728", "SelectAdvancementTabS2CPacket");
        CLASS_MAP.put("class_2730", "ServerMetadataS2CPacket");
        CLASS_MAP.put("class_2732", "OverlayMessageS2CPacket");
        CLASS_MAP.put("class_2734", "WorldBorderCenterChangedS2CPacket");
        CLASS_MAP.put("class_2736", "WorldBorderInterpolateSizeS2CPacket");
        CLASS_MAP.put("class_2739", "EntityTrackerUpdateS2CPacket");
        CLASS_MAP.put("class_2741", "EntityAttributesS2CPacket");
        CLASS_MAP.put("class_2743", "EntityVelocityUpdateS2CPacket");
        CLASS_MAP.put("class_2744", "EntityEquipmentUpdateS2CPacket");
        CLASS_MAP.put("class_2746", "ExperienceBarUpdateS2CPacket");
        CLASS_MAP.put("class_2748", "ExperienceBarUpdateS2CPacket");
        CLASS_MAP.put("class_2749", "HealthUpdateS2CPacket");
        CLASS_MAP.put("class_2751", "ScoreboardObjectiveUpdateS2CPacket");
        CLASS_MAP.put("class_2753", "SetPassengersS2CPacket");
        CLASS_MAP.put("class_2755", "TeamS2CPacket");
        CLASS_MAP.put("class_2757", "ScoreboardScoreUpdateS2CPacket");
        CLASS_MAP.put("class_2759", "ScoreboardDisplayS2CPacket");
        CLASS_MAP.put("class_2761", "WorldTimeUpdateS2CPacket");
        CLASS_MAP.put("class_2763", "TitleS2CPacket");
        CLASS_MAP.put("class_2765", "PlaySoundFromEntityS2CPacket");
        CLASS_MAP.put("class_2767", "PlaySoundS2CPacket");
        CLASS_MAP.put("class_2769", "StopSoundS2CPacket");
        CLASS_MAP.put("class_2772", "PlayerListHeaderS2CPacket");
        CLASS_MAP.put("class_2773", "NbtQueryResponseS2CPacket");
        CLASS_MAP.put("class_2775", "ItemPickupAnimationS2CPacket");
        CLASS_MAP.put("class_2777", "EntityPositionS2CPacket");
        CLASS_MAP.put("class_2779", "VehicleMoveS2CPacket");
        CLASS_MAP.put("class_2781", "EntityAttributesS2CPacket");
        CLASS_MAP.put("class_2783", "EntityStatusEffectS2CPacket");
        CLASS_MAP.put("class_2785", "SynchronizeTagsS2CPacket");
        CLASS_MAP.put("class_2787", "TitleFadeS2CPacket");
        CLASS_MAP.put("class_2789", "SubtitleS2CPacket");
        CLASS_MAP.put("class_2791", "ActionBarS2CPacket");
        CLASS_MAP.put("class_2821", "SignEditorOpenS2CPacket");
        CLASS_MAP.put("class_2824", "PlayerInteractEntityC2SPacket"); // actually C2S
        CLASS_MAP.put("class_2828", "PlayerMoveC2SPacket");
        CLASS_MAP.put("class_2829", "PlayerMoveC2SPacket$PositionAndOnGround");
        CLASS_MAP.put("class_2830", "PlayerMoveC2SPacket$Full");
        CLASS_MAP.put("class_2831", "PlayerMoveC2SPacket$LookAndOnGround");
        CLASS_MAP.put("class_2848", "ClientCommandC2SPacket");
        CLASS_MAP.put("class_2851", "PlayerInputC2SPacket");
        CLASS_MAP.put("class_2868", "UpdateSelectedSlotC2SPacket");
        CLASS_MAP.put("class_2886", "PlayerInteractItemC2SPacket");
        CLASS_MAP.put("class_4463", "PlayerActionResponseS2CPacket");
        CLASS_MAP.put("class_5900", "TeamS2CPacket");
        CLASS_MAP.put("class_6373", "CommonPingS2CPacket");
        CLASS_MAP.put("class_6374", "CommonPongC2SPacket");
        CLASS_MAP.put("class_7439", "GameMessageS2CPacket");
        CLASS_MAP.put("class_8042", "BundleS2CPacket");
        CLASS_MAP.put("class_8143", "EntityDamageS2CPacket");
        CLASS_MAP.put("class_9836", "ClientTickEndC2SPacket");
        CLASS_MAP.put("class_10264", "EntityPositionSyncS2CPacket");
        CLASS_MAP.put("class_2604", "EntitySpawnS2CPacket");
        CLASS_MAP.put("class_2629", "BossBarS2CPacket");
        CLASS_MAP.put("class_2663", "EntityStatusS2CPacket");
    }

    /**
     * Translate an intermediary class name to the Yarn-mapped readable name.
     * Input can be "class_XXXX" or "net.minecraft.class_XXXX".
     */
    public static String translate(String rawClassName) {
        // Extract the simple class name part
        String simple = rawClassName;
        int lastDot = rawClassName.lastIndexOf('.');
        if (lastDot >= 0) {
            simple = rawClassName.substring(lastDot + 1);
        }
        // Handle inner classes: class_2684$class_2685 -> look up the inner class
        String mapped = CLASS_MAP.get(simple);
        if (mapped != null) return mapped;
        // If not found, return as-is
        return simple;
    }
}
