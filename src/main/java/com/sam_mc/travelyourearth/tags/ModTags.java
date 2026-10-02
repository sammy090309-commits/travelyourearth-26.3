package com.sam_mc.travelyourearth.tags;

import com.sam_mc.travelyourearth.TravelYourEarth;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * Tag keys of the mod. Their contents are generated in ModBlockTagsProvider and ModItemTagsProvider.
 */
public class ModTags {

    // =========================================================================
    // Blocks
    // =========================================================================
    public static class Blocks {

        // ---- Mod tags (travelyourearth:...) ----
        public static final TagKey<Block> NEEDS_RUBY_TOOL = createTag("needs_ruby_tool");
        public static final TagKey<Block> INCORRECT_FOR_RUBY_TOOL = createTag("incorrect_for_ruby_tool");

        // ---- Common tags (c:...) -> compatibility with other mods ----
        // The "parent" tags (c:ores, c:storage_blocks...) already exist in NeoForge: Tags.Blocks.*
        public static final TagKey<Block> ORES_RUBY = commonTag("ores/ruby");
        public static final TagKey<Block> STORAGE_BLOCKS_RUBY = commonTag("storage_blocks/ruby");

        private static TagKey<Block> createTag(String name) {
            return BlockTags.create(Identifier.fromNamespaceAndPath(TravelYourEarth.MODID, name));
        }

        private static TagKey<Block> commonTag(String name) {
            return BlockTags.create(Identifier.fromNamespaceAndPath("c", name));
        }
    }

    // =========================================================================
    // Items
    // =========================================================================
    public static class Items {

        // ---- Mod tags (travelyourearth:...) ----
        public static final TagKey<Item> RUBY_REPAIRABLE = createTag("ruby_repairable");

        // ---- Common tags (c:...) -> compatibility with other mods ----
        public static final TagKey<Item> GEMS_RUBY = commonTag("gems/ruby");
        public static final TagKey<Item> ORES_RUBY = commonTag("ores/ruby");
        public static final TagKey<Item> STORAGE_BLOCKS_RUBY = commonTag("storage_blocks/ruby");

        private static TagKey<Item> createTag(String name) {
            return ItemTags.create(Identifier.fromNamespaceAndPath(TravelYourEarth.MODID, name));
        }

        private static TagKey<Item> commonTag(String name) {
            return ItemTags.create(Identifier.fromNamespaceAndPath("c", name));
        }
    }
}