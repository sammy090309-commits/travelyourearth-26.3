package com.sam_mc.travelyourearth.villager;

import com.sam_mc.travelyourearth.block.ModBlocks;
import com.sam_mc.travelyourearth.enchantment.ModEnchantments;
import com.sam_mc.travelyourearth.item.ModItems;
import com.sam_mc.travelyourearth.map.ModMapDecorations;
import com.sam_mc.travelyourearth.potion.ModPotions;
import com.sam_mc.travelyourearth.tags.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.InstrumentTags;
import net.minecraft.tags.StructureTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.InstrumentComponent;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Illegal trades of every profession (Beta 1.2 "Mercado Negro", final version).
 * Each illegal villager gets 2 DIFFERENT trades at random from its profession's list
 * when it reaches master level.
 *
 * Prices: 1 ruby = 20 emeralds. Rarity = XP levels: Common 2, Rare 4, Strange 6.
 *
 * PROTOTYPE: written in Java to test the whole system first.
 * TODO: move them to datagen (own data registry) once the system works.
 */
public final class IllegalTrades {

    /**
     * Price multiplier = 0: vanilla demand and reputation discounts don't touch illegal prices.
     * The price only goes up 5% per purchase (IllegalVillagers.currentPrice), and never down.
     */
    private static final float PRICE_MULTIPLIER = 0.0F;

    private static final IllegalRarity COMMON = IllegalRarity.COMMON;
    private static final IllegalRarity RARE = IllegalRarity.RARE;
    private static final IllegalRarity STRANGE = IllegalRarity.STRANGE;

    // =========================================================================
    // Random tables
    // =========================================================================

    private record TreasureBook(ResourceKey<Enchantment> enchantment, int level, int price) {}

    /** Treasure books (+25% more, rounded up): Mending 15, Swift Sneak III 18, Wind Burst III 15, Soul Speed III 13, Frost Walker II 10. */
    private static final List<TreasureBook> TREASURE_BOOKS = List.of(
            new TreasureBook(Enchantments.MENDING, 1, 15),
            new TreasureBook(Enchantments.SWIFT_SNEAK, 3, 18),
            new TreasureBook(Enchantments.WIND_BURST, 3, 15),
            new TreasureBook(Enchantments.SOUL_SPEED, 3, 13),
            new TreasureBook(Enchantments.FROST_WALKER, 2, 10));

    private record TrimPrice(Item template, int price, IllegalRarity rarity) {}

    /** Every armor trim except Silence, with its price (+25% more, rounded up) and rarity. */
    private static final List<TrimPrice> TRIMS = List.of(
            new TrimPrice(Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE, 7, COMMON),
            new TrimPrice(Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE, 7, COMMON),
            new TrimPrice(Items.COAST_ARMOR_TRIM_SMITHING_TEMPLATE, 7, COMMON),
            new TrimPrice(Items.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE, 8, COMMON),
            new TrimPrice(Items.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE, 8, COMMON),
            new TrimPrice(Items.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE, 8, COMMON),
            new TrimPrice(Items.HOST_ARMOR_TRIM_SMITHING_TEMPLATE, 8, COMMON),
            new TrimPrice(Items.WILD_ARMOR_TRIM_SMITHING_TEMPLATE, 9, COMMON),
            new TrimPrice(Items.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE, 13, RARE),
            new TrimPrice(Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE, 13, RARE),
            new TrimPrice(Items.EYE_ARMOR_TRIM_SMITHING_TEMPLATE, 14, RARE),
            new TrimPrice(Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE, 14, RARE),
            new TrimPrice(Items.VEX_ARMOR_TRIM_SMITHING_TEMPLATE, 14, RARE),
            new TrimPrice(Items.FLOW_ARMOR_TRIM_SMITHING_TEMPLATE, 14, RARE),
            new TrimPrice(Items.BOLT_ARMOR_TRIM_SMITHING_TEMPLATE, 14, RARE),
            new TrimPrice(Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE, 18, STRANGE),
            new TrimPrice(Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE, 20, STRANGE));

    // =========================================================================
    // Trade lists of every profession
    // =========================================================================

