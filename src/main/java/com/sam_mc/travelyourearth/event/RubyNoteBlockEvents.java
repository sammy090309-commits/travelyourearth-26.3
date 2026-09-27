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
 * Bloque musical + bloque de rubí debajo = guitarra eléctrica.
 * El enum de instrumentos vanilla no se puede extender en NeoForge 26.3, así que
 * cancelamos la nota vanilla y tocamos la nuestra con la misma afinación y partícula.
 */
@EventBusSubscriber(modid = TravelYourEarth.MODID)
public class RubyNoteBlockEvents {

    @SubscribeEvent
    public static void onNoteBlockPlay(NoteBlockEvent.Play event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        // Si hay una cabeza de mob encima, suena la cabeza (como en vanilla)
        if (!event.getInstrument().isTunable()) return;

        BlockPos pos = event.getPos();
        if (!level.getBlockState(pos.below()).is(ModBlocks.RUBY_BLOCK.get())) return;

        event.setCanceled(true);

        int note = event.getVanillaNoteId(); // 0..24
        float pitch = NoteBlock.getPitchFromNote(note);

        level.playSound(null, pos, ModSounds.NOTE_BLOCK_RUBY.get(), SoundSource.RECORDS, 3.0F, pitch);

        // Partícula de nota con su color (count = 0 -> el primer valor es el color)
        level.sendParticles(ParticleTypes.NOTE,
                pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5,
                0, note / 24.0, 0.0, 0.0, 1.0);
    }
}