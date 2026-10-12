package com.sam_mc.travelyourearth.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Info about ONE illegal offer of a villager (it has 2). The slots are in the same order as the
 * illegal offers in the villager's trade list.
 *
 * @param levelCost       XP levels it costs (from its rarity: 2 / 4 / 6)
 * @param oncePerVillager true = removed after it's sold out
 * @param basePrice       starting price (rubies or ruby blocks). 0 = unknown (old villager): price never changes
 * @param timesBought     how many times it has been bought: every purchase raises the price 5% (it never goes down)
 */
public record IllegalSlot(int levelCost, boolean oncePerVillager, int basePrice, int timesBought) {

    public static final Codec<IllegalSlot> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("level_cost").forGetter(IllegalSlot::levelCost),
            Codec.BOOL.optionalFieldOf("once_per_villager", false).forGetter(IllegalSlot::oncePerVillager),
            Codec.INT.optionalFieldOf("base_price", 0).forGetter(IllegalSlot::basePrice),
            Codec.INT.optionalFieldOf("times_bought", 0).forGetter(IllegalSlot::timesBought)
    ).apply(instance, IllegalSlot::new));

    public static final StreamCodec<ByteBuf, IllegalSlot> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, IllegalSlot::levelCost,
            ByteBufCodecs.BOOL, IllegalSlot::oncePerVillager,
            ByteBufCodecs.VAR_INT, IllegalSlot::basePrice,
            ByteBufCodecs.VAR_INT, IllegalSlot::timesBought,
            IllegalSlot::new
    );

    /** Copy with one more purchase. */
    public IllegalSlot bought() {
        return new IllegalSlot(this.levelCost, this.oncePerVillager, this.basePrice, this.timesBought + 1);
    }
}