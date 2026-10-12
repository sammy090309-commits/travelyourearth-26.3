package com.sam_mc.travelyourearth.potion;

import com.sam_mc.travelyourearth.TravelYourEarth;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Potions of the mod. Brewing recipes are generated in ModRecipeProvider (datagen). */
public class ModPotions {

    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(Registries.POTION, TravelYourEarth.MODID);

    // Potion of Decay works like vanilla Potion of Poison, but with Wither and shorter
    // (Wither can kill, Poison can't):
    //   Decay      -> Wither I,  0:20 (400 ticks)
    //   Long Decay -> Wither I,  0:40 (800 ticks)   (redstone)
    //   Decay II   -> Wither II, 0:10 (200 ticks)   (glowstone)
    // Splash: same duration. Lingering: 1/4. Tipped arrows: 1/8 (vanilla calculates them).
    // All 3 share the name "decay" for their translation keys, like vanilla (long_poison and strong_poison use "poison").

    public static final DeferredHolder<Potion, Potion> DECAY = POTIONS.register("decay",
            () -> new Potion("decay", new MobEffectInstance(MobEffects.WITHER, 400, 0)));

    public static final DeferredHolder<Potion, Potion> LONG_DECAY = POTIONS.register("long_decay",
            () -> new Potion("decay", new MobEffectInstance(MobEffects.WITHER, 800, 0)));

    public static final DeferredHolder<Potion, Potion> STRONG_DECAY = POTIONS.register("strong_decay",
            () -> new Potion("decay", new MobEffectInstance(MobEffects.WITHER, 300, 1)));

    public static void register(IEventBus eventBus) {
        POTIONS.register(eventBus);
    }
}