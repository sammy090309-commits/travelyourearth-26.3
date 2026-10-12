package com.sam_mc.travelyourearth.event;

import com.sam_mc.travelyourearth.advancement.ModAdvancements;
import com.sam_mc.travelyourearth.TravelYourEarth;
import com.sam_mc.travelyourearth.attachment.IllegalChaseData;
import com.sam_mc.travelyourearth.attachment.IllegalVillagerData;
import com.sam_mc.travelyourearth.attachment.ModAttachments;
import com.sam_mc.travelyourearth.config.IllegalVillagerConfig;
import com.sam_mc.travelyourearth.villager.IllegalVillagers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Part 2 of the illegal villager behavior: getting caught.
 *
 * - SEEN: if a legal villager or an iron golem sees you trading with it (screen open with its illegal
 *   trades, or buying one), it gets SCARED: it closes the deal and runs away from villagers and golems.
 * - SNITCH: if you ring a bell 4 blocks or closer to an illegal villager whose secret you know, it becomes WANTED
 *   (5 minutes): it runs away from everyone. Like raiders, the bell resonates and it glows for
 *   10 seconds; ring the bell again to see where it is.
 * - ESCAPED: if the time runs out, it leaves the village and disappears far away from the players.
 * - CATCH: put it in a boat before the time runs out. It stops being illegal forever and the village
 *   celebrates (fireworks!) and thanks you like curing a zombie villager (discounts + Hero of the Village). If it escapes, it will never trust you again.
 */
@EventBusSubscriber(modid = TravelYourEarth.MODID)
public class IllegalChaseEvents {

    /** Same range as the vanilla bell that makes raiders glow. */
    private static final double BELL_RANGE = 48.0D;

    /** Run speed: about the same as vanilla villagers when they panic (0.5-0.6). */
    private static final float FLEE_SPEED = 0.6F;

    /** WANTED: a bit faster than a normal panic, but slow enough to catch it with a boat. */
    private static final float WANTED_FLEE_SPEED = 0.8F;

    /** Speed of the villagers that come to catch the wanted one. */
    private static final float HELPER_SPEED = 0.75F;

    /** Other illegal villagers this close to the bell avoid the snitch for a while. */
    private static final double COLLEAGUES_RANGE = 96.0D;

    /** An escaped villager walks away in steps of this many blocks, always in the same direction. */
    private static final int LEAVE_STEP_BLOCKS = 16;

    /** Turns (degrees) tried when the way out is blocked: straight first, then more and more to the sides. */
    private static final int[] LEAVE_TURNS = {0, 45, -45, 90, -90};

    /** The spot each escaped villager is walking to right now (to know if the walk target is ours). */
    private static final Map<Villager, Vec3> LEAVE_TARGET = new WeakHashMap<>();

    /** The direction each escaped villager walks to (weak: forgets removed villagers). */
    private static final Map<Villager, Vec3> LEAVE_DIRECTION = new WeakHashMap<>();

    /** An escaped villager disappears when no player is this close (80 blocks = 5 chunks). */
    private static final double LEAVE_DESPAWN_DISTANCE = 80.0D;

    /** To snitch you must ring a bell this close to the illegal villager (4 blocks). */
    private static final double SNITCH_RANGE = 4.0D;

    /**
     * While a colleague is wanted, the other illegal villagers hide at home (nervous, sweating,
     * no trades). Game time until each one hides (weak: forgets removed villagers).
     */
    private static final Map<Villager, Long> HIDING_UNTIL = new WeakHashMap<>();

    /** Hero of the Village reward for catching one: 5 minutes. */
    private static final int HERO_REWARD_TICKS = 6000;

    private IllegalChaseEvents() {}

    public static IllegalChaseData chase(Villager villager) {
        return villager.getData(ModAttachments.ILLEGAL_CHASE);
    }

    /** Called when the villager shows its secret to a player: from now on that player can snitch on it. */
    public static void remember(Villager villager, UUID player) {
        villager.setData(ModAttachments.ILLEGAL_CHASE, chase(villager).withKnown(player));
    }

    /** Someone saw it trading: it runs away from villagers and golems for a while. */
    public static void scare(Villager villager, ServerLevel level) {
        long until = level.getGameTime() + IllegalVillagerConfig.SCARED_TICKS.get();
        villager.setData(ModAttachments.ILLEGAL_CHASE, chase(villager).withScaredUntil(until));
        shun(villager, level, IllegalVillagerConfig.SHUN_SEEN_TICKS.get());
    }

