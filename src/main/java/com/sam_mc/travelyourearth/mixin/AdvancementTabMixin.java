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
 * "Earth" background for the mod's advancement tab.
 *
 * Vanilla (AdvancementTab#extractContents) repeats ONE single 16x16 texture over the whole background.
 * Here we intercept each tile and, ONLY if the tab background is Earth dirt:
 *
 *   first row -> Earth grass  (earth_grass_side)
 *   the rest  -> Earth dirt   (earth_dirt, the normal background texture)
 *
 * - Big tree (can be moved up/down): the "first row" is the top row of the MAP,
 *   so when scrolling the ground moves together with the advancements.
 * - Small tree (Minecraft centers it and doesn't let you move it): the background is aligned
 *   with the top edge of the window, so the grass stays at the top and never looks "empty".
 *
 * Other tabs (vanilla and from other mods) aren't touched.
 */
@Mixin(AdvancementTab.class)
public abstract class AdvancementTabMixin {

    @Shadow @Final private Identifier background;
    @Shadow private double scrollY;

    @Shadow public abstract boolean canScrollVertically();

    /** Background texture that enables Earth mode (set by ModAdvancementProvider: "block/earth_dirt"). */
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
                // Big tree: row based on the MAP (subtracting the scroll). Row 0 is the top edge.
                row = Math.floorDiv(y - intScrollY, 16);
            } else {
                // Small tree: vanilla shifts the tiles "intScrollY % 16" pixels.
                // We undo it so the rows start right at the edge of the window.
                y -= intScrollY % 16;
                row = Math.floorDiv(y, 16);
            }

            if (row == 0) {
                texture = TRAVELYOUREARTH$EARTH_GRASS_SIDE;   // grass on top
            }
            // any other row: Earth dirt (the normal background)
        }

        graphics.blit(pipeline, texture, x, y, u, v, width, height, textureWidth, textureHeight);
    }
}