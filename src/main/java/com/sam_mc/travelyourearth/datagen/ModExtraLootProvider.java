package com.sam_mc.travelyourearth.datagen;

import com.sam_mc.travelyourearth.TravelYourEarth;
import com.sam_mc.travelyourearth.block.ModBlocks;
import com.sam_mc.travelyourearth.item.ModItems;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.level.storage.loot.functions.EnchantWithLevelsFunction;
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
 * Loot extra del rubí que se AÑADE a cofres, vaults y jarrones de vanilla.
 *
 * Regla general (valores de la wiki oficial, Java Edition):
 *   - Donde vanilla pone diamante / herramienta / arma / armadura / bloque de diamante,
 *     aquí va su versión de rubí con la MISMA probabilidad por cofre, cantidad,
 *     daño y encantamiento.
 *   - Donde solo hay esmeralda (iglú, ruinas oceánicas), el rubí usa la probabilidad de la esmeralda.
 *   - Mansión y puesto de saqueadores: valores propios del mod (no hay diamante equivalente).
 *   - Aldeas: sin rubí.
 *
 * Cada key se declara junto a la tabla vanilla a la que se engancha (chest / vault / pot).
 * ModGlobalLootModifierProvider recorre TARGETS y crea los modificadores solo,
 * así que para añadir loot nuevo basta con: 1) declarar la key aquí y 2) su tabla en run().
 */
public class ModExtraLootProvider implements LootTableSubProvider {

    /** A qué tabla vanilla se engancha cada key, y con qué nombre de modificador. */
    public record Target(String modifierName, String vanillaTable) {}

    // OJO: tiene que estar declarado ANTES que las keys (Java inicializa los static en orden).
    private static final Map<ResourceKey<LootTable>, Target> TARGETS_INTERNAL = new LinkedHashMap<>();
    public static final Map<ResourceKey<LootTable>, Target> TARGETS = Collections.unmodifiableMap(TARGETS_INTERNAL);

    // =========================================================================
    // =========================  1. COFRES (y barriles)  ======================
    // =========================================================================

    // --- Templo del desierto ---------------------------------------------------
    public static final ResourceKey<LootTable> DESERT_PYRAMID_RUBY =
            chest("desert_pyramid", "desert_pyramid/ruby");
    public static final ResourceKey<LootTable> DESERT_PYRAMID_HORSE_ARMOR =
            chest("desert_pyramid", "desert_pyramid/horse_armor");

    // --- Templo de la jungla ---------------------------------------------------
    public static final ResourceKey<LootTable> JUNGLE_TEMPLE_RUBY =
            chest("jungle_temple", "jungle_temple/ruby");
    public static final ResourceKey<LootTable> JUNGLE_TEMPLE_HORSE_ARMOR =
            chest("jungle_temple", "jungle_temple/horse_armor");

    // --- Mazmorra ----------------------------------------------------------------
    public static final ResourceKey<LootTable> MONSTER_ROOM_HORSE_ARMOR =
            chest("simple_dungeon", "monster_room/horse_armor");

    // --- Mina abandonada (incluye las de cueva de azufre: misma tabla vanilla) ---
    public static final ResourceKey<LootTable> MINESHAFT_RUBY =
            chest("abandoned_mineshaft", "mineshaft/ruby");

    // --- Campamento abandonado (26.3) — cofre secreto ---------------------------
    public static final ResourceKey<LootTable> ABANDONED_CAMP_SECRET_RUBY =
            chest("abandoned_camp_secret_chest", "abandoned_camp/secret_ruby");

    // --- Iglú (solo esmeralda en vanilla) ------------------------------------------
    public static final ResourceKey<LootTable> IGLOO_RUBY =
            chest("igloo_chest", "igloo/ruby");

    // --- Tesoro enterrado --------------------------------------------------------
    public static final ResourceKey<LootTable> BURIED_TREASURE_RUBY =
            chest("buried_treasure", "buried_treasure/ruby");
    public static final ResourceKey<LootTable> BURIED_TREASURE_NAUTILUS_ARMOR =
            chest("buried_treasure", "buried_treasure/nautilus_armor");

