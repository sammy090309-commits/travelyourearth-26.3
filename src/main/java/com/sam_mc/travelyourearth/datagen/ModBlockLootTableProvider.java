package com.sam_mc.travelyourearth.datagen;

import com.sam_mc.travelyourearth.block.ModBlocks;
import com.sam_mc.travelyourearth.item.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

import java.util.Set;

/** Generates what each block of the mod drops when broken. */
public class ModBlockLootTableProvider extends BlockLootSubProvider {

    public ModBlockLootTableProvider(LootTableSubProvider.Context context) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), context);
    }

    @Override
    protected void generate() {

        // =====================================================================
        // Ruby
        // =====================================================================

        dropSelf(ModBlocks.RUBY_BLOCK.get());

        dropSelf(ModBlocks.HARDENED_GLASS.get());

        // Ores: drop a ruby (affected by Fortune); with Silk Touch, the ore itself
        this.add(ModBlocks.RUBY_ORE.get(),
                block -> createOreDrop(ModBlocks.RUBY_ORE.get(), ModItems.RUBY.get()));

        this.add(ModBlocks.DEEPSLATE_RUBY_ORE.get(),
                block -> createOreDrop(ModBlocks.DEEPSLATE_RUBY_ORE.get(), ModItems.RUBY.get()));

        // =====================================================================
        // Earth
        // =====================================================================

        // Earth block drops itself (like concrete).
        // Since it has requiresCorrectToolForDrops, it only drops when broken with a pickaxe.
        dropSelf(ModBlocks.EARTH_BLOCK.get());

        // Earth dirt drops itself
        dropSelf(ModBlocks.EARTH_DIRT.get());

        // Earth grass drops Earth dirt; with Silk Touch, the grass block itself (like vanilla)
        this.add(ModBlocks.EARTH_GRASS.get(),
                block -> createSingleItemTableWithSilkTouch(block, ModBlocks.EARTH_DIRT.get()));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().map(Holder::value)::iterator;
    }
}