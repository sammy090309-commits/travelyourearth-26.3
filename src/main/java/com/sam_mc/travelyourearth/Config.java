package com.sam_mc.travelyourearth;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Mod configuration.
 *
 * CLIENT -> config/travelyourearth-client.toml  (only affects your screen)
 * LOCAL  -> game data: used by the server, including the internal "server" of a singleplayer world
 *           (it was called COMMON before NeoForge 26.3.0.37-beta; the variable names keep "COMMON")
 *
 * The CLIENT options are shown in our own screen (Config -> Client), see TravelYourEarthOptionsScreen.
 */
public class Config {

    // =========================================================================
    // CLIENT
    // =========================================================================
    private static final ModConfigSpec.Builder CLIENT_BUILDER = new ModConfigSpec.Builder();

    // ---- Interface ----

    public static final ModConfigSpec.BooleanValue SHOW_CREATIVE_TAB = CLIENT_BUILDER
            .comment("Shows the \"Travel Your Earth\" Mod Tab with every item from the mod (off by default).",
                    "The items still appear in the vanilla tabs too.",
                    "If you change it with a world open, leave and join again to see it.")
            .translation("travelyourearth.configuration.showCreativeTab")
            .define("showCreativeTab", false);

    public static final ModConfigSpec.BooleanValue MENU_EFFECTS = CLIENT_BUILDER
            .comment("Animations of the mod's config menu: the floating logo, the beating splash text",
                    "and the surprise when you click \"Made with <3\". Turn it off for a still menu.")
            .translation("travelyourearth.configuration.menuEffects")
            .define("menuEffects", true);

    // ---- Inventories ----

    public static final ModConfigSpec.BooleanValue ENHANCED_BEACON = CLIENT_BUILDER
            .comment("Improved beacon screen: the materials panel (book button + search box)",
                    "and the payment items rotating at the bottom. Off = vanilla beacon screen.")
            .translation("travelyourearth.configuration.enhancedBeacon")
            .define("enhancedBeacon", true);

    public static final ModConfigSpec.BooleanValue DARK_ILLEGAL_TRADES = CLIENT_BUILDER
            .comment("Illegal trades (and the smuggler's ruby trades) are drawn with a dark button.",
                    "Off = they look like normal trades.")
            .translation("travelyourearth.configuration.darkIllegalTrades")
            .define("darkIllegalTrades", true);

    public static final ModConfigSpec.BooleanValue ILLEGAL_RAID_WARNING = CLIENT_BUILDER
            .comment("Shows \"Illegal deal: it can start a raid\" when you hover an illegal trade.",
                    "The \"Level Requirement\" line is always shown when you don't have enough XP.")
            .translation("travelyourearth.configuration.illegalRaidWarning")
            .define("illegalRaidWarning", true);

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