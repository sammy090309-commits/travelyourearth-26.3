package com.sam_mc.travelyourearth.client.config.report;

import com.mojang.blaze3d.Blaze3D;
import com.sam_mc.travelyourearth.TravelYourEarth;
import com.sam_mc.travelyourearth.client.ModFonts;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The in-game form, page by page (like the GitHub template), for bugs and feedback.
 *
 *                     Report a bug
 *                   Page 2 of 5  ● ● ○ ○ ○
 *        ┌──────────────────────────────────────────┐
 *        │ Short title *                            │
 *        │ [______________________________________] │
 *        │ What happened? *                         │
 *        │ [                                      ] │
 *        │ [                                      ] │
 *        └──────────────────────────────────────────┘
 *  [ Exit ]                         [ ‹ Back ] [ Next › ]
 *
 * Bug:      Info (automatic) -> What happened -> How to reproduce -> Other mods + log -> Stars + Send
 * Feedback: What you liked -> What you'd change -> Ideas -> Stars + Send
 *
 * "Send" opens GitHub in the browser with everything already written (see ReportSender):
 * the player only presses "Create". Fields with * are required to go to the next page.
 */
public class ReportFormScreen extends Screen {

    // =====================================================================
    // Pages
    // =====================================================================

    private enum Type {
        INFO,   // versions + singleplayer / server (automatic)
        TEXT,   // one line
        AREA,   // several lines
        LOG,    // "attach latest.log" toggle
        STARS   // ★★★★★
    }

    /** One thing to fill. id = same id as in the GitHub template (.yml). */
    private record Field(Type type, String id, boolean required, int height) {}

    private record Page(String name, List<Field> fields) {}

    private static Field text(String id, boolean required) {
        return new Field(Type.TEXT, id, required, 0);
    }

    private static Field area(String id, boolean required, int height) {
        return new Field(Type.AREA, id, required, height);
    }

    private static List<Page> bugPages() {
        return List.of(
                new Page("info", List.of(new Field(Type.INFO, "info", false, 0))),
                new Page("what", List.of(text("title", true), area("description", true, 70))),
                new Page("how", List.of(area("steps", true, 55), area("expected", false, 40))),
                new Page("extra", List.of(area("other-mods", false, 60), new Field(Type.LOG, "log", false, 0))),
                new Page("rating", List.of(new Field(Type.STARS, "rating", true, 0))));
    }

    private static List<Page> feedbackPages() {
        return List.of(
                new Page("liked", List.of(area("liked", false, 90))),
                new Page("improve", List.of(area("improve", false, 90))),
                new Page("ideas", List.of(area("ideas", false, 90))),
                new Page("rating", List.of(new Field(Type.STARS, "rating", true, 0))));
    }

    // =====================================================================
    // Layout and colors
    // =====================================================================
    private static final int MAX_COLUMN_W = 340;
    private static final int TOP = 54;              // where the panel starts
    private static final int BOTTOM_BAND_H = 33;
    private static final int LABEL_H = 12;
    private static final int GAP = 8;
    private static final int TEXT_LIMIT = 1500;
    private static final int STAR_SIZE = 22;

    private static final int PANEL_COLOR = 0x66101418;
    private static final int PANEL_BORDER = 0x40FFFFFF;
    private static final int LABEL_COLOR = 0xFFFFFFFF;
    private static final int HINT_COLOR = 0xFFA0A0A0;
    private static final int REQUIRED_COLOR = 0xFFFF6060;
    private static final int STAR_ON = 0xFFFFC83C;      // gold
    private static final int STAR_OFF = 0xFF4A4A4A;
    private static final int DOT_ON = 0xFFFFFFFF;
    private static final int DOT_OFF = 0xFF555555;
    private static final int SEND_COLOR = 0xFF55FF55;
    private static final int OK_COLOR = 0xFF55FF55;

    private static final String[] SIDES = {"Singleplayer", "Multiplayer server", "Both"};

    // =====================================================================
    // State
    // =====================================================================
    private final Screen parent;
    private final ReportChoiceScreen.Kind kind;
    private final List<Page> pages;
    /** Everything written so far (field id -> text). It survives page changes and window resizes. */
    private final Map<String, String> values = new HashMap<>();
    private int page;
    private int side;
    private int rating;
    private int hoveredStar = -1;
    private boolean attachLog;
    private boolean sending;
    private boolean done;
    /** The log couldn't be uploaded (shown on the "done" page). */
    private boolean logFailed;

    // Placed in init()
    private final List<Label> labels = new ArrayList<>();
    private final List<StarButton> stars = new ArrayList<>();
    private int columnX;
    private int columnW;
    private int panelBottom;
    private int infoY = -1;
    private FlatButton nextButton;

    private record Label(Component text, int x, int y, int color) {}

