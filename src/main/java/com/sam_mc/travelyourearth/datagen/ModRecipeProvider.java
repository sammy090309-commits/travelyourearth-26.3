package com.sam_mc.travelyourearth.datagen;

import com.sam_mc.travelyourearth.TravelYourEarth;
import com.sam_mc.travelyourearth.block.ModBlocks;
import com.sam_mc.travelyourearth.item.ModItems;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.core.registries.MultiRegistryBootstrap;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import java.util.Set;

/** Generates the recipes of the mod (crafting, smelting and blasting). */
public class ModRecipeProvider extends RecipeProvider {

    public ModRecipeProvider(BootstrapContext<Recipe<?>> recipeOutput, BootstrapContext<Advancement> advancementOutput) {
        super(recipeOutput, advancementOutput);
    }

    // =====================================================================
    // BOOTSTRAP (replaces the old Runner class)
    // =====================================================================
    public static MultiRegistryBootstrap create() {
        return new MultiRegistryBootstrap() {
            @Override
            public Set<ResourceKey<? extends Registry<?>>> requestedRegistries() {
                return Set.of(Registries.RECIPE, Registries.ADVANCEMENT);
            }

            @Override
            public void run(MultiRegistryBootstrap.BootstrapGetter registries) {
                new ModRecipeProvider(
                        registries.get(Registries.RECIPE),
                        registries.get(Registries.ADVANCEMENT)
                ).buildRecipes();
            }
        };
    }

    // =====================================================================
    // RECIPES
    // =====================================================================
    @Override
    protected void buildRecipes() {
        buildRubyBlockRecipes();
        buildRubyToolRecipes();
        buildRubyArmorRecipes();
        buildRubySmeltingRecipes();
    }

    // ---------------------------------------------------------------------
    // Blocks
    // ---------------------------------------------------------------------
    private void buildRubyBlockRecipes() {
        shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.RUBY_BLOCK.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.RUBY.get())
                .unlockedBy(getHasName(ModItems.RUBY.get()), has(ModItems.RUBY.get()))
                .group("ruby")
                .save(output);

        shapeless(RecipeCategory.MISC, ModItems.RUBY.get(), 9)
                .requires(ModBlocks.RUBY_BLOCK.get())
                .unlockedBy(getHasName(ModBlocks.RUBY_BLOCK.get()), has(ModBlocks.RUBY_BLOCK.get()))
                .group("ruby")
                .save(output, TravelYourEarth.MODID + ":ruby_from_ruby_block");

        shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.HARDENED_GLASS.get(), 6)
                .pattern(" G ")
                .pattern("GRG")
                .pattern(" G ")
                .define('G', Blocks.GLASS)
                .define('R', ModItems.RUBY.get())
                .unlockedBy(getHasName(ModItems.RUBY.get()), has(ModItems.RUBY.get()))
                .group("tempered_glass")
                .save(output);
    }

    // ---------------------------------------------------------------------
    // Tools and weapons
    // ---------------------------------------------------------------------
    private void buildRubyToolRecipes() {
        rubyTool(RecipeCategory.TOOLS,  ModItems.RUBY_AXE.get(),     "AA", "SA", "S ");
        rubyTool(RecipeCategory.TOOLS,  ModItems.RUBY_HOE.get(),     "AA", "S ", "S ");
        rubyTool(RecipeCategory.TOOLS,  ModItems.RUBY_PICKAXE.get(), "AAA", " S ", " S ");
        rubyTool(RecipeCategory.TOOLS,  ModItems.RUBY_SHOVEL.get(),  "A", "S", "S");
        rubyTool(RecipeCategory.COMBAT, ModItems.RUBY_SPEAR.get(),   "  A", " S ", "S  ");
        rubyTool(RecipeCategory.COMBAT, ModItems.RUBY_SWORD.get(),   "A", "A", "S");
    }

    // ---------------------------------------------------------------------
    // Armor
    // ---------------------------------------------------------------------
    private void buildRubyArmorRecipes() {
        rubyArmor(ModItems.RUBY_BOOTS.get(),      "A A", "A A");
        rubyArmor(ModItems.RUBY_CHESTPLATE.get(), "A A", "AAA", "AAA");
        rubyArmor(ModItems.RUBY_HELMET.get(),     "AAA", "A A");
        rubyArmor(ModItems.RUBY_LEGGINGS.get(),   "AAA", "A A", "A A");
    }

    // ---------------------------------------------------------------------
    // Smelting (furnace and blast furnace)
    // ---------------------------------------------------------------------
    private void buildRubySmeltingRecipes() {
        List<ItemLike> rubySmeltables = List.of(
                ModBlocks.RUBY_ORE.get(),
                ModBlocks.DEEPSLATE_RUBY_ORE.get()
        );

        // Same as diamond in 26.3: 200 ticks in the furnace and in the blast furnace
        oreSmelting(rubySmeltables, RecipeCategory.MISC, CookingBookCategory.MISC, ModItems.RUBY.get(), 0.85f, 200, "ruby");
        oreBlasting(rubySmeltables, RecipeCategory.MISC, CookingBookCategory.MISC, ModItems.RUBY.get(), 0.85f, 200, "ruby");
    }

    // =====================================================================
    // HELPERS
    // =====================================================================

    /** Ruby tool: 'A' = ruby, 'S' = stick. */
    private void rubyTool(RecipeCategory category, ItemLike result, String... rows) {
        var recipe = shaped(category, result);
        for (String row : rows) {
            recipe.pattern(row);
        }
        recipe.define('A', ModItems.RUBY.get())
                .define('S', Items.STICK)
                .unlockedBy(getHasName(ModItems.RUBY.get()), has(ModItems.RUBY.get()))
                .save(output);
    }

    /** Ruby armor piece: 'A' = ruby. */
    private void rubyArmor(ItemLike result, String... rows) {
        var recipe = shaped(RecipeCategory.COMBAT, result);
        for (String row : rows) {
            recipe.pattern(row);
        }
        recipe.define('A', ModItems.RUBY.get())
                .unlockedBy(getHasName(ModItems.RUBY.get()), has(ModItems.RUBY.get()))
                .save(output);
    }

    /** Same as vanilla, but the recipes are saved with the mod's namespace (travelyourearth:...). */
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