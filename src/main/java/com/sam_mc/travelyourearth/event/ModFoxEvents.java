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
 * Zorros con rubí en la boca.
 *
 * Vanilla (wiki, Java): el 20% de los zorros aparece con algo en la boca y, de esos,
 * el 5% es esmeralda -> 1% de todos los zorros. Esto está escrito en Java dentro de
 * Fox#populateDefaultEquipmentSlots, NO en una loot table, así que no se puede hacer con datagen.
 *
 * Rubí: el 4% de los ítems de la boca -> ~0.8% de todos los zorros (un poquito menos que la esmeralda).
 * Como en vanilla, el zorro suelta al morir el ítem que lleva en la boca (100%).
 */
@EventBusSubscriber(modid = TravelYourEarth.MODID)
public class ModFoxEvents {

    /** Parte de los ítems de la boca que serán rubí (vanilla: esmeralda = 0.05). */
    private static final float RUBY_SHARE_OF_MOUTH_ITEMS = 0.02f;

    /** Marca guardada en el zorro para tirar el dado solo UNA vez en su vida. */
    private static final String CHECKED_TAG = TravelYourEarth.MODID + ":mouth_item_checked";

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof Fox fox)) return;

        // No usamos loadedFromDisk(): los zorros que aparecen al generar el mundo (la mayoría)
        // entran como "cargados del disco". Por eso guardamos una marca en el propio zorro.
        CompoundTag data = fox.getPersistentData();
        if (data.getBooleanOr(CHECKED_TAG, false)) return;
        data.putBoolean(CHECKED_TAG, true);

        ItemStack mouth = fox.getItemBySlot(EquipmentSlot.MAINHAND);
        // Solo reemplazamos lo que trae al aparecer (huevo, trigo, cuero, pluma, conejo...).
        // Nunca tocamos la esmeralda, así ella se queda en su 1% de vanilla.
        if (mouth.isEmpty() || mouth.is(Items.EMERALD)) return;

        // 0.04 / 0.95: así el rubí queda en el 4% del total de ítems de la boca
        float chance = RUBY_SHARE_OF_MOUTH_ITEMS / (1.0f - 0.05f);
        if (fox.getRandom().nextFloat() < chance) {
            fox.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.RUBY.get()));
        }
    }
}