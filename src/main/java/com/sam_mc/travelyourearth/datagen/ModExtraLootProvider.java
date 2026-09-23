package com.sam_mc.travelyourearth.datagen;

import com.sam_mc.travelyourearth.TravelYourEarth;
import com.sam_mc.travelyourearth.block.ModBlocks;
import com.sam_mc.travelyourearth.item.ModItems;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.EnchantWithLevelsFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemDamageFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProviders;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

public class ModExtraLootProvider implements LootTableSubProvider {

    // =========================================================================
    // RESOURCE KEYS
    // =========================================================================

    // Desert
    public static final ResourceKey<LootTable> RUBY_DESERT_PYRAMID =
            createKey("extra/glm/ruby_desert_pyramid");
    public static final ResourceKey<LootTable> RUBY_HORSE_ARMOR_DESERT_PYRAMID =
            createKey("extra/glm/ruby_horse_armor_desert_pyramid");
    public static final ResourceKey<LootTable> RUBY_DESERT_PYRAMID_ARCHAEOLOGY =
            createKey("extra/glm/ruby_desert_pyramid_archaeology");

    // Jungle Temple
    public static final ResourceKey<LootTable> RUBY_JUNGLE_TEMPLE =
            createKey("extra/glm/ruby_jungle_temple");
    public static final ResourceKey<LootTable> RUBY_HORSE_ARMOR_JUNGLE_TEMPLE =
            createKey("extra/glm/ruby_horse_armor_jungle_temple");

    // Monster Room
    public static final ResourceKey<LootTable> RUBY_HORSE_ARMOR_MONSTER_ROOM =
            createKey("extra/glm/ruby_horse_armor_monster_room");

    // Shipwreck
    public static final ResourceKey<LootTable> NAUTILUS_ARMOR_RUBY_SHIPWRECK_SUPPLY =
            createKey("extra/glm/ruby_nautilus_armor_shipwreck_supply");
    public static final ResourceKey<LootTable> RUBY_SHIPWRECK_TREASURE =
            createKey("extra/glm/ruby_shipwreck_treasure");
    public static final ResourceKey<LootTable> NAUTILUS_ARMOR_RUBY_SHIPWRECK_TREASURE =
            createKey("extra/glm/ruby_nautilus_armor_shipwreck_treasure");
    public static final ResourceKey<LootTable> NAUTILUS_ARMOR_RUBY_SHIPWRECK_MAP =
            createKey("extra/glm/ruby_nautilus_armor_shipwreck_map");

    // Mineshaft & Buried Treasure
    public static final ResourceKey<LootTable> RUBY_MINESHAFT =
            createKey("extra/glm/ruby_mineshaft");
    public static final ResourceKey<LootTable> RUBY_BURIED_TREASURE =
            createKey("extra/glm/ruby_buried_treasure");
    public static final ResourceKey<LootTable> NAUTILUS_ARMOR_RUBY_BURIED_TREASURE =
            createKey("extra/glm/ruby_nautilus_armor_buried_treasure");

    // End City
    public static final ResourceKey<LootTable> RUBY_END_CITY =
            createKey("extra/glm/ruby_end_city");
    public static final ResourceKey<LootTable> RUBY_HORSE_ARMOR_END_CITY =
            createKey("extra/glm/ruby_horse_armor_end_city");
    public static final ResourceKey<LootTable> RUBY_ENCHANTED_SWORD_END_CITY =
            createKey("extra/glm/ruby_sword_enchanted_end_city");
    public static final ResourceKey<LootTable> RUBY_ENCHANTED_PICKAXE_END_CITY =
            createKey("extra/glm/ruby_enchanted_pickaxe_end_city");
    public static final ResourceKey<LootTable> RUBY_ENCHANTED_SHOVEL_END_CITY =
            createKey("extra/glm/ruby_enchanted_shovel_end_city");
    public static final ResourceKey<LootTable> RUBY_ENCHANTED_HELMET_END_CITY =
            createKey("extra/glm/ruby_enchanted_helmet_end_city");
    public static final ResourceKey<LootTable> RUBY_ENCHANTED_CHESTPLATE_END_CITY =
            createKey("extra/glm/ruby_enchanted_chestplate_end_city");
    public static final ResourceKey<LootTable> RUBY_ENCHANTED_LEGGINGS_END_CITY =
            createKey("extra/glm/ruby_enchanted_leggings_end_city");
    public static final ResourceKey<LootTable> RUBY_ENCHANTED_BOOTS_END_CITY =
            createKey("extra/glm/ruby_enchanted_boots_end_city");


    // Nether Bridge
    public static final ResourceKey<LootTable> RUBY_NETHER_BRIDGE =
            createKey("extra/glm/ruby_nether_bridge");
    public static final ResourceKey<LootTable> RUBY_HORSE_ARMOR_NETHER_BRIDGE =
            createKey("extra/glm/ruby_horse_armor_nether_bridge");

    // Stronghold
    public static final ResourceKey<LootTable> RUBY_STRONGHOLD_CORRIDOR =
            createKey("extra/glm/ruby_stronghold_corridor");
    public static final ResourceKey<LootTable> RUBY_STRONGHOLD_CROSSING =
            createKey("extra/glm/ruby_stronghold_crossing");
    public static final ResourceKey<LootTable> RUBY_HORSE_ARMOR_STRONGHOLD_CORRIDOR =
            createKey("extra/glm/ruby_horse_armor_stronghold_corridor");

