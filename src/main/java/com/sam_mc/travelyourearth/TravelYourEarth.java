package com.sam_mc.travelyourearth;

import com.sam_mc.travelyourearth.block.ModBlocks;
import com.sam_mc.travelyourearth.item.ModItems;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(TravelYourEarth.MODID)
public class TravelYourEarth {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "travelyourearth";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public TravelYourEarth(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        //  modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {

    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.insertBefore(
                    new ItemStack(Items.DIAMOND),
                    new ItemStack(ModItems.RUBY.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
        }

        // Pestaña de Bloques de Construcción
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.insertBefore(
                    new ItemStack(Items.DIAMOND_BLOCK),
                    new ItemStack(ModBlocks.RUBY_BLOCK.get()), // O ModItems.RUBY_BLOCK.get() según donde registres tu BlockItem
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
        }

        if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
            event.insertBefore(
                    new ItemStack(Items.DIAMOND_ORE),
                    new ItemStack(ModBlocks.RUBY_ORE.get()), // O ModItems.RUBY_BLOCK.get() según donde registres tu BlockItem
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
        }

        if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
            event.insertAfter(
                    new ItemStack(ModBlocks.RUBY_ORE),
                    new ItemStack(ModBlocks.DEEPSLATE_RUBY_ORE.get()), // O ModItems.RUBY_BLOCK.get() según donde registres tu BlockItem
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
        }

        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.insertBefore(
                    new ItemStack(Items.DIAMOND_SWORD),
                    new ItemStack(ModItems.RUBY_SWORD.get()), // O ModItems.RUBY_BLOCK.get() según donde registres tu BlockItem
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
        }

        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.insertBefore(
                    new ItemStack(Items.DIAMOND_AXE),
                    new ItemStack(ModItems.RUBY_AXE.get()), // O ModItems.RUBY_BLOCK.get() según donde registres tu BlockItem
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
        }

        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.insertBefore(
                    new ItemStack(Items.DIAMOND_SPEAR),
                    new ItemStack(ModItems.RUBY_SPEAR.get()), // O ModItems.RUBY_BLOCK.get() según donde registres tu BlockItem
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
        }

        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.insertBefore(
                    new ItemStack(Items.DIAMOND_SHOVEL),
                    new ItemStack(ModItems.RUBY_SHOVEL.get()), // O ModItems.RUBY_BLOCK.get() según donde registres tu BlockItem
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
        }

        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.insertAfter(
                    new ItemStack((ItemLike) ModItems.RUBY_SHOVEL),
                    new ItemStack(ModItems.RUBY_PICKAXE.get()), // O ModItems.RUBY_BLOCK.get() según donde registres tu BlockItem
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
        }

        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.insertAfter(
                    new ItemStack((ItemLike) ModItems.RUBY_PICKAXE),
                    new ItemStack(ModItems.RUBY_AXE.get()), // O ModItems.RUBY_BLOCK.get() según donde registres tu BlockItem
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
        }

        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES){
            event.insertAfter(
                    new ItemStack((ItemLike) ModItems.RUBY_AXE),
                    new ItemStack(ModItems.RUBY_HOE.get()), // O ModItems.RUBY_BLOCK.get() según donde registres tu BlockItem
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
        }


        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            // Inserta el casco justo después del de diamante
            event.insertBefore(
                    new ItemStack(Items.DIAMOND_HELMET),
                    new ItemStack(ModItems.RUBY_HELMET.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
            // Inserta el peto después del casco de rubí
            event.insertAfter(
                    new ItemStack(ModItems.RUBY_HELMET.get()),
                    new ItemStack(ModItems.RUBY_CHESTPLATE.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
            // Inserta las perneras después del peto de rubí
            event.insertAfter(
                    new ItemStack(ModItems.RUBY_CHESTPLATE.get()),
                    new ItemStack(ModItems.RUBY_LEGGINGS.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
            // Inserta las botas después de las perneras de rubí
            event.insertAfter(
                    new ItemStack(ModItems.RUBY_LEGGINGS.get()),
                    new ItemStack(ModItems.RUBY_BOOTS.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
        }

        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.insertBefore(
                    new ItemStack(Items.DIAMOND_HORSE_ARMOR),
                    new ItemStack(ModItems.RUBY_HORSE_ARMOR.get()), // O ModItems.RUBY_BLOCK.get() según donde registres tu BlockItem
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
        }

        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.insertBefore(
                    new ItemStack(Items.DIAMOND_NAUTILUS_ARMOR),
                    new ItemStack(ModItems.RUBY_NAUTILUS_ARMOR.get()), // O ModItems.RUBY_BLOCK.get() según donde registres tu BlockItem
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
        }

    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }
}