    public ReportFormScreen(Screen parent, ReportChoiceScreen.Kind kind) {
        super(Component.translatable("travelyourearth.report." + kind.name + ".title"));
        this.parent = parent;
        this.kind = kind;
        this.pages = kind == ReportChoiceScreen.Kind.BUG ? bugPages() : feedbackPages();
        // Filled by the game, the player can still change them
        this.values.put("other-mods", ReportSender.otherMods());
        this.side = ReportSender.detectSide();
    }

    // =====================================================================
    // Widgets
    // =====================================================================

    @Override
    protected void init() {
        this.labels.clear();
        this.stars.clear();
        this.infoY = -1;
        this.nextButton = null;
        this.columnW = Math.min(MAX_COLUMN_W, this.width - 40);
        this.columnX = (this.width - this.columnW) / 2;
        int buttonY = this.height - BOTTOM_BAND_H + (BOTTOM_BAND_H - 20) / 2;

        if (this.done || this.sending) {
            // "Sending..." / "Done!" page: just one button to go back
            this.panelBottom = TOP + 70;
            if (this.done) {
                this.addRenderableWidget(new FlatButton((this.width - 120) / 2, TOP + 80, 120, 20,
                        Component.translatable("travelyourearth.report.back_to_menu"), b -> this.onClose()));
            }
            return;
        }

        int y = TOP + 10;
        for (Field field : this.pages.get(this.page).fields()) {
            y = addField(field, y);
        }
        this.panelBottom = y;

        // --- Bottom: [Exit] ......... [‹ Back] [Next ›] / [Send ✔] ---
        this.addRenderableWidget(new FlatButton(8, buttonY, 70, 20,
                Component.translatable("travelyourearth.config.exit"), b -> this.onClose()));
        FlatButton back = this.addRenderableWidget(new FlatButton(this.width - 2 * 90 - 12, buttonY, 90, 20,
                Component.translatable("travelyourearth.report.previous"), b -> goTo(this.page - 1)));
        back.active = this.page > 0;

        boolean last = this.page == this.pages.size() - 1;
        this.nextButton = this.addRenderableWidget(new FlatButton(this.width - 90 - 8, buttonY, 90, 20,
                Component.translatable(last ? "travelyourearth.report.send" : "travelyourearth.report.next"),
                b -> {
                    if (last) send();
                    else goTo(this.page + 1);
                }));
        if (last) this.nextButton.withTextColor(SEND_COLOR);
    }

    /** Adds one field (its label + its widget) at y, and returns where the next one goes. */
    private int addField(Field field, int y) {
        Component label = Component.translatable("travelyourearth.report.field." + field.id());
        if (field.required()) label = label.copy().append(Component.literal(" *").withColor(REQUIRED_COLOR));

        switch (field.type()) {
            case INFO -> {
                // 3 lines of automatic info (drawn in extractRenderState) + the singleplayer / server button
                this.infoY = y;
                y += 3 * 12 + 6;
                this.labels.add(new Label(Component.translatable("travelyourearth.report.field.side"), this.columnX, y + 6, LABEL_COLOR));
                this.addRenderableWidget(new FlatButton(this.columnX + this.columnW - 150, y, 150, 20,
                        sideName(), b -> {
                    this.side = (this.side + 1) % SIDES.length;
                    b.setMessage(sideName());
                }));
                return y + 20 + GAP;
            }
            case TEXT -> {
                this.labels.add(new Label(label, this.columnX, y, LABEL_COLOR));
                EditBox box = new EditBox(this.font, this.columnX, y + LABEL_H, this.columnW, 18, label);
                box.setMaxLength(100);
                box.setHint(Component.translatable("travelyourearth.report.field." + field.id() + ".hint").withColor(0x808080));
                box.setValue(this.values.getOrDefault(field.id(), ""));
                box.setResponder(value -> this.values.put(field.id(), value));
                this.addRenderableWidget(box);
                return y + LABEL_H + 18 + GAP;
            }
            case AREA -> {
                this.labels.add(new Label(label, this.columnX, y, LABEL_COLOR));
                MultiLineEditBox box = MultiLineEditBox.builder()
                        .setX(this.columnX)
                        .setY(y + LABEL_H)
                        .setPlaceholder(Component.translatable("travelyourearth.report.field." + field.id() + ".hint"))
                        .build(this.font, this.columnW, field.height(), label);
                box.setCharacterLimit(TEXT_LIMIT);
                box.setValue(this.values.getOrDefault(field.id(), ""));
                box.setValueListener(value -> this.values.put(field.id(), value));
                this.addRenderableWidget(box);
                // + room for vanilla's "123/1500" counter under the box
                return y + LABEL_H + field.height() + 16 + GAP;
            }
            case LOG -> {
                this.addRenderableWidget(new LogToggle(this.columnX, y));
                this.labels.add(new Label(Component.translatable("travelyourearth.report.field.log"),
                        this.columnX + 29 + 6, y + 4, LABEL_COLOR));
                this.labels.add(new Label(Component.translatable("travelyourearth.report.field.log.hint"),
                        this.columnX + 29 + 6, y + 16, HINT_COLOR));
                return y + 30 + GAP;
            }
            case STARS -> {
                this.labels.add(new Label(Component.translatable("travelyourearth.report.field.rating." + this.kind.name),
                        -1, y, LABEL_COLOR)); // -1 = centered
                int starsW = 5 * STAR_SIZE + 4 * 6;
                int starX = (this.width - starsW) / 2;
                for (int i = 0; i < 5; i++) {
                    this.stars.add(this.addRenderableWidget(new StarButton(starX + i * (STAR_SIZE + 6), y + 16, i)));
                }
                return y + 16 + STAR_SIZE + 14 + GAP;
            }
        }
        return y;
    }

