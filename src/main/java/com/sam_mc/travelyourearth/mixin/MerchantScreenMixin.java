package com.sam_mc.travelyourearth.mixin;

import com.sam_mc.travelyourearth.Config;
import com.sam_mc.travelyourearth.TravelYourEarth;
import com.sam_mc.travelyourearth.block.ModBlocks;
import com.sam_mc.travelyourearth.item.ModItems;
import com.sam_mc.travelyourearth.event.WanderingSmugglerEvents;
import com.sam_mc.travelyourearth.villager.IllegalVillagers;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.renderer.RenderPipelines;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Dark button for the illegal trade (1.2).
 *
 * Vanilla draws the 7 trade buttons (the normal "widget/button" sprite, shared by every button
 * in the game) and THEN the items of each offer. We draw our dark sprite right in between:
 * over the button of the illegal offer, and under its items.
 *
 * How we know which offer is illegal: golden rule of the mod -> villagers never ask for rubies
 * in normal trades, so an offer paid with rubies (or ruby blocks) is always an illegal one.
 *
 * Client options (Config -> Client): "Dark illegal trades" (the dark button) and
 * "Raid warning" (the warning line in the tooltip).
 */
@Mixin(MerchantScreen.class)
public abstract class MerchantScreenMixin extends AbstractContainerScreen<MerchantMenu> {

    @Unique private static final Identifier TRAVELYOUREARTH$ILLEGAL =
            Identifier.fromNamespaceAndPath(TravelYourEarth.MODID, "widget/illegal_trade");
    @Unique private static final Identifier TRAVELYOUREARTH$ILLEGAL_HIGHLIGHTED =
            Identifier.fromNamespaceAndPath(TravelYourEarth.MODID, "widget/illegal_trade_highlighted");
    @Unique private static final Identifier TRAVELYOUREARTH$ILLEGAL_DISABLED =
            Identifier.fromNamespaceAndPath(TravelYourEarth.MODID, "widget/illegal_trade_disabled");
    /** Grayer arrow for the illegal trade (10x9, like vanilla's container/villager/trade_arrow). */
    @Unique private static final Identifier TRAVELYOUREARTH$ILLEGAL_ARROW =
            Identifier.fromNamespaceAndPath(TravelYourEarth.MODID, "container/villager/illegal_trade_arrow");
    @Unique private static final Identifier TRAVELYOUREARTH$ILLEGAL_ARROW_OUT_OF_STOCK =
            Identifier.fromNamespaceAndPath(TravelYourEarth.MODID, "container/villager/illegal_trade_arrow_out_of_stock");

    // Same numbers as vanilla MerchantScreen
    @Unique private static final int TRAVELYOUREARTH$BUTTONS = 7;
    @Unique private static final int TRAVELYOUREARTH$BUTTON_X = 5;
    @Unique private static final int TRAVELYOUREARTH$FIRST_BUTTON_Y = 18; // 16 + 2
    @Unique private static final int TRAVELYOUREARTH$BUTTON_WIDTH = 88;
    @Unique private static final int TRAVELYOUREARTH$BUTTON_HEIGHT = 20;

    @Shadow private int scrollOff;

    private MerchantScreenMixin(MerchantMenu menu, Inventory inventory, Component title, int width, int height) {
        super(menu, inventory, title, width, height);
    }