    // --- Naufragio -----------------------------------------------------------------
    public static final ResourceKey<LootTable> SHIPWRECK_SUPPLY_NAUTILUS_ARMOR =
            chest("shipwreck_supply", "shipwreck/supply_nautilus_armor");
    public static final ResourceKey<LootTable> SHIPWRECK_TREASURE_RUBY =
            chest("shipwreck_treasure", "shipwreck/treasure_ruby");
    public static final ResourceKey<LootTable> SHIPWRECK_TREASURE_NAUTILUS_ARMOR =
            chest("shipwreck_treasure", "shipwreck/treasure_nautilus_armor");
    public static final ResourceKey<LootTable> SHIPWRECK_MAP_NAUTILUS_ARMOR =
            chest("shipwreck_map", "shipwreck/map_nautilus_armor");

    // --- Ruinas oceánicas (rubí = esmeralda; armadura = la de diamante) ------------
    public static final ResourceKey<LootTable> OCEAN_RUIN_SMALL_RUBY =
            chest("underwater_ruin_small", "ocean_ruin/small_ruby");
    public static final ResourceKey<LootTable> OCEAN_RUIN_SMALL_NAUTILUS_ARMOR =
            chest("underwater_ruin_small", "ocean_ruin/small_nautilus_armor");
    public static final ResourceKey<LootTable> OCEAN_RUIN_BIG_RUBY =
            chest("underwater_ruin_big", "ocean_ruin/big_ruby");
    public static final ResourceKey<LootTable> OCEAN_RUIN_BIG_NAUTILUS_ARMOR =
            chest("underwater_ruin_big", "ocean_ruin/big_nautilus_armor");

    // --- Stronghold (el cofre del "altar" es stronghold_corridor en vanilla) --------
    public static final ResourceKey<LootTable> STRONGHOLD_ALTAR_RUBY =
            chest("stronghold_corridor", "stronghold/altar_ruby");
    public static final ResourceKey<LootTable> STRONGHOLD_ALTAR_HORSE_ARMOR =
            chest("stronghold_corridor", "stronghold/altar_horse_armor");

    // --- Ciudad antigua -------------------------------------------------------------
    public static final ResourceKey<LootTable> ANCIENT_CITY_HOE =
            chest("ancient_city", "ancient_city/hoe_damaged_enchanted");
    public static final ResourceKey<LootTable> ANCIENT_CITY_LEGGINGS =
            chest("ancient_city", "ancient_city/leggings_enchanted");
    public static final ResourceKey<LootTable> ANCIENT_CITY_HORSE_ARMOR =
            chest("ancient_city", "ancient_city/horse_armor");

    // --- Fortaleza del Nether ---------------------------------------------------------
    public static final ResourceKey<LootTable> NETHER_FORTRESS_RUBY =
            chest("nether_bridge", "nether_fortress/ruby");
    public static final ResourceKey<LootTable> NETHER_FORTRESS_HORSE_ARMOR =
            chest("nether_bridge", "nether_fortress/horse_armor");

    // --- Bastión: tesoro -----------------------------------------------------------------
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

    // --- Bastión: cofre genérico y establo de hoglins ----------------------------------
    public static final ResourceKey<LootTable> BASTION_OTHER_PICKAXE =
            chest("bastion_other", "bastion/other_pickaxe_enchanted");
    public static final ResourceKey<LootTable> BASTION_OTHER_SHOVEL =
            chest("bastion_other", "bastion/other_shovel");
    public static final ResourceKey<LootTable> BASTION_HOGLIN_STABLE_PICKAXE =
            chest("bastion_hoglin_stable", "bastion/hoglin_stable_pickaxe_damaged_enchanted");
    public static final ResourceKey<LootTable> BASTION_HOGLIN_STABLE_SHOVEL =
            chest("bastion_hoglin_stable", "bastion/hoglin_stable_shovel_damaged_enchanted");

