package com.sam_mc.travelyourearth.advancement;

import com.sam_mc.travelyourearth.TravelYourEarth;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

/**
 * Names of the 1.2 advancements and a helper to give them from code.
 *
 * Every advancement exists in TWO versions (see ModAdvancementProvider):
 *   travelyourearth:adventure/<name>   -> vanilla "Adventure" tab   (modAdvancementTab OFF)
 *   travelyourearth:ruby/<name>        -> the mod's own tab          (modAdvancementTab ON)
 * Only one of them is loaded; award() tries both, so it always works.
 */
public final class ModAdvancements {

    // Logical names (also used by ModAdvancementSync)
    public static final String WANNA_BUY = "wanna_buy";                       // Psst, Wanna Buy?
    public static final String CAUGHT_RED_HANDED = "caught_red_handed";       // Caught Red-Handed
    public static final String VIVA_LA_REVOLUCION = "viva_la_revolucion";     // Viva la Revolución (hidden)
    public static final String FORBIDDEN_KNOWLEDGE = "forbidden_knowledge";   // Forbidden Knowledge (hidden)

    // Criteria (all "impossible": they're given from code)
    public static final String CRITERION_DONE = "done";
    public static final String CRITERION_BANISHMENT = "banishment_book";
    public static final String CRITERION_HARPOON = "harpoon_book";
    public static final String CRITERION_COWARDICE = "cowardice_book";

    private ModAdvancements() {}

    /** Vanilla tab version. */
    public static Identifier adventure(String name) {
        return Identifier.fromNamespaceAndPath(TravelYourEarth.MODID, "adventure/" + name);
    }

    /** Mod tab version. */
    public static Identifier modTab(String name) {
        return Identifier.fromNamespaceAndPath(TravelYourEarth.MODID, "ruby/" + name);
    }

    /** Gives a criterion of an advancement (whichever version is loaded). Does nothing if already done. */
    public static void award(ServerPlayer player, String name, String criterion) {
        for (Identifier id : new Identifier[]{adventure(name), modTab(name)}) {
            AdvancementHolder advancement = player.level().getServer().getAdvancements().get(id);
            if (advancement != null) {
                player.getAdvancements().award(advancement, criterion);
            }
        }
    }

    public static void award(ServerPlayer player, String name) {
        award(player, name, CRITERION_DONE);
    }
}