    private static final Map<ResourceKey<VillagerProfession>, List<IllegalTrade>> TRADES = Map.ofEntries(

            // =================================================================
            // Mason (Albañil)
            // =================================================================
            Map.entry(VillagerProfession.MASON, List.of(
                    simple("calcite", COMMON, rubies(1), Items.CALCITE, 8),
                    simple("smooth_basalt", COMMON, rubies(1), Items.SMOOTH_BASALT, 16),
                    simple("obsidian", COMMON, rubies(4), Items.OBSIDIAN, 4),
                    simple("crying_obsidian", COMMON, rubies(5), Items.CRYING_OBSIDIAN, 4),
                    simple("polished_blackstone_bricks", COMMON, rubies(1), Items.POLISHED_BLACKSTONE_BRICKS, 16),
                    // End stone bricks: double, you need to have been to the End to get them
                    simple("end_stone_bricks", COMMON, rubies(2), Items.END_STONE_BRICKS, 16))),

            // =================================================================
            // Librarian (Bibliotecario)
            // =================================================================
            Map.entry(VillagerProfession.LIBRARIAN, List.of(
                    // Banishment I-V: round((5 + 3*level) * 0.65) -> 5/7/9/11/13 rubies (a bit pricier than the other books)
                    new IllegalTrade("banishment_book", false, (level, pos) -> {
                        int enchLevel = 1 + level.getRandom().nextInt(5);
                        return deal(RARE, rubies(Math.round((5 + 3 * enchLevel) * 0.65F)),
                                book(level, ModEnchantments.BANISHMENT, enchLevel));
                    }),
                    // Harpoon I-III: round((2 + 3*level) * 0.55) -> 3..6 rubies
                    new IllegalTrade("harpoon_book", false, (level, pos) -> {
                        int enchLevel = 1 + level.getRandom().nextInt(3);
                        return deal(RARE, rubies(Math.round((2 + 3 * enchLevel) * 0.55F)),
                                book(level, ModEnchantments.HARPOON, enchLevel));
                    }),
                    // Cowardice I-III: round((4 + 3*level) * 0.55) -> 4..7 rubies
                    new IllegalTrade("cowardice_book", false, (level, pos) -> {
                        int enchLevel = 1 + level.getRandom().nextInt(3);
                        return deal(RARE, rubies(Math.round((4 + 3 * enchLevel) * 0.55F)),
                                book(level, ModEnchantments.COWARDICE, enchLevel));
                    }),
                    // Random book enchanted like a level 30 enchanting table: 8-14 rubies
                    new IllegalTrade("level_30_book", false, (level, pos) ->
                            deal(COMMON, rubies(8 + level.getRandom().nextInt(7)), level30Book(level))),
                    // Treasure book (only once per villager)
                    new IllegalTrade("treasure_book", true, (level, pos) -> {
                        TreasureBook pick = TREASURE_BOOKS.get(level.getRandom().nextInt(TREASURE_BOOKS.size()));
                        return limitedDeal(STRANGE, rubies(pick.price()), book(level, pick.enchantment(), pick.level()), 1);
                    }))),

            // =================================================================
            // Butcher (Carnicero)
            // =================================================================
            Map.entry(VillagerProfession.BUTCHER, List.of(
                    new IllegalTrade("goat_horn", false, (level, pos) ->
                            goatHorn(level, InstrumentTags.REGULAR_GOAT_HORNS)
                                    .flatMap(horn -> deal(COMMON, rubies(7), horn))),
                    simple("rabbit_foot", COMMON, rubies(1), Items.RABBIT_FOOT, 1),
                    // Screaming goat horn: only 2 times per villager
                    new IllegalTrade("screaming_goat_horn", true, (level, pos) ->
                            goatHorn(level, InstrumentTags.SCREAMING_GOAT_HORNS)
                                    .flatMap(horn -> limitedDeal(RARE, rubies(10), horn, 2))))),

            // =================================================================
            // Cartographer (Cartógrafo) - rubies + compass, like vanilla explorer maps
            // =================================================================
            Map.entry(VillagerProfession.CARTOGRAPHER, List.of(
                    new IllegalTrade("trail_ruins_map", false, (level, pos) ->
                            explorerMap(level, pos, ModTags.Structures.ON_TRAIL_RUINS_MAPS,
                                    ModMapDecorations.TRAIL_RUINS, ModItems.BURIED_TRAIL_RUINS_MAP.get(), (byte) 2)
                                    .flatMap(map -> deal(RARE, rubies(5), compass(), map))),
                    new IllegalTrade("ancient_city_map", false, (level, pos) ->
                            explorerMap(level, pos, ModTags.Structures.ON_ANCIENT_CITY_MAPS,
                                    vanillaDecoration(level, "ancient_city", MapDecorationTypes.RED_X),
                                    Items.BURIED_ANCIENT_CITY_MAP, (byte) 2)
                                    .flatMap(map -> deal(STRANGE, rubies(15), compass(), map))),
                    new IllegalTrade("treasure_map", false, (level, pos) ->
                            explorerMap(level, pos, StructureTags.ON_TREASURE_MAPS,
                                    MapDecorationTypes.RED_X, Items.BURIED_TREASURE_MAP, (byte) 1)
                                    .flatMap(map -> deal(RARE, rubies(7), compass(), map))))),

            // =================================================================
            // Cleric (Clérigo)
            // =================================================================
            Map.entry(VillagerProfession.CLERIC, List.of(
                    new IllegalTrade("random_potion", false, (level, pos) -> randomPotion(level.getRandom())),
                    new IllegalTrade("dragon_breath", false, (level, pos) ->
                            deal(RARE, rubies(5), Optional.of(new ItemCost(Items.GLASS_BOTTLE, 1)),
                                    new ItemStack(Items.DRAGON_BREATH))),
                    // Blaze powder: 4 for 3 rubies, restocks (saves you an early trip to the Nether)
                    simple("blaze_powder", COMMON, rubies(3), Items.BLAZE_POWDER, 4),
                    // Ominous bottle (Bad Omen I-V): 5 rubies, +10% per effect level -> 5/6/6/7/7
                    new IllegalTrade("ominous_bottle", false, (level, pos) -> {
                        int amplifier = level.getRandom().nextInt(5); // 0..4 = Bad Omen I..V
                        ItemStack bottle = new ItemStack(Items.OMINOUS_BOTTLE);
                        setFromInt(bottle, DataComponents.OMINOUS_BOTTLE_AMPLIFIER, amplifier);
                        int price = (int) Math.ceil(5 * (1.0F + 0.1F * amplifier));
                        return deal(RARE, rubies(price), bottle);
                    }))),

            // =================================================================
            // Fletcher (Flechero)
            // =================================================================
            Map.entry(VillagerProfession.FLETCHER, List.of(
                    new IllegalTrade("decay_arrows", false, (level, pos) -> {
                        int variant = level.getRandom().nextInt(3); // 0 normal, 1 long, 2 strong
                        Holder<Potion> potion = variant == 0 ? ModPotions.DECAY
                                : variant == 1 ? ModPotions.LONG_DECAY : ModPotions.STRONG_DECAY;
                        ItemStack arrows = PotionContents.createItemStack(Items.TIPPED_ARROW, potion);
                        arrows.setCount(8);
                        return deal(RARE, rubies(3 + variant), arrows);
                    }),
                    simple("wind_charges", COMMON, rubies(3), Items.WIND_CHARGE, 8))),

            // =================================================================
            // Farmer (Granjero)
            // =================================================================
            Map.entry(VillagerProfession.FARMER, List.of(
                    simple("golden_apple", COMMON, rubies(6), Items.GOLDEN_APPLE, 1),
                    simple("chorus_fruit", COMMON, rubies(5), Items.CHORUS_FRUIT, 8),
                    simple("glistering_melon", COMMON, rubies(3), Items.GLISTERING_MELON_SLICE, 2))),

            // =================================================================
            // Armorer (Armero)
            // =================================================================
            Map.entry(VillagerProfession.ARMORER, List.of(
                    new IllegalTrade("ruby_armor", false, (level, pos) ->
                            deal(RARE, rubies(24), highlyEnchanted(level,
                                    ModItems.RUBY_HELMET.get(), ModItems.RUBY_CHESTPLATE.get(),
                                    ModItems.RUBY_LEGGINGS.get(), ModItems.RUBY_BOOTS.get()))),
                    new IllegalTrade("armor_trim", false, (level, pos) -> {
                        TrimPrice pick = TRIMS.get(level.getRandom().nextInt(TRIMS.size()));
                        return deal(pick.rarity(), rubies(pick.price()), new ItemStack(pick.template()));
                    }))),

            // =================================================================
            // Toolsmith (Herrero de herramientas)
            // =================================================================
            Map.entry(VillagerProfession.TOOLSMITH, List.of(
                    new IllegalTrade("ruby_tool", false, (level, pos) ->
                            deal(RARE, rubies(22), highlyEnchanted(level,
                                    ModItems.RUBY_PICKAXE.get(), ModItems.RUBY_AXE.get(),
                                    ModItems.RUBY_SHOVEL.get(), ModItems.RUBY_HOE.get()))),
                    new IllegalTrade("special_tool", false, (level, pos) -> specialTool(level)),
                    // Enchanted netherite hoe (level 15-30), base 25 rubies
                    new IllegalTrade("netherite_hoe", false, (level, pos) ->
                            deal(RARE, rubies(25), highlyEnchanted(level, Items.NETHERITE_HOE))))),

            // =================================================================
            // Weaponsmith (Herrero de armas)
            // =================================================================
            Map.entry(VillagerProfession.WEAPONSMITH, List.of(
                    new IllegalTrade("ruby_weapon", false, (level, pos) ->
                            deal(RARE, rubies(24), highlyEnchanted(level,
                                    ModItems.RUBY_SWORD.get(), ModItems.RUBY_AXE.get(), ModItems.RUBY_SPEAR.get()))),
                    // Enchanted netherite spear (level 15-30): a bit more than the netherite hoe, it's much more useful
                    new IllegalTrade("netherite_spear", false, (level, pos) ->
                            deal(RARE, rubies(28), highlyEnchanted(level, Items.NETHERITE_SPEAR))),
                    simple("breeze_rods", RARE, rubies(4), Items.BREEZE_ROD, 4),
                    // Glowing arrows (spectral arrows): 8 for 2 rubies
                    simple("spectral_arrows", COMMON, rubies(2), Items.SPECTRAL_ARROW, 8))),

            // =================================================================
            // Shepherd (Pastor) - double agent
            // =================================================================
            Map.entry(VillagerProfession.SHEPHERD, List.of(
                    simple("rebellion_pattern", RARE, rubies(5), ModItems.REBELLION_BANNER_PATTERN.get(), 1),
                    new IllegalTrade("ominous_banner", false, (level, pos) ->
                            deal(COMMON, rubies(3), ominousBanner(level))))),

            // =================================================================
            // Leatherworker (Peletero)
            // =================================================================
            Map.entry(VillagerProfession.LEATHERWORKER, List.of(
                    simple("rabbit_hide", COMMON, rubies(2), Items.RABBIT_HIDE, 8),
                    simple("phantom_membrane", COMMON, rubies(3), Items.PHANTOM_MEMBRANE, 4),
                    simple("armadillo_scutes", COMMON, rubies(4), Items.ARMADILLO_SCUTE, 2))),

            // =================================================================
            // Fisherman (Pescador)
            // =================================================================
            Map.entry(VillagerProfession.FISHERMAN, List.of(
                    simple("turtle_scute", COMMON, rubies(5), Items.TURTLE_SCUTE, 1),
                    // Axolotl bucket: only once per villager
                    simpleOnce("axolotl_bucket", RARE, rubies(10), Items.AXOLOTL_BUCKET, 1, 1),
                    simple("nautilus_shell", RARE, rubies(12), Items.NAUTILUS_SHELL, 1),
                    simple("heart_of_the_sea", STRANGE, rubies(20), Items.HEART_OF_THE_SEA, 1)))
    );

