package com.sam_mc.travelyourearth.event;

import com.sam_mc.travelyourearth.TravelYourEarth;
import com.sam_mc.travelyourearth.attachment.ModAttachments;
import com.sam_mc.travelyourearth.villager.IllegalVillagers;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Villager parties! Made like vanilla's raid victory celebration (CelebrateVillagersSurvivedRaid):
 * they jump and shoot fireworks with random colors and shapes.
 *
 * - Legal villagers celebrate when a trafficker is caught in a boat, and bring gifts to the snitch
 *   (like to the Hero of the Village). The caught one is furious (angry particles).
 * - Illegal villagers celebrate WITH THE ILLAGERS when the illagers win a raid.
 *   (Illagers never attack illegal villagers: they're on the same side. During the raid the illegal
 *   villagers are excited, don't sweat, don't run away and stay next to the illagers.)
 */
@EventBusSubscriber(modid = TravelYourEarth.MODID)
public class CelebrationEvents {

    /**
     * Party after catching a trafficker: like vanilla villagers after winning a raid
     * (CelebrateVillagersSurvivedRaid: 1 firework every ~200 ticks each, about 30 seconds).
     */
    public static final int CAPTURE_CELEBRATION_TICKS = 600;
    public static final int CAPTURE_FIREWORK_CHANCE = 200;

    /** Party when the illagers win a raid: same as vanilla villagers' victory party. */
    public static final int RAID_CELEBRATION_TICKS = 600;
    public static final int RAID_FIREWORK_CHANCE = 200;

    /** The illagers lost the raid: illegal villagers are angry for 2 minutes while they leave the village. */
    public static final int RAID_LOST_SULK_TICKS = 2400;

    /** The caught trafficker is furious: angry particles for 2 minutes. Also used when one escapes. */
    public static final int CAPTURED_ANGRY_TICKS = 2400;

    /** Gifts: the villager has this long to reach the snitch, and throws when this close (like vanilla). */
    private static final int GIFT_TIMEOUT_TICKS = 600;
    private static final double GIFT_DISTANCE = 5.0D;

    /** A party: until when, and 1 firework every "fireworkChance" ticks (on average). */
    private record Party(long until, int fireworkChance) {}

    /** Party of each villager (weak: forgets removed villagers). */
    private static final Map<Villager, Party> PARTIES = new WeakHashMap<>();

    /** The raid each illegal villager already celebrated (so it doesn't celebrate the same raid twice). */
    private static final Map<Villager, Raid> CELEBRATED_RAID = new WeakHashMap<>();

    private CelebrationEvents() {}

    /** Starts (or extends) a party for this villager. */
    public static void celebrate(Villager villager, int ticks, int fireworkChance) {
        long until = villager.level().getGameTime() + ticks;
        Party old = PARTIES.get(villager);
        if (old != null) {
            until = Math.max(until, old.until());
            fireworkChance = Math.min(fireworkChance, old.fireworkChance());
        }
        PARTIES.put(villager, new Party(until, fireworkChance));
        villager.playSound(SoundEvents.VILLAGER_CELEBRATE, 1.0F, 1.0F);
    }

    // =========================================================================
    // Angry particles (vanilla entity event 13 = angry villager clouds)
    // =========================================================================

    private static final Map<Villager, Long> ANGRY_UNTIL = new WeakHashMap<>();

    /** One angry puff every 3 seconds (vanilla shows one puff each time you hit a villager). */
    private static final int ANGRY_PUFF_EVERY_TICKS = 60;

    public static void beAngry(Villager villager, int ticks) {
        long until = villager.level().getGameTime() + ticks;
        ANGRY_UNTIL.merge(villager, until, Math::max);
        // One puff right away, exactly like when you hit a villager
        if (villager.level() instanceof ServerLevel level) {
            level.broadcastEntityEvent(villager, (byte) 13);
        }
    }

    /**
     * Villagers sulking (angry, they don't trade with anyone): illegal ones when the illagers lose a raid,
     * and everyone when a trafficker escapes.
     */
    private static final Map<Villager, Long> SULKING_UNTIL = new WeakHashMap<>();

    /** Angry particles AND no trades with anyone for a while. */
    public static void sulk(Villager villager, int ticks) {
        long until = villager.level().getGameTime() + ticks;
        SULKING_UNTIL.merge(villager, until, Math::max);
        beAngry(villager, ticks);
        villager.playSound(SoundEvents.VILLAGER_NO, 1.0F, 1.0F);
    }

    public static boolean isSulking(Villager villager) {
        Long until = SULKING_UNTIL.get(villager);
        return until != null && villager.level().getGameTime() < until;
    }

