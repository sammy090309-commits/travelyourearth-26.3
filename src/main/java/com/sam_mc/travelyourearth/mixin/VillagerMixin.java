package com.sam_mc.travelyourearth.mixin;

import com.sam_mc.travelyourearth.attachment.ModAttachments;
import com.sam_mc.travelyourearth.villager.IllegalVillagers;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Changes to vanilla villagers for the illegal ones:
 * - Illegal prices never go down (no reputation / Hero of the Village discounts).
 * - Illegal villagers don't give Hero of the Village discounts on ANY of their trades.
 * - Illegal villagers never have babies (loners).
 * - During a raid, illegal villagers don't sweat (they're on the illagers' side!).
 */
@Mixin(Villager.class)
public abstract class VillagerMixin {

    /**
     * When you open the trade screen, vanilla (Villager.updateSpecialPrices) gives discounts:
     * good reputation and Hero of the Village. After that, we remove any discount from the illegal offers.
     * (Price RAISES are fine: those are done by us.)
     */
    @Inject(method = "updateSpecialPrices", at = @At("TAIL"))
    private void travelyourearth$noIllegalDiscounts(Player player, CallbackInfo ci) {
        Villager villager = (Villager) (Object) this;

        // Illegal villagers ignore the Hero of the Village: we redo the discounts with ONLY
        // the reputation part (same formula as vanilla), without the Hero discount
        if (IllegalVillagers.isIllegalActive(villager)) {
            int reputation = villager.getPlayerReputation(player);
            for (MerchantOffer offer : villager.getOffers()) {
                offer.resetSpecialPriceDiff();
                if (reputation != 0 && !IllegalVillagers.isIllegalOffer(offer)) {
                    offer.addToSpecialPriceDiff(-Mth.floor(reputation * offer.getPriceMultiplier()));
                }
            }
        }

        // Illegal offers never get cheaper (from any villager)
        for (MerchantOffer offer : villager.getOffers()) {
            if (IllegalVillagers.isIllegalOffer(offer) && offer.getSpecialPriceDiff() < 0) {
                offer.resetSpecialPriceDiff();
            }
        }
    }

    /**
     * Entity event 42 = the worried sweat drops (splash particles) villagers show during a raid.
     * Illegal villagers aren't worried at all: no sweat. (Their happy particles come from CelebrationEvents.)
     * This runs on the client: the "illegal" data is synced, so the client knows.
     */
    @Inject(method = "handleEntityEvent", at = @At("HEAD"), cancellable = true)
    private void travelyourearth$noRaidSweat(byte id, CallbackInfo ci) {
        Villager villager = (Villager) (Object) this;
        if (id == 42 && villager.hasData(ModAttachments.ILLEGAL_VILLAGER) && IllegalVillagers.isIllegal(villager)) {
            ci.cancel();
        }
    }

    /** Illegal villagers are loners: they never have babies. */
    @Inject(method = "canBreed", at = @At("HEAD"), cancellable = true)
    private void travelyourearth$noBabies(org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir) {
        Villager villager = (Villager) (Object) this;
        if (villager.hasData(ModAttachments.ILLEGAL_VILLAGER) && IllegalVillagers.isIllegal(villager)) {
            cir.setReturnValue(false);
        }
    }
}