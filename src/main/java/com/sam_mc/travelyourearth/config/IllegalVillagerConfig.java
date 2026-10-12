package com.sam_mc.travelyourearth.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Settings of the illegal villagers (Beta 1.2).
 * File: config/travelyourearth-illegal.toml (created the first time you open the game).
 * Edit it with the game closed, or save it while the game is open: NeoForge reloads it.
 */
public class IllegalVillagerConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    /** 1 Minecraft day = 24000 ticks. 3 days = 72000 ticks. */
    public static final ModConfigSpec.IntValue RESTOCK_TICKS = BUILDER
            .comment("Ticks an illegal villager needs to restock its illegal trades.",
                    "1 day = 24000 ticks. Default: 72000 (3 days). For testing try 200 (10 seconds).")
            .defineInRange("illegalRestockTicks", 72000, 20, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue SCARED_TICKS = BUILDER
            .comment("Ticks an illegal villager runs away from villagers and iron golems after being seen trading.",
                    "Default: 1200 (1 minute).")
            .defineInRange("illegalScaredTicks", 1200, 20, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue WANTED_TICKS = BUILDER
            .comment("Ticks an illegal villager is WANTED (glowing, running away) after someone rings the bell on it",
                    "Catch it with a boat before the time runs out! Default: 6000 (5 minutes).")
            .defineInRange("illegalWantedTicks", 6000, 20, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue AVOID_TICKS = BUILDER
            .comment("Ticks the OTHER illegal villagers avoid a player after they snitch on one with a bell.",
                    "(The snitched villager itself avoids that player forever.) Default: 6000 (5 minutes).")
            .defineInRange("illegalAvoidTicks", 6000, 20, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue NIGHT_EVERY = BUILDER
            .comment("Traffickers go out 1 night of every N days (all on the same night, so they meet).",
                    "Default: 3. For testing use 1 (every night).")
            .defineInRange("illegalNightEvery", 3, 1, 100);

    // Ticks the other villagers don't talk to an illegal villager, depending on what happened
    public static final ModConfigSpec.IntValue SHUN_SEEN_TICKS = BUILDER
            .comment("Ticks the other villagers don't talk to an illegal villager after it's SEEN trading.",
                    "Default: 24000 (1 day).")
            .defineInRange("illegalShunSeenTicks", 24000, 20, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue SHUN_WANTED_TICKS = BUILDER
            .comment("Ticks the other villagers don't talk to an illegal villager after it's SNITCHED on with a bell.",
                    "Default: 72000 (3 days).")
            .defineInRange("illegalShunWantedTicks", 72000, 20, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue SHUN_CAUGHT_TICKS = BUILDER
            .comment("Ticks the other villagers don't talk to an illegal villager after it's CAUGHT in a boat.",
                    "Default: 168000 (7 days).")
            .defineInRange("illegalShunCaughtTicks", 168000, 20, Integer.MAX_VALUE);

    public static final ModConfigSpec SPEC = BUILDER.build();
}