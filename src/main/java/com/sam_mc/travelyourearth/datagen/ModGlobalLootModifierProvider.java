package com.sam_mc.travelyourearth.datagen;

import com.sam_mc.travelyourearth.TravelYourEarth;
import com.sam_mc.travelyourearth.item.ModItems;
import com.sam_mc.travelyourearth.loot.ReplaceWithItemLootModifier;
import com.sam_mc.travelyourearth.loot.RubyTrialGearLootModifier;
import com.sam_mc.travelyourearth.loot.SavannaVillageChestModifier;
import com.sam_mc.travelyourearth.loot.SpawnMobsLootModifier;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.AddTableLootModifier;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Generates the global loot modifiers of the mod:
 *   1-3. Chests, vaults and decorated pots  -> extra tables from ModExtraLootProvider
 *   4.   Archaeology                         -> replaces the item of the suspicious block
 *   5.   Equipment                           -> ruby gear for ominous trial chamber mobs
 *   6.   Savanna village chests              -> diamond -> ruby, extra rubies, ruby gear (rarer than iron)
 *   7.   Trial chamber pots                  -> sometimes bats / silverfish jump out (1.2.1)
 */
public class ModGlobalLootModifierProvider extends GlobalLootModifierProvider {

    // =========================================================================
    // ARCHAEOLOGY RULES
    // =========================================================================
    // Archaeology gives ONE item per brushed block, so the vanilla chance is just weight / total weight.
    // Since the ruby REPLACES the item, the other items (emerald included) drop a little.
    // relativeToEmerald() takes that into account, so the FINAL ruby chance follows the rule exactly.

    /** Same chance as the emerald (table where every item has the same chance). NOT boosted (repeated chance). */
    private static final float SAME_AS_EMERALD = 1.0f;
    /** Table with emerald only: 0.8x the emerald (same rule as ModExtraLootProvider), x1.1 since 1.2. */
    private static final float EMERALD_ONLY = 0.8f * ModExtraLootProvider.LOOT_BOOST;

    /**
     * Replacement chance so that, after replacing, ruby = factor x (the emerald that is left).
     * Example: emerald 12.5%, factor 1.0 -> replace 11.1% -> ruby 11.1% and emerald 11.1%.
     */
    private static float relativeToEmerald(float emerald, float factor) {
        float target = emerald * factor;
        return target / (1f + target);
    }

