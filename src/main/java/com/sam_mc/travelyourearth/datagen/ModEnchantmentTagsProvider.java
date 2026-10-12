package com.sam_mc.travelyourearth.datagen;

import com.sam_mc.travelyourearth.TravelYourEarth;
import com.sam_mc.travelyourearth.enchantment.ModEnchantments;
import com.sam_mc.travelyourearth.tags.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.concurrent.CompletableFuture;

/** Generates the enchantment tags of the mod. */
public class ModEnchantmentTagsProvider extends TagsProvider<Enchantment> {

    public ModEnchantmentTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, Registries.ENCHANTMENT, lookupProvider, TravelYourEarth.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        // Banishment joins the vanilla "damage" group (Sharpness, Smite, Bane of Arthropods...):
        // so those enchantments can't be combined with it either (both ways), like vanilla Smite.
        // It's NOT added to #minecraft:in_enchanting_table / on_random_loot / tradeable:
        // only the illegal librarian sells it.
        getOrCreateRawBuilder(EnchantmentTags.DAMAGE_EXCLUSIVE)
                .addElement(ModEnchantments.BANISHMENT.identifier());

        // Harpoon can't be combined with Knockback
        getOrCreateRawBuilder(ModTags.Enchantments.HARPOON_EXCLUSIVE)
                .addElement(Identifier.withDefaultNamespace("knockback"));
    }
}