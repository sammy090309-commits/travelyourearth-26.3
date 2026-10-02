package com.sam_mc.travelyourearth.datagen;

import com.sam_mc.travelyourearth.TravelYourEarth;
import com.sam_mc.travelyourearth.sound.ModSounds;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.data.SoundDefinition;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;

/** Generates assets/travelyourearth/sounds.json */
public class ModSoundDefinitionsProvider extends SoundDefinitionsProvider {

    public ModSoundDefinitionsProvider(PackOutput output) {
        super(output, TravelYourEarth.MODID);
    }

    @Override
    public void registerSounds() {

        // Note block on top of a ruby block: electric guitar
        add(ModSounds.NOTE_BLOCK_RUBY, SoundDefinition.definition()
                .subtitle("subtitles.block.note_block.note") // vanilla subtitle
                .with(sound(modSound("note/ruby"))));

        // Equipping ruby armor: 4 variants, played at 30% volume
        add(ModSounds.ARMOR_EQUIP_RUBY, SoundDefinition.definition()
                .subtitle("subtitles.travelyourearth.item.armor.equip_ruby")
                .with(
                        sound(modSound("item/armor/equip_ruby1")).volume(0.30),
                        sound(modSound("item/armor/equip_ruby2")).volume(0.30),
                        sound(modSound("item/armor/equip_ruby3")).volume(0.30),
                        sound(modSound("item/armor/equip_ruby4")).volume(0.30)
                ));
    }

    /** Shortcut: assets/travelyourearth/sounds/<path>.ogg */
    private static Identifier modSound(String path) {
        return Identifier.fromNamespaceAndPath(TravelYourEarth.MODID, path);
    }
}