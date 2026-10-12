package com.sam_mc.travelyourearth.client.config;

import com.sam_mc.travelyourearth.client.ModFonts;
import com.sam_mc.travelyourearth.Config;
import com.sam_mc.travelyourearth.TravelYourEarth;
import com.sam_mc.travelyourearth.client.config.report.ReportChoiceScreen;
import com.mojang.blaze3d.Blaze3D;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.neoforged.fml.ModContainer;
import org.joml.Matrix3x2f;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Main config screen of Travel Your Earth (Mods -> Travel Your Earth -> Config).
 *
 *            [ logo: Minecraft Travel Your Earth ]
 *                                Configs!   <- yellow splash text, like the title screen
 *  ███████████████████████ OPTIONS band ███████████████████████
 *                  [Client]     [Local]
 *          [Report bugs] [Feedback] [Wiki (locked)]
 *  ████████████████████████ bottom band ███████████████████████
 *  [<] back button (top-left, like Bedrock); Esc also goes back
 *
 * The background is vanilla's: the blurred world when you're playing, the panorama in the menus.
 */
public class TravelYourEarthConfigScreen extends Screen {

    // ---------------------------------------------------------------------
    // Textures
    // ---------------------------------------------------------------------
    /** Logo sprite: assets/travelyourearth/textures/gui/sprites/config/logo.png (512 x 153), next to the icons. */
    private static final Identifier LOGO = id("config/logo");
    private static final int LOGO_TEXTURE_W = 512;
    private static final int LOGO_TEXTURE_H = 153;

    /** Icons: assets/travelyourearth/textures/gui/sprites/config/<name>.png (+ _highlighted, wiki_disabled). */
    private static final int ICON_SIZE = 20;
    private static final int BACK_SIZE = 22;
    private static final int BACK_MARGIN = 6;

    /** Splash text: same yellow and angle (-20 degrees) as vanilla's title screen. */
    private static final int SPLASH_COLOR = 0xFFFF00;
    private static final float SPLASH_ANGLE = -0.34906584F;
    /** Size of the splash text (vanilla uses 100; smaller number = smaller text). */
    private static final float SPLASH_SIZE = 85.0F;

    // ---------------------------------------------------------------------
    // Layout
    // ---------------------------------------------------------------------
    private static final int OPTIONS_BAND_H = 100;
    private static final int OPTIONS_HEADER_H = 26;      // "Options" title + its line, above the icons
    private static final int OPTIONS_LINE_MAX_W = 240;   // the line under "Options"
    private static final int OPTIONS_LINE_MARGIN = 20;
    private static final float OPTIONS_TITLE_SCALE = 1.35F;  // "Options" a bit bigger than normal text
    /** Options band, Bedrock style: a big box in the center holding the options, and two lower
     *  "arms" (one on each side) that hold it, going to the edges of the screen. */
    private static final int OPTIONS_PANEL_W = 280;       // center box (the line under "Options" fits inside)
    private static final int OPTIONS_ARM_INSET = 20;      // the arms are this much shorter at the top and at the bottom
    private static final int EDGE_LIGHT = 0x40FFFFFF;     // vertical borders of the boxes (like vanilla's separators)
    private static final int EDGE_DARK = 0xBF000000;
    private static final long LOGO_FLOAT_TIME = 5000L;     // one slow "breath" every 5 seconds
    private static final float LOGO_FLOAT_AMOUNT = 2.5F;   // how many pixels it goes up and down
    private static final int HEART_COUNT = 8;
    private static final long HEART_LIFE = 1500L;
    /** Your CurseForge profile (opened when you click "Made with <3", after vanilla's "Open this link?"). */
    private static final URI PROFILE_URL = URI.create("https://www.curseforge.com/members/sam_mc123/projects");
    private static final int FUN_SPLASH_COUNT = 6;        // travelyourearth.config.splash.fun.0 ... .5
    private static final long FUN_SPLASH_TIME = 3000L;
    private static final int FOOTER_COLOR = 0x9A9A9A;   // gray text of the bottom band (like Jade's "Made with")
    private static final int HEART_COLOR = 0xE04848;
    private static final int FOOTER_HOVER_COLOR = 0xFFFFFF;  // "Made with <3" lights up when the mouse is on it
    private static final int HEART_HOVER_COLOR = 0xFF7A7A;
    private static final long PRESS_TIME = 220L;            // the "pressed" squish of "Made with <3"
    private static final float PRESS_AMOUNT = 0.15F;        // shrinks 15% and comes back
    private static final float HOVER_SPEED = 0.2F;          // how fast it lights up / fades back
    private static final int LINE_LIGHT = 0xFFAAAAAA;   // light gray (the line)
    private static final int LINE_DARK = 0xFF2A2A2A;    // dark gray (its shadow)   // fixed: it holds the icons, which have a fixed size
    private static final int CELL_W = 84;           // space for each icon + its label
    private static final int ROW_H = 34;            // 24 icon + label

