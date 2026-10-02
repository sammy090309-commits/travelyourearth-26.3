package com.sam_mc.travelyourearth.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sam_mc.travelyourearth.Config;
import net.neoforged.neoforge.common.conditions.ICondition;

/**
 * Data condition: "is the modAdvancementTab option set to this value?".
 *
 * In the advancement JSON files it looks like this:
 *   "neoforge:conditions": [ { "type": "travelyourearth:advancement_tab", "enabled": true } ]
 *
 * NeoForge checks it when the world loads (or with /reload): if it returns false, that
 * advancement is simply NOT loaded. This is how we choose which version of each advancement exists.
 */
public record ModAdvancementTabCondition(boolean enabled) implements ICondition {

    public static final MapCodec<ModAdvancementTabCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(Codec.BOOL.fieldOf("enabled").forGetter(ModAdvancementTabCondition::enabled))
            .apply(instance, ModAdvancementTabCondition::new));

    @Override
    public boolean test(ICondition.IContext context) {
        // If for some reason the config isn't loaded yet, use the default value (false = Adventure tab)
        boolean modTab = Config.COMMON_SPEC.isLoaded()
                ? Config.MOD_ADVANCEMENT_TAB.get()
                : Config.MOD_ADVANCEMENT_TAB.getDefault();
        return modTab == enabled;
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}