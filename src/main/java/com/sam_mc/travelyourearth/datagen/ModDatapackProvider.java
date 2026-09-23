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

public class ModDatapackProvider {

    // VERIFICAR: en 26.2 esta línea usaba "Registries.CONFIGURED_FEATURE".
    // En 26.3, Feature se fusionó con FeatureConfiguration (confirmado en el
    // changelog oficial de la 26.3), y la carpeta del datapack pasó de
    // "worldgen/configured_feature" a "worldgen/feature". Por convención de
    // Mojang (el nombre de la constante siempre coincide con la carpeta),
    // lo más probable es que la constante ahora sea "Registries.FEATURE".
    // CÓMO CONFIRMARLO EN 10 SEGUNDOS: en IntelliJ, borra "FEATURE" de esta
    // línea, escribe "Registries." y presiona Ctrl+Espacio. Va a mostrarte
    // la lista real de constantes disponibles en TU versión exacta de NeoForge.
    // Busca la que apunte a features (probablemente "FEATURE"). Si no es la
    // que puse aquí, pégame el nombre exacto que te sugirió y la corrijo.
    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.FEATURE, ModConfiguredFeatures::bootstrap)
            .add(Registries.PLACED_FEATURE, ModPlacedFeatures::bootstrap)
            .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ModBiomeModifiers::bootstrap);

    // ANTES: "ModDatapackProvider" heredaba (extends) de DatapackBuiltinEntriesProvider
    // y tenía un constructor que llamaba a super(output, registries, BUILDER, modIds).
    //
    // AHORA: ese constructor "crudo" de DatapackBuiltinEntriesProvider pide parámetros
    // internos que un mod no arma a mano (Collection<RegistryDataLoader.RegistryData<?>>).
    // Para mods, NeoForge ahora expone un método estático de fábrica en su lugar:
    // DatapackBuiltinEntriesProvider.forWorldLayer(output, name, registries, builder, modIds).
    // Por eso esta clase ya NO hereda de DatapackBuiltinEntriesProvider, sino que
    // simplemente devuelve una instancia lista para usar con este método "create".
    //
    // El parámetro "name" es nuevo (antes no existía) y, según el código fuente de
    // NeoForge, es solo una etiqueta/identificador del provider (para logs y el
    // sistema de generación) — no afecta el contenido generado, así que usar el
    // MODID ahí es seguro.
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