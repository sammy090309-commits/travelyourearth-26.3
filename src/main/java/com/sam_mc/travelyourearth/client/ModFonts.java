package com.sam_mc.travelyourearth.client;

import com.sam_mc.travelyourearth.TravelYourEarth;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

/**
 * The mod's fonts.
 *
 * TITLE = assets/travelyourearth/font/title.json (Bedrock-style titles of the config menu).
 * The JSON says which file the letters come from: change the font there, not here.
 * Letters the font doesn't have (ñ, á, é...) fall back to the vanilla font (the "reference" provider).
 */
public final class ModFonts {

    /** 26.3: a font is a FontDescription; ours is a font JSON in assets (FontDescription.Resource). */
    public static final FontDescription TITLE =
            new FontDescription.Resource(Identifier.fromNamespaceAndPath(TravelYourEarth.MODID, "title"));

    private ModFonts() {}

    /** The same text, written with the title font. */
    public static MutableComponent title(Component text) {
        return text.copy().withStyle(style -> style.withFont(TITLE));
    }
}
