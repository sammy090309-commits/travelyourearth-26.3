package com.sam_mc.travelyourearth.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sam_mc.travelyourearth.enchantment.ModEnchantments;
import com.sam_mc.travelyourearth.item.ModItems;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterials;
import net.minecraft.world.item.equipment.trim.TrimPatterns;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

import java.util.Optional;

/**
 * Ruby gear for ominous trial chamber mobs (equipment/trial_chamber_* tables).
 *   1. Armor: with armor_chance, the whole vanilla set becomes ruby (enchantments kept, copper + flow trim).
 *   2. Melee weapon: with weapon_chance, the vanilla sword becomes a ruby axe or spear (enchantments kept).
 * Added in ModGlobalLootModifierProvider.
 */
public class RubyTrialGearLootModifier extends LootModifier {

    public static final MapCodec<RubyTrialGearLootModifier> CODEC = RecordCodecBuilder.mapCodec(inst ->
            LootModifier.codecStart(inst).and(inst.group(
                    Codec.floatRange(0.0F, 1.0F).fieldOf("armor_chance").forGetter(m -> m.armorChance),
                    Codec.floatRange(0.0F, 1.0F).fieldOf("weapon_chance").forGetter(m -> m.weaponChance)
            )).apply(inst, RubyTrialGearLootModifier::new)
    );

    private final float armorChance;
    private final float weaponChance;

    /**
     * 1.2: chance of ANY chestplate (iron, diamond or ruby) coming with Cowardice, and of the ruby
     * spear coming with Harpoon (30%).
     */
    private static final float SPECIAL_ENCHANT_CHANCE = 0.30F;

    /** Fixed levels, like vanilla's trial gear (always Protection IV...): Cowardice III, Harpoon II. */
    private static final int COWARDICE_LEVEL = 3;
    private static final int HARPOON_LEVEL = 2;

    // 26.3: same change as in LootModifier / ReplaceWithItemLootModifier -
    // a single optional condition wrapped in a Holder, instead of an array.
    public RubyTrialGearLootModifier(Optional<Holder<LootItemCondition>> condition, int priority,
                                     float armorChance, float weaponChance) {
        super(condition, priority);
        this.armorChance = armorChance;
        this.weaponChance = weaponChance;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }

    // =========================================================================
    // Logic
    // =========================================================================

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
        RandomSource random = context.getRandom();

        // 1) ARMOR: if vanilla generated pieces, with some chance the whole set becomes ruby
        boolean hasArmor = false;
        for (ItemStack stack : loot) {
            if (armorSlotOf(stack) != null) {
                hasArmor = true;
                break;
            }
        }
        if (hasArmor && random.nextFloat() < armorChance) {
            RegistryAccess registries = context.getLevel().registryAccess();
            for (int i = 0; i < loot.size(); i++) {
                ItemStack original = loot.get(i);
                EquipmentSlot slot = armorSlotOf(original);
                if (slot != null) {
                    loot.set(i, toRubyArmor(original, slot, registries));
                }
            }
        }

        // 1b) COWARDICE: ANY chestplate (iron, diamond or ruby), same chance for all of them
        for (ItemStack stack : loot) {
            if (armorSlotOf(stack) == EquipmentSlot.CHEST && random.nextFloat() < SPECIAL_ENCHANT_CHANCE) {
                addModEnchantment(stack, ModEnchantments.COWARDICE, COWARDICE_LEVEL, context.getLevel().registryAccess());
            }
        }

        // 2) MELEE WEAPON: the vanilla sword becomes a ruby axe or spear, keeping its enchantments
        if (weaponChance > 0.0F && random.nextFloat() < weaponChance) {
            for (int i = 0; i < loot.size(); i++) {
                ItemStack original = loot.get(i);
                Item item = original.getItem();
                if (item == Items.IRON_SWORD || item == Items.DIAMOND_SWORD) {
                    Item rubyItem = random.nextBoolean() ? ModItems.RUBY_AXE.get() : ModItems.RUBY_SPEAR.get();
                    ItemStack rubyWeapon = new ItemStack(rubyItem);

                    var enchantments = original.get(DataComponents.ENCHANTMENTS);
                    if (enchantments != null && !enchantments.isEmpty()) {
                        rubyWeapon.set(DataComponents.ENCHANTMENTS, enchantments);
                    }
                    // Harpoon pulls, Knockback pushes: a spear that kept vanilla's Knockback I never gets Harpoon
                    if (rubyItem == ModItems.RUBY_SPEAR.get() && !hasEnchantment(rubyWeapon, Enchantments.KNOCKBACK)
                            && random.nextFloat() < SPECIAL_ENCHANT_CHANCE) {
                        addModEnchantment(rubyWeapon, ModEnchantments.HARPOON, HARPOON_LEVEL, context.getLevel().registryAccess());
                    }

                    loot.set(i, rubyWeapon);
                    break;
                }
            }
        }
        return loot;
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    /** Adds one of the mod's enchantments at a fixed level, keeping the others. */
    private static void addModEnchantment(ItemStack stack, ResourceKey<Enchantment> key, int level,
                                          RegistryAccess registries) {
        registries.lookupOrThrow(Registries.ENCHANTMENT).get(key)
                .ifPresent(enchantment -> stack.enchant(enchantment, level));
    }

    /** True if the item already has this enchantment (any level). */
    private static boolean hasEnchantment(ItemStack stack, ResourceKey<Enchantment> key) {
        return stack.getEnchantments().keySet().stream().anyMatch(holder -> holder.is(key));
    }

    private static ItemStack toRubyArmor(ItemStack original, EquipmentSlot slot, RegistryAccess registries) {
        ItemStack ruby = new ItemStack(rubyArmorFor(slot));

        // Ominous trial enchantments (Protection IV, Fire Protection IV, Projectile Protection IV)
        var enchantments = original.get(DataComponents.ENCHANTMENTS);
        if (enchantments != null && !enchantments.isEmpty()) {
            ruby.set(DataComponents.ENCHANTMENTS, enchantments);
        }

        // Armor trim: flow + copper, like the diamond one
        var materials = registries.lookupOrThrow(Registries.TRIM_MATERIAL);
        var patterns = registries.lookupOrThrow(Registries.TRIM_PATTERN);
        ruby.set(DataComponents.TRIM, new ArmorTrim(
                materials.getOrThrow(TrimMaterials.COPPER),
                patterns.getOrThrow(TrimPatterns.FLOW)));

        return ruby;
    }

    // Armor slot if the item is a real armor piece (excludes pumpkins, heads, etc.)
    private static EquipmentSlot armorSlotOf(ItemStack stack) {
        if (stack.isEmpty() || !stack.isDamageableItem()) return null;
        var equippable = stack.get(DataComponents.EQUIPPABLE);
        if (equippable == null) return null;
        EquipmentSlot slot = equippable.slot();
        boolean isArmorSlot = slot == EquipmentSlot.HEAD || slot == EquipmentSlot.CHEST
                || slot == EquipmentSlot.LEGS || slot == EquipmentSlot.FEET;
        return isArmorSlot ? slot : null;
    }

    private static Item rubyArmorFor(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> ModItems.RUBY_HELMET.get();
            case CHEST -> ModItems.RUBY_CHESTPLATE.get();
            case LEGS -> ModItems.RUBY_LEGGINGS.get();
            case FEET -> ModItems.RUBY_BOOTS.get();
            default -> throw new IllegalArgumentException("Not an armor slot: " + slot);
        };
    }
}