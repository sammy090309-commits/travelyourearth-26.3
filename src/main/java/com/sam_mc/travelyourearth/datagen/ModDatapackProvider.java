package com.sam_mc.travelyourearth.datagen;

import com.sam_mc.travelyourearth.TravelYourEarth;
import com.sam_mc.travelyourearth.worldgen.ModBiomeModifiers;
import com.sam_mc.travelyourearth.worldgen.ModConfiguredFeatures;
import com.sam_mc.travelyourearth.worldgen.ModPlacedFeatures;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Generates the datapack registry entries of the mod: features, placed features
 * and biome modifiers (ruby ore generation).
 */
public class ModDatapackProvider {

    /**
     * 26.3: Feature was merged with FeatureConfiguration, so the registry is now
     * Registries.FEATURE (folder "worldgen/feature"; in 26.2 it was CONFIGURED_FEATURE).
     */
    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.FEATURE, ModConfiguredFeatures::bootstrap)
            .add(Registries.PLACED_FEATURE, ModPlacedFeatures::bootstrap)
            .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ModBiomeModifiers::bootstrap);

    /**
     * 26.3: this class no longer extends DatapackBuiltinEntriesProvider. Its raw constructor
     * needs internal parameters that mods don't build by hand, so NeoForge exposes the
     * factory method forWorldLayer(output, name, registries, builder, modIds) instead.
     * "name" is only a label for the provider (logs / data generation), so the MODID is fine.
     */
    public static DatapackBuiltinEntriesProvider create(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        return DatapackBuiltinEntriesProvider.forWorldLayer(
                output,
                TravelYourEarth.MODID,
                registries,
                BUILDER,
                Set.of(TravelYourEarth.MODID)
        );
    }
}