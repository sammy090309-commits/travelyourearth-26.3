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
 * How many ruby veins are attempted per chunk and at what height.
 * (Vein shape: ModConfiguredFeatures | biomes: ModBiomeModifiers)
 *
 * Vanilla reference (26.3 OrePlacements):
 *   - Diamond: 7 + 2 + 4 (+1 every 9 chunks) veins below Y 16, many below the world -> ~3-4 ores/chunk
 *   - Emerald (only mountains): 100 attempts, size 3, triangle Y -16..480, most in the air -> ~3 ores/chunk
 *   - Gold extra (badlands): 50 attempts, size 9, uniform Y 32..256
 *
 * Ruby per chunk (approx., 1.2): savanna ~13 > dappled forest ~9-10 > mountains ~5-6 > anywhere ~0.5
 */
public class ModPlacedFeatures {

    public static final ResourceKey<PlacedFeature> RUBY_ORE_PLACED_KEY = registerKey("ruby_ore_placed");
    public static final ResourceKey<PlacedFeature> MOUNTAIN_RUBY_ORE_PLACED_KEY = registerKey("mountain_ruby_ore_placed");
    public static final ResourceKey<PlacedFeature> DAPPLED_RUBY_ORE_PLACED_KEY = registerKey("dappled_ruby_ore_placed");
    public static final ResourceKey<PlacedFeature> SAVANNA_RUBY_ORE_PLACED_KEY = registerKey("savanna_ruby_ore_placed");

    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        var configuredFeatures = context.lookup(Registries.FEATURE);

        // 1. ALL BIOMES: MUCH rarer than diamond (~0.5 ores/chunk, diamond ~3-4).
        // Same shape as the diamond main vein: triangle from Y -144 to Y 16
        // (peak at Y -64, the bottom; half of it is below the world), but only
        // ONE attempt every 2 chunks.
        register(context, RUBY_ORE_PLACED_KEY,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.RUBY_ORE_KEY),
                OrePlacements.rareOrePlacement(2,
                        HeightRangePlacement.triangle(
                                VerticalAnchor.absolute(-144),
                                VerticalAnchor.absolute(16))));

        // 2. MOUNTAINS: like vanilla's emerald, but UPSIDE DOWN (deeper = more) and a bit more common.
        // 3 attempts, triangle from Y -192 to Y 64 (peak at Y -64, the bottom of the world):
        // half of them land below the world -> ~1.5 big veins, ~5-6 ores/chunk.
        register(context, MOUNTAIN_RUBY_ORE_PLACED_KEY,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.MOUNTAIN_RUBY_ORE_KEY),
                OrePlacements.commonOrePlacement(3,
                        HeightRangePlacement.triangle(
                                VerticalAnchor.absolute(-192),
                                VerticalAnchor.absolute(64))));

        // 3. DAPPLED FOREST: like vanilla's gold in the badlands (same height: uniform Y 32..256,
        // big veins of 9), but MUCH softer: 8 attempts instead of 50 (~9-10 ores/chunk).
        // Attempts that land in the air don't place anything.
        register(context, DAPPLED_RUBY_ORE_PLACED_KEY,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.DAPPLED_RUBY_ORE_KEY),
                OrePlacements.commonOrePlacement(8,
                        HeightRangePlacement.uniform(
                                VerticalAnchor.absolute(32),
                                VerticalAnchor.absolute(256))));

        // 4. SAVANNAS: the best place for rubies (~13 ores/chunk). 25 attempts, triangle from
        // Y 0 to Y 192 (peak at Y 96, just under the savanna's surface), so most of them hit stone.
        register(context, SAVANNA_RUBY_ORE_PLACED_KEY,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.SAVANNA_RUBY_ORE_KEY),
                OrePlacements.commonOrePlacement(25,
                        HeightRangePlacement.triangle(
                                VerticalAnchor.absolute(0),
                                VerticalAnchor.absolute(192))));
    }

    private static ResourceKey<PlacedFeature> registerKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(TravelYourEarth.MODID, name));
    }

    private static void register(BootstrapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key,
                                 Holder<Feature> configuration, List<PlacementModifier> modifiers) {
        context.register(key, new PlacedFeature(configuration, List.copyOf(modifiers)));
    }
}