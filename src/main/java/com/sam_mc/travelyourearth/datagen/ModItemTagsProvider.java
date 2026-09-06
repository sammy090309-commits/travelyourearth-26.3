package com.sam_mc.travelyourearth.datagen;

import com.sam_mc.travelyourearth.TravelYourEarth;
import com.sam_mc.travelyourearth.block.ModBlocks;
import com.sam_mc.travelyourearth.item.ModItems;
import com.sam_mc.travelyourearth.tags.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.ItemTags;
import net.neoforged.neoforge.common.data.ItemTagsProvider;

import java.util.concurrent.CompletableFuture;

public class ModItemTagsProvider extends ItemTagsProvider {
    public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, TravelYourEarth.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {

        tag(ModTags.Items.RUBY_REPAIRABLE)
                .add(ModItems.RUBY.getKey());


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
        tag(ItemTags.SULFUR_CUBE_SWALLOWABLE).add(ModItems.RUBY.getKey());

        tag(ItemTags.SULFUR_CUBE_ARCHETYPE_SLOW_BOUNCY)
                .add(ModBlocks.RUBY_BLOCK.get().asItem().builtInRegistryHolder().key())
;

        tag(ItemTags.SULFUR_CUBE_SWALLOWABLE)
                .add(ModBlocks.RUBY_BLOCK.get().asItem().builtInRegistryHolder().key())
                .add(ModBlocks.RUBY_ORE.get().asItem().builtInRegistryHolder().key())
                .add(ModBlocks.DEEPSLATE_RUBY_ORE.get().asItem().builtInRegistryHolder().key());

    }


}
