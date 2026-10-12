package com.sam_mc.travelyourearth.datagen;

import com.sam_mc.travelyourearth.worldgen.ModTrialChambers;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.concurrent.CompletableFuture;

/**
 * 1.2.1: writes data/minecraft/worldgen/structure/trial_chambers.json (vanilla + the parched group).
 *
 * Why a provider of its own: NeoForge's registry datagen (GatherDataRegistryEntriesEvent) only writes
 * the entries of the mod's namespace (travelyourearth:...), so a minecraft:... entry registered there is
 * silently skipped. This provider builds the structure with the same code (ModTrialChambers) and
 * encodes it with the vanilla codec, so it's still 100% datagen.
 */
public class ModTrialChambersStructureProvider implements DataProvider {

    private final PackOutput.PathProvider structures;
    private final CompletableFuture<HolderLookup.Provider> registries;

    public ModTrialChambersStructureProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        this.structures = output.createRegistryElementsPathProvider(Registries.STRUCTURE);
        this.registries = registries;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return registries.thenCompose(lookup -> {
            Structure trialChambers = ModTrialChambers.createTrialChambers(
                    lookup.lookupOrThrow(Registries.BIOME),
                    lookup.lookupOrThrow(Registries.TEMPLATE_POOL));
            return DataProvider.saveStable(cache, lookup, Structure.DIRECT_CODEC, trialChambers,
                    structures.json(BuiltinStructures.TRIAL_CHAMBERS.identifier()));
        });
    }

    @Override
    public String getName() {
        return "Trial Chambers structure (parched spawner)";
    }
}
