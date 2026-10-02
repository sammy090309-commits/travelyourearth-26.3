package com.sam_mc.travelyourearth.event;

import java.util.Set;

import com.sam_mc.travelyourearth.TravelYourEarth;
import com.sam_mc.travelyourearth.item.ModItems;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

/**
 * Naturally spawned mobs with ruby armor.
 *
 * When one of the target mobs spawns with vanilla armor, it has a RUBY_CHANCE chance of
 * having its armor turned into ruby (same slots, keeping enchantments and wear).
 * Ominous trial chamber mobs are handled by RubyTrialGearLootModifier instead.
 */
@EventBusSubscriber(modid = TravelYourEarth.MODID)
public class MobEquipmentHandler {

    // =========================================================================
    // Settings
    // =========================================================================

    // Chance that a mob that already has vanilla armor switches to ruby
    private static final float RUBY_CHANCE = 0.04F;
    // 1.0 = all its pieces become ruby. Less than 1.0 = it can stay mixed with vanilla
    private static final float PIECE_REPLACE_CHANCE = 1.0F;
    // Testing only: gives vanilla armor to mobs that don't have any. Leave it on false
    private static final boolean DEBUG_FORCE_VANILLA_GEAR = false;

    // Target mobs (registry id). No zombie villager or drowned
    private static final Set<String> TARGET_MOBS = Set.of(
            "zombie", "husk", "skeleton", "stray", "bogged", "parched"
    );

    // Allowed spawn reasons. TRIAL_SPAWNER is left out on purpose (ominous trials use RubyTrialGearLootModifier)
    private static final Set<String> ALLOWED_SPAWN_REASONS = Set.of(
            "NATURAL", "CHUNK_GENERATION", "STRUCTURE", "SPAWNER",
            "EVENT", "REINFORCEMENT", "JOCKEY",
            "COMMAND", "SPAWN_ITEM_USE", "DISPENSER" // /summon, spawn eggs and dispensers (useful for testing)
    );

    private static final String PENDING_TAG = "travelyourearth_ruby_pending";

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    // =========================================================================
    // Events
    // =========================================================================

    // ---------- Step 1: mark the mob when it spawns (before vanilla gives it its gear) ----------
    @SubscribeEvent
    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        Mob mob = event.getEntity();
        if (!TARGET_MOBS.contains(idOf(mob))) return;
        if (!ALLOWED_SPAWN_REASONS.contains(event.getSpawnType().name())) return;
        mob.addTag(PENDING_TAG);
    }

    // ---------- Step 2: replace when it joins the world (vanilla already gave it its gear) ----------
    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof Mob mob)) return;
        if (!mob.removeTag(PENDING_TAG)) return; // only mobs marked when spawning, and only once

        if (DEBUG_FORCE_VANILLA_GEAR) debugGiveVanillaGear(mob);

        // Does it have vanilla armor?
        boolean hasArmor = false;
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            if (isReplaceableArmor(mob.getItemBySlot(slot), slot)) {
                hasArmor = true;
                break;
            }
        }
        if (!hasArmor) return;

        RandomSource random = mob.getRandom();
        if (random.nextFloat() >= RUBY_CHANCE) return;

        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack current = mob.getItemBySlot(slot);
            if (!isReplaceableArmor(current, slot)) continue;

            Item rubyItem = rubyPieceFor(slot);
            if (current.getItem() == rubyItem) continue;
            if (random.nextFloat() >= PIECE_REPLACE_CHANCE) continue;

            mob.setItemSlot(slot, toRuby(current, rubyItem));
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    // Builds the ruby piece copying the enchantments and the wear ratio
    private static ItemStack toRuby(ItemStack original, Item rubyItem) {
        ItemStack ruby = new ItemStack(rubyItem);

        var enchantments = original.get(DataComponents.ENCHANTMENTS);
        if (enchantments != null && !enchantments.isEmpty()) {
            ruby.set(DataComponents.ENCHANTMENTS, enchantments);
        }

        int oldMax = original.getMaxDamage();
        int newMax = ruby.getMaxDamage();
        if (oldMax > 0 && newMax > 0) {
            float wear = (float) original.getDamageValue() / oldMax;
            int newDamage = Math.round(wear * newMax);
            ruby.setDamageValue(Math.min(newDamage, newMax - 1));
        }
        return ruby;
    }

    // Real armor: damageable and equippable in that slot (excludes pumpkins and heads)
    private static boolean isReplaceableArmor(ItemStack stack, EquipmentSlot slot) {
        if (stack.isEmpty() || !stack.isDamageableItem()) return false;
        var equippable = stack.get(DataComponents.EQUIPPABLE);
        return equippable != null && equippable.slot() == slot;
    }

    private static Item rubyPieceFor(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> ModItems.RUBY_HELMET.get();
            case CHEST -> ModItems.RUBY_CHESTPLATE.get();
            case LEGS -> ModItems.RUBY_LEGGINGS.get();
            case FEET -> ModItems.RUBY_BOOTS.get();
            default -> throw new IllegalArgumentException("Not an armor slot: " + slot);
        };
    }

    private static String idOf(Mob mob) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).getPath();
    }

    // Testing only: worn diamond chestplate and leggings, if the mob has no chestplate
    private static void debugGiveVanillaGear(Mob mob) {
        if (!mob.getItemBySlot(EquipmentSlot.CHEST).isEmpty()) return;
        ItemStack chest = new ItemStack(Items.DIAMOND_CHESTPLATE);
        ItemStack legs = new ItemStack(Items.DIAMOND_LEGGINGS);
        chest.setDamageValue(100);
        legs.setDamageValue(50);
        mob.setItemSlot(EquipmentSlot.CHEST, chest);
        mob.setItemSlot(EquipmentSlot.LEGS, legs);
    }
}