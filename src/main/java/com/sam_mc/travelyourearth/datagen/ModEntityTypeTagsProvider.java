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

        // =====================================================================
        // Mobs in decorated pots (1.2.1): how many fit in one pot.
        // Not in any tag = 1 (default). In two tags = the lowest number wins.
        // Other mods / datapacks can add their own mobs to these tags.
        // =====================================================================
        vanilla(ModTags.EntityTypes.POT_CAPACITY_8,
                "bat", "silverfish", "endermite", "bee", "vex", "allay",
                "cod", "salmon", "tropical_fish", "pufferfish", "tadpole");

        vanilla(ModTags.EntityTypes.POT_CAPACITY_4,
                "chicken", "rabbit", "fox", "cat", "ocelot", "parrot", "frog", "axolotl",
                "armadillo", "turtle", "squid", "glow_squid", "cave_spider", "slime", "magma_cube",
                "villager", "copper_golem", "sulfur_cube");

        vanilla(ModTags.EntityTypes.POT_CAPACITY_2,
                // animals
                "cow", "mooshroom", "pig", "sheep", "goat", "wolf", "llama", "trader_llama", "panda",
                "polar_bear", "dolphin", "strider", "nautilus",
                // monsters
                "zombie", "husk", "drowned", "zombie_villager", "zombified_piglin",
                "skeleton", "stray", "bogged", "parched", "wither_skeleton",
                "spider", "creeper", "enderman", "blaze", "breeze", "guardian", "phantom",
                "witch", "pillager", "vindicator", "evoker", "illusioner",
                "piglin", "piglin_brute", "hoglin", "zoglin", "creaking", "zombie_nautilus",
                "wandering_trader");

        vanilla(ModTags.EntityTypes.POT_CAPACITY_1,
                "ravager", "iron_golem", "snow_golem", "warden", "elder_guardian", "shulker",
                "horse", "donkey", "mule", "skeleton_horse", "zombie_horse", "camel", "camel_husk",
                "sniffer", "ghast", "happy_ghast", "giant");

        vanilla(ModTags.EntityTypes.CANNOT_ENTER_POT,
                "wither", "ender_dragon");
    }

    /** Adds vanilla mobs (minecraft:...) to a tag. */
    private void vanilla(net.minecraft.tags.TagKey<EntityType<?>> tag, String... ids) {
        var builder = getOrCreateRawBuilder(tag);
        for (String id : ids) {
            builder.addElement(Identifier.withDefaultNamespace(id));
        }
    }
}