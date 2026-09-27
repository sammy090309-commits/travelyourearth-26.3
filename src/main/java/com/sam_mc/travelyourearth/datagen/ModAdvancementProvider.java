package com.sam_mc.travelyourearth.datagen;

import com.sam_mc.travelyourearth.TravelYourEarth;
import com.sam_mc.travelyourearth.block.ModBlocks;
import com.sam_mc.travelyourearth.condition.ModAdvancementTabCondition;
import com.sam_mc.travelyourearth.event.RubyArmorFireHandler;
import com.sam_mc.travelyourearth.item.ModItems;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.advancements.triggers.ImpossibleTrigger;
import net.minecraft.advancements.triggers.InventoryChangeTrigger;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * Logros del mod. Se generan DOS versiones de cada uno, con condiciones,
 * y la opción "modAdvancementTab" (config COMMON) decide cuál se carga:
 *
 *   OFF (por defecto)                      ON
 *   Aventura                               Travel Your Earth   <- pestaña propia (raíz, icono: bloque terráqueo)
 *    └ ¿Volviste?                           └ ¿Volviste?
 *       └ A prueba de fuego                    └ A prueba de fuego
 *
 *   IDs:  travelyourearth:adventure/...    travelyourearth:ruby/...
 */
public class ModAdvancementProvider extends AdvancementSubProvider {

    /**
     * Fondo de la pestaña propia: tierra Earth (textures/block/earth_dirt.png).
     * AdvancementTabMixin lo reconoce y le pone césped Earth arriba y cielo encima.
     */
    private static final Identifier MOD_TAB_BACKGROUND =
            Identifier.fromNamespaceAndPath(TravelYourEarth.MODID, "block/earth_dirt");

    private static final ModAdvancementTabCondition VANILLA_TAB = new ModAdvancementTabCondition(false);
    private static final ModAdvancementTabCondition MOD_TAB = new ModAdvancementTabCondition(true);

    public ModAdvancementProvider(BootstrapContext<Advancement> output) {
        super(output);
    }

    @Override
    public void generate() {

        // =====================================================================
        // VERSIÓN 1: en la pestaña vanilla "Aventura"  (opción OFF)
        // =====================================================================
        // Referencia al logro vanilla "Aventura". No crea ningún archivo, solo sirve de "padre".
        AdvancementHolder adventureRoot = Advancement.Builder.advancement()
                .build(Identifier.withDefaultNamespace("adventure/root"));

        AdvancementHolder obtainRubyVanilla = obtainRuby()
                .parent(adventureRoot)
                .display(
                        ModItems.RUBY.get(),
                        Component.translatable("advancements.travelyourearth.obtain_ruby.title"),
                        Component.translatable("advancements.travelyourearth.obtain_ruby.description"),
                        AdvancementType.TASK,
                        true,   // cartelito arriba a la derecha
                        true,   // anunciarlo en el chat
                        false)  // no oculto
                .save(output, Identifier.fromNamespaceAndPath(TravelYourEarth.MODID, "adventure/obtain_ruby"), VANILLA_TAB);

        fireproof()
                .parent(obtainRubyVanilla)
                .save(output, RubyArmorFireHandler.FIREPROOF_ADVANCEMENT, VANILLA_TAB);

        // =====================================================================
        // VERSIÓN 2: pestaña propia del mod  (opción ON)
        // =====================================================================
        // RAÍZ "Travel Your Earth": da nombre, icono (bloque terráqueo) y fondo a la pestaña.
        // Se consigue a la vez que "¿Volviste?" (al tener un rubí), pero en SILENCIO
        // (sin cartel ni chat), igual que las raíces vanilla. Así la pestaña aparece con tu primer rubí.
        AdvancementHolder modTabRoot = obtainRuby()
                .rootDisplay(
                        ModBlocks.EARTH_BLOCK.get().asItem(), // el icono tiene que ser un ÍTEM
                        Component.translatable("advancements.travelyourearth.root.title"),
                        Component.translatable("advancements.travelyourearth.root.description"),
                        MOD_TAB_BACKGROUND, // rootDisplay + fondo = raíz de una pestaña nueva
                        AdvancementType.TASK,
                        false,  // sin cartelito
                        false,  // sin mensaje en el chat
                        false)
                .save(output, Identifier.fromNamespaceAndPath(TravelYourEarth.MODID, "ruby/root"), MOD_TAB);

        AdvancementHolder obtainRubyModTab = obtainRuby()
                .parent(modTabRoot)
                .display(
                        ModItems.RUBY.get(),
                        Component.translatable("advancements.travelyourearth.obtain_ruby.title"),
                        Component.translatable("advancements.travelyourearth.obtain_ruby.description"),
                        AdvancementType.TASK,
                        true,
                        true,
                        false)
                .save(output, Identifier.fromNamespaceAndPath(TravelYourEarth.MODID, "ruby/obtain_ruby"), MOD_TAB);

        fireproof()
                .parent(obtainRubyModTab)
                .save(output, RubyArmorFireHandler.FIREPROOF_ADVANCEMENT_MOD_TAB, MOD_TAB);
    }

    // =========================================================================
    // Partes que comparten las dos versiones
    // =========================================================================

    /** ¿VOLVISTE? — guiño a Minecraft Earth, donde el rubí era la moneda. */
    private static Advancement.Builder obtainRuby() {
        return Advancement.Builder.advancement()
                .addCriterion("has_ruby", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.RUBY.get()));
    }

    /**
     * A PRUEBA DE FUEGO — criterio "impossible": no se cumple solo.
     * Lo da RubyArmorFireHandler cuando la armadura de rubí le apaga el fuego al jugador.
     */
    private static Advancement.Builder fireproof() {
        return Advancement.Builder.advancement()
                .display(
                        ModItems.RUBY_CHESTPLATE.get(),
                        Component.translatable("advancements.travelyourearth.fireproof.title"),
                        Component.translatable("advancements.travelyourearth.fireproof.description"),
                        AdvancementType.TASK,
                        true,
                        true,
                        false)
                .addCriterion(RubyArmorFireHandler.FIREPROOF_CRITERION,
                        CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance()));
    }
}