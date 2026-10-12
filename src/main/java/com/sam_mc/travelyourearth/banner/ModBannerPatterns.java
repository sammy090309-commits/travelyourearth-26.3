package com.sam_mc.travelyourearth.banner;

import com.sam_mc.travelyourearth.TravelYourEarth;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.entity.BannerPattern;

/**
 * Banner patterns of the mod. Like enchantments, they are DATA (registry "banner_pattern"):
 * this class only holds the keys and the bootstrap that datagen uses to write the JSON.
 *
 * Textures (white on transparent, the game tints them):
 *   assets/travelyourearth/textures/entity/banner/rebellion.png  (64x64)
 *   assets/travelyourearth/textures/entity/shield/rebellion.png  (64x64)
 */
public class ModBannerPatterns {

    /** Crossed axe + hoe + star: the villagers' rebellion. Sold by the illegal shepherd. */
    public static final ResourceKey<BannerPattern> REBELLION = create("rebellion");

    public static void bootstrap(BootstrapContext<BannerPattern> context) {
        register(context, REBELLION);
    }

    private static ResourceKey<BannerPattern> create(String name) {
        return ResourceKey.create(Registries.BANNER_PATTERN, Identifier.fromNamespaceAndPath(TravelYourEarth.MODID, name));
    }

    /**
     * Same as vanilla: assetId = the texture name, translationKey = "block.<modid>.banner.<name>".
     * The game adds ".<color>" to the translation key (e.g. block.travelyourearth.banner.rebellion.red).
     */
    private static void register(BootstrapContext<BannerPattern> context, ResourceKey<BannerPattern> key) {
        Identifier id = key.identifier();
        context.register(key, new BannerPattern(id, "block." + id.getNamespace() + ".banner." + id.getPath()));
    }
}