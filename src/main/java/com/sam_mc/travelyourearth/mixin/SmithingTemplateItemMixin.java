package com.sam_mc.travelyourearth.mixin;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.SmithingTemplateItem;

@Mixin(SmithingTemplateItem.class)
public class SmithingTemplateItemMixin {

    // Añade el icono de rubí a la rotación de la mesa de herrería, justo después de la esmeralda
    @Inject(method = "createTrimmableMaterialIconList", at = @At("RETURN"), cancellable = true)
    private static void travelyourearth$addRubyIcon(CallbackInfoReturnable<List<Identifier>> cir) {
        List<Identifier> icons = new ArrayList<>(cir.getReturnValue());

        Identifier emerald = Identifier.withDefaultNamespace("container/slot/emerald");
        Identifier ruby = Identifier.fromNamespaceAndPath("travelyourearth", "container/slot/ruby");

        int emeraldIndex = icons.indexOf(emerald);
        if (emeraldIndex >= 0) {
            icons.add(emeraldIndex + 1, ruby);
        } else {
            icons.add(ruby); // por si la lista cambió y no encuentra la esmeralda
        }

        cir.setReturnValue(icons);
    }
}