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
import net.minecraft.world.level.levelgen.heightproviders.BiasedToBottomHeight;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

import java.util.List;

public class ModPlacedFeatures {
    // CF given to the Placed Features
    // How Many will be placed and some relation in where

    public static final ResourceKey<PlacedFeature> RUBY_ORE_PLACED_KEY = registerKey("ruby_ore_placed");
    public static final ResourceKey<PlacedFeature> MOUNTAIN_RUBY_ORE_PLACED_KEY = registerKey("mountain_ruby_ore_placed");


    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        var configuredFeatures = context.lookup(Registries.FEATURE);

// 1. RUBÍ NORMAL (Estilo Redstone/Diamante)
        register(context, RUBY_ORE_PLACED_KEY,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.RUBY_ORE_KEY),
                OrePlacements.commonOrePlacement(2,
                        HeightRangePlacement.of(
                                BiasedToBottomHeight.of(
                                        VerticalAnchor.absolute(-64),
                                        VerticalAnchor.absolute(5),
                                        2
                                )
                        )));

// 2. RUBÍ DE MONTAÑA (Estilo Esmeralda gigante)
        register(context, MOUNTAIN_RUBY_ORE_PLACED_KEY,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.MOUNTAIN_RUBY_ORE_KEY),
                OrePlacements.commonOrePlacement(5,
                        HeightRangePlacement.triangle(
                                VerticalAnchor.absolute(-64),
                                VerticalAnchor.absolute(50)

                        )));


    }

    private static ResourceKey<PlacedFeature> registerKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(TravelYourEarth.MODID, name));
    }

    private static void register(BootstrapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key,
                                 Holder<Feature> configuration, List<PlacementModifier> modifiers) {
        context.register(key, new PlacedFeature(configuration, List.copyOf(modifiers)));
    }
}
