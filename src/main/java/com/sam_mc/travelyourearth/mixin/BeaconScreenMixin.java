package com.sam_mc.travelyourearth.mixin;

import com.sam_mc.travelyourearth.client.BeaconPayments;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.BeaconScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Beacon screen: the payment items shown at the bottom of the GUI rotate in pages of 3
 * (gems, ingots, and items from other mods), changing every 5 seconds.
 * The pages come from BeaconPayments, the same list used by BeaconPaymentPanel.
 */
@Mixin(BeaconScreen.class)
public abstract class BeaconScreenMixin {

    // =========================================================================
    // Constants and state
    // =========================================================================

    @Unique
    private static final Identifier BEACON_CUSTOM_TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/beacon.png");

    @Unique
    private static final int PAGE_TICKS = 100; // 5 seconds per page

    @Unique
    private int travelyourearth$timer = 0;
    @Unique
    private int travelyourearth$page = 0;
    @Unique
    private List<List<Item>> travelyourearth$pages = null; // calculated when the beacon opens

    @Unique
    private List<List<Item>> travelyourearth$getPages() {
        if (this.travelyourearth$pages == null) {
            // Pages of 3 from the payment tag: gems, ingots, and items from other mods
            this.travelyourearth$pages = BeaconPayments.pages();
        }
        return this.travelyourearth$pages;
    }

    // =========================================================================
    // Injections
    // =========================================================================

    // 1. Timer that changes the page every 5 seconds
    @Inject(method = "containerTick", at = @At("HEAD"))
    private void travelyourearth$onContainerTick(CallbackInfo ci) {
        this.travelyourearth$timer++;

        if (this.travelyourearth$timer >= PAGE_TICKS) {
            this.travelyourearth$timer = 0;
            int count = this.travelyourearth$getPages().size();
            if (count > 0) {
                this.travelyourearth$page = (this.travelyourearth$page + 1) % count;
            }
        }
    }

    // 2. Replaces the background, darkens the outer screen and cancels the original drawing
    @Inject(method = "extractBackground", at = @At("HEAD"), cancellable = true)
    private void travelyourearth$overrideBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        BeaconScreen screen = (BeaconScreen) (Object) this;

        // Cancel right away so Minecraft does NOT run its default drawing
        ci.cancel();

        // Darken the outer screen (semi-transparent overlay over the whole window)
        graphics.fill(0, 0, screen.width, screen.height, -1072689136);

        // REAL position of the window (it changes when the materials panel opens)
        AbstractContainerScreenAccessorMixin pos = (AbstractContainerScreenAccessorMixin) screen;
        int xo = pos.travelyourearth$getLeftPos();
        int yo = pos.travelyourearth$getTopPos();

        // Draw the beacon texture
        graphics.blit(RenderPipelines.GUI_TEXTURED, BEACON_CUSTOM_TEXTURE, xo, yo, 0.0F, 0.0F, 230, 219, 256, 256);

        // Draw the active page (up to 3 items, between the separators)
        List<List<Item>> pages = this.travelyourearth$getPages();
        if (pages.isEmpty()) return;
        List<Item> page = pages.get(this.travelyourearth$page % pages.size());
        for (int i = 0; i < page.size(); i++) {
            graphics.item(new ItemStack(page.get(i)), xo + 41 + i * 22, yo + 108);
        }
    }
}