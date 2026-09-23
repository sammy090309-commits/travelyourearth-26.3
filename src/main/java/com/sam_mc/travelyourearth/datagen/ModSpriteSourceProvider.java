package com.sam_mc.travelyourearth.datagen;

import com.sam_mc.travelyourearth.TravelYourEarth;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.renderer.texture.atlas.sources.PalettedPermutations;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.AtlasIds;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.data.SpriteSourceProvider;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * NUEVO (26.3): añade las texturas de trim de rubí al atlas de ÍTEMS.
 *
 * Por qué hace falta:
 * - Los íconos de armadura con trim usan sprites como "minecraft:trims/items/boots_trim_ruby".
 *   Esos sprites no existen como PNG: el atlas los fabrica recoloreando la textura base del trim
 *   con una paleta (igual que vanilla con cobre, hierro, etc.).
 * - NeoForge 26.3.0.7-beta intenta hacerlo automáticamente, pero todavía usa la paleta base VIEJA
 *   ("trims/color_palettes/trim_palette"), que en 26.3 ya no existe. Por eso fallaba.
 *
 * Esto genera assets/minecraft/atlases/items.json dentro del mod. Los atlas se SUMAN entre packs,
 * así que no reemplaza el de vanilla: solo le añade nuestras dos permutaciones.
 * Es lo mismo que hace el AtlasProvider de vanilla en 26.3 (paleta base "minecraft:trim_base").
 */
public class ModSpriteSourceProvider extends SpriteSourceProvider {

    public ModSpriteSourceProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, TravelYourEarth.MODID);
    }

    @Override
    protected void gather() {
        atlas(AtlasIds.ITEMS).addSource(new PalettedPermutations(
                // Texturas base de trim de vanilla (una por pieza de armadura)
                List.of(
                        ItemModelGenerators.TRIM_PREFIX_HELMET,
                        ItemModelGenerators.TRIM_PREFIX_CHESTPLATE,
                        ItemModelGenerators.TRIM_PREFIX_LEGGINGS,
                        ItemModelGenerators.TRIM_PREFIX_BOOTS
                ),
                // Paleta base de 26.3 -> assets/minecraft/textures/palettes/trim_base.png
                Identifier.withDefaultNamespace("trim_base"),
                // sufijo del sprite -> paleta (assets/travelyourearth/textures/palettes/trim/<nombre>.png)
                Map.of(
                        "ruby", Identifier.fromNamespaceAndPath(TravelYourEarth.MODID, "trim/ruby"),
                        "ruby_darker", Identifier.fromNamespaceAndPath(TravelYourEarth.MODID, "trim/ruby_darker")
                )
        ));
    }
}
