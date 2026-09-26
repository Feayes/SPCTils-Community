package com.reveth.spctils.features.dailyquest;

import java.util.HashMap;
import java.util.Map;

public class QuestRegistry {
    public static final Map<String, QuestDefinition> QUESTS = new HashMap<>();

    static {
        // Dojo
        register("Pemburu Slime Rawa", QuestDefinition.Type.DAILY, QuestDefinition.Trigger.KILL, "Slime Abnormal");
        register("Pembersihan Inti Magma", QuestDefinition.Type.DAILY, QuestDefinition.Trigger.KILL, "Magma Abnormal");
        register("Penakluk Tirani Slime", QuestDefinition.Type.DAILY, QuestDefinition.Trigger.KILL, "Mega Slime");
        register("Bencana Katastrofe Kawah", QuestDefinition.Type.DAILY, QuestDefinition.Trigger.KILL, "Mega Magma");
        register("Hama Ladang Dojo", QuestDefinition.Type.DAILY, QuestDefinition.Trigger.KILL, "Farm Rat");

        // Mamarat & Anomaly (Raid)
        register("Mama Rat", QuestDefinition.Type.RAID, QuestDefinition.Trigger.CHAT_RAID, "[daily quest] progress daily mama rat bertambah!:"); 
        register("Destroy The Anomaly", QuestDefinition.Type.RAID, QuestDefinition.Trigger.CHAT_RAID, "[daily quest] progress daily anomaly hunter:"); 

        // Guild (Membunuh)
        register("Forest Skeleton", QuestDefinition.Type.DAILY, QuestDefinition.Trigger.KILL, "Forest Skeleton");
        register("Bandit Leader", QuestDefinition.Type.DAILY, QuestDefinition.Trigger.KILL, "Bandit Leader");
        register("Forest Golem", QuestDefinition.Type.DAILY, QuestDefinition.Trigger.KILL, "Forest Golem");

        // Guild (Chat Reward)
        register("Nern, The Corrupted Witch", QuestDefinition.Type.DAILY, QuestDefinition.Trigger.CHAT_REWARD, "[loot] kamu berhasil mengklaim reward resin nern");
        register("Groot, The Forest Keeper", QuestDefinition.Type.DAILY, QuestDefinition.Trigger.CHAT_REWARD, "[loot] kamu berhasil mengklaim reward resin groot");
    }

    public static void register(String name, QuestDefinition.Type type, QuestDefinition.Trigger trigger, String target) {
        QUESTS.put(name.toLowerCase(), new QuestDefinition(name, type, trigger, target));
    }
    
    public static QuestDefinition getQuest(String name) {
        if (name == null) return null;
        return QUESTS.get(name.toLowerCase());
    }

    public static QuestDefinition findQuestByGuiItemName(String itemName, String extractedName) {
        // 1. Try exact match on item name
        QuestDefinition def = getQuest(itemName);
        if (def != null) return def;

        // 2. Try exact match on extracted name (e.g. after stripping "Basmi 100x")
        if (extractedName != null && !extractedName.isEmpty()) {
            def = getQuest(extractedName);
            if (def != null) return def;
        }

        // 3. Fallback: partial match against registered quest names
        String lowerItem = itemName.toLowerCase();
        for (QuestDefinition q : QUESTS.values()) {
            if (lowerItem.contains(q.loreName.toLowerCase())) {
                return q;
            }
        }

        // 4. Fallback: match against trigger target
        for (QuestDefinition q : QUESTS.values()) {
            if (q.triggerTarget != null) {
                String targetLower = q.triggerTarget.toLowerCase();
                if (lowerItem.contains(targetLower) || (extractedName != null && extractedName.toLowerCase().contains(targetLower))) {
                    return q;
                }
            }
        }
        return null;
    }
}
