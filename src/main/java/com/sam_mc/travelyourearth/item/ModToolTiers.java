package com.sam_mc.travelyourearth.item;

import com.sam_mc.travelyourearth.tags.ModTags;
import net.minecraft.world.item.ToolMaterial;

/** Tool materials of the mod. */
public class ModToolTiers {

    /**
     * Ruby tools, between iron and diamond.
     *   blocks it can't mine: #travelyourearth:incorrect_for_ruby_tool (like iron: no obsidian)
     *   durability 1200 | mining speed 7 | attack damage bonus 2.5 | enchantability 18
     *   repaired with #travelyourearth:ruby_repairable
     * (vanilla iron: 250, 6, 2, 14 | vanilla diamond: 1561, 8, 3, 10)
     */
    public static final ToolMaterial RUBY = new ToolMaterial(ModTags.Blocks.INCORRECT_FOR_RUBY_TOOL,
            1200, 7f, 2.5f, 18, ModTags.Items.RUBY_REPAIRABLE);
}