package com.sam_mc.travelyourearth.condition;

import com.mojang.serialization.MapCodec;
import com.sam_mc.travelyourearth.TravelYourEarth;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** Data conditions of the mod (used in the "neoforge:conditions" of JSON files). */
public class ModConditions {

    public static final DeferredRegister<MapCodec<? extends ICondition>> CONDITION_CODECS =
            DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, TravelYourEarth.MODID);

    /** travelyourearth:advancement_tab */
    public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<ModAdvancementTabCondition>> ADVANCEMENT_TAB =
            CONDITION_CODECS.register("advancement_tab", () -> ModAdvancementTabCondition.CODEC);

    public static void register(IEventBus eventBus) {
        CONDITION_CODECS.register(eventBus);
    }
}