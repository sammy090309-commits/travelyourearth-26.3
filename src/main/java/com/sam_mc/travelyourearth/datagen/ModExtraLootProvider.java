package com.sam_mc.travelyourearth.datagen;

import com.sam_mc.travelyourearth.TravelYourEarth;
import com.sam_mc.travelyourearth.block.ModBlocks;
import com.sam_mc.travelyourearth.enchantment.ModEnchantments;
import com.sam_mc.travelyourearth.item.ModItems;
import com.sam_mc.travelyourearth.map.ModMapDecorations;
import com.sam_mc.travelyourearth.tags.ModTags;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.trading.VillagerTrades;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.DiscardItem;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.level.storage.loot.functions.EnchantWithLevelsFunction;
import net.minecraft.world.level.storage.loot.functions.ExplorationMapFunction;
import net.minecraft.world.level.storage.loot.functions.FilteredFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemDamageFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProviders;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Extra ruby loot that is ADDED to vanilla chests, vaults and decorated pots.
 *
 * RUBIES (gem and block) follow these rules, based on the vanilla 26.3 loot tables
 * (chance per chest of getting at least one diamond / emerald):
 *   - Table with diamond only:           ruby = 1.2x the diamond.
 *   - Table with diamond and emerald:    ruby = 1.5x the diamond, but always below the emerald.
 *       * If diamond and emerald have the same chance, the ruby gets that same chance.
 *       * If the emerald is rarer than the diamond, the ruby goes right between them.
 *   - Table where every item has the same chance: ruby = that same chance.
 *   - Table with emerald only (igloo, ocean ruins): ruby = 0.8x the emerald.
 *   - Woodland mansion and pillager outpost: the mod's own values, raised just a little (x1.1).
 *   Rubies are NOT affected by RARITY_MULTIPLIER.
 *
 * RUBY EQUIPMENT (tools, weapons, armor, horse / nautilus armor) keeps the same chance, count,
 * damage and enchantment as its diamond version, multiplied by RARITY_MULTIPLIER.
 *
 * Villages: only SAVANNA villages ("the country of the ruby"): the savanna house chest gets its own
 * ruby table (that chest only exists in savanna villages, so no other village gets rubies).
 *
 * Each key is declared together with the vanilla table it hooks into (chest / vault / pot).
 * ModGlobalLootModifierProvider goes through TARGETS and creates the modifiers by itself,
 * so to add new loot you only need to: 1) declare the key here and 2) add its table in run().
 */
public class ModExtraLootProvider implements LootTableSubProvider {

    /** Which vanilla table each key hooks into, and the name of its modifier. */
    public record Target(String modifierName, String vanillaTable) {}

    // CAREFUL: this must be declared BEFORE the keys (Java initializes statics in order).
    private static final Map<ResourceKey<LootTable>, Target> TARGETS_INTERNAL = new LinkedHashMap<>();
    public static final Map<ResourceKey<LootTable>, Target> TARGETS = Collections.unmodifiableMap(TARGETS_INTERNAL);

    // =========================================================================
    // =========================  1. CHESTS (and barrels)  =====================
    // =========================================================================

    // --- Desert pyramid ----------------------------------------------------------
    public static final ResourceKey<LootTable> DESERT_PYRAMID_RUBY =
            chest("desert_pyramid", "desert_pyramid/ruby");
    public static final ResourceKey<LootTable> DESERT_PYRAMID_HORSE_ARMOR =
            chest("desert_pyramid", "desert_pyramid/horse_armor");

    // --- Jungle temple -----------------------------------------------------------
    public static final ResourceKey<LootTable> JUNGLE_TEMPLE_RUBY =
            chest("jungle_temple", "jungle_temple/ruby");
    public static final ResourceKey<LootTable> JUNGLE_TEMPLE_HORSE_ARMOR =
            chest("jungle_temple", "jungle_temple/horse_armor");

    // --- Dungeon -------------------------------------------------------------------
    public static final ResourceKey<LootTable> MONSTER_ROOM_HORSE_ARMOR =
            chest("simple_dungeon", "monster_room/horse_armor");

    // --- Abandoned mineshaft (includes sulfur cave mineshafts: same vanilla table) --
    public static final ResourceKey<LootTable> MINESHAFT_RUBY =
            chest("abandoned_mineshaft", "mineshaft/ruby");

    // --- Abandoned camp (26.3) — secret chest ----------------------------------------
    public static final ResourceKey<LootTable> ABANDONED_CAMP_SECRET_RUBY =
            chest("abandoned_camp_secret_chest", "abandoned_camp/secret_ruby");

    // Trail ruins map: one more of the "buried" explorer maps of the secret chest (1.2)
    public static final ResourceKey<LootTable> ABANDONED_CAMP_SECRET_TRAIL_RUINS_MAP =
            chest("abandoned_camp_secret_chest", "abandoned_camp/secret_trail_ruins_map");

    // --- Igloo (emerald only in vanilla) ----------------------------------------------
    public static final ResourceKey<LootTable> IGLOO_RUBY =
            chest("igloo_chest", "igloo/ruby");

    // --- Buried treasure ----------------------------------------------------------------
    public static final ResourceKey<LootTable> BURIED_TREASURE_RUBY =
            chest("buried_treasure", "buried_treasure/ruby");
    public static final ResourceKey<LootTable> BURIED_TREASURE_NAUTILUS_ARMOR =
            chest("buried_treasure", "buried_treasure/nautilus_armor");

    // --- Shipwreck -----------------------------------------------------------------------
    public static final ResourceKey<LootTable> SHIPWRECK_SUPPLY_NAUTILUS_ARMOR =
            chest("shipwreck_supply", "shipwreck/supply_nautilus_armor");
    public static final ResourceKey<LootTable> SHIPWRECK_TREASURE_RUBY =
            chest("shipwreck_treasure", "shipwreck/treasure_ruby");
    public static final ResourceKey<LootTable> SHIPWRECK_TREASURE_NAUTILUS_ARMOR =
            chest("shipwreck_treasure", "shipwreck/treasure_nautilus_armor");
    public static final ResourceKey<LootTable> SHIPWRECK_MAP_NAUTILUS_ARMOR =
            chest("shipwreck_map", "shipwreck/map_nautilus_armor");

