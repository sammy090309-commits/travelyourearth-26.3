package com.sam_mc.travelyourearth.datagen;

import com.sam_mc.travelyourearth.TravelYourEarth;
import com.sam_mc.travelyourearth.block.ModBlocks;
import com.sam_mc.travelyourearth.item.ModItems;
import com.sam_mc.travelyourearth.item.ModTrimMaterials;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.color.item.Dye;
import net.minecraft.client.renderer.item.SelectItemModel;
import net.minecraft.client.renderer.item.properties.select.TrimMaterialProperty;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.trim.TrimMaterial;

import java.util.ArrayList;
import java.util.List;

public class ModModelProvider extends ModelProvider {
    public ModModelProvider(PackOutput output){
        super(output, TravelYourEarth.MODID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        itemModels.generateFlatItem(ModItems.RUBY.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.RUBY_SWORD.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(ModItems.RUBY_PICKAXE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(ModItems.RUBY_SHOVEL.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(ModItems.RUBY_AXE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(ModItems.RUBY_HOE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateSpear(ModItems.RUBY_SPEAR.get());

        // CAMBIO (26.3): antes era itemModels.generateTrimmableArmorSet(...).
        // Ese método de vanilla solo conoce los materiales de trim vanilla, así que el caso del
        // rubí se había añadido A MANO al JSON generado. Ahora lo genera el datagen.
        generateRubyTrimmableItem(itemModels, ModItems.RUBY_HELMET.get(), ItemModelGenerators.TRIM_PREFIX_HELMET);
        generateRubyTrimmableItem(itemModels, ModItems.RUBY_CHESTPLATE.get(), ItemModelGenerators.TRIM_PREFIX_CHESTPLATE);
        generateRubyTrimmableItem(itemModels, ModItems.RUBY_LEGGINGS.get(), ItemModelGenerators.TRIM_PREFIX_LEGGINGS);
        generateRubyTrimmableItem(itemModels, ModItems.RUBY_BOOTS.get(), ItemModelGenerators.TRIM_PREFIX_BOOTS);

        // NUEVO: trim de rubí visible en el ícono de las armaduras VANILLA.
        // Los modelos vanilla solo conocen los 11 materiales vanilla, así que sobrescribimos su
        // definición de ítem (assets/minecraft/items/*.json) añadiendo el caso del rubí.
        // Para los materiales vanilla se REUSAN los modelos de vanilla (no se regeneran).
        addRubyTrimToVanillaArmorSet(itemModels, Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS, true);
        addRubyTrimToVanillaArmorSet(itemModels, Items.COPPER_HELMET, Items.COPPER_CHESTPLATE, Items.COPPER_LEGGINGS, Items.COPPER_BOOTS, false);
        addRubyTrimToVanillaArmorSet(itemModels, Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS, false);
        addRubyTrimToVanillaArmorSet(itemModels, Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS, false);
        addRubyTrimToVanillaArmorSet(itemModels, Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS, false);
        addRubyTrimToVanillaArmorSet(itemModels, Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS, false);
        addRubyTrimToVanillaArmorSet(itemModels, Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS, false);
        addRubyTrimToVanillaItem(itemModels, Items.TURTLE_HELMET, ItemModelGenerators.TRIM_PREFIX_HELMET, false);

        itemModels.generateFlatItem(ModItems.RUBY_HORSE_ARMOR.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.RUBY_NAUTILUS_ARMOR.get(), ModelTemplates.FLAT_ITEM);

        /* BLOCKS */
        blockModels.createTrivialCube(ModBlocks.RUBY_BLOCK.get());
        blockModels.createTrivialCube(ModBlocks.RUBY_ORE.get());
        blockModels.createTrivialCube(ModBlocks.DEEPSLATE_RUBY_ORE.get());
        blockModels.createTrivialCube(ModBlocks.HARDENED_GLASS.get());
    }

    /**
     * Copia de ItemModelGenerators#generateTrimmableItem (vanilla 26.3, sin capa teñible)
     * + un caso extra para el material de trim de rubí.
     *
     * - Materiales vanilla: igual que vanilla (ruby_boots_copper_trim, ruby_boots_iron_trim, ...).
     * - Rubí sobre armadura de rubí: usa la paleta "ruby_darker", igual que vanilla hace con
     *   hierro sobre hierro, oro sobre oro, etc. Sprite: minecraft:trims/items/boots_trim_ruby_darker
     *   (lo crea ModSpriteSourceProvider).
     */
    private static void generateRubyTrimmableItem(ItemModelGenerators itemModels, Item armor, Identifier slotTrimPrefix) {
        Identifier modelLocation = ModelLocationUtils.getModelLocation(armor);
        Material itemTexture = TextureMapping.getItemTexture(armor);
        List<SelectItemModel.SwitchCase<ResourceKey<TrimMaterial>>> cases = new ArrayList<>();

        // 1) Los 11 materiales de trim vanilla
        for (ItemModelGenerators.TrimMaterialData material : ItemModelGenerators.TRIM_MATERIAL_MODELS) {
            Identifier trimModelLocation = modelLocation.withSuffix("_" + material.palette().suffix() + "_trim");
            Material trimOverlay = new Material(slotTrimPrefix.withSuffix("_" + material.palette().suffix()));
            itemModels.generateLayeredItem(trimModelLocation, itemTexture, trimOverlay);
            cases.add(ItemModelUtils.when(material.materialKey(), ItemModelUtils.plainModel(trimModelLocation)));
        }

        // 2) Nuestro material: rubí (versión oscura porque la armadura también es de rubí)
        Identifier rubyTrimModelLocation = modelLocation.withSuffix("_ruby_trim");
        Material rubyTrimOverlay = new Material(slotTrimPrefix.withSuffix("_ruby_darker"));
        itemModels.generateLayeredItem(rubyTrimModelLocation, itemTexture, rubyTrimOverlay);
        cases.add(ItemModelUtils.when(ModTrimMaterials.RUBY, ItemModelUtils.plainModel(rubyTrimModelLocation)));

        // 3) Modelo sin trim (el que se usa casi siempre) + definición del ítem con el select
        ModelTemplates.FLAT_ITEM.create(modelLocation, TextureMapping.layer0(itemTexture), itemModels.modelOutput);
        itemModels.itemModelOutput.accept(
                armor,
                ItemModelUtils.select(new TrimMaterialProperty(), ItemModelUtils.plainModel(modelLocation), cases)
        );
    }

    /** Color por defecto del cuero en vanilla (el mismo que usa ItemModelGenerators). */
    private static final int LEATHER_DEFAULT_COLOR = -6265536;

    private static void addRubyTrimToVanillaArmorSet(ItemModelGenerators itemModels, Item helmet, Item chestplate,
                                                     Item leggings, Item boots, boolean hasDyedLayer) {
        addRubyTrimToVanillaItem(itemModels, helmet, ItemModelGenerators.TRIM_PREFIX_HELMET, hasDyedLayer);
        addRubyTrimToVanillaItem(itemModels, chestplate, ItemModelGenerators.TRIM_PREFIX_CHESTPLATE, hasDyedLayer);
        addRubyTrimToVanillaItem(itemModels, leggings, ItemModelGenerators.TRIM_PREFIX_LEGGINGS, hasDyedLayer);
        addRubyTrimToVanillaItem(itemModels, boots, ItemModelGenerators.TRIM_PREFIX_BOOTS, hasDyedLayer);
    }

    /**
     * Rehace la definición de ítem de una armadura vanilla igual que vanilla 26.3
     * (ItemModelGenerators#generateTrimmableItem) + el caso del rubí.
     *
     * - Casos vanilla: apuntan a los modelos que YA trae el juego (minecraft:item/iron_helmet_copper_trim, ...),
     *   así que no se sobrescribe ningún modelo vanilla, solo la definición del ítem.
     * - Caso rubí: genera minecraft:item/<pieza>_ruby_trim con el sprite minecraft:trims/items/<slot>_trim_ruby
     *   (lo crea ModSpriteSourceProvider).
     */
    private static void addRubyTrimToVanillaItem(ItemModelGenerators itemModels, Item armor, Identifier slotTrimPrefix, boolean hasDyedLayer) {
        Identifier modelLocation = ModelLocationUtils.getModelLocation(armor);
        List<SelectItemModel.SwitchCase<ResourceKey<TrimMaterial>>> cases = new ArrayList<>();

        // 1) Materiales vanilla -> modelos vanilla existentes
        for (ItemModelGenerators.TrimMaterialData material : ItemModelGenerators.TRIM_MATERIAL_MODELS) {
            Identifier vanillaTrimModel = modelLocation.withSuffix("_" + material.palette().suffix() + "_trim");
            cases.add(ItemModelUtils.when(material.materialKey(), armorModel(vanillaTrimModel, hasDyedLayer)));
        }

        // 2) Rubí -> modelo nuevo
        Identifier rubyTrimModel = modelLocation.withSuffix("_ruby_trim");
        Material itemTexture = TextureMapping.getItemTexture(armor);
        Material rubyTrimOverlay = new Material(slotTrimPrefix.withSuffix("_ruby"));
        if (hasDyedLayer) {
            // Cuero: base teñible + overlay sin teñir + trim (3 capas, como vanilla)
            itemModels.generateLayeredItem(rubyTrimModel, itemTexture, TextureMapping.getItemTexture(armor, "_overlay"), rubyTrimOverlay);
        } else {
            itemModels.generateLayeredItem(rubyTrimModel, itemTexture, rubyTrimOverlay);
        }
        cases.add(ItemModelUtils.when(ModTrimMaterials.RUBY, armorModel(rubyTrimModel, hasDyedLayer)));

        // 3) Sin trim -> modelo vanilla existente
        itemModels.itemModelOutput.accept(
                armor,
                ItemModelUtils.select(new TrimMaterialProperty(), armorModel(modelLocation, hasDyedLayer), cases)
        );
    }

    private static net.minecraft.client.renderer.item.ItemModel.Unbaked armorModel(Identifier model, boolean hasDyedLayer) {
        return hasDyedLayer
                ? ItemModelUtils.tintedModel(model, new Dye(LEATHER_DEFAULT_COLOR))
                : ItemModelUtils.plainModel(model);
    }
}