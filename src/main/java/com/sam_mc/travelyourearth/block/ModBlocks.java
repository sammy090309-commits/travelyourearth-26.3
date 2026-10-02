package com.sam_mc.travelyourearth.block;

import com.sam_mc.travelyourearth.TravelYourEarth;
import com.sam_mc.travelyourearth.item.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;

/**
 * All blocks added by Travel Your Earth.
 * Every block registered with {@link #registerBlock} also gets its BlockItem automatically.
 */
public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(TravelYourEarth.MODID);

    // =========================================================================
    // Ruby
    // =========================================================================

    /** Ruby block. Bright red on maps (like the redstone block). */
    public static final DeferredBlock<Block> RUBY_BLOCK = registerBlock("ruby_block",
            properties -> new Block(properties.mapColor(MapColor.CRIMSON_NYLIUM).strength(4f)
                    .requiresCorrectToolForDrops().sound(SoundType.METAL)));

    /** Ruby ore. Same as diamond ore: stone map color + bass drum in note blocks. */
    public static final DeferredBlock<Block> RUBY_ORE = registerBlock("ruby_ore",
            properties -> new DropExperienceBlock(UniformInt.of(2, 4), properties.mapColor(MapColor.STONE)
                    .instrument(NoteBlockInstrument.BASEDRUM).strength(3f)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)));

    /** Deepslate ruby ore. Same as deepslate diamond ore. */
    public static final DeferredBlock<Block> DEEPSLATE_RUBY_ORE = registerBlock("deepslate_ruby_ore",
            properties -> new DropExperienceBlock(UniformInt.of(3, 5), properties.mapColor(MapColor.DEEPSLATE)
                    .instrument(NoteBlockInstrument.BASEDRUM).strength(4f)
                    .requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE)));

    /** Hardened glass. Red on maps (like red stained glass). */
    public static final DeferredBlock<Block> HARDENED_GLASS = registerBlock("hardened_glass",
            properties -> new TransparentBlock(properties.mapColor(MapColor.COLOR_RED).strength(2.5F, 3600000.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.GLASS)
                    .noOcclusion()
                    .isValidSpawn((state, level, pos, entityType) -> false)
                    .isRedstoneConductor((state, level, pos) -> false)
                    .isSuffocating((state, level, pos) -> false)));

    // =========================================================================
    // Earth (hidden blocks: no recipe or world generation, only creative or /give)
    // =========================================================================

    /** Earth block. Like concrete (hardness 1.8, pickaxe, stone sound). Icon of the Mod Tab. */
    public static final DeferredBlock<Block> EARTH_BLOCK = registerBlock("earth_block",
            properties -> new Block(properties.mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .strength(1.8F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)));

    /** Earth dirt. Like vanilla dirt (shovel, dirt sound). */
    public static final DeferredBlock<Block> EARTH_DIRT = registerBlock("earth_dirt",
            properties -> new Block(properties.mapColor(MapColor.COLOR_BLUE)
                    .strength(0.5F)
                    .sound(SoundType.GRAVEL)));

    /** Earth grass block. Like the vanilla grass block, but without biome tint and it doesn't spread. */
    public static final DeferredBlock<Block> EARTH_GRASS = registerBlock("earth_grass",
            properties -> new Block(properties.mapColor(MapColor.GRASS)
                    .strength(0.6F)
                    .sound(SoundType.GRASS)));

    // =========================================================================
    // Helpers
    // =========================================================================

    /** Registers a block and its BlockItem. */
    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Function<BlockBehaviour.Properties, T> function) {
        DeferredBlock<T> toReturn = BLOCKS.registerBlock(name, function);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.registerItem(name, properties -> new BlockItem(block.get(), properties.useBlockDescriptionPrefix()));
    }

    public static ResourceKey<Block> getRK(Block block) {
        return BuiltInRegistries.BLOCK.getResourceKey(block).get();
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}