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
        json.addProperty("asset_name", "ruby");
        json.addProperty("ingredient", TravelYourEarth.MODID + ":ruby");
        json.addProperty("item_model_index", 0.8F);

        JsonObject description = new JsonObject();
        description.addProperty("translate", "trim_material.travelyourearth.ruby");
        description.addProperty("color", "#FA1E4E");
        json.add("description", description);

        JsonObject overrideArmorMaterials = new JsonObject();
        overrideArmorMaterials.addProperty(TravelYourEarth.MODID + ":ruby", "ruby_darker");
        json.add("override_armor_materials", overrideArmorMaterials);

        return DataProvider.saveStable(cachedOutput, json, path);
    }

    @Override
    public String getName() {
        return "Ruby Trim Material Provider";
    }
}