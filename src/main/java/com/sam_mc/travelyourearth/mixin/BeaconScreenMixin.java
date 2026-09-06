package com.sam_mc.travelyourearth.mixin;

import com.sam_mc.travelyourearth.item.ModItems; // Asegúrate de ajustar esta ruta según donde tengas registrado tu Rubí
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.BeaconScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeaconScreen.class)
public abstract class BeaconScreenMixin {

    private static final Identifier BEACON_CUSTOM_TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/beacon.png");

    private int travelyourearth$timer = 0;
    private boolean travelyourearth$isGemPage = false;

    // 1. Contador que cambia la página cada 5 segundos (100 ticks)
    @Inject(method = "containerTick", at = @At("HEAD"))
    private void travelyourearth$onContainerTick(CallbackInfo ci) {
        this.travelyourearth$timer++;

        if (this.travelyourearth$timer >= 100) {
            this.travelyourearth$timer = 0;
            this.travelyourearth$isGemPage = !this.travelyourearth$isGemPage;
        }
    }

    // 2. Sobrescribe el fondo, oscurece la pantalla exterior y cancela el dibujado original
    @Inject(method = "extractBackground", at = @At("HEAD"), cancellable = true)
    private void travelyourearth$overrideBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        BeaconScreen screen = (BeaconScreen) (Object) this;

        // Cancela de inmediato para que Minecraft NO ejecute sus gráficos por defecto
        ci.cancel();

        // Oscurece la pantalla de afuera (efecto similar a la mesa de crafteo dibujando un velo semitransparente en toda la ventana)
        graphics.fill(0, 0, screen.width, screen.height, -1072689136);

        // Posición de la ventana en pantalla
        int xo = (screen.width - 230) / 2;
        int yo = (screen.height - 219) / 2;

        // Renderiza la textura de la baliza
        graphics.blit(RenderPipelines.GUI_TEXTURED, BEACON_CUSTOM_TEXTURE, xo, yo, 0.0F, 0.0F, 230, 219, 256, 256);

        // Renderiza la página activa
        if (this.travelyourearth$isGemPage) {
            // PÁGINA 1: Gemas
            graphics.item(new ItemStack(Items.DIAMOND), xo + 41, yo + 108);
            graphics.item(new ItemStack(Items.EMERALD), xo + 63, yo + 108);
            graphics.item(new ItemStack(ModItems.RUBY.get()), xo + 85, yo + 108);
        } else {
            // PÁGINA 2: Lingotes
            graphics.item(new ItemStack(Items.NETHERITE_INGOT), xo + 41, yo + 108);
            graphics.item(new ItemStack(Items.GOLD_INGOT), xo + 63, yo + 108);
            graphics.item(new ItemStack(Items.IRON_INGOT), xo + 85, yo + 108);


        }
    }
}