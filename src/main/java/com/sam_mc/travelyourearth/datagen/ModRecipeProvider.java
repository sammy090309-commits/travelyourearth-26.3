package com.sam_mc.travelyourearth.datagen;

import com.sam_mc.travelyourearth.TravelYourEarth;
import com.sam_mc.travelyourearth.block.ModBlocks;
import com.sam_mc.travelyourearth.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    public static class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
            super(packOutput, registries);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new ModRecipeProvider(registries, output);
        }

        @Override
        public String getName() {
            return "Travel Your Earth Recipes";
        }
    }

    @Override
    protected void buildRecipes() {
        // Receta de Bloque de Rubí (3x3 rubíes)
        shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.RUBY_BLOCK.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.RUBY.get())
                .unlockedBy(getHasName(ModItems.RUBY.get()), has(ModItems.RUBY.get()))
                .group("ruby")
                .save(output);

        // Descrafto de Bloque de Rubí -> 9 Rubíes
        shapeless(RecipeCategory.MISC, ModItems.RUBY.get(), 9)
                .requires(ModBlocks.RUBY_BLOCK.get())
                .unlockedBy(getHasName(ModBlocks.RUBY_BLOCK.get()), has(ModBlocks.RUBY_BLOCK.get()))
                .group("ruby")
                .save(output);

        // Espada de Rubí
        shaped(RecipeCategory.COMBAT, ModItems.RUBY_SWORD.get())
                .pattern("A")
                .pattern("A")
                .pattern("S")
                .define('A', ModItems.RUBY.get())
                .define('S', Items.STICK)
                .unlockedBy(getHasName(ModItems.RUBY.get()), has(ModItems.RUBY.get()))
                .unlockedBy(getHasName(Items.STICK), has(Items.STICK))
                .group("ruby")
                .save(output);

        // Pico de Rubí
        shaped(RecipeCategory.TOOLS, ModItems.RUBY_PICKAXE.get())
                .pattern("AAA")
                .pattern(" S ")
                .pattern(" S ")
                .define('A', ModItems.RUBY.get())
                .define('S', Items.STICK)
                .unlockedBy(getHasName(ModItems.RUBY.get()), has(ModItems.RUBY.get()))
                .unlockedBy(getHasName(Items.STICK), has(Items.STICK))
                .group("ruby")
                .save(output);

        // Pala de Rubí
        shaped(RecipeCategory.TOOLS, ModItems.RUBY_SHOVEL.get())
                .pattern("A")
                .pattern("S")
                .pattern("S")
                .define('A', ModItems.RUBY.get())
                .define('S', Items.STICK)
                .unlockedBy(getHasName(ModItems.RUBY.get()), has(ModItems.RUBY.get()))
                .unlockedBy(getHasName(Items.STICK), has(Items.STICK))
                .group("ruby")
                .save(output);

        // Hacha de Rubí
        shaped(RecipeCategory.TOOLS, ModItems.RUBY_AXE.get())
                .pattern("AA")
                .pattern("SA")
                .pattern("S ")
                .define('A', ModItems.RUBY.get())
                .define('S', Items.STICK)
                .unlockedBy(getHasName(ModItems.RUBY.get()), has(ModItems.RUBY.get()))
                .unlockedBy(getHasName(Items.STICK), has(Items.STICK))
                .group("ruby")
                .save(output);

        // Azada de Rubí
        shaped(RecipeCategory.TOOLS, ModItems.RUBY_HOE.get())
                .pattern("AA")
                .pattern("S ")
                .pattern("S ")
                .define('A', ModItems.RUBY.get())
                .define('S', Items.STICK)
                .unlockedBy(getHasName(ModItems.RUBY.get()), has(ModItems.RUBY.get()))
                .unlockedBy(getHasName(Items.STICK), has(Items.STICK))
                .group("ruby")
                .save(output);

        // Lanza de Rubí
        shaped(RecipeCategory.COMBAT, ModItems.RUBY_SPEAR.get())
                .pattern("  A")
                .pattern(" S ")
                .pattern("S  ")
                .define('A', ModItems.RUBY.get())
                .define('S', Items.STICK)
                .unlockedBy(getHasName(ModItems.RUBY.get()), has(ModItems.RUBY.get()))
                .unlockedBy(getHasName(Items.STICK), has(Items.STICK))
                .group("ruby")
                .save(output);


        shaped(RecipeCategory.COMBAT, ModItems.RUBY_HELMET.get())
                .pattern("AAA")
                .pattern("A A")
                .define('A', ModItems.RUBY.get())
                .unlockedBy(getHasName(ModItems.RUBY.get()), has(ModItems.RUBY))
                .group("ruby")
                .save(output);

        shaped(RecipeCategory.COMBAT, ModItems.RUBY_CHESTPLATE.get())
                .pattern("A A")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.RUBY.get())
                .unlockedBy(getHasName(ModItems.RUBY.get()), has(ModItems.RUBY))
                .group("ruby")
                .save(output);

        shaped(RecipeCategory.COMBAT, ModItems.RUBY_LEGGINGS.get())
                .pattern("AAA")
                .pattern("A A")
                .pattern("A A")
                .define('A', ModItems.RUBY.get())
                .unlockedBy(getHasName(ModItems.RUBY.get()), has(ModItems.RUBY))
                .group("ruby")
                .save(output);

        shaped(RecipeCategory.COMBAT, ModItems.RUBY_BOOTS.get())
                .pattern("A A")
                .pattern("A A")
                .define('A', ModItems.RUBY.get())
                .unlockedBy(getHasName(ModItems.RUBY.get()), has(ModItems.RUBY))
                .group("ruby")
                .save(output);


        // Fundición de minerales (Horno y Alto Horno)
        List<ItemLike> RUBY_SMELTABLES = List.of(
                ModBlocks.RUBY_ORE.get(),
                ModBlocks.DEEPSLATE_RUBY_ORE.get()





        );

        oreSmelting(RUBY_SMELTABLES, RecipeCategory.MISC, CookingBookCategory.MISC, ModItems.RUBY.get(), 0.25f, 200, "ruby");
        oreBlasting(RUBY_SMELTABLES, RecipeCategory.MISC, CookingBookCategory.MISC, ModItems.RUBY.get(), 0.25f, 100, "ruby");
    }

    @Override
    protected <T extends AbstractCookingRecipe> void oreCooking(AbstractCookingRecipe.Factory<T> factory, List<ItemLike> smeltables,
                                                                RecipeCategory craftingCategory, CookingBookCategory cookingCategory, ItemLike result,
                                                                float experience, int cookingTime, String group, String fromDesc) {
        for (ItemLike itemlike : smeltables) {
            SimpleCookingRecipeBuilder.generic(Ingredient.of(itemlike), craftingCategory, cookingCategory, result, experience, cookingTime, factory)
                    .group(group)
                    .unlockedBy(getHasName(itemlike), has(itemlike))
                    .save(output, TravelYourEarth.MODID + ":" + getItemName(result) + fromDesc + "_" + getItemName(itemlike));
        }
    }
}