    private record PotionPrice(Holder<Potion> potion, int price, boolean rare) {}

    /**
     * Every potion the cleric can sell, with its base price (normal bottle):
     * basic 3 (long 4, II 5) · offensive 4 (long/II 5) · turtle master 7 · decay 5/6/7.
     * Turtle master and decay are Rare, the rest Common. Built on demand (the mod's potions must be registered).
     */
    private static List<PotionPrice> potionTable() {
        return List.of(
                // Basic
                basic(Potions.NIGHT_VISION, 3), basic(Potions.LONG_NIGHT_VISION, 4),
                basic(Potions.INVISIBILITY, 3), basic(Potions.LONG_INVISIBILITY, 4),
                basic(Potions.FIRE_RESISTANCE, 3), basic(Potions.LONG_FIRE_RESISTANCE, 4),
                basic(Potions.WATER_BREATHING, 3), basic(Potions.LONG_WATER_BREATHING, 4),
                basic(Potions.SLOW_FALLING, 3), basic(Potions.LONG_SLOW_FALLING, 4),
                basic(Potions.LEAPING, 3), basic(Potions.LONG_LEAPING, 4), basic(Potions.STRONG_LEAPING, 5),
                basic(Potions.SWIFTNESS, 3), basic(Potions.LONG_SWIFTNESS, 4), basic(Potions.STRONG_SWIFTNESS, 5),
                basic(Potions.SLOWNESS, 3), basic(Potions.LONG_SLOWNESS, 4), basic(Potions.STRONG_SLOWNESS, 5),
                basic(Potions.WEAKNESS, 3), basic(Potions.LONG_WEAKNESS, 4),
                basic(Potions.WIND_CHARGED, 3), basic(Potions.WEAVING, 3),
                basic(Potions.OOZING, 3), basic(Potions.INFESTED, 3),
                // Offensive
                basic(Potions.HEALING, 4), basic(Potions.STRONG_HEALING, 5),
                basic(Potions.HARMING, 4), basic(Potions.STRONG_HARMING, 5),
                basic(Potions.POISON, 4), basic(Potions.LONG_POISON, 5), basic(Potions.STRONG_POISON, 5),
                basic(Potions.REGENERATION, 4), basic(Potions.LONG_REGENERATION, 5), basic(Potions.STRONG_REGENERATION, 5),
                basic(Potions.STRENGTH, 4), basic(Potions.LONG_STRENGTH, 5), basic(Potions.STRONG_STRENGTH, 5),
                // Rare ones
                rare(Potions.TURTLE_MASTER, 7), rare(Potions.LONG_TURTLE_MASTER, 7), rare(Potions.STRONG_TURTLE_MASTER, 7),
                rare(ModPotions.DECAY, 5), rare(ModPotions.LONG_DECAY, 6), rare(ModPotions.STRONG_DECAY, 7));
    }