    // --- Ocean ruins (emerald only; armor = the diamond one) ------------------------------
    public static final ResourceKey<LootTable> OCEAN_RUIN_SMALL_RUBY =
            chest("underwater_ruin_small", "ocean_ruin/small_ruby");
    public static final ResourceKey<LootTable> OCEAN_RUIN_SMALL_NAUTILUS_ARMOR =
            chest("underwater_ruin_small", "ocean_ruin/small_nautilus_armor");
    public static final ResourceKey<LootTable> OCEAN_RUIN_BIG_RUBY =
            chest("underwater_ruin_big", "ocean_ruin/big_ruby");
    public static final ResourceKey<LootTable> OCEAN_RUIN_BIG_NAUTILUS_ARMOR =
            chest("underwater_ruin_big", "ocean_ruin/big_nautilus_armor");

    // --- Stronghold (the "altar" chest is stronghold_corridor in vanilla) --------------------
    public static final ResourceKey<LootTable> STRONGHOLD_ALTAR_RUBY =
            chest("stronghold_corridor", "stronghold/altar_ruby");
    public static final ResourceKey<LootTable> STRONGHOLD_ALTAR_HORSE_ARMOR =
            chest("stronghold_corridor", "stronghold/altar_horse_armor");

    // --- Ancient city ---------------------------------------------------------------------------
    public static final ResourceKey<LootTable> ANCIENT_CITY_HOE =
            chest("ancient_city", "ancient_city/hoe_damaged_enchanted");
    public static final ResourceKey<LootTable> ANCIENT_CITY_LEGGINGS =
            chest("ancient_city", "ancient_city/leggings_enchanted");
    public static final ResourceKey<LootTable> ANCIENT_CITY_HORSE_ARMOR =
            chest("ancient_city", "ancient_city/horse_armor");

    // --- Nether fortress -------------------------------------------------------------------------
    public static final ResourceKey<LootTable> NETHER_FORTRESS_RUBY =
            chest("nether_bridge", "nether_fortress/ruby");
    public static final ResourceKey<LootTable> NETHER_FORTRESS_HORSE_ARMOR =
            chest("nether_bridge", "nether_fortress/horse_armor");

    // --- Bastion: treasure ------------------------------------------------------------------------
    public static final ResourceKey<LootTable> BASTION_TREASURE_RUBY =
            chest("bastion_treasure", "bastion/treasure_ruby");
    public static final ResourceKey<LootTable> BASTION_TREASURE_SWORD =
            chest("bastion_treasure", "bastion/treasure_sword");
    public static final ResourceKey<LootTable> BASTION_TREASURE_SWORD_DAMAGED_ENCHANTED =
            chest("bastion_treasure", "bastion/treasure_sword_damaged_enchanted");
    public static final ResourceKey<LootTable> BASTION_TREASURE_SPEAR =
            chest("bastion_treasure", "bastion/treasure_spear");
    public static final ResourceKey<LootTable> BASTION_TREASURE_SPEAR_DAMAGED_ENCHANTED =
            chest("bastion_treasure", "bastion/treasure_spear_damaged_enchanted");
    public static final ResourceKey<LootTable> BASTION_TREASURE_HELMET =
            chest("bastion_treasure", "bastion/treasure_helmet");
    public static final ResourceKey<LootTable> BASTION_TREASURE_HELMET_DAMAGED_ENCHANTED =
            chest("bastion_treasure", "bastion/treasure_helmet_damaged_enchanted");
    public static final ResourceKey<LootTable> BASTION_TREASURE_CHESTPLATE =
            chest("bastion_treasure", "bastion/treasure_chestplate");
    public static final ResourceKey<LootTable> BASTION_TREASURE_CHESTPLATE_DAMAGED_ENCHANTED =
            chest("bastion_treasure", "bastion/treasure_chestplate_damaged_enchanted");
    public static final ResourceKey<LootTable> BASTION_TREASURE_LEGGINGS =
            chest("bastion_treasure", "bastion/treasure_leggings");
    public static final ResourceKey<LootTable> BASTION_TREASURE_LEGGINGS_DAMAGED_ENCHANTED =
            chest("bastion_treasure", "bastion/treasure_leggings_damaged_enchanted");
    public static final ResourceKey<LootTable> BASTION_TREASURE_BOOTS =
            chest("bastion_treasure", "bastion/treasure_boots");
    public static final ResourceKey<LootTable> BASTION_TREASURE_BOOTS_DAMAGED_ENCHANTED =
            chest("bastion_treasure", "bastion/treasure_boots_damaged_enchanted");

    // --- Bastion: generic chest and hoglin stable ----------------------------------------------------
    public static final ResourceKey<LootTable> BASTION_OTHER_PICKAXE =
            chest("bastion_other", "bastion/other_pickaxe_enchanted");
    public static final ResourceKey<LootTable> BASTION_OTHER_SHOVEL =
            chest("bastion_other", "bastion/other_shovel");
    public static final ResourceKey<LootTable> BASTION_HOGLIN_STABLE_PICKAXE =
            chest("bastion_hoglin_stable", "bastion/hoglin_stable_pickaxe_damaged_enchanted");
    public static final ResourceKey<LootTable> BASTION_HOGLIN_STABLE_SHOVEL =
            chest("bastion_hoglin_stable", "bastion/hoglin_stable_shovel_damaged_enchanted");

    // --- End city ----------------------------------------------------------------------------------------
    public static final ResourceKey<LootTable> END_CITY_RUBY =
            chest("end_city_treasure", "end_city/ruby");
    public static final ResourceKey<LootTable> END_CITY_SWORD =
            chest("end_city_treasure", "end_city/sword_enchanted");
    public static final ResourceKey<LootTable> END_CITY_SPEAR =
            chest("end_city_treasure", "end_city/spear_enchanted");
    public static final ResourceKey<LootTable> END_CITY_PICKAXE =
            chest("end_city_treasure", "end_city/pickaxe_enchanted");
    public static final ResourceKey<LootTable> END_CITY_SHOVEL =
            chest("end_city_treasure", "end_city/shovel_enchanted");
    public static final ResourceKey<LootTable> END_CITY_HELMET =
            chest("end_city_treasure", "end_city/helmet_enchanted");
    public static final ResourceKey<LootTable> END_CITY_CHESTPLATE =
            chest("end_city_treasure", "end_city/chestplate_enchanted");
    public static final ResourceKey<LootTable> END_CITY_LEGGINGS =
            chest("end_city_treasure", "end_city/leggings_enchanted");
    public static final ResourceKey<LootTable> END_CITY_BOOTS =
            chest("end_city_treasure", "end_city/boots_enchanted");
    public static final ResourceKey<LootTable> END_CITY_HORSE_ARMOR =
            chest("end_city_treasure", "end_city/horse_armor");

