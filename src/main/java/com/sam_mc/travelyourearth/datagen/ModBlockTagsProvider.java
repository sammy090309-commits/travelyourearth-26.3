package com.sam_mc.travelyourearth.datagen;

import com.sam_mc.travelyourearth.TravelYourEarth;
import com.sam_mc.travelyourearth.block.ModBlocks;
import com.sam_mc.travelyourearth.tags.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

import java.util.concurrent.CompletableFuture;

/** Generates the block tags of the mod (vanilla tags + common "c:" tags). */
public class ModBlockTagsProvider extends BlockTagsProvider {

    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, TravelYourEarth.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {

        // =====================================================================
        // MINECRAFT
        // =====================================================================

        // Mined with a pickaxe
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.getRK(ModBlocks.RUBY_BLOCK.get()))
                .add(ModBlocks.getRK(ModBlocks.RUBY_ORE.get()))
                .add(ModBlocks.getRK(ModBlocks.DEEPSLATE_RUBY_ORE.get()))
                .add(ModBlocks.getRK(ModBlocks.HARDENED_GLASS.get()))
                .add(ModBlocks.getRK(ModBlocks.EARTH_BLOCK.get()));   // earth block

        // Mined with a shovel (like vanilla dirt and grass)
        tag(BlockTags.MINEABLE_WITH_SHOVEL)
                .add(ModBlocks.getRK(ModBlocks.EARTH_DIRT.get()))
                .add(ModBlocks.getRK(ModBlocks.EARTH_GRASS.get()));

        // Count as "dirt": flowers, trees, sugar cane, bamboo... can be planted on them
        tag(BlockTags.DIRT)
                .add(ModBlocks.getRK(ModBlocks.EARTH_DIRT.get()))
                .add(ModBlocks.getRK(ModBlocks.EARTH_GRASS.get()));

        // Needs an iron pickaxe or better.
        // (The earth block does NOT go here: like concrete, any pickaxe works, even wood.)
        tag(BlockTags.NEEDS_IRON_TOOL)
                .add(ModBlocks.getRK(ModBlocks.RUBY_BLOCK.get()))
                .add(ModBlocks.getRK(ModBlocks.RUBY_ORE.get()))
                .add(ModBlocks.getRK(ModBlocks.DEEPSLATE_RUBY_ORE.get()))
                .add(ModBlocks.getRK(ModBlocks.HARDENED_GLASS.get()));

        tag(BlockTags.BEACON_BASE_BLOCKS)
                .add(ModBlocks.getRK(ModBlocks.RUBY_BLOCK.get()));

        // What the ruby pickaxe CAN'T mine (like iron: no obsidian).
        // To make it mine everything like diamond, leave only: tag(ModTags.Blocks.INCORRECT_FOR_RUBY_TOOL);
        tag(ModTags.Blocks.INCORRECT_FOR_RUBY_TOOL)
                .addTag(BlockTags.NEEDS_DIAMOND_TOOL);

        // 26.3: solid blocks (rain, ender pearls, suffocation, water...).
        // The glass keeps its .isSuffocating(false) from ModBlocks.
        tag(BlockTags.BLOCKS_MOTION_NO_LEAVES)
                .add(ModBlocks.getRK(ModBlocks.RUBY_BLOCK.get()))
                .add(ModBlocks.getRK(ModBlocks.HARDENED_GLASS.get()))
                .add(ModBlocks.getRK(ModBlocks.EARTH_BLOCK.get()))    // solid like concrete
                .add(ModBlocks.getRK(ModBlocks.EARTH_DIRT.get()))
                .add(ModBlocks.getRK(ModBlocks.EARTH_GRASS.get()));

        // Ores get into blocks_motion through #minecraft:ores, like vanilla ores
        tag(BlockTags.ORES)
                .add(ModBlocks.getRK(ModBlocks.RUBY_ORE.get()))
                .add(ModBlocks.getRK(ModBlocks.DEEPSLATE_RUBY_ORE.get()));

        // =====================================================================
        // COMMON (c:) -> compatibility with other mods
        // =====================================================================

        // c:ores/ruby  ->  inside c:ores
        tag(ModTags.Blocks.ORES_RUBY)
                .add(ModBlocks.getRK(ModBlocks.RUBY_ORE.get()))
                .add(ModBlocks.getRK(ModBlocks.DEEPSLATE_RUBY_ORE.get()));
        tag(Tags.Blocks.ORES)
                .addTag(ModTags.Blocks.ORES_RUBY);

        // Which stone each ore is in (world generation mods / recipes with stone)
        tag(Tags.Blocks.ORES_IN_GROUND_STONE)
                .add(ModBlocks.getRK(ModBlocks.RUBY_ORE.get()));
        tag(Tags.Blocks.ORES_IN_GROUND_DEEPSLATE)
                .add(ModBlocks.getRK(ModBlocks.DEEPSLATE_RUBY_ORE.get()));

        // They drop 1 item, like diamond ores (used by ore doubling mods)
        tag(Tags.Blocks.ORE_RATES_SINGULAR)
                .add(ModBlocks.getRK(ModBlocks.RUBY_ORE.get()))
                .add(ModBlocks.getRK(ModBlocks.DEEPSLATE_RUBY_ORE.get()));

        // c:storage_blocks/ruby  ->  inside c:storage_blocks
        tag(ModTags.Blocks.STORAGE_BLOCKS_RUBY)
                .add(ModBlocks.getRK(ModBlocks.RUBY_BLOCK.get()));
        tag(Tags.Blocks.STORAGE_BLOCKS)
                .addTag(ModTags.Blocks.STORAGE_BLOCKS_RUBY);
    }
}