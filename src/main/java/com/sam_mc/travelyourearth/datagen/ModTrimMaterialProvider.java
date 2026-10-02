package com.sam_mc.travelyourearth.datagen;

import com.google.gson.JsonObject;
import com.sam_mc.travelyourearth.TravelYourEarth;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import org.jetbrains.annotations.Contract;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/** Generates data/travelyourearth/trim_material/ruby.json (the ruby trim material). */
public class ModTrimMaterialProvider implements DataProvider {

    private final PackOutput output;

    @Contract(pure = true)
    public ModTrimMaterialProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        Path path = this.output.getOutputFolder(PackOutput.Target.DATA_PACK)
                .resolve("travelyourearth/trim_material/ruby.json");

        JsonObject json = new JsonObject();

        // 26.3: TrimMaterial(Identifier paletteId, Component description).
        // palette_id is a path relative to textures/palettes/ (vanilla's Palette.ID_CONVERTER).
        // Vanilla uses "minecraft:trim/copper" -> assets/minecraft/textures/palettes/trim/copper.png
        // Ours -> assets/travelyourearth/textures/palettes/trim/ruby.png
        json.addProperty("palette_id", TravelYourEarth.MODID + ":trim/ruby");

        // Name shown in the tooltip, in ruby red
        JsonObject description = new JsonObject();
        description.addProperty("translate", "trim_material.travelyourearth.ruby");
        description.addProperty("color", "#D9253B");
        json.add("description", description);

        return DataProvider.saveStable(cachedOutput, json, path);
    }

    @Override
    public String getName() {
        return "Ruby Trim Material Provider";
    }
}