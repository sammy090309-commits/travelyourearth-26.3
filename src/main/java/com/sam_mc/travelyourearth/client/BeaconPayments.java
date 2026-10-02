package com.sam_mc.travelyourearth.client;

import com.sam_mc.travelyourearth.item.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;

import java.util.ArrayList;
import java.util.List;

/**
 * Ordered list of beacon payments (tag #minecraft:beacon_payment_items).
 * Used by the materials panel (the book) and by the pages of the minerals tab,
 * so both show the same items in the same order, including items from other mods.
 *
 * Order:  1. Gems     (diamond, emerald, ruby, and gems from other mods: #c:gems)
 *         2. Ingots   (netherite, gold, iron, and ingots from other mods: #c:ingots)
 *         3. Others   (any other payment from other mods)
 */
public final class BeaconPayments {

    // =========================================================================
    // Constants
    // =========================================================================

    public static final int ITEMS_PER_PAGE = 3; // slots between the separators of the tab

    private static final List<Item> GEMS = List.of(Items.DIAMOND, Items.EMERALD);
    private static final List<Item> INGOTS = List.of(Items.NETHERITE_INGOT, Items.GOLD_INGOT, Items.IRON_INGOT);

    private BeaconPayments() {}

    // =========================================================================
    // Public API
    // =========================================================================

    /** All payments in order: gems, ingots, others. */
    public static List<Item> ordered() {
        List<Item> result = new ArrayList<>();
        for (List<Item> group : groups()) result.addAll(group);
        return result;
    }

    /**
     * Pages of 3 for the minerals tab. Each group is split separately,
     * so a page never mixes gems with ingots.
     */
    public static List<List<Item>> pages() {
        List<List<Item>> pages = new ArrayList<>();
        for (List<Item> group : groups()) {
            for (int i = 0; i < group.size(); i += ITEMS_PER_PAGE) {
                pages.add(group.subList(i, Math.min(i + ITEMS_PER_PAGE, group.size())));
            }
        }
        return pages;
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private static List<List<Item>> groups() {
        List<Item> gems = new ArrayList<>();
        List<Item> ingots = new ArrayList<>();
        List<Item> others = new ArrayList<>();

        // Known items first, in their order
        for (Item item : GEMS) addIfPayment(gems, item);
        addIfPayment(gems, ModItems.RUBY.get());
        for (Item item : INGOTS) addIfPayment(ingots, item);

        // Then items from other mods, based on their tags
        for (Item item : BuiltInRegistries.ITEM) {
            if (!isPayment(item) || gems.contains(item) || ingots.contains(item)) continue;
            ItemStack stack = new ItemStack(item);
            if (stack.is(Tags.Items.GEMS)) gems.add(item);
            else if (stack.is(Tags.Items.INGOTS)) ingots.add(item);
            else others.add(item);
        }

        List<List<Item>> groups = new ArrayList<>();
        if (!gems.isEmpty()) groups.add(gems);
        if (!ingots.isEmpty()) groups.add(ingots);
        if (!others.isEmpty()) groups.add(others);
        return groups;
    }

    private static boolean isPayment(Item item) {
        return new ItemStack(item).is(ItemTags.BEACON_PAYMENT_ITEMS);
    }

    private static void addIfPayment(List<Item> list, Item item) {
        if (isPayment(item) && !list.contains(item)) list.add(item);
    }
}