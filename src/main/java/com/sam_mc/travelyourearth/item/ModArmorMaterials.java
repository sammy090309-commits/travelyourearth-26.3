package com.sam_mc.travelyourearth.item;

import com.google.common.collect.Maps;
import com.sam_mc.travelyourearth.TravelYourEarth;
import com.sam_mc.travelyourearth.tags.ModTags;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;

import java.util.Map;

public class ModArmorMaterials {
    public static final ResourceKey<? extends Registry<EquipmentAsset>> ROOTID = ResourceKey.createRegistryKey(Identifier.withDefaultNamespace("equipment_asset"));

    public static final ResourceKey<EquipmentAsset> RUBY_KEY = ResourceKey.create(ROOTID, Identifier.fromNamespaceAndPath(TravelYourEarth.MODID, "ruby"));

    public static final ArmorMaterial RUBY_ARMOR_MATERIAL = new ArmorMaterial(1200,
            makeDefense(2, 6, 8, 2, 18), 18, SoundEvents.ARMOR_EQUIP_DIAMOND,
            2f, 0f, ModTags.Items.RUBY_REPAIRABLE, RUBY_KEY);

    private static Map<ArmorType, Integer> makeDefense(int boots, int legs, int chest, int helm, int body) {
        return Maps.newEnumMap(
                Map.of(ArmorType.BOOTS, boots, ArmorType.LEGGINGS, legs, ArmorType.CHESTPLATE, chest, ArmorType.HELMET, helm, ArmorType.BODY, body)
        );
    }


}
