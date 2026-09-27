package com.sam_mc.travelyourearth.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sam_mc.travelyourearth.Config;
import net.neoforged.neoforge.common.conditions.ICondition;

/**
 * Condición de datos: "¿la opción modAdvancementTab está en este valor?".
 *
 * En los JSON de los logros se ve así:
 *   "neoforge:conditions": [ { "type": "travelyourearth:advancement_tab", "enabled": true } ]
 *
 * NeoForge la revisa al cargar el mundo (o con /reload): si da false, ese logro
 * simplemente NO se carga. Así elegimos qué versión de cada logro existe.
 */
public record ModAdvancementTabCondition(boolean enabled) implements ICondition {

    public static final MapCodec<ModAdvancementTabCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(Codec.BOOL.fieldOf("enabled").forGetter(ModAdvancementTabCondition::enabled))
            .apply(instance, ModAdvancementTabCondition::new));

    @Override
    public boolean test(ICondition.IContext context) {
        // Si por algo la config aún no cargó, usamos el valor por defecto (false = pestaña Aventura)
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