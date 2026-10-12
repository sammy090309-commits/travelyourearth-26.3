package com.sam_mc.travelyourearth.attachment;

import com.sam_mc.travelyourearth.TravelYourEarth;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

/** Data attachments of the mod (extra data saved on entities, chunks, etc.). */
public class ModAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, TravelYourEarth.MODID);

    /**
     * Illegal trader data of a villager.
     * - serialize: it's saved in the world, so a villager stays illegal after reloading.
     * - sync: it's sent to the client (needed later for the ruby badge).
     */
    public static final Supplier<AttachmentType<IllegalVillagerData>> ILLEGAL_VILLAGER = ATTACHMENT_TYPES.register(
            "illegal_villager",
            () -> AttachmentType.builder(() -> IllegalVillagerData.NOT_ROLLED)
                    .serialize(IllegalVillagerData.MAP_CODEC)
                    .sync(IllegalVillagerData.STREAM_CODEC)
                    .build()
    );

    /**
     * The player the illegal villager is showing its secret to right now (ruby badge + illegal trade
     * + illegal item in its hand). Decided ONLY on the server (with the 1-2 s delay) and synced
     * to the clients, so the badge, the trade and the item always change at the same time.
     * Not saved in the world (no serialize): it's recalculated every few ticks.
     */
    public static final Supplier<AttachmentType<Optional<UUID>>> SECRET_REVEALED_TO = ATTACHMENT_TYPES.register(
            "secret_revealed_to",
            () -> AttachmentType.<Optional<UUID>>builder(Optional::empty)
                    .sync(ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC))
                    .build()
    );

    /**
     * "Police" data of an illegal villager: who knows its secret, if it's scared (seen trading)
     * or WANTED (someone rang the bell on it). Saved in the world, NOT sent to the client.
     */
    public static final Supplier<AttachmentType<IllegalChaseData>> ILLEGAL_CHASE = ATTACHMENT_TYPES.register(
            "illegal_chase",
            () -> AttachmentType.builder(() -> IllegalChaseData.NONE)
                    .serialize(IllegalChaseData.MAP_CODEC)
                    .build()
    );

    /**
     * True while an illegal villager celebrates the illagers' victory in a raid: it shows its ruby
     * badge to EVERYONE (no secrets anymore, it's proud). Synced to the clients, not saved.
     */
    public static final Supplier<AttachmentType<Boolean>> PROUD_TRAFFICKER = ATTACHMENT_TYPES.register(
            "proud_trafficker",
            () -> AttachmentType.builder(() -> Boolean.FALSE)
                    .sync(ByteBufCodecs.BOOL)
                    .build()
    );

    /**
     * Savanna villagers: how many of their trades were already checked to use rubies
     * (new trades get added at the end of the list when they level up). Saved in the world.
     */
    public static final Supplier<AttachmentType<Integer>> SAVANNA_TRADES_CHECKED = ATTACHMENT_TYPES.register(
            "savanna_trades_checked",
            () -> AttachmentType.builder(() -> 0)
                    .serialize(com.mojang.serialization.Codec.INT.fieldOf("checked"))
                    .build()
    );

    /**
     * Wandering traders: 0 = not decided yet, 1 = normal, 2 = SMUGGLER (has ruby trades, dark buttons).
     * Saved in the world and synced to the clients (the trade screen needs it for the dark buttons).
     */
    public static final Supplier<AttachmentType<Integer>> SMUGGLER_TRADER = ATTACHMENT_TYPES.register(
            "smuggler_trader",
            () -> AttachmentType.builder(() -> 0)
                    .serialize(com.mojang.serialization.Codec.INT.fieldOf("state"))
                    .sync(ByteBufCodecs.VAR_INT)
                    .build()
    );

    /**
     * Decorated pots (1.2.1): the mobs stored inside, as their spawn egg (with the count = how many).
     * Kept APART from the pot's normal item, so hoppers can't take them out: they only come out
     * (alive) when the pot breaks. Saved in the world, not synced (the client doesn't need it).
     */
    public static final Supplier<AttachmentType<net.minecraft.world.item.ItemStack>> POT_MOBS = ATTACHMENT_TYPES.register(
            "pot_mobs",
            () -> AttachmentType.builder(() -> net.minecraft.world.item.ItemStack.EMPTY)
                    .serialize(net.minecraft.world.item.ItemStack.OPTIONAL_CODEC.fieldOf("mobs"))
                    .build()
    );

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}