    /** Vanilla textures for the bands, exactly like the options / NeoForge config screens. */
    private static final Identifier MENU_LIST_BACKGROUND = Identifier.withDefaultNamespace("textures/gui/menu_list_background.png");
    private static final Identifier INWORLD_MENU_LIST_BACKGROUND = Identifier.withDefaultNamespace("textures/gui/inworld_menu_list_background.png");

    private final Screen parent;
    private final ModContainer container;

    // Layout: recalculated in init() (also when the window or the GUI scale changes),
    // proportional to the screen size so it looks the same at every GUI scale.
    private int topBandH;
    private int bottomBandH;
    private int optionsBandTop;
    private int logoTop;
    private int logoW;
    private int logoH;

    // "Made with <3 by Sam_MC123": an invisible clickable area; the text is drawn by us
    // (it lights up instead of getting underlined, and squishes when you press it)
    private PlainTextButton madeByButton;
    private int madeByX;
    private int madeByY;
    private float madeByHover;          // 0 = normal, 1 = fully lit (goes smoothly between them)
    private long pressStart = -1;

    // Surprise when you click "Made with <3 by Sam_MC123"
    private final List<Heart> hearts = new ArrayList<>();
    private final RandomSource random = RandomSource.create();
    private boolean madeByWasHovered;   // to throw the hearts only when the mouse ARRIVES
    private long funSplashUntil = -1;
    private int funSplashIndex;

    /** A floating heart: where it is, how it moves and when it was born. */
    private static final class Heart {
        float x, y, speedX, speedY;
        final long born;

        Heart(float x, float y, float speedX, float speedY, long born) {
            this.x = x; this.y = y; this.speedX = speedX; this.speedY = speedY; this.born = born;
        }
    }

