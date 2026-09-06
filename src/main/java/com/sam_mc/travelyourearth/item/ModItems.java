package com.sam_mc.travelyourearth.item;

import com.sam_mc.travelyourearth.TravelYourEarth;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.equipment.ArmorType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.resources.ResourceKey;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TravelYourEarth.MODID);

    public static final DeferredItem<Item> RUBY = ITEMS.registerItem("ruby",
            properties -> new Item(properties.trimMaterial(ModTrimMaterials.RUBY)));

    public static final DeferredItem<Item> RUBY_SWORD = ITEMS.registerItem("ruby_sword",
            properties -> new Item(properties.sword(ModToolTiers.RUBY, 3, -2.4f)));
    public static final DeferredItem<Item> RUBY_PICKAXE = ITEMS.registerItem("ruby_pickaxe",
            properties -> new Item(properties.pickaxe(ModToolTiers.RUBY, 1, -2.8f)));
    public static final DeferredItem<Item> RUBY_SHOVEL = ITEMS.registerItem("ruby_shovel",
            properties -> new ShovelItem(ModToolTiers.RUBY, 1.5f, -3.0f, properties));
    public static final DeferredItem<Item> RUBY_AXE = ITEMS.registerItem("ruby_axe",
            properties -> new AxeItem(ModToolTiers.RUBY, 6, -3.2f, properties));
    public static final DeferredItem<Item> RUBY_HOE = ITEMS.registerItem("ruby_hoe",
            properties -> new HoeItem(ModToolTiers.RUBY, 0, -3.0f, properties));
    public static final DeferredItem<Item> RUBY_SPEAR = ITEMS.registerItem("ruby_spear",
            properties -> new Item(properties.spear(ModToolTiers.RUBY, 0.95f, 0.7f, 0.7f,
                    3.5f, 13f, 8.5f, 5.1f, 13.37f, 4.67f)));



    public static final DeferredItem<Item> RUBY_HELMET = ITEMS.registerItem("ruby_helmet",
            properties -> new Item(properties.humanoidArmor(ModArmorMaterials.RUBY_ARMOR_MATERIAL, ArmorType.HELMET)));
    public static final DeferredItem<Item> RUBY_CHESTPLATE = ITEMS.registerItem("ruby_chestplate",
            properties -> new Item(properties.humanoidArmor(ModArmorMaterials.RUBY_ARMOR_MATERIAL, ArmorType.CHESTPLATE)));
    public static final DeferredItem<Item> RUBY_LEGGINGS = ITEMS.registerItem("ruby_leggings",
            properties -> new Item(properties.humanoidArmor(ModArmorMaterials.RUBY_ARMOR_MATERIAL, ArmorType.LEGGINGS)));
    public static final DeferredItem<Item> RUBY_BOOTS = ITEMS.registerItem("ruby_boots",
            properties -> new Item(properties.humanoidArmor(ModArmorMaterials.RUBY_ARMOR_MATERIAL, ArmorType.BOOTS)));
    public static final DeferredItem<Item> RUBY_HORSE_ARMOR = ITEMS.registerItem("ruby_horse_armor",
            properties -> new Item(properties.horseArmor(ModArmorMaterials.RUBY_ARMOR_MATERIAL)));
    public static final DeferredItem<Item> RUBY_NAUTILUS_ARMOR = ITEMS.registerItem("ruby_nautilus_armor",
            properties -> new Item(properties.nautilusArmor(ModArmorMaterials.RUBY_ARMOR_MATERIAL)));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}

