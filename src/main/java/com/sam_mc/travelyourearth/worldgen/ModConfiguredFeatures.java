package com.sam_mc.travelyourearth.worldgen;

import com.sam_mc.travelyourearth.TravelYourEarth;
import com.sam_mc.travelyourearth.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.levelgen.feature.BlockReplacement;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.OreFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

import java.util.List;

/**
 * Shape and size of each ruby vein.
 *   OreFeature(targets, size, discardChanceOnAirExposure)
 *   - size: how big the vein can get (vanilla diamond main vein: 4)
 *   - discard: chance of removing an ore that touches air, like caves (vanilla diamond main vein: 0.5)
 */
public class ModConfiguredFeatures {

    public static final ResourceKey<Feature> RUBY_ORE_KEY = registerKey("ruby_ore");
    public static final ResourceKey<Feature> MOUNTAIN_RUBY_ORE_KEY = registerKey("mountain_ruby_ore");
    public static final ResourceKey<Feature> DAPPLED_RUBY_ORE_KEY = registerKey("dappled_ruby_ore");

    public static void bootstrap(BootstrapContext<Feature> context) {
        RuleTest stoneReplaceables = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
        RuleTest deepslateReplaceables = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);

        // In stone -> ruby ore; in deepslate -> deepslate ruby ore
        List<BlockReplacement> rubyTargets = List.of(
                new BlockReplacement(stoneReplaceables, ModBlocks.RUBY_ORE.get().defaultBlockState()),
                new BlockReplacement(deepslateReplaceables, ModBlocks.DEEPSLATE_RUBY_ORE.get().defaultBlockState())
        );

        // 1. ALL BIOMES: size 5 (a bit bigger than the diamond's 4), 50% discard when touching air.
        context.register(RUBY_ORE_KEY, new OreFeature(rubyTargets, 5, 0.5f));

        // 2. MOUNTAINS: size 8 (twice the diamond's 4), 30% discard when touching air.
        context.register(MOUNTAIN_RUBY_ORE_KEY, new OreFeature(rubyTargets, 8, 0.3f));

        // 3. DAPPLED FOREST: size 3 (small veins), no discard.
        context.register(DAPPLED_RUBY_ORE_KEY, new OreFeature(rubyTargets, 3));
    }

    public static ResourceKey<Feature> registerKey(String name) {
        return ResourceKey.create(Registries.FEATURE, Identifier.fromNamespaceAndPath(TravelYourEarth.MODID, name));
    }
}