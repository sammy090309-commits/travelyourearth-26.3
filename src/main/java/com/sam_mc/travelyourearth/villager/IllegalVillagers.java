package com.sam_mc.travelyourearth.villager;

import com.sam_mc.travelyourearth.attachment.IllegalSlot;
import com.sam_mc.travelyourearth.attachment.IllegalVillagerData;
import com.sam_mc.travelyourearth.attachment.ModAttachments;
import com.sam_mc.travelyourearth.block.ModBlocks;
import com.sam_mc.travelyourearth.config.IllegalVillagerConfig;
import com.sam_mc.travelyourearth.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.StructureTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Helper methods for the illegal trader system (1.2). */
public final class IllegalVillagers {

    /**
     * Illegal chance (min - max) of each village type. Each village gets its own number inside
     * its range (always the same for the same village and seed), so some villages are more
     * "corrupt" than others. Jungle and swamp (player villages there) use the plains range.
     */
    private record ChanceRange(float min, float max) {}

    private static final ChanceRange PLAINS_CHANCE = new ChanceRange(0.072F, 0.175F);
    private static final Map<String, ChanceRange> CHANCE_BY_TYPE = Map.of(
            "plains", PLAINS_CHANCE,
            // Savanna: the ruby is LEGAL there (its second currency), so there are no illegal villagers
            "savanna", new ChanceRange(0.0F, 0.0F),
            "taiga", new ChanceRange(0.093F, 0.182F),
            "snowy", new ChanceRange(0.053F, 0.102F),
            "snow", new ChanceRange(0.053F, 0.102F),   // villager type name of the snowy villages
            "desert", new ChanceRange(0.112F, 0.172F));

    /** Villagers outside a generated village use "zones" of 10x10 chunks as their village. */
    private static final int ZONE_SIZE_CHUNKS = 10;

    /**
     * Trust = vanilla reputation (gossip) of the player with the villager.
     * Every 5 points of reputation = 1 trust level, from 0 to 4.
     * Vanilla: each trade gives +2 reputation (up to 25), curing it from a zombie gives a lot more,
     * hitting it takes it away.
     */
    public static final int REPUTATION_PER_TRUST_LEVEL = 5;
    public static final int MAX_TRUST_LEVEL = 4;

    /** Trust needed to see its illegal trades during the day (reputation 15, about 8 trades). */
    public static final int TRUST_TO_REVEAL = 3;

    /**
     * Raid chance on each illegal trade:
     * (10% - 2% per trust level, min 2%, + 0/2/4% by rarity) x village factor (0.5 - 1.5).
     */
    private static final float BASE_RAID_CHANCE = 0.10F;
    private static final float RAID_CHANCE_PER_TRUST = 0.02F;
    private static final float MIN_RAID_CHANCE = 0.02F;
    private static final float RAID_CHANCE_PER_RARITY = 0.02F;

    /** Every purchase raises the price 5%. Prices never go down. */
    public static final float PRICE_RAISE_PER_TRADE = 1.05F;
    public static final int MAX_RUBY_PRICE = 64;
    public static final int MAX_RUBY_BLOCK_PRICE = 16;

    /** Chance that the illegal villager "can't keep its mouth shut" and spreads the gossip itself. */
    public static final float LEAK_CHANCE = 0.15F;

    /** How far (in blocks) villagers and golems can see an illegal trade. */
    public static final double WITNESS_RANGE = 16.0D;

    /** Master level (vanilla max). Reaching it unlocks the secret phase. */
    public static final int MASTER_LEVEL = 5;

    /** How many illegal trades each illegal villager offers. */
    public static final int ILLEGAL_TRADES_PER_VILLAGER = 2;

    private IllegalVillagers() {}

    /** True if the villager is a nitwit (nitwits can never be illegal). */
    public static boolean isNitwit(Villager villager) {
        return villager.getVillagerData().profession().is(VillagerProfession.NITWIT);
    }

    public static IllegalVillagerData data(Villager villager) {
        return villager.getData(ModAttachments.ILLEGAL_VILLAGER);
    }

    /** Raw data of the villager (true even in Peaceful). */
    public static boolean isIllegal(Villager villager) {
        return data(villager).illegal();
    }

    /**
     * True if the illegal system should work for this villager right now:
     * it's illegal, it's not a nitwit and the world is NOT in Peaceful.
     */
    public static boolean isIllegalActive(Villager villager) {
        return isIllegal(villager)
                && !isNitwit(villager)
                && villager.level().getDifficulty() != Difficulty.PEACEFUL;
    }

