package com.sam_mc.travelyourearth.loot;

import com.sam_mc.travelyourearth.pot.PotMobs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

import java.util.Optional;

/**
 * 1.2.1: with "chance", min..max mobs of "entity" come out of the container when its loot is rolled.
 * Used for the decorated pots of the trial chambers (bats and silverfish): their loot is rolled
 * when the pot breaks, so the mobs jump out of the broken pot (PotMobs.popOut). The normal loot is NOT changed.
 */
public class SpawnMobsLootModifier extends LootModifier {

    public static final MapCodec<SpawnMobsLootModifier> CODEC =
            RecordCodecBuilder.mapCodec(inst -> codecStart(inst)
                    .and(BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("entity").forGetter(m -> m.entity))
                    .and(Codec.FLOAT.fieldOf("chance").forGetter(m -> m.chance))
                    .and(Codec.INT.fieldOf("min").forGetter(m -> m.min))
                    .and(Codec.INT.fieldOf("max").forGetter(m -> m.max))
                    .apply(inst, SpawnMobsLootModifier::new));

    private final EntityType<?> entity;
    private final float chance;
    private final int min;
    private final int max;

    public SpawnMobsLootModifier(Optional<Holder<LootItemCondition>> condition, int priority,
                                 EntityType<?> entity, float chance, int min, int max) {
        super(condition, priority);
        this.entity = entity;
        this.chance = chance;
        this.min = min;
        this.max = Math.max(min, max);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (context.getRandom().nextFloat() >= chance) return generatedLoot;

        Vec3 origin = context.getOptional(LootContextParams.ORIGIN);
        if (origin == null) return generatedLoot;

        ServerLevel level = context.getLevel();
        BlockPos pos = BlockPos.containing(origin);
        int count = min + context.getRandom().nextInt(max - min + 1);
        for (int i = 0; i < count; i++) {
            Entity mob = entity.spawn(level, pos, EntitySpawnReason.TRIGGERED);
            if (mob != null) {
                // Out of the broken pot: a little above it, spread around if there's more than one
                PotMobs.popOut(mob, pos, context.getRandom(), count > 1);
            }
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
