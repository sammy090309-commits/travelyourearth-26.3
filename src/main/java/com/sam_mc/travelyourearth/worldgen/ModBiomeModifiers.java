package com.sam_mc.travelyourearth.worldgen;

import com.sam_mc.travelyourearth.TravelYourEarth;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModBiomeModifiers {

    public static final ResourceKey<BiomeModifier> ADD_RUBY_ORE = registerKey("add_ruby_ore");
    public static final ResourceKey<BiomeModifier> ADD_MOUNTAIN_RUBY_ORE = registerKey("add_mountain_ruby_ore");

    public static void bootstrap(BootstrapContext<BiomeModifier> context) {
        var placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        var biomes = context.lookup(Registries.BIOME);

        // 1. Generación normal (Si usas un Tag personalizado como OVERWORLD_WITHOUT_MOUNTAINS, cámbialo aquí)
        context.register(ADD_RUBY_ORE, new BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.RUBY_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES
        ));

        // 2. Generación exclusiva para Montañas y Colinas (Vetas de tamaño 8)
        context.register(ADD_MOUNTAIN_RUBY_ORE, new BiomeModifiers.AddFeaturesBiomeModifier(
                HolderSet.direct(
                        biomes.getOrThrow(Biomes.JAGGED_PEAKS),
                        biomes.getOrThrow(Biomes.FROZEN_PEAKS),
                        biomes.getOrThrow(Biomes.STONY_PEAKS),
                        biomes.getOrThrow(Biomes.SNOWY_SLOPES),
                        biomes.getOrThrow(Biomes.GROVE),
                        biomes.getOrThrow(Biomes.MEADOW),
                        biomes.getOrThrow(Biomes.WINDSWEPT_HILLS),
                        biomes.getOrThrow(Biomes.WINDSWEPT_GRAVELLY_HILLS)
                ),
                HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.MOUNTAIN_RUBY_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES
        ));
    }

    private static ResourceKey<BiomeModifier> registerKey(String name) {
        return ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, Identifier.fromNamespaceAndPath(TravelYourEarth.MODID, name));
    }
}