    // Trial Chambers
    public static final ResourceKey<LootTable> RUBY_TRIAL_CHAMBERS_INTERSECTION =
            createKey("extra/glm/ruby_trial_chambers_intersection");
    public static final ResourceKey<LootTable> RUBY_BLOCK_TRIAL_CHAMBERS_INTERSECTION =
            createKey("extra/glm/ruby_block_from_trial_chambers_intersection");
    public static final ResourceKey<LootTable> RUBY_DAMAGED_PICKAXE_TRIAL_CHAMBERS_INTERSECTION =
            createKey("ruby_damaged_pickaxe_from_trial_chambers_intersection");
    public static final ResourceKey<LootTable> RUBY_DAMAGED_AXE_TRIAL_CHAMBERS_INTERSECTION =
            createKey("ruby_damaged_axe_from_trial_chambers_intersection");
    public static final ResourceKey<LootTable> RUBY_TRIAL_CHAMBERS_REWARD =
            createKey("extra/glm/ruby_trial_chambers_reward");
    public static final ResourceKey<LootTable> RUBY_ENCHANTED_SPEAR_TRIAL_CHAMBERS_REWARD =
            createKey("extra/glm/ruby_enchanted_spear_trial_chambers_reward");
    public static final ResourceKey<LootTable> RUBY_ENCHANTED_LEGGINGS_TRIAL_CHAMBERS_REWARD =
            createKey("extra/glm/ruby_enchanted_leggings_trial_chambers_reward");
    public static final ResourceKey<LootTable> RUBY_TRIAL_CHAMBERS_REWARD_OMINOUS =
            createKey("extra/glm/ruby_trial_chambers_reward_ominous");
    public static final ResourceKey<LootTable> RUBY_BLOCK_TRIAL_CHAMBERS_REWARD_OMINOUS =
            createKey("extra/glm/ruby_block_trial_chambers_reward_ominous");
    public static final ResourceKey<LootTable> RUBY_ENCHANTED_SPEAR_TRIAL_CHAMBERS_REWARD_OMINOUS =
            createKey("extra/glm/ruby_enchanted_spear_trial_chambers_reward_ominous");
    public static final ResourceKey<LootTable> RUBY_ENCHANTED_LEGGINGS_TRIAL_CHAMBERS_REWARD_OMINOUS =
            createKey("extra/glm/ruby_enchanted_leggings_trial_chambers_reward_ominous");
    public static final ResourceKey<LootTable> RUBY_TRIAL_CHAMBERS_CORRIDOR_POT =
            createKey("extra/glm/ruby_trial_chambers_corridor_pot");

    // Bastion
    public static final ResourceKey<LootTable> RUBY_BASTION_TREASURE =
            createKey("extra/glm/ruby_bastion_treasure");
    public static final ResourceKey<LootTable> RUBY_SWORD_BASTION_TREASURE =
            createKey("extra/glm/ruby_sword_bastion_treasure");
    public static final ResourceKey<LootTable> RUBY_SWORD_DAMAGED_ENCHANTED_BASTION_TREASURE =
            createKey("extra/glm/ruby_sword_damaged_enchanted_bastion_treasure");
    public static final ResourceKey<LootTable> RUBY_ENCHANTED_PICKAXE_BASTION_OTHER =
            createKey("extra/glm/ruby_enchanted_pickaxe_bastion_other");
    public static final ResourceKey<LootTable> RUBY_ENCHANTED_PICKAXE_BASTION_HOGLIN_STABLE =
            createKey("extra/glm/ruby_enchanted_pickaxe_bastion_hoglin_stable");
    public static final ResourceKey<LootTable> RUBY_ENCHANTED_SHOVEL_BASTION_OTHER =
            createKey("extra/glm/ruby_enchanted_shovel_bastion_other");
    public static final ResourceKey<LootTable> RUBY_ENCHANTED_SHOVEL_BASTION_HOGLIN_STABLE =
            createKey("extra/glm/ruby_enchanted_shovel_bastion_hoglin_stable");
    public static final ResourceKey<LootTable> RUBY_SPEAR_BASTION_TREASURE =
            createKey("extra/glm/ruby_spear_bastion_treasure");
    public static final ResourceKey<LootTable> RUBY_SPEAR_DAMAGED_ENCHANTED_BASTION_TREASURE =
            createKey("extra/glm/ruby_spear_damaged_enchanted_bastion_treasure");
    public static final ResourceKey<LootTable> RUBY_HELMET_BASTION_TREASURE =
            createKey("extra/glm/ruby_helmet_bastion_treasure");
    public static final ResourceKey<LootTable> RUBY_HELMET_DAMAGED_ENCHANTED_BASTION_TREASURE =
            createKey("extra/glm/ruby_helmet_damaged_enchanted_bastion_treasure");
    public static final ResourceKey<LootTable> RUBY_CHESTPLATE_BASTION_TREASURE =
            createKey("extra/glm/ruby_chestplate_bastion_treasure");
    public static final ResourceKey<LootTable> RUBY_CHESTPLATE_DAMAGED_ENCHANTED_BASTION_TREASURE =
            createKey("extra/glm/ruby_chestplate_damaged_enchanted_bastion_treasure");
    public static final ResourceKey<LootTable> RUBY_LEGGINGS_BASTION_TREASURE =
            createKey("extra/glm/ruby_leggings_bastion_treasure");
    public static final ResourceKey<LootTable> RUBY_LEGGINGS_DAMAGED_ENCHANTED_BASTION_TREASURE =
            createKey("extra/glm/ruby_leggings_damaged_enchanted_bastion_treasure");
    public static final ResourceKey<LootTable> RUBY_BOOTS_BASTION_TREASURE =
            createKey("extra/glm/ruby_boots_bastion_treasure");
    public static final ResourceKey<LootTable> RUBY_BOOTS_DAMAGED_ENCHANTED_BASTION_TREASURE =
            createKey("extra/glm/ruby_boots_damaged_enchanted_bastion_treasure");

