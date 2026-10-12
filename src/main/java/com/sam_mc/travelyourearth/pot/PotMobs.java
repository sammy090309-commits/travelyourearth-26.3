package com.sam_mc.travelyourearth.pot;

import com.sam_mc.travelyourearth.attachment.ModAttachments;
import com.sam_mc.travelyourearth.tags.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;

/**
 * 1.2.1: mobs inside decorated pots.
 *
 * In CREATIVE, sneaking + using a spawn egg on a pot puts the mob inside the pot
 * (PotMobEvents). Like the normal item of the pot, only ONE mob type per pot.
 * When the pot breaks (pickaxe, arrow, explosion, piston...), the mobs come out alive
 * (DecoratedPotRemovalMixin): a little above the pot, and pushed in random directions if there's more than one.
 *
 * How many fit is decided by entity type tags (datagen: ModEntityTypeTagsProvider):
 * travelyourearth:pot_capacity/8, /4, /2, /1. Not in any tag = 1. In two tags = the lowest wins.
 * travelyourearth:cannot_enter_pot = never (Wither, Ender Dragon).
 */
public final class PotMobs {

    /** Mobs that aren't in any capacity tag. */
    public static final int DEFAULT_CAPACITY = 1;

    private PotMobs() {}

    /** How many mobs of this type fit in one pot (0 = it can't go in). */
    public static int capacity(EntityType<?> type) {
        Holder<EntityType<?>> holder = type.builtInRegistryHolder();
        if (holder.is(ModTags.EntityTypes.CANNOT_ENTER_POT)) return 0;
        // From the lowest to the highest: the lowest number wins
        if (holder.is(ModTags.EntityTypes.POT_CAPACITY_1)) return 1;
        if (holder.is(ModTags.EntityTypes.POT_CAPACITY_2)) return 2;
        if (holder.is(ModTags.EntityTypes.POT_CAPACITY_4)) return 4;
        if (holder.is(ModTags.EntityTypes.POT_CAPACITY_8)) return 8;
        return DEFAULT_CAPACITY;
    }

    /** The mobs stored in this pot (their spawn egg; count = how many). EMPTY if none. */
    public static ItemStack stored(DecoratedPotBlockEntity pot) {
        return pot.getData(ModAttachments.POT_MOBS);
    }

    /**
     * Tries to put one more mob (from this spawn egg) into the pot.
     * @return true if it went in, false if it doesn't fit (full, another mob type, or forbidden)
     */
    public static boolean tryInsert(DecoratedPotBlockEntity pot, ItemStack egg) {
        EntityType<?> type = SpawnEggItem.getType(egg);
        if (type == null) return false;

        int capacity = capacity(type);
        ItemStack inside = stored(pot);
        ItemStack result;
        if (inside.isEmpty()) {
            if (capacity < 1) return false;
            result = egg.copyWithCount(1);
        } else {
            // Same mob (and same egg data, e.g. a custom name) and still room
            if (!ItemStack.isSameItemSameComponents(inside, egg) || inside.getCount() >= capacity) return false;
            result = inside.copyWithCount(inside.getCount() + 1);
        }
        pot.setData(ModAttachments.POT_MOBS, result);
        pot.setChanged();
        return true;
    }

    /** 0..1: how full the pot is (for the pitch of the insert sound, like vanilla). */
    public static float fullness(DecoratedPotBlockEntity pot) {
        ItemStack inside = stored(pot);
        EntityType<?> type = SpawnEggItem.getType(inside);
        if (inside.isEmpty() || type == null) return 0F;
        return (float) inside.getCount() / Math.max(1, capacity(type));
    }

    /** The pot is being removed: the mobs come out alive (server only). */
    public static void release(DecoratedPotBlockEntity pot, ServerLevel level, BlockPos pos) {
        ItemStack inside = stored(pot);
        if (inside.isEmpty()) return;
        pot.setData(ModAttachments.POT_MOBS, ItemStack.EMPTY);

        EntityType<?> type = SpawnEggItem.getType(inside);
        if (type == null || !type.canSpawn(level)) return;
        boolean scatter = inside.getCount() > 1;
        for (int i = 0; i < inside.getCount(); i++) {
            // Same as using the egg: keeps its custom name / entity data
            Entity mob = type.spawn(level, inside.copyWithCount(1), null, pos, EntitySpawnReason.SPAWN_ITEM_USE, false, false);
            if (mob != null) {
                popOut(mob, pos, level.getRandom(), scatter);
            }
        }
    }

    // =========================================================================
    // "Popping out" of the pot
    // =========================================================================

    /** The mob appears this high above the bottom of the pot (it "springs" out of it). */
    private static final double POP_HEIGHT = 0.4D;
    /** Little jump up. */
    private static final double POP_UP_MIN = 0.25D;
    private static final double POP_UP_MAX = 0.35D;
    /** Sideways push when there's more than one (soft, like the items of a broken chest). */
    private static final double SCATTER_MIN = 0.12D;
    private static final double SCATTER_MAX = 0.22D;

    /**
     * Puts the mob a little above the pot and gives it a little jump.
     * scatter = true (more than one mob): each one is also pushed in a random direction,
     * so they spread around the pot instead of all being in the same spot.
     * Also used by SpawnMobsLootModifier (bats / silverfish of the trial chamber pots).
     */
    public static void popOut(Entity mob, BlockPos pos, RandomSource random, boolean scatter) {
        mob.setPos(pos.getX() + 0.5D, pos.getY() + POP_HEIGHT, pos.getZ() + 0.5D);

        double up = POP_UP_MIN + random.nextDouble() * (POP_UP_MAX - POP_UP_MIN);
        double x = 0.0D;
        double z = 0.0D;
        if (scatter) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double speed = SCATTER_MIN + random.nextDouble() * (SCATTER_MAX - SCATTER_MIN);
            x = Math.cos(angle) * speed;
            z = Math.sin(angle) * speed;
        }
        // push() also tells the clients about the new speed (needsSync)
        mob.push(x, up, z);
    }
}
