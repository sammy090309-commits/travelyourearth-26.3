package com.sam_mc.travelyourearth.mixin;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.SmithingTemplateItem;

/**
 * Smithing table: adds the ruby to the rotating material icons shown in the empty
 * material slot when a trim template is placed.
 */
@Mixin(SmithingTemplateItem.class)
public class SmithingTemplateItemMixin {

    // Adds the ruby icon to the smithing table rotation, right after the emerald
    @Inject(method = "createTrimmableMaterialIconList", at = @At("RETURN"), cancellable = true)
    private static void travelyourearth$addRubyIcon(CallbackInfoReturnable<List<Identifier>> cir) {
        List<Identifier> icons = new ArrayList<>(cir.getReturnValue());

        Identifier emerald = Identifier.withDefaultNamespace("container/slot/emerald");
        Identifier ruby = Identifier.fromNamespaceAndPath("travelyourearth", "container/slot/ruby");

        int emeraldIndex = icons.indexOf(emerald);
        if (emeraldIndex >= 0) {
            icons.add(emeraldIndex + 1, ruby);
        } else {
            icons.add(ruby); // in case the list changed and the emerald isn't found
        }

        cir.setReturnValue(icons);
    }
}