    public TravelYourEarthConfigScreen(ModContainer container, Screen parent) {
        super(Component.translatable("travelyourearth.config.title"));
        this.container = container;
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        calculateLayout();

        // --- Bottom band: version (left), "Made with <3 by Sam_MC123" (center), credits (right) ---
        int textY = this.height - this.bottomBandH + (this.bottomBandH - this.font.lineHeight) / 2 + 1;
        StringWidget version = this.addRenderableOnly(new StringWidget(
                Component.literal("v" + this.container.getModInfo().getVersion()).withColor(FOOTER_COLOR), this.font));
        version.setPosition(8, textY);

        // Clickable: opens your CurseForge profile. The button has NO text (so vanilla doesn't draw
        // the underline); we draw the text ourselves in extractMadeBy(). With the mouse on it the text
        // becomes "Visit my profile" and some hearts fly up; it's as wide as the longer of the two texts.
        int madeByW = Math.max(this.font.width(madeBy(false)), this.font.width(visitProfile(FOOTER_COLOR)));
        this.madeByX = centerX;
        this.madeByY = textY;
        this.madeByButton = this.addRenderableWidget(new PlainTextButton(centerX - madeByW / 2, textY - 2,
                madeByW, this.font.lineHeight + 4, Component.empty(), b -> {
            this.pressStart = Util.getMillis();
            openProfile();
        }, this.font));

        StringWidget credits = this.addRenderableOnly(new StringWidget(
                Component.translatable("travelyourearth.config.credits", "Bobicraft").withColor(FOOTER_COLOR), this.font));
        credits.setPosition(this.width - credits.getWidth() - 8, textY);

        // --- Options band: 2 icons on top, 3 below (a pyramid, centered) ---
        // "Options" title: drawn (bigger) in extractRenderState, with its line under it

        int row1Y = this.optionsBandTop + OPTIONS_HEADER_H;
        int row2Y = row1Y + ROW_H + 4;

        // Client and Local: our own Jade-style screens
        addIcon(centerX - CELL_W / 2, row1Y, "client", "travelyourearth.config.client",
                b -> this.minecraft.gui.setScreen(TravelYourEarthOptionsScreen.client(this)), true);
        addIcon(centerX + CELL_W / 2, row1Y, "local", "travelyourearth.config.local",
                b -> this.minecraft.gui.setScreen(TravelYourEarthOptionsScreen.local(this)), true);

        // Bugs / Feedback: first choose "on the website" (GitHub) or "in the game" (form page by page)
        addIcon(centerX - CELL_W, row2Y, "bugs", "travelyourearth.config.bugs",
                b -> this.minecraft.gui.setScreen(new ReportChoiceScreen(this, ReportChoiceScreen.Kind.BUG)), true);
        addIcon(centerX, row2Y, "feedback", "travelyourearth.config.feedback",
                b -> this.minecraft.gui.setScreen(new ReportChoiceScreen(this, ReportChoiceScreen.Kind.FEEDBACK)), true);
        addIcon(centerX + CELL_W, row2Y, "wiki", "travelyourearth.config.wiki", b -> {}, false); // coming soon

        // --- Back button, like Bedrock's: top-left corner of the top band ---
        // Sprites: textures/gui/sprites/config/back.png and back_highlighted.png (22 x 22)
        WidgetSprites backSprites = new WidgetSprites(id("config/back"), id("config/back"), id("config/back_highlighted"));
        ImageButton back = new ImageButton(BACK_MARGIN, (this.topBandH - BACK_SIZE) / 2, BACK_SIZE, BACK_SIZE,
                backSprites, b -> this.onClose(), CommonComponents.GUI_BACK);
        this.addRenderableWidget(back);

        // "Back" text next to the arrow (vanilla translation: "Atrás" in Spanish)
        StringWidget backLabel = this.addRenderableOnly(new StringWidget(ModFonts.title(CommonComponents.GUI_BACK), this.font));
        backLabel.setPosition(BACK_MARGIN + BACK_SIZE + 5, (this.topBandH - this.font.lineHeight) / 2 + 1);
    }

    /** One icon button (centered on centerX) with its name under it. */
    private void addIcon(int centerX, int y, String name, String langKey, Button.OnPress onPress, boolean active) {
        WidgetSprites sprites = new WidgetSprites(
                id("config/" + name),
                id(active ? "config/" + name : "config/" + name + "_disabled"),
                id("config/" + name + "_highlighted"));
        ImageButton button = new ImageButton(centerX - ICON_SIZE / 2, y, ICON_SIZE, ICON_SIZE,
                sprites, onPress, Component.translatable(langKey));
        button.active = active;
        if (!active) {
            button.setTooltip(Tooltip.create(Component.translatable("travelyourearth.config.coming_soon")));
        }
        this.addRenderableWidget(button);

        StringWidget label = this.addRenderableOnly(new StringWidget(
                Component.translatable(langKey).withColor(active ? 0xE0E0E0 : 0x808080), this.font));
        placeCentered(label, centerX, y + ICON_SIZE + 3);
    }

