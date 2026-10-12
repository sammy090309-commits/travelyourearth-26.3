package com.sam_mc.travelyourearth.event;

import com.sam_mc.travelyourearth.advancement.ModAdvancements;
import com.sam_mc.travelyourearth.TravelYourEarth;
import com.sam_mc.travelyourearth.attachment.IllegalSlot;
import com.sam_mc.travelyourearth.attachment.IllegalVillagerData;
import com.sam_mc.travelyourearth.attachment.ModAttachments;
import com.sam_mc.travelyourearth.config.IllegalVillagerConfig;
import com.sam_mc.travelyourearth.villager.IllegalTrade;
import com.sam_mc.travelyourearth.villager.IllegalTrades;
import com.sam_mc.travelyourearth.villager.IllegalVillagers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingConversionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.TradeWithVillagerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.WeakHashMap;

/** Events of the illegal trader system (1.2). */
@EventBusSubscriber(modid = TravelYourEarth.MODID)
public class IllegalVillagerEvents {

    /**
     * What we changed in a villager's trade list while a player has its trade screen open.
     * Everything is undone when the screen closes. Key = player.
     *
     * @param villager the villager being traded with
     * @param blocked  illegal offers shown as "out of stock" (not enough XP levels, or Peaceful),
     *                 each with its real uses
     * @param hidden   illegal offers HIDDEN (the secret wasn't revealed to the player). They go back
     *                 to the end of the list, in the same order
     */
    private record Session(Villager villager, List<BlockedOffer> blocked, List<MerchantOffer> hidden) {}

    private record BlockedOffer(MerchantOffer offer, int realUses) {}

    private static final Map<UUID, Session> SESSIONS = new HashMap<>();

    /** Random delay before the secret is shown/hidden: 20-40 ticks = 1-2 seconds. */
    private static final int MIN_REVEAL_DELAY_TICKS = 20;
    private static final int MAX_REVEAL_DELAY_TICKS = 40;

    /** Pending change of SECRET_REVEALED_TO for each villager (weak: forgets removed entities). */
    private static final Map<LivingEntity, RevealTimer> REVEAL_TIMERS = new WeakHashMap<>();

    private static final class RevealTimer {
        Optional<UUID> target = Optional.empty();
        long switchAt = -1L; // -1 = no change pending
    }

    // =========================================================================
    // 2. Villager <-> zombie villager: the "illegal" data belongs to the villager itself
    // =========================================================================
    @SubscribeEvent
    public static void onConversion(LivingConversionEvent.Post event) {
        LivingEntity from = event.getEntity();
        LivingEntity to = event.getOutcome();

        boolean fromVillager = from instanceof Villager || from instanceof ZombieVillager;
        boolean toVillager = to instanceof Villager || to instanceof ZombieVillager;
        if (!fromVillager || !toVillager) return;
        if (!from.hasData(ModAttachments.ILLEGAL_VILLAGER)) return;

        to.setData(ModAttachments.ILLEGAL_VILLAGER, from.getData(ModAttachments.ILLEGAL_VILLAGER));
    }

    // =========================================================================
    // 3. Tick: show/hide the secret, look at rubies, unlock the secret phase,
    //    remove sold out "once per villager" trades
    // =========================================================================
    @SubscribeEvent
    public static void onVillagerTick(EntityTickEvent.Post event) {
        // Show/hide the secret (badge + trades + item in hand), every 5 ticks. Zombie villagers too.
        if ((event.getEntity() instanceof Villager || event.getEntity() instanceof ZombieVillager)
                && event.getEntity() instanceof LivingEntity living
                && living.level() instanceof ServerLevel serverLevel
                && living.tickCount % 5 == 0) {
            updateSecretReveal(living, serverLevel);
        }

        if (!(event.getEntity() instanceof Villager villager)) return;
        if (!(villager.level() instanceof ServerLevel level)) return;

        // 1. Roll the chance of its village, once per villager, when it's an ADULT. Done in the tick
        //    (not when it joins the world) because the world around it is fully loaded by then
        // (Babies wait: it's decided when they grow up)
        if (!villager.isBaby() && !IllegalVillagers.data(villager).rolled()) {
            villager.setData(ModAttachments.ILLEGAL_VILLAGER, IllegalVillagers.roll(villager, level));
        }

        if (!IllegalVillagers.isIllegalActive(villager)) return;

        // Look at players holding a ruby (like piglins with gold), every 5 ticks
        if (villager.tickCount % 5 == 0) {
            lookAtRubyHolder(villager);
        }

        // Every 20 ticks (1 second)
        if (villager.tickCount % 20 == 0) {
            tryUnlockSecretPhase(villager);
            // Nobody trading: safe to change the trade list
            if (villager.getTradingPlayer() == null) {
                restoreLeftovers(villager);
                enforceSlowRestock(villager, level);
                removeSoldOutOnceTrades(villager);
                applyPriceRaises(villager);
            }
        }
    }

