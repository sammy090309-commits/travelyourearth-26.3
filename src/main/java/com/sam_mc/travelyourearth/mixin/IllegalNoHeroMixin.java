package com.sam_mc.travelyourearth.mixin;

import com.sam_mc.travelyourearth.villager.IllegalVillagers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.CelebrateVillagersSurvivedRaid;
import net.minecraft.world.entity.ai.behavior.GiveGiftToHero;
import net.minecraft.world.entity.npc.villager.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Illegal villagers don't care about the Hero of the Village:
 * - CelebrateVillagersSurvivedRaid: they don't celebrate (jumps + fireworks) when the VILLAGE wins a raid
 *   (they wanted the illagers to win!).
 * - GiveGiftToHero: they don't throw gifts to the Hero of the Village.
 * Both vanilla behaviors simply never start for them.
 */
@Mixin({CelebrateVillagersSurvivedRaid.class, GiveGiftToHero.class})
public abstract class IllegalNoHeroMixin {

    @Inject(method = "checkExtraStartConditions(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/npc/villager/Villager;)Z",
            at = @At("HEAD"), cancellable = true)
    private void travelyourearth$notForIllegal(ServerLevel level, Villager villager, CallbackInfoReturnable<Boolean> cir) {
        if (IllegalVillagers.isIllegalActive(villager)) {
            cir.setReturnValue(false);
        }
    }
}