    // --- End City --------------------------------------------------------------------------
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

    // --- Trial Chambers: cofre y barril de intersección ------------------------------------
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

    // --- Mansión del bosque (VALORES PROPIOS, sin cambios) ----------------------------------
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

    // --- Puesto de saqueadores (VALORES PROPIOS, sin cambios) ---------------------------------
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

    // =========================================================================
    // =========================  2. VAULTS (Trial Chambers)  ==================
    // =========================================================================
    // "chests/trial_chambers/reward" es la tabla del vault normal (y de los cofres de recompensa)
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

    // =========================================================================
    // =========================  3. JARRONES  =================================
    // =========================================================================
    // Tu loot de jarrones, SIN CAMBIOS (el jarrón tiene 1 solo hueco).
    public static final ResourceKey<LootTable> TRIAL_CORRIDOR_POT_RUBY =
            pot("trial_chambers/corridor", "trial_chambers/corridor_ruby");

    // (4. Arqueología y 5. Equipamiento viven en ModGlobalLootModifierProvider:
    //  no usan tablas extra, usan modificadores propios.)


    // =========================================================================
    // KEYS: helpers
    // =========================================================================

    /** Engancha a "minecraft:chests/<vanillaChest>". */
    private static ResourceKey<LootTable> chest(String vanillaChest, String path) {
        return register("chests/" + vanillaChest, "chests/" + path);
    }

    /** Vaults: su tabla vanilla también vive en "chests/...", pero va en su sección. */
    private static ResourceKey<LootTable> vault(String vanillaVault, String path) {
        return register("chests/" + vanillaVault, "vaults/" + path);
    }

    /** Engancha a "minecraft:pots/<vanillaPot>". */
    private static ResourceKey<LootTable> pot(String vanillaPot, String path) {
        return register("pots/" + vanillaPot, "pots/" + path);
    }

    private static ResourceKey<LootTable> register(String vanillaTable, String path) {
        // La tabla se genera en data/travelyourearth/loot_table/extra/<path>.json
        ResourceKey<LootTable> key = ResourceKey.create(Registries.LOOT_TABLE,
                Identifier.fromNamespaceAndPath(TravelYourEarth.MODID, "extra/" + path));
        // El modificador se llama igual: data/travelyourearth/loot_modifiers/<path>.json
        if (TARGETS_INTERNAL.put(key, new Target(path, vanillaTable)) != null) {
            throw new IllegalStateException("Key de loot duplicada: " + path);
        }
        return key;
    }


    // =========================================================================
    // GENERACIÓN
    // =========================================================================

    // 26.3: LootTableSubProvider recibe un Context y sobreescribe run().
    private final LootTableSubProvider.Context output;
    private HolderGetter<Enchantment> enchantments; // se llena al inicio de run()
    private final Set<ResourceKey<LootTable>> generated = new HashSet<>();

    public ModExtraLootProvider(LootTableSubProvider.Context output) {
        this.output = output;
    }

