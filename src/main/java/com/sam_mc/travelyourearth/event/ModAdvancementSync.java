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
 * Mantiene los logros del mod aunque cambies la opción "Mod Tab de logros".
 *
 * El problema: cada logro existe en DOS versiones con nombres distintos
 * (travelyourearth:adventure/... en "Aventura" y travelyourearth:ruby/... en la pestaña del mod)
 * y solo una está cargada. Minecraft guarda el progreso por nombre y BORRA el de los logros
 * que no están cargados, así que al cambiar la opción se perdía el progreso.
 *
 * La solución: una "libreta" propia guardada en el jugador (sobrevive a la muerte).
 *   - Al conseguir un logro del mod -> se anota su nombre "lógico" (obtain_ruby, fireproof).
 *   - Al entrar al mundo o con /reload -> se da la versión que esté cargada de cada logro anotado.
 */
@EventBusSubscriber(modid = TravelYourEarth.MODID)
public class ModAdvancementSync {

    /** Dónde se guarda la libreta dentro de los datos del jugador. */
    private static final String NOTEBOOK = TravelYourEarth.MODID + ":earned_advancements";

    /**
     * Nombre lógico -> todas sus versiones (la de Aventura y la de la pestaña del mod).
     * Si añades un logro nuevo con dos versiones, añádelo aquí también.
     */
    private static final Map<String, List<Identifier>> ADVANCEMENTS = Map.of(
            "obtain_ruby", List.of(
                    id("adventure/obtain_ruby"),
                    id("ruby/root"),          // raíz "Travel Your Earth" (se gana a la vez que ¿Volviste?)
                    id("ruby/obtain_ruby")),
            "fireproof", List.of(
                    id("adventure/fireproof"),
                    id("ruby/fireproof"))
    );

    // =========================================================================
    // 1) Anotar en la libreta cuando se consigue un logro del mod
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
    // 2) Sincronizar al entrar al mundo...
    // =========================================================================
    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            sync(player);
        }
    }

    // ...y después de /reload (la opción también se puede aplicar con /reload)
    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() == null) { // null = /reload para todos (al entrar ya lo hace onLogin)
            event.getRelevantPlayers().forEach(ModAdvancementSync::sync);
        }
    }

    private static void sync(ServerPlayer player) {
        MinecraftServer server = player.level().getServer();

        for (Map.Entry<String, List<Identifier>> entry : ADVANCEMENTS.entrySet()) {
            String name = entry.getKey();

            // a) ¿Ya tiene hecha alguna versión cargada? -> anotarlo
            //    (sirve para los jugadores que ganaron el logro antes de existir la libreta)
            for (Identifier id : entry.getValue()) {
                AdvancementHolder holder = server.getAdvancements().get(id);
                if (holder != null && player.getAdvancements().getOrStartProgress(holder).isDone()) {
                    write(player, name);
                }
            }

            // b) Si está anotado -> dar TODAS las versiones que estén cargadas y le falten
            if (read(player, name)) {
                for (Identifier id : entry.getValue()) {
                    AdvancementHolder holder = server.getAdvancements().get(id);
                    if (holder == null) continue; // esta versión no está cargada ahora

                    AdvancementProgress progress = player.getAdvancements().getOrStartProgress(holder);
                    if (!progress.isDone()) {
                        // Copia de la lista para no modificarla mientras se recorre
                        for (String criterion : new ArrayList<>(toList(progress.getRemainingCriteria()))) {
                            player.getAdvancements().award(holder, criterion);
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // Libreta: CompoundTag dentro de "PlayerPersisted" (NeoForge lo copia al morir)
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
    // Ayudas
    // =========================================================================
    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(TravelYourEarth.MODID, path);
    }

    /** De "travelyourearth:ruby/fireproof" saca "fireproof"; null si no es uno de estos logros. */
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
