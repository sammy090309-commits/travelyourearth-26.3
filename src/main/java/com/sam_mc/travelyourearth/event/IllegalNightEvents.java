package com.sam_mc.travelyourearth.event;

import com.sam_mc.travelyourearth.TravelYourEarth;
import com.sam_mc.travelyourearth.attachment.ModAttachments;
import com.sam_mc.travelyourearth.villager.IllegalVillagers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Part 3: traffickers at night.
 *
 * 1 night of every 3 (when the other villagers go to sleep) ALL illegal villagers (any level)
 * don't sleep: they drink an Invisibility potion EXACTLY like the Wandering Trader (potion in the hand,
 * drinking animation, swirl particles) and sneak to the center of the
 * village (the bell = their meeting point) to do "their things" with the other traffickers:
 * they chat in fixed pairs that take turns, and share gossip about the players they trust.
 *
 * ONLY MASTERS (secret phase unlocked) can trade: if a player they trust gets closer than 8 blocks,
 * they drink a bucket of milk (like the Wandering Trader in the morning),
 * become visible, stop and show their illegal trades WITHOUT needing a ruby
 * (the night reveal rules are in IllegalVillagerEvents.updateSecretReveal).
 * When the player leaves, they drink invisibility again. Lower levels ignore the player and keep
 * sneaking around invisible. In the morning everyone drinks milk and goes back to normal.
 *
 * How the brain is "convinced": vanilla villagers only walk home to sleep when they have no
 * walk target. We keep giving them a walk target near the bell all night long.
 */
@EventBusSubscriber(modid = TravelYourEarth.MODID)
public class IllegalNightEvents {

    /** Invisibility (3 minutes, from the vanilla potion) is drunk again before it runs out. */
    private static final int REFRESH_BELOW_TICKS = 100;

    /** They wander around the bell within this distance. */
    private static final int CENTER_RADIUS = 2;
    private static final double MAX_DISTANCE_FROM_CENTER = 4.0D;

    /** Two traffickers this close "chat": they stop, look at each other and whisper. */
    private static final double CHAT_DISTANCE = 5.0D;

    /** Slow and sneaky. */
    private static final float SNEAK_SPEED = 0.45F;

    /** Villagers doing their night shift right now (to clean up in the morning). */
    private static final Map<Villager, Boolean> ON_NIGHT_SHIFT = new WeakHashMap<>();

