package com.sam_mc.travelyourearth.event;

import com.sam_mc.travelyourearth.TravelYourEarth;
import com.sam_mc.travelyourearth.item.ModItems;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Armadura de rubí: apaga el fuego del que la lleva puesta en cuanto sale del fuego/lava.
 * Funciona con cualquier pieza (casco, peto, grebas, botas, armadura de caballo o de nautilus).
 * Después de apagarse hay 2 segundos de espera antes de poder volver a apagarse.
 *
 * Si es un jugador, le da el logro "A prueba de fuego".
 */
@EventBusSubscriber(modid = TravelYourEarth.MODID)
public class RubyArmorFireHandler {

    private static final int COOLDOWN_TICKS = 2 * 20; // 2 segundos

    /**
     * Logro "A prueba de fuego" (lo genera ModAdvancementProvider). Hay dos versiones y
     * solo UNA está cargada, según la opción "modAdvancementTab" de la config:
     */
    public static final Identifier FIREPROOF_ADVANCEMENT =                 // pestaña Aventura (OFF)
            Identifier.fromNamespaceAndPath(TravelYourEarth.MODID, "adventure/fireproof");
    public static final Identifier FIREPROOF_ADVANCEMENT_MOD_TAB =         // pestaña del mod (ON)
            Identifier.fromNamespaceAndPath(TravelYourEarth.MODID, "ruby/fireproof");
    /** Nombre del criterio dentro del logro. */
    public static final String FIREPROOF_CRITERION = "ruby_armor_extinguished";

    /**
     * Entidad -> tick del juego a partir del cual puede volver a apagarse.
     * WeakHashMap: cuando la entidad muere o se descarga, Java borra su entrada solo
     * (con un HashMap normal por UUID, el mapa crecía para siempre).
     */
    private static final Map<LivingEntity, Long> NEXT_EXTINGUISH_TICK = new WeakHashMap<>();

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (!(entity.level() instanceof ServerLevel level)) return;

        // Primero lo más barato: esto corre para TODAS las entidades en CADA tick
        if (!entity.isOnFire()) return;
        if (!hasAnyRubyArmorEquipped(entity)) return;

        // Cooldown: comparamos con el tiempo del mundo (sin restar 1 cada tick)
        long now = level.getGameTime();
        Long nextAllowed = NEXT_EXTINGUISH_TICK.get(entity);
        if (nextAllowed != null && now < nextAllowed) return;

        // Intencional: los no-muertos con rubí al sol también se apagan cada 2 s,
        // así que siguen quemándose y mueren, pero más lento.

        // Si sigue dentro del fuego o la lava, no se apaga (se volvería a encender al instante)
        if (entity.isInLava() || isTouchingFireOrLava(entity)) return;

        // 1. Apagar
        entity.clearFire();

        // 2. Sonido y humo
        level.playSound(
                null,
                entity.getX(), entity.getY(), entity.getZ(),
                SoundEvents.FIRE_EXTINGUISH,
                entity.getSoundSource(), // jugador -> PLAYERS, zombi -> HOSTILE, caballo -> NEUTRAL...
                1.0F,
                1.0F
        );
        level.sendParticles(
                ParticleTypes.WHITE_SMOKE,
                entity.getX(), entity.getY() + 1.0, entity.getZ(),
                25,
                0.3, 0.4, 0.3,
                0.05
        );

        // 3. Cooldown de 2 segundos
        NEXT_EXTINGUISH_TICK.put(entity, now + COOLDOWN_TICKS);

        // 4. Logro "A prueba de fuego" (si ya lo tiene, award() no hace nada).
        //    Probamos las dos versiones: la que no esté cargada da null y se salta.
        if (entity instanceof ServerPlayer player) {
            for (Identifier id : new Identifier[]{FIREPROOF_ADVANCEMENT, FIREPROOF_ADVANCEMENT_MOD_TAB}) {
                AdvancementHolder advancement = level.getServer().getAdvancements().get(id);
                if (advancement != null) {
                    player.getAdvancements().award(advancement, FIREPROOF_CRITERION);
                }
            }
        }
    }

    /** ¿Algún bloque dentro de su caja de colisión es fuego, fuego de almas o lava? */
    private static boolean isTouchingFireOrLava(LivingEntity entity) {
        return BlockPos.betweenClosedStream(entity.getBoundingBox())
                .anyMatch(pos -> {
                    BlockState state = entity.level().getBlockState(pos);
                    return state.is(BlockTags.FIRE) || state.is(Blocks.LAVA); // FIRE = fuego + fuego de almas
                });
    }

    private static boolean hasAnyRubyArmorEquipped(LivingEntity entity) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                ItemStack stack = entity.getItemBySlot(slot);
                if (stack.is(ModItems.RUBY_HELMET.get())
                        || stack.is(ModItems.RUBY_CHESTPLATE.get())
                        || stack.is(ModItems.RUBY_LEGGINGS.get())
                        || stack.is(ModItems.RUBY_BOOTS.get())) {
                    return true;
                }
            }
        }

        ItemStack bodyStack = entity.getItemBySlot(EquipmentSlot.BODY);
        return bodyStack.is(ModItems.RUBY_HORSE_ARMOR.get())
                || bodyStack.is(ModItems.RUBY_NAUTILUS_ARMOR.get());
    }
}