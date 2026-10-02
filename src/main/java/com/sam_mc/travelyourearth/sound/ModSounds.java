package com.sam_mc.travelyourearth.sound;

import com.sam_mc.travelyourearth.TravelYourEarth;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** All the sounds of the mod (the .ogg files are linked in ModSoundDefinitionsProvider). */
public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, TravelYourEarth.MODID);

    /** Note block on top of a ruby block: electric guitar (tuned to F#3). */
    public static final DeferredHolder<SoundEvent, SoundEvent> NOTE_BLOCK_RUBY =
            SOUND_EVENTS.register("block.note_block.ruby", SoundEvent::createVariableRangeEvent);

    /** Sound when equipping a ruby armor piece (4 variants). */
    public static final DeferredHolder<SoundEvent, SoundEvent> ARMOR_EQUIP_RUBY =
            SOUND_EVENTS.register("item.armor.equip_ruby", SoundEvent::createVariableRangeEvent);

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}