    // --- Trial chambers: intersection chest and barrel --------------------------------------------------
    public static final ResourceKey<LootTable> TRIAL_INTERSECTION_RUBY =
            chest("trial_chambers/intersection", "trial_chambers/intersection_ruby");
    public static final ResourceKey<LootTable> TRIAL_INTERSECTION_RUBY_BLOCK =
            chest("trial_chambers/intersection", "trial_chambers/intersection_ruby_block");
    public static final ResourceKey<LootTable> TRIAL_INTERSECTION_PICKAXE =
            chest("trial_chambers/intersection", "trial_chambers/intersection_pickaxe_damaged");
    public static final ResourceKey<LootTable> TRIAL_INTERSECTION_AXE =
            chest("trial_chambers/intersection", "trial_chambers/intersection_axe_damaged");
    public static final ResourceKey<LootTable> TRIAL_BARREL_RUBY =
            chest("trial_chambers/intersection_barrel", "trial_chambers/barrel_ruby");
    public static final ResourceKey<LootTable> TRIAL_BARREL_PICKAXE =
            chest("trial_chambers/intersection_barrel", "trial_chambers/barrel_pickaxe_damaged");
    public static final ResourceKey<LootTable> TRIAL_BARREL_AXE =
            chest("trial_chambers/intersection_barrel", "trial_chambers/barrel_axe_damaged_enchanted");

    // --- Woodland mansion (the mod's OWN VALUES) ----------------------------------------------------------
    public static final ResourceKey<LootTable> MANSION_RUBY =
            chest("woodland_mansion", "woodland_mansion/ruby");
    public static final ResourceKey<LootTable> MANSION_SWORD =
            chest("woodland_mansion", "woodland_mansion/sword");
    public static final ResourceKey<LootTable> MANSION_SWORD_DAMAGED_ENCHANTED =
            chest("woodland_mansion", "woodland_mansion/sword_damaged_enchanted");
    public static final ResourceKey<LootTable> MANSION_SPEAR =
            chest("woodland_mansion", "woodland_mansion/spear");
    public static final ResourceKey<LootTable> MANSION_SPEAR_DAMAGED_ENCHANTED =
            chest("woodland_mansion", "woodland_mansion/spear_damaged_enchanted");
    public static final ResourceKey<LootTable> MANSION_PICKAXE =
            chest("woodland_mansion", "woodland_mansion/pickaxe_enchanted");
    public static final ResourceKey<LootTable> MANSION_AXE =
            chest("woodland_mansion", "woodland_mansion/axe_enchanted");
    public static final ResourceKey<LootTable> MANSION_SHOVEL =
            chest("woodland_mansion", "woodland_mansion/shovel_enchanted");
    public static final ResourceKey<LootTable> MANSION_HOE =
            chest("woodland_mansion", "woodland_mansion/hoe_enchanted");
    public static final ResourceKey<LootTable> MANSION_HELMET =
            chest("woodland_mansion", "woodland_mansion/helmet");
    public static final ResourceKey<LootTable> MANSION_HELMET_DAMAGED_ENCHANTED =
            chest("woodland_mansion", "woodland_mansion/helmet_damaged_enchanted");
    public static final ResourceKey<LootTable> MANSION_CHESTPLATE =
            chest("woodland_mansion", "woodland_mansion/chestplate");
    public static final ResourceKey<LootTable> MANSION_CHESTPLATE_DAMAGED_ENCHANTED =
            chest("woodland_mansion", "woodland_mansion/chestplate_damaged_enchanted");
    public static final ResourceKey<LootTable> MANSION_LEGGINGS =
            chest("woodland_mansion", "woodland_mansion/leggings");
    public static final ResourceKey<LootTable> MANSION_LEGGINGS_DAMAGED_ENCHANTED =
            chest("woodland_mansion", "woodland_mansion/leggings_damaged_enchanted");
    public static final ResourceKey<LootTable> MANSION_BOOTS =
            chest("woodland_mansion", "woodland_mansion/boots");
    public static final ResourceKey<LootTable> MANSION_BOOTS_DAMAGED_ENCHANTED =
            chest("woodland_mansion", "woodland_mansion/boots_damaged_enchanted");
    public static final ResourceKey<LootTable> MANSION_HORSE_ARMOR =
            chest("woodland_mansion", "woodland_mansion/horse_armor");
    // Banishment (1.2): illagers live in mansions, so their nemesis enchantment hides there (rare)
    public static final ResourceKey<LootTable> MANSION_BANISHMENT_BOOK =
            chest("woodland_mansion", "woodland_mansion/banishment_book");
    public static final ResourceKey<LootTable> MANSION_SWORD_BANISHMENT =
            chest("woodland_mansion", "woodland_mansion/sword_banishment");
    public static final ResourceKey<LootTable> MANSION_SPEAR_BANISHMENT =
            chest("woodland_mansion", "woodland_mansion/spear_banishment");
    public static final ResourceKey<LootTable> MANSION_AXE_BANISHMENT =
            chest("woodland_mansion", "woodland_mansion/axe_banishment");

    // --- Pillager outpost (the mod's OWN VALUES) -------------------------------------------------------------
    public static final ResourceKey<LootTable> OUTPOST_RUBY =
            chest("pillager_outpost", "pillager_outpost/ruby");
    public static final ResourceKey<LootTable> OUTPOST_SWORD =
            chest("pillager_outpost", "pillager_outpost/sword_damaged");
    public static final ResourceKey<LootTable> OUTPOST_SPEAR =
            chest("pillager_outpost", "pillager_outpost/spear_damaged");
    public static final ResourceKey<LootTable> OUTPOST_PICKAXE =
            chest("pillager_outpost", "pillager_outpost/pickaxe_damaged");
    public static final ResourceKey<LootTable> OUTPOST_AXE =
            chest("pillager_outpost", "pillager_outpost/axe_damaged");
    public static final ResourceKey<LootTable> OUTPOST_CHESTPLATE =
            chest("pillager_outpost", "pillager_outpost/chestplate_damaged");
    public static final ResourceKey<LootTable> OUTPOST_BOOTS =
            chest("pillager_outpost", "pillager_outpost/boots_damaged");
    public static final ResourceKey<LootTable> OUTPOST_HORSE_ARMOR =
            chest("pillager_outpost", "pillager_outpost/horse_armor");

