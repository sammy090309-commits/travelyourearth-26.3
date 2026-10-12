package com.sam_mc.travelyourearth;

import com.sam_mc.travelyourearth.client.config.TravelYourEarthConfigScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * Client-only entry point of the mod.
 * This class isn't loaded on dedicated servers, so it's safe to use client code here.
 */
@Mod(value = TravelYourEarth.MODID, dist = Dist.CLIENT)
public class TravelYourEarthClient {

    public TravelYourEarthClient(ModContainer container) {
        // "Config" button in Mods -> Travel Your Earth: our own main screen
        // (logo, Client / Local / Bugs / Feedback / Wiki). Client and Local open NeoForge's options list.
        container.registerExtensionPoint(IConfigScreenFactory.class, TravelYourEarthConfigScreen::new);
    }
}