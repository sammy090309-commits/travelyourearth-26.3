package com.sam_mc.travelyourearth.tags;

import com.sam_mc.travelyourearth.TravelYourEarth;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Tag keys of the mod. Their contents are generated in ModBlockTagsProvider, ModItemTagsProvider,
 * ModStructureTagsProvider, ModEntityTypeTagsProvider, ModEnchantmentTagsProvider and ModBannerPatternTagsProvider.
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

    // =========================================================================
    // Structures
    // =========================================================================
    public static class Structures {

        /** Structures the Trail Ruins explorer map can point to. */
        public static final TagKey<Structure> ON_TRAIL_RUINS_MAPS = createTag("on_trail_ruins_maps");

        /** Structures the illegal cartographer's Ancient City map can point to. */
        public static final TagKey<Structure> ON_ANCIENT_CITY_MAPS = createTag("on_ancient_city_maps");

        private static TagKey<Structure> createTag(String name) {
            return TagKey.create(Registries.STRUCTURE, Identifier.fromNamespaceAndPath(TravelYourEarth.MODID, name));
        }
    }

    // =========================================================================
    // Entity types
    // =========================================================================
    public static class EntityTypes {

        /**
         * Mobs that take extra damage from Banishment: the whole "big nose" family
         * (villagers, illagers, witches...) plus the ravager.
         */
        public static final TagKey<EntityType<?>> SENSITIVE_TO_BANISHMENT = createTag("sensitive_to_banishment");

        // ---- Mobs in decorated pots (1.2.1) ----
        // How many mobs of each type fit in one pot (creative: sneak + spawn egg on a pot).
        // Not in any tag = 1. In more than one tag = the LOWEST number wins.
        public static final TagKey<EntityType<?>> POT_CAPACITY_8 = createTag("pot_capacity/8");
        public static final TagKey<EntityType<?>> POT_CAPACITY_4 = createTag("pot_capacity/4");
        public static final TagKey<EntityType<?>> POT_CAPACITY_2 = createTag("pot_capacity/2");
        public static final TagKey<EntityType<?>> POT_CAPACITY_1 = createTag("pot_capacity/1");
        /** Mobs that can never go into a pot (Wither, Ender Dragon...). */
        public static final TagKey<EntityType<?>> CANNOT_ENTER_POT = createTag("cannot_enter_pot");

        private static TagKey<EntityType<?>> createTag(String name) {
            return TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(TravelYourEarth.MODID, name));
        }
    }

    // =========================================================================
    // Enchantments
    // =========================================================================
    public static class Enchantments {

        /** Enchantments that can't be combined with Harpoon (Knockback: one pushes, the other pulls). */
        public static final TagKey<Enchantment> HARPOON_EXCLUSIVE = createTag("exclusive_set/harpoon");

        private static TagKey<Enchantment> createTag(String name) {
            return TagKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath(TravelYourEarth.MODID, name));
        }
    }

    // =========================================================================
    // Banner patterns
    // =========================================================================
    public static class BannerPatterns {

        /** Patterns unlocked in the loom by the Rebellion Banner Pattern item (like #minecraft:pattern_item/flow). */
        public static final TagKey<BannerPattern> PATTERN_ITEM_REBELLION = createTag("pattern_item/rebellion");

        private static TagKey<BannerPattern> createTag(String name) {
            return TagKey.create(Registries.BANNER_PATTERN, Identifier.fromNamespaceAndPath(TravelYourEarth.MODID, name));
        }
    }
}