    // --- Savanna village house ("the country of the ruby": the mod's OWN VALUE) -----------------------------
    // "village_savanna_house" only exists in savanna villages, so no other village gets this ruby.
    public static final ResourceKey<LootTable> SAVANNA_HOUSE_RUBY =
            chest("village/village_savanna_house", "village/savanna_house_ruby");

    // =========================================================================
    // =========================  2. VAULTS (Trial chambers)  ==================
    // =========================================================================
    // "chests/trial_chambers/reward" is the table of the normal vault (and of the reward chests)
    public static final ResourceKey<LootTable> VAULT_RUBY =
            vault("trial_chambers/reward", "trial_chambers/ruby");
    public static final ResourceKey<LootTable> VAULT_SPEAR =
            vault("trial_chambers/reward", "trial_chambers/spear_enchanted");
    public static final ResourceKey<LootTable> VAULT_LEGGINGS =
            vault("trial_chambers/reward", "trial_chambers/leggings_enchanted");

    public static final ResourceKey<LootTable> OMINOUS_VAULT_RUBY =
            vault("trial_chambers/reward_ominous", "trial_chambers/ominous_ruby");
    public static final ResourceKey<LootTable> OMINOUS_VAULT_RUBY_BLOCK =
            vault("trial_chambers/reward_ominous", "trial_chambers/ominous_ruby_block");
    public static final ResourceKey<LootTable> OMINOUS_VAULT_SPEAR =
            vault("trial_chambers/reward_ominous", "trial_chambers/ominous_spear_enchanted");
    public static final ResourceKey<LootTable> OMINOUS_VAULT_LEGGINGS =
            vault("trial_chambers/reward_ominous", "trial_chambers/ominous_leggings_enchanted");
    // Cowardice and Harpoon (1.2): very rare, ONLY in the ominous vault
    public static final ResourceKey<LootTable> OMINOUS_VAULT_SPEAR_HARPOON =
            vault("trial_chambers/reward_ominous", "trial_chambers/ominous_spear_harpoon");
    public static final ResourceKey<LootTable> OMINOUS_VAULT_COWARDICE_BOOK =
            vault("trial_chambers/reward_ominous", "trial_chambers/ominous_cowardice_book");
    public static final ResourceKey<LootTable> OMINOUS_VAULT_HARPOON_BOOK =
            vault("trial_chambers/reward_ominous", "trial_chambers/ominous_harpoon_book");

    // =========================================================================
    // =========================  3. DECORATED POTS  ===========================
    // =========================================================================
    public static final ResourceKey<LootTable> TRIAL_CORRIDOR_POT_RUBY =
            pot("trial_chambers/corridor", "trial_chambers/corridor_ruby");

    // (4. Archaeology and 5. Equipment live in ModGlobalLootModifierProvider:
    //  they don't use extra tables, they use their own modifiers.)

    // =========================================================================
    // KEYS: helpers
    // =========================================================================

    /** Hooks into "minecraft:chests/<vanillaChest>". */
    private static ResourceKey<LootTable> chest(String vanillaChest, String path) {
        return register("chests/" + vanillaChest, "chests/" + path);
    }

    /** Vaults: their vanilla table also lives in "chests/...", but they go in their own section. */
    private static ResourceKey<LootTable> vault(String vanillaVault, String path) {
        return register("chests/" + vanillaVault, "vaults/" + path);
    }

    /** Hooks into "minecraft:pots/<vanillaPot>". */
    private static ResourceKey<LootTable> pot(String vanillaPot, String path) {
        return register("pots/" + vanillaPot, "pots/" + path);
    }

    private static ResourceKey<LootTable> register(String vanillaTable, String path) {
        // The table is generated in data/travelyourearth/loot_table/extra/<path>.json
        ResourceKey<LootTable> key = ResourceKey.create(Registries.LOOT_TABLE,
                Identifier.fromNamespaceAndPath(TravelYourEarth.MODID, "extra/" + path));
        // The modifier has the same name: data/travelyourearth/loot_modifiers/<path>.json
        if (TARGETS_INTERNAL.put(key, new Target(path, vanillaTable)) != null) {
            throw new IllegalStateException("Duplicated loot key: " + path);
        }
        return key;
    }

    // =========================================================================
    // RARITY
    // =========================================================================

    /**
     * Multiplies the chances of the ruby EQUIPMENT in this file (not the rubies themselves).
     *   1.0  = same as the vanilla diamond equipment
     *   0.75 = 25% less likely (a bit rarer)   <- current
     *   0.5  = half as likely
     * Counts, damage and enchantments do NOT change.
     * It's public so archaeology uses the same value (ModGlobalLootModifierProvider).
     */
    public static final float RARITY_MULTIPLIER = 0.75f;

    /**
     * 1.2: a little more loot in general (x1.1): rubies, ruby equipment, archaeology, savanna chests.
     * NOT for the tables where the ruby copies a "repeated" chance (sameAsAll, diamond == emerald,
     * SAME_AS_EMERALD in archaeology). Public: ModGlobalLootModifierProvider uses it too.
     */
    public static final float LOOT_BOOST = 1.1f;
    /** Ruby weapons (sword, spear, axe) get a smaller boost. */
    private static final float WEAPON_BOOST = 1.05f;
    /** Pillager outposts and woodland mansions (the illagers' homes): MUCH more ruby loot. */
    private static final float ILLAGER_HOME_BOOST = 1.5f;
    /** Mansion weapons: this part of them comes with Banishment instead of plain. */
    private static final float MANSION_BANISHMENT_SHARE = 0.15f;
    /** Ominous vault spear: this part of them comes with Harpoon too. */
    private static final float OMINOUS_SPEAR_HARPOON_SHARE = 0.10f;

    /** Ruby in a table with diamond only: 1.2x the diamond. */
    private static final float RUBY_WITH_DIAMOND = 1.2f;
    /** Ruby in a table with diamond and emerald: 1.5x the diamond (always below the emerald). */
    private static final float RUBY_WITH_DIAMOND_AND_EMERALD = 1.5f;
    /** Highest the ruby can get compared to the emerald (90% of it). */
    private static final float RUBY_MAX_VS_EMERALD = 0.9f;
    /** Ruby in a table with emerald only: 0.8x the emerald. */
    private static final float RUBY_EMERALD_ONLY = 0.8f;
    /** Savanna village house: chance per chest of getting rubies (own value, very common). */
    private static final float SAVANNA_HOUSE_RUBY_CHANCE = 0.45f;
    /** Woodland mansion and pillager outpost: own values raised just a little. */
    private static final float RUBY_OWN_VALUES_BOOST = 1.1f;

