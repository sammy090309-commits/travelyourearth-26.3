package com.sam_mc.travelyourearth.event;

import com.sam_mc.travelyourearth.TravelYourEarth;
import com.sam_mc.travelyourearth.attachment.ModAttachments;
import com.sam_mc.travelyourearth.villager.WanderingSmugglerTrades;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffers;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * Wandering traders (1.2): half of them are smugglers and bring 2 ruby trades at the END of
 * their list. They keep their name and everything else; the only visible change is that their
 * RUBY trades have dark buttons (see MerchantScreenMixin). The emerald ones stay vanilla.
 *
 * Decided ONCE per trader (saved + synced in SMUGGLER_TRADER):
 *   0 = not decided yet, 1 = normal trader, 2 = smuggler (ruby trades already added).
 * It's decided on its first tick, and also right before you open its menu (HIGHEST), so the
 * ruby trades are always there the first time you see the list.
 */
@EventBusSubscriber(modid = TravelYourEarth.MODID)
public class WanderingSmugglerEvents {

    public static final int NOT_DECIDED = 0;
    public static final int NORMAL = 1;
    public static final int SMUGGLER = 2;

    private WanderingSmugglerEvents() {}

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        decide(event.getTarget());
    }

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        decide(event.getEntity());
    }

    /** True if this entity is a wandering trader that turned out to be a smuggler (works on the client too). */
    public static boolean isSmuggler(Entity entity) {
        return isWanderingTrader(entity)
                && entity.getData(ModAttachments.SMUGGLER_TRADER) == SMUGGLER;
    }

    /**
     * True if the entity is a wandering trader. Checked by its id ("minecraft:wandering_trader"),
     * so it doesn't depend on the name of the EntityType constant / class in 26.3.
     */
    public static boolean isWanderingTrader(Entity entity) {
        return WANDERING_TRADER_ID.equals(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()));
    }

    private static final Identifier WANDERING_TRADER_ID = Identifier.withDefaultNamespace("wandering_trader");

    private static void decide(Entity entity) {
        if (!isWanderingTrader(entity)) return;
        if (!(entity.level() instanceof ServerLevel level)) return;
        if (!(entity instanceof Merchant trader)) return;
        if (entity.getData(ModAttachments.SMUGGLER_TRADER) != NOT_DECIDED) return;

        if (entity.getRandom().nextFloat() >= WanderingSmugglerTrades.SMUGGLER_CHANCE) {
            entity.setData(ModAttachments.SMUGGLER_TRADER, NORMAL);
            return;
        }
        // getOffers() makes the vanilla trades first, so ours go at the END of the list
        MerchantOffers offers = trader.getOffers();
        offers.addAll(WanderingSmugglerTrades.pick(level));
        entity.setData(ModAttachments.SMUGGLER_TRADER, SMUGGLER);
    }
}