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
 * Pestaña creativa "Travel Your Earth".
 *
 * La pestaña siempre se registra, pero si la config "showCreativeTab" está en false
 * no le metemos ningún ítem, y Minecraft esconde solo las pestañas vacías.
 * (Así no hay que tocar registros según la config, que daría problemas en servidores.)
 */
public class ModCreativeModTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TravelYourEarth.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TRAVEL_YOUR_EARTH_TAB =
            CREATIVE_MODE_TABS.register("travelyourearth_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.travelyourearth"))
                    .icon(() -> new ItemStack(ModBlocks.EARTH_BLOCK.get()))
                    .withTabsBefore(CreativeModeTabs.SPAWN_EGGS) // va después de las de vanilla
                    .displayItems((parameters, output) -> {
                        if (!isEnabled()) return; // pestaña vacía = oculta

                        // Materiales
                        output.accept(ModItems.RUBY.get());
                        output.accept(ModBlocks.RUBY_BLOCK.get());
                        output.accept(ModBlocks.RUBY_ORE.get());
                        output.accept(ModBlocks.DEEPSLATE_RUBY_ORE.get());
                        output.accept(ModBlocks.HARDENED_GLASS.get());

                        // Herramientas
                        output.accept(ModItems.RUBY_SHOVEL.get());
                        output.accept(ModItems.RUBY_PICKAXE.get());
                        output.accept(ModItems.RUBY_AXE.get());
                        output.accept(ModItems.RUBY_HOE.get());

                        // Armas
                        output.accept(ModItems.RUBY_SWORD.get());
                        output.accept(ModItems.RUBY_SPEAR.get());

                        // Armaduras
                        output.accept(ModItems.RUBY_HELMET.get());
                        output.accept(ModItems.RUBY_CHESTPLATE.get());
                        output.accept(ModItems.RUBY_LEGGINGS.get());
                        output.accept(ModItems.RUBY_BOOTS.get());
                        output.accept(ModItems.RUBY_HORSE_ARMOR.get());
                        output.accept(ModItems.RUBY_NAUTILUS_ARMOR.get());
                    })
                    .build());

    /** La config es del cliente: si no está cargada (por ejemplo en un servidor), la tratamos como activada. */
    private static boolean isEnabled() {
        return !Config.SPEC.isLoaded() || Config.SHOW_CREATIVE_TAB.get();
    }

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}