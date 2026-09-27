package com.sam_mc.travelyourearth.datagen;

import com.sam_mc.travelyourearth.TravelYourEarth;
import com.sam_mc.travelyourearth.sound.ModSounds;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.data.SoundDefinition;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;

/** Genera assets/travelyourearth/sounds.json */
public class ModSoundDefinitionsProvider extends SoundDefinitionsProvider {

    public ModSoundDefinitionsProvider(PackOutput output) {
        super(output, TravelYourEarth.MODID);
    }

    @Override
    public void registerSounds() {
        add(ModSounds.NOTE_BLOCK_RUBY, SoundDefinition.definition()
                .subtitle("subtitles.block.note_block.note") // subtítulo vanilla
                .with(sound(Identifier.fromNamespaceAndPath(TravelYourEarth.MODID, "note/ruby"))));
    }
}