    /** When an illegal villager reaches master level, it unlocks 2 DIFFERENT random trades of its list. */
    private static void tryUnlockSecretPhase(Villager villager) {
        IllegalVillagerData data = IllegalVillagers.data(villager);
        if (data.secretUnlocked()) return;
        if (villager.getVillagerData().level() < IllegalVillagers.MASTER_LEVEL) return;
        if (!(villager.level() instanceof ServerLevel level)) return;

        List<IllegalTrade> pool = new ArrayList<>(IllegalTrades.forProfession(villager.getVillagerData().profession()));
        if (pool.isEmpty()) return;

        // Shuffle (Fisher-Yates) and take the first ones that can be created here
        for (int i = pool.size() - 1; i > 0; i--) {
            int j = villager.getRandom().nextInt(i + 1);
            IllegalTrade tmp = pool.get(i);
            pool.set(i, pool.get(j));
            pool.set(j, tmp);
        }

        List<MerchantOffer> offers = new ArrayList<>();
        List<IllegalSlot> slots = new ArrayList<>();
        for (IllegalTrade trade : pool) {
            if (offers.size() >= IllegalVillagers.ILLEGAL_TRADES_PER_VILLAGER) break;
            // Some trades can't be made here (e.g. a map with no structure nearby): skip them
            trade.factory().create(level, villager.blockPosition()).ifPresent(deal -> {
                offers.add(deal.offer());
                slots.add(new IllegalSlot(deal.rarity().levelCost(), trade.oncePerVillager(),
                        deal.offer().getBaseCostA().getCount(), 0));
            });
        }
        if (offers.isEmpty()) return; // nothing works here: try again later

        // Added at the END of the list, in the same order as the slots
        villager.getOffers().addAll(offers);
        villager.setData(ModAttachments.ILLEGAL_VILLAGER, data.withSecretUnlocked(slots, level.getGameTime()));
    }

    /**
     * "Once per villager" trades that are sold out disappear forever (offer + its slot).
     * Only called when nobody has the trade screen open (changing the list mid-trade breaks the screen).
     */
    private static void removeSoldOutOnceTrades(Villager villager) {
        List<MerchantOffer> illegal = IllegalVillagers.illegalOffers(villager);
        IllegalVillagerData data = IllegalVillagers.data(villager);

        // Backwards, so removing one doesn't move the index of the next ones
        for (int i = Math.min(illegal.size(), data.slots().size()) - 1; i >= 0; i--) {
            MerchantOffer offer = illegal.get(i);
            if (data.slots().get(i).oncePerVillager() && offer.isOutOfStock()) {
                villager.getOffers().remove(offer);
                data = data.withoutSlot(i);
            }
        }
        if (data != IllegalVillagers.data(villager)) {
            villager.setData(ModAttachments.ILLEGAL_VILLAGER, data);
        }
    }

    /**
     * Uses of each illegal offer the last time we looked. If vanilla resets them (restock),
     * we see that the uses went DOWN. Weak: forgets offers that don't exist anymore.
     */
    private static final Map<MerchantOffer, Integer> KNOWN_USES = new WeakHashMap<>();

