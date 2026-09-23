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

public class ModConfiguredFeatures {
    public static final ResourceKey<Feature> RUBY_ORE_KEY = registerKey("ruby_ore");
    public static final ResourceKey<Feature> MOUNTAIN_RUBY_ORE_KEY = registerKey("mountain_ruby_ore");

    public static void bootstrap(BootstrapContext<Feature> context) {
        RuleTest stoneReplaceables = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
        RuleTest deepslateReplaceables = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);

        context.register(RUBY_ORE_KEY, new OreFeature(
                List.of(
                        new BlockReplacement(stoneReplaceables, ModBlocks.RUBY_ORE.get().defaultBlockState()),
                        new BlockReplacement(deepslateReplaceables, ModBlocks.DEEPSLATE_RUBY_ORE.get().defaultBlockState())
                ),
                3
        ));

        context.register(MOUNTAIN_RUBY_ORE_KEY, new OreFeature(
                List.of(
                        new BlockReplacement(stoneReplaceables, ModBlocks.RUBY_ORE.get().defaultBlockState()),
                        new BlockReplacement(deepslateReplaceables, ModBlocks.DEEPSLATE_RUBY_ORE.get().defaultBlockState())
                ),
                8
        ));
    }

    public static ResourceKey<Feature> registerKey(String name) {
        return ResourceKey.create(Registries.FEATURE, Identifier.fromNamespaceAndPath(TravelYourEarth.MODID, name));
    }
}