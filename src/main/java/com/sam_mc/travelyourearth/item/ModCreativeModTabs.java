package com.sam_mc.travelyourearth.item;

import com.sam_mc.travelyourearth.Config;
import com.sam_mc.travelyourearth.TravelYourEarth;
import com.sam_mc.travelyourearth.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * "Travel Your Earth" creative tab.
 *
 * The tab is always registered, but if the "showCreativeTab" config is false
 * we don't put any item in it, and Minecraft hides empty tabs by itself.
 * (This way we don't change registries based on the config, which would cause problems on servers.)
 */
public class ModCreativeModTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TravelYourEarth.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TRAVEL_YOUR_EARTH_TAB =
            CREATIVE_MODE_TABS.register("travelyourearth_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.travelyourearth"))
                    .icon(() -> new ItemStack(ModBlocks.EARTH_BLOCK.get()))
                    .withTabsBefore(CreativeModeTabs.SPAWN_EGGS) // goes after the vanilla tabs
                    .displayItems((parameters, output) -> {
                        if (!isEnabled()) return; // empty tab = hidden

                        // Materials
                        output.accept(ModItems.RUBY.get());
                        output.accept(ModBlocks.RUBY_BLOCK.get());
                        output.accept(ModBlocks.RUBY_ORE.get());
                        output.accept(ModBlocks.DEEPSLATE_RUBY_ORE.get());
                        output.accept(ModBlocks.HARDENED_GLASS.get());

                        // Tools
                        output.accept(ModItems.RUBY_SHOVEL.get());
                        output.accept(ModItems.RUBY_PICKAXE.get());
                        output.accept(ModItems.RUBY_AXE.get());
                        output.accept(ModItems.RUBY_HOE.get());

                        // Weapons
                        output.accept(ModItems.RUBY_SWORD.get());
                        output.accept(ModItems.RUBY_SPEAR.get());

                        // Armor
                        output.accept(ModItems.RUBY_HELMET.get());
                        output.accept(ModItems.RUBY_CHESTPLATE.get());
                        output.accept(ModItems.RUBY_LEGGINGS.get());
                        output.accept(ModItems.RUBY_BOOTS.get());
                        output.accept(ModItems.RUBY_HORSE_ARMOR.get());
                        output.accept(ModItems.RUBY_NAUTILUS_ARMOR.get());
                    })
                    .build());

    /** The config is client-side: if it isn't loaded (for example on a server), we treat it as enabled. */
    private static boolean isEnabled() {
        return !Config.SPEC.isLoaded() || Config.SHOW_CREATIVE_TAB.get();
    }

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}