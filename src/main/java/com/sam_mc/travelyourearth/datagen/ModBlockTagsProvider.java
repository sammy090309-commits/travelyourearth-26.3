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

public class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, TravelYourEarth.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {

        // =====================================================================
        // MINECRAFT
        // =====================================================================

        // Se mina con pico
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.getRK(ModBlocks.RUBY_BLOCK.get()))
                .add(ModBlocks.getRK(ModBlocks.RUBY_ORE.get()))
                .add(ModBlocks.getRK(ModBlocks.DEEPSLATE_RUBY_ORE.get()))
                .add(ModBlocks.getRK(ModBlocks.HARDENED_GLASS.get()))
                .add(ModBlocks.getRK(ModBlocks.EARTH_BLOCK.get()));   // bloque terráqueo

        // Se minan con pala (como la tierra y el pasto vanilla)
        tag(BlockTags.MINEABLE_WITH_SHOVEL)
                .add(ModBlocks.getRK(ModBlocks.EARTH_DIRT.get()))
                .add(ModBlocks.getRK(ModBlocks.EARTH_GRASS.get()));

        // Cuentan como "tierra": se pueden plantar flores, árboles, caña, bambú... encima
        tag(BlockTags.DIRT)
                .add(ModBlocks.getRK(ModBlocks.EARTH_DIRT.get()))
                .add(ModBlocks.getRK(ModBlocks.EARTH_GRASS.get()));

        // Necesita pico de hierro o mejor.
        // (El bloque terráqueo NO va aquí: como el concreto, sirve cualquier pico, hasta el de madera.)
        tag(BlockTags.NEEDS_IRON_TOOL)
                .add(ModBlocks.getRK(ModBlocks.RUBY_BLOCK.get()))
                .add(ModBlocks.getRK(ModBlocks.RUBY_ORE.get()))
                .add(ModBlocks.getRK(ModBlocks.DEEPSLATE_RUBY_ORE.get()))
                .add(ModBlocks.getRK(ModBlocks.HARDENED_GLASS.get()));

        tag(BlockTags.BEACON_BASE_BLOCKS)
                .add(ModBlocks.getRK(ModBlocks.RUBY_BLOCK.get()));

        // Qué NO puede minar el pico de rubí (como el hierro: nada de obsidiana).
        // Para que mine todo como el diamante, deja solo: tag(ModTags.Blocks.INCORRECT_FOR_RUBY_TOOL);
        tag(ModTags.Blocks.INCORRECT_FOR_RUBY_TOOL)
                .addTag(BlockTags.NEEDS_DIAMOND_TOOL);

        // 26.3: bloques sólidos (lluvia, perlas de ender, asfixia, agua...).
        // El vidrio mantiene su .isSuffocating(false) de ModBlocks.
        tag(BlockTags.BLOCKS_MOTION_NO_LEAVES)
                .add(ModBlocks.getRK(ModBlocks.RUBY_BLOCK.get()))
                .add(ModBlocks.getRK(ModBlocks.HARDENED_GLASS.get()))
                .add(ModBlocks.getRK(ModBlocks.EARTH_BLOCK.get()))    // sólido como el concreto
                .add(ModBlocks.getRK(ModBlocks.EARTH_DIRT.get()))
                .add(ModBlocks.getRK(ModBlocks.EARTH_GRASS.get()));

        // Las menas entran a blocks_motion a través de #minecraft:ores, como las vanilla
        tag(BlockTags.ORES)
                .add(ModBlocks.getRK(ModBlocks.RUBY_ORE.get()))
                .add(ModBlocks.getRK(ModBlocks.DEEPSLATE_RUBY_ORE.get()));

        // =====================================================================
        // COMUNES (c:) -> compatibilidad con otros mods
        // =====================================================================

        // c:ores/ruby  ->  dentro de c:ores
        tag(ModTags.Blocks.ORES_RUBY)
                .add(ModBlocks.getRK(ModBlocks.RUBY_ORE.get()))
                .add(ModBlocks.getRK(ModBlocks.DEEPSLATE_RUBY_ORE.get()));
        tag(Tags.Blocks.ORES)
                .addTag(ModTags.Blocks.ORES_RUBY);

        // En qué piedra está cada mena (mods de generación / recetas con piedra)
        tag(Tags.Blocks.ORES_IN_GROUND_STONE)
                .add(ModBlocks.getRK(ModBlocks.RUBY_ORE.get()));
        tag(Tags.Blocks.ORES_IN_GROUND_DEEPSLATE)
                .add(ModBlocks.getRK(ModBlocks.DEEPSLATE_RUBY_ORE.get()));

        // Sueltan 1 ítem, como las menas de diamante (mods que duplican menas lo usan)
        tag(Tags.Blocks.ORE_RATES_SINGULAR)
                .add(ModBlocks.getRK(ModBlocks.RUBY_ORE.get()))
                .add(ModBlocks.getRK(ModBlocks.DEEPSLATE_RUBY_ORE.get()));

        // c:storage_blocks/ruby  ->  dentro de c:storage_blocks
        tag(ModTags.Blocks.STORAGE_BLOCKS_RUBY)
                .add(ModBlocks.getRK(ModBlocks.RUBY_BLOCK.get()));
        tag(Tags.Blocks.STORAGE_BLOCKS)
                .addTag(ModTags.Blocks.STORAGE_BLOCKS_RUBY);
    }
}