    /** Right after the buttons are drawn and before the items: paint the dark button. */
    @Inject(method = "extractContents",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/inventory/MerchantScreen;extractScroller(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIIILnet/minecraft/world/item/trading/MerchantOffers;)V"))
    private void travelyourearth$drawIllegalButtons(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        MerchantOffers offers = this.menu.getOffers();
        int xo = (this.width - this.imageWidth) / 2;
        int yo = (this.height - this.imageHeight) / 2;
        int firstOffer = offers.size() > TRAVELYOUREARTH$BUTTONS ? this.scrollOff : 0;
        // Wandering smuggler: its RUBY trades are dark (no raid warning); the emerald ones stay vanilla
        boolean smuggler = travelyourearth$tradingWithSmuggler();

        for (int row = 0; row < TRAVELYOUREARTH$BUTTONS; row++) {
            int index = firstOffer + row;
            if (index >= offers.size()) break;

            MerchantOffer offer = offers.get(index);
            boolean illegal = travelyourearth$isIllegal(offer);
            if (!illegal && !(smuggler && travelyourearth$usesRubies(offer))) continue;

            int x = xo + TRAVELYOUREARTH$BUTTON_X;
            int y = yo + TRAVELYOUREARTH$FIRST_BUTTON_Y + row * TRAVELYOUREARTH$BUTTON_HEIGHT;
            boolean hovered = mouseX >= x && mouseX < x + TRAVELYOUREARTH$BUTTON_WIDTH
                    && mouseY >= y && mouseY < y + TRAVELYOUREARTH$BUTTON_HEIGHT;

            // "Dark illegal trades" off: the button stays like a normal one (the tooltip still works)
            if (Config.DARK_ILLEGAL_TRADES.get()) {
                Identifier sprite = offer.isOutOfStock() ? TRAVELYOUREARTH$ILLEGAL_DISABLED      // no XP / Peaceful
                        : hovered ? TRAVELYOUREARTH$ILLEGAL_HIGHLIGHTED
                        : TRAVELYOUREARTH$ILLEGAL;
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y,
                        TRAVELYOUREARTH$BUTTON_WIDTH, TRAVELYOUREARTH$BUTTON_HEIGHT);
            }

            if (hovered && illegal) {
                travelyourearth$xpTooltip(graphics, offer, mouseX, mouseY);
            }
        }
    }

    /** Muted colors for the tooltip (less bright than the normal gold / red). */
    @Unique private static final int TRAVELYOUREARTH$WARNING_COLOR = 0xD08E1C;   // muted gold (a bit saturated)

    /**
     * Tooltip of the illegal trade (it replaces vanilla's item tooltip on the illegal buttons),
     * laid out like vanilla's attribute lines ("When in Main Hand:"):
     *
     *   Enchanted Book
     *   Harpoon III                              <- the item's normal tooltip (enchantments, effects...)
     *
     *   Illegal deal: it can start a raid        <- muted gold (you have the XP levels)
     *   Level Requirement: 2                     <- red, like the enchanting table (you DON'T have them)
     *   minecraft:enchanted_book                 <- F3 + H gray lines, always last
     * (The XP cost travels in the offer's XP field, see IllegalTrades.offer.)
     */
    @Unique
    private void travelyourearth$xpTooltip(GuiGraphicsExtractor graphics, MerchantOffer offer, int mouseX, int mouseY) {
        int levels = offer.getXp();
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        // The item's FULL vanilla tooltip (name, enchantments, potion effects, lore...),
        // the same one you see when you hover a normal trade or an item in your inventory.
        // With advanced tooltips on (F3 + H) vanilla adds gray lines at the END (item id,
        // "15 component(s)", durability): we split them off to put them BELOW our warning.
        Minecraft minecraft = Minecraft.getInstance();
        ItemStack result = offer.getResult();
        List<Component> full = Screen.getTooltipFromItem(minecraft, result);
        List<Component> normal = result.getTooltipLines(Item.TooltipContext.of(minecraft.level), player, TooltipFlag.NORMAL);
        int split = Math.min(normal.size(), full.size());
        List<Component> lines = new ArrayList<>(full.subList(0, split));
        List<Component> advancedLines = full.subList(split, full.size());
        lines.add(Component.empty()); // blank line before our warning, like vanilla's attribute sections

        boolean enough = levels <= 0 || player.getAbilities().instabuild || player.experienceLevel >= levels;
        // "Raid warning" off and you have the XP: nothing to add, vanilla's normal item tooltip shows instead
        if (enough && !Config.ILLEGAL_RAID_WARNING.get()) return;
        if (enough) {
            // You can pay it: only the warning
            lines.add(Component.translatable("gui.travelyourearth.illegal_trade.raid_warning")
                    .withColor(TRAVELYOUREARTH$WARNING_COLOR));
        } else {
            // Same line as the enchanting table: "Level Requirement: 6" in red (vanilla translation)
            lines.add(Component.translatable("container.enchant.level.requirement", levels)
                    .withStyle(ChatFormatting.RED));
        }
        lines.addAll(advancedLines); // F3 + H gray lines always at the very bottom, like vanilla
        graphics.setComponentTooltipForNextFrame(this.font, lines, mouseX, mouseY);
    }

