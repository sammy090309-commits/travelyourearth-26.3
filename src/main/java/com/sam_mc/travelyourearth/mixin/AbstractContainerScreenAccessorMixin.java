package com.sam_mc.travelyourearth.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Deja leer y cambiar "leftPos" (posición X del GUI) y leer "topPos" (posición Y) de cualquier pantalla con inventario.
 * Lo usamos para correr el faro a la derecha al abrir el panel, como hace el libro de recetas.
 */
@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessorMixin {

    @Accessor("leftPos")
    int travelyourearth$getLeftPos();

    @Accessor("leftPos")
    void travelyourearth$setLeftPos(int leftPos);

    @Accessor("topPos")
    int travelyourearth$getTopPos();
}