    /** Table with diamond only. */
    private static float withDiamond(float diamond) {
        return diamond * RUBY_WITH_DIAMOND * LOOT_BOOST;
    }

    /** Table with diamond and emerald. */
    private static float withDiamondAndEmerald(float diamond, float emerald) {
        if (diamond == emerald) return diamond;                  // same chance -> same chance (no boost)
        if (emerald < diamond) return (diamond + emerald) / 2f * LOOT_BOOST;  // emerald rarer -> between them
        return Math.min(diamond * RUBY_WITH_DIAMOND_AND_EMERALD * LOOT_BOOST, emerald * RUBY_MAX_VS_EMERALD);
    }

    /** Table where every item has the same chance. */
    private static float sameAsAll(float chance) {
        return chance;
    }

    /** Table with emerald only. */
    private static float emeraldOnly(float emerald) {
        return emerald * RUBY_EMERALD_ONLY * LOOT_BOOST;
    }

    /** Woodland mansion and pillager outpost (base value -> same as before, x1.1). */
    private static float ownValues(float base) {
        return base * RARITY_MULTIPLIER * RUBY_OWN_VALUES_BOOST * ILLAGER_HOME_BOOST;
    }

    // =========================================================================
    // GENERATION
    // =========================================================================

    // 26.3: LootTableSubProvider receives a Context and overrides run().
    private final LootTableSubProvider.Context output;
    private HolderGetter<Enchantment> enchantments; // filled at the start of run()
    private HolderGetter<Structure> structures;     // filled at the start of run()
    private final Set<ResourceKey<LootTable>> generated = new HashSet<>();

    public ModExtraLootProvider(LootTableSubProvider.Context output) {
        this.output = output;
    }

