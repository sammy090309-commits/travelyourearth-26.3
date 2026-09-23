package com.sam_mc.travelyourearth.datagen;

import com.sam_mc.travelyourearth.TravelYourEarth;
import com.sam_mc.travelyourearth.loot.RubyTrialGearLootModifier;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.AddTableLootModifier;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;
import com.sam_mc.travelyourearth.item.ModItems;
import com.sam_mc.travelyourearth.loot.ReplaceWithItemLootModifier;
import net.minecraft.world.item.Item;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ModGlobalLootModifierProvider extends GlobalLootModifierProvider {

    public ModGlobalLootModifierProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, TravelYourEarth.MODID);
    }

    @Override
    protected void start() {
        // =====================================================================
        // DESERT PYRAMID
        // =====================================================================
        //chest
        addModifier("ruby_from_desert_pyramid", "chests/desert_pyramid", ModExtraLootProvider.RUBY_DESERT_PYRAMID);
        addModifier("ruby_horse_armor_from_desert_pyramid", "chests/desert_pyramid", ModExtraLootProvider.RUBY_HORSE_ARMOR_DESERT_PYRAMID);
        //archeology
        addReplacement("ruby_from_desert_pyramid_archaeology", "archaeology/desert_pyramid", ModItems.RUBY.get(), 0.125f);

        // =====================================================================
        // DESERT WELL
        // =====================================================================
        addReplacement("ruby_from_desert_well_archaeology", "archaeology/desert_well", ModItems.RUBY.get(), 0.125f);

        // =====================================================================
        // JUNGLE TEMPLE
        // =====================================================================
        addModifier("ruby_from_jungle_temple", "chests/jungle_temple", ModExtraLootProvider.RUBY_JUNGLE_TEMPLE);
        addModifier("ruby_horse_armor_from_jungle_temple", "chests/jungle_temple", ModExtraLootProvider.RUBY_HORSE_ARMOR_JUNGLE_TEMPLE);

        // =====================================================================
        // MONSTER ROOM
        // =====================================================================
        addModifier("ruby_horse_armor_from_monster_room", "chests/simple_dungeon", ModExtraLootProvider.RUBY_HORSE_ARMOR_MONSTER_ROOM);

        // =====================================================================
        // SHIPWRECK
        // =====================================================================
        addModifier("ruby_from_shipwreck_supply", "chests/shipwreck_supply", ModExtraLootProvider.NAUTILUS_ARMOR_RUBY_SHIPWRECK_SUPPLY);
        addModifier("ruby_from_shipwreck_treasure", "chests/shipwreck_treasure", ModExtraLootProvider.RUBY_SHIPWRECK_TREASURE);
        addModifier("ruby_nautilus_armor_from_shipwreck_treasure", "chests/shipwreck_treasure", ModExtraLootProvider.NAUTILUS_ARMOR_RUBY_SHIPWRECK_TREASURE);
        addModifier("ruby_from_shipwreck_map", "chests/shipwreck_map", ModExtraLootProvider.NAUTILUS_ARMOR_RUBY_SHIPWRECK_MAP);

        // =====================================================================
        // MINESHAFT & BURIED TREASURE
        // =====================================================================
        addModifier("ruby_from_mineshaft", "chests/abandoned_mineshaft", ModExtraLootProvider.RUBY_MINESHAFT);
        addModifier("ruby_from_buried_treasure", "chests/buried_treasure", ModExtraLootProvider.RUBY_BURIED_TREASURE);
        addModifier("ruby_nautilus_armor_from_buried_treasure", "chests/buried_treasure", ModExtraLootProvider.NAUTILUS_ARMOR_RUBY_BURIED_TREASURE);

        // =====================================================================
        // END CITY
        // =====================================================================
        addModifier("ruby_from_end_city", "chests/end_city_treasure", ModExtraLootProvider.RUBY_END_CITY);
        addModifier("ruby_enchanted_sword_from_end_city", "chests/end_city_treasure", ModExtraLootProvider.RUBY_ENCHANTED_SWORD_END_CITY);
        addModifier("ruby_enchanted_pickaxe_from_end_city", "chests/end_city_treasure", ModExtraLootProvider.RUBY_ENCHANTED_PICKAXE_END_CITY);
        addModifier("ruby_enchanted_shovel_from_end_city", "chests/end_city_treasure", ModExtraLootProvider.RUBY_ENCHANTED_SHOVEL_END_CITY);
        addModifier("ruby_enchanted_helmet_from_end_city", "chests/end_city_treasure", ModExtraLootProvider.RUBY_ENCHANTED_HELMET_END_CITY);
        addModifier("ruby_enchanted_chestplate_from_end_city", "chests/end_city_treasure", ModExtraLootProvider.RUBY_ENCHANTED_CHESTPLATE_END_CITY);
        addModifier("ruby_enchanted_leggings_from_end_city", "chests/end_city_treasure", ModExtraLootProvider.RUBY_ENCHANTED_LEGGINGS_END_CITY);
        addModifier("ruby_enchanted_boots_from_end_city", "chests/end_city_treasure", ModExtraLootProvider.RUBY_ENCHANTED_BOOTS_END_CITY);
        addModifier("ruby_horse_armor_from_end_city", "chests/end_city_treasure", ModExtraLootProvider.RUBY_HORSE_ARMOR_END_CITY);

        // =====================================================================
        // NETHER BRIDGE
        // =====================================================================
        addModifier("ruby_from_nether_bridge", "chests/nether_bridge", ModExtraLootProvider.RUBY_NETHER_BRIDGE);
        addModifier("ruby_horse_armor_from_nether_bridge", "chests/nether_bridge", ModExtraLootProvider.RUBY_HORSE_ARMOR_NETHER_BRIDGE);

        // =====================================================================
        // STRONGHOLD
        // =====================================================================
        addModifier("ruby_from_stronghold_corridor", "chests/stronghold_corridor", ModExtraLootProvider.RUBY_STRONGHOLD_CORRIDOR);
        addModifier("ruby_horse_armor_from_stronghold_corridor", "chests/stronghold_corridor", ModExtraLootProvider.RUBY_HORSE_ARMOR_STRONGHOLD_CORRIDOR);
        addModifier("ruby_from_stronghold_crossing", "chests/stronghold_crossing", ModExtraLootProvider.RUBY_STRONGHOLD_CROSSING);

        // =====================================================================
        // BASTION
        // =====================================================================
        addModifier("ruby_from_bastion_treasure", "chests/bastion_treasure", ModExtraLootProvider.RUBY_BASTION_TREASURE);
        addModifier("ruby_sword_from_bastion_treasure", "chests/bastion_treasure", ModExtraLootProvider.RUBY_SWORD_BASTION_TREASURE);
        addModifier("ruby_sword_damaged_enchanted_from_bastion_treasure", "chests/bastion_treasure", ModExtraLootProvider.RUBY_SWORD_DAMAGED_ENCHANTED_BASTION_TREASURE);
        addModifier("ruby_enchanted_pickaxe_from_bastion_other", "chests/bastion_other", ModExtraLootProvider.RUBY_ENCHANTED_PICKAXE_BASTION_OTHER);
        addModifier("ruby_enchanted_pickaxe_from_hoglin_stable", "chests/bastion_hoglin_stable", ModExtraLootProvider.RUBY_ENCHANTED_PICKAXE_BASTION_HOGLIN_STABLE);
        addModifier("ruby_enchanted_shovel_from_bastion_other", "chests/bastion_other", ModExtraLootProvider.RUBY_ENCHANTED_SHOVEL_BASTION_OTHER);
        addModifier("ruby_enchanted_shovel_from_hoglin_stable", "chests/bastion_hoglin_stable", ModExtraLootProvider.RUBY_ENCHANTED_SHOVEL_BASTION_HOGLIN_STABLE);
        addModifier("ruby_spear_from_bastion_treasure", "chests/bastion_treasure", ModExtraLootProvider.RUBY_SPEAR_BASTION_TREASURE);
        addModifier("ruby_spear_damaged_enchanted_from_bastion_treasure", "chests/bastion_treasure", ModExtraLootProvider.RUBY_SPEAR_DAMAGED_ENCHANTED_BASTION_TREASURE);
        addModifier("ruby_helmet_from_bastion_treasure", "chests/bastion_treasure", ModExtraLootProvider.RUBY_HELMET_BASTION_TREASURE);
        addModifier("ruby_helmet_damaged_enchanted_from_bastion_treasure", "chests/bastion_treasure", ModExtraLootProvider.RUBY_HELMET_DAMAGED_ENCHANTED_BASTION_TREASURE);
        addModifier("ruby_chastplate_from_bastion_treasure", "chests/bastion_treasure", ModExtraLootProvider.RUBY_CHESTPLATE_BASTION_TREASURE);
        addModifier("ruby_chastplate_damaged_enchanted_from_bastion_treasure", "chests/bastion_treasure", ModExtraLootProvider.RUBY_CHESTPLATE_DAMAGED_ENCHANTED_BASTION_TREASURE);
        addModifier("ruby_leggings_from_bastion_treasure", "chests/bastion_treasure", ModExtraLootProvider.RUBY_LEGGINGS_BASTION_TREASURE);
        addModifier("ruby_leggings_damaged_enchanted_from_bastion_treasure", "chests/bastion_treasure", ModExtraLootProvider.RUBY_LEGGINGS_DAMAGED_ENCHANTED_BASTION_TREASURE);
        addModifier("ruby_boots_from_bastion_treasure", "chests/bastion_treasure", ModExtraLootProvider.RUBY_BOOTS_BASTION_TREASURE);
        addModifier("ruby_boots_damaged_enchanted_from_bastion_treasure", "chests/bastion_treasure", ModExtraLootProvider.RUBY_BOOTS_DAMAGED_ENCHANTED_BASTION_TREASURE);
        // =====================================================================
        // TRIAL CHAMBERS
        // =====================================================================
        //chests
        addModifier("ruby_from_trial_chambers_intersection", "chests/trial_chambers/intersection", ModExtraLootProvider.RUBY_TRIAL_CHAMBERS_INTERSECTION);
        addModifier("ruby_block_from_trial_chambers_intersection", "chests/trial_chambers/intersection", ModExtraLootProvider.RUBY_BLOCK_TRIAL_CHAMBERS_INTERSECTION);
        addModifier("ruby_damaged_pickaxe_from_trial_chambers_intersection", "chests/trial_chambers/intersection", ModExtraLootProvider.RUBY_DAMAGED_PICKAXE_TRIAL_CHAMBERS_INTERSECTION);
        addModifier("ruby_damaged_axe_from_trial_chambers_intersection", "chests/trial_chambers/intersection", ModExtraLootProvider.RUBY_DAMAGED_AXE_TRIAL_CHAMBERS_INTERSECTION);
        //Vaults
        addModifier("ruby_from_trial_chambers_reward", "chests/trial_chambers/reward", ModExtraLootProvider.RUBY_TRIAL_CHAMBERS_REWARD);
        addModifier("ruby_enchanted_spear_from_chambers_reward", "chests/trial_chambers/reward", ModExtraLootProvider.RUBY_ENCHANTED_SPEAR_TRIAL_CHAMBERS_REWARD);
        addModifier("ruby_enchanted_leggings_from_chambers_reward", "chests/trial_chambers/reward", ModExtraLootProvider.RUBY_ENCHANTED_LEGGINGS_TRIAL_CHAMBERS_REWARD);
        //Ominous Vaults
        addModifier("ruby_from_trial_chambers_reward_ominous", "chests/trial_chambers/reward_ominous", ModExtraLootProvider.RUBY_TRIAL_CHAMBERS_REWARD_OMINOUS);
        addModifier("ruby_block_from_trial_chambers_reward_ominous", "chests/trial_chambers/reward_ominous", ModExtraLootProvider.RUBY_BLOCK_TRIAL_CHAMBERS_REWARD_OMINOUS);
        addModifier("ruby_enchanted_spear_from_chambers_reward_ominous", "chests/trial_chambers/reward_ominous", ModExtraLootProvider.RUBY_ENCHANTED_SPEAR_TRIAL_CHAMBERS_REWARD_OMINOUS);
        addModifier("ruby_enchanted_leggings_from_chambers_reward_ominous", "chests/trial_chambers/reward_ominous", ModExtraLootProvider.RUBY_ENCHANTED_LEGGINGS_TRIAL_CHAMBERS_REWARD_OMINOUS);
        //Pots
        addModifier("ruby_from_trial_pots", "pots/trial_chambers/corridor", ModExtraLootProvider.RUBY_TRIAL_CHAMBERS_CORRIDOR_POT);


        // =====================================================================
        // WOODLAND MANSION
        // =====================================================================
        addModifier("ruby_from_woodland_mansion", "chests/woodland_mansion", ModExtraLootProvider.RUBY_WOODLAND_MANSION);
        addModifier("ruby_sword_from_woodland_mansion", "chests/woodland_mansion", ModExtraLootProvider.RUBY_SWORD_WOODLAND_MANSION);
        addModifier("ruby_sword_damaged_enchanted_from_woodland_mansion", "chests/woodland_mansion", ModExtraLootProvider.RUBY_SWORD_DAMAGED_ENCHANTED_WOODLAND_MANSION);
        addModifier("ruby_enchanted_pickaxe_from_woodland_mansion", "chests/woodland_mansion", ModExtraLootProvider.RUBY_ENCHANTED_PICKAXE_WOODLAND_MANSION);
        addModifier("ruby_enchanted_pickaxe_from_woodland_mansion", "chests/woodland_mansion", ModExtraLootProvider.RUBY_ENCHANTED_PICKAXE_WOODLAND_MANSION);
        addModifier("ruby_enchanted_axe_from_woodland_mansion", "chests/woodland_mansion", ModExtraLootProvider.RUBY_ENCHANTED_AXE_WOODLAND_MANSION);
        addModifier("ruby_enchanted_shovel_from_woodland_mansion", "chests/woodland_mansion", ModExtraLootProvider.RUBY_SHOVEL_ENCHANTED_WOODLAND_MANSION);
        addModifier("ruby_enchanted_hoe_from_woodland_mansion", "chests/woodland_mansion", ModExtraLootProvider.RUBY_HOE_ENCHANTED_WOODLAND_MANSION);
        addModifier("ruby_spear_from_woodland_mansion", "chests/woodland_mansion", ModExtraLootProvider.RUBY_SPEAR_WOODLAND_MANSION);
        addModifier("ruby_spear_damaged_enchanted_from_woodland_mansion", "chests/woodland_mansion", ModExtraLootProvider.RUBY_SPEAR_DAMAGED_ENCHANTED_WOODLAND_MANSION);
        addModifier("ruby_helmet_from_woodland_mansion", "chests/woodland_mansion", ModExtraLootProvider.RUBY_HELMET_WOODLAND_MANSION);
        addModifier("ruby_helmet_damaged_enchanted_from_woodland_mansion", "chests/woodland_mansion", ModExtraLootProvider.RUBY_HELMET_DAMAGED_ENCHANTED_WOODLAND_MANSION);
        addModifier("ruby_chestplate_from_woodland_mansion", "chests/woodland_mansion", ModExtraLootProvider.RUBY_CHESTPLATE_WOODLAND_MANSION);
        addModifier("ruby_chestplate_damaged_enchanted_from_woodland_mansion", "chests/woodland_mansion", ModExtraLootProvider.RUBY_CHESTPLATE_DAMAGED_ENCHANTED_WOODLAND_MANSION);
        addModifier("ruby_leggings_from_woodland_mansion", "chests/woodland_mansion", ModExtraLootProvider.RUBY_LEGGINGS_WOODLAND_MANSION);
        addModifier("ruby_leggings_damaged_enchanted_from_woodland_mansion", "chests/woodland_mansion", ModExtraLootProvider.RUBY_LEGGINGS_DAMAGED_ENCHANTED_WOODLAND_MANSION);
        addModifier("ruby_boots_from_woodland_mansion", "chests/woodland_mansion", ModExtraLootProvider.RUBY_BOOTS_WOODLAND_MANSION);
        addModifier("ruby_boots_damaged_enchanted_from_woodland_mansion", "chests/woodland_mansion", ModExtraLootProvider.RUBY_BOOTS_DAMAGED_ENCHANTED_WOODLAND_MANSION);
        addModifier("ruby_horse_armor_from_woodland_mansion", "chests/woodland_mansion", ModExtraLootProvider.RUBY_HORSE_ARMOR_WOODLAND_MANSION);


        // =====================================================================
        // PILLAGER OUTPOST
        // =====================================================================
        addModifier("ruby_from_pillager_outpost", "chests/pillager_outpost", ModExtraLootProvider.RUBY_PILLAGER_OUTPOST);
        addModifier("ruby_spear_from_pillager_outpost", "chests/pillager_outpost", ModExtraLootProvider.RUBY_SPEAR_PILLAGER_OUTPOST);
        addModifier("ruby_sword_from_pillager_outpost", "chests/pillager_outpost", ModExtraLootProvider.RUBY_SWORD_PILLAGER_OUTPOST);
        addModifier("ruby_axe_from_pillager_outpost", "chests/pillager_outpost", ModExtraLootProvider.RUBY_AXE_PILLAGER_OUTPOST);
        addModifier("ruby_pickaxe_from_pillager_outpost", "chests/pillager_outpost", ModExtraLootProvider.RUBY_PICKAXE_PILLAGER_OUTPOST);
        addModifier("ruby_chestplate_from_pillager_outpost", "chests/pillager_outpost", ModExtraLootProvider.RUBY_CHESTPLATE_PILLAGER_OUTPOST);
        addModifier("ruby_boots_from_pillager_outpost", "chests/pillager_outpost", ModExtraLootProvider.RUBY_BOOTS_PILLAGER_OUTPOST);
        addModifier("ruby_horse_armor_from_pillager_outpost", "chests/pillager_outpost", ModExtraLootProvider.RUBY_HORSE_ARMOR_PILLAGER_OUTPOST);
        // =====================================================================
        // ANCIENTS CITY
        // =====================================================================
        addModifier("ruby_hoe_damaged_enchanted_from_ancient_city", "chests/ancient_city", ModExtraLootProvider.RUBY_HOE_DAMAGED_ENCHANTED_ANCIENT_CITY);
        addModifier("ruby_leggings_enchanted_from_ancient_city", "chests/ancient_city", ModExtraLootProvider.RUBY_LEGGINGS_ENCHANTED_ANCIENT_CITY);
        addModifier("ruby_horse_armor_from_ancient_city", "chests/ancient_city", ModExtraLootProvider.RUBY_HORSE_ARMOR_ANCIENT_CITY);

        // =====================================================================
        // OCEAN RUINS
        // =====================================================================
        addModifier("ruby_nautilus_armor_from_underwater_ruin_small", "chests/underwater_ruin_small", ModExtraLootProvider.NAUTILUS_ARMOR_RUBY_UNDERWATER_RUIN_SMALL);
        addModifier("ruby_nautilus_armor_from_underwater_ruin_big", "chests/underwater_ruin_big", ModExtraLootProvider.NAUTILUS_ARMOR_RUBY_UNDERWATER_RUIN_BIG);
        //Archeology
        addReplacement("ruby_from_ocean_ruin_warm_archaeology", "archaeology/ocean_ruin_warm", ModItems.RUBY.get(), 0.133f);
        addReplacement("ruby_from_ocean_ruin_cold_archaeology", "archaeology/ocean_ruin_cold", ModItems.RUBY.get(), 0.133f);

        // =====================================================================
        // TRIAL RUINS
        // =====================================================================
        addReplacement("ruby_from_trail_ruins_common_archaeology", "archaeology/trail_ruins_common", ModItems.RUBY.get(), 0.044f);

        // =====================================================================
        // TRIAL CHAMBERS (trial ominoso)
        // =====================================================================
        addRubyTrialGear("ruby_gear_ominous_melee",  "equipment/trial_chamber_melee",  0.125F, 0.2222F);
        addRubyTrialGear("ruby_gear_ominous_ranged", "equipment/trial_chamber_ranged", 0.125F, 0.0F);
    }

    // NUEVO en 26.3: helper para envolver una condicion suelta en el formato
    // que ahora pide LootModifier: Optional<Holder<LootItemCondition>> en vez
    // de un array LootItemCondition[]. Holder.direct(...) crea un Holder "suelto"
    // (no registrado), que es exactamente lo que se necesita aqui porque estas
    // condiciones no vienen de un registro con datapack, se arman a mano.
    private static Optional<Holder<LootItemCondition>> condition(String lootTablePath) {
        LootItemCondition builtCondition = new LootTableIdCondition.Builder(Identifier.withDefaultNamespace(lootTablePath)).build();
        return Optional.of(Holder.direct(builtCondition));
    }

    // Helper method to eliminate boilerplate code for standard table additions
    private void addModifier(String name, String lootTablePath, net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable> extraLootTable) {
        this.add(name, new AddTableLootModifier(
                condition(lootTablePath),
                1000,
                extraLootTable
        ));
    }

    private void addReplacement(String name, String lootTablePath, Item item, float chance) {
        this.add(name, new ReplaceWithItemLootModifier(
                condition(lootTablePath),
                1000,
                item,
                chance));
    }

    private void addRubyTrialGear(String name, String lootTablePath, float armorChance, float weaponChance) {
        this.add(name, new RubyTrialGearLootModifier(
                condition(lootTablePath),
                1000,
                armorChance,
                weaponChance
        ));
    }


}