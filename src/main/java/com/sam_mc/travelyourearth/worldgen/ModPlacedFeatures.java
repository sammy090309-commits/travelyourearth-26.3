package com.sam_mc.travelyourearth.worldgen;

import com.sam_mc.travelyourearth.TravelYourEarth;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.OrePlacements;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

import java.util.List;

/**
 * Cuántas vetas de rubí se intentan poner por chunk y a qué altura.
 *
 * Referencia vanilla (wiki oficial, Java):
 *   - Diamante: 4 tandas (7 + 1/9 + 4 + 2 vetas por chunk) debajo de Y 16  → ≈ 26 menas/chunk
 *   - Hierro: 90 + 10 + 10 vetas por chunk                                 → muchísimo más
 * Los números "≈ menas/chunk" son aproximados (cuentan lo que cae dentro del mundo y bajo tierra).
 */
public class ModPlacedFeatures {
    public static final ResourceKey<PlacedFeature> RUBY_ORE_PLACED_KEY = registerKey("ruby_ore_placed");
    public static final ResourceKey<PlacedFeature> MOUNTAIN_RUBY_ORE_PLACED_KEY = registerKey("mountain_ruby_ore_placed");
    public static final ResourceKey<PlacedFeature> DAPPLED_RUBY_ORE_PLACED_KEY = registerKey("dappled_ruby_ore_placed");

    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        var configuredFeatures = context.lookup(Registries.FEATURE);

        // 1. TODOS LOS BIOMAS: MUCHO más raro que el diamante.
        // Misma forma que la veta principal del diamante: triángulo de Y -144 a Y 16
        // (pico en Y -64, el fondo; nada por encima de Y 16), pero solo 4 vetas en vez de 7
        // y sin las otras 3 tandas del diamante → ≈ 4 menas/chunk (el diamante ≈ 26).
        register(context, RUBY_ORE_PLACED_KEY,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.RUBY_ORE_KEY),
                OrePlacements.commonOrePlacement(4,
                        HeightRangePlacement.triangle(
                                VerticalAnchor.absolute(-144),
                                VerticalAnchor.absolute(16))));

        // 2. MONTAÑAS: LA MISMA probabilidad que el diamante.
        // Se suma a la de "todos los biomas": 14 vetas pequeñas de Y 60 hacia abajo (pico en Y -2)
        // ≈ 25 menas + ≈ 4 de la general ≈ 29 menas/chunk ≈ lo mismo que el diamante (≈ 26).
        register(context, MOUNTAIN_RUBY_ORE_PLACED_KEY,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.MOUNTAIN_RUBY_ORE_KEY),
                OrePlacements.commonOrePlacement(14,
                        HeightRangePlacement.triangle(
                                VerticalAnchor.absolute(-64),
                                VerticalAnchor.absolute(60))));

        // 3. BOSQUE MOTEADO: uniforme de Y 12 a Y 256 (igual de probable a cualquier altura).
        // Un poco MÁS común que el diamante pero MÁS raro que el hierro:
        // 20 intentos (≈ 1/3 caen bajo tierra) → ≈ 36 menas + ≈ 4 de la general ≈ 40 menas/chunk.
        register(context, DAPPLED_RUBY_ORE_PLACED_KEY,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.DAPPLED_RUBY_ORE_KEY),
                OrePlacements.commonOrePlacement(20,
                        HeightRangePlacement.uniform(
                                VerticalAnchor.absolute(12),
                                VerticalAnchor.absolute(256))));
    }

    private static ResourceKey<PlacedFeature> registerKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(TravelYourEarth.MODID, name));
    }

    private static void register(BootstrapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key,
                                 Holder<Feature> configuration, List<PlacementModifier> modifiers) {
        context.register(key, new PlacedFeature(configuration, List.copyOf(modifiers)));
    }
}