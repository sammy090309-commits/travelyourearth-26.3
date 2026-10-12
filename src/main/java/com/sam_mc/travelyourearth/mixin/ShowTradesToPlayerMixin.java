package com.sam_mc.travelyourearth.mixin;

import com.sam_mc.travelyourearth.villager.IllegalVillagers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.ShowTradesToPlayer;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Vanilla "show trades": when you hold an item a villager accepts as payment, it shows
 * in its hand what it would sell you for it. The illegal trade is paid with rubies, so
 * vanilla would show it even in front of witnesses.
 *
 * This mixin makes the illegal item in the villager's hand follow the SAME rules as the
 * ruby badge and the illegal trades (IllegalVillagers.isRevealedTo).
 */
@Mixin(ShowTradesToPlayer.class)
public abstract class ShowTradesToPlayerMixin {

    @Shadow @Final private List<ItemStack> displayItems;
    @Shadow private @Nullable ItemStack playerItemStack;

    /** After vanilla picks the items to show: remove the illegal ones if the secret can't be shown. */
    @Inject(method = "updateDisplayItems", at = @At("TAIL"))
    private void travelyourearth$hideIllegalTrades(Villager villager, CallbackInfo ci) {
        List<MerchantOffer> illegal = IllegalVillagers.illegalOffers(villager);
        if (illegal.isEmpty()) return;

        Player player = villager.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET)
                .filter(target -> target instanceof Player)
                .map(target -> (Player) target)
                .orElse(null);

        // Same moment as the ruby badge and the illegal trades (decided by the server, with the delay)
        boolean canShow = player != null
                && IllegalVillagers.isIllegalActive(villager)
                && IllegalVillagers.isRevealedTo(villager, player);
        if (canShow) return;

        for (MerchantOffer offer : illegal) {
            ItemStack illegalResult = offer.getResult();
            this.displayItems.removeIf(stack -> ItemStack.isSameItemSameComponents(stack, illegalResult));
        }
    }

    /**
     * While the villager is drinking (Invisibility potion or milk at night, IllegalNightEvents),
     * vanilla must not put trade items in its hand: that would interrupt the drink.
     */
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void travelyourearth$dontInterruptDrinkTick(ServerLevel level, Villager body, long timestamp, CallbackInfo ci) {
        if (body.isUsingItem()) ci.cancel();
    }

    /** Same when the behavior stops: vanilla empties the hand, which would take the potion away. */
    @Inject(method = "stop", at = @At("HEAD"), cancellable = true)
    private void travelyourearth$dontInterruptDrinkStop(ServerLevel level, Villager body, long timestamp, CallbackInfo ci) {
        if (body.isUsingItem()) ci.cancel();
    }

    /**
     * Vanilla only recalculates the items when you change the item in your hand.
     * While you hold a ruby (or a ruby block), recalculate every 5 ticks, so the item appears/disappears
     * right when the ruby badge changes.
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void travelyourearth$refreshWithRuby(ServerLevel level, Villager body, long timestamp, CallbackInfo ci) {
        if (timestamp % 5L == 0L
                && this.playerItemStack != null
                && IllegalVillagers.isRubyMoney(this.playerItemStack)
                && IllegalVillagers.data(body).secretUnlocked()) {
            this.playerItemStack = null; // forces vanilla to pick the items again this tick
        }
    }
}