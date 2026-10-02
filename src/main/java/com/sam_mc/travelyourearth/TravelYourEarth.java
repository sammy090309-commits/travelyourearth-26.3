package com.sam_mc.travelyourearth;

import com.sam_mc.travelyourearth.block.ModBlocks;
import com.sam_mc.travelyourearth.condition.ModConditions;
import com.sam_mc.travelyourearth.item.ModCreativeModTabs;
import com.sam_mc.travelyourearth.item.ModItems;
import com.sam_mc.travelyourearth.loot.ModLootModifiers;
import com.sam_mc.travelyourearth.sound.ModSounds;
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

/**
 * Main class of Travel Your Earth.
 * (The client-only part, like the config screen, is in TravelYourEarthClient.)
 */
// The value here must match an entry in the META-INF/neoforge.mods.toml file
@Mod(TravelYourEarth.MODID)
public class TravelYourEarth {

    /** Mod id, in a common place for everything to reference. */
    public static final String MODID = "travelyourearth";
    public static final Logger LOGGER = LogUtils.getLogger();

    // =========================================================================
    // Setup
    // =========================================================================

    // The constructor is the first code that runs when the mod is loaded.
    // FML recognizes some parameter types like IEventBus or ModContainer and passes them in automatically.
    public TravelYourEarth(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        // Registries
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModCreativeModTabs.register(modEventBus);   // "Travel Your Earth" creative tab
        ModLootModifiers.register(modEventBus);
        ModSounds.register(modEventBus);            // mod sounds
        ModConditions.register(modEventBus);        // data conditions (advancement tab option)

        NeoForge.EVENT_BUS.register(this);

        // Adds the mod items to the vanilla creative tabs
        modEventBus.addListener(this::addCreative);

        // Client config (config/travelyourearth-client.toml)
        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
        // Local config (formerly COMMON; renamed to LOCAL in NeoForge 26.3.0.37-beta)
        modContainer.registerConfig(ModConfig.Type.LOCAL, Config.COMMON_SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {

    }

    // =========================================================================
    // Vanilla creative tabs: each ruby item goes next to its diamond version
    // =========================================================================
    private void addCreative(BuildCreativeModeTabContentsEvent event) {

        // --- Ingredients ---
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.insertBefore(
                    new ItemStack(Items.DIAMOND),
                    new ItemStack(ModItems.RUBY.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
        }

        // --- Building blocks ---
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.insertBefore(
                    new ItemStack(Items.DIAMOND_BLOCK),
                    new ItemStack(ModBlocks.RUBY_BLOCK.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
        }

        // --- Natural blocks: ruby ore, then deepslate ruby ore ---
        if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
            event.insertBefore(
                    new ItemStack(Items.DIAMOND_ORE),
                    new ItemStack(ModBlocks.RUBY_ORE.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
        }

        if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
            event.insertAfter(
                    new ItemStack(ModBlocks.RUBY_ORE),
                    new ItemStack(ModBlocks.DEEPSLATE_RUBY_ORE.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
        }

        // --- Combat: weapons ---
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.insertBefore(
                    new ItemStack(Items.DIAMOND_SWORD),
                    new ItemStack(ModItems.RUBY_SWORD.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
        }

        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.insertBefore(
                    new ItemStack(Items.DIAMOND_AXE),
                    new ItemStack(ModItems.RUBY_AXE.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
        }

        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.insertBefore(
                    new ItemStack(Items.DIAMOND_SPEAR),
                    new ItemStack(ModItems.RUBY_SPEAR.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
        }

        // --- Tools and utilities: shovel -> pickaxe -> axe -> hoe ---
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.insertBefore(
                    new ItemStack(Items.DIAMOND_SHOVEL),
                    new ItemStack(ModItems.RUBY_SHOVEL.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
        }

        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.insertAfter(
                    new ItemStack((ItemLike) ModItems.RUBY_SHOVEL),
                    new ItemStack(ModItems.RUBY_PICKAXE.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
        }

        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.insertAfter(
                    new ItemStack((ItemLike) ModItems.RUBY_PICKAXE),
                    new ItemStack(ModItems.RUBY_AXE.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
        }

        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES){
            event.insertAfter(
                    new ItemStack((ItemLike) ModItems.RUBY_AXE),
                    new ItemStack(ModItems.RUBY_HOE.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
        }

        // --- Combat: armor ---
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            // Helmet right before the diamond one
            event.insertBefore(
                    new ItemStack(Items.DIAMOND_HELMET),
                    new ItemStack(ModItems.RUBY_HELMET.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
            // Chestplate after the ruby helmet
            event.insertAfter(
                    new ItemStack(ModItems.RUBY_HELMET.get()),
                    new ItemStack(ModItems.RUBY_CHESTPLATE.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
            // Leggings after the ruby chestplate
            event.insertAfter(
                    new ItemStack(ModItems.RUBY_CHESTPLATE.get()),
                    new ItemStack(ModItems.RUBY_LEGGINGS.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
            // Boots after the ruby leggings
            event.insertAfter(
                    new ItemStack(ModItems.RUBY_LEGGINGS.get()),
                    new ItemStack(ModItems.RUBY_BOOTS.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
        }

        // --- Combat: animal armor ---
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.insertBefore(
                    new ItemStack(Items.DIAMOND_HORSE_ARMOR),
                    new ItemStack(ModItems.RUBY_HORSE_ARMOR.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
        }

        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.insertBefore(
                    new ItemStack(Items.DIAMOND_NAUTILUS_ARMOR),
                    new ItemStack(ModItems.RUBY_NAUTILUS_ARMOR.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
        }

        // --- Colored blocks: hardened glass after tinted glass ---
        if (event.getTabKey() == CreativeModeTabs.COLORED_BLOCKS) {
            event.insertAfter(
                    new ItemStack(Items.TINTED_GLASS),
                    new ItemStack(ModBlocks.HARDENED_GLASS.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }
}