    private Component sideName() {
        return Component.translatable("travelyourearth.report.side." + this.side);
    }

    private void goTo(int page) {
        this.page = page;
        this.rebuildWidgets();
    }

    // =====================================================================
    // Drawing
    // =====================================================================

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        // Panel behind the fields (drawn first so the widgets stay on top)
        graphics.fill(this.columnX - 10, TOP, this.columnX + this.columnW + 10, this.panelBottom, PANEL_COLOR);
        graphics.outline(this.columnX - 10, TOP, this.columnW + 20, this.panelBottom - TOP, PANEL_BORDER);

        // "Next" only works when the required fields of this page are filled
        if (this.nextButton != null) {
            this.nextButton.active = pageComplete();
        }

        // Which star is under the mouse (to light them up before clicking)
        this.hoveredStar = -1;
        for (StarButton star : this.stars) {
            if (star.isHovered()) this.hoveredStar = star.index;
        }

        super.extractRenderState(graphics, mouseX, mouseY, a);

        // Title, "Page 2 of 5" and the dots ● ● ○ ○ ○
        graphics.centeredText(this.font, ModFonts.title(this.title), this.width / 2, 12, LABEL_COLOR);
        if (!this.done && !this.sending) {
            Component pageName = Component.translatable("travelyourearth.report.page." + this.pages.get(this.page).name());
            graphics.centeredText(this.font, pageName, this.width / 2, 26, HINT_COLOR);
            int dotsW = this.pages.size() * 8 - 3;
            int dotX = (this.width - dotsW) / 2;
            for (int i = 0; i < this.pages.size(); i++) {
                int color = i <= this.page ? DOT_ON : DOT_OFF;
                graphics.fill(dotX + i * 8, 40, dotX + i * 8 + 5, 45, color);
            }
        }

        if (this.sending) {
            graphics.centeredText(this.font, Component.translatable("travelyourearth.report.sending"),
                    this.width / 2, TOP + 30, LABEL_COLOR);
            return;
        }
        if (this.done) {
            graphics.centeredText(this.font, ModFonts.title(Component.translatable("travelyourearth.report.done")),
                    this.width / 2, TOP + 16, OK_COLOR);
            graphics.textWithWordWrap(this.font, Component.translatable("travelyourearth.report.done.description"),
                    this.columnX, TOP + 32, this.columnW, HINT_COLOR);
            if (this.logFailed) {
                graphics.centeredText(this.font, Component.translatable("travelyourearth.report.log_failed"),
                        this.width / 2, TOP + 58, REQUIRED_COLOR);
            }
            return;
        }

        // Labels of the fields
        for (Label label : this.labels) {
            if (label.x() < 0) {
                graphics.centeredText(this.font, label.text(), this.width / 2, label.y(), label.color());
            } else {
                graphics.text(this.font, label.text(), label.x(), label.y(), label.color());
            }
        }

        // Automatic info (bug, page 1)
        if (this.infoY >= 0) {
            infoLine(graphics, "mod_version", ReportSender.modVersion(), this.infoY);
            infoLine(graphics, "neoforge_version", ReportSender.neoForgeVersion(), this.infoY + 12);
            infoLine(graphics, "minecraft_version", ReportSender.minecraftVersion(), this.infoY + 24);
        }

        // Rating text under the stars: "★★★★☆ (4/5)"
        if (!this.stars.isEmpty()) {
            int shown = this.hoveredStar >= 0 ? this.hoveredStar + 1 : this.rating;
            if (shown > 0) {
                graphics.centeredText(this.font, Component.translatable("travelyourearth.report.stars." + shown),
                        this.width / 2, this.stars.get(0).getY() + STAR_SIZE + 4, STAR_ON);
            }
        }
    }

    /** "✔ Mod version: 1.2.0-beta" */
    private void infoLine(GuiGraphicsExtractor graphics, String key, String value, int y) {
        Component line = Component.literal("✔ ").withColor(OK_COLOR)
                .append(Component.translatable("travelyourearth.report.info." + key).withColor(HINT_COLOR))
                .append(Component.literal(" " + value).withColor(LABEL_COLOR));
        graphics.text(this.font, line, this.columnX, y, LABEL_COLOR);
    }

    // =====================================================================
    // Values and sending
    // =====================================================================

    private boolean pageComplete() {
        for (Field field : this.pages.get(this.page).fields()) {
            if (!field.required()) continue;
            if (field.type() == Type.STARS) {
                if (this.rating == 0) return false;
            } else if (this.values.getOrDefault(field.id(), "").isBlank()) {
                return false;
            }
        }
        return true;
    }

    /** "Send": uploads the log first (if ticked), then opens GitHub with everything written. */
    private void send() {
        if (this.kind == ReportChoiceScreen.Kind.BUG && this.attachLog) {
            this.sending = true;
            this.rebuildWidgets();
            ReportSender.uploadLog().thenAccept(url -> this.minecraft.execute(() -> openGitHub(url)));
        } else {
            openGitHub(null);
        }
    }

    private void openGitHub(String logUrl) {
        Map<String, String> fields = new LinkedHashMap<>();
        String title;
        if (this.kind == ReportChoiceScreen.Kind.BUG) {
            title = "[Bug] " + this.values.getOrDefault("title", "").strip();
            fields.put("mod-version", ReportSender.modVersion());
            fields.put("neoforge-version", ReportSender.neoForgeVersion() + " (Minecraft " + ReportSender.minecraftVersion() + ")");
            fields.put("side", SIDES[this.side]);
            fields.put("description", this.values.get("description"));
            fields.put("steps", this.values.get("steps"));
            fields.put("expected", this.values.get("expected"));
            fields.put("rating", ReportSender.stars(this.rating));
            fields.put("other-mods", this.values.get("other-mods"));
            if (this.attachLog) {
                this.logFailed = logUrl == null;
                fields.put("log", logUrl != null ? logUrl : "(The log couldn't be uploaded from the game, I'll attach it here)");
            }
        } else {
            title = "[Feedback] " + ReportSender.stars(this.rating);
            fields.put("rating", ReportSender.stars(this.rating));
            fields.put("liked", this.values.get("liked"));
            fields.put("improve", this.values.get("improve"));
            fields.put("ideas", this.values.get("ideas"));
            fields.put("mod-version", ReportSender.modVersion());
        }

        Blaze3D.openUri(ReportSender.issueLink(this.kind.template, title, fields));
        this.sending = false;
        this.done = true;
        this.rebuildWidgets();
    }

    @Override
    public void onClose() {
        // Back to where the player chose web / game... or to the main menu once it's sent
        this.minecraft.gui.setScreen(this.done && this.parent instanceof ReportChoiceScreen choice
                ? choice.parentScreen() : this.parent);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(TravelYourEarth.MODID, path);
    }

    // =====================================================================
    // Widgets
    // =====================================================================

    /** One clickable star. All the stars up to the one under the mouse light up. */
    private class StarButton extends Button {
        private final int index;

        StarButton(int x, int y, int index) {
            super(x, y, STAR_SIZE, STAR_SIZE, Component.translatable("travelyourearth.report.stars." + (index + 1)),
                    b -> rating = ((StarButton) b).index + 1, DEFAULT_NARRATION);
            this.index = index;
        }

        @Override
        public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            int lit = hoveredStar >= 0 ? hoveredStar + 1 : rating;
            int color = this.index < lit ? STAR_ON : STAR_OFF;
            // A "★" from the font, 2.5x bigger (same trick as the splash text)
            ActiveTextCollector text = graphics.textRenderer();
            Matrix3x2f pose = new Matrix3x2f(text.defaultParameters().pose())
                    .translate(this.getX() + STAR_SIZE / 2.0F, this.getY() + STAR_SIZE / 2.0F)
                    .scale(2.5F);
            text.accept(TextAlignment.LEFT, -3, -4, text.defaultParameters().withPose(pose),
                    Component.literal("★").withColor(color));
        }
    }

    /** "Attach latest.log" with the Bedrock-style toggle sprites. */
    private class LogToggle extends Button {
        LogToggle(int x, int y) {
            super(x, y + 4, 29, 17, Component.translatable("travelyourearth.report.field.log"),
                    b -> attachLog = !attachLog, DEFAULT_NARRATION);
        }

        @Override
        public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            String sprite = "config/toggle_" + (attachLog ? "on" : "off") + (this.isHovered() ? "_hover" : "");
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, id(sprite), this.getX(), this.getY(), 29, 17);
        }
    }
}