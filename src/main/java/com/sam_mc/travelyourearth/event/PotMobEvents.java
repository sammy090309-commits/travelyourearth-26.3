package com.sam_mc.travelyourearth.event;

import com.sam_mc.travelyourearth.TravelYourEarth;
import com.sam_mc.travelyourearth.pot.PotMobs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * 1.2.1: CREATIVE + sneaking + spawn egg on a decorated pot = the mob goes INTO the pot.
 *
 * Vanilla: sneaking with an item in the hand skips the pot (useItemOn) and the egg spawns the mob
 * on top. This event runs before that, so we take the egg instead. Feedback is the vanilla one of
 * the pot: wobble + insert sound + dust plume (or the "fail" wobble when it doesn't fit).
 * In survival nothing changes. Clicking WITHOUT sneaking stays vanilla too (the egg goes in as an item).
 */
@EventBusSubscriber(modid = TravelYourEarth.MODID)
public class PotMobEvents {

    private PotMobEvents() {}

    @SubscribeEvent
    public static void onUseOnPot(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (!player.isCreative() || !player.isSecondaryUseActive()) return;

        ItemStack egg = event.getItemStack();
        if (!(egg.getItem() instanceof SpawnEggItem) || SpawnEggItem.getType(egg) == null) return;

        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        if (!(level.getBlockEntity(pos) instanceof DecoratedPotBlockEntity pot)) return;

        // Both sides: the egg must NOT spawn the mob on top of the pot
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (!(level instanceof ServerLevel serverLevel)) return;

        if (PotMobs.tryInsert(pot, egg)) {
            // Same as vanilla inserting an item (DecoratedPotBlock.useItemOn). Creative: the egg is not used up
            pot.wobble(DecoratedPotBlockEntity.WobbleStyle.POSITIVE);
            level.playSound(null, pos, SoundEvents.DECORATED_POT_INSERT, SoundSource.BLOCKS, 1.0F, 0.7F + 0.5F * PotMobs.fullness(pot));
            serverLevel.sendParticles(ParticleTypes.DUST_PLUME, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 7, 0.0, 0.0, 0.0, 0.0);
        } else {
            // Full, another mob type, or a forbidden mob: like vanilla clicking with an empty hand
            pot.wobble(DecoratedPotBlockEntity.WobbleStyle.NEGATIVE);
            level.playSound(null, pos, SoundEvents.DECORATED_POT_INSERT_FAIL, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
    }
}
