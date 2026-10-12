package com.sam_mc.travelyourearth.worldgen;

import com.mojang.datafixers.util.Pair;
import com.sam_mc.travelyourearth.TravelYourEarth;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.data.worldgen.TrialChambersStructurePools;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentTable;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TrialSpawnerBlock;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerConfig;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasBinding;
import net.minecraft.world.level.levelgen.structure.pools.alias.RandomGroupPoolAlias;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.PosAlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProcessorRule;
import net.minecraft.world.level.levelgen.structure.templatesystem.RandomBlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity.AppendStatic;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 1.2.1: the PARCHED gets its own trial spawner in the trial chambers.
 *
 * Vanilla: each trial chamber picks ONE skeleton family for all its ranged spawners
 * (skeleton, stray or bogged), each with a normal and a slow spawner. The parched becomes the
 * 4th family (same weight: 1 of every 4 trial chambers).
 *
 * Everything is datagen (no hand-made NBT): the parched spawner REUSES the vanilla skeleton
 * spawner piece, and a processor changes it:
 *   - half of the bone blocks (random) -> chiseled sandstone (bone + desert, like the bogged is bone + swamp),
 *   - the trial spawner -> the parched configs (normal + ominous).
 *
 * Registries (TravelYourEarthDataGen): TRIAL_SPAWNER_CONFIG, PROCESSOR_LIST, TEMPLATE_POOL.
 * The structure minecraft:trial_chambers is REPLACED (ModTrialChambersStructureProvider): it's rebuilt
 * exactly like vanilla 26.3 (net.minecraft.data.worldgen.Structures) with the parched group added.
 */
public final class ModTrialChambers {

    /** Half of the bone blocks of the piece become chiseled sandstone. */
    private static final float SANDSTONE_CHANCE = 0.5F;

    // ---- Trial spawner configs ----
    public static final ResourceKey<TrialSpawnerConfig> RANGED_PARCHED_NORMAL = config("trial_chamber/ranged/parched/normal");
    public static final ResourceKey<TrialSpawnerConfig> RANGED_PARCHED_OMINOUS = config("trial_chamber/ranged/parched/ominous");
    public static final ResourceKey<TrialSpawnerConfig> SLOW_RANGED_PARCHED_NORMAL = config("trial_chamber/slow_ranged/parched/normal");
    public static final ResourceKey<TrialSpawnerConfig> SLOW_RANGED_PARCHED_OMINOUS = config("trial_chamber/slow_ranged/parched/ominous");

    // ---- Processor lists ----
    public static final ResourceKey<StructureProcessorList> RANGED_PARCHED_PROCESSOR = processor("trial_chambers/spawner/ranged/parched");
    public static final ResourceKey<StructureProcessorList> SLOW_RANGED_PARCHED_PROCESSOR = processor("trial_chambers/spawner/slow_ranged/parched");

    // ---- Template pools ----
    public static final ResourceKey<StructureTemplatePool> RANGED_PARCHED_POOL = pool("trial_chambers/spawner/ranged/parched");
    public static final ResourceKey<StructureTemplatePool> SLOW_RANGED_PARCHED_POOL = pool("trial_chambers/spawner/slow_ranged/parched");

    private ModTrialChambers() {}

    // =========================================================================
    // 1. TRIAL SPAWNER CONFIGS (same values as the vanilla skeleton ones, TrialSpawnerConfigs)
    // =========================================================================
    public static void bootstrapSpawnerConfigs(BootstrapContext<TrialSpawnerConfig> context) {
        context.register(RANGED_PARCHED_NORMAL,
                trialChamberBase().spawnPotentialsDefinition(WeightedList.of(spawnData(EntityTypes.PARCHED, null))).build());
        context.register(RANGED_PARCHED_OMINOUS,
                trialChamberBase().lootTablesToEject(ominousLoot())
                        .spawnPotentialsDefinition(WeightedList.of(spawnData(EntityTypes.PARCHED, BuiltInLootTables.EQUIPMENT_TRIAL_CHAMBER_RANGED)))
                        .build());
        context.register(SLOW_RANGED_PARCHED_NORMAL,
                trialChamberSlowRanged().spawnPotentialsDefinition(WeightedList.of(spawnData(EntityTypes.PARCHED, null))).build());
        context.register(SLOW_RANGED_PARCHED_OMINOUS,
                trialChamberSlowRanged().lootTablesToEject(ominousLoot())
                        .spawnPotentialsDefinition(WeightedList.of(spawnData(EntityTypes.PARCHED, BuiltInLootTables.EQUIPMENT_TRIAL_CHAMBER_RANGED)))
                        .build());
    }

