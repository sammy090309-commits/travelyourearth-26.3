package com.sam_mc.travelyourearth.item;

import com.sam_mc.travelyourearth.TravelYourEarth;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.trim.TrimMaterial;

public class ModTrimMaterials {
    public static final ResourceKey<TrimMaterial> RUBY =
            ResourceKey.create(Registries.TRIM_MATERIAL, Identifier.fromNamespaceAndPath(TravelYourEarth.MODID, "ruby"));


}