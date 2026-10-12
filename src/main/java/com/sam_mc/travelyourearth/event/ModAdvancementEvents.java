package com.sam_mc.travelyourearth.event;

import com.sam_mc.travelyourearth.TravelYourEarth;
import com.sam_mc.travelyourearth.advancement.ModAdvancements;
import com.sam_mc.travelyourearth.banner.ModBannerPatterns;
import com.sam_mc.travelyourearth.enchantment.ModEnchantments;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * 1.2 advancements that depend on what you HAVE in your inventory (checked once per second):
 *   - Viva la Revolución: a banner with the Rebellion pattern (made in the loom).
 *   - Forbidden Knowledge: an enchanted book of Banishment, of Harpoon and of Cowardice
 *     (each book is its own criterion, so you don't need the 3 at the same time).
 * The other two (Psst, Wanna Buy? and Caught Red-Handed) are given in IllegalVillagerEvents
 * and IllegalChaseEvents.
 */
@EventBusSubscriber(modid = TravelYourEarth.MODID)
public class ModAdvancementEvents {

    private ModAdvancementEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.tickCount % 20 != 0) return;

        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty()) continue;

            // Viva la Revolución: any banner (or shield) with the Rebellion pattern
            BannerPatternLayers layers = stack.get(DataComponents.BANNER_PATTERNS);
            if (layers != null) {
                for (BannerPatternLayers.Layer layer : layers.layers()) {
                    if (layer.pattern().is(ModBannerPatterns.REBELLION)) {
                        ModAdvancements.award(player, ModAdvancements.VIVA_LA_REVOLUCION);
                    }
                }
            }

            // Forbidden Knowledge: the 3 illegal books
            ItemEnchantments stored = stack.get(DataComponents.STORED_ENCHANTMENTS);
            if (stored != null && !stored.isEmpty()) {
                checkBook(player, stored, ModEnchantments.BANISHMENT, ModAdvancements.CRITERION_BANISHMENT);
                checkBook(player, stored, ModEnchantments.HARPOON, ModAdvancements.CRITERION_HARPOON);
                checkBook(player, stored, ModEnchantments.COWARDICE, ModAdvancements.CRITERION_COWARDICE);
            }
        }
    }

    private static void checkBook(ServerPlayer player, ItemEnchantments stored,
                                  ResourceKey<Enchantment> enchantment, String criterion) {
        for (Holder<Enchantment> holder : stored.keySet()) {
            if (holder.is(enchantment)) {
                ModAdvancements.award(player, ModAdvancements.FORBIDDEN_KNOWLEDGE, criterion);
                return;
            }
        }
    }
}