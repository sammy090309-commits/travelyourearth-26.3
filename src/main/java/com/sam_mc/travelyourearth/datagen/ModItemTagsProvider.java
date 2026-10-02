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

/** Generates the item tags of the mod (mod tags + vanilla tags + common "c:" tags). */
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
        // (NeoForge puts these vanilla tags inside c:tools and c:armors/humanoid automatically)
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

        // The ruby pickaxe gets the maximum shards from amethyst clusters (like the diamond one)
        tag(ItemTags.CLUSTER_MAX_HARVESTABLES).add(ModItems.RUBY_PICKAXE.getKey());

        // Item version of #minecraft:ores (like the diamond ores)
        tag(BlockItemTags.ORES.item())
                .add(ModBlocks.RUBY_ORE.get().asItem().builtInRegistryHolder().key())
                .add(ModBlocks.DEEPSLATE_RUBY_ORE.get().asItem().builtInRegistryHolder().key());

        // Sulfur cube: same as diamond.
        // The block and the ores "bounce slowly". This already makes them swallowable,
        // because vanilla joins every archetype in sulfur_cube_swallowable.
        // The ruby (gem) can NOT be swallowed, same as the diamond.
        tag(ItemTags.SULFUR_CUBE_ARCHETYPE_SLOW_BOUNCY)
                .add(ModBlocks.RUBY_BLOCK.get().asItem().builtInRegistryHolder().key())
                .add(ModBlocks.RUBY_ORE.get().asItem().builtInRegistryHolder().key())
                .add(ModBlocks.DEEPSLATE_RUBY_ORE.get().asItem().builtInRegistryHolder().key());

        // =====================================================================
        // COMMON (c:) -> compatibility with other mods
        // =====================================================================

        // c:gems/ruby  ->  inside c:gems
        tag(ModTags.Items.GEMS_RUBY)
                .add(ModItems.RUBY.getKey());
        tag(Tags.Items.GEMS)
                .addTag(ModTags.Items.GEMS_RUBY);

        // c:ores/ruby  ->  inside c:ores
        tag(ModTags.Items.ORES_RUBY)
                .add(ModBlocks.RUBY_ORE.get().asItem().builtInRegistryHolder().key())
                .add(ModBlocks.DEEPSLATE_RUBY_ORE.get().asItem().builtInRegistryHolder().key());
        tag(Tags.Items.ORES)
                .addTag(ModTags.Items.ORES_RUBY);

        // Which stone each ore is in
        tag(Tags.Items.ORES_IN_GROUND_STONE)
                .add(ModBlocks.RUBY_ORE.get().asItem().builtInRegistryHolder().key());
        tag(Tags.Items.ORES_IN_GROUND_DEEPSLATE)
                .add(ModBlocks.DEEPSLATE_RUBY_ORE.get().asItem().builtInRegistryHolder().key());

        // They drop 1 item, like the diamond ores
        tag(Tags.Items.ORE_RATES_SINGULAR)
                .add(ModBlocks.RUBY_ORE.get().asItem().builtInRegistryHolder().key())
                .add(ModBlocks.DEEPSLATE_RUBY_ORE.get().asItem().builtInRegistryHolder().key());

        // c:storage_blocks/ruby  ->  inside c:storage_blocks
        tag(ModTags.Items.STORAGE_BLOCKS_RUBY)
                .add(ModBlocks.RUBY_BLOCK.get().asItem().builtInRegistryHolder().key());
        tag(Tags.Items.STORAGE_BLOCKS)
                .addTag(ModTags.Items.STORAGE_BLOCKS_RUBY);

        // Tools: NeoForge only puts the vanilla ones here, so we add ours
        // (same as the diamond pickaxe / sword / axe / spear)
        tag(Tags.Items.MINING_TOOL_TOOLS)
                .add(ModItems.RUBY_PICKAXE.getKey());
        tag(Tags.Items.MELEE_WEAPON_TOOLS)
                .add(ModItems.RUBY_SWORD.getKey())
                .add(ModItems.RUBY_AXE.getKey())
                .add(ModItems.RUBY_SPEAR.getKey());

        // Animal armor (player armor already gets in through the vanilla tags)
        tag(Tags.Items.ARMORS_HORSE)
                .add(ModItems.RUBY_HORSE_ARMOR.getKey());
        tag(Tags.Items.ARMORS_NAUTILUS)
                .add(ModItems.RUBY_NAUTILUS_ARMOR.getKey());
    }
}