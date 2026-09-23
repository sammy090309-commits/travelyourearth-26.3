package com.sam_mc.travelyourearth.datagen;

import com.google.gson.JsonObject;
import com.sam_mc.travelyourearth.TravelYourEarth;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import org.jetbrains.annotations.Contract;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

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
        // palette_id es una ruta relativa a textures/palettes/ (Palette.ID_CONVERTER de vanilla).
        // Vanilla usa "minecraft:trim/copper" -> assets/minecraft/textures/palettes/trim/copper.png
        // CAMBIO: "travelyourearth:ruby" apuntaba a textures/palettes/ruby.png, que no existe.
        // Ahora -> assets/travelyourearth/textures/palettes/trim/ruby.png
        json.addProperty("palette_id", TravelYourEarth.MODID + ":trim/ruby");

        JsonObject description = new JsonObject();
        description.addProperty("translate", "trim_material.travelyourearth.ruby");
        description.addProperty("color", "#FA1E4E");
        json.add("description", description);

        return DataProvider.saveStable(cachedOutput, json, path);
    }

    @Override
    public String getName() {
        return "Ruby Trim Material Provider";
    }
}