    private static PotionPrice basic(Holder<Potion> potion, int price) {
        return new PotionPrice(potion, price, false);
    }

    private static PotionPrice rare(Holder<Potion> potion, int price) {
        return new PotionPrice(potion, price, true);
    }

    private IllegalTrades() {}

    // =========================================================================
    // Public API
    // =========================================================================

    /** The illegal trade list of a profession, or an empty list if it has none (unemployed, nitwit...). */
    public static List<IllegalTrade> forProfession(Holder<VillagerProfession> profession) {
        return profession.unwrapKey().map(key -> TRADES.getOrDefault(key, List.of())).orElse(List.of());
    }

    // =========================================================================
    // Trade builders
    // =========================================================================

    /** Fixed trade: price -> item x count. Uses before restocking come from the rarity. */
    private static IllegalTrade simple(String name, IllegalRarity rarity, ItemCost cost, ItemLike item, int count) {
        return new IllegalTrade(name, false, (level, pos) -> deal(rarity, cost, new ItemStack(item, count)));
    }

    /** Fixed trade that disappears forever after being bought maxUses times. */
    private static IllegalTrade simpleOnce(String name, IllegalRarity rarity, ItemCost cost, ItemLike item, int count, int maxUses) {
        return new IllegalTrade(name, true, (level, pos) -> limitedDeal(rarity, cost, new ItemStack(item, count), maxUses));
    }

