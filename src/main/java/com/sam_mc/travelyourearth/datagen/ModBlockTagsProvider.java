package com.sam_mc.travelyourearth.datagen;

import com.sam_mc.travelyourearth.TravelYourEarth;
import com.sam_mc.travelyourearth.block.ModBlocks;
import com.sam_mc.travelyourearth.tags.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, TravelYourEarth.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {


        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.getRK(ModBlocks.RUBY_BLOCK.get()))
                .add(ModBlocks.getRK(ModBlocks.RUBY_ORE.get()))
                .add(ModBlocks.getRK(ModBlocks.DEEPSLATE_RUBY_ORE.get()));


        tag(BlockTags.NEEDS_IRON_TOOL)
                .add(ModBlocks.getRK(ModBlocks.RUBY_BLOCK.get()))
                .add(ModBlocks.getRK(ModBlocks.RUBY_ORE.get()))
                .add(ModBlocks.getRK(ModBlocks.DEEPSLATE_RUBY_ORE.get()));

        tag(BlockTags.BEACON_BASE_BLOCKS)
                .add(ModBlocks.getRK(ModBlocks.RUBY_BLOCK.get()));

        tag(BlockTags.BEACON_BASE_BLOCKS)
                .add(ModBlocks.getRK(ModBlocks.RUBY_BLOCK.get()));









    }
}
