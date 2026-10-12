package com.sam_mc.travelyourearth.client.config.report;

import com.mojang.blaze3d.Blaze3D;
import com.sam_mc.travelyourearth.TravelYourEarth;
import com.sam_mc.travelyourearth.client.ModFonts;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.net.URI;

/**
 * "Report bugs" / "Feedback": choose where to do it.
 *
 *        ┌───────────────────┐   ┌───────────────────┐
 *        │       [icon]      │   │       [icon]      │
 *        │  On the website   │   │    In the game    │
 *        │   (GitHub page)   │   │ (without leaving) │
 *        └───────────────────┘   └───────────────────┘
 *                         [ Back ]
 */
public class ReportChoiceScreen extends Screen {

    public enum Kind {
        BUG("bugs", ReportSender.BUG_TEMPLATE),
        FEEDBACK("feedback", ReportSender.FEEDBACK_TEMPLATE);

        /** Name of its icon (config/bugs, config/feedback) and of its lang keys. */
        final String name;
        final String template;

        Kind(String name, String template) {
            this.name = name;
            this.template = template;
        }
    }

    private static final int CARD_W = 140;
    private static final int CARD_H = 90;
    private static final int CARD_GAP = 16;
    private static final int ICON = 20;
    private static final int CARD_BORDER = 0x40FFFFFF;
    private static final int CARD_HOVER_BORDER = 0x90FFFFFF;
    private static final int SUBTITLE_COLOR = 0xFFA0A0A0;

    private final Screen parent;
    private final Kind kind;
    private FlatButton webCard;
    private FlatButton gameCard;

    public ReportChoiceScreen(Screen parent, Kind kind) {
        super(Component.translatable("travelyourearth.report." + kind.name + ".title"));
        this.parent = parent;
        this.kind = kind;
    }

    @Override
    protected void init() {
        int left = (this.width - (CARD_W * 2 + CARD_GAP)) / 2;
        int top = (this.height - CARD_H) / 2;

        // The cards are buttons without text: we draw their inside in extractRenderState
        this.webCard = this.addRenderableWidget(new FlatButton(left, top, CARD_W, CARD_H, Component.empty(),
                b -> openWebsite()));
        this.gameCard = this.addRenderableWidget(new FlatButton(left + CARD_W + CARD_GAP, top, CARD_W, CARD_H, Component.empty(),
                b -> this.minecraft.gui.setScreen(new ReportFormScreen(this, this.kind))));

        this.addRenderableWidget(new FlatButton((this.width - 80) / 2, top + CARD_H + 20, 80, 20,
                Component.translatable("gui.back"), b -> this.onClose()));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);

        graphics.centeredText(this.font, ModFonts.title(this.title), this.width / 2, this.webCard.getY() - 30, 0xFFFFFFFF);
        graphics.centeredText(this.font, Component.translatable("travelyourearth.report.choose"),
                this.width / 2, this.webCard.getY() - 16, SUBTITLE_COLOR);

        drawCard(graphics, this.webCard, "web");
        drawCard(graphics, this.gameCard, "game");
    }

    /** Border, icon, name and a short explanation of one card. */
    private void drawCard(GuiGraphicsExtractor graphics, FlatButton card, String where) {
        int x = card.getX(), y = card.getY(), w = card.getWidth(), h = card.getHeight();
        boolean hovered = card.isHovered();
        // (the dark background is the button's own, it gets lighter with the mouse on it)
        graphics.outline(x, y, w, h, hovered ? CARD_HOVER_BORDER : CARD_BORDER);

        // Web: the "wiki" book icon; in game: the bug / feedback icon
        Identifier icon = id(where.equals("web") ? "config/wiki" : "config/" + this.kind.name);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, icon, x + (w - ICON) / 2, y + 14, ICON, ICON);

        graphics.centeredText(this.font, ModFonts.title(Component.translatable("travelyourearth.report." + where)),
                x + w / 2, y + 44, 0xFFFFFFFF);
        graphics.textWithWordWrap(this.font, Component.translatable("travelyourearth.report." + where + ".description"),
                x + 8, y + 58, w - 16, SUBTITLE_COLOR);
    }

    /** The empty template on GitHub (with vanilla's "Do you want to open this link?" window). */
    private void openWebsite() {
        URI uri = URI.create(ReportSender.ISSUES_URL + "?template=" + this.kind.template);
        this.minecraft.gui.setScreen(new ConfirmLinkScreen(open -> {
            if (open) {
                Blaze3D.openUri(uri);
            }
            this.minecraft.gui.setScreen(this);
        }, uri, false));
    }

    /** The screen this one was opened from (the mod's main config menu). */
    public Screen parentScreen() {
        return this.parent;
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(this.parent);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(TravelYourEarth.MODID, path);
    }
}