    private IllegalNightEvents() {}

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Villager villager)) return;
        if (!(villager.level() instanceof ServerLevel level)) return;
        if (villager.tickCount % 2 != 0) return;

        // A potion/milk finished: put the empty hand back (like the Wandering Trader)
        finishDrinkIfDone(villager);

        boolean onTheRun = IllegalChaseEvents.isOnTheRun(villager);
        // Every illegal villager goes out on the trafficking nights (any level)
        boolean nightShift = IllegalVillagers.isOutAtNight(villager) && !onTheRun && !villager.isBaby();

        // Morning: drinks milk and goes back to normal. On the run (glowing): no time to drink
        if (!nightShift) {
            breakUp(villager); // no more chatting: its partner looks for someone else
            if (ON_NIGHT_SHIFT.containsKey(villager)) {
                if (!villager.hasEffect(MobEffects.INVISIBILITY)) {
                    ON_NIGHT_SHIFT.remove(villager);
                } else if (onTheRun) {
                    villager.removeEffect(MobEffects.INVISIBILITY);
                    ON_NIGHT_SHIFT.remove(villager);
                } else {
                    startDrinking(villager, new ItemStack(Items.MILK_BUCKET));
                }
            }
            return;
        }
        ON_NIGHT_SHIFT.put(villager, Boolean.TRUE);

        // Traffickers don't sleep at night
        if (villager.isSleeping()) {
            villager.stopSleeping();
        }

        Brain<Villager> brain = villager.getBrain();
        // Only masters attend players: the rest have nothing to sell yet (no milk, they stay invisible)
        Player client = IllegalVillagers.isNightTrafficker(villager) ? revealedPlayer(villager, level) : null;

        // A trusted player is close: drinks a bucket of milk, stops and attends them
        if (client != null) {
            if (villager.hasEffect(MobEffects.INVISIBILITY)) {
                startDrinking(villager, new ItemStack(Items.MILK_BUCKET));
            }
            brain.eraseMemory(MemoryModuleType.WALK_TARGET);
            brain.setMemoryWithExpiry(MemoryModuleType.LOOK_TARGET, new EntityTracker(client, true), 10L);
            return;
        }

        // Drinks an Invisibility potion (when it has none, or it's about to end)... but ONLY when
        // nobody is watching: drinking a potion in the middle of the village would give it away
        MobEffectInstance invisibility = villager.getEffect(MobEffects.INVISIBILITY);
        if (invisibility == null || invisibility.getDuration() < REFRESH_BELOW_TICKS) {
            if (canDrinkUnseen(villager, level)) {
                startDrinking(villager, PotionContents.createItemStack(Items.POTION, Potions.INVISIBILITY));
            } else if (invisibility == null) {
                // Visible and watched: acts normal (goes towards its bed) and waits until nobody is looking
                actNormal(villager, level, brain);
                return;
            }
        }
        Villager partner = findPartner(villager, level);
        if (partner != null && villager.distanceTo(partner) < CHAT_DISTANCE
                && villager.blockPosition().closerThan(meetingPoint(villager, level), MAX_DISTANCE_FROM_CENTER + 2)) {
            chatWith(villager, partner, brain, level);
        } else {
            sneakToCenter(villager, level, brain);
            // With a partner: looks at it while they meet. Without one: waits its turn
            // at the bell, watching the others chat
            Villager watch = partner != null ? partner : nearestOutAtNight(villager, level);
            if (watch != null) {
                brain.setMemoryWithExpiry(MemoryModuleType.LOOK_TARGET, new EntityTracker(watch, true), 10L);
            }
        }
    }

    /** The player it's showing its secret to right now (null if none). */
    private static Player revealedPlayer(Villager villager, ServerLevel level) {
        Optional<UUID> revealedTo = villager.getData(ModAttachments.SECRET_REVEALED_TO);
        return revealedTo.map(level::getPlayerByUUID).orElse(null);
    }

    // =========================================================================
    // Not in front of everyone!
    // =========================================================================

    /** Players this close that it doesn't trust would see it drinking. */
    private static final double DRINK_WITNESS_RANGE = 16.0D;

    /**
     * True if nobody can see it drinking: no iron golem and no AWAKE legal villager within 16 blocks
     * (IllegalVillagers.isAlone), and no player it doesn't trust within 16 blocks.
     * Other illegal villagers and players it trusts are "in the business": they don't count.
     */
    private static boolean canDrinkUnseen(Villager villager, ServerLevel level) {
        if (!IllegalVillagers.isAlone(villager)) return false;
        return level.getEntitiesOfClass(Player.class, villager.getBoundingBox().inflate(DRINK_WITNESS_RANGE),
                player -> !player.isSpectator() && !IllegalVillagers.trusts(villager, player)).isEmpty();
    }

    /**
     * While someone is watching, it pretends it's going to bed like everyone else (walks to its bed,
     * without sleeping). As soon as nobody is looking, it drinks the potion and sneaks out.
     * Without a bed it just stands still.
     */
    private static void actNormal(Villager villager, ServerLevel level, Brain<Villager> brain) {
        Optional<BlockPos> bed = brain.getMemory(MemoryModuleType.HOME)
                .filter(home -> home.dimension() == level.dimension())
                .map(GlobalPos::pos);
        if (bed.isPresent() && !villager.blockPosition().closerThan(bed.get(), 2.0D)) {
            brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(bed.get(), 0.5F, 1));
        } else {
            brain.eraseMemory(MemoryModuleType.WALK_TARGET);
        }
    }

    // =========================================================================
    // Drinking, exactly like the Wandering Trader (vanilla UseItemGoal):
    // the item goes to its hand, it "uses" it (drinking animation + gulp sounds),
    // and when it finishes, the item itself does its job:
    // - the Invisibility potion gives the effect (with its swirl particles),
    // - the milk bucket removes all effects.
    // =========================================================================

    /** Villagers drinking right now (to empty their hand when they finish). */
    private static final Map<Villager, Boolean> DRINKING = new WeakHashMap<>();

    private static void startDrinking(Villager villager, ItemStack drink) {
        if (villager.isUsingItem() || DRINKING.containsKey(villager)) return; // already drinking
        villager.setItemSlot(EquipmentSlot.MAINHAND, drink);
        villager.startUsingItem(InteractionHand.MAIN_HAND);
        DRINKING.put(villager, drink.is(Items.MILK_BUCKET));
    }

    private static void finishDrinkIfDone(Villager villager) {
        Boolean milk = DRINKING.get(villager);
        if (milk == null || villager.isUsingItem()) return;

        DRINKING.remove(villager);
        // Only the normal drinking sound (from the potion/milk itself): no Wandering Trader voice
        villager.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
    }

    // =========================================================================
    // Walking
    // =========================================================================

    /**
     * Keeps a walk target near the bell all night (so the brain never sends it home to sleep).
     * Far away: walks to the bell. Already there: strolls slowly around it.
     */
    private static void sneakToCenter(Villager villager, ServerLevel level, Brain<Villager> brain) {
        BlockPos center = meetingPoint(villager, level);

        Optional<WalkTarget> current = brain.getMemory(MemoryModuleType.WALK_TARGET);
        boolean goingToCenter = current.isPresent()
                && current.get().getTarget().currentBlockPosition().closerThan(center, MAX_DISTANCE_FROM_CENTER);
        if (goingToCenter) return; // already on its way (or strolling around the bell)

        RandomSource random = villager.getRandom();
        BlockPos spot = center.offset(
                random.nextInt(CENTER_RADIUS * 2 + 1) - CENTER_RADIUS,
                0,
                random.nextInt(CENTER_RADIUS * 2 + 1) - CENTER_RADIUS);
        brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(spot, SNEAK_SPEED, 1));
    }

    /** The village bell (vanilla "meeting point" memory). If it doesn't know one, it stays around its spot. */
    private static BlockPos meetingPoint(Villager villager, ServerLevel level) {
        return villager.getBrain().getMemory(MemoryModuleType.MEETING_POINT)
                .filter(pos -> pos.dimension() == level.dimension())
                .map(GlobalPos::pos)
                .orElseGet(villager::blockPosition);
    }

    // =========================================================================
    // Night chats: FIXED PAIRS that take turns
    //
    // Each trafficker picks a free partner (one that isn't chatting with someone else) and they
    // stay together as a pair. The ones left over wait their turn at the bell, watching.
    // After CHAT_LENGTH the pair splits up and each looks for someone NEW (if there is anyone),
    // so with 3 or more everybody ends up talking to everybody during the night.
    // =========================================================================

    /** Search range for partners. */
    private static final double PARTNER_RANGE = 16.0D;
    /** How long a conversation lasts once they're together (1 minute). */
    private static final long CHAT_LENGTH = 1200L;
    /** After this much chatting they've shared their gossip (10 seconds). */
    private static final long GOSSIP_AFTER = 200L;

    /** Current partner of each trafficker (always both ways: A -> B and B -> A). */
    private static final Map<Villager, Villager> PARTNERS = new WeakHashMap<>();
    /** Its previous partner (to prefer someone new next time). */
    private static final Map<Villager, Villager> LAST_PARTNER = new WeakHashMap<>();
    /** Game time when the current conversation really started (when they got together). */
    private static final Map<Villager, Long> CHAT_STARTED = new WeakHashMap<>();
    /** Already shared its gossip in the current conversation. */
    private static final Map<Villager, Boolean> GOSSIP_SHARED = new WeakHashMap<>();

    /** True if "other" can be out chatting with "villager" right now. */
    private static boolean canChat(Villager villager, Villager other) {
        return other != villager
                && other.isAlive()
                && other.level() == villager.level()
                && !other.isBaby()
                && IllegalVillagers.isOutAtNight(other)
                && !IllegalChaseEvents.isOnTheRun(other)
                && villager.distanceToSqr(other) <= PARTNER_RANGE * PARTNER_RANGE;
    }

    /** Its partner if the pair is still valid (both ways), or null. Doesn't change anything. */
    private static Villager currentPartner(Villager villager) {
        Villager partner = PARTNERS.get(villager);
        if (partner == null || PARTNERS.get(partner) != villager || !canChat(villager, partner)) return null;
        return partner;
    }

    /** Ends its conversation (if any): both are free again. */
    private static void breakUp(Villager villager) {
        Villager partner = PARTNERS.remove(villager);
        CHAT_STARTED.remove(villager);
        GOSSIP_SHARED.remove(villager);
        if (partner == null) return;
        LAST_PARTNER.put(villager, partner);
        if (PARTNERS.get(partner) == villager) {
            PARTNERS.remove(partner);
            CHAT_STARTED.remove(partner);
            GOSSIP_SHARED.remove(partner);
            LAST_PARTNER.put(partner, villager);
        }
    }

    /**
     * Its partner for tonight's chat: keeps the current one until the conversation is over,
     * then picks the nearest FREE trafficker (preferring someone it didn't just talk to).
     * Null = nobody free: it waits its turn.
     */
    private static Villager findPartner(Villager villager, ServerLevel level) {
        Villager partner = currentPartner(villager);
        if (partner != null) {
            Long started = CHAT_STARTED.get(villager);
            if (started == null || level.getGameTime() - started < CHAT_LENGTH) return partner;
        }
        breakUp(villager); // conversation over (or the partner left): free again

        Villager last = LAST_PARTNER.get(villager);
        List<Villager> free = level.getEntitiesOfClass(Villager.class, villager.getBoundingBox().inflate(PARTNER_RANGE),
                other -> canChat(villager, other) && currentPartner(other) == null);
        if (free.isEmpty()) return null;

        // Someone new first; the last partner only if it's the only one around
        Villager chosen = free.stream()
                .filter(other -> other != last)
                .min(Comparator.comparingDouble(villager::distanceToSqr))
                .orElse(free.getFirst());

        breakUp(chosen); // just in case it pointed to someone who left
        PARTNERS.put(villager, chosen);
        PARTNERS.put(chosen, villager);
        return chosen;
    }

    /** The nearest other trafficker out tonight (to watch while waiting its turn), or null. */
    private static Villager nearestOutAtNight(Villager villager, ServerLevel level) {
        return level.getEntitiesOfClass(Villager.class, villager.getBoundingBox().inflate(PARTNER_RANGE),
                        other -> canChat(villager, other))
                .stream()
                .min(Comparator.comparingDouble(villager::distanceToSqr))
                .orElse(null);
    }

    /**
     * "Doing their things": the two traffickers stay together at the bell, look at each other
     * and whisper (quiet, low villager sounds), like vanilla villagers chatting at a meeting.
     * After 10 seconds together they share their gossip about players (shareGossip).
     */
    private static void chatWith(Villager villager, Villager partner, Brain<Villager> brain, ServerLevel level) {
        long now = level.getGameTime();
        CHAT_STARTED.putIfAbsent(villager, now);

        brain.setMemoryWithExpiry(MemoryModuleType.LOOK_TARGET, new EntityTracker(partner, true), 10L);
        // Stays next to its partner (a walk target keeps the brain from sending it home to sleep)
        brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(partner, SNEAK_SPEED, 2));
        if (villager.getRandom().nextInt(25) == 0) {
            villager.playSound(SoundEvents.VILLAGER_AMBIENT, 0.4F, 0.8F + villager.getRandom().nextFloat() * 0.2F);
        }

        if (!GOSSIP_SHARED.containsKey(villager) && now - CHAT_STARTED.get(villager) >= GOSSIP_AFTER) {
            GOSSIP_SHARED.put(villager, Boolean.TRUE);
            shareGossip(villager, partner, level);
        }
    }

    // =========================================================================
    // Gossip: "this one is trustworthy, I've done business with them"
    // =========================================================================

    /** Reputation the partner's recommendation gives per conversation (MINOR_POSITIVE, weight 1). */
    private static final int RECOMMENDATION_REPUTATION = 2;
    /**
     * A recommendation can't get you closer than this to the partner's own trust:
     * partner trust 4 (max) -> up to trust 2. Never enough to see the illegal trades
     * (TRUST_TO_REVEAL = 3): for that you still have to earn it yourself.
     */
    private static final int RECOMMENDATION_GAP = 2;

    /**
     * The villager "listens" to its partner: for every player the partner trusts MORE,
     * the villager starts trusting them a little bit (vanilla MINOR_POSITIVE gossip, which
     * fades by itself over time like any vanilla gossip).
     * Both villagers of the pair call this, so the recommendations go both ways.
     */
    private static void shareGossip(Villager villager, Villager partner, ServerLevel level) {
        for (Player player : level.players()) {
            if (player.isSpectator()) continue;
            if (IllegalChaseEvents.avoids(villager, player)) continue; // a snitch: no recommendation helps

            int partnerTrust = IllegalVillagers.trustLevel(partner, player);
            int ownTrust = IllegalVillagers.trustLevel(villager, player);
            if (ownTrust < partnerTrust - RECOMMENDATION_GAP) {
                villager.getGossips().add(player.getUUID(), GossipType.MINOR_POSITIVE, RECOMMENDATION_REPUTATION);
            }
        }
    }
}