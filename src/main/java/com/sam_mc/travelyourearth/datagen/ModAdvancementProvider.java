package com.sam_mc.travelyourearth.datagen;

import com.sam_mc.travelyourearth.TravelYourEarth;
import com.sam_mc.travelyourearth.advancement.ModAdvancements;
import com.sam_mc.travelyourearth.block.ModBlocks;
import com.sam_mc.travelyourearth.condition.ModAdvancementTabCondition;
import com.sam_mc.travelyourearth.event.RubyArmorFireHandler;
import com.sam_mc.travelyourearth.item.ModItems;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.advancements.triggers.ImpossibleTrigger;
import net.minecraft.advancements.triggers.InventoryChangeTrigger;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;

/**
 * Logros del mod. Se generan DOS versiones de cada uno, con condiciones,
 * y la opción "modAdvancementTab" (config COMMON) decide cuál se carga:
 *
 *   OFF (por defecto)                      ON
 *   Aventura                               Travel Your Earth   <- pestaña propia (raíz, icono: bloque terráqueo)
 *    └ ¿Volviste?                           └ ¿Volviste?
 *       ├ A prueba de fuego                    ├ A prueba de fuego
 *       └ Psst… ¿quieres comprar?              └ Psst… ¿quieres comprar?           (1.2, tarea)
 *          ├ ¡Atrapado con las manos...!          ├ ¡Atrapado con las manos...!   (1.2, meta)
 *          ├ ¡Viva la revolución!  (oculto)       ├ ¡Viva la revolución!          (1.2, tarea, oculto)
 *          └ Conocimiento prohibido (oculto)      └ Conocimiento prohibido        (1.2, desafío, oculto)
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

        illegalTree(obtainRubyVanilla, false);

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

        illegalTree(obtainRubyModTab, true);
    }

    // =========================================================================
    // 1.2: el mercado negro (las dos versiones usan esto)
    // =========================================================================

    /** Los 4 logros de la 1.2, colgando de "¿Volviste?". modTab = qué versión generar. */
    private void illegalTree(AdvancementHolder parent, boolean modTab) {
        ModAdvancementTabCondition condition = modTab ? MOD_TAB : VANILLA_TAB;

        // PSST… ¿QUIERES COMPRAR? — tarea. Tu primer trato ilegal (lo da IllegalVillagerEvents).
        AdvancementHolder wannaBuy = fromCode(ModAdvancements.CRITERION_DONE)
                .parent(parent)
                .display(ModBlocks.RUBY_BLOCK.get().asItem(),
                        Component.translatable("advancements.travelyourearth.wanna_buy.title"),
                        Component.translatable("advancements.travelyourearth.wanna_buy.description"),
                        AdvancementType.TASK, true, true, false)
                .save(output, id(ModAdvancements.WANNA_BUY, modTab), condition);

        // ¡ATRAPADO CON LAS MANOS EN LA MASA! — meta. Atrapar a un buscado en un bote (IllegalChaseEvents).
        fromCode(ModAdvancements.CRITERION_DONE)
                .parent(wannaBuy)
                .display(Items.OAK_BOAT,
                        Component.translatable("advancements.travelyourearth.caught_red_handed.title"),
                        Component.translatable("advancements.travelyourearth.caught_red_handed.description"),
                        AdvancementType.GOAL, true, true, false)
                .save(output, id(ModAdvancements.CAUGHT_RED_HANDED, modTab), condition);

        // ¡VIVA LA REVOLUCIÓN! — tarea OCULTA. Un estandarte con el diseño Rebelión (ModAdvancementEvents).
        fromCode(ModAdvancements.CRITERION_DONE)
                .parent(wannaBuy)
                .display(ModItems.REBELLION_BANNER_PATTERN.get(),
                        Component.translatable("advancements.travelyourearth.viva_la_revolucion.title"),
                        Component.translatable("advancements.travelyourearth.viva_la_revolucion.description"),
                        AdvancementType.TASK, true, true, true)
                .save(output, id(ModAdvancements.VIVA_LA_REVOLUCION, modTab), condition);

        // CONOCIMIENTO PROHIBIDO — desafío OCULTO (+100 XP, como los desafíos vanilla).
        // 3 criterios, uno por libro: no hace falta tenerlos a la vez (ModAdvancementEvents).
        fromCode(ModAdvancements.CRITERION_BANISHMENT, ModAdvancements.CRITERION_HARPOON, ModAdvancements.CRITERION_COWARDICE)
                .parent(wannaBuy)
                .display(Items.ENCHANTED_BOOK,
                        Component.translatable("advancements.travelyourearth.forbidden_knowledge.title"),
                        Component.translatable("advancements.travelyourearth.forbidden_knowledge.description"),
                        AdvancementType.CHALLENGE, true, true, true)
                .rewards(AdvancementRewards.Builder.experience(100))
                .save(output, id(ModAdvancements.FORBIDDEN_KNOWLEDGE, modTab), condition);
    }

    /** Logro con criterios "impossible": no se cumplen solos, se dan desde el código. */
    private static Advancement.Builder fromCode(String... criteria) {
        Advancement.Builder builder = Advancement.Builder.advancement();
        for (String criterion : criteria) {
            builder.addCriterion(criterion, CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance()));
        }
        return builder;
    }

    private static Identifier id(String name, boolean modTab) {
        return modTab ? ModAdvancements.modTab(name) : ModAdvancements.adventure(name);
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