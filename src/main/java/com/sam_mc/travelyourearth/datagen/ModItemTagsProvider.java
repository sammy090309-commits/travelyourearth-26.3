package com.sam_mc.travelyourearth.datagen;

import com.sam_mc.travelyourearth.TravelYourEarth;
import com.sam_mc.travelyourearth.block.ModBlocks;
import com.sam_mc.travelyourearth.item.ModItems;
import com.sam_mc.travelyourearth.tags.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockItemTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ItemTagsProvider;

import java.util.concurrent.CompletableFuture;

/**
 * Generates the item tags of the mod (mod tags + vanilla tags + common "c:" tags).
 *
 * 26.3: uses the "raw builder" (getOrCreateRawBuilder + addElement), the same way as
 * ModEnchantmentTagsProvider. It writes the item ids directly, so it doesn't depend on
 * what tag(...).add(...) expects in this NeoForge version.
 */
public class ModItemTagsProvider extends ItemTagsProvider {

    public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, TravelYourEarth.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {

        // =====================================================================
        // MOD
        // =====================================================================
        add(ModTags.Items.RUBY_REPAIRABLE, ModItems.RUBY.get());

        // =====================================================================
        // MINECRAFT
        // =====================================================================
        // (NeoForge puts these vanilla tags inside c:tools and c:armors/humanoid automatically)
        add(ItemTags.SWORDS, ModItems.RUBY_SWORD.get());
        add(ItemTags.PICKAXES, ModItems.RUBY_PICKAXE.get());
        add(ItemTags.SHOVELS, ModItems.RUBY_SHOVEL.get());
        add(ItemTags.AXES, ModItems.RUBY_AXE.get());
        add(ItemTags.HOES, ModItems.RUBY_HOE.get());
        add(ItemTags.SPEARS, ModItems.RUBY_SPEAR.get());

        add(ItemTags.HEAD_ARMOR, ModItems.RUBY_HELMET.get());
        add(ItemTags.CHEST_ARMOR, ModItems.RUBY_CHESTPLATE.get());
        add(ItemTags.LEG_ARMOR, ModItems.RUBY_LEGGINGS.get());
        add(ItemTags.FOOT_ARMOR, ModItems.RUBY_BOOTS.get());

        add(ItemTags.TRIM_MATERIALS, ModItems.RUBY.get());
        add(ItemTags.BEACON_PAYMENT_ITEMS, ModItems.RUBY.get());

        // The ruby pickaxe gets the maximum shards from amethyst clusters (like the diamond one)
        add(ItemTags.CLUSTER_MAX_HARVESTABLES, ModItems.RUBY_PICKAXE.get());

        // The loom only accepts patterns that are in #minecraft:loom_patterns (26.3)
        add(ItemTags.LOOM_PATTERNS, ModItems.REBELLION_BANNER_PATTERN.get());

        // Item version of #minecraft:ores (like the diamond ores)
        add(BlockItemTags.ORES.item(), ModBlocks.RUBY_ORE.get(), ModBlocks.DEEPSLATE_RUBY_ORE.get());

        // Sulfur cube: same as diamond.
        // The block and the ores "bounce slowly". This already makes them swallowable,
        // because vanilla joins every archetype in sulfur_cube_swallowable.
        // The ruby (gem) can NOT be swallowed, same as the diamond.
        add(ItemTags.SULFUR_CUBE_ARCHETYPE_SLOW_BOUNCY,
                ModBlocks.RUBY_BLOCK.get(), ModBlocks.RUBY_ORE.get(), ModBlocks.DEEPSLATE_RUBY_ORE.get());

        // =====================================================================
        // COMMON (c:) -> compatibility with other mods
        // =====================================================================

        // c:gems/ruby  ->  inside c:gems
        add(ModTags.Items.GEMS_RUBY, ModItems.RUBY.get());
        addTag(Tags.Items.GEMS, ModTags.Items.GEMS_RUBY);

        // c:ores/ruby  ->  inside c:ores
        add(ModTags.Items.ORES_RUBY, ModBlocks.RUBY_ORE.get(), ModBlocks.DEEPSLATE_RUBY_ORE.get());
        addTag(Tags.Items.ORES, ModTags.Items.ORES_RUBY);

        // Which stone each ore is in
        add(Tags.Items.ORES_IN_GROUND_STONE, ModBlocks.RUBY_ORE.get());
        add(Tags.Items.ORES_IN_GROUND_DEEPSLATE, ModBlocks.DEEPSLATE_RUBY_ORE.get());

        // They drop 1 item, like the diamond ores
        add(Tags.Items.ORE_RATES_SINGULAR, ModBlocks.RUBY_ORE.get(), ModBlocks.DEEPSLATE_RUBY_ORE.get());

        // c:storage_blocks/ruby  ->  inside c:storage_blocks
        add(ModTags.Items.STORAGE_BLOCKS_RUBY, ModBlocks.RUBY_BLOCK.get());
        addTag(Tags.Items.STORAGE_BLOCKS, ModTags.Items.STORAGE_BLOCKS_RUBY);

        // Tools: NeoForge only puts the vanilla ones here, so we add ours
        // (same as the diamond pickaxe / sword / axe / spear)
        add(Tags.Items.MINING_TOOL_TOOLS, ModItems.RUBY_PICKAXE.get());
        add(Tags.Items.MELEE_WEAPON_TOOLS, ModItems.RUBY_SWORD.get(), ModItems.RUBY_AXE.get(), ModItems.RUBY_SPEAR.get());

        // Animal armor (player armor already gets in through the vanilla tags)
        add(Tags.Items.ARMORS_HORSE, ModItems.RUBY_HORSE_ARMOR.get());
        add(Tags.Items.ARMORS_NAUTILUS, ModItems.RUBY_NAUTILUS_ARMOR.get());
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    /** Adds items (or block items) to a tag, by their id (e.g. travelyourearth:ruby). */
    private void add(TagKey<Item> tag, ItemLike... items) {
        TagBuilder builder = getOrCreateRawBuilder(tag);
        for (ItemLike item : items) {
            builder.addElement(BuiltInRegistries.ITEM.getKey(item.asItem()));
        }
    }

    /** Puts a whole tag inside another one (e.g. c:gems/ruby inside c:gems). */
    private void addTag(TagKey<Item> tag, TagKey<Item> child) {
        getOrCreateRawBuilder(tag).addTag(child.location());
    }
}