    /**
     * Slow restock: vanilla restocks ALL trades up to 2 times per day. The illegal ones only restock
     * every 3 days (config: illegalRestockTicks). "Once per villager" trades never restock.
     * If vanilla restocked an illegal offer too early, its uses are put back.
     */
    private static void enforceSlowRestock(Villager villager, ServerLevel level) {
        IllegalVillagerData data = IllegalVillagers.data(villager);
        List<MerchantOffer> illegal = IllegalVillagers.illegalOffers(villager);
        long now = level.getGameTime();
        boolean canRestock = now - data.lastRestock() >= IllegalVillagerConfig.RESTOCK_TICKS.get();
        boolean restocked = false;

        for (int i = 0; i < illegal.size(); i++) {
            MerchantOffer offer = illegal.get(i);
            Integer known = KNOWN_USES.get(offer);
            if (known != null && offer.getUses() < known) {
                boolean once = i < data.slots().size() && data.slots().get(i).oncePerVillager();
                if (canRestock && !once) {
                    restocked = true;      // allowed: keep vanilla's restock
                } else {
                    setUses(offer, known); // too early: put the uses back
                }
            }
            KNOWN_USES.put(offer, offer.getUses());
        }

        if (restocked) {
            villager.setData(ModAttachments.ILLEGAL_VILLAGER, data.withLastRestock(now));
        }
    }

    private static void setUses(MerchantOffer offer, int uses) {
        offer.resetUses();
        for (int i = 0; i < uses; i++) {
            offer.increaseUses();
        }
    }

    /**
     * Updates the price of the illegal offers: +5% per purchase, never lower, max 64 rubies / 16 ruby blocks.
     * MerchantOffer can't change its price, so the offer is replaced by a copy with the new price
     * (same uses, same place in the list). Only when nobody is trading.
     */
    private static void applyPriceRaises(Villager villager) {
        List<MerchantOffer> illegal = IllegalVillagers.illegalOffers(villager);
        List<IllegalSlot> slots = IllegalVillagers.data(villager).slots();
        MerchantOffers offers = villager.getOffers();

        for (int i = 0; i < Math.min(illegal.size(), slots.size()); i++) {
            MerchantOffer offer = illegal.get(i);
            int price = IllegalVillagers.currentPrice(offer, slots.get(i));
            if (price <= 0 || price == offer.getBaseCostA().getCount()) continue;

            MerchantOffer raised = new MerchantOffer(
                    new ItemCost(offer.getBaseCostA().getItem(), price),
                    offer.getItemCostB(),
                    offer.getResult().copy(),
                    offer.getUses(),
                    offer.getMaxUses(),
                    offer.getXp(),
                    offer.getPriceMultiplier(),
                    offer.getDemand());
            offers.set(offers.indexOf(offer), raised);
            KNOWN_USES.put(raised, raised.getUses());
        }
    }

