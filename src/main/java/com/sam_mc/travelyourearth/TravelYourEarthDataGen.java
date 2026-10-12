package com.sam_mc.travelyourearth;

import com.sam_mc.travelyourearth.banner.ModBannerPatterns;
import com.sam_mc.travelyourearth.datagen.*;
import com.sam_mc.travelyourearth.enchantment.ModEnchantments;
import com.sam_mc.travelyourearth.worldgen.ModBiomeModifiers;
import com.sam_mc.travelyourearth.worldgen.ModConfiguredFeatures;
import com.sam_mc.travelyourearth.worldgen.ModPlacedFeatures;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.data.event.GatherDataRegistryEntriesEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;
import java.util.Set;

/** Runs every data generator of the mod (runData). */
@EventBusSubscriber(modid = TravelYourEarth.MODID)
public class TravelYourEarthDataGen {

    /**
     * 1. DATAPACK REGISTRY ENTRIES: features, placed features, biome modifiers, enchantments,
     *    banner patterns, loot tables, recipes and advancements.
     *
     * NeoForge 26.3.0.58+: event.createWorldRegistryObjects(...) and createReloadableRegistryObjects(...)
     * were removed. Everything goes here now, in one list (NeoForge itself knows which ones are
     * "world" and which ones are "reloadable"). event.add(...) works like the old RegistrySetBuilder.
     */
    @SubscribeEvent
    public static void gatherRegistryEntries(GatherDataRegistryEntriesEvent event) {
        event.add(Registries.FEATURE, ModConfiguredFeatures::bootstrap)
                .add(Registries.PLACED_FEATURE, ModPlacedFeatures::bootstrap)
                .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ModBiomeModifiers::bootstrap)
                .add(Registries.ENCHANTMENT, ModEnchantments::bootstrap)         // enchantments (1.2)
                .add(Registries.BANNER_PATTERN, ModBannerPatterns::bootstrap)    // banner patterns (1.2)
                .add(Registries.LOOT_TABLE, new LootTableProvider(
                        Set.of(),
                        List.of(
                                new LootTableProvider.SubProviderEntry(ModBlockLootTableProvider::new, LootContextParamSets.BLOCK),
                                new LootTableProvider.SubProviderEntry(ModExtraLootProvider::new, LootContextParamSets.ALL_PARAMS))))
                .add(ModRecipeProvider.create())                                  // recipes (+ their advancements)
                .add(Registries.ADVANCEMENT, new AdvancementProvider(List.of(ModAdvancementProvider::new)));
    }

    /** 2. "Normal" providers (they don't go through the registry entries above). */
    @SubscribeEvent
    public static void gatherClientData(GatherDataEvent.Client event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();

        // The reloadable lookup is the most complete one (world layer + reloadable):
        // it's what tags and loot modifiers need
        var lookupProvider = event.getReloadableLookupProvider();

        generator.addProvider(true, new ModTrimMaterialProvider(packOutput));
        generator.addProvider(true, new ModModelProvider(packOutput));
        generator.addProvider(true, new ModBlockTagsProvider(packOutput, lookupProvider));
        generator.addProvider(true, new ModItemTagsProvider(packOutput, lookupProvider));
        generator.addProvider(true, new ModStructureTagsProvider(packOutput, lookupProvider)); // explorer map tags (1.2)
        generator.addProvider(true, new ModEnchantmentTagsProvider(packOutput, lookupProvider)); // enchantment tags (1.2)
        generator.addProvider(true, new ModEntityTypeTagsProvider(packOutput, lookupProvider)); // entity tags (1.2)
        generator.addProvider(true, new ModBannerPatternTagsProvider(packOutput, lookupProvider)); // banner pattern tags (1.2)
        generator.addProvider(true, new ModEquipmentAssetProvider(packOutput));
        generator.addProvider(true, new ModGlobalLootModifierProvider(packOutput, lookupProvider));
        generator.addProvider(true, new ModSoundDefinitionsProvider(packOutput)); // sounds.json
        generator.addProvider(true, new ModParticleDescriptionProvider(packOutput)); // particles/*.json (golden_spark)
    }
}