    // =========================================================================
    // Gifts for the snitch, like vanilla villagers give to the Hero of the Village
    // (same loot tables: minecraft:gameplay/hero_of_the_village/<profession>_gift)
    // =========================================================================

    private record Gift(UUID player, long until) {}

    private static final Map<Villager, Gift> GIFTS = new WeakHashMap<>();

    public static void giveGift(Villager villager, UUID player) {
        GIFTS.put(villager, new Gift(player, villager.level().getGameTime() + GIFT_TIMEOUT_TICKS));
    }

    /** Walks to the player and, when close, throws its gift. */
    private static void tickGift(Villager villager, ServerLevel level) {
        Gift gift = GIFTS.get(villager);
        if (gift == null) return;

        Player player = level.getPlayerByUUID(gift.player());
        if (player == null || level.getGameTime() >= gift.until() || player.level() != level) {
            GIFTS.remove(villager);
            return;
        }
        if (villager.distanceTo(player) > GIFT_DISTANCE) {
            villager.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(player, 0.6F, 3));
            return;
        }

        GIFTS.remove(villager);
        villager.getBrain().setMemoryWithExpiry(MemoryModuleType.LOOK_TARGET, new EntityTracker(player, true), 40L);
        for (ItemStack item : giftItems(villager, level)) {
            BehaviorUtils.throwItem(villager, item, player.position());
        }
    }

    private static List<ItemStack> giftItems(Villager villager, ServerLevel level) {
        String name;
        if (villager.isBaby()) {
            name = "baby_gift";
        } else {
            Optional<String> profession = villager.getVillagerData().profession().unwrapKey()
                    .map(key -> key.identifier().getPath());
            if (profession.isEmpty() || profession.get().equals("none") || profession.get().equals("nitwit")) {
                return List.of(); // vanilla: unemployed villagers and nitwits don't give gifts
            }
            name = profession.get() + "_gift";
        }
        ResourceKey<LootTable> table = ResourceKey.create(Registries.LOOT_TABLE,
                Identifier.withDefaultNamespace("gameplay/hero_of_the_village/" + name));
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, villager.position())
                .withParameter(LootContextParams.THIS_ENTITY, villager)
                .create(LootContextParamSets.GIFT);
        return level.getServer().reloadableRegistries().getLootTable(table).getRandomItems(params);
    }

    // =========================================================================
    // Tick: jump + fireworks / illagers won?
    // =========================================================================
    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Villager villager)) return;
        if (!(villager.level() instanceof ServerLevel level)) return;

        // Illegal villagers: did the illagers just win the raid here? Party with them!
        if (villager.tickCount % 20 == 0 && IllegalVillagers.isIllegalActive(villager)) {
            Raid raid = level.getRaidAt(villager.blockPosition());
            if (raid != null && raid.isLoss() && CELEBRATED_RAID.get(villager) != raid) {
                CELEBRATED_RAID.put(villager, raid);
                celebrate(villager, RAID_CELEBRATION_TICKS, RAID_FIREWORK_CHANCE);
                // Proud: shows its ruby badge to everyone while it celebrates
                villager.setData(ModAttachments.PROUD_TRAFFICKER, Boolean.TRUE);
            }
            // The illagers LOST: like a caught trafficker, it's furious (angry clouds, no trades)
            // and it leaves the village forever (it disappears far away from the players)
            if (raid != null && raid.isVictory() && CELEBRATED_RAID.get(villager) != raid) {
                CELEBRATED_RAID.put(villager, raid);
                sulk(villager, RAID_LOST_SULK_TICKS);
                villager.setData(ModAttachments.ILLEGAL_CHASE,
                        villager.getData(ModAttachments.ILLEGAL_CHASE).withLeaving());
            }
        }

        // Angry particles: one puff (like when you hit it) every 3 seconds while angry
        Long angryUntil = ANGRY_UNTIL.get(villager);
        if (angryUntil != null) {
            if (level.getGameTime() >= angryUntil) {
                ANGRY_UNTIL.remove(villager);
            } else if (villager.tickCount % ANGRY_PUFF_EVERY_TICKS == 0) {
                level.broadcastEntityEvent(villager, (byte) 13);
            }
        }

        // Gifts for the snitch
        if (villager.tickCount % 10 == 0) {
            tickGift(villager, level);
        }

        Party party = PARTIES.get(villager);
        if (party == null) return;
        if (level.getGameTime() >= party.until()) {
            PARTIES.remove(villager);
            if (villager.hasData(ModAttachments.PROUD_TRAFFICKER) && villager.getData(ModAttachments.PROUD_TRAFFICKER)) {
                villager.setData(ModAttachments.PROUD_TRAFFICKER, Boolean.FALSE);
            }
            return;
        }

        RandomSource random = villager.getRandom();
        // Jump (like vanilla celebrating villagers)
        if (villager.onGround() && random.nextInt(15) == 0) {
            villager.getJumpControl().jump();
        }
        // Celebrate sound now and then
        if (random.nextInt(80) == 0) {
            villager.playSound(SoundEvents.VILLAGER_CELEBRATE, 1.0F, 1.0F);
        }
        // Firework (only under the open sky, so it doesn't explode inside the house)
        if (random.nextInt(party.fireworkChance()) == 0 && level.canSeeSky(villager.blockPosition().above())) {
            shootFirework(level, villager, random);
        }
    }

    /** A firework with random color and shape, like vanilla villagers after winning a raid. */
    private static void shootFirework(ServerLevel level, Villager villager, RandomSource random) {
        DyeColor color = DyeColor.values()[random.nextInt(DyeColor.values().length)];
        FireworkExplosion.Shape shape = FireworkExplosion.Shape.values()[random.nextInt(FireworkExplosion.Shape.values().length)];
        FireworkExplosion explosion = new FireworkExplosion(shape, IntList.of(color.getFireworkColor()), IntList.of(), false, false);

        ItemStack rocket = new ItemStack(Items.FIREWORK_ROCKET);
        rocket.set(DataComponents.FIREWORKS, new Fireworks(1, List.of(explosion)));

        FireworkRocketEntity entity = new FireworkRocketEntity(level, villager,
                villager.getX(), villager.getEyeY(), villager.getZ(), rocket);
        level.addFreshEntity(entity);
    }

    // =========================================================================
    // During a raid: illegal villagers are EXCITED and stay next to the illagers
    // =========================================================================

    /** How far they look for an illager to stay with. */
    private static final double RAIDER_SEARCH_RANGE = 48.0D;

    @SubscribeEvent
    public static void onRaidTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Villager villager)) return;
        if (!(villager.level() instanceof ServerLevel level)) return;
        if (villager.tickCount % 5 != 0) return;
        if (!IllegalVillagers.isIllegalActive(villager) || villager.isBaby()) return;

        Raid raid = level.getRaidAt(villager.blockPosition());
        boolean raidGoing = raid != null && raid.isActive() && !raid.isOver();

        // Ruby badge for everyone from the moment the raid starts (until the raid ends;
        // if the illagers win, it keeps it during the victory party)
        boolean proud = villager.hasData(ModAttachments.PROUD_TRAFFICKER) && villager.getData(ModAttachments.PROUD_TRAFFICKER);
        if (raidGoing && !proud) {
            villager.setData(ModAttachments.PROUD_TRAFFICKER, Boolean.TRUE);
        } else if (!raidGoing && proud && !PARTIES.containsKey(villager)) {
            villager.setData(ModAttachments.PROUD_TRAFFICKER, Boolean.FALSE);
        }
        if (!raidGoing) return;

        // Wake up! There's a party outside
        if (villager.isSleeping()) {
            villager.stopSleeping();
        }

        // Go next to the nearest illager (or ravager) and look at it
        level.getEntitiesOfClass(Raider.class, villager.getBoundingBox().inflate(RAIDER_SEARCH_RANGE), Raider::isAlive)
                .stream()
                .min(java.util.Comparator.comparingDouble(villager::distanceToSqr))
                .ifPresent(raider -> {
                    villager.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(raider, 0.6F, 3));
                    villager.getBrain().setMemoryWithExpiry(MemoryModuleType.LOOK_TARGET,
                            new EntityTracker(raider, true), 10L);
                });

        // Excited instead of worried: happy particles (entity event 14, like when you trade), little jumps and cheers
        RandomSource random = villager.getRandom();
        if (random.nextInt(8) == 0) {
            level.broadcastEntityEvent(villager, (byte) 14);
        }
        if (villager.onGround() && random.nextInt(10) == 0) {
            villager.getJumpControl().jump();
        }
        if (random.nextInt(30) == 0) {
            villager.playSound(SoundEvents.VILLAGER_CELEBRATE, 1.0F, 1.0F);
        }
    }

    // =========================================================================
    // Illagers never attack illegal villagers (they're on the same side)
    // =========================================================================
    @SubscribeEvent
    public static void onRaiderTarget(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Raider)) return;
        LivingEntity target = event.getNewAboutToBeSetTarget();
        if (target instanceof Villager villager && IllegalVillagers.isIllegalActive(villager)) {
            event.setCanceled(true);
        }
    }
}