    /**
     * The other villagers won't talk to it for a while. The time depends on what happened (config):
     * seen trading 1 day, snitched on 3 days, caught 7 days. Never shortens a longer one.
     */
    public static void shun(Villager villager, ServerLevel level, int ticks) {
        long until = level.getGameTime() + ticks;
        villager.setData(ModAttachments.ILLEGAL_CHASE, chase(villager).withShunnedUntil(until));
    }

    /** True if the other villagers don't want to talk to it right now. */
    public static boolean isShunned(Villager villager) {
        return villager.hasData(ModAttachments.ILLEGAL_CHASE)
                && chase(villager).isShunned(villager.level().getGameTime());
    }

    // =========================================================================
    // Shunned: nobody talks to it (vanilla villagers "talk" by setting each other as INTERACTION_TARGET:
    // chatting at the bell, sharing food, gossiping). We cut those conversations.
    // =========================================================================
    @SubscribeEvent
    public static void onShunTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Villager villager)) return;
        if (!(villager.level() instanceof ServerLevel)) return;
        if (villager.tickCount % 5 != 0) return;

        Optional<LivingEntity> target = villager.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET);
        if (target.isEmpty() || !(target.get() instanceof Villager other)) return;

        // Talking to a shunned (or leaving) villager, or being that one: conversation over
        if (isShunned(other) || isShunned(villager) || isLeaving(other) || isLeaving(villager)) {
            villager.getBrain().eraseMemory(MemoryModuleType.INTERACTION_TARGET);
            other.getBrain().eraseMemory(MemoryModuleType.INTERACTION_TARGET);
        }
    }

    /** True if this villager (illegal or not anymore) doesn't want to be near this player right now. */
    public static boolean avoids(Villager villager, Player player) {
        return villager.hasData(ModAttachments.ILLEGAL_CHASE)
                && chase(villager).avoids(player.getUUID(), villager.level().getGameTime());
    }

    /** True while it's scared or wanted: it doesn't trade with anyone. */
    public static boolean isOnTheRun(Villager villager) {
        IllegalChaseData chase = chase(villager);
        long now = villager.level().getGameTime();
        return chase.isWanted() || chase.isScared(now) || chase.leaving() || isHiding(villager);
    }

    /** True while it hides at home because a colleague is wanted. */
    public static boolean isHiding(Villager villager) {
        Long until = HIDING_UNTIL.get(villager);
        return until != null && villager.level().getGameTime() < until;
    }

    // =========================================================================
    // Tick: watch for witnesses while trading, run away, get caught, escape
    // =========================================================================
    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Villager villager)) return;
        if (!(villager.level() instanceof ServerLevel level)) return;
        if (villager.tickCount % 5 != 0) return;
        // Only villagers with police data (illegal ones, and the ones that WERE illegal and hate a snitch)
        if (!villager.hasData(ModAttachments.ILLEGAL_CHASE) && !IllegalVillagers.isIllegal(villager)) return;

        IllegalChaseData chase = chase(villager);
        long now = level.getGameTime();

        // 1. Wanted: caught in a boat? Time's up? Everyone comes to catch it
        if (chase.isWanted() && IllegalVillagers.isIllegal(villager)) {
            if (isInBoat(villager)) {
                caught(villager, level, chase);
                return;
            }
            if (now >= chase.wantedUntil()) {
                escaped(villager, level, chase);
                return;
            }
            callTheVillage(villager, level);
            sweat(villager, level);
            // Wanted: paranoid (24 blocks) and keeps running even when it loses sight of you
            runAway(villager, IllegalChaseEvents::isChaser, WANTED_FLEE_SPEED, WANTED_THREAT_RANGE, true);
            return;
        }

        // 1b. Escaped: walks away from the village; disappears when no player can see it (far away)
        if (chase.leaving()) {
            leaveVillage(villager, level);
            return;
        }

        // 2. A player it hates (snitched on it) or avoids (snitched on a colleague): keeps away
        Player avoidedPlayer = level.getNearestPlayer(villager.getX(), villager.getY(), villager.getZ(), 16.0D,
                entity -> entity instanceof Player p && !p.isSpectator() && chase.avoids(p.getUUID(), now));
        if (avoidedPlayer != null) {
            if (villager.getTradingPlayer() == avoidedPlayer) {
                avoidedPlayer.closeContainer();
            }
            runAway(villager, entity -> entity == avoidedPlayer, FLEE_SPEED);
            return;
        }

        if (!IllegalVillagers.isIllegal(villager)) {
            calmDown(villager);
            return;
        }

        // 3. A colleague is wanted: nervous (sweat) and hiding at home, no trades
        if (isHiding(villager)) {
            if (villager.getTradingPlayer() != null) {
                villager.getTradingPlayer().closeContainer();
            }
            sweat(villager, level);
            goHome(villager, level);
            return;
        }

        // 4. Scared: closes the deal and runs away from villagers and golems
        if (chase.isScared(now)) {
            if (villager.getTradingPlayer() != null) {
                villager.getTradingPlayer().closeContainer();
            }
            sweat(villager, level);
            runAway(villager, IllegalVillagers::isWitness, FLEE_SPEED);
            return;
        }

        // Not running from anything anymore
        calmDown(villager);

        // 5. Trading with its illegal trades on screen: does anyone see the player?
        Player trader = villager.getTradingPlayer();
        if (trader != null && IllegalVillagers.isRevealedTo(villager, trader)
                && !IllegalVillagers.illegalOffers(villager).isEmpty()) {
            List<Entity> witnesses = witnessesOf(level, trader, villager);
            if (!witnesses.isEmpty()) {
                seen(villager, level, trader, witnesses);
            }
        }
    }

    /** Legal villagers and iron golems within 16 blocks that can see the player. */
    public static List<Entity> witnessesOf(ServerLevel level, Player player, Villager illegal) {
        return level.getEntities(player, player.getBoundingBox().inflate(IllegalVillagers.WITNESS_RANGE),
                entity -> entity != illegal && IllegalVillagers.isWitness(entity)
                        && entity instanceof net.minecraft.world.entity.LivingEntity living
                        && living.hasLineOfSight(player));
    }

    /**
     * Caught in the act while the screen is open: the deal is closed, the witnesses gossip
     * (and iron golems attack right away), and the illegal villager runs away.
     */
    private static void seen(Villager villager, ServerLevel level, Player player, List<Entity> witnesses) {
        for (Entity witness : witnesses) {
            if (witness instanceof Villager legal) {
                legal.getGossips().add(player.getUUID(), GossipType.MINOR_NEGATIVE, 25);
            } else if (witness instanceof IronGolem golem) {
                golem.setTarget(player);
            }
        }
        player.closeContainer();
        scare(villager, level);
    }

    /**
     * The bell rang: every legal (awake) villager and iron golem around comes to surround the wanted
     * villager (the golems don't hurt it: you have to catch it with a boat).
     */
    private static void callTheVillage(Villager wanted, ServerLevel level) {
        AABB area = wanted.getBoundingBox().inflate(BELL_RANGE);
        for (Villager helper : level.getEntitiesOfClass(Villager.class, area,
                v -> v != wanted && !IllegalVillagers.isIllegal(v) && !v.isSleeping() && !v.isBaby())) {
            helper.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(wanted, HELPER_SPEED, 2));
        }
        for (IronGolem golem : level.getEntitiesOfClass(IronGolem.class, area, g -> g.getTarget() != wanted)) {
            golem.getNavigation().moveTo(wanted, 1.0D);
        }
    }

    /**
     * Nervous sweat drops (the same "splash" particles villagers show during a raid).
     * Sent from the server as particles, because the normal raid sweat event is turned off
     * for illegal villagers (VillagerMixin).
     */
    private static void sweat(Villager villager, ServerLevel level) {
        if (villager.tickCount % 10 != 0) return;
        level.sendParticles(ParticleTypes.SPLASH,
                villager.getX(), villager.getY() + villager.getBbHeight() + 0.1D, villager.getZ(),
                5, villager.getBbWidth() * 0.5D, 0.2D, villager.getBbWidth() * 0.5D, 0.0D);
    }

    /**
     * Walks away from the village (from its bell, or from where it is if it doesn't know the bell).
     * When no player is within 64 blocks, it's gone for good: it frees its bed and workstation and disappears.
     */
    private static void leaveVillage(Villager villager, ServerLevel level) {
        if (level.getNearestPlayer(villager, LEAVE_DESPAWN_DISTANCE) == null) {
            releasePois(villager, level);
            villager.discard();
            return;
        }
        if (villager.getTradingPlayer() != null) {
            villager.getTradingPlayer().closeContainer();
        }
        if (villager.isSleeping()) {
            villager.stopSleeping(); // no time to sleep: it's leaving
        }
        // Decide its way out FIRST (it uses the bell it remembers)...
        Vec3 direction = LEAVE_DIRECTION.computeIfAbsent(villager, v -> leaveDirection(v, level));
        // ...and then it forgets the village: no work, no bed, no bell, no chatting
        forgetVillage(villager, level);

        // Still walking to OUR target? Keep going. (Vanilla's brain keeps giving it its own walk targets:
        // going to work, strolling... Those are replaced by ours.)
        Vec3 ourTarget = LEAVE_TARGET.get(villager);
        Optional<WalkTarget> current = villager.getBrain().getMemory(MemoryModuleType.WALK_TARGET);
        if (ourTarget != null && current.isPresent()
                && current.get().getTarget().currentPosition().distanceToSqr(ourTarget) < 4.0D
                && villager.position().distanceToSqr(ourTarget) > 4.0D) {
            return;
        }

        // LandRandomPos only picks safe LAND spots (no water, lava, fire, cliffs...).
        // If the way is blocked (sea, ravine...), it tries a detour turning 45 or 90 degrees,
        // but it does NOT change its main direction: next step it tries straight out again.
        for (int turn : LEAVE_TURNS) {
            Vec3 tryDirection = direction.yRot((float) Math.toRadians(turn));
            Vec3 farAway = villager.position().add(tryDirection.scale(LEAVE_STEP_BLOCKS));
            Vec3 next = LandRandomPos.getPosTowards(villager, LEAVE_STEP_BLOCKS, 7, farAway);
            if (next != null) {
                walkLeaving(villager, next);
                return;
            }
        }
        // No dry land that way (a village on islands, a lake...): it swims straight across.
        // Villagers can swim, so we aim at the surface 16 blocks ahead, water or not.
        Vec3 ahead = villager.position().add(direction.scale(LEAVE_STEP_BLOCKS));
        int x = Mth.floor(ahead.x);
        int z = Mth.floor(ahead.z);
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        walkLeaving(villager, Vec3.atBottomCenterOf(new BlockPos(x, y, z)));
    }

    private static void walkLeaving(Villager villager, Vec3 spot) {
        villager.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(spot, FLEE_SPEED, 1));
        LEAVE_TARGET.put(villager, spot);
    }

    /** How far it looks for the village bell when it doesn't remember one. */
    private static final int FIND_BELL_RANGE = 48;

    /**
     * Its way out (decided once): away from the village bell. If it doesn't remember the bell
     * (a new villager, a summoned one...), it looks for the nearest bell around; with no bell at all,
     * away from the nearest player (the one chasing it); with nobody around, a random direction.
     */
    private static Vec3 leaveDirection(Villager villager, ServerLevel level) {
        Vec3 from = villager.getBrain().getMemory(MemoryModuleType.MEETING_POINT)
                .filter(bell -> bell.dimension() == level.dimension())
                .map(bell -> Vec3.atCenterOf(bell.pos()))
                .or(() -> level.getPoiManager().findClosest(type -> type.is(PoiTypes.MEETING),
                                villager.blockPosition(), FIND_BELL_RANGE, PoiManager.Occupancy.ANY)
                        .map(Vec3::atCenterOf))
                .or(() -> Optional.ofNullable(level.getNearestPlayer(villager, LEAVE_DESPAWN_DISTANCE))
                        .map(Entity::position))
                .orElse(null);

        Vec3 out = from == null ? Vec3.ZERO : villager.position().subtract(from).multiply(1, 0, 1);
        if (out.lengthSqr() < 1.0E-4) {
            double angle = villager.getRandom().nextDouble() * Math.PI * 2;
            out = new Vec3(Math.cos(angle), 0, Math.sin(angle));
        }
        return out.normalize();
    }

    /** Everything that ties a villager to its village (vanilla brain memories). */
    private static final List<MemoryModuleType<GlobalPos>> VILLAGE_MEMORIES = List.of(MemoryModuleType.HOME,
            MemoryModuleType.JOB_SITE, MemoryModuleType.POTENTIAL_JOB_SITE, MemoryModuleType.MEETING_POINT);

    /**
     * An escaped villager doesn't care about the village anymore: it frees its bed, workstation and
     * bell spot (for the others) and forgets them, so vanilla's brain never sends it to work, to bed
     * or to the meeting again. Checked every time (every 5 ticks): if the brain grabs a new bed or
     * workstation on its way out, it lets it go again. And it doesn't talk to anyone
     * (the other villagers can't talk to it either: onShunTick).
     */
    private static void forgetVillage(Villager villager, ServerLevel level) {
        releasePois(villager, level);
        var brain = villager.getBrain();
        for (MemoryModuleType<GlobalPos> type : VILLAGE_MEMORIES) {
            brain.eraseMemory(type);
        }
        brain.eraseMemory(MemoryModuleType.INTERACTION_TARGET);
    }

    /** True if it escaped and is leaving the village forever. */
    public static boolean isLeaving(Villager villager) {
        return villager.hasData(ModAttachments.ILLEGAL_CHASE) && chase(villager).leaving();
    }

    /**
     * Frees its bed, workstation and bell spot, so other villagers can use them
     * (same as vanilla does when a villager dies: PoiManager.release on each of its POI memories).
     */
    private static void releasePois(Villager villager, ServerLevel level) {
        for (MemoryModuleType<GlobalPos> type : VILLAGE_MEMORIES) {
            villager.getBrain().getMemory(type)
                    .filter(pos -> pos.dimension() == level.dimension())
                    .ifPresent(pos -> level.getPoiManager().release(pos.pos()));
        }
    }

    /** Walks to its bed (vanilla "home" memory). Without a home it just stays where it is. */
    private static void goHome(Villager villager, ServerLevel level) {
        villager.getBrain().getMemory(MemoryModuleType.HOME)
                .filter(home -> home.dimension() == level.dimension())
                .ifPresent(home -> villager.getBrain().setMemory(MemoryModuleType.WALK_TARGET,
                        new WalkTarget(home.pos(), FLEE_SPEED, 1)));
    }

    /** The chase ended (caught or escaped): the colleagues around can come out of their homes. */
    private static void stopHiding(Villager wanted, ServerLevel level) {
        for (Villager colleague : level.getEntitiesOfClass(Villager.class,
                wanted.getBoundingBox().inflate(COLLEAGUES_RANGE), v -> v != wanted)) {
            HIDING_UNTIL.remove(colleague);
        }
    }

    /** Wanted villagers run away from players, iron golems and legal villagers. */
    private static boolean isChaser(Entity entity) {
        return (entity instanceof Player player && !player.isSpectator() && !player.isCreative())
                || IllegalVillagers.isWitness(entity);
    }

    // =========================================================================
    // Running away, FOCUSED
    //
    // Before: every 5 ticks it picked a NEW random spot (zig-zag, hesitating), and between those
    // updates vanilla's brain sneaked in its own walk targets (going to work, strolling, chatting).
    // Now: it picks ONE escape spot and sticks to it until it gets there (or the threat cuts it off),
    // and if vanilla's brain changes its walk target, we put ours back right away.
    // A WANTED villager also keeps running from the last place it saw a chaser,
    // instead of calming down as soon as you're a bit far away.
    // =========================================================================

    /** Threats are seen this far. */
    private static final double THREAT_RANGE = 16.0D;
    /** A WANTED villager is more paranoid: it sees chasers from further away. */
    private static final double WANTED_THREAT_RANGE = 24.0D;
    /** Each escape run goes about this far. */
    private static final int FLEE_DISTANCE = 16;
    /** "Got there" = this close to its escape spot (squared: 2 blocks). */
    private static final double REACHED_SQR = 4.0D;

    /** The escape spot each villager is running to right now. */
    private static final Map<Villager, Vec3> FLEE_TARGET = new WeakHashMap<>();
    /** Where it last saw a chaser (a wanted villager keeps running away from there). */
    private static final Map<Villager, Vec3> LAST_THREAT = new WeakHashMap<>();

    /** Scared / avoiding a player: runs while the threat is close, calms down when it's gone. */
    private static void runAway(Villager villager, Predicate<Entity> isThreat, float speed) {
        runAway(villager, isThreat, speed, THREAT_RANGE, false);
    }

    /**
     * Runs away from the nearest threat, like vanilla panic but focused on ONE escape spot.
     * @param keepRunning true (wanted): with no threat in sight, keeps running away from where it last saw one
     */
    private static void runAway(Villager villager, Predicate<Entity> isThreat, float speed, double range, boolean keepRunning) {
        Optional<Entity> threat = villager.level().getEntities(villager,
                        villager.getBoundingBox().inflate(range), isThreat).stream()
                .min(Comparator.comparingDouble(villager::distanceToSqr));

        Vec3 danger;
        if (threat.isPresent()) {
            danger = threat.get().position();
            LAST_THREAT.put(villager, danger);
        } else {
            danger = keepRunning ? LAST_THREAT.get(villager) : null;
        }
        if (danger == null) {
            calmDown(villager); // nobody around: it can relax
            return;
        }

        var brain = villager.getBrain();
        // No time for chatting, looking at rubies or anything else: RUN
        brain.eraseMemory(MemoryModuleType.INTERACTION_TARGET);
        if (villager.isSleeping()) villager.stopSleeping();

        Vec3 target = FLEE_TARGET.get(villager);
        Vec3 here = villager.position();
        boolean needNewSpot = target == null
                || here.distanceToSqr(target) < REACHED_SQR            // got there: next run
                || danger.distanceToSqr(target) < here.distanceToSqr(target); // the threat is closer to the spot: cut off!
        if (needNewSpot) {
            target = LandRandomPos.getPosAway(villager, FLEE_DISTANCE, 7, danger);
            if (target == null) return; // nowhere safe this tick: try again in 5 ticks
            FLEE_TARGET.put(villager, target);
        }

        // Vanilla's brain may have replaced our walk target (work, stroll...): put ours back
        Optional<WalkTarget> current = brain.getMemory(MemoryModuleType.WALK_TARGET);
        boolean ours = current.isPresent()
                && current.get().getTarget().currentPosition().distanceToSqr(target) < 1.0D
                && current.get().getSpeedModifier() == speed;
        if (!ours) {
            brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(target, speed, 0));
        }
    }

    /** Forgets its escape (not running anymore). */
    private static void calmDown(Villager villager) {
        FLEE_TARGET.remove(villager);
        LAST_THREAT.remove(villager);
    }

    /** True if it's riding a boat (or a raft): checked by name so it works with every boat type. */
    private static boolean isInBoat(Villager villager) {
        Entity vehicle = villager.getVehicle();
        if (vehicle == null) return false;
        String path = BuiltInRegistries.ENTITY_TYPE.getKey(vehicle.getType()).getPath();
        return path.endsWith("boat") || path.endsWith("raft");
    }

    // =========================================================================
    // Caught / escaped
    // =========================================================================

    /** Players this close to the caught villager get the "Caught Red-Handed" advancement. */
    private static final double CAUGHT_ADVANCEMENT_RANGE = 16.0;

    /** Caught! It stops being illegal forever, and the snitch gets the reward. */
    private static void caught(Villager villager, ServerLevel level, IllegalChaseData chaseBefore) {
        stopHiding(villager, level);
        calmDown(villager);
        // Shunned: the village won't talk to it for a long time (it stays shunned after being "legal")
        IllegalChaseData chase = chaseBefore.withShunnedUntil(
                level.getGameTime() + IllegalVillagerConfig.SHUN_CAUGHT_TICKS.get());
        // Not illegal anymore: no ruby trades, no badge, no secret
        villager.getOffers().removeIf(IllegalVillagers::isIllegalOffer);
        villager.setData(ModAttachments.ILLEGAL_VILLAGER, IllegalVillagerData.LEGAL);
        villager.setData(ModAttachments.SECRET_REVEALED_TO, Optional.empty());
        villager.setData(ModAttachments.ILLEGAL_CHASE, chase.withoutWanted());
        villager.removeEffect(MobEffects.GLOWING);
        villager.playSound(SoundEvents.VILLAGER_NO, 1.0F, 1.0F);
        // Furious: angry particles for a few seconds
        CelebrationEvents.beAngry(villager, CelebrationEvents.CAPTURED_ANGRY_TICKS);

        // The legal villagers around celebrate like after winning a raid (jumps + some fireworks)
        // and come to give gifts to the snitch, like vanilla villagers do to the Hero of the Village
        for (Villager neighbour : legalNeighbours(level, villager)) {
            CelebrationEvents.celebrate(neighbour, CelebrationEvents.CAPTURE_CELEBRATION_TICKS,
                    CelebrationEvents.CAPTURE_FIREWORK_CHANCE);
            if (chase.snitch().isPresent() && !neighbour.isSleeping()) {
                CelebrationEvents.giveGift(neighbour, chase.snitch().get());
            } else if (!neighbour.isSleeping() && !neighbour.isBaby()) {
                neighbour.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(villager, HELPER_SPEED, 3));
            }
        }
        // Advancement "Caught Red-Handed": every player around (the boat, the chase...) gets it
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, villager.getBoundingBox().inflate(CAUGHT_ADVANCEMENT_RANGE))) {
            ModAdvancements.award(player, ModAdvancements.CAUGHT_RED_HANDED);
        }
        chase.snitch().map(level::getPlayerByUUID).ifPresent(snitch -> {
            if (snitch instanceof ServerPlayer serverSnitch) {
                ModAdvancements.award(serverSnitch, ModAdvancements.CAUGHT_RED_HANDED);
            }
        });

        // The iron golems come too (and stop chasing)
        for (IronGolem golem : level.getEntitiesOfClass(IronGolem.class, villager.getBoundingBox().inflate(BELL_RANGE))) {
            golem.getNavigation().moveTo(villager, 1.0D);
        }

        chase.snitch().ifPresent(snitchId -> {
            // The village thanks the snitch EXACTLY like curing a zombie villager:
            // vanilla gives MAJOR_POSITIVE 20 + MINOR_POSITIVE 25 -> big discounts
            for (Villager neighbour : legalNeighbours(level, villager)) {
                neighbour.getGossips().add(snitchId, GossipType.MAJOR_POSITIVE, 20);
                neighbour.getGossips().add(snitchId, GossipType.MINOR_POSITIVE, 25);
            }
            Player snitch = level.getPlayerByUUID(snitchId);
            if (snitch != null) {
                snitch.addEffect(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, HERO_REWARD_TICKS, 0));
            }
        });
    }

    /** Legal villagers within the bell range (48 blocks), without the caught one. */
    private static List<Villager> legalNeighbours(ServerLevel level, Villager caught) {
        return level.getEntitiesOfClass(Villager.class, caught.getBoundingBox().inflate(BELL_RANGE),
                v -> v != caught && !IllegalVillagers.isIllegal(v));
    }

    /** Escaped: it's not wanted anymore, but it will never trust the snitch again (big bad reputation). */
    private static void escaped(Villager villager, ServerLevel level, IllegalChaseData chase) {
        stopHiding(villager, level);
        calmDown(villager);
        // Escaped: it leaves the village forever (it disappears when no player is close)
        villager.setData(ModAttachments.ILLEGAL_CHASE, chase.withLeaving());
        villager.removeEffect(MobEffects.GLOWING);
        villager.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);

        // Everyone is angry for 2 minutes (angry clouds + no trades): the escaped one,
        // and the legal villagers that couldn't catch it
        CelebrationEvents.sulk(villager, CelebrationEvents.CAPTURED_ANGRY_TICKS);
        for (Villager neighbour : legalNeighbours(level, villager)) {
            CelebrationEvents.sulk(neighbour, CelebrationEvents.CAPTURED_ANGRY_TICKS);
        }

        chase.snitch().ifPresent(snitchId -> villager.getGossips().add(snitchId, GossipType.MAJOR_NEGATIVE, 25));
    }

    // =========================================================================
    // Bell resonance, like vanilla BellBlockEntity with raiders:
    // ~5 ticks after ringing -> BELL_RESONATE sound starts; 40 ticks after ringing -> they glow.
    // =========================================================================

    private static final int RESONATE_SOUND_DELAY_TICKS = 5;
    private static final int RESONATE_GLOW_DELAY_TICKS = 40;

    /** Glow time: 10 seconds (longer than vanilla's 3 s, so you can follow it). */
    private static final int BELL_GLOW_TICKS = 200;

    private record BellKey(ServerLevel level, BlockPos pos) {}

    /** A ringing bell: when it was rung, and if the resonance sound already played. */
    private static final class Resonance {
        final long rungAt;
        boolean soundPlayed;

        Resonance(long rungAt) {
            this.rungAt = rungAt;
        }
    }

    private static final Map<BellKey, Resonance> PENDING_RESONANCE = new HashMap<>();

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || PENDING_RESONANCE.isEmpty()) return;

        Iterator<Map.Entry<BellKey, Resonance>> it = PENDING_RESONANCE.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<BellKey, Resonance> entry = it.next();
            if (entry.getKey().level() != level) continue;
            BlockPos bell = entry.getKey().pos();
            Resonance resonance = entry.getValue();
            long elapsed = level.getGameTime() - resonance.rungAt;

            // The long "resonate" sound starts right after the ring (like vanilla)
            if (!resonance.soundPlayed && elapsed >= RESONATE_SOUND_DELAY_TICKS) {
                resonance.soundPlayed = true;
                level.playSound(null, bell, SoundEvents.BELL_RESONATE, SoundSource.BLOCKS, 1.0F, 1.0F);
            }

            // When the resonance ends: the wanted villagers glow
            if (elapsed >= RESONATE_GLOW_DELAY_TICKS) {
                it.remove();
                for (Villager wanted : level.getEntitiesOfClass(Villager.class, new AABB(bell).inflate(BELL_RANGE),
                        v -> IllegalVillagers.isIllegal(v) && chase(v).isWanted())) {
                    wanted.addEffect(new MobEffectInstance(MobEffects.GLOWING, BELL_GLOW_TICKS, 0, false, false));
                }
            }
        }
    }

    // =========================================================================
    // Snitch: ring a bell near an illegal villager whose secret you know
    // =========================================================================
    @SubscribeEvent
    public static void onRingBell(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        if (!level.getBlockState(event.getPos()).is(Blocks.BELL)) return;

        Player player = event.getEntity();
        BlockPos bell = event.getPos();
        int wantedTicks = IllegalVillagerConfig.WANTED_TICKS.get();

        // To snitch you have to ring a bell right next to it (4 blocks)
        List<Villager> suspects = level.getEntitiesOfClass(Villager.class, new AABB(bell).inflate(SNITCH_RANGE),
                v -> IllegalVillagers.isIllegal(v)
                        && IllegalVillagers.data(v).secretUnlocked()
                        && chase(v).knows(player.getUUID())
                        && !chase(v).isWanted());

        for (Villager suspect : suspects) {
            suspect.setData(ModAttachments.ILLEGAL_CHASE,
                    chase(suspect).withWanted(level.getGameTime() + wantedTicks, player.getUUID()));
            shun(suspect, level, IllegalVillagerConfig.SHUN_WANTED_TICKS.get());
            // Stop showing its secret right away
            suspect.setData(ModAttachments.SECRET_REVEALED_TO, Optional.empty());
            if (suspect.getTradingPlayer() != null) {
                suspect.getTradingPlayer().closeContainer();
            }
        }

        // Like the bell with raiders: if there's any wanted villager around (new or already wanted),
        // the bell resonates and 2 seconds later they glow for 10 seconds. Ring it again to find them again!
        boolean anyWanted = !level.getEntitiesOfClass(Villager.class, new AABB(bell).inflate(BELL_RANGE),
                v -> IllegalVillagers.isIllegal(v) && chase(v).isWanted()).isEmpty();
        if (anyWanted) {
            PENDING_RESONANCE.putIfAbsent(new BellKey(level, bell.immutable()), new Resonance(level.getGameTime()));
        }

        if (!suspects.isEmpty()) {
            // Word spreads among the traffickers: the other illegal villagers avoid the snitch for a while
            long avoidUntil = level.getGameTime() + IllegalVillagerConfig.AVOID_TICKS.get();
            for (Villager colleague : level.getEntitiesOfClass(Villager.class, new AABB(bell).inflate(COLLEAGUES_RANGE),
                    v -> IllegalVillagers.isIllegal(v) && !suspects.contains(v))) {
                colleague.setData(ModAttachments.ILLEGAL_CHASE, chase(colleague).withAvoided(player.getUUID(), avoidUntil));
                // ...and hide at home, sweating, while the chase lasts
                HIDING_UNTIL.put(colleague, level.getGameTime() + wantedTicks);
            }
        }
    }

    // =========================================================================
    // While it's on the run it doesn't want to trade with anyone
    // =========================================================================
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getLevel() instanceof ServerLevel)) return;
        if (!(event.getTarget() instanceof Villager villager)) return;
        boolean onTheRun = IllegalVillagers.isIllegal(villager) && isOnTheRun(villager);
        boolean avoidsYou = avoids(villager, event.getEntity());
        boolean sulking = CelebrationEvents.isSulking(villager); // the illagers lost the raid / a trafficker escaped
        if (!onTheRun && !avoidsYou && !sulking) return;

        // Shakes its head, like vanilla when it doesn't want to trade
        villager.setUnhappyCounter(40);
        villager.playSound(SoundEvents.VILLAGER_NO, 1.0F, 1.0F);
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }
}