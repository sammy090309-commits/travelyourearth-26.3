package com.sam_mc.travelyourearth.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Lets us read and change "leftPos" (X position of the GUI) and read "topPos" (Y position) of any inventory screen.
 * We use it to move the beacon to the right when the panel opens, like the recipe book does.
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