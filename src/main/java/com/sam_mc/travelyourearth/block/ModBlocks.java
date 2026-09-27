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

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(TravelYourEarth.MODID);

    // Rojo vivo en el mapa (como el bloque de redstone)
    public static final DeferredBlock<Block> RUBY_BLOCK = registerBlock("ruby_block",
            properties -> new Block(properties.mapColor(MapColor.CRIMSON_NYLIUM).strength(4f)
                    .requiresCorrectToolForDrops().sound(SoundType.METAL)));

    // Igual que la mena de diamante: color de piedra + bombo en el bloque musical
    public static final DeferredBlock<Block> RUBY_ORE = registerBlock("ruby_ore",
            properties -> new DropExperienceBlock(UniformInt.of(2, 4), properties.mapColor(MapColor.STONE)
                    .instrument(NoteBlockInstrument.BASEDRUM).strength(3f)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)));

    // Igual que la mena de diamante de pizarra profunda
    public static final DeferredBlock<Block> DEEPSLATE_RUBY_ORE = registerBlock("deepslate_ruby_ore",
            properties -> new DropExperienceBlock(UniformInt.of(3, 5), properties.mapColor(MapColor.DEEPSLATE)
                    .instrument(NoteBlockInstrument.BASEDRUM).strength(4f)
                    .requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE)));

    // Rojo en el mapa (como el vidrio tintado rojo)
    public static final DeferredBlock<Block> HARDENED_GLASS = registerBlock("hardened_glass",
            properties -> new TransparentBlock(properties.mapColor(MapColor.COLOR_RED).strength(2.5F, 3600000.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.GLASS)
                    .noOcclusion()
                    .isValidSpawn((state, level, pos, entityType) -> false)
                    .isRedstoneConductor((state, level, pos) -> false)
                    .isSuffocating((state, level, pos) -> false)));

    // Bloque terráqueo: como el concreto (dureza 1.8, pico, sonido de concreto).
    // Sin receta ni generación: bloque "oculto", solo por creativo o /give. Es el icono de la Mod Tab.
    public static final DeferredBlock<Block> EARTH_BLOCK = registerBlock("earth_block",
            properties -> new Block(properties.mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .strength(1.8F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)));

    // Tierra Earth: como la tierra vanilla (pala, sonido de tierra). Sin receta ni generación.
    public static final DeferredBlock<Block> EARTH_DIRT = registerBlock("earth_dirt",
            properties -> new Block(properties.mapColor(MapColor.COLOR_BLUE)
                    .strength(0.5F)
                    .sound(SoundType.GRAVEL)));

    // Césped Earth: como el bloque de pasto vanilla, pero sin tinte de bioma y sin expandirse.
    public static final DeferredBlock<Block> EARTH_GRASS = registerBlock("earth_grass",
            properties -> new Block(properties.mapColor(MapColor.GRASS)
                    .strength(0.6F)
                    .sound(SoundType.GRASS)));

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Function<BlockBehaviour.Properties, T> function){
        DeferredBlock<T> toReturn = BLOCKS.registerBlock(name, function);
        registerBlockItem(name, toReturn);
        return  toReturn;
    }




    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.registerItem(name, properties -> new BlockItem(block.get(), properties.useBlockDescriptionPrefix()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

    public static ResourceKey<Block> getRK(Block block) {
        return BuiltInRegistries.BLOCK.getResourceKey(block).get();
    }

}