    @Override
    public void run() {
        this.enchantments = this.output.lookup(Registries.ENCHANTMENT);

        // Atajos para que las líneas no sean kilométricas
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

        // Lectura rápida de cada línea:
        //   add(KEY, probabilidad_por_cofre, ítem, [count / damage / enchant...])
        //   damage(min, max) = durabilidad RESTANTE (1.0 = nueva)

        // =====================================================================
        // 1. COFRES
        // =====================================================================

        // --- Templo del desierto ---
        add(DESERT_PYRAMID_RUBY,           0.1023f, ruby, count(1, 4));  // 10.2%
        add(DESERT_PYRAMID_HORSE_ARMOR,    0.0894f, horseArmor);  // 8.9%

        // --- Templo de la jungla ---
        add(JUNGLE_TEMPLE_RUBY,            0.091f, ruby, count(1, 4));  // 98.1%  ⚠️ ¿seguro? esto es 98.1%, quizás querías 0.0981f
        add(JUNGLE_TEMPLE_HORSE_ARMOR,     0.0731f, horseArmor);  // 7.3%

        // --- Mazmorra ---
        add(MONSTER_ROOM_HORSE_ARMOR,      0.0679f, horseArmor);  // 6.8%

        // --- Mina abandonada (normal y de cueva de azufre) ---
        add(MINESHAFT_RUBY,                0.1021f, ruby, count(1, 2));  // 10.2%

        // --- Campamento abandonado: cofre secreto ---
        add(ABANDONED_CAMP_SECRET_RUBY,    0.5313f, ruby, count(1, 3));   // 54.2%

        // --- Iglú (base: esmeralda) ---
        add(IGLOO_RUBY,                    0.0213f, ruby);  // 2.1%

        // --- Tesoro enterrado ---
        add(BURIED_TREASURE_RUBY,            0.6123f, ruby, count(2, 5));  // 61.2%
        add(BURIED_TREASURE_NAUTILUS_ARMOR,  0.0116f, nautilusArmor);  // 1.2% (antes 1.1%)

        // --- Naufragio ---
        add(SHIPWRECK_SUPPLY_NAUTILUS_ARMOR,    0.0108f, nautilusArmor);  // 1.1%
        add(SHIPWRECK_TREASURE_RUBY,            0.3421f, ruby);  // 34.2%
        add(SHIPWRECK_TREASURE_NAUTILUS_ARMOR,  0.0168f, nautilusArmor);  // 1.7% (antes 1.1%)
        add(SHIPWRECK_MAP_NAUTILUS_ARMOR,       0.0108f, nautilusArmor);  // 1.1%

        // --- Ruinas oceánicas (base: esmeralda) ---
        add(OCEAN_RUIN_SMALL_RUBY,            0.052f, ruby);  // 5.2%
        add(OCEAN_RUIN_SMALL_NAUTILUS_ARMOR,  0.0063f, nautilusArmor);  // 0.6% (antes 1.1%)
        add(OCEAN_RUIN_BIG_RUBY,              0.052f, ruby);  // 5.2%
        add(OCEAN_RUIN_BIG_NAUTILUS_ARMOR,    0.0066f, nautilusArmor);  // 0.7% (antes 1.1%)

        // --- Stronghold (altar) ---
        add(STRONGHOLD_ALTAR_RUBY,         0.0912f, ruby, count(2, 3));  // 9.1%
        add(STRONGHOLD_ALTAR_HORSE_ARMOR,  0.0276f, horseArmor);  // 2.8% (antes 2.5%)

        // --- Ciudad antigua (encantamiento nivel 30-50) ---
        add(ANCIENT_CITY_HOE,          0.1646f, hoe, damage(0.8f, 1.0f), enchantLevels(30, 50));  // 16.5%
        add(ANCIENT_CITY_LEGGINGS,     0.1646f, leggings, enchantLevels(30, 50));  // 16.5%
        add(ANCIENT_CITY_HORSE_ARMOR,  0.1646f, horseArmor);  // 16.5%

        // --- Fortaleza del Nether ---
        add(NETHER_FORTRESS_RUBY,         0.181f, ruby, count(1, 3));  // 18.1%
        add(NETHER_FORTRESS_HORSE_ARMOR,  0.1105f, horseArmor);  // 11.1% (tu rubí casi no cambió)

        // --- Bastión: tesoro (encantamiento aleatorio de cualquier nivel) ---
        add(BASTION_TREASURE_RUBY,                         0.142f, ruby, count(3, 8));  // 14.2%
        add(BASTION_TREASURE_SWORD,                        0.1604f, sword);  // 16% (antes 15.2%)
        add(BASTION_TREASURE_SWORD_DAMAGED_ENCHANTED,      0.1604f, sword, damage(0.8f, 1.0f), enchantRandomly());  // 16% (antes 15.2%)
        add(BASTION_TREASURE_SPEAR,                        0.1604f, spear);  // 16% (antes 15.2%)
        add(BASTION_TREASURE_SPEAR_DAMAGED_ENCHANTED,      0.1604f, spear, damage(0.8f, 1.0f), enchantRandomly());  // 16% (antes 15.2%)
        add(BASTION_TREASURE_HELMET,                       0.1348f, helmet);  // 13.5% (antes 12.8%)
        add(BASTION_TREASURE_HELMET_DAMAGED_ENCHANTED,     0.1604f, helmet, damage(0.8f, 1.0f), enchantRandomly());  // 16% (antes 15.2%)
        add(BASTION_TREASURE_CHESTPLATE,                   0.1348f, chestplate);  // 13.5% (antes 12.8%)
        add(BASTION_TREASURE_CHESTPLATE_DAMAGED_ENCHANTED,  0.1604f, chestplate, damage(0.8f, 1.0f), enchantRandomly());  // 16% (antes 15.2%)
        add(BASTION_TREASURE_LEGGINGS,                     0.1348f, leggings);  // 13.5% (antes 12.8%)
        add(BASTION_TREASURE_LEGGINGS_DAMAGED_ENCHANTED,   0.1604f, leggings, damage(0.8f, 1.0f), enchantRandomly());  // 16% (antes 15.2%)
        add(BASTION_TREASURE_BOOTS,                        0.1348f, boots);  // 13.5% (antes 12.8%)
        add(BASTION_TREASURE_BOOTS_DAMAGED_ENCHANTED,      0.1604f, boots, damage(0.8f, 1.0f), enchantRandomly());  // 16% (antes 15.2%)

        // --- Bastión: genérico y establo de hoglins ---
        add(BASTION_OTHER_PICKAXE,          0.0674f, pickaxe, enchantRandomly());  // 6.7%
        add(BASTION_OTHER_SHOVEL,           0.0674f, shovel);  // 6.7% (vanilla: sin encantar)
        add(BASTION_HOGLIN_STABLE_PICKAXE,  0.12f, pickaxe, damage(0.15f, 0.95f), enchantRandomly());  // 12%
        add(BASTION_HOGLIN_STABLE_SHOVEL,   0.15f, shovel, damage(0.15f, 0.8f), enchantRandomly());  // 15%

        // --- End City (encantamiento nivel 20-39) ---
        add(END_CITY_RUBY,         0.5812f, ruby, count(2, 8));  // 58.1%
        add(END_CITY_SWORD,        0.2146f, sword, enchantLevels(20, 39));  // 21.5% (antes 12.7%)
        add(END_CITY_SPEAR,        0.2146f, spear, enchantLevels(20, 39));  // 21.5% (antes 12.7%)
        add(END_CITY_PICKAXE,      0.2146f, pickaxe, enchantLevels(20, 39));  // 21.5% (antes 12.7%)
        add(END_CITY_SHOVEL,       0.2146f, shovel, enchantLevels(20, 39));  // 21.5% (antes 12.7%)
        add(END_CITY_HELMET,       0.2146f, helmet, enchantLevels(20, 39));  // 21.5% (antes 12.7%)
        add(END_CITY_CHESTPLATE,   0.2146f, chestplate, enchantLevels(20, 39));  // 21.5% (antes 12.7%)
        add(END_CITY_LEGGINGS,     0.2146f, leggings, enchantLevels(20, 39));  // 21.5% (antes 12.7%)
        add(END_CITY_BOOTS,        0.2146f, boots, enchantLevels(20, 39));  // 21.5% (antes 12.7%)
        add(END_CITY_HORSE_ARMOR,  0.0745f, horseArmor);  // 7.4% (antes 4.4%)

        // --- Trial Chambers: cofre de intersección ---
        add(TRIAL_INTERSECTION_RUBY,        0.3481f, ruby, count(1, 3));  // 34.8%
        add(TRIAL_INTERSECTION_RUBY_BLOCK,  0.023f, rubyBlock);  // 2.3%
        add(TRIAL_INTERSECTION_PICKAXE,     0.1422f, pickaxe, damage(0.1f, 0.5f));  // 14.2% (antes 11.2%)
        add(TRIAL_INTERSECTION_AXE,         0.1422f, axe, damage(0.1f, 0.5f));  // 14.2% (antes 11.2%)

        // --- Trial Chambers: barril de intersección ---
        add(TRIAL_BARREL_RUBY,     0.0624f, ruby, count(1, 2));  // 6.2%
        add(TRIAL_BARREL_PICKAXE,  0.0609f, pickaxe, damage(0.15f, 0.8f));  // 6.1% (antes 5.9%)
        add(TRIAL_BARREL_AXE,      0.0609f, axe, damage(0.4f, 0.9f), enchantRandomly());  // 6.1% (antes 5.9%)

        // --- Mansión del bosque (VALORES PROPIOS, sin cambios; encantamiento 15-30) ---
        add(MANSION_RUBY,                          0.32f, ruby, count(2, 4));
        add(MANSION_SWORD,                         0.034f, sword);
        add(MANSION_SWORD_DAMAGED_ENCHANTED,       0.012f, sword, damage(0.8f, 1.0f), enchantLevels(15, 30));
        add(MANSION_SPEAR,                         0.034f, spear);
        add(MANSION_SPEAR_DAMAGED_ENCHANTED,       0.012f, spear, damage(0.8f, 1.0f), enchantLevels(15, 30));
        add(MANSION_PICKAXE,                       0.034f, pickaxe, enchantLevels(15, 30));
        add(MANSION_AXE,                           0.034f, axe, enchantLevels(15, 30));
        add(MANSION_SHOVEL,                        0.034f, shovel, enchantLevels(15, 30));
        add(MANSION_HOE,                           0.034f, hoe, enchantLevels(15, 30));
        add(MANSION_HELMET,                        0.032f, helmet);
        add(MANSION_HELMET_DAMAGED_ENCHANTED,      0.021f, helmet, damage(0.8f, 1.0f), enchantLevels(15, 30));
        add(MANSION_CHESTPLATE,                    0.032f, chestplate);
        add(MANSION_CHESTPLATE_DAMAGED_ENCHANTED,  0.021f, chestplate, damage(0.8f, 1.0f), enchantLevels(15, 30));
        add(MANSION_LEGGINGS,                      0.032f, leggings);
        add(MANSION_LEGGINGS_DAMAGED_ENCHANTED,    0.021f, leggings, damage(0.8f, 1.0f), enchantLevels(15, 30));
        add(MANSION_BOOTS,                         0.032f, boots);
        add(MANSION_BOOTS_DAMAGED_ENCHANTED,       0.021f, boots, damage(0.8f, 1.0f), enchantLevels(15, 30));
        add(MANSION_HORSE_ARMOR,                   0.012f, horseArmor);

        // --- Puesto de saqueadores (VALORES PROPIOS, sin cambios) ---
        add(OUTPOST_RUBY,         0.45f, ruby, count(1, 3));
        add(OUTPOST_SWORD,        0.15f, sword, damage(0.3f, 0.9f));
        add(OUTPOST_SPEAR,        0.15f, spear, damage(0.3f, 0.9f));
        add(OUTPOST_PICKAXE,      0.176f, pickaxe, damage(0.3f, 0.9f));
        add(OUTPOST_AXE,          0.172f, axe, damage(0.3f, 0.9f));
        add(OUTPOST_CHESTPLATE,   0.092f, chestplate, damage(0.3f, 0.9f));
        add(OUTPOST_BOOTS,        0.092f, boots, damage(0.3f, 0.9f));
        add(OUTPOST_HORSE_ARMOR,  0.10f, horseArmor);

        // =====================================================================
        // 2. VAULTS
        // =====================================================================

        // --- Vault normal (encantamiento nivel 5-15) ---
        add(VAULT_RUBY,        0.1023f, ruby, count(1, 2));  // 10.2%
        // (vanilla aquí da hacha y peto de diamante; en el mod: lanza y grebas con los mismos valores)
        add(VAULT_SPEAR,     0.0381f, spear, enchantLevels(5, 15));  // 3.8% (antes 3.5%)
        add(VAULT_LEGGINGS,  0.0381f, leggings, enchantLevels(5, 15));  // 3.8% (antes 3.5%)

        // --- Vault ominoso (encantamiento nivel 10-20) ---
        add(OMINOUS_VAULT_RUBY,        0.2639f, ruby, count(2, 3));  // 26.4%
        add(OMINOUS_VAULT_RUBY_BLOCK,  0.028f, rubyBlock);  // 2.8%
        add(OMINOUS_VAULT_SPEAR,       0.0828f, spear, enchantLevels(10, 20));  // 8.3% (tu rubí no cambió)
        add(OMINOUS_VAULT_LEGGINGS,    0.0828f, leggings, enchantLevels(10, 20));  // 8.3% (tu rubí no cambió)

        // =====================================================================
        // 3. JARRONES
        // =====================================================================
        add(TRIAL_CORRIDOR_POT_RUBY,  0.122f, ruby, count(2, 3));

        // Seguridad: si declaraste una key arriba y te olvidaste de su tabla, datagen avisa.
        for (ResourceKey<LootTable> key : TARGETS.keySet()) {
            if (!this.generated.contains(key)) {
                throw new IllegalStateException("Key sin tabla en ModExtraLootProvider: " + TARGETS.get(key).modifierName());
            }
        }
    }