    private static Optional<IllegalTrade.Deal> deal(IllegalRarity rarity, ItemCost cost, ItemStack result) {
        return deal(rarity, cost, Optional.empty(), result);
    }

    private static Optional<IllegalTrade.Deal> deal(IllegalRarity rarity, ItemCost cost, Optional<ItemCost> costB, ItemStack result) {
        return offer(rarity, cost, costB, result, rarity.defaultMaxUses());
    }

    private static Optional<IllegalTrade.Deal> limitedDeal(IllegalRarity rarity, ItemCost cost, ItemStack result, int maxUses) {
        return offer(rarity, cost, Optional.empty(), result, maxUses);
    }

    /**
     * The offer's "villager XP" field stores the XP LEVELS the trade costs (2 / 4 / 6).
     * That field is sent to the client with the offer, so the trade screen can show the cost
     * in the tooltip. (Illegal villagers are always masters: the villager XP doesn't matter anymore.)
     */
    private static Optional<IllegalTrade.Deal> offer(IllegalRarity rarity, ItemCost cost, Optional<ItemCost> costB,
                                                     ItemStack result, int maxUses) {
        if (result.isEmpty()) return Optional.empty();
        MerchantOffer offer = new MerchantOffer(cost, costB, result, maxUses, rarity.levelCost(), PRICE_MULTIPLIER);
        return Optional.of(new IllegalTrade.Deal(offer, rarity));
    }

