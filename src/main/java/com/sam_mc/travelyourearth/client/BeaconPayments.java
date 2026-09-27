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
 * Lista ordenada de los pagos del faro (tag #minecraft:beacon_payment_items).
 * La usan el panel de materiales (el libro) y las páginas de la pestaña de minerales,
 * así los dos muestran lo mismo y en el mismo orden, incluidos los ítems de otros mods.
 *
 * Orden:  1. Gemas     (diamante, esmeralda, rubí, y gemas de otros mods: #c:gems)
 *         2. Lingotes  (netherita, oro, hierro, y lingotes de otros mods: #c:ingots)
 *         3. Otros     (cualquier otro pago de otros mods)
 */
public final class BeaconPayments {

    public static final int ITEMS_PER_PAGE = 3; // huecos entre los separadores de la pestaña

    private static final List<Item> GEMS = List.of(Items.DIAMOND, Items.EMERALD);
    private static final List<Item> INGOTS = List.of(Items.NETHERITE_INGOT, Items.GOLD_INGOT, Items.IRON_INGOT);

    private BeaconPayments() {}

    /** Todos los pagos en orden: gemas, lingotes, otros. */
    public static List<Item> ordered() {
        List<Item> result = new ArrayList<>();
        for (List<Item> group : groups()) result.addAll(group);
        return result;
    }

    /**
     * Páginas de 3 para la pestaña de minerales. Cada grupo se reparte por separado,
     * así una página nunca mezcla gemas con lingotes.
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

    private static List<List<Item>> groups() {
        List<Item> gems = new ArrayList<>();
        List<Item> ingots = new ArrayList<>();
        List<Item> others = new ArrayList<>();

        // Primero los conocidos, en su orden
        for (Item item : GEMS) addIfPayment(gems, item);
        addIfPayment(gems, ModItems.RUBY.get());
        for (Item item : INGOTS) addIfPayment(ingots, item);

        // Después los de otros mods, según sus tags
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