    // =========================================================================
    // HELPERS DE TABLAS
    // =========================================================================

    /**
     * 1 pool, 1 tirada, con "chance" de dar el ítem, aplicándole las funciones en orden.
     * Ej: add(KEY, 0.15f, sword, damage(0.8f, 1.0f), enchantRandomly());
     */
    private void add(ResourceKey<LootTable> key, float chance, ItemLike item, LootItemFunction.Builder... functions) {
        if (!TARGETS.containsKey(key)) {
            throw new IllegalStateException("Tabla sin key registrada: " + key);
        }
        if (!this.generated.add(key)) {
            throw new IllegalStateException("Tabla generada dos veces: " + TARGETS.get(key).modifierName());
        }
        // "var": así no dependemos del nombre exacto del tipo que devuelve lootTableItem en 26.3
        var entry = LootItem.lootTableItem(item.asItem());
        for (LootItemFunction.Builder function : functions) {
            entry.apply(function);
        }
        this.output.accept(key, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(chance))
                        .add(entry)));
    }

    /** Cantidad aleatoria entre min y max (ambos incluidos). */
    private static LootItemFunction.Builder count(int min, int max) {
        return SetItemCountFunction.setCount(ContextIntProviders.between(min, max));
    }

    /** Durabilidad RESTANTE entre min y max (1.0 = nueva, 0.1 = casi rota). */
    private static LootItemFunction.Builder damage(float min, float max) {
        return SetItemDamageFunction.setDamage(ContextFloatProviders.between(min, max));
    }

    /** Como una mesa de encantamientos de nivel min-max (vanilla: End City 20-39, Ciudad antigua 30-50). */
    private LootItemFunction.Builder enchantLevels(int min, int max) {
        return EnchantWithLevelsFunction.enchantWithLevels(this.enchantments,
                ContextIntProviders.between(min, max));
    }

    /** Un encantamiento aleatorio de cualquier nivel (vanilla: bastiones). */
    private LootItemFunction.Builder enchantRandomly() {
        return EnchantRandomlyFunction.randomEnchantment()
                .withOneOf(this.enchantments.getOrThrow(EnchantmentTags.ON_RANDOM_LOOT));
    }
}