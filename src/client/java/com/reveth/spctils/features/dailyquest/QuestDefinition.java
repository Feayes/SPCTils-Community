package com.reveth.spctils.features.dailyquest;

public class QuestDefinition {
    public enum Type { DAILY, RAID }
    public enum Trigger { KILL, CHAT_REWARD, CHAT_RAID }

    public final String loreName;
    public final Type type;
    public final Trigger trigger;
    public final String triggerTarget;

    public QuestDefinition(String loreName, Type type, Trigger trigger, String triggerTarget) {
        this.loreName = loreName;
        this.type = type;
        this.trigger = trigger;
        this.triggerTarget = triggerTarget;
    }
}
