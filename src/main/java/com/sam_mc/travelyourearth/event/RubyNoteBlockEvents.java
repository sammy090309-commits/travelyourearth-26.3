package com.sam_mc.travelyourearth.event;

import com.sam_mc.travelyourearth.TravelYourEarth;
import com.sam_mc.travelyourearth.block.ModBlocks;
import com.sam_mc.travelyourearth.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.NoteBlock;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.NoteBlockEvent;

/**
 * Note block + ruby block below = electric guitar.
 * The vanilla instrument enum can't be extended in NeoForge 26.3, so we cancel
 * the vanilla note and play ours with the same pitch and particle.
 */
@EventBusSubscriber(modid = TravelYourEarth.MODID)
public class RubyNoteBlockEvents {

    @SubscribeEvent
    public static void onNoteBlockPlay(NoteBlockEvent.Play event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        // If there's a mob head on top, the head plays (like in vanilla)
        if (!event.getInstrument().isTunable()) return;

        BlockPos pos = event.getPos();
        if (!level.getBlockState(pos.below()).is(ModBlocks.RUBY_BLOCK.get())) return;

        event.setCanceled(true);

        int note = event.getVanillaNoteId(); // 0..24
        float pitch = NoteBlock.getPitchFromNote(note);

        level.playSound(null, pos, ModSounds.NOTE_BLOCK_RUBY.get(), SoundSource.RECORDS, 3.0F, pitch);

        // Note particle with its color (count = 0 -> the first value is the color)
        level.sendParticles(ParticleTypes.NOTE,
                pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5,
                0, note / 24.0, 0.0, 0.0, 1.0);
    }
}