    // Woodland Mansion
    public static final ResourceKey<LootTable> RUBY_WOODLAND_MANSION =
            createKey("extra/glm/ruby_woodland_mansion");
    public static final ResourceKey<LootTable> RUBY_SWORD_WOODLAND_MANSION =
            createKey("extra/glm/ruby_sword_woodland_mansion");
    public static final ResourceKey<LootTable> RUBY_SWORD_DAMAGED_ENCHANTED_WOODLAND_MANSION =
            createKey("extra/glm/ruby_sword_damaged_enchanted_woodland_mansion");
    public static final ResourceKey<LootTable> RUBY_SPEAR_WOODLAND_MANSION =
            createKey("extra/glm/ruby_spear_woodland_mansion");
    public static final ResourceKey<LootTable> RUBY_SPEAR_DAMAGED_ENCHANTED_WOODLAND_MANSION =
            createKey("extra/glm/ruby_spear_damaged_enchanted_woodland_mansion");
    public static final ResourceKey<LootTable> RUBY_ENCHANTED_PICKAXE_WOODLAND_MANSION =
            createKey("extra/glm/ruby_enchanted_pickaxe_woodland_mansion");
    public static final ResourceKey<LootTable> RUBY_ENCHANTED_AXE_WOODLAND_MANSION =
            createKey("extra/glm/ruby_axe_enchanted_woodland_mansion");
    public static final ResourceKey<LootTable> RUBY_SHOVEL_ENCHANTED_WOODLAND_MANSION =
            createKey("extra/glm/ruby_shovel_enchanted_woodland_mansion");
    public static final ResourceKey<LootTable> RUBY_HOE_ENCHANTED_WOODLAND_MANSION =
            createKey("extra/glm/ruby_hoe_damaged_enchanted_woodland_mansion");
    public static final ResourceKey<LootTable> RUBY_HELMET_WOODLAND_MANSION =
            createKey("extra/glm/ruby_helmet_woodland_mansion");
    public static final ResourceKey<LootTable> RUBY_HELMET_DAMAGED_ENCHANTED_WOODLAND_MANSION =
            createKey("extra/glm/ruby_helmet_damaged_enchanted_woodland_mansion");
    public static final ResourceKey<LootTable> RUBY_CHESTPLATE_WOODLAND_MANSION =
            createKey("extra/glm/ruby_chestplate_woodland_mansion");
    public static final ResourceKey<LootTable> RUBY_CHESTPLATE_DAMAGED_ENCHANTED_WOODLAND_MANSION =
            createKey("extra/glm/ruby_chestplate_damaged_enchanted_woodland_mansion");
    public static final ResourceKey<LootTable> RUBY_LEGGINGS_WOODLAND_MANSION =
            createKey("extra/glm/ruby_leggings_woodland_mansion");
    public static final ResourceKey<LootTable> RUBY_LEGGINGS_DAMAGED_ENCHANTED_WOODLAND_MANSION =
            createKey("extra/glm/ruby_leggings_damaged_enchanted_woodland_mansion");
    public static final ResourceKey<LootTable> RUBY_BOOTS_WOODLAND_MANSION =
            createKey("extra/glm/ruby_boots_woodland_mansion");
    public static final ResourceKey<LootTable> RUBY_BOOTS_DAMAGED_ENCHANTED_WOODLAND_MANSION =
            createKey("extra/glm/ruby_boots_damaged_enchanted_woodland_mansion");
    public static final ResourceKey<LootTable> RUBY_HORSE_ARMOR_WOODLAND_MANSION =
            createKey("extra/glm/ruby_horse_armor_woodland_mansion");


    // Pillager Outpost
    public static final ResourceKey<LootTable> RUBY_PILLAGER_OUTPOST =
            createKey("extra/glm/ruby_pillager_outpost");
    public static final ResourceKey<LootTable> RUBY_SWORD_PILLAGER_OUTPOST =
            createKey("extra/glm/ruby_sword_pillager_outpost");
    public static final ResourceKey<LootTable> RUBY_SPEAR_PILLAGER_OUTPOST =
            createKey("extra/glm/ruby_spear_pillager_outpost");
    public static final ResourceKey<LootTable> RUBY_AXE_PILLAGER_OUTPOST =
            createKey("extra/glm/ruby_axe_pillager_outpost");
    public static final ResourceKey<LootTable> RUBY_PICKAXE_PILLAGER_OUTPOST =
            createKey("extra/glm/ruby_pickaxe_pillager_outpost");
    public static final ResourceKey<LootTable> RUBY_CHESTPLATE_PILLAGER_OUTPOST =
            createKey("extra/glm/ruby_chestplate_pillager_outpost");
    public static final ResourceKey<LootTable> RUBY_BOOTS_PILLAGER_OUTPOST =
            createKey("extra/glm/ruby_boots_pillager_outpost");
    public static final ResourceKey<LootTable> RUBY_HORSE_ARMOR_PILLAGER_OUTPOST =
            createKey("extra/glm/ruby_horse_armor_pillager_outpost");

    // Ancient City
    public static final ResourceKey<LootTable> RUBY_HOE_DAMAGED_ENCHANTED_ANCIENT_CITY =
            createKey("extra/glm/ruby_hoe_damaged_enchanted_ancient_city");
    public static final ResourceKey<LootTable> RUBY_LEGGINGS_ENCHANTED_ANCIENT_CITY =
            createKey("extra/glm/ruby_leggings_damaged_enchanted_ancient_city");
    public static final ResourceKey<LootTable> RUBY_HORSE_ARMOR_ANCIENT_CITY =
            createKey("extra/glm/ruby_horse_armor_ancient_city");

    //Ocean Ruins
    public static final ResourceKey<LootTable> NAUTILUS_ARMOR_RUBY_UNDERWATER_RUIN_SMALL =
            createKey("extra/glm/ruby_underwater_ruin_small");
    public static final ResourceKey<LootTable> NAUTILUS_ARMOR_RUBY_UNDERWATER_RUIN_BIG =
            createKey("extra/glm/ruby_underwater_ruin_big");


