package com.reveth.spctils.features.dailyquest;

import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class Quest {
    private final String itemId;
    private final String targetName;
    private int currentProgress;
    private final int maxProgress;

    private transient ItemStack iconCache;

    public Quest(ItemStack icon, String targetName, int currentProgress, int maxProgress) {
        this.itemId = Registries.ITEM.getId(icon.getItem()).toString();
        this.iconCache = icon;
        this.targetName = targetName;
        this.currentProgress = currentProgress;
        this.maxProgress = maxProgress;
    }

    public ItemStack getIcon() {
        if (iconCache == null) {
            iconCache = Registries.ITEM.get(Identifier.of(itemId)).getDefaultStack();
        }
        return iconCache;
    }

    public String getTargetName() {
        return targetName;
    }

    public int getCurrentProgress() {
        return currentProgress;
    }

    public void incrementProgress() {
        if (this.currentProgress < this.maxProgress) {
            this.currentProgress++;
        }
    }

    public void resetProgress() {
        this.currentProgress = 0;
    }

    public int getMaxProgress() {
        return maxProgress;
    }

    public boolean isCompleted() {
        return currentProgress >= maxProgress;
    }
}