    @Override
    public void run() {
        this.enchantments = this.output.lookup(Registries.ENCHANTMENT);
        this.structures = this.output.lookup(Registries.STRUCTURE);

        // Shortcuts so the lines aren't super long
        ItemLike ruby = ModItems.RUBY.get();
        ItemLike rubyBlock = ModBlocks.RUBY_BLOCK.get();
        ItemLike sword = ModItems.RUBY_SWORD.get();
        ItemLike spear = ModItems.RUBY_SPEAR.get();
        ItemLike pickaxe = ModItems.RUBY_PICKAXE.get();
        ItemLike axe = ModItems.RUBY_AXE.get();
        ItemLike shovel = ModItems.RUBY_SHOVEL.get();
        ItemLike hoe = ModItems.RUBY_HOE.get();
        ItemLike helmet = ModItems.RUBY_HELMET.get();
        ItemLike chestplate = ModItems.RUBY_CHESTPLATE.get();
        ItemLike leggings = ModItems.RUBY_LEGGINGS.get();
        ItemLike boots = ModItems.RUBY_BOOTS.get();
        ItemLike horseArmor = ModItems.RUBY_HORSE_ARMOR.get();
        ItemLike nautilusArmor = ModItems.RUBY_NAUTILUS_ARMOR.get();

        // How to read each line:
        //   addRuby(KEY, rule(vanilla diamond, vanilla emerald), item, [count...])  -> exact chance
        //   add(KEY, BASE_chance_per_chest, item, [count / damage / enchant...])    -> base x RARITY_MULTIPLIER
        //   damage(min, max) = REMAINING durability (1.0 = new)
        //   The % in the comments are the final chances per chest.

        // =====================================================================
        // 1. CHESTS
        // =====================================================================

        // --- Desert pyramid (vanilla: diamond 5.9%, emerald 17.0%) ---
        addRuby(DESERT_PYRAMID_RUBY,       withDiamondAndEmerald(0.0594f, 0.1703f), ruby, count(1, 4));  // 8.9%
        add(DESERT_PYRAMID_HORSE_ARMOR,    0.0894f, horseArmor);  // 8.9%

        // --- Jungle temple (vanilla: diamond 12.7%, emerald 8.6%) ---
        addRuby(JUNGLE_TEMPLE_RUBY,        withDiamondAndEmerald(0.1271f, 0.0864f), ruby, count(1, 4));  // 10.7%
        add(JUNGLE_TEMPLE_HORSE_ARMOR,     0.0731f, horseArmor);  // 7.3%

        // --- Dungeon ---
        add(MONSTER_ROOM_HORSE_ARMOR,      0.0679f, horseArmor);  // 6.8%

        // --- Abandoned mineshaft, normal and sulfur cave (vanilla: diamond 8.9%) ---
        addRuby(MINESHAFT_RUBY,            withDiamond(0.0888f), ruby, count(1, 2));  // 10.7%

        // --- Abandoned camp: secret chest (vanilla: diamond and the 4 potions, all 36.0%) ---
        addRuby(ABANDONED_CAMP_SECRET_RUBY, sameAsAll(0.36f), ruby, count(1, 3));  // 36.0%
        // Trail ruins map: vanilla gives 1 of 7 "buried" maps here (14.3% each); ours is like an 8th one.
        // Same functions as vanilla's maps: finds the nearest trail ruins, and if there's none, no map.
        addRuby(ABANDONED_CAMP_SECRET_TRAIL_RUINS_MAP, 0.125f, ModItems.BURIED_TRAIL_RUINS_MAP.get(),
                ExplorationMapFunction.makeExplorationMap(this.structures.getOrThrow(ModTags.Structures.ON_TRAIL_RUINS_MAPS))
                        .setMapDecoration(ModMapDecorations.TRAIL_RUINS)
                        .setSkipKnownStructures(true),
                discardIfNotValidMap());  // 12.5%

        // --- Igloo (vanilla: emerald 7.6%) ---
        addRuby(IGLOO_RUBY,                emeraldOnly(0.0764f), ruby);  // 6.1%

        // --- Buried treasure (vanilla: diamond 53.1%, emerald 53.1%) ---
        addRuby(BURIED_TREASURE_RUBY,        withDiamondAndEmerald(0.5309f, 0.5309f), ruby, count(2, 5));  // 53.1%
        add(BURIED_TREASURE_NAUTILUS_ARMOR,  0.0116f, nautilusArmor);  // 1.2%

        // --- Shipwreck (treasure, vanilla: diamond 14.1%, emerald 73.7%) ---
        add(SHIPWRECK_SUPPLY_NAUTILUS_ARMOR,    0.0108f, nautilusArmor);  // 1.1%
        addRuby(SHIPWRECK_TREASURE_RUBY,        withDiamondAndEmerald(0.1409f, 0.7372f), ruby);  // 21.1%
        add(SHIPWRECK_TREASURE_NAUTILUS_ARMOR,  0.0168f, nautilusArmor);  // 1.7%
        add(SHIPWRECK_MAP_NAUTILUS_ARMOR,       0.0108f, nautilusArmor);  // 1.1%

        // --- Ocean ruins (vanilla: emerald 15.4% small, 14.1% big) ---
        addRuby(OCEAN_RUIN_SMALL_RUBY,        emeraldOnly(0.154f), ruby);  // 12.3%
        add(OCEAN_RUIN_SMALL_NAUTILUS_ARMOR,  0.0063f, nautilusArmor);  // 0.6%
        addRuby(OCEAN_RUIN_BIG_RUBY,          emeraldOnly(0.141f), ruby);  // 11.3%
        add(OCEAN_RUIN_BIG_NAUTILUS_ARMOR,    0.0066f, nautilusArmor);  // 0.7%

        // --- Stronghold altar (vanilla: diamond 7.3%) ---
        addRuby(STRONGHOLD_ALTAR_RUBY,     withDiamond(0.0725f), ruby, count(2, 3));  // 8.7%
        add(STRONGHOLD_ALTAR_HORSE_ARMOR,  0.0276f, horseArmor);  // 2.8%

        // --- Ancient city (enchantment level 30-50) ---
        add(ANCIENT_CITY_HOE,          0.1646f, hoe, damage(0.8f, 1.0f), enchantLevels(30, 50));  // 16.5%
        add(ANCIENT_CITY_LEGGINGS,     0.1646f, leggings, enchantLevels(30, 50));  // 16.5%
        add(ANCIENT_CITY_HORSE_ARMOR,  0.1646f, horseArmor);  // 16.5%

        // --- Nether fortress (vanilla: diamond 17.9%) ---
        addRuby(NETHER_FORTRESS_RUBY,     withDiamond(0.179f), ruby, count(1, 3));  // 21.5%
        add(NETHER_FORTRESS_HORSE_ARMOR,  0.1105f, horseArmor);  // 11.1%

        // --- Bastion treasure (vanilla: diamond 12.8%; random enchantment of any level) ---
        addRuby(BASTION_TREASURE_RUBY,                     withDiamond(0.128f), ruby, count(3, 8));  // 15.4%
        add(BASTION_TREASURE_SWORD,                        0.1604f, sword);  // 16%
        add(BASTION_TREASURE_SWORD_DAMAGED_ENCHANTED,      0.1604f, sword, damage(0.8f, 1.0f), enchantRandomly());  // 16%
        add(BASTION_TREASURE_SPEAR,                        0.1604f, spear);  // 16%
        add(BASTION_TREASURE_SPEAR_DAMAGED_ENCHANTED,      0.1604f, spear, damage(0.8f, 1.0f), enchantRandomly());  // 16%
        add(BASTION_TREASURE_HELMET,                       0.1348f, helmet);  // 13.5%
        add(BASTION_TREASURE_HELMET_DAMAGED_ENCHANTED,     0.1604f, helmet, damage(0.8f, 1.0f), enchantRandomly());  // 16%
        add(BASTION_TREASURE_CHESTPLATE,                   0.1348f, chestplate);  // 13.5%
        add(BASTION_TREASURE_CHESTPLATE_DAMAGED_ENCHANTED, 0.1604f, chestplate, damage(0.8f, 1.0f), enchantRandomly());  // 16%
        add(BASTION_TREASURE_LEGGINGS,                     0.1348f, leggings);  // 13.5%
        add(BASTION_TREASURE_LEGGINGS_DAMAGED_ENCHANTED,   0.1604f, leggings, damage(0.8f, 1.0f), enchantRandomly());  // 16%
        add(BASTION_TREASURE_BOOTS,                        0.1348f, boots);  // 13.5%
        add(BASTION_TREASURE_BOOTS_DAMAGED_ENCHANTED,      0.1604f, boots, damage(0.8f, 1.0f), enchantRandomly());  // 16%

        // --- Bastion: generic chest and hoglin stable ---
        add(BASTION_OTHER_PICKAXE,          0.0674f, pickaxe, enchantRandomly());  // 6.7%
        add(BASTION_OTHER_SHOVEL,           0.0674f, shovel);  // 6.7% (vanilla: not enchanted)
        add(BASTION_HOGLIN_STABLE_PICKAXE,  0.12f, pickaxe, damage(0.15f, 0.95f), enchantRandomly());  // 12%
        add(BASTION_HOGLIN_STABLE_SHOVEL,   0.15f, shovel, damage(0.15f, 0.8f), enchantRandomly());  // 15%

        // --- End city (vanilla: diamond 20.4%, emerald 8.6%; enchantment level 20-39) ---
        addRuby(END_CITY_RUBY,     withDiamondAndEmerald(0.2038f, 0.0864f), ruby, count(2, 8));  // 14.5%
        add(END_CITY_SWORD,        0.2146f, sword, enchantLevels(20, 39));  // 21.5%
        add(END_CITY_SPEAR,        0.2146f, spear, enchantLevels(20, 39));  // 21.5%
        add(END_CITY_PICKAXE,      0.2146f, pickaxe, enchantLevels(20, 39));  // 21.5%
        add(END_CITY_SHOVEL,       0.2146f, shovel, enchantLevels(20, 39));  // 21.5%
        add(END_CITY_HELMET,       0.2146f, helmet, enchantLevels(20, 39));  // 21.5%
        add(END_CITY_CHESTPLATE,   0.2146f, chestplate, enchantLevels(20, 39));  // 21.5%
        add(END_CITY_LEGGINGS,     0.2146f, leggings, enchantLevels(20, 39));  // 21.5%
        add(END_CITY_BOOTS,        0.2146f, boots, enchantLevels(20, 39));  // 21.5%
        add(END_CITY_HORSE_ARMOR,  0.0745f, horseArmor);  // 7.4%

        // --- Trial chambers: intersection chest (vanilla: diamond 21.5%; diamond block 2.3%, emerald block 11.2%) ---
        addRuby(TRIAL_INTERSECTION_RUBY,        withDiamond(0.2151f), ruby, count(1, 3));  // 25.8%
        addRuby(TRIAL_INTERSECTION_RUBY_BLOCK,  withDiamondAndEmerald(0.0231f, 0.1118f), rubyBlock);  // 3.5%
        add(TRIAL_INTERSECTION_PICKAXE,         0.1422f, pickaxe, damage(0.1f, 0.5f));  // 14.2%
        add(TRIAL_INTERSECTION_AXE,             0.1422f, axe, damage(0.1f, 0.5f));  // 14.2%

        // --- Trial chambers: intersection barrel (vanilla: diamond 5.9%) ---
        addRuby(TRIAL_BARREL_RUBY,  withDiamond(0.0594f), ruby, count(1, 2));  // 7.1%
        add(TRIAL_BARREL_PICKAXE,   0.0609f, pickaxe, damage(0.15f, 0.8f));  // 6.1%
        add(TRIAL_BARREL_AXE,       0.0609f, axe, damage(0.4f, 0.9f), enchantRandomly());  // 6.1%

        // --- Woodland mansion (OWN VALUES; enchantment 15-30) ---
        addRuby(MANSION_RUBY,                      ownValues(0.32f), ruby, count(2, 4));  // 39.6% (1.2: x1.5, the illagers' home)
        addIllagerHome(MANSION_SWORD,                         0.034f * (1 - MANSION_BANISHMENT_SHARE), sword);
        addIllagerHome(MANSION_SWORD_DAMAGED_ENCHANTED,       0.012f, sword, damage(0.8f, 1.0f), enchantLevels(15, 30));
        addIllagerHome(MANSION_SPEAR,                         0.034f * (1 - MANSION_BANISHMENT_SHARE), spear);
        addIllagerHome(MANSION_SPEAR_DAMAGED_ENCHANTED,       0.012f, spear, damage(0.8f, 1.0f), enchantLevels(15, 30));
        addIllagerHome(MANSION_PICKAXE,                       0.034f, pickaxe, enchantLevels(15, 30));
        addIllagerHome(MANSION_AXE,                           0.034f * (1 - MANSION_BANISHMENT_SHARE), axe, enchantLevels(15, 30));
        addIllagerHome(MANSION_SHOVEL,                        0.034f, shovel, enchantLevels(15, 30));
        addIllagerHome(MANSION_HOE,                           0.034f, hoe, enchantLevels(15, 30));
        addIllagerHome(MANSION_HELMET,                        0.032f, helmet);
        addIllagerHome(MANSION_HELMET_DAMAGED_ENCHANTED,      0.021f, helmet, damage(0.8f, 1.0f), enchantLevels(15, 30));
        addIllagerHome(MANSION_CHESTPLATE,                    0.032f, chestplate);
        addIllagerHome(MANSION_CHESTPLATE_DAMAGED_ENCHANTED,  0.021f, chestplate, damage(0.8f, 1.0f), enchantLevels(15, 30));
        addIllagerHome(MANSION_LEGGINGS,                      0.032f, leggings);
        addIllagerHome(MANSION_LEGGINGS_DAMAGED_ENCHANTED,    0.021f, leggings, damage(0.8f, 1.0f), enchantLevels(15, 30));
        addIllagerHome(MANSION_BOOTS,                         0.032f, boots);
        addIllagerHome(MANSION_BOOTS_DAMAGED_ENCHANTED,       0.021f, boots, damage(0.8f, 1.0f), enchantLevels(15, 30));
        addIllagerHome(MANSION_HORSE_ARMOR,                   0.012f, horseArmor);
        // Banishment: 15% of the mansion's ruby weapons come with it, and a rare book (3% per chest)
        addIllagerHome(MANSION_SWORD_BANISHMENT, 0.034f * MANSION_BANISHMENT_SHARE, sword, onlyEnchantment(ModEnchantments.BANISHMENT));
        addIllagerHome(MANSION_SPEAR_BANISHMENT, 0.034f * MANSION_BANISHMENT_SHARE, spear, onlyEnchantment(ModEnchantments.BANISHMENT));
        addIllagerHome(MANSION_AXE_BANISHMENT,   0.034f * MANSION_BANISHMENT_SHARE, axe, onlyEnchantment(ModEnchantments.BANISHMENT));
        addRuby(MANSION_BANISHMENT_BOOK, 0.03f, Items.BOOK, onlyEnchantment(ModEnchantments.BANISHMENT));  // 3%

        // --- Pillager outpost (OWN VALUES) ---
        addRuby(OUTPOST_RUBY,     ownValues(0.45f), ruby, count(1, 3));  // 55.7% (1.2: x1.5, the illagers' home)
        addIllagerHome(OUTPOST_SWORD,        0.15f, sword, damage(0.3f, 0.9f));
        addIllagerHome(OUTPOST_SPEAR,        0.15f, spear, damage(0.3f, 0.9f));
        addIllagerHome(OUTPOST_PICKAXE,      0.176f, pickaxe, damage(0.3f, 0.9f));
        addIllagerHome(OUTPOST_AXE,          0.172f, axe, damage(0.3f, 0.9f));
        addIllagerHome(OUTPOST_CHESTPLATE,   0.092f, chestplate, damage(0.3f, 0.9f));
        addIllagerHome(OUTPOST_BOOTS,        0.092f, boots, damage(0.3f, 0.9f));
        addIllagerHome(OUTPOST_HORSE_ARMOR,  0.10f, horseArmor);

        // --- Savanna village house (OWN VALUE: the ruby is the savanna's money, so it's common).
        //     Vanilla emerald there: ~11% per chest -> the ruby is way more common on purpose. ---
        addRuby(SAVANNA_HOUSE_RUBY, SAVANNA_HOUSE_RUBY_CHANCE * LOOT_BOOST, ruby, count(1, 4));  // 49.5%

        // =====================================================================
        // 2. VAULTS
        // =====================================================================

        // --- Normal vault (vanilla: diamond 8.5%, emerald 38.4%; enchantment level 5-15) ---
        addRuby(VAULT_RUBY,  withDiamondAndEmerald(0.0853f, 0.3844f), ruby, count(1, 2));  // 12.8%
        // (vanilla gives a diamond axe and chestplate here; in the mod: spear and leggings with the same values)
        add(VAULT_SPEAR,     0.0381f, spear, enchantLevels(5, 15));  // 3.8%
        add(VAULT_LEGGINGS,  0.0381f, leggings, enchantLevels(5, 15));  // 3.8%

        // --- Ominous vault (vanilla: diamond 26.4%, emerald 56.2%; diamond block 2.8%, emerald block 13.8%;
        //     enchantment level 10-20) ---
        addRuby(OMINOUS_VAULT_RUBY,        withDiamondAndEmerald(0.2639f, 0.5621f), ruby, count(2, 3));  // 39.6%
        addRuby(OMINOUS_VAULT_RUBY_BLOCK,  withDiamondAndEmerald(0.0276f, 0.1379f), rubyBlock);  // 4.1%
        add(OMINOUS_VAULT_SPEAR,           0.0828f * (1 - OMINOUS_SPEAR_HARPOON_SHARE), spear, enchantLevels(10, 20));
        // 10% of the ominous spears come with Harpoon too; Cowardice and Harpoon books: 2.5% each (very rare)
        add(OMINOUS_VAULT_SPEAR_HARPOON,   0.0828f * OMINOUS_SPEAR_HARPOON_SHARE, spear, enchantLevels(10, 20),
                onlyEnchantment(ModEnchantments.HARPOON));
        addRuby(OMINOUS_VAULT_COWARDICE_BOOK, 0.025f, Items.BOOK, onlyEnchantment(ModEnchantments.COWARDICE));  // 2.5%
        addRuby(OMINOUS_VAULT_HARPOON_BOOK,   0.025f, Items.BOOK, onlyEnchantment(ModEnchantments.HARPOON));    // 2.5%
        add(OMINOUS_VAULT_LEGGINGS,        0.0828f, leggings, enchantLevels(10, 20));  // 8.3%

        // =====================================================================
        // 3. DECORATED POTS
        // =====================================================================

        // --- Trial chambers corridor (vanilla: diamond 1.4%, emerald 35.6%) ---
        addRuby(TRIAL_CORRIDOR_POT_RUBY,  withDiamondAndEmerald(0.0142f, 0.3561f), ruby, count(2, 3));  // 2.1%

        // Safety check: if you declared a key above and forgot its table, datagen warns you.
        for (ResourceKey<LootTable> key : TARGETS.keySet()) {
            if (!this.generated.contains(key)) {
                throw new IllegalStateException("Key without a table in ModExtraLootProvider: " + TARGETS.get(key).modifierName());
            }
        }
    }