    private static TrialSpawnerConfig.Builder trialChamberBase() {
        return TrialSpawnerConfig.builder().simultaneousMobs(3.0F).simultaneousMobsAddedPerPlayer(0.5F).ticksBetweenSpawn(20);
    }

    private static TrialSpawnerConfig.Builder trialChamberSlowRanged() {
        return TrialSpawnerConfig.builder().simultaneousMobs(4.0F).simultaneousMobsAddedPerPlayer(2.0F).ticksBetweenSpawn(160);
    }

    private static WeightedList<ResourceKey<LootTable>> ominousLoot() {
        return WeightedList.<ResourceKey<LootTable>>builder()
                .add(BuiltInLootTables.SPAWNER_OMINOUS_TRIAL_CHAMBER_KEY, 3)
                .add(BuiltInLootTables.SPAWNER_OMINOUS_TRIAL_CHAMBER_CONSUMABLES, 7)
                .build();
    }

    /** Same as vanilla TrialSpawnerConfigs.customSpawnDataWithEquipment (equipment never drops: 0.0). */
    private static SpawnData spawnData(EntityType<?> type, ResourceKey<LootTable> equipment) {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(type).toString());
        Optional<EquipmentTable> table = Optional.ofNullable(equipment).map(loot -> new EquipmentTable(loot, 0.0F));
        return new SpawnData(tag, Optional.empty(), table);
    }

    // =========================================================================
    // 2. PROCESSOR LISTS: skeleton spawner piece -> parched spawner piece
    // =========================================================================
    public static void bootstrapProcessors(BootstrapContext<StructureProcessorList> context) {
        context.register(RANGED_PARCHED_PROCESSOR, parchedProcessor(RANGED_PARCHED_NORMAL, RANGED_PARCHED_OMINOUS));
        context.register(SLOW_RANGED_PARCHED_PROCESSOR, parchedProcessor(SLOW_RANGED_PARCHED_NORMAL, SLOW_RANGED_PARCHED_OMINOUS));
    }

    private static StructureProcessorList parchedProcessor(ResourceKey<TrialSpawnerConfig> normal, ResourceKey<TrialSpawnerConfig> ominous) {
        // The trial spawner keeps the vanilla piece's state (waiting for players, not ominous)
        // and gets the parched configs (they REPLACE the skeleton ones in its block entity data)
        CompoundTag configs = new CompoundTag();
        configs.putString("normal_config", normal.identifier().toString());
        configs.putString("ominous_config", ominous.identifier().toString());

        return new StructureProcessorList(List.of(new RuleProcessor(List.of(
                // Bone + desert: half of the bone blocks -> chiseled sandstone
                new ProcessorRule(
                        new RandomBlockMatchTest(Blocks.BONE_BLOCK, SANDSTONE_CHANCE),
                        AlwaysTrueTest.INSTANCE,
                        Blocks.CHISELED_SANDSTONE.defaultBlockState()),
                // Skeleton spawner -> parched spawner
                new ProcessorRule(
                        new BlockMatchTest(Blocks.TRIAL_SPAWNER),
                        AlwaysTrueTest.INSTANCE,
                        PosAlwaysTrueTest.INSTANCE,
                        Blocks.TRIAL_SPAWNER.defaultBlockState()
                                .setValue(TrialSpawnerBlock.STATE, TrialSpawnerState.WAITING_FOR_PLAYERS)
                                .setValue(TrialSpawnerBlock.OMINOUS, false),
                        new AppendStatic(configs))
        ))));
    }

    // =========================================================================
    // 3. TEMPLATE POOLS: the vanilla skeleton spawner pieces + the parched processors
    // =========================================================================
    public static void bootstrapPools(BootstrapContext<StructureTemplatePool> context) {
        HolderGetter<StructureTemplatePool> pools = context.lookup(Registries.TEMPLATE_POOL);
        HolderGetter<StructureProcessorList> processors = context.lookup(Registries.PROCESSOR_LIST);
        var empty = pools.getOrThrow(Pools.EMPTY);

        context.register(RANGED_PARCHED_POOL, new StructureTemplatePool(empty,
                List.of(Pair.of(StructurePoolElement.single(
                        "minecraft:" + TrialChambersStructurePools.spawner("ranged/skeleton"),
                        processors.getOrThrow(RANGED_PARCHED_PROCESSOR)), 1)),
                StructureTemplatePool.Projection.RIGID));

        context.register(SLOW_RANGED_PARCHED_POOL, new StructureTemplatePool(empty,
                List.of(Pair.of(StructurePoolElement.single(
                        "minecraft:" + TrialChambersStructurePools.spawner("slow_ranged/skeleton"),
                        processors.getOrThrow(SLOW_RANGED_PARCHED_PROCESSOR)), 1)),
                StructureTemplatePool.Projection.RIGID));
    }

    // =========================================================================
    // 4. STRUCTURE: minecraft:trial_chambers = vanilla 26.3 + the parched group
    // =========================================================================
    // NeoForge's registry datagen (GatherDataRegistryEntriesEvent) only writes the mod's own
    // namespace, so this one is written by ModTrialChambersStructureProvider instead.

    /** The trial chambers structure, copied from net.minecraft.data.worldgen.Structures (26.3); only the alias bindings change. */
    public static Structure createTrialChambers(HolderGetter<Biome> biomes, HolderGetter<StructureTemplatePool> templates) {
        return new JigsawStructure(
                new Structure.StructureSettings.Builder(biomes.getOrThrow(BiomeTags.HAS_TRIAL_CHAMBERS))
                        .generationStep(GenerationStep.Decoration.UNDERGROUND_STRUCTURES)
                        .terrainAdapation(TerrainAdjustment.ENCAPSULATE)
                        .spawnOverrides(
                                Arrays.stream(MobCategory.values())
                                        .collect(Collectors.toMap(
                                                c -> (MobCategory) c,
                                                c -> new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.PIECE, WeightedList.of()))))
                        .build(),
                templates.getOrThrow(TrialChambersStructurePools.START),
                Optional.empty(),
                20,
                UniformHeight.of(VerticalAnchor.absolute(-40), VerticalAnchor.absolute(-20)),
                false,
                Optional.empty(),
                new JigsawStructure.MaxDistance(116),
                aliasBindingsWithParched(),
                new DimensionPadding(10),
                LiquidSettings.IGNORE_WATERLOGGING);
    }

    /**
     * The vanilla alias bindings (TrialChambersStructurePools.ALIAS_BINDINGS) with one more group in
     * the "skeleton family" random group: parched (ranged + slow ranged), weight 1 like the others.
     */
    private static List<PoolAliasBinding> aliasBindingsWithParched() {
        List<PoolAliasBinding> result = new ArrayList<>();
        for (PoolAliasBinding binding : TrialChambersStructurePools.ALIAS_BINDINGS) {
            if (binding instanceof RandomGroupPoolAlias group) {
                WeightedList.Builder<List<PoolAliasBinding>> groups = WeightedList.builder();
                group.groups().unwrap().forEach(entry -> groups.add(entry.value(), entry.weight()));
                groups.add(List.of(
                        PoolAliasBinding.direct(Pools.createKey(TrialChambersStructurePools.spawner("contents/ranged")), RANGED_PARCHED_POOL),
                        PoolAliasBinding.direct(Pools.createKey(TrialChambersStructurePools.spawner("contents/slow_ranged")), SLOW_RANGED_PARCHED_POOL)
                ), 1);
                result.add(PoolAliasBinding.randomGroup(groups.build()));
            } else {
                result.add(binding);
            }
        }
        return List.copyOf(result);
    }

    // =========================================================================
    // KEYS
    // =========================================================================
    private static ResourceKey<TrialSpawnerConfig> config(String path) {
        return ResourceKey.create(Registries.TRIAL_SPAWNER_CONFIG, Identifier.fromNamespaceAndPath(TravelYourEarth.MODID, path));
    }

    private static ResourceKey<StructureProcessorList> processor(String path) {
        return ResourceKey.create(Registries.PROCESSOR_LIST, Identifier.fromNamespaceAndPath(TravelYourEarth.MODID, path));
    }

    private static ResourceKey<StructureTemplatePool> pool(String path) {
        return ResourceKey.create(Registries.TEMPLATE_POOL, Identifier.fromNamespaceAndPath(TravelYourEarth.MODID, path));
    }
}
