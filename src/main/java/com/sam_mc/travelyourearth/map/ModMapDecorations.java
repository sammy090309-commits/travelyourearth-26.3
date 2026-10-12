package com.sam_mc.travelyourearth.map;

import com.sam_mc.travelyourearth.TravelYourEarth;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Map icons (decorations) of the mod, registered like vanilla's MapDecorationTypes.
 * The texture of each icon is assets/travelyourearth/textures/map/decorations/<name>.png (8x8).
 */
public class ModMapDecorations {

    public static final DeferredRegister<MapDecorationType> MAP_DECORATIONS =
            DeferredRegister.create(Registries.MAP_DECORATION_TYPE, TravelYourEarth.MODID);

    /**
     * Trail Ruins explorer map icon (the decorated pot).
     * 26.3: MapDecorationType(assetId, showOnItemFrame, trackCount)
     * - showOnItemFrame = true -> also visible on maps inside item frames (like vanilla explorer icons)
     * - trackCount = false     -> not counted like player markers
     */
    public static final DeferredHolder<MapDecorationType, MapDecorationType> TRAIL_RUINS = MAP_DECORATIONS.register(
            "trail_ruins",
            () -> new MapDecorationType(
                    Identifier.fromNamespaceAndPath(TravelYourEarth.MODID, "trail_ruins"),
                    true,
                    false)
    );

    public static void register(IEventBus eventBus) {
        MAP_DECORATIONS.register(eventBus);
    }
}