    /** Rolls the chance of its village and returns the data for a new (adult) villager. */
    public static IllegalVillagerData roll(Villager villager, ServerLevel level) {
        boolean illegal = !isNitwit(villager) && villager.getRandom().nextFloat() < villageChance(villager, level);
        return illegal ? IllegalVillagerData.NEW_ILLEGAL : IllegalVillagerData.LEGAL;
    }

    /**
     * The illegal chance of the villager's village.
     * - Type: the vanilla village structure it's in (village_plains, village_desert...).
     *   Outside a generated village: its own villager type (the biome it comes from).
     * - Exact number inside the type's range: decided by the village "id" (corner of the village
     *   structure, or a 10x10 chunk zone outside villages) + the world seed.
     */
    public static float villageChance(Villager villager, ServerLevel level) {
        VillageInfo village = villageInfo(villager, level);
        ChanceRange range = CHANCE_BY_TYPE.getOrDefault(village.type(), PLAINS_CHANCE);

        // Same seed + same village = same number, every time
        long seed = level.getSeed() ^ (village.idX() * 341873128712L) ^ (village.idZ() * 132897987541L);
        float roll = RandomSource.create(seed).nextFloat();
        return range.min() + roll * (range.max() - range.min());
    }

    /** Type of village ("plains", "desert"...) and its "id" (corner of the structure, or a 10x10 chunk zone). */
    private record VillageInfo(String type, long idX, long idZ) {}

    private static VillageInfo villageInfo(Villager villager, ServerLevel level) {
        BlockPos pos = villager.blockPosition();
        StructureStart village = level.structureManager().getStructureWithPieceAt(pos, StructureTags.VILLAGE);
        if (village.isValid()) {
            // "minecraft:village_desert" -> "desert"
            String type = level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getKey(village.getStructure()) instanceof Identifier id
                    ? id.getPath().replace("village_", "") : "plains";
            // Corner of the whole village: the same for all its villagers
            return new VillageInfo(type, village.getBoundingBox().minX(), village.getBoundingBox().minZ());
        }
        int zoneBlocks = ZONE_SIZE_CHUNKS * 16;
        String type = villager.getVillagerData().type().unwrapKey().map(key -> key.identifier().getPath()).orElse("plains");
        // +1M: never the same as a real village corner
        return new VillageInfo(type, Math.floorDiv(pos.getX(), zoneBlocks) + 1_000_000L, Math.floorDiv(pos.getZ(), zoneBlocks) + 1_000_000L);
    }

    /**
     * How "corrupt" this type of village is compared to plains (average chance of its range / plains average),
     * between 0.5 and 1.5. Desert and taiga villages (more traffickers) attract more raids; snowy and savanna fewer.
     */
    public static float villageRaidFactor(Villager villager, ServerLevel level) {
        ChanceRange range = CHANCE_BY_TYPE.getOrDefault(villageInfo(villager, level).type(), PLAINS_CHANCE);
        float average = (range.min() + range.max()) / 2.0F;
        float plainsAverage = (PLAINS_CHANCE.min() + PLAINS_CHANCE.max()) / 2.0F;
        return Math.max(0.5F, Math.min(1.5F, average / plainsAverage));
    }

    // =========================================================================
    // Trust (vanilla reputation)
    // =========================================================================

    /** Trust level of the player with this villager: 0 - 4 (see REPUTATION_PER_TRUST_LEVEL). */
    public static int trustLevel(Villager villager, Player player) {
        int reputation = villager.getPlayerReputation(player);
        return Math.max(0, Math.min(MAX_TRUST_LEVEL, reputation / REPUTATION_PER_TRUST_LEVEL));
    }

    /** True if the villager trusts the player enough to show its illegal trades during the day. */
    public static boolean trusts(Villager villager, Player player) {
        return trustLevel(villager, player) >= TRUST_TO_REVEAL;
    }

    /**
     * Raid (Bad Omen) chance of an illegal trade:
     * (10% - 2% per trust level, min 2%) + rarity bonus (Common +0%, Rare +2%, Strange +4%),
     * all multiplied by the village factor (more traffickers in that type of village = more raids).
     */
    public static float raidChance(Villager villager, Player player, IllegalSlot slot, ServerLevel level) {
        float chance = Math.max(MIN_RAID_CHANCE, BASE_RAID_CHANCE - RAID_CHANCE_PER_TRUST * trustLevel(villager, player));
        chance += rarityStep(slot) * RAID_CHANCE_PER_RARITY;
        return chance * villageRaidFactor(villager, level);
    }

