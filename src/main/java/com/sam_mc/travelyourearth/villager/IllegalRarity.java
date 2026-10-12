package com.sam_mc.travelyourearth.villager;

/**
 * Rarity of an illegal trade: decides how many XP LEVELS it costs
 * and how many times it can be bought before the villager restocks.
 */
public enum IllegalRarity {
    COMMON(2, 3),   // Común
    RARE(4, 2),     // Raro
    STRANGE(6, 1);  // Extraño

    private final int levelCost;
    private final int defaultMaxUses;

    IllegalRarity(int levelCost, int defaultMaxUses) {
        this.levelCost = levelCost;
        this.defaultMaxUses = defaultMaxUses;
    }

    /** XP levels taken from the player on each trade. */
    public int levelCost() {
        return this.levelCost;
    }

    /** Uses before restocking, if the trade doesn't say otherwise. */
    public int defaultMaxUses() {
        return this.defaultMaxUses;
    }
}