    public ModGlobalLootModifierProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, TravelYourEarth.MODID);
    }

    @Override
    protected void start() {

        // =====================================================================
        // 1-3. CHESTS, VAULTS AND DECORATED POTS
        // =====================================================================
        // Everything comes from ModExtraLootProvider.TARGETS: each key already knows which
        // vanilla table it hooks into. To add/remove loot you only touch ModExtraLootProvider.
        for (Map.Entry<ResourceKey<LootTable>, ModExtraLootProvider.Target> entry : ModExtraLootProvider.TARGETS.entrySet()) {
            ModExtraLootProvider.Target target = entry.getValue();
            addModifier(target.modifierName(), target.vanillaTable(), entry.getKey());
        }

        // =====================================================================
        // 4. ARCHAEOLOGY (replaces the item of the suspicious block)
        // =====================================================================
        // Only where vanilla has emerald. Values from the vanilla 26.3 loot tables.

        // Desert pyramid: all 8 items have the same chance (diamond and emerald 12.5%) -> ruby = emerald
        addReplacement("archaeology/desert_pyramid",     "archaeology/desert_pyramid",
                ModItems.RUBY.get(), relativeToEmerald(0.125f, SAME_AS_EMERALD));   // 11.1%

        // Emerald only -> ruby = 0.8x emerald
        addReplacement("archaeology/desert_well",        "archaeology/desert_well",
                ModItems.RUBY.get(), relativeToEmerald(0.125f, EMERALD_ONLY));      // 9.9%  (emerald 12.5%)
        addReplacement("archaeology/ocean_ruin_warm",    "archaeology/ocean_ruin_warm",
                ModItems.RUBY.get(), relativeToEmerald(0.1333f, EMERALD_ONLY));     // 10.5% (emerald 13.3%)
        addReplacement("archaeology/ocean_ruin_cold",    "archaeology/ocean_ruin_cold",
                ModItems.RUBY.get(), relativeToEmerald(0.1333f, EMERALD_ONLY));     // 10.5% (emerald 13.3%)
        addReplacement("archaeology/trail_ruins_common", "archaeology/trail_ruins_common",
                ModItems.RUBY.get(), relativeToEmerald(0.0444f, EMERALD_ONLY));     // 3.8%  (emerald 4.4%)

        // =====================================================================
        // 5. EQUIPMENT (ominous trial chamber mobs; unchanged)
        // =====================================================================
        addRubyTrialGear("ruby_gear_ominous_melee",  "equipment/trial_chamber_melee",  0.125F, 0.2222F);
        addRubyTrialGear("ruby_gear_ominous_ranged", "equipment/trial_chamber_ranged", 0.125F, 0.0F);

        // =====================================================================
        // 6. SAVANNA VILLAGE CHESTS ("the country of the ruby")
        // =====================================================================
        // Hooks into the profession chests (weaponsmith, toolsmith...), but the modifier itself checks
        // that the chest is REALLY inside a savanna village. In other villages nothing changes.
        // (The savanna HOUSE has its own independent ruby table: ModExtraLootProvider.)
        for (String table : VILLAGE_CHESTS) {
            this.add("chests/village/savanna_" + table, new SavannaVillageChestModifier(
                    condition("chests/village/" + table),
                    1000,
                    SAVANNA_RUBY_CHANCE,
                    SAVANNA_GEAR_CHANCE));
        }

        // =====================================================================
        // 7. TRIAL CHAMBER POTS (1.2.1): sometimes bats or silverfish jump out
        // =====================================================================
        // The pot loot is rolled when the pot breaks, so the mobs come out of the broken pot.
        // Two independent rolls; the normal loot of the pot stays the same.
        this.add("pots/trial_chambers_bats", new SpawnMobsLootModifier(
                condition("pots/trial_chambers/corridor"), 1000, EntityTypes.BAT, 0.05F, 1, 3));        // 5%: 1-3 bats
        this.add("pots/trial_chambers_silverfish", new SpawnMobsLootModifier(
                condition("pots/trial_chambers/corridor"), 1000, EntityTypes.SILVERFISH, 0.03F, 1, 3)); // 3%: 1-3 silverfish
    }

    // =========================================================================
    // SAVANNA VILLAGES
    // =========================================================================

    /** Rubies very common: 75% chance per emerald stack (was 1/4, then 1/3). */
    private static final float SAVANNA_RUBY_CHANCE = 0.75F * ModExtraLootProvider.LOOT_BOOST;  // 82.5%

    /** Ruby gear "like the iron, but rarer": 35% chance per iron gear piece in the chest. */
    private static final float SAVANNA_GEAR_CHANCE = 0.35F * ModExtraLootProvider.LOOT_BOOST;  // 38.5%

    /**
     * Vanilla village chest tables (inside chests/village/) shared by every village type.
     * Left out: the houses of the other village types, and "village_savanna_house", which has its
     * own independent ruby table in ModExtraLootProvider (SAVANNA_HOUSE_RUBY).
     */
    private static final List<String> VILLAGE_CHESTS = List.of(
            "village_armorer", "village_butcher", "village_cartographer", "village_fisher",
            "village_fletcher", "village_mason", "village_shepherd", "village_tannery",
            "village_temple", "village_toolsmith", "village_weaponsmith");

    // =========================================================================
    // HELPERS
    // =========================================================================

    // 26.3: LootModifier needs an Optional<Holder<LootItemCondition>>. Holder.direct(...)
    // creates a "loose" (unregistered) Holder, which is what we need here.
    private static Optional<Holder<LootItemCondition>> condition(String lootTablePath) {
        LootItemCondition builtCondition = new LootTableIdCondition.Builder(Identifier.withDefaultNamespace(lootTablePath)).build();
        return Optional.of(Holder.direct(builtCondition));
    }

    /** Adds the mod's extra table to the given vanilla table. */
    private void addModifier(String name, String vanillaTable, ResourceKey<LootTable> extraLootTable) {
        this.add(name, new AddTableLootModifier(
                condition(vanillaTable),
                1000,
                extraLootTable
        ));
    }

    /** Archaeology: with "chance", replaces the item of the suspicious block with "item". */
    private void addReplacement(String name, String vanillaTable, Item item, float chance) {
        this.add(name, new ReplaceWithItemLootModifier(
                condition(vanillaTable),
                1000,
                item,
                chance));
    }

    /** Ruby equipment for ominous trial chamber mobs. */
    private void addRubyTrialGear(String name, String vanillaTable, float armorChance, float weaponChance) {
        this.add(name, new RubyTrialGearLootModifier(
                condition(vanillaTable),
                1000,
                armorChance,
                weaponChance
        ));
    }
}