    /**
     * Decides who the villager shows its secret to: the nearest player (16 blocks) holding a ruby,
     * only if the villager is alone. The change waits a random 1-2 s (both ways) and is cancelled
     * if the conditions go back before that. The result is synced to the clients (ruby badge).
     */
    private static void updateSecretReveal(LivingEntity villager, ServerLevel level) {
        IllegalVillagerData data = villager.getData(ModAttachments.ILLEGAL_VILLAGER);
        Optional<UUID> current = villager.getData(ModAttachments.SECRET_REVEALED_TO);

        Optional<UUID> desired = Optional.empty();
        if (data.illegal() && data.secretUnlocked()
                && level.getDifficulty() != Difficulty.PEACEFUL
                && !(villager instanceof Villager onTheRun && IllegalChaseEvents.isOnTheRun(onTheRun))) {
            Player player = null;
            if (villager instanceof Villager trafficker && IllegalVillagers.isNightTrafficker(trafficker)) {
                // Night rules: closer than 8 blocks + it trusts you. No ruby needed
                // (it drinks milk and shows itself: IllegalNightEvents).
                // Nobody else within 8 blocks: no legal villager (even asleep) and no iron golem.
                // Other illegal villagers don't count (they keep the secret).
                if (IllegalVillagers.isAloneAtNight(trafficker)) {
                    player = level.getNearestPlayer(villager.getX(), villager.getY(), villager.getZ(),
                            IllegalVillagers.NIGHT_REVEAL_RANGE,
                            entity -> entity instanceof Player p && !p.isSpectator() && IllegalVillagers.trusts(trafficker, p)
                                    && !IllegalChaseEvents.avoids(trafficker, p));
                }
            } else if (IllegalVillagers.isAlone(villager)) {
                // Day rules: alone (16 blocks) + holds a ruby + the villager trusts them
                // (zombie villagers can't trust: only the ruby)
                player = level.getNearestPlayer(villager.getX(), villager.getY(), villager.getZ(),
                        IllegalVillagers.WITNESS_RANGE,
                        entity -> entity instanceof Player p && !p.isSpectator() && IllegalVillagers.isHoldingRuby(p)
                                && (!(villager instanceof Villager v)
                                || (IllegalVillagers.trusts(v, p) && !IllegalChaseEvents.avoids(v, p))));
            }
            if (player != null) desired = Optional.of(player.getUUID());
        }

        RevealTimer timer = REVEAL_TIMERS.computeIfAbsent(villager, v -> new RevealTimer());
        long now = level.getGameTime();

        if (desired.equals(current)) {
            timer.switchAt = -1L; // nothing to change (or the change was cancelled)
        } else if (timer.switchAt < 0 || !desired.equals(timer.target)) {
            timer.target = desired; // start the countdown
            timer.switchAt = now + MIN_REVEAL_DELAY_TICKS
                    + level.getRandom().nextInt(MAX_REVEAL_DELAY_TICKS - MIN_REVEAL_DELAY_TICKS + 1);
        } else if (now >= timer.switchAt) {
            villager.setData(ModAttachments.SECRET_REVEALED_TO, desired); // time's up: switch (synced)
            timer.switchAt = -1L;
            // From now on this player knows its secret (and can snitch on it with a bell)
            if (villager instanceof Villager v && desired.isPresent()) {
                IllegalChaseEvents.remember(v, desired.get());
            }
        }
    }

    /** How close a ruby has to be for the illegal villager to notice it. */
    private static final double RUBY_NOTICE_RANGE = 10.0D;
    /** It walks towards you until it's this close (then it just stands and stares). */
    private static final int RUBY_STOP_DISTANCE = 3;
    /** Slow, sneaky walk (vanilla villagers walk at about 0.5). */
    private static final float RUBY_WALK_SPEED = 0.45F;

    /**
     * Hint to spot illegal villagers: if a player within 10 blocks holds a ruby (or a ruby block),
     * the illegal villager STOPS what it's doing, walks slowly towards them and stays 3 blocks away
     * staring at the ruby, like a piglin with gold ("psst... is that a ruby?").
     * Normal villagers ignore rubies, so it's easy to tell them apart.
     * Not while it's running away, asleep or trading, nor towards a player it avoids (snitch).
     */
    private static void lookAtRubyHolder(Villager villager) {
        if (villager.isSleeping() || villager.getTradingPlayer() != null || IllegalChaseEvents.isOnTheRun(villager)) return;
        Player player = villager.level().getNearestPlayer(villager, RUBY_NOTICE_RANGE);
        if (player == null || player.isSpectator()) return;
        if (!IllegalVillagers.isHoldingRuby(player)) return;
        if (IllegalChaseEvents.avoids(villager, player)) return;

        // Villagers use a "Brain": we give it a look target and a walk target for half a second
        // (renewed every 5 ticks, so it keeps following while you hold the ruby)
        var brain = villager.getBrain();
        brain.setMemoryWithExpiry(MemoryModuleType.LOOK_TARGET, new EntityTracker(player, true), 10L);
        if (villager.distanceTo(player) > RUBY_STOP_DISTANCE + 0.5F) {
            brain.setMemoryWithExpiry(MemoryModuleType.WALK_TARGET,
                    new WalkTarget(player, RUBY_WALK_SPEED, RUBY_STOP_DISTANCE), 10L);
        } else {
            brain.eraseMemory(MemoryModuleType.WALK_TARGET); // close enough: stand still and stare
        }
    }

