package com.sam_mc.travelyourearth.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.sam_mc.travelyourearth.TravelYourEarth;
import com.sam_mc.travelyourearth.mixin.AbstractContainerScreenAccessorMixin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.BeaconScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Beacon: a book button (x=10, y=101) that opens a panel with ALL beacon payment items
 * (tag #minecraft:beacon_payment_items), with a search box and scrolling.
 * Clicking an item puts 1 of it in the payment slot (if another item was there, it is returned first).
 *
 * Textures (assets/travelyourearth/textures/gui/):
 *   beacon_payments.png                             139 x 138  (panel)
 *   sprites/beacon/payments_button.png               16 x 16   (book)
 *   sprites/beacon/payments_button_highlighted.png   16 x 16   (book while hovered)
 */
@EventBusSubscriber(modid = TravelYourEarth.MODID, value = Dist.CLIENT)
public class BeaconPaymentPanel {

    // =========================================================================
    // Constants
    // =========================================================================

    // ---- Vanilla beacon GUI size ----
    private static final int GUI_W = 230, GUI_H = 219;
    private static final int PAYMENT_SLOT = 0; // slot 0 = payment slot; 1..36 = inventory

    // ---- Book button (next to the left of the minerals tab) ----
    private static final int BUTTON_X = 10, BUTTON_Y = 101;
    private static final int BUTTON_W = 16, BUTTON_H = 16;

    // ---- Panel (sizes from beacon_payments.png) ----
    private static final int PANEL_W = 139, PANEL_H = 138;
    private static final int TITLE_Y = 5;                        // green strip: y 3..13
    private static final int SEARCH_X = 21, SEARCH_Y = 17;       // right of the magnifying glass
    private static final int SEARCH_W = 111, SEARCH_H = 12;      // x 21..131
    private static final int GRID_X = 7, GRID_Y = 31;            // first cell
    private static final int CELL = 25, COLUMNS = 5, ROWS = 4;   // cells like the recipe book
    private static final int ITEM_OFFSET = (CELL - 16) / 2;      // centered item (4 px margin)
    private static final int GAP = 2;                            // space between panel and GUI
    private static final int SHIFT = (PANEL_W + GAP) / 2;        // how far the beacon moves when opened (like the crafting table)

    // ---- Textures ----
    private static final Identifier PANEL_TEXTURE = id("textures/gui/beacon_payments.png");
    // Vanilla recipe book cells (25x25)
    private static final Identifier SLOT_CRAFTABLE = Identifier.withDefaultNamespace("recipe_book/slot_craftable");
    private static final Identifier SLOT_UNCRAFTABLE = Identifier.withDefaultNamespace("recipe_book/slot_uncraftable");
    private static final Identifier BTN = id("beacon/payments_button");
    private static final Identifier BTN_HL = id("beacon/payments_button_highlighted");

    // =========================================================================
    // State
    // =========================================================================

    private static boolean open = false;        // remembered between beacons
    private static boolean useShift = true;     // false if the window is so narrow that the panel goes to the right
    private static String query = "";           // search box text
    private static int scrollRow = 0;           // how many rows we scrolled down with the mouse wheel
    private static List<Item> allPayments = List.of();
    private static List<Item> filtered = List.of();

    private static int panelX, panelY;
    private static PanelBackground background;
    private static EditBox searchBox;
    private static final List<PaymentButton> CELLS = new ArrayList<>();

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(TravelYourEarth.MODID, path);
    }

    // =========================================================================
    // Events
    // =========================================================================

    /** Creates the widgets when the beacon opens (or when the window is resized). */
    @SubscribeEvent
    public static void onInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof BeaconScreen screen)) return;

        // Like the recipe book: if the panel fits on the left, opening it moves the beacon
        // SHIFT px to the right so that panel + beacon stay centered.
        int centeredLeft = (screen.width - GUI_W) / 2;
        useShift = centeredLeft + SHIFT - GAP - PANEL_W >= 2;
        if (open && useShift) {
            shiftScreen(screen, SHIFT); // moves the beacon and its buttons (ours don't exist yet)
        }

        AbstractContainerScreenAccessorMixin pos = (AbstractContainerScreenAccessorMixin) screen;
        int left = pos.travelyourearth$getLeftPos();
        int top = pos.travelyourearth$getTopPos();

        // Panel on the left of the GUI; if it doesn't fit, on the right (without moving the beacon)
        panelX = useShift ? left - GAP - PANEL_W : left + GUI_W + GAP;
        panelY = top;

        allPayments = findPaymentItems();
        applyFilter();

        // 1) Panel background (first, so it stays below everything)
        background = new PanelBackground(panelX, panelY);
        event.addListener(background);

        // 2) Search box
        Minecraft mc = Minecraft.getInstance();
        searchBox = new EditBox(mc.font, panelX + SEARCH_X, panelY + SEARCH_Y, SEARCH_W, SEARCH_H,
                Component.translatable("gui." + TravelYourEarth.MODID + ".beacon_payments.search"));
        searchBox.setMaxLength(50);
        searchBox.setHint(Component.translatable("gui." + TravelYourEarth.MODID + ".beacon_payments.search"));
        searchBox.setValue(query);
        searchBox.setResponder(text -> {
            query = text;
            scrollRow = 0;
            applyFilter();
        });
        event.addListener(searchBox);

        // 3) Cells (always 5x4; each one shows the item for its position based on scroll/search)
        CELLS.clear();
        for (int i = 0; i < COLUMNS * ROWS; i++) {
            int x = panelX + GRID_X + (i % COLUMNS) * CELL;
            int y = panelY + GRID_Y + (i / COLUMNS) * CELL;
            PaymentButton cell = new PaymentButton(x, y, i, screen);
            CELLS.add(cell);
            event.addListener(cell);
        }

        // 4) Book button
        event.addListener(new ToggleButton(left + BUTTON_X, top + BUTTON_Y, screen));

        updateVisibility();
    }

    /** Mouse wheel over the panel = scroll. */
    @SubscribeEvent
    public static void onScroll(ScreenEvent.MouseScrolled.Pre event) {
        if (!(event.getScreen() instanceof BeaconScreen) || !open) return;
        if (!isOverPanel(event.getMouseX(), event.getMouseY())) return;

        int maxRow = Math.max(0, (filtered.size() + COLUMNS - 1) / COLUMNS - ROWS);
        scrollRow = Math.clamp(scrollRow - (int) Math.signum(event.getScrollDeltaY()), 0, maxRow);
        event.setCanceled(true);
    }

    /** While typing in the search box, keys (like E) don't close the beacon. */
    @SubscribeEvent
    public static void onKey(ScreenEvent.KeyPressed.Pre event) {
        if (!(event.getScreen() instanceof BeaconScreen)) return;
        if (searchBox == null || !searchBox.isFocused() || !searchBox.visible) return;
        if (event.getKey() == InputConstants.KEY_ESCAPE) return; // ESC still closes it

        searchBox.keyPressed(event.getKeyEvent());
        event.setCanceled(true);
    }

    // =========================================================================
    // Logic
    // =========================================================================

    /**
     * Moves the beacon dx pixels: its position (leftPos) and every button on the screen
     * (powers, confirm, cancel, and ours too if they already exist).
     */
    private static void shiftScreen(BeaconScreen screen, int dx) {
        AbstractContainerScreenAccessorMixin accessor = (AbstractContainerScreenAccessorMixin) screen;
        accessor.travelyourearth$setLeftPos(accessor.travelyourearth$getLeftPos() + dx);
        for (GuiEventListener child : screen.children()) {
            if (child instanceof AbstractWidget widget) {
                widget.setX(widget.getX() + dx);
            }
        }
        panelX += dx;
    }

    private static void updateVisibility() {
        if (background != null) background.visible = open;
        if (searchBox != null) {
            searchBox.visible = open;
            if (!open) searchBox.setFocused(false);
        }
        for (PaymentButton cell : CELLS) cell.visible = open;
    }

    private static boolean isOverPanel(double mx, double my) {
        return mx >= panelX && mx < panelX + PANEL_W && my >= panelY && my < panelY + PANEL_H;
    }

    /** All beacon payments in order (gems, ingots, others). See BeaconPayments. */
    private static List<Item> findPaymentItems() {
        return BeaconPayments.ordered();
    }

    /** Filters by name (in the game's language). */
    private static void applyFilter() {
        String q = query.trim().toLowerCase(Locale.ROOT);
        if (q.isEmpty()) {
            filtered = allPayments;
            return;
        }
        List<Item> result = new ArrayList<>();
        for (Item item : allPayments) {
            String name = new ItemStack(item).getHoverName().getString().toLowerCase(Locale.ROOT);
            if (name.contains(q)) result.add(item);
        }
        filtered = result;
    }

    private static int countInInventory(BeaconMenu menu, Item item) {
        int total = 0;
        for (int i = PAYMENT_SLOT + 1; i < menu.slots.size(); i++) {
            ItemStack stack = menu.slots.get(i).getItem();
            if (stack.is(item)) total += stack.getCount();
        }
        return total;
    }

    private static int findSlotWith(BeaconMenu menu, Item item) {
        for (int i = PAYMENT_SLOT + 1; i < menu.slots.size(); i++) {
            if (menu.slots.get(i).getItem().is(item)) return i;
        }
        return -1;
    }

    /** Puts 1 of the item in the payment slot, using normal inventory clicks. */
    private static void putInPaymentSlot(BeaconScreen screen, Item item) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameMode == null || mc.player == null) return;

        BeaconMenu menu = screen.getMenu();
        if (!menu.getCarried().isEmpty()) return;
        Slot payment = menu.getSlot(PAYMENT_SLOT);
        if (payment.getItem().is(item)) return;
        int id = menu.containerId;

        // If there was already another payment, return it to the inventory (shift + click)
        if (payment.hasItem()) {
            mc.gameMode.handleContainerInput(id, PAYMENT_SLOT, 0, ContainerInput.QUICK_MOVE, mc.player);
            if (payment.hasItem()) return; // inventory full
        }

        int source = findSlotWith(menu, item);
        if (source == -1) return;

        // pick up the stack -> right click on the slot (leaves 1) -> put the rest back
        mc.gameMode.handleContainerInput(id, source, 0, ContainerInput.PICKUP, mc.player);
        mc.gameMode.handleContainerInput(id, PAYMENT_SLOT, 1, ContainerInput.PICKUP, mc.player);
        mc.gameMode.handleContainerInput(id, source, 0, ContainerInput.PICKUP, mc.player);
    }

    // =========================================================================
    // Widgets
    // =========================================================================

    /** Book button (16x16): like the recipe book, it looks the same open or closed. */
    private static class ToggleButton extends Button {
        ToggleButton(int x, int y, BeaconScreen screen) {
            super(x, y, BUTTON_W, BUTTON_H,
                    Component.translatable("gui." + TravelYourEarth.MODID + ".beacon_payments"),
                    b -> {
                        open = !open;
                        if (useShift) {
                            shiftScreen(screen, open ? SHIFT : -SHIFT);
                        }
                        updateVisibility();
                    }, DEFAULT_NARRATION);
        }

        @Override
        public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            boolean hl = this.isHoveredOrFocused();
            Identifier sprite = hl ? BTN_HL : BTN;
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, this.getX(), this.getY(), BUTTON_W, BUTTON_H);
        }
    }

    /** Panel background + title. Doesn't receive clicks. */
    private static class PanelBackground extends AbstractWidget {
        PanelBackground(int x, int y) {
            super(x, y, PANEL_W, PANEL_H, Component.empty());
            this.active = false;
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, PANEL_TEXTURE, this.getX(), this.getY(),
                    0.0F, 0.0F, PANEL_W, PANEL_H, PANEL_W, PANEL_H);
            Component title = Component.translatable("gui." + TravelYourEarth.MODID + ".beacon_payments");
            graphics.centeredText(Minecraft.getInstance().font, title.getVisualOrderText(),
                    this.getX() + PANEL_W / 2, this.getY() + TITLE_Y, 0xFFFFFFFF);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {}

        @Override
        public boolean isActive() {
            return false;
        }
    }

    /** Panel cell. Shows the item filtered[scroll * 5 + index]. */
    private static class PaymentButton extends Button {
        private final int index;
        private final BeaconScreen screen;

        PaymentButton(int x, int y, int index, BeaconScreen screen) {
            super(x, y, CELL, CELL, Component.empty(), b -> {
                Item item = ((PaymentButton) b).currentItem();
                if (item != null) putInPaymentSlot(((PaymentButton) b).screen, item);
            }, DEFAULT_NARRATION);
            this.index = index;
            this.screen = screen;
        }

        Item currentItem() {
            int i = scrollRow * COLUMNS + this.index;
            return i < filtered.size() ? filtered.get(i) : null;
        }

        @Override
        public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            Item item = currentItem();
            int x = this.getX(), y = this.getY();

            // Like the recipe book: if this cell has no item, nothing is drawn
            if (item == null) {
                this.active = false;
                return;
            }

            Minecraft mc = Minecraft.getInstance();
            ItemStack stack = new ItemStack(item);
            BeaconMenu menu = this.screen.getMenu();
            boolean beaconActive = menu.getLevels() > 0;   // has a pyramid
            boolean hasItem = countInInventory(menu, item) > 0;
            this.active = beaconActive && hasItem;

            if (!beaconActive) {
                // Inactive beacon: locked gray cell, can't be used
                graphics.fill(x, y, x + CELL, y + CELL, 0xFF555555);
                graphics.fill(x, y, x + CELL - 1, y + 1, 0xFF373737);
                graphics.fill(x, y, x + 1, y + CELL - 1, 0xFF373737);
                graphics.fill(x + 1, y + CELL - 1, x + CELL, y + CELL, 0xFF8B8B8B);
                graphics.fill(x + CELL - 1, y + 1, x + CELL, y + CELL, 0xFF8B8B8B);
            } else if (hasItem) {
                // You have it: recipe book cell (recipe available)
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_CRAFTABLE, x, y, CELL, CELL);
            } else {
                // You don't have it: red recipe book cell (recipe not available)
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_UNCRAFTABLE, x, y, CELL, CELL);
            }

            // Centered item, without numbers
            graphics.item(stack, x + ITEM_OFFSET, y + ITEM_OFFSET);

            if (!beaconActive) {
                // gray overlay on top of the item: locked
                graphics.fill(x + 1, y + 1, x + CELL - 1, y + CELL - 1, 0x99404040);
            } else if (this.active && this.isHovered()) {
                // highlight while hovering
                graphics.fill(x + 1, y + 1, x + CELL - 1, y + CELL - 1, 0x80FFFFFF);
            }

            if (this.isHovered()) {
                graphics.setTooltipForNextFrame(mc.font, stack, mouseX, mouseY);
            }
        }
    }
}