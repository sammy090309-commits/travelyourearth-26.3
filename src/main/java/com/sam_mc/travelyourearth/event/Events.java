package com.sam_mc.travelyourearth.event;

import com.sam_mc.travelyourearth.TravelYourEarth;
import com.sam_mc.travelyourearth.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = TravelYourEarth.MODID)
public class Events {

    private static final Map<UUID, Integer> COOLDOWNS = new HashMap<>();
    private static final int COOLDOWN_TICKS = 2 * 20; // Exactamente 2 segundos (40 ticks)

    @SubscribeEvent
    public static void onEntityTickPre(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (entity.level().isClientSide()) return;

        UUID entityId = entity.getUUID();
        int currentCooldown = COOLDOWNS.getOrDefault(entityId, 0);

        // Disminuimos el contador en cada tick
        if (currentCooldown > 0) {
            COOLDOWNS.put(entityId, currentCooldown - 1);
            return; // Si el cooldown está activo, bloqueamos la habilidad por completo
        }

        // Si la entidad no está quemándose, no ejecutamos nada
        if (!entity.isOnFire()) return;

        // Comprobamos si la entidad está pisando o tocando fuego, fuego de almas o lava
        boolean isTouchingDamageBlock = BlockPos.betweenClosedStream(entity.getBoundingBox())
                .anyMatch(pos -> {
                    BlockState state = entity.level().getBlockState(pos);
                    return state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE) || state.is(Blocks.LAVA);
                });

        // Si no está dentro de la fuente directa de fuego/lava y la armadura está equipada
        if (!isTouchingDamageBlock && !entity.isInLava()) {
            if (hasAnyRubyArmorEquipped(entity)) {

                // 1. Apagado lógico y visual
                entity.clearFire();
                entity.setRemainingFireTicks(0);

                // 2. Efectos visuales y de sonido
                if (entity.level() instanceof ServerLevel serverLevel) {
                    serverLevel.playSound(
                            null,
                            entity.getX(), entity.getY(), entity.getZ(),
                            SoundEvents.FIRE_EXTINGUISH,
                            SoundSource.PLAYERS,
                            1.0F,
                            1.0F
                    );

                    serverLevel.sendParticles(
                            ParticleTypes.WHITE_SMOKE,
                            entity.getX(), entity.getY() + 1.0, entity.getZ(),
                            25,
                            0.3, 0.4, 0.3,
                            0.05
                    );
                }

                // 3. Reiniciamos el cooldown global a 2 segundos
                COOLDOWNS.put(entityId, COOLDOWN_TICKS);
            }
        }
    }

    private static boolean hasAnyRubyArmorEquipped(LivingEntity entity) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                ItemStack stack = entity.getItemBySlot(slot);
                if (stack.is(ModItems.RUBY_HELMET.get()) ||
                        stack.is(ModItems.RUBY_CHESTPLATE.get()) ||
                        stack.is(ModItems.RUBY_LEGGINGS.get()) ||
                        stack.is(ModItems.RUBY_BOOTS.get())) {
                    return true;
                }
            }
        }

        ItemStack bodyStack = entity.getItemBySlot(EquipmentSlot.BODY);
        if (bodyStack.is(ModItems.RUBY_HORSE_ARMOR.get()) ||
                bodyStack.is(ModItems.RUBY_NAUTILUS_ARMOR.get())) {
            return true;
        }

        return false;
    }
}