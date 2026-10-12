package com.sam_mc.travelyourearth.item;

import com.sam_mc.travelyourearth.TravelYourEarth;
import com.sam_mc.travelyourearth.tags.ModTags;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.MapDecorations;
import net.minecraft.world.item.equipment.ArmorType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * All items added by Travel Your Earth.
 * (Block items are registered automatically in ModBlocks.)
 */
public class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TravelYourEarth.MODID);

    // =========================================================================
    // Materials
    // =========================================================================

    /** Ruby: also works as a trim material in the smithing table. */
    public static final DeferredItem<Item> RUBY = ITEMS.registerItem("ruby",
            properties -> new Item(properties.trimMaterial(ModTrimMaterials.RUBY)));

    // =========================================================================
    // Tools and weapons
    // =========================================================================

    // Sword, pickaxe and shovel: same parameters as vanilla (ModToolTiers makes the difference)
    public static final DeferredItem<Item> RUBY_SWORD = ITEMS.registerItem("ruby_sword",
            properties -> new Item(properties.sword(ModToolTiers.RUBY, 3, -2.4f)));
    public static final DeferredItem<Item> RUBY_PICKAXE = ITEMS.registerItem("ruby_pickaxe",
            properties -> new Item(properties.pickaxe(ModToolTiers.RUBY, 1, -2.8f)));
    public static final DeferredItem<Item> RUBY_SHOVEL = ITEMS.registerItem("ruby_shovel",
            properties -> new Item(properties.shovel(ModToolTiers.RUBY, 1.5f, -3.0f)));

    // Between iron (6, -3.1) and diamond (5, -3.0)
    public static final DeferredItem<Item> RUBY_AXE = ITEMS.registerItem("ruby_axe",
            properties -> new Item(properties.axe(ModToolTiers.RUBY, 5.5f, -3.05f)));

    // Between iron (-2, -1) and diamond (-3, 0)
    public static final DeferredItem<Item> RUBY_HOE = ITEMS.registerItem("ruby_hoe",
            properties -> new Item(properties.hoe(ModToolTiers.RUBY, -2.5f, -0.5f)));

    // Between the iron and the diamond spear.
    // knockbackThreshold (5.1) and damageThreshold (4.6) are fixed in vanilla.
    public static final DeferredItem<Item> RUBY_SPEAR = ITEMS.registerItem("ruby_spear",
            properties -> new Item(properties.spear(ModToolTiers.RUBY, 1.0f, 1.01f, 0.55f,
                    2.75f, 10.5f, 6.63f, 5.1f, 10.63f, 4.6f)));

    // =========================================================================
    // Armor (stats in ModArmorMaterials)
    // =========================================================================

    public static final DeferredItem<Item> RUBY_HELMET = ITEMS.registerItem("ruby_helmet",
            properties -> new Item(properties.humanoidArmor(ModArmorMaterials.RUBY_ARMOR_MATERIAL, ArmorType.HELMET)));
    public static final DeferredItem<Item> RUBY_CHESTPLATE = ITEMS.registerItem("ruby_chestplate",
            properties -> new Item(properties.humanoidArmor(ModArmorMaterials.RUBY_ARMOR_MATERIAL, ArmorType.CHESTPLATE)));
    public static final DeferredItem<Item> RUBY_LEGGINGS = ITEMS.registerItem("ruby_leggings",
            properties -> new Item(properties.humanoidArmor(ModArmorMaterials.RUBY_ARMOR_MATERIAL, ArmorType.LEGGINGS)));
    public static final DeferredItem<Item> RUBY_BOOTS = ITEMS.registerItem("ruby_boots",
            properties -> new Item(properties.humanoidArmor(ModArmorMaterials.RUBY_ARMOR_MATERIAL, ArmorType.BOOTS)));

    // Animal armor
    public static final DeferredItem<Item> RUBY_HORSE_ARMOR = ITEMS.registerItem("ruby_horse_armor",
            properties -> new Item(properties.horseArmor(ModArmorMaterials.RUBY_ARMOR_MATERIAL)));
    public static final DeferredItem<Item> RUBY_NAUTILUS_ARMOR = ITEMS.registerItem("ruby_nautilus_armor",
            properties -> new Item(properties.nautilusArmor(ModArmorMaterials.RUBY_ARMOR_MATERIAL)));

    // =========================================================================
    // Maps (1.2)
    // =========================================================================

    /**
     * Trail Ruins explorer map, sold by the illegal cartographer.
     * Registered like vanilla 26.3 explorer maps (MapItem + empty MAP_DECORATIONS).
     * "buried_" like vanilla's buried_trial_chambers_map / buried_ancient_city_map:
     * the trail ruins are underground.
     * Not in #minecraft:extendable_maps -> can't be zoomed out with paper (like vanilla explorer maps).
     */
    public static final DeferredItem<Item> BURIED_TRAIL_RUINS_MAP = ITEMS.registerItem("buried_trail_ruins_map",
            properties -> new MapItem(properties.component(DataComponents.MAP_DECORATIONS, MapDecorations.EMPTY)));

    // =========================================================================
    // Banner patterns (1.2)
    // =========================================================================

    /**
     * Rebellion Banner Pattern: put it in the loom to stamp the crossed axe + hoe + star.
     * Same as vanilla flow/guster patterns (stack of 1, rare). Sold by the illegal shepherd.
     */
    public static final DeferredItem<Item> REBELLION_BANNER_PATTERN = ITEMS.registerItem("rebellion_banner_pattern",
            properties -> new Item(properties.stacksTo(1).rarity(Rarity.RARE)
                    // 26.3: delayedComponent, like vanilla flow/guster. The patterns are data that loads
                    // AFTER the items, so the tag is looked up later through the context.
                    .delayedComponent(DataComponents.PROVIDES_BANNER_PATTERNS,
                            context -> context.getOrThrow(ModTags.BannerPatterns.PATTERN_ITEM_REBELLION))));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}