    // Helper method to keep definitions clean
    private static ResourceKey<LootTable> createKey(String path) {
        return ResourceKey.create(Registries.LOOT_TABLE,
                Identifier.fromNamespaceAndPath(TravelYourEarth.MODID, path));
    }

    // 26.3: LootTableSubProvider ya no recibe un HolderLookup.Provider en el
    // constructor ni implementa generate(BiConsumer<...>). Ahora recibe un
    // LootTableSubProvider.Context (que ya trae resuelto el random sequence y
    // el param set) y sobreescribe run() sin argumentos, llamando a
    // this.output.accept(...) para registrar cada tabla.
    private final LootTableSubProvider.Context output;

    public ModExtraLootProvider(LootTableSubProvider.Context output) {
        this.output = output;
    }

    @Override
    public void run() {

        HolderGetter<Enchantment> enchantments = this.output.lookup(Registries.ENCHANTMENT);

        // =====================================================================
        // DESERT PYRAMID & ARCHAEOLOGY
        // =====================================================================
        this.output.accept(RUBY_DESERT_PYRAMID, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.25f))
                        .add(LootItem.lootTableItem(ModItems.RUBY.get()))
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.between(1, 3)))));

        this.output.accept(RUBY_HORSE_ARMOR_DESERT_PYRAMID, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.044f))
                        .add(LootItem.lootTableItem(ModItems.RUBY_HORSE_ARMOR.get()))
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(1)))));

        this.output.accept(RUBY_DESERT_PYRAMID_ARCHAEOLOGY, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.580F)) // 20% de probabilidad al cepillar
                        .add(LootItem.lootTableItem(ModItems.RUBY.get())
                                .apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(1)))
                        )
                )
        );

        // =====================================================================
        // JUNGLE TEMPLE
        // =====================================================================
        this.output.accept(RUBY_JUNGLE_TEMPLE, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.123f))
                        .add(LootItem.lootTableItem(ModItems.RUBY.get()))
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.between(1, 4)))));

        this.output.accept(RUBY_HORSE_ARMOR_JUNGLE_TEMPLE, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.044f))
                        .add(LootItem.lootTableItem(ModItems.RUBY_HORSE_ARMOR.get()))
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(1)))));

        // =====================================================================
        // MONSTER ROOM
        // =====================================================================
        this.output.accept(RUBY_HORSE_ARMOR_MONSTER_ROOM, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.075f))
                        .add(LootItem.lootTableItem(ModItems.RUBY_HORSE_ARMOR.get()))
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(1)))));

        // =====================================================================
        // SHIPWRECK
        // =====================================================================
        this.output.accept(NAUTILUS_ARMOR_RUBY_SHIPWRECK_SUPPLY, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.05f))
                        .add(LootItem.lootTableItem(ModItems.RUBY_NAUTILUS_ARMOR.get()))
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(1)))));

        this.output.accept(RUBY_SHIPWRECK_TREASURE, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.15f))
                        .add(LootItem.lootTableItem(ModItems.RUBY.get()))
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.between(1, 2)))));

        this.output.accept(NAUTILUS_ARMOR_RUBY_SHIPWRECK_MAP, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.019f))
                        .add(LootItem.lootTableItem(ModItems.RUBY_NAUTILUS_ARMOR.get()))
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(1)))));

        this.output.accept(NAUTILUS_ARMOR_RUBY_SHIPWRECK_TREASURE, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.019f))
                        .add(LootItem.lootTableItem(ModItems.RUBY_NAUTILUS_ARMOR.get()))
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(1)))));

        // =====================================================================
        // MINESHAFT & BURIED TREASURE
        // =====================================================================
        this.output.accept(RUBY_MINESHAFT, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.10f))
                        .add(LootItem.lootTableItem(ModItems.RUBY.get()))
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.between(1, 2)))));

        this.output.accept(RUBY_BURIED_TREASURE, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.75f))
                        .add(LootItem.lootTableItem(ModItems.RUBY.get()))
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.between(2, 5)))));

        this.output.accept(NAUTILUS_ARMOR_RUBY_BURIED_TREASURE, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.019f))
                        .add(LootItem.lootTableItem(ModItems.RUBY_NAUTILUS_ARMOR.get()))
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(1)))));

        // =====================================================================
        // END CITY
        // =====================================================================
        this.output.accept(RUBY_END_CITY, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.35f))
                        .add(LootItem.lootTableItem(ModItems.RUBY.get()))
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.between(2, 9)))));

        this.output.accept(RUBY_HORSE_ARMOR_END_CITY, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.044f))
                        .add(LootItem.lootTableItem(ModItems.RUBY_HORSE_ARMOR.get()))
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(1)))));

        // VERIFICAR: EnchantWithLevelsFunction.enchantWithLevels ya no debería
        // necesitar el HolderLookup.Provider como primer argumento (antes
        // "this.registries", que ya no existe con el nuevo Context). Si el
        // compilador marca error aquí, pega ese mensaje y ajustamos la firma.
        this.output.accept(RUBY_ENCHANTED_SWORD_END_CITY, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.153f)) // 15% probabilidad
                        .add(LootItem.lootTableItem(ModItems.RUBY_SWORD.get())
                                .apply(EnchantWithLevelsFunction.enchantWithLevels(enchantments, ContextIntProviders.between(15, 30))))));

        this.output.accept(RUBY_ENCHANTED_PICKAXE_END_CITY, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.148f)) // 15% probabilidad
                        .add(LootItem.lootTableItem(ModItems.RUBY_PICKAXE.get())
                                .apply(EnchantWithLevelsFunction.enchantWithLevels(enchantments, ContextIntProviders.between(15, 30))))));

        this.output.accept(RUBY_ENCHANTED_SHOVEL_END_CITY, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.148f)) // 15% probabilidad
                        .add(LootItem.lootTableItem(ModItems.RUBY_SHOVEL.get())
                                .apply(EnchantWithLevelsFunction.enchantWithLevels(enchantments, ContextIntProviders.between(15, 30))))));

        this.output.accept(RUBY_ENCHANTED_HELMET_END_CITY, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.148f)) // 15% probabilidad
                        .add(LootItem.lootTableItem(ModItems.RUBY_HELMET.get())
                                .apply(EnchantWithLevelsFunction.enchantWithLevels(enchantments, ContextIntProviders.between(15, 30))))));

        this.output.accept(RUBY_ENCHANTED_CHESTPLATE_END_CITY, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.148f)) // 15% probabilidad
                        .add(LootItem.lootTableItem(ModItems.RUBY_CHESTPLATE.get())
                                .apply(EnchantWithLevelsFunction.enchantWithLevels(enchantments, ContextIntProviders.between(15, 30))))));

        this.output.accept(RUBY_ENCHANTED_LEGGINGS_END_CITY, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.148f)) // 15% probabilidad
                        .add(LootItem.lootTableItem(ModItems.RUBY_LEGGINGS.get())
                                .apply(EnchantWithLevelsFunction.enchantWithLevels(enchantments, ContextIntProviders.between(15, 30))))));

        this.output.accept(RUBY_ENCHANTED_BOOTS_END_CITY, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.148f)) // 15% probabilidad
                        .add(LootItem.lootTableItem(ModItems.RUBY_BOOTS.get())
                                .apply(EnchantWithLevelsFunction.enchantWithLevels(enchantments, ContextIntProviders.between(15, 30))))));

        // =====================================================================
        // NETHER BRIDGE
        // =====================================================================
        this.output.accept(RUBY_NETHER_BRIDGE, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.173f))
                        .add(LootItem.lootTableItem(ModItems.RUBY.get()))
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.between(1, 3)))));

        this.output.accept(RUBY_HORSE_ARMOR_NETHER_BRIDGE, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.111f))
                        .add(LootItem.lootTableItem(ModItems.RUBY_HORSE_ARMOR.get()))
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(1)))));

        // =====================================================================
        // STRONGHOLD
        // =====================================================================
        this.output.accept(RUBY_STRONGHOLD_CORRIDOR, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.173f))
                        .add(LootItem.lootTableItem(ModItems.RUBY.get()))
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.between(1, 3)))));

        this.output.accept(RUBY_STRONGHOLD_CROSSING, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.052f))
                        .add(LootItem.lootTableItem(ModItems.RUBY.get()))
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.between(1, 3)))));

        this.output.accept(RUBY_HORSE_ARMOR_STRONGHOLD_CORRIDOR, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.024f))
                        .add(LootItem.lootTableItem(ModItems.RUBY_HORSE_ARMOR.get()))
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(1)))));

        // =====================================================================
        // BASTION
        // =====================================================================
        this.output.accept(RUBY_BASTION_TREASURE, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.45f))
                        .add(LootItem.lootTableItem(ModItems.RUBY.get()))
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.between(2, 5)))));

        this.output.accept(RUBY_SWORD_BASTION_TREASURE, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.171f))
                        .add(LootItem.lootTableItem(ModItems.RUBY_SWORD.get()))
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(1)))));

        this.output.accept(RUBY_SWORD_DAMAGED_ENCHANTED_BASTION_TREASURE, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.15f)) // 15% probabilidad
                        .add(LootItem.lootTableItem(ModItems.RUBY_SWORD.get())
                                .apply(SetItemDamageFunction.setDamage(ContextFloatProviders.between(0.8f, 1f)))
                                .apply(EnchantWithLevelsFunction.enchantWithLevels(enchantments, ContextIntProviders.between(15, 30))))));

        this.output.accept(RUBY_ENCHANTED_PICKAXE_BASTION_OTHER, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.067f)) // 15% probabilidad
                        .add(LootItem.lootTableItem(ModItems.RUBY_PICKAXE.get())
                                .apply(EnchantWithLevelsFunction.enchantWithLevels(enchantments, ContextIntProviders.between(15, 30))))));

        this.output.accept(RUBY_ENCHANTED_PICKAXE_BASTION_HOGLIN_STABLE, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.132f)) // 15% probabilidad
                        .add(LootItem.lootTableItem(ModItems.RUBY_PICKAXE.get())
                                .apply(SetItemDamageFunction.setDamage(ContextFloatProviders.between(0.15f, 0.95f)))
                                .apply(EnchantWithLevelsFunction.enchantWithLevels(enchantments, ContextIntProviders.between(15, 30))))));

        this.output.accept(RUBY_ENCHANTED_SHOVEL_BASTION_OTHER, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.067f))
                        .add(LootItem.lootTableItem(ModItems.RUBY_SHOVEL.get()))
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(1)))));

        this.output.accept(RUBY_ENCHANTED_SHOVEL_BASTION_HOGLIN_STABLE, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.132f)) // 15% probabilidad
                        .add(LootItem.lootTableItem(ModItems.RUBY_SHOVEL.get())
                                .apply(SetItemDamageFunction.setDamage(ContextFloatProviders.between(0.15f, 0.95f)))
                                .apply(EnchantWithLevelsFunction.enchantWithLevels(enchantments, ContextIntProviders.between(15, 30))))));

        this.output.accept(RUBY_SPEAR_BASTION_TREASURE, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.171f))
                        .add(LootItem.lootTableItem(ModItems.RUBY_SPEAR.get()))
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(1)))));

        this.output.accept(RUBY_SPEAR_DAMAGED_ENCHANTED_BASTION_TREASURE, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.15f)) // 15% probabilidad
                        .add(LootItem.lootTableItem(ModItems.RUBY_SPEAR.get())
                                .apply(SetItemDamageFunction.setDamage(ContextFloatProviders.between(0.8f, 1f)))
                                .apply(EnchantWithLevelsFunction.enchantWithLevels(enchantments, ContextIntProviders.between(15, 30))))));

        this.output.accept(RUBY_HELMET_BASTION_TREASURE, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.132f))
                        .add(LootItem.lootTableItem(ModItems.RUBY_HELMET.get()))
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(1)))));

        this.output.accept(RUBY_HELMET_DAMAGED_ENCHANTED_BASTION_TREASURE, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.15f)) // 15% probabilidad
                        .add(LootItem.lootTableItem(ModItems.RUBY_HELMET.get())
                                .apply(SetItemDamageFunction.setDamage(ContextFloatProviders.between(0.8f, 1f)))
                                .apply(EnchantWithLevelsFunction.enchantWithLevels(enchantments, ContextIntProviders.between(15, 30))))));

        this.output.accept(RUBY_CHESTPLATE_BASTION_TREASURE, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.132f))
                        .add(LootItem.lootTableItem(ModItems.RUBY_CHESTPLATE.get()))
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(1)))));

        this.output.accept(RUBY_CHESTPLATE_DAMAGED_ENCHANTED_BASTION_TREASURE, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.15f)) // 15% probabilidad
                        .add(LootItem.lootTableItem(ModItems.RUBY_CHESTPLATE.get())
                                .apply(SetItemDamageFunction.setDamage(ContextFloatProviders.between(0.8f, 1f)))
                                .apply(EnchantWithLevelsFunction.enchantWithLevels(enchantments, ContextIntProviders.between(15, 30))))));

        this.output.accept(RUBY_LEGGINGS_BASTION_TREASURE, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.132f))
                        .add(LootItem.lootTableItem(ModItems.RUBY_LEGGINGS.get()))
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(1)))));

        this.output.accept(RUBY_LEGGINGS_DAMAGED_ENCHANTED_BASTION_TREASURE, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.15f)) // 15% probabilidad
                        .add(LootItem.lootTableItem(ModItems.RUBY_LEGGINGS.get())
                                .apply(SetItemDamageFunction.setDamage(ContextFloatProviders.between(0.8f, 1f)))
                                .apply(EnchantWithLevelsFunction.enchantWithLevels(enchantments, ContextIntProviders.between(15, 30))))));

        this.output.accept(RUBY_BOOTS_BASTION_TREASURE, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.132f))
                        .add(LootItem.lootTableItem(ModItems.RUBY_HELMET.get()))
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(1)))));

        this.output.accept(RUBY_BOOTS_DAMAGED_ENCHANTED_BASTION_TREASURE, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.15f)) // 15% probabilidad
                        .add(LootItem.lootTableItem(ModItems.RUBY_BOOTS.get())
                                .apply(SetItemDamageFunction.setDamage(ContextFloatProviders.between(0.8f, 1f)))
                                .apply(EnchantWithLevelsFunction.enchantWithLevels(enchantments, ContextIntProviders.between(15, 30))))));

        // =====================================================================
        // TRIAL CHAMBERS
        // =====================================================================
        this.output.accept(RUBY_TRIAL_CHAMBERS_INTERSECTION, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.38f))
                        .add(LootItem.lootTableItem(ModItems.RUBY.get()))
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.between(2, 5)))));

        this.output.accept(RUBY_BLOCK_TRIAL_CHAMBERS_INTERSECTION, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.021f))
                        .add(LootItem.lootTableItem(ModBlocks.RUBY_BLOCK.get()))
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(1)))));

        this.output.accept(RUBY_DAMAGED_PICKAXE_TRIAL_CHAMBERS_INTERSECTION, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.132f)) // 15% probabilidad
                        .add(LootItem.lootTableItem(ModItems.RUBY_PICKAXE.get())
                                .apply(SetItemDamageFunction.setDamage(ContextFloatProviders.between(0.2f, 0.8f))))));

        this.output.accept(RUBY_DAMAGED_AXE_TRIAL_CHAMBERS_INTERSECTION, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.132f)) // 15% probabilidad
                        .add(LootItem.lootTableItem(ModItems.RUBY_AXE.get())
                                .apply(SetItemDamageFunction.setDamage(ContextFloatProviders.between(0.2f, 0.8f))))));

        this.output.accept(RUBY_TRIAL_CHAMBERS_REWARD, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.15f))
                        .add(LootItem.lootTableItem(ModItems.RUBY.get()))
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.between(1, 3)))));

        this.output.accept(RUBY_ENCHANTED_SPEAR_TRIAL_CHAMBERS_REWARD, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.054f)) // 15% probabilidad
                        .add(LootItem.lootTableItem(ModItems.RUBY_SPEAR.get())
                                .apply(EnchantWithLevelsFunction.enchantWithLevels(enchantments, ContextIntProviders.between(15, 30))))));

        this.output.accept(RUBY_ENCHANTED_LEGGINGS_TRIAL_CHAMBERS_REWARD, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.031f)) // 15% probabilidad
                        .add(LootItem.lootTableItem(ModItems.RUBY_LEGGINGS.get())
                                .apply(EnchantWithLevelsFunction.enchantWithLevels(enchantments, ContextIntProviders.between(15, 30))))));

        this.output.accept(RUBY_TRIAL_CHAMBERS_REWARD_OMINOUS, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.34f))
                        .add(LootItem.lootTableItem(ModItems.RUBY.get()))
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.between(1, 3)))));

        this.output.accept(RUBY_BLOCK_TRIAL_CHAMBERS_REWARD_OMINOUS, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.029f))
                        .add(LootItem.lootTableItem(ModBlocks.RUBY_BLOCK.get()))
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(1)))));

        this.output.accept(RUBY_ENCHANTED_SPEAR_TRIAL_CHAMBERS_REWARD_OMINOUS, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.094f)) // 15% probabilidad
                        .add(LootItem.lootTableItem(ModItems.RUBY_SPEAR.get())
                                .apply(EnchantWithLevelsFunction.enchantWithLevels(enchantments, ContextIntProviders.between(15, 30))))));

        this.output.accept(RUBY_ENCHANTED_LEGGINGS_TRIAL_CHAMBERS_REWARD_OMINOUS, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.094f)) // 15% probabilidad
                        .add(LootItem.lootTableItem(ModItems.RUBY_LEGGINGS.get())
                                .apply(EnchantWithLevelsFunction.enchantWithLevels(enchantments, ContextIntProviders.between(15, 30))))));

        this.output.accept(RUBY_TRIAL_CHAMBERS_CORRIDOR_POT, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.124f))
                        .add(LootItem.lootTableItem(ModItems.RUBY.get()))
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.between(2, 3)))));

        // =====================================================================
        // WOODLAND MANSION
        // =====================================================================
        this.output.accept(RUBY_WOODLAND_MANSION, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.32f)) // 35% de probabilidad
                        .add(LootItem.lootTableItem(ModItems.RUBY.get()))
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.between(2, 4)))));

        this.output.accept(RUBY_SWORD_WOODLAND_MANSION, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.034f))
                        .add(LootItem.lootTableItem(ModItems.RUBY_SWORD.get()))
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(1)))));

        this.output.accept(RUBY_SWORD_DAMAGED_ENCHANTED_WOODLAND_MANSION, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.012f)) // 15% probabilidad
                        .add(LootItem.lootTableItem(ModItems.RUBY_SWORD.get())
                                .apply(SetItemDamageFunction.setDamage(ContextFloatProviders.between(0.8f, 1f)))
                                .apply(EnchantWithLevelsFunction.enchantWithLevels(enchantments, ContextIntProviders.between(15, 30))))));

        this.output.accept(RUBY_SPEAR_WOODLAND_MANSION, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.034f))
                        .add(LootItem.lootTableItem(ModItems.RUBY_SPEAR.get()))
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(1)))));

        this.output.accept(RUBY_SPEAR_DAMAGED_ENCHANTED_WOODLAND_MANSION, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.012f)) // 15% probabilidad
                        .add(LootItem.lootTableItem(ModItems.RUBY_SPEAR.get())
                                .apply(SetItemDamageFunction.setDamage(ContextFloatProviders.between(0.8f, 1f)))
                                .apply(EnchantWithLevelsFunction.enchantWithLevels(enchantments, ContextIntProviders.between(15, 30))))));

        this.output.accept(RUBY_ENCHANTED_PICKAXE_WOODLAND_MANSION, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.034f)) // 15% probabilidad
                        .add(LootItem.lootTableItem(ModItems.RUBY_PICKAXE.get())
                                .apply(EnchantWithLevelsFunction.enchantWithLevels(enchantments, ContextIntProviders.between(15, 30))))));

        this.output.accept(RUBY_ENCHANTED_AXE_WOODLAND_MANSION, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.034f)) // 15% probabilidad
                        .add(LootItem.lootTableItem(ModItems.RUBY_AXE.get())
                                .apply(EnchantWithLevelsFunction.enchantWithLevels(enchantments, ContextIntProviders.between(15, 30))))));

        this.output.accept(RUBY_SHOVEL_ENCHANTED_WOODLAND_MANSION, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.034f)) // 15% probabilidad
                        .add(LootItem.lootTableItem(ModItems.RUBY_SHOVEL.get())
                                .apply(EnchantWithLevelsFunction.enchantWithLevels(enchantments, ContextIntProviders.between(15, 30))))));

        this.output.accept(RUBY_HOE_ENCHANTED_WOODLAND_MANSION, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.034f)) // 15% probabilidad
                        .add(LootItem.lootTableItem(ModItems.RUBY_HOE.get())
                                .apply(EnchantWithLevelsFunction.enchantWithLevels(enchantments, ContextIntProviders.between(15, 30))))));

        this.output.accept(RUBY_HELMET_WOODLAND_MANSION, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.032f))
                        .add(LootItem.lootTableItem(ModItems.RUBY_HELMET.get()))
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(1)))));

        this.output.accept(RUBY_HELMET_DAMAGED_ENCHANTED_WOODLAND_MANSION, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.021f)) // 15% probabilidad
                        .add(LootItem.lootTableItem(ModItems.RUBY_HELMET.get())
                                .apply(SetItemDamageFunction.setDamage(ContextFloatProviders.between(0.8f, 1f)))
                                .apply(EnchantWithLevelsFunction.enchantWithLevels(enchantments, ContextIntProviders.between(15, 30))))));

        this.output.accept(RUBY_CHESTPLATE_WOODLAND_MANSION, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.032f))
                        .add(LootItem.lootTableItem(ModItems.RUBY_CHESTPLATE.get()))
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(1)))));

        this.output.accept(RUBY_CHESTPLATE_DAMAGED_ENCHANTED_WOODLAND_MANSION, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.021f)) // 15% probabilidad
                        .add(LootItem.lootTableItem(ModItems.RUBY_CHESTPLATE.get())
                                .apply(SetItemDamageFunction.setDamage(ContextFloatProviders.between(0.8f, 1f)))
                                .apply(EnchantWithLevelsFunction.enchantWithLevels(enchantments, ContextIntProviders.between(15, 30))))));

        this.output.accept(RUBY_LEGGINGS_WOODLAND_MANSION, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.032f))
                        .add(LootItem.lootTableItem(ModItems.RUBY_LEGGINGS.get()))
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(1)))));

        this.output.accept(RUBY_LEGGINGS_DAMAGED_ENCHANTED_WOODLAND_MANSION, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.021f)) // 15% probabilidad
                        .add(LootItem.lootTableItem(ModItems.RUBY_LEGGINGS.get())
                                .apply(SetItemDamageFunction.setDamage(ContextFloatProviders.between(0.8f, 1f)))
                                .apply(EnchantWithLevelsFunction.enchantWithLevels(enchantments, ContextIntProviders.between(15, 30))))));

        this.output.accept(RUBY_BOOTS_WOODLAND_MANSION, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.032f))
                        .add(LootItem.lootTableItem(ModItems.RUBY_HELMET.get()))
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(1)))));

        this.output.accept(RUBY_BOOTS_DAMAGED_ENCHANTED_WOODLAND_MANSION, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.021f)) // 15% probabilidad
                        .add(LootItem.lootTableItem(ModItems.RUBY_BOOTS.get())
                                .apply(SetItemDamageFunction.setDamage(ContextFloatProviders.between(0.8f, 1f)))
                                .apply(EnchantWithLevelsFunction.enchantWithLevels(enchantments, ContextIntProviders.between(15, 30))))));

        this.output.accept(RUBY_HORSE_ARMOR_WOODLAND_MANSION, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.012f))
                        .add(LootItem.lootTableItem(ModItems.RUBY_HORSE_ARMOR.get()))
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(1)))));

        // =====================================================================
        // PILLAGER OUTPOST
        // =====================================================================
        this.output.accept(RUBY_PILLAGER_OUTPOST, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.45f)) // 30% de probabilidad
                        .add(LootItem.lootTableItem(ModItems.RUBY.get()))
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.between(1, 3)))));

        this.output.accept(RUBY_SWORD_PILLAGER_OUTPOST, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.15f))
                        .add(LootItem.lootTableItem(ModItems.RUBY_SWORD.get()))
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(1)))
                        .apply(SetItemDamageFunction.setDamage(ContextFloatProviders.between(0.3f, 0.9f)))));

        this.output.accept(RUBY_SPEAR_PILLAGER_OUTPOST, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.15f))
                        .add(LootItem.lootTableItem(ModItems.RUBY_SPEAR.get()))
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(1)))
                        .apply(SetItemDamageFunction.setDamage(ContextFloatProviders.between(0.3f, 0.9f)))));

        this.output.accept(RUBY_PICKAXE_PILLAGER_OUTPOST, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.176f))
                        .add(LootItem.lootTableItem(ModItems.RUBY_PICKAXE.get()))
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(1)))
                        .apply(SetItemDamageFunction.setDamage(ContextFloatProviders.between(0.3f, 0.9f)))));

        this.output.accept(RUBY_AXE_PILLAGER_OUTPOST, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.172f))
                        .add(LootItem.lootTableItem(ModItems.RUBY_AXE.get()))
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(1)))
                        .apply(SetItemDamageFunction.setDamage(ContextFloatProviders.between(0.3f, 0.9f)))));

        this.output.accept(RUBY_CHESTPLATE_PILLAGER_OUTPOST, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.092f))
                        .add(LootItem.lootTableItem(ModItems.RUBY_CHESTPLATE.get()))
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(1)))
                        .apply(SetItemDamageFunction.setDamage(ContextFloatProviders.between(0.3f, 0.9f)))));

        this.output.accept(RUBY_BOOTS_PILLAGER_OUTPOST, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.092f))
                        .add(LootItem.lootTableItem(ModItems.RUBY_BOOTS.get()))
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(1)))
                        .apply(SetItemDamageFunction.setDamage(ContextFloatProviders.between(0.3f, 0.9f)))));

        this.output.accept(RUBY_HORSE_ARMOR_PILLAGER_OUTPOST, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.10f))
                        .add(LootItem.lootTableItem(ModItems.RUBY_HORSE_ARMOR.get()))
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(1)))));

        // =====================================================================
        // ANCIENTS CITY
        // =====================================================================
        this.output.accept(RUBY_HOE_DAMAGED_ENCHANTED_ANCIENT_CITY, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.15f)) // 15% probabilidad
                        .add(LootItem.lootTableItem(ModItems.RUBY_HOE.get())
                                .apply(SetItemDamageFunction.setDamage(ContextFloatProviders.between(0.8f, 1f)))
                                .apply(EnchantWithLevelsFunction.enchantWithLevels(enchantments, ContextIntProviders.between(15, 30))))));

        this.output.accept(RUBY_LEGGINGS_ENCHANTED_ANCIENT_CITY, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.17f))
                        .add(LootItem.lootTableItem(ModItems.RUBY_LEGGINGS.get())
                                .apply(EnchantWithLevelsFunction.enchantWithLevels(enchantments, ContextIntProviders.between(15, 30))))));

        this.output.accept(RUBY_HORSE_ARMOR_ANCIENT_CITY, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.174f))
                        .add(LootItem.lootTableItem(ModItems.RUBY_HORSE_ARMOR.get()))
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(1)))));

        // =====================================================================
        // OCEAN RUINS
        // =====================================================================
        this.output.accept(NAUTILUS_ARMOR_RUBY_UNDERWATER_RUIN_SMALL, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.023f))
                        .add(LootItem.lootTableItem(ModItems.RUBY_NAUTILUS_ARMOR.get()))
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(1)))));

        this.output.accept(NAUTILUS_ARMOR_RUBY_UNDERWATER_RUIN_BIG, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.075f))
                        .add(LootItem.lootTableItem(ModItems.RUBY_NAUTILUS_ARMOR.get()))
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(1)))));

    }
}