package com.sam_mc.travelyourearth.client.particle;

import com.sam_mc.travelyourearth.TravelYourEarth;
import com.sam_mc.travelyourearth.particle.ModParticles;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

/** Client only: tells the game how to draw each particle of the mod. */
@EventBusSubscriber(modid = TravelYourEarth.MODID, value = Dist.CLIENT)
public class ModParticleProviders {

    @SubscribeEvent
    public static void registerProviders(RegisterParticleProvidersEvent event) {
        // registerSpriteSet = the particle uses the textures listed in its .json (golden_spark_0..3)
        event.registerSpriteSet(ModParticles.GOLDEN_SPARK.get(), GoldenSparkParticle.Provider::new);
    }
}