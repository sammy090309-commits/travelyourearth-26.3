package com.sam_mc.travelyourearth.datagen;

import com.sam_mc.travelyourearth.TravelYourEarth;
import com.sam_mc.travelyourearth.block.ModBlocks;
import com.sam_mc.travelyourearth.item.ModItems;
import com.sam_mc.travelyourearth.potion.ModPotions;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.predicates.PotionsPredicate;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.core.registries.MultiRegistryBootstrap;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.BrewingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PotionIngredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import java.util.Set;

/** Generates the recipes of the mod (crafting, smelting, blasting and brewing). */
public class ModRecipeProvider extends RecipeProvider {

    /** Kept so we can register recipes that have no builder (like brewing). */
    private final BootstrapContext<Recipe<?>> recipes;

    public ModRecipeProvider(BootstrapContext<Recipe<?>> recipeOutput, BootstrapContext<Advancement> advancementOutput) {
        super(recipeOutput, advancementOutput);
        this.recipes = recipeOutput;
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
        buildDecayBrewingRecipes();
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

    // ---------------------------------------------------------------------
    // Brewing stand (26.3: brewing recipes are data-driven, minecraft:brewing)
    // ---------------------------------------------------------------------
    private void buildDecayBrewingRecipes() {
        // The brewing recipe output is a fixed item, so each bottle type needs its own recipe.
        List<Item> bottles = List.of(Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION);

        for (Item bottle : bottles) {
            // Awkward + Wither Rose -> Decay (Wither I, 0:45)
            brewing(bottle, Potions.AWKWARD, Items.WITHER_ROSE, bottle, ModPotions.DECAY);
            // Decay + Redstone -> Long Decay (Wither I, 1:30)
            brewing(bottle, ModPotions.DECAY, Items.REDSTONE, bottle, ModPotions.LONG_DECAY);
            // Decay + Glowstone Dust -> Decay II (Wither II, 0:21)
            brewing(bottle, ModPotions.DECAY, Items.GLOWSTONE_DUST, bottle, ModPotions.STRONG_DECAY);
        }

        // Bottle conversions: Gunpowder -> splash, Dragon's Breath -> lingering
        for (Holder<Potion> decay : List.<Holder<Potion>>of(ModPotions.DECAY, ModPotions.LONG_DECAY, ModPotions.STRONG_DECAY)) {
            brewing(Items.POTION, decay, Items.GUNPOWDER, Items.SPLASH_POTION, decay);
            brewing(Items.SPLASH_POTION, decay, Items.DRAGON_BREATH, Items.LINGERING_POTION, decay);
        }
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

    /**
     * One brewing recipe: (inputBottle with inputPotion) + reagent -> (outputBottle with outputPotion).
     * Saved as travelyourearth:brewing/<output bottle>_<output potion>_from_<input bottle>_<input potion>.
     */
    private void brewing(Item inputBottle, Holder<Potion> inputPotion, Item reagent,
                         Item outputBottle, Holder<Potion> outputPotion) {
        PotionIngredient input = PotionIngredient.of(inputBottle, PotionsPredicate.ofPotion(inputPotion));
        PotionIngredient reagentIngredient = PotionIngredient.of(reagent);
        // Built without an ItemStack: in datagen the item components are not bound yet.
        ItemStackTemplate result = new ItemStackTemplate(outputBottle, DataComponentPatch.builder()
                .set(DataComponents.POTION_CONTENTS, new PotionContents(outputPotion))
                .build());

        String name = "brewing/" + getItemName(outputBottle) + "_" + potionName(outputPotion)
                + "_from_" + getItemName(inputBottle) + "_" + potionName(inputPotion);
        ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE,
                Identifier.fromNamespaceAndPath(TravelYourEarth.MODID, name));

        recipes.register(key, new BrewingRecipe(input, reagentIngredient, result));
    }

    /** "minecraft:awkward" -> "awkward", "travelyourearth:long_decay" -> "long_decay". */
    private static String potionName(Holder<Potion> potion) {
        return potion.unwrapKey().orElseThrow().identifier().getPath();
    }
}