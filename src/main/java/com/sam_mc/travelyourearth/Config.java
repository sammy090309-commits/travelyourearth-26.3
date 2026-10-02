package com.sam_mc.travelyourearth;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Mod configuration.
 *
 * CLIENT -> config/travelyourearth-client.toml  (only affects your screen)
 * LOCAL  -> game data: used by the server, including the internal "server" of a singleplayer world
 *           (it was called COMMON before NeoForge 26.3.0.37-beta; the variable names keep "COMMON")
 */
public class Config {

    // =========================================================================
    // CLIENT
    // =========================================================================
    private static final ModConfigSpec.Builder CLIENT_BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue SHOW_CREATIVE_TAB = CLIENT_BUILDER
            .comment("Shows the \"Travel Your Earth\" Mod Tab with every item from the mod.",
                    "The items still appear in the vanilla tabs too.",
                    "If you change it with a world open, leave and join again to see it.")
            .translation("travelyourearth.configuration.showCreativeTab")
            .define("showCreativeTab", true);

    /** Client config (it's called SPEC so the code that already uses it doesn't break). */
    public static final ModConfigSpec SPEC = CLIENT_BUILDER.build();

    // =========================================================================
    // LOCAL (formerly COMMON)
    // =========================================================================
    private static final ModConfigSpec.Builder COMMON_BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue MOD_ADVANCEMENT_TAB = COMMON_BUILDER
            .comment("Moves the mod's advancements from the vanilla \"Adventure\" tab to their own Mod Tab.",
                    "The tab appears after getting your first Ruby.",
                    "If you change it with a world open, leave and join again (or use /reload).")
            .translation("travelyourearth.configuration.modAdvancementTab")
            .define("modAdvancementTab", false);

    public static final ModConfigSpec COMMON_SPEC = COMMON_BUILDER.build();
}