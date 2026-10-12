package com.sam_mc.travelyourearth.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sam_mc.travelyourearth.event.SavannaVillagerEvents;
import com.sam_mc.travelyourearth.item.ModItems;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Chests of SAVANNA VILLAGES ("the country of the ruby").
 *
 * Only works when the chest is really inside a savanna village (checked with the structure at
 * the chest's position), so the same vanilla table (e.g. village_weaponsmith) in a plains
 * village stays 100% vanilla.
 *
 * 1. Diamond -> ruby: diamonds, diamond blocks and diamond gear become their ruby version
 *    (same amount, keeps enchantments/damage).
 * 2. Rubies very common: for every emerald stack in the chest there's a
 *    "ruby_chance" (0.75) chance of ALSO getting a ruby stack of the same size.
 * 3. Ruby gear "like the iron, but rarer": for every IRON tool/weapon/armor piece in the chest
 *    there's a "gear_chance" (0.35) chance of ALSO getting its ruby version. So the ruby gear
 *    shows up in the same chests as the iron (weaponsmith, toolsmith, armorer...), just less often.
 */
public class SavannaVillageChestModifier extends LootModifier {

    public static final MapCodec<SavannaVillageChestModifier> CODEC = RecordCodecBuilder.mapCodec(inst ->
            codecStart(inst)
                    .and(Codec.floatRange(0.0F, 1.0F).fieldOf("ruby_chance").forGetter(m -> m.rubyChance))
                    .and(Codec.floatRange(0.0F, 1.0F).fieldOf("gear_chance").forGetter(m -> m.gearChance))
                    .apply(inst, SavannaVillageChestModifier::new));

    /** Chance per emerald stack of also adding rubies (0.75 = 3 of every 4 emerald stacks). */
    private final float rubyChance;

    /** Chance per iron gear piece of also adding its ruby version (less likely than the iron). */
    private final float gearChance;

    public SavannaVillageChestModifier(Optional<Holder<LootItemCondition>> conditions, int priority,
                                       float rubyChance, float gearChance) {
        super(conditions, priority);
        this.rubyChance = rubyChance;
        this.gearChance = gearChance;
    }

    /** Iron gear -> its ruby version. Built on demand (the mod's items must be registered). */
    private static Map<Item, Supplier<? extends ItemLike>> ironToRuby() {
        return Map.ofEntries(
                Map.entry(Items.IRON_SWORD, ModItems.RUBY_SWORD),
                Map.entry(Items.IRON_SPEAR, ModItems.RUBY_SPEAR),
                Map.entry(Items.IRON_AXE, ModItems.RUBY_AXE),
                Map.entry(Items.IRON_PICKAXE, ModItems.RUBY_PICKAXE),
                Map.entry(Items.IRON_SHOVEL, ModItems.RUBY_SHOVEL),
                Map.entry(Items.IRON_HOE, ModItems.RUBY_HOE),
                Map.entry(Items.IRON_HELMET, ModItems.RUBY_HELMET),
                Map.entry(Items.IRON_CHESTPLATE, ModItems.RUBY_CHESTPLATE),
                Map.entry(Items.IRON_LEGGINGS, ModItems.RUBY_LEGGINGS),
                Map.entry(Items.IRON_BOOTS, ModItems.RUBY_BOOTS));
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (!isInSavannaVillage(context)) return generatedLoot;

        Map<Item, Supplier<? extends ItemLike>> rubyVersions = SavannaVillagerEvents.diamondToRuby();
        Map<Item, Supplier<? extends ItemLike>> ironVersions = ironToRuby();
        ObjectArrayList<ItemStack> extraRubies = new ObjectArrayList<>();

        for (int i = 0; i < generatedLoot.size(); i++) {
            ItemStack stack = generatedLoot.get(i);
            if (stack.isEmpty()) continue;

            // 1. Diamond -> ruby
            Item ruby = SavannaVillagerEvents.rubyItem(stack.getItem(), rubyVersions);
            if (ruby != null) {
                generatedLoot.set(i, stack.transmuteCopy(ruby, stack.getCount()));
                continue;
            }

            // 2. Emeralds -> "ruby_chance" of rubies too
            if (stack.is(Items.EMERALD) && context.getRandom().nextFloat() < rubyChance) {
                extraRubies.add(new ItemStack(ModItems.RUBY.get(), stack.getCount()));
            }

            // 3. Iron gear -> sometimes ALSO its ruby version (like the iron, but rarer)
            Supplier<? extends ItemLike> rubyGear = ironVersions.get(stack.getItem());
            if (rubyGear != null && context.getRandom().nextFloat() < gearChance) {
                extraRubies.add(new ItemStack(rubyGear.get()));
            }
        }

        generatedLoot.addAll(extraRubies);
        return generatedLoot;
    }

    /** True if the chest (the ORIGIN of the loot) is inside a piece of a savanna village. */
    private static boolean isInSavannaVillage(LootContext context) {
        Vec3 origin = context.getOptional(LootContextParams.ORIGIN); // 26.3 name (was getOptionalParameter)
        if (origin == null) return false;

        ServerLevel level = context.getLevel();
        StructureStart start = level.structureManager().getStructureWithPieceAt(
                BlockPos.containing(origin),
                structure -> structure.is(BuiltinStructures.VILLAGE_SAVANNA));
        return start.isValid();
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}