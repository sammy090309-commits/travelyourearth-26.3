package com.sam_mc.travelyourearth;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Configuración del mod.
 *
 * CLIENT -> config/travelyourearth-client.toml  (solo afecta a tu pantalla)
 * COMMON -> config/travelyourearth-common.toml  (datos del juego: la usa el servidor,
 *                                                también el "servidor" interno de un mundo de un jugador)
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

    /** Config del cliente (se llama SPEC para no romper el código que ya lo usa). */
    public static final ModConfigSpec SPEC = CLIENT_BUILDER.build();

    // =========================================================================
    // COMMON
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