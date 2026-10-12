package com.sam_mc.travelyourearth.datagen;

import com.sam_mc.travelyourearth.TravelYourEarth;
import com.sam_mc.travelyourearth.particle.ModParticles;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.data.ParticleDescriptionProvider;

/**
 * Makes assets/travelyourearth/particles/<name>.json: which textures each particle uses.
 * (The textures themselves are in textures/particle/.)
 */
public class ModParticleDescriptionProvider extends ParticleDescriptionProvider {

    public ModParticleDescriptionProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void addDescriptions() {
        // golden_spark_0 ... golden_spark_3 (4 textures). false = in that order (each sparkle picks one at random anyway)
        spriteSet(ModParticles.GOLDEN_SPARK.get(),
                Identifier.fromNamespaceAndPath(TravelYourEarth.MODID, "golden_spark"), 4, false);
    }
}