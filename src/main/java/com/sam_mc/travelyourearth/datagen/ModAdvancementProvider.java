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
 * Mod advancements. TWO versions of each one are generated, with conditions,
 * and the "modAdvancementTab" option (LOCAL config) decides which one is loaded:
 *
 *   OFF (default)                          ON
 *   Adventure                              Travel Your Earth   <- own tab (root, icon: earth block)
 *    └ You're Back?                         └ You're Back?
 *       └ Fireproof                            └ Fireproof
 *
 *   IDs:  travelyourearth:adventure/...    travelyourearth:ruby/...
 */
public class ModAdvancementProvider extends AdvancementSubProvider {

    // =========================================================================
    // Constants
    // =========================================================================

    /**
     * Background of the own tab: Earth dirt (textures/block/earth_dirt.png).
     * AdvancementTabMixin recognizes it and puts Earth grass on the top row.
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
        // VERSION 1: in the vanilla "Adventure" tab  (option OFF)
        // =====================================================================
        // Reference to the vanilla "Adventure" advancement. It doesn't create any file, it's only used as a parent.
        AdvancementHolder adventureRoot = Advancement.Builder.advancement()
                .build(Identifier.withDefaultNamespace("adventure/root"));

        AdvancementHolder obtainRubyVanilla = obtainRuby()
                .parent(adventureRoot)
                .display(
                        ModItems.RUBY.get(),
                        Component.translatable("advancements.travelyourearth.obtain_ruby.title"),
                        Component.translatable("advancements.travelyourearth.obtain_ruby.description"),
                        AdvancementType.TASK,
                        true,   // toast in the top right corner
                        true,   // announce in chat
                        false)  // not hidden
                .save(output, Identifier.fromNamespaceAndPath(TravelYourEarth.MODID, "adventure/obtain_ruby"), VANILLA_TAB);

        fireproof()
                .parent(obtainRubyVanilla)
                .save(output, RubyArmorFireHandler.FIREPROOF_ADVANCEMENT, VANILLA_TAB);

        // =====================================================================
        // VERSION 2: the mod's own tab  (option ON)
        // =====================================================================
        // ROOT "Travel Your Earth": gives the tab its name, icon (earth block) and background.
        // It's earned at the same time as "You're Back?" (when you get a ruby), but SILENTLY
        // (no toast or chat), like vanilla roots. This way the tab appears with your first ruby.
        AdvancementHolder modTabRoot = obtainRuby()
                .rootDisplay(
                        ModBlocks.EARTH_BLOCK.get().asItem(), // the icon must be an ITEM
                        Component.translatable("advancements.travelyourearth.root.title"),
                        Component.translatable("advancements.travelyourearth.root.description"),
                        MOD_TAB_BACKGROUND, // rootDisplay + background = root of a new tab
                        AdvancementType.TASK,
                        false,  // no toast
                        false,  // no chat message
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
    // Parts shared by both versions
    // =========================================================================

    /** YOU'RE BACK? — a nod to Minecraft Earth, where the ruby was the currency. */
    private static Advancement.Builder obtainRuby() {
        return Advancement.Builder.advancement()
                .addCriterion("has_ruby", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.RUBY.get()));
    }

    /**
     * FIREPROOF — "impossible" criterion: it's never completed on its own.
     * RubyArmorFireHandler grants it when the ruby armor puts out the player's fire.
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