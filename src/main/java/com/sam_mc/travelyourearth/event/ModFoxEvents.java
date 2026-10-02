package com.sam_mc.travelyourearth.event;

import com.sam_mc.travelyourearth.TravelYourEarth;
import com.sam_mc.travelyourearth.item.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

/**
 * Foxes with a ruby in their mouth.
 *
 * Vanilla (wiki, Java): 20% of foxes spawn with something in their mouth and, of those,
 * 5% is an emerald -> 1% of all foxes. This is written in Java inside
 * Fox#populateDefaultEquipmentSlots, NOT in a loot table, so it can't be done with datagen.
 *
 * Ruby: 2% of the mouth items -> ~0.4% of all foxes (less than the emerald).
 * Like in vanilla, the fox drops the item in its mouth when it dies (100%).
 */
@EventBusSubscriber(modid = TravelYourEarth.MODID)
public class ModFoxEvents {

    /** Share of the mouth items that will be ruby (vanilla: emerald = 0.05). */
    private static final float RUBY_SHARE_OF_MOUTH_ITEMS = 0.02f;

    /** Mark saved on the fox so the dice is rolled only ONCE in its life. */
    private static final String CHECKED_TAG = TravelYourEarth.MODID + ":mouth_item_checked";

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof Fox fox)) return;

        // We don't use loadedFromDisk(): foxes that spawn during world generation (most of them)
        // join as "loaded from disk". That's why we save a mark on the fox itself.
        CompoundTag data = fox.getPersistentData();
        if (data.getBooleanOr(CHECKED_TAG, false)) return;
        data.putBoolean(CHECKED_TAG, true);

        ItemStack mouth = fox.getItemBySlot(EquipmentSlot.MAINHAND);
        // We only replace what it brings when spawning (egg, wheat, leather, feather, rabbit...).
        // We never touch the emerald, so it stays at its vanilla 1%.
        if (mouth.isEmpty() || mouth.is(Items.EMERALD)) return;

        // 0.02 / 0.95: this way the ruby ends up as 2% of all mouth items
        float chance = RUBY_SHARE_OF_MOUTH_ITEMS / (1.0f - 0.05f);
        if (fox.getRandom().nextFloat() < chance) {
            fox.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.RUBY.get()));
        }
    }
}