package com.sam_mc.travelyourearth.event;

import com.sam_mc.travelyourearth.TravelYourEarth;
import com.sam_mc.travelyourearth.villager.IllegalVillagers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.schedule.Activity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.Optional;

/**
 * Part 4: illegal villagers are antisocial workaholics.
 *
 * Vanilla villager day: WORK (morning) -> MEET (afternoon: they meet at the bell and gossip)
 * -> IDLE (free time: walking around, chatting, playing) -> REST (night).
 *
 * Illegal villagers skip most of the social time: on 3 of every 4 days they don't go to the
 * meeting at the bell nor hang out in their free time. They stay at their workstation instead,
 * "taking care of their business".
 *
 * They also never have babies: Villager.canBreed() is false for them (VillagerMixin).
 */
@EventBusSubscriber(modid = TravelYourEarth.MODID)
public class IllegalPersonalityEvents {

    /** Chance (each day) of skipping the social time to stay at work. */
    private static final float SKIP_SOCIAL_CHANCE = 0.75F;

    /** "At the workstation" = this close to it. */
    private static final double AT_WORK_DISTANCE = 3.0D;

    private static final float WALK_SPEED = 0.5F;

    private IllegalPersonalityEvents() {}

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Villager villager)) return;
        if (!(villager.level() instanceof ServerLevel level)) return;
        if (villager.tickCount % 10 != 0) return;
        if (!IllegalVillagers.isIllegalActive(villager) || villager.isBaby()) return;

        // Other behaviors are more important: trading, on the run, raids...
        if (villager.getTradingPlayer() != null || IllegalChaseEvents.isOnTheRun(villager)) return;

        Brain<Villager> brain = villager.getBrain();
        boolean socialTime = brain.isActive(Activity.MEET) || brain.isActive(Activity.IDLE);
        if (!socialTime || !skipsSocialToday(villager, level)) return;

        // Its workstation (vanilla "job site" memory). Unemployed: nothing to do
        Optional<GlobalPos> jobSite = brain.getMemory(MemoryModuleType.JOB_SITE)
                .filter(pos -> pos.dimension() == level.dimension());
        if (jobSite.isEmpty()) return;

        BlockPos workstation = jobSite.get().pos();
        if (!villager.blockPosition().closerThan(workstation, AT_WORK_DISTANCE)) {
            // Back to work
            brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(workstation, WALK_SPEED, 1));
        } else {
            // Already there: stays and looks at its workstation (no strolling around)
            brain.eraseMemory(MemoryModuleType.WALK_TARGET);
            brain.setMemoryWithExpiry(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(workstation), 20L);
        }
    }

    /**
     * Decided once per Minecraft day and villager (always the same answer during the same day):
     * 3 of every 4 days it skips the meeting.
     */
    private static boolean skipsSocialToday(Villager villager, ServerLevel level) {
        long day = level.getGameTime() / 24000L;
        long seed = villager.getUUID().getLeastSignificantBits() ^ (day * 0x9E3779B97F4A7C15L);
        return RandomSource.create(seed).nextFloat() < SKIP_SOCIAL_CHANCE;
    }
}