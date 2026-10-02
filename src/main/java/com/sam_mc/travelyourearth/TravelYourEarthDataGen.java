package com.sam_mc.travelyourearth;

import com.sam_mc.travelyourearth.datagen.*;
import com.sam_mc.travelyourearth.worldgen.ModBiomeModifiers;
import com.sam_mc.travelyourearth.worldgen.ModConfiguredFeatures;
import com.sam_mc.travelyourearth.worldgen.ModPlacedFeatures;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;
import java.util.Set;

/** Runs every data generator of the mod (runData). */
@EventBusSubscriber(modid = TravelYourEarth.MODID)
public class TravelYourEarthDataGen {

    @SubscribeEvent
    public static void gatherClientData(GatherDataEvent.Client event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();

        // =====================================================================
        // 1. "WORLD" REGISTRIES (not reloadable): features, placed features, biome modifiers.
        // 26.3: event.createDatapackRegistryObjects(...) no longer exists; registries are
        // split depending on whether they're reloadable or not.
        // =====================================================================
        RegistrySetBuilder worldBuilder = new RegistrySetBuilder()
                .add(Registries.FEATURE, ModConfiguredFeatures::bootstrap)
                .add(Registries.PLACED_FEATURE, ModPlacedFeatures::bootstrap)
                .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ModBiomeModifiers::bootstrap);
        event.createWorldRegistryObjects(worldBuilder, Set.of(TravelYourEarth.MODID));

        // =====================================================================
        // 2. RELOADABLE REGISTRIES: loot tables, recipes and advancements.
        // .add(MultiRegistryBootstrap) doesn't take a ResourceKey, because the
        // MultiRegistryBootstrap already declares its registries in requestedRegistries().
        // That's why Registries.RECIPE isn't passed here.
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
                .add(ModRecipeProvider.create())
                .add(Registries.ADVANCEMENT, new AdvancementProvider(List.of(ModAdvancementProvider::new)));
        event.createReloadableRegistryObjects(reloadableBuilder, Set.of(TravelYourEarth.MODID));

        // =====================================================================
        // 3. "Normal" providers (they don't go through RegistrySetBuilder).
        // 26.3: event.getLookupProvider() no longer exists -> getWorldLookupProvider()
        // or getReloadableLookupProvider(). We use the reloadable one because it's the most
        // complete (world layer + reloadable), which is what tags and loot modifiers need.
        // =====================================================================
        var lookupProvider = event.getReloadableLookupProvider();

        generator.addProvider(true, new ModTrimMaterialProvider(packOutput));
        generator.addProvider(true, new ModModelProvider(packOutput));
        generator.addProvider(true, new ModBlockTagsProvider(packOutput, lookupProvider));
        generator.addProvider(true, new ModItemTagsProvider(packOutput, lookupProvider));
        generator.addProvider(true, new ModEquipmentAssetProvider(packOutput));
        generator.addProvider(true, new ModGlobalLootModifierProvider(packOutput, lookupProvider));
        generator.addProvider(true, new ModSoundDefinitionsProvider(packOutput)); // sounds.json
    }
}