package com.sam_mc.travelyourearth.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sam_mc.travelyourearth.TravelYourEarth;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.VillagerLikeModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.VillagerDataHolderRenderState;
import net.minecraft.resources.Identifier;

/**
 * Draws the ruby badge ON TOP of the vanilla level badge (diamond), like VillagerProfessionLayer does.
 * The ruby gem uses the same pixels as the diamond one, so it covers it completely.
 *
 * Only drawn when RubyBadgeClient decided it (render data SHOW_RUBY_BADGE = true).
 *
 * Textures:
 *   assets/travelyourearth/textures/entity/villager/profession_level/ruby.png
 *   assets/travelyourearth/textures/entity/zombie_villager/profession_level/ruby.png
 */
public class RubyBadgeLayer<S extends LivingEntityRenderState & VillagerDataHolderRenderState,
        M extends EntityModel<S> & VillagerLikeModel<S>> extends RenderLayer<S, M> {

    private final Identifier texture;

    /** @param path "villager" or "zombie_villager" (same folder names as vanilla). */
    public RubyBadgeLayer(RenderLayerParent<S, M> renderer, String path) {
        super(renderer);
        this.texture = Identifier.fromNamespaceAndPath(TravelYourEarth.MODID,
                "textures/entity/" + path + "/profession_level/ruby.png");
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords,
                       S state, float yRot, float xRot) {
        if (state.isInvisible || state.isBaby) return;

        Boolean show = state.getRenderData(RubyBadgeClient.SHOW_RUBY_BADGE);
        if (show == null || !show) return;

        // Same call as vanilla's level badge (order 3), but with order 4 -> drawn right after it, on top
        renderColoredCutoutModel(this.getParentModel(), this.texture, poseStack, submitNodeCollector,
                lightCoords, state, -1, 4);
    }
}