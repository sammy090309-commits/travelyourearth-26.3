package com.sam_mc.travelyourearth.client.config.report;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/**
 * Sodium-style flat button (same look as the bottom buttons of the options screens):
 * dark and see-through, a bit lighter with the mouse on it, gray text when it can't be used.
 */
public class FlatButton extends Button {
    private static final int COLOR = 0x60000000;
    private static final int HOVER_COLOR = 0x80383838;
    private static final int TEXT = 0xFFFFFFFF;
    private static final int TEXT_DISABLED = 0xFF8A8A8A;

    /** Text color while active (white by default; green for "Send"). */
    private int textColor = TEXT;

    public FlatButton(int x, int y, int width, int height, Component message, OnPress onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
    }

    public FlatButton withTextColor(int color) {
        this.textColor = color;
        return this;
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int x = this.getX(), y = this.getY();
        boolean lit = this.active && this.isHovered();
        graphics.fill(x, y, x + this.width, y + this.height, lit ? HOVER_COLOR : COLOR);
        graphics.centeredText(Minecraft.getInstance().font, this.getMessage(), x + this.width / 2,
                y + (this.height - 8) / 2, this.active ? this.textColor : TEXT_DISABLED);
    }
}