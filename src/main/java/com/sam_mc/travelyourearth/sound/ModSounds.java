package com.sam_mc.travelyourearth.sound;

import com.sam_mc.travelyourearth.TravelYourEarth;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Todos los sonidos del mod. */
public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, TravelYourEarth.MODID);

    /** Bloque musical encima de un bloque de rubí: guitarra eléctrica (afinada en F#3). */
    public static final DeferredHolder<SoundEvent, SoundEvent> NOTE_BLOCK_RUBY =
            SOUND_EVENTS.register("block.note_block.ruby", SoundEvent::createVariableRangeEvent);

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}