package com.sam_mc.travelyourearth.villager;

import com.sam_mc.travelyourearth.block.ModBlocks;
import com.sam_mc.travelyourearth.item.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Ruby trades of the WANDERING TRADER (1.2).
 *
 * Half of the wandering traders bring 2 extra ruby trades at the end of their list, picked
 * completely at random from both lists (2 he sells, 2 he buys, or one of each):
 *   - SELLS: rare things "from far away", paid with rubies.
 *   - BUYS: he pays you in rubies. He's the ONLY one in the game who gives you rubies
 *     in a trade (savanna villagers never do: there the ruby is the money).
 * No side effects: no XP cost, no Bad Omen, no witnesses (he isn't from the village).
 */
public final class WanderingSmugglerTrades {

    /** Chance of a wandering trader being a smuggler (bringing ruby trades). */
    public static final float SMUGGLER_CHANCE = 0.5F;

    /** Same XP as vanilla wandering trader trades. */
    private static final int XP = 1;
    /** Price multiplier (wandering traders don't use demand, so it doesn't matter much). */
    private static final float PRICE_MULTIPLIER = 0.05F;

    private WanderingSmugglerTrades() {}

    // =========================================================================
    // 1. THINGS HE SELLS (you pay rubies)
    // =========================================================================
    private static final List<Function<Level, MerchantOffer>> SELLS = List.of(
            level -> sell(rubies(12), new ItemStack(Items.ENCHANTED_GOLDEN_APPLE), 1),
            level -> sell(rubies(7), new ItemStack(Items.END_CRYSTAL), 3),
            level -> sell(rubies(10), new ItemStack(Items.SNIFFER_EGG), 1),
            level -> sell(rubies(5), new ItemStack(Items.OMINOUS_BOTTLE), 2),
            level -> sell(rubies(3), new ItemStack(Items.TRIAL_KEY), 2),
            level -> sell(rubies(8), new ItemStack(Items.OMINOUS_TRIAL_KEY), 1),
            level -> sell(rubies(10), enchantedRubyAxe(level), 1),
            level -> armorTrim(level.getRandom()),
            level -> sell(rubies(2), new ItemStack(Items.GHAST_TEAR), 5),
            level -> sell(rubies(5), new ItemStack(Items.SCULK_SENSOR), 3),
            level -> sell(rubies(7), new ItemStack(Items.SCULK_CATALYST), 1),
            level -> sell(rubies(12), new ItemStack(Items.SCULK_SHRIEKER), 1),
            level -> sell(rubyBlocks(4), new ItemStack(Items.TOTEM_OF_UNDYING), 1),
            level -> potion(level.getRandom()),
            level -> sell(rubies(12), new ItemStack(Items.SHULKER_SHELL), 4),
            level -> sell(rubies(7), new ItemStack(Items.BUDDING_AMETHYST), 1),
            level -> c418Disc(level.getRandom()));

    // =========================================================================
    // 2. THINGS HE BUYS (he pays you rubies: the only one in the game!)
    // =========================================================================
    private static final List<Function<Level, MerchantOffer>> BUYS = List.of(
            level -> buy(Items.DIAMOND, 2, 1, 3),
            level -> buy(Items.CREEPER_HEAD, 1, 12, 1),
            level -> buy(Items.GOLDEN_APPLE, 1, 3, 2),
            level -> buy(Items.GOLDEN_CARROT, 1, 1, 3),
            level -> buy(Items.NETHER_WART, 16, 2, 2),
            level -> buy(Items.MILK_BUCKET, 1, 1, 1),
            level -> buy(Items.FERMENTED_SPIDER_EYE, 1, 1, 5));

    /** How many ruby trades a smuggler brings. */
    public static final int RUBY_TRADES = 2;

    /**
     * The ruby trades of a smuggler: completely random from BOTH lists together, never the same
     * one twice. It can be 2 he sells, 2 he buys, or one of each.
     */
    public static List<MerchantOffer> pick(Level level) {
        RandomSource random = level.getRandom();
        List<Function<Level, MerchantOffer>> all = new ArrayList<>(SELLS);
        all.addAll(BUYS);
        List<MerchantOffer> picked = new ArrayList<>();
        for (int i = 0; i < RUBY_TRADES && !all.isEmpty(); i++) {
            picked.add(all.remove(random.nextInt(all.size())).apply(level));
        }
        return picked;
    }

    // =========================================================================
    // Special trades
    // =========================================================================

    /** Armor trims (all but Silence): 10 rubies the easy ones, 18 medium, 22 hard. Only once. */
    private static MerchantOffer armorTrim(RandomSource random) {
        record Trim(Item template, int price) {}
        List<Trim> trims = List.of(
                // Easy to find (outposts, deserts, beaches, jungles, trail ruins, normal trial chambers)
                new Trim(Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE, 10),
                new Trim(Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE, 10),
                new Trim(Items.COAST_ARMOR_TRIM_SMITHING_TEMPLATE, 10),
                new Trim(Items.WILD_ARMOR_TRIM_SMITHING_TEMPLATE, 10),
                new Trim(Items.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE, 10),
                new Trim(Items.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE, 10),
                new Trim(Items.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE, 10),
                new Trim(Items.HOST_ARMOR_TRIM_SMITHING_TEMPLATE, 10),
                // Medium (ancient cities, bastions, fortresses, ocean monuments, ominous vaults)
                new Trim(Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE, 18),
                new Trim(Items.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE, 18),
                new Trim(Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE, 18),
                new Trim(Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE, 18),
                new Trim(Items.FLOW_ARMOR_TRIM_SMITHING_TEMPLATE, 18),
                new Trim(Items.BOLT_ARMOR_TRIM_SMITHING_TEMPLATE, 18),
                // Hard (strongholds, woodland mansions, end cities)
                new Trim(Items.EYE_ARMOR_TRIM_SMITHING_TEMPLATE, 22),
                new Trim(Items.VEX_ARMOR_TRIM_SMITHING_TEMPLATE, 22),
                new Trim(Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE, 22));
        Trim pick = trims.get(random.nextInt(trims.size()));
        return sell(rubies(pick.price()), new ItemStack(pick.template()), 1);
    }

    /** Fire Resistance, Swiftness, Regeneration and Night Vision: price by its value, level and duration. */
    private static MerchantOffer potion(RandomSource random) {
        record PotionPrice(Holder<Potion> potion, int price) {}
        List<PotionPrice> potions = List.of(
                new PotionPrice(Potions.FIRE_RESISTANCE, 3), new PotionPrice(Potions.LONG_FIRE_RESISTANCE, 4),
                new PotionPrice(Potions.SWIFTNESS, 2), new PotionPrice(Potions.LONG_SWIFTNESS, 3),
                new PotionPrice(Potions.STRONG_SWIFTNESS, 4),
                new PotionPrice(Potions.REGENERATION, 4), new PotionPrice(Potions.LONG_REGENERATION, 5),
                new PotionPrice(Potions.STRONG_REGENERATION, 6),
                new PotionPrice(Potions.NIGHT_VISION, 2), new PotionPrice(Potions.LONG_NIGHT_VISION, 3));
        PotionPrice pick = potions.get(random.nextInt(potions.size()));
        return sell(rubies(pick.price()), PotionContents.createItemStack(Items.POTION, pick.potion()), 2);
    }

    /** All of C418's music discs: 8 rubies. */
    private static MerchantOffer c418Disc(RandomSource random) {
        List<Item> discs = List.of(
                Items.MUSIC_DISC_13, Items.MUSIC_DISC_CAT, Items.MUSIC_DISC_BLOCKS, Items.MUSIC_DISC_CHIRP,
                Items.MUSIC_DISC_FAR, Items.MUSIC_DISC_MALL, Items.MUSIC_DISC_MELLOHI, Items.MUSIC_DISC_STAL,
                Items.MUSIC_DISC_STRAD, Items.MUSIC_DISC_WARD, Items.MUSIC_DISC_11, Items.MUSIC_DISC_WAIT);
        return sell(rubies(8), new ItemStack(discs.get(random.nextInt(discs.size()))), 1);
    }

    /** Ruby axe enchanted like a good villager trade (level 15-30, same pool as vanilla trades). */
    private static ItemStack enchantedRubyAxe(Level level) {
        RegistryAccess access = level.registryAccess();
        Optional<HolderSet.Named<Enchantment>> pool =
                access.lookupOrThrow(Registries.ENCHANTMENT).get(EnchantmentTags.ON_TRADED_EQUIPMENT);
        int enchantingLevel = 15 + level.getRandom().nextInt(16);
        return EnchantmentHelper.enchantItem(level.getRandom(), new ItemStack(ModItems.RUBY_AXE.get()),
                enchantingLevel, access, pool);
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private static MerchantOffer sell(ItemCost price, ItemStack result, int maxUses) {
        return new MerchantOffer(price, Optional.empty(), result, maxUses, XP, PRICE_MULTIPLIER);
    }

    private static MerchantOffer buy(ItemLike item, int count, int rubies, int maxUses) {
        return new MerchantOffer(new ItemCost(item, count), Optional.empty(),
                new ItemStack(ModItems.RUBY.get(), rubies), maxUses, XP, PRICE_MULTIPLIER);
    }

    private static ItemCost rubies(int count) {
        return new ItemCost(ModItems.RUBY.get(), count);
    }

    private static ItemCost rubyBlocks(int count) {
        return new ItemCost(ModBlocks.RUBY_BLOCK.get(), count);
    }
}