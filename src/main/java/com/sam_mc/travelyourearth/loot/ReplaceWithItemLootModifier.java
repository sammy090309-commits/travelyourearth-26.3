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

public class ReplaceWithItemLootModifier extends LootModifier {

    public static final MapCodec<ReplaceWithItemLootModifier> CODEC =
            RecordCodecBuilder.mapCodec(inst -> codecStart(inst)
                    .and(BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(m -> m.item))
                    .and(Codec.FLOAT.fieldOf("chance").forGetter(m -> m.chance))
                    .apply(inst, ReplaceWithItemLootModifier::new));

    private final Item item;
    private final float chance;

    // ANTES (26.2): public ReplaceWithItemLootModifier(LootItemCondition[] conditions, int priority, Item item, float chance)
    // AHORA (26.3): LootModifier ya no recibe un array de condiciones, recibe
    // una sola condicion opcional envuelta en Holder (Optional<Holder<LootItemCondition>>).
    // Verificado contra el codigo fuente actual de LootModifier en NeoForge 26.3.
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