    private static ItemCost rubies(int amount) {
        return new ItemCost(ModItems.RUBY.get(), amount);
    }

    private static ItemCost rubyBlocks(int amount) {
        return new ItemCost(ModBlocks.RUBY_BLOCK.get(), amount);
    }

    private static Optional<ItemCost> compass() {
        return Optional.of(new ItemCost(Items.COMPASS, 1));
    }

    // =========================================================================
    // Item helpers
    // =========================================================================

    private static Holder<Enchantment> enchantment(Level level, ResourceKey<Enchantment> key) {
        return level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
    }

    private static ItemStack book(Level level, ResourceKey<Enchantment> key, int enchLevel) {
        return EnchantmentHelper.createBook(new EnchantmentInstance(enchantment(level, key), enchLevel));
    }

    /** A book enchanted exactly like the enchanting table at level 30 (same enchantment pool). */
    private static ItemStack level30Book(Level level) {
        RegistryAccess access = level.registryAccess();
        Optional<HolderSet.Named<Enchantment>> pool =
                access.lookupOrThrow(Registries.ENCHANTMENT).get(EnchantmentTags.IN_ENCHANTING_TABLE);
        return EnchantmentHelper.enchantItem(level.getRandom(), new ItemStack(Items.BOOK), 30, access, pool);
    }

    /**
     * Picks one of the items at random and enchants it with a HIGH enchanting level (15-30),
     * better than vanilla villagers (5-19). Same pool as vanilla trades: #minecraft:on_traded_equipment.
     */
    private static ItemStack highlyEnchanted(Level level, ItemLike... options) {
        RandomSource random = level.getRandom();
        ItemStack stack = new ItemStack(options[random.nextInt(options.length)]);
        RegistryAccess access = level.registryAccess();
        Optional<HolderSet.Named<Enchantment>> pool =
                access.lookupOrThrow(Registries.ENCHANTMENT).get(EnchantmentTags.ON_TRADED_EQUIPMENT);
        int enchantingLevel = 15 + random.nextInt(16);
        return EnchantmentHelper.enchantItem(random, stack, enchantingLevel, access, pool);
    }

    /** Brush 6 · shears 6 · flint and steel 4 · fishing rod 9. All of them with Mending + very good enchantments. */
    private static Optional<IllegalTrade.Deal> specialTool(Level level) {
        ItemStack stack;
        int price;
        switch (level.getRandom().nextInt(4)) {
            case 0 -> {
                stack = new ItemStack(Items.BRUSH);
                ench(level, stack, Enchantments.UNBREAKING, 3);
                price = 6;
            }
            case 1 -> {
                stack = new ItemStack(Items.SHEARS);
                ench(level, stack, Enchantments.EFFICIENCY, 5);
                ench(level, stack, Enchantments.UNBREAKING, 3);
                price = 6;
            }
            case 2 -> {
                stack = new ItemStack(Items.FLINT_AND_STEEL);
                ench(level, stack, Enchantments.UNBREAKING, 3);
                price = 4;
            }
            default -> {
                stack = new ItemStack(Items.FISHING_ROD);
                ench(level, stack, Enchantments.LURE, 3);
                ench(level, stack, Enchantments.LUCK_OF_THE_SEA, 3);
                ench(level, stack, Enchantments.UNBREAKING, 3);
                price = 9;
            }
        }
        ench(level, stack, Enchantments.MENDING, 1);
        return deal(COMMON, rubies(price), stack);
    }