    // ---------------------------------------------------------------------
    // Drawing
    // ---------------------------------------------------------------------

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        // The 3 bands, made exactly like vanilla's menus: darker list background + separator lines
        band(graphics, 0, this.topBandH, false, true);                                              // top
        optionsBand(graphics);                                                                      // options
        band(graphics, this.height - this.bottomBandH, this.height, true, false);                   // bottom

        // Line under the "Options" title, like the one in your picture: a light gray line with
        // its dark "shadow" 1 pixel lower and 1 pixel to the right (that's what makes the little border)
        int lineW = Math.min(OPTIONS_LINE_MAX_W, this.width - 2 * OPTIONS_LINE_MARGIN);
        int lineX = (this.width - lineW) / 2;
        int lineY = this.optionsBandTop + 20;
        graphics.fill(lineX, lineY, lineX + lineW, lineY + 1, LINE_LIGHT);
        graphics.fill(lineX + 1, lineY + 1, lineX + lineW + 1, lineY + 2, LINE_DARK);

        // "Options", a bit bigger than the normal text (scaled from its center)
        Component optionsTitle = ModFonts.title(Component.translatable("travelyourearth.config.options"));
        graphics.pose().pushMatrix();
        graphics.pose().translate(this.width / 2.0F, this.optionsBandTop + 6.0F);
        graphics.pose().scale(OPTIONS_TITLE_SCALE);
        graphics.centeredText(this.font, optionsTitle, 0, 0, 0xFFFFFFFF);
        graphics.pose().popMatrix();

