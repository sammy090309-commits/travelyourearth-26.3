package com.sam_mc.travelyourearth.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.sam_mc.travelyourearth.villager.IllegalVillagers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Illegal villagers DON'T count to defend the village in a raid.
 *
 * Vanilla: every tick the raid asks "is this still a village?" (ServerLevel.isVillage(center)).
 * When the answer is no (the villagers are dead, so their beds/workstations are free), the
 * village LOSES. Illegal villagers keep their beds, so the village would never lose while one is
 * alive (and the illagers never attack them).
 *
 * Now the answer is also "no" when there's no LEGAL villager left around the raid:
 * if only traffickers are left, the illagers win.
 */
@Mixin(Raid.class)
public abstract class RaidMixin {

    /** Vanilla raids happen within ~96 blocks of their center. */
    private static final double TRAVELYOUREARTH$RAID_AREA = 96.0D;

    @WrapOperation(method = "tick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;isVillage(Lnet/minecraft/core/BlockPos;)Z"),
            require = 0)
    private boolean travelyourearth$onlyLegalVillagersDefend(ServerLevel level, BlockPos center, Operation<Boolean> original) {
        if (!original.call(level, center)) return false;
        // Still a village only if at least one LEGAL villager is alive around the raid
        return !level.getEntitiesOfClass(Villager.class, new AABB(center).inflate(TRAVELYOUREARTH$RAID_AREA),
                villager -> villager.isAlive() && !IllegalVillagers.isIllegal(villager)).isEmpty();
    }
}