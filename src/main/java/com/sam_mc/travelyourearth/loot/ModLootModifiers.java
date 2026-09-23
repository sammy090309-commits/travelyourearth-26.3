package com.sam_mc.travelyourearth.loot;

import com.mojang.serialization.MapCodec;
import com.sam_mc.travelyourearth.TravelYourEarth;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModLootModifiers {

    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, TravelYourEarth.MODID);

    public static final Supplier<MapCodec<? extends IGlobalLootModifier>> REPLACE_WITH_ITEM =
            LOOT_MODIFIERS.register("replace_with_item", () -> ReplaceWithItemLootModifier.CODEC);

    public static final Supplier<MapCodec<? extends IGlobalLootModifier>> RUBY_TRIAL_GEAR  =
            LOOT_MODIFIERS.register("ruby_trial_gear", () -> RubyTrialGearLootModifier.CODEC);

    public static void register(IEventBus eventBus) {
        LOOT_MODIFIERS.register(eventBus);
    }
}
