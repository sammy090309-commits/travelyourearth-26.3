package com.sam_mc.travelyourearth.event;

import com.sam_mc.travelyourearth.TravelYourEarth;
import com.sam_mc.travelyourearth.enchantment.ModEnchantments;
import com.sam_mc.travelyourearth.particle.ModParticles;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.Optional;

/**
 * Cowardice enchantment (inspired by Minecraft Dungeons), only on CHESTPLATES:
 * the healthier you are, the braver you hit. Everything you hurt takes more damage
 * (melee, arrows, tridents...).
 *
 *  1) EXTRA DAMAGE, +15% per level at full health:  I = +15%   II = +30%   III = +45%
 *     It doesn't turn off when you get hurt: every heart you lose takes away a bit of it,
 *     until it reaches 0 at 3 hearts.
 *       Example, Cowardice III with 10 hearts:
 *         10 hearts +45%   8 hearts +32%   6 hearts +19%   4 hearts +6%   3 hearts +0%
 *
 *  2) THE LAST 3 HEARTS: you're scared, so your damage goes a bit DOWN (-5% per heart lost below 3):
 *         3 hearts 0%   2 hearts -5%   1 heart -10%   half a heart -12.5%
 *
 *  3) GOLDEN SPARKLES (particle travelyourearth:golden_spark): ONLY when a hit carries the extra damage,
 *     a burst of them comes out of the target (more sparkles the bigger the bonus).
 */
@EventBusSubscriber(modid = TravelYourEarth.MODID)
public class CowardiceEvents {

    /** Extra damage per level at full health: I = 15%, II = 30%, III = 45%. */
    private static final float BONUS_PER_LEVEL = 0.15F;
    /** At this health (3 hearts) the bonus is 0; below it the penalty starts. */
    private static final float SCARED_HEALTH = 6.0F;
    /** Damage lost per heart (2 health) below 3 hearts. */
    private static final float PENALTY_PER_HEART = 0.05F;

    private CowardiceEvents() {}

    @SubscribeEvent
    public static void onHurt(LivingIncomingDamageEvent event) {
        LivingEntity target = event.getEntity();
        if (!(target.level() instanceof ServerLevel level)) return;

        // The one who caused the damage (the shooter for arrows)
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) return;
        if (attacker == target) return;

        Optional<Holder.Reference<Enchantment>> cowardice =
                level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(ModEnchantments.COWARDICE);
        if (cowardice.isEmpty()) return;

        // Cowardice level of the chestplate the attacker is wearing
        int enchLevel = EnchantmentHelper.getEnchantmentLevel(cowardice.get(), attacker);
        if (enchLevel <= 0) return;

        float health = attacker.getHealth();
        float maxHealth = attacker.getMaxHealth();
        float multiplier;

        if (health > SCARED_HEALTH) {
            // Brave: the full bonus at full health, going down heart by heart until 0 at 3 hearts
            float maxBonus = BONUS_PER_LEVEL * enchLevel;
            float brave = Mth.clamp((health - SCARED_HEALTH) / Math.max(1.0F, maxHealth - SCARED_HEALTH), 0.0F, 1.0F);
            float bonus = maxBonus * brave;
            multiplier = 1.0F + bonus;
            if (bonus > 0.0F) {
                goldenBurst(level, target, brave);
            }
        } else {
            // Scared (3 hearts or less): a little less damage
            float heartsBelow = (SCARED_HEALTH - health) / 2.0F;
            multiplier = 1.0F - PENALTY_PER_HEART * heartsBelow;
        }

        event.setAmount(event.getAmount() * multiplier);
    }

    /**
     * Burst of golden sparkles (your golden_spark particle), made EXACTLY like vanilla makes the
     * crit / Sharpness burst (its "tracking emitter"): each sparkle starts at a random spot around
     * the target's body and flies outwards in a random direction, a little upwards.
     * @param strength 0-1: how much of the bonus is active (more sparkles at full health; vanilla uses 16)
     */
    private static void goldenBurst(ServerLevel level, LivingEntity target, float strength) {
        RandomSource random = target.getRandom();
        int count = 6 + Math.round(10 * strength);
        for (int i = 0; i < count; i++) {
            // A random direction inside a ball (same as vanilla)
            double xa, ya, za;
            do {
                xa = random.nextFloat() * 2.0F - 1.0F;
                ya = random.nextFloat() * 2.0F - 1.0F;
                za = random.nextFloat() * 2.0F - 1.0F;
            } while (xa * xa + ya * ya + za * za > 1.0D);

            double x = target.getX() + xa * target.getBbWidth() / 4.0D;
            double y = target.getY(0.5D) + ya * target.getBbHeight() / 4.0D;
            double z = target.getZ() + za * target.getBbWidth() / 4.0D;
            // count 0 = the 3 numbers after it are the EXACT speed of the sparkle (like vanilla's emitter)
            level.sendParticles(ModParticles.GOLDEN_SPARK.get(), x, y, z, 0, xa, ya + 0.2D, za, 1.0D);
        }
    }
}