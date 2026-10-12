package com.sam_mc.travelyourearth.mixin;

import com.sam_mc.travelyourearth.pot.PotMobs;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 1.2.1: when a decorated pot is removed, the mobs inside come out alive.
 *
 * preRemoveSideEffects is where vanilla drops the contents of any container block
 * (BlockEntity.preRemoveSideEffects -> Containers.dropContents). It runs on the server every time
 * the block goes away: broken with a tool (shattered or not), hit by a projectile, blown up,
 * destroyed by a piston... DecoratedPotBlockEntity doesn't override it, so the mixin goes on BlockEntity
 * and only acts for pots.
 */
@Mixin(BlockEntity.class)
public abstract class DecoratedPotRemovalMixin {

    @Inject(method = "preRemoveSideEffects(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V",
            at = @At("HEAD"))
    private void travelyourearth$releasePotMobs(BlockPos pos, BlockState state, CallbackInfo ci) {
        if ((Object) this instanceof DecoratedPotBlockEntity pot && pot.getLevel() instanceof ServerLevel level) {
            PotMobs.release(pot, level, pos);
        }
    }
}
