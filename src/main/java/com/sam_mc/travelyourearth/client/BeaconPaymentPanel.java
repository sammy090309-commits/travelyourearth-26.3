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
 * Faro: botón del libro (x=10, y=101) que abre un panel con TODOS los pagos del faro
 * (tag #minecraft:beacon_payment_items), con buscador y scroll.
 * Clic en un ítem -> pone 1 en el hueco de pago (si había otro, primero lo devuelve).
 *
 * Texturas (assets/travelyourearth/textures/gui/):
 *   beacon_payments.png                         139 x 138  (panel)
 *   sprites/beacon/payments_button.png              16 x 16  (libro)
 *   sprites/beacon/payments_button_highlighted.png  16 x 16  (libro con el ratón encima)
 */
@EventBusSubscriber(modid = TravelYourEarth.MODID, value = Dist.CLIENT)
public class BeaconPaymentPanel {

    // ---- Medidas del GUI del faro (vanilla) ----
    private static final int GUI_W = 230, GUI_H = 219;
    private static final int PAYMENT_SLOT = 0; // slot 0 = hueco de pago; 1..36 = inventario

    // ---- Botón del libro (pegado a la izquierda de la pestaña de minerales) ----
    private static final int BUTTON_X = 10, BUTTON_Y = 101;
    private static final int BUTTON_W = 16, BUTTON_H = 16;

    // ---- Panel (medidas de tu beacon_payments.png) ----
    private static final int PANEL_W = 139, PANEL_H = 138;
    private static final int TITLE_Y = 5;                       // franja verde: y 3..13
    private static final int SEARCH_X = 21, SEARCH_Y = 17;      // a la derecha de la lupa
    private static final int SEARCH_W = 111, SEARCH_H = 12;     // x 21..131
    private static final int GRID_X = 7, GRID_Y = 31;           // primera casilla
    private static final int CELL = 25, COLUMNS = 5, ROWS = 4;   // casillas como el libro de recetas
    private static final int ITEM_OFFSET = (CELL - 16) / 2;      // ítem centrado (4 px de margen)
    private static final int GAP = 2;                           // separación panel <-> GUI
    private static final int SHIFT = (PANEL_W + GAP) / 2;       // cuánto se corre el faro al abrir (como la mesa de crafteo)

    private static final Identifier PANEL_TEXTURE = id("textures/gui/beacon_payments.png");
    // Casillas del libro de recetas de vanilla (25x25)
    private static final Identifier SLOT_CRAFTABLE = Identifier.withDefaultNamespace("recipe_book/slot_craftable");
    private static final Identifier SLOT_UNCRAFTABLE = Identifier.withDefaultNamespace("recipe_book/slot_uncraftable");
    private static final Identifier BTN = id("beacon/payments_button");
    private static final Identifier BTN_HL = id("beacon/payments_button_highlighted");

    // ---- Estado ----
    private static boolean open = false;        // se recuerda entre faros
    private static boolean useShift = true;     // false si la ventana es tan estrecha que el panel va a la derecha
    private static String query = "";           // texto del buscador
    private static int scrollRow = 0;           // cuántas filas bajamos con la rueda
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
    // Crear los widgets al abrir el faro (o al cambiar el tamaño de la ventana)
    // =========================================================================
    @SubscribeEvent
    public static void onInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof BeaconScreen screen)) return;

        // Como el libro de recetas: si el panel cabe a la izquierda, al abrirlo el faro
        // se corre SHIFT px a la derecha para que panel + faro queden centrados.
        int centeredLeft = (screen.width - GUI_W) / 2;
        useShift = centeredLeft + SHIFT - GAP - PANEL_W >= 2;
        if (open && useShift) {
            shiftScreen(screen, SHIFT); // mueve el faro y sus botones (aún no están los nuestros)
        }

        AbstractContainerScreenAccessorMixin pos = (AbstractContainerScreenAccessorMixin) screen;
        int left = pos.travelyourearth$getLeftPos();
        int top = pos.travelyourearth$getTopPos();

        // Panel a la izquierda del GUI; si no cabe, a la derecha (sin mover el faro)
        panelX = useShift ? left - GAP - PANEL_W : left + GUI_W + GAP;
        panelY = top;

        allPayments = findPaymentItems();
        applyFilter();

        // 1) Fondo del panel (primero, para que quede debajo de todo)
        background = new PanelBackground(panelX, panelY);
        event.addListener(background);

        // 2) Buscador
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

        // 3) Casillas (siempre 5x4; cada una muestra el ítem que le toque según scroll/búsqueda)
        CELLS.clear();
        for (int i = 0; i < COLUMNS * ROWS; i++) {
            int x = panelX + GRID_X + (i % COLUMNS) * CELL;
            int y = panelY + GRID_Y + (i / COLUMNS) * CELL;
            PaymentButton cell = new PaymentButton(x, y, i, screen);
            CELLS.add(cell);
            event.addListener(cell);
        }

        // 4) Botón del libro
        event.addListener(new ToggleButton(left + BUTTON_X, top + BUTTON_Y, screen));

        updateVisibility();
    }

    // Rueda del ratón encima del panel = scroll
    @SubscribeEvent
    public static void onScroll(ScreenEvent.MouseScrolled.Pre event) {
        if (!(event.getScreen() instanceof BeaconScreen) || !open) return;
        if (!isOverPanel(event.getMouseX(), event.getMouseY())) return;

        int maxRow = Math.max(0, (filtered.size() + COLUMNS - 1) / COLUMNS - ROWS);
        scrollRow = Math.clamp(scrollRow - (int) Math.signum(event.getScrollDeltaY()), 0, maxRow);
        event.setCanceled(true);
    }

    // Mientras escribes en el buscador, las teclas (como la E) no cierran el faro
    @SubscribeEvent
    public static void onKey(ScreenEvent.KeyPressed.Pre event) {
        if (!(event.getScreen() instanceof BeaconScreen)) return;
        if (searchBox == null || !searchBox.isFocused() || !searchBox.visible) return;
        if (event.getKey() == InputConstants.KEY_ESCAPE) return; // ESC sigue cerrando

        searchBox.keyPressed(event.getKeyEvent());
        event.setCanceled(true);
    }

    // =========================================================================
    // Lógica
    // =========================================================================
    /**
     * Corre el faro dx píxeles: su posición (leftPos) y todos los botones de la pantalla
     * (poderes, confirmar, cancelar, y también los nuestros si ya existen).
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

    /** Todos los pagos del faro en orden (gemas, lingotes, otros). Ver BeaconPayments. */
    private static List<Item> findPaymentItems() {
        return BeaconPayments.ordered();
    }

    /** Filtra por el nombre (en el idioma del juego). */
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

    /** Pone 1 del ítem en el hueco de pago, con clics normales de inventario. */
    private static void putInPaymentSlot(BeaconScreen screen, Item item) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameMode == null || mc.player == null) return;

        BeaconMenu menu = screen.getMenu();
        if (!menu.getCarried().isEmpty()) return;
        Slot payment = menu.getSlot(PAYMENT_SLOT);
        if (payment.getItem().is(item)) return;
        int id = menu.containerId;

        // Si ya había otro pago, devolverlo al inventario (shift + clic)
        if (payment.hasItem()) {
            mc.gameMode.handleContainerInput(id, PAYMENT_SLOT, 0, ContainerInput.QUICK_MOVE, mc.player);
            if (payment.hasItem()) return; // inventario lleno
        }

        int source = findSlotWith(menu, item);
        if (source == -1) return;

        // coger el montón -> clic derecho en el hueco (deja 1) -> devolver el resto
        mc.gameMode.handleContainerInput(id, source, 0, ContainerInput.PICKUP, mc.player);
        mc.gameMode.handleContainerInput(id, PAYMENT_SLOT, 1, ContainerInput.PICKUP, mc.player);
        mc.gameMode.handleContainerInput(id, source, 0, ContainerInput.PICKUP, mc.player);
    }

    // =========================================================================
    // Widgets
    // =========================================================================

    /** Botón del libro (16x16): como el libro de recetas, se ve igual abierto o cerrado. */
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

    /** Fondo del panel + título. No recibe clics. */
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

    /** Casilla del panel. Muestra el ítem filtered[scroll*5 + index]. */
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

            // Como el libro de recetas: si no hay ítem para esta casilla, no se dibuja nada
            if (item == null) {
                this.active = false;
                return;
            }

            Minecraft mc = Minecraft.getInstance();
            ItemStack stack = new ItemStack(item);
            BeaconMenu menu = this.screen.getMenu();
            boolean beaconActive = menu.getLevels() > 0;   // tiene pirámide
            boolean hasItem = countInInventory(menu, item) > 0;
            this.active = beaconActive && hasItem;

            if (!beaconActive) {
                // Faro sin activar: casilla gris bloqueada, no se puede usar
                graphics.fill(x, y, x + CELL, y + CELL, 0xFF555555);
                graphics.fill(x, y, x + CELL - 1, y + 1, 0xFF373737);
                graphics.fill(x, y, x + 1, y + CELL - 1, 0xFF373737);
                graphics.fill(x + 1, y + CELL - 1, x + CELL, y + CELL, 0xFF8B8B8B);
                graphics.fill(x + CELL - 1, y + 1, x + CELL, y + CELL, 0xFF8B8B8B);
            } else if (hasItem) {
                // Lo tienes: casilla del libro de recetas (receta disponible)
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_CRAFTABLE, x, y, CELL, CELL);
            } else {
                // No lo tienes: casilla roja del libro de recetas (receta no disponible)
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_UNCRAFTABLE, x, y, CELL, CELL);
            }

            // Ítem centrado, sin números
            graphics.item(stack, x + ITEM_OFFSET, y + ITEM_OFFSET);

            if (!beaconActive) {
                // velo gris encima del ítem: bloqueado
                graphics.fill(x + 1, y + 1, x + CELL - 1, y + CELL - 1, 0x99404040);
            } else if (this.active && this.isHovered()) {
                // brillo al pasar el ratón
                graphics.fill(x + 1, y + 1, x + CELL - 1, y + CELL - 1, 0x80FFFFFF);
            }

            if (this.isHovered()) {
                graphics.setTooltipForNextFrame(mc.font, stack, mouseX, mouseY);
            }
        }
    }
}