        // Logo, floating gently up and down. The offset is a FLOAT moved with the pose (not whole pixels),
        // so it glides smoothly instead of jumping 1 pixel at a time.
        int logoX = (this.width - this.logoW) / 2;
        float phase = (float) (Util.getMillis() % LOGO_FLOAT_TIME) / LOGO_FLOAT_TIME * ((float) Math.PI * 2F);
        // "Menu effects" option off (Config -> Client): the logo stays still
        float floatY = effects() ? Mth.sin(phase) * LOGO_FLOAT_AMOUNT : 0.0F;
        graphics.pose().pushMatrix();
        graphics.pose().translate(0.0F, floatY);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, LOGO, logoX, this.logoTop, this.logoW, this.logoH);
        graphics.pose().popMatrix();

        // Buttons and labels
        super.extractRenderState(graphics, mouseX, mouseY, a);

        extractMadeBy(graphics);

        // Yellow splash text on the corner of the logo, exactly like the title screen's
        extractSplash(graphics, logoX + this.logoW * 86 / 100, this.logoTop + floatY + this.logoH * 78 / 100);

        extractHearts(graphics);
    }

    // ---------------------------------------------------------------------
    // Surprise! (click on "Made with <3 by Sam_MC123")
    // ---------------------------------------------------------------------

    /** "Menu effects" option (Config -> Client): animations of this menu on/off. */
    private static boolean effects() {
        return Config.MENU_EFFECTS.get();
    }

    /** Vanilla's "Do you want to open this link?" window, then back to this menu. */
    private void openProfile() {
        this.minecraft.gui.setScreen(new ConfirmLinkScreen(open -> {
            if (open) {
                Blaze3D.openUri(PROFILE_URL);
            }
            this.minecraft.gui.setScreen(this);
        }, PROFILE_URL, true));
    }

    /** The mouse just arrived on "Made with <3": a burst of hearts flies up from the text. */
    private void throwHearts() {
        if (!effects()) return;
        long now = Util.getMillis();
        for (int i = 0; i < HEART_COUNT; i++) {
            this.hearts.add(new Heart(
                    this.madeByX + (this.random.nextFloat() - 0.5F) * 40.0F, this.madeByY,
                    (this.random.nextFloat() - 0.5F) * 1.6F,
                    -1.2F - this.random.nextFloat() * 1.4F,
                    now));
        }
    }

    /** Hearts float up, drift a bit to the sides and fade out in 1.5 seconds. */
    private void extractHearts(GuiGraphicsExtractor graphics) {
        if (this.hearts.isEmpty()) return;
        long now = Util.getMillis();
        ActiveTextCollector textRenderer = graphics.textRenderer();
        Component heart = Component.literal("\u2764").withColor(HEART_COLOR);
        this.hearts.removeIf(h -> now - h.born > HEART_LIFE);
        for (Heart h : this.hearts) {
            h.x += h.speedX;
            h.y += h.speedY;
            h.speedY += 0.02F; // slows down a bit while it goes up
            float alpha = 1.0F - (now - h.born) / (float) HEART_LIFE;
            float scale = 1.0F + (now - h.born) / (float) HEART_LIFE;
            Matrix3x2f pose = new Matrix3x2f(textRenderer.defaultParameters().pose()).translate(h.x, h.y).scale(scale);
            textRenderer.accept(TextAlignment.LEFT, -3, -4,
                    textRenderer.defaultParameters().withOpacity(alpha).withPose(pose), heart);
        }
    }

    /** "Visit my profile" (shown instead of "Made with <3" while the mouse is on it). */
    private static Component visitProfile(int color) {
        return Component.translatable("travelyourearth.config.visit_profile").withColor(color);
    }

    /** "Made with <3 by Sam_MC123", lit up while the mouse is on it. */
    private Component madeBy(boolean lit) {
        return madeByColored(lit ? FOOTER_HOVER_COLOR : FOOTER_COLOR, lit ? HEART_HOVER_COLOR : HEART_COLOR);
    }

    private Component madeByColored(int textColor, int heartColor) {
        return Component.translatable("travelyourearth.config.made_with").withColor(textColor)
                .append(Component.literal(" \u2764 ").withColor(heartColor))
                .append(Component.translatable("travelyourearth.config.made_by", "Sam_MC123").withColor(textColor));
    }

    /**
     * Draws "Made with <3 by Sam_MC123": it lights up smoothly when you hover it,
     * and when you click it squishes like a button being pressed and pops back.
     */
    private void extractMadeBy(GuiGraphicsExtractor graphics) {
        if (this.madeByButton == null) return;

        // Smooth light up / fade back (a little each frame)
        // Only when the mouse is on it or while you press it. (NOT "focused": after a click vanilla keeps
        // the button focused, and that's why it stayed white before.)
        boolean hovered = this.madeByButton.isHovered();
        if (hovered && !this.madeByWasHovered) {
            throwHearts(); // the mouse just arrived
        }
        this.madeByWasHovered = hovered;

        float target = hovered || this.pressStart >= 0 ? 1.0F : 0.0F;
        this.madeByHover += (target - this.madeByHover) * HOVER_SPEED;
        int textColor = lerpColor(FOOTER_COLOR, FOOTER_HOVER_COLOR, this.madeByHover);
        // Mouse on it: "Visit my profile" instead of "Made with <3 by Sam_MC123"
        Component text = hovered
                ? visitProfile(textColor)
                : madeByColored(textColor, lerpColor(HEART_COLOR, HEART_HOVER_COLOR, this.madeByHover));

        // Press: shrinks quickly and pops back with a tiny bounce
        float scale = 1.0F;
        if (this.pressStart >= 0) {
            float t = (Util.getMillis() - this.pressStart) / (float) PRESS_TIME;
            if (t >= 1.0F) {
                this.pressStart = -1;
            } else {
                scale = 1.0F - Mth.sin(t * (float) Math.PI) * PRESS_AMOUNT;
            }
        }

        // Drawn from its center, so it shrinks towards the middle (like a real button)
        int textW = this.font.width(text);
        ActiveTextCollector textRenderer = graphics.textRenderer();
        Matrix3x2f pose = new Matrix3x2f(textRenderer.defaultParameters().pose())
                .translate(this.madeByX, this.madeByY + this.font.lineHeight / 2.0F)
                .scale(scale);
        textRenderer.accept(TextAlignment.LEFT, -textW / 2, -this.font.lineHeight / 2,
                textRenderer.defaultParameters().withPose(pose), text);
    }

    /** Mixes two RGB colors: amount 0 = from, 1 = to. */
    private static int lerpColor(int from, int to, float amount) {
        int r = Math.round(Mth.lerp(amount, (from >> 16) & 0xFF, (to >> 16) & 0xFF));
        int g = Math.round(Mth.lerp(amount, (from >> 8) & 0xFF, (to >> 8) & 0xFF));
        int b = Math.round(Mth.lerp(amount, from & 0xFF, to & 0xFF));
        return (r << 16) | (g << 8) | b;
    }

    /**
     * Copy of vanilla's SplashRenderer (26.3), but placed on OUR logo instead of the title screen's:
     * yellow, tilted -20 degrees and "beating" (it grows and shrinks once per second).
     */
    private void extractSplash(GuiGraphicsExtractor graphics, float x, float y) {
        String splashKey = Util.getMillis() < this.funSplashUntil
                ? "travelyourearth.config.splash.fun." + this.funSplashIndex
                : "travelyourearth.config.splash";
        Component splash = Component.translatable(splashKey).withColor(SPLASH_COLOR);
        int textWidth = this.font.width(splash);
        ActiveTextCollector textRenderer = graphics.textRenderer();
        float textPhase = !effects() ? 1.8F : 1.8F - Mth.abs(Mth.sin((double) ((float) (Util.getMillis() % 1000L) / 1000.0F * ((float) Math.PI * 2F))) * 0.1F);
        float textScale = textPhase * SPLASH_SIZE / (float) (textWidth + 32);
        Matrix3x2f transform = new Matrix3x2f(textRenderer.defaultParameters().pose())
                .translate(x, y)
                .rotate(SPLASH_ANGLE)
                .scale(textScale);
        ActiveTextCollector.Parameters parameters = textRenderer.defaultParameters().withPose(transform);
        textRenderer.accept(TextAlignment.LEFT, -textWidth / 2, -8, parameters, splash);
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    /**
     * Everything is a part of the screen height, so the screen looks the same with any window size
     * and GUI scale (with GUI scale 4 or a small window everything gets smaller, but nothing overlaps).
     */
    private void calculateLayout() {
        this.topBandH = Mth.clamp(Math.round(this.height * 0.065F), 22, 32);
        this.bottomBandH = Mth.clamp(Math.round(this.height * 0.10F), 26, 48);   // a bit taller than before

        // Options band well above the middle, but never touching the bottom band
        int maxOptionsTop = this.height - this.bottomBandH - 8 - OPTIONS_BAND_H;
        this.optionsBandTop = Math.min(Math.round(this.height * 0.40F), maxOptionsTop);

        // Logo: under the top band, as big as it fits (up to 320 wide / 70% of the screen)
        this.logoTop = this.topBandH + Mth.clamp(Math.round(this.height * 0.06F), 6, 32);
        int maxLogoH = this.optionsBandTop - this.logoTop - 10;
        int byWidth = Math.min(320, this.width * 70 / 100);
        int byHeight = maxLogoH * LOGO_TEXTURE_W / LOGO_TEXTURE_H;
        this.logoW = Math.max(100, Math.min(byWidth, byHeight));
        this.logoH = this.logoW * LOGO_TEXTURE_H / LOGO_TEXTURE_W;
    }

    /**
     * A band like vanilla's list area: the darker list background, with the header separator line on top
     * and/or the footer separator line at the bottom (the "in world" versions while you're playing).
     */
    private void band(GuiGraphicsExtractor graphics, int top, int bottom, boolean lineOnTop, boolean lineAtBottom) {
        boolean inWorld = this.minecraft.level != null;
        Screen.extractMenuBackgroundTexture(graphics, inWorld ? INWORLD_MENU_LIST_BACKGROUND : MENU_LIST_BACKGROUND,
                0, top, 0.0F, 0.0F, this.width, bottom - top);
        if (lineOnTop) {
            Identifier line = inWorld ? Screen.INWORLD_FOOTER_SEPARATOR : Screen.FOOTER_SEPARATOR;
            graphics.blit(RenderPipelines.GUI_TEXTURED, line, 0, top - 1, 0.0F, 0.0F, this.width, 2, 32, 2);
        }
        if (lineAtBottom) {
            Identifier line = inWorld ? Screen.INWORLD_HEADER_SEPARATOR : Screen.HEADER_SEPARATOR;
            graphics.blit(RenderPipelines.GUI_TEXTURED, line, 0, bottom - 1, 0.0F, 0.0F, this.width, 2, 32, 2);
        }
    }

    /**
     * The options band, like the sketch:
     *
     *  ┌───────────┐┌──────────────┐┌───────────┐
     *  │  left arm ││    center    ││ right arm │     <- the arms are shorter (top and bottom)
     *  └───────────┘│  (options)   │└───────────┘
     *               └──────────────┘
     * The arms go first, so the center box is drawn on top of where they meet.
     */
    private void optionsBand(GuiGraphicsExtractor graphics) {
        int top = this.optionsBandTop;
        int bottom = top + OPTIONS_BAND_H;
        int panelW = Math.min(OPTIONS_PANEL_W, this.width - 40);
        int panelLeft = (this.width - panelW) / 2;
        int panelRight = panelLeft + panelW;

        // Arms (only if there's space on the sides)
        if (panelLeft > 0) {
            box(graphics, 0, panelLeft, top + OPTIONS_ARM_INSET, bottom - OPTIONS_ARM_INSET, false, true);
            box(graphics, panelRight, this.width, top + OPTIONS_ARM_INSET, bottom - OPTIONS_ARM_INSET, true, false);
        }
        // Center box with the options
        box(graphics, panelLeft, panelRight, top, bottom, true, true);
    }

    /**
     * A box with the same look as the bands (vanilla's darker list background + separator lines):
     * separators on top and at the bottom, and thin borders on the sides you ask for.
     */
    private void box(GuiGraphicsExtractor graphics, int left, int right, int top, int bottom,
                     boolean edgeLeft, boolean edgeRight) {
        boolean inWorld = this.minecraft.level != null;
        int w = right - left;
        Screen.extractMenuBackgroundTexture(graphics, inWorld ? INWORLD_MENU_LIST_BACKGROUND : MENU_LIST_BACKGROUND,
                left, top, left, top, w, bottom - top);
        Identifier topLine = inWorld ? Screen.INWORLD_FOOTER_SEPARATOR : Screen.FOOTER_SEPARATOR;
        Identifier bottomLine = inWorld ? Screen.INWORLD_HEADER_SEPARATOR : Screen.HEADER_SEPARATOR;
        graphics.blit(RenderPipelines.GUI_TEXTURED, topLine, left, top - 1, 0.0F, 0.0F, w, 2, 32, 2);
        graphics.blit(RenderPipelines.GUI_TEXTURED, bottomLine, left, bottom - 1, 0.0F, 0.0F, w, 2, 32, 2);
        // Side borders: 2 pixels (dark + light), like the separators turned sideways
        if (edgeLeft) {
            graphics.fill(left - 1, top, left, bottom, EDGE_DARK);
            graphics.fill(left, top, left + 1, bottom, EDGE_LIGHT);
        }
        if (edgeRight) {
            graphics.fill(right - 1, top, right, bottom, EDGE_LIGHT);
            graphics.fill(right, top, right + 1, bottom, EDGE_DARK);
        }
    }

    private void placeCentered(StringWidget widget, int centerX, int y) {
        widget.setPosition(centerX - widget.getWidth() / 2, y);
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(this.parent);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(TravelYourEarth.MODID, path);
    }
}