package com.sam_mc.travelyourearth;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * Client-only entry point of the mod.
 * This class isn't loaded on dedicated servers, so it's safe to use client code here.
 */
@Mod(value = TravelYourEarth.MODID, dist = Dist.CLIENT)
public class TravelYourEarthClient {

    public TravelYourEarthClient(ModContainer container) {
        // Enables the "Config" button in Mods -> Travel Your Earth.
        // NeoForge builds the screen by itself from Config.java.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}