    // =========================================================================
    // TABLE HELPERS
    // =========================================================================

    /**
     * Ruby EQUIPMENT: 1 pool, 1 roll, with "chance x RARITY_MULTIPLIER" of giving the item,
     * applying the functions in order.
     * Example: add(KEY, 0.15f, sword, damage(0.8f, 1.0f), enchantRandomly());
     */
    private void add(ResourceKey<LootTable> key, float chance, ItemLike item, LootItemFunction.Builder... functions) {
        addTable(key, chance * RARITY_MULTIPLIER * boostFor(item), item, functions);
    }

    /** Pillager outposts and woodland mansions: like add(), but x1.5 (ILLAGER_HOME_BOOST). */
    private void addIllagerHome(ResourceKey<LootTable> key, float chance, ItemLike item, LootItemFunction.Builder... functions) {
        add(key, chance * ILLAGER_HOME_BOOST, item, functions);
    }

    /** The general boost: weapons a bit less than the rest. */
    private static float boostFor(ItemLike item) {
        Item it = item.asItem();
        boolean weapon = it == ModItems.RUBY_SWORD.get() || it == ModItems.RUBY_SPEAR.get() || it == ModItems.RUBY_AXE.get();
        return weapon ? WEAPON_BOOST : LOOT_BOOST;
    }

