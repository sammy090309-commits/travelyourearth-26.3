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

@EventBusSubscriber(modid = TravelYourEarth.MODID)
public class MobEquipmentHandler {

    // ===================== AJUSTES =====================
    // Prob. de que un mob que ya tiene armadura vanilla pase a rubí
    private static final float RUBY_CHANCE = 0.04F;
    // 1.0 = todas sus piezas pasan a rubí. Menos de 1.0 = puede quedar mezclado con vanilla
    private static final float PIECE_REPLACE_CHANCE = 1.0F;
    // Solo para pruebas: da armadura vanilla a los mobs que no tengan. Déjalo en false
    private static final boolean DEBUG_FORCE_VANILLA_GEAR = false;

    // Mobs del sistema (id del registro). Sin zombi aldeano ni ahogado
    private static final Set<String> TARGET_MOBS = Set.of(
            "zombie", "husk", "skeleton", "stray", "bogged", "parched"
    );

    // Motivos de spawn permitidos. TRIAL_SPAWNER queda fuera a propósito (trial ominoso, más adelante)
    private static final Set<String> ALLOWED_SPAWN_REASONS = Set.of(
            "NATURAL", "CHUNK_GENERATION", "STRUCTURE", "SPAWNER",
            "EVENT", "REINFORCEMENT", "JOCKEY",
            "COMMAND", "SPAWN_ITEM_USE", "DISPENSER" // /summon, huevos y dispensadores (útiles para probar)
    );

    private static final String PENDING_TAG = "travelyourearth_ruby_pending";

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    // ---------- Paso 1: marcar al nacer (antes de que vanilla ponga su equipo) ----------
    @SubscribeEvent
    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        Mob mob = event.getEntity();
        if (!TARGET_MOBS.contains(idOf(mob))) return;
        if (!ALLOWED_SPAWN_REASONS.contains(event.getSpawnType().name())) return;
        mob.addTag(PENDING_TAG);
    }

    // ---------- Paso 2: reemplazar al entrar al mundo (vanilla ya puso su equipo) ----------
    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof Mob mob)) return;
        if (!mob.removeTag(PENDING_TAG)) return; // solo mobs marcados al nacer, y una sola vez

        if (DEBUG_FORCE_VANILLA_GEAR) debugGiveVanillaGear(mob);

        // ¿Tiene armadura vanilla?
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

    // Construye la pieza de rubí copiando encantamientos y proporción de desgaste
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

    // Armadura real: dañable y equipable en ese slot (excluye calabazas y cabezas)
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
            default -> throw new IllegalArgumentException("No es un slot de armadura: " + slot);
        };
    }

    private static String idOf(Mob mob) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).getPath();
    }

    // Solo pruebas: pechera y pantalón de diamante, ya desgastados, si el mob no trae pechera
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