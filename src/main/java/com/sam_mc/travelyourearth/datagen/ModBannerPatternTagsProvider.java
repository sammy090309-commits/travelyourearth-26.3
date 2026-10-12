package com.sam_mc.travelyourearth.datagen;

import com.sam_mc.travelyourearth.TravelYourEarth;
import com.sam_mc.travelyourearth.banner.ModBannerPatterns;
import com.sam_mc.travelyourearth.tags.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.level.block.entity.BannerPattern;

import java.util.concurrent.CompletableFuture;

/** Generates the banner pattern tags of the mod. */
public class ModBannerPatternTagsProvider extends TagsProvider<BannerPattern> {

    public ModBannerPatternTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, Registries.BANNER_PATTERN, lookupProvider, TravelYourEarth.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        // Like vanilla #minecraft:pattern_item/flow: the patterns our item unlocks in the loom.
        // It is NOT in #minecraft:no_item_required -> you need the item to use it.
        getOrCreateRawBuilder(ModTags.BannerPatterns.PATTERN_ITEM_REBELLION)
                .addElement(ModBannerPatterns.REBELLION.identifier());
    }
}