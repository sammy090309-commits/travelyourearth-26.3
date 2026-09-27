package com.sam_mc.travelyourearth.datagen;

import com.sam_mc.travelyourearth.TravelYourEarth;
import com.sam_mc.travelyourearth.item.ModItems;
import com.sam_mc.travelyourearth.loot.ReplaceWithItemLootModifier;
import com.sam_mc.travelyourearth.loot.RubyTrialGearLootModifier;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.AddTableLootModifier;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ModGlobalLootModifierProvider extends GlobalLootModifierProvider {

    public ModGlobalLootModifierProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, TravelYourEarth.MODID);
    }

    @Override
    protected void start() {

        // =====================================================================
        // 1-3. COFRES, VAULTS Y JARRONES
        // =====================================================================
        // Todo sale de ModExtraLootProvider.TARGETS: cada key ya sabe a qué tabla
        // vanilla se engancha. Para añadir/quitar loot solo tocas ModExtraLootProvider.
        for (Map.Entry<ResourceKey<LootTable>, ModExtraLootProvider.Target> entry : ModExtraLootProvider.TARGETS.entrySet()) {
            ModExtraLootProvider.Target target = entry.getValue();
            addModifier(target.modifierName(), target.vanillaTable(), entry.getKey());
        }

        // =====================================================================
        // 4. ARQUEOLOGÍA (reemplaza el ítem del bloque sospechoso; sin cambios)
        // =====================================================================
        // Solo donde vanilla tiene esmeralda
        addReplacement("archaeology/desert_pyramid",     "archaeology/desert_pyramid",     ModItems.RUBY.get(), 0.125f); // 12.5%
        addReplacement("archaeology/desert_well",        "archaeology/desert_well",        ModItems.RUBY.get(), 0.125f); // 12.5%
        addReplacement("archaeology/ocean_ruin_warm",    "archaeology/ocean_ruin_warm",    ModItems.RUBY.get(), 0.133f); // 13.3%
        addReplacement("archaeology/ocean_ruin_cold",    "archaeology/ocean_ruin_cold",    ModItems.RUBY.get(), 0.133f); // 13.3%
        addReplacement("archaeology/trail_ruins_common", "archaeology/trail_ruins_common", ModItems.RUBY.get(), 0.044f); // 4.4%

        // =====================================================================
        // 5. EQUIPAMIENTO (mobs de las trial chambers ominosas; sin cambios)
        // =====================================================================
        addRubyTrialGear("ruby_gear_ominous_melee",  "equipment/trial_chamber_melee",  0.125F, 0.2222F);
        addRubyTrialGear("ruby_gear_ominous_ranged", "equipment/trial_chamber_ranged", 0.125F, 0.0F);
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    // 26.3: LootModifier pide Optional<Holder<LootItemCondition>>. Holder.direct(...)
    // crea un Holder "suelto" (no registrado), que es lo que hace falta aquí.
    private static Optional<Holder<LootItemCondition>> condition(String lootTablePath) {
        LootItemCondition builtCondition = new LootTableIdCondition.Builder(Identifier.withDefaultNamespace(lootTablePath)).build();
        return Optional.of(Holder.direct(builtCondition));
    }

    /** Añade la tabla extra del mod a la tabla vanilla indicada. */
    private void addModifier(String name, String vanillaTable, ResourceKey<LootTable> extraLootTable) {
        this.add(name, new AddTableLootModifier(
                condition(vanillaTable),
                1000,
                extraLootTable
        ));
    }

    /** Arqueología: con "chance" cambia el ítem del bloque sospechoso por "item". */
    private void addReplacement(String name, String vanillaTable, Item item, float chance) {
        this.add(name, new ReplaceWithItemLootModifier(
                condition(vanillaTable),
                1000,
                item,
                chance));
    }

    /** Equipamiento de rubí para los mobs de las trial chambers ominosas. */
    private void addRubyTrialGear(String name, String vanillaTable, float armorChance, float weaponChance) {
        this.add(name, new RubyTrialGearLootModifier(
                condition(vanillaTable),
                1000,
                armorChance,
                weaponChance
        ));
    }
}