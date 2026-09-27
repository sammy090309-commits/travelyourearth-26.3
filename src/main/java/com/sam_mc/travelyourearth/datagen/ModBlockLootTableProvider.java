package com.sam_mc.travelyourearth.datagen;

import com.sam_mc.travelyourearth.block.ModBlocks;
import com.sam_mc.travelyourearth.item.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.data.loot.LootTableSubProvider;

import java.util.Set;

public class ModBlockLootTableProvider extends BlockLootSubProvider {
    public ModBlockLootTableProvider(LootTableSubProvider.Context context) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), context);
    }

    @Override
    protected void generate() {
        dropSelf(ModBlocks.RUBY_BLOCK.get());

        dropSelf(ModBlocks.HARDENED_GLASS.get());

        // NUEVO: el bloque terráqueo se suelta a sí mismo (como el concreto).
        // Como tiene requiresCorrectToolForDrops, solo cae si lo rompes con pico.
        dropSelf(ModBlocks.EARTH_BLOCK.get());

        // NUEVO: tierra Earth -> se suelta a sí misma
        dropSelf(ModBlocks.EARTH_DIRT.get());

        // NUEVO: césped Earth -> suelta tierra Earth; con toque de seda, el césped (como vanilla)
        this.add(ModBlocks.EARTH_GRASS.get(),
                block -> createSingleItemTableWithSilkTouch(block, ModBlocks.EARTH_DIRT.get()));

        this.add(ModBlocks.RUBY_ORE.get(),
                block -> createOreDrop(ModBlocks.RUBY_ORE.get(), ModItems.RUBY.get()));

        this.add(ModBlocks.DEEPSLATE_RUBY_ORE.get(),
                block -> createOreDrop(ModBlocks.DEEPSLATE_RUBY_ORE.get(), ModItems.RUBY.get()));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().map(Holder::value)::iterator;
    }
}