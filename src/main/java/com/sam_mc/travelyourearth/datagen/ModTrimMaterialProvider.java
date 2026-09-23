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

        // NUEVO EN 26.3 (confirmado por el record real TrimMaterial(Identifier paletteId, Component description)):
        // el schema ahora SOLO tiene "palette_id" y "description". "asset_name" y
        // "override_armor_assets" ya no existen - por eso el juego crasheaba pidiendo palette_id.
        //
        // palette_id ocupa el rol que tenia "asset_name": aqui usamos nuestro propio
        // namespace para que apunte a nuestra propia paleta/textura de trim en vez de
        // reusar una vanilla (quartz, hierro, etc).
        //
        // OJO: este valor NO lo valida el compilador - si esta mal no crashea, solo se
        // veria mal o invisible en el juego. Verificar poniendose la armadura de rubi
        // y aplicando una plantilla de trim en la mesa de herreria.
        json.addProperty("palette_id", TravelYourEarth.MODID + ":ruby");

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