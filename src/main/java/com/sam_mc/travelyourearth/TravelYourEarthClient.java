package com.sam_mc.travelyourearth;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

// Esta clase no se carga en servidores dedicados: aquí es seguro usar código del cliente.
@Mod(value = TravelYourEarth.MODID, dist = Dist.CLIENT)
public class TravelYourEarthClient {
    public TravelYourEarthClient(ModContainer container) {
        // Activa el botón "Config" en Mods -> Travel Your Earth.
        // NeoForge crea la pantalla sola a partir de tu Config.java.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}