    /**
     * Bad Omen LEVEL (1-5) = how big the raid is:
     * Common 1, Rare 2, Strange 3; +1 if it barely trusts you (trust 0-1), -1 if it fully trusts you (trust 4);
     * +1 in very corrupt villages (factor 1.1 or more).
     */
    public static int badOmenLevel(Villager villager, Player player, IllegalSlot slot, ServerLevel level) {
        int omen = 1 + rarityStep(slot);
        int trust = trustLevel(villager, player);
        if (trust <= 1) omen++;
        if (trust >= MAX_TRUST_LEVEL) omen--;
        if (villageRaidFactor(villager, level) >= 1.1F) omen++;
        return Math.max(1, Math.min(5, omen));
    }

    /** Common 0, Rare 1, Strange 2 (from the XP cost: 2 / 4 / 6 levels). */
    private static int rarityStep(IllegalSlot slot) {
        return Math.max(0, (slot.levelCost() - IllegalRarity.COMMON.levelCost()) / 2);
    }

    // =========================================================================
    // Prices
    // =========================================================================

    /**
     * Price the offer should have now: base price +5% per purchase (rounded), never lower than the base,
     * and at most 64 rubies / 16 ruby blocks. -1 if the slot doesn't know its base price (old villager).
     */
    public static int currentPrice(MerchantOffer offer, IllegalSlot slot) {
        if (slot.basePrice() <= 0) return -1;
        int max = offer.getBaseCostA().is(ModBlocks.RUBY_BLOCK.get().asItem()) ? MAX_RUBY_BLOCK_PRICE : MAX_RUBY_PRICE;
        int price = Math.round(slot.basePrice() * (float) Math.pow(PRICE_RAISE_PER_TRADE, slot.timesBought()));
        return Math.min(max, Math.max(slot.basePrice(), price));
    }

    // =========================================================================
    // Illegal offers. GOLDEN RULE: an offer is illegal if it's paid with rubies
    // or ruby blocks (no legal trade uses them).
    // =========================================================================

    /**
     * True if the offer is an illegal one: paid with rubies or ruby blocks AND with price multiplier 0
     * (every illegal offer is created like that; vanilla offers never use 0). This way the LEGAL ruby
     * trades of savanna villagers (they keep vanilla's multiplier) are not taken as illegal.
     */
    public static boolean isIllegalOffer(MerchantOffer offer) {
        ItemStack costA = offer.getBaseCostA();
        boolean paidWithRubies = costA.is(ModItems.RUBY.get()) || costA.is(ModBlocks.RUBY_BLOCK.get().asItem());
        return paidWithRubies && offer.getPriceMultiplier() == 0.0F;
    }

    /** True if the villager is from the savanna (its villager type), where the ruby is a legal currency. */
    public static boolean isSavannaVillager(Villager villager) {
        return villager.getVillagerData().type().unwrapKey()
                .map(key -> key.identifier().getPath().equals("savanna"))
                .orElse(false);
    }

    /** The illegal offers of the villager, in the same order as in its trade list (= order of the slots). */
    public static List<MerchantOffer> illegalOffers(Villager villager) {
        List<MerchantOffer> result = new ArrayList<>();
        if (!data(villager).secretUnlocked()) return result;
        for (MerchantOffer offer : villager.getOffers()) {
            if (isIllegalOffer(offer)) result.add(offer);
        }
        return result;
    }

    /** Position of the offer among the illegal offers (0, 1...), or -1 if it isn't one. */
    public static int slotIndex(Villager villager, MerchantOffer offer) {
        List<MerchantOffer> illegal = illegalOffers(villager);
        for (int i = 0; i < illegal.size(); i++) {
            if (illegal.get(i) == offer) return i;
        }
        return -1;
    }

    /**
     * Info (XP cost, once per villager) of an illegal offer.
     * If it's missing (old villager from an older test version), it's treated as a Rare trade.
     */
    public static IllegalSlot slotOf(Villager villager, MerchantOffer offer) {
        int index = slotIndex(villager, offer);
        List<IllegalSlot> slots = data(villager).slots();
        if (index >= 0 && index < slots.size()) return slots.get(index);
        return new IllegalSlot(IllegalRarity.RARE.levelCost(), false, 0, 0);
    }

    // =========================================================================
    // The secret is shown (ruby badge + illegal trades) only with the SAME rules
    // =========================================================================

