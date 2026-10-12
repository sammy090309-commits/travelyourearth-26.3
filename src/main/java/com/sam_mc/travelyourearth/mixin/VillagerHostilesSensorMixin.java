package com.sam_mc.travelyourearth.mixin;

import com.sam_mc.travelyourearth.villager.IllegalVillagers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.sensing.VillagerHostilesSensor;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.raid.Raider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Vanilla villagers "see" illagers as hostile: they panic and run away from them.
 * Illegal villagers are on the illagers' side, so for them illagers (and ravagers) are NOT hostile:
 * no panic, no running away. Zombies are still scary.
 */
@Mixin(VillagerHostilesSensor.class)
public abstract class VillagerHostilesSensorMixin {

    @Inject(method = "isMatchingEntity", at = @At("HEAD"), cancellable = true)
    private void travelyourearth$illagersAreFriends(ServerLevel level, LivingEntity villager, LivingEntity target,
                                                    CallbackInfoReturnable<Boolean> cir) {
        if (target instanceof Raider && villager instanceof Villager v && IllegalVillagers.isIllegalActive(v)) {
            cir.setReturnValue(false);
        }
    }
}