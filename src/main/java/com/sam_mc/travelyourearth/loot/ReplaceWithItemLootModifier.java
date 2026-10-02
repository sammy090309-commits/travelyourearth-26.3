package com.sam_mc.travelyourearth.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

import java.util.Optional;

/**
 * Loot modifier that, with "chance", REPLACES all the generated loot with 1 "item".
 * Used for archaeology (suspicious sand / gravel), where each block gives a single item.
 */
public class ReplaceWithItemLootModifier extends LootModifier {

    public static final MapCodec<ReplaceWithItemLootModifier> CODEC =
            RecordCodecBuilder.mapCodec(inst -> codecStart(inst)
                    .and(BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(m -> m.item))
                    .and(Codec.FLOAT.fieldOf("chance").forGetter(m -> m.chance))
                    .apply(inst, ReplaceWithItemLootModifier::new));

    private final Item item;
    private final float chance;

    // 26.3: LootModifier no longer receives an array of conditions, it receives
    // a single optional condition wrapped in a Holder (Optional<Holder<LootItemCondition>>).
    public ReplaceWithItemLootModifier(Optional<Holder<LootItemCondition>> condition, int priority, Item item, float chance) {
        super(condition, priority);
        this.item = item;
        this.chance = chance;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (context.getRandom().nextFloat() < chance) {
            ObjectArrayList<ItemStack> replacement = new ObjectArrayList<>();
            replacement.add(new ItemStack(item));
            return replacement;
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}