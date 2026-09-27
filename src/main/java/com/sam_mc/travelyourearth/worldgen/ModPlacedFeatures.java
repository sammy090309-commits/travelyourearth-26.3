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

public class ModPlacedFeatures {
    // Cuántas vetas se intentan poner por chunk y a qué altura
    public static final ResourceKey<PlacedFeature> RUBY_ORE_PLACED_KEY = registerKey("ruby_ore_placed");
    public static final ResourceKey<PlacedFeature> MOUNTAIN_RUBY_ORE_PLACED_KEY = registerKey("mountain_ruby_ore_placed");
    public static final ResourceKey<PlacedFeature> DAPPLED_RUBY_ORE_PLACED_KEY = registerKey("dappled_ruby_ore_placed");

    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        var configuredFeatures = context.lookup(Registries.FEATURE);

        // 1. TODOS LOS BIOMAS: Y 5 hacia abajo, triangular.
        // El triángulo va de -133 a 5 → pico en Y -64 (el fondo), igual que el diamante:
        // más rubí cuanto más abajo y nada por encima de Y 5.
        // 24 intentos (≈ la mitad caen dentro del mundo) → ≈ 24 menas/chunk,
        // un poco más raro que el diamante (≈ 31).
        register(context, RUBY_ORE_PLACED_KEY,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.RUBY_ORE_KEY),
                OrePlacements.commonOrePlacement(8,
                        HeightRangePlacement.triangle(
                                VerticalAnchor.absolute(-64),
                                VerticalAnchor.absolute(5))));

        // 2. MONTAÑAS: Y 60 hacia abajo, triangular (pico en Y -2).
        // 9 vetas pequeñas → ≈ 18 menas/chunk, ~1.5 veces la esmeralda de montaña (≈ 12).
        register(context, MOUNTAIN_RUBY_ORE_PLACED_KEY,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.MOUNTAIN_RUBY_ORE_KEY),
                OrePlacements.commonOrePlacement(12,
                        HeightRangePlacement.triangle(
                                VerticalAnchor.absolute(-64),
                                VerticalAnchor.absolute(60))));

        // 3. DAPPLED FOREST: Y 90 hacia abajo, uniforme (igual de probable a cualquier altura).
        // 25 intentos → ≈ 125 menas/chunk, mucho más común que el oro extra de las Badlands (≈ 47).
        register(context, DAPPLED_RUBY_ORE_PLACED_KEY,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.DAPPLED_RUBY_ORE_KEY),
                OrePlacements.commonOrePlacement(35,
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