    private static void ench(Level level, ItemStack stack, ResourceKey<Enchantment> key, int enchLevel) {
        stack.enchant(enchantment(level, key), enchLevel);
    }

    /** A random potion of the table, in a random bottle: normal 50%, splash 30% (+1), lingering 20% (+2). */
    private static Optional<IllegalTrade.Deal> randomPotion(RandomSource random) {
        List<PotionPrice> table = potionTable();
        PotionPrice pick = table.get(random.nextInt(table.size()));

        float roll = random.nextFloat();
        Item bottle = roll < 0.5F ? Items.POTION : roll < 0.8F ? Items.SPLASH_POTION : Items.LINGERING_POTION;
        int extra = bottle == Items.SPLASH_POTION ? 1 : bottle == Items.LINGERING_POTION ? 2 : 0;

        return deal(pick.rare() ? RARE : COMMON, rubies(pick.price() + extra),
                PotionContents.createItemStack(bottle, pick.potion()));
    }

    /** A goat horn with a random sound of the tag (regular or screaming horns). */
    private static Optional<ItemStack> goatHorn(Level level, TagKey<Instrument> sounds) {
        return level.registryAccess().lookupOrThrow(Registries.INSTRUMENT).get(sounds)
                .flatMap(set -> set.getRandomElement(level.getRandom()))
                .map(sound -> {
                    ItemStack horn = new ItemStack(Items.GOAT_HORN);
                    horn.set(DataComponents.INSTRUMENT, new InstrumentComponent(sound));
                    return horn;
                });
    }

    /**
     * Sets a component that is saved as a number (like the ominous bottle level), whatever its Java type is:
     * it's read with the component's own codec from the number. Works if 26.3 stores it as an Integer
     * or as a small record (OminousBottleAmplifier).
     */
    private static <T> void setFromInt(ItemStack stack, DataComponentType<T> type, int value) {
        type.codecOrThrow().parse(NbtOps.INSTANCE, IntTag.valueOf(value)).result()
                .ifPresent(component -> stack.set(type, component));
    }

    private static ItemStack ominousBanner(Level level) {
        return Raid.getOminousBannerInstance(level.registryAccess().lookupOrThrow(Registries.BANNER_PATTERN));
    }

    /**
     * A vanilla map icon looked up by its name (minecraft:<path>). If it doesn't exist, uses the fallback.
     * This way the code compiles even if we don't know the exact Java constant name.
     */
    private static Holder<MapDecorationType> vanillaDecoration(Level level, String path, Holder<MapDecorationType> fallback) {
        ResourceKey<MapDecorationType> key = ResourceKey.create(Registries.MAP_DECORATION_TYPE, Identifier.withDefaultNamespace(path));
        return level.registryAccess().lookupOrThrow(Registries.MAP_DECORATION_TYPE).get(key)
                .<Holder<MapDecorationType>>map(holder -> holder)
                .orElse(fallback);
    }

    /**
     * Explorer map, made like vanilla 26.3 does (ExplorationMapFunction):
     * finds the nearest structure of the tag (up to 100 chunks away, skipping already explored ones),
     * fills the map and marks the structure with the icon. Empty if there's no structure nearby.
     */
    private static Optional<ItemStack> explorerMap(ServerLevel level, BlockPos origin, TagKey<Structure> destination,
                                                   Holder<MapDecorationType> icon, Item mapItem, byte zoom) {
        Optional<HolderSet.Named<Structure>> structures =
                level.registryAccess().lookupOrThrow(Registries.STRUCTURE).get(destination);
        if (structures.isEmpty()) return Optional.empty();

        BlockPos target = level.findNearestMapStructure(structures.get(), origin, 100, true);
        if (target == null) return Optional.empty();

        ItemStack map = new ItemStack(mapItem);
        MapItem.applyNewSavedData(level, map, target.getX(), target.getZ(), zoom, true, true);
        MapItem.renderBiomePreviewMap(level, map);
        MapItemSavedData.addTargetDecoration(map, target, "+", icon);
        return Optional.of(map);
    }
}