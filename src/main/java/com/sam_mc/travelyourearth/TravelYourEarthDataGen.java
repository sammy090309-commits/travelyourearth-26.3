package com.sam_mc.travelyourearth;

import com.sam_mc.travelyourearth.datagen.*;
import com.sam_mc.travelyourearth.worldgen.ModBiomeModifiers;
import com.sam_mc.travelyourearth.worldgen.ModConfiguredFeatures;
import com.sam_mc.travelyourearth.worldgen.ModPlacedFeatures;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;
import java.util.Set;

@EventBusSubscriber(modid = TravelYourEarth.MODID)
public class TravelYourEarthDataGen {
    @SubscribeEvent
    public static void gatherClientData(GatherDataEvent.Client event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();

        // =====================================================================
        // REGISTROS "WORLD" (NO reloadables): features, placed features, biome modifiers.
        // ANTES: esto (junto con las loot tables) se mandaba con
        // event.createDatapackRegistryObjects(...), método que ya NO existe.
        // AHORA: se separa segun si el registro es reloadable o no.
        //
        // VERIFICAR: Registries.FEATURE sigue siendo mi mejor inferencia para lo que
        // antes era Registries.CONFIGURED_FEATURE (ver comentario detallado en
        // ModDatapackProvider.java). Confirmalo con Ctrl+Espacio si sigue en rojo.
        // =====================================================================
        RegistrySetBuilder worldBuilder = new RegistrySetBuilder()
                .add(Registries.FEATURE, ModConfiguredFeatures::bootstrap)
                .add(Registries.PLACED_FEATURE, ModPlacedFeatures::bootstrap)
                .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ModBiomeModifiers::bootstrap);
        event.createWorldRegistryObjects(worldBuilder, Set.of(TravelYourEarth.MODID));

        // =====================================================================
        // REGISTROS RELOADABLES: loot tables, y recetas+advancements juntos.
        //
        // CONFIRMADO (fuente decompilada de RegistrySetBuilder): existe una
        // sobrecarga .add(MultiRegistryBootstrap) que NO lleva ResourceKey,
        // porque el propio MultiRegistryBootstrap ya declara sus registros
        // via requestedRegistries(). Por eso aqui NO se pasa Registries.RECIPE.
        // =====================================================================
        RegistrySetBuilder reloadableBuilder = new RegistrySetBuilder()
                .add(
                        Registries.LOOT_TABLE,
                        new LootTableProvider(
                                Set.of(),
                                List.of(
                                        new LootTableProvider.SubProviderEntry(ModBlockLootTableProvider::new, LootContextParamSets.BLOCK),
                                        new LootTableProvider.SubProviderEntry(ModExtraLootProvider::new, LootContextParamSets.ALL_PARAMS)
                                )
                        )
                )
                .add(ModRecipeProvider.create());
        event.createReloadableRegistryObjects(reloadableBuilder, Set.of(TravelYourEarth.MODID));

        // =====================================================================
        // Providers "normales" (no van por RegistrySetBuilder).
        // ANTES: event.getLookupProvider() -> YA NO EXISTE.
        // AHORA: event.getWorldLookupProvider() o event.getReloadableLookupProvider().
        // Uso el "reloadable" porque es el mas completo (incluye todo lo del world layer
        // mas lo reloadable), que es lo que normalmente necesitan tags y loot modifiers.
        // =====================================================================
        var lookupProvider = event.getReloadableLookupProvider();

        generator.addProvider(true, new ModTrimMaterialProvider(packOutput));
        generator.addProvider(true, new ModModelProvider(packOutput));
        generator.addProvider(true, new ModBlockTagsProvider(packOutput, lookupProvider));
        generator.addProvider(true, new ModItemTagsProvider(packOutput, lookupProvider));
        generator.addProvider(true, new ModEquipmentAssetProvider(packOutput));
        generator.addProvider(true, new ModGlobalLootModifierProvider(packOutput, lookupProvider));
    }
}