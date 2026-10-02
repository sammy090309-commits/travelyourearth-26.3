package com.sam_mc.travelyourearth.event;

import com.sam_mc.travelyourearth.TravelYourEarth;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Keeps the mod advancements even if you change the "Mod Advancement Tab" option.
 *
 * The problem: each advancement exists in TWO versions with different names
 * (travelyourearth:adventure/... in "Adventure" and travelyourearth:ruby/... in the mod tab)
 * and only one is loaded. Minecraft saves progress by name and DELETES the progress of
 * advancements that aren't loaded, so changing the option used to lose the progress.
 *
 * The solution: our own "notebook" saved in the player (it survives death).
 *   - When a mod advancement is earned -> its "logical" name is written down (obtain_ruby, fireproof).
 *   - When joining the world or with /reload -> the loaded version of each written advancement is granted.
 */
@EventBusSubscriber(modid = TravelYourEarth.MODID)
public class ModAdvancementSync {

    /** Where the notebook is saved inside the player data. */
    private static final String NOTEBOOK = TravelYourEarth.MODID + ":earned_advancements";

    /**
     * Logical name -> all its versions (the Adventure one and the mod tab one).
     * If you add a new advancement with two versions, add it here too.
     */
    private static final Map<String, List<Identifier>> ADVANCEMENTS = Map.of(
            "obtain_ruby", List.of(
                    id("adventure/obtain_ruby"),
                    id("ruby/root"),          // "Travel Your Earth" root (earned together with "You're Back?")
                    id("ruby/obtain_ruby")),
            "fireproof", List.of(
                    id("adventure/fireproof"),
                    id("ruby/fireproof"))
    );

    // =========================================================================
    // 1) Write it in the notebook when a mod advancement is earned
    // =========================================================================
    @SubscribeEvent
    public static void onAdvancementEarned(AdvancementEvent.AdvancementEarnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        String name = logicalName(event.getAdvancement().id());
        if (name != null) {
            write(player, name);
        }
    }

    // =========================================================================
    // 2) Sync when joining the world...
    // =========================================================================
    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            sync(player);
        }
    }

    // ...and after /reload (the option can also be applied with /reload)
    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() == null) { // null = /reload for everyone (onLogin already handles joining)
            event.getRelevantPlayers().forEach(ModAdvancementSync::sync);
        }
    }

    private static void sync(ServerPlayer player) {
        MinecraftServer server = player.level().getServer();

        for (Map.Entry<String, List<Identifier>> entry : ADVANCEMENTS.entrySet()) {
            String name = entry.getKey();

            // a) Has it already completed a loaded version? -> write it down
            //    (useful for players who earned the advancement before the notebook existed)
            for (Identifier id : entry.getValue()) {
                AdvancementHolder holder = server.getAdvancements().get(id);
                if (holder != null && player.getAdvancements().getOrStartProgress(holder).isDone()) {
                    write(player, name);
                }
            }

            // b) If it's written down -> grant ALL the loaded versions it's missing
            if (read(player, name)) {
                for (Identifier id : entry.getValue()) {
                    AdvancementHolder holder = server.getAdvancements().get(id);
                    if (holder == null) continue; // this version isn't loaded right now

                    AdvancementProgress progress = player.getAdvancements().getOrStartProgress(holder);
                    if (!progress.isDone()) {
                        // Copy of the list so it isn't modified while looping over it
                        for (String criterion : new ArrayList<>(toList(progress.getRemainingCriteria()))) {
                            player.getAdvancements().award(holder, criterion);
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // Notebook: CompoundTag inside "PlayerPersisted" (NeoForge copies it on death)
    // =========================================================================
    private static boolean read(Player player, String name) {
        CompoundTag persisted = player.getPersistentData().getCompoundOrEmpty(Player.PERSISTED_NBT_TAG);
        return persisted.getCompoundOrEmpty(NOTEBOOK).getBooleanOr(name, false);
    }

    private static void write(Player player, String name) {
        CompoundTag data = player.getPersistentData();
        CompoundTag persisted = data.getCompoundOrEmpty(Player.PERSISTED_NBT_TAG);
        CompoundTag notebook = persisted.getCompoundOrEmpty(NOTEBOOK);
        notebook.putBoolean(name, true);
        persisted.put(NOTEBOOK, notebook);
        data.put(Player.PERSISTED_NBT_TAG, persisted);
    }

    // =========================================================================
    // Helpers
    // =========================================================================
    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(TravelYourEarth.MODID, path);
    }

    /** From "travelyourearth:ruby/fireproof" gets "fireproof"; null if it isn't one of these advancements. */
    private static String logicalName(Identifier id) {
        for (Map.Entry<String, List<Identifier>> entry : ADVANCEMENTS.entrySet()) {
            if (entry.getValue().contains(id)) return entry.getKey();
        }
        return null;
    }

    private static List<String> toList(Iterable<String> criteria) {
        List<String> list = new ArrayList<>();
        criteria.forEach(list::add);
        return list;
    }
}