    /**
     * "Farmer - Master" -> "Farmer - Trafficker" while it shows you its secret (same moment as the ruby
     * badge: the illegal trades are only in the list while the secret is shown).
     * Vanilla builds the level name with Component.translatable("merchant.level." + level).
     */
    @WrapOperation(method = "extractLabels",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;"),
            require = 0)
    private MutableComponent travelyourearth$traffickerLevel(String key, Operation<MutableComponent> original) {
        if (key.equals("merchant.level.5") && travelyourearth$showsSecret()) {
            return Component.translatable("merchant.level.travelyourearth.trafficker");
        }
        return original.call(key);
    }

    @Unique
    private boolean travelyourearth$showsSecret() {
        for (MerchantOffer offer : this.menu.getOffers()) {
            if (travelyourearth$isIllegal(offer)) return true;
        }
        return false;
    }

    /** Illegal trades: paid with rubies or ruby blocks, with price multiplier 0... and never in the savanna. */
    @Unique
    private static boolean travelyourearth$isIllegal(MerchantOffer offer) {
        // Same rule as IllegalVillagers.isIllegalOffer: rubies + price multiplier 0
        boolean paidWithRubies = offer.getBaseCostA().is(ModItems.RUBY.get())
                || offer.getBaseCostA().is(ModBlocks.RUBY_BLOCK.get().asItem());
        if (!paidWithRubies || offer.getPriceMultiplier() != 0.0F) return false;
        // Wandering traders: their ruby trades have no side effects (no raid, no XP)
        Entity trading = travelyourearth$tradingEntity();
        if (trading != null && WanderingSmugglerEvents.isWanderingTrader(trading)) return false;
        // The client may not receive the real price multiplier (it can arrive as 0 for EVERY trade),
        // so a legal savanna ruby trade could look illegal. In the savanna the ruby is legal:
        // if we're trading with a savanna villager, it's never dark.
        return !travelyourearth$tradingWithSavannaVillager();
    }

    /**
     * The merchant we're trading with: while the trade screen is open the camera can't move,
     * so it's the entity under the crosshair (the one we right-clicked).
     */
    @Unique
    private static Entity travelyourearth$tradingEntity() {
        Minecraft minecraft = Minecraft.getInstance();
        Entity looked = minecraft.crosshairPickEntity;
        if (looked instanceof Villager || (looked != null && WanderingSmugglerEvents.isWanderingTrader(looked))) {
            return looked;
        }
        // Backup: the closest villager / wandering trader within 8 blocks
        Player player = minecraft.player;
        if (player == null) return null;
        Entity closest = null;
        double best = Double.MAX_VALUE;
        for (Entity entity : player.level().getEntities(player, player.getBoundingBox().inflate(8.0),
                e -> e instanceof Villager || WanderingSmugglerEvents.isWanderingTrader(e))) {
            double distance = entity.distanceToSqr(player);
            if (distance < best) {
                best = distance;
                closest = entity;
            }
        }
        return closest;
    }

    /** True if the offer asks for rubies (or ruby blocks) or gives them. */
    @Unique
    private static boolean travelyourearth$usesRubies(MerchantOffer offer) {
        return travelyourearth$isRuby(offer.getBaseCostA())
                || travelyourearth$isRuby(offer.getCostB())
                || travelyourearth$isRuby(offer.getResult());
    }

    @Unique
    private static boolean travelyourearth$isRuby(ItemStack stack) {
        return stack.is(ModItems.RUBY.get()) || stack.is(ModBlocks.RUBY_BLOCK.get().asItem());
    }

    /**
     * True if we're trading with a wandering trader. We DON'T wait for the synced "smuggler" data:
     * it can arrive a moment after the screen opens (gray first, dark after reopening).
     * Only smugglers have ruby trades, so "wandering trader + trade with rubies" is enough,
     * and it's right from the very first frame.
     */
    @Unique
    private static boolean travelyourearth$tradingWithSmuggler() {
        Entity entity = travelyourearth$tradingEntity();
        return entity != null && WanderingSmugglerEvents.isWanderingTrader(entity);
    }

    /**
     * True if the villager we're trading with is from the savanna.
     * The screen doesn't know which villager it is, but while it's open the camera can't move,
     * so it's the villager under the crosshair (the one we right-clicked).
     * Backup: the closest villager within 8 blocks.
     */
    @Unique
    private static boolean travelyourearth$tradingWithSavannaVillager() {
        return travelyourearth$tradingEntity() instanceof Villager villager
                && IllegalVillagers.isSavannaVillager(villager);
    }

}