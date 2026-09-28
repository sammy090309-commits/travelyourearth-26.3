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
    // Qué forma y tamaño tiene cada veta de rubí
    public static final ResourceKey<Feature> RUBY_ORE_KEY = registerKey("ruby_ore");
    public static final ResourceKey<Feature> MOUNTAIN_RUBY_ORE_KEY = registerKey("mountain_ruby_ore");
    public static final ResourceKey<Feature> DAPPLED_RUBY_ORE_KEY = registerKey("dappled_ruby_ore");

    public static void bootstrap(BootstrapContext<Feature> context) {
        RuleTest stoneReplaceables = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
        RuleTest deepslateReplaceables = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);

        // En piedra → mena de rubí; en pizarra profunda → mena de rubí de pizarra
        List<BlockReplacement> rubyTargets = List.of(
                new BlockReplacement(stoneReplaceables, ModBlocks.RUBY_ORE.get().defaultBlockState()),
                new BlockReplacement(deepslateReplaceables, ModBlocks.DEEPSLATE_RUBY_ORE.get().defaultBlockState())
        );

        // 1. TODOS LOS BIOMAS: igual que la veta principal del diamante (size 4 → 1 a 5 menas,
        //    promedio ≈ 2) y 50% de descarte si toca aire (cuevas).
        context.register(RUBY_ORE_KEY, new OreFeature(rubyTargets, 5, 0.5f));

        // 2. MONTAÑAS: mismas vetas que el diamante (size 4, 50% de descarte con aire).
        context.register(MOUNTAIN_RUBY_ORE_KEY, new OreFeature(rubyTargets, 8, 0.5f));

        // 3. BOSQUE MOTEADO: size 8 → normalmente 4 a 8 menas por veta (promedio ≈ 5.7).
        context.register(DAPPLED_RUBY_ORE_KEY, new OreFeature(rubyTargets, 3));
    }

    public static ResourceKey<Feature> registerKey(String name) {
        return ResourceKey.create(Registries.FEATURE, Identifier.fromNamespaceAndPath(TravelYourEarth.MODID, name));
    }
}