    /** True if the stack is a ruby or a ruby block (both work as "money" for illegal villagers). */
    public static boolean isRubyMoney(ItemStack stack) {
        return stack.is(ModItems.RUBY.get()) || stack.is(ModBlocks.RUBY_BLOCK.get().asItem());
    }

    /** True if the player holds a ruby OR a ruby block in the main hand or the off hand. */
    public static boolean isHoldingRuby(Player player) {
        return isRubyMoney(player.getMainHandItem()) || isRubyMoney(player.getOffhandItem());
    }

    /**
     * True if there are no possible witnesses within WITNESS_RANGE (16 blocks): no iron golems
     * and no LEGAL villagers. Other illegal villagers don't count: they're accomplices, not witnesses.
     */
    public static boolean isAlone(LivingEntity villager) {
        return villager.level().getEntities(villager,
                villager.getBoundingBox().inflate(WITNESS_RANGE),
                IllegalVillagers::isWitness
        ).isEmpty();
    }

    /** At night the trafficker is more careful, but only looks close by. */
    public static final double NIGHT_WITNESS_RANGE = 8.0D;

    /**
     * Night: no legal villager (awake OR asleep) and no iron golem closer than 8 blocks.
     * Other illegal villagers don't count: they're traffickers too and keep the secret.
     */
    public static boolean isAloneAtNight(Villager villager) {
        return villager.level().getEntities(villager,
                villager.getBoundingBox().inflate(NIGHT_WITNESS_RANGE),
                entity -> entity instanceof IronGolem || (entity instanceof Villager other && !isIllegal(other))
        ).isEmpty();
    }

    /**
     * Iron golems and AWAKE legal villagers are witnesses.
     * Illegal villagers keep the secret, and sleeping villagers can't see anything.
     */
    public static boolean isWitness(Entity entity) {
        if (entity instanceof IronGolem) return true;
        return entity instanceof Villager other && !isIllegal(other) && !other.isSleeping();
    }

    // =========================================================================
    // Night (part 3)
    // =========================================================================

    /** At night the trafficker shows its trades to players this close, without a ruby. */
    public static final double NIGHT_REVEAL_RANGE = 8.0D;

    /**
     * True when the villager is in its "sleep time" (vanilla schedule: REST, from sunset at 12000
     * until the morning). We ask the villager's own brain instead of reading the clock:
     * in 26.3 the day time moved to the new world clocks, and this way it always matches vanilla.
     */
    public static boolean isNight(Villager villager) {
        return villager.getBrain().isActive(Activity.REST);
    }

    /**
     * True if this illegal villager goes out tonight: illegal + active + night + one of the
     * trafficking nights (1 of every 3). ANY level: apprentices go out too (invisible, to the bell,
     * chatting with the others), but they can't trade yet (see isNightTrafficker).
     */
    public static boolean isOutAtNight(Villager villager) {
        return isIllegalActive(villager) && isNight(villager) && isTraffickingNight(villager.level());
    }

    /**
     * True if this illegal villager can TRADE at night right now: it's out (isOutAtNight)
     * AND it's a master (secret phase unlocked). Only these drink the milk and show their trades.
     */
    public static boolean isNightTrafficker(Villager villager) {
        return isOutAtNight(villager) && data(villager).secretUnlocked();
    }


    /**
     * True on the trafficking nights: 1 of every N (config illegalNightEvery, default 3), the same for all. The day number comes from the game time (24000 ticks per day,
     * the day starts in the morning, so the whole night counts as the same day).
     */
    public static boolean isTraffickingNight(net.minecraft.world.level.Level level) {
        return (level.getGameTime() / 24000L) % IllegalVillagerConfig.NIGHT_EVERY.get() == 0;
    }

    /**
     * Day rules to show the secret: the villager trusts the player, the player holds a ruby
     * (or a ruby block) AND the villager is alone.
     * The server checks them (with a 1-2 s delay) and saves the result in SECRET_REVEALED_TO.
     */
    public static boolean showsSecretTo(Villager villager, Player player) {
        return trusts(villager, player) && isHoldingRuby(player) && isAlone(villager);
    }

    /**
     * True if the villager (or zombie villager) is showing its secret to this player RIGHT NOW.
     * The ruby badge, the illegal trades and the illegal item in its hand all use this,
     * so they always change together.
     */
    public static boolean isRevealedTo(LivingEntity villager, Player player) {
        Optional<UUID> revealedTo = villager.getData(ModAttachments.SECRET_REVEALED_TO);
        return revealedTo.isPresent() && revealedTo.get().equals(player.getUUID());
    }
}