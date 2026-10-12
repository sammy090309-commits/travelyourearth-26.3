package com.sam_mc.travelyourearth.particle;

import com.sam_mc.travelyourearth.TravelYourEarth;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The mod's particles.
 *
 * golden_spark: golden sparkles of the Cowardice enchantment (only when a hit carries its extra damage).
 *   Textures: assets/travelyourearth/textures/particle/golden_spark_0.png ... golden_spark_3.png (8 x 8)
 *   Each sparkle picks ONE of them at random (they're not an animation).
 *   Drawn on the client by GoldenSparkParticle; its .json is made by datagen (ModParticleDescriptionProvider).
 */
public class ModParticles {

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, TravelYourEarth.MODID);

    /** false = it respects the "Particles: Minimal/Decreased" video setting like most vanilla particles. */
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> GOLDEN_SPARK =
            PARTICLE_TYPES.register("golden_spark", () -> new SimpleParticleType(false));

    public static void register(IEventBus eventBus) {
        PARTICLE_TYPES.register(eventBus);
    }
}