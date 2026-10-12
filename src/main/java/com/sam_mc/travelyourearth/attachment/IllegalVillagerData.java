package com.sam_mc.travelyourearth.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;

/**
 * Data stored on every villager (and zombie villager) to know if it's an illegal trader.
 *
 * @param illegal        true if this villager is an illegal trader (the chance depends on its village type)
 * @param secretUnlocked true once the secret phase has been unlocked (master level)
 * @param slots          one entry per illegal offer it has (2), in the same order as in its trade list
 * @param lastRestock    game time (ticks) of the last restock of its illegal trades (slow: every 3 days)
 * @param rolled         true once the "is it illegal?" chance was rolled (when it's an ADULT: babies wait)
 */
public record IllegalVillagerData(boolean illegal, boolean secretUnlocked, List<IllegalSlot> slots, long lastRestock,
                                  boolean rolled) {

    /** Default of every villager: not rolled yet (babies keep this until they grow up). */
    public static final IllegalVillagerData NOT_ROLLED = new IllegalVillagerData(false, false, List.of(), 0L, false);

    /** A normal (legal) villager (already rolled, or caught). */
    public static final IllegalVillagerData LEGAL = new IllegalVillagerData(false, false, List.of(), 0L, true);

    /** A freshly rolled illegal villager (secret phase not unlocked yet). */
    public static final IllegalVillagerData NEW_ILLEGAL = new IllegalVillagerData(true, false, List.of(), 0L, true);

    /** Saves the data in the world (save file). */
    public static final MapCodec<IllegalVillagerData> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.fieldOf("illegal").forGetter(IllegalVillagerData::illegal),
            Codec.BOOL.optionalFieldOf("secret_unlocked", false).forGetter(IllegalVillagerData::secretUnlocked),
            IllegalSlot.CODEC.listOf().optionalFieldOf("slots", List.of()).forGetter(IllegalVillagerData::slots),
            Codec.LONG.optionalFieldOf("last_restock", 0L).forGetter(IllegalVillagerData::lastRestock),
            // Missing in old saves and in /summon commands -> true (already decided: don't roll again)
            Codec.BOOL.optionalFieldOf("rolled", true).forGetter(IllegalVillagerData::rolled)
    ).apply(instance, IllegalVillagerData::new));

    /** Sends the data from the server to the client (ruby badge). */
    public static final StreamCodec<ByteBuf, IllegalVillagerData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, IllegalVillagerData::illegal,
            ByteBufCodecs.BOOL, IllegalVillagerData::secretUnlocked,
            IllegalSlot.STREAM_CODEC.apply(ByteBufCodecs.list()), IllegalVillagerData::slots,
            ByteBufCodecs.VAR_LONG, IllegalVillagerData::lastRestock,
            ByteBufCodecs.BOOL, IllegalVillagerData::rolled,
            IllegalVillagerData::new
    );

    /** Returns a copy with the secret phase unlocked, its illegal offers' slots and the restock clock started. */
    public IllegalVillagerData withSecretUnlocked(List<IllegalSlot> newSlots, long now) {
        return new IllegalVillagerData(this.illegal, true, List.copyOf(newSlots), now, this.rolled);
    }

    /** Returns a copy with the slot at that index replaced. */
    public IllegalVillagerData withSlot(int index, IllegalSlot slot) {
        List<IllegalSlot> copy = new ArrayList<>(this.slots);
        if (index >= 0 && index < copy.size()) copy.set(index, slot);
        return new IllegalVillagerData(this.illegal, this.secretUnlocked, List.copyOf(copy), this.lastRestock, this.rolled);
    }

    /** Returns a copy without the slot at that index (an offer bought "once per villager"). */
    public IllegalVillagerData withoutSlot(int index) {
        List<IllegalSlot> copy = new ArrayList<>(this.slots);
        if (index >= 0 && index < copy.size()) copy.remove(index);
        return new IllegalVillagerData(this.illegal, this.secretUnlocked, List.copyOf(copy), this.lastRestock, this.rolled);
    }

    /** Returns a copy with a new restock time. */
    public IllegalVillagerData withLastRestock(long time) {
        return new IllegalVillagerData(this.illegal, this.secretUnlocked, this.slots, time, this.rolled);
    }
}