    // =========================================================================
    // 4. Before the trade screen opens: hide or block the illegal trades
    // =========================================================================
    @SubscribeEvent
    public static void onInteractVillager(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getLevel() instanceof ServerLevel)) return;
        if (!(event.getTarget() instanceof Villager villager)) return;

        Player player = event.getEntity();
        // Someone else is trading with it: don't touch its list (vanilla won't open the screen anyway)
        if (villager.getTradingPlayer() != null && villager.getTradingPlayer() != player) return;

        // Just in case: undo leftovers of a previous screen of this player
        restore(player);
        // Vanilla may have restocked a moment ago: undo it if it's too early (before the screen opens)
        if (event.getLevel() instanceof ServerLevel serverLevel && IllegalVillagers.isIllegal(villager)) {
            enforceSlowRestock(villager, serverLevel);
        }

        List<MerchantOffer> illegal = IllegalVillagers.illegalOffers(villager);
        if (illegal.isEmpty()) return;

        // Only if the villager is showing you its secret right now (same moment as the ruby badge).
        // If not, ALL the illegal trades are hidden (the smuggler doesn't trust you here)
        if (!IllegalVillagers.isRevealedTo(villager, player)) {
            villager.getOffers().removeAll(illegal);
            session(player, villager).hidden().addAll(illegal);
            return;
        }

        // Blocked (red X) if you can't pay the XP. No chat message: the tooltip of the trade says it
        blockUnaffordable(player, villager);
    }

    private static Session session(Player player, Villager villager) {
        return SESSIONS.computeIfAbsent(player.getUUID(),
                uuid -> new Session(villager, new ArrayList<>(), new ArrayList<>()));
    }

    /**
     * Shows as "out of stock" (red X, like vanilla) every illegal offer the player can't pay
     * with XP levels right now (or all of them in Peaceful). The real uses are restored on close.
     *
     * @return the smallest XP cost among the newly blocked offers (for the message), or 0 if none
     */
    private static int blockUnaffordable(Player player, Villager villager) {
        boolean peaceful = !IllegalVillagers.isIllegalActive(villager);
        int smallestCost = 0;
        boolean blockedAny = false;

        for (MerchantOffer offer : IllegalVillagers.illegalOffers(villager)) {
            if (offer.isOutOfStock()) continue; // really sold out (or already blocked)

            int cost = IllegalVillagers.slotOf(villager, offer).levelCost();
            boolean notEnoughXp = !player.getAbilities().instabuild && player.experienceLevel < cost;
            if (!peaceful && !notEnoughXp) continue;

            session(player, villager).blocked().add(new BlockedOffer(offer, offer.getUses()));
            offer.setToOutOfStock();
            blockedAny = true;
            if (notEnoughXp && !peaceful && (smallestCost == 0 || cost < smallestCost)) {
                smallestCost = cost;
            }
        }
        return blockedAny && smallestCost == 0 ? -1 : smallestCost; // -1 = blocked, but no message (Peaceful)
    }

    /**
     * When the trade screen closes:
     * 1. blocked illegal offers get their real uses back,
     * 2. hidden illegal offers go back to the END of the list (same order),
     * 3. sold out "once per villager" trades disappear.
     */
    @SubscribeEvent
    public static void onCloseTrade(PlayerContainerEvent.Close event) {
        if (!(event.getContainer() instanceof MerchantMenu)) return;
        restore(event.getEntity());
    }

    /** Undoes everything we changed for this player (see Session). */
    private static void restore(Player player) {
        Session session = SESSIONS.remove(player.getUUID());
        if (session == null) return;
        undo(session);
        removeSoldOutOnceTrades(session.villager());
        applyPriceRaises(session.villager());
    }

    /** Safety: the screen never opened (or never closed properly) and nobody is trading with the villager. */
    private static void restoreLeftovers(Villager villager) {
        SESSIONS.values().removeIf(session -> {
            if (session.villager() != villager) return false;
            undo(session);
            return true;
        });
    }

    private static void undo(Session session) {
        for (BlockedOffer entry : session.blocked()) {
            setUses(entry.offer(), entry.realUses());
            KNOWN_USES.put(entry.offer(), entry.realUses()); // not a restock: it was only blocked
        }
        for (MerchantOffer offer : session.hidden()) {
            if (!session.villager().getOffers().contains(offer)) {
                session.villager().getOffers().add(offer);
            }
        }
    }

    // =========================================================================
    // 5. After an illegal trade: XP cost + penalties
    // =========================================================================
    @SubscribeEvent
    public static void onTrade(TradeWithVillagerEvent event) {
        if (!(event.getAbstractVillager() instanceof Villager villager)) return;
        if (!(villager.level() instanceof ServerLevel level)) return;
        if (!IllegalVillagers.isIllegalActive(villager)) return;

        MerchantOffer offer = event.getMerchantOffer();
        if (!IllegalVillagers.isIllegalOffer(offer)) return;

        Player player = event.getEntity();
        IllegalSlot slot = IllegalVillagers.slotOf(villager, offer);

        // Advancement "Psst, Wanna Buy?": your first illegal deal
        if (player instanceof ServerPlayer serverPlayer) {
            ModAdvancements.award(serverPlayer, ModAdvancements.WANNA_BUY);
        }

        // Count the purchase: the price goes up 5% when the screen closes
        int slotIndex = IllegalVillagers.slotIndex(villager, offer);
        IllegalVillagerData data = IllegalVillagers.data(villager);
        if (slotIndex >= 0 && slotIndex < data.slots().size()) {
            villager.setData(ModAttachments.ILLEGAL_VILLAGER, data.withSlot(slotIndex, data.slots().get(slotIndex).bought()));
        }

        // a) Takes XP levels (from the trade's rarity) instead of giving XP
        if (!player.getAbilities().instabuild) {
            player.giveExperienceLevels(-slot.levelCost());
        }

        // b) Bad Omen (a raid if you're in a village): chance and level depend on trust, rarity and village type
        if (level.getRandom().nextFloat() < IllegalVillagers.raidChance(villager, player, slot, level)) {
            int omenLevel = IllegalVillagers.badOmenLevel(villager, player, slot, level);
            player.addEffect(new MobEffectInstance(MobEffects.BAD_OMEN, 120000, omenLevel - 1));
        }

        // c) Witnesses: LEGAL villagers that can see you gossip about you (vanilla gossip: when your
        //    reputation in the village gets too low, the iron golems attack you). Iron golems that see you
        //    attack right away. Other illegal villagers are accomplices: they don't snitch.
        //    If anyone saw it, the illegal villager gets scared and runs away (IllegalChaseEvents)
        List<Entity> witnesses = IllegalChaseEvents.witnessesOf(level, player, villager);
        for (Entity witness : witnesses) {
            if (witness instanceof Villager legal) {
                legal.getGossips().add(player.getUUID(), GossipType.MINOR_NEGATIVE, 25);
            } else if (witness instanceof IronGolem golem) {
                golem.setTarget(player);
            }
        }
        if (!witnesses.isEmpty()) {
            IllegalChaseEvents.scare(villager, level);
        }

        // e) 15%: the illegal villager can't keep its mouth shut (it will spread the gossip itself)
        if (level.getRandom().nextFloat() < IllegalVillagers.LEAK_CHANCE) {
            villager.getGossips().add(player.getUUID(), GossipType.MINOR_NEGATIVE, 25);
        }

        // f) Now you have fewer levels: block the illegal trades you can't pay anymore
        //    (so you can't buy again with 0 levels) and send the updated list to the screen
        if (blockUnaffordable(player, villager) != 0 && player.containerMenu instanceof MerchantMenu menu) {
            MerchantOffers offers = villager.getOffers();
            player.sendMerchantOffers(menu.containerId, offers, villager.getVillagerData().level(),
                    villager.getVillagerXp(), villager.showProgressBar(), villager.canRestock());
        }
    }
}