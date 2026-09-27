package com.sam_mc.travelyourearth.datagen;

import com.sam_mc.travelyourearth.TravelYourEarth;
import com.sam_mc.travelyourearth.block.ModBlocks;
import com.sam_mc.travelyourearth.item.ModItems;
import com.sam_mc.travelyourearth.tags.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockItemTags;
import net.minecraft.tags.ItemTags;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ItemTagsProvider;

import java.util.concurrent.CompletableFuture;

public class ModItemTagsProvider extends ItemTagsProvider {
    public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, TravelYourEarth.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {

        // =====================================================================
        // MOD
        // =====================================================================
        tag(ModTags.Items.RUBY_REPAIRABLE)
                .add(ModItems.RUBY.getKey());

        // =====================================================================
        // MINECRAFT
        // =====================================================================
        // (NeoForge mete estos tags vanilla dentro de c:tools y c:armors/humanoid automáticamente)
        tag(ItemTags.SWORDS).add(ModItems.RUBY_SWORD.getKey());
        tag(ItemTags.PICKAXES).add(ModItems.RUBY_PICKAXE.getKey());
        tag(ItemTags.SHOVELS).add(ModItems.RUBY_SHOVEL.getKey());
        tag(ItemTags.AXES).add(ModItems.RUBY_AXE.getKey());
        tag(ItemTags.HOES).add(ModItems.RUBY_HOE.getKey());
        tag(ItemTags.SPEARS).add(ModItems.RUBY_SPEAR.getKey());

        tag(ItemTags.HEAD_ARMOR).add(ModItems.RUBY_HELMET.getKey());
        tag(ItemTags.CHEST_ARMOR).add(ModItems.RUBY_CHESTPLATE.getKey());
        tag(ItemTags.LEG_ARMOR).add(ModItems.RUBY_LEGGINGS.getKey());
        tag(ItemTags.FOOT_ARMOR).add(ModItems.RUBY_BOOTS.getKey());

        tag(ItemTags.TRIM_MATERIALS).add(ModItems.RUBY.getKey());
        tag(ItemTags.BEACON_PAYMENT_ITEMS).add(ModItems.RUBY.getKey());

        // El pico de rubí saca el máximo de fragmentos de los cúmulos de amatista (como el de diamante)
        tag(ItemTags.CLUSTER_MAX_HARVESTABLES).add(ModItems.RUBY_PICKAXE.getKey());

        // Versión ítem de #minecraft:ores (como las menas de diamante)
        tag(BlockItemTags.ORES.item())
                .add(ModBlocks.RUBY_ORE.get().asItem().builtInRegistryHolder().key())
                .add(ModBlocks.DEEPSLATE_RUBY_ORE.get().asItem().builtInRegistryHolder().key());

        // Cubo de azufre igual que el diamante.
        // El bloque y las menas "rebotan lento". Esto ya los hace tragables solos,
        // porque vanilla junta todos los arquetipos en sulfur_cube_swallowable.
        // El rubí (gema) NO se puede tragar, igual que el diamante.
        tag(ItemTags.SULFUR_CUBE_ARCHETYPE_SLOW_BOUNCY)
                .add(ModBlocks.RUBY_BLOCK.get().asItem().builtInRegistryHolder().key())
                .add(ModBlocks.RUBY_ORE.get().asItem().builtInRegistryHolder().key())
                .add(ModBlocks.DEEPSLATE_RUBY_ORE.get().asItem().builtInRegistryHolder().key());

        // =====================================================================
        // COMUNES (c:) -> compatibilidad con otros mods
        // =====================================================================

        // c:gems/ruby  ->  dentro de c:gems
        tag(ModTags.Items.GEMS_RUBY)
                .add(ModItems.RUBY.getKey());
        tag(Tags.Items.GEMS)
                .addTag(ModTags.Items.GEMS_RUBY);

        // c:ores/ruby  ->  dentro de c:ores
        tag(ModTags.Items.ORES_RUBY)
                .add(ModBlocks.RUBY_ORE.get().asItem().builtInRegistryHolder().key())
                .add(ModBlocks.DEEPSLATE_RUBY_ORE.get().asItem().builtInRegistryHolder().key());
        tag(Tags.Items.ORES)
                .addTag(ModTags.Items.ORES_RUBY);

        // En qué piedra está cada mena
        tag(Tags.Items.ORES_IN_GROUND_STONE)
                .add(ModBlocks.RUBY_ORE.get().asItem().builtInRegistryHolder().key());
        tag(Tags.Items.ORES_IN_GROUND_DEEPSLATE)
                .add(ModBlocks.DEEPSLATE_RUBY_ORE.get().asItem().builtInRegistryHolder().key());

        // Sueltan 1 ítem, como las menas de diamante
        tag(Tags.Items.ORE_RATES_SINGULAR)
                .add(ModBlocks.RUBY_ORE.get().asItem().builtInRegistryHolder().key())
                .add(ModBlocks.DEEPSLATE_RUBY_ORE.get().asItem().builtInRegistryHolder().key());

        // c:storage_blocks/ruby  ->  dentro de c:storage_blocks
        tag(ModTags.Items.STORAGE_BLOCKS_RUBY)
                .add(ModBlocks.RUBY_BLOCK.get().asItem().builtInRegistryHolder().key());
        tag(Tags.Items.STORAGE_BLOCKS)
                .addTag(ModTags.Items.STORAGE_BLOCKS_RUBY);

        // Herramientas: NeoForge solo mete aquí las vanilla, así que añadimos las nuestras
        // (igual que el pico / espada / hacha / lanza de diamante)
        tag(Tags.Items.MINING_TOOL_TOOLS)
                .add(ModItems.RUBY_PICKAXE.getKey());
        tag(Tags.Items.MELEE_WEAPON_TOOLS)
                .add(ModItems.RUBY_SWORD.getKey())
                .add(ModItems.RUBY_AXE.getKey())
                .add(ModItems.RUBY_SPEAR.getKey());

        // Armaduras de animales (las de jugador ya entran solas por los tags vanilla)
        tag(Tags.Items.ARMORS_HORSE)
                .add(ModItems.RUBY_HORSE_ARMOR.getKey());
        tag(Tags.Items.ARMORS_NAUTILUS)
                .add(ModItems.RUBY_NAUTILUS_ARMOR.getKey());
    }
}