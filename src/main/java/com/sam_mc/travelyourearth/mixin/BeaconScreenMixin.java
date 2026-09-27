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

@Mixin(BeaconScreen.class)
public abstract class BeaconScreenMixin {

    @Unique
    private static final Identifier BEACON_CUSTOM_TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/beacon.png");

    @Unique
    private static final int PAGE_TICKS = 100; // 5 segundos por página

    @Unique
    private int travelyourearth$timer = 0;
    @Unique
    private int travelyourearth$page = 0;
    @Unique
    private List<List<Item>> travelyourearth$pages = null; // se calcula al abrir el faro

    @Unique
    private List<List<Item>> travelyourearth$getPages() {
        if (this.travelyourearth$pages == null) {
            // Páginas de 3 desde el tag de pagos: gemas, lingotes, y lo de otros mods
            this.travelyourearth$pages = BeaconPayments.pages();
        }
        return this.travelyourearth$pages;
    }

    // 1. Contador que cambia de página cada 5 segundos
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

    // 2. Sobrescribe el fondo, oscurece la pantalla exterior y cancela el dibujado original
    @Inject(method = "extractBackground", at = @At("HEAD"), cancellable = true)
    private void travelyourearth$overrideBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        BeaconScreen screen = (BeaconScreen) (Object) this;

        // Cancela de inmediato para que Minecraft NO ejecute sus gráficos por defecto
        ci.cancel();

        // Oscurece la pantalla de afuera (velo semitransparente en toda la ventana)
        graphics.fill(0, 0, screen.width, screen.height, -1072689136);

        // Posición REAL de la ventana (cambia cuando se abre el panel de materiales)
        AbstractContainerScreenAccessorMixin pos = (AbstractContainerScreenAccessorMixin) screen;
        int xo = pos.travelyourearth$getLeftPos();
        int yo = pos.travelyourearth$getTopPos();

        // Renderiza la textura de la baliza
        graphics.blit(RenderPipelines.GUI_TEXTURED, BEACON_CUSTOM_TEXTURE, xo, yo, 0.0F, 0.0F, 230, 219, 256, 256);

        // Renderiza la página activa (hasta 3 ítems, entre los separadores)
        List<List<Item>> pages = this.travelyourearth$getPages();
        if (pages.isEmpty()) return;
        List<Item> page = pages.get(this.travelyourearth$page % pages.size());
        for (int i = 0; i < page.size(); i++) {
            graphics.item(new ItemStack(page.get(i)), xo + 41 + i * 22, yo + 108);
        }
    }
}