    /**
     * Exactly this enchantment, at a random level (I..max). On a book it becomes an enchanted book.
     * Only if the item supports it (vanilla enchant_randomly checks that by itself).
     */
    private LootItemFunction.Builder onlyEnchantment(ResourceKey<Enchantment> enchantment) {
        return EnchantRandomlyFunction.randomEnchantment().withEnchantment(this.enchantments.getOrThrow(enchantment));
    }

    /** Same as vanilla's explorer maps: if no structure was found (blank map), the map is thrown away. */
    private static LootItemFunction.Builder discardIfNotValidMap() {
        return FilteredFunction.filtered(VillagerTrades.anyValidMap().build()).onFail(DiscardItem.discardItem().build());
    }

    /**
     * RUBIES (gem and block): 1 pool, 1 roll, with exactly "chance" of giving the item.
     * NOT affected by RARITY_MULTIPLIER (the rules above already set the final chance).
     */
    private void addRuby(ResourceKey<LootTable> key, float chance, ItemLike item, LootItemFunction.Builder... functions) {
        addTable(key, chance, item, functions);
    }

    private void addTable(ResourceKey<LootTable> key, float finalChance, ItemLike item, LootItemFunction.Builder... functions) {
        if (!TARGETS.containsKey(key)) {
            throw new IllegalStateException("Table without a registered key: " + key);
        }
        if (!this.generated.add(key)) {
            throw new IllegalStateException("Table generated twice: " + TARGETS.get(key).modifierName());
        }
        // "var": this way we don't depend on the exact type name that lootTableItem returns in 26.3
        var entry = LootItem.lootTableItem(item.asItem());
        for (LootItemFunction.Builder function : functions) {
            entry.apply(function);
        }
        this.output.accept(key, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(finalChance))
                        .add(entry)));
    }

    /** Random count between min and max (both included). */
    private static LootItemFunction.Builder count(int min, int max) {
        return SetItemCountFunction.setCount(ContextIntProviders.between(min, max));
    }

    /** REMAINING durability between min and max (1.0 = new, 0.1 = almost broken). */
    private static LootItemFunction.Builder damage(float min, float max) {
        return SetItemDamageFunction.setDamage(ContextFloatProviders.between(min, max));
    }

    /** Like an enchanting table of level min-max (vanilla: end city 20-39, ancient city 30-50). */
    private LootItemFunction.Builder enchantLevels(int min, int max) {
        return EnchantWithLevelsFunction.enchantWithLevels(this.enchantments,
                ContextIntProviders.between(min, max));
    }

    /** One random enchantment of any level (vanilla: bastions). */
    private LootItemFunction.Builder enchantRandomly() {
        return EnchantRandomlyFunction.randomEnchantment()
                .withOneOf(this.enchantments.getOrThrow(EnchantmentTags.ON_RANDOM_LOOT));
    }
}