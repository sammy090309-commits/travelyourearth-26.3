package com.sam_mc.travelyourearth.enchantment;

import com.sam_mc.travelyourearth.TravelYourEarth;
import com.sam_mc.travelyourearth.tags.ModTags;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.predicates.entity.EntityTypePredicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.AddValue;
import net.minecraft.world.level.storage.loot.LootContext.EntityTarget;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;

/**
 * Enchantments of the mod. In 26.3 enchantments are DATA: they're generated with datagen
 * (TravelYourEarthDataGen -> worldBuilder) into data/travelyourearth/enchantment/<name>.json.
 *
 * None of them is in #minecraft:in_enchanting_table, so they never appear in the enchanting table,
 * chests or normal villagers: only the illegal librarian sells them.
 */
public class ModEnchantments {

    /**
     * Banishment ("Destierro"): Smite, but against the whole "big nose" family
     * (villagers, wandering traders, zombie villagers, illagers, witches) and ravagers.
     * In Minecraft's lore illagers are villagers banished from their villages: this enchantment banishes them again.
     * The list is the entity tag travelyourearth:sensitive_to_banishment (ModEntityTypeTagsProvider).
     */
    public static final ResourceKey<Enchantment> BANISHMENT = key("banishment");

    /**
     * Harpoon ("Arpón"): spear enchantment. Instead of pushing the target away, the hit PULLS it towards you.
     * The definition (where it goes, levels, cost) is data, but the pull itself is code:
     * HarpoonEvents flips the direction of the vanilla knockback (vanilla has no data effect that
     * can point "towards the attacker").
     */
    public static final ResourceKey<Enchantment> HARPOON = key("harpoon");

    /**
     * Cowardice ("Cobardía"), inspired by Minecraft Dungeons: CHESTPLATE enchantment (the heart). The more health
     * you have, the more damage you deal (up to +15% / +30% / +45% at full health, going down heart by heart);
     * with 3 hearts or less you get scared and deal a bit less. Golden sparkles on boosted hits.
     * The definition is data; the bonus itself is code (CowardiceEvents): vanilla's damage effects
     * only work on the weapon, not on armor, and can't check the attacker's health.
     */
    public static final ResourceKey<Enchantment> COWARDICE = key("cowardice");

    public static void bootstrap(BootstrapContext<Enchantment> context) {
        HolderGetter<Enchantment> enchantments = context.lookup(Registries.ENCHANTMENT);
        HolderGetter<Item> items = context.lookup(Registries.ITEM);
        HolderGetter<EntityType<?>> entityTypes = context.lookup(Registries.ENTITY_TYPE);

        // Copy of vanilla 26.3 SMITE, changing the target tag and a small nerf:
        // EntityTypeTags.SENSITIVE_TO_SMITE -> ModTags.EntityTypes.SENSITIVE_TO_BANISHMENT
        // - Weight 5, levels I-V, cost like Smite, anvil cost 2
        // - +2 damage per level (Smite: +2.5) against the "big nose" family and ravagers:
        //   illagers are more dangerous than zombies, so the fight against them should stay harder.
        //   I = +2   II = +4   III = +6   IV = +8   V = +10
        // - Incompatible with Sharpness, Smite, Bane of Arthropods... (#minecraft:exclusive_set/damage)
        register(context, BANISHMENT, Enchantment.enchantment(
                        Enchantment.definition(
                                items.getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
                                items.getOrThrow(ItemTags.MELEE_WEAPON_ENCHANTABLE),
                                5,
                                5,
                                Enchantment.dynamicCost(5, 8),
                                Enchantment.dynamicCost(25, 8),
                                2,
                                new EquipmentSlotGroup[]{EquipmentSlotGroup.MAINHAND}))
                .exclusiveWith(enchantments.getOrThrow(EnchantmentTags.DAMAGE_EXCLUSIVE))
                .withEffect(
                        EnchantmentEffectComponents.DAMAGE,
                        new AddValue(LevelBasedValue.perLevel(2.0F)),
                        LootItemEntityPropertyCondition.hasProperties(EntityTarget.THIS,
                                EntityPredicate.Builder.entity().entityType(EntityTypePredicate.of(entityTypes, ModTags.EntityTypes.SENSITIVE_TO_BANISHMENT)))));

        // Harpoon: only for spears (#minecraft:enchantable/lunge, the same items as vanilla Lunge)
        // - Weight 2 (rare), levels I-III, cost like Lunge, anvil cost 4
        // - Incompatible with Knockback (#travelyourearth:exclusive_set/harpoon): one pushes, the other pulls
        // - No data effect: the pull is done in HarpoonEvents
        register(context, HARPOON, Enchantment.enchantment(
                        Enchantment.definition(
                                items.getOrThrow(ItemTags.LUNGE_ENCHANTABLE),
                                2,
                                3,
                                Enchantment.dynamicCost(5, 8),
                                Enchantment.dynamicCost(25, 8),
                                4,
                                new EquipmentSlotGroup[]{EquipmentSlotGroup.MAINHAND}))
                .exclusiveWith(enchantments.getOrThrow(ModTags.Enchantments.HARPOON_EXCLUSIVE)));

        // Cowardice: ONLY chestplates (the heart is where your courage... or cowardice is)
        // - Weight 2 (rare), levels I-III, cost like Protection, anvil cost 4
        // - No data effect: the damage bonus is done in CowardiceEvents
        register(context, COWARDICE, Enchantment.enchantment(
                Enchantment.definition(
                        items.getOrThrow(ItemTags.CHEST_ARMOR_ENCHANTABLE),
                        2,
                        3,
                        Enchantment.dynamicCost(10, 10),
                        Enchantment.dynamicCost(40, 10),
                        4,
                        new EquipmentSlotGroup[]{EquipmentSlotGroup.CHEST})));
    }

    private static void register(BootstrapContext<Enchantment> context, ResourceKey<Enchantment> key, Enchantment.Builder builder) {
        context.register(key, builder.build(key.identifier()));
    }

    private static ResourceKey<Enchantment> key(String id) {
        return ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath(TravelYourEarth.MODID, id));
    }
}