package com.sam_mc.travelyourearth.datagen;

import com.sam_mc.travelyourearth.TravelYourEarth;
import com.sam_mc.travelyourearth.tags.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.concurrent.CompletableFuture;

/** Generates the structure tags of the mod (which structures each explorer map points to). */
public class ModStructureTagsProvider extends TagsProvider<Structure> {

    public ModStructureTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, Registries.STRUCTURE, lookupProvider, TravelYourEarth.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        // Trail Ruins explorer map -> vanilla trail ruins
        // (same idea as vanilla's "minecraft:on_trial_chambers_maps")
        getOrCreateRawBuilder(ModTags.Structures.ON_TRAIL_RUINS_MAPS)
                .addElement(Identifier.fromNamespaceAndPath("minecraft", "trail_ruins"));

        // Ancient City map (illegal cartographer) -> vanilla ancient city
        getOrCreateRawBuilder(ModTags.Structures.ON_ANCIENT_CITY_MAPS)
                .addElement(Identifier.fromNamespaceAndPath("minecraft", "ancient_city"));
    }
}