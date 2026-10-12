package com.sam_mc.travelyourearth.client;

import com.google.common.reflect.TypeToken;
import com.sam_mc.travelyourearth.TravelYourEarth;
import com.sam_mc.travelyourearth.attachment.ModAttachments;
import com.sam_mc.travelyourearth.villager.IllegalVillagers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;


/**
 * Ruby badge (1.2): on an illegal villager (or zombie villager) with the secret phase unlocked,
 * the diamond level badge turns into a RUBY badge when:
 *   - there are no other villagers or iron golems within 16 blocks, and
 *   - YOU are holding a ruby (main hand or off hand).
 *
 * The SERVER decides it (IllegalVillagerEvents, with a random 1-2 s delay) and syncs it
 * (SECRET_REVEALED_TO): only that player sees the ruby badge, at the same moment the
 * illegal trade and the illegal item in its hand appear.
 */
@EventBusSubscriber(modid = TravelYourEarth.MODID, value = Dist.CLIENT)
public class RubyBadgeClient {

    /** "Show the ruby badge?" saved in the render state of each villager / zombie villager. */
    public static final ContextKey<Boolean> SHOW_RUBY_BADGE =
            new ContextKey<>(Identifier.fromNamespaceAndPath(TravelYourEarth.MODID, "show_ruby_badge"));

    // =========================================================================
    // 1) Every frame: copy "is the secret revealed to me?" into the render state
    // =========================================================================
    @SubscribeEvent
    public static void registerRenderStateModifiers(RegisterRenderStateModifiersEvent event) {
        event.registerEntityModifier(
                new TypeToken<LivingEntityRenderer<LivingEntity, LivingEntityRenderState, ?>>() {},
                (entity, state) -> {
                    if (entity instanceof Villager || entity instanceof ZombieVillager) {
                        state.setRenderData(SHOW_RUBY_BADGE, shouldShowRubyBadge(entity));
                    }
                });
    }

    private static boolean shouldShowRubyBadge(LivingEntity entity) {
        // The server decides who sees the secret (with the 1-2 s delay) and syncs it:
        // the badge changes at the same moment as the illegal trade and the item in its hand.
        // Savanna masters: the ruby is their legal currency, so their master badge is the ruby one
        // (instead of the diamond one), for everyone and always
        if (entity instanceof Villager villager && villager.getVillagerData().level() >= 5
                && IllegalVillagers.isSavannaVillager(villager)) {
            return true;
        }
        // Also shown to EVERYONE while it celebrates the illagers' victory in a raid (proud of it!)
        if (entity.hasData(ModAttachments.PROUD_TRAFFICKER) && entity.getData(ModAttachments.PROUD_TRAFFICKER)) {
            return true;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        return player != null && IllegalVillagers.isRevealedTo(entity, player);
    }

    // =========================================================================
    // 2) Add the ruby badge layer to the villager and zombie villager renderers
    // =========================================================================
    @SubscribeEvent
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (EntityType<?> type : event.getEntityTypes()) {
            // "minecraft:villager" -> "villager", "minecraft:zombie_villager" -> "zombie_villager"
            String path = BuiltInRegistries.ENTITY_TYPE.getKey(type).getPath();
            if (!path.equals("villager") && !path.equals("zombie_villager")) continue;

            if (event.getRenderer(type) instanceof LivingEntityRenderer renderer) {
                renderer.addLayer(new RubyBadgeLayer(renderer, path));
            }
        }
    }
}