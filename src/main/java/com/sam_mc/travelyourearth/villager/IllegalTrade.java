package com.sam_mc.travelyourearth.villager;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.trading.MerchantOffer;

import java.util.Optional;

/**
 * One illegal trade of a profession's list.
 *
 * @param name             short name, only for debug messages
 * @param oncePerVillager  true = it disappears FOREVER once it's sold out (never restocks),
 *                         e.g. the Eye of Ender (1 time) or the screaming goat horn (2 times)
 * @param factory          builds the actual offer (price, item, rarity). Some trades are random
 *                         (random potion, random template, random book level...), so price and
 *                         rarity are decided when the offer is created.
 */
public record IllegalTrade(String name, boolean oncePerVillager, Factory factory) {

    /** The offer created for a villager + its rarity (XP cost). */
    public record Deal(MerchantOffer offer, IllegalRarity rarity) {}

    @FunctionalInterface
    public interface Factory {
        /** Empty if it can't be created here (e.g. a map with no structure nearby). */
        Optional<Deal> create(ServerLevel level, BlockPos origin);
    }
}