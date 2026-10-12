package com.sam_mc.travelyourearth.client.config;

import com.sam_mc.travelyourearth.Config;
import com.sam_mc.travelyourearth.TravelYourEarth;
import com.sam_mc.travelyourearth.client.ModFonts;
import com.sam_mc.travelyourearth.config.IllegalVillagerConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Options screen in Jade's style, used for Config -> Client AND Config -> Local:
 *
 *  ┌──────────────┬──────────────────────────────────────────────────────┐
 *  │ Search...    │                      Gameplay                        │
 *  │▌[#] Progress │  Villagers ───────────────────────────────────────   │
 *  │ [#] Gameplay │ ┌────────────────────────┐ ┌────────────────────────┐│
 *  │     › Villag.│ │ [clock] Nights  < 3 d >│ │ [bell] Scared  < 1 min>││
 *  │              │ └────────────────────────┘ └────────────────────────┘│
 *  ├──────────────┴──────────────────────────────────────────────────────┤
 *  │                                     [ Exit ] [ Apply ] [ Done ]     │
 *  └─────────────────────────────────────────────────────────────────────┘
 *
 * - Categories (sidebar, with a Bedrock-style icon) can have SECTIONS (subcategories, like "› Villagers").
 * - Cards: the option's icon, its name and its control on the right:
 *     on/off options  -> Bedrock-style toggle
 *     time options    -> "<  3 days  >"  (friendly steps, never raw ticks)
 * - Hover a card to see what the option does.
 * - Exit throws the changes away, Apply saves and stays, Done saves and leaves.
 *
 * Sprites (assets/travelyourearth/textures/gui/sprites/config/):
 *   toggle_on.png / toggle_off.png (29 x 17) + _hover, _pressed, _focused, _disabled
 *   category_<name>.png (20 x 20): category_interface, category_inventories, category_progress, category_gameplay
 */
public class TravelYourEarthOptionsScreen extends Screen {

    // =====================================================================
    // Icons
    // =====================================================================

    /**
     * The 16x16 picture on a card. We DON'T use real items (graphics.item / new ItemStack):
     * in the main menu (Mods -> Config) the items aren't ready yet ("Components not bound yet" crash),
     * so we draw the item's texture directly, which works everywhere.
     */
    public interface Icon {
        void draw(GuiGraphicsExtractor graphics, int x, int y);
    }

    /** An item texture: assets/<namespace>/textures/item/<name>.png (16 x 16). */
    private static Icon itemTexture(String namespace, String name) {
        return texture(Identifier.fromNamespaceAndPath(namespace, "textures/item/" + name + ".png"));
    }

    /** A block texture: assets/<namespace>/textures/block/<name>.png (16 x 16). */
    private static Icon blockTexture(String namespace, String name) {
        return texture(Identifier.fromNamespaceAndPath(namespace, "textures/block/" + name + ".png"));
    }

    private static Icon texture(Identifier texture) {
        return (graphics, x, y) -> graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0.0F, 0.0F, 16, 16, 16, 16);
    }

    /**
     * The beacon, built from vanilla's OWN block textures, seen from the front like its item:
     * the obsidian base, the blue core and the glass on top (same sizes as vanilla's beacon model:
     * base 12 x 3, core 10 x 11, glass 16 x 16). Nothing is copied: we just draw the game's files.
     */
    private static Icon beacon() {
        Identifier obsidian = Identifier.withDefaultNamespace("textures/block/obsidian.png");
        Identifier core = Identifier.withDefaultNamespace("textures/block/beacon.png");
        Identifier glass = Identifier.withDefaultNamespace("textures/block/glass.png");
        return (graphics, x, y) -> {
            graphics.blit(RenderPipelines.GUI_TEXTURED, obsidian, x + 2, y + 13, 2.0F, 13.0F, 12, 3, 16, 16);
            graphics.blit(RenderPipelines.GUI_TEXTURED, core, x + 3, y + 2, 0.0F, 0.0F, 10, 11, 16, 16, 16, 16);
            graphics.blit(RenderPipelines.GUI_TEXTURED, glass, x, y, 0.0F, 0.0F, 16, 16, 16, 16);
        };
    }

    // =====================================================================
    // What the screens show
    // =====================================================================

    /** TICKS = time options ("5 min", "3 days"), NIGHTS = "Every 3 days". */
    public enum Unit { TICKS, NIGHTS }

    /**
     * One option: its config value, its name in the lang, its icon and (for numbers) its steps.
     * steps == null -> on/off toggle.
     */
    public record Option(String name, ModConfigSpec.ConfigValue<?> value, Icon icon, int[] steps, Unit unit) {
        boolean isToggle() {
            return this.steps == null;
        }

        String langKey() {
            return "travelyourearth.configuration." + this.name;
        }
    }

    /** A subcategory ("› Villagers"). name == null -> the options go right under the category title. */
    public record Section(String name, List<Option> options) {
        Component title() {
            return Component.translatable("travelyourearth.config.section." + this.name);
        }
    }

    /**
     * A category of the sidebar. Its Bedrock-style icon (20 x 20) is
     * textures/gui/sprites/config/category_<name>.png
     */
    public record Category(String name, List<Section> sections) {
        /** A category without subcategories. */
        static Category of(String name, Option... options) {
            return new Category(name, List.of(new Section(null, List.of(options))));
        }

        Component title() {
            return Component.translatable("travelyourearth.config.category." + this.name);
        }

        Identifier icon() {
            return id("config/category_" + this.name);
        }
    }

    /** Friendly steps for the time options (in ticks): 10 s ... 10 days. */
    private static final int[] TIME_STEPS = {
            200, 600, 1200, 2400, 6000, 12000, 24000, 48000, 72000, 120000, 168000, 240000};
    /** Steps for "smuggling nights": every 1, 2, 3... days. */
    private static final int[] NIGHT_STEPS = {1, 2, 3, 4, 5, 7, 10};

    private static Option toggle(String name, ModConfigSpec.BooleanValue value, Icon icon) {
        return new Option(name, value, icon, null, null);
    }

    private static Option time(String name, ModConfigSpec.IntValue value, Icon icon) {
        return new Option(name, value, icon, TIME_STEPS, Unit.TICKS);
    }

    private static Option nights(String name, ModConfigSpec.IntValue value, Icon icon) {
        return new Option(name, value, icon, NIGHT_STEPS, Unit.NIGHTS);
    }

    /** Config -> Client (config/travelyourearth-client.toml). */
    public static TravelYourEarthOptionsScreen client(Screen parent) {
        List<Category> categories = List.of(
                Category.of("interface",
                        toggle("showCreativeTab", Config.SHOW_CREATIVE_TAB, itemTexture(TravelYourEarth.MODID, "ruby")),
                        toggle("menuEffects", Config.MENU_EFFECTS, itemTexture("minecraft", "nether_star"))),
                Category.of("inventories",
                        toggle("enhancedBeacon", Config.ENHANCED_BEACON, beacon()),
                        toggle("darkIllegalTrades", Config.DARK_ILLEGAL_TRADES, itemTexture("minecraft", "emerald")),
                        toggle("illegalRaidWarning", Config.ILLEGAL_RAID_WARNING, itemTexture("minecraft", "ominous_bottle"))));
        return new TravelYourEarthOptionsScreen(parent, Component.translatable("travelyourearth.config.client"), "client",
                categories, List.of(Config.SPEC));
    }

    /** Config -> Local (travelyourearth-common.toml + travelyourearth-illegal.toml). */
    public static TravelYourEarthOptionsScreen local(Screen parent) {
        List<Category> categories = List.of(
                Category.of("progress",
                        toggle("modAdvancementTab", Config.MOD_ADVANCEMENT_TAB, itemTexture("minecraft", "writable_book"))),
                new Category("gameplay", List.of(
                        new Section("villagers", List.of(
                                nights("illegalNightEvery", IllegalVillagerConfig.NIGHT_EVERY, itemTexture("minecraft", "clock_00")),
                                time("illegalRestockTicks", IllegalVillagerConfig.RESTOCK_TICKS, blockTexture(TravelYourEarth.MODID, "ruby_block")),
                                time("illegalScaredTicks", IllegalVillagerConfig.SCARED_TICKS, itemTexture("minecraft", "bell")),
                                time("illegalWantedTicks", IllegalVillagerConfig.WANTED_TICKS, itemTexture("minecraft", "oak_boat")),
                                time("illegalAvoidTicks", IllegalVillagerConfig.AVOID_TICKS, itemTexture("minecraft", "spider_eye")),
                                time("illegalShunSeenTicks", IllegalVillagerConfig.SHUN_SEEN_TICKS, itemTexture("minecraft", "ender_eye")),
                                time("illegalShunWantedTicks", IllegalVillagerConfig.SHUN_WANTED_TICKS, itemTexture("minecraft", "goat_horn")),
                                time("illegalShunCaughtTicks", IllegalVillagerConfig.SHUN_CAUGHT_TICKS, itemTexture("minecraft", "lead")))))));
        return new TravelYourEarthOptionsScreen(parent, Component.translatable("travelyourearth.config.local"), "local",
                categories, List.of(Config.COMMON_SPEC, IllegalVillagerConfig.SPEC));
    }

    // =====================================================================
    // Layout and colors
    // =====================================================================
    private static final int SIDEBAR_W = 120;
    private static final int TITLE_ICON = 20;       // client / local icon next to the screen name
    private static final int SEARCH_Y = 32;
    private static final int BOTTOM_BAND_H = 33;
    private static final int CARD_H = 26;
    private static final int CARD_GAP = 4;
    private static final int HEADER_H = 20;
    private static final int SUBHEADER_H = 16;
    private static final int TOP_MARGIN = 6;
    private static final int SCROLL_STEP = 20;

    private static final int SIDEBAR_TOP = 64;      // text line of the first category in the sidebar
    private static final int CATEGORY_STEP = 26;    // category row (with its 20 x 20 icon)
    private static final int SECTION_STEP = 16;     // "› Villagers" row
    private static final int CATEGORY_ICON_SIZE = 20;
    private static final int CATEGORY_ICON_X = 10;  // Bedrock-style icon, then the name
    private static final int CATEGORY_TEXT_X = 34;
    private static final int SECTION_TEXT_X = 40;   // subcategories a bit to the right

    private static final int TOGGLE_W = 29;
    private static final int TOGGLE_H = 17;
    private static final long PRESS_TIME = 150L;    // how long the "pressed" sprite stays after a click

    private static final int ARROW_W = 12;          // "<" and ">" of the time options
    private static final int VALUE_W = 70;          // space for "10 min" / "Cada 3 días"
    private static final int STEP_W = ARROW_W * 2 + VALUE_W;

    private static final int SIDEBAR_COLOR = 0x99000000;
    private static final int CARD_COLOR = 0x66101418;           // dark, see-through (like Jade's cards)
    private static final int CARD_HOVER_COLOR = 0x88283038;
    private static final int CARD_BORDER = 0x40FFFFFF;
    private static final int CARD_HOVER_BORDER = 0x90FFFFFF;
    private static final int NAME_COLOR = 0xFFFFFFFF;
    private static final int VALUE_COLOR = 0xFFFFFF80;          // light yellow, like Sodium's values
    private static final int SUBHEADER_COLOR = 0xFFC8C8C8;
    private static final int SUBHEADER_LINE = 0x50FFFFFF;
    private static final int SIDEBAR_TEXT = 0xB0B0B0;
    private static final int SIDEBAR_TEXT_SELECTED = 0xFFFFFF;

    // Sodium-style flat buttons
    private static final int FLAT_BUTTON_W = 70;
    private static final int FLAT_BUTTON_H = 20;
    private static final int FLAT_BUTTON_GAP = 4;
    private static final int DEFAULTS_BUTTON_W = 110;
    private static final int FLAT_COLOR = 0x60000000;          // dark, see-through
    private static final int FLAT_HOVER_COLOR = 0x80383838;    // a bit lighter with the mouse on it
    private static final int FLAT_TEXT = 0xFFFFFFFF;
    private static final int FLAT_TEXT_DISABLED = 0xFF8A8A8A;  // gray while it can't be used

    private static final Identifier MENU_LIST_BACKGROUND = Identifier.withDefaultNamespace("textures/gui/menu_list_background.png");
    private static final Identifier INWORLD_MENU_LIST_BACKGROUND = Identifier.withDefaultNamespace("textures/gui/inworld_menu_list_background.png");

    // =====================================================================
    // State
    // =====================================================================
    private final Screen parent;
    /** Icon next to the screen's name at the top of the sidebar (the same as on the main menu: client / local). */
    private final Identifier titleIcon;
    /** Categories whose subcategories are folded in the sidebar (arrow pointing right). */
    private final java.util.Set<Integer> collapsed = new java.util.HashSet<>();
    private final List<Category> categories;
    /** Config files saved by Apply / Done. */
    private final List<ModConfigSpec> specs;
    /** Changes not saved yet. */
    private final Map<Option, Object> pending = new HashMap<>();
    /** "Apply" button: only active when there are changes to save. */
    private FlatButton applyButton;
    /** "Defaults" button (bottom-left): only active when some option isn't on its default value. */
    private FlatButton defaultsButton;

    // Placed in init()
    private final List<Card> cards = new ArrayList<>();
    private final List<Header> headers = new ArrayList<>();
    private final List<Header> subheaders = new ArrayList<>();
    /** Thin lines between two categories (y of each line). */
    private final List<Integer> dividers = new ArrayList<>();
    /** Sidebar rows: every category and every subcategory, with where it starts in the list. */
    private final List<SidebarEntry> sidebar = new ArrayList<>();

    private String search = "";
    private boolean searchFocused;
    private int scroll;
    private int contentHeight;
    private int selectedCategory;
    private int selectedSection = -1;   // -1 = the category itself

    private record Card(Option option, int x, int y, int w) {}

    private record Header(Component title, int x, int y) {}

    /** section == -1 -> the category row itself. anchor = y in the list (without scroll), -1 if hidden by the search. */
    private record SidebarEntry(int category, int section, int y, int anchor) {}

    private TravelYourEarthOptionsScreen(Screen parent, Component title, String iconName,
                                         List<Category> categories, List<ModConfigSpec> specs) {
        super(title);
        this.titleIcon = id("config/" + iconName);
        this.parent = parent;
        this.categories = categories;
        this.specs = specs;
        // Subcategories folded by default: every category that has any starts with its arrow closed
        for (int c = 0; c < categories.size(); c++) {
            if (categories.get(c).sections().stream().anyMatch(sec -> sec.name() != null)) {
                this.collapsed.add(c);
            }
        }
    }

    // =====================================================================
    // Widgets
    // =====================================================================

    @Override
    protected void init() {
        this.cards.clear();
        this.headers.clear();
        this.subheaders.clear();
        this.dividers.clear();
        this.sidebar.clear();
        int listBottom = this.height - BOTTOM_BAND_H;

        // --- Search box ---
        EditBox searchBox = new EditBox(this.font, 6, SEARCH_Y, SIDEBAR_W - 12, 16,
                Component.translatable("travelyourearth.config.search"));
        searchBox.setHint(Component.translatable("travelyourearth.config.search").withColor(0x808080));
        searchBox.setValue(this.search);
        searchBox.setResponder(text -> {
            if (!text.equals(this.search)) {
                this.search = text;
                this.scroll = 0;
                this.searchFocused = true;
                this.rebuildWidgets();
            }
        });
        this.addRenderableWidget(searchBox);
        if (this.searchFocused) {
            this.setInitialFocus(searchBox);
        }

        // --- Cards (and where every category / subcategory starts, for the sidebar) ---
        int contentX = SIDEBAR_W + 8;
        int contentW = this.width - contentX - 8;
        int columns = columns(contentW);
        int cardW = (contentW - (columns - 1) * CARD_GAP) / columns;

        List<Integer> anchors = new ArrayList<>(); // same order as the sidebar rows
        int y = TOP_MARGIN - this.scroll;
        for (Category category : this.categories) {
            boolean anyShown = category.sections().stream().anyMatch(s -> s.options().stream().anyMatch(this::matchesSearch));
            if (!anyShown) {
                anchors.add(-1);
                category.sections().forEach(s -> { if (s.name() != null) anchors.add(-1); });
                continue;
            }

            // Thin line between this category and the one above (same style as the subcategory line)
            if (!this.headers.isEmpty()) {
                this.dividers.add(y + 2);
                y += 6;
            }

            // Category title, centered over the cards (like Jade)
            anchors.add(y + this.scroll);
            this.headers.add(new Header(category.title(), contentX + contentW / 2, y + 6));
            y += HEADER_H;

            for (Section section : category.sections()) {
                List<Option> shown = section.options().stream().filter(this::matchesSearch).toList();
                if (section.name() != null) {
                    anchors.add(shown.isEmpty() ? -1 : y + this.scroll);
                }
                if (shown.isEmpty()) continue;

                // Subcategory title: "Villagers ─────────"
                if (section.name() != null) {
                    this.subheaders.add(new Header(section.title(), contentX + 2, y + 4));
                    y += SUBHEADER_H;
                }

                for (int i = 0; i < shown.size(); i++) {
                    int col = i % columns;
                    int x = contentX + col * (cardW + CARD_GAP);
                    // Only cards fully inside the list get their control (no scissor needed)
                    if (y >= 0 && y + CARD_H <= listBottom) {
                        addCard(shown.get(i), x, y, cardW);
                    }
                    if (col == columns - 1 || i == shown.size() - 1) {
                        y += CARD_H + CARD_GAP;
                    }
                }
                y += 2;
            }
            y += 4;
        }
        this.contentHeight = y + this.scroll;

        // --- Sidebar: categories (icon + name) and their subcategories ("› Villagers") ---
        int rowY = SIDEBAR_TOP;
        int anchorIndex = 0;
        for (int c = 0; c < this.categories.size(); c++) {
            Category category = this.categories.get(c);
            addSidebarRow(c, -1, rowY, anchors.get(anchorIndex++), category.title(), CATEGORY_TEXT_X);

            // Arrow to fold / unfold the subcategories (only if it has any)
            boolean hasSections = category.sections().stream().anyMatch(sec -> sec.name() != null);
            boolean folded = this.collapsed.contains(c);
            if (hasSections) {
                int index = c;
                this.addRenderableWidget(new ArrowButton(SIDEBAR_W - 14, rowY - 1, folded, b -> toggleFold(index)));
            }
            rowY += CATEGORY_STEP;

            for (int s = 0; s < category.sections().size(); s++) {
                Section section = category.sections().get(s);
                if (section.name() == null) continue;
                int anchor = anchors.get(anchorIndex++);
                if (folded) continue; // folded: the subcategory isn't shown in the sidebar
                int sectionY = rowY - 8; // subcategories sit close under their category
                addSidebarRow(c, s, sectionY, anchor, section.title(), SECTION_TEXT_X);
                rowY = sectionY + SECTION_STEP + 6;
            }
        }

        // --- Bottom band, Sodium style: [Exit] [Apply] [Done] ---
        int buttonY = this.height - BOTTOM_BAND_H + (BOTTOM_BAND_H - FLAT_BUTTON_H) / 2;
        int acceptX = this.width - FLAT_BUTTON_W - 8;
        int applyX = acceptX - FLAT_BUTTON_W - FLAT_BUTTON_GAP;
        int exitX = applyX - FLAT_BUTTON_W - FLAT_BUTTON_GAP;
        // Bottom-left: put every option of this screen back to its default value
        // (like the other changes, it's only saved with Apply / Done)
        this.defaultsButton = this.addRenderableWidget(new FlatButton(8, buttonY, DEFAULTS_BUTTON_W, FLAT_BUTTON_H,
                Component.translatable("travelyourearth.config.defaults"), b -> resetToDefaults()));
        this.defaultsButton.setTooltip(net.minecraft.client.gui.components.Tooltip.create(
                Component.translatable("travelyourearth.config.defaults.tooltip")));

        this.addRenderableWidget(new FlatButton(exitX, buttonY, FLAT_BUTTON_W, FLAT_BUTTON_H,
                Component.translatable("travelyourearth.config.exit"), b -> this.onClose()));
        this.applyButton = this.addRenderableWidget(new FlatButton(applyX, buttonY, FLAT_BUTTON_W, FLAT_BUTTON_H,
                Component.translatable("travelyourearth.config.apply"), b -> save()));
        this.addRenderableWidget(new FlatButton(acceptX, buttonY, FLAT_BUTTON_W, FLAT_BUTTON_H,
                Component.translatable("travelyourearth.config.accept"), b -> {
            save();
            this.onClose();
        }));
    }

    private void addSidebarRow(int category, int section, int y, int anchor, Component title, int textX) {
        int index = this.sidebar.size();
        this.sidebar.add(new SidebarEntry(category, section, y, anchor));
        boolean selected = isSelected(this.sidebar.get(index));
        PlainTextButton button = this.addRenderableWidget(new PlainTextButton(textX, y, SIDEBAR_W - textX - 16, 12, // leaves room for the fold arrow
                title.copy().withColor(anchor < 0 ? 0x606060 : selected ? SIDEBAR_TEXT_SELECTED : SIDEBAR_TEXT),
                b -> jumpTo(index), this.font));
        button.active = anchor >= 0; // hidden by the search -> can't be clicked
    }

    /** One card's control: a toggle, or "<  value  >" for numbers. */
    private void addCard(Option option, int x, int y, int w) {
        this.cards.add(new Card(option, x, y, w));
        if (option.isToggle()) {
            this.addRenderableWidget(new ToggleWidget(x + w - TOGGLE_W - 5, y + (CARD_H - TOGGLE_H) / 2, option));
        } else {
            int stepX = x + w - STEP_W - 5;
            int stepY = y + (CARD_H - TOGGLE_H) / 2;
            this.addRenderableWidget(new FlatButton(stepX, stepY, ARROW_W, TOGGLE_H,
                    Component.literal("<"), b -> step(option, -1)));
            this.addRenderableWidget(new FlatButton(stepX + ARROW_W + VALUE_W, stepY, ARROW_W, TOGGLE_H,
                    Component.literal(">"), b -> step(option, 1)));
        }
    }

    private static int columns(int contentW) {
        return contentW >= 450 ? 3 : contentW >= 290 ? 2 : 1;
    }

    // =====================================================================
    // Drawing
    // =====================================================================

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        int listBottom = this.height - BOTTOM_BAND_H;

        // Sidebar
        graphics.fill(0, 0, SIDEBAR_W, listBottom, SIDEBAR_COLOR);

        // Top of the sidebar: the screen's icon and name ("Client" / "Local"), like on the main menu
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.titleIcon, 8, 6, TITLE_ICON, TITLE_ICON);
        graphics.text(this.font, ModFonts.title(this.title), 8 + TITLE_ICON + 6, 6 + (TITLE_ICON - 8) / 2, NAME_COLOR);

        for (SidebarEntry entry : this.sidebar) {
            boolean selected = isSelected(entry);
            if (entry.section() < 0) {
                // Category: Bedrock-style icon (a bit faded if it isn't the selected one)
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.categories.get(entry.category()).icon(),
                        CATEGORY_ICON_X, entry.y() - 6, CATEGORY_ICON_SIZE, CATEGORY_ICON_SIZE,
                        selected ? 1.0F : 0.6F);
                if (selected) graphics.fill(4, entry.y() - 6, 6, entry.y() + 14, 0xFFFFFFFF);
            } else if (selected) {
                // Subcategory: a shorter white mark
                graphics.fill(4, entry.y() - 2, 6, entry.y() + 10, 0xFFFFFFFF);
            }
        }

        // Category titles (centered) and subcategory titles ("Villagers ──────")
        for (Header header : this.headers) {
            if (header.y() >= 0 && header.y() + 9 <= listBottom) {
                graphics.centeredText(this.font, ModFonts.title(header.title()), header.x(), header.y(), NAME_COLOR);
            }
        }
        int contentRight = this.width - 8;
        for (int dividerY : this.dividers) {
            if (dividerY >= 0 && dividerY < listBottom) {
                graphics.fill(SIDEBAR_W + 8, dividerY, contentRight, dividerY + 1, SUBHEADER_LINE);
            }
        }
        for (Header sub : this.subheaders) {
            if (sub.y() >= 0 && sub.y() + 9 <= listBottom) {
                Component subTitle = ModFonts.title(sub.title());
                graphics.text(this.font, subTitle, sub.x(), sub.y(), SUBHEADER_COLOR);
                int lineX = sub.x() + this.font.width(subTitle) + 6;
                graphics.fill(lineX, sub.y() + 4, contentRight, sub.y() + 5, SUBHEADER_LINE);
            }
        }

        // Cards: background, border, icon, name and (for numbers) the value
        Card hoveredCard = null;
        for (Card card : this.cards) {
            int x = card.x(), y = card.y(), w = card.w();
            boolean hovered = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + CARD_H && mouseY < listBottom;
            if (hovered) hoveredCard = card;

            graphics.fill(x, y, x + w, y + CARD_H, hovered ? CARD_HOVER_COLOR : CARD_COLOR);
            graphics.outline(x, y, w, CARD_H, hovered ? CARD_HOVER_BORDER : CARD_BORDER);

            card.option().icon().draw(graphics, x + 5, y + (CARD_H - 16) / 2);

            // Name, cut with "..." if the card is too narrow
            int controlW = card.option().isToggle() ? TOGGLE_W : STEP_W;
            int maxNameW = w - 26 - controlW - 10;
            String name = Component.translatable(card.option().langKey()).getString();
            if (this.font.width(name) > maxNameW) {
                name = this.font.plainSubstrByWidth(name, Math.max(0, maxNameW - this.font.width("..."))) + "...";
            }
            graphics.text(this.font, name, x + 26, y + (CARD_H - 8) / 2, NAME_COLOR);

            // Value between "<" and ">"
            if (!card.option().isToggle()) {
                int valueCenter = x + w - 5 - STEP_W + ARROW_W + VALUE_W / 2;
                graphics.centeredText(this.font, format(card.option(), (Integer) current(card.option())),
                        valueCenter, y + (CARD_H - 8) / 2, VALUE_COLOR);
            }
        }

        // Bottom band, like vanilla's footer
        boolean inWorld = this.minecraft.level != null;
        Screen.extractMenuBackgroundTexture(graphics, inWorld ? INWORLD_MENU_LIST_BACKGROUND : MENU_LIST_BACKGROUND,
                0, listBottom, 0.0F, 0.0F, this.width, BOTTOM_BAND_H);
        Identifier line = inWorld ? Screen.INWORLD_FOOTER_SEPARATOR : Screen.FOOTER_SEPARATOR;
        graphics.blit(RenderPipelines.GUI_TEXTURED, line, 0, listBottom - 1, 0.0F, 0.0F, this.width, 2, 32, 2);

        // "Apply" is gray (and does nothing) until you change something
        if (this.applyButton != null) {
            this.applyButton.active = !this.pending.isEmpty();
        }
        // "Defaults" is gray when everything is already on its default value
        if (this.defaultsButton != null) {
            this.defaultsButton.active = !allDefaults();
        }

        super.extractRenderState(graphics, mouseX, mouseY, a);

        // What the hovered option does (its ".tooltip" text in the lang)
        if (hoveredCard != null) {
            Component description = Component.translatable(hoveredCard.option().langKey() + ".tooltip");
            graphics.setTooltipForNextFrame(this.font, this.font.split(description, 220), mouseX, mouseY);
        }
    }

    // =====================================================================
    // Scrolling, search, sidebar
    // =====================================================================

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int newScroll = Mth.clamp(this.scroll - (int) (scrollY * SCROLL_STEP), 0, maxScroll());
        if (newScroll != this.scroll) {
            this.scroll = newScroll;
            this.searchFocused = false;
            // The sidebar follows the list: the last category / subcategory that already started
            for (SidebarEntry entry : this.sidebar) {
                if (entry.anchor() >= 0 && entry.anchor() - TOP_MARGIN <= this.scroll) select(entry);
            }
            this.rebuildWidgets();
        }
        return true;
    }

    /** Clicking a category or subcategory in the sidebar scrolls the list to it. */
    private void jumpTo(int index) {
        SidebarEntry entry = this.sidebar.get(index);
        int anchor = entry.anchor();
        if (anchor < 0) return;
        select(entry);
        this.scroll = Mth.clamp(anchor - TOP_MARGIN, 0, maxScroll());
        this.searchFocused = false;
        this.rebuildWidgets();
    }

    private void select(SidebarEntry entry) {
        this.selectedCategory = entry.category();
        this.selectedSection = entry.section();
    }

    /**
     * Is this sidebar row the selected one? If the selected subcategory is folded away,
     * its category row is shown as selected instead.
     */
    private boolean isSelected(SidebarEntry entry) {
        if (entry.category() != this.selectedCategory) return false;
        if (entry.section() == this.selectedSection) return true;
        return entry.section() < 0 && this.collapsed.contains(entry.category());
    }

    /** The arrow next to a category: folds / unfolds its subcategories in the sidebar. */
    private void toggleFold(int category) {
        if (!this.collapsed.remove(category)) {
            this.collapsed.add(category);
        }
        this.searchFocused = false;
        this.rebuildWidgets();
    }

    private int maxScroll() {
        return Math.max(0, this.contentHeight - (this.height - BOTTOM_BAND_H) + TOP_MARGIN);
    }

    private boolean matchesSearch(Option option) {
        if (this.search.isBlank()) return true;
        String name = Component.translatable(option.langKey()).getString().toLowerCase(Locale.ROOT);
        return name.contains(this.search.toLowerCase(Locale.ROOT).trim());
    }

    // =====================================================================
    // Values
    // =====================================================================

    private Object current(Option option) {
        return this.pending.containsKey(option) ? this.pending.get(option) : option.value().get();
    }

    /** Remembers a change (or forgets it if you put the saved value back). */
    private void change(Option option, Object newValue) {
        if (newValue.equals(option.value().get())) {
            this.pending.remove(option);
        } else {
            this.pending.put(option, newValue);
        }
    }

    /** Every option of every category of this screen. */
    private List<Option> allOptions() {
        List<Option> all = new ArrayList<>();
        for (Category category : this.categories) {
            for (Section section : category.sections()) {
                all.addAll(section.options());
            }
        }
        return all;
    }

    /** Puts every option back to its default value (not saved until Apply / Done). */
    private void resetToDefaults() {
        for (Option option : allOptions()) {
            change(option, option.value().getDefault());
        }
    }

    private boolean allDefaults() {
        for (Option option : allOptions()) {
            if (!current(option).equals(option.value().getDefault())) return false;
        }
        return true;
    }

    /** "<" / ">" of a number option: one step down / up (it stops at the ends). */
    private void step(Option option, int direction) {
        int saved = (Integer) option.value().get();
        int[] steps = withValue(option.steps(), saved);
        int now = (Integer) current(option);
        int index = Arrays.binarySearch(steps, now);
        if (index < 0) index = 0;
        int next = Mth.clamp(index + direction, 0, steps.length - 1);
        change(option, steps[next]);
    }

    /** The steps plus the value the file already has (if it isn't one of them), sorted. */
    private static int[] withValue(int[] steps, int value) {
        for (int step : steps) {
            if (step == value) return steps;
        }
        int[] result = Arrays.copyOf(steps, steps.length + 1);
        result[steps.length] = value;
        Arrays.sort(result);
        return result;
    }

    /** "3 days", "5 min", "30 s"... or "Every 3 days" / "Every night". */
    private static Component format(Option option, int value) {
        if (option.unit() == Unit.NIGHTS) {
            return value == 1
                    ? Component.translatable("travelyourearth.config.unit.every_day")
                    : Component.translatable("travelyourearth.config.unit.every_days", value);
        }
        if (value % 24000 == 0) {
            int days = value / 24000;
            return days == 1
                    ? Component.translatable("travelyourearth.config.unit.day")
                    : Component.translatable("travelyourearth.config.unit.days", days);
        }
        if (value >= 1200) {
            return Component.translatable("travelyourearth.config.unit.minutes", trim(value / 1200.0));
        }
        return Component.translatable("travelyourearth.config.unit.seconds", trim(value / 20.0));
    }

    private static String trim(double number) {
        return number == Math.floor(number) ? String.valueOf((long) number) : String.format(Locale.ROOT, "%.1f", number);
    }

    /** Writes the changes into the config files (NeoForge reloads them by itself). */
    @SuppressWarnings("unchecked")
    private void save() {
        if (this.pending.isEmpty()) return;
        for (Map.Entry<Option, Object> change : this.pending.entrySet()) {
            ((ModConfigSpec.ConfigValue<Object>) change.getKey().value()).set(change.getValue());
        }
        for (ModConfigSpec spec : this.specs) {
            spec.save();
        }
        this.pending.clear();
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(this.parent);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(TravelYourEarth.MODID, path);
    }

    // =====================================================================
    // Sodium-style flat button
    // =====================================================================

    /** A flat, dark button with centered text: lighter with the mouse on it, gray text when inactive. */
    private class FlatButton extends Button {
        FlatButton(int x, int y, int width, int height, Component message, OnPress onPress) {
            super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        }

        @Override
        public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            int x = this.getX(), y = this.getY();
            boolean lit = this.active && this.isHovered();
            graphics.fill(x, y, x + this.width, y + this.height, lit ? FLAT_HOVER_COLOR : FLAT_COLOR);
            graphics.centeredText(font, this.getMessage(), x + this.width / 2, y + (this.height - 8) / 2,
                    this.active ? FLAT_TEXT : FLAT_TEXT_DISABLED);
        }
    }

    // =====================================================================
    // Fold arrow of the sidebar
    // =====================================================================

    /**
     * Small pixel triangle next to a category that has subcategories:
     * pointing down = unfolded (subcategories shown), pointing right = folded. White with the mouse on it.
     */
    private class ArrowButton extends Button {
        private final boolean folded;

        ArrowButton(int x, int y, boolean folded, OnPress onPress) {
            super(x, y, 10, 10, Component.translatable(folded ? "travelyourearth.config.unfold" : "travelyourearth.config.fold"),
                    onPress, DEFAULT_NARRATION);
            this.folded = folded;
        }

        @Override
        public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            int color = this.isHovered() ? 0xFFFFFFFF : 0xFFB0B0B0;
            int x = this.getX(), y = this.getY();
            if (this.folded) {
                // ▶  (3 wide, 5 tall)
                for (int i = 0; i < 3; i++) {
                    graphics.fill(x + 3 + i, y + 2 + i, x + 4 + i, y + 7 - i, color);
                }
            } else {
                // ▼  (5 wide, 3 tall)
                for (int i = 0; i < 3; i++) {
                    graphics.fill(x + 2 + i, y + 3 + i, x + 7 - i, y + 4 + i, color);
                }
            }
        }
    }

    // =====================================================================
    // Bedrock-style toggle
    // =====================================================================

    /**
     * The on/off switch with the 5 states you drew:
     * normal, _hover (mouse on it), _pressed (right after a click), _focused (selected with Tab), _disabled.
     */
    private class ToggleWidget extends Button {
        private final Option option;
        private long pressStart = -1;
        /** True when the focus came from a mouse click: then we don't show the white "keyboard" frame. */
        private boolean focusFromMouse;

        ToggleWidget(int x, int y, Option option) {
            super(x, y, TOGGLE_W, TOGGLE_H, Component.translatable(option.langKey()),
                    b -> ((ToggleWidget) b).flip(), DEFAULT_NARRATION);
            this.option = option;
        }

        private boolean on() {
            return (Boolean) current(this.option);
        }

        private void flip() {
            change(this.option, !on());
            this.pressStart = Util.getMillis();
            // Clicked with the mouse (not with Enter/Space): no keyboard frame afterwards
            this.focusFromMouse = this.isHovered();
        }

        @Override
        public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            if (!this.isFocused()) {
                this.focusFromMouse = false;
            }

            String state;
            if (!this.active) {
                state = "_disabled";
            } else if (this.pressStart >= 0 && Util.getMillis() - this.pressStart < PRESS_TIME) {
                state = "_pressed";
            } else if (this.isHovered()) {
                state = "_hover";
            } else if (this.isFocused() && !this.focusFromMouse) {
                state = "_focused";
            } else {
                state = "";
            }

            String sprite = "config/toggle_" + (on() ? "on" : "off") + state;
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, id(sprite), this.getX(), this.getY(), TOGGLE_W, TOGGLE_H);
        }
    }
}