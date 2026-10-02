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
 * Vanilla reference (official wiki, Java):
 *   - Diamond: 4 batches (7 + 1/9 + 4 + 2 veins per chunk) below Y 16, size 4  -> about 26 ores/chunk
 *   - Iron: 90 + 10 + 10 veins per chunk                                        -> much more
 */
public class ModPlacedFeatures {

    public static final ResourceKey<PlacedFeature> RUBY_ORE_PLACED_KEY = registerKey("ruby_ore_placed");
    public static final ResourceKey<PlacedFeature> MOUNTAIN_RUBY_ORE_PLACED_KEY = registerKey("mountain_ruby_ore_placed");
    public static final ResourceKey<PlacedFeature> DAPPLED_RUBY_ORE_PLACED_KEY = registerKey("dappled_ruby_ore_placed");

    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        var configuredFeatures = context.lookup(Registries.FEATURE);

        // 1. ALL BIOMES: much rarer than diamond.
        // Same shape as the diamond main vein: triangle from Y -144 to Y 16
        // (peak at Y -64, the bottom; nothing above Y 16), but only 4 veins instead of 7
        // and without the other 3 diamond batches.
        register(context, RUBY_ORE_PLACED_KEY,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.RUBY_ORE_KEY),
                OrePlacements.commonOrePlacement(4,
                        HeightRangePlacement.triangle(
                                VerticalAnchor.absolute(-144),
                                VerticalAnchor.absolute(16))));

        // 2. MOUNTAINS: added on top of the "all biomes" layer.
        // 14 veins per chunk, triangle from Y -64 to Y 60 (peak at Y -2).
        register(context, MOUNTAIN_RUBY_ORE_PLACED_KEY,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.MOUNTAIN_RUBY_ORE_KEY),
                OrePlacements.commonOrePlacement(14,
                        HeightRangePlacement.triangle(
                                VerticalAnchor.absolute(-64),
                                VerticalAnchor.absolute(60))));

        // 3. DAPPLED FOREST: added on top of the "all biomes" layer.
        // 30 attempts per chunk, uniform from Y 12 to Y 256 (same chance at any height;
        // attempts that land in the air don't place anything).
        register(context, DAPPLED_RUBY_ORE_PLACED_KEY,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.DAPPLED_RUBY_ORE_KEY),
                OrePlacements.commonOrePlacement(30,
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