package com.sam_mc.travelyourearth.datagen;

import com.sam_mc.travelyourearth.TravelYourEarth;
import com.sam_mc.travelyourearth.tags.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

import java.util.concurrent.CompletableFuture;

/** Generates the entity type tags of the mod. */
public class ModEntityTypeTagsProvider extends TagsProvider<EntityType<?>> {

    public ModEntityTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, Registries.ENTITY_TYPE, lookupProvider, TravelYourEarth.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        // Banishment: the whole "big nose" family + the ravager
        getOrCreateRawBuilder(ModTags.EntityTypes.SENSITIVE_TO_BANISHMENT)
                // #minecraft:raiders = pillager, vindicator, evoker, illusioner, ravager and witch
                .addTag(Identifier.withDefaultNamespace("raiders"))
                .addElement(Identifier.withDefaultNamespace("villager"))
                .addElement(Identifier.withDefaultNamespace("wandering_trader"))
                .addElement(Identifier.withDefaultNamespace("zombie_villager"));
    }
}