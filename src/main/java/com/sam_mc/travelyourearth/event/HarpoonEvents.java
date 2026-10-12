package com.sam_mc.travelyourearth.event;

import com.sam_mc.travelyourearth.TravelYourEarth;
import com.sam_mc.travelyourearth.enchantment.ModEnchantments;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;

import java.util.Optional;

/**
 * Harpoon enchantment: a hit with a Harpoon spear PULLS the target towards the attacker
 * instead of pushing it away, and the target is STUNNED for a moment (longer with each level).
 *
 * Both spear attacks harpoon: the JAB (the normal stab, left click) and the CHARGE (holding right
 * click). Players and mobs alike.
 *
 * How: vanilla calls knockback() right after a melee hit. We flip its direction (ratio X/Z)
 * when the one who just hit the target is holding a Harpoon weapon. Vanilla still applies
 * knockback resistance (ravagers, netherite...), so heavy mobs are barely pulled.
 */
@EventBusSubscriber(modid = TravelYourEarth.MODID)
public class HarpoonEvents {

    /**
     * Pull strength of each level, compared to a normal hit's knockback.
     * Very different on purpose, so each level feels clearly stronger:
     *   I   = 0.6x  (a small tug)
     *   II  = 1.2x  (a bit more than a normal hit, but towards you)
     *   III = 2.0x  (a real yank!)
     * Levels above III (commands) use the last value.
     */
    private static final float[] PULL_PER_LEVEL = {0.6F, 1.2F, 2.0F};

    /**
     * Stun of each level (ticks, 20 = 1 second): the pulled mob is almost frozen for a moment,
     * so you can hit it again.
     *   I = 0.5 s   II = 1 s   III = 1.5 s
     */
    private static final int[] STUN_TICKS_PER_LEVEL = {10, 20, 30};

    /** Slowness level of the stun (amplifier 4 = Slowness V: the mob can barely move). */
    private static final int STUN_SLOWNESS_AMPLIFIER = 4;

    @SubscribeEvent
    public static void onKnockBack(LivingKnockBackEvent event) {
        LivingEntity target = event.getEntity();
        if (!(target.level() instanceof ServerLevel level)) return;

        // Who hit the target THIS tick (vanilla saves it right before calling knockback)
        LivingEntity attacker = target.getLastHurtByMob();
        if (attacker == null || target.getLastHurtByMobTimestamp() != target.tickCount) return;

        Optional<Holder.Reference<Enchantment>> harpoon =
                level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(ModEnchantments.HARPOON);
        if (harpoon.isEmpty()) return;

        ItemStack weapon = attacker.getMainHandItem();
        int enchLevel = EnchantmentHelper.getItemEnchantmentLevel(harpoon.get(), weapon);
        if (enchLevel <= 0) return;

        // Both spear attacks harpoon: the JAB (normal stab) and the CHARGE (holding right click),
        // for players and mobs alike.

        // Same strength scale, opposite direction: towards the attacker
        float multiplier = PULL_PER_LEVEL[Math.min(enchLevel, PULL_PER_LEVEL.length) - 1];
        event.setStrength(event.getStrength() * multiplier);
        event.setRatioX(-event.getRatioX());
        event.setRatioZ(-event.getRatioZ());

        stun(target, enchLevel);
    }

    /** Short stun: very strong Slowness, and mobs stop walking where they were going. */
    private static void stun(LivingEntity target, int enchLevel) {
        int ticks = STUN_TICKS_PER_LEVEL[Math.min(enchLevel, STUN_TICKS_PER_LEVEL.length) - 1];
        // ambient false, no particles, no icon: it's a hit stun, not a potion
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, ticks, STUN_SLOWNESS_AMPLIFIER,
                false, false, false));
        if (target instanceof Mob mob) {
            mob.getNavigation().stop();
        }
    }
}