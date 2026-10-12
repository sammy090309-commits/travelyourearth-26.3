package com.sam_mc.travelyourearth.event;

import com.sam_mc.travelyourearth.TravelYourEarth;
import com.sam_mc.travelyourearth.attachment.ModAttachments;
import com.sam_mc.travelyourearth.block.ModBlocks;
import com.sam_mc.travelyourearth.item.ModItems;
import com.sam_mc.travelyourearth.villager.IllegalVillagers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Savanna villagers: "the country of the ruby". There the ruby is a LEGAL second currency.
 *
 * Every time a savanna villager gets new trades (first right-click, level up), each new trade is
 * checked once, BEFORE you see it (no emerald-then-ruby flicker):
 * - Everything with DIAMOND becomes RUBY, same price: diamond gear -> ruby gear (keeps its
 *   enchantments), diamonds -> rubies, diamond block -> ruby block.
 * - 70% of the trades that COST emeralds are paid with rubies instead: 1 ruby = 5 emeralds
 *   (rounded up). Trades where the villager PAYS you emeralds stay the same (no ruby farms).
 * - The ruby is the MONEY: villagers never sell rubies (or ruby blocks).
 * - Trades paid with rubies have 25% more uses before running out than the emerald ones.
 *
 * These ruby trades are LEGAL: they keep vanilla's price multiplier, so they're never taken
 * as illegal ones (illegal = rubies + multiplier 0): no XP cost, no raid, no witnesses.
 */
@EventBusSubscriber(modid = TravelYourEarth.MODID)
public class SavannaVillagerEvents {

    /** Chance of a trade that costs emeralds being paid with rubies. */
    private static final float RUBY_PRICE_CHANCE = 0.70F;

    /** 1 ruby = 5 emeralds. */
    private static final int EMERALDS_PER_RUBY = 5;

    /** Trades paid with rubies can be used 25% more times before running out (than with emeralds). */
    private static final float RUBY_RESTOCK_BONUS = 1.25F;

    /** Smallest vanilla price multiplier. Savanna ruby trades never go below it, so they're never illegal. */
    private static final float LEGAL_MIN_MULTIPLIER = 0.05F;

    /** Diamond item -> its ruby version. Built on demand (the mod's items must be registered).
     *  Also used by SavannaVillageChestModifier (the chests of savanna villages). */
    public static Map<Item, Supplier<? extends ItemLike>> diamondToRuby() {
        return Map.ofEntries(
                Map.entry(Items.DIAMOND, ModItems.RUBY),
                Map.entry(Items.DIAMOND_BLOCK, ModBlocks.RUBY_BLOCK),
                Map.entry(Items.DIAMOND_SWORD, ModItems.RUBY_SWORD),
                Map.entry(Items.DIAMOND_SPEAR, ModItems.RUBY_SPEAR),
                Map.entry(Items.DIAMOND_AXE, ModItems.RUBY_AXE),
                Map.entry(Items.DIAMOND_PICKAXE, ModItems.RUBY_PICKAXE),
                Map.entry(Items.DIAMOND_SHOVEL, ModItems.RUBY_SHOVEL),
                Map.entry(Items.DIAMOND_HOE, ModItems.RUBY_HOE),
                Map.entry(Items.DIAMOND_HELMET, ModItems.RUBY_HELMET),
                Map.entry(Items.DIAMOND_CHESTPLATE, ModItems.RUBY_CHESTPLATE),
                Map.entry(Items.DIAMOND_LEGGINGS, ModItems.RUBY_LEGGINGS),
                Map.entry(Items.DIAMOND_BOOTS, ModItems.RUBY_BOOTS),
                Map.entry(Items.DIAMOND_HORSE_ARMOR, ModItems.RUBY_HORSE_ARMOR),
                Map.entry(Items.DIAMOND_NAUTILUS_ARMOR, ModItems.RUBY_NAUTILUS_ARMOR));
    }

    private SavannaVillagerEvents() {}

    /**
     * BEFORE the trade screen opens (the right-click): the trades are already in rubies the very
     * first time you see them. (HIGHEST so it runs before anything else looks at the trades.)
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Villager villager)) return;
        if (!(villager.level() instanceof ServerLevel)) return;
        if (!IllegalVillagers.isSavannaVillager(villager)) return;
        process(villager);
    }

    /**
     * Every tick: new trades (level up) are converted at once. In 26.3 the trade screen updates
     * live, so if someone is trading we send them the new list right away (no need to close it).
     */
    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Villager villager)) return;
        if (!(villager.level() instanceof ServerLevel)) return;
        if (!IllegalVillagers.isSavannaVillager(villager)) return;

        if (process(villager)
                && villager.getTradingPlayer() instanceof ServerPlayer player
                && player.containerMenu instanceof MerchantMenu menu) {
            player.sendMerchantOffers(menu.containerId, villager.getOffers(), villager.getVillagerData().level(),
                    villager.getVillagerXp(), villager.showProgressBar(), villager.canRestock());
        }
    }

    /** Converts the trades that weren't checked yet and legalizes the ruby ones. True if something changed. */
    private static boolean process(Villager villager) {
        MerchantOffers offers = villager.getOffers();
        boolean changed = false;

        // 0. In the savanna the ruby is LEGAL: no trade can ever look illegal (dark button).
        //    Fixes villagers converted before (some vanilla trades come with price multiplier 0).
        for (int i = 0; i < offers.size(); i++) {
            MerchantOffer offer = offers.get(i);
            if (IllegalVillagers.isIllegalOffer(offer)) {
                offers.set(i, legalize(offer));
                changed = true;
            }
        }

        int checked = villager.getData(ModAttachments.SAVANNA_TRADES_CHECKED);
        // The list got SHORTER = the villager lost its job and got new trades -> check everything again
        if (checked > offers.size()) checked = 0;
        if (checked >= offers.size()) {
            if (checked != villager.getData(ModAttachments.SAVANNA_TRADES_CHECKED)) {
                villager.setData(ModAttachments.SAVANNA_TRADES_CHECKED, checked);
            }
            return changed;
        }

        Map<Item, Supplier<? extends ItemLike>> rubyVersions = diamondToRuby();
        for (int i = checked; i < offers.size(); i++) {
            MerchantOffer offer = offers.get(i);
            MerchantOffer savanna = rubyfy(offer, villager, rubyVersions);
            if (savanna != offer) {
                offers.set(i, savanna);
                changed = true;
            }
        }
        villager.setData(ModAttachments.SAVANNA_TRADES_CHECKED, offers.size());
        return changed;
    }

    /** The savanna version of a trade (or the same trade if nothing changes). */
    private static MerchantOffer rubyfy(MerchantOffer offer, Villager villager,
                                        Map<Item, Supplier<? extends ItemLike>> rubyVersions) {
        ItemCost costA = offer.getItemCostA();
        Optional<ItemCost> costB = offer.getItemCostB();
        ItemStack result = offer.getResult().copy();
        boolean changed = false;

        // 1. Diamond -> ruby (same amount, same price)
        //    The ruby is the MONEY: villagers never SELL rubies (or ruby blocks), so a trade that
        //    would give you rubies keeps its vanilla result. Ruby gear is fine (it's not money).
        Item resultRuby = rubyItem(result.getItem(), rubyVersions);
        if (resultRuby != null && !isRubyMoney(resultRuby)) {
            result = result.transmuteCopy(resultRuby, result.getCount()); // keeps enchantments, damage...
            changed = true;
        }
        Item costARuby = rubyItem(costA.item().value(), rubyVersions);
        if (costARuby != null) {
            costA = new ItemCost(costARuby, costA.count());
            changed = true;
        }
        if (costB.isPresent()) {
            Item costBRuby = rubyItem(costB.get().item().value(), rubyVersions);
            if (costBRuby != null) {
                costB = Optional.of(new ItemCost(costBRuby, costB.get().count()));
                changed = true;
            }
        }

        // 2. 70%: paid with rubies instead of emeralds (1 ruby = 5 emeralds, rounded up)
        if (costA.item().value() == Items.EMERALD && villager.getRandom().nextFloat() < RUBY_PRICE_CHANCE) {
            costA = new ItemCost(ModItems.RUBY.get(), rubiesFor(costA.count()));
            changed = true;
        } else if (costB.isPresent() && costB.get().item().value() == Items.EMERALD
                && villager.getRandom().nextFloat() < RUBY_PRICE_CHANCE) {
            costB = Optional.of(new ItemCost(ModItems.RUBY.get(), rubiesFor(costB.get().count())));
            changed = true;
        }

        if (!changed) return offer;
        // Never multiplier 0: rubies + multiplier 0 = ILLEGAL (dark button, XP cost, raid...).
        // If a vanilla trade had 0, it gets the smallest vanilla one (0.05) so it stays LEGAL.
        float multiplier = Math.max(offer.getPriceMultiplier(), LEGAL_MIN_MULTIPLIER);

        // 3. Paid with rubies -> 25% more uses before running out (they "restock more")
        boolean paidWithRubies = isRubyMoney(costA.item().value())
                || costB.map(cost -> isRubyMoney(cost.item().value())).orElse(false);
        int maxUses = paidWithRubies
                ? (int) Math.ceil(offer.getMaxUses() * RUBY_RESTOCK_BONUS)
                : offer.getMaxUses();

        return new MerchantOffer(costA, costB, result, offer.getUses(), maxUses,
                offer.getXp(), multiplier, offer.getDemand());
    }

    /** The same trade, but with the smallest vanilla price multiplier so it's never taken as illegal. */
    private static MerchantOffer legalize(MerchantOffer offer) {
        return new MerchantOffer(offer.getItemCostA(), offer.getItemCostB(), offer.getResult().copy(),
                offer.getUses(), offer.getMaxUses(), offer.getXp(), LEGAL_MIN_MULTIPLIER, offer.getDemand());
    }

    /** Rubies and ruby blocks: the savanna's MONEY (paid, never sold). */
    private static boolean isRubyMoney(Item item) {
        return item == ModItems.RUBY.get() || item == ModBlocks.RUBY_BLOCK.get().asItem();
    }

    private static int rubiesFor(int emeralds) {
        return Math.max(1, (emeralds + EMERALDS_PER_RUBY - 1) / EMERALDS_PER_RUBY);
    }

    /** The ruby version of a diamond item, or null. Also used by the savanna village chests. */
    public static Item rubyItem(Item item, Map<Item, Supplier<? extends ItemLike>> rubyVersions) {
        Supplier<? extends ItemLike> ruby = rubyVersions.get(item);
        return ruby == null ? null : ruby.get().asItem();
    }
}