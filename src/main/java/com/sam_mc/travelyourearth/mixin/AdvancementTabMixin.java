package com.sam_mc.travelyourearth.mixin;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.sam_mc.travelyourearth.TravelYourEarth;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Fondo "Earth" para la pestaña de logros del mod.
 *
 * Vanilla (AdvancementTab#extractContents) repite UNA sola textura de 16x16 en todo el fondo.
 * Aquí interceptamos cada cuadro y, SOLO si el fondo de la pestaña es la tierra Earth:
 *
 *   primera fila -> césped Earth  (earth_grass_side)
 *   el resto     -> tierra Earth  (earth_dirt, la textura normal del fondo)
 *
 * - Árbol grande (se puede mover arriba/abajo): la "primera fila" es la de arriba del MAPA,
 *   así al hacer scroll el suelo se mueve junto con los logros.
 * - Árbol pequeño (Minecraft lo centra y no deja moverlo): el fondo se alinea con el borde
 *   de arriba de la ventana, así el césped queda pegado arriba y nunca se ve "vacío".
 *
 * Las demás pestañas (vanilla y de otros mods) no se tocan.
 */
@Mixin(AdvancementTab.class)
public abstract class AdvancementTabMixin {

    @Shadow @Final private Identifier background;
    @Shadow private double scrollY;

    @Shadow public abstract boolean canScrollVertically();

    /** Textura de fondo que activa el modo Earth (la pone ModAdvancementProvider: "block/earth_dirt"). */
    @Unique
    private static final Identifier TRAVELYOUREARTH$EARTH_DIRT =
            Identifier.fromNamespaceAndPath(TravelYourEarth.MODID, "textures/block/earth_dirt.png");

    @Unique
    private static final Identifier TRAVELYOUREARTH$EARTH_GRASS_SIDE =
            Identifier.fromNamespaceAndPath(TravelYourEarth.MODID, "textures/block/earth_grass_side.png");

    @Redirect(
            method = "extractContents",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blit(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIII)V"
            )
    )
    private void travelyourearth$earthBackground(GuiGraphicsExtractor graphics, RenderPipeline pipeline, Identifier texture,
                                                 int x, int y, float u, float v,
                                                 int width, int height, int textureWidth, int textureHeight) {
        if (TRAVELYOUREARTH$EARTH_DIRT.equals(this.background)) {
            int intScrollY = Mth.floor(this.scrollY);
            int row;

            if (this.canScrollVertically()) {
                // Árbol grande: fila según el MAPA (restando el scroll). La fila 0 es el borde de arriba.
                row = Math.floorDiv(y - intScrollY, 16);
            } else {
                // Árbol pequeño: vanilla corre los cuadros "intScrollY % 16" píxeles.
                // Los devolvemos para que las filas empiecen justo en el borde de la ventana.
                y -= intScrollY % 16;
                row = Math.floorDiv(y, 16);
            }

            if (row == 0) {
                texture = TRAVELYOUREARTH$EARTH_GRASS_SIDE;   // césped arriba
            }
            // cualquier otra fila: tierra Earth (el fondo normal)
        }

        graphics.blit(pipeline, texture